package com.agriconnect.farmersportalapis.repository

import com.agriconnect.farmersportalapis.domain.eudr.ExporterProcessorConnection
import com.agriconnect.farmersportalapis.domain.eudr.ConnectionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ExporterProcessorConnectionRepository : JpaRepository<ExporterProcessorConnection, String> {
    
    @Query("SELECT c FROM ExporterProcessorConnection c WHERE c.exporter.id = :exporterId AND c.status = :status")
    fun findByExporterIdAndStatus(@Param("exporterId") exporterId: String, @Param("status") status: ConnectionStatus): List<ExporterProcessorConnection>
    
    @Query("SELECT c FROM ExporterProcessorConnection c WHERE c.exporter.id = :exporterId")
    fun findByExporterId(@Param("exporterId") exporterId: String): List<ExporterProcessorConnection>
    
    @Query("SELECT c FROM ExporterProcessorConnection c WHERE c.exporter.id = :exporterId AND c.processor.id = :processorId")
    fun findByExporterIdAndProcessorId(@Param("exporterId") exporterId: String, @Param("processorId") processorId: String): ExporterProcessorConnection?
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ExporterProcessorConnection c WHERE c.exporter.id = :exporterId AND c.processor.id = :processorId")
    fun existsByExporterIdAndProcessorId(@Param("exporterId") exporterId: String, @Param("processorId") processorId: String): Boolean
}
