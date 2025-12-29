package com.agriconnect.farmersportalapis.repository

import com.agriconnect.farmersportalapis.domain.supplychain.ConnectionType
import com.agriconnect.farmersportalapis.domain.supplychain.SupplierConnection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SupplierConnectionRepository : JpaRepository<SupplierConnection, String> {
    
    fun findByWorkflowId(workflowId: String): List<SupplierConnection>
    
    fun findByWorkflowId(workflowId: String, pageable: Pageable): Page<SupplierConnection>
    
    fun findByFromSupplierId(supplierId: String): List<SupplierConnection>
    
    fun findByToSupplierId(supplierId: String): List<SupplierConnection>
    
    fun findByConnectionType(connectionType: ConnectionType): List<SupplierConnection>
    
    @Query("SELECT sc FROM SupplierConnection sc WHERE sc.workflow.id = :workflowId ORDER BY sc.createdAt ASC")
    fun findByWorkflowIdOrderByCreatedAtAsc(workflowId: String): List<SupplierConnection>
    
    @Query("SELECT sc FROM SupplierConnection sc WHERE sc.fromSupplier.id = :supplierId OR sc.toSupplier.id = :supplierId")
    fun findAllBySupplierId(supplierId: String): List<SupplierConnection>
    
    @Query("""
        SELECT sc FROM SupplierConnection sc 
        WHERE sc.workflow.id = :workflowId 
        AND sc.fromSupplier.id = :fromSupplierId 
        AND sc.toSupplier.id = :toSupplierId
    """)
    fun findByWorkflowAndSuppliers(workflowId: String, fromSupplierId: String, toSupplierId: String): SupplierConnection?
    
    @Query("""
        SELECT COALESCE(SUM(sc.quantityKg), 0) 
        FROM SupplierConnection sc 
        WHERE sc.workflow.id = :workflowId 
        AND sc.toSupplier.id = :supplierId
    """)
    fun getTotalQuantityReceivedBySupplier(workflowId: String, supplierId: String): java.math.BigDecimal
    
    @Query("""
        SELECT COALESCE(SUM(sc.quantityKg), 0) 
        FROM SupplierConnection sc 
        WHERE sc.workflow.id = :workflowId 
        AND sc.fromSupplier.id = :supplierId
    """)
    fun getTotalQuantitySentBySupplier(workflowId: String, supplierId: String): java.math.BigDecimal
    
    @Query("SELECT COUNT(sc) FROM SupplierConnection sc WHERE sc.workflow.id = :workflowId")
    fun countByWorkflowId(workflowId: String): Long
    
    @Query("SELECT sc FROM SupplierConnection sc WHERE sc.hederaTransactionId IS NOT NULL AND sc.workflow.id = :workflowId")
    fun findVerifiedConnectionsByWorkflow(workflowId: String): List<SupplierConnection>
    
    fun existsByWorkflowIdAndFromSupplierIdAndToSupplierId(
        workflowId: String, 
        fromSupplierId: String, 
        toSupplierId: String
    ): Boolean
}
