package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDateTime

data class PickupRouteResponseDto(
    val routeId: String,
    val zoneId: String,
    val exporterId: String,
    val zoneSupervisorId: String,
    val scheduledDate: LocalDateTime,
    val status: String,
    val totalDistanceKm: Double,
    val estimatedDurationMinutes: Int,
    val stops: List<PickupRouteStopDto>
)
