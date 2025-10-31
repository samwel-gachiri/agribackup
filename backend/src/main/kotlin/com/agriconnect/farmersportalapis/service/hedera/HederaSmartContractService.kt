package com.agriconnect.farmersportalapis.service.hedera

import com.hedera.hashgraph.sdk.ContractFunctionParameters
import com.hedera.hashgraph.sdk.ContractFunctionResult
import com.hedera.hashgraph.sdk.ContractId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class HederaSmartContractService(
    private val hederaNetworkInitialization: HederaNetworkInitialization
) {

    private val logger = LoggerFactory.getLogger(HederaSmartContractService::class.java)

    // Store deployed contract IDs for different batch compliance contracts
    private val batchComplianceContracts = mutableMapOf<String, ContractId>()

    fun deployBatchComplianceContract(batchId: String): ContractId {
        return try {
            // In a real implementation, you would have compiled Solidity bytecode
            // For now, we'll use a placeholder bytecode
            val bytecode = generatePlaceholderBytecode()

            val contractId = hederaNetworkInitialization.deploySmartContract(bytecode)
            batchComplianceContracts[batchId] = contractId

            logger.info("Deployed batch compliance contract for batch $batchId: $contractId")
            contractId
        } catch (e: Exception) {
            logger.error("Failed to deploy batch compliance contract for batch $batchId", e)
            throw RuntimeException("Failed to deploy smart contract", e)
        }
    }

    fun validateBatchCompliance(batchId: String, complianceData: Map<String, Any>): ComplianceResult {
        val contractId = batchComplianceContracts[batchId]
            ?: throw IllegalArgumentException("No compliance contract found for batch $batchId")

        return try {
            val params = ContractFunctionParameters()
                .addString(batchId)
                .addString(serializeComplianceData(complianceData))

            val result = hederaNetworkInitialization.executeContractFunction(
                contractId,
                "validateCompliance",
                params
            )

            parseComplianceResult(result)
        } catch (e: Exception) {
            logger.error("Failed to validate batch compliance for batch $batchId", e)
            ComplianceResult(
                isCompliant = false,
                violations = listOf("Smart contract validation failed: ${e.message}"),
                canProceed = false
            )
        }
    }

    fun holdBatch(batchId: String, reason: String): Boolean {
        val contractId = batchComplianceContracts[batchId]
            ?: throw IllegalArgumentException("No compliance contract found for batch $batchId")

        return try {
            val params = ContractFunctionParameters()
                .addString(batchId)
                .addString(reason)

            hederaNetworkInitialization.executeContractFunction(contractId, "holdBatch", params)

            logger.info("Batch $batchId held by smart contract. Reason: $reason")
            true
        } catch (e: Exception) {
            logger.error("Failed to hold batch $batchId via smart contract", e)
            false
        }
    }

    fun releaseBatch(batchId: String): Boolean {
        val contractId = batchComplianceContracts[batchId]
            ?: throw IllegalArgumentException("No compliance contract found for batch $batchId")

        return try {
            val params = ContractFunctionParameters()
                .addString(batchId)

            hederaNetworkInitialization.executeContractFunction(contractId, "releaseBatch", params)

            logger.info("Batch $batchId released by smart contract")
            true
        } catch (e: Exception) {
            logger.error("Failed to release batch $batchId via smart contract", e)
            false
        }
    }

    fun updateBatchStatus(batchId: String, status: String, metadata: Map<String, String>): Boolean {
        val contractId = batchComplianceContracts[batchId]
            ?: throw IllegalArgumentException("No compliance contract found for batch $batchId")

        return try {
            val params = ContractFunctionParameters()
                .addString(batchId)
                .addString(status)
                .addString(serializeMetadata(metadata))

            hederaNetworkInitialization.executeContractFunction(contractId, "updateStatus", params)

            logger.debug("Updated batch $batchId status to $status via smart contract")
            true
        } catch (e: Exception) {
            logger.error("Failed to update batch $batchId status via smart contract", e)
            false
        }
    }

    fun getBatchStatus(batchId: String): BatchContractStatus? {
        val contractId = batchComplianceContracts[batchId] ?: return null

        return try {
            val params = ContractFunctionParameters()
                .addString(batchId)

            val result = hederaNetworkInitialization.executeContractFunction(contractId, "getBatchStatus", params)

            parseBatchStatus(result)
        } catch (e: Exception) {
            logger.error("Failed to get batch status for $batchId from smart contract", e)
            null
        }
    }

    fun checkComplianceDeadlines(): List<String> {
        val expiredBatches = mutableListOf<String>()

        batchComplianceContracts.forEach { (batchId, contractId) ->
            try {
                val result = hederaNetworkInitialization.executeContractFunction(
                    contractId,
                    "checkDeadline",
                    null
                )

                if (result.getBool(0)) {
                    expiredBatches.add(batchId)
                }
            } catch (e: Exception) {
                logger.warn("Failed to check deadline for batch $batchId", e)
            }
        }

        return expiredBatches
    }

    private fun generatePlaceholderBytecode(): ByteArray {
        // This is a placeholder. In a real implementation, you would have
        // compiled Solidity smart contract bytecode for EUDR compliance
        return "608060405234801561001057600080fd5b50".hexToByteArray()
    }

    private fun String.hexToByteArray(): ByteArray {
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    private fun serializeComplianceData(data: Map<String, Any>): String {
        return data.entries.joinToString(";") { "${it.key}:${it.value}" }
    }

    private fun serializeMetadata(metadata: Map<String, String>): String {
        return metadata.entries.joinToString(";") { "${it.key}:${it.value}" }
    }

    private fun parseComplianceResult(result: ContractFunctionResult): ComplianceResult {
        return try {
            val isCompliant = result.getBool(0)
            val violationsString = result.getString(1)
            val canProceed = result.getBool(2)

            val violations = if (violationsString.isNotBlank()) {
                violationsString.split(";")
            } else {
                emptyList()
            }

            ComplianceResult(isCompliant, violations, canProceed)
        } catch (e: Exception) {
            logger.error("Failed to parse compliance result", e)
            ComplianceResult(false, listOf("Failed to parse contract result"), false)
        }
    }

    private fun parseBatchStatus(result: ContractFunctionResult): BatchContractStatus {
        return try {
            BatchContractStatus(
                batchId = result.getString(0),
                status = result.getString(1),
                isHeld = result.getBool(2),
                lastUpdated = result.getUint256(3).toLong(),
                metadata = result.getString(4)
            )
        } catch (e: Exception) {
            logger.error("Failed to parse batch status", e)
            throw RuntimeException("Failed to parse batch status from contract", e)
        }
    }

    data class ComplianceResult(
        val isCompliant: Boolean,
        val violations: List<String>,
        val canProceed: Boolean
    )

    data class BatchContractStatus(
        val batchId: String,
        val status: String,
        val isHeld: Boolean,
        val lastUpdated: Long,
        val metadata: String
    )
}