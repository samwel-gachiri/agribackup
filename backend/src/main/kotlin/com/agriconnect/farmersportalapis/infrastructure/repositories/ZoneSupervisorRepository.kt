package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.profile.ZoneSupervisor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ZoneSupervisorRepository : JpaRepository<ZoneSupervisor, String> {
    
    fun findByUserProfileId(userId: String): ZoneSupervisor?
    
    fun findByStatus(status: String): List<ZoneSupervisor>
    
    @Query("SELECT zs FROM ZoneSupervisor zs WHERE zs.userProfile.email = :email")
    fun findByEmail(@Param("email") email: String): ZoneSupervisor?
    
    @Query("SELECT zs FROM ZoneSupervisor zs WHERE zs.userProfile.phoneNumber = :phoneNumber")
    fun findByPhoneNumber(@Param("phoneNumber") phoneNumber: String): ZoneSupervisor?
    
    @Query("SELECT zs FROM ZoneSupervisor zs WHERE zs.status = 'ACTIVE'")
    fun findAllActive(): List<ZoneSupervisor>
    
    @Query("SELECT zs FROM ZoneSupervisor zs JOIN zs.zones z WHERE z.id = :zoneId AND zs.status = 'ACTIVE'")
    fun findActiveByZoneId(@Param("zoneId") zoneId: String): List<ZoneSupervisor>
    
    @Query("SELECT zs FROM ZoneSupervisor zs JOIN zs.zones z WHERE z.exporter.id = :exporterId AND zs.status = 'ACTIVE'")
    fun findActiveByExporterId(@Param("exporterId") exporterId: String): List<ZoneSupervisor>
}