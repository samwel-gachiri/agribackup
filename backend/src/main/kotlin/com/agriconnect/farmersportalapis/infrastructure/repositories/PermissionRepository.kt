package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.auth.Permission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PermissionRepository : JpaRepository<Permission, String> {
    
    fun findByName(name: String): Permission?
    
    @Query("SELECT p FROM Permission p JOIN p.roles r WHERE r.id = :roleId")
    fun findByRoleId(@Param("roleId") roleId: String): List<Permission>
    
    @Query("SELECT p FROM Permission p JOIN p.roles r JOIN r.userProfiles u WHERE u.id = :userId")
    fun findByUserId(@Param("userId") userId: String): List<Permission>
    
    fun findByNameIn(names: List<String>): List<Permission>
}