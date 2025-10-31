package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.common.enums.FarmerProduceStatus
import com.agriconnect.farmersportalapis.domain.profile.FarmerProduce
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FarmerProduceRepository :  JpaRepository<FarmerProduce, String>{
    @Query("""
        SELECT DISTINCT fp FROM FarmerProduce fp
        WHERE 
        (LOWER(fp.farmProduce.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(fp.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        AND (:status IS NULL OR fp.status != :status)
    """)
    fun searchByNameOrDescription(searchTerm: String, status: FarmerProduceStatus): List<FarmerProduce>
    
    @Query("""
        SELECT DISTINCT fp, 
        (6371 * acos(cos(radians(:latitude)) * cos(radians(l.latitude)) * 
        cos(radians(l.longitude) - radians(:longitude)) + 
        sin(radians(:latitude)) * sin(radians(l.latitude)))) AS distance
        FROM FarmerProduce fp
        JOIN Location l ON l.farmer.id = fp.farmer.id
        WHERE (6371 * acos(cos(radians(:latitude)) * cos(radians(l.latitude)) * 
        cos(radians(l.longitude) - radians(:longitude)) + 
        sin(radians(:latitude)) * sin(radians(l.latitude)))) <= :maxDistance
        ORDER BY (sin(radians(:latitude)) * sin(radians(l.latitude)))
    """)
    fun searchByLocation(latitude: Double, longitude: Double, maxDistance: Double): List<Array<Any>>

    fun findByFarmerId(farmerId: String?): List<FarmerProduce>

    @Query("""
        SELECT fp FROM FarmerProduce fp
        where fp.farmer.id in (:farmerIds)
    """)
    fun findByFarmerIdIn(farmerIds: List<String>): List<FarmerProduce>

    @Query("""
        SELECT fp FROM FarmerProduce fp
        JOIN FarmerExporterRelationship fer ON fer.farmer.id = fp.farmer.id
        WHERE fer.exporter.id = :exporterId
    """)
    fun findByExporterId(exporterId: String): List<FarmerProduce>

    @Query("""
        SELECT fp FROM FarmerProduce fp
        WHERE fp.farmer.id = :farmerId
    """)
    fun findByFarmerIdSimple(farmerId: String): List<FarmerProduce>

    @Query("""
        SELECT fp FROM FarmerProduce fp
        JOIN FarmerExporterRelationship fer ON fer.farmer.id = fp.farmer.id
        WHERE fer.exporter.id = :exporterId
          AND fp.predictedHarvestDate BETWEEN :start AND :end
          AND (fp.status <> 'HARVESTED')
    """)
    fun findPredictedHarvestsForExporter(
        exporterId: String,
        start: java.time.LocalDate,
        end: java.time.LocalDate
    ): List<FarmerProduce>
}