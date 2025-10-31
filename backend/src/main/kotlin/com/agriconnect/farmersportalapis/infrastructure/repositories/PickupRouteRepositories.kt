package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.profile.PickupRoute
import com.agriconnect.farmersportalapis.domain.profile.PickupRouteStop
import org.springframework.data.jpa.repository.JpaRepository

interface PickupRouteRepository : JpaRepository<PickupRoute, String> {
    fun findByZoneSupervisorId(zoneSupervisorId: String): List<PickupRoute>
    fun findByExporterId(exporterId: String): List<PickupRoute>
    fun findByZoneId(zoneId: String): List<PickupRoute>
    fun findByZoneSupervisorIdAndScheduledDateBetween(zoneSupervisorId: String, start: java.time.LocalDateTime, end: java.time.LocalDateTime): List<PickupRoute>
}

interface PickupRouteStopRepository : JpaRepository<PickupRouteStop, String> {
    fun findByRouteIdOrderBySequenceOrder(routeId: String): List<PickupRouteStop>
    fun deleteByRouteId(routeId: String)
}
