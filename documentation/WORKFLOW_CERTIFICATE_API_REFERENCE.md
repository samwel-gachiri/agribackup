# Supply Chain Workflow Certificate API Reference

## Overview
This document describes the API endpoints and service methods for managing EUDR Compliance Certificate NFTs for supply chain workflows.

---

## REST API Endpoints

### 1. Get All Certificates
Retrieves all EUDR compliance certificates from both legacy batches and supply chain workflows.

**Endpoint:** `GET /api/eudr/certificates`

**Authorization:** `EXPORTER`, `SYSTEM_ADMIN`, `VERIFIER`, `AUDITOR`, `IMPORTER`

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "workflow-uuid-123",
      "certificateType": "WORKFLOW",
      "workflowName": "Coffee Export Q4 2025",
      "produceType": "Coffee",
      "totalQuantityKg": 5000.00,
      "exporterId": "exp-123",
      "exporterCompanyName": "ABC Exporters Ltd",
      "createdAt": "2025-12-01T10:00:00",
      "completedAt": "2025-12-10T10:00:00",
      "status": "COMPLETED",
      "currentStage": "COMPLETED",
      "certificateStatus": "COMPLIANT",
      "complianceCertificateNftId": "0.0.123456",
      "complianceCertificateSerialNumber": 1,
      "complianceCertificateTransactionId": "0.0.123@1234567890.0",
      "currentOwnerAccountId": "0.0.789012",
      "certificateIssuedAt": "2025-12-10T10:00:00"
    },
    {
      "id": "batch-uuid-456",
      "certificateType": "BATCH",
      "batchCode": "EUDR-COF-20251210-001",
      "commodityDescription": "Arabica Coffee Beans",
      "countryOfProduction": "Kenya",
      "createdBy": "user-123",
      "createdAt": "2025-12-01T10:00:00",
      "status": "APPROVED",
      "riskLevel": "LOW",
      "riskRationale": "All compliance checks passed",
      "hederaTransactionId": "0.0.123@1234567890.0",
      "complianceCertificateNftId": null,
      "complianceCertificateSerialNumber": null,
      "complianceCertificateTransactionId": null
    }
  ],
  "counts": {
    "total": 2,
    "batchCertificates": 1,
    "workflowCertificates": 1
  },
  "message": "Certificates retrieved successfully"
}
```

**Certificate Types:**
- `WORKFLOW` - Certificate from supply chain workflow (new implementation)
- `BATCH` - Certificate from legacy EUDR batch system

---

## Service Methods

### HederaMainService

#### 1. Issue Workflow Compliance Certificate NFT

Issues a new EUDR Compliance Certificate NFT for a supply chain workflow.

**Method Signature:**
```kotlin
fun issueWorkflowComplianceCertificateNft(
    workflowId: String,
    exporterAccountId: AccountId,
    complianceData: Map<String, String>
): Pair<String, Long>
```

**Parameters:**
- `workflowId`: Unique identifier of the supply chain workflow
- `exporterAccountId`: Hedera account ID of the exporter (e.g., `AccountId.fromString("0.0.123456")`)
- `complianceData`: Map containing compliance information:
  - `workflowName` - Name of the workflow
  - `produceType` - Type of produce (Coffee, Tea, Cocoa, etc.)
  - `totalQuantityKg` - Total quantity in kilograms
  - `totalFarmers` - Number of farmers involved
  - `totalProductionUnits` - Number of production units (farms)
  - `gpsCoordinatesCount` - Number of GPS coordinates captured
  - `deforestationStatus` - Status (e.g., "VERIFIED_FREE")
  - `originCountry` - Country of origin
  - `riskLevel` - Risk level (LOW, MEDIUM, HIGH)
  - `traceabilityHash` - Hash of traceability data

**Returns:** `Pair<String, Long>`
- First: Hedera transaction ID
- Second: NFT serial number

**Example:**
```kotlin
val complianceData = mapOf(
    "workflowName" to "Coffee Export Q4 2025",
    "produceType" to "Coffee",
    "totalQuantityKg" to "5000",
    "totalFarmers" to "25",
    "totalProductionUnits" to "30",
    "gpsCoordinatesCount" to "30",
    "deforestationStatus" to "VERIFIED_FREE",
    "originCountry" to "Kenya",
    "riskLevel" to "LOW",
    "traceabilityHash" to "abc123def456"
)

val (transactionId, serialNumber) = hederaMainService.issueWorkflowComplianceCertificateNft(
    workflowId = workflow.id,
    exporterAccountId = AccountId.fromString(exporter.hederaAccountId),
    complianceData = complianceData
)

// Update workflow
workflow.complianceCertificateNftId = hederaTokenService.getEudrComplianceCertificateNftId().toString()
workflow.complianceCertificateSerialNumber = serialNumber
workflow.complianceCertificateTransactionId = transactionId
workflow.currentOwnerAccountId = exporter.hederaAccountId
workflow.certificateStatus = CertificateStatus.COMPLIANT
workflow.certificateIssuedAt = LocalDateTime.now()
workflowRepository.save(workflow)
```

**Throws:**
- `RuntimeException` - If NFT minting or transfer fails

---

#### 2. Transfer Workflow Compliance Certificate NFT

Transfers an EUDR Compliance Certificate NFT from one account to another (e.g., exporter to importer).

**Method Signature:**
```kotlin
fun transferWorkflowComplianceCertificateNft(
    fromAccountId: AccountId,
    toAccountId: AccountId,
    workflowId: String
): Boolean
```

**Parameters:**
- `fromAccountId`: Current holder of the certificate NFT
- `toAccountId`: Recipient of the certificate NFT
- `workflowId`: Workflow identifier (for logging)

**Returns:** `Boolean`
- `true` if transfer succeeded
- `false` if transfer failed

**Example:**
```kotlin
val success = hederaMainService.transferWorkflowComplianceCertificateNft(
    fromAccountId = AccountId.fromString(exporter.hederaAccountId),
    toAccountId = AccountId.fromString(importer.hederaAccountId),
    workflowId = workflow.id
)

if (success) {
    workflow.currentOwnerAccountId = importer.hederaAccountId
    workflow.certificateStatus = CertificateStatus.TRANSFERRED_TO_IMPORTER
    workflowRepository.save(workflow)
    
    logger.info("Certificate transferred successfully for workflow ${workflow.id}")
} else {
    logger.error("Failed to transfer certificate for workflow ${workflow.id}")
}
```

---

### HederaTokenService

#### 3. Issue Workflow Compliance Certificate NFT (Internal)

Internal method called by `HederaMainService`. Handles the actual NFT minting and transfer.

**Method Signature:**
```kotlin
fun issueWorkflowComplianceCertificateNft(
    workflowId: String,
    exporterAccountId: AccountId,
    complianceData: Map<String, String>
): Pair<String, Long>
```

**Process:**
1. Check if EUDR Certificate NFT collection exists, create if not
2. Mint 1 unique NFT for the workflow
3. Transfer NFT to exporter's account
4. Record issuance on Hedera Consensus Service
5. Return transaction ID and serial number

---

#### 4. Transfer Workflow Compliance Certificate NFT (Internal)

Internal method for transferring certificates.

**Method Signature:**
```kotlin
fun transferWorkflowComplianceCertificateNft(
    fromAccountId: AccountId,
    toAccountId: AccountId,
    workflowId: String
): Boolean
```

**Process:**
1. Get EUDR Certificate NFT token ID
2. Create and execute transfer transaction
3. Record transfer on Hedera Consensus Service
4. Return success status

---

### HederaConsensusServices

#### 5. Record Workflow Certificate Issuance

Records certificate issuance on Hedera Consensus Service (HCS).

**Method Signature:**
```kotlin
fun recordWorkflowComplianceCertificateIssuance(
    workflowId: String,
    exporterAccountId: AccountId,
    nftSerialNumber: Long,
    complianceData: Map<String, String>
): String
```

**HCS Message Structure:**
```json
{
  "eventType": "WORKFLOW_EUDR_CERTIFICATE_ISSUED",
  "entityId": "workflow-uuid",
  "entityType": "WorkflowComplianceCertificate",
  "timestamp": "2025-12-10T10:00:00Z",
  "data": {
    "workflowId": "workflow-uuid",
    "exporterAccountId": "0.0.123456",
    "nftSerialNumber": "1",
    "certificateType": "EUDR_WORKFLOW_COMPLIANCE",
    "issuedAt": "2025-12-10T10:00:00Z",
    "workflowName": "Coffee Export Q4 2025",
    "produceType": "Coffee",
    "totalQuantityKg": "5000",
    "totalFarmers": "25",
    "totalProductionUnits": "30",
    "gpsCoordinatesCount": "30",
    "deforestationStatus": "VERIFIED_FREE",
    "originCountry": "Kenya",
    "riskLevel": "LOW",
    "traceabilityHash": "abc123def456"
  }
}
```

---

#### 6. Record Workflow Certificate Transfer

Records certificate transfer on HCS.

**Method Signature:**
```kotlin
fun recordWorkflowCertificateTransfer(
    workflowId: String,
    fromAccountId: AccountId,
    toAccountId: AccountId
): String
```

**HCS Message Structure:**
```json
{
  "eventType": "WORKFLOW_CERTIFICATE_TRANSFERRED",
  "entityId": "workflow-uuid",
  "entityType": "WorkflowComplianceCertificate",
  "timestamp": "2025-12-10T12:00:00Z",
  "data": {
    "workflowId": "workflow-uuid",
    "fromAccountId": "0.0.123456",
    "toAccountId": "0.0.789012",
    "transferredAt": "2025-12-10T12:00:00Z"
  }
}
```

---

### SupplyChainWorkflowService

#### 7. Get All Workflows

Retrieves all supply chain workflows (for certificate listing).

**Method Signature:**
```kotlin
fun getAllWorkflows(): List<SupplyChainWorkflow>
```

**Returns:** List of all workflows with their certificate details

**Example:**
```kotlin
val workflows = supplyChainWorkflowService.getAllWorkflows()

workflows.forEach { workflow ->
    println("Workflow: ${workflow.workflowName}")
    println("Certificate Status: ${workflow.certificateStatus}")
    println("NFT Serial: ${workflow.complianceCertificateSerialNumber}")
    println("Current Owner: ${workflow.currentOwnerAccountId}")
}
```

---

## Certificate Lifecycle States

### CertificateStatus Enum

| Status | Description | Typical Transition From |
|--------|-------------|------------------------|
| `NOT_CREATED` | Certificate doesn't exist yet | Initial state |
| `PENDING_VERIFICATION` | Workflow complete, awaiting compliance checks | NOT_CREATED |
| `COMPLIANT` | Certificate issued and valid | PENDING_VERIFICATION |
| `IN_TRANSIT` | Shipment in transit with certificate | COMPLIANT |
| `TRANSFERRED_TO_IMPORTER` | Certificate transferred to importer | IN_TRANSIT |
| `CUSTOMS_VERIFIED` | Customs verified the certificate | TRANSFERRED_TO_IMPORTER |
| `DELIVERED` | Goods delivered, certificate archived | CUSTOMS_VERIFIED |
| `FROZEN` | Certificate revoked due to fraud | Any state |
| `EXPIRED` | Certificate validity period expired | DELIVERED |

---

## Integration Example: Complete Workflow Certificate Flow

### Step 1: Create Workflow
```kotlin
val workflow = supplyChainWorkflowService.createWorkflow(
    exporterId = exporter.exporterId,
    request = CreateWorkflowRequestDto(
        workflowName = "Coffee Export Q4 2025",
        produceType = "Coffee"
    )
)
// Status: IN_PROGRESS, CertificateStatus: NOT_CREATED
```

### Step 2: Add Supply Chain Events
```kotlin
// Add collection events
supplyChainWorkflowService.addCollectionEvent(workflow.id, collectionRequest)

// Add consolidation events
supplyChainWorkflowService.addConsolidationEvent(workflow.id, consolidationRequest)

// Add shipment events
supplyChainWorkflowService.addShipmentEvent(workflow.id, shipmentRequest)
```

### Step 3: Verify Compliance
```kotlin
val complianceCheck = verifyWorkflowCompliance(workflow.id)
if (complianceCheck.isCompliant) {
    // Update status to pending verification
    workflow.certificateStatus = CertificateStatus.PENDING_VERIFICATION
    workflowRepository.save(workflow)
}
```

### Step 4: Issue Certificate
```kotlin
if (workflow.certificateStatus == CertificateStatus.PENDING_VERIFICATION) {
    val complianceData = buildComplianceData(workflow)
    
    val (transactionId, serialNumber) = hederaMainService.issueWorkflowComplianceCertificateNft(
        workflowId = workflow.id,
        exporterAccountId = AccountId.fromString(exporter.hederaAccountId),
        complianceData = complianceData
    )
    
    workflow.complianceCertificateNftId = hederaTokenService.getEudrComplianceCertificateNftId().toString()
    workflow.complianceCertificateSerialNumber = serialNumber
    workflow.complianceCertificateTransactionId = transactionId
    workflow.currentOwnerAccountId = exporter.hederaAccountId
    workflow.certificateStatus = CertificateStatus.COMPLIANT
    workflow.certificateIssuedAt = LocalDateTime.now()
    workflowRepository.save(workflow)
}
```

### Step 5: Transfer Certificate to Importer
```kotlin
val success = hederaMainService.transferWorkflowComplianceCertificateNft(
    fromAccountId = AccountId.fromString(exporter.hederaAccountId),
    toAccountId = AccountId.fromString(importer.hederaAccountId),
    workflowId = workflow.id
)

if (success) {
    workflow.currentOwnerAccountId = importer.hederaAccountId
    workflow.certificateStatus = CertificateStatus.TRANSFERRED_TO_IMPORTER
    workflowRepository.save(workflow)
}
```

### Step 6: Customs Verification
```kotlin
// When customs verifies the certificate
workflow.certificateStatus = CertificateStatus.CUSTOMS_VERIFIED
workflowRepository.save(workflow)
```

### Step 7: Delivery Complete
```kotlin
// When goods are delivered
workflow.certificateStatus = CertificateStatus.DELIVERED
workflow.status = WorkflowStatus.COMPLETED
workflow.completedAt = LocalDateTime.now()
workflowRepository.save(workflow)
```

---

## Error Handling

### Common Errors

#### 1. NFT Minting Failed
```kotlin
try {
    val (txId, serial) = hederaMainService.issueWorkflowComplianceCertificateNft(...)
} catch (e: RuntimeException) {
    logger.error("Failed to mint certificate NFT: ${e.message}")
    // Handle error - notify user, retry, or mark as failed
}
```

#### 2. Transfer Failed
```kotlin
val success = hederaMainService.transferWorkflowComplianceCertificateNft(...)
if (!success) {
    logger.error("Certificate transfer failed for workflow ${workflow.id}")
    // Retry transfer or notify administrators
}
```

#### 3. Account Not Found
```kotlin
try {
    val exporterAccount = AccountId.fromString(exporter.hederaAccountId)
} catch (e: IllegalArgumentException) {
    logger.error("Invalid Hedera account ID: ${exporter.hederaAccountId}")
    // Create account or update with valid ID
}
```

---

## Best Practices

### 1. Always Verify Compliance Before Issuing
```kotlin
// ❌ Bad
val (txId, serial) = hederaMainService.issueWorkflowComplianceCertificateNft(...)

// ✅ Good
if (isWorkflowCompliant(workflow)) {
    val (txId, serial) = hederaMainService.issueWorkflowComplianceCertificateNft(...)
} else {
    throw IllegalStateException("Cannot issue certificate - workflow not compliant")
}
```

### 2. Update Database Immediately After Blockchain Transaction
```kotlin
// ✅ Good
val (txId, serial) = hederaMainService.issueWorkflowComplianceCertificateNft(...)

workflow.complianceCertificateTransactionId = txId
workflow.complianceCertificateSerialNumber = serial
workflow.certificateIssuedAt = LocalDateTime.now()
workflowRepository.save(workflow) // Save immediately
```

### 3. Log All Certificate Operations
```kotlin
logger.info("Issuing certificate for workflow ${workflow.id}")
val (txId, serial) = hederaMainService.issueWorkflowComplianceCertificateNft(...)
logger.info("Certificate issued - TxID: $txId, Serial: $serial")
```

### 4. Handle Transfers Transactionally
```kotlin
@Transactional
fun transferCertificateToImporter(workflowId: String, importerId: String) {
    val workflow = workflowRepository.findById(workflowId).orElseThrow()
    val importer = importerRepository.findById(importerId).orElseThrow()
    
    val success = hederaMainService.transferWorkflowComplianceCertificateNft(
        fromAccountId = AccountId.fromString(workflow.currentOwnerAccountId),
        toAccountId = AccountId.fromString(importer.hederaAccountId),
        workflowId = workflow.id
    )
    
    if (success) {
        workflow.currentOwnerAccountId = importer.hederaAccountId
        workflow.certificateStatus = CertificateStatus.TRANSFERRED_TO_IMPORTER
        workflowRepository.save(workflow)
    } else {
        throw RuntimeException("Certificate transfer failed")
    }
}
```

---

## Testing

### Unit Test Example
```kotlin
@Test
fun `should issue workflow certificate successfully`() {
    // Given
    val workflow = createTestWorkflow()
    val complianceData = createTestComplianceData()
    
    // When
    val (txId, serial) = hederaMainService.issueWorkflowComplianceCertificateNft(
        workflowId = workflow.id,
        exporterAccountId = testExporterAccountId,
        complianceData = complianceData
    )
    
    // Then
    assertNotNull(txId)
    assertTrue(serial > 0)
    verify(hederaConsensusService).recordWorkflowComplianceCertificateIssuance(
        workflowId = workflow.id,
        exporterAccountId = testExporterAccountId,
        nftSerialNumber = serial,
        complianceData = complianceData
    )
}
```

### Integration Test Example
```kotlin
@Test
@Transactional
fun `should complete full certificate lifecycle`() {
    // 1. Create workflow
    val workflow = createTestWorkflow()
    assertEquals(CertificateStatus.NOT_CREATED, workflow.certificateStatus)
    
    // 2. Issue certificate
    val (txId, serial) = issueCertificate(workflow)
    workflow.certificateStatus = CertificateStatus.COMPLIANT
    
    // 3. Transfer to importer
    transferCertificate(workflow, testImporter)
    assertEquals(CertificateStatus.TRANSFERRED_TO_IMPORTER, workflow.certificateStatus)
    
    // 4. Customs verification
    workflow.certificateStatus = CertificateStatus.CUSTOMS_VERIFIED
    
    // 5. Delivery
    workflow.certificateStatus = CertificateStatus.DELIVERED
    
    // Verify final state
    val finalWorkflow = workflowRepository.findById(workflow.id).orElseThrow()
    assertEquals(CertificateStatus.DELIVERED, finalWorkflow.certificateStatus)
    assertNotNull(finalWorkflow.complianceCertificateSerialNumber)
}
```

---

## Monitoring and Logging

### Key Metrics to Track
1. Certificate issuance rate
2. Transfer success rate
3. Average time from workflow completion to certificate issuance
4. Number of certificates by status
5. Failed transactions and retries

### Logging Guidelines
```kotlin
// Certificate issuance
logger.info("Issuing certificate for workflow ${workflow.id}")
logger.info("Compliance data: $complianceData")
logger.info("Certificate issued - TxID: $txId, Serial: $serial")

// Certificate transfer
logger.info("Transferring certificate for workflow ${workflow.id}")
logger.info("From: $fromAccountId -> To: $toAccountId")
logger.info("Transfer completed successfully")

// Errors
logger.error("Failed to issue certificate for workflow ${workflow.id}: ${e.message}", e)
logger.warn("Certificate transfer failed - will retry")
```

---

## Support and Troubleshooting

### Common Issues

#### Issue: Certificate not appearing in frontend
**Solution:** Ensure workflow has `complianceCertificateNftId` populated and `certificateStatus != NOT_CREATED`

#### Issue: Transfer fails silently
**Solution:** Check that both accounts have associated the EUDR Certificate NFT token

#### Issue: Serial number is 0
**Solution:** NFT minting may have failed - check Hedera transaction status on HashScan

---

## Additional Resources

- [Hedera Token Service Documentation](https://docs.hedera.com/hedera/sdks-and-apis/sdks/token-service)
- [EUDR Compliance Guide](./EUDR_CERTIFICATION_AND_SUPPLY_CHAIN_GUIDE.md)
- [Certificate Lifecycle Documentation](./EUDR_CERTIFICATE_LIFECYCLE_AND_STATE_MANAGEMENT.md)
- [Supply Chain Workflow Implementation](./SUPPLY_CHAIN_WORKFLOW_CERTIFICATE_IMPLEMENTATION.md)
