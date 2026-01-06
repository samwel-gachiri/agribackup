package com.agriconnect.farmersportalapis.application.dtos

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Consumer-safe DTO for public batch verification.
 * Contains only information safe to share publicly - no sensitive business data.
 */
data class PublicBatchVerificationDto(
    val verified: Boolean,
    val batchCode: String,
    val product: ProductInfo,
    val origin: OriginInfo,
    val compliance: ComplianceInfo,
    val supplyChainJourney: List<JourneyStep>,
    val blockchain: BlockchainProof,
    val verifiedAt: LocalDateTime
)

data class ProductInfo(
    val commodity: String,
    val quantity: BigDecimal,
    val unit: String,
    val harvestDate: LocalDate?,
    val harvestPeriodStart: LocalDate?,
    val harvestPeriodEnd: LocalDate?
)

data class OriginInfo(
    val country: String,
    val farmerCount: Int,
    val productionUnitCount: Int,
    val isDeforestationFree: Boolean
)

data class ComplianceInfo(
    val eudrCompliant: Boolean,
    val riskLevel: String?,
    val status: String,
    val lastAssessedAt: LocalDateTime?
)

data class JourneyStep(
    val step: Int,
    val event: String,
    val location: String?,
    val date: LocalDateTime?,
    val actor: String?
)

data class BlockchainProof(
    val network: String,
    val transactionId: String?,
    val consensusTimestamp: Instant?,
    val explorerUrl: String?
)
