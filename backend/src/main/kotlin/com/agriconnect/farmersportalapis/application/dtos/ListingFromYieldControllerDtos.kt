package com.agriconnect.farmersportalapis.application.dtos

data class YieldAvailableAmountResponseDto(
    val yieldId: String,
    val availableAmount: Double
)

data class UpdateListedAmountRequestDto(
    val listedAmount: Double
)

data class YieldToListingRequestDto(
    val yieldId: String,
    val listingAmount: Double,
    val pricePerUnit: Double
)
