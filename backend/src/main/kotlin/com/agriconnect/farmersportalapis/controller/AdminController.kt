package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.AdminService
import com.agriconnect.farmersportalapis.service.common.impl.ExporterService
import com.agriconnect.farmersportalapis.service.common.impl.ZoneSupervisorService
import com.agriconnect.farmersportalapis.application.util.Result
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin-service")
@Tag(name = "Admin Management", description = "APIs for managing System Admins, Zone Supervisors, and their operations")
class AdminController(
    private val adminService: AdminService,
    private val zoneSupervisorService: ZoneSupervisorService,
    private val exporterService: ExporterService
) {
    @Operation(
        summary = "Create a new zone",
        description = "Creates a new operational zone for a System Admin or Exporter"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Zone created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
        ApiResponse(responseCode = "404", description = "Exporter or creator not found")
    ])
    @PostMapping("/zones")
    fun createZone(
        @RequestBody @Valid request: CreateZoneRequestDto
    ): Result<ZoneResponseDto> {
        val (userId, role) = extractUserIdAndRole()
        return adminService.createZone(request, userId, role)
    }

    // --- Newly added zone management & discovery endpoints ---
    @Operation(
        summary = "List zones accessible to current user",
        description = "Returns zones based on role: Exporter -> own zones, System Admin -> all zones, Zone Supervisor -> assigned zones"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Zones listed successfully")
    ])
    @GetMapping("/zones")
    fun listZones(): Result<List<ZoneResponseDto>> {
        val (userId, role) = extractUserIdAndRole()
        return adminService.listZonesForUser(userId, role)
    }

    @Operation(
        summary = "List zones for current user (alias)",
        description = "Alias for /zones providing clarity for dashboards"
    )
    @GetMapping("/zones/my-zones")
    fun listMyZones(): Result<List<ZoneResponseDto>> {
        val (userId, role) = extractUserIdAndRole()
        return adminService.listZonesForUser(userId, role)
    }

    @Operation(
        summary = "List zones for exporter",
        description = "Lists all zones belonging to a specific exporter (exporter or system admin only)"
    )
    @GetMapping("/zones/exporter/{exporterId}")
    fun listZonesByExporter(
        @PathVariable exporterId: String
    ): Result<List<ZoneResponseDto>> {
        val (userId, role) = extractUserIdAndRole()
        return adminService.listZonesByExporter(exporterId, userId, role)
    }

    @Operation(
        summary = "Delete zone",
        description = "Deletes a zone (Exporter can delete own zones; System Admin can delete any)"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Zone deleted successfully"),
        ApiResponse(responseCode = "403", description = "Not authorized to delete zone"),
        ApiResponse(responseCode = "404", description = "Zone not found")
    ])
    @DeleteMapping("/zones/{zoneId}")
    fun deleteZone(
        @PathVariable zoneId: String
    ): Result<String> {
        val (userId, role) = extractUserIdAndRole()
        return adminService.deleteZone(zoneId, userId, role)
    }

    @Operation(
        summary = "Get optimal pickup route preview for a zone",
        description = "Provides an optimized sequence of farmers (preview only, does not persist)"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Route preview generated"),
        ApiResponse(responseCode = "404", description = "Zone or Zone Supervisor not found")
    ])
    @GetMapping("/zones/{zoneId}/optimal-route")
    fun getOptimalRoute(
        @PathVariable zoneId: String
    ): Result<BuyerPickupRouteResponseDto> {
        val (userId, _) = extractUserIdAndRole()
        return zoneSupervisorService.getOptimalPickupRoute(zoneId, userId)
    }

    @Operation(
        summary = "Get zone comments (alias of zone details)",
        description = "Returns zone details including comments for UI convenience"
    )
    @GetMapping("/zones/{zoneId}/comments")
    fun getZoneComments(
        @PathVariable zoneId: String
    ): Result<ZoneResponseDto> {
        val (userId, role) = extractUserIdAndRole()
        return adminService.getZoneDetails(zoneId, userId, role)
    }

    @Operation(
        summary = "Assign Zone Supervisor to zone",
        description = "Assigns a Zone Supervisor to a specific zone"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Zone Supervisor assigned successfully"),
        ApiResponse(responseCode = "404", description = "Zone or Zone Supervisor not found"),
        ApiResponse(responseCode = "400", description = "Zone Supervisor is not active")
    ])
    @PostMapping("/zones/{zoneId}/supervisors")
    fun addZoneSupervisorToZone(
        @Parameter(description = "ID of the zone", required = true)
        @PathVariable zoneId: String,
        @RequestBody @Valid request: AssignZoneSupervisorDto
    ): Result<ZoneResponseDto> {
        return adminService.addZoneSupervisorToZone(zoneId, request)
    }

    @Operation(
        summary = "Add farmer to zone",
        description = "Adds a farmer to a specific zone, creating a Farmer-Exporter relationship"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Farmer added to zone successfully"),
        ApiResponse(responseCode = "404", description = "Zone or farmer not found"),
        ApiResponse(responseCode = "400", description = "Farmer already in zone")
    ])
    @PostMapping("/zones/{zoneId}/farmers")
    fun addFarmerToZone(
        @Parameter(description = "ID of the zone", required = true)
        @PathVariable zoneId: String,
        @RequestBody @Valid request: AddFarmerToZoneDto
    ): Result<FarmerInZoneResponseDto> {
        return adminService.addFarmerToZone(zoneId, request)
    }

    @Operation(
        summary = "Add existing farmer to zone via lookup",
        description = "Looks up a farmer by email or phone and adds them to the zone if not already present"
    )
    @PostMapping("/zones/{zoneId}/farmers/lookup")
    fun addExistingFarmerToZone(
        @PathVariable zoneId: String,
        @RequestBody request: AddExistingFarmerLookupDto
    ): Result<FarmerInZoneResponseDto> = adminService.addExistingFarmerToZone(zoneId, request)

    @Operation(
        summary = "List farmers in a zone",
        description = "Returns all farmers associated with a specific zone (authorized roles only)"
    )
    @GetMapping("/zones/{zoneId}/farmers")
    fun listFarmersInZone(
        @Parameter(description = "ID of the zone", required = true)
        @PathVariable zoneId: String
    ): Result<List<FarmerInZoneResponseDto>> {
        val (userId, role) = extractUserIdAndRole()
        return adminService.listFarmersInZone(zoneId, userId, role)
    }

    // --- System Admin & Zone Supervisor management (creation & listings) ---
    @Operation(
        summary = "Create System Admin",
        description = "Creates a new System Admin (requires MANAGE_SYSTEM_ADMIN permission)"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "System Admin created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input or duplicate email"),
        ApiResponse(responseCode = "403", description = "Not authorized")
    ])
    @PostMapping("/system-admins")
    fun createSystemAdmin(
        @RequestBody @Valid request: CreateSystemAdminRequestDto
    ): Result<SystemAdminResponseDto> = exporterService.createSystemAdmin(request)

    @Operation(
        summary = "Create Zone Supervisor",
        description = "Creates a new Zone Supervisor (requires MANAGE_ZONE_SUPERVISOR permission)"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Zone Supervisor created successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input or duplicate email"),
        ApiResponse(responseCode = "403", description = "Not authorized")
    ])
    @PostMapping("/zone-supervisors")
    fun createZoneSupervisor(
        @RequestBody @Valid request: CreateZoneSupervisorRequestDto
    ): Result<ZoneSupervisorResponseDto> = exporterService.createZoneSupervisor(request)

    @Operation(
        summary = "List active System Admins",
        description = "Returns all active System Admin accounts"
    )
    @GetMapping("/system-admins")
    fun listSystemAdmins(): Result<List<SystemAdminResponseDto>> = exporterService.listSystemAdmins()

        @Operation(summary = "Search System Admins", description = "Search by name, email, phone, or ID (active only)")
        @GetMapping("/system-admins/search")
        fun searchSystemAdmins(@RequestParam("q") q: String): Result<List<SystemAdminResponseDto>> = exporterService.searchSystemAdmins(q)

        @Operation(summary = "Get System Admin details", description = "Fetch details for a specific System Admin")
        @GetMapping("/system-admins/{systemAdminId}")
        fun getSystemAdminDetails(@PathVariable systemAdminId: String): Result<SystemAdminResponseDto> = exporterService.getSystemAdminDetails(systemAdminId)

        @Operation(summary = "Update System Admin", description = "Update basic profile info (name/email/phone)")
        @PutMapping("/system-admins/{systemAdminId}")
        fun updateSystemAdmin(
            @PathVariable systemAdminId: String,
            @RequestBody request: UpdateSystemAdminRequestDto
        ): Result<SystemAdminResponseDto> = exporterService.updateSystemAdmin(systemAdminId, request)

        @Operation(summary = "Reactivate System Admin", description = "Set status back to ACTIVE")
        @PostMapping("/system-admins/{systemAdminId}/reactivate")
        fun reactivateSystemAdmin(@PathVariable systemAdminId: String): Result<SystemAdminResponseDto> = exporterService.reactivateSystemAdmin(systemAdminId)

    @Operation(
        summary = "List active Zone Supervisors",
        description = "Returns all active Zone Supervisor accounts"
    )
    @GetMapping("/zone-supervisors")
    fun listZoneSupervisors(): Result<List<ZoneSupervisorResponseDto>> = exporterService.listZoneSupervisors()

    @Operation(
        summary = "Deactivate System Admin",
        description = "Soft-deactivates a System Admin (status -> INACTIVE)"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "System Admin deactivated successfully"),
        ApiResponse(responseCode = "404", description = "System Admin not found"),
        ApiResponse(responseCode = "403", description = "Not authorized")
    ])
    @DeleteMapping("/system-admins/{systemAdminId}")
    fun deleteSystemAdmin(
        @PathVariable systemAdminId: String
    ): Result<String> = exporterService.deleteSystemAdmin(systemAdminId)

    @Operation(
        summary = "Deactivate Zone Supervisor",
        description = "Soft-deactivates a Zone Supervisor (status -> INACTIVE)"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Zone Supervisor deactivated successfully"),
        ApiResponse(responseCode = "404", description = "Zone Supervisor not found"),
        ApiResponse(responseCode = "403", description = "Not authorized")
    ])
    @DeleteMapping("/zone-supervisors/{zoneSupervisorId}")
    fun deleteZoneSupervisor(
        @PathVariable zoneSupervisorId: String
    ): Result<String> = exporterService.deleteZoneSupervisor(zoneSupervisorId)

    @Operation(
        summary = "Get Zone Supervisor details",
        description = "Retrieves details of a Zone Supervisor, including assigned zones"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Zone Supervisor retrieved successfully"),
        ApiResponse(responseCode = "404", description = "Zone Supervisor not found")
    ])
    @GetMapping("/zone-supervisors/{zoneSupervisorId}")
    fun getZoneSupervisorDetails(
        @Parameter(description = "ID of the Zone Supervisor", required = true)
        @PathVariable zoneSupervisorId: String
    ): Result<ZoneSupervisorResponseDto> {
        return adminService.getZoneSupervisorDetails(zoneSupervisorId)
    }

    @Operation(summary = "Zone Supervisor Overviews", description = "Aggregated metrics per Zone Supervisor (zones, farmers, earliest harvest)")
    @GetMapping("/zone-supervisors-overview")
    fun listZoneSupervisorOverviews(): Result<List<ZoneSupervisorOverviewDto>> = adminService.listZoneSupervisorOverviews()

    @Operation(summary = "Zone Supervisor Map Data", description = "Zones + supervisor assignment & farmer counts for map rendering")
    @GetMapping("/zone-supervisors-map-data")
    fun getZoneSupervisorMapData(): Result<Map<String, Any>> = adminService.getZoneSupervisorMapData()

    @Operation(summary = "Unassign Zone Supervisor from Zone", description = "Removes a zone assignment from a supervisor")
    @DeleteMapping("/zones/{zoneId}/supervisors/{zoneSupervisorId}")
    fun unassignZoneSupervisor(
        @PathVariable zoneId: String,
        @PathVariable zoneSupervisorId: String
    ): Result<ZoneResponseDto> = adminService.unassignZoneSupervisorFromZone(zoneId, zoneSupervisorId)

    @Operation(
        summary = "Get zone details",
        description = "Retrieves details of a zone, including comments, for authorized users (Exporters, System Admins, or Farmers in the zone)"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Zone retrieved successfully"),
        ApiResponse(responseCode = "404", description = "Zone or user not found"),
        ApiResponse(responseCode = "403", description = "User not authorized for this zone")
    ])
    @GetMapping("/zones/{zoneId}")
    fun getZoneDetails(
        @Parameter(description = "ID of the zone", required = true)
        @PathVariable zoneId: String
    ): Result<ZoneResponseDto> {
        val (userId, role) = extractUserIdAndRole()
        return adminService.getZoneDetails(zoneId, userId, role)
    }

    @Operation(
        summary = "Add or update zone comment",
        description = "Allows a Zone Supervisor to add or update a comment on a zone they are assigned to"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Zone comment updated successfully"),
        ApiResponse(responseCode = "404", description = "Zone or Zone Supervisor not found"),
        ApiResponse(responseCode = "403", description = "Zone Supervisor not assigned to this zone")
    ])
    @PutMapping("/zones/{zoneId}/comments")
    fun addZoneComment(
        @Parameter(description = "ID of the zone", required = true)
        @PathVariable zoneId: String,
        @RequestBody @Valid request: UpdateZoneCommentDto
    ): Result<ZoneResponseDto> {
        val (userId, _) = extractUserIdAndRole()
        return adminService.addZoneComment(zoneId, request, userId)
    }

    @Operation(
        summary = "Edit farmer details",
        description = "Allows a Zone Supervisor to edit a farmer's details with consent"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Farmer details updated successfully"),
        ApiResponse(responseCode = "404", description = "Farmer or Zone Supervisor not found"),
        ApiResponse(responseCode = "403", description = "Zone Supervisor not authorized or consent not provided")
    ])
    @PutMapping("/farmers/{farmerId}")
    fun editFarmerDetails(
        @Parameter(description = "ID of the farmer", required = true)
        @PathVariable farmerId: String,
        @RequestBody @Valid request: UpdateFarmerRequestDto
    ): Result<FarmerResponseDto> {
        val (userId, _) = extractUserIdAndRole()
        return adminService.editFarmerDetails(farmerId, request, userId)
    }

    @Operation(
        summary = "Schedule a pickup",
        description = "Allows a Zone Supervisor to schedule a pickup for a farmer in their zone"
    )
    @ApiResponses(value = [
        ApiResponse(responseCode = "200", description = "Pickup scheduled successfully"),
        ApiResponse(responseCode = "400", description = "Invalid input"),
        ApiResponse(responseCode = "404", description = "Farmer, exporter, or produce listing not found"),
        ApiResponse(responseCode = "403", description = "Zone Supervisor not authorized")
    ])
    @PostMapping("/pickup-schedules")
    fun schedulePickup(
        @RequestBody @Valid request: SchedulePickupRequestDto
    ): Result<PickupScheduleResponseDto> {
        val (userId, _) = extractUserIdAndRole()
        return adminService.schedulePickup(request, userId)
    }

    // --- helpers ---
    private fun extractUserIdAndRole(): Pair<String, String> {
        val auth = SecurityContextHolder.getContext().authentication
        val userId = auth?.principal as? String ?: ""
        val role = auth?.authorities
            ?.firstOrNull { it.authority.startsWith("ROLE_") }
            ?.authority?.removePrefix("ROLE_") ?: "UNKNOWN"
        return userId to role
    }
}