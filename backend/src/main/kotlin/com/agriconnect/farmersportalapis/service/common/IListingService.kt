package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.AddOrderToListingDto
import com.agriconnect.farmersportalapis.application.dtos.ListingResponseDto
import com.agriconnect.farmersportalapis.application.dtos.updateListingRequestDto
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.domain.listing.ListingOrder
import com.agriconnect.farmersportalapis.domain.listing.ProduceListing
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface IListingService {
    fun getListings(): Result<List<ProduceListing>>

    fun updateListing(updateListingRequestDto: updateListingRequestDto): Result<ProduceListing>


    fun unlist(produceListingId: String): Result<String>
    fun getListing(listingId: String): Result<ListingResponseDto>
    fun getFarmerListings(farmerId: String, pageable: Pageable): Result<Page<ProduceListing>>
    fun addOrderToListing(addOrderToListingDto: AddOrderToListingDto): Result<ProduceListing>
    fun acceptOrder(orderId: String): Result<ListingOrder>
    fun declineOrder(orderId: String, farmerComment: String): Result<ListingOrder>
    fun confirmSupply(orderId: String): Result<ListingOrder>
    fun confirmPayment(orderId: String): Result<ListingOrder>
}