package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.HarvestPredictionDto
import java.time.LocalDate

interface HarvestPredictionService {
    
    /**
     * Generate AI-powered harvest prediction for a specific farmer produce
     */
    fun predictHarvest(farmerProduceId: String): HarvestPredictionDto
    
    /**
     * Generate predictions for multiple farmer produces in batch
     */
    fun predictHarvestBatch(farmerProduceIds: List<String>): List<HarvestPredictionDto>
    
    /**
     * Update prediction with actual harvest date for machine learning improvement
     */
    fun updatePrediction(farmerProduceId: String, actualHarvestDate: LocalDate): HarvestPredictionDto
}