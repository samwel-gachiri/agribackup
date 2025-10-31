package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.service.common.S3Service
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.auth.Role
import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.common.enums.ExporterVerificationStatus
import com.agriconnect.farmersportalapis.domain.profile.Exporter
import com.agriconnect.farmersportalapis.domain.profile.SystemAdmin
import com.agriconnect.farmersportalapis.domain.profile.ZoneSupervisor
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ExporterService(
    private val exporterRepository: ExporterRepository,
    private val systemAdminRepository: SystemAdminRepository,
    private val adminRepository: AdminRepository,
    private val zoneSupervisorRepository: ZoneSupervisorRepository,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val permissionRepository: PermissionRepository,
    private val zoneRepository: ZoneRepository,
    private val relationshipRepository: FarmerExporterRelationshipRepository,
    private val passwordEncoder: org.springframework.security.crypto.password.PasswordEncoder,
    private val s3Service: S3Service,
    private val smsService: SmsService,
    private val emailService: EmailService
) {
    private val logger = LoggerFactory.getLogger(ExporterService::class.java)

    companion object {
        const val EXPORTER_NOT_FOUND = "Exporter not found"
        const val DEFAULT_INITIAL_PASSWORD = "12345"
    }

    @Transactional(readOnly = true)
    fun getExporter(exporterId: String): Result<ExporterResponseDto> {
        return try {
            val exporter = exporterRepository.findByIdOrNull(exporterId)
                ?: return ResultFactory.getFailResult(EXPORTER_NOT_FOUND)
            ResultFactory.getSuccessResult(exporter.toResponseDto(), "Exporter retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving exporter {}: {}", exporterId, e.message, e)
            ResultFactory.getFailResult("Failed to retrieve exporter: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('UPDATE_EXPORTER') or #exporterId == principal.username")
    fun updateExporter(exporterId: String, request: UpdateExporterRequestDto): Result<ExporterResponseDto> {
        return try {
            val existingExporter = exporterRepository.findByIdOrNull(exporterId)
                ?: return ResultFactory.getFailResult(EXPORTER_NOT_FOUND)

            request.name?.let { existingExporter.userProfile.fullName = it }
            request.email?.let { existingExporter.userProfile.email = it }
            request.phoneNumber?.let { existingExporter.userProfile.phoneNumber = it }
            existingExporter.userProfile.updatedAt = LocalDateTime.now()

            val updatedExporter = exporterRepository.save(existingExporter)
            logger.info("Exporter {} updated successfully", exporterId)
            ResultFactory.getSuccessResult(updatedExporter.toResponseDto(), "Exporter updated successfully")
        } catch (e: Exception) {
            logger.error("Error updating exporter {}: {}", exporterId, e.message, e)
            ResultFactory.getFailResult("Failed to update exporter: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('VERIFY_EXPORTER')")
    fun verifyExporter(exporterId: String): Result<ExporterResponseDto> {
        return try {
            val exporter = exporterRepository.findByIdOrNull(exporterId)
                ?: return ResultFactory.getFailResult(EXPORTER_NOT_FOUND)

            exporter.verificationStatus = ExporterVerificationStatus.VERIFIED
            exporter.userProfile.updatedAt = LocalDateTime.now()

            val verifiedExporter = exporterRepository.save(exporter)
            logger.info("Exporter {} verified successfully", exporterId)
            ResultFactory.getSuccessResult(verifiedExporter.toResponseDto(), "Exporter verified successfully")
        } catch (e: Exception) {
            logger.error("Error verifying exporter {}: {}", exporterId, e.message, e)
            ResultFactory.getFailResult("Failed to verify exporter: ${e.message}")
        }
    }

    // Superadmin capabilities for Exporters
    
    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_ADMIN')")
    fun createSystemAdmin(request: CreateSystemAdminRequestDto): Result<SystemAdminResponseDto> {
        return try {
            val systemAdminRole = roleRepository.findByName("SYSTEM_ADMIN")
                ?: return ResultFactory.getFailResult("SYSTEM_ADMIN role not found")

            var tempPassword: String? = null
            val existingUser = userRepository.findByEmail(request.email)
            val userProfile = if (existingUser != null) {
                // Ensure role attached
                if (existingUser.roles.none { it.name == systemAdminRole.name }) {
                    existingUser.roles.add(systemAdminRole)
                }
                // Update basic profile info if provided
                existingUser.fullName = request.fullName
                if (request.phoneNumber != null) existingUser.phoneNumber = request.phoneNumber
                // Reactivate the account if previously inactive
                if (!existingUser.isActive) {
                    logger.warn("Reactivating existing user {} while creating System Admin", existingUser.email)
                    existingUser.isActive = true
                }
                // If user has no password set (blank), issue a temporary password
                if (existingUser.passwordHash.isBlank()) {
                    tempPassword = DEFAULT_INITIAL_PASSWORD
                    existingUser.passwordHash = passwordEncoder.encode(tempPassword)
                    logger.info("Issued temp password for existing System Admin user {}", existingUser.email)
                }
                userRepository.save(existingUser)
            } else {
                tempPassword = DEFAULT_INITIAL_PASSWORD
                val newUser = UserProfile(
                    id = java.util.UUID.randomUUID().toString(),
                    email = request.email,
                    passwordHash = passwordEncoder.encode(tempPassword),
                    fullName = request.fullName,
                    phoneNumber = request.phoneNumber,
                    isActive = true
                )
                newUser.roles.add(systemAdminRole)
                userRepository.save(newUser)
            }

            // If a SystemAdmin entity already exists for this user, just return it (idempotent)    
            val existingSystemAdmin = systemAdminRepository.findByUserProfileId(userProfile.id)
            val savedSystemAdmin = if (existingSystemAdmin != null) {
                if (existingSystemAdmin.status != "ACTIVE") {
                    existingSystemAdmin.status = "ACTIVE"
                    systemAdminRepository.save(existingSystemAdmin)
                } else existingSystemAdmin
            } else {
                systemAdminRepository.save(
                    SystemAdmin(
                        id = java.util.UUID.randomUUID().toString(),
                        userProfile = userProfile,
                        status = "ACTIVE"
                    )
                )
            }

            logger.info("System Admin upsert successful: {} (newUser={}, tempPasswordIssued={})", savedSystemAdmin.id, existingUser == null, tempPassword != null)
            ResultFactory.getSuccessResult(savedSystemAdmin.toResponseDto(tempPassword), if (tempPassword != null) "System Admin created successfully (temporary password issued)" else "System Admin linked successfully")
        } catch (e: Exception) {
            logger.error("Error creating System Admin: {}", e.message, e)
            ResultFactory.getFailResult("Failed to create System Admin: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('MANAGE_ZONE_SUPERVISOR')")
    fun createZoneSupervisor(request: CreateZoneSupervisorRequestDto): Result<ZoneSupervisorResponseDto> {
        return try {
            val zoneSupervisorRole = roleRepository.findByName("ZONE_SUPERVISOR")
                ?: return ResultFactory.getFailResult("ZONE_SUPERVISOR role not found")

            var tempPassword: String? = null
            val existingUser = userRepository.findByEmail(request.email)
            val userProfile = if (existingUser != null) {
                if (existingUser.roles.none { it.name == zoneSupervisorRole.name }) {
                    existingUser.roles.add(zoneSupervisorRole)
                }
                existingUser.fullName = request.fullName
                if (request.phoneNumber != null) existingUser.phoneNumber = request.phoneNumber
                // Reactivate the account if previously inactive
                if (!existingUser.isActive) {
                    logger.warn("Reactivating existing user {} while creating Zone Supervisor", existingUser.email)
                    existingUser.isActive = true
                }
                // If user has no password set (blank), issue a temporary password
                if (existingUser.passwordHash.isBlank()) {
                    tempPassword = DEFAULT_INITIAL_PASSWORD
                    existingUser.passwordHash = passwordEncoder.encode(tempPassword)
                    logger.info("Issued temp password for existing Zone Supervisor user {}", existingUser.email)
                }
                userRepository.save(existingUser)
            } else {
                tempPassword = DEFAULT_INITIAL_PASSWORD
                val newUser = UserProfile(
                    id = java.util.UUID.randomUUID().toString(),
                    email = request.email,
                    passwordHash = passwordEncoder.encode(tempPassword),
                    fullName = request.fullName,
                    phoneNumber = request.phoneNumber,
                    isActive = true
                )
                newUser.roles.add(zoneSupervisorRole)
                userRepository.save(newUser)
            }

            val existingZS = zoneSupervisorRepository.findByUserProfileId(userProfile.id)
            val zoneSupervisor = if (existingZS != null) {
                if (existingZS.status != "ACTIVE") {
                    existingZS.status = "ACTIVE"
                    zoneSupervisorRepository.save(existingZS)
                } else existingZS
            } else {
                zoneSupervisorRepository.save(
                    ZoneSupervisor(
                        id = java.util.UUID.randomUUID().toString(),
                        userProfile = userProfile,
                        status = "ACTIVE"
                    )
                )
            }

            // Assign zones if provided (merge new ones)
            request.zoneIds?.let { ids ->
                val zones = zoneRepository.findAllById(ids)
                if (zones.size != ids.size) {
                    val missing = ids.toSet() - zones.map { it.id }.toSet()
                    return ResultFactory.getFailResult("Some zones not found: $missing")
                }
                // Add only zones not already associated
                val existingZoneIds = zoneSupervisor.zones.map { it.id }.toSet()
                zones.filter { it.id !in existingZoneIds }.forEach { zoneSupervisor.zones.add(it) }
            }
            val savedZoneSupervisor = zoneSupervisorRepository.save(zoneSupervisor)
            logger.info("Zone Supervisor upsert successful: {} (newUser={}, tempPasswordIssued={})", savedZoneSupervisor.id, existingUser == null, tempPassword != null)

            ResultFactory.getSuccessResult(savedZoneSupervisor.toResponseDto(tempPassword), if (tempPassword != null) "Zone Supervisor created successfully (temporary password issued)" else "Zone Supervisor linked successfully")
        } catch (e: Exception) {
            logger.error("Error creating Zone Supervisor: {}", e.message, e)
            ResultFactory.getFailResult("Failed to create Zone Supervisor: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_ADMIN')")
    fun deleteSystemAdmin(systemAdminId: String): Result<String> {
        return try {
            val systemAdmin = systemAdminRepository.findByIdOrNull(systemAdminId)
                ?: return ResultFactory.getFailResult("System Admin not found")

            systemAdmin.status = "INACTIVE"
            systemAdminRepository.save(systemAdmin)
            
            logger.info("System Admin {} deactivated successfully", systemAdminId)
            ResultFactory.getSuccessResult("System Admin deactivated successfully", "System Admin deactivated successfully")
        } catch (e: Exception) {
            logger.error("Error deactivating System Admin {}: {}", systemAdminId, e.message, e)
            ResultFactory.getFailResult("Failed to deactivate System Admin: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('MANAGE_ZONE_SUPERVISOR')")
    fun deleteZoneSupervisor(zoneSupervisorId: String): Result<String> {
        return try {
            val zoneSupervisor = zoneSupervisorRepository.findByIdOrNull(zoneSupervisorId)
                ?: return ResultFactory.getFailResult("Zone Supervisor not found")

            zoneSupervisor.status = "INACTIVE"
            zoneSupervisorRepository.save(zoneSupervisor)
            
            logger.info("Zone Supervisor {} deactivated successfully", zoneSupervisorId)
            ResultFactory.getSuccessResult("Zone Supervisor deactivated successfully", "Zone Supervisor deactivated successfully")
        } catch (e: Exception) {
            logger.error("Error deactivating Zone Supervisor {}: {}", zoneSupervisorId, e.message, e)
            ResultFactory.getFailResult("Failed to deactivate Zone Supervisor: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_ADMIN')")
    fun listSystemAdmins(): Result<List<SystemAdminResponseDto>> {
        return try {
            val systemAdmins = systemAdminRepository.findAllActive()
            val responseDtos = systemAdmins.map { it.toResponseDto() }
            ResultFactory.getSuccessResult(responseDtos, "System Admins retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving System Admins: {}", e.message, e)
            ResultFactory.getFailResult("Failed to retrieve System Admins: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_ADMIN')")
    fun getSystemAdminDetails(systemAdminId: String): Result<SystemAdminResponseDto> {
        return try {
            val systemAdmin = systemAdminRepository.findByIdOrNull(systemAdminId)
                ?: return ResultFactory.getFailResult("System Admin not found")
            ResultFactory.getSuccessResult(systemAdmin.toResponseDto(), "System Admin retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving System Admin {}: {}", systemAdminId, e.message, e)
            ResultFactory.getFailResult("Failed to retrieve System Admin: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_ADMIN')")
    fun updateSystemAdmin(systemAdminId: String, request: UpdateSystemAdminRequestDto): Result<SystemAdminResponseDto> {
        return try {
            val systemAdmin = systemAdminRepository.findByIdOrNull(systemAdminId)
                ?: return ResultFactory.getFailResult("System Admin not found")
            val profile = systemAdmin.userProfile
            request.fullName?.let { profile.fullName = it }
            request.email?.let { newEmail ->
                if (newEmail != profile.email) {
                    if (userRepository.findByEmail(newEmail) != null) {
                        return ResultFactory.getFailResult("Email already in use")
                    }
                    profile.email = newEmail
                }
            }
            request.phoneNumber?.let { profile.phoneNumber = it }
            profile.updatedAt = LocalDateTime.now()
            val saved = systemAdminRepository.save(systemAdmin)
            ResultFactory.getSuccessResult(saved.toResponseDto(), "System Admin updated successfully")
        } catch (e: Exception) {
            logger.error("Error updating System Admin {}: {}", systemAdminId, e.message, e)
            ResultFactory.getFailResult("Failed to update System Admin: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_ADMIN')")
    fun reactivateSystemAdmin(systemAdminId: String): Result<SystemAdminResponseDto> {
        return try {
            val systemAdmin = systemAdminRepository.findByIdOrNull(systemAdminId)
                ?: return ResultFactory.getFailResult("System Admin not found")
            if (systemAdmin.status != "ACTIVE") {
                systemAdmin.status = "ACTIVE"
            }
            val saved = systemAdminRepository.save(systemAdmin)
            ResultFactory.getSuccessResult(saved.toResponseDto(), "System Admin reactivated successfully")
        } catch (e: Exception) {
            logger.error("Error reactivating System Admin {}: {}", systemAdminId, e.message, e)
            ResultFactory.getFailResult("Failed to reactivate System Admin: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_ADMIN')")
    fun searchSystemAdmins(query: String): Result<List<SystemAdminResponseDto>> {
        return try {
            val q = query.trim().lowercase()
            if (q.isBlank()) return listSystemAdmins()
            val active = systemAdminRepository.findAllActive()
            val filtered = active.filter { sa ->
                val p = sa.userProfile
                (p.fullName.lowercase().contains(q)) ||
                (p.email?.lowercase()?.contains(q) == true) ||
                (p.phoneNumber?.lowercase()?.contains(q) == true) ||
                sa.id.lowercase().contains(q)
            }
            ResultFactory.getSuccessResult(filtered.map { it.toResponseDto() }, "${filtered.size} System Admin(s) matched search")
        } catch (e: Exception) {
            logger.error("Error searching System Admins '{}': {}", query, e.message, e)
            ResultFactory.getFailResult("Failed to search System Admins: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('MANAGE_ZONE_SUPERVISOR')")
    fun listZoneSupervisors(): Result<List<ZoneSupervisorResponseDto>> {
        return try {
            val zoneSupervisors = zoneSupervisorRepository.findAllActive()
            val responseDtos = zoneSupervisors.map { it.toResponseDto() }
            ResultFactory.getSuccessResult(responseDtos, "Zone Supervisors retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving Zone Supervisors: {}", e.message, e)
            ResultFactory.getFailResult("Failed to retrieve Zone Supervisors: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_ADMIN') or hasAuthority('MANAGE_ZONE_SUPERVISOR')")
    fun updateRolePermissions(request: UpdateRolePermissionsRequestDto): Result<RolePermissionsResponseDto> {
        return try {
            val role = roleRepository.findByIdOrNull(request.roleId)
                ?: return ResultFactory.getFailResult("Role not found")

            val permissions = permissionRepository.findByNameIn(request.permissionNames)
            if (permissions.size != request.permissionNames.size) {
                return ResultFactory.getFailResult("Some permissions not found")
            }

            role.permissions.clear()
            role.permissions.addAll(permissions)
            
            val savedRole = roleRepository.save(role)
            logger.info("Role {} permissions updated successfully", request.roleId)
            
            ResultFactory.getSuccessResult(savedRole.toPermissionsResponseDto(), "Role permissions updated successfully")
        } catch (e: Exception) {
            logger.error("Error updating role permissions: {}", e.message, e)
            ResultFactory.getFailResult("Failed to update role permissions: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_ADMIN') or hasAuthority('MANAGE_ZONE_SUPERVISOR')")
    fun getSystemAnalytics(exporterId: String): Result<ExporterSystemAnalyticsDto> {
        return try {
            val exporter = exporterRepository.findByIdOrNull(exporterId)
                ?: return ResultFactory.getFailResult(EXPORTER_NOT_FOUND)

            val zones = zoneRepository.findByExporterId(exporterId)
            val totalFarmers = relationshipRepository.findByExporterId(exporterId).size
            val activeSystemAdmins = systemAdminRepository.findAllActive().size
            val activeZoneSupervisors = zoneSupervisorRepository.findAllActive().size

            val analytics = ExporterSystemAnalyticsDto(
                totalZones = zones.size,
                totalFarmers = totalFarmers,
                activeSystemAdmins = activeSystemAdmins,
                activeZoneSupervisors = activeZoneSupervisors,
                zoneBreakdown = zones.map { zone ->
                    ZoneAnalyticsDto(
                        zoneId = zone.id,
                        zoneName = zone.name,
                        farmerCount = relationshipRepository.countFarmersByZoneId(zone.id).toInt(),
                        supervisorCount = zone.supervisors.size
                    )
                }
            )

            ResultFactory.getSuccessResult(analytics, "System analytics retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving system analytics: {}", e.message, e)
            ResultFactory.getFailResult("Failed to retrieve system analytics: ${e.message}")
        }
    }

    // Simple harvest prediction aggregation for an exporter (used by exporter dashboards)
    fun listHarvestPredictions(
        exporterId: String,
        farmerProduceRepository: com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerProduceRepository
    ): Result<List<HarvestPredictionDto2>> {
        return try {
            val exporter = exporterRepository.findByIdOrNull(exporterId)
                ?: return ResultFactory.getFailResult("Exporter not found")
            val produces = farmerProduceRepository.findByExporterId(exporter.id)
            val mapped = produces.map { fp ->
                HarvestPredictionDto2(
                    id="",
                    farmerProduceId = fp.id,
                    farmerId = fp.farmer.id ?: "",
                    farmerName = fp.farmer.userProfile.fullName,
                    produceName = fp.farmProduce.name,
                    plantingDate = fp.plantingDate,
                    predictedHarvestDate = fp.predictedHarvestDate,
                    predictedSpecies = fp.predictedSpecies,
                    confidence = fp.predictionConfidence,
                    status = fp.status.name,
                    actualHarvestDate = fp.actualHarvestDate,
                )
            }
            ResultFactory.getSuccessResult(mapped, "Harvest predictions aggregated")
        } catch (e: Exception) {
            logger.error("Error listing harvest predictions for exporter {}", exporterId, e)
            ResultFactory.getFailResult("Failed to list harvest predictions: ${e.message}")
        }
    }

    // generateInitialPassword replaced by fixed DEFAULT_INITIAL_PASSWORD per requirements

    // Extension functions for DTOs
    private fun Exporter.toResponseDto() = ExporterResponseDto(
        id = id,
        companyName = companyName,
        companyDesc = companyDesc,
        licenseId = licenseId,
        verificationStatus = verificationStatus,
        exportLicenseFormUrl = exportLicenseFormUrl
    )

    private fun SystemAdmin.toResponseDto(tempPassword: String? = null) = SystemAdminResponseDto(
        id = id,
        userId = userProfile.id,
        fullName = userProfile.fullName,
        email = userProfile.email,
        phoneNumber = userProfile.phoneNumber,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tempPassword = tempPassword
    )

    private fun ZoneSupervisor.toResponseDto(tempPassword: String? = null) = ZoneSupervisorResponseDto(
        id = id,
        userId = userProfile.id,
        fullName = userProfile.fullName,
        email = userProfile.email,
        phoneNumber = userProfile.phoneNumber,
        status = status,
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
                farmerCount = zone.farmerRelationships.size,
                supervisorIds = zone.supervisors.map { it.id }
            )
        },
        createdAt = createdAt,
        updatedAt = updatedAt,
        tempPassword = tempPassword
    )

    private fun Role.toPermissionsResponseDto() = RolePermissionsResponseDto(
        roleId = id,
        roleName = name,
        permissions = permissions.map { 
            PermissionDto(
                id = it.id,
                name = it.name,
                description = it.description
            )
        }
    )

    private fun getCurrentExporter(): Exporter {
        val authentication = SecurityContextHolder.getContext().authentication
        val exporterId = authentication.name
        return exporterRepository.findByIdOrNull(exporterId) ?: throw IllegalStateException("Exporter not found")
    }

    private fun getActiveAdminsWithContactInfo(): List<Pair<String, String>> {
        // Get all active system admins with phone numbers and emails
        val systemAdmins = adminRepository.findAllAdmins()
        return systemAdmins
            .filter { it.userProfile.phoneNumber != null && it.userProfile.email != null }
            .map { Pair(it.userProfile.phoneNumber!!, it.userProfile.email!!) }
    }

    private fun notifyAdminsOfLicenseReview(exporterName: String, licenseId: String) {
        try {
            val admins = getActiveAdminsWithContactInfo()
            logger.info("Notifying {} admins about license review for exporter: {}", admins.size, exporterName)

            admins.forEach { (phoneNumber, email) ->
                try {
                    // Extract admin name from email (before @) or use "Admin"
                    val adminName = email.substringBefore("@").replace(".", " ").capitalize()
                    // Send email notification
                    emailService.sendLicenseReviewNotificationEmail(email, adminName, exporterName, licenseId)
                    logger.debug("Email notification sent to admin: {}", email)
                } catch (e: Exception) {
                    logger.error("Failed to send email notification to admin {}: {}", email, e.message)
                    // Continue with other notifications even if email fails
                }

                try {
                    // Send SMS notification
                    smsService.sendLicenseReviewNotificationSms(phoneNumber, exporterName, licenseId)
                    logger.debug("SMS notification sent to admin phone: {}", phoneNumber)
                } catch (e: Exception) {
                    logger.error("Failed to send SMS notification to admin {}: {}", phoneNumber, e.message)
                    // Continue with other notifications even if SMS fails
                }
            }
        } catch (e: Exception) {
            logger.error("Error during admin notification process: {}", e.message)
            // Don't throw exception to avoid blocking license submission
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun submitLicense(request: SubmitLicenseRequestDto): Result<ExporterResponseDto> {
        return try {
            // Get current user from security context
            val exporter = getCurrentExporter()

            // Check if license ID already exists (only if licenseId is not null)
            if (request.licenseId != null && exporterRepository.existsByLicenseId(request.licenseId)) {
                return ResultFactory.getFailResult("License ID already registered")
            }

            // Update exporter with license ID and set status to UNDER_REVIEW
            exporter.licenseId = request.licenseId
            exporter.verificationStatus = ExporterVerificationStatus.UNDER_REVIEW
            exporter.userProfile.updatedAt = LocalDateTime.now()

            val savedExporter = exporterRepository.save(exporter)

            logger.info("License ID submitted for exporter {}: {}", exporter.id, request.licenseId)

            // Notify admins about the license review request
            notifyAdminsOfLicenseReview(exporter.userProfile.fullName, request.licenseId ?: "N/A")

            ResultFactory.getSuccessResult(savedExporter.toResponseDto(), "License ID submitted successfully")
        } catch (e: Exception) {
            logger.error("Error submitting license ID: {}", e.message, e)
            ResultFactory.getFailResult("Failed to submit license ID: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun submitLicenseWithDocument(request: SubmitLicenseWithDocumentRequestDto, document: org.springframework.web.multipart.MultipartFile): Result<ExporterResponseDto> {
        return try {
            // Get current user from security context
            val exporter = getCurrentExporter()

            // Check if license ID already exists (only if licenseId is not null)
            if (request.licenseId != null && exporterRepository.existsByLicenseId(request.licenseId)) {
                return ResultFactory.getFailResult("License ID already registered")
            }

            // Validate file
            if (document.isEmpty) {
                return ResultFactory.getFailResult("Document file is required")
            }

            // Check file size (10MB limit)
            val maxSize = 10 * 1024 * 1024 // 10MB in bytes
            if (document.size > maxSize) {
                return ResultFactory.getFailResult("Document file size must be less than 10MB")
            }

            // Check file type
            val allowedTypes = listOf("application/pdf", "image/jpeg", "image/jpg", "image/png")
            if (document.contentType !in allowedTypes) {
                return ResultFactory.getFailResult("Document must be a PDF, JPG, or PNG file")
            }

            // Upload document to S3
            val documentUrl = s3Service.uploadLicenseDocument(document)

            // Update exporter with license ID, document URL and set status to UNDER_REVIEW
            exporter.licenseId = request.licenseId
            exporter.exportLicenseFormUrl = documentUrl
            exporter.verificationStatus = ExporterVerificationStatus.UNDER_REVIEW
            exporter.userProfile.updatedAt = LocalDateTime.now()

            val savedExporter = exporterRepository.save(exporter)

            logger.info("License ID and document submitted for exporter {}: {}, document: {}", exporter.id, request.licenseId, documentUrl)

            // Notify admins about the license review request
            notifyAdminsOfLicenseReview(exporter.userProfile.fullName, request.licenseId ?: "N/A")

            ResultFactory.getSuccessResult(savedExporter.toResponseDto(), "License and document submitted successfully")
        } catch (e: Exception) {
            logger.error("Error submitting license and document: {}", e.message, e)
            ResultFactory.getFailResult("Failed to submit license and document: ${e.message}")
        }
    }
}