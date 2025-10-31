package com.agriconnect.farmersportalapis.buyers.controller

import com.agriconnect.farmersportalapis.buyers.application.services.impl.BSDashboardService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/buyers-service/api/dashboard")
@Tag(name = "Buyer Dashboard management", description = "APIs for giving data to the buyers dashboard")
class BuyerDashboardController(
    @Autowired val BSDashboardService: BSDashboardService
) {
    @GetMapping("/live/count")
    @Operation(summary = "Get live count summary", description = "Retrieve a count of active requests", responses = [ApiResponse(responseCode = "200", description = "OK")])
    fun getBiddingCountSummary(
        @Parameter(description = "Id of buyer")
        @RequestParam("buyerId", defaultValue = "") buyerId: String
    ) = BSDashboardService.getLiveService(buyerId)
}