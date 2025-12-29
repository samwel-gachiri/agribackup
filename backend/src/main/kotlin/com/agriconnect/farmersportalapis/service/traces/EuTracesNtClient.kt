package com.agriconnect.farmersportalapis.service.traces

import com.agriconnect.farmersportalapis.config.EuTracesNtConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * HTTP Client for EU TRACES NT API communication
 * 
 * This client handles:
 * - Authentication using UsernameToken with digest password
 * - DDS submission to TRACES NT endpoints
 * - Status polling for submitted statements
 * - Echo service for connectivity testing
 * 
 * When TRACES_NT_ENABLED=false, the client operates in mock mode
 * returning simulated responses for development/demo purposes.
 */
@Service
class EuTracesNtClient(
    private val config: EuTracesNtConfig,
    @Qualifier("tracesNtRestTemplate") private val restTemplate: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    companion object {
        private const val ECHO_ENDPOINT = "/ws/EudrEchoService"
        private const val DDS_SUBMIT_ENDPOINT = "/ws/EudrDdsService/submit"
        private const val DDS_STATUS_ENDPOINT = "/ws/EudrDdsService/status"
    }
    
    /**
     * Test connectivity to TRACES NT API
     */
    fun testConnectivity(): TracesConnectivityResult {
        if (!config.enabled) {
            logger.info("TRACES NT is disabled, returning mock connectivity result")
            return TracesConnectivityResult(
                connected = true,
                mode = "MOCK",
                message = "TRACES NT integration is in mock mode. Set TRACES_NT_ENABLED=true to enable live API.",
                environment = config.environment,
                timestamp = LocalDateTime.now()
            )
        }
        
        return try {
            val url = "${config.getActiveBaseUrl()}$ECHO_ENDPOINT?message=AgriBackup%20Connectivity%20Test"
            val headers = buildAuthHeaders()
            val entity = HttpEntity<String>(headers)
            
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)
            
            TracesConnectivityResult(
                connected = response.statusCode.is2xxSuccessful,
                mode = "LIVE",
                message = response.body ?: "Connected successfully",
                environment = config.environment,
                timestamp = LocalDateTime.now()
            )
        } catch (e: Exception) {
            logger.error("TRACES NT connectivity test failed: ${e.message}")
            TracesConnectivityResult(
                connected = false,
                mode = "LIVE",
                message = "Connection failed: ${e.message}",
                environment = config.environment,
                timestamp = LocalDateTime.now()
            )
        }
    }
    
    /**
     * Submit a Due Diligence Statement to TRACES NT
     */
    fun submitDds(dds: DueDiligenceStatement, xmlContent: String): TracesSubmissionResponse {
        if (!config.enabled) {
            logger.info("TRACES NT is disabled, returning mock submission response")
            return mockSubmissionResponse(dds)
        }
        
        if (!config.hasCredentials()) {
            logger.warn("TRACES NT credentials not configured")
            return TracesSubmissionResponse(
                success = false,
                tracesReferenceNumber = null,
                verificationNumber = null,
                status = "ERROR",
                message = "TRACES NT credentials not configured. Please set TRACES_NT_USERNAME and TRACES_NT_AUTH_KEY.",
                submittedAt = null
            )
        }
        
        return try {
            val url = "${config.getActiveBaseUrl()}$DDS_SUBMIT_ENDPOINT"
            val headers = buildAuthHeaders().apply {
                contentType = MediaType.APPLICATION_XML
                set("x-api-eudr-version", config.apiVersion.toString())
            }
            val entity = HttpEntity(xmlContent, headers)
            
            logger.info("Submitting DDS to TRACES NT: ${dds.header.internalReference}")
            val response = restTemplate.exchange(url, HttpMethod.POST, entity, TracesApiResponse::class.java)
            
            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val apiResponse = response.body!!
                TracesSubmissionResponse(
                    success = true,
                    tracesReferenceNumber = apiResponse.referenceNumber,
                    verificationNumber = apiResponse.verificationNumber,
                    status = apiResponse.status,
                    message = apiResponse.message ?: "Submission successful",
                    submittedAt = LocalDateTime.now()
                )
            } else {
                TracesSubmissionResponse(
                    success = false,
                    tracesReferenceNumber = null,
                    verificationNumber = null,
                    status = "ERROR",
                    message = "Submission failed with status: ${response.statusCode}",
                    submittedAt = null
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to submit DDS to TRACES NT: ${e.message}", e)
            TracesSubmissionResponse(
                success = false,
                tracesReferenceNumber = null,
                verificationNumber = null,
                status = "ERROR",
                message = "Submission failed: ${e.message}",
                submittedAt = null
            )
        }
    }
    
    /**
     * Check status of a submitted DDS
     */
    fun checkStatus(referenceNumber: String): TracesStatusResponse {
        if (!config.enabled) {
            return mockStatusResponse(referenceNumber)
        }
        
        return try {
            val url = "${config.getActiveBaseUrl()}$DDS_STATUS_ENDPOINT?reference=$referenceNumber"
            val headers = buildAuthHeaders()
            val entity = HttpEntity<String>(headers)
            
            val response = restTemplate.exchange(url, HttpMethod.GET, entity, TracesApiStatusResponse::class.java)
            
            if (response.statusCode.is2xxSuccessful && response.body != null) {
                val apiResponse = response.body!!
                TracesStatusResponse(
                    referenceNumber = referenceNumber,
                    status = TracesSubmissionStatus.valueOf(apiResponse.status.uppercase()),
                    lastUpdated = LocalDateTime.now(),
                    authorityFeedback = apiResponse.feedback,
                    validUntil = apiResponse.validUntil
                )
            } else {
                TracesStatusResponse(
                    referenceNumber = referenceNumber,
                    status = TracesSubmissionStatus.PENDING,
                    lastUpdated = null,
                    authorityFeedback = "Status check failed",
                    validUntil = null
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to check DDS status: ${e.message}", e)
            TracesStatusResponse(
                referenceNumber = referenceNumber,
                status = TracesSubmissionStatus.PENDING,
                lastUpdated = null,
                authorityFeedback = "Status check failed: ${e.message}",
                validUntil = null
            )
        }
    }
    
    // ========================================================================
    // AUTHENTICATION
    // ========================================================================
    
    /**
     * Build authentication headers for TRACES NT API
     * Uses UsernameToken with digest password as per TRACES NT security requirements
     */
    private fun buildAuthHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            set("Accept", MediaType.APPLICATION_JSON_VALUE)
            
            if (config.hasCredentials()) {
                // Generate nonce and timestamp for UsernameToken
                val nonce = generateNonce()
                val created = Instant.now().toString()
                val passwordDigest = generatePasswordDigest(nonce, created, config.auth.authKey)
                
                // Set WS-Security UsernameToken headers
                set("X-WSSE", buildWsseHeader(config.auth.username, passwordDigest, nonce, created))
                set("Authorization", "WSSE profile=\"UsernameToken\"")
            }
        }
    }
    
    private fun generateNonce(): String {
        val nonceBytes = ByteArray(16)
        Random().nextBytes(nonceBytes)
        return Base64.getEncoder().encodeToString(nonceBytes)
    }
    
    private fun generatePasswordDigest(nonce: String, created: String, password: String): String {
        val nonceBytes = Base64.getDecoder().decode(nonce)
        val createdBytes = created.toByteArray(Charsets.UTF_8)
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        
        val combined = nonceBytes + createdBytes + passwordBytes
        val digest = MessageDigest.getInstance("SHA-1").digest(combined)
        return Base64.getEncoder().encodeToString(digest)
    }
    
    private fun buildWsseHeader(username: String, passwordDigest: String, nonce: String, created: String): String {
        return """UsernameToken Username="$username", PasswordDigest="$passwordDigest", Nonce="$nonce", Created="$created""""
    }
    
    // ========================================================================
    // MOCK RESPONSES (for development/demo)
    // ========================================================================
    
    private fun mockSubmissionResponse(dds: DueDiligenceStatement): TracesSubmissionResponse {
        val mockReference = "EUDR-${System.currentTimeMillis()}-MOCK"
        val mockVerification = "VRF-${UUID.randomUUID().toString().take(8).uppercase()}"
        
        return TracesSubmissionResponse(
            success = true,
            tracesReferenceNumber = mockReference,
            verificationNumber = mockVerification,
            status = "RECEIVED",
            message = "[MOCK MODE] DDS received successfully. In production, this would be submitted to EU TRACES NT. " +
                    "Internal reference: ${dds.header.internalReference}",
            submittedAt = LocalDateTime.now()
        )
    }
    
    private fun mockStatusResponse(referenceNumber: String): TracesStatusResponse {
        return TracesStatusResponse(
            referenceNumber = referenceNumber,
            status = TracesSubmissionStatus.RECEIVED,
            lastUpdated = LocalDateTime.now(),
            authorityFeedback = "[MOCK MODE] Statement is pending review by competent authority.",
            validUntil = null
        )
    }
}

// ========================================================================
// API RESPONSE MODELS
// ========================================================================

data class TracesConnectivityResult(
    val connected: Boolean,
    val mode: String,
    val message: String,
    val environment: String,
    val timestamp: LocalDateTime
)

data class TracesApiResponse(
    val referenceNumber: String?,
    val verificationNumber: String?,
    val status: String,
    val message: String?
)

data class TracesApiStatusResponse(
    val status: String,
    val feedback: String?,
    val validUntil: java.time.LocalDate?
)
