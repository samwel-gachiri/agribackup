package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * GrievanceCase Entity
 *
 * Per EUDR Article 13, operators must establish grievance mechanisms
 * and respond to substantiated concerns regarding EUDR compliance.
 * This entity tracks complaints, concerns, and their resolution.
 *
 * Grievances can be raised by:
 * - Farmers about unfair treatment
 * - NGOs about environmental concerns
 * - Authorities about compliance issues
 * - Other stakeholders in the supply chain
 */
@Entity
@Table(
    name = "grievance_cases",
    indexes = [
        Index(name = "idx_grievance_status", columnList = "status"),
        Index(name = "idx_grievance_target_entity", columnList = "target_entity_type, target_entity_id"),
        Index(name = "idx_grievance_submitted_by", columnList = "submitted_by"),
        Index(name = "idx_grievance_assigned_to", columnList = "assigned_to"),
        Index(name = "idx_grievance_priority", columnList = "priority"),
        Index(name = "idx_grievance_submitted_at", columnList = "submitted_at")
    ]
)
class GrievanceCase(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "grievance_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    /**
     * Human-readable case reference number (e.g., "GRV-2026-00123")
     */
    @Column(name = "case_number", unique = true, nullable = false, length = 20)
    var caseNumber: String,

    /**
     * User ID of the person who submitted the grievance
     */
    @Column(name = "submitted_by", nullable = false, length = 36)
    var submittedBy: String,

    /**
     * Name of the person/organization who submitted (for display)
     */
    @Column(name = "submitter_name", nullable = false)
    var submitterName: String,

    /**
     * Contact email of the submitter
     */
    @Column(name = "submitter_email", nullable = false)
    var submitterEmail: String,

    /**
     * Contact phone (optional)
     */
    @Column(name = "submitter_phone", length = 30)
    var submitterPhone: String? = null,

    /**
     * Type of submitter (FARMER, NGO, AUTHORITY, PUBLIC, INTERNAL, OTHER)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "submitter_type", nullable = false)
    var submitterType: GrievanceSubmitterType,

    /**
     * Type of entity the grievance is about (EXPORTER, AGGREGATOR, PROCESSOR, etc.)
     */
    @Column(name = "target_entity_type", nullable = false, length = 50)
    var targetEntityType: String,

    /**
     * ID of the entity the grievance is about
     */
    @Column(name = "target_entity_id", nullable = false, length = 36)
    var targetEntityId: String,

    /**
     * Name of the target entity for display
     */
    @Column(name = "target_entity_name")
    var targetEntityName: String? = null,

    /**
     * Category of the grievance
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    var category: GrievanceCategory,

    /**
     * Brief title/summary of the grievance
     */
    @Column(name = "title", nullable = false)
    var title: String,

    /**
     * Detailed description of the grievance
     */
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    var description: String,

    /**
     * Evidence or supporting document URLs (JSON array)
     */
    @Column(name = "evidence_document_ids", columnDefinition = "TEXT")
    var evidenceDocumentIds: String? = null,

    /**
     * Current status of the grievance
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: GrievanceStatus = GrievanceStatus.SUBMITTED,

    /**
     * Priority level
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    var priority: GrievancePriority = GrievancePriority.MEDIUM,

    /**
     * User ID of the person assigned to handle this case
     */
    @Column(name = "assigned_to", length = 36)
    var assignedTo: String? = null,

    /**
     * Name of the assigned handler
     */
    @Column(name = "assigned_to_name")
    var assignedToName: String? = null,

    /**
     * When the case was submitted
     */
    @Column(name = "submitted_at", nullable = false)
    var submittedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * When the case was acknowledged (status changed from SUBMITTED)
     */
    @Column(name = "acknowledged_at")
    var acknowledgedAt: LocalDateTime? = null,

    /**
     * Target date for resolution (SLA)
     */
    @Column(name = "due_date")
    var dueDate: LocalDateTime? = null,

    /**
     * When the case was resolved
     */
    @Column(name = "resolved_at")
    var resolvedAt: LocalDateTime? = null,

    /**
     * When the case was closed
     */
    @Column(name = "closed_at")
    var closedAt: LocalDateTime? = null,

    /**
     * Resolution outcome
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_outcome")
    var resolutionOutcome: GrievanceResolutionOutcome? = null,

    /**
     * Notes on how the case was resolved
     */
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    var resolutionNotes: String? = null,

    /**
     * Actions taken to resolve the grievance (JSON array)
     */
    @Column(name = "resolution_actions", columnDefinition = "TEXT")
    var resolutionActions: String? = null,

    /**
     * Internal notes (not visible to submitter)
     */
    @Column(name = "internal_notes", columnDefinition = "TEXT")
    var internalNotes: String? = null,

    /**
     * Whether the submitter was notified of resolution
     */
    @Column(name = "submitter_notified", nullable = false)
    var submitterNotified: Boolean = false,

    /**
     * Whether the grievance is escalated
     */
    @Column(name = "is_escalated", nullable = false)
    var isEscalated: Boolean = false,

    /**
     * Escalation reason if escalated
     */
    @Column(name = "escalation_reason")
    var escalationReason: String? = null,

    /**
     * Related grievance ID (if this is a follow-up)
     */
    @Column(name = "related_grievance_id", length = 36)
    var relatedGrievanceId: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    /**
     * Comments/updates on this grievance case
     */
    @OneToMany(mappedBy = "grievanceCase", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var comments: MutableList<GrievanceComment> = mutableListOf()
) {
    /**
     * Check if the case is overdue
     */
    fun isOverdue(): Boolean {
        if (dueDate == null) return false
        if (status == GrievanceStatus.RESOLVED || status == GrievanceStatus.CLOSED) return false
        return LocalDateTime.now().isAfter(dueDate)
    }

    /**
     * Add a comment to the grievance
     */
    fun addComment(comment: GrievanceComment) {
        comments.add(comment)
        comment.grievanceCase = this
    }
}

/**
 * Comments/updates on a grievance case
 */
@Entity
@Table(
    name = "grievance_comments",
    indexes = [
        Index(name = "idx_comment_grievance_id", columnList = "grievance_id"),
        Index(name = "idx_comment_created_at", columnList = "created_at")
    ]
)
class GrievanceComment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "comment_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grievance_id", nullable = false)
    var grievanceCase: GrievanceCase,

    @Column(name = "author_id", nullable = false, length = 36)
    var authorId: String,

    @Column(name = "author_name", nullable = false)
    var authorName: String,

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    var content: String,

    /**
     * Whether this comment is internal (not visible to submitter)
     */
    @Column(name = "is_internal", nullable = false)
    var isInternal: Boolean = false,

    /**
     * Attached document IDs (JSON array)
     */
    @Column(name = "attachment_ids", columnDefinition = "TEXT")
    var attachmentIds: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Type of entity submitting the grievance
 */
enum class GrievanceSubmitterType {
    FARMER,
    NGO,
    AUTHORITY,
    PUBLIC,
    INTERNAL,
    SUPPLIER,
    BUYER,
    OTHER
}

/**
 * Category of grievance
 */
enum class GrievanceCategory {
    ENVIRONMENTAL_CONCERN,      // Deforestation, land degradation, etc.
    LABOR_RIGHTS,               // Worker treatment, fair wages, etc.
    LAND_RIGHTS,                // Disputes over land ownership
    PRICING_DISPUTE,            // Unfair pricing practices
    CONTRACT_VIOLATION,         // Breach of agreements
    QUALITY_ISSUE,              // Product quality concerns
    CERTIFICATION_FRAUD,        // False certifications
    DATA_PRIVACY,               // GDPR and data concerns
    COMPLIANCE_VIOLATION,       // EUDR or other regulatory violations
    OTHER
}

/**
 * Status workflow for grievances
 */
enum class GrievanceStatus {
    SUBMITTED,          // Initial submission
    ACKNOWLEDGED,       // Case received and assigned
    UNDER_INVESTIGATION,// Being actively investigated
    PENDING_RESPONSE,   // Waiting for response from target entity
    PENDING_EVIDENCE,   // Waiting for additional evidence
    ESCALATED,          // Escalated to higher authority
    RESOLVED,           // Resolution reached
    CLOSED,             // Case closed (resolved or withdrawn)
    REJECTED            // Case rejected as invalid/out of scope
}

/**
 * Priority levels
 */
enum class GrievancePriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Outcome of resolution
 */
enum class GrievanceResolutionOutcome {
    SUBSTANTIATED,      // Grievance was valid and addressed
    PARTIALLY_SUBSTANTIATED,
    UNSUBSTANTIATED,    // Grievance was not valid
    WITHDRAWN,          // Submitter withdrew the complaint
    OUT_OF_SCOPE,       // Not within our jurisdiction
    REFERRED            // Referred to external authority
}
