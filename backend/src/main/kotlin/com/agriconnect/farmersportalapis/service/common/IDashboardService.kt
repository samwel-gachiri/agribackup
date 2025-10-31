package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.DashboardDto
import com.agriconnect.farmersportalapis.application.util.Result

interface IDashboardService {
    fun getLiveService(farmerId: String): Result<DashboardDto>
}