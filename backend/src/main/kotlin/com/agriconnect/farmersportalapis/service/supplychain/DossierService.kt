package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.domain.eudr.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.AuditLogRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrBatchRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrDocumentRepository
import com.agriconnect.farmersportalapis.service.common.RiskAssessmentResult
import com.agriconnect.farmersportalapis.service.common.RiskAssessmentService
import com.agriconnect.farmersportalapis.service.hedera.IpfsDocumentService
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import java.awt.Color
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
@Transactional(readOnly = true)
class DossierService(
    private val eudrBatchRepository: EudrBatchRepository,
    private val eudrDocumentRepository: EudrDocumentRepository,
    private val auditLogRepository: AuditLogRepository,
    private val ipfsDocumentService: IpfsDocumentService,
    private val riskAssessmentService: RiskAssessmentService,
    private val objectMapper: ObjectMapper,
    @Lazy private val supplyChainEventBridgeService: SupplyChainEventBridgeService
) {

    private val logger = LoggerFactory.getLogger(DossierService::class.java)

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    /**
     * Generate comprehensive dossier for a batch
     */
    fun generateDossier(
        batchId: String,
        format: DossierFormat = DossierFormat.JSON,
        includePresignedUrls: Boolean = true,
        expiryMinutes: Int = 60
    ): DossierResult {
        logger.info("Generating dossier for batch: $batchId, format: $format")

        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        // Get risk assessment
        val riskAssessment = riskAssessmentService.assessBatchRisk(batchId)

        // Get documents with presigned URLs
        val documents = getDocumentsWithUrls(batch, includePresignedUrls, expiryMinutes)

        // Get audit trail
        val auditTrail = getAuditTrail(batchId)

        // Get combined supply chain events from both EudrBatch and Workflow systems
        val combinedData = try {
            supplyChainEventBridgeService.getAllEventsForBatch(batchId)
        } catch (e: Exception) {
            logger.warn("Failed to get combined events, falling back to batch events only: ${e.message}")
            null
        }

        // Build supply chain events - prefer combined data, fallback to batch events
        val supplyChainEventSummaries = if (combinedData != null && combinedData.supplyChainEvents.isNotEmpty()) {
            combinedData.supplyChainEvents.map { event ->
                SupplyChainEventSummary(
                    id = event.id,
                    actionType = event.eventType,
                    fromEntityId = event.fromEntity,
                    fromEntityType = event.fromEntityType,
                    toEntityId = event.toEntity,
                    toEntityType = event.toEntityType,
                    timestamp = event.timestamp,
                    location = event.location,
                    transportMethod = event.transportMethod,
                    hederaTransactionId = event.hederaTransactionId
                )
            }
        } else {
            // Fallback to batch supply chain events
            val batchEvents = batch.supplyChainEvents.sortedBy { it.eventTimestamp }
            if (batchEvents.isEmpty()) {
                // Create placeholder events from batch data
                createPlaceholderSupplyChainEvents(batch)
            } else {
                batchEvents.map { SupplyChainEventSummary.fromEvent(it) }
            }
        }

        // Build processing events - prefer combined data, fallback to batch events
        val processingEventSummaries = if (combinedData != null && combinedData.processingEvents.isNotEmpty()) {
            combinedData.processingEvents.map { event ->
                ProcessingEventSummary(
                    id = event.id,
                    processorId = event.toEntity,
                    processingType = event.eventType,
                    inputQuantity = event.quantity ?: BigDecimal.ZERO,
                    outputQuantity = event.quantity ?: BigDecimal.ZERO,
                    processingDate = event.timestamp,
                    notes = event.notes,
                    hederaTransactionId = event.hederaTransactionId
                )
            }
        } else {
            // Fallback to batch processing events
            val batchProcessingEvents = batch.processingEvents.sortedBy { it.processingDate }
            if (batchProcessingEvents.isEmpty() && batch.processor != null) {
                // Create placeholder processing event
                listOf(
                    ProcessingEventSummary(
                        id = "placeholder-${batch.id}",
                        processorId = batch.processor!!.id,
                        processingType = "PROCESSING",
                        inputQuantity = batch.quantity,
                        outputQuantity = batch.quantity,
                        processingDate = batch.createdAt,
                        notes = "Processing by ${batch.processor!!.facilityName}",
                        hederaTransactionId = null
                    )
                )
            } else {
                batchProcessingEvents.map { ProcessingEventSummary.fromEvent(it) }
            }
        }

        // Create dossier data
        val dossierData = DossierData(
            batch = BatchSummary.fromBatch(batch),
            riskAssessment = riskAssessment,
            documents = documents,
            auditTrail = auditTrail,
            supplyChain = supplyChainEventSummaries,
            processingEvents = processingEventSummaries,
            generatedAt = LocalDateTime.now(),
            generatedBy = "SYSTEM" // In real implementation, get from security context
        )

        // Generate dossier in requested format
        val dossierContent: Any = when (format) {
            DossierFormat.JSON -> generateJsonDossier(dossierData)
            DossierFormat.PDF -> generatePdfDossierBytes(dossierData)
            DossierFormat.ZIP -> generateZipDossier(dossierData)
        }

        logger.info("Dossier generated successfully for batch: $batchId")
        return DossierResult(
            batchId = batchId,
            format = format,
            content = dossierContent,
            contentType = getContentType(format),
            filename = generateFilename(batch, format),
            generatedAt = LocalDateTime.now()
        )
    }

    /**
     * Create placeholder supply chain events from batch data when no events exist
     */
    private fun createPlaceholderSupplyChainEvents(batch: EudrBatch): List<SupplyChainEventSummary> {
        val events = mutableListOf<SupplyChainEventSummary>()

        // Create harvest events from production units
        for ((index, bpu) in batch.productionUnits.withIndex()) {
            val productionUnit = bpu.productionUnit
            events.add(
                SupplyChainEventSummary(
                    id = "harvest-${batch.id}-$index",
                    actionType = "HARVEST",
                    fromEntityId = productionUnit.farmer?.id,
                    fromEntityType = "FARMER",
                    toEntityId = productionUnit.id,
                    toEntityType = "PRODUCTION_UNIT",
                    timestamp = batch.harvestDate?.atStartOfDay() ?: batch.createdAt,
                    location = productionUnit.wgs84Coordinates,
                    transportMethod = null,
                    hederaTransactionId = null
                )
            )
        }

        // Add aggregation event if aggregator exists
        if (batch.aggregator != null) {
            events.add(
                SupplyChainEventSummary(
                    id = "aggregation-${batch.id}",
                    actionType = "AGGREGATION",
                    fromEntityId = batch.productionUnits.firstOrNull()?.productionUnit?.id,
                    fromEntityType = "PRODUCTION_UNIT",
                    toEntityId = batch.aggregator!!.id,
                    toEntityType = "AGGREGATOR",
                    timestamp = batch.createdAt,
                    location = batch.aggregator!!.facilityAddress,
                    transportMethod = null,
                    hederaTransactionId = null
                )
            )
        }

        // Add processing event if processor exists
        if (batch.processor != null) {
            events.add(
                SupplyChainEventSummary(
                    id = "processing-${batch.id}",
                    actionType = "PROCESSING",
                    fromEntityId = batch.aggregator?.id,
                    fromEntityType = if (batch.aggregator != null) "AGGREGATOR" else null,
                    toEntityId = batch.processor!!.id,
                    toEntityType = "PROCESSOR",
                    timestamp = batch.createdAt,
                    location = batch.processor!!.facilityAddress,
                    transportMethod = null,
                    hederaTransactionId = null
                )
            )
        }

        return events
    }

    /**
     * Get documents with presigned URLs
     */
    private fun getDocumentsWithUrls(
        batch: EudrBatch,
        includePresignedUrls: Boolean,
        expiryMinutes: Int
    ): List<DocumentSummary> {
        return batch.documents.map { document ->
            val presignedUrl = if (includePresignedUrls && document.s3Key.isNotEmpty()) {
                try {
                    ipfsDocumentService.getDocumentAccessUrl(document.id, expiryMinutes)
                } catch (e: Exception) {
                    logger.warn("Failed to generate presigned URL for document ${document.id}", e)
                    null
                }
            } else null

            DocumentSummary.fromDocument(document, presignedUrl)
        }
    }

    /**
     * Get audit trail for batch
     */
    private fun getAuditTrail(batchId: String): List<AuditLogSummary> {
        val auditLogs = auditLogRepository.findByEntityIdAndEntityType(batchId, "EudrBatch")
        return auditLogs.map { AuditLogSummary.fromAuditLog(it) }
    }

    /**
     * Generate JSON dossier
     */
    private fun generateJsonDossier(data: DossierData): String {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data)
    }

    /**
     * Generate PDF dossier with comprehensive batch information - returns raw bytes
     */
    private fun generatePdfDossierBytes(data: DossierData): ByteArray {
        val outputStream = ByteArrayOutputStream()
        
        try {
            val document = Document(PageSize.A4, 50f, 50f, 50f, 50f)
            PdfWriter.getInstance(document, outputStream)
            document.open()
            
            // Define colors
            val primaryColor = Color(33, 150, 83)  // Green
            val headerColor = Color(240, 248, 245)
            val dangerColor = Color(220, 53, 69)
            val warningColor = Color(255, 193, 7)
            
            // Add cover page
            addCoverPage(document, data, primaryColor)
            
            // Add batch summary section
            addBatchSummary(document, data.batch, primaryColor, headerColor)
            
            // Add risk assessment section
            addRiskAssessmentSection(document, data.riskAssessment, primaryColor, headerColor, dangerColor, warningColor)
            
            // Add supply chain timeline
            addSupplyChainTimeline(document, data.supplyChain, primaryColor, headerColor)
            
            // Add processing events
            addProcessingEvents(document, data.processingEvents, primaryColor, headerColor)
            
            // Add documents section with QR codes
            addDocumentsSection(document, data.documents, primaryColor, headerColor)
            
            // Add audit trail
            addAuditTrail(document, data.auditTrail, primaryColor, headerColor)
            
            // Add compliance statement
            addComplianceStatement(document, data, primaryColor)
            
            document.close()
            
            return outputStream.toByteArray()
            
        } catch (e: Exception) {
            logger.error("Error generating PDF dossier", e)
            throw RuntimeException("Failed to generate PDF dossier: ${e.message}", e)
        }
    }
    
    /**
     * Generate PDF dossier as Base64 string (for storage/transmission)
     */
    private fun generatePdfDossier(data: DossierData): String {
        try {
        val pdfBytes = generatePdfDossierBytes(data)
        return Base64.getEncoder().encodeToString(pdfBytes)
            
        } catch (e: Exception) {
            logger.error("Error generating PDF dossier", e)
            throw RuntimeException("Failed to generate PDF dossier: ${e.message}", e)
        }
    }
    
    private fun addCoverPage(document: Document, data: DossierData, primaryColor: Color) {
        document.add(Paragraph("\n\n\n"))
        
        // Title
        val titleFont = Font(Font.HELVETICA, 28f, Font.BOLD, primaryColor)
        val titleParagraph = Paragraph("EUDR COMPLIANCE DOSSIER", titleFont)
        titleParagraph.alignment = Element.ALIGN_CENTER
        document.add(titleParagraph)
        
        document.add(Paragraph("\n"))
        
        // Batch code
        val batchFont = Font(Font.HELVETICA, 18f, Font.NORMAL, Color.BLACK)
        val batchParagraph = Paragraph("Batch: ${data.batch.batchCode}", batchFont)
        batchParagraph.alignment = Element.ALIGN_CENTER
        document.add(batchParagraph)
        
        document.add(Paragraph("\n"))
        
        // Key information table
        val infoTable = PdfPTable(2)
        infoTable.widthPercentage = 100f
        infoTable.addCell(createCell("Commodity:", true))
        infoTable.addCell(createCell(data.batch.commodityDescription))
        infoTable.addCell(createCell("Country of Production:", true))
        infoTable.addCell(createCell(data.batch.countryOfProduction))
        infoTable.addCell(createCell("Quantity:", true))
        infoTable.addCell(createCell("${data.batch.quantity} ${data.batch.unit}"))
        infoTable.addCell(createCell("Risk Level:", true))
        infoTable.addCell(createCell(data.riskAssessment.riskLevel.name))
        infoTable.addCell(createCell("Generated Date:", true))
        infoTable.addCell(createCell(data.generatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
        
        document.add(infoTable)
        
        document.add(Paragraph("\n\n"))
        
        // QR Code for batch verification (Hedera link)
        val qrCode = generateQRCode("https://hashscan.io/testnet/topic/${data.batch.id}")
        if (qrCode != null) {
            val qrParagraph = Paragraph("Scan for Blockchain Verification")
            qrParagraph.alignment = Element.ALIGN_CENTER
            val qrFont = Font(Font.HELVETICA, 10f)
            qrParagraph.font = qrFont
            document.add(qrParagraph)
            qrCode.alignment = Element.ALIGN_CENTER
            document.add(qrCode)
        }
        
        document.newPage()
    }
    
    private fun addBatchSummary(document: Document, batch: BatchSummary, primaryColor: Color, headerColor: Color) {
        document.add(createSectionHeader("1. BATCH SUMMARY", primaryColor))
        
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        
        table.addCell(createCell("Batch Code:", true))
        table.addCell(createCell(batch.batchCode))
        table.addCell(createCell("HS Code:", true))
        table.addCell(createCell(batch.hsCode ?: "N/A"))
        table.addCell(createCell("Status:", true))
        table.addCell(createCell(batch.status.name))
        table.addCell(createCell("Harvest Date:", true))
        table.addCell(createCell(batch.harvestDate?.toString() ?: "N/A"))
        table.addCell(createCell("Created By:", true))
        table.addCell(createCell(batch.createdBy))
        table.addCell(createCell("Created At:", true))
        table.addCell(createCell(batch.createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
        
        document.add(table)
        document.add(Paragraph("\n"))
    }
    
    private fun addRiskAssessmentSection(
        document: Document, 
        risk: RiskAssessmentResult, 
        primaryColor: Color,
        headerColor: Color,
        dangerColor: Color,
        warningColor: Color
    ) {
        document.add(createSectionHeader("2. RISK ASSESSMENT", primaryColor))
        
        // Overall risk
        val riskColor = when (risk.riskLevel) {
            RiskLevel.HIGH -> dangerColor
            RiskLevel.MEDIUM -> warningColor
            else -> primaryColor
        }
        
        val riskFont = Font(Font.HELVETICA, 14f, Font.BOLD, riskColor)
        document.add(Paragraph("Overall Risk Level: ${risk.riskLevel.name}", riskFont))
        
        val scoreFont = Font(Font.HELVETICA, 12f)
        document.add(Paragraph("Risk Score: ${String.format("%.2f", risk.overallScore)}/100", scoreFont))
        
        document.add(Paragraph("\n"))
        
        // Component breakdown
        val componentsFont = Font(Font.HELVETICA, 12f, Font.BOLD)
        document.add(Paragraph("Risk Components:", componentsFont))
        
        val componentTable = PdfPTable(3)
        componentTable.widthPercentage = 100f
        componentTable.setWidths(floatArrayOf(3f, 2f, 5f))
        componentTable.addCell(createHeaderCell("Component", headerColor))
        componentTable.addCell(createHeaderCell("Score", headerColor))
        componentTable.addCell(createHeaderCell("Details", headerColor))
        
        // Add all risk components to table
        val allComponents = listOf(
            risk.components.countryRisk,
            risk.components.deforestationRisk,
            risk.components.supplierRisk,
            risk.components.commodityRisk,
            risk.components.documentationRisk,
            risk.components.geospatialRisk
        )
        
        allComponents.forEach { component ->
            componentTable.addCell(createCell(component.name))
            componentTable.addCell(createCell(String.format("%.1f", component.score * 100)))
            componentTable.addCell(createCell(component.justification))
        }
        
        document.add(componentTable)
        
        // Recommendations
        if (risk.recommendations.isNotEmpty()) {
            document.add(Paragraph("\n"))
            val recFont = Font(Font.HELVETICA, 12f, Font.BOLD)
            document.add(Paragraph("Recommendations:", recFont))
            val itemFont = Font(Font.HELVETICA, 10f)
            risk.recommendations.forEach { recommendation ->
                document.add(Paragraph("• $recommendation", itemFont))
            }
        }
        
        document.add(Paragraph("\n"))
    }
    
    private fun addSupplyChainTimeline(
        document: Document, 
        events: List<SupplyChainEventSummary>,
        primaryColor: Color,
        headerColor: Color
    ) {
        document.add(createSectionHeader("3. SUPPLY CHAIN TIMELINE", primaryColor))
        
        if (events.isEmpty()) {
            val italicFont = Font(Font.HELVETICA, 12f, Font.ITALIC)
            document.add(Paragraph("No supply chain events recorded.", italicFont))
        } else {
            val table = PdfPTable(5)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(2f, 2f, 3f, 2f, 2f))
            table.addCell(createHeaderCell("Date", headerColor))
            table.addCell(createHeaderCell("Action", headerColor))
            table.addCell(createHeaderCell("From → To", headerColor))
            table.addCell(createHeaderCell("Transport", headerColor))
            table.addCell(createHeaderCell("Location", headerColor))
            
            events.forEach { event ->
                table.addCell(createCell(event.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
                table.addCell(createCell(event.actionType))
                
                val fromTo = if (event.fromEntityType != null) {
                    "${event.fromEntityType} → ${event.toEntityType}"
                } else {
                    event.toEntityType
                }
                table.addCell(createCell(fromTo))
                table.addCell(createCell(event.transportMethod ?: "N/A"))
                table.addCell(createCell(event.location ?: "N/A"))
            }
            
            document.add(table)
        }
        
        document.add(Paragraph("\n"))
    }
    
    private fun addProcessingEvents(
        document: Document,
        events: List<ProcessingEventSummary>,
        primaryColor: Color,
        headerColor: Color
    ) {
        document.add(createSectionHeader("4. PROCESSING EVENTS", primaryColor))
        
        if (events.isEmpty()) {
            val italicFont = Font(Font.HELVETICA, 12f, Font.ITALIC)
            document.add(Paragraph("No processing events recorded.", italicFont))
        } else {
            val table = PdfPTable(5)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(2f, 2f, 2f, 2f, 3f))
            table.addCell(createHeaderCell("Date", headerColor))
            table.addCell(createHeaderCell("Type", headerColor))
            table.addCell(createHeaderCell("Input", headerColor))
            table.addCell(createHeaderCell("Output", headerColor))
            table.addCell(createHeaderCell("Notes", headerColor))
            
            events.forEach { event ->
                table.addCell(createCell(event.processingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                table.addCell(createCell(event.processingType))
                table.addCell(createCell(event.inputQuantity.toString()))
                table.addCell(createCell(event.outputQuantity.toString()))
                table.addCell(createCell(event.notes ?: "N/A"))
            }
            
            document.add(table)
        }
        
        document.add(Paragraph("\n"))
    }
    
    private fun addDocumentsSection(
        document: Document,
        documents: List<DocumentSummary>,
        primaryColor: Color,
        headerColor: Color
    ) {
        document.add(createSectionHeader("5. SUPPORTING DOCUMENTS", primaryColor))
        
        if (documents.isEmpty()) {
            val italicFont = Font(Font.HELVETICA, 12f, Font.ITALIC)
            document.add(Paragraph("No documents attached.", italicFont))
        } else {
            val table = PdfPTable(5)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(3f, 2f, 2f, 2f, 2f))
            table.addCell(createHeaderCell("Document", headerColor))
            table.addCell(createHeaderCell("Type", headerColor))
            table.addCell(createHeaderCell("Issuer", headerColor))
            table.addCell(createHeaderCell("Issue Date", headerColor))
            table.addCell(createHeaderCell("Checksum", headerColor))
            
            documents.forEach { doc ->
                table.addCell(createCell(doc.fileName))
                table.addCell(createCell(doc.documentType.name))
                table.addCell(createCell(doc.issuer ?: "N/A"))
                table.addCell(createCell(doc.issueDate?.toString() ?: "N/A"))
                table.addCell(createCell(doc.checksumSha256.take(16) + "..."))
            }
            
            document.add(table)
            
            document.add(Paragraph("\n"))
            val boldFont = Font(Font.HELVETICA, 10f, Font.BOLD)
            document.add(Paragraph("Document Verification:", boldFont))
            val italicFont = Font(Font.HELVETICA, 9f, Font.ITALIC)
            document.add(Paragraph("All documents are stored on IPFS with SHA-256 checksums for integrity verification.", italicFont))
        }
        
        document.add(Paragraph("\n"))
    }
    
    private fun addAuditTrail(
        document: Document,
        auditLogs: List<AuditLogSummary>,
        primaryColor: Color,
        headerColor: Color
    ) {
        document.add(createSectionHeader("6. AUDIT TRAIL", primaryColor))
        
        if (auditLogs.isEmpty()) {
            val italicFont = Font(Font.HELVETICA, 12f, Font.ITALIC)
            document.add(Paragraph("No audit logs available.", italicFont))
        } else {
            val table = PdfPTable(4)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(2f, 2f, 2f, 4f))
            table.addCell(createHeaderCell("Timestamp", headerColor))
            table.addCell(createHeaderCell("Action", headerColor))
            table.addCell(createHeaderCell("Actor", headerColor))
            table.addCell(createHeaderCell("Details", headerColor))
            
            auditLogs.take(20).forEach { log ->  // Limit to last 20 entries
                table.addCell(createCell(log.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                table.addCell(createCell(log.action))
                table.addCell(createCell("${log.actorRole}\n(${log.actorId})"))
                table.addCell(createCell(log.details ?: "N/A"))
            }
            
            document.add(table)
            
            if (auditLogs.size > 20) {
                document.add(Paragraph("\n"))
                val italicFont = Font(Font.HELVETICA, 9f, Font.ITALIC)
                document.add(Paragraph("Showing 20 of ${auditLogs.size} audit entries. Full trail available in JSON format.", italicFont))
            }
        }
        
        document.add(Paragraph("\n"))
    }
    
    private fun addComplianceStatement(document: Document, data: DossierData, primaryColor: Color) {
        document.newPage()
        document.add(createSectionHeader("7. COMPLIANCE STATEMENT", primaryColor))
        
        document.add(Paragraph(
            "This dossier certifies that batch ${data.batch.batchCode} has been assessed " +
            "for compliance with the European Union Deforestation Regulation (EUDR) 2023/1115."
        ))
        
        document.add(Paragraph("\n"))
        
        val complianceStatus = when (data.riskAssessment.riskLevel) {
            RiskLevel.NONE, RiskLevel.LOW -> "COMPLIANT - This batch meets EUDR requirements and presents low deforestation risk."
            RiskLevel.MEDIUM -> "CONDITIONAL - This batch requires additional verification and mitigation measures."
            RiskLevel.HIGH -> "NON-COMPLIANT - This batch presents significant deforestation risk and requires immediate mitigation."
        }
        
        val boldFont = Font(Font.HELVETICA, 12f, Font.BOLD)
        document.add(Paragraph(complianceStatus, boldFont))
        
        document.add(Paragraph("\n"))
        
        // Blockchain verification info
        document.add(Paragraph("Blockchain Verification:", boldFont))
        val smallFont = Font(Font.HELVETICA, 10f)
        document.add(Paragraph(
            "All data in this dossier has been recorded on Hedera Hashgraph for immutable verification. " +
            "Visit https://hashscan.io/testnet to verify transactions.", smallFont
        ))
        
        document.add(Paragraph("\n\n"))
        
        // Signature area
        val signatureTable = PdfPTable(2)
        signatureTable.widthPercentage = 100f
        val cell1 = PdfPCell(Paragraph("Generated By:\n\n_____________________\n${data.generatedBy}"))
        cell1.minimumHeight = 80f
        cell1.setPadding(5f)
        signatureTable.addCell(cell1)
        val cell2 = PdfPCell(Paragraph("Authority Signature:\n\n_____________________\nDate: __________"))
        cell2.minimumHeight = 80f
        cell2.setPadding(5f)
        signatureTable.addCell(cell2)
        
        document.add(signatureTable)
        
        document.add(Paragraph("\n"))
        val tinyFont = Font(Font.HELVETICA, 8f)
        val docIdParagraph = Paragraph("Document ID: ${data.batch.id}", tinyFont)
        docIdParagraph.alignment = Element.ALIGN_CENTER
        document.add(docIdParagraph)
    }
    
    private fun createSectionHeader(text: String, color: Color): Paragraph {
        val font = Font(Font.HELVETICA, 16f, Font.BOLD, color)
        val paragraph = Paragraph(text, font)
        paragraph.spacingBefore = 10f
        paragraph.spacingAfter = 10f
        return paragraph
    }
    
    private fun createHeaderCell(text: String, backgroundColor: Color): PdfPCell {
        val boldFont = Font(Font.HELVETICA, 10f, Font.BOLD)
        val cell = PdfPCell(Paragraph(text, boldFont))
        cell.backgroundColor = backgroundColor
        cell.setPadding(5f)
        return cell
    }
    
    private fun createCell(text: String, bold: Boolean = false, minHeight: Float = 0f): PdfPCell {
        val font = if (bold) Font(Font.HELVETICA, 10f, Font.BOLD) else Font(Font.HELVETICA, 10f)
        val cell = PdfPCell(Paragraph(text, font))
        cell.setPadding(5f)
        if (minHeight > 0) {
            cell.minimumHeight = minHeight
        }
        return cell
    }
    
    private fun generateQRCode(url: String): Image? {
        return try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200)
            val outputStream = ByteArrayOutputStream()
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
            val img = Image.getInstance(outputStream.toByteArray())
            img.scaleToFit(150f, 150f)
            img
        } catch (e: Exception) {
            logger.warn("Failed to generate QR code", e)
            null
        }
    }

    /**
     * Generate ZIP dossier containing JSON and documents
     */
    private fun generateZipDossier(data: DossierData): String {
        // Placeholder implementation
        // In real implementation, create ZIP with JSON metadata and document files
        val jsonData = generateJsonDossier(data)
        return "ZIP_GENERATION_NOT_IMPLEMENTED_YET\n\nJSON_DATA:\n$jsonData"
    }

    /**
     * Get content type for format
     */
    private fun getContentType(format: DossierFormat): String {
        return when (format) {
            DossierFormat.JSON -> "application/json"
            DossierFormat.PDF -> "application/pdf"
            DossierFormat.ZIP -> "application/zip"
        }
    }

    /**
     * Generate filename for dossier
     */
    private fun generateFilename(batch: EudrBatch, format: DossierFormat): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val extension = when (format) {
            DossierFormat.JSON -> "json"
            DossierFormat.PDF -> "pdf"
            DossierFormat.ZIP -> "zip"
        }
        return "EUDR_Dossier_${batch.batchCode}_${timestamp}.${extension}"
    }

    /**
     * Validate dossier access permissions
     */
    fun validateDossierAccess(batchId: String, userId: String, userRoles: List<String>): Boolean {
        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        // Owner can always access
        if (batch.createdBy == userId) return true

        // System admins and auditors can access all dossiers
        if (userRoles.any { it in listOf("SYSTEM_ADMIN", "AUDITOR", "VERIFIER") }) return true

        // Exporters can access dossiers for batches they created
        if (userRoles.contains("EXPORTER") && batch.createdBy == userId) return true

        return false
    }

    /**
     * Get dossier metadata without full content
     */
    fun getDossierMetadata(batchId: String): DossierMetadata {
        val batch = eudrBatchRepository.findById(batchId)
            .orElseThrow { IllegalArgumentException("Batch not found: $batchId") }

        val documentCount = batch.documents.size
        val riskAssessment = riskAssessmentService.assessBatchRisk(batchId)

        return DossierMetadata(
            batchId = batchId,
            batchCode = batch.batchCode,
            commodity = batch.commodityDescription,
            riskLevel = riskAssessment.riskLevel,
            documentCount = documentCount,
            lastUpdated = batch.createdAt,
            availableFormats = DossierFormat.values().toList()
        )
    }
}

// Data classes for dossier generation

enum class DossierFormat {
    JSON, PDF, ZIP
}

data class DossierResult(
    val batchId: String,
    val format: DossierFormat,
    val content: Any, // Can be String (JSON) or ByteArray (PDF, ZIP)
    val contentType: String,
    val filename: String,
    val generatedAt: LocalDateTime
)

data class DossierData(
    val batch: BatchSummary,
    val riskAssessment: RiskAssessmentResult,
    val documents: List<DocumentSummary>,
    val auditTrail: List<AuditLogSummary>,
    val supplyChain: List<SupplyChainEventSummary>,
    val processingEvents: List<ProcessingEventSummary>,
    val generatedAt: LocalDateTime,
    val generatedBy: String
)

data class DossierMetadata(
    val batchId: String,
    val batchCode: String,
    val commodity: String,
    val riskLevel: RiskLevel,
    val documentCount: Int,
    val lastUpdated: LocalDateTime,
    val availableFormats: List<DossierFormat>
)

// Summary data classes

data class BatchSummary(
    val id: String,
    val batchCode: String,
    val commodityDescription: String,
    val hsCode: String?,
    val quantity: BigDecimal,
    val unit: String,
    val countryOfProduction: String,
    val harvestDate: LocalDate?,
    val createdBy: String,
    val createdAt: LocalDateTime,
    val status: BatchStatus,
    val riskLevel: RiskLevel?
) {
    companion object {
        fun fromBatch(batch: EudrBatch): BatchSummary {
            return BatchSummary(
                id = batch.id,
                batchCode = batch.batchCode,
                commodityDescription = batch.commodityDescription,
                hsCode = batch.hsCode,
                quantity = batch.quantity,
                unit = batch.unit,
                countryOfProduction = batch.countryOfProduction,
                harvestDate = batch.harvestDate,
                createdBy = batch.createdBy,
                createdAt = batch.createdAt,
                status = batch.status,
                riskLevel = batch.riskLevel
            )
        }
    }
}

data class DocumentSummary(
    val id: String,
    val documentType: EudrDocumentType,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val checksumSha256: String,
    val uploadedAt: LocalDateTime,
    val issuer: String?,
    val issueDate: LocalDate?,
    val expiryDate: LocalDate?,
    val presignedUrl: String?
) {
    companion object {
        fun fromDocument(document: EudrDocument, presignedUrl: String?): DocumentSummary {
            return DocumentSummary(
                id = document.id,
                documentType = document.documentType,
                fileName = document.fileName,
                mimeType = document.mimeType,
                fileSize = document.fileSize,
                checksumSha256 = document.checksumSha256,
                uploadedAt = document.uploadedAt,
                issuer = document.issuer,
                issueDate = document.issueDate,
                expiryDate = document.expiryDate,
                presignedUrl = presignedUrl
            )
        }
    }
}

data class AuditLogSummary(
    val id: String,
    val action: String,
    val actorId: String,
    val actorRole: String,
    val timestamp: LocalDateTime,
    val details: String?
) {
    companion object {
        fun fromAuditLog(auditLog: AuditLog): AuditLogSummary {
            return AuditLogSummary(
                id = auditLog.id,
                action = auditLog.action,
                actorId = auditLog.actorId,
                actorRole = auditLog.actorRole,
                timestamp = auditLog.timestamp,
                details = auditLog.detailsJson
            )
        }
    }
}

data class SupplyChainEventSummary(
    val id: String,
    val actionType: String, // Changed to String to accept both enum names and string values
    val fromEntityId: String?,
    val fromEntityType: String?,
    val toEntityId: String?,
    val toEntityType: String,
    val timestamp: LocalDateTime,
    val location: String?,
    val transportMethod: String?,
    val hederaTransactionId: String? = null
) {
    companion object {
        fun fromEvent(event: SupplyChainEvent): SupplyChainEventSummary {
            return SupplyChainEventSummary(
                id = event.id,
                actionType = event.actionType.name,
                fromEntityId = event.fromEntityId,
                fromEntityType = event.fromEntityType,
                toEntityId = event.toEntityId,
                toEntityType = event.toEntityType,
                timestamp = event.eventTimestamp,
                location = event.locationCoordinates,
                transportMethod = event.transportMethod,
                hederaTransactionId = event.hederaTransactionId
            )
        }
    }
}

data class ProcessingEventSummary(
    val id: String,
    val processorId: String,
    val processingType: String,
    val inputQuantity: BigDecimal,
    val outputQuantity: BigDecimal,
    val processingDate: LocalDateTime,
    val notes: String? = null, // Changed from processingNotes to notes to match usage
    val hederaTransactionId: String? = null
) {
    companion object {
        fun fromEvent(event: ProcessingEvent): ProcessingEventSummary {
            return ProcessingEventSummary(
                id = event.id,
                processorId = event.processor.id,
                processingType = event.processingType,
                inputQuantity = event.inputQuantity,
                outputQuantity = event.outputQuantity,
                processingDate = event.processingDate,
                notes = event.processingNotes,
                hederaTransactionId = event.hederaTransactionId
            )
        }
    }
}