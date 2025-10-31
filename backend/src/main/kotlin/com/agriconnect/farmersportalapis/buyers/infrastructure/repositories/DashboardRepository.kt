package com.agriconnect.farmersportalapis.buyers.infrastructure.repositories

import com.agriconnect.farmersportalapis.buyers.domain.common.enums.OrderStatus
import com.agriconnect.farmersportalapis.buyers.domain.common.valueobject.Money
import com.agriconnect.farmersportalapis.buyers.domain.request.ProduceRequest
import com.agriconnect.farmersportalapis.buyers.domain.request.RequestOrder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface DashboardRequestRepository: JpaRepository<ProduceRequest, String> {

    @Query(value = "SELECT count(*) FROM produce_requests pl " +
            "JOIN preferred_produces fp ON pl.preferred_produces_id = fp.id " +
            "WHERE pl.status IN :stages AND fp.buyer_id = :buyerId", nativeQuery = true)
    fun getProduceRequestStageCount(@Param("stages") stages: Array<String>, @Param("buyerId") buyerId: String): Int

}

@Repository
interface DashboardRequestOrderRepository: JpaRepository<RequestOrder, String> {

    @Query(value = "SELECT count(*) FROM RequestOrder lo WHERE lo.produceRequest.preferredProduce.buyer.id = :buyerId")
    fun getBuyersInteractions(@Param("buyerId") buyerId: String): Int

    @Query(value = "SELECT new com.agriconnect.farmersportalapis.buyers.domain.common.valueobject.Money(COALESCE(sum(lo.quantity * lo.produceRequest.price.price), 0.0), lo.produceRequest.price.currency) FROM RequestOrder lo WHERE lo.status = :status AND lo.produceRequest.preferredProduce.buyer.id = :buyerId AND lo.dateCreated > :dateCreated group by lo.produceRequest.price.currency")
    fun getRevenue(@Param("buyerId") buyerId: String, @Param("status") status: OrderStatus = OrderStatus.SUPPLIED_AND_PAID, @Param("dateCreated") dateCreated: LocalDateTime = LocalDateTime.MIN): Money

}
