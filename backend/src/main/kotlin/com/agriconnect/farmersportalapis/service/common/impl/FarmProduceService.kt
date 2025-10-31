package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.IFarmProduceService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.BSProduceRepository
import com.agriconnect.farmersportalapis.domain.common.enums.FarmProduceStatus
import com.agriconnect.farmersportalapis.domain.common.enums.FarmerProduceStatus
import com.agriconnect.farmersportalapis.domain.common.model.FarmProduce
import com.agriconnect.farmersportalapis.domain.profile.FarmerProduce
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmProduceRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerProduceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class FarmProduceService: IFarmProduceService {
    @Autowired
    lateinit var farmProduceRepository: FarmProduceRepository

    @Autowired
    lateinit var farmerProduceRepository: FarmerProduceRepository

    @Autowired
    lateinit var buyersProduceRepository: BSProduceRepository

    val produceNotFoundTxt = "Produce not found."

    override fun createFarmProduce(createFarmProduceDto: createFarmProduceDto): Result<FarmProduce> {
        return try {
            val savedFarmProduce = farmProduceRepository.saveAndFlush(FarmProduce(
                name = createFarmProduceDto.name,
                description = createFarmProduceDto.description,
                farmingType = createFarmProduceDto.farmingType,
                status = FarmProduceStatus.ACTIVE,
            ))
            ResultFactory.getSuccessResult(savedFarmProduce)
        }catch (e: Exception) {
                ResultFactory.getFailResult(msg=e.message)
        }
    }

    override fun deleteFarmProduce(farmProduceId: String): Result<String> {
        val produceExists = farmProduceRepository.findByIdOrNull(farmProduceId)
            ?: return ResultFactory.getFailResult(msg = produceNotFoundTxt)
        farmProduceRepository.delete(produceExists)

        return ResultFactory.getSuccessResult<String>(msg = "Farm produce deleted successfully")
    }

    override fun getFarmProduces(): Result<List<FarmProduceDto>> {
        return try {
            val farmerProduces = farmProduceRepository.findAll().map {
                FarmProduceDto(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    farmingType = it.farmingType,
                    source = "FARMER"
                )
            }

            val buyerProduces = buyersProduceRepository.findAll().map {
                FarmProduceDto(
                    id = it.id,
                    name = it.name,
                    description = it.description,
                    farmingType = it.farmingType,
                    source = "BUYER"
                )
            }

            // Merge lists, ensuring unique names by using a map
            val combinedProduces = (farmerProduces + buyerProduces)
                .associateBy { it.name }  // Ensures uniqueness based on name
                .values.toList()

            ResultFactory.getSuccessResult(combinedProduces)
        } catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }
    // search for produce
    fun searchProduce(request: ProduceSearchRequest): List<ProduceSearchResponse> {
        return when (request.searchType ?: SearchType.ALL) {
            SearchType.LOCATION -> searchByLocation(request)
            SearchType.NAME, SearchType.DESCRIPTION, SearchType.ALL -> searchByNameOrDescription(request)
        }
    }

    private fun searchByNameOrDescription(request: ProduceSearchRequest): List<ProduceSearchResponse> {
        val produces = farmerProduceRepository.searchByNameOrDescription(
            request.searchTerm,
            FarmerProduceStatus.INACTIVE
        )
        return produces.map { it.toSearchResponse() }
    }

    private fun searchByLocation(request: ProduceSearchRequest): List<ProduceSearchResponse> {
        require(request.latitude != null && request.longitude != null) {
            "Latitude and longitude are required for location search"
        }

        val maxDistance = request.maxDistance ?: 50.0 // Default 50km radius

        val results = farmerProduceRepository.searchByLocation(
            request.latitude,
            request.longitude,
            maxDistance
        )

        return results.map { result ->
            val produce = result[0] as FarmerProduce
            val distance = result[1] as Double
            produce.toSearchResponse(distance)
        }
    }

    private fun FarmerProduce.toSearchResponse(distance: Double? = null) = ProduceSearchResponse(
        id = id,
        name = farmProduce.name,
        description = description,
        farmingType = farmingType,
        status = farmProduce.status,
        farmer = FarmerInfo(
            id = farmer.id,
            name = farmer.userProfile?.fullName,
            contactInfo = farmer.userProfile?.phoneNumber
        ),
        location = LocationInfo(
            latitude = farmer.location?.latitude,
            longitude = farmer.location?.longitude,
            customName = farmer.location?.customName
        ),
        distance = distance
    )
}