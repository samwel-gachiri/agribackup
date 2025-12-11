package com.agriconnect.farmersportalapis.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

/**
 * Google Earth Engine Configuration
 * 
 * Configures authentication and HTTP client for accessing Google Earth Engine API
 * to retrieve Sentinel-2 and Landsat imagery for deforestation analysis.
 * 
 * Setup Instructions:
 * 1. Create Google Cloud Project: https://console.cloud.google.com
 * 2. Enable Earth Engine API
 * 3. Create Service Account with Earth Engine permissions
 * 4. Download JSON key file
 * 5. Set path in application.yml: earth-engine.credentials-path
 */
@Configuration
class EarthEngineConfig {

    private val logger = LoggerFactory.getLogger(EarthEngineConfig::class.java)

    @Value("\${earth-engine.credentials-path:}")
    private lateinit var credentialsPath: String

    @Value("\${earth-engine.project-id:}")
    private lateinit var projectId: String

    @Value("\${earth-engine.enabled:false}")
    private var earthEngineEnabled: Boolean = false

    @Value("\${earth-engine.api.base-url:https://earthengine.googleapis.com}")
    private lateinit var earthEngineApiBaseUrl: String

    /**
     * Creates Google credentials from service account JSON file
     */
    @Bean
    fun googleCredentials(): GoogleCredentials? {
        return try {
            if (!earthEngineEnabled) {
                logger.info("Earth Engine is disabled. Set earth-engine.enabled=true to enable.")
                return null
            }

            if (credentialsPath.isBlank()) {
                logger.warn("Earth Engine credentials path not configured. Set earth-engine.credentials-path in application.yml")
                return null
            }

            logger.info("Loading Earth Engine credentials from: $credentialsPath")
            
            val credentials = ServiceAccountCredentials.fromStream(
                FileInputStream(credentialsPath)
            ).createScoped(listOf(
                "https://www.googleapis.com/auth/earthengine",
                "https://www.googleapis.com/auth/cloud-platform"
            ))

            logger.info("Successfully loaded Earth Engine credentials for project: $projectId")
            credentials
        } catch (e: Exception) {
            logger.error("Failed to load Earth Engine credentials", e)
            null
        }
    }

    /**
     * HTTP client configured for Earth Engine API calls
     */
    @Bean
    fun earthEngineHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Configuration bean for Earth Engine parameters
     */
    @Bean
    fun earthEngineParameters(): EarthEngineParameters {
        return EarthEngineParameters(
            enabled = earthEngineEnabled,
            projectId = projectId,
            apiBaseUrl = earthEngineApiBaseUrl
        )
    }
}

/**
 * Earth Engine configuration parameters
 */
data class EarthEngineParameters(
    val enabled: Boolean,
    val projectId: String,
    val apiBaseUrl: String
)
