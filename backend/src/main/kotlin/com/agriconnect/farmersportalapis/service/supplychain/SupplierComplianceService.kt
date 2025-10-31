package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.domain.eudr.EudrBatch
import com.agriconnect.farmersportalapis.domain.eudr.RiskLevel
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrBatchRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProductionUnitRepository
import com.agriconnect.farmersportalapis.service.common.RiskAssessmentService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class SupplierComplianceService(
    private val farmerRepository: FarmerRepository,
    private val productionUnitRepository: ProductionUnitRepository,
    private val eudrBatchRepository: EudrBatchRepository,
    private val riskAssessmentService: RiskAssessmentService
) {

    private val logger = LoggerFactory.getLogger(SupplierComplianceService::class.java)

    companion object {
        private const val COMPLIANT_THRESHOLD = 0.7 // 70% compliance score threshold
        private const val RECENT_BATCHES_DAYS = 365
    }

    /**
     * Get compliance overview for all suppliers of an exporter
     */
    fun getSupplierComplianceOverview(exporterId: String): SupplierComplianceOverview {
        logger.info("Getting supplier compliance overview for exporter: $exporterId")

        // Get all farmers associated with this exporter's zones
        val farmers = farmerRepository.findByExporterId(exporterId)

        val supplierCompliances = farmers.map { farmer ->
            getSupplierCompliance(farmer.id!!)
        }

        val compliantCount = supplierCompliances.count { it.complianceStatus == ComplianceStatus.COMPLIANT }
        val pendingCount = supplierCompliances.count { it.complianceStatus == ComplianceStatus.PENDING_REVIEW }
        val atRiskCount = supplierCompliances.count { it.complianceStatus == ComplianceStatus.AT_RISK }

        return SupplierComplianceOverview(
            totalSuppliers = supplierCompliances.size,
            compliantSuppliers = compliantCount,
            pendingSuppliers = pendingCount,
            atRiskSuppliers = atRiskCount,
            suppliers = supplierCompliances
        )
    }

    /**
     * Get detailed compliance information for a specific supplier
     */
    fun getSupplierCompliance(supplierId: String): SupplierCompliance {
        logger.info("Getting compliance details for supplier: $supplierId")

        val farmer = farmerRepository.findById(supplierId)
            .orElseThrow { IllegalArgumentException("Supplier not found: $supplierId") }

        // Get all production units for this farmer
        val productionUnits = productionUnitRepository.findByFarmerId(supplierId)

        // Get all batches associated with these production units
        val batchIds = productionUnits.flatMap { unit ->
            unit.batchRelationships.map { it.batch.id }
        }.distinct()

        // Get recent batches (last 365 days)
        val recentBatches = eudrBatchRepository.findByIdsAndDateRange(
            batchIds,
            LocalDateTime.now().minusDays(RECENT_BATCHES_DAYS.toLong()),
            LocalDateTime.now()
        )

        // Calculate compliance metrics
        val complianceMetrics = calculateComplianceMetrics(recentBatches)

        // Determine overall compliance status
        val complianceStatus = determineComplianceStatus(complianceMetrics.overallScore)

        return SupplierCompliance(
            supplierId = supplierId,
            supplierName = farmer.userProfile?.fullName ?: "Unknown",
            region = farmer.location?.customName ?: "Unknown",
            primaryCommodity = determinePrimaryCommodity(recentBatches),
            complianceStatus = complianceStatus,
            complianceScore = complianceMetrics.overallScore,
            totalBatches = recentBatches.size,
            compliantBatches = complianceMetrics.compliantBatches,
            atRiskBatches = complianceMetrics.atRiskBatches,
            lastAssessmentDate = complianceMetrics.lastAssessmentDate,
            riskFactors = complianceMetrics.riskFactors
        )
    }

    /**
     * Calculate compliance metrics from batch data
     */
    private fun calculateComplianceMetrics(batches: List<EudrBatch>): ComplianceMetrics {
        if (batches.isEmpty()) {
            return ComplianceMetrics(
                overallScore = 0.0,
                compliantBatches = 0,
                atRiskBatches = 0,
                lastAssessmentDate = null,
                riskFactors = listOf("No recent batches found")
            )
        }

        var totalScore = 0.0
        var compliantCount = 0
        var atRiskCount = 0
        val riskFactors = mutableListOf<String>()
        var latestAssessment: LocalDateTime? = null

        batches.forEach { batch ->
            // If batch has been risk assessed, use that score
            if (batch.riskLevel != null) {
                val batchScore = when (batch.riskLevel) {
                    RiskLevel.NONE, RiskLevel.LOW -> 0.9
                    RiskLevel.MEDIUM -> 0.6
                    RiskLevel.HIGH -> 0.2
                    null -> 0.5 // Should not happen due to null check above
                }

                totalScore += batchScore

                if (batchScore >= COMPLIANT_THRESHOLD) {
                    compliantCount++
                } else if (batchScore < 0.4) {
                    atRiskCount++
                    riskFactors.add("High risk batch: ${batch.batchCode}")
                }

                if (batch.createdAt.isAfter(latestAssessment ?: LocalDateTime.MIN)) {
                    latestAssessment = batch.createdAt
                }
            } else {
                // Batch not assessed - consider as pending
                totalScore += 0.5 // Medium score for unassessed
                riskFactors.add("Unassessed batch: ${batch.batchCode}")
            }
        }

        val overallScore = (totalScore / batches.size * 100).toInt() / 100.0

        return ComplianceMetrics(
            overallScore = overallScore,
            compliantBatches = compliantCount,
            atRiskBatches = atRiskCount,
            lastAssessmentDate = latestAssessment,
            riskFactors = riskFactors.take(5) // Limit to top 5 risk factors
        )
    }

    /**
     * Determine primary commodity from batches
     */
    private fun determinePrimaryCommodity(batches: List<EudrBatch>): String {
        if (batches.isEmpty()) return "Unknown"

        val commodityCounts = batches.groupBy { it.commodityDescription }
            .mapValues { it.value.size }

        return commodityCounts.maxByOrNull { it.value }?.key ?: "Mixed"
    }

    private fun determineComplianceStatus(score: Double): ComplianceStatus {
        return when {
            score >= COMPLIANT_THRESHOLD -> ComplianceStatus.COMPLIANT
            score >= 0.4 -> ComplianceStatus.PENDING_REVIEW
            score >= 0.0 -> ComplianceStatus.AT_RISK
            else -> ComplianceStatus.AT_RISK
        }
    }
}

// Data classes for supplier compliance

enum class ComplianceStatus {
    COMPLIANT,
    PENDING_REVIEW,
    AT_RISK
}

data class SupplierComplianceOverview(
    val totalSuppliers: Int,
    val compliantSuppliers: Int,
    val pendingSuppliers: Int,
    val atRiskSuppliers: Int,
    val suppliers: List<SupplierCompliance>
)

data class SupplierCompliance(
    val supplierId: String,
    val supplierName: String,
    val region: String,
    val primaryCommodity: String,
    val complianceStatus: ComplianceStatus,
    val complianceScore: Double, // 0.0 to 1.0
    val totalBatches: Int,
    val compliantBatches: Int,
    val atRiskBatches: Int,
    val lastAssessmentDate: LocalDateTime?,
    val riskFactors: List<String>
)

private data class ComplianceMetrics(
    val overallScore: Double,
    val compliantBatches: Int,
    val atRiskBatches: Int,
    val lastAssessmentDate: LocalDateTime?,
    val riskFactors: List<String>
)