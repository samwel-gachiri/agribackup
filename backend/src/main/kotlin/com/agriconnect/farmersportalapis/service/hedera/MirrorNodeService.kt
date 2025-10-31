package com.agriconnect.farmersportalapis.service.hedera

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.time.Instant

/**
 * Mirror Node Service - Query Hedera's public Mirror Node REST API
 * 
 * This demonstrates the proper pattern: We DON'T duplicate blockchain data
 * in our database. Instead, we query Mirror Node when we need full details.
 * 
 * Mirror Node API Documentation: https://docs.hedera.com/hedera/sdks-and-apis/rest-api
 */
@Service
class MirrorNodeService(
    private val restTemplate: RestTemplate = RestTemplate(),
    private val objectMapper: ObjectMapper,
    @Value("\${hedera.mirror-node.url:https://testnet.mirrornode.hedera.com}") 
    private val mirrorNodeUrl: String
) {
    
    /**
     * Get account details from Mirror Node
     * GET /api/v1/accounts/{accountId}
     */
    fun getAccount(accountId: String): JsonNode? {
        return try {
            val url = "$mirrorNodeUrl/api/v1/accounts/$accountId"
            val response = restTemplate.getForObject<String>(url)
            objectMapper.readTree(response)
        } catch (e: Exception) {
            println("Failed to get account $accountId: ${e.message}")
            null
        }
    }
    
    /**
     * Get account transactions from Mirror Node
     * GET /api/v1/accounts/{accountId}/transactions
     */
    fun getAccountTransactions(
        accountId: String,
        limit: Int = 25,
        timestampGt: Instant? = null
    ): JsonNode? {
        return try {
            var url = "$mirrorNodeUrl/api/v1/accounts/$accountId/transactions?limit=$limit"
            timestampGt?.let {
                url += "&timestamp=gt:${it.epochSecond}.${it.nano}"
            }
            val response = restTemplate.getForObject<String>(url)
            objectMapper.readTree(response)
        } catch (e: Exception) {
            println("Failed to get transactions for $accountId: ${e.message}")
            null
        }
    }
    
    /**
     * Get specific transaction details
     * GET /api/v1/transactions/{transactionId}
     */
    fun getTransaction(transactionId: String): JsonNode? {
        return try {
            val url = "$mirrorNodeUrl/api/v1/transactions/$transactionId"
            val response = restTemplate.getForObject<String>(url)
            objectMapper.readTree(response)
        } catch (e: Exception) {
            println("Failed to get transaction $transactionId: ${e.message}")
            null
        }
    }
    
    /**
     * Get topic information
     * GET /api/v1/topics/{topicId}
     */
    fun getTopic(topicId: String): JsonNode? {
        return try {
            val url = "$mirrorNodeUrl/api/v1/topics/$topicId"
            val response = restTemplate.getForObject<String>(url)
            objectMapper.readTree(response)
        } catch (e: Exception) {
            println("Failed to get topic $topicId: ${e.message}")
            null
        }
    }
    
    /**
     * Get topic messages - This is the KEY method!
     * GET /api/v1/topics/{topicId}/messages
     * 
     * This is how we retrieve the ACTUAL message content that we submitted.
     * We only store the reference (transaction ID) in our DB.
     */
    fun getTopicMessages(
        topicId: String,
        limit: Int = 25,
        sequenceNumberGt: Long? = null,
        timestampGt: Instant? = null
    ): JsonNode? {
        return try {
            var url = "$mirrorNodeUrl/api/v1/topics/$topicId/messages?limit=$limit&order=asc"
            sequenceNumberGt?.let {
                url += "&sequencenumber=gt:$it"
            }
            timestampGt?.let {
                url += "&timestamp=gt:${it.epochSecond}.${it.nano}"
            }
            val response = restTemplate.getForObject<String>(url)
            objectMapper.readTree(response)
        } catch (e: Exception) {
            println("Failed to get messages for topic $topicId: ${e.message}")
            null
        }
    }
    
    /**
     * Get specific topic message by sequence number
     * GET /api/v1/topics/{topicId}/messages/{sequenceNumber}
     */
    fun getTopicMessage(topicId: String, sequenceNumber: Long): JsonNode? {
        return try {
            val url = "$mirrorNodeUrl/api/v1/topics/$topicId/messages/$sequenceNumber"
            val response = restTemplate.getForObject<String>(url)
            objectMapper.readTree(response)
        } catch (e: Exception) {
            println("Failed to get message $sequenceNumber from topic $topicId: ${e.message}")
            null
        }
    }
    
    /**
     * Get token information
     * GET /api/v1/tokens/{tokenId}
     */
    fun getToken(tokenId: String): JsonNode? {
        return try {
            val url = "$mirrorNodeUrl/api/v1/tokens/$tokenId"
            val response = restTemplate.getForObject<String>(url)
            objectMapper.readTree(response)
        } catch (e: Exception) {
            println("Failed to get token $tokenId: ${e.message}")
            null
        }
    }
    
    /**
     * Get NFT details
     * GET /api/v1/tokens/{tokenId}/nfts/{serialNumber}
     */
    fun getNFT(tokenId: String, serialNumber: Long): JsonNode? {
        return try {
            val url = "$mirrorNodeUrl/api/v1/tokens/$tokenId/nfts/$serialNumber"
            val response = restTemplate.getForObject<String>(url)
            objectMapper.readTree(response)
        } catch (e: Exception) {
            println("Failed to get NFT $tokenId/$serialNumber: ${e.message}")
            null
        }
    }
    
    /**
     * Get NFT transaction history
     * GET /api/v1/tokens/{tokenId}/nfts/{serialNumber}/transactions
     */
    fun getNFTTransactions(tokenId: String, serialNumber: Long): JsonNode? {
        return try {
            val url = "$mirrorNodeUrl/api/v1/tokens/$tokenId/nfts/$serialNumber/transactions"
            val response = restTemplate.getForObject<String>(url)
            objectMapper.readTree(response)
        } catch (e: Exception) {
            println("Failed to get NFT transactions for $tokenId/$serialNumber: ${e.message}")
            null
        }
    }
    
    /**
     * Get token balances
     * GET /api/v1/tokens/{tokenId}/balances
     */
    fun getTokenBalances(tokenId: String): JsonNode? {
        return try {
            val url = "$mirrorNodeUrl/api/v1/tokens/$tokenId/balances"
            val response = restTemplate.getForObject<String>(url)
            objectMapper.readTree(response)
        } catch (e: Exception) {
            println("Failed to get token balances for $tokenId: ${e.message}")
            null
        }
    }
    
    /**
     * Get smart contract details
     * GET /api/v1/contracts/{contractId}
     */
    fun getContract(contractId: String): JsonNode? {
        return try {
            val url = "$mirrorNodeUrl/api/v1/contracts/$contractId"
            val response = restTemplate.getForObject<String>(url)
            objectMapper.readTree(response)
        } catch (e: Exception) {
            println("Failed to get contract $contractId: ${e.message}")
            null
        }
    }
    
    /**
     * Verify document hash against HCS message
     * This is a helper method that combines our DB reference with Mirror Node query
     */
    fun verifyDocumentHash(topicId: String, sequenceNumber: Long, expectedHash: String): Boolean {
        val message = getTopicMessage(topicId, sequenceNumber) ?: return false
        
        // Decode base64 message content
        val messageBase64 = message.get("message")?.asText() ?: return false
        val messageBytes = java.util.Base64.getDecoder().decode(messageBase64)
        val messageContent = String(messageBytes)
        
        // Compare with expected hash
        return messageContent.contains(expectedHash)
    }
}
