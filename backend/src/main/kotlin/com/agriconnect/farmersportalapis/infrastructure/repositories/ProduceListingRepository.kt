package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.listing.ProduceListing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProduceListingRepository: JpaRepository<ProduceListing, String> {
    @Query("SELECT p FROM ProduceListing p where p.farmerProduce.farmer.id = :farmerId")
    fun getFarmerListings(@Param("farmerId") farmerId: String, pageable: Pageable): Page<ProduceListing>

    @Query("SELECT p FROM ProduceListing p where p.farmerProduce.farmer.id = :farmerId AND p.status = 'ACTIVE'")
    fun findByFarmerId(farmerId: String): List<ProduceListing>

    @Query("SELECT p.price.price FROM ProduceListing p WHERE p.farmerProduce.farmProduce.name = :produce ORDER BY p.createdAt DESC LIMIT 1")
    fun findLatestPriceByProduce(produce: String): Double?
}