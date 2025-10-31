package com.agriconnect.farmersportalapis.service.common.impl

import com.agriconnect.farmersportalapis.infrastructure.feign.TextSmsClient
import com.agriconnect.farmersportalapis.infrastructure.feign.TextSmsRequest
import com.twilio.Twilio
import com.twilio.rest.api.v2010.account.Message
import com.twilio.type.PhoneNumber
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class SmsService(
    private val textSmsClient: TextSmsClient,
    @Value("\${textsms.apikey}") private val textSmsApiKey: String,
    @Value("\${textsms.partnerID}") private val textSmsPartnerID: String,
    @Value("\${textsms.shortcode}") private val textSmsShortcode: String,
    @Value("\${twilio.account-sid}") private val twilioAccountSid: String,
    @Value("\${twilio.auth-token}") private val twilioAuthToken: String,
    @Value("\${twilio.phone-number}") private val twilioPhoneNumber: String
) {
    private val logger = LoggerFactory.getLogger(SmsService::class.java)

    private fun sendSms(phoneNumber: String, messageBody: String, context: String = "SMS") {
        try {
            if (phoneNumber.startsWith("+254")) {
                val request = TextSmsRequest(
                    apikey = textSmsApiKey,
                    partnerID = textSmsPartnerID,
                    message = messageBody,
                    shortcode = textSmsShortcode,
                    mobile = phoneNumber
                )
                val rawResponse = textSmsClient.sendSmsRaw(request)
                logger.info("$context TextSMS raw response: $rawResponse")

                val responseWrapper = textSmsClient.sendSms(request)
                val response = responseWrapper.responses.firstOrNull()
                    ?: throw RuntimeException("TextSMS API returned empty response list")
                if (response.responseCode == 200) {
                    logger.info("$context TextSMS sent successfully to $phoneNumber, message ID: ${response.messageid ?: "unknown"}")
                } else {
                    val errorMessage = response.responseDescription ?: "Unknown TextSMS API error"
                    logger.error("$context TextSMS failed for $phoneNumber: $errorMessage (responseCode: ${response.responseCode})")
                    throw RuntimeException("TextSMS API error: $errorMessage")
                }
            } else {
                Twilio.init(twilioAccountSid, twilioAuthToken)
                val message = Message.creator(
                    PhoneNumber(phoneNumber),
                    PhoneNumber(twilioPhoneNumber),
                    messageBody
                ).create()
                logger.info("$context Twilio SMS sent successfully to $phoneNumber, message ID: ${message.sid}")
            }
        } catch (e: Exception) {
            logger.error("Failed to send $context SMS to $phoneNumber: ${e.message}", e)
            throw RuntimeException("Failed to send $context SMS: ${e.message}")
        }
    }

    private fun sendSmsTest(phoneNumber: String, message: String, context: String = "Test"): String {
        try {
            if (phoneNumber.startsWith("+254")) {
                val request = TextSmsRequest(
                    apikey = textSmsApiKey,
                    partnerID = textSmsPartnerID,
                    message = message,
                    shortcode = textSmsShortcode,
                    mobile = phoneNumber
                )
                val rawResponse = textSmsClient.sendSmsRaw(request)
                logger.info("$context TextSMS raw response: $rawResponse")

                val responseWrapper = textSmsClient.sendSms(request)
                val response = responseWrapper.responses.firstOrNull()
                    ?: return "FAILED: TextSMS API returned empty response list"
                if (response.responseCode == 200) {
                    logger.info("$context TextSMS sent successfully to $phoneNumber, message ID: ${response.messageid ?: "unknown"}")
                    return "SUCCESS: SMS sent via TextSMS"
                } else {
                    val errorMessage = response.responseDescription ?: "Unknown TextSMS API error"
                    logger.error("$context TextSMS failed for $phoneNumber: $errorMessage (responseCode: ${response.responseCode})")
                    return "FAILED: $errorMessage"
                }
            } else {
                Twilio.init(twilioAccountSid, twilioAuthToken)
                val smsMessage = Message.creator(
                    PhoneNumber(phoneNumber),
                    PhoneNumber(twilioPhoneNumber),
                    message
                ).create()
                logger.info("$context Twilio SMS sent successfully to $phoneNumber, message ID: ${smsMessage.sid}")
                return "SUCCESS: SMS sent via Twilio"
            }
        } catch (e: Exception) {
            logger.error("Failed to send $context SMS to $phoneNumber: ${e.message}", e)
            return "FAILED: ${e.message}"
        }
    }

    @Async
    fun sendPasswordResetSms(phoneNumber: String, otp: String, resetLink: String) {
        val messageBody = "Your OTP is: $otp. Reset your password: $resetLink\nThis OTP expires in 1 hour."
        sendSms(phoneNumber, messageBody, "Password Reset")
    }

    @Async
    fun sendWelcomeSms(phoneNumber: String, fullName: String, roleType: String) {
        val roleDisplayName = when (roleType.uppercase()) {
            "FARMER" -> "Farmer"
            "BUYER" -> "Buyer"
            "EXPORTER" -> "Exporter"
            else -> "User"
        }
        val messageBody = "Welcome to AgriBackup, $fullName! 🎉\n\nYour $roleDisplayName account has been created successfully. You can now access all features of our agricultural marketplace.\n\nHappy farming! 🌱"
        sendSms(phoneNumber, messageBody, "Welcome")
    }

    @Async
    fun sendLicenseReviewNotificationSms(phoneNumber: String, exporterName: String, licenseId: String) {
        val messageBody = "🚨 NEW LICENSE REVIEW ALERT 🚨\n\n$exporterName has submitted a license for review.\nLicense ID: $licenseId\n\nPlease review at: https://agribackup.com/admin/login\n\n- AgriBackup System"
        sendSms(phoneNumber, messageBody, "License Review Notification")
    }
    fun testSmsDirect(phoneNumber: String, message: String = "Test SMS from AgriBackup API"): String {
        logger.info("Testing SMS directly to: $phoneNumber")
        return sendSmsTest(phoneNumber, message, "Test")
    }
}