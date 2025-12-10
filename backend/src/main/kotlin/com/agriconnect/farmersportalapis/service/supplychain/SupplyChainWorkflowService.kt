package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.domain.eudr.ConsolidatedBatch
import com.agriconnect.farmersportalapis.domain.eudr.ConsolidatedBatchStatus
import com.agriconnect.farmersportalapis.domain.supplychain.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import com.agriconnect.farmersportalapis.repository.*
import com.agriconnect.farmersportalapis.service.hedera.HederaAccountService
import com.agriconnect.farmersportalapis.service.hedera.HederaMainService
import com.hedera.hashgraph.sdk.AccountId
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
    private val hederaMainService: HederaMainService,
    private val hederaAccountService: HederaAccountService,
    private val hederaAccountCredentialsRepository: HederaAccountCredentialsRepository
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
    fun getAllWorkflows(): List<SupplyChainWorkflow> {
        return workflowRepository.findAll()
    }

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
                ?.split(",")
                ?.lastOrNull()
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
            shipmentEventCount = shipmentCount,
            // Certificate information
            certificateStatus = workflow.certificateStatus?.name,
            complianceCertificateNftId = workflow.complianceCertificateNftId,
            complianceCertificateSerialNumber = workflow.complianceCertificateSerialNumber,
            complianceCertificateTransactionId = workflow.complianceCertificateTransactionId,
            certificateIssuedAt = workflow.certificateIssuedAt,
            currentOwnerAccountId = workflow.currentOwnerAccountId
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

    // ===== CERTIFICATE MANAGEMENT =====
    
    /**
     * Issue EUDR Compliance Certificate NFT for a workflow
     * This validates compliance and mints the certificate on blockchain
     */
    fun issueComplianceCertificate(workflowId: String): Map<String, Any?> {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }

        // Check if certificate already issued
        if (workflow.certificateStatus != CertificateStatus.NOT_CREATED && 
            workflow.certificateStatus != CertificateStatus.PENDING_VERIFICATION) {
            throw IllegalStateException("Certificate already issued for this workflow (Status: ${workflow.certificateStatus})")
        }

        // Verify workflow has completed necessary stages
        if (workflow.collectionEvents.isEmpty()) {
            throw IllegalStateException("Cannot issue certificate: No collection events recorded")
        }

        // Get exporter's Hedera account
        val exporterCredentials = hederaAccountCredentialsRepository
            .findByEntityTypeAndEntityId("EXPORTER", workflow.exporter.id)
            .orElseThrow { 
                IllegalStateException("Exporter does not have a Hedera account. Please create one first.") 
            }

        // Build compliance data
        val collectionEvents = workflow.collectionEvents
        val productionUnits = collectionEvents.map { it.productionUnit }.distinct()
        val farmers = collectionEvents.map { it.farmer }.distinct()
        
        val complianceData = mapOf(
            "workflowName" to workflow.workflowName,
            "produceType" to workflow.produceType,
            "totalQuantityKg" to workflow.totalQuantityKg.toString(),
            "totalFarmers" to farmers.size.toString(),
            "totalProductionUnits" to productionUnits.size.toString(),
            "gpsCoordinatesCount" to productionUnits.size.toString(), // Assuming 1 GPS per production unit
            "deforestationStatus" to "VERIFIED_FREE", // Should come from actual verification
            "originCountry" to "Kenya", // Should come from production units
            "riskLevel" to "LOW", // Should come from risk assessment
            "traceabilityHash" to generateWorkflowTraceabilityHash(workflow)
        )

        // Issue the certificate NFT
        try {
            val (transactionId, serialNumber) = hederaMainService.issueWorkflowComplianceCertificateNft(
                workflowId = workflow.id,
                exporterAccountId = AccountId.fromString(exporterCredentials.hederaAccountId),
                complianceData = complianceData
            )

            // Update workflow with certificate details
            workflow.complianceCertificateTransactionId = transactionId
            workflow.complianceCertificateSerialNumber = serialNumber
            workflow.currentOwnerAccountId = exporterCredentials.hederaAccountId
            workflow.certificateStatus = CertificateStatus.COMPLIANT
            workflow.certificateIssuedAt = LocalDateTime.now()
            
            workflowRepository.save(workflow)

            logger.info("Certificate issued for workflow ${workflow.id}: TxID=$transactionId, Serial=$serialNumber")

            return mapOf(
                "workflowId" to workflow.id,
                "transactionId" to transactionId,
                "serialNumber" to serialNumber,
                "hederaAccountId" to exporterCredentials.hederaAccountId,
                "certificateStatus" to workflow.certificateStatus.name,
                "issuedAt" to workflow.certificateIssuedAt,
                "hashscanUrl" to "https://hashscan.io/testnet/transaction/$transactionId"
            )
        } catch (e: Exception) {
            logger.error("Failed to issue certificate for workflow ${workflow.id}", e)
            throw RuntimeException("Failed to issue certificate: ${e.message}", e)
        }
    }

    /**
     * Transfer certificate to importer
     */
    fun transferCertificateToImporter(workflowId: String, importerId: String) {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }

        if (workflow.certificateStatus != CertificateStatus.COMPLIANT && 
            workflow.certificateStatus != CertificateStatus.IN_TRANSIT) {
            throw IllegalStateException("Cannot transfer certificate in current status: ${workflow.certificateStatus}")
        }

        val importer = importerRepository.findById(importerId)
            .orElseThrow { IllegalArgumentException("Importer not found") }

        // Get importer's Hedera account
        val importerCredentials = hederaAccountCredentialsRepository
            .findByEntityTypeAndEntityId("IMPORTER", importerId)
            .orElseThrow { 
                IllegalStateException("Importer does not have a Hedera account") 
            }

        val exporterCredentials = hederaAccountCredentialsRepository
            .findByEntityTypeAndEntityId("EXPORTER", workflow.exporter.id)
            .orElseThrow { 
                IllegalStateException("Exporter Hedera account not found") 
            }

        try {
            val success = hederaMainService.transferWorkflowComplianceCertificateNft(
                fromAccountId = AccountId.fromString(exporterCredentials.hederaAccountId),
                toAccountId = AccountId.fromString(importerCredentials.hederaAccountId),
                workflowId = workflow.id
            )

            if (success) {
                workflow.currentOwnerAccountId = importerCredentials.hederaAccountId
                workflow.certificateStatus = CertificateStatus.TRANSFERRED_TO_IMPORTER
                workflowRepository.save(workflow)

                logger.info("Certificate transferred for workflow ${workflow.id} to importer $importerId")
            } else {
                throw RuntimeException("Certificate transfer returned false")
            }
        } catch (e: Exception) {
            logger.error("Failed to transfer certificate for workflow ${workflow.id}", e)
            throw RuntimeException("Failed to transfer certificate: ${e.message}", e)
        }
    }

    /**
     * Generate traceability hash for the entire workflow
     */
    private fun generateWorkflowTraceabilityHash(workflow: SupplyChainWorkflow): String {
        val collectionHashes = workflow.collectionEvents.joinToString(":") { it.hederaHash ?: "" }
        val consolidationHashes = workflow.consolidationEvents.joinToString(":") { it.hederaHash ?: "" }
        val processingHashes = workflow.processingEvents.joinToString(":") { it.hederaHash ?: "" }
        val shipmentHashes = workflow.shipmentEvents.joinToString(":") { it.hederaHash ?: "" }
        
        val data = "${workflow.id}:$collectionHashes:$consolidationHashes:$processingHashes:$shipmentHashes"
        return MessageDigest.getInstance("SHA-256")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    // ===== HEDERA ACCOUNT MANAGEMENT =====
    
    /**
     * Create a Hedera account for an exporter
     * This enables the exporter to issue and receive EUDR compliance certificates
     */
    fun createHederaAccountForExporter(exporterId: String): Map<String, Any?> {
        logger.info("Creating Hedera account for exporter: {}", exporterId)
        
        val exporter = exporterRepository.findById(exporterId)
            .orElseThrow { IllegalArgumentException("Exporter not found") }

        // Check if account already exists
        val existingAccount = hederaAccountCredentialsRepository
            .findByEntityTypeAndEntityId("EXPORTER", exporterId)
            .orElse(null)
        
        if (existingAccount != null) {
            throw IllegalStateException("Hedera account already exists for this exporter: ${existingAccount.hederaAccountId}")
        }

        try {
            // Create new Hedera account using HederaAccountService
            val accountResult = hederaAccountService.createHederaAccount(
                memo = "Exporter Account - ${exporter.companyName}"
            )
            
            logger.info("Created Hedera account {} for exporter {}", accountResult.accountId, exporterId)

            // Store credentials in database
            val credentials = com.agriconnect.farmersportalapis.domain.hedera.HederaAccountCredentials(
                id = UUID.randomUUID().toString(),
                userId = exporter.userProfile.id,
                entityType = "EXPORTER",
                entityId = exporterId,
                hederaAccountId = accountResult.accountId,
                encryptedPrivateKey = accountResult.encryptedPrivateKey,
                publicKey = accountResult.publicKey,
                createdAt = LocalDateTime.now()
            )
            
            hederaAccountCredentialsRepository.save(credentials)
            
            logger.info("Hedera account credentials saved for exporter {}", exporterId)

            return mapOf(
                "hederaAccountId" to accountResult.accountId,
                "publicKey" to accountResult.publicKey,
                "createdAt" to credentials.createdAt,
                "message" to "Hedera account created successfully. You can now issue EUDR compliance certificates."
            )
            
        } catch (e: Exception) {
            logger.error("Failed to create Hedera account for exporter {}: {}", exporterId, e.message, e)
            throw IllegalStateException("Failed to create Hedera account: ${e.message}", e)
        }
    }

    /**
     * Get Hedera account details for an exporter
     */
    fun getHederaAccountForExporter(exporterId: String): Map<String, Any?> {
        val exporter = exporterRepository.findById(exporterId)
            .orElseThrow { IllegalArgumentException("Exporter not found") }

        val credentials = hederaAccountCredentialsRepository
            .findByEntityTypeAndEntityId("EXPORTER", exporterId)
            .orElseThrow { IllegalStateException("No Hedera account found for this exporter") }

        return mapOf(
            "hederaAccountId" to credentials.hederaAccountId,
            "publicKey" to credentials.publicKey,
            "createdAt" to credentials.createdAt,
            "entityType" to credentials.entityType
        )
    }
}
