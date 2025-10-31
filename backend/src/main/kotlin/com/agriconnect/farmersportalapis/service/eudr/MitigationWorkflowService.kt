package com.agriconnect.farmersportalapis.service.eudr

import com.agriconnect.farmersportalapis.domain.eudr.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import com.agriconnect.farmersportalapis.service.hedera.HederaConsensusServices
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class MitigationWorkflowService(
    private val mitigationWorkflowRepository: MitigationWorkflowRepository,
    private val mitigationActionRepository: MitigationActionRepository,
    private val eudrBatchRepository: EudrBatchRepository,
    private val hederaConsensusService: HederaConsensusServices,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(MitigationWorkflowService::class.java)

    /**
     * Create a new mitigation workflow for a batch
     */
    @Transactional
    fun createWorkflow(
        batchId: String,
        riskLevel: com.agriconnect.farmersportalapis.domain.eudr.RiskLevel,
        createdBy: String,
        initialActions: List<CreateMitigationActionDto> = emptyList()
    ): MitigationWorkflowDto {
        logger.info("Creating mitigation workflow for batch: $batchId with risk level: $riskLevel")

        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        // Create workflow
        val workflow = MitigationWorkflow(
            batch = batch,
            riskLevel = riskLevel,
            status = MitigationStatus.PENDING,
            createdBy = createdBy,
            completedAt = null,
            completionNotes = null,
            hederaTransactionId = null
        )

        val savedWorkflow = mitigationWorkflowRepository.save(workflow)

        // Record on Hedera blockchain
        try {
            val workflowData = mapOf(
                "workflowId" to savedWorkflow.id,
                "batchId" to batchId,
                "batchCode" to batch.batchCode,
                "riskLevel" to riskLevel.name,
                "createdBy" to createdBy,
                "createdAt" to LocalDateTime.now().toString(),
                "status" to MitigationStatus.PENDING.name
            )

            val txId = hederaConsensusService.recordMitigationWorkflowCreation(
                workflowId = savedWorkflow.id,
                batchId = batchId,
                riskLevel = riskLevel.name,
                createdBy = createdBy,
                metadata = objectMapper.writeValueAsString(workflowData)
            )

            savedWorkflow.hederaTransactionId = txId
            mitigationWorkflowRepository.save(savedWorkflow)

            logger.info("Recorded mitigation workflow on Hedera: transaction $txId")
        } catch (e: Exception) {
            logger.error("Failed to record mitigation workflow on Hedera", e)
            // Continue even if Hedera recording fails
        }

        // Add initial actions if provided
        if (initialActions.isNotEmpty()) {
            initialActions.forEach { actionDto ->
                addMitigationAction(savedWorkflow.id, actionDto, createdBy)
            }
        }

        logger.info("Created mitigation workflow: ${savedWorkflow.id}")
        return convertToDto(mitigationWorkflowRepository.findById(savedWorkflow.id).orElseThrow())
    }

    /**
     * Add a mitigation action to a workflow
     */
    @Transactional
    fun addMitigationAction(
        workflowId: String,
        actionDto: CreateMitigationActionDto,
        addedBy: String
    ): MitigationActionDto {
        logger.info("Adding mitigation action to workflow: $workflowId")

        val workflow = mitigationWorkflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found: $workflowId") }

        // Update workflow status to IN_PROGRESS if it was PENDING
        if (workflow.status == MitigationStatus.PENDING) {
            workflow.status = MitigationStatus.IN_PROGRESS
            mitigationWorkflowRepository.save(workflow)
        }

        // Create action
        val action = MitigationAction(
            workflow = workflow,
            actionType = actionDto.actionType,
            description = actionDto.description,
            status = MitigationActionStatus.PENDING,
            assignedTo = actionDto.assignedTo,
            dueDate = actionDto.dueDate,
            completedAt = null,
            completionEvidence = null,
            hederaTransactionId = null
        )

        val savedAction = mitigationActionRepository.save(action)

        // Record on Hedera blockchain
        try {
            val actionData = mapOf(
                "actionId" to savedAction.id,
                "workflowId" to workflowId,
                "batchId" to workflow.batch.id,
                "actionType" to actionDto.actionType.name,
                "description" to actionDto.description,
                "assignedTo" to actionDto.assignedTo,
                "dueDate" to actionDto.dueDate?.toString(),
                "createdAt" to LocalDateTime.now().toString()
            )

            val txId = hederaConsensusService.recordMitigationAction(
                actionId = savedAction.id,
                workflowId = workflowId,
                actionType = actionDto.actionType.name,
                assignedTo = actionDto.assignedTo ?: "UNASSIGNED",
                metadata = objectMapper.writeValueAsString(actionData)
            )

            savedAction.hederaTransactionId = txId
            mitigationActionRepository.save(savedAction)

            logger.info("Recorded mitigation action on Hedera: transaction $txId")
        } catch (e: Exception) {
            logger.error("Failed to record mitigation action on Hedera", e)
            // Continue even if Hedera recording fails
        }

        logger.info("Created mitigation action: ${savedAction.id}")
        return convertActionToDto(savedAction)
    }

    /**
     * Update mitigation action status
     */
    @Transactional
    fun updateActionStatus(
        actionId: String,
        newStatus: MitigationActionStatus,
        completionEvidence: String?,
        updatedBy: String
    ): MitigationActionDto {
        logger.info("Updating mitigation action status: $actionId to $newStatus")

        val action = mitigationActionRepository.findById(actionId)
            .orElseThrow { IllegalArgumentException("Action not found: $actionId") }

        val oldStatus = action.status
        action.status = newStatus

        if (newStatus == MitigationActionStatus.COMPLETED) {
            action.completedAt = LocalDateTime.now()
            action.completionEvidence = completionEvidence
        }

        val savedAction = mitigationActionRepository.save(action)

        // Check if all actions are completed to complete workflow
        checkAndCompleteWorkflow(action.workflow.id)

        // Record on Hedera blockchain
        try {
            val statusData = mapOf(
                "actionId" to actionId,
                "workflowId" to action.workflow.id,
                "oldStatus" to oldStatus.name,
                "newStatus" to newStatus.name,
                "updatedBy" to updatedBy,
                "updatedAt" to LocalDateTime.now().toString(),
                "completionEvidence" to completionEvidence
            )

            hederaConsensusService.recordMitigationActionStatusChange(
                actionId = actionId,
                workflowId = action.workflow.id,
                oldStatus = oldStatus.name,
                newStatus = newStatus.name,
                updatedBy = updatedBy,
                metadata = objectMapper.writeValueAsString(statusData)
            )

            logger.info("Recorded action status change on Hedera")
        } catch (e: Exception) {
            logger.error("Failed to record action status change on Hedera", e)
        }

        logger.info("Updated action status: $actionId from $oldStatus to $newStatus")
        return convertActionToDto(savedAction)
    }

    /**
     * Complete a mitigation workflow
     */
    @Transactional
    fun completeWorkflow(
        workflowId: String,
        completionNotes: String?,
        completedBy: String
    ): MitigationWorkflowDto {
        logger.info("Completing mitigation workflow: $workflowId")

        val workflow = mitigationWorkflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found: $workflowId") }

        // Check if all actions are completed
        val pendingActions = mitigationActionRepository.countByWorkflowIdAndStatus(
            workflowId,
            MitigationActionStatus.PENDING
        )
        val inProgressActions = mitigationActionRepository.countByWorkflowIdAndStatus(
            workflowId,
            MitigationActionStatus.IN_PROGRESS
        )

        if (pendingActions > 0 || inProgressActions > 0) {
            throw IllegalStateException(
                "Cannot complete workflow: $pendingActions pending and $inProgressActions in-progress actions remain"
            )
        }

        workflow.status = MitigationStatus.COMPLETED
        workflow.completedAt = LocalDateTime.now()
        workflow.completionNotes = completionNotes

        val savedWorkflow = mitigationWorkflowRepository.save(workflow)

        // Record on Hedera blockchain
        try {
            val completionData = mapOf(
                "workflowId" to workflowId,
                "batchId" to workflow.batch.id,
                "completedBy" to completedBy,
                "completedAt" to LocalDateTime.now().toString(),
                "completionNotes" to completionNotes,
                "totalActions" to workflow.actions.size,
                "completedActions" to workflow.actions.count { it.status == MitigationActionStatus.COMPLETED }
            )

            hederaConsensusService.recordMitigationWorkflowCompletion(
                workflowId = workflowId,
                batchId = workflow.batch.id,
                completedBy = completedBy,
                metadata = objectMapper.writeValueAsString(completionData)
            )

            logger.info("Recorded workflow completion on Hedera")
        } catch (e: Exception) {
            logger.error("Failed to record workflow completion on Hedera", e)
        }

        logger.info("Completed mitigation workflow: $workflowId")
        return convertToDto(savedWorkflow)
    }

    /**
     * Get workflows by batch
     */
    fun getWorkflowsByBatch(batchId: String): List<MitigationWorkflowDto> {
        return mitigationWorkflowRepository.findByBatchId(batchId)
            .map { convertToDto(it) }
    }

    /**
     * Get workflow by ID
     */
    fun getWorkflowById(workflowId: String): MitigationWorkflowDto {
        val workflow = mitigationWorkflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found: $workflowId") }
        return convertToDto(workflow)
    }

    /**
     * Get active workflows (pending or in-progress)
     */
    fun getActiveWorkflows(): List<MitigationWorkflowDto> {
        return mitigationWorkflowRepository.findActiveWorkflows()
            .map { convertToDto(it) }
    }

    /**
     * Get workflows by creator
     */
    fun getWorkflowsByCreator(createdBy: String): List<MitigationWorkflowDto> {
        return mitigationWorkflowRepository.findByCreatedBy(createdBy)
            .map { convertToDto(it) }
    }

    /**
     * Get actions for a workflow
     */
    fun getActionsByWorkflow(workflowId: String): List<MitigationActionDto> {
        return mitigationActionRepository.findByWorkflowId(workflowId)
            .map { convertActionToDto(it) }
    }

    /**
     * Get actions assigned to a user
     */
    fun getActionsAssignedTo(userId: String): List<MitigationActionDto> {
        return mitigationActionRepository.findByAssignedTo(userId)
            .map { convertActionToDto(it) }
    }

    /**
     * Get overdue actions
     */
    fun getOverdueActions(): List<MitigationActionDto> {
        return mitigationActionRepository.findOverdueActions()
            .map { convertActionToDto(it) }
    }

    /**
     * Get high-risk pending workflows
     */
    fun getHighRiskPendingWorkflows(): List<MitigationWorkflowDto> {
        return mitigationWorkflowRepository.findHighRiskPendingWorkflows()
            .map { convertToDto(it) }
    }

    /**
     * Get workflow statistics
     */
    fun getWorkflowStatistics(): MitigationStatisticsDto {
        return MitigationStatisticsDto(
            totalWorkflows = mitigationWorkflowRepository.count(),
            pendingWorkflows = mitigationWorkflowRepository.countByStatus(MitigationStatus.PENDING),
            inProgressWorkflows = mitigationWorkflowRepository.countByStatus(MitigationStatus.IN_PROGRESS),
            completedWorkflows = mitigationWorkflowRepository.countByStatus(MitigationStatus.COMPLETED),
            rejectedWorkflows = mitigationWorkflowRepository.countByStatus(MitigationStatus.REJECTED),
            highRiskWorkflows = mitigationWorkflowRepository.countByRiskLevel(com.agriconnect.farmersportalapis.domain.eudr.RiskLevel.HIGH),
            mediumRiskWorkflows = mitigationWorkflowRepository.countByRiskLevel(com.agriconnect.farmersportalapis.domain.eudr.RiskLevel.MEDIUM),
            lowRiskWorkflows = mitigationWorkflowRepository.countByRiskLevel(com.agriconnect.farmersportalapis.domain.eudr.RiskLevel.LOW)
        )
    }

    /**
     * Check if workflow can be completed and complete it automatically
     */
    private fun checkAndCompleteWorkflow(workflowId: String) {
        val workflow = mitigationWorkflowRepository.findById(workflowId).orElse(null) ?: return

        if (workflow.status == MitigationStatus.COMPLETED) return

        val pendingCount = mitigationActionRepository.countByWorkflowIdAndStatus(
            workflowId,
            MitigationActionStatus.PENDING
        )
        val inProgressCount = mitigationActionRepository.countByWorkflowIdAndStatus(
            workflowId,
            MitigationActionStatus.IN_PROGRESS
        )

        // Auto-complete if all actions are done
        if (pendingCount == 0L && inProgressCount == 0L) {
            workflow.status = MitigationStatus.COMPLETED
            workflow.completedAt = LocalDateTime.now()
            workflow.completionNotes = "Automatically completed - all actions finished"
            mitigationWorkflowRepository.save(workflow)

            logger.info("Auto-completed workflow: $workflowId")
        }
    }

    /**
     * Convert MitigationWorkflow entity to DTO
     */
    private fun convertToDto(workflow: MitigationWorkflow): MitigationWorkflowDto {
        val actions = mitigationActionRepository.findByWorkflowId(workflow.id)

        return MitigationWorkflowDto(
            id = workflow.id,
            batchId = workflow.batch.id,
            batchCode = workflow.batch.batchCode,
            riskLevel = workflow.riskLevel,
            status = workflow.status,
            createdAt = workflow.createdAt,
            createdBy = workflow.createdBy,
            completedAt = workflow.completedAt,
            completionNotes = workflow.completionNotes,
            hederaTransactionId = workflow.hederaTransactionId,
            actions = actions.map { convertActionToDto(it) },
            totalActions = actions.size,
            pendingActions = actions.count { it.status == MitigationActionStatus.PENDING },
            inProgressActions = actions.count { it.status == MitigationActionStatus.IN_PROGRESS },
            completedActions = actions.count { it.status == MitigationActionStatus.COMPLETED }
        )
    }

    /**
     * Convert MitigationAction entity to DTO
     */
    private fun convertActionToDto(action: MitigationAction): MitigationActionDto {
        return MitigationActionDto(
            id = action.id,
            workflowId = action.workflow.id,
            actionType = action.actionType,
            description = action.description,
            status = action.status,
            assignedTo = action.assignedTo,
            dueDate = action.dueDate,
            createdAt = action.createdAt,
            completedAt = action.completedAt,
            completionEvidence = action.completionEvidence,
            hederaTransactionId = action.hederaTransactionId
        )
    }
}

// DTOs for API communication

data class CreateMitigationActionDto(
    val actionType: MitigationActionType,
    val description: String,
    val assignedTo: String?,
    val dueDate: LocalDate?
)

data class MitigationWorkflowDto(
    val id: String,
    val batchId: String,
    val batchCode: String,
    val riskLevel: com.agriconnect.farmersportalapis.domain.eudr.RiskLevel,
    val status: MitigationStatus,
    val createdAt: LocalDateTime,
    val createdBy: String,
    val completedAt: LocalDateTime?,
    val completionNotes: String?,
    val hederaTransactionId: String?,
    val actions: List<MitigationActionDto>,
    val totalActions: Int,
    val pendingActions: Int,
    val inProgressActions: Int,
    val completedActions: Int
)

data class MitigationActionDto(
    val id: String,
    val workflowId: String,
    val actionType: MitigationActionType,
    val description: String,
    val status: MitigationActionStatus,
    val assignedTo: String?,
    val dueDate: LocalDate?,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val completionEvidence: String?,
    val hederaTransactionId: String?
)

data class MitigationStatisticsDto(
    val totalWorkflows: Long,
    val pendingWorkflows: Long,
    val inProgressWorkflows: Long,
    val completedWorkflows: Long,
    val rejectedWorkflows: Long,
    val highRiskWorkflows: Long,
    val mediumRiskWorkflows: Long,
    val lowRiskWorkflows: Long
)

data class UpdateActionStatusDto(
    val newStatus: MitigationActionStatus,
    val completionEvidence: String?
)
