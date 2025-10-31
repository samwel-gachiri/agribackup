package com.agriconnect.farmersportalapis.buyers.application.services.impl

import com.agriconnect.farmersportalapis.buyers.application.dtos.createFarmProduceDto
import com.agriconnect.farmersportalapis.buyers.application.services.IBSFarmProduceService
import com.agriconnect.farmersportalapis.buyers.application.util.Result
import com.agriconnect.farmersportalapis.buyers.application.util.ResultFactory
import com.agriconnect.farmersportalapis.buyers.domain.common.enums.FarmProduceStatus
import com.agriconnect.farmersportalapis.buyers.domain.common.model.BSFarmProduce
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.BSProduceRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class BSFarmProduceService: IBSFarmProduceService {
    @Autowired
    lateinit var BSProduceRepository: BSProduceRepository

    val produceNotFoundTxt = "Produce not found."

    override fun createFarmProduce(createFarmProduceDto: createFarmProduceDto): Result<BSFarmProduce> {
        return try {
            ResultFactory.getSuccessResult(BSProduceRepository.saveAndFlush(BSFarmProduce(
                name = createFarmProduceDto.name,
                description = createFarmProduceDto.description,
                farmingType = createFarmProduceDto.farmingType,
                status = FarmProduceStatus.ACTIVE,
            )))
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun deleteFarmProduce(farmProduceId: String): Result<String> {
        val produceExists = BSProduceRepository.findByIdOrNull(farmProduceId)
            ?: return ResultFactory.getFailResult(msg = produceNotFoundTxt)
        BSProduceRepository.delete(produceExists)

        return ResultFactory.getSuccessResult<String>(msg = "Farm produce deleted successfully")
    }

    override fun getFarmProduces(): Result<List<BSFarmProduce>> {
        return try {
            val produces = BSProduceRepository.findAll()
            ResultFactory.getSuccessResult(produces);
        }catch (e: Exception) {
            ResultFactory.getFailResult(e.message)
        }

    }
}