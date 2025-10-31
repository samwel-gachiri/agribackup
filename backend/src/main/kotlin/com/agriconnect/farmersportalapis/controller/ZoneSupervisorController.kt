package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.FarmerLocationResponseDto
import com.agriconnect.farmersportalapis.application.dtos.ZoneResponseDto
import com.agriconnect.farmersportalapis.service.common.impl.ZoneSupervisorService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/zone-supervisor-service")
@Tag(name = "Zone Supervisor", description = "Endpoints for zone supervisors to access their data")
class ZoneSupervisorController(
    private val zoneSupervisorService: ZoneSupervisorService
) {

    @Operation(
        summary = "List farmers in supervisor's zones",
        description = "Returns all farmers (with locations) across zones assigned to the authenticated Zone Supervisor"
    )
    @ApiResponse(responseCode = "200", description = "Farmers retrieved successfully")
    @GetMapping("/farmers")
    fun listFarmersInZones(): Result<List<FarmerLocationResponseDto>> {
        val auth = SecurityContextHolder.getContext().authentication
        val userId = auth?.principal as? String
            ?: return ResultFactory.getFailResult("Unauthenticated")
        return zoneSupervisorService.getFarmersInZones(userId)
    }

    @Operation(
        summary = "List assigned zones",
        description = "Returns zones assigned to the authenticated Zone Supervisor"
    )
    @ApiResponse(responseCode = "200", description = "Zones retrieved successfully")
    @GetMapping("/zones/assigned")
    fun listAssignedZones(): Result<List<ZoneResponseDto>> {
        val auth = SecurityContextHolder.getContext().authentication
        val userId = auth?.principal as? String
            ?: return ResultFactory.getFailResult("Unauthenticated")
        return zoneSupervisorService.getAssignedZones(userId)
    }
}
