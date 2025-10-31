package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.ExporterResponseDto
import com.agriconnect.farmersportalapis.application.dtos.PickupScheduleResponseDto
import com.agriconnect.farmersportalapis.application.dtos.UpdateExporterRequestDto
import com.agriconnect.farmersportalapis.application.dtos.ZoneResponseDto
import com.agriconnect.farmersportalapis.service.common.impl.ExporterService
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.common.enums.ExporterVerificationStatus
import com.agriconnect.farmersportalapis.domain.common.enums.PickupStatus
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@WebMvcTest(ExporterController::class)
class ExporterControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var exporterService: ExporterService

    private lateinit var testExporterResponse: ExporterResponseDto
    private lateinit var testZoneResponse: ZoneResponseDto
    private lateinit var testPickupScheduleResponse: PickupScheduleResponseDto

    @BeforeEach
    fun setup() {
        testExporterResponse = ExporterResponseDto(
            id = "test-id",
            licenseId = "LICENSE123",
            companyName = "",
            companyDesc = "",
            verificationStatus = ExporterVerificationStatus.PENDING,
            exportLicenseFormUrl = "d",
        )

        testZoneResponse = ZoneResponseDto(
            id = "zone-id",
            name = "Test Zone",
            produceType = "Vegetables",
            centerLatitude = BigDecimal("1.2345"),
            centerLongitude = BigDecimal("6.7890"),
            radiusKm = BigDecimal("10.0"),
            exporterId = "test-id",
            farmerCount = 0,
            comments = "",
            creatorId = "",
            supervisorIds = listOf(),
        )

        testPickupScheduleResponse = PickupScheduleResponseDto(
            id = "pickup-id",
            exporterId = "test-id",
            farmerId = "farmer-id",
            produceListingId = "listing-id",
            scheduledDate = LocalDateTime.now(),
            status = PickupStatus.SCHEDULED,
            pickupNotes = "Test pickup",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

//    @Test
//    fun `createExporter should return success response`() {
//        val request = CreateExporterRequestDto(
//            id = "amdka",
//            name = "Test Exporter",
//            licenseId = "LICENSE123",
//            email = "test@example.com",
//            phoneNumber = "1234567890"
//        )
//
//        whenever(exporterService.createExporter(any()))
//            .thenReturn(ResultFactory.getSuccessResult(testExporterResponse))
//
//        mockMvc.perform(
//            MockMvcRequestBuilders.post("/exporters-service/exporter")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request)))
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(testExporterResponse.id))
//    }

    @Test
    fun `getExporter should return exporter details`() {
        whenever(exporterService.getExporter("test-id"))
            .thenReturn(ResultFactory.getSuccessResult(testExporterResponse))

        mockMvc.perform(MockMvcRequestBuilders.get("/exporters-service/exporter/test-id"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(testExporterResponse.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.companyName").value(testExporterResponse.companyName))
    }

    @Test
    fun `updateExporter should update and return exporter details`() {
        val request = UpdateExporterRequestDto(
            name = "Updated Name",
            email = "updated@example.com",
            phoneNumber = "0987654321"
        )

        whenever(exporterService.updateExporter(any(), any()))
            .thenReturn(ResultFactory.getSuccessResult(testExporterResponse))

        mockMvc.perform(
            MockMvcRequestBuilders.put("/exporters-service/exporter/test-id")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
    }

    @Test
    fun `verifyExporter should return verified exporter`() {
        whenever(exporterService.verifyExporter("test-id"))
            .thenReturn(
                ResultFactory.getSuccessResult(testExporterResponse.copy(
                verificationStatus = ExporterVerificationStatus.VERIFIED
            )))

        mockMvc.perform(MockMvcRequestBuilders.put("/exporters-service/exporter/test-id/verify"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.verificationStatus").value("VERIFIED"))
    }

//    @Test
//    fun `createZone should return created zone`() {
//        val request = CreateZoneRequestDto(
//            exporterId = "test-id",
//            name = "Test Zone",
//            produceType = "Vegetables",
//            centerLatitude = BigDecimal("1.2345"),
//            centerLongitude = BigDecimal("6.7890"),
//            radiusKm = BigDecimal("10.0")
//        )
//
//        whenever(exporterService.createZone(any()))
//            .thenReturn(ResultFactory.getSuccessResult(testZoneResponse))
//
//        mockMvc.perform(
//            MockMvcRequestBuilders.post("/exporters-service/exporter/zones")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request)))
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(testZoneResponse.id))
//    }
//
//    @Test
//    fun `addFarmerToZone should return relationship details`() {
//        val farmerInZoneResponse = FarmerInZoneResponseDto(
//            farmerId = "farmer-id",
//            farmerName = "Test Farmer",
//            farmSize = 100.0,
//            farmName = "Test Farm",
//            location = null,
//            joinedAt = LocalDateTime.now()
//        )
//
//        whenever(exporterService.addFarmerToZone(any(), any()))
//            .thenReturn(ResultFactory.getSuccessResult(farmerInZoneResponse))
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/exporters-service/exporter/zones/zone-id/farmers/farmer-id"))
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data.farmerId").value(farmerInZoneResponse.farmerId))
//    }
//
//    @Test
//    fun `getExporterZones should return list of zones`() {
//        whenever(exporterService.getExporterZones("test-id"))
//            .thenReturn(ResultFactory.getSuccessResult(listOf(testZoneResponse)))
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/exporters-service/exporter/test-id/zones"))
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").value(testZoneResponse.id))
//    }
//
//    @Test
//    fun `getZoneFarmers should return list of farmers in zone`() {
//        val expectedFarmers = listOf(
//            FarmerInZoneResponseDto(
//                farmerId = "farmer-1",
//                farmerName = "Farmer One",
//                farmSize = 100.0,
//                farmName = "Farm One",
//                location = Location(
//                    latitude = 1.2345,
//                    longitude = 6.7890,
//                    customName = "Location One",
//
//                ),
//                joinedAt = LocalDateTime.now()
//            )
//        )
//
//        whenever(exporterService.getZoneFarmers("zone-1"))
//            .thenReturn(ResultFactory.getSuccessResult(expectedFarmers))
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/exporters-service/exporter/zones/zone-1/farmers"))
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].farmerId").value("farmer-1"))
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].farmerName").value("Farmer One"))
//    }
//
//    @Test
//    fun `getZoneFarmersPaginated should return paged list of farmers in zone`() {
//        val farmer = FarmerInZoneResponseDto(
//            farmerId = "farmer-1",
//            farmerName = "Farmer One",
//            farmSize = 100.0,
//            farmName = "Farm One",
//            location = null,
//            joinedAt = LocalDateTime.now()
//        )
//
//        val pageable = PageRequest.of(0, 10)
//        val page = PageImpl(listOf(farmer))
//
//        whenever(exporterService.getZoneFarmersWithPagination("zone-1", pageable))
//            .thenReturn(ResultFactory.getSuccessResult(page))
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/exporters-service/exporter/zones/zone-1/farmers/paginated")
//            .param("page", "0")
//            .param("size", "10"))
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].farmerId").value("farmer-1"))
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data.totalElements").value(1))
//    }
//
//    @Test
//    fun `schedulePickup should return pickup schedule`() {
//        val request = SchedulePickupRequestDto(
//            exporterId = "test-id",
//            farmerId = "farmer-id",
//            produceListingId = "listing-id",
//            scheduledDate = LocalDateTime.now(),
//            pickupNotes = "Test pickup"
//        )
//
//        whenever(exporterService.schedulePickup(any()))
//            .thenReturn(ResultFactory.getSuccessResult(testPickupScheduleResponse))
//
//        mockMvc.perform(
//            MockMvcRequestBuilders.post("/exporters-service/exporter/pickups")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request)))
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(testPickupScheduleResponse.id))
//    }
//
//    @Test
//    fun `getPickupSchedules should return paged pickup schedules`() {
//        val pageable = PageRequest.of(0, 10)
//        val page = PageImpl(listOf(testPickupScheduleResponse))
//
//        whenever(exporterService.getPickupSchedules("test-id", pageable))
//            .thenReturn(ResultFactory.getSuccessResult(page))
//
//        mockMvc.perform(
//            MockMvcRequestBuilders.get("/exporters-service/exporter/test-id/pickups")
//            .param("page", "0")
//            .param("size", "10"))
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].id").value(testPickupScheduleResponse.id))
//    }
//
//    @Test
//    fun `updateZone should update and return zone details`() {
//        val zoneId = "zone-1"
//        val request = UpdateZoneRequestDto(
//            name = "Updated Zone",
//            produceType = "Vegetables",
//            centerLatitude = BigDecimal("1.2345"),
//            centerLongitude = BigDecimal("6.7890"),
//            radiusKm = BigDecimal("10.0")
//        )
//
//        val updatedZone = ZoneResponseDto(
//            id = zoneId,
//            name = request.name,
//            produceType = request.produceType,
//            centerLatitude = request.centerLatitude,
//            centerLongitude = request.centerLongitude,
//            radiusKm = request.radiusKm,
//            exporterId = "exporter-1",
//            farmerCount = 5,
//            comments = "",
//            creatorId = "",
//            supervisorIds = listOf(),
//        )
//
//        whenever(exporterService.updateZone(zoneId, request))
//            .thenReturn(ResultFactory.getSuccessResult(updatedZone))
//
//        mockMvc.perform(MockMvcRequestBuilders.put("/exporters-service/exporter/zones/$zoneId")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request)))
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(zoneId))
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(request.name))
//            .andExpect(MockMvcResultMatchers.jsonPath("$.data.produceType").value(request.produceType))
//    }
//
//    @Test
//    fun `updateZone should return error when zone not found`() {
//        val zoneId = "non-existent-zone"
//        val request = UpdateZoneRequestDto(
//            name = "Updated Zone",
//            produceType = "Vegetables",
//            centerLatitude = BigDecimal("1.2345"),
//            centerLongitude = BigDecimal("6.7890"),
//            radiusKm = BigDecimal("10.0")
//        )
//
//        whenever(exporterService.updateZone(zoneId, request))
//            .thenReturn(ResultFactory.getFailResult("Zone not found with id: $zoneId"))
//
//        mockMvc.perform(MockMvcRequestBuilders.put("/exporters-service/exporter/zones/$zoneId")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request)))
//            .andExpect(MockMvcResultMatchers.status().isOk)
//            .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false))
//            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Zone not found with id: $zoneId"))
//    }
//
//    @Test
//    fun `updateZone should validate request parameters`() {
//        val zoneId = "zone-1"
//        val invalidRequest = UpdateZoneRequestDto(
//            name = "", // Invalid: empty name
//            produceType = "Vegetables",
//            centerLatitude = BigDecimal("91.0"), // Invalid: latitude > 90
//            centerLongitude = BigDecimal("6.7890"),
//            radiusKm = BigDecimal("-1.0") // Invalid: negative radius
//        )
//
//        mockMvc.perform(MockMvcRequestBuilders.put("/exporters-service/exporter/zones/$zoneId")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(invalidRequest)))
//            .andExpect(MockMvcResultMatchers.status().isBadRequest)
//    }

}