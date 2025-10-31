
package com.agriconnect.farmersportalapis.application.security

import com.agriconnect.farmersportalapis.application.util.JwtUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestUri = request.requestURI
        logger.info("=== JWT Filter Processing Request: {} {} ===", request.method, requestUri)

        val token = extractTokenFromRequest(request)
        logger.info("Token extracted: {}", if (token != null) "Present (${token.take(20)}...)" else "Not found")

        if (token != null && SecurityContextHolder.getContext().authentication == null) {
            try {
                logger.info("Processing JWT token...")

                val roleSpecificId = jwtUtil.getRoleSpecificIdFromToken(token)
                val role = jwtUtil.getRoleFromToken(token)
                val userDetails = jwtUtil.getUserDetailsFromToken(token)

                logger.info("Extracted from JWT:")
                logger.info("  - Role Specific ID (subject): {}", roleSpecificId)
                logger.info("  - Role: {}", role)
                logger.info("  - User Details: {}", userDetails)

                if (roleSpecificId != null && role != null) {
                    // Extract permissions and roles from JWT token
                    val permissions = userDetails["permissions"] as? List<String> ?: emptyList()
                    val roles = userDetails["roles"] as? List<String> ?: emptyList()

                    logger.info("  - Roles from JWT: {}", roles)
                    logger.info("  - Permissions from JWT: {}", permissions)

                    // Create authorities from both roles and permissions
                    val authorities = mutableSetOf<SimpleGrantedAuthority>()

                    // Add role authorities
                    roles.forEach { roleAuthority ->
                        authorities.add(SimpleGrantedAuthority(roleAuthority))
                        logger.info("    Added role authority: {}", roleAuthority)
                    }

                    // Add permission authorities
                    permissions.forEach { permission ->
                        authorities.add(SimpleGrantedAuthority(permission))
                        logger.info("    Added permission authority: {}", permission)
                    }

                    logger.info("Total authorities set: {}", authorities)

                    // Create authentication token with all authorities
                    val authToken = UsernamePasswordAuthenticationToken(
                        roleSpecificId,
                        null,
                        authorities
                    )

                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken

                    logger.info("Authentication set successfully for user: {}", roleSpecificId)
                    logger.info("Final authorities in context: {}",
                        SecurityContextHolder.getContext().authentication?.authorities)
                } else {
                    logger.warn("Missing roleSpecificId or role in JWT token")
                }
            } catch (e: Exception) {
                logger.error("JWT authentication failed for request {}: {}", requestUri, e.message, e)
                SecurityContextHolder.clearContext()
            }
        } else {
            logger.info("Skipping JWT processing: token={}, existingAuth={}",
                token != null,
                SecurityContextHolder.getContext().authentication != null)
        }

        logger.info("=== End JWT Filter Processing ===")
        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        logger.info("Authorization header: {}", if (bearerToken != null) "Bearer ***" else "Not present")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}