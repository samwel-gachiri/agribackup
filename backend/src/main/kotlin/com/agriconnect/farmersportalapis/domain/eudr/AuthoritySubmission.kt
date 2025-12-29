package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Entity to track authority report submissions for EUDR compliance
 * 
 * This stores the history of submissions to regulatory authorities,
 * including status tracking, blockchain verification, and authority feedback.
 */
@Entity
@Table(name = "authority_submissions")
class AuthoritySubmission(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "submission_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    var batch: EudrBatch,

    @Column(name = "authority_code", nullable = false, length = 50)
    var authorityCode: String,

    @Column(name = "authority_name", length = 200)
    var authorityName: String? = null,

    @Column(name = "submitted_by", nullable = false)
    var submittedBy: String,

    @Column(name = "report_filename", nullable = false)
    var reportFilename: String,

    @Column(name = "report_hash", length = 64)
    var reportHash: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: SubmissionStatus = SubmissionStatus.SUBMITTED,

    @Column(name = "submission_reference", length = 100)
    var submissionReference: String? = null,

    @Column(name = "authority_reference", length = 100)
    var authorityReference: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "authority_feedback", columnDefinition = "TEXT")
    var authorityFeedback: String? = null,

    @Column(name = "hedera_transaction_id", length = 100)
    var hederaTransactionId: String? = null,

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false)
    var submittedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_status_update")
    var lastStatusUpdate: LocalDateTime? = null,

    @Column(name = "acknowledged_at")
    var acknowledgedAt: LocalDateTime? = null,

    @Column(name = "approved_at")
    var approvedAt: LocalDateTime? = null,

    @Column(name = "rejected_at")
    var rejectedAt: LocalDateTime? = null
)

enum class SubmissionStatus {
    DRAFT,              // Submission prepared but not sent
    SUBMITTED,          // Submitted to authority
    ACKNOWLEDGED,       // Authority acknowledged receipt
    UNDER_REVIEW,       // Authority is reviewing
    ADDITIONAL_INFO_REQUIRED,  // Authority requests more info
    APPROVED,           // Submission approved
    CONDITIONALLY_APPROVED,    // Approved with conditions
    REJECTED,           // Submission rejected
    EXPIRED,            // Submission expired
    CANCELLED           // Cancelled by submitter
}

/**
 * Supported regulatory authorities for EUDR compliance
 */
enum class AuthorityCode(val displayName: String, val countryCode: String) {
    // EU Central Systems
    TRACES_NT("EU TRACES NT System (Due Diligence Statement)", "EU"),
    EU_EUDR_PORTAL("EU EUDR Information System", "EU"),
    DE_BMEL("German Federal Ministry for Food and Agriculture", "DE"),
    FR_DGCCRF("French Directorate-General for Competition, Consumer Affairs and Fraud Control", "FR"),
    NL_NVWA("Netherlands Food and Consumer Product Safety Authority", "NL"),
    BE_FASFC("Belgian Federal Agency for the Safety of the Food Chain", "BE"),
    IT_ICQRF("Italian Central Inspectorate for Fraud Repression and Quality Protection", "IT"),
    ES_AICA("Spanish Information and Food Quality Control Agency", "ES"),
    AT_BAES("Austrian Federal Office for Food Safety", "AT"),
    PL_IJHARS("Polish Agricultural and Food Quality Inspection", "PL"),
    SE_LIVSMEDELSVERKET("Swedish Food Agency", "SE"),
    PT_ASAE("Portuguese Food and Economic Safety Authority", "PT"),
    DK_FVST("Danish Veterinary and Food Administration", "DK"),
    FI_RUOKAVIRASTO("Finnish Food Authority", "FI"),
    IE_FSAI("Food Safety Authority of Ireland", "IE"),
    GR_EFET("Hellenic Food Authority", "GR"),
    CZ_SZPI("Czech Agriculture and Food Inspection Authority", "CZ"),
    RO_ANPC("Romanian National Consumer Protection Authority", "RO"),
    HU_NEBIH("Hungarian National Food Chain Safety Office", "HU"),
    SK_SVPS("Slovak State Veterinary and Food Administration", "SK"),
    BG_BFSA("Bulgarian Food Safety Agency", "BG"),
    HR_MZPP("Croatian Ministry of Agriculture", "HR"),
    SI_UVHVVR("Slovenian Food, Veterinary and Plant Administration", "SI"),
    LT_VMVT("Lithuanian State Food and Veterinary Service", "LT"),
    LV_PVD("Latvian Food and Veterinary Service", "LV"),
    EE_VTA("Estonian Veterinary and Food Board", "EE"),
    CY_SVPH("Cyprus State Veterinary Services", "CY"),
    MT_MFA("Malta Food Agency", "MT"),
    LU_OSQCA("Luxembourg Food Safety Agency", "LU"),
    KE_KEPHIS("Kenya Plant Health Inspectorate Service", "KE"),
    KE_AFA("Kenya Agriculture and Food Authority", "KE"),
    ET_ECAE("Ethiopian Conformity Assessment Enterprise", "ET"),
    UG_UCDA("Uganda Coffee Development Authority", "UG"),
    TZ_TCA("Tanzania Coffee Authority", "TZ"),
    RW_NAEB("Rwanda National Agricultural Export Development Board", "RW"),
    CO_FNC("Colombian National Federation of Coffee Growers", "CO"),
    BR_MAPA("Brazilian Ministry of Agriculture", "BR"),
    VN_MARD("Vietnam Ministry of Agriculture and Rural Development", "VN"),
    ID_KEMENTAN("Indonesian Ministry of Agriculture", "ID"),
    GH_COCOBOD("Ghana Cocoa Board", "GH"),
    CI_CCC("Ivory Coast Coffee-Cocoa Council", "CI")
}
