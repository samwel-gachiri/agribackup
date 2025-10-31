package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.domain.common.model.FarmerBuyerConnection

interface IConnectionService {
    fun connectFarmerToBuyer(farmerId: String, buyerId: String): Result<FarmerBuyerConnection>
    fun getBuyersForFarmer(farmerId: String): Result<List<FarmerBuyerConnection>>
    fun getFarmersForBuyer(buyerId: String): Result<List<FarmerBuyerConnection>>
    fun disconnectFarmerAndBuyer(farmerId: String, buyerId: String): Result<String>
}