package com.agriconnect.farmersportalapis.application.dtos

import com.agriconnect.farmersportalapis.domain.common.enums.FarmProduceStatus
import com.agriconnect.farmersportalapis.domain.common.enums.RequestStatus
import com.agriconnect.farmersportalapis.domain.common.enums.RequestType
import com.agriconnect.farmersportalapis.domain.common.valueobject.Money
import io.swagger.v3.oas.annotations.media.Schema
import lombok.Builder
import lombok.Data
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.time.LocalDateTime

//
//@Builder
//@Data
//data class createFarmerProfileDto(
//    var username: String,
//    var name: String,
//    var
//)

@Builder
@Data
data class FarmersRequestDto(
    @field:Schema(name = "limit", description = "Number of farmers to fetch", example = "50")
    var limit: Int
)


@Builder
@Data
data class AddFarmerProduceDto(
    val farmerId: String,
    val produceName: String,
    val description: String,
    val farmingType: String,
    val yieldAmount: Double? = null,
    val yieldUnit: String? = null,
    val seasonYear: Int? = null,
    val seasonName: String? = null,
    val images: List<MultipartFile>? = null
)

@Builder
@Data
data class UpdateFarmerDto(
    var farmerId: String,
    var name: String,
    var phoneNumber: String,
    var email: String,
    var location: LocationDto,
)
@Builder
@Data
data class UpdateFarmerProduceDto(
    val description: String,
    val farmingType: String,
    val newImages: List<MultipartFile>?,
    val removeImageUrls: List<String>?
)


@Builder
@Data
data class FarmProduceDto(
    var id: String,
    var name: String,
    var description: String?,
    var farmingType: String?,
    var source: String,
)

@Builder
@Data
data class AddOrderToListingDto(
    var listingId: String,
    var buyerId: String,
    var quantity: Double,
)

@Builder
@Data
data class createFarmProduceDto(
    var name: String,
    var description: String,
    var farmingType: String,
)

@Builder
@Data
data class listAProduceRequestDto(
    var farmerProduceId: String,
    var quantity: Double,
    var price: Money,
    var unit: String,
)

@Builder
@Data
data class updateListingRequestDto(
    var farmerProduceId: String,
    var quantity: Double,
    var price: Money,
    var unit: Double,
)

@Builder
@Data
data class UpdateLocationRequestDto(
    var farmerId: String?,
    var locationDto: LocationDto,
)

@Builder
@Data
data class DashboardDto(
    var activeListings: Int,
    var buyersInteraction: Int,
    var revenue30Days: Money,
    // New enriched analytics
    var activeCrops: Int = 0,
    var upcomingHarvestsCount: Int = 0,
    var marketTips: List<String> = emptyList(),
    var dailyInsight: String? = null,
//    var connectionsMade: Int,
//    var requestsAccepted: Int,
)

@Builder
@Data
data class SalesReportDTO(
    val saleMonth: String,
    val produceName: String,
    val totalSold: Double,
    val totalRevenue: Double
)

@Builder
@Data
data class ReportDto(
    var date: LocalDateTime,
    var product: String,
    var quantitySold: Double,
    var currency: String,
    var revenue: Double,
)

@Builder
@Data
data class ProduceSalesDTO(
    val name: String,
    val totalSales: Double
)

@Builder
@Data
data class DailyListingsDTO(
    val listingDate: LocalDate,
    val totalListings: Int,
    val totalOrders: Int
)

@Builder
@Data
data class OrderReportDTO(
    val totalListings: Int,
    val totalOrders: Int,
    val totalPendingOrders: Int,
    val totalBookedOrders: Int,
    val totalDeclinedOrders: Int,
    val totalSuppliedOrders: Int,
    val totalSuppliedAndPaidOrders: Int,
    val totalTransactionAmount: Double
)

@Builder
@Data
data class DailySignInDTO(
    val date: LocalDate,
    val farmersSignedIn: Int,
    val buyersSignedIn: Int
)

@Builder
@Data
data class TotalUsersDTO(
    val totalFarmers: Int,
    val totalBuyers: Int
)

@Builder
@Data
data class ChatRequest(
    val userId: String,
    val question: String,
    val userSection: String? = null,
    val responseType: String? = ""
)
@Builder
@Data
data class FeatureRequestCreateDto(
    val requestType: RequestType,
    val message: String,
    val userId: String,
    val userSection: String,
    val aiGenerated: Boolean = false,
    val originalPrompt: String? = null
)

@Builder
@Data
data class FeatureRequestUpdateDto(
    val status: RequestStatus,
    val adminNotes: String?
)
@Builder
@Data
data class FeatureRequestFilterDto(
    val status: RequestStatus? = null,
    val requestType: RequestType? = null,
    val userSection: String? = null,
    val userId: Long? = null
)

// produce search dto
data class ProduceSearchRequest(
    val searchTerm: String,
    val searchType: SearchType? = null,
    val maxDistance: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

enum class SearchType {
    NAME, DESCRIPTION, LOCATION, ALL
}

data class ProduceSearchResponse(
    val id: String,
    val name: String,
    val description: String?,
    val farmingType: String?,
    val status: FarmProduceStatus,
    val farmer: FarmerInfo,
    val location: LocationInfo,
    val distance: Double? = null
)

data class FarmerInfo(
    val id: String?,
    val name: String?,
    val contactInfo: String?
)

data class LocationInfo(
    val latitude: Double?,
    val longitude: Double?,
    val customName: String?
)
