package com.agriconnect.farmersportalapis.domain.eudr

import com.agriconnect.farmersportalapis.domain.profile.Farmer
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.io.geojson.GeoJsonWriter
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "production_units")
class ProductionUnit(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "unit_id", length = 36, nullable = false, unique = true)
    var id: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_id", nullable = false)
    var farmer: Farmer,

    @Column(name = "unit_name", nullable = false)
    var unitName: String,

    @JsonIgnore
    @Column(name = "parcel_geojson", columnDefinition = "geometry(Polygon,4326)")
    var parcelGeometry: Geometry?,

    @Column(name = "area_hectares", nullable = false)
    var areaHectares: BigDecimal,

    @Column(name = "wgs84_coordinates", columnDefinition = "TEXT")
    var wgs84Coordinates: String?,

    @Column(name = "administrative_region")
    var administrativeRegion: String?,

    @Column(name = "last_verified_at")
    var lastVerifiedAt: LocalDateTime?,

    @Column(name = "hedera_hash")
    var hederaHash: String?,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?,

    @JsonIgnore
    @OneToMany(mappedBy = "productionUnit", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var deforestationAlerts: MutableList<DeforestationAlert> = mutableListOf(),

    @JsonIgnore
    @OneToMany(mappedBy = "productionUnit", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var batchRelationships: MutableList<BatchProductionUnit> = mutableListOf()
) {
    /**
     * Returns the parcel geometry as a GeoJSON string for API responses
     */
    @JsonProperty("parcelGeometryGeoJson")
    fun getParcelGeometryGeoJson(): String? {
        return try {
            parcelGeometry?.let { 
                GeoJsonWriter().write(it)
            }
        } catch (e: Exception) {
            null
        }
    }
}