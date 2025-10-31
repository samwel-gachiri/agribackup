package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.AddFarmerToZoneDto
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.*

// Minimal slice test without loading full context: inline controller reproducing binding
@RestController
@RequestMapping("/test-binding")
private class SingleFieldTestController {
    @PostMapping("/zones/{zoneId}/farmers")
    fun add(@PathVariable zoneId: String, @RequestBody dto: AddFarmerToZoneDto) = dto
}

class AddFarmerToZoneControllerBindingTest {
    private val objectMapper = jacksonObjectMapper()
    private val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(SingleFieldTestController()).build()

    @Test
    fun `single field body binds correctly`() {
        val json = """{"farmerId":"f-123"}"""
        val mvcResult = mockMvc.perform(
            post("/test-binding/zones/z-1/farmers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andReturn()
        val returned = objectMapper.readValue(mvcResult.response.contentAsString, AddFarmerToZoneDto::class.java)
        assertEquals("f-123", returned.farmerId)
    }
}
