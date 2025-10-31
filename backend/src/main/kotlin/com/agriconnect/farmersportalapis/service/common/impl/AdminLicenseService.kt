package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.application.dtos.*
import com.agriconnect.farmersportalapis.application.util.Result
import com.agriconnect.farmersportalapis.application.util.ResultFactory
import com.agriconnect.farmersportalapis.domain.common.enums.ExporterVerificationStatus
import com.agriconnect.farmersportalapis.domain.profile.Admin
import com.agriconnect.farmersportalapis.domain.profile.Exporter
import com.agriconnect.farmersportalapis.infrastructure.repositories.AdminEntityRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.ExporterRepository
import com.agriconnect.farmersportalapis.infrastructure.repositories.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AdminLicenseService(
    private val adminRepository: AdminEntityRepository,
    private val exporterRepository: ExporterRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val emailService: EmailService
) {
    private val logger = LoggerFactory.getLogger(AdminLicenseService::class.java)

    @Transactional(readOnly = true)
    fun adminLogin(request: AdminLoginRequestDto): Result<AdminLoginResponseDto> {
        return try {
            val user = userRepository.findByEmail(request.email)
                ?: return ResultFactory.getFailResult("Invalid credentials")

            // Check if user is an admin
            val admin = adminRepository.findByUserProfile(user)
                ?: return ResultFactory.getFailResult("Not authorized as admin")

            // Verify password (assuming password is stored in user profile)
            if (!passwordEncoder.matches(request.password, user.passwordHash ?: "")) {
                return ResultFactory.getFailResult("Invalid credentials")
            }

            // Generate JWT token (simplified - in real implementation, use proper JWT service)
            val token = "admin-jwt-token-${admin.id}"

            val response = AdminLoginResponseDto(
                adminId = admin.id,
                userId = user.id,
                fullName = user.fullName,
                email = user.email ?: "",
                role = admin.role,
                department = admin.department,
                token = token
            )

            ResultFactory.getSuccessResult(response, "Admin login successful")
        } catch (e: Exception) {
            logger.error("Error during admin login: {}", e.message, e)
            ResultFactory.getFailResult("Login failed: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    fun getPendingLicenses(adminId: String): Result<List<LicenseReviewDto>> {
        return try {
            // Verify admin exists
            adminRepository.findByIdOrNull(adminId)
                ?: return ResultFactory.getFailResult("Admin not found")

            val exporters = exporterRepository.findByVerificationStatus(ExporterVerificationStatus.UNDER_REVIEW)

            val licenses = exporters.map { exporter ->
                LicenseReviewDto(
                    licenseId = exporter.id,
                    exporterId = exporter.id,
                    exporterName = exporter.userProfile.fullName,
                    companyName = exporter.companyName,
                    licenseIdValue = exporter.licenseId,
                    documentUrl = exporter.exportLicenseFormUrl,
                    submittedAt = LocalDateTime.now(), // TODO: Add submitted timestamp to entity
                    verificationStatus = exporter.verificationStatus.name,
                    reviewerId = null,
                    reviewedAt = null
                )
            }

            ResultFactory.getSuccessResult(licenses, "Pending licenses retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving pending licenses: {}", e.message, e)
            ResultFactory.getFailResult("Failed to retrieve pending licenses: ${e.message}")
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    fun reviewLicense(licenseId: String, request: LicenseReviewRequestDto, adminId: String): Result<LicenseReviewResponseDto> {
        return try {
            // Verify admin exists
            val admin = adminRepository.findByIdOrNull(adminId)
                ?: return ResultFactory.getFailResult("Admin not found")

            val exporter = exporterRepository.findByIdOrNull(licenseId)
                ?: return ResultFactory.getFailResult("License/Exporter not found")

            val newStatus = when (request.decision.uppercase()) {
                "APPROVE" -> ExporterVerificationStatus.VERIFIED
                "REJECT" -> ExporterVerificationStatus.REJECTED
                else -> return ResultFactory.getFailResult("Invalid decision. Must be APPROVE or REJECT")
            }

            exporter.verificationStatus = newStatus
            val savedExporter = exporterRepository.save(exporter)

            val reviewedAt = LocalDateTime.now()

            // Send email notification
            val emailSent = sendLicenseReviewEmail(exporter, request.decision, request.comments ?: "", admin)

            val response = LicenseReviewResponseDto(
                licenseId = licenseId,
                exporterId = exporter.id,
                decision = request.decision,
                comments = request.comments,
                reviewedBy = admin.userProfile.fullName,
                reviewedAt = reviewedAt,
                emailSent = emailSent
            )

            ResultFactory.getSuccessResult(response, "License reviewed successfully")
        } catch (e: Exception) {
            logger.error("Error reviewing license {}: {}", licenseId, e.message, e)
            ResultFactory.getFailResult("Failed to review license: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    fun getLicenseDetails(licenseId: String, adminId: String): Result<LicenseDetailDto> {
        return try {
            // Verify admin exists
            adminRepository.findByIdOrNull(adminId)
                ?: return ResultFactory.getFailResult("Admin not found")

            val exporter = exporterRepository.findByIdOrNull(licenseId)
                ?: return ResultFactory.getFailResult("License/Exporter not found")

            val details = LicenseDetailDto(
                licenseId = exporter.id,
                exporterId = exporter.id,
                exporterName = exporter.userProfile.fullName,
                companyName = exporter.companyName,
                email = exporter.userProfile.email ?: "",
                phoneNumber = exporter.userProfile.phoneNumber ?: "",
                licenseIdValue = exporter.licenseId,
                documentUrl = exporter.exportLicenseFormUrl,
                submittedAt = LocalDateTime.now(), // TODO: Add submitted timestamp
                verificationStatus = exporter.verificationStatus.name,
                reviewerId = null, // TODO: Add reviewer tracking
                reviewedAt = null, // TODO: Add reviewed timestamp
                reviewComments = null // TODO: Add review comments
            )

            ResultFactory.getSuccessResult(details, "License details retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving license details: {}", e.message, e)
            ResultFactory.getFailResult("Failed to retrieve license details: ${e.message}")
        }
    }

    @Transactional(readOnly = true)
    fun getAdminProfile(adminId: String): Result<AdminProfileDto> {
        return try {
            val admin = adminRepository.findByIdOrNull(adminId)
                ?: return ResultFactory.getFailResult("Admin not found")

            val profile = AdminProfileDto(
                adminId = admin.id,
                userId = admin.userProfile.id,
                fullName = admin.userProfile.fullName,
                email = admin.userProfile.email ?: "",
                role = admin.role,
                department = admin.department,
                createdAt = admin.userProfile.createdAt,
                updatedAt = admin.userProfile.updatedAt
            )

            ResultFactory.getSuccessResult(profile, "Admin profile retrieved successfully")
        } catch (e: Exception) {
            logger.error("Error retrieving admin profile: {}", e.message, e)
            ResultFactory.getFailResult("Failed to retrieve admin profile: ${e.message}")
        }
    }

    private fun sendLicenseReviewEmail(exporter: Exporter, decision: String, comments: String, admin: Admin): Boolean {
        return try {
            val subject = when (decision.uppercase()) {
                "APPROVE" -> "🎉 Congratulations! Your AgriBackup Export License is Approved!"
                "REJECT" -> "License Review Update - AgriBackup"
                else -> "License Review Decision - AgriBackup"
            }

            val decisionColor = when (decision.uppercase()) {
                "APPROVE" -> "#2e7d32" // green-700
                "REJECT" -> "#d32f2f"  // red-700
                else -> "#666666"      // gray-600
            }

            val decisionIcon = when (decision.uppercase()) {
                "APPROVE" -> "✅"
                "REJECT" -> "❌"
                else -> "ℹ️"
            }

            val decisionTitle = when (decision.uppercase()) {
                "APPROVE" -> "License Approved!"
                "REJECT" -> "License Under Review"
                else -> "License Update"
            }

            val mainMessage = when (decision.uppercase()) {
                "APPROVE" -> """
                    <div style="background-color: #e8f5e8; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2e7d32;">
                        <h3 style="color: #2e7d32; margin-top: 0;">🎉 Congratulations, ${exporter.userProfile.fullName}!</h3>
                        <p style="margin-bottom: 0; line-height: 1.6;">
                            Your export license application has been <strong>APPROVED</strong>! You can now start onboarding your farmers, registering zones, complying with EUDR regulations, and getting the most out of the AgriBackup platform. We are glad to have you on board!
                        </p>
                    </div>
                """
                "REJECT" -> """
                    <div style="background-color: #ffebee; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #d32f2f;">
                        <h3 style="color: #d32f2f; margin-top: 0;">License Application Update</h3>
                        <p style="margin-bottom: 0; line-height: 1.6;">
                            ${if (comments.isNotBlank()) "Comments from reviewer: <em>$comments</em>" else "We need some additional information to process your application. Please review our feedback and resubmit with the requested details."}
                        </p>
                    </div>
                """
                else -> """
                    <div style="background-color: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #666666;">
                        <h3 style="color: #666666; margin-top: 0;">License Review Update</h3>
                        <p style="margin-bottom: 0; line-height: 1.6;">
                            Your application is currently being reviewed. We'll update you as soon as we have more information.
                        </p>
                    </div>
                """
            }

            val body = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2e7d32;">Welcome to AgriBackup</h2>

                    <p>Dear ${exporter.userProfile.fullName},</p>

                    <p>We have reviewed your export license application. Here are the results:</p>

                    <!-- Decision Section -->
                    $mainMessage

                    <!-- Review Details -->
                    <div style="background-color: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="color: #2e7d32; margin-top: 0;">Review Details</h3>
                        <div style="line-height: 1.6;">
                            <p><strong>Decision:</strong> <span style="color: $decisionColor;">$decisionIcon ${decision.uppercase()}</span></p>
                            <p><strong>Reviewed By:</strong> ${admin.userProfile.fullName}</p>
                            ${if (admin.department != null) "<p><strong>Department:</strong> ${admin.department}</p>" else ""}
                            <p><strong>License ID:</strong> ${exporter.licenseId ?: "N/A"}</p>
                        </div>
                    </div>

                    <!-- Next Steps -->
                    <div style="background-color: #fff3e0; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #f57c00;">
                        <h4 style="color: #f57c00; margin-top: 0;">What's Next?</h4>
                        ${when (decision.uppercase()) {
                "APPROVE" -> """
                        <ul style="line-height: 1.6; margin-bottom: 0;">
                            <li><strong>Onboard your farmers</strong> and manage their profiles on the platform</li>
                            <li><strong>Register and manage zones</strong> for efficient agricultural operations</li>
                            <li><strong>Ensure EUDR compliance</strong> with our regulatory tools and reporting features</li>
                            <li><strong>Connect with the AgriBackup ecosystem</strong> for maximum platform benefits</li>
                            <li><strong>Access comprehensive analytics</strong> to optimize your agricultural business</li>
                        </ul>
                        """
                "REJECT" -> """
                        <ul style="line-height: 1.6; margin-bottom: 0;">
                            <li><strong>Review the feedback</strong> provided above</li>
                            <li><strong>Update your application</strong> with the requested information</li>
                            <li><strong>Resubmit your license application</strong> when ready</li>
                            <li><strong>Contact support</strong> if you need clarification</li>
                        </ul>
                        """
                else -> """
                        <ul style="line-height: 1.6; margin-bottom: 0;">
                            <li><strong>Check your dashboard</strong> for updates on your application</li>
                            <li><strong>Complete any pending requirements</strong></li>
                            <li><strong>Contact support</strong> if you have questions</li>
                        </ul>
                        """
            }}
                    </div>

                    <!-- CTA Button -->
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://agribackup.com/signin"
                           style="background-color: #2e7d32;
                                  color: white;
                                  text-decoration: none;
                                  padding: 12px 24px;
                                  border-radius: 6px;
                                  font-weight: bold;
                                  display: inline-block;">
                            ${if (decision.uppercase() == "APPROVE") "Sign in" else "Access Your Dashboard"}
                        </a>
                    </div>

                    <p>If you have any questions or need help getting started, don't hesitate to contact our support team at <a href="mailto:support@agribackup.com">support@agribackup.com</a>.</p>

                    <p>We're excited to have you as part of the AgriBackup community!</p>

                    <p style="margin-bottom: 30px;">Best regards,<br/>
                    <strong>The AgriBackup Team</strong></p>

                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;"/>

                    <div style="text-align: center; color: #666; font-size: 12px;">
                        <p>This is an automated message from AgriBackup.</p>
                        <p>© 2025 AgriBackup. All rights reserved.</p>
                        <p><a href="https://www.agribackup.com" style="color: #2e7d32;">Visit our website</a> | <a href="https://www.agribackup.com/privacy" style="color: #2e7d32;">Privacy Policy</a> | <a href="https://www.agribackup.com/terms" style="color: #2e7d32;">Terms of Service</a></p>
                    </div>
                </div>
            """.trimIndent()

            emailService.sendEmail(exporter.userProfile.email ?: "", subject, body)
            true
        } catch (e: Exception) {
            logger.error("Failed to send license review email to ${exporter.userProfile.email}: {}", e.message, e)
            false
        }
    }
}