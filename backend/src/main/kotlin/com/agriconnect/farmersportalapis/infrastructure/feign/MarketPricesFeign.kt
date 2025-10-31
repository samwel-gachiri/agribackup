package com.agriconnect.farmersportalapis.infrastructure.feign

import com.agriconnect.farmersportalapis.application.dtos.MarketPrices
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(
    name = "marketPriceClient",
    url = "https://kamis.kaopdata.co.ke/imwa-market-prices-api.php?mid=1"
)
interface MarketPricesFeign {
    @GetMapping
    fun getMarketPrices(): List<MarketPrices>
}