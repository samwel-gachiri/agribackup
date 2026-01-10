package com.agriconnect.farmersportalapis.domain.eudr

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * AuthorisedRepresentative Entity
 *
 * Per EUDR Article 6, operators not established in the EU must designate
 * an Authorised Representative established in a Member State. The AR acts
 * on behalf of the operator for compliance purposes.
 *
 * CRITICAL: The AR holds the EORI number required for EU TRACES system access.
 * Without a valid EORI, non-EU exporters cannot submit Due Diligence Statements.
 *
 * Key responsibilities of an AR:
 * - Submit due diligence statements on behalf of the operator
 * - Ensure compliance with EUDR obligations
 * - Cooperate with competent authorities
 * - Keep records for at least 5 years
 */
@Entity
@Table(
    name = "authorised_representatives",
    indexes = [
        Index(name = "idx_ar_eori_number", columnList = "eori_number"),
        Index(name = "idx_ar_user_profile", columnList = "user_profile_id"),
        Index(name = "idx_ar_registration_number", columnList = "registration_number"),
        Index(name = "idx_ar_is_active", columnList = "is_active"),
        Index(name = "idx_ar_member_state", columnList = "eu_member_state")
    ]
)
class AuthorisedRepresentative(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "representative_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    /**
     * User profile for the AR - allows them to log in and act on behalf of exporters
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", unique = true)
    var userProfile: UserProfile? = null,

    /**
     * CRITICAL: EORI (Economic Operators Registration and Identification) number
     * This is required for EU TRACES system access and DDS submission.
     * Format: [Country Code (2 chars)] + [Unique ID (up to 15 chars)]
     * Example: "NL123456789012345" or "DE987654321"
     * Note: Can be null during initial signup, but required for DDS submission.
     */
    @Column(name = "eori_number", length = 17, unique = true)
    var eoriNumber: String? = null,

    /**
     * Legal name of the Authorised Representative company
     */
    @Column(name = "company_name")
    var companyName: String? = null,

    /**
     * EU Member State where the AR is established
     * ISO 3166-1 alpha-2 code (e.g., "DE", "FR", "NL")
     */
    @Column(name = "eu_member_state", length = 2)
    var euMemberState: String? = null,

    /**
     * Business registration number in the EU Member State
     */
    @Column(name = "registration_number")
    var registrationNumber: String? = null,

    /**
     * VAT identification number (required for commercial AR services)
     */
    @Column(name = "vat_number", length = 20)
    var vatNumber: String? = null,

    /**
     * Primary contact email for the AR
     */
    @Column(name = "contact_email")
    var contactEmail: String? = null,

    /**
     * Contact phone number
     */
    @Column(name = "contact_phone", length = 30)
    var contactPhone: String? = null,

    /**
     * Name of the primary contact person at the AR organization
     */
    @Column(name = "contact_person_name")
    var contactPersonName: String? = null,

    /**
     * Business address of the AR in the EU (required for full registration)
     */
    @Column(name = "business_address", columnDefinition = "TEXT")
    var businessAddress: String? = null,

    /**
     * Whether this AR is verified and active in the system
     */
    @Column(name = "is_verified", nullable = false)
    var isVerified: Boolean = false,

    /**
     * Whether this AR is currently accepting new mandates
     */
    @Column(name = "is_accepting_mandates", nullable = false)
    var isAcceptingMandates: Boolean = true,

    /**
     * Whether this AR account is active
     */
    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Validates EORI number format
     * EORI format: 2-letter country code + up to 15 alphanumeric characters
     */
    fun isEoriValid(): Boolean {
        val eori = eoriNumber ?: return false
        val eoriRegex = Regex("^[A-Z]{2}[A-Z0-9]{1,15}$")
        return eoriRegex.matches(eori)
    }

    /**
     * Gets the country code from the EORI number
     */
    fun getEoriCountryCode(): String {
        val eori = eoriNumber ?: return ""
        return if (eori.length >= 2) eori.substring(0, 2) else ""
    }

    /**
     * Checks if this AR can submit DDS (has valid EORI and is verified)
     */
    fun canSubmitDDS(): Boolean {
        return isVerified && isActive && isEoriValid()
    }
}

/**
 * Enum for EU Member States (for validation)
 */
enum class EuMemberState(val code: String, val displayName: String) {
    AT("AT", "Austria"),
    BE("BE", "Belgium"),
    BG("BG", "Bulgaria"),
    HR("HR", "Croatia"),
    CY("CY", "Cyprus"),
    CZ("CZ", "Czech Republic"),
    DK("DK", "Denmark"),
    EE("EE", "Estonia"),
    FI("FI", "Finland"),
    FR("FR", "France"),
    DE("DE", "Germany"),
    GR("GR", "Greece"),
    HU("HU", "Hungary"),
    IE("IE", "Ireland"),
    IT("IT", "Italy"),
    LV("LV", "Latvia"),
    LT("LT", "Lithuania"),
    LU("LU", "Luxembourg"),
    MT("MT", "Malta"),
    NL("NL", "Netherlands"),
    PL("PL", "Poland"),
    PT("PT", "Portugal"),
    RO("RO", "Romania"),
    SK("SK", "Slovakia"),
    SI("SI", "Slovenia"),
    ES("ES", "Spain"),
    SE("SE", "Sweden");

    companion object {
        fun fromCode(code: String): EuMemberState? = entries.find { it.code == code }
        fun isValidCode(code: String): Boolean = entries.any { it.code == code }
    }
}
