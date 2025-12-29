package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.domain.eudr.DeforestationAlert
import com.agriconnect.farmersportalapis.domain.supplychain.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import com.agriconnect.farmersportalapis.repository.*
import com.agriconnect.farmersportalapis.service.common.ImporterService
import com.agriconnect.farmersportalapis.service.hedera.AsyncHederaService
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
    private val workflowProductionUnitRepository: WorkflowProductionUnitRepository,
    private val exporterRepository: ExporterRepository,
    private val productionUnitRepository: ProductionUnitRepository,
    private val supplierRepository: SupplyChainSupplierRepository,  // Flexible supply chain supplier
    private val eudrBatchService: EudrBatchService,
    private val consolidatedBatchRepository: ConsolidatedBatchRepository,
    private val importerRepository: ImporterRepository,
    private val importerService: ImporterService,  // For Hedera account management
    private val farmerRepository: FarmerRepository,
    private val hederaMainService: HederaMainService,
    private val hederaAccountService: HederaAccountService,
    private val hederaAccountCredentialsRepository: HederaAccountCredentialsRepository,
    private val deforestationAlertRepository: DeforestationAlertRepository,
    private val riskAssessmentService: com.agriconnect.farmersportalapis.service.common.RiskAssessmentService,
    private val asyncHederaService: AsyncHederaService  // Async Hedera recording for non-blocking operations
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

    // ===== COLLECTION EVENTS (Production Unit → Collector Supplier) =====
    fun addCollectionEvent(workflowId: String, request: AddCollectionEventRequestDto): WorkflowCollectionEventResponseDto {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }

        val productionUnit = productionUnitRepository.findById(request.productionUnitId)
            .orElseThrow { IllegalArgumentException("Production unit not found") }

        // Use flexible supply chain supplier instead of rigid aggregator
        val collectorSupplier = supplierRepository.findById(request.aggregatorId)
            .orElseThrow { IllegalArgumentException("Collector supplier not found: ${request.aggregatorId}") }

        val farmer = farmerRepository.findById(request.farmerId)
            .orElseThrow { IllegalArgumentException("Farmer not found") }

        val event = WorkflowCollectionEvent(
            id = UUID.randomUUID().toString(),
            workflow = workflow,
            productionUnit = productionUnit,
            collectorSupplier = collectorSupplier,
            farmer = farmer,
            quantityCollectedKg = request.quantityCollectedKg,
            collectionDate = request.collectionDate,
            qualityGrade = request.qualityGrade,
            notes = request.notes,
            createdAt = LocalDateTime.now()
        )

        val saved = collectionEventRepository.save(event)

        // Record collection event on Hedera blockchain ASYNCHRONOUSLY
        // This ensures the user doesn't have to wait for blockchain confirmation
        asyncHederaService.recordCollectionEventAsync(saved)
        logger.info("Collection event ${saved.id} queued for async Hedera recording")

        // Update workflow total quantity and stage
        updateWorkflowQuantityAndStage(workflow)

        return toCollectionEventResponseDto(saved)
    }

    fun getCollectionEvents(workflowId: String): List<WorkflowCollectionEventResponseDto> {
        return collectionEventRepository.findByWorkflowId(workflowId)
            .map { toCollectionEventResponseDto(it) }
    }

    // ===== CONSOLIDATION EVENTS (Source Supplier → Target Supplier) =====
    fun addConsolidationEvent(workflowId: String, request: AddConsolidationEventRequestDto): WorkflowConsolidationEventResponseDto {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }

        val sourceSupplier = supplierRepository.findById(request.aggregatorId)
            .orElseThrow { IllegalArgumentException("Source supplier not found: ${request.aggregatorId}") }

        val targetSupplier = supplierRepository.findById(request.processorId)
            .orElseThrow { IllegalArgumentException("Target supplier not found: ${request.processorId}") }

        // Validate: Check if source supplier has enough quantity available
        val availableQuantity = getAvailableQuantityForSupplier(workflowId, request.aggregatorId)
        if (request.quantitySentKg > availableQuantity) {
            throw IllegalArgumentException("Insufficient quantity. Available: $availableQuantity kg, Requested: ${request.quantitySentKg} kg")
        }

        val event = WorkflowConsolidationEvent(
            id = UUID.randomUUID().toString(),
            workflow = workflow,
            sourceSupplier = sourceSupplier,
            targetSupplier = targetSupplier,
            quantitySentKg = request.quantitySentKg,
            consolidationDate = request.consolidationDate,
            transportDetails = request.transportDetails,
            batchNumber = request.batchNumber,
            notes = request.notes,
            createdAt = LocalDateTime.now()
        )

        val saved = consolidationEventRepository.save(event)

        // ===== CREATE CONSOLIDATED BATCH =====
        // Get number of unique farmers from collection events for this supplier
        val collectionEvents = collectionEventRepository.findByWorkflowId(workflowId)
            .filter { it.collectorSupplier.id == sourceSupplier.id }
        val numberOfFarmers = collectionEvents.map { it.farmer.id }.distinct().count()

        // Calculate average quality grade from collection events
        val qualityGrades = collectionEvents.mapNotNull { it.qualityGrade }
        val averageQualityGrade = if (qualityGrades.isNotEmpty()) {
            qualityGrades.groupBy { it }.maxByOrNull { it.value.size }?.key
        } else null

        // Note: ConsolidatedBatch still uses legacy Aggregator - keeping for backward compatibility
        // In future, this should also be migrated to use SupplyChainSupplier
        
        // Record consolidation event on Hedera blockchain ASYNCHRONOUSLY
        asyncHederaService.recordConsolidationEventAsync(saved)
        logger.info("Consolidation event ${saved.id} queued for async Hedera recording")

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

        val processorSupplier = supplierRepository.findById(request.processorId)
            .orElseThrow { IllegalArgumentException("Processor supplier not found: ${request.processorId}") }

        val event = WorkflowProcessingEvent(
            id = UUID.randomUUID().toString(),
            workflow = workflow,
            processorSupplier = processorSupplier,
            quantityProcessedKg = request.getEffectiveQuantityProcessedKg(),
            processingDate = request.processingDate,
            processingType = request.getEffectiveProcessingType(),
            outputQuantityKg = request.outputQuantityKg,
            processingNotes = request.getEffectiveNotes(),
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

    // ===== SHIPMENT EVENTS (Supplier → Importer) =====
    fun addShipmentEvent(workflowId: String, request: AddShipmentEventRequestDto): WorkflowShipmentEventResponseDto {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }

        // Shipper is optional - could be any supplier in the chain
        val shipperSupplier = request.processorId?.let { 
            supplierRepository.findById(it).orElse(null) 
        }

        val importer = importerRepository.findById(request.importerId)
            .orElseThrow { IllegalArgumentException("Importer not found") }

        val event = WorkflowShipmentEvent(
            id = UUID.randomUUID().toString(),
            workflow = workflow,
            shipperSupplier = shipperSupplier,
            importer = importer,
            quantityShippedKg = request.quantityShippedKg,
            shipmentDate = request.shipmentDate ?: LocalDateTime.now(),
            expectedArrivalDate = request.expectedArrivalDate,
            shippingCompany = request.shippingCompany,
            trackingNumber = request.trackingNumber ?: request.billOfLading,
            destinationPort = request.destinationPort,
            shipmentNotes = request.shipmentNotes,
            createdAt = LocalDateTime.now()
        )

        val saved = shipmentEventRepository.save(event)

        // Record shipment event on Hedera blockchain ASYNCHRONOUSLY
        // This ensures the user doesn't have to wait for blockchain confirmation
        asyncHederaService.recordShipmentEventAsync(saved)
        logger.info("Shipment event ${saved.id} queued for async Hedera recording")

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
    fun getAvailableQuantityForSupplier(workflowId: String, supplierId: String): BigDecimal {
        // Get total collected by this supplier
        val collectedEvents = collectionEventRepository.findByWorkflowAndSupplier(workflowId, supplierId)
        val totalCollected = collectedEvents.sumOf { it.quantityCollectedKg }

        // Get total already sent by this supplier
        val totalSent = consolidationEventRepository.getTotalSentBySupplier(workflowId, supplierId) ?: BigDecimal.ZERO

        return totalCollected.subtract(totalSent).max(BigDecimal.ZERO)
    }

    fun getAvailableQuantitiesPerAggregator(workflowId: String): List<AvailableQuantityDto> {
        val collectionEvents = collectionEventRepository.findByWorkflowId(workflowId)
        val supplierMap = mutableMapOf<String, MutableList<WorkflowCollectionEvent>>()

        collectionEvents.forEach { event ->
            supplierMap.computeIfAbsent(event.collectorSupplier.id) { mutableListOf() }.add(event)
        }

        return supplierMap.map { (supplierId, events) ->
            val totalCollected = events.sumOf { it.quantityCollectedKg }
            val totalSent = consolidationEventRepository.getTotalSentBySupplier(workflowId, supplierId) ?: BigDecimal.ZERO
            val available = totalCollected.subtract(totalSent).max(BigDecimal.ZERO)

            AvailableQuantityDto(
                aggregatorId = supplierId,  // Keep field name for backward compatibility with frontend
                aggregatorName = events.first().collectorSupplier.supplierName,
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
        val linkedUnitsCount = workflowProductionUnitRepository.countActiveByWorkflowId(workflow.id)

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
            linkedProductionUnits = linkedUnitsCount.toInt(),
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
            aggregatorId = event.collectorSupplier.id,  // Field name kept for API compatibility
            aggregatorName = event.collectorSupplier.supplierName,
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
            aggregatorId = event.sourceSupplier.id,  // Field name kept for API compatibility
            aggregatorName = event.sourceSupplier.supplierName,
            processorId = event.targetSupplier.id,
            processorName = event.targetSupplier.supplierName,
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
            processorId = event.processorSupplier.id,
            processorName = event.processorSupplier.supplierName,
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
            processorId = event.shipperSupplier?.id ?: "",
            processorName = event.shipperSupplier?.supplierName ?: "Direct Export",
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
        val data = "${event.id}:${event.sourceSupplier.id}:${event.targetSupplier.id}:" +
                "${event.quantitySentKg}:${event.consolidationDate}:${event.batchNumber}"
        return MessageDigest.getInstance("SHA-256")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    // Helper method to generate hash for shipment data integrity
    private fun generateShipmentDataHash(event: WorkflowShipmentEvent): String {
        val data = "${event.id}:${event.shipperSupplier?.id ?: "direct"}:${event.importer.id}:" +
                "${event.quantityShippedKg}:${event.shipmentDate}:${event.trackingNumber}"
        return MessageDigest.getInstance("SHA-256")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    // ===== CERTIFICATE MANAGEMENT =====
    
    /**
     * Validation result for workflow compliance
     */
    private data class ComplianceValidationResult(
        val isCompliant: Boolean,
        val failureReasons: List<String> = emptyList(),
        val totalFarmers: Int = 0,
        val totalProductionUnits: Int = 0,
        val gpsCoordinatesCount: Int = 0,
        val deforestationStatus: String = "UNKNOWN",
        val originCountry: String = "UNKNOWN",
        val riskLevel: String = "HIGH",
        val traceabilityHash: String = ""
    )

    /**
     * Validates if workflow meets all EUDR compliance requirements
     */
    private fun validateWorkflowCompliance(workflow: SupplyChainWorkflow): ComplianceValidationResult {
        val failureReasons = mutableListOf<String>()
        
        // 1. Verify collection events exist
        if (workflow.collectionEvents.isEmpty()) {
            failureReasons.add("No collection events recorded")
            return ComplianceValidationResult(isCompliant = false, failureReasons = failureReasons)
        }

        val collectionEvents = workflow.collectionEvents
        val productionUnits = collectionEvents.map { it.productionUnit }.distinct()
        val farmers = collectionEvents.map { it.farmer }.distinct()

        // 2. Verify all production units have GPS coordinates
        val unitsWithoutGps = productionUnits.filter { it.wgs84Coordinates == null && it.parcelGeometry == null }
        if (unitsWithoutGps.isNotEmpty()) {
            failureReasons.add("${unitsWithoutGps.size} production unit(s) missing GPS coordinates")
        }

        // 3. Verify all production units are verified (satellite imagery check)
        val unverifiedUnits = productionUnits.filter { it.lastVerifiedAt == null }
        if (unverifiedUnits.isNotEmpty()) {
            failureReasons.add("${unverifiedUnits.size} production unit(s) not verified with satellite imagery")
        }

        // 4. Verify deforestation-free status for all production units using existing service
        val eudrCutoffDate = LocalDateTime.of(2020, 12, 31, 0, 0)
        val productionUnitIds = productionUnits.map { it.id }
        
        val recentAlerts = if (productionUnitIds.isNotEmpty()) {
            deforestationAlertRepository.findByProductionUnitIdsAndDateRange(
                productionUnitIds,
                eudrCutoffDate,
                LocalDateTime.now()
            )
        } else {
            emptyList()
        }
        
        val criticalAlerts = recentAlerts.filter { 
            it.severity in listOf(
                DeforestationAlert.Severity.HIGH,
                DeforestationAlert.Severity.CRITICAL
            )
        }
        
        if (criticalAlerts.isNotEmpty()) {
            failureReasons.add("${criticalAlerts.size} HIGH/CRITICAL deforestation alert(s) detected after 2020-12-31")
        }

        // 5. Verify supply chain traceability - FLEXIBLE approach
        // Consolidation is OPTIONAL - some supply chains go directly from collection to processing or export
        // The key requirement is having collection events with proper production unit links
        val hasCollections = collectionEvents.isNotEmpty()
        val hasConsolidations = workflow.consolidationEvents.isNotEmpty()
        val hasProcessing = workflow.processingEvents.isNotEmpty()
        
        if (!hasCollections) {
            failureReasons.add("No collection events - traceability starts at collection point")
        }
        
        // If there are consolidations, validate quantity consistency
        if (hasConsolidations) {
            // 6. Verify quantity consistency when consolidation exists
            val totalCollectedKg = collectionEvents.sumOf { it.quantityCollectedKg }
            val totalConsolidatedKg = workflow.consolidationEvents.sumOf { it.quantitySentKg }
            
            if (totalConsolidatedKg > totalCollectedKg) {
                failureReasons.add("Consolidation quantity ($totalConsolidatedKg kg) exceeds collection quantity ($totalCollectedKg kg)")
            }
        }

        // 7. Determine origin country from production units
        val countries = productionUnits.mapNotNull { it.administrativeRegion }.distinct()
        val originCountry = countries.firstOrNull() ?: "UNKNOWN"
        
        if (originCountry == "UNKNOWN") {
            failureReasons.add("Unable to determine origin country from production units")
        }

        // 8. Use existing RiskAssessmentService for comprehensive risk calculation
        // Traceability is complete if we have collections (consolidation and processing are optional)
        val traceabilityComplete = hasCollections && productionUnits.isNotEmpty()
        val riskLevel = try {
            // Create a temporary batch-like assessment based on workflow data
            calculateWorkflowRiskLevel(
                productionUnits = productionUnits,
                deforestationAlerts = recentAlerts,
                originCountry = originCountry,
                hasGpsGaps = unitsWithoutGps.isNotEmpty(),
                hasVerificationGaps = unverifiedUnits.isNotEmpty(),
                traceabilityComplete = traceabilityComplete
            )
        } catch (e: Exception) {
            logger.warn("Failed to calculate risk level using RiskAssessmentService: ${e.message}")
            "HIGH" // Default to HIGH if assessment fails
        }

        // 9. Block certificate issuance for HIGH risk unless explicitly approved
        if (riskLevel == "HIGH") {
            failureReasons.add("Risk assessment determined HIGH risk - requires manual compliance review before certificate issuance")
        }

        // 10. Determine deforestation status
        val deforestationStatus = when {
            criticalAlerts.isNotEmpty() -> "DEFORESTATION_DETECTED"
            unitsWithoutGps.isNotEmpty() || unverifiedUnits.isNotEmpty() -> "VERIFICATION_INCOMPLETE"
            recentAlerts.isNotEmpty() -> "ALERTS_UNDER_REVIEW"
            else -> "VERIFIED_FREE"
        }

        // 11. Generate traceability hash
        val traceabilityHash = generateWorkflowTraceabilityHash(workflow)

        val isCompliant = failureReasons.isEmpty()

        logger.info("""
            Compliance Validation for Workflow ${workflow.id}:
            - Compliant: $isCompliant
            - Farmers: ${farmers.size}
            - Production Units: ${productionUnits.size}
            - GPS Coverage: ${productionUnits.size - unitsWithoutGps.size}/${productionUnits.size}
            - Verified Units: ${productionUnits.size - unverifiedUnits.size}/${productionUnits.size}
            - Deforestation Alerts: ${recentAlerts.size} (${criticalAlerts.size} critical)
            - Risk Level: $riskLevel
            - Deforestation Status: $deforestationStatus
            ${if (!isCompliant) "- Failure Reasons: ${failureReasons.joinToString("; ")}" else ""}
        """.trimIndent())

        return ComplianceValidationResult(
            isCompliant = isCompliant,
            failureReasons = failureReasons,
            totalFarmers = farmers.size,
            totalProductionUnits = productionUnits.size,
            gpsCoordinatesCount = productionUnits.count { it.wgs84Coordinates != null || it.parcelGeometry != null },
            deforestationStatus = deforestationStatus,
            originCountry = originCountry,
            riskLevel = riskLevel,
            traceabilityHash = traceabilityHash
        )
    }

    /**
     * Calculate comprehensive risk level for workflow using multiple factors
     * Integrates with existing RiskAssessmentService methodology
     */
    private fun calculateWorkflowRiskLevel(
        productionUnits: List<com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit>,
        deforestationAlerts: List<com.agriconnect.farmersportalapis.domain.eudr.DeforestationAlert>,
        originCountry: String,
        hasGpsGaps: Boolean,
        hasVerificationGaps: Boolean,
        traceabilityComplete: Boolean
    ): String {
        // High-risk countries (based on EUDR Annex and risk matrix)
        val highRiskCountries = setOf(
            "BRAZIL", "INDONESIA", "DEMOCRATIC_REPUBLIC_OF_CONGO", 
            "MALAYSIA", "BOLIVIA", "PERU", "COLOMBIA", "PARAGUAY"
        )
        
        var riskScore = 0.0
        
        // 1. Deforestation Alert Risk (40% weight)
        val criticalAlertCount = deforestationAlerts.count { 
            it.severity in listOf(
                DeforestationAlert.Severity.HIGH,
                DeforestationAlert.Severity.CRITICAL
            )
        }
        val mediumAlertCount = deforestationAlerts.count { 
            it.severity == com.agriconnect.farmersportalapis.domain.eudr.DeforestationAlert.Severity.MEDIUM 
        }
        
        val deforestationScore = when {
            criticalAlertCount > 0 -> 0.9
            mediumAlertCount > 2 -> 0.7
            mediumAlertCount > 0 -> 0.5
            deforestationAlerts.isNotEmpty() -> 0.3
            else -> 0.1
        }
        riskScore += deforestationScore * 0.4
        
        // 2. Geospatial Verification Risk (25% weight)
        val verifiedUnits = productionUnits.count { it.lastVerifiedAt != null }
        val unitsWithGps = productionUnits.count { it.wgs84Coordinates != null || it.parcelGeometry != null }
        val geoVerificationRatio = if (productionUnits.isNotEmpty()) {
            (verifiedUnits + unitsWithGps).toDouble() / (productionUnits.size * 2)
        } else {
            0.0
        }
        val geospatialScore = 1.0 - geoVerificationRatio // Higher score for less verification
        riskScore += geospatialScore * 0.25
        
        // 3. Country Risk (20% weight)
        val countryScore = if (originCountry.uppercase() in highRiskCountries) 0.8 else 0.3
        riskScore += countryScore * 0.2
        
        // 4. Traceability Completeness (15% weight)
        val traceabilityScore = if (traceabilityComplete) 0.1 else 0.8
        riskScore += traceabilityScore * 0.15
        
        // 5. GPS Coverage (bonus/penalty)
        if (hasGpsGaps) {
            riskScore += 0.1 // 10% penalty for GPS gaps
        }
        
        // Normalize score to 0-1 range
        riskScore = riskScore.coerceIn(0.0, 1.0)
        
        // Determine risk level from score
        return when {
            riskScore >= 0.7 -> "HIGH"
            riskScore >= 0.4 -> "MEDIUM"
            else -> "LOW"
        }
    }

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

        // ===== COMPLIANCE VALIDATION =====
        val validationResult = validateWorkflowCompliance(workflow)
        if (!validationResult.isCompliant) {
            throw IllegalStateException("Workflow failed compliance checks: ${validationResult.failureReasons.joinToString("; ")}")
        }

        // Get exporter's Hedera account
        val exporterCredentials = hederaAccountCredentialsRepository
            .findByEntityTypeAndEntityId("EXPORTER", workflow.exporter.id)
            .orElseThrow { 
                IllegalStateException("Exporter does not have a Hedera account. Please create one first.") 
            }

        // Build compliance data from validation result
        val complianceData = mapOf(
            "workflowName" to workflow.workflowName,
            "produceType" to workflow.produceType,
            "totalQuantityKg" to workflow.totalQuantityKg.toString(),
            "totalFarmers" to validationResult.totalFarmers.toString(),
            "totalProductionUnits" to validationResult.totalProductionUnits.toString(),
            "gpsCoordinatesCount" to validationResult.gpsCoordinatesCount.toString(),
            "deforestationStatus" to validationResult.deforestationStatus,
            "originCountry" to validationResult.originCountry,
            "riskLevel" to validationResult.riskLevel,
            "traceabilityHash" to validationResult.traceabilityHash
        )

        // Issue the certificate NFT
        try {
            val (transactionId, serialNumber, nftTokenId) = hederaMainService.issueWorkflowComplianceCertificateNft(
                workflowId = workflow.id,
                exporterAccountId = AccountId.fromString(exporterCredentials.hederaAccountId),
                complianceData = complianceData
            )

            // Update workflow with certificate details
            workflow.complianceCertificateTransactionId = transactionId
            workflow.complianceCertificateSerialNumber = serialNumber
            workflow.complianceCertificateNftId = nftTokenId
            workflow.currentOwnerAccountId = exporterCredentials.hederaAccountId
            workflow.certificateStatus = CertificateStatus.COMPLIANT
            workflow.certificateIssuedAt = LocalDateTime.now()
            
            workflowRepository.save(workflow)

            logger.info("Certificate issued for workflow ${workflow.id}: TxID=$transactionId, Serial=$serialNumber, NFT=$nftTokenId")

            return mapOf(
                "workflowId" to workflow.id,
                "transactionId" to transactionId,
                "serialNumber" to serialNumber,
                "nftTokenId" to nftTokenId,
                "hederaAccountId" to exporterCredentials.hederaAccountId,
                "certificateStatus" to workflow.certificateStatus.name,
                "issuedAt" to workflow.certificateIssuedAt,
                "hashscanUrl" to "https://hashscan.io/testnet/token/$nftTokenId"
            )
        } catch (e: Exception) {
            logger.error("Failed to issue certificate for workflow ${workflow.id}", e)
            throw RuntimeException("Failed to issue certificate: ${e.message}", e)
        }
    }

    /**
     * Validate workflow for certificate (public method for pre-validation)
     * Returns validation result as a Map for API response
     */
    fun validateWorkflowForCertificate(workflowId: String): Map<String, Any?> {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }
        
        // Check if certificate already issued
        if (workflow.certificateStatus != CertificateStatus.NOT_CREATED && 
            workflow.certificateStatus != CertificateStatus.PENDING_VERIFICATION) {
            return mapOf(
                "isCompliant" to false,
                "failureReasons" to listOf("Certificate already issued for this workflow (Status: ${workflow.certificateStatus})")
            )
        }
        
        val validationResult = validateWorkflowCompliance(workflow)
        return mapOf(
            "isCompliant" to validationResult.isCompliant,
            "failureReasons" to validationResult.failureReasons,
            "totalFarmers" to validationResult.totalFarmers,
            "totalProductionUnits" to validationResult.totalProductionUnits,
            "gpsCoordinatesCount" to validationResult.gpsCoordinatesCount,
            "deforestationStatus" to validationResult.deforestationStatus,
            "originCountry" to validationResult.originCountry,
            "riskLevel" to validationResult.riskLevel
        )
    }

    /**
     * Issue EUDR Compliance Certificate NFT asynchronously
     * Returns immediately with pending status, processes in background
     */
    fun issueComplianceCertificateAsync(workflowId: String): Map<String, Any?> {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found") }

        // Check if certificate already issued
        if (workflow.certificateStatus != CertificateStatus.NOT_CREATED && 
            workflow.certificateStatus != CertificateStatus.PENDING_VERIFICATION) {
            throw IllegalStateException("Certificate already issued for this workflow (Status: ${workflow.certificateStatus})")
        }

        // Get exporter's Hedera account (fail fast if not available)
        val exporterCredentials = hederaAccountCredentialsRepository
            .findByEntityTypeAndEntityId("EXPORTER", workflow.exporter.id)
            .orElseThrow { 
                IllegalStateException("Exporter does not have a Hedera account. Please create one first.") 
            }

        // Mark workflow as pending verification immediately
        workflow.certificateStatus = CertificateStatus.PENDING_VERIFICATION
        workflowRepository.save(workflow)

        // Build compliance data for async processing
        val validationResult = validateWorkflowCompliance(workflow)
        val complianceData = mapOf(
            "workflowName" to workflow.workflowName,
            "produceType" to workflow.produceType,
            "totalQuantityKg" to workflow.totalQuantityKg.toString(),
            "totalFarmers" to validationResult.totalFarmers.toString(),
            "totalProductionUnits" to validationResult.totalProductionUnits.toString(),
            "gpsCoordinatesCount" to validationResult.gpsCoordinatesCount.toString(),
            "deforestationStatus" to validationResult.deforestationStatus,
            "originCountry" to validationResult.originCountry,
            "riskLevel" to validationResult.riskLevel,
            "traceabilityHash" to validationResult.traceabilityHash
        )

        // Process certificate issuance asynchronously
        Thread {
            try {
                logger.info("Starting async certificate issuance for workflow ${workflow.id}")
                val (transactionId, serialNumber, nftTokenId) = hederaMainService.issueWorkflowComplianceCertificateNft(
                    workflowId = workflow.id,
                    exporterAccountId = AccountId.fromString(exporterCredentials.hederaAccountId),
                    complianceData = complianceData
                )

                // Update workflow with certificate details
                val updatedWorkflow = workflowRepository.findById(workflowId).orElse(null)
                if (updatedWorkflow != null) {
                    updatedWorkflow.complianceCertificateTransactionId = transactionId
                    updatedWorkflow.complianceCertificateSerialNumber = serialNumber
                    updatedWorkflow.complianceCertificateNftId = nftTokenId
                    updatedWorkflow.currentOwnerAccountId = exporterCredentials.hederaAccountId
                    updatedWorkflow.certificateStatus = CertificateStatus.COMPLIANT
                    updatedWorkflow.certificateIssuedAt = LocalDateTime.now()
                    workflowRepository.save(updatedWorkflow)
                    logger.info("Certificate issued successfully for workflow ${workflow.id}: TxID=$transactionId, Serial=$serialNumber, NFT=$nftTokenId")
                }
            } catch (e: Exception) {
                logger.error("Async certificate issuance failed for workflow ${workflow.id}", e)
                // Revert status to NOT_CREATED on failure
                val failedWorkflow = workflowRepository.findById(workflowId).orElse(null)
                if (failedWorkflow != null && failedWorkflow.certificateStatus == CertificateStatus.PENDING_VERIFICATION) {
                    failedWorkflow.certificateStatus = CertificateStatus.NOT_CREATED
                    workflowRepository.save(failedWorkflow)
                }
            }
        }.start()

        return mapOf(
            "workflowId" to workflow.id,
            "status" to "PENDING_VERIFICATION",
            "message" to "Certificate issuance started. Check workflow status for completion.",
            "exporterHederaAccount" to exporterCredentials.hederaAccountId
        )
    }

    /**
     * Transfer certificate to importer
     * Auto-creates a Hedera account for the importer if they don't have one
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

        // Get or create importer's Hedera account
        val importerCredentials = try {
            hederaAccountCredentialsRepository
                .findByEntityTypeAndEntityId("IMPORTER", importerId)
                .orElseGet {
                    // Auto-create Hedera account for importer
                    logger.info("Importer $importerId doesn't have a Hedera account, creating one...")
                    importerService.getOrCreateHederaAccountForImporter(importerId)
                }
        } catch (e: Exception) {
            logger.error("Failed to get or create Hedera account for importer $importerId", e)
            throw IllegalStateException("Failed to setup Hedera account for importer: ${e.message}")
        }

        val exporterCredentials = hederaAccountCredentialsRepository
            .findByEntityTypeAndEntityId("EXPORTER", workflow.exporter.id)
            .orElseThrow { 
                IllegalStateException("Exporter Hedera account not found") 
            }

        // Get the NFT serial number from the workflow
        val serialNumber = workflow.complianceCertificateSerialNumber
            ?: throw IllegalStateException("Workflow does not have a certificate serial number. Certificate may not have been issued properly.")

        try {
            val success = hederaMainService.transferWorkflowComplianceCertificateNft(
                fromAccountId = AccountId.fromString(exporterCredentials.hederaAccountId),
                toAccountId = AccountId.fromString(importerCredentials.hederaAccountId),
                workflowId = workflow.id,
                serialNumber = serialNumber
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

    // ===== PRODUCTION UNIT LINKING (Stage 1: PRODUCTION_REGISTRATION) =====
    
    /**
     * Link a production unit to a workflow for EUDR Stage 1 registration
     */
    fun linkProductionUnit(workflowId: String, request: LinkProductionUnitRequestDto): WorkflowProductionUnitResponseDto {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found: $workflowId") }
        
        val productionUnit = productionUnitRepository.findById(request.productionUnitId)
            .orElseThrow { IllegalArgumentException("Production unit not found: ${request.productionUnitId}") }
        
        // Check if already linked
        if (workflowProductionUnitRepository.existsByWorkflowIdAndProductionUnitId(workflowId, request.productionUnitId)) {
            throw IllegalStateException("Production unit is already linked to this workflow")
        }
        
        val link = WorkflowProductionUnit(
            id = UUID.randomUUID().toString(),
            workflow = workflow,
            productionUnit = productionUnit,
            status = WorkflowProductionUnitStatus.PENDING,
            geolocationVerified = productionUnit.lastVerifiedAt != null,
            notes = request.notes
        )
        
        val saved = workflowProductionUnitRepository.save(link)
        logger.info("Linked production unit {} to workflow {}", request.productionUnitId, workflowId)
        
        return toWorkflowProductionUnitDto(saved)
    }
    
    /**
     * Get all production units linked to a workflow
     */
    fun getLinkedProductionUnits(workflowId: String): List<WorkflowProductionUnitResponseDto> {
        val links = workflowProductionUnitRepository.findByWorkflowId(workflowId)
        return links.map { toWorkflowProductionUnitDto(it) }
    }
    
    /**
     * Unlink a production unit from a workflow
     */
    fun unlinkProductionUnit(workflowId: String, productionUnitId: String) {
        val link = workflowProductionUnitRepository.findByWorkflowIdAndProductionUnitId(workflowId, productionUnitId)
            ?: throw IllegalArgumentException("Production unit not linked to this workflow")
        
        // Check if there are collection events for this production unit
        val collectionEvents = collectionEventRepository.findByWorkflowId(workflowId)
            .filter { it.productionUnit.id == productionUnitId }
        
        if (collectionEvents.isNotEmpty()) {
            throw IllegalStateException("Cannot unlink production unit with existing collection events")
        }
        
        workflowProductionUnitRepository.delete(link)
        logger.info("Unlinked production unit {} from workflow {}", productionUnitId, workflowId)
    }
    
    /**
     * Update production unit verification status
     */
    fun updateProductionUnitStatus(
        workflowId: String, 
        productionUnitId: String, 
        geolocationVerified: Boolean? = null,
        deforestationChecked: Boolean? = null,
        deforestationClear: Boolean? = null
    ): WorkflowProductionUnitResponseDto {
        val link = workflowProductionUnitRepository.findByWorkflowIdAndProductionUnitId(workflowId, productionUnitId)
            ?: throw IllegalArgumentException("Production unit not linked to this workflow")
        
        geolocationVerified?.let { link.geolocationVerified = it }
        deforestationChecked?.let { link.deforestationChecked = it }
        deforestationClear?.let { link.deforestationClear = it }
        
        // Update status based on verification state
        link.status = when {
            link.deforestationChecked && link.deforestationClear == true -> WorkflowProductionUnitStatus.DEFORESTATION_CLEAR
            link.geolocationVerified -> WorkflowProductionUnitStatus.VERIFIED
            else -> WorkflowProductionUnitStatus.PENDING
        }
        
        link.updatedAt = LocalDateTime.now()
        val saved = workflowProductionUnitRepository.save(link)
        
        return toWorkflowProductionUnitDto(saved)
    }
    
    /**
     * Set skip processing flag - for raw commodities that don't require processing
     */
    fun setSkipProcessing(workflowId: String, skip: Boolean) {
        val workflow = workflowRepository.findById(workflowId).orElseThrow {
            IllegalArgumentException("Workflow not found: $workflowId")
        }
        
        workflow.skipProcessing = skip
        workflow.updatedAt = LocalDateTime.now()
        workflowRepository.save(workflow)
        
        logger.info("Set skipProcessing={} for workflow {}", skip, workflowId)
    }
    
    private fun toWorkflowProductionUnitDto(link: WorkflowProductionUnit): WorkflowProductionUnitResponseDto {
        val productionUnit = link.productionUnit
        val farmer = productionUnit.farmer
        
        return WorkflowProductionUnitResponseDto(
            id = link.id,
            workflowId = link.workflow.id,
            productionUnitId = productionUnit.id,
            productionUnitName = productionUnit.unitName,
            farmerId = farmer.id,
            farmerName = farmer.let { it.userProfile.fullName.trim().ifEmpty { "Unknown" } },
            administrativeRegion = productionUnit.administrativeRegion,
            areaHectares = productionUnit.areaHectares,
            primaryCrops = productionUnit.farmer.farmerProduces.toString(),
            status = link.status.name,
            geolocationVerified = link.geolocationVerified,
            deforestationChecked = link.deforestationChecked,
            deforestationClear = link.deforestationClear,
            notes = link.notes,
            linkedAt = link.linkedAt,
            lastVerifiedAt = productionUnit.lastVerifiedAt
        )
    }

    fun getWorkflowProductionUnits(workflowId: String): List<WorkflowProductionUnit> {
        return workflowProductionUnitRepository.findByWorkflowId(workflowId)
    }
}
