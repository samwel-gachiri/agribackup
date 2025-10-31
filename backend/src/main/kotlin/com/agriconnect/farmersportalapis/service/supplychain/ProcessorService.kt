package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.eudr.ProcessingEvent
import com.agriconnect.farmersportalapis.domain.eudr.Processor
import com.agriconnect.farmersportalapis.domain.eudr.ProcessorVerificationStatus
import com.agriconnect.farmersportalapis.domain.hedera.HederaAccountCredentials
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import com.agriconnect.farmersportalapis.service.hedera.HederaAccountService
import com.agriconnect.farmersportalapis.service.hedera.HederaConsensusServices
import com.agriconnect.farmersportalapis.service.hedera.HederaTokenService
import com.hedera.hashgraph.sdk.Hbar
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class ProcessorService(
    private val processorRepository: ProcessorRepository,
    private val processingEventRepository: ProcessingEventRepository,
    private val userRepository: UserRepository,
    private val exporterRepository: ExporterRepository,
    private val eudrBatchRepository: EudrBatchRepository,
    private val hederaConsensusService: HederaConsensusServices,
    private val hederaAccountService: HederaAccountService,
    private val hederaAccountCredentialsRepository: HederaAccountCredentialsRepository,
    private val hederaTokenService: HederaTokenService,
    private val exporterProcessorConnectionRepository: com.agriconnect.farmersportalapis.repository.ExporterProcessorConnectionRepository
) {

    fun createProcessor(dto: CreateProcessorRequestDto): ProcessorResponseDto {
        // Create user profile with generated UUID
        val user = UserProfile(
            id = UUID.randomUUID().toString(),
            email = dto.email,
            phoneNumber = dto.phoneNumber,
            fullName = dto.fullName,
            passwordHash = "TEMPORARY_HASH"
        )
        val savedUser = userRepository.save(user)

        // Create Hedera account for processor
        val hederaAccountResult = try {
            hederaAccountService.createHederaAccount(
                initialBalance = Hbar.from(10),
                memo = "AgriBackup Processor: ${dto.facilityName}"
            )
        } catch (e: Exception) {
            println("Failed to create Hedera account for processor: ${e.message}")
            null
        }

        // Create processor entity
        val processor = Processor(
            facilityName = dto.facilityName,
            facilityAddress = dto.facilityAddress ?: "",
            processingCapabilities = dto.processingCapabilities,
            certificationDetails = dto.certifications,
            verificationStatus = ProcessorVerificationStatus.PENDING,
            userProfile = savedUser,
            hederaAccountId = hederaAccountResult?.accountId
        )

        val savedProcessor = processorRepository.save(processor)

        // Store Hedera credentials and associate with EUDR Certificate NFT
        if (hederaAccountResult != null) {
            try {
                val credentials = HederaAccountCredentials(
                    userId = savedUser.id!!,
                    entityType = "PROCESSOR",
                    entityId = savedProcessor.id,
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

                    println("Associated EUDR Compliance Certificate NFT with processor account: ${hederaAccountResult.accountId}")
                }
            } catch (e: Exception) {
                println("Failed to store Hedera credentials or associate NFT: ${e.message}")
            }
        }

        return mapToResponseDto(savedProcessor)
    }

    fun updateProcessor(processorId: String, dto: UpdateProcessorRequestDto): ProcessorResponseDto {
        val processor = processorRepository.findById(processorId)
            .orElseThrow { NoSuchElementException("Processor not found with id: $processorId") }

        dto.facilityName?.let { processor.facilityName = it }
        dto.facilityAddress?.let { processor.facilityAddress = it }
        dto.processingCapabilities?.let { processor.processingCapabilities = it }
        dto.certifications?.let { processor.certificationDetails = it }

        val updated = processorRepository.save(processor)
        return mapToResponseDto(updated)
    }

    fun verifyProcessor(processorId: String, status: ProcessorVerificationStatus): ProcessorResponseDto {
        val processor = processorRepository.findById(processorId)
            .orElseThrow { NoSuchElementException("Processor not found with id: $processorId") }

        processor.verificationStatus = status

        val updated = processorRepository.save(processor)
        return mapToResponseDto(updated)
    }

    fun getProcessorById(processorId: String): ProcessorResponseDto {
        val processor = processorRepository.findById(processorId)
            .orElseThrow { NoSuchElementException("Processor not found with id: $processorId") }
        return mapToResponseDto(processor)
    }

    fun getAllProcessors(pageable: Pageable): Page<ProcessorResponseDto> {
        return processorRepository.findAll(pageable).map { mapToResponseDto(it) }
    }

    fun getProcessorsByVerificationStatus(status: ProcessorVerificationStatus, pageable: Pageable): Page<ProcessorResponseDto> {
        return processorRepository.findByVerificationStatus(status, pageable).map { mapToResponseDto(it) }
    }

    fun getProcessorStatistics(processorId: String): ProcessorStatisticsDto {
        val processor = processorRepository.findById(processorId)
            .orElseThrow { NoSuchElementException("Processor not found with id: $processorId") }

        val allEvents = processingEventRepository.findByProcessor_Id(processorId)

        val totalBatchesProcessed = allEvents.size
        val totalInputQuantityKg = allEvents.sumOf { it.inputQuantity }
        val totalOutputQuantityKg = allEvents.sumOf { it.outputQuantity }
        val averageYieldPercentage = if (totalInputQuantityKg > BigDecimal.ZERO) {
            totalOutputQuantityKg.divide(totalInputQuantityKg, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100))
        } else {
            BigDecimal.ZERO
        }

        val currentMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        val currentMonthProcessingKg = allEvents
            .filter { it.processingDate.isAfter(currentMonth) }
            .sumOf { it.inputQuantity }

        val processingTypeStats = allEvents.groupBy { it.processingType }
            .map { (type, events) ->
                val inputKg = events.sumOf { it.inputQuantity }
                val outputKg = events.sumOf { it.outputQuantity }
                val yieldPct = if (inputKg > BigDecimal.ZERO) {
                    outputKg.divide(inputKg, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100))
                } else {
                    BigDecimal.ZERO
                }
                ProcessingTypeSummaryDto(
                    processingType = type,
                    totalBatchesProcessed = events.size,
                    totalInputQuantityKg = inputKg,
                    totalOutputQuantityKg = outputKg,
                    averageYieldPercentage = yieldPct
                )
            }
            .sortedByDescending { it.totalBatchesProcessed }
            .take(5)

        return ProcessorStatisticsDto(
            processorId = processorId,
            totalBatchesProcessed = totalBatchesProcessed,
            totalInputQuantityKg = totalInputQuantityKg,
            totalOutputQuantityKg = totalOutputQuantityKg,
            averageYieldPercentage = averageYieldPercentage,
            currentMonthProcessingKg = currentMonthProcessingKg,
            topProcessingTypes = processingTypeStats,
            averageProcessingTimeHours = null
        )
    }

    fun createProcessingEvent(dto: CreateProcessingEventRequestDto): ProcessingEventResponseDto {
        val processor = processorRepository.findById(dto.processorId)
            .orElseThrow { NoSuchElementException("Processor not found with id: ${dto.processorId}") }

        val batch = eudrBatchRepository.findById(dto.batchId)
            .orElseThrow { NoSuchElementException("Batch not found with id: ${dto.batchId}") }

        val event = ProcessingEvent(
            batch = batch,
            processor = processor,
            processingType = dto.processingType,
            inputQuantity = dto.inputQuantityKg,
            outputQuantity = dto.outputQuantityKg,
            processingDate = dto.processingDate,
            processingNotes = dto.processingNotes,
            hederaTransactionId = null
        )

        val savedEvent = processingEventRepository.save(event)

        // Record processing event on Hedera blockchain
        try {
            val hederaTransactionId = hederaConsensusService.recordProcessingEvent(savedEvent)
            savedEvent.hederaTransactionId = hederaTransactionId
            processingEventRepository.save(savedEvent)
            println("Processing event recorded on Hedera: $hederaTransactionId")
        } catch (e: Exception) {
            println("Failed to record processing event on Hedera: ${e.message}")
        }

        return mapToEventResponseDto(savedEvent)
    }

    fun getProcessingEventsByProcessor(processorId: String, pageable: Pageable): Page<ProcessingEventResponseDto> {
        val events = processingEventRepository.findByProcessor_Id(processorId, pageable)
        return events.map { event ->
            mapToEventResponseDto(event)
        }
    }

    fun getProcessingEventById(eventId: String): ProcessingEventResponseDto {
        val event = processingEventRepository.findById(eventId)
            .orElseThrow { NoSuchElementException("Processing event not found with id: $eventId") }

        return mapToEventResponseDto(event)
    }

    fun getProcessingEventsByBatch(batchId: String, pageable: Pageable): Page<ProcessingEventResponseDto> {
        val events = processingEventRepository.findByBatch_Id(batchId, pageable)
        return events.map { event ->
            mapToEventResponseDto(event)
        }
    }

    // ============================================
    // MAPPING FUNCTIONS
    // ============================================

    private fun mapToResponseDto(processor: Processor): ProcessorResponseDto {
        val totalBatchesProcessed = processingEventRepository.countByProcessor_Id(processor.id)

        return ProcessorResponseDto(
            id = processor.id,
            facilityName = processor.facilityName,
            facilityAddress = processor.facilityAddress,
            processorType = null,
            processingCapabilities = processor.processingCapabilities,
            capacityPerDayKg = null,
            certifications = processor.certificationDetails,
            verificationStatus = processor.verificationStatus,
            totalBatchesProcessed = totalBatchesProcessed,
            hederaAccountId = null,
            createdAt = processor.createdAt,
            updatedAt = processor.createdAt, // Use createdAt since no updatedAt field exists
            userProfile = UserProfileSummaryDto(
                id = processor.userProfile.id,
                fullName = processor.userProfile.fullName,
                email = processor.userProfile.email,
                phoneNumber = processor.userProfile.phoneNumber
            )
        )
    }

    private fun mapToEventResponseDto(event: ProcessingEvent): ProcessingEventResponseDto {
        val yieldPercentage = if (event.inputQuantity > BigDecimal.ZERO) {
            event.outputQuantity.divide(event.inputQuantity, 4, RoundingMode.HALF_UP).multiply(BigDecimal(100))
        } else {
            BigDecimal.ZERO
        }

        return ProcessingEventResponseDto(
            id = event.id,
            processorId = event.processor.id,
            processorName = event.processor.facilityName,
            batchId = event.batch.id,
            batchCode = event.batch.batchCode,
            processingType = event.processingType,
            inputQuantityKg = event.inputQuantity,
            outputQuantityKg = event.outputQuantity,
            yieldPercentage = yieldPercentage,
            processingDate = event.processingDate,
            processingNotes = event.processingNotes,
            qualityMetrics = null,
            temperatureLog = null,
            hederaTransactionId = event.hederaTransactionId,
            createdAt = event.createdAt
        )
    }

    // ============================================
    // CONNECTION MANAGEMENT METHODS
    // ============================================

    /**
     * Get processors connected to an exporter
     */
    fun getConnectedProcessors(exporterId: String, pageable: Pageable): Page<ProcessorResponseDto> {
        val connections = exporterProcessorConnectionRepository.findByExporterIdAndStatus(
            exporterId, 
            com.agriconnect.farmersportalapis.domain.eudr.ConnectionStatus.ACTIVE
        )
        
        val processorIds = connections.map { it.processor.id }
        val processors = if (processorIds.isNotEmpty()) {
            processorRepository.findAllById(processorIds)
        } else {
            emptyList()
        }
        
        // Manual pagination since we're filtering in memory
        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, processors.size)
        val pageContent = if (start < processors.size) processors.subList(start, end) else emptyList()
        
        return org.springframework.data.domain.PageImpl(
            pageContent.map { mapToResponseDto(it) },
            pageable,
            processors.size.toLong()
        )
    }

    /**
     * Get processors available to connect (not yet connected to the exporter)
     */
    fun getAvailableProcessors(exporterId: String, pageable: Pageable): Page<ProcessorResponseDto> {
        val connections = exporterProcessorConnectionRepository.findByExporterId(exporterId)
        val connectedProcessorIds = connections.map { it.processor.id }.toSet()
        
        // Get all processors and filter out connected ones
        val allProcessors = processorRepository.findAll()
        val availableProcessors = allProcessors.filter { it.id !in connectedProcessorIds }
        
        // Manual pagination
        val start = pageable.offset.toInt()
        val end = minOf(start + pageable.pageSize, availableProcessors.size)
        val pageContent = if (start < availableProcessors.size) availableProcessors.subList(start, end) else emptyList()
        
        return org.springframework.data.domain.PageImpl(
            pageContent.map { mapToResponseDto(it) },
            pageable,
            availableProcessors.size.toLong()
        )
    }

    /**
     * Connect an exporter to a processor
     */
    fun connectExporterToProcessor(exporterId: String, processorId: String, notes: String? = null): Boolean {
        // Check if connection already exists
        if (exporterProcessorConnectionRepository.existsByExporterIdAndProcessorId(exporterId, processorId)) {
            throw IllegalStateException("Connection already exists between exporter and processor")
        }
        
        // Validate exporter and processor exist
        val exporter = exporterRepository.findById(exporterId)
            .orElseThrow { NoSuchElementException("Exporter not found with id: $exporterId") }
            
        val processor = processorRepository.findById(processorId)
            .orElseThrow { NoSuchElementException("Processor not found with id: $processorId") }
        
        // Create connection
        val connection = com.agriconnect.farmersportalapis.domain.eudr.ExporterProcessorConnection(
            exporter = exporter,
            processor = processor,
            status = com.agriconnect.farmersportalapis.domain.eudr.ConnectionStatus.ACTIVE,
            notes = notes
        )
        
        exporterProcessorConnectionRepository.save(connection)
        return true
    }

    /**
     * Disconnect an exporter from a processor
     */
    fun disconnectExporterFromProcessor(exporterId: String, processorId: String): Boolean {
        val connection = exporterProcessorConnectionRepository.findByExporterIdAndProcessorId(exporterId, processorId)
            ?: throw NoSuchElementException("Connection not found")
        
        exporterProcessorConnectionRepository.delete(connection)
        return true
    }
}