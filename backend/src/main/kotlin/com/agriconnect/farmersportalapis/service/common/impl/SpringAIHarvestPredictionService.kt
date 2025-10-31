package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.HarvestPredictionDto
import com.agriconnect.farmersportalapis.service.common.HarvestPredictionService
import com.agriconnect.farmersportalapis.domain.common.enums.FarmerProduceStatus
import com.agriconnect.farmersportalapis.domain.profile.FarmerProduce
import com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerProduceRepository
import org.slf4j.LoggerFactory
import org.springframework.ai.anthropic.AnthropicChatModel
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor
import org.springframework.ai.chat.memory.MessageWindowChatMemory
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.min

@Service
class SpringAIHarvestPredictionService(
    private val chatModel: AnthropicChatModel,
    private val farmerProduceRepository: FarmerProduceRepository
) : HarvestPredictionService {

    private val logger = LoggerFactory.getLogger(SpringAIHarvestPredictionService::class.java)

    companion object {
        private const val PREDICTION_PROMPT_TEMPLATE = """
            You are an agricultural AI expert specializing in crop harvest predictions.
            
            Given the following crop information:
            - Crop Type: {cropType}
            - Planting Date: {plantingDate}
            - Location: {location}
            - Current Date: {currentDate}
            - Days Since Planting: {daysSincePlanting}
            - Season: {season}
            - Historical Data: {historicalData}
            
            Please provide a harvest prediction in the following JSON format:
            {{
                "predictedHarvestDate": "YYYY-MM-DD",
                "confidence": 0.85,
                "predictedSpecies": "specific variety if applicable",
                "estimatedYield": 150.5,
                "yieldUnit": "kg",
                "growthStage": "flowering/fruiting/mature",
                "riskFactors": ["weather", "pests", "disease"],
                "recommendations": ["irrigation advice", "fertilizer timing"]
            }}
            
            Base your prediction on:
            1. Typical growing cycles for {cropType}
            2. Seasonal patterns and climate considerations
            3. Current growth stage based on days since planting
            4. Regional agricultural practices
            
            Ensure the predicted harvest date is realistic and falls within the typical growing period for {cropType}.
            Confidence should be between 0.6 and 0.95 based on data quality and crop predictability.
        """
    }
    private val chatMemory = MessageWindowChatMemory
        .builder()
        .maxMessages(10) // Limit memory size to prevent excessive token usage
        .build()
    private val chatClient = ChatClient
        .builder(chatModel)
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
        .defaultSystem(PREDICTION_PROMPT_TEMPLATE)
        .build()

    override fun predictHarvest(farmerProduceId: String): HarvestPredictionDto {
        return try {
            val farmerProduce = farmerProduceRepository.findById(farmerProduceId)
                .orElseThrow { IllegalArgumentException("Farmer produce not found with id: $farmerProduceId") }

            // Try AI prediction first
            val aiPrediction = generateAIPrediction(farmerProduce)
            
            // If AI prediction fails, fall back to heuristic
            aiPrediction ?: generateHeuristicPrediction(farmerProduce)
            
        } catch (e: Exception) {
            logger.error("Error predicting harvest for farmer produce $farmerProduceId", e)
            // Fallback to heuristic prediction
            val farmerProduce = farmerProduceRepository.findById(farmerProduceId).orElse(null)
            farmerProduce?.let { generateHeuristicPrediction(it) }
                ?: throw RuntimeException("Failed to generate harvest prediction", e)
        }
    }

    override fun predictHarvestBatch(farmerProduceIds: List<String>): List<HarvestPredictionDto> {
        return farmerProduceIds.mapNotNull { id ->
            try {
                predictHarvest(id)
            } catch (e: Exception) {
                logger.warn("Failed to predict harvest for farmer produce $id", e)
                null
            }
        }
    }

    override fun updatePrediction(farmerProduceId: String, actualHarvestDate: LocalDate): HarvestPredictionDto {
        val farmerProduce = farmerProduceRepository.findById(farmerProduceId)
            .orElseThrow { IllegalArgumentException("Farmer produce not found with id: $farmerProduceId") }

        // Update the farmer produce with actual harvest date
        farmerProduce.actualHarvestDate = actualHarvestDate
        farmerProduce.status = FarmerProduceStatus.HARVESTED
        farmerProduceRepository.save(farmerProduce)

        // Generate updated prediction (for learning purposes)
        return generateUpdatedPrediction(farmerProduce, actualHarvestDate)
    }

    private fun generateAIPrediction(farmerProduce: FarmerProduce): HarvestPredictionDto? {
        return try {
            val currentDate = LocalDate.now()
            val plantingDate = farmerProduce.plantingDate ?: return null
            val daysSincePlanting = ChronoUnit.DAYS.between(plantingDate, currentDate)
            
            // Prepare context for AI
            val context = mapOf(
                "cropType" to (farmerProduce.farmProduce?.name ?: "Unknown"),
                "plantingDate" to plantingDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                "location" to (farmerProduce.farmer?.location?.customName ?: "Unknown"),
                "currentDate" to currentDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                "daysSincePlanting" to daysSincePlanting.toString(),
                "season" to getCurrentSeason(currentDate),
                "historicalData" to getHistoricalDataSummary(farmerProduce)
            )

            val promptTemplate = PromptTemplate(PREDICTION_PROMPT_TEMPLATE)
            val prompt = promptTemplate.create(context)

            val response = chatClient.prompt(prompt).call().content()
            
            // Parse AI response and convert to DTO
            parseAIResponse(response, farmerProduce)
            
        } catch (e: Exception) {
            logger.warn("AI prediction failed for farmer produce ${farmerProduce.id}, falling back to heuristic", e)
            null
        }
    }

    private fun parseAIResponse(response: String?, farmerProduce: FarmerProduce): HarvestPredictionDto {
        // Simple JSON parsing - in production, use a proper JSON parser
        return try {
            // Extract JSON from response (assuming it's wrapped in text)
            val jsonStart = response?.indexOf("{")!!
            val jsonEnd = response?.lastIndexOf("}")?.plus(1)!!
            val jsonResponse = response?.substring(jsonStart, jsonEnd)!!
            
            // For now, create a prediction based on the response content
            // In production, you'd use Jackson or similar to parse JSON
            val predictedDate = extractDateFromResponse(jsonResponse)
                ?: generateHeuristicHarvestDate(farmerProduce)
            val confidence = extractConfidenceFromResponse(jsonResponse) ?: 0.75
            
            HarvestPredictionDto(
                id = farmerProduce.id!!,
                farmerProduceId = farmerProduce.id!!,
                farmerName = farmerProduce.farmer?.userProfile?.fullName ?: "Unknown",
                produceName = farmerProduce.farmProduce?.name ?: "Unknown",
                plantingDate = farmerProduce.plantingDate,
                predictedHarvestDate = predictedDate,
                confidence = confidence,
                predictedSpecies = extractSpeciesFromResponse(jsonResponse),
                estimatedYield = extractYieldFromResponse(jsonResponse),
                yieldUnit = "kg",
                growthStage = extractGrowthStageFromResponse(jsonResponse) ?: "growing",
                status = "PREDICTED",
                createdAt = java.time.LocalDateTime.now(),
                updatedAt = java.time.LocalDateTime.now()
            )
        } catch (e: Exception) {
            logger.error("Failed to parse AI response", e)
            generateHeuristicPrediction(farmerProduce)
        }
    }

    private fun extractDateFromResponse(response: String): LocalDate? {
        return try {
            val dateRegex = """"predictedHarvestDate":\s*"(\d{4}-\d{2}-\d{2})"""".toRegex()
            val matchResult = dateRegex.find(response)
            matchResult?.groupValues?.get(1)?.let { LocalDate.parse(it) }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractConfidenceFromResponse(response: String): Double? {
        return try {
            val confidenceRegex = """"confidence":\s*([0-9.]+)""".toRegex()
            val matchResult = confidenceRegex.find(response)
            matchResult?.groupValues?.get(1)?.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    private fun extractSpeciesFromResponse(response: String): String? {
        return try {
            val speciesRegex = """"predictedSpecies":\s*"([^"]+)"""".toRegex()
            val matchResult = speciesRegex.find(response)
            matchResult?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }

    private fun extractYieldFromResponse(response: String): Double? {
        return try {
            val yieldRegex = """"estimatedYield":\s*([0-9.]+)""".toRegex()
            val matchResult = yieldRegex.find(response)
            matchResult?.groupValues?.get(1)?.toDouble()
        } catch (e: Exception) {
            null
        }
    }

    private fun extractGrowthStageFromResponse(response: String): String? {
        return try {
            val stageRegex = """"growthStage":\s*"([^"]+)"""".toRegex()
            val matchResult = stageRegex.find(response)
            matchResult?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }

    private fun generateHeuristicPrediction(farmerProduce: FarmerProduce): HarvestPredictionDto {
        val predictedDate = generateHeuristicHarvestDate(farmerProduce)
        val confidence = calculateHeuristicConfidence(farmerProduce)
        
        return HarvestPredictionDto(
            id = farmerProduce.id!!,
            farmerProduceId = farmerProduce.id!!,
            farmerName = farmerProduce.farmer?.userProfile?.fullName ?: "Unknown",
            produceName = farmerProduce.farmProduce?.name ?: "Unknown",
            plantingDate = farmerProduce.plantingDate,
            predictedHarvestDate = predictedDate,
            confidence = confidence,
            predictedSpecies = null,
            estimatedYield = estimateHeuristicYield(farmerProduce),
            yieldUnit = "kg",
            growthStage = calculateGrowthStage(farmerProduce),
            status = "PREDICTED",
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        )
    }

    private fun generateHeuristicHarvestDate(farmerProduce: FarmerProduce): LocalDate {
        val plantingDate = farmerProduce.plantingDate ?: LocalDate.now().minusDays(30)
        val cropType = farmerProduce.farmProduce?.name?.lowercase() ?: "unknown"
        
        // Typical growing periods for common crops (in days)
        val growingPeriods = mapOf(
            "tomato" to 75..90,
            "tomatoes" to 75..90,
            "corn" to 90..120,
            "maize" to 90..120,
            "wheat" to 120..150,
            "rice" to 120..150,
            "beans" to 60..90,
            "potatoes" to 70..100,
            "carrots" to 70..80,
            "lettuce" to 45..65,
            "spinach" to 40..50,
            "cabbage" to 70..100,
            "onions" to 90..120,
            "peppers" to 70..85,
            "cucumber" to 50..70,
            "squash" to 50..65
        )
        
        val period = growingPeriods.entries.find { cropType.contains(it.key) }?.value
            ?: (70..100) // Default period
        
        val averageDays = (period.first + period.last) / 2
        return plantingDate.plusDays(averageDays.toLong())
    }

    private fun calculateHeuristicConfidence(farmerProduce: FarmerProduce): Double {
        var confidence = 0.7 // Base confidence
        
        // Increase confidence if we have planting date
        if (farmerProduce.plantingDate != null) {
            confidence += 0.1
        }
        
        // Increase confidence if we know the crop type
        if (!farmerProduce.farmProduce?.name.isNullOrBlank()) {
            confidence += 0.1
        }
        
        // Increase confidence if we have location data
        if (farmerProduce.farmer?.location != null) {
            confidence += 0.05
        }
        
        return min(confidence, 0.95)
    }

    private fun estimateHeuristicYield(farmerProduce: FarmerProduce): Double? {
        val cropType = farmerProduce.farmProduce?.name?.lowercase() ?: return null
        
        // Average yields per plant/area for common crops (kg)
        val averageYields = mapOf(
            "tomato" to 5.0,
            "tomatoes" to 5.0,
            "corn" to 0.5,
            "maize" to 0.5,
            "wheat" to 3.0,
            "rice" to 4.0,
            "beans" to 1.5,
            "potatoes" to 2.0,
            "carrots" to 1.0,
            "lettuce" to 0.5,
            "spinach" to 0.3,
            "cabbage" to 2.5,
            "onions" to 1.2,
            "peppers" to 2.0,
            "cucumber" to 3.0,
            "squash" to 4.0
        )
        
        return averageYields.entries.find { cropType.contains(it.key) }?.value
    }

    private fun calculateGrowthStage(farmerProduce: FarmerProduce): String {
        val plantingDate = farmerProduce.plantingDate ?: return "unknown"
        val daysSincePlanting = ChronoUnit.DAYS.between(plantingDate, LocalDate.now())
        
        return when {
            daysSincePlanting < 14 -> "germination"
            daysSincePlanting < 30 -> "seedling"
            daysSincePlanting < 60 -> "vegetative"
            daysSincePlanting < 90 -> "flowering"
            daysSincePlanting < 120 -> "fruiting"
            else -> "mature"
        }
    }

    private fun getCurrentSeason(date: LocalDate): String {
        return when (date.monthValue) {
            12, 1, 2 -> "Winter"
            3, 4, 5 -> "Spring"
            6, 7, 8 -> "Summer"
            9, 10, 11 -> "Fall"
            else -> "Unknown"
        }
    }

    private fun getHistoricalDataSummary(farmerProduce: FarmerProduce): String {
        // In a real implementation, this would query historical data
        return "Limited historical data available for this crop and location"
    }

    private fun generateUpdatedPrediction(farmerProduce: FarmerProduce, actualHarvestDate: LocalDate): HarvestPredictionDto {
        return HarvestPredictionDto(
            id = farmerProduce.id!!,
            farmerProduceId = farmerProduce.id!!,
            farmerName = farmerProduce.farmer?.userProfile?.fullName ?: "Unknown",
            produceName = farmerProduce.farmProduce?.name ?: "Unknown",
            plantingDate = farmerProduce.plantingDate,
            predictedHarvestDate = actualHarvestDate,
            confidence = 1.0, // 100% confidence since it's actual
            predictedSpecies = null,
            estimatedYield = null,
            yieldUnit = "kg",
            growthStage = "harvested",
            status = "HARVESTED",
            createdAt = java.time.LocalDateTime.now(),
            updatedAt = java.time.LocalDateTime.now()
        )
    }
}