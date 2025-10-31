package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.common.enums.PickupRouteStatus
import com.agriconnect.farmersportalapis.domain.common.enums.PickupStatus
import com.agriconnect.farmersportalapis.domain.common.enums.PickupStopStatus
import com.agriconnect.farmersportalapis.domain.profile.*
import com.agriconnect.farmersportalapis.domain.profile.PickupRoute
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class ZoneSupervisorService(
    private val zoneSupervisorRepository: ZoneSupervisorRepository,
    private val zoneRepository: ZoneRepository,
    private val farmerRepository: FarmerRepository,
    private val relationshipRepository: FarmerExporterRelationshipRepository,
    private val pickupScheduleRepository: PickupScheduleRepository,
    private val produceListingRepository: ProduceListingRepository,
    private val exporterRepository: ExporterRepository,
    private val farmerProduceRepository: FarmerProduceRepository
) {
    private val logger = LoggerFactory.getLogger(ZoneSupervisorService::class.java)

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_ZONE_SUPERVISOR')")
    fun getAssignedZones(userId: String): Result<List<ZoneResponseDto>> {
        return try {
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")

            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }

            val zones = zoneSupervisor.zones.map { zone ->
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
                    farmerCount = relationshipRepository.countFarmersByZoneId(zone.id).toInt(),
                    supervisorIds = zone.supervisors.map { it.id }
                )
            }

            ResultFactory.getSuccessResult(zones, "Assigned zones retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving assigned zones for user {}: {}", userId, e.message, e)
            ResultFactory.getFailResult("Failed to retrieve assigned zones: ${e.message}")
        }
    } 

    // --- Multi-stop Pickup Route Management ---
    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('SCHEDULE_PICKUP')")
    fun createPickupRoute(request: CreatePickupRouteRequestDto, userId: String,
                          pickupRouteRepository: PickupRouteRepository,
                          pickupRouteStopRepository: PickupRouteStopRepository):  Result<BuyerPickupRouteResponseDto> {
        return try {
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult(msg="Zone Supervisor not found")
            if (zoneSupervisor.status != "ACTIVE") return ResultFactory.getFailResult(msg="Zone Supervisor is not active")

            val zone = zoneRepository.findByIdOrNull(request.zoneId)
                ?: return ResultFactory.getFailResult(msg="Zone not found")
            if (!zoneSupervisor.zones.contains(zone)) return ResultFactory.getFailResult(msg="Not assigned to zone")
            val exporter = exporterRepository.findByIdOrNull(request.exporterId)
                ?: return ResultFactory.getFailResult(msg="Exporter not found")

            // Load farmers & verify membership
            val relationships = relationshipRepository.findByZoneId(zone.id)
            val farmersMap = relationships.associateBy { it.farmer.id }
            val farmers = request.farmerIds.mapNotNull { id ->
                if (!farmersMap.containsKey(id)) return ResultFactory.getFailResult("Farmer $id not in zone")
                farmersMap[id]!!.farmer
            }

            val route = PickupRoute(
                id = UUID.randomUUID().toString(),
                exporter = exporter,
                zoneSupervisor = zoneSupervisor,
                zone = zone,
                scheduledDate = request.scheduledDate,
                status = PickupRouteStatus.PLANNED,
//                name = request.routeName ?: "Pickup Route ${LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)}",
                totalDistanceKm = 0.0,
                estimatedDurationMinutes = 0
            )
            val savedRoute = pickupRouteRepository.save(route)

            // Simple ordering nearest-neighbor from zone center
            val ordered = optimizeRoute(farmers, zone)
            val stops = ordered.mapIndexed { idx, farmer ->
                PickupRouteStop(
                    id = UUID.randomUUID().toString(),
                    route = savedRoute,
                    farmer = farmer,
                    sequenceOrder = idx + 1,
                    status = PickupStopStatus.PENDING
                )
            }
            pickupRouteStopRepository.saveAll(stops)
            savedRoute.stops.addAll(stops)

            // metrics
            val totalKm = stops.zipWithNext { a, b ->
                val la = a.farmer.location; val lb = b.farmer.location
                if (la != null && lb != null) calculateDistance(la.latitude, la.longitude, lb.latitude, lb.longitude) else 0.0
            }.sum()
            savedRoute.totalDistanceKm = totalKm
            val avgSpeedKmh = 40.0 // TODO: zone-specific speed
            savedRoute.estimatedDurationMinutes = if (totalKm > 0) ((totalKm / avgSpeedKmh) * 60).toInt() else 0
            pickupRouteRepository.save(savedRoute)

            val response = savedRoute.toResponseDto(stops)
            ResultFactory.getSuccessResult(data=response, "Pickup route created")
        } catch (e: Exception) {
            logger.error("Error creating pickup route", e)
            ResultFactory.getFailResult("Failed to create pickup route: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCHEDULE_PICKUP')")
    fun getPickupRoute(routeId: String, userId: String, pickupRouteRepository: PickupRouteRepository,
                       pickupRouteStopRepository: PickupRouteStopRepository): Result<BuyerPickupRouteResponseDto> {
        return try {
            val route = pickupRouteRepository.findByIdOrNull(routeId)
                ?: return ResultFactory.getFailResult("Route not found")
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")
            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }
            if (route.zoneSupervisor.id != zoneSupervisor.id) return ResultFactory.getFailResult("Forbidden")
            val stops = pickupRouteStopRepository.findByRouteIdOrderBySequenceOrder(route.id)
            logger.info("Retrieved pickup route {} successfully for user {}", routeId, userId)
            ResultFactory.getSuccessResult(route.toResponseDto(stops), "Route retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving pickup route {}: {}", routeId, e.message, e)
            ResultFactory.getFailResult("Failed to fetch route: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('SCHEDULE_PICKUP')")
    fun updateRouteStatus(routeId: String, dto: UpdatePickupRouteStatusDto, userId: String,
                          pickupRouteRepository: PickupRouteRepository): Result<BuyerPickupRouteResponseDto> {
        return try {
            val route = pickupRouteRepository.findByIdOrNull(routeId)
                ?: return ResultFactory.getFailResult("Route not found")
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")
            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }
            if (route.zoneSupervisor.id != zoneSupervisor.id) return ResultFactory.getFailResult("Forbidden")
            route.status = PickupRouteStatus.valueOf(dto.status)
            route.updatedAt = LocalDateTime.now()
            pickupRouteRepository.save(route)
            logger.info("Updated pickup route {} status to {} by user {}", routeId, dto.status, userId)
            val stops = route.stops.sortedBy { it.sequenceOrder }
            ResultFactory.getSuccessResult(route.toResponseDto(stops), "Route status updated")
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to update route status: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('SCHEDULE_PICKUP')")
    fun updateStopStatus(routeId: String, stopId: String, dto: UpdatePickupStopStatusDto, userId: String,
                         pickupRouteRepository: PickupRouteRepository,
                         pickupRouteStopRepository: PickupRouteStopRepository): Result<BuyerPickupRouteResponseDto> {
        return try {
            val route = pickupRouteRepository.findByIdOrNull(routeId)
                ?: return ResultFactory.getFailResult("Route not found")
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")
            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }
            if (route.zoneSupervisor.id != zoneSupervisor.id) return ResultFactory.getFailResult("Forbidden")
            val stop = pickupRouteStopRepository.findByIdOrNull(stopId)
                ?: return ResultFactory.getFailResult("Stop not found")
            if (stop.route.id != route.id) return ResultFactory.getFailResult("Stop not in route")
            stop.status = PickupStopStatus.valueOf(dto.status)
            val now = LocalDateTime.now()
            when (stop.status) {
                PickupStopStatus.ARRIVED -> stop.arrivalTime = now
                PickupStopStatus.COMPLETED -> { stop.completionTime = now; if (stop.arrivalTime == null) stop.arrivalTime = now }
                else -> {}
            }
            stop.notes = dto.notes
            pickupRouteStopRepository.save(stop)
            val stops = pickupRouteStopRepository.findByRouteIdOrderBySequenceOrder(route.id)
            ResultFactory.getSuccessResult(route.toResponseDto(stops), "Stop status updated")
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to update stop: ${e.message}")
        }
    }

    // Order farmers for route using center-based nearest neighbor
    private fun orderFarmersForRoute(farmers: List<Farmer>, zone: Zone): List<Farmer> {
        val unvisited = farmers.toMutableList()
        val ordered = mutableListOf<Farmer>()
        var currentLat = zone.centerLatitude.toDouble()
        var currentLon = zone.centerLongitude.toDouble()
        while (unvisited.isNotEmpty()) {
            val next = unvisited.minByOrNull { f ->
                val loc = f.location ?: return@minByOrNull Double.MAX_VALUE
                calculateDistance(currentLat, currentLon, loc.latitude, loc.longitude)
            } ?: break
            ordered.add(next)
            val nl = next.location
            if (nl != null) { currentLat = nl.latitude; currentLon = nl.longitude }
            unvisited.remove(next)
        }
        return ordered
    }

    // Improved route optimization: nearest neighbor seed + 2-opt refinement
    private fun optimizeRoute(farmers: List<Farmer>, zone: Zone): List<Farmer> {
        if (farmers.size <= 2) return farmers
        val initial = orderFarmersForRoute(farmers, zone)
        val locs = initial.map { it.location }
        val n = initial.size
        val indexOf = initial.withIndex().associate { it.value.id to it.index }
        fun distance(i: Int, j: Int): Double {
            val a = locs[i]; val b = locs[j]
            if (a == null || b == null) return Double.MAX_VALUE
            return calculateDistance(a.latitude, a.longitude, b.latitude, b.longitude)
        }
        val order = initial.toMutableList()
        var improved: Boolean
        var iterations = 0
        do {
            improved = false
            iterations++
            for (i in 0 until n - 2) {
                for (k in i + 1 until n - 1) {
                    val current = distance(i, i + 1) + distance(k, k + 1)
                    val proposed = distance(i, k) + distance(i + 1, k + 1)
                    if (proposed + 0.0001 < current) {
                        order.subList(i + 1, k + 1).reverse()
                        improved = true
                    }
                }
            }
        } while (improved && iterations < 10)
        return order
    }

    private fun PickupRoute.toResponseDto(stops: List<PickupRouteStop>): BuyerPickupRouteResponseDto = BuyerPickupRouteResponseDto(
        routeId = id,
        zoneId = zone.id,
        exporterId = exporter.id,
        zoneSupervisorId = zoneSupervisor.id,
        scheduledDate = scheduledDate,
        status = status.name,
        totalDistanceKm = totalDistanceKm ?: 0.0,
        estimatedDurationMinutes = estimatedDurationMinutes ?: 0,
        stops = stops.map { s ->
                PickupRouteStopDto(
                    stopId = s.id,
                    farmerId = s.farmer.id!!,
                    farmerName = s.farmer.userProfile.fullName,
                    sequenceOrder = s.sequenceOrder,
                    status = s.status.name,
                    arrivalTime = s.arrivalTime,
                    completionTime = s.completionTime,
                    notes = s.notes,
                    latitude = s.farmer.location?.latitude,
                    longitude = s.farmer.location?.longitude
                )
        }
    )

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCHEDULE_PICKUP')")
    fun listPickupRoutes(date: java.time.LocalDate, userId: String,
                         pickupRouteRepository: PickupRouteRepository): Result<List<PickupRouteSummaryDto>> {
        return try {
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")
            val start = date.atStartOfDay()
            val end = date.plusDays(1).atStartOfDay().minusNanos(1)
            val routes = pickupRouteRepository.findByZoneSupervisorIdAndScheduledDateBetween(zoneSupervisor.id!!, start, end)
            val summaries = routes.map { r ->
                PickupRouteSummaryDto(
                    routeId = r.id,
                    zoneId = r.zone.id,
                    scheduledDate = r.scheduledDate,
                    status = r.status.name,
                    stopCount = r.stops.size,
                    totalDistanceKm = r.totalDistanceKm,
                    estimatedDurationMinutes = r.estimatedDurationMinutes
                )
            }.sortedBy { it.scheduledDate }
            ResultFactory.getSuccessResult(summaries)
        } catch (e: Exception) {
            ResultFactory.getFailResult("Failed to list routes: ${e.message}")
        }
    }
   @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('ADD_ZONE_COMMENT')")
    fun updateZoneComment(zoneId: String, request: UpdateZoneCommentDto, userId: String): Result<ZoneResponseDto> {
        return try {
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")

            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }

            val zone = zoneRepository.findByIdOrNull(zoneId)
                ?: return ResultFactory.getFailResult("Zone not found")

            // Check if Zone Supervisor is assigned to this zone
            if (!zoneSupervisor.zones.contains(zone)) {
                return ResultFactory.getFailResult("Zone Supervisor not assigned to this zone")
            }

            zone.comments = request.comments
            val savedZone = zoneRepository.save(zone)
            
            logger.info("Zone comment updated for zone {} by Zone Supervisor {}", zoneId, userId)
            ResultFactory.getSuccessResult(savedZone.toResponseDto(), "Zone comment updated successfully")
        } catch (e: Exception) {
            logger.error("Error updating zone comment: {}", e.message, e)
            ResultFactory.getFailResult("Failed to update zone comment: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('EDIT_FARMER')")
    fun editFarmerDetails(farmerId: String, request: UpdateFarmerRequestDto, userId: String): Result<FarmerResponseDto> {
        return try {
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")

            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }

            val farmer = farmerRepository.findByIdOrNull(farmerId)
                ?: return ResultFactory.getFailResult("Farmer not found")

            // Check if Zone Supervisor manages a zone with this farmer
            val relationship = relationshipRepository.findByFarmerId(farmerId)
                .find { zoneSupervisor.zones.contains(it.zone) }
                ?: return ResultFactory.getFailResult("Zone Supervisor not authorized to edit this farmer")

            // Validate consent
            if (!verifyFarmerConsent(farmer, request.consentToken)) {
                return ResultFactory.getFailResult("Farmer consent not provided or invalid")
            }

            // Update farmer details
            request.farmName?.let { farmer.farmName = it }
            request.farmSize?.let { farmer.farmSize = it }
            request.location?.let { farmer.location = it }

            val savedFarmer = farmerRepository.save(farmer)
            logger.info("Farmer {} details updated by Zone Supervisor {}", farmerId, userId)
            
            ResultFactory.getSuccessResult(savedFarmer.toResponseDto(), "Farmer details updated successfully")
        } catch (e: Exception) {
            logger.error("Error updating farmer details: {}", e.message, e)
            ResultFactory.getFailResult("Failed to update farmer details: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('SCHEDULE_PICKUP')")
    fun schedulePickup(request: SchedulePickupRequestDto, userId: String): Result<PickupScheduleResponseDto> {
        return try {
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")

            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }

            val farmer = farmerRepository.findByIdOrNull(request.farmerId)
                ?: return ResultFactory.getFailResult("Farmer not found")
            
            val exporter = exporterRepository.findByIdOrNull(request.exporterId)
                ?: return ResultFactory.getFailResult("Exporter not found")
            
            val produceListing = produceListingRepository.findByIdOrNull(request.produceListingId)
                ?: return ResultFactory.getFailResult("Produce listing not found")

            // Verify Zone Supervisor manages a zone with this farmer and exporter
            val relationship = relationshipRepository.findByFarmerId(request.farmerId)
                .find { zoneSupervisor.zones.contains(it.zone) && it.exporter.id == request.exporterId }
                ?: return ResultFactory.getFailResult("Zone Supervisor not authorized for this farmer and exporter")

            // Validate location within zone boundaries
            if (!isLocationWithinZone(farmer, relationship.zone!!)) {
                return ResultFactory.getFailResult("Farmer location is outside zone boundaries")
            }

            val schedule = PickupSchedule(
                id = UUID.randomUUID().toString(),
                farmer = farmer,
                exporter = exporter,
                produceListing = produceListing,
                scheduledDate = request.scheduledDate,
                status = PickupStatus.SCHEDULED,
                pickupNotes = request.pickupNotes,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            val savedSchedule = pickupScheduleRepository.save(schedule)
            logger.info("Pickup scheduled for farmer {} by Zone Supervisor {}", request.farmerId, userId)
            
            ResultFactory.getSuccessResult(savedSchedule.toResponseDto(), "Pickup scheduled successfully")
        } catch (e: Exception) {
            logger.error("Error scheduling pickup: {}", e.message, e)
            ResultFactory.getFailResult("Failed to schedule pickup: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_ZONE_SUPERVISOR')")
    fun getFarmersInZones(userId: String): Result<List<FarmerLocationResponseDto>> {
        return try {
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")

            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }

            val farmers = mutableListOf<FarmerLocationResponseDto>()
            
            zoneSupervisor.zones.forEach { zone ->
                val zoneRelationships = relationshipRepository.findByZoneId(zone.id)
                zoneRelationships.forEach { relationship ->
                    val farmer = relationship.farmer
                    farmers.add(
                        FarmerLocationResponseDto(
                            farmerId = farmer.id!!,
                            farmerName = farmer.userProfile.fullName,
                            farmName = farmer.farmName,
                            location = farmer.location,
                            zoneId = zone.id,
                            zoneName = zone.name,
                            lastPickupDate = getLastPickupDate(farmer.id!!)
                        )
                    )
                }
            }

            ResultFactory.getSuccessResult(farmers, "Farmers in zones retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving farmers in zones for user {}: {}", userId, e.message, e)
            ResultFactory.getFailResult("Failed to retrieve farmers: ${e.message}")
        }
    }   
 @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCHEDULE_PICKUP')")
    fun getOptimalPickupRoute(zoneId: String, userId: String): Result<BuyerPickupRouteResponseDto> {
        return try {
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")

            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }

            val zone = zoneRepository.findByIdOrNull(zoneId)
                ?: return ResultFactory.getFailResult("Zone not found")

            if (!zoneSupervisor.zones.contains(zone)) {
                return ResultFactory.getFailResult("Zone Supervisor not assigned to this zone")
            }

            val relationships = relationshipRepository.findByZoneId(zoneId)
            val farmers = relationships.map { it.farmer }.filter { it.location != null }
            if (farmers.isEmpty()) return ResultFactory.getFailResult("No farmers with location in zone")

            val ordered = optimizeRoute(farmers, zone)
            val stops = ordered.mapIndexed { idx, farmer ->
                PickupRouteStopDto(
                    stopId = null,
                    farmerId = farmer.id!!,
                    farmerName = farmer.userProfile.fullName,
                    sequenceOrder = idx + 1,
                    status = PickupStopStatus.PENDING.name,
                    arrivalTime = null,
                    completionTime = null,
                    notes = null,
                    latitude = farmer.location?.latitude,
                    longitude = farmer.location?.longitude
                )
            }
            val totalKm = ordered.zipWithNext { a, b ->
                val la = a.location; val lb = b.location
                if (la != null && lb != null) calculateDistance(la.latitude, la.longitude, lb.latitude, lb.longitude) else 0.0
            }.sum()
            val avgSpeedKmh = 40.0
            val durationMinutes = if (totalKm > 0) ((totalKm / avgSpeedKmh) * 60).toInt() else 0
            val preview = BuyerPickupRouteResponseDto(
                routeId = UUID.randomUUID().toString(),
                zoneId = zone.id,
                exporterId = zone.exporter.id,
                zoneSupervisorId = zoneSupervisor.id,
                scheduledDate = LocalDateTime.now(),
                status = PickupRouteStatus.PLANNED.name,
                totalDistanceKm = totalKm,
                estimatedDurationMinutes = durationMinutes,
                stops = stops
            )
            ResultFactory.getSuccessResult(preview, "Optimal pickup route preview generated")
        } catch (e: Exception) {
            logger.error("Error calculating optimal pickup route: {}", e.message, e)
            ResultFactory.getFailResult("Failed to calculate pickup route: ${e.message}")
        }
    }

    // Helper methods
    private fun verifyFarmerConsent(farmer: Farmer, consentToken: String?): Boolean {
        // TODO: Implement proper consent verification logic
        // This could involve checking a consent token, database flag, or external service
        return consentToken != null && consentToken.isNotBlank()
    }

    private fun isLocationWithinZone(farmer: Farmer, zone: Zone): Boolean {
        // TODO: Implement proper geospatial validation
        // This should check if farmer's location is within the zone's radius
        val farmerLocation = farmer.location ?: return false
        
        // Simple distance calculation (in production, use proper geospatial libraries)
        val distance = calculateDistance(
            farmerLocation.latitude, farmerLocation.longitude,
            zone.centerLatitude.toDouble(), zone.centerLongitude.toDouble()
        )
        
        return distance <= zone.radiusKm.toDouble()
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        // Haversine formula for calculating distance between two points
        val R = 6371.0 // Earth's radius in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    private fun getLastPickupDate(farmerId: String): LocalDateTime? {
        return pickupScheduleRepository.findByFarmerId(farmerId)
            .filter { it.status == PickupStatus.COMPLETED }
            .maxByOrNull { it.scheduledDate }
            ?.scheduledDate
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('SCHEDULE_PICKUP')")
    fun suggestPickups(
        exporterId: String,
        start: java.time.LocalDate,
        end: java.time.LocalDate,
        zoneId: String? = null,
        minConfidence: Double? = 0.4
    ): Result<List<SuggestedPickupDto>> {
        return try {
            val predictions = farmerProduceRepository.findPredictedHarvestsForExporter(exporterId, start, end)
                .filter { it.predictionConfidence == null || (minConfidence == null || it.predictionConfidence!! >= minConfidence) }
                .filter { fp ->
                    if (zoneId == null) return@filter true
                    // ensure farmer is in zone via relationship
                    val rels = relationshipRepository.findByFarmerId(fp.farmer.id!!)
                    rels.any { it.zone?.id == zoneId }
                }

            val suggestions = predictions.map { fp ->
                val yields = fp.yields
                val avgYield = if (yields.isNotEmpty()) yields.map { it.yieldAmount }.average() else null
                val unit = yields.firstOrNull()?.yieldUnit
                SuggestedPickupDto(
                    farmerId = fp.farmer.id!!,
                    farmerName = fp.farmer.userProfile.fullName,
                    produceName = fp.farmProduce.name,
                    predictedHarvestDate = fp.predictedHarvestDate,
                    predictedSpecies = fp.predictedSpecies,
                    confidence = fp.predictionConfidence,
                    expectedQuantity = avgYield,
                    expectedUnit = unit,
                    latitude = fp.farmer.location?.latitude,
                    longitude = fp.farmer.location?.longitude
                )
            }.sortedWith(compareBy(nullsLast(), SuggestedPickupDto::predictedHarvestDate))

            ResultFactory.getSuccessResult(suggestions)
        } catch (e: Exception) {
            logger.error("Error suggesting pickups: {}", e.message, e)
            ResultFactory.getFailResult("Failed to suggest pickups: ${e.message}")
        }
    }

    // (Deprecated preview optimization method removed; real route creation handled in createPickupRoute)
    // Extension functions for DTOs
    private fun Zone.toResponseDto() = ZoneResponseDto(
        id = id,
        name = name,
        produceType = produceType,
        centerLatitude = centerLatitude,
        centerLongitude = centerLongitude,
        radiusKm = radiusKm,
        exporterId = exporter.id,
        creatorId = creator?.id,
        comments = comments,
        farmerCount = relationshipRepository.countFarmersByZoneId(id).toInt(),
        supervisorIds = supervisors.map { it.id }
    )

    private fun Farmer.toResponseDto() = FarmerResponseDto(
        id = id,
        farmName = farmName,
        farmSize = farmSize,
        location = location,
        userId = userProfile.id,
        fullName = userProfile.fullName,
        email = userProfile.email,
        phoneNumber = userProfile.phoneNumber
    )

    private fun PickupSchedule.toResponseDto() = PickupScheduleResponseDto(
        id = id,
        exporterId = exporter.id,
        farmerId = farmer.id,
        produceListingId = produceListing.id,
        scheduledDate = scheduledDate,
        status = status,
        pickupNotes = pickupNotes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}