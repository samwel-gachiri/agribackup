package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.infrastructure.feign.TwelveDataFeign
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Service for fetching real commodity prices from Twelve Data API
 * Provides agricultural commodity prices for coffee, corn, wheat, and other products
 * 
 * Free tier: 800 API calls/day
 */
@Service
class CommodityPriceService(
    private val twelveDataFeign: TwelveDataFeign
) {
    
    private val logger = LoggerFactory.getLogger(CommodityPriceService::class.java)
    
    @Value("\${twelvedata.api.key:}")
    private lateinit var apiKey: String
    
    // Agricultural commodity symbols from Twelve Data
    companion object {
        val AGRICULTURAL_COMMODITIES = mapOf(
            "KC1" to "Coffee Arabica",        // Coffee futures
            "CC1" to "Cocoa",                  // Cocoa futures
            "SB1" to "Sugar",                  // Sugar futures
            "C_1" to "Maize (Corn)",           // Corn futures
            "W_1" to "Wheat",                  // Wheat futures
            "S_1" to "Soybeans",               // Soybean futures
            "RR1" to "Rice",                   // Rice futures
            "CT1" to "Cotton"                  // Cotton futures
        )
        
        // Default USD to KES exchange rate (updated when API is available)
        const val DEFAULT_KES_RATE = 155.0
    }

    /**
     * Get latest commodity prices for dashboard display
     */
    @PostConstruct
    fun init() {
        if (apiKey.isBlank()) {
            logger.warn("Twelve Data API key not configured. Market prices will be unavailable.")
        } else {
            logger.info("Twelve Data API key configured successfully.")
        }
    }

    @Cacheable(value = ["commodityPrices"], key = "'latest'", unless = "#result == null || #result.isEmpty()")
    fun getLatestPrices(): List<CommodityPriceDto> {
        if (apiKey.isBlank()) {
            logger.warn("Twelve Data API key not configured")
            return emptyList()
        }
        
        val kesRate = getKesExchangeRate()
        
        return AGRICULTURAL_COMMODITIES.mapNotNull { (symbol, name) ->
            try {
                val quoteResponse = twelveDataFeign.getQuote(apiKey, symbol)
                
                if (quoteResponse.status == "error" || quoteResponse.close == null) {
                    logger.debug("No data for $symbol: ${quoteResponse.message}")
                    return@mapNotNull null
                }
                
                val closePrice = quoteResponse.close.toDoubleOrNull() ?: return@mapNotNull null
                val changePercent = quoteResponse.percent_change?.toDoubleOrNull() ?: 0.0
                
                CommodityPriceDto(
                    symbol = symbol,
                    name = name,
                    priceUsd = BigDecimal.valueOf(closePrice).setScale(2, RoundingMode.HALF_UP),
                    priceKes = BigDecimal.valueOf(closePrice * kesRate).setScale(2, RoundingMode.HALF_UP),
                    unit = getUnitForCommodity(symbol),
                    date = quoteResponse.datetime ?: "",
                    changePercent = BigDecimal.valueOf(changePercent).setScale(2, RoundingMode.HALF_UP).toDouble()
                )
            } catch (e: Exception) {
                logger.warn("Failed to get price for $symbol: ${e.message}")
                null
            }
        }.sortedByDescending { it.priceKes }
    }
    
    /**
     * Get prices with price change percentage
     */
    @Cacheable(value = ["commodityPricesWithChange"], key = "'dashboard'", unless = "#result == null || #result.isEmpty()")
    fun getPricesWithChange(): List<CommodityPriceDto> {
        return getLatestPrices()
    }
    
    /**
     * Get price history for a specific commodity
     */
    fun getPriceHistory(symbol: String, days: Int = 7): List<PriceHistoryPoint> {
        if (apiKey.isBlank()) {
            return emptyList()
        }
        
        val kesRate = getKesExchangeRate()
        
        return try {
            val response = twelveDataFeign.getTimeSeries(
                apiKey = apiKey,
                symbol = symbol,
                interval = "1day",
                outputSize = days
            )
            
            if (response.status == "error" || response.values == null) {
                return emptyList()
            }
            
            response.values.mapNotNull { value ->
                val closePrice = value.close?.toDoubleOrNull() ?: return@mapNotNull null
                PriceHistoryPoint(
                    date = value.datetime ?: "",
                    priceUsd = BigDecimal.valueOf(closePrice).setScale(2, RoundingMode.HALF_UP),
                    priceKes = BigDecimal.valueOf(closePrice * kesRate).setScale(2, RoundingMode.HALF_UP)
                )
            }.reversed() // Oldest first
        } catch (e: Exception) {
            logger.error("Failed to fetch price history for $symbol: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Get available commodity symbols
     */
    fun getAvailableSymbols(): Map<String, String> {
        return AGRICULTURAL_COMMODITIES
    }
    
    /**
     * Get current USD to KES exchange rate
     */
    private fun getKesExchangeRate(): Double {
        return try {
            val response = twelveDataFeign.getExchangeRate(apiKey, "USD/KES")
            response.rate ?: DEFAULT_KES_RATE
        } catch (e: Exception) {
            logger.debug("Using default KES rate: ${e.message}")
            DEFAULT_KES_RATE
        }
    }
    
    private fun getUnitForCommodity(symbol: String): String {
        return when (symbol) {
            "KC1" -> "per lb"       // Coffee in cents/lb
            "CC1" -> "per ton"      // Cocoa in USD/ton
            "SB1" -> "per lb"       // Sugar in cents/lb
            "C_1" -> "per bushel"   // Corn in cents/bushel
            "W_1" -> "per bushel"   // Wheat in cents/bushel
            "S_1" -> "per bushel"   // Soybeans in cents/bushel
            "RR1" -> "per cwt"      // Rice in USD/cwt
            "CT1" -> "per lb"       // Cotton in cents/lb
            else -> "per unit"
        }
    }
}

/**
 * DTO for commodity price
 */
data class CommodityPriceDto(
    val symbol: String,
    val name: String,
    val priceUsd: BigDecimal,
    val priceKes: BigDecimal,
    val unit: String,
    val date: String,
    val changePercent: Double?
)

/**
 * Price history point
 */
data class PriceHistoryPoint(
    val date: String,
    val priceUsd: BigDecimal,
    val priceKes: BigDecimal
)
