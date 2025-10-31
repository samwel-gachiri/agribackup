package com.agriconnect.farmersportalapis.buyers.infrastructure.repositories

import com.agriconnect.farmersportalapis.buyers.domain.request.ProduceRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProduceRequestRepository: JpaRepository<ProduceRequest, String> {
    @Query("SELECT p FROM ProduceRequest p where p.preferredProduce.buyer.id = :buyerId")
    fun getBuyerRequests(@Param("buyerId") buyerId: String, pageable: Pageable): Page<ProduceRequest>

}