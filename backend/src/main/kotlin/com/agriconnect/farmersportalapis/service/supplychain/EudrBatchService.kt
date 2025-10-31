package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.application.dtos.BatchResponseDto
import com.agriconnect.farmersportalapis.application.dtos.CreateBatchRequestDto
import com.agriconnect.farmersportalapis.domain.eudr.BatchProductionUnit
import com.agriconnect.farmersportalapis.domain.eudr.BatchStatus
import com.agriconnect.farmersportalapis.domain.eudr.EudrBatch
import com.agriconnect.farmersportalapis.infrastructure.repositories.CountryRiskRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrBatchRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProductionUnitRepository
import com.agriconnect.farmersportalapis.service.hedera.HederaConsensusServices
import com.agriconnect.farmersportalapis.service.hedera.HederaMainService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class EudrBatchService(
    private val eudrBatchRepository: EudrBatchRepository,
    private val productionUnitRepository: ProductionUnitRepository,
    private val countryRiskRepository: CountryRiskRepository,
    private val hederaConsensusService: HederaConsensusServices,
    private val hederaMainService: HederaMainService,
) {

    private val logger = LoggerFactory.getLogger(EudrBatchService::class.java)

    @Transactional
    fun createBatch(request: CreateBatchRequestDto, createdBy: String): EudrBatch {
        logger.info("Creating new EUDR batch: ${request.batchCode} by user: $createdBy")

        // Validate batch code uniqueness
        if (eudrBatchRepository.existsByBatchCode(request.batchCode)) {
            throw IllegalArgumentException("Batch code ${request.batchCode} already exists")
        }

        // Get country risk level
        val countryRisk = countryRiskRepository.findByCountryCode(request.countryOfProduction)
            ?: throw IllegalArgumentException("Country ${request.countryOfProduction} not found in risk database")

        // Create the batch
        val batch = EudrBatch(
            batchCode = request.batchCode,
            commodityDescription = request.commodityDescription,
            hsCode = request.hsCode,
            quantity = request.quantity,
            unit = request.unit,
            countryOfProduction = request.countryOfProduction,
            countryRiskLevel = countryRisk.riskLevel,
            harvestDate = request.harvestDate,
            harvestPeriodStart = request.harvestPeriodStart,
            harvestPeriodEnd = request.harvestPeriodEnd,
            createdBy = createdBy,
            status = BatchStatus.CREATED,
            riskLevel = null,
            riskRationale = null,
            hederaTransactionId = null,
            hederaConsensusTimestamp = null
        )

        // Associate production units if provided
        if (request.productionUnitIds.isNotEmpty()) {
            val productionUnits = productionUnitRepository.findAllById(request.productionUnitIds)
            if (productionUnits.size != request.productionUnitIds.size) {
                throw IllegalArgumentException("Some production unit IDs not found")
            }

            // Distribute quantity equally among production units
            val quantityPerUnit = request.quantity.divide(BigDecimal(productionUnits.size))
            val percentagePerUnit = BigDecimal(100).divide(BigDecimal(productionUnits.size), 2, BigDecimal.ROUND_HALF_UP)

            productionUnits.forEach { productionUnit ->
                val batchProductionUnit = BatchProductionUnit(
                    batch = batch,
                    productionUnit = productionUnit,
                    quantityAllocated = quantityPerUnit,
                    percentageContribution = percentagePerUnit
                )
                batch.productionUnits.add(batchProductionUnit)
            }
        }

        // Save the batch
        val savedBatch = eudrBatchRepository.save(batch)

        // Record batch creation on Hedera for immutable audit trail
        try {
            val hederaTransactionId = hederaMainService.recordBatchCreation(savedBatch)
            savedBatch.hederaTransactionId = hederaTransactionId
            eudrBatchRepository.save(savedBatch)
            logger.info("Batch ${savedBatch.batchCode} recorded on Hedera with transaction ID: $hederaTransactionId")
        } catch (e: Exception) {
            logger.error("Failed to record batch creation on Hedera for batch ${savedBatch.batchCode}", e)
            // Don't fail the batch creation, but log the error
        }

        logger.info("Successfully created EUDR batch: ${savedBatch.batchCode} with ID: ${savedBatch.id}")
        return savedBatch
    }

    fun getBatchById(batchId: String): EudrBatch? {
        return eudrBatchRepository.findById(batchId).orElse(null)
    }

    fun getBatchByCode(batchCode: String): EudrBatch? {
        return eudrBatchRepository.findByBatchCode(batchCode)
    }

    fun getBatchesByCreator(createdBy: String): List<EudrBatch> {
        return eudrBatchRepository.findByCreatedBy(createdBy)
    }

    fun getBatchesByStatus(status: BatchStatus): List<EudrBatch> {
        return eudrBatchRepository.findByStatus(status)
    }

    @Transactional
    fun updateBatchStatus(batchId: String, newStatus: BatchStatus, updatedBy: String, reason: String? = null): EudrBatch {
        logger.info("Updating batch $batchId status to $newStatus by user: $updatedBy")

        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        val oldStatus = batch.status

        // Update status
        batch.status = newStatus
        batch.riskRationale = reason ?: batch.riskRationale

        val savedBatch = eudrBatchRepository.save(batch)

        // Record status change on Hedera
        try {
            hederaConsensusService.recordBatchStatusChange(savedBatch, oldStatus, newStatus)
            logger.info("Batch status change recorded on Hedera for batch ${savedBatch.batchCode}")
        } catch (e: Exception) {
            logger.error("Failed to record batch status change on Hedera for batch ${savedBatch.batchCode}", e)
        }

        return savedBatch
    }

    @Transactional
    fun transferBatch(batchId: String, toActorId: String, toActorRole: String, transferredBy: String, reason: String? = null): EudrBatch {
        logger.info("Transferring batch $batchId to actor $toActorId ($toActorRole) by user: $transferredBy")

        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        val fromActorId = batch.createdBy

        // Update batch status to IN_TRANSIT
        batch.status = BatchStatus.IN_TRANSIT
        batch.riskRationale = reason ?: "Transferred to $toActorRole: $toActorId"

        val savedBatch = eudrBatchRepository.save(batch)

        // Record batch transfer on Hedera
        try {
            hederaConsensusService.recordBatchTransfer(savedBatch, fromActorId, toActorId)
            logger.info("Batch transfer recorded on Hedera for batch ${savedBatch.batchCode}")
        } catch (e: Exception) {
            logger.error("Failed to record batch transfer on Hedera for batch ${savedBatch.batchCode}", e)
        }

        return savedBatch
    }

    fun getAllBatches(): List<EudrBatch> {
        return eudrBatchRepository.findAll()
    }

    fun convertToResponseDto(batch: EudrBatch): BatchResponseDto {
        return BatchResponseDto(
            id = batch.id,
            batchCode = batch.batchCode,
            commodityDescription = batch.commodityDescription,
            hsCode = batch.hsCode,
            quantity = batch.quantity,
            unit = batch.unit,
            countryOfProduction = batch.countryOfProduction,
            countryRiskLevel = batch.countryRiskLevel,
            harvestDate = batch.harvestDate,
            harvestPeriodStart = batch.harvestPeriodStart,
            harvestPeriodEnd = batch.harvestPeriodEnd,
            createdBy = batch.createdBy,
            createdAt = batch.createdAt.toString(),
            status = batch.status.name,
            riskLevel = batch.riskLevel?.name,
            riskRationale = batch.riskRationale,
            hederaTransactionId = batch.hederaTransactionId,
            productionUnitCount = batch.productionUnits.size,
            documentCount = batch.documents.size
        )
    }
}