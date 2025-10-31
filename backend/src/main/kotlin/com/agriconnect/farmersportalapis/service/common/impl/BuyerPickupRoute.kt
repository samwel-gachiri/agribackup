package com.agriconnect.farmersportalapis.service.common.impl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "buyer_pickup_routes")
class BuyerPickupRoute(
    @Id
    @Column(name = "route_id", length = 36)
    val id: String,

    @Column(name = "buyer_id", nullable = false, length = 36)
    val buyerId: String,

    @Column(name = "status", nullable = false)
    var status: String,

    @Column(name = "total_distance")
    var totalDistance: Double,

    @Column(name = "estimated_duration")
    var estimatedDuration: Int,

    @Column(name = "total_cost")
    var totalCost: Double,

    @Column(name = "pickup_date")
    var pickupDate: java.time.LocalDate?,

    @Column(name = "start_time")
    var startTime: String?,

    @Column(name = "created_at", nullable = false)
    var createdAt: java.time.LocalDateTime,

    @Column(name = "confirmed_at")
    var confirmedAt: java.time.LocalDateTime? = null
)

@Entity
@Table(name = "buyer_route_waypoints")
class RouteWaypoint(
    @Id
    @Column(name = "waypoint_id", length = 36)
    val id: String,

    @Column(name = "route_id", nullable = false, length = 36)
    val routeId: String,

    @Column(name = "farmer_id", length = 36)
    val farmerId: String?,

    @Column(name = "sequence_number", nullable = false)
    val sequenceNumber: Int,

    @Column(name = "estimated_arrival")
    val estimatedArrival: String?,

    @Column(name = "estimated_duration")
    val estimatedDuration: Int?,

    @Column(name = "latitude")
    val latitude: Double?,

    @Column(name = "longitude")
    val longitude: Double?,

    @Column(name = "special_instructions")
    val specialInstructions: String?
)
