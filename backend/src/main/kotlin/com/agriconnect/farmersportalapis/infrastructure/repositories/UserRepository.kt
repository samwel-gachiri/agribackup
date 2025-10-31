package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.auth.PasswordResetToken
import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserProfile, String> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): UserProfile?
    fun findByPhoneNumber(phoneNumber: String): UserProfile?
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    @Query("""
        SELECT COUNT(u) > 0
        FROM UserProfile u
        JOIN u.roles r
        WHERE u.email = :email AND r.name = :roleName
    """)
    fun existsByEmailAndRole(email: String, roleName: String): Boolean

    @Query("""
        SELECT COUNT(u) > 0
        FROM UserProfile u
        JOIN u.roles r
        WHERE u.phoneNumber = :phoneNumber AND r.name = :roleName
    """)
    fun existsByPhoneNumberAndRole(phoneNumber: String, roleName: String): Boolean
}
@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, Long> {
    fun findByToken(token: String): PasswordResetToken?
    fun deleteByUser(user: UserProfile)
}