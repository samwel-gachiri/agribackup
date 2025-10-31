package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "processing_events")
class ProcessingEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    var batch: EudrBatch,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id", nullable = false)
    var processor: Processor,

    @Column(name = "processing_type", nullable = false)
    var processingType: String,

    @Column(name = "input_quantity", nullable = false)
    var inputQuantity: BigDecimal,

    @Column(name = "output_quantity", nullable = false)
    var outputQuantity: BigDecimal,

    @Column(name = "processing_date", nullable = false)
    var processingDate: LocalDateTime,

    @Column(name = "processing_notes", columnDefinition = "TEXT")
    var processingNotes: String?,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
)