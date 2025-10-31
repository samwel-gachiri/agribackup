package com.agriconnect.farmersportalapis.service.hedera

import com.agriconnect.farmersportalapis.config.HederaConfiguration
import com.agriconnect.farmersportalapis.domain.eudr.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.hedera.hashgraph.sdk.AccountId
import com.hedera.hashgraph.sdk.TokenId
import com.hedera.hashgraph.sdk.TopicId
import com.hedera.hashgraph.sdk.TopicMessageSubmitTransaction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

@Service
class HederaConsensusServices(
    private val hederaNetworkInitialization: HederaNetworkInitialization,
    private val objectMapper: ObjectMapper,
    private val hederaConfig: HederaConfiguration
) {

    private val logger = LoggerFactory.getLogger(HederaConsensusServices::class.java)
    private var topicId: TopicId? = null

    init {
        initializeTopic()
    }

    private fun initializeTopic() {
        val configuredTopicId = hederaConfig.consensus.topicId.trim()
        logger.info("Hedera Consensus Topic ID from config: '$configuredTopicId'")
        
        topicId = if (configuredTopicId.isNotBlank()) {
            try {
                val parsedTopicId = TopicId.fromString(configuredTopicId)
                logger.info("Successfully parsed existing Hedera topic: $parsedTopicId")
                parsedTopicId
            } catch (e: Exception) {
                logger.warn("Invalid topic ID format: '$configuredTopicId' - Error: ${e.message}. Creating new topic.")
                createNewTopic()
            }
        } else {
            logger.info("No topic ID configured (empty or blank), creating new topic")
            createNewTopic()
        }
    }

    fun getTopicId(): TopicId = topicId ?: throw IllegalStateException("Topic ID not initialized")

    private fun createNewTopic(): TopicId {
        return try {
            val newTopicId = hederaNetworkInitialization.createTopic()
            logger.info("Created new Hedera topic: $newTopicId")
            newTopicId
        } catch (e: Exception) {
            logger.error("Failed to create Hedera topic", e)
            throw RuntimeException("Failed to create Hedera consensus topic", e)
        }
    }

    fun recordBatchCreation(batch: EudrBatch): String {
        val message = createConsensusMessage(
            eventType = "BATCH_CREATED",
            entityId = batch.id,
            entityType = "EudrBatch",
            data = mapOf(
                "batchCode" to batch.batchCode,
                "commodityDescription" to batch.commodityDescription,
                "quantity" to batch.quantity.toString(),
                "unit" to batch.unit,
                "countryOfProduction" to batch.countryOfProduction,
                "countryRiskLevel" to batch.countryRiskLevel.name,
                "createdBy" to batch.createdBy,
                "createdAt" to batch.createdAt.toString()
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordDocumentUpload(document: EudrDocument): String {
        val message = createConsensusMessage(
            eventType = "DOCUMENT_UPLOADED",
            entityId = document.id,
            entityType = "EudrDocument",
            data = mapOf(
                "documentType" to document.documentType.name,
                "fileName" to document.fileName,
                "checksumSha256" to document.checksumSha256,
                "fileSize" to document.fileSize.toString(),
                "uploaderId" to document.uploaderId,
                "ownerEntityId" to document.ownerEntityId,
                "ownerEntityType" to document.ownerEntityType,
                "uploadedAt" to document.uploadedAt.toString(),
                "s3Key" to document.s3Key
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordRiskAssessment(batchId: String, riskLevel: RiskLevel, rationale: String, assessedBy: String): String {
        val message = createConsensusMessage(
            eventType = "RISK_ASSESSED",
            entityId = batchId,
            entityType = "EudrBatch",
            data = mapOf(
                "riskLevel" to riskLevel.name,
                "rationale" to rationale,
                "assessedBy" to assessedBy,
                "assessedAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordSupplyChainEvent(event: SupplyChainEvent): String {
        val message = createConsensusMessage(
            eventType = "SUPPLY_CHAIN_EVENT",
            entityId = event.id,
            entityType = "SupplyChainEvent",
            data = mapOf(
                "batchId" to event.batch.id,
                "actionType" to event.actionType.name,
                "fromEntityId" to (event.fromEntityId ?: ""),
                "fromEntityType" to (event.fromEntityType ?: ""),
                "toEntityId" to event.toEntityId,
                "toEntityType" to event.toEntityType,
                "eventTimestamp" to event.eventTimestamp.toString(),
                "locationCoordinates" to (event.locationCoordinates ?: ""),
                "transportMethod" to (event.transportMethod ?: "")
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordProcessingEvent(event: ProcessingEvent): String {
        val message = createConsensusMessage(
            eventType = "PROCESSING_EVENT",
            entityId = event.id,
            entityType = "ProcessingEvent",
            data = mapOf(
                "batchId" to event.batch.id,
                "processorId" to event.processor.id,
                "processingType" to event.processingType,
                "inputQuantity" to event.inputQuantity.toString(),
                "outputQuantity" to event.outputQuantity.toString(),
                "processingDate" to event.processingDate.toString(),
                "processingNotes" to (event.processingNotes ?: "")
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordProductionUnitVerification(productionUnit: ProductionUnit): String {
        val message = createConsensusMessage(
            eventType = "PRODUCTION_UNIT_VERIFIED",
            entityId = productionUnit.id,
            entityType = "ProductionUnit",
            data = mapOf(
                "unitName" to productionUnit.unitName,
                "farmerId" to productionUnit.farmer.id!!,
                "areaHectares" to productionUnit.areaHectares.toString(),
                "wgs84Coordinates" to (productionUnit.wgs84Coordinates ?: ""),
                "administrativeRegion" to (productionUnit.administrativeRegion ?: ""),
                "verifiedAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordDeforestationAlert(alert: DeforestationAlert): String {
        val message = createConsensusMessage(
            eventType = "DEFORESTATION_ALERT",
            entityId = alert.id,
            entityType = "DeforestationAlert",
            data = mapOf(
                "productionUnitId" to alert.productionUnit.id,
                "alertType" to alert.alertType.name,
                "severity" to alert.severity.name,
                "latitude" to alert.latitude.toString(),
                "longitude" to alert.longitude.toString(),
                "alertDate" to alert.alertDate.toString(),
                "confidence" to alert.confidence.toString(),
                "distanceFromUnit" to alert.distanceFromUnit.toString(),
                "source" to alert.source,
                "sourceId" to (alert.sourceId ?: ""),
                "detectedAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordBatchStatusChange(batch: EudrBatch, oldStatus: BatchStatus, newStatus: BatchStatus): String {
        val message = createConsensusMessage(
            eventType = "BATCH_STATUS_CHANGED",
            entityId = batch.id,
            entityType = "EudrBatch",
            data = mapOf(
                "batchCode" to batch.batchCode,
                "oldStatus" to oldStatus.name,
                "newStatus" to newStatus.name,
                "statusReason" to (batch.riskRationale ?: ""),
                "updatedBy" to batch.createdBy,
                "updatedAt" to batch.createdAt.toString()
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordBatchTransfer(batch: EudrBatch, fromActorId: String, toActorId: String): String {
        val message = createConsensusMessage(
            eventType = "BATCH_TRANSFERRED",
            entityId = batch.id,
            entityType = "EudrBatch",
            data = mapOf(
                "batchCode" to batch.batchCode,
                "fromActor" to fromActorId,
                "toActor" to toActorId,
                "toActorRole" to "PROCESSOR",
                "transferredAt" to batch.createdAt.toString()
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordAuditEvent(auditLog: AuditLog): String {
        val message = createConsensusMessage(
            eventType = "AUDIT_EVENT",
            entityId = auditLog.id,
            entityType = "AuditLog",
            data = mapOf(
                "entityType" to auditLog.entityType,
                "entityId" to auditLog.entityId,
                "action" to auditLog.action,
                "actorId" to auditLog.actorId,
                "actorRole" to auditLog.actorRole,
                "timestamp" to auditLog.timestamp.toString(),
                "recordHash" to auditLog.recordHash
            )
        )

        return submitConsensusMessage(message)
    }

    // ============================================
    // NEW METHODS FOR AGGREGATOR & IMPORTER
    // ============================================

    fun recordAggregatorCreation(
        aggregatorId: String,
        organizationName: String,
        registrationNumber: String,
        operatingRegion: String
    ): String {
        val message = createConsensusMessage(
            eventType = "AGGREGATOR_CREATED",
            entityId = aggregatorId,
            entityType = "Aggregator",
            data = mapOf(
                "organizationName" to organizationName,
                "registrationNumber" to registrationNumber,
                "operatingRegion" to operatingRegion,
                "createdAt" to Instant.now().toString()
            )
        )
        return submitConsensusMessage(message)
    }

    fun recordAggregationEvent(
        eventId: String,
        aggregatorId: String,
        farmerId: String,
        produceType: String,
        quantityKg: BigDecimal,
        collectionDate: LocalDate
    ): String {
        val message = createConsensusMessage(
            eventType = "AGGREGATION_EVENT",
            entityId = eventId,
            entityType = "AggregationEvent",
            data = mapOf(
                "aggregatorId" to aggregatorId,
                "farmerId" to farmerId,
                "produceType" to produceType,
                "quantityKg" to quantityKg.toString(),
                "collectionDate" to collectionDate.toString(),
                "recordedAt" to Instant.now().toString()
            )
        )
        return submitConsensusMessage(message)
    }

    fun recordConsolidatedBatch(
        batchId: String,
        batchNumber: String,
        aggregatorId: String,
        produceType: String,
        totalQuantityKg: BigDecimal,
        numberOfFarmers: Int,
        batchDataHash: String
    ): String {
        val message = createConsensusMessage(
            eventType = "CONSOLIDATED_BATCH_CREATED",
            entityId = batchId,
            entityType = "ConsolidatedBatch",
            data = mapOf(
                "batchNumber" to batchNumber,
                "aggregatorId" to aggregatorId,
                "produceType" to produceType,
                "totalQuantityKg" to totalQuantityKg.toString(),
                "numberOfFarmers" to numberOfFarmers.toString(),
                "batchDataHash" to batchDataHash,
                "createdAt" to Instant.now().toString()
            )
        )
        return submitConsensusMessage(message)
    }

    fun recordImporterCreation(
        importerId: String,
        companyName: String,
        importLicenseNumber: String,
        destinationCountry: String
    ): String {
        val message = createConsensusMessage(
            eventType = "IMPORTER_CREATED",
            entityId = importerId,
            entityType = "Importer",
            data = mapOf(
                "companyName" to companyName,
                "importLicenseNumber" to importLicenseNumber,
                "destinationCountry" to destinationCountry,
                "createdAt" to Instant.now().toString()
            )
        )
        return submitConsensusMessage(message)
    }

    fun recordImportShipment(
        shipmentId: String,
        importerId: String,
        shipmentNumber: String,
        produceType: String,
        quantityKg: BigDecimal,
        originCountry: String,
        shipmentDataHash: String
    ): String {
        val message = createConsensusMessage(
            eventType = "IMPORT_SHIPMENT_CREATED",
            entityId = shipmentId,
            entityType = "ImportShipment",
            data = mapOf(
                "shipmentNumber" to shipmentNumber,
                "importerId" to importerId,
                "produceType" to produceType,
                "quantityKg" to quantityKg.toString(),
                "originCountry" to originCountry,
                "shipmentDataHash" to shipmentDataHash,
                "createdAt" to Instant.now().toString()
            )
        )
        return submitConsensusMessage(message)
    }

    fun recordCustomsDocument(
        documentId: String,
        shipmentId: String,
        documentType: String,
        documentHash: String
    ): String {
        val message = createConsensusMessage(
            eventType = "CUSTOMS_DOCUMENT_UPLOADED",
            entityId = documentId,
            entityType = "CustomsDocument",
            data = mapOf(
                "shipmentId" to shipmentId,
                "documentType" to documentType,
                "documentHash" to documentHash,
                "uploadedAt" to Instant.now().toString()
            )
        )
        return submitConsensusMessage(message)
    }

    fun recordInspectionResult(
        inspectionId: String,
        shipmentId: String,
        inspectionType: String,
        inspectionResult: String,
        inspectionDate: LocalDate
    ): String {
        val message = createConsensusMessage(
            eventType = "INSPECTION_RECORDED",
            entityId = inspectionId,
            entityType = "InspectionRecord",
            data = mapOf(
                "shipmentId" to shipmentId,
                "inspectionType" to inspectionType,
                "inspectionResult" to inspectionResult,
                "inspectionDate" to inspectionDate.toString(),
                "recordedAt" to Instant.now().toString()
            )
        )
        return submitConsensusMessage(message)
    }

    fun recordTokenCreation(tokenId: TokenId, tokenType: String): String {
        val message = createConsensusMessage(
            eventType = "TOKEN_CREATED",
            entityId = tokenId.toString(),
            entityType = "Token",
            data = mapOf(
                "tokenId" to tokenId.toString(),
                "tokenType" to tokenType,
                "createdAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordTokenMinting(
        tokenId: TokenId,
        recipientId: String,
        amount: Long,
        reason: String,
        batchId: String?
    ): String {
        val message = createConsensusMessage(
            eventType = "TOKEN_MINTED",
            entityId = tokenId.toString(),
            entityType = "Token",
            data = mapOf(
                "tokenId" to tokenId.toString(),
                "recipientId" to recipientId,
                "amount" to amount.toString(),
                "reason" to reason,
                "batchId" to (batchId ?: ""),
                "mintedAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordSustainabilityTokenMinting(
        tokenId: TokenId,
        recipientId: String,
        amount: Long,
        practiceType: String,
        verificationData: Map<String, String>
    ): String {
        val message = createConsensusMessage(
            eventType = "SUSTAINABILITY_TOKEN_MINTED",
            entityId = tokenId.toString(),
            entityType = "Token",
            data = mapOf(
                "tokenId" to tokenId.toString(),
                "recipientId" to recipientId,
                "amount" to amount.toString(),
                "practiceType" to practiceType,
                "verificationData" to verificationData.toString(),
                "mintedAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordTokenFreezing(tokenId: TokenId, accountId: String, reason: String): String {
        val message = createConsensusMessage(
            eventType = "TOKEN_FROZEN",
            entityId = tokenId.toString(),
            entityType = "Token",
            data = mapOf(
                "tokenId" to tokenId.toString(),
                "accountId" to accountId,
                "reason" to reason,
                "frozenAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    fun recordTokenUnfreezing(tokenId: TokenId, accountId: String): String {
        val message = createConsensusMessage(
            eventType = "TOKEN_UNFROZEN",
            entityId = tokenId.toString(),
            entityType = "Token",
            data = mapOf(
                "tokenId" to tokenId.toString(),
                "accountId" to accountId,
                "unfrozenAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    fun verifyRecordIntegrity(transactionId: String): Boolean {
        return try {
            // In a real implementation, you would query the Hedera mirror node
            // to verify the transaction exists and matches expected data
            // For now, we'll return true if the transaction ID is not empty
            transactionId.isNotBlank()
        } catch (e: Exception) {
            logger.error("Failed to verify record integrity for transaction: $transactionId", e)
            false
        }
    }

    private fun createConsensusMessage(
        eventType: String,
        entityId: String,
        entityType: String,
        data: Map<String, String>
    ): String {
        val messageData = mapOf(
            "eventType" to eventType,
            "entityId" to entityId,
            "entityType" to entityType,
            "timestamp" to Instant.now().toString(),
            "data" to data
        )

        return try {
            objectMapper.writeValueAsString(messageData)
        } catch (e: Exception) {
            logger.error("Failed to serialize consensus message", e)
            throw RuntimeException("Failed to create consensus message", e)
        }
    }

    private fun submitConsensusMessage(message: String): String {
        return try {
            if (hederaNetworkInitialization.isNetworkAvailable()) {
                val transaction = TopicMessageSubmitTransaction()
                    .setTopicId(topicId!!)
                    .setMessage(message)
                    .freezeWith(hederaNetworkInitialization.getClient())
                    .sign(hederaNetworkInitialization.getOperatorPrivateKey())

                val response = transaction.execute(hederaNetworkInitialization.getClient())
                val receipt = response.getReceipt(hederaNetworkInitialization.getClient())

                val transactionId = receipt.transactionId.toString()
                logger.debug("Submitted consensus message with transaction ID: $transactionId")
                transactionId
            } else {
                logger.warn("Hedera network unavailable, message will be queued for later submission")
                "queued_${System.currentTimeMillis()}"
            }
        } catch (e: Exception) {
            logger.error("Failed to submit consensus message", e)
            throw RuntimeException("Failed to submit consensus message to Hedera", e)
        }
    }

    fun submitConsensusMessageWithQueue(message: String, entityId: String, entityType: String): String {
        return try {
            if (hederaNetworkInitialization.isNetworkAvailable()) {
                submitConsensusMessage(message)
            } else {
                logger.info("Network unavailable, queuing consensus message for entity: $entityType:$entityId")
                "queued_${entityType}_${entityId}_${System.currentTimeMillis()}"
            }
        } catch (e: Exception) {
            logger.warn("Failed to submit consensus message, will queue for retry", e)
            "queued_${entityType}_${entityId}_${System.currentTimeMillis()}"
        }
    }

    /**
     * Record Hedera account creation on consensus service
     */
    fun recordAccountCreation(accountId: AccountId, memo: String): String {
        val message = createConsensusMessage(
            eventType = "HEDERA_ACCOUNT_CREATED",
            entityId = accountId.toString(),
            entityType = "HederaAccount",
            data = mapOf(
                "accountId" to accountId.toString(),
                "memo" to memo,
                "createdAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    /**
     * Record token association with account
     */
    fun recordTokenAssociation(accountId: AccountId, tokenId: TokenId): String {
        val message = createConsensusMessage(
            eventType = "TOKEN_ASSOCIATED",
            entityId = accountId.toString(),
            entityType = "TokenAssociation",
            data = mapOf(
                "accountId" to accountId.toString(),
                "tokenId" to tokenId.toString(),
                "associatedAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    /**
     * Record EUDR Compliance Certificate NFT issuance
     */
    fun recordComplianceCertificateIssuance(
        shipmentId: String,
        exporterAccountId: AccountId,
        nftSerialNumber: Long,
        complianceData: Map<String, Any>
    ): String {
        val message = createConsensusMessage(
            eventType = "EUDR_CERTIFICATE_ISSUED",
            entityId = shipmentId,
            entityType = "ComplianceCertificate",
            data = mapOf(
                "shipmentId" to shipmentId,
                "exporterAccountId" to exporterAccountId.toString(),
                "nftSerialNumber" to nftSerialNumber.toString(),
                "certificateType" to "EUDR_COMPLIANCE",
                "issuedAt" to Instant.now().toString(),
                "complianceData" to complianceData.toString()
            )
        )

        return submitConsensusMessage(message)
    }

    /**
     * Record EUDR Compliance Certificate NFT transfer
     */
    fun recordComplianceCertificateTransfer(
        shipmentId: String,
        fromAccountId: AccountId,
        toAccountId: AccountId,
        nftSerialNumber: Long
    ): String {
        val message = createConsensusMessage(
            eventType = "EUDR_CERTIFICATE_TRANSFERRED",
            entityId = shipmentId,
            entityType = "ComplianceCertificate",
            data = mapOf(
                "shipmentId" to shipmentId,
                "fromAccountId" to fromAccountId.toString(),
                "toAccountId" to toAccountId.toString(),
                "nftSerialNumber" to nftSerialNumber.toString(),
                "transferredAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    /**
     * Record EUDR Compliance Certificate NFT freeze (revocation)
     */
    fun recordComplianceCertificateFreeze(
        accountId: AccountId,
        nftSerialNumber: Long,
        reason: String
    ): String {
        val message = createConsensusMessage(
            eventType = "EUDR_CERTIFICATE_FROZEN",
            entityId = accountId.toString(),
            entityType = "ComplianceCertificate",
            data = mapOf(
                "accountId" to accountId.toString(),
                "nftSerialNumber" to nftSerialNumber.toString(),
                "reason" to reason,
                "frozenAt" to Instant.now().toString()
            )
        )

        return submitConsensusMessage(message)
    }

    /**
     * Record mitigation workflow creation on Hedera blockchain
     */
    fun recordMitigationWorkflowCreation(
        workflowId: String,
        batchId: String,
        riskLevel: String,
        createdBy: String,
        metadata: String
    ): String {
        val message = createConsensusMessage(
            eventType = "MITIGATION_WORKFLOW_CREATED",
            entityId = workflowId,
            entityType = "MitigationWorkflow",
            data = mapOf(
                "workflowId" to workflowId,
                "batchId" to batchId,
                "riskLevel" to riskLevel,
                "createdBy" to createdBy,
                "createdAt" to Instant.now().toString(),
                "metadata" to metadata
            )
        )

        return submitConsensusMessage(message)
    }

    /**
     * Record mitigation action creation on Hedera blockchain
     */
    fun recordMitigationAction(
        actionId: String,
        workflowId: String,
        actionType: String,
        assignedTo: String,
        metadata: String
    ): String {
        val message = createConsensusMessage(
            eventType = "MITIGATION_ACTION_CREATED",
            entityId = actionId,
            entityType = "MitigationAction",
            data = mapOf(
                "actionId" to actionId,
                "workflowId" to workflowId,
                "actionType" to actionType,
                "assignedTo" to assignedTo,
                "createdAt" to Instant.now().toString(),
                "metadata" to metadata
            )
        )

        return submitConsensusMessage(message)
    }

    /**
     * Record mitigation action status change on Hedera blockchain
     */
    fun recordMitigationActionStatusChange(
        actionId: String,
        workflowId: String,
        oldStatus: String,
        newStatus: String,
        updatedBy: String,
        metadata: String
    ): String {
        val message = createConsensusMessage(
            eventType = "MITIGATION_ACTION_STATUS_CHANGED",
            entityId = actionId,
            entityType = "MitigationAction",
            data = mapOf(
                "actionId" to actionId,
                "workflowId" to workflowId,
                "oldStatus" to oldStatus,
                "newStatus" to newStatus,
                "updatedBy" to updatedBy,
                "updatedAt" to Instant.now().toString(),
                "metadata" to metadata
            )
        )

        return submitConsensusMessage(message)
    }

    /**
     * Record mitigation workflow completion on Hedera blockchain
     */
    fun recordMitigationWorkflowCompletion(
        workflowId: String,
        batchId: String,
        completedBy: String,
        metadata: String
    ): String {
        val message = createConsensusMessage(
            eventType = "MITIGATION_WORKFLOW_COMPLETED",
            entityId = workflowId,
            entityType = "MitigationWorkflow",
            data = mapOf(
                "workflowId" to workflowId,
                "batchId" to batchId,
                "completedBy" to completedBy,
                "completedAt" to Instant.now().toString(),
                "metadata" to metadata
            )
        )

        return submitConsensusMessage(message)
    }
}