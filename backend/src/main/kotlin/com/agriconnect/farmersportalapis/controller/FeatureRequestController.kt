package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.FeatureRequestCreateDto
import com.agriconnect.farmersportalapis.application.dtos.FeatureRequestFilterDto
import com.agriconnect.farmersportalapis.application.dtos.FeatureRequestResponseDto
import com.agriconnect.farmersportalapis.application.dtos.FeatureRequestUpdateDto
import com.agriconnect.farmersportalapis.service.common.FeatureRequestService
import com.agriconnect.farmersportalapis.domain.common.enums.RequestStatus
import com.agriconnect.farmersportalapis.domain.common.enums.RequestType
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/feature-requests")
class FeatureRequestController(
    private val featureRequestService: FeatureRequestService
) {

    @PostMapping
    @Operation(description = "Creates a new feature request")
    fun createFeatureRequest(
        @Valid @RequestBody dto: FeatureRequestCreateDto
    ): ResponseEntity<FeatureRequestResponseDto> {
        val createdRequest = featureRequestService.createFeatureRequest(dto)
        return ResponseEntity(createdRequest, HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    @Operation(description = "Get a specific feature request")
    fun getFeatureRequest(
        @PathVariable id: Long
    ): ResponseEntity<FeatureRequestResponseDto> {
        val request = featureRequestService.getFeatureRequestById(id)
        return ResponseEntity.ok(request)
    }

    @GetMapping
    @Operation(description="Get all feature request")
    fun getAllFeatureRequests(
        @RequestParam(required = false) status: RequestStatus?,
        @RequestParam(required = false) requestType: RequestType?,
        @RequestParam(required = false) userSection: String?,
        @RequestParam(required = false) userId: Long?,
        pageable: Pageable
    ): ResponseEntity<Page<FeatureRequestResponseDto>> {
        val filter = FeatureRequestFilterDto(status, requestType, userSection, userId)
        val requests = featureRequestService.getAllFeatureRequests(filter, pageable)
        return ResponseEntity.ok(requests)
    }

    @GetMapping("/user/{userId}")
    @Operation(description = "Gets all feature request for a specific user")
    fun getUserFeatureRequests(
        @PathVariable userId: Long,
        pageable: Pageable
    ): ResponseEntity<Page<FeatureRequestResponseDto>> {
        val requests = featureRequestService.getUserFeatureRequests(userId, pageable)
        return ResponseEntity.ok(requests)
    }

    @PutMapping("/{id}")
    @Operation(description = "Updates a feature request (status/notes)")
    fun updateFeatureRequest(
        @PathVariable id: Long,
        @Valid @RequestBody dto: FeatureRequestUpdateDto
    ): ResponseEntity<FeatureRequestResponseDto> {
        val updatedRequest = featureRequestService.updateFeatureRequest(id, dto)
        return ResponseEntity.ok(updatedRequest)
    }
}