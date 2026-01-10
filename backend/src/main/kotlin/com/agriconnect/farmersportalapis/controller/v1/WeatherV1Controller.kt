package com.agriconnect.farmersportalapis.controller.v1

import com.agriconnect.farmersportalapis.service.common.impl.WeatherResponseDto
import com.agriconnect.farmersportalapis.service.common.impl.WeatherService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for weather-related endpoints
 */
@RestController
@RequestMapping("/api/v1/weather")
@Tag(name = "Weather API", description = "Real-time weather data for farmers")
class WeatherV1Controller(
    private val weatherService: WeatherService
) {

    @Operation(
        summary = "Get weather forecast",
        description = "Returns current weather and 7-day forecast for a specific location. Uses caching.",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping
    fun getWeather(
        @Parameter(description = "Latitude of location")
        @RequestParam(required = false) latitude: Double?,
        @Parameter(description = "Longitude of location")
        @RequestParam(required = false) longitude: Double?
    ): ResponseEntity<WeatherResponseDto> {
        return ResponseEntity.ok(weatherService.getWeatherForecast(latitude, longitude))
    }
}
