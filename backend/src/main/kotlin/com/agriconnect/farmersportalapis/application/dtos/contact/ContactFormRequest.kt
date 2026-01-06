package com.agriconnect.farmersportalapis.application.dtos.contact

data class ContactFormRequest(
    val name: String,
    val email: String,
    val subject: String,
    val message: String
)
