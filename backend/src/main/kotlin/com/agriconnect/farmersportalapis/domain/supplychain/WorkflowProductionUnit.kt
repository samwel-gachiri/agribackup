package com.agriconnect.farmersportalapis.domain.supplychain

import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Join entity linking Production Units to Supply Chain Workflows
 * 
 * This represents Stage 1 (PRODUCTION_REGISTRATION) of EUDR compliance,
 * where production units are registered to a workflow BEFORE collection events.
 * 
 * This is separate from WorkflowCollectionEvent which tracks actual
 * collection of produce (Stage 4: COLLECTION_AGGREGATION).
 */
@Entity
@Table(
    name = "workflow_production_units",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_workflow_production_unit",
            columnNames = ["workflow_id", "production_unit_id"]
        )
    ],
    indexes = [
        Index(name = "idx_workflow_prod_unit_workflow", columnList = "workflow_id"),
        Index(name = "idx_workflow_prod_unit_production_unit", columnList = "production_unit_id")
    ]
)
class WorkflowProductionUnit(
    @Id
    @Column(name = "id", length = 36)
    var id: String = UUID.randomUUID().toString(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    var workflow: SupplyChainWorkflow,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_unit_id", nullable = false)
    var productionUnit: ProductionUnit,

    /**
     * Status of this production unit registration in the workflow
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    var status: WorkflowProductionUnitStatus = WorkflowProductionUnitStatus.PENDING,

    /**
     * Whether the production unit's geolocation has been verified for this workflow
     */
    @Column(name = "geolocation_verified")
    var geolocationVerified: Boolean = false,

    /**
     * Whether deforestation check has been completed for this production unit
     */
    @Column(name = "deforestation_checked")
    var deforestationChecked: Boolean = false,

    /**
     * Result of deforestation check - true if no deforestation detected
     */
    @Column(name = "deforestation_clear")
    var deforestationClear: Boolean? = null,

    /**
     * Notes about this production unit's inclusion in the workflow
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "linked_at", nullable = false)
    var linkedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "linked_by")
    var linkedBy: String? = null,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class WorkflowProductionUnitStatus {
    PENDING,           // Just linked, awaiting verification
    VERIFIED,          // Geolocation verified
    DEFORESTATION_CLEAR, // Deforestation check passed
    READY,             // Ready for collection
    COLLECTED,         // Has collection events
    REMOVED            // Removed from workflow
}
