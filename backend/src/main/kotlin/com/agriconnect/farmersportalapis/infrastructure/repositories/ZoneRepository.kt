package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.profile.Zone
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface ZoneRepository : JpaRepository<Zone, String> {
    
    fun findByExporterId(exporterId: String): List<Zone>
    
    fun findByName(name: String): Zone?
    
    fun findByNameAndExporterId(name: String, exporterId: String): Zone?
    
    @Query("SELECT z FROM Zone z WHERE z.creator.id = :creatorId")
    fun findByCreatorId(@Param("creatorId") creatorId: String): List<Zone>
    
    @Query("SELECT z FROM Zone z JOIN z.supervisors zs WHERE zs.id = :supervisorId")
    fun findBySupervisorId(@Param("supervisorId") supervisorId: String): List<Zone>
    
    @Query("SELECT z FROM Zone z WHERE z.exporter.id = :exporterId AND z.produceType = :produceType")
    fun findByExporterIdAndProduceType(@Param("exporterId") exporterId: String, @Param("produceType") produceType: String): List<Zone>
}