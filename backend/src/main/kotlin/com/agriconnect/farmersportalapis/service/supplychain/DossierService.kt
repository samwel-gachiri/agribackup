package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.domain.eudr.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.AuditLogRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrBatchRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.EudrDocumentRepository
import com.agriconnect.farmersportalapis.service.common.RiskAssessmentResult
import com.agriconnect.farmersportalapis.service.common.RiskAssessmentService
import com.agriconnect.farmersportalapis.service.hedera.IpfsDocumentService
import com.fasterxml.jackson.databind.ObjectMapper
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.io.image.ImageDataFactory
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64

@Service
@Transactional(readOnly = true)
class DossierService(
    private val eudrBatchRepository: EudrBatchRepository,
    private val eudrDocumentRepository: EudrDocumentRepository,
    private val auditLogRepository: AuditLogRepository,
    private val ipfsDocumentService: IpfsDocumentService,
    private val riskAssessmentService: RiskAssessmentService,
    private val objectMapper: ObjectMapper
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

        // Get supply chain events
        val supplyChainEvents = batch.supplyChainEvents.sortedBy { it.eventTimestamp }

        // Get processing events
        val processingEvents = batch.processingEvents.sortedBy { it.processingDate }

        // Create dossier data
        val dossierData = DossierData(
            batch = BatchSummary.fromBatch(batch),
            riskAssessment = riskAssessment,
            documents = documents,
            auditTrail = auditTrail,
            supplyChain = supplyChainEvents.map { SupplyChainEventSummary.fromEvent(it) },
            processingEvents = processingEvents.map { ProcessingEventSummary.fromEvent(it) },
            generatedAt = LocalDateTime.now(),
            generatedBy = "SYSTEM" // In real implementation, get from security context
        )

        // Generate dossier in requested format
        val dossierContent = when (format) {
            DossierFormat.JSON -> generateJsonDossier(dossierData)
            DossierFormat.PDF -> generatePdfDossier(dossierData)
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
     * Generate PDF dossier with comprehensive batch information
     */
    private fun generatePdfDossier(data: DossierData): String {
        val outputStream = ByteArrayOutputStream()
        
        try {
            val pdfWriter = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)
            
            // Define colors
            val primaryColor = DeviceRgb(33, 150, 83)  // Green
            val headerColor = DeviceRgb(240, 248, 245)
            val dangerColor = DeviceRgb(220, 53, 69)
            val warningColor = DeviceRgb(255, 193, 7)
            
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
            
            // Convert to Base64 string for storage/transmission
            val pdfBytes = outputStream.toByteArray()
            return Base64.getEncoder().encodeToString(pdfBytes)
            
        } catch (e: Exception) {
            logger.error("Error generating PDF dossier", e)
            throw RuntimeException("Failed to generate PDF dossier: ${e.message}", e)
        }
    }
    
    private fun addCoverPage(document: Document, data: DossierData, primaryColor: DeviceRgb) {
        document.add(Paragraph("\n\n\n"))
        
        // Title
        document.add(
            Paragraph("EUDR COMPLIANCE DOSSIER")
                .setFontSize(28f)
                .setBold()
                .setFontColor(primaryColor)
                .setTextAlignment(TextAlignment.CENTER)
        )
        
        document.add(Paragraph("\n"))
        
        // Batch code
        document.add(
            Paragraph("Batch: ${data.batch.batchCode}")
                .setFontSize(18f)
                .setTextAlignment(TextAlignment.CENTER)
        )
        
        document.add(Paragraph("\n"))
        
        // Key information table
        val infoTable = Table(2).useAllAvailableWidth()
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
            document.add(
                Paragraph("Scan for Blockchain Verification")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10f)
            )
            document.add(qrCode.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER))
        }
        
        document.add(com.itextpdf.layout.element.AreaBreak())
    }
    
    private fun addBatchSummary(document: Document, batch: BatchSummary, primaryColor: DeviceRgb, headerColor: DeviceRgb) {
        document.add(createSectionHeader("1. BATCH SUMMARY", primaryColor))
        
        val table = Table(2).useAllAvailableWidth()
        table.setBackgroundColor(headerColor)
        
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
        primaryColor: DeviceRgb,
        headerColor: DeviceRgb,
        dangerColor: DeviceRgb,
        warningColor: DeviceRgb
    ) {
        document.add(createSectionHeader("2. RISK ASSESSMENT", primaryColor))
        
        // Overall risk
        val riskColor = when (risk.riskLevel) {
            RiskLevel.HIGH -> dangerColor
            RiskLevel.MEDIUM -> warningColor
            else -> primaryColor
        }
        
        document.add(
            Paragraph("Overall Risk Level: ${risk.riskLevel.name}")
                .setFontSize(14f)
                .setBold()
                .setFontColor(riskColor)
        )
        
        document.add(
            Paragraph("Risk Score: ${String.format("%.2f", risk.overallScore)}/100")
                .setFontSize(12f)
        )
        
        document.add(Paragraph("\n"))
        
        // Component breakdown
        document.add(Paragraph("Risk Components:").setBold())
        
        val componentTable = Table(floatArrayOf(3f, 2f, 5f)).useAllAvailableWidth()
        componentTable.addHeaderCell(createHeaderCell("Component", headerColor))
        componentTable.addHeaderCell(createHeaderCell("Score", headerColor))
        componentTable.addHeaderCell(createHeaderCell("Details", headerColor))
        
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
            document.add(Paragraph("Recommendations:").setBold())
            risk.recommendations.forEach { recommendation ->
                document.add(Paragraph("• $recommendation").setFontSize(10f))
            }
        }
        
        document.add(Paragraph("\n"))
    }
    
    private fun addSupplyChainTimeline(
        document: Document, 
        events: List<SupplyChainEventSummary>,
        primaryColor: DeviceRgb,
        headerColor: DeviceRgb
    ) {
        document.add(createSectionHeader("3. SUPPLY CHAIN TIMELINE", primaryColor))
        
        if (events.isEmpty()) {
            document.add(Paragraph("No supply chain events recorded.").setItalic())
        } else {
            val table = Table(floatArrayOf(2f, 2f, 3f, 2f, 2f)).useAllAvailableWidth()
            table.addHeaderCell(createHeaderCell("Date", headerColor))
            table.addHeaderCell(createHeaderCell("Action", headerColor))
            table.addHeaderCell(createHeaderCell("From → To", headerColor))
            table.addHeaderCell(createHeaderCell("Transport", headerColor))
            table.addHeaderCell(createHeaderCell("Location", headerColor))
            
            events.forEach { event ->
                table.addCell(createCell(event.eventTimestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
                table.addCell(createCell(event.actionType.name))
                
                val fromTo = if (event.fromEntityType != null) {
                    "${event.fromEntityType} → ${event.toEntityType}"
                } else {
                    event.toEntityType
                }
                table.addCell(createCell(fromTo))
                table.addCell(createCell(event.transportMethod ?: "N/A"))
                table.addCell(createCell(event.locationCoordinates ?: "N/A"))
            }
            
            document.add(table)
        }
        
        document.add(Paragraph("\n"))
    }
    
    private fun addProcessingEvents(
        document: Document,
        events: List<ProcessingEventSummary>,
        primaryColor: DeviceRgb,
        headerColor: DeviceRgb
    ) {
        document.add(createSectionHeader("4. PROCESSING EVENTS", primaryColor))
        
        if (events.isEmpty()) {
            document.add(Paragraph("No processing events recorded.").setItalic())
        } else {
            val table = Table(floatArrayOf(2f, 2f, 2f, 2f, 3f)).useAllAvailableWidth()
            table.addHeaderCell(createHeaderCell("Date", headerColor))
            table.addHeaderCell(createHeaderCell("Type", headerColor))
            table.addHeaderCell(createHeaderCell("Input", headerColor))
            table.addHeaderCell(createHeaderCell("Output", headerColor))
            table.addHeaderCell(createHeaderCell("Notes", headerColor))
            
            events.forEach { event ->
                table.addCell(createCell(event.processingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))))
                table.addCell(createCell(event.processingType))
                table.addCell(createCell(event.inputQuantity.toString()))
                table.addCell(createCell(event.outputQuantity.toString()))
                table.addCell(createCell(event.processingNotes ?: "N/A"))
            }
            
            document.add(table)
        }
        
        document.add(Paragraph("\n"))
    }
    
    private fun addDocumentsSection(
        document: Document,
        documents: List<DocumentSummary>,
        primaryColor: DeviceRgb,
        headerColor: DeviceRgb
    ) {
        document.add(createSectionHeader("5. SUPPORTING DOCUMENTS", primaryColor))
        
        if (documents.isEmpty()) {
            document.add(Paragraph("No documents attached.").setItalic())
        } else {
            val table = Table(floatArrayOf(3f, 2f, 2f, 2f, 2f)).useAllAvailableWidth()
            table.addHeaderCell(createHeaderCell("Document", headerColor))
            table.addHeaderCell(createHeaderCell("Type", headerColor))
            table.addHeaderCell(createHeaderCell("Issuer", headerColor))
            table.addHeaderCell(createHeaderCell("Issue Date", headerColor))
            table.addHeaderCell(createHeaderCell("Checksum", headerColor))
            
            documents.forEach { doc ->
                table.addCell(createCell(doc.fileName))
                table.addCell(createCell(doc.documentType.name))
                table.addCell(createCell(doc.issuer ?: "N/A"))
                table.addCell(createCell(doc.issueDate?.toString() ?: "N/A"))
                table.addCell(createCell(doc.checksumSha256.take(16) + "..."))
            }
            
            document.add(table)
            
            document.add(Paragraph("\n"))
            document.add(Paragraph("Document Verification:").setBold().setFontSize(10f))
            document.add(Paragraph("All documents are stored on IPFS with SHA-256 checksums for integrity verification.")
                .setFontSize(9f)
                .setItalic())
        }
        
        document.add(Paragraph("\n"))
    }
    
    private fun addAuditTrail(
        document: Document,
        auditLogs: List<AuditLogSummary>,
        primaryColor: DeviceRgb,
        headerColor: DeviceRgb
    ) {
        document.add(createSectionHeader("6. AUDIT TRAIL", primaryColor))
        
        if (auditLogs.isEmpty()) {
            document.add(Paragraph("No audit logs available.").setItalic())
        } else {
            val table = Table(floatArrayOf(2f, 2f, 2f, 4f)).useAllAvailableWidth()
            table.addHeaderCell(createHeaderCell("Timestamp", headerColor))
            table.addHeaderCell(createHeaderCell("Action", headerColor))
            table.addHeaderCell(createHeaderCell("Actor", headerColor))
            table.addHeaderCell(createHeaderCell("Details", headerColor))
            
            auditLogs.take(20).forEach { log ->  // Limit to last 20 entries
                table.addCell(createCell(log.timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
                table.addCell(createCell(log.action))
                table.addCell(createCell("${log.actorRole}\n(${log.actorId})"))
                table.addCell(createCell(log.details ?: "N/A"))
            }
            
            document.add(table)
            
            if (auditLogs.size > 20) {
                document.add(Paragraph("\n"))
                document.add(Paragraph("Showing 20 of ${auditLogs.size} audit entries. Full trail available in JSON format.")
                    .setFontSize(9f)
                    .setItalic())
            }
        }
        
        document.add(Paragraph("\n"))
    }
    
    private fun addComplianceStatement(document: Document, data: DossierData, primaryColor: DeviceRgb) {
        document.add(com.itextpdf.layout.element.AreaBreak())
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
        
        document.add(Paragraph(complianceStatus).setBold())
        
        document.add(Paragraph("\n"))
        
        // Blockchain verification info
        document.add(Paragraph("Blockchain Verification:").setBold())
        document.add(Paragraph(
            "All data in this dossier has been recorded on Hedera Hashgraph for immutable verification. " +
            "Visit https://hashscan.io/testnet to verify transactions."
        ).setFontSize(10f))
        
        document.add(Paragraph("\n\n"))
        
        // Signature area
        val signatureTable = Table(2).useAllAvailableWidth()
        signatureTable.addCell(createCell("Generated By:\n\n_____________________\n${data.generatedBy}", false, 80f))
        signatureTable.addCell(createCell("Authority Signature:\n\n_____________________\nDate: __________", false, 80f))
        
        document.add(signatureTable)
        
        document.add(Paragraph("\n"))
        document.add(
            Paragraph("Document ID: ${data.batch.id}")
                .setFontSize(8f)
                .setTextAlignment(TextAlignment.CENTER)
        )
    }
    
    private fun createSectionHeader(text: String, color: DeviceRgb): Paragraph {
        return Paragraph(text)
            .setFontSize(16f)
            .setBold()
            .setFontColor(color)
            .setMarginTop(10f)
            .setMarginBottom(10f)
    }
    
    private fun createHeaderCell(text: String, backgroundColor: DeviceRgb): Cell {
        return Cell()
            .add(Paragraph(text).setBold())
            .setBackgroundColor(backgroundColor)
            .setPadding(5f)
    }
    
    private fun createCell(text: String, bold: Boolean = false, minHeight: Float = 0f): Cell {
        val cell = Cell().add(Paragraph(text).apply { if (bold) setBold() })
        cell.setPadding(5f)
        if (minHeight > 0) {
            cell.setMinHeight(minHeight)
        }
        return cell
    }
    
    private fun generateQRCode(url: String): Image? {
        return try {
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 200, 200)
            val outputStream = ByteArrayOutputStream()
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
            val imageData = ImageDataFactory.create(outputStream.toByteArray())
            Image(imageData).scaleToFit(150f, 150f)
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
    val content: String,
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
    val actionType: SupplyChainActionType,
    val fromEntityId: String?,
    val fromEntityType: String?,
    val toEntityId: String,
    val toEntityType: String,
    val eventTimestamp: LocalDateTime,
    val locationCoordinates: String?,
    val transportMethod: String?
) {
    companion object {
        fun fromEvent(event: SupplyChainEvent): SupplyChainEventSummary {
            return SupplyChainEventSummary(
                id = event.id,
                actionType = event.actionType,
                fromEntityId = event.fromEntityId,
                fromEntityType = event.fromEntityType,
                toEntityId = event.toEntityId,
                toEntityType = event.toEntityType,
                eventTimestamp = event.eventTimestamp,
                locationCoordinates = event.locationCoordinates,
                transportMethod = event.transportMethod
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
    val processingNotes: String?
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
                processingNotes = event.processingNotes
            )
        }
    }
}