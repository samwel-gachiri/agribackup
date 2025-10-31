package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "eudr_batches")
class EudrBatch(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "batch_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @Column(name = "batch_code", unique = true, nullable = false)
    var batchCode: String,

    @Column(name = "commodity_description", nullable = false)
    var commodityDescription: String,

    @Column(name = "hs_code")
    var hsCode: String?,

    @Column(name = "quantity", nullable = false)
    var quantity: BigDecimal,

    @Column(name = "unit", nullable = false)
    var unit: String,

    @Column(name = "country_of_production", nullable = false)
    var countryOfProduction: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "country_risk_level", nullable = false)
    var countryRiskLevel: CountryRiskLevel,

    @Column(name = "harvest_date")
    var harvestDate: LocalDate?,

    @Column(name = "harvest_period_start")
    var harvestPeriodStart: LocalDate?,

    @Column(name = "harvest_period_end")
    var harvestPeriodEnd: LocalDate?,

    @Column(name = "created_by", nullable = false)
    var createdBy: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aggregator_id")
    var aggregator: Aggregator? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id")
    var processor: Processor? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: BatchStatus = BatchStatus.CREATED,

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level")
    var riskLevel: RiskLevel?,

    @Column(name = "risk_rationale", columnDefinition = "TEXT")
    var riskRationale: String?,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?,

    @Column(name = "hedera_consensus_timestamp")
    var hederaConsensusTimestamp: Instant?,

    @OneToMany(mappedBy = "batch", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var productionUnits: MutableList<BatchProductionUnit> = mutableListOf(),

    @OneToMany(mappedBy = "batch", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var supplyChainEvents: MutableList<SupplyChainEvent> = mutableListOf(),

    @OneToMany(mappedBy = "batch", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var documents: MutableList<EudrDocument> = mutableListOf(),

    @OneToMany(mappedBy = "batch", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var processingEvents: MutableList<ProcessingEvent> = mutableListOf(),

    @OneToMany(mappedBy = "batch", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var farmerCollections: MutableList<FarmerCollection> = mutableListOf(),

    @OneToOne(mappedBy = "batch", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var shipment: BatchShipment? = null,

    @OneToMany(mappedBy = "batch", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var inspections: MutableList<BatchInspection> = mutableListOf()
)

enum class CountryRiskLevel {
    LOW, STANDARD, HIGH
}

enum class BatchStatus {
    CREATED, IN_TRANSIT, PROCESSING, PROCESSED, EXPORTED, DELIVERED, REJECTED
}

enum class RiskLevel {
    NONE, LOW, MEDIUM, HIGH
}