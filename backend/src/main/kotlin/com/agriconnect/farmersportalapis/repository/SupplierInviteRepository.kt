package com.agriconnect.farmersportalapis.repository

import com.agriconnect.farmersportalapis.domain.supplychain.InviteStatus
import com.agriconnect.farmersportalapis.domain.supplychain.SupplierInvite
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SupplierInviteRepository : JpaRepository<SupplierInvite, String> {
    
    fun findByEmail(email: String): List<SupplierInvite>
    
    fun findByInviterId(inviterId: String): List<SupplierInvite>
    
    fun findByStatus(status: InviteStatus): List<SupplierInvite>
    
    fun findByInviterIdAndStatus(inviterId: String, status: InviteStatus): List<SupplierInvite>
    
    fun findByEmailAndStatus(email: String, status: InviteStatus): SupplierInvite?
}
