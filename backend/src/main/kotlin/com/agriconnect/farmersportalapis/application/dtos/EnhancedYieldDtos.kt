package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDate
import java.time.LocalDateTime

data class EnhancedRecordYieldRequestDto(
    val farmerProduceId: String,
    val yieldAmount: Double,
    val yieldUnit: String,
    val harvestDate: LocalDate,
    val plantingDate: LocalDate? = null, // Enhanced: Optional planting date override
    val seasonYear: Int? = null,
    val seasonName: String? = null,
    val notes: String? = null,
    val qualityGrade: String? = null, // Enhanced: Quality assessment
    val moistureContent: Double? = null, // Enhanced: Moisture percentage
    val storageLocation: String? = null // Enhanced: Where the yield is stored
)

data class FarmerProduceForYieldDto(
    val id: String,
    val produceName: String,
    val status: String,
    val plantingDate: LocalDate?,
    val predictedHarvestDate: LocalDate?,
    val actualHarvestDate: LocalDate?,
    val displayName: String, // Formatted display name for UI
    val canRecordYield: Boolean, // Whether yield can be recorded for this produce
    val lastYieldDate: LocalDate?, // Last time yield was recorded
    val totalYieldsRecorded: Int // Number of yields already recorded
)

data class YieldValidationResponseDto(
    val isValid: Boolean,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val estimatedGrowthDays: Int? = null,
    val expectedYieldRange: YieldRange? = null
)

data class YieldRange(
    val minYield: Double,
    val maxYield: Double,
    val unit: String,
    val confidence: String // "low", "medium", "high"
)

data class YieldStatisticsResponseDto(
    val totalYields: Int,
    val totalProduction: Double,
    val averageYieldPerHarvest: Double,
    val bestYield: YieldRecord?,
    val worstYield: YieldRecord?,
    val yieldsByMonth: Map<String, Double>,
    val yieldsByCrop: Map<String, Double>,
    val yieldsBySeason: Map<String, Double>,
    val growthDaysAnalysis: GrowthDaysAnalysis
)

data class YieldRecord(
    val amount: Double,
    val unit: String,
    val harvestDate: LocalDate,
    val produceName: String,
    val growthDays: Int?
)

data class GrowthDaysAnalysis(
    val averageGrowthDays: Double,
    val shortestGrowthPeriod: Int,
    val longestGrowthPeriod: Int,
    val growthDaysByProduce: Map<String, Double>
)

data class YieldTrendsResponseDto(
    val monthlyTrends: List<MonthlyYieldTrend>,
    val seasonalTrends: List<SeasonalYieldTrend>,
    val cropPerformanceTrends: List<CropPerformanceTrend>,
    val overallTrend: String, // "increasing", "decreasing", "stable"
    val trendConfidence: Double // 0.0 to 1.0
)

data class MonthlyYieldTrend(
    val month: String,
    val year: Int,
    val totalYield: Double,
    val averageYield: Double,
    val harvestCount: Int
)

data class SeasonalYieldTrend(
    val season: String,
    val averageYield: Double,
    val totalYield: Double,
    val harvestCount: Int,
    val years: List<Int>
)

data class CropPerformanceTrend(
    val cropName: String,
    val averageYield: Double,
    val totalYield: Double,
    val harvestCount: Int,
    val averageGrowthDays: Double,
    val performanceRating: String // "excellent", "good", "average", "poor"
)