package com.agriconnect.farmersportalapis.application.dtos

import com.agriconnect.farmersportalapis.domain.eudr.EudrDocumentType
import java.time.LocalDateTime

data class DocumentUploadRequest(
    val documentType: EudrDocumentType,
    val ownerEntityId: String,
    val ownerEntityType: String,
    val metadata: Map<String, String> = emptyMap()
)

data class DocumentUploadResponse(
    val success: Boolean,
    val documentId: String?,
    val fileName: String?,
    val ipfsHash: String?,
    val checksumSha256: String?,
    val fileSize: Long?,
    val hederaTransactionId: String?,
    val message: String?,
    val error: String? = null
)

data class DocumentResponse(
    val id: String,
    val fileName: String,
    val originalFileName: String,
    val mimeType: String,
    val fileSize: Long,
    val checksumSha256: String,
    val documentType: EudrDocumentType,
    val ownerEntityId: String,
    val ownerEntityType: String,
    val uploaderId: String,
    val ipfsHash: String?,
    val hederaTransactionId: String?,
    val hederaHash: String?,
    val uploadedAt: LocalDateTime,
    val retentionDate: LocalDateTime?,
    val isIpfsVerified: Boolean,
    val isHederaVerified: Boolean,
    val accessUrl: String?,
    val metadata: Map<String, String> = emptyMap()
)

data class DocumentSearchRequest(
    val query: String? = null,
    val documentType: EudrDocumentType? = null,
    val ownerEntityType: String? = null,
    val ownerEntityId: String? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "uploadedAt",
    val sortDirection: String = "DESC"
)

data class DocumentSearchResponse(
    val documents: List<DocumentResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val searchCriteria: DocumentSearchRequest
)

data class DocumentIntegrityCheckRequest(
    val documentIds: List<String>
)

data class DocumentIntegrityCheckResponse(
    val results: List<DocumentIntegrityResult>
)

data class DocumentIntegrityResult(
    val documentId: String,
    val isValid: Boolean,
    val isIpfsVerified: Boolean,
    val isHederaVerified: Boolean,
    val isHashConsistent: Boolean,
    val ipfsHash: String?,
    val hederaTransactionId: String?,
    val lastVerified: LocalDateTime,
    val error: String? = null
)

data class DocumentAccessRequest(
    val documentId: String,
    val expiryMinutes: Int = 60,
    val accessType: AccessType = AccessType.VIEW
)

enum class AccessType {
    VIEW, DOWNLOAD, SHARE
}

data class DocumentAccessResponse(
    val success: Boolean,
    val accessUrl: String?,
    val expiresAt: LocalDateTime?,
    val accessToken: String?,
    val error: String? = null
)

data class DocumentMetadataUpdateRequest(
    val metadata: Map<String, String>
)

data class DocumentRetentionRequest(
    val retentionDate: LocalDateTime,
    val reason: String
)

data class DocumentRetentionResponse(
    val success: Boolean,
    val documentId: String,
    val retentionDate: LocalDateTime?,
    val message: String?
)

data class DocumentStatisticsRequest(
    val ownerEntityId: String? = null,
    val ownerEntityType: String? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val groupBy: DocumentStatisticsGroupBy = DocumentStatisticsGroupBy.DOCUMENT_TYPE
)



data class DocumentStatisticsResponse(
    val statistics: List<DocumentStatistic>,
    val summary: DocumentStatisticsSummary
)

data class DocumentStatistic(
    val category: String,
    val count: Long,
    val totalSize: Long,
    val averageSize: Double,
    val percentage: Double
)

data class DocumentStatisticsSummary(
    val totalDocuments: Long,
    val totalSize: Long,
    val averageSize: Double,
    val ipfsDocuments: Long,
    val hederaVerifiedDocuments: Long,
    val documentTypes: Map<EudrDocumentType, Long>
)

data class IpfsStatsResponse(
    val totalPins: Int,
    val totalSize: Long,
    val isConnected: Boolean,
    val gatewayUrl: String,
    val networkStats: IpfsNetworkStats?,
    val error: String? = null
)

data class IpfsNetworkStats(
    val peersConnected: Int,
    val repoSize: Long,
    val storageMax: Long,
    val version: String
)

data class DocumentBulkOperationRequest(
    val documentIds: List<String>,
    val operation: BulkOperation,
    val parameters: Map<String, String> = emptyMap()
)

enum class BulkOperation {
    DELETE, UPDATE_METADATA, SET_RETENTION, VERIFY_INTEGRITY, REGENERATE_ACCESS_URLS
}

data class DocumentBulkOperationResponse(
    val success: Boolean,
    val processedCount: Int,
    val failedCount: Int,
    val results: List<BulkOperationResult>,
    val errors: List<String> = emptyList()
)

data class BulkOperationResult(
    val documentId: String,
    val success: Boolean,
    val message: String?,
    val error: String? = null
)

data class DocumentVersionRequest(
    val documentId: String,
    val versionComment: String? = null
)

data class DocumentVersionResponse(
    val success: Boolean,
    val versionId: String?,
    val versionNumber: Int?,
    val previousVersionId: String?,
    val message: String?
)

data class DocumentAuditLogRequest(
    val documentId: String,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null
)

data class DocumentAuditLogResponse(
    val documentId: String,
    val auditEntries: List<DocumentAuditEntry>
)

data class DocumentAuditEntry(
    val id: String,
    val action: String,
    val actorId: String,
    val actorRole: String,
    val timestamp: LocalDateTime,
    val details: Map<String, String>,
    val ipAddress: String?,
    val userAgent: String?
)

data class DocumentExportRequest(
    val searchCriteria: DocumentSearchRequest,
    val format: ExportFormat,
    val includeContent: Boolean = false,
    val includeMetadata: Boolean = true
)



data class DocumentExportResponse(
    val success: Boolean,
    val downloadUrl: String?,
    val fileName: String?,
    val recordCount: Int,
    val expiresAt: LocalDateTime?,
    val error: String? = null
)