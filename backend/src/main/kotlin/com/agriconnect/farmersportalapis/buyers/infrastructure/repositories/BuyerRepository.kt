package com.agriconnect.farmersportalapis.buyers.infrastructure.repositories

import com.agriconnect.farmersportalapis.buyers.domain.profile.Buyer
import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BuyerRepository :  JpaRepository<Buyer, String> {
//    @Query("SELECT b FROM Buyer b WHERE b.id = :idOrUid OR b.uid = :idOrUid")
//    fun findByIdOrUid(@Param("idOrUid") idOrUid: String): Buyer?
    fun findByUserProfile(userProfile: UserProfile): Optional<Buyer>
}