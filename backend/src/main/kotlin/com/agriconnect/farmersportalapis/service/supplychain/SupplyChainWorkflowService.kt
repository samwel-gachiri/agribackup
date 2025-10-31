package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.domain.eudr.ConsolidatedBatch
import com.agriconnect.farmersportalapis.domain.eudr.ConsolidatedBatchStatus
import com.agriconnect.farmersportalapis.domain.supplychain.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import com.agriconnect.farmersportalapis.repository.*
import com.agriconnect.farmersportalapis.service.hedera.HederaMainService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class SupplyChainWorkflowService(
    private val workflowRepository: SupplyChainWorkflowRepository,
    private val collectionEventRepository: WorkflowCollectionEventRepository,
    private val consolidationEventRepository: WorkflowConsolidationEventRepository,
    private val processingEventRepository: WorkflowProcessingEventRepository,
    private val shipmentEventRepository: WorkflowShipmentEventRepository,
    private val exporterRepository: ExporterRepository,
    private val productionUnitRepository: ProductionUnitRepository,
    private val aggregatorRepository: AggregatorRepository,
    private val eudrBatchService: EudrBatchService,
    private val processorRepository: ProcessorRepository,
    private val consolidatedBatchRepository: ConsolidatedBatchRepository,
    private val importerRepository: ImporterRepository,
    private val farmerRepository: FarmerRepository,
    private val hederaMainService: HederaMainService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // ===== CREATE WORKFLOW =====
    fun createWorkflow(exporterId: String, request: CreateWorkflowRequestDto): WorkflowResponseDto {
        val exporter = exporterRepository.findById(exporterId)
            .orElseThrow { IllegalArgumentException("Exporter not found") }

        val workflow = SupplyChainWorkflow(
            id = UUID.randomUUID().toString(),
            exporter = exporter,
            workflowName = request.workflowName,
            produceType = request.produceType,
            status = WorkflowStatus.IN_PROGRESS,
            currentStage = WorkflowStage.COLLECTION,
            totalQuantityKg = BigDecimal.ZERO,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val saved = workflowRepository.save(workflow)
        return toWorkflowResponseDto(saved)
    }

    // ===== GET WORKFLOWS =====
    fun getWorkflowsByExporter(exporterId: String, pageable: Pageable): Page<WorkflowResponseDto> {
        val workflows = workflowRepository.findByExporterId(exporterId, pageable)
        val dtos = workflows.content.map { toWorkflowResponseDto(it) }
        return PageImpl(dtos, pageable, workflows.totalElements)
    }

    fun getWorkflowById(workflowId: String): WorkflowResponseDto {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }
        return toWorkflowResponseDto(workflow)
    }

    fun getWorkflowSummary(workflowId: String): WorkflowSummaryDto {
        val workflow = getWorkflowById(workflowId)
        val collections = getCollectionEvents(workflowId)
        val consolidations = getConsolidationEvents(workflowId)
        val processings = getProcessingEvents(workflowId)
        val shipments = getShipmentEvents(workflowId)

        return WorkflowSummaryDto(
            workflow = workflow,
            collectionEvents = collections,
            consolidationEvents = consolidations,
            processingEvents = processings,
            shipmentEvents = shipments
        )
    }

    // ===== COLLECTION EVENTS (Production Unit → Aggregator) =====
    fun addCollectionEvent(workflowId: String, request: AddCollectionEventRequestDto): WorkflowCollectionEventResponseDto {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }

        val productionUnit = productionUnitRepository.findById(request.productionUnitId)
            .orElseThrow { IllegalArgumentException("Production unit not found") }

        val aggregator = aggregatorRepository.findById(request.aggregatorId)
            .orElseThrow { IllegalArgumentException("Aggregator not found") }

        val farmer = farmerRepository.findById(request.farmerId)
            .orElseThrow { IllegalArgumentException("Farmer not found") }

        val event = WorkflowCollectionEvent(
            id = UUID.randomUUID().toString(),
            workflow = workflow,
            productionUnit = productionUnit,
            aggregator = aggregator,
            farmer = farmer,
            quantityCollectedKg = request.quantityCollectedKg,
            collectionDate = request.collectionDate,
            qualityGrade = request.qualityGrade,
            notes = request.notes,
            createdAt = LocalDateTime.now()
        )

        val saved = collectionEventRepository.save(event)

        // Record collection event on Hedera blockchain
        try {
            logger.info("Recording collection event ${saved.id} to Hedera blockchain")
            val hederaTransactionId = hederaMainService.recordAggregationEvent(
                eventId = saved.id,
                aggregatorId = aggregator.id!!,
                farmerId = farmer.id!!,
                produceType = workflow.produceType,
                quantityKg = request.quantityCollectedKg,
                collectionDate = request.collectionDate.toLocalDate()
            )
            logger.info("Collection event ${saved.id} recorded on Hedera with transaction ID: $hederaTransactionId")

            // Update event with Hedera transaction ID
            saved.hederaTransactionId = hederaTransactionId
            collectionEventRepository.save(saved)
        } catch (e: Exception) {
            logger.error("Failed to record collection event to Hedera blockchain", e)
            // Don't fail the entire transaction, just log the error
        }

        // Update workflow total quantity and stage
        updateWorkflowQuantityAndStage(workflow)

        return toCollectionEventResponseDto(saved)
    }

    fun getCollectionEvents(workflowId: String): List<WorkflowCollectionEventResponseDto> {
        return collectionEventRepository.findByWorkflowId(workflowId)
            .map { toCollectionEventResponseDto(it) }
    }

    // ===== CONSOLIDATION EVENTS (Aggregator → Processor) =====
    fun addConsolidationEvent(workflowId: String, request: AddConsolidationEventRequestDto): WorkflowConsolidationEventResponseDto {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }

        val aggregator = aggregatorRepository.findById(request.aggregatorId)
            .orElseThrow { IllegalArgumentException("Aggregator not found") }

        val processor = processorRepository.findById(request.processorId)
            .orElseThrow { IllegalArgumentException("Processor not found") }

        // Validate: Check if aggregator has enough quantity available
        val availableQuantity = getAvailableQuantityForAggregator(workflowId, request.aggregatorId)
        if (request.quantitySentKg > availableQuantity) {
            throw IllegalArgumentException("Insufficient quantity. Available: $availableQuantity kg, Requested: ${request.quantitySentKg} kg")
        }

        val event = WorkflowConsolidationEvent(
            id = UUID.randomUUID().toString(),
            workflow = workflow,
            aggregator = aggregator,
            processor = processor,
            quantitySentKg = request.quantitySentKg,
            consolidationDate = request.consolidationDate,
            transportDetails = request.transportDetails,
            batchNumber = request.batchNumber,
            notes = request.notes,
            createdAt = LocalDateTime.now()
        )

        val saved = consolidationEventRepository.save(event)

        // ===== CREATE CONSOLIDATED BATCH =====
        // Get number of unique farmers from collection events for this aggregator
        val collectionEvents = collectionEventRepository.findByWorkflowId(workflowId)
            .filter { it.aggregator.id == aggregator.id }
        val numberOfFarmers = collectionEvents.map { it.farmer.id }.distinct().count()

        // Calculate average quality grade from collection events
        val qualityGrades = collectionEvents.mapNotNull { it.qualityGrade }
        val averageQualityGrade = if (qualityGrades.isNotEmpty()) {
            qualityGrades.groupBy { it }.maxByOrNull { it.value.size }?.key
        } else null

        val consolidatedBatch = ConsolidatedBatch(
            aggregator = aggregator,
            batchNumber = request.batchNumber ?: "BATCH-${saved.id.substring(0, 8)}",
            produceType = workflow.produceType,
            totalQuantityKg = request.quantitySentKg,
            numberOfFarmers = numberOfFarmers,
            averageQualityGrade = averageQualityGrade,
            consolidationDate = request.consolidationDate,
            destinationEntityId = processor.id,
            destinationEntityType = "PROCESSOR",
            status = ConsolidatedBatchStatus.CREATED,
            transportDetails = request.transportDetails,
            hederaTransactionId = null, // Will be set below
            hederaBatchHash = null,
            shipmentDate = null,
            deliveryDate = null,
        )

        val savedConsolidatedBatch = consolidatedBatchRepository.save(consolidatedBatch)

        // ===== CREATE EUDR BATCH FOR COMPLIANCE =====
        // Extract country from aggregator's facility address or use default
        val countryOfProduction = aggregator.facilityAddress
            ?.split(",")
            ?.lastOrNull()
            ?.trim() ?: "Unknown"

        // Create EUDR batch code using the consolidation batch number
        val eudrBatchCode = "EUDR-${savedConsolidatedBatch.batchNumber}"

        val createBatchRequest = CreateBatchRequestDto(
            batchCode = eudrBatchCode,
            commodityDescription = "${workflow.produceType} - Consolidated Batch",
            hsCode = null, // Could be derived from produce type if needed
            quantity = request.quantitySentKg,
            unit = "kg",
            countryOfProduction = countryOfProduction,
            harvestDate = null, // Could be derived from collection events
            harvestPeriodStart = null,
            harvestPeriodEnd = null,
            productionUnitIds = collectionEvents.map { it.productionUnit.id }.distinct()
        )

        eudrBatchService.createBatch(createBatchRequest, aggregator.id!!)

        // Record consolidation event on Hedera blockchain
        try {
            // Get number of unique farmers from collection events
            val collectionEvents = collectionEventRepository.findByWorkflowId(workflowId)
                .filter { it.aggregator.id == aggregator.id }
            val numberOfFarmers = collectionEvents.map { it.farmer.id }.distinct().count()

            // Generate hash of batch data for integrity verification
            val batchDataHash = generateBatchDataHash(saved)
            val hederaTransactionId = hederaMainService.recordConsolidatedBatch(
                batchId = saved.id,
                batchNumber = request.batchNumber ?: "BATCH-${saved.id.substring(0, 8)}",
                aggregatorId = aggregator.id,
                produceType = workflow.produceType,
                totalQuantityKg = request.quantitySentKg,
                numberOfFarmers = numberOfFarmers,
                batchDataHash = batchDataHash
            )
            saved.hederaTransactionId = hederaTransactionId
            savedConsolidatedBatch.hederaTransactionId = hederaTransactionId
            savedConsolidatedBatch.hederaBatchHash = batchDataHash
            consolidationEventRepository.save(saved)
            consolidatedBatchRepository.save(savedConsolidatedBatch)
            logger.info("Consolidation event ${saved.id} recorded on Hedera: $hederaTransactionId")
        } catch (e: Exception) {
            logger.error("Failed to record consolidation event on Hedera blockchain", e)
            // Continue without blockchain - non-critical failure
        }

        // Update workflow stage
        updateWorkflowQuantityAndStage(workflow)

        return toConsolidationEventResponseDto(saved)
    }

    fun getConsolidationEvents(workflowId: String): List<WorkflowConsolidationEventResponseDto> {
        return consolidationEventRepository.findByWorkflowId(workflowId)
            .map { toConsolidationEventResponseDto(it) }
    }

    // ===== PROCESSING EVENTS =====
    fun addProcessingEvent(workflowId: String, request: AddProcessingEventRequestDto): WorkflowProcessingEventResponseDto {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }

        val processor = processorRepository.findById(request.processorId)
            .orElseThrow { IllegalArgumentException("Processor not found") }

        val event = WorkflowProcessingEvent(
            id = UUID.randomUUID().toString(),
            workflow = workflow,
            processor = processor,
            quantityProcessedKg = request.quantityProcessedKg,
            processingDate = request.processingDate,
            processingType = request.processingType,
            outputQuantityKg = request.outputQuantityKg,
            processingNotes = request.processingNotes,
            createdAt = LocalDateTime.now()
        )

        val saved = processingEventRepository.save(event)

        // Update workflow stage
        updateWorkflowQuantityAndStage(workflow)

        return toProcessingEventResponseDto(saved)
    }

    fun getProcessingEvents(workflowId: String): List<WorkflowProcessingEventResponseDto> {
        return processingEventRepository.findByWorkflowId(workflowId)
            .map { toProcessingEventResponseDto(it) }
    }

    // ===== SHIPMENT EVENTS (Processor → Importer) =====
    fun addShipmentEvent(workflowId: String, request: AddShipmentEventRequestDto): WorkflowShipmentEventResponseDto {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }

        val processor = processorRepository.findById(request.processorId)
            .orElseThrow { IllegalArgumentException("Processor not found") }

        val importer = importerRepository.findById(request.importerId)
            .orElseThrow { IllegalArgumentException("Importer not found") }

        val event = WorkflowShipmentEvent(
            id = UUID.randomUUID().toString(),
            workflow = workflow,
            processor = processor,
            importer = importer,
            quantityShippedKg = request.quantityShippedKg,
            shipmentDate = request.shipmentDate,
            expectedArrivalDate = request.expectedArrivalDate,
            shippingCompany = request.shippingCompany,
            trackingNumber = request.trackingNumber,
            destinationPort = request.destinationPort,
            shipmentNotes = request.shipmentNotes,
            createdAt = LocalDateTime.now()
        )

        val saved = shipmentEventRepository.save(event)

        // Record shipment event on Hedera blockchain
        try {
            // Generate hash of shipment data for integrity verification
            val shipmentDataHash = generateShipmentDataHash(saved)
            
            // Extract origin country from processor facility address (fallback to "Unknown")
            val originCountry = processor.facilityAddress
                .split(",")
                .lastOrNull()
                ?.trim() ?: "Unknown"
            
            val hederaTransactionId = hederaMainService.recordImportShipment(
                shipmentId = saved.id,
                importerId = importer.id!!,
                shipmentNumber = request.trackingNumber ?: "SHIP-${saved.id.substring(0, 8)}",
                produceType = workflow.produceType,
                quantityKg = request.quantityShippedKg,
                originCountry = originCountry,
                shipmentDataHash = shipmentDataHash
            )
            saved.hederaTransactionId = hederaTransactionId
            shipmentEventRepository.save(saved)
            logger.info("Shipment event ${saved.id} recorded on Hedera: $hederaTransactionId")
        } catch (e: Exception) {
            logger.error("Failed to record shipment event on Hedera blockchain", e)
            // Continue without blockchain - non-critical failure
        }

        // Update workflow stage
        updateWorkflowQuantityAndStage(workflow)

        return toShipmentEventResponseDto(saved)
    }

    fun getShipmentEvents(workflowId: String): List<WorkflowShipmentEventResponseDto> {
        return shipmentEventRepository.findByWorkflowId(workflowId)
            .map { toShipmentEventResponseDto(it) }
    }

    // ===== HELPER: UPDATE WORKFLOW =====
    private fun updateWorkflowQuantityAndStage(workflow: SupplyChainWorkflow) {
        val totalCollected = collectionEventRepository.getTotalCollectedQuantity(workflow.id) ?: BigDecimal.ZERO
        val totalConsolidated = consolidationEventRepository.getTotalConsolidatedQuantity(workflow.id) ?: BigDecimal.ZERO
        val totalProcessed = processingEventRepository.getTotalProcessedQuantity(workflow.id) ?: BigDecimal.ZERO
        val totalShipped = shipmentEventRepository.getTotalShippedQuantity(workflow.id) ?: BigDecimal.ZERO

        workflow.totalQuantityKg = totalCollected

        // Update stage based on progress
        workflow.currentStage = when {
            totalShipped > BigDecimal.ZERO -> WorkflowStage.SHIPMENT
            totalProcessed > BigDecimal.ZERO -> WorkflowStage.PROCESSING
            totalConsolidated > BigDecimal.ZERO -> WorkflowStage.CONSOLIDATION
            totalCollected > BigDecimal.ZERO -> WorkflowStage.COLLECTION
            else -> WorkflowStage.COLLECTION
        }

        // Mark as completed if all quantity has been shipped
        if (totalShipped >= totalCollected && totalCollected > BigDecimal.ZERO) {
            workflow.status = WorkflowStatus.COMPLETED
            workflow.currentStage = WorkflowStage.COMPLETED
            if (workflow.completedAt == null) {
                workflow.completedAt = LocalDateTime.now()
            }
        }

        workflow.updatedAt = LocalDateTime.now()
        workflowRepository.save(workflow)
    }

    // ===== HELPER: GET AVAILABLE QUANTITIES =====
    fun getAvailableQuantityForAggregator(workflowId: String, aggregatorId: String): BigDecimal {
        // Get total collected by this aggregator
        val collectedEvents = collectionEventRepository.findByWorkflowAndAggregator(workflowId, aggregatorId)
        val totalCollected = collectedEvents.sumOf { it.quantityCollectedKg }

        // Get total already sent by this aggregator
        val totalSent = consolidationEventRepository.getTotalSentByAggregator(workflowId, aggregatorId) ?: BigDecimal.ZERO

        return totalCollected.subtract(totalSent).max(BigDecimal.ZERO)
    }

    fun getAvailableQuantitiesPerAggregator(workflowId: String): List<AvailableQuantityDto> {
        val collectionEvents = collectionEventRepository.findByWorkflowId(workflowId)
        val aggregatorMap = mutableMapOf<String, MutableList<WorkflowCollectionEvent>>()

        collectionEvents.forEach { event ->
            aggregatorMap.computeIfAbsent(event.aggregator.id) { mutableListOf() }.add(event)
        }

        return aggregatorMap.map { (aggregatorId, events) ->
            val totalCollected = events.sumOf { it.quantityCollectedKg }
            val totalSent = consolidationEventRepository.getTotalSentByAggregator(workflowId, aggregatorId) ?: BigDecimal.ZERO
            val available = totalCollected.subtract(totalSent).max(BigDecimal.ZERO)

            AvailableQuantityDto(
                aggregatorId = aggregatorId,
                aggregatorName = events.first().aggregator.organizationName,
                totalCollected = totalCollected,
                totalSent = totalSent,
                available = available
            )
        }
    }

    // ===== MAPPERS =====
    private fun toWorkflowResponseDto(workflow: SupplyChainWorkflow): WorkflowResponseDto {
        val totalCollected = collectionEventRepository.getTotalCollectedQuantity(workflow.id) ?: BigDecimal.ZERO
        val totalConsolidated = consolidationEventRepository.getTotalConsolidatedQuantity(workflow.id) ?: BigDecimal.ZERO
        val totalProcessed = processingEventRepository.getTotalProcessedQuantity(workflow.id) ?: BigDecimal.ZERO
        val totalShipped = shipmentEventRepository.getTotalShippedQuantity(workflow.id) ?: BigDecimal.ZERO

        val collectionCount = collectionEventRepository.findByWorkflowId(workflow.id).size
        val consolidationCount = consolidationEventRepository.findByWorkflowId(workflow.id).size
        val processingCount = processingEventRepository.findByWorkflowId(workflow.id).size
        val shipmentCount = shipmentEventRepository.findByWorkflowId(workflow.id).size

        return WorkflowResponseDto(
            id = workflow.id,
            exporterId = workflow.exporter.id,
            workflowName = workflow.workflowName,
            produceType = workflow.produceType,
            status = workflow.status.name,
            currentStage = workflow.currentStage.name,
            totalQuantityKg = workflow.totalQuantityKg,
            createdAt = workflow.createdAt,
            updatedAt = workflow.updatedAt,
            completedAt = workflow.completedAt,
            totalCollected = totalCollected,
            totalConsolidated = totalConsolidated,
            totalProcessed = totalProcessed,
            totalShipped = totalShipped,
            availableForConsolidation = totalCollected.subtract(totalConsolidated).max(BigDecimal.ZERO),
            availableForProcessing = totalConsolidated.subtract(totalProcessed).max(BigDecimal.ZERO),
            availableForShipment = totalProcessed.subtract(totalShipped).max(BigDecimal.ZERO),
            collectionEventCount = collectionCount,
            consolidationEventCount = consolidationCount,
            processingEventCount = processingCount,
            shipmentEventCount = shipmentCount
        )
    }

    private fun toCollectionEventResponseDto(event: WorkflowCollectionEvent): WorkflowCollectionEventResponseDto {
        return WorkflowCollectionEventResponseDto(
            id = event.id,
            workflowId = event.workflow.id,
            productionUnitId = event.productionUnit.id,
            productionUnitName = event.productionUnit.unitName,
            aggregatorId = event.aggregator.id,
            aggregatorName = event.aggregator.organizationName,
            farmerId = event.farmer.id ?: "",
            farmerName = event.farmer.userProfile.fullName,
            quantityCollectedKg = event.quantityCollectedKg,
            collectionDate = event.collectionDate,
            qualityGrade = event.qualityGrade,
            notes = event.notes,
            hederaHash = event.hederaHash,
            hederaTransactionId = event.hederaTransactionId,
            createdAt = event.createdAt
        )
    }

    private fun toConsolidationEventResponseDto(event: WorkflowConsolidationEvent): WorkflowConsolidationEventResponseDto {
        return WorkflowConsolidationEventResponseDto(
            id = event.id,
            workflowId = event.workflow.id,
            aggregatorId = event.aggregator.id,
            aggregatorName = event.aggregator.organizationName,
            processorId = event.processor.id,
            processorName = event.processor.facilityName,
            quantitySentKg = event.quantitySentKg,
            consolidationDate = event.consolidationDate,
            transportDetails = event.transportDetails,
            batchNumber = event.batchNumber,
            notes = event.notes,
            hederaHash = event.hederaHash,
            hederaTransactionId = event.hederaTransactionId,
            createdAt = event.createdAt
        )
    }

    private fun toProcessingEventResponseDto(event: WorkflowProcessingEvent): WorkflowProcessingEventResponseDto {
        return WorkflowProcessingEventResponseDto(
            id = event.id,
            workflowId = event.workflow.id,
            processorId = event.processor.id,
            processorName = event.processor.facilityName,
            quantityProcessedKg = event.quantityProcessedKg,
            processingDate = event.processingDate,
            processingType = event.processingType,
            outputQuantityKg = event.outputQuantityKg,
            processingNotes = event.processingNotes,
            hederaHash = event.hederaHash,
            hederaTransactionId = event.hederaTransactionId,
            createdAt = event.createdAt
        )
    }

    private fun toShipmentEventResponseDto(event: WorkflowShipmentEvent): WorkflowShipmentEventResponseDto {
        return WorkflowShipmentEventResponseDto(
            id = event.id,
            workflowId = event.workflow.id,
            processorId = event.processor.id,
            processorName = event.processor.facilityName,
            importerId = event.importer.id,
            importerName = event.importer.companyName,
            quantityShippedKg = event.quantityShippedKg,
            shipmentDate = event.shipmentDate,
            expectedArrivalDate = event.expectedArrivalDate,
            actualArrivalDate = event.actualArrivalDate,
            shippingCompany = event.shippingCompany,
            trackingNumber = event.trackingNumber,
            destinationPort = event.destinationPort,
            shipmentNotes = event.shipmentNotes,
            hederaHash = event.hederaHash,
            hederaTransactionId = event.hederaTransactionId,
            createdAt = event.createdAt
        )
    }

    // Helper method to generate hash for batch data integrity
    private fun generateBatchDataHash(event: WorkflowConsolidationEvent): String {
        val data = "${event.id}:${event.aggregator.id}:${event.processor.id}:" +
                "${event.quantitySentKg}:${event.consolidationDate}:${event.batchNumber}"
        return MessageDigest.getInstance("SHA-256")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    // Helper method to generate hash for shipment data integrity
    private fun generateShipmentDataHash(event: WorkflowShipmentEvent): String {
        val data = "${event.id}:${event.processor.id}:${event.importer.id}:" +
                "${event.quantityShippedKg}:${event.shipmentDate}:${event.trackingNumber}"
        return MessageDigest.getInstance("SHA-256")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
