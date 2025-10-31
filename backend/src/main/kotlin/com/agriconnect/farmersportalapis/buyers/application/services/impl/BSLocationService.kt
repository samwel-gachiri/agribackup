package com.agriconnect.farmersportalapis.buyers.application.services.impl

import com.agriconnect.farmersportalapis.buyers.application.dtos.LocationRequestDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.UpdateBuyerDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.UpdateLocationRequestDto
import com.agriconnect.farmersportalapis.buyers.application.services.IBSLocationService
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.application.util.ResultFactory
import com.agriconnect.farmersportalapis.buyers.domain.common.model.BuyerLocation
import com.agriconnect.farmersportalapis.buyers.domain.profile.Buyer
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.BuyerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Service
class BSLocationService: IBSLocationService {
    @Autowired
    lateinit var buyerRepository: BuyerRepository

    @Autowired
    lateinit var buyerService: BuyerService

    val buyerNotFound = "Buyer is not found"
    override fun getBuyersNearYou(latitude: Double, longitude: Double, maxDistance: Double?): Result<List<Buyer?>>{
        val buyers = buyerRepository.findAll()

        val nearbyBuyers = buyers.filter { buyer ->
            buyer.location?.let { location ->
                val distance = calculateDistance(
                    latitude1 = latitude,
                    longitude1 = longitude,
                    latitude2 = location.latitude,
                    longitude2 = location.longitude
                )
                distance <= (maxDistance ?: 10.0)
            } ?: false
        }

        return ResultFactory.getSuccessResult(nearbyBuyers)
    }

    private fun calculateDistance(
        latitude1: Double,
        longitude1: Double,
        latitude2: Double,
        longitude2: Double
    ): Double {
        val earthRadius = 6371.0 // kilometers

        val dLat = Math.toRadians(latitude2 - latitude1)
        val dLon = Math.toRadians(longitude2 - longitude1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(latitude1)) * cos(Math.toRadians(latitude2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(Math.sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    override fun updateLocation(updateLocationRequestDto: UpdateLocationRequestDto): Result<BuyerLocation> {
        return try {
            if (updateLocationRequestDto.buyerId!!.isEmpty()) {
                return ResultFactory.getFailResult("Buyer ID cannot be null or empty")
            }

            val existingFarmer = buyerRepository.findByIdOrNull(updateLocationRequestDto.buyerId)
                ?: return ResultFactory.getFailResult("Farmer not found with ID: ${updateLocationRequestDto.buyerId}")

//            if (!isValidCoordinates(
//                    updateLocationRequestDto.locationDto.latitude,
//                    updateLocationRequestDto.locationDto.longitude
//                )
//            ) {
//                return ResultFactory.getFailResult("Invalid coordinates provided")
//            }

            val location = buyerService.updateBuyer(
                UpdateBuyerDto(
                    buyerId = updateLocationRequestDto.buyerId!!,
                    phoneNumber = existingFarmer.userProfile?.phoneNumber!!,
                    email = existingFarmer.userProfile?.email!!,
                    name = existingFarmer.userProfile?.fullName!!,
                    location = LocationRequestDto(
                        latitude = updateLocationRequestDto.locationDto.latitude,
                        longitude = updateLocationRequestDto.locationDto.longitude,
                        customName = updateLocationRequestDto.locationDto.customName
                    ),
                )
            ).data?.location!!

            ResultFactory.getSuccessResult(location)
        } catch (e: Exception) {
            ResultFactory.getFailResult(e.message ?: "Unknown error occurred while updating location")
        }
    }

    override fun getBuyerLocation(buyerId: String): Result<BuyerLocation?> {
        val buyer = buyerRepository.findByIdOrNull(buyerId)
            ?: return ResultFactory.getFailResult(msg = buyerNotFound)

        return ResultFactory.getSuccessResult(buyer.location)
    }
}