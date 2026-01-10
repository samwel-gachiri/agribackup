package com.agriconnect.farmersportalapis.service.supplychain

import com.agriconnect.farmersportalapis.domain.supplychain.*
import com.agriconnect.farmersportalapis.infrastructure.repositories.ExporterRepository
import com.agriconnect.farmersportalapis.repository.SupplierConnectionRepository
import com.agriconnect.farmersportalapis.repository.SupplierInviteRepository
import com.agriconnect.farmersportalapis.repository.SupplyChainSupplierRepository
import com.agriconnect.farmersportalapis.repository.SupplyChainWorkflowRepository
import com.agriconnect.farmersportalapis.service.hedera.AsyncHederaService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Service for managing flexible supply chain with any supplier types
 * 
 * Supports:
 * - Farmer → Supplier(A) → Supplier(B) → ... → Supplier(N)
 * - Any supplier type can connect to any other supplier type
 * - Dynamic chain length instead of fixed stages
 */
@Service
class FlexibleSupplyChainService(
    private val supplierRepository: SupplyChainSupplierRepository,
    private val connectionRepository: SupplierConnectionRepository,
    private val workflowRepository: SupplyChainWorkflowRepository,
    private val inviteRepository: SupplierInviteRepository,
    private val exporterRepository: ExporterRepository,
    private val asyncHederaService: AsyncHederaService?,
    private val transferRequestRepository: com.agriconnect.farmersportalapis.repository.TransferRequestRepository,
    private val farmerCollectionRepository: com.agriconnect.farmersportalapis.infrastructure.repositories.FarmerCollectionRepository
) {
    private val log = LoggerFactory.getLogger(FlexibleSupplyChainService::class.java)

    // ==================== SUPPLIER MANAGEMENT ====================

    /**
     * Get all active suppliers with optional filtering
     */
    fun getSuppliers(
        page: Int = 0,
        size: Int = 50,
        supplierType: SupplierType? = null,
        countryCode: String? = null,
        isActive: Boolean = true
    ): Page<SupplierDto> {
        val pageable = PageRequest.of(page, size)
        
        val suppliers = if (isActive) {
            supplierRepository.findByIsActiveTrue()
        } else {
            supplierRepository.findAll()
        }
        
        val filtered = suppliers.filter { supplier ->
            (supplierType == null || supplier.supplierType == supplierType) &&
            (countryCode == null || supplier.countryCode == countryCode)
        }
        
        val dtos = filtered.map { it.toDto() }
        
        // Manual pagination
        val start = page * size
        val end = minOf(start + size, dtos.size)
        val pagedContent = if (start < dtos.size) dtos.subList(start, end) else emptyList()
        
        return org.springframework.data.domain.PageImpl(
            pagedContent,
            pageable,
            dtos.size.toLong()
        )
    }

    /**
     * Get all active suppliers as a simple list
     */
    fun getAllActiveSuppliers(): List<SupplierDto> {
        return supplierRepository.findByIsActiveTrue().map { it.toDto() }
    }

    /**
     * Get a single supplier by ID
     */
    fun getSupplier(supplierId: String): SupplierDto {
        val supplier = supplierRepository.findById(supplierId)
            .orElseThrow { IllegalArgumentException("Supplier not found: $supplierId") }
        return supplier.toDto()
    }

    /**
     * Create a new supplier
     */
    @Transactional
    fun createSupplier(request: CreateSupplierRequest): SupplierDto {
        val supplier = SupplyChainSupplier(
            supplierName = request.supplierName,
            supplierCode = request.supplierCode,
            supplierType = request.supplierType,
            businessRegistrationNumber = request.businessRegistrationNumber,
            taxId = request.taxId,
            countryCode = request.countryCode,
            region = request.region,
            gpsLatitude = request.gpsLatitude,
            gpsLongitude = request.gpsLongitude,
            commoditiesHandled = request.commoditiesHandled,
            certifications = request.certifications,
            capacityPerMonthKg = request.capacityPerMonthKg,
            verificationStatus = SupplierVerificationStatus.PENDING
        )
        
        val saved = supplierRepository.save(supplier)
        log.info("Created supplier: ${saved.id} - ${saved.supplierName} (${saved.supplierType})")
        
        return saved.toDto()
    }

    /**
     * Update supplier verification status
     */
    @Transactional
    fun updateVerificationStatus(supplierId: String, status: SupplierVerificationStatus): SupplierDto {
        val supplier = supplierRepository.findById(supplierId)
            .orElseThrow { IllegalArgumentException("Supplier not found: $supplierId") }
        
        supplier.verificationStatus = status
        if (status == SupplierVerificationStatus.VERIFIED) {
            supplier.verifiedAt = LocalDateTime.now()
        }
        supplier.updatedAt = LocalDateTime.now()
        
        val updated = supplierRepository.save(supplier)
        log.info("Updated supplier ${supplierId} verification status to $status")
        
        return updated.toDto()
    }

    // ==================== CHAIN MANAGEMENT ====================

    /**
     * Get the supply chain for a workflow (ordered list of suppliers with connections)
     */
    fun getWorkflowChain(workflowId: String): List<ChainNodeDto> {
        val connections = connectionRepository.findByWorkflowIdOrderByCreatedAtAsc(workflowId)
        
        if (connections.isEmpty()) {
            return emptyList()
        }
        
        // Build ordered chain from connections
        val chainNodes = mutableListOf<ChainNodeDto>()
        val processedSupplierIds = mutableSetOf<String>()
        
        for (connection in connections) {
            // Add source if not already in chain
            connection.fromSupplier?.let { fromSupplier ->
                if (fromSupplier.id !in processedSupplierIds) {
                    chainNodes.add(ChainNodeDto(
                        id = fromSupplier.id,
                        supplierName = fromSupplier.supplierName,
                        supplierType = fromSupplier.supplierType.name,
                        supplierCode = fromSupplier.supplierCode,
                        countryCode = fromSupplier.countryCode,
                        region = fromSupplier.region,
                        verificationStatus = fromSupplier.verificationStatus.name,
                        connectionInfo = null // No incoming connection for source
                    ))
                    processedSupplierIds.add(fromSupplier.id)
                }
            }
            
            // Add destination with connection info
            if (connection.toSupplier.id !in processedSupplierIds) {
                chainNodes.add(ChainNodeDto(
                    id = connection.toSupplier.id,
                    supplierName = connection.toSupplier.supplierName,
                    supplierType = connection.toSupplier.supplierType.name,
                    supplierCode = connection.toSupplier.supplierCode,
                    countryCode = connection.toSupplier.countryCode,
                    region = connection.toSupplier.region,
                    verificationStatus = connection.toSupplier.verificationStatus.name,
                    connectionInfo = ConnectionInfoDto(
                        connectionId = connection.id,
                        quantityKg = connection.quantityKg,
                        connectionType = connection.connectionType.name,
                        transferDate = connection.transferDate,
                        batchReference = connection.batchReference,
                        hederaTransactionId = connection.hederaTransactionId
                    )
                ))
                processedSupplierIds.add(connection.toSupplier.id)
            }
        }
        
        return chainNodes
    }

    /**
     * Create a connection between two suppliers in a workflow
     */
    @Transactional
    fun createConnection(workflowId: String, request: CreateConnectionRequest): ConnectionDto {
        val workflow = workflowRepository.findById(workflowId)
            .orElseThrow { IllegalArgumentException("Workflow not found: $workflowId") }
        
        val fromSupplier = request.fromSupplierId?.let { 
            supplierRepository.findById(it)
                .orElseThrow { IllegalArgumentException("Source supplier not found: $it") }
        }
        
        val toSupplier = supplierRepository.findById(request.toSupplierId)
            .orElseThrow { IllegalArgumentException("Destination supplier not found: ${request.toSupplierId}") }
        
        // Check if connection already exists
        if (fromSupplier != null) {
            val existingConnection = connectionRepository.findByWorkflowAndSuppliers(
                workflowId, fromSupplier.id, toSupplier.id
            )
            if (existingConnection != null) {
                throw IllegalArgumentException("Connection already exists between these suppliers in this workflow")
            }
        }
        
        val connection = SupplierConnection(
            workflow = workflow,
            fromSupplier = fromSupplier,
            toSupplier = toSupplier,
            connectionType = request.connectionType,
            quantityKg = request.quantityKg,
            transferDate = request.transferDate,
            batchReference = request.batchReference,
            transportMethod = request.transportMethod,
            transportDocumentRef = request.transportDocumentRef,
            notes = request.notes
        )
        
        val saved = connectionRepository.save(connection)
        
        log.info("Created connection in workflow $workflowId: ${fromSupplier?.supplierName ?: "Origin"} → ${toSupplier.supplierName}")
        
        // TODO: Add Hedera recording for supply chain connections when recordSupplyChainEvent method is implemented
        // For now, connections are recorded in database only
        
        return saved.toDto()
    }

    /**
     * Get all connections for a workflow
     */
    fun getWorkflowConnections(workflowId: String): List<ConnectionDto> {
        return connectionRepository.findByWorkflowIdOrderByCreatedAtAsc(workflowId)
            .map { it.toDto() }
    }

    /**
     * Get connection statistics for a workflow
     */
    fun getConnectionStats(workflowId: String): ConnectionStatsDto {
        val connections = connectionRepository.findByWorkflowId(workflowId)
        
        val totalQuantity = connections.sumOf { it.quantityKg }
        val verifiedCount = connections.count { it.hederaTransactionId != null }
        
        // Group by connection type
        val byType = connections.groupBy { it.connectionType }
            .mapValues { (_, list) -> 
                ConnectionTypeStats(
                    count = list.size,
                    totalQuantityKg = list.sumOf { it.quantityKg }
                )
            }
        
        // Get unique suppliers in chain
        val supplierIds = connections.flatMap { listOfNotNull(it.fromSupplier?.id, it.toSupplier.id) }.toSet()
        
        return ConnectionStatsDto(
            workflowId = workflowId,
            totalConnections = connections.size,
            totalQuantityKg = totalQuantity,
            verifiedOnHedera = verifiedCount,
            uniqueSuppliers = supplierIds.size,
            connectionsByType = byType.mapKeys { it.key.name }
        )
    }

    /**
     * Delete a connection (only if not verified on Hedera)
     */
    @Transactional
    fun deleteConnection(connectionId: String): Boolean {
        val connection = connectionRepository.findById(connectionId)
            .orElseThrow { IllegalArgumentException("Connection not found: $connectionId") }
        
        if (connection.hederaTransactionId != null) {
            throw IllegalStateException("Cannot delete connection that has been verified on blockchain")
        }
        
        connectionRepository.delete(connection)
        log.info("Deleted connection: $connectionId")
        
        return true
    }

    // ==================== EXPORTER CONNECTION MANAGEMENT ====================

    /**
     * Get all suppliers connected to an exporter
     */
    fun getConnectedSuppliersForExporter(exporterId: String): List<SupplierDto> {
        // Get suppliers connected to this exporter using the new connectedExporterId field
        val suppliers = supplierRepository.findByConnectedExporterId(exporterId)
        return suppliers.map { it.toDto() }
    }

    /**
     * Get sub-suppliers (children) for a supplier
     */
    fun getSubSuppliersForSupplier(supplierId: String): List<SupplierDto> {
        val supplier = supplierRepository.findById(supplierId)
            .orElseThrow { IllegalArgumentException("Supplier not found: $supplierId") }
        
        // Get all suppliers where parentSupplier = this supplier
        val subSuppliers = supplierRepository.findByParentSupplier_Id(supplierId)
        log.info("Found ${subSuppliers.size} sub-suppliers for supplier $supplierId")
        return subSuppliers.map { it.toDto() }
    }

    /**
     * Get suppliers a farmer can send to
     * Returns ALL suppliers that accept from farmers, with recently connected ones at the top
     */
    fun getSuppliersAcceptingFromFarmers(farmerId: String, supplierTypes: List<String>): List<SupplierDto> {
        val types = supplierTypes.mapNotNull { 
            try { SupplierType.valueOf(it.uppercase()) } 
            catch (e: Exception) { null }
        }
        
        // Get IDs of recently connected suppliers (from transfers and collections)
        val recentSupplierIds = mutableSetOf<String>()
        
        // 1. Get suppliers the farmer has sent transfers to
        val transferSupplierIds = transferRequestRepository.findDistinctSupplierIdsByFarmerId(farmerId)
        recentSupplierIds.addAll(transferSupplierIds)
        
        // 2. Get aggregators that have collected from this farmer
        val aggregators = farmerCollectionRepository.findDistinctAggregatorsByFarmerId(farmerId)
        for (agg in aggregators) {
            agg.userProfile?.id?.let { userId ->
                supplierRepository.findByUserProfileId(userId)?.let { supplier ->
                    recentSupplierIds.add(supplier.id)
                }
            }
        }
        
        // 3. Get ALL active suppliers of the specified types
        val allSuppliers = supplierRepository.findBySupplierTypeInAndIsActiveTrue(types)
        
        // 4. Sort: recent suppliers first, then alphabetically
        val sortedSuppliers = allSuppliers.sortedWith(
            compareByDescending<SupplyChainSupplier> { it.id in recentSupplierIds }
                .thenBy { it.supplierName }
        )
        
        log.info("Found ${sortedSuppliers.size} suppliers for farmer $farmerId (${recentSupplierIds.size} recent)")
        return sortedSuppliers.map { it.toDto() }
    }

    /**
     * Create a direct connection between a supplier and exporter (not workflow-based)
     * This links a supplier to an exporter so they can use them in workflows
     */
    @Transactional
    fun createDirectSupplierConnection(
        sourceSupplierId: String,
        targetExporterId: String,
        connectionType: String
    ): Map<String, Any> {
        val sourceSupplier = supplierRepository.findById(sourceSupplierId)
            .orElseThrow { IllegalArgumentException("Source supplier not found: $sourceSupplierId") }
        
        // Check if already connected to this exporter
        if (sourceSupplier.connectedExporterId == targetExporterId) {
            throw IllegalArgumentException("Supplier is already connected to this exporter")
        }
        
        // Connect the supplier to the exporter
        sourceSupplier.connectedExporterId = targetExporterId
        sourceSupplier.updatedAt = LocalDateTime.now()
        supplierRepository.save(sourceSupplier)
        
        log.info("Connected supplier $sourceSupplierId to exporter $targetExporterId")
        
        return mapOf(
            "id" to sourceSupplier.id,
            "sourceSupplierId" to sourceSupplierId,
            "targetExporterId" to targetExporterId,
            "connectionType" to connectionType,
            "createdAt" to LocalDateTime.now().toString()
        )
    }

    /**
     * Delete a direct connection between supplier and exporter
     */
    @Transactional
    fun deleteDirectSupplierConnection(supplierId: String, exporterId: String) {
        val supplier = supplierRepository.findById(supplierId)
            .orElseThrow { IllegalArgumentException("Supplier not found: $supplierId") }
        
        if (supplier.connectedExporterId != exporterId) {
            throw IllegalArgumentException("Supplier $supplierId is not connected to exporter $exporterId")
        }
        
        supplier.connectedExporterId = null
        supplier.updatedAt = LocalDateTime.now()
        supplierRepository.save(supplier)
        
        log.info("Disconnected supplier $supplierId from exporter $exporterId")
    }

    // ==================== SUPPLIER INVITE MANAGEMENT ====================

    /**
     * Create a supplier invite - supports both exporter and supplier inviters
     */
    @Transactional
    fun createSupplierInvite(
        email: String,
        supplierName: String?,
        supplierType: SupplierType?,
        inviterId: String?,
        inviterType: String = "EXPORTER",
        message: String?
    ): SupplierInviteDto {
        // Check if invite already exists for this email
        val existingInvite = inviteRepository.findByEmailAndStatus(email, InviteStatus.PENDING)
        if (existingInvite != null) {
            throw IllegalArgumentException("An invite is already pending for this email")
        }
        
        // Get inviter info based on inviter type
        var inviterName: String? = null
        var inviterCompany: String? = null
        var parentSupplierId: String? = null

        if (inviterId != null) {
            if (inviterType == "SUPPLIER") {
                // Supplier inviting a sub-supplier
                val inviterSupplier = supplierRepository.findById(inviterId).orElse(null)
                if (inviterSupplier != null) {
                    inviterName = inviterSupplier.getContactPerson() ?: inviterSupplier.supplierName
                    inviterCompany = inviterSupplier.supplierName
                    parentSupplierId = inviterId  // Set parent relationship for sub-supplier
                }
            } else {
                // Exporter inviting a supplier (existing flow)
                val exporter = exporterRepository.findById(inviterId).orElse(null)
                if (exporter != null) {
                    inviterName = exporter.userProfile?.fullName ?: exporter.companyName
                    inviterCompany = exporter.companyName
                }
            }
        }
        
        val invite = SupplierInvite(
            email = email,
            supplierName = supplierName,
            supplierType = supplierType,
            inviterId = inviterId,
            inviterName = inviterName,
            inviterCompany = inviterCompany,
            message = message,
            parentSupplierId = parentSupplierId,
            inviterType = inviterType,
            status = InviteStatus.PENDING,
            lastSentAt = LocalDateTime.now()
        )
        
        val saved = inviteRepository.save(invite)
        log.info("Created supplier invite: ${saved.id} for email: $email (inviterType: $inviterType, parentSupplierId: $parentSupplierId)")
        
        return saved.toDto()
    }

    /**
     * Get pending invites (optionally filtered by exporter)
     */
    fun getPendingInvites(exporterId: String?): List<SupplierInviteDto> {
        val invites = if (exporterId != null) {
            inviteRepository.findByInviterIdAndStatus(exporterId, InviteStatus.PENDING)
        } else {
            inviteRepository.findByStatus(InviteStatus.PENDING)
        }
        return invites.map { it.toDto() }
    }

    /**
     * Get a single invite by ID
     */
    fun getInvite(inviteId: String): SupplierInviteDto {
        val invite = inviteRepository.findById(inviteId)
            .orElseThrow { IllegalArgumentException("Invite not found: $inviteId") }
        return invite.toDto()
    }

    /**
     * Cancel a pending invite
     */
    @Transactional
    fun cancelInvite(inviteId: String) {
        val invite = inviteRepository.findById(inviteId)
            .orElseThrow { IllegalArgumentException("Invite not found: $inviteId") }
        
        if (invite.status != InviteStatus.PENDING) {
            throw IllegalStateException("Can only cancel pending invites")
        }
        
        invite.status = InviteStatus.CANCELLED
        inviteRepository.save(invite)
        
        log.info("Cancelled invite: $inviteId")
    }

    /**
     * Mark invite as resent (increment counter)
     */
    @Transactional
    fun markInviteResent(inviteId: String) {
        val invite = inviteRepository.findById(inviteId)
            .orElseThrow { IllegalArgumentException("Invite not found: $inviteId") }
        
        invite.resentCount++
        invite.lastSentAt = LocalDateTime.now()
        inviteRepository.save(invite)
        
        log.info("Marked invite as resent: $inviteId (count: ${invite.resentCount})")
    }

    /**
     * Accept an invite (called when user registers via invite link)
     */
    @Transactional
    fun acceptInvite(inviteId: String, supplierId: String) {
        val invite = inviteRepository.findById(inviteId)
            .orElseThrow { IllegalArgumentException("Invite not found: $inviteId") }
        
        if (invite.status != InviteStatus.PENDING) {
            throw IllegalStateException("Invite is no longer pending")
        }
        
        invite.status = InviteStatus.ACCEPTED
        invite.acceptedAt = LocalDateTime.now()
        inviteRepository.save(invite)
        
        val newSupplier = supplierRepository.findById(supplierId).orElse(null)
        
        // Handle based on inviter type
        if (invite.inviterType == "SUPPLIER" && invite.parentSupplierId != null) {
            // Sub-supplier invite - set up parent-child relationship
            val parentSupplier = supplierRepository.findById(invite.parentSupplierId!!).orElse(null)
            if (parentSupplier != null && newSupplier != null) {
                newSupplier.parentSupplier = parentSupplier
                newSupplier.updatedAt = LocalDateTime.now()
                supplierRepository.save(newSupplier)
                log.info("Set up parent-child relationship: ${parentSupplier.supplierName} -> ${newSupplier.supplierName}")
            }
        } else if (invite.inviterId != null) {
            // Exporter invite - create connection to exporter
            createDirectSupplierConnection(supplierId, invite.inviterId!!, "SUPPLY_AGREEMENT")
        }
        
        log.info("Accepted invite: $inviteId, supplier: $supplierId (inviterType: ${invite.inviterType})")
    }

    private fun SupplierInvite.toDto() = SupplierInviteDto(
        id = this.id,
        email = this.email,
        supplierName = this.supplierName,
        supplierType = this.supplierType?.name,
        inviterId = this.inviterId,
        inviterName = this.inviterName,
        inviterCompany = this.inviterCompany,
        message = this.message,
        parentSupplierId = this.parentSupplierId,
        inviterType = this.inviterType,
        status = this.status.name,
        resentCount = this.resentCount,
        lastSentAt = this.lastSentAt,
        acceptedAt = this.acceptedAt,
        createdAt = this.createdAt
    )

    // ==================== EXTENSION FUNCTIONS ====================

    private fun SupplyChainSupplier.toDto() = SupplierDto(
        id = this.id,
        supplierName = this.supplierName,
        supplierCode = this.supplierCode,
        supplierType = this.supplierType.name,
        businessRegistrationNumber = this.businessRegistrationNumber,
        taxId = this.taxId,
        countryCode = this.countryCode,
        region = this.region,
        gpsLatitude = this.gpsLatitude,
        gpsLongitude = this.gpsLongitude,
        commoditiesHandled = this.commoditiesHandled,
        certifications = this.certifications,
        capacityPerMonthKg = this.capacityPerMonthKg,
        hederaAccountId = this.hederaAccountId,
        verificationStatus = this.verificationStatus.name,
        verifiedAt = this.verifiedAt,
        isActive = this.isActive,
        email = this.getEmail(),
        phoneNumber = this.getPhoneNumber(),
        contactPerson = this.getContactPerson(),
        connectedExporterId = this.connectedExporterId,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )

    private fun SupplierConnection.toDto() = ConnectionDto(
        id = this.id,
        workflowId = this.workflow.id,
        fromSupplierId = this.fromSupplier?.id,
        fromSupplierName = this.fromSupplier?.supplierName,
        fromSupplierType = this.fromSupplier?.supplierType?.name,
        toSupplierId = this.toSupplier.id,
        toSupplierName = this.toSupplier.supplierName,
        toSupplierType = this.toSupplier.supplierType.name,
        connectionType = this.connectionType.name,
        quantityKg = this.quantityKg,
        transferDate = this.transferDate,
        batchReference = this.batchReference,
        transportMethod = this.transportMethod,
        transportDocumentRef = this.transportDocumentRef,
        notes = this.notes,
        hederaTransactionId = this.hederaTransactionId,
        createdAt = this.createdAt
    )
}

// ==================== DTOs ====================

data class SupplierDto(
    val id: String,
    val supplierName: String,
    val supplierCode: String?,
    val supplierType: String,
    val businessRegistrationNumber: String?,
    val taxId: String?,
    val countryCode: String,
    val region: String?,
    val gpsLatitude: Double?,
    val gpsLongitude: Double?,
    val commoditiesHandled: String?,
    val certifications: String?,
    val capacityPerMonthKg: BigDecimal?,
    val hederaAccountId: String?,
    val verificationStatus: String,
    val verifiedAt: LocalDateTime?,
    val isActive: Boolean,
    val email: String?,
    val phoneNumber: String?,
    val contactPerson: String?,
    val connectedExporterId: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class CreateSupplierRequest(
    val supplierName: String,
    val supplierCode: String? = null,
    val supplierType: SupplierType,
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

data class ChainNodeDto(
    val id: String,
    val supplierName: String,
    val supplierType: String,
    val supplierCode: String?,
    val countryCode: String,
    val region: String?,
    val verificationStatus: String,
    val connectionInfo: ConnectionInfoDto?
)

data class ConnectionInfoDto(
    val connectionId: String,
    val quantityKg: BigDecimal,
    val connectionType: String,
    val transferDate: LocalDateTime,
    val batchReference: String?,
    val hederaTransactionId: String?
)

data class ConnectionDto(
    val id: String,
    val workflowId: String,
    val fromSupplierId: String?,
    val fromSupplierName: String?,
    val fromSupplierType: String?,
    val toSupplierId: String,
    val toSupplierName: String,
    val toSupplierType: String,
    val connectionType: String,
    val quantityKg: BigDecimal,
    val transferDate: LocalDateTime,
    val batchReference: String?,
    val transportMethod: String?,
    val transportDocumentRef: String?,
    val notes: String?,
    val hederaTransactionId: String?,
    val createdAt: LocalDateTime
)

data class CreateConnectionRequest(
    val fromSupplierId: String?, // Null for origin (e.g., first farmer harvest)
    val toSupplierId: String,
    val connectionType: ConnectionType,
    val quantityKg: BigDecimal,
    val transferDate: LocalDateTime,
    val batchReference: String? = null,
    val transportMethod: String? = null,
    val transportDocumentRef: String? = null,
    val notes: String? = null
)

data class ConnectionStatsDto(
    val workflowId: String,
    val totalConnections: Int,
    val totalQuantityKg: BigDecimal,
    val verifiedOnHedera: Int,
    val uniqueSuppliers: Int,
    val connectionsByType: Map<String, ConnectionTypeStats>
)

data class ConnectionTypeStats(
    val count: Int,
    val totalQuantityKg: BigDecimal
)

data class SupplierInviteDto(
    val id: String,
    val email: String,
    val supplierName: String?,
    val supplierType: String?,
    val inviterId: String?,
    val inviterName: String?,
    val inviterCompany: String?,
    val message: String?,
    val parentSupplierId: String?,
    val inviterType: String?,
    val status: String,
    val resentCount: Int,
    val lastSentAt: LocalDateTime,
    val acceptedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)
