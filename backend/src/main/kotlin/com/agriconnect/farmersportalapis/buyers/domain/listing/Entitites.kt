package com.agriconnect.farmersportalapis.buyers.domain.request

import com.agriconnect.farmersportalapis.buyers.application.annotations.Aggregate
import com.agriconnect.farmersportalapis.buyers.domain.common.enums.OrderStatus
import com.agriconnect.farmersportalapis.buyers.domain.common.enums.ProduceRequestStatus
import com.agriconnect.farmersportalapis.buyers.domain.common.valueobject.Money
import com.agriconnect.farmersportalapis.buyers.domain.profile.PreferredProduce
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Aggregate
@Entity
@Table(name="produce_requests")
class ProduceRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "produceRequestId", length = 36, nullable = false, unique = true)
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
    var status: ProduceRequestStatus,

    @Column(name = "date_created")
    var dateCreated: LocalDateTime,

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "buyerId", nullable = false, referencedColumnName = "buyerId")
//    var buyer: Buyer,
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "farmProduceId", nullable = false, referencedColumnName = "farmProduceId")
//    var farmProduce: FarmProduce,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "preferred_produces_id", nullable = false, referencedColumnName = "id")
    var preferredProduce: PreferredProduce,

    @OneToMany(mappedBy = "produceRequest", cascade = [CascadeType.ALL], orphanRemoval = true)
    val requestOrders: MutableList<RequestOrder> = mutableListOf()
)

@Entity
@Table(name="request_orders")
class RequestOrder(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "orderId", length = 36, nullable = false, unique = true)
    var id: String = "",

    @Column
    var farmerId: String,

    @ManyToOne
    @JoinColumn(name = "produceRequestId", nullable = false, referencedColumnName = "produceRequestId")
    @JsonIgnore
    var produceRequest: ProduceRequest,

    @Column
    var dateCreated: LocalDateTime,

    @Column(nullable = true)
    var dateAccepted: LocalDateTime? = null,

    @Column(nullable = true)
    var dateSupplied: LocalDateTime? = null,

    @Column(nullable = true)
    var datePaid: LocalDateTime? = null,


    @Column
    var quantity: Double,

    @Column
    @Enumerated(EnumType.STRING)
    var status: OrderStatus = OrderStatus.BOOKED_FOR_SUPPLY

)