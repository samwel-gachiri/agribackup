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

data class BuyerLocationDto(
    val latitude: Double,
    val longitude: Double,
    val customName: String
)

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

data class AddBuyerProduceDto(
    val name: String
)


// Login DTO
data class LoginDto(
    val emailOrPhone: String,
    val password: String,
    val roleType: RoleType
)

// Login Response DTO
data class LoginResponseDto(
    val token: String? = null, // JWT token if you're using JWT
    val roleSpecificData: Any? = null // Could be Farmer, Buyer, etc
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