package com.agriconnect.farmersportalapis.domain.common.model

import com.agriconnect.farmersportalapis.domain.common.enums.FarmProduceStatus
import com.agriconnect.farmersportalapis.domain.common.enums.MessageType
import com.agriconnect.farmersportalapis.domain.common.enums.RequestStatus
import com.agriconnect.farmersportalapis.domain.common.enums.RequestType
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import lombok.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "farm_produces")
class FarmProduce(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "farmProduceId", length = 36, nullable = false, unique = true)
    var id: String = "",

    @Column(name = "name")
    var name: String,

    @Column(name = "description", nullable = true)
    var description: String?,

    @Column(name = "farmingType", nullable = true)
    var farmingType: String?,

    @Column
    @Enumerated(EnumType.STRING)
    var status: FarmProduceStatus
)

@Entity
@Table(name="fs_locations")
class Location(
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
    @JoinColumn(name = "farmerId")
    var farmer: Farmer?
){
    // Convenience constructor
    constructor(latitude: Double, longitude: Double, customName: String) : this(
        id = "",
        latitude = latitude,
        longitude = longitude,
        customName = customName,
        farmer = null
    )
}


@Entity
@Table(name = "feature_request")
data class FeatureRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: String,

    @Column(name = "user_section", nullable = false, length = 50)
    val userSection: String,

    @Column(name = "request_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    val requestType: RequestType,

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    val message: String,

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var status: RequestStatus = RequestStatus.OPEN,

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    var adminNotes: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "ai_generated", nullable = false)
    val aiGenerated: Boolean = false,

    @Column(name = "original_prompt", columnDefinition = "TEXT")
    val originalPrompt: String? = null,
)


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class ChatMessage {
    var type: MessageType? = null
    var content: String? = null
    var sender: String? = null
    var responseType: String? = null
    var userSection: String? = "farmer"
}
