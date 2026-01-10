package com.agriconnect.farmersportalapis.service.eudr

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.eudr.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.ExporterRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.UserRepository
import com.agriconnect.farmersportalapis.repository.AuthorisedRepresentativeRepository
import com.agriconnect.farmersportalapis.repository.RepresentativeMandateRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Service for managing Authorised Representatives and their mandates
 *
 * EUDR Article 6 requires non-EU operators to designate an AR established in the EU.
 * This service handles:
 * - AR registration and verification
 * - Mandate creation (exporter invites AR or AR offers services)
 * - Mandate acceptance/rejection
 * - Mandate revocation
 * - AR lookup for exporters
 */
@Service
@Transactional
class AuthorisedRepresentativeService(
    private val arRepository: AuthorisedRepresentativeRepository,
    private val mandateRepository: RepresentativeMandateRepository,
    private val exporterRepository: ExporterRepository,
    private val userRepository: UserRepository
) {

    // ==================== AR Registration ====================

    /**
     * Register a new Authorised Representative
     */
    fun registerAR(request: RegisterARRequest): AuthorisedRepresentative {
        // Validate EORI format
        validateEoriNumber(request.eoriNumber)

        // Check EORI doesn't already exist
        if (arRepository.existsByEoriNumber(request.eoriNumber)) {
            throw IllegalArgumentException("EORI number already registered: ${request.eoriNumber}")
        }

        // Create or link user profile
        val userProfile = if (request.userProfileId != null) {
            userRepository.findById(request.userProfileId).orElseThrow {
                IllegalArgumentException("User profile not found: ${request.userProfileId}")
            }
        } else if (request.email != null) {
            // Create new user profile for AR
            val user = UserProfile(
                id = UUID.randomUUID().toString(),
                email = request.email,
                phoneNumber = request.phoneNumber ?: "",
                fullName = request.contactPersonName,
                passwordHash = "TEMPORARY_HASH" // Should be set during registration
            )
            userRepository.save(user)
        } else {
            throw IllegalArgumentException("Either userProfileId or email must be provided")
        }

        val ar = AuthorisedRepresentative(
            id = UUID.randomUUID().toString(),
            userProfile = userProfile,
            eoriNumber = request.eoriNumber.uppercase(),
            companyName = request.companyName,
            euMemberState = request.euMemberState,
            registrationNumber = request.registrationNumber,
            vatNumber = request.vatNumber,
            contactEmail = request.contactEmail,
            contactPhone = request.contactPhone,
            contactPersonName = request.contactPersonName,
            businessAddress = request.businessAddress,
            isVerified = false, // Requires manual verification
            isAcceptingMandates = true,
            isActive = true
        )

        return arRepository.save(ar)
    }

    /**
     * Validate EORI number format
     * Format: 2-letter country code + up to 15 alphanumeric characters
     */
    fun validateEoriNumber(eoriNumber: String) {
        val regex = Regex("^[A-Z]{2}[A-Z0-9]{1,15}$")
        if (!regex.matches(eoriNumber.uppercase())) {
            throw IllegalArgumentException(
                "Invalid EORI format. Expected: 2-letter EU country code + up to 15 alphanumeric characters. " +
                "Example: NL123456789012345"
            )
        }

        // Validate country code is an EU member state
        val countryCode = eoriNumber.substring(0, 2).uppercase()
        try {
            EuMemberState.valueOf(countryCode)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("EORI country code '$countryCode' is not a valid EU member state")
        }
    }

    /**
     * Get AR by ID
     */
    fun getARById(arId: String): AuthorisedRepresentative? {
        return arRepository.findById(arId).orElse(null)
    }

    /**
     * Get AR by EORI number
     */
    fun getARByEori(eoriNumber: String): AuthorisedRepresentative? {
        return arRepository.findByEoriNumber(eoriNumber.uppercase())
    }

    /**
     * Get AR for logged-in user
     */
    fun getARByUserProfile(userProfileId: String): AuthorisedRepresentative? {
        return arRepository.findByUserProfileId(userProfileId)
    }

    /**
     * Find available ARs in a specific EU member state
     */
    fun findAvailableARsInMemberState(memberState: String): List<AuthorisedRepresentative> {
        return arRepository.findByEuMemberStateAndIsVerifiedTrueAndIsActiveTrueAndIsAcceptingMandatesTrue(memberState)
    }

    /**
     * Search for ARs by company name
     */
    fun searchARs(searchTerm: String): List<AuthorisedRepresentative> {
        return arRepository.searchByCompanyName(searchTerm)
    }

    /**
     * Get all available ARs
     */
    fun getAllAvailableARs(): List<AuthorisedRepresentative> {
        return arRepository.findAllAvailableARs()
    }

    /**
     * Verify an AR (admin function)
     */
    fun verifyAR(arId: String): AuthorisedRepresentative {
        val ar = arRepository.findById(arId).orElseThrow {
            IllegalArgumentException("AR not found: $arId")
        }
        ar.isVerified = true
        return arRepository.save(ar)
    }

    // ==================== Mandate Management ====================

    /**
     * Exporter invites an AR to represent them
     */
    fun inviteAR(request: InviteARRequest): RepresentativeMandate {
        val exporter = exporterRepository.findById(request.exporterId).orElseThrow {
            IllegalArgumentException("Exporter not found: ${request.exporterId}")
        }

        val ar = arRepository.findById(request.arId).orElseThrow {
            IllegalArgumentException("AR not found: ${request.arId}")
        }

        // Check AR is verified and accepting mandates
        if (!ar.isVerified) {
            throw IllegalStateException("AR is not verified and cannot accept mandates")
        }
        if (!ar.isAcceptingMandates) {
            throw IllegalStateException("AR is not currently accepting new mandates")
        }

        // Check exporter doesn't already have an active mandate
        if (mandateRepository.exporterHasActiveMandate(request.exporterId)) {
            throw IllegalStateException("Exporter already has an active mandate. Revoke the existing mandate first.")
        }

        val mandate = RepresentativeMandate(
            id = UUID.randomUUID().toString(),
            exporter = exporter,
            representative = ar,
            status = MandateStatus.PENDING,
            initiatedBy = MandateInitiator.EXPORTER,
            validFrom = request.validFrom ?: LocalDate.now(),
            validTo = request.validTo,
            scope = request.scope ?: MandateScope.FULL,
            invitationMessage = request.message
        )

        return mandateRepository.save(mandate)
    }

    /**
     * AR offers to represent an exporter
     */
    fun offerRepresentation(request: OfferRepresentationRequest): RepresentativeMandate {
        val ar = arRepository.findById(request.arId).orElseThrow {
            IllegalArgumentException("AR not found: ${request.arId}")
        }

        val exporter = exporterRepository.findById(request.exporterId).orElseThrow {
            IllegalArgumentException("Exporter not found: ${request.exporterId}")
        }

        // Check exporter doesn't already have an active mandate
        if (mandateRepository.exporterHasActiveMandate(request.exporterId)) {
            throw IllegalStateException("Exporter already has an active mandate")
        }

        val mandate = RepresentativeMandate(
            id = UUID.randomUUID().toString(),
            exporter = exporter,
            representative = ar,
            status = MandateStatus.PENDING,
            initiatedBy = MandateInitiator.REPRESENTATIVE,
            validFrom = request.validFrom ?: LocalDate.now(),
            validTo = request.validTo,
            scope = request.scope ?: MandateScope.FULL,
            invitationMessage = request.message
        )

        return mandateRepository.save(mandate)
    }

    /**
     * Accept a pending mandate
     */
    fun acceptMandate(mandateId: String): RepresentativeMandate {
        val mandate = mandateRepository.findById(mandateId).orElseThrow {
            IllegalArgumentException("Mandate not found: $mandateId")
        }

        mandate.accept()
        return mandateRepository.save(mandate)
    }

    /**
     * Reject a pending mandate
     */
    fun rejectMandate(mandateId: String, reason: String?): RepresentativeMandate {
        val mandate = mandateRepository.findById(mandateId).orElseThrow {
            IllegalArgumentException("Mandate not found: $mandateId")
        }

        mandate.reject(reason)
        return mandateRepository.save(mandate)
    }

    /**
     * Revoke an active mandate
     */
    fun revokeMandate(mandateId: String, revokedBy: MandateInitiator, reason: String?): RepresentativeMandate {
        val mandate = mandateRepository.findById(mandateId).orElseThrow {
            IllegalArgumentException("Mandate not found: $mandateId")
        }

        mandate.revoke(revokedBy, reason)
        return mandateRepository.save(mandate)
    }

    /**
     * Get the active mandate for an exporter
     */
    fun getActiveMandateForExporter(exporterId: String): RepresentativeMandate? {
        return mandateRepository.findActiveValidMandateForExporter(exporterId)
    }

    /**
     * Get all mandates for an AR
     */
    fun getMandatesForAR(arId: String): List<RepresentativeMandate> {
        return mandateRepository.findByRepresentativeId(arId)
    }

    /**
     * Get pending mandates for an AR (invites they need to respond to)
     */
    fun getPendingMandatesForAR(arId: String): List<RepresentativeMandate> {
        return mandateRepository.findByRepresentativeIdAndStatusOrderByCreatedAtDesc(arId, MandateStatus.PENDING)
    }

    /**
     * Get pending mandates for an exporter (AR offers they need to respond to)
     */
    fun getPendingMandatesForExporter(exporterId: String): List<RepresentativeMandate> {
        return mandateRepository.findByExporterIdAndStatusOrderByCreatedAtDesc(exporterId, MandateStatus.PENDING)
    }

    /**
     * Upload signed mandate document
     */
    fun uploadMandateDocument(mandateId: String, documentUrl: String, documentId: String?): RepresentativeMandate {
        val mandate = mandateRepository.findById(mandateId).orElseThrow {
            IllegalArgumentException("Mandate not found: $mandateId")
        }

        mandate.signedDocumentUrl = documentUrl
        mandate.documentId = documentId
        return mandateRepository.save(mandate)
    }

    /**
     * Check and update expired mandates
     */
    fun processExpiredMandates(): Int {
        val expiredMandates = mandateRepository.findExpiredMandates(LocalDate.now())
        expiredMandates.forEach { it.checkExpiration() }
        mandateRepository.saveAll(expiredMandates)
        return expiredMandates.size
    }

    /**
     * Get mandates expiring soon
     */
    fun getMandatesExpiringSoon(daysThreshold: Long = 30): List<RepresentativeMandate> {
        val today = LocalDate.now()
        return mandateRepository.findMandatesExpiringSoon(today, today.plusDays(daysThreshold))
    }

    // ==================== AR Access Control ====================

    /**
     * Check if an AR can access a specific exporter's data
     * This is the "masquerade" permission check
     */
    fun canARAccessExporter(arId: String, exporterId: String): Boolean {
        val mandate = mandateRepository.findActiveValidMandateForExporter(exporterId)
        return mandate?.representative?.id == arId && mandate.isCurrentlyValid()
    }

    /**
     * Get the AR's scope of access for an exporter
     */
    fun getARScopeForExporter(arId: String, exporterId: String): MandateScope? {
        val mandate = mandateRepository.findActiveValidMandateForExporter(exporterId)
        return if (mandate?.representative?.id == arId && mandate.isCurrentlyValid()) {
            mandate.scope
        } else {
            null
        }
    }

    /**
     * Get all exporters that an AR can act on behalf of
     */
    fun getExportersForAR(arId: String): List<String> {
        return mandateRepository.findByRepresentativeIdAndStatus(arId, MandateStatus.ACTIVE)
            .filter { it.isCurrentlyValid() }
            .map { it.exporter.id }
    }
}

// ==================== Request DTOs ====================

data class RegisterARRequest(
    val eoriNumber: String,
    val companyName: String,
    val euMemberState: String,
    val registrationNumber: String,
    val vatNumber: String,
    val contactEmail: String,
    val contactPhone: String,
    val contactPersonName: String,
    val businessAddress: String,
    val userProfileId: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null
)

data class InviteARRequest(
    val exporterId: String,
    val arId: String,
    val validFrom: LocalDate? = null,
    val validTo: LocalDate? = null,
    val scope: MandateScope? = null,
    val message: String? = null
)

data class OfferRepresentationRequest(
    val arId: String,
    val exporterId: String,
    val validFrom: LocalDate? = null,
    val validTo: LocalDate? = null,
    val scope: MandateScope? = null,
    val message: String? = null
)
