package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.application.dtos.ReportDto
import com.agriconnect.farmersportalapis.domain.listing.ListingOrder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ListingOrderReportRepository: JpaRepository<ListingOrder, String> {
    @Query(value = """
       SELECT new com.agriconnect.farmersportalapis.application.dtos.ReportDto(
            o.datePaid,
            o.produceListing.farmerProduce.farmProduce.name,
            sum(o.quantity),
            o.produceListing.price.currency,
            sum(o.quantity * o.produceListing.price.price)) 
        FROM ListingOrder o
        WHERE o.produceListing.farmerProduce.farmer.id = :farmerId
        AND o.datePaid >= :startDateTime
        AND o.datePaid <= :endDateTime
        group by o.produceListing.farmerProduce.id, o.produceListing.farmerProduce.farmProduce.id, o.datePaid, o.produceListing.price.currency
    """
    )
    fun getReport(@Param("farmerId") farmerId: String, @Param("startDateTime") startDateTime: LocalDateTime, @Param("endDateTime") endDateTime: LocalDateTime): List<ReportDto>

    @Query(value = """
       SELECT new com.agriconnect.farmersportalapis.application.dtos.ReportDto(
            o.datePaid,
            o.produceListing.farmerProduce.farmProduce.name,
            o.quantity,
            o.produceListing.price.currency,
            o.quantity * o.produceListing.price.price
        ) 
        FROM ListingOrder o
        WHERE o.produceListing.farmerProduce.farmer.id = :farmerId
        AND o.datePaid >= :startDateTime
        AND o.datePaid <= :endDateTime
    """
    )
    fun getOrderHistory(@Param("farmerId") farmerId: String, @Param("startDateTime") startDateTime: LocalDateTime, @Param("endDateTime") endDateTime: LocalDateTime): List<ReportDto>
}