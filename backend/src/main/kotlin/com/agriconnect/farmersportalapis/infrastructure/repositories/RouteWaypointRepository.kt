package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.service.common.impl.RouteWaypoint
import org.springframework.data.jpa.repository.JpaRepository

interface RouteWaypointRepository : JpaRepository<RouteWaypoint, String> {
    fun findByRouteIdOrderBySequenceNumber(routeId: String): List<RouteWaypoint>
    fun deleteByRouteId(routeId: String)
}
