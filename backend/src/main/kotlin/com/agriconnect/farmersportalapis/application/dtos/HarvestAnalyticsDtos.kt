package com.agriconnect.farmersportalapis.application.dtos

import java.math.BigDecimal
import java.time.LocalDate

data class HarvestAnalyticsResponseDto(
    val farmerId: String? = null,
    val produceId: String? = null,
    val totalYields: Int,
    val totalYieldAmount: BigDecimal,
    val averageYield: BigDecimal,
    val yieldsByMonth: Map<String, BigDecimal>,
    val bestMonth: String?,
    val worstMonth: String?,
    val lastHarvestDate: LocalDate?
)

data class SeasonalTrendsResponseDto(
    val trends: Map<String, BigDecimal>
)

data class ProduceComparisonResponseDto(
    val produceStats: List<ProduceStat>
)

data class ProduceStat(
    val produceId: String,
    val produceName: String,
    val totalYield: BigDecimal,
    val averageYield: BigDecimal
)
