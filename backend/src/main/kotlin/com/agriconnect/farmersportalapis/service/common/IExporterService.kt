package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.application.util.Result
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface IExporterService {
    fun getExporter(exporterId: String): Result<ExporterResponseDto>
    fun updateExporter(exporterId: String, request: UpdateExporterRequestDto): Result<ExporterResponseDto>
    fun verifyExporter(exporterId: String): Result<ExporterResponseDto>
    fun createZone(request: CreateZoneRequestDto): Result<ZoneResponseDto>
    fun addFarmerToZone(farmerId: String, zoneId: String): Result<FarmerInZoneResponseDto>
    fun getExporterZones(exporterId: String): Result<List<ZoneResponseDto>>
    fun getZoneFarmers(zoneId: String): Result<List<FarmerInZoneResponseDto>>
    fun schedulePickup(request: SchedulePickupRequestDto): Result<PickupScheduleResponseDto>
    fun getPickupSchedules(exporterId: String, pageable: Pageable): Result<Page<PickupScheduleResponseDto>>
}
