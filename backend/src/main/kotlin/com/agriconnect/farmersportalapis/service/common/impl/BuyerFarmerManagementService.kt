package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.domain.common.enums.ConnectionStatus
import com.agriconnect.farmersportalapis.domain.common.model.FarmerBuyerConnection
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.math.abs

@Service
@Transactional
class BuyerFarmerManagementService(
    private val connectionRepository: ConnectionRepository,
    private val farmerRepository: FarmerRepository,
    private val buyerRepository: com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.BuyerRepository,
    private val farmerProduceRepository: FarmerProduceRepository,
    private val produceYieldRepository: ProduceYieldRepository,
    private val produceListingRepository: ProduceListingRepository,
    private val listingOrderRepository: ListingOrderRepository
) {

    fun searchAvailableFarmers(request: BuyerFarmerSearchRequestDto): List<FarmerSearchResultDto> {
        // Get farmers not already connected to this buyer
        val connectedFarmerIds = connectionRepository
            .findByBuyerIdAndStatus(request.buyerId, ConnectionStatus.ACTIVE)
            .map { it.farmerId }

        val farmers = if (request.searchQuery.isNullOrBlank()) {
            farmerRepository.findAll(PageRequest.of(0, 50)).content
        } else {
            // Check if search query contains digits (likely a phone number)
            val isPhoneSearch = request.searchQuery.any { it.isDigit() }
            if (isPhoneSearch) {
                farmerRepository.findByPhoneNumberContaining(request.searchQuery, PageRequest.of(0, 50)).content
            } else {
                farmerRepository.findByNameOrEmailContainingIgnoreCase(
                    request.searchQuery, PageRequest.of(0, 50)
                ).content
            }
        }

        return farmers
            .filter { it.id !in connectedFarmerIds }
            .map { farmer ->
                val produces = farmerProduceRepository.findByFarmerId(farmer.id)
                val upcomingHarvests = produces
                    .filter { it.predictedHarvestDate?.isAfter(LocalDate.now()) == true }
                    .take(3)

                FarmerSearchResultDto(
                    farmerId = farmer.id,
                    firstName = farmer.userProfile.fullName,
                    lastName = "",
                    location = (farmer.location?.customName ?: "Location not specified") as String,
                    phoneNumber = farmer.userProfile.phoneNumber,
                    email = farmer.userProfile.email,
                    totalProduces = produces.size,
                    upcomingHarvests = upcomingHarvests.size,
                    distance = calculateDistance(request.location?.toString(), farmer.location?.customName),
                    produceTypes = produces.mapNotNull { it.predictedSpecies }.distinct()
                )
            }
            .sortedBy { it.distance }
    }

    fun createFarmerConnection(buyerId: String, request: BuyerFarmerConnectionRequestDto): BuyerFarmerConnectionResponseDto {
        // Check if connection already exists
        val existingConnection = connectionRepository
            .findByFarmerIdAndBuyerId(request.farmerId, buyerId)

        if (existingConnection != null) {
            throw IllegalStateException("Connection already exists between buyer and farmer")
        }

        val connection = FarmerBuyerConnection(
            id = UUID.randomUUID().toString(),
            farmerId = request.farmerId,
            buyerId = buyerId,
            status = ConnectionStatus.ACTIVE,
            notes = request.notes,
            notificationPreference = request.notificationPreference,
            priorityLevel = request.priorityLevel,
            createdAt = LocalDateTime.now()
        )

        val savedConnection = connectionRepository.save(connection)
        val farmer = farmerRepository.findById(request.farmerId)
            .orElseThrow { IllegalArgumentException("Farmer not found") }

        return BuyerFarmerConnectionResponseDto(
            connectionId = savedConnection.id,
            farmerId = farmer.id,
            farmerName = "${farmer.userProfile.fullName} ${""}",
            status = savedConnection.status,
            createdAt = savedConnection.createdAt
        )
    }

    fun getBuyerFarmers(buyerId: String): List<BuyerFarmerResponseDto> {
        val connections = connectionRepository.findByBuyerIdAndStatus(buyerId, ConnectionStatus.ACTIVE)
        
        return connections.map { connection ->
            val farmer = farmerRepository.findById(connection.farmerId)
                .orElseThrow { IllegalArgumentException("Farmer not found") }
            
            val produces = farmerProduceRepository.findByFarmerId(farmer.id)
            val upcomingHarvests = produces
                .filter { it.predictedHarvestDate?.isAfter(LocalDate.now()) == true }
                .map { produce ->
                    HarvestPredictionDto2(
                        id="",
                        farmerProduceId = produce.id,
                        farmerId = produce.farmer.id,
                        farmerName = "${farmer.userProfile.fullName} ${""}",
                        produceName = produce.predictedSpecies ?: "Unknown",
                        plantingDate = produce.plantingDate,
                        predictedHarvestDate = produce.predictedHarvestDate,
                        predictedSpecies = produce.predictedSpecies,
                        confidence = produce.predictionConfidence,
                        status = "PREDICTED",
                        actualHarvestDate = produce.actualHarvestDate,
                    )
                }

            val performanceMetrics = calculateFarmerPerformance(farmer.id, buyerId)

            BuyerFarmerResponseDto(
                connectionId = connection.id,
                farmer = FarmerSummaryDto(
                    farmerId = farmer.id,
                    firstName = farmer.userProfile.fullName,
                    lastName = "",
                    location = farmer.location?.customName ?: "Location not specified",
                    phoneNumber = farmer.userProfile.phoneNumber,
                    email = farmer.userProfile.email
                ),
                status = connection.status,
                upcomingHarvests = upcomingHarvests,
                performanceMetrics = performanceMetrics,
                distance = null, // Would need buyer location to calculate
                notes = connection.notes
            )
        }
    }

    fun removeFarmerConnection(connectionId: String) {
        val connection = connectionRepository.findById(connectionId)
            .orElseThrow { IllegalArgumentException("Connection not found") }
        
        connection.status = ConnectionStatus.INACTIVE
        connectionRepository.save(connection)
    }

    fun getFarmerDetails(farmerId: String, buyerId: String): FarmerDetailsResponseDto {
        val farmer = farmerRepository.findById(farmerId)
            .orElseThrow { IllegalArgumentException("Farmer not found") }
        
        val connection = connectionRepository.findByFarmerIdAndBuyerId(farmerId, buyerId)
            ?: throw IllegalArgumentException("No connection found between buyer and farmer")

        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        val yields = produceYieldRepository.findByFarmerProduceIdIn(produces.map { it.id })
        val listings = produceListingRepository.findByFarmerId(farmerId)

        val harvestSchedule = produces.map { produce ->
            HarvestScheduleDto(
                farmerProduceId = produce.id,
                produceName = produce.predictedSpecies ?: "Unknown",
                plantingDate = produce.plantingDate,
                predictedHarvestDate = produce.predictedHarvestDate,
                actualHarvestDate = produce.actualHarvestDate,
                status = produce.status,
                confidence = produce.predictionConfidence
            )
        }

        val performanceMetrics = calculateFarmerPerformance(farmerId, buyerId)

        return FarmerDetailsResponseDto(
            farmer = FarmerSummaryDto(
                farmerId = farmer.id,
                firstName = farmer.userProfile.fullName,
                lastName = "",
                location = farmer.location?.customName?: "Location not specified",
                phoneNumber = farmer.userProfile.phoneNumber,
                email = farmer.userProfile.email
            ),
            connectionDetails = ConnectionDetailsDto(
                connectionId = connection.id,
                status = connection.status,
                createdAt = connection.createdAt,
                notes = connection.notes
            ),
            harvestSchedule = harvestSchedule,
            performanceMetrics = performanceMetrics,
            totalProduces = produces.size,
            totalYields = yields.size,
            totalListings = listings.size
        )
    }

    @Transactional
    fun updateFarmerNotes(connectionId: String, notes: String) {
        val connection = connectionRepository.findById(connectionId)
            .orElseThrow { IllegalArgumentException("Connection not found") }

        // Update the notes field
        connection.notes = notes
        connectionRepository.save(connection)
    }

    fun getBuyerDashboardAnalytics(buyerId: String): BuyerDashboardAnalyticsDto {
        val connections = connectionRepository.findByBuyerIdAndStatus(buyerId, ConnectionStatus.ACTIVE)
        val farmerIds = connections.map { it.farmerId }
        
        val totalFarmers = connections.size
        val totalOrders = listingOrderRepository.findAll().count { it.buyerId == buyerId }
        val pendingOrders = listingOrderRepository.findAll().count { it.buyerId == buyerId && it.status.name == "PENDING" }
        
        val produces = farmerProduceRepository.findByFarmerIdIn(farmerIds)
        val upcomingHarvests = produces.count { 
            it.predictedHarvestDate?.isAfter(LocalDate.now()) == true 
        }

        // Calculate monthly order trends (last 6 months)
        val monthlyTrends = (0..5).map { monthsAgo ->
            val date = LocalDate.now().minusMonths(monthsAgo.toLong())
            val orderCount = listingOrderRepository.findAll().count { order ->
                order.buyerId == buyerId && 
                order.dateCreated.toLocalDate().let { orderDate ->
                    orderDate.isAfter(date.withDayOfMonth(1).minusDays(1)) &&
                    orderDate.isBefore(date.withDayOfMonth(date.lengthOfMonth()).plusDays(1))
                }
            }
            MonthlyTrendDto(
                month = date.month.name,
                year = date.year,
                orderCount = orderCount,
                totalAmount = 0.0 // Would need order amount calculation
            )
        }.reversed()

        return BuyerDashboardAnalyticsDto(
            totalFarmers = totalFarmers,
            totalOrders = totalOrders,
            pendingOrders = pendingOrders,
            upcomingHarvests = upcomingHarvests,
            monthlyTrends = monthlyTrends,
            topProduceTypes = getTopProduceTypes(farmerIds),
            averageDeliveryTime = calculateAverageDeliveryTime(buyerId)
        )
    }

    fun getFarmerPerformanceAnalytics(buyerId: String): List<FarmerPerformanceAnalyticsDto> {
        val connections = connectionRepository.findByBuyerIdAndStatus(buyerId, ConnectionStatus.ACTIVE)
        
        return connections.map { connection ->
            val farmer = farmerRepository.findById(connection.farmerId)
                .orElseThrow { IllegalArgumentException("Farmer not found") }
            
            val performance = calculateFarmerPerformance(connection.farmerId, buyerId)
            
            FarmerPerformanceAnalyticsDto(
                farmerId = farmer.id,
                farmerName = "${farmer.userProfile.fullName} ${""}",
                reliabilityScore = performance.reliabilityScore,
                qualityRating = performance.qualityRating,
                onTimeDeliveryRate = performance.onTimeDeliveryRate,
                totalOrders = performance.totalOrders,
                averageYield = performance.averageYield,
                lastOrderDate = getLastOrderDate(connection.farmerId, buyerId),
                upcomingHarvests = getUpcomingHarvestsCount(connection.farmerId)
            )
        }
    }

    fun getFarmerPerformance(farmerId: String, buyerId: String): FarmerPerformanceDto {
        return calculateFarmerPerformance(farmerId, buyerId)
    }

    fun getSeasonalTrends(buyerId: String): SeasonalTrendsDto {
        val connections = connectionRepository.findByBuyerIdAndStatus(buyerId, ConnectionStatus.ACTIVE)
        val farmerIds = connections.map { it.farmerId }
        
        val produces = farmerProduceRepository.findByFarmerIdIn(farmerIds)
        
        // Group by season based on planting/harvest dates
        val seasonalData = produces.groupBy { produce ->
            when (produce.plantingDate?.monthValue) {
                in 3..5 -> "Spring"
                in 6..8 -> "Summer"
                in 9..11 -> "Fall"
                else -> "Winter"
            }
        }.mapValues { (_, produces) ->
            SeasonalDataDto(
                totalProduces = produces.size,
                averageYield = calculateAverageYieldForProduces(produces.map { it.id }),
                topProduceTypes = produces.mapNotNull { it.predictedSpecies }
                    .groupingBy { it }.eachCount()
                    .toList().sortedByDescending { it.second }.take(5)
                    .map { ProduceTypeCountDto(it.first, it.second) }
            )
        }

        return SeasonalTrendsDto(
            spring = seasonalData["Spring"] ?: SeasonalDataDto(0, 0.0, emptyList()),
            summer = seasonalData["Summer"] ?: SeasonalDataDto(0, 0.0, emptyList()),
            fall = seasonalData["Fall"] ?: SeasonalDataDto(0, 0.0, emptyList()),
            winter = seasonalData["Winter"] ?: SeasonalDataDto(0, 0.0, emptyList())
        )
    }

    private fun calculateFarmerPerformance(farmerId: String?, buyerId: String): FarmerPerformanceDto {
        val orders = listingOrderRepository.findAll().filter { 
            it.produceListing.farmerProduce.farmer.id == farmerId && it.buyerId == buyerId 
        }
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        val yields = produceYieldRepository.findByFarmerProduceIdIn(produces.map { it.id })

        val totalOrders = orders.size
        val onTimeOrders = orders.count { it.status.name == "SUPPLIED_AND_PAID" } // Simplified
        val onTimeDeliveryRate = if (totalOrders > 0) (onTimeOrders.toDouble() / totalOrders) * 100 else 0.0

        val averageYield = if (yields.isNotEmpty()) {
            yields.map { it.yieldAmount }.average()
        } else 0.0

        // Calculate reliability score based on harvest predictions vs actual
        val reliabilityScore = produces
            .filter { it.predictedHarvestDate != null && it.actualHarvestDate != null }
            .map { produce ->
                val predicted = produce.predictedHarvestDate!!
                val actual = produce.actualHarvestDate!!
                val daysDifference = abs(predicted.toEpochDay() - actual.toEpochDay())
                maxOf(0.0, 100.0 - (daysDifference * 2)) // 2 points per day difference
            }
            .takeIf { it.isNotEmpty() }?.average() ?: 75.0 // Default score

        return FarmerPerformanceDto(
            reliabilityScore = reliabilityScore,
            qualityRating = 4.2, // Would be calculated from buyer ratings
            onTimeDeliveryRate = onTimeDeliveryRate,
            totalOrders = totalOrders,
            averageYield = averageYield,
            seasonalAvailability = mapOf(
                "Spring" to produces.filter { it.plantingDate?.monthValue in 3..5 }
                    .mapNotNull { it.predictedSpecies }.distinct(),
                "Summer" to produces.filter { it.plantingDate?.monthValue in 6..8 }
                    .mapNotNull { it.predictedSpecies }.distinct(),
                "Fall" to produces.filter { it.plantingDate?.monthValue in 9..11 }
                    .mapNotNull { it.predictedSpecies }.distinct(),
                "Winter" to produces.filter { it.plantingDate?.monthValue !in 3..11 }
                    .mapNotNull { it.predictedSpecies }.distinct()
            )
        )
    }

    private fun calculateDistance(buyerLocation: String?, farmerLocation: String?): Double? {
        // Simplified distance calculation - would use proper geolocation in production
        return if (buyerLocation != null && farmerLocation != null) {
            Random().nextDouble() * 50 // Random distance for demo
        } else null
    }

    private fun getTopProduceTypes(farmerIds: List<String>): List<ProduceTypeCountDto> {
        val produces = farmerProduceRepository.findByFarmerIdIn(farmerIds)
        return produces.mapNotNull { it.predictedSpecies }
            .groupingBy { it }.eachCount()
            .toList().sortedByDescending { it.second }.take(5)
            .map { ProduceTypeCountDto(it.first, it.second) }
    }

    private fun calculateAverageDeliveryTime(buyerId: String): Double {
        // Simplified calculation - would use actual delivery data
        return 2.5 // Average days
    }

    private fun getLastOrderDate(farmerId: String, buyerId: String): LocalDate? {
        return listingOrderRepository.findAll()
            .filter { it.produceListing.farmerProduce.farmer.id == farmerId && it.buyerId == buyerId }
            .maxByOrNull { it.dateCreated }?.dateCreated?.toLocalDate()
    }

    private fun getUpcomingHarvestsCount(farmerId: String): Int {
        return farmerProduceRepository.findByFarmerId(farmerId)
            .count { it.predictedHarvestDate?.isAfter(LocalDate.now()) == true }
    }

    private fun calculateAverageYieldForProduces(produceIds: List<String>): Double {
        val yields = produceYieldRepository.findByFarmerProduceIdIn(produceIds)
        return if (yields.isNotEmpty()) yields.map { it.yieldAmount }.average() else 0.0
    }
}