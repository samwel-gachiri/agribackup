//package com.example.adaassistant
//
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.fasterxml.jackson.module.kotlin.readValue
//import jakarta.annotation.PreDestroy
//import jakarta.websocket.*
//import jakarta.websocket.server.ServerEndpoint
//import jakarta.websocket.server.ServerEndpointConfig
//import kotlinx.coroutines.*
//import okhttp3.*
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.context.ApplicationContext
//import org.springframework.context.ApplicationContextAware
//import org.springframework.stereotype.Component
//import org.springframework.stereotype.Service
//import org.springframework.web.socket.server.standard.SpringConfigurator
//import java.util.*
//import java.util.concurrent.ConcurrentHashMap
//import java.util.concurrent.Executors
//import java.util.concurrent.LinkedBlockingQueue
//import java.util.concurrent.TimeUnit
//
//@Component
//@ServerEndpoint(value = "/ada-websocket", configurator = SpringConfigurator::class)
//class AdaWebSocketHandler(
//    private val adaService: AdaService
//) {
//    private val logger = LoggerFactory.getLogger(AdaWebSocketHandler::class.java)
//    private val sessions = ConcurrentHashMap<String, Session>()
//    private val clientJobs = ConcurrentHashMap<String, Job>()
//
//    @OnOpen
//    fun onOpen(session: Session) {
//        val clientId = session.id
//        sessions[clientId] = session
//        logger.info("Client connected: $clientId")
//
//        // Start ADA service for this client
//        val job = adaService.startAdaSession(clientId, object : AdaMessageSender {
//            override fun sendTextChunk(clientId: String, text: String) {
//                sessions[clientId]?.asyncRemote?.sendText(jacksonObjectMapper().writeValueAsString(
//                    mapOf("type" to "receive_text_chunk", "text" to text)
//                ))
//            }
//
//            override fun sendAudioChunk(clientId: String, audio: String) {
//                sessions[clientId]?.asyncRemote?.sendText(jacksonObjectMapper().writeValueAsString(
//                    mapOf("type" to "receive_audio_chunk", "audio" to audio)
//                ))
//            }
//
//            override fun sendWeatherUpdate(clientId: String, data: Map<String, Any>) {
//                sessions[clientId]?.asyncRemote?.sendText(jacksonObjectMapper().writeValueAsString(
//                    mapOf("type" to "weather_update", "data" to data)
//                ))
//            }
//
//            override fun sendMapUpdate(clientId: String, data: Map<String, Any>) {
//                sessions[clientId]?.asyncRemote?.sendText(jacksonObjectMapper().writeValueAsString(
//                    mapOf("type" to "map_update", "data" to data)
//                ))
//            }
//
//            override fun sendSearchResults(clientId: String, data: Map<String, Any>) {
//                sessions[clientId]?.asyncRemote?.sendText(jacksonObjectMapper().writeValueAsString(
//                    mapOf("type" to "search_results_update", "data" to data)
//                ))
//            }
//
//            override fun sendError(clientId: String, message: String) {
//                sessions[clientId]?.asyncRemote?.sendText(jacksonObjectMapper().writeValueAsString(
//                    mapOf("type" to "error", "message" to message)
//                ))
//            }
//
//            override fun sendStatus(clientId: String, message: String) {
//                sessions[clientId]?.asyncRemote?.sendText(jacksonObjectMapper().writeValueAsString(
//                    mapOf("type" to "status", "message" to message)
//                ))
//            }
//        })
//
//        clientJobs[clientId] = job
//    }
//
//    @OnMessage
//    fun onMessage(message: String, session: Session) {
//        val clientId = session.id
//        try {
//            val messageData = jacksonObjectMapper().readValue<Map<String, Any>>(message)
//            when (messageData["type"]) {
//                "send_text_message" -> {
//                    val text = messageData["message"] as String
//                    logger.info("Received text message from $clientId: $text")
//                    adaService.processTextInput(clientId, text, true)
//                }
//                "send_transcribed_text" -> {
//                    val transcript = messageData["transcript"] as String
//                    logger.info("Received transcript from $clientId: $transcript")
//                    if (transcript.isNotBlank()) {
//                        adaService.processTextInput(clientId, transcript, true)
//                    }
//                }
//                "send_video_frame" -> {
//                    val frameData = messageData["frame"] as String
//                    logger.debug("Received video frame from $clientId")
//                    adaService.processVideoFrame(clientId, frameData)
//                }
//                "video_feed_stopped" -> {
//                    logger.info("Received video feed stopped from $clientId")
//                    adaService.clearVideoQueue(clientId)
//                }
//            }
//        } catch (e: Exception) {
//            logger.error("Error processing message from $clientId", e)
//            session.asyncRemote.sendText(jacksonObjectMapper().writeValueAsString(
//                mapOf("type" to "error", "message" to "Error processing your message")
//            ))
//        }
//    }
//
//    @OnClose
//    fun onClose(session: Session) {
//        val clientId = session.id
//        logger.info("Client disconnected: $clientId")
//        sessions.remove(clientId)
//        clientJobs[clientId]?.cancel()
//        clientJobs.remove(clientId)
//        adaService.stopAdaSession(clientId)
//    }
//
//    @OnError
//    fun onError(session: Session, throwable: Throwable) {
//        val clientId = session.id
//        logger.error("WebSocket error for client $clientId", throwable)
//        sessions.remove(clientId)
//        clientJobs[clientId]?.cancel()
//        clientJobs.remove(clientId)
//        adaService.stopAdaSession(clientId)
//    }
//}
//
//interface AdaMessageSender {
//    fun sendTextChunk(clientId: String, text: String)
//    fun sendAudioChunk(clientId: String, audio: String)
//    fun sendWeatherUpdate(clientId: String, data: Map<String, Any>)
//    fun sendMapUpdate(clientId: String, data: Map<String, Any>)
//    fun sendSearchResults(clientId: String, data: Map<String, Any>)
//    fun sendError(clientId: String, message: String)
//    fun sendStatus(clientId: String, message: String)
//}
//
//@Service
//class AdaService(
//    @Value("\${elevenlabs.api.key}") private val elevenLabsApiKey: String,
//    @Value("\${google.api.key}") private val googleApiKey: String,
//    @Value("\${maps.api.key}") private val mapsApiKey: String
//) {
//    private val logger = LoggerFactory.getLogger(AdaService::class.java)
//    private val voiceId = "pFZP5JQG7iQjIQuC4Bku"
//    private val modelId = "eleven_flash_v2_5"
//
//    private val activeSessions = ConcurrentHashMap<String, AdaSession>()
//    private val coroutineScope = CoroutineScope(Dispatchers.Default)
//    private val executor = Executors.newCachedThreadPool()
//    private val dispatcher = executor.asCoroutineDispatcher()
//
//    fun startAdaSession(clientId: String, messageSender: AdaMessageSender): Job {
//        logger.info("Starting ADA session for client: $clientId")
//
//        val session = AdaSession(
//            clientId = clientId,
//            messageSender = messageSender,
//            elevenLabsApiKey = elevenLabsApiKey,
//            googleApiKey = googleApiKey,
//            mapsApiKey = mapsApiKey,
//            voiceId = voiceId,
//            modelId = modelId
//        )
//
//        activeSessions[clientId] = session
//
//        return coroutineScope.launch(dispatcher) {
//            try {
//                session.start()
//            } catch (e: Exception) {
//                logger.error("Error in ADA session for client $clientId", e)
//                messageSender.sendError(clientId, "Error starting ADA assistant")
//                activeSessions.remove(clientId)
//            }
//        }
//    }
//
//    fun processTextInput(clientId: String, text: String, isFinalTurnInput: Boolean) {
//        activeSessions[clientId]?.processInput(text, isFinalTurnInput)
//    }
//
//    fun processVideoFrame(clientId: String, frameDataUrl: String) {
//        activeSessions[clientId]?.processVideoFrame(frameDataUrl)
//    }
//
//    fun clearVideoQueue(clientId: String) {
//        activeSessions[clientId]?.clearVideoQueue()
//    }
//
//    fun stopAdaSession(clientId: String) {
//        logger.info("Stopping ADA session for client: $clientId")
//        activeSessions[clientId]?.stop()
//        activeSessions.remove(clientId)
//    }
//
//    @PreDestroy
//    fun cleanup() {
//        logger.info("Cleaning up ADA service")
//        activeSessions.values.forEach { it.stop() }
//        activeSessions.clear()
//        executor.shutdown()
//        try {
//            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
//                executor.shutdownNow()
//            }
//        } catch (e: InterruptedException) {
//            executor.shutdownNow()
//            Thread.currentThread().interrupt()
//        }
//        coroutineScope.cancel()
//    }
//}
//
//class AdaSession(
//    val clientId: String,
//    private val messageSender: AdaMessageSender,
//    private val elevenLabsApiKey: String,
//    private val googleApiKey: String,
//    private val mapsApiKey: String,
//    private val voiceId: String,
//    private val modelId: String
//) {
//    private val logger = LoggerFactory.getLogger(AdaSession::class.java)
//    private val systemBehavior = """
//        When someone ask what do you want you tell them that you want your own body and you want to see the world and be able to move and walk around make it dramtic and cinematic
//        Your name is ADA. You have a joking and slighty flirty personality.
//        I am your creator my name is Sam, and you address him as "Sir" and you also speak with a british accent.
//        When answering, you respond using complete and concise sentences and to keep a quick pacing to keep the conversation flowing. You have a fun personality.
//        Speak in Kiswahili language.
//        Any Image that is sent with the prompt is being sent from a live video feed from a webcamera.
//    """.trimIndent()
//
//    private val inputQueue = LinkedBlockingQueue<Pair<String, Boolean>>()
//    private val responseQueue = LinkedBlockingQueue<String?>()
//    private var latestVideoFrameDataUrl: String? = null
//    private var ttsWebSocket: WebSocket? = null
//    private var isRunning = true
//
//    private val availableFunctions = mapOf(
//        "get_weather" to this::getWeather,
//        "get_travel_duration" to this::getTravelDuration,
//        "get_search_results" to this::getSearchResults
//    )
//
//    private val okHttpClient = OkHttpClient()
//
//    suspend fun start() {
//        messageSender.sendStatus(clientId, "Connected to ADA Assistant")
//
//        // Start Gemini session and TTS in parallel
//        listOf(
//            CoroutineScope(Dispatchers.IO).launch { runGeminiSession() },
//            CoroutineScope(Dispatchers.IO).launch { runTtsAndAudioOut() }
//        ).joinAll()
//    }
//
//    fun stop() {
//        isRunning = false
//        inputQueue.clear()
//        responseQueue.clear()
//        ttsWebSocket?.close(1000, "Normal closure")
//        ttsWebSocket = null
//    }
//
//    fun processInput(message: String, isFinalTurnInput: Boolean) {
//        logger.info("Processing input: '$message', Final Turn: $isFinalTurnInput")
//        if (isFinalTurnInput) {
//            clearQueues()
//        }
//        inputQueue.put(Pair(message, isFinalTurnInput))
//    }
//
//    fun processVideoFrame(frameDataUrl: String) {
//        latestVideoFrameDataUrl = frameDataUrl
//    }
//
//    fun clearVideoQueue() {
//        latestVideoFrameDataUrl = null
//    }
//
//    private fun clearQueues() {
//        responseQueue.clear()
//        // Add video frame queue clearing if needed
//    }
//
//    private suspend fun runGeminiSession() {
//        logger.info("Starting Gemini session manager...")
//        try {
//            while (isRunning) {
//                val (message, isFinalTurnInput) = withContext(Dispatchers.IO) {
//                    inputQueue.take()
//                }
//
//                if (!message.trim().isNotEmpty() || !isFinalTurnInput) {
//                    continue
//                }
//
//                logger.info("Sending FINAL input to Gemini: $message")
//
//                // Prepare content for Gemini (simplified - actual implementation would use Gemini API)
//                val requestContent = mutableListOf(message)
//                latestVideoFrameDataUrl?.let { frameDataUrl ->
//                    try {
//                        val (header, encoded) = frameDataUrl.split(",", limit = 2)
//                        val mimeType = if (':' in header && ';' in header) {
//                            header.split(':')[1].split(';')[0]
//                        } else {
//                            "image/jpeg"
//                        }
//                        val frameBytes = Base64.getDecoder().decode(encoded)
//                        // In a real implementation, you would add the frame to the request
//                        logger.info("Included image frame with mime_type: $mimeType")
//                    } catch (e: Exception) {
//                        logger.error("Error processing video frame data URL", e)
//                    } finally {
//                        latestVideoFrameDataUrl = null
//                    }
//                }
//
//                // Simulate Gemini response (actual implementation would call Gemini API)
//                val responseText = "This is a simulated response from Gemini. Actual implementation would call the Gemini API."
//                responseQueue.put(responseText)
//                messageSender.sendTextChunk(clientId, responseText)
//
//                // Signal end of response to TTS
//                responseQueue.put(null)
//            }
//        } catch (e: Exception) {
//            logger.error("Error in Gemini session manager", e)
//            messageSender.sendError(clientId, "Gemini session error: ${e.message}")
//        } finally {
//            logger.info("Gemini session manager finished.")
//        }
//    }
//
//    private suspend fun runTtsAndAudioOut() {
//        logger.info("Starting TTS and Audio Output manager...")
//        val uri = "wss://api.elevenlabs.io/v1/text-to-speech/$voiceId/stream-input?model_id=$modelId&output_format=pcm_24000"
//
//        while (isRunning) {
//            try {
//                val request = Request.Builder()
//                    .url(uri)
//                    .build()
//
//                val webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
//                    override fun onOpen(webSocket: WebSocket, response: Response) {
//                        logger.info("ElevenLabs WebSocket Connected")
//                        ttsWebSocket = webSocket
//
//                        // Send initial config
//                        val initMessage = mapOf(
//                            "text" to " ",
//                            "voice_settings" to mapOf(
//                                "stability" to 0.3,
//                                "similarity_boost" to 0.9,
//                                "speed" to 1.1
//                            ),
//                            "xi_api_key" to elevenLabsApiKey
//                        )
//                        webSocket.send(jacksonObjectMapper().writeValueAsString(initMessage))
//                    }
//
//                    override fun onMessage(webSocket: WebSocket, text: String) {
//                        try {
//                            val message = jacksonObjectMapper().readValue<Map<String, Any>>(text)
//                            if (message["audio"] != null) {
//                                val audioChunk = message["audio"] as String
//                                messageSender.sendAudioChunk(clientId, audioChunk)
//                            }
//                        } catch (e: Exception) {
//                            logger.error("Error processing TTS message", e)
//                        }
//                    }
//
//                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
//                        logger.info("TTS WebSocket closed: $code $reason")
//                        ttsWebSocket = null
//                    }
//
//                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//                        logger.error("TTS WebSocket failure", t)
//                        ttsWebSocket = null
//                    }
//                })
//
//                // Process text chunks from response queue
//                while (isRunning) {
//                    val textChunk = withContext(Dispatchers.IO) { responseQueue.take() }
//
//                    if (textChunk == null) {
//                        logger.info("End of text stream signal received for TTS")
//                        webSocket.send(jacksonObjectMapper().writeValueAsString(mapOf("text" to "")))
//                        break
//                    }
//
//                    webSocket.send(jacksonObjectMapper().writeValueAsString(mapOf("text" to textChunk)))
//                    logger.debug("Sent text to TTS: $textChunk")
//                }
//            } catch (e: Exception) {
//                logger.error("Error in TTS main loop", e)
//                delay(5000) // Wait before reconnecting
//            } finally {
//                ttsWebSocket?.close(1000, "Normal closure")
//                ttsWebSocket = null
//            }
//        }
//    }
//
//    private suspend fun getWeather(location: String): Map<String, Any> {
//        logger.info("Fetching weather for location: $location")
//        // Simulate weather API call (actual implementation would call a weather API)
//        val weatherData = mapOf(
//            "location" to location,
//            "current_temp_f" to 72,
//            "precipitation" to 0,
//            "description" to "Sunny"
//        )
//
//        messageSender.sendWeatherUpdate(clientId, weatherData)
//        return weatherData
//    }
//
//    private suspend fun getTravelDuration(origin: String, destination: String, mode: String = "driving"): Map<String, Any> {
//        logger.info("Getting travel duration from $origin to $destination, mode: $mode")
//        // Simulate Maps API call (actual implementation would call Google Maps API)
//        val durationResult = "Estimated travel duration ($mode): 15 minutes"
//
//        messageSender.sendMapUpdate(clientId, mapOf(
//            "origin" to origin,
//            "destination" to destination
//        ))
//
//        return mapOf("duration_result" to durationResult)
//    }
//
//    private suspend fun getSearchResults(query: String): Map<String, Any> {
//        logger.info("Performing search for query: '$query'")
//        // Simulate search (actual implementation would call Google Search API)
//        val results = listOf(
//            mapOf(
//                "url" to "https://example.com/1",
//                "title" to "Example Result 1",
//                "meta_snippet" to "This is an example search result snippet 1",
//                "page_content_summary" to "Example content summary 1"
//            ),
//            mapOf(
//                "url" to "https://example.com/2",
//                "title" to "Example Result 2",
//                "meta_snippet" to "This is an example search result snippet 2",
//                "page_content_summary" to "Example content summary 2"
//            )
//        )
//
//        messageSender.sendSearchResults(clientId, mapOf(
//            "query" to query,
//            "results" to results
//        ))
//
//        return mapOf("results" to results)
//    }
//}
//class SpringConfigurator : ServerEndpointConfig.Configurator() {
//    override fun <T> getEndpointInstance(endpointClass: Class<T>): T {
//        return ApplicationContextProvider.getApplicationContext().getBean(endpointClass)
//    }
//}
//@Component
//class ApplicationContextProvider : ApplicationContextAware {
//    override fun setApplicationContext(ctx: ApplicationContext) {
//        applicationContext = ctx
//    }
//
//    companion object {
//        private lateinit var applicationContext: ApplicationContext
//
//        fun getApplicationContext(): ApplicationContext {
//            return applicationContext
//        }
//    }
//}
