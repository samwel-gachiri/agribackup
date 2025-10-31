package com.agriconnect.farmersportalapis.domain.eudr

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Aggregator/Cooperative Entity
 * Represents an organization that collects produce from multiple farmers
 * and creates consolidated batches for processing or export
 */
@Entity
@Table(name = "aggregators")
class Aggregator(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "aggregator_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    var userProfile: UserProfile,

    @Column(name = "organization_name", nullable = false)
    var organizationName: String,

    @Column(name = "organization_type", nullable = false)
    @Enumerated(EnumType.STRING)
    var organizationType: AggregatorType = AggregatorType.COOPERATIVE,

    @Column(name = "registration_number")
    var registrationNumber: String?,

    @Column(name = "facility_address", nullable = false)
    var facilityAddress: String,

    @Column(name = "storage_capacity_tons")
    var storageCapacityTons: BigDecimal?,

    @Column(name = "collection_radius_km")
    var collectionRadiusKm: BigDecimal?,

    @Column(name = "primary_commodities", columnDefinition = "TEXT")
    var primaryCommodities: String?, // JSON array of commodities

    @Column(name = "certification_details", columnDefinition = "TEXT")
    var certificationDetails: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    var verificationStatus: AggregatorVerificationStatus = AggregatorVerificationStatus.PENDING,

    @Column(name = "total_farmers_connected")
    var totalFarmersConnected: Int = 0,

    @Column(name = "total_batches_collected")
    var totalBatchesCollected: Int = 0,

    @Column(name = "hedera_account_id")
    var hederaAccountId: String?,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "aggregator", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var aggregationEvents: MutableList<AggregationEvent> = mutableListOf(),

    @OneToMany(mappedBy = "aggregator", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var consolidatedBatches: MutableList<ConsolidatedBatch> = mutableListOf()
)

enum class AggregatorType {
    COOPERATIVE,
    WAREHOUSE,
    COLLECTION_CENTER,
    FARMER_ASSOCIATION,
    PRIVATE_AGGREGATOR
}

enum class AggregatorVerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED,
    SUSPENDED
}

/**
 * Aggregation Event
 * Tracks individual farmer produce collection by aggregator
 */
@Entity
@Table(name = "aggregation_events")
class AggregationEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aggregator_id", nullable = false)
    var aggregator: Aggregator,

    @Column(name = "farmer_id", nullable = false)
    var farmerId: String,

    @Column(name = "farmer_name")
    var farmerName: String?,

    @Column(name = "produce_type", nullable = false)
    var produceType: String,

    @Column(name = "quantity_kg", nullable = false)
    var quantityKg: BigDecimal,

    @Column(name = "quality_grade")
    var qualityGrade: String?,

    @Column(name = "price_per_kg")
    var pricePerKg: BigDecimal?,

    @Column(name = "total_payment")
    var totalPayment: BigDecimal?,

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    var paymentStatus: PaymentStatus = PaymentStatus.PENDING,

    @Column(name = "collection_date", nullable = false)
    var collectionDate: LocalDateTime,

    @Column(name = "collection_location_gps")
    var collectionLocationGps: String?,

    @Column(name = "moisture_content")
    var moistureContent: BigDecimal?,

    @Column(name = "impurity_percentage")
    var impurityPercentage: BigDecimal?,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String?,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consolidated_batch_id")
    var consolidatedBatch: ConsolidatedBatch?
)

enum class PaymentStatus {
    PENDING,
    PARTIAL,
    PAID,
    OVERDUE,
    CANCELLED
}

/**
 * Consolidated Batch
 * Represents a batch created by aggregator from multiple farmer collections
 */
@Entity
@Table(name = "consolidated_batches")
class ConsolidatedBatch(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "batch_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aggregator_id", nullable = false)
    var aggregator: Aggregator,

    @Column(name = "batch_number", nullable = false, unique = true)
    var batchNumber: String,

    @Column(name = "produce_type", nullable = false)
    var produceType: String,

    @Column(name = "total_quantity_kg", nullable = false)
    var totalQuantityKg: BigDecimal,

    @Column(name = "number_of_farmers")
    var numberOfFarmers: Int = 0,

    @Column(name = "average_quality_grade")
    var averageQualityGrade: String?,

    @Column(name = "consolidation_date", nullable = false)
    var consolidationDate: LocalDateTime,

    @Column(name = "destination_entity_id")
    var destinationEntityId: String?,

    @Column(name = "destination_entity_type")
    var destinationEntityType: String?, // PROCESSOR, EXPORTER, etc.

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: ConsolidatedBatchStatus = ConsolidatedBatchStatus.CREATED,

    @Column(name = "shipment_date")
    var shipmentDate: LocalDateTime?,

    @Column(name = "delivery_date")
    var deliveryDate: LocalDateTime?,

    @Column(name = "transport_details", columnDefinition = "TEXT")
    var transportDetails: String?,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?,

    @Column(name = "hedera_batch_hash")
    var hederaBatchHash: String?,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "consolidatedBatch", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var aggregationEvents: MutableList<AggregationEvent> = mutableListOf()
)

enum class ConsolidatedBatchStatus {
    CREATED,
    QUALITY_CHECKED,
    READY_FOR_SHIPMENT,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}
