package com.agriconnect.farmersportalapis.buyers.application.services

import com.agriconnect.farmersportalapis.buyers.application.dtos.DashboardDto
import com.agriconnect.farmersportalapis.buyers.application.util.Result

interface IBSDashboardService {
    fun getLiveService(buyerId: String): Result<DashboardDto>
}