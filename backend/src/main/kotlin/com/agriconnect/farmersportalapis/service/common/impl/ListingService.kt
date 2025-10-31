package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.AddOrderToListingDto
import com.agriconnect.farmersportalapis.application.dtos.ListingResponseDto
import com.agriconnect.farmersportalapis.application.dtos.updateListingRequestDto
import com.agriconnect.farmersportalapis.service.common.IListingService
import com.agriconnect.farmersportalapis.service.common.S3Service
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.common.enums.FarmerProduceStatus
import com.agriconnect.farmersportalapis.domain.common.enums.OrderStatus
import com.agriconnect.farmersportalapis.domain.common.enums.ProduceListingStatus
import com.agriconnect.farmersportalapis.domain.common.valueobject.Money
import com.agriconnect.farmersportalapis.domain.listing.ListingOrder
import com.agriconnect.farmersportalapis.domain.listing.ProduceListing
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachePut
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

@Service
class ListingService(
    private val cacheManager: CacheManager,
    private var produceListingRepository: ProduceListingRepository,
    private var farmerProduceRepository: FarmerProduceRepository,
    private var listingOrderRepository: ListingOrderRepository,
    private var farmProduceRepository: FarmProduceRepository,
    private var farmerRepository: FarmerRepository,
    private val s3Service: S3Service
): IListingService {

    private val logger = LoggerFactory.getLogger(ListingService::class.java)

    val orderNotFound = "Order not found"
    val produceListingNotFound = "Listing not found."

    override fun getListings(): Result<List<ProduceListing>> {
        return try {
            val cache = cacheManager.getCache("listings")
            val cachedListings = cache?.get("listings")?.get()?.let { it as? List<*> }
                ?.filterIsInstance<ProduceListing>()
            if (!cachedListings.isNullOrEmpty()) {
                return ResultFactory.getSuccessResult(cachedListings)
            }
            // fetch from db if not cached
            val produceListings = produceListingRepository.findAll()

            // store in cache
            cache?.put("listings", produceListings)

            ResultFactory.getSuccessResult(produceListings);
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }
//    fun uploadListingImages(listingId: String, images: List<MultipartFile>): Result<ProduceListing> {
//        val listing = produceListingRepository.findById(listingId)
//            .orElse(null) ?: return ResultFactory.getFailResult("Listing not found")
//
//        val imageUrls = images.map { s3Service.uploadFile(it) }
//        listing.imageUrls = imageUrls
//
//        return ResultFactory.getSuccessResult(
//            data = produceListingRepository.save(listing)
//        )
//    }
//
//    fun deleteListingImage(listingId: String, imageUrl: String): Result<ProduceListing> {
//        val listing = produceListingRepository.findById(listingId)
//            .orElse(null) ?: return ResultFactory.getFailResult("Listing not found")
//
//        s3Service.deleteFile(imageUrl)
//        listing.imageUrls = listing.imageUrls?.filter { it != imageUrl }
//
//        return ResultFactory.getSuccessResult(
//            data = produceListingRepository.save(listing)
//        )
//    }

//    fun listAProduceWithImage(
//        farmerId: String,
//        produceName: String,
//        quantity: String,
//        price: String,
//        images: List<MultipartFile>?
//    ): Result<FarmerProduce> {
//        return try {
//            if (quantity.toDoubleOrNull() == null || price.toDoubleOrNull() == null) {
//                return ResultFactory.getFailResult("Invalid quantity or price format")
//            }
//
//            val farmer = farmerRepository.findByIdOrNull(farmerId)
//                ?: return ResultFactory.getFailResult(msg = "Farmer not found")
//
//            // Find or create farm produce
//            val farmProduce = farmProduceRepository.findByNameLikeIgnoreCase(produceName).firstOrNull()
//                ?: farmProduceRepository.save(
//                    FarmProduce(
//                        name = produceName,
//                        description = "Auto-created produce",
//                        farmingType = "Standard",
//                        status = FarmProduceStatus.ACTIVE
//                    )
//                )
//
//
//            val farmerProduce = FarmerProduce(
//                farmer = farmer,
//                farmProduce = farmProduce,
//                description = "Listed from mobile",
//                farmingType = "Standard",
//                status = FarmerProduceStatus.ON_SALE,
//            )
//
//            // Save FarmerProduce first
//            val savedFarmerProduce = farmerProduceRepository.saveAndFlush(farmerProduce)
//
//            // Add reference after persisting FarmerProduce
//            farmer.farmerProduces.add(savedFarmerProduce)
//            farmerRepository.saveAndFlush(farmer)
//
//            // Create listing
//            val imageUrls = images?.map { s3Service.uploadFile(it) }
//
//            val listing = ProduceListing(
//                id = UUID.randomUUID().toString(),
//                farmerProduce = savedFarmerProduce,
//                price = price.toDouble(),
//                quantity = quantity.toDouble(),
//                unit = "KG",
//                status = ProduceListingStatus.ACTIVE,
//                createdAt = LocalDateTime.now(),
//                rating = 0.00,
//                imageUrls = imageUrls
//            )
//            produceListingRepository.save(listing)
//
//            ResultFactory.getSuccessResult(savedFarmerProduce)
//        } catch (e: Exception) {
//            println("Error Occurred: $e")
//            ResultFactory.getFailResult(msg = "Failed to create listing: ${e.message}")
//        }
//    }
    fun addProduceToListing(
        farmerProduceId: String,
        quantity: Double,
        price: Money,
        unit: String,
        images: List<MultipartFile>?
    ): Result<ProduceListing> {
        return try {
            logger.info("Looking up farmer produce with id: $farmerProduceId")
            val farmerProduce = farmerProduceRepository.findByIdOrNull(farmerProduceId)
            if (farmerProduce == null) {
                logger.error("Farmer produce not found with id: $farmerProduceId")
                return ResultFactory.getFailResult(msg = "Farmer produce not found.")
            }
            logger.info("Found farmer produce: ${farmerProduce.id}, farm produce: ${farmerProduce.farmProduce?.name}")

            val imageUrls = images?.map { s3Service.uploadFile(it) }
            logger.info("Uploaded ${imageUrls?.size ?: 0} images")

            val newListing = produceListingRepository.saveAndFlush(
                ProduceListing(
                    farmerProduce = farmerProduce,
                    price = price,
                    quantity = quantity,
                    unit = unit,
                    status = ProduceListingStatus.ACTIVE,
                    createdAt = LocalDateTime.now(),
                    rating = 0.00,
                    imageUrls = imageUrls,
                )
            )
            logger.info("Created new listing with id: ${newListing.id}")

            farmerProduce.status = FarmerProduceStatus.ON_SALE
            farmerProduceRepository.saveAndFlush(farmerProduce)

            ResultFactory.getSuccessResult(newListing)
        } catch (e: Exception) {
            logger.error("Error creating listing", e)
            ResultFactory.getFailResult(e.message)
        }
    }

//    @CachePut(value = ["listings"], key = "'listings'")
//    override fun listAProduce(listAProduceRequestDto: listAProduceRequestDto): Result<ProduceListing> {
//        return try {
//            val farmerProduce = farmerProduceRepository.findByIdOrNull(listAProduceRequestDto.farmerProduceId)
//                ?: return ResultFactory.getFailResult(msg = "Farmer produce not found.")
//            println("The farmer produce is: "+farmerProduce.id+" farmer: "+farmerProduce.farmer.name)
//            println(farmerProduce)
//            val newListing = produceListingRepository.saveAndFlush(
//                ProduceListing(
//                    id = UUID.randomUUID().toString(),
//                    farmerProduce = farmerProduce,
//                    price = listAProduceRequestDto.price,
//                    quantity = listAProduceRequestDto.quantity,
//                    unit = listAProduceRequestDto.unit,
//                    status = ProduceListingStatus.ACTIVE,
//                    createdAt = LocalDateTime.now(),
//                    rating = 0.00,
//                )
//            )
//            farmerProduce.status = FarmerProduceStatus.ON_SALE
//            farmerProduceRepository.saveAndFlush(farmerProduce)
//
//            val cache = cacheManager.getCache("listings")
//            val cachedData = cache?.get("listings")?.get()
//
//            // Ensure it's a List<*> first, then filter and make it mutable
//            val currentListings = (cachedData as? List<*>)?.filterIsInstance<ProduceListing>()?.toMutableList() ?: mutableListOf()
//
//            // Add the new listing
//            currentListings.add(newListing)
//
//            // Update the cache
//            cache?.put("listings", currentListings)
//
//
//            ResultFactory.getSuccessResult(newListing)
//        }catch (e: Exception) {
//            ResultFactory.getFailResult(e.message)
//        }
//    }

    override fun updateListing(updateListingRequestDto: updateListingRequestDto): Result<ProduceListing> {
        TODO("Not yet implemented")
    }

    @CachePut(value = ["listings"], key = "'listings'")
    override fun addOrderToListing(addOrderToListingDto: AddOrderToListingDto): Result<ProduceListing> {
        try {
            if (addOrderToListingDto.quantity <= 0) {
                return ResultFactory.getFailResult(msg = "Please input the correct quantity")
            }

            val produceListing = produceListingRepository.findByIdOrNull(addOrderToListingDto.listingId)
                ?: return ResultFactory.getFailResult(msg = produceListingNotFound)

            if (produceListing.quantity < addOrderToListingDto.quantity) {
                return ResultFactory.getFailResult(msg = "You have submitted more quantity than requested")
            }

            // Ensure produceListing is managed
            val managedListing = produceListingRepository.saveAndFlush(produceListing)

            // Create and persist the new order
            val newOrder = listingOrderRepository.save(
                ListingOrder(
                    buyerId = addOrderToListingDto.buyerId,
                    quantity = addOrderToListingDto.quantity,
                    dateCreated = LocalDateTime.now(),
                    status = OrderStatus.PENDING_ACCEPTANCE,
                    produceListing = managedListing
                )
            )

            managedListing.listingOrders.add(newOrder)

            val quantityOrdered = managedListing.listingOrders.sumOf { it.quantity }
            if (managedListing.quantity == quantityOrdered) {
                managedListing.farmerProduce.status = FarmerProduceStatus.ON_FARM
                managedListing.status = ProduceListingStatus.CLOSED
                farmerProduceRepository.saveAndFlush(managedListing.farmerProduce)
            }
            updateListingInCache(managedListing)
            return ResultFactory.getSuccessResult(produceListingRepository.saveAndFlush(managedListing))
        } catch (e: Exception) {
            return ResultFactory.getFailResult(e.message)
        }
    }


    override fun acceptOrder(orderId: String): Result<ListingOrder> {
        return try {
            val order = listingOrderRepository.findByIdOrNull(orderId)
                ?: return ResultFactory.getFailResult(msg=orderNotFound)
            order.dateAccepted = LocalDateTime.now()
            order.status = OrderStatus.BOOKED_FOR_SUPPLY
            ResultFactory.getSuccessResult(listingOrderRepository.saveAndFlush(order))
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun declineOrder(orderId: String, farmerComment: String): Result<ListingOrder> {
        return try {
            val order = listingOrderRepository.findByIdOrNull(orderId)
                ?: return ResultFactory.getFailResult(msg=orderNotFound)
            order.dateDeclined = LocalDateTime.now()
            order.status = OrderStatus.DECLINED
            order.farmerComment = farmerComment
            ResultFactory.getSuccessResult(listingOrderRepository.saveAndFlush(order))
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun confirmSupply(orderId: String): Result<ListingOrder> {
        return try {
            val order = listingOrderRepository.findByIdOrNull(orderId)
                ?: return ResultFactory.getFailResult(msg=orderNotFound)
            order.dateSupplied = LocalDateTime.now()
            order.status = OrderStatus.SUPPLIED
            ResultFactory.getSuccessResult(listingOrderRepository.saveAndFlush(order))
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun confirmPayment(orderId: String): Result<ListingOrder> {
        return try {
            val order = listingOrderRepository.findByIdOrNull(orderId)
                ?: return ResultFactory.getFailResult(msg=orderNotFound)
            order.datePaid = LocalDateTime.now()
            order.status = OrderStatus.SUPPLIED_AND_PAID
            ResultFactory.getSuccessResult(listingOrderRepository.saveAndFlush(order))
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun unlist(produceListingId: String): Result<String> {
        return try {
            val produceListing = produceListingRepository.findByIdOrNull(produceListingId)
                ?: return ResultFactory.getFailResult(msg = produceListingNotFound)
            produceListing.status = ProduceListingStatus.CANCELLED
            ResultFactory.getSuccessResult(data="Unlisted")
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun getListing(listingId: String): Result<ListingResponseDto> {
        return try {
            val produceListing = produceListingRepository.findByIdOrNull(listingId)
                ?: return ResultFactory.getFailResult(msg = produceListingNotFound)
            val listingResponseDto =
                ListingResponseDto(
                    produceListing = produceListing,
                    quantityLeft = produceListing.quantity,
                    quantitySold = 0.00,
                    earnings = 0.00,
                    noOfPurchases = produceListing.listingOrders.size
                )

            produceListing.listingOrders.forEach { listingOrder: ListingOrder ->
                run {
                    listingResponseDto.quantitySold += listingOrder.quantity
                    listingResponseDto.quantityLeft -= listingOrder.quantity
                    listingResponseDto.earnings += (listingOrder.quantity * produceListing.price.price)
                }
            }
            ResultFactory.getSuccessResult(listingResponseDto)
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun getFarmerListings(farmerId: String, pageable: Pageable): Result<Page<ProduceListing>> {
        return try {
            ResultFactory.getSuccessResult(produceListingRepository.getFarmerListings(farmerId, pageable))
        }catch (e: Exception) {
            ResultFactory.getFailResult(msg = e.message)
        }
    }

    // cache
    fun getCachedListings(): List<ProduceListing> {
        val cache = cacheManager.getCache("listings")
        return cache?.get("listings")?.get()?.let { it as? List<*> }
            ?.filterIsInstance<ProduceListing>() ?: emptyList()
    }
    fun getCachedListingById(listingId: String): ProduceListing? {
        val cache = cacheManager.getCache("listings")
        val cachedData = cache?.get("listings")?.get()

        // Ensure safe type conversion and filter the required listing
        return (cachedData as? List<*>)?.filterIsInstance<ProduceListing>()
            ?.find { it.id == listingId }
    }


    fun addListingToCache(newListing: ProduceListing) {
        val cache = cacheManager.getCache("listings")
        val existingListings = getCachedListings().toMutableList()
        existingListings.add(newListing)
        cache?.put("listings", existingListings)
    }

    fun updateListingInCache(updatedListing: ProduceListing) {
        val cache = cacheManager.getCache("listings")
        val existingListings = getCachedListings().toMutableList()
        val index = existingListings.indexOfFirst { it.id == updatedListing.id }
        if (index != -1) {
            existingListings[index] = updatedListing
            cache?.put("listings", existingListings)
        }
    }
}