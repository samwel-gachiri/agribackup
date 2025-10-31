package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import org.locationtech.jts.geom.Geometry
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "deforestation_alerts")
class DeforestationAlert(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "alert_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_unit_id", nullable = false)
    var productionUnit: ProductionUnit,

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    var alertType: AlertType,

    @Column(name = "alert_geometry", columnDefinition = "geometry(Point,4326)")
    var alertGeometry: Geometry?,

    @Column(name = "latitude", precision = 10, scale = 8)
    var latitude: BigDecimal,

    @Column(name = "longitude", precision = 11, scale = 8)
    var longitude: BigDecimal,

    @Column(name = "alert_date", nullable = false)
    var alertDate: LocalDateTime,

    @Column(name = "confidence", precision = 5, scale = 4)
    var confidence: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    var severity: Severity,

    @Column(name = "distance_from_unit", precision = 10, scale = 2)
    var distanceFromUnit: BigDecimal,

    @Column(name = "source", nullable = false)
    var source: String,

    @Column(name = "source_id")
    var sourceId: String?,

    @Column(name = "metadata", columnDefinition = "TEXT")
    var metadata: String?,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?,

    @Column(name = "hedera_hash")
    var hederaHash: String?,

    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,
    
    @Column(name = "is_reviewed")
    var isReviewed: Boolean = false,
    
    @Column(name = "reviewed_at")
    var reviewedAt: LocalDateTime? = null,
    
    @Column(name = "reviewer_id")
    var reviewerId: String? = null,
    
    @Column(name = "review_notes", columnDefinition = "TEXT")
    var reviewNotes: String? = null
) {
    
    enum class AlertType {
        GLAD_DEFORESTATION,
        FIRE_ALERT,
        TREE_LOSS,
        LAND_USE_CHANGE,
        ILLEGAL_LOGGING
    }
    
    enum class Severity {
        HIGH,
        MEDIUM,
        LOW,
        INFO
    }
}