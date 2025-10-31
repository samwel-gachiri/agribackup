package com.agriconnect.farmersportalapis.application.dtos

import com.agriconnect.farmersportalapis.domain.eudr.AggregatorType
import com.agriconnect.farmersportalapis.domain.eudr.AggregatorVerificationStatus
import com.agriconnect.farmersportalapis.domain.eudr.ConsolidatedBatchStatus
import com.agriconnect.farmersportalapis.domain.eudr.PaymentStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

// ============================================
// AGGREGATOR REQUEST DTOs
// ============================================

data class CreateAggregatorRequestDto(
    @field:NotBlank(message = "Organization name is required")
    val organizationName: String,

    @field:NotNull(message = "Organization type is required")
    val organizationType: String?,

    val registrationNumber: String?,

    @field:NotBlank(message = "Facility address is required")
    val facilityAddress: String?,

    val storageCapacityTons: Double?,

    val collectionRadiusKm: Double?,

    val primaryCommodities: List<String>?,

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

data class UpdateAggregatorRequestDto(
    val organizationName: String?,
    val facilityAddress: String?,
    val storageCapacityTons: BigDecimal?,
    val collectionRadiusKm: BigDecimal?,
    val primaryCommodities: List<String>?,
    val certificationDetails: String?,
    val hederaAccountId: String?
)

data class CreateAggregationEventRequestDto(
    @field:NotBlank(message = "Aggregator ID is required")
    val aggregatorId: String,
    
    @field:NotBlank(message = "Farmer ID is required")
    val farmerId: String,
    
    @field:NotBlank(message = "Produce type is required")
    val produceType: String,
    
    @field:NotNull(message = "Quantity is required")
    @field:Positive(message = "Quantity must be positive")
    val quantityKg: BigDecimal,
    
    val qualityGrade: String?,
    val pricePerKg: BigDecimal?,
    val collectionLocationGps: String?,
    val moistureContent: BigDecimal?,
    val impurityPercentage: BigDecimal?,
    val notes: String?
)

data class CreateConsolidatedBatchRequestDto(
    @field:NotBlank(message = "Aggregator ID is required")
    val aggregatorId: String,
    
    @field:NotBlank(message = "Produce type is required")
    val produceType: String,
    
    @field:NotNull(message = "Aggregation event IDs are required")
    val aggregationEventIds: List<String>,
    
    val destinationEntityId: String?,
    val destinationEntityType: String?,
    val transportDetails: String?
)

data class UpdatePaymentStatusRequestDto(
    @field:NotBlank(message = "Aggregation event ID is required")
    val aggregationEventId: String,
    
    @field:NotNull(message = "Payment status is required")
    val paymentStatus: PaymentStatus,
    
    val totalPayment: BigDecimal?
)

// ============================================
// AGGREGATOR RESPONSE DTOs
// ============================================

data class AggregatorResponseDto(
    val id: String,
    val organizationName: String,
    val organizationType: AggregatorType,
    val registrationNumber: String?,
    val facilityAddress: String,
    val storageCapacityTons: BigDecimal?,
    val collectionRadiusKm: BigDecimal?,
    val primaryCommodities: List<String>?,
    val certificationDetails: String?,
    val verificationStatus: AggregatorVerificationStatus,
    val totalFarmersConnected: Int,
    val totalBatchesCollected: Int,
    val hederaAccountId: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val userProfile: UserProfileSummaryDto
)

data class AggregationEventResponseDto(
    val id: String,
    val aggregatorId: String,
    val aggregatorName: String,
    val farmerId: String,
    val farmerName: String?,
    val produceType: String,
    val quantityKg: BigDecimal,
    val qualityGrade: String?,
    val pricePerKg: BigDecimal?,
    val totalPayment: BigDecimal?,
    val paymentStatus: PaymentStatus,
    val collectionDate: LocalDateTime,
    val collectionLocationGps: String?,
    val moistureContent: BigDecimal?,
    val impurityPercentage: BigDecimal?,
    val notes: String?,
    val hederaTransactionId: String?,
    val consolidatedBatchId: String?,
    val createdAt: LocalDateTime
)

data class ConsolidatedBatchResponseDto(
    val id: String,
    val aggregatorId: String,
    val aggregatorName: String,
    val batchNumber: String,
    val produceType: String,
    val totalQuantityKg: BigDecimal,
    val numberOfFarmers: Int,
    val averageQualityGrade: String?,
    val consolidationDate: LocalDateTime,
    val destinationEntityId: String?,
    val destinationEntityType: String?,
    val status: ConsolidatedBatchStatus,
    val shipmentDate: LocalDateTime?,
    val deliveryDate: LocalDateTime?,
    val transportDetails: String?,
    val hederaTransactionId: String?,
    val hederaBatchHash: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val aggregationEvents: List<AggregationEventSummaryDto>?
)

data class AggregationEventSummaryDto(
    val id: String,
    val farmerId: String,
    val farmerName: String?,
    val quantityKg: BigDecimal,
    val qualityGrade: String?,
    val collectionDate: LocalDateTime
)

data class AggregatorStatisticsDto(
    val aggregatorId: String,
    val totalFarmersConnected: Int,
    val totalCollectionEvents: Int,
    val totalConsolidatedBatches: Int,
    val totalQuantityCollectedKg: BigDecimal,
    val totalPaymentsMade: BigDecimal,
    val pendingPaymentsCount: Int,
    val currentMonthCollectionKg: BigDecimal,
    val averageQualityGrade: String?,
    val topProduceTypes: List<ProduceTypeSummaryDto>
)

data class ProduceTypeSummaryDto(
    val produceType: String,
    val totalQuantityKg: BigDecimal,
    val numberOfCollections: Int,
    val averagePrice: BigDecimal?
)

data class UserProfileSummaryDto(
    val id: String,
    val fullName: String,
    val email: String?,
    val phoneNumber: String?
)
