package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.service.common.impl.ListingOrderService
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
@RequestMapping(path = ["/farmers-service/listing-orders"])
@Tag(name = "Listing Orders management", description = "Manages listing orders of produces")
class ListingOrderController(
    @Autowired var listingOrderService: ListingOrderService
) {
    @Operation(
        summary = "Gives out all orders or a specific buyer",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping
    fun findListingOrderByBuyer(
        @Parameter(description = "Id of Buyer")
        @RequestParam("buyerId", defaultValue = "") buyerId: String,
    ) = listingOrderService.findListingOrderByBuyerId(buyerId)
}