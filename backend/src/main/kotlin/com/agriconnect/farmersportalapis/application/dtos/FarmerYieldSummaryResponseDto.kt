package com.agriconnect.farmersportalapis.application.dtos

data class FarmerYieldSummaryResponseDto(
    val farmerId: String,
    val totalYields: Int,
    val totalYieldAmount: Double,
    val averageYield: Double
)
