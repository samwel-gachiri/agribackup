package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDate

// Used for returning harvest status for a produce
data class HarvestStatusResponseDto(
    val farmerProduceId: String,
    val produceName: String,
    val status: String,
    val plantingDate: LocalDate?,
    val predictedHarvestDate: LocalDate?,
    val daysToHarvest: Int?,
    val growthProgress: Int?,
    val totalYields: Int,
    val totalYieldAmount: Double,
    val lastHarvestDate: LocalDate?
)
