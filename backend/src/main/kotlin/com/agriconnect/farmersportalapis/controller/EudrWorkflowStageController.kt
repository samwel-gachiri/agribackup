package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.domain.eudr.EudrComplianceStage
import com.agriconnect.farmersportalapis.service.eudr.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * EUDR Workflow Stage Controller
 * 
 * REST API endpoints for managing EUDR compliance workflow stages.
 * Provides step-by-step guidance through the 10 compliance stages.
 */
@RestController
@RequestMapping("/api/eudr/workflow-stages")
@Tag(name = "EUDR Workflow Stages", description = "EUDR Compliance Workflow Stage Management")
@CrossOrigin(origins = ["*"])
class EudrWorkflowStageController(
    private val eudrWorkflowStageService: EudrWorkflowStageService
) {

    // ===== STAGE INFORMATION =====

    @GetMapping("/stages")
    @Operation(summary = "Get all EUDR compliance stages", description = "Returns all 10 EUDR compliance stages with their descriptions and requirements")
    fun getAllStages(): ResponseEntity<List<StageInfoDto>> {
        val stages = EudrComplianceStage.entries.map { stage ->
            StageInfoDto(
                stage = stage.name,
                order = stage.order,
                displayName = stage.displayName,
                description = stage.description,
                requiredActions = stage.requiredActions,
                automatedActions = stage.automatedActions,
                nextStage = stage.nextStage,
                previousStage = stage.previousStage
            )
        }
        return ResponseEntity.ok(stages)
    }

    @GetMapping("/stages/{stageName}")
    @Operation(summary = "Get specific stage information", description = "Returns detailed information about a specific EUDR compliance stage")
    fun getStageInfo(@PathVariable stageName: String): ResponseEntity<StageInfoDto> {
        val stage = EudrComplianceStage.fromName(stageName)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(StageInfoDto(
            stage = stage.name,
            order = stage.order,
            displayName = stage.displayName,
            description = stage.description,
            requiredActions = stage.requiredActions,
            automatedActions = stage.automatedActions,
            nextStage = stage.nextStage,
            previousStage = stage.previousStage
        ))
    }

    @GetMapping("/stages/{stageName}/guidance")
    @Operation(summary = "Get stage guidance", description = "Returns detailed guidance, tips, and EUDR article references for a stage")
    fun getStageGuidance(@PathVariable stageName: String): ResponseEntity<EudrStageGuidanceDto> {
        val stage = EudrComplianceStage.fromName(stageName)
            ?: return ResponseEntity.notFound().build()
        
        return ResponseEntity.ok(eudrWorkflowStageService.getStageGuidance(stage))
    }

    // ===== WORKFLOW STAGE STATUS =====

    @GetMapping("/workflows/{workflowId}/current-stage")
    @Operation(summary = "Get current stage for workflow", description = "Returns the current EUDR compliance stage for a specific workflow")
    fun getCurrentStage(@PathVariable workflowId: String): ResponseEntity<EudrStageStatusDto> {
        return try {
            val status = eudrWorkflowStageService.getCurrentStage(workflowId)
            ResponseEntity.ok(status)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/workflows/{workflowId}/progress")
    @Operation(summary = "Get workflow progress overview", description = "Returns the complete EUDR compliance progress for a workflow including all stage statuses")
    fun getWorkflowProgress(@PathVariable workflowId: String): ResponseEntity<EudrWorkflowProgressDto> {
        return try {
            val progress = eudrWorkflowStageService.getWorkflowProgress(workflowId)
            ResponseEntity.ok(progress)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    // ===== STAGE VALIDATION =====

    @GetMapping("/workflows/{workflowId}/validate/{stageName}")
    @Operation(summary = "Validate stage requirements", description = "Validates if a workflow meets all requirements for a specific stage")
    fun validateStageRequirements(
        @PathVariable workflowId: String,
        @PathVariable stageName: String
    ): ResponseEntity<StageValidationResult> {
        val stage = EudrComplianceStage.fromName(stageName)
            ?: return ResponseEntity.badRequest().build()
        
        return try {
            val result = eudrWorkflowStageService.validateStageRequirements(workflowId, stage)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    // ===== STAGE ADVANCEMENT =====

    @PostMapping("/workflows/{workflowId}/advance")
    @Operation(summary = "Advance to next stage", description = "Attempts to advance the workflow to the next EUDR compliance stage")
    fun advanceToNextStage(@PathVariable workflowId: String): ResponseEntity<StageAdvancementResult> {
        return try {
            val result = eudrWorkflowStageService.advanceToNextStage(workflowId)
            if (result.success) {
                ResponseEntity.ok(result)
            } else {
                ResponseEntity.badRequest().body(result)
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/workflows/{workflowId}/revert")
    @Operation(summary = "Revert to previous stage", description = "Reverts the workflow to the previous stage for corrections")
    fun revertToPreviousStage(
        @PathVariable workflowId: String,
        @RequestBody request: RevertStageRequest
    ): ResponseEntity<StageAdvancementResult> {
        return try {
            val result = eudrWorkflowStageService.revertToPreviousStage(workflowId, request.reason)
            if (result.success) {
                ResponseEntity.ok(result)
            } else {
                ResponseEntity.badRequest().body(result)
            }
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    // ===== RISK ASSESSMENT =====

    @PostMapping("/workflows/{workflowId}/risk-assessment")
    @Operation(summary = "Trigger risk assessment", description = "Triggers a comprehensive risk assessment for the workflow based on country, deforestation, and supply chain factors")
    fun triggerRiskAssessment(@PathVariable workflowId: String): ResponseEntity<RiskAssessmentResultDto> {
        return try {
            val result = eudrWorkflowStageService.triggerRiskAssessment(workflowId)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    // ===== DUE DILIGENCE STATEMENT =====

    @PostMapping("/workflows/{workflowId}/due-diligence-statement")
    @Operation(summary = "Generate Due Diligence Statement", description = "Generates the EU Due Diligence Statement required for market access")
    fun generateDueDiligenceStatement(@PathVariable workflowId: String): ResponseEntity<DueDiligenceStatementDto> {
        return try {
            val result = eudrWorkflowStageService.generateDueDiligenceStatement(workflowId)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/workflows/{workflowId}/generate-dds")
    @Operation(summary = "Generate Due Diligence Statement (alias)", description = "Alias for due-diligence-statement endpoint")
    fun generateDds(@PathVariable workflowId: String): ResponseEntity<DueDiligenceStatementDto> {
        return generateDueDiligenceStatement(workflowId)
    }

    // ===== DDS DOWNLOAD & PREVIEW =====

    @GetMapping("/workflows/{workflowId}/dds/download")
    @Operation(summary = "Download DDS as PDF", description = "Downloads the Due Diligence Statement as a PDF document")
    fun downloadDdsPdf(@PathVariable workflowId: String): ResponseEntity<ByteArrayResource> {
        return try {
            val pdfBytes = eudrWorkflowStageService.generateDdsPdf(workflowId)
            val resource = ByteArrayResource(pdfBytes)
            
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"DDS-$workflowId.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.size.toLong())
                .body(resource)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @GetMapping("/workflows/{workflowId}/dds/preview")
    @Operation(summary = "Preview DDS data", description = "Returns DDS data for preview without generating a file")
    fun previewDds(@PathVariable workflowId: String): ResponseEntity<DueDiligenceStatementDto> {
        return try {
            val result = eudrWorkflowStageService.generateDueDiligenceStatement(workflowId)
            ResponseEntity.ok(result)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}

// ===== Request/Response DTOs =====

data class StageInfoDto(
    val stage: String,
    val order: Int,
    val displayName: String,
    val description: String,
    val requiredActions: List<String>,
    val automatedActions: List<String>,
    val nextStage: String?,
    val previousStage: String?
)

data class RevertStageRequest(
    val reason: String
)
