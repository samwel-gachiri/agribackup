package com.agriconnect.farmersportalapis.buyers.controller


import com.agriconnect.farmersportalapis.buyers.application.dtos.AddProducesToBuyerDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.UpdateBuyerDto
import com.agriconnect.farmersportalapis.buyers.application.services.impl.BuyerService
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.domain.profile.Buyer
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/buyers-service/buyer")
@Tag(name = "Buyer Management", description = "APIs for managing buyers")
class BuyerServiceController {
    @Autowired
    lateinit var buyerService: BuyerService

//    @Operation(
//        summary = "Creates a new buyer profile",
//        responses = [ApiResponse(responseCode = "200", description = "OK")]
//    )
//    @PostMapping
//    fun saveProfile(
//        @RequestBody buyer: Buyer
//    ) = buyerService.createBuyer(buyer)

    @Operation(
        summary = "Updates a buyer profile",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @PutMapping
    fun saveProfile(
        @RequestBody updateBuyerDto: UpdateBuyerDto
    ) = buyerService.updateBuyer(updateBuyerDto)
    @Operation(
        summary = "Add list of produce as buyers' produce",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @PostMapping(*["/add-buyer-produce"])
    fun addProducesToBuyer(
        @RequestBody addProducesToBuyerDto: AddProducesToBuyerDto
    ) = buyerService.addProducesToBuyer(addProducesToBuyerDto)

    @Operation(
        summary = "Gives out a list of buyers",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/list")
    fun getBuyers(): Result<List<Buyer>> {
        return buyerService.getBuyers()
    }

    @Operation(
        summary = "Gets out a specific buyer",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping
    fun getBuyer(
        @Parameter(description = "Id of buyer")
        @RequestParam("buyerId", defaultValue = "") buyerId: String,
    ): Result<Buyer> {
        return buyerService.getBuyer(buyerId)
    }

}
