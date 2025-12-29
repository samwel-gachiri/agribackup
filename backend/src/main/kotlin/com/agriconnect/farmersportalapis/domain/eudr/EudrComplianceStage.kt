package com.agriconnect.farmersportalapis.domain.eudr

import java.time.LocalDateTime

/**
 * EUDR Compliance Workflow Stages
 * 
 * The EU Deforestation Regulation (EUDR) requires operators and traders to demonstrate
 * that products placed on the EU market are:
 * 1. Deforestation-free
 * 2. Legally produced
 * 3. Covered by a Due Diligence Statement (DDS)
 * 
 * This enum defines the complete workflow from production to market access.
 */
enum class EudrComplianceStage(
    val order: Int,
    val displayName: String,
    val description: String,
    val requiredActions: List<String>,
    val automatedActions: List<String>,
    val nextStage: String?,
    val previousStage: String?
) {
    // STAGE 1: Production Registration
    PRODUCTION_REGISTRATION(
        order = 1,
        displayName = "Production Registration",
        description = "Register production units (farms/plots) with geolocation data for traceability",
        requiredActions = listOf(
            "Register production unit with GPS coordinates",
            "Upload land ownership/use documents",
            "Specify commodity type and production capacity",
            "Link to farmer profile"
        ),
        automatedActions = listOf(
            "Generate unique production unit ID",
            "Record on Hedera blockchain",
            "Calculate plot area from coordinates"
        ),
        nextStage = "GEOLOCATION_VERIFICATION",
        previousStage = null
    ),
    
    // STAGE 2: Geolocation Verification
    GEOLOCATION_VERIFICATION(
        order = 2,
        displayName = "Geolocation Verification",
        description = "Verify that production coordinates are accurate and within registered boundaries",
        requiredActions = listOf(
            "Review plotted coordinates on map",
            "Confirm boundary accuracy",
            "Upload supporting evidence (satellite imagery, field photos)"
        ),
        automatedActions = listOf(
            "Cross-reference with national land registries",
            "Validate coordinate format and precision",
            "Record verification on blockchain"
        ),
        nextStage = "DEFORESTATION_CHECK",
        previousStage = "PRODUCTION_REGISTRATION"
    ),
    
    // STAGE 3: Deforestation Risk Check
    DEFORESTATION_CHECK(
        order = 3,
        displayName = "Deforestation Risk Assessment",
        description = "Check if production area has deforestation alerts using satellite monitoring",
        requiredActions = listOf(
            "Review deforestation check results",
            "Address any flagged alerts",
            "Provide evidence if alerts are false positives"
        ),
        automatedActions = listOf(
            "Query Global Forest Watch API",
            "Analyze satellite imagery for forest loss",
            "Calculate risk score based on proximity to protected areas",
            "Generate deforestation-free certificate if passed"
        ),
        nextStage = "COLLECTION_AGGREGATION",
        previousStage = "GEOLOCATION_VERIFICATION"
    ),
    
    // STAGE 4: Collection & Aggregation
    COLLECTION_AGGREGATION(
        order = 4,
        displayName = "Collection & Aggregation",
        description = "Collect produce from verified production units and consolidate at aggregation points",
        requiredActions = listOf(
            "Record collection from each production unit",
            "Specify quantity, quality grade, and collection date",
            "Assign to aggregator/collection center",
            "Generate batch number for consolidated produce"
        ),
        automatedActions = listOf(
            "Calculate total quantity from linked production units",
            "Verify all source units passed deforestation check",
            "Record aggregation event on blockchain",
            "Generate traceability QR code"
        ),
        nextStage = "PROCESSING",
        previousStage = "DEFORESTATION_CHECK"
    ),
    
    // STAGE 5: Processing
    PROCESSING(
        order = 5,
        displayName = "Processing",
        description = "Process raw commodities (if applicable) while maintaining chain of custody",
        requiredActions = listOf(
            "Record input quantity from aggregation",
            "Specify processing type and output quantity",
            "Maintain mass balance records",
            "Link output batch to input batches"
        ),
        automatedActions = listOf(
            "Verify processor is certified and registered",
            "Calculate processing yield/conversion rate",
            "Record processing event on blockchain",
            "Update batch status"
        ),
        nextStage = "RISK_ASSESSMENT",
        previousStage = "COLLECTION_AGGREGATION"
    ),
    
    // STAGE 6: Overall Risk Assessment
    RISK_ASSESSMENT(
        order = 6,
        displayName = "Risk Assessment",
        description = "Conduct comprehensive risk assessment based on country, product, and supply chain complexity",
        requiredActions = listOf(
            "Review automated risk score",
            "Address any high-risk factors identified",
            "Implement additional mitigation measures if needed"
        ),
        automatedActions = listOf(
            "Calculate country risk level (EU benchmarking)",
            "Assess supply chain complexity score",
            "Evaluate historical compliance data",
            "Generate overall risk classification (NEGLIGIBLE, LOW, STANDARD, HIGH)"
        ),
        nextStage = "DUE_DILIGENCE_STATEMENT",
        previousStage = "PROCESSING"
    ),
    
    // STAGE 7: Due Diligence Statement
    DUE_DILIGENCE_STATEMENT(
        order = 7,
        displayName = "Due Diligence Statement (DDS)",
        description = "Generate and submit the Due Diligence Statement required for EU market access",
        requiredActions = listOf(
            "Review all collected information",
            "Verify completeness of documentation",
            "Sign/authorize the DDS",
            "Submit to EU Information System (if direct submission)"
        ),
        automatedActions = listOf(
            "Compile all supply chain data into DDS format",
            "Generate unique DDS reference number",
            "Hash and record DDS on blockchain",
            "Generate PDF dossier for records"
        ),
        nextStage = "EXPORT_SHIPMENT",
        previousStage = "RISK_ASSESSMENT"
    ),
    
    // STAGE 8: Export & Shipment
    EXPORT_SHIPMENT(
        order = 8,
        displayName = "Export & Shipment",
        description = "Ship products to EU destination with all compliance documentation",
        requiredActions = listOf(
            "Create shipment with destination details",
            "Attach DDS reference to shipping documents",
            "Notify importer of shipment",
            "Provide customs reference numbers"
        ),
        automatedActions = listOf(
            "Generate EUDR compliance certificate NFT",
            "Transfer certificate ownership to shipment",
            "Record export event on blockchain",
            "Update batch status to IN_TRANSIT"
        ),
        nextStage = "CUSTOMS_CLEARANCE",
        previousStage = "DUE_DILIGENCE_STATEMENT"
    ),
    
    // STAGE 9: Customs Clearance
    CUSTOMS_CLEARANCE(
        order = 9,
        displayName = "Customs Clearance",
        description = "Clear customs at EU port of entry with EUDR documentation",
        requiredActions = listOf(
            "Provide DDS to customs authorities",
            "Present compliance certificate",
            "Address any customs queries"
        ),
        automatedActions = listOf(
            "Verify DDS with EU Information System",
            "Validate compliance certificate NFT",
            "Record customs verification on blockchain"
        ),
        nextStage = "DELIVERY_COMPLETE",
        previousStage = "EXPORT_SHIPMENT"
    ),
    
    // STAGE 10: Delivery Complete
    DELIVERY_COMPLETE(
        order = 10,
        displayName = "Delivery Complete",
        description = "Goods delivered to importer, compliance cycle complete",
        requiredActions = listOf(
            "Confirm receipt by importer",
            "Archive all documentation for 5-year retention",
            "Transfer certificate ownership to importer"
        ),
        automatedActions = listOf(
            "Mark workflow as COMPLETED",
            "Transfer NFT certificate to importer",
            "Generate final compliance report",
            "Archive on blockchain for audit trail"
        ),
        nextStage = null,
        previousStage = "CUSTOMS_CLEARANCE"
    );
    
    companion object {
        fun fromOrder(order: Int): EudrComplianceStage? = entries.find { it.order == order }
        fun fromName(name: String): EudrComplianceStage? = entries.find { it.name == name }
        fun getFirstStage(): EudrComplianceStage = PRODUCTION_REGISTRATION
        fun getLastStage(): EudrComplianceStage = DELIVERY_COMPLETE
    }
}

/**
 * Risk Classification per EUDR Article 29
 */
enum class EudrRiskClassification(
    val displayName: String,
    val description: String,
    val requiredDueDiligence: String
) {
    NEGLIGIBLE(
        displayName = "Negligible Risk",
        description = "Country classified as low-risk by EU Commission",
        requiredDueDiligence = "Simplified due diligence - basic information collection"
    ),
    LOW(
        displayName = "Low Risk",
        description = "No significant risk indicators identified",
        requiredDueDiligence = "Standard due diligence with documented checks"
    ),
    STANDARD(
        displayName = "Standard Risk",
        description = "Normal risk level requiring full due diligence",
        requiredDueDiligence = "Full due diligence including enhanced verification"
    ),
    HIGH(
        displayName = "High Risk",
        description = "Elevated risk requiring additional mitigation",
        requiredDueDiligence = "Enhanced due diligence with third-party verification and ongoing monitoring"
    )
}

/**
 * Workflow Stage Progress Tracker
 */
data class StageProgress(
    val stage: EudrComplianceStage,
    val status: StageStatus,
    val startedAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val completionPercentage: Int,
    val pendingActions: List<String>,
    val completedActions: List<String>,
    val blockers: List<String>,
    val notes: String?
)

enum class StageStatus {
    NOT_STARTED,
    IN_PROGRESS,
    PENDING_REVIEW,
    BLOCKED,
    COMPLETED,
    SKIPPED
}
