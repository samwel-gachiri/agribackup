package com.agriconnect.farmersportalapis.controller

import com.agriconnect.farmersportalapis.service.common.impl.UssdService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/ussd")
class UssdController(private val ussdService: UssdService) {
    @PostMapping("/process")
    fun processUssdRequest(
        @RequestParam("sessionId") sessionId: String,
        @RequestParam("serviceCode") serviceCode: String,
        @RequestParam("phoneNumber") phoneNumber: String,
        @RequestParam("text") text: String? = "",
    ): String {
        val decodedText = URLDecoder.decode(text, StandardCharsets.UTF_8).trim()
        return ussdService.handleUssdRequest(sessionId, phoneNumber, decodedText)
    }
}