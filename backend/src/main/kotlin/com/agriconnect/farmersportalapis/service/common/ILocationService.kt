package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.UpdateLocationRequestDto
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.domain.common.model.Location
import com.agriconnect.farmersportalapis.domain.profile.Farmer

interface ILocationService {

    fun getFarmersNearYou(latitude: Double, longitude: Double, maxDistance: Double = 10.0): Result<List<Farmer?>>

    fun updateLocation(updateLocationRequestDto: UpdateLocationRequestDto): Result<Location>

    fun getFarmerLocation(farmerId: String): Result<Location?>
}