package com.agriconnect.farmersportalapis.application.util

import com.agriconnect.farmersportalapis.domain.auth.Permission
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil {

    @Value("\${jwt.secret}")
    private lateinit var secret: String

//    @Value("\${jwt.expiration}")
    private var expiration: Long = 86400

    fun generateToken(
        userDetails: UserDetails,
        role: String,
        roleSpecificId: String,
        name: String,
        email: String?,
        phoneNumber: String?,
        permissions: MutableSet<Permission>?,
    ): String {
        val claims = Jwts.claims().setSubject(roleSpecificId) // Set sub to roleSpecificId
        claims["sub"] = roleSpecificId
        claims["role"] = role
        claims["user"] = mapOf(
            "name" to name,
            "email" to (email ?: ""),
            "phone_number" to (phoneNumber ?: ""),
            "roles" to userDetails.authorities.map { it.authority },
            "permissions" to permissions.orEmpty().map { it.name }
        )
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration * 1000))
            .signWith(SignatureAlgorithm.HS512, secret.toByteArray())
            .compact()
    }

    fun getUsernameFromToken(token: String): String? {
        return getClaimsFromToken(token).subject // Returns roleSpecificId
    }

    fun getRoleFromToken(token: String): String? {
        return getClaimsFromToken(token)["role"] as? String
    }

    fun getRoleSpecificIdFromToken(token: String): String? {
        return getClaimsFromToken(token).subject // Subject is roleSpecificId
    }

    val logger = LoggerFactory.getLogger(JwtUtil::class.java)

    fun getUserDetailsFromToken(token: String): Map<String, Any> {
        val userDetails = getClaimsFromToken(token)["user"] as Map<String, Any>
        logger.info("User details: {}", userDetails)
        return userDetails
    }

    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        val username = getUsernameFromToken(token) // username is roleSpecificId
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun getClaimsFromToken(token: String): Claims {
        return Jwts.parser()
            .setSigningKey(secret.toByteArray())
            .parseClaimsJws(token)
            .body
    }

    private fun isTokenExpired(token: String): Boolean {
        val expiration = getClaimsFromToken(token).expiration
        return expiration.before(Date())
    }
}