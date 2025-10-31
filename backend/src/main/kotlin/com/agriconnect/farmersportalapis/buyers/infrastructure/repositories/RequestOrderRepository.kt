package com.agriconnect.farmersportalapis.buyers.infrastructure.repositories

import com.agriconnect.farmersportalapis.buyers.application.dtos.RequestOrderDto
import com.agriconnect.farmersportalapis.buyers.domain.request.RequestOrder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RequestOrderRepository: JpaRepository<RequestOrder, String> {
    @Query("""
        SELECT new com.agriconnect.farmersportalapis.buyers.application.dtos.RequestOrderDto(
            lo.id, pl.id, lo.farmerId, lo.dateCreated, lo.dateAccepted, lo.dateSupplied, lo.datePaid, 
            lo.quantity, lo.status, f.name, f.description, f.status
        )
        FROM RequestOrder lo
        JOIN lo.produceRequest pl
        JOIN pl.preferredProduce fp
        JOIN fp.BSFarmProduce f
        WHERE lo.farmerId = :farmerId
    """)
    fun findRequestOrderByFarmerId(farmerId: String): List<RequestOrderDto>
}