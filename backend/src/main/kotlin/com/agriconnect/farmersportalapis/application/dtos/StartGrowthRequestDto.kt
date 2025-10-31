package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDate

// Used for starting growth/planting
data class StartGrowthRequestDto(
        val farmerProduceId: String,
        val plantingDate: LocalDate,
        val imageUrl: String? = null, // Optional image for AI
        val notes: String? = null // Optional notes/context for AI
)
