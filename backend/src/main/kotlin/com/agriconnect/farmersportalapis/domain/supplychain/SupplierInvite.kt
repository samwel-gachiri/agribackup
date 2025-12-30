package com.agriconnect.farmersportalapis.domain.supplychain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

/**
 * Entity representing a supplier invitation
 * Exporters can invite potential suppliers to register and connect
 */
@Entity
@Table(name = "supplier_invites")
data class SupplierInvite(
    @Id
    @Column(name = "id", nullable = false)
    var id: String = UUID.randomUUID().toString(),

    @Column(name = "email", nullable = false)
    var email: String,

    @Column(name = "supplier_name")
    var supplierName: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "supplier_type")
    var supplierType: SupplierType? = null,

    @Column(name = "inviter_id")
    var inviterId: String? = null,

    @Column(name = "inviter_name")
    var inviterName: String? = null,

    @Column(name = "inviter_company")
    var inviterCompany: String? = null,

    @Column(name = "message", length = 1000)
    var message: String? = null,

    // For sub-supplier invitations - tracks which supplier invited this person
    @Column(name = "parent_supplier_id")
    var parentSupplierId: String? = null,

    // Type of inviter: EXPORTER or SUPPLIER
    @Column(name = "inviter_type", length = 20)
    var inviterType: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: InviteStatus = InviteStatus.PENDING,

    @Column(name = "resent_count")
    var resentCount: Int = 0,

    @Column(name = "last_sent_at")
    var lastSentAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "accepted_at")
    var acceptedAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

enum class InviteStatus {
    PENDING,
    ACCEPTED,
    EXPIRED,
    CANCELLED
}
