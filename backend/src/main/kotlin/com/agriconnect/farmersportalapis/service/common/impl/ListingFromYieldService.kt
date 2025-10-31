package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.CreateListingFromYieldRequestDto
import com.agriconnect.farmersportalapis.application.dtos.ListingFromYieldResponseDto
import com.agriconnect.farmersportalapis.application.dtos.YieldAvailableAmountResponseDto
import com.agriconnect.farmersportalapis.application.dtos.UpdateListedAmountRequestDto
import com.agriconnect.farmersportalapis.application.dtos.YieldToListingRequestDto
import com.agriconnect.farmersportalapis.application.dtos.YieldRecordResponseDto
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.listing.ProduceListing
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProduceListingRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProduceYieldRepository
import org.springframework.stereotype.Service

@Service
class ListingFromYieldService(
    private val produceYieldRepository: ProduceYieldRepository,
    private val produceListingRepository: ProduceListingRepository
) {
    fun createListingFromYield(dto: CreateListingFromYieldRequestDto): Result<ListingFromYieldResponseDto> {
        val yieldRecord = produceYieldRepository.findById(dto.yieldId).orElse(null)
            ?: return ResultFactory.getFailResult("Yield record not found.")
        // Validate available amount
        if (dto.listingAmount > yieldRecord.remainingAmount) {
            return ResultFactory.getFailResult("Listing amount exceeds available yield.")
        }
        if (dto.listingAmount <= 0.0) {
            return ResultFactory.getFailResult("Listing amount must be positive.")
        }
        // Create listing
        val listing = ProduceListing(
            id = java.util.UUID.randomUUID().toString(),
            quantity = dto.listingAmount.toDouble(),
            price = com.agriconnect.farmersportalapis.domain.common.valueobject.Money(
                price = dto.pricePerUnit.toDouble(),
                currency = "KES" // or use a dynamic value if available
            ),
            unit = yieldRecord.yieldUnit,
            rating = 0.0,
            status = com.agriconnect.farmersportalapis.domain.common.enums.ProduceListingStatus.ACTIVE,
            createdAt = java.time.LocalDateTime.now(),
            imageUrls = yieldRecord.farmerProduce.imageUrls ?: listOf(),
            farmerProduce = yieldRecord.farmerProduce
        )
        produceListingRepository.save(listing)
        // Update yield record
        yieldRecord.listedAmount = yieldRecord.listedAmount + dto.listingAmount.toDouble()
        yieldRecord.remainingAmount = yieldRecord.yieldAmount - yieldRecord.listedAmount
        produceYieldRepository.save(yieldRecord)
        return ResultFactory.getSuccessResult(
            ListingFromYieldResponseDto(
                listingId = listing.id,
                yieldId = yieldRecord.id,
                listedAmount = listing.quantity,
                remainingYield = yieldRecord.remainingAmount,
                produceName = yieldRecord.farmerProduce.farmProduce.name,
                harvestDate = yieldRecord.harvestDate,
                message = "Listing created successfully from yield."
            )
        )
    }

    fun getYieldAvailableAmount(yieldId: String): Result<YieldAvailableAmountResponseDto> {
        val yieldRecord = produceYieldRepository.findById(yieldId).orElse(null)
            ?: return ResultFactory.getFailResult("Yield record not found.")
        return ResultFactory.getSuccessResult(
            YieldAvailableAmountResponseDto(
                yieldId = yieldId,
                availableAmount = yieldRecord.remainingAmount
            )
        )
    }

    fun updateListedAmount(yieldId: String, dto: UpdateListedAmountRequestDto): Result<YieldRecordResponseDto> {
        val yieldRecord = produceYieldRepository.findById(yieldId).orElse(null)
            ?: return ResultFactory.getFailResult("Yield record not found.")
        val newListedAmount = dto.listedAmount
        if (newListedAmount < 0.0 || newListedAmount > yieldRecord.yieldAmount) {
            return ResultFactory.getFailResult("Invalid listed amount.")
        }
        yieldRecord.listedAmount = newListedAmount
        yieldRecord.remainingAmount = yieldRecord.yieldAmount - newListedAmount
        produceYieldRepository.save(yieldRecord)
        // Return updated yield record as YieldRecordResponseDto
        return ResultFactory.getSuccessResult(
            YieldRecordResponseDto(
                id = yieldRecord.id,
                farmerProduceId = yieldRecord.farmerProduce.id,
                produceName = yieldRecord.farmerProduce.farmProduce.name,
                yieldAmount = yieldRecord.yieldAmount,
                yieldUnit = yieldRecord.yieldUnit,
                harvestDate = yieldRecord.harvestDate,
                seasonYear = yieldRecord.seasonYear,
                seasonName = yieldRecord.seasonName,
                notes = yieldRecord.notes,
                listedAmount = yieldRecord.listedAmount,
                remainingAmount = yieldRecord.remainingAmount,
                growthDays = 0, // You can calculate if needed
                createdAt = yieldRecord.createdAt
            )
        )
    }
    fun yieldToListing(dto: YieldToListingRequestDto): Result<ListingFromYieldResponseDto> {
        val yieldRecord = produceYieldRepository.findById(dto.yieldId).orElse(null)
            ?: return ResultFactory.getFailResult("Yield record not found.")
        if (dto.listingAmount > yieldRecord.remainingAmount) {
            return ResultFactory.getFailResult("Listing amount exceeds available yield.")
        }
        if (dto.listingAmount <= 0.0) {
            return ResultFactory.getFailResult("Listing amount must be positive.")
        }
        val listing = ProduceListing(
            id = java.util.UUID.randomUUID().toString(),
            quantity = dto.listingAmount,
            price = com.agriconnect.farmersportalapis.domain.common.valueobject.Money(
                price = dto.pricePerUnit,
                currency = "KES"
            ),
            unit = yieldRecord.yieldUnit,
            rating = 0.0,
            status = com.agriconnect.farmersportalapis.domain.common.enums.ProduceListingStatus.ACTIVE,
            createdAt = java.time.LocalDateTime.now(),
            imageUrls = yieldRecord.farmerProduce.imageUrls ?: listOf(),
            farmerProduce = yieldRecord.farmerProduce
        )
        produceListingRepository.save(listing)
        yieldRecord.listedAmount = yieldRecord.listedAmount + dto.listingAmount
        yieldRecord.remainingAmount = yieldRecord.yieldAmount - yieldRecord.listedAmount
        produceYieldRepository.save(yieldRecord)
        return ResultFactory.getSuccessResult(
            ListingFromYieldResponseDto(
                listingId = listing.id,
                yieldId = yieldRecord.id,
                listedAmount = listing.quantity,
                remainingYield = yieldRecord.remainingAmount,
                produceName = yieldRecord.farmerProduce.farmProduce.name,
                harvestDate = yieldRecord.harvestDate,
                message = "Yield successfully converted to listing."
            )
        )
    }

    fun isYieldFullyListed(yieldId: String): Result<Boolean> {
        val yieldRecord = produceYieldRepository.findById(yieldId).orElse(null)
            ?: return ResultFactory.getFailResult("Yield record not found.")
        return ResultFactory.getSuccessResult(yieldRecord.remainingAmount == 0.0)
    }
}
