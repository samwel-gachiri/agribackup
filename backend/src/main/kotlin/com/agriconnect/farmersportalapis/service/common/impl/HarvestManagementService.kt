package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.common.enums.FarmerProduceStatus
import com.agriconnect.farmersportalapis.domain.profile.FarmerProduce
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerProduceRepository
import com.agriconnect.farmersportalapis.service.common.AiService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class HarvestManagementService(
        private val farmerProduceRepository: FarmerProduceRepository,
        private val aiService: AiService
) {
    fun startGrowth(
            dto: StartGrowthRequestDto,
            imageBytes: ByteArray? = null
    ): Result<HarvestStatusResponseDto> {
        // Validate planting date
        if (dto.plantingDate.isAfter(LocalDate.now())) {
            return ResultFactory.getFailResult("Planting date cannot be in the future.")
        }
        val produce =
                farmerProduceRepository.findById(dto.farmerProduceId).orElse(null)
                        ?: return ResultFactory.getFailResult("Produce not found.")

        // Call AI for prediction
        val aiPrompt = buildString {
            append("Predict the harvest date for the following crop.\n")
            append("Planting date: ${dto.plantingDate}\n")
            append("Produce: ${produce.farmProduce.name}\n")
            if (!dto.notes.isNullOrBlank()) append("Notes: ${dto.notes}\n")
            append("Current status: ${produce.status}\n")
            append("If an image is provided, use it to detect the growth stage.\n")
        }
        // For now, pass imageBytes as null or base64 if your AI supports it
        val aiResult = aiService.getChatCompletion(aiPrompt, produce.farmer.id ?: "unknown")

        // Parse AI result (assume JSON or structured string, adapt as needed)
        // Example expected: { "predictedHarvestDate": "2025-12-01", "confidence": 0.92, "species":
        // "Maize", "modelVersion": "ai-v2" }
        val predictedDate =
                Regex("predictedHarvestDate\\s*[:=]\\s*([0-9-]+)")
                        .find(aiResult ?: "")
                        ?.groupValues
                        ?.getOrNull(1)
        val confidence =
                Regex("confidence\\s*[:=]\\s*([0-9.]+)")
                        .find(aiResult ?: "")
                        ?.groupValues
                        ?.getOrNull(1)
                        ?.toDoubleOrNull()
        val species =
                Regex("species\\s*[:=]\\s*([A-Za-z0-9 ]+)")
                        .find(aiResult ?: "")
                        ?.groupValues
                        ?.getOrNull(1)
        val modelVersion =
                Regex("modelVersion\\s*[:=]\\s*([A-Za-z0-9.-]+)")
                        .find(aiResult ?: "")
                        ?.groupValues
                        ?.getOrNull(1)

        produce.plantingDate = dto.plantingDate
        produce.status = FarmerProduceStatus.GROWING
        produce.predictedHarvestDate = predictedDate?.let { LocalDate.parse(it) }
        produce.predictionConfidence = confidence ?: 0.0
        produce.predictedSpecies = species ?: produce.farmProduce.name
        produce.aiModelVersion = modelVersion ?: "ai-v1"
        farmerProduceRepository.save(produce)
        return ResultFactory.getSuccessResult(toStatusDto(produce))
    }

    fun markHarvested(
            farmerProduceId: String,
            dto: MarkHarvestedRequestDto
    ): Result<HarvestStatusResponseDto> {
        val produce =
                farmerProduceRepository.findById(farmerProduceId).orElse(null)
                        ?: return ResultFactory.getFailResult("Produce not found.")
        produce.actualHarvestDate = dto.actualHarvestDate
        produce.status = FarmerProduceStatus.HARVESTED
        farmerProduceRepository.save(produce)
        return ResultFactory.getSuccessResult(toStatusDto(produce))
    }

    fun getHarvestStatusForFarmer(farmerId: String): Result<List<HarvestStatusResponseDto>> {
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        return ResultFactory.getSuccessResult(produces.map { toStatusDto(it) })
    }

    fun getReadyToHarvest(farmerId: String): Result<List<HarvestStatusResponseDto>> {
        val produces =
                farmerProduceRepository.findByFarmerId(farmerId).filter {
                    it.status == FarmerProduceStatus.READY_TO_HARVEST
                }
        return ResultFactory.getSuccessResult(data = produces.map { toStatusDto(it) })
    }

    private fun toStatusDto(produce: FarmerProduce): HarvestStatusResponseDto {
        val daysToHarvest =
                produce.plantingDate?.let { planting ->
                    produce.predictedHarvestDate?.let {
                        ChronoUnit.DAYS.between(planting, it).toInt()
                    }
                }
        val growthProgress =
                produce.plantingDate?.let { planting ->
                    produce.predictedHarvestDate?.let { predicted ->
                        val total = ChronoUnit.DAYS.between(planting, predicted).toDouble()
                        val elapsed = ChronoUnit.DAYS.between(planting, LocalDate.now()).toDouble()
                        ((elapsed / total) * 100).toInt().coerceIn(0, 100)
                    }
                }
                        ?: 0
        return HarvestStatusResponseDto(
                farmerProduceId = produce.id,
                produceName = produce.farmProduce.name,
                status = produce.status.name,
                plantingDate = produce.plantingDate,
                predictedHarvestDate = produce.predictedHarvestDate,
                daysToHarvest = daysToHarvest,
                growthProgress = growthProgress,
                totalYields = produce.yields.size,
                totalYieldAmount = produce.yields.sumOf { it.yieldAmount },
                lastHarvestDate =
                        produce.yields
                                .maxByOrNull { it.harvestDate ?: java.time.LocalDate.MIN }
                                ?.harvestDate
        )
    }

    // Manual Harvest Override Methods

    fun overrideHarvestDate(
        farmerProduceId: String,
        request: ManualHarvestOverrideRequestDto
    ): Result<HarvestStatusResponseDto> {
        val produce = farmerProduceRepository.findById(farmerProduceId).orElse(null)
            ?: return ResultFactory.getFailResult("Produce not found.")

        // Validate the override date
        val validation = validateManualOverride(farmerProduceId, request)
        if (!validation.isValid) {
            return ResultFactory.getFailResult("Invalid override date: ${validation.errors.joinToString(", ")}")
        }

        // Store original predicted date for history
        val originalPredictedDate = produce.predictedHarvestDate

        // Apply the override
        produce.predictedHarvestDate = request.manualHarvestDate
        produce.manualOverride = true
        produce.manualOverrideReason = request.reason
        produce.manualOverrideNotes = request.notes
        produce.manualOverrideDate = LocalDateTime.now()

        // Save the override history (in a real implementation, you'd have a separate entity)
        // For now, we'll store it in the produce entity fields
        farmerProduceRepository.save(produce)

        return ResultFactory.getSuccessResult(toStatusDto(produce))
    }

    fun validateManualOverride(
        farmerProduceId: String,
        request: ManualHarvestOverrideRequestDto
    ): ValidationResultDto {
        val produce = farmerProduceRepository.findById(farmerProduceId).orElse(null)
            ?: return ValidationResultDto(
                isValid = false,
                errors = listOf("Produce not found")
            )

        val warnings = mutableListOf<String>()
        val errors = mutableListOf<String>()
        val suggestions = mutableListOf<String>()

        // Validate planting date exists
        val plantingDate = produce.plantingDate
        if (plantingDate == null) {
            errors.add("Cannot override harvest date: planting date not set")
            return ValidationResultDto(isValid = false, errors = errors)
        }

        // Validate override date is not in the past
        if (request.manualHarvestDate.isBefore(LocalDate.now())) {
            errors.add("Manual harvest date cannot be in the past")
        }

        // Validate override date is after planting date
        if (request.manualHarvestDate.isBefore(plantingDate)) {
            errors.add("Manual harvest date cannot be before planting date")
        }

        // Check if override date is significantly different from AI prediction
        produce.predictedHarvestDate?.let { aiPredicted ->
            val daysDifference = ChronoUnit.DAYS.between(aiPredicted, request.manualHarvestDate)
            when {
                Math.abs(daysDifference) > 30 -> {
                    warnings.add("Manual date differs significantly from AI prediction (${Math.abs(daysDifference)} days)")
                    suggestions.add("Consider reviewing the planting date or crop type")
                }
                Math.abs(daysDifference) > 14 -> {
                    warnings.add("Manual date differs from AI prediction by ${Math.abs(daysDifference)} days")
                }
            }
        }

        // Check growing period reasonableness
        val growingDays = ChronoUnit.DAYS.between(plantingDate, request.manualHarvestDate)
        val cropName = produce.farmProduce.name.lowercase()
        
        when {
            growingDays < 30 -> {
                warnings.add("Growing period seems very short (${growingDays} days)")
                suggestions.add("Verify planting date and crop type")
            }
            growingDays > 365 -> {
                warnings.add("Growing period seems very long (${growingDays} days)")
                suggestions.add("Consider if this is a perennial crop or verify dates")
            }
            cropName.contains("tomato") && (growingDays < 60 || growingDays > 120) -> {
                warnings.add("Unusual growing period for tomatoes (${growingDays} days, typical: 75-90 days)")
            }
            cropName.contains("corn") && (growingDays < 70 || growingDays > 140) -> {
                warnings.add("Unusual growing period for corn (${growingDays} days, typical: 90-120 days)")
            }
        }

        return ValidationResultDto(
            isValid = errors.isEmpty(),
            warnings = warnings,
            errors = errors,
            suggestions = suggestions
        )
    }

    fun getManualOverrideHistory(farmerProduceId: String): List<ManualOverrideHistoryDto> {
        val produce = farmerProduceRepository.findById(farmerProduceId).orElse(null)
            ?: return emptyList()

        // In a real implementation, you'd query a separate override history table
        // For now, return current override if it exists
        return if (produce.manualOverride == true) {
            listOf(
                ManualOverrideHistoryDto(
                    id = produce.id?.hashCode()?.toLong() ?: 0L,
                    farmerProduceId = produce.id,
                    originalPredictedDate = null, // Would need to store this separately
                    overriddenDate = produce.predictedHarvestDate ?: LocalDate.now(),
                    reason = produce.manualOverrideReason,
                    notes = produce.manualOverrideNotes,
                    createdBy = produce.farmer?.userProfile?.fullName ?: "Unknown",
                    createdAt = produce.manualOverrideDate ?: LocalDateTime.now()
                )
            )
        } else {
            emptyList()
        }
    }

    fun removeManualOverride(farmerProduceId: String): Result<HarvestStatusResponseDto> {
        val produce = farmerProduceRepository.findById(farmerProduceId).orElse(null)
            ?: return ResultFactory.getFailResult("Produce not found.")

        if (produce.manualOverride != true) {
            return ResultFactory.getFailResult("No manual override found to remove.")
        }

        // Remove manual override and regenerate AI prediction
        produce.manualOverride = false
        produce.manualOverrideReason = null
        produce.manualOverrideNotes = null
        produce.manualOverrideDate = null

        // Regenerate AI prediction (simplified - in reality you'd call the AI service)
        produce.predictedHarvestDate = generateFallbackPrediction(produce)

        farmerProduceRepository.save(produce)

        return ResultFactory.getSuccessResult(toStatusDto(produce))
    }

    private fun generateFallbackPrediction(produce: FarmerProduce): LocalDate? {
        val plantingDate = produce.plantingDate ?: return null
        val cropName = produce.farmProduce.name.lowercase()
        
        // Simple heuristic prediction based on crop type
        val averageDays = when {
            cropName.contains("tomato") -> 80
            cropName.contains("corn") || cropName.contains("maize") -> 100
            cropName.contains("wheat") -> 130
            cropName.contains("rice") -> 120
            cropName.contains("beans") -> 75
            cropName.contains("potato") -> 85
            else -> 90 // Default
        }
        
        return plantingDate.plusDays(averageDays.toLong())
    }
}
