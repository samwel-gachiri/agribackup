package com.agriconnect.farmersportalapis.service.common

//import com.agriconnect.farmersportalapis.application.dtos.GetFarmersResponseDto
import com.agriconnect.farmersportalapis.application.dtos.UpdateFarmerDto
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.domain.profile.Farmer

interface IFarmerService {
//    fun createFarmer(farmer: Farmer): Result<Farmer>
    fun updateFarmer(updateFarmerDto: UpdateFarmerDto): Result<Farmer>

    fun getFarmers(): Result<List<Farmer>>

    fun getFarmer(farmerId: String): Result<Farmer>

//    fun updateFarmer(farmer: Farmer): Result<String>
}