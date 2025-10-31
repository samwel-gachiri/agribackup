package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.eudr.AuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface AuditLogRepository : JpaRepository<AuditLog, String> {

    fun findByEntityId(entityId: String): List<AuditLog>

    fun findByEntityType(entityType: String): List<AuditLog>

    fun findByEntityIdAndEntityType(entityId: String, entityType: String): List<AuditLog>

    fun findByActorId(actorId: String): List<AuditLog>

    fun findByAction(action: String): List<AuditLog>

    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startDate AND :endDate")
    fun findByTimestampBetween(
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<AuditLog>

    @Query("SELECT a FROM AuditLog a WHERE a.entityId = :entityId AND a.entityType = :entityType AND a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    fun findByEntityIdAndEntityTypeAndTimestampBetween(
        @Param("entityId") entityId: String,
        @Param("entityType") entityType: String,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): List<AuditLog>

    @Query("SELECT a FROM AuditLog a WHERE a.hederaTransactionId IS NOT NULL")
    fun findWithHederaTransactionId(): List<AuditLog>
}