package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.service.common.impl.FarmProduceService
import com.agriconnect.farmersportalapis.service.supplychain.FarmerService
import com.agriconnect.farmersportalapis.service.common.impl.LocationService
import com.agriconnect.farmersportalapis.application.util.CommonUtils
import com.agriconnect.farmersportalapis.application.util.DuckDuckGoSearchTool
import org.springframework.ai.anthropic.AnthropicChatModel
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Service


@Service
class AiService(
    private val chatModel: AnthropicChatModel,
    featureRequestService: FeatureRequestService,
    locationService: LocationService,
    farmerService: FarmerService,
    farmProduceService: FarmProduceService,
    simpMessagingTemplate: SimpMessagingTemplate
) {
    private val retryTemplate = RetryTemplate().apply {
        setRetryPolicy(SimpleRetryPolicy(5))  // Increased to 5 retries
        setBackOffPolicy(ExponentialBackOffPolicy().apply {
            initialInterval = 2000L  // Start with 2 seconds
            multiplier = 2.5        // Increased multiplier for more spacing
            maxInterval = 30000L    // Increased max interval to 30 seconds
        })
    }

    private val chatMemory = MessageWindowChatMemory
        .builder()
        .maxMessages(10) // Limit memory size to prevent excessive token usage
        .build()

    private val farmerChatClient = ChatClient
        .builder(chatModel)
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .defaultTools(
            CommonUtils(),
            DuckDuckGoSearchTool(),
        )
        .defaultSystem("""
            You are an agricultural expert assistant. Your responsibilities include:
            1. Answering agricultural questions
            2. Identifying when users are reporting issues or requesting features
            3. Automatically creating feature requests when appropriate
            
            Respond in the same language as the question.
            Keep responses concise and focused.
        """.trimIndent())
        .build()

    private val buyerChatClient = ChatClient
        .builder(chatModel)
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .defaultTools(
            CommonUtils(),
            DuckDuckGoSearchTool()
        )
        .defaultSystem("""
            You are a buyer assistant. Your responsibilities include:
            1. Answering buyer questions
            2. Identifying when users are reporting issues or requesting features
            3. Automatically creating feature requests when appropriate
            
            Respond in the same language as the question.
            Keep responses concise and focused.
        """.trimIndent())
        .build()

    fun getChatCompletion(promptText: String, userId: String): String? {
        return try {
            retryTemplate.execute<String, RuntimeException> {
                val userMessage = UserMessage(promptText)
                chatMemory.add(userId, userMessage)
                farmerChatClient
                    .prompt(promptText)
                    .call()
                    .content()
            }
        } catch (e: Exception) {
            // Fallback response in case of persistent failures
            "I apologize, but I'm experiencing high traffic at the moment. Please try again in a few moments."
        }
    }
}
