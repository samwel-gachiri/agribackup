package com.agriconnect.farmersportalapis.buyers.domain.common.enums

enum class FarmProduceStatus {
    INACTIVE,
    ACTIVE,
    IS_SELLING
}

enum class BuyerProduceStatus {
    INACTIVE,
    ACTIVE,
    REQUESTING,
}

enum class ProduceRequestStatus {
    ACTIVE,
    INACTIVE,
    CANCELLED,
    CLOSED
}

val activeRequests = arrayOf(ProduceRequestStatus.ACTIVE.name)
enum class OrderStatus {
    PENDING_ACCEPTANCE,
    BOOKED_FOR_SUPPLY,
    SUPPLIED,
    SUPPLIED_AND_PAID,
    CANCELLED
}