package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.common.enums.ConnectionStatus
import com.agriconnect.farmersportalapis.domain.common.model.FarmerBuyerConnection
import org.springframework.data.jpa.repository.JpaRepository

interface ConnectionRepository: JpaRepository<FarmerBuyerConnection, String> {
    fun findByFarmerId(farmerId: String): List<FarmerBuyerConnection>
    fun findByBuyerId(buyerId: String): List<FarmerBuyerConnection>
    fun findByFarmerIdAndBuyerId(farmerId: String, buyerId: String): FarmerBuyerConnection?
    fun findByBuyerIdAndStatus(buyerId: String, status: ConnectionStatus): List<FarmerBuyerConnection>
}