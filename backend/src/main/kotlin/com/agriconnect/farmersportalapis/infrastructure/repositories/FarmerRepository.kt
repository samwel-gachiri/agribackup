package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.profile.Farmer
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FarmerRepository : JpaRepository<Farmer, String> {

//    @Query("SELECT f FROM Farmer f WHERE f.id = :idOrUid OR f.uid = :idOrUid")
//    fun findByIdOrUid(@Param("idOrUid") idOrUid: String): Farmer?
    fun findByUserProfile(userProfile: UserProfile): Optional<Farmer>

    //farmerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
    //                request.searchQuery, request.searchQuery, PageRequest.of(0, 50)
    //            ).content
    @Query("SELECT f FROM Farmer f WHERE LOWER(f.userProfile.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(f.userProfile.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    fun findByNameOrEmailContainingIgnoreCase(searchTerm: String, pageRequest: PageRequest): Page<Farmer>

    @Query("SELECT f FROM Farmer f WHERE f.userProfile.phoneNumber LIKE CONCAT('%', :phoneNumber, '%')")
    fun findByPhoneNumberContaining(phoneNumber: String, pageRequest: PageRequest): Page<Farmer>

    @Query("SELECT DISTINCT f FROM Farmer f JOIN FarmerExporterRelationship fer ON f.id = fer.farmer.id WHERE fer.exporter.id = :exporterId")
    fun findByExporterId(@Param("exporterId") exporterId: String): List<Farmer>
}