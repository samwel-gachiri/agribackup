package com.agriconnect.farmersportalapis.buyers.application.services.impl

import com.agriconnect.farmersportalapis.buyers.application.dtos.DashboardDto
import com.agriconnect.farmersportalapis.buyers.application.services.IBSDashboardService
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.application.util.ResultFactory
import com.agriconnect.farmersportalapis.buyers.domain.common.enums.activeRequests
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.DashboardRequestOrderRepository
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.DashboardRequestRepository
import com.agriconnect.farmersportalapis.domain.common.valueobject.Money
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BSDashboardService(
    @Autowired var dashboardRequestRepository: DashboardRequestRepository,
    @Autowired var dashboardRequestOrderRepository: DashboardRequestOrderRepository
): IBSDashboardService {

    override fun getLiveService(buyerId: String): Result<DashboardDto> {
        val activeRequests = dashboardRequestRepository.getProduceRequestStageCount(stages = activeRequests, buyerId = buyerId)
        val buyerInteractions = dashboardRequestOrderRepository.getBuyersInteractions(buyerId)
        val revenue30days = try {
            dashboardRequestOrderRepository.getRevenue(
                buyerId = buyerId,
                dateCreated = LocalDateTime.now().minusDays(30))
        }catch (_: Exception) {
            Money(
                0.00,
                "Ksh. "
            )
        }

        return ResultFactory.getSuccessResult(
            msg = "Live count summary loaded",
            data = DashboardDto(
                activeRequests = activeRequests,
                buyersInteraction = buyerInteractions,
                revenue30Days = revenue30days,
            )
        )
    }
}