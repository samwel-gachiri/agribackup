package com.agriconnect.farmersportalapis.domain.common.model

import com.agriconnect.farmersportalapis.domain.common.enums.ConnectionStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "farmer_buyer_connections")
class FarmerBuyerConnection(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @Column(name="farmer_id")
    var farmerId: String,

    @Column(name="buyer_id")
    var buyerId: String,

    @Enumerated(EnumType.STRING)
    var status: ConnectionStatus,

    @Column(name = "notes", length = 500)
    var notes: String? = null,

    @Column(name = "notification_preference")
    var notificationPreference: String? = null,

    @Column(name = "priority_level")
    var priorityLevel: String? = null,

    @Column(name = "created_at")
    var createdAt: LocalDateTime
)