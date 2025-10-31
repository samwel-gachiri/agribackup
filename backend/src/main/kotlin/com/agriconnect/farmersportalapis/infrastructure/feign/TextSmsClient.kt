package com.agriconnect.farmersportalapis.infrastructure.feign

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(name = "textSmsClient", url = "https://sms.textsms.co.ke")
interface TextSmsClient {
    @PostMapping("/api/services/sendsms/")
    fun sendSms(@RequestBody request: TextSmsRequest): TextSmsResponseWrapper

    @PostMapping("/api/services/sendsms/")
    fun sendSmsRaw(@RequestBody request: TextSmsRequest): String
}

data class TextSmsRequest(
    @JsonProperty("apikey") val apikey: String,
    @JsonProperty("partnerID") val partnerID: String,
    @JsonProperty("message") val message: String,
    @JsonProperty("shortcode") val shortcode: String,
    @JsonProperty("mobile") val mobile: String
)

data class TextSmsResponseWrapper(
    @JsonProperty("responses") val responses: List<TextSmsResponse>
)

data class TextSmsResponse(
    @JsonProperty("response-code") val responseCode: Int?,
    @JsonProperty("response-description") val responseDescription: String?,
    @JsonProperty("mobile") val mobile: Long?,
    @JsonProperty("messageid") val messageid: String?,
    @JsonProperty("networkid") val networkid: Int?
)