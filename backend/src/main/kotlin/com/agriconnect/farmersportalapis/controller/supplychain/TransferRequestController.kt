package com.agriconnect.farmersportalapis.controller.supplychain

import com.agriconnect.farmersportalapis.domain.supplychain.TransferRequest
import com.agriconnect.farmersportalapis.service.supplychain.TransferRequestService
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/v1/transfers")
class TransferRequestController(
    private val transferRequestService: TransferRequestService
) {

    // ==================== CREATE TRANSFERS ====================

    /**
     * Create transfer from Farmer to Supplier
     */
    @PostMapping("/farmer")
    fun createFarmerTransfer(@RequestBody request: FarmerTransferRequest): ResponseEntity<TransferRequest> {
        val transfer = transferRequestService.createFarmerTransfer(
            fromFarmerId = request.fromFarmerId,
            fromProductionUnitId = request.productionUnitId,
            toSupplierId = request.toSupplierId,
            produceType = request.produceType,
            quantityKg = request.quantityKg,
            qualityGrade = request.qualityGrade,
            notes = request.notes,
            farmerName = request.farmerName ?: "Unknown Farmer"
        )
        return ResponseEntity.ok(transfer)
    }

    /**
     * Create transfer from Supplier to Supplier
     */
    @PostMapping
    fun createSupplierTransfer(@RequestBody request: SupplierTransferRequest): ResponseEntity<TransferRequest> {
        val transfer = transferRequestService.createSupplierTransfer(
            fromSupplierId = request.fromSupplierId,
            toSupplierId = request.toSupplierId,
            produceType = request.produceType,
            quantityKg = request.quantityKg,
            qualityGrade = request.qualityGrade,
            notes = request.notes
        )
        return ResponseEntity.ok(transfer)
    }

    // ==================== CONFIRM / DISPUTE / REJECT ====================

    /**
     * Confirm transfer receipt (Two-Party Handshake)
     */
    @PutMapping("/{transferId}/confirm")
    fun confirmTransfer(
        @PathVariable transferId: String,
        @RequestBody request: ConfirmTransferRequest
    ): ResponseEntity<TransferRequest> {
        val transfer = transferRequestService.confirmTransfer(
            transferId = transferId,
            receivedQuantityKg = request.receivedQuantityKg,
            notes = request.notes
        )
        return ResponseEntity.ok(transfer)
    }

    /**
     * Dispute transfer with different quantity
     */
    @PutMapping("/{transferId}/dispute")
    fun disputeTransfer(
        @PathVariable transferId: String,
        @RequestBody request: DisputeTransferRequest
    ): ResponseEntity<TransferRequest> {
        val transfer = transferRequestService.disputeTransfer(
            transferId = transferId,
            actualQuantityKg = request.actualQuantityKg,
            reason = request.reason
        )
        return ResponseEntity.ok(transfer)
    }

    /**
     * Reject transfer
     */
    @PutMapping("/{transferId}/reject")
    fun rejectTransfer(
        @PathVariable transferId: String,
        @RequestBody(required = false) request: RejectTransferRequest?
    ): ResponseEntity<TransferRequest> {
        val transfer = transferRequestService.rejectTransfer(transferId, request?.reason)
        return ResponseEntity.ok(transfer)
    }

    // ==================== GET TRANSFERS ====================

    /**
     * Get incoming transfers for a supplier
     */
    @GetMapping("/incoming/{supplierId}")
    fun getIncomingTransfers(
        @PathVariable supplierId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<TransferRequest>> {
        return ResponseEntity.ok(transferRequestService.getIncomingTransfers(supplierId, page, size))
    }

    /**
     * Get outgoing transfers for a supplier
     */
    @GetMapping("/outgoing/{supplierId}")
    fun getOutgoingTransfers(
        @PathVariable supplierId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<TransferRequest>> {
        return ResponseEntity.ok(transferRequestService.getOutgoingTransfers(supplierId, page, size))
    }

    /**
     * Get outgoing transfers for a farmer
     */
    @GetMapping("/outgoing/farmer/{farmerId}")
    fun getFarmerOutgoingTransfers(
        @PathVariable farmerId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<Page<TransferRequest>> {
        return ResponseEntity.ok(transferRequestService.getFarmerOutgoingTransfers(farmerId, page, size))
    }

    /**
     * Count pending transfers for badge/notification
     */
    @GetMapping("/pending/count/{supplierId}")
    fun countPendingTransfers(@PathVariable supplierId: String): ResponseEntity<Map<String, Long>> {
        val count = transferRequestService.countPendingTransfers(supplierId)
        return ResponseEntity.ok(mapOf("count" to count))
    }
}

// ==================== DTOs ====================

data class FarmerTransferRequest(
    val fromFarmerId: String,
    val productionUnitId: String,
    val toSupplierId: String,
    val produceType: String,
    val quantityKg: BigDecimal,
    val qualityGrade: String? = null,
    val notes: String? = null,
    val farmerName: String? = null
)

data class SupplierTransferRequest(
    val fromSupplierId: String,
    val toSupplierId: String,
    val produceType: String,
    val quantityKg: BigDecimal,
    val qualityGrade: String? = null,
    val notes: String? = null
)

data class ConfirmTransferRequest(
    val receivedQuantityKg: BigDecimal,
    val notes: String? = null
)

data class DisputeTransferRequest(
    val actualQuantityKg: BigDecimal,
    val reason: String
)

data class RejectTransferRequest(
    val reason: String? = null
)
