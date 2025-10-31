package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.HarvestPredictionDto2
import com.agriconnect.farmersportalapis.service.common.impl.ExporterService
import com.agriconnect.farmersportalapis.service.supplychain.FarmerService
import com.agriconnect.farmersportalapis.service.supplychain.FarmerService.HarvestPredictionRequest
import com.agriconnect.farmersportalapis.service.supplychain.FarmerService.HarvestPredictionResponse
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerProduceRepository
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/harvest/predictions")
class HarvestPredictionController(
    private val farmerService: FarmerService,
    private val exporterService: ExporterService,
    private val farmerProduceRepository: FarmerProduceRepository
) {
    data class PredictHarvestDto(
        val farmerProduceId: String,
        val plantingDate: LocalDate
    )

    data class MarkHarvestedDto(
        val actualHarvestDate: LocalDate
    )

    @PostMapping("/predict")
    fun predict(@RequestBody dto: PredictHarvestDto): Result<HarvestPredictionResponse> {
        val auth = SecurityContextHolder.getContext().authentication
        // Future: validate ownership/permissions
        val request = HarvestPredictionRequest(
            farmerProduceId = dto.farmerProduceId,
            plantingDate = dto.plantingDate
        )
        return farmerService.predictHarvest(request)
    }

    @PostMapping("/{farmerProduceId}/harvested")
    fun markHarvested(@PathVariable farmerProduceId: String, @RequestBody dto: MarkHarvestedDto): Result<HarvestPredictionResponse> {
        return farmerService.markHarvested(farmerProduceId, dto.actualHarvestDate)
    }

    // Exporter view of all predictions across its network
    @GetMapping("/exporter/{exporterId}/predictions")
    fun exporterPredictions(@PathVariable exporterId: String): Result<List<HarvestPredictionDto2>> {
        return exporterService.listHarvestPredictions(exporterId, farmerProduceRepository)
    }

    // Farmer-specific stored prediction list (for buyer/exporter to poll)
    @GetMapping("/farmer/{farmerId}/stored-predictions")
    fun farmerPredictions(@PathVariable farmerId: String): Result<List<HarvestPredictionDto2>> {
        return try {
            val list = farmerProduceRepository.findByFarmerIdSimple(farmerId).map { fp ->
                HarvestPredictionDto2(
                    farmerProduceId = fp.id,
                    farmerId = fp.farmer.id ?: "",
                    farmerName = fp.farmer.userProfile.fullName,
                    produceName = fp.farmProduce.name,
                    plantingDate = fp.plantingDate,
                    predictedHarvestDate = fp.predictedHarvestDate,
                    predictedSpecies = fp.predictedSpecies,
                    confidence = fp.predictionConfidence,
                    status = fp.status.name,
                    actualHarvestDate = fp.actualHarvestDate,
                    id = ""
                )
            }
            ResultFactory.getSuccessResult(list)
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to load farmer predictions: ${e.message}")
        }
    }
}
