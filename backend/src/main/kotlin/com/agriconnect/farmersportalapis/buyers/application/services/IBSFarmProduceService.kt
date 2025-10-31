package com.agriconnect.farmersportalapis.buyers.application.services

import com.agriconnect.farmersportalapis.buyers.application.dtos.createFarmProduceDto
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.domain.common.model.BSFarmProduce

interface IBSFarmProduceService {
    fun createFarmProduce(createFarmProduceDto: createFarmProduceDto): Result<BSFarmProduce>
    fun deleteFarmProduce(farmProduceId: String): Result<String>
    fun getFarmProduces(): Result<List<BSFarmProduce>>
}