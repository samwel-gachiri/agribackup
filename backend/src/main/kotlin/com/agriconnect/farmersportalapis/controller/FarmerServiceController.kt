package com.agriconnect.farmersportalapis.controller


import com.agriconnect.farmersportalapis.application.dtos.UpdateFarmerDto
import com.agriconnect.farmersportalapis.application.dtos.UpdateFarmerProduceDto
import com.agriconnect.farmersportalapis.service.common.S3Service
import com.agriconnect.farmersportalapis.service.supplychain.FarmerService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import com.agriconnect.farmersportalapis.domain.profile.FarmerProduce
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile


@RestController
@RequestMapping("/farmers-service/farmer")
@Tag(name = "Farmer Management", description = "APIs for managing farmers")
class FarmerServiceController(
    var farmerService: FarmerService,
    private val s3Service: S3Service  // Add S3Service dependency

) {
    private val logger = LoggerFactory.getLogger(FarmerServiceController::class.java)

    //    @Operation(
    //        summary = "Creates a new farmer profile",
    //        responses = [ApiResponse(responseCode = "200", description = "OK")]
    //    )
    //    @PostMapping
    //    fun saveProfile(
    //        @RequestBody farmer: Farmer
    //    ) = farmerService.createFarmer(farmer)

    @Operation(
        summary = "Updates a farmer profile",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @PutMapping
    fun saveProfile(
        @RequestBody updateFarmerDto: UpdateFarmerDto
    ) = farmerService.updateFarmer(updateFarmerDto)


    @Operation(
        summary = "Add produce to farmer with smart name matching",
        description = "Adds a produce to farmer with duplicate prevention and name similarity checking",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @PostMapping(value = ["/add-smart-produce"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addSmartFarmerProduce(
        @RequestParam("farmerId") farmerId: String,
        @RequestParam("produceName") produceName: String,
        @RequestParam("description", required = false) description: String?,
        @RequestParam("farmingType", required = false) farmingType: String?,
        @RequestParam("yieldAmount", required = false) yieldAmount: Double?,
        @RequestParam("yieldUnit", required = false) yieldUnit: String? = "KG",
        @RequestParam("seasonYear", required = false) seasonYear: Int?,
        @RequestParam("seasonName", required = false) seasonName: String?,
        @RequestParam("plantingDate", required = false) plantingDate: String?,
        @RequestParam("harvestDate", required = false) harvestDate: String?,
        @RequestParam(value = "images", required = false) images: List<MultipartFile>?
    ): Result<FarmerProduce> {

        return try {
            logger.info("Received request to add produce for farmer: $farmerId, produce: $produceName")

            val farmerProduce = farmerService.addProduceToFarmer(
                farmerId = farmerId,
                produceName = produceName,
                description = description,
                farmingType = farmingType,
                yieldAmount = yieldAmount,
                yieldUnit = yieldUnit,
                seasonYear = seasonYear,
                seasonName = seasonName,
                plantingDate = plantingDate?.let { java.time.LocalDate.parse(it) },
                harvestDate = harvestDate?.let { java.time.LocalDate.parse(it) },
                images = images
            )

            ResultFactory.getSuccessResult(farmerProduce)
        } catch (e: IllegalStateException) {
            ResultFactory.getFailResult(e.message ?: "Error adding produce")
        } catch (e: Exception) {
            ResultFactory.getFailResult("An unexpected error occurred")
        }
    }

    @Operation(
        summary = "Updates farmers' produce with new value",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @PutMapping("/update-farmer-produce/{produceId}")
    fun updateFarmerProduce(
        @PathVariable produceId: String,
        @ModelAttribute updateDto: UpdateFarmerProduceDto
    ): Result<FarmerProduce> {
        return farmerService.updateProduce(produceId, updateDto)
    }
    @Operation(
        summary = "Gives out a list of farmers",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/list")
    fun getFarmers(): Result<List<Farmer>> {
        return farmerService.getFarmers()
    }

    @Operation(
        summary = "Gets out a specific farmer",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping
    fun getFarmer(
        @Parameter(description = "Id of farmer")
        @RequestParam("farmerId", defaultValue = "") farmerId: String,
    ): Result<Farmer> {
        return farmerService.getFarmer(farmerId)
    }

    @Operation(
        summary = "Gets farmer's produces",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/{farmerId}/produces")
    fun getFarmerProduces(
        @Parameter(description = "Id of farmer")
        @PathVariable farmerId: String
    ): Result<List<FarmerProduce>> {
        return farmerService.getFarmerProduces(farmerId)
    }
}