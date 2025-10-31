package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.AdminRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PlatformAdminService(private val adminRepository: AdminRepository) {

    fun getProduceSales(): List<ProduceSalesDTO> {
        return adminRepository.getProduceSales().map { row ->
            ProduceSalesDTO(
                name = row[0] as String,
                totalSales = (row[1] as Number).toDouble()
            )
        }
    }

    val logger = LoggerFactory.getLogger(PlatformAdminService::class.java)

    fun getDailyListingsAndOrders(): List<DailyListingsDTO> {
        return adminRepository.getDailyListingsAndOrders().map { row ->
            DailyListingsDTO(
                listingDate = (row[0] as java.sql.Date).toLocalDate(),
                totalListings = (row[1] as Number).toInt(),
                totalOrders = (row[2] as Number).toInt()
            )
        }
    }

    fun getDailySignIns(): List<DailySignInDTO> {
        return adminRepository.getDailySignIns().map {
            DailySignInDTO(
                date = (it[0] as java.sql.Date).toLocalDate(),
                farmersSignedIn = (it[1] as Number).toInt(),
                buyersSignedIn = (it[2] as Number).toInt()
            )
        }
    }

    fun getTotalFarmersAndBuyers(): TotalUsersDTO {
        return TotalUsersDTO(
            totalFarmers = adminRepository.getTotalFarmers(),
            totalBuyers = adminRepository.getTotalBuyers()
        )
    }

    fun getOrderReport(): OrderReportDTO? {
        val resultList = adminRepository.getOrderReport()

        if (resultList.isEmpty()) return null

        val result = resultList[0] as Array<*>

        return OrderReportDTO(
            totalListings = (result[0] as Number).toInt(),
            totalOrders = (result[1] as Number).toInt(),
            totalPendingOrders = (result[2] as Number).toInt(),
            totalBookedOrders = (result[3] as Number).toInt(),
            totalDeclinedOrders = (result[4] as Number).toInt(),
            totalSuppliedOrders = (result[5] as Number).toInt(),
            totalSuppliedAndPaidOrders = (result[6] as Number).toInt(),
            totalTransactionAmount = (result[7] as Number).toDouble()
        )
    }
}
