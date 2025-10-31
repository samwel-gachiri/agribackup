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

    fun createProductionUnit(
        farmer: Farmer,
        unitName: String,
        geoJsonPolygon: String,
        administrativeRegion: String? = null
    ): ProductionUnit {

        // Validate and parse GeoJSON
        val geometry = validateAndParseGeoJson(geoJsonPolygon)

        // Calculate area in hectares
        val areaHectares = calculateAreaInHectares(geometry)

        // Extract WGS84 coordinates
        val wgs84Coordinates = extractWgs84Coordinates(geometry)

        // Create production unit
        val productionUnit = ProductionUnit(
            farmer = farmer,
            unitName = unitName,
            parcelGeometry = geometry,
            areaHectares = areaHectares,
            wgs84Coordinates = wgs84Coordinates,
            administrativeRegion = administrativeRegion,
            lastVerifiedAt = LocalDateTime.now(),
            hederaHash = null,
            hederaTransactionId = null
        )

        // Save to database
        val savedUnit = productionUnitRepository.save(productionUnit)

        // Record on Hedera DLT
        try {
            val hederaTransactionId = hederaMainService.recordProductionUnitVerification(savedUnit)
            savedUnit.hederaTransactionId = hederaTransactionId
            savedUnit.hederaHash = calculatePolygonHash(geoJsonPolygon)
            productionUnitRepository.save(savedUnit)
        } catch (e: Exception) {
            logger.warn("Failed to record production unit on Hedera DLT", e)
        }

        logger.info("Created production unit ${savedUnit.id} for farmer ${farmer.id} with area ${areaHectares} hectares")
        return savedUnit
    }

    fun updateProductionUnit(
        unitId: String,
        unitName: String? = null,
        geoJsonPolygon: String? = null,
        administrativeRegion: String? = null
    ): ProductionUnit {

        val existingUnit = productionUnitRepository.findById(unitId)
            .orElseThrow { IllegalArgumentException("Production unit not found: $unitId") }

        var geometryChanged = false

        // Update fields if provided
        unitName?.let { existingUnit.unitName = it }
        administrativeRegion?.let { existingUnit.administrativeRegion = it }

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
}