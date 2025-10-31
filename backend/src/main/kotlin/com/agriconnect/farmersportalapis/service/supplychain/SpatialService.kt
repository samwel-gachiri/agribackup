package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProductionUnitRepository
import org.locationtech.jts.geom.*
import org.locationtech.jts.io.geojson.GeoJsonReader
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
@Transactional
class SpatialService(
    private val productionUnitRepository: ProductionUnitRepository,
    private val geometryFactory: GeometryFactory
) {

    private val logger = LoggerFactory.getLogger(SpatialService::class.java)
    private val geoJsonReader = GeoJsonReader(geometryFactory)
    private val geoJsonWriter = GeoJsonWriter()

    companion object {
        private const val WGS84_SRID = 4326
        private const val EARTH_RADIUS_KM = 6371.0
    }

    init {
        // SRID is set during geometry creation
    }

    /**
     * Create or update production unit with spatial geometry
     */
    fun createProductionUnitWithGeometry(
        farmerId: String,
        unitName: String,
        geoJsonGeometry: String,
        areaHectares: BigDecimal,
        administrativeRegion: String? = null
    ): ProductionUnit {
        logger.info("Creating production unit with geometry for farmer: $farmerId")

        // Parse GeoJSON geometry
        val geometry = parseGeoJsonGeometry(geoJsonGeometry)

        // Validate geometry
        validateGeometry(geometry)

        // Calculate area if not provided or validate provided area
        val calculatedArea = calculateAreaHectares(geometry)
        if (areaHectares.toDouble() == 0.0) {
            // Use calculated area if not provided
            val finalArea = BigDecimal.valueOf(calculatedArea)
            logger.info("Using calculated area: ${finalArea} hectares")
        } else {
            // Validate provided area is reasonable
            val areaRatio = areaHectares.toDouble() / calculatedArea
            if (areaRatio < 0.5 || areaRatio > 2.0) {
                logger.warn("Provided area (${areaHectares} ha) differs significantly from calculated area (${String.format("%.2f", calculatedArea)} ha)")
            }
        }

        // Create WGS84 coordinate string
        val wgs84Coords = generateWgs84Coordinates(geometry)

        // Create production unit
        val userProfile = UserProfile(
            email = null,
            passwordHash = "",
            fullName = "Placeholder Farmer"
        ).apply { id = farmerId }
        
        val farmer = Farmer(
            userProfile = userProfile
        ).apply { id = farmerId }
        
        val productionUnit = ProductionUnit(
            farmer = farmer,
            unitName = unitName,
            parcelGeometry = geometry,
            areaHectares = areaHectares,
            wgs84Coordinates = wgs84Coords,
            administrativeRegion = administrativeRegion,
            lastVerifiedAt = null,
            hederaHash = null,
            hederaTransactionId = null
        )

        return productionUnitRepository.save(productionUnit)
    }

    /**
     * Update production unit geometry
     */
    fun updateProductionUnitGeometry(
        unitId: String,
        geoJsonGeometry: String,
        areaHectares: BigDecimal? = null
    ): ProductionUnit {
        val productionUnit = productionUnitRepository.findById(unitId)
            .orElseThrow { IllegalArgumentException("Production unit not found: $unitId") }

        val geometry = parseGeoJsonGeometry(geoJsonGeometry)
        validateGeometry(geometry)

        productionUnit.parcelGeometry = geometry

        if (areaHectares != null) {
            productionUnit.areaHectares = areaHectares
        } else {
            // Recalculate area
            val calculatedArea = calculateAreaHectares(geometry)
            productionUnit.areaHectares = BigDecimal.valueOf(calculatedArea)
        }

        productionUnit.wgs84Coordinates = generateWgs84Coordinates(geometry)
        productionUnit.lastVerifiedAt = LocalDateTime.now()

        return productionUnitRepository.save(productionUnit)
    }

    /**
     * Find production units within distance of a point
     */
    fun findProductionUnitsNearPoint(
        latitude: Double,
        longitude: Double,
        distanceKm: Double
    ): List<ProductionUnit> {
        val point = geometryFactory.createPoint(Coordinate(longitude, latitude))
        point.srid = WGS84_SRID

        // Create buffer around point
        val buffer = point.buffer(degreesFromKm(distanceKm))

        return productionUnitRepository.findIntersectingGeometries(buffer)
    }

    /**
     * Find production units intersecting with a geometry
     */
    fun findProductionUnitsIntersecting(geometry: Geometry): List<ProductionUnit> {
        return productionUnitRepository.findIntersectingGeometries(geometry)
    }

    /**
     * Calculate distance between two geometries
     */
    fun calculateDistance(geometry1: Geometry, geometry2: Geometry): Double {
        return geometry1.distance(geometry2) * EARTH_RADIUS_KM
    }

    /**
     * Check if geometries intersect
     */
    fun geometriesIntersect(geometry1: Geometry, geometry2: Geometry): Boolean {
        return geometry1.intersects(geometry2)
    }

    /**
     * Get geometry as GeoJSON
     */
    fun geometryToGeoJson(geometry: Geometry): String {
        return geoJsonWriter.write(geometry)
    }

    /**
     * Validate geometry for EUDR compliance
     */
    fun validateGeometry(geometry: Geometry): GeometryValidationResult {
        val issues = mutableListOf<String>()

        // Check if geometry is valid
        if (!geometry.isValid) {
            issues.add("Geometry is not valid")
        }

        // Check geometry type (should be Polygon or MultiPolygon)
        if (geometry !is Polygon && geometry !is MultiPolygon) {
            issues.add("Geometry must be a Polygon or MultiPolygon")
        }

        // Check SRID
        if (geometry.srid != WGS84_SRID) {
            issues.add("Geometry must use WGS84 coordinate system (SRID 4326)")
        }

        // Check coordinate bounds
        val envelope = geometry.envelopeInternal
        if (envelope.minX < -180 || envelope.maxX > 180 ||
            envelope.minY < -90 || envelope.maxY > 90) {
            issues.add("Coordinates are outside valid WGS84 bounds")
        }

        // Check area (reasonable minimum size)
        val areaHectares = calculateAreaHectares(geometry)
        if (areaHectares < 0.01) { // Less than 100 m²
            issues.add("Area is too small (minimum 0.01 hectares)")
        }

        if (areaHectares > 100000) { // More than 1000 km²
            issues.add("Area is too large (maximum 100,000 hectares)")
        }

        return GeometryValidationResult(
            isValid = issues.isEmpty(),
            issues = issues,
            areaHectares = areaHectares,
            geometryType = geometry.geometryType
        )
    }

    /**
     * Parse GeoJSON geometry string
     */
    private fun parseGeoJsonGeometry(geoJson: String): Geometry {
        return try {
            val geometry = geoJsonReader.read(geoJson)
            geometry.srid = WGS84_SRID
            geometry
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid GeoJSON geometry: ${e.message}")
        }
    }

    /**
     * Calculate area in hectares from geometry
     */
    private fun calculateAreaHectares(geometry: Geometry): Double {
        // For WGS84, we need to use geodesic calculations
        // This is a simplified approximation - in production, use a proper geospatial library
        val envelope = geometry.envelopeInternal
        val widthDegrees = envelope.maxX - envelope.minX
        val heightDegrees = envelope.maxY - envelope.minY

        // Rough approximation: convert degrees to meters
        // 1 degree ≈ 111 km at equator
        val widthKm = widthDegrees * 111.0 * Math.cos(Math.toRadians((envelope.minY + envelope.maxY) / 2))
        val heightKm = heightDegrees * 111.0

        // Calculate area in square kilometers, then convert to hectares
        val areaKm2 = widthKm * heightKm
        return areaKm2 * 100 // 1 km² = 100 hectares
    }

    /**
     * Generate WGS84 coordinate string from geometry
     */
    private fun generateWgs84Coordinates(geometry: Geometry): String {
        val centroid = geometry.centroid
        return "${centroid.x},${centroid.y}"
    }

    /**
     * Convert kilometers to degrees (approximate)
     */
    private fun degreesFromKm(km: Double): Double {
        return km / 111.0 // Rough approximation
    }

    /**
     * Create buffer geometry around a point
     */
    fun createBuffer(point: Geometry, distanceKm: Double): Geometry {
        val distanceDegrees = degreesFromKm(distanceKm)
        return point.buffer(distanceDegrees)
    }

    /**
     * Get spatial statistics for production units
     */
    fun getSpatialStatistics(): SpatialStatistics {
        val allUnits = productionUnitRepository.findAll()

        val totalArea = allUnits.sumOf { it.areaHectares.toDouble() }
        val averageArea = if (allUnits.isNotEmpty()) totalArea / allUnits.size else 0.0
        val totalUnits = allUnits.size

        // Count by geometry validity
        var validGeometries = 0
        var invalidGeometries = 0

        allUnits.forEach { unit ->
            unit.parcelGeometry?.let { geometry ->
                val validation = validateGeometry(geometry)
                if (validation.isValid) validGeometries++ else invalidGeometries++
            } ?: invalidGeometries++
        }

        return SpatialStatistics(
            totalUnits = totalUnits,
            totalAreaHectares = totalArea,
            averageAreaHectares = averageArea,
            validGeometries = validGeometries,
            invalidGeometries = invalidGeometries
        )
    }
}

// Data classes for spatial operations

data class GeometryValidationResult(
    val isValid: Boolean,
    val issues: List<String>,
    val areaHectares: Double,
    val geometryType: String
)

data class SpatialStatistics(
    val totalUnits: Int,
    val totalAreaHectares: Double,
    val averageAreaHectares: Double,
    val validGeometries: Int,
    val invalidGeometries: Int
)