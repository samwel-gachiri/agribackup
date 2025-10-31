package com.agriconnect.farmersportalapis.buyers.controller

import com.agriconnect.farmersportalapis.buyers.application.services.impl.RequestOrderService
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
@RequestMapping(path = ["/buyers-service/request-orders"])
@Tag(name = "Request Orders management", description = "Manages request orders of produces")
class RequestOrderController(
    @Autowired var requestOrderService: RequestOrderService
) {
    @Operation(
        summary = "Gives out all orders or a specific farmer",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping
    fun findListingOrderByFarmer(
        @Parameter(description = "Id of Farmer")
        @RequestParam("farmerId", defaultValue = "") farmerId: String,
    ) = requestOrderService.findRequestOrderByFarmerId(farmerId)
}