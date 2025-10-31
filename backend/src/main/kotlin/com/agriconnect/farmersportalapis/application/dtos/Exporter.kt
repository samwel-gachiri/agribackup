package com.agriconnect.farmersportalapis.application.dtos

import com.agriconnect.farmersportalapis.domain.common.enums.ExporterVerificationStatus
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import lombok.Builder
import lombok.Data
import java.math.BigDecimal

@Builder
@Data
data class CreateExporterRequestDto(
    val id: String,
    val uid: String? = null,
    val name: String,
    val licenseId: String,
    val email: String,
    val phoneNumber: String
)

@Builder
@Data
data class UpdateExporterRequestDto(
    val name: String?,
    val email: String?,
    val phoneNumber: String?
)

//@Builder
//@Data
//data class CreateZoneRequestDto(
//    val exporterId: String,
//    val name: String,
//    val produceType: String?,
//    val centerLatitude: BigDecimal,
//    val centerLongitude: BigDecimal,
//    val radiusKm: BigDecimal
//)

//@Builder
//@Data
//data class SchedulePickupRequestDto(
//    val exporterId: String,
//    val farmerId: String,
//    val produceListingId: String,
//    val scheduledDate: LocalDateTime,
//    val pickupNotes: String?
//)

@Builder
@Data
data class ExporterResponseDto(
    val id: String,
    val companyName: String?,
    val companyDesc: String?,
    val licenseId: String?,
    val verificationStatus: ExporterVerificationStatus,
    val exportLicenseFormUrl: String?
)

//@Builder
//@Data
//data class ZoneResponseDto(
//    val id: String,
//    val name: String,
//    val produceType: String?,
//    val centerLatitude: BigDecimal,
//    val centerLongitude: BigDecimal,
//    val radiusKm: BigDecimal,
//    val exporterId: String,
//    val farmerCount: Int
//)

//@Builder
//@Data
//data class FarmerInZoneResponseDto(
//    val farmerId: String,
//    val farmerName: String,
//    val farmSize: Double?,
//    val farmName: String?,
//    val location: Location?,
//    val joinedAt: LocalDateTime
//)
//
//@Builder
//@Data
//data class PickupScheduleResponseDto(
//    val id: String,
//    val exporterId: String,
//    val farmerId: String,
//    val produceListingId: String,
//    val scheduledDate: LocalDateTime,
//    val status: PickupStatus,
//    val pickupNotes: String?,
//    val createdAt: LocalDateTime,
//    val updatedAt: LocalDateTime
//)

data class UpdateZoneRequestDto(
    @field:NotEmpty(message = "Zone name cannot be empty")
    val name: String,

    @field:NotNull(message = "Produce type cannot be null")
    val produceType: String,

    @field:NotNull(message = "Center latitude is required")
    @field:DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
    @field:DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
    val centerLatitude: BigDecimal,

    @field:NotNull(message = "Center longitude is required")
    @field:DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
    @field:DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
    val centerLongitude: BigDecimal,

    @field:NotNull(message = "Radius is required")
    @field:DecimalMin(value = "0.1", message = "Radius must be greater than 0")
    val radiusKm: BigDecimal
)

// ------------------ new ------------------------- //

data class UserRegistrationDto(
    val email: String?,
    val password: String,
    val fullName: String,
    val phoneNumber: String?
)

data class SystemAdminRegistrationDto(
    val user: UserRegistrationDto
)

// Moved to AdminDtos.kt: SystemAdminResponseDto

data class ZoneSupervisorRegistrationDto(
    val user: UserRegistrationDto
)

// Moved to AdminDtos.kt: ZoneSupervisorResponseDto

// Moved to AdminDtos.kt: CreateZoneRequestDto

// Moved to AdminDtos.kt: ZoneResponseDto

// Moved to AdminDtos.kt: AssignZoneSupervisorDto

// Moved to AdminDtos.kt: AddFarmerToZoneDto

// Moved to AdminDtos.kt: UpdateZoneCommentDto

// Moved to AdminDtos.kt: UpdateFarmerRequestDto

// Moved to AdminDtos.kt: FarmerResponseDto

// Moved to AdminDtos.kt: FarmerInZoneResponseDto

// Moved to AdminDtos.kt: SchedulePickupRequestDto

@Builder
@Data
data class SubmitLicenseRequestDto(
    val licenseId: String?
)

@Builder
@Data
data class SubmitLicenseWithDocumentRequestDto(
    val licenseId: String?
)