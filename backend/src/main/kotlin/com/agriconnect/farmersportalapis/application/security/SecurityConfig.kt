package com.agriconnect.farmersportalapis.application.security

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val env: Environment // Inject Environment to check active profiles
) {

    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        // Allow cron-job.org and existing origins
        configuration.allowedOrigins = listOf(
            "http://localhost:3030",
            "http://localhost:8080",
            "https://www.agribackup.com",
            "https://cron-job.org"
        )
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/admin/**").permitAll()
                    .requestMatchers("/ping").permitAll()
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    // Allow public access to produce endpoints for registration
                    .requestMatchers("/farmers-service/produce").permitAll()
                    .requestMatchers("/buyers-service/produce").permitAll()

                    // Admin service zone listing - allow either specific permissions OR roles
                    // Temporarily commented out role-based access - allow authenticated users
//
//                    .requestMatchers(HttpMethod.GET, "/api/admin-service/zones", "/api/admin-service/zones/**")
//                        .hasAnyAuthority(
//                            "VIEW_ZONE_SUPERVISOR", "ADD_FARMER",
//                            "ROLE_SYSTEM_ADMIN", "ROLE_EXPORTER", "ROLE_ZONE_SUPERVISOR"
//                        )
//
//                    // Other admin-service endpoints restricted by role (ROLE_ prefix already added in JWT filter)
//                    .requestMatchers("/api/admin-service/**")
//                        .hasAnyAuthority("ROLE_SYSTEM_ADMIN", "ROLE_EXPORTER", "ROLE_ZONE_SUPERVISOR")
//
//                    // Role-based access for custom prefixes (convert to roles consistently)
//                    .requestMatchers("/farmers-service/**").hasRole("FARMER")
//                    .requestMatchers("/buyers-service/**").hasRole("BUYER")
//                    .requestMatchers("/exporters-service/**").hasRole("EXPORTER")

                    // Authenticated endpoints - allow all authenticated users for now
                    .requestMatchers("/connection-service/**").authenticated()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}