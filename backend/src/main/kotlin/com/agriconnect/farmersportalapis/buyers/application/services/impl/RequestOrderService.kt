package com.agriconnect.farmersportalapis.buyers.application.services.impl

import com.agriconnect.farmersportalapis.buyers.application.dtos.RequestOrderDto
import com.agriconnect.farmersportalapis.buyers.application.services.IRequestOrderService
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.application.util.ResultFactory
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.RequestOrderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RequestOrderService(@Autowired val requestOrderRepository: RequestOrderRepository): IRequestOrderService {

    override fun findRequestOrderByFarmerId(farmerId: String): Result<List<RequestOrderDto>> {
        return try {
            ResultFactory.getSuccessResult(
                data = requestOrderRepository.findRequestOrderByFarmerId(farmerId),
            )
        }catch (e: Exception){
            ResultFactory.getFailResult(msg = e.message)
        }
    }
}