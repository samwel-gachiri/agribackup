package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.profile.FarmerExporterRelationship
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FarmerExporterRelationshipRepository : JpaRepository<FarmerExporterRelationship, String> {
    
    fun findByFarmerIdAndExporterId(farmerId: String, exporterId: String): FarmerExporterRelationship?
    
    fun findByFarmerId(farmerId: String): List<FarmerExporterRelationship>
    
    fun findByExporterId(exporterId: String): List<FarmerExporterRelationship>
    
    fun findByZoneId(zoneId: String): List<FarmerExporterRelationship>
    
    @Query("SELECT fer FROM FarmerExporterRelationship fer WHERE fer.zone.id = :zoneId AND fer.exporter.id = :exporterId")
    fun findByZoneIdAndExporterId(@Param("zoneId") zoneId: String, @Param("exporterId") exporterId: String): List<FarmerExporterRelationship>
    
    @Query("SELECT COUNT(fer) FROM FarmerExporterRelationship fer WHERE fer.zone.id = :zoneId")
    fun countFarmersByZoneId(@Param("zoneId") zoneId: String): Long
}