package com.agriconnect.farmersportalapis.repository

import com.agriconnect.farmersportalapis.domain.supplychain.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SupplyChainWorkflowRepository : JpaRepository<SupplyChainWorkflow, String> {
    
    fun findByExporterId(exporterId: String, pageable: Pageable): Page<SupplyChainWorkflow>
    
    fun findByExporterIdAndStatus(exporterId: String, status: WorkflowStatus, pageable: Pageable): Page<SupplyChainWorkflow>
    
    @Query("SELECT w FROM SupplyChainWorkflow w WHERE w.exporter.id = :exporterId AND w.status = :status")
    fun findActiveWorkflowsByExporter(@Param("exporterId") exporterId: String, @Param("status") status: WorkflowStatus): List<SupplyChainWorkflow>
}

@Repository
interface WorkflowCollectionEventRepository : JpaRepository<WorkflowCollectionEvent, String> {
    
    fun findByWorkflowId(workflowId: String): List<WorkflowCollectionEvent>
    
    @Query("SELECT SUM(e.quantityCollectedKg) FROM WorkflowCollectionEvent e WHERE e.workflow.id = :workflowId")
    fun getTotalCollectedQuantity(@Param("workflowId") workflowId: String): java.math.BigDecimal?
    
    @Query("SELECT e FROM WorkflowCollectionEvent e WHERE e.workflow.id = :workflowId AND e.aggregator.id = :aggregatorId")
    fun findByWorkflowAndAggregator(@Param("workflowId") workflowId: String, @Param("aggregatorId") aggregatorId: String): List<WorkflowCollectionEvent>
}

@Repository
interface WorkflowConsolidationEventRepository : JpaRepository<WorkflowConsolidationEvent, String> {
    
    fun findByWorkflowId(workflowId: String): List<WorkflowConsolidationEvent>
    
    @Query("SELECT SUM(e.quantitySentKg) FROM WorkflowConsolidationEvent e WHERE e.workflow.id = :workflowId")
    fun getTotalConsolidatedQuantity(@Param("workflowId") workflowId: String): java.math.BigDecimal?
    
    @Query("SELECT SUM(e.quantitySentKg) FROM WorkflowConsolidationEvent e WHERE e.workflow.id = :workflowId AND e.aggregator.id = :aggregatorId")
    fun getTotalSentByAggregator(@Param("workflowId") workflowId: String, @Param("aggregatorId") aggregatorId: String): java.math.BigDecimal?
}

@Repository
interface WorkflowProcessingEventRepository : JpaRepository<WorkflowProcessingEvent, String> {
    
    fun findByWorkflowId(workflowId: String): List<WorkflowProcessingEvent>
    
    @Query("SELECT SUM(e.quantityProcessedKg) FROM WorkflowProcessingEvent e WHERE e.workflow.id = :workflowId")
    fun getTotalProcessedQuantity(@Param("workflowId") workflowId: String): java.math.BigDecimal?
}

@Repository
interface WorkflowShipmentEventRepository : JpaRepository<WorkflowShipmentEvent, String> {
    
    fun findByWorkflowId(workflowId: String): List<WorkflowShipmentEvent>
    
    @Query("SELECT SUM(e.quantityShippedKg) FROM WorkflowShipmentEvent e WHERE e.workflow.id = :workflowId")
    fun getTotalShippedQuantity(@Param("workflowId") workflowId: String): java.math.BigDecimal?
}
