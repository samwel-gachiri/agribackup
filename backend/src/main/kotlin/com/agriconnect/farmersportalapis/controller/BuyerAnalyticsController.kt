package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.BuyerAnalyticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/buyer/analytics")
@CrossOrigin(origins = ["http://localhost:3000"])
class BuyerAnalyticsController(
    private val buyerAnalyticsService: BuyerAnalyticsService
) {

    @GetMapping("/{buyerId}/dashboard")
    fun getDashboardAnalytics(@PathVariable buyerId: String): ResponseEntity<BuyerDashboardAnalyticsDto> {
        val analytics = buyerAnalyticsService.getBuyerDashboardSummary(buyerId)
        return ResponseEntity.ok(analytics)
    }

    @GetMapping("/{buyerId}/farmer-performance")
    fun getFarmerPerformanceMetrics(@PathVariable buyerId: String): ResponseEntity<List<FarmerPerformanceAnalyticsDto>> {
        val metrics = buyerAnalyticsService.getFarmerPerformanceMetrics(buyerId)
        return ResponseEntity.ok(metrics)
    }

    @GetMapping("/{buyerId}/seasonal-trends")
    fun getSeasonalTrends(@PathVariable buyerId: String): ResponseEntity<SeasonalTrendsDto> {
        val trends = buyerAnalyticsService.getSeasonalAvailabilityTrends(buyerId)
        return ResponseEntity.ok(trends)
    }

    @GetMapping("/{buyerId}/farmer-comparison")
    fun getFarmerComparison(
        @PathVariable buyerId: String,
        @RequestParam farmerIds: List<String>
    ): ResponseEntity<List<FarmerPerformanceAnalyticsDto>> {
        val comparison = buyerAnalyticsService.getFarmerComparisonAnalytics(buyerId, farmerIds)
        return ResponseEntity.ok(comparison)
    }

    @GetMapping("/{buyerId}/order-analytics")
    fun getOrderAnalytics(@PathVariable buyerId: String): ResponseEntity<BuyerOrderAnalyticsDto> {
        val analytics = buyerAnalyticsService.getBuyerOrderAnalytics(buyerId)
        return ResponseEntity.ok(analytics)
    }
}