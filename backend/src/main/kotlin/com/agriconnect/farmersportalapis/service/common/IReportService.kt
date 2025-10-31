package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.ReportDto
import com.agriconnect.farmersportalapis.application.util.Result
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

interface IReportService {
    fun getReport(farmerId: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime): Result<List<ReportDto>>
    fun getOrdersHistory(farmerId: String, startDateTime: LocalDateTime, endDateTime: LocalDateTime): Result<List<ReportDto>>
    fun getRevenueReportPdf(
        farmerId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): ResponseEntity<Any>
}