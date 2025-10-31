package com.agriconnect.farmersportalapis.buyers.application.services.impl

import com.agriconnect.farmersportalapis.buyers.application.dtos.AddProducesToBuyerDto
import com.agriconnect.farmersportalapis.buyers.application.dtos.UpdateBuyerDto
import com.agriconnect.farmersportalapis.buyers.application.services.IBuyerService
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.application.util.ResultFactory
import com.agriconnect.farmersportalapis.buyers.domain.common.enums.BuyerProduceStatus
import com.agriconnect.farmersportalapis.buyers.domain.common.model.BuyerLocation
import com.agriconnect.farmersportalapis.buyers.domain.profile.Buyer
import com.agriconnect.farmersportalapis.buyers.domain.profile.PreferredProduce
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.BSProduceRepository
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.BuyerRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BuyerService: IBuyerService {

    @Autowired
    lateinit var buyerRepository: BuyerRepository

    val logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var BSProduceRepository: BSProduceRepository

    val buyerNotFound = "Buyer is not found"

//    override fun createBuyer(buyer: Buyer): Result<Buyer> {
//        buyer.createdAt = LocalDateTime.now()
//        return try {
//            val savedBuyer = buyerRepository.saveAndFlush(buyer)
//            ResultFactory.getSuccessResult(
//                data = savedBuyer,
//                msg = "${savedBuyer.name} saved"
//            )
//        }catch (e: Exception){
//            ResultFactory.getFailResult(e.message)
//        }
//    }

    override fun updateBuyer(updateBuyerDto: UpdateBuyerDto): Result<Buyer> {
        return try {
            val buyer = buyerRepository.findByIdOrNull(updateBuyerDto.buyerId)
                ?: return ResultFactory.getFailResult(msg = buyerNotFound);
            buyer.applyUpdates(dto = updateBuyerDto)
            ResultFactory.getSuccessResult(data = buyerRepository.saveAndFlush(buyer), msg = "updated")
        } catch (e: Exception) {
            logger.error("Error updating buyer", e)
            ResultFactory.getFailResult(msg = "Could not fetch users. Invalid user pool or user group")
        }
    }

    private fun Buyer.applyUpdates(dto: UpdateBuyerDto) {
//        phoneNumber = dto.phoneNumber
//        email = dto.email
//        name = dto.name
        location = dto.location.let { BuyerLocation(
            latitude = it.latitude,
            longitude = it.longitude,
            customName = it.customName,
            buyer = this,
        )}
    }
    override fun getBuyers(): Result<List<Buyer>> {
        return try {
            val result = buyerRepository.findAll()
            ResultFactory.getSuccessResult(data = result)
        } catch (e: Exception) {
            logger.error("Error fetching buyers", e)
            ResultFactory.getFailResult(msg = "Could not fetch users. Invalid user pool or user group")
        }
    }

    override fun getBuyer(buyerId: String): Result<Buyer> {
        return try {
            val result = buyerRepository.findByIdOrNull(buyerId)
                ?: return ResultFactory.getFailResult(msg = buyerNotFound);

            ResultFactory.getSuccessResult(data = result)
        } catch (e: Exception) {
            logger.error("Error fetching buyer with ID: $buyerId", e)
            ResultFactory.getFailResult(msg = "Could not fetch users. Invalid user pool or user group")
        }
    }

    override fun addProducesToBuyer(addProducesToBuyerDto: AddProducesToBuyerDto): Result<List<PreferredProduce>> {
        return try {
            val buyer = buyerRepository.findByIdOrNull(addProducesToBuyerDto.buyerId)
                ?: return ResultFactory.getFailResult(msg = buyerNotFound)
            val produces = BSProduceRepository.findAllById(addProducesToBuyerDto.buyerProducesId)

            if (produces.size == 0)
                return ResultFactory.getFailResult(msg = "No produce matched the ids given")

            // produces.forEach { f -> println("produce: ${f.name}") }

            for(produce in produces) {
                buyer.preferredProduces.add(PreferredProduce(
                    buyer = buyer,
                    BSFarmProduce = produce,
                    status = BuyerProduceStatus.ACTIVE
                ))
            }
            // println("saving buyer: ${buyer.id} with ${buyer.preferredProduces.size} produces")
            val savedBuyer = buyerRepository.saveAndFlush(buyer);
            // println("saved buyer: $savedBuyer")
            ResultFactory.getSuccessResult(savedBuyer.preferredProduces)
        } catch (e: Exception) {
            logger.error("Error adding produces to buyer", e)
            ResultFactory.getFailResult(msg = "Could not fetch users. Invalid user pool or user group")
        }
    }

//    override fun updateBuyer(buyer: Buyer): Result<String> {
////        buyerRepository.
//    }

}