package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.ListingOrderDto
import com.agriconnect.farmersportalapis.service.common.IListingOrderService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.infrastructure.repositories.ListingOrderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ListingOrderService(@Autowired val listingOrderRepository: ListingOrderRepository): IListingOrderService {

    override fun findListingOrderByBuyerId(buyerId: String): Result<List<ListingOrderDto>> {
        return try {
            ResultFactory.getSuccessResult(
                data = listingOrderRepository.findListingOrderByBuyerId(buyerId),
            )
        }catch (e: Exception){
            ResultFactory.getFailResult(msg = e.message)
        }
    }
}