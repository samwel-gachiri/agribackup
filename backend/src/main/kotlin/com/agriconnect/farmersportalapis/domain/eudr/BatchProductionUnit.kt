package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "batch_production_units")
class BatchProductionUnit(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "relationship_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    var batch: EudrBatch,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_unit_id", nullable = false)
    var productionUnit: ProductionUnit,

    @Column(name = "quantity_allocated", nullable = false)
    var quantityAllocated: BigDecimal,

    @Column(name = "percentage_contribution")
    var percentageContribution: BigDecimal?,

    @CreationTimestamp
    @Column(name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now()
)