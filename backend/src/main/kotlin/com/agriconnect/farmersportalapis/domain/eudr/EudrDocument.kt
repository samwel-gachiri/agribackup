package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "eudr_documents")
class EudrDocument(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @Column(name = "owner_entity_id", nullable = false)
    var ownerEntityId: String,

    @Column(name = "owner_entity_type", nullable = false)
    var ownerEntityType: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    var batch: EudrBatch?,

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    var documentType: EudrDocumentType,

    @Column(name = "issuer")
    var issuer: String?,

    @Column(name = "issue_date")
    var issueDate: LocalDate?,

    @Column(name = "expiry_date")
    var expiryDate: LocalDate?,

    @Column(name = "s3_key", nullable = false)
    var s3Key: String,

    @Column(name = "file_name", nullable = false)
    var fileName: String,

    @Column(name = "mime_type", nullable = false)
    var mimeType: String,

    @Column(name = "checksum_sha256", nullable = false)
    var checksumSha256: String,

    @Column(name = "file_size", nullable = false)
    var fileSize: Long,

    @Column(name = "uploader_id", nullable = false)
    var uploaderId: String,

    @CreationTimestamp
    @Column(name = "uploaded_at")
    var uploadedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "exif_latitude")
    var exifLatitude: Double?,

    @Column(name = "exif_longitude")
    var exifLongitude: Double?,

    @Column(name = "exif_timestamp")
    var exifTimestamp: LocalDateTime?,

    @Column(name = "tags", columnDefinition = "JSON")
    var tags: String?,

    @Column(name = "retention_until")
    var retentionUntil: LocalDate?,

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    var visibility: DocumentVisibility = DocumentVisibility.PRIVATE,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?,

    @Column(name = "hedera_hash_record")
    var hederaHashRecord: String?
)

enum class EudrDocumentType {
    // Basic document types
    LICENSE, 
    PERMIT, 
    INVOICE, 
    CERTIFICATE,
    OTHER,
    
    // Specific license types
    FARMING_LICENSE,
    EXPORT_LICENSE,
    ENVIRONMENTAL_PERMIT,
    
    // Certificates
    LAND_OWNERSHIP_CERTIFICATE,
    LAND_RIGHTS_CERTIFICATE,
    ORGANIC_CERTIFICATE,
    QUALITY_CERTIFICATE,
    PHYTOSANITARY_CERTIFICATE,
    INSURANCE_CERTIFICATE,
    
    // Trade documents
    BILL_OF_LADING,
    PACKING_LIST,
    
    // Compliance documents
    SUPPLIER_DECLARATION,
    VERIFIER_REPORT,
    THIRD_PARTY_VERIFICATION,
    RISK_ASSESSMENT_REPORT,
    DUE_DILIGENCE_STATEMENT,
    SUPPLY_CHAIN_MAP,
    DEFORESTATION_ANALYSIS,
    SATELLITE_IMAGERY,
    FIELD_INSPECTION_REPORT,
    CORRECTIVE_ACTION_PLAN,
    COMPLIANCE_REPORT,
    
    // Processing documents
    PROCESSING_RECORD,
    TRANSPORT_DOCUMENT,
    
    // Harvest and geospatial documents
    HARVEST_RECORD,
    GEOLOCATION_DATA
}

enum class DocumentVisibility {
    PRIVATE, RESTRICTED, PUBLIC
}