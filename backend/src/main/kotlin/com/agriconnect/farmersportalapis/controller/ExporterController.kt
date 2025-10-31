package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.ExporterService
import com.agriconnect.farmersportalapis.application.util.Result
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/exporters-service/exporter")
@Tag(name = "Exporter Management", description = "APIs for managing exporter profiles and verification")
@SecurityRequirement(name = "bearerAuth")
class ExporterController(
    private val exporterService: ExporterService,
    private val adminService: com.agriconnect.farmersportalapis.service.common.impl.AdminService
) {
    @Operation(
        summary = "Get exporter details",
        description = "Retrieves detailed information about an exporter by their ID"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Exporter details retrieved successfully"),
        ApiResponse(responseCode = "404", description = "Exporter not found")
    ])
    @GetMapping("/{exporterId}")
    fun getExporter(
        @Parameter(description = "ID of the exporter", required = true)
        @PathVariable exporterId: String
    ): Result<ExporterResponseDto> {
        return exporterService.getExporter(exporterId)
    }

    @Operation(
        summary = "Update exporter profile",
        description = "Updates an existing exporter's information"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Exporter updated successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
        ApiResponse(responseCode = "404", description = "Exporter not found"),
        ApiResponse(responseCode = "403", description = "Unauthorized to update this exporter")
    ])
    @PutMapping("/{exporterId}")
    fun updateExporter(
        @Parameter(description = "ID of the exporter to update", required = true)
        @PathVariable exporterId: String,
        @RequestBody @Valid request: UpdateExporterRequestDto
    ): Result<ExporterResponseDto> {
        return exporterService.updateExporter(exporterId, request)
    }

    @Operation(
        summary = "Verify an exporter",
        description = "Changes exporter's verification status to VERIFIED"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Exporter verified successfully"),
        ApiResponse(responseCode = "404", description = "Exporter not found"),
        ApiResponse(responseCode = "403", description = "Unauthorized to verify exporters")
    ])
    @PutMapping("/{exporterId}/verify")
    fun verifyExporter(
        @Parameter(description = "ID of the exporter to verify", required = true)
        @PathVariable exporterId: String
    ): Result<ExporterResponseDto> {
        return exporterService.verifyExporter(exporterId)
    }

    // --- Alias endpoints for role management (mirrors AdminController) to keep backward compatibility with older frontend paths ---
    @Operation(summary = "(Alias) Create System Admin under exporter namespace")
    @PostMapping("/system-admins")
    fun createSystemAdminAlias(@RequestBody @Valid request: CreateSystemAdminRequestDto): Result<SystemAdminResponseDto> =
        exporterService.createSystemAdmin(request)

    @Operation(summary = "(Alias) Create Zone Supervisor under exporter namespace")
    @PostMapping("/zone-supervisors")
    fun createZoneSupervisorAlias(@RequestBody @Valid request: CreateZoneSupervisorRequestDto): Result<ZoneSupervisorResponseDto> =
        exporterService.createZoneSupervisor(request)

    // --- Alias zone endpoints (backward compatibility) ---
    @Operation(summary = "(Alias) List exporter zones")
    @GetMapping("/{exporterId}/zones")
    fun listExporterZonesAlias(@PathVariable exporterId: String): Result<List<ZoneResponseDto>> {
        // Reuse adminService authorization logic
        return adminService.listZonesByExporter(exporterId, exporterId, "EXPORTER")
    }

    @Operation(summary = "(Alias) Add farmer to zone")
    @PostMapping("/zones/{zoneId}/farmers/{farmerId}")
    fun addFarmerToZoneAlias(
        @PathVariable zoneId: String,
        @PathVariable farmerId: String
    ): Result<FarmerInZoneResponseDto> {
        return adminService.addFarmerToZone(zoneId, AddFarmerToZoneDto(farmerId))
    }

    @Operation(summary = "(Alias) List farmers in zone")
    @GetMapping("/zones/{zoneId}/farmers")
    fun listFarmersInZoneAlias(@PathVariable zoneId: String): Result<List<FarmerInZoneResponseDto>> {
        // Limited auth context; treat as exporter-owned access; real auth enforced via security filters
        return adminService.listFarmersInZone(zoneId, "", "SYSTEM_ADMIN")
    }

    @Operation(
        summary = "Submit license ID for verification",
        description = "Allows an exporter to submit their license ID for verification"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "License ID submitted successfully"),
        ApiResponse(responseCode = "400", description = "Invalid license ID"),
        ApiResponse(responseCode = "404", description = "Exporter not found")
    ])
    @PostMapping("/submit-license")
    fun submitLicense(@RequestBody @Valid request: SubmitLicenseRequestDto): Result<ExporterResponseDto> {
        return exporterService.submitLicense(request)
    }

    @Operation(
        summary = "Submit license ID and document for verification",
        description = "Allows an exporter to submit their license ID and export license document for verification"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "License and document submitted successfully"),
        ApiResponse(responseCode = "400", description = "Invalid license ID or document"),
        ApiResponse(responseCode = "404", description = "Exporter not found")
    ])
    @PostMapping("/submit-license-document", consumes = [org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE])
    fun submitLicenseWithDocument(
        @RequestParam("licenseId", required = false) licenseId: String?,
        @RequestParam("document") document: org.springframework.web.multipart.MultipartFile
    ): Result<ExporterResponseDto> {
        val request = SubmitLicenseWithDocumentRequestDto(licenseId)
        return exporterService.submitLicenseWithDocument(request, document)
    }
}