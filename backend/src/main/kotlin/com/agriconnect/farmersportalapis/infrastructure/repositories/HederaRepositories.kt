package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.hedera.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface HederaEntityRegistryRepository : JpaRepository<HederaEntityRegistry, String> {
    fun findByEntityTypeAndEntityId(entityType: String, entityId: String): HederaEntityRegistry?
    fun findByHederaAccountId(hederaAccountId: String): HederaEntityRegistry?
    fun findByEntityType(entityType: String): List<HederaEntityRegistry>
    fun findByIsActive(isActive: Boolean): List<HederaEntityRegistry>
}

@Repository
interface HederaTopicRegistryRepository : JpaRepository<HederaTopicRegistry, String> {
    fun findByHederaTopicId(hederaTopicId: String): HederaTopicRegistry?
    fun findByChannelType(channelType: String): HederaTopicRegistry?
    fun findByIsActive(isActive: Boolean): List<HederaTopicRegistry>
}

@Repository
interface HederaEntityLedgerRefRepository : JpaRepository<HederaEntityLedgerRef, String> {
    fun findByEntityTypeAndEntityId(entityType: String, entityId: String): List<HederaEntityLedgerRef>
    fun findByHederaTransactionId(hederaTransactionId: String): HederaEntityLedgerRef?
    fun findByOperationType(operationType: String): List<HederaEntityLedgerRef>
    fun findByHederaTopicId(hederaTopicId: String): List<HederaEntityLedgerRef>
    fun findByConsensusTimestampBetween(start: LocalDateTime, end: LocalDateTime): List<HederaEntityLedgerRef>
    fun findByEntityTypeAndOperationType(entityType: String, operationType: String): List<HederaEntityLedgerRef>
}

@Repository
interface HederaDocumentProofRepository : JpaRepository<HederaDocumentProof, String> {
    fun findByEntityTypeAndEntityId(entityType: String, entityId: String): List<HederaDocumentProof>
    fun findByDocumentType(documentType: String): List<HederaDocumentProof>
    fun findByDataHash(dataHash: String): HederaDocumentProof?
    fun findByHederaTransactionId(hederaTransactionId: String): HederaDocumentProof?
    fun findByConsensusTimestampBetween(start: LocalDateTime, end: LocalDateTime): List<HederaDocumentProof>
}

@Repository
interface HederaNFTRegistryRepository : JpaRepository<HederaNFTRegistry, String> {
    fun findByEntityTypeAndEntityId(entityType: String, entityId: String): HederaNFTRegistry?
    fun findByHederaTokenIdAndSerialNumber(hederaTokenId: String, serialNumber: Long): HederaNFTRegistry?
    fun findByHederaTokenId(hederaTokenId: String): List<HederaNFTRegistry>
    fun findByCurrentOwnerAccountId(currentOwnerAccountId: String): List<HederaNFTRegistry>
    fun findByEntityType(entityType: String): List<HederaNFTRegistry>
}

@Repository
interface HederaSyncCheckpointRepository : JpaRepository<HederaSyncCheckpoint, String> {
    fun findBySyncType(syncType: String): HederaSyncCheckpoint?
    fun findBySyncStatus(syncStatus: SyncStatus): List<HederaSyncCheckpoint>
}
