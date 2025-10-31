package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.eudr.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AggregatorRepository : JpaRepository<Aggregator, String> {

    fun findByUserProfile(userProfile: UserProfile): Aggregator?

    fun findByOrganizationName(organizationName: String): Aggregator?
    
    fun findByVerificationStatus(status: AggregatorVerificationStatus, pageable: Pageable): Page<Aggregator>
    
    @Query("SELECT a FROM Aggregator a WHERE a.userProfile.id = :userId")
    fun findByUserId(@Param("userId") userId: String): Aggregator?
    
    fun findByRegistrationNumber(registrationNumber: String): Aggregator?
    
    @Query("SELECT a FROM Aggregator a WHERE a.organizationType = :type")
    fun findByOrganizationType(@Param("type") type: AggregatorType): List<Aggregator>
}

@Repository
interface AggregationEventRepository : JpaRepository<AggregationEvent, String> {
    
    fun findByFarmerId(farmerId: String): List<AggregationEvent>
    fun findByAggregatorId(aggregatorId: String): List<AggregationEvent>

    fun findByAggregatorId(aggregatorId: String, page: Pageable): Page<AggregationEvent>

    fun findByConsolidatedBatchId(consolidatedBatchId: String): List<AggregationEvent>
    
    @Query("SELECT e FROM AggregationEvent e WHERE e.aggregator.id = :aggregatorId AND e.collectionDate BETWEEN :startDate AND :endDate")
    fun findByAggregatorAndDateRange(
        @Param("aggregatorId") aggregatorId: String,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<AggregationEvent>
    
    @Query("SELECT e FROM AggregationEvent e WHERE e.paymentStatus = :status")
    fun findByPaymentStatus(@Param("status") status: PaymentStatus): List<AggregationEvent>
    
    fun countByAggregatorIdAndPaymentStatus(aggregatorId: String, status: PaymentStatus): Int

    fun countByAggregatorId(aggregatorId: String): Int

    @Query("SELECT e FROM AggregationEvent e WHERE e.paymentStatus = :status AND e.aggregator.id = :aggregatorId")
    fun findByAggregatorAndPaymentStatus(
        @Param("aggregatorId") aggregatorId: String,
        @Param("status") status: PaymentStatus
    )

    fun findByAggregatorIdAndPaymentStatus(aggregatorId: String, status: PaymentStatus): List<AggregationEvent>

    @Query("SELECT e FROM AggregationEvent e WHERE e.aggregator.id = :aggregatorId AND e.paymentStatus = :status AND e.collectionDate BETWEEN :startDate AND :endDate")
    fun findByAggregatorAndPaymentStatusAndDateRange()
}

@Repository
interface ConsolidatedBatchRepository : JpaRepository<ConsolidatedBatch, String> {
    
    fun findByAggregatorId(aggregatorId: String, pageable: Pageable): Page<ConsolidatedBatch>
    
    fun findByBatchNumber(batchNumber: String): ConsolidatedBatch?
    
    fun findByStatus(status: ConsolidatedBatchStatus): List<ConsolidatedBatch>
    
    @Query("SELECT b FROM ConsolidatedBatch b WHERE b.aggregator.id = :aggregatorId AND b.status = :status")
    fun findByAggregatorAndStatus(
        @Param("aggregatorId") aggregatorId: String,
        @Param("status") status: ConsolidatedBatchStatus
    ): List<ConsolidatedBatch>
    
    @Query("SELECT b FROM ConsolidatedBatch b WHERE b.destinationEntityId = :entityId AND b.destinationEntityType = :entityType")
    fun findByDestination(
        @Param("entityId") entityId: String,
        @Param("entityType") entityType: String
    ): List<ConsolidatedBatch>

    fun countByAggregatorIdAndStatus(aggregatorId: String, status: ConsolidatedBatchStatus): Int
    fun countByAggregatorId(aggregatorId: String): Int

}

@Repository
interface ImporterRepository : JpaRepository<Importer, String> {

    fun findByUserProfile(userProfile: UserProfile): Importer?

    fun findByCompanyName(companyName: String): Importer?
    
    fun findByImportLicenseNumber(licenseNumber: String): Importer?
    
    fun findByVerificationStatus(status: ImporterVerificationStatus, pageable: Pageable): Page<Importer>
    
    @Query("SELECT i FROM Importer i WHERE i.userProfile.id = :userId")
    fun findByUserId(@Param("userId") userId: String): Importer?
    
    fun findByDestinationCountry(country: String): List<Importer>
}

@Repository
interface ImportShipmentRepository : JpaRepository<ImportShipment, String> {
    
    fun findByImporterId(importerId: String, pageable: Pageable): Page<ImportShipment>
    
    fun findByShipmentNumber(shipmentNumber: String): ImportShipment?
    
    fun findByStatus(status: ShipmentStatus): List<ImportShipment>
    
    fun findByEudrComplianceStatus(status: EudrComplianceStatus): List<ImportShipment>
    
    @Query("SELECT s FROM ImportShipment s WHERE s.importer.id = :importerId AND s.status = :status")
    fun findByImporterAndStatus(
        @Param("importerId") importerId: String,
        @Param("status") status: ShipmentStatus
    ): List<ImportShipment>
    
    @Query("SELECT s FROM ImportShipment s WHERE s.sourceBatchId = :batchId")
    fun findBySourceBatchId(@Param("batchId") batchId: String): List<ImportShipment>
    
    fun countByImporterIdAndStatus(importerId: String, status: ShipmentStatus): Int
    
    fun countByImporterIdAndEudrComplianceStatus(importerId: String, status: EudrComplianceStatus): Int
}

@Repository
interface CustomsDocumentRepository : JpaRepository<CustomsDocument, String> {
    
    fun findByShipmentId(shipmentId: String): List<CustomsDocument>
    
    fun findByDocumentType(documentType: String): List<CustomsDocument>
    
    fun findByChecksumSha256(checksum: String): CustomsDocument?
}

@Repository
interface InspectionRecordRepository : JpaRepository<InspectionRecord, String> {
    
    fun findByShipmentId(shipmentId: String): List<InspectionRecord>
    
    fun findByInspectionType(inspectionType: String): List<InspectionRecord>
    
    fun findByInspectionResult(result: InspectionResult): List<InspectionRecord>
    
    @Query("SELECT r FROM InspectionRecord r WHERE r.shipment.id = :shipmentId ORDER BY r.inspectionDate DESC")
    fun findByShipmentIdOrderByDateDesc(@Param("shipmentId") shipmentId: String): List<InspectionRecord>
}
