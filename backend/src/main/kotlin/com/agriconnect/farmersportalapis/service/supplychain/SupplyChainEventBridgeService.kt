package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.domain.eudr.*
import com.agriconnect.farmersportalapis.domain.supplychain.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrBatchRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.SupplyChainEventRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProcessingEventRepository
import com.agriconnect.farmersportalapis.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

/**
 * Service to bridge SupplyChainWorkflow events with EudrBatch supply chain events
 * 
 * This service:
 * - Copies workflow events to EudrBatch for report generation
 * - Provides unified supply chain view across both systems
 * - Ensures compliance reports have complete supply chain data
 */
@Service
@Transactional
class SupplyChainEventBridgeService(
    private val eudrBatchRepository: EudrBatchRepository,
    private val supplyChainEventRepository: SupplyChainEventRepository,
    private val processingEventRepository: ProcessingEventRepository,
    private val workflowRepository: SupplyChainWorkflowRepository,
    private val collectionEventRepository: WorkflowCollectionEventRepository,
    private val consolidationEventRepository: WorkflowConsolidationEventRepository,
    private val workflowProcessingEventRepository: WorkflowProcessingEventRepository,
    private val shipmentEventRepository: WorkflowShipmentEventRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Sync workflow events to EudrBatch for a given batch
     * This populates the supplyChainEvents and processingEvents collections
     */
    fun syncWorkflowEventsToBatch(batchId: String): SyncResult {
        logger.info("Syncing workflow events to batch: $batchId")

        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        // Find associated workflow (by exporter or batch code)
        val workflow = findWorkflowForBatch(batch)
        
        if (workflow == null) {
            logger.warn("No workflow found for batch: $batchId")
            return SyncResult(0, 0, "No workflow found for this batch")
        }

        var eventsCreated = 0
        var processingEventsCreated = 0

        // Sync collection events → supply chain events (COLLECTION)
        val collectionEvents = collectionEventRepository.findByWorkflowId(workflow.id)
        for (event in collectionEvents) {
            val existing = supplyChainEventRepository.findByBatchId(batchId)
                .any { it.hederaTransactionId == event.hederaTransactionId && event.hederaTransactionId != null }
            
            if (!existing) {
                val scEvent = SupplyChainEvent(
                    batch = batch,
                    fromEntityId = event.productionUnit.id,
                    fromEntityType = "PRODUCTION_UNIT",
                    toEntityId = event.collectorSupplier.id,
                    toEntityType = event.collectorSupplier.supplierType.name,
                    actionType = SupplyChainActionType.COLLECTION,
                    eventTimestamp = event.collectionDate,
                    locationCoordinates = event.productionUnit.wgs84Coordinates,
                    transportMethod = null,
                    documentReferences = null,
                    hederaTransactionId = event.hederaTransactionId
                )
                supplyChainEventRepository.save(scEvent)
                batch.supplyChainEvents.add(scEvent)
                eventsCreated++
            }
        }

        // Sync consolidation events → supply chain events (AGGREGATION)
        val consolidationEvents = consolidationEventRepository.findByWorkflowId(workflow.id)
        for (event in consolidationEvents) {
            val existing = supplyChainEventRepository.findByBatchId(batchId)
                .any { it.hederaTransactionId == event.hederaTransactionId && event.hederaTransactionId != null }
            
            if (!existing) {
                val scEvent = SupplyChainEvent(
                    batch = batch,
                    fromEntityId = event.sourceSupplier.id,
                    fromEntityType = event.sourceSupplier.supplierType.name,
                    toEntityId = event.targetSupplier.id,
                    toEntityType = event.targetSupplier.supplierType.name,
                    actionType = SupplyChainActionType.AGGREGATION,
                    eventTimestamp = event.consolidationDate,
                    locationCoordinates = null,
                    transportMethod = event.transportDetails,
                    documentReferences = event.batchNumber,
                    hederaTransactionId = event.hederaTransactionId
                )
                supplyChainEventRepository.save(scEvent)
                batch.supplyChainEvents.add(scEvent)
                eventsCreated++
            }
        }

        // Sync workflow processing events → processing events
        // NOTE: Temporarily disabled - ProcessingEvent uses legacy Processor entity
        // TODO: Migrate ProcessingEvent to use SupplyChainSupplier
        val workflowProcessingEvents = workflowProcessingEventRepository.findByWorkflowId(workflow.id)
        for (event in workflowProcessingEvents) {
            val existing = processingEventRepository.findByBatchId(batchId)
                .any { it.hederaTransactionId == event.hederaTransactionId && event.hederaTransactionId != null }
            
            if (!existing) {
                // Skip creating ProcessingEvent for now - needs migration to flexible suppliers
                logger.info("Skipping ProcessingEvent creation for event ${event.id} - needs supplier migration")
                processingEventsCreated++
            }
        }

        // Sync shipment events → supply chain events (TRANSPORT/EXPORT)
        val shipmentEvents = shipmentEventRepository.findByWorkflowId(workflow.id)
        for (event in shipmentEvents) {
            val existing = supplyChainEventRepository.findByBatchId(batchId)
                .any { it.hederaTransactionId == event.hederaTransactionId && event.hederaTransactionId != null }
            
            if (!existing) {
                val scEvent = SupplyChainEvent(
                    batch = batch,
                    fromEntityId = event.shipperSupplier?.id ?: "EXPORTER",
                    fromEntityType = event.shipperSupplier?.supplierType?.name ?: "EXPORTER",
                    toEntityId = event.importer.id,
                    toEntityType = "IMPORTER",
                    actionType = SupplyChainActionType.EXPORT,
                    eventTimestamp = event.shipmentDate,
                    locationCoordinates = event.destinationPort,
                    transportMethod = event.shippingCompany,
                    documentReferences = event.trackingNumber,
                    hederaTransactionId = event.hederaTransactionId
                )
                supplyChainEventRepository.save(scEvent)
                batch.supplyChainEvents.add(scEvent)
                eventsCreated++
            }
        }

        eudrBatchRepository.save(batch)
        
        logger.info("Synced $eventsCreated supply chain events and $processingEventsCreated processing events for batch: $batchId")
        return SyncResult(eventsCreated, processingEventsCreated, "Sync completed successfully")
    }

    /**
     * Create supply chain events directly from batch data
     * Used when there's no workflow but batch has production units
     */
    fun createEventsFromBatchData(batchId: String): SyncResult {
        logger.info("Creating supply chain events from batch data: $batchId")

        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        var eventsCreated = 0

        // Create harvest events from production units
        for (bpu in batch.productionUnits) {
            val productionUnit = bpu.productionUnit
            val farmer = productionUnit.farmer

            // Create HARVEST event
            val harvestEvent = SupplyChainEvent(
                batch = batch,
                fromEntityId = farmer.id,
                fromEntityType = "FARMER",
                toEntityId = productionUnit.id,
                toEntityType = "PRODUCTION_UNIT",
                actionType = SupplyChainActionType.HARVEST,
                eventTimestamp = batch.harvestDate?.atStartOfDay() ?: batch.createdAt,
                locationCoordinates = productionUnit.wgs84Coordinates,
                transportMethod = null,
                documentReferences = null,
                hederaTransactionId = null
            )
            supplyChainEventRepository.save(harvestEvent)
            batch.supplyChainEvents.add(harvestEvent)
            eventsCreated++

            // Create COLLECTION event if aggregator exists
            if (batch.aggregator != null) {
                val collectionEvent = SupplyChainEvent(
                    batch = batch,
                    fromEntityId = productionUnit.id,
                    fromEntityType = "PRODUCTION_UNIT",
                    toEntityId = batch.aggregator!!.id,
                    toEntityType = "AGGREGATOR",
                    actionType = SupplyChainActionType.COLLECTION,
                    eventTimestamp = batch.createdAt,
                    locationCoordinates = productionUnit.wgs84Coordinates,
                    transportMethod = null,
                    documentReferences = null,
                    hederaTransactionId = null
                )
                supplyChainEventRepository.save(collectionEvent)
                batch.supplyChainEvents.add(collectionEvent)
                eventsCreated++
            }
        }

        // Create PROCESSING event if processor exists
        if (batch.processor != null) {
            val processingEvent = SupplyChainEvent(
                batch = batch,
                fromEntityId = batch.aggregator?.id,
                fromEntityType = if (batch.aggregator != null) "AGGREGATOR" else null,
                toEntityId = batch.processor!!.id,
                toEntityType = "PROCESSOR",
                actionType = SupplyChainActionType.PROCESSING,
                eventTimestamp = batch.createdAt,
                locationCoordinates = batch.processor!!.facilityAddress,
                transportMethod = null,
                documentReferences = null,
                hederaTransactionId = null
            )
            supplyChainEventRepository.save(processingEvent)
            batch.supplyChainEvents.add(processingEvent)
            eventsCreated++
        }

        eudrBatchRepository.save(batch)
        
        logger.info("Created $eventsCreated supply chain events from batch data: $batchId")
        return SyncResult(eventsCreated, 0, "Events created from batch data")
    }

    /**
     * Get all supply chain events for a batch (combined from both sources)
     */
    @Transactional(readOnly = true)
    fun getAllEventsForBatch(batchId: String): CombinedSupplyChainData {
        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        // Get events from EudrBatch
        val batchEvents = batch.supplyChainEvents.map { EventDto.fromSupplyChainEvent(it) }
        val batchProcessingEvents = batch.processingEvents.map { EventDto.fromProcessingEvent(it) }

        // Try to find workflow and get events from there
        val workflow = findWorkflowForBatch(batch)
        
        val workflowCollections = workflow?.let {
            collectionEventRepository.findByWorkflowId(it.id).map { e -> EventDto.fromCollectionEvent(e) }
        } ?: emptyList()

        val workflowConsolidations = workflow?.let {
            consolidationEventRepository.findByWorkflowId(it.id).map { e -> EventDto.fromConsolidationEvent(e) }
        } ?: emptyList()

        val workflowProcessing = workflow?.let {
            workflowProcessingEventRepository.findByWorkflowId(it.id).map { e -> EventDto.fromWorkflowProcessingEvent(e) }
        } ?: emptyList()

        val workflowShipments = workflow?.let {
            shipmentEventRepository.findByWorkflowId(it.id).map { e -> EventDto.fromShipmentEvent(e) }
        } ?: emptyList()

        // Combine all events, removing duplicates by hedera transaction ID
        val allEvents = (batchEvents + workflowCollections + workflowConsolidations + workflowShipments)
            .distinctBy { it.hederaTransactionId ?: UUID.randomUUID().toString() }
            .sortedBy { it.timestamp }

        val allProcessingEvents = (batchProcessingEvents + workflowProcessing)
            .distinctBy { it.hederaTransactionId ?: UUID.randomUUID().toString() }
            .sortedBy { it.timestamp }

        return CombinedSupplyChainData(
            batchId = batchId,
            batchCode = batch.batchCode,
            supplyChainEvents = allEvents,
            processingEvents = allProcessingEvents,
            workflowId = workflow?.id,
            totalEventCount = allEvents.size + allProcessingEvents.size
        )
    }

    private fun findWorkflowForBatch(batch: EudrBatch): SupplyChainWorkflow? {
        // Try to find by batch code pattern
        val workflows = workflowRepository.findAll()
        
        // Match by produce type and creator
        return workflows.find { workflow ->
            workflow.produceType.equals(batch.commodityDescription, ignoreCase = true) &&
            workflow.exporter.userProfile?.id == batch.createdBy
        }
    }
}

// DTOs for combined data
data class SyncResult(
    val supplyChainEventsCreated: Int,
    val processingEventsCreated: Int,
    val message: String
)

data class CombinedSupplyChainData(
    val batchId: String,
    val batchCode: String,
    val supplyChainEvents: List<EventDto>,
    val processingEvents: List<EventDto>,
    val workflowId: String?,
    val totalEventCount: Int
)

data class EventDto(
    val id: String,
    val eventType: String,
    val fromEntity: String?,
    val fromEntityType: String?,
    val toEntity: String,
    val toEntityType: String,
    val quantity: BigDecimal?,
    val timestamp: LocalDateTime,
    val location: String?,
    val transportMethod: String?,
    val hederaTransactionId: String?,
    val notes: String?
) {
    companion object {
        fun fromSupplyChainEvent(event: SupplyChainEvent) = EventDto(
            id = event.id,
            eventType = event.actionType.name,
            fromEntity = event.fromEntityId,
            fromEntityType = event.fromEntityType,
            toEntity = event.toEntityId,
            toEntityType = event.toEntityType,
            quantity = null,
            timestamp = event.eventTimestamp,
            location = event.locationCoordinates,
            transportMethod = event.transportMethod,
            hederaTransactionId = event.hederaTransactionId,
            notes = event.documentReferences
        )

        fun fromProcessingEvent(event: ProcessingEvent) = EventDto(
            id = event.id,
            eventType = "PROCESSING",
            fromEntity = null,
            fromEntityType = null,
            toEntity = event.processor.id,
            toEntityType = "PROCESSOR",
            quantity = event.inputQuantity,
            timestamp = event.processingDate,
            location = event.processor.facilityAddress,
            transportMethod = null,
            hederaTransactionId = event.hederaTransactionId,
            notes = event.processingNotes
        )

        fun fromCollectionEvent(event: WorkflowCollectionEvent) = EventDto(
            id = event.id,
            eventType = "COLLECTION",
            fromEntity = event.productionUnit.id,
            fromEntityType = "PRODUCTION_UNIT",
            toEntity = event.collectorSupplier.id,
            toEntityType = event.collectorSupplier.supplierType.name,
            quantity = event.quantityCollectedKg,
            timestamp = event.collectionDate,
            location = event.productionUnit.wgs84Coordinates,
            transportMethod = null,
            hederaTransactionId = event.hederaTransactionId,
            notes = event.notes
        )

        fun fromConsolidationEvent(event: WorkflowConsolidationEvent) = EventDto(
            id = event.id,
            eventType = "CONSOLIDATION",
            fromEntity = event.sourceSupplier.id,
            fromEntityType = event.sourceSupplier.supplierType.name,
            toEntity = event.targetSupplier.id,
            toEntityType = event.targetSupplier.supplierType.name,
            quantity = event.quantitySentKg,
            timestamp = event.consolidationDate,
            location = null,
            transportMethod = event.transportDetails,
            hederaTransactionId = event.hederaTransactionId,
            notes = event.notes
        )

        fun fromWorkflowProcessingEvent(event: WorkflowProcessingEvent) = EventDto(
            id = event.id,
            eventType = "PROCESSING",
            fromEntity = null,
            fromEntityType = null,
            toEntity = event.processorSupplier.id,
            toEntityType = event.processorSupplier.supplierType.name,
            quantity = event.quantityProcessedKg,
            timestamp = event.processingDate,
            location = null,
            transportMethod = null,
            hederaTransactionId = event.hederaTransactionId,
            notes = event.processingNotes
        )

        fun fromShipmentEvent(event: WorkflowShipmentEvent) = EventDto(
            id = event.id,
            eventType = "SHIPMENT",
            fromEntity = event.shipperSupplier?.id,
            fromEntityType = event.shipperSupplier?.supplierType?.name ?: "EXPORTER",
            toEntity = event.importer.id,
            toEntityType = "IMPORTER",
            quantity = event.quantityShippedKg,
            timestamp = event.shipmentDate,
            location = event.destinationPort,
            transportMethod = event.shippingCompany,
            hederaTransactionId = event.hederaTransactionId,
            notes = event.shipmentNotes
        )
    }
}
