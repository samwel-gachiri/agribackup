package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.BuyerFarmerSearchRequestDto
import com.agriconnect.farmersportalapis.application.dtos.FarmerSearchResultDto
import com.agriconnect.farmersportalapis.service.common.impl.BuyerFarmerManagementService
import com.agriconnect.farmersportalapis.service.common.impl.YieldRecordingService
import com.agriconnect.farmersportalapis.service.supplychain.FlexibleSupplyChainService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/farmers")
@Tag(name = "Farmer", description = "APIs for farmer operations")
class FarmerController(
    private val buyerFarmerManagementService: BuyerFarmerManagementService,
    private val yieldRecordingService: YieldRecordingService,
    private val flexibleSupplyChainService: FlexibleSupplyChainService
) {

    @Operation(
        summary = "Search farmers by phone number or name",
        description = "Search for available farmers that buyers can connect with. Supports phone number or name search.",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/search")
    fun searchFarmers(
        @Parameter(description = "Phone number or name to search for")
        @RequestParam("phone", required = false) phone: String?,
        @Parameter(description = "Name to search for (alternative to phone)")
        @RequestParam("name", required = false) name: String?,
        authentication: Authentication
    ): ResponseEntity<List<FarmerSearchResultDto>> {
        // Get buyer ID from authentication context
        val buyerId = authentication.name // Assuming buyer ID is stored as username

        // Use phone parameter if provided, otherwise use name
        val searchQuery = phone ?: name ?: ""

        val request = BuyerFarmerSearchRequestDto(
            buyerId = buyerId,
            searchQuery = searchQuery,
            location = null,
            maxDistance = null,
            produceTypes = null
        )

        val farmers = buyerFarmerManagementService.searchAvailableFarmers(request)
        return ResponseEntity.ok(farmers)
    }

    @Operation(
        summary = "Get farmer's produces for yield recording",
        description = "Get all produces for a farmer that can be used for yield recording",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/{farmerId}/produces/for-yield-recording")
    fun getFarmerProducesForYieldRecording(
        @Parameter(description = "Id of farmer")
        @PathVariable farmerId: String
    ): ResponseEntity<*> {
        val result = yieldRecordingService.getFarmerProducesForYieldRecording(farmerId)
        return if (result.success) {
            ResponseEntity.ok(result.data)
        } else {
            ResponseEntity.badRequest().body(result.msg + result.data)
        }
    }

    @Operation(
        summary = "Get connected suppliers for a farmer",
        description = "Get aggregators, cooperatives and farmer groups that a farmer can send produce to",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/{farmerId}/connected-suppliers")
    fun getConnectedSuppliers(
        @Parameter(description = "Id of farmer")
        @PathVariable farmerId: String
    ): ResponseEntity<Any> {
        // Include all supplier types that farmers can send produce to
        val supplierTypes = listOf(
            "AGGREGATOR", "COOPERATIVE", "FARMER_GROUP", 
            "PROCESSOR", "TRADER", "WAREHOUSE", "EXPORTER"
        )
        val suppliers = flexibleSupplyChainService.getSuppliersAcceptingFromFarmers(farmerId, supplierTypes)
        return ResponseEntity.ok(suppliers)
    }
}