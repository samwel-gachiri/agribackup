package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.ImporterService
import com.agriconnect.farmersportalapis.domain.eudr.ImporterVerificationStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/importers")
@Tag(name = "Importer Management", description = "APIs for managing importers, shipments, customs documents, and inspections")
@SecurityRequirement(name = "bearer-jwt")
class ImporterController(
    private val importerService: ImporterService
) {

    @PostMapping
    @Operation(summary = "Create new importer", description = "Register a new import company")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPORTER')")
    fun createImporter(@Valid @RequestBody dto: CreateImporterRequestDto): ResponseEntity<ImporterResponseDto> {
        val created = importerService.createImporter(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{importerId}")
    @Operation(summary = "Update importer details", description = "Update importer company information")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('IMPORTER') and #importerId == authentication.principal.entityId)")
    fun updateImporter(
        @PathVariable importerId: String,
        @Valid @RequestBody dto: UpdateImporterRequestDto
    ): ResponseEntity<ImporterResponseDto> {
        val updated = importerService.updateImporter(importerId, dto)
        return ResponseEntity.ok(updated)
    }

    @PatchMapping("/{importerId}/verify")
    @Operation(summary = "Verify importer", description = "Admin verification of importer registration")
    @PreAuthorize("hasRole('ADMIN')")
    fun verifyImporter(
        @PathVariable importerId: String,
        @RequestParam status: ImporterVerificationStatus
    ): ResponseEntity<ImporterResponseDto> {
        val verified = importerService.verifyImporter(importerId, status)
        return ResponseEntity.ok(verified)
    }

    @GetMapping("/{importerId}")
    @Operation(summary = "Get importer by ID", description = "Retrieve detailed importer information")
    fun getImporterById(@PathVariable importerId: String): ResponseEntity<ImporterResponseDto> {
        val importer = importerService.getImporterById(importerId)
        return ResponseEntity.ok(importer)
    }

    @GetMapping
    @Operation(summary = "List all importers", description = "Get paginated list of all importers")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllImporters(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<Page<ImporterResponseDto>> {
        val sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        val pageable = PageRequest.of(page, size, sort)
        val importers = importerService.getAllImporters(pageable)
        return ResponseEntity.ok(importers)
    }

    @GetMapping("/verification-status/{status}")
    @Operation(summary = "Get importers by verification status", description = "Filter importers by verification status")
    @PreAuthorize("hasRole('ADMIN')")
    fun getImportersByVerificationStatus(
        @PathVariable status: ImporterVerificationStatus,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ImporterResponseDto>> {
        val pageable = PageRequest.of(page, size)
        val importers = importerService.getImportersByVerificationStatus(status, pageable)
        return ResponseEntity.ok(importers)
    }

    @GetMapping("/{importerId}/statistics")
    @Operation(summary = "Get importer statistics", description = "Retrieve importer shipment metrics and statistics")
    fun getImporterStatistics(@PathVariable importerId: String): ResponseEntity<ImporterStatisticsDto> {
        val statistics = importerService.getImporterStatistics(importerId)
        return ResponseEntity.ok(statistics)
    }

    // ============================================
    // SHIPMENT MANAGEMENT
    // ============================================

    @PostMapping("/{importerId}/shipments")
    @Operation(summary = "Create import shipment", description = "Register a new inbound shipment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPORTER') or (hasRole('IMPORTER') and #importerId == authentication.principal.entityId)")
    fun createImportShipment(
        @PathVariable importerId: String,
        @Valid @RequestBody dto: CreateImportShipmentRequestDto
    ): ResponseEntity<ImportShipmentResponseDto> {
        // Verify importerId matches dto
        if (dto.importerId != importerId) {
            return ResponseEntity.badRequest().build()
        }
        
        val shipment = importerService.createImportShipment(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(shipment)
    }

    @GetMapping("/{importerId}/shipments")
    @Operation(summary = "Get shipments by importer", description = "List all shipments for an importer")
    fun getShipmentsByImporter(
        @PathVariable importerId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ImportShipmentResponseDto>> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        val shipments = importerService.getShipmentsByImporter(importerId, pageable)
        return ResponseEntity.ok(shipments)
    }

    @GetMapping("/shipments/{shipmentId}")
    @Operation(summary = "Get shipment by ID", description = "Retrieve detailed shipment information with documents and inspections")
    fun getShipmentById(@PathVariable shipmentId: String): ResponseEntity<ImportShipmentResponseDto> {
        val shipment = importerService.getShipmentById(shipmentId)
        return ResponseEntity.ok(shipment)
    }

    @PatchMapping("/shipments/{shipmentId}/status")
    @Operation(summary = "Update shipment status", description = "Update tracking status of a shipment")
    @PreAuthorize("hasRole('IMPORTER') or hasRole('ADMIN')")
    fun updateShipmentStatus(
        @PathVariable shipmentId: String,
        @Valid @RequestBody dto: UpdateShipmentStatusRequestDto
    ): ResponseEntity<ImportShipmentResponseDto> {
        // Verify shipmentId matches dto
        if (dto.shipmentId != shipmentId) {
            return ResponseEntity.badRequest().build()
        }
        
        val updated = importerService.updateShipmentStatus(dto)
        return ResponseEntity.ok(updated)
    }

    @PatchMapping("/shipments/{shipmentId}/eudr-compliance")
    @Operation(summary = "Update EUDR compliance status", description = "Update EUDR compliance determination for a shipment")
    @PreAuthorize("hasRole('IMPORTER') or hasRole('ADMIN')")
    fun updateEudrCompliance(
        @PathVariable shipmentId: String,
        @Valid @RequestBody dto: UpdateEudrComplianceRequestDto
    ): ResponseEntity<ImportShipmentResponseDto> {
        // Verify shipmentId matches dto
        if (dto.shipmentId != shipmentId) {
            return ResponseEntity.badRequest().build()
        }
        
        val updated = importerService.updateEudrCompliance(dto)
        return ResponseEntity.ok(updated)
    }

    // ============================================
    // CUSTOMS DOCUMENTS
    // ============================================

    @PostMapping("/shipments/{shipmentId}/customs-documents")
    @Operation(summary = "Upload customs document", description = "Upload regulatory document for a shipment")
    @PreAuthorize("hasRole('IMPORTER') or hasRole('ADMIN')")
    fun uploadCustomsDocument(
        @PathVariable shipmentId: String,
        @Valid @RequestBody dto: UploadCustomsDocumentRequestDto
    ): ResponseEntity<CustomsDocumentResponseDto> {
        // Verify shipmentId matches dto
        if (dto.shipmentId != shipmentId) {
            return ResponseEntity.badRequest().build()
        }
        
        val document = importerService.uploadCustomsDocument(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(document)
    }

    // ============================================
    // INSPECTION RECORDS
    // ============================================

    @PostMapping("/shipments/{shipmentId}/inspections")
    @Operation(summary = "Create inspection record", description = "Record quality or compliance inspection result")
    @PreAuthorize("hasRole('IMPORTER') or hasRole('ADMIN')")
    fun createInspectionRecord(
        @PathVariable shipmentId: String,
        @Valid @RequestBody dto: CreateInspectionRecordRequestDto
    ): ResponseEntity<InspectionRecordResponseDto> {
        // Verify shipmentId matches dto
        if (dto.shipmentId != shipmentId) {
            return ResponseEntity.badRequest().build()
        }
        
        val inspection = importerService.createInspectionRecord(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(inspection)
    }

    // ============================================
    // EUDR COMPLIANCE CERTIFICATE OPERATIONS
    // ============================================

    @PostMapping("/shipments/{shipmentId}/verify-and-certify")
    @Operation(
        summary = "Verify EUDR compliance and issue certificate NFT",
        description = """
            Performs comprehensive EUDR verification including:
            - GPS coordinates verification for all production units
            - Deforestation-free status verification
            - Complete supply chain traceability validation
            - Risk assessment based on origin country
            
            If compliant, issues EUDR Compliance Certificate NFT to exporter's Hedera account.
            This certificate serves as immutable proof of compliance.
        """
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPORTER')")
    fun verifyAndIssueCertificate(
        @PathVariable shipmentId: String
    ): ResponseEntity<ImportShipmentResponseDto> {
        val result = importerService.verifyAndIssueComplianceCertificate(shipmentId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/shipments/{shipmentId}/transfer-certificate")
    @Operation(
        summary = "Transfer EUDR certificate NFT to importer",
        description = """
            Transfers the EUDR Compliance Certificate NFT from exporter to importer.
            This happens when the importer accepts the shipment.
            
            The certificate transfer is recorded on Hedera blockchain and provides
            an immutable record of shipment ownership change.
        """
    )
    @PreAuthorize("hasRole('IMPORTER') or hasRole('ADMIN')")
    fun transferCertificate(
        @PathVariable shipmentId: String,
        @RequestParam importerId: String
    ): ResponseEntity<ImportShipmentResponseDto> {
        val result = importerService.transferComplianceCertificateToImporter(shipmentId, importerId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/shipments/{shipmentId}/verify-customs")
    @Operation(
        summary = "Verify certificate for customs clearance",
        description = """
            Customs verification endpoint that checks:
            - Certificate NFT exists and is in importer's account
            - Certificate is authentic and not frozen/revoked
            - Shipment compliance status is valid
            
            Used by customs authorities to grant instant clearance for compliant shipments.
            Returns verification result with approval status.
        """
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('IMPORTER') or hasRole('CUSTOMS')")
    fun verifyCustomsCompliance(
        @PathVariable shipmentId: String
    ): ResponseEntity<CustomsVerificationResponseDto> {
        val result = importerService.verifyCustomsCompliance(shipmentId)
        return ResponseEntity.ok(result)
    }

    // ============================================
    // CONNECTION MANAGEMENT ENDPOINTS
    // ============================================

    @GetMapping("/connected")
    @Operation(summary = "Get connected importers", description = "List importers connected to the logged-in exporter")
    @PreAuthorize("hasRole('EXPORTER')")
    fun getConnectedImporters(
        @RequestParam exporterId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ImporterResponseDto>> {
        val pageable = PageRequest.of(page, size)
        val importers = importerService.getConnectedImporters(exporterId, pageable)
        return ResponseEntity.ok(importers)
    }

    @GetMapping("/available")
    @Operation(summary = "Get available importers", description = "List importers available to connect (not yet connected)")
    @PreAuthorize("hasRole('EXPORTER')")
    fun getAvailableImporters(
        @RequestParam exporterId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ImporterResponseDto>> {
        val pageable = PageRequest.of(page, size)
        val importers = importerService.getAvailableImporters(exporterId, pageable)
        return ResponseEntity.ok(importers)
    }

    @PostMapping("/{importerId}/connect")
    @Operation(summary = "Connect to importer", description = "Create a connection between exporter and importer")
    @PreAuthorize("hasRole('EXPORTER')")
    fun connectToImporter(
        @PathVariable importerId: String,
        @RequestParam exporterId: String,
        @RequestParam(required = false) notes: String?
    ): ResponseEntity<Map<String, Any>> {
        val success = importerService.connectExporterToImporter(exporterId, importerId, notes)
        return ResponseEntity.ok(mapOf("success" to success, "message" to "Connected successfully"))
    }

    @DeleteMapping("/{importerId}/disconnect")
    @Operation(summary = "Disconnect from importer", description = "Remove connection between exporter and importer")
    @PreAuthorize("hasRole('EXPORTER')")
    fun disconnectFromImporter(
        @PathVariable importerId: String,
        @RequestParam exporterId: String
    ): ResponseEntity<Map<String, Any>> {
        val success = importerService.disconnectExporterFromImporter(exporterId, importerId)
        return ResponseEntity.ok(mapOf("success" to success, "message" to "Disconnected successfully"))
    }
}
