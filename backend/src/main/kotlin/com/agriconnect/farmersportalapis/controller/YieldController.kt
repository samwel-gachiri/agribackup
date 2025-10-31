package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.YieldRecordingService
import com.agriconnect.farmersportalapis.application.util.Result
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/yields")
@Tag(name = "Yield Recording", description = "CRUD operations for produce yields")
class YieldController(
    private val yieldService: YieldRecordingService
) {
    @Operation(summary = "Record a new yield for a produce")
    @PostMapping("/record")
    fun recordYield(@RequestBody dto: RecordYieldRequestDto): Result<YieldRecordResponseDto> =
        yieldService.recordYield(dto)

    @Operation(summary = "Get all yields for a farmer produce")
    @GetMapping("/farmer-produce/{farmerProduceId}")
    fun getYieldsByProduce(@PathVariable farmerProduceId: String): Result<List<YieldRecordResponseDto>> =
        yieldService.getYieldsByProduce(farmerProduceId)

    @Operation(summary = "Update a yield record")
    @PutMapping("/{yieldId}")
    fun updateYield(
        @PathVariable yieldId: String,
        @RequestBody dto: UpdateYieldRequestDto
    ): Result<YieldRecordResponseDto> =
        yieldService.updateYield(yieldId, dto)

    @Operation(summary = "Delete a yield record")
    @DeleteMapping("/{yieldId}")
    fun deleteYield(@PathVariable yieldId: String): Result<Boolean> =
        yieldService.deleteYield(yieldId)

    @Operation(summary = "Get yield summary for a farmer")
    @GetMapping("/farmer/{farmerId}/summary")
    fun getFarmerYieldSummary(
        @PathVariable farmerId: String,
        @RequestParam(required = false) limit: Int?
    ): Result<FarmerYieldSummaryResponseDto> =
        yieldService.getFarmerYieldSummary(farmerId, limit)

    @Operation(summary = "Get farmer's produces available for yield recording")
    @GetMapping("/farmer/{farmerId}/produces-for-recording")
    fun getFarmerProducesForYieldRecording(@PathVariable farmerId: String): Result<List<FarmerProduceForYieldDto>> =
        yieldService.getFarmerProducesForYieldRecording(farmerId)

    @Operation(summary = "Record yield with enhanced validation and planting date")
    @PostMapping("/record-enhanced")
    fun recordEnhancedYield(@RequestBody dto: EnhancedRecordYieldRequestDto): Result<YieldRecordResponseDto> =
        yieldService.recordEnhancedYield(dto)

    @Operation(summary = "Get yield statistics for a farmer")
    @GetMapping("/farmer/{farmerId}/statistics")
    fun getFarmerYieldStatistics(
        @PathVariable farmerId: String,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): Result<YieldStatisticsResponseDto> =
        yieldService.getFarmerYieldStatistics(farmerId, startDate, endDate)

    @Operation(summary = "Validate yield data before recording")
    @PostMapping("/validate")
    fun validateYieldData(@RequestBody dto: EnhancedRecordYieldRequestDto): Result<YieldValidationResponseDto> =
        yieldService.validateYieldData(dto)

    @Operation(summary = "Get yield trends for a farmer")
    @GetMapping("/farmer/{farmerId}/trends")
    fun getFarmerYieldTrends(
        @PathVariable farmerId: String,
        @RequestParam(required = false) months: Int?
    ): Result<YieldTrendsResponseDto> =
        yieldService.getFarmerYieldTrends(farmerId, months ?: 12)
}
