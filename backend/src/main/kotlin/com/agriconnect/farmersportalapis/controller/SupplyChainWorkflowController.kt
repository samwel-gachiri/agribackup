package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.supplychain.DeforestationAlertService
import com.agriconnect.farmersportalapis.service.supplychain.SupplyChainWorkflowService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/supply-chain/workflows")
@Tag(name = "Supply Chain Workflow", description = "Visual workflow builder for end-to-end supply chain management")
@SecurityRequirement(name = "bearer-jwt")
class SupplyChainWorkflowController(
    private val workflowService: SupplyChainWorkflowService,
    private val deforestationAlertService: DeforestationAlertService
) {

    // ===== CREATE WORKFLOW =====
    @PostMapping("/exporter/{exporterId}")
    @Operation(summary = "Create new supply chain workflow")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun createWorkflow(
        @PathVariable exporterId: String,
        @RequestBody request: CreateWorkflowRequestDto
    ): ResponseEntity<WorkflowResponseDto> {
        val workflow = workflowService.createWorkflow(exporterId, request)
        return ResponseEntity.ok(workflow)
    }

    // ===== GET WORKFLOWS =====
    @GetMapping("/exporter/{exporterId}")
    @Operation(summary = "Get all workflows for exporter")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun getWorkflowsByExporter(
        @PathVariable exporterId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Map<String, Any?>> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val workflows = workflowService.getWorkflowsByExporter(exporterId, pageable)
        
        return ResponseEntity.ok(mapOf(
            "content" to workflows.content,
            "totalElements" to workflows.totalElements,
            "totalPages" to workflows.totalPages,
            "currentPage" to page,
            "pageSize" to size
        ))
    }

    @GetMapping("/{workflowId}")
    @Operation(summary = "Get workflow by ID")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun getWorkflowById(
        @PathVariable workflowId: String
    ): ResponseEntity<WorkflowResponseDto> {
        val workflow = workflowService.getWorkflowById(workflowId)
        return ResponseEntity.ok(workflow)
    }

    @GetMapping("/{workflowId}/summary")
    @Operation(summary = "Get complete workflow summary with all events")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun getWorkflowSummary(
        @PathVariable workflowId: String
    ): ResponseEntity<WorkflowSummaryDto> {
        val summary = workflowService.getWorkflowSummary(workflowId)
        return ResponseEntity.ok(summary)
    }

    // ===== COLLECTION EVENTS (Production Unit → Aggregator) =====
    @PostMapping("/{workflowId}/collection")
    @Operation(summary = "Add collection event", description = "Connect production unit to aggregator with quantity")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun addCollectionEvent(
        @PathVariable workflowId: String,
        @RequestBody request: AddCollectionEventRequestDto
    ): ResponseEntity<WorkflowCollectionEventResponseDto> {
        val event = workflowService.addCollectionEvent(workflowId, request)
        return ResponseEntity.ok(event)
    }

    @GetMapping("/{workflowId}/collection")
    @Operation(summary = "Get all collection events for workflow")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun getCollectionEvents(
        @PathVariable workflowId: String
    ): ResponseEntity<List<WorkflowCollectionEventResponseDto>> {
        val events = workflowService.getCollectionEvents(workflowId)
        return ResponseEntity.ok(events)
    }

    // ===== CONSOLIDATION EVENTS (Aggregator → Processor) =====
    @PostMapping("/{workflowId}/consolidation")
    @Operation(summary = "Add consolidation event", description = "Connect aggregator to processor with quantity (can split)")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun addConsolidationEvent(
        @PathVariable workflowId: String,
        @RequestBody request: AddConsolidationEventRequestDto
    ): ResponseEntity<WorkflowConsolidationEventResponseDto> {
        val event = workflowService.addConsolidationEvent(workflowId, request)
        return ResponseEntity.ok(event)
    }

    @GetMapping("/{workflowId}/consolidation")
    @Operation(summary = "Get all consolidation events for workflow")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun getConsolidationEvents(
        @PathVariable workflowId: String
    ): ResponseEntity<List<WorkflowConsolidationEventResponseDto>> {
        val events = workflowService.getConsolidationEvents(workflowId)
        return ResponseEntity.ok(events)
    }

    // ===== PROCESSING EVENTS =====
    @PostMapping("/{workflowId}/processing")
    @Operation(summary = "Add processing event")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun addProcessingEvent(
        @PathVariable workflowId: String,
        @RequestBody request: AddProcessingEventRequestDto
    ): ResponseEntity<WorkflowProcessingEventResponseDto> {
        val event = workflowService.addProcessingEvent(workflowId, request)
        return ResponseEntity.ok(event)
    }

    @GetMapping("/{workflowId}/processing")
    @Operation(summary = "Get all processing events for workflow")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun getProcessingEvents(
        @PathVariable workflowId: String
    ): ResponseEntity<List<WorkflowProcessingEventResponseDto>> {
        val events = workflowService.getProcessingEvents(workflowId)
        return ResponseEntity.ok(events)
    }

    // ===== SHIPMENT EVENTS (Processor → Importer) =====
    @PostMapping("/{workflowId}/shipment")
    @Operation(summary = "Add shipment event", description = "Connect processor to importer for shipping")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun addShipmentEvent(
        @PathVariable workflowId: String,
        @RequestBody request: AddShipmentEventRequestDto
    ): ResponseEntity<WorkflowShipmentEventResponseDto> {
        val event = workflowService.addShipmentEvent(workflowId, request)
        return ResponseEntity.ok(event)
    }

    @GetMapping("/{workflowId}/shipment")
    @Operation(summary = "Get all shipment events for workflow")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun getShipmentEvents(
        @PathVariable workflowId: String
    ): ResponseEntity<List<WorkflowShipmentEventResponseDto>> {
        val events = workflowService.getShipmentEvents(workflowId)
        return ResponseEntity.ok(events)
    }

    // ===== AVAILABLE QUANTITIES =====
    @GetMapping("/{workflowId}/available-quantities")
    @Operation(summary = "Get available quantities per aggregator", description = "Shows how much each aggregator has available to send to processors")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun getAvailableQuantities(
        @PathVariable workflowId: String
    ): ResponseEntity<List<AvailableQuantityDto>> {
        val quantities = workflowService.getAvailableQuantitiesPerAggregator(workflowId)
        return ResponseEntity.ok(quantities)
    }

    // ===== PRODUCTION UNIT LINKING (Stage 1: PRODUCTION_REGISTRATION) =====
    @GetMapping("/{workflowId}/production-units")
    @Operation(
        summary = "Get linked production units",
        description = "Get all production units linked to this workflow for EUDR Stage 1 registration"
    )
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun getLinkedProductionUnits(
        @PathVariable workflowId: String
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            val units = workflowService.getLinkedProductionUnits(workflowId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to units,
                "count" to units.size
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message,
                "error" to "INVALID_REQUEST"
            ))
        }
    }

    @PostMapping("/{workflowId}/production-units")
    @Operation(
        summary = "Link production unit to workflow",
        description = "Link a production unit to this workflow for EUDR Stage 1 registration"
    )
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun linkProductionUnit(
        @PathVariable workflowId: String,
        @RequestBody request: LinkProductionUnitRequestDto
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            val link = workflowService.linkProductionUnit(workflowId, request)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Production unit linked successfully",
                "data" to link
            ))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message,
                "error" to "ALREADY_LINKED"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message,
                "error" to "INVALID_REQUEST"
            ))
        }
    }

    @PostMapping("/{workflowId}/production-units/{productionUnitId}/verify-geolocation")
    @Operation(
        summary = "Verify geolocation for production unit",
        description = "Mark a production unit's geolocation as verified (EUDR Stage 2)"
    )
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun verifyGeolocation(
        @PathVariable workflowId: String,
        @PathVariable productionUnitId: String
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            val updated = workflowService.updateProductionUnitStatus(
                workflowId = workflowId,
                productionUnitId = productionUnitId,
                geolocationVerified = true
            )
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Geolocation verified successfully",
                "data" to updated
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message,
                "error" to "INVALID_REQUEST"
            ))
        }
    }

    @PostMapping("/{workflowId}/production-units/{productionUnitId}/check-deforestation")
    @Operation(
        summary = "Run deforestation check for production unit",
        description = "Run a deforestation check using Global Forest Watch satellite imagery (EUDR Stage 3)"
    )
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun checkDeforestation(
        @PathVariable workflowId: String,
        @PathVariable productionUnitId: String
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            // Verify the production unit is linked to this workflow
            val link = workflowService.getWorkflowProductionUnits(workflowId)
                .find { it.productionUnit.id == productionUnitId }
                ?: throw IllegalArgumentException("Production unit not linked to this workflow")
            
            val productionUnit = link.productionUnit
            
            // Run actual EUDR compliance check using Global Forest Watch
            val isCompliant = deforestationAlertService.checkEudrCompliance(productionUnit)
            
            // Get alert summary for detailed information
            val alertSummary = deforestationAlertService.getAlertSummary(productionUnitId)
            
            // Update the workflow production unit status
            val updated = workflowService.updateProductionUnitStatus(
                workflowId = workflowId,
                productionUnitId = productionUnitId,
                deforestationChecked = true,
                deforestationClear = isCompliant
            )
            
            val message = if (isCompliant) {
                "Deforestation check completed - No post-2020 deforestation detected. EUDR Compliant."
            } else {
                "Deforestation check completed - WARNING: Deforestation alerts found. Not EUDR Compliant."
            }
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to message,
                "data" to updated,
                "deforestationClear" to isCompliant,
                "eudrCompliant" to isCompliant,
                "alertSummary" to mapOf(
                    "totalAlerts" to alertSummary.totalAlerts,
                    "highSeverityAlerts" to alertSummary.highSeverityAlerts,
                    "mediumSeverityAlerts" to alertSummary.mediumSeverityAlerts,
                    "lowSeverityAlerts" to alertSummary.lowSeverityAlerts,
                    "gladAlerts" to alertSummary.gladAlerts,
                    "treeCoverLossAlerts" to alertSummary.treeCoverLossAlerts,
                    "lastAlertDate" to alertSummary.lastAlertDate?.toString()
                )
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message,
                "error" to "INVALID_REQUEST"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to run deforestation check: ${e.message}",
                "error" to "DEFORESTATION_CHECK_FAILED"
            ))
        }
    }

    @DeleteMapping("/{workflowId}/production-units/{productionUnitId}")
    @Operation(
        summary = "Unlink production unit from workflow",
        description = "Remove a production unit from this workflow (only if no collection events exist)"
    )
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun unlinkProductionUnit(
        @PathVariable workflowId: String,
        @PathVariable productionUnitId: String
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            workflowService.unlinkProductionUnit(workflowId, productionUnitId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Production unit unlinked successfully"
            ))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message,
                "error" to "HAS_COLLECTION_EVENTS"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message,
                "error" to "NOT_LINKED"
            ))
        }
    }

    // ===== PROCESSING STAGE (OPTIONAL) =====
    @PostMapping("/{workflowId}/skip-processing")
    @Operation(
        summary = "Skip processing stage",
        description = "Mark the workflow as not requiring processing (for raw commodity exports)"
    )
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun skipProcessing(
        @PathVariable workflowId: String,
        @RequestParam(defaultValue = "true") skip: Boolean
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            workflowService.setSkipProcessing(workflowId, skip)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to if (skip) "Processing stage skipped" else "Processing stage restored",
                "skipProcessing" to skip
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        }
    }

    // ===== CERTIFICATE MANAGEMENT =====
    @PostMapping("/{workflowId}/issue-certificate")
    @Operation(
        summary = "Issue EUDR Compliance Certificate NFT for workflow (async)",
        description = "Initiates async issuance of a blockchain-based EUDR compliance certificate NFT after all compliance checks pass. Returns immediately with pending status."
    )
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun issueCertificate(
        @PathVariable workflowId: String
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            // First validate compliance synchronously (fast)
            val validationResult = workflowService.validateWorkflowForCertificate(workflowId)
            
            val isCompliant = validationResult["isCompliant"] as? Boolean ?: false
            if (!isCompliant) {
                return ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "message" to "Workflow failed compliance checks: ${(validationResult["failureReasons"] as List<*>).joinToString(", ")}",
                    "error" to "COMPLIANCE_CHECK_FAILED",
                    "validationResult" to validationResult
                ))
            }
            
            // Start async certificate issuance (returns immediately)
            val result = workflowService.issueComplianceCertificateAsync(workflowId)
            
            ResponseEntity.accepted().body(mapOf(
                "success" to true,
                "message" to "Certificate issuance initiated. This process runs in the background.",
                "status" to "PENDING",
                "data" to result,
                "note" to "Hedera blockchain operations may take 30-60 seconds. Poll the workflow status endpoint to check completion."
            ))
        } catch (e: IllegalStateException) {
            val isHederaAccountError = e.message?.contains("Hedera account", ignoreCase = true) == true
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message,
                "error" to "COMPLIANCE_CHECK_FAILED",
                "actionRequired" to if (isHederaAccountError) "CREATE_HEDERA_ACCOUNT" else null,
                "actionUrl" to if (isHederaAccountError) "/api/v1/supply-chain/workflows/exporter/{exporterId}/hedera-account" else null
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message,
                "error" to "INVALID_REQUEST"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to issue certificate: ${e.message}",
                "error" to "CERTIFICATE_ISSUANCE_FAILED"
            ))
        }
    }

    @PostMapping("/{workflowId}/transfer-certificate")
    @Operation(
        summary = "Transfer certificate to importer",
        description = "Transfers the EUDR compliance certificate NFT to the importer's Hedera account"
    )
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun transferCertificate(
        @PathVariable workflowId: String,
        @RequestParam importerId: String
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            workflowService.transferCertificateToImporter(workflowId, importerId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Certificate transferred successfully to importer"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message,
                "error" to "INVALID_REQUEST"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to transfer certificate: ${e.message}",
                "error" to "CERTIFICATE_TRANSFER_FAILED"
            ))
        }
    }

    @PostMapping("/exporter/{exporterId}/hedera-account")
    @Operation(
        summary = "Create Hedera account for exporter",
        description = "Creates a new Hedera account for the exporter to enable certificate issuance"
    )
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun createHederaAccount(
        @PathVariable exporterId: String
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            val accountData = workflowService.createHederaAccountForExporter(exporterId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Hedera account created successfully",
                "data" to accountData
            ))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message,
                "error" to "ACCOUNT_EXISTS_OR_CREATION_FAILED"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to create Hedera account: ${e.message}",
                "error" to "ACCOUNT_CREATION_FAILED"
            ))
        }
    }

    @GetMapping("/exporter/{exporterId}/hedera-account")
    @Operation(
        summary = "Check if exporter has Hedera account",
        description = "Returns the exporter's Hedera account status and details if account exists"
    )
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun getHederaAccount(
        @PathVariable exporterId: String
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            val accountData = workflowService.getHederaAccountForExporter(exporterId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "hasAccount" to true,
                "data" to accountData
            ))
        } catch (e: IllegalStateException) {
            ResponseEntity.ok(mapOf(
                "success" to true,
                "hasAccount" to false,
                "message" to "No Hedera account found for this exporter"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to check Hedera account: ${e.message}"
            ))
        }
    }
}
