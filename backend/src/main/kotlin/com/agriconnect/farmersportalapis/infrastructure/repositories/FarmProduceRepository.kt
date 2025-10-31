package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.common.model.FarmProduce
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FarmProduceRepository: JpaRepository<FarmProduce, String> {
    @Query("SELECT f FROM FarmProduce f WHERE f.status = 'ACTIVE'")
    fun findAllActiveProduces(): List<FarmProduce>

    fun findByNameLikeIgnoreCase(name: String): List<FarmProduce>

    @Query("""
        SELECT fp FROM FarmProduce fp 
        WHERE LOWER(fp.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    fun findSimilarByName(@Param("name") name: String): List<FarmProduce>

}