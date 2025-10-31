package com.agriconnect.farmersportalapis.controller
import com.agriconnect.farmersportalapis.application.dtos.UpdateLocationRequestDto
import com.agriconnect.farmersportalapis.service.common.impl.LocationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/farmers-service/location")
@Tag(name = "Farmer location Management", description = "APIs for managing farmers' locations")
class LocationServiceController {
    @Autowired
    lateinit var locationService: LocationService

    @Operation(
        summary = "Gets the location of a certain farmer",
        responses = [io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping(path = ["/farmer"])
    fun findFarmerLocation(
        @Parameter(description = "Id of farmer")
        @RequestParam("farmerId", defaultValue = "")
        farmerId: String,
    ) = locationService.getFarmerLocation(farmerId)

    @Operation(
        summary = "Gets the farmers near you together with their location",
        responses = [io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping(path = ["/farmers"])
    fun getFarmersNearYou(
        @Parameter(description = "Latitude of your location")
        @RequestParam("latitude") latitude: Double,
        @Parameter(description = "Longitude of your location")
        @RequestParam("longitude") longitude: Double,
        @Parameter(description = "Maximum distance in kilometers")
        @RequestParam("maxDistance", defaultValue = "10.0") maxDistance: Double
    ) = locationService.getFarmersNearYou(latitude, longitude, maxDistance)

    @Operation(
        summary = "Updates the location of a farmer",
        responses = [io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")]
    )
    @PutMapping(path = ["/farmer"])
    fun updateLocation(
        @RequestBody updateLocationRequestDto: UpdateLocationRequestDto
    ) = locationService.updateLocation(updateLocationRequestDto)


}