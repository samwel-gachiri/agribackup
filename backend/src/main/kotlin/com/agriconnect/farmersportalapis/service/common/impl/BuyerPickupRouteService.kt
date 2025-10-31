package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.domain.common.enums.ConnectionStatus
import com.agriconnect.farmersportalapis.domain.listing.ListingOrder
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
@Transactional
class BuyerPickupRouteService(
    private val connectionRepository: ConnectionRepository,
    private val farmerRepository: FarmerRepository,
    private val listingOrderRepository: ListingOrderRepository,
    private val pickupRouteRepository: BuyerPickupRouteRepository,
    private val routeWaypointRepository: RouteWaypointRepository
) {

    fun generateOptimalRoute(buyerId: String, request: GenerateRouteRequestDto): BuyerPickupRouteResponseDto {
        // Validate buyer has connections to all requested farmers
        val connections = connectionRepository.findByBuyerIdAndStatus(buyerId, ConnectionStatus.ACTIVE)
        val connectedFarmerIds = connections.map { it.farmerId }
        
        val validFarmerIds = request.farmerIds.filter { it in connectedFarmerIds }
        if (validFarmerIds.isEmpty()) {
            throw IllegalArgumentException("No valid farmer connections found")
        }

        // Get farmer locations and orders
        val farmers = farmerRepository.findAllById(validFarmerIds)
        val farmerOrders = getFarmerOrders(buyerId, validFarmerIds)
        
        // Generate route using optimization algorithm
        val optimizedWaypoints = optimizeRoute(farmers, farmerOrders, request)
        
        // Calculate route statistics
        val totalDistance = calculateTotalDistance(optimizedWaypoints)
        val estimatedDuration = calculateTotalDuration(optimizedWaypoints)
        val totalCost = calculateRouteCost(totalDistance, estimatedDuration)
        
        // Create and save route com.agriconnect.farmersportalapis.application.common.impl
        val route = BuyerPickupRoute(
            id = UUID.randomUUID().toString(),
            buyerId = buyerId,
            status = "DRAFT",
            totalDistance = totalDistance,
            estimatedDuration = estimatedDuration,
            totalCost = totalCost,
            pickupDate = request.preferredDate ?: LocalDate.now().plusDays(1),
            startTime = request.startTime ?: "08:00",
            createdAt = LocalDateTime.now()
        )
        
        // For now, return a mock response since we don't have the full repository setup
        return BuyerPickupRouteResponseDto(
            routeId = route.id,
            buyerId = buyerId,
            status = "DRAFT",
            totalDistanceKm = totalDistance,
            estimatedDurationMinutes = estimatedDuration,
            totalCost = totalCost,
            pickupDate = request.preferredDate ?: LocalDate.now().plusDays(1),
            startTime = request.startTime ?: "08:00",
            waypoints = optimizedWaypoints,
            routeGeometry = null,
            createdAt = LocalDateTime.now(),
            confirmedAt = null,
            zoneId = "",
            exporterId = "",
            zoneSupervisorId = "",
            scheduledDate = LocalDateTime.now(),
            stops = optimizedWaypoints.map { waypoint ->
                PickupRouteStopDto(
                    stopId = null,
                    farmerId = waypoint.farmerId ?: "",
                    farmerName = waypoint.farmerName,
                    sequenceOrder = waypoint.sequenceNumber,
                    status = "PENDING",
                    arrivalTime = null,
                    completionTime = null,
                    notes = waypoint.specialInstructions,
                    latitude = waypoint.location.latitude,
                    longitude = waypoint.location.longitude
                )
            }
        )
    }

    fun getBuyerRoutes(buyerId: String): List<BuyerPickupRouteResponseDto> {
        val routes = pickupRouteRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId)
        return routes.map { route ->
            val waypoints = routeWaypointRepository.findByRouteIdOrderBySequenceNumber(route.id)
            val farmerIds = waypoints.mapNotNull { it.farmerId }
            val farmerOrders = getFarmerOrders(buyerId, farmerIds)
            mapToRouteResponse(route, waypoints, farmerOrders)
        }
    }

    fun getRouteDetails(routeId: String): PickupRouteDetailsDto {
        val route = pickupRouteRepository.findById(routeId)
            .orElseThrow { IllegalArgumentException("Route not found") }
        
        val waypoints = routeWaypointRepository.findByRouteIdOrderBySequenceNumber(routeId)
        val farmerIds = waypoints.map { it.farmerId }.filterNotNull()
        val farmers = farmerRepository.findAllById(farmerIds)
        val farmerOrders = getFarmerOrders(route.buyerId, farmerIds)
        
        val routeResponse = mapToRouteResponse(route, waypoints, farmerOrders)
        
        val farmerDetails = farmers.map { farmer ->
            val orders = farmerOrders[farmer.id] ?: emptyList<ListingOrder>()
            FarmerPickupDetailsDto(
                farmer = FarmerSummaryDto(
                    farmerId = farmer.id,
                    firstName = farmer.userProfile?.fullName,
                    lastName = "",
                    location = (farmer.location?.customName ?: "Location not specified") as String,
                    phoneNumber = farmer.userProfile.phoneNumber,
                    email = farmer.userProfile.email
                ),
                orders = orders.map { order ->
                    PickupOrderDto(
                        orderId = order.id,
                        produceName = order.produceListing.farmerProduce.predictedSpecies ?: "Unknown",
                        quantity = order.quantity,
                        unit = order.produceListing.unit,
                        estimatedWeight = order.quantity * 1.0, // Simplified weight calculation
                        packagingRequirements = null
                    )
                },
                location = LocationDto(farmer.location?.latitude, farmer.location?.longitude, farmer.location?.customName),
                contactInfo = ContactInfoDto(
                    phoneNumber = farmer.userProfile.phoneNumber,
                    alternatePhone = null,
                    email = farmer.userProfile.email
                ),
                accessInstructions = null,
                availableTimeWindows = listOf(
                    TimeWindowDto("08:00", "17:00", null)
                )
            )
        }
        
        val routeStatistics = RouteStatisticsDto(
            totalFarmers = farmers.size,
            totalOrders = farmerOrders.values.sumOf { it.size },
            totalWeight = farmerOrders.values.flatten().sumOf { it.quantity },
            totalValue = farmerOrders.values.flatten().sumOf { it.quantity * it.produceListing.price.price },
            fuelCost = route.totalCost * 0.3, // Simplified fuel cost
            estimatedFuelConsumption = route.totalDistance * 0.08, // L/km
            carbonFootprint = route.totalDistance * 0.2 // kg CO2
        )
        
        return PickupRouteDetailsDto(
            route = routeResponse,
            farmerDetails = farmerDetails,
            routeStatistics = routeStatistics,
            weatherForecast = null // Would integrate with weather API
        )
    }

    fun confirmRoute(routeId: String): BuyerPickupRouteResponseDto {
        val route = pickupRouteRepository.findById(routeId)
            .orElseThrow { IllegalArgumentException("Route not found") }
        route.status = "CONFIRMED"
        route.confirmedAt = LocalDateTime.now()
        val savedRoute = pickupRouteRepository.save(route)
        val waypoints = routeWaypointRepository.findByRouteIdOrderBySequenceNumber(routeId)
        val farmerIds = waypoints.mapNotNull { it.farmerId }
        val farmerOrders = getFarmerOrders(route.buyerId, farmerIds)
        return mapToRouteResponse(savedRoute, waypoints, farmerOrders)
    }

    fun optimizeRoute(routeId: String, request: OptimizeRouteRequestDto): BuyerPickupRouteResponseDto {
        val route = pickupRouteRepository.findById(routeId)
            .orElseThrow { IllegalArgumentException("Route not found") }
        val waypoints = routeWaypointRepository.findByRouteIdOrderBySequenceNumber(routeId)
        val farmers = farmerRepository.findAllById(waypoints.mapNotNull { it.farmerId })
        val farmerOrders = getFarmerOrders(route.buyerId, waypoints.mapNotNull { it.farmerId })
        // Re-optimize the route based on new preferences
        val optimizedWaypoints = optimizeRoute(farmers, farmerOrders, 
            GenerateRouteRequestDto(
                buyerId = route.buyerId,
                farmerIds = waypoints.mapNotNull { it.farmerId },
                preferredDate = route.pickupDate,
                startTime = route.startTime,
                maxDistance = request.constraints?.maxDistance,
                vehicleCapacity = request.constraints?.vehicleCapacity,
                optimizationPreference = request.optimizationPreference
            )
        )
        // Update route statistics
        route.totalDistance = calculateTotalDistance(optimizedWaypoints)
        route.estimatedDuration = calculateTotalDuration(optimizedWaypoints)
        route.totalCost = calculateRouteCost(route.totalDistance, route.estimatedDuration)
        val savedRoute = pickupRouteRepository.save(route)
        // Delete old waypoints and create new ones
        routeWaypointRepository.deleteByRouteId(routeId)
        val newWaypoints = optimizedWaypoints.mapIndexed { index, waypoint ->
            RouteWaypoint(
                id = UUID.randomUUID().toString(),
                routeId = routeId,
                farmerId = waypoint.farmerId,
                sequenceNumber = index + 1,
                estimatedArrival = waypoint.estimatedArrival,
                estimatedDuration = waypoint.estimatedDuration,
                latitude = waypoint.location.latitude,
                longitude = waypoint.location.longitude,
                specialInstructions = waypoint.specialInstructions
            )
        }
        routeWaypointRepository.saveAll(newWaypoints)
        return mapToRouteResponse(savedRoute, newWaypoints, farmerOrders)
    }

    fun cancelRoute(routeId: String) {
        val route = pickupRouteRepository.findById(routeId)
            .orElseThrow { IllegalArgumentException("Route not found") }
        
        route.status = "CANCELLED"
        pickupRouteRepository.save(route)
    }

    fun getAvailableFarmersForPickup(buyerId: String): List<FarmerAvailabilityDto> {
        val connections = connectionRepository.findByBuyerIdAndStatus(buyerId, ConnectionStatus.ACTIVE)
        
        return connections.mapNotNull { connection ->
            val farmer = farmerRepository.findById(connection.farmerId).orElse(null)
            if (farmer != null) {
                val orders = listingOrderRepository.findAll()
                    .filter { 
                        it.buyerId == buyerId && 
                        it.produceListing.farmerProduce.farmer.id == farmer.id &&
                        it.status.name in listOf("BOOKED_FOR_SUPPLY", "CONFIRMED")
                    }
                
                if (orders.isNotEmpty()) {
                    FarmerAvailabilityDto(
                        farmerId = farmer.id,
                        farmerName = "${farmer.userProfile?.fullName} ${""}",
                        location = LocationDto(farmer.location?.latitude, farmer.location?.longitude, farmer.location?.customName),
                        availableOrders = orders.map { order ->
                            AvailableOrderDto(
                                orderId = order.id,
                                produceName = order.produceListing.farmerProduce.predictedSpecies ?: "Unknown",
                                quantity = order.quantity,
                                unit = order.produceListing.unit,
                                readyDate = LocalDate.now(),
                                expiryDate = null,
                                priority = "MEDIUM"
                            )
                        },
                        estimatedTotalWeight = orders.sumOf { it.quantity },
                        preferredPickupTimes = listOf(
                            TimeWindowDto("08:00", "12:00", null),
                            TimeWindowDto("14:00", "17:00", null)
                        ),
                        specialInstructions = null,
                        lastPickupDate = null,
                        reliabilityScore = 85.0 // Would be calculated from historical data
                    )
                } else null
            } else null
        }
    }

    fun notifyFarmersOfPickup(routeId: String): NotificationResponseDto {
        val route = pickupRouteRepository.findById(routeId)
            .orElseThrow { IllegalArgumentException("Route not found") }
        
        val waypoints = routeWaypointRepository.findByRouteIdOrderBySequenceNumber(routeId)
        val farmers = farmerRepository.findAllById(waypoints.map { it.farmerId })
        
        val notifications = farmers.map { farmer ->
            // Simulate notification sending
            val success = Math.random() > 0.1 // 90% success rate
            
            FarmerNotificationDto(
                farmerId = farmer.id,
                farmerName = "${farmer.userProfile?.fullName} ${""}",
                notificationStatus = if (success) "SENT" else "FAILED",
                sentAt = LocalDateTime.now(),
                deliveredAt = if (success) LocalDateTime.now().plusMinutes(1) else null
            )
        }
        
        return NotificationResponseDto(
            routeId = routeId,
            notificationsSent = notifications.count { it.notificationStatus == "SENT" },
            notificationsFailed = notifications.count { it.notificationStatus == "FAILED" },
            farmerNotifications = notifications
        )
    }

    private fun getFarmerOrders(buyerId: String, farmerIds: List<String>): Map<String, List<ListingOrder>> {
        val orders = listingOrderRepository.findAll()
            .filter { 
                it.buyerId == buyerId && 
                it.produceListing.farmerProduce.farmer.id in farmerIds &&
                it.status.name in listOf("BOOKED_FOR_SUPPLY", "CONFIRMED")
            }
        
        return orders.groupBy { it.produceListing.farmerProduce.farmer.id as String }
    }

    private fun optimizeRoute(
        farmers: List<Farmer>, 
        farmerOrders: Map<String, List<ListingOrder>>,
        request: GenerateRouteRequestDto
    ): List<RouteWaypointDto> {
        // Simplified route optimization - in production would use proper routing algorithms
        val waypoints = farmers.mapIndexed { index, farmer ->
            val orders = farmerOrders[farmer.id] ?: emptyList()
            val estimatedDuration = maxOf(15, orders.size * 10) // 15 min minimum, 10 min per order
            
            RouteWaypointDto(
                waypointId = UUID.randomUUID().toString(),
                farmerId = farmer.id,
                farmerName = farmer.userProfile?.fullName,
                location = LocationDto(farmer.location?.latitude, farmer.location?.longitude, farmer.location?.customName),
                estimatedArrival = calculateArrivalTime(request.startTime ?: "08:00", index * 45),
                estimatedDuration = estimatedDuration,
                orderDetails = orders.map { order ->
                    PickupOrderDto(
                        orderId = order.id,
                        produceName = order.produceListing.farmerProduce.predictedSpecies ?: "Unknown",
                        quantity = order.quantity,
                        unit = order.produceListing.unit,
                        estimatedWeight = order.quantity * 1.0,
                        packagingRequirements = null
                    )
                },
                specialInstructions = null,
                sequenceNumber = index + 1
            )
        }
        
        // Sort by optimization preference
        return when (request.optimizationPreference) {
            "DISTANCE" -> waypoints.sortedBy { calculateDistanceFromStart(it.location) }
            "TIME" -> waypoints.sortedBy { it.estimatedDuration }
            else -> waypoints // Default order
        }
    }

    private fun calculateArrivalTime(startTime: String, minutesOffset: Int): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val start = java.time.LocalTime.parse(startTime, formatter)
        val arrival = start.plusMinutes(minutesOffset.toLong())
        return arrival.format(formatter)
    }

    private fun calculateDistanceFromStart(location: LocationDto): Double {
        // Simplified distance calculation - would use actual coordinates
        return Math.random() * 50 // Random distance for demo
    }

    private fun calculateTotalDistance(waypoints: List<RouteWaypointDto>): Double {
        // Simplified calculation - would use actual routing service
        return waypoints.size * 15.0 + Math.random() * 20
    }

    private fun calculateTotalDuration(waypoints: List<RouteWaypointDto>): Int {
        return waypoints.sumOf { it.estimatedDuration } + (waypoints.size - 1) * 30 // 30 min travel between stops
    }

    private fun calculateRouteCost(distance: Double, duration: Int): Double {
        val fuelCost = distance * 0.15 // $0.15 per km
        val timeCost = duration * 0.5 / 60 // $0.50 per minute
        return fuelCost + timeCost
    }

    private fun mapToRouteResponse(
        route: BuyerPickupRoute,
        waypoints: List<RouteWaypoint>,
        farmerOrders: Map<String, List<ListingOrder>>
    ): BuyerPickupRouteResponseDto {
        val waypointDtos = waypoints.map { waypoint ->
            val farmer = farmerRepository.findById(waypoint.farmerId!!)
                .orElseThrow { IllegalArgumentException("Farmer not found") }
            val orders = farmerOrders[waypoint.farmerId] ?: emptyList()
            RouteWaypointDto(
                waypointId = waypoint.id,
                farmerId = waypoint.farmerId,
                farmerName = "${farmer.userProfile?.fullName} ${""}",
                location = LocationDto(waypoint.latitude, waypoint.longitude, farmer.location?.customName),
                estimatedArrival = waypoint.estimatedArrival ?: "08:00",
                estimatedDuration = waypoint.estimatedDuration ?: 0,
                orderDetails = orders.map { order ->
                    PickupOrderDto(
                        orderId = order.id,
                        produceName = order.produceListing.farmerProduce.predictedSpecies ?: "Unknown",
                        quantity = order.quantity,
                        unit = order.produceListing.unit,
                        estimatedWeight = order.quantity * 1.0,
                        packagingRequirements = null
                    )
                },
                specialInstructions = waypoint.specialInstructions,
                sequenceNumber = waypoint.sequenceNumber
            )
        }
        val stops = waypointDtos.map { waypoint ->
            PickupRouteStopDto(
                stopId = null,
                farmerId = waypoint.farmerId ?: "",
                farmerName = waypoint.farmerName,
                sequenceOrder = waypoint.sequenceNumber,
                status = "PENDING",
                arrivalTime = null,
                completionTime = null,
                notes = waypoint.specialInstructions,
                latitude = waypoint.location.latitude,
                longitude = waypoint.location.longitude
            )
        }
        return BuyerPickupRouteResponseDto(
            routeId = route.id,
            zoneId = "", // Provide actual zoneId if available
            exporterId = "", // Provide actual exporterId if available
            zoneSupervisorId = "", // Provide actual zoneSupervisorId if available
            scheduledDate = route.createdAt, // Use createdAt as scheduledDate for now
            status = route.status,
            totalDistanceKm = route.totalDistance,
            estimatedDurationMinutes = route.estimatedDuration,
            stops = stops,
            buyerId = route.buyerId,
            totalCost = route.totalCost,
            pickupDate = route.pickupDate,
            startTime = route.startTime,
            waypoints = waypointDtos,
            routeGeometry = null,
            createdAt = route.createdAt,
            confirmedAt = route.confirmedAt
        )
    }
}

// Note: domain entities PickupRoute and RouteWaypoint are defined under
// com.agriconnect.farmersportalapis.domain.profile. Remove local temp classes.