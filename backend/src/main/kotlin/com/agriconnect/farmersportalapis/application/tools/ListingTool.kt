package com.agriconnect.farmersportalapis.application.tools

import com.agriconnect.farmersportalapis.service.common.impl.ListingService

class ListingTool(
    private val listingService: ListingService,
) {
//    // post a listing to a user in the database as an ai agent tool
//    @Tool(
//        name="postListingForFarmer",
//        description="This adds a new produce listing to a farmer's profile"
//    )
//    fun postListing(
//        name: String,
//        description: String,
//        quantity: Double,
//        price: Double,
//        unitOfMeasurements: String,
//        userId: String
//    ) {
//        val farmProduceId = ""
//        listingService.listAProduce(listAProduceRequestDto(
//            farmerProduceId = farmProduceId,
//            quantity =  quantity,
//            price = price,
//            unit = unitOfMeasurements))
//    }
}