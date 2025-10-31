package com.agriconnect.farmersportalapis.domain.common.enums

enum class FarmProduceStatus {
    INACTIVE,
    ACTIVE,
    IS_SELLING
}

enum class FarmerProduceStatus {
    INACTIVE,
    ON_FARM,
    ON_SALE,
    HARVEST_PLANNED,
    GROWING,
    READY_TO_HARVEST,
    HARVESTED
}

enum class ProduceListingStatus {
    ACTIVE,
    INACTIVE,
    CANCELLED,
    CLOSED
}

val activeListings = arrayOf(ProduceListingStatus.ACTIVE.name)
enum class OrderStatus {
    PENDING_ACCEPTANCE,
    DECLINED,
    BOOKED_FOR_SUPPLY,
    SUPPLIED,
    SUPPLIED_AND_PAID,
    CANCELLED
}

enum class ConnectionStatus {
    ACTIVE,
    INACTIVE
}

enum class RequestType {
    FEATURE,
    BUG,
    IMPROVEMENT
}

enum class RequestStatus {
    OPEN,
    IN_PROGRESS,
    COMPLETED,
    REJECTED,
    DUPLICATE
}


enum class MessageType {
    CHAT,
    JOIN,
    LEAVE
}

enum class ExporterVerificationStatus {
    PENDING,
    UNDER_REVIEW,
    VERIFIED,
    REJECTED
}

enum class PickupStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    RESCHEDULED
}

// New route-level statuses for multi-stop pickup routing
enum class PickupRouteStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

// Per-stop status within a route
enum class PickupStopStatus {
    PENDING,
    ARRIVED,
    SKIPPED,
    COMPLETED
}
