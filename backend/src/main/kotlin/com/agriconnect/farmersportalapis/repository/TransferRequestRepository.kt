package com.agriconnect.farmersportalapis.repository

import com.agriconnect.farmersportalapis.domain.supplychain.TransferRequest
import com.agriconnect.farmersportalapis.domain.supplychain.TransferStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface TransferRequestRepository : JpaRepository<TransferRequest, String> {

    // Find incoming transfers for a supplier (where they are the recipient)
    fun findByToSupplierIdOrderByCreatedAtDesc(toSupplierId: String, pageable: Pageable): Page<TransferRequest>

    // Find outgoing transfers for a supplier
    fun findByFromSupplierIdOrderByCreatedAtDesc(fromSupplierId: String, pageable: Pageable): Page<TransferRequest>

    // Find outgoing transfers for a farmer
    fun findByFromFarmerIdOrderByCreatedAtDesc(fromFarmerId: String, pageable: Pageable): Page<TransferRequest>

    // Find pending transfers for a recipient
    fun findByToSupplierIdAndStatus(toSupplierId: String, status: TransferStatus): List<TransferRequest>

    // Count pending incoming transfers
    fun countByToSupplierIdAndStatus(toSupplierId: String, status: TransferStatus): Long

    // Count by status for analytics
    @Query("SELECT t.status, COUNT(t) FROM TransferRequest t WHERE t.toSupplierId = :supplierId GROUP BY t.status")
    fun countByStatusForSupplier(supplierId: String): List<Array<Any>>

    // Find disputed transfers
    fun findByStatusOrderByCreatedAtDesc(status: TransferStatus, pageable: Pageable): Page<TransferRequest>

    // Find transfers with discrepancies (receiver quantity differs from sender)
    @Query("SELECT t FROM TransferRequest t WHERE t.receiverQuantityKg IS NOT NULL AND t.receiverQuantityKg != t.senderQuantityKg")
    fun findTransfersWithDiscrepancies(pageable: Pageable): Page<TransferRequest>

    // Find all transfers involving a production unit
    fun findByFromProductionUnitId(productionUnitId: String): List<TransferRequest>

    // Find distinct suppliers a farmer has sent to
    @Query("SELECT DISTINCT t.toSupplierId FROM TransferRequest t WHERE t.fromFarmerId = :farmerId")
    fun findDistinctSupplierIdsByFarmerId(farmerId: String): List<String>
}
