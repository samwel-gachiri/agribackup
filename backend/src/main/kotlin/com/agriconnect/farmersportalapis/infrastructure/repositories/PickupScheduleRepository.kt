package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.profile.PickupSchedule
import com.agriconnect.farmersportalapis.domain.common.enums.PickupStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PickupScheduleRepository : JpaRepository<PickupSchedule, String> {
    
    fun findByFarmerId(farmerId: String): List<PickupSchedule>
    
    fun findByExporterId(exporterId: String): List<PickupSchedule>
    
    fun findByStatus(status: PickupStatus): List<PickupSchedule>
    
    fun findByScheduledDateBetween(startDate: LocalDateTime, endDate: LocalDateTime): List<PickupSchedule>
    
    @Query("SELECT ps FROM PickupSchedule ps WHERE ps.exporter.id = :exporterId AND ps.status = :status")
    fun findByExporterIdAndStatus(@Param("exporterId") exporterId: String, @Param("status") status: PickupStatus): List<PickupSchedule>
    
    @Query("SELECT ps FROM PickupSchedule ps WHERE ps.farmer.id = :farmerId AND ps.scheduledDate >= :fromDate")
    fun findUpcomingByFarmerId(@Param("farmerId") farmerId: String, @Param("fromDate") fromDate: LocalDateTime): List<PickupSchedule>
}