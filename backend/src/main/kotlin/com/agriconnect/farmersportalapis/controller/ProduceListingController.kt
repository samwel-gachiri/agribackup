package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.AddOrderToListingDto
import com.agriconnect.farmersportalapis.application.dtos.ListingResponseDto
import com.agriconnect.farmersportalapis.application.dtos.updateListingRequestDto
import com.agriconnect.farmersportalapis.service.common.impl.ListingService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.domain.common.valueobject.Money
import com.agriconnect.farmersportalapis.domain.listing.ListingOrder
import com.agriconnect.farmersportalapis.domain.listing.ProduceListing
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping(path = ["/farmers-service/listing"])
@Tag(name = "Produce Listing management", description = "Manages listing of produces")
class ProduceListingController(
    var listingService: ListingService
) {
    @Operation(
        summary = "Gets all of the listings"
    )
    @GetMapping("/list")
    fun getListings() = listingService.getListings()

    @Operation(
        summary = "Gets out a specific listing",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping
    fun getListing(
        @Parameter(description = "Id of listing")
        @RequestParam("listingId", defaultValue = "") listingId: String,
    ): Result<ListingResponseDto> {
        return listingService.getListing(listingId)
    }

    @Operation(
        summary = "Gets out listings for a specific farmer"
    )
    @GetMapping("/farmer")
    fun getListingByFarmer(
        @Parameter(description = "Id of farmer")
        @RequestParam("farmerId", defaultValue = "") farmerId: String,
        pageable: Pageable
    ): Result<Page<ProduceListing>> {
        return listingService.getFarmerListings(farmerId, pageable)
    }

//    @PostMapping(value = ["/post-listing"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
//    fun addFarmerProduce(
//        @RequestParam("farmerId") farmerId: String,
//        @RequestParam("produceName") produceName: String,
//        @RequestParam("quantity") quantity: String,
//        @RequestParam("price") price: String,
//        @RequestParam(value = "images", required = false) images: List<MultipartFile>? = null
//    ) = listingService.listAProduceWithImage(farmerId, produceName, quantity, images)

//    @Operation(
//        summary = "Lists a produce from a farmer"
//    )
//    @PostMapping
//    fun listAProduce(
//        @RequestBody listAProduceRequestDto: listAProduceRequestDto
//    ) = listingService.listAProduce(listAProduceRequestDto)

    @Operation(
        summary = "Lists a Farmer produce for sale"
    )
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addProduceToListing(
        @RequestParam("farmerProduceId") farmerProduceId: String,
        @RequestParam("quantity") quantity: Double,
        @RequestParam("price") price: Double,
        @RequestParam("currency") currency: String,
        @RequestParam("unit") unit: String,
        @RequestParam(value = "images", required = false) images: List<MultipartFile>? = null,
        @RequestParam(value = "produceYieldId", required = false) produceYieldId: String? = null
    ): Result<ProduceListing> {
        logger.info("Received request to add listing for farmerProduceId: $farmerProduceId, quantity: $quantity, price: $price")
        val money = Money(price, currency)
        val result = listingService.addProduceToListing(farmerProduceId, quantity, money, unit, images)
        logger.info("Listing service result: success=${result.success}, message=${result.msg}")
        return result
    }

    @Operation(
        summary = "Updates a listing"
    )

    @PutMapping
    fun updateListing(
        @RequestBody updateListingRequestDto: updateListingRequestDto
    ) = listingService.updateListing(updateListingRequestDto)

//    @PostMapping("/{listingId}/images")
//    @Operation(
//        summary = "Upload images for a listing",
//        description = "Upload one or more images for a produce listing. Images will be stored in S3."
//    )
//    fun uploadListingImages(
//        @PathVariable listingId: String,
//        @RequestParam("images") images: List<MultipartFile>
//    ): Result<ProduceListing> {
//        return listingService.uploadListingImages(listingId, images)
//    }
//
//    @DeleteMapping("/{listingId}/images")
//    @Operation(
//        summary = "Delete a listing image",
//        description = "Delete a specific image from a produce listing"
//    )
//    fun deleteListingImage(
//        @PathVariable listingId: String,
//        @RequestParam imageUrl: String
//    ): Result<ProduceListing> {
//        return listingService.deleteListingImage(listingId, imageUrl)
//    }

    @Operation(
        summary = "Adds order to a listing"
    )
    @PostMapping("/order")
    fun addOrderToListing(
        @RequestBody addOrderToListingDto: AddOrderToListingDto
    ): Result<ProduceListing> {
        val res = listingService.addOrderToListing(addOrderToListingDto)
        emitEvent(addOrderToListingDto.listingId, "order-added-to-listing")
        return res
    }

    @Operation(
        summary = "Accept order from buyer"
    )
    @PutMapping("/order/accept")
    fun acceptOrder(
        @Parameter(description = "order id")
        @RequestParam("orderId", defaultValue = "") orderId: String,
        @RequestParam listingId: String,
    ): Result<ListingOrder> {
        val res = listingService.acceptOrder(orderId)
        emitEvent(listingId = listingId, name = "order-accepted")
        return res
    }
    @Operation(
        summary = "Decline order from buyer"
    )
    @PutMapping("/order/decline")
    fun declineOrder(
        @Parameter(description = "order id")
        @RequestParam("orderId", defaultValue = "") orderId: String,
        @Parameter(description = "farmer's comment")
        @RequestParam("farmerId", defaultValue = "") farmerComment: String,
        @RequestParam listingId: String,
    ): Result<ListingOrder> {
        val res = listingService.declineOrder(orderId, farmerComment)
        emitEvent(listingId = listingId, name = "order-accepted")
        return res
    }
    @Operation(
        summary = "Confirm produce supplied [buyer]"
    )
    @PutMapping("/order/confirm-supply")
    fun confirmSupply(
        @Parameter(description = "order id")
        @RequestParam("orderId", defaultValue = "") orderId: String,
        @RequestParam listingId: String,
    ): Result<ListingOrder> {
        val res = listingService.confirmSupply(orderId)
        emitEvent(listingId = listingId, name = "supply-confirmed")
        return res
    }
    @Operation(
        summary = "Confirm payment done to a farmer"
    )
    @PutMapping("/order/confirm-payment")
    fun confirmPayment(
        @Parameter(description = "order id")
        @RequestParam("orderId", defaultValue = "") orderId: String,
        @RequestParam listingId: String,
    ): Result<ListingOrder> {
        val res = listingService.confirmPayment(orderId)
        emitEvent(listingId = listingId, name = "payment-confirmed", orderId);
        return res
    }
    fun emitEvent(listingId: String, name: String, data: String = "") {
        val emitter = emitters[listingId]
        if (emitter != null) {
            val event = SseEmitter.event()
                .name(name)
                .data(data) 
            try {
                emitter.send(event)
            } catch (e: Exception) {
                emitter.completeWithError(e)
            }
        }
    }

    private val emitters = ConcurrentHashMap<String, SseEmitter>()
    private val logger = LoggerFactory.getLogger(ProduceListingController::class.java)

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(
        @RequestParam listingId: String
    ): SseEmitter {
        logger.info("Setting emitter for listingId: {}", listingId)
        val emitter = SseEmitter(Long.MAX_VALUE)

        // registering the event
        emitters[listingId] = emitter

        emitter.onCompletion {
            logger.info("Emitter for listingId {} completed", listingId)
            emitters.remove(listingId)
        }

        emitter.onTimeout {
            logger.info("Emitter for listingId {} timed out", listingId)
            emitters.remove(listingId)
        }

        logger.info("Emitter for listingId {} set!", listingId)
        return emitter
    }

}