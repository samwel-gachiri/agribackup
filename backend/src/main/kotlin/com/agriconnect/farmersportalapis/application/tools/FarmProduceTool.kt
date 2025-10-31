package com.agriconnect.farmersportalapis.application.tools

import com.agriconnect.farmersportalapis.application.dtos.ProduceSearchRequest
import com.agriconnect.farmersportalapis.application.dtos.SearchType
import com.agriconnect.farmersportalapis.service.common.impl.FarmProduceService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.messaging.simp.SimpMessagingTemplate

class FarmProduceTool(
    private val farmProduceService: FarmProduceService,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
    private val logger: Logger = LoggerFactory.getLogger(UserTool::class.java)
    @Tool(name="SearchForProduce", description = "Searches for produce and sends to frontend")
    fun searchForProduce(
        @ToolParam(description = "Produce to be searched")
        farmProduce: String,
        userLatitude: Double,
        userLongitude: Double,
        @ToolParam(description = "User id given to the model")
        userId: String
    ) {
        val produceSearchRequest = ProduceSearchRequest(
            searchTerm = farmProduce,
            searchType = SearchType.ALL,
            latitude = userLatitude,
            longitude = userLongitude
        )
        logger.info("Produce search triggered by AI agent")
        val produces = farmProduceService.searchProduce(produceSearchRequest)
        simpMessagingTemplate.convertAndSendToUser(userId, "ai/chat/display-farm-produce", produces)
    }
}