package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.domain.common.enums.ConnectionStatus
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.math.abs

@Service
@Transactional
class BuyerAnalyticsService(
    private val connectionRepository: ConnectionRepository,
    private val farmerRepository: FarmerRepository,
    private val farmerProduceRepository: FarmerProduceRepository,
    private val produceYieldRepository: ProduceYieldRepository,
    private val produceListingRepository: ProduceListingRepository,
    private val listingOrderRepository: ListingOrderRepository
) {

    fun getBuyerDashboardSummary(buyerId: String): BuyerDashboardAnalyticsDto {
        val connections = connectionRepository.findByBuyerIdAndStatus(buyerId, ConnectionStatus.ACTIVE)
        val farmerIds = connections.map { it.farmerId }
        
        val totalFarmers = connections.size
        val totalOrders = listingOrderRepository.findAll().count { it.buyerId == buyerId }
        val pendingOrders = listingOrderRepository.findAll().count { 
            it.buyerId == buyerId && it.status.name == "PENDING" 
        }
        
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
                totalAmount = calculateMonthlyOrderAmount(buyerId, date)
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

    fun getFarmerPerformanceMetrics(buyerId: String): List<FarmerPerformanceAnalyticsDto> {
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

    fun getSeasonalAvailabilityTrends(buyerId: String): SeasonalTrendsDto {
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

    fun getFarmerComparisonAnalytics(buyerId: String, farmerIds: List<String>): List<FarmerPerformanceAnalyticsDto> {
        return farmerIds.mapNotNull { farmerId ->
            // Verify farmer is connected to buyer
            val connection = connectionRepository.findByFarmerIdAndBuyerId(farmerId, buyerId)
            if (connection != null && connection.status == ConnectionStatus.ACTIVE) {
                val farmer = farmerRepository.findById(farmerId)
                    .orElseThrow { IllegalArgumentException("Farmer not found") }
                
                val performance = calculateFarmerPerformance(farmerId, buyerId)
                
                FarmerPerformanceAnalyticsDto(
                    farmerId = farmer.id,
                    farmerName = "${farmer.userProfile.fullName} ${""}",
                    reliabilityScore = performance.reliabilityScore,
                    qualityRating = performance.qualityRating,
                    onTimeDeliveryRate = performance.onTimeDeliveryRate,
                    totalOrders = performance.totalOrders,
                    averageYield = performance.averageYield,
                    lastOrderDate = getLastOrderDate(farmerId, buyerId),
                    upcomingHarvests = getUpcomingHarvestsCount(farmerId)
                )
            } else null
        }
    }

    fun getBuyerOrderAnalytics(buyerId: String): BuyerOrderAnalyticsDto {
        val orders = listingOrderRepository.findAll().filter { it.buyerId == buyerId }
        
        val totalOrders = orders.size
        val completedOrders = orders.count { it.status.name == "SUPPLIED_AND_PAID" }
        val pendingOrders = orders.count { it.status.name == "PENDING" }
        val cancelledOrders = orders.count { it.status.name == "DECLINED" }
        
        val totalSpent = orders.filter { it.status.name == "SUPPLIED_AND_PAID" }
            .sumOf { it.quantity * it.produceListing.price.price }
        
        val averageOrderValue = if (completedOrders > 0) totalSpent / completedOrders else 0.0
        
        // Calculate order trends by month
        val orderTrends = (0..11).map { monthsAgo ->
            val date = LocalDate.now().minusMonths(monthsAgo.toLong())
            val monthOrders = orders.filter { order ->
                order.dateCreated.toLocalDate().let { orderDate ->
                    orderDate.isAfter(date.withDayOfMonth(1).minusDays(1)) &&
                    orderDate.isBefore(date.withDayOfMonth(date.lengthOfMonth()).plusDays(1))
                }
            }
            
            MonthlyOrderTrendDto(
                month = date.month.name,
                year = date.year,
                orderCount = monthOrders.size,
                totalAmount = monthOrders.filter { it.status.name == "SUPPLIED_AND_PAID" }
                    .sumOf { it.quantity * it.produceListing.price.price },
                averageOrderValue = if (monthOrders.isNotEmpty()) 
                    monthOrders.sumOf { it.quantity * it.produceListing.price.price } / monthOrders.size else 0.0
            )
        }.reversed()

        return BuyerOrderAnalyticsDto(
            totalOrders = totalOrders,
            completedOrders = completedOrders,
            pendingOrders = pendingOrders,
            cancelledOrders = cancelledOrders,
            totalSpent = totalSpent,
            averageOrderValue = averageOrderValue,
            orderTrends = orderTrends,
            topProducts = getTopOrderedProducts(buyerId)
        )
    }

    private fun calculateFarmerPerformance(farmerId: String, buyerId: String): FarmerPerformanceDto {
        val orders = listingOrderRepository.findAll().filter { 
            it.produceListing.farmerProduce.farmer.id == farmerId && it.buyerId == buyerId 
        }
        val produces = farmerProduceRepository.findByFarmerId(farmerId)
        val yields = produceYieldRepository.findByFarmerProduceIdIn(produces.map { it.id })

        val totalOrders = orders.size
        val onTimeOrders = orders.count { it.status.name == "SUPPLIED_AND_PAID" }
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

    private fun calculateMonthlyOrderAmount(buyerId: String, date: LocalDate): Double {
        return listingOrderRepository.findAll()
            .filter { order ->
                order.buyerId == buyerId && 
                order.status.name == "SUPPLIED_AND_PAID" &&
                order.dateCreated.toLocalDate().let { orderDate ->
                    orderDate.isAfter(date.withDayOfMonth(1).minusDays(1)) &&
                    orderDate.isBefore(date.withDayOfMonth(date.lengthOfMonth()).plusDays(1))
                }
            }
            .sumOf { it.quantity * it.produceListing.price.price }
    }

    private fun getTopProduceTypes(farmerIds: List<String>): List<ProduceTypeCountDto> {
        val produces = farmerProduceRepository.findByFarmerIdIn(farmerIds)
        return produces.mapNotNull { it.predictedSpecies }
            .groupingBy { it }.eachCount()
            .toList().sortedByDescending { it.second }.take(5)
            .map { ProduceTypeCountDto(it.first, it.second) }
    }

    private fun calculateAverageDeliveryTime(buyerId: String): Double {
        val completedOrders = listingOrderRepository.findAll()
            .filter { it.buyerId == buyerId && it.status.name == "SUPPLIED_AND_PAID" }
        
        if (completedOrders.isEmpty()) return 0.0
        
        val deliveryTimes = completedOrders.mapNotNull { order ->
            order.dateSupplied?.let { supplied ->
                val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                    order.dateCreated.toLocalDate(), 
                    supplied.toLocalDate()
                )
                daysBetween.toDouble()
            }
        }
        
        return if (deliveryTimes.isNotEmpty()) deliveryTimes.average() else 0.0
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

    private fun getTopOrderedProducts(buyerId: String): List<ProduceTypeCountDto> {
        val orders = listingOrderRepository.findAll()
            .filter { it.buyerId == buyerId && it.status.name == "SUPPLIED_AND_PAID" }
        
        return orders.mapNotNull { it.produceListing.farmerProduce.predictedSpecies }
            .groupingBy { it }.eachCount()
            .toList().sortedByDescending { it.second }.take(5)
            .map { ProduceTypeCountDto(it.first, it.second) }
    }

    fun getBuyerCharts(buyerId: String): BuyerChartsResponseDto {
        // Get order trends for the last 6 months
        val orderTrends = getOrderTrendsData(buyerId)

        // Get spend analysis by product category
        val spendAnalysis = getSpendAnalysisData(buyerId)

        return BuyerChartsResponseDto(
            orderTrends = orderTrends,
            spendAnalysis = spendAnalysis
        )
    }

    private fun getOrderTrendsData(buyerId: String): ChartDataDto {
        val now = LocalDate.now()
        val months = (0..5).map { now.minusMonths(it.toLong()) }

        val labels = months.map { "${it.month.name.substring(0, 3)} ${it.year}" }.reversed()
        val data = months.reversed().map { month ->
            listingOrderRepository.findAll()
                .filter { order ->
                    order.buyerId == buyerId &&
                    order.status.name == "SUPPLIED_AND_PAID" &&
                    order.dateCreated.toLocalDate().let { orderDate ->
                        orderDate.year == month.year && orderDate.month == month.month
                    }
                }
                .size.toDouble()
        }

        return ChartDataDto(
            labels = labels,
            data = data,
            type = "line",
            title = "Order Trends",
            yAxisLabel = "Number of Orders"
        )
    }

    private fun getSpendAnalysisData(buyerId: String): ChartDataDto {
        val orders = listingOrderRepository.findAll()
            .filter { it.buyerId == buyerId && it.status.name == "SUPPLIED_AND_PAID" }

        val spendByCategory = orders
            .groupBy { it.produceListing.farmerProduce.predictedSpecies ?: "Unknown" }
            .mapValues { (_, categoryOrders) ->
                categoryOrders.sumOf { it.quantity * it.produceListing.price.price }
            }
            .toList()
            .sortedByDescending { it.second }
            .take(5)

        val labels = spendByCategory.map { it.first }
        val data = spendByCategory.map { it.second }

        return ChartDataDto(
            labels = labels,
            data = data,
            type = "pie",
            title = "Spend Analysis by Product",
            yAxisLabel = null
        )
    }
}