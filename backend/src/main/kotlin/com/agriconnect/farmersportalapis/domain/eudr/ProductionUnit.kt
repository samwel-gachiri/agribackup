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

    @JsonIgnore
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

    @Column(name = "country_code", length = 3)
    var countryCode: String? = null,

    @Column(name = "last_verified_at")
    var lastVerifiedAt: LocalDateTime?,

    @Column(name = "hedera_hash")
    var hederaHash: String?,

    @Column(name = "hedera_transaction_id")
    var hederaTransactionId: String?,

    /**
     * Single geolocation point (lat,lon) for plots ≤4 hectares per EUDR Article 9(1)(d).
     * For plots >4 hectares, polygon geometry is mandatory and this field is ignored.
     * Format: "latitude,longitude" e.g., "-1.2921,36.8219"
     */
    @Column(name = "geolocation_point")
    var geolocationPoint: String? = null,

    /**
     * Radius in meters around the geolocation point.
     * Used to define the scope for deforestation checks and calculate area.
     * Only applicable when using geolocation point instead of polygon.
     */
    @Column(name = "radius_meters")
    var radiusMeters: Double? = null,

    /**
     * Type of geolocation: POLYGON or POINT
     * Determines which geometry fields are used.
     */
    @Column(name = "geolocation_type", length = 20)
    var geolocationType: String = "POLYGON",

    /**
     * When true, geometry cannot be modified (set after first batch assignment or deforestation verification).
     * This ensures EUDR audit trail immutability.
     */
    @Column(name = "is_locked", nullable = false)
    var isLocked: Boolean = false,

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