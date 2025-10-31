package com.agriconnect.farmersportalapis.infrastructure.repositories

import com.agriconnect.farmersportalapis.domain.common.enums.RequestStatus
import com.agriconnect.farmersportalapis.domain.common.enums.RequestType
import com.agriconnect.farmersportalapis.domain.common.model.FeatureRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface FeatureRequestRepository : JpaRepository<FeatureRequest, Long> {

    @Query("""
        SELECT fr FROM FeatureRequest fr
        WHERE (:status IS NULL OR fr.status = :status)
        AND (:requestType IS NULL OR fr.requestType = :requestType)
        AND (:userSection IS NULL OR fr.userSection = :userSection)
        AND (:userId IS NULL OR fr.userId = :userId)
        ORDER BY fr.createdAt DESC
    """)
    fun findAllFiltered(
        @Param("status") status: RequestStatus?,
        @Param("requestType") requestType: RequestType?,
        @Param("userSection") userSection: String?,
        @Param("userId") userId: Long?,
        pageable: Pageable
    ): Page<FeatureRequest>

    fun findByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<FeatureRequest>
}