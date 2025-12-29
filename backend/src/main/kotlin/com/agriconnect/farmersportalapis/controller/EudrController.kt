package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.BatchTransferRequestDto
import com.agriconnect.farmersportalapis.application.dtos.CreateBatchRequestDto
import com.agriconnect.farmersportalapis.application.dtos.UpdateBatchStatusRequestDto
import com.agriconnect.farmersportalapis.domain.eudr.BatchStatus
import com.agriconnect.farmersportalapis.domain.eudr.RiskLevel
import com.agriconnect.farmersportalapis.domain.eudr.SubmissionStatus
import com.agriconnect.farmersportalapis.service.common.RiskAssessmentService
import com.agriconnect.farmersportalapis.service.hedera.HederaTokenService
import com.agriconnect.farmersportalapis.service.supplychain.*
import com.agriconnect.farmersportalapis.service.traces.EuTracesNtClient
import com.agriconnect.farmersportalapis.service.traces.EuTracesNtDdsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/eudr")
@Tag(name = "EUDR Compliance", description = "EUDR compliance and risk assessment endpoints")
class EudrController(
    private val riskAssessmentService: RiskAssessmentService,
    private val dossierService: DossierService,
    private val supplierComplianceService: SupplierComplianceService,
    private val eudrBatchService: EudrBatchService,
    private val hederaTokenService: HederaTokenService,
    private val supplyChainWorkflowService: SupplyChainWorkflowService,
    private val authoritySubmissionService: AuthoritySubmissionService,
    private val tracesNtDdsService: EuTracesNtDdsService,
    private val tracesNtClient: EuTracesNtClient
) {

    @PostMapping("/assess")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Assess risk for a batch", description = "Perform comprehensive risk assessment for an EUDR batch")
    fun assessBatchRisk(@RequestParam batchId: String): ResponseEntity<Any> {
        return try {
            val result = riskAssessmentService.assessBatchRisk(batchId)
            if (result.riskLevel == RiskLevel.LOW ) {
                hederaTokenService.getEudrComplianceCertificateNftId()
            }
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to result,
                "message" to "Risk assessment completed successfully"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to assess batch risk: ${e.message}"
            ))
        }
    }

    @GetMapping("/assess")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Assess risk for a batch", description = "Perform comprehensive risk assessment for an EUDR batch")
    fun getAssessment(@RequestParam batchId: String): ResponseEntity<Any> {
        return try {
            val result = riskAssessmentService.assessBatchRisk(batchId)
            if (result.riskLevel == RiskLevel.LOW ) {
                hederaTokenService.getEudrComplianceCertificateNftId()
            }
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to result,
                "message" to "Risk assessment completed successfully"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to assess batch risk: ${e.message}"
            ))
        }
    }

    @PostMapping("/assess/bulk")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Bulk risk assessment", description = "Assess risk for multiple batches")
    fun assessBatchRiskBulk(@RequestBody batchIds: List<String>): ResponseEntity<Any> {
        return try {
            val results = riskAssessmentService.assessBatchRiskBulk(batchIds)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to results,
                "message" to "Bulk risk assessment completed for ${batchIds.size} batches"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to perform bulk risk assessment: ${e.message}"
            ))
        }
    }

    @GetMapping("/assess/{batchId}/history")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Get risk assessment history", description = "Retrieve risk assessment history for a batch")
    fun getRiskAssessmentHistory(@PathVariable batchId: String): ResponseEntity<Any> {
        return try {
            val history = riskAssessmentService.getRiskAssessmentHistory(batchId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to history,
                "message" to "Risk assessment history retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve risk assessment history: ${e.message}"
            ))
        }
    }

    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR')")
    @Operation(summary = "Generate dossier/report", description = "Generate comprehensive EUDR dossier for regulatory compliance")
    fun generateDossier(
        @RequestParam batchId: String,
        @RequestParam(defaultValue = "JSON") format: String,
        @RequestParam(defaultValue = "true") includePresignedUrls: Boolean,
        @RequestParam(defaultValue = "60") expiryMinutes: Int,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            // Validate access permissions
            val userId = authentication.name
            val userRoles = authentication.authorities.map { it.authority }
            if (!dossierService.validateDossierAccess(batchId, userId, userRoles)) {
                return ResponseEntity.status(403).body(mapOf(
                    "success" to false,
                    "message" to "Access denied to dossier"
                ))
            }

            val dossierFormat = when (format.uppercase()) {
                "PDF" -> DossierFormat.PDF
                "ZIP" -> DossierFormat.ZIP
                else -> DossierFormat.JSON
            }

            val result = dossierService.generateDossier(batchId, dossierFormat, includePresignedUrls, expiryMinutes)

            val headers = HttpHeaders()
            headers.contentType = MediaType.parseMediaType(result.contentType)
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${result.filename}\"")

            ResponseEntity.ok()
                .headers(headers)
                .body(result.content)

        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to generate dossier: ${e.message}"
            ))
        }
    }

    @GetMapping("/report/{batchId}/metadata")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR')")
    @Operation(summary = "Get dossier metadata", description = "Get metadata about available dossier without generating full content")
    fun getDossierMetadata(@PathVariable batchId: String): ResponseEntity<Any> {
        return try {
            val metadata = dossierService.getDossierMetadata(batchId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to metadata,
                "message" to "Dossier metadata retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve dossier metadata: ${e.message}"
            ))
        }
    }

    @GetMapping("/supplier-compliance")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get supplier compliance overview", description = "Get compliance overview for all suppliers associated with the current exporter")
    fun getSupplierComplianceOverview(authentication: Authentication): ResponseEntity<Any> {
        return try {
            val exporterId = authentication.name // Assuming authentication name is the exporter ID
            val overview = supplierComplianceService.getSupplierComplianceOverview(exporterId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to overview,
                "message" to "Supplier compliance overview retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve supplier compliance overview: ${e.message}"
            ))
        }
    }

    @GetMapping("/supplier-compliance/{supplierId}")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Get supplier compliance details", description = "Get detailed compliance information for a specific supplier")
    fun getSupplierCompliance(@PathVariable supplierId: String): ResponseEntity<Any> {
        return try {
            val compliance = supplierComplianceService.getSupplierCompliance(supplierId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to compliance,
                "message" to "Supplier compliance details retrieved successfully"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve supplier compliance details: ${e.message}"
            ))
        }
    }

    @PostMapping("/batches")
    @PreAuthorize("hasAnyRole('FARMER', 'EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Create a new EUDR batch", description = "Create a new batch with production units and record on Hedera for immutable audit trail")
    fun createBatch(@RequestBody request: CreateBatchRequestDto, authentication: Authentication): ResponseEntity<Any> {
        return try {
            val createdBy = authentication.name
            val batch = eudrBatchService.createBatch(request, createdBy)
            val response = eudrBatchService.convertToResponseDto(batch)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to response,
                "message" to "Batch created successfully with Hedera audit trail"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to create batch: ${e.message}"
            ))
        }
    }

    @GetMapping("/batches/{batchId}")
    @PreAuthorize("hasAnyRole('FARMER', 'EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Get batch details", description = "Retrieve detailed information about a specific batch")
    fun getBatch(@PathVariable batchId: String): ResponseEntity<Any> {
        return try {
            val batch = eudrBatchService.getBatchById(batchId)
                ?: return ResponseEntity.notFound().build()

            val response = eudrBatchService.convertToResponseDto(batch)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to response,
                "message" to "Batch details retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve batch: ${e.message}"
            ))
        }
    }

    @GetMapping("/batches")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Get batches by creator", description = "Retrieve all batches created by the current user")
    fun getMyBatches(authentication: Authentication): ResponseEntity<Any> {
        return try {
            val createdBy = authentication.name
            val responses = eudrBatchService.getBatchesByCreatorAsDto(createdBy)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to responses,
                "message" to "Batches retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve batches: ${e.message}"
            ))
        }
    }

        @GetMapping("/certificates")
        @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR', 'IMPORTER')")
        @Operation(summary = "Get all EUDR compliance certificates", description = "Retrieve all certificates from both EUDR batches and supply chain workflows")
        fun getCertificates(): ResponseEntity<Any> {
            return try {
                // Get certificates from EUDR batches (legacy)
                val batches = eudrBatchService.getAllBatches()
                val batchCertificates = batches.map { batch ->
                    mapOf(
                        "id" to batch.id,
                        "certificateType" to "BATCH",
                        "batchCode" to batch.batchCode,
                        "commodityDescription" to batch.commodityDescription,
                        "countryOfProduction" to batch.countryOfProduction,
                        "createdBy" to batch.createdBy,
                        "createdAt" to batch.createdAt,
                        "status" to batch.status.name,
                        "riskLevel" to batch.riskLevel?.name,
                        "riskRationale" to batch.riskRationale,
                        "hederaTransactionId" to batch.hederaTransactionId,
                        "complianceCertificateNftId" to try { batch.javaClass.getDeclaredField("complianceCertificateNftId").get(batch) } catch (e: Exception) { null },
                        "complianceCertificateSerialNumber" to try { batch.javaClass.getDeclaredField("complianceCertificateSerialNumber").get(batch) } catch (e: Exception) { null },
                        "complianceCertificateTransactionId" to try { batch.javaClass.getDeclaredField("complianceCertificateTransactionId").get(batch) } catch (e: Exception) { null }
                    )
                }

                // Get certificates from supply chain workflows (new proper implementation)
                val workflows = supplyChainWorkflowService.getAllWorkflows()
                val workflowCertificates = workflows.map { workflow ->
                    mapOf(
                        "id" to workflow.id,
                        "certificateType" to "WORKFLOW",
                        "workflowName" to workflow.workflowName,
                        "produceType" to workflow.produceType,
                        "totalQuantityKg" to workflow.totalQuantityKg,
                        "exporterId" to workflow.exporter.id,
                        "exporterCompanyName" to workflow.exporter.companyName,
                        "createdAt" to workflow.createdAt,
                        "completedAt" to workflow.completedAt,
                        "status" to workflow.status.name,
                        "currentStage" to workflow.currentStage.name,
                        "certificateStatus" to workflow.certificateStatus.name,
                        "complianceCertificateNftId" to workflow.complianceCertificateNftId,
                        "complianceCertificateSerialNumber" to workflow.complianceCertificateSerialNumber,
                        "complianceCertificateTransactionId" to workflow.complianceCertificateTransactionId,
                        "currentOwnerAccountId" to workflow.currentOwnerAccountId,
                        "certificateIssuedAt" to workflow.certificateIssuedAt
                    )
                }

                // Combine both types
                val allCertificates = batchCertificates + workflowCertificates

                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "data" to allCertificates,
                    "message" to "Certificates retrieved successfully",
                    "counts" to mapOf(
                        "total" to allCertificates.size,
                        "batchCertificates" to batchCertificates.size,
                        "workflowCertificates" to workflowCertificates.size
                    )
                ))
            } catch (e: Exception) {
                ResponseEntity.internalServerError().body(mapOf(
                    "success" to false,
                    "message" to "Failed to retrieve certificates: ${e.message}"
                ))
            }
        }
    @PutMapping("/batches/{batchId}/status")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Update batch status", description = "Update the status of a batch and record the change on Hedera")
    fun updateBatchStatus(
        @PathVariable batchId: String,
        @RequestBody request: UpdateBatchStatusRequestDto,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            val updatedBy = authentication.name
            val newStatus = BatchStatus.valueOf(request.newStatus.uppercase())
            val batch = eudrBatchService.updateBatchStatus(batchId, newStatus, updatedBy, request.reason)
            val response = eudrBatchService.convertToResponseDto(batch)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to response,
                "message" to "Batch status updated successfully with Hedera audit trail"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to update batch status: ${e.message}"
            ))
        }
    }

    @PostMapping("/batches/{batchId}/transfer")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Transfer batch ownership", description = "Transfer a batch to another actor in the supply chain and record on Hedera")
    fun transferBatch(
        @PathVariable batchId: String,
        @RequestBody request: BatchTransferRequestDto,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            val transferredBy = authentication.name
            val batch = eudrBatchService.transferBatch(batchId, request.toActorId, request.toActorRole, transferredBy, request.transferReason)
            val response = eudrBatchService.convertToResponseDto(batch)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to response,
                "message" to "Batch transferred successfully with Hedera audit trail"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to transfer batch: ${e.message}"
            ))
        }
    }

    // ========== AUTHORITY VERIFICATION ENDPOINTS ==========

    @GetMapping("/authority-report/{batchId}")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR')")
    @Operation(summary = "Generate authority compliance report", description = "Generate comprehensive compliance report for regulatory authorities")
    fun generateAuthorityReport(
        @PathVariable batchId: String,
        @RequestParam(defaultValue = "JSON") format: String,
        @RequestParam(defaultValue = "false") includeDigitalSignature: Boolean,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            val userId = authentication.name
            val userRoles = authentication.authorities.map { it.authority }
            
            // Validate access permissions
            if (!dossierService.validateDossierAccess(batchId, userId, userRoles)) {
                return ResponseEntity.status(403).body(mapOf(
                    "success" to false,
                    "message" to "Access denied to generate authority report"
                ))
            }

            val dossierFormat = when (format.uppercase()) {
                "PDF" -> DossierFormat.PDF
                "ZIP" -> DossierFormat.ZIP
                else -> DossierFormat.JSON
            }

            // Generate the dossier with full details
            val result = dossierService.generateDossier(
                batchId = batchId,
                format = dossierFormat,
                includePresignedUrls = true,
                expiryMinutes = 180 // 3 hours for authority review
            )

            // Add digital signature if requested
            val finalContent = if (includeDigitalSignature && format.uppercase() == "PDF") {
                // In production, implement digital signature using Java security libraries
                result.content
            } else {
                result.content
            }

            val headers = HttpHeaders()
            headers.contentType = MediaType.parseMediaType(result.contentType)
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Authority_Report_${result.filename}\"")
            
            // Set content length for binary content
            if (finalContent is ByteArray) {
                headers.contentLength = finalContent.size.toLong()
            }

            ResponseEntity.ok()
                .headers(headers)
                .body(finalContent)

        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to generate authority report: ${e.message}"
            ))
        }
    }

    @GetMapping("/authority-report/export")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'AUDITOR')")
    @Operation(summary = "Bulk export authority reports", description = "Export compliance reports for multiple batches")
    fun bulkExportAuthorityReports(
        @RequestParam batchIds: List<String>,
        @RequestParam(defaultValue = "JSON") format: String,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        @RequestParam(required = false) riskLevel: String?,
        @RequestParam(required = false) status: String?,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            val userId = authentication.name
            val userRoles = authentication.authorities.map { it.authority }

            // Filter batches based on provided criteria
            val filteredBatchIds = batchIds.filter { batchId ->
                try {
                    dossierService.validateDossierAccess(batchId, userId, userRoles)
                } catch (e: Exception) {
                    false
                }
            }

            if (filteredBatchIds.isEmpty()) {
                return ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "message" to "No accessible batches found matching the criteria"
                ))
            }

            val dossierFormat = when (format.uppercase()) {
                "PDF" -> DossierFormat.PDF
                else -> DossierFormat.JSON
            }

            // Generate reports for all accessible batches
            val reports = filteredBatchIds.mapNotNull { batchId ->
                try {
                    val result = dossierService.generateDossier(
                        batchId = batchId,
                        format = dossierFormat,
                        includePresignedUrls = true,
                        expiryMinutes = 180
                    )
                    mapOf(
                        "batchId" to batchId,
                        "filename" to result.filename,
                        "content" to result.content,
                        "generatedAt" to result.generatedAt.toString()
                    )
                } catch (e: Exception) {
                    null
                }
            }

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to mapOf(
                    "totalRequested" to batchIds.size,
                    "totalGenerated" to reports.size,
                    "reports" to reports
                ),
                "message" to "Bulk export completed: ${reports.size} of ${batchIds.size} reports generated"
            ))

        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to perform bulk export: ${e.message}"
            ))
        }
    }

    @PostMapping("/authority-report/submit")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Submit report to authority", description = "Submit compliance report to regulatory authority system")
    fun submitAuthorityReport(
        @RequestParam batchId: String,
        @RequestParam authorityCode: String,
        @RequestParam(required = false) notes: String?,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            val userId = authentication.name
            val userRoles = authentication.authorities.map { it.authority }

            // Validate access
            if (!dossierService.validateDossierAccess(batchId, userId, userRoles)) {
                return ResponseEntity.status(403).body(mapOf(
                    "success" to false,
                    "message" to "Access denied to submit authority report"
                ))
            }

            // Submit using the new service
            val submission = authoritySubmissionService.submitReport(
                batchId = batchId,
                authorityCode = authorityCode,
                userId = userId,
                notes = notes
            )

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to submission,
                "message" to "Report submitted successfully to authority ${submission.authorityName ?: authorityCode}"
            ))

        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to submit authority report: ${e.message}"
            ))
        }
    }

    @GetMapping("/authority-report/submissions")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'AUDITOR')")
    @Operation(summary = "Get submission history", description = "Retrieve history of authority report submissions")
    fun getSubmissionHistory(
        @RequestParam(required = false) batchId: String?,
        @RequestParam(required = false) authorityCode: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            val userId = authentication.name
            val submissionStatus = status?.let { 
                try { SubmissionStatus.valueOf(it) } catch (e: Exception) { null }
            }

            val submissions = authoritySubmissionService.getSubmissionHistory(
                userId = userId,
                batchId = batchId,
                authorityCode = authorityCode,
                status = submissionStatus,
                page = page,
                size = size
            )

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to mapOf(
                    "submissions" to submissions.content,
                    "page" to page,
                    "size" to size,
                    "totalElements" to submissions.totalElements,
                    "totalPages" to submissions.totalPages
                ),
                "message" to "Submission history retrieved successfully"
            ))

        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve submission history: ${e.message}"
            ))
        }
    }

    @GetMapping("/authority-report/submission-status/{submissionId}")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'AUDITOR')")
    @Operation(summary = "Check submission status", description = "Check the status of a submitted authority report")
    fun getSubmissionStatus(@PathVariable submissionId: String): ResponseEntity<Any> {
        return try {
            val submission = authoritySubmissionService.getSubmission(submissionId)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to submission,
                "message" to "Submission status retrieved successfully"
            ))

        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve submission status: ${e.message}"
            ))
        }
    }

    @PutMapping("/authority-report/submission-status/{submissionId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'AUDITOR')")
    @Operation(summary = "Update submission status", description = "Update the status of a submitted authority report")
    fun updateSubmissionStatus(
        @PathVariable submissionId: String,
        @RequestParam status: String,
        @RequestParam(required = false) authorityFeedback: String?,
        @RequestParam(required = false) authorityReference: String?
    ): ResponseEntity<Any> {
        return try {
            val newStatus = try {
                SubmissionStatus.valueOf(status)
            } catch (e: Exception) {
                return ResponseEntity.badRequest().body(mapOf(
                    "success" to false,
                    "message" to "Invalid status: $status"
                ))
            }

            val submission = authoritySubmissionService.updateStatus(
                submissionId = submissionId,
                newStatus = newStatus,
                authorityFeedback = authorityFeedback,
                authorityReference = authorityReference
            )

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to submission,
                "message" to "Submission status updated successfully"
            ))

        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to update submission status: ${e.message}"
            ))
        }
    }

    @GetMapping("/authority-report/authorities")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get available authorities", description = "Get list of supported regulatory authorities")
    fun getAvailableAuthorities(): ResponseEntity<Any> {
        return try {
            val authorities = authoritySubmissionService.getAvailableAuthorities()

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to authorities,
                "message" to "Authorities retrieved successfully"
            ))

        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve authorities: ${e.message}"
            ))
        }
    }

    @GetMapping("/authority-report/stats")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Get submission statistics", description = "Get submission statistics for the current user")
    fun getSubmissionStats(authentication: Authentication): ResponseEntity<Any> {
        return try {
            val userId = authentication.name
            val stats = authoritySubmissionService.getSubmissionStats(userId)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to stats,
                "message" to "Submission statistics retrieved successfully"
            ))

        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve submission statistics: ${e.message}"
            ))
        }
    }

    // ========== EU TRACES NT DDS ENDPOINTS ==========

    @GetMapping("/traces/test-connection")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Test TRACES NT connectivity", description = "Test connection to EU TRACES NT API")
    fun testTracesConnection(): ResponseEntity<Any> {
        return try {
            val result = tracesNtClient.testConnectivity()
            
            ResponseEntity.ok(mapOf(
                "success" to result.connected,
                "data" to result,
                "message" to result.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to test TRACES NT connection: ${e.message}"
            ))
        }
    }

    @GetMapping("/traces/dds/{workflowId}")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Get DDS data", description = "Get Due Diligence Statement data for a workflow")
    fun getDdsData(@PathVariable workflowId: String): ResponseEntity<Any> {
        return try {
            val dds = tracesNtDdsService.getDdsData(workflowId)
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to dds,
                "message" to "DDS data retrieved successfully"
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to retrieve DDS data: ${e.message}"
            ))
        }
    }

    @GetMapping("/traces/dds/{workflowId}/export")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Export DDS", description = "Export Due Diligence Statement as XML or JSON for TRACES NT submission")
    fun exportDds(
        @PathVariable workflowId: String,
        @RequestParam(defaultValue = "XML") format: String
    ): ResponseEntity<Any> {
        return try {
            val content: String
            val contentType: MediaType
            val filename: String

            when (format.uppercase()) {
                "XML" -> {
                    content = tracesNtDdsService.exportAsXml(workflowId)
                    contentType = MediaType.APPLICATION_XML
                    filename = "dds_${workflowId.takeLast(8)}.xml"
                }
                "JSON" -> {
                    content = tracesNtDdsService.exportAsJson(workflowId)
                    contentType = MediaType.APPLICATION_JSON
                    filename = "dds_${workflowId.takeLast(8)}.json"
                }
                else -> {
                    return ResponseEntity.badRequest().body(mapOf(
                        "success" to false,
                        "message" to "Invalid format. Supported formats: XML, JSON"
                    ))
                }
            }

            val headers = HttpHeaders()
            headers.contentType = contentType
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            
            ResponseEntity.ok()
                .headers(headers)
                .body(content)

        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to export DDS: ${e.message}"
            ))
        }
    }

    @GetMapping("/traces/dds/{workflowId}/geojson")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER')")
    @Operation(summary = "Export production plots GeoJSON", description = "Export production plot locations as GeoJSON for TRACES NT")
    fun exportGeoJson(@PathVariable workflowId: String): ResponseEntity<Any> {
        return try {
            val geoJson = tracesNtDdsService.exportGeoJson(workflowId)
            
            val headers = HttpHeaders()
            headers.contentType = MediaType.APPLICATION_JSON
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"production_plots_${workflowId.takeLast(8)}.geojson\"")
            
            ResponseEntity.ok()
                .headers(headers)
                .body(geoJson)

        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to export GeoJSON: ${e.message}"
            ))
        }
    }

    @PostMapping("/traces/dds/{workflowId}/submit")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Submit DDS to TRACES NT", description = "Submit Due Diligence Statement directly to EU TRACES NT API (if configured)")
    fun submitDdsToTraces(
        @PathVariable workflowId: String,
        authentication: Authentication
    ): ResponseEntity<Any> {
        return try {
            val userId = authentication.name
            
            // Generate DDS
            val dds = tracesNtDdsService.generateDds(workflowId)
            val xmlContent = tracesNtDdsService.exportAsXml(workflowId)
            
            // Submit to TRACES NT
            val result = tracesNtClient.submitDds(dds, xmlContent)
            
            if (result.success) {
                // Also record in our authority submission system
                result.tracesReferenceNumber?.let { tracesRef ->
                    try {
                        authoritySubmissionService.submitReport(
                            batchId = workflowId,
                            authorityCode = "TRACES_NT",
                            userId = userId,
                            notes = "TRACES Reference: $tracesRef, Verification: ${result.verificationNumber}"
                        )
                    } catch (e: Exception) {
                        // Log but don't fail the request
                    }
                }
                
                ResponseEntity.ok(mapOf(
                    "success" to true,
                    "data" to mapOf(
                        "tracesReferenceNumber" to result.tracesReferenceNumber,
                        "verificationNumber" to result.verificationNumber,
                        "status" to result.status,
                        "submittedAt" to result.submittedAt
                    ),
                    "message" to result.message
                ))
            } else {
                ResponseEntity.status(502).body(mapOf(
                    "success" to false,
                    "message" to result.message
                ))
            }

        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to e.message
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to submit DDS to TRACES NT: ${e.message}"
            ))
        }
    }

    @GetMapping("/traces/status/{referenceNumber}")
    @PreAuthorize("hasAnyRole('EXPORTER', 'SYSTEM_ADMIN')")
    @Operation(summary = "Check TRACES NT status", description = "Check the status of a DDS submission in TRACES NT")
    fun checkTracesStatus(@PathVariable referenceNumber: String): ResponseEntity<Any> {
        return try {
            val status = tracesNtClient.checkStatus(referenceNumber)
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to status,
                "message" to "Status retrieved successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.internalServerError().body(mapOf(
                "success" to false,
                "message" to "Failed to check TRACES NT status: ${e.message}"
            ))
        }
    }
}
