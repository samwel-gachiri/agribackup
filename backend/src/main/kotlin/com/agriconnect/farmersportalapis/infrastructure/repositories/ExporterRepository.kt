package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.application.dtos.FarmerInZoneResponseDto
import com.agriconnect.farmersportalapis.domain.common.enums.ExporterVerificationStatus
import com.agriconnect.farmersportalapis.domain.profile.Exporter
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExporterRepository : JpaRepository<Exporter, String> {
    // In ExporterRepository
    @Query("SELECT e FROM Exporter e JOIN FETCH e.userProfile WHERE e.userProfile.id = :userId")
    fun findByUserProfile(@Param("userId") userId: String): Optional<Exporter>

    fun existsByLicenseId(licenseId: String): Boolean

    fun findByVerificationStatus(verificationStatus: ExporterVerificationStatus): List<Exporter>
    @Query("""
        SELECT DISTINCT f 
        FROM Farmer f 
        JOIN FarmerExporterRelationship fer 
        ON f.id = fer.farmer.id 
        WHERE fer.zone.id = :zoneId
    """)
    fun findFarmersByZoneId(zoneId: String): List<Farmer>

    // Alternative approach with more detailed information
    @Query(
        """
        SELECT new com.agriconnect.farmersportalapis.application.dtos.FarmerInZoneResponseDto(
            f.id,
            f.userProfile.fullName,
            f.farmSize,
            f.farmName,
            f.location,
            fer.createdAt
        )
        FROM Farmer f 
        JOIN FarmerExporterRelationship fer 
        ON f.id = fer.farmer.id 
        WHERE fer.zone.id = :zoneId
    """
    )
    fun findFarmersInZoneWithDetails(zoneId: String): List<FarmerInZoneResponseDto>

    // Optional: Add pagination support
    @Query(
        """
        SELECT new com.agriconnect.farmersportalapis.application.dtos.FarmerInZoneResponseDto(
            f.id,
            f.userProfile.fullName,
            f.farmSize,
            f.farmName,
            f.location,
            fer.createdAt
        )
        FROM Farmer f 
        JOIN FarmerExporterRelationship fer 
        ON f.id = fer.farmer.id 
        WHERE fer.zone.id = :zoneId
    """
    )
    fun findFarmersInZoneWithDetails(zoneId: String, pageable: Pageable): Page<FarmerInZoneResponseDto>

//    @Query("SELECT e FROM Exporter e WHERE e.id = :idOrUid OR e.uid = :idOrUid")
//    fun findByIdOrUid(@Param("idOrUid") idOrUid: String): Exporter?


}

// Duplicated repository interfaces removed (see dedicated files in infrastructure.repositories package)