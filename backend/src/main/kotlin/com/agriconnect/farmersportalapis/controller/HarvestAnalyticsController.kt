package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.HarvestAnalyticsService
import com.agriconnect.farmersportalapis.application.util.Result
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/harvest/analytics")
@Tag(name = "Harvest Analytics", description = "Analytics and performance metrics for harvest and yields")
class HarvestAnalyticsController(
    private val analyticsService: HarvestAnalyticsService
) {
    @Operation(summary = "Get analytics and yield summaries for a farmer")
    @GetMapping("/farmer/{farmerId}")
    fun getFarmerAnalytics(@PathVariable farmerId: String): Result<HarvestAnalyticsResponseDto> =
        analyticsService.getFarmerAnalytics(farmerId)

    @Operation(summary = "Get analytics and yield summaries for a produce")
    @GetMapping("/produce/{produceId}")
    fun getProduceAnalytics(@PathVariable produceId: String): Result<HarvestAnalyticsResponseDto> =
        analyticsService.getProduceAnalytics(produceId)

    @Operation(summary = "Get seasonal trends for all yields")
    @GetMapping("/seasonal-trends")
    fun getSeasonalTrends(): Result<SeasonalTrendsResponseDto> =
        analyticsService.getSeasonalTrends()

    @Operation(summary = "Compare performance between produces")
    @GetMapping("/produce-comparison")
    fun getProduceComparison(): Result<ProduceComparisonResponseDto> =
        analyticsService.getProduceComparison()
}
