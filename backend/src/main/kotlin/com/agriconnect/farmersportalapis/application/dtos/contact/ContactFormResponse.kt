package com.agriconnect.farmersportalapis.application.dtos.contact

data class ContactFormResponse(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any>? = null
)
