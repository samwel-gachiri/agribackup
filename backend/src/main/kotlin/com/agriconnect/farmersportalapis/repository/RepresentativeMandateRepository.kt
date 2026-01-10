package com.agriconnect.farmersportalapis.repository

import com.agriconnect.farmersportalapis.domain.eudr.MandateStatus
import com.agriconnect.farmersportalapis.domain.eudr.RepresentativeMandate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface RepresentativeMandateRepository : JpaRepository<RepresentativeMandate, String> {
    
    /**
     * Find all mandates for an exporter
     */
    fun findByExporterId(exporterId: String): List<RepresentativeMandate>
    
    /**
     * Find all mandates for an AR
     */
    fun findByRepresentativeId(representativeId: String): List<RepresentativeMandate>
    
    /**
     * Find mandate by exporter and status
     */
    fun findByExporterIdAndStatus(exporterId: String, status: MandateStatus): RepresentativeMandate?
    
    /**
     * Find all mandates for an AR with a specific status
     */
    fun findByRepresentativeIdAndStatus(representativeId: String, status: MandateStatus): List<RepresentativeMandate>
    
    /**
     * Find the active mandate for an exporter (there should only be one)
     */
    fun findByExporterIdAndStatusAndValidToAfterOrValidToIsNull(
        exporterId: String, 
        status: MandateStatus,
        today: LocalDate
    ): RepresentativeMandate?
    
    /**
     * Find the currently valid mandate for an exporter
     */
    @Query("""
        SELECT m FROM RepresentativeMandate m 
        WHERE m.exporter.id = :exporterId 
        AND m.status = 'ACTIVE'
        AND m.validFrom <= :today
        AND (m.validTo IS NULL OR m.validTo >= :today)
    """)
    fun findActiveValidMandateForExporter(
        @Param("exporterId") exporterId: String,
        @Param("today") today: LocalDate = LocalDate.now()
    ): RepresentativeMandate?
    
    /**
     * Find pending mandates for an AR (invites they need to respond to)
     */
    fun findByRepresentativeIdAndStatusOrderByCreatedAtDesc(
        representativeId: String, 
        status: MandateStatus
    ): List<RepresentativeMandate>
    
    /**
     * Find pending mandates for an exporter (AR offers they need to respond to)
     */
    fun findByExporterIdAndStatusOrderByCreatedAtDesc(
        exporterId: String, 
        status: MandateStatus
    ): List<RepresentativeMandate>
    
    /**
     * Check if exporter already has an active mandate
     */
    @Query("""
        SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END 
        FROM RepresentativeMandate m 
        WHERE m.exporter.id = :exporterId 
        AND m.status = 'ACTIVE'
    """)
    fun exporterHasActiveMandate(@Param("exporterId") exporterId: String): Boolean
    
    /**
     * Find all mandates expiring within a certain number of days
     */
    @Query("""
        SELECT m FROM RepresentativeMandate m 
        WHERE m.status = 'ACTIVE' 
        AND m.validTo IS NOT NULL 
        AND m.validTo BETWEEN :today AND :expirationThreshold
    """)
    fun findMandatesExpiringSoon(
        @Param("today") today: LocalDate,
        @Param("expirationThreshold") expirationThreshold: LocalDate
    ): List<RepresentativeMandate>
    
    /**
     * Find all expired mandates that are still marked as active
     */
    @Query("""
        SELECT m FROM RepresentativeMandate m 
        WHERE m.status = 'ACTIVE' 
        AND m.validTo IS NOT NULL 
        AND m.validTo < :today
    """)
    fun findExpiredMandates(@Param("today") today: LocalDate): List<RepresentativeMandate>
    
    /**
     * Count active mandates for an AR (for capacity management)
     */
    fun countByRepresentativeIdAndStatus(representativeId: String, status: MandateStatus): Long
}
