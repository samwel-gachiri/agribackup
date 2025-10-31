package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.eudr.CountryRisk
import com.agriconnect.farmersportalapis.domain.eudr.CountryRiskLevel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CountryRiskRepository : JpaRepository<CountryRisk, String> {
    
    fun findByCountryCode(countryCode: String): CountryRisk?
    
    fun findByRiskLevel(riskLevel: CountryRiskLevel): List<CountryRisk>
    
    @Query("SELECT c FROM CountryRisk c WHERE c.countryName LIKE %:name%")
    fun findByCountryNameContaining(@Param("name") name: String): List<CountryRisk>
    
    @Query("SELECT c FROM CountryRisk c ORDER BY c.lastUpdated DESC")
    fun findAllOrderByLastUpdatedDesc(): List<CountryRisk>
    
    fun existsByCountryCode(countryCode: String): Boolean
}