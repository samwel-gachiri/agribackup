package com.agriconnect.farmersportalapis.service.farmer

import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerProduceRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProduceListingRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProductionUnitRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Service for tracking farmer activities for the dashboard activity feed
 */
@Service
class FarmerActivityService(
    private val productionUnitRepository: ProductionUnitRepository,
    private val farmerProduceRepository: FarmerProduceRepository,
    private val produceListingRepository: ProduceListingRepository
) {

    /**
     * Get recent activities for a farmer
     * Aggregates activities from production units, produce registrations, and listings
     */
    fun getRecentActivities(farmerId: String, limit: Int = 10): List<FarmerActivityDto> {
        val activities = mutableListOf<FarmerActivityDto>()

        // Get production unit registrations
        try {
            val units = productionUnitRepository.findByFarmerId(farmerId)
            units.forEach { unit ->
                activities.add(
                    FarmerActivityDto(
                        id = "unit-${unit.id}",
                        action = "registered production unit",
                        description = "Registered ${unit.unitName} (${unit.areaHectares} ha)",
                        entityType = "PRODUCTION_UNIT",
                        entityId = unit.id,
                        createdAt = unit.lastVerifiedAt ?: LocalDateTime.now(),
                        userName = null
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore errors
        }

        // Get farmer produce registrations
        try {
            val produces = farmerProduceRepository.findByFarmerId(farmerId)
            produces.forEach { produce ->
                activities.add(
                    FarmerActivityDto(
                        id = "produce-${produce.id}",
                        action = "added produce",
                        description = "Added ${produce.farmProduce.name} to farm",
                        entityType = "FARMER_PRODUCE",
                        entityId = produce.id,
                        createdAt = produce.plantingDate?.atStartOfDay() ?: LocalDateTime.now(),
                        userName = null
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore errors
        }

        // Get listings
        try {
            val listings = produceListingRepository.findByFarmerId(farmerId)
            listings.forEach { listing ->
                val produceName = listing.farmerProduce.farmProduce.name
                activities.add(
                    FarmerActivityDto(
                        id = "listing-${listing.id}",
                        action = "created listing",
                        description = "Listed ${listing.quantity} kg of $produceName for sale",
                        entityType = "LISTING",
                        entityId = listing.id,
                        createdAt = listing.createdAt,
                        userName = null
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore errors
        }

        // Sort by date (newest first) and limit
        return activities
            .sortedByDescending { it.createdAt }
            .take(limit)
    }
}

/**
 * DTO for farmer activity
 */
data class FarmerActivityDto(
    val id: String,
    val action: String,
    val description: String,
    val entityType: String,
    val entityId: String,
    val createdAt: LocalDateTime,
    val userName: String?
) {
    fun toResponseMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "action" to action,
            "description" to description,
            "entityType" to entityType,
            "entityId" to entityId,
            "createdAt" to createdAt.format(DateTimeFormatter.ISO_DATE_TIME),
            "userName" to userName
        )
    }
}
