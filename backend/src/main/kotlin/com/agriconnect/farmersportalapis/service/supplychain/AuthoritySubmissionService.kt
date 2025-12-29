package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.domain.eudr.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrBatchRepository
import com.agriconnect.farmersportalapis.repository.AuthoritySubmissionRepository
import com.agriconnect.farmersportalapis.service.hedera.AsyncHederaService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.LocalDateTime

/**
 * Service for managing authority report submissions
 * 
 * This service handles:
 * - Creating and tracking submissions to regulatory authorities
 * - Status updates and tracking
 * - Async blockchain recording of submissions
 * - Submission history and statistics
 */
@Service
@Transactional
class AuthoritySubmissionService(
    private val submissionRepository: AuthoritySubmissionRepository,
    private val batchRepository: EudrBatchRepository,
    private val dossierService: DossierService,
    private val asyncHederaService: AsyncHederaService
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Submit report to authority
     */
    fun submitReport(
        batchId: String,
        authorityCode: String,
        userId: String,
        notes: String? = null
    ): AuthoritySubmissionDto {
        logger.info("Submitting report for batch $batchId to authority $authorityCode")

        val batch = batchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        // Generate the report
        val report = dossierService.generateDossier(
            batchId = batchId,
            format = DossierFormat.PDF,
            includePresignedUrls = true,
            expiryMinutes = 180
        )

        // Calculate report hash for verification
        val reportHash = if (report.content is ByteArray) {
            calculateHash(report.content as ByteArray)
        } else {
            calculateHash((report.content as String).toByteArray())
        }

        // Get authority details
        val authority = try {
            AuthorityCode.valueOf(authorityCode)
        } catch (e: Exception) {
            null
        }

        // Create submission record
        val submission = AuthoritySubmission(
            batch = batch,
            authorityCode = authorityCode,
            authorityName = authority?.displayName,
            submittedBy = userId,
            reportFilename = report.filename,
            reportHash = reportHash,
            status = SubmissionStatus.SUBMITTED,
            submissionReference = generateSubmissionReference(batch, authorityCode),
            notes = notes,
            submittedAt = LocalDateTime.now()
        )

        val saved = submissionRepository.save(submission)

        // Record on blockchain asynchronously
        asyncHederaService.recordAuthoritySubmissionAsync(
            submissionId = saved.id,
            batchId = batchId,
            authorityCode = authorityCode,
            reportHash = reportHash
        ).thenAccept { transactionId ->
            if (transactionId != null) {
                submission.hederaTransactionId = transactionId
                submissionRepository.save(submission)
                logger.info("Authority submission recorded on blockchain: $transactionId")
            }
        }

        logger.info("Report submitted successfully: ${saved.id}")
        return toDto(saved)
    }

    /**
     * Update submission status
     */
    fun updateStatus(
        submissionId: String,
        newStatus: SubmissionStatus,
        authorityFeedback: String? = null,
        authorityReference: String? = null
    ): AuthoritySubmissionDto {
        val submission = submissionRepository.findById(submissionId)
            .orElseThrow { IllegalArgumentException("Submission not found: $submissionId") }

        submission.status = newStatus
        submission.lastStatusUpdate = LocalDateTime.now()
        submission.authorityFeedback = authorityFeedback ?: submission.authorityFeedback
        submission.authorityReference = authorityReference ?: submission.authorityReference

        when (newStatus) {
            SubmissionStatus.ACKNOWLEDGED -> submission.acknowledgedAt = LocalDateTime.now()
            SubmissionStatus.APPROVED, SubmissionStatus.CONDITIONALLY_APPROVED -> {
                submission.approvedAt = LocalDateTime.now()
            }
            SubmissionStatus.REJECTED -> submission.rejectedAt = LocalDateTime.now()
            else -> {}
        }

        val saved = submissionRepository.save(submission)
        return toDto(saved)
    }

    /**
     * Get submission by ID
     */
    @Transactional(readOnly = true)
    fun getSubmission(submissionId: String): AuthoritySubmissionDto {
        val submission = submissionRepository.findById(submissionId)
            .orElseThrow { IllegalArgumentException("Submission not found: $submissionId") }
        return toDto(submission)
    }

    /**
     * Get submissions for a batch
     */
    @Transactional(readOnly = true)
    fun getSubmissionsForBatch(batchId: String): List<AuthoritySubmissionDto> {
        return submissionRepository.findByBatch_Id(batchId).map { toDto(it) }
    }

    /**
     * Get submission history for user with filters
     */
    @Transactional(readOnly = true)
    fun getSubmissionHistory(
        userId: String,
        batchId: String? = null,
        authorityCode: String? = null,
        status: SubmissionStatus? = null,
        page: Int = 0,
        size: Int = 20
    ): Page<AuthoritySubmissionDto> {
        val pageable = PageRequest.of(page, size, Sort.by("submittedAt").descending())
        return submissionRepository.findByFilters(userId, batchId, authorityCode, status, pageable)
            .map { toDto(it) }
    }

    /**
     * Get all submissions for a user
     */
    @Transactional(readOnly = true)
    fun getSubmissionsByUser(userId: String, pageable: Pageable): Page<AuthoritySubmissionDto> {
        return submissionRepository.findBySubmittedBy(userId, pageable).map { toDto(it) }
    }

    /**
     * Get submission statistics for user
     */
    @Transactional(readOnly = true)
    fun getSubmissionStats(userId: String): SubmissionStatsDto {
        val statusCounts = submissionRepository.countByStatusForUser(userId)
            .associate { 
                val status = it[0] as SubmissionStatus
                val count = (it[1] as Long).toInt()
                status.name to count
            }

        val authorityCounts = submissionRepository.countByAuthorityForUser(userId)
            .associate {
                val authority = it[0] as String
                val count = (it[1] as Long).toInt()
                authority to count
            }

        val totalSubmissions = statusCounts.values.sum()
        val pendingCount = statusCounts.getOrDefault(SubmissionStatus.SUBMITTED.name, 0) +
                statusCounts.getOrDefault(SubmissionStatus.ACKNOWLEDGED.name, 0) +
                statusCounts.getOrDefault(SubmissionStatus.UNDER_REVIEW.name, 0)
        val approvedCount = statusCounts.getOrDefault(SubmissionStatus.APPROVED.name, 0) +
                statusCounts.getOrDefault(SubmissionStatus.CONDITIONALLY_APPROVED.name, 0)
        val rejectedCount = statusCounts.getOrDefault(SubmissionStatus.REJECTED.name, 0)

        return SubmissionStatsDto(
            totalSubmissions = totalSubmissions,
            pendingSubmissions = pendingCount,
            approvedSubmissions = approvedCount,
            rejectedSubmissions = rejectedCount,
            statusBreakdown = statusCounts,
            authorityBreakdown = authorityCounts
        )
    }

    /**
     * Get available authorities
     */
    fun getAvailableAuthorities(): List<AuthorityInfoDto> {
        return AuthorityCode.entries.map {
            AuthorityInfoDto(
                code = it.name,
                name = it.displayName,
                countryCode = it.countryCode
            )
        }
    }

    /**
     * Check if batch has pending submissions
     */
    @Transactional(readOnly = true)
    fun hasPendingSubmissions(batchId: String): Boolean {
        val pendingStatuses = listOf(
            SubmissionStatus.SUBMITTED,
            SubmissionStatus.ACKNOWLEDGED,
            SubmissionStatus.UNDER_REVIEW,
            SubmissionStatus.ADDITIONAL_INFO_REQUIRED
        )
        return submissionRepository.findByBatch_Id(batchId)
            .any { it.status in pendingStatuses }
    }

    private fun generateSubmissionReference(batch: EudrBatch, authorityCode: String): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(8)
        return "SUB-${batch.batchCode.takeLast(6)}-${authorityCode.take(4)}-$timestamp"
    }

    private fun calculateHash(data: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(data)
            .fold("") { str, byte -> str + "%02x".format(byte) }
    }

    private fun toDto(submission: AuthoritySubmission): AuthoritySubmissionDto {
        return AuthoritySubmissionDto(
            id = submission.id,
            batchId = submission.batch.id,
            batchCode = submission.batch.batchCode,
            authorityCode = submission.authorityCode,
            authorityName = submission.authorityName,
            submittedBy = submission.submittedBy,
            reportFilename = submission.reportFilename,
            status = submission.status.name,
            submissionReference = submission.submissionReference,
            authorityReference = submission.authorityReference,
            notes = submission.notes,
            authorityFeedback = submission.authorityFeedback,
            hederaTransactionId = submission.hederaTransactionId,
            submittedAt = submission.submittedAt,
            lastStatusUpdate = submission.lastStatusUpdate,
            acknowledgedAt = submission.acknowledgedAt,
            approvedAt = submission.approvedAt,
            rejectedAt = submission.rejectedAt
        )
    }
}

// DTOs
data class AuthoritySubmissionDto(
    val id: String,
    val batchId: String,
    val batchCode: String,
    val authorityCode: String,
    val authorityName: String?,
    val submittedBy: String,
    val reportFilename: String,
    val status: String,
    val submissionReference: String?,
    val authorityReference: String?,
    val notes: String?,
    val authorityFeedback: String?,
    val hederaTransactionId: String?,
    val submittedAt: LocalDateTime,
    val lastStatusUpdate: LocalDateTime?,
    val acknowledgedAt: LocalDateTime?,
    val approvedAt: LocalDateTime?,
    val rejectedAt: LocalDateTime?
)

data class SubmissionStatsDto(
    val totalSubmissions: Int,
    val pendingSubmissions: Int,
    val approvedSubmissions: Int,
    val rejectedSubmissions: Int,
    val statusBreakdown: Map<String, Int>,
    val authorityBreakdown: Map<String, Int>
)

data class AuthorityInfoDto(
    val code: String,
    val name: String,
    val countryCode: String
)
