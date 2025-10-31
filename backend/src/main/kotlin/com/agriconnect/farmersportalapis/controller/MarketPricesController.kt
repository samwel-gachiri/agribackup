package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.service.common.impl.MarketPriceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/farmers-service/market-prices")
@Tag(name = "Market prices")
class MarketPricesController(
    private val marketPriceService: MarketPriceService
) {
    @Operation(
        summary = "Gets the market price of a certain place",
        responses = [io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping
    fun getMarketPrices() = marketPriceService.getMarketPrices()
}