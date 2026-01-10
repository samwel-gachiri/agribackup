package com.agriconnect.farmersportalapis.application.dtos.auth

import com.agriconnect.farmersportalapis.application.dtos.AddFarmerProduceDto
import com.agriconnect.farmersportalapis.domain.auth.RoleType
import com.agriconnect.farmersportalapis.domain.common.model.Location

data class UserRegistrationDto(
        val email: String?,
        val phoneNumber: String?,
        val password: String,
        val fullName: String
)

data class FarmerRegistrationDto(
        val user: UserRegistrationDto,
        val farmName: String?,
        val farmSize: Double?,
        val location: Location?,
        val farmerProduces: List<AddFarmerProduceDto> = mutableListOf()
)

data class BuyerRegistrationDto(
        val user: UserRegistrationDto,
        val companyName: String?,
        val businessType: String?,
        val location: BuyerLocationDto?,
        val preferredProduces: List<AddBuyerProduceDto> = mutableListOf()
)

data class BuyerLocationDto(val latitude: Double, val longitude: Double, val customName: String)

data class ExporterRegistrationDto(
        val user: UserRegistrationDto,
        val licenseId: String? = null,
        val companyName: String?,
        val companyDesc: String?
)

data class AggregatorRegistrationDto(
        val user: UserRegistrationDto,
        val organizationName: String,
        val organizationType: String?,
        val registrationNumber: String?,
        val facilityAddress: String?,
        val storageCapacityTons: Double?,
        val collectionRadiusKm: Double?,
        val primaryCommodities: List<String>?,
        val certificationDetails: String?
)

data class ProcessorRegistrationDto(
        val user: UserRegistrationDto,
        val facilityName: String,
        val facilityAddress: String?,
        val processingCapabilities: String?,
        val certifications: String?
)

data class ImporterRegistrationDto(
        val user: UserRegistrationDto,
        val companyName: String,
        val importLicenseNumber: String?,
        val companyAddress: String?,
        val destinationCountry: String?,
        val destinationPort: String?,
        val importCategories: List<String>?,
        val eudrComplianceOfficer: String?,
        val certificationDetails: String?
)

/**
 * EUDR Article 6 - Authorised Representative Registration
 * ARs must have EORI number and be established in an EU member state
 */
data class AuthorisedRepresentativeRegistrationDto(
        val user: UserRegistrationDto,
        val eoriNumber: String,
        val companyName: String,
        val euMemberState: String,
        val registrationNumber: String,
        val vatNumber: String,
        val contactPhone: String,
        val businessAddress: String
)

data class AddBuyerProduceDto(val name: String)

// Role-specific detail DTOs for signup
data class FarmerDetailsDto(
    val farmName: String? = null,
    val farmSize: String? = null,
    val location: LocationDto? = null
)

data class ExporterDetailsDto(
    val companyName: String? = null,
    val originCountry: String? = null
)

data class BuyerDetailsDto(
    val businessName: String? = null,
    val location: LocationDto? = null
)

data class ImporterDetailsDto(
    val businessName: String? = null,
    val location: LocationDto? = null
)

data class SupplierDetailsDto(
    val businessName: String? = null,
    val location: LocationDto? = null
)

data class ARDetailsDto(
    val eoriNumber: String? = null,
    val companyName: String? = null,
    val euMemberState: String? = null,
    val registrationNumber: String? = null,
    val vatNumber: String? = null,
    val businessAddress: String? = null
)

data class LocationDto(
    val customName: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)

data class RoleDetailsDto(
    val FARMER: FarmerDetailsDto? = null,
    val EXPORTER: ExporterDetailsDto? = null,
    val BUYER: BuyerDetailsDto? = null,
    val IMPORTER: ImporterDetailsDto? = null,
    val SUPPLIER: SupplierDetailsDto? = null,
    val AUTHORISED_REPRESENTATIVE: ARDetailsDto? = null
)

// Assign Roles DTO
data class AssignRolesDto(
    val userId: String, 
    val roles: List<RoleType>,
    val supplierType: String? = null, // Optional: AGGREGATOR, PROCESSOR, DISTRIBUTOR, etc.
    val roleDetails: RoleDetailsDto? = null // Role-specific details from signup
)

// Login DTO
data class LoginDto(
        val emailOrPhone: String,
        val password: String,
        val roleType: RoleType? = null // Optional for smart role detection
)

// Role Option for multi-role selection
data class RoleOption(val roleType: String, val displayName: String, val description: String?)

// Login Response DTO
data class LoginResponseDto(
        val token: String? = null, // JWT token if you're using JWT
        val roleSpecificData: Any? = null, // Could be Farmer, Buyer, etc
        val requiresRoleSelection: Boolean = false, // True if user has multiple roles
        val availableRoles: List<RoleOption>? = null // Available roles for selection
)

// Slim login role payloads to avoid cyclic/heavy JPA entity graphs in auth responses
data class BuyerLoginDto(
        val id: String,
        val userId: String,
        val fullName: String,
        val email: String?,
        val phoneNumber: String?,
        val companyName: String?,
        val businessType: String?
)

data class ExporterLoginDto(
        val id: String,
        val userId: String,
        val fullName: String,
        val email: String?,
        val phoneNumber: String?,
        val licenseId: String?,
        val verificationStatus: String,
        val companyName: String?,
        val companyDesc: String?
)

data class AggregatorLoginDto(
        val id: String,
        val userId: String,
        val fullName: String,
        val email: String?,
        val phoneNumber: String?,
        val organizationName: String,
        val verificationStatus: String,
        val hederaAccountId: String?
)

data class ProcessorLoginDto(
        val id: String,
        val userId: String,
        val fullName: String,
        val email: String?,
        val phoneNumber: String?,
        val facilityName: String,
        val verificationStatus: String,
        val hederaAccountId: String?
)

data class ImporterLoginDto(
        val id: String,
        val userId: String,
        val fullName: String,
        val email: String?,
        val phoneNumber: String?,
        val companyName: String,
        val verificationStatus: String,
        val hederaAccountId: String?
)

data class SystemAdminLoginDto(
        val id: String,
        val userId: String,
        val fullName: String,
        val email: String?,
        val phoneNumber: String?,
        val status: String
)

data class ZoneSupervisorLoginDto(
        val id: String,
        val userId: String,
        val fullName: String,
        val email: String?,
        val phoneNumber: String?,
        val status: String
)

data class AdminLoginDto(
        val id: String,
        val userId: String,
        val fullName: String,
        val email: String?,
        val phoneNumber: String?,
        val role: String?,
        val department: String?
)

data class AuthorisedRepresentativeLoginDto(
        val id: String,
        val userId: String,
        val fullName: String,
        val email: String?,
        val phoneNumber: String?,
        val eoriNumber: String?,
        val companyName: String?,
        val euMemberState: String?,
        val isVerified: Boolean,
        val isAcceptingMandates: Boolean
)

data class GoogleLoginDto(
        val credential: String,
        val portalContext: String? = null,
        val selectedRole: String? = null // For completing multi-role login
)
