package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.application.dtos.contact.ContactFormRequest
import com.agriconnect.farmersportalapis.application.dtos.contact.ContactFormResponse
import com.agriconnect.farmersportalapis.service.common.ContactService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = ["*"])
class ContactController(private val contactService: ContactService) {
    private val logger = LoggerFactory.getLogger(ContactController::class.java)

    @PostMapping("/send")
    fun sendContactMessage(@RequestBody request: ContactFormRequest): ResponseEntity<ContactFormResponse> {
        return try {
            logger.info("Received contact form submission from: ${request.email}")
            
            val result = contactService.sendContactEmail(request)
            
            if (result.success) {
                logger.info("Contact email sent successfully to: ${request.email}")
                ResponseEntity.ok(result)
            } else {
                logger.error("Failed to send contact email: ${result.message}")
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result)
            }
        } catch (e: Exception) {
            logger.error("Error processing contact form: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ContactFormResponse(
                    success = false,
                    message = "An error occurred while sending your message. Please try again later.",
                    data = null
                )
            )
        }
    }
}
