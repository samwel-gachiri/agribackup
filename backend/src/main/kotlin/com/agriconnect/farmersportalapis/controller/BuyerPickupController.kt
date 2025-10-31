package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.BuyerPickupRouteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/buyer")
@CrossOrigin(origins = ["http://localhost:3000"])
class BuyerPickupController(
    private val buyerPickupRouteService: BuyerPickupRouteService
) {

    @PostMapping("/pickup/{buyerId}/routes/generate")
    fun generatePickupRoute(
        @PathVariable buyerId: String,
        @Valid @RequestBody request: GenerateRouteRequestDto
    ): ResponseEntity<BuyerPickupRouteResponseDto> {
        val route = buyerPickupRouteService.generateOptimalRoute(buyerId, request)
        return ResponseEntity.ok(route)
    }

    @GetMapping("/pickup/{buyerId}/routes")
    fun getBuyerRoutes(@PathVariable buyerId: String): ResponseEntity<List<BuyerPickupRouteResponseDto>> {
        val routes = buyerPickupRouteService.getBuyerRoutes(buyerId)
        return ResponseEntity.ok(routes)
    }

    @GetMapping("/pickup/routes/{routeId}")
    fun getRouteDetails(@PathVariable routeId: String): ResponseEntity<PickupRouteDetailsDto> {
        val details = buyerPickupRouteService.getRouteDetails(routeId)
        return ResponseEntity.ok(details)
    }

    @PutMapping("/pickup/routes/{routeId}/confirm")
    fun confirmRoute(@PathVariable routeId: String): ResponseEntity<BuyerPickupRouteResponseDto> {
        val route = buyerPickupRouteService.confirmRoute(routeId)
        return ResponseEntity.ok(route)
    }

    @PutMapping("/pickup/routes/{routeId}/optimize")
    fun optimizeRoute(
        @PathVariable routeId: String,
        @Valid @RequestBody request: OptimizeRouteRequestDto
    ): ResponseEntity<BuyerPickupRouteResponseDto> {
        val route = buyerPickupRouteService.optimizeRoute(routeId, request)
        return ResponseEntity.ok(route)
    }

    @DeleteMapping("/pickup/routes/{routeId}")
    fun cancelRoute(@PathVariable routeId: String): ResponseEntity<Void> {
        buyerPickupRouteService.cancelRoute(routeId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{buyerId}/farmers/available-for-pickup")
    fun getAvailableFarmersForPickup(@PathVariable buyerId: String): ResponseEntity<List<FarmerAvailabilityDto>> {
        val farmers = buyerPickupRouteService.getAvailableFarmersForPickup(buyerId)
        return ResponseEntity.ok(farmers)
    }

    @PostMapping("/pickup/routes/{routeId}/notify-farmers")
    fun notifyFarmersOfPickup(@PathVariable routeId: String): ResponseEntity<NotificationResponseDto> {
        val response = buyerPickupRouteService.notifyFarmersOfPickup(routeId)
        return ResponseEntity.ok(response)
    }
}