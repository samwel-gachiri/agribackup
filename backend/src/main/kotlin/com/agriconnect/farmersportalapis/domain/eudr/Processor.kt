package com.agriconnect.farmersportalapis.domain.eudr

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "processors")
class Processor(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "processor_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    var userProfile: UserProfile,

    @Column(name = "facility_name", nullable = false)
    var facilityName: String,

    @Column(name = "facility_address", nullable = false)
    var facilityAddress: String,

    @Column(name = "processing_capabilities", columnDefinition = "TEXT")
    var processingCapabilities: String?,

    @Column(name = "certification_details", columnDefinition = "TEXT")
    var certificationDetails: String?,

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    var verificationStatus: ProcessorVerificationStatus = ProcessorVerificationStatus.PENDING,

    @Column(name = "hedera_account_id", length = 50)
    var hederaAccountId: String? = null,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "processor", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var processingEvents: MutableList<ProcessingEvent> = mutableListOf(),

    @OneToMany(mappedBy = "processor", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var batches: MutableList<EudrBatch> = mutableListOf()
)

enum class ProcessorVerificationStatus {
    PENDING, VERIFIED, REJECTED, SUSPENDED
}