package com.agriconnect.farmersportalapis.config

import feign.Request
import feign.Retryer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class GlobalForestWatchClientConfiguration {

    @Bean
    fun feignRetryer(): Retryer {
        return Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 5)
    }

    @Bean
    fun feignOptions(): Request.Options {
        return Request.Options(
            10, TimeUnit.SECONDS, // connect timeout
            30, TimeUnit.SECONDS, // read timeout
            true // follow redirects
        )
    }
}