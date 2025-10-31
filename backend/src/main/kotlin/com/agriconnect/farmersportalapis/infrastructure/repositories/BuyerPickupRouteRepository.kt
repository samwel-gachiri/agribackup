package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.service.common.impl.BuyerPickupRoute
import org.springframework.data.jpa.repository.JpaRepository

interface BuyerPickupRouteRepository : JpaRepository<BuyerPickupRoute, String> {
    fun findByBuyerIdOrderByCreatedAtDesc(buyerId: String): List<BuyerPickupRoute>
    fun findByBuyerIdAndStatus(buyerId: String, status: String): List<BuyerPickupRoute>
}