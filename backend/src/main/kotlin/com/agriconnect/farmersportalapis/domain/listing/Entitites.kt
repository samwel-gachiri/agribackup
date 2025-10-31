package com.agriconnect.farmersportalapis.domain.listing

import com.agriconnect.farmersportalapis.application.annotations.Aggregate
import com.agriconnect.farmersportalapis.domain.common.enums.OrderStatus
import com.agriconnect.farmersportalapis.domain.common.enums.ProduceListingStatus
import com.agriconnect.farmersportalapis.domain.common.valueobject.Money
import com.agriconnect.farmersportalapis.domain.profile.FarmerProduce
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Aggregate
@Entity
@Table(name="produce_listings")
class ProduceListing(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "produceListingId", length = 36, nullable = false, unique = true)
    var id: String = "",

    @Column(name = "quantity")
    var quantity: Double,

    @Embedded
    var price: Money,

    @Column
    var unit: String,

    @Column
    var rating: Double = 0.00,

    @Column
    @Enumerated(EnumType.STRING)
    var status: ProduceListingStatus,

    @Column
    var createdAt: LocalDateTime,

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "farmerId", nullable = false, referencedColumnName = "farmerId")
//    var farmer: Farmer,
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "farmProduceId", nullable = false, referencedColumnName = "farmProduceId")
//    var farmProduce: FarmProduce,

    @ElementCollection
    @CollectionTable(name = "listing_images_urls", joinColumns = [JoinColumn(name = "produce_listing_id")])
    @Column(name = "image_url")
    var imageUrls: List<String>? = listOf(),

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmer_produces_id", nullable = false, referencedColumnName = "id")
    var farmerProduce: FarmerProduce,

    @OneToMany(mappedBy = "produceListing", cascade = [CascadeType.ALL], orphanRemoval = true)
    val listingOrders: MutableList<ListingOrder> = mutableListOf()
)

@Entity
@Table(name="listing_orders")
class ListingOrder(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "orderId", length = 36, nullable = false, unique = true)
    var id: String = "",

    @Column
    var buyerId: String,

    @ManyToOne
    @JoinColumn(name = "produceListingId", nullable = false, referencedColumnName = "produceListingId")
    @JsonIgnore
    var produceListing: ProduceListing,

    @Column
    var dateCreated: LocalDateTime,

    @Column(nullable = true)
    var dateAccepted: LocalDateTime? = null,

    @Column(nullable = true)
    var dateDeclined: LocalDateTime? = null,

    @Column(nullable = true)
    var dateSupplied: LocalDateTime? = null,

    @Column(nullable = true)
    var datePaid: LocalDateTime? = null,


    @Column
    var quantity: Double,

    @Column
    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.BOOKED_FOR_SUPPLY,

    @Column(name="buyer_comment")
    var buyerComment: String? = "",

    @Column(name="farmer_comment")
    var farmerComment: String? = ""
)