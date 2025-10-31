package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.hedera.HederaAccountCredentials
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface HederaAccountCredentialsRepository : JpaRepository<HederaAccountCredentials, String> {
    
    fun findByUserId(userId: String): Optional<HederaAccountCredentials>
    
    fun findByHederaAccountId(hederaAccountId: String): Optional<HederaAccountCredentials>
    
    fun findByEntityTypeAndEntityId(entityType: String, entityId: String): Optional<HederaAccountCredentials>
    
    fun findByEntityType(entityType: String): List<HederaAccountCredentials>
    
    fun findByIsActive(isActive: Boolean): List<HederaAccountCredentials>
    
    fun existsByHederaAccountId(hederaAccountId: String): Boolean
}
