package com.agriconnect.farmersportalapis.config


import com.agriconnect.farmersportalapis.domain.common.enums.MessageType
import com.agriconnect.farmersportalapis.domain.common.model.ChatMessage
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent


@Component
@Slf4j
@RequiredArgsConstructor
class WebSocketEventListener(
    private val messagingTemplate: SimpMessageSendingOperations? = null
) {
    val logger = LoggerFactory.getLogger(WebSocketEventListener::class.java)
    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val username = headerAccessor.sessionAttributes!!["username"] as String?
        if (username != null) {
            logger.info("user disconnected: {}", username)
            val chatMessage = ChatMessage()
                chatMessage.type = MessageType.LEAVE
                chatMessage.sender = username
                chatMessage.content = ""
            messagingTemplate!!.convertAndSend("/topic/public", chatMessage)
        }
    }
}

