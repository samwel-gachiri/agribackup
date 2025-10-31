package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDateTime

data class CreatePickupRouteRequestDto(
    val zoneId: String,
    val exporterId: String,
    val farmerIds: List<String>,
    val scheduledDate: LocalDateTime,
    val routeName: String? = null
)
