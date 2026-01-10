package com.agriconnect.farmersportalapis.repository

import com.agriconnect.farmersportalapis.domain.auth.UserProfile
import com.agriconnect.farmersportalapis.domain.supplychain.SupplyChainSupplier
import com.agriconnect.farmersportalapis.domain.supplychain.SupplierType
import com.agriconnect.farmersportalapis.domain.supplychain.SupplierVerificationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SupplyChainSupplierRepository : JpaRepository<SupplyChainSupplier, String> {
    
    fun findByUserProfile(userProfile: UserProfile): SupplyChainSupplier?
    
    fun findByUserProfileId(userProfileId: String): SupplyChainSupplier?
    
    fun findBySupplierType(type: SupplierType): List<SupplyChainSupplier>
    
    fun findByVerificationStatus(status: SupplierVerificationStatus): List<SupplyChainSupplier>
    
    fun findByCountryCode(countryCode: String): List<SupplyChainSupplier>
    
    fun findByIsActiveTrue(): List<SupplyChainSupplier>
    
    fun findByParentSupplier_Id(parentId: String): List<SupplyChainSupplier>
    
    @Query("SELECT s FROM SupplyChainSupplier s WHERE s.parentSupplier IS NULL")
    fun findRootSuppliers(): List<SupplyChainSupplier>
    
    @Query("SELECT s FROM SupplyChainSupplier s WHERE s.parentSupplier IS NULL AND s.isActive = true")
    fun findActiveRootSuppliers(): List<SupplyChainSupplier>
    
    @Query("SELECT COUNT(s) FROM SupplyChainSupplier s WHERE s.parentSupplier.id = :parentId")
    fun countSubSuppliers(parentId: String): Long
    
    @Query("SELECT DISTINCT s.countryCode FROM SupplyChainSupplier s WHERE s.countryCode IS NOT NULL")
    fun findDistinctCountryCodes(): List<String>
    
    fun findBySupplierCode(code: String): SupplyChainSupplier?
    
    @Query("SELECT s FROM SupplyChainSupplier s WHERE LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :name, '%'))")
    fun searchByName(name: String): List<SupplyChainSupplier>
    
    // Find suppliers connected to a specific exporter
    fun findByConnectedExporterId(exporterId: String): List<SupplyChainSupplier>
    
    fun findByConnectedExporterIdAndIsActiveTrue(exporterId: String): List<SupplyChainSupplier>
    
    // Find active suppliers by types (for farmer transfers)
    fun findBySupplierTypeInAndIsActiveTrue(types: List<SupplierType>): List<SupplyChainSupplier>
}
