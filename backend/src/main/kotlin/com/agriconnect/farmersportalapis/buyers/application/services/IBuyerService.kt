package com.agriconnect.farmersportalapis.buyers.application.services

//import com.agriconnect.farmersportalapis.buyers.application.dtos.GetBuyersResponseDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.AddProducesToBuyerDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.UpdateBuyerDto
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.domain.profile.Buyer
import com.agriconnect.farmersportalapis.buyers.domain.profile.PreferredProduce

interface IBuyerService {
//    fun createBuyer(buyer: Buyer): Result<Buyer>

    fun updateBuyer(updateBuyerDto: UpdateBuyerDto): Result<Buyer>

    fun getBuyers(): Result<List<Buyer>>

    fun getBuyer(buyerId: String): Result<Buyer>

    fun addProducesToBuyer(addProducesToBuyerDto: AddProducesToBuyerDto): Result<List<PreferredProduce>>

//    fun updateBuyer(buyer: Buyer): Result<String>
}