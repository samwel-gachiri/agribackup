package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDate
import java.time.LocalDateTime

data class BuyerPickupRouteResponseDto(
    val routeId: String,
    val zoneId: String,
    val exporterId: String,
    val zoneSupervisorId: String,
    val scheduledDate: LocalDateTime,
    val status: String,
    val totalDistanceKm: Double,
    val estimatedDurationMinutes: Int,
    val stops: List<PickupRouteStopDto>,
    val buyerId: String? = null,
    val totalCost: Double? = null,
    val pickupDate: LocalDate? = null,
    val startTime: String? = null,
    val waypoints: List<RouteWaypointDto>? = null,
    val routeGeometry: String? = null,
    val createdAt: LocalDateTime? = null,
    val confirmedAt: LocalDateTime? = null
)
