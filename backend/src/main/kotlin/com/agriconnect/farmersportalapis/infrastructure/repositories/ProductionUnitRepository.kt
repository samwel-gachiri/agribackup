package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import org.locationtech.jts.geom.Geometry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductionUnitRepository : JpaRepository<ProductionUnit, String> {
    
    fun findByFarmer(farmer: Farmer): List<ProductionUnit>
    
    fun findByFarmerId(farmerId: String): List<ProductionUnit>
    
    @Query("SELECT p FROM ProductionUnit p WHERE p.unitName LIKE %:name%")
    fun findByUnitNameContaining(@Param("name") name: String): List<ProductionUnit>
    
    @Query("SELECT p FROM ProductionUnit p WHERE p.administrativeRegion = :region")
    fun findByAdministrativeRegion(@Param("region") region: String): List<ProductionUnit>
    
    @Query(value = "SELECT * FROM production_units p WHERE ST_Intersects(p.parcel_geojson, :geometry)", nativeQuery = true)
    fun findByParcelGeometryIntersects(@Param("geometry") geometry: Geometry): List<ProductionUnit>
    
    @Query(value = "SELECT * FROM production_units p WHERE ST_Within(p.parcel_geojson, :geometry)", nativeQuery = true)
    fun findByParcelGeometryWithin(@Param("geometry") geometry: Geometry): List<ProductionUnit>
    
    @Query(value = "SELECT * FROM production_units p WHERE ST_DWithin(p.parcel_geojson, :geometry, :distance)", nativeQuery = true)
    fun findByParcelGeometryWithinDistance(
        @Param("geometry") geometry: Geometry,
        @Param("distance") distance: Double
    ): List<ProductionUnit>
    
    @Query(value = "SELECT * FROM production_units p WHERE ST_Intersects(p.parcel_geojson, :geometry)", nativeQuery = true)
    fun findIntersectingGeometries(@Param("geometry") geometry: Geometry): List<ProductionUnit>
}