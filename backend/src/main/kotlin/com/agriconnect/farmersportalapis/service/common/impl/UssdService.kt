package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.domain.common.model.FarmProduce
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmProduceRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ProduceListingRepository

import org.springframework.stereotype.Service

@Service
class UssdService(
    private val farmProduceRepository: FarmProduceRepository,
    private val produceListingRepository: ProduceListingRepository
) {

    fun handleUssdRequest(sessionId: String, phoneNumber: String, userInput: String): String {
        val produces = farmProduceRepository.findAllActiveProduces()

        return when {
            userInput.isEmpty() -> farmProduceMenu(produces) // Bypass welcome page, go straight to produce list
            userInput.toIntOrNull() != null && userInput.toInt() in 1..produces.size -> fetchMarketPrice(produces[userInput.toInt() - 1].name)
            else -> "END Invalid input. Please try again."
        }
    }

    private fun farmProduceMenu(produces: List<FarmProduce>): String {
        val menu = produces.mapIndexed { index, produce -> "${index + 1}. ${produce.name}" }.joinToString("\n")
        return "CON Select a produce:\n$menu"
    }

    private fun fetchMarketPrice(produceName: String): String {
        val price = produceListingRepository.findLatestPriceByProduce(produceName)
        return if (price != null) {
            "CON Current price of $produceName: KES ${price} per KG\n" +
                    "1. Back to market prices\n" +
                    "2. Exit"
        } else {
            "END Sorry, no price data available."
        }
    }
}
