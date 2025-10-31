package com.agriconnect.farmersportalapis.repository

import com.agriconnect.farmersportalapis.domain.eudr.ExporterAggregatorConnection
import com.agriconnect.farmersportalapis.domain.eudr.ConnectionStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ExporterAggregatorConnectionRepository : JpaRepository<ExporterAggregatorConnection, String> {
    
    @Query("SELECT c FROM ExporterAggregatorConnection c WHERE c.exporter.id = :exporterId AND c.status = :status")
    fun findByExporterIdAndStatus(@Param("exporterId") exporterId: String, @Param("status") status: ConnectionStatus): List<ExporterAggregatorConnection>
    
    @Query("SELECT c FROM ExporterAggregatorConnection c WHERE c.exporter.id = :exporterId")
    fun findByExporterId(@Param("exporterId") exporterId: String): List<ExporterAggregatorConnection>
    
    @Query("SELECT c FROM ExporterAggregatorConnection c WHERE c.exporter.id = :exporterId AND c.aggregator.id = :aggregatorId")
    fun findByExporterIdAndAggregatorId(@Param("exporterId") exporterId: String, @Param("aggregatorId") aggregatorId: String): ExporterAggregatorConnection?
    
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM ExporterAggregatorConnection c WHERE c.exporter.id = :exporterId AND c.aggregator.id = :aggregatorId")
    fun existsByExporterIdAndAggregatorId(@Param("exporterId") exporterId: String, @Param("aggregatorId") aggregatorId: String): Boolean
}
