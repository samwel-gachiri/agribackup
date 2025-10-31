package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.service.common.impl.ReportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime


@RestController
@RequestMapping("/farmers-service/api/reports")
@Tag(name = "Farmer Reports APIs", description = "APIs for the reports given to the farmer")
class FarmerReportController(
    @Autowired var reportService: ReportService
) {

    @GetMapping("/orders/grouped")
    @Operation(
        summary = "Get the orders grouped by the farm produce"
    )
    fun getReport(
        @Parameter(description = "Id of farmer")
        @RequestParam("farmerId", defaultValue = "") farmerId: String,
        @RequestParam("startDateTime")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDateTime: LocalDateTime,

        @RequestParam("endDateTime")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDateTime: LocalDateTime
    ) = reportService.getReport(
        farmerId,
        startDateTime,
        endDateTime)
    @GetMapping("/orders/history")
    @Operation(summary = "Gets the whole list of orders")
    fun getOrdersHistory(
        @Parameter(description = "Id of farmer")
        @RequestParam("farmerId", defaultValue = "") farmerId: String,
        @RequestParam("startDateTime")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDateTime: LocalDateTime,

        @RequestParam("endDateTime")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDateTime: LocalDateTime
    ) = reportService.getOrdersHistory(
        farmerId,
        startDateTime,
        endDateTime
    )
    @GetMapping("/orders/pdf")
    @Operation(summary = "Retrieves a list of orders on a pdf")
    fun getReportPdf(
        @Parameter(description = "Id of farmer")
        @RequestParam("farmerId", defaultValue = "") farmerId: String,
        @RequestParam("startDateTime")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDateTime: LocalDateTime,
        @RequestParam("endDateTime")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDateTime: LocalDateTime
    ): ResponseEntity<Any> {
       return reportService.getRevenueReportPdf(
            farmerId,
            startDateTime,
            endDateTime
        )
    }

    @GetMapping("/listing/pdf")
    @Operation(summary = "Retrieves listing details on a pdf")
    fun getReportPdf(
        @Parameter(description = "Id of listing")
        @RequestParam("listingId", defaultValue = "") listingId: String,
    ): ResponseEntity<Any> {
        return reportService.getListingReportPdf(
            listingId,
        )
    }
}