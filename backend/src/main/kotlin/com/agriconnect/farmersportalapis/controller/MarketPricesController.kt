package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.service.common.impl.CommodityPriceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for real-time commodity market prices
 * Uses Commodities-API for global agricultural commodity prices
 */
@RestController
@RequestMapping("/api/v1/market-prices")
@Tag(name = "Market Prices", description = "Real-time commodity prices for agricultural products")
class MarketPricesController(
    private val commodityPriceService: CommodityPriceService
) {

    /**
     * Get all latest commodity prices
     */
    @Operation(
        summary = "Get latest commodity prices",
        description = "Returns latest prices for agricultural commodities (coffee, tea, maize, etc.) in both USD and KES",
        responses = [io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping
    fun getAllPrices(): ResponseEntity<Any> {
        return try {
            val prices = commodityPriceService.getLatestPrices()
            ResponseEntity.ok(mapOf(
                "success" to true,
                "count" to prices.size,
                "data" to prices
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(mapOf(
                "success" to false,
                "message" to "Failed to fetch commodity prices: ${e.message}",
                "data" to emptyList<Any>()
            ))
        }
    }

    /**
     * Get latest prices with price change percentage - optimized for dashboard
     */
    @Operation(
        summary = "Get latest prices for farmer dashboard",
        description = "Returns commodity prices with 30-day price change percentage, optimized for dashboard display",
        responses = [io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/latest")
    fun getLatestPrices(
        @Parameter(description = "Filter by commodity symbol (e.g., COFFEE, TEA)")
        @RequestParam(required = false) symbol: String?
    ): ResponseEntity<Any> {
        return try {
            var prices = commodityPriceService.getPricesWithChange()
            
            // Filter by symbol if provided
            if (!symbol.isNullOrBlank()) {
                prices = prices.filter { 
                    it.symbol.equals(symbol, ignoreCase = true) ||
                    it.name.contains(symbol, ignoreCase = true)
                }
            }
            
            // Transform to frontend-expected format
            val dashboardPrices = prices.map { price ->
                mapOf(
                    "produce" to price.name,
                    "market" to "Global",
                    "pricePerKg" to price.priceKes,
                    "priceUsd" to price.priceUsd,
                    "unit" to price.unit,
                    "changePercent" to (price.changePercent ?: 0.0),
                    "symbol" to price.symbol,
                    "lastUpdated" to price.date,
                    "history" to commodityPriceService.getPriceHistory(price.symbol, 7).map {
                        mapOf("date" to it.date, "price" to it.priceKes)
                    }
                )
            }
            
            ResponseEntity.ok(mapOf(
                "success" to true,
                "count" to dashboardPrices.size,
                "prices" to dashboardPrices
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(mapOf(
                "success" to false,
                "message" to "Failed to fetch prices: ${e.message}",
                "prices" to emptyList<Any>()
            ))
        }
    }

    /**
     * Get price history for a specific commodity
     */
    @Operation(
        summary = "Get price history for a commodity",
        description = "Returns price history for the last N days for a specific commodity",
        responses = [io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/history/{symbol}")
    fun getPriceHistory(
        @Parameter(description = "Commodity symbol (e.g., COFFEE, TEA, CORN)")
        @PathVariable symbol: String,
        @Parameter(description = "Number of days of history (default: 7)")
        @RequestParam(defaultValue = "7") days: Int
    ): ResponseEntity<Any> {
        return try {
            val history = commodityPriceService.getPriceHistory(symbol.uppercase(), days)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "symbol" to symbol.uppercase(),
                "count" to history.size,
                "history" to history.map {
                    mapOf(
                        "date" to it.date,
                        "priceUsd" to it.priceUsd,
                        "priceKes" to it.priceKes
                    )
                }
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(mapOf(
                "success" to false,
                "message" to "Failed to fetch price history: ${e.message}",
                "history" to emptyList<Any>()
            ))
        }
    }

    /**
     * Get available commodity symbols
     */
    @Operation(
        summary = "Get available commodity symbols",
        description = "Returns list of available commodity symbols and their names",
        responses = [io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/symbols")
    fun getSymbols(): ResponseEntity<Any> {
        return try {
            val symbols = commodityPriceService.getAvailableSymbols()
            ResponseEntity.ok(mapOf(
                "success" to true,
                "count" to symbols.size,
                "symbols" to symbols.map { (symbol, name) ->
                    mapOf("symbol" to symbol, "name" to name)
                }
            ))
        } catch (e: Exception) {
            ResponseEntity.ok(mapOf(
                "success" to false,
                "message" to "Failed to fetch symbols: ${e.message}",
                "symbols" to emptyList<Any>()
            ))
        }
    }
}