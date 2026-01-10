package com.agriconnect.farmersportalapis.repository

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.eudr.AuthorisedRepresentative
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AuthorisedRepresentativeRepository : JpaRepository<AuthorisedRepresentative, String> {
    
    /**
     * Find AR by EORI number (unique identifier for EU TRACES)
     */
    fun findByEoriNumber(eoriNumber: String): AuthorisedRepresentative?
    
    /**
     * Find AR by user profile ID (for logged-in AR access)
     */
    fun findByUserProfileId(userProfileId: String): AuthorisedRepresentative?
    
    /**
     * Check if EORI number already exists
     */
    fun existsByEoriNumber(eoriNumber: String): Boolean
    
    /**
     * Find all verified and active ARs accepting mandates in a specific EU member state
     */
    fun findByEuMemberStateAndIsVerifiedTrueAndIsActiveTrueAndIsAcceptingMandatesTrue(
        euMemberState: String
    ): List<AuthorisedRepresentative>
    
    /**
     * Find all active ARs
     */
    fun findByIsActiveTrue(): List<AuthorisedRepresentative>
    
    /**
     * Find all verified ARs
     */
    fun findByIsVerifiedTrueAndIsActiveTrue(): List<AuthorisedRepresentative>
    
    /**
     * Search ARs by company name (case-insensitive partial match)
     */
    @Query("SELECT ar FROM AuthorisedRepresentative ar WHERE LOWER(ar.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND ar.isActive = true")
    fun searchByCompanyName(@Param("searchTerm") searchTerm: String): List<AuthorisedRepresentative>
    
    /**
     * Find ARs by registration number
     */
    fun findByRegistrationNumber(registrationNumber: String): AuthorisedRepresentative?
    
    /**
     * Find all ARs that are accepting mandates for a list of commodities
     */
    @Query("""
        SELECT ar FROM AuthorisedRepresentative ar 
        WHERE ar.isActive = true 
        AND ar.isVerified = true 
        AND ar.isAcceptingMandates = true
        ORDER BY ar.companyName
    """)
    fun findAllAvailableARs(): List<AuthorisedRepresentative>

    fun findByUserProfile(userProfile: UserProfile): AuthorisedRepresentative?
}
