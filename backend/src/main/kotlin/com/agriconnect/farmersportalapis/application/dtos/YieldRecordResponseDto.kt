package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDate
import java.time.LocalDateTime

data class YieldRecordResponseDto(
    val id: String,
    val farmerProduceId: String,
    val produceName: String,
    val yieldAmount: Double,
    val yieldUnit: String,
    val harvestDate: LocalDate?,
    val seasonYear: Int?,
    val seasonName: String?,
    val notes: String?,
    val listedAmount: Double,
    val remainingAmount: Double,
    val growthDays: Int,
    val createdAt: LocalDateTime
)
