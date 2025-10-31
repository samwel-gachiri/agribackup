package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.auth.*
import com.agriconnect.farmersportalapis.application.security.CustomUserDetailsService
import com.agriconnect.farmersportalapis.application.util.JwtUtil
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.buyers.application.services.IBuyerService
import com.agriconnect.farmersportalapis.buyers.domain.common.model.BuyerLocation
import com.agriconnect.farmersportalapis.buyers.domain.profile.Buyer
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.BSProduceRepository
import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.BuyerRepository
import com.agriconnect.farmersportalapis.domain.auth.PasswordResetToken
import com.agriconnect.farmersportalapis.domain.auth.RoleType
import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.common.enums.ExporterVerificationStatus
import com.agriconnect.farmersportalapis.domain.eudr.Aggregator
import com.agriconnect.farmersportalapis.domain.eudr.AggregatorType
import com.agriconnect.farmersportalapis.domain.eudr.Importer
import com.agriconnect.farmersportalapis.domain.eudr.Processor
import com.agriconnect.farmersportalapis.domain.profile.Exporter
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import com.agriconnect.farmersportalapis.service.common.ImporterService
import com.agriconnect.farmersportalapis.service.hedera.HederaAccountService
import com.agriconnect.farmersportalapis.service.hedera.HederaTokenService
import com.agriconnect.farmersportalapis.service.supplychain.AggregatorService
import com.agriconnect.farmersportalapis.service.supplychain.FarmerService
import com.agriconnect.farmersportalapis.service.supplychain.ProcessorService
import jakarta.persistence.EntityNotFoundException
import jakarta.persistence.PersistenceException
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val farmerRepository: FarmerRepository,
    private val buyerRepository: BuyerRepository,
    private val exporterRepository: ExporterRepository,
    private val aggregatorRepository: AggregatorRepository,
    private val processorRepository: ProcessorRepository,
    private val importerRepository: ImporterRepository,
    private val adminRepository: AdminEntityRepository,
    private val systemAdminRepository: SystemAdminRepository,
    private val zoneSupervisorRepository: ZoneSupervisorRepository,
    private val hederaAccountCredentialsRepository: HederaAccountCredentialsRepository,
    private val passwordEncoder: PasswordEncoder,
    private val farmerService: FarmerService,
    private val buyerService: IBuyerService,
    private val aggregatorService: AggregatorService,
    private val processorService: ProcessorService,
    private val importerService: ImporterService,
    private val hederaAccountService: HederaAccountService,
    private val hederaTokenService: HederaTokenService,
    private val BSProduceRepository: BSProduceRepository,
    private val jwtUtil: JwtUtil,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: CustomUserDetailsService,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val emailService: EmailService,
    private val smsService: SmsService
) {
    private val random = SecureRandom()
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    @Transactional(rollbackFor = [Exception::class])
    fun registerFarmer(request: FarmerRegistrationDto): Result<Farmer> {
        return try {
            validateRegistrationRequest(request.user)

            // Handle blank email/phone by setting to null
            val email = request.user.email?.takeIf { it.isNotBlank() }
            val phoneNumber = request.user.phoneNumber?.takeIf { it.isNotBlank() }

            // Check for role-specific duplicates
            if (email != null && userRepository.existsByEmailAndRole(email, RoleType.FARMER.name)) {
                logger.warn("Registration failed: Email {} already registered as Farmer", email)
                return ResultFactory.getFailResult("Email already registered as Farmer")
            }
            if (phoneNumber != null && userRepository.existsByPhoneNumberAndRole(phoneNumber, RoleType.FARMER.name)) {
                logger.warn("Registration failed: Phone number {} already registered as Farmer", phoneNumber)
                return ResultFactory.getFailResult("Phone number already registered as Farmer")
            }

            // Check for existing user by email or phone number
            val existingUser = when {
                email != null -> userRepository.findByEmail(email)
                phoneNumber != null -> userRepository.findByPhoneNumber(phoneNumber)
                else -> null
            }

            val farmerRole = roleRepository.findByName(RoleType.FARMER.name)
                ?: throw EntityNotFoundException("Farmer role not found")

            val user = if (existingUser != null) {
                // Reuse existing user and add Farmer role
                if (!existingUser.roles.contains(farmerRole)) {
                    existingUser.roles.add(farmerRole)
                    userRepository.save(existingUser)
                } else {
                    existingUser
                }
            } else {
                // Create new user
                val newUser = createUser(request.user.copy(email = email)) // Update email to null if blank
                newUser.roles.add(farmerRole)
                userRepository.save(newUser)
            }

            // Create Farmer entity
            val farmer = Farmer(
                id = UUID.randomUUID().toString(),
                userProfile = user,
                farmName = request.farmName,
                farmSize = request.farmSize,
            )

            var savedFarmer = farmerRepository.save(farmer)
            savedFarmer.location = request.location
            savedFarmer.location?.farmer = savedFarmer
            savedFarmer = farmerRepository.save(savedFarmer)

            request.farmerProduces.forEach { produceDto ->
                try {
                    farmerService.addProduceToFarmer(
                        savedFarmer.id!!,
                        produceDto.produceName,
                        produceDto.description,
                        produceDto.farmingType,
                        produceDto.yieldAmount,
                        produceDto.yieldUnit,
                        images = produceDto.images
                    )
                } catch (e: Exception) {
                    logger.error("Failed to add produce for farmer {}: {}", savedFarmer.id, e.message, e)
                    throw PersistenceException("Failed to add produce for farmer: ${e.message}", e)
                }
            }

            logger.info("Farmer registered successfully: id={}, email={}", user.id, user.email)

            // Send welcome email asynchronously if user has email
            if (user.email != null) {
                try {
                    emailService.sendWelcomeEmail(user.email!!, user.fullName ?: "Farmer", "FARMER")
                    logger.info("Welcome email queued for farmer: {}", user.email)
                } catch (e: Exception) {
                    logger.warn("Failed to queue welcome email to {}: {}", user.email, e.message)
                    // Don't fail registration if email queuing fails
                }
            }

            // Send welcome SMS asynchronously if user has phone number
            if (user.phoneNumber != null) {
                try {
                    smsService.sendWelcomeSms(user.phoneNumber!!, user.fullName ?: "Farmer", "FARMER")
                    logger.info("Welcome SMS queued for farmer: {}", user.phoneNumber)
                } catch (e: Exception) {
                    logger.warn("Failed to queue welcome SMS to {}: {}", user.phoneNumber, e.message)
                    // Don't fail registration if SMS queuing fails
                }
            }

            ResultFactory.getSuccessResult(savedFarmer)
        } catch (e: PersistenceException) {
            logger.error("Database error during farmer registration: {}", e.message, e)
            throw e
        } catch (e: EntityNotFoundException) {
            logger.error("Entity not found during farmer registration: {}", e.message, e)
            return ResultFactory.getFailResult("Registration failed: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error during farmer registration: {}", e.message, e)
            return ResultFactory.getFailResult("Failed to register farmer: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun registerBuyer(request: BuyerRegistrationDto): Result<Buyer> {
        return try {
            validateRegistrationRequest(request.user)

            // Handle blank email/phone by setting to null
            val email = request.user.email?.takeIf { it.isNotBlank() }
            val phoneNumber = request.user.phoneNumber?.takeIf { it.isNotBlank() }

            // Check for role-specific duplicates
            if (email != null && userRepository.existsByEmailAndRole(email, RoleType.BUYER.name)) {
                logger.warn("Registration failed: Email {} already registered as Buyer", email)
                return ResultFactory.getFailResult("Email already registered as Buyer")
            }
            if (phoneNumber != null && userRepository.existsByPhoneNumberAndRole(phoneNumber, RoleType.BUYER.name)) {
                logger.warn("Registration failed: Phone number {} already registered as Buyer", phoneNumber)
                return ResultFactory.getFailResult("Phone number already registered as Buyer")
            }

            // Check for existing user by email or phone number
            val existingUser = when {
                email != null -> userRepository.findByEmail(email)
                phoneNumber != null -> userRepository.findByPhoneNumber(phoneNumber)
                else -> null
            }

            val buyerRole = roleRepository.findByName(RoleType.BUYER.name)
                ?: throw EntityNotFoundException("Buyer role not found")

            val user = if (existingUser != null) {
                // Reuse existing user and add Buyer role
                if (!existingUser.roles.contains(buyerRole)) {
                    existingUser.roles.add(buyerRole)
                    userRepository.save(existingUser)
                } else {
                    existingUser
                }
            } else {
                // Create new user
                val newUser = createUser(request.user.copy(email = email)) // Update email to null if blank
                newUser.roles.add(buyerRole)
                userRepository.save(newUser)
            }

            // Create Buyer entity
            val buyer = Buyer(
                id = UUID.randomUUID().toString(),
                userProfile = user,
                companyName = request.companyName,
                businessType = request.businessType,
            )

            var savedBuyer = buyerRepository.save(buyer)

            // Handle location if provided
            if (request.location != null) {
                savedBuyer.location = BuyerLocation(
                    latitude = request.location.latitude,
                    longitude = request.location.longitude,
                    customName = request.location.customName,
                    buyer = savedBuyer
                )
                savedBuyer = buyerRepository.save(savedBuyer)
            }

            // Handle preferred produces if provided
            if (request.preferredProduces.isNotEmpty()) {
                val produceIds = mutableListOf<String>()

                for (preferredProduce in request.preferredProduces) {
                    try {
                        // Check if produce exists by name (case insensitive)
                        val existingProduce = BSProduceRepository.findAll().find {
                            it.name.equals(preferredProduce.name, ignoreCase = true)
                        }

                        val produceId = if (existingProduce != null) {
                            existingProduce.id!!
                        } else {
                            // Create new produce
                            val newProduce = com.agriconnect.farmersportalapis.buyers.domain.common.model.BSFarmProduce(
                                name = preferredProduce.name,
                                description = preferredProduce.name, // Use name as description for now
                                farmingType = "GENERAL", // Default farming type
                                status = com.agriconnect.farmersportalapis.buyers.domain.common.enums.FarmProduceStatus.ACTIVE
                            )
                            BSProduceRepository.saveAndFlush(newProduce).id!!
                        }

                        produceIds.add(produceId)
                    } catch (e: Exception) {
                        logger.error("Failed to process preferred produce '{}' for buyer {}: {}", preferredProduce.name, savedBuyer.id, e.message, e)
                        // Continue with other produces instead of failing the entire registration
                    }
                }

                if (produceIds.isNotEmpty()) {
                    try {
                        buyerService.addProducesToBuyer(
                            com.agriconnect.farmersportalapis.buyers.application.dtos.AddProducesToBuyerDto(
                                buyerId = savedBuyer.id!!,
                                buyerProducesId = produceIds
                            )
                        )
                        logger.info("Added {} preferred produces for buyer: {}", produceIds.size, savedBuyer.id)
                    } catch (e: Exception) {
                        logger.error("Failed to add preferred produces for buyer {}: {}", savedBuyer.id, e.message, e)
                        throw PersistenceException("Failed to add preferred produces for buyer: ${e.message}", e)
                    }
                }
            }

            // Send welcome email asynchronously if user has email
            if (user.email != null) {
                try {
                    emailService.sendWelcomeEmail(user.email!!, user.fullName ?: "Buyer", "BUYER")
                    logger.info("Welcome email queued for buyer: {}", user.email)
                } catch (e: Exception) {
                    logger.warn("Failed to queue welcome email to {}: {}", user.email, e.message)
                    // Don't fail registration if email queuing fails
                }
            }

            // Send welcome SMS asynchronously if user has phone number
            if (user.phoneNumber != null) {
                try {
                    smsService.sendWelcomeSms(user.phoneNumber!!, user.fullName ?: "Buyer", "BUYER")
                    logger.info("Welcome SMS queued for buyer: {}", user.phoneNumber)
                } catch (e: Exception) {
                    logger.warn("Failed to queue welcome SMS to {}: {}", user.phoneNumber, e.message)
                    // Don't fail registration if SMS queuing fails
                }
            }

            ResultFactory.getSuccessResult(savedBuyer)
        } catch (e: PersistenceException) {
            logger.error("Database error during buyer registration: {}", e.message, e)
            throw e
        } catch (e: EntityNotFoundException) {
            logger.error("Entity not found during buyer registration: {}", e.message, e)
            return ResultFactory.getFailResult("Registration failed: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error during buyer registration: {}", e.message, e)
            return ResultFactory.getFailResult("Failed to register buyer: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun registerExporter(request: ExporterRegistrationDto): Result<Exporter> {
        return try {
            validateRegistrationRequest(request.user)

            // Handle blank email/phone by setting to null
            val email = request.user.email?.takeIf { it.isNotBlank() }
            val phoneNumber = request.user.phoneNumber?.takeIf { it.isNotBlank() }

            // Check for role-specific duplicates
            if (email != null && userRepository.existsByEmailAndRole(email, RoleType.EXPORTER.name)) {
                logger.warn("Registration failed: Email {} already registered as Exporter", email)
                return ResultFactory.getFailResult("Email already registered as Exporter")
            }
            if (phoneNumber != null && userRepository.existsByPhoneNumberAndRole(phoneNumber, RoleType.EXPORTER.name)) {
                logger.warn("Registration failed: Phone number {} already registered as Exporter", phoneNumber)
                return ResultFactory.getFailResult("Phone number already registered as Exporter")
            }
            if (request.licenseId != null && exporterRepository.existsByLicenseId(request.licenseId)) {
                logger.warn("Registration failed: License ID {} already registered", request.licenseId)
                return ResultFactory.getFailResult("License ID already registered")
            }

            // Check for existing user by email or phone number
            val existingUser = when {
                email != null -> userRepository.findByEmail(email)
                phoneNumber != null -> userRepository.findByPhoneNumber(phoneNumber)
                else -> null
            }

            val exporterRole = roleRepository.findByName(RoleType.EXPORTER.name)
                ?: throw EntityNotFoundException("Exporter role not found")

            val user = if (existingUser != null) {
                // Reuse existing user and add Exporter role
                if (!existingUser.roles.contains(exporterRole)) {
                    existingUser.roles.add(exporterRole)
                    userRepository.save(existingUser)
                } else {
                    existingUser
                }
            } else {
                // Create new user
                val newUser = createUser(request.user.copy(email = email)) // Update email to null if blank
                newUser.roles.add(exporterRole)
                userRepository.save(newUser)
            }

            // Create Exporter entity
            val exporter = Exporter(
                id = UUID.randomUUID().toString(),
                userProfile = user,
                licenseId = request.licenseId,
                verificationStatus = ExporterVerificationStatus.PENDING,
                companyName = request.companyName,
                companyDesc = request.companyDesc
            )
            val savedExporter = exporterRepository.save(exporter)

            logger.info("Exporter registered successfully: id={}, email={}", user.id, user.email)

            // Send welcome email if user has email
            if (user.email != null) {
                try {
                    emailService.sendWelcomeEmail(user.email!!, user.fullName ?: "Exporter", "EXPORTER")
                } catch (e: Exception) {
                    logger.warn("Failed to send welcome email to {}: {}", user.email, e.message)
                    // Don't fail registration if email sending fails
                }
            }

            // Send welcome SMS asynchronously if user has phone number
            if (user.phoneNumber != null) {
                try {
                    smsService.sendWelcomeSms(user.phoneNumber!!, user.fullName ?: "Exporter", "EXPORTER")
                    logger.info("Welcome SMS queued for exporter: {}", user.phoneNumber)
                } catch (e: Exception) {
                    logger.warn("Failed to queue welcome SMS to {}: {}", user.phoneNumber, e.message)
                    // Don't fail registration if SMS queuing fails
                }
            }

            ResultFactory.getSuccessResult(savedExporter)
        } catch (e: PersistenceException) {
            logger.error("Database error during exporter registration: {}", e.message, e)
            throw e
        } catch (e: EntityNotFoundException) {
            logger.error("Entity not found during exporter registration: {}", e.message, e)
            return ResultFactory.getFailResult("Registration failed: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error during exporter registration: {}", e.message, e)
            return ResultFactory.getFailResult("Failed to register exporter: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun registerAggregator(request: AggregatorRegistrationDto): Result<Aggregator> {
        return try {
            validateRegistrationRequest(request.user)

            // Handle blank email/phone by setting to null
            val email = request.user.email?.takeIf { it.isNotBlank() }
            val phoneNumber = request.user.phoneNumber?.takeIf { it.isNotBlank() }

            // Check for role-specific duplicates
            if (email != null && userRepository.existsByEmailAndRole(email, RoleType.AGGREGATOR.name)) {
                logger.warn("Registration failed: Email {} already registered as Aggregator", email)
                return ResultFactory.getFailResult("Email already registered as Aggregator")
            }
            if (phoneNumber != null && userRepository.existsByPhoneNumberAndRole(phoneNumber, RoleType.AGGREGATOR.name)) {
                logger.warn("Registration failed: Phone number {} already registered as Aggregator", phoneNumber)
                return ResultFactory.getFailResult("Phone number already registered as Aggregator")
            }

            // Check for existing user by email or phone number
            val existingUser = when {
                email != null -> userRepository.findByEmail(email)
                phoneNumber != null -> userRepository.findByPhoneNumber(phoneNumber)
                else -> null
            }

            val aggregatorRole = roleRepository.findByName(RoleType.AGGREGATOR.name)
                ?: throw EntityNotFoundException("Aggregator role not found")

            val user = if (existingUser != null) {
                // Reuse existing user and add Aggregator role
                if (!existingUser.roles.contains(aggregatorRole)) {
                    existingUser.roles.add(aggregatorRole)
                    userRepository.save(existingUser)
                } else {
                    existingUser
                }
            } else {
                // Create new user
                val newUser = createUser(request.user.copy(email = email))
                newUser.roles.add(aggregatorRole)
                userRepository.save(newUser)
            }

            // Create Aggregator entity WITHOUT Hedera account initially
            val aggregator = Aggregator(
                organizationName = request.organizationName,
                organizationType = request.organizationType?.let { 
                    try { AggregatorType.valueOf(it.uppercase()) } 
                    catch (e: Exception) { AggregatorType.COOPERATIVE }
                } ?: AggregatorType.COOPERATIVE,
                registrationNumber = request.registrationNumber,
                facilityAddress = request.facilityAddress ?: "",
                storageCapacityTons = request.storageCapacityTons?.let { BigDecimal.valueOf(it) },
                collectionRadiusKm = request.collectionRadiusKm?.let { BigDecimal.valueOf(it) },
                primaryCommodities = request.primaryCommodities?.joinToString(","),
                certificationDetails = request.certificationDetails,
                hederaAccountId = null, // Will be set asynchronously
                userProfile = user
            )

            val savedAggregator = aggregatorRepository.save(aggregator)

            // Create Hedera account asynchronously (non-blocking)
            // This prevents registration from failing if Hedera network is slow or unavailable
            try {
                createHederaAccountAsync(
                    userId = user.id!!,
                    aggregatorId = savedAggregator.id!!,
                    organizationName = request.organizationName
                )
            } catch (e: Exception) {
                logger.warn("Failed to initiate async Hedera account creation for aggregator: {}", e.message)
                // Registration still succeeds even if Hedera account creation fails
                // Admin can manually create/associate Hedera account later
            }

            logger.info("Aggregator registered successfully: id={}, email={}", user.id, user.email)

            // Send welcome email if user has email
            if (user.email != null) {
                try {
                    emailService.sendWelcomeEmail(user.email!!, user.fullName ?: "Aggregator", "AGGREGATOR")
                } catch (e: Exception) {
                    logger.warn("Failed to send welcome email to {}: {}", user.email, e.message)
                }
            }

            // Send welcome SMS if user has phone number
            if (user.phoneNumber != null) {
                try {
                    smsService.sendWelcomeSms(user.phoneNumber!!, user.fullName ?: "Aggregator", "AGGREGATOR")
                    logger.info("Welcome SMS queued for aggregator: {}", user.phoneNumber)
                } catch (e: Exception) {
                    logger.warn("Failed to queue welcome SMS to {}: {}", user.phoneNumber, e.message)
                }
            }

            ResultFactory.getSuccessResult(savedAggregator)
        } catch (e: PersistenceException) {
            logger.error("Database error during aggregator registration: {}", e.message, e)
            throw e
        } catch (e: EntityNotFoundException) {
            logger.error("Entity not found during aggregator registration: {}", e.message, e)
            return ResultFactory.getFailResult("Registration failed: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error during aggregator registration: {}", e.message, e)
            return ResultFactory.getFailResult("Failed to register aggregator: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun registerProcessor(request: ProcessorRegistrationDto): Result<Processor> {
        return try {
            validateRegistrationRequest(request.user)

            // Handle blank email/phone by setting to null
            val email = request.user.email?.takeIf { it.isNotBlank() }
            val phoneNumber = request.user.phoneNumber?.takeIf { it.isNotBlank() }

            // Check for role-specific duplicates
            if (email != null && userRepository.existsByEmailAndRole(email, RoleType.PROCESSOR.name)) {
                logger.warn("Registration failed: Email {} already registered as Processor", email)
                return ResultFactory.getFailResult("Email already registered as Processor")
            }
            if (phoneNumber != null && userRepository.existsByPhoneNumberAndRole(phoneNumber, RoleType.PROCESSOR.name)) {
                logger.warn("Registration failed: Phone number {} already registered as Processor", phoneNumber)
                return ResultFactory.getFailResult("Phone number already registered as Processor")
            }

            // Check for existing user by email or phone number
            val existingUser = when {
                email != null -> userRepository.findByEmail(email)
                phoneNumber != null -> userRepository.findByPhoneNumber(phoneNumber)
                else -> null
            }

            val processorRole = roleRepository.findByName(RoleType.PROCESSOR.name)
                ?: throw EntityNotFoundException("Processor role not found")

            val user = if (existingUser != null) {
                // Reuse existing user and add Processor role
                if (!existingUser.roles.contains(processorRole)) {
                    existingUser.roles.add(processorRole)
                    userRepository.save(existingUser)
                } else {
                    existingUser
                }
            } else {
                // Create new user
                val newUser = createUser(request.user.copy(email = email))
                newUser.roles.add(processorRole)
                userRepository.save(newUser)
            }

            // Create Hedera account for the processor
            val hederaAccountResult = try {
                hederaAccountService.createHederaAccount(
                    initialBalance = com.hedera.hashgraph.sdk.Hbar.from(10),
                    memo = "AgriBackup Processor: ${request.facilityName}"
                )
            } catch (e: Exception) {
                logger.warn("Failed to create Hedera account for processor: {}", e.message)
                null
            }

            // Create Processor entity directly
            val processor = Processor(
                facilityName = request.facilityName,
                facilityAddress = request.facilityAddress ?: "",
                processingCapabilities = request.processingCapabilities,
                certificationDetails = request.certifications,
                hederaAccountId = hederaAccountResult?.accountId,
                userProfile = user
            )

            val savedProcessor = processorRepository.save(processor)

            // Store Hedera credentials if account was created
            if (hederaAccountResult != null) {
                val credentials = com.agriconnect.farmersportalapis.domain.hedera.HederaAccountCredentials(
                    userId = user.id!!,
                    entityType = "PROCESSOR",
                    entityId = savedProcessor.id,
                    hederaAccountId = hederaAccountResult.accountId,
                    publicKey = hederaAccountResult.publicKey,
                    encryptedPrivateKey = hederaAccountResult.encryptedPrivateKey,
                    initialBalanceHbar = "10",
                    accountMemo = "AgriBackup Processor: ${request.facilityName}",
                    creationTransactionId = hederaAccountResult.transactionId
                )
                hederaAccountCredentialsRepository.save(credentials)

                // Associate with EUDR Compliance Certificate NFT
                try {
                    val eudrCertificateNftId = hederaTokenService.getEudrComplianceCertificateNftId()
                    if (eudrCertificateNftId != null) {
                        hederaAccountService.associateTokenWithAccount(
                            hederaAccountResult.accountId,
                            hederaAccountResult.encryptedPrivateKey,
                            eudrCertificateNftId
                        )
                        credentials.tokensAssociated = """["${eudrCertificateNftId}"]"""
                        hederaAccountCredentialsRepository.save(credentials)
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to associate EUDR Certificate NFT with processor account: {}", e.message)
                }
            }

            logger.info("Processor registered successfully: id={}, email={}", user.id, user.email)

            // Send welcome email if user has email
            if (user.email != null) {
                try {
                    emailService.sendWelcomeEmail(user.email!!, user.fullName ?: "Processor", "PROCESSOR")
                } catch (e: Exception) {
                    logger.warn("Failed to send welcome email to {}: {}", user.email, e.message)
                }
            }

            // Send welcome SMS if user has phone number
            if (user.phoneNumber != null) {
                try {
                    smsService.sendWelcomeSms(user.phoneNumber!!, user.fullName ?: "Processor", "PROCESSOR")
                    logger.info("Welcome SMS queued for processor: {}", user.phoneNumber)
                } catch (e: Exception) {
                    logger.warn("Failed to queue welcome SMS to {}: {}", user.phoneNumber, e.message)
                }
            }

            ResultFactory.getSuccessResult(savedProcessor)
        } catch (e: PersistenceException) {
            logger.error("Database error during processor registration: {}", e.message, e)
            throw e
        } catch (e: EntityNotFoundException) {
            logger.error("Entity not found during processor registration: {}", e.message, e)
            return ResultFactory.getFailResult("Registration failed: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error during processor registration: {}", e.message, e)
            return ResultFactory.getFailResult("Failed to register processor: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun registerImporter(request: ImporterRegistrationDto): Result<Importer> {
        return try {
            validateRegistrationRequest(request.user)

            // Handle blank email/phone by setting to null
            val email = request.user.email?.takeIf { it.isNotBlank() }
            val phoneNumber = request.user.phoneNumber?.takeIf { it.isNotBlank() }

            // Check for role-specific duplicates
            if (email != null && userRepository.existsByEmailAndRole(email, RoleType.IMPORTER.name)) {
                logger.warn("Registration failed: Email {} already registered as Importer", email)
                return ResultFactory.getFailResult("Email already registered as Importer")
            }
            if (phoneNumber != null && userRepository.existsByPhoneNumberAndRole(phoneNumber, RoleType.IMPORTER.name)) {
                logger.warn("Registration failed: Phone number {} already registered as Importer", phoneNumber)
                return ResultFactory.getFailResult("Phone number already registered as Importer")
            }

            // Check for existing user by email or phone number
            val existingUser = when {
                email != null -> userRepository.findByEmail(email)
                phoneNumber != null -> userRepository.findByPhoneNumber(phoneNumber)
                else -> null
            }

            val importerRole = roleRepository.findByName(RoleType.IMPORTER.name)
                ?: throw EntityNotFoundException("Importer role not found")

            val user = if (existingUser != null) {
                // Reuse existing user and add Importer role
                if (!existingUser.roles.contains(importerRole)) {
                    existingUser.roles.add(importerRole)
                    userRepository.save(existingUser)
                } else {
                    existingUser
                }
            } else {
                // Create new user
                val newUser = createUser(request.user.copy(email = email))
                newUser.roles.add(importerRole)
                userRepository.save(newUser)
            }

            // Create Hedera account for the importer
            val hederaAccountResult = try {
                hederaAccountService.createHederaAccount(
                    initialBalance = com.hedera.hashgraph.sdk.Hbar.from(10),
                    memo = "AgriBackup Importer: ${request.companyName}"
                )
            } catch (e: Exception) {
                logger.warn("Failed to create Hedera account for importer: {}", e.message)
                null
            }

            // Create Importer entity directly
            val importer = Importer(
                companyName = request.companyName,
                importLicenseNumber = request.importLicenseNumber ?: "PENDING",
                companyAddress = request.companyAddress ?: "",
                destinationCountry = request.destinationCountry ?: "Unknown",
                destinationPort = request.destinationPort,
                importCategories = request.importCategories?.joinToString(","),
                eudrComplianceOfficer = request.eudrComplianceOfficer,
                certificationDetails = request.certificationDetails,
                hederaAccountId = hederaAccountResult?.accountId,
                userProfile = user
            )

            val savedImporter = importerRepository.save(importer)

            // Store Hedera credentials if account was created
            if (hederaAccountResult != null) {
                val credentials = com.agriconnect.farmersportalapis.domain.hedera.HederaAccountCredentials(
                    userId = user.id!!,
                    entityType = "IMPORTER",
                    entityId = savedImporter.id,
                    hederaAccountId = hederaAccountResult.accountId,
                    publicKey = hederaAccountResult.publicKey,
                    encryptedPrivateKey = hederaAccountResult.encryptedPrivateKey,
                    initialBalanceHbar = "10",
                    accountMemo = "AgriBackup Importer: ${request.companyName}",
                    creationTransactionId = hederaAccountResult.transactionId
                )
                hederaAccountCredentialsRepository.save(credentials)

                // Associate with EUDR Compliance Certificate NFT
                try {
                    val eudrCertificateNftId = hederaTokenService.getEudrComplianceCertificateNftId()
                    if (eudrCertificateNftId != null) {
                        hederaAccountService.associateTokenWithAccount(
                            hederaAccountResult.accountId,
                            hederaAccountResult.encryptedPrivateKey,
                            eudrCertificateNftId
                        )
                        credentials.tokensAssociated = """["${eudrCertificateNftId}"]"""
                        hederaAccountCredentialsRepository.save(credentials)
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to associate EUDR Certificate NFT with importer account: {}", e.message)
                }
            }

            logger.info("Importer registered successfully: id={}, email={}", user.id, user.email)

            // Send welcome email if user has email
            if (user.email != null) {
                try {
                    emailService.sendWelcomeEmail(user.email!!, user.fullName ?: "Importer", "IMPORTER")
                } catch (e: Exception) {
                    logger.warn("Failed to send welcome email to {}: {}", user.email, e.message)
                }
            }

            // Send welcome SMS if user has phone number
            if (user.phoneNumber != null) {
                try {
                    smsService.sendWelcomeSms(user.phoneNumber!!, user.fullName ?: "Importer", "IMPORTER")
                    logger.info("Welcome SMS queued for importer: {}", user.phoneNumber)
                } catch (e: Exception) {
                    logger.warn("Failed to queue welcome SMS to {}: {}", user.phoneNumber, e.message)
                }
            }

            ResultFactory.getSuccessResult(savedImporter)
        } catch (e: PersistenceException) {
            logger.error("Database error during importer registration: {}", e.message, e)
            throw e
        } catch (e: EntityNotFoundException) {
            logger.error("Entity not found during importer registration: {}", e.message, e)
            return ResultFactory.getFailResult("Registration failed: ${e.message}")
        } catch (e: Exception) {
            logger.error("Unexpected error during importer registration: {}", e.message, e)
            return ResultFactory.getFailResult("Failed to register importer: ${e.message}")
        }
    }

    private fun validateRegistrationRequest(user: UserRegistrationDto) {
        if (user.password.isBlank()) {
            throw IllegalArgumentException("Password cannot be empty")
        }

        if (user.fullName.isBlank()) {
            throw IllegalArgumentException("Full name cannot be empty")
        }

        // Ensure at least one contact method is provided
        val hasEmail = user.email?.isNotBlank() == true
        val hasPhone = user.phoneNumber?.isNotBlank() == true

        if (!hasEmail && !hasPhone) {
            throw IllegalArgumentException("At least one contact method (email or phone number) must be provided")
        }
    }

    /**
     * Creates Hedera account asynchronously to avoid blocking registration
     * Updates aggregator with Hedera account details when creation completes
     */
    @org.springframework.scheduling.annotation.Async
    fun createHederaAccountAsync(
        userId: String,
        aggregatorId: String,
        organizationName: String
    ) {
        logger.info("Starting async Hedera account creation for aggregator ID: {}", aggregatorId)
        
        try {
            // Create Hedera account
            val hederaAccountResult = hederaAccountService.createHederaAccount(
                initialBalance = com.hedera.hashgraph.sdk.Hbar.from(10),
                memo = "AgriBackup Aggregator: $organizationName"
            )
            
            logger.info("Hedera account created successfully: {} for aggregator ID: {}", 
                hederaAccountResult.accountId, aggregatorId)
            
            // Update aggregator with Hedera account ID
            val aggregator = aggregatorRepository.findById(aggregatorId).orElseThrow {
                EntityNotFoundException("Aggregator not found with ID: $aggregatorId")
            }
            aggregator.hederaAccountId = hederaAccountResult.accountId
            aggregatorRepository.save(aggregator)
            
            // Store Hedera credentials
            val credentials = com.agriconnect.farmersportalapis.domain.hedera.HederaAccountCredentials(
                userId = userId,
                entityType = "AGGREGATOR",
                entityId = aggregatorId,
                hederaAccountId = hederaAccountResult.accountId,
                publicKey = hederaAccountResult.publicKey,
                encryptedPrivateKey = hederaAccountResult.encryptedPrivateKey,
                initialBalanceHbar = "10",
                accountMemo = "AgriBackup Aggregator: $organizationName",
                creationTransactionId = hederaAccountResult.transactionId
            )
            hederaAccountCredentialsRepository.save(credentials)
            
            // Associate with EUDR Compliance Certificate NFT
            try {
                val eudrCertificateNftId = hederaTokenService.getEudrComplianceCertificateNftId()
                if (eudrCertificateNftId != null) {
                    hederaAccountService.associateTokenWithAccount(
                        hederaAccountResult.accountId,
                        hederaAccountResult.encryptedPrivateKey,
                        eudrCertificateNftId
                    )
                    credentials.tokensAssociated = """["${eudrCertificateNftId}"]"""
                    hederaAccountCredentialsRepository.save(credentials)
                    logger.info("EUDR Certificate NFT associated with aggregator Hedera account: {}", 
                        hederaAccountResult.accountId)
                }
            } catch (e: Exception) {
                logger.warn("Failed to associate EUDR Certificate NFT with aggregator account: {}", e.message)
            }
            
            logger.info("Async Hedera account creation completed for aggregator ID: {}", aggregatorId)
        } catch (e: Exception) {
            logger.error("Failed to create Hedera account asynchronously for aggregator ID: {}", 
                aggregatorId, e)
            // The aggregator registration has already succeeded
            // Admin can manually create/associate Hedera account later if needed
        }
    }

    private fun createUser(request: UserRegistrationDto): UserProfile {
        // Handle blank email/phone by setting to null
        val email = request.email?.takeIf { it.isNotBlank() }
        val phoneNumber = request.phoneNumber?.takeIf { it.isNotBlank() }

        return UserProfile(
            id = UUID.randomUUID().toString(),
            email = email,
            passwordHash = passwordEncoder.encode(request.password),
            fullName = request.fullName,
            phoneNumber = phoneNumber,
            isActive = true,
            roles = mutableSetOf(),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    fun login(request: LoginDto): Result<LoginResponseDto> {
        val start = System.currentTimeMillis()
        logger.info("[AuthService] Received login request: emailOrPhone={}, roleType={}",
            request.emailOrPhone, request.roleType)

        return try {
            // Validate input
            if (request.emailOrPhone.isBlank()) {
                logger.warn("Validation failed: Email or phone number is null or empty")
                return ResultFactory.getFailResult("Email or phone number cannot be empty")
            }
            if (request.password.isBlank()) {
                logger.warn("Validation failed: Password is null or empty")
                return ResultFactory.getFailResult("Password cannot be empty")
            }
            logger.debug("Input validation passed: emailOrPhone={}, roleType={}",
                request.emailOrPhone, request.roleType)

            val user = userRepository.findByEmail(request.emailOrPhone)
                ?: userRepository.findByPhoneNumber(request.emailOrPhone)
            if (user == null) {
                return ResultFactory.getFailResult("User not found: ${request.emailOrPhone}")
            }

            logger.debug("[AuthService] Attempting authentication for emailOrPhone={}", request.emailOrPhone)
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(request.emailOrPhone, request.password)
            )

            logger.debug("[AuthService] Authentication successful for emailOrPhone={}", request.emailOrPhone)

            val userDetailsWithRoleId = userDetailsService.loadUserByUsernameAndRole(
                request.emailOrPhone,
                request.roleType.name
            )
            val userDetails = userDetailsWithRoleId.userDetails
            logger.debug("[AuthService] UserDetails loaded: username={}, authorities={}",
                userDetails.username, userDetails.authorities)

            if (!user.isActive) {
                logger.warn("[AuthService] User account is inactive: email={}", user.email)
                return ResultFactory.getFailResult("Account is inactive")
            }

            val hasRequestedRole = userDetails.authorities.any { it.authority == "ROLE_${request.roleType.name}" }
            if (!hasRequestedRole) {
                logger.warn("[AuthService] User does not have requested role: email={}, role={}",
                    user.email, request.roleType.name)
                return ResultFactory.getFailResult("User does not have ${request.roleType.name} role")
            }

            val roleSpecificData = when (request.roleType) {
                RoleType.FARMER -> farmerRepository.findByUserProfile(user).get().let { f ->
                    mapOf(
                        "id" to (f.id ?: ""),
                        "userId" to (f.userProfile.id ?: ""),
                        "fullName" to (f.userProfile.fullName ?: ""),
                        "email" to f.userProfile.email,
                        "phoneNumber" to f.userProfile.phoneNumber,
                        "farmName" to f.farmName,
                        "farmSize" to f.farmSize
                    )
                }
                RoleType.BUYER -> buyerRepository.findByUserProfile(user).get().let { b ->
                    BuyerLoginDto(
                        id = b.id ?: "",
                        userId = b.userProfile.id ?: "",
                        fullName = b.userProfile.fullName ?: "",
                        email = b.userProfile.email,
                        phoneNumber = b.userProfile.phoneNumber,
                        companyName = b.companyName,
                        businessType = b.businessType
                    )
                }
                RoleType.EXPORTER -> exporterRepository.findByUserProfile(user.id).get().let { e ->
                    ExporterLoginDto(
                        id = e.id,
                        userId = e.userProfile.id ?: "",
                        fullName = e.userProfile.fullName ?: "",
                        email = e.userProfile.email,
                        phoneNumber = e.userProfile.phoneNumber,
                        licenseId = e.licenseId,
                        verificationStatus = e.verificationStatus.name,
                        companyName = e.companyName,
                        companyDesc = e.companyDesc
                    )
                }
                RoleType.AGGREGATOR -> {
                    logger.debug("[AuthService] Resolving Aggregator entity for userId={}", user.id)
                    aggregatorRepository.findByUserId(user.id)?.let { a ->
                        AggregatorLoginDto(
                            id = a.id,
                            userId = a.userProfile.id ?: "",
                            fullName = a.userProfile.fullName ?: "",
                            email = a.userProfile.email,
                            phoneNumber = a.userProfile.phoneNumber,
                            organizationName = a.organizationName,
                            verificationStatus = a.verificationStatus.name,
                            hederaAccountId = a.hederaAccountId
                        )
                    }
                }
                RoleType.PROCESSOR -> {
                    logger.debug("[AuthService] Resolving Processor entity for userId={}", user.id)
                    processorRepository.findByUserProfile_Id(user.id)?.let { p ->
                        ProcessorLoginDto(
                            id = p.id,
                            userId = p.userProfile.id ?: "",
                            fullName = p.userProfile.fullName ?: "",
                            email = p.userProfile.email,
                            phoneNumber = p.userProfile.phoneNumber,
                            facilityName = p.facilityName,
                            verificationStatus = p.verificationStatus.name,
                            hederaAccountId = p.hederaAccountId
                        )
                    }
                }
                RoleType.IMPORTER -> {
                    logger.debug("[AuthService] Resolving Importer entity for userId={}", user.id)
                    importerRepository.findByUserId(user.id)?.let { i ->
                        ImporterLoginDto(
                            id = i.id,
                            userId = i.userProfile.id ?: "",
                            fullName = i.userProfile.fullName ?: "",
                            email = i.userProfile.email,
                            phoneNumber = i.userProfile.phoneNumber,
                            companyName = i.companyName,
                            verificationStatus = i.verificationStatus.name,
                            hederaAccountId = i.hederaAccountId
                        )
                    }
                }
                RoleType.ADMIN -> {
                    logger.debug("[AuthService] Resolving Admin entity for userId={}", user.id)
                    adminRepository.findByUserProfileId(user.id)?.let { admin ->
                        AdminLoginDto(
                            id = admin.id,
                            userId = admin.userProfile.id ?: "",
                            fullName = admin.userProfile.fullName ?: "",
                            email = admin.userProfile.email,
                            phoneNumber = admin.userProfile.phoneNumber,
                            role = admin.role,
                            department = admin.department
                        )
                    }
                }
                RoleType.SYSTEM_ADMIN -> {
                    logger.debug("[AuthService] Resolving System Admin entity for userId={}", user.id)
                    systemAdminRepository.findByUserProfileId(user.id)?.let { sa ->
                    SystemAdminLoginDto(
                        id = sa.id,
                        userId = sa.userProfile.id ?: "",
                        fullName = sa.userProfile.fullName ?: "",
                        email = sa.userProfile.email,
                        phoneNumber = sa.userProfile.phoneNumber,
                        status = sa.status
                    )
                }}
                RoleType.ZONE_SUPERVISOR -> {
                    logger.debug("[AuthService] Resolving Zone Supervisor entity for userId={}", user.id)
                    zoneSupervisorRepository.findByUserProfileId(user.id)?.let { zs ->
                    ZoneSupervisorLoginDto(
                        id = zs.id,
                        userId = zs.userProfile.id ?: "",
                        fullName = zs.userProfile.fullName ?: "",
                        email = zs.userProfile.email,
                        phoneNumber = zs.userProfile.phoneNumber,
                        status = zs.status
                    )
                }}
            }
            logger.debug("[AuthService] Role-specific data retrieved: roleType={}, data={}",
                request.roleType, roleSpecificData)

            val permissions = user.roles.firstOrNull { role -> role.name.uppercase().equals(request.roleType.name) }?.permissions
            val token = jwtUtil.generateToken(
                userDetails,
                request.roleType.name,
                userDetailsWithRoleId.roleSpecificId,
                userDetailsWithRoleId.name,
                userDetailsWithRoleId.email,
                userDetailsWithRoleId.phoneNumber,
                permissions
            )
            logger.debug("[AuthService] JWT token generated for emailOrPhone={}", request.emailOrPhone)

            logger.info("[AuthService] ROLE SPECIFIC DATA={}", roleSpecificData)
            val loginResponse = LoginResponseDto(
                token = token,
                roleSpecificData = roleSpecificData
            )

            val elapsed = System.currentTimeMillis() - start
            logger.info("[AuthService] Login successful for principal={}, roleType={}, elapsed={} ms", request.emailOrPhone, request.roleType, elapsed)

            // Send login notification email if user has email
            /*
            if (user.email != null) {
                try {
                    val loginTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    emailService.sendLoginNotificationEmail(
                        user.email!!,
                        user.fullName ?: "User",
                        loginTime
                    )
                } catch (e: Exception) {
                    logger.warn("Failed to send login notification email to {}: {}", user.email, e.message)
                    // Don't fail login if email sending fails
                }
            }
            */

            return ResultFactory.getSuccessResult(loginResponse)
        } catch (e: BadCredentialsException) {
            logger.error("[AuthService] Authentication failed: Invalid credentials for emailOrPhone={}", request.emailOrPhone, e)
            return ResultFactory.getFailResult("Invalid " + (if (request.emailOrPhone.contains("@")) "email" else "phone") + " and password")
        } catch (e: DisabledException) {
            logger.error("[AuthService] Authentication failed: Account disabled for emailOrPhone={}", request.emailOrPhone, e)
            return ResultFactory.getFailResult("Account is disabled")
        } catch (e: UsernameNotFoundException) {
            logger.error("[AuthService] User not found for emailOrPhone={}: {}", request.emailOrPhone, e.message, e)
            return ResultFactory.getFailResult("User not found")
        } catch (e: EntityNotFoundException) {
            logger.error("[AuthService] Entity not found during login for emailOrPhone={}: {}", request.emailOrPhone, e.message, e)
            return ResultFactory.getFailResult("Login failed: ${e.message}")
        } catch (e: Exception) {
            logger.error("[AuthService] Unexpected error during login for emailOrPhone={}: {}", request.emailOrPhone, e.message, e)
            return ResultFactory.getFailResult("Login failed: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun requestPasswordReset(input: String): Result<Boolean> {
        return try {
            if (input.isBlank()) {
                logger.warn("Invalid input for password reset: Input is empty")
                throw IllegalArgumentException("Input cannot be empty")
            }

            val user = when {
                input.contains("@") -> userRepository.findByEmail(input)
                    ?: throw UsernameNotFoundException("User with email $input not found")
                input.matches(Regex("^[0-9+\\-\\s()]{8,15}$")) -> userRepository.findByPhoneNumber(input)
                    ?: throw UsernameNotFoundException("User with phone number $input not found")
                else -> throw IllegalArgumentException("Invalid email or phone number format")
            }

            if (!user.isActive) {
                logger.warn("Password reset requested for inactive account: {}", input)
                return ResultFactory.getFailResult("Account is inactive")
            }

            // Delete existing tokens
            try {
                passwordResetTokenRepository.deleteByUser(user)
            } catch (e: PersistenceException) {
                logger.error("Failed to delete existing tokens for user {}: {}", input, e.message, e)
                throw e // Allow transaction rollback
            }

            // Generate 6-digit OTP
            val otp = (100000 + random.nextInt(900000)).toString()
            val expiryDate = LocalDateTime.now().plusHours(1)
            val resetToken = PasswordResetToken(token = otp, user = user, expiryDate = expiryDate)
            passwordResetTokenRepository.save(resetToken)

            // Send reset link
            val resetLink = "https://www.agribackup.com//reset-password?otp=$otp"
            try {
                if (input.contains("@")) {
                    emailService.sendPasswordResetEmail(user.email!!, otp, resetLink)
                } else {
                    smsService.sendPasswordResetSms(user.phoneNumber!!, otp, resetLink)
                }
            } catch (e: Exception) {
                logger.error("Failed to send password reset notification to {}: {}", input, e.message, e)
                throw PersistenceException("Failed to send password reset notification: ${e.message}", e)
            }

            logger.info("Password reset OTP generated for user: {}", input)
            ResultFactory.getSuccessResult(true, "Password reset OTP sent to your email or phone number")
        } catch (e: PersistenceException) {
            logger.error("Database error during password reset request for {}: {}", input, e.message, e)
            throw e // Allow transaction rollback
        } catch (e: UsernameNotFoundException) {
            logger.error("User not found for password reset: {}", input, e)
            return ResultFactory.getFailResult("User not found")
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid input for password reset: {}", input, e)
            return ResultFactory.getFailResult(e.message ?: "Invalid input")
        } catch (e: Exception) {
            logger.error("Unexpected error during password reset request for {}: {}", input, e.message, e)
            return ResultFactory.getFailResult("Password reset request failed: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun resetPassword(token: String, newPassword: String): Result<Boolean> {
        return try {
            if (token.isBlank() || newPassword.isBlank()) {
                logger.warn("Invalid input for password reset: token or password is empty")
                throw IllegalArgumentException("Token and new password cannot be empty")
            }

            val resetToken = passwordResetTokenRepository.findByToken(token)
                ?: throw IllegalArgumentException("Invalid or expired token")

            if (resetToken.isExpired()) {
                passwordResetTokenRepository.delete(resetToken)
                logger.warn("Password reset attempt with expired token")
                throw IllegalArgumentException("Token has expired")
            }

            val user = resetToken.user
            if (!user.isActive) {
                logger.warn("Password reset attempted for inactive account: {}", user.email)
                return ResultFactory.getFailResult("Account is inactive")
            }

            user.passwordHash = passwordEncoder.encode(newPassword)
            userRepository.save(user)

            // Delete the used token
            try {
                passwordResetTokenRepository.delete(resetToken)
            } catch (e: PersistenceException) {
                logger.error("Failed to delete used password reset token: {}", e.message, e)
                throw e // Allow transaction rollback
            }

            logger.info("Password reset successful for user: {}", user.email)
            ResultFactory.getSuccessResult(true, "Password reset successful")
        } catch (e: PersistenceException) {
            logger.error("Database error during password reset: {}", e.message, e)
            throw e // Allow transaction rollback
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid input for password reset: {}", e.message, e)
            return ResultFactory.getFailResult(e.message ?: "Invalid input")
        } catch (e: Exception) {
            logger.error("Unexpected error during password reset: {}", e.message, e)
            return ResultFactory.getFailResult("Password reset failed: ${e.message}")
        }
    }
}