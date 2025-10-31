package com.agriconnect.farmersportalapis.domain.eudr

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Importer Entity
 * Represents an organization that imports agricultural products
 * and ensures EUDR compliance at the final destination
 */
@Entity
@Table(name = "importers")
class Importer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "importer_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    var userProfile: UserProfile,

    @Column(name = "company_name", nullable = false)
    var companyName: String,

    @Column(name = "import_license_number", nullable = false)
    var importLicenseNumber: String,

    @Column(name = "company_address", nullable = false)
    var companyAddress: String,

    @Column(name = "destination_country", nullable = false)
    var destinationCountry: String,

    @Column(name = "destination_port")
    var destinationPort: String?,

    @Column(name = "import_categories", columnDefinition = "TEXT")
    var importCategories: String?, // JSON array of product categories

    @Column(name = "eudr_compliance_officer")
    var eudrComplianceOfficer: String?,

    @Column(name = "certification_details", columnDefinition = "TEXT")
    var certificationDetails: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    var verificationStatus: ImporterVerificationStatus = ImporterVerificationStatus.PENDING,

    @Column(name = "total_shipments_received")
    var totalShipmentsReceived: Int = 0,

    @Column(name = "total_import_volume_kg")
    var totalImportVolumeKg: BigDecimal = BigDecimal.ZERO,

    @Column(name = "hedera_account_id")
    var hederaAccountId: String?,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name ="updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "importer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var shipments: MutableList<ImportShipment> = mutableListOf()
)

enum class ImporterVerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    SUSPENDED
}

/**
 * Import Shipment
 * Tracks individual shipments received by importer
 */
@Entity
@Table(name = "import_shipments")
class ImportShipment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "shipment_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importer_id", nullable = false)
    var importer: Importer,

    @Column(name = "shipment_number", nullable = false, unique = true)
    var shipmentNumber: String,

    @Column(name = "source_batch_id")
    var sourceBatchId: String?, // Links to ConsolidatedBatch or EudrBatch

    @Column(name = "source_entity_id")
    var sourceEntityId: String?,

    @Column(name = "source_entity_type")
    var sourceEntityType: String?, // PROCESSOR, AGGREGATOR, EXPORTER

    @Column(name = "produce_type", nullable = false)
    var produceType: String,

    @Column(name = "quantity_kg", nullable = false)
    var quantityKg: BigDecimal,

    @Column(name = "origin_country", nullable = false)
    var originCountry: String,

    @Column(name = "departure_port")
    var departurePort: String?,

    @Column(name = "arrival_port")
    var arrivalPort: String?,

    @Column(name = "shipping_date")
    var shippingDate: LocalDate?,

    @Column(name = "estimated_arrival_date")
    var estimatedArrivalDate: LocalDate?,

    @Column(name = "actual_arrival_date")
    var actualArrivalDate: LocalDate?,

    @Column(name = "customs_clearance_date")
    var customsClearanceDate: LocalDate?,

    @Column(name = "customs_reference_number")
    var customsReferenceNumber: String?,

    @Column(name = "bill_of_lading_number")
    var billOfLadingNumber: String?,

    @Column(name = "container_numbers", columnDefinition = "TEXT")
    var containerNumbers: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status")
    var status: ShipmentStatus = ShipmentStatus.PENDING,

    @Enumerated(EnumType.STRING)
    @Column(name = "eudr_compliance_status")
    var eudrComplianceStatus: EudrComplianceStatus = EudrComplianceStatus.PENDING_VERIFICATION,

    @Column(name = "quality_inspection_passed")
    var qualityInspectionPassed: Boolean? = null,

    @Column(name = "quality_inspection_date")
    var qualityInspectionDate: LocalDate?,

    @Column(name = "quality_inspection_notes", columnDefinition = "TEXT")
    var qualityInspectionNotes: String?,

    @Column(name = "transport_method")
    var transportMethod: String?,

    @Column(name = "transport_company")
    var transportCompany: String?,

    @Column(name = "temperature_controlled")
    var temperatureControlled: Boolean = false,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?,

    @Column(name = "hedera_shipment_hash")
    var hederaShipmentHash: String?,

    @Column(name = "compliance_certificate_nft_id", length = 50)
    var complianceCertificateNftId: String? = null,

    @Column(name = "compliance_certificate_serial_number")
    var complianceCertificateSerialNumber: Long? = null,

    @Column(name = "compliance_certificate_transaction_id", length = 100)
    var complianceCertificateTransactionId: String? = null,

    @Column(name = "current_owner_account_id", length = 50)
    var currentOwnerAccountId: String? = null,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "shipment", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var customsDocuments: MutableList<CustomsDocument> = mutableListOf(),

    @OneToMany(mappedBy = "shipment", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var inspectionRecords: MutableList<InspectionRecord> = mutableListOf()
)

enum class ShipmentStatus {
    PENDING,
    IN_TRANSIT,
    CUSTOMS_CLEARANCE,
    QUALITY_INSPECTION,
    APPROVED,
    REJECTED,
    DELIVERED,
    CANCELLED
}

enum class EudrComplianceStatus {
    PENDING_VERIFICATION,
    COMPLIANT,
    NON_COMPLIANT,
    REQUIRES_ADDITIONAL_INFO,
    UNDER_REVIEW
}

/**
 * Customs Document
 * Stores customs-related documentation for import shipments
 */
@Entity
@Table(name = "customs_documents")
class CustomsDocument(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "document_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    var shipment: ImportShipment,

    @Column(name = "document_type", nullable = false)
    var documentType: String, // BILL_OF_LADING, CUSTOMS_DECLARATION, PHYTOSANITARY_CERTIFICATE, etc.

    @Column(name = "document_number")
    var documentNumber: String?,

    @Column(name = "issue_date")
    var issueDate: LocalDate?,

    @Column(name = "issuing_authority")
    var issuingAuthority: String?,

    @Column(name = "s3_key", nullable = false)
    var s3Key: String,

    @Column(name = "file_name", nullable = false)
    var fileName: String,

    @Column(name = "file_size")
    var fileSize: Long?,

    @Column(name = "checksum_sha256")
    var checksumSha256: String?,

    @Column(name = "hedera_document_hash")
    var hederaDocumentHash: String?,

    @CreationTimestamp
    @Column(name = "uploaded_at")
    var uploadedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * Inspection Record
 * Records quality and compliance inspections for shipments
 */
@Entity
@Table(name = "inspection_records")
class InspectionRecord(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inspection_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    var shipment: ImportShipment,

    @Column(name = "inspection_type", nullable = false)
    var inspectionType: String, // QUALITY, PHYTOSANITARY, EUDR_COMPLIANCE, etc.

    @Column(name = "inspection_date", nullable = false)
    var inspectionDate: LocalDate,

    @Column(name = "inspector_name")
    var inspectorName: String?,

    @Column(name = "inspector_agency")
    var inspectorAgency: String?,

    @Column(name = "inspection_result", nullable = false)
    @Enumerated(EnumType.STRING)
    var inspectionResult: InspectionResult,

    @Column(name = "findings", columnDefinition = "TEXT")
    var findings: String?,

    @Column(name = "recommendations", columnDefinition = "TEXT")
    var recommendations: String?,

    @Column(name = "certificate_number")
    var certificateNumber: String?,

    @Column(name = "hedera_inspection_hash")
    var hederaInspectionHash: String?,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
)

enum class InspectionResult {
    PASSED,
    PASSED_WITH_CONDITIONS,
    FAILED,
    PENDING
}
