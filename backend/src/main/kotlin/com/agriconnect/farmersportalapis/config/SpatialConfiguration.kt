package com.agriconnect.farmersportalapis.config

import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpatialConfiguration {

    companion object {
        private const val WGS84_SRID = 4326
    }

    @Bean
    fun geometryFactory(): GeometryFactory {
        val precisionModel = PrecisionModel(PrecisionModel.FLOATING)
        val geometryFactory = GeometryFactory(precisionModel, WGS84_SRID)
        return geometryFactory
    }
}