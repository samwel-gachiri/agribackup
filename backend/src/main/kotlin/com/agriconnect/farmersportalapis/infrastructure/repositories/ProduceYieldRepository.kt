package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.profile.ProduceYield
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProduceYieldRepository : JpaRepository<ProduceYield, String> {
    fun findByFarmerProduceId(farmerProduceId: String): List<ProduceYield>

    @Query("SELECT py FROM ProduceYield py WHERE py.farmerProduce.id IN :producesId")
    fun findByFarmerProduceIdIn(producesId: List<String>): List<ProduceYield>
}
