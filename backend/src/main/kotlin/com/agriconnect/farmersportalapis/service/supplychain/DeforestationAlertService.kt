package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.domain.eudr.DeforestationAlert
import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import com.agriconnect.farmersportalapis.infrastructure.feign.GfwQueryRequest
import com.agriconnect.farmersportalapis.infrastructure.feign.GlobalForestWatchClient
import com.agriconnect.farmersportalapis.infrastructure.repositories.DeforestationAlertRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProductionUnitRepository
import com.agriconnect.farmersportalapis.service.hedera.HederaConsensusServices
import com.fasterxml.jackson.databind.ObjectMapper
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

@Service
@Transactional
class DeforestationAlertService(
    private val deforestationAlertRepository: DeforestationAlertRepository,
    private val productionUnitRepository: ProductionUnitRepository,
    private val hederaConsensusService: HederaConsensusServices,
    private val objectMapper: ObjectMapper,
    private val gfwClient: GlobalForestWatchClient
) {

    private val logger = LoggerFactory.getLogger(DeforestationAlertService::class.java)
    private val geometryFactory = GeometryFactory()

    @Value("\${gfw.api.base-url:https://data-api.globalforestwatch.org}")
    private lateinit var gfwApiBaseUrl: String

    @Value("\${gfw.api.key:}")
    private lateinit var gfwApiKey: String

    @Value("\${deforestation.monitoring.enabled:true}")
    private var monitoringEnabled: Boolean = true

    @Value("\${deforestation.buffer.distance.km:5.0}")
    private var bufferDistanceKm: Double = 5.0

    companion object {
        private const val WGS84_SRID = 4326
        // GFW Data API v3 endpoints and dataset IDs
        private const val GLAD_DATASET_ID = "umd_glad_landsat_alerts"
        private const val GLAD_VERSION = "latest"
        private const val VIIRS_DATASET_ID = "nasa_viirs_fire_alerts"
        private const val VIIRS_VERSION = "latest"
        private const val TREE_COVER_LOSS_DATASET_ID = "umd_tree_cover_loss"
        private const val TREE_COVER_LOSS_VERSION = "v1.11"
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }

    /**
     * Scheduled task to fetch and process deforestation alerts every 6 hours
     */
    @Scheduled(fixedRate = 21600000) // 6 hours in milliseconds
    fun processScheduledDeforestationAlerts() {
        if (!monitoringEnabled) {
            logger.debug("Deforestation monitoring is disabled")
            return
        }

        logger.info("Starting scheduled deforestation alert processing")

        try {
            val productionUnits = productionUnitRepository.findAll()
            logger.info("Processing deforestation alerts for ${productionUnits.size} production units")

            productionUnits.forEach { unit ->
                processDeforestationAlertsForUnit(unit)
            }

            logger.info("Completed scheduled deforestation alert processing")
        } catch (e: Exception) {
            logger.error("Failed to process scheduled deforestation alerts", e)
        }
    }

    /**
     * Process deforestation alerts for a specific production unit
     */
    @Async
    fun processDeforestationAlertsForUnit(productionUnit: ProductionUnit): CompletableFuture<List<DeforestationAlert>> {
        return CompletableFuture.supplyAsync {
            try {
                logger.info("Processing deforestation alerts for production unit ${productionUnit.id}")

                val alerts = mutableListOf<DeforestationAlert>()

                // Fetch GLAD alerts
                val gladAlerts = fetchGladAlerts(productionUnit)
                alerts.addAll(gladAlerts)

                // Fetch VIIRS fire alerts
                val viirsAlerts = fetchViirsAlerts(productionUnit)
                alerts.addAll(viirsAlerts)

                // Fetch tree loss data
                val treeLossAlerts = fetchTreeLossAlerts(productionUnit)
                alerts.addAll(treeLossAlerts)

                // Process and save alerts
                val processedAlerts = alerts.map { alert ->
                    processAndSaveAlert(alert, productionUnit)
                }.filterNotNull()

                logger.info("Processed ${processedAlerts.size} deforestation alerts for unit ${productionUnit.id}")
                processedAlerts
            } catch (e: Exception) {
                logger.error("Failed to process deforestation alerts for unit ${productionUnit.id}", e)
                emptyList()
            }
        }
    }

    /**
     * Check a geometry for deforestation alerts without saving them
     * Used for pre-validation before creating a production unit
     */
    fun checkGeometryForAlerts(geoJsonPolygon: String): AlertSummary {
        return try {
            logger.info("Checking geometry for deforestation alerts")
            
            // Parse GeoJSON and create temporary geometry
            val jsonNode = objectMapper.readTree(geoJsonPolygon)
            val coordinates = jsonNode.get("coordinates")
            
            // Create a temporary geometry from GeoJSON
            val geometry = parseGeoJsonToGeometry(geoJsonPolygon)
            
            // Fetch alerts from all sources
            // global land analysis and discovery alerts
            val gladAlerts = fetchGladAlertsForGeometry(geometry, geoJsonPolygon)

            val viirsAlerts = fetchViirsAlertsForGeometry(geometry, geoJsonPolygon)
            val treeLossAlerts = fetchTreeLossAlertsForGeometry(geometry, geoJsonPolygon)
            
            val totalAlerts = gladAlerts + viirsAlerts + treeLossAlerts
            
            // Calculate severity counts (simplified - based on confidence or distance)
            val highSeverity = (gladAlerts + viirsAlerts + treeLossAlerts) / 3 // This is simplified
            val mediumSeverity = totalAlerts - highSeverity
            val lowSeverity = 0
            
            logger.info("Geometry check complete: $totalAlerts total alerts (GLAD: $gladAlerts, VIIRS: $viirsAlerts, Tree Loss: $treeLossAlerts)")
            
            AlertSummary(
                totalAlerts = totalAlerts,
                gladAlerts = gladAlerts,
                viirsAlerts = viirsAlerts,
                treeCoverLossAlerts = treeLossAlerts,
                highSeverityAlerts = highSeverity,
                mediumSeverityAlerts = mediumSeverity,
                lowSeverityAlerts = lowSeverity,
                lastAlertDate = if (totalAlerts > 0) LocalDateTime.now() else null,
                averageDistance = 0.0 // Not calculated for preview
            )
        } catch (e: Exception) {
            logger.error("Failed to check geometry for alerts", e)
            // Return empty summary on error
            AlertSummary(
                totalAlerts = 0,
                gladAlerts = 0,
                viirsAlerts = 0,
                treeCoverLossAlerts = 0,
                highSeverityAlerts = 0,
                mediumSeverityAlerts = 0,
                lowSeverityAlerts = 0,
                lastAlertDate = null,
                averageDistance = 0.0
            )
        }
    }
    
    /**
     * Parse GeoJSON string to JTS Geometry
     */
    private fun parseGeoJsonToGeometry(geoJsonString: String): Geometry {
        val jsonNode = objectMapper.readTree(geoJsonString)
        val coordinatesNode = jsonNode.get("coordinates")
        
        // Extract the first ring (outer boundary) from the polygon
        val firstRing = coordinatesNode.get(0)
        
        val coordinates = mutableListOf<Coordinate>()
        firstRing.forEach { coordNode ->
            val lon = coordNode.get(0).asDouble()
            val lat = coordNode.get(1).asDouble()
            coordinates.add(Coordinate(lon, lat))
        }
        
        val polygon = geometryFactory.createPolygon(coordinates.toTypedArray())
        polygon.srid = WGS84_SRID
        
        return polygon
    }

    /**
     * Fetch GLAD alerts count for a geometry without saving
     */
    private fun fetchGladAlertsForGeometry(geometry: Geometry, geoJson: String): Int {
        return try {
            val sql = """
                SELECT COUNT(*) as alert_count
                FROM data 
                WHERE ST_Intersects(geometry, ST_GeomFromGeoJSON('$geoJson'))
                AND alert_date >= '${LocalDateTime.now().minusDays(30).format(DATE_FORMATTER)}'
            """.trimIndent()

            logger.debug("Fetching GLAD alerts count with SQL: $sql")
            
            val response = gfwClient.queryDataset(
                datasetId = GLAD_DATASET_ID,
                version = GLAD_VERSION,
                apiKey = gfwApiKey,
                request = GfwQueryRequest(sql)
            )
            
            logger.debug("GLAD response: ${response.data?.size ?: 0} rows")
            
            response.data?.get(0)?.get("alert_count")?.toString()?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            logger.warn("Failed to fetch GLAD alerts count for geometry check", e)
            0
        }
    }

    /**
     * Fetch VIIRS alerts count for a geometry without saving
     */
    private fun fetchViirsAlertsForGeometry(geometry: Geometry, geoJson: String): Int {
        return try {
            val sql = """
                SELECT COUNT(*) as alert_count
                FROM data 
                WHERE ST_Intersects(geometry, ST_GeomFromGeoJSON('$geoJson'))
                AND alert_date >= '${LocalDateTime.now().minusDays(7).format(DATE_FORMATTER)}'
            """.trimIndent()

            logger.debug("Fetching VIIRS alerts count with SQL: $sql")
            
            val response = gfwClient.queryDataset(
                datasetId = VIIRS_DATASET_ID,
                version = VIIRS_VERSION,
                apiKey = gfwApiKey,
                request = GfwQueryRequest(sql)
            )
            
            logger.debug("VIIRS response: ${response.data?.size ?: 0} rows")
            
            response.data?.get(0)?.get("alert_count")?.toString()?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            logger.warn("Failed to fetch VIIRS alerts count for geometry check", e)
            0
        }
    }

    /**
     * Fetch tree cover loss alerts count for a geometry without saving
     */
    private fun fetchTreeLossAlertsForGeometry(geometry: Geometry, geoJson: String): Int {
        return try {
            val sql = """
                SELECT COUNT(*) as alert_count
                FROM data 
                WHERE ST_Intersects(geometry, ST_GeomFromGeoJSON('$geoJson'))
                AND umd_tree_cover_loss__year >= ${LocalDateTime.now().year - 1}
            """.trimIndent()

            logger.debug("Fetching tree cover loss alerts count with SQL: $sql")
            
            val response = gfwClient.queryDataset(
                datasetId = TREE_COVER_LOSS_DATASET_ID,
                version = TREE_COVER_LOSS_VERSION,
                apiKey = gfwApiKey,
                request = GfwQueryRequest(sql)
            )
            
            logger.debug("Tree cover loss response: ${response.data?.size ?: 0} rows")
            
            response.data?.get(0)?.get("alert_count")?.toString()?.toIntOrNull() ?: 0
        } catch (e: Exception) {
            logger.warn("Failed to fetch tree cover loss alerts count for geometry check", e)
            0
        }
    }


    /**
     * Fetch GLAD (Global Land Analysis and Discovery) alerts
     * Using GFW Data API v3: https://data-api.globalforestwatch.org/dataset/{dataset_id}/{version}/query
     */
    private fun fetchGladAlerts(productionUnit: ProductionUnit): List<DeforestationAlert> {
        return try {
            val geometry = productionUnit.parcelGeometry ?: run {
                logger.warn("No geometry for production unit ${productionUnit.id}")
                return emptyList()
            }
            val geoJson = convertGeometryToGeoJson(geometry)

            // Build SQL query to get alerts within the geometry
            val sql = """
                SELECT latitude, longitude, confidence, alert_date 
                FROM data 
                WHERE ST_Intersects(geometry, ST_GeomFromGeoJSON('$geoJson'))
                AND alert_date >= '${LocalDateTime.now().minusDays(30).format(DATE_FORMATTER)}'
                ORDER BY alert_date DESC
                LIMIT 100
            """.trimIndent()

            logger.debug("GLAD SQL Query: $sql")
            logger.debug("GeoJSON: $geoJson")

            val response = gfwClient.queryDataset(
                datasetId = GLAD_DATASET_ID,
                version = GLAD_VERSION,
                apiKey = gfwApiKey,
                request = GfwQueryRequest(sql)
            )
            
            logger.debug("GLAD API Response: ${response.data?.size ?: 0} alerts")

            parseGladAlertsResponse(response, productionUnit)
        } catch (e: Exception) {
            logger.error("Failed to fetch GLAD alerts for unit ${productionUnit.id}", e)
            emptyList()
        }
    }

    /**
     * Fetch VIIRS fire alerts
     * Using GFW Data API v3
     */
    private fun fetchViirsAlerts(productionUnit: ProductionUnit): List<DeforestationAlert> {
        return try {
            val geometry = productionUnit.parcelGeometry ?: return emptyList()
            val geoJson = convertGeometryToGeoJson(geometry)

            val sql = """
                SELECT latitude, longitude, confidence, alert_date, bright_ti4
                FROM data 
                WHERE ST_Intersects(geometry, ST_GeomFromGeoJSON('$geoJson'))
                AND alert_date >= '${LocalDateTime.now().minusDays(7).format(DATE_FORMATTER)}'
                ORDER BY alert_date DESC
                LIMIT 100
            """.trimIndent()

            logger.debug("VIIRS SQL Query: $sql")

            val response = gfwClient.queryDataset(
                datasetId = VIIRS_DATASET_ID,
                version = VIIRS_VERSION,
                apiKey = gfwApiKey,
                request = GfwQueryRequest(sql)
            )
            
            logger.debug("VIIRS API Response: ${response.data?.size ?: 0} alerts")

            parseViirsAlertsResponse(response, productionUnit)
        } catch (e: Exception) {
            logger.error("Failed to fetch VIIRS alerts for unit ${productionUnit.id}", e)
            emptyList()
        }
    }

    /**
     * Fetch tree cover loss data
     * Using GFW Data API v3
     */
    private fun fetchTreeLossAlerts(productionUnit: ProductionUnit): List<DeforestationAlert> {
        return try {
            val geometry = productionUnit.parcelGeometry ?: return emptyList()
            val geoJson = convertGeometryToGeoJson(geometry)
            val currentYear = LocalDateTime.now().year

            val sql = """
                SELECT umd_tree_cover_loss__year, umd_tree_cover_loss__ha
                FROM data 
                WHERE ST_Intersects(geometry, ST_GeomFromGeoJSON('$geoJson'))
                AND umd_tree_cover_loss__year >= ${currentYear - 2}
                ORDER BY umd_tree_cover_loss__year DESC
            """.trimIndent()

            logger.debug("Tree cover loss SQL Query: $sql")

            val response = gfwClient.queryDataset(
                datasetId = TREE_COVER_LOSS_DATASET_ID,
                version = TREE_COVER_LOSS_VERSION,
                apiKey = gfwApiKey,
                request = GfwQueryRequest(sql)
            )
            
            logger.debug("Tree cover loss API Response: ${response.data?.size ?: 0} alerts")

            parseTreeLossResponse(response, productionUnit)
        } catch (e: Exception) {
            logger.error("Failed to fetch tree loss data for unit ${productionUnit.id}", e)
            emptyList()
        }
    }

    private fun parseGladAlertsResponse(response: com.agriconnect.farmersportalapis.infrastructure.feign.GfwQueryResponse, productionUnit: ProductionUnit): List<DeforestationAlert> {
        if (response.data.isNullOrEmpty()) return emptyList()

        return try {
            val alerts = mutableListOf<DeforestationAlert>()

            response.data.forEach { alertData ->
                val alert = createDeforestationAlertFromMap(
                    alertData = alertData,
                    alertType = DeforestationAlert.AlertType.GLAD_DEFORESTATION,
                    productionUnit = productionUnit,
                    source = "Global Forest Watch GLAD"
                )
                if (alert != null) alerts.add(alert)
            }

            alerts
        } catch (e: Exception) {
            logger.error("Failed to parse GLAD alerts response", e)
            emptyList()
        }
    }

    private fun parseViirsAlertsResponse(response: com.agriconnect.farmersportalapis.infrastructure.feign.GfwQueryResponse, productionUnit: ProductionUnit): List<DeforestationAlert> {
        if (response.data.isNullOrEmpty()) return emptyList()

        return try {
            val alerts = mutableListOf<DeforestationAlert>()

            response.data.forEach { alertData ->
                val alert = createDeforestationAlertFromMap(
                    alertData = alertData,
                    alertType = DeforestationAlert.AlertType.FIRE_ALERT,
                    productionUnit = productionUnit,
                    source = "VIIRS Fire Alerts"
                )
                if (alert != null) alerts.add(alert)
            }

            alerts
        } catch (e: Exception) {
            logger.error("Failed to parse VIIRS alerts response", e)
            emptyList()
        }
    }

    private fun parseTreeLossResponse(response: com.agriconnect.farmersportalapis.infrastructure.feign.GfwQueryResponse, productionUnit: ProductionUnit): List<DeforestationAlert> {
        if (response.data.isNullOrEmpty()) return emptyList()

        return try {
            val alerts = mutableListOf<DeforestationAlert>()

            response.data.forEach { alertData ->
                val alert = createDeforestationAlertFromMap(
                    alertData = alertData,
                    alertType = DeforestationAlert.AlertType.TREE_LOSS,
                    productionUnit = productionUnit,
                    source = "Global Forest Watch Tree Loss"
                )
                if (alert != null) alerts.add(alert)
            }

            alerts
        } catch (e: Exception) {
            logger.error("Failed to parse tree loss response", e)
            emptyList()
        }
    }

    private fun createDeforestationAlertFromMap(
        alertData: Map<String, Any>,
        alertType: DeforestationAlert.AlertType,
        productionUnit: ProductionUnit,
        source: String
    ): DeforestationAlert? {
        return try {
            val latitude = (alertData["latitude"] ?: alertData["lat"])?.toString()?.toDoubleOrNull() ?: return null
            val longitude = (alertData["longitude"] ?: alertData["lng"])?.toString()?.toDoubleOrNull() ?: return null
            val confidence = (alertData["confidence"])?.toString()?.toDoubleOrNull() ?: 0.0
            val alertDate = (alertData["alert_date"] ?: alertData["date"])?.toString()?.let {
                try {
                    LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (e: Exception) {
                    LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                }
            } ?: LocalDateTime.now()

            // Create point geometry for alert location
            val alertPoint = geometryFactory.createPoint(Coordinate(longitude, latitude))
            alertPoint.srid = WGS84_SRID

            // Calculate distance from production unit
            val distance = calculateDistance(productionUnit.parcelGeometry!!, alertPoint)

            // Determine severity based on distance and confidence
            val severity = calculateAlertSeverity(distance, confidence, alertType)

            DeforestationAlert(
                productionUnit = productionUnit,
                alertType = alertType,
                alertGeometry = alertPoint,
                latitude = BigDecimal(latitude),
                longitude = BigDecimal(longitude),
                alertDate = alertDate,
                confidence = BigDecimal(confidence),
                severity = severity,
                distanceFromUnit = BigDecimal(distance),
                source = source,
                sourceId = alertData["id"]?.toString(),
                metadata = objectMapper.writeValueAsString(alertData),
                hederaTransactionId = null,
                hederaHash = null,
                createdAt = LocalDateTime.now()
            )
        } catch (e: Exception) {
            logger.error("Failed to create deforestation alert from map", e)
            null
        }
    }

    private fun processAndSaveAlert(alert: DeforestationAlert, productionUnit: ProductionUnit): DeforestationAlert? {
        return try {
            // Check if alert already exists
            val existingAlert = deforestationAlertRepository.findBySourceIdAndProductionUnit(
                alert.sourceId ?: "",
                productionUnit
            )

            if (existingAlert != null) {
                logger.debug("Alert already exists: ${alert.sourceId}")
                return existingAlert
            }

            // Save alert to database
            val savedAlert = deforestationAlertRepository.save(alert)

            // Record on Hedera DLT
            try {
                val hederaTransactionId = hederaConsensusService.recordDeforestationAlert(savedAlert)
                savedAlert.hederaTransactionId = hederaTransactionId
                savedAlert.hederaHash = calculateAlertHash(savedAlert)
                deforestationAlertRepository.save(savedAlert)
            } catch (e: Exception) {
                logger.warn("Failed to record deforestation alert on Hedera DLT", e)
            }

            // Trigger notifications for high severity alerts
            if (savedAlert.severity == DeforestationAlert.Severity.HIGH) {
                triggerHighSeverityAlertNotification(savedAlert)
            }

            logger.info("Processed and saved deforestation alert ${savedAlert.id} for unit ${productionUnit.id}")
            savedAlert
        } catch (e: Exception) {
            logger.error("Failed to process and save deforestation alert", e)
            null
        }
    }

    private fun calculateBoundingBox(geometry: Geometry, bufferKm: Double): List<Double> {
        val envelope = geometry.envelopeInternal

        // Convert km to degrees (rough approximation)
        val bufferDegrees = bufferKm / 111.0

        return listOf(
            envelope.minX - bufferDegrees, // min longitude
            envelope.minY - bufferDegrees, // min latitude
            envelope.maxX + bufferDegrees, // max longitude
            envelope.maxY + bufferDegrees  // max latitude
        )
    }

    /**
     * Convert JTS Geometry to GeoJSON string for GFW API queries
     */
    private fun convertGeometryToGeoJson(geometry: Geometry): String {
        return try {
            val geoJsonWriter = org.locationtech.jts.io.geojson.GeoJsonWriter()
            geoJsonWriter.write(geometry)
        } catch (e: Exception) {
            logger.error("Failed to convert geometry to GeoJSON", e)
            // Return a simple point as fallback
            val centroid = geometry.centroid
            """{"type":"Point","coordinates":[${centroid.x},${centroid.y}]}"""
        }
    }

    private fun calculateDistance(geometry: Geometry, point: Point): Double {
        // Calculate distance in kilometers using Haversine formula
        val centroid = geometry.centroid
        val lat1 = Math.toRadians(centroid.y)
        val lon1 = Math.toRadians(centroid.x)
        val lat2 = Math.toRadians(point.y)
        val lon2 = Math.toRadians(point.x)

        val dlat = lat2 - lat1
        val dlon = lon2 - lon1

        val a = Math.sin(dlat / 2) * Math.sin(dlat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dlon / 2) * Math.sin(dlon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return 6371.0 * c // Earth radius in kilometers
    }

    private fun calculateAlertSeverity(
        distance: Double,
        confidence: Double,
        alertType: DeforestationAlert.AlertType
    ): DeforestationAlert.Severity {
        return when {
            distance <= 1.0 && confidence >= 0.8 -> DeforestationAlert.Severity.HIGH
            distance <= 2.0 && confidence >= 0.6 -> DeforestationAlert.Severity.MEDIUM
            distance <= 5.0 && confidence >= 0.4 -> DeforestationAlert.Severity.LOW
            else -> DeforestationAlert.Severity.INFO
        }
    }

    private fun calculateAlertHash(alert: DeforestationAlert): String {
        val alertData = "${alert.alertType}_${alert.latitude}_${alert.longitude}_${alert.alertDate}_${alert.confidence}"
        return MessageDigest.getInstance("SHA-256")
            .digest(alertData.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    private fun triggerHighSeverityAlertNotification(alert: DeforestationAlert) {
        // This would integrate with notification service
        logger.warn("HIGH SEVERITY deforestation alert detected for production unit ${alert.productionUnit.id}: ${alert.alertType} at distance ${alert.distanceFromUnit}km")

        // TODO: Implement actual notification logic
        // - Email notifications to farmer and supervisors
        // - SMS alerts for critical alerts
        // - Dashboard notifications
        // - Regulatory authority notifications if required
    }

    /**
     * Get deforestation alerts for a specific production unit
     */
    fun getAlertsForProductionUnit(
        productionUnitId: String,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null,
        severity: DeforestationAlert.Severity? = null
    ): List<DeforestationAlert> {
        return deforestationAlertRepository.findByProductionUnitIdAndFilters(
            productionUnitId,
            startDate ?: LocalDateTime.now().minusMonths(3),
            endDate ?: LocalDateTime.now(),
            severity
        )
    }

    /**
     * Get summary statistics for deforestation alerts
     */
    fun getAlertSummary(productionUnitId: String): AlertSummary {
        val alerts = deforestationAlertRepository.findByProductionUnitId(productionUnitId)

        return AlertSummary(
            totalAlerts = alerts.size,
            highSeverityAlerts = alerts.count { it.severity == DeforestationAlert.Severity.HIGH },
            mediumSeverityAlerts = alerts.count { it.severity == DeforestationAlert.Severity.MEDIUM },
            lowSeverityAlerts = alerts.count { it.severity == DeforestationAlert.Severity.LOW },
            lastAlertDate = alerts.maxByOrNull { it.alertDate }?.alertDate,
            averageDistance = alerts.map { it.distanceFromUnit.toDouble() }.average().takeIf { !it.isNaN() } ?: 0.0
        )
    }

    /**
     * Get alert trends over time
     */
    fun getAlertTrends(
        productionUnitIds: List<String>? = null,
        timeRange: String = "30d"
    ): AlertTrendsResult {
        val endDate = LocalDateTime.now()
        val startDate = when (timeRange) {
            "7d" -> endDate.minusDays(7)
            "30d" -> endDate.minusDays(30)
            "90d" -> endDate.minusDays(90)
            else -> endDate.minusDays(30)
        }

        // This would implement actual trend calculation
        // For now, return placeholder data
        return AlertTrendsResult(
            timeRange = timeRange,
            totalAlerts = 0,
            trendDirection = "stable",
            changePercentage = 0.0,
            dailyBreakdown = emptyList()
        )
    }

    /**
     * Get alert statistics by severity
     */
    fun getAlertStatisticsBySeverity(
        productionUnitId: String? = null,
        startDate: LocalDateTime? = null,
        endDate: LocalDateTime? = null
    ): Map<String, Int> {
        val alerts = if (productionUnitId != null) {
            deforestationAlertRepository.findByProductionUnitIdAndFilters(
                productionUnitId,
                startDate ?: LocalDateTime.now().minusMonths(1),
                endDate ?: LocalDateTime.now(),
                null
            )
        } else {
            deforestationAlertRepository.findAll()
        }

        return mapOf(
            "HIGH" to alerts.count { it.severity == DeforestationAlert.Severity.HIGH },
            "MEDIUM" to alerts.count { it.severity == DeforestationAlert.Severity.MEDIUM },
            "LOW" to alerts.count { it.severity == DeforestationAlert.Severity.LOW },
            "INFO" to alerts.count { it.severity == DeforestationAlert.Severity.INFO }
        )
    }

    /**
     * Mark alert as reviewed
     */
    fun markAlertAsReviewed(alertId: String, reviewerId: String, reviewNotes: String? = null): DeforestationAlert {
        val alert = deforestationAlertRepository.findById(alertId)
            .orElseThrow { IllegalArgumentException("Alert not found: $alertId") }

        alert.isReviewed = true
        alert.reviewedAt = LocalDateTime.now()
        alert.reviewerId = reviewerId
        alert.reviewNotes = reviewNotes

        return deforestationAlertRepository.save(alert)
    }

    /**
     * Get unreviewed alerts count
     */
    fun getUnreviewedAlertsCount(productionUnitId: String? = null): Long {
        return if (productionUnitId != null) {
            deforestationAlertRepository.countByProductionUnitIdAndIsReviewedFalse(productionUnitId)
        } else {
            deforestationAlertRepository.countByIsReviewedFalse()
        }
    }

    data class AlertSummary(
        val totalAlerts: Int,
        val highSeverityAlerts: Int,
        val mediumSeverityAlerts: Int,
        val lowSeverityAlerts: Int,
        val lastAlertDate: LocalDateTime?,
        val averageDistance: Double,
        val gladAlerts: Int = 0,
        val viirsAlerts: Int = 0,
        val treeCoverLossAlerts: Int = 0
    )

    data class AlertTrendsResult(
        val timeRange: String,
        val totalAlerts: Int,
        val trendDirection: String,
        val changePercentage: Double,
        val dailyBreakdown: List<DailyAlertCount>
    )

    data class DailyAlertCount(
        val date: LocalDateTime,
        val count: Int,
        val severityBreakdown: Map<String, Int>
    )
}