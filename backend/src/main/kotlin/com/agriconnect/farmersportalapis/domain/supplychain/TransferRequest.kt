package com.agriconnect.farmersportalapis.domain.supplychain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * TransferRequest - Two-Party Handshake Entity
 * 
 * Represents a pending transfer between supply chain actors.
 * Data is only written to Hedera after BOTH parties confirm.
 */
@Entity
@Table(name = "transfer_requests")
class TransferRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, unique = true)
    var id: String = "",

    // Sender information
    @Column(name = "from_supplier_id")
    var fromSupplierId: String? = null,

    @Column(name = "from_farmer_id")
    var fromFarmerId: String? = null,

    @Column(name = "from_production_unit_id")
    var fromProductionUnitId: String? = null,

    @Column(name = "sender_name", nullable = false)
    var senderName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 50)
    var senderType: SupplierType,

    // Recipient information
    @Column(name = "to_supplier_id", nullable = false)
    var toSupplierId: String,

    @Column(name = "recipient_name")
    var recipientName: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", length = 50)
    var recipientType: SupplierType? = null,

    // Produce details
    @Column(name = "produce_type", nullable = false)
    var produceType: String,

    @Column(name = "quality_grade", length = 10)
    var qualityGrade: String? = null,

    // Quantities - both parties record their claim
    @Column(name = "sender_quantity_kg", nullable = false, precision = 15, scale = 2)
    var senderQuantityKg: BigDecimal,

    @Column(name = "receiver_quantity_kg", precision = 15, scale = 2)
    var receiverQuantityKg: BigDecimal? = null,

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: TransferStatus = TransferStatus.PENDING,

    // Timestamps
    @Column(name = "transfer_date")
    var transferDate: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "sender_confirmed_at")
    var senderConfirmedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "receiver_confirmed_at")
    var receiverConfirmedAt: LocalDateTime? = null,

    // Notes
    @Column(name = "sender_notes", columnDefinition = "TEXT")
    var senderNotes: String? = null,

    @Column(name = "receiver_notes", columnDefinition = "TEXT")
    var receiverNotes: String? = null,

    @Column(name = "dispute_reason", columnDefinition = "TEXT")
    var disputeReason: String? = null,

    // Hedera - only written after both confirm
    @Column(name = "hedera_transaction_id", length = 100)
    var hederaTransactionId: String? = null,

    @Column(name = "hedera_topic_id", length = 50)
    var hederaTopicId: String? = null
) {
    /**
     * Check if there is a quantity discrepancy
     */
    fun hasDiscrepancy(): Boolean {
        return receiverQuantityKg != null && receiverQuantityKg != senderQuantityKg
    }

    /**
     * Get discrepancy amount (positive = receiver got less, negative = receiver got more)
     */
    fun getDiscrepancyKg(): BigDecimal? {
        return receiverQuantityKg?.let { senderQuantityKg.subtract(it) }
    }

    /**
     * Check if transfer is fully confirmed by both parties
     */
    fun isFullyConfirmed(): Boolean {
        return status == TransferStatus.CONFIRMED && receiverConfirmedAt != null
    }

    /**
     * Check if transfer originated from a farmer
     */
    fun isFromFarmer(): Boolean = fromFarmerId != null

    /**
     * Check if transfer originated from a supplier
     */
    fun isFromSupplier(): Boolean = fromSupplierId != null
}

enum class TransferStatus {
    PENDING,          // Sender created, awaiting receiver confirmation
    CONFIRMED,        // Both parties confirmed, written to Hedera
    REJECTED,         // Receiver rejected the transfer
    DISPUTED,         // Receiver confirmed with different quantity
    CANCELLED         // Sender cancelled before confirmation
}
