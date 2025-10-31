package com.agriconnect.farmersportalapis.buyers.application.dtos

import com.agriconnect.farmersportalapis.buyers.domain.common.enums.FarmProduceStatus
import com.agriconnect.farmersportalapis.buyers.domain.common.enums.OrderStatus
import com.agriconnect.farmersportalapis.buyers.domain.common.model.BuyerLocation
import com.agriconnect.farmersportalapis.buyers.domain.request.ProduceRequest
import lombok.Builder
import lombok.Data
import java.time.LocalDateTime

//
//@Builder
//@Data
//data class GetBuyersResponseDto(
//    var id: String,
//    var name: String,
//    var produces: List<BuyerProduce>
//)

@Builder
@Data
data class BuyersLocationDto(
    var buyerName:String,
    var buyerLocation: BuyerLocation,
)

@Builder
@Data
data class RequestResponseDto(
    var quantitySold: Double,
    var quantityLeft: Double,
    var earnings: Double,
    val noOfPurchases: Int,
    val produceRequest: ProduceRequest,
)
@Builder
@Data
data class RequestOrderDto(
    val orderId: String,
    val requestId: String,
    val farmerId: String,
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

data class BuyerReportDTO(
    val buyerId: String,
    val farmerName: String,
    val produceName: String,
    val totalSpent: Double,
    val farmerInteractions: Long,
    val pendingOrders: Long,
    val acceptedOrders: Long,
    val suppliedOrders: Long
)