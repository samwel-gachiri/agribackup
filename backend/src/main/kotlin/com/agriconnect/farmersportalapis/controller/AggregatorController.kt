package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.domain.eudr.AggregatorVerificationStatus
import com.agriconnect.farmersportalapis.domain.eudr.ConsolidatedBatchStatus
import com.agriconnect.farmersportalapis.service.supplychain.AggregatorService
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
@RequestMapping("/api/v1/aggregators")
@Tag(name = "Aggregator Management", description = "APIs for managing aggregators, collection events, and consolidated batches")
@SecurityRequirement(name = "bearer-jwt")
class AggregatorController(
    private val aggregatorService: AggregatorService
) {

    @PostMapping
    @Operation(summary = "Create new aggregator", description = "Register a new cooperative or collection center")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPORTER')")
    fun createAggregator(@Valid @RequestBody dto: CreateAggregatorRequestDto): ResponseEntity<AggregatorResponseDto> {
        val created = aggregatorService.createAggregator(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @PutMapping("/{aggregatorId}")
    @Operation(summary = "Update aggregator details", description = "Update aggregator profile information")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPORTER') or (hasRole('AGGREGATOR') and #aggregatorId == authentication.principal.entityId)")
    fun updateAggregator(
        @PathVariable aggregatorId: String,
        @Valid @RequestBody dto: UpdateAggregatorRequestDto
    ): ResponseEntity<AggregatorResponseDto> {
        val updated = aggregatorService.updateAggregator(aggregatorId, dto)
        return ResponseEntity.ok(updated)
    }

    @PatchMapping("/{aggregatorId}/verify")
    @Operation(summary = "Verify aggregator", description = "Admin verification of aggregator registration")
    @PreAuthorize("hasRole('ADMIN')")
    fun verifyAggregator(
        @PathVariable aggregatorId: String,
        @RequestParam status: AggregatorVerificationStatus
    ): ResponseEntity<AggregatorResponseDto> {
        val verified = aggregatorService.verifyAggregator(aggregatorId, status)
        return ResponseEntity.ok(verified)
    }

    @GetMapping("/{aggregatorId}")
    @Operation(summary = "Get aggregator by ID", description = "Retrieve detailed aggregator information")
    fun getAggregatorById(@PathVariable aggregatorId: String): ResponseEntity<AggregatorResponseDto> {
        val aggregator = aggregatorService.getAggregatorById(aggregatorId)
        return ResponseEntity.ok(aggregator)
    }

    @GetMapping
    @Operation(summary = "List all aggregators", description = "Get paginated list of all aggregators")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPORTER')")
    fun getAllAggregators(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "DESC") sortDirection: String
    ): ResponseEntity<Page<AggregatorResponseDto>> {
        val sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        val pageable = PageRequest.of(page, size, sort)
        val aggregators = aggregatorService.getAllAggregators(pageable)
        return ResponseEntity.ok(aggregators)
    }

    @GetMapping("/verification-status/{status}")
    @Operation(summary = "Get aggregators by verification status", description = "Filter aggregators by verification status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXPORTER')")
    fun getAggregatorsByVerificationStatus(
        @PathVariable status: AggregatorVerificationStatus,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<AggregatorResponseDto>> {
        val pageable = PageRequest.of(page, size)
        val aggregators = aggregatorService.getAggregatorsByVerificationStatus(status, pageable)
        return ResponseEntity.ok(aggregators)
    }

    @GetMapping("/{aggregatorId}/statistics")
    @Operation(summary = "Get aggregator statistics", description = "Retrieve aggregator performance metrics and statistics")
    fun getAggregatorStatistics(@PathVariable aggregatorId: String): ResponseEntity<AggregatorStatisticsDto> {
        val statistics = aggregatorService.getAggregatorStatistics(aggregatorId)
        return ResponseEntity.ok(statistics)
    }

    // ============================================
    // AGGREGATION EVENTS
    // ============================================

    @PostMapping("/{aggregatorId}/collection-events")
    @Operation(summary = "Create collection event", description = "Record produce collection from a farmer")
    @PreAuthorize("hasRole('AGGREGATOR') and #aggregatorId == authentication.principal.entityId")
    fun createAggregationEvent(
        @PathVariable aggregatorId: String,
        @Valid @RequestBody dto: CreateAggregationEventRequestDto
    ): ResponseEntity<AggregationEventResponseDto> {
        // Verify aggregatorId matches dto
        if (dto.aggregatorId != aggregatorId) {
            return ResponseEntity.badRequest().build()
        }
        
        val event = aggregatorService.createAggregationEvent(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(event)
    }

    @GetMapping("/{aggregatorId}/collection-events")
    @Operation(summary = "Get collection events", description = "List all collection events for an aggregator")
    fun getAggregationEventsByAggregator(
        @PathVariable aggregatorId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<AggregationEventResponseDto>> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "collectionDate"))
        val events = aggregatorService.getAggregationEventsByAggregator(aggregatorId, pageable)
        return ResponseEntity.ok(events)
    }

    @PatchMapping("/collection-events/{aggregationEventId}/payment")
    @Operation(summary = "Update payment status", description = "Update payment status for a collection event")
    @PreAuthorize("hasRole('AGGREGATOR') or hasRole('ADMIN')")
    fun updatePaymentStatus(
        @PathVariable aggregationEventId: String,
        @Valid @RequestBody dto: UpdatePaymentStatusRequestDto
    ): ResponseEntity<AggregationEventResponseDto> {
        // Verify aggregationEventId matches dto
        if (dto.aggregationEventId != aggregationEventId) {
            return ResponseEntity.badRequest().build()
        }
        
        val updated = aggregatorService.updatePaymentStatus(dto)
        return ResponseEntity.ok(updated)
    }

    // ============================================
    // CONSOLIDATED BATCHES
    // ============================================

    @PostMapping("/{aggregatorId}/consolidated-batches")
    @Operation(summary = "Create consolidated batch", description = "Consolidate multiple farmer collections into a batch")
    @PreAuthorize("hasRole('AGGREGATOR') and #aggregatorId == authentication.principal.entityId")
    fun createConsolidatedBatch(
        @PathVariable aggregatorId: String,
        @Valid @RequestBody dto: CreateConsolidatedBatchRequestDto
    ): ResponseEntity<ConsolidatedBatchResponseDto> {
        // Verify aggregatorId matches dto
        if (dto.aggregatorId != aggregatorId) {
            return ResponseEntity.badRequest().build()
        }
        
        val batch = aggregatorService.createConsolidatedBatch(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(batch)
    }

    @GetMapping("/{aggregatorId}/consolidated-batches")
    @Operation(summary = "Get consolidated batches", description = "List all consolidated batches for an aggregator")
    fun getConsolidatedBatchesByAggregator(
        @PathVariable aggregatorId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<ConsolidatedBatchResponseDto>> {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "consolidationDate"))
        val batches = aggregatorService.getConsolidatedBatchesByAggregator(aggregatorId, pageable)
        return ResponseEntity.ok(batches)
    }

    @PatchMapping("/consolidated-batches/{batchId}/status")
    @Operation(summary = "Update batch status", description = "Update status of a consolidated batch")
    @PreAuthorize("hasRole('AGGREGATOR') or hasRole('ADMIN')")
    fun updateBatchStatus(
        @PathVariable batchId: String,
        @RequestParam status: ConsolidatedBatchStatus
    ): ResponseEntity<ConsolidatedBatchResponseDto> {
        val updated = aggregatorService.updateBatchStatus(batchId, status)
        return ResponseEntity.ok(updated)
    }

    // ============================================
    // CONNECTION MANAGEMENT ENDPOINTS
    // ============================================

    @GetMapping("/connected")
    @Operation(summary = "Get connected aggregators", description = "List aggregators connected to the logged-in exporter")
    @PreAuthorize("hasRole('EXPORTER')")
    fun getConnectedAggregators(
        @RequestParam exporterId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<AggregatorResponseDto>> {
        val pageable = PageRequest.of(page, size)
        val aggregators = aggregatorService.getConnectedAggregators(exporterId, pageable)
        return ResponseEntity.ok(aggregators)
    }

    @GetMapping("/available")
    @Operation(summary = "Get available aggregators", description = "List aggregators available to connect (not yet connected)")
    @PreAuthorize("hasRole('EXPORTER')")
    fun getAvailableAggregators(
        @RequestParam exporterId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<AggregatorResponseDto>> {
        val pageable = PageRequest.of(page, size)
        val aggregators = aggregatorService.getAvailableAggregators(exporterId, pageable)
        return ResponseEntity.ok(aggregators)
    }

    @PostMapping("/{aggregatorId}/connect")
    @Operation(summary = "Connect to aggregator", description = "Create a connection between exporter and aggregator")
    @PreAuthorize("hasRole('EXPORTER')")
    fun connectToAggregator(
        @PathVariable aggregatorId: String,
        @RequestParam exporterId: String,
        @RequestParam(required = false) notes: String?
    ): ResponseEntity<Map<String, Any>> {
        val success = aggregatorService.connectExporterToAggregator(exporterId, aggregatorId, notes)
        return ResponseEntity.ok(mapOf("success" to success, "message" to "Connected successfully"))
    }

    @DeleteMapping("/{aggregatorId}/disconnect")
    @Operation(summary = "Disconnect from aggregator", description = "Remove connection between exporter and aggregator")
    @PreAuthorize("hasRole('EXPORTER')")
    fun disconnectFromAggregator(
        @PathVariable aggregatorId: String,
        @RequestParam exporterId: String
    ): ResponseEntity<Map<String, Any>> {
        val success = aggregatorService.disconnectExporterFromAggregator(exporterId, aggregatorId)
        return ResponseEntity.ok(mapOf("success" to success, "message" to "Disconnected successfully"))
    }
}
