package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDateTime

// Shared DTO for a single stop on a pickup route
data class PickupRouteStopDto(
    val stopId: String?,
    val farmerId: String,
    val farmerName: String?,
    val sequenceOrder: Int,
    val status: String,
    val arrivalTime: LocalDateTime?,
    val completionTime: LocalDateTime?,
    val notes: String?,
    val latitude: Double? = null,
    val longitude: Double? = null
)
