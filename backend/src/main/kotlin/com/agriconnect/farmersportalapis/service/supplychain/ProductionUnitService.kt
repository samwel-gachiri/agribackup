package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProductionUnitRepository
import com.agriconnect.farmersportalapis.service.hedera.HederaConsensusServices
import com.agriconnect.farmersportalapis.service.hedera.HederaMainService
import com.fasterxml.jackson.databind.ObjectMapper
import org.locationtech.jts.geom.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.wololo.jts2geojson.GeoJSONReader
import org.wololo.jts2geojson.GeoJSONWriter
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import java.time.LocalDateTime

@Service
@Transactional
class ProductionUnitService(
    private val productionUnitRepository: ProductionUnitRepository,
    private val hederaConsensusService: HederaConsensusServices,
    private val hederaMainService: HederaMainService,
    private val objectMapper: ObjectMapper,
    private val farmerExporterRelationshipRepository: com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerExporterRelationshipRepository
) {

    private val logger = LoggerFactory.getLogger(ProductionUnitService::class.java)
    private val geoJsonReader = GeoJSONReader()
    private val geoJsonWriter = GeoJSONWriter()
    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

    companion object {
        /**
         * EUDR Article 9(1)(d): Plots exceeding 4 hectares require polygon geometry.
         * Plots ≤4 hectares may use a single geolocation point instead.
         */
        val EUDR_POLYGON_THRESHOLD_HECTARES: BigDecimal = BigDecimal("4.0")
    }

    /**
     * Creates a new production unit with EUDR compliance validation.
     * 
     * @param farmer The farmer who owns/manages this plot
     * @param unitName Display name for the production unit
     * @param geoJsonPolygon Polygon geometry (required if area >4ha, optional for ≤4ha)
     * @param geolocationPoint Single lat,lon point for plots ≤4ha (format: "lat,lon")
     * @param radiusMeters Radius in meters around the geolocation point (required when using point)
     * @param administrativeRegion Optional administrative region name
     * @param countryCode Optional ISO country code
     * @throws IllegalArgumentException if EUDR validation fails
     */
    fun createProductionUnit(
        farmer: Farmer,
        unitName: String,
        geoJsonPolygon: String? = null,
        geolocationPoint: String? = null,
        radiusMeters: Double? = null,
        administrativeRegion: String? = null,
        countryCode: String? = null
    ): ProductionUnit {

        // Determine geolocation type
        val geolocationType = if (!geoJsonPolygon.isNullOrBlank()) "POLYGON" else "POINT"

        // EUDR Article 9(1)(d) compliance validation
        validateEudrGeolocationRequirements(geoJsonPolygon, geolocationPoint, radiusMeters)

        // Parse geometry if polygon provided
        val geometry = geoJsonPolygon?.let { validateAndParseGeoJson(it) }

        // Calculate area: from polygon if available, otherwise from radius
        val areaHectares = when {
            geometry != null -> calculateAreaInHectares(geometry)
            radiusMeters != null -> calculateAreaFromRadius(radiusMeters)
            else -> throw IllegalArgumentException("Either polygon geometry or radius is required to calculate area")
        }

        // Extract WGS84 coordinates (from polygon centroid or from geolocation point)
        val wgs84Coordinates = geometry?.let { extractWgs84Coordinates(it) } ?: geolocationPoint

        // Auto-detect country code from coordinates if not provided
        val resolvedCountryCode = countryCode 
            ?: geometry?.let { detectCountryFromGeometry(it) }
            ?: geolocationPoint?.let { detectCountryFromPoint(it) }

        // Create production unit with EUDR fields
        val productionUnit = ProductionUnit(
            farmer = farmer,
            unitName = unitName,
            parcelGeometry = geometry,
            areaHectares = areaHectares,
            wgs84Coordinates = wgs84Coordinates,
            administrativeRegion = administrativeRegion,
            countryCode = resolvedCountryCode,
            lastVerifiedAt = LocalDateTime.now(),
            hederaHash = null,
            hederaTransactionId = null,
            geolocationPoint = geolocationPoint,
            radiusMeters = radiusMeters,
            geolocationType = geolocationType,
            isLocked = false
        )

        // Save to database
        val savedUnit = productionUnitRepository.save(productionUnit)

        // Record on Hedera DLT
        try {
            val hederaTransactionId = hederaMainService.recordProductionUnitVerification(savedUnit)
            savedUnit.hederaTransactionId = hederaTransactionId
            if (geoJsonPolygon != null) {
                savedUnit.hederaHash = calculatePolygonHash(geoJsonPolygon)
            }
            productionUnitRepository.save(savedUnit)
        } catch (e: Exception) {
            logger.warn("Failed to record production unit on Hedera DLT", e)
        }

        logger.info("Created production unit ${savedUnit.id} for farmer ${farmer.id} with area ${areaHectares} hectares")
        return savedUnit
    }

    /**
     * Updates an existing production unit.
     * Geometry updates are blocked if the unit is locked (after batch assignment).
     */
    fun updateProductionUnit(
        unitId: String,
        unitName: String? = null,
        geoJsonPolygon: String? = null,
        geolocationPoint: String? = null,
        administrativeRegion: String? = null,
        countryCode: String? = null
    ): ProductionUnit {

        val existingUnit = productionUnitRepository.findById(unitId)
            .orElseThrow { IllegalArgumentException("Production unit not found: $unitId") }

        // EUDR Compliance: Prevent geometry modification on locked units
        if (existingUnit.isLocked && (geoJsonPolygon != null || geolocationPoint != null)) {
            throw IllegalStateException(
                "EUDR Compliance Error: Cannot modify geometry of locked production unit. " +
                "Unit was locked after batch assignment or deforestation verification to maintain audit trail."
            )
        }

        var geometryChanged = false

        // Update fields if provided
        unitName?.let { existingUnit.unitName = it }
        administrativeRegion?.let { existingUnit.administrativeRegion = it }
        countryCode?.let { existingUnit.countryCode = it }

        // Update geometry if provided
        geoJsonPolygon?.let { geoJson ->
            val newGeometry = validateAndParseGeoJson(geoJson)
            val newAreaHectares = calculateAreaInHectares(newGeometry)
            val newWgs84Coordinates = extractWgs84Coordinates(newGeometry)

            existingUnit.parcelGeometry = newGeometry
            existingUnit.areaHectares = newAreaHectares
            existingUnit.wgs84Coordinates = newWgs84Coordinates
            existingUnit.lastVerifiedAt = LocalDateTime.now()
            geometryChanged = true
        }

        val updatedUnit = productionUnitRepository.save(existingUnit)

        // Record geometry changes on Hedera DLT
        if (geometryChanged && geoJsonPolygon != null) {
            try {
                val hederaTransactionId = hederaConsensusService.recordProductionUnitVerification(updatedUnit)
                updatedUnit.hederaTransactionId = hederaTransactionId
                updatedUnit.hederaHash = calculatePolygonHash(geoJsonPolygon)
                productionUnitRepository.save(updatedUnit)
            } catch (e: Exception) {
                logger.warn("Failed to record production unit update on Hedera DLT", e)
            }
        }

        logger.info("Updated production unit $unitId")
        return updatedUnit
    }

    fun getProductionUnitsByFarmer(farmerId: String): List<ProductionUnit> {
        return productionUnitRepository.findByFarmerId(farmerId)
    }

    fun getProductionUnitById(unitId: String): ProductionUnit? {
        return productionUnitRepository.findById(unitId).orElse(null)
    }

    fun deleteProductionUnit(unitId: String) {
        val unit = productionUnitRepository.findById(unitId)
            .orElseThrow { IllegalArgumentException("Production unit not found: $unitId") }

        productionUnitRepository.delete(unit)
        logger.info("Deleted production unit $unitId")
    }

    /**
     * Update country code for a specific production unit by detecting from its geometry.
     * Returns the detected country code or null if detection fails.
     */
    fun updateCountryCodeForUnit(unitId: String): String? {
        val unit = productionUnitRepository.findById(unitId)
            .orElseThrow { IllegalArgumentException("Production unit not found: $unitId") }

        if (unit.parcelGeometry == null) {
            logger.warn("Production unit $unitId has no geometry - cannot detect country")
            return null
        }

        val detectedCountry = detectCountryFromGeometry(unit.parcelGeometry!!)
        if (detectedCountry != null) {
            unit.countryCode = detectedCountry
            productionUnitRepository.save(unit)
            logger.info("Updated production unit $unitId with country code: $detectedCountry")
        }
        return detectedCountry
    }

    /**
     * Backfill country codes for all production units that have null country_code.
     * Returns count of updated units.
     */
    fun backfillCountryCodesForAllUnits(): Int {
        val allUnits = productionUnitRepository.findAll()
        var updatedCount = 0

        allUnits.filter { it.countryCode.isNullOrBlank() && it.parcelGeometry != null }.forEach { unit ->
            try {
                val detectedCountry = detectCountryFromGeometry(unit.parcelGeometry!!)
                if (detectedCountry != null) {
                    unit.countryCode = detectedCountry
                    productionUnitRepository.save(unit)
                    updatedCount++
                    logger.info("Backfilled country code $detectedCountry for unit ${unit.id}")
                }
                // Add small delay to avoid rate limiting on Nominatim API
                Thread.sleep(1100)  // Nominatim requires 1 request per second
            } catch (e: Exception) {
                logger.error("Failed to backfill country code for unit ${unit.id}", e)
            }
        }

        logger.info("Backfilled country codes for $updatedCount production units")
        return updatedCount
    }

    /**
     * Get country code for a production unit, detecting from geometry if not set.
     * This can be called during risk assessment for units without country codes.
     */
    fun getOrDetectCountryCode(unit: ProductionUnit): String? {
        // If already set, return it
        if (!unit.countryCode.isNullOrBlank()) {
            return unit.countryCode
        }

        // Try to detect from geometry
        if (unit.parcelGeometry != null) {
            val detectedCountry = detectCountryFromGeometry(unit.parcelGeometry!!)
            if (detectedCountry != null) {
                // Save it for future use
                unit.countryCode = detectedCountry
                productionUnitRepository.save(unit)
                logger.info("Detected and saved country code $detectedCountry for unit ${unit.id}")
            }
            return detectedCountry
        }

        return null
    }


    /**
     * Get all production units from farmers connected to an exporter
     * This retrieves production units from all farmers who have a relationship with the specified exporter
     */
    fun getProductionUnitsForExporter(exporterId: String): List<ProductionUnit> {
        logger.info("Fetching production units for exporter: $exporterId")
        
        // Get all farmer-exporter relationships for this exporter
        val relationships = farmerExporterRelationshipRepository.findByExporterId(exporterId)
        
        if (relationships.isEmpty()) {
            logger.info("No farmers connected to exporter $exporterId")
            return emptyList()
        }
        
        logger.info("Found ${relationships.size} farmers connected to exporter $exporterId")
        
        // Get production units for all connected farmers
        val productionUnits = mutableListOf<ProductionUnit>()
        
        relationships.forEach { relationship ->
            val units = productionUnitRepository.findByFarmerId(relationship.farmer.id!!)
            // Eagerly load the farmer relationship to avoid lazy loading issues
            units.forEach { unit ->
                unit.farmer // Access to initialize the lazy proxy
            }
            productionUnits.addAll(units)
        }
        
        logger.info("Found ${productionUnits.size} production units for exporter $exporterId")
        return productionUnits
    }

    fun findProductionUnitsWithinArea(geoJsonArea: String): List<ProductionUnit> {
        val areaGeometry = validateAndParseGeoJson(geoJsonArea)
        return productionUnitRepository.findByParcelGeometryWithin(areaGeometry)
    }

    fun findProductionUnitsIntersectingArea(geoJsonArea: String): List<ProductionUnit> {
        val areaGeometry = validateAndParseGeoJson(geoJsonArea)
        return productionUnitRepository.findByParcelGeometryIntersects(areaGeometry)
    }

    fun findProductionUnitsWithinDistance(
        centerPoint: String,
        distanceKm: Double
    ): List<ProductionUnit> {
        val pointGeometry = validateAndParseGeoJson(centerPoint)
        return productionUnitRepository.findByParcelGeometryWithinDistance(pointGeometry, distanceKm * 1000) // Convert to meters
    }

    fun validatePolygonIntegrity(unitId: String): Boolean {
        val unit = getProductionUnitById(unitId) ?: return false

        return try {
            if (unit.hederaTransactionId != null && unit.parcelGeometry != null) {
                val currentGeoJson = geoJsonWriter.write(unit.parcelGeometry)
                val currentHash = calculatePolygonHash(currentGeoJson.toString())

                // Verify against stored hash
                val hashMatches = unit.hederaHash == currentHash

                // Verify against Hedera DLT
                val hederaVerified = hederaConsensusService.verifyRecordIntegrity(unit.hederaTransactionId!!)

                hashMatches && hederaVerified
            } else {
                false
            }
        } catch (e: Exception) {
            logger.error("Failed to validate polygon integrity for unit $unitId", e)
            false
        }
    }

    fun exportProductionUnitAsGeoJson(unitId: String): String? {
        val unit = getProductionUnitById(unitId) ?: return null

        return try {
            if (unit.parcelGeometry != null) {
                val geoJsonGeometry = geoJsonWriter.write(unit.parcelGeometry)

                // Create complete GeoJSON feature
                val feature = mapOf(
                    "type" to "Feature",
                    "properties" to mapOf(
                        "unitId" to unit.id,
                        "unitName" to unit.unitName,
                        "farmerId" to unit.farmer.id,
                        "areaHectares" to unit.areaHectares,
                        "administrativeRegion" to unit.administrativeRegion,
                        "lastVerified" to unit.lastVerifiedAt?.toString(),
                        "hederaVerified" to (unit.hederaTransactionId != null)
                    ),
                    "geometry" to objectMapper.readValue(geoJsonGeometry.toString(), Map::class.java)
                )

                objectMapper.writeValueAsString(feature)
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error("Failed to export production unit as GeoJSON", e)
            null
        }
    }

    fun importProductionUnitsFromGeoJson(
        farmer: Farmer,
        geoJsonFeatureCollection: String
    ): List<ProductionUnit> {

        return try {
            val featureCollection = objectMapper.readValue(geoJsonFeatureCollection, Map::class.java)
            val features = featureCollection["features"] as? List<Map<String, Any>>
                ?: throw IllegalArgumentException("Invalid GeoJSON FeatureCollection")

            val importedUnits = mutableListOf<ProductionUnit>()

            features.forEach { feature ->
                val properties = feature["properties"] as? Map<String, Any> ?: emptyMap()
                val geometry = feature["geometry"] as? Map<String, Any>
                    ?: throw IllegalArgumentException("Feature missing geometry")

                val unitName = properties["name"] as? String
                    ?: properties["unitName"] as? String
                    ?: "Imported Unit ${System.currentTimeMillis()}"

                val administrativeRegion = properties["administrativeRegion"] as? String
                    ?: properties["region"] as? String

                val geometryGeoJson = objectMapper.writeValueAsString(geometry)

                val unit = createProductionUnit(
                    farmer = farmer,
                    unitName = unitName,
                    geoJsonPolygon = geometryGeoJson,
                    administrativeRegion = administrativeRegion
                )

                importedUnits.add(unit)
            }

            logger.info("Imported ${importedUnits.size} production units for farmer ${farmer.id}")
            importedUnits
        } catch (e: Exception) {
            logger.error("Failed to import production units from GeoJSON", e)
            throw RuntimeException("Failed to import production units", e)
        }
    }

    private fun validateAndParseGeoJson(geoJson: String): Geometry {
        return try {
            val geometry = geoJsonReader.read(geoJson)

            // Validate geometry type
            if (geometry !is Polygon && geometry !is MultiPolygon) {
                throw IllegalArgumentException("Geometry must be a Polygon or MultiPolygon")
            }

            // Validate coordinate system (should be WGS84)
            if (geometry.srid != 4326) {
                geometry.srid = 4326
            }

            // Validate minimum area (e.g., at least 100 square meters)
            val area = calculateAreaInSquareMeters(geometry)
            if (area < 100) {
                throw IllegalArgumentException("Polygon area too small (minimum 100 square meters)")
            }

            // Validate coordinate precision (should have reasonable precision)
            validateCoordinatePrecision(geometry)

            geometry
        } catch (e: Exception) {
            logger.error("Failed to validate GeoJSON: $geoJson", e)
            throw IllegalArgumentException("Invalid GeoJSON polygon: ${e.message}", e)
        }
    }

    private fun validateCoordinatePrecision(geometry: Geometry) {
        val coordinates = geometry.coordinates
        coordinates.forEach { coord ->
            // Check longitude range (-180 to 180)
            if (coord.x < -180 || coord.x > 180) {
                throw IllegalArgumentException("Invalid longitude: ${coord.x}")
            }

            // Check latitude range (-90 to 90)
            if (coord.y < -90 || coord.y > 90) {
                throw IllegalArgumentException("Invalid latitude: ${coord.y}")
            }

            // Check precision (should not have more than 8 decimal places)
            val lonPrecision = coord.x.toString().substringAfter(".").length
            val latPrecision = coord.y.toString().substringAfter(".").length

            if (lonPrecision > 8 || latPrecision > 8) {
                throw IllegalArgumentException("Coordinate precision too high (max 8 decimal places)")
            }
        }
    }

    private fun calculateAreaInHectares(geometry: Geometry): BigDecimal {
        val areaSquareMeters = calculateAreaInSquareMeters(geometry)
        return BigDecimal(areaSquareMeters / 10000.0).setScale(4, RoundingMode.HALF_UP)
    }

    private fun calculateAreaInSquareMeters(geometry: Geometry): Double {
        // For accurate area calculation, we should use a projected coordinate system
        // For now, we'll use a simple approximation for WGS84
        return geometry.area * 111319.9 * 111319.9 * Math.cos(Math.toRadians(geometry.centroid.y))
    }

    private fun extractWgs84Coordinates(geometry: Geometry): String {
        return try {
            val coordinates = geometry.coordinates
            val coordList = coordinates.map { "${it.x},${it.y}" }
            coordList.joinToString(";")
        } catch (e: Exception) {
            logger.warn("Failed to extract WGS84 coordinates", e)
            ""
        }
    }

    private fun calculatePolygonHash(geoJson: String): String {
        return try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val hashBytes = messageDigest.digest(geoJson.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            logger.error("Failed to calculate polygon hash", e)
            ""
        }
    }

    /**
     * Detect country code from geometry using reverse geocoding API.
     * Uses Nominatim (OpenStreetMap) free reverse geocoding service.
     */
    private fun detectCountryFromGeometry(geometry: Geometry): String? {
        return try {
            val centroid = geometry.centroid
            val lat = centroid.y
            val lon = centroid.x

            logger.info("Detecting country from coordinates using reverse geocoding: lat=$lat, lon=$lon")

            // Use Nominatim reverse geocoding API (free, no API key required)
            // Format: https://nominatim.openstreetmap.org/reverse?lat=X&lon=Y&format=json
            val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json&zoom=3"

            val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "AgriBackup-EUDR-Compliance/1.0")
            connection.setRequestProperty("Accept", "application/json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = objectMapper.readTree(response)

                // Extract country code from address object
                val address = jsonResponse.get("address")
                val countryCode2 = address?.get("country_code")?.asText()?.uppercase()

                // Convert ISO 3166-1 alpha-2 to alpha-3
                val countryCode3 = countryCode2?.let { convertToAlpha3(it) }

                if (countryCode3 != null) {
                    logger.info("Detected country $countryCode3 (from $countryCode2) via reverse geocoding for lat=$lat, lon=$lon")
                } else {
                    logger.warn("Could not extract country code from reverse geocoding response for lat=$lat, lon=$lon")
                }

                countryCode3
            } else {
                logger.warn("Reverse geocoding API returned status ${connection.responseCode} for lat=$lat, lon=$lon")
                null
            }
        } catch (e: Exception) {
            logger.error("Failed to detect country from geometry via reverse geocoding", e)
            null
        }
    }

    /**
     * Convert ISO 3166-1 alpha-2 country code to alpha-3
     */
    private fun convertToAlpha3(alpha2: String): String? {
        val mapping = mapOf(
            "KE" to "KEN", "ET" to "ETH", "UG" to "UGA", "TZ" to "TZA", "RW" to "RWA",
            "BI" to "BDI", "GH" to "GHA", "CI" to "CIV", "NG" to "NGA", "CM" to "CMR",
            "CD" to "COD", "BR" to "BRA", "CO" to "COL", "ID" to "IDN", "VN" to "VNM",
            "MY" to "MYS", "IN" to "IND", "PE" to "PER", "EC" to "ECU", "GT" to "GTM",
            "HN" to "HND", "CR" to "CRI", "NI" to "NIC", "MX" to "MEX", "SV" to "SLV",
            "PA" to "PAN", "PH" to "PHL", "TH" to "THA", "MM" to "MMR", "LA" to "LAO",
            "KH" to "KHM", "PG" to "PNG", "LR" to "LBR", "SL" to "SLE", "GN" to "GIN",
            "SN" to "SEN", "ML" to "MLI", "BF" to "BFA", "NE" to "NER", "TD" to "TCD",
            "CF" to "CAF", "CG" to "COG", "AO" to "AGO", "ZM" to "ZMB", "ZW" to "ZWE",
            "MZ" to "MOZ", "MW" to "MWI", "MG" to "MDG", "SO" to "SOM", "DJ" to "DJI",
            "ER" to "ERI", "SS" to "SSD", "SD" to "SDN", "EG" to "EGY", "LY" to "LBY",
            "TN" to "TUN", "DZ" to "DZA", "MA" to "MAR", "MR" to "MRT", "CV" to "CPV",
            "GM" to "GMB", "GW" to "GNB", "TG" to "TGO", "BJ" to "BEN", "GA" to "GAB",
            "GQ" to "GNQ", "ST" to "STP", "NA" to "NAM", "BW" to "BWA", "LS" to "LSO",
            "SZ" to "SWZ", "ZA" to "ZAF"
        )
        return mapping[alpha2.uppercase()]
    }

    // ========================================
    // EUDR COMPLIANCE VALIDATION METHODS
    // ========================================

    /**
     * Validates EUDR Article 9(1)(d) geolocation requirements.
     * 
     * Rules:
     * - Plots >4 hectares: Polygon geometry is MANDATORY
     * - Plots ≤4 hectares: Either polygon OR single geolocation point with radius is acceptable
     * - At least one geolocation method must be provided
     * 
     * @throws IllegalArgumentException if validation fails
     */
    private fun validateEudrGeolocationRequirements(
        geoJsonPolygon: String?,
        geolocationPoint: String?,
        radiusMeters: Double?
    ) {
        val hasPolygon = !geoJsonPolygon.isNullOrBlank()
        val hasPoint = !geolocationPoint.isNullOrBlank()

        // Must have at least one geolocation method
        if (!hasPolygon && !hasPoint) {
            throw IllegalArgumentException(
                "EUDR Compliance Error: At least one geolocation method is required. " +
                "Provide either polygon geometry or a single geolocation point (lat,lon) with radius."
            )
        }

        // If only point provided, must have radius
        if (!hasPolygon && hasPoint && radiusMeters == null) {
            throw IllegalArgumentException(
                "EUDR Compliance Error: When using geolocation point instead of polygon, " +
                "radius in meters is required to define the area scope."
            )
        }

        // Validate radius is positive
        if (radiusMeters != null && radiusMeters <= 0) {
            throw IllegalArgumentException(
                "EUDR Compliance Error: Radius must be a positive number (got: $radiusMeters meters)"
            )
        }

        // Calculate area from radius and check 4-hectare threshold
        if (radiusMeters != null && !hasPolygon) {
            val areaFromRadius = calculateAreaFromRadius(radiusMeters)
            if (areaFromRadius > EUDR_POLYGON_THRESHOLD_HECTARES) {
                throw IllegalArgumentException(
                    "EUDR Compliance Error: Plots exceeding 4 hectares require polygon geometry coordinates. " +
                    "A single geolocation point is only acceptable for plots of 4 hectares or less. " +
                    "(Calculated area from radius: ${areaFromRadius.setScale(2, java.math.RoundingMode.HALF_UP)} ha, " +
                    "Threshold: $EUDR_POLYGON_THRESHOLD_HECTARES ha, Radius: $radiusMeters m)"
                )
            }
        }

        // Validate geolocation point format if provided
        if (hasPoint) {
            validateGeolocationPointFormat(geolocationPoint!!)
        }

        logger.debug("EUDR geolocation validation passed: polygon=$hasPolygon, point=$hasPoint, radius=$radiusMeters")
    }

    /**
     * Calculates area in hectares from a circular radius.
     * Formula: Area = π * r² (in square meters), then convert to hectares.
     * 1 hectare = 10,000 square meters
     */
    private fun calculateAreaFromRadius(radiusMeters: Double): BigDecimal {
        val areaSquareMeters = Math.PI * radiusMeters * radiusMeters
        return BigDecimal(areaSquareMeters / 10000).setScale(4, java.math.RoundingMode.HALF_UP)
    }

    /**
     * Detects country from a geolocation point string using reverse geocoding.
     */
    private fun detectCountryFromPoint(point: String): String? {
        return try {
            val parts = point.split(",")
            if (parts.size != 2) return null
            
            val lat = parts[0].trim().toDouble()
            val lon = parts[1].trim().toDouble()
            
            // Create a temporary point geometry for reverse geocoding
            val pointGeom = geometryFactory.createPoint(Coordinate(lon, lat))
            detectCountryFromGeometry(pointGeom)
        } catch (e: Exception) {
            logger.warn("Failed to detect country from point: $point", e)
            null
        }
    }

    /**
     * Validates geolocation point format (expected: "lat,lon")
     */
    private fun validateGeolocationPointFormat(point: String) {
        val parts = point.split(",")
        if (parts.size != 2) {
            throw IllegalArgumentException(
                "Invalid geolocation point format. Expected 'latitude,longitude' (e.g., '-1.2921,36.8219')"
            )
        }

        try {
            val lat = parts[0].trim().toDouble()
            val lon = parts[1].trim().toDouble()

            if (lat < -90 || lat > 90) {
                throw IllegalArgumentException("Latitude must be between -90 and 90 degrees (got: $lat)")
            }
            if (lon < -180 || lon > 180) {
                throw IllegalArgumentException("Longitude must be between -180 and 180 degrees (got: $lon)")
            }
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException(
                "Invalid geolocation coordinates. Latitude and longitude must be valid numbers."
            )
        }
    }

    /**
     * Locks a production unit to prevent geometry modifications.
     * Should be called after batch assignment or successful deforestation verification.
     * 
     * @param unitId The production unit ID to lock
     * @param reason The reason for locking (for audit trail)
     * @return The locked production unit
     */
    fun lockProductionUnit(unitId: String, reason: String): ProductionUnit {
        val unit = productionUnitRepository.findById(unitId)
            .orElseThrow { IllegalArgumentException("Production unit not found: $unitId") }

        if (unit.isLocked) {
            logger.info("Production unit $unitId is already locked")
            return unit
        }

        unit.isLocked = true
        val lockedUnit = productionUnitRepository.save(unit)
        
        logger.info("Locked production unit $unitId. Reason: $reason")
        
        // Record lock event on Hedera for immutable audit trail
        try {
            hederaConsensusService.recordProductionUnitLock(lockedUnit, reason)
        } catch (e: Exception) {
            logger.warn("Failed to record production unit lock on Hedera DLT", e)
        }

        return lockedUnit
    }

    /**
     * Checks if a production unit is locked.
     */
    fun isProductionUnitLocked(unitId: String): Boolean {
        return productionUnitRepository.findById(unitId)
            .map { it.isLocked }
            .orElse(false)
    }
}