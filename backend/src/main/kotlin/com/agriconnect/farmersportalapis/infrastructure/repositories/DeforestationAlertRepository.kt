package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.eudr.DeforestationAlert
import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface DeforestationAlertRepository : JpaRepository<DeforestationAlert, String> {
    
    fun findByProductionUnitId(productionUnitId: String): List<DeforestationAlert>
    
    fun findByProductionUnit(productionUnit: ProductionUnit): List<DeforestationAlert>
    
    fun findBySourceIdAndProductionUnit(sourceId: String, productionUnit: ProductionUnit): DeforestationAlert?
    
    @Query("""
        SELECT da FROM DeforestationAlert da 
        WHERE da.productionUnit.id = :productionUnitId 
        AND da.alertDate BETWEEN :startDate AND :endDate
        AND (:severity IS NULL OR da.severity = :severity)
        ORDER BY da.alertDate DESC
    """)
    fun findByProductionUnitIdAndFilters(
        @Param("productionUnitId") productionUnitId: String,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
        @Param("severity") severity: DeforestationAlert.Severity?
    ): List<DeforestationAlert>
    
    @Query("""
        SELECT da FROM DeforestationAlert da 
        WHERE da.severity = :severity 
        AND da.alertDate >= :since
        ORDER BY da.alertDate DESC
    """)
    fun findBySeveritySince(
        @Param("severity") severity: DeforestationAlert.Severity,
        @Param("since") since: LocalDateTime
    ): List<DeforestationAlert>
    
    @Query("""
        SELECT da FROM DeforestationAlert da 
        WHERE da.alertType = :alertType 
        AND da.alertDate BETWEEN :startDate AND :endDate
        ORDER BY da.alertDate DESC
    """)
    fun findByAlertTypeAndDateRange(
        @Param("alertType") alertType: DeforestationAlert.AlertType,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<DeforestationAlert>
    
    @Query("""
        SELECT COUNT(da) FROM DeforestationAlert da 
        WHERE da.productionUnit.id = :productionUnitId 
        AND da.severity = :severity
    """)
    fun countByProductionUnitIdAndSeverity(
        @Param("productionUnitId") productionUnitId: String,
        @Param("severity") severity: DeforestationAlert.Severity
    ): Long
    
    @Query("""
        SELECT da FROM DeforestationAlert da 
        WHERE da.productionUnit.farmer.id = :farmerId
        AND da.alertDate >= :since
        ORDER BY da.alertDate DESC
    """)
    fun findByFarmerIdSince(
        @Param("farmerId") farmerId: String,
        @Param("since") since: LocalDateTime
    ): List<DeforestationAlert>
    
    @Query("""
        SELECT da FROM DeforestationAlert da 
        WHERE da.hederaTransactionId IS NOT NULL
        AND da.alertDate BETWEEN :startDate AND :endDate
        ORDER BY da.alertDate DESC
    """)
    fun findVerifiedAlertsByDateRange(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<DeforestationAlert>
    
    @Query("""
        SELECT da FROM DeforestationAlert da 
        WHERE da.distanceFromUnit <= :maxDistanceKm
        AND da.alertDate >= :since
        ORDER BY da.alertDate DESC
    """)
    fun findAlertsWithinDistance(
        maxDistanceKm: Double,
        since: LocalDateTime
    ): List<DeforestationAlert>
    
    @Query("""
        SELECT da FROM DeforestationAlert da 
        WHERE da.productionUnit.id IN :productionUnitIds 
        AND da.alertDate BETWEEN :startDate AND :endDate
        ORDER BY da.alertDate DESC
    """)
    fun findByProductionUnitIdsAndDateRange(
        @Param("productionUnitIds") productionUnitIds: List<String>,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<DeforestationAlert>
    
    fun countByProductionUnitIdAndIsReviewedFalse(productionUnitId: String): Long
    
    fun countByIsReviewedFalse(): Long
    
    @Query("""
        SELECT da FROM DeforestationAlert da 
        WHERE da.isReviewed = false
        AND da.severity = :severity
        ORDER BY da.alertDate DESC
    """)
    fun findUnreviewedBySeverity(
        @Param("severity") severity: DeforestationAlert.Severity
    ): List<DeforestationAlert>
    
    @Query("""
        SELECT DATE(da.alertDate) as alertDate, COUNT(da) as alertCount
        FROM DeforestationAlert da 
        WHERE da.alertDate BETWEEN :startDate AND :endDate
        GROUP BY DATE(da.alertDate)
        ORDER BY DATE(da.alertDate)
    """)
    fun getDailyAlertCounts(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<Array<Any>>
}