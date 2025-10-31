package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.domain.eudr.MitigationActionStatus
import com.agriconnect.farmersportalapis.domain.eudr.MitigationActionType
import com.agriconnect.farmersportalapis.domain.eudr.RiskLevel
import com.agriconnect.farmersportalapis.service.eudr.CreateMitigationActionDto
import com.agriconnect.farmersportalapis.service.eudr.MitigationWorkflowService
import com.agriconnect.farmersportalapis.service.eudr.UpdateActionStatusDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/eudr/mitigation")
@Tag(name = "EUDR Mitigation", description = "Mitigation workflow and action management for EUDR compliance")
class MitigationController(
    private val mitigationWorkflowService: MitigationWorkflowService
) {

    @PostMapping("/workflows")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Create mitigation workflow", description = "Create a new mitigation workflow for a batch with risk issues")
    fun createWorkflow(
        @RequestBody request: CreateWorkflowRequest,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            val createdBy = authentication.name

            val workflow = mitigationWorkflowService.createWorkflow(
                batchId = request.batchId,
                riskLevel = RiskLevel.valueOf(request.riskLevel),
                createdBy = createdBy,
                initialActions = request.initialActions.map {
                    CreateMitigationActionDto(
                        actionType = MitigationActionType.valueOf(it.actionType),
                        description = it.description,
                        assignedTo = it.assignedTo,
                        dueDate = it.dueDate
                    )
                }
            )

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to workflow,
                "message" to "Mitigation workflow created successfully with Hedera audit trail"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to create mitigation workflow: ${e.message}"
            ))
        }
    }

    @PostMapping("/workflows/{workflowId}/actions")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Add mitigation action", description = "Add a new mitigation action to an existing workflow")
    fun addAction(
        @PathVariable workflowId: String,
        @RequestBody request: CreateActionRequest,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            val addedBy = authentication.name

            val action = mitigationWorkflowService.addMitigationAction(
                workflowId = workflowId,
                actionDto = CreateMitigationActionDto(
                    actionType = MitigationActionType.valueOf(request.actionType),
                    description = request.description,
                    assignedTo = request.assignedTo,
                    dueDate = request.dueDate
                ),
                addedBy = addedBy
            )

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to action,
                "message" to "Mitigation action added successfully"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to add mitigation action: ${e.message}"
            ))
        }
    }

    @PutMapping("/actions/{actionId}/status")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Update action status", description = "Update the status of a mitigation action and record on Hedera")
    fun updateActionStatus(
        @PathVariable actionId: String,
        @RequestBody request: UpdateStatusRequest,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            val updatedBy = authentication.name

            val action = mitigationWorkflowService.updateActionStatus(
                actionId = actionId,
                newStatus = MitigationActionStatus.valueOf(request.newStatus),
                completionEvidence = request.completionEvidence,
                updatedBy = updatedBy
            )

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to action,
                "message" to "Action status updated successfully with Hedera audit trail"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to update action status: ${e.message}"
            ))
        }
    }

    @PutMapping("/workflows/{workflowId}/complete")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Complete workflow", description = "Complete a mitigation workflow after all actions are done")
    fun completeWorkflow(
        @PathVariable workflowId: String,
        @RequestBody request: CompleteWorkflowRequest,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            val completedBy = authentication.name

            val workflow = mitigationWorkflowService.completeWorkflow(
                workflowId = workflowId,
                completionNotes = request.completionNotes,
                completedBy = completedBy
            )

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to workflow,
                "message" to "Workflow completed successfully with Hedera audit trail"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: IllegalStateException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to complete workflow: ${e.message}"
            ))
        }
    }

    @GetMapping("/workflows/batch/{batchId}")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR')")
    @Operation(summary = "Get workflows by batch", description = "Get all mitigation workflows for a specific batch")
    fun getWorkflowsByBatch(@PathVariable batchId: String): ResponseEntity<Any> {
        return try {
            val workflows = mitigationWorkflowService.getWorkflowsByBatch(batchId)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to workflows,
                "message" to "Workflows retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve workflows: ${e.message}"
            ))
        }
    }

    @GetMapping("/workflows/{workflowId}")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR')")
    @Operation(summary = "Get workflow details", description = "Get detailed information about a specific workflow")
    fun getWorkflow(@PathVariable workflowId: String): ResponseEntity<Any> {
        return try {
            val workflow = mitigationWorkflowService.getWorkflowById(workflowId)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to workflow,
                "message" to "Workflow details retrieved successfully"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve workflow: ${e.message}"
            ))
        }
    }

    @GetMapping("/workflows/active")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Get active workflows", description = "Get all pending or in-progress mitigation workflows")
    fun getActiveWorkflows(): ResponseEntity<Any> {
        return try {
            val workflows = mitigationWorkflowService.getActiveWorkflows()

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to workflows,
                "message" to "Active workflows retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve active workflows: ${e.message}"
            ))
        }
    }

    @GetMapping("/workflows/my")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Get my workflows", description = "Get all workflows created by the current user")
    fun getMyWorkflows(authentication: Authentication): ResponseEntity<Any> {
        return try {
            val createdBy = authentication.name
            val workflows = mitigationWorkflowService.getWorkflowsByCreator(createdBy)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to workflows,
                "message" to "User workflows retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve user workflows: ${e.message}"
            ))
        }
    }

    @GetMapping("/workflows/{workflowId}/actions")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR')")
    @Operation(summary = "Get workflow actions", description = "Get all actions for a specific workflow")
    fun getWorkflowActions(@PathVariable workflowId: String): ResponseEntity<Any> {
        return try {
            val actions = mitigationWorkflowService.getActionsByWorkflow(workflowId)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to actions,
                "message" to "Workflow actions retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve workflow actions: ${e.message}"
            ))
        }
    }

    @GetMapping("/actions/my")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Get my actions", description = "Get all actions assigned to the current user")
    fun getMyActions(authentication: Authentication): ResponseEntity<Any> {
        return try {
            val userId = authentication.name
            val actions = mitigationWorkflowService.getActionsAssignedTo(userId)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to actions,
                "message" to "Assigned actions retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve assigned actions: ${e.message}"
            ))
        }
    }

    @GetMapping("/actions/overdue")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Get overdue actions", description = "Get all overdue mitigation actions")
    fun getOverdueActions(): ResponseEntity<Any> {
        return try {
            val actions = mitigationWorkflowService.getOverdueActions()

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to actions,
                "message" to "Overdue actions retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve overdue actions: ${e.message}"
            ))
        }
    }

    @GetMapping("/workflows/high-risk-pending")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Get high-risk pending workflows", description = "Get all high-risk workflows that are still pending")
    fun getHighRiskPendingWorkflows(): ResponseEntity<Any> {
        return try {
            val workflows = mitigationWorkflowService.getHighRiskPendingWorkflows()

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to workflows,
                "message" to "High-risk pending workflows retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve high-risk workflows: ${e.message}"
            ))
        }
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Get workflow statistics", description = "Get statistical overview of mitigation workflows")
    fun getStatistics(): ResponseEntity<Any> {
        return try {
            val statistics = mitigationWorkflowService.getWorkflowStatistics()

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to statistics,
                "message" to "Workflow statistics retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve workflow statistics: ${e.message}"
            ))
        }
    }
}

// Request DTOs

data class CreateWorkflowRequest(
    val batchId: String,
    val riskLevel: String,
    val initialActions: List<CreateActionRequestDto> = emptyList()
)

data class CreateActionRequestDto(
    val actionType: String,
    val description: String,
    val assignedTo: String?,
    val dueDate: LocalDate?
)

data class CreateActionRequest(
    val actionType: String,
    val description: String,
    val assignedTo: String?,
    val dueDate: LocalDate?
)

data class UpdateStatusRequest(
    val newStatus: String,
    val completionEvidence: String?
)

data class CompleteWorkflowRequest(
    val completionNotes: String?
)
