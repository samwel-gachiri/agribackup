package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.domain.eudr.ProcessingEvent
import com.agriconnect.farmersportalapis.domain.eudr.SupplyChainActionType
import com.agriconnect.farmersportalapis.domain.eudr.SupplyChainEvent
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrBatchRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProcessingEventRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProcessorRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.SupplyChainEventRepository
import com.agriconnect.farmersportalapis.service.hedera.HederaConsensusServices
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class SupplyChainService(
    private val supplyChainEventRepository: SupplyChainEventRepository,
    private val processingEventRepository: ProcessingEventRepository,
    private val eudrBatchRepository: EudrBatchRepository,
    private val processorRepository: ProcessorRepository,
    private val hederaConsensusService: HederaConsensusServices
) {

    private val logger = LoggerFactory.getLogger(SupplyChainService::class.java)

    @Transactional
    fun recordSupplyChainEvent(
        batchId: String,
        fromEntityId: String?,
        fromEntityType: String?,
        toEntityId: String,
        toEntityType: String,
        actionType: SupplyChainActionType,
        eventTimestamp: LocalDateTime = LocalDateTime.now(),
        locationCoordinates: String? = null,
        transportMethod: String? = null,
        documentReferences: String? = null
    ): SupplyChainEvent {
        logger.info("Recording supply chain event for batch $batchId: $actionType from $fromEntityType:$fromEntityId to $toEntityType:$toEntityId")

        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        val event = SupplyChainEvent(
            batch = batch,
            fromEntityId = fromEntityId,
            fromEntityType = fromEntityType,
            toEntityId = toEntityId,
            toEntityType = toEntityType,
            actionType = actionType,
            eventTimestamp = eventTimestamp,
            locationCoordinates = locationCoordinates,
            transportMethod = transportMethod,
            documentReferences = documentReferences,
            hederaTransactionId = null
        )

        val savedEvent = supplyChainEventRepository.save(event)

        // Record on Hedera for immutable audit trail
        try {
            val hederaTransactionId = hederaConsensusService.recordSupplyChainEvent(savedEvent)
            savedEvent.hederaTransactionId = hederaTransactionId
            supplyChainEventRepository.save(savedEvent)
            logger.info("Supply chain event recorded on Hedera with transaction ID: $hederaTransactionId")
        } catch (e: Exception) {
            logger.error("Failed to record supply chain event on Hedera", e)
        }

        return savedEvent
    }

    @Transactional
    fun recordProcessingEvent(
        batchId: String,
        processorId: String,
        processingType: String,
        inputQuantity: BigDecimal,
        outputQuantity: BigDecimal,
        processingDate: LocalDateTime,
        processingNotes: String? = null
    ): ProcessingEvent {
        logger.info("Recording processing event for batch $batchId by processor $processorId")

        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        val processor = processorRepository.findById(processorId)
            .orElseThrow { IllegalArgumentException("Processor not found: $processorId") }

        val event = ProcessingEvent(
            batch = batch,
            processor = processor,
            processingType = processingType,
            inputQuantity = inputQuantity,
            outputQuantity = outputQuantity,
            processingDate = processingDate,
            processingNotes = processingNotes,
            hederaTransactionId = null
        )

        val savedEvent = processingEventRepository.save(event)

        // Record on Hedera for immutable audit trail
        try {
            val hederaTransactionId = hederaConsensusService.recordProcessingEvent(savedEvent)
            savedEvent.hederaTransactionId = hederaTransactionId
            processingEventRepository.save(savedEvent)
            logger.info("Processing event recorded on Hedera with transaction ID: $hederaTransactionId")
        } catch (e: Exception) {
            logger.error("Failed to record processing event on Hedera", e)
        }

        return savedEvent
    }

    fun getSupplyChainEventsForBatch(batchId: String): List<SupplyChainEvent> {
        return supplyChainEventRepository.findByBatchId(batchId)
    }

    fun getProcessingEventsForBatch(batchId: String): List<ProcessingEvent> {
        return processingEventRepository.findByBatchId(batchId)
    }

    fun getAllSupplyChainEvents(): List<SupplyChainEvent> {
        return supplyChainEventRepository.findAll()
    }

    fun getAllProcessingEvents(): List<ProcessingEvent> {
        return processingEventRepository.findAll()
    }
}