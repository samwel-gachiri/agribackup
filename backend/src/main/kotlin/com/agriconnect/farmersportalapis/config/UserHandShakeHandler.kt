package com.agriconnect.farmersportalapis.config

import com.sun.security.auth.UserPrincipal
import org.slf4j.LoggerFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal
import java.util.*

class UserHandshakeHandler : DefaultHandshakeHandler() {
    private val LOG = LoggerFactory.getLogger(UserHandshakeHandler::class.java)
    override fun determineUser(
        request: ServerHttpRequest,
        wsHandler: WebSocketHandler,
        attributes: Map<String, Any>
    ): Principal? {
        // Get the 'userId' header from the request
//        val userId = request.headers["userId"]?.firstOrNull()
        val uri = request.uri
        val queryParams = uri.query?.split("&")
            ?.mapNotNull {
                val parts = it.split("=")
                if (parts.size == 2) parts[0] to parts[1] else null
            }?.toMap() ?: emptyMap()

        val userId = queryParams["userId"]

        if (userId.isNullOrEmpty()) {
            val randomId = UUID.randomUUID().toString()
            LOG.warn("No user ID provided in headers, generating random ID: {}", randomId)
            return UserPrincipal(randomId)
        }

        LOG.info("User with ID '{}' connected via WebSocket", userId)
        return UserPrincipal(userId)
    }
}