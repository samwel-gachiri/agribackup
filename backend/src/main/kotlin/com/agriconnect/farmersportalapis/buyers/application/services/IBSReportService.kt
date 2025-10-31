package com.agriconnect.farmersportalapis.buyers.application.services

import com.agriconnect.farmersportalapis.buyers.application.dtos.ReportDto
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

interface IBSReportService {
    fun getReport(buyerId: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime): Result<List<ReportDto>>
    fun getOrdersHistory(buyerId: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime): Result<List<ReportDto>>
    fun getRevenueReportPdf(
        buyerId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): ResponseEntity<Any>
}