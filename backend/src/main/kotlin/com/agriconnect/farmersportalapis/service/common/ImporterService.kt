package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.eudr.*
import com.agriconnect.farmersportalapis.domain.hedera.HederaAccountCredentials
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import com.agriconnect.farmersportalapis.service.eudr.EudrVerificationService
import com.agriconnect.farmersportalapis.service.hedera.HederaAccountService
import com.agriconnect.farmersportalapis.service.hedera.HederaConsensusServices
import com.agriconnect.farmersportalapis.service.hedera.HederaTokenService
import com.hedera.hashgraph.sdk.AccountId
import com.hedera.hashgraph.sdk.Hbar
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
@Transactional
class ImporterService(
    private val importerRepository: ImporterRepository,
    private val importShipmentRepository: ImportShipmentRepository,
    private val customsDocumentRepository: CustomsDocumentRepository,
    private val inspectionRecordRepository: InspectionRecordRepository,
    private val userRepository: UserRepository,
    private val exporterRepository: ExporterRepository,
    private val hederaConsensusService: HederaConsensusServices,
    private val hederaAccountService: HederaAccountService,
    private val hederaAccountCredentialsRepository: HederaAccountCredentialsRepository,
    private val hederaTokenService: HederaTokenService,
    private val eudrVerificationService: EudrVerificationService,
    private val exporterImporterConnectionRepository: com.agriconnect.farmersportalapis.repository.ExporterImporterConnectionRepository
) {

    fun createImporter(dto: CreateImporterRequestDto): ImporterResponseDto {
        // Create user profile with generated UUID
        val user = UserProfile(
            id = java.util.UUID.randomUUID().toString(),
            email = dto.email,
            phoneNumber = dto.phoneNumber,
            fullName = dto.fullName,
            passwordHash = "TEMPORARY_HASH"
        )
        val savedUser = userRepository.save(user)

        // Create Hedera account for importer
        val hederaAccountResult = try {
            hederaAccountService.createHederaAccount(
                initialBalance = Hbar.from(10),
                memo = "AgriBackup Importer: ${dto.companyName}"
            )
        } catch (e: Exception) {
            println("Failed to create Hedera account for importer: ${e.message}")
            null
        }

        // Create importer entity
        val importer = Importer(
            companyName = dto.companyName,
            importLicenseNumber = dto.importLicenseNumber ?: "",
            companyAddress = dto.companyAddress ?: "",
            destinationCountry = dto.destinationCountry ?: "",
            destinationPort = dto.destinationPort,
            importCategories = dto.importCategories?.joinToString(","),
            eudrComplianceOfficer = dto.eudrComplianceOfficer,
            certificationDetails = dto.certificationDetails,
            hederaAccountId = hederaAccountResult?.accountId,
            userProfile = savedUser
        )

        val savedImporter = importerRepository.save(importer)

        // Store Hedera credentials and associate with EUDR Certificate NFT
        if (hederaAccountResult != null) {
            try {
                val credentials = HederaAccountCredentials(
                    userId = savedUser.id!!,
                    entityType = "IMPORTER",
                    entityId = savedImporter.id,
                    hederaAccountId = hederaAccountResult.accountId,
                    publicKey = hederaAccountResult.publicKey,
                    encryptedPrivateKey = hederaAccountResult.encryptedPrivateKey,
                    creationTransactionId = hederaAccountResult.transactionId,
                    isActive = true,
                    tokensAssociated = "[]",
                    createdAt = LocalDateTime.now(),
                    lastUsedAt = LocalDateTime.now()
                )
                hederaAccountCredentialsRepository.save(credentials)

                // Associate with EUDR Compliance Certificate NFT
                val eudrCertificateNftId = hederaTokenService.getEudrComplianceCertificateNftId()
                if (eudrCertificateNftId != null) {
                    hederaAccountService.associateTokenWithAccount(
                        hederaAccountResult.accountId,
                        hederaAccountResult.encryptedPrivateKey,
                        eudrCertificateNftId
                    )

                    // Update credentials with associated token
                    credentials.tokensAssociated = """["${eudrCertificateNftId}"]"""
                    hederaAccountCredentialsRepository.save(credentials)

                    println("Associated EUDR Compliance Certificate NFT with importer account: ${hederaAccountResult.accountId}")
                }

                // Record importer creation on Hedera HCS
                hederaConsensusService.recordImporterCreation(
                    importerId = savedImporter.id,
                    companyName = savedImporter.companyName,
                    importLicenseNumber = savedImporter.importLicenseNumber,
                    destinationCountry = savedImporter.destinationCountry
                )
            } catch (e: Exception) {
                println("Failed to store Hedera credentials, associate NFT, or record on HCS: ${e.message}")
            }
        }

        return mapToResponseDto(savedImporter)
    }

    fun updateImporter(importerId: String, dto: UpdateImporterRequestDto): ImporterResponseDto {
        val importer = importerRepository.findById(importerId)
            .orElseThrow { NoSuchElementException("Importer not found with id: $importerId") }

        dto.companyName?.let { importer.companyName = it }
        dto.companyAddress?.let { importer.companyAddress = it }
        dto.destinationPort?.let { importer.destinationPort = it }
        dto.importCategories?.let { importer.importCategories = it.joinToString(",") }
        dto.eudrComplianceOfficer?.let { importer.eudrComplianceOfficer = it }
        dto.certificationDetails?.let { importer.certificationDetails = it }
        dto.hederaAccountId?.let { importer.hederaAccountId = it }
        importer.updatedAt = LocalDateTime.now()

        val updated = importerRepository.save(importer)
        return mapToResponseDto(updated)
    }

    fun verifyImporter(importerId: String, status: ImporterVerificationStatus): ImporterResponseDto {
        val importer = importerRepository.findById(importerId)
            .orElseThrow { NoSuchElementException("Importer not found with id: $importerId") }

        importer.verificationStatus = status
        importer.updatedAt = LocalDateTime.now()

        val updated = importerRepository.save(importer)
        return mapToResponseDto(updated)
    }

    fun createImportShipment(dto: CreateImportShipmentRequestDto): ImportShipmentResponseDto {
        val importer = importerRepository.findById(dto.importerId)
            .orElseThrow { NoSuchElementException("Importer not found with id: ${dto.importerId}") }

        // Generate shipment data hash for Hedera
        val shipmentDataHash = generateShipmentHash(
            shipmentNumber = dto.shipmentNumber,
            produceType = dto.produceType,
            quantityKg = dto.quantityKg,
            originCountry = dto.originCountry,
            sourceBatchId = dto.sourceBatchId
        )

        val shipment = ImportShipment(
            importer = importer,
            shipmentNumber = dto.shipmentNumber,
            sourceBatchId = dto.sourceBatchId,
            sourceEntityId = dto.sourceEntityId,
            sourceEntityType = dto.sourceEntityType,
            produceType = dto.produceType,
            quantityKg = dto.quantityKg,
            originCountry = dto.originCountry,
            departurePort = dto.departurePort,
            arrivalPort = dto.arrivalPort,
            shippingDate = dto.shippingDate,
            estimatedArrivalDate = dto.estimatedArrivalDate,
            actualArrivalDate = null,
            customsClearanceDate = null,
            customsReferenceNumber = null,
            billOfLadingNumber = dto.billOfLadingNumber,
            containerNumbers = dto.containerNumbers?.joinToString(","),
            status = ShipmentStatus.PENDING,
            eudrComplianceStatus = EudrComplianceStatus.PENDING_VERIFICATION,
            qualityInspectionPassed = null,
            qualityInspectionDate = null,
            qualityInspectionNotes = null,
            transportMethod = dto.transportMethod,
            transportCompany = dto.transportCompany,
            temperatureControlled = dto.temperatureControlled,
            hederaTransactionId = null,
            hederaShipmentHash = null
        )

        val savedShipment = importShipmentRepository.save(shipment)

        // Record on Hedera HCS
        try {
            val hederaTxId = hederaConsensusService.recordImportShipment(
                shipmentId = savedShipment.id,
                importerId = importer.id,
                shipmentNumber = savedShipment.shipmentNumber,
                produceType = savedShipment.produceType,
                quantityKg = savedShipment.quantityKg,
                originCountry = savedShipment.originCountry,
                shipmentDataHash = shipmentDataHash
            )
            savedShipment.hederaTransactionId = hederaTxId
            savedShipment.hederaShipmentHash = shipmentDataHash
            importShipmentRepository.save(savedShipment)
        } catch (e: Exception) {
            println("Failed to record import shipment on Hedera: ${e.message}")
        }

        // Update importer statistics
        importer.totalShipmentsReceived = importer.totalShipmentsReceived + 1
        importer.totalImportVolumeKg = importer.totalImportVolumeKg + dto.quantityKg
        importerRepository.save(importer)

        return mapToShipmentResponseDto(savedShipment)
    }

    fun updateShipmentStatus(dto: UpdateShipmentStatusRequestDto): ImportShipmentResponseDto {
        val shipment = importShipmentRepository.findById(dto.shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found with id: ${dto.shipmentId}") }

        shipment.status = dto.status
        dto.actualArrivalDate?.let { shipment.actualArrivalDate = it }
        dto.customsClearanceDate?.let { shipment.customsClearanceDate = it }
        dto.customsReferenceNumber?.let { shipment.customsReferenceNumber = it }

        val updated = importShipmentRepository.save(shipment)
        return mapToShipmentResponseDto(updated)
    }

    fun updateEudrCompliance(dto: UpdateEudrComplianceRequestDto): ImportShipmentResponseDto {
        val shipment = importShipmentRepository.findById(dto.shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found with id: ${dto.shipmentId}") }

        shipment.eudrComplianceStatus = dto.complianceStatus

        val updated = importShipmentRepository.save(shipment)
        return mapToShipmentResponseDto(updated)
    }

    fun uploadCustomsDocument(dto: UploadCustomsDocumentRequestDto): CustomsDocumentResponseDto {
        val shipment = importShipmentRepository.findById(dto.shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found with id: ${dto.shipmentId}") }

        // Generate document hash
        val documentHash = dto.checksumSha256 ?: generateDocumentHash(dto.s3Key, dto.fileName)

        val document = CustomsDocument(
            shipment = shipment,
            documentType = dto.documentType,
            documentNumber = dto.documentNumber,
            issueDate = dto.issueDate,
            issuingAuthority = dto.issuingAuthority,
            s3Key = dto.s3Key,
            fileName = dto.fileName,
            fileSize = dto.fileSize,
            checksumSha256 = dto.checksumSha256,
            hederaDocumentHash = null
        )

        val savedDocument = customsDocumentRepository.save(document)

        // Record on Hedera HCS
        try {
            val hederaTxId = hederaConsensusService.recordCustomsDocument(
                documentId = savedDocument.id,
                shipmentId = shipment.id,
                documentType = savedDocument.documentType,
                documentHash = documentHash
            )
            savedDocument.hederaDocumentHash = hederaTxId
            customsDocumentRepository.save(savedDocument)
        } catch (e: Exception) {
            println("Failed to record customs document on Hedera: ${e.message}")
        }

        return mapToDocumentResponseDto(savedDocument)
    }

    fun createInspectionRecord(dto: CreateInspectionRecordRequestDto): InspectionRecordResponseDto {
        val shipment = importShipmentRepository.findById(dto.shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found with id: ${dto.shipmentId}") }

        val inspection = InspectionRecord(
            shipment = shipment,
            inspectionType = dto.inspectionType,
            inspectionDate = dto.inspectionDate,
            inspectorName = dto.inspectorName,
            inspectorAgency = dto.inspectorAgency,
            inspectionResult = dto.inspectionResult,
            findings = dto.findings,
            recommendations = dto.recommendations,
            certificateNumber = dto.certificateNumber,
            hederaInspectionHash = null
        )

        val savedInspection = inspectionRecordRepository.save(inspection)

        // Record on Hedera HCS
        try {
            val hederaTxId = hederaConsensusService.recordInspectionResult(
                inspectionId = savedInspection.id,
                shipmentId = shipment.id,
                inspectionType = savedInspection.inspectionType,
                inspectionResult = savedInspection.inspectionResult.name,
                inspectionDate = savedInspection.inspectionDate
            )
            savedInspection.hederaInspectionHash = hederaTxId
            inspectionRecordRepository.save(savedInspection)
        } catch (e: Exception) {
            println("Failed to record inspection on Hedera: ${e.message}")
        }

        // Update shipment with inspection result
        if (dto.inspectionResult == InspectionResult.PASSED) {
            shipment.qualityInspectionPassed = true
            shipment.qualityInspectionDate = dto.inspectionDate
        } else if (dto.inspectionResult == InspectionResult.FAILED) {
            shipment.qualityInspectionPassed = false
            shipment.qualityInspectionDate = dto.inspectionDate
        }
        importShipmentRepository.save(shipment)

        return mapToInspectionResponseDto(savedInspection)
    }

    @Transactional(readOnly = true)
    fun getImporterById(importerId: String): ImporterResponseDto {
        val importer = importerRepository.findById(importerId)
            .orElseThrow { NoSuchElementException("Importer not found with id: $importerId") }
        return mapToResponseDto(importer)
    }

    @Transactional(readOnly = true)
    fun getAllImporters(pageable: Pageable): Page<ImporterResponseDto> {
        return importerRepository.findAll(pageable).map { mapToResponseDto(it) }
    }

    @Transactional(readOnly = true)
    fun getImportersByVerificationStatus(
        status: ImporterVerificationStatus,
        pageable: Pageable
    ): Page<ImporterResponseDto> {
        return importerRepository.findByVerificationStatus(status, pageable)
            .map { mapToResponseDto(it) }
    }

    @Transactional(readOnly = true)
    fun getShipmentsByImporter(
        importerId: String,
        pageable: Pageable
    ): Page<ImportShipmentResponseDto> {
        return importShipmentRepository.findByImporterId(importerId, pageable)
            .map { mapToShipmentResponseDto(it) }
    }

    @Transactional(readOnly = true)
    fun getShipmentById(shipmentId: String): ImportShipmentResponseDto {
        val shipment = importShipmentRepository.findById(shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found with id: $shipmentId") }
        return mapToShipmentResponseDto(shipment, includeDetails = true)
    }

    @Transactional(readOnly = true)
    fun getImporterStatistics(importerId: String): ImporterStatisticsDto {
        val importer = importerRepository.findById(importerId)
            .orElseThrow { NoSuchElementException("Importer not found with id: $importerId") }

        val totalShipments = importer.totalShipmentsReceived ?: 0
        val totalVolume = importer.totalImportVolumeKg ?: BigDecimal.ZERO

        // Calculate shipment status counts
        val pendingShipments = importShipmentRepository.countByImporterIdAndStatus(
            importerId,
            ShipmentStatus.PENDING
        )

        val inTransitShipments = importShipmentRepository.countByImporterIdAndStatus(
            importerId,
            ShipmentStatus.IN_TRANSIT
        )

        val customsClearanceShipments = importShipmentRepository.countByImporterIdAndStatus(
            importerId,
            ShipmentStatus.CUSTOMS_CLEARANCE
        )

        val deliveredShipments = importShipmentRepository.countByImporterIdAndStatus(
            importerId,
            ShipmentStatus.DELIVERED
        )

        // Calculate EUDR compliance counts
        val compliantShipments = importShipmentRepository.countByImporterIdAndEudrComplianceStatus(
            importerId,
            EudrComplianceStatus.COMPLIANT
        )

        val nonCompliantShipments = importShipmentRepository.countByImporterIdAndEudrComplianceStatus(
            importerId,
            EudrComplianceStatus.NON_COMPLIANT
        )

        // Simplified statistics - real implementation would need GROUP BY queries
        val currentMonthVolume = BigDecimal.ZERO // TODO: Implement with date range query
        val topOriginCountries = emptyList<OriginCountrySummaryDto>() // TODO: Implement GROUP BY query
        val topProduceTypes = emptyList<ProduceTypeSummaryDto>() // TODO: Implement GROUP BY query

        return ImporterStatisticsDto(
            importerId = importerId,
            totalShipmentsReceived = totalShipments,
            totalImportVolumeKg = totalVolume,
            pendingShipments = pendingShipments,
            inTransitShipments = inTransitShipments,
            customsClearanceShipments = customsClearanceShipments,
            deliveredShipments = deliveredShipments,
            eudrCompliantShipments = compliantShipments,
            nonCompliantShipments = nonCompliantShipments,
            currentMonthVolumeKg = currentMonthVolume,
            topOriginCountries = topOriginCountries,
            topProduceTypes = topProduceTypes
        )
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    private fun mapToResponseDto(importer: Importer): ImporterResponseDto {
        return ImporterResponseDto(
            id = importer.id,
            companyName = importer.companyName,
            importLicenseNumber = importer.importLicenseNumber,
            companyAddress = importer.companyAddress,
            destinationCountry = importer.destinationCountry,
            destinationPort = importer.destinationPort,
            importCategories = importer.importCategories?.split(","),
            eudrComplianceOfficer = importer.eudrComplianceOfficer,
            certificationDetails = importer.certificationDetails,
            verificationStatus = importer.verificationStatus,
            totalShipmentsReceived = importer.totalShipmentsReceived,
            totalImportVolumeKg = importer.totalImportVolumeKg,
            hederaAccountId = importer.hederaAccountId,
            createdAt = importer.createdAt,
            updatedAt = importer.updatedAt,
            userProfile = UserProfileSummaryDto(
                id = importer.userProfile.id,
                email = importer.userProfile.email ?: "",
                phoneNumber = importer.userProfile.phoneNumber ?: "",
                fullName = importer.userProfile.fullName
            )
        )
    }

    private fun mapToShipmentResponseDto(
        shipment: ImportShipment,
        includeDetails: Boolean = false
    ): ImportShipmentResponseDto {
        return ImportShipmentResponseDto(
            id = shipment.id,
            importerId = shipment.importer.id,
            importerName = shipment.importer.companyName,
            shipmentNumber = shipment.shipmentNumber,
            sourceBatchId = shipment.sourceBatchId,
            sourceEntityId = shipment.sourceEntityId,
            sourceEntityType = shipment.sourceEntityType,
            produceType = shipment.produceType,
            quantityKg = shipment.quantityKg,
            originCountry = shipment.originCountry,
            departurePort = shipment.departurePort,
            arrivalPort = shipment.arrivalPort,
            shippingDate = shipment.shippingDate,
            estimatedArrivalDate = shipment.estimatedArrivalDate,
            actualArrivalDate = shipment.actualArrivalDate,
            customsClearanceDate = shipment.customsClearanceDate,
            customsReferenceNumber = shipment.customsReferenceNumber,
            billOfLadingNumber = shipment.billOfLadingNumber,
            containerNumbers = shipment.containerNumbers?.split(","),
            status = shipment.status,
            eudrComplianceStatus = shipment.eudrComplianceStatus,
            qualityInspectionPassed = shipment.qualityInspectionPassed,
            qualityInspectionDate = shipment.qualityInspectionDate,
            qualityInspectionNotes = shipment.qualityInspectionNotes,
            transportMethod = shipment.transportMethod,
            transportCompany = shipment.transportCompany,
            temperatureControlled = shipment.temperatureControlled,
            hederaTransactionId = shipment.hederaTransactionId,
            hederaShipmentHash = shipment.hederaShipmentHash,
            createdAt = shipment.createdAt,
            updatedAt = shipment.updatedAt,
            inspectionRecords = if (includeDetails) shipment.inspectionRecords.map {
                InspectionRecordSummaryDto(
                    id = it.id,
                    inspectionType = it.inspectionType,
                    inspectionDate = it.inspectionDate,
                    inspectionResult = it.inspectionResult,
                    certificateNumber = it.certificateNumber
                )
            } else null,
            customsDocuments = if (includeDetails) shipment.customsDocuments.map {
                CustomsDocumentSummaryDto(
                    id = it.id,
                    documentType = it.documentType,
                    documentNumber = it.documentNumber,
                    fileName = it.fileName,
                    uploadedAt = it.uploadedAt
                )
            } else null
        )
    }

    private fun mapToDocumentResponseDto(document: CustomsDocument): CustomsDocumentResponseDto {
        return CustomsDocumentResponseDto(
            id = document.id,
            shipmentId = document.shipment.id,
            documentType = document.documentType,
            documentNumber = document.documentNumber,
            issueDate = document.issueDate,
            issuingAuthority = document.issuingAuthority,
            s3Key = document.s3Key,
            fileName = document.fileName,
            fileSize = document.fileSize,
            checksumSha256 = document.checksumSha256,
            hederaDocumentHash = document.hederaDocumentHash,
            uploadedAt = document.uploadedAt
        )
    }

    private fun mapToInspectionResponseDto(inspection: InspectionRecord): InspectionRecordResponseDto {
        return InspectionRecordResponseDto(
            id = inspection.id,
            shipmentId = inspection.shipment.id,
            inspectionType = inspection.inspectionType,
            inspectionDate = inspection.inspectionDate,
            inspectorName = inspection.inspectorName,
            inspectorAgency = inspection.inspectorAgency,
            inspectionResult = inspection.inspectionResult,
            findings = inspection.findings,
            recommendations = inspection.recommendations,
            certificateNumber = inspection.certificateNumber,
            hederaInspectionHash = inspection.hederaInspectionHash,
            createdAt = inspection.createdAt
        )
    }

    private fun generateShipmentHash(
        shipmentNumber: String,
        produceType: String,
        quantityKg: BigDecimal,
        originCountry: String,
        sourceBatchId: String?
    ): String {
        val hashInput = StringBuilder()
        hashInput.append("SHIPMENT:$shipmentNumber")
        hashInput.append("|PRODUCE:$produceType")
        hashInput.append("|QTY:$quantityKg")
        hashInput.append("|ORIGIN:$originCountry")
        sourceBatchId?.let { hashInput.append("|BATCH:$it") }

        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(hashInput.toString().toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateDocumentHash(s3Key: String, fileName: String): String {
        val hashInput = "$s3Key|$fileName|${System.currentTimeMillis()}"
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(hashInput.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verify shipment compliance and issue EUDR Compliance Certificate NFT
     * 
     * This method:
     * 1. Runs comprehensive EUDR checks (GPS, deforestation, traceability)
     * 2. If compliant, issues NFT certificate to exporter's account
     * 3. Updates shipment with certificate details
     * 4. Records on blockchain
     * 
     * @param shipmentId Shipment to verify
     * @return Updated shipment with compliance status and certificate info
     */
    fun verifyAndIssueComplianceCertificate(shipmentId: String): ImportShipmentResponseDto {
        val shipment = importShipmentRepository.findById(shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found: $shipmentId") }

        // Run EUDR compliance verification
        val complianceResult = eudrVerificationService.verifyShipmentCompliance(shipmentId)

        if (complianceResult.isCompliant) {
            // Get exporter's Hedera credentials
            // For now, we'll use the source entity (could be exporter or processor)
            val exporterCredentials = if (shipment.sourceEntityType == "EXPORTER") {
                hederaAccountCredentialsRepository
                    .findByEntityTypeAndEntityId("EXPORTER", shipment.sourceEntityId!!)
                    .orElse(null)
            } else {
                // If source is processor, need to find the exporter
                // For simplicity, try to get any exporter account
                null
            }

            if (exporterCredentials != null) {
                try {
                    // Issue EUDR Compliance Certificate NFT
                    val txId = hederaTokenService.issueComplianceCertificateNft(
                        shipmentId = shipment.id,
                        exporterAccountId = AccountId.fromString(exporterCredentials.hederaAccountId),
                        complianceData = complianceResult.complianceData
                    )

                    // Update shipment with certificate info
                    val nftId = hederaTokenService.getEudrComplianceCertificateNftId()
                    shipment.complianceCertificateNftId = nftId?.toString()
                    shipment.complianceCertificateSerialNumber = 1
                    shipment.complianceCertificateTransactionId = txId
                    shipment.currentOwnerAccountId = exporterCredentials.hederaAccountId
                    shipment.eudrComplianceStatus = EudrComplianceStatus.COMPLIANT
                    shipment.status = ShipmentStatus.APPROVED

                    println("✅ EUDR Certificate issued for shipment $shipmentId")
                    println("   Transaction: $txId")
                    println("   Current owner: ${exporterCredentials.hederaAccountId}")
                } catch (e: Exception) {
                    println("❌ Failed to issue certificate: ${e.message}")
                    shipment.eudrComplianceStatus = EudrComplianceStatus.COMPLIANT
                }
            } else {
                println("⚠️ Shipment compliant but no exporter account found")
                shipment.eudrComplianceStatus = EudrComplianceStatus.COMPLIANT
            }
        } else {
            // Not compliant - update status and log reasons
            shipment.eudrComplianceStatus = EudrComplianceStatus.NON_COMPLIANT
            println("❌ Shipment $shipmentId failed EUDR verification:")
            complianceResult.failureReasons.forEach { println("   - $it") }
        }

        val updated = importShipmentRepository.save(shipment)
        return mapToShipmentResponseDto(updated)
    }

    /**
     * Transfer EUDR Compliance Certificate NFT from exporter to importer
     * 
     * This method:
     * 1. Retrieves credentials for both exporter and importer
     * 2. Transfers NFT on Hedera blockchain
     * 3. Updates shipment ownership
     * 4. Records transfer on HCS
     * 
     * @param shipmentId Shipment being transferred
     * @param importerId Importer receiving the shipment
     * @return Updated shipment with new ownership
     */
    fun transferComplianceCertificateToImporter(shipmentId: String, importerId: String): ImportShipmentResponseDto {
        val shipment = importShipmentRepository.findById(shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found: $shipmentId") }

        // Validate shipment has certificate
        if (shipment.complianceCertificateNftId == null) {
            throw IllegalStateException("Shipment does not have a compliance certificate")
        }

        // Get importer
        val importer = importerRepository.findById(importerId)
            .orElseThrow { NoSuchElementException("Importer not found: $importerId") }

        // Get importer's Hedera credentials
        val importerCredentials = hederaAccountCredentialsRepository
            .findByEntityTypeAndEntityId("IMPORTER", importerId)
            .orElseThrow { IllegalStateException("Importer does not have Hedera account") }

        // Get current owner (exporter) credentials
        val currentOwnerAccountId = shipment.currentOwnerAccountId
            ?: throw IllegalStateException("Shipment has no current owner")

        val exporterCredentials = hederaAccountCredentialsRepository
            .findByHederaAccountId(currentOwnerAccountId)
            .orElseThrow { IllegalStateException("Current owner credentials not found") }

        try {
            // Transfer certificate NFT
            val success = hederaTokenService.transferComplianceCertificateNft(
                fromAccountId = AccountId.fromString(exporterCredentials.hederaAccountId),
                toAccountId = AccountId.fromString(importerCredentials.hederaAccountId),
                shipmentId = shipmentId
            )

            if (success) {
                // Update shipment ownership
                shipment.currentOwnerAccountId = importerCredentials.hederaAccountId
                shipment.status = ShipmentStatus.CUSTOMS_CLEARANCE
                shipment.importer = importer

                val updated = importShipmentRepository.save(shipment)

                println("✅ Certificate transferred for shipment $shipmentId")
                println("   From: ${exporterCredentials.hederaAccountId}")
                println("   To: ${importerCredentials.hederaAccountId}")

                return mapToShipmentResponseDto(updated)
            } else {
                throw RuntimeException("Certificate transfer failed")
            }
        } catch (e: Exception) {
            println("❌ Failed to transfer certificate: ${e.message}")
            throw RuntimeException("Failed to transfer compliance certificate", e)
        }
    }

    /**
     * Verify customs compliance by checking certificate authenticity
     * 
     * This method:
     * 1. Checks if importer has the certificate NFT
     * 2. Queries blockchain for certificate history
     * 3. Validates certificate is not frozen/revoked
     * 4. Returns verification result
     * 
     * @param shipmentId Shipment being verified
     * @return Customs verification result
     */
    fun verifyCustomsCompliance(shipmentId: String): CustomsVerificationResponseDto {
        val shipment = importShipmentRepository.findById(shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found: $shipmentId") }

        // Check if shipment has certificate
        if (shipment.complianceCertificateNftId == null) {
            return CustomsVerificationResponseDto(
                shipmentId = shipmentId,
                approved = false,
                certificateValid = false,
                complianceStatus = "NON_COMPLIANT",
                message = "No EUDR compliance certificate found for this shipment",
                verifiedAt = LocalDateTime.now()
            )
        }

        // Get current owner (should be importer)
        val currentOwnerAccountId = shipment.currentOwnerAccountId
            ?: return CustomsVerificationResponseDto(
                shipmentId = shipmentId,
                approved = false,
                certificateValid = false,
                complianceStatus = "UNKNOWN_OWNER",
                message = "Certificate owner unknown",
                verifiedAt = LocalDateTime.now()
            )

        try {
            // Check if account has the certificate NFT
            val accountId = AccountId.fromString(currentOwnerAccountId)
            val hasNFT = hederaTokenService.hasValidComplianceCertificate(accountId)

            if (hasNFT) {
                // Certificate found and valid
                shipment.status = ShipmentStatus.APPROVED
                shipment.customsClearanceDate = java.time.LocalDate.now()
                importShipmentRepository.save(shipment)

                return CustomsVerificationResponseDto(
                    shipmentId = shipmentId,
                    approved = true,
                    certificateValid = true,
                    complianceStatus = "COMPLIANT",
                    message = "EUDR certificate verified - customs clearance granted",
                    certificateNftId = shipment.complianceCertificateNftId,
                    certificateSerialNumber = shipment.complianceCertificateSerialNumber,
                    currentOwner = currentOwnerAccountId,
                    verifiedAt = LocalDateTime.now()
                )
            } else {
                // Certificate not found or frozen
                return CustomsVerificationResponseDto(
                    shipmentId = shipmentId,
                    approved = false,
                    certificateValid = false,
                    complianceStatus = "CERTIFICATE_NOT_FOUND",
                    message = "Certificate not found in account or has been frozen",
                    verifiedAt = LocalDateTime.now()
                )
            }
        } catch (e: Exception) {
            println("❌ Customs verification failed: ${e.message}")
            return CustomsVerificationResponseDto(
                shipmentId = shipmentId,
                approved = false,
                certificateValid = false,
                complianceStatus = "VERIFICATION_ERROR",
                message = "Error during verification: ${e.message}",
                verifiedAt = LocalDateTime.now()
            )
        }
    }

    // ============================================
    // CONNECTION MANAGEMENT METHODS
    // ============================================

    /**
     * Get importers connected to an exporter
     */
    fun getConnectedImporters(exporterId: String, pageable: Pageable): Page<ImporterResponseDto> {
        val connections = exporterImporterConnectionRepository.findByExporterIdAndStatus(
            exporterId,
            com.agriconnect.farmersportalapis.domain.eudr.ConnectionStatus.ACTIVE
        )

        val importerIds = connections.map { it.importer.id }
        val importers = if (importerIds.isNotEmpty()) {
            importerRepository.findAllById(importerIds)
        } else {
            emptyList()
        }

        // Manual pagination
        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, importers.size)
        val pageContent = if (start < importers.size) importers.subList(start, end) else emptyList()

        return org.springframework.data.domain.PageImpl(
            pageContent.map { mapToResponseDto(it) },
            pageable,
            importers.size.toLong()
        )
    }

    /**
     * Get importers available to connect (not yet connected to the exporter)
     */
    fun getAvailableImporters(exporterId: String, pageable: Pageable): Page<ImporterResponseDto> {
        val connections = exporterImporterConnectionRepository.findByExporterId(exporterId)
        val connectedImporterIds = connections.map { it.importer.id }.toSet()

        // Get all importers and filter out connected ones
        val allImporters = importerRepository.findAll()
        val availableImporters = allImporters.filter { it.id !in connectedImporterIds }

        // Manual pagination
        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, availableImporters.size)
        val pageContent = if (start < availableImporters.size) availableImporters.subList(start, end) else emptyList()

        return org.springframework.data.domain.PageImpl(
            pageContent.map { mapToResponseDto(it) },
            pageable,
            availableImporters.size.toLong()
        )
    }

    /**
     * Connect an exporter to an importer
     */
    fun connectExporterToImporter(exporterId: String, importerId: String, notes: String? = null): Boolean {
        // Check if connection already exists
        if (exporterImporterConnectionRepository.existsByExporterIdAndImporterId(exporterId, importerId)) {
            throw IllegalStateException("Connection already exists between exporter and importer")
        }

        // Validate exporter and importer exist
        val exporter = exporterRepository.findById(exporterId)
            .orElseThrow { NoSuchElementException("Exporter not found with id: $exporterId") }

        val importer = importerRepository.findById(importerId)
            .orElseThrow { NoSuchElementException("Importer not found with id: $importerId") }

        // Create connection
        val connection = com.agriconnect.farmersportalapis.domain.eudr.ExporterImporterConnection(
            exporter = exporter,
            importer = importer,
            status = com.agriconnect.farmersportalapis.domain.eudr.ConnectionStatus.ACTIVE,
            notes = notes
        )

        exporterImporterConnectionRepository.save(connection)
        return true
    }

    /**
     * Disconnect an exporter from an importer
     */
    fun disconnectExporterFromImporter(exporterId: String, importerId: String): Boolean {
        val connection = exporterImporterConnectionRepository.findByExporterIdAndImporterId(exporterId, importerId)
            ?: throw NoSuchElementException("Connection not found")

        exporterImporterConnectionRepository.delete(connection)
        return true
    }
}
