//package com.agriconnect.farmersportalapis.application.common
//
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.cancel
//import kotlinx.coroutines.launch
//import org.java_websocket.client.WebSocketClient
//import org.java_websocket.handshake.ServerHandshake
//import java.net.URI
//import java.nio.ByteBuffer
//import java.util.*
//import java.util.concurrent.LinkedBlockingQueue
//
//class ElevenLabsWebSocketClient(
//    voiceId: String,
//    private val apiKey: String,
//    private val onAudioChunk: (ByteArray) -> Unit
//) : WebSocketClient(URI("wss://api.elevenlabs.io/v1/text-to-speech/$voiceId/stream-input?model_id=eleven_monolingual_v2&output_format=pcm_24000")) {
//
//    // the text given for tts
//    private val textQueue = LinkedBlockingQueue<String>()
//    private var isRunning = true
//    private val scope = CoroutineScope(Dispatchers.IO)
//
//    init {
//        connect()
//    }
//
//    override fun onOpen(handshakedata: ServerHandshake?) {
//        // Send initial configuration
//        val config = """
//            {
//                "text": " ",
//                "voice_settings": {
//                    "stability": 0.3,
//                    "similarity_boost": 0.9,
//                    "speed": 1.1
//                },
//                "xi_api_key": "$apiKey"
//            }
//        """.trimIndent()
//        send(config)
//
//        // Start text sender coroutine
//        scope.launch {
//            while (isRunning) {
//                try {
//                    val text = textQueue.take()
//                    if (text.isEmpty()) {
//                        // End of stream signal
//                        send("""{"text": ""}""")
//                        break
//                    }
//                    send("""{"text": "$text"}""")
//                } catch (e: InterruptedException) {
//                    break
//                } catch (e: Exception) {
//                    onError(e)
//                }
//            }
//        }
//    }
//
//    override fun onMessage(message: String?) {
//        message?.let {
//            try {
//                val json = jacksonObjectMapper().readTree(it)
//                if (json.has("audio")) {
//                    val audioChunk = Base64.getDecoder().decode(json["audio"].asText())
//                    onAudioChunk(audioChunk)
//                }
//            } catch (e: Exception) {
//                onError(e)
//            }
//        }
//    }
//
//    override fun onMessage(bytes: ByteBuffer?) {
//        // Handle binary messages if needed
//    }
//
//    override fun onClose(code: Int, reason: String?, remote: Boolean) {
//        isRunning = false
//        scope.cancel()
//    }
//
//    override fun onError(ex: Exception?) {
//        ex?.let { onError(it) }
//    }
//
//    fun sendText(text: String) {
//        textQueue.put(text)
//    }
//
//    fun closeConnection() {
//        isRunning = false
//        close()
//        scope.cancel()
//    }
//}