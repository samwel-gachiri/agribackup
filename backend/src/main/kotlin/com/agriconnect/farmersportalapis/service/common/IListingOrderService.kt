package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.ListingOrderDto
import com.agriconnect.farmersportalapis.application.util.Result

interface IListingOrderService {
    fun findListingOrderByBuyerId(buyerId: String): Result<List<ListingOrderDto>>
}