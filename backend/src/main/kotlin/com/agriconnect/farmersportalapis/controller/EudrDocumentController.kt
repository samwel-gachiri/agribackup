package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.hedera.IpfsDocumentService
import com.agriconnect.farmersportalapis.domain.eudr.EudrDocumentType
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/documents")
@CrossOrigin(origins = ["*"])
class EudrDocumentController(
    private val ipfsDocumentService: IpfsDocumentService,
    private val objectMapper: ObjectMapper
) {
    
    private val logger = LoggerFactory.getLogger(EudrDocumentController::class.java)
    
    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadDocument(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("documentType") documentType: EudrDocumentType,
        @RequestParam("ownerEntityId") ownerEntityId: String,
        @RequestParam("ownerEntityType") ownerEntityType: String,
        @RequestParam("uploaderId") uploaderId: String,
        @RequestParam("metadata", required = false) metadataJson: String?
    ): ResponseEntity<DocumentUploadResponse> {
        return try {
            val metadata = if (metadataJson != null) {
                objectMapper.readValue(metadataJson, Map::class.java) as Map<String, String>
            } else {
                emptyMap()
            }
            
            val document = ipfsDocumentService.uploadDocument(
                file = file,
                documentType = documentType,
                ownerEntityId = ownerEntityId,
                ownerEntityType = ownerEntityType,
                uploaderId = uploaderId,
                metadata = metadata
            )
            
            val response = DocumentUploadResponse(
                success = true,
                documentId = document.id,
                fileName = document.fileName,
                ipfsHash = document.s3Key,
                checksumSha256 = document.checksumSha256,
                fileSize = document.fileSize,
                hederaTransactionId = document.hederaTransactionId,
                message = "Document uploaded successfully"
            )
            
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid document upload request", e)
            val response = DocumentUploadResponse(
                success = false,
                documentId = null,
                fileName = null,
                ipfsHash = null,
                checksumSha256 = null,
                fileSize = null,
                hederaTransactionId = null,
                message = null,
                error = e.message
            )
            ResponseEntity.badRequest().body(response)
        } catch (e: Exception) {
            logger.error("Failed to upload document", e)
            val response = DocumentUploadResponse(
                success = false,
                documentId = null,
                fileName = null,
                ipfsHash = null,
                checksumSha256 = null,
                fileSize = null,
                hederaTransactionId = null,
                message = null,
                error = "Upload failed: ${e.message}"
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
        }
    }
    
    @GetMapping("/{documentId}")
    fun getDocument(@PathVariable documentId: String): ResponseEntity<DocumentResponse> {
        return try {
            val documents = ipfsDocumentService.getDocumentsByOwner("", "") // This needs to be fixed
            val document = documents.find { it.id == documentId }
                ?: throw IllegalArgumentException("Document not found: $documentId")
            
            val response = DocumentResponse(
                id = document.id,
                fileName = document.fileName,
                originalFileName = document.fileName, // Using fileName as originalFileName doesn't exist
                mimeType = document.mimeType,
                fileSize = document.fileSize,
                checksumSha256 = document.checksumSha256,
                documentType = document.documentType,
                ownerEntityId = document.ownerEntityId,
                ownerEntityType = document.ownerEntityType,
                uploaderId = document.uploaderId,
                ipfsHash = document.s3Key,
                hederaTransactionId = document.hederaTransactionId,
                hederaHash = document.hederaHashRecord,
                uploadedAt = document.uploadedAt,
                retentionDate = document.retentionUntil?.atStartOfDay(),
                isIpfsVerified = document.s3Key.isNotEmpty(),
                isHederaVerified = document.hederaTransactionId != null,
                accessUrl = if (document.s3Key.isNotEmpty()) {
                    ipfsDocumentService.getDocumentAccessUrl(documentId)
                } else null
            )
            
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("Document not found: $documentId", e)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to get document: $documentId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/{documentId}/download")
    fun downloadDocument(@PathVariable documentId: String): ResponseEntity<ByteArrayResource> {
        return try {
            val documentBytes = ipfsDocumentService.downloadDocument(documentId)
            val documents = ipfsDocumentService.getDocumentsByOwner("", "") // This needs to be fixed
            val document = documents.find { it.id == documentId }
                ?: throw IllegalArgumentException("Document not found: $documentId")
            
            val resource = ByteArrayResource(documentBytes)
            
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${document.fileName}\"")
                .contentType(MediaType.parseMediaType(document.mimeType))
                .contentLength(documentBytes.size.toLong())
                .body(resource)
        } catch (e: IllegalArgumentException) {
            logger.warn("Document not found for download: $documentId", e)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Failed to download document: $documentId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @PostMapping("/{documentId}/access")
    fun getDocumentAccess(
        @PathVariable documentId: String,
        @RequestBody request: DocumentAccessRequest
    ): ResponseEntity<DocumentAccessResponse> {
        return try {
            val accessUrl = ipfsDocumentService.getDocumentAccessUrl(documentId, request.expiryMinutes)
            val expiresAt = LocalDateTime.now().plusMinutes(request.expiryMinutes.toLong())
            
            val response = DocumentAccessResponse(
                success = true,
                accessUrl = accessUrl,
                expiresAt = expiresAt,
                accessToken = null // Would be extracted from URL if needed
            )
            
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("Document not found for access: $documentId", e)
            val response = DocumentAccessResponse(
                success = false,
                accessUrl = null,
                expiresAt = null,
                accessToken = null,
                error = e.message
            )
            ResponseEntity.badRequest().body(response)
        } catch (e: Exception) {
            logger.error("Failed to get document access: $documentId", e)
            val response = DocumentAccessResponse(
                success = false,
                accessUrl = null,
                expiresAt = null,
                accessToken = null,
                error = "Access generation failed: ${e.message}"
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
        }
    }
    
    @PostMapping("/search")
    fun searchDocuments(@RequestBody request: DocumentSearchRequest): ResponseEntity<DocumentSearchResponse> {
        return try {
            val documents = ipfsDocumentService.searchDocuments(
                query = request.query,
                documentType = request.documentType,
                ownerEntityType = request.ownerEntityType,
                startDate = request.startDate,
                endDate = request.endDate
            )
            
            val responses = documents.map { document ->
                DocumentResponse(
                    id = document.id,
                    fileName = document.fileName,
                    originalFileName = document.fileName,
                    mimeType = document.mimeType,
                    fileSize = document.fileSize,
                    checksumSha256 = document.checksumSha256,
                    documentType = document.documentType,
                    ownerEntityId = document.ownerEntityId,
                    ownerEntityType = document.ownerEntityType,
                    uploaderId = document.uploaderId,
                    ipfsHash = document.s3Key,
                    hederaTransactionId = document.hederaTransactionId,
                    hederaHash = document.hederaHashRecord,
                    uploadedAt = document.uploadedAt,
                    retentionDate = document.retentionUntil?.atStartOfDay(),
                    isIpfsVerified = document.s3Key.isNotEmpty(),
                    isHederaVerified = document.hederaTransactionId != null,
                    accessUrl = null // Not included in search results for performance
                )
            }
            
            val searchResponse = DocumentSearchResponse(
                documents = responses,
                totalElements = responses.size.toLong(),
                totalPages = (responses.size + request.size - 1) / request.size,
                currentPage = request.page,
                pageSize = request.size,
                searchCriteria = request
            )
            
            ResponseEntity.ok(searchResponse)
        } catch (e: Exception) {
            logger.error("Failed to search documents", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @PostMapping("/integrity-check")
    fun checkDocumentIntegrity(@RequestBody request: DocumentIntegrityCheckRequest): ResponseEntity<DocumentIntegrityCheckResponse> {
        return try {
            val results = request.documentIds.map { documentId ->
                try {
                    val integrityResult = ipfsDocumentService.verifyDocumentIntegrity(documentId)
                    DocumentIntegrityResult(
                        documentId = documentId,
                        isValid = integrityResult.isValid,
                        isIpfsVerified = integrityResult.isIpfsVerified,
                        isHederaVerified = integrityResult.isHederaVerified,
                        isHashConsistent = integrityResult.isHashConsistent,
                        ipfsHash = integrityResult.ipfsHash,
                        hederaTransactionId = integrityResult.hederaTransactionId,
                        lastVerified = integrityResult.lastVerified,
                        error = integrityResult.error
                    )
                } catch (e: Exception) {
                    DocumentIntegrityResult(
                        documentId = documentId,
                        isValid = false,
                        isIpfsVerified = false,
                        isHederaVerified = false,
                        isHashConsistent = false,
                        ipfsHash = null,
                        hederaTransactionId = null,
                        lastVerified = LocalDateTime.now(),
                        error = e.message
                    )
                }
            }
            
            val response = DocumentIntegrityCheckResponse(results = results)
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to check document integrity", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/owner/{ownerEntityId}")
    fun getDocumentsByOwner(
        @PathVariable ownerEntityId: String,
        @RequestParam ownerEntityType: String
    ): ResponseEntity<List<DocumentResponse>> {
        return try {
            val documents = ipfsDocumentService.getDocumentsByOwner(ownerEntityId, ownerEntityType)
            
            val responses = documents.map { document ->
                DocumentResponse(
                    id = document.id,
                    fileName = document.fileName,
                    originalFileName = document.fileName,
                    mimeType = document.mimeType,
                    fileSize = document.fileSize,
                    checksumSha256 = document.checksumSha256,
                    documentType = document.documentType,
                    ownerEntityId = document.ownerEntityId,
                    ownerEntityType = document.ownerEntityType,
                    uploaderId = document.uploaderId,
                    ipfsHash = document.s3Key,
                    hederaTransactionId = document.hederaTransactionId,
                    hederaHash = document.hederaHashRecord,
                    uploadedAt = document.uploadedAt,
                    retentionDate = document.retentionUntil?.atStartOfDay(),
                    isIpfsVerified = document.s3Key.isNotEmpty(),
                    isHederaVerified = document.hederaTransactionId != null,
                    accessUrl = null
                )
            }
            
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get documents by owner: $ownerEntityId", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @GetMapping("/type/{documentType}")
    fun getDocumentsByType(@PathVariable documentType: EudrDocumentType): ResponseEntity<List<DocumentResponse>> {
        return try {
            val documents = ipfsDocumentService.getDocumentsByType(documentType)
            
            val responses = documents.map { document ->
                DocumentResponse(
                    id = document.id,
                    fileName = document.fileName,
                    originalFileName = document.fileName,
                    mimeType = document.mimeType,
                    fileSize = document.fileSize,
                    checksumSha256 = document.checksumSha256,
                    documentType = document.documentType,
                    ownerEntityId = document.ownerEntityId,
                    ownerEntityType = document.ownerEntityType,
                    uploaderId = document.uploaderId,
                    ipfsHash = document.s3Key,
                    hederaTransactionId = document.hederaTransactionId,
                    hederaHash = document.hederaHashRecord,
                    uploadedAt = document.uploadedAt,
                    retentionDate = document.retentionUntil?.atStartOfDay(),
                    isIpfsVerified = document.s3Key.isNotEmpty(),
                    isHederaVerified = document.hederaTransactionId != null,
                    accessUrl = null
                )
            }
            
            ResponseEntity.ok(responses)
        } catch (e: Exception) {
            logger.error("Failed to get documents by type: $documentType", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
    
    @DeleteMapping("/{documentId}")
    fun deleteDocument(@PathVariable documentId: String): ResponseEntity<Map<String, Any>> {
        return try {
            ipfsDocumentService.deleteDocument(documentId)
            
            val response: Map<String, Any> = mapOf(
                "success" to true,
                "message" to "Document deleted successfully",
                "documentId" to documentId
            )
            
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("Document not found for deletion: $documentId", e)
            val response: Map<String, Any> = mapOf(
                "success" to false,
                "error" to (e.message ?: "Unknown error"),
                "documentId" to documentId
            )
            ResponseEntity.badRequest().body(response)
        } catch (e: Exception) {
            logger.error("Failed to delete document: $documentId", e)
            val response: Map<String, Any> = mapOf(
                "success" to false,
                "error" to "Deletion failed: ${e.message ?: "Unknown error"}",
                "documentId" to documentId
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
        }
    }
    
    @GetMapping("/ipfs/stats")
    fun getIpfsStats(): ResponseEntity<IpfsStatsResponse> {
        return try {
            val stats = ipfsDocumentService.getIpfsStats()
            
            val response = IpfsStatsResponse(
                totalPins = stats.totalPins,
                totalSize = stats.totalSize,
                isConnected = stats.isConnected,
                gatewayUrl = "https://ipfs.io", // Would be configurable
                networkStats = null, // Would be populated with actual IPFS network stats
                error = stats.error
            )
            
            ResponseEntity.ok(response)
        } catch (e: Exception) {
            logger.error("Failed to get IPFS stats", e)
            val response = IpfsStatsResponse(
                totalPins = 0,
                totalSize = 0,
                isConnected = false,
                gatewayUrl = "",
                networkStats = null,
                error = e.message
            )
            ResponseEntity.ok(response)
        }
    }
    
    @GetMapping("/test/config")
    fun testConfiguration(): ResponseEntity<Map<String, Any>> {
        return try {
            val configInfo = mapOf(
                "ipfsEnabled" to ipfsDocumentService.getIpfsStats().isConnected,
                "supportedDocumentTypes" to EudrDocumentType.values().map { it.name },
                "maxFileSize" to "50MB",
                "allowedMimeTypes" to listOf(
                    "application/pdf",
                    "image/jpeg", 
                    "image/png",
                    "image/tiff",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "text/plain",
                    "application/json"
                )
            )
            
            ResponseEntity.ok(configInfo)
        } catch (e: Exception) {
            logger.error("Failed to get configuration info", e)
            val errorInfo = mapOf(
                "error" to (e.message ?: "Unknown error"),
                "ipfsEnabled" to false,
                "supportedDocumentTypes" to emptyList<String>()
            )
            ResponseEntity.ok(errorInfo)
        }
    }
}