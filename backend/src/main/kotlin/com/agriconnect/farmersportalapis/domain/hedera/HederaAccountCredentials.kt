package com.agriconnect.farmersportalapis.domain.hedera

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

/**
 * Stores encrypted Hedera account credentials for platform users
 * Private keys are encrypted before storage for security
 */
@Entity
@Table(name = "hedera_account_credentials")
class HederaAccountCredentials(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false)
    var id: String = "",

    @Column(name = "user_id", nullable = false, unique = true)
    var userId: String,

    @Column(name = "entity_type", nullable = false)
    var entityType: String, // AGGREGATOR, PROCESSOR, IMPORTER, EXPORTER, FARMER

    @Column(name = "entity_id", nullable = false)
    var entityId: String,

    @Column(name = "hedera_account_id", nullable = false, unique = true)
    var hederaAccountId: String,

    @Column(name = "public_key", nullable = false)
    var publicKey: String,

    @Column(name = "encrypted_private_key", nullable = false, columnDefinition = "TEXT")
    var encryptedPrivateKey: String,

    @Column(name = "initial_balance_hbar")
    var initialBalanceHbar: String? = null,

    @Column(name = "account_memo")
    var accountMemo: String? = null,

    @Column(name = "creation_transaction_id")
    var creationTransactionId: String? = null,

    @Column(name = "tokens_associated", columnDefinition = "TEXT")
    var tokensAssociated: String? = null, // JSON array of token IDs

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_used_at")
    var lastUsedAt: LocalDateTime? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null
)
