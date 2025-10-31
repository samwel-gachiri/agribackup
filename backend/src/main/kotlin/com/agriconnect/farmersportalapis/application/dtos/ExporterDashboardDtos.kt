package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDateTime

// Dashboard Synchronization DTOs
data class ExporterDashboardSyncDto(
    val exporterId: String,
    val lastSyncTime: LocalDateTime,
    val analytics: ExporterSystemAnalyticsDto,
    val zones: List<ZoneResponseDto>,
    val farmers: List<FarmerSummaryDto>,
    val harvestPredictions: List<HarvestPredictionDto>,
    val recentActivities: List<RecentActivityDto>,
    val upcomingHarvests: Int,
    val dataIntegrityStatus: String // SYNCHRONIZED, OUT_OF_SYNC, ERROR
)

data class RealTimeAnalyticsDto(
    val exporterId: String,
    val timestamp: LocalDateTime,
    val totalZones: Int,
    val totalFarmers: Int,
    val totalOrders: Int,
    val pendingOrders: Int,
    val totalYields: Int,
    val activeListings: Int,
    val systemLoad: Double,
    val dataFreshness: String // REAL_TIME, CACHED, STALE
)

data class RecentActivityDto(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: LocalDateTime,
    val type: String, // SYNC, UPDATE, CREATE, DELETE, CALCULATION
    val severity: String // INFO, WARNING, ERROR
)

// Data Integrity DTOs
data class DataIntegrityReportDto(
    val exporterId: String,
    val checkTime: LocalDateTime,
    val totalIssues: Int,
    val highSeverityIssues: Int,
    val mediumSeverityIssues: Int,
    val lowSeverityIssues: Int,
    val issues: List<DataIntegrityIssueDto>,
    val overallStatus: String // HEALTHY, MINOR_ISSUES, WARNING, CRITICAL
)

data class DataIntegrityIssueDto(
    val type: String, // ORPHANED_RELATIONSHIP, ZONE_WITHOUT_SUPERVISOR, etc.
    val description: String,
    val severity: String, // HIGH, MEDIUM, LOW
    val affectedEntity: String,
    val entityId: String?
)

data class DataIntegrityFixResultDto(
    val exporterId: String,
    val fixTime: LocalDateTime,
    val totalIssuesFound: Int,
    val issuesFixed: Int,
    val issuesFailed: Int,
    val fixedIssues: List<String>,
    val failedFixes: List<String>,
    val finalStatus: String // ALL_FIXED, PARTIALLY_FIXED, FAILED
)

// Enhanced Zone Analytics
data class ExporterZoneAnalyticsDto(
    val zoneId: String,
    val zoneName: String,
    val farmerCount: Int,
    val supervisorCount: Int,
    val activeListings: Int = 0,
    val totalOrders: Int = 0,
    val averageYield: Double = 0.0,
    val lastActivity: LocalDateTime? = null
)

// System Analytics Response
data class ExporterExporterSystemAnalyticsDto(
    val totalZones: Int,
    val totalFarmers: Int,
    val activeSystemAdmins: Int,
    val activeZoneSupervisors: Int,
    val zoneBreakdown: List<ZoneAnalyticsDto>
)