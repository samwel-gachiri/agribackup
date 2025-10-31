package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.LocationDto
import com.agriconnect.farmersportalapis.application.dtos.UpdateFarmerDto
import com.agriconnect.farmersportalapis.application.dtos.UpdateLocationRequestDto
import com.agriconnect.farmersportalapis.service.common.ILocationService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.common.model.Location
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerRepository
import com.agriconnect.farmersportalapis.service.supplychain.FarmerService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class LocationService: ILocationService {
    @Autowired
    lateinit var farmerRepository: FarmerRepository
    
    @Autowired
    lateinit var farmerService: FarmerService

    private val logger = LoggerFactory.getLogger(LocationService::class.java)

    val farmerNotFound = "Farmer is not found"
    override fun getFarmersNearYou(
        latitude: Double,
        longitude: Double,
        maxDistance: Double
    ): Result<List<Farmer?>> {
        return try {
            val allFarmers = farmerRepository.findAll()
            val nearbyFarmers = allFarmers.filter { farmer ->
                farmer?.location?.let { location ->
                    // Calculate distance using Haversine formula
                    val R = 6371 // Earth's radius in kilometers
                    val lat1 = Math.toRadians(latitude)
                    val lat2 = Math.toRadians(location.latitude)
                    val dLat = Math.toRadians(location.latitude - latitude)
                    val dLon = Math.toRadians(location.longitude - longitude)

                    val a = sin(dLat / 2) * sin(dLat / 2) +
                            cos(lat1) * cos(lat2) *
                            sin(dLon / 2) * sin(dLon / 2)

                    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
                    val distance = R * c

                    distance <= maxDistance // Return farmers within 10km radius
                } ?: false
            }
            ResultFactory.getSuccessResult(nearbyFarmers)
        } catch (e: Exception) {
            logger.error("Error in getting near farmers: ${e.message}", e)
            ResultFactory.getFailResult(e.message)
        }
    }

    
    override fun updateLocation(updateLocationRequestDto: UpdateLocationRequestDto): Result<Location> {
        return try {
            if (updateLocationRequestDto.farmerId!!.isEmpty()) {
                return ResultFactory.getFailResult("Farmer ID cannot be null or empty")
            }

            val existingFarmer = farmerRepository.findByIdOrNull(updateLocationRequestDto.farmerId)
                ?: return ResultFactory.getFailResult("Farmer not found with ID: ${updateLocationRequestDto.farmerId}")

            if (!isValidCoordinates(
                    updateLocationRequestDto.locationDto.latitude ?: 0.0,
                    updateLocationRequestDto.locationDto.longitude ?: 0.0
                )
            ) {
                return ResultFactory.getFailResult("Invalid coordinates provided")
            }

            val location = farmerService.updateFarmer(UpdateFarmerDto(
                farmerId = updateLocationRequestDto.farmerId!!,
                phoneNumber = existingFarmer.userProfile?.phoneNumber!!,
                email = existingFarmer.userProfile?.email!!,
                name = existingFarmer.userProfile?.fullName!!,
                location = LocationDto(
                    latitude = updateLocationRequestDto.locationDto.latitude,
                    longitude = updateLocationRequestDto.locationDto.longitude,
                    customName = updateLocationRequestDto.locationDto.customName
                ),
            )).data?.location!!

            ResultFactory.getSuccessResult(location)
        } catch (e: Exception) {
            ResultFactory.getFailResult(e.message ?: "Unknown error occurred while updating location")
        }
    }

    override fun getFarmerLocation(farmerId: String): Result<Location?> {
        return try {
            val farmer = farmerRepository.findByIdOrNull(farmerId)
                ?: return ResultFactory.getFailResult(farmerNotFound)

            ResultFactory.getSuccessResult(farmer.location)
        } catch (e: Exception) {
            logger.error("Error in getFarmerLocation: ${e.message}", e)
            ResultFactory.getFailResult(e.message)
        }
    }
}

private fun isValidCoordinates(latitude: Double, longitude: Double): Boolean {
    return latitude in -90.0..90.0 && longitude in -180.0..180.0
}