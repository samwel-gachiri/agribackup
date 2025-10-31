package com.agriconnect.farmersportalapis.domain.eudr

import com.agriconnect.farmersportalapis.domain.profile.Exporter
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(
    name = "exporter_processor_connections",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["exporter_id", "processor_id"])
    ]
)
class ExporterProcessorConnection(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exporter_id", nullable = false)
    var exporter: Exporter,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processor_id", nullable = false)
    var processor: Processor,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: ConnectionStatus = ConnectionStatus.ACTIVE,

    @CreationTimestamp
    @Column(name = "connected_at", nullable = false)
    var connectedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "notes", columnDefinition = "TEXT")
    var notes: String? = null
)

enum class ConnectionStatus {
    ACTIVE, INACTIVE, SUSPENDED
}
