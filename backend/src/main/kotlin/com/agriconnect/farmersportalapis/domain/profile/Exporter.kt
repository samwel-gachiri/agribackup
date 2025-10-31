package com.agriconnect.farmersportalapis.domain.profile

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.common.enums.ExporterVerificationStatus
import com.agriconnect.farmersportalapis.domain.common.enums.PickupStatus
import com.agriconnect.farmersportalapis.domain.common.enums.PickupRouteStatus
import com.agriconnect.farmersportalapis.domain.common.enums.PickupStopStatus
import com.agriconnect.farmersportalapis.domain.listing.ProduceListing
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "exporters")
class Exporter(
    @Id
    @Column(name = "exporter_id", length = 36)
    var id: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    var userProfile: UserProfile,

    @Column(name = "license_id", nullable = true, unique = true)
    var licenseId: String?,

    @Column(name = "company_name")
    var companyName: String? = null,

    @Column(name = "company_desc")
    var companyDesc: String? = null,

    @Column(name = "verification_status")
    @Enumerated(EnumType.STRING)
    var verificationStatus: ExporterVerificationStatus = ExporterVerificationStatus.PENDING,

    @Column(name = "export_license_form_url")
    var exportLicenseFormUrl: String? = null,

    @OneToMany(mappedBy = "exporter", cascade = [CascadeType.ALL], orphanRemoval = true)
    var zones: MutableList<Zone> = mutableListOf(),

    @OneToMany(mappedBy = "exporter", cascade = [CascadeType.ALL])
    var farmerRelationships: MutableList<FarmerExporterRelationship> = mutableListOf(),
)

@Entity
@Table(name = "farmer_exporter_relationships")
class FarmerExporterRelationship(
    @Id
    @Column(name = "relationship_id", length = 36)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    var farmer: Farmer,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exporter_id", nullable = false)
    var exporter: Exporter,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    var zone: Zone? = null,

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "pickup_schedules")
class PickupSchedule(
    @Id
    @Column(name = "schedule_id", length = 36)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    var farmer: Farmer,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exporter_id", nullable = false)
    var exporter: Exporter,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produce_listing_id", nullable = false)
    var produceListing: ProduceListing,

    @Column(name = "scheduled_date", nullable = false)
    var scheduledDate: LocalDateTime,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: PickupStatus = PickupStatus.SCHEDULED,

    @Column(name = "pickup_notes")
    var pickupNotes: String? = null,

    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "pickup_routes")
class PickupRoute(
    @Id
    @Column(name = "route_id", length = 36)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exporter_id", nullable = false)
    var exporter: Exporter,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_supervisor_id", nullable = false)
    var zoneSupervisor: ZoneSupervisor,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", nullable = false)
    var zone: Zone,

    @Column(name = "scheduled_date", nullable = false)
    var scheduledDate: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: PickupRouteStatus = PickupRouteStatus.PLANNED,

    @Column(name = "total_distance_km")
    var totalDistanceKm: Double? = null,

    @Column(name = "estimated_duration_minutes")
    var estimatedDurationMinutes: Int? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
,

    @OneToMany(mappedBy = "route", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var stops: MutableList<PickupRouteStop> = mutableListOf()
)

@Entity
@Table(name = "pickup_route_stops")
class PickupRouteStop(
    @Id
    @Column(name = "stop_id", length = 36)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    var route: PickupRoute,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    var farmer: Farmer,

    @Column(name = "sequence_order", nullable = false)
    var sequenceOrder: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: PickupStopStatus = PickupStopStatus.PENDING,

    @Column(name = "arrival_time")
    var arrivalTime: LocalDateTime? = null,

    @Column(name = "completion_time")
    var completionTime: LocalDateTime? = null,

    @Column(name = "notes")
    var notes: String? = null
)

@Entity
@Table(name = "system_admins")
class SystemAdmin(
    @Id
    @Column(name = "system_admin_id", length = 36)
    var id: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    var userProfile: UserProfile,

    @Column(name = "status", nullable = false)
    var status: String = "ACTIVE",

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "zone_supervisors")
class ZoneSupervisor(
    @Id
    @Column(name = "zone_supervisor_id", length = 36)
    var id: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    var userProfile: UserProfile,

    @Column(name = "status", nullable = false)
    var status: String = "ACTIVE",

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @ManyToMany
    @JoinTable(
        name = "zone_supervisor_zones",
        joinColumns = [JoinColumn(name = "zone_supervisor_id")],
        inverseJoinColumns = [JoinColumn(name = "zone_id")]
    )
    var zones: MutableList<Zone> = mutableListOf()
)

@Entity
@Table(name = "zones")
class Zone(
    @Id
    @Column(name = "zone_id", length = 36)
    var id: String = "",

    @Column(nullable = false)
    var name: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exporter_id", nullable = false)
    var exporter: Exporter,

    @Column(name = "produce_type")
    var produceType: String? = null,

    @Column(name = "center_latitude", nullable = false)
    var centerLatitude: java.math.BigDecimal,

    @Column(name = "center_longitude", nullable = false)
    var centerLongitude: java.math.BigDecimal,

    @Column(name = "radius_km", nullable = false)
    var radiusKm: java.math.BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    // DB constraint (changeSet 20250720-8) allows NULL + FK with ON DELETE SET NULL – reflect here
    @JoinColumn(name = "creator_id", nullable = true)
    var creator: UserProfile? = null,

    @Column(name = "comments")
    var comments: String? = null,

    @OneToMany(mappedBy = "zone")
    var farmerRelationships: MutableList<FarmerExporterRelationship> = mutableListOf(),

    @ManyToMany(mappedBy = "zones")
    var supervisors: MutableList<ZoneSupervisor> = mutableListOf()
)
