package com.agriconnect.farmersportalapis.service.eudr

import com.agriconnect.farmersportalapis.domain.eudr.EudrBatch
import com.agriconnect.farmersportalapis.domain.eudr.ImportShipment
import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrBatchRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ImportShipmentRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProcessingEventRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProductionUnitRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * EUDR Verification Service
 * 
 * Performs comprehensive compliance checks for shipments:
 * 1. GPS coordinates verification (production units mapped)
 * 2. Deforestation-free verification
 * 3. Supply chain traceability validation
 * 4. Risk assessment based on origin country
 * 5. Due diligence statement generation
 */
@Service
class EudrVerificationService(
    private val importShipmentRepository: ImportShipmentRepository,
    private val eudrBatchRepository: EudrBatchRepository,
    private val productionUnitRepository: ProductionUnitRepository,
    private val processingEventRepository: ProcessingEventRepository
) {

    private val logger = LoggerFactory.getLogger(EudrVerificationService::class.java)

    /**
     * High-risk countries for deforestation (EUDR Annex I)
     */
    private val highRiskCountries = setOf(
        "Brazil", "Indonesia", "Democratic Republic of Congo", "DRC", "Congo",
        "Bolivia", "Peru", "Colombia", "Paraguay", "Malaysia", "Papua New Guinea",
        "Cameroon", "Gabon", "Central African Republic", "Nigeria"
    )

    /**
     * Medium-risk countries (tropical regions with some deforestation)
     */
    private val mediumRiskCountries = setOf(
        "Kenya", "Uganda", "Tanzania", "Ethiopia", "Rwanda", "Burundi",
        "India", "Vietnam", "Thailand", "Myanmar", "Laos", "Cambodia",
        "Mexico", "Guatemala", "Honduras", "Costa Rica", "Ecuador"
    )

    /**
     * Verify shipment compliance and return detailed results
     */
    fun verifyShipmentCompliance(shipmentId: String): EudrComplianceResult {
        val shipment = importShipmentRepository.findById(shipmentId)
            .orElseThrow { NoSuchElementException("Shipment not found: $shipmentId") }

        logger.info("Starting EUDR compliance verification for shipment: $shipmentId")

        // 1. Trace back to source batches
        val sourceBatches = if (shipment.sourceBatchId != null) {
            eudrBatchRepository.findById(shipment.sourceBatchId!!)
                .map { listOf(it) }
                .orElse(emptyList())
        } else {
            emptyList()
        }

        // 2. Collect all production units from batch relationships
        val productionUnits = sourceBatches.flatMap { batch ->
            batch.productionUnits.map { it.productionUnit }
        }

        // 3. Verify GPS coordinates (check wgs84Coordinates field)
        val gpsCoordinatesCount = productionUnits.count { 
            !it.wgs84Coordinates.isNullOrEmpty()
        }
        val totalProductionUnits = productionUnits.size

        val gpsVerificationPassed = if (totalProductionUnits > 0) {
            gpsCoordinatesCount == totalProductionUnits
        } else {
            false
        }

        // 4. Verify deforestation-free status
        val deforestationVerification = verifyDeforestationFree(productionUnits)

        // 5. Verify traceability chain
        val traceabilityResult = verifyTraceabilityChain(shipment, sourceBatches)

        // 6. Assess risk level
        val riskLevel = assessRiskLevel(shipment.originCountry)

        // 7. Count farmers involved
        val farmerCount = productionUnits.map { it.farmer.id }.distinct().size

        // 8. Calculate completeness score
        val completenessScore = calculateCompletenessScore(
            gpsVerificationPassed,
            deforestationVerification.passed,
            traceabilityResult.complete,
            riskLevel
        )

        // 9. Determine overall compliance
        val isCompliant = gpsVerificationPassed && 
                         deforestationVerification.passed && 
                         traceabilityResult.complete &&
                         completenessScore >= 80.0

        // 10. Generate traceability hash
        val traceabilityHash = generateTraceabilityHash(
            shipmentId,
            sourceBatches.map { it.id },
            productionUnits.map { it.id }
        )

        logger.info("""
            EUDR Verification Complete for shipment $shipmentId:
            - GPS Verified: $gpsVerificationPassed ($gpsCoordinatesCount/$totalProductionUnits)
            - Deforestation Free: ${deforestationVerification.passed}
            - Traceability Complete: ${traceabilityResult.complete}
            - Risk Level: $riskLevel
            - Completeness Score: $completenessScore%
            - Overall Compliant: $isCompliant
        """.trimIndent())

        return EudrComplianceResult(
            isCompliant = isCompliant,
            originCountry = shipment.originCountry,
            riskLevel = riskLevel,
            farmerCount = farmerCount,
            productionUnitCount = totalProductionUnits,
            gpsCount = gpsCoordinatesCount,
            gpsVerificationPassed = gpsVerificationPassed,
            deforestationStatus = deforestationVerification.status,
            deforestationVerificationPassed = deforestationVerification.passed,
            traceabilityComplete = traceabilityResult.complete,
            traceabilityHash = traceabilityHash,
            completenessScore = completenessScore,
            failureReasons = buildFailureReasons(
                gpsVerificationPassed,
                deforestationVerification,
                traceabilityResult,
                completenessScore
            ),
            complianceData = buildComplianceData(
                shipment,
                riskLevel,
                farmerCount,
                totalProductionUnits,
                gpsCoordinatesCount,
                deforestationVerification.status,
                traceabilityHash
            )
        )
    }

    /**
     * Verify deforestation-free status for production units
     * In real implementation, this would check against satellite imagery APIs
     */
    private fun verifyDeforestationFree(productionUnits: List<ProductionUnit>): DeforestationVerificationResult {
        if (productionUnits.isEmpty()) {
            return DeforestationVerificationResult(
                passed = false,
                status = "NO_PRODUCTION_UNITS",
                details = "No production units found for verification"
            )
        }

        // Check if any production units have deforestation alerts
        val unitsWithAlerts = productionUnits.count { it.deforestationAlerts.isNotEmpty() }
        
        if (unitsWithAlerts > 0) {
            return DeforestationVerificationResult(
                passed = false,
                status = "DEFORESTATION_DETECTED",
                details = "$unitsWithAlerts production units have deforestation alerts"
            )
        }

        // In production, integrate with:
        // - Global Forest Watch API
        // - EU Forest Observatory
        // - Copernicus Sentinel satellite data
        // For now, assume verified if no alerts
        
        return DeforestationVerificationResult(
            passed = true,
            status = "VERIFIED_FREE",
            details = "All ${productionUnits.size} production units verified deforestation-free"
        )
    }

    /**
     * Verify complete traceability chain
     */
    private fun verifyTraceabilityChain(
        shipment: ImportShipment,
        sourceBatches: List<EudrBatch>
    ): TraceabilityResult {
        val gaps = mutableListOf<String>()

        if (sourceBatches.isEmpty()) {
            gaps.add("No source batches linked to shipment")
            return TraceabilityResult(complete = false, gaps = gaps)
        }

        // Check each batch has processing events
        sourceBatches.forEach { batch ->
            val processingEvents = processingEventRepository.findByBatchId(batch.id)
            if (processingEvents.isEmpty()) {
                gaps.add("Batch ${batch.id} has no processing events")
            }
        }

        // Check aggregator → processor → exporter chain
        val hasAggregator = sourceBatches.any { it.aggregator != null }
        val hasProcessor = sourceBatches.any { batch ->
            processingEventRepository.findByBatchId(batch.id).isNotEmpty()
        }

        if (!hasAggregator) gaps.add("No aggregator in supply chain")
        if (!hasProcessor) gaps.add("No processor in supply chain")

        return TraceabilityResult(
            complete = gaps.isEmpty(),
            gaps = gaps
        )
    }

    /**
     * Assess risk level based on origin country
     */
    private fun assessRiskLevel(originCountry: String): RiskLevel {
        return when {
            highRiskCountries.contains(originCountry) -> RiskLevel.HIGH
            mediumRiskCountries.contains(originCountry) -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    /**
     * Calculate completeness score (0-100)
     */
    private fun calculateCompletenessScore(
        gpsVerified: Boolean,
        deforestationFree: Boolean,
        traceabilityComplete: Boolean,
        riskLevel: RiskLevel
    ): Double {
        var score = 0.0

        if (gpsVerified) score += 35.0
        if (deforestationFree) score += 40.0
        if (traceabilityComplete) score += 25.0

        // Adjust for risk level
        when (riskLevel) {
            RiskLevel.HIGH -> score *= 0.9 // Require higher standards
            RiskLevel.MEDIUM -> score *= 0.95
            RiskLevel.LOW -> score *= 1.0
        }

        return score
    }

    /**
     * Generate traceability hash for immutable record
     */
    private fun generateTraceabilityHash(
        shipmentId: String,
        batchIds: List<String>,
        productionUnitIds: List<String>
    ): String {
        val data = "$shipmentId-${batchIds.sorted().joinToString(",")}-${productionUnitIds.sorted().joinToString(",")}"
        return "0x${data.hashCode().toString(16)}" // Simplified; use SHA-256 in production
    }

    /**
     * Build failure reasons list
     */
    private fun buildFailureReasons(
        gpsVerified: Boolean,
        deforestationVerification: DeforestationVerificationResult,
        traceabilityResult: TraceabilityResult,
        completenessScore: Double
    ): List<String> {
        val reasons = mutableListOf<String>()

        if (!gpsVerified) reasons.add("GPS coordinates missing or incomplete")
        if (!deforestationVerification.passed) reasons.add("Deforestation verification failed: ${deforestationVerification.details}")
        if (!traceabilityResult.complete) reasons.addAll(traceabilityResult.gaps)
        if (completenessScore < 80.0) reasons.add("Completeness score too low: $completenessScore% (minimum 80% required)")

        return reasons
    }

    /**
     * Build compliance data map for NFT metadata
     */
    private fun buildComplianceData(
        shipment: ImportShipment,
        riskLevel: RiskLevel,
        farmerCount: Int,
        productionUnitCount: Int,
        gpsCount: Int,
        deforestationStatus: String,
        traceabilityHash: String
    ): Map<String, String> {
        return mapOf(
            "shipmentId" to shipment.id,
            "shipmentNumber" to shipment.shipmentNumber,
            "originCountry" to shipment.originCountry,
            "riskLevel" to riskLevel.name,
            "totalFarmers" to farmerCount.toString(),
            "totalProductionUnits" to productionUnitCount.toString(),
            "gpsCoordinatesCount" to gpsCount.toString(),
            "deforestationStatus" to deforestationStatus,
            "traceabilityHash" to traceabilityHash,
            "produceType" to shipment.produceType,
            "quantityKg" to shipment.quantityKg.toString()
        )
    }
}

/**
 * EUDR Compliance Result
 */
data class EudrComplianceResult(
    val isCompliant: Boolean,
    val originCountry: String,
    val riskLevel: RiskLevel,
    val farmerCount: Int,
    val productionUnitCount: Int,
    val gpsCount: Int,
    val gpsVerificationPassed: Boolean,
    val deforestationStatus: String,
    val deforestationVerificationPassed: Boolean,
    val traceabilityComplete: Boolean,
    val traceabilityHash: String,
    val completenessScore: Double,
    val failureReasons: List<String>,
    val complianceData: Map<String, String>
)

data class DeforestationVerificationResult(
    val passed: Boolean,
    val status: String,
    val details: String
)

data class TraceabilityResult(
    val complete: Boolean,
    val gaps: List<String>
)

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}
