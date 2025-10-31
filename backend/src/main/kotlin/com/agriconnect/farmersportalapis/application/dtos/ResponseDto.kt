package com.agriconnect.farmersportalapis.application.dtos

//import com.agriconnect.farmersportalapis.domain.profile.FarmerProduce
import com.agriconnect.farmersportalapis.domain.common.enums.FarmProduceStatus
import com.agriconnect.farmersportalapis.domain.common.enums.OrderStatus
import com.agriconnect.farmersportalapis.domain.common.enums.RequestStatus
import com.agriconnect.farmersportalapis.domain.common.enums.RequestType
import com.agriconnect.farmersportalapis.domain.listing.ProduceListing
import lombok.Builder
import lombok.Data
import java.time.LocalDate
import java.time.LocalDateTime


// LocationDto & FarmerLocationDto moved to AdminDtos.kt to avoid duplication

@Builder
@Data
data class FarmerProduceDto(
    var id: String,
    var name: String,
    var description: String,
)

@Builder
@Data
data class ListingResponseDto(
    var quantitySold: Double,
    var quantityLeft: Double,
    var earnings: Double,
    val noOfPurchases: Int,
    val produceListing: ProduceListing,
)

@Builder
@Data
data class MarketPrices(
    var market_name: String,
    var product_name: String,
    var retail_price: Double,
    var wholesale_price: Double?,
    var classification: String?,
    var retail_unit: String,
    var wholesale_unit: String?,
    var wholesale_unit_amount: Int?,
    var date: LocalDate,
)

@Builder
@Data
data class ListingOrderDto(
    val orderId: String,
    val listingId: String,
    val pricePerUnit: Double,
    val buyerId: String,
    val dateCreated: LocalDateTime,
    val dateAccepted: LocalDateTime?,
    val dateSupplied: LocalDateTime?,
    val datePaid: LocalDateTime?,
    val quantity: Double,
    val status: OrderStatus,
    val produceName: String,
    val produceDescription: String,
    val produceStatus: FarmProduceStatus
)

@Builder
@Data
data class ExistFarmerOrBuyerDTO(
    val farmer: Boolean,
    val buyer: Boolean,
    val exporter: Boolean
)

@Builder
@Data
data class FeatureRequestResponseDto(
    val id: Long,
    val userId: String,
    val userSection: String,
    val requestType: RequestType,
    val message: String,
    val status: RequestStatus,
    val adminNotes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)