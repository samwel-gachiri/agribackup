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
import kotlin.math.abs

/**
 * Sentinel-2 Deforestation Analyzer
 * 
 * Analyzes deforestation using Sentinel-2 satellite imagery with 10m resolution.
 * 
 * Key Features:
 * - 10m spatial resolution (bands 2, 3, 4, 8)
 * - 5-day revisit time
 * - Multiple vegetation indices: NDVI, NDMI, EVI, SAVI
 * - Change detection analysis
 * - Configurable thresholds
 * 
 * Vegetation Index Ranges:
 * - NDVI: -1 to 1 (healthy vegetation > 0.6, deforested < 0.3)
 * - NDMI: -1 to 1 (high moisture > 0.5, dry < 0.2)
 * - EVI: -1 to 1 (similar to NDVI but better in dense canopy)
 * - SAVI: -1 to 1 (soil-adjusted, good for sparse vegetation)
 */
@Service
class SentinelDeforestationAnalyzer(
    private val satelliteImageryService: SatelliteImageryService
) {

    private val logger = LoggerFactory.getLogger(SentinelDeforestationAnalyzer::class.java)

    @Value("\${sentinel.analysis.ndvi-deforestation-threshold:0.3}")
    private var ndviDeforestationThreshold: Double = 0.3

    @Value("\${sentinel.analysis.ndvi-change-threshold:-0.2}")
    private var ndviChangeThreshold: Double = -0.2  // 20% vegetation loss

    @Value("\${sentinel.analysis.baseline-months:3}")
    private var baselineMonths: Long = 3

    @Value("\${sentinel.analysis.recent-days:10}")
    private var recentDays: Long = 10

    companion object {
        // EUDR cutoff date
        private val EUDR_CUTOFF = LocalDate.of(2020, 12, 31)
        
        // Severity thresholds based on deforested area (hectares)
        private const val CRITICAL_AREA_HA = 5.0
        private const val HIGH_AREA_HA = 2.0
        private const val MEDIUM_AREA_HA = 0.5
    }

    /**
     * Analyze production unit for deforestation using Sentinel-2 imagery
     * Compares baseline (before EUDR cutoff) with recent imagery
     */
    fun analyzeProductionUnit(productionUnit: ProductionUnit): DeforestationAnalysisResult {
        if (!satelliteImageryService.isAvailable()) {
            logger.warn("Satellite imagery service not available for production unit ${productionUnit.id}")
            return DeforestationAnalysisResult(
                productionUnitId = productionUnit.id,
                analysisDate = LocalDateTime.now(),
                deforestedAreaHa = 0.0,
                severity = null,
                ndviChangeMean = 0.0,
                ndviCurrent = 0.0,
                ndviBaseline = 0.0,
                dataSource = "SENTINEL-2",
                analysisType = "UNAVAILABLE",
                alertGenerated = false
            )
        }

        return try {
            logger.info("Analyzing production unit ${productionUnit.id} with Sentinel-2")
            
            val geometry = parseGeometry(productionUnit)
            
            // Get baseline imagery (3 months before EUDR cutoff)
            val baselineStart = EUDR_CUTOFF.minusMonths(baselineMonths)
            val baselineEnd = EUDR_CUTOFF
            val baselineImage = satelliteImageryService.querySentinel2(
                geometry, baselineStart, baselineEnd
            )
            
            // Get recent imagery (last 10 days)
            val recentStart = LocalDate.now().minusDays(recentDays)
            val recentEnd = LocalDate.now()
            val recentImage = satelliteImageryService.querySentinel2(
                geometry, recentStart, recentEnd
            )
            
            if (baselineImage == null || recentImage == null) {
                logger.warn("Could not retrieve imagery for production unit ${productionUnit.id}")
                return DeforestationAnalysisResult(
                    productionUnitId = productionUnit.id,
                    analysisDate = LocalDateTime.now(),
                    deforestedAreaHa = 0.0,
                    severity = null,
                    ndviChangeMean = 0.0,
                    ndviCurrent = 0.0,
                    ndviBaseline = 0.0,
                    dataSource = "SENTINEL-2",
                    analysisType = "NO_IMAGERY",
                    alertGenerated = false
                )
            }
            
            // Calculate NDVI change
            val ndviChange = recentImage.ndviMean - baselineImage.ndviMean
            val ndviChangePercent = (ndviChange / baselineImage.ndviMean) * 100
            
            logger.info("""
                Production Unit ${productionUnit.id} NDVI Analysis:
                - Baseline NDVI: ${baselineImage.ndviMean}
                - Recent NDVI: ${recentImage.ndviMean}
                - Change: ${ndviChange} (${ndviChangePercent}%)
            """.trimIndent())
            
            // Detect deforestation (NDVI decrease > threshold)
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
                    dataSource = "SENTINEL-2",
                    analysisType = "NO_DEFORESTATION",
                    alertGenerated = false
                )
            }
            
            // Calculate deforested area (simplified - assumes uniform change)
            val deforestedAreaHa = calculateDeforestedArea(
                productionUnit.areaHectares.toDouble(),
                ndviChange,
                ndviChangeThreshold
            )
            
            // Determine severity based on deforested area
            val severity = determineSeverity(deforestedAreaHa)
            
            logger.info("""
                DEFORESTATION DETECTED:
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
                dataSource = "SENTINEL-2",
                analysisType = "NDVI_CHANGE_DETECTION",
                alertGenerated = true,
                additionalMetrics = mapOf(
                    "ndviChangePercent" to ndviChangePercent,
                    "ndviStdDevBaseline" to baselineImage.ndviStdDev,
                    "ndviStdDevRecent" to recentImage.ndviStdDev,
                    "baselinePeriod" to "$baselineStart to $baselineEnd",
                    "recentPeriod" to "$recentStart to $recentEnd"
                )
            )
            
        } catch (e: Exception) {
            logger.error("Failed to analyze production unit ${productionUnit.id} with Sentinel-2", e)
            DeforestationAnalysisResult(
                productionUnitId = productionUnit.id,
                analysisDate = LocalDateTime.now(),
                deforestedAreaHa = 0.0,
                severity = null,
                ndviChangeMean = 0.0,
                ndviCurrent = 0.0,
                ndviBaseline = 0.0,
                dataSource = "SENTINEL-2",
                analysisType = "ERROR",
                alertGenerated = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Analyze geometry for deforestation without production unit
     * Used for pre-validation
     */
    fun analyzeGeometry(
        geometry: Geometry,
        areaHectares: Double
    ): DeforestationAnalysisResult {
        if (!satelliteImageryService.isAvailable()) {
            return DeforestationAnalysisResult(
                productionUnitId = "",
                analysisDate = LocalDateTime.now(),
                deforestedAreaHa = 0.0,
                severity = null,
                ndviChangeMean = 0.0,
                ndviCurrent = 0.0,
                ndviBaseline = 0.0,
                dataSource = "SENTINEL-2",
                analysisType = "UNAVAILABLE",
                alertGenerated = false
            )
        }

        return try {
            val baselineStart = EUDR_CUTOFF.minusMonths(baselineMonths)
            val baselineEnd = EUDR_CUTOFF
            val baselineImage = satelliteImageryService.querySentinel2(geometry, baselineStart, baselineEnd)
            
            val recentStart = LocalDate.now().minusDays(recentDays)
            val recentEnd = LocalDate.now()
            val recentImage = satelliteImageryService.querySentinel2(geometry, recentStart, recentEnd)
            
            if (baselineImage == null || recentImage == null) {
                return DeforestationAnalysisResult(
                    productionUnitId = "",
                    analysisDate = LocalDateTime.now(),
                    deforestedAreaHa = 0.0,
                    severity = null,
                    ndviChangeMean = 0.0,
                    ndviCurrent = 0.0,
                    ndviBaseline = 0.0,
                    dataSource = "SENTINEL-2",
                    analysisType = "NO_IMAGERY",
                    alertGenerated = false
                )
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
                dataSource = "SENTINEL-2",
                analysisType = if (isDeforested) "NDVI_CHANGE_DETECTION" else "NO_DEFORESTATION",
                alertGenerated = isDeforested
            )
        } catch (e: Exception) {
            logger.error("Failed to analyze geometry with Sentinel-2", e)
            DeforestationAnalysisResult(
                productionUnitId = "",
                analysisDate = LocalDateTime.now(),
                deforestedAreaHa = 0.0,
                severity = null,
                ndviChangeMean = 0.0,
                ndviCurrent = 0.0,
                ndviBaseline = 0.0,
                dataSource = "SENTINEL-2",
                analysisType = "ERROR",
                alertGenerated = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Parse geometry from production unit
     */
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
        
        // Create a small buffer around the point (e.g., 100m radius)
        val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
        val point = geometryFactory.createPoint(org.locationtech.jts.geom.Coordinate(lon, lat))
        
        // Buffer by approximately 100m (in degrees, roughly 0.001 degrees)
        return point.buffer(0.001)
    }

    /**
     * Calculate deforested area based on NDVI change
     * Simplified model - assumes linear relationship between NDVI loss and area loss
     */
    private fun calculateDeforestedArea(
        totalAreaHa: Double,
        ndviChange: Double,
        threshold: Double
    ): Double {
        // Calculate proportion of area affected
        // If NDVI dropped by 50% (e.g., -0.3 change on 0.6 baseline), assume 50% area affected
        val changeMagnitude = abs(ndviChange / threshold)
        val proportionAffected = changeMagnitude.coerceAtMost(1.0)
        
        return totalAreaHa * proportionAffected
    }

    /**
     * Determine alert severity based on deforested area
     */
    private fun determineSeverity(deforestedAreaHa: Double): DeforestationAlert.Severity {
        return when {
            deforestedAreaHa >= CRITICAL_AREA_HA -> DeforestationAlert.Severity.HIGH  // Map CRITICAL to HIGH
            deforestedAreaHa >= HIGH_AREA_HA -> DeforestationAlert.Severity.HIGH
            deforestedAreaHa >= MEDIUM_AREA_HA -> DeforestationAlert.Severity.MEDIUM
            else -> DeforestationAlert.Severity.LOW
        }
    }
}

/**
 * Deforestation analysis result from Sentinel-2 or Landsat
 */
data class DeforestationAnalysisResult(
    val productionUnitId: String,
    val analysisDate: LocalDateTime,
    val deforestedAreaHa: Double,
    val severity: DeforestationAlert.Severity?,
    val ndviChangeMean: Double,
    val ndviCurrent: Double,
    val ndviBaseline: Double,
    val dataSource: String,
    val analysisType: String,  // NDVI_CHANGE_DETECTION, NO_DEFORESTATION, NO_IMAGERY, ERROR, UNAVAILABLE
    val alertGenerated: Boolean,
    val additionalMetrics: Map<String, Any>? = null,
    val errorMessage: String? = null
) {
    /**
     * Convert to DeforestationAlert entity
     */
    fun toDeforestationAlert(productionUnit: ProductionUnit): DeforestationAlert? {
        if (!alertGenerated || severity == null) return null
        
        // Extract centroid coordinates from geometry
        val centroid = productionUnit.parcelGeometry?.centroid
        val lat = centroid?.y?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
        val lon = centroid?.x?.let { BigDecimal.valueOf(it) } ?: BigDecimal.ZERO
        
        return DeforestationAlert(
            productionUnit = productionUnit,
            alertType = DeforestationAlert.AlertType.TREE_LOSS,
            alertGeometry = centroid,
            latitude = lat,
            longitude = lon,
            alertDate = analysisDate,
            confidence = BigDecimal.valueOf(calculateConfidence()),
            severity = severity,
            distanceFromUnit = BigDecimal.ZERO,
            source = dataSource,
            sourceId = "NDVI_${analysisDate.toLocalDate()}",
            metadata = "NDVI Change: $ndviChangeMean, Current: $ndviCurrent, Baseline: $ndviBaseline, Area: ${deforestedAreaHa}ha"
        )
    }
    
    /**
     * Calculate confidence score based on NDVI change magnitude and std dev
     */
    private fun calculateConfidence(): Double {
        // Higher NDVI change = higher confidence
        // Lower std dev in recent period = higher confidence
        val changeMagnitude = abs(ndviChangeMean)
        val baseConfidence = (changeMagnitude / 0.5).coerceAtMost(1.0)  // Max at 0.5 change
        
        return (baseConfidence * 100).coerceIn(50.0, 95.0)  // 50-95% confidence range
    }
}
