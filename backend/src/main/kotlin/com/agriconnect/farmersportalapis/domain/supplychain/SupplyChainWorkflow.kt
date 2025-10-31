package com.agriconnect.farmersportalapis.domain.supplychain

import com.agriconnect.farmersportalapis.domain.profile.Exporter
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "supply_chain_workflows")
class SupplyChainWorkflow(
    @Id
    @Column(name = "workflow_id", length = 36)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exporter_id", nullable = false)
    var exporter: Exporter,

    @Column(name = "workflow_name", nullable = false)
    var workflowName: String,

    @Column(name = "produce_type", nullable = false, length = 100)
    var produceType: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: WorkflowStatus = WorkflowStatus.IN_PROGRESS,

    @Enumerated(EnumType.STRING)
    @Column(name = "current_stage", nullable = false, length = 50)
    var currentStage: WorkflowStage = WorkflowStage.COLLECTION,

    @Column(name = "total_quantity_kg", precision = 15, scale = 2)
    var totalQuantityKg: BigDecimal = BigDecimal.ZERO,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "workflow", cascade = [CascadeType.ALL], orphanRemoval = true)
    var collectionEvents: MutableList<WorkflowCollectionEvent> = mutableListOf(),

    @OneToMany(mappedBy = "workflow", cascade = [CascadeType.ALL], orphanRemoval = true)
    var consolidationEvents: MutableList<WorkflowConsolidationEvent> = mutableListOf(),

    @OneToMany(mappedBy = "workflow", cascade = [CascadeType.ALL], orphanRemoval = true)
    var processingEvents: MutableList<WorkflowProcessingEvent> = mutableListOf(),

    @OneToMany(mappedBy = "workflow", cascade = [CascadeType.ALL], orphanRemoval = true)
    var shipmentEvents: MutableList<WorkflowShipmentEvent> = mutableListOf()
)

enum class WorkflowStatus {
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class WorkflowStage {
    COLLECTION,
    CONSOLIDATION,
    PROCESSING,
    SHIPMENT,
    COMPLETED
}
