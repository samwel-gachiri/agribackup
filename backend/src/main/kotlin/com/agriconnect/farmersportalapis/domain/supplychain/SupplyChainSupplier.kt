package com.agriconnect.farmersportalapis.domain.supplychain

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Flexible Supplier entity for multi-tier supply chain support
 * 
 * This entity supports:
 * - Multiple supplier types (farmer, aggregator, cooperative, trader, etc.)
 * - Sub-supplier relationships (hierarchical supply chains)
 * - Direct connections to workflows
 * - Flexible metadata for different commodity types
 */
@Entity
@Table(name = "supply_chain_suppliers")
class SupplyChainSupplier(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "supplier_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id")
    var userProfile: UserProfile? = null,

    @Column(name = "supplier_name", nullable = false)
    var supplierName: String,

    @Column(name = "supplier_code", unique = true)
    var supplierCode: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "supplier_type", nullable = false, length = 50)
    var supplierType: SupplierType,

    @Column(name = "business_registration_number")
    var businessRegistrationNumber: String? = null,

    @Column(name = "tax_id")
    var taxId: String? = null,

    @Column(name = "country_code", nullable = false, length = 3)
    var countryCode: String,

    @Column(name = "region")
    var region: String? = null,

//    @Column(name = "address", columnDefinition = "TEXT")
//    var address: String? = null,

    @Column(name = "gps_latitude")
    var gpsLatitude: Double? = null,

    @Column(name = "gps_longitude")
    var gpsLongitude: Double? = null,

    @Column(name = "commodities_handled", columnDefinition = "TEXT")
    var commoditiesHandled: String? = null, // Comma-separated list

    @Column(name = "certifications", columnDefinition = "TEXT")
    var certifications: String? = null, // JSON or comma-separated

    @Column(name = "capacity_per_month_kg", precision = 15, scale = 2)
    var capacityPerMonthKg: BigDecimal? = null,

    @Column(name = "hedera_account_id", length = 50)
    var hederaAccountId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", length = 50)
    var verificationStatus: SupplierVerificationStatus = SupplierVerificationStatus.PENDING,

    @Column(name = "verified_at")
    var verifiedAt: LocalDateTime? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    // Direct connection to an exporter (for exporter-supplier relationships)
    @Column(name = "connected_exporter_id")
    var connectedExporterId: String? = null,

    // Self-referencing for sub-supplier hierarchy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_supplier_id")
    var parentSupplier: SupplyChainSupplier? = null,

    @OneToMany(mappedBy = "parentSupplier", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var subSuppliers: MutableList<SupplyChainSupplier> = mutableListOf(),

    // Supplier relationships in workflows
    @OneToMany(mappedBy = "fromSupplier", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var outgoingConnections: MutableList<SupplierConnection> = mutableListOf(),

    @OneToMany(mappedBy = "toSupplier", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var incomingConnections: MutableList<SupplierConnection> = mutableListOf()
) {
    /**
     * Get all ancestors in the supply chain hierarchy
     */
    fun getAncestors(): List<SupplyChainSupplier> {
        val ancestors = mutableListOf<SupplyChainSupplier>()
        var current = parentSupplier
        while (current != null) {
            ancestors.add(current)
            current = current.parentSupplier
        }
        return ancestors
    }

    /**
     * Get hierarchy level (0 = root, 1 = direct sub-supplier, etc.)
     */
    fun getHierarchyLevel(): Int = getAncestors().size

    /**
     * Check if this supplier is a sub-supplier of another
     */
    fun isSubSupplierOf(supplier: SupplyChainSupplier): Boolean {
        return getAncestors().any { it.id == supplier.id }
    }

    /**
     * Get contact email from the linked UserProfile
     */
    fun getEmail(): String? = userProfile?.email

    /**
     * Get contact phone from the linked UserProfile
     */
    fun getPhoneNumber(): String? = userProfile?.phoneNumber

    /**
     * Get contact person name from the linked UserProfile
     */
    fun getContactPerson(): String? = userProfile?.fullName

    /**
     * Check if this supplier is of a specific type
     */
    fun isType(type: SupplierType): Boolean = supplierType == type

    /**
     * Check if this supplier can act as a source in the supply chain
     */
    fun canBeSource(): Boolean = supplierType in listOf(
        SupplierType.FARMER,
        SupplierType.FARMER_GROUP,
        SupplierType.COOPERATIVE,
        SupplierType.AGGREGATOR,
        SupplierType.TRADER,
        SupplierType.PROCESSOR,
        SupplierType.WAREHOUSE,
        SupplierType.EXPORTER
    )

    /**
     * Check if this supplier can act as a destination in the supply chain
     */
    fun canBeDestination(): Boolean = supplierType in listOf(
        SupplierType.AGGREGATOR,
        SupplierType.TRADER,
        SupplierType.PROCESSOR,
        SupplierType.DISTRIBUTOR,
        SupplierType.WAREHOUSE,
        SupplierType.EXPORTER,
        SupplierType.IMPORTER,
        SupplierType.WHOLESALER,
        SupplierType.RETAILER
    )
}

/**
 * Connection between suppliers in a workflow
 * Represents transfer of goods from one supplier to another
 */
@Entity
@Table(name = "supplier_connections")
class SupplierConnection(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "connection_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workflow_id", nullable = false)
    var workflow: SupplyChainWorkflow,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_supplier_id")
    var fromSupplier: SupplyChainSupplier? = null, // Null for origin (farm)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_supplier_id", nullable = false)
    var toSupplier: SupplyChainSupplier,

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_type", nullable = false, length = 50)
    var connectionType: ConnectionType,

    @Column(name = "quantity_kg", nullable = false, precision = 15, scale = 2)
    var quantityKg: BigDecimal,

    @Column(name = "transfer_date", nullable = false)
    var transferDate: LocalDateTime,

    @Column(name = "batch_reference")
    var batchReference: String? = null,

    @Column(name = "transport_method")
    var transportMethod: String? = null,

    @Column(name = "transport_document_ref")
    var transportDocumentRef: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null,

    @Column(name = "hedera_transaction_id", length = 100)
    var hederaTransactionId: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

enum class SupplierType {
    FARMER,             // Individual farmer
    FARMER_GROUP,       // Group of farmers
    COOPERATIVE,        // Farmer cooperative
    AGGREGATOR,         // Produce aggregator
    TRADER,             // Independent trader
    PROCESSOR,          // Processing facility
    EXPORTER,           // Export company
    IMPORTER,           // Import company
    DISTRIBUTOR,        // Distribution company
    WHOLESALER,         // Wholesale company
    RETAILER,           // Retail company
    WAREHOUSE,          // Storage facility
    TRANSPORTER,        // Logistics provider
    CERTIFICATION_BODY, // Certification organization
    OTHER               // Other supplier type
}

enum class SupplierVerificationStatus {
    PENDING,
    DOCUMENTS_SUBMITTED,
    UNDER_REVIEW,
    VERIFIED,
    REJECTED,
    SUSPENDED,
    EXPIRED
}

enum class ConnectionType {
    HARVEST,            // Initial harvest from farm
    COLLECTION,         // Collection from farmer/cooperative
    AGGREGATION,        // Aggregation from multiple sources
    TRANSPORT,          // In-transit handoff
    STORAGE,            // Warehouse receipt
    PROCESSING,         // Processing facility intake
    QUALITY_CHECK,      // Quality inspection point
    CONSOLIDATION,      // Batch consolidation
    EXPORT,             // Export from origin country
    IMPORT,             // Import to destination country
    DISTRIBUTION,       // Distribution handoff
    DELIVERY            // Final delivery
}
