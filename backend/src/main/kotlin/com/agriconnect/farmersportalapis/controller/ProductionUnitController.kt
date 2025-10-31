package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.service.common.GeoJsonUtilService
import com.agriconnect.farmersportalapis.service.supplychain.ProductionUnitService
import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/production-units")
@Tag(name = "Production Units", description = "EUDR Production Unit Management")
class ProductionUnitController(
    private val productionUnitService: ProductionUnitService,
    private val geoJsonUtilService: GeoJsonUtilService,
    private val farmerRepository: FarmerRepository
) {
    
    private val logger = LoggerFactory.getLogger(ProductionUnitController::class.java)
    
    @PostMapping
    @Operation(summary = "Create a new production unit")
    @PreAuthorize("hasRole('FARMER') or hasRole('ZONE_SUPERVISOR') or hasRole('SYSTEM_ADMIN')")
    fun createProductionUnit(
        @RequestBody request: CreateProductionUnitRequest
    ): ResponseEntity<ApiResponse<ProductionUnit>> {
        
        return try {
            // Validate GeoJSON
            val validation = geoJsonUtilService.validateGeoJsonPolygon(request.geoJsonPolygon)
            if (!validation.isValid) {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("Invalid polygon: ${validation.errors.joinToString(", ")}")
                )
            }
            
            // Get farmer
            val farmer = farmerRepository.findById(request.farmerId).orElse(null)
                ?: return ResponseEntity.badRequest().body(
                    ApiResponse.error("Farmer not found: ${request.farmerId}")
                )
            
            val productionUnit = productionUnitService.createProductionUnit(
                farmer = farmer,
                unitName = request.unitName,
                geoJsonPolygon = request.geoJsonPolygon,
                administrativeRegion = request.administrativeRegion
            )
            
            ResponseEntity.ok(ApiResponse.success(productionUnit))
        } catch (e: Exception) {
            logger.error("Failed to create production unit", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to create production unit: ${e.message}"))
        }
    }
    
    @PutMapping("/{unitId}")
    @Operation(summary = "Update a production unit")
    @PreAuthorize("hasRole('FARMER') or hasRole('ZONE_SUPERVISOR') or hasRole('SYSTEM_ADMIN')")
    fun updateProductionUnit(
        @PathVariable unitId: String,
        @RequestBody request: UpdateProductionUnitRequest
    ): ResponseEntity<ApiResponse<ProductionUnit>> {
        
        return try {
            // Validate GeoJSON if provided
            request.geoJsonPolygon?.let { geoJson ->
                val validation = geoJsonUtilService.validateGeoJsonPolygon(geoJson)
                if (!validation.isValid) {
                    return ResponseEntity.badRequest().body(
                        ApiResponse.error("Invalid polygon: ${validation.errors.joinToString(", ")}")
                    )
                }
            }
            
            val productionUnit = productionUnitService.updateProductionUnit(
                unitId = unitId,
                unitName = request.unitName,
                geoJsonPolygon = request.geoJsonPolygon,
                administrativeRegion = request.administrativeRegion
            )
            
            ResponseEntity.ok(ApiResponse.success(productionUnit))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Invalid request"))
        } catch (e: Exception) {
            logger.error("Failed to update production unit", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to update production unit: ${e.message}"))
        }
    }
    
    @GetMapping("/farmer/{farmerId}")
    @Operation(summary = "Get production units by farmer")
    @PreAuthorize("hasRole('FARMER') or hasRole('BUYER') or hasRole('ZONE_SUPERVISOR') or hasRole('SYSTEM_ADMIN')")
    fun getProductionUnitsByFarmer(
        @PathVariable farmerId: String
    ): ResponseEntity<ApiResponse<List<ProductionUnit>>> {
        
        return try {
            val units = productionUnitService.getProductionUnitsByFarmer(farmerId)
            ResponseEntity.ok(ApiResponse.success(units))
        } catch (e: Exception) {
            logger.error("Failed to get production units for farmer $farmerId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get production units"))
        }
    }
    
    @GetMapping("/{unitId}")
    @Operation(summary = "Get production unit by ID")
    @PreAuthorize("hasRole('FARMER') or hasRole('BUYER') or hasRole('ZONE_SUPERVISOR') or hasRole('SYSTEM_ADMIN')")
    fun getProductionUnit(
        @PathVariable unitId: String
    ): ResponseEntity<ApiResponse<ProductionUnit>> {
        
        return try {
            val unit = productionUnitService.getProductionUnitById(unitId)
                ?: return ResponseEntity.notFound().build()
            
            ResponseEntity.ok(ApiResponse.success(unit))
        } catch (e: Exception) {
            logger.error("Failed to get production unit $unitId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get production unit"))
        }
    }
    
    @DeleteMapping("/{unitId}")
    @Operation(summary = "Delete a production unit")
    @PreAuthorize("hasRole('FARMER') or hasRole('SYSTEM_ADMIN')")
    fun deleteProductionUnit(
        @PathVariable unitId: String
    ): ResponseEntity<ApiResponse<String>> {
        
        return try {
            productionUnitService.deleteProductionUnit(unitId)
            ResponseEntity.ok(ApiResponse.success("Production unit deleted successfully"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().body(ApiResponse.error(e.message ?: "Invalid request"))
        } catch (e: Exception) {
            logger.error("Failed to delete production unit $unitId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete production unit"))
        }
    }
    
    @PostMapping("/validate-polygon")
    @Operation(summary = "Validate polygon coordinates")
    fun validatePolygon(
        @RequestBody request: ValidatePolygonRequest
    ): ResponseEntity<ApiResponse<GeoJsonUtilService.CoordinateValidationResult>> {
        
        return try {
            val validation = geoJsonUtilService.validateGeoJsonPolygon(request.geoJsonPolygon)
            ResponseEntity.ok(ApiResponse.success(validation))
        } catch (e: Exception) {
            logger.error("Failed to validate polygon", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to validate polygon"))
        }
    }
    
    @PostMapping("/calculate-metrics")
    @Operation(summary = "Calculate polygon metrics")
    fun calculatePolygonMetrics(
        @RequestBody request: ValidatePolygonRequest
    ): ResponseEntity<ApiResponse<GeoJsonUtilService.PolygonMetrics?>> {
        
        return try {
            val metrics = geoJsonUtilService.calculatePolygonMetrics(request.geoJsonPolygon)
            ResponseEntity.ok(ApiResponse.success(metrics))
        } catch (e: Exception) {
            logger.error("Failed to calculate polygon metrics", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to calculate metrics"))
        }
    }
    
    @GetMapping("/{unitId}/export")
    @Operation(summary = "Export production unit as GeoJSON")
    @PreAuthorize("hasRole('FARMER') or hasRole('ZONE_SUPERVISOR') or hasRole('SYSTEM_ADMIN')")
    fun exportProductionUnit(
        @PathVariable unitId: String
    ): ResponseEntity<ApiResponse<String>> {
        
        return try {
            val geoJson = productionUnitService.exportProductionUnitAsGeoJson(unitId)
                ?: return ResponseEntity.notFound().build()
            
            ResponseEntity.ok(ApiResponse.success(geoJson))
        } catch (e: Exception) {
            logger.error("Failed to export production unit $unitId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to export production unit"))
        }
    }
    
    @PostMapping("/farmer/{farmerId}/import")
    @Operation(summary = "Import production units from GeoJSON")
    @PreAuthorize("hasRole('FARMER') or hasRole('ZONE_SUPERVISOR') or hasRole('SYSTEM_ADMIN')")
    fun importProductionUnits(
        @PathVariable farmerId: String,
        @RequestBody request: ImportProductionUnitsRequest
    ): ResponseEntity<ApiResponse<List<ProductionUnit>>> {
        
        return try {
            val farmer = farmerRepository.findById(farmerId).orElse(null)
                ?: return ResponseEntity.badRequest().body(
                    ApiResponse.error("Farmer not found: $farmerId")
                )
            
            val units = productionUnitService.importProductionUnitsFromGeoJson(
                farmer = farmer,
                geoJsonFeatureCollection = request.geoJsonFeatureCollection
            )
            
            ResponseEntity.ok(ApiResponse.success(units))
        } catch (e: Exception) {
            logger.error("Failed to import production units for farmer $farmerId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to import production units: ${e.message}"))
        }
    }
    
    @PostMapping("/convert-esri")
    @Operation(summary = "Convert ESRI geometry to GeoJSON")
    fun convertEsriToGeoJson(
        @RequestBody request: ConvertEsriRequest
    ): ResponseEntity<ApiResponse<String?>> {
        
        return try {
            val geoJson = geoJsonUtilService.convertEsriToGeoJson(request.esriGeometry)
            ResponseEntity.ok(ApiResponse.success(geoJson))
        } catch (e: Exception) {
            logger.error("Failed to convert ESRI geometry", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to convert ESRI geometry"))
        }
    }
    
    @GetMapping("/{unitId}/verify-integrity")
    @Operation(summary = "Verify polygon integrity against Hedera DLT")
    @PreAuthorize("hasRole('FARMER') or hasRole('ZONE_SUPERVISOR') or hasRole('SYSTEM_ADMIN')")
    fun verifyPolygonIntegrity(
        @PathVariable unitId: String
    ): ResponseEntity<ApiResponse<Boolean>> {
        
        return try {
            val isValid = productionUnitService.validatePolygonIntegrity(unitId)
            ResponseEntity.ok(ApiResponse.success(isValid))
        } catch (e: Exception) {
            logger.error("Failed to verify polygon integrity for unit $unitId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to verify polygon integrity"))
        }
    }
    
    @PostMapping("/search/within-area")
    @Operation(summary = "Find production units within area")
    @PreAuthorize("hasRole('ZONE_SUPERVISOR') or hasRole('SYSTEM_ADMIN')")
    fun findProductionUnitsWithinArea(
        @RequestBody request: SpatialSearchRequest
    ): ResponseEntity<ApiResponse<List<ProductionUnit>>> {
        
        return try {
            val units = productionUnitService.findProductionUnitsWithinArea(request.geoJsonArea)
            ResponseEntity.ok(ApiResponse.success(units))
        } catch (e: Exception) {
            logger.error("Failed to search production units within area", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to search production units"))
        }
    }
    
    @PostMapping("/search/intersecting-area")
    @Operation(summary = "Find production units intersecting area")
    @PreAuthorize("hasRole('ZONE_SUPERVISOR') or hasRole('SYSTEM_ADMIN')")
    fun findProductionUnitsIntersectingArea(
        @RequestBody request: SpatialSearchRequest
    ): ResponseEntity<ApiResponse<List<ProductionUnit>>> {
        
        return try {
            val units = productionUnitService.findProductionUnitsIntersectingArea(request.geoJsonArea)
            ResponseEntity.ok(ApiResponse.success(units))
        } catch (e: Exception) {
            logger.error("Failed to search production units intersecting area", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to search production units"))
        }
    }

    @GetMapping("/exporter/{exporterId}/connected")
    @Operation(summary = "Get production units connected to exporter", description = "Returns all production units from farmers who are connected to this exporter through aggregators")
    @PreAuthorize("hasRole('EXPORTER') or hasRole('ADMIN')")
    fun getProductionUnitsConnectedToExporter(
        @PathVariable exporterId: String
    ): ResponseEntity<ApiResponse<List<ProductionUnit>>> {
        
        return try {
            val units = productionUnitService.getProductionUnitsForExporter(exporterId)
            ResponseEntity.ok(ApiResponse.success(units))
        } catch (e: Exception) {
            logger.error("Failed to get production units for exporter $exporterId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get production units"))
        }
    }
    
    // Request/Response DTOs
    data class CreateProductionUnitRequest(
        val farmerId: String,
        val unitName: String,
        val geoJsonPolygon: String,
        val administrativeRegion: String? = null
    )
    
    data class UpdateProductionUnitRequest(
        val unitName: String? = null,
        val geoJsonPolygon: String? = null,
        val administrativeRegion: String? = null
    )
    
    data class ValidatePolygonRequest(
        val geoJsonPolygon: String
    )
    
    data class ImportProductionUnitsRequest(
        val geoJsonFeatureCollection: String
    )
    
    data class ConvertEsriRequest(
        val esriGeometry: String
    )
    
    data class SpatialSearchRequest(
        val geoJsonArea: String
    )
    
    data class ApiResponse<T>(
        val success: Boolean,
        val data: T? = null,
        val message: String? = null
    ) {
        companion object {
            fun <T> success(data: T): ApiResponse<T> = ApiResponse(true, data)
            fun <T> error(message: String): ApiResponse<T> = ApiResponse(false, null, message)
        }
    }
}