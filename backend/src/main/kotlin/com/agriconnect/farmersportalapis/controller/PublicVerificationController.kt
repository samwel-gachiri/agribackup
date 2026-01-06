package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.eudr.BatchStatus
import com.agriconnect.farmersportalapis.domain.eudr.RiskLevel
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrBatchRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * Public verification endpoints - NO AUTHENTICATION REQUIRED.
 * These endpoints allow consumers to verify product authenticity by scanning QR codes.
 */
@RestController
@RequestMapping("/api/public")
@Tag(name = "Public Verification", description = "Public endpoints for consumer product verification (no auth required)")
class PublicVerificationController(
    private val eudrBatchRepository: EudrBatchRepository
) {
    private val logger = LoggerFactory.getLogger(PublicVerificationController::class.java)

    @GetMapping("/verify/{batchCode}")
    @Operation(
        summary = "Verify product by batch code",
        description = "Public endpoint for consumers to verify product authenticity and view supply chain journey"
    )
    fun verifyBatch(@PathVariable batchCode: String): ResponseEntity<Result<PublicBatchVerificationDto>> {
        logger.info("Public verification request for batch: $batchCode")
        
        val batch = eudrBatchRepository.findByBatchCode(batchCode)
            ?: return ResponseEntity.ok(
                ResultFactory.getFailResult("Product not found. Please check the QR code and try again.")
            )

        try {
            // Build supply chain journey from events
            val journeySteps = mutableListOf<JourneyStep>()
            var stepCounter = 1

            // Add creation as first step
            journeySteps.add(
                JourneyStep(
                    step = stepCounter++,
                    event = "Batch Created",
                    location = batch.countryOfProduction,
                    date = batch.createdAt,
                    actor = "Producer"
                )
            )

            // Add farmer collections
            batch.farmerCollections.sortedBy { it.collectionDate }.forEach { collection ->
                journeySteps.add(
                    JourneyStep(
                        step = stepCounter++,
                        event = "Collected from Farm",
                        location = batch.countryOfProduction,
                        date = collection.collectionDate,
                        actor = "Farmer"
                    )
                )
            }

            // Add processing events
            batch.processingEvents.sortedBy { it.processingDate }.forEach { event ->
                journeySteps.add(
                    JourneyStep(
                        step = stepCounter++,
                        event = event.processingType,
                        location = batch.countryOfProduction, // Use batch country as processor location
                        date = event.processingDate,
                        actor = "Processor"
                    )
                )
            }

            // Add supply chain events
            batch.supplyChainEvents.sortedBy { it.eventTimestamp }.forEach { event ->
                journeySteps.add(
                    JourneyStep(
                        step = stepCounter++,
                        event = event.actionType.name.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() },
                        location = event.locationCoordinates ?: batch.countryOfProduction,
                        date = event.eventTimestamp,
                        actor = event.toEntityType.replace("_", " ").lowercase()
                            .replaceFirstChar { it.uppercase() }
                    )
                )
            }

            // Determine compliance status
            val isCompliant = batch.riskLevel != RiskLevel.HIGH && 
                              batch.status != BatchStatus.REJECTED

            // Check if deforestation-free (based on production units having no recent alerts)
            val productionUnitCount = batch.productionUnits.size
            val isDeforestationFree = batch.productionUnits.all { batchPU ->
                // Check if production unit has no deforestation alerts after 2020
                batchPU.productionUnit?.deforestationAlerts?.none { 
                    it.alertDate?.isAfter(java.time.LocalDateTime.of(2020, 12, 31, 0, 0)) == true
                } ?: true
            }

            // Build Hedera explorer URL
            val explorerUrl = batch.hederaTransactionId?.let {
                "https://hashscan.io/testnet/transaction/$it"
            }

            val verificationDto = PublicBatchVerificationDto(
                verified = true,
                batchCode = batch.batchCode,
                product = ProductInfo(
                    commodity = batch.commodityDescription,
                    quantity = batch.quantity,
                    unit = batch.unit,
                    harvestDate = batch.harvestDate,
                    harvestPeriodStart = batch.harvestPeriodStart,
                    harvestPeriodEnd = batch.harvestPeriodEnd
                ),
                origin = OriginInfo(
                    country = batch.countryOfProduction,
                    farmerCount = batch.farmerCollections.distinctBy { it.farmer?.id }.size,
                    productionUnitCount = productionUnitCount,
                    isDeforestationFree = isDeforestationFree
                ),
                compliance = ComplianceInfo(
                    eudrCompliant = isCompliant,
                    riskLevel = batch.riskLevel?.name,
                    status = batch.status.name,
                    lastAssessedAt = batch.createdAt
                ),
                supplyChainJourney = journeySteps,
                blockchain = BlockchainProof(
                    network = "Hedera Hashgraph (Testnet)",
                    transactionId = batch.hederaTransactionId,
                    consensusTimestamp = batch.hederaConsensusTimestamp,
                    explorerUrl = explorerUrl
                ),
                verifiedAt = LocalDateTime.now()
            )

            logger.info("Successfully verified batch: $batchCode")
            return ResponseEntity.ok(ResultFactory.getSuccessResult(verificationDto))

        } catch (e: Exception) {
            logger.error("Error verifying batch $batchCode", e)
            return ResponseEntity.ok(
                ResultFactory.getFailResult("Verification failed. Please try again later.")
            )
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Public health check endpoint")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf(
            "status" to "healthy",
            "service" to "AgriBackup Public Verification API",
            "timestamp" to LocalDateTime.now().toString()
        ))
    }
}
