package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.eudr.BatchStatus
import com.agriconnect.farmersportalapis.domain.eudr.CountryRiskLevel
import com.agriconnect.farmersportalapis.domain.eudr.EudrBatch
import com.agriconnect.farmersportalapis.domain.eudr.RiskLevel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface EudrBatchRepository : JpaRepository<EudrBatch, String> {
    
    fun findByBatchCode(batchCode: String): EudrBatch?
    
    fun findByCreatedBy(createdBy: String): List<EudrBatch>
    
    fun findByStatus(status: BatchStatus): List<EudrBatch>
    
    fun findByCountryOfProduction(countryOfProduction: String): List<EudrBatch>
    
    fun findByCountryRiskLevel(countryRiskLevel: CountryRiskLevel): List<EudrBatch>
    
    fun findByRiskLevel(riskLevel: RiskLevel): List<EudrBatch>
    
    @Query("SELECT b FROM EudrBatch b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    fun findByCreatedAtBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<EudrBatch>
    
    @Query("SELECT b FROM EudrBatch b WHERE b.harvestDate BETWEEN :startDate AND :endDate")
    fun findByHarvestDateBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<EudrBatch>
    
    @Query("SELECT b FROM EudrBatch b WHERE b.riskLevel IN :riskLevels")
    fun findByRiskLevelIn(@Param("riskLevels") riskLevels: List<RiskLevel>): List<EudrBatch>
    
    fun existsByBatchCode(batchCode: String): Boolean
    
    @Query("SELECT b FROM EudrBatch b WHERE b.id IN :batchIds AND b.createdAt BETWEEN :startDate AND :endDate")
    fun findByIdsAndDateRange(
        @Param("batchIds") batchIds: List<String>,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<EudrBatch>
}