package com.agriconnect.farmersportalapis.controller.supplychain

import com.agriconnect.farmersportalapis.domain.supplychain.ConnectionType
import com.agriconnect.farmersportalapis.domain.supplychain.SupplierType
import com.agriconnect.farmersportalapis.domain.supplychain.SupplierVerificationStatus
import com.agriconnect.farmersportalapis.service.common.impl.EmailService
import com.agriconnect.farmersportalapis.service.supplychain.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * REST API for flexible supply chain management
 * 
 * Provides endpoints for:
 * - Supplier CRUD operations
 * - Supply chain connections (any supplier type to any other)
 * - Supplier invitations via email
 * - Chain visualization and statistics
 */
@RestController
@RequestMapping("/api/v1/supply-chain")
class FlexibleSupplyChainController(
    private val flexibleSupplyChainService: FlexibleSupplyChainService,
    private val emailService: EmailService
) {

    // ==================== SUPPLIER ENDPOINTS ====================

    /**
     * Get all suppliers with optional filtering
     */
    @GetMapping("/suppliers")
    fun getSuppliers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        @RequestParam(required = false) supplierType: String?,
        @RequestParam(required = false) countryCode: String?,
        @RequestParam(defaultValue = "true") isActive: Boolean
    ): ResponseEntity<Any> {
        val type = supplierType?.let { 
            try { SupplierType.valueOf(it.uppercase()) } 
            catch (e: Exception) { null }
        }
        
        val suppliers = flexibleSupplyChainService.getSuppliers(
            page = page,
            size = size,
            supplierType = type,
            countryCode = countryCode,
            isActive = isActive
        )
        
        return ResponseEntity.ok(suppliers)
    }

    /**
     * Get a single supplier by ID
     */
    @GetMapping("/suppliers/{supplierId}")
    fun getSupplier(@PathVariable supplierId: String): ResponseEntity<SupplierDto> {
        val supplier = flexibleSupplyChainService.getSupplier(supplierId)
        return ResponseEntity.ok(supplier)
    }

    /**
     * Create a new supplier
     */
    @PostMapping("/suppliers")
    fun createSupplier(@RequestBody request: CreateSupplierRequestDto): ResponseEntity<SupplierDto> {
        val supplier = flexibleSupplyChainService.createSupplier(
            CreateSupplierRequest(
                supplierName = request.supplierName,
                supplierCode = request.supplierCode,
                supplierType = SupplierType.valueOf(request.supplierType.uppercase()),
                businessRegistrationNumber = request.businessRegistrationNumber,
                taxId = request.taxId,
                countryCode = request.countryCode,
                region = request.region,
                gpsLatitude = request.gpsLatitude,
                gpsLongitude = request.gpsLongitude,
                commoditiesHandled = request.commoditiesHandled,
                certifications = request.certifications,
                capacityPerMonthKg = request.capacityPerMonthKg
            )
        )
        return ResponseEntity.ok(supplier)
    }

    /**
     * Update supplier verification status
     */
    @PutMapping("/suppliers/{supplierId}/verification")
    fun updateVerificationStatus(
        @PathVariable supplierId: String,
        @RequestBody request: UpdateVerificationStatusRequest
    ): ResponseEntity<SupplierDto> {
        val status = SupplierVerificationStatus.valueOf(request.status.uppercase())
        val supplier = flexibleSupplyChainService.updateVerificationStatus(supplierId, status)
        return ResponseEntity.ok(supplier)
    }

    // ==================== CHAIN ENDPOINTS ====================

    /**
     * Get the supply chain for a workflow (ordered list of suppliers)
     */
    @GetMapping("/workflows/{workflowId}/chain")
    fun getWorkflowChain(@PathVariable workflowId: String): ResponseEntity<List<ChainNodeDto>> {
        val chain = flexibleSupplyChainService.getWorkflowChain(workflowId)
        return ResponseEntity.ok(chain)
    }

    /**
     * Get all connections for a workflow
     */
    @GetMapping("/workflows/{workflowId}/connections")
    fun getWorkflowConnections(@PathVariable workflowId: String): ResponseEntity<List<ConnectionDto>> {
        val connections = flexibleSupplyChainService.getWorkflowConnections(workflowId)
        return ResponseEntity.ok(connections)
    }

    /**
     * Create a connection between two suppliers in a workflow
     */
    @PostMapping("/workflows/{workflowId}/connections")
    fun createConnection(
        @PathVariable workflowId: String,
        @RequestBody request: CreateConnectionRequestDto
    ): ResponseEntity<ConnectionDto> {
        val connection = flexibleSupplyChainService.createConnection(
            workflowId = workflowId,
            request = CreateConnectionRequest(
                fromSupplierId = request.fromSupplierId,
                toSupplierId = request.toSupplierId,
                connectionType = ConnectionType.valueOf(request.connectionType.uppercase()),
                quantityKg = request.quantityKg,
                transferDate = request.transferDate,
                batchReference = request.batchReference,
                transportMethod = request.transportMethod,
                transportDocumentRef = request.transportDocumentRef,
                notes = request.notes
            )
        )
        return ResponseEntity.ok(connection)
    }

    /**
     * Get connection statistics for a workflow
     */
    @GetMapping("/workflows/{workflowId}/connections/stats")
    fun getConnectionStats(@PathVariable workflowId: String): ResponseEntity<ConnectionStatsDto> {
        val stats = flexibleSupplyChainService.getConnectionStats(workflowId)
        return ResponseEntity.ok(stats)
    }

    /**
     * Delete a connection (only if not verified on Hedera)
     */
    @DeleteMapping("/connections/{connectionId}")
    fun deleteConnection(@PathVariable connectionId: String): ResponseEntity<Map<String, Any>> {
        val deleted = flexibleSupplyChainService.deleteConnection(connectionId)
        return ResponseEntity.ok(mapOf(
            "success" to deleted,
            "message" to "Connection deleted successfully"
        ))
    }

    // ==================== EXPORTER CONNECTION ENDPOINTS ====================

    /**
     * Get connected suppliers for an exporter
     */
    @GetMapping("/exporter/{exporterId}/connected-suppliers")
    fun getConnectedSuppliers(@PathVariable exporterId: String): ResponseEntity<Any> {
        val suppliers = flexibleSupplyChainService.getConnectedSuppliersForExporter(exporterId)
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "data" to suppliers
        ))
    }

    /**
     * Get sub-suppliers for a supplier (children in hierarchy)
     */
    @GetMapping("/suppliers/{supplierId}/sub-suppliers")
    fun getSubSuppliers(@PathVariable supplierId: String): ResponseEntity<Any> {
        val subSuppliers = flexibleSupplyChainService.getSubSuppliersForSupplier(supplierId)
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "data" to subSuppliers
        ))
    }

    /**
     * Get pending invites sent by a supplier (for sub-supplier invitations)
     */
    @GetMapping("/suppliers/{supplierId}/invites")
    fun getSupplierInvites(@PathVariable supplierId: String): ResponseEntity<Any> {
        val invites = flexibleSupplyChainService.getPendingInvites(supplierId)
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "data" to invites
        ))
    }

    /**
     * Create a direct connection between supplier and exporter
     */
    @PostMapping("/connections")
    fun createDirectConnection(@RequestBody request: DirectConnectionRequestDto): ResponseEntity<Any> {
        return try {
            val result = flexibleSupplyChainService.createDirectSupplierConnection(
                sourceSupplierId = request.sourceSupplierId,
                targetExporterId = request.targetSupplierId,
                connectionType = request.connectionType ?: "SUPPLY_AGREEMENT"
            )
            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to result,
                "message" to "Connection created successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to (e.message ?: "Failed to create connection")
            ))
        }
    }

    /**
     * Delete a direct connection
     */
    @DeleteMapping("/connections/{fromSupplierId}/{toSupplierId}")
    fun deleteDirectConnection(
        @PathVariable fromSupplierId: String,
        @PathVariable toSupplierId: String
    ): ResponseEntity<Any> {
        return try {
            flexibleSupplyChainService.deleteDirectSupplierConnection(fromSupplierId, toSupplierId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Connection deleted successfully"
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to (e.message ?: "Failed to delete connection")
            ))
        }
    }

    // ==================== SUPPLIER INVITE ENDPOINTS ====================

    /**
     * Send an invitation email to a potential supplier
     */
    @PostMapping("/invites")
    fun sendSupplierInvite(@RequestBody request: SupplierInviteRequestDto): ResponseEntity<Any> {
        return try {
            // Support both legacy exporterId and new inviterId
            val effectiveInviterId = request.inviterId ?: request.exporterId
            val effectiveInviterType = request.inviterType ?: "EXPORTER"

            // Store the invite
            val invite = flexibleSupplyChainService.createSupplierInvite(
                email = request.email,
                supplierName = request.supplierName,
                supplierType = request.supplierType?.let { SupplierType.valueOf(it.uppercase()) },
                inviterId = effectiveInviterId,
                inviterType = effectiveInviterType,
                message = request.message
            )

            // Send email - use agribackup.com for production
            val registrationLink = "https://agribackup.com/signup?invite=${invite.id}&type=${request.supplierType ?: "SUPPLIER"}"
            val inviterDisplayName = invite.inviterName ?: "A partner"
            val companyName = invite.inviterCompany ?: "AgriBackup"
            
            val emailBody = buildSupplierInviteEmail(
                recipientName = request.supplierName,
                inviterName = inviterDisplayName,
                companyName = companyName,
                message = request.message,
                registrationLink = registrationLink
            )

            // Build subject line with inviter name
            val emailSubject = "$inviterDisplayName from $companyName wants to connect on AgriBackup"

            emailService.sendEmail(
                to = request.email,
                subject = emailSubject,
                body = emailBody
            )

            ResponseEntity.ok(mapOf(
                "success" to true,
                "data" to invite,
                "message" to "Invitation sent to ${request.email}"
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to (e.message ?: "Failed to send invitation")
            ))
        }
    }

    /**
     * Get pending invites sent by an exporter
     */
    @GetMapping("/invites/pending")
    fun getPendingInvites(
        @RequestParam(required = false) exporterId: String?
    ): ResponseEntity<Any> {
        val invites = flexibleSupplyChainService.getPendingInvites(exporterId)
        return ResponseEntity.ok(mapOf(
            "success" to true,
            "data" to invites
        ))
    }

    /**
     * Cancel a pending invite
     */
    @DeleteMapping("/invites/{inviteId}")
    fun cancelInvite(@PathVariable inviteId: String): ResponseEntity<Any> {
        return try {
            flexibleSupplyChainService.cancelInvite(inviteId)
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Invite cancelled"
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to (e.message ?: "Failed to cancel invite")
            ))
        }
    }

    /**
     * Resend an invite email
     */
    @PostMapping("/invites/{inviteId}/resend")
    fun resendInvite(@PathVariable inviteId: String): ResponseEntity<Any> {
        return try {
            val invite = flexibleSupplyChainService.getInvite(inviteId)
            
            val registrationLink = "https://agribackup.com/signup?invite=${invite.id}&type=${invite.supplierType ?: "SUPPLIER"}"
            val inviterDisplayName = invite.inviterName ?: "A partner"
            val companyName = invite.inviterCompany ?: "AgriBackup"
            
            val emailBody = buildSupplierInviteEmail(
                recipientName = invite.supplierName,
                inviterName = inviterDisplayName,
                companyName = companyName,
                message = invite.message,
                registrationLink = registrationLink
            )

            val emailSubject = "Reminder: $inviterDisplayName from $companyName wants to connect on AgriBackup"

            emailService.sendEmail(
                to = invite.email,
                subject = emailSubject,
                body = emailBody
            )

            flexibleSupplyChainService.markInviteResent(inviteId)

            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "Invitation resent to ${invite.email}"
            ))
        } catch (e: Exception) {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to (e.message ?: "Failed to resend invitation")
            ))
        }
    }

    private fun buildSupplierInviteEmail(
        recipientName: String?,
        inviterName: String,
        companyName: String,
        message: String?,
        registrationLink: String
    ): String {
        val greeting = if (recipientName.isNullOrBlank()) "Hello" else "Hello $recipientName"
        val personalMessage = if (message.isNullOrBlank()) "" else """
            <p style="margin: 20px 0; padding: 15px; background: #f5f5f5; border-left: 4px solid #4CAF50; font-style: italic;">
                "$message"
            </p>
        """
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px; background: #f9fafb;">
                <div style="background: linear-gradient(135deg, #2e7d32 0%, #4caf50 100%); padding: 30px; text-align: center; border-radius: 12px 12px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 28px;">🌱 AgriBackup</h1>
                </div>
                
                <div style="background: white; padding: 32px; border: 1px solid #e5e7eb; border-top: none;">
                    <h2 style="color: #111827; font-size: 22px; margin-bottom: 8px; font-weight: 600;">Welcome to AgriBackup!</h2>
                    
                    <p style="font-size: 16px; color: #374151; margin-bottom: 24px;">
                        <strong style="color: #2e7d32;">$inviterName</strong> from <strong>$companyName</strong> has invited you to join their supply chain network.
                    </p>
                    
                    $personalMessage
                    
                    <p style="font-size: 15px; color: #6b7280;">Click below to accept the invitation and get started:</p>
                    
                    <div style="text-align: center; margin: 32px 0;">
                        <a href="$registrationLink" style="display: inline-block; background: #2e7d32; color: white; padding: 16px 40px; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 16px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                            Accept Invitation
                        </a>
                    </div>
                    
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">
                    
                    <p style="font-size: 14px; color: #6b7280; margin-bottom: 16px;">
                        By joining AgriBackup, you'll be able to:
                    </p>
                    <ul style="color: #6b7280; font-size: 14px; padding-left: 20px;">
                        <li style="margin-bottom: 8px;">Connect with exporters and supply chain partners</li>
                        <li style="margin-bottom: 8px;">Track produce with full traceability</li>
                        <li style="margin-bottom: 8px;">Manage EUDR compliance documentation</li>
                        <li>Build verified, blockchain-backed records</li>
                    </ul>
                    
                    <p style="color: #9ca3af; font-size: 13px; margin-top: 24px;">
                        Questions? Contact us at <a href="mailto:support@agribackup.com" style="color: #2e7d32;">support@agribackup.com</a>
                    </p>
                </div>
                
                <div style="background: #f3f4f6; padding: 20px; text-align: center; font-size: 12px; color: #9ca3af; border-radius: 0 0 12px 12px;">
                    <p style="margin: 0;">© 2025 AgriBackup. All rights reserved.</p>
                    <p style="margin: 8px 0 0 0;">Empowering sustainable agriculture through technology.</p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    // ==================== METADATA ENDPOINTS ====================

    /**
     * Get all supplier types
     */
    @GetMapping("/metadata/supplier-types")
    fun getSupplierTypes(): ResponseEntity<List<SupplierTypeInfo>> {
        val types = SupplierType.entries.map { type ->
            SupplierTypeInfo(
                value = type.name,
                displayName = type.name.replace("_", " ").lowercase()
                    .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } },
                canBeSource = type in listOf(
                    SupplierType.FARMER,
                    SupplierType.FARMER_GROUP,
                    SupplierType.COOPERATIVE,
                    SupplierType.AGGREGATOR,
                    SupplierType.TRADER,
                    SupplierType.PROCESSOR,
                    SupplierType.WAREHOUSE,
                    SupplierType.EXPORTER
                ),
                canBeDestination = type in listOf(
                    SupplierType.AGGREGATOR,
                    SupplierType.TRADER,
                    SupplierType.PROCESSOR,
                    SupplierType.DISTRIBUTOR,
                    SupplierType.WAREHOUSE,
                    SupplierType.EXPORTER,
                    SupplierType.IMPORTER,
                    SupplierType.WHOLESALER,
                    SupplierType.RETAILER
                )
            )
        }
        return ResponseEntity.ok(types)
    }

    /**
     * Get all connection types
     */
    @GetMapping("/metadata/connection-types")
    fun getConnectionTypes(): ResponseEntity<List<ConnectionTypeInfo>> {
        val types = ConnectionType.entries.map { type ->
            ConnectionTypeInfo(
                value = type.name,
                displayName = type.name.replace("_", " ").lowercase()
                    .split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            )
        }
        return ResponseEntity.ok(types)
    }
}

// ==================== REQUEST/RESPONSE DTOs ====================

data class CreateSupplierRequestDto(
    val supplierName: String,
    val supplierCode: String? = null,
    val supplierType: String,
    val businessRegistrationNumber: String? = null,
    val taxId: String? = null,
    val countryCode: String,
    val region: String? = null,
    val gpsLatitude: Double? = null,
    val gpsLongitude: Double? = null,
    val commoditiesHandled: String? = null,
    val certifications: String? = null,
    val capacityPerMonthKg: BigDecimal? = null
)

data class UpdateVerificationStatusRequest(
    val status: String
)

data class CreateConnectionRequestDto(
    val fromSupplierId: String?,
    val toSupplierId: String,
    val connectionType: String,
    val quantityKg: BigDecimal,
    val transferDate: LocalDateTime,
    val batchReference: String? = null,
    val transportMethod: String? = null,
    val transportDocumentRef: String? = null,
    val notes: String? = null
)

data class SupplierTypeInfo(
    val value: String,
    val displayName: String,
    val canBeSource: Boolean,
    val canBeDestination: Boolean
)

data class ConnectionTypeInfo(
    val value: String,
    val displayName: String
)

data class DirectConnectionRequestDto(
    val sourceSupplierId: String,
    val targetSupplierId: String,
    val connectionType: String? = null
)

data class SupplierInviteRequestDto(
    val email: String,
    val supplierName: String? = null,
    val supplierType: String? = null,
    val inviterId: String? = null,  // ID of exporter or supplier sending invite
    val inviterType: String? = null, // "EXPORTER" or "SUPPLIER"
    val message: String? = null,
    // Legacy field for backwards compatibility
    val exporterId: String? = null
)
