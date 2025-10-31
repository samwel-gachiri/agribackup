package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.profile.FarmerProduce
import com.agriconnect.farmersportalapis.domain.profile.ProduceYield
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerProduceRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProduceYieldRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

@Service
class FarmerReportsService(
    private val farmerProduceRepository: FarmerProduceRepository,
    private val produceYieldRepository: ProduceYieldRepository
) {

    fun getFarmerReports(farmerId: String, startDate: String?, endDate: String?): Result<FarmerReportsResponseDto> {
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        var allYields = produces.flatMap { produceYieldRepository.findByFarmerProduceId(it.id) }

        // Filter by date range if provided
        if (startDate != null) {
            val start = LocalDate.parse(startDate)
            allYields = allYields.filter { it.harvestDate?.isAfter(start.minusDays(1)) == true }
        }
        if (endDate != null) {
            val end = LocalDate.parse(endDate)
            allYields = allYields.filter { it.harvestDate?.isBefore(end.plusDays(1)) == true }
        }

        val metrics = calculateFarmerMetrics(produces, allYields)
        val yieldRecords = allYields.map { yield ->
            val growthDays = yield.farmerProduce.plantingDate?.let { plantingDate ->
                yield.harvestDate?.let { harvestDate ->
                    ChronoUnit.DAYS.between(plantingDate, harvestDate).toInt()
                }
            }

            val accuracy = calculatePredictionAccuracy(yield)

            YieldRecordSummaryDto(
                id = yield.id,
                produceName = yield.farmerProduce.farmProduce.name,
                plantingDate = yield.farmerProduce.plantingDate,
                harvestDate = yield.harvestDate,
                yieldAmount = yield.yieldAmount,
                yieldUnit = yield.yieldUnit,
                growthDays = growthDays,
                seasonName = yield.seasonName,
                accuracy = accuracy
            )
        }

        val response = FarmerReportsResponseDto(
            metrics = metrics,
            yieldRecords = yieldRecords
        )

        return ResultFactory.getSuccessResult(response)
    }

    fun getFarmerAnalytics(farmerId: String, startDate: String?, endDate: String?): Result<FarmerAnalyticsResponseDto> {
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        var allYields = produces.flatMap { produceYieldRepository.findByFarmerProduceId(it.id) }

        // Filter by date range
        if (startDate != null) {
            val start = LocalDate.parse(startDate)
            allYields = allYields.filter { it.harvestDate?.isAfter(start.minusDays(1)) == true }
        }
        if (endDate != null) {
            val end = LocalDate.parse(endDate)
            allYields = allYields.filter { it.harvestDate?.isBefore(end.plusDays(1)) == true }
        }

        val yieldTrends = calculateYieldTrends(allYields)
        val harvestAccuracy = calculateHarvestAccuracyDataSimple(allYields)
        val seasonalPerformance = calculateSeasonalPerformanceDataSimple(allYields)
        val cropComparison = calculateCropComparisonDataSimple(allYields)

        val analytics = FarmerAnalyticsResponseDto(
            yieldTrends = yieldTrends,
            harvestAccuracy = harvestAccuracy,
            seasonalPerformance = seasonalPerformance,
            cropComparison = cropComparison
        )

        return ResultFactory.getSuccessResult(analytics)
    }

    fun getSeasonalPerformance(farmerId: String, years: Int): Result<SeasonalPerformanceResponseDto> {
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        val cutoffDate = LocalDate.now().minusYears(years.toLong())
        val allYields = produces.flatMap { produceYieldRepository.findByFarmerProduceId(it.id) }
            .filter { it.harvestDate?.isAfter(cutoffDate) == true }

        val yearlyPerformance = allYields.groupBy { it.harvestDate?.year }
            .mapNotNull { (year, yields) ->
                year?.let {
                    val seasonalData = yields.groupBy { it.seasonName ?: "Unknown" }
                        .mapValues { it.value.sumOf { yield -> yield.yieldAmount } }

                    YearlyPerformanceDto(
                        year = it,
                        totalYield = yields.sumOf { yield -> yield.yieldAmount },
                        averageYield = yields.map { yield -> yield.yieldAmount }.average(),
                        harvestCount = yields.size,
                        seasons = seasonalData
                    )
                }
            }.sortedBy { it.year }

        val seasonalAverages = allYields.groupBy { it.seasonName ?: "Unknown" }
            .mapValues { it.value.map { yield -> yield.yieldAmount }.average() }

        val bestYear = yearlyPerformance.maxByOrNull { it.averageYield }?.year
        val bestSeason = seasonalAverages.maxByOrNull { it.value }?.key

        val response = SeasonalPerformanceResponseDto(
            yearlyPerformance = yearlyPerformance,
            seasonalAverages = seasonalAverages,
            bestPerformingYear = bestYear,
            bestPerformingSeason = bestSeason,
            performanceTrend = "stable", // TODO: implement correct trend logic
            recommendations = listOf("Continue current seasonal practices")
        )

        return ResultFactory.getSuccessResult(response)
    }

    fun getFarmerReportCharts(
        farmerId: String,
        startDate: String?,
        endDate: String?
    ): Result<FarmerReportChartsResponseDto> {
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        var allYields = produces.flatMap { produceYieldRepository.findByFarmerProduceId(it.id) }

        // Filter by date range
        if (startDate != null) {
            val start = LocalDate.parse(startDate)
            allYields = allYields.filter { it.harvestDate?.isAfter(start.minusDays(1)) == true }
        }
        if (endDate != null) {
            val end = LocalDate.parse(endDate)
            allYields = allYields.filter { it.harvestDate?.isBefore(end.plusDays(1)) == true }
        }

        val yieldTrendsData = calculateYieldTrends(allYields)
        val accuracyData = calculateHarvestAccuracyDataSimple(allYields)
        val seasonalData = calculateSeasonalPerformanceDataSimple(allYields)
        val cropComparisonData = calculateCropComparisonDataSimple(allYields)
        val yieldTrendsChart = chartDataToDto(createYieldTrendsChart(yieldTrendsData), "Yield Trends", "Yield")
        val accuracyChart = chartDataToDto(createAccuracyChart(accuracyData), "Harvest Accuracy", "Accuracy (%)")
        val seasonalChart = chartDataToDto(createSeasonalChart(seasonalData), "Seasonal Performance", "Yield")
        val cropComparisonChart = chartDataToDto(createCropComparisonChart(cropComparisonData), "Crop Comparison", "Yield")

        val charts = FarmerReportChartsResponseDto(
            yieldTrends = yieldTrendsChart,
            accuracy = accuracyChart,
            seasonal = seasonalChart,
            cropComparison = cropComparisonChart
        )

        return ResultFactory.getSuccessResult(charts)
    }
    private fun chartDataToDto(chart: ChartData, title: String, yAxisLabel: String? = null): ChartDataDto {
        val mainDataset = chart.datasets.firstOrNull()
        return ChartDataDto(
            labels = chart.labels,
            data = mainDataset?.data ?: emptyList(),
            type = chart.type,
            title = title,
            yAxisLabel = yAxisLabel
        )
    }

    fun exportReportsPDF(farmerId: String, startDate: String?, endDate: String?, includeCharts: Boolean): ByteArray {
        // In a real implementation, this would generate a PDF using a library like iText
        // For now, return a mock PDF content
        val content = "Farmer Reports PDF for farmer $farmerId from $startDate to $endDate"
        return content.toByteArray()
    }

    fun exportReportsCSV(farmerId: String, startDate: String?, endDate: String?): ByteArray {
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        var allYields = produces.flatMap { produceYieldRepository.findByFarmerProduceId(it.id) }

        // Filter by date range
        if (startDate != null) {
            val start = LocalDate.parse(startDate)
            allYields = allYields.filter { it.harvestDate?.isAfter(start.minusDays(1)) == true }
        }
        if (endDate != null) {
            val end = LocalDate.parse(endDate)
            allYields = allYields.filter { it.harvestDate?.isBefore(end.plusDays(1)) == true }
        }

        val csvContent = buildString {
            appendLine("Produce Name,Planting Date,Harvest Date,Yield Amount,Yield Unit,Growth Days,Season,Notes")
            allYields.forEach { yield ->
                val growthDays = yield.farmerProduce.plantingDate?.let { plantingDate ->
                    yield.harvestDate?.let { harvestDate ->
                        ChronoUnit.DAYS.between(plantingDate, harvestDate).toInt()
                    }
                } ?: ""

                appendLine("${yield.farmerProduce.farmProduce.name},${yield.farmerProduce.plantingDate ?: ""},${yield.harvestDate ?: ""},${yield.yieldAmount},${yield.yieldUnit},$growthDays,${yield.seasonName ?: ""},\"${yield.notes ?: ""}\"")
            }
        }

        return csvContent.toByteArray()
    }

    fun getHarvestAccuracy(farmerId: String, startDate: String?, endDate: String?): Result<HarvestAccuracyResponseDto> {
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        var allYields = produces.flatMap { produceYieldRepository.findByFarmerProduceId(it.id) }

        // Filter by date range
        if (startDate != null) {
            val start = LocalDate.parse(startDate)
            allYields = allYields.filter { it.harvestDate?.isAfter(start.minusDays(1)) == true }
        }
        if (endDate != null) {
            val end = LocalDate.parse(endDate)
            allYields = allYields.filter { it.harvestDate?.isBefore(end.plusDays(1)) == true }
        }

        val accuracyData = calculateDetailedAccuracy(produces)
        return ResultFactory.getSuccessResult(accuracyData)
    }

    fun getCropPerformance(farmerId: String, startDate: String?, endDate: String?): Result<CropPerformanceResponseDto> {
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        var allYields = produces.flatMap { produceYieldRepository.findByFarmerProduceId(it.id) }

        // Filter by date range
        if (startDate != null) {
            val start = LocalDate.parse(startDate)
            allYields = allYields.filter { it.harvestDate?.isAfter(start.minusDays(1)) == true }
        }
        if (endDate != null) {
            val end = LocalDate.parse(endDate)
            allYields = allYields.filter { it.harvestDate?.isBefore(end.plusDays(1)) == true }
        }

        val cropPerformances = allYields.groupBy { it.farmerProduce.farmProduce.name }
            .map { (cropName, yields) ->
                val yieldAmounts = yields.map { it.yieldAmount }
                val avgYield = yieldAmounts.average()
                val consistency = calculateConsistency(allYields)
                val avgGrowthDays = yields.mapNotNull { yield ->
                    yield.farmerProduce.plantingDate?.let { plantingDate ->
                        yield.harvestDate?.let { harvestDate ->
                            ChronoUnit.DAYS.between(plantingDate, harvestDate).toDouble()
                        }
                    }
                }.average()

                CropPerformanceDetailDto(
                    cropName = cropName,
                    totalYield = yields.sumOf { it.yieldAmount },
                    averageYield = avgYield,
                    averageGrowthDays = avgGrowthDays,
//                    yieldConsistency = consistenePerformanceRating(avgYield),
                    yieldConsistency = calculateConsistency(allYields),
                    trend = calculateCropTrend(yields),
                    harvestCount = 0,
                    performanceRating = "Good"
                )
            }

        val bestCrop = cropPerformances.maxByOrNull { it.averageYield }?.cropName ?: "None"
        val mostConsistent = cropPerformances.minByOrNull { it.yieldConsistency }?.cropName ?: "None"

        val response = CropPerformanceResponseDto(
            cropPerformances = cropPerformances,
            bestPerformingCrop = bestCrop,
            mostConsistentCrop = mostConsistent,
            recommendations = generateCropRecommendations(allYields)
        )

        return ResultFactory.getSuccessResult(response)
    }

    fun getFarmingEfficiency(farmerId: String, months: Int): Result<FarmingEfficiencyResponseDto> {

        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        val cutoffDate = LocalDate.now().minusMonths(months.toLong())
        val allYields = produces.flatMap { produceYieldRepository.findByFarmerProduceId(it.id) }
            .filter { it.harvestDate?.isAfter(cutoffDate) == true }

        val efficiency = calculateFarmingEfficiency(produces, allYields)
        return ResultFactory.getSuccessResult(efficiency)
    }
    // Helper methods

    private fun calculateFarmerMetrics(produces: List<FarmerProduce>, yields: List<ProduceYield>): FarmerMetricsDto {
        val totalYields = yields.size
        val totalProduction = yields.sumOf { it.yieldAmount }

        val accuracyData = produces.mapNotNull { produce ->
            val produceYields = yields.filter { it.farmerProduce.id == produce.id }
            if (produceYields.isNotEmpty() && produce.predictedHarvestDate != null) {
                val avgAccuracy = produceYields.mapNotNull { calculatePredictionAccuracy(it) }.average()
                avgAccuracy
            } else null
        }

        val harvestAccuracy = if (accuracyData.isNotEmpty()) accuracyData.average() else 0.0

        val growthDaysData = yields.mapNotNull { yield ->
            yield.farmerProduce.plantingDate?.let { plantingDate ->
                yield.harvestDate?.let { harvestDate ->
                    ChronoUnit.DAYS.between(plantingDate, harvestDate).toInt()
                }
            }
        }

        val avgGrowthDays = if (growthDaysData.isNotEmpty()) growthDaysData.average().toInt() else 0

        return FarmerMetricsDto(
            totalYields = totalYields,
            totalProduction = totalProduction,
            harvestAccuracy = harvestAccuracy,
            avgGrowthDays = avgGrowthDays
        )
    }

    private fun calculatePredictionAccuracy(yield: ProduceYield): Double? {
        val predictedDate = yield.farmerProduce.predictedHarvestDate
        val actualDate = yield.harvestDate

        return if (predictedDate != null && actualDate != null) {
            val daysDifference = abs(ChronoUnit.DAYS.between(predictedDate, actualDate))
            val accuracy = when {
                daysDifference <= 3 -> 100.0
                daysDifference <= 7 -> 90.0
                daysDifference <= 14 -> 75.0
                daysDifference <= 30 -> 50.0
                else -> 25.0
            }
            accuracy
        } else null
    }

    private fun calculateYieldTrends(yields: List<ProduceYield>): YieldTrendsData {
        val monthlyData = yields.groupBy {
            it.harvestDate?.format(DateTimeFormatter.ofPattern("yyyy-MM")) ?: "Unknown"
        }.mapValues { it.value.sumOf { yield -> yield.yieldAmount } }
            .toSortedMap()

        val labels = monthlyData.keys.toList()
        val values = monthlyData.values.toList()

        val trend = if (values.size >= 3) {
            val recent = values.takeLast(3).average()
            val earlier = values.dropLast(3).takeLast(3).average()
            when {
                recent > earlier * 1.1 -> "increasing"
                recent < earlier * 0.9 -> "decreasing"
                else -> "stable"
            }
        } else "stable"

        val trendPercentage = if (values.size >= 2) {
            ((values.last() - values.first()) / values.first()) * 100
        } else 0.0

        return YieldTrendsData(
            labels = labels,
            values = values,
            trend = trend,
            trendPercentage = trendPercentage
        )
    }

    // Missing method implementations
    private fun calculateHarvestAccuracyDataSimple(yields: List<ProduceYield>): HarvestAccuracyData {
        val accurateHarvests = yields.count { yield ->
            yield.farmerProduce.predictedHarvestDate != null && yield.harvestDate != null &&
            kotlin.math.abs(java.time.temporal.ChronoUnit.DAYS.between(yield.farmerProduce.predictedHarvestDate, yield.harvestDate)) <= 7
        }
        val totalPredictions = yields.count { it.farmerProduce.predictedHarvestDate != null }
        val accuracy = if (totalPredictions > 0) (accurateHarvests.toDouble() / totalPredictions) * 100 else 0.0
        return HarvestAccuracyData(
            overallAccuracy = accuracy,
            accurateCount = totalPredictions,
            inaccurateCount = totalPredictions - accurateHarvests,
            averageDeviationDays = 0.0,
            accuracyByProduce = emptyMap()
        )
    }

    private fun calculateSeasonalPerformanceDataSimple(yields: List<ProduceYield>): SeasonalPerformanceData {
        val seasonalData = yields.groupBy { yield ->
            yield.harvestDate?.monthValue?.let { month ->
                when (month) {
                    in 3..5 -> "Spring"
                    in 6..8 -> "Summer"
                    in 9..11 -> "Fall"
                    else -> "Winter"
                }
            } ?: "Unknown"
        }.mapValues { (_, seasonYields) ->
            seasonYields.sumOf { it.yieldAmount }
        }

        return SeasonalPerformanceData(
//            spring = seasonalData["Spring"] ?: 0.0,
//            summer = seasonalData["Summer"] ?: 0.0,
//            fall = seasonalData["Fall"] ?: 0.0,
//            winter = seasonalData["Winter"] ?: 0.0
            seasons = seasonalData.map { it.key },
            averageYields = seasonalData.map { it.value },
            bestSeason = seasonalData.maxByOrNull { it.value }?.key ?: "Unknown",
            worstSeason = seasonalData.minByOrNull { it.value }?.key ?: "Unknown",
            seasonalVariation = seasonalData.map { it.value }.average()
        )
    }

    private fun calculateCropComparisonDataSimple(yields: List<ProduceYield>): CropComparisonData {
        val cropData = yields.groupBy { it.farmerProduce.predictedSpecies ?: "Unknown" }
            .mapValues { (_, cropYields) ->
                CropPerformance(
                    totalYield = cropYields.sumOf { it.yieldAmount },
                    averageYield = cropYields.map { it.yieldAmount }.average(),
                    harvestCount = cropYields.size
                )
            }

        return CropComparisonData(
            labels = cropData.map { it.key },
            values = cropData.map { it.value.totalYield },
            averageYields = cropData.map { it.value.averageYield },
            bestPerformingCrop = cropData.maxByOrNull { it.value.averageYield }?.key ?: "Unknown",
            mostConsistentCrop = cropData.minByOrNull { it.value.averageYield }?.key ?: "Unknown",
        )
    }

    private fun calculatePerformanceTrend(yields: List<ProduceYield>): String {
        if (yields.size < 2) return "stable"
        val recent = yields.takeLast(3).map { it.yieldAmount }.average()
        val earlier = yields.take(3).map { it.yieldAmount }.average()
        return when {
            recent > earlier * 1.1 -> "improving"
            recent < earlier * 0.9 -> "declining"
            else -> "stable"
        }
    }

    private fun generateSeasonalRecommendations(yields: List<ProduceYield>): List<String> {
        val recommendations = mutableListOf<String>()
        val seasonalData = calculateSeasonalPerformanceDataSimple(yields)

        // Find spring and summer indices and their values
        val springIndex = seasonalData.seasons.indexOf("Spring")
        val summerIndex = seasonalData.seasons.indexOf("Summer")
        val fallIndex = seasonalData.seasons.indexOf("Fall")
        val winterIndex = seasonalData.seasons.indexOf("Winter")

        // Compare seasons if they exist
        if (springIndex != -1 && summerIndex != -1 &&
            seasonalData.averageYields[springIndex] > seasonalData.averageYields[summerIndex]) {
            recommendations.add("Consider focusing more on spring planting for better yields")
        }
        if (fallIndex != -1 && winterIndex != -1 &&
            seasonalData.averageYields[fallIndex] > seasonalData.averageYields[winterIndex]) {
            recommendations.add("Fall harvests show better performance than winter")
        }

//        if (seasonalData.spring > seasonalData.summer) {
//            recommendations.add("Consider focusing more on spring planting for better yields")
//        }
//        if (seasonalData.fall > seasonalData.winter) {
//            recommendations.add("Fall harvests show better performance than winter")
//        }
        
        return recommendations.ifEmpty { listOf("Continue current seasonal practices") }
    }

    private fun createYieldTrendsChart(data: YieldTrendsData): ChartData {
        return ChartData(
            type = "line",
            labels = data.labels,
            datasets = listOf(
                ChartDataset(
                    label = "Yield Trends",
                    data = data.values,
                    borderColor = "#4CAF50",
                    backgroundColor = "rgba(76, 175, 80, 0.1)"
                )
            )
        )
    }

    private fun createAccuracyChart(data: HarvestAccuracyData): ChartData {
        return ChartData(
            type = "doughnut",
            labels = listOf("Accurate", "Inaccurate"),
            datasets = listOf(
                ChartDataset(
                    label = "Harvest Accuracy",
                    data = listOf(data.accurateCount.toDouble(), (data.inaccurateCount).toDouble()),
                    backgroundColor = listOf("#4CAF50", "#F44336")
                )
            )
        )
    }

    private fun createSeasonalChart(data: SeasonalPerformanceData): ChartData {
        return ChartData(
            type = "bar",
            labels = listOf("Spring", "Summer", "Fall", "Winter"),
            datasets = listOf(
                ChartDataset(
                    label = "Seasonal Performance",
//                    data = listOf(data.spring, data.summer, data.fall, data.winter),
                    data = listOf(data.averageYields.sum()),
                    backgroundColor = listOf("#8BC34A", "#FFC107", "#FF9800", "#2196F3")
                )
            )
        )
    }

    private fun createCropComparisonChart(data: CropComparisonData): ChartData {
        return ChartData(
            type = "bar",
            labels = data.labels,
            datasets = listOf(
                ChartDataset(
                    label = "Crop Comparison",
                    data = data.values,
                    backgroundColor = "#9C27B0"
                )
            )
        )
    }

    private fun calculateDetailedAccuracy(produces: List<FarmerProduce>): HarvestAccuracyResponseDto {
        val accuracyData = calculateHarvestAccuracyDataSimple(produces.map { produce -> produceYieldRepository.findByFarmerProduceId(produce.id) }.flatten())
        return HarvestAccuracyResponseDto(
            overallAccuracy = accuracyData.overallAccuracy,
            totalPredictions = accuracyData.accurateCount + accuracyData.inaccurateCount,
            accuratePredictions = accuracyData.accurateCount,
            averageDeviationDays = accuracyData.averageDeviationDays,
            accuracyTrend = "",
            accuracyByMonth = emptyMap(),
            accuracyByCrop = accuracyData.accuracyByProduce,
            recommendations = emptyList()
        )
    }

    private fun calculateConsistency(yields: List<ProduceYield>): Double {
        if (yields.isEmpty()) return 0.0
        val values = yields.map { it.yieldAmount }
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        return if (mean > 0) (1 - (stdDev / mean)) * 100 else 0.0
    }

    private fun consistenePerformanceRating(consistency: Double): String {
        return when {
            consistency >= 80 -> "Excellent"
            consistency >= 60 -> "Good"
            consistency >= 40 -> "Fair"
            else -> "Needs Improvement"
        }
    }

    private fun calculateCropTrend(yields: List<ProduceYield>): String {
        return calculatePerformanceTrend(yields)
    }

    private fun generateCropRecommendations(yields: List<ProduceYield>): List<String> {
        return generateSeasonalRecommendations(yields)
    }

    private fun calculateFarmingEfficiency(produces: List<FarmerProduce>, yields: List<ProduceYield>): FarmingEfficiencyResponseDto {
        val totalProduction = yields.sumOf { it.yieldAmount }
        val averageYield = if (yields.isNotEmpty()) yields.map { it.yieldAmount }.average() else 0.0
        val efficiency = if (produces.isNotEmpty()) (totalProduction / produces.size) else 0.0
        
        return FarmingEfficiencyResponseDto(
            overallEfficiency = efficiency,
//            totalProduction = totalProduction,
//            averageYield = averageYield,
//            recommendations = listOf("Continue current practices", "Consider crop rotation")
            recommendations = emptyList(),
            yieldPerDay = totalProduction / (yields.size / 24.0),
            resourceUtilization = efficiency,
            timeToHarvest = 0.00,
            efficiencyTrend = "",
            efficiencyFactors = emptyList()
        )
    }
}