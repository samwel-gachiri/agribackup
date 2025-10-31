package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "mitigation_actions")
class MitigationAction(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "action_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    var workflow: MitigationWorkflow,

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    var actionType: MitigationActionType,

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    var description: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MitigationActionStatus = MitigationActionStatus.PENDING,

    @Column(name = "assigned_to")
    var assignedTo: String?,

    @Column(name = "due_date")
    var dueDate: LocalDate?,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "completed_at")
    var completedAt: LocalDateTime?,

    @Column(name = "completion_evidence", columnDefinition = "TEXT")
    var completionEvidence: String?,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?
)

enum class MitigationActionType {
    ADDITIONAL_DOCUMENTATION, VERIFICATION_VISIT, BATCH_HOLD, SUPPLIER_QUESTIONNAIRE, SITE_INSPECTION
}

enum class MitigationActionStatus {
    PENDING, IN_PROGRESS, COMPLETED, REJECTED
}