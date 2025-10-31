package com.agriconnect.farmersportalapis.application.dtos

import com.agriconnect.farmersportalapis.domain.common.enums.PickupStatus
import com.agriconnect.farmersportalapis.domain.common.model.Location
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDateTime

// Request DTOs
data class CreateZoneRequestDto(
    @field:NotBlank(message = "Zone name is required")
    val name: String,
    
    val produceType: String?,
    
    @field:NotNull(message = "Center latitude is required")
    @field:DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @field:DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    val centerLatitude: BigDecimal,
    
    @field:NotNull(message = "Center longitude is required")
    @field:DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @field:DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    val centerLongitude: BigDecimal,
    
    @field:NotNull(message = "Radius is required")
    @field:DecimalMin(value = "0.1", message = "Radius must be greater than 0")
    val radiusKm: BigDecimal,
    
    @field:NotBlank(message = "Exporter ID is required")
    val exporterId: String
)

data class AssignZoneSupervisorDto(
    @field:NotBlank(message = "Zone Supervisor ID is required")
    val zoneSupervisorId: String
)

data class AddFarmerToZoneDto(
    @field:NotBlank(message = "Farmer ID is required")
    val farmerId: String
)

data class AddExistingFarmerLookupDto(
    val email: String? = null,
    val phoneNumber: String? = null
)

data class UpdateZoneCommentDto(
    val comments: String?
)

data class UpdateFarmerRequestDto(
    val farmName: String?,
    val farmSize: Double?,
    val location: Location?,
    val consentToken: String?
)

data class SchedulePickupRequestDto(
    @field:NotBlank(message = "Farmer ID is required")
    val farmerId: String,
    
    @field:NotBlank(message = "Exporter ID is required")
    val exporterId: String,
    
    @field:NotBlank(message = "Produce listing ID is required")
    val produceListingId: String,
    
    @field:NotNull(message = "Scheduled date is required")
    val scheduledDate: LocalDateTime,
    
    val pickupNotes: String?
)

// Response DTOs
data class ZoneResponseDto(
    val id: String,
    val name: String,
    val produceType: String?,
    val centerLatitude: BigDecimal,
    val centerLongitude: BigDecimal,
    val radiusKm: BigDecimal,
    val exporterId: String,
    val creatorId: String?,
    val comments: String?,
    val farmerCount: Int,
    val supervisorIds: List<String>
)

data class FarmerInZoneResponseDto(
    val farmerId: String?,
    val farmerName: String,
    val farmSize: Double?,
    val farmName: String?,
    val location: Location?,
    val joinedAt: LocalDateTime,
    // Newly added enriched fields
    val phoneNumber: String? = null,
    // Lightweight produce summaries (name + status)
    val produces: List<FarmerProduceSummaryDto> = emptyList(),
    // Expected harvest projections (per produce)
    val expectedHarvests: List<ExpectedHarvestDto> = emptyList()
)
{
    // Legacy projection constructor (JPQL 'new' expression) without the new fields
    constructor(
        farmerId: String?,
        farmerName: String,
        farmSize: Double?,
        farmName: String?,
        location: Location?,
        joinedAt: LocalDateTime
    ) : this(
        farmerId = farmerId,
        farmerName = farmerName,
        farmSize = farmSize,
        farmName = farmName,
        location = location,
        joinedAt = joinedAt,
        phoneNumber = null,
        produces = emptyList(),
        expectedHarvests = emptyList()
    )
}

data class FarmerProduceSummaryDto(
    val id: String,
    val name: String?,
    val status: String
)

data class ExpectedHarvestDto(
    val produceId: String,
    val predictedHarvestDate: java.time.LocalDate?,
    val predictedSpecies: String?,
    val predictionConfidence: Double?
)

data class ZoneSupervisorResponseDto(
    val id: String,
    val userId: String,
    val fullName: String,
    val email: String?,
    val phoneNumber: String?,
    val status: String,
    val zones: List<ZoneResponseDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    // Present only when a brand‑new user account was created server-side
    val tempPassword: String? = null
)

data class FarmerResponseDto(
    val id: String?,
    val farmName: String?,
    val farmSize: Double?,
    val location: Location?,
    val userId: String,
    val fullName: String,
    val email: String?,
    val phoneNumber: String?
)

data class PickupScheduleResponseDto(
    val id: String,
    val exporterId: String,
    val farmerId: String?,
    val produceListingId: String,
    val scheduledDate: LocalDateTime,
    val status: PickupStatus,
    val pickupNotes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// --- Pickup Route DTOs ---
// --- Pickup Route DTOs ---
// Pickup route DTOs are defined in their dedicated files to avoid duplicate declarations:
// - CreatePickupRouteRequestDto.kt
// - PickupRouteResponseDto.kt
// Keep any shared helper DTOs in their dedicated files as well.

data class PickupRouteSummaryDto(
    val routeId: String,
    val zoneId: String,
    val scheduledDate: java.time.LocalDateTime,
    val status: String,
    val stopCount: Int,
    val totalDistanceKm: Double?,
    val estimatedDurationMinutes: Int?
)

data class UpdatePickupStopStatusDto(
    val status: String,
    val notes: String? = null
)

data class UpdatePickupRouteStatusDto(
    val status: String
)

// --- Prediction-driven pickup suggestion DTOs ---
data class SuggestedPickupDto(
    val farmerId: String,
    val farmerName: String,
    val produceName: String,
    val predictedHarvestDate: java.time.LocalDate?,
    val predictedSpecies: String?,
    val confidence: Double?,
    val expectedQuantity: Double?,
    val expectedUnit: String?,
    val latitude: Double?,
    val longitude: Double?
)
// Exporter Superadmin DTOs
data class CreateSystemAdminRequestDto(
    @field:NotBlank(message = "Full name is required")
    val fullName: String,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Valid email is required")
    val email: String,
    
    val phoneNumber: String?
)

// Update System Admin profile (name, email, phone). Email uniqueness checked server-side.
data class UpdateSystemAdminRequestDto(
    val fullName: String?,
    val email: String?,
    val phoneNumber: String?
)

data class CreateZoneSupervisorRequestDto(
    @field:NotBlank(message = "Full name is required")
    val fullName: String,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Valid email is required")
    val email: String,
    
    val phoneNumber: String?,

    // Optional initial zone assignments (must belong to exporter making request)
    val zoneIds: List<String>? = null
)

data class UpdateRolePermissionsRequestDto(
    @field:NotBlank(message = "Role ID is required")
    val roleId: String,
    
    @field:NotEmpty(message = "At least one permission is required")
    val permissionNames: List<String>
)

data class SystemAdminResponseDto(
    val id: String,
    val userId: String,
    val fullName: String,
    val email: String?,
    val phoneNumber: String?,
    val status: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    // Present only when a brand‑new user account was created server-side
    val tempPassword: String? = null
)

data class RolePermissionsResponseDto(
    val roleId: String,
    val roleName: String,
    val permissions: List<PermissionDto>
)

data class PermissionDto(
    val id: String,
    val name: String,
    val description: String?
)

data class ExporterSystemAnalyticsDto(
    val totalZones: Int,
    val totalFarmers: Int,
    val activeSystemAdmins: Int,
    val activeZoneSupervisors: Int,
    val zoneBreakdown: List<ZoneAnalyticsDto>
)

data class ZoneAnalyticsDto(
    val zoneId: String,
    val zoneName: String,
    val farmerCount: Int,
    val supervisorCount: Int
)
// Zone Supervisor specific DTOs
data class FarmerLocationResponseDto(
    val farmerId: String,
    val farmerName: String,
    val farmName: String?,
    val location: com.agriconnect.farmersportalapis.domain.common.model.Location?,
    val zoneId: String,
    val zoneName: String,
    val lastPickupDate: LocalDateTime?
)

data class FarmerLocationDto(
    val farmerId: String,
    val farmerName: String,
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val zoneId: String,
    val lastPickupDate: LocalDateTime?
)

// Geospatial Validation DTOs
data class GeospatialValidationResult(
    val isValid: Boolean,
    val distance: BigDecimal,
    val zoneId: String,
    val zoneName: String,
    val farmerLocation: LocationDto,
    val zoneCenter: LocationDto,
    val zoneRadius: BigDecimal,
    val validationMessage: String
)

data class ZoneBoundaryValidationResult(
    val isValid: Boolean,
    val overlaps: List<ZoneOverlapInfo>,
    val suggestedRadius: BigDecimal?,
    val validationMessage: String
)

data class ZoneOverlapInfo(
    val existingZone: ZoneResponseDto,
    val distance: BigDecimal,
    val hasOverlap: Boolean,
    val overlapDistance: BigDecimal,
    val overlapPercentage: Int
)

data class OptimalZoneResult(
    val recommendedZone: ZoneResponseDto,
    val distance: BigDecimal,
    val isWithinBounds: Boolean,
    val alternativeZones: List<AlternativeZoneInfo>,
    val recommendation: String
)

data class AlternativeZoneInfo(
    val zone: ZoneResponseDto,
    val distance: BigDecimal,
    val isWithinBounds: Boolean
)

data class ZoneDistanceInfo(
    val zone: com.agriconnect.farmersportalapis.domain.profile.Zone,
    val distance: BigDecimal,
    val isWithinBounds: Boolean
)

data class CoordinateValidationResult(
    val isValid: Boolean,
    val latitudeValid: Boolean,
    val longitudeValid: Boolean,
    val precisionValid: Boolean,
    val formattedLatitude: String,
    val formattedLongitude: String,
    val validationMessage: String
)

data class LocationDto(
    val latitude: Double?,
    val longitude: Double?,
    val customName: String?
)

// --- Zone Supervisor Management (Overview & Map) ---
data class ZoneSupervisorOverviewDto(
    val id: String,
    val userId: String,
    val fullName: String,
    val email: String?,
    val phoneNumber: String?,
    val status: String,
    val assignedZoneIds: List<String>,
    val assignedZonesCount: Int,
    val farmerCount: Int,
    val earliestPredictedHarvestDate: java.time.LocalDate?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ZoneSupervisorMapZoneDto(
    val zoneId: String,
    val name: String,
    val produceType: String?,
    val centerLatitude: Double,
    val centerLongitude: Double,
    val radiusKm: Double,
    val supervisorIds: List<String>,
    val supervisorCount: Int,
    val farmerCount: Int
)

// Admin License Management DTOs
data class AdminLoginRequestDto(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Valid email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
)

data class AdminLoginResponseDto(
    val adminId: String,
    val userId: String,
    val fullName: String,
    val email: String,
    val role: String?,
    val department: String?,
    val token: String
)

data class LicenseReviewDto(
    val licenseId: String,
    val exporterId: String,
    val exporterName: String,
    val companyName: String?,
    val licenseIdValue: String?,
    val documentUrl: String?,
    val submittedAt: LocalDateTime,
    val verificationStatus: String,
    val reviewerId: String?,
    val reviewedAt: LocalDateTime?
)

data class LicenseReviewRequestDto(
    @field:NotBlank(message = "Decision is required")
    val decision: String, // "APPROVE" or "REJECT"

    val comments: String?
)

data class LicenseReviewResponseDto(
    val licenseId: String,
    val exporterId: String,
    val decision: String,
    val comments: String?,
    val reviewedBy: String,
    val reviewedAt: LocalDateTime,
    val emailSent: Boolean
)

data class LicenseDetailDto(
    val licenseId: String,
    val exporterId: String,
    val exporterName: String,
    val companyName: String?,
    val email: String,
    val phoneNumber: String,
    val licenseIdValue: String?,
    val documentUrl: String?,
    val submittedAt: LocalDateTime,
    val verificationStatus: String,
    val reviewerId: String?,
    val reviewedAt: LocalDateTime?,
    val reviewComments: String?
)

data class AdminProfileDto(
    val adminId: String,
    val userId: String,
    val fullName: String,
    val email: String,
    val role: String?,
    val department: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)