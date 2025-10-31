package com.agriconnect.farmersportalapis.buyers.controller

import com.agriconnect.farmersportalapis.service.common.impl.BuyerReportService
import com.agriconnect.farmersportalapis.buyers.application.dtos.BuyerReportDTO
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime


@RestController
@Tag(name = "Buyer Reports APIs", description = "APIs for the reports given to the buyer")
@RequestMapping("/api/reports")
class ReportController(private val buyerReportService: BuyerReportService) {
    @GetMapping("/buyer")
    fun getBuyerReport(
        @RequestParam buyerId: String,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDate: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDate: LocalDateTime
    ): ResponseEntity<List<BuyerReportDTO>> {
        val report = buyerReportService.getBuyerReport(buyerId, startDate, endDate)
        return ResponseEntity.ok(report)
    }
}

//@RestController
//@RequestMapping("/buyers-service/api/reports")
//@Tag(name = "Buyer Reports APIs", description = "APIs for the reports given to the buyer")
//class BuyerReportController(
//    @Autowired var BSReportService: BSReportService
//) {
//
//    @GetMapping("/orders/grouped")
//    @Operation(
//        summary = "Get the orders grouped by the farm produce"
//    )
//    fun getReport(
//        @Parameter(description = "Id of buyer")
//        @RequestParam("buyerId", defaultValue = "") buyerId: String,
//        @RequestParam("startDateTime")
//        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDateTime: LocalDateTime,
//
//        @RequestParam("endDateTime")
//        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDateTime: LocalDateTime
//    ) = BSReportService.getReport(
//        buyerId,
//        startDateTime,
//        endDateTime)
//    @GetMapping("/orders/history")
//    @Operation(summary = "Gets the whole list of orders")
//    fun getOrdersHistory(
//        @Parameter(description = "Id of buyer")
//        @RequestParam("buyerId", defaultValue = "") buyerId: String,
//        @RequestParam("startDateTime")
//        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDateTime: LocalDateTime,
//
//        @RequestParam("endDateTime")
//        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDateTime: LocalDateTime
//    ) = BSReportService.getOrdersHistory(
//        buyerId,
//        startDateTime,
//        endDateTime
//    )
//    @GetMapping("/orders/pdf")
//    @Operation(summary = "Retrieves a list of orders on a pdf")
//    fun getReportPdf(
//        @Parameter(description = "Id of buyer")
//        @RequestParam("buyerId", defaultValue = "") buyerId: String,
//        @RequestParam("startDateTime")
//        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startDateTime: LocalDateTime,
//        @RequestParam("endDateTime")
//        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) endDateTime: LocalDateTime
//    ): ResponseEntity<Any> {
//       return BSReportService.getRevenueReportPdf(
//            buyerId,
//            startDateTime,
//            endDateTime
//        )
//    }
//
//    @GetMapping("/pdf")
//    fun generatePdfReport(): ResponseEntity<ByteArrayResource> {
//        val pdfBytes: ByteArray = BSReportService.generatePdfReport()
//        val headers = HttpHeaders()
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
//        return ResponseEntity.ok()
//            .headers(headers)
//            .contentType(MediaType.APPLICATION_PDF)
//            .body(ByteArrayResource(pdfBytes))
//    }
//
//}