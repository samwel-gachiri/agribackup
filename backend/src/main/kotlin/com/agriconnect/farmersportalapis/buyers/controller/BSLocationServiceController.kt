package com.agriconnect.farmersportalapis.buyers.controller
import com.agriconnect.farmersportalapis.buyers.application.dtos.UpdateLocationRequestDto
import com.agriconnect.farmersportalapis.buyers.application.services.impl.BSLocationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/buyers-service/location")
@Tag(name = "Buyer location Management", description = "APIs for managing buyers' locations")
class BSLocationServiceController {
    @Autowired
    lateinit var BSLocationService: BSLocationService

    @Operation(
        summary = "Gets the location of a certain buyer",
        responses = [io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping(path = ["/buyer"])
    fun findBuyerLocation(
        @Parameter(description = "Id of buyer")
        @RequestParam("buyerId", defaultValue = "") buyerId: String,
    ) = BSLocationService.getBuyerLocation(buyerId)

    @Operation(
        summary = "Gets the buyers near you together with their location",
        responses = [io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")]
    )
    
    @GetMapping(path = ["/buyers"])
    fun getBuyersNearYou(
        @Parameter(description = "Latitude of your location")
        @RequestParam("latitude") latitude: Double,
        @Parameter(description = "Longitude of your location")
        @RequestParam("longitude") longitude: Double,
        @Parameter(description = "Maximum distance in kilometers")
        @RequestParam("maxDistance", defaultValue = "10.0") maxDistance: Double
    ) = BSLocationService.getBuyersNearYou(latitude, longitude, maxDistance)

    @Operation(
        summary = "Updates the location of a buyer",
        responses = [io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")]
    )
    @PutMapping(path = ["/buyer"])
    fun updateLocation(
        @RequestBody updateLocationRequestDto: UpdateLocationRequestDto
    ) = BSLocationService.updateLocation(updateLocationRequestDto)


}