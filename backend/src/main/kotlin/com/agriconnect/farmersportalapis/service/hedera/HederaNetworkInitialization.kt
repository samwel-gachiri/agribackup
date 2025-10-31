package com.agriconnect.farmersportalapis.service.hedera

import com.agriconnect.farmersportalapis.config.HederaConfiguration
import com.hedera.hashgraph.sdk.*
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeoutException

@Service
class HederaNetworkInitialization(
    private val hederaConfig: HederaConfiguration
) {

    private val logger = LoggerFactory.getLogger(HederaNetworkInitialization::class.java)
    private lateinit var client: Client
    private lateinit var operatorAccountId: AccountId
    private lateinit var operatorPrivateKey: PrivateKey

    @PostConstruct
    fun initialize() {
        try {
            validateConfiguration()

            operatorAccountId = AccountId.fromString(hederaConfig.account.id)
            operatorPrivateKey = PrivateKey.fromString(hederaConfig.account.privateKey)

            client = when (hederaConfig.network.type.lowercase()) {
                "testnet" -> {
                    logger.info("Initializing Hedera Testnet client")
                    Client.forTestnet()
                }
                "mainnet" -> {
                    logger.info("Initializing Hedera Mainnet client")
                    Client.forMainnet()
                }
                else -> {
                    logger.warn("Unknown network type: ${hederaConfig.network.type}, defaulting to testnet")
                    Client.forTestnet()
                }
            }

            client.setOperator(operatorAccountId, operatorPrivateKey)

            // Set default transaction fees and timeouts
            client.setDefaultMaxTransactionFee(Hbar.fromTinybars(100_000_000)) // 1 HBAR
            client.setDefaultMaxQueryPayment(Hbar.fromTinybars(10_000_000)) // 0.1 HBAR

            // Validate connection
            validateConnection()

            logger.info("Hedera client initialized successfully for network: ${hederaConfig.network.type}")
        } catch (e: Exception) {
            logger.error("Failed to initialize Hedera client", e)
            throw RuntimeException("Hedera client initialization failed", e)
        }
    }

    private fun validateConfiguration() {
        require(hederaConfig.account.id.isNotBlank()) { "Hedera account ID is required" }
        require(hederaConfig.account.privateKey.isNotBlank()) { "Hedera private key is required" }
        require(hederaConfig.network.type.isNotBlank()) { "Hedera network type is required" }
    }

    fun getClient(): Client = client
    fun getOperatorAccountId(): AccountId = operatorAccountId
    fun getOperatorPrivateKey(): PrivateKey = operatorPrivateKey


    fun submitConsensusMessage(topicId: TopicId, message: String): TransactionReceipt {
        return executeWithRetry {
            val transaction = TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage(message)
                .freezeWith(client)
                .sign(operatorPrivateKey)

            val response = transaction.execute(client)
            response.getReceipt(client)
        }
    }

    fun createTopic(): TopicId {
        return executeWithRetry {
            val transaction = TopicCreateTransaction()
                .setTopicMemo("EUDR Compliance Topic")
                .freezeWith(client)
                .sign(operatorPrivateKey)

            val response = transaction.execute(client)
            val receipt = response.getReceipt(client)
            receipt.topicId!!
        }
    }

    fun deploySmartContract(bytecode: ByteArray): ContractId {
        return executeWithRetry {
            // First create a file to store the bytecode
            val fileCreateTransaction = FileCreateTransaction()
                .setContents(bytecode)
                .freezeWith(client)
                .sign(operatorPrivateKey)

            val fileResponse = fileCreateTransaction.execute(client)
            val fileReceipt = fileResponse.getReceipt(client)
            val fileId = fileReceipt.fileId!!

            // Then create the contract
            val contractCreateTransaction = ContractCreateTransaction()
                .setBytecodeFileId(fileId)
                .setGas(100000)
                .freezeWith(client)
                .sign(operatorPrivateKey)

            val contractResponse = contractCreateTransaction.execute(client)
            val contractReceipt = contractResponse.getReceipt(client)
            contractReceipt.contractId!!
        }
    }

    fun executeContractFunction(
        contractId: ContractId,
        functionName: String,
        params: ContractFunctionParameters? = ContractFunctionParameters()
    ): ContractFunctionResult {
        return executeWithRetry {
            val transaction = ContractExecuteTransaction()
                .setContractId(contractId)
                .setGas(100000)
                .setFunction(functionName, params!!)
                .freezeWith(client)
                .sign(operatorPrivateKey)

            val response = transaction.execute(client)
            val receipt = response.getReceipt(client)
            response.getRecord(client).contractFunctionResult!!
        }
    }

    fun createNFT(tokenName: String, symbol: String): TokenId {
        return executeWithRetry {
            // creating the NFT
            val transaction = TokenCreateTransaction()
                .setTokenName(tokenName)
                .setTokenSymbol(symbol)
                .setDecimals(0)
                .setInitialSupply(0)
                .setTreasuryAccountId(operatorAccountId)
                .setAdminKey(operatorPrivateKey.publicKey)
                .setSupplyKey(operatorPrivateKey.publicKey)
                .freezeWith(client)
                .sign(operatorPrivateKey)

            // setting the tokentype here because java is preventing use of method to set the token type
            transaction.tokenType = TokenType.NON_FUNGIBLE_UNIQUE

            val response = transaction.execute(client)
            val receipt = response.getReceipt(client)
            receipt.tokenId!!
        }
    }

    private fun validateConnection() {
        try {
            val query = AccountBalanceQuery()
                .setAccountId(operatorAccountId)

            query.execute(client)
            logger.info("Hedera connection validated successfully")
        } catch (e: Exception) {
            logger.error("Hedera connection validation failed", e)
            throw RuntimeException("Failed to validate Hedera connection", e)
        }
    }

    fun isNetworkAvailable(): Boolean {
        return try {
            val query = AccountBalanceQuery()
                .setAccountId(operatorAccountId)
            query.execute(client)
            true
        } catch (e: Exception) {
            logger.debug("Hedera network availability check failed", e)
            false
        }
    }

    fun getNetworkType(): String = hederaConfig.network.type

    fun getAccountId(): AccountId = operatorAccountId

    private fun <T> executeWithRetry(operation: () -> T): T {
        var lastException: Exception? = null

        repeat(hederaConfig.retry.maxAttempts) { attempt ->
            try {
                return operation()
            } catch (e: TimeoutException) {
                lastException = e
                logger.warn("Hedera operation timeout on attempt ${attempt + 1}, retrying...")
                if (attempt < hederaConfig.retry.maxAttempts - 1) {
                    val delay = calculateBackoffDelay(attempt)
                    Thread.sleep(delay)
                }
            } catch (e: PrecheckStatusException) {
                lastException = e
                logger.warn("Hedera precheck failed on attempt ${attempt + 1}: ${e.status}, retrying...")
                if (attempt < hederaConfig.retry.maxAttempts - 1) {
                    val delay = calculateBackoffDelay(attempt)
                    Thread.sleep(delay)
                }
            } catch (e: ReceiptStatusException) {
                lastException = e
                logger.warn("Hedera receipt status failed on attempt ${attempt + 1}: ${e.receipt.status}, retrying...")
                if (attempt < hederaConfig.retry.maxAttempts - 1) {
                    val delay = calculateBackoffDelay(attempt)
                    Thread.sleep(delay)
                }
            } catch (e: Exception) {
                logger.error("Hedera operation failed on attempt ${attempt + 1}", e)
                throw e
            }
        }

        throw RuntimeException("Hedera operation failed after ${hederaConfig.retry.maxAttempts} attempts", lastException)
    }

    private fun calculateBackoffDelay(attempt: Int): Long {
        val exponentialDelay = hederaConfig.retry.backoffDelay * (1L shl attempt) // Exponential backoff
        return minOf(exponentialDelay, hederaConfig.retry.maxBackoffDelay)
    }
}