package com.agriconnect.farmersportalapis.application.tools

import com.agriconnect.farmersportalapis.service.supplychain.FarmerService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool

class UserTool(
    private val farmerService: FarmerService,
) {
    private val logger: Logger = LoggerFactory.getLogger(UserTool::class.java)
    @Tool(
        name="getUserDetails",
        description = "get name and produces of user"
    )
    fun getUserDetails(userId: String, userSection: String): String {
        logger.info("fetching user details")
        if (userSection.lowercase().startsWith("farmer")) {
            val farmerResult = farmerService.getFarmer(userId)
            return if (farmerResult.success) {
                var producesList = ""
                try {
                    if (farmerResult.data?.farmerProduces?.size!! > 0  ) {
                        producesList += "preferredProduces="
                        farmerResult.data.farmerProduces
                        for (farmProduce in farmerResult.data.farmerProduces) {
                            producesList += "{name=${farmProduce.farmProduce.name}, status=${farmProduce.status}}"
                        }
                    }
                    else
                        producesList += "preferredProduces=none"
                } catch (e: Exception)  {
                    logger.error(e.message)
                }
                """
                Retrieved ${farmerResult.data?.userProfile?.fullName} details successfully
                $producesList
                """.trimIndent()
            } else {
                logger.error("{} section details retrieval error: {}", userSection, farmerResult.msg)
                """
                Failed to retrieve details.
                msg: ${farmerResult.msg}
            """.trimIndent()
            }
        } else {
            logger.error("{} section is unknown", userSection)
            return """
                userSection $userSection has not been set yet to get credentials
            """.trimIndent()
        }
    }

//    @Tool(
//        name="getIdOfAProduceOfFarmer",
//        description="This adds produce to farmers profile"
//    )
//    fun getProduceId(
//        farmProduceName: String,
//        userId: String
//    ): String {
//        val getFarmerRes = farmerService.getFarmer(userId)
//        if (!getFarmerRes.success) {
//            return getFarmerRes.msg ?: "farmer of $userId not found"
//        }
//        val farmer = getFarmerRes.data!!
//
//        val getProduceRes = farmer.farmerProduces.filter { it.farmProduce.name == farmProduceName }
//        return if (getProduceRes.isNotEmpty()) {
//            getProduceRes.first().id.toString()
//        } else {
//            "No produce found with name $farmProduceName"
//        }
//    }
//
//    @Tool(
//        name="addProduceToFarmerProfile",
//        description="This adds produce to farmers profile"
//    )
//    fun addProduceToFarmer(
//        produceId: String,
//        produceDesc: String,
//        produceFarmingType: String,
//        userId: String
//    ) {
//
//    }
}