package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.profile.SystemAdmin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SystemAdminRepository : JpaRepository<SystemAdmin, String> {
    
    fun findByUserProfileId(userId: String): SystemAdmin?
    
    fun findByStatus(status: String): List<SystemAdmin>
    
    @Query("SELECT sa FROM SystemAdmin sa WHERE sa.userProfile.email = :email")
    fun findByEmail(@Param("email") email: String): SystemAdmin?
    
    @Query("SELECT sa FROM SystemAdmin sa WHERE sa.userProfile.phoneNumber = :phoneNumber")
    fun findByPhoneNumber(@Param("phoneNumber") phoneNumber: String): SystemAdmin?

    @Query("SELECT sa FROM Admin sa")
    fun findAllActive(): List<SystemAdmin>
}