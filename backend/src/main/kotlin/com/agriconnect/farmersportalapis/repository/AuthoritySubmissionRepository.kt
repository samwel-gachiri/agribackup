package com.agriconnect.farmersportalapis.repository

import com.agriconnect.farmersportalapis.domain.eudr.AuthoritySubmission
import com.agriconnect.farmersportalapis.domain.eudr.SubmissionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AuthoritySubmissionRepository : JpaRepository<AuthoritySubmission, String> {
    
    fun findByBatch_Id(batchId: String): List<AuthoritySubmission>
    
    fun findByBatch_Id(batchId: String, pageable: Pageable): Page<AuthoritySubmission>
    
    fun findBySubmittedBy(userId: String): List<AuthoritySubmission>
    
    fun findBySubmittedBy(userId: String, pageable: Pageable): Page<AuthoritySubmission>
    
    fun findByAuthorityCode(authorityCode: String): List<AuthoritySubmission>
    
    fun findByAuthorityCode(authorityCode: String, pageable: Pageable): Page<AuthoritySubmission>
    
    fun findByStatus(status: SubmissionStatus): List<AuthoritySubmission>
    
    fun findByStatus(status: SubmissionStatus, pageable: Pageable): Page<AuthoritySubmission>
    
    fun findBySubmittedByAndStatus(userId: String, status: SubmissionStatus): List<AuthoritySubmission>
    
    fun findByBatch_IdAndAuthorityCode(batchId: String, authorityCode: String): List<AuthoritySubmission>
    
    fun findBySubmissionReference(reference: String): AuthoritySubmission?
    
    fun findByAuthorityReference(reference: String): AuthoritySubmission?
    
    @Query("""
        SELECT s FROM AuthoritySubmission s 
        WHERE s.submittedBy = :userId 
        AND (:batchId IS NULL OR s.batch.id = :batchId)
        AND (:authorityCode IS NULL OR s.authorityCode = :authorityCode)
        AND (:status IS NULL OR s.status = :status)
        ORDER BY s.submittedAt DESC
    """)
    fun findByFilters(
        userId: String,
        batchId: String?,
        authorityCode: String?,
        status: SubmissionStatus?,
        pageable: Pageable
    ): Page<AuthoritySubmission>
    
    @Query("""
        SELECT COUNT(s) FROM AuthoritySubmission s 
        WHERE s.submittedBy = :userId 
        AND s.status = :status
    """)
    fun countBySubmittedByAndStatus(userId: String, status: SubmissionStatus): Long
    
    @Query("""
        SELECT s FROM AuthoritySubmission s 
        WHERE s.status IN (:statuses) 
        AND s.submittedAt < :before
    """)
    fun findStaleSubmissions(statuses: List<SubmissionStatus>, before: LocalDateTime): List<AuthoritySubmission>
    
    @Query("""
        SELECT s.authorityCode, COUNT(s) 
        FROM AuthoritySubmission s 
        WHERE s.submittedBy = :userId 
        GROUP BY s.authorityCode
    """)
    fun countByAuthorityForUser(userId: String): List<Array<Any>>
    
    @Query("""
        SELECT s.status, COUNT(s) 
        FROM AuthoritySubmission s 
        WHERE s.submittedBy = :userId 
        GROUP BY s.status
    """)
    fun countByStatusForUser(userId: String): List<Array<Any>>
}
