package com.agriconnect.farmersportalapis.buyers.infrastructure.repositories

import com.agriconnect.farmersportalapis.buyers.domain.common.model.BSFarmProduce
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BSProduceRepository: JpaRepository<BSFarmProduce, String> {
}