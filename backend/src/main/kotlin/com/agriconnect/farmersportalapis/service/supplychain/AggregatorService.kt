package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.eudr.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import com.agriconnect.farmersportalapis.service.hedera.HederaAccountService
import com.agriconnect.farmersportalapis.service.hedera.HederaConsensusServices
import com.agriconnect.farmersportalapis.service.hedera.HederaTokenService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class AggregatorService(
    private val aggregatorRepository: AggregatorRepository,
    private val aggregationEventRepository: AggregationEventRepository,
    private val consolidatedBatchRepository: ConsolidatedBatchRepository,
    private val userRepository: UserRepository,
    private val hederaConsensusService: HederaConsensusServices,
    private val hederaAccountService: HederaAccountService,
    private val hederaAccountCredentialsRepository: HederaAccountCredentialsRepository,
    private val hederaTokenService: HederaTokenService,
    private val exporterRepository: ExporterRepository,
    private val exporterAggregatorConnectionRepository: com.agriconnect.farmersportalapis.repository.ExporterAggregatorConnectionRepository
) {

    fun createAggregator(dto: CreateAggregatorRequestDto): AggregatorResponseDto {
        // Create user profile with generated UUID
        val user = UserProfile(
            id = UUID.randomUUID().toString(),
            email = dto.email,
            phoneNumber = dto.phoneNumber,
            fullName = dto.fullName,
            passwordHash = "TEMPORARY_HASH"
        )
        val savedUser = userRepository.save(user)

        // Create Hedera account for the aggregator
        val hederaAccountResult = try {
            hederaAccountService.createHederaAccount(
                initialBalance = com.hedera.hashgraph.sdk.Hbar.from(10),
                memo = "AgriBackup Aggregator: ${dto.organizationName}"
            )
        } catch (e: Exception) {
            println("Failed to create Hedera account for aggregator: ${e.message}")
            null
        }

        // Create aggregator entity
        val aggregator = Aggregator(
            organizationName = dto.organizationName,
            organizationType = dto.organizationType?.let { 
                try { AggregatorType.valueOf(it.uppercase()) } 
                catch (e: Exception) { AggregatorType.COOPERATIVE }
            } ?: AggregatorType.COOPERATIVE,
            registrationNumber = dto.registrationNumber,
            facilityAddress = dto.facilityAddress ?: "",
            storageCapacityTons = dto.storageCapacityTons?.let { BigDecimal.valueOf(it) },
            collectionRadiusKm = dto.collectionRadiusKm?.let { BigDecimal.valueOf(it) },
            primaryCommodities = dto.primaryCommodities?.joinToString(","),
            certificationDetails = dto.certificationDetails,
            hederaAccountId = hederaAccountResult?.accountId ?: dto.hederaAccountId,
            userProfile = savedUser
        )

        val savedAggregator = aggregatorRepository.save(aggregator)

        // Store Hedera credentials if account was created
        if (hederaAccountResult != null) {
            val credentials = com.agriconnect.farmersportalapis.domain.hedera.HederaAccountCredentials(
                userId = savedUser.id!!,
                entityType = "AGGREGATOR",
                entityId = savedAggregator.id,
                hederaAccountId = hederaAccountResult.accountId,
                publicKey = hederaAccountResult.publicKey,
                encryptedPrivateKey = hederaAccountResult.encryptedPrivateKey,
                initialBalanceHbar = "10",
                accountMemo = "AgriBackup Aggregator: ${dto.organizationName}",
                creationTransactionId = hederaAccountResult.transactionId
            )
            hederaAccountCredentialsRepository.save(credentials)

            // Associate with EUDR Compliance Certificate NFT
            try {
                val eudrCertificateNftId = hederaTokenService.getEudrComplianceCertificateNftId()
                if (eudrCertificateNftId != null) {
                    hederaAccountService.associateTokenWithAccount(
                        hederaAccountResult.accountId,
                        hederaAccountResult.encryptedPrivateKey,
                        eudrCertificateNftId
                    )
                    credentials.tokensAssociated = """["${eudrCertificateNftId}"]"""
                    hederaAccountCredentialsRepository.save(credentials)
                    println("Associated EUDR Compliance Certificate NFT with aggregator account")
                }
            } catch (e: Exception) {
                println("Failed to associate EUDR Certificate NFT with aggregator account: ${e.message}")
            }
        }

        return mapToResponseDto(savedAggregator)
    }

    fun updateAggregator(aggregatorId: String, dto: UpdateAggregatorRequestDto): AggregatorResponseDto {
        val aggregator = aggregatorRepository.findById(aggregatorId)
            .orElseThrow { NoSuchElementException("Aggregator not found with id: $aggregatorId") }

        dto.organizationName?.let { aggregator.organizationName = it }
        dto.facilityAddress?.let { aggregator.facilityAddress = it }
        dto.storageCapacityTons?.let { aggregator.storageCapacityTons = it }
        dto.collectionRadiusKm?.let { aggregator.collectionRadiusKm = it }
        dto.primaryCommodities?.let { aggregator.primaryCommodities = it.joinToString(",") }
        dto.certificationDetails?.let { aggregator.certificationDetails = it }
        dto.hederaAccountId?.let { aggregator.hederaAccountId = it }
        aggregator.updatedAt = LocalDateTime.now()

        val updated = aggregatorRepository.save(aggregator)
        return mapToResponseDto(updated)
    }

    fun verifyAggregator(aggregatorId: String, status: AggregatorVerificationStatus): AggregatorResponseDto {
        val aggregator = aggregatorRepository.findById(aggregatorId)
            .orElseThrow { NoSuchElementException("Aggregator not found with id: $aggregatorId") }

        aggregator.verificationStatus = status
        aggregator.updatedAt = LocalDateTime.now()

        val updated = aggregatorRepository.save(aggregator)
        return mapToResponseDto(updated)
    }

    fun createAggregationEvent(dto: CreateAggregationEventRequestDto): AggregationEventResponseDto {
        val aggregator = aggregatorRepository.findById(dto.aggregatorId)
            .orElseThrow { NoSuchElementException("Aggregator not found with id: ${dto.aggregatorId}") }

        val totalPayment = if (dto.pricePerKg != null) dto.quantityKg.multiply(dto.pricePerKg) else null

        val event = AggregationEvent(
            aggregator = aggregator,
            farmerId = dto.farmerId,
            farmerName = null,
            produceType = dto.produceType,
            quantityKg = dto.quantityKg,
            collectionDate = LocalDateTime.now(),
            qualityGrade = dto.qualityGrade,
            moistureContent = dto.moistureContent,
            pricePerKg = dto.pricePerKg,
            totalPayment = totalPayment,
            paymentStatus = PaymentStatus.PENDING,
            collectionLocationGps = dto.collectionLocationGps,
            impurityPercentage = dto.impurityPercentage,
            notes = dto.notes,
            hederaTransactionId = null,
            consolidatedBatch = null
        )

        val savedEvent = aggregationEventRepository.save(event)

        // Update aggregator statistics
        aggregator.totalFarmersConnected += 1
        aggregator.totalBatchesCollected += 1
        aggregator.updatedAt = LocalDateTime.now()
        aggregatorRepository.save(aggregator)

        return mapToEventResponseDto(savedEvent)
    }

    fun createConsolidatedBatch(dto: CreateConsolidatedBatchRequestDto): ConsolidatedBatchResponseDto {
        val aggregator = aggregatorRepository.findById(dto.aggregatorId)
            .orElseThrow { NoSuchElementException("Aggregator not found with id: ${dto.aggregatorId}") }

        // Fetch aggregation events
        val events = aggregationEventRepository.findAllById(dto.aggregationEventIds)
        if (events.size != dto.aggregationEventIds.size) {
            throw IllegalArgumentException("Some aggregation events not found")
        }

        // Verify all events belong to this aggregator
        events.forEach { event ->
            if (event.aggregator.id != aggregator.id) {
                throw IllegalArgumentException("Event ${event.id} does not belong to aggregator ${aggregator.id}")
            }
        }

        val totalQuantity = events.sumOf { it.quantityKg }
        val batchNumber = "BATCH-${aggregator.id.take(8)}-${System.currentTimeMillis()}"

        val batch = ConsolidatedBatch(
            aggregator = aggregator,
            batchNumber = batchNumber,
            produceType = dto.produceType,
            totalQuantityKg = totalQuantity,
            numberOfFarmers = events.map { it.farmerId }.distinct().size,
            averageQualityGrade = events.mapNotNull { it.qualityGrade }.firstOrNull(),
            consolidationDate = LocalDateTime.now(),
            destinationEntityId = dto.destinationEntityId,
            destinationEntityType = dto.destinationEntityType,
            status = ConsolidatedBatchStatus.CREATED,
            shipmentDate = null,
            deliveryDate = null,
            transportDetails = dto.transportDetails,
            hederaTransactionId = null,
            hederaBatchHash = null,
            aggregationEvents = events.toMutableList()
        )

        val savedBatch = consolidatedBatchRepository.save(batch)

        // Update aggregator statistics
        aggregator.totalBatchesCollected += 1
        aggregator.updatedAt = LocalDateTime.now()
        aggregatorRepository.save(aggregator)

        return mapToBatchResponseDto(savedBatch)
    }

    fun updateBatchStatus(batchId: String, status: ConsolidatedBatchStatus): ConsolidatedBatchResponseDto {
        val batch = consolidatedBatchRepository.findById(batchId)
            .orElseThrow { NoSuchElementException("Consolidated batch not found with id: $batchId") }

        batch.status = status
        if (status == ConsolidatedBatchStatus.IN_TRANSIT) {
            batch.shipmentDate = LocalDateTime.now()
        } else if (status == ConsolidatedBatchStatus.DELIVERED) {
            batch.deliveryDate = LocalDateTime.now()
        }
        batch.updatedAt = LocalDateTime.now()

        val updated = consolidatedBatchRepository.save(batch)
        return mapToBatchResponseDto(updated)
    }

    fun updatePaymentStatus(dto: UpdatePaymentStatusRequestDto): AggregationEventResponseDto {
        val event = aggregationEventRepository.findById(dto.aggregationEventId)
            .orElseThrow { NoSuchElementException("Aggregation event not found with id: ${dto.aggregationEventId}") }

        event.paymentStatus = dto.paymentStatus
        if (dto.totalPayment != null) {
            event.totalPayment = dto.totalPayment
        }

        val updated = aggregationEventRepository.save(event)
        return mapToEventResponseDto(updated)
    }

    @Transactional(readOnly = true)
    fun getAggregatorById(aggregatorId: String): AggregatorResponseDto {
        val aggregator = aggregatorRepository.findById(aggregatorId)
            .orElseThrow { NoSuchElementException("Aggregator not found with id: $aggregatorId") }
        return mapToResponseDto(aggregator)
    }

    @Transactional(readOnly = true)
    fun getAllAggregators(pageable: Pageable): Page<AggregatorResponseDto> {
        return aggregatorRepository.findAll(pageable).map { mapToResponseDto(it) }
    }

    @Transactional(readOnly = true)
    fun getAggregatorsByVerificationStatus(
        status: AggregatorVerificationStatus,
        pageable: Pageable
    ): Page<AggregatorResponseDto> {
        return aggregatorRepository.findByVerificationStatus(status, pageable)
            .map { mapToResponseDto(it) }
    }

    @Transactional(readOnly = true)
    fun getAggregationEventsByAggregator(
        aggregatorId: String,
        pageable: Pageable
    ): Page<AggregationEventResponseDto> {
        return aggregationEventRepository.findByAggregatorId(aggregatorId, pageable)
            .map { mapToEventResponseDto(it) }
    }

    @Transactional(readOnly = true)
    fun getConsolidatedBatchesByAggregator(
        aggregatorId: String,
        pageable: Pageable
    ): Page<ConsolidatedBatchResponseDto> {
        return consolidatedBatchRepository.findByAggregatorId(aggregatorId, pageable)
            .map { mapToBatchResponseDto(it) }
    }

    @Transactional(readOnly = true)
    fun getConsolidatedBatchById(batchId: String): ConsolidatedBatchResponseDto {
        val batch = consolidatedBatchRepository.findById(batchId)
            .orElseThrow { NoSuchElementException("Consolidated batch not found with id: $batchId") }
        return mapToBatchResponseDto(batch)
    }

    @Transactional(readOnly = true)
    fun getAggregatorStatistics(aggregatorId: String): AggregatorStatisticsDto {
        val aggregator = aggregatorRepository.findById(aggregatorId)
            .orElseThrow { NoSuchElementException("Aggregator not found with id: $aggregatorId") }

        val totalEvents = aggregationEventRepository.countByAggregatorId(aggregatorId)
        val totalVolume = aggregationEventRepository.findByAggregatorId(aggregatorId)
            .sumOf { it.quantityKg }
        val totalBatches = consolidatedBatchRepository.countByAggregatorId(aggregatorId)

        // Calculate additional statistics
        val pendingPayments = aggregationEventRepository.countByAggregatorIdAndPaymentStatus(
            aggregatorId,
            PaymentStatus.PENDING
        )

        val paidEvents = aggregationEventRepository.countByAggregatorIdAndPaymentStatus(
            aggregatorId,
            PaymentStatus.PAID
        )

        val totalPayments = aggregationEventRepository.findByAggregatorIdAndPaymentStatus(aggregatorId, PaymentStatus.PAID)
            .sumOf { it.totalPayment ?: BigDecimal.ZERO }

        // Get top produce types (simplified)
        val topProduceTypes = emptyList<ProduceTypeSummaryDto>()

        // Calculate current month volume (simplified)
        val currentMonthVolume = BigDecimal.ZERO

        return AggregatorStatisticsDto(
            aggregatorId = aggregatorId,
            totalFarmersConnected = aggregator.totalFarmersConnected,
            totalConsolidatedBatches = totalBatches.toInt(),
            totalQuantityCollectedKg = totalVolume,
            totalPaymentsMade = totalPayments,
            pendingPaymentsCount = pendingPayments.toInt(),
            currentMonthCollectionKg = currentMonthVolume,
            averageQualityGrade = null,
            topProduceTypes = topProduceTypes,
            totalCollectionEvents = totalEvents.toInt()
        )
    }

    // ============================================
    // PRIVATE HELPER METHODS
    // ============================================

    private fun mapToResponseDto(aggregator: Aggregator): AggregatorResponseDto {
        return AggregatorResponseDto(
            id = aggregator.id,
            organizationName = aggregator.organizationName,
            organizationType = aggregator.organizationType,
            registrationNumber = aggregator.registrationNumber,
            facilityAddress = aggregator.facilityAddress,
            storageCapacityTons = aggregator.storageCapacityTons,
            collectionRadiusKm = aggregator.collectionRadiusKm,
            primaryCommodities = aggregator.primaryCommodities?.split(","),
            certificationDetails = aggregator.certificationDetails,
            verificationStatus = aggregator.verificationStatus,
            totalFarmersConnected = aggregator.totalFarmersConnected,
            totalBatchesCollected = aggregator.totalBatchesCollected,
            hederaAccountId = aggregator.hederaAccountId,
            createdAt = aggregator.createdAt,
            updatedAt = aggregator.updatedAt,
            userProfile = UserProfileSummaryDto(
                id = aggregator.userProfile.id,
                email = aggregator.userProfile.email ?: "",
                phoneNumber = aggregator.userProfile.phoneNumber ?: "",
                fullName = aggregator.userProfile.fullName ?: ""
            )
        )
    }

    private fun mapToEventResponseDto(event: AggregationEvent): AggregationEventResponseDto {
        return AggregationEventResponseDto(
            id = event.id,
            aggregatorId = event.aggregator.id,
            aggregatorName = event.aggregator.organizationName,
            farmerId = event.farmerId,
            farmerName = event.farmerName,
            produceType = event.produceType,
            quantityKg = event.quantityKg,
            qualityGrade = event.qualityGrade,
            pricePerKg = event.pricePerKg,
            totalPayment = event.totalPayment,
            paymentStatus = event.paymentStatus,
            collectionDate = event.collectionDate,
            collectionLocationGps = event.collectionLocationGps,
            moistureContent = event.moistureContent,
            impurityPercentage = event.impurityPercentage,
            notes = event.notes,
            hederaTransactionId = event.hederaTransactionId,
            consolidatedBatchId = event.consolidatedBatch?.id,
            createdAt = event.createdAt
        )
    }

    private fun mapToBatchResponseDto(batch: ConsolidatedBatch): ConsolidatedBatchResponseDto {
        return ConsolidatedBatchResponseDto(
            id = batch.id,
            aggregatorId = batch.aggregator.id,
            aggregatorName = batch.aggregator.organizationName,
            batchNumber = batch.batchNumber,
            produceType = batch.produceType,
            totalQuantityKg = batch.totalQuantityKg,
            numberOfFarmers = batch.numberOfFarmers,
            averageQualityGrade = batch.averageQualityGrade,
            consolidationDate = batch.consolidationDate,
            destinationEntityId = batch.destinationEntityId,
            destinationEntityType = batch.destinationEntityType,
            status = batch.status,
            shipmentDate = batch.shipmentDate,
            deliveryDate = batch.deliveryDate,
            transportDetails = batch.transportDetails,
            hederaTransactionId = batch.hederaTransactionId,
            hederaBatchHash = batch.hederaBatchHash,
            createdAt = batch.createdAt,
            updatedAt = batch.updatedAt,
            aggregationEvents = batch.aggregationEvents.map {
                AggregationEventSummaryDto(
                    id = it.id,
                    farmerId = it.farmerId,
                    farmerName = it.farmerName,
                    quantityKg = it.quantityKg,
                    qualityGrade = it.qualityGrade,
                    collectionDate = it.collectionDate
                )
            }
        )
    }

    private fun buildBatchHashData(batch: ConsolidatedBatch): String {
        // Create deterministic hash input
        val hashInput = StringBuilder()
        hashInput.append("BATCH:${batch.batchNumber}")
        hashInput.append("|PRODUCE:${batch.produceType}")
        hashInput.append("|QTY:${batch.totalQuantityKg}")
        hashInput.append("|FARMERS:${batch.numberOfFarmers}")
        hashInput.append("|DATE:${batch.consolidationDate}")

        // Include all farmer IDs in sorted order for consistency
        val farmerIds = batch.aggregationEvents.map { it.farmerId }.sorted()
        hashInput.append("|FARMER_IDS:${farmerIds.joinToString(",")}")

        // Generate SHA-256 hash
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(hashInput.toString().toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    // ============================================
    // CONNECTION MANAGEMENT METHODS
    // ============================================

    /**
     * Get aggregators connected to an exporter
     */
    fun getConnectedAggregators(exporterId: String, pageable: Pageable): Page<AggregatorResponseDto> {
        val connections = exporterAggregatorConnectionRepository.findByExporterIdAndStatus(
            exporterId,
            com.agriconnect.farmersportalapis.domain.eudr.ConnectionStatus.ACTIVE
        )

        val aggregatorIds = connections.map { it.aggregator.id }
        val aggregators = if (aggregatorIds.isNotEmpty()) {
            aggregatorRepository.findAllById(aggregatorIds)
        } else {
            emptyList()
        }

        // Manual pagination
        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, aggregators.size)
        val pageContent = if (start < aggregators.size) aggregators.subList(start, end) else emptyList()

        return org.springframework.data.domain.PageImpl(
            pageContent.map { mapToResponseDto(it) },
            pageable,
            aggregators.size.toLong()
        )
    }

    /**
     * Get aggregators available to connect (not yet connected to the exporter)
     */
    fun getAvailableAggregators(exporterId: String, pageable: Pageable): Page<AggregatorResponseDto> {
        val connections = exporterAggregatorConnectionRepository.findByExporterId(exporterId)
        val connectedAggregatorIds = connections.map { it.aggregator.id }.toSet()

        // Get all aggregators and filter out connected ones
        val allAggregators = aggregatorRepository.findAll()
        val availableAggregators = allAggregators.filter { it.id !in connectedAggregatorIds }

        // Manual pagination
        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, availableAggregators.size)
        val pageContent = if (start < availableAggregators.size) availableAggregators.subList(start, end) else emptyList()

        return org.springframework.data.domain.PageImpl(
            pageContent.map { mapToResponseDto(it) },
            pageable,
            availableAggregators.size.toLong()
        )
    }

    /**
     * Connect an exporter to an aggregator
     */
    fun connectExporterToAggregator(exporterId: String, aggregatorId: String, notes: String? = null): Boolean {
        // Check if connection already exists
        if (exporterAggregatorConnectionRepository.existsByExporterIdAndAggregatorId(exporterId, aggregatorId)) {
            throw IllegalStateException("Connection already exists between exporter and aggregator")
        }

        // Validate exporter and aggregator exist
        val exporter = exporterRepository.findById(exporterId)
            .orElseThrow { NoSuchElementException("Exporter not found with id: $exporterId") }

        val aggregator = aggregatorRepository.findById(aggregatorId)
            .orElseThrow { NoSuchElementException("Aggregator not found with id: $aggregatorId") }

        // Create connection
        val connection = ExporterAggregatorConnection(
            exporter = exporter,
            aggregator = aggregator,
            status = ConnectionStatus.ACTIVE,
            notes = notes
        )

        exporterAggregatorConnectionRepository.save(connection)
        return true
    }

    /**
     * Disconnect an exporter from an aggregator
     */
    fun disconnectExporterFromAggregator(exporterId: String, aggregatorId: String): Boolean {
        val connection = exporterAggregatorConnectionRepository.findByExporterIdAndAggregatorId(exporterId, aggregatorId)
            ?: throw NoSuchElementException("Connection not found")

        exporterAggregatorConnectionRepository.delete(connection)
        return true
    }
}