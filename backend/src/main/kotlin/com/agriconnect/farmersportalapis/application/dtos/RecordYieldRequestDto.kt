package com.agriconnect.farmersportalapis.application.dtos

import java.math.BigDecimal
import java.time.LocalDate

data class RecordYieldRequestDto(
    val farmerProduceId: String,
    val yieldAmount: Double,
    val yieldUnit: String,
    val harvestDate: LocalDate,
    val seasonYear: Int?,
    val seasonName: String?,
    val notes: String?
)
