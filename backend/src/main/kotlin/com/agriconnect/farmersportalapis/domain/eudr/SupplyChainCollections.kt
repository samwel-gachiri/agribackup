package com.agriconnect.farmersportalapis.domain.eudr

import com.agriconnect.farmersportalapis.domain.profile.Farmer
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * FarmerCollection Entity
 * Tracks individual produce collections from farmers by aggregators
 * Maps to: farmer_collections table
 */
@Entity
@Table(name = "farmer_collections")
class FarmerCollection(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "collection_id", length = 36, nullable = false)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aggregator_id", nullable = false)
    var aggregator: Aggregator,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    var farmer: Farmer,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    var batch: EudrBatch? = null,

    @Column(name = "collection_date", nullable = false)
    var collectionDate: LocalDateTime,

    @Column(name = "produce_type", nullable = false)
    var produceType: String,

    @Column(name = "quantity_kg", precision = 15, scale = 2, nullable = false)
    var quantityKg: BigDecimal,

    @Column(name = "quality_grade", length = 50)
    var qualityGrade: String? = null,

    @Column(name = "moisture_content", precision = 5, scale = 2)
    var moistureContent: BigDecimal? = null,

    @Column(name = "price_per_kg", precision = 10, scale = 2)
    var pricePerKg: BigDecimal? = null,

    @Column(name = "total_amount", precision = 15, scale = 2)
    var totalAmount: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50, nullable = false)
    var paymentStatus: PaymentStatus = PaymentStatus.PENDING,

    @Column(name = "payment_method", length = 50)
    var paymentMethod: String? = null,

    @Column(name = "payment_date")
    var paymentDate: LocalDateTime? = null,

    @Column(name = "receipt_number", length = 100)
    var receiptNumber: String? = null,

    @Column(name = "collection_location")
    var collectionLocation: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "hedera_transaction_id", length = 100)
    var hederaTransactionId: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * BatchShipment Entity
 * Tracks international shipments of batches to importers
 * Maps to: batch_shipments table
 */
@Entity
@Table(name = "batch_shipments")
class BatchShipment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "shipment_id", length = 36, nullable = false)
    var id: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false, unique = true)
    var batch: EudrBatch,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "importer_id", nullable = false)
    var importer: Importer,

    @Column(name = "shipment_number", length = 100, nullable = false, unique = true)
    var shipmentNumber: String,

    @Column(name = "origin_country", length = 100, nullable = false)
    var originCountry: String,

    @Column(name = "departure_port")
    var departurePort: String? = null,

    @Column(name = "arrival_port")
    var arrivalPort: String? = null,

    @Column(name = "shipping_date", nullable = false)
    var shippingDate: LocalDate,

    @Column(name = "estimated_arrival_date")
    var estimatedArrivalDate: LocalDate? = null,

    @Column(name = "actual_arrival_date")
    var actualArrivalDate: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "shipment_status", length = 50, nullable = false)
    var shipmentStatus: ShipmentStatus = ShipmentStatus.PENDING,

    @Column(name = "customs_reference_number", length = 100)
    var customsReferenceNumber: String? = null,

    @Column(name = "customs_clearance_date")
    var customsClearanceDate: LocalDate? = null,

    @Column(name = "bill_of_lading_number", length = 100)
    var billOfLadingNumber: String? = null,

    @Column(name = "container_numbers", columnDefinition = "TEXT")
    var containerNumbers: String? = null,

    @Column(name = "transport_method", length = 100)
    var transportMethod: String? = null,

    @Column(name = "transport_company")
    var transportCompany: String? = null,

    @Column(name = "temperature_controlled")
    var temperatureControlled: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "eudr_compliance_status", length = 50)
    var eudrComplianceStatus: EudrComplianceStatus = EudrComplianceStatus.PENDING_VERIFICATION,

    @Column(name = "hedera_shipment_hash")
    var hederaShipmentHash: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "shipment", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var inspections: MutableList<BatchInspection> = mutableListOf()
)

/**
 * BatchInspection Entity
 * Records quality and compliance inspections for batches/shipments
 * Maps to: batch_inspections table
 */
@Entity
@Table(name = "batch_inspections")
class BatchInspection(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "inspection_id", length = 36, nullable = false)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    var batch: EudrBatch? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    var shipment: BatchShipment? = null,

    @Column(name = "inspection_type", length = 100, nullable = false)
    var inspectionType: String,

    @Column(name = "inspection_date", nullable = false)
    var inspectionDate: LocalDate,

    @Column(name = "inspector_name", nullable = false)
    var inspectorName: String,

    @Column(name = "inspector_agency")
    var inspectorAgency: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_result", length = 50, nullable = false)
    var inspectionResult: InspectionResult,

    @Column(name = "findings", columnDefinition = "TEXT")
    var findings: String? = null,

    @Column(name = "recommendations", columnDefinition = "TEXT")
    var recommendations: String? = null,

    @Column(name = "certificate_number", length = 100)
    var certificateNumber: String? = null,

    @Column(name = "hedera_inspection_hash")
    var hederaInspectionHash: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
