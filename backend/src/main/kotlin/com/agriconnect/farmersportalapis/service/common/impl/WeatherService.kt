package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.infrastructure.feign.OpenMeteoFeign
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class WeatherService(
    private val openMeteoFeign: OpenMeteoFeign
) {
    private val logger = LoggerFactory.getLogger(WeatherService::class.java)

    /**
     * Get weather forecast for a specific location
     * Defaults to Nairobi coordinates if not provided
     */
    @Cacheable(value = ["weatherCache"], key = "#latitude + ',' + #longitude")
    fun getWeatherForecast(latitude: Double?, longitude: Double?): WeatherResponseDto {
        // Default to Nairobi if coordinates missing
        val lat = latitude ?: -1.2921
        val lon = longitude ?: 36.8219
        
        try {
            val response = openMeteoFeign.getForecast(
                latitude = lat, 
                longitude = lon
            )
            
            val current = response.current
            val daily = response.daily
            
            // Map current weather
            val currentWeather = if (current != null) {
                CurrentWeatherDto(
                    temperature = current.temperature_2m ?: 0.0,
                    condition = getWeatherCondition(current.weather_code ?: 0),
                    humidity = current.relative_humidity_2m ?: 0,
                    windSpeed = current.wind_speed_10m ?: 0.0,
                    icon = getWeatherIcon(current.weather_code ?: 0)
                )
            } else {
                CurrentWeatherDto(25.0, "Sunny", 50, 10.0, "mdi-weather-sunny")
            }
            
            // Map forecast
            val forecast = mutableListOf<DailyForecastDto>()
            if (daily != null && daily.time != null) {
                for (i in 0 until (daily.time.size.coerceAtMost(7))) {
                    val dateStr = daily.time[i]
                    val date = LocalDate.parse(dateStr)
                    val dayName = date.format(DateTimeFormatter.ofPattern("EEE")) // Mon, Tue...
                    
                    forecast.add(
                        DailyForecastDto(
                            day = if (i == 0) "Today" else dayName,
                            date = dateStr,
                            maxTemp = daily.temperature_2m_max?.getOrNull(i) ?: 0.0,
                            minTemp = daily.temperature_2m_min?.getOrNull(i) ?: 0.0,
                            precipitation = daily.precipitation_sum?.getOrNull(i) ?: 0.0,
                            condition = getWeatherCondition(daily.weather_code?.getOrNull(i) ?: 0),
                            icon = getWeatherIcon(daily.weather_code?.getOrNull(i) ?: 0)
                        )
                    )
                }
            }
            
            return WeatherResponseDto(
                latitude = lat,
                longitude = lon,
                current = currentWeather,
                forecast = forecast
            )
            
        } catch (e: Exception) {
            logger.error("Failed to fetch weather", e)
            // Return fallback data
            return WeatherResponseDto(
                latitude = lat,
                longitude = lon,
                current = CurrentWeatherDto(22.0, "Service Unavailable", 0, 0.0, "mdi-alert-circle"),
                forecast = emptyList()
            )
        }
    }
    
    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "Clear sky"
            1, 2, 3 -> "Partly cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing Rain"
            71, 73, 75 -> "Snow fall"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }
    
    private fun getWeatherIcon(code: Int): String {
        return when (code) {
            0 -> "mdi-weather-sunny"
            1, 2 -> "mdi-weather-partly-cloudy"
            3 -> "mdi-weather-cloudy"
            45, 48 -> "mdi-weather-fog"
            51, 53, 55, 56, 57 -> "mdi-weather-partly-rainy"
            61, 63, 65, 66, 67, 80, 81, 82 -> "mdi-weather-pouring"
            71, 73, 75, 77, 85, 86 -> "mdi-weather-snowy"
            95, 96, 99 -> "mdi-weather-lightning"
            else -> "mdi-weather-cloudy"
        }
    }
}

data class WeatherResponseDto(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentWeatherDto,
    val forecast: List<DailyForecastDto>
)

data class CurrentWeatherDto(
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val icon: String
)

data class DailyForecastDto(
    val day: String,
    val date: String,
    val maxTemp: Double,
    val minTemp: Double,
    val precipitation: Double,
    val condition: String,
    val icon: String
)
