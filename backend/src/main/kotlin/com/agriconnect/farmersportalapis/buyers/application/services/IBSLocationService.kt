package com.agriconnect.farmersportalapis.buyers.application.services

import com.agriconnect.farmersportalapis.buyers.application.dtos.UpdateLocationRequestDto
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.domain.common.model.BuyerLocation
import com.agriconnect.farmersportalapis.buyers.domain.profile.Buyer

interface IBSLocationService {

    fun getBuyersNearYou(latitude: Double, longitude: Double, maxDistance: Double? = 10.0): Result<List<Buyer?>>

    fun updateLocation(updateLocationRequestDto: UpdateLocationRequestDto): Result<BuyerLocation>

    fun getBuyerLocation(buyerId: String): Result<BuyerLocation?>
}