package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.profile.Admin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AdminEntityRepository : JpaRepository<Admin, String> {
    fun findByUserProfileId(userId: String): Admin?
    fun findByUserProfile(userProfile: com.agriconnect.farmersportalapis.domain.auth.UserProfile): Admin?
}