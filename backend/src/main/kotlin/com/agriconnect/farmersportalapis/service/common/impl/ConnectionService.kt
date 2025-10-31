package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.service.common.IConnectionService
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.common.enums.ConnectionStatus
import com.agriconnect.farmersportalapis.domain.common.model.FarmerBuyerConnection
import com.agriconnect.farmersportalapis.infrastructure.repositories.ConnectionRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ConnectionService(
    @Autowired var farmerRepository: FarmerRepository,
    @Autowired var connectionRepository: ConnectionRepository
): IConnectionService {

    val farmerNotFound = "Farmer is not found"

    override fun connectFarmerToBuyer(farmerId: String, buyerId: String): Result<FarmerBuyerConnection> {
        farmerRepository.findByIdOrNull(farmerId)
            ?: return ResultFactory.getFailResult(msg = farmerNotFound)

        val existingConnection = connectionRepository.findByFarmerIdAndBuyerId(farmerId, buyerId)
        if (existingConnection != null) {
            return ResultFactory.getFailResult(msg = "Connection already exists")
        }

        return try {
            ResultFactory.getSuccessResult(connectionRepository.save(
                FarmerBuyerConnection(
                    farmerId = farmerId,
                    buyerId = buyerId,
                    status = ConnectionStatus.ACTIVE,
                    createdAt = LocalDateTime.now()
                )
            ))
        } catch (e: Exception){
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun getBuyersForFarmer(farmerId: String): Result<List<FarmerBuyerConnection>> {
        return try {
            ResultFactory.getSuccessResult(connectionRepository.findByFarmerId(farmerId))
        } catch (e: Exception){
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun getFarmersForBuyer(buyerId: String): Result<List<FarmerBuyerConnection>> {
        return try {
            ResultFactory.getSuccessResult(connectionRepository.findByBuyerId(buyerId))
        } catch (e: Exception){
            ResultFactory.getFailResult(e.message)
        }
    }

    override fun disconnectFarmerAndBuyer(farmerId: String, buyerId: String): Result<String> {
        return try {
            val connection = connectionRepository.findByFarmerIdAndBuyerId(farmerId, buyerId)
            return if (connection != null) {
                connectionRepository.delete(connection)
                ResultFactory.getSuccessResult(data="Connection Successfully removed")
            } else {
                ResultFactory.getFailResult(msg="No existing connection to remove")
            }
        } catch (e: Exception){
            ResultFactory.getFailResult(e.message)
        }
    }

}