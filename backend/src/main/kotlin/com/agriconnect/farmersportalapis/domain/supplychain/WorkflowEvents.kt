package com.agriconnect.farmersportalapis.domain.supplychain

import com.agriconnect.farmersportalapis.domain.eudr.Aggregator
import com.agriconnect.farmersportalapis.domain.eudr.Importer
import com.agriconnect.farmersportalapis.domain.eudr.Processor
import com.agriconnect.farmersportalapis.domain.eudr.ProductionUnit
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "workflow_collection_events")
class WorkflowCollectionEvent(
    @Id
    @Column(name = "event_id", length = 36)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    var workflow: SupplyChainWorkflow,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_unit_id", nullable = false)
    var productionUnit: ProductionUnit,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aggregator_id", nullable = false)
    var aggregator: Aggregator,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    var farmer: Farmer,

    @Column(name = "quantity_collected_kg", nullable = false, precision = 15, scale = 2)
    var quantityCollectedKg: BigDecimal,

    @Column(name = "collection_date", nullable = false)
    var collectionDate: LocalDateTime,

    @Column(name = "quality_grade", length = 50)
    var qualityGrade: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "hedera_hash")
    var hederaHash: String? = null,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "workflow_consolidation_events")
class WorkflowConsolidationEvent(
    @Id
    @Column(name = "event_id", length = 36)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    var workflow: SupplyChainWorkflow,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aggregator_id", nullable = false)
    var aggregator: Aggregator,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id", nullable = false)
    var processor: Processor,

    @Column(name = "quantity_sent_kg", nullable = false, precision = 15, scale = 2)
    var quantitySentKg: BigDecimal,

    @Column(name = "consolidation_date", nullable = false)
    var consolidationDate: LocalDateTime,

    @Column(name = "transport_details", columnDefinition = "TEXT")
    var transportDetails: String? = null,

    @Column(name = "batch_number", length = 100)
    var batchNumber: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "hedera_hash")
    var hederaHash: String? = null,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "workflow_processing_events")
class WorkflowProcessingEvent(
    @Id
    @Column(name = "event_id", length = 36)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    var workflow: SupplyChainWorkflow,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id", nullable = false)
    var processor: Processor,

    @Column(name = "quantity_processed_kg", nullable = false, precision = 15, scale = 2)
    var quantityProcessedKg: BigDecimal,

    @Column(name = "processing_date", nullable = false)
    var processingDate: LocalDateTime,

    @Column(name = "processing_type", length = 100)
    var processingType: String? = null,

    @Column(name = "output_quantity_kg", precision = 15, scale = 2)
    var outputQuantityKg: BigDecimal? = null,

    @Column(name = "processing_notes", columnDefinition = "TEXT")
    var processingNotes: String? = null,

    @Column(name = "hedera_hash")
    var hederaHash: String? = null,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "workflow_shipment_events")
class WorkflowShipmentEvent(
    @Id
    @Column(name = "event_id", length = 36)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    var workflow: SupplyChainWorkflow,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id", nullable = false)
    var processor: Processor,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importer_id", nullable = false)
    var importer: Importer,

    @Column(name = "quantity_shipped_kg", nullable = false, precision = 15, scale = 2)
    var quantityShippedKg: BigDecimal,

    @Column(name = "shipment_date", nullable = false)
    var shipmentDate: LocalDateTime,

    @Column(name = "expected_arrival_date")
    var expectedArrivalDate: LocalDateTime? = null,

    @Column(name = "actual_arrival_date")
    var actualArrivalDate: LocalDateTime? = null,

    @Column(name = "shipping_company")
    var shippingCompany: String? = null,

    @Column(name = "tracking_number")
    var trackingNumber: String? = null,

    @Column(name = "destination_port")
    var destinationPort: String? = null,

    @Column(name = "shipment_notes", columnDefinition = "TEXT")
    var shipmentNotes: String? = null,

    @Column(name = "hedera_hash")
    var hederaHash: String? = null,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
