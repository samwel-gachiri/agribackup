package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.BuyerFarmerManagementService
import com.agriconnect.farmersportalapis.service.common.impl.BuyerAnalyticsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/buyer")
@CrossOrigin(origins = ["http://localhost:3000"])
class BuyerFarmerController(
    private val buyerFarmerManagementService: BuyerFarmerManagementService,
    private val buyerAnalyticsService: BuyerAnalyticsService
) {

    @PostMapping("/farmers/search")
    fun searchFarmers(@Valid @RequestBody request: BuyerFarmerSearchRequestDto): ResponseEntity<List<FarmerSearchResultDto>> {
        val farmers = buyerFarmerManagementService.searchAvailableFarmers(request)
        return ResponseEntity.ok(farmers)
    }

    @PostMapping("/{buyerId}/farmers/invite")
    fun inviteFarmer(
        @PathVariable buyerId: String,
        @Valid @RequestBody request: BuyerFarmerConnectionRequestDto
    ): ResponseEntity<BuyerFarmerConnectionResponseDto> {
        val connection = buyerFarmerManagementService.createFarmerConnection(buyerId, request)
        return ResponseEntity.ok(connection)
    }

    @PostMapping("/{buyerId}/farmers")
    fun connectFarmer(
        @PathVariable buyerId: String,
        @Valid @RequestBody request: BuyerFarmerConnectionRequestDto
    ): ResponseEntity<BuyerFarmerConnectionResponseDto> {
        val connection = buyerFarmerManagementService.createFarmerConnection(buyerId, request)
        return ResponseEntity.ok(connection)
    }

    @GetMapping("/{buyerId}/farmers")
    fun getBuyerFarmers(@PathVariable buyerId: String): ResponseEntity<List<BuyerFarmerResponseDto>> {
        val farmers = buyerFarmerManagementService.getBuyerFarmers(buyerId)
        return ResponseEntity.ok(farmers)
    }

    @DeleteMapping("/farmers/{connectionId}")
    fun removeFarmer(@PathVariable connectionId: String): ResponseEntity<Void> {
        buyerFarmerManagementService.removeFarmerConnection(connectionId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/farmers/{farmerId}/details")
    fun getFarmerDetails(
        @PathVariable farmerId: String,
        @RequestParam buyerId: String
    ): ResponseEntity<FarmerDetailsResponseDto> {
        val details = buyerFarmerManagementService.getFarmerDetails(farmerId, buyerId)
        return ResponseEntity.ok(details)
    }

    @PutMapping("/farmers/{connectionId}/notes")
    fun updateFarmerNotes(
        @PathVariable connectionId: String,
        @Valid @RequestBody request: UpdateFarmerNotesRequestDto
    ): ResponseEntity<Void> {
        buyerFarmerManagementService.updateFarmerNotes(connectionId, request.notes)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/{buyerId}/analytics/dashboard")
    fun getBuyerDashboardAnalytics(@PathVariable buyerId: String): ResponseEntity<BuyerDashboardAnalyticsDto> {
        val analytics = buyerFarmerManagementService.getBuyerDashboardAnalytics(buyerId)
        return ResponseEntity.ok(analytics)
    }

    @GetMapping("/{buyerId}/analytics/farmers")
    fun getFarmerPerformanceAnalytics(@PathVariable buyerId: String): ResponseEntity<List<FarmerPerformanceAnalyticsDto>> {
        val analytics = buyerFarmerManagementService.getFarmerPerformanceAnalytics(buyerId)
        return ResponseEntity.ok(analytics)
    }

    @GetMapping("/farmers/{farmerId}/performance")
    fun getFarmerPerformance(
        @PathVariable farmerId: String,
        @RequestParam buyerId: String
    ): ResponseEntity<FarmerPerformanceDto> {
        val performance = buyerFarmerManagementService.getFarmerPerformance(farmerId, buyerId)
        return ResponseEntity.ok(performance)
    }

    @GetMapping("/{buyerId}/analytics/seasonal-trends")
    fun getSeasonalTrends(@PathVariable buyerId: String): ResponseEntity<SeasonalTrendsDto> {
        val trends = buyerFarmerManagementService.getSeasonalTrends(buyerId)
        return ResponseEntity.ok(trends)
    }

    @GetMapping("/{buyerId}/analytics/charts")
    fun getBuyerCharts(@PathVariable buyerId: String): ResponseEntity<BuyerChartsResponseDto> {
        val charts = buyerAnalyticsService.getBuyerCharts(buyerId)
        return ResponseEntity.ok(charts)
    }
}