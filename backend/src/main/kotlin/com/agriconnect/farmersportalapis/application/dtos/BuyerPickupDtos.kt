package com.agriconnect.farmersportalapis.application.dtos

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate
import java.time.LocalDateTime

// Request DTOs
data class GenerateRouteRequestDto(
    @field:NotBlank val buyerId: String,
    val farmerIds: List<String>,
    val preferredDate: LocalDate?,
    val startTime: String?, // HH:mm format
    val maxDistance: Double?,
    val vehicleCapacity: Double?,
    val optimizationPreference: String? = "TIME" // TIME, DISTANCE, COST
)

data class OptimizeRouteRequestDto(
    val optimizationPreference: String, // TIME, DISTANCE, COST
    val constraints: RouteConstraintsDto?
)

data class RouteConstraintsDto(
    val maxDistance: Double?,
    val maxDuration: Int?, // minutes
    val vehicleCapacity: Double?,
    val avoidTolls: Boolean = false,
    val avoidHighways: Boolean = false
)

// Response DTOs
// The buyer-specific route response uses the dedicated BuyerPickupRouteResponseDto
// declared in its own file. Do not use a typealias here to avoid duplicate
// declarations across the codebase.

data class RouteWaypointDto(
    val waypointId: String,
    val farmerId: String?,
    val farmerName: String?,
    val location: LocationDto,
    val estimatedArrival: String, // HH:mm format
    val estimatedDuration: Int, // minutes at location
    val orderDetails: List<PickupOrderDto>,
    val specialInstructions: String?,
    val sequenceNumber: Int
)

data class PickupOrderDto(
    val orderId: String,
    val produceName: String,
    val quantity: Double,
    val unit: String,
    val estimatedWeight: Double,
    val packagingRequirements: String?
)

data class PickupRouteDetailsDto(
    val route: BuyerPickupRouteResponseDto,
    val farmerDetails: List<FarmerPickupDetailsDto>,
    val routeStatistics: RouteStatisticsDto,
    val weatherForecast: WeatherForecastDto?
)

data class FarmerPickupDetailsDto(
    val farmer: FarmerSummaryDto,
    val orders: List<PickupOrderDto>,
    val location: LocationDto,
    val contactInfo: ContactInfoDto,
    val accessInstructions: String?,
    val availableTimeWindows: List<TimeWindowDto>
)

data class ContactInfoDto(
    val phoneNumber: String?,
    val alternatePhone: String?,
    val email: String?
)

data class TimeWindowDto(
    val startTime: String, // HH:mm
    val endTime: String,   // HH:mm
    val dayOfWeek: String?
)

data class RouteStatisticsDto(
    val totalFarmers: Int,
    val totalOrders: Int,
    val totalWeight: Double,
    val totalValue: Double,
    val fuelCost: Double,
    val estimatedFuelConsumption: Double,
    val carbonFootprint: Double
)

data class WeatherForecastDto(
    val date: LocalDate,
    val temperature: String,
    val conditions: String,
    val precipitation: String,
    val windSpeed: String,
    val visibility: String
)

data class FarmerAvailabilityDto(
    val farmerId: String?,
    val farmerName: String,
    val location: LocationDto,
    val availableOrders: List<AvailableOrderDto>,
    val estimatedTotalWeight: Double,
    val preferredPickupTimes: List<TimeWindowDto>,
    val specialInstructions: String?,
    val lastPickupDate: LocalDate?,
    val reliabilityScore: Double
)

data class AvailableOrderDto(
    val orderId: String,
    val produceName: String,
    val quantity: Double,
    val unit: String,
    val readyDate: LocalDate,
    val expiryDate: LocalDate?,
    val priority: String // HIGH, MEDIUM, LOW
)

data class NotificationResponseDto(
    val routeId: String,
    val notificationsSent: Int,
    val notificationsFailed: Int,
    val farmerNotifications: List<FarmerNotificationDto>
)

data class FarmerNotificationDto(
    val farmerId: String?,
    val farmerName: String,
    val notificationStatus: String, // SENT, DELIVERED, FAILED
    val sentAt: LocalDateTime,
    val deliveredAt: LocalDateTime?
)

// Route optimization DTOs
data class RouteOptimizationResultDto(
    val originalRoute: BuyerPickupRouteResponseDto,
    val optimizedRoute: BuyerPickupRouteResponseDto,
    val improvements: RouteImprovementsDto
)

data class RouteImprovementsDto(
    val distanceSaved: Double,
    val timeSaved: Int, // minutes
    val costSaved: Double,
    val fuelSaved: Double,
    val carbonReduced: Double
)

// Manual route planning DTOs
data class ManualRouteRequestDto(
    val buyerId: String,
    val waypoints: List<ManualWaypointDto>,
    val routePreferences: RoutePreferencesDto
)

data class ManualWaypointDto(
    val farmerId: String,
    val sequenceNumber: Int,
    val estimatedDuration: Int, // minutes
    val notes: String?
)

data class RoutePreferencesDto(
    val avoidTolls: Boolean = false,
    val avoidHighways: Boolean = false,
    val preferScenicRoute: Boolean = false,
    val optimizeFor: String = "TIME" // TIME, DISTANCE, FUEL
)

// NOTE: Previously a typealias pointed to PickupRouteResponseDto. The rich
// BuyerPickupRouteResponseDto is declared above and should be used directly.