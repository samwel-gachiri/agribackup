package com.agriconnect.farmersportalapis.application.tools

import com.agriconnect.farmersportalapis.application.dtos.FeatureRequestCreateDto
import com.agriconnect.farmersportalapis.service.common.FeatureRequestService
import com.agriconnect.farmersportalapis.domain.common.enums.RequestType
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Component

@Component
class FeatureRequestTool(
    private val featureRequestService: FeatureRequestService
) {

    @Tool(
        name = "create_feature_request",
        description = """
            Creates a new feature request or bug report when a user reports an issue or suggests an improvement.
            Returns the created request ID and confirmation message.
        """
    )
    fun createFeatureRequest(
        userId: String,
        userSection: String,
        message: String,
        isBug: Boolean = false,
        originalPrompt: String
    ): String {
        val requestType = if (isBug) RequestType.BUG else RequestType.FEATURE
        val dto = FeatureRequestCreateDto(
            requestType = requestType,
            message = message,
            userId = userId,
            userSection = userSection,
            aiGenerated = true,
            originalPrompt = originalPrompt
        )
        
        val createdRequest = featureRequestService.createFeatureRequest(dto)
        
        return """
            Request #${createdRequest.id} created successfully.
            Type: ${createdRequest.requestType}
            Status: ${createdRequest.status}
            Message: "${createdRequest.message}"
        """.trimIndent()
    }
}