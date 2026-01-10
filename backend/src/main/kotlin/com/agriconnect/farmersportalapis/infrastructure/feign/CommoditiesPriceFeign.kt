package com.agriconnect.farmersportalapis.infrastructure.feign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Feign client for Twelve Data API (twelvedata.com)
 * Provides real-time commodity prices including agricultural products
 * 
 * Free tier: 800 API calls/day, 8 calls/minute
 * Docs: https://twelvedata.com/docs
 */
@FeignClient(
    name = "twelveDataClient",
    url = "\${twelvedata.api.url:https://api.twelvedata.com}"
)
interface TwelveDataFeign {
    
    /**
     * Get list of available commodities
     */
    @GetMapping("/commodities")
    fun getCommodities(
        @RequestParam("apikey") apiKey: String,
        @RequestParam("category", required = false) category: String?
    ): TwelveDataCommoditiesResponse
    
    /**
     * Get latest price for a symbol
     */
    @GetMapping("/price")
    fun getPrice(
        @RequestParam("apikey") apiKey: String,
        @RequestParam("symbol") symbol: String
    ): TwelveDataPriceResponse
    
    /**
     * Get quote (more detailed price info)
     */
    @GetMapping("/quote")
    fun getQuote(
        @RequestParam("apikey") apiKey: String,
        @RequestParam("symbol") symbol: String
    ): TwelveDataQuoteResponse
    
    /**
     * Get time series data
     */
    @GetMapping("/time_series")
    fun getTimeSeries(
        @RequestParam("apikey") apiKey: String,
        @RequestParam("symbol") symbol: String,
        @RequestParam("interval") interval: String,
        @RequestParam("outputsize", required = false) outputSize: Int?
    ): TwelveDataTimeSeriesResponse
    
    /**
     * Get exchange rate (for currency conversion)
     */
    @GetMapping("/exchange_rate")
    fun getExchangeRate(
        @RequestParam("apikey") apiKey: String,
        @RequestParam("symbol") symbol: String
    ): TwelveDataExchangeRateResponse
}

// Response DTOs

data class TwelveDataCommoditiesResponse(
    val data: List<TwelveDataCommodity>?,
    val status: String?,
    val code: Int?,
    val message: String?
)

data class TwelveDataCommodity(
    val symbol: String,
    val name: String?,
    val category: String?,
    val description: String?
)

data class TwelveDataPriceResponse(
    val price: String?,
    val status: String?,
    val code: Int?,
    val message: String?
)

data class TwelveDataQuoteResponse(
    val symbol: String?,
    val name: String?,
    val exchange: String?,
    val datetime: String?,
    val open: String?,
    val high: String?,
    val low: String?,
    val close: String?,
    val previous_close: String?,
    val change: String?,
    val percent_change: String?,
    val status: String?,
    val code: Int?,
    val message: String?
)

data class TwelveDataTimeSeriesResponse(
    val meta: TwelveDataMeta?,
    val values: List<TwelveDataValue>?,
    val status: String?,
    val code: Int?,
    val message: String?
)

data class TwelveDataMeta(
    val symbol: String?,
    val interval: String?,
    val currency: String?,
    val type: String?
)

data class TwelveDataValue(
    val datetime: String?,
    val open: String?,
    val high: String?,
    val low: String?,
    val close: String?
)

data class TwelveDataExchangeRateResponse(
    val symbol: String?,
    val rate: Double?,
    val timestamp: Long?,
    val status: String?,
    val code: Int?,
    val message: String?
)
