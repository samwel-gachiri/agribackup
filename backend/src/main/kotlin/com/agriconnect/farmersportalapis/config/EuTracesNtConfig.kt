package com.agriconnect.farmersportalapis.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

/**
 * Configuration properties for EU TRACES NT integration
 * 
 * TRACES NT (Trade Control and Expert System - New Technology) is the EU's official
 * platform for submitting Due Diligence Statements (DDS) under EUDR.
 */
@Configuration
@ConfigurationProperties(prefix = "traces-nt")
class EuTracesNtConfig {
    
    var enabled: Boolean = false
    var environment: String = "acceptance"
    var baseUrl: BaseUrl = BaseUrl()
    var apiVersion: Int = 2
    var auth: Auth = Auth()
    var timeout: Timeout = Timeout()
    
    class BaseUrl {
        var acceptance: String = "https://webgate.acceptance.ec.europa.eu/tracesnt-alpha"
        var production: String = "https://webgate.ec.europa.eu/tracesnt"
    }
    
    class Auth {
        var username: String = ""
        var authKey: String = ""
    }
    
    class Timeout {
        var connectMs: Int = 30000
        var readMs: Int = 60000
    }
    
    /**
     * Get the active base URL based on environment setting
     */
    fun getActiveBaseUrl(): String {
        return when (environment.lowercase()) {
            "production", "prod" -> baseUrl.production
            else -> baseUrl.acceptance
        }
    }
    
    /**
     * Check if credentials are configured
     */
    fun hasCredentials(): Boolean {
        return auth.username.isNotBlank() && auth.authKey.isNotBlank()
    }
    
    /**
     * RestTemplate configured for TRACES NT API calls
     */
    @Bean("tracesNtRestTemplate")
    fun tracesNtRestTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(timeout.connectMs)
            setReadTimeout(timeout.readMs)
        }
        return RestTemplate(factory)
    }
}
