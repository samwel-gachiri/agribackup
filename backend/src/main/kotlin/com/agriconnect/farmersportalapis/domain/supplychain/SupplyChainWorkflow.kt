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

    // EUDR Compliance Certificate NFT tracking
    @Column(name = "compliance_certificate_nft_id", length = 50)
    var complianceCertificateNftId: String? = null,

    @Column(name = "compliance_certificate_serial_number")
    var complianceCertificateSerialNumber: Long? = null,

    @Column(name = "compliance_certificate_transaction_id", length = 100)
    var complianceCertificateTransactionId: String? = null,

    @Column(name = "current_owner_account_id", length = 50)
    var currentOwnerAccountId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "certificate_status", length = 50)
    var certificateStatus: CertificateStatus = CertificateStatus.NOT_CREATED,

    @Column(name = "certificate_issued_at")
    var certificateIssuedAt: LocalDateTime? = null,

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

enum class CertificateStatus {
    NOT_CREATED,           // Certificate hasn't been issued yet
    PENDING_VERIFICATION,  // Workflow data collected, awaiting compliance checks
    COMPLIANT,             // Certificate issued and valid
    IN_TRANSIT,            // Certificate with exporter during transit
    TRANSFERRED_TO_IMPORTER, // Certificate transferred to importer
    CUSTOMS_VERIFIED,      // Customs authority verified the certificate
    DELIVERED,             // Goods delivered, certificate archived
    FROZEN,                // Certificate revoked due to fraud
    EXPIRED                // Certificate validity period expired
}
