package com.agriconnect.farmersportalapis.application.tools

import com.agriconnect.farmersportalapis.service.common.impl.LocationService
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.messaging.simp.SimpMessagingTemplate

class LocationTool(
    private val locationService: LocationService,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {

    val logger = LoggerFactory.getLogger(LocationTool::class.java)

    @Tool(
        name = "getFarmerLocation",
        description = "Gets the farmer location from the database"
    )
    fun getFarmerLocation(userid: String): String {
        val location = locationService.getFarmerLocation(userid)
        return if (location.success) {
            """
            name: ${location.data?.customName}
            latitude: ${location.data?.latitude}
            longitude: ${location.data?.longitude}
        """.trimIndent()
        } else {
            """
                Failed to retrieve location.
                msg: ${location.msg}
            """.trimIndent()
        }
    }
    @Tool(
        name="requestLocation",
        description = "Use this when farmer location is not found in the database"
    )
    fun requestLocation(
        @ToolParam(description = "User id given to the model")
        userId: String
    ): String {
        logger.info("requesting location for user $userId")
        simpMessagingTemplate.convertAndSendToUser(userId, "ai/chat/request-location", "Please share your location.")
        return "location request sent to user $userId."
    }
}