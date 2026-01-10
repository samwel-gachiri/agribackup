package com.agriconnect.farmersportalapis.controller.v1

import com.agriconnect.farmersportalapis.service.supplychain.TransferRequestService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * V1 Controller for supplier-specific endpoints
 */
@RestController
@RequestMapping("/api/v1/suppliers")
class SupplierV1Controller(
    private val transferRequestService: TransferRequestService
) {
    
    /**
     * Get inventory for a supplier (confirmed incoming transfers)
     */
    @GetMapping("/{supplierId}/inventory")
    fun getSupplierInventory(@PathVariable supplierId: String): ResponseEntity<Any> {
        val inventory = transferRequestService.getInventoryForSupplier(supplierId)
        return ResponseEntity.ok(inventory)
    }
}
