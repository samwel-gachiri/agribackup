package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.domain.eudr.MandateInitiator
import com.agriconnect.farmersportalapis.domain.eudr.MandateScope
import com.agriconnect.farmersportalapis.domain.eudr.MandateStatus
import com.agriconnect.farmersportalapis.service.eudr.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * Controller for Authorised Representative management
 *
 * EUDR Article 6 requires non-EU operators to designate an Authorised Representative (AR)
 * established in the EU. This controller handles:
 * - AR registration and lookup
 * - Mandate invitation flow (exporter invites AR)
 * - Mandate offer flow (AR offers to represent exporter)
 * - Mandate management (accept, reject, revoke)
 */
@RestController
@RequestMapping("/api/v1/authorised-representatives")
@Tag(name = "Authorised Representatives", description = "APIs for managing EUDR Authorised Representatives and mandates")
@SecurityRequirement(name = "bearer-jwt")
class AuthorisedRepresentativeController(
    private val arService: AuthorisedRepresentativeService
) {

    // ==================== AR Registration & Lookup ====================

    @PostMapping("/register")
    @Operation(
        summary = "Register as an Authorised Representative",
        description = "Register a new AR with EORI number. Requires verification by admin before accepting mandates."
    )
    fun registerAR(@Valid @RequestBody request: RegisterARRequestDto): ResponseEntity<ARResponseDto> {
        val ar = arService.registerAR(
            RegisterARRequest(
                eoriNumber = request.eoriNumber,
                companyName = request.companyName,
                euMemberState = request.euMemberState,
                registrationNumber = request.registrationNumber,
                vatNumber = request.vatNumber,
                contactEmail = request.contactEmail,
                contactPhone = request.contactPhone,
                contactPersonName = request.contactPersonName,
                businessAddress = request.businessAddress,
                email = request.email,
                phoneNumber = request.phoneNumber
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(ar.toResponseDto())
    }

    @GetMapping("/{arId}")
    @Operation(summary = "Get AR by ID")
    fun getARById(@PathVariable arId: String): ResponseEntity<ARResponseDto> {
        val ar = arService.getARById(arId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ar.toResponseDto())
    }

    @GetMapping("/eori/{eoriNumber}")
    @Operation(summary = "Get AR by EORI number")
    fun getARByEori(@PathVariable eoriNumber: String): ResponseEntity<ARResponseDto> {
        val ar = arService.getARByEori(eoriNumber)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ar.toResponseDto())
    }

    @GetMapping("/me")
    @Operation(summary = "Get AR profile for current user")
    @PreAuthorize("hasRole('AUTHORISED_REPRESENTATIVE')")
    fun getMyARProfile(): ResponseEntity<ARResponseDto> {
        // TODO: Extract user profile ID from authentication context
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @GetMapping("/available")
    @Operation(
        summary = "List available ARs",
        description = "Get list of verified ARs that are accepting new mandates. Public endpoint for exporter discovery."
    )
    @PreAuthorize("permitAll()")
    fun getAvailableARs(
        @RequestParam(required = false) memberState: String?
    ): ResponseEntity<List<ARResponseDto>> {
        val ars = if (memberState != null) {
            arService.findAvailableARsInMemberState(memberState)
        } else {
            arService.getAllAvailableARs()
        }
        return ResponseEntity.ok(ars.map { it.toResponseDto() })
    }

    @GetMapping("/search")
    @Operation(summary = "Search ARs by company name")
    fun searchARs(@RequestParam query: String): ResponseEntity<List<ARResponseDto>> {
        val ars = arService.searchARs(query)
        return ResponseEntity.ok(ars.map { it.toResponseDto() })
    }

    @PatchMapping("/{arId}/verify")
    @Operation(summary = "Verify AR (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    fun verifyAR(@PathVariable arId: String): ResponseEntity<ARResponseDto> {
        val ar = arService.verifyAR(arId)
        return ResponseEntity.ok(ar.toResponseDto())
    }

    // ==================== Mandate Management ====================

    @PostMapping("/mandates/invite")
    @Operation(
        summary = "Invite AR to represent exporter",
        description = "Non-EU exporter invites an AR to act on their behalf for EUDR compliance"
    )
    @PreAuthorize("hasRole('EXPORTER')")
    fun inviteAR(@Valid @RequestBody request: InviteARRequestDto): ResponseEntity<MandateResponseDto> {
        val mandate = arService.inviteAR(
            InviteARRequest(
                exporterId = request.exporterId,
                arId = request.arId,
                validFrom = request.validFrom,
                validTo = request.validTo,
                scope = request.scope,
                message = request.message
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(mandate.toResponseDto())
    }

    @PostMapping("/mandates/offer")
    @Operation(
        summary = "AR offers representation to exporter",
        description = "AR offers to represent a non-EU exporter"
    )
    @PreAuthorize("hasRole('AUTHORISED_REPRESENTATIVE')")
    fun offerRepresentation(@Valid @RequestBody request: OfferRepresentationRequestDto): ResponseEntity<MandateResponseDto> {
        val mandate = arService.offerRepresentation(
            OfferRepresentationRequest(
                arId = request.arId,
                exporterId = request.exporterId,
                validFrom = request.validFrom,
                validTo = request.validTo,
                scope = request.scope,
                message = request.message
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(mandate.toResponseDto())
    }

    @PatchMapping("/mandates/{mandateId}/accept")
    @Operation(summary = "Accept a pending mandate")
    fun acceptMandate(@PathVariable mandateId: String): ResponseEntity<MandateResponseDto> {
        val mandate = arService.acceptMandate(mandateId)
        return ResponseEntity.ok(mandate.toResponseDto())
    }

    @PatchMapping("/mandates/{mandateId}/reject")
    @Operation(summary = "Reject a pending mandate")
    fun rejectMandate(
        @PathVariable mandateId: String,
        @RequestBody(required = false) request: RejectMandateRequestDto?
    ): ResponseEntity<MandateResponseDto> {
        val mandate = arService.rejectMandate(mandateId, request?.reason)
        return ResponseEntity.ok(mandate.toResponseDto())
    }

    @PatchMapping("/mandates/{mandateId}/revoke")
    @Operation(summary = "Revoke an active mandate")
    fun revokeMandate(
        @PathVariable mandateId: String,
        @RequestBody request: RevokeMandateRequestDto
    ): ResponseEntity<MandateResponseDto> {
        val mandate = arService.revokeMandate(mandateId, request.revokedBy, request.reason)
        return ResponseEntity.ok(mandate.toResponseDto())
    }

    @GetMapping("/mandates/{mandateId}")
    @Operation(summary = "Get mandate by ID")
    fun getMandateById(@PathVariable mandateId: String): ResponseEntity<MandateResponseDto> {
        // TODO: Implement in service
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }

    @GetMapping("/mandates/exporter/{exporterId}")
    @Operation(summary = "Get all mandates for an exporter")
    fun getMandatesForExporter(@PathVariable exporterId: String): ResponseEntity<List<MandateResponseDto>> {
        // TODO: Implement in service - for now return pending
        val pending = arService.getPendingMandatesForExporter(exporterId)
        return ResponseEntity.ok(pending.map { it.toResponseDto() })
    }

    @GetMapping("/mandates/exporter/{exporterId}/active")
    @Operation(summary = "Get active mandate for an exporter")
    fun getActiveMandateForExporter(@PathVariable exporterId: String): ResponseEntity<MandateResponseDto> {
        val mandate = arService.getActiveMandateForExporter(exporterId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(mandate.toResponseDto())
    }

    @GetMapping("/mandates/ar/{arId}")
    @Operation(summary = "Get all mandates for an AR")
    fun getMandatesForAR(@PathVariable arId: String): ResponseEntity<List<MandateResponseDto>> {
        val mandates = arService.getMandatesForAR(arId)
        return ResponseEntity.ok(mandates.map { it.toResponseDto() })
    }

    @GetMapping("/mandates/ar/{arId}/pending")
    @Operation(summary = "Get pending mandate invites for an AR")
    fun getPendingMandatesForAR(@PathVariable arId: String): ResponseEntity<List<MandateResponseDto>> {
        val pending = arService.getPendingMandatesForAR(arId)
        return ResponseEntity.ok(pending.map { it.toResponseDto() })
    }

    @PostMapping("/mandates/{mandateId}/document")
    @Operation(summary = "Upload signed mandate document")
    fun uploadMandateDocument(
        @PathVariable mandateId: String,
        @RequestBody request: UploadDocumentRequestDto
    ): ResponseEntity<MandateResponseDto> {
        val mandate = arService.uploadMandateDocument(mandateId, request.documentUrl, request.documentId)
        return ResponseEntity.ok(mandate.toResponseDto())
    }

    // ==================== Access Control ====================

    @GetMapping("/access/{arId}/exporter/{exporterId}")
    @Operation(summary = "Check if AR can access exporter data")
    fun checkARAccess(
        @PathVariable arId: String,
        @PathVariable exporterId: String
    ): ResponseEntity<AccessCheckResponseDto> {
        val canAccess = arService.canARAccessExporter(arId, exporterId)
        val scope = arService.getARScopeForExporter(arId, exporterId)
        return ResponseEntity.ok(AccessCheckResponseDto(canAccess, scope))
    }

    @GetMapping("/exporters/{arId}")
    @Operation(summary = "Get exporters that AR can act on behalf of")
    @PreAuthorize("hasRole('AUTHORISED_REPRESENTATIVE')")
    fun getExportersForAR(@PathVariable arId: String): ResponseEntity<List<String>> {
        val exporterIds = arService.getExportersForAR(arId)
        return ResponseEntity.ok(exporterIds)
    }

    // ==================== Admin ====================

    @PostMapping("/admin/process-expired")
    @Operation(summary = "Process expired mandates (admin/scheduled)")
    @PreAuthorize("hasRole('ADMIN')")
    fun processExpiredMandates(): ResponseEntity<Map<String, Int>> {
        val count = arService.processExpiredMandates()
        return ResponseEntity.ok(mapOf("expiredMandatesProcessed" to count))
    }

    @GetMapping("/admin/expiring-soon")
    @Operation(summary = "Get mandates expiring soon")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AUTHORISED_REPRESENTATIVE')")
    fun getMandatesExpiringSoon(
        @RequestParam(defaultValue = "30") daysThreshold: Long
    ): ResponseEntity<List<MandateResponseDto>> {
        val mandates = arService.getMandatesExpiringSoon(daysThreshold)
        return ResponseEntity.ok(mandates.map { it.toResponseDto() })
    }
}

// ==================== Request DTOs ====================

data class RegisterARRequestDto(
    @field:NotBlank(message = "EORI number is required")
    @field:Pattern(
        regexp = "^[A-Za-z]{2}[A-Za-z0-9]{1,15}$",
        message = "EORI must be 2-letter country code + up to 15 alphanumeric characters"
    )
    val eoriNumber: String,

    @field:NotBlank(message = "Company name is required")
    val companyName: String,

    @field:NotBlank(message = "EU member state is required")
    val euMemberState: String,

    @field:NotBlank(message = "Registration number is required")
    val registrationNumber: String,

    @field:NotBlank(message = "VAT number is required")
    val vatNumber: String,

    @field:NotBlank(message = "Contact email is required")
    val contactEmail: String,

    @field:NotBlank(message = "Contact phone is required")
    val contactPhone: String,

    @field:NotBlank(message = "Contact person name is required")
    val contactPersonName: String,

    @field:NotBlank(message = "Business address is required")
    val businessAddress: String,

    val email: String? = null,
    val phoneNumber: String? = null
)

data class InviteARRequestDto(
    @field:NotBlank(message = "Exporter ID is required")
    val exporterId: String,

    @field:NotBlank(message = "AR ID is required")
    val arId: String,

    val validFrom: LocalDate? = null,
    val validTo: LocalDate? = null,
    val scope: MandateScope? = null,
    val message: String? = null
)

data class OfferRepresentationRequestDto(
    @field:NotBlank(message = "AR ID is required")
    val arId: String,

    @field:NotBlank(message = "Exporter ID is required")
    val exporterId: String,

    val validFrom: LocalDate? = null,
    val validTo: LocalDate? = null,
    val scope: MandateScope? = null,
    val message: String? = null
)

data class RejectMandateRequestDto(
    val reason: String? = null
)

data class RevokeMandateRequestDto(
    val revokedBy: MandateInitiator,
    val reason: String? = null
)

data class UploadDocumentRequestDto(
    @field:NotBlank(message = "Document URL is required")
    val documentUrl: String,
    val documentId: String? = null
)

data class AccessCheckResponseDto(
    val canAccess: Boolean,
    val scope: MandateScope?
)
// give people 2nd day for trust using case study and testimonial, why did you reach to me ? day 7 to 10 is telling them about it has been good
// ==================== Response DTOs ====================

data class ARResponseDto(
    val id: String,
    val eoriNumber: String?,
    val companyName: String?,
    val euMemberState: String?,
    val registrationNumber: String?,
    val vatNumber: String?,
    val contactEmail: String?,
    val contactPhone: String?,
    val contactPersonName: String?,
    val businessAddress: String?,
    val isVerified: Boolean,
    val isAcceptingMandates: Boolean,
    val isActive: Boolean,
    val createdAt: String?
)

data class MandateResponseDto(
    val id: String,
    val exporterId: String,
    val exporterName: String?,
    val representativeId: String,
    val representativeName: String?,
    val representativeEori: String?,
    val status: MandateStatus,
    val initiatedBy: MandateInitiator,
    val validFrom: LocalDate,
    val validTo: LocalDate?,
    val scope: MandateScope,
    val signedDocumentUrl: String?,
    val invitationMessage: String?,
    val isCurrentlyValid: Boolean,
    val createdAt: String?,
    val acceptedAt: String?,
    val rejectedAt: String?
)

// ==================== Extension Functions ====================

private fun com.agriconnect.farmersportalapis.domain.eudr.AuthorisedRepresentative.toResponseDto() = ARResponseDto(
    id = this.id,
    eoriNumber = this.eoriNumber,
    companyName = this.companyName,
    euMemberState = this.euMemberState,
    registrationNumber = this.registrationNumber,
    vatNumber = this.vatNumber,
    contactEmail = this.contactEmail,
    contactPhone = this.contactPhone ?: "",
    contactPersonName = this.contactPersonName ?: "",
    businessAddress = this.businessAddress,
    isVerified = this.isVerified,
    isAcceptingMandates = this.isAcceptingMandates,
    isActive = this.isActive,
    createdAt = this.createdAt?.toString()
)

private fun com.agriconnect.farmersportalapis.domain.eudr.RepresentativeMandate.toResponseDto() = MandateResponseDto(
    id = this.id,
    exporterId = this.exporter.id,
    exporterName = this.exporter.companyName,
    representativeId = this.representative.id,
    representativeName = this.representative.companyName,
    representativeEori = this.representative.eoriNumber,
    status = this.status,
    initiatedBy = this.initiatedBy,
    validFrom = this.validFrom,
    validTo = this.validTo,
    scope = this.scope,
    signedDocumentUrl = this.signedDocumentUrl,
    invitationMessage = this.invitationMessage,
    isCurrentlyValid = this.isCurrentlyValid(),
    createdAt = this.createdAt.toString(),
    acceptedAt = this.acceptedAt?.toString(),
    rejectedAt = this.rejectedAt?.toString()
)
