package com.agriconnect.farmersportalapis.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

@Configuration
@EnableAsync
@EnableScheduling
class AsyncConfiguration {
    
    @Bean(name = ["hederaTaskExecutor"])
    fun hederaTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2
        executor.maxPoolSize = 5
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("Hedera-")
        executor.initialize()
        return executor
    }
    
    @Bean(name = ["eudrTaskExecutor"])
    fun eudrTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 3
        executor.maxPoolSize = 10
        executor.queueCapacity = 200
        executor.setThreadNamePrefix("EUDR-")
        executor.initialize()
        return executor
    }
}