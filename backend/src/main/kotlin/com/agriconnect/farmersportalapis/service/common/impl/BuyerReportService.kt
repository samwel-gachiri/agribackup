package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.util.PdfUtils
import com.agriconnect.farmersportalapis.buyers.application.dtos.BuyerReportDTO
import com.agriconnect.farmersportalapis.infrastructure.repositories.ListingOrderRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BuyerReportService(private val listingOrderRepository: ListingOrderRepository, val pdfUtils: PdfUtils) {

    fun getBuyerReport(buyerId: String, startDate: LocalDateTime, endDate: LocalDateTime): List<BuyerReportDTO> {
        return listingOrderRepository.findBuyerReport(buyerId, startDate, endDate)
    }
}
