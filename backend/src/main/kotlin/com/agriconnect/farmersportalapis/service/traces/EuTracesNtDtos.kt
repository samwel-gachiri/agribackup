package com.agriconnect.farmersportalapis.service.traces

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTOs for EU TRACES NT Due Diligence Statement (DDS)
 * 
 * These models represent the data structure required for EUDR compliance
 * submissions to the EU TRACES NT system.
 */

// ============================================================================
// MAIN DDS STRUCTURE
// ============================================================================

/**
 * Complete Due Diligence Statement for TRACES NT submission
 */
@JacksonXmlRootElement(localName = "DueDiligenceStatement", namespace = "http://ec.europa.eu/traces/eudr/dds/v2")
data class DueDiligenceStatement(
    @JacksonXmlProperty(localName = "Header")
    val header: DdsHeader,
    
    @JacksonXmlProperty(localName = "Operator")
    val operator: OperatorInfo,
    
    @JacksonXmlProperty(localName = "Product")
    val product: ProductInfo,
    
    @JacksonXmlProperty(localName = "CountryOfProduction")
    val countryOfProduction: String,
    
    @JacksonXmlProperty(localName = "Geolocation")
    val geolocation: GeolocationData,
    
    @JacksonXmlProperty(localName = "SupplyChain")
    val supplyChain: SupplyChainInfo,
    
    @JacksonXmlProperty(localName = "RiskAssessment")
    val riskAssessment: RiskAssessmentInfo,
    
    @JacksonXmlProperty(localName = "Declaration")
    val declaration: DeclarationInfo
)

/**
 * DDS Header information
 */
data class DdsHeader(
    @JacksonXmlProperty(localName = "InternalReference")
    val internalReference: String,
    
    @JacksonXmlProperty(localName = "SubmissionDate")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    val submissionDate: LocalDateTime,
    
    @JacksonXmlProperty(localName = "StatementVersion")
    val statementVersion: Int = 1,
    
    @JacksonXmlProperty(localName = "TracesReferenceNumber")
    val tracesReferenceNumber: String? = null,
    
    @JacksonXmlProperty(localName = "VerificationNumber")
    val verificationNumber: String? = null
)

// ============================================================================
// OPERATOR INFORMATION
// ============================================================================

data class OperatorInfo(
    @JacksonXmlProperty(localName = "Name")
    val name: String,
    
    @JacksonXmlProperty(localName = "Address")
    val address: AddressInfo,
    
    @JacksonXmlProperty(localName = "EORI")
    val eori: String? = null,
    
    @JacksonXmlProperty(localName = "VATNumber")
    val vatNumber: String? = null,
    
    @JacksonXmlProperty(localName = "ActivityType")
    val activityType: ActivityType,
    
    @JacksonXmlProperty(localName = "AuthorizedSignatory")
    val authorizedSignatory: SignatoryInfo
)

data class AddressInfo(
    @JacksonXmlProperty(localName = "Street")
    val street: String,
    
    @JacksonXmlProperty(localName = "PostalCode")
    val postalCode: String? = null,
    
    @JacksonXmlProperty(localName = "City")
    val city: String,
    
    @JacksonXmlProperty(localName = "Country")
    val country: String
)

data class SignatoryInfo(
    @JacksonXmlProperty(localName = "Name")
    val name: String,
    
    @JacksonXmlProperty(localName = "Position")
    val position: String? = null,
    
    @JacksonXmlProperty(localName = "Email")
    val email: String? = null,
    
    @JacksonXmlProperty(localName = "Phone")
    val phone: String? = null
)

enum class ActivityType {
    @JsonProperty("PLACING_ON_MARKET")
    PLACING_ON_MARKET,          // Import to EU
    
    @JsonProperty("MAKING_AVAILABLE")
    MAKING_AVAILABLE,           // Trading within EU
    
    @JsonProperty("EXPORT")
    EXPORT,                     // Export from EU
    
    @JsonProperty("DOMESTIC_PRODUCTION")
    DOMESTIC_PRODUCTION         // Produced in EU
}

// ============================================================================
// PRODUCT INFORMATION
// ============================================================================

data class ProductInfo(
    @JacksonXmlProperty(localName = "CommodityCategory")
    val commodityCategory: EudrCommodity,
    
    @JacksonXmlProperty(localName = "HSCode")
    val hsCode: String,
    
    @JacksonXmlProperty(localName = "ScientificName")
    val scientificName: String? = null,
    
    @JacksonXmlProperty(localName = "CommonName")
    val commonName: String,
    
    @JacksonXmlProperty(localName = "Description")
    val description: String,
    
    @JacksonXmlProperty(localName = "Quantity")
    val quantity: QuantityInfo,
    
    @JacksonXmlProperty(localName = "HarvestPeriod")
    val harvestPeriod: HarvestPeriod? = null,
    
    @JacksonXmlProperty(localName = "ProcessingMethod")
    val processingMethod: String? = null
)

data class QuantityInfo(
    @JacksonXmlProperty(localName = "Value")
    val value: Double,
    
    @JacksonXmlProperty(localName = "Unit")
    val unit: String = "KGM"  // ISO unit code for kilograms
)

data class HarvestPeriod(
    @JacksonXmlProperty(localName = "Start")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val start: LocalDate,
    
    @JacksonXmlProperty(localName = "End")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val end: LocalDate
)

/**
 * EUDR regulated commodities (Annex I)
 */
enum class EudrCommodity {
    CATTLE,
    COCOA,
    COFFEE,
    OIL_PALM,
    RUBBER,
    SOYA,
    WOOD
}

// ============================================================================
// GEOLOCATION DATA
// ============================================================================

data class GeolocationData(
    @JacksonXmlProperty(localName = "TotalPlots")
    val totalPlots: Int,
    
    @JacksonXmlProperty(localName = "TotalAreaHectares")
    val totalAreaHectares: Double,
    
    @JacksonXmlProperty(localName = "GeoJSONReference")
    val geoJsonReference: String? = null,
    
    @JacksonXmlProperty(localName = "Plots")
    @JacksonXmlElementWrapper(useWrapping = true)
    val plots: List<PlotInfo>
)

data class PlotInfo(
    @JacksonXmlProperty(localName = "PlotId")
    val plotId: String,
    
    @JacksonXmlProperty(localName = "FarmName")
    val farmName: String? = null,
    
    @JacksonXmlProperty(localName = "FarmerName")
    val farmerName: String? = null,
    
    @JacksonXmlProperty(localName = "AreaHectares")
    val areaHectares: Double,
    
    @JacksonXmlProperty(localName = "Centroid")
    val centroid: CoordinateInfo,
    
    @JacksonXmlProperty(localName = "HarvestDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val harvestDate: LocalDate? = null
)

data class CoordinateInfo(
    @JacksonXmlProperty(localName = "Latitude")
    val latitude: Double,
    
    @JacksonXmlProperty(localName = "Longitude")
    val longitude: Double
)

// ============================================================================
// SUPPLY CHAIN INFORMATION
// ============================================================================

data class SupplyChainInfo(
    @JacksonXmlProperty(localName = "Actor")
    @JacksonXmlElementWrapper(useWrapping = false)
    val actors: List<SupplyChainActor>,
    
    @JacksonXmlProperty(localName = "BatchReferences")
    val batchReferences: BatchReferences? = null
)

data class SupplyChainActor(
    @JacksonXmlProperty(localName = "Role")
    val role: ActorRole,
    
    @JacksonXmlProperty(localName = "Name")
    val name: String,
    
    @JacksonXmlProperty(localName = "Registration")
    val registration: String? = null,
    
    @JacksonXmlProperty(localName = "Country")
    val country: String,
    
    @JacksonXmlProperty(localName = "Facility")
    val facility: String? = null,
    
    @JacksonXmlProperty(localName = "ProcessingDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val processingDate: LocalDate? = null
)

enum class ActorRole {
    PRODUCER,
    AGGREGATOR,
    PROCESSOR,
    EXPORTER,
    TRADER,
    IMPORTER
}

data class BatchReferences(
    @JacksonXmlProperty(localName = "AgriBackupWorkflowId")
    val agriBackupWorkflowId: String? = null,
    
    @JacksonXmlProperty(localName = "InternalBatchCode")
    val internalBatchCode: String? = null,
    
    @JacksonXmlProperty(localName = "ExportLotNumber")
    val exportLotNumber: String? = null,
    
    @JacksonXmlProperty(localName = "HederaTransactionId")
    val hederaTransactionId: String? = null
)

// ============================================================================
// RISK ASSESSMENT INFORMATION
// ============================================================================

data class RiskAssessmentInfo(
    @JacksonXmlProperty(localName = "OverallRiskLevel")
    val overallRiskLevel: DdsRiskLevel,
    
    @JacksonXmlProperty(localName = "DeforestationFreeStatus")
    val deforestationFreeStatus: DeforestationStatus,
    
    @JacksonXmlProperty(localName = "CutoffDateCompliance")
    val cutoffDateCompliance: Boolean,
    
    @JacksonXmlProperty(localName = "VerificationMethods")
    @JacksonXmlElementWrapper(useWrapping = true)
    val verificationMethods: List<VerificationMethod>,
    
    @JacksonXmlProperty(localName = "Certifications")
    @JacksonXmlElementWrapper(useWrapping = true)
    val certifications: List<CertificationInfo>? = null,
    
    @JacksonXmlProperty(localName = "MitigationActions")
    val mitigationActions: String? = null
)

enum class DdsRiskLevel {
    NEGLIGIBLE,
    LOW,
    STANDARD,
    HIGH
}

enum class DeforestationStatus {
    CONFIRMED,
    UNCONFIRMED,
    PENDING_VERIFICATION
}

enum class VerificationMethod {
    SATELLITE_IMAGERY,
    GLOBAL_FOREST_WATCH,
    THIRD_PARTY_AUDIT,
    ON_GROUND_VERIFICATION,
    SELF_DECLARATION,
    CERTIFICATION_SCHEME
}

data class CertificationInfo(
    @JacksonXmlProperty(localName = "Scheme")
    val scheme: String,
    
    @JacksonXmlProperty(localName = "CertificateNumber")
    val certificateNumber: String,
    
    @JacksonXmlProperty(localName = "ValidUntil")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val validUntil: LocalDate? = null
)

// ============================================================================
// DECLARATION
// ============================================================================

data class DeclarationInfo(
    @JacksonXmlProperty(localName = "DeforestationFreeCompliance")
    val deforestationFreeCompliance: Boolean,
    
    @JacksonXmlProperty(localName = "LegalityCompliance")
    val legalityCompliance: Boolean,
    
    @JacksonXmlProperty(localName = "DueDiligenceExercised")
    val dueDiligenceExercised: Boolean,
    
    @JacksonXmlProperty(localName = "SignatureDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    val signatureDate: LocalDate,
    
    @JacksonXmlProperty(localName = "BlockchainReference")
    val blockchainReference: BlockchainReference? = null
)

data class BlockchainReference(
    @JacksonXmlProperty(localName = "Platform")
    val platform: String = "HEDERA",
    
    @JacksonXmlProperty(localName = "TransactionId")
    val transactionId: String? = null,
    
    @JacksonXmlProperty(localName = "NftTokenId")
    val nftTokenId: String? = null,
    
    @JacksonXmlProperty(localName = "NftSerialNumber")
    val nftSerialNumber: Long? = null
)

// ============================================================================
// API RESPONSE MODELS
// ============================================================================

/**
 * Response from TRACES NT after DDS submission
 */
data class TracesSubmissionResponse(
    val success: Boolean,
    val tracesReferenceNumber: String?,
    val verificationNumber: String?,
    val status: String?,
    val message: String?,
    val submittedAt: LocalDateTime?
)

/**
 * Status check response from TRACES NT
 */
data class TracesStatusResponse(
    val referenceNumber: String,
    val status: TracesSubmissionStatus,
    val lastUpdated: LocalDateTime?,
    val authorityFeedback: String?,
    val validUntil: LocalDate?
)

enum class TracesSubmissionStatus {
    PENDING,
    RECEIVED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    EXPIRED,
    CANCELLED
}

// ============================================================================
// GEOJSON MODELS
// ============================================================================

/**
 * GeoJSON FeatureCollection for production plots
 */
data class ProductionPlotsGeoJson(
    val type: String = "FeatureCollection",
    val name: String,
    val features: List<PlotFeature>
)

data class PlotFeature(
    val type: String = "Feature",
    val properties: PlotProperties,
    val geometry: PlotGeometry
)

data class PlotProperties(
    val plotId: String,
    val farmName: String?,
    val farmerName: String?,
    val areaHectares: Double,
    val commodityType: String,
    val harvestDate: String?
)

data class PlotGeometry(
    val type: String,  // "Polygon" or "Point"
    val coordinates: Any  // List<List<List<Double>>> for Polygon, List<Double> for Point
)
