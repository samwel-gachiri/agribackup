package com.agriconnect.farmersportalapis.dto.eudr

import com.agriconnect.farmersportalapis.domain.eudr.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

// ============= AGGREGATOR DTOs =============

/**
 * Maps to: supply_chain_events table (WHERE action_type='COLLECTION')
 * Represents farmer produce collection events
 */
data class AggregationEventDTO(
    val eventId: String,
    val aggregatorId: String,
    val farmerId: String,
    val farmerName: String? = null,
    val collectionDate: LocalDateTime,
    val quantityKg: BigDecimal,
    val produceType: String,
    val qualityGrade: String? = null,
    val pricePerKg: BigDecimal? = null,
    val totalAmount: BigDecimal? = null,
    val paymentStatus: PaymentStatus,
    val paymentMethod: String? = null,
    val receiptNumber: String? = null,
    val collectionLocation: String? = null,
    val notes: String? = null,
    val hederaTransactionId: String? = null,
    val createdAt: LocalDateTime
)

data class CreateAggregationEventRequest(
    val farmerId: String,
    val quantityKg: BigDecimal,
    val produceType: String,
    val qualityGrade: String? = null,
    val pricePerKg: BigDecimal? = null,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val paymentMethod: String? = null,
    val collectionLocation: String? = null,
    val notes: String? = null
)

/**
 * Maps to: eudr_batches table (WHERE aggregator_id IS NOT NULL)
 * Represents consolidated batches ready for processing
 */
data class ConsolidatedBatchDTO(
    val batchId: String,
    val aggregatorId: String,
    val aggregationDate: LocalDate,
    val numberOfFarmers: Int,
    val totalQuantityKg: BigDecimal,
    val produceType: String,
    val averageQualityGrade: String? = null,
    val sourceEvents: List<String>? = null, // Collection event IDs
    val status: ConsolidatedBatchStatus,
    val destinationProcessorId: String? = null,
    val shippingDate: LocalDate? = null,
    val deliveryDate: LocalDate? = null,
    val transportDetails: String? = null,
    val hederaBatchHash: String? = null,
    val hederaTransactionId: String? = null,
    val createdAt: LocalDateTime
)

data class CreateConsolidatedBatchRequest(
    val collectionEventIds: List<String>,
    val produceType: String,
    val destinationProcessorId: String? = null,
    val transportDetails: String? = null
)

data class AggregatorRegistrationRequest(
    val userId: String,
    val organizationName: String,
    val registrationNumber: String,
    val aggregatorType: AggregatorType,
    val operatingRegion: String,
    val address: String,
    val capacity: String? = null,
    val numberOfMembers: Int? = null,
    val certifications: String? = null,
    val storageCapacityKg: BigDecimal? = null,
    val collectionCenterCount: Int? = null
)

data class AggregatorDTO(
    val id: String,
    val userId: String,
    val organizationName: String,
    val registrationNumber: String,
    val aggregatorType: AggregatorType,
    val operatingRegion: String,
    val address: String,
    val capacity: String? = null,
    val numberOfMembers: Int? = null,
    val certifications: String? = null,
    val storageCapacityKg: BigDecimal? = null,
    val collectionCenterCount: Int? = null,
    val verificationStatus: AggregatorVerificationStatus,
    val verifiedAt: LocalDateTime? = null,
    val totalCollectionEventsCount: Int? = 0,
    val totalVolumeCollectedKg: BigDecimal? = BigDecimal.ZERO,
    val totalBatchesCreated: Int? = 0,
    val hederaAccountId: String? = null,
    val hederaTransactionId: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AggregatorStatisticsDTO(
    val totalCollections: Int,
    val totalVolumeKg: BigDecimal,
    val totalFarmers: Int,
    val totalBatches: Int,
    val averageQualityGrade: String? = null,
    val pendingPayments: BigDecimal,
    val recentCollections: List<AggregationEventDTO>
)

// ============= IMPORTER DTOs =============

/**
 * Maps to: eudr_batches table (WHERE importer_id IS NOT NULL)
 * Represents import shipments
 */
data class ImportShipmentDTO(
    val batchId: String,
    val importerId: String,
    val shipmentNumber: String,
    val originCountry: String,
    val exporterName: String? = null,
    val sourceBatchId: String? = null, // Reference to processor batch
    val totalQuantityKg: BigDecimal,
    val produceType: String,
    val shippingDate: LocalDate,
    val estimatedArrivalDate: LocalDate? = null,
    val actualArrivalDate: LocalDate? = null,
    val portOfEntry: String? = null,
    val customsReferenceNumber: String? = null,
    val customsClearanceDate: LocalDate? = null,
    val customsClearanceStatus: String? = null,
    val inspectionRequired: Boolean,
    val inspectionDate: LocalDate? = null,
    val inspectionResult: InspectionResult? = null,
    val eudrComplianceStatus: EudrComplianceStatus,
    val eudrDueDiligenceStatementId: String? = null,
    val status: ShipmentStatus,
    val hederaShipmentHash: String? = null,
    val hederaTransactionId: String? = null,
    val createdAt: LocalDateTime
)

data class CreateImportShipmentRequest(
    val shipmentNumber: String,
    val originCountry: String,
    val exporterName: String? = null,
    val sourceBatchId: String? = null,
    val totalQuantityKg: BigDecimal,
    val produceType: String,
    val shippingDate: LocalDate,
    val estimatedArrivalDate: LocalDate? = null,
    val portOfEntry: String? = null,
    val eudrDueDiligenceStatementId: String? = null
)

/**
 * Maps to: eudr_documents table (WHERE shipment_id IS NOT NULL AND document_type IN customs/inspection types)
 * Represents customs and inspection documents
 */
data class CustomsDocumentDTO(
    val documentId: String,
    val shipmentId: String,
    val documentType: String,
    val documentNumber: String,
    val issuingAuthority: String? = null,
    val issueDate: LocalDate,
    val expiryDate: LocalDate? = null,
    val s3Key: String,
    val hederaHash: String? = null,
    val uploadedAt: LocalDateTime
)

data class UploadCustomsDocumentRequest(
    val shipmentId: String,
    val documentType: String,
    val documentNumber: String,
    val issuingAuthority: String? = null,
    val issueDate: LocalDate,
    val expiryDate: LocalDate? = null,
    val fileContent: ByteArray
)

data class InspectionRecordDTO(
    val documentId: String,
    val shipmentId: String,
    val inspectionType: String,
    val inspectionDate: LocalDate,
    val inspectorName: String,
    val inspectorOrganization: String? = null,
    val inspectionResult: InspectionResult,
    val findings: String? = null,
    val complianceIssues: String? = null,
    val recommendations: String? = null,
    val certificateNumber: String? = null,
    val s3Key: String? = null,
    val hederaHash: String? = null,
    val createdAt: LocalDateTime
)

data class CreateInspectionRecordRequest(
    val shipmentId: String,
    val inspectionType: String,
    val inspectorName: String,
    val inspectorOrganization: String? = null,
    val inspectionResult: InspectionResult,
    val findings: String? = null,
    val complianceIssues: String? = null,
    val recommendations: String? = null,
    val certificateNumber: String? = null
)

data class ImporterRegistrationRequest(
    val userId: String,
    val companyName: String,
    val importLicenseNumber: String,
    val companyAddress: String,
    val destinationCountry: String,
    val destinationPort: String? = null,
    val importCategories: List<String>? = null,
    val eudrComplianceOfficer: String? = null,
    val certificationDetails: String? = null
)

data class ImporterDTO(
    val id: String,
    val userId: String,
    val companyName: String,
    val importLicenseNumber: String,
    val companyAddress: String,
    val destinationCountry: String,
    val destinationPort: String? = null,
    val importCategories: List<String>? = null,
    val eudrComplianceOfficer: String? = null,
    val certificationDetails: String? = null,
    val verificationStatus: ImporterVerificationStatus,
    val verifiedAt: LocalDateTime? = null,
    val totalShipmentsReceived: Int? = 0,
    val totalImportVolumeKg: BigDecimal? = BigDecimal.ZERO,
    val hederaAccountId: String? = null,
    val hederaTransactionId: String? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ImporterStatisticsDTO(
    val totalShipments: Int,
    val totalVolumeKg: BigDecimal,
    val pendingShipments: Int,
    val inTransitShipments: Int,
    val customsClearanceShipments: Int,
    val deliveredShipments: Int,
    val complianceRate: BigDecimal,
    val recentShipments: List<ImportShipmentDTO>
)

// ============= TRACEABILITY DTOs =============

data class CompleteSupplyChainTraceabilityDTO(
    val batchId: String,
    val produceType: String,
    val totalQuantityKg: BigDecimal,
    val currentStage: String,
    val farmers: List<FarmerTraceabilityInfo>,
    val aggregator: AggregatorTraceabilityInfo? = null,
    val processor: ProcessorTraceabilityInfo? = null,
    val importer: ImporterTraceabilityInfo? = null,
    val timeline: List<SupplyChainEventSummary>,
    val hederaTransactions: List<HederaTransactionSummary>,
    val eudrCompliance: EudrComplianceSummary
)

data class FarmerTraceabilityInfo(
    val farmerId: String,
    val farmerName: String,
    val farmLocation: String,
    val quantityKg: BigDecimal,
    val collectionDate: LocalDateTime,
    val qualityGrade: String? = null
)

data class AggregatorTraceabilityInfo(
    val aggregatorId: String,
    val organizationName: String,
    val aggregationDate: LocalDate,
    val numberOfFarmers: Int,
    val totalQuantityKg: BigDecimal
)

data class ProcessorTraceabilityInfo(
    val processorId: String,
    val processorName: String,
    val processingDate: LocalDate,
    val processedQuantityKg: BigDecimal,
    val processingMethod: String? = null
)

data class ImporterTraceabilityInfo(
    val importerId: String,
    val companyName: String,
    val destinationCountry: String,
    val shipmentNumber: String,
    val shippingDate: LocalDate,
    val arrivalDate: LocalDate? = null,
    val customsStatus: String? = null,
    val inspectionResult: InspectionResult? = null
)

data class SupplyChainEventSummary(
    val eventType: String,
    val actorName: String,
    val actorType: String,
    val timestamp: LocalDateTime,
    val location: String? = null,
    val details: String? = null
)

data class HederaTransactionSummary(
    val transactionId: String,
    val consensusTimestamp: String,
    val eventType: String,
    val hashscanUrl: String
)

data class EudrComplianceSummary(
    val complianceStatus: EudrComplianceStatus,
    val dueDiligenceCompleted: Boolean,
    val geoCoordinatesVerified: Boolean,
    val documentationComplete: Boolean,
    val riskAssessment: String? = null,
    val complianceOfficer: String? = null
)
