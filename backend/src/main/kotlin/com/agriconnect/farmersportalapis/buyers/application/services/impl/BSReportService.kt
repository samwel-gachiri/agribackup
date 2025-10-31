package com.agriconnect.farmersportalapis.buyers.application.services.impl

import com.agriconnect.farmersportalapis.buyers.application.dtos.ReportDto
import com.agriconnect.farmersportalapis.buyers.application.services.IBSReportService
import com.agriconnect.farmersportalapis.buyers.application.util.BSPdfUtils
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.application.util.ResultFactory
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.RequestOrderReportRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BSReportService(
    @Autowired var requestOrderReportRepository: RequestOrderReportRepository,
    @Autowired val BSPdfUtils: BSPdfUtils
): IBSReportService {
    override fun getReport(
        buyerId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Result<List<ReportDto>> {
        return ResultFactory.getSuccessResult(
            data = requestOrderReportRepository.getReport(buyerId, startDateTime, endDateTime)
        )
    }

    override fun getOrdersHistory(
        buyerId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Result<List<ReportDto>> {
        return ResultFactory.getSuccessResult(
            data = requestOrderReportRepository.getOrderHistory(buyerId, startDateTime, endDateTime)
        )
    }

    override fun getRevenueReportPdf(
        buyerId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): ResponseEntity<Any> {
        val data = requestOrderReportRepository.getOrderHistory(buyerId, startDateTime, endDateTime)
        val fileName = "Revenue_${startDateTime.dayOfMonth}-${startDateTime.month}-${startDateTime.year}_TO_${endDateTime.dayOfMonth}-${endDateTime.month}-${endDateTime.year}.pdf"
        val fileContent: ByteArray = BSPdfUtils.downloadReportPdf(data, fileName,"${startDateTime}_TO_${endDateTime}")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDispositionFormData("attachment", fileName)
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        return ResponseEntity(fileContent, headers, HttpStatus.OK)
    }
}