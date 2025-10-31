package com.agriconnect.farmersportalapis.application.dtos

import com.agriconnect.farmersportalapis.domain.eudr.DeforestationAlert
import java.math.BigDecimal
import java.time.LocalDateTime

data class DeforestationAlertResponse(
    val id: String,
    val productionUnitId: String,
    val productionUnitName: String,
    val farmerId: String,
    val farmerName: String,
    val alertType: DeforestationAlert.AlertType,
    val severity: DeforestationAlert.Severity,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val alertDate: LocalDateTime,
    val confidence: BigDecimal,
    val distanceFromUnit: BigDecimal,
    val source: String,
    val sourceId: String?,
    val isHederaVerified: Boolean,
    val hederaTransactionId: String?,
    val createdAt: LocalDateTime
)

data class DeforestationAlertSummaryResponse(
    val productionUnitId: String,
    val totalAlerts: Int,
    val highSeverityAlerts: Int,
    val mediumSeverityAlerts: Int,
    val lowSeverityAlerts: Int,
    val infoAlerts: Int,
    val lastAlertDate: LocalDateTime?,
    val averageDistance: Double,
    val alertsByType: Map<DeforestationAlert.AlertType, Int>,
    val recentTrend: AlertTrend
)

data class AlertTrend(
    val direction: TrendDirection,
    val changePercentage: Double,
    val comparisonPeriod: String
)

enum class TrendDirection {
    INCREASING, DECREASING, STABLE
}

data class DeforestationAlertSearchRequest(
    val productionUnitIds: List<String>? = null,
    val farmerIds: List<String>? = null,
    val alertTypes: List<DeforestationAlert.AlertType>? = null,
    val severities: List<DeforestationAlert.Severity>? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val minConfidence: BigDecimal? = null,
    val maxDistance: BigDecimal? = null,
    val centerLatitude: Double? = null,
    val centerLongitude: Double? = null,
    val radiusKm: Double? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "alertDate",
    val sortDirection: String = "DESC"
)

data class DeforestationAlertSearchResponse(
    val alerts: List<DeforestationAlertResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val searchCriteria: DeforestationAlertSearchRequest
)

data class AlertProcessingRequest(
    val productionUnitId: String,
    val forceRefresh: Boolean = false
)

data class AlertProcessingResponse(
    val success: Boolean,
    val processedAlerts: Int,
    val newAlerts: Int,
    val errors: List<String> = emptyList(),
    val processingTime: Long
)

data class AlertIntegrityCheckRequest(
    val alertIds: List<String>
)

data class AlertIntegrityCheckResponse(
    val results: List<AlertIntegrityResult>
)

data class AlertIntegrityResult(
    val alertId: String,
    val isValid: Boolean,
    val isHederaVerified: Boolean,
    val hederaTransactionId: String?,
    val expectedHash: String?,
    val actualHash: String?,
    val error: String? = null
)

data class AlertNotificationRequest(
    val alertId: String,
    val notificationTypes: List<NotificationType>,
    val recipients: List<String>? = null,
    val message: String? = null
)

enum class NotificationType {
    EMAIL, SMS, PUSH, DASHBOARD
}

data class AlertNotificationResponse(
    val success: Boolean,
    val notificationsSent: Int,
    val failedNotifications: List<String> = emptyList()
)

data class AlertExportRequest(
    val searchCriteria: DeforestationAlertSearchRequest,
    val format: ExportFormat,
    val includeGeometry: Boolean = false,
    val includeMetadata: Boolean = false
)



data class AlertExportResponse(
    val success: Boolean,
    val downloadUrl: String?,
    val fileName: String?,
    val recordCount: Int,
    val expiresAt: LocalDateTime?,
    val error: String? = null
)

data class AlertStatisticsRequest(
    val productionUnitIds: List<String>? = null,
    val farmerIds: List<String>? = null,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val groupBy: AlertStatisticsGroupBy = AlertStatisticsGroupBy.DAY
)



data class AlertStatisticsResponse(
    val statistics: List<AlertStatistic>,
    val summary: AlertStatisticsSummary
)

data class AlertStatistic(
    val period: String,
    val totalAlerts: Int,
    val alertsBySeverity: Map<DeforestationAlert.Severity, Int>,
    val alertsByType: Map<DeforestationAlert.AlertType, Int>,
    val averageConfidence: Double,
    val averageDistance: Double
)

data class AlertStatisticsSummary(
    val totalPeriods: Int,
    val totalAlerts: Int,
    val peakPeriod: String?,
    val peakAlertCount: Int,
    val trendDirection: TrendDirection,
    val averageAlertsPerPeriod: Double
)

data class AlertConfigurationRequest(
    val monitoringEnabled: Boolean,
    val bufferDistanceKm: Double,
    val alertThresholds: AlertThresholds,
    val notificationSettings: NotificationSettings
)

data class AlertThresholds(
    val highSeverityDistance: Double,
    val mediumSeverityDistance: Double,
    val lowSeverityDistance: Double,
    val minimumConfidence: Double
)

data class NotificationSettings(
    val enableEmailNotifications: Boolean,
    val enableSmsNotifications: Boolean,
    val enablePushNotifications: Boolean,
    val highSeverityImmediate: Boolean,
    val dailySummaryEnabled: Boolean,
    val weeklySummaryEnabled: Boolean
)

data class AlertConfigurationResponse(
    val success: Boolean,
    val currentConfiguration: AlertConfigurationRequest,
    val message: String? = null
)

// Geometry Check DTOs
data class GeometryCheckRequest(
    val geoJsonPolygon: String
)

data class GeometryCheckResponse(
    val hasAlerts: Boolean,
    val totalAlerts: Int,
    val gladAlerts: Int,
    val viirsAlerts: Int,
    val treeCoverLossAlerts: Int,
    val highSeverityCount: Int,
    val mediumSeverityCount: Int,
    val lowSeverityCount: Int,
    val riskLevel: String,
    val message: String,
    val error: String? = null
)
