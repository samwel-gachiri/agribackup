package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.domain.eudr.ProcessorVerificationStatus
import com.agriconnect.farmersportalapis.service.supplychain.ProcessorService
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
@RequestMapping("/api/v1/processors")
@Tag(name = "Processor Management", description = "APIs for managing processors and processing events")
@SecurityRequirement(name = "bearer-jwt")
class ProcessorController(
    private val processorService: ProcessorService
) {

    @PostMapping
    @Operation(summary = "Create new processor", description = "Register a new processing facility")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPORTER')")
    fun createProcessor(@Valid @RequestBody dto: CreateProcessorRequestDto): ResponseEntity<ProcessorResponseDto> {
        val created = processorService.createProcessor(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{processorId}")
    @Operation(summary = "Update processor details", description = "Update processor facility information")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPORTER') or (hasRole('PROCESSOR') and #processorId == authentication.principal.entityId)")
    fun updateProcessor(
        @PathVariable processorId: String,
        @Valid @RequestBody dto: UpdateProcessorRequestDto
    ): ResponseEntity<ProcessorResponseDto> {
        val updated = processorService.updateProcessor(processorId, dto)
        return ResponseEntity.ok(updated)
    }

    @PatchMapping("/{processorId}/verify")
    @Operation(summary = "Verify processor", description = "Admin verification of processor registration")
    @PreAuthorize("hasRole('ADMIN')")
    fun verifyProcessor(
        @PathVariable processorId: String,
        @RequestParam status: ProcessorVerificationStatus
    ): ResponseEntity<ProcessorResponseDto> {
        val verified = processorService.verifyProcessor(processorId, status)
        return ResponseEntity.ok(verified)
    }

    @GetMapping("/{processorId}")
    @Operation(summary = "Get processor by ID", description = "Retrieve detailed processor information")
    fun getProcessorById(@PathVariable processorId: String): ResponseEntity<ProcessorResponseDto> {
        val processor = processorService.getProcessorById(processorId)
        return ResponseEntity.ok(processor)
    }

    @GetMapping
    @Operation(summary = "List all processors", description = "Get paginated list of all processors")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPORTER')")
    fun getAllProcessors(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<Page<ProcessorResponseDto>> {
        val sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        val pageable = PageRequest.of(page, size, sort)
        val processors = processorService.getAllProcessors(pageable)
        return ResponseEntity.ok(processors)
    }

    @GetMapping("/verification-status/{status}")
    @Operation(summary = "Get processors by verification status", description = "Filter processors by verification status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPORTER')")
    fun getProcessorsByVerificationStatus(
        @PathVariable status: ProcessorVerificationStatus,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ProcessorResponseDto>> {
        val pageable = PageRequest.of(page, size)
        val processors = processorService.getProcessorsByVerificationStatus(status, pageable)
        return ResponseEntity.ok(processors)
    }

    @GetMapping("/{processorId}/statistics")
    @Operation(summary = "Get processor statistics", description = "Retrieve processor performance metrics and statistics")
    fun getProcessorStatistics(@PathVariable processorId: String): ResponseEntity<ProcessorStatisticsDto> {
        val statistics = processorService.getProcessorStatistics(processorId)
        return ResponseEntity.ok(statistics)
    }

    // ============================================
    // PROCESSING EVENTS
    // ============================================

    @PostMapping("/{processorId}/processing-events")
    @Operation(summary = "Create processing event", description = "Record a batch processing event")
    @PreAuthorize("hasRole('PROCESSOR') and #processorId == authentication.principal.entityId")
    fun createProcessingEvent(
        @PathVariable processorId: String,
        @Valid @RequestBody dto: CreateProcessingEventRequestDto
    ): ResponseEntity<ProcessingEventResponseDto> {
        // Verify processorId matches dto
        if (dto.processorId != processorId) {
            return ResponseEntity.badRequest().build()
        }
        
        val event = processorService.createProcessingEvent(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(event)
    }

    @GetMapping("/{processorId}/processing-events")
    @Operation(summary = "Get processing events", description = "List all processing events for a processor")
    fun getProcessingEventsByProcessor(
        @PathVariable processorId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ProcessingEventResponseDto>> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "processingDate"))
        val events = processorService.getProcessingEventsByProcessor(processorId, pageable)
        return ResponseEntity.ok(events)
    }

    @GetMapping("/processing-events/{eventId}")
    @Operation(summary = "Get processing event by ID", description = "Retrieve detailed processing event information")
    fun getProcessingEventById(@PathVariable eventId: String): ResponseEntity<ProcessingEventResponseDto> {
        val event = processorService.getProcessingEventById(eventId)
        return ResponseEntity.ok(event)
    }

    @GetMapping("/batches/{batchId}/processing-events")
    @Operation(summary = "Get processing events by batch", description = "List all processing events for a specific batch")
    fun getProcessingEventsByBatch(
        @PathVariable batchId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ProcessingEventResponseDto>> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "processingDate"))
        val events = processorService.getProcessingEventsByBatch(batchId, pageable)
        return ResponseEntity.ok(events)
    }

    // ============================================
    // CONNECTION MANAGEMENT ENDPOINTS
    // ============================================

    @GetMapping("/connected")
    @Operation(summary = "Get connected processors", description = "List processors connected to the logged-in exporter")
    @PreAuthorize("hasRole('EXPORTER')")
    fun getConnectedProcessors(
        @RequestParam exporterId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ProcessorResponseDto>> {
        val pageable = PageRequest.of(page, size)
        val processors = processorService.getConnectedProcessors(exporterId, pageable)
        return ResponseEntity.ok(processors)
    }

    @GetMapping("/available")
    @Operation(summary = "Get available processors", description = "List processors available to connect (not yet connected)")
    @PreAuthorize("hasRole('EXPORTER')")
    fun getAvailableProcessors(
        @RequestParam exporterId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ProcessorResponseDto>> {
        val pageable = PageRequest.of(page, size)
        val processors = processorService.getAvailableProcessors(exporterId, pageable)
        return ResponseEntity.ok(processors)
    }

    @PostMapping("/{processorId}/connect")
    @Operation(summary = "Connect to processor", description = "Create a connection between exporter and processor")
    @PreAuthorize("hasRole('EXPORTER')")
    fun connectToProcessor(
        @PathVariable processorId: String,
        @RequestParam exporterId: String,
        @RequestParam(required = false) notes: String?
    ): ResponseEntity<Map<String, Any>> {
        val success = processorService.connectExporterToProcessor(exporterId, processorId, notes)
        return ResponseEntity.ok(mapOf("success" to success, "message" to "Connected successfully"))
    }

    @DeleteMapping("/{processorId}/disconnect")
    @Operation(summary = "Disconnect from processor", description = "Remove connection between exporter and processor")
    @PreAuthorize("hasRole('EXPORTER')")
    fun disconnectFromProcessor(
        @PathVariable processorId: String,
        @RequestParam exporterId: String
    ): ResponseEntity<Map<String, Any>> {
        val success = processorService.disconnectExporterFromProcessor(exporterId, processorId)
        return ResponseEntity.ok(mapOf("success" to success, "message" to "Disconnected successfully"))
    }
}
