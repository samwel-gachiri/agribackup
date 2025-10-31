package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.DashboardDto
import com.agriconnect.farmersportalapis.application.dtos.SalesReportDTO
import com.agriconnect.farmersportalapis.service.common.IDashboardService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.common.enums.OrderStatus
import com.agriconnect.farmersportalapis.domain.common.enums.activeListings
import com.agriconnect.farmersportalapis.domain.common.valueobject.Money
import com.agriconnect.farmersportalapis.service.common.AiService
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerProduceRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.DashboardListingOrderRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.DashboardListingRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class DashboardService(
    @Autowired var dashboardListingRepository: DashboardListingRepository,
    @Autowired var dashboardListingOrderRepository: DashboardListingOrderRepository,
    @Autowired var farmerRepository: FarmerRepository,
    @Autowired var farmerProduceRepository: FarmerProduceRepository,
    @Autowired var aiService: AiService
): IDashboardService {
    private val logger = LoggerFactory.getLogger(DashboardService::class.java)
    override fun getLiveService(farmerId: String): Result<DashboardDto> {
        val result = farmerRepository.findByIdOrNull(farmerId)
            ?: return ResultFactory.getFailResult(msg = "farmer is not found")
        logger.info("Farmer is found {}", result.id)
        val activeListings = dashboardListingRepository.getProduceListingStageCount(stages = activeListings, farmerId = result.id)
        logger.info("Active listings {}", activeListings)
        val buyerInteractions = dashboardListingOrderRepository.getBuyersInteractions(farmerId)
        logger.info("Buyers interactions {}", buyerInteractions)
        val revenue30days = try {
            dashboardListingOrderRepository.getRevenue(
                farmerId = farmerId,
                dateCreated = LocalDateTime.now().minusYears(1))
        }catch (_: Exception) {
            Money(
                0.00,
                "Ksh. "
            )
        }
        logger.info("Revenue {}", revenue30days.price)

        // New: derive richer analytics for the farmer
        val produces = farmerProduceRepository.findByFarmerIdSimple(result.id!!)
        val activeCropNames = produces
            .filter { it.status.name != "HARVESTED" }
            .mapNotNull { it.farmProduce?.name }
            .distinct()
        val activeCrops = activeCropNames.size

        val today = LocalDate.now()
        val horizon = today.plusDays(60)
        val upcomingHarvestsCount = produces.count {
            val d = it.predictedHarvestDate
            d != null && (d.isEqual(today) || (d.isAfter(today) && d.isBefore(horizon.plusDays(1))))
        }

        // Build an AI prompt from current crops (livestock not modeled yet; hook left for future)
        val cropsSummary = if (produces.isNotEmpty()) {
            produces.joinToString("\n") { fp ->
                val n = fp.farmProduce?.name ?: "Unknown"
                val st = fp.status.name
                val ph = fp.predictedHarvestDate?.toString() ?: "unknown"
                "- name=$n; status=$st; predictedHarvestDate=$ph"
            }
        } else "(none)"

        val prompt = """
            You are an agricultural coach. Based on the farmer's current crops and predicted harvests, write 1–2 short, highly actionable insights for today (max ~280 characters total). If no crops are listed, give a seasonal preparation tip suitable for smallholder farmers in East Africa.

            CROPS:
            $cropsSummary

            Return plain text only (no bullets, no markdown).
        """.trimIndent()

        val aiInsight = try {
            aiService.getChatCompletion(promptText = prompt, userId = farmerId)
        } catch (e: Exception) {
            logger.warn("AI insight generation failed: {}", e.message)
            null
        }
        val dailyInsight = aiInsight ?: run {
            val fallback = if (activeCrops > 0) "Walk your fields early, scout for pests, and mulch to retain moisture. Thin overcrowded areas and plan inputs for crops due in the next 60 days." else "Test soil moisture, clean tools, and plan staggered plantings for steady supply."
            fallback
        }

        // Simple market tips placeholder (keep static but concise)
        val marketTips = listOf(
            "List harvest-ready produce 3–5 days early to capture demand.",
            "Bundle small starter packs to attract first-time buyers.",
            "Add clear photos and units to improve conversions."
        )
        return ResultFactory.getSuccessResult(
            msg = "Live count summary loaded",
            data = DashboardDto(
                activeListings = activeListings,
                buyersInteraction = buyerInteractions,
                revenue30Days = revenue30days,
                activeCrops = activeCrops,
                upcomingHarvestsCount = upcomingHarvestsCount,
                marketTips = marketTips,
                dailyInsight = dailyInsight
            )
        )
    }

    fun getSalesReport(farmerId: String): List<SalesReportDTO> {
        val result = dashboardListingOrderRepository.getSalesReport(farmerId, OrderStatus.SUPPLIED_AND_PAID)

        return result.map { row ->
            val data = row as Array<*>  // Ensure it's cast correctly
            SalesReportDTO(
                saleMonth = data[0] as String,
                produceName = data[1] as String,
                totalSold = (data[2] as Number).toDouble(),  // Safe casting to Double
                totalRevenue = (data[3] as Number).toDouble()
            )
        }
    }
}