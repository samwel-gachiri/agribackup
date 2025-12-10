package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
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
    private val workflowService: SupplyChainWorkflowService
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

    // ===== CERTIFICATE MANAGEMENT =====
    @PostMapping("/{workflowId}/issue-certificate")
    @Operation(
        summary = "Issue EUDR Compliance Certificate NFT for workflow",
        description = "Issues a blockchain-based EUDR compliance certificate NFT after all compliance checks pass"
    )
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun issueCertificate(
        @PathVariable workflowId: String
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            val result = workflowService.issueComplianceCertificate(workflowId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Certificate issued successfully",
                "data" to result
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
