package com.agriconnect.farmersportalapis.buyers.infrastructure.repositories

import com.agriconnect.farmersportalapis.buyers.application.dtos.ReportDto
import com.agriconnect.farmersportalapis.buyers.domain.request.RequestOrder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RequestOrderReportRepository: JpaRepository<RequestOrder, String> {
    @Query(value = """
       SELECT new com.agriconnect.farmersportalapis.buyers.application.dtos.ReportDto(
            o.datePaid,
            o.produceRequest.preferredProduce.BSFarmProduce.name,
            sum(o.quantity),
            o.produceRequest.price.currency,
            sum(o.quantity * o.produceRequest.price.price)) 
        FROM RequestOrder o
        WHERE o.produceRequest.preferredProduce.buyer.id = :buyerId
        AND o.datePaid >= :startDateTime
        AND o.datePaid <= :endDateTime
        group by o.produceRequest.preferredProduce.id, o.produceRequest.preferredProduce.BSFarmProduce.id, o.datePaid, o.produceRequest.price.currency
    """
    )
    fun getReport(@Param("buyerId") buyerId: String, @Param("startDateTime") startDateTime: LocalDateTime, @Param("endDateTime") endDateTime: LocalDateTime): List<ReportDto>

    @Query(value = """
       SELECT new com.agriconnect.farmersportalapis.buyers.application.dtos.ReportDto(
            o.datePaid,
            o.produceRequest.preferredProduce.BSFarmProduce.name,
            o.quantity,
            o.produceRequest.price.currency,
            o.quantity * o.produceRequest.price.price
        ) 
        FROM RequestOrder o
        WHERE o.produceRequest.preferredProduce.buyer.id = :buyerId
        AND o.datePaid >= :startDateTime
        AND o.datePaid <= :endDateTime
    """
    )
    fun getOrderHistory(@Param("buyerId") buyerId: String, @Param("startDateTime") startDateTime: LocalDateTime, @Param("endDateTime") endDateTime: LocalDateTime): List<ReportDto>
}