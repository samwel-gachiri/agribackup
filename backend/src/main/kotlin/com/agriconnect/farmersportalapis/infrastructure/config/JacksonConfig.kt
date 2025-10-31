package com.agriconnect.farmersportalapis.infrastructure.config

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JacksonConfig {

    @Bean
    fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer = Jackson2ObjectMapperBuilderCustomizer()
}

class Jackson2ObjectMapperBuilderCustomizer : (Jackson2ObjectMapperBuilder) -> Unit {
    override fun invoke(builder: Jackson2ObjectMapperBuilder) {
        builder
            .modules(kotlinModule())
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .featuresToDisable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
            )
            .featuresToEnable(
                MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS,
                MapperFeature.ALLOW_EXPLICIT_PROPERTY_RENAMING
            )
            .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
    }
}
