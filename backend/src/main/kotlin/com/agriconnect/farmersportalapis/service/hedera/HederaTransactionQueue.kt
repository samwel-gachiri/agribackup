package com.agriconnect.farmersportalapis.service.hedera

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

@Service
class HederaTransactionQueue(
    private val hederaNetworkInitialization: HederaNetworkInitialization,
    private val hederaConsensusService: HederaConsensusServices
) {

    private val logger = LoggerFactory.getLogger(HederaTransactionQueue::class.java)
    private val transactionQueue = ConcurrentLinkedQueue<QueuedTransaction>()
    private val isProcessing = AtomicBoolean(false)

    data class QueuedTransaction(
        val id: String,
        val type: TransactionType,
        val payload: String,
        val entityId: String,
        val entityType: String,
        val createdAt: LocalDateTime = LocalDateTime.now(),
        var retryCount: Int = 0,
        val maxRetries: Int = 5
    )

    enum class TransactionType {
        CONSENSUS_MESSAGE, CONTRACT_EXECUTION, TOKEN_CREATION
    }

    fun queueConsensusMessage(
        entityId: String,
        entityType: String,
        message: String
    ): String {
        val transactionId = generateTransactionId()
        val queuedTransaction = QueuedTransaction(
            id = transactionId,
            type = TransactionType.CONSENSUS_MESSAGE,
            payload = message,
            entityId = entityId,
            entityType = entityType
        )

        transactionQueue.offer(queuedTransaction)
        logger.info("Queued consensus message for entity: $entityType:$entityId")

        // Try to process immediately if network is available
        processQueueAsync()

        return transactionId
    }

    @Async("hederaTaskExecutor")
    fun processQueueAsync() {
        if (isProcessing.compareAndSet(false, true)) {
            try {
                processQueue()
            } finally {
                isProcessing.set(false)
            }
        }
    }

    @Scheduled(fixedDelay = 30000) // Process queue every 30 seconds
    fun processQueueScheduled() {
        if (!transactionQueue.isEmpty()) {
            processQueueAsync()
        }
    }

    private fun processQueue() {
        logger.debug("Processing Hedera transaction queue, size: ${transactionQueue.size}")

        val iterator = transactionQueue.iterator()
        while (iterator.hasNext()) {
            val transaction = iterator.next()

            try {
                when (transaction.type) {
                    TransactionType.CONSENSUS_MESSAGE -> {
                        processConsensusMessage(transaction)
                        iterator.remove()
                        logger.debug("Successfully processed queued consensus message: ${transaction.id}")
                    }
                    TransactionType.CONTRACT_EXECUTION -> {
                        // Future implementation for contract execution
                        iterator.remove()
                    }
                    TransactionType.TOKEN_CREATION -> {
                        // Future implementation for token creation
                        iterator.remove()
                    }
                }
            } catch (e: Exception) {
                transaction.retryCount++
                logger.warn("Failed to process queued transaction ${transaction.id}, retry ${transaction.retryCount}/${transaction.maxRetries}", e)

                if (transaction.retryCount >= transaction.maxRetries) {
                    logger.error("Max retries exceeded for transaction ${transaction.id}, removing from queue")
                    iterator.remove()
                } else if (isNetworkError(e)) {
                    // Keep in queue for network errors, but break to avoid processing more
                    logger.info("Network error detected, stopping queue processing")
                    break
                } else {
                    // Remove non-network errors immediately
                    logger.error("Non-recoverable error for transaction ${transaction.id}, removing from queue", e)
                    iterator.remove()
                }
            }
        }
    }

    private fun processConsensusMessage(transaction: QueuedTransaction) {
        // Extract topic ID from the consensus service
        val topicId = hederaConsensusService.getTopicId()
        hederaNetworkInitialization.submitConsensusMessage(topicId, transaction.payload)
    }

    private fun isNetworkError(exception: Exception): Boolean {
        val message = exception.message?.lowercase() ?: ""
        return message.contains("timeout") ||
               message.contains("connection") ||
               message.contains("network") ||
               message.contains("unavailable") ||
               exception is SocketTimeoutException ||
               exception is ConnectException
    }

    private fun generateTransactionId(): String {
        return "queue_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    fun getQueueSize(): Int = transactionQueue.size

    fun clearQueue() {
        transactionQueue.clear()
        logger.info("Cleared Hedera transaction queue")
    }
}