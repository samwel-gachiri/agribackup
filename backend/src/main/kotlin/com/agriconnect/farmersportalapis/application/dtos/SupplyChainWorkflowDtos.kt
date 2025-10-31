package com.agriconnect.farmersportalapis.application.dtos

import java.math.BigDecimal
import java.time.LocalDateTime

// ===== CREATE WORKFLOW =====
data class CreateWorkflowRequestDto(
    val workflowName: String,
    val produceType: String
)

data class WorkflowResponseDto(
    val id: String,
    val exporterId: String,
    val workflowName: String,
    val produceType: String,
    val status: String,
    val currentStage: String,
    val totalQuantityKg: BigDecimal,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    
    // Accumulated quantities at each stage
    val totalCollected: BigDecimal,
    val totalConsolidated: BigDecimal,
    val totalProcessed: BigDecimal,
    val totalShipped: BigDecimal,
    
    // Available quantities for next stage
    val availableForConsolidation: BigDecimal,
    val availableForProcessing: BigDecimal,
    val availableForShipment: BigDecimal,
    
    // Event counts
    val collectionEventCount: Int,
    val consolidationEventCount: Int,
    val processingEventCount: Int,
    val shipmentEventCount: Int
)

// ===== COLLECTION EVENTS =====
data class AddCollectionEventRequestDto(
    val productionUnitId: String,
    val aggregatorId: String,
    val farmerId: String,
    val quantityCollectedKg: BigDecimal,
    val collectionDate: LocalDateTime,
    val qualityGrade: String?,
    val notes: String?
)

data class WorkflowCollectionEventResponseDto(
    val id: String,
    val workflowId: String,
    val productionUnitId: String,
    val productionUnitName: String,
    val aggregatorId: String,
    val aggregatorName: String,
    val farmerId: String,
    val farmerName: String,
    val quantityCollectedKg: BigDecimal,
    val collectionDate: LocalDateTime,
    val qualityGrade: String?,
    val notes: String?,
    val hederaHash: String?,
    val hederaTransactionId: String?,
    val createdAt: LocalDateTime
)

// ===== CONSOLIDATION EVENTS =====
data class AddConsolidationEventRequestDto(
    val aggregatorId: String,
    val processorId: String,
    val quantitySentKg: BigDecimal,
    val consolidationDate: LocalDateTime,
    val transportDetails: String?,
    val batchNumber: String?,
    val notes: String?
)

data class WorkflowConsolidationEventResponseDto(
    val id: String,
    val workflowId: String,
    val aggregatorId: String,
    val aggregatorName: String,
    val processorId: String,
    val processorName: String,
    val quantitySentKg: BigDecimal,
    val consolidationDate: LocalDateTime,
    val transportDetails: String?,
    val batchNumber: String?,
    val notes: String?,
    val hederaHash: String?,
    val hederaTransactionId: String?,
    val createdAt: LocalDateTime
)

// ===== PROCESSING EVENTS =====
data class AddProcessingEventRequestDto(
    val processorId: String,
    val quantityProcessedKg: BigDecimal,
    val processingDate: LocalDateTime,
    val processingType: String?,
    val outputQuantityKg: BigDecimal?,
    val processingNotes: String?
)

data class WorkflowProcessingEventResponseDto(
    val id: String,
    val workflowId: String,
    val processorId: String,
    val processorName: String,
    val quantityProcessedKg: BigDecimal,
    val processingDate: LocalDateTime,
    val processingType: String?,
    val outputQuantityKg: BigDecimal?,
    val processingNotes: String?,
    val hederaHash: String?,
    val hederaTransactionId: String?,
    val createdAt: LocalDateTime
)

// ===== SHIPMENT EVENTS =====
data class AddShipmentEventRequestDto(
    val processorId: String,
    val importerId: String,
    val quantityShippedKg: BigDecimal,
    val shipmentDate: LocalDateTime,
    val expectedArrivalDate: LocalDateTime?,
    val shippingCompany: String?,
    val trackingNumber: String?,
    val destinationPort: String?,
    val shipmentNotes: String?
)

data class WorkflowShipmentEventResponseDto(
    val id: String,
    val workflowId: String,
    val processorId: String,
    val processorName: String,
    val importerId: String,
    val importerName: String,
    val quantityShippedKg: BigDecimal,
    val shipmentDate: LocalDateTime,
    val expectedArrivalDate: LocalDateTime?,
    val actualArrivalDate: LocalDateTime?,
    val shippingCompany: String?,
    val trackingNumber: String?,
    val destinationPort: String?,
    val shipmentNotes: String?,
    val hederaHash: String?,
    val hederaTransactionId: String?,
    val createdAt: LocalDateTime
)

// ===== WORKFLOW SUMMARY =====
data class WorkflowSummaryDto(
    val workflow: WorkflowResponseDto,
    val collectionEvents: List<WorkflowCollectionEventResponseDto>,
    val consolidationEvents: List<WorkflowConsolidationEventResponseDto>,
    val processingEvents: List<WorkflowProcessingEventResponseDto>,
    val shipmentEvents: List<WorkflowShipmentEventResponseDto>
)

// ===== AVAILABLE QUANTITY INFO =====
data class AvailableQuantityDto(
    val aggregatorId: String,
    val aggregatorName: String,
    val totalCollected: BigDecimal,
    val totalSent: BigDecimal,
    val available: BigDecimal
)
