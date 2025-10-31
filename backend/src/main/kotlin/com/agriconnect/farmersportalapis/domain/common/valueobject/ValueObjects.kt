package com.agriconnect.farmersportalapis.domain.common.valueobject

import com.agriconnect.farmersportalapis.application.annotations.ValueObject
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.Embeddable

@ValueObject
@Schema(name = "money", description = "It contains the price and currency")
@Embeddable
data class Money(
    @field:Schema(name = "price", description = "This is the amount of money")
    var price: Double,

    @field:Schema(name = "currency", description = "This is the system of money used in a country")
    var currency: String

)