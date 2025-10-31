//package com.agriconnect.farmersportalapis.application.common
//
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.stereotype.Service
//import org.springframework.web.socket.BinaryMessage
//import org.springframework.web.socket.WebSocketSession
//import java.util.concurrent.ConcurrentHashMap
//
//@Service
//class ElevenLabsStreamingService(
//    @Value("\${elevenlabs.api.key}") private val apiKey: String,
//    @Value("\${elevenlabs.voice.id}") private val voiceId: String
//) {
//    private val activeSessions = ConcurrentHashMap<String, ElevenLabsWebSocketClient>()
//
//    fun startStreamingSession(userId: String, session: WebSocketSession) {
//        val client = ElevenLabsWebSocketClient(
//            voiceId = voiceId,
//            apiKey = apiKey
//        ) { audioChunk ->
//            try {
//                session.sendMessage(BinaryMessage(audioChunk))
//            } catch (e: Exception) {
//                // Handle error sending to client
//                stopStreamingSession(userId)
//            }
//        }
//
//        activeSessions[userId] = client
//    }
//
//    fun sendTextToStream(userId: String, text: String) {
//        activeSessions[userId]?.sendText(text)
//    }
//
//    fun stopStreamingSession(userId: String) {
//        activeSessions[userId]?.closeConnection()
//        activeSessions.remove(userId)
//    }
//
//    fun hasActiveSession(userId: String): Boolean {
//        return activeSessions.containsKey(userId)
//    }
//}