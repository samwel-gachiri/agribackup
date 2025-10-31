package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDate

// Used for marking a produce as harvested
data class MarkHarvestedRequestDto(
    val actualHarvestDate: LocalDate
)
