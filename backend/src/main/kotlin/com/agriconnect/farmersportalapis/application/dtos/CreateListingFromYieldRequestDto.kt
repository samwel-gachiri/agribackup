package com.agriconnect.farmersportalapis.application.dtos

import java.math.BigDecimal
import java.time.LocalDate

data class CreateListingFromYieldRequestDto(
    val yieldId: String,
    val listingAmount: Double,
    val pricePerUnit: BigDecimal,
    val description: String?,
    val availableFrom: LocalDate,
    val availableTo: LocalDate?
)
