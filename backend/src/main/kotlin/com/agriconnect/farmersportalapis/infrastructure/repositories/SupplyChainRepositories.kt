package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.eudr.ProcessingEvent
import com.agriconnect.farmersportalapis.domain.eudr.Processor
import com.agriconnect.farmersportalapis.domain.eudr.SupplyChainEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SupplyChainEventRepository : JpaRepository<SupplyChainEvent, String> {
    fun findByBatchId(batchId: String): List<SupplyChainEvent>
    fun findByFromEntityId(fromEntityId: String): List<SupplyChainEvent>
    fun findByToEntityId(toEntityId: String): List<SupplyChainEvent>
}

@Repository
interface ProcessingEventRepository : JpaRepository<ProcessingEvent, String> {
    fun findByBatchId(batchId: String): List<ProcessingEvent>
    fun findByBatch_Id(batchId: String, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<ProcessingEvent>
    fun findByProcessor_Id(processorId: String): List<ProcessingEvent>
    fun findByProcessor_Id(processorId: String, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<ProcessingEvent>
    fun countByProcessor_Id(processorId: String): Int
}

@Repository
interface ProcessorRepository : JpaRepository<Processor, String> {
    fun findByUserProfile(userProfile: UserProfile): Processor?
    fun findByFacilityName(facilityName: String): List<Processor>
    fun findByUserProfile_Id(userId: String): Processor?
    fun findByVerificationStatus(status: com.agriconnect.farmersportalapis.domain.eudr.ProcessorVerificationStatus, pageable: org.springframework.data.domain.Pageable): org.springframework.data.domain.Page<com.agriconnect.farmersportalapis.domain.eudr.Processor>
}

@Repository
interface FarmerCollectionRepository : JpaRepository<com.agriconnect.farmersportalapis.domain.eudr.FarmerCollection, String> {
    fun findByAggregatorId(aggregatorId: String): List<com.agriconnect.farmersportalapis.domain.eudr.FarmerCollection>
    fun findByFarmerId(farmerId: String): List<com.agriconnect.farmersportalapis.domain.eudr.FarmerCollection>
    fun findByBatchId(batchId: String): List<com.agriconnect.farmersportalapis.domain.eudr.FarmerCollection>
    fun findByPaymentStatus(paymentStatus: com.agriconnect.farmersportalapis.domain.eudr.PaymentStatus): List<com.agriconnect.farmersportalapis.domain.eudr.FarmerCollection>
    fun findByAggregatorIdAndPaymentStatus(aggregatorId: String, paymentStatus: com.agriconnect.farmersportalapis.domain.eudr.PaymentStatus): List<com.agriconnect.farmersportalapis.domain.eudr.FarmerCollection>
}

@Repository
interface BatchShipmentRepository : JpaRepository<com.agriconnect.farmersportalapis.domain.eudr.BatchShipment, String> {
    fun findByBatchId(batchId: String): com.agriconnect.farmersportalapis.domain.eudr.BatchShipment?
    fun findByImporterId(importerId: String): List<com.agriconnect.farmersportalapis.domain.eudr.BatchShipment>
    fun findByShipmentNumber(shipmentNumber: String): com.agriconnect.farmersportalapis.domain.eudr.BatchShipment?
    fun findByShipmentStatus(status: com.agriconnect.farmersportalapis.domain.eudr.ShipmentStatus): List<com.agriconnect.farmersportalapis.domain.eudr.BatchShipment>
    fun findByImporterIdAndShipmentStatus(importerId: String, status: com.agriconnect.farmersportalapis.domain.eudr.ShipmentStatus): List<com.agriconnect.farmersportalapis.domain.eudr.BatchShipment>
}

@Repository
interface BatchInspectionRepository : JpaRepository<com.agriconnect.farmersportalapis.domain.eudr.BatchInspection, String> {
    fun findByBatchId(batchId: String): List<com.agriconnect.farmersportalapis.domain.eudr.BatchInspection>
    fun findByShipmentId(shipmentId: String): List<com.agriconnect.farmersportalapis.domain.eudr.BatchInspection>
    fun findByInspectionResult(result: com.agriconnect.farmersportalapis.domain.eudr.InspectionResult): List<com.agriconnect.farmersportalapis.domain.eudr.BatchInspection>
    fun findByInspectionType(type: String): List<com.agriconnect.farmersportalapis.domain.eudr.BatchInspection>
}