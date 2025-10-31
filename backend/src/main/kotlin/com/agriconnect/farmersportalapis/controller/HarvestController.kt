package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.HarvestPredictionService
import com.agriconnect.farmersportalapis.service.common.impl.HarvestManagementService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/harvest")
@Tag(name = "Harvest Management", description = "Manages crop growth and harvest lifecycle with AI predictions")
class HarvestController(
    private val harvestService: HarvestManagementService,
    private val harvestPredictionService: HarvestPredictionService
) {

    val logger = LoggerFactory.getLogger(HarvestController::class.java)

    @Operation(summary = "Start growth for a produce (planting)")
    @PostMapping("/start-growth")
    fun startGrowth(@RequestBody dto: StartGrowthRequestDto): Result<HarvestStatusResponseDto> {
        return try {
            logger.info("Received start-growth request: farmerProduceId=${dto.farmerProduceId}, plantingDate=${dto.plantingDate}")
            val result = harvestService.startGrowth(dto, null)
            logger.info("Start-growth result: success=${result.success}, message=${result.msg}")
            result
        } catch (e: Exception) {
            logger.error("Error in start-growth endpoint", e)
            ResultFactory.getFailResult("Internal server error: ${e.message}")
        }
    }

    @Operation(summary = "Mark a produce as harvested (single or multiple harvests)")
    @PostMapping("/{farmerProduceId}/mark-harvested")
    fun markHarvested(
            @PathVariable farmerProduceId: String,
            @RequestBody dto: MarkHarvestedRequestDto
    ): Result<HarvestStatusResponseDto> = harvestService.markHarvested(farmerProduceId, dto)

    @Operation(summary = "Get harvest status for all produces of a farmer")
    @GetMapping("/farmer/{farmerId}/status")
    fun getHarvestStatus(@PathVariable farmerId: String): Result<List<HarvestStatusResponseDto>> =
            harvestService.getHarvestStatusForFarmer(farmerId)

    @Operation(summary = "Get produces ready to harvest for dashboard display")
    @GetMapping("/farmer/{farmerId}/ready-to-harvest")
    fun getReadyToHarvest(@PathVariable farmerId: String): Result<List<HarvestStatusResponseDto>> =
            harvestService.getReadyToHarvest(farmerId)

    // AI-Enhanced Prediction Endpoints
    
    @Operation(summary = "Get AI-powered harvest prediction for a specific farmer produce")
    @GetMapping("/predictions/{farmerProduceId}")
    fun getHarvestPrediction(@PathVariable farmerProduceId: String): Result<HarvestPredictionDto> {
        return try {
            val prediction = harvestPredictionService.predictHarvest(farmerProduceId)
            ResultFactory.getSuccessResult(prediction)
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to generate harvest prediction: ${e.message}")
        }
    }

    @Operation(summary = "Get AI predictions for all farmer produces")
    @PostMapping("/predictions/batch")
    fun getBatchHarvestPredictions(@RequestBody farmerProduceIds: List<String>): Result<List<HarvestPredictionDto>> {
        return try {
            val predictions = harvestPredictionService.predictHarvestBatch(farmerProduceIds)
            ResultFactory.getSuccessResult(predictions)
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to generate batch predictions: ${e.message}")
        }
    }

    @Operation(summary = "Get all harvest predictions for a farmer")
    @GetMapping("/farmer/{farmerId}/predictions")
    fun getFarmerHarvestPredictions(@PathVariable farmerId: String): Result<List<HarvestPredictionDto>> {
        return try {
            // Get all farmer produces for this farmer
            val farmerProduces = harvestService.getHarvestStatusForFarmer(farmerId)
            if (farmerProduces.success) {
                val farmerProduceIds = farmerProduces.data?.mapNotNull { it.farmerProduceId } ?: emptyList()
                val predictions = harvestPredictionService.predictHarvestBatch(farmerProduceIds)
                ResultFactory.getSuccessResult(predictions)
            } else {
                ResultFactory.getFailResult("Failed to get farmer produces")
            }
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to get farmer predictions: ${e.message}")
        }
    }

    @Operation(summary = "Update prediction with actual harvest date for learning")
    @PutMapping("/predictions/{farmerProduceId}/actual")
    fun updatePredictionWithActual(
        @PathVariable farmerProduceId: String,
        @RequestBody actualHarvestDate: LocalDate
    ): Result<HarvestPredictionDto> {
        return try {
            val updatedPrediction = harvestPredictionService.updatePrediction(farmerProduceId, actualHarvestDate)
            ResultFactory.getSuccessResult(updatedPrediction)
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to update prediction: ${e.message}")
        }
    }

    @Operation(summary = "Get harvest predictions with fallback mechanism")
    @GetMapping("/predictions/{farmerProduceId}/with-fallback")
    fun getHarvestPredictionWithFallback(@PathVariable farmerProduceId: String): Result<HarvestPredictionDto> {
        return try {
            // Try AI prediction first
            val prediction = harvestPredictionService.predictHarvest(farmerProduceId)
            ResultFactory.getSuccessResult(prediction, "AI prediction generated successfully")
        } catch (aiException: Exception) {
            try {
                // Fallback to heuristic prediction
                val prediction = harvestPredictionService.predictHarvest(farmerProduceId)
                ResultFactory.getSuccessResult(prediction, "Fallback prediction generated (AI unavailable)")
            } catch (fallbackException: Exception) {
                ResultFactory.getFailResult("Both AI and fallback predictions failed: ${fallbackException.message}")
            }
        }
    }

    // Manual Harvest Override Endpoints

    @Operation(summary = "Override predicted harvest date with manual date")
    @PutMapping("/{farmerProduceId}/override-harvest-date")
    fun overrideHarvestDate(
        @PathVariable farmerProduceId: String,
        @RequestBody request: ManualHarvestOverrideRequestDto
    ): Result<HarvestStatusResponseDto> {
        return try {
            val result = harvestService.overrideHarvestDate(farmerProduceId, request)
            if (result.success) {
                ResultFactory.getSuccessResult(result.data!!, "Harvest date overridden successfully")
            } else {
                ResultFactory.getFailResult(result.msg ?: "Failed to override harvest date")
            }
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to override harvest date: ${e.message}")
        }
    }

    @Operation(summary = "Get manual override history for a farmer produce")
    @GetMapping("/{farmerProduceId}/override-history")
    fun getOverrideHistory(@PathVariable farmerProduceId: String): Result<List<ManualOverrideHistoryDto>> {
        return try {
            val history = harvestService.getManualOverrideHistory(farmerProduceId)
            ResultFactory.getSuccessResult(history)
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to get override history: ${e.message}")
        }
    }

    @Operation(summary = "Validate manual harvest date override")
    @PostMapping("/{farmerProduceId}/validate-override")
    fun validateHarvestOverride(
        @PathVariable farmerProduceId: String,
        @RequestBody request: ManualHarvestOverrideRequestDto
    ): Result<ValidationResultDto> {
        return try {
            val validation = harvestService.validateManualOverride(farmerProduceId, request)
            ResultFactory.getSuccessResult(validation)
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to validate override: ${e.message}")
        }
    }

    @Operation(summary = "Remove manual override and revert to AI prediction")
    @DeleteMapping("/{farmerProduceId}/remove-override")
    fun removeManualOverride(@PathVariable farmerProduceId: String): Result<HarvestStatusResponseDto> {
        return try {
            val result = harvestService.removeManualOverride(farmerProduceId)
            if (result.success) {
                ResultFactory.getSuccessResult(result.data!!, "Manual override removed successfully")
            } else {
                ResultFactory.getFailResult(result.msg ?: "Failed to remove manual override")
            }
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to remove manual override: ${e.message}")
        }
    }
}
