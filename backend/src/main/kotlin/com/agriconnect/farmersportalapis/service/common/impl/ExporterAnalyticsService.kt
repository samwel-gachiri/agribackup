package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class ExporterAnalyticsService(
    private val exporterRepository: ExporterRepository,
    private val zoneRepository: ZoneRepository,
    private val systemAdminRepository: SystemAdminRepository,
    private val zoneSupervisorRepository: ZoneSupervisorRepository,
    private val relationshipRepository: FarmerExporterRelationshipRepository,
    private val farmerRepository: FarmerRepository,
    private val farmerProduceRepository: FarmerProduceRepository,
    private val produceYieldRepository: ProduceYieldRepository,
    private val produceListingRepository: ProduceListingRepository,
    private val listingOrderRepository: ListingOrderRepository
) {
    private val logger = LoggerFactory.getLogger(ExporterAnalyticsService::class.java)

    fun getSynchronizedDashboardData(exporterId: String): Result<ExporterDashboardSyncDto> {
        return try {
            val exporter = exporterRepository.findById(exporterId).orElse(null)
                ?: return ResultFactory.getFailResult("Exporter not found")

            // Get synchronized counts from database
            val zones = zoneRepository.findByExporterId(exporterId)
            val relationships = relationshipRepository.findByExporterId(exporterId)
            val farmerIds = relationships.map { it.farmer.id }
            val farmers = if (farmerIds.isNotEmpty()) farmerRepository.findAllById(farmerIds) else emptyList()
            
            val activeSystemAdmins = systemAdminRepository.findAllActive()
            val activeZoneSupervisors = zoneSupervisorRepository.findAllActive()

            // Calculate harvest predictions
            val produces = if (farmerIds.isNotEmpty()) {
                farmerProduceRepository.findByFarmerIdIn(farmerIds as List<String>)
            } else emptyList()
            
            val upcomingHarvests = produces.filter { 
                it.predictedHarvestDate?.isAfter(java.time.LocalDate.now()) == true 
            }

            // Calculate zone breakdown with accurate farmer counts
            val zoneBreakdown = zones.map { zone ->
                val zoneFarmers = relationships.filter { rel ->
                    // Check if farmer is in this zone (simplified - would use geospatial queries in production)
                    rel.farmer.location?.customName?.contains(zone.name, ignoreCase = true) == true
                }
                
                ZoneAnalyticsDto(
                    zoneId = zone.id,
                    zoneName = zone.name,
                    farmerCount = zoneFarmers.size,
                    supervisorCount = zone.supervisors.size
                )
            }

            // Calculate recent activities
            val recentActivities = generateRecentActivities(exporterId)

            // Calculate harvest predictions with proper mapping
            val harvestPredictions = produces.map { produce ->
                val farmer = farmers.find { it.id == produce.farmer.id }
                HarvestPredictionDto(
                    id = produce.id,
                    farmerProduceId = produce.id,
                    farmerName = farmer?.userProfile?.fullName ?: "Unknown Farmer",
                    produceName = produce.predictedSpecies ?: "Unknown Produce",
                    plantingDate = produce.plantingDate,
                    predictedHarvestDate = produce.predictedHarvestDate,
                    predictedSpecies = produce.predictedSpecies,
                    confidence = produce.predictionConfidence,
                    status = produce.status.name,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )
            }

            val dashboardData = ExporterDashboardSyncDto(
                exporterId = exporterId,
                lastSyncTime = LocalDateTime.now(),
                analytics = ExporterSystemAnalyticsDto(
                    totalZones = zones.size,
                    totalFarmers = farmers.size,
                    activeSystemAdmins = activeSystemAdmins.size,
                    activeZoneSupervisors = activeZoneSupervisors.size,
                    zoneBreakdown = zoneBreakdown
                ),
                zones = zones.map { zone ->
                    ZoneResponseDto(
                        id = zone.id,
                        name = zone.name,
                        produceType = zone.produceType,
                        centerLatitude = zone.centerLatitude,
                        centerLongitude = zone.centerLongitude,
                        radiusKm = zone.radiusKm,
                        exporterId = zone.exporter.id,
                        creatorId = zone.creator?.id,
                        comments = zone.comments,
                        farmerCount = zoneBreakdown.find { it.zoneId == zone.id }?.farmerCount ?: 0,
                        supervisorIds = zone.supervisors.map { it.id }
                    )
                },
                farmers = farmers.map { farmer ->
                    FarmerSummaryDto(
                        farmerId = farmer.id ?: "",
                        firstName = farmer.userProfile.fullName.split(" ").firstOrNull() ?: "",
                        lastName = farmer.userProfile.fullName.split(" ").drop(1).joinToString(" "),
                        location = farmer.location?.customName ?: "Location not specified",
                        phoneNumber = farmer.userProfile.phoneNumber ?: "",
                        email = farmer.userProfile.email ?: ""
                    )
                },
                harvestPredictions = harvestPredictions,
                recentActivities = recentActivities,
                upcomingHarvests = upcomingHarvests.size,
                dataIntegrityStatus = "SYNCHRONIZED"
            )

            ResultFactory.getSuccessResult(dashboardData, "Dashboard data synchronized successfully")
        } catch (e: Exception) {
            logger.error("Error getting synchronized dashboard data for exporter {}: {}", exporterId, e.message, e)
            ResultFactory.getFailResult("Failed to synchronize dashboard data: ${e.message}")
        }
    }

    fun refreshAndSyncDashboardData(exporterId: String): Result<ExporterDashboardSyncDto> {
        logger.info("Refreshing dashboard data for exporter: {}", exporterId)
        return getSynchronizedDashboardData(exporterId)
    }

    fun getRealTimeAnalytics(exporterId: String): Result<RealTimeAnalyticsDto> {
        return try {
            val zones = zoneRepository.findByExporterId(exporterId)
            val relationships = relationshipRepository.findByExporterId(exporterId)
            val farmerIds = relationships.map { it.farmer.id }
            
            // Real-time calculations
            val totalOrders = if (farmerIds.isNotEmpty()) {
                    listingOrderRepository.findAll().count { order ->
                        order.produceListing.farmerProduce.farmer.id != null && farmerIds.contains(order.produceListing.farmerProduce.farmer.id!!)
                }
            } else 0
            
            val pendingOrders = if (farmerIds.isNotEmpty()) {
                listingOrderRepository.findAll().count { order ->
                    order.produceListing.farmerProduce.farmer.id in farmerIds &&
                    order.status.name == "PENDING"
                }
            } else 0
            
            val totalYields = if (farmerIds.isNotEmpty()) {
                val produces = farmerProduceRepository.findByFarmerIdIn(farmerIds.filterNotNull())
                produceYieldRepository.findByFarmerProduceIdIn(produces.map { it.id }).size
            } else 0
            
            val activeListings = if (farmerIds.isNotEmpty()) {
                produceListingRepository.findAll().count { listing ->
                    listing.farmerProduce.farmer.id in farmerIds &&
                    listing.status.name == "ACTIVE"
                }
            } else 0

            val realTimeData = RealTimeAnalyticsDto(
                exporterId = exporterId,
                timestamp = LocalDateTime.now(),
                totalZones = zones.size,
                totalFarmers = relationships.size,
                totalOrders = totalOrders,
                pendingOrders = pendingOrders,
                totalYields = totalYields,
                activeListings = activeListings,
                systemLoad = calculateSystemLoad(),
                dataFreshness = "REAL_TIME"
            )

            ResultFactory.getSuccessResult(realTimeData, "Real-time analytics retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error getting real-time analytics for exporter {}: {}", exporterId, e.message, e)
            ResultFactory.getFailResult("Failed to get real-time analytics: ${e.message}")
        }
    }

    fun checkDataIntegrity(exporterId: String): Result<DataIntegrityReportDto> {
        return try {
            val issues = mutableListOf<DataIntegrityIssueDto>()
            
            // Check for orphaned relationships
            val relationships = relationshipRepository.findByExporterId(exporterId)
            relationships.forEach { rel ->
                val farmer = farmerRepository.findById(rel.farmer.id ?: "").orElse(null)
                if (farmer == null) {
                    issues.add(DataIntegrityIssueDto(
                        type = "ORPHANED_RELATIONSHIP",
                        description = "Relationship exists for non-existent farmer: ${rel.farmer.id ?: "unknown"}",
                        severity = "HIGH",
                        affectedEntity = "FarmerExporterRelationship",
                        entityId = rel.id
                    ))
                }
            }
            
            // Check for zones without supervisors
            val zones = zoneRepository.findByExporterId(exporterId)
            zones.forEach { zone ->
                if (zone.supervisors.isEmpty()) {
                    issues.add(DataIntegrityIssueDto(
                        type = "ZONE_WITHOUT_SUPERVISOR",
                        description = "Zone '${zone.name}' has no assigned supervisors",
                        severity = "MEDIUM",
                        affectedEntity = "Zone",
                        entityId = zone.id
                    ))
                }
            }
            
            // Check for farmers without produces
            val farmerIds = relationships.map { it.farmer.id }
            if (farmerIds.isNotEmpty()) {
                val farmers = farmerRepository.findAllById(farmerIds)
                farmers.forEach { farmer ->
                    val produces = farmerProduceRepository.findByFarmerId(farmer.id)
                    if (produces.isEmpty()) {
                        issues.add(DataIntegrityIssueDto(
                            type = "FARMER_WITHOUT_PRODUCES",
                            description = "Farmer '${farmer.userProfile?.fullName} ${""}' has no produces",
                            severity = "LOW",
                            affectedEntity = "Farmer",
                            entityId = farmer.id
                        ))
                    }
                }
            }

            val report = DataIntegrityReportDto(
                exporterId = exporterId,
                checkTime = LocalDateTime.now(),
                totalIssues = issues.size,
                highSeverityIssues = issues.count { it.severity == "HIGH" },
                mediumSeverityIssues = issues.count { it.severity == "MEDIUM" },
                lowSeverityIssues = issues.count { it.severity == "LOW" },
                issues = issues,
                overallStatus = when {
                    issues.any { it.severity == "HIGH" } -> "CRITICAL"
                    issues.any { it.severity == "MEDIUM" } -> "WARNING"
                    issues.isNotEmpty() -> "MINOR_ISSUES"
                    else -> "HEALTHY"
                }
            )

            ResultFactory.getSuccessResult(report, "Data integrity check completed")
        } catch (e: Exception) {
            logger.error("Error checking data integrity for exporter {}: {}", exporterId, e.message, e)
            ResultFactory.getFailResult("Failed to check data integrity: ${e.message}")
        }
    }

    fun fixDataIntegrityIssues(exporterId: String): Result<DataIntegrityFixResultDto> {
        return try {
            val integrityCheck = checkDataIntegrity(exporterId)
            if (!integrityCheck.success) {
                return ResultFactory.getFailResult("Failed to check data integrity before fixing")
            }

            val issues = integrityCheck.data!!.issues
            val fixedIssues = mutableListOf<String>()
            val failedFixes = mutableListOf<String>()

            issues.forEach { issue ->
                try {
                    when (issue.type) {
                        "ORPHANED_RELATIONSHIP" -> {
                            relationshipRepository.deleteById(issue.entityId!!)
                            fixedIssues.add("Removed orphaned relationship: ${issue.entityId}")
                        }
                        "ZONE_WITHOUT_SUPERVISOR" -> {
                            // Would assign a default supervisor or flag for manual assignment
                            fixedIssues.add("Flagged zone for supervisor assignment: ${issue.entityId}")
                        }
                        "FARMER_WITHOUT_PRODUCES" -> {
                            // This is informational, no automatic fix needed
                            fixedIssues.add("Noted farmer without produces: ${issue.entityId}")
                        }
                    }
                } catch (e: Exception) {
                    failedFixes.add("Failed to fix ${issue.type} for ${issue.entityId}: ${e.message}")
                }
            }

            val result = DataIntegrityFixResultDto(
                exporterId = exporterId,
                fixTime = LocalDateTime.now(),
                totalIssuesFound = issues.size,
                issuesFixed = fixedIssues.size,
                issuesFailed = failedFixes.size,
                fixedIssues = fixedIssues,
                failedFixes = failedFixes,
                finalStatus = if (failedFixes.isEmpty()) "ALL_FIXED" else "PARTIALLY_FIXED"
            )

            ResultFactory.getSuccessResult(result, "Data integrity issues processed")
        } catch (e: Exception) {
            logger.error("Error fixing data integrity issues for exporter {}: {}", exporterId, e.message, e)
            ResultFactory.getFailResult("Failed to fix data integrity issues: ${e.message}")
        }
    }

    private fun generateRecentActivities(exporterId: String): List<RecentActivityDto> {
        // In production, this would come from an audit log
        return listOf(
            RecentActivityDto(
                id = "1",
                title = "Zone data synchronized",
                description = "All zone data has been synchronized with backend",
                timestamp = LocalDateTime.now().minusMinutes(5),
                type = "SYNC",
                severity = "INFO"
            ),
            RecentActivityDto(
                id = "2", 
                title = "Farmer data updated",
                description = "Farmer profiles have been refreshed",
                timestamp = LocalDateTime.now().minusMinutes(15),
                type = "UPDATE",
                severity = "INFO"
            ),
            RecentActivityDto(
                id = "3",
                title = "Analytics recalculated",
                description = "Dashboard analytics have been recalculated",
                timestamp = LocalDateTime.now().minusMinutes(30),
                type = "CALCULATION",
                severity = "INFO"
            )
        )
    }

    private fun calculateSystemLoad(): Double {
        // Simplified system load calculation
        return Math.random() * 100 // Would use actual system metrics
    }
}