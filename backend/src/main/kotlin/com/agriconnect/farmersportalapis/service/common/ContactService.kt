package com.agriconnect.farmersportalapis.service.common

import com.agriconnect.farmersportalapis.application.dtos.contact.ContactFormRequest
import com.agriconnect.farmersportalapis.application.dtos.contact.ContactFormResponse
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Service
class ContactService(
    private val objectMapper: ObjectMapper,
    private val httpClient: OkHttpClient
) {
    private val logger = LoggerFactory.getLogger(ContactService::class.java)

    @Value("\${resend.api.key:}")
    private lateinit var resendApiKey: String

    @Value("\${resend.api.url:https://api.resend.com/emails}")
    private lateinit var resendApiUrl: String

    @Value("\${contact.email:contact@agribackup.com}")
    private lateinit var contactEmail: String

    fun sendContactEmail(request: ContactFormRequest): ContactFormResponse {
        return try {
            if (resendApiKey.isBlank()) {
                logger.error("Resend API key is not configured")
                return ContactFormResponse(
                    success = false,
                    message = "Email service is not properly configured"
                )
            }

            logger.info("Attempting to send email via Resend API")
            logger.debug("Resend API Key configured: ${if (resendApiKey.isNotBlank()) "Yes (${resendApiKey.take(10)}...)" else "No"}")
            
            val emailPayload = buildEmailPayload(request)
            val response = sendEmailViaResend(emailPayload)

            if (response.isSuccessful) {
                logger.info("Email sent successfully via Resend")
                ContactFormResponse(
                    success = true,
                    message = "Your message has been sent successfully. We'll get back to you soon!",
                    data = mapOf("emailId" to (response.body?.string() ?: ""))
                )
            } else {
                val errorBody = response.body?.string() ?: "No error details"
                logger.error("Resend API error: ${response.code} - ${response.message}")
                logger.error("Resend API error body: $errorBody")
                
                // Check if it's a domain verification error
                val errorMessage = if (errorBody.contains("verify a domain", ignoreCase = true)) {
                    "Email service is in testing mode. Please contact support."
                } else {
                    "Failed to send email. Please try again later."
                }
                
                ContactFormResponse(
                    success = false,
                    message = errorMessage
                )
            }
        } catch (e: Exception) {
            logger.error("Error sending contact email: ${e.message}", e)
            ContactFormResponse(
                success = false,
                message = "An error occurred while processing your request"
            )
        }
    }

    private fun buildEmailPayload(request: ContactFormRequest): Map<String, Any> {
        return mapOf(
            "from" to "AgriBackup Contact <onboarding@resend.dev>",
            "to" to listOf(contactEmail),
            "reply_to" to request.email,
            "subject" to "[Contact Form] ${request.subject}",
            "html" to generateEmailTemplate(request),
            "text" to buildPlainTextEmail(request)
        )
    }

    private fun sendEmailViaResend(payload: Map<String, Any>): okhttp3.Response {
        val jsonPayload = objectMapper.writeValueAsString(payload)
        val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(resendApiUrl)
            .addHeader("Authorization", "Bearer $resendApiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        return httpClient.newCall(request).execute()
    }

    private fun buildPlainTextEmail(request: ContactFormRequest): String {
        return """
            Name: ${request.name}
            Email: ${request.email}
            Subject: ${request.subject}
            
            Message:
            ${request.message}
        """.trimIndent()
    }

    private fun generateEmailTemplate(request: ContactFormRequest): String {
        val currentTime = ZonedDateTime.now(ZoneId.of("Africa/Nairobi"))
            .format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' h:mm a z"))

        return """
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>New Contact Form Submission</title>
    <style>
      body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
      .container { max-width: 600px; margin: 0 auto; padding: 20px; }
      .header { background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
      .header h1 { margin: 0; font-size: 24px; }
      .header p { margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; }
      .content { background-color: #ffffff; padding: 40px 30px; border: 1px solid #e0e0e0; border-top: none; }
      .content h2 { color: #2e7d32; font-size: 20px; margin: 0 0 20px 0; }
      .detail-table { width: 100%; margin-bottom: 30px; }
      .detail-row { border-bottom: 1px solid #e0e0e0; }
      .detail-label { padding: 12px 0; font-weight: bold; color: #555; }
      .detail-value { padding: 12px 0; text-align: right; color: #333; }
      .detail-value a { color: #2e7d32; text-decoration: none; }
      .message-section { margin-top: 30px; }
      .message-section h3 { color: #2e7d32; font-size: 18px; margin: 0 0 15px 0; }
      .message-box { background-color: #f9f9f9; padding: 20px; border-radius: 6px; border-left: 4px solid #2e7d32; white-space: pre-wrap; }
      .reply-button { display: inline-block; background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%); color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; margin-top: 20px; }
      .footer { background-color: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; color: #777; border-radius: 0 0 8px 8px; }
    </style>
  </head>
  <body>
    <div class="container">
      <div class="header">
        <h1>AgriBackup</h1>
        <p>New Contact Form Submission</p>
      </div>
      
      <div class="content">
        <h2>Contact Details</h2>
        
        <table class="detail-table">
          <tr class="detail-row">
            <td class="detail-label">Name:</td>
            <td class="detail-value">${request.name}</td>
          </tr>
          <tr class="detail-row">
            <td class="detail-label">Email:</td>
            <td class="detail-value"><a href="mailto:${request.email}">${request.email}</a></td>
          </tr>
          <tr class="detail-row">
            <td class="detail-label">Subject:</td>
            <td class="detail-value">${request.subject}</td>
          </tr>
        </table>
        
        <div class="message-section">
          <h3>Message</h3>
          <div class="message-box">${request.message}</div>
        </div>
        
        <a href="mailto:${request.email}" class="reply-button">Reply to ${request.name}</a>
      </div>
      
      <div class="footer">
        <p>This email was sent from the AgriBackup contact form.</p>
        <p>Received on $currentTime</p>
      </div>
    </div>
  </body>
</html>
        """.trimIndent()
    }
}
