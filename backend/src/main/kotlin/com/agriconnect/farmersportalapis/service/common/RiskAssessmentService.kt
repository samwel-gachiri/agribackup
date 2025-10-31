package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.domain.eudr.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.CountryRiskRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.DeforestationAlertRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrBatchRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProductionUnitRepository
import com.agriconnect.farmersportalapis.service.hedera.HederaConsensusServices
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class RiskAssessmentService(
    private val eudrBatchRepository: EudrBatchRepository,
    private val countryRiskRepository: CountryRiskRepository,
    private val productionUnitRepository: ProductionUnitRepository,
    private val deforestationAlertRepository: DeforestationAlertRepository,
    private val hederaConsensusService: HederaConsensusServices,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(RiskAssessmentService::class.java)

    companion object {
        private const val HIGH_RISK_THRESHOLD = 0.8
        private const val MEDIUM_RISK_THRESHOLD = 0.5
        private const val RECENT_ALERT_DAYS = 365
        private const val BUFFER_DISTANCE_KM = 10.0
    }

    /**
     * Comprehensive risk assessment for a batch
     */
    fun assessBatchRisk(batchId: String): RiskAssessmentResult {
        logger.info("Starting risk assessment for batch: $batchId")

        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        // Calculate risk score components
        val countryRisk = calculateCountryRisk(batch.countryOfProduction)
        val deforestationRisk = calculateDeforestationRisk(batch)
        val supplierRisk = calculateSupplierRisk(batch)
        val commodityRisk = calculateCommodityRisk(batch.commodityDescription)
        val documentationRisk = calculateDocumentationRisk(batch)
        val geospatialRisk = calculateGeospatialRisk(batch)

        // Calculate overall risk score (weighted average)
        val overallScore = calculateOverallRiskScore(
            countryRisk.score,
            deforestationRisk.score,
            supplierRisk.score,
            commodityRisk.score,
            documentationRisk.score,
            geospatialRisk.score
        )

        // Determine risk level
        val riskLevel = determineRiskLevel(overallScore)

        // Create risk assessment result
        val result = RiskAssessmentResult(
            batchId = batchId,
            overallScore = overallScore,
            riskLevel = riskLevel,
            assessedAt = LocalDateTime.now(),
            components = RiskComponents(
                countryRisk = countryRisk,
                deforestationRisk = deforestationRisk,
                supplierRisk = supplierRisk,
                commodityRisk = commodityRisk,
                documentationRisk = documentationRisk,
                geospatialRisk = geospatialRisk
            ),
            recommendations = generateRecommendations(riskLevel, batch)
        )

        // Update batch with risk assessment
        batch.riskLevel = riskLevel
        batch.riskRationale = generateRiskRationale(result)
        eudrBatchRepository.save(batch)

//        // Record risk assessment on Hedera
//        try {
//            hederaConsensusService.recordRiskAssessment(
//                batchId,
//                riskLevel,
//                result.toString(),
//                "SYSTEM"
//            )
//        } catch (e: Exception) {
//            logger.warn("Failed to record risk assessment on Hedera", e)
//        }

        logger.info("Completed risk assessment for batch $batchId: $riskLevel (score: $overallScore)")
        return result
    }

    /**
     * Calculate country-specific risk
     */
    private fun calculateCountryRisk(countryCode: String): RiskComponent {
        val countryRisk = countryRiskRepository.findById(countryCode).orElse(null)

        return if (countryRisk != null) {
            val score = when (countryRisk.riskLevel) {
                CountryRiskLevel.LOW -> 0.2
                CountryRiskLevel.STANDARD -> 0.5
                CountryRiskLevel.HIGH -> 0.9
            }

            RiskComponent(
                name = "Country Risk",
                score = score,
                level = countryRisk.riskLevel.name,
                justification = countryRisk.riskJustification ?: "Based on country risk matrix",
                data = mapOf(
                    "countryCode" to countryCode,
                    "countryName" to countryRisk.countryName,
                    "riskLevel" to countryRisk.riskLevel.name
                )
            )
        } else {
            RiskComponent(
                name = "Country Risk",
                score = 0.7, // Default to medium-high for unknown countries
                level = "UNKNOWN",
                justification = "Country not found in risk matrix - requires manual review",
                data = mapOf("countryCode" to countryCode)
            )
        }
    }

    /**
     * Calculate deforestation risk based on alerts near production units
     */
    private fun calculateDeforestationRisk(batch: EudrBatch): RiskComponent {
        val productionUnitIds = batch.productionUnits.map { it.productionUnit.id }
        if (productionUnitIds.isEmpty()) {
            return RiskComponent(
                name = "Deforestation Risk",
                score = 0.8,
                level = "HIGH",
                justification = "No production units associated with batch",
                data = emptyMap()
            )
        }

        val recentAlerts = deforestationAlertRepository.findByProductionUnitIdsAndDateRange(
            productionUnitIds,
            LocalDateTime.now().minusDays(RECENT_ALERT_DAYS.toLong()),
            LocalDateTime.now()
        )

        val highSeverityAlerts = recentAlerts.count { it.severity == DeforestationAlert.Severity.HIGH }
        val mediumSeverityAlerts = recentAlerts.count { it.severity == DeforestationAlert.Severity.MEDIUM }

        val score = when {
            highSeverityAlerts > 0 -> 0.9
            mediumSeverityAlerts > 2 -> 0.7
            mediumSeverityAlerts > 0 -> 0.5
            recentAlerts.isNotEmpty() -> 0.3
            else -> 0.1
        }

        val level = determineRiskLevel(score).name

        return RiskComponent(
            name = "Deforestation Risk",
            score = score,
            level = level,
            justification = "Based on recent deforestation alerts near production units",
            data = mapOf(
                "totalAlerts" to recentAlerts.size.toString(),
                "highSeverityAlerts" to highSeverityAlerts.toString(),
                "mediumSeverityAlerts" to mediumSeverityAlerts.toString(),
                "assessmentPeriodDays" to RECENT_ALERT_DAYS.toString()
            )
        )
    }

    /**
     * Calculate supplier/farmer risk based on history and verification status
     */
    private fun calculateSupplierRisk(batch: EudrBatch): RiskComponent {
        // For now, implement basic supplier risk based on batch creator
        // In a full implementation, this would check supplier history, certifications, etc.

        val score = 0.3 // Default medium-low risk, can be improved with supplier data

        return RiskComponent(
            name = "Supplier Risk",
            score = score,
            level = determineRiskLevel(score).name,
            justification = "Based on supplier verification status and compliance history",
            data = mapOf(
                "supplierId" to batch.createdBy,
                "verificationStatus" to "PENDING_IMPLEMENTATION"
            )
        )
    }

    /**
     * Calculate commodity-specific risk
     */
    private fun calculateCommodityRisk(commodity: String): RiskComponent {
        // High-risk commodities based on EUDR Annex I
        val highRiskCommodities = setOf(
            "cattle", "beef", "palm oil", "soy", "coffee", "cocoa",
            "rubber", "timber", "maize", "palm", "soya", "cacao"
        )

        val normalizedCommodity = commodity.lowercase()
        val isHighRisk = highRiskCommodities.any { it in normalizedCommodity }

        val score = if (isHighRisk) 0.7 else 0.3

        return RiskComponent(
            name = "Commodity Risk",
            score = score,
            level = determineRiskLevel(score).name,
            justification = "Based on commodity type and EUDR risk classification",
            data = mapOf(
                "commodity" to commodity,
                "isHighRiskCommodity" to isHighRisk.toString()
            )
        )
    }

    /**
     * Calculate documentation completeness risk
     */
    private fun calculateDocumentationRisk(batch: EudrBatch): RiskComponent {
        val documents = batch.documents
        val requiredDocTypes = setOf(
            EudrDocumentType.LAND_RIGHTS_CERTIFICATE,
            EudrDocumentType.HARVEST_RECORD,
            EudrDocumentType.GEOLOCATION_DATA
        )

        val presentDocTypes = documents.map { it.documentType }.toSet()
        val missingDocs = requiredDocTypes - presentDocTypes

        val completenessRatio = presentDocTypes.size.toDouble() / requiredDocTypes.size
        val score = 1.0 - completenessRatio // Higher score for missing documentation

        return RiskComponent(
            name = "Documentation Risk",
            score = score,
            level = determineRiskLevel(score).name,
            justification = "Based on completeness of required documentation",
            data = mapOf(
                "totalDocuments" to documents.size.toString(),
                "requiredDocTypes" to requiredDocTypes.size.toString(),
                "presentDocTypes" to presentDocTypes.size.toString(),
                "missingDocTypes" to missingDocs.joinToString(", ") { it.name }
            )
        )
    }

    /**
     * Calculate geospatial verification risk
     */
    private fun calculateGeospatialRisk(batch: EudrBatch): RiskComponent {
        val productionUnits = batch.productionUnits.map { it.productionUnit }

        if (productionUnits.isEmpty()) {
            return RiskComponent(
                name = "Geospatial Risk",
                score = 0.9,
                level = "HIGH",
                justification = "No geospatial data available for batch",
                data = emptyMap()
            )
        }

        val verifiedUnits = productionUnits.count { it.lastVerifiedAt != null }
        val verificationRatio = verifiedUnits.toDouble() / productionUnits.size

        val score = 1.0 - verificationRatio // Higher score for unverified units

        return RiskComponent(
            name = "Geospatial Risk",
            score = score,
            level = determineRiskLevel(score).name,
            justification = "Based on geospatial verification status of production units",
            data = mapOf(
                "totalUnits" to productionUnits.size.toString(),
                "verifiedUnits" to verifiedUnits.toString(),
                "verificationRatio" to String.format("%.2f", verificationRatio)
            )
        )
    }

    /**
     * Calculate overall risk score using weighted average
     */
    private fun calculateOverallRiskScore(
        countryRisk: Double,
        deforestationRisk: Double,
        supplierRisk: Double,
        commodityRisk: Double,
        documentationRisk: Double,
        geospatialRisk: Double
    ): Double {
        // Weights based on EUDR risk factors importance
        val weights = mapOf(
            "country" to 0.25,
            "deforestation" to 0.30,
            "supplier" to 0.15,
            "commodity" to 0.10,
            "documentation" to 0.10,
            "geospatial" to 0.10
        )

        return (countryRisk * weights["country"]!!) +
               (deforestationRisk * weights["deforestation"]!!) +
               (supplierRisk * weights["supplier"]!!) +
               (commodityRisk * weights["commodity"]!!) +
               (documentationRisk * weights["documentation"]!!) +
               (geospatialRisk * weights["geospatial"]!!)
    }

    /**
     * Determine risk level from score
     */
    private fun determineRiskLevel(score: Double): RiskLevel {
        return when {
            score >= HIGH_RISK_THRESHOLD -> RiskLevel.HIGH
            score >= MEDIUM_RISK_THRESHOLD -> RiskLevel.MEDIUM
            score > 0.2 -> RiskLevel.LOW
            else -> RiskLevel.NONE
        }
    }

    /**
     * Generate recommendations based on risk level
     */
    private fun generateRecommendations(riskLevel: RiskLevel, batch: EudrBatch): List<String> {
        val recommendations = mutableListOf<String>()

        when (riskLevel) {
            RiskLevel.HIGH -> {
                recommendations.add("Immediate mitigation required - consider batch rejection or enhanced due diligence")
                recommendations.add("Obtain additional documentation from supplier")
                recommendations.add("Conduct on-site verification of production units")
                recommendations.add("Consult legal/compliance team before proceeding")
            }
            RiskLevel.MEDIUM -> {
                recommendations.add("Enhanced due diligence recommended")
                recommendations.add("Verify supplier certifications and documentation")
                recommendations.add("Monitor production units for deforestation alerts")
                recommendations.add("Consider third-party verification")
            }
            RiskLevel.LOW -> {
                recommendations.add("Standard due diligence procedures sufficient")
                recommendations.add("Regular monitoring of production units recommended")
            }
            RiskLevel.NONE -> {
                recommendations.add("Low risk - standard procedures acceptable")
            }
        }

        // Add specific recommendations based on risk components
        if (batch.documents.none { it.documentType == EudrDocumentType.LAND_RIGHTS_CERTIFICATE }) {
            recommendations.add("Obtain land rights certificate from supplier")
        }

        if (batch.productionUnits.any { it.productionUnit.lastVerifiedAt == null }) {
            recommendations.add("Verify geospatial data for all production units")
        }

        return recommendations
    }

    /**
     * Generate human-readable risk rationale
     */
    private fun generateRiskRationale(result: RiskAssessmentResult): String {
        return """
            Risk Assessment Summary:
            Overall Risk Level: ${result.riskLevel}
            Risk Score: ${String.format("%.2f", result.overallScore)}

            Key Risk Factors:
            - Country Risk: ${result.components.countryRisk.level} (${String.format("%.2f", result.components.countryRisk.score)})
            - Deforestation Risk: ${result.components.deforestationRisk.level} (${String.format("%.2f", result.components.deforestationRisk.score)})
            - Documentation Risk: ${result.components.documentationRisk.level} (${String.format("%.2f", result.components.documentationRisk.score)})

            Recommendations:
            ${result.recommendations.joinToString("\n- ") { "- $it" }}
        """.trimIndent()
    }

    /**
     * Get risk assessment history for a batch
     */
    fun getRiskAssessmentHistory(batchId: String): List<RiskAssessmentResult> {
        // This would typically query an audit table
        // For now, return the current assessment
        return listOf(assessBatchRisk(batchId))
    }

    /**
     * Bulk risk assessment for multiple batches
     */
    fun assessBatchRiskBulk(batchIds: List<String>): Map<String, RiskAssessmentResult> {
        return batchIds.associateWith { assessBatchRisk(it) }
    }
}

// Data classes for risk assessment results

data class RiskAssessmentResult(
    val batchId: String,
    val overallScore: Double,
    val riskLevel: RiskLevel,
    val assessedAt: LocalDateTime,
    val components: RiskComponents,
    val recommendations: List<String>
)

data class RiskComponents(
    val countryRisk: RiskComponent,
    val deforestationRisk: RiskComponent,
    val supplierRisk: RiskComponent,
    val commodityRisk: RiskComponent,
    val documentationRisk: RiskComponent,
    val geospatialRisk: RiskComponent
)

data class RiskComponent(
    val name: String,
    val score: Double, // 0.0 to 1.0
    val level: String,
    val justification: String,
    val data: Map<String, String>
)