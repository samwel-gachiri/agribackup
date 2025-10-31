package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "supply_chain_events")
class SupplyChainEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    var batch: EudrBatch,

    @Column(name = "from_entity_id")
    var fromEntityId: String?,

    @Column(name = "from_entity_type")
    var fromEntityType: String?,

    @Column(name = "to_entity_id", nullable = false)
    var toEntityId: String,

    @Column(name = "to_entity_type", nullable = false)
    var toEntityType: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    var actionType: SupplyChainActionType,

    @Column(name = "event_timestamp", nullable = false)
    var eventTimestamp: LocalDateTime,

    @Column(name = "location_coordinates")
    var locationCoordinates: String?,

    @Column(name = "transport_method")
    var transportMethod: String?,

    @Column(name = "document_references", columnDefinition = "TEXT")
    var documentReferences: String?,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
)

enum class SupplyChainActionType {
    HARVEST, COLLECTION, AGGREGATION, PROCESSING, TRANSPORT, EXPORT, DELIVERY
}