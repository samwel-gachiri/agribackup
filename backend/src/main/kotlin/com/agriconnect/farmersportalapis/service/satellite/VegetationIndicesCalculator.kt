package com.agriconnect.farmersportalapis.service.satellite

import org.springframework.stereotype.Service
import kotlin.math.sqrt

/**
 * Vegetation Indices Calculator
 * 
 * Calculates various vegetation indices from satellite imagery for enhanced
 * deforestation and vegetation health analysis.
 * 
 * Supported Indices:
 * - NDVI (Normalized Difference Vegetation Index)
 * - NDMI (Normalized Difference Moisture Index)
 * - EVI (Enhanced Vegetation Index)
 * - SAVI (Soil-Adjusted Vegetation Index)
 * - NDWI (Normalized Difference Water Index)
 * - NBR (Normalized Burn Ratio)
 * 
 * Band Mappings:
 * Sentinel-2: Blue=B2, Green=B3, Red=B4, NIR=B8, SWIR1=B11, SWIR2=B12
 * Landsat 8/9: Blue=B2, Green=B3, Red=B4, NIR=B5, SWIR1=B6, SWIR2=B7
 */
@Service
class VegetationIndicesCalculator {

    /**
     * Calculate NDVI (Normalized Difference Vegetation Index)
     * Formula: (NIR - Red) / (NIR + Red)
     * Range: -1 to 1
     * Interpretation:
     * - > 0.6: Dense healthy vegetation
     * - 0.3 - 0.6: Moderate vegetation
     * - 0.2 - 0.3: Sparse vegetation / grassland
     * - < 0.2: Bare soil, rock, water
     * - < 0: Non-vegetation (water, snow, clouds)
     */
    fun calculateNDVI(nir: Double, red: Double): Double {
        val denominator = nir + red
        return if (denominator == 0.0) 0.0 else (nir - red) / denominator
    }

    /**
     * Calculate NDMI (Normalized Difference Moisture Index)
     * Formula: (NIR - SWIR1) / (NIR + SWIR1)
     * Range: -1 to 1
     * Interpretation:
     * - > 0.5: High moisture content (healthy vegetation)
     * - 0.2 - 0.5: Moderate moisture
     * - < 0.2: Low moisture / water stress
     * - < 0: Bare soil, urban areas
     * 
     * Useful for:
     * - Detecting drought stress
     * - Fire risk assessment
     * - Irrigation monitoring
     */
    fun calculateNDMI(nir: Double, swir1: Double): Double {
        val denominator = nir + swir1
        return if (denominator == 0.0) 0.0 else (nir - swir1) / denominator
    }

    /**
     * Calculate EVI (Enhanced Vegetation Index)
     * Formula: 2.5 * ((NIR - Red) / (NIR + 6*Red - 7.5*Blue + 1))
     * Range: -1 to 1
     * Interpretation: Similar to NDVI but reduces atmospheric influence
     * 
     * Advantages:
     * - Better performance in dense canopy areas
     * - Reduces atmospheric effects
     * - More sensitive to vegetation structure
     */
    fun calculateEVI(nir: Double, red: Double, blue: Double): Double {
        val numerator = nir - red
        val denominator = nir + (6.0 * red) - (7.5 * blue) + 1.0
        return if (denominator == 0.0) 0.0 else 2.5 * (numerator / denominator)
    }

    /**
     * Calculate SAVI (Soil-Adjusted Vegetation Index)
     * Formula: ((NIR - Red) / (NIR + Red + L)) * (1 + L)
     * L = soil brightness correction factor (typically 0.5)
     * Range: -1 to 1
     * Interpretation: Similar to NDVI but accounts for soil background
     * 
     * Useful for:
     * - Areas with sparse vegetation
     * - Early growth stages
     * - Reducing soil background influence
     */
    fun calculateSAVI(nir: Double, red: Double, L: Double = 0.5): Double {
        val numerator = nir - red
        val denominator = nir + red + L
        return if (denominator == 0.0) 0.0 else ((numerator / denominator) * (1.0 + L))
    }

    /**
     * Calculate NDWI (Normalized Difference Water Index)
     * Formula: (Green - NIR) / (Green + NIR)
     * Range: -1 to 1
     * Interpretation:
     * - > 0.3: Water bodies
     * - 0 - 0.3: Wet soil, moisture
     * - < 0: Vegetation, dry soil
     * 
     * Useful for:
     * - Water body mapping
     * - Flood monitoring
     * - Wetland delineation
     */
    fun calculateNDWI(green: Double, nir: Double): Double {
        val denominator = green + nir
        return if (denominator == 0.0) 0.0 else (green - nir) / denominator
    }

    /**
     * Calculate NBR (Normalized Burn Ratio)
     * Formula: (NIR - SWIR2) / (NIR + SWIR2)
     * Range: -1 to 1
     * Interpretation:
     * - > 0.3: Healthy vegetation
     * - 0.1 - 0.3: Moderate vegetation
     * - < 0.1: Burned area, bare soil
     * 
     * Useful for:
     * - Burn severity mapping
     * - Fire scar detection
     * - Post-fire recovery monitoring
     */
    fun calculateNBR(nir: Double, swir2: Double): Double {
        val denominator = nir + swir2
        return if (denominator == 0.0) 0.0 else (nir - swir2) / denominator
    }

    /**
     * Calculate dNBR (delta Normalized Burn Ratio)
     * Compares NBR before and after an event to assess change
     * Formula: NBR_prefire - NBR_postfire
     * 
     * Interpretation:
     * - > 0.66: High severity burn
     * - 0.44 - 0.66: Moderate-high severity
     * - 0.27 - 0.44: Moderate-low severity
     * - 0.1 - 0.27: Low severity
     * - < 0.1: Unburned / regrowth
     */
    fun calculateDeltaNBR(nbrBefore: Double, nbrAfter: Double): Double {
        return nbrBefore - nbrAfter
    }

    /**
     * Comprehensive vegetation health assessment
     * Combines multiple indices for robust analysis
     */
    fun assessVegetationHealth(
        blue: Double,
        green: Double,
        red: Double,
        nir: Double,
        swir1: Double,
        swir2: Double
    ): VegetationHealthAssessment {
        val ndvi = calculateNDVI(nir, red)
        val ndmi = calculateNDMI(nir, swir1)
        val evi = calculateEVI(nir, red, blue)
        val savi = calculateSAVI(nir, red)
        val ndwi = calculateNDWI(green, nir)
        val nbr = calculateNBR(nir, swir2)

        // Overall health score (0-1 scale)
        val healthScore = (
            (ndvi.coerceIn(0.0, 1.0) * 0.3) +
            (ndmi.coerceIn(0.0, 1.0) * 0.2) +
            (evi.coerceIn(0.0, 1.0) * 0.2) +
            (savi.coerceIn(0.0, 1.0) * 0.2) +
            (nbr.coerceIn(0.0, 1.0) * 0.1)
        )

        val healthStatus = when {
            healthScore > 0.7 -> "HEALTHY"
            healthScore > 0.5 -> "MODERATE"
            healthScore > 0.3 -> "STRESSED"
            else -> "DEGRADED"
        }

        val waterPresence = ndwi > 0.3
        val burnEvidence = nbr < 0.1

        return VegetationHealthAssessment(
            ndvi = ndvi,
            ndmi = ndmi,
            evi = evi,
            savi = savi,
            ndwi = ndwi,
            nbr = nbr,
            healthScore = healthScore,
            healthStatus = healthStatus,
            waterPresence = waterPresence,
            burnEvidence = burnEvidence,
            moistureStress = ndmi < 0.2,
            interpretation = interpretVegetationHealth(healthScore, ndvi, ndmi, nbr)
        )
    }

    /**
     * Interpret vegetation health based on multiple indices
     */
    private fun interpretVegetationHealth(
        healthScore: Double,
        ndvi: Double,
        ndmi: Double,
        nbr: Double
    ): String {
        return when {
            nbr < 0.1 -> "Possible burn scar or severe degradation detected"
            ndmi < 0.2 -> "Vegetation water stress detected - potential drought impact"
            ndvi > 0.6 && ndmi > 0.4 -> "Healthy, well-watered vegetation canopy"
            ndvi < 0.3 && healthScore < 0.4 -> "Significant vegetation loss or sparse cover"
            ndvi < 0.3 -> "Possible deforestation or land clearing"
            healthScore > 0.6 -> "Overall healthy vegetation"
            else -> "Moderate vegetation health with some stress indicators"
        }
    }
}

/**
 * Comprehensive vegetation health assessment result
 */
data class VegetationHealthAssessment(
    val ndvi: Double,
    val ndmi: Double,
    val evi: Double,
    val savi: Double,
    val ndwi: Double,
    val nbr: Double,
    val healthScore: Double,
    val healthStatus: String,
    val waterPresence: Boolean,
    val burnEvidence: Boolean,
    val moistureStress: Boolean,
    val interpretation: String
)
