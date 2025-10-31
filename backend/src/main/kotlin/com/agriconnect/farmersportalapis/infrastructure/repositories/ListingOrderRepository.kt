package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.application.dtos.ListingOrderDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.BuyerReportDTO
import com.agriconnect.farmersportalapis.domain.listing.ListingOrder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ListingOrderRepository: JpaRepository<ListingOrder, String> {
    @Query("""
        SELECT new com.agriconnect.farmersportalapis.application.dtos.ListingOrderDto(
            lo.id, pl.id, pl.price.price, lo.buyerId, lo.dateCreated, lo.dateAccepted, lo.dateSupplied, lo.datePaid, 
            lo.quantity, lo.status, f.name, f.description, f.status
        )
        FROM ListingOrder lo
        JOIN lo.produceListing pl
        JOIN pl.farmerProduce fp
        JOIN fp.farmProduce f
        WHERE lo.buyerId = :buyerId
    """)
    fun findListingOrderByBuyerId(buyerId: String): List<ListingOrderDto>

    @Query(
        """
        SELECT new com.agriconnect.farmersportalapis.buyers.application.dtos.BuyerReportDTO(
            lo.buyerId,
            f.userProfile.fullName AS farmerName,
            fp.farmProduce.name AS produceName,
            SUM(lo.quantity * pl.price.price) AS totalSpent,
            COUNT(DISTINCT lo.produceListing.farmerProduce.farmer.id) AS farmerInteractions,
            SUM(CASE WHEN lo.status = 'PENDING_ACCEPTANCE' THEN 1 ELSE 0 END) AS pendingOrders,
            SUM(CASE WHEN lo.status = 'BOOKED_FOR_SUPPLY' THEN 1 ELSE 0 END) AS acceptedOrders,
            SUM(CASE WHEN lo.status = 'SUPPLIED' THEN 1 ELSE 0 END) AS suppliedOrders
        )
        FROM ListingOrder lo
        JOIN lo.produceListing pl
        JOIN pl.farmerProduce fp
        JOIN fp.farmer f
        WHERE lo.buyerId = :buyerId AND lo.dateCreated BETWEEN :startDate AND :endDate
        GROUP BY lo.buyerId, f.userProfile.fullName, fp.farmProduce.name
    """
    )
    fun findBuyerReport(@Param("buyerId") buyerId: String, @Param("startDate") startDate: LocalDateTime, @Param("endDate") endDate: LocalDateTime): List<BuyerReportDTO>

    @Query("""
        SELECT new com.agriconnect.farmersportalapis.application.dtos.ListingOrderDto(
            lo.id, pl.id, pl.price.price, lo.buyerId, lo.dateCreated, lo.dateAccepted, lo.dateSupplied, lo.datePaid, 
            lo.quantity, lo.status, f.name, f.description, f.status
        )
        FROM ListingOrder lo
        JOIN lo.produceListing pl
        JOIN pl.farmerProduce fp
        JOIN fp.farmProduce f
        WHERE lo.buyerId = :buyerId
        AND lo.dateCreated >= :startDateTime AND lo.dateCreated <= :endDateTime
    """)
    fun findListingOrderByBuyerIdBetweenDates(buyerId: String, startDateTime: LocalDateTime,  endDateTime: LocalDateTime): List<ListingOrderDto>
}