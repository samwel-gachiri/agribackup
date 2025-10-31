package com.agriconnect.farmersportalapis.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.net.HttpURLConnection
import java.time.Duration

@Configuration
class RestTemplateConfiguration {
    
    @Bean
    fun restTemplate(): RestTemplate {
        val factory = object : SimpleClientHttpRequestFactory() {
            override fun prepareConnection(connection: HttpURLConnection, httpMethod: String) {
                super.prepareConnection(connection, httpMethod)
                // Enable following redirects for POST requests
                connection.instanceFollowRedirects = true
            }
        }
        factory.setConnectTimeout(Duration.ofSeconds(30))
        factory.setReadTimeout(Duration.ofSeconds(60))
        
        return RestTemplate(factory)
    }
    
    @Bean("ipfsRestTemplate")
    fun ipfsRestTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(Duration.ofSeconds(60))
        factory.setReadTimeout(Duration.ofSeconds(120))
        
        return RestTemplate(factory)
    }
}