package com.agriconnect.farmersportalapis.application.dtos

import java.time.LocalDate
import java.time.LocalDateTime

data class HarvestPredictionDto(
    val id: String,
    val farmerProduceId: String,
    val farmerName: String,
    val produceName: String,
    val plantingDate: LocalDate?,
    val predictedHarvestDate: LocalDate?,
    val confidence: Double?, // 0.0 to 1.0
    val predictedSpecies: String? = null,
    val estimatedYield: Double? = null,
    val yieldUnit: String? = "kg",
    val growthStage: String? = null,
    val status: String = "PREDICTED", // PREDICTED, HARVESTED, OVERDUE
    val riskFactors: List<String>? = null,
    val recommendations: List<String>? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class BatchPredictionRequestDto(
    val farmerProduceIds: List<Long>
)

data class UpdatePredictionRequestDto(
    val actualHarvestDate: LocalDate
)

data class ManualHarvestOverrideRequestDto(
    val manualHarvestDate: LocalDate,
    val reason: String? = null,
    val notes: String? = null
)

data class ManualOverrideHistoryDto(
    val id: Long,
    val farmerProduceId: String,
    val originalPredictedDate: LocalDate?,
    val overriddenDate: LocalDate,
    val reason: String?,
    val notes: String?,
    val createdBy: String,
    val createdAt: LocalDateTime
)

data class ValidationResultDto(
    val isValid: Boolean,
    val warnings: List<String> = emptyList(),
    val errors: List<String> = emptyList(),
    val suggestions: List<String> = emptyList()
)