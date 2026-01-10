package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.domain.supplychain.SupplierType
import com.agriconnect.farmersportalapis.domain.supplychain.TransferRequest
import com.agriconnect.farmersportalapis.domain.supplychain.TransferStatus
import com.agriconnect.farmersportalapis.repository.SupplyChainSupplierRepository
import com.agriconnect.farmersportalapis.repository.TransferRequestRepository
import com.agriconnect.farmersportalapis.service.hedera.HederaMainService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class TransferRequestService(
    private val transferRequestRepository: TransferRequestRepository,
    private val supplierRepository: SupplyChainSupplierRepository,
    private val hederaMainService: HederaMainService
) {
    private val logger = LoggerFactory.getLogger(TransferRequestService::class.java)

    /**
     * Create transfer from Farmer to Supplier
     */
    @Transactional
    fun createFarmerTransfer(
        fromFarmerId: String,
        fromProductionUnitId: String,
        toSupplierId: String,
        produceType: String,
        quantityKg: BigDecimal,
        qualityGrade: String?,
        notes: String?,
        farmerName: String
    ): TransferRequest {
        val recipient = supplierRepository.findById(toSupplierId).orElse(null)

        val transfer = TransferRequest(
            fromFarmerId = fromFarmerId,
            fromProductionUnitId = fromProductionUnitId,
            toSupplierId = toSupplierId,
            senderName = farmerName,
            senderType = SupplierType.FARMER,
            recipientName = recipient?.supplierName,
            recipientType = recipient?.supplierType,
            produceType = produceType,
            qualityGrade = qualityGrade,
            senderQuantityKg = quantityKg,
            senderNotes = notes,
            status = TransferStatus.PENDING,
            transferDate = LocalDateTime.now()
        )

        logger.info("Transfer created: Farmer $fromFarmerId -> Supplier $toSupplierId, $quantityKg kg $produceType")
        return transferRequestRepository.save(transfer)
    }

    /**
     * Create transfer from Supplier to Supplier
     */
    @Transactional
    fun createSupplierTransfer(
        fromSupplierId: String,
        toSupplierId: String,
        produceType: String,
        quantityKg: BigDecimal,
        qualityGrade: String?,
        notes: String?
    ): TransferRequest {
        val sender = supplierRepository.findById(fromSupplierId).orElseThrow {
            IllegalArgumentException("Sender supplier not found: $fromSupplierId")
        }
        val recipient = supplierRepository.findById(toSupplierId).orElse(null)

        val transfer = TransferRequest(
            fromSupplierId = fromSupplierId,
            toSupplierId = toSupplierId,
            senderName = sender.supplierName,
            senderType = sender.supplierType,
            recipientName = recipient?.supplierName,
            recipientType = recipient?.supplierType,
            produceType = produceType,
            qualityGrade = qualityGrade,
            senderQuantityKg = quantityKg,
            senderNotes = notes,
            status = TransferStatus.PENDING,
            transferDate = LocalDateTime.now()
        )

        logger.info("Transfer created: Supplier $fromSupplierId -> Supplier $toSupplierId, $quantityKg kg $produceType")
        return transferRequestRepository.save(transfer)
    }

    /**
     * Confirm transfer receipt - Two-Party Handshake
     */
    @Transactional
    fun confirmTransfer(
        transferId: String,
        receivedQuantityKg: BigDecimal,
        notes: String?
    ): TransferRequest {
        val transfer = transferRequestRepository.findById(transferId).orElseThrow {
            IllegalArgumentException("Transfer not found: $transferId")
        }

        if (transfer.status != TransferStatus.PENDING) {
            throw IllegalStateException("Transfer is not in PENDING status")
        }

        transfer.receiverQuantityKg = receivedQuantityKg
        transfer.receiverNotes = notes
        transfer.receiverConfirmedAt = LocalDateTime.now()

        // Check for discrepancy
        if (transfer.hasDiscrepancy()) {
            transfer.status = TransferStatus.DISPUTED
            logger.warn("Transfer $transferId has discrepancy: sent ${transfer.senderQuantityKg} kg, received $receivedQuantityKg kg")
        } else {
            transfer.status = TransferStatus.CONFIRMED
        }

        // Write to Hedera after both parties confirm
        writeToHedera(transfer)

        logger.info("Transfer $transferId confirmed with $receivedQuantityKg kg, status: ${transfer.status}")
        return transferRequestRepository.save(transfer)
    }

    /**
     * Dispute transfer with reason
     */
    @Transactional
    fun disputeTransfer(
        transferId: String,
        actualQuantityKg: BigDecimal,
        reason: String
    ): TransferRequest {
        val transfer = transferRequestRepository.findById(transferId).orElseThrow {
            IllegalArgumentException("Transfer not found: $transferId")
        }

        transfer.receiverQuantityKg = actualQuantityKg
        transfer.disputeReason = reason
        transfer.receiverConfirmedAt = LocalDateTime.now()
        transfer.status = TransferStatus.DISPUTED

        // Write dispute to Hedera (both claims are immutable)
        writeToHedera(transfer)

        logger.warn("Transfer $transferId disputed: $reason")
        return transferRequestRepository.save(transfer)
    }

    /**
     * Reject transfer
     */
    @Transactional
    fun rejectTransfer(transferId: String, reason: String?): TransferRequest {
        val transfer = transferRequestRepository.findById(transferId).orElseThrow {
            IllegalArgumentException("Transfer not found: $transferId")
        }

        transfer.status = TransferStatus.REJECTED
        transfer.receiverNotes = reason
        transfer.receiverConfirmedAt = LocalDateTime.now()

        logger.info("Transfer $transferId rejected: $reason")
        return transferRequestRepository.save(transfer)
    }

    /**
     * Get incoming transfers for a supplier
     */
    fun getIncomingTransfers(supplierId: String, page: Int, size: Int): Page<TransferRequest> {
        return transferRequestRepository.findByToSupplierIdOrderByCreatedAtDesc(
            supplierId, PageRequest.of(page, size)
        )
    }

    /**
     * Get outgoing transfers for a supplier
     */
    fun getOutgoingTransfers(supplierId: String, page: Int, size: Int): Page<TransferRequest> {
        return transferRequestRepository.findByFromSupplierIdOrderByCreatedAtDesc(
            supplierId, PageRequest.of(page, size)
        )
    }

    /**
     * Get outgoing transfers for a farmer
     */
    fun getFarmerOutgoingTransfers(farmerId: String, page: Int, size: Int): Page<TransferRequest> {
        return transferRequestRepository.findByFromFarmerIdOrderByCreatedAtDesc(
            farmerId, PageRequest.of(page, size)
        )
    }

    /**
     * Count pending transfers for a supplier
     */
    fun countPendingTransfers(supplierId: String): Long {
        return transferRequestRepository.countByToSupplierIdAndStatus(supplierId, TransferStatus.PENDING)
    }

    /**
     * Get inventory for a supplier based on confirmed incoming transfers
     */
    fun getInventoryForSupplier(supplierId: String): List<SupplierInventoryItem> {
        // Get all confirmed incoming transfers for this supplier
        val confirmedTransfers = transferRequestRepository.findByToSupplierIdAndStatus(
            supplierId, TransferStatus.CONFIRMED
        )
        
        // Transform into inventory items
        return confirmedTransfers.map { transfer ->
            SupplierInventoryItem(
                id = transfer.id,
                produceType = transfer.produceType,
                qualityGrade = transfer.qualityGrade ?: "N/A",
                quantityKg = transfer.receiverQuantityKg ?: transfer.senderQuantityKg,
                sourceName = transfer.senderName ?: "Unknown",
                sourceType = transfer.senderType?.name ?: "UNKNOWN",
                receivedAt = transfer.receiverConfirmedAt ?: transfer.createdAt,
                hederaTransactionId = transfer.hederaTransactionId
            )
        }
    }

    /**
     * Write confirmed transfer to Hedera
     */
    private fun writeToHedera(transfer: TransferRequest) {
        try {
            hederaMainService?.let { service ->
                val transactionId = service.recordTransferEvent(
                    transferId = transfer.id,
                    senderName = transfer.senderName ?: "Unknown",
                    senderType = transfer.senderType?.name ?: "UNKNOWN",
                    recipientName = transfer.recipientName ?: "Unknown",
                    recipientType = transfer.recipientType?.name ?: "UNKNOWN",
                    produceType = transfer.produceType,
                    senderQuantityKg = transfer.senderQuantityKg,
                    receiverQuantityKg = transfer.receiverQuantityKg,
                    status = transfer.status.name,
                    hasDiscrepancy = transfer.hasDiscrepancy(),
                    discrepancyKg = transfer.getDiscrepancyKg()
                )
                transfer.hederaTransactionId = transactionId
                logger.info("Transfer ${transfer.id} written to Hedera: $transactionId")
            }
        } catch (e: Exception) {
            logger.error("Failed to write transfer ${transfer.id} to Hedera: ${e.message}")
            // Don't fail the transaction - Hedera write can be retried
        }
    }
}

// DTO for inventory items
data class SupplierInventoryItem(
    val id: String,
    val produceType: String,
    val qualityGrade: String,
    val quantityKg: java.math.BigDecimal,
    val sourceName: String,
    val sourceType: String,
    val receivedAt: java.time.LocalDateTime,
    val hederaTransactionId: String?
)
