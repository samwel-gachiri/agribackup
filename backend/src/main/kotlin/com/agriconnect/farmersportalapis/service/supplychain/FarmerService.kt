package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.application.dtos.UpdateFarmerDto
import com.agriconnect.farmersportalapis.application.dtos.UpdateFarmerProduceDto
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.common.enums.FarmProduceStatus
import com.agriconnect.farmersportalapis.domain.common.enums.FarmerProduceStatus
import com.agriconnect.farmersportalapis.domain.common.model.FarmProduce
import com.agriconnect.farmersportalapis.domain.common.model.Location
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import com.agriconnect.farmersportalapis.domain.profile.FarmerProduce
import com.agriconnect.farmersportalapis.domain.profile.ProduceYield
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmProduceRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerProduceRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerRepository
import com.agriconnect.farmersportalapis.service.common.HarvestPredictionService
import com.agriconnect.farmersportalapis.service.common.IFarmerService
import com.agriconnect.farmersportalapis.service.common.S3Service
import jakarta.transaction.Transactional
import org.apache.commons.text.similarity.LevenshteinDistance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class FarmerService(
    private val s3Service: S3Service,
    private val harvestPredictionService: HarvestPredictionService
): IFarmerService {

    @Autowired
    lateinit var farmerRepository: FarmerRepository

    @Autowired
    lateinit var farmProduceRepository: FarmProduceRepository

    @Autowired
    lateinit var farmerProduceRepository: FarmerProduceRepository

//    @Autowired
//    lateinit var locationService: LocationService

    val farmerNotFound = "Farmer is not found"

    fun createFarmer(farmer: Farmer): Result<Farmer> {
        farmer.userProfile.createdAt = LocalDateTime.now()
        // If uid is provided but id is not, use uid as id

        return try {
            farmer.location?.farmer = farmer
            val savedFarmer = farmerRepository.saveAndFlush(farmer)
            ResultFactory.getSuccessResult(
                data = savedFarmer,
                msg = "${savedFarmer.userProfile.fullName} saved"
            )
        }catch (e: Exception){
            ResultFactory.getFailResult(e.message)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FarmerService::class.java)
        const val UPDATE_ERROR_MSG = "Failed to update farmer: %s"
    }

    data class HarvestPredictionRequest(
        val farmerProduceId: String,
        val plantingDate: LocalDate,
    val imageUrls: List<String>? = null,
    val description: String? = null,
    val imageBytes: ByteArray? = null
    )

    data class HarvestPredictionResponse(
        val farmerProduceId: String,
        val predictedSpecies: String?,
        val predictionConfidence: Double?,
        val predictedHarvestDate: LocalDate?,
        val modelVersion: String?,
        val status: FarmerProduceStatus
    )

    override fun updateFarmer(updateFarmerDto: UpdateFarmerDto): Result<Farmer> {
        return try {
            val farmer = farmerRepository.findByIdOrNull(updateFarmerDto.farmerId)
                ?: return ResultFactory.getFailResult(msg = farmerNotFound)
            farmer.applyUpdates(updateFarmerDto)
            val updatedFarmer = farmerRepository.saveAndFlush(farmer)
            ResultFactory.getSuccessResult(
                data = updatedFarmer,
                msg = "Successfully updated farmer details"
            )
        } catch (e: Exception) {
            logger.error("Error updating farmer", e)
            ResultFactory.getFailResult(msg = UPDATE_ERROR_MSG.format(e.message))
        }
    }

    /**
     * Triggers (or retriggers) harvest prediction for a farmer produce.
     * Uses AI-powered prediction service with fallback to heuristics.
     */
    @Transactional
    fun predictHarvest(request: HarvestPredictionRequest, imageBytes: ByteArray? = null, description: String? = null): Result<HarvestPredictionResponse> {
        return try {
            val produce = farmerProduceRepository.findByIdOrNull(request.farmerProduceId)
                ?: return ResultFactory.getFailResult("FarmerProduce not found")

            // Update planting date if newly provided
            if (produce.plantingDate == null || produce.plantingDate != request.plantingDate) {
                produce.plantingDate = request.plantingDate
            }

            // Use AI-powered harvest prediction service
            val predictionDto = harvestPredictionService.predictHarvest(request.farmerProduceId)

            // Update produce with AI prediction results
            produce.predictedSpecies = predictionDto.predictedSpecies ?: produce.farmProduce.name
            produce.predictedHarvestDate = predictionDto.predictedHarvestDate
            produce.predictionConfidence = predictionDto.confidence ?: 0.75
            produce.aiModelVersion = "anthropic-claude-v1" // Updated to reflect AI usage
            if (produce.status == FarmerProduceStatus.ON_FARM) {
                produce.status = FarmerProduceStatus.HARVEST_PLANNED
            }
            farmerProduceRepository.saveAndFlush(produce)

            ResultFactory.getSuccessResult(
                HarvestPredictionResponse(
                    farmerProduceId = produce.id,
                    predictedSpecies = produce.predictedSpecies,
                    predictionConfidence = produce.predictionConfidence,
                    predictedHarvestDate = produce.predictedHarvestDate,
                    modelVersion = produce.aiModelVersion,
                    status = produce.status
                ),
                "AI-powered harvest prediction updated"
            )
        } catch (e: Exception) {
            logger.error("Error predicting harvest", e)
            ResultFactory.getFailResult("Failed to predict harvest: ${e.message}")
        }
    }

    @Transactional
    fun markHarvested(farmerProduceId: String, actualHarvestDate: LocalDate): Result<HarvestPredictionResponse> {
        return try {
            val produce = farmerProduceRepository.findByIdOrNull(farmerProduceId)
                ?: return ResultFactory.getFailResult("FarmerProduce not found")
            produce.actualHarvestDate = actualHarvestDate
            produce.status = FarmerProduceStatus.HARVESTED
            farmerProduceRepository.saveAndFlush(produce)
            ResultFactory.getSuccessResult(
                HarvestPredictionResponse(
                    farmerProduceId = produce.id,
                    predictedSpecies = produce.predictedSpecies,
                    predictionConfidence = produce.predictionConfidence,
                    predictedHarvestDate = produce.predictedHarvestDate,
                    modelVersion = produce.aiModelVersion,
                    status = produce.status
                ),
                "Harvest marked as completed"
            )
        } catch (e: Exception) {
            logger.error("Error marking harvest", e)
            ResultFactory.getFailResult("Failed to mark harvest: ${e.message}")
        }
    }

    private fun Farmer.applyUpdates(dto: UpdateFarmerDto) {
//        phoneNumber = dto.phoneNumber
//        email = dto.email
//        name = dto.name
        location = dto.location.let {
            Location(
                latitude = it.latitude!!,
                longitude = it.longitude!!,
                customName = it.customName!!,
            )
        }
    }

    override fun getFarmers(): Result<List<Farmer>> {
        return try {
            val result = farmerRepository.findAll()
            ResultFactory.getSuccessResult(data = result)
        } catch (e: Exception) {
            logger.error("Error fetching farmers", e)
            ResultFactory.getFailResult(msg = "Could not fetch users. Invalid user pool or user group")
        }
    }

    override fun getFarmer(farmerId: String): Result<Farmer> {
        return try {
            val result = farmerRepository.findByIdOrNull(farmerId)
                ?: return ResultFactory.getFailResult(msg = farmerNotFound);

            ResultFactory.getSuccessResult(data = result)
        } catch (e: Exception) {
            logger.error("Error fetching farmer with ID: $farmerId", e)
            ResultFactory.getFailResult(msg = "Could not fetch users. Invalid user pool or user group")
        }
    }

    fun getFarmerProduces(farmerId: String): Result<List<FarmerProduce>> {
        return try {
            val farmer = farmerRepository.findByIdOrNull(farmerId)
                ?: return ResultFactory.getFailResult(msg = farmerNotFound)

            val produces = farmer.farmerProduces.filter { it.status != FarmerProduceStatus.INACTIVE }
            ResultFactory.getSuccessResult(data = produces)
        } catch (e: Exception) {
            logger.error("Error fetching farmer produces", e)
            ResultFactory.getFailResult(msg = "Could not fetch farmer produces: ${e.message}")
        }
    }

    @Transactional
    fun addProduceToFarmer(
        farmerId: String,
        produceName: String,
        description: String?,
        farmingType: String?,
        yieldAmount: Double?,
        yieldUnit: String? = "KG",
        seasonYear: Int? = null,
        seasonName: String? = null,
        plantingDate: LocalDate? = null,
        harvestDate: LocalDate? = null,
        images: List<MultipartFile>?
    ): FarmerProduce {
        val farmer = farmerRepository.findById(farmerId)
            .orElseThrow { NoSuchElementException("Farmer not found with ID: $farmerId") }

        // Check if farmer already has this produce
        val existingProduce = farmer.farmerProduces.find {
            it.farmProduce.name.equals(produceName, ignoreCase = true)
        }
        if (existingProduce != null) {
            throw IllegalStateException("Farmer already has produce: $produceName")
        }

        // Find similar produces using Levenshtein distance
        val similarProduces = farmProduceRepository.findSimilarByName(produceName)
        val levenshtein = LevenshteinDistance()

        val matchingProduce = similarProduces.minByOrNull { produce ->
            levenshtein.apply(produce.name.lowercase(), produceName.lowercase())
        }?.takeIf { produce ->
            // Consider names similar if Levenshtein distance is less than 3
            levenshtein.apply(produce.name.lowercase(), produceName.lowercase()) < 3
        } ?: createNewFarmProduce(produceName, description, farmingType)
        // Handle image upload first
        val imageUrls = if (!images.isNullOrEmpty()) {
            logger.info("Processing ${images.size} images")
            try {
                images.mapNotNull { file ->
                    try {
                        logger.info("Uploading image: ${file.originalFilename}")
                        s3Service.uploadFile(file)
                    } catch (e: Exception) {
                        logger.error("Failed to upload image ${file.originalFilename}: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing images: ${e.message}")
                null
            }
        } else {
            logger.info("No images provided")
            null
        }
        // Create new FarmerProduce
        val farmerProduce = FarmerProduce(
            description = description,
            farmingType = farmingType,
            farmer = farmer,
            farmProduce = matchingProduce,
            status = FarmerProduceStatus.ON_FARM,
            imageUrls = imageUrls,
            plantingDate = plantingDate,
            actualHarvestDate = harvestDate
        )

        // Add yield information if provided
        if (yieldAmount != 0.0 && yieldAmount != null && yieldUnit != null) {
            val yield = ProduceYield(
                farmerProduce = farmerProduce,
                seasonYear = seasonYear,
                seasonName = seasonName,
                yieldAmount = yieldAmount,
                yieldUnit = yieldUnit
            )
            farmerProduce.yields.add(yield)
        }

        farmer.farmerProduces.add(farmerProduce)
        farmerRepository.save(farmer)

        // Trigger harvest prediction if planting date is provided but harvest date is not
        if (plantingDate != null && harvestDate == null) {
            logger.info("Triggering harvest prediction for produce ${farmerProduce.id} with planting date $plantingDate")
            try {
                val predictionRequest = HarvestPredictionRequest(
                    farmerProduceId = farmerProduce.id,
                    plantingDate = plantingDate
                )
                val predictionResult = predictHarvest(predictionRequest)
                if (predictionResult.success) {
                    logger.info("Harvest prediction successful for produce ${farmerProduce.id}")
                } else {
                    logger.warn("Harvest prediction failed for produce ${farmerProduce.id}: ${predictionResult.msg}")
                }
            } catch (e: Exception) {
                logger.error("Error during harvest prediction for produce ${farmerProduce.id}", e)
            }
        }

        return farmerProduce
    }

    private fun createNewFarmProduce(name: String, description: String?, farmingType: String?): FarmProduce {
        return farmProduceRepository.save(
            FarmProduce(
                name = name,
                description = description,
                farmingType = farmingType,
                status = FarmProduceStatus.ACTIVE,
            )
        )
    }


    @Transactional
    fun updateProduce(produceId: String, updateDto: UpdateFarmerProduceDto): Result<FarmerProduce> {
        val produce = farmerProduceRepository.findByIdOrNull(produceId)
            ?: return ResultFactory.getFailResult(msg = "Produce not found.")
        var removedImageUrls = listOf<String>()
        // Delete removed images from S3
        if (updateDto.removeImageUrls != null) {
            updateDto.removeImageUrls.forEach { s3Service.deleteFile(it) }
            removedImageUrls = updateDto.removeImageUrls
        }
        // Upload new images
        var newImageUrls = listOf<String>()
        if (updateDto.newImages != null)
            newImageUrls = updateDto.newImages.map { s3Service.uploadFile(it) }

        // Update entity
        produce.description = updateDto.description
        produce.farmingType = updateDto.farmingType
        produce.imageUrls = (produce.imageUrls?.minus(removedImageUrls.toSet()))?.plus(newImageUrls)

        return ResultFactory.getSuccessResult(farmerProduceRepository.save(produce))
    }

//    override fun updateFarmer(farmer: Farmer): Result<String> {
////        farmerRepository.
//    }

}