package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDate

data class HarvestPredictionDto2(
    val farmerProduceId: String,
    val farmerId: String?,
    val farmerName: String,
    val produceName: String,
    val plantingDate: LocalDate?,
    val predictedHarvestDate: LocalDate?,
    val predictedSpecies: String?,
    val confidence: Double?,
    val status: String,
    val actualHarvestDate: LocalDate?,
    val id: String
)
