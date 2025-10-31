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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class YieldRecordingService(
    private val farmerProduceRepository: FarmerProduceRepository,
    private val produceYieldRepository: ProduceYieldRepository
) {
    fun recordYield(dto: RecordYieldRequestDto): Result<YieldRecordResponseDto> {
        // Validation
        if (dto.yieldAmount <= 0.0) {
            return ResultFactory.getFailResult("Yield amount must be positive.")
        }
        if (dto.harvestDate.isAfter(LocalDate.now())) {
            return ResultFactory.getFailResult("Harvest date cannot be in the future.")
        }
        val produce = farmerProduceRepository.findById(dto.farmerProduceId).orElse(null)
            ?: return ResultFactory.getFailResult("Produce not found.")
        // Create new yield
        val yieldRecord = ProduceYield(
            id = java.util.UUID.randomUUID().toString(),
            farmerProduce = produce,
            seasonYear = dto.seasonYear,
            seasonName = dto.seasonName,
            yieldAmount = dto.yieldAmount,
            yieldUnit = dto.yieldUnit,
            notes = dto.notes,
            createdAt = LocalDateTime.now(),
            harvestDate = dto.harvestDate,
            listedAmount = 0.0,
            remainingAmount = dto.yieldAmount
        )
        produceYieldRepository.save(yieldRecord)
        return ResultFactory.getSuccessResult(toYieldDto(yieldRecord, produce))
    }

    fun getYieldsByProduce(farmerProduceId: String): Result<List<YieldRecordResponseDto>> {
        val produce = farmerProduceRepository.findById(farmerProduceId).orElse(null)
            ?: return ResultFactory.getFailResult("Produce not found.")
        val yields = produceYieldRepository.findByFarmerProduceId(farmerProduceId)
        return ResultFactory.getSuccessResult(yields.map { toYieldDto(it, produce) })
    }

    fun updateYield(yieldId: String, dto: UpdateYieldRequestDto): Result<YieldRecordResponseDto> {
        val yieldRecord = produceYieldRepository.findById(yieldId).orElse(null)
            ?: return ResultFactory.getFailResult("Yield record not found.")
        if (dto.yieldAmount != null && dto.yieldAmount > 0.00) {
            yieldRecord.yieldAmount = dto.yieldAmount
        }
        if (dto.yieldUnit != null) {
            yieldRecord.yieldUnit = dto.yieldUnit
        }
        if (dto.harvestDate != null && !dto.harvestDate.isAfter(LocalDate.now())) {
            yieldRecord.harvestDate = dto.harvestDate
        }
        if (dto.seasonYear != null) {
            yieldRecord.seasonYear = dto.seasonYear
        }
        if (dto.seasonName != null) {
            yieldRecord.seasonName = dto.seasonName
        }
        if (dto.notes != null) {
            yieldRecord.notes = dto.notes
        }
        produceYieldRepository.save(yieldRecord)
        return ResultFactory.getSuccessResult(toYieldDto(yieldRecord, yieldRecord.farmerProduce))
    }

    fun deleteYield(yieldId: String): Result<Boolean> {
        val yieldRecord = produceYieldRepository.findById(yieldId).orElse(null)
            ?: return ResultFactory.getFailResult("Yield record not found.")
        produceYieldRepository.delete(yieldRecord)
        return ResultFactory.getSuccessResult(true)
    }

    fun getFarmerYieldSummary(farmerId: String, limit: Int? = null): Result<FarmerYieldSummaryResponseDto> {
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        val allYields = produces.flatMap { produceYieldRepository.findByFarmerProduceId(it.id) }
            .sortedByDescending { it.harvestDate }
            .let { if (limit != null) it.take(limit) else it }
        
        val totalYieldAmount = allYields.sumOf { it.yieldAmount }
        val totalYields = allYields.size
        val averageYield = if (totalYields > 0) totalYieldAmount/totalYields else 0.0
        
        val summary = FarmerYieldSummaryResponseDto(
            farmerId = farmerId,
            totalYields = totalYields,
            totalYieldAmount = totalYieldAmount,
            averageYield = averageYield
        )
        return ResultFactory.getSuccessResult(summary)
    }

    // Enhanced Yield Recording Methods

    fun getFarmerProducesForYieldRecording(farmerId: String): Result<List<FarmerProduceForYieldDto>> {
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        val produceList = produces.map { produce ->
            val yields = produceYieldRepository.findByFarmerProduceId(produce.id)
            val lastYieldDate = yields.maxByOrNull { it.harvestDate ?: LocalDate.MIN }?.harvestDate
            
            FarmerProduceForYieldDto(
                id = produce.id,
                produceName = produce.farmProduce.name,
                status = produce.status.name,
                plantingDate = produce.plantingDate,
                predictedHarvestDate = produce.predictedHarvestDate,
                actualHarvestDate = produce.actualHarvestDate,
                displayName = "${produce.farmProduce.name} - ${produce.status.name}",
                canRecordYield = canRecordYieldForProduce(produce),
                lastYieldDate = lastYieldDate,
                totalYieldsRecorded = yields.size
            )
        }
        return ResultFactory.getSuccessResult(produceList)
    }

    fun recordEnhancedYield(dto: EnhancedRecordYieldRequestDto): Result<YieldRecordResponseDto> {
        // Enhanced validation
        val validation = validateYieldData(dto)
        if (!(validation.data?.isValid!!)) {
            return ResultFactory.getFailResult("Validation failed: ${validation.data?.errors?.joinToString(", ")}")
        }

        val produce = farmerProduceRepository.findById(dto.farmerProduceId).orElse(null)
            ?: return ResultFactory.getFailResult("Produce not found.")

        // Update planting date if provided and different
        if (dto.plantingDate != null && dto.plantingDate != produce.plantingDate) {
            produce.plantingDate = dto.plantingDate
            farmerProduceRepository.save(produce)
        }

        // Create enhanced yield record
        val yieldRecord = ProduceYield(
            id = java.util.UUID.randomUUID().toString(),
            farmerProduce = produce,
            seasonYear = dto.seasonYear ?: LocalDate.now().year,
            seasonName = dto.seasonName ?: getCurrentSeason(),
            yieldAmount = dto.yieldAmount,
            yieldUnit = dto.yieldUnit,
            notes = dto.notes,
            createdAt = LocalDateTime.now(),
            harvestDate = dto.harvestDate,
            listedAmount = 0.0,
            remainingAmount = dto.yieldAmount,
            // Enhanced fields (would need to add these to ProduceYield entity)
            qualityGrade = dto.qualityGrade,
            moistureContent = dto.moistureContent,
            storageLocation = dto.storageLocation
        )

        produceYieldRepository.save(yieldRecord)
        return ResultFactory.getSuccessResult(toYieldDto(yieldRecord, produce))
    }

    fun validateYieldData(dto: EnhancedRecordYieldRequestDto): Result<YieldValidationResponseDto> {
        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        // Basic validation
        if (dto.yieldAmount <= 0.0) {
            errors.add("Yield amount must be positive")
        }

        if (dto.harvestDate.isAfter(LocalDate.now())) {
            errors.add("Harvest date cannot be in the future")
        }

        val produce = farmerProduceRepository.findById(dto.farmerProduceId).orElse(null)
        if (produce == null) {
            errors.add("Produce not found")
            return ResultFactory.getSuccessResult(YieldValidationResponseDto(
                isValid = false,
                errors = errors
            ))
        }

        // Enhanced validation
        val plantingDate = dto.plantingDate ?: produce.plantingDate
        var estimatedGrowthDays: Int? = null

        if (plantingDate != null) {
            estimatedGrowthDays = ChronoUnit.DAYS.between(plantingDate, dto.harvestDate).toInt()
            
            if (estimatedGrowthDays < 0) {
                errors.add("Harvest date cannot be before planting date")
            } else {
                // Validate growth period based on crop type
                val cropName = produce.farmProduce.name.lowercase()
                val expectedRange = getExpectedGrowthDays(cropName)
                
                if (estimatedGrowthDays < expectedRange.first) {
                    warnings.add("Growth period seems short (${estimatedGrowthDays} days, expected: ${expectedRange.first}-${expectedRange.second} days)")
                    suggestions.add("Verify planting and harvest dates")
                } else if (estimatedGrowthDays > expectedRange.second) {
                    warnings.add("Growth period seems long (${estimatedGrowthDays} days, expected: ${expectedRange.first}-${expectedRange.second} days)")
                    suggestions.add("Consider if this is normal for your growing conditions")
                }
            }
        }

        // Yield amount validation
        val expectedYieldRange = getExpectedYieldRange(produce.farmProduce.name, dto.yieldUnit)
        if (dto.yieldAmount < expectedYieldRange.minYield) {
            warnings.add("Yield amount seems low for ${produce.farmProduce.name}")
            suggestions.add("Consider factors that might have affected yield")
        } else if (dto.yieldAmount > expectedYieldRange.maxYield) {
            warnings.add("Yield amount seems high for ${produce.farmProduce.name}")
            suggestions.add("Verify measurement accuracy")
        }

        // Moisture content validation
        dto.moistureContent?.let { moisture ->
            if (moisture < 0 || moisture > 100) {
                errors.add("Moisture content must be between 0 and 100 percent")
            } else if (moisture > 25) {
                warnings.add("High moisture content may affect storage")
                suggestions.add("Consider drying before storage")
            }
        }

        val validation = YieldValidationResponseDto(
            isValid = errors.isEmpty(),
            warnings = warnings,
            errors = errors,
            suggestions = suggestions,
            estimatedGrowthDays = estimatedGrowthDays,
            expectedYieldRange = expectedYieldRange
        )

        return ResultFactory.getSuccessResult(validation)
    }

    fun getFarmerYieldStatistics(farmerId: String, startDate: String?, endDate: String?): Result<YieldStatisticsResponseDto> {
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

        if (allYields.isEmpty()) {
            return ResultFactory.getSuccessResult(YieldStatisticsResponseDto(
                totalYields = 0,
                totalProduction = 0.0,
                averageYieldPerHarvest = 0.0,
                bestYield = null,
                worstYield = null,
                yieldsByMonth = emptyMap(),
                yieldsByCrop = emptyMap(),
                yieldsBySeason = emptyMap(),
                growthDaysAnalysis = GrowthDaysAnalysis(0.0, 0, 0, emptyMap())
            ))
        }

        val totalProduction = allYields.sumOf { it.yieldAmount }
        val averageYield = totalProduction / allYields.size

        val bestYield = allYields.maxByOrNull { it.yieldAmount }?.let { yield ->
            YieldRecord(
                amount = yield.yieldAmount,
                unit = yield.yieldUnit,
                harvestDate = yield.harvestDate ?: LocalDate.now(),
                produceName = yield.farmerProduce.farmProduce.name,
                growthDays = calculateGrowthDays(yield)
            )
        }

        val worstYield = allYields.minByOrNull { it.yieldAmount }?.let { yield ->
            YieldRecord(
                amount = yield.yieldAmount,
                unit = yield.yieldUnit,
                harvestDate = yield.harvestDate ?: LocalDate.now(),
                produceName = yield.farmerProduce.farmProduce.name,
                growthDays = calculateGrowthDays(yield)
            )
        }

        val yieldsByMonth = allYields.groupBy { 
            it.harvestDate?.format(DateTimeFormatter.ofPattern("yyyy-MM")) ?: "Unknown"
        }.mapValues { it.value.sumOf { yield -> yield.yieldAmount } }

        val yieldsByCrop = allYields.groupBy { 
            it.farmerProduce.farmProduce.name 
        }.mapValues { it.value.sumOf { yield -> yield.yieldAmount } }

        val yieldsBySeason = allYields.groupBy { 
            it.seasonName ?: "Unknown"
        }.mapValues { it.value.sumOf { yield -> yield.yieldAmount } }

        val growthDaysData = allYields.mapNotNull { yield ->
            calculateGrowthDays(yield)?.let { days ->
                yield.farmerProduce.farmProduce.name to days
            }
        }

        val growthDaysAnalysis = if (growthDaysData.isNotEmpty()) {
            GrowthDaysAnalysis(
                averageGrowthDays = growthDaysData.map { it.second }.average(),
                shortestGrowthPeriod = growthDaysData.minOf { it.second },
                longestGrowthPeriod = growthDaysData.maxOf { it.second },
                growthDaysByProduce = growthDaysData.groupBy { it.first }
                    .mapValues { it.value.map { pair -> pair.second }.average() }
            )
        } else {
            GrowthDaysAnalysis(0.0, 0, 0, emptyMap())
        }

        val statistics = YieldStatisticsResponseDto(
            totalYields = allYields.size,
            totalProduction = totalProduction,
            averageYieldPerHarvest = averageYield,
            bestYield = bestYield,
            worstYield = worstYield,
            yieldsByMonth = yieldsByMonth,
            yieldsByCrop = yieldsByCrop,
            yieldsBySeason = yieldsBySeason,
            growthDaysAnalysis = growthDaysAnalysis
        )

        return ResultFactory.getSuccessResult(statistics)
    }

    fun getFarmerYieldTrends(farmerId: String, months: Int): Result<YieldTrendsResponseDto> {
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        val cutoffDate = LocalDate.now().minusMonths(months.toLong())
        val allYields = produces.flatMap { produceYieldRepository.findByFarmerProduceId(it.id) }
            .filter { it.harvestDate?.isAfter(cutoffDate) == true }

        if (allYields.isEmpty()) {
            return ResultFactory.getSuccessResult(YieldTrendsResponseDto(
                monthlyTrends = emptyList(),
                seasonalTrends = emptyList(),
                cropPerformanceTrends = emptyList(),
                overallTrend = "stable",
                trendConfidence = 0.0
            ))
        }

        // Calculate monthly trends
        val monthlyTrends = allYields.groupBy { 
            val date = it.harvestDate ?: LocalDate.now()
            "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
        }.map { (monthYear, yields) ->
            val parts = monthYear.split("-")
            MonthlyYieldTrend(
                month = parts[1],
                year = parts[0].toInt(),
                totalYield = yields.sumOf { it.yieldAmount },
                averageYield = yields.map { it.yieldAmount }.average(),
                harvestCount = yields.size
            )
        }.sortedBy { "${it.year}-${it.month}" }

        // Calculate seasonal trends
        val seasonalTrends = allYields.groupBy { it.seasonName ?: "Unknown" }
            .map { (season, yields) ->
                SeasonalYieldTrend(
                    season = season,
                    averageYield = yields.map { it.yieldAmount }.average(),
                    totalYield = yields.sumOf { it.yieldAmount },
                    harvestCount = yields.size,
                    years = yields.mapNotNull { it.harvestDate?.year }.distinct().sorted()
                )
            }

        // Calculate crop performance trends
        val cropPerformanceTrends = allYields.groupBy { it.farmerProduce.farmProduce.name }
            .map { (cropName, yields) ->
                val avgYield = yields.map { it.yieldAmount }.average()
                val avgGrowthDays = yields.mapNotNull { calculateGrowthDays(it) }.average()
                
                CropPerformanceTrend(
                    cropName = cropName,
                    averageYield = avgYield,
                    totalYield = yields.sumOf { it.yieldAmount },
                    harvestCount = yields.size,
                    averageGrowthDays = avgGrowthDays,
                    performanceRating = calculatePerformanceRating(avgYield, cropName)
                )
            }

        // Calculate overall trend
        val overallTrend = if (monthlyTrends.size >= 3) {
            val recent = monthlyTrends.takeLast(3).map { it.averageYield }.average()
            val earlier = monthlyTrends.dropLast(3).takeLast(3).map { it.averageYield }.average()
            when {
                recent > earlier * 1.1 -> "increasing"
                recent < earlier * 0.9 -> "decreasing"
                else -> "stable"
            }
        } else {
            "stable"
        }

        val trendConfidence = if (monthlyTrends.size >= 6) 0.8 else 0.4

        val trends = YieldTrendsResponseDto(
            monthlyTrends = monthlyTrends,
            seasonalTrends = seasonalTrends,
            cropPerformanceTrends = cropPerformanceTrends,
            overallTrend = overallTrend,
            trendConfidence = trendConfidence
        )

        return ResultFactory.getSuccessResult(trends)
    }

    // Helper methods

    private fun canRecordYieldForProduce(produce: FarmerProduce): Boolean {
        return produce.status.name in listOf("GROWING", "READY_TO_HARVEST", "HARVESTED")
    }

    private fun getCurrentSeason(): String {
        return when (LocalDate.now().monthValue) {
            12, 1, 2 -> "Winter"
            3, 4, 5 -> "Spring"
            6, 7, 8 -> "Summer"
            9, 10, 11 -> "Fall"
            else -> "Unknown"
        }
    }

    private fun getExpectedGrowthDays(cropName: String): Pair<Int, Int> {
        return when {
            cropName.contains("tomato") -> 60 to 100
            cropName.contains("corn") || cropName.contains("maize") -> 80 to 120
            cropName.contains("wheat") -> 120 to 150
            cropName.contains("rice") -> 100 to 140
            cropName.contains("beans") -> 50 to 90
            cropName.contains("potato") -> 70 to 100
            else -> 60 to 120 // Default range
        }
    }

    private fun getExpectedYieldRange(cropName: String, unit: String): YieldRange {
        val baseRanges = when {
            cropName.lowercase().contains("tomato") -> 2.0 to 8.0
            cropName.lowercase().contains("corn") -> 0.3 to 1.0
            cropName.lowercase().contains("wheat") -> 2.0 to 5.0
            cropName.lowercase().contains("rice") -> 3.0 to 6.0
            cropName.lowercase().contains("beans") -> 1.0 to 3.0
            cropName.lowercase().contains("potato") -> 1.5 to 4.0
            else -> 1.0 to 5.0 // Default range
        }

        return YieldRange(
            minYield = baseRanges.first,
            maxYield = baseRanges.second,
            unit = unit,
            confidence = "medium"
        )
    }

    private fun calculateGrowthDays(yield: ProduceYield): Int? {
        val plantingDate = yield.farmerProduce.plantingDate
        val harvestDate = yield.harvestDate
        return if (plantingDate != null && harvestDate != null) {
            ChronoUnit.DAYS.between(plantingDate, harvestDate).toInt()
        } else null
    }

    private fun calculatePerformanceRating(avgYield: Double, cropName: String): String {
        val expectedRange = getExpectedYieldRange(cropName, "kg")
        val midpoint = (expectedRange.minYield + expectedRange.maxYield) / 2
        
        return when {
            avgYield >= expectedRange.maxYield * 0.9 -> "excellent"
            avgYield >= midpoint -> "good"
            avgYield >= expectedRange.minYield -> "average"
            else -> "poor"
        }
    }

    private fun toYieldDto(yieldRecord: ProduceYield, produce: FarmerProduce): YieldRecordResponseDto {
        val growthDays = produce.plantingDate?.let { ChronoUnit.DAYS.between(it, yieldRecord.harvestDate).toInt() } ?: 0
        return YieldRecordResponseDto(
            id = yieldRecord.id,
            farmerProduceId = produce.id,
            produceName = produce.farmProduce.name,
            yieldAmount = yieldRecord.yieldAmount,
            yieldUnit = yieldRecord.yieldUnit,
            harvestDate = yieldRecord.harvestDate,
            seasonYear = yieldRecord.seasonYear,
            seasonName = yieldRecord.seasonName,
            notes = yieldRecord.notes,
            listedAmount = yieldRecord.listedAmount,
            remainingAmount = yieldRecord.remainingAmount,
            growthDays = growthDays,
            createdAt = yieldRecord.createdAt
        )
    }
}
