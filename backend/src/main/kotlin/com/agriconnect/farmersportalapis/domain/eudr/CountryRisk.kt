package com.agriconnect.farmersportalapis.domain.eudr

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "country_risk_matrix")
class CountryRisk(
    @Id
    @Column(name = "country_code", length = 3)
    var countryCode: String,

    @Column(name = "country_name", nullable = false)
    var countryName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    var riskLevel: CountryRiskLevel,

    @Column(name = "risk_justification", columnDefinition = "TEXT")
    var riskJustification: String?,

    @Column(name = "last_updated", nullable = false)
    var lastUpdated: LocalDateTime,

    @Column(name = "updated_by", nullable = false)
    var updatedBy: String
)