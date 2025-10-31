package com.agriconnect.farmersportalapis.repository

import com.agriconnect.farmersportalapis.domain.eudr.ExporterImporterConnection
import com.agriconnect.farmersportalapis.domain.eudr.ConnectionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ExporterImporterConnectionRepository : JpaRepository<ExporterImporterConnection, String> {
    
    @Query("SELECT c FROM ExporterImporterConnection c WHERE c.exporter.id = :exporterId AND c.status = :status")
    fun findByExporterIdAndStatus(@Param("exporterId") exporterId: String, @Param("status") status: ConnectionStatus): List<ExporterImporterConnection>
    
    @Query("SELECT c FROM ExporterImporterConnection c WHERE c.exporter.id = :exporterId")
    fun findByExporterId(@Param("exporterId") exporterId: String): List<ExporterImporterConnection>
    
    @Query("SELECT c FROM ExporterImporterConnection c WHERE c.exporter.id = :exporterId AND c.importer.id = :importerId")
    fun findByExporterIdAndImporterId(@Param("exporterId") exporterId: String, @Param("importerId") importerId: String): ExporterImporterConnection?
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ExporterImporterConnection c WHERE c.exporter.id = :exporterId AND c.importer.id = :importerId")
    fun existsByExporterIdAndImporterId(@Param("exporterId") exporterId: String, @Param("importerId") importerId: String): Boolean
}
