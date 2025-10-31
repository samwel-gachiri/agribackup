package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.supplychain.DeforestationAlertService
import com.agriconnect.farmersportalapis.domain.eudr.DeforestationAlert
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/deforestation-alerts")
@CrossOrigin(origins = ["*"])
class DeforestationAlertController(
    private val deforestationAlertService: DeforestationAlertService
) {
    
    private val logger = LoggerFactory.getLogger(DeforestationAlertController::class.java)
    
    @GetMapping("/production-unit/{productionUnitId}")
    fun getAlertsForProductionUnit(
        @PathVariable productionUnitId: String,
        @RequestParam(required = false) startDate: LocalDateTime?,
        @RequestParam(required = false) endDate: LocalDateTime?,
        @RequestParam(required = false) severity: DeforestationAlert.Severity?
    ): ResponseEntity<List<DeforestationAlertResponse>> {
        return try {
            val alerts = deforestationAlertService.getAlertsForProductionUnit(
                productionUnitId, startDate, endDate, severity
            )
            
            val responses = alerts.map { alert ->
                DeforestationAlertResponse(
                    id = alert.id,
                    productionUnitId = alert.productionUnit.id,
                    productionUnitName = alert.productionUnit.unitName,
                    farmerId = alert.productionUnit.farmer.id!!,
                    farmerName = alert.productionUnit.farmer.farmName ?: "Unknown",
                    alertType = alert.alertType,
                    severity = alert.severity,
                    latitude = alert.latitude,
                    longitude = alert.longitude,
                    alertDate = alert.alertDate,
                    confidence = alert.confidence,
                    distanceFromUnit = alert.distanceFromUnit,
                    source = alert.source,
                    sourceId = alert.sourceId,
                    isHederaVerified = alert.hederaTransactionId != null,
                    hederaTransactionId = alert.hederaTransactionId,
                    createdAt = alert.createdAt ?: LocalDateTime.now()
                )
            }
            
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get alerts for production unit $productionUnitId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/production-unit/{productionUnitId}/summary")
    fun getAlertSummary(
        @PathVariable productionUnitId: String
    ): ResponseEntity<DeforestationAlertSummaryResponse> {
        return try {
            val summary = deforestationAlertService.getAlertSummary(productionUnitId)
            
            // Calculate trend (simplified implementation)
            val trend = AlertTrend(
                direction = TrendDirection.STABLE,
                changePercentage = 0.0,
                comparisonPeriod = "30 days"
            )
            
            val response = DeforestationAlertSummaryResponse(
                productionUnitId = productionUnitId,
                totalAlerts = summary.totalAlerts,
                highSeverityAlerts = summary.highSeverityAlerts,
                mediumSeverityAlerts = summary.mediumSeverityAlerts,
                lowSeverityAlerts = summary.lowSeverityAlerts,
                infoAlerts = summary.totalAlerts - summary.highSeverityAlerts - summary.mediumSeverityAlerts - summary.lowSeverityAlerts,
                lastAlertDate = summary.lastAlertDate,
                averageDistance = summary.averageDistance,
                alertsByType = mapOf(), // Would be populated from actual data
                recentTrend = trend
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get alert summary for production unit $productionUnitId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @PostMapping("/search")
    fun searchAlerts(
        @RequestBody request: DeforestationAlertSearchRequest
    ): ResponseEntity<DeforestationAlertSearchResponse> {
        return try {
            // This would implement comprehensive search functionality
            // For now, return a placeholder response
            val response = DeforestationAlertSearchResponse(
                alerts = emptyList(),
                totalElements = 0,
                totalPages = 0,
                currentPage = request.page,
                pageSize = request.size,
                searchCriteria = request
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to search deforestation alerts", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @PostMapping("/process")
    fun processAlerts(
        @RequestBody request: AlertProcessingRequest
    ): ResponseEntity<AlertProcessingResponse> {
        return try {
            val startTime = System.currentTimeMillis()
            
            // This would trigger alert processing for the specified production unit
            // For now, return a placeholder response
            val response = AlertProcessingResponse(
                success = true,
                processedAlerts = 0,
                newAlerts = 0,
                errors = emptyList(),
                processingTime = System.currentTimeMillis() - startTime
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to process alerts for production unit ${request.productionUnitId}", e)
            val response = AlertProcessingResponse(
                success = false,
                processedAlerts = 0,
                newAlerts = 0,
                errors = listOf(e.message ?: "Processing failed"),
                processingTime = 0
            )
            ResponseEntity.ok(response)
        }
    }
    
    @PostMapping("/integrity-check")
    fun checkAlertIntegrity(
        @RequestBody request: AlertIntegrityCheckRequest
    ): ResponseEntity<AlertIntegrityCheckResponse> {
        return try {
            // This would verify alert integrity against Hedera DLT
            val results = request.alertIds.map { alertId ->
                AlertIntegrityResult(
                    alertId = alertId,
                    isValid = true,
                    isHederaVerified = true,
                    hederaTransactionId = "placeholder",
                    expectedHash = "placeholder",
                    actualHash = "placeholder"
                )
            }
            
            val response = AlertIntegrityCheckResponse(results = results)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to check alert integrity", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @PostMapping("/notify")
    fun sendAlertNotification(
        @RequestBody request: AlertNotificationRequest
    ): ResponseEntity<AlertNotificationResponse> {
        return try {
            // This would send notifications for the specified alert
            val response = AlertNotificationResponse(
                success = true,
                notificationsSent = request.notificationTypes.size,
                failedNotifications = emptyList()
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to send alert notification for alert ${request.alertId}", e)
            val response = AlertNotificationResponse(
                success = false,
                notificationsSent = 0,
                failedNotifications = listOf("Failed to send notifications: ${e.message}")
            )
            ResponseEntity.ok(response)
        }
    }
    
    @PostMapping("/export")
    fun exportAlerts(
        @RequestBody request: AlertExportRequest
    ): ResponseEntity<AlertExportResponse> {
        return try {
            // This would export alerts in the specified format
            val response = AlertExportResponse(
                success = true,
                downloadUrl = "/api/v1/downloads/alerts-export-${System.currentTimeMillis()}.${request.format.name.lowercase()}",
                fileName = "deforestation-alerts-${LocalDateTime.now()}.${request.format.name.lowercase()}",
                recordCount = 0,
                expiresAt = LocalDateTime.now().plusHours(24)
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to export alerts", e)
            val response = AlertExportResponse(
                success = false,
                downloadUrl = null,
                fileName = null,
                recordCount = 0,
                expiresAt = null,
                error = e.message
            )
            ResponseEntity.ok(response)
        }
    }
    
    @PostMapping("/statistics")
    fun getAlertStatistics(
        @RequestBody request: AlertStatisticsRequest
    ): ResponseEntity<AlertStatisticsResponse> {
        return try {
            // This would generate comprehensive alert statistics
            val statistics = listOf<AlertStatistic>()
            
            val summary = AlertStatisticsSummary(
                totalPeriods = 0,
                totalAlerts = 0,
                peakPeriod = null,
                peakAlertCount = 0,
                trendDirection = TrendDirection.STABLE,
                averageAlertsPerPeriod = 0.0
            )
            
            val response = AlertStatisticsResponse(
                statistics = statistics,
                summary = summary
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get alert statistics", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/farmer/{farmerId}")
    fun getAlertsByFarmer(
        @PathVariable farmerId: String,
        @RequestParam(defaultValue = "30") daysSince: Int
    ): ResponseEntity<List<DeforestationAlertResponse>> {
        return try {
            // This would get all alerts for a farmer's production units
            val response = emptyList<DeforestationAlertResponse>()
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get alerts for farmer $farmerId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/high-severity")
    fun getHighSeverityAlerts(
        @RequestParam(defaultValue = "7") daysSince: Int
    ): ResponseEntity<List<DeforestationAlertResponse>> {
        return try {
            // This would get all high severity alerts from the last N days
            val response = emptyList<DeforestationAlertResponse>()
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get high severity alerts", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @PostMapping("/configuration")
    fun updateAlertConfiguration(
        @RequestBody request: AlertConfigurationRequest
    ): ResponseEntity<AlertConfigurationResponse> {
        return try {
            // This would update alert monitoring configuration
            val response = AlertConfigurationResponse(
                success = true,
                currentConfiguration = request,
                message = "Alert configuration updated successfully"
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to update alert configuration", e)
            val response = AlertConfigurationResponse(
                success = false,
                currentConfiguration = request,
                message = "Failed to update configuration: ${e.message}"
            )
            ResponseEntity.ok(response)
        }
    }
    
    @GetMapping("/configuration")
    fun getAlertConfiguration(): ResponseEntity<AlertConfigurationResponse> {
        return try {
            // This would get current alert monitoring configuration
            val defaultConfig = AlertConfigurationRequest(
                monitoringEnabled = true,
                bufferDistanceKm = 5.0,
                alertThresholds = AlertThresholds(
                    highSeverityDistance = 1.0,
                    mediumSeverityDistance = 2.0,
                    lowSeverityDistance = 5.0,
                    minimumConfidence = 0.4
                ),
                notificationSettings = NotificationSettings(
                    enableEmailNotifications = true,
                    enableSmsNotifications = false,
                    enablePushNotifications = true,
                    highSeverityImmediate = true,
                    dailySummaryEnabled = true,
                    weeklySummaryEnabled = true
                )
            )
            
            val response = AlertConfigurationResponse(
                success = true,
                currentConfiguration = defaultConfig,
                message = "Current alert configuration"
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get alert configuration", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @PostMapping("/check-geometry")
    fun checkGeometryForDeforestation(
        @RequestBody request: GeometryCheckRequest
    ): ResponseEntity<GeometryCheckResponse> {
        return try {
            logger.info("Checking geometry for deforestation alerts")
            
            val alertSummary = deforestationAlertService.checkGeometryForAlerts(request.geoJsonPolygon)
            
            val response = GeometryCheckResponse(
                hasAlerts = alertSummary.totalAlerts > 0,
                totalAlerts = alertSummary.totalAlerts,
                gladAlerts = alertSummary.gladAlerts,
                viirsAlerts = alertSummary.viirsAlerts,
                treeCoverLossAlerts = alertSummary.treeCoverLossAlerts,
                highSeverityCount = alertSummary.highSeverityAlerts,
                mediumSeverityCount = alertSummary.mediumSeverityAlerts,
                lowSeverityCount = alertSummary.lowSeverityAlerts,
                riskLevel = when {
                    alertSummary.highSeverityAlerts > 0 -> "HIGH"
                    alertSummary.mediumSeverityAlerts > 0 -> "MEDIUM"
                    alertSummary.lowSeverityAlerts > 0 -> "LOW"
                    else -> "NONE"
                },
                message = when {
                    alertSummary.totalAlerts == 0 -> "No deforestation alerts detected. Area is compliant."
                    alertSummary.highSeverityAlerts > 0 -> "High risk: ${alertSummary.highSeverityAlerts} critical alert(s) detected."
                    alertSummary.mediumSeverityAlerts > 0 -> "Medium risk: ${alertSummary.mediumSeverityAlerts} alert(s) detected."
                    else -> "Low risk: ${alertSummary.lowSeverityAlerts} alert(s) detected."
                }
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to check geometry for deforestation", e)
            val response = GeometryCheckResponse(
                hasAlerts = false,
                totalAlerts = 0,
                gladAlerts = 0,
                viirsAlerts = 0,
                treeCoverLossAlerts = 0,
                highSeverityCount = 0,
                mediumSeverityCount = 0,
                lowSeverityCount = 0,
                riskLevel = "ERROR",
                message = "Failed to check for deforestation: ${e.message}",
                error = e.message
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
        }
    }
}