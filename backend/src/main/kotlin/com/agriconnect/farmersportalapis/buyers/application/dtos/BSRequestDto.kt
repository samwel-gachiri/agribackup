package com.agriconnect.farmersportalapis.buyers.application.dtos

import com.agriconnect.farmersportalapis.buyers.domain.common.valueobject.Money
import io.swagger.v3.oas.annotations.media.Schema
import lombok.Builder
import lombok.Data
import java.time.LocalDateTime

//
//@Builder
//@Data
//data class createBuyerProfileDto(
//    var username: String,
//    var name: String,
//    var
//)

@Builder
@Data
data class BuyersRequestDto(
    @field:Schema(name = "limit", description = "Number of buyers to fetch", example = "50")
    var limit: Int
)

@Builder
@Data
data class UpdateBuyerDto(
    var buyerId: String,
    var name: String,
    var phoneNumber: String,
    var email: String,
    var location: LocationRequestDto
)

@Builder
@Data
data class AddProducesToBuyerDto(
    var buyerId: String,
    var buyerProducesId: List<String>,
)

@Builder
@Data
data class AddOrderToRequestDto(
    var requestId: String,
    var farmerId: String,
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
data class requestAProduceRequestDto(
    var buyerProduceId: String,
    var quantity: Double,
    var price: Money,
    var unit: String,
)

@Builder
@Data
data class updateRequestRequestDto(
    var buyerProduceId: String,
    var quantity: Double,
    var price: Money,
    var unit: Double,
)

@Builder
@Data
data class LocationRequestDto(
    var latitude: Double,
    var longitude: Double,
    var customName: String
)

@Builder
@Data
data class UpdateLocationRequestDto(
    var buyerId: String,
    var locationDto: LocationRequestDto,
)

@Builder
@Data
data class DashboardDto(
    var activeRequests: Int,
    var buyersInteraction: Int,
    var revenue30Days: Any,
//    var connectionsMade: Int,
//    var requestsAccepted: Int,
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