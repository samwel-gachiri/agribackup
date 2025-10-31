package com.agriconnect.farmersportalapis.application.dtos

import com.agriconnect.farmersportalapis.domain.common.enums.ConnectionStatus
import com.agriconnect.farmersportalapis.domain.common.enums.FarmerProduceStatus
import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime


// Request DTOs
data class BuyerFarmerSearchRequestDto(
    @field:NotBlank val buyerId: String,
    val searchQuery: String?,
    val location: LocationDto?,
    val maxDistance: Double?,
    val produceTypes: List<String>?
)

data class BuyerFarmerConnectionRequestDto(
    @field:NotBlank val farmerId: String,
    val notes: String?,
    val notificationPreference: String? = "all", // all, important, none
    val priorityLevel: String? = "normal" // high, normal, low
)

data class UpdateFarmerNotesRequestDto(
    val notes: String
)

// Response DTOs
data class FarmerSearchResultDto(
    val farmerId: String?,
    val firstName: String,
    val lastName: String,
    val location: String,
    val phoneNumber: String?,
    val email: String?,
    val totalProduces: Int,
    val upcomingHarvests: Int,
    val distance: Double?,
    val produceTypes: List<String>
)
//data class FarmerSearchResultDto(
//    val id: String,
//    val fullName: String,
//    val phoneNumber: String,
//    val email: String?,
//    val avatar: String?,
//    val location: LocationSummaryDto?,
//    val currentCrops: List<String>,
//    val rating: Double?,
//    val isAlreadyConnected: Boolean
//)


data class BuyerFarmerConnectionResponseDto(
    val connectionId: String,
    val farmerId: String?,
    val farmerName: String,
    val status: ConnectionStatus,
    val createdAt: LocalDateTime
)

data class BuyerFarmerResponseDto(
    val connectionId: String,
    val farmer: FarmerSummaryDto,
    val status: ConnectionStatus,
    val upcomingHarvests: List<HarvestPredictionDto2>,
    val performanceMetrics: FarmerPerformanceDto,
    val distance: Double?,
    val notes: String?
)

data class FarmerDetailsResponseDto(
    val farmer: FarmerSummaryDto,
    val connectionDetails: ConnectionDetailsDto,
    val harvestSchedule: List<HarvestScheduleDto>,
    val performanceMetrics: FarmerPerformanceDto,
    val totalProduces: Int,
    val totalYields: Int,
    val totalListings: Int
)

data class ConnectionDetailsDto(
    val connectionId: String,
    val status: ConnectionStatus,
    val createdAt: LocalDateTime,
    val notes: String?
)

data class HarvestScheduleDto(
    val farmerProduceId: String,
    val produceName: String,
    val plantingDate: LocalDate?,
    val predictedHarvestDate: LocalDate?,
    val actualHarvestDate: LocalDate?,
    val status: FarmerProduceStatus,
    val confidence: Double?
)

data class FarmerPerformanceDto(
    val reliabilityScore: Double,
    val qualityRating: Double,
    val onTimeDeliveryRate: Double,
    val totalOrders: Int,
    val averageYield: Double,
    val seasonalAvailability: Map<String, List<String>>
)

// Analytics DTOs
data class BuyerDashboardAnalyticsDto(
    val totalFarmers: Int,
    val totalOrders: Int,
    val pendingOrders: Int,
    val upcomingHarvests: Int,
    val monthlyTrends: List<MonthlyTrendDto>,
    val topProduceTypes: List<ProduceTypeCountDto>,
    val averageDeliveryTime: Double
)

data class MonthlyTrendDto(
    val month: String,
    val year: Int,
    val orderCount: Int,
    val totalAmount: Double
)

data class ProduceTypeCountDto(
    val produceName: String,
    val count: Int
)

data class FarmerPerformanceAnalyticsDto(
    val farmerId: String?,
    val farmerName: String,
    val reliabilityScore: Double,
    val qualityRating: Double,
    val onTimeDeliveryRate: Double,
    val totalOrders: Int,
    val averageYield: Double,
    val lastOrderDate: LocalDate?,
    val upcomingHarvests: Int
)

data class FarmerPerformanceAnalyticsDto2(
    val connectionId: String,
    val farmer: FarmerSummaryDto,
    val performanceMetrics: FarmerPerformanceMetricsDto,
    val orderHistory: List<OrderSummaryDto>,
    val yieldHistory: List<YieldSummaryDto>,
    val reliabilityTrend: String, // improving, declining, stable
    val qualityTrend: String,
    val recommendations: List<String>
)


data class SeasonalTrendsDto(
    val spring: SeasonalDataDto,
    val summer: SeasonalDataDto,
    val fall: SeasonalDataDto,
    val winter: SeasonalDataDto
)

data class SeasonalDataDto(
    val totalProduces: Int,
    val averageYield: Double,
    val topProduceTypes: List<ProduceTypeCountDto>
)

// Existing DTOs (keeping for backward compatibility)

data class BuyerFarmerConnectionDto(
    val connectionId: String,
    val farmer: FarmerSummaryDto,
    val connectionDate: LocalDateTime,
    val status: String, // ACTIVE, INACTIVE, PENDING, BLOCKED
    val notes: String?,
    val notificationPreference: String?,
    val priorityLevel: String?,
    val performanceMetrics: FarmerPerformanceMetricsDto?,
    val upcomingHarvests: List<UpcomingHarvestDto>?
)

data class FarmerSummaryDto(
    val farmerId: String?,
    val firstName: String?,
    val lastName: String,
    val location: String,
    val phoneNumber: String?,
    val email: String?
)

data class LocationSummaryDto(
    val customName: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class FarmerPerformanceMetricsDto(
    val qualityRating: Double, // 1-5 stars
    val reliabilityScore: Double, // 0-100 percentage
    val totalOrders: Int,
    val onTimeDeliveries: Double, // Percentage
    val averageOrderValue: Double,
    val lastOrderDate: LocalDate?
)

data class UpcomingHarvestDto(
    val id: String,
    val produceName: String,
    val expectedDate: LocalDate,
    val estimatedQuantity: Double?,
    val unit: String?
)


data class ConnectToFarmerRequestDto(
    val farmerId: String,
    val notes: String?,
    val notificationPreference: String? = "all", // all, important, none
    val priorityLevel: String? = "normal" // high, normal, low
)

data class UpdateConnectionNotesRequestDto(
    val notes: String
)

data class UpdateConnectionStatusRequestDto(
    val status: String, // ACTIVE, INACTIVE, BLOCKED
    val reason: String?
)

data class OrderSummaryDto(
    val id: String,
    val orderDate: LocalDate,
    val totalAmount: Double,
    val productCount: Int,
    val status: String,
    val deliveryDate: LocalDate?
)

data class YieldSummaryDto(
    val produceName: String,
    val harvestDate: LocalDate,
    val yieldAmount: Double,
    val yieldUnit: String,
    val quality: String?
)

data class InviteFarmerRequestDto(
    val phoneNumber: String,
    val name: String,
    val message: String?
)

data class InvitationResponseDto(
    val invitationId: String,
    val phoneNumber: String,
    val name: String,
    val status: String, // SENT, DELIVERED, FAILED
    val sentAt: LocalDateTime
)

data class FarmerAvailabilityDto2(
    val farmerId: String,
    val farmerName: String,
    val location: LocationSummaryDto?,
    val availableProducts: List<AvailableProductDto>,
    val estimatedTotalLoad: Double,
    val preferredPickupTime: String?,
    val specialInstructions: String?
)

data class AvailableProductDto(
    val produceName: String,
    val estimatedQuantity: Double,
    val unit: String,
    val harvestDate: LocalDate,
    val quality: String?
)

data class ConnectionActivityDto(
    val id: String,
    val activityType: String, // ORDER_PLACED, HARVEST_COMPLETED, NOTE_UPDATED, etc.
    val title: String,
    val description: String,
    val date: LocalDateTime,
    val relatedEntityId: String?,
    val relatedEntityType: String? // ORDER, YIELD, CONNECTION
)

data class FarmerOrderHistoryDto(
    val orderId: String,
    val orderDate: LocalDate,
    val products: List<OrderProductDto>,
    val totalAmount: Double,
    val status: String,
    val deliveryDate: LocalDate?,
    val rating: Double?,
    val feedback: String?
)

data class OrderProductDto(
    val produceName: String,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val totalPrice: Double
)

// Additional Analytics DTOs
data class BuyerOrderAnalyticsDto(
    val totalOrders: Int,
    val completedOrders: Int,
    val pendingOrders: Int,
    val cancelledOrders: Int,
    val totalSpent: Double,
    val averageOrderValue: Double,
    val orderTrends: List<MonthlyOrderTrendDto>,
    val topProducts: List<ProduceTypeCountDto>
)

data class MonthlyOrderTrendDto(
    val month: String,
    val year: Int,
    val orderCount: Int,
    val totalAmount: Double,
    val averageOrderValue: Double
)

data class BuyerChartsResponseDto(
    val orderTrends: ChartDataDto,
    val spendAnalysis: ChartDataDto
)