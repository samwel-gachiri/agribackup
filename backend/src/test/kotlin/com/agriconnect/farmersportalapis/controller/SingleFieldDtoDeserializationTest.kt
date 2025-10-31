package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.AddFarmerToZoneDto
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SingleFieldDtoDeserializationTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `deserialize single field dto`() {
        val json = """{"farmerId":"abc123"}"""
        val dto = mapper.readValue(json, AddFarmerToZoneDto::class.java)
        assertEquals("abc123", dto.farmerId)
    }
}
