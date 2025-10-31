package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.ExporterService
import com.agriconnect.farmersportalapis.service.common.impl.ExporterAnalyticsService
import com.agriconnect.farmersportalapis.application.util.Result
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/exporters-service/exporter")
@CrossOrigin(origins = ["http://localhost:3000"])
class ExporterAnalyticsController(
    private val exporterService: ExporterService,
    private val exporterAnalyticsService: ExporterAnalyticsService
) {

    @GetMapping("/analytics")
    fun getExporterAnalytics(@RequestParam exporterId: String): ResponseEntity<Result<ExporterSystemAnalyticsDto>> {
        val result = exporterService.getSystemAnalytics(exporterId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{exporterId}/dashboard/synchronized")
    fun getSynchronizedDashboardData(@PathVariable exporterId: String): ResponseEntity<Result<ExporterDashboardSyncDto>> {
        val result = exporterAnalyticsService.getSynchronizedDashboardData(exporterId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{exporterId}/analytics/real-time")
    fun getRealTimeAnalytics(@PathVariable exporterId: String): ResponseEntity<Result<RealTimeAnalyticsDto>> {
        val result = exporterAnalyticsService.getRealTimeAnalytics(exporterId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{exporterId}/dashboard/refresh")
    fun refreshDashboardData(@PathVariable exporterId: String): ResponseEntity<Result<ExporterDashboardSyncDto>> {
        val result = exporterAnalyticsService.refreshAndSyncDashboardData(exporterId)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/{exporterId}/data-integrity/check")
    fun checkDataIntegrity(@PathVariable exporterId: String): ResponseEntity<Result<DataIntegrityReportDto>> {
        val result = exporterAnalyticsService.checkDataIntegrity(exporterId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/{exporterId}/data-integrity/fix")
    fun fixDataIntegrityIssues(@PathVariable exporterId: String): ResponseEntity<Result<DataIntegrityFixResultDto>> {
        val result = exporterAnalyticsService.fixDataIntegrityIssues(exporterId)
        return ResponseEntity.ok(result)
    }
}