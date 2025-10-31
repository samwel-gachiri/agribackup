package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDate

data class UpdateYieldRequestDto(
    val yieldAmount: Double?,
    val yieldUnit: String?,
    val harvestDate: LocalDate?,
    val seasonYear: Int?,
    val seasonName: String?,
    val notes: String?
)
