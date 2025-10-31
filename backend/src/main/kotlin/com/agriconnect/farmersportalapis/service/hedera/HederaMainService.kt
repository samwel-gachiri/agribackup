package com.agriconnect.farmersportalapis.service.hedera

import com.agriconnect.farmersportalapis.domain.eudr.*
import com.hedera.hashgraph.sdk.AccountId
import com.hedera.hashgraph.sdk.TokenId
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class HederaMainService(
    private val hcs: HederaConsensusServices,
    private val hts: HederaTokenService
) {
    /* ==== SUPPLY CHAIN EVENTS FROM FARM TO EXPORT === */

    // recoding a deforestation free farm land / production unit that has been verified by satelites
    fun recordProductionUnitVerification(productionUnit: ProductionUnit) = hcs.recordProductionUnitVerification(productionUnit)

    // Record aggregation / collection event from farm by Aggregators
    fun recordAggregationEvent(
        eventId: String,
        aggregatorId: String,
        farmerId: String,
        produceType: String,
        quantityKg: BigDecimal,
        collectionDate: LocalDate
    )  = hcs.recordAggregationEvent(eventId, aggregatorId,farmerId, produceType, quantityKg, collectionDate)

    // Recording consolidated batch from multiple aggregators
    fun recordConsolidatedBatch(
        batchId: String,
        batchNumber: String,
        aggregatorId: String,
        produceType: String,
        totalQuantityKg: BigDecimal,
        numberOfFarmers: Int,
        batchDataHash: String
    ) = hcs.recordConsolidatedBatch(batchId, batchNumber, aggregatorId, produceType, totalQuantityKg,numberOfFarmers,batchDataHash)

    // Batches can also be taken to processors
    fun recordProcessingEvent(event: ProcessingEvent) = hcs.recordProcessingEvent(event)

    // Record risk assessment result when it happens
    fun recordRiskAssessment(
        batchId: String,
        riskLevel: RiskLevel,
        rationale: String,
        assessedBy: String
    ) = hcs.recordRiskAssessment(batchId, riskLevel, rationale, assessedBy)

    // Record Import shipment
    fun recordImportShipment(
        shipmentId: String,
        importerId: String,
        shipmentNumber: String,
        produceType: String,
        quantityKg: BigDecimal,
        originCountry: String,
        shipmentDataHash: String
    ) = hcs.recordImportShipment(shipmentId, importerId, shipmentNumber, produceType, quantityKg, originCountry, shipmentDataHash)

    // once shipped the shipment will be inspected and information will be recorded
    fun recordInspectionResult(
        inspectionId: String,
        shipmentId: String,
        documentType: String,
        documentHash: String
    ) = hcs.recordCustomsDocument(inspectionId, shipmentId, documentType, documentHash)

    // Record the WHOLE EUDR batch information
    fun recordBatchCreation(batch: EudrBatch) = hcs.recordBatchCreation(batch)

    // Record document upload e.g. Exporter's License
    fun recordDocumentUpload(document: EudrDocument) = hcs.recordDocumentUpload(document)

    /**
     * Create EUDR Compliance Certificate NFT Collection
     * This is a one-time setup to create the NFT collection
     * Each certificate will be minted as a unique NFT within this collection
     */
    fun createEudrComplianceCertificateNft() = hts.createEudrComplianceCertificateNft()

    /**
     * Issue EUDR Compliance Certificate NFT for a shipment
     *
     * This NFT is proof of compliance, NOT a reward
     * Issued ONLY when shipment passes ALL EUDR checks:
     * - GPS coordinates captured from all production units
     * - Deforestation-free verification completed
     * - Complete supply chain traceability (farmer → aggregator → processor → exporter)
     * - Risk assessment passed (origin country, data completeness)
     * - Due diligence statement generated
     *
     * @param shipmentId Unique shipment identifier
     * @param exporterAccountId Hedera account of the exporter
     * @param complianceData Map containing:
     *        - originCountry
     *        - riskLevel (LOW/MEDIUM/HIGH)
     *        - totalFarmers
     *        - totalProductionUnits
     *        - gpsCoordinatesCount
     *        - deforestationStatus (VERIFIED_FREE)
     *        - traceabilityHash
     * @return Hedera transaction ID of the NFT issuance
     */
    fun issueComplianceCertificateNft(
        shipmentId: String,
        exporterAccountId: AccountId,
        complianceData: Map<String, String>
    ) = hts.issueComplianceCertificateNft(shipmentId , exporterAccountId, complianceData)

    /**
     * Transfer EUDR Compliance Certificate NFT to another account
     * Use case: Exporter transfers NFT to importer with the physical shipment
     *
     * @param fromAccountId Current holder of the NFT
     * @param toAccountId Recipient of the NFT (importer)
     * @param shipmentId Shipment identifier for logging
     * @return true if transfer succeeded
     */
    fun transferComplianceCertificateNft(
        fromAccountId: AccountId,
        toAccountId: AccountId,
        shipmentId: String
    ) = hts.transferComplianceCertificateNft(fromAccountId, toAccountId, shipmentId)
}
