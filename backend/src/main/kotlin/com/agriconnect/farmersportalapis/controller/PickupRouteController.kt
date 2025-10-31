package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.ZoneSupervisorService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.infrastructure.repositories.PickupRouteRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.PickupRouteStopRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin-service/pickup-routes")
@Tag(name = "Pickup Route Management", description = "APIs for managing multi-stop pickup routes")
class PickupRouteController(
    private val zoneSupervisorService: ZoneSupervisorService,
    private val pickupRouteRepository: PickupRouteRepository,
    private val pickupRouteStopRepository: PickupRouteStopRepository
) {

    @PostMapping
    @Operation(summary = "Create a pickup route")
    fun createRoute(@RequestBody request: CreatePickupRouteRequestDto,
                    @AuthenticationPrincipal user: UserDetails): Result<BuyerPickupRouteResponseDto> =
        zoneSupervisorService.createPickupRoute(request, user.username, pickupRouteRepository, pickupRouteStopRepository)

    @GetMapping("/{routeId}")
    @Operation(summary = "Get a pickup route")
    fun getRoute(@PathVariable routeId: String, @AuthenticationPrincipal user: UserDetails): Result<BuyerPickupRouteResponseDto> =
        zoneSupervisorService.getPickupRoute(routeId, user.username, pickupRouteRepository, pickupRouteStopRepository)

    @GetMapping
    @Operation(summary = "List pickup routes for a date")
    fun listRoutes(@RequestParam("date") date: String, @AuthenticationPrincipal user: UserDetails): Result<List<PickupRouteSummaryDto>> {
        val localDate = java.time.LocalDate.parse(date)
        return zoneSupervisorService.listPickupRoutes(localDate, user.username, pickupRouteRepository)
    }

    @PatchMapping("/{routeId}/status")
    @Operation(summary = "Update route status")
    fun updateRouteStatus(@PathVariable routeId: String, @RequestBody dto: UpdatePickupRouteStatusDto,
                          @AuthenticationPrincipal user: UserDetails): Result<BuyerPickupRouteResponseDto> =
        zoneSupervisorService.updateRouteStatus(routeId, dto, user.username, pickupRouteRepository)

    @PatchMapping("/{routeId}/stops/{stopId}")
    @Operation(summary = "Update pickup stop status")
    fun updateStopStatus(@PathVariable routeId: String, @PathVariable stopId: String,
                         @RequestBody dto: UpdatePickupStopStatusDto,
                         @AuthenticationPrincipal user: UserDetails): Result<BuyerPickupRouteResponseDto> =
        zoneSupervisorService.updateStopStatus(routeId, stopId, dto, user.username, pickupRouteRepository, pickupRouteStopRepository)

    @GetMapping("/suggestions")
    @Operation(summary = "Suggest pickups from harvest predictions")
    fun suggestPickups(
        @RequestParam exporterId: String,
        @RequestParam start: String,
        @RequestParam end: String,
        @RequestParam(required = false) zoneId: String?,
        @RequestParam(required = false) minConfidence: Double?
    ): Result<List<SuggestedPickupDto>> {
        val s = java.time.LocalDate.parse(start)
        val e = java.time.LocalDate.parse(end)
        return zoneSupervisorService.suggestPickups(exporterId, s, e, zoneId, minConfidence)
    }
}
