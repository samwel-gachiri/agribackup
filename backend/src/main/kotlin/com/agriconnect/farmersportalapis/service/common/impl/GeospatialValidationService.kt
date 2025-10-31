package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.profile.Zone
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import com.agriconnect.farmersportalapis.infrastructure.repositories.ZoneRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import kotlin.math.*

@Service
class GeospatialValidationService(
    private val zoneRepository: ZoneRepository
) {
    private val logger = LoggerFactory.getLogger(GeospatialValidationService::class.java)

    companion object {
        const val EARTH_RADIUS_KM = 6371.0
        const val MIN_ZONE_RADIUS_KM = 0.1
        const val MAX_ZONE_RADIUS_KM = 100.0
        const val COORDINATE_PRECISION = 6
    }

    /**
     * Validates if a farmer's location is within a specific zone's boundaries
     */
    fun validateFarmerInZone(farmer: Farmer, zone: Zone): Result<GeospatialValidationResult> {
        return try {
            val farmerLocation = farmer.location
                ?: return ResultFactory.getFailResult("Farmer location not available")

            val distance = calculateDistance(
                farmerLocation.latitude,
                farmerLocation.longitude,
                zone.centerLatitude.toDouble(),
                zone.centerLongitude.toDouble()
            )

            val isWithinBounds = distance <= zone.radiusKm.toDouble()
            
            val result = GeospatialValidationResult(
                isValid = isWithinBounds,
                distance = BigDecimal.valueOf(distance),
                zoneId = zone.id,
                zoneName = zone.name,
                farmerLocation = LocationDto(
                    latitude = farmerLocation.latitude,
                    longitude = farmerLocation.longitude,
                    customName = farmerLocation.customName ?: ""
                ),
                zoneCenter = LocationDto(
                    latitude = zone.centerLatitude.toDouble(),
                    longitude = zone.centerLongitude.toDouble(),
                    customName = zone.name
                ),
                zoneRadius = zone.radiusKm,
                validationMessage = if (isWithinBounds) 
                    "Farmer is within zone boundaries" 
                else 
                    "Farmer is ${String.format("%.2f", distance - zone.radiusKm.toDouble())} km outside zone boundaries"
            )

            logger.info("Geospatial validation for farmer {} in zone {}: {} (distance: {} km)", 
                farmer.id, zone.id, if (isWithinBounds) "VALID" else "INVALID", distance)

            ResultFactory.getSuccessResult(result, "Geospatial validation completed")
        } catch (e: Exception) {
            logger.error("Error validating farmer location in zone: {}", e.message, e)
            ResultFactory.getFailResult("Failed to validate farmer location: ${e.message}")
        }
    }

    /**
     * Validates zone boundaries and checks for overlaps with existing zones
     */
    fun validateZoneBoundaries(zoneRequest: CreateZoneRequestDto, excludeZoneId: String? = null): Result<ZoneBoundaryValidationResult> {
        return try {
            // Validate coordinate ranges
            if (!isValidLatitude(zoneRequest.centerLatitude.toDouble())) {
                return ResultFactory.getFailResult("Invalid latitude: must be between -90 and 90 degrees")
            }
            
            if (!isValidLongitude(zoneRequest.centerLongitude.toDouble())) {
                return ResultFactory.getFailResult("Invalid longitude: must be between -180 and 180 degrees")
            }

            // Validate radius
            if (zoneRequest.radiusKm.toDouble() < MIN_ZONE_RADIUS_KM) {
                return ResultFactory.getFailResult("Zone radius too small: minimum ${MIN_ZONE_RADIUS_KM} km")
            }
            
            if (zoneRequest.radiusKm.toDouble() > MAX_ZONE_RADIUS_KM) {
                return ResultFactory.getFailResult("Zone radius too large: maximum ${MAX_ZONE_RADIUS_KM} km")
            }

            // Check for overlaps with existing zones
            val existingZones = zoneRepository.findByExporterId(zoneRequest.exporterId)
                .filter { it.id != excludeZoneId }

            val overlaps = mutableListOf<ZoneOverlapInfo>()
            
            for (existingZone in existingZones) {
                val overlapInfo = checkZoneOverlap(zoneRequest, existingZone)
                if (overlapInfo.hasOverlap) {
                    overlaps.add(overlapInfo)
                }
            }

            val result = ZoneBoundaryValidationResult(
                isValid = overlaps.isEmpty(),
                overlaps = overlaps,
                suggestedRadius = if (overlaps.isNotEmpty()) calculateOptimalRadius(zoneRequest, existingZones) else null,
                validationMessage = if (overlaps.isEmpty()) 
                    "Zone boundaries are valid" 
                else 
                    "Zone overlaps with ${overlaps.size} existing zone(s)"
            )

            logger.info("Zone boundary validation for new zone at ({}, {}): {} overlaps found", 
                zoneRequest.centerLatitude, zoneRequest.centerLongitude, overlaps.size)

            ResultFactory.getSuccessResult(result, "Zone boundary validation completed")
        } catch (e: Exception) {
            logger.error("Error validating zone boundaries: {}", e.message, e)
            ResultFactory.getFailResult("Failed to validate zone boundaries: ${e.message}")
        }
    }

    /**
     * Finds the optimal zone for a farmer based on their location
     */
    fun findOptimalZoneForFarmer(farmer: Farmer, exporterId: String): Result<OptimalZoneResult> {
        return try {
            val farmerLocation = farmer.location
                ?: return ResultFactory.getFailResult("Farmer location not available")

            val availableZones = zoneRepository.findByExporterId(exporterId)
            
            if (availableZones.isEmpty()) {
                return ResultFactory.getFailResult("No zones available for this exporter")
            }

            val zoneDistances = availableZones.map { zone ->
                val distance = calculateDistance(
                    farmerLocation.latitude,
                    farmerLocation.longitude,
                    zone.centerLatitude.toDouble(),
                    zone.centerLongitude.toDouble()
                )
                
                ZoneDistanceInfo(
                    zone = zone,
                    distance = BigDecimal.valueOf(distance),
                    isWithinBounds = distance <= zone.radiusKm.toDouble()
                )
            }.sortedBy { it.distance.toDouble() }

            val optimalZone = zoneDistances.firstOrNull { it.isWithinBounds }
                ?: zoneDistances.first() // If no zone contains the farmer, suggest the closest one

            val result = OptimalZoneResult(
                recommendedZone = optimalZone.zone.toResponseDto(),
                distance = optimalZone.distance,
                isWithinBounds = optimalZone.isWithinBounds,
                alternativeZones = zoneDistances.take(3).filter { it.zone.id != optimalZone.zone.id }
                    .map { AlternativeZoneInfo(it.zone.toResponseDto(), it.distance, it.isWithinBounds) },
                recommendation = if (optimalZone.isWithinBounds) 
                    "Farmer is within ${optimalZone.zone.name} zone boundaries" 
                else 
                    "Farmer is closest to ${optimalZone.zone.name} but outside boundaries. Consider expanding zone or creating new zone."
            )

            logger.info("Optimal zone search for farmer {}: recommended zone {} (distance: {} km)", 
                farmer.id, optimalZone.zone.id, optimalZone.distance)

            ResultFactory.getSuccessResult(result, "Optimal zone found")
        } catch (e: Exception) {
            logger.error("Error finding optimal zone for farmer: {}", e.message, e)
            ResultFactory.getFailResult("Failed to find optimal zone: ${e.message}")
        }
    }

    /**
     * Validates geographic coordinates format and precision
     */
    fun validateCoordinates(latitude: Double, longitude: Double): Result<CoordinateValidationResult> {
        return try {
            val latValid = isValidLatitude(latitude)
            val lonValid = isValidLongitude(longitude)
            val precisionValid = hasValidPrecision(latitude, longitude)

            val result = CoordinateValidationResult(
                isValid = latValid && lonValid && precisionValid,
                latitudeValid = latValid,
                longitudeValid = lonValid,
                precisionValid = precisionValid,
                formattedLatitude = formatCoordinate(latitude),
                formattedLongitude = formatCoordinate(longitude),
                validationMessage = when {
                    !latValid -> "Invalid latitude: must be between -90 and 90 degrees"
                    !lonValid -> "Invalid longitude: must be between -180 and 180 degrees"
                    !precisionValid -> "Coordinate precision too high: maximum $COORDINATE_PRECISION decimal places"
                    else -> "Coordinates are valid"
                }
            )

            ResultFactory.getSuccessResult(result, "Coordinate validation completed")
        } catch (e: Exception) {
            logger.error("Error validating coordinates: {}", e.message, e)
            ResultFactory.getFailResult("Failed to validate coordinates: ${e.message}")
        }
    }

    // Private helper methods
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    private fun checkZoneOverlap(newZone: CreateZoneRequestDto, existingZone: Zone): ZoneOverlapInfo {
        val distance = calculateDistance(
            newZone.centerLatitude.toDouble(),
            newZone.centerLongitude.toDouble(),
            existingZone.centerLatitude.toDouble(),
            existingZone.centerLongitude.toDouble()
        )

        val combinedRadius = newZone.radiusKm.toDouble() + existingZone.radiusKm.toDouble()
        val hasOverlap = distance < combinedRadius
        val overlapDistance = if (hasOverlap) combinedRadius - distance else 0.0

        return ZoneOverlapInfo(
            existingZone = existingZone.toResponseDto(),
            distance = BigDecimal.valueOf(distance),
            hasOverlap = hasOverlap,
            overlapDistance = BigDecimal.valueOf(overlapDistance),
            overlapPercentage = if (hasOverlap) 
                ((overlapDistance / newZone.radiusKm.toDouble()) * 100).toInt() 
            else 0
        )
    }

    private fun calculateOptimalRadius(newZone: CreateZoneRequestDto, existingZones: List<Zone>): BigDecimal {
        val minDistanceToExisting = existingZones.minOfOrNull { zone ->
            calculateDistance(
                newZone.centerLatitude.toDouble(),
                newZone.centerLongitude.toDouble(),
                zone.centerLatitude.toDouble(),
                zone.centerLongitude.toDouble()
            ) - zone.radiusKm.toDouble()
        } ?: newZone.radiusKm.toDouble()

        return BigDecimal.valueOf(maxOf(MIN_ZONE_RADIUS_KM, minDistanceToExisting * 0.9))
    }

    private fun isValidLatitude(latitude: Double): Boolean = latitude in -90.0..90.0
    private fun isValidLongitude(longitude: Double): Boolean = longitude in -180.0..180.0
    
    private fun hasValidPrecision(latitude: Double, longitude: Double): Boolean {
        val latStr = latitude.toString()
        val lonStr = longitude.toString()
        val latDecimals = if (latStr.contains('.')) latStr.substringAfter('.').length else 0
        val lonDecimals = if (lonStr.contains('.')) lonStr.substringAfter('.').length else 0
        return latDecimals <= COORDINATE_PRECISION && lonDecimals <= COORDINATE_PRECISION
    }

    private fun formatCoordinate(coordinate: Double): String {
        return String.format("%.${COORDINATE_PRECISION}f", coordinate)
    }

    private fun Zone.toResponseDto() = ZoneResponseDto(
        id = id,
        name = name,
        produceType = produceType,
        centerLatitude = centerLatitude,
        centerLongitude = centerLongitude,
        radiusKm = radiusKm,
        exporterId = exporter.id,
        creatorId = creator?.id,
        comments = comments,
        farmerCount = farmerRelationships.size,
        supervisorIds = supervisors.map { it.id }
    )
}