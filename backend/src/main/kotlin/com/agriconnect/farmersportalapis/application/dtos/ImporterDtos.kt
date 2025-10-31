package com.agriconnect.farmersportalapis.application.dtos

import com.agriconnect.farmersportalapis.domain.eudr.EudrComplianceStatus
import com.agriconnect.farmersportalapis.domain.eudr.ImporterVerificationStatus
import com.agriconnect.farmersportalapis.domain.eudr.InspectionResult
import com.agriconnect.farmersportalapis.domain.eudr.ShipmentStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

// ============================================
// IMPORTER REQUEST DTOs
// ============================================

data class CreateImporterRequestDto(
    @field:NotBlank(message = "Company name is required")
    val companyName: String,

    @field:NotBlank(message = "Import license number is required")
    val importLicenseNumber: String?,

    @field:NotBlank(message = "Company address is required")
    val companyAddress: String?,

    @field:NotBlank(message = "Destination country is required")
    val destinationCountry: String?,

    val destinationPort: String?,

    val importCategories: List<String>?,

    val eudrComplianceOfficer: String?,

    val certificationDetails: String?,

    val hederaAccountId: String?,
    
    // User profile details
    @field:NotBlank(message = "Email is required")
    val email: String?,

    @field:NotBlank(message = "Phone number is required")
    val phoneNumber: String?,

    @field:NotBlank(message = "Full name is required")
    val fullName: String
)

data class UpdateImporterRequestDto(
    val companyName: String?,
    val companyAddress: String?,
    val destinationPort: String?,
    val importCategories: List<String>?,
    val eudrComplianceOfficer: String?,
    val certificationDetails: String?,
    val hederaAccountId: String?
)

data class CreateImportShipmentRequestDto(
    @field:NotBlank(message = "Importer ID is required")
    val importerId: String,
    
    @field:NotBlank(message = "Shipment number is required")
    val shipmentNumber: String,
    
    val sourceBatchId: String?,
    val sourceEntityId: String?,
    val sourceEntityType: String?,
    
    @field:NotBlank(message = "Produce type is required")
    val produceType: String,
    
    @field:NotNull(message = "Quantity is required")
    @field:Positive(message = "Quantity must be positive")
    val quantityKg: BigDecimal,
    
    @field:NotBlank(message = "Origin country is required")
    val originCountry: String,
    
    val departurePort: String?,
    val arrivalPort: String?,
    val shippingDate: LocalDate?,
    val estimatedArrivalDate: LocalDate?,
    val billOfLadingNumber: String?,
    val containerNumbers: List<String>?,
    val transportMethod: String?,
    val transportCompany: String?,
    val temperatureControlled: Boolean = false
)

data class UpdateShipmentStatusRequestDto(
    @field:NotBlank(message = "Shipment ID is required")
    val shipmentId: String,
    
    @field:NotNull(message = "Status is required")
    val status: ShipmentStatus,
    
    val actualArrivalDate: LocalDate?,
    val customsClearanceDate: LocalDate?,
    val customsReferenceNumber: String?,
    val notes: String?
)

data class UpdateEudrComplianceRequestDto(
    @field:NotBlank(message = "Shipment ID is required")
    val shipmentId: String,
    
    @field:NotNull(message = "Compliance status is required")
    val complianceStatus: EudrComplianceStatus,
    
    val notes: String?
)

data class CreateInspectionRecordRequestDto(
    @field:NotBlank(message = "Shipment ID is required")
    val shipmentId: String,
    
    @field:NotBlank(message = "Inspection type is required")
    val inspectionType: String,
    
    @field:NotNull(message = "Inspection date is required")
    val inspectionDate: LocalDate,
    
    val inspectorName: String?,
    val inspectorAgency: String?,
    
    @field:NotNull(message = "Inspection result is required")
    val inspectionResult: InspectionResult,
    
    val findings: String?,
    val recommendations: String?,
    val certificateNumber: String?
)

data class UploadCustomsDocumentRequestDto(
    @field:NotBlank(message = "Shipment ID is required")
    val shipmentId: String,
    
    @field:NotBlank(message = "Document type is required")
    val documentType: String,
    
    val documentNumber: String?,
    val issueDate: LocalDate?,
    val issuingAuthority: String?,
    
    @field:NotBlank(message = "S3 key is required")
    val s3Key: String,
    
    @field:NotBlank(message = "File name is required")
    val fileName: String,
    
    val fileSize: Long?,
    val checksumSha256: String?
)

// ============================================
// IMPORTER RESPONSE DTOs
// ============================================

data class ImporterResponseDto(
    val id: String,
    val companyName: String,
    val importLicenseNumber: String,
    val companyAddress: String,
    val destinationCountry: String,
    val destinationPort: String?,
    val importCategories: List<String>?,
    val eudrComplianceOfficer: String?,
    val certificationDetails: String?,
    val verificationStatus: ImporterVerificationStatus,
    val totalShipmentsReceived: Int,
    val totalImportVolumeKg: BigDecimal,
    val hederaAccountId: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val userProfile: UserProfileSummaryDto
)

data class ImportShipmentResponseDto(
    val id: String,
    val importerId: String,
    val importerName: String,
    val shipmentNumber: String,
    val sourceBatchId: String?,
    val sourceEntityId: String?,
    val sourceEntityType: String?,
    val produceType: String,
    val quantityKg: BigDecimal,
    val originCountry: String,
    val departurePort: String?,
    val arrivalPort: String?,
    val shippingDate: LocalDate?,
    val estimatedArrivalDate: LocalDate?,
    val actualArrivalDate: LocalDate?,
    val customsClearanceDate: LocalDate?,
    val customsReferenceNumber: String?,
    val billOfLadingNumber: String?,
    val containerNumbers: List<String>?,
    val status: ShipmentStatus,
    val eudrComplianceStatus: EudrComplianceStatus,
    val qualityInspectionPassed: Boolean?,
    val qualityInspectionDate: LocalDate?,
    val qualityInspectionNotes: String?,
    val transportMethod: String?,
    val transportCompany: String?,
    val temperatureControlled: Boolean,
    val hederaTransactionId: String?,
    val hederaShipmentHash: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val inspectionRecords: List<InspectionRecordSummaryDto>?,
    val customsDocuments: List<CustomsDocumentSummaryDto>?
)

data class InspectionRecordResponseDto(
    val id: String,
    val shipmentId: String,
    val inspectionType: String,
    val inspectionDate: LocalDate,
    val inspectorName: String?,
    val inspectorAgency: String?,
    val inspectionResult: InspectionResult,
    val findings: String?,
    val recommendations: String?,
    val certificateNumber: String?,
    val hederaInspectionHash: String?,
    val createdAt: LocalDateTime
)

data class InspectionRecordSummaryDto(
    val id: String,
    val inspectionType: String,
    val inspectionDate: LocalDate,
    val inspectionResult: InspectionResult,
    val certificateNumber: String?
)

data class CustomsDocumentResponseDto(
    val id: String,
    val shipmentId: String,
    val documentType: String,
    val documentNumber: String?,
    val issueDate: LocalDate?,
    val issuingAuthority: String?,
    val s3Key: String,
    val fileName: String,
    val fileSize: Long?,
    val checksumSha256: String?,
    val hederaDocumentHash: String?,
    val uploadedAt: LocalDateTime
)

data class CustomsDocumentSummaryDto(
    val id: String,
    val documentType: String,
    val documentNumber: String?,
    val fileName: String,
    val uploadedAt: LocalDateTime
)

data class ImporterStatisticsDto(
    val importerId: String,
    val totalShipmentsReceived: Int,
    val totalImportVolumeKg: BigDecimal,
    val pendingShipments: Int,
    val inTransitShipments: Int,
    val customsClearanceShipments: Int,
    val deliveredShipments: Int,
    val eudrCompliantShipments: Int,
    val nonCompliantShipments: Int,
    val currentMonthVolumeKg: BigDecimal,
    val topOriginCountries: List<OriginCountrySummaryDto>,
    val topProduceTypes: List<ProduceTypeSummaryDto>
)

data class OriginCountrySummaryDto(
    val country: String,
    val totalShipments: Int,
    val totalVolumeKg: BigDecimal
)

data class ShipmentTraceabilityDto(
    val shipmentId: String,
    val shipmentNumber: String,
    val currentStatus: ShipmentStatus,
    val eudrCompliance: EudrComplianceStatus,
    val originTrace: OriginTraceDto?,
    val supplyChainEvents: List<SupplyChainEventSummaryDto>,
    val hederaVerification: HederaVerificationDto,
    val inspectionResults: List<InspectionRecordSummaryDto>,
    val customsDocuments: List<CustomsDocumentSummaryDto>
)

data class OriginTraceDto(
    val farmerId: String?,
    val farmerName: String?,
    val productionUnitId: String?,
    val productionUnitCoordinates: String?,
    val harvestDate: LocalDate?,
    val aggregatorName: String?,
    val processorName: String?
)

data class SupplyChainEventSummaryDto(
    val eventType: String,
    val actorName: String,
    val actorRole: String,
    val timestamp: LocalDateTime,
    val location: String?,
    val quantity: BigDecimal?,
    val hederaTransactionId: String?
)

data class HederaVerificationDto(
    val isVerified: Boolean,
    val topicId: String?,
    val transactionIds: List<String>,
    val consensusTimestamps: List<String>,
    val documentHashes: Map<String, String>,
    val verificationUrl: String?
)

// ============================================
// EUDR CERTIFICATE DTOs
// ============================================

data class CustomsVerificationResponseDto(
    val shipmentId: String,
    val approved: Boolean,
    val certificateValid: Boolean,
    val complianceStatus: String,
    val message: String,
    val certificateNftId: String? = null,
    val certificateSerialNumber: Long? = null,
    val currentOwner: String? = null,
    val verifiedAt: LocalDateTime
)
