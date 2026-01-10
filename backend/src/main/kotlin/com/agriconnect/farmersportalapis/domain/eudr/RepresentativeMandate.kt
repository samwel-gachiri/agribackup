package com.agriconnect.farmersportalapis.domain.eudr

import com.agriconnect.farmersportalapis.domain.profile.Exporter
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * RepresentativeMandate - Links a non-EU Exporter to an EU-based Authorised Representative
 *
 * EUDR Article 6 requires non-EU operators to designate an Authorised Representative
 * established in the EU. This entity represents the formal mandate agreement between
 * the non-EU exporter and their EU-based AR.
 *
 * Mandate Lifecycle:
 * 1. PENDING - Exporter invites AR or AR requests to represent exporter
 * 2. ACTIVE - Both parties have agreed, mandate is in effect
 * 3. REJECTED - Invited party declined the mandate
 * 4. EXPIRED - Mandate validity period has ended
 * 5. REVOKED - Either party terminated the mandate early
 *
 * The AR can only submit DDS on behalf of the exporter while the mandate is ACTIVE
 * and within the valid date range.
 */
@Entity
@Table(
    name = "representative_mandates",
    indexes = [
        Index(name = "idx_mandate_exporter_id", columnList = "exporter_id"),
        Index(name = "idx_mandate_representative_id", columnList = "representative_id"),
        Index(name = "idx_mandate_status", columnList = "status"),
        Index(name = "idx_mandate_valid_from", columnList = "valid_from"),
        Index(name = "idx_mandate_valid_to", columnList = "valid_to")
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_exporter_active_mandate",
            columnNames = ["exporter_id", "status"]
        )
    ]
)
data class RepresentativeMandate(
    @Id
    @Column(name = "mandate_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    /**
     * The non-EU exporter who is delegating DDS submission authority
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exporter_id", nullable = false)
    var exporter: Exporter,

    /**
     * The EU-based Authorised Representative who will act on behalf of the exporter
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_id", nullable = false)
    var representative: AuthorisedRepresentative,

    /**
     * Current status of the mandate
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: MandateStatus = MandateStatus.PENDING,

    /**
     * Who initiated this mandate request
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "initiated_by", length = 20, nullable = false)
    var initiatedBy: MandateInitiator,

    /**
     * Date when the mandate becomes effective
     */
    @Column(name = "valid_from", nullable = false)
    var validFrom: LocalDate,

    /**
     * Date when the mandate expires (null = indefinite until revoked)
     */
    @Column(name = "valid_to")
    var validTo: LocalDate? = null,

    /**
     * URL to the signed mandate document (legal agreement)
     */
    @Column(name = "signed_document_url")
    var signedDocumentUrl: String? = null,

    /**
     * Reference to the document in the documents table
     */
    @Column(name = "document_id", length = 36)
    var documentId: String? = null,

    /**
     * Scope of the mandate - what the AR is authorized to do
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", length = 30, nullable = false)
    var scope: MandateScope = MandateScope.FULL,

    /**
     * Specific commodity types this mandate covers (null = all commodities)
     */
    @Column(name = "commodity_restrictions")
    var commodityRestrictions: String? = null,

    /**
     * Optional message from the initiator when inviting
     */
    @Column(name = "invitation_message", columnDefinition = "TEXT")
    var invitationMessage: String? = null,

    /**
     * Optional reason when mandate is rejected or revoked
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    var rejectionReason: String? = null,

    /**
     * Timestamp when the mandate was accepted
     */
    @Column(name = "accepted_at")
    var acceptedAt: LocalDateTime? = null,

    /**
     * Timestamp when the mandate was rejected
     */
    @Column(name = "rejected_at")
    var rejectedAt: LocalDateTime? = null,

    /**
     * Timestamp when the mandate was revoked
     */
    @Column(name = "revoked_at")
    var revokedAt: LocalDateTime? = null,

    /**
     * Who revoked the mandate (EXPORTER or REPRESENTATIVE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "revoked_by", length = 20)
    var revokedBy: MandateInitiator? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Check if the mandate is currently valid for DDS submission
     */
    fun isCurrentlyValid(): Boolean {
        if (status != MandateStatus.ACTIVE) return false
        val today = LocalDate.now()
        val afterStart = !today.isBefore(validFrom)
        val beforeEnd = validTo == null || !today.isAfter(validTo)
        return afterStart && beforeEnd
    }

    /**
     * Check if mandate is expiring within the specified number of days
     */
    fun isExpiringSoon(daysThreshold: Long = 30): Boolean {
        if (validTo == null || status != MandateStatus.ACTIVE) return false
        val today = LocalDate.now()
        return validTo!!.minusDays(daysThreshold).isBefore(today) && !today.isAfter(validTo)
    }

    /**
     * Check if mandate can be accepted (is pending and not expired)
     */
    fun canBeAccepted(): Boolean {
        if (status != MandateStatus.PENDING) return false
        val today = LocalDate.now()
        // Cannot accept if the proposed start date has already passed significantly
        return !today.isAfter(validFrom.plusMonths(1))
    }

    /**
     * Accept this mandate
     */
    fun accept() {
        if (!canBeAccepted()) {
            throw IllegalStateException("Mandate cannot be accepted in current state")
        }
        status = MandateStatus.ACTIVE
        acceptedAt = LocalDateTime.now()
    }

    /**
     * Reject this mandate
     */
    fun reject(reason: String?) {
        if (status != MandateStatus.PENDING) {
            throw IllegalStateException("Only pending mandates can be rejected")
        }
        status = MandateStatus.REJECTED
        rejectionReason = reason
        rejectedAt = LocalDateTime.now()
    }

    /**
     * Revoke this mandate
     */
    fun revoke(by: MandateInitiator, reason: String?) {
        if (status != MandateStatus.ACTIVE) {
            throw IllegalStateException("Only active mandates can be revoked")
        }
        status = MandateStatus.REVOKED
        revokedBy = by
        rejectionReason = reason
        revokedAt = LocalDateTime.now()
    }

    /**
     * Check and update if mandate has expired
     */
    fun checkExpiration() {
        if (status == MandateStatus.ACTIVE && validTo != null && LocalDate.now().isAfter(validTo)) {
            status = MandateStatus.EXPIRED
        }
    }
}

/**
 * Status of a representative mandate
 */
enum class MandateStatus {
    /** Mandate invitation sent, awaiting response */
    PENDING,
    /** Mandate is accepted and currently in effect */
    ACTIVE,
    /** Mandate invitation was rejected */
    REJECTED,
    /** Mandate validity period has ended */
    EXPIRED,
    /** Mandate was terminated early by either party */
    REVOKED
}

/**
 * Who initiated the mandate request
 */
enum class MandateInitiator {
    /** Exporter invited the AR to represent them */
    EXPORTER,
    /** AR offered their services to the exporter */
    REPRESENTATIVE
}

/**
 * Scope of what the AR is authorized to do under this mandate
 */
enum class MandateScope {
    /** Full authority - can submit DDS, manage supply chain, handle compliance */
    FULL,
    /** DDS only - can only submit Due Diligence Statements */
    DDS_ONLY,
    /** View only - can view exporter data but not submit on their behalf */
    VIEW_ONLY
}
