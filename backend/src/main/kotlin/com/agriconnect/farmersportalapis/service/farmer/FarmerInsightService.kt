package com.agriconnect.farmersportalapis.service.farmer

import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerProduceRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProductionUnitRepository
import com.agriconnect.farmersportalapis.service.common.impl.CommodityPriceService
import com.agriconnect.farmersportalapis.service.common.impl.WeatherService
import org.springframework.stereotype.Service

@Service
class FarmerInsightService(
    private val productionUnitRepository: ProductionUnitRepository,
    private val farmerProduceRepository: FarmerProduceRepository,
    private val weatherService: WeatherService,
    private val commodityPriceService: CommodityPriceService
) {

    fun getInsight(farmerId: String): String {
        // 1. Identify Farmer's Crops
        val crops = mutableSetOf<String>()
        
        // From production units
        try {
            productionUnitRepository.findByFarmerId(farmerId).forEach { 
                crops.add(it.unitName) // Assuming unitName often contains crop name like "Coffee Block A"
            }
        } catch (e: Exception) {}
        
        // From produce registration
        try {
            farmerProduceRepository.findByFarmerId(farmerId).forEach { 
                crops.add(it.farmProduce.name)
            }
        } catch (e: Exception) {}
        
        if (crops.isEmpty()) {
            return "Start by adding your crops or production units to receive personalized AI insights."
        }
        
        // Normalize crop names (simple logic)
        val mainCrop = crops.firstOrNull { 
            it.contains("Coffee", true) || 
            it.contains("Tea", true) || 
            it.contains("Maize", true) || 
            it.contains("Corn", true)
        } ?: crops.first()
        
        // 2. Check Market Prices
        val prices = commodityPriceService.getPricesWithChange()
        val cropPrice = prices.find { price -> 
            mainCrop.contains(price.name, true) || 
            (mainCrop.contains("Corn", true) && price.name.contains("Maize", true)) ||
            (mainCrop.contains("Maize", true) && price.name.contains("Corn", true))
        }
        
        if (cropPrice != null && (cropPrice.changePercent ?: 0.0) > 2.0) {
            return "Good news! Global ${cropPrice.name} prices are up ${cropPrice.changePercent}%. Consider preparing your harvest for sale."
        }
        
        // 3. Check Weather (Location needed - assume first unit location or Nairobi)
        // In a real scenario, we'd fetch the farmer's location. 
        // For now, we'll generic advice if we can't get specific location easily without circular dependency or extra lookups
        // But we can try to get location from production units if available
        
        // (Simplified for this service - could inject LocationService if needed)
        
        return "Based on your $mainCrop production, ensure you are monitoring for local pests. Update your scout reports regularily."
    }
}
