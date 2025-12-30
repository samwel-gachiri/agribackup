package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.auth.*
import com.agriconnect.farmersportalapis.service.common.impl.AuthService
import com.agriconnect.farmersportalapis.service.common.impl.SmsService
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService, private val smsService: SmsService) {
        private val logger = LoggerFactory.getLogger(AuthController::class.java)

        @PostMapping("/register/user")
        fun registerUser(@RequestBody request: UserRegistrationDto) =
                authService.registerUser(request)

        @PostMapping("/register/farmer")
        fun registerFarmer(@RequestBody request: FarmerRegistrationDto) =
                authService.registerFarmer(request)

        @PostMapping("/register/buyer")
        fun registerBuyer(@RequestBody request: BuyerRegistrationDto) =
                authService.registerBuyer(request)

        @PostMapping("/register/exporter")
        fun registerExporter(@RequestBody request: ExporterRegistrationDto) =
                authService.registerExporter(request)

        @PostMapping("/register/aggregator")
        fun registerAggregator(@RequestBody request: AggregatorRegistrationDto) =
                authService.registerAggregator(request)

        @PostMapping("/register/processor")
        fun registerProcessor(@RequestBody request: ProcessorRegistrationDto) =
                authService.registerProcessor(request)

        @PostMapping("/register/importer")
        fun registerImporter(@RequestBody request: ImporterRegistrationDto) =
                authService.registerImporter(request)

        @PostMapping("/login")
        fun login(
                @RequestBody request: LoginDto
        ): com.agriconnect.farmersportalapis.application.util.Result<LoginResponseDto> {
                val start = System.currentTimeMillis()
                logger.info(
                        "[AuthController] /api/auth/login received: roleType={}, principal={}",
                        request.roleType,
                        request.emailOrPhone
                )
                return try {
                        val result = authService.login(request)
                        val elapsed = System.currentTimeMillis() - start
                        logger.info(
                                "[AuthController] /api/auth/login completed in {} ms: success={}, message={}",
                                elapsed,
                                result.success,
                                result.msg
                        )
                        result
                } catch (e: Exception) {
                        val elapsed = System.currentTimeMillis() - start
                        logger.error(
                                "[AuthController] /api/auth/login failed after {} ms: {}",
                                elapsed,
                                e.message,
                                e
                        )
                        throw e
                }
        }

        @PostMapping("/google-login")
        fun googleLogin(
                @RequestBody request: GoogleLoginDto
        ): com.agriconnect.farmersportalapis.application.util.Result<LoginResponseDto> {
                val start = System.currentTimeMillis()
                logger.info("[AuthController] /api/auth/google-login received")
                return try {
                        val result = authService.googleLogin(request)
                        val elapsed = System.currentTimeMillis() - start
                        logger.info(
                                "[AuthController] /api/auth/google-login completed in {} ms: success={}",
                                elapsed,
                                result.success
                        )
                        result
                } catch (e: Exception) {
                        val elapsed = System.currentTimeMillis() - start
                        logger.error(
                                "[AuthController] /api/auth/google-login failed after {} ms: {}",
                                elapsed,
                                e.message,
                                e
                        )
                        throw e
                }
        }

        @PostMapping("/forgot-password")
        fun forgotPassword(@RequestBody request: ForgotPasswordRequest) =
                authService.requestPasswordReset(request.input)

        @PostMapping("/reset-password")
        fun resetPassword(@RequestBody request: ResetPasswordRequest) =
                authService.resetPassword(request.otp, request.newPassword)

        @PostMapping("/assign-roles")
        fun assignRoles(@RequestBody request: AssignRolesDto) = authService.assignRoles(request)

        @GetMapping("/my-roles")
        fun getMyRoles(
                @org.springframework.web.bind.annotation.RequestHeader("Authorization") authHeader: String?
        ): com.agriconnect.farmersportalapis.application.util.Result<List<RoleOption>> {
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        return com.agriconnect.farmersportalapis.application.util.ResultFactory.getFailResult("Not authenticated")
                }
                val token = authHeader.substring(7)
                return authService.getUserRolesFromToken(token)
        }

        @PostMapping("/test-sms")
        fun testSms(@RequestBody request: TestSmsRequest): Map<String, String> {
                return try {
                        logger.info("Testing SMS to: {}", request.phoneNumber)
                        val result =
                                smsService.testSmsDirect(
                                        request.phoneNumber,
                                        request.message
                                                ?: "Test SMS from AgriBackup API - ${request.name ?: "Test User"}"
                                )
                        mapOf(
                                "status" to
                                        if (result.startsWith("SUCCESS")) "success" else "error",
                                "message" to result
                        )
                } catch (e: Exception) {
                        logger.error("Failed to send test SMS: {}", e.message, e)
                        mapOf("status" to "error", "message" to "Failed to send SMS: ${e.message}")
                }
        }
}

data class ForgotPasswordRequest(val input: String, val mobile: String? = null)

data class ResetPasswordRequest(val otp: String, val newPassword: String)

data class TestSmsRequest(
        val phoneNumber: String,
        val name: String? = null,
        val role: String? = null,
        val message: String? = null
)
