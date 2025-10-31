package com.agriconnect.farmersportalapis.service.common.impl

import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val javaMailSender: JavaMailSender
) {
    private val logger = LoggerFactory.getLogger(EmailService::class.java)

    @Async
    fun sendPasswordResetEmail(to: String, otp: String, resetLink: String) {
        try {
            val message = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true)
            helper.setTo(to)
            helper.setSubject("Password Reset Request")
            val emailContent = """
                <h3>Password Reset</h3>
                <p>Your one-time password (OTP) is: <strong>$otp</strong></p>
                <p>Click the link below to reset your password:</p>
                <a href="$resetLink">Reset Password</a>
                <p>This OTP will expire in 1 hour.</p>
            """.trimIndent()
            helper.setText(emailContent, true)
            logger.info("Sending email to $to with OTP: $otp")
            javaMailSender.send(message)
            logger.info("Email sent successfully to $to")
        } catch (e: Exception) {
            logger.error("Failed to send email to $to: ${e.message}", e)
            throw e
        }
    }

    @Async
    fun sendLoginNotificationEmail(to: String, fullName: String, loginTime: String, ipAddress: String? = null) {
        try {
            val message = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true)
            helper.setTo(to)
            helper.setSubject("Login Notification - AgriBackup")
            val emailContent = """
                <h3>Login Notification</h3>
                <p>Dear $fullName,</p>
                <p>We detected a successful login to your AgriBackup account.</p>
                <p><strong>Login Details:</strong></p>
                <ul>
                    <li><strong>Time:</strong> $loginTime</li>
                    ${if (ipAddress != null) "<li><strong>IP Address:</strong> $ipAddress</li>" else ""}
                    <li><strong>Device/Browser:</strong> Web Application</li>
                </ul>
                <p>If this was you, no action is required. If you did not log in to your account, please contact our support team immediately.</p>
                <p>For your security, we recommend:</p>
                <ul>
                    <li>Using a strong, unique password</li>
                    <li>Enabling two-factor authentication when available</li>
                    <li>Regularly monitoring your account activity</li>
                </ul>
                <p>Best regards,<br/>AgriBackup Security Team</p>
                <hr/>
                <p style="font-size: 12px; color: #666;">
                    This is an automated message. Please do not reply to this email.
                    If you have any questions, contact us at support@agribackup.com
                </p>
            """.trimIndent()
            helper.setText(emailContent, true)
            logger.info("Sending login notification email to $to")
            javaMailSender.send(message)
            logger.info("Login notification email sent successfully to $to")
        } catch (e: Exception) {
            logger.error("Failed to send login notification email to $to: ${e.message}", e)
            // Don't throw exception for login notifications to avoid blocking login
        }
    }

    @Async
    fun sendWelcomeEmail(to: String, fullName: String, userRole: String) {
        try {
            val message = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true)
            helper.setTo(to)
            helper.setSubject("Welcome to AgriBackup - Your Account is Ready!")

            val roleDisplayName = when (userRole.uppercase()) {
                "FARMER" -> "Farmer"
                "BUYER" -> "Buyer"
                "EXPORTER" -> "Exporter"
                "SYSTEM_ADMIN" -> "System Administrator"
                "ZONE_SUPERVISOR" -> "Zone Supervisor"
                else -> "User"
            }

            val emailContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2e7d32;">Welcome to AgriBackup, $fullName!</h2>

                    <p>Thank you for joining the AgriBackup community. Your $roleDisplayName account has been successfully created and is ready to use.</p>

                    <div style="background-color: #f5f5f5; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="color: #2e7d32; margin-top: 0;">What's Next?</h3>
                        <ul style="line-height: 1.6;">
                            <li><strong>Complete your profile:</strong> Add more details to help us serve you better</li>
                            <li><strong>Explore the platform:</strong> Discover features tailored for $roleDisplayName</li>
                            <li><strong>Connect with others:</strong> Start building your agricultural network</li>
                        </ul>
                    </div>

                    <div style="background-color: #e8f5e8; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2e7d32;">
                        <h4 style="color: #2e7d32; margin-top: 0;">Getting Started Tips:</h4>
                        <ul style="margin-bottom: 0; line-height: 1.6;">
                            <li>Log in to your account using your email/phone and password</li>
                            <li>Update your profile information and preferences</li>
                            <li>Familiarize yourself with the dashboard and available features</li>
                            <li>Reach out to our support team if you need assistance</li>
                        </ul>
                    </div>

                    <p>If you have any questions or need help getting started, don't hesitate to contact our support team at <a href="mailto:support@agribackup.com">support@agribackup.com</a>.</p>

                    <p>We're excited to have you as part of the AgriBackup community!</p>

                    <p style="margin-bottom: 30px;">Best regards,<br/>
                    <strong>The AgriBackup Team</strong></p>

                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;"/>

                    <div style="text-align: center; color: #666; font-size: 12px;">
                        <p>This is an automated welcome message from AgriBackup.</p>
                        <p>© 2024 AgriBackup. All rights reserved.</p>
                        <p><a href="https://www.agribackup.com" style="color: #2e7d32;">Visit our website</a> | <a href="https://www.agribackup.com/privacy" style="color: #2e7d32;">Privacy Policy</a> | <a href="https://www.agribackup.com/terms" style="color: #2e7d32;">Terms of Service</a></p>
                    </div>
                </div>
            """.trimIndent()

            helper.setText(emailContent, true)
            logger.info("Sending welcome email to $to for role: $userRole")
            javaMailSender.send(message)
            logger.info("Welcome email sent successfully to $to")
        } catch (e: Exception) {
            logger.error("Failed to send welcome email to $to: ${e.message}", e)
            // Don't throw exception for welcome emails to avoid blocking registration
        }
    }

    @Async
    fun sendLicenseReviewNotificationEmail(to: String, adminName: String, exporterName: String, licenseId: String) {
        try {
            val message = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true)
            helper.setTo(to)
            helper.setSubject("🚨 License Review Required - AgriBackup")

            val emailContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 20px; margin-bottom: 20px;">
                        <h2 style="color: #856404; margin-top: 0;">📋 License Review Alert</h2>
                        <p style="margin-bottom: 0;"><strong>$exporterName</strong> has submitted a license for review.</p>
                    </div>

                    <div style="background-color: #f8f9fa; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="color: #2e7d32; margin-top: 0;">License Details:</h3>
                        <table style="width: 100%; border-collapse: collapse;">
                            <tr>
                                <td style="padding: 8px 0; border-bottom: 1px solid #dee2e6;"><strong>Exporter:</strong></td>
                                <td style="padding: 8px 0; border-bottom: 1px solid #dee2e6;">$exporterName</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0; border-bottom: 1px solid #dee2e6;"><strong>License ID:</strong></td>
                                <td style="padding: 8px 0; border-bottom: 1px solid #dee2e6;">$licenseId</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px 0;"><strong>Submitted:</strong></td>
                                <td style="padding: 8px 0;">${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}</td>
                            </tr>
                        </table>
                    </div>

                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://agribackup.com/admin/login" style="background-color: #2e7d32; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">
                            🔍 Review License
                        </a>
                    </div>

                    <div style="background-color: #e8f5e8; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #2e7d32;">
                        <h4 style="color: #2e7d32; margin-top: 0;">Action Required:</h4>
                        <ul style="margin-bottom: 0; line-height: 1.6;">
                            <li>Log in to the admin dashboard</li>
                            <li>Navigate to License Management</li>
                            <li>Review the submitted license documents</li>
                            <li>Approve or reject based on compliance requirements</li>
                        </ul>
                    </div>

                    <p>Dear $adminName,</p>
                    <p>A new license application has been submitted and requires your immediate attention. Please review the application thoroughly to ensure compliance with AgriBackup standards.</p>

                    <p style="margin-bottom: 30px;">Best regards,<br/>
                    <strong>AgriBackup Automated System</strong></p>

                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;"/>

                    <div style="text-align: center; color: #666; font-size: 12px;">
                        <p>This is an automated notification from AgriBackup License Management System.</p>
                        <p>© 2024 AgriBackup. All rights reserved.</p>
                    </div>
                </div>
            """.trimIndent()

            helper.setText(emailContent, true)
            logger.info("Sending license review notification email to $to for exporter: $exporterName")
            javaMailSender.send(message)
            logger.info("License review notification email sent successfully to $to")
        } catch (e: Exception) {
            logger.error("Failed to send license review notification email to $to: ${e.message}", e)
            // Don't throw exception for notification emails to avoid blocking license submission
        }
    }
    @Async
    fun sendEmail(to: String, subject: String, body: String) {
        try {
            val message = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true)
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(body, true) // true indicates HTML content
            logger.info("Sending email to $to with subject: $subject")
            javaMailSender.send(message)
            logger.info("Email sent successfully to $to")
        } catch (e: Exception) {
            logger.error("Failed to send email to $to: ${e.message}", e)
            throw e
        }
    }
}