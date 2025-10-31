package com.agriconnect.farmersportalapis.application.security

import com.agriconnect.farmersportalapis.buyers.infrastructure.repositories.BuyerRepository
import com.agriconnect.farmersportalapis.domain.auth.RoleType
import com.agriconnect.farmersportalapis.infrastructure.repositories.*
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository,
    private val farmerRepository: FarmerRepository,
    private val buyerRepository: BuyerRepository,
    private val exporterRepository: ExporterRepository,
    private val adminRepository: AdminEntityRepository,
    private val systemAdminRepository: SystemAdminRepository,
    private val zoneSupervisorRepository: ZoneSupervisorRepository,
    private val aggregatorRepository: AggregatorRepository,
    private val processorRepository: ProcessorRepository,
    private val importerRepository: ImporterRepository,
) : UserDetailsService {

    private val logger = LoggerFactory.getLogger(CustomUserDetailsService::class.java)

    data class UserDetailsWithRoleId(
        val userDetails: UserDetails,
        val roleSpecificId: String,
        val name: String,
        val email: String?,
        val phoneNumber: String?
    )

    fun loadUserByUsernameAndRole(username: String, role: String?): UserDetailsWithRoleId {
        logger.debug("Loading user by username: {} and role parameter: {}", username, role)

        if (username.isBlank()) {
            logger.error("Username is blank")
            throw UsernameNotFoundException("Username cannot be empty")
        }

        val user = userRepository.findByEmail(username)
            ?: userRepository.findByPhoneNumber(username)
            ?: run {
                logger.warn("User not found for username: {}", username)
                throw UsernameNotFoundException("User not found with email or phone: $username")
            }

        logger.debug("User found: name: {}, isActive={}", user.fullName, user.isActive)
        logger.debug("User roles: {}", user.roles.map { it.name }) // <-- Log user's roles

        if (user.passwordHash == null || user.passwordHash == "") {
            logger.error("User passwordHash is null or blank for username: {}", username)
            throw IllegalArgumentException("User password hash cannot be null or blank")
        }

        val selectedRole = if (role.isNullOrBlank()) {
            user.roles.firstOrNull()?.name
            ?: throw IllegalArgumentException("No valid role found for user: $username")
        } else {
            role.uppercase()
        }

        logger.debug("Selected role to use: {}", selectedRole) // <-- Log selected role

        if (!user.roles.any { it.name == selectedRole }) {
            logger.error("User does not have role: {} for username: {}", selectedRole, username)
            throw IllegalArgumentException("User does not have role: $selectedRole")
        }

        val roleSpecificId = when (selectedRole) {
            RoleType.FARMER.name -> {
                val farmer = farmerRepository.findByUserProfile(user).orElse(null)
                    ?: throw IllegalArgumentException("Farmer not found for user: $username")
                farmer.id.toString()
            }
            RoleType.BUYER.name -> {
                val buyer = buyerRepository.findByUserProfile(user).orElse(null)
                    ?: throw IllegalArgumentException("Buyer not found for user: $username")
                buyer.id.toString()
            }
            RoleType.EXPORTER.name -> {
                val exporter = exporterRepository.findByUserProfile(user.id).orElse(null)
                    ?: throw IllegalArgumentException("Exporter not found for user: $username")
                exporter.id.toString()
            }
            RoleType.ADMIN.name -> {
                val admin = adminRepository.findByUserProfileId(user.id)
                    ?: throw IllegalArgumentException("Admin not found for user: $username")
                admin.id.toString()
            }
            RoleType.SYSTEM_ADMIN.name -> {
                val sa = systemAdminRepository.findByUserProfileId(user.id)
                    ?: throw IllegalArgumentException("System Admin not found for user: $username")
                sa.id.toString()
            }
            RoleType.ZONE_SUPERVISOR.name -> {
                val zs = zoneSupervisorRepository.findByUserProfileId(user.id)
                    ?: throw IllegalArgumentException("Zone Supervisor not found for user: $username")
                zs.id.toString()
            }
            RoleType.AGGREGATOR.name -> {
                val zs = aggregatorRepository.findByUserProfile(user)
                    ?: throw IllegalArgumentException("Aggregator not found for user: $username")
                zs.id.toString()
            }
            RoleType.PROCESSOR.name -> {
                val zs = processorRepository.findByUserProfile(user)
                    ?: throw IllegalArgumentException("Processor not found for user: $username")
                zs.id.toString()
            }
            RoleType.IMPORTER.name -> {
                val zs = importerRepository.findByUserProfile(user)
                    ?: throw IllegalArgumentException("Importer not found for user: $username")
                zs.id.toString()
            }
            else -> throw IllegalArgumentException("Invalid role: $selectedRole")
        }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_$selectedRole"))
        logger.debug("Authorities loaded for user {}: {}", user.phoneNumber, authorities)

        val userDetails = User(
            roleSpecificId,
            user.passwordHash,
            user.isActive,
            true,
            true,
            true,
            authorities
        )

        return UserDetailsWithRoleId(
            userDetails = userDetails,
            roleSpecificId = roleSpecificId,
            name = user.fullName,
            email = user.email,
            phoneNumber = user.phoneNumber
        )
    }


    override fun loadUserByUsername(username: String): UserDetails {
        logger.debug("Loading user by username: {}", username)
        // Default to first role if no specific role is provided
        return loadUserByUsernameAndRole(username, null).userDetails
    }
}