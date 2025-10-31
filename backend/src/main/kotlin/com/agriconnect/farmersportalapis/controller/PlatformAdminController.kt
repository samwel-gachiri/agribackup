package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.PlatformAdminService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class PlatformAdminController(private val platformAdminService: PlatformAdminService) {

    @Operation(summary = "get produce names with their total sales")
    @GetMapping("/produce-sales")
    fun getProduceSales(): ResponseEntity<List<ProduceSalesDTO>> {
        return ResponseEntity.ok(platformAdminService.getProduceSales())
    }

    @Operation(summary = "Gets the daily listings and orders made")
    @GetMapping("/daily-listings")
    fun getDailyListingsAndOrders(): ResponseEntity<List<DailyListingsDTO>> {
        return ResponseEntity.ok(platformAdminService.getDailyListingsAndOrders())
    }

    @Operation(summary = "Gets the farmers and buyers daily sign in")
    @GetMapping("/daily-signins")
    fun getDailySignIns(): ResponseEntity<List<DailySignInDTO>> {
        return ResponseEntity.ok(platformAdminService.getDailySignIns())
    }

    @Operation(summary = "Gets the total number of farmers and buyers")
    @GetMapping("/total-users")
    fun getTotalFarmersAndBuyers(): ResponseEntity<TotalUsersDTO> {
        return ResponseEntity.ok(platformAdminService.getTotalFarmersAndBuyers())
    }

    @Operation(summary = "Gets the reports on orders")
    @GetMapping("/order-report")
    fun getOrderReport(): ResponseEntity<OrderReportDTO> {
        val report = platformAdminService.getOrderReport()
        return if (report != null) {
            ResponseEntity.ok(report)
        } else {
            ResponseEntity.noContent().build()
        }
    }
}
