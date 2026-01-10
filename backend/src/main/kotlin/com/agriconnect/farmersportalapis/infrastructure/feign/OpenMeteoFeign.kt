package com.agriconnect.farmersportalapis.infrastructure.feign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Feign client for Open-Meteo Weather API
 * Free for non-commercial use, up to 10,000 daily API calls.
 * No API key required.
 * Docs: https://open-meteo.com/en/docs
 */
@FeignClient(
    name = "openMeteoClient",
    url = "https://api.open-meteo.com/v1"
)
interface OpenMeteoFeign {

    @GetMapping("/forecast")
    fun getForecast(
        @RequestParam("latitude") latitude: Double,
        @RequestParam("longitude") longitude: Double,
        @RequestParam("current") current: String = "temperature_2m,weather_code,relative_humidity_2m,wind_speed_10m",
        @RequestParam("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum",
        @RequestParam("timezone") timezone: String = "auto",
        @RequestParam("forecast_days") forecastDays: Int = 7
    ): OpenMeteoResponse
}

data class OpenMeteoResponse(
    val latitude: Double?,
    val longitude: Double?,
    val current: CurrentWeather?,
    val daily: DailyWeather?
)

data class CurrentWeather(
    val time: String?,
    val interval: Int?,
    val temperature_2m: Double?,
    val weather_code: Int?,
    val relative_humidity_2m: Int?,
    val wind_speed_10m: Double?
)

data class DailyWeather(
    val time: List<String>?,
    val weather_code: List<Int>?,
    val temperature_2m_max: List<Double>?,
    val temperature_2m_min: List<Double>?,
    val precipitation_sum: List<Double>?
)
