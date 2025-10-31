package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProduceYieldRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.format.DateTimeFormatter

@Service
class HarvestAnalyticsService(
    private val produceYieldRepository: ProduceYieldRepository
) {
    fun getFarmerAnalytics(farmerId: String): Result<HarvestAnalyticsResponseDto> {
        val yields = produceYieldRepository.findAll().filter { it.farmerProduce.farmer.id == farmerId }
        return ResultFactory.getSuccessResult(aggregateAnalytics(yields, farmerId = farmerId))
    }

    fun getProduceAnalytics(produceId: String): Result<HarvestAnalyticsResponseDto> {
        val yields = produceYieldRepository.findByFarmerProduceId(produceId)
        return ResultFactory.getSuccessResult(aggregateAnalytics(yields, produceId = produceId))
    }

    fun getSeasonalTrends(): Result<SeasonalTrendsResponseDto> {
        val yields = produceYieldRepository.findAll()
        val trends = yields.groupBy { it.harvestDate?.month?.name ?: "UNKNOWN" }
            .mapValues { entry ->
                entry.value.fold(BigDecimal.ZERO) { acc, y -> acc + BigDecimal.valueOf(y.yieldAmount) }
            }
        return ResultFactory.getSuccessResult(SeasonalTrendsResponseDto(trends))
    }

    fun getProduceComparison(): Result<ProduceComparisonResponseDto> {
        val yields = produceYieldRepository.findAll()
        val produceStats = yields.groupBy { it.farmerProduce.id }.map { (produceId, yieldsList) ->
            val produceName = yieldsList.firstOrNull()?.farmerProduce?.farmProduce?.name ?: "Unknown"
            val totalYield = yieldsList.fold(BigDecimal.ZERO) { acc, y -> acc + BigDecimal.valueOf(y.yieldAmount) }
            val avgYield = if (yieldsList.isNotEmpty()) totalYield.divide(BigDecimal(yieldsList.size), 2, java.math.RoundingMode.HALF_UP) else BigDecimal.ZERO
            ProduceStat(
                produceId = produceId,
                produceName = produceName,
                totalYield = totalYield,
                averageYield = avgYield
            )
        }
        return ResultFactory.getSuccessResult(ProduceComparisonResponseDto(produceStats))
    }

    private fun aggregateAnalytics(
        yields: List<com.agriconnect.farmersportalapis.domain.profile.ProduceYield>,
        farmerId: String? = null,
        produceId: String? = null
    ): HarvestAnalyticsResponseDto {
        val totalYields = yields.size
        val totalYieldAmount = yields.fold(BigDecimal.ZERO) { acc, y -> acc + BigDecimal.valueOf(y.yieldAmount) }
        val averageYield = if (totalYields > 0) totalYieldAmount.divide(BigDecimal(totalYields), 2, java.math.RoundingMode.HALF_UP) else BigDecimal.ZERO
        val yieldsByMonth = yields.groupBy {
            it.harvestDate?.format(DateTimeFormatter.ofPattern("yyyy-MM")) ?: "UNKNOWN"
        }.mapValues { entry ->
            entry.value.fold(BigDecimal.ZERO) { acc, y -> acc + BigDecimal.valueOf(y.yieldAmount) }
        }
        val bestMonth = yieldsByMonth.maxByOrNull { it.value }?.key
        val worstMonth = yieldsByMonth.minByOrNull { it.value }?.key
        val lastHarvestDate = yields.maxByOrNull { it.harvestDate ?: java.time.LocalDate.MIN }?.harvestDate
        return HarvestAnalyticsResponseDto(
            farmerId = farmerId,
            produceId = produceId,
            totalYields = totalYields,
            totalYieldAmount = totalYieldAmount,
            averageYield = averageYield,
            yieldsByMonth = yieldsByMonth,
            bestMonth = bestMonth,
            worstMonth = worstMonth,
            lastHarvestDate = lastHarvestDate
        )
    }
}
