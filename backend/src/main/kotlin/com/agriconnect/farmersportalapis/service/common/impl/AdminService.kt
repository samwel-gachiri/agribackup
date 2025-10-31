package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.impl.ExporterService.Companion.EXPORTER_NOT_FOUND
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.profile.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import org.slf4j.LoggerFactory
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class AdminService(
    private val systemAdminRepository: SystemAdminRepository,
    private val zoneSupervisorRepository: ZoneSupervisorRepository,
    private val zoneRepository: ZoneRepository,
    private val farmerRepository: FarmerRepository,
    private val exporterRepository: ExporterRepository,
    private val relationshipRepository: FarmerExporterRelationshipRepository,
    private val userRepository: UserRepository,
    private val pickupScheduleRepository: PickupScheduleRepository,
    private val produceListingRepository: ProduceListingRepository
)
{
    private val logger = LoggerFactory.getLogger(AdminService::class.java)

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Transactional(readOnly = true)
    fun getUserProfileByRoleAndSub(role: String, sub: String): Result<UserProfile> {
        return try {
            val userProfile = when (role) {
                "EXPORTER" -> {
                    exporterRepository.findByIdOrNull(sub)?.userProfile
                        ?: return ResultFactory.getFailResult("Exporter not found for ID: $sub")
                }
                "SYSTEM_ADMIN" -> {
                    systemAdminRepository.findByIdOrNull(sub)?.userProfile
                        ?: return ResultFactory.getFailResult("System Admin not found for ID: $sub")
                }
                "ZONE_SUPERVISOR" -> {
                    zoneSupervisorRepository.findByIdOrNull(sub)?.takeIf { it.status == "ACTIVE" }?.userProfile
                        ?: return ResultFactory.getFailResult("Zone Supervisor not found or not active for ID: $sub")
                }
                else -> {
                    userRepository.findByIdOrNull(sub)
                        ?: return ResultFactory.getFailResult("User not found for ID: $sub")
                }
            }
            logger.info("Retrieved userId {} for role {} and sub {}", userProfile.id, role, sub)
            ResultFactory.getSuccessResult(userProfile, "User ID retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving userId for role {} and sub {}: {}", role, sub, e.message, e)
            ResultFactory.getFailResult("Failed to retrieve user ID: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('CREATE_ZONE')")
    fun createZone(request: CreateZoneRequestDto, sub: String, role: String): Result<ZoneResponseDto> {
        return try {
            val exporter = exporterRepository.findByIdOrNull(request.exporterId)
                ?: return ResultFactory.getFailResult(EXPORTER_NOT_FOUND)

            // Map userId (sub, role-specific ID) to UserProfile based on role
            val userProfile = getUserProfileByRoleAndSub(role, sub).data

            // If user is an EXPORTER, ensure they are creating a zone for their own exporter ID
            if (role == "EXPORTER" && exporter.id != sub) {
                logger.warn("Exporter user {} attempted to create zone for different exporter {}", sub, request.exporterId)
                return ResultFactory.getFailResult("Exporter can only create zones for their own profile")
            }

            val zone = Zone(
                id = UUID.randomUUID().toString(),
                name = request.name,
                exporter = exporter,
                produceType = request.produceType,
                centerLatitude = request.centerLatitude,
                centerLongitude = request.centerLongitude,
                radiusKm = request.radiusKm,
                creator = userProfile
            )

            val savedZone = zoneRepository.save(zone)
            logger.info("Zone created successfully: id={}, creatorId={}, role={}", savedZone.id, userProfile?.id, role)
            ResultFactory.getSuccessResult(savedZone.toResponseDto(), "Zone created successfully")
        } catch (e: Exception) {
            logger.error("Error creating zone for user {} with role {}: {}", sub, role, e.message, e)
            ResultFactory.getFailResult("Failed to create zone: ${e.message}")
        }
    }

    // --- Newly added zone listing helpers ---
    @Transactional(readOnly = true)
    fun listZonesForUser(userId: String, role: String): Result<List<ZoneResponseDto>> {
        return try {
            val zones = when (role) {
                "EXPORTER" -> zoneRepository.findByExporterId(userId)
                "SYSTEM_ADMIN" -> zoneRepository.findAll().toList()
                "ZONE_SUPERVISOR" -> zoneSupervisorRepository.findByUserProfileId(userId)?.zones ?: emptyList()
                else -> emptyList()
            }
            ResultFactory.getSuccessResult(zones.map { it.toResponseDto() }, "Zones retrieved")
        } catch (e: Exception) {
            logger.error("Error listing zones for user {} role {}: {}", userId, role, e.message, e)
            ResultFactory.getFailResult("Failed to list zones: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    fun listZonesByExporter(exporterId: String, requesterId: String, role: String): Result<List<ZoneResponseDto>> {
        return try {
            if (role == "EXPORTER" && exporterId != requesterId) {
                return ResultFactory.getFailResult("Not authorized to view other exporter zones")
            }
            val zones = zoneRepository.findByExporterId(exporterId)
            ResultFactory.getSuccessResult(zones.map { it.toResponseDto() }, "Exporter zones retrieved")
        } catch (e: Exception) {
            logger.error("Error listing zones for exporter {}: {}", exporterId, e.message, e)
            ResultFactory.getFailResult("Failed to list exporter zones: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    fun listFarmersInZone(zoneId: String, requesterId: String, role: String): Result<List<FarmerInZoneResponseDto>> {
        return try {
            val zone = zoneRepository.findByIdOrNull(zoneId)
                ?: return ResultFactory.getFailResult("Zone not found")

            // Authorization: exporter owning zone, system admin, assigned zone supervisor, or any farmer in zone (to view peers?)
            val authorized = when (role) {
                "SYSTEM_ADMIN" -> true
                "EXPORTER" -> zone.exporter.id == requesterId
                "ZONE_SUPERVISOR" -> {
                    val supervisor = zoneSupervisorRepository.findByUserProfileId(requesterId)
                    supervisor != null && supervisor.zones.contains(zone)
                }
                "FARMER" -> relationshipRepository.findByFarmerId(requesterId).any { it.zone?.id == zoneId }
                else -> false
            }
            if (!authorized) {
                return ResultFactory.getFailResult("Not authorized to view farmers in this zone")
            }

            val relationships = relationshipRepository.findByZoneId(zoneId)
            val dto = relationships.map { it.toFarmerInZoneDto() }
            ResultFactory.getSuccessResult(dto, "Zone farmers retrieved")
        } catch (e: Exception) {
            logger.error("Error listing farmers in zone {}: {}", zoneId, e.message, e)
            ResultFactory.getFailResult("Failed to list farmers in zone: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun deleteZone(zoneId: String, requesterId: String, role: String): Result<String> {
        return try {
            val zone = zoneRepository.findByIdOrNull(zoneId) ?: return ResultFactory.getFailResult("Zone not found")
            if (role == "EXPORTER" && zone.exporter.id != requesterId) {
                return ResultFactory.getFailResult("Not authorized to delete this zone")
            }
            if (role != "SYSTEM_ADMIN" && role != "EXPORTER") {
                return ResultFactory.getFailResult("Not authorized to delete zones")
            }
            zoneRepository.delete(zone)
            ResultFactory.getSuccessResult("OK", "Zone deleted successfully")
        } catch (e: Exception) {
            logger.error("Error deleting zone {}: {}", zoneId, e.message, e)
            ResultFactory.getFailResult("Failed to delete zone: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('ADD_ZONE_COMMENT')")
    fun addZoneComment(zoneId: String, request: UpdateZoneCommentDto, userId: String): Result<ZoneResponseDto> {
        return try {
            val zone = zoneRepository.findByIdOrNull(zoneId)
                ?: return ResultFactory.getFailResult("Zone not found")
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found or not active")

            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }

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
    @PreAuthorize("hasAuthority('ADD_ZONE_SUPERVISOR')")
    fun addZoneSupervisorToZone(zoneId: String, request: AssignZoneSupervisorDto): Result<ZoneResponseDto> {
        return try {
            val zone = zoneRepository.findByIdOrNull(zoneId)
                ?: return ResultFactory.getFailResult("Zone not found")
            val zoneSupervisor = zoneSupervisorRepository.findByIdOrNull(request.zoneSupervisorId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")

            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }

            if (!zoneSupervisor.zones.contains(zone)) {
                zoneSupervisor.zones.add(zone)
                zoneRepository.save(zone)
                logger.info("Zone Supervisor {} assigned to zone {}", request.zoneSupervisorId, zoneId)
            }

            ResultFactory.getSuccessResult(zone.toResponseDto(), "Zone Supervisor assigned successfully")
        } catch (e: Exception) {
            logger.error("Error assigning Zone Supervisor to zone: {}", e.message, e)
            ResultFactory.getFailResult("Failed to assign Zone Supervisor: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('ADD_FARMER')")
    fun addFarmerToZone(zoneId: String, request: AddFarmerToZoneDto): Result<FarmerInZoneResponseDto> {
        return try {
            val farmer = farmerRepository.findByIdOrNull(request.farmerId)
                ?: return ResultFactory.getFailResult("Farmer not found")
            val zone = zoneRepository.findByIdOrNull(zoneId)
                ?: return ResultFactory.getFailResult("Zone not found")

            // Check if farmer is already in this zone
            val existingRelationship = relationshipRepository.findByFarmerIdAndExporterId(farmer.id!!, zone.exporter.id)
            if (existingRelationship != null && existingRelationship.zone?.id == zoneId) {
                return ResultFactory.getFailResult("Farmer already in zone")
            }

            val relationship = FarmerExporterRelationship(
                id = UUID.randomUUID().toString(),
                farmer = farmer,
                exporter = zone.exporter,
                zone = zone,
                createdAt = LocalDateTime.now()
            )

            val savedRelationship = relationshipRepository.save(relationship)
            logger.info("Farmer {} added to zone {}", request.farmerId, zoneId)
            ResultFactory.getSuccessResult(savedRelationship.toFarmerInZoneDto(), "Farmer added to zone successfully")
        } catch (e: Exception) {
            logger.error("Error adding farmer to zone: {}", e.message, e)
            ResultFactory.getFailResult("Failed to add farmer to zone: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('ADD_FARMER')")
    fun addExistingFarmerToZone(zoneId: String, request: AddExistingFarmerLookupDto): Result<FarmerInZoneResponseDto> {
        return try {
            if (request.email.isNullOrBlank() && request.phoneNumber.isNullOrBlank()) {
                return ResultFactory.getFailResult("Provide email or phoneNumber")
            }
            val zone = zoneRepository.findByIdOrNull(zoneId) ?: return ResultFactory.getFailResult("Zone not found")
            val userProfile = when {
                !request.email.isNullOrBlank() -> userRepository.findByEmail(request.email)
                !request.phoneNumber.isNullOrBlank() -> userRepository.findByPhoneNumber(request.phoneNumber)
                else -> null
            } ?: return ResultFactory.getFailResult("Farmer user not found")

            val farmer = farmerRepository.findByUserProfile(userProfile).orElse(null)
                ?: return ResultFactory.getFailResult("Farmer profile not found")

            val existingRelationship = relationshipRepository.findByFarmerIdAndExporterId(farmer.id!!, zone.exporter.id)
            if (existingRelationship != null && existingRelationship.zone?.id == zoneId) {
                return ResultFactory.getFailResult("Farmer already in zone")
            }

            val relationship = FarmerExporterRelationship(
                id = java.util.UUID.randomUUID().toString(),
                farmer = farmer,
                exporter = zone.exporter,
                zone = zone,
                createdAt = java.time.LocalDateTime.now()
            )
            val saved = relationshipRepository.save(relationship)
            ResultFactory.getSuccessResult(saved.toFarmerInZoneDto(), "Farmer added to zone successfully")
        } catch (e: Exception) {
            logger.error("Error adding existing farmer to zone {}: {}", zoneId, e.message, e)
            ResultFactory.getFailResult("Failed to add existing farmer: ${e.message}")
        }
    }

    @PreAuthorize("hasAuthority('VIEW_ZONE_SUPERVISOR')")
    fun getZoneSupervisorDetails(zoneSupervisorId: String): Result<ZoneSupervisorResponseDto> {
        return try {
            val zoneSupervisor = zoneSupervisorRepository.findByIdOrNull(zoneSupervisorId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")
            ResultFactory.getSuccessResult(zoneSupervisor.toResponseDto(), "Zone Supervisor retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving Zone Supervisor details: {}", e.message, e)
            ResultFactory.getFailResult("Failed to retrieve Zone Supervisor: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('EDIT_FARMER')")
    fun editFarmerDetails(farmerId: String, request: UpdateFarmerRequestDto, userId: String): Result<FarmerResponseDto> {
        return try {
            val farmer = farmerRepository.findByIdOrNull(farmerId)
                ?: return ResultFactory.getFailResult("Farmer not found")
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found or not active")
            
            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }

            // Check if Zone Supervisor manages a zone with this farmer
            val relationship = relationshipRepository.findByFarmerId(farmerId)
                .find { zoneSupervisor.zones.contains(it.zone) }
                ?: return ResultFactory.getFailResult("Zone Supervisor not authorized to edit this farmer")

            // Validate consent (assuming consentToken is stored or verified)
            if (!verifyFarmerConsent(farmer, request.consentToken)) {
                return ResultFactory.getFailResult("Farmer consent not provided")
            }

            request.farmName?.let { farmer.farmName = it }
            request.farmSize?.let { farmer.farmSize = it }
            request.location?.let { farmer.location = it }
//            farmer.updatedAt = LocalDateTime.now()

            val savedFarmer = farmerRepository.save(farmer)
            logger.info("Farmer {} details updated by Zone Supervisor {}", farmerId, userId)
            ResultFactory.getSuccessResult(savedFarmer.toResponseDto(), "Farmer details updated successfully")
        } catch (e: Exception) {
            logger.error("Error updating farmer details: {}", e.message, e)
            ResultFactory.getFailResult("Failed to update farmer details: ${e.message}")
        }
    }

    private fun verifyFarmerConsent(farmer: Farmer, consentToken: String?): Boolean {
        // TODO: Implement consent verification logic (e.g., check token or flag in Farmer entity)
        return consentToken != null // Replace with actual logic
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('SCHEDULE_PICKUP')")
    fun schedulePickup(request: SchedulePickupRequestDto, userId: String): Result<PickupScheduleResponseDto> {
        return try {
            val farmer = farmerRepository.findByIdOrNull(request.farmerId)
                ?: return ResultFactory.getFailResult("Farmer not found")
            val exporter = exporterRepository.findByIdOrNull(request.exporterId)
                ?: return ResultFactory.getFailResult("Exporter not found")
            val produceListing = produceListingRepository.findByIdOrNull(request.produceListingId)
                ?: return ResultFactory.getFailResult("Produce listing not found")
            val zoneSupervisor = zoneSupervisorRepository.findByUserProfileId(userId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found or not active")
            
            if (zoneSupervisor.status != "ACTIVE") {
                return ResultFactory.getFailResult("Zone Supervisor is not active")
            }

            // Verify Zone Supervisor manages a zone with this farmer
            val relationship = relationshipRepository.findByFarmerId(request.farmerId)
                .find { zoneSupervisor.zones.contains(it.zone) && it.exporter.id == request.exporterId }
                ?: return ResultFactory.getFailResult("Zone Supervisor not authorized for this farmer and exporter")

            val schedule = PickupSchedule(
                id = UUID.randomUUID().toString(),
                farmer = farmer,
                exporter = exporter,
                produceListing = produceListing,
                scheduledDate = request.scheduledDate,
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


    @PreAuthorize("hasAuthority('VIEW_ZONE_SUPERVISOR') or hasAuthority('ADD_FARMER')")
    fun getZoneDetails(zoneId: String, userId: String, role: String): Result<ZoneResponseDto> {
        logger.info("=== AdminService.getZoneDetails Debug ===")
        logger.info("Requested zoneId: {}", zoneId)
        logger.info("User ID: {}", userId)
        logger.info("User Role: {}", role)

        // Log current security context
        val authentication = SecurityContextHolder.getContext().authentication
        logger.info("Current authentication: {}", authentication)
        logger.info("Authentication authorities: {}", authentication?.authorities)
        logger.info("Authentication principal: {}", authentication?.principal)

        try {
            // Your existing zone retrieval logic here
            val zone = zoneRepository.findById(zoneId)
                .orElseThrow { RuntimeException("Zone not found with id: $zoneId") }

            logger.info("Zone found: {}", zone.id)
            logger.info("Zone exporter ID: {}", zone.exporter?.id)

            // Check authorization logic
            when (role) {
                "EXPORTER" -> {
                    logger.info("Checking EXPORTER authorization...")
                    val exporter = exporterRepository.findById(userId)
                    if (exporter.isPresent) {
                        logger.info("Exporter found with ID: {}", userId)
                        // Check if this exporter owns the zone
                        if (zone.exporter?.id == userId) {
                            logger.info("✓ EXPORTER owns this zone")
                        } else {
                            logger.warn("✗ EXPORTER does not own this zone. Zone exporter: {}, Request user: {}",
                                zone.exporter?.id, userId)
                        }
                    } else {
                        logger.warn("✗ EXPORTER not found with ID: {}", userId)
                    }
                }
                "SYSTEM_ADMIN" -> {
                    logger.info("✓ SYSTEM_ADMIN has access to all zones")
                }
                "FARMER" -> {
                    logger.info("Checking FARMER authorization...")
                    // Check if farmer is in this zone through relationships
                    val farmerInZone = relationshipRepository.findByFarmerId(userId)
                        .any { it.zone?.id == zoneId }
                    if (farmerInZone) {
                        logger.info("✓ FARMER is in this zone")
                    } else {
                        logger.warn("✗ FARMER is not in this zone")
                    }
                }
                else -> {
                    logger.warn("Unknown role: {}", role)
                }
            }

            val result = ResultFactory.getSuccessResult(zone.toResponseDto(), "Zone retrieved successfully")
            logger.info("=== End AdminService.getZoneDetails Debug ===")
            return result

        } catch (e: Exception) {
            logger.error("Error in getZoneDetails: {}", e.message, e)
            logger.info("=== End AdminService.getZoneDetails Debug ===")
            return ResultFactory.getFailResult("Failed to retrieve zone: ${e.message}")
        }
    }

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
        farmerCount = farmerRelationships.size,
        supervisorIds = supervisors.map { it.id }
    )
    private fun FarmerExporterRelationship.toFarmerInZoneDto() = FarmerInZoneResponseDto(
        farmerId = farmer.id,
        farmerName = farmer.userProfile.fullName,
        farmSize = farmer.farmSize,
        farmName = farmer.farmName,
        location = farmer.location,
        joinedAt = createdAt,
        phoneNumber = farmer.userProfile.phoneNumber,
        produces = farmer.farmerProduces.map { fp ->
            FarmerProduceSummaryDto(
                id = fp.id,
                name = fp.farmProduce.name,
                status = fp.status.name
            )
        },
        expectedHarvests = farmer.farmerProduces.map { fp ->
            ExpectedHarvestDto(
                produceId = fp.id,
                predictedHarvestDate = fp.predictedHarvestDate,
                predictedSpecies = fp.predictedSpecies,
                predictionConfidence = fp.predictionConfidence
            )
        }
    )
    private fun ZoneSupervisor.toResponseDto() = ZoneSupervisorResponseDto(
        id = id,
        userId = userProfile.id,
        fullName = userProfile.fullName,
        email = userProfile.email,
        phoneNumber = userProfile.phoneNumber,
        status = status,
        zones = zones.map { it.toResponseDto() },
        createdAt = createdAt,
        updatedAt = updatedAt
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

    // ---- Zone Supervisor Management (Overview & Map Data) ----

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('MANAGE_ZONE_SUPERVISOR') or hasAuthority('VIEW_ZONE_SUPERVISOR')")
    fun listZoneSupervisorOverviews(): Result<List<ZoneSupervisorOverviewDto>> = try {
        val supervisors = zoneSupervisorRepository.findAllActive()
        val mapped = supervisors.mapNotNull { zs ->
            try {
                val up = zs.userProfile // may trigger lazy load; could throw if FK broken
                val farmers = zs.zones.flatMap { it.farmerRelationships }.map { it.farmer }.distinct()
                val earliest = farmers.flatMap { f -> f.farmerProduces.mapNotNull { it.predictedHarvestDate } }.minOrNull()
                ZoneSupervisorOverviewDto(
                    id = zs.id,
                    userId = up.id,
                    fullName = up.fullName,
                    email = up.email,
                    phoneNumber = up.phoneNumber,
                    status = zs.status,
                    assignedZoneIds = zs.zones.map { it.id },
                    assignedZonesCount = zs.zones.size,
                    farmerCount = farmers.size,
                    earliestPredictedHarvestDate = earliest,
                    createdAt = zs.createdAt,
                    updatedAt = zs.updatedAt
                )
            } catch (ex: jakarta.persistence.EntityNotFoundException) {
                logger.warn("Skipping ZoneSupervisor {} due to missing UserProfile reference", zs.id)
                null
            }
        }
        ResultFactory.getSuccessResult(mapped, "Zone Supervisor overviews retrieved (skipped ${supervisors.size - mapped.size} with missing profiles)")
    } catch (e: Exception) {
        logger.error("Error listing zone supervisor overviews: {}", e.message, e)
        ResultFactory.getFailResult("Failed to list zone supervisors: ${e.message}")
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('MANAGE_ZONE_SUPERVISOR') or hasAuthority('VIEW_ZONE_SUPERVISOR')")
    fun getZoneSupervisorMapData(): Result<Map<String, Any>> = try {
        val traceId = UUID.randomUUID().toString()
        logger.info("[MapData][{}] START getZoneSupervisorMapData", traceId)
        logger.info("[MapData][{}][Phase 1] About to fetch zones", traceId)
        val zones = try {
            zoneRepository.findAll().toList()
        } catch (zEx: Exception) {
            logger.error("[MapData][{}][Phase 1] Exception during zoneRepository.findAll: {}", traceId, zEx.message, zEx)
            throw zEx
        }
        logger.info("[MapData][{}][Phase 1] Fetched {} zones", traceId, zones.size)

        // Orphan diagnostics BEFORE accessing any supervisor collections
        try {
            val orphanSupervisorCount = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM zone_supervisors zs LEFT JOIN users u ON zs.user_id = u.id WHERE u.id IS NULL"
            ).singleResult
            val totalSupervisorCount = entityManager.createNativeQuery("SELECT COUNT(*) FROM zone_supervisors").singleResult
            logger.warn("[MapData][{}][Diag] OrphanSupervisorCount={} / TotalSupervisors={}", traceId, orphanSupervisorCount, totalSupervisorCount)
            if ((orphanSupervisorCount as Number).toLong() > 0) {
                @Suppress("UNCHECKED_CAST")
                val sample = entityManager.createNativeQuery(
                    "SELECT zone_supervisor_id,user_id FROM zone_supervisors zs LEFT JOIN users u ON zs.user_id = u.id WHERE u.id IS NULL LIMIT 5"
                ).resultList
                logger.warn("[MapData][{}][Diag] Sample orphan rows (up to 5) = {}", traceId, sample)
            }
        } catch (diagEx: Exception) {
            logger.error("[MapData][{}][Diag] Failed to run orphan diagnostics: {}", traceId, diagEx.message, diagEx)
        }

        logger.info("[MapData][{}][Phase 2] Begin building zone DTOs", traceId)

    val zoneDtos = zones.mapIndexed { zoneIdx, z ->
            logger.info(
                "[MapData][{}][Zone {} of {}] id={}, name='{}', supervisorsSize={}, farmerRelationshipsSize={}",
                traceId, zoneIdx + 1, zones.size, z.id, z.name, z.supervisors.size, z.farmerRelationships.size
            )
            val supervisorIds = mutableListOf<String>()
            var skipped = 0
            z.supervisors.forEachIndexed { supIdx, sup ->
                logger.info(
                    "[MapData][{}][Zone {}][Sup {} of {}] Attempting supervisor entity access (entityHash={})",
                    traceId, z.id, supIdx + 1, z.supervisors.size, System.identityHashCode(sup)
                )
                try {
                    val supId = sup.id // should be safe field access
                    logger.info("[MapData][{}][Zone {}][Sup {}] id resolved={} status={} zonesSize={} (lazy userProfile ahead)", traceId, z.id, supIdx + 1, supId, sup.status, sup.zones.size)
                    // Try touching userProfile lazily to surface potential orphan early
                    try {
                        val up = sup.userProfile
                        logger.debug("[MapData][{}][Zone {}][Sup {}] userProfile id={} name={}", traceId, z.id, supIdx + 1, up.id, up.fullName)
                    } catch (upEx: jakarta.persistence.EntityNotFoundException) {
                        skipped++
                        logger.warn("[MapData][{}][Zone {}][Sup {}] MISSING userProfile (EntityNotFound) for supervisor id={} : {}", traceId, z.id, supIdx + 1, sup.id, upEx.message)
                    } catch (lazyEx: Exception) {
                        logger.error("[MapData][{}][Zone {}][Sup {}] Unexpected exception accessing userProfile for supervisor id={} : {}", traceId, z.id, supIdx + 1, sup.id, lazyEx.message, lazyEx)
                    }
                    supervisorIds.add(supId)
                } catch (ex: jakarta.persistence.EntityNotFoundException) {
                    skipped++
                    logger.warn("[MapData][{}][Zone {}][Sup {}] Orphaned ZoneSupervisor reference: {}", traceId, z.id, supIdx + 1, ex.message)
                } catch (ex: Exception) {
                    logger.error("[MapData][{}][Zone {}][Sup {}] General error reading supervisor: {}", traceId, z.id, supIdx + 1, ex.message, ex)
                }
            }
            if (skipped > 0) {
                logger.warn("[MapData][{}][Zone {}] Skipped {} orphan/broken supervisor references", traceId, z.id, skipped)
            }

            val dto = try {
                ZoneSupervisorMapZoneDto(
                    zoneId = z.id,
                    name = z.name,
                    produceType = z.produceType,
                    centerLatitude = z.centerLatitude.toDouble(),
                    centerLongitude = z.centerLongitude.toDouble(),
                    radiusKm = z.radiusKm.toDouble(),
                    supervisorIds = supervisorIds,
                    supervisorCount = supervisorIds.size,
                    farmerCount = z.farmerRelationships.size
                )
            } catch (convEx: Exception) {
                logger.error("[MapData][{}][Zone {}] Error converting zone fields (lat/long/radius) : {}", traceId, z.id, convEx.message, convEx)
                throw convEx
            }
            logger.info(
                "[MapData][{}][Zone {}] DTO built supervisorCount={} farmerCount={} produceType={}",
                traceId, z.id, dto.supervisorCount, dto.farmerCount, dto.produceType
            )
            dto
        }

    logger.info("[MapData][{}][Phase 3] Building supervisor overviews (calling listZoneSupervisorOverviews)", traceId)
        val supervisorOverResult = listZoneSupervisorOverviews()
        val supervisorOverviews = supervisorOverResult.data ?: emptyList()
        logger.info(
            "[MapData][{}] Supervisor overviews size={} (msg='{}')", traceId, supervisorOverviews.size, supervisorOverResult.msg
        )

        val payload = mapOf(
            "traceId" to traceId,
            "zones" to zoneDtos,
            "supervisors" to supervisorOverviews
        )
    logger.info("[MapData][{}][Phase 4] SUCCESS assembled payload zones={} supervisors={} ", traceId, zoneDtos.size, supervisorOverviews.size)
        ResultFactory.getSuccessResult(payload, "Map data retrieved")
    } catch (e: Exception) {
        // Attempt to surface root cause chain
        val root: Throwable = generateSequence<Throwable>(e) { it.cause }.last()
        logger.error("[MapData] FAILED rootCause={} rootMessage={} fullMessage={}", root::class.qualifiedName, root.message, e.message, e)
        ResultFactory.getFailResult("Failed to get map data: ${root.message ?: e.message}")
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('MANAGE_ZONE_SUPERVISOR')")
    fun unassignZoneSupervisorFromZone(zoneId: String, zoneSupervisorId: String): Result<ZoneResponseDto> = try {
        val zone = zoneRepository.findByIdOrNull(zoneId) ?: return ResultFactory.getFailResult("Zone not found")
        val supervisor = zoneSupervisorRepository.findByIdOrNull(zoneSupervisorId) ?: return ResultFactory.getFailResult("Zone Supervisor not found")
        if (supervisor.zones.contains(zone)) {
            supervisor.zones.remove(zone)
            zone.supervisors.removeIf { it.id == supervisor.id }
        }
        ResultFactory.getSuccessResult(zone.toResponseDto(), "Zone Supervisor unassigned")
    } catch (e: Exception) {
        logger.error("Error unassigning zone supervisor {} from zone {}: {}", zoneSupervisorId, zoneId, e.message, e)
        ResultFactory.getFailResult("Failed to unassign: ${e.message}")
    }
}