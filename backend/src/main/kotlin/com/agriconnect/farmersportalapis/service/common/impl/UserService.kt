package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.infrastructure.repositories.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(private val userRepository: UserRepository) {

//    fun getExistence(phoneNumber: String, email: String):Result<ExistFarmerOrBuyerDTO> {
//        return ResultFactory.getSuccessResult(userRepository.getExistence(phoneNumber, email))
//    }
}
