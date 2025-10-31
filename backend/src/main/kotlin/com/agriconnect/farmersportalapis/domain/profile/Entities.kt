package com.agriconnect.farmersportalapis.domain.profile

import com.agriconnect.farmersportalapis.application.annotations.Aggregate
import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.common.enums.FarmerProduceStatus
import com.agriconnect.farmersportalapis.domain.common.model.FarmProduce
import com.agriconnect.farmersportalapis.domain.common.model.Location
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Aggregate
@Entity
@Table(name= "farmers")
class Farmer(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "farmer_id", length = 36, nullable = false, unique = true)
    var id: String? = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    var userProfile: UserProfile,

    @Column(name = "farm_size")
    var farmSize: Double? = null,

    @Column(name = "farm_name")
    var farmName: String? = "",

    @OneToMany(mappedBy = "farmer", cascade = [CascadeType.ALL], orphanRemoval = true)
    val farmerProduces: MutableList<FarmerProduce> = mutableListOf(),

    @OneToOne(mappedBy = "farmer", fetch = FetchType.EAGER, cascade = [CascadeType.ALL], orphanRemoval = true)
    var location: Location? = null,

    @OneToMany(mappedBy = "farmer", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var farmerCollections: MutableList<com.agriconnect.farmersportalapis.domain.eudr.FarmerCollection> = mutableListOf()

)

@Entity
@Table(name = "farmer_produces")
class FarmerProduce(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @Column(nullable = true)
    var description: String?,

    @Column(nullable = true)
    var farmingType: String?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmerId", nullable = false, referencedColumnName = "farmer_id")
    @JsonIgnore
    var farmer: Farmer,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmProduceId", nullable = false, referencedColumnName = "farmProduceId")
    var farmProduce: FarmProduce,

    @Column
    @Enumerated(EnumType.STRING)
    var status: FarmerProduceStatus,

    @ElementCollection
    @CollectionTable(name = "produce_images_urls", joinColumns = [JoinColumn(name = "produce_id")])
    @Column(name = "image_url")
    var imageUrls: List<String>? = listOf(),

    @OneToMany(mappedBy = "farmerProduce", cascade = [CascadeType.ALL], orphanRemoval = true)
    val yields: MutableList<ProduceYield> = mutableListOf(),

    // --- Harvest prediction & tracking fields ---
    @Column(name = "planting_date")
    var plantingDate: java.time.LocalDate? = null,

    @Column(name = "predicted_species")
    var predictedSpecies: String? = null,

    @Column(name = "prediction_confidence")
    var predictionConfidence: Double? = null,

    @Column(name = "predicted_harvest_date")
    var predictedHarvestDate: java.time.LocalDate? = null,

    @Column(name = "ai_model_version")
    var aiModelVersion: String? = null,

    @Column(name = "actual_harvest_date")
    var actualHarvestDate: java.time.LocalDate? = null,

    // --- Manual Override fields ---
    @Column(name = "manual_override")
    var manualOverride: Boolean? = false,

    @Column(name = "manual_override_reason")
    var manualOverrideReason: String? = null,

    @Column(name = "manual_override_notes")
    var manualOverrideNotes: String? = null,

    @Column(name = "manual_override_date")
    var manualOverrideDate: java.time.LocalDateTime? = null
)

@Entity
@Table(name = "produce_yields")
class ProduceYield(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_produce_id", nullable = false)
    @JsonIgnore
    var farmerProduce: FarmerProduce,

    @Column(name = "season_year")
    var seasonYear: Int?,

    @Column(name = "season_name")
    var seasonName: String?,

    @Column(name = "yield_amount")
    var yieldAmount: Double,

    @Column(name = "yield_unit")
    var yieldUnit: String,

    @Column(name = "listed_amount")
    var listedAmount: Double = 0.0,

    @Column(name = "remaining_amount")
    var remainingAmount: Double = 0.0,

    @Column(name = "harvest_date")
    var harvestDate: java.time.LocalDate? = null,

    @Column(name = "notes")
    var notes: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // Enhanced yield recording fields
    @Column(name = "quality_grade")
    var qualityGrade: String? = null,

    @Column(name = "moisture_content")
    var moistureContent: Double? = null,

    @Column(name = "storage_location")
    var storageLocation: String? = null
)
