package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.FarmProduceDto
import com.agriconnect.farmersportalapis.application.dtos.createFarmProduceDto
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.domain.common.model.FarmProduce

interface IFarmProduceService {
    fun createFarmProduce(createFarmProduceDto: createFarmProduceDto): Result<FarmProduce>
    fun deleteFarmProduce(farmProduceId: String): Result<String>
    fun getFarmProduces(): Result<List<FarmProduceDto>>
}