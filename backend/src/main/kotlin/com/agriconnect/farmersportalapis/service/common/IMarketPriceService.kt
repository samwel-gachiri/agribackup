package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.MarketPrices
import com.agriconnect.farmersportalapis.application.util.Result

interface IMarketPriceService {
    fun getMarketPrices(): Result<List<MarketPrices>>
}