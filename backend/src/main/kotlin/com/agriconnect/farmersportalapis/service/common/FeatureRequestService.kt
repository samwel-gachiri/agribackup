package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.FeatureRequestCreateDto
import com.agriconnect.farmersportalapis.application.dtos.FeatureRequestFilterDto
import com.agriconnect.farmersportalapis.application.dtos.FeatureRequestResponseDto
import com.agriconnect.farmersportalapis.application.dtos.FeatureRequestUpdateDto
import com.agriconnect.farmersportalapis.domain.common.model.FeatureRequest
import com.agriconnect.farmersportalapis.infrastructure.repositories.FeatureRequestRepository
import com.agriconnect.farmersportalapis.service.common.impl.EmailService
import javassist.NotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class FeatureRequestService(
    private val featureRequestRepository: FeatureRequestRepository,
    private val emailService: EmailService
) {

    @Transactional
    open fun createFeatureRequest(dto: FeatureRequestCreateDto): FeatureRequestResponseDto {
        val featureRequest = FeatureRequest(
            userId = dto.userId,
            userSection = dto.userSection,
            requestType = dto.requestType,
            message = dto.message
        )

        val savedRequest = featureRequestRepository.save(featureRequest)
        // send notification email to admin/support about the new feedback/feature request
        try {
            val subject = "New Feedback Submitted - AgriBackup"
            val body = """
                <h3>New Feedback / Feature Request</h3>
                <p><strong>Type:</strong> ${'$'}{savedRequest.requestType}</p>
                <p><strong>Section:</strong> ${'$'}{savedRequest.userSection ?: "Unknown"}</p>
                <p><strong>User ID:</strong> ${'$'}{savedRequest.userId ?: "Unknown"}</p>
                <p><strong>Message:</strong></p>
                <p>${'$'}{savedRequest.message}</p>
                <p>Submitted at: ${'$'}{savedRequest.createdAt}</p>
            """.trimIndent()

            emailService.sendEmail("samgachiri2002@gmail.com", subject, body)
        } catch (e: Exception) {
            // Don't fail the feature request creation if email sending fails; log and continue
            // Using println to avoid adding logger dependency here; EmailService already logs failures.
            println("Failed to send feature request notification email: ${'$'}{e.message}")
        }
        return savedRequest.toResponseDto()
    }

    @Transactional(readOnly = true)
    open fun getFeatureRequestById(id: Long): FeatureRequestResponseDto {
        val featureRequest = featureRequestRepository.findById(id)
            .orElseThrow { NotFoundException("Feature request not found with id: $id") }
        return featureRequest.toResponseDto()
    }

    @Transactional(readOnly = true)
    open fun getAllFeatureRequests(filter: FeatureRequestFilterDto, pageable: Pageable): Page<FeatureRequestResponseDto> {
        return featureRequestRepository.findAllFiltered(
            status = filter.status,
            requestType = filter.requestType,
            userSection = filter.userSection,
            userId = filter.userId,
            pageable = pageable
        ).map { it.toResponseDto() }
    }

    @Transactional(readOnly = true)
    open fun getUserFeatureRequests(userId: Long, pageable: Pageable): Page<FeatureRequestResponseDto> {
        return featureRequestRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map { it.toResponseDto() }
    }

    @Transactional
    open fun updateFeatureRequest(id: Long, dto: FeatureRequestUpdateDto): FeatureRequestResponseDto {
        val featureRequest = featureRequestRepository.findById(id)
            .orElseThrow { NotFoundException("Feature request not found with id: $id") }
        
        featureRequest.status = dto.status
        dto.adminNotes?.let { featureRequest.adminNotes = it }
        
        val updatedRequest = featureRequestRepository.save(featureRequest)
        return updatedRequest.toResponseDto()
    }

    private fun FeatureRequest.toResponseDto(): FeatureRequestResponseDto {
        return FeatureRequestResponseDto(
            id = id,
            userId = userId,
            userSection = userSection,
            requestType = requestType,
            message = message,
            status = status,
            adminNotes = adminNotes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}