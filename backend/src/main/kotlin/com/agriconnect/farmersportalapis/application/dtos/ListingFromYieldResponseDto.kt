package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDate

data class ListingFromYieldResponseDto(
    val listingId: String,
    val yieldId: String,
    val listedAmount: Double,
    val remainingYield: Double,
    val produceName: String,
    val harvestDate: LocalDate?,
    val message: String
)
