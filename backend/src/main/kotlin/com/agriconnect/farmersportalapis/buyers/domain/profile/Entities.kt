package com.agriconnect.farmersportalapis.buyers.domain.profile

import com.agriconnect.farmersportalapis.buyers.application.annotations.Aggregate
import com.agriconnect.farmersportalapis.buyers.domain.common.enums.BuyerProduceStatus
import com.agriconnect.farmersportalapis.buyers.domain.common.model.BSFarmProduce
import com.agriconnect.farmersportalapis.buyers.domain.common.model.BuyerLocation
import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Aggregate
@Entity
@Table(name= "buyers")
class Buyer(
    @Id
    @Column(name = "buyer_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    var userProfile: UserProfile,

    @Column(name = "company_name")
    var companyName: String? = null,

    @Column(name = "business_type")
    var businessType: String? = null,

    @OneToMany(mappedBy = "buyer", cascade = [CascadeType.ALL], orphanRemoval = true)
    val preferredProduces: MutableList<PreferredProduce> = mutableListOf(),

    @OneToOne(mappedBy = "buyer", fetch = FetchType.EAGER, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    var location: BuyerLocation? = null
)

@Entity
@Table(name = "preferred_produces")
class PreferredProduce(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyerId", nullable = false, referencedColumnName = "buyer_id")
    @JsonIgnore
    var buyer: Buyer,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmProduceId", nullable = false, referencedColumnName = "farmProduceId")
    var BSFarmProduce: BSFarmProduce,

    @Column
    @Enumerated(EnumType.STRING)
    var status: BuyerProduceStatus,
)