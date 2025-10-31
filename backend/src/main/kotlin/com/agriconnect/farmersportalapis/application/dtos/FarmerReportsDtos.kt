package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDate
import java.time.LocalDateTime

data class FarmerReportsResponseDto(
    val metrics: FarmerMetricsDto,
    val yieldRecords: List<YieldRecordSummaryDto>
)

data class FarmerMetricsDto(
    val totalYields: Int,
    val totalProduction: Double,
    val harvestAccuracy: Double, // Percentage
    val avgGrowthDays: Int
)

data class YieldRecordSummaryDto(
    val id: String,
    val produceName: String,
    val plantingDate: LocalDate?,
    val harvestDate: LocalDate?,
    val yieldAmount: Double,
    val yieldUnit: String,
    val growthDays: Int?,
    val seasonName: String?,
    val accuracy: Double? // Prediction accuracy percentage
)

data class FarmerAnalyticsResponseDto(
    val yieldTrends: YieldTrendsData,
    val harvestAccuracy: HarvestAccuracyData,
    val seasonalPerformance: SeasonalPerformanceData,
    val cropComparison: CropComparisonData
)

data class YieldTrendsData(
    val labels: List<String>, // Dates or periods
    val values: List<Double>, // Yield amounts
    val trend: String, // "increasing", "decreasing", "stable"
    val trendPercentage: Double
)

data class HarvestAccuracyData(
    val overallAccuracy: Double,
    val accurateCount: Int,
    val inaccurateCount: Int,
    val averageDeviationDays: Double,
    val accuracyByProduce: Map<String, Double>
)

data class SeasonalPerformanceData(
    val seasons: List<String>,
    val averageYields: List<Double>,
    val bestSeason: String,
    val worstSeason: String,
    val seasonalVariation: Double
)

data class CropComparisonData(
    val labels: List<String>, // Crop names
    val values: List<Double>, // Total production
    val averageYields: List<Double>,
    val bestPerformingCrop: String,
    val mostConsistentCrop: String
)

data class SeasonalPerformanceResponseDto(
    val yearlyPerformance: List<YearlyPerformanceDto>,
    val seasonalAverages: Map<String, Double>,
    val bestPerformingYear: Int?,
    val bestPerformingSeason: String?,
    val performanceTrend: String,
    val recommendations: List<String>
)

data class YearlyPerformanceDto(
    val year: Int,
    val totalYield: Double,
    val averageYield: Double,
    val harvestCount: Int,
    val seasons: Map<String, Double>
)

data class FarmerReportChartsResponseDto(
    val yieldTrends: ChartDataDto,
    val accuracy: ChartDataDto,
    val seasonal: ChartDataDto,
    val cropComparison: ChartDataDto
)

data class ChartDataDto(
    val labels: List<String>,
    val data: List<Double>,
    val type: String, // "line", "bar", "pie", etc.
    val title: String,
    val yAxisLabel: String? = null
)

data class HarvestAccuracyResponseDto(
    val overallAccuracy: Double,
    val totalPredictions: Int,
    val accuratePredictions: Int,
    val averageDeviationDays: Double,
    val accuracyTrend: String,
    val accuracyByMonth: Map<String, Double>,
    val accuracyByCrop: Map<String, Double>,
    val recommendations: List<String>
)

data class CropPerformanceResponseDto(
    val cropPerformances: List<CropPerformanceDetailDto>,
    val bestPerformingCrop: String,
    val mostConsistentCrop: String,
    val recommendations: List<String>
)

data class CropPerformanceDetailDto(
    val cropName: String,
    val totalYield: Double,
    val averageYield: Double,
    val harvestCount: Int,
    val averageGrowthDays: Double,
    val yieldConsistency: Double, // Standard deviation
    val performanceRating: String, // "excellent", "good", "average", "poor"
    val trend: String // "improving", "declining", "stable"
)

data class FarmingEfficiencyResponseDto(
    val overallEfficiency: Double, // 0-100 percentage
    val yieldPerDay: Double,
    val resourceUtilization: Double,
    val timeToHarvest: Double,
    val efficiencyTrend: String,
    val efficiencyFactors: List<EfficiencyFactorDto>,
    val recommendations: List<String>
)

data class EfficiencyFactorDto(
    val factor: String, // "growth_time", "yield_consistency", "prediction_accuracy"
    val score: Double, // 0-100
    val impact: String, // "high", "medium", "low"
    val description: String
)
// End of FarmerReportsDtos

data class CropPerformance(
    val totalYield: Double,
    val averageYield: Double,
    val harvestCount: Int
)

data class ChartData(
    val type: String,
    val labels: List<String>,
    val datasets: List<ChartDataset>
)

data class ChartDataset(
    val label: String,
    val data: List<Double>,
    val borderColor: String? = null,
    val backgroundColor: Any? = null
)
// NOTE: Simplified HarvestAccuracyResponseDto and FarmingEfficiencyResponseDto
// definitions were removed to avoid redeclaration. Use the richer types
// defined earlier in this file: HarvestAccuracyResponseDto and
// FarmingEfficiencyResponseDto.