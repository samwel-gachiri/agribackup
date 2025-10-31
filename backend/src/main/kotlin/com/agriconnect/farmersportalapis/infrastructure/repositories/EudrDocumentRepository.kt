package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.eudr.EudrDocument
import com.agriconnect.farmersportalapis.domain.eudr.EudrDocumentType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface EudrDocumentRepository : JpaRepository<EudrDocument, String> {
    
    fun findByChecksumSha256(checksumSha256: String): EudrDocument?
    
    fun findByOwnerEntityIdAndOwnerEntityType(ownerEntityId: String, ownerEntityType: String): List<EudrDocument>
    
    fun findByDocumentType(documentType: EudrDocumentType): List<EudrDocument>
    
    fun findByUploaderId(uploaderId: String): List<EudrDocument>
    
    fun findByS3Key(s3Key: String): EudrDocument?
    
    fun findByHederaTransactionId(hederaTransactionId: String): EudrDocument?
    
    @Query("""
        SELECT d FROM EudrDocument d 
        WHERE (:query IS NULL OR 
               LOWER(d.fileName) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:documentType IS NULL OR d.documentType = :documentType)
        AND (:ownerEntityType IS NULL OR d.ownerEntityType = :ownerEntityType)
        AND (:startDate IS NULL OR d.uploadedAt >= :startDate)
        AND (:endDate IS NULL OR d.uploadedAt <= :endDate)
        ORDER BY d.uploadedAt DESC
    """)
    fun searchDocuments(
        @Param("query") query: String?,
        @Param("documentType") documentType: EudrDocumentType?,
        @Param("ownerEntityType") ownerEntityType: String?,
        @Param("startDate") startDate: LocalDateTime?,
        @Param("endDate") endDate: LocalDateTime?
    ): List<EudrDocument>
    
    @Query("""
        SELECT d FROM EudrDocument d 
        WHERE d.ownerEntityId = :entityId 
        AND d.ownerEntityType = :entityType 
        AND d.documentType = :documentType
        ORDER BY d.uploadedAt DESC
    """)
    fun findByOwnerAndType(
        @Param("entityId") entityId: String,
        @Param("entityType") entityType: String,
        @Param("documentType") documentType: EudrDocumentType
    ): List<EudrDocument>
    
    @Query("""
        SELECT COUNT(d) FROM EudrDocument d 
        WHERE d.ownerEntityId = :entityId 
        AND d.ownerEntityType = :entityType
    """)
    fun countByOwner(
        @Param("entityId") entityId: String,
        @Param("entityType") entityType: String
    ): Long
    
    @Query("""
        SELECT d FROM EudrDocument d 
        WHERE d.hederaTransactionId IS NOT NULL
        AND d.uploadedAt BETWEEN :startDate AND :endDate
        ORDER BY d.uploadedAt DESC
    """)
    fun findVerifiedDocumentsByDateRange(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<EudrDocument>
    
    @Query(
        """
        SELECT d FROM EudrDocument d 
        WHERE d.s3Key IS NOT NULL AND d.s3Key != ''
        AND d.uploadedAt >= :since
        ORDER BY d.uploadedAt DESC
    """
    )
    fun findIpfsDocumentsSince(
        @Param("since") since: LocalDateTime
    ): List<EudrDocument>
    
    @Query("""
        SELECT d FROM EudrDocument d 
        WHERE d.retentionUntil IS NOT NULL 
        AND d.retentionUntil <= :cutoffDate
    """)
    fun findDocumentsForRetention(
        @Param("cutoffDate") cutoffDate: LocalDate
    ): List<EudrDocument>
    
    @Query("""
        SELECT d.documentType, COUNT(d) 
        FROM EudrDocument d 
        WHERE d.ownerEntityId = :entityId 
        AND d.ownerEntityType = :entityType
        GROUP BY d.documentType
    """)
    fun getDocumentTypeStatistics(
        @Param("entityId") entityId: String,
        @Param("entityType") entityType: String
    ): List<Array<Any>>
    
    @Query("""
        SELECT SUM(d.fileSize) FROM EudrDocument d 
        WHERE d.ownerEntityId = :entityId 
        AND d.ownerEntityType = :entityType
    """)
    fun getTotalFileSizeByOwner(
        @Param("entityId") entityId: String,
        @Param("entityType") entityType: String
    ): Long?
    
    fun deleteByUploadedAtBefore(cutoffDate: LocalDateTime): Long
}