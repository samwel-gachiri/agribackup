package com.agriconnect.farmersportalapis.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * Async configuration for non-blocking Hedera operations
 * 
 * This configuration creates a dedicated thread pool for Hedera blockchain
 * recording operations, allowing the main application to respond quickly
 * while blockchain operations happen in the background.
 */
@Configuration
@EnableAsync
@EnableScheduling
class AsyncConfiguration {

    /**
     * Task executor for Hedera operations
     * 
     * Configuration:
     * - Core pool size: 5 threads
     * - Max pool size: 10 threads  
     * - Queue capacity: 100 operations
     * - Thread name prefix for easy debugging
     */
    @Bean(name = ["hederaTaskExecutor"])
    fun hederaTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("hedera-async-")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(60)
        executor.initialize()
        return executor
    }

    /**
     * General purpose async executor for other background tasks
     */
    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 3
        executor.maxPoolSize = 8
        executor.queueCapacity = 50
        executor.setThreadNamePrefix("async-")
        executor.setWaitForTasksToCompleteOnShutdown(true)
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
