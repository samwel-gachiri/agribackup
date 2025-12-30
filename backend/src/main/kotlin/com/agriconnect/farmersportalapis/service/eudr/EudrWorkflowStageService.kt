package com.agriconnect.farmersportalapis.service.eudr

import com.agriconnect.farmersportalapis.domain.eudr.CountryRiskLevel
import com.agriconnect.farmersportalapis.domain.eudr.EudrComplianceStage
import com.agriconnect.farmersportalapis.domain.eudr.EudrRiskClassification
import com.agriconnect.farmersportalapis.domain.eudr.StageStatus
import com.agriconnect.farmersportalapis.domain.supplychain.CertificateStatus
import com.agriconnect.farmersportalapis.domain.supplychain.SupplyChainWorkflow
import com.agriconnect.farmersportalapis.domain.supplychain.WorkflowStage
import com.agriconnect.farmersportalapis.infrastructure.repositories.CountryRiskRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProductionUnitRepository
import com.agriconnect.farmersportalapis.repository.*
import com.agriconnect.farmersportalapis.service.common.RiskAssessmentService
import com.agriconnect.farmersportalapis.service.hedera.AsyncHederaService
import com.agriconnect.farmersportalapis.service.supplychain.DeforestationAlertService
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * EUDR Workflow Stage Service
 * 
 * This service manages the progression through EUDR compliance stages,
 * providing step-by-step guidance, validation, and automatic actions.
 * 
 * Key Features:
 * - Track current compliance stage for each workflow
 * - Validate stage requirements before progression
 * - Trigger automatic actions at each stage
 * - Provide user guidance for required actions
 * - Record stage transitions on blockchain
 */
@Service
@Transactional
class EudrWorkflowStageService(
    private val workflowRepository: SupplyChainWorkflowRepository,
    private val productionUnitRepository: ProductionUnitRepository,
    private val collectionEventRepository: WorkflowCollectionEventRepository,
    private val consolidationEventRepository: WorkflowConsolidationEventRepository,
    private val processingEventRepository: WorkflowProcessingEventRepository,
    private val shipmentEventRepository: WorkflowShipmentEventRepository,
    private val workflowProductionUnitRepository: WorkflowProductionUnitRepository,
    private val countryRiskRepository: CountryRiskRepository,
    private val deforestationAlertService: DeforestationAlertService,
    private val riskAssessmentService: RiskAssessmentService,
    private val asyncHederaService: AsyncHederaService,
    private val productionUnitService: com.agriconnect.farmersportalapis.service.supplychain.ProductionUnitService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // ===== STAGE PROGRESS TRACKING =====

    /**
     * Get current EUDR compliance stage for a workflow
     */
    fun getCurrentStage(workflowId: String): EudrStageStatusDto {
        val workflow = getWorkflow(workflowId)
        val eudrStage = determineEudrStage(workflow)
        
        return EudrStageStatusDto(
            workflowId = workflowId,
            currentStage = eudrStage,
            stageOrder = eudrStage.order,
            displayName = eudrStage.displayName,
            description = eudrStage.description,
            requiredActions = eudrStage.requiredActions,
            automatedActions = eudrStage.automatedActions,
            progress = calculateStageProgress(workflow, eudrStage),
            canAdvance = canAdvanceToNextStage(workflow, eudrStage),
            nextStage = eudrStage.nextStage?.let { EudrComplianceStage.fromName(it) },
            previousStage = eudrStage.previousStage?.let { EudrComplianceStage.fromName(it) },
            blockers = getStageBlockers(workflow, eudrStage)
        )
    }

    /**
     * Get full EUDR workflow progress overview
     */
    fun getWorkflowProgress(workflowId: String): EudrWorkflowProgressDto {
        val workflow = getWorkflow(workflowId)
        val currentStage = determineEudrStage(workflow)
        
        val stageStatuses = EudrComplianceStage.entries.map { stage ->
            StageProgressItem(
                stage = stage,
                order = stage.order,
                displayName = stage.displayName,
                status = getStageStatus(workflow, stage, currentStage),
                completionPercentage = if (stage.order < currentStage.order) 100 
                    else if (stage == currentStage) calculateStageProgress(workflow, stage)
                    else 0,
                isCurrentStage = stage == currentStage,
                isFutureStage = stage.order > currentStage.order
            )
        }
        
        // Calculate pre-compliance checklist data
        val linkedUnits = workflowProductionUnitRepository.findByWorkflowId(workflowId)
        val linkedProductionUnitsCount = linkedUnits.size
        val verifiedProductionUnitsCount = linkedUnits.count { it.geolocationVerified }
        val deforestationClearUnitsCount = linkedUnits.count { it.deforestationChecked && it.deforestationClear == true }
        
        // Get collection event count
        val collections = collectionEventRepository.findByWorkflowId(workflowId)
        val collectionEventCount = collections.size
        
        // Calculate risk assessment details
        val riskClassification = determineRiskClassification(workflow)
        val hasAlerts = linkedUnits.any { it.productionUnit.deforestationAlerts.isNotEmpty() }
        
        // Determine component risks for display
        val deforestationRisk = when {
            hasAlerts -> "HIGH"
            deforestationClearUnitsCount == linkedProductionUnitsCount && linkedProductionUnitsCount > 0 -> "LOW"
            deforestationClearUnitsCount > 0 -> "MEDIUM"
            else -> "N/A"
        }
        
        val countryRisk = when {
            collections.isEmpty() -> "N/A"
            else -> {
                // Check country of origin - for now use region as proxy
                val regions = collections.mapNotNull { it.productionUnit.administrativeRegion }.distinct()
                if (regions.isNotEmpty()) "LOW" else "N/A"  // Simplified - could be enhanced with country risk database
            }
        }
        
        val complexityRisk = when {
            collections.isEmpty() -> "N/A"
            collections.size <= 3 -> "LOW"
            collections.size <= 10 -> "MEDIUM"
            else -> "HIGH"
        }
        
        // Calculate risk score (0-100)
        val riskScore = when (riskClassification) {
            EudrRiskClassification.NEGLIGIBLE -> 10.0
            EudrRiskClassification.LOW -> 30.0
            EudrRiskClassification.STANDARD -> 50.0
            EudrRiskClassification.HIGH -> 80.0
        }
        
        // Determine if risk has been assessed (based on stage progression)
        val riskAssessedAt = if (currentStage.order >= EudrComplianceStage.RISK_ASSESSMENT.order && 
            linkedProductionUnitsCount > 0 && collectionEventCount > 0) {
            workflow.updatedAt  // Use workflow updated time as proxy for assessment time
        } else null
        
        return EudrWorkflowProgressDto(
            workflowId = workflowId,
            workflowName = workflow.workflowName,
            produceType = workflow.produceType,
            currentStage = currentStage,
            overallProgress = calculateOverallProgress(currentStage),
            stages = stageStatuses,
            estimatedCompletionDate = estimateCompletionDate(workflow, currentStage),
            riskClassification = riskClassification,
            certificateStatus = workflow.certificateStatus.name,
            
            // Pre-compliance checklist data
            linkedProductionUnits = linkedProductionUnitsCount,
            verifiedProductionUnits = verifiedProductionUnitsCount,
            deforestationClearUnits = deforestationClearUnitsCount,
            collectionEventCount = collectionEventCount,
            
            // Risk assessment details
            countryRisk = countryRisk,
            complexityRisk = complexityRisk,
            deforestationRisk = deforestationRisk,
            riskScore = riskScore,
            riskAssessedAt = riskAssessedAt,
            
            // DDS info (check if certificate exists)
            ddsReference = workflow.complianceCertificateNftId?.let { "DDS-${it.take(8).uppercase()}" },
            ddsGeneratedAt = workflow.certificateIssuedAt,
            
            // Blockchain info
            blockchainTransactionId = workflow.complianceCertificateTransactionId
        )
    }

    /**
     * Get detailed guidance for a specific stage
     */
    fun getStageGuidance(stage: EudrComplianceStage): EudrStageGuidanceDto {
        return EudrStageGuidanceDto(
            stage = stage,
            order = stage.order,
            displayName = stage.displayName,
            description = stage.description,
            requiredActions = stage.requiredActions.mapIndexed { index, action ->
                ActionItem(
                    id = "${stage.name}_REQ_$index",
                    action = action,
                    type = ActionType.REQUIRED,
                    helpText = getHelpTextForAction(stage, action)
                )
            },
            automatedActions = stage.automatedActions.mapIndexed { index, action ->
                ActionItem(
                    id = "${stage.name}_AUTO_$index",
                    action = action,
                    type = ActionType.AUTOMATED,
                    helpText = null
                )
            },
            nextSteps = getNextStepsGuidance(stage),
            eudrArticleReference = getEudrArticleReference(stage),
            tips = getTipsForStage(stage)
        )
    }

    // ===== STAGE ADVANCEMENT =====

    /**
     * Attempt to advance workflow to the next stage
     */
    fun advanceToNextStage(workflowId: String): StageAdvancementResult {
        val workflow = getWorkflow(workflowId)
        val currentStage = determineEudrStage(workflow)
        
        // Check if can advance
        val blockers = getStageBlockers(workflow, currentStage)
        if (blockers.isNotEmpty()) {
            return StageAdvancementResult(
                success = false,
                previousStage = currentStage,
                currentStage = currentStage,
                message = "Cannot advance due to incomplete requirements",
                blockers = blockers
            )
        }
        
        val nextStage = currentStage.nextStage?.let { EudrComplianceStage.fromName(it) }
            ?: return StageAdvancementResult(
                success = false,
                previousStage = currentStage,
                currentStage = currentStage,
                message = "Workflow is already at the final stage",
                blockers = emptyList()
            )
        
        // Execute automatic actions for stage completion
        executeStageCompletionActions(workflow, currentStage)
        
        // Update workflow stage tracking
        updateWorkflowStage(workflow, nextStage)
        
        // Execute automatic actions for new stage entry
        executeStageEntryActions(workflow, nextStage)
        
        logger.info("Workflow {} advanced from {} to {}", workflowId, currentStage.name, nextStage.name)
        
        return StageAdvancementResult(
            success = true,
            previousStage = currentStage,
            currentStage = nextStage,
            message = "Successfully advanced to ${nextStage.displayName}",
            blockers = emptyList()
        )
    }

    /**
     * Move back to previous stage (for corrections)
     */
    fun revertToPreviousStage(workflowId: String, reason: String): StageAdvancementResult {
        val workflow = getWorkflow(workflowId)
        val currentStage = determineEudrStage(workflow)
        
        val previousStage = currentStage.previousStage?.let { EudrComplianceStage.fromName(it) }
            ?: return StageAdvancementResult(
                success = false,
                previousStage = currentStage,
                currentStage = currentStage,
                message = "Cannot revert from the first stage",
                blockers = emptyList()
            )
        
        // Update workflow stage tracking
        updateWorkflowStage(workflow, previousStage)
        
        logger.info("Workflow {} reverted from {} to {} - Reason: {}", 
            workflowId, currentStage.name, previousStage.name, reason)
        
        return StageAdvancementResult(
            success = true,
            previousStage = currentStage,
            currentStage = previousStage,
            message = "Reverted to ${previousStage.displayName} for corrections",
            blockers = emptyList()
        )
    }

    // ===== STAGE VALIDATION =====

    /**
     * Validate if workflow meets requirements for a specific stage
     */
    fun validateStageRequirements(workflowId: String, stage: EudrComplianceStage): StageValidationResult {
        val workflow = getWorkflow(workflowId)
        val validations = mutableListOf<ValidationItem>()
        
        when (stage) {
            EudrComplianceStage.PRODUCTION_REGISTRATION -> {
                validations.addAll(validateProductionRegistration(workflow))
            }
            EudrComplianceStage.GEOLOCATION_VERIFICATION -> {
                validations.addAll(validateGeolocationVerification(workflow))
            }
            EudrComplianceStage.DEFORESTATION_CHECK -> {
                validations.addAll(validateDeforestationCheck(workflow))
            }
            EudrComplianceStage.COLLECTION_AGGREGATION -> {
                validations.addAll(validateCollectionAggregation(workflow))
            }
            EudrComplianceStage.PROCESSING -> {
                validations.addAll(validateProcessing(workflow))
            }
            EudrComplianceStage.RISK_ASSESSMENT -> {
                validations.addAll(validateRiskAssessment(workflow))
            }
            EudrComplianceStage.DUE_DILIGENCE_STATEMENT -> {
                validations.addAll(validateDueDiligenceStatement(workflow))
            }
            EudrComplianceStage.EXPORT_SHIPMENT -> {
                validations.addAll(validateExportShipment(workflow))
            }
            EudrComplianceStage.CUSTOMS_CLEARANCE -> {
                validations.addAll(validateCustomsClearance(workflow))
            }
            EudrComplianceStage.DELIVERY_COMPLETE -> {
                validations.addAll(validateDeliveryComplete(workflow))
            }
        }
        
        val allPassed = validations.all { it.passed }
        val failedCount = validations.count { !it.passed }
        
        return StageValidationResult(
            workflowId = workflowId,
            stage = stage,
            allRequirementsMet = allPassed,
            validations = validations,
            failedRequirements = failedCount,
            message = if (allPassed) "All requirements for ${stage.displayName} are met" 
                else "$failedCount requirement(s) not met for ${stage.displayName}"
        )
    }

    // ===== AUTOMATIC ACTIONS =====

    /**
     * Trigger automatic risk assessment
     */
    fun triggerRiskAssessment(workflowId: String): RiskAssessmentResultDto {
        val workflow = getWorkflow(workflowId)
        
        // Get all production units linked through collection events
        val collections = collectionEventRepository.findByWorkflowId(workflowId)
        val productionUnits = collections.map { it.productionUnit }.distinctBy { it.id }
        
        // Calculate risk factors
        val riskFactors = mutableListOf<RiskFactor>()
        
        // 1. Country Risk - use country codes from production units, fallback to exporter origin
        val countryRiskScore = calculateCountryRiskScore(productionUnits, workflow.exporter.originCountry)
        riskFactors.add(RiskFactor(
            type = "COUNTRY",
            description = "Country of origin risk based on EUDR benchmarking",
            score = countryRiskScore.score,
            details = countryRiskScore.details
        ))
        
        // 2. Deforestation Risk - check alerts across all production units
        val hasAlerts = productionUnits.any { it.deforestationAlerts.isNotEmpty() }
        val allReviewed = productionUnits.all { pu -> 
            pu.deforestationAlerts.isEmpty() || pu.deforestationAlerts.all { it.isReviewed }
        }
        val deforestationScore = when {
            hasAlerts && !allReviewed -> 80.0
            hasAlerts && allReviewed -> 30.0  // Had alerts but all reviewed
            productionUnits.isEmpty() -> 50.0  // Unknown - no data
            else -> 10.0  // Clean - no alerts
        }
        val deforestationLevel = when {
            deforestationScore >= 70 -> "HIGH"
            deforestationScore >= 40 -> "STANDARD"
            else -> "LOW"
        }
        riskFactors.add(RiskFactor(
            type = "DEFORESTATION",
            description = "Deforestation alert status from Global Forest Watch",
            score = deforestationScore,
            details = if (hasAlerts) "Deforestation alerts detected on ${productionUnits.count { it.deforestationAlerts.isNotEmpty() }} production unit(s)" 
                      else "No deforestation alerts"
        ))
        
        // 3. Supply Chain Complexity Risk
        val complexityScore = when {
            collections.isEmpty() -> 50.0
            collections.size <= 3 -> 15.0  // Simple supply chain
            collections.size <= 10 -> 35.0  // Moderate complexity
            collections.size <= 25 -> 55.0  // Complex
            else -> 75.0  // Very complex
        }
        val complexityLevel = when {
            complexityScore >= 60 -> "HIGH"
            complexityScore >= 30 -> "STANDARD"
            else -> "LOW"
        }
        riskFactors.add(RiskFactor(
            type = "SUPPLY_CHAIN",
            description = "Supply chain complexity based on number of sources",
            score = complexityScore,
            details = "${collections.size} collection event(s) from ${productionUnits.size} production unit(s)"
        ))
        
        // Calculate weighted overall score
        val overallScore = (countryRiskScore.score * 0.35) + (deforestationScore * 0.40) + (complexityScore * 0.25)
        
        val classification = when {
            overallScore < 20 -> EudrRiskClassification.NEGLIGIBLE
            overallScore < 40 -> EudrRiskClassification.LOW
            overallScore < 60 -> EudrRiskClassification.STANDARD
            else -> EudrRiskClassification.HIGH
        }
        
        logger.info("Risk assessment completed for workflow {}: {} (score: {})", workflowId, classification, overallScore)
        
        // *** SAVE RISK ASSESSMENT RESULTS TO WORKFLOW ***
        workflow.riskClassification = classification
        workflow.riskScore = overallScore
        workflow.riskAssessedAt = LocalDateTime.now()
        workflow.updatedAt = LocalDateTime.now()
        workflowRepository.save(workflow)
        
        logger.info("Risk assessment SAVED to workflow {} - classification: {}, score: {}", workflowId, classification, overallScore)
        
        return RiskAssessmentResultDto(
            workflowId = workflowId,
            overallScore = overallScore,
            classification = classification,
            riskFactors = riskFactors,
            assessedAt = workflow.riskAssessedAt!!,
            recommendedActions = getRecommendedActionsForRisk(classification)
        )
    }
    
    /**
     * Calculate country risk score based on production unit countries
     */
    private data class CountryRiskResult(val score: Double, val details: String, val level: String)
    
    private fun calculateCountryRiskScore(productionUnits: List<com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit>, fallbackCountry: String? = null): CountryRiskResult {
        if (productionUnits.isEmpty()) {
            // Try fallback country if no production units
            return if (fallbackCountry != null) {
                val code = inferCountryCode(fallbackCountry) ?: fallbackCountry
                lookupCountryRisk(listOf(code), "from exporter origin")
            } else {
                CountryRiskResult(50.0, "No production units - risk unknown", "STANDARD")
            }
        }

        // Priority 1: Use direct countryCode field OR detect on-the-fly from geometry
        val countryCodes = productionUnits.mapNotNull { pu ->
            // Try existing country code first, then detect from geometry
            productionUnitService.getOrDetectCountryCode(pu)
        }.distinct()
        
        if (countryCodes.isNotEmpty()) {
            return lookupCountryRisk(countryCodes, "from production unit country (detected from geometry)")
        }

        // Priority 2: Infer from administrative region
        val inferredCodes = productionUnits.mapNotNull { pu ->
            inferCountryCode(pu.administrativeRegion)
        }.distinct()
        if (inferredCodes.isNotEmpty()) {
            return lookupCountryRisk(inferredCodes, "inferred from administrative region")
        }

        // Priority 3: Use fallback exporter origin country
        if (fallbackCountry != null) {
            val fallbackCode = inferCountryCode(fallbackCountry) ?: fallbackCountry
            return lookupCountryRisk(listOf(fallbackCode), "from exporter origin (fallback)")
        }

        return CountryRiskResult(50.0, "Country could not be determined from production unit data", "STANDARD")
    }

    private fun lookupCountryRisk(countryCodes: List<String>, source: String): CountryRiskResult {
        val countryRisks = countryCodes.mapNotNull { code ->
            countryRiskRepository.findByCountryCode(code)
        }
        if (countryRisks.isEmpty()) {
            return CountryRiskResult(50.0, "Countries not in risk database: ${countryCodes.joinToString()} ($source)", "STANDARD")
        }
        val scores = countryRisks.map { cr ->
            when (cr.riskLevel) {
                CountryRiskLevel.LOW -> 15.0
                CountryRiskLevel.STANDARD -> 50.0
                CountryRiskLevel.HIGH -> 85.0
            }
        }
        val avgScore = scores.average()
        val level = when {
            avgScore >= 60 -> "HIGH"
            avgScore >= 35 -> "STANDARD"
            else -> "LOW"
        }
        val countryNames = countryRisks.map { "${it.countryName} (${it.riskLevel})" }.joinToString(", ")
        return CountryRiskResult(avgScore, "Countries: $countryNames ($source)", level)
    }
    
    /**
     * Infer country code from administrative region string
     */
    private fun inferCountryCode(region: String?): String? {
        if (region.isNullOrBlank()) return null
        
        val lowerRegion = region.lowercase()
        
        // Common country mappings based on region names
        return when {
            lowerRegion.contains("kenya") || lowerRegion.contains("nairobi") || 
            lowerRegion.contains("mombasa") || lowerRegion.contains("kiambu") ||
            lowerRegion.contains("nakuru") || lowerRegion.contains("kisumu") -> "KEN"
            
            lowerRegion.contains("ethiopia") || lowerRegion.contains("addis") ||
            lowerRegion.contains("oromia") || lowerRegion.contains("amhara") -> "ETH"
            
            lowerRegion.contains("uganda") || lowerRegion.contains("kampala") -> "UGA"
            lowerRegion.contains("tanzania") || lowerRegion.contains("dar es salaam") -> "TZA"
            lowerRegion.contains("rwanda") || lowerRegion.contains("kigali") -> "RWA"
            lowerRegion.contains("brazil") || lowerRegion.contains("brasil") -> "BRA"
            lowerRegion.contains("colombia") || lowerRegion.contains("bogota") -> "COL"
            lowerRegion.contains("indonesia") || lowerRegion.contains("jakarta") -> "IDN"
            lowerRegion.contains("vietnam") || lowerRegion.contains("hanoi") -> "VNM"
            lowerRegion.contains("ghana") || lowerRegion.contains("accra") -> "GHA"
            lowerRegion.contains("ivory coast") || lowerRegion.contains("côte d'ivoire") -> "CIV"
            lowerRegion.contains("peru") || lowerRegion.contains("lima") -> "PER"
            lowerRegion.contains("malaysia") || lowerRegion.contains("kuala lumpur") -> "MYS"
            lowerRegion.contains("india") || lowerRegion.contains("mumbai") || 
            lowerRegion.contains("delhi") -> "IND"
            lowerRegion.contains("mexico") || lowerRegion.contains("méxico") -> "MEX"
            lowerRegion.contains("guatemala") -> "GTM"
            lowerRegion.contains("honduras") -> "HND"
            lowerRegion.contains("costa rica") -> "CRI"
            lowerRegion.contains("nicaragua") -> "NIC"
            lowerRegion.contains("ecuador") || lowerRegion.contains("quito") -> "ECU"
            
            else -> null
        }
    }

    /**
     * Generate Due Diligence Statement
     */
    fun generateDueDiligenceStatement(workflowId: String): DueDiligenceStatementDto {
        val workflow = getWorkflow(workflowId)
        val ddsReference = "DDS-${UUID.randomUUID().toString().take(8).uppercase()}"
        
        // Compile all required information
        val collections = collectionEventRepository.findByWorkflowId(workflowId)
        val consolidations = consolidationEventRepository.findByWorkflowId(workflowId)
        val processings = processingEventRepository.findByWorkflowId(workflowId)
        
        val productionUnits = collections.map { it.productionUnit }.distinctBy { it.id }
        
        return DueDiligenceStatementDto(
            ddsReference = ddsReference,
            workflowId = workflowId,
            generatedAt = LocalDateTime.now(),
            exporter = workflow.exporter.userProfile.id ?: "",
            produceType = workflow.produceType,
            totalQuantityKg = workflow.totalQuantityKg,
            productionUnitsCount = productionUnits.size,
            collectionsCount = collections.size,
            consolidationsCount = consolidations.size,
            processingsCount = processings.size,
            countriesOfOrigin = productionUnits.mapNotNull { it.administrativeRegion }.distinct(),
            riskClassification = determineRiskClassification(workflow),
            status = "GENERATED",
            hederaTransactionId = null  // Will be set when recorded on blockchain
        )
    }

    /**
     * Generate Due Diligence Statement as PDF
     * Enhanced with QR codes, detailed geolocation data, and blockchain verification
     * per EUDR Article 4 requirements
     */
    fun generateDdsPdf(workflowId: String): ByteArray {
        val workflow = getWorkflow(workflowId)
        val dds = generateDueDiligenceStatement(workflowId)
        val collections = collectionEventRepository.findByWorkflowId(workflowId)
        val consolidations = consolidationEventRepository.findByWorkflowId(workflowId)
        val processings = processingEventRepository.findByWorkflowId(workflowId)
        val productionUnits = collections.map { it.productionUnit }.distinctBy { it.id }
        val linkedUnits = workflowProductionUnitRepository.findByWorkflowId(workflowId)
        
        val outputStream = ByteArrayOutputStream()
        val pdfWriter = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)
        
        val primaryColor = DeviceRgb(0, 100, 0)  // Green for agriculture theme
        val headerColor = DeviceRgb(34, 139, 34) // Forest green
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        
        // ===== COVER/HEADER SECTION =====
        document.add(Paragraph("EU Due Diligence Statement")
            .setFontSize(24f)
            .setBold()
            .setFontColor(primaryColor)
            .setTextAlignment(TextAlignment.CENTER))
        
        document.add(Paragraph("EUDR Regulation 2023/1115 Compliance Documentation")
            .setFontSize(12f)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10f))
        
        // Reference Information with QR Code
        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
            .useAllAvailableWidth()
        
        // Left side - Reference info
        val refCell = Cell()
        refCell.add(Paragraph("DDS Reference: ${dds.ddsReference}").setBold().setFontSize(12f))
        refCell.add(Paragraph("Generated: ${dds.generatedAt.format(dateFormatter)}").setFontSize(10f))
        refCell.add(Paragraph("Workflow ID: $workflowId").setFontSize(9f).setItalic())
        headerTable.addCell(refCell.setBorder(null))
        
        // Right side - QR Code for verification
        val qrCell = Cell()
        // Build verification URL - prioritize certificate transaction, then DDS transaction, then NFT token
        val verificationUrl = when {
            workflow.complianceCertificateTransactionId != null -> 
                "https://hashscan.io/testnet/transaction/${workflow.complianceCertificateTransactionId}"
            workflow.ddsHederaTransactionId != null -> 
                "https://hashscan.io/testnet/transaction/${workflow.ddsHederaTransactionId}"
            workflow.complianceCertificateNftId != null -> 
                "https://hashscan.io/testnet/token/${workflow.complianceCertificateNftId}"
            else -> 
                "https://hashscan.io/testnet" // Generic fallback - user can search
        }
        val qrCode = generateQRCode(verificationUrl, 80)
        if (qrCode != null) {
            qrCell.add(qrCode.setHorizontalAlignment(HorizontalAlignment.CENTER))
            qrCell.add(Paragraph("Scan to Verify").setFontSize(7f).setTextAlignment(TextAlignment.CENTER))
        }
        headerTable.addCell(qrCell.setBorder(null))
        document.add(headerTable)
        document.add(Paragraph().setMarginBottom(15f))
        
        // ===== SECTION 1: OPERATOR INFORMATION =====
        document.add(createSectionHeader("1. Operator Information", headerColor))
        val operatorTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f)))
            .useAllAvailableWidth()
        operatorTable.addCell(createLabelCell("Operator ID:"))
        operatorTable.addCell(createValueCell(workflow.exporter.userProfile.id ?: "N/A"))
        operatorTable.addCell(createLabelCell("Company Name:"))
        operatorTable.addCell(createValueCell(workflow.exporter.companyName ?: "N/A"))
        operatorTable.addCell(createLabelCell("Export License:"))
        operatorTable.addCell(createValueCell(workflow.exporter.licenseId ?: "N/A"))
        operatorTable.addCell(createLabelCell("Country:"))
//        operatorTable.addCell(createValueCell(workflow.exporter.originCountry ?: "N/A"))
        document.add(operatorTable)
        document.add(Paragraph().setMarginBottom(10f))
        
        // ===== SECTION 2: PRODUCT INFORMATION =====
        document.add(createSectionHeader("2. Product Information", headerColor))
        val productTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f)))
            .useAllAvailableWidth()
        productTable.addCell(createLabelCell("Product Type:"))
        productTable.addCell(createValueCell(workflow.produceType))
        productTable.addCell(createLabelCell("Total Quantity:"))
        productTable.addCell(createValueCell("${workflow.totalQuantityKg} kg"))
        productTable.addCell(createLabelCell("Countries of Origin:"))
        productTable.addCell(createValueCell(dds.countriesOfOrigin.joinToString(", ").ifEmpty { workflow.exporter.originCountry ?: "N/A" }))
        productTable.addCell(createLabelCell("HS Code:"))
        productTable.addCell(createValueCell(getHsCodeForProduct(workflow.produceType)))
        document.add(productTable)
        document.add(Paragraph().setMarginBottom(10f))
        
        // ===== SECTION 3: PRODUCTION UNITS - DETAILED GEOLOCATION (EUDR Article 9) =====
        document.add(createSectionHeader("3. Production Units - Geolocation Data", headerColor))
        document.add(Paragraph("Per EUDR Article 9(1)(a), geolocation of all plots of land where the commodity was produced:")
            .setFontSize(9f).setItalic().setMarginBottom(5f))
        
        if (productionUnits.isNotEmpty()) {
            val geoTable = Table(UnitValue.createPercentArray(floatArrayOf(15f, 25f, 30f, 15f, 15f)))
                .useAllAvailableWidth()
            
            // Header row
            geoTable.addHeaderCell(createHeaderCell("Unit ID"))
            geoTable.addHeaderCell(createHeaderCell("Unit Name"))
            geoTable.addHeaderCell(createHeaderCell("WGS84 Coordinates"))
            geoTable.addHeaderCell(createHeaderCell("Area (ha)"))
            geoTable.addHeaderCell(createHeaderCell("Verified"))
            
            productionUnits.forEach { unit ->
                val linkedUnit = linkedUnits.find { it.productionUnit.id == unit.id }
                geoTable.addCell(createValueCell(unit.id.take(8) + "..."))
                geoTable.addCell(createValueCell(unit.unitName ?: "N/A"))
                geoTable.addCell(createValueCell(unit.wgs84Coordinates ?: "N/A"))
                geoTable.addCell(createValueCell(unit.areaHectares?.toString() ?: "N/A"))
                geoTable.addCell(createValueCell(if (linkedUnit?.geolocationVerified == true) "✓" else "✗"))
            }
            document.add(geoTable)
        } else {
            document.add(Paragraph("No production units linked").setItalic())
        }
        document.add(Paragraph().setMarginBottom(10f))
        
        // ===== SECTION 4: DEFORESTATION VERIFICATION =====
        document.add(createSectionHeader("4. Deforestation-Free Verification", headerColor))
        val geoVerified = linkedUnits.count { it.geolocationVerified }
        val deforestationClear = linkedUnits.count { it.deforestationClear == true }
        val deforestationChecked = linkedUnits.count { it.deforestationChecked }
        
        val deforestTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .useAllAvailableWidth()
        deforestTable.addCell(createLabelCell("Plots Geo-verified:"))
        deforestTable.addCell(createValueCell("$geoVerified / ${linkedUnits.size} (${if (linkedUnits.isNotEmpty()) (geoVerified * 100 / linkedUnits.size) else 0}%)"))
        deforestTable.addCell(createLabelCell("Deforestation Checks Performed:"))
        deforestTable.addCell(createValueCell("$deforestationChecked / ${linkedUnits.size}"))
        deforestTable.addCell(createLabelCell("Deforestation-Free Confirmed:"))
        deforestTable.addCell(createValueCell("$deforestationClear / ${linkedUnits.size}"))
        deforestTable.addCell(createLabelCell("Cutoff Date Compliance:"))
        deforestTable.addCell(createValueCell("December 31, 2020"))
        document.add(deforestTable)
        document.add(Paragraph().setMarginBottom(10f))
        
        // ===== SECTION 5: SUPPLY CHAIN TRACEABILITY =====
        document.add(createSectionHeader("5. Supply Chain Traceability", headerColor))
        
        // Summary table
        val traceTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
            .useAllAvailableWidth()
        traceTable.addCell(createLabelCell("Collection Events:"))
        traceTable.addCell(createValueCell("${dds.collectionsCount}"))
        traceTable.addCell(createLabelCell("Consolidation Events:"))
        traceTable.addCell(createValueCell("${dds.consolidationsCount}"))
        traceTable.addCell(createLabelCell("Processing Events:"))
        traceTable.addCell(createValueCell("${dds.processingsCount}"))
        document.add(traceTable)
        
        // Detailed supply chain events with blockchain transaction IDs
        if (collections.isNotEmpty()) {
            document.add(Paragraph("Collection Events with Blockchain Verification:").setBold().setFontSize(10f).setMarginTop(10f))
            val collTable = Table(UnitValue.createPercentArray(floatArrayOf(15f, 25f, 15f, 25f, 20f)))
                .useAllAvailableWidth()
            collTable.addHeaderCell(createHeaderCell("Date"))
            collTable.addHeaderCell(createHeaderCell("From"))
            collTable.addHeaderCell(createHeaderCell("Qty (kg)"))
            collTable.addHeaderCell(createHeaderCell("To"))
            collTable.addHeaderCell(createHeaderCell("Hedera TX"))
            
            collections.take(10).forEach { event ->
                collTable.addCell(createValueCell(event.collectionDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                collTable.addCell(createValueCell(event.farmer.userProfile.fullName ?: "Farmer"))
                collTable.addCell(createValueCell(event.quantityCollectedKg.toString()))
                collTable.addCell(createValueCell(event.collectorSupplier.supplierName))
                // Show full transaction ID or pending status
                val txCell = if (event.hederaTransactionId != null) {
                    createValueCell(event.hederaTransactionId!!).setFontSize(7f)
                } else {
                    createValueCell("Pending").setItalic()
                }
                collTable.addCell(txCell)
            }
            document.add(collTable)
            if (collections.size > 10) {
                document.add(Paragraph("... and ${collections.size - 10} more collection events").setFontSize(8f).setItalic())
            }
        }
        
        // Add Consolidation Events if present
        if (consolidations.isNotEmpty()) {
            document.add(Paragraph("Consolidation Events (Supplier → Supplier Transfers):").setBold().setFontSize(10f).setMarginTop(10f))
            val consTable = Table(UnitValue.createPercentArray(floatArrayOf(15f, 25f, 15f, 25f, 20f)))
                .useAllAvailableWidth()
            consTable.addHeaderCell(createHeaderCell("Date"))
            consTable.addHeaderCell(createHeaderCell("From"))
            consTable.addHeaderCell(createHeaderCell("Qty (kg)"))
            consTable.addHeaderCell(createHeaderCell("To"))
            consTable.addHeaderCell(createHeaderCell("Hedera TX"))
            
            consolidations.take(10).forEach { event ->
                consTable.addCell(createValueCell(event.consolidationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                consTable.addCell(createValueCell(event.sourceSupplier.supplierName))
                consTable.addCell(createValueCell(event.quantitySentKg.toString()))
                consTable.addCell(createValueCell(event.targetSupplier.supplierName))
                val txCell = if (event.hederaTransactionId != null) {
                    createValueCell(event.hederaTransactionId!!).setFontSize(7f)
                } else {
                    createValueCell("Pending").setItalic()
                }
                consTable.addCell(txCell)
            }
            document.add(consTable)
        }
        
        // Add Processing Events if present
        if (processings.isNotEmpty()) {
            document.add(Paragraph("Processing Events:").setBold().setFontSize(10f).setMarginTop(10f))
            val procTable = Table(UnitValue.createPercentArray(floatArrayOf(15f, 25f, 15f, 15f, 30f)))
                .useAllAvailableWidth()
            procTable.addHeaderCell(createHeaderCell("Date"))
            procTable.addHeaderCell(createHeaderCell("Processor"))
            procTable.addHeaderCell(createHeaderCell("Input (kg)"))
            procTable.addHeaderCell(createHeaderCell("Output (kg)"))
            procTable.addHeaderCell(createHeaderCell("Hedera TX"))
            
            processings.take(10).forEach { event ->
                procTable.addCell(createValueCell(event.processingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                procTable.addCell(createValueCell(event.processorSupplier.supplierName))
                procTable.addCell(createValueCell(event.quantityProcessedKg.toString()))
                procTable.addCell(createValueCell(event.outputQuantityKg?.toString() ?: "N/A"))
                val txCell = if (event.hederaTransactionId != null) {
                    createValueCell(event.hederaTransactionId!!).setFontSize(7f)
                } else {
                    createValueCell("Pending").setItalic()
                }
                procTable.addCell(txCell)
            }
            document.add(procTable)
        }
        document.add(Paragraph().setMarginBottom(10f))
        
        // ===== SECTION 6: RISK ASSESSMENT =====
        document.add(createSectionHeader("6. Risk Assessment", headerColor))
        val riskColor = when (dds.riskClassification) {
            EudrRiskClassification.NEGLIGIBLE -> DeviceRgb(0, 128, 0)
            EudrRiskClassification.LOW -> DeviceRgb(34, 139, 34)
            EudrRiskClassification.STANDARD -> DeviceRgb(255, 165, 0)
            EudrRiskClassification.HIGH -> DeviceRgb(255, 69, 0)
        }
        val riskTable = Table(UnitValue.createPercentArray(floatArrayOf(30f, 70f)))
            .useAllAvailableWidth()
        riskTable.addCell(createLabelCell("Risk Classification:"))
        riskTable.addCell(Cell().add(Paragraph(dds.riskClassification.name).setFontColor(riskColor).setBold()))
        riskTable.addCell(createLabelCell("Assessment Date:"))
        riskTable.addCell(createValueCell(workflow.riskAssessedAt?.format(dateFormatter) ?: "Pending"))
        riskTable.addCell(createLabelCell("Due Diligence Level:"))
        riskTable.addCell(createValueCell(dds.riskClassification.requiredDueDiligence))
        document.add(riskTable)
        document.add(Paragraph().setMarginBottom(10f))
        
        // ===== SECTION 7: COMPLIANCE DECLARATION =====
        document.add(createSectionHeader("7. Compliance Declaration", headerColor))
        document.add(Paragraph("""
            I, the undersigned operator, hereby declare that:
            
            ✓ The relevant product is deforestation-free (Article 3(a))
            ✓ The product has been produced in accordance with relevant legislation of the country of production (Article 3(b))
            ✓ The product is covered by this Due Diligence Statement (Article 3(c))
            ✓ Due diligence has been exercised in accordance with Article 8 of EU Regulation 2023/1115
            ✓ Information collection has been completed for all plots of land (Article 9(1))
            ✓ Risk assessment has been performed (Article 10)
            ✓ Risk mitigation measures have been applied where necessary (Article 11)
            
            The information contained in this statement is accurate and complete to the best of my knowledge.
        """.trimIndent())
            .setFontSize(9f)
            .setMarginBottom(15f))
        
        // ===== SECTION 8: BLOCKCHAIN VERIFICATION =====
        document.add(createSectionHeader("8. Blockchain Verification (Hedera Hashgraph)", headerColor))
        
        // Certificate status banner
        val certStatus = when {
            workflow.complianceCertificateNftId != null -> "✓ CERTIFICATE ISSUED"
            workflow.certificateStatus == CertificateStatus.PENDING_VERIFICATION -> "⏳ CERTIFICATE PENDING"
            else -> "⏳ CERTIFICATE NOT YET ISSUED"
        }
        val statusColor = if (workflow.complianceCertificateNftId != null) DeviceRgb(0, 128, 0) else DeviceRgb(255, 165, 0)
        document.add(Paragraph(certStatus)
            .setFontColor(statusColor)
            .setBold()
            .setFontSize(11f)
            .setMarginBottom(10f))
        
        val blockchainTable = Table(UnitValue.createPercentArray(floatArrayOf(35f, 65f)))
            .useAllAvailableWidth()
        
        // Certificate NFT with clickable link
        blockchainTable.addCell(createLabelCell("Certificate NFT Token:"))
        if (workflow.complianceCertificateNftId != null) {
            val nftUrl = "https://hashscan.io/testnet/token/${workflow.complianceCertificateNftId}"
            blockchainTable.addCell(createValueCell("${workflow.complianceCertificateNftId}\n→ $nftUrl").setFontSize(8f))
        } else {
            blockchainTable.addCell(createValueCell("Will be created when certificate is issued").setItalic())
        }
        
        blockchainTable.addCell(createLabelCell("Certificate Serial #:"))
        blockchainTable.addCell(createValueCell(workflow.complianceCertificateSerialNumber?.toString() ?: "Pending"))
        
        // Certificate TX with full link
        blockchainTable.addCell(createLabelCell("Certificate TX:"))
        if (workflow.complianceCertificateTransactionId != null) {
            val certTxUrl = "https://hashscan.io/testnet/transaction/${workflow.complianceCertificateTransactionId}"
            blockchainTable.addCell(createValueCell("${workflow.complianceCertificateTransactionId}\n→ $certTxUrl").setFontSize(8f))
        } else {
            blockchainTable.addCell(createValueCell("Pending").setItalic())
        }
        
        // DDS TX with full link
        blockchainTable.addCell(createLabelCell("DDS Consensus TX:"))
        if (workflow.ddsHederaTransactionId != null) {
            val ddsTxUrl = "https://hashscan.io/testnet/transaction/${workflow.ddsHederaTransactionId}"
            blockchainTable.addCell(createValueCell("${workflow.ddsHederaTransactionId}\n→ $ddsTxUrl").setFontSize(8f))
        } else {
            blockchainTable.addCell(createValueCell("Pending").setItalic())
        }
        
        document.add(blockchainTable)
        
        // Verification instructions
        document.add(Paragraph().setMarginTop(10f))
        document.add(Paragraph("HOW TO VERIFY THIS DOCUMENT:").setBold().setFontSize(9f))
        document.add(Paragraph("""
            1. Scan the QR code above OR visit hashscan.io/testnet
            2. Search for the Transaction ID or Token ID listed above
            3. Verify the transaction timestamp matches the document date
            4. Confirm the memo/metadata matches this DDS Reference: ${dds.ddsReference}
            5. For the NFT certificate, verify ownership matches the exporter's Hedera account
            
            All Hedera transactions are immutable and publicly verifiable.
            Network: Hedera Testnet (use Mainnet for production)
        """.trimIndent())
            .setFontSize(8f)
            .setMarginTop(5f))
        
        // ===== FOOTER =====
        document.add(Paragraph().setMarginTop(30f))
        document.add(Paragraph("─".repeat(80)).setTextAlignment(TextAlignment.CENTER).setFontSize(8f))
        document.add(Paragraph("Generated by AgriConnect EUDR Compliance System")
            .setFontSize(8f)
            .setTextAlignment(TextAlignment.CENTER)
            .setItalic())
        document.add(Paragraph("Document ID: ${dds.ddsReference} | Workflow: $workflowId | Generated: ${dds.generatedAt.format(dateFormatter)}")
            .setFontSize(7f)
            .setTextAlignment(TextAlignment.CENTER))
        
        document.close()
        return outputStream.toByteArray()
    }
    
    /**
     * Generate QR Code for blockchain verification
     */
    private fun generateQRCode(content: String, size: Int = 100): Image? {
        return try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size)
            val outputStream = ByteArrayOutputStream()
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
            val imageData = ImageDataFactory.create(outputStream.toByteArray())
            Image(imageData)
        } catch (e: Exception) {
            logger.warn("Failed to generate QR code: ${e.message}")
            null
        }
    }
    
    private fun createHeaderCell(text: String): Cell {
        return Cell().add(Paragraph(text).setBold().setFontSize(9f))
            .setBackgroundColor(DeviceRgb(200, 230, 200))
    }
    
    private fun createSectionHeader(title: String, color: DeviceRgb): Paragraph {
        return Paragraph(title)
            .setFontSize(14f)
            .setBold()
            .setFontColor(color)
            .setMarginTop(15f)
            .setMarginBottom(8f)
    }
    
    private fun createLabelCell(text: String): Cell {
        return Cell().add(Paragraph(text).setBold().setFontSize(10f))
            .setBackgroundColor(DeviceRgb(240, 240, 240))
    }
    
    private fun createValueCell(text: String): Cell {
        return Cell().add(Paragraph(text).setFontSize(10f))
    }

    /**
     * Get HS Code for a produce type based on EUDR commodity classification
     * Reference: EU Regulation 2023/1115 Annex I
     */
    private fun getHsCodeForProduct(produceType: String): String {
        val normalizedType = produceType.lowercase().trim()
        return when {
            normalizedType.contains("coffee") -> "0901 - Coffee"
            normalizedType.contains("cocoa") || normalizedType.contains("cacao") -> "1801 - Cocoa beans"
            normalizedType.contains("palm") && normalizedType.contains("oil") -> "1511 - Palm oil"
            normalizedType.contains("soy") || normalizedType.contains("soya") -> "1201 - Soya beans"
            normalizedType.contains("rubber") -> "4001 - Natural rubber"
            normalizedType.contains("cattle") || normalizedType.contains("beef") -> "0102 - Live cattle / 0201 - Beef"
            normalizedType.contains("wood") || normalizedType.contains("timber") -> "44 - Wood and articles of wood"
            normalizedType.contains("charcoal") -> "4402 - Wood charcoal"
            normalizedType.contains("leather") -> "41 - Raw hides and skins"
            normalizedType.contains("chocolate") -> "1806 - Chocolate"
            normalizedType.contains("furniture") -> "9403 - Furniture"
            normalizedType.contains("paper") || normalizedType.contains("pulp") -> "47/48 - Paper pulp / Paper"
            normalizedType.contains("printed") -> "49 - Printed matter"
            else -> "N/A (Specify HS code)"
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    private fun getWorkflow(workflowId: String): SupplyChainWorkflow {
        return workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found: $workflowId") }
    }

    private fun determineEudrStage(workflow: SupplyChainWorkflow): EudrComplianceStage {
        // Use the explicitly tracked EUDR compliance stage
        return workflow.eudrComplianceStage
    }

    private fun calculateStageProgress(workflow: SupplyChainWorkflow, stage: EudrComplianceStage): Int {
        return when (stage) {
            EudrComplianceStage.PRODUCTION_REGISTRATION -> {
                // Stage 1: Progress based on linked production units
                val linkedUnits = workflowProductionUnitRepository.findByWorkflowId(workflow.id)
                if (linkedUnits.isEmpty()) 0
                else 100 // Any linked units means ready to proceed
            }
            EudrComplianceStage.GEOLOCATION_VERIFICATION -> {
                // Stage 2: Progress based on verified geolocation
                val linkedUnits = workflowProductionUnitRepository.findByWorkflowId(workflow.id)
                if (linkedUnits.isEmpty()) 0
                else {
                    val verified = linkedUnits.count { it.geolocationVerified }
                    (verified * 100) / linkedUnits.size
                }
            }
            EudrComplianceStage.DEFORESTATION_CHECK -> {
                // Stage 3: Progress based on deforestation checks
                val linkedUnits = workflowProductionUnitRepository.findByWorkflowId(workflow.id)
                if (linkedUnits.isEmpty()) 0
                else {
                    val checked = linkedUnits.count { it.deforestationChecked && it.deforestationClear == true }
                    (checked * 100) / linkedUnits.size
                }
            }
            EudrComplianceStage.COLLECTION_AGGREGATION -> {
                // Stage 4: Progress based on collection events
                val collections = collectionEventRepository.findByWorkflowId(workflow.id)
                if (collections.isEmpty()) 0 else 100
            }
            EudrComplianceStage.PROCESSING -> {
                // Stage 5: Progress based on consolidation OR collection (processing is optional)
                val consolidations = consolidationEventRepository.findByWorkflowId(workflow.id)
                val collections = collectionEventRepository.findByWorkflowId(workflow.id)
                if (consolidations.isEmpty() && collections.isEmpty()) 0 else 100
            }
            EudrComplianceStage.RISK_ASSESSMENT -> {
                // Stage 6: Progress based on risk assessment completion
                // Risk assessment is ready when we have collections (processing is optional)
                val collections = collectionEventRepository.findByWorkflowId(workflow.id)
                val hasRiskAssessment = workflow.riskClassification != null || workflow.riskAssessedAt != null
                when {
                    collections.isEmpty() -> 0
                    hasRiskAssessment -> 100
                    else -> 50  // Has data but needs assessment
                }
            }
            EudrComplianceStage.DUE_DILIGENCE_STATEMENT -> {
                // Stage 7: Progress based on certificate status
                when (workflow.certificateStatus) {
                    CertificateStatus.NOT_CREATED -> 0
                    CertificateStatus.PENDING_VERIFICATION -> 50
                    CertificateStatus.COMPLIANT -> 100
                    else -> 100
                }
            }
            EudrComplianceStage.EXPORT_SHIPMENT -> {
                // Stage 8: Progress based on shipment
                val shipments = shipmentEventRepository.findByWorkflowId(workflow.id)
                if (shipments.isEmpty()) 0 else 100
            }
            EudrComplianceStage.CUSTOMS_CLEARANCE -> {
                // Stage 9: Progress based on certificate transfer
                when (workflow.certificateStatus) {
                    CertificateStatus.IN_TRANSIT -> 50
                    CertificateStatus.TRANSFERRED_TO_IMPORTER -> 75
                    CertificateStatus.CUSTOMS_VERIFIED -> 100
                    else -> 0
                }
            }
            EudrComplianceStage.DELIVERY_COMPLETE -> {
                // Stage 10: Final delivery status
                if (workflow.certificateStatus == CertificateStatus.DELIVERED) 100 else 50
            }
        }
    }

    private fun canAdvanceToNextStage(workflow: SupplyChainWorkflow, stage: EudrComplianceStage): Boolean {
        return getStageBlockers(workflow, stage).isEmpty()
    }

    private fun getStageBlockers(workflow: SupplyChainWorkflow, stage: EudrComplianceStage): List<String> {
        val blockers = mutableListOf<String>()
        
        when (stage) {
            EudrComplianceStage.PRODUCTION_REGISTRATION -> {
                // Stage 1: Check for linked production units (via WorkflowProductionUnit)
                val linkedUnits = workflowProductionUnitRepository.findByWorkflowId(workflow.id)
                if (linkedUnits.isEmpty()) {
                    blockers.add("No production units linked to this workflow. Link at least one production unit to proceed.")
                }
            }
            EudrComplianceStage.GEOLOCATION_VERIFICATION -> {
                // Stage 2: Check if all linked production units have verified geolocation
                val linkedUnits = workflowProductionUnitRepository.findByWorkflowId(workflow.id)
                val unverified = linkedUnits.filter { !it.geolocationVerified }
                if (unverified.isNotEmpty()) {
                    blockers.add("${unverified.size} production unit(s) need geolocation verification")
                }
            }
            EudrComplianceStage.DEFORESTATION_CHECK -> {
                // Stage 3: Check for deforestation alerts on linked production units
                val linkedUnits = workflowProductionUnitRepository.findByWorkflowId(workflow.id)
                linkedUnits.forEach { link ->
                    if (link.productionUnit.deforestationAlerts.isNotEmpty()) {
                        blockers.add("Production unit ${link.productionUnit.unitName} has active deforestation alerts")
                    }
                    if (!link.deforestationChecked) {
                        blockers.add("Deforestation check not completed for ${link.productionUnit.unitName}")
                    }
                }
            }
            EudrComplianceStage.COLLECTION_AGGREGATION -> {
                // Stage 4: Check for collection events
                val collections = collectionEventRepository.findByWorkflowId(workflow.id)
                if (collections.isEmpty()) {
                    blockers.add("No collection events recorded. Record at least one collection to proceed.")
                }
            }
            EudrComplianceStage.PROCESSING -> {
                // Stage 5: Check for consolidation OR collection events (flexible supply chain)
                // Consolidation is optional - direct farmer-to-processor flow is also valid
                val consolidations = consolidationEventRepository.findByWorkflowId(workflow.id)
                val collections = collectionEventRepository.findByWorkflowId(workflow.id)
                if (consolidations.isEmpty() && collections.isEmpty()) {
                    blockers.add("No collection or consolidation events recorded. Record collections or send produce to processors to proceed.")
                }
            }
            EudrComplianceStage.RISK_ASSESSMENT -> {
                // Stage 6: Risk assessment stage - processing is OPTIONAL
                // Some supply chains export raw produce without processing
                // The key requirement is that we have collection data to assess risk
                val collections = collectionEventRepository.findByWorkflowId(workflow.id)
                if (collections.isEmpty()) {
                    blockers.add("No collection events recorded. At least one collection is required for risk assessment.")
                }
                // Note: Processing events are optional - exporters can proceed with raw produce
            }
            EudrComplianceStage.DUE_DILIGENCE_STATEMENT -> {
                // Stage 7: Check that risk assessment has been completed AND certificate status
                // Risk assessment MUST be completed before generating DDS
                if (workflow.riskAssessedAt == null || workflow.riskClassification == null) {
                    blockers.add("Risk assessment has not been completed. Run risk assessment before generating the Due Diligence Statement.")
                }
                if (workflow.certificateStatus == CertificateStatus.NOT_CREATED) {
                    blockers.add("Compliance certificate not yet created")
                }
            }
            EudrComplianceStage.EXPORT_SHIPMENT -> {
                // Stage 8: Check for shipment events
                val shipments = shipmentEventRepository.findByWorkflowId(workflow.id)
                if (shipments.isEmpty()) {
                    blockers.add("No shipment created for this workflow")
                }
            }
            EudrComplianceStage.CUSTOMS_CLEARANCE -> {
                // Stage 9: Check certificate status
                if (workflow.certificateStatus != CertificateStatus.IN_TRANSIT && 
                    workflow.certificateStatus != CertificateStatus.TRANSFERRED_TO_IMPORTER) {
                    blockers.add("Certificate must be transferred to importer for customs clearance")
                }
            }
            EudrComplianceStage.DELIVERY_COMPLETE -> {
                // Stage 10: Final delivery
                if (workflow.certificateStatus != CertificateStatus.CUSTOMS_VERIFIED) {
                    blockers.add("Customs verification must be completed before marking delivery complete")
                }
            }
        }
        
        return blockers
    }

    private fun getStageStatus(
        workflow: SupplyChainWorkflow, 
        stage: EudrComplianceStage, 
        currentStage: EudrComplianceStage
    ): StageStatus {
        return when {
            stage.order < currentStage.order -> StageStatus.COMPLETED
            stage == currentStage -> StageStatus.IN_PROGRESS
            else -> StageStatus.NOT_STARTED
        }
    }

    private fun calculateOverallProgress(currentStage: EudrComplianceStage): Int {
        val totalStages = EudrComplianceStage.entries.size
        return ((currentStage.order - 1) * 100) / totalStages
    }

    private fun estimateCompletionDate(
        workflow: SupplyChainWorkflow, 
        currentStage: EudrComplianceStage
    ): LocalDateTime {
        val remainingStages = EudrComplianceStage.entries.size - currentStage.order
        // Estimate 2 days per remaining stage
        return LocalDateTime.now().plusDays((remainingStages * 2).toLong())
    }

    private fun determineRiskClassification(workflow: SupplyChainWorkflow): EudrRiskClassification {
        // Simplified risk classification
        val collections = collectionEventRepository.findByWorkflowId(workflow.id)
        val hasAlerts = collections.any { 
            it.productionUnit.deforestationAlerts.isNotEmpty()
        }
        
        return when {
            hasAlerts -> EudrRiskClassification.HIGH
            collections.isEmpty() -> EudrRiskClassification.STANDARD
            collections.all { it.productionUnit.lastVerifiedAt != null } -> EudrRiskClassification.LOW
            else -> EudrRiskClassification.STANDARD
        }
    }

    private fun updateWorkflowStage(workflow: SupplyChainWorkflow, newStage: EudrComplianceStage) {
        // Update the EUDR compliance stage
        workflow.eudrComplianceStage = newStage
        workflow.stageUpdatedAt = LocalDateTime.now()
        
        // Map EUDR stage to workflow stage for legacy compatibility
        val workflowStage = when (newStage) {
            EudrComplianceStage.PRODUCTION_REGISTRATION,
            EudrComplianceStage.GEOLOCATION_VERIFICATION,
            EudrComplianceStage.DEFORESTATION_CHECK -> WorkflowStage.COLLECTION
            EudrComplianceStage.COLLECTION_AGGREGATION -> WorkflowStage.CONSOLIDATION
            EudrComplianceStage.PROCESSING,
            EudrComplianceStage.RISK_ASSESSMENT -> WorkflowStage.PROCESSING
            EudrComplianceStage.DUE_DILIGENCE_STATEMENT,
            EudrComplianceStage.EXPORT_SHIPMENT -> WorkflowStage.SHIPMENT
            EudrComplianceStage.CUSTOMS_CLEARANCE,
            EudrComplianceStage.DELIVERY_COMPLETE -> WorkflowStage.COMPLETED
        }
        
        workflow.currentStage = workflowStage
        workflow.updatedAt = LocalDateTime.now()
        workflowRepository.save(workflow)
    }

    private fun executeStageCompletionActions(workflow: SupplyChainWorkflow, stage: EudrComplianceStage) {
        // Execute any automatic actions when completing a stage
        when (stage) {
            EudrComplianceStage.DEFORESTATION_CHECK -> {
                // Auto-trigger risk assessment
                triggerRiskAssessment(workflow.id)
            }
            EudrComplianceStage.RISK_ASSESSMENT -> {
                // Prepare for DDS generation
                logger.info("Risk assessment completed for workflow {}", workflow.id)
            }
            else -> { /* No automatic actions */ }
        }
    }

    private fun executeStageEntryActions(workflow: SupplyChainWorkflow, stage: EudrComplianceStage) {
        // Execute any automatic actions when entering a new stage
        when (stage) {
            EudrComplianceStage.RISK_ASSESSMENT -> {
                // Auto-trigger risk assessment
                triggerRiskAssessment(workflow.id)
            }
            EudrComplianceStage.DUE_DILIGENCE_STATEMENT -> {
                // Record stage entry on blockchain
                asyncHederaService.recordCollectionEventAsync(
                    collectionEventRepository.findByWorkflowId(workflow.id).firstOrNull() ?: return
                )
            }
            else -> { /* No automatic actions */ }
        }
    }

    private fun getHelpTextForAction(stage: EudrComplianceStage, action: String): String {
        return when {
            action.contains("GPS") || action.contains("coordinates") -> 
                "Use a GPS device or the map interface to capture precise location data"
            action.contains("upload") -> 
                "Accepted formats: PDF, PNG, JPG. Maximum file size: 10MB"
            action.contains("verify") || action.contains("Verify") -> 
                "Review the information carefully and confirm it is accurate"
            action.contains("blockchain") -> 
                "This action will be recorded immutably on the Hedera blockchain"
            else -> "Complete this action to proceed with the compliance workflow"
        }
    }

    private fun getNextStepsGuidance(stage: EudrComplianceStage): List<String> {
        return when (stage) {
            EudrComplianceStage.PRODUCTION_REGISTRATION -> listOf(
                "After registering production units, proceed to verify their geolocations",
                "Ensure all GPS coordinates are accurate before advancing"
            )
            EudrComplianceStage.DEFORESTATION_CHECK -> listOf(
                "If deforestation alerts are found, investigate and provide evidence",
                "Contact the farmer to clarify any flagged areas"
            )
            EudrComplianceStage.RISK_ASSESSMENT -> listOf(
                "Review the automated risk assessment results",
                "Implement mitigation measures for high-risk factors"
            )
            EudrComplianceStage.DUE_DILIGENCE_STATEMENT -> listOf(
                "The DDS will be generated automatically from your workflow data",
                "Review all information before authorizing the statement"
            )
            else -> listOf("Complete all required actions to advance to the next stage")
        }
    }

    private fun getEudrArticleReference(stage: EudrComplianceStage): String {
        return when (stage) {
            EudrComplianceStage.PRODUCTION_REGISTRATION -> "Article 9(1)(a) - Geolocation of plots"
            EudrComplianceStage.GEOLOCATION_VERIFICATION -> "Article 9(1)(a) - Verification of coordinates"
            EudrComplianceStage.DEFORESTATION_CHECK -> "Article 10 - Risk assessment requirements"
            EudrComplianceStage.RISK_ASSESSMENT -> "Article 10 - Due diligence obligations"
            EudrComplianceStage.DUE_DILIGENCE_STATEMENT -> "Article 4 - Due Diligence Statement"
            EudrComplianceStage.EXPORT_SHIPMENT -> "Article 12 - Placing on the market"
            else -> "EUDR Regulation (EU) 2023/1115"
        }
    }

    private fun getTipsForStage(stage: EudrComplianceStage): List<String> {
        return when (stage) {
            EudrComplianceStage.PRODUCTION_REGISTRATION -> listOf(
                "Use the latest satellite imagery for boundary verification",
                "Include a buffer zone around your production area"
            )
            EudrComplianceStage.DEFORESTATION_CHECK -> listOf(
                "Deforestation cutoff date is December 31, 2020",
                "Degradation of primary forests is also prohibited"
            )
            EudrComplianceStage.DUE_DILIGENCE_STATEMENT -> listOf(
                "Keep your DDS for at least 5 years",
                "Be prepared to provide information to competent authorities"
            )
            else -> emptyList()
        }
    }

    private fun getRecommendedActionsForRisk(classification: EudrRiskClassification): List<String> {
        return when (classification) {
            EudrRiskClassification.NEGLIGIBLE -> listOf(
                "Standard documentation is sufficient",
                "Proceed with simplified due diligence"
            )
            EudrRiskClassification.LOW -> listOf(
                "Ensure all documentation is complete",
                "Verify supplier certifications"
            )
            EudrRiskClassification.STANDARD -> listOf(
                "Conduct thorough documentation review",
                "Verify all geolocation data",
                "Check deforestation databases"
            )
            EudrRiskClassification.HIGH -> listOf(
                "Engage third-party verification",
                "Implement enhanced monitoring",
                "Consider on-site inspections",
                "Document all mitigation measures"
            )
        }
    }

    // Validation helper methods
    private fun validateProductionRegistration(workflow: SupplyChainWorkflow): List<ValidationItem> {
        val collections = collectionEventRepository.findByWorkflowId(workflow.id)
        return listOf(
            ValidationItem(
                requirement = "At least one production unit linked",
                passed = collections.isNotEmpty(),
                details = if (collections.isEmpty()) "No production units found" else "${collections.size} production unit(s) linked"
            ),
            ValidationItem(
                requirement = "All production units have GPS coordinates",
                passed = collections.all { 
                    !it.productionUnit.wgs84Coordinates.isNullOrBlank() || it.productionUnit.parcelGeometry != null
                },
                details = "GPS coordinates are required for traceability"
            )
        )
    }

    private fun validateGeolocationVerification(workflow: SupplyChainWorkflow): List<ValidationItem> {
        val collections = collectionEventRepository.findByWorkflowId(workflow.id)
        return listOf(
            ValidationItem(
                requirement = "All production units verified",
                passed = collections.all { it.productionUnit.lastVerifiedAt != null },
                details = "Each production unit must be verified for accurate geolocation"
            )
        )
    }

    private fun validateDeforestationCheck(workflow: SupplyChainWorkflow): List<ValidationItem> {
        val collections = collectionEventRepository.findByWorkflowId(workflow.id)
        val allClear = collections.none { 
            it.productionUnit.deforestationAlerts.isNotEmpty()
        }
        return listOf(
            ValidationItem(
                requirement = "No active deforestation alerts",
                passed = allClear,
                details = if (allClear) "All production units passed deforestation check" 
                    else "One or more production units have active alerts"
            )
        )
    }

    private fun validateCollectionAggregation(workflow: SupplyChainWorkflow): List<ValidationItem> {
        val consolidations = consolidationEventRepository.findByWorkflowId(workflow.id)
        return listOf(
            ValidationItem(
                requirement = "At least one consolidation event",
                passed = consolidations.isNotEmpty(),
                details = "Collected produce must be consolidated for processing"
            )
        )
    }

    private fun validateProcessing(workflow: SupplyChainWorkflow): List<ValidationItem> {
        val processings = processingEventRepository.findByWorkflowId(workflow.id)
        
        // Processing is OPTIONAL - many exporters sell raw commodities without processing
        // If no processing events exist, we consider this as "skipped" rather than incomplete
        val hasProcessingEvents = processings.isNotEmpty()
        val processingSkippedOrComplete = hasProcessingEvents || workflow.skipProcessing == true
        
        return listOf(
            ValidationItem(
                requirement = "Processing (optional)",
                passed = true, // Always pass - processing is optional
                details = if (hasProcessingEvents) {
                    "${processings.size} processing event(s) recorded"
                } else {
                    "No processing required for raw commodity export (skip this step if not applicable)"
                }
            )
        )
    }

    private fun validateRiskAssessment(workflow: SupplyChainWorkflow): List<ValidationItem> {
        return listOf(
            ValidationItem(
                requirement = "Risk assessment completed",
                passed = true, // Auto-triggered
                details = "Risk assessment is automatically calculated"
            )
        )
    }

    private fun validateDueDiligenceStatement(workflow: SupplyChainWorkflow): List<ValidationItem> {
        return listOf(
            ValidationItem(
                requirement = "Compliance certificate created",
                passed = workflow.certificateStatus != CertificateStatus.NOT_CREATED,
                details = "DDS must be generated with compliance certificate"
            )
        )
    }

    private fun validateExportShipment(workflow: SupplyChainWorkflow): List<ValidationItem> {
        val shipments = shipmentEventRepository.findByWorkflowId(workflow.id)
        return listOf(
            ValidationItem(
                requirement = "Shipment created",
                passed = shipments.isNotEmpty(),
                details = "Create a shipment with destination details"
            )
        )
    }

    private fun validateCustomsClearance(workflow: SupplyChainWorkflow): List<ValidationItem> {
        return listOf(
            ValidationItem(
                requirement = "Customs verification completed",
                passed = workflow.certificateStatus == CertificateStatus.CUSTOMS_VERIFIED,
                details = "Certificate must be verified by customs"
            )
        )
    }

    private fun validateDeliveryComplete(workflow: SupplyChainWorkflow): List<ValidationItem> {
        return listOf(
            ValidationItem(
                requirement = "Delivery confirmed",
                passed = workflow.certificateStatus == CertificateStatus.DELIVERED,
                details = "Importer must confirm receipt"
            )
        )
    }
}

// ===== DTOs =====

data class EudrStageStatusDto(
    val workflowId: String,
    val currentStage: EudrComplianceStage,
    val stageOrder: Int,
    val displayName: String,
    val description: String,
    val requiredActions: List<String>,
    val automatedActions: List<String>,
    val progress: Int,
    val canAdvance: Boolean,
    val nextStage: EudrComplianceStage?,
    val previousStage: EudrComplianceStage?,
    val blockers: List<String>
)

data class EudrWorkflowProgressDto(
    val workflowId: String,
    val workflowName: String,
    val produceType: String,
    val currentStage: EudrComplianceStage,
    val overallProgress: Int,
    val stages: List<StageProgressItem>,
    val estimatedCompletionDate: LocalDateTime,
    val riskClassification: EudrRiskClassification,
    val certificateStatus: String,
    
    // Pre-compliance checklist data
    val linkedProductionUnits: Int = 0,
    val verifiedProductionUnits: Int = 0,
    val deforestationClearUnits: Int = 0,
    val collectionEventCount: Int = 0,
    
    // Risk assessment details
    val countryRisk: String? = null,
    val complexityRisk: String? = null,
    val deforestationRisk: String? = null,
    val riskScore: Double? = null,
    val riskAssessedAt: LocalDateTime? = null,
    
    // DDS info
    val ddsReference: String? = null,
    val ddsGeneratedAt: LocalDateTime? = null,
    
    // Blockchain info
    val blockchainTransactionId: String? = null
)

data class StageProgressItem(
    val stage: EudrComplianceStage,
    val order: Int,
    val displayName: String,
    val status: StageStatus,
    val completionPercentage: Int,
    val isCurrentStage: Boolean,
    val isFutureStage: Boolean
)

data class EudrStageGuidanceDto(
    val stage: EudrComplianceStage,
    val order: Int,
    val displayName: String,
    val description: String,
    val requiredActions: List<ActionItem>,
    val automatedActions: List<ActionItem>,
    val nextSteps: List<String>,
    val eudrArticleReference: String,
    val tips: List<String>
)

data class ActionItem(
    val id: String,
    val action: String,
    val type: ActionType,
    val helpText: String?
)

enum class ActionType {
    REQUIRED,
    AUTOMATED,
    OPTIONAL
}

data class StageAdvancementResult(
    val success: Boolean,
    val previousStage: EudrComplianceStage,
    val currentStage: EudrComplianceStage,
    val message: String,
    val blockers: List<String>
)

data class StageValidationResult(
    val workflowId: String,
    val stage: EudrComplianceStage,
    val allRequirementsMet: Boolean,
    val validations: List<ValidationItem>,
    val failedRequirements: Int,
    val message: String
)

data class ValidationItem(
    val requirement: String,
    val passed: Boolean,
    val details: String
)

data class RiskAssessmentResultDto(
    val workflowId: String,
    val overallScore: Double,
    val classification: EudrRiskClassification,
    val riskFactors: List<RiskFactor>,
    val assessedAt: LocalDateTime,
    val recommendedActions: List<String>
)

data class RiskFactor(
    val type: String,
    val description: String,
    val score: Double,
    val details: String
)

data class DueDiligenceStatementDto(
    val ddsReference: String,
    val workflowId: String,
    val generatedAt: LocalDateTime,
    val exporter: String,
    val produceType: String,
    val totalQuantityKg: java.math.BigDecimal,
    val productionUnitsCount: Int,
    val collectionsCount: Int,
    val consolidationsCount: Int,
    val processingsCount: Int,
    val countriesOfOrigin: List<String>,
    val riskClassification: EudrRiskClassification,
    val status: String,
    val hederaTransactionId: String?
)
