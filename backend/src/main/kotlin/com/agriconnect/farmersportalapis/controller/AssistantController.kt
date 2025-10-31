package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.ChatRequest
import com.agriconnect.farmersportalapis.service.common.AiService
import com.agriconnect.farmersportalapis.domain.common.model.ChatMessage
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.HtmlUtils
import java.security.Principal


@RestController
@RequestMapping("/api/assistant")
@Tag(name="AI Assistant", description = "This section is used to manage the AI assistant")
class AssistantController(
    private val aiService: AiService,
    private val simpMessagingTemplate: SimpMessagingTemplate
) {
//    private val elevenLabsService: ElevenLabsStreamingService,
    val logger = LoggerFactory.getLogger(AssistantController::class.java)

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): String? {
        // Include user section in the prompt for the AI to use
        val prompt = """
            userId=${request.userId}
            userSection=${request.userSection ?: "farmer"}
            prompt=${request.question}
        """.trimIndent()
        return aiService.getChatCompletion(prompt, request.userId)
    }

//    @MessageMapping("/message")
//    @SendTo("/topic/messages")
//    @Throws(InterruptedException::class)
//    fun getMessage(message: Message): ResponseMessage? {
//        Thread.sleep(1000)
////        notificationService.sendGlobalNotification()
//        return ResponseMessage(HtmlUtils.htmlEscape(message.messageContent!!))
//    }

    @MessageMapping("/private-message")
    @SendToUser("/topic/private-messages")
    @Throws(InterruptedException::class)
    fun getPrivateMessage(
        message: Message,
        principal: Principal
    ): ResponseMessage? {
        Thread.sleep(1000)
//        notificationService.sendPrivateNotification(principal.getName())
        return ResponseMessage(
            HtmlUtils.htmlEscape(
                ("Sending private message to user " + principal.name + ": "
                        + message.messageContent!!)
            )
        )
    }

    @MessageMapping("/chat.sendPrompt") // message mapping is like postmapping in that it handles message from the user
    @SendToUser("/ai/chat") // sends a message privately to a specific user
    fun sendMessage(
        @Payload request: ChatMessage,
        principal: Principal
    ): ChatMessage {
        val resMsg = ChatMessage()
        if (request.sender.isNullOrEmpty()) {
            resMsg.content = "Sender is not given"
        }
        if (request.content.isNullOrEmpty()) {
            resMsg.content = "content is not given"
        }
        val prompt = """
            userId=${request.sender}
            userSection=${request.userSection ?: "farmer"}
            prompt=${request.content}
        """.trimIndent()
//        // Get the text response from AI
        val textResponse = aiService.getChatCompletion(
            promptText = prompt,
            userId = request.sender!!,
        )
//        val textResponse = "Hello world"

        // If client requested audio, start streaming
//        if (chatMessage.responseType == "audio") {
//            // Start a new streaming session if not exists
//            if (!elevenLabsService.hasActiveSession(chatMessage.sender!!)) {
//                // You'll need to implement a way to get the WebSocketSession
//                val userSession = getWebSocketSessionForUser(chatMessage.sender!!)
//                elevenLabsService.startStreamingSession(chatMessage.sender!!, userSession)
//            }
//
//            // Send text to ElevenLabs for streaming
//            if (textResponse != null) {
//                elevenLabsService.sendTextToStream(chatMessage.sender!!, textResponse)
//            }
//
//            // For the regular chat channel, just send a placeholder
//            resMsg.content = "[Audio response is being streamed]"
//        }
//        else {
            // Regular text response
            resMsg.content = textResponse
//        }

        return resMsg
    }

    @SendToUser("/ai/chat/request-location") // sends a message privately to a specific user
    fun executeLocationUpdate(
        principal: Principal
    ) {
        logger.info("Executing location update for user: ${principal.name}")
    }
//
//    @SendToUser("/ai/chat/request-location") // sends a message privately to a specific user
//    fun executeLocationUpdate(
//        principal: Principal
//    ) {
//        logger.info("Executing location update for user: ${principal.name}")
//    }

}

class Message {
    var messageContent: String? = null
}
class ResponseMessage {
    var content: String? = null

    constructor()

    constructor(content: String?) {
        this.content = content
    }
}