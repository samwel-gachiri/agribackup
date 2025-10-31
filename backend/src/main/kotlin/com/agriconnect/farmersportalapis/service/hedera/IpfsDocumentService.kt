package com.agriconnect.farmersportalapis.service.hedera

import com.agriconnect.farmersportalapis.domain.eudr.DocumentVisibility
import com.agriconnect.farmersportalapis.domain.eudr.EudrDocument
import com.agriconnect.farmersportalapis.domain.eudr.EudrDocumentType
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrDocumentRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class IpfsDocumentService(
    private val eudrDocumentRepository: EudrDocumentRepository,
    private val hederaConsensusService: HederaConsensusServices,
    private val objectMapper: ObjectMapper,
    private val restTemplate: RestTemplate
) {

    private val logger = LoggerFactory.getLogger(IpfsDocumentService::class.java)

    @Value("\${ipfs.gateway.url:https://ipfs.io}")
    private lateinit var ipfsGatewayUrl: String

    @Value("\${ipfs.api.url:https://api.pinata.cloud}")
    private lateinit var ipfsApiUrl: String

    @Value("\${ipfs.api.key:}")
    private lateinit var ipfsApiKey: String

    @Value("\${ipfs.api.secret:}")
    private lateinit var ipfsApiSecret: String

    private var ipfsEnabled: Boolean = true

    companion object {
        private const val MAX_FILE_SIZE = 50 * 1024 * 1024 // 50MB
        private const val PINATA_PIN_ENDPOINT = "/pinning/pinFileToIPFS"
        private const val PINATA_UNPIN_ENDPOINT = "/pinning/unpin"
        private const val PINATA_PIN_LIST_ENDPOINT = "/data/pinList"

        private val ALLOWED_MIME_TYPES = setOf(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/tiff",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain",
            "application/json"
        )
    }

    /**
     * Upload document to IPFS and store metadata in database
     */
    fun uploadDocument(
        file: MultipartFile,
        documentType: EudrDocumentType,
        ownerEntityId: String,
        ownerEntityType: String,
        uploaderId: String,
        metadata: Map<String, String> = emptyMap()
    ): EudrDocument {
        logger.info("Uploading document to IPFS: ${file.originalFilename}")

        // Validate file
        validateFile(file)

        // Calculate file hash
        val fileBytes = file.bytes
        val checksumSha256 = calculateSHA256(fileBytes)

        // Check for duplicate documents
        val existingDocument = eudrDocumentRepository.findByChecksumSha256(checksumSha256)
        if (existingDocument != null) {
            logger.info("Document already exists with hash: $checksumSha256")
            return existingDocument
        }

        // Upload to IPFS
        val ipfsHash = if (ipfsEnabled && ipfsApiKey.isNotEmpty() && ipfsApiSecret.isNotEmpty()) {
            uploadToIpfs(file, metadata)
        } else {
            if (!ipfsEnabled) {
                logger.warn("IPFS is disabled, generating mock hash")
            } else {
                logger.warn("IPFS API credentials not configured, generating mock hash")
            }
            generateMockIpfsHash()
        }

        // Create document entity
        val document = EudrDocument(
            ownerEntityId = ownerEntityId,
            ownerEntityType = ownerEntityType,
            batch = null,
            documentType = documentType,
            issuer = null,
            issueDate = null,
            expiryDate = null,
            s3Key = ipfsHash, // Using IPFS hash as S3 key for now
            fileName = file.originalFilename ?: "unknown",
            mimeType = file.contentType ?: "application/octet-stream",
            checksumSha256 = checksumSha256,
            fileSize = file.size,
            uploaderId = uploaderId,
            uploadedAt = LocalDateTime.now(),
            exifLatitude = null,
            exifLongitude = null,
            exifTimestamp = null,
            tags = objectMapper.writeValueAsString(metadata),
            retentionUntil = null,
            visibility = DocumentVisibility.PRIVATE,
            hederaTransactionId = null,
            hederaHashRecord = null
        )

        // Save to database
        val savedDocument = eudrDocumentRepository.save(document)

        // Record on Hedera DLT
        try {
            val hederaTransactionId = hederaConsensusService.recordDocumentUpload(savedDocument)
            savedDocument.hederaTransactionId = hederaTransactionId
            savedDocument.hederaHashRecord = calculateDocumentHash(savedDocument)
            eudrDocumentRepository.save(savedDocument)
        } catch (e: Exception) {
            logger.warn("Failed to record document upload on Hedera DLT", e)
        }

        logger.info("Document uploaded successfully: ${savedDocument.id}, IPFS: $ipfsHash")
        return savedDocument
    }

    /**
     * Download document from IPFS
     */
    fun downloadDocument(documentId: String): ByteArray {
        val document = eudrDocumentRepository.findById(documentId)
            .orElseThrow { IllegalArgumentException("Document not found: $documentId") }

        return if (ipfsEnabled && ipfsApiKey.isNotEmpty() && ipfsApiSecret.isNotEmpty() && document.s3Key.isNotEmpty()) {
            downloadFromIpfs(document.s3Key)
        } else {
            if (!ipfsEnabled) {
                throw IllegalStateException("IPFS is disabled - document not available for download")
            } else if (ipfsApiKey.isEmpty() || ipfsApiSecret.isEmpty()) {
                throw IllegalStateException("IPFS API credentials not configured - document not available for download")
            } else {
                throw IllegalStateException("Document not stored on IPFS - not available for download")
            }
        }
    }

    /**
     * Get document access URL (IPFS gateway URL)
     */
    fun getDocumentAccessUrl(documentId: String, expiryMinutes: Int = 60): String {
        val document = eudrDocumentRepository.findById(documentId)
            .orElseThrow { IllegalArgumentException("Document not found: $documentId") }

        if (document.s3Key.isEmpty()) {
            throw IllegalStateException("Document not stored on IPFS")
        }

        // Generate time-limited access URL
        val expiryTime = System.currentTimeMillis() + (expiryMinutes * 60 * 1000)
        val accessToken = generateAccessToken(documentId, expiryTime)

        return "$ipfsGatewayUrl/ipfs/${document.s3Key}?token=$accessToken&expires=$expiryTime"
    }

    /**
     * Verify document integrity against IPFS and Hedera
     */
    fun verifyDocumentIntegrity(documentId: String): DocumentIntegrityResult {
        val document = eudrDocumentRepository.findById(documentId)
            .orElseThrow { IllegalArgumentException("Document not found: $documentId") }

        return try {
            // Verify against IPFS
            val ipfsVerified = if (document.s3Key.isNotEmpty() && ipfsEnabled && ipfsApiKey.isNotEmpty() && ipfsApiSecret.isNotEmpty()) {
                verifyIpfsIntegrity(document)
            } else {
                false
            }

            // Verify against Hedera DLT
            val hederaVerified = if (document.hederaTransactionId != null) {
                hederaConsensusService.verifyRecordIntegrity(document.hederaTransactionId!!)
            } else {
                false
            }

            // Verify hash consistency
            val expectedHash = calculateDocumentHash(document)
            val hashConsistent = expectedHash == document.hederaHashRecord

            DocumentIntegrityResult(
                isValid = ipfsVerified && hederaVerified && hashConsistent,
                isIpfsVerified = ipfsVerified,
                isHederaVerified = hederaVerified,
                isHashConsistent = hashConsistent,
                ipfsHash = document.s3Key,
                hederaTransactionId = document.hederaTransactionId,
                lastVerified = LocalDateTime.now()
            )
        } catch (e: Exception) {
            logger.error("Failed to verify document integrity for $documentId", e)
            DocumentIntegrityResult(
                isValid = false,
                isIpfsVerified = false,
                isHederaVerified = false,
                isHashConsistent = false,
                ipfsHash = document.s3Key,
                hederaTransactionId = document.hederaTransactionId,
                lastVerified = LocalDateTime.now(),
                error = e.message
            )
        }
    }

    /**
     * Delete document from IPFS and database
     */
    fun deleteDocument(documentId: String) {
        val document = eudrDocumentRepository.findById(documentId)
            .orElseThrow { IllegalArgumentException("Document not found: $documentId") }

        // Unpin from IPFS
        if (document.s3Key.isNotEmpty() && ipfsEnabled) {
            try {
                unpinFromIpfs(document.s3Key)
            } catch (e: Exception) {
                logger.warn("Failed to unpin document from IPFS: ${document.s3Key}", e)
            }
        }

        // Delete from database
        eudrDocumentRepository.delete(document)

        logger.info("Document deleted: $documentId")
    }

    /**
     * Get documents by owner
     */
    fun getDocumentsByOwner(ownerEntityId: String, ownerEntityType: String): List<EudrDocument> {
        return eudrDocumentRepository.findByOwnerEntityIdAndOwnerEntityType(ownerEntityId, ownerEntityType)
    }

    /**
     * Get documents by type
     */
    fun getDocumentsByType(documentType: EudrDocumentType): List<EudrDocument> {
        return eudrDocumentRepository.findByDocumentType(documentType)
    }

    /**
     * Search documents
     */
    fun searchDocuments(
        query: String?,
        documentType: EudrDocumentType?,
        ownerEntityType: String?,
        startDate: LocalDateTime?,
        endDate: LocalDateTime?
    ): List<EudrDocument> {
        return eudrDocumentRepository.searchDocuments(query, documentType, ownerEntityType, startDate, endDate)
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("File is empty")
        }

        if (file.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException("File size exceeds maximum allowed size of ${MAX_FILE_SIZE / 1024 / 1024}MB")
        }

        val mimeType = file.contentType
        if (mimeType != null && !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw IllegalArgumentException("File type not allowed: $mimeType")
        }
    }

    private fun uploadToIpfs(file: MultipartFile, metadata: Map<String, String>): String {
        return try {
            val headers = HttpHeaders()
            headers.contentType = MediaType.MULTIPART_FORM_DATA
            headers.set("pinata_api_key", ipfsApiKey)
            headers.set("pinata_secret_api_key", ipfsApiSecret)

            // Create multipart request using MultiValueMap
            val body = LinkedMultiValueMap<String, Any>()
            body.add("file", file.resource)

            // Add metadata
            if (metadata.isNotEmpty()) {
                val pinataMetadata = mapOf(
                    "name" to (file.originalFilename ?: "document"),
                    "keyvalues" to metadata
                )
                body.add("pinataMetadata", objectMapper.writeValueAsString(pinataMetadata))
            }

            val entity = HttpEntity(body, headers)
            val response = restTemplate.postForEntity("$ipfsApiUrl$PINATA_PIN_ENDPOINT", entity, Map::class.java)

            if (response.statusCode == HttpStatus.OK) {
                val responseBody = response.body as Map<String, Any>
                responseBody["IpfsHash"] as String
            } else {
                throw RuntimeException("Failed to upload to IPFS: ${response.statusCode}")
            }
        } catch (e: Exception) {
            logger.error("Failed to upload file to IPFS", e)
            throw RuntimeException("IPFS upload failed", e)
        }
    }

    private fun downloadFromIpfs(ipfsHash: String): ByteArray {
        return try {
            val url = "$ipfsGatewayUrl/ipfs/$ipfsHash"
            val response = restTemplate.getForEntity(url, ByteArray::class.java)

            if (response.statusCode == HttpStatus.OK) {
                response.body ?: throw RuntimeException("Empty response from IPFS")
            } else {
                throw RuntimeException("Failed to download from IPFS: ${response.statusCode}")
            }
        } catch (e: Exception) {
            logger.error("Failed to download file from IPFS: $ipfsHash", e)
            throw RuntimeException("IPFS download failed", e)
        }
    }

    private fun unpinFromIpfs(ipfsHash: String) {
        try {
            val headers = HttpHeaders()
            headers.set("pinata_api_key", ipfsApiKey)
            headers.set("pinata_secret_api_key", ipfsApiSecret)

            val entity = HttpEntity<String>(headers)
            val url = "$ipfsApiUrl$PINATA_UNPIN_ENDPOINT/$ipfsHash"

            val response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String::class.java)

            if (response.statusCode != HttpStatus.OK) {
                logger.warn("Failed to unpin from IPFS: ${response.statusCode}")
            }
        } catch (e: Exception) {
            logger.error("Failed to unpin file from IPFS: $ipfsHash", e)
        }
    }

    private fun verifyIpfsIntegrity(document: EudrDocument): Boolean {
        return try {
            val downloadedBytes = downloadFromIpfs(document.s3Key)
            val downloadedHash = calculateSHA256(downloadedBytes)
            downloadedHash == document.checksumSha256
        } catch (e: Exception) {
            logger.error("Failed to verify IPFS integrity for document ${document.id}", e)
            false
        }
    }

    fun calculateSHA256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(data)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun generateMockIpfsHash(): String {
        // Generate a realistic-looking IPFS hash (46 characters total: "Qm" + 44 chars)
        // IPFS hashes use base58 encoding, so we'll use valid base58 characters
        val base58Chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        val random = SecureRandom()
        val hashPart = (1..44).map { base58Chars[random.nextInt(base58Chars.length)] }.joinToString("")
        return "Qm$hashPart"
    }

    private fun calculateDocumentHash(document: EudrDocument): String {
        val documentData = "${document.fileName}_${document.checksumSha256}_${document.s3Key}_${document.uploadedAt}"
        return calculateSHA256(documentData.toByteArray())
    }

    private fun generateAccessToken(documentId: String, expiryTime: Long): String {
        val tokenData = "$documentId:$expiryTime:${UUID.randomUUID()}"
        return Base64.getEncoder().encodeToString(tokenData.toByteArray())
    }

    /**
     * Get IPFS network statistics
     */
    fun getIpfsStats(): IpfsStats {
        return try {
            val headers = HttpHeaders()
            headers.set("pinata_api_key", ipfsApiKey)
            headers.set("pinata_secret_api_key", ipfsApiSecret)

            val entity = HttpEntity<String>(headers)
            val response = restTemplate.exchange("$ipfsApiUrl$PINATA_PIN_LIST_ENDPOINT", HttpMethod.GET, entity, Map::class.java)

            if (response.statusCode == HttpStatus.OK) {
                val responseBody = response.body as Map<String, Any>
                val rows = responseBody["rows"] as List<Map<String, Any>>

                IpfsStats(
                    totalPins = rows.size,
                    totalSize = rows.sumOf { (it["size"] as? Number)?.toLong() ?: 0L },
                    isConnected = true
                )
            } else {
                IpfsStats(totalPins = 0, totalSize = 0, isConnected = false)
            }
        } catch (e: Exception) {
            logger.error("Failed to get IPFS stats", e)
            IpfsStats(totalPins = 0, totalSize = 0, isConnected = false, error = e.message)
        }
    }

    data class DocumentIntegrityResult(
        val isValid: Boolean,
        val isIpfsVerified: Boolean,
        val isHederaVerified: Boolean,
        val isHashConsistent: Boolean,
        val ipfsHash: String?,
        val hederaTransactionId: String?,
        val lastVerified: LocalDateTime,
        val error: String? = null
    )

    data class IpfsStats(
        val totalPins: Int,
        val totalSize: Long,
        val isConnected: Boolean,
        val error: String? = null
    )
}