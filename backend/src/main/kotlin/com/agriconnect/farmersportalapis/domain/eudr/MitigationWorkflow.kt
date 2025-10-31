package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "mitigation_workflows")
class MitigationWorkflow(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "workflow_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    var batch: EudrBatch,

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    var riskLevel: RiskLevel,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MitigationStatus = MitigationStatus.PENDING,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_by", nullable = false)
    var createdBy: String,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime?,

    @Column(name = "completion_notes", columnDefinition = "TEXT")
    var completionNotes: String?,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?,

    @OneToMany(mappedBy = "workflow", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var actions: MutableList<MitigationAction> = mutableListOf()
)

enum class MitigationStatus {
    PENDING, IN_PROGRESS, COMPLETED, REJECTED
}