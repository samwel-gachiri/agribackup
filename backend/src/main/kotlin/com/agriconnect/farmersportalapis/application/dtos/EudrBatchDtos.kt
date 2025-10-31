package com.agriconnect.farmersportalapis.application.dtos

import com.agriconnect.farmersportalapis.domain.eudr.CountryRiskLevel
import java.math.BigDecimal
import java.time.LocalDate

data class CreateBatchRequestDto(
    val batchCode: String,
    val commodityDescription: String,
    val hsCode: String? = null,
    val quantity: BigDecimal,
    val unit: String,
    val countryOfProduction: String,
    val harvestDate: LocalDate? = null,
    val harvestPeriodStart: LocalDate? = null,
    val harvestPeriodEnd: LocalDate? = null,
    val productionUnitIds: List<String> = emptyList()
)

data class BatchResponseDto(
    val id: String,
    val batchCode: String,
    val commodityDescription: String,
    val hsCode: String?,
    val quantity: BigDecimal,
    val unit: String,
    val countryOfProduction: String,
    val countryRiskLevel: CountryRiskLevel,
    val harvestDate: LocalDate?,
    val harvestPeriodStart: LocalDate?,
    val harvestPeriodEnd: LocalDate?,
    val createdBy: String,
    val createdAt: String,
    val status: String,
    val riskLevel: String?,
    val riskRationale: String?,
    val hederaTransactionId: String?,
    val productionUnitCount: Int,
    val documentCount: Int
)

data class UpdateBatchStatusRequestDto(
    val newStatus: String,
    val reason: String? = null
)

data class BatchTransferRequestDto(
    val toActorId: String,
    val toActorRole: String,
    val transferReason: String? = null
)