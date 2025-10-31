package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.eudr.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository for MitigationWorkflow entities
 */
@Repository
interface MitigationWorkflowRepository : JpaRepository<MitigationWorkflow, String> {

    /**
     * Find all workflows for a specific batch
     */
    fun findByBatchId(batchId: String): List<MitigationWorkflow>

    /**
     * Find workflows by status
     */
    fun findByStatus(status: MitigationStatus): List<MitigationWorkflow>

    /**
     * Find workflows by risk level
     */
    fun findByRiskLevel(riskLevel: RiskLevel): List<MitigationWorkflow>

    /**
     * Find workflows by creator
     */
    fun findByCreatedBy(createdBy: String): List<MitigationWorkflow>

    /**
     * Find pending or in-progress workflows
     */
    @Query("SELECT w FROM MitigationWorkflow w WHERE w.status IN ('PENDING', 'IN_PROGRESS') ORDER BY w.createdAt DESC")
    fun findActiveWorkflows(): List<MitigationWorkflow>

    /**
     * Find workflows by batch and status
     */
    fun findByBatchIdAndStatus(batchId: String, status: MitigationStatus): List<MitigationWorkflow>

    /**
     * Count workflows by status
     */
    fun countByStatus(status: MitigationStatus): Long

    /**
     * Count workflows by risk level
     */
    fun countByRiskLevel(riskLevel: RiskLevel): Long

    /**
     * Find workflows with high risk that are still pending
     */
    @Query("SELECT w FROM MitigationWorkflow w WHERE w.riskLevel = 'HIGH' AND w.status = 'PENDING' ORDER BY w.createdAt ASC")
    fun findHighRiskPendingWorkflows(): List<MitigationWorkflow>
}

/**
 * Repository for MitigationAction entities
 */
@Repository
interface MitigationActionRepository : JpaRepository<MitigationAction, String> {

    /**
     * Find all actions for a specific workflow
     */
    fun findByWorkflowId(workflowId: String): List<MitigationAction>

    /**
     * Find actions by status
     */
    fun findByStatus(status: MitigationActionStatus): List<MitigationAction>

    /**
     * Find actions assigned to a specific user
     */
    fun findByAssignedTo(assignedTo: String): List<MitigationAction>

    /**
     * Find actions by type
     */
    fun findByActionType(actionType: MitigationActionType): List<MitigationAction>

    /**
     * Find pending actions assigned to a user
     */
    fun findByAssignedToAndStatus(assignedTo: String, status: MitigationActionStatus): List<MitigationAction>

    /**
     * Find overdue actions (past due date and not completed)
     */
    @Query("""
        SELECT a FROM MitigationAction a 
        WHERE a.dueDate < CURRENT_DATE 
        AND a.status NOT IN ('COMPLETED', 'REJECTED')
        ORDER BY a.dueDate ASC
    """)
    fun findOverdueActions(): List<MitigationAction>

    /**
     * Count actions by status for a workflow
     */
    @Query("SELECT COUNT(a) FROM MitigationAction a WHERE a.workflow.id = :workflowId AND a.status = :status")
    fun countByWorkflowIdAndStatus(
        @Param("workflowId") workflowId: String,
        @Param("status") status: MitigationActionStatus
    ): Long

    /**
     * Find actions with upcoming due dates (within specified days)
     */
    @Query("""
        SELECT a FROM MitigationAction a 
        WHERE a.dueDate >= CURRENT_DATE 
        AND a.dueDate <= CURRENT_DATE + :days DAY
        AND a.status NOT IN ('COMPLETED', 'REJECTED')
        ORDER BY a.dueDate ASC
    """)
    fun findActionsWithUpcomingDueDate(@Param("days") days: Int): List<MitigationAction>
}
