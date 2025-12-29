package com.agriconnect.farmersportalapis.service.hedera

import com.hedera.hashgraph.sdk.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Hedera Token Service for AgriBackup
 * 
 * EUDR Compliance Certificate NFT Strategy:
 * 
 * - Each compliant shipment receives ONE unique NFT certificate
 * - The NFT proves the shipment meets all EUDR requirements
 * - Acts as a digital passport that travels with the shipment
 * - Transferable from exporter → importer → customs
 * - Verifiable on blockchain for instant customs clearance
 * - Can be frozen/revoked if fraud is detected
 * 
 * This is NOT an incentive - it's mandatory proof of compliance
 */
@Service
class HederaTokenService(
    private val hederaNetworkInitialization: HederaNetworkInitialization,
    private val hederaConsensusService: HederaConsensusServices
) {

    private val logger = LoggerFactory.getLogger(HederaTokenService::class.java)

    // Store the EUDR Compliance Certificate NFT token ID
    private var eudrComplianceCertificateNftId: TokenId? = null

    /**
     * Create EUDR Compliance Certificate NFT Collection
     * This is a one-time setup to create the NFT collection
     * Each certificate will be minted as a unique NFT within this collection
     */
    fun createEudrComplianceCertificateNft(): TokenId {
        return try {
            val tokenId = hederaNetworkInitialization.createNFT(
                "EUDR Compliance Certificate",
                "EUDR-CERT"
            )

            eudrComplianceCertificateNftId = tokenId

            // Record NFT collection creation on consensus service
//            hederaConsensusService.recordTokenCreation(tokenId, "EUDR_COMPLIANCE_CERTIFICATE_NFT")

            logger.info("Created EUDR Compliance Certificate NFT collection: $tokenId")
            tokenId
        } catch (e: Exception) {
            logger.error("Failed to create EUDR compliance certificate NFT collection", e)
            throw RuntimeException("Failed to create EUDR compliance certificate NFT", e)
        }
    }

    /**
     * Issue EUDR Compliance Certificate NFT for a supply chain workflow
     * 
     * This NFT is proof of compliance for the ENTIRE workflow, NOT a reward
     * Issued ONLY when workflow passes ALL EUDR checks:
     * - GPS coordinates captured from all production units
     * - Deforestation-free verification completed for all farms
     * - Complete supply chain traceability (farmer → aggregator → processor → exporter)
     * - Risk assessment passed (origin country, data completeness)
     * - All collection events properly recorded
     * 
     * @param workflowId Unique workflow identifier
     * @param exporterAccountId Hedera account of the exporter
     * @param complianceData Map containing:
     *        - workflowName
     *        - produceType
     *        - totalQuantityKg
     *        - totalFarmers
     *        - totalProductionUnits
     *        - gpsCoordinatesCount
     *        - deforestationStatus (VERIFIED_FREE)
     *        - originCountry
     *        - riskLevel (LOW/MEDIUM/HIGH)
     *        - traceabilityHash
     * @return Triple of (Hedera transaction ID, NFT serial number, NFT token ID)
     */
    fun issueWorkflowComplianceCertificateNft(
        workflowId: String,
        exporterAccountId: AccountId,
        complianceData: Map<String, String>
    ): Triple<String, Long, String> {
        val tokenId = eudrComplianceCertificateNftId ?: createEudrComplianceCertificateNft()
        
        val maxRetries = 3
        var lastException: Exception? = null

        for (attempt in 1..maxRetries) {
            try {
                logger.info("Attempting to mint NFT for workflow $workflowId (attempt $attempt/$maxRetries)")
                
                // Hedera NFT metadata limit: 100 bytes
                // Store only essential identifier - full data is in consensus service
                val metadata = "WORKFLOW:$workflowId".toByteArray()
                
                // Mint ONE unique NFT for this workflow
                val transaction = TokenMintTransaction()
                    .setTokenId(tokenId)
                    .addMetadata(metadata) // NFTs require metadata (max 100 bytes)
                    .setMaxTransactionFee(Hbar.from(10))
                    .freezeWith(hederaNetworkInitialization.getClient())
                    .sign(hederaNetworkInitialization.getOperatorPrivateKey())

                val response = transaction.execute(hederaNetworkInitialization.getClient())
                val receipt = response.getReceipt(hederaNetworkInitialization.getClient())
                val serialNumber = receipt.serials[0].toLong()

                // Transfer NFT certificate to exporter's account (with retry)
                transferCertificateNftWithRetry(tokenId, exporterAccountId, serialNumber)

                val transactionId = receipt.transactionId.toString()
                val nftTokenId = tokenId.toString()
                logger.info("Issued EUDR Compliance Certificate NFT for workflow $workflowId to exporter $exporterAccountId")
                logger.info("NFT Token ID: $nftTokenId, Serial Number: $serialNumber")
                logger.info("Certificate details: $complianceData")
                
                return Triple(transactionId, serialNumber, nftTokenId)
            } catch (e: java.util.concurrent.TimeoutException) {
                lastException = e
                logger.warn("Timeout on attempt $attempt for minting NFT for workflow $workflowId. Retrying...")
                if (attempt < maxRetries) {
                    Thread.sleep(2000L * attempt) // Exponential backoff: 2s, 4s, 6s
                }
            } catch (e: Exception) {
                lastException = e
                // For non-timeout errors, check if it's retriable
                if (e.cause is java.util.concurrent.TimeoutException) {
                    logger.warn("Timeout (nested) on attempt $attempt for workflow $workflowId. Retrying...")
                    if (attempt < maxRetries) {
                        Thread.sleep(2000L * attempt)
                    }
                } else {
                    // Non-retriable error
                    logger.error("Failed to issue compliance certificate NFT for workflow $workflowId", e)
                    throw RuntimeException("Failed to issue EUDR compliance certificate NFT for workflow", e)
                }
            }
        }
        
        logger.error("All $maxRetries attempts failed for minting NFT for workflow $workflowId", lastException)
        throw RuntimeException("Failed to issue EUDR compliance certificate NFT after $maxRetries attempts", lastException)
    }
    
    /**
     * Transfer NFT to exporter with retry logic
     */
    private fun transferCertificateNftWithRetry(tokenId: TokenId, exporterAccountId: AccountId, serialNumber: Long) {
        val maxRetries = 3
        var lastException: Exception? = null
        
        for (attempt in 1..maxRetries) {
            try {
                transferCertificateNft(tokenId, exporterAccountId, serialNumber)
                return
            } catch (e: java.util.concurrent.TimeoutException) {
                lastException = e
                logger.warn("Timeout transferring NFT on attempt $attempt. Retrying...")
                if (attempt < maxRetries) {
                    Thread.sleep(2000L * attempt)
                }
            } catch (e: Exception) {
                if (e.cause is java.util.concurrent.TimeoutException) {
                    lastException = e
                    logger.warn("Timeout (nested) transferring NFT on attempt $attempt. Retrying...")
                    if (attempt < maxRetries) {
                        Thread.sleep(2000L * attempt)
                    }
                } else {
                    throw e
                }
            }
        }
        
        logger.error("All $maxRetries attempts failed for transferring NFT", lastException)
        throw RuntimeException("Failed to transfer NFT after $maxRetries attempts", lastException)
    }

    /**
     * Issue EUDR Compliance Certificate NFT for a shipment
     * 
     * This NFT is proof of compliance, NOT a reward
     * Issued ONLY when shipment passes ALL EUDR checks:
     * - GPS coordinates captured from all production units
     * - Deforestation-free verification completed
     * - Complete supply chain traceability (farmer → aggregator → processor → exporter)
     * - Risk assessment passed (origin country, data completeness)
     * - Due diligence statement generated
     * 
     * @param shipmentId Unique shipment identifier
     * @param exporterAccountId Hedera account of the exporter
     * @param complianceData Map containing:
     *        - originCountry
     *        - riskLevel (LOW/MEDIUM/HIGH)
     *        - totalFarmers
     *        - totalProductionUnits
     *        - gpsCoordinatesCount
     *        - deforestationStatus (VERIFIED_FREE)
     *        - traceabilityHash
     * @return Hedera transaction ID of the NFT issuance
     */
    fun issueComplianceCertificateNft(
        shipmentId: String,
        exporterAccountId: AccountId,
        complianceData: Map<String, String>
    ): String {
        val tokenId = eudrComplianceCertificateNftId ?: createEudrComplianceCertificateNft()

        return try {
            // Hedera NFT metadata limit: 100 bytes
            // Store only essential identifier - full data is in consensus service
            val metadata = "SHIPMENT:$shipmentId".toByteArray()
            
            // Mint ONE unique NFT for this shipment
            val transaction = TokenMintTransaction()
                .setTokenId(tokenId)
                .addMetadata(metadata) // NFTs require metadata (max 100 bytes)
                .setMaxTransactionFee(Hbar.from(10))
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(hederaNetworkInitialization.getOperatorPrivateKey())

            val response = transaction.execute(hederaNetworkInitialization.getClient())
            val receipt = response.getReceipt(hederaNetworkInitialization.getClient())
            val serialNumber = receipt.serials[0].toLong()

            // Transfer NFT certificate to exporter's account
            transferCertificateNft(tokenId, exporterAccountId, serialNumber)

            // Record certificate issuance on consensus service with full compliance data
            hederaConsensusService.recordComplianceCertificateIssuance(
                shipmentId = shipmentId,
                exporterAccountId = AccountId.fromString(exporterAccountId.toString()),
                nftSerialNumber = serialNumber,
                complianceData = complianceData
            )

            val transactionId = receipt.transactionId.toString()
            logger.info("Issued EUDR Compliance Certificate NFT for shipment $shipmentId to exporter $exporterAccountId")
            logger.info("Certificate details: $complianceData")
            transactionId
        } catch (e: Exception) {
            logger.error("Failed to issue compliance certificate NFT for shipment $shipmentId", e)
            throw RuntimeException("Failed to issue EUDR compliance certificate NFT", e)
        }
    }

    /**
     * Transfer EUDR Compliance Certificate NFT to another account
     * Use case: Exporter transfers NFT to importer with the physical shipment
     * 
     * @param fromAccountId Current holder of the NFT
     * @param toAccountId Recipient of the NFT (importer)
     * @param workflowId Workflow identifier for logging
     * @param serialNumber The NFT serial number to transfer
     * @return true if transfer succeeded
     */
    fun transferWorkflowComplianceCertificateNft(
        fromAccountId: AccountId,
        toAccountId: AccountId,
        workflowId: String,
        serialNumber: Long
    ): Boolean {
        val tokenId = eudrComplianceCertificateNftId ?: run {
            logger.error("No EUDR Compliance Certificate NFT token exists")
            return false
        }

        return try {
            // First, ensure the recipient has associated with the token
            ensureTokenAssociation(toAccountId, tokenId)
            
            // Create NftId for the specific NFT
            val nftId = NftId(tokenId, serialNumber)
            
            // Use addNftTransfer for NFTs (not addTokenTransfer which is for fungible tokens)
            val transaction = TransferTransaction()
                .addNftTransfer(nftId, fromAccountId, toAccountId)
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(hederaNetworkInitialization.getOperatorPrivateKey())

            val response = transaction.execute(hederaNetworkInitialization.getClient())
            val receipt = response.getReceipt(hederaNetworkInitialization.getClient())
            
            if (receipt.status != Status.SUCCESS) {
                logger.error("NFT transfer failed with status: ${receipt.status}")
                return false
            }

            // Record transfer on consensus service
            hederaConsensusService.recordWorkflowCertificateTransfer(workflowId, fromAccountId, toAccountId)

            logger.info("Transferred EUDR Compliance Certificate NFT for workflow $workflowId")
            logger.info("From: $fromAccountId → To: $toAccountId, Serial: $serialNumber")
            true
        } catch (e: Exception) {
            logger.error("Failed to transfer compliance certificate NFT for workflow $workflowId", e)
            false
        }
    }
    
    /**
     * Ensure an account has associated with the EUDR certificate token
     * Required before receiving NFTs on Hedera
     */
    private fun ensureTokenAssociation(accountId: AccountId, tokenId: TokenId) {
        try {
            // Try to associate - if already associated, this will fail gracefully
            val transaction = TokenAssociateTransaction()
                .setAccountId(accountId)
                .setTokenIds(listOf(tokenId))
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(hederaNetworkInitialization.getOperatorPrivateKey())
            
            transaction.execute(hederaNetworkInitialization.getClient())
            logger.info("Account $accountId associated with token $tokenId")
        } catch (e: Exception) {
            // TOKEN_ALREADY_ASSOCIATED_TO_ACCOUNT is expected if already associated
            if (e.message?.contains("TOKEN_ALREADY_ASSOCIATED") == true) {
                logger.debug("Account $accountId already associated with token $tokenId")
            } else {
                logger.warn("Could not associate token (may already be associated): ${e.message}")
            }
        }
    }

    /**
     * Transfer EUDR Compliance Certificate NFT to another account
     * Use case: Exporter transfers NFT to importer with the physical shipment
     * 
     * @param fromAccountId Current holder of the NFT
     * @param toAccountId Recipient of the NFT (importer)
     * @param shipmentId Shipment identifier for logging
     * @param serialNumber The NFT serial number to transfer
     * @return true if transfer succeeded
     */
    fun transferComplianceCertificateNft(
        fromAccountId: AccountId,
        toAccountId: AccountId,
        shipmentId: String,
        serialNumber: Long
    ): Boolean {
        val tokenId = eudrComplianceCertificateNftId ?: return false

        return try {
            // Ensure recipient has associated with the token
            ensureTokenAssociation(toAccountId, tokenId)
            
            // Create NftId for the specific NFT
            val nftId = NftId(tokenId, serialNumber)
            
            // Use addNftTransfer for NFTs
            val transaction = TransferTransaction()
                .addNftTransfer(nftId, fromAccountId, toAccountId)
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(hederaNetworkInitialization.getOperatorPrivateKey())

            transaction.execute(hederaNetworkInitialization.getClient())

            logger.info("Transferred EUDR Compliance Certificate NFT for shipment $shipmentId")
            logger.info("From: $fromAccountId → To: $toAccountId, Serial: $serialNumber")
            true
        } catch (e: Exception) {
            logger.error("Failed to transfer compliance certificate NFT for shipment $shipmentId", e)
            false
        }
    }

    /**
     * Freeze EUDR Compliance Certificate NFT
     * Use case: Fraud detected, revoke compliance status
     * 
     * @param accountId Account holding the fraudulent certificate
     * @param reason Reason for freezing (e.g., "FRAUD_DETECTED", "FALSE_GPS_DATA")
     * @return true if freeze succeeded
     */
    fun freezeComplianceCertificateNft(accountId: AccountId, reason: String): Boolean {
        val tokenId = eudrComplianceCertificateNftId ?: return false

        return try {
            val transaction = TokenFreezeTransaction()
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(hederaNetworkInitialization.getOperatorPrivateKey())

            transaction.execute(hederaNetworkInitialization.getClient())

            // Record freezing on consensus service
            hederaConsensusService.recordTokenFreezing(tokenId, accountId.toString(), reason)

            logger.warn("FROZE EUDR Compliance Certificate NFT for account $accountId")
            logger.warn("Reason: $reason")
            true
        } catch (e: Exception) {
            logger.error("Failed to freeze compliance certificate NFT for account $accountId", e)
            false
        }
    }

    /**
     * Unfreeze EUDR Compliance Certificate NFT
     * Use case: Issue resolved, restore compliance status
     * 
     * @param accountId Account to unfreeze
     * @return true if unfreeze succeeded
     */
    fun unfreezeComplianceCertificateNft(accountId: AccountId): Boolean {
        val tokenId = eudrComplianceCertificateNftId ?: return false

        return try {
            val transaction = TokenUnfreezeTransaction()
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(hederaNetworkInitialization.getOperatorPrivateKey())

            transaction.execute(hederaNetworkInitialization.getClient())

            // Record unfreezing on consensus service
            hederaConsensusService.recordTokenUnfreezing(tokenId, accountId.toString())

            logger.info("Unfroze EUDR Compliance Certificate NFT for account $accountId")
            true
        } catch (e: Exception) {
            logger.error("Failed to unfreeze compliance certificate NFT for account $accountId", e)
            false
        }
    }

    /**
     * Get EUDR Compliance Certificate NFT balance for an account
     * Should return 1 if account has the certificate, 0 if not
     * 
     * @param accountId Account to check
     * @return Number of certificates (0 or 1 per shipment)
     */
    fun getCertificateNftBalance(accountId: AccountId): Long {
        val tokenId = eudrComplianceCertificateNftId ?: return 0L
        return getTokenBalance(tokenId, accountId)
    }

    /**
     * Check if account has valid EUDR Compliance Certificate NFT
     * 
     * @param accountId Account to check
     * @return true if account has at least one certificate NFT
     */
    fun hasValidComplianceCertificate(accountId: AccountId): Boolean {
        return getCertificateNftBalance(accountId) > 0
    }

    /**
     * Generic token balance query
     */
    private fun getTokenBalance(tokenId: TokenId, accountId: AccountId): Long {
        return try {
            val query = AccountBalanceQuery()
                .setAccountId(accountId)

            val balance = query.execute(hederaNetworkInitialization.getClient())
            balance.tokens[tokenId] ?: 0L
        } catch (e: Exception) {
            logger.error("Failed to get token balance for account $accountId", e)
            0L
        }
    }

    /**
     * Transfer NFT from treasury to recipient (internal use during minting)
     */
    private fun transferCertificateNft(tokenId: TokenId, toAccountId: AccountId, serialNumber: Long): Boolean {
        return try {
            val operatorAccountId = hederaNetworkInitialization.getAccountId()
            
            // Ensure recipient has associated with the token
            ensureTokenAssociation(toAccountId, tokenId)
            
            // Create NftId for the specific NFT
            val nftId = NftId(tokenId, serialNumber)

            // Use addNftTransfer for NFTs (not addTokenTransfer which is for fungible tokens)
            val transaction = TransferTransaction()
                .addNftTransfer(nftId, operatorAccountId, toAccountId)
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(hederaNetworkInitialization.getOperatorPrivateKey())

            transaction.execute(hederaNetworkInitialization.getClient())

            logger.debug("Transferred certificate NFT serial $serialNumber from $operatorAccountId to $toAccountId")
            true
        } catch (e: Exception) {
            logger.error("Failed to transfer certificate NFT to $toAccountId", e)
            false
        }
    }

    /**
     * Get the EUDR Compliance Certificate NFT token ID
     * Used for account associations
     * 
     * @return TokenId of the EUDR certificate NFT collection, or null if not created yet
     */
    fun getEudrComplianceCertificateNftId(): TokenId? = eudrComplianceCertificateNftId

    // Backward compatibility alias (deprecated)
    @Deprecated("Use getEudrComplianceCertificateNftId()", ReplaceWith("getEudrComplianceCertificateNftId()"))
    fun getComplianceTokenId(): TokenId? = eudrComplianceCertificateNftId
}