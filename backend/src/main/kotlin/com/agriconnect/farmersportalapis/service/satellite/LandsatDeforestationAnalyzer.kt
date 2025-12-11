package com.agriconnect.farmersportalapis.service.satellite

import com.agriconnect.farmersportalapis.domain.eudr.DeforestationAlert
import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import org.locationtech.jts.geom.Geometry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Landsat 8/9 Deforestation Analyzer
 * 
 * Backup analyzer using Landsat imagery when Sentinel-2 is unavailable (e.g., cloud cover).
 * Combines Landsat 8 and Landsat 9 for better temporal resolution.
 * 
 * Key Features:
 * - 30m spatial resolution
 * - 8-day revisit time (when combining L8 + L9)
 * - Cloud masking and quality assessment
 * - Same analysis methodology as Sentinel-2 for consistency
 * 
 * Use Cases:
 * - Sentinel-2 has excessive cloud cover
 * - Historical analysis (Landsat 8 available since 2013)
 * - Larger production units where 30m resolution is sufficient
 */
@Service
class LandsatDeforestationAnalyzer(
    private val satelliteImageryService: SatelliteImageryService
) {

    private val logger = LoggerFactory.getLogger(LandsatDeforestationAnalyzer::class.java)

    @Value("\${landsat.analysis.ndvi-deforestation-threshold:0.3}")
    private var ndviDeforestationThreshold: Double = 0.3

    @Value("\${landsat.analysis.ndvi-change-threshold:-0.2}")
    private var ndviChangeThreshold: Double = -0.2

    @Value("\${landsat.analysis.baseline-months:3}")
    private var baselineMonths: Long = 3

    @Value("\${landsat.analysis.recent-days:16}")  // Longer period due to 16-day revisit
    private var recentDays: Long = 16

    companion object {
        private val EUDR_CUTOFF = LocalDate.of(2020, 12, 31)
    }

    /**
     * Analyze production unit for deforestation using Landsat 8/9 imagery
     * Uses same logic as Sentinel-2 but with 30m resolution
     */
    fun analyzeProductionUnit(productionUnit: ProductionUnit): DeforestationAnalysisResult {
        if (!satelliteImageryService.isAvailable()) {
            logger.warn("Satellite imagery service not available for production unit ${productionUnit.id}")
            return createUnavailableResult(productionUnit.id)
        }

        return try {
            logger.info("Analyzing production unit ${productionUnit.id} with Landsat 8/9")
            
            val geometry = parseGeometry(productionUnit)
            
            // Get baseline imagery
            val baselineStart = EUDR_CUTOFF.minusMonths(baselineMonths)
            val baselineEnd = EUDR_CUTOFF
            val baselineImage = satelliteImageryService.queryLandsat(
                geometry, baselineStart, baselineEnd
            )
            
            // Get recent imagery (16 days for Landsat)
            val recentStart = LocalDate.now().minusDays(recentDays)
            val recentEnd = LocalDate.now()
            val recentImage = satelliteImageryService.queryLandsat(
                geometry, recentStart, recentEnd
            )
            
            if (baselineImage == null || recentImage == null) {
                logger.warn("Could not retrieve Landsat imagery for production unit ${productionUnit.id}")
                return createNoImageryResult(productionUnit.id)
            }
            
            // Calculate NDVI change (same as Sentinel-2)
            val ndviChange = recentImage.ndviMean - baselineImage.ndviMean
            val ndviChangePercent = (ndviChange / baselineImage.ndviMean) * 100
            
            logger.info("""
                Production Unit ${productionUnit.id} Landsat NDVI Analysis:
                - Baseline NDVI: ${baselineImage.ndviMean}
                - Recent NDVI: ${recentImage.ndviMean}
                - Change: ${ndviChange} (${ndviChangePercent}%)
            """.trimIndent())
            
            // Detect deforestation
            val isDeforested = ndviChange < ndviChangeThreshold || recentImage.ndviMean < ndviDeforestationThreshold
            
            if (!isDeforested) {
                return DeforestationAnalysisResult(
                    productionUnitId = productionUnit.id,
                    analysisDate = LocalDateTime.now(),
                    deforestedAreaHa = 0.0,
                    severity = null,
                    ndviChangeMean = ndviChange,
                    ndviCurrent = recentImage.ndviMean,
                    ndviBaseline = baselineImage.ndviMean,
                    dataSource = "LANDSAT-8/9",
                    analysisType = "NO_DEFORESTATION",
                    alertGenerated = false
                )
            }
            
            // Calculate deforested area and severity
            val deforestedAreaHa = calculateDeforestedArea(
                productionUnit.areaHectares.toDouble(),
                ndviChange,
                ndviChangeThreshold
            )
            
            val severity = determineSeverity(deforestedAreaHa)
            
            logger.info("""
                DEFORESTATION DETECTED (Landsat):
                - Production Unit: ${productionUnit.id}
                - Deforested Area: ${deforestedAreaHa} ha
                - Severity: ${severity}
                - NDVI Change: ${ndviChange}
            """.trimIndent())
            
            DeforestationAnalysisResult(
                productionUnitId = productionUnit.id,
                analysisDate = LocalDateTime.now(),
                deforestedAreaHa = deforestedAreaHa,
                severity = severity,
                ndviChangeMean = ndviChange,
                ndviCurrent = recentImage.ndviMean,
                ndviBaseline = baselineImage.ndviMean,
                dataSource = "LANDSAT-8/9",
                analysisType = "NDVI_CHANGE_DETECTION",
                alertGenerated = true,
                additionalMetrics = mapOf(
                    "ndviChangePercent" to ndviChangePercent,
                    "resolution" to "30m",
                    "revisitTime" to "8-16 days",
                    "baselinePeriod" to "${baselineStart} to ${baselineEnd}",
                    "recentPeriod" to "${recentStart} to ${recentEnd}"
                )
            )
            
        } catch (e: Exception) {
            logger.error("Failed to analyze production unit ${productionUnit.id} with Landsat", e)
            createErrorResult(productionUnit.id, e.message)
        }
    }

    /**
     * Analyze geometry for deforestation (pre-validation)
     */
    fun analyzeGeometry(geometry: Geometry, areaHectares: Double): DeforestationAnalysisResult {
        if (!satelliteImageryService.isAvailable()) {
            return createUnavailableResult("")
        }

        return try {
            val baselineStart = EUDR_CUTOFF.minusMonths(baselineMonths)
            val baselineEnd = EUDR_CUTOFF
            val baselineImage = satelliteImageryService.queryLandsat(geometry, baselineStart, baselineEnd)
            
            val recentStart = LocalDate.now().minusDays(recentDays)
            val recentEnd = LocalDate.now()
            val recentImage = satelliteImageryService.queryLandsat(geometry, recentStart, recentEnd)
            
            if (baselineImage == null || recentImage == null) {
                return createNoImageryResult("")
            }
            
            val ndviChange = recentImage.ndviMean - baselineImage.ndviMean
            val isDeforested = ndviChange < ndviChangeThreshold || recentImage.ndviMean < ndviDeforestationThreshold
            
            val deforestedAreaHa = if (isDeforested) {
                calculateDeforestedArea(areaHectares, ndviChange, ndviChangeThreshold)
            } else 0.0
            
            val severity = if (isDeforested) determineSeverity(deforestedAreaHa) else null
            
            DeforestationAnalysisResult(
                productionUnitId = "",
                analysisDate = LocalDateTime.now(),
                deforestedAreaHa = deforestedAreaHa,
                severity = severity,
                ndviChangeMean = ndviChange,
                ndviCurrent = recentImage.ndviMean,
                ndviBaseline = baselineImage.ndviMean,
                dataSource = "LANDSAT-8/9",
                analysisType = if (isDeforested) "NDVI_CHANGE_DETECTION" else "NO_DEFORESTATION",
                alertGenerated = isDeforested
            )
        } catch (e: Exception) {
            logger.error("Failed to analyze geometry with Landsat", e)
            createErrorResult("", e.message)
        }
    }

    private fun parseGeometry(productionUnit: ProductionUnit): Geometry {
        // Use parcelGeometry if available, otherwise parse from wgs84Coordinates
        if (productionUnit.parcelGeometry != null) {
            return productionUnit.parcelGeometry!!
        }
        
        // Fallback: parse wgs84Coordinates (assuming "lat,lon" format)
        val coords = productionUnit.wgs84Coordinates?.split(",")
            ?: throw IllegalArgumentException("No geometry or coordinates for production unit ${productionUnit.id}")
        
        val lat = coords[0].trim().toDouble()
        val lon = coords[1].trim().toDouble()
        
        val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
        val point = geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(lon, lat))
        
        return point.buffer(0.001)
    }

    private fun calculateDeforestedArea(totalAreaHa: Double, ndviChange: Double, threshold: Double): Double {
        val changeMagnitude = kotlin.math.abs(ndviChange / threshold)
        val proportionAffected = changeMagnitude.coerceAtMost(1.0)
        return totalAreaHa * proportionAffected
    }

    private fun determineSeverity(deforestedAreaHa: Double): DeforestationAlert.Severity {
        return when {
            deforestedAreaHa >= 5.0 -> DeforestationAlert.Severity.HIGH  // Map CRITICAL to HIGH
            deforestedAreaHa >= 2.0 -> DeforestationAlert.Severity.HIGH
            deforestedAreaHa >= 0.5 -> DeforestationAlert.Severity.MEDIUM
            else -> DeforestationAlert.Severity.LOW
        }
    }

    private fun createUnavailableResult(productionUnitId: String) = DeforestationAnalysisResult(
        productionUnitId = productionUnitId,
        analysisDate = LocalDateTime.now(),
        deforestedAreaHa = 0.0,
        severity = null,
        ndviChangeMean = 0.0,
        ndviCurrent = 0.0,
        ndviBaseline = 0.0,
        dataSource = "LANDSAT-8/9",
        analysisType = "UNAVAILABLE",
        alertGenerated = false
    )

    private fun createNoImageryResult(productionUnitId: String) = DeforestationAnalysisResult(
        productionUnitId = productionUnitId,
        analysisDate = LocalDateTime.now(),
        deforestedAreaHa = 0.0,
        severity = null,
        ndviChangeMean = 0.0,
        ndviCurrent = 0.0,
        ndviBaseline = 0.0,
        dataSource = "LANDSAT-8/9",
        analysisType = "NO_IMAGERY",
        alertGenerated = false
    )

    private fun createErrorResult(productionUnitId: String, errorMessage: String?) = DeforestationAnalysisResult(
        productionUnitId = productionUnitId,
        analysisDate = LocalDateTime.now(),
        deforestedAreaHa = 0.0,
        severity = null,
        ndviChangeMean = 0.0,
        ndviCurrent = 0.0,
        ndviBaseline = 0.0,
        dataSource = "LANDSAT-8/9",
        analysisType = "ERROR",
        alertGenerated = false,
        errorMessage = errorMessage
    )
}
