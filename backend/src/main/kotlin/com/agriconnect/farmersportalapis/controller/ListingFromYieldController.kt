package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.ListingFromYieldService
import com.agriconnect.farmersportalapis.application.util.Result
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/listings")
@Tag(name = "Listing Integration", description = "Create and manage listings from yield records")
class ListingFromYieldController(
    private val listingFromYieldService: ListingFromYieldService
) {
    @Operation(summary = "Create a listing from a yield record")
    @PostMapping("/from-yield")
    fun createListingFromYield(@RequestBody dto: CreateListingFromYieldRequestDto) =
        listingFromYieldService.createListingFromYield(dto)

    @Operation(summary = "Check available amount for a yield")
    @GetMapping("/yield/{yieldId}/available-amount")
    fun getYieldAvailableAmount(@PathVariable yieldId: String): Result<YieldAvailableAmountResponseDto> =
        listingFromYieldService.getYieldAvailableAmount(yieldId)

    @Operation(summary = "Update listed amount for a yield (after sale/listing)")
    @PutMapping("/yield/{yieldId}/update-listed-amount")
    fun updateListedAmount(
        @PathVariable yieldId: String,
        @RequestBody dto: UpdateListedAmountRequestDto
    ): Result<YieldRecordResponseDto> =
        listingFromYieldService.updateListedAmount(yieldId, dto)

    @Operation(summary = "Convert a yield to a listing (with business logic)")
    @PostMapping("/yield-to-listing")
    fun yieldToListing(@RequestBody dto: YieldToListingRequestDto) =
        listingFromYieldService.yieldToListing(dto)
}
