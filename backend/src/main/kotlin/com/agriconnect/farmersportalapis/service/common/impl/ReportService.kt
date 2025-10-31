package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.ReportDto
import com.agriconnect.farmersportalapis.application.util.PdfUtils
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.infrastructure.repositories.ListingOrderReportRepository
import com.agriconnect.farmersportalapis.service.common.IReportService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReportService(
    @Autowired var listingOrderReportRepository: ListingOrderReportRepository,
    @Autowired val pdfUtils: PdfUtils,
    @Autowired val listingService: ListingService,
): IReportService {
    override fun getReport(
        farmerId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Result<List<ReportDto>> {
        return ResultFactory.getSuccessResult(
            data = listingOrderReportRepository.getReport(farmerId, startDateTime, endDateTime)
        )
    }

    override fun getOrdersHistory(
        farmerId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): Result<List<ReportDto>> {
        return ResultFactory.getSuccessResult(
            data = listingOrderReportRepository.getOrderHistory(farmerId, startDateTime, endDateTime)
        )
    }

    override fun getRevenueReportPdf(
        farmerId: String,
        startDateTime: LocalDateTime,
        endDateTime: LocalDateTime
    ): ResponseEntity<Any> {
        val data = listingOrderReportRepository.getOrderHistory(farmerId, startDateTime, endDateTime)
        val fileName = "Revenue_${startDateTime.dayOfMonth}-${startDateTime.month}-${startDateTime.year}_TO_${endDateTime.dayOfMonth}-${endDateTime.month}-${endDateTime.year}.pdf"
        val fileContent: ByteArray = pdfUtils.downloadReportPdf(data, fileName,"Revenue Made From ${startDateTime} To ${endDateTime}")
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDispositionFormData("attachment", fileName)
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        return ResponseEntity(fileContent, headers, HttpStatus.OK)
    }

    fun getListingReportPdf(
        listingId: String,
    ): ResponseEntity<Any> {
        val data = listingService.getListing(listingId).data ?: return ResponseEntity(HttpStatus.OK)
        val orders: List<Map<String, Any>> = data.produceListing.listingOrders.map {
            mapOf(
                "dateCreated" to it.dateCreated,
                "quantity" to it.quantity,
                "status" to it.status,
                "revenue" to (it.quantity * data.produceListing.price.price)
            )
        }
        val fileName = "Revenue_${listingId}.pdf"
        val fileContent: ByteArray = pdfUtils.downloadListingReportPdf(
            orders,
            fileName,
            "Listing Report",
            quantitySold = data.quantitySold,
            earnings = data.earnings,
            currency = data.produceListing.price.currency,
            produceName = data.produceListing.farmerProduce.farmProduce.name,
            produceImage = data.produceListing.farmerProduce.imageUrls?.first(),
        )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_PDF
        headers.setContentDispositionFormData("attachment", fileName)
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        return ResponseEntity(fileContent, headers, HttpStatus.OK)
    }

}