package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.service.farmer.FarmerActivityService
import com.agriconnect.farmersportalapis.service.supplychain.FarmerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controller for farmer-related API endpoints used by the farmer dashboard
 */
@RestController
@RequestMapping("/farmers-service/api/farmers")
@Tag(name = "Farmer API", description = "APIs for farmer dashboard")
class FarmerApiController(
    private val farmerActivityService: FarmerActivityService,
    private val farmerService: FarmerService,
    private val farmerInsightService: com.agriconnect.farmersportalapis.service.farmer.FarmerInsightService
) {
    
    /**
     * Get AI insight for farmer dashboard
     */
    @Operation(summary = "Get AI Insight", description = "Returns a contextual insight based on crops and market data")
    @GetMapping("/{farmerId}/insight")
    fun getInsight(
        @Parameter(description = "Id of farmer")
        @PathVariable farmerId: String
    ): ResponseEntity<Map<String, String>> {
        val insight = farmerInsightService.getInsight(farmerId)
        return ResponseEntity.ok(mapOf("insight" to insight))
    }
    
    /**
     * Get recent activities for a farmer
     * Used by the farmer dashboard activity feed
     */
    @Operation(
        summary = "Get farmer's recent activities",
        description = "Returns recent activities including production unit registrations, produce additions, and listings",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/{farmerId}/activities")
    fun getActivities(
        @Parameter(description = "Id of farmer")
        @PathVariable farmerId: String,
        @Parameter(description = "Maximum number of activities to return")
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<Map<String, Any?>>> {
        val activities = farmerActivityService.getRecentActivities(farmerId, limit)
        return ResponseEntity.ok(activities.map { it.toResponseMap() })
    }
    
    /**
     * Get farmer profile by ID
     */
    @Operation(
        summary = "Get farmer profile",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/{farmerId}")
    fun getFarmer(
        @Parameter(description = "Id of farmer")
        @PathVariable farmerId: String
    ): ResponseEntity<Any> {
        val result = farmerService.getFarmer(farmerId)
        return if (result.success) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.badRequest().body(result)
        }
    }
    
    /**
     * Get profile completion status for a farmer
     * Used by the dashboard to show onboarding progress
     */
    @Operation(
        summary = "Get farmer's profile completion status",
        description = "Returns percentage completion and next recommended step",
        responses = [ApiResponse(responseCode = "200", description = "OK")]
    )
    @GetMapping("/{farmerId}/profile-completion")
    fun getProfileCompletion(
        @Parameter(description = "Id of farmer")
        @PathVariable farmerId: String
    ): ResponseEntity<Map<String, Any>> {
        val result = farmerService.getFarmer(farmerId)
        
        if (!result.success || result.data == null) {
            return ResponseEntity.ok(mapOf(
                "percentage" to 30,
                "nextStep" to "Complete Your Profile",
                "nextStepDescription" to "Add your personal details"
            ))
        }
        
        val farmer = result.data
        var completed = 0
        val totalFields = 6
        
        // Check profile fields
        // 1. Full Name (from UserProfile)
        if (farmer.userProfile.fullName.isNotBlank()) completed++
        
        // 2. Phone Number (from UserProfile)
        if (farmer.userProfile.phoneNumber?.isNotBlank() == true) completed++
        
        // 3. Farm Name (from Farmer)
        if (farmer.farmName?.isNotBlank() == true) completed++
        
        // 4. Farm Size (from Farmer)
        if (farmer.farmSize != null && farmer.farmSize!! > 0) completed++
        
        // 5. Location (from Farmer - check if set)
        if (farmer.location != null) completed++
        
        // 6. Email (from UserProfile)
        if (farmer.userProfile.email?.isNotBlank() == true) completed++
        
        val percentage = (completed.toDouble() / totalFields * 100).toInt()
        
        // Determine next step
        val nextStep = when {
            farmer.userProfile.fullName.isBlank() -> "Complete Your Profile"
            farmer.userProfile.phoneNumber.isNullOrBlank() -> "Add Phone Number"
            farmer.location == null -> "Add Location"
            farmer.farmName.isNullOrBlank() -> "Add Farm Details"
            else -> "Add Production Unit"
        }
        
        val nextStepDescription = when (nextStep) {
            "Complete Your Profile" -> "Add your personal details"
            "Add Phone Number" -> "Add your contact number"
            "Add Location" -> "Pin your farm's location"
            "Add Farm Details" -> "Set farm name and size"
            else -> "Register your farm land for EUDR compliance"
        }
        
        return ResponseEntity.ok(mapOf(
            "percentage" to percentage,
            "nextStep" to nextStep,
            "nextStepDescription" to nextStepDescription,
            "fieldsCompleted" to completed,
            "totalFields" to totalFields
        ))
    }
}
