package com.agriconnect.farmersportalapis.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "hedera")
data class HederaConfiguration(
    var network: NetworkConfig = NetworkConfig(),
    var account: AccountConfig = AccountConfig(),
    var consensus: ConsensusConfig = ConsensusConfig(),
    var retry: RetryConfig = RetryConfig()
) {
    
    data class NetworkConfig(
        var type: String = "testnet",
        var endpoint: String = "https://testnet.mirrornode.hedera.com"
    )
    
    data class AccountConfig(
        var id: String = "",
        var privateKey: String = ""
    )
    
    data class ConsensusConfig(
        var topicId: String = ""
    )
    
    data class RetryConfig(
        var maxAttempts: Int = 3,
        var backoffDelay: Long = 1000,
        var maxBackoffDelay: Long = 10000
    )
}