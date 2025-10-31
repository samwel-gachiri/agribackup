package com.agriconnect.farmersportalapis.service.common

import com.fasterxml.jackson.databind.ObjectMapper
import org.locationtech.jts.geom.*
import org.locationtech.jts.io.geojson.GeoJsonReader
import org.locationtech.jts.io.geojson.GeoJsonWriter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class GeoJsonUtilService(
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(GeoJsonUtilService::class.java)
    private val geoJsonReader = GeoJsonReader()
    private val geoJsonWriter = GeoJsonWriter()
    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
    
    data class CoordinateValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList(),
        val warnings: List<String> = emptyList()
    )
    
    data class PolygonMetrics(
        val areaHectares: BigDecimal,
        val perimeterKm: BigDecimal,
        val centroidLat: Double,
        val centroidLon: Double,
        val boundingBox: BoundingBox
    )
    
    data class BoundingBox(
        val minLat: Double,
        val minLon: Double,
        val maxLat: Double,
        val maxLon: Double
    )
    
    fun validateGeoJsonPolygon(geoJson: String): CoordinateValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            // Parse JSON structure
            val jsonObject = objectMapper.readValue(geoJson, Map::class.java)
            
            // Validate GeoJSON structure
            validateGeoJsonStructure(jsonObject, errors)
            
            if (errors.isNotEmpty()) {
                return CoordinateValidationResult(false, errors, warnings)
            }
            
            // Parse geometry
            val geometry = geoJsonReader.read(geoJson)
            
            // Validate geometry type
            if (geometry !is Polygon && geometry !is MultiPolygon) {
                errors.add("Geometry must be a Polygon or MultiPolygon")
                return CoordinateValidationResult(false, errors, warnings)
            }
            
            // Validate coordinates
            validateCoordinates(geometry, errors, warnings)
            
            // Validate polygon properties
            validatePolygonProperties(geometry, errors, warnings)
            
        } catch (e: Exception) {
            errors.add("Failed to parse GeoJSON: ${e.message}")
        }
        
        return CoordinateValidationResult(errors.isEmpty(), errors, warnings)
    }
    
    fun calculatePolygonMetrics(geoJson: String): PolygonMetrics? {
        return try {
            val geometry = geoJsonReader.read(geoJson)
            
            if (geometry !is Polygon && geometry !is MultiPolygon) {
                return null
            }
            
            val envelope = geometry.envelopeInternal
            val centroid = geometry.centroid
            
            PolygonMetrics(
                areaHectares = calculateAreaInHectares(geometry),
                perimeterKm = calculatePerimeterInKm(geometry),
                centroidLat = centroid.y,
                centroidLon = centroid.x,
                boundingBox = BoundingBox(
                    minLat = envelope.minY,
                    minLon = envelope.minX,
                    maxLat = envelope.maxY,
                    maxLon = envelope.maxX
                )
            )
        } catch (e: Exception) {
            logger.error("Failed to calculate polygon metrics", e)
            null
        }
    }
    
    fun convertEsriToGeoJson(esriGeometry: String): String? {
        return try {
            // Parse ESRI geometry format and convert to GeoJSON
            // This is a simplified implementation - in practice, you'd use ESRI libraries
            val esriData = objectMapper.readValue(esriGeometry, Map::class.java)
            
            when (esriData["geometryType"]) {
                "esriGeometryPolygon" -> {
                    convertEsriPolygonToGeoJson(esriData)
                }
                else -> {
                    logger.warn("Unsupported ESRI geometry type: ${esriData["geometryType"]}")
                    null
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to convert ESRI geometry to GeoJSON", e)
            null
        }
    }
    
    fun simplifyPolygon(geoJson: String, toleranceMeters: Double = 1.0): String? {
        return try {
            val geometry = geoJsonReader.read(geoJson)
            
            // Convert tolerance from meters to degrees (approximate)
            val toleranceDegrees = toleranceMeters / 111319.9
            
            val simplifiedGeometry = org.locationtech.jts.simplify.DouglasPeuckerSimplifier
                .simplify(geometry, toleranceDegrees)
            
            geoJsonWriter.write(simplifiedGeometry)
        } catch (e: Exception) {
            logger.error("Failed to simplify polygon", e)
            null
        }
    }
    
    fun bufferPolygon(geoJson: String, bufferMeters: Double): String? {
        return try {
            val geometry = geoJsonReader.read(geoJson)
            
            // Convert buffer from meters to degrees (approximate)
            val bufferDegrees = bufferMeters / 111319.9
            
            val bufferedGeometry = geometry.buffer(bufferDegrees)
            
            geoJsonWriter.write(bufferedGeometry)
        } catch (e: Exception) {
            logger.error("Failed to buffer polygon", e)
            null
        }
    }
    
    fun checkPolygonIntersection(geoJson1: String, geoJson2: String): Boolean {
        return try {
            val geometry1 = geoJsonReader.read(geoJson1)
            val geometry2 = geoJsonReader.read(geoJson2)
            
            geometry1.intersects(geometry2)
        } catch (e: Exception) {
            logger.error("Failed to check polygon intersection", e)
            false
        }
    }
    
    fun calculatePolygonOverlap(geoJson1: String, geoJson2: String): Double {
        return try {
            val geometry1 = geoJsonReader.read(geoJson1)
            val geometry2 = geoJsonReader.read(geoJson2)
            
            val intersection = geometry1.intersection(geometry2)
            val area1 = calculateAreaInSquareMeters(geometry1)
            val intersectionArea = calculateAreaInSquareMeters(intersection)
            
            if (area1 > 0) intersectionArea / area1 else 0.0
        } catch (e: Exception) {
            logger.error("Failed to calculate polygon overlap", e)
            0.0
        }
    }
    
    fun createCircularPolygon(centerLat: Double, centerLon: Double, radiusMeters: Double): String {
        val center = geometryFactory.createPoint(Coordinate(centerLon, centerLat))
        
        // Convert radius from meters to degrees (approximate)
        val radiusDegrees = radiusMeters / 111319.9
        
        val bufferedGeometry = center.buffer(radiusDegrees)
        
        return geoJsonWriter.write(bufferedGeometry)
    }
    
    private fun validateGeoJsonStructure(jsonObject: Map<*, *>, errors: MutableList<String>) {
        // Check required fields
        if (!jsonObject.containsKey("type")) {
            errors.add("Missing 'type' field")
        }
        
        val type = jsonObject["type"] as? String
        if (type != "Polygon" && type != "MultiPolygon") {
            errors.add("Type must be 'Polygon' or 'MultiPolygon'")
        }
        
        if (!jsonObject.containsKey("coordinates")) {
            errors.add("Missing 'coordinates' field")
        }
        
        // Validate coordinates structure
        val coordinates = jsonObject["coordinates"]
        if (coordinates !is List<*>) {
            errors.add("Coordinates must be an array")
        }
    }
    
    private fun validateCoordinates(geometry: Geometry, errors: MutableList<String>, warnings: MutableList<String>) {
        val coordinates = geometry.coordinates
        
        if (coordinates.isEmpty()) {
            errors.add("Polygon has no coordinates")
            return
        }
        
        coordinates.forEach { coord ->
            // Validate longitude range
            if (coord.x < -180 || coord.x > 180) {
                errors.add("Invalid longitude: ${coord.x} (must be between -180 and 180)")
            }
            
            // Validate latitude range
            if (coord.y < -90 || coord.y > 90) {
                errors.add("Invalid latitude: ${coord.y} (must be between -90 and 90)")
            }
            
            // Check coordinate precision
            val lonStr = coord.x.toString()
            val latStr = coord.y.toString()
            
            if (lonStr.contains(".")) {
                val lonPrecision = lonStr.substringAfter(".").length
                if (lonPrecision > 8) {
                    warnings.add("Longitude precision very high (${lonPrecision} decimal places)")
                }
            }
            
            if (latStr.contains(".")) {
                val latPrecision = latStr.substringAfter(".").length
                if (latPrecision > 8) {
                    warnings.add("Latitude precision very high (${latPrecision} decimal places)")
                }
            }
        }
    }
    
    private fun validatePolygonProperties(geometry: Geometry, errors: MutableList<String>, warnings: MutableList<String>) {
        // Check minimum area (100 square meters)
        val areaSquareMeters = calculateAreaInSquareMeters(geometry)
        if (areaSquareMeters < 100) {
            errors.add("Polygon area too small: ${areaSquareMeters.toInt()} m² (minimum 100 m²)")
        }
        
        // Check maximum area (reasonable farm size - 100,000 hectares)
        val areaHectares = areaSquareMeters / 10000.0
        if (areaHectares > 100000) {
            warnings.add("Polygon area very large: ${areaHectares.toInt()} hectares")
        }
        
        // Check if polygon is valid (no self-intersections, etc.)
        if (!geometry.isValid) {
            errors.add("Polygon geometry is invalid")
        }
        
        // Check polygon complexity (number of vertices)
        val numVertices = geometry.numPoints
        if (numVertices > 1000) {
            warnings.add("Polygon has many vertices ($numVertices), consider simplifying")
        }
    }
    
    private fun convertEsriPolygonToGeoJson(esriData: Map<*, *>): String? {
        return try {
            // Simplified ESRI to GeoJSON conversion
            // In practice, you'd use proper ESRI libraries
            val rings = esriData["rings"] as? List<List<List<Double>>>
                ?: return null
            
            val coordinates = rings.map { ring ->
                ring.map { point ->
                    listOf(point[0], point[1]) // [longitude, latitude]
                }
            }
            
            val geoJsonPolygon = mapOf(
                "type" to "Polygon",
                "coordinates" to coordinates
            )
            
            objectMapper.writeValueAsString(geoJsonPolygon)
        } catch (e: Exception) {
            logger.error("Failed to convert ESRI polygon", e)
            null
        }
    }
    
    private fun calculateAreaInHectares(geometry: Geometry): BigDecimal {
        val areaSquareMeters = calculateAreaInSquareMeters(geometry)
        return BigDecimal(areaSquareMeters / 10000.0).setScale(4, RoundingMode.HALF_UP)
    }
    
    private fun calculateAreaInSquareMeters(geometry: Geometry): Double {
        // Approximate area calculation for WGS84
        // For more accuracy, should project to appropriate UTM zone
        val centroidLat = geometry.centroid.y
        val latCorrectionFactor = Math.cos(Math.toRadians(centroidLat))
        
        return geometry.area * 111319.9 * 111319.9 * latCorrectionFactor
    }
    
    private fun calculatePerimeterInKm(geometry: Geometry): BigDecimal {
        // Approximate perimeter calculation for WGS84
        val perimeterDegrees = geometry.length
        val perimeterMeters = perimeterDegrees * 111319.9
        val perimeterKm = perimeterMeters / 1000.0
        
        return BigDecimal(perimeterKm).setScale(3, RoundingMode.HALF_UP)
    }
}