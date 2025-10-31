package com.agriconnect.farmersportalapis.service.hedera

import com.hedera.hashgraph.sdk.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

/**
 * Service for managing Hedera accounts for platform users
 * Handles account creation, token association, and secure key storage
 */
@Service
class HederaAccountService(
    private val hederaNetworkInitialization: HederaNetworkInitialization,
    private val hederaConsensusService: HederaConsensusServices
) {

    private val logger = LoggerFactory.getLogger(HederaAccountService::class.java)
    
    // In production, this should come from secure configuration (environment variable)
    private val encryptionKey = System.getenv("HEDERA_KEY_ENCRYPTION_SECRET")
        ?: "DEFAULT_KEY_FOR_DEV_ONLY_CHANGE_IN_PROD" // 32 bytes minimum
    
    private val secureRandom = SecureRandom()

    /**
     * Create a new Hedera account for a platform user
     * @param initialBalance Initial HBAR balance for the account (default 10 HBAR)
     * @param memo Account memo for identification
     * @return Pair of AccountId and encrypted PrivateKey
     */
    fun createHederaAccount(
        initialBalance: Hbar = Hbar.from(10),
        memo: String = "AgriBackup Platform Account"
    ): HederaAccountCreationResult {
        return try {
            // Generate new key pair
            val newPrivateKey = PrivateKey.generateED25519()
            val newPublicKey = newPrivateKey.publicKey

            logger.info("Creating new Hedera account with initial balance: $initialBalance")

            // Create account transaction
            val transaction = AccountCreateTransaction()
                .setKey(newPublicKey)
                .setInitialBalance(initialBalance)
                .setAccountMemo(memo)
                .setMaxAutomaticTokenAssociations(10) // Allow automatic token associations
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(hederaNetworkInitialization.getOperatorPrivateKey())

            val response = transaction.execute(hederaNetworkInitialization.getClient())
            val receipt = response.getReceipt(hederaNetworkInitialization.getClient())
            val accountId = receipt.accountId!!

            // Encrypt private key for secure storage
            val encryptedPrivateKey = encryptPrivateKey(newPrivateKey.toString())

            logger.info("Successfully created Hedera account: $accountId")

            // Record account creation on consensus service
            try {
                hederaConsensusService.recordAccountCreation(accountId, memo)
            } catch (e: Exception) {
                logger.warn("Failed to record account creation on consensus service: ${e.message}")
            }

            HederaAccountCreationResult(
                accountId = accountId.toString(),
                publicKey = newPublicKey.toString(),
                encryptedPrivateKey = encryptedPrivateKey,
                transactionId = response.transactionId.toString()
            )
        } catch (e: Exception) {
            logger.error("Failed to create Hedera account", e)
            throw RuntimeException("Failed to create Hedera account: ${e.message}", e)
        }
    }

    /**
     * Associate a token with an account
     * @param accountId Account to associate token with
     * @param encryptedPrivateKey Encrypted private key of the account
     * @param tokenId Token to associate
     */
    fun associateTokenWithAccount(
        accountId: String,
        encryptedPrivateKey: String,
        tokenId: TokenId
    ): Boolean {
        return try {
            val accountIdObj = AccountId.fromString(accountId)
            val privateKey = decryptPrivateKey(encryptedPrivateKey)

            logger.info("Associating token $tokenId with account $accountId")

            val transaction = TokenAssociateTransaction()
                .setAccountId(accountIdObj)
                .setTokenIds(listOf(tokenId))
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(privateKey) // Account holder must sign

            val response = transaction.execute(hederaNetworkInitialization.getClient())
            response.getReceipt(hederaNetworkInitialization.getClient())

            logger.info("Successfully associated token $tokenId with account $accountId")

            // Record token association on consensus service
            try {
                hederaConsensusService.recordTokenAssociation(accountIdObj, tokenId)
            } catch (e: Exception) {
                logger.warn("Failed to record token association on consensus service: ${e.message}")
            }

            true
        } catch (e: Exception) {
            logger.error("Failed to associate token with account $accountId", e)
            false
        }
    }

    /**
     * Associate multiple tokens with an account
     */
    fun associateMultipleTokens(
        accountId: String,
        encryptedPrivateKey: String,
        tokenIds: List<TokenId>
    ): Boolean {
        return try {
            val accountIdObj = AccountId.fromString(accountId)
            val privateKey = decryptPrivateKey(encryptedPrivateKey)

            logger.info("Associating ${tokenIds.size} tokens with account $accountId")

            val transaction = TokenAssociateTransaction()
                .setAccountId(accountIdObj)
                .setTokenIds(tokenIds)
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(privateKey)

            val response = transaction.execute(hederaNetworkInitialization.getClient())
            response.getReceipt(hederaNetworkInitialization.getClient())

            logger.info("Successfully associated ${tokenIds.size} tokens with account $accountId")
            true
        } catch (e: Exception) {
            logger.error("Failed to associate multiple tokens with account $accountId", e)
            false
        }
    }

    /**
     * Get account balance
     */
    fun getAccountBalance(accountId: String): AccountBalance? {
        return try {
            val accountIdObj = AccountId.fromString(accountId)
            val query = AccountBalanceQuery()
                .setAccountId(accountIdObj)

            query.execute(hederaNetworkInitialization.getClient())
        } catch (e: Exception) {
            logger.error("Failed to get balance for account $accountId", e)
            null
        }
    }

    /**
     * Get token balance for specific token
     */
    fun getTokenBalance(accountId: String, tokenId: TokenId): Long {
        return try {
            val balance = getAccountBalance(accountId)
            balance?.tokens?.get(tokenId) ?: 0L
        } catch (e: Exception) {
            logger.error("Failed to get token balance for account $accountId", e)
            0L
        }
    }

    /**
     * Encrypt private key for secure storage using AES-GCM
     */
    private fun encryptPrivateKey(privateKeyString: String): String {
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12)
            secureRandom.nextBytes(iv)
            
            val keySpec = SecretKeySpec(encryptionKey.take(32).toByteArray(), "AES")
            val gcmSpec = GCMParameterSpec(128, iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
            val encryptedBytes = cipher.doFinal(privateKeyString.toByteArray())
            
            // Combine IV and encrypted data
            val combined = iv + encryptedBytes
            return Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            logger.error("Failed to encrypt private key", e)
            throw RuntimeException("Encryption failed", e)
        }
    }

    /**
     * Decrypt private key from storage
     */
    private fun decryptPrivateKey(encryptedKey: String): PrivateKey {
        try {
            val combined = Base64.getDecoder().decode(encryptedKey)
            val iv = combined.take(12).toByteArray()
            val encryptedBytes = combined.drop(12).toByteArray()
            
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val keySpec = SecretKeySpec(encryptionKey.take(32).toByteArray(), "AES")
            val gcmSpec = GCMParameterSpec(128, iv)
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            val privateKeyString = String(decryptedBytes)
            
            return PrivateKey.fromString(privateKeyString)
        } catch (e: Exception) {
            logger.error("Failed to decrypt private key", e)
            throw RuntimeException("Decryption failed", e)
        }
    }

    /**
     * Transfer HBAR between accounts
     */
    fun transferHbar(
        fromAccountId: String,
        fromEncryptedPrivateKey: String,
        toAccountId: String,
        amount: Hbar
    ): Boolean {
        return try {
            val fromAccount = AccountId.fromString(fromAccountId)
            val toAccount = AccountId.fromString(toAccountId)
            val privateKey = decryptPrivateKey(fromEncryptedPrivateKey)

            val transaction = TransferTransaction()
                .addHbarTransfer(fromAccount, amount.negated())
                .addHbarTransfer(toAccount, amount)
                .freezeWith(hederaNetworkInitialization.getClient())
                .sign(privateKey)

            transaction.execute(hederaNetworkInitialization.getClient())
            logger.info("Transferred $amount HBAR from $fromAccountId to $toAccountId")
            true
        } catch (e: Exception) {
            logger.error("Failed to transfer HBAR", e)
            false
        }
    }

    /**
     * Export account credentials (for user backup)
     * WARNING: Only call this when user explicitly requests their credentials
     */
    fun exportAccountCredentials(
        accountId: String,
        encryptedPrivateKey: String,
        userId: String
    ): AccountCredentialsExport {
        logger.warn("Exporting account credentials for user: $userId, account: $accountId")
        
        val privateKey = decryptPrivateKey(encryptedPrivateKey)
        
        return AccountCredentialsExport(
            accountId = accountId,
            privateKey = privateKey.toString(),
            publicKey = privateKey.publicKey.toString(),
            warning = "KEEP THIS SAFE! Anyone with access to your private key can control your account."
        )
    }
}

/**
 * Result of account creation
 */
data class HederaAccountCreationResult(
    val accountId: String,
    val publicKey: String,
    val encryptedPrivateKey: String,
    val transactionId: String
)

/**
 * Account credentials for export/backup
 */
data class AccountCredentialsExport(
    val accountId: String,
    val privateKey: String,
    val publicKey: String,
    val warning: String
)
