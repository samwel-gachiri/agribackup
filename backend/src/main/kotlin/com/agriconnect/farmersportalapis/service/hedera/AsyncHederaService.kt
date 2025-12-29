package com.agriconnect.farmersportalapis.service.hedera

import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import com.agriconnect.farmersportalapis.domain.supplychain.WorkflowCollectionEvent
import com.agriconnect.farmersportalapis.domain.supplychain.WorkflowConsolidationEvent
import com.agriconnect.farmersportalapis.domain.supplychain.WorkflowProcessingEvent
import com.agriconnect.farmersportalapis.domain.supplychain.WorkflowShipmentEvent
import com.agriconnect.farmersportalapis.repository.WorkflowCollectionEventRepository
import com.agriconnect.farmersportalapis.repository.WorkflowConsolidationEventRepository
import com.agriconnect.farmersportalapis.repository.WorkflowProcessingEventRepository
import com.agriconnect.farmersportalapis.repository.WorkflowShipmentEventRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture

/**
 * Async Hedera Service for non-blocking blockchain recording
 * 
 * This service handles all Hedera Consensus Service (HCS) and Hedera Token Service (HTS)
 * operations asynchronously to avoid blocking the main application thread.
 * 
 * Benefits:
 * - Faster response times for users
 * - Supply chain events are recorded immediately in database
 * - Blockchain recording happens in background
 * - Retry logic for failed transactions
 */
@Service
class AsyncHederaService(
    private val hederaMainService: HederaMainService,
    private val collectionEventRepository: WorkflowCollectionEventRepository,
    private val consolidationEventRepository: WorkflowConsolidationEventRepository,
    private val processingEventRepository: WorkflowProcessingEventRepository,
    private val shipmentEventRepository: WorkflowShipmentEventRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Async record production unit verification
     */
    @Async("hederaTaskExecutor")
    fun recordProductionUnitVerificationAsync(productionUnit: ProductionUnit): CompletableFuture<String?> {
        return CompletableFuture.supplyAsync {
            try {
                logger.info("Async recording production unit verification: ${productionUnit.id}")
                val transactionId = hederaMainService.recordProductionUnitVerification(productionUnit)
                logger.info("Production unit verification recorded: $transactionId")
                transactionId
            } catch (e: Exception) {
                logger.error("Failed to record production unit verification: ${productionUnit.id}", e)
                null
            }
        }
    }

    /**
     * Async record collection event and update with transaction ID
     */
    @Async("hederaTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordCollectionEventAsync(event: WorkflowCollectionEvent): CompletableFuture<String?> {
        return CompletableFuture.supplyAsync {
            try {
                logger.info("Async recording collection event: ${event.id}")
                val transactionId = hederaMainService.recordAggregationEvent(
                    eventId = event.id,
                    aggregatorId = event.collectorSupplier.id,
                    farmerId = event.farmer.id ?: "",
                    produceType = event.workflow.produceType,
                    quantityKg = event.quantityCollectedKg,
                    collectionDate = event.collectionDate.toLocalDate()
                )
                
                // Update event with transaction ID
                if (transactionId != null) {
                    val updated = collectionEventRepository.findById(event.id)
                    if (updated.isPresent) {
                        updated.get().hederaTransactionId = transactionId
                        collectionEventRepository.save(updated.get())
                    }
                }
                
                logger.info("Collection event recorded: $transactionId")
                transactionId
            } catch (e: Exception) {
                logger.error("Failed to record collection event: ${event.id}", e)
                null
            }
        }
    }

    /**
     * Async record consolidation event and update with transaction ID
     */
    @Async("hederaTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordConsolidationEventAsync(event: WorkflowConsolidationEvent): CompletableFuture<String?> {
        return CompletableFuture.supplyAsync {
            try {
                logger.info("Async recording consolidation event: ${event.id}")
                val transactionId = hederaMainService.recordConsolidatedBatch(
                    batchId = event.id,
                    batchNumber = event.batchNumber ?: "BATCH-${event.id.substring(0, 8)}",
                    aggregatorId = event.sourceSupplier.id,
                    produceType = event.workflow.produceType,
                    totalQuantityKg = event.quantitySentKg,
                    numberOfFarmers = 0, // Will be calculated from collection events
                    batchDataHash = generateHash(event)
                )
                
                // Update event with transaction ID
                if (transactionId != null) {
                    val updated = consolidationEventRepository.findById(event.id)
                    if (updated.isPresent) {
                        updated.get().hederaTransactionId = transactionId
                        consolidationEventRepository.save(updated.get())
                    }
                }
                
                logger.info("Consolidation event recorded: $transactionId")
                transactionId
            } catch (e: Exception) {
                logger.error("Failed to record consolidation event: ${event.id}", e)
                null
            }
        }
    }

    /**
     * Async record processing event and update with transaction ID
     */
    @Async("hederaTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordProcessingEventAsync(event: WorkflowProcessingEvent): CompletableFuture<String?> {
        return CompletableFuture.supplyAsync {
            try {
                logger.info("Async recording processing event: ${event.id}")
                
                // Use workflow processing event method that doesn't require a full entity
                val transactionId = hederaMainService.recordWorkflowProcessingEvent(
                    eventId = event.id,
                    processorId = event.processorSupplier.id,
                    processingType = event.processingType ?: "PROCESSING",
                    inputQuantity = event.quantityProcessedKg,
                    outputQuantity = event.outputQuantityKg ?: event.quantityProcessedKg,
                    processingDate = event.processingDate,
                    processingNotes = event.processingNotes
                )
                
                // Update event with transaction ID
                val updated = processingEventRepository.findById(event.id)
                if (updated.isPresent) {
                    updated.get().hederaTransactionId = transactionId
                    processingEventRepository.save(updated.get())
                }

                logger.info("Processing event recorded: $transactionId")
                transactionId
            } catch (e: Exception) {
                logger.error("Failed to record processing event: ${event.id}", e)
                null
            }
        }
    }

    /**
     * Async record shipment event and update with transaction ID
     */
    @Async("hederaTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordShipmentEventAsync(event: WorkflowShipmentEvent): CompletableFuture<String?> {
        return CompletableFuture.supplyAsync {
            try {
                logger.info("Async recording shipment event: ${event.id}")
                val transactionId = hederaMainService.recordImportShipment(
                    shipmentId = event.id,
                    importerId = event.importer.id,
                    shipmentNumber = event.trackingNumber ?: "SHP-${event.id.substring(0, 8)}",
                    produceType = event.workflow.produceType,
                    quantityKg = event.quantityShippedKg,
                    originCountry = "KE", // Default to Kenya, can be enhanced
                    shipmentDataHash = generateHash(event)
                )
                
                // Update event with transaction ID
                if (transactionId != null) {
                    val updated = shipmentEventRepository.findById(event.id)
                    if (updated.isPresent) {
                        updated.get().hederaTransactionId = transactionId
                        shipmentEventRepository.save(updated.get())
                    }
                }
                
                logger.info("Shipment event recorded: $transactionId")
                transactionId
            } catch (e: Exception) {
                logger.error("Failed to record shipment event: ${event.id}", e)
                null
            }
        }
    }

    /**
     * Async record authority submission
     */
    @Async("hederaTaskExecutor")
    fun recordAuthoritySubmissionAsync(
        submissionId: String,
        batchId: String,
        authorityCode: String,
        reportHash: String
    ): CompletableFuture<String?> {
        return CompletableFuture.supplyAsync {
            try {
                logger.info("Async recording authority submission: $submissionId")
                // Use existing recordInspectionResult for authority submissions
                val transactionId = hederaMainService.recordInspectionResult(
                    inspectionId = submissionId,
                    shipmentId = batchId,
                    documentType = "AUTHORITY_SUBMISSION_$authorityCode",
                    documentHash = reportHash
                )
                logger.info("Authority submission recorded: $transactionId")
                transactionId
            } catch (e: Exception) {
                logger.error("Failed to record authority submission: $submissionId", e)
                null
            }
        }
    }

    /**
     * Batch async recording - record multiple events in parallel
     */
    @Async("hederaTaskExecutor")
    fun recordMultipleEventsAsync(
        collectionEvents: List<WorkflowCollectionEvent> = emptyList(),
        consolidationEvents: List<WorkflowConsolidationEvent> = emptyList(),
        processingEvents: List<WorkflowProcessingEvent> = emptyList(),
        shipmentEvents: List<WorkflowShipmentEvent> = emptyList()
    ): CompletableFuture<Map<String, String?>> {
        return CompletableFuture.supplyAsync {
            val results = mutableMapOf<String, String?>()
            
            collectionEvents.forEach { event ->
                try {
                    val txId = recordCollectionEventAsync(event).get()
                    results["collection_${event.id}"] = txId
                } catch (e: Exception) {
                    logger.error("Failed to record collection event ${event.id}", e)
                    results["collection_${event.id}"] = null
                }
            }
            
            consolidationEvents.forEach { event ->
                try {
                    val txId = recordConsolidationEventAsync(event).get()
                    results["consolidation_${event.id}"] = txId
                } catch (e: Exception) {
                    logger.error("Failed to record consolidation event ${event.id}", e)
                    results["consolidation_${event.id}"] = null
                }
            }
            
            processingEvents.forEach { event ->
                try {
                    val txId = recordProcessingEventAsync(event).get()
                    results["processing_${event.id}"] = txId
                } catch (e: Exception) {
                    logger.error("Failed to record processing event ${event.id}", e)
                    results["processing_${event.id}"] = null
                }
            }
            
            shipmentEvents.forEach { event ->
                try {
                    val txId = recordShipmentEventAsync(event).get()
                    results["shipment_${event.id}"] = txId
                } catch (e: Exception) {
                    logger.error("Failed to record shipment event ${event.id}", e)
                    results["shipment_${event.id}"] = null
                }
            }
            
            results
        }
    }

    private fun generateHash(event: Any): String {
        val data = event.toString()
        return java.security.MessageDigest.getInstance("SHA-256")
            .digest(data.toByteArray())
            .fold("") { str, byte -> str + "%02x".format(byte) }
    }
}
