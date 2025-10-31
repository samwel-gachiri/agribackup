package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "audit_logs")
class AuditLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "log_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @Column(name = "entity_type", nullable = false)
    var entityType: String,

    @Column(name = "entity_id", nullable = false)
    var entityId: String,

    @Column(name = "action", nullable = false)
    var action: String,

    @Column(name = "actor_id", nullable = false)
    var actorId: String,

    @Column(name = "actor_role", nullable = false)
    var actorRole: String,

    @Column(name = "timestamp", nullable = false)
    var timestamp: LocalDateTime = LocalDateTime.now(),

    @Column(name = "details_json", columnDefinition = "JSON")
    var detailsJson: String?,

    @Column(name = "record_hash", nullable = false)
    var recordHash: String,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?
)