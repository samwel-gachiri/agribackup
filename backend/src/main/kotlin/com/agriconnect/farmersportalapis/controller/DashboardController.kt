package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.service.common.impl.DashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/farmers-service/api/dashboard")
@Tag(name = "Farmer Dashboard management", description = "APIs for giving data to the farmers dashboard")
class DashboardController(
    @Autowired val dashboardService: DashboardService
) {
    @GetMapping("/live/count")
    @Operation(summary = "Get live count summary", description = "Retrieve a count of active listings", responses = [ApiResponse(responseCode = "200", description = "OK")])
    fun getLiveCount(
        @Parameter(description = "Id of farmer")
        @RequestParam("farmerId", defaultValue = "") farmerId: String
    ) = dashboardService.getLiveService(farmerId)

    @Operation(summary = "Gets the sales report")
    @GetMapping("/sales-report")
    fun getSalesReport(@RequestParam farmerId: String) = ResponseEntity.ok(dashboardService.getSalesReport(farmerId))
}