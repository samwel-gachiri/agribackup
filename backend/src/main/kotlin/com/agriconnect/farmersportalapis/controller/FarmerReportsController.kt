package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.FarmerReportsService
import com.agriconnect.farmersportalapis.application.util.Result
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/farmers/{farmerId}/reports")
@Tag(name = "Farmer Reports", description = "Analytics and reporting for farmers")
class FarmerReportsController(
    private val farmerReportsService: FarmerReportsService
) {

    @Operation(summary = "Get farmer analytics dashboard data")
    @GetMapping
    fun getFarmerReports(
        @PathVariable farmerId: String,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): Result<FarmerReportsResponseDto> =
        farmerReportsService.getFarmerReports(farmerId, startDate, endDate)

    @Operation(summary = "Get yield trends and harvest accuracy calculations")
    @GetMapping("/analytics")
    fun getFarmerAnalytics(
        @PathVariable farmerId: String,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): Result<FarmerAnalyticsResponseDto> =
        farmerReportsService.getFarmerAnalytics(farmerId, startDate, endDate)

    @Operation(summary = "Get seasonal performance analysis")
    @GetMapping("/seasonal-performance")
    fun getSeasonalPerformance(
        @PathVariable farmerId: String,
        @RequestParam(required = false) years: Int?
    ): Result<SeasonalPerformanceResponseDto> =
        farmerReportsService.getSeasonalPerformance(farmerId, years ?: 3)

    @Operation(summary = "Get chart data for farmer reports")
    @GetMapping("/charts")
    fun getFarmerReportCharts(
        @PathVariable farmerId: String,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): Result<FarmerReportChartsResponseDto> =
        farmerReportsService.getFarmerReportCharts(farmerId, startDate, endDate)

    @Operation(summary = "Export farmer reports as PDF")
    @GetMapping("/export/pdf")
    fun exportReportsPDF(
        @PathVariable farmerId: String,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?,
        @RequestParam(required = false) includeCharts: Boolean?
    ): ResponseEntity<ByteArray> {
        return try {
            val pdfData = farmerReportsService.exportReportsPDF(farmerId, startDate, endDate, includeCharts ?: true)
            
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=farmer-reports-$farmerId.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfData)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    @Operation(summary = "Export farmer reports as CSV")
    @GetMapping("/export/csv")
    fun exportReportsCSV(
        @PathVariable farmerId: String,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): ResponseEntity<ByteArray> {
        return try {
            val csvData = farmerReportsService.exportReportsCSV(farmerId, startDate, endDate)
            
            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=farmer-reports-$farmerId.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData)
        } catch (e: Exception) {
            ResponseEntity.internalServerError().build()
        }
    }

    @Operation(summary = "Get harvest accuracy metrics")
    @GetMapping("/harvest-accuracy")
    fun getHarvestAccuracy(
        @PathVariable farmerId: String,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): Result<HarvestAccuracyResponseDto> =
        farmerReportsService.getHarvestAccuracy(farmerId, startDate, endDate)

    @Operation(summary = "Get crop performance comparison")
    @GetMapping("/crop-performance")
    fun getCropPerformance(
        @PathVariable farmerId: String,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): Result<CropPerformanceResponseDto> =
        farmerReportsService.getCropPerformance(farmerId, startDate, endDate)

    @Operation(summary = "Get farming efficiency metrics")
    @GetMapping("/efficiency")
    fun getFarmingEfficiency(
        @PathVariable farmerId: String,
        @RequestParam(required = false) months: Int?
    ): Result<FarmingEfficiencyResponseDto> =
        farmerReportsService.getFarmingEfficiency(farmerId, months ?: 12)
}