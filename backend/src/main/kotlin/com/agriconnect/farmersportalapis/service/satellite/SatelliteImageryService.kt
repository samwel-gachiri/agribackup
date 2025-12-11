package com.agriconnect.farmersportalapis.service.satellite

import com.agriconnect.farmersportalapis.config.EarthEngineParameters
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.auth.oauth2.GoogleCredentials
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.locationtech.jts.geom.Geometry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Satellite Imagery Service
 * 
 * Core service for querying Google Earth Engine to retrieve Sentinel-2 and Landsat imagery.
 * Provides unified interface for satellite data access regardless of data source.
 * 
 * Sentinel-2:
 * - 10m resolution (bands 2, 3, 4, 8)
 * - 5-day revisit time
 * - 13 spectral bands
 * - Available from 2015-06-23
 * 
 * Landsat 8/9:
 * - 30m resolution
 * - 16-day revisit time (8 days combined with Landsat 9)
 * - 11 spectral bands
 * - Available from 2013 (Landsat 8), 2021 (Landsat 9)
 */
@Service
class SatelliteImageryService(
    private val googleCredentials: GoogleCredentials?,
    private val earthEngineHttpClient: OkHttpClient,
    private val earthEngineParameters: EarthEngineParameters,
    private val objectMapper: ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(SatelliteImageryService::class.java)
    
    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
        
        // Sentinel-2 collections
        const val SENTINEL2_SR = "COPERNICUS/S2_SR_HARMONIZED"  // Surface Reflectance
        const val SENTINEL2_CLOUD_PROBABILITY = "COPERNICUS/S2_CLOUD_PROBABILITY"
        
        // Landsat collections
        const val LANDSAT8_SR = "LANDSAT/LC08/C02/T1_L2"  // Landsat 8 Surface Reflectance
        const val LANDSAT9_SR = "LANDSAT/LC09/C02/T1_L2"  // Landsat 9 Surface Reflectance
        
        // Cloud cover threshold (percentage)
        const val MAX_CLOUD_COVER = 20.0
    }

    /**
     * Check if Earth Engine is available and configured
     */
    fun isAvailable(): Boolean {
        val available = earthEngineParameters.enabled && googleCredentials != null
        if (!available) {
            if (!earthEngineParameters.enabled) {
                logger.debug("Earth Engine is disabled in configuration")
            }
            if (googleCredentials == null) {
                logger.warn("Earth Engine credentials not available. Please check:\n" +
                           "  1. EARTH_ENGINE_CREDENTIALS_PATH is set correctly\n" +
                           "  2. Service account JSON file exists at the specified path\n" +
                           "  3. Service account has Earth Engine access")
            }
        }
        return available
    }

    /**
     * Query Sentinel-2 imagery for a given geometry and date range
     * Returns the median composite of all images in the date range with cloud cover < 20%
     */
    fun querySentinel2(
        geometry: Geometry,
        startDate: LocalDate,
        endDate: LocalDate,
        maxCloudCover: Double = MAX_CLOUD_COVER
    ): SatelliteImageResult? {
        if (!isAvailable()) {
            logger.warn("Earth Engine not available for Sentinel-2 query")
            return null
        }

        return try {
            logger.info("Querying Sentinel-2 for dates ${startDate} to ${endDate}")
            
            val geoJson = geometryToGeoJson(geometry)
            
            // Earth Engine Python-like query converted to REST API call
            val script = """
                var geometry = ee.Geometry(${geoJson});
                var startDate = '${startDate.format(DATE_FORMATTER)}';
                var endDate = '${endDate.format(DATE_FORMATTER)}';
                
                // Load Sentinel-2 Surface Reflectance
                var s2 = ee.ImageCollection('${SENTINEL2_SR}')
                    .filterBounds(geometry)
                    .filterDate(startDate, endDate)
                    .filter(ee.Filter.lt('CLOUDY_PIXEL_PERCENTAGE', ${maxCloudCover}));
                
                // Calculate NDVI for each image
                var withNDVI = s2.map(function(image) {
                    var ndvi = image.normalizedDifference(['B8', 'B4']).rename('NDVI');
                    return image.addBands(ndvi);
                });
                
                // Get median composite
                var composite = withNDVI.median();
                
                // Calculate statistics
                var stats = composite.select('NDVI').reduceRegion({
                    reducer: ee.Reducer.mean()
                        .combine(ee.Reducer.min(), '', true)
                        .combine(ee.Reducer.max(), '', true)
                        .combine(ee.Reducer.stdDev(), '', true),
                    geometry: geometry,
                    scale: 10,
                    maxPixels: 1e9
                });
                
                stats;
            """.trimIndent()

            val result = executeEarthEngineScript(script)
            
            parseSatelliteResult(result, "SENTINEL-2", startDate, endDate)
        } catch (e: Exception) {
            logger.error("Failed to query Sentinel-2 imagery", e)
            null
        }
    }

    /**
     * Query Landsat 8/9 imagery for a given geometry and date range
     * Combines both Landsat 8 and 9 for better temporal resolution
     */
    fun queryLandsat(
        geometry: Geometry,
        startDate: LocalDate,
        endDate: LocalDate,
        maxCloudCover: Double = MAX_CLOUD_COVER
    ): SatelliteImageResult? {
        if (!isAvailable()) {
            logger.warn("Earth Engine not available for Landsat query")
            return null
        }

        return try {
            logger.info("Querying Landsat 8/9 for dates ${startDate} to ${endDate}")
            
            val geoJson = geometryToGeoJson(geometry)
            
            val script = """
                var geometry = ee.Geometry(${geoJson});
                var startDate = '${startDate.format(DATE_FORMATTER)}';
                var endDate = '${endDate.format(DATE_FORMATTER)}';
                
                // Load Landsat 8 Surface Reflectance
                var l8 = ee.ImageCollection('${LANDSAT8_SR}')
                    .filterBounds(geometry)
                    .filterDate(startDate, endDate)
                    .filter(ee.Filter.lt('CLOUD_COVER', ${maxCloudCover}));
                
                // Load Landsat 9 Surface Reflectance
                var l9 = ee.ImageCollection('${LANDSAT9_SR}')
                    .filterBounds(geometry)
                    .filterDate(startDate, endDate)
                    .filter(ee.Filter.lt('CLOUD_COVER', ${maxCloudCover}));
                
                // Merge collections
                var landsat = l8.merge(l9);
                
                // Calculate NDVI (NIR - Red) / (NIR + Red)
                // Landsat: NIR = B5, Red = B4
                var withNDVI = landsat.map(function(image) {
                    var ndvi = image.normalizedDifference(['SR_B5', 'SR_B4']).rename('NDVI');
                    return image.addBands(ndvi);
                });
                
                // Get median composite
                var composite = withNDVI.median();
                
                // Calculate statistics
                var stats = composite.select('NDVI').reduceRegion({
                    reducer: ee.Reducer.mean()
                        .combine(ee.Reducer.min(), '', true)
                        .combine(ee.Reducer.max(), '', true)
                        .combine(ee.Reducer.stdDev(), '', true),
                    geometry: geometry,
                    scale: 30,
                    maxPixels: 1e9
                });
                
                stats;
            """.trimIndent()

            val result = executeEarthEngineScript(script)
            
            parseSatelliteResult(result, "LANDSAT-8/9", startDate, endDate)
        } catch (e: Exception) {
            logger.error("Failed to query Landsat imagery", e)
            null
        }
    }

    /**
     * Execute Earth Engine script via REST API
     */
    private fun executeEarthEngineScript(script: String): Map<String, Any> {
        // Refresh credentials if needed
        googleCredentials?.refreshIfExpired()
        
        val accessToken = googleCredentials?.accessToken?.tokenValue
            ?: throw IllegalStateException("No access token available")

        val requestBody = objectMapper.writeValueAsString(
            mapOf(
                "expression" to script,
                "fileFormat" to "JSON"
            )
        )

        val request = Request.Builder()
            .url("${earthEngineParameters.apiBaseUrl}/v1/projects/${earthEngineParameters.projectId}:computeValue")
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        earthEngineHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "No error details"
                val errorMsg = when (response.code) {
                    404 -> "Earth Engine project not found or API not enabled. Please ensure:\n" +
                           "  1. Project ID '${earthEngineParameters.projectId}' is correct\n" +
                           "  2. Earth Engine API is enabled: gcloud services enable earthengine.googleapis.com\n" +
                           "  3. Service account has Earth Engine permissions\n" +
                           "  Error details: $errorBody"
                    401, 403 -> "Authentication failed. Please check service account credentials and permissions.\n" +
                           "  Error details: $errorBody"
                    else -> "Earth Engine API call failed: ${response.code} ${response.message}\n" +
                           "  Error details: $errorBody"
                }
                throw RuntimeException(errorMsg)
            }

            val responseBody = response.body?.string()
                ?: throw RuntimeException("Empty response from Earth Engine")

            return objectMapper.readValue(responseBody)
        }
    }

    /**
     * Convert JTS Geometry to GeoJSON string
     */
    private fun geometryToGeoJson(geometry: Geometry): String {
        val coordinates = geometry.coordinates.map { coord ->
            listOf(coord.x, coord.y)
        }
        
        return objectMapper.writeValueAsString(
            mapOf(
                "type" to "Polygon",
                "coordinates" to listOf(coordinates)
            )
        )
    }

    /**
     * Parse Earth Engine result into SatelliteImageResult
     */
    private fun parseSatelliteResult(
        result: Map<String, Any>,
        dataSource: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): SatelliteImageResult {
        val ndviMean = (result["NDVI_mean"] as? Number)?.toDouble() ?: 0.0
        val ndviMin = (result["NDVI_min"] as? Number)?.toDouble() ?: 0.0
        val ndviMax = (result["NDVI_max"] as? Number)?.toDouble() ?: 0.0
        val ndviStdDev = (result["NDVI_stdDev"] as? Number)?.toDouble() ?: 0.0

        return SatelliteImageResult(
            dataSource = dataSource,
            startDate = startDate,
            endDate = endDate,
            ndviMean = ndviMean,
            ndviMin = ndviMin,
            ndviMax = ndviMax,
            ndviStdDev = ndviStdDev,
            rawResult = result
        )
    }
}

/**
 * Result from satellite imagery query
 */
data class SatelliteImageResult(
    val dataSource: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val ndviMean: Double,
    val ndviMin: Double,
    val ndviMax: Double,
    val ndviStdDev: Double,
    val rawResult: Map<String, Any>
)
