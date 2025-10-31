package com.agriconnect.farmersportalapis.domain.hedera

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * HederaEntityRegistry - Maps our entities to Hedera accounts (REFERENCE ONLY)
 * 
 * Purpose: Link application entities to their Hedera account IDs
 * Rationale: We need to know which Hedera account represents which entity
 * 
 * IMPORTANT: This does NOT store account history, balances, or transactions.
 * Query Mirror Node API for that data:
 * - GET /api/v1/accounts/{accountId} - Account details
 * - GET /api/v1/accounts/{accountId}/transactions - Transaction history
 */
@Entity
@Table(name = "hedera_entity_registry")
class HederaEntityRegistry(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "registry_id", length = 36)
    var id: String = "",

    @Column(name = "entity_type", length = 50, nullable = false)
    var entityType: String, // farmer, aggregator, processor, importer, batch

    @Column(name = "entity_id", length = 36, nullable = false)
    var entityId: String, // Primary key of the entity in our system

    @Column(name = "hedera_account_id", length = 50, nullable = false)
    var hederaAccountId: String, // e.g., "0.0.12345"

    @Column(name = "public_key", length = 200)
    var publicKey: String? = null, // For signature verification

    @Column(name = "created_transaction_id", length = 100)
    var createdTransactionId: String? = null, // Transaction that created this account

    @Column(name = "registered_at", nullable = false)
    var registeredAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * HederaTopicRegistry - Our HCS topics (REFERENCE ONLY)
 * 
 * Purpose: Track which HCS topics we use for different supply chain channels
 * Rationale: We need to know which topic to submit messages to
 * 
 * IMPORTANT: This does NOT store topic messages.
 * Query Mirror Node API for message history:
 * - GET /api/v1/topics/{topicId}/messages - All messages
 * - GET /api/v1/topics/{topicId}/messages/{sequenceNumber} - Specific message
 */
@Entity
@Table(name = "hedera_topic_registry")
class HederaTopicRegistry(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "topic_id", length = 36)
    var id: String = "",

    @Column(name = "hedera_topic_id", length = 50, nullable = false, unique = true)
    var hederaTopicId: String, // e.g., "0.0.98765"

    @Column(name = "topic_name", nullable = false)
    var topicName: String, // Descriptive name

    @Column(name = "channel_type", length = 50, nullable = false)
    var channelType: String, // FARMER_EVENTS, BATCH_TRACKING, EUDR_COMPLIANCE, etc.

    @Column(name = "purpose_description", columnDefinition = "TEXT")
    var purposeDescription: String? = null,

    @Column(name = "submit_key_alias", length = 100)
    var submitKeyAlias: String? = null, // Alias of the key that can submit

    @Column(name = "created_transaction_id", length = 100)
    var createdTransactionId: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_active")
    var isActive: Boolean = true
)

/**
 * HederaEntityLedgerRef - Entity-to-transaction mappings
 * 
 * Purpose: Store which Hedera transactions relate to which entities
 * Rationale: Enables quick lookup: "Show me all blockchain transactions for this batch"
 * 
 * IMPORTANT: This only stores the REFERENCE (transaction ID).
 * Query Mirror Node API for full transaction data:
 * - GET /api/v1/transactions/{transactionId} - Full transaction details
 */
@Entity
@Table(name = "hedera_entity_ledger_refs")
class HederaEntityLedgerRef(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ref_id", length = 36)
    var id: String = "",

    @Column(name = "entity_type", length = 50, nullable = false)
    var entityType: String, // farmer, batch, collection, shipment, inspection

    @Column(name = "entity_id", length = 36, nullable = false)
    var entityId: String,

    @Column(name = "operation_type", length = 50, nullable = false)
    var operationType: String, // CREATED, UPDATED, TRANSFERRED, VERIFIED, etc.

    @Column(name = "hedera_transaction_id", length = 100, nullable = false)
    var hederaTransactionId: String, // The pointer to Hedera ledger

    @Column(name = "hedera_topic_id", length = 50)
    var hederaTopicId: String? = null, // If this was an HCS message

    @Column(name = "sequence_number")
    var sequenceNumber: Long? = null, // HCS message sequence number

    @Column(name = "consensus_timestamp", nullable = false)
    var consensusTimestamp: LocalDateTime, // From Hedera

    @Column(name = "hashscan_url", length = 500)
    var hashscanUrl: String? = null, // Direct link to HashScan explorer

    @Column(name = "memo", columnDefinition = "TEXT")
    var memo: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * HederaDocumentProof - Cryptographic proofs for documents anchored on Hedera
 * 
 * Purpose: Store proof that a document existed at a specific time
 * Rationale: Compute hash locally → submit to HCS → store proof reference
 *            Enables tamper-proof verification without storing document on-chain
 * 
 * IMPORTANT: We store the hash and transaction reference, NOT the document.
 * Verification flow:
 * 1. User uploads document
 * 2. We compute SHA-256 hash
 * 3. Submit hash to HCS topic
 * 4. Store: hash + transaction_id + consensus_timestamp
 * 5. Later: Re-compute hash, query Mirror Node, verify match
 */
@Entity
@Table(name = "hedera_document_proofs")
class HederaDocumentProof(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "proof_id", length = 36)
    var id: String = "",

    @Column(name = "entity_type", length = 50, nullable = false)
    var entityType: String,

    @Column(name = "entity_id", length = 36, nullable = false)
    var entityId: String,

    @Column(name = "document_type", length = 100, nullable = false)
    var documentType: String, // EUDR_DECLARATION, INSPECTION_REPORT, etc.

    @Column(name = "document_name")
    var documentName: String? = null,

    @Column(name = "data_hash", length = 64, nullable = false)
    var dataHash: String, // SHA-256 hash of the document

    @Column(name = "hedera_transaction_id", length = 100, nullable = false)
    var hederaTransactionId: String, // Transaction where hash was submitted

    @Column(name = "hedera_topic_id", length = 50)
    var hederaTopicId: String? = null, // Topic where hash was published

    @Column(name = "sequence_number")
    var sequenceNumber: Long? = null, // HCS message sequence number

    @Column(name = "consensus_timestamp", nullable = false)
    var consensusTimestamp: LocalDateTime, // When hash was anchored

    @Column(name = "verification_url", length = 500)
    var verificationUrl: String? = null, // HashScan URL for public verification

    @Column(name = "ipfs_cid", length = 100)
    var ipfsCid: String? = null, // If document stored on IPFS

    @Column(name = "proof_metadata", columnDefinition = "JSON")
    var proofMetadata: String? = null, // Additional proof info (merkle path, signatures)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * HederaNFTRegistry - NFTs we minted for our entities (REFERENCE ONLY)
 * 
 * Purpose: Track which NFTs represent which batches/entities
 * Rationale: NFTs provide unique, transferable identifiers for supply chain items
 * 
 * IMPORTANT: This does NOT store NFT transaction history or ownership changes.
 * Query Mirror Node API for that:
 * - GET /api/v1/tokens/{tokenId}/nfts/{serialNumber} - NFT details
 * - GET /api/v1/tokens/{tokenId}/nfts/{serialNumber}/transactions - Transfer history
 */
@Entity
@Table(name = "hedera_nft_registry")
class HederaNFTRegistry(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "nft_id", length = 36)
    var id: String = "",

    @Column(name = "hedera_token_id", length = 50, nullable = false)
    var hederaTokenId: String, // e.g., "0.0.11111"

    @Column(name = "serial_number", nullable = false)
    var serialNumber: Long, // NFT serial number

    @Column(name = "entity_type", length = 50, nullable = false)
    var entityType: String, // batch, farmer_collection, shipment

    @Column(name = "entity_id", length = 36, nullable = false)
    var entityId: String,

    @Column(name = "mint_transaction_id", length = 100)
    var mintTransactionId: String? = null, // Transaction that minted this NFT

    @Column(name = "current_owner_account_id", length = 50)
    var currentOwnerAccountId: String? = null, // Updated on transfers

    @Column(name = "ipfs_metadata_cid", length = 100)
    var ipfsMetadataCid: String? = null, // IPFS CID of metadata JSON

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

/**
 * HederaSyncCheckpoint - Mirror Node API sync state tracking
 * 
 * Purpose: Track where we left off when polling Mirror Node for updates
 * Rationale: Prevents duplicate processing and enables efficient polling
 * 
 * Use cases:
 * - Poll Mirror Node every 5 minutes for new topic messages
 * - Sync account transactions since last checkpoint
 * - Update NFT ownership based on recent transfers
 */
@Entity
@Table(name = "hedera_sync_checkpoints")
class HederaSyncCheckpoint(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "checkpoint_id", length = 36)
    var id: String = "",

    @Column(name = "sync_type", length = 50, nullable = false, unique = true)
    var syncType: String, // TOPIC_MESSAGES, ACCOUNT_TRANSACTIONS, TOKEN_TRANSFERS, etc.

    @Column(name = "last_consensus_timestamp", nullable = false)
    var lastConsensusTimestamp: LocalDateTime, // Last timestamp we processed

    @Column(name = "last_sequence_number")
    var lastSequenceNumber: Long? = null, // For topic messages

    @Column(name = "records_processed")
    var recordsProcessed: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", length = 50)
    var syncStatus: SyncStatus = SyncStatus.COMPLETED,

    @Column(name = "error_message", columnDefinition = "TEXT")
    var errorMessage: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

// Enums
enum class SyncStatus {
    COMPLETED, IN_PROGRESS, FAILED
}

