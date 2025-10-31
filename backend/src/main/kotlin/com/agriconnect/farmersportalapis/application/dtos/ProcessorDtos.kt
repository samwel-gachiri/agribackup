package com.agriconnect.farmersportalapis.application.dtos

import com.agriconnect.farmersportalapis.domain.eudr.ProcessorVerificationStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDateTime

// ============================================
// PROCESSOR REQUEST DTOs
// ============================================

data class CreateProcessorRequestDto(
    @field:NotBlank(message = "Facility name is required")
    val facilityName: String,

    @field:NotBlank(message = "Facility address is required")
    val facilityAddress: String?,

    val processorType: String?,

    val processingCapabilities: String?,

    val capacityPerDayKg: BigDecimal?,

    val certifications: String?,

    val hederaAccountId: String?,
    
    // User profile details
    @field:NotBlank(message = "Email is required")
    val email: String?,

    @field:NotBlank(message = "Phone number is required")
    val phoneNumber: String?,

    @field:NotBlank(message = "Full name is required")
    val fullName: String
)

data class UpdateProcessorRequestDto(
    val facilityName: String?,
    val facilityAddress: String?,
    val processorType: String?,
    val processingCapabilities: String?,
    val capacityPerDayKg: BigDecimal?,
    val certifications: String?,
    val hederaAccountId: String?
)

data class CreateProcessingEventRequestDto(
    @field:NotBlank(message = "Processor ID is required")
    val processorId: String,
    
    @field:NotBlank(message = "Batch ID is required")
    val batchId: String,
    
    @field:NotBlank(message = "Processing type is required")
    val processingType: String,
    
    @field:NotNull(message = "Input quantity is required")
    @field:Positive(message = "Input quantity must be positive")
    val inputQuantityKg: BigDecimal,
    
    @field:NotNull(message = "Output quantity is required")
    @field:Positive(message = "Output quantity must be positive")
    val outputQuantityKg: BigDecimal,
    
    @field:NotNull(message = "Processing date is required")
    val processingDate: LocalDateTime,
    
    val processingNotes: String?,
    
    val qualityMetrics: String?,
    
    val temperatureLog: String?
)

// ============================================
// PROCESSOR RESPONSE DTOs
// ============================================

data class ProcessorResponseDto(
    val id: String,
    val facilityName: String,
    val facilityAddress: String,
    val processorType: String?,
    val processingCapabilities: String?,
    val capacityPerDayKg: BigDecimal?,
    val certifications: String?,
    val verificationStatus: ProcessorVerificationStatus,
    val totalBatchesProcessed: Int,
    val hederaAccountId: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val userProfile: UserProfileSummaryDto
)

data class ProcessingEventResponseDto(
    val id: String,
    val processorId: String,
    val processorName: String,
    val batchId: String,
    val batchCode: String?,
    val processingType: String,
    val inputQuantityKg: BigDecimal,
    val outputQuantityKg: BigDecimal,
    val yieldPercentage: BigDecimal,
    val processingDate: LocalDateTime,
    val processingNotes: String?,
    val qualityMetrics: String?,
    val temperatureLog: String?,
    val hederaTransactionId: String?,
    val createdAt: LocalDateTime
)

data class ProcessorStatisticsDto(
    val processorId: String,
    val totalBatchesProcessed: Int,
    val totalInputQuantityKg: BigDecimal,
    val totalOutputQuantityKg: BigDecimal,
    val averageYieldPercentage: BigDecimal,
    val currentMonthProcessingKg: BigDecimal,
    val topProcessingTypes: List<ProcessingTypeSummaryDto>,
    val averageProcessingTimeHours: BigDecimal?
)

data class ProcessingTypeSummaryDto(
    val processingType: String,
    val totalBatchesProcessed: Int,
    val totalInputQuantityKg: BigDecimal,
    val totalOutputQuantityKg: BigDecimal,
    val averageYieldPercentage: BigDecimal
)
