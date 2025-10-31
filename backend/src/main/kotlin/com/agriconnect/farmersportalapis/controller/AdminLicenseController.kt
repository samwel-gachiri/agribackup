package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.application.dtos.auth.LoginDto
import com.agriconnect.farmersportalapis.service.common.impl.AdminLicenseService
import com.agriconnect.farmersportalapis.service.common.impl.AuthService
import com.agriconnect.farmersportalapis.application.util.JwtUtil
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin License Management", description = "APIs for admin license review and approval")
class AdminLicenseController(
    private val adminLicenseService: AdminLicenseService,
    private val authService: AuthService,
    private val jwtUtil: JwtUtil
) {

    @Operation(
        summary = "Admin login",
        description = "Authenticate admin user for admin subdomain access"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Admin authenticated successfully"),
        ApiResponse(responseCode = "401", description = "Invalid credentials"),
        ApiResponse(responseCode = "403", description = "Not authorized as admin")
    ])
    @PostMapping("/login")
    fun adminLogin(@RequestBody @Valid request: AdminLoginRequestDto): Result<AdminLoginResponseDto> {
        // Convert AdminLoginRequestDto to LoginDto for AuthService
        val loginDto = LoginDto(
            emailOrPhone = request.email,
            password = request.password,
            roleType = com.agriconnect.farmersportalapis.domain.auth.RoleType.ADMIN
        )

        val authResult = authService.login(loginDto)

        return if (authResult.success && authResult.data != null) {
            val loginData = authResult.data
            val adminData = loginData.roleSpecificData as? com.agriconnect.farmersportalapis.application.dtos.auth.AdminLoginDto

            val adminResponse = AdminLoginResponseDto(
                adminId = adminData?.id ?: "",
                userId = adminData?.userId ?: "",
                fullName = adminData?.fullName ?: "",
                email = request.email,
                role = adminData?.role ?: "ADMIN",
                department = adminData?.department,
                token = loginData.token ?: ""
            )

            ResultFactory.getSuccessResult(adminResponse, "Admin login successful")
        } else {
            ResultFactory.getFailResult(authResult.msg ?: "Login failed")
        }
    }

    @Operation(
        summary = "Get licenses pending review",
        description = "Retrieve all exporter licenses that are pending admin review"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Licenses retrieved successfully")
    ])
    @GetMapping("/licenses/pending")
    fun getPendingLicenses(): Result<List<LicenseReviewDto>> {
        val adminId = extractAdminId()
        return adminLicenseService.getPendingLicenses(adminId)
    }

    @Operation(
        summary = "Review and approve license",
        description = "Approve or reject an exporter's license application"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "License reviewed successfully"),
        ApiResponse(responseCode = "404", description = "License or exporter not found"),
        ApiResponse(responseCode = "403", description = "Not authorized to review licenses")
    ])
    @PostMapping("/licenses/{licenseId}/review")
    fun reviewLicense(
        @Parameter(description = "ID of the license to review", required = true)
        @PathVariable licenseId: String,
        @RequestBody @Valid request: LicenseReviewRequestDto
    ): Result<LicenseReviewResponseDto> {
        val adminId = extractAdminId()
        return adminLicenseService.reviewLicense(licenseId, request, adminId)
    }

    @Operation(
        summary = "Get license details",
        description = "Get detailed information about a specific license for review"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "License details retrieved successfully"),
        ApiResponse(responseCode = "404", description = "License not found")
    ])
    @GetMapping("/licenses/{licenseId}")
    fun getLicenseDetails(
        @Parameter(description = "ID of the license", required = true)
        @PathVariable licenseId: String
    ): Result<LicenseDetailDto> {
        val adminId = extractAdminId()
        return adminLicenseService.getLicenseDetails(licenseId, adminId)
    }

    @Operation(
        summary = "Get admin profile",
        description = "Get current admin user's profile information"
    )
    @GetMapping("/profile")
    fun getAdminProfile(): Result<AdminProfileDto> {
        val adminId = extractAdminId()
        return adminLicenseService.getAdminProfile(adminId)
    }

    private fun extractAdminId(): String {
        val auth = SecurityContextHolder.getContext().authentication
        return auth?.principal as? String ?: ""
    }
}