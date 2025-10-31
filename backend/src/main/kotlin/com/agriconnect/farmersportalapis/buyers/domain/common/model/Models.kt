package com.agriconnect.farmersportalapis.buyers.domain.common.model

import com.agriconnect.farmersportalapis.buyers.domain.common.enums.FarmProduceStatus
import com.agriconnect.farmersportalapis.buyers.domain.profile.Buyer
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

@Entity
@Table(name = "bs_farm_produces")
class BSFarmProduce(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "farmProduceId", length = 36, nullable = false, unique = true)
    var id: String = "",

    @Column(name = "name")
    var name: String,

    @Column(name = "description")
    var description: String,

    @Column
    var farmingType: String,

    @Column
    @Enumerated(EnumType.STRING)
    var status: FarmProduceStatus
)

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name="bs_locations")
class BuyerLocation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @Column
    var latitude: Double,

    @Column
    var longitude: Double,

    @Column(name="customName")
    var customName: String,

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "buyerId")
    var buyer: Buyer
//    @Id
//    @Column(name = "buyerId")
//    var buyerId: String,
) {
//    constructor(latitude: Double, longitude: Double, customName: String, buyer: Buyer) : this(
//        id = "",
//        latitude = latitude,
//        longitude = longitude,
//        customName = customName,
//        buyer = buyer
//    )
}


