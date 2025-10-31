package com.agriconnect.farmersportalapis.buyers.application.services

import com.agriconnect.farmersportalapis.buyers.application.dtos.RequestOrderDto
import com.agriconnect.farmersportalapis.buyers.application.util.Result

interface IRequestOrderService {
    fun findRequestOrderByFarmerId(farmerId: String): Result<List<RequestOrderDto>>
}