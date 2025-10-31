package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.MarketPrices
import com.agriconnect.farmersportalapis.service.common.IMarketPriceService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.infrastructure.feign.MarketPricesFeign
import org.springframework.stereotype.Service

@Service
class MarketPriceService(
    private val marketPricesFeign: MarketPricesFeign
): IMarketPriceService {
    override fun getMarketPrices(): Result<List<MarketPrices>> {
        return ResultFactory.getSuccessResult(marketPricesFeign.getMarketPrices())
    }
}