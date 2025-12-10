# Supply Chain Workflow Certificate Implementation - Summary

## Problem Statement
The previous implementation was giving certificates as derived values from import shipments, which was incorrect. Each supply chain workflow should have its own NFT certificate that gets minted when the workflow completes and passes EUDR compliance checks.

## Solution Overview
Implemented a proper NFT certificate system for supply chain workflows where:
1. Each workflow can have its own EUDR Compliance Certificate NFT
2. Certificates are minted (not derived) when workflows are verified as compliant
3. Certificates track their lifecycle from issuance to customs verification
4. Both legacy batch certificates and new workflow certificates are supported

---

## Changes Made

### 1. Database Schema Changes

#### Added to `supply_chain_workflows` table:
- `compliance_certificate_nft_id` (varchar(50)) - Hedera Token ID of the issued NFT
- `compliance_certificate_serial_number` (bigint) - Serial number of the minted NFT
- `compliance_certificate_transaction_id` (varchar(100)) - Hedera transaction ID of issuance
- `current_owner_account_id` (varchar(50)) - Current Hedera account holding the certificate
- `certificate_status` (varchar(50)) - Lifecycle status of the certificate
- `certificate_issued_at` (timestamp) - When the certificate was issued

#### Certificate Status Enum Values:
- `NOT_CREATED` - Certificate hasn't been issued yet
- `PENDING_VERIFICATION` - Workflow data collected, awaiting compliance checks
- `COMPLIANT` - Certificate issued and valid
- `IN_TRANSIT` - Certificate with exporter during transit
- `TRANSFERRED_TO_IMPORTER` - Certificate transferred to importer
- `CUSTOMS_VERIFIED` - Customs authority verified the certificate
- `DELIVERED` - Goods delivered, certificate archived
- `FROZEN` - Certificate revoked due to fraud
- `EXPIRED` - Certificate validity period expired

**Migration File:** `028_add_certificate_columns_to_workflows.yml`

---

### 2. Backend Changes

#### A. Domain Model (`SupplyChainWorkflow.kt`)
Added certificate tracking fields to the workflow entity:
```kotlin
@Column(name = "compliance_certificate_nft_id", length = 50)
var complianceCertificateNftId: String? = null

@Column(name = "compliance_certificate_serial_number")
var complianceCertificateSerialNumber: Long? = null

@Column(name = "compliance_certificate_transaction_id", length = 100)
var complianceCertificateTransactionId: String? = null

@Column(name = "current_owner_account_id", length = 50)
var currentOwnerAccountId: String? = null

@Enumerated(EnumType.STRING)
@Column(name = "certificate_status", length = 50)
var certificateStatus: CertificateStatus = CertificateStatus.NOT_CREATED

@Column(name = "certificate_issued_at")
var certificateIssuedAt: LocalDateTime? = null
```

Added `CertificateStatus` enum with all lifecycle states.

#### B. Hedera Token Service (`HederaTokenService.kt`)
Added new method for issuing workflow certificates:

**`issueWorkflowComplianceCertificateNft()`**
- Mints a unique NFT for the workflow
- Returns both transaction ID and serial number
- Transfers certificate to exporter's Hedera account
- Records issuance on Hedera Consensus Service

**`transferWorkflowComplianceCertificateNft()`**
- Transfers certificate between accounts (exporter → importer)
- Records transfer on HCS
- Updates ownership tracking

#### C. Hedera Consensus Services (`HederaConsensusServices.kt`)
Added new recording methods:

**`recordWorkflowComplianceCertificateIssuance()`**
- Records when a workflow certificate is issued
- Includes full compliance data:
  - Workflow name and produce type
  - Total quantity and farmers
  - GPS coordinates count
  - Deforestation status
  - Risk level and origin country
  - Traceability hash

**`recordWorkflowCertificateTransfer()`**
- Records certificate transfers between accounts
- Tracks from/to account IDs and timestamp

#### D. Hedera Main Service (`HederaMainService.kt`)
Exposed public methods for workflow certificate operations:
- `issueWorkflowComplianceCertificateNft()`
- `transferWorkflowComplianceCertificateNft()`

#### E. Supply Chain Workflow Service (`SupplyChainWorkflowService.kt`)
Added method:
- `getAllWorkflows()` - Returns all workflows for certificate listing

#### F. EUDR Controller (`EudrController.kt`)
Updated `/api/eudr/certificates` endpoint to return both:
1. **Legacy batch certificates** (from `eudr_batches` table)
2. **Workflow certificates** (from `supply_chain_workflows` table)

Response includes:
```json
{
  "success": true,
  "data": [
    {
      "certificateType": "WORKFLOW",
      "id": "workflow-uuid",
      "workflowName": "Coffee Export Q4 2025",
      "produceType": "Coffee",
      "totalQuantityKg": 5000,
      "exporterId": "exp-123",
      "exporterCompanyName": "ABC Exporters",
      "certificateStatus": "COMPLIANT",
      "complianceCertificateNftId": "0.0.123456",
      "complianceCertificateSerialNumber": 1,
      "complianceCertificateTransactionId": "0.0.123@1234567890.0",
      "currentOwnerAccountId": "0.0.789012",
      "certificateIssuedAt": "2025-12-10T10:00:00"
    }
  ],
  "counts": {
    "total": 50,
    "batchCertificates": 20,
    "workflowCertificates": 30
  }
}
```

---

### 3. Frontend Changes

#### Certificate Viewer (`CertificateViewer.vue`)

**Updated Certificate Display:**
- Shows certificate type badge ("Workflow Certificate" vs "Legacy Batch")
- Displays workflow name for workflow certificates
- Shows appropriate status colors and icons for all certificate statuses

**Enhanced Filtering:**
- Added all new certificate status values to filter options
- Filter now checks both `certificateStatus` and `eudrComplianceStatus`

**Improved Search:**
- Searches across workflow names, shipment numbers, produce types, and exporter names
- Handles both workflow and batch certificates

**New Status Icons & Colors:**
| Status | Color | Icon |
|--------|-------|------|
| COMPLIANT | success (green) | mdi-check-circle |
| PENDING_VERIFICATION | warning (orange) | mdi-clock-alert-outline |
| IN_TRANSIT | info (blue) | mdi-truck-delivery |
| TRANSFERRED_TO_IMPORTER | purple | mdi-swap-horizontal |
| CUSTOMS_VERIFIED | teal | mdi-shield-check |
| DELIVERED | success (green) | mdi-package-variant-closed |
| FROZEN | error (red) | mdi-snowflake-alert |
| EXPIRED | grey darken-2 | mdi-clock-remove |

---

## How to Use the New System

### For Exporters:

#### Step 1: Create a Supply Chain Workflow
```
1. Navigate to "Supply Chain Workflow"
2. Create new workflow (e.g., "Coffee Export Q4 2025")
3. Select produce type and other details
```

#### Step 2: Record Supply Chain Events
```
1. Add collection events (production unit → aggregator)
2. Add consolidation events (aggregator → processor)
3. Add processing events (if applicable)
4. Add shipment events (processor → importer)
```

#### Step 3: Issue EUDR Compliance Certificate
Once all compliance checks pass, call:
```kotlin
val (transactionId, serialNumber) = hederaMainService.issueWorkflowComplianceCertificateNft(
    workflowId = workflow.id,
    exporterAccountId = exporter.hederaAccountId,
    complianceData = mapOf(
        "workflowName" to workflow.workflowName,
        "produceType" to workflow.produceType,
        "totalQuantityKg" to workflow.totalQuantityKg.toString(),
        "totalFarmers" to totalFarmers.toString(),
        "totalProductionUnits" to totalUnits.toString(),
        "gpsCoordinatesCount" to gpsCount.toString(),
        "deforestationStatus" to "VERIFIED_FREE",
        "originCountry" to originCountry,
        "riskLevel" to "LOW",
        "traceabilityHash" to calculateHash(workflow)
    )
)

// Update workflow with certificate details
workflow.complianceCertificateNftId = tokenId
workflow.complianceCertificateSerialNumber = serialNumber
workflow.complianceCertificateTransactionId = transactionId
workflow.currentOwnerAccountId = exporter.hederaAccountId
workflow.certificateStatus = CertificateStatus.COMPLIANT
workflow.certificateIssuedAt = LocalDateTime.now()
```

#### Step 4: Transfer Certificate to Importer
When shipment is sent:
```kotlin
hederaMainService.transferWorkflowComplianceCertificateNft(
    fromAccountId = exporter.hederaAccountId,
    toAccountId = importer.hederaAccountId,
    workflowId = workflow.id
)

// Update workflow
workflow.currentOwnerAccountId = importer.hederaAccountId
workflow.certificateStatus = CertificateStatus.TRANSFERRED_TO_IMPORTER
```

### For Importers:
View certificates in the Certificate Viewer:
```
1. Navigate to "Certificates" section
2. Filter by "TRANSFERRED_TO_IMPORTER" or "COMPLIANT"
3. View certificate details including NFT serial number and blockchain transaction
4. Verify on blockchain via HashScan link
```

---

## Blockchain Integration

Each workflow certificate issuance is recorded on Hedera:

**HCS Message Format:**
```json
{
  "eventType": "WORKFLOW_EUDR_CERTIFICATE_ISSUED",
  "entityId": "workflow-uuid",
  "entityType": "WorkflowComplianceCertificate",
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
    "traceabilityHash": "sha256hash"
  }
}
```

**NFT Properties:**
- Token Symbol: EUDR-CERT
- Token Type: Non-Fungible Token (NFT)
- Supply: 1 per workflow
- Transferable: Yes
- Revocable: Yes (via freeze)

---

## Benefits of This Implementation

### 1. Proper Certificate Ownership
✅ Each workflow has its own minted NFT certificate (not derived)
✅ Certificate ownership is explicit and tracked on blockchain
✅ Certificates can be transferred between supply chain actors

### 2. Complete Traceability
✅ Certificate lifecycle is fully tracked from issuance to customs
✅ All state changes are recorded on Hedera blockchain
✅ Immutable audit trail for compliance verification

### 3. Compliance Verification
✅ Customs can instantly verify certificate authenticity on blockchain
✅ No need to trust exporter's database
✅ Certificate includes all compliance data (GPS coordinates, deforestation status, etc.)

### 4. Fraud Prevention
✅ Certificates can be frozen if fraud is detected
✅ All transfers are recorded and verifiable
✅ Cannot be duplicated or forged

### 5. User Experience
✅ Unified certificate viewer shows both legacy and new certificates
✅ Clear status indicators and lifecycle tracking
✅ Easy filtering and search across all certificates

---

## Database Migration

Run the following to apply changes:
```bash
./mvnw liquibase:update
```

Or the migration will automatically run on next application startup.

**Rollback (if needed):**
```bash
./mvnw liquibase:rollback -Dliquibase.rollbackCount=1
```

---

## Testing the Implementation

### 1. Test Certificate Issuance
```kotlin
@Test
fun testIssueWorkflowCertificate() {
    val workflow = createTestWorkflow()
    val (txId, serial) = hederaMainService.issueWorkflowComplianceCertificateNft(
        workflowId = workflow.id,
        exporterAccountId = AccountId.fromString("0.0.123456"),
        complianceData = testComplianceData
    )
    
    assertNotNull(txId)
    assertTrue(serial > 0)
}
```

### 2. Test Certificate Transfer
```kotlin
@Test
fun testTransferCertificate() {
    val success = hederaMainService.transferWorkflowComplianceCertificateNft(
        fromAccountId = AccountId.fromString("0.0.123456"),
        toAccountId = AccountId.fromString("0.0.789012"),
        workflowId = workflow.id
    )
    
    assertTrue(success)
}
```

### 3. Test Frontend Certificate Display
1. Create multiple workflows with different certificate statuses
2. Navigate to Certificate Viewer
3. Verify all workflows appear with correct status badges
4. Test filtering by certificate status
5. Test search functionality

---

## Migration Path for Existing Data

For existing workflows without certificates:
1. Identify completed workflows that should have certificates
2. Verify they passed all EUDR compliance checks
3. Mint certificates retroactively using the `issueWorkflowComplianceCertificateNft()` method
4. Update workflow records with certificate details

**Example Migration Script:**
```kotlin
fun migrateLegacyWorkflows() {
    val completedWorkflows = workflowRepository.findByStatusAndCertificateStatusIsNull(
        WorkflowStatus.COMPLETED,
        CertificateStatus.NOT_CREATED
    )
    
    completedWorkflows.forEach { workflow ->
        if (isEudrCompliant(workflow)) {
            val (txId, serial) = issueWorkflowComplianceCertificateNft(
                workflowId = workflow.id,
                exporterAccountId = workflow.exporter.hederaAccountId,
                complianceData = buildComplianceData(workflow)
            )
            
            workflow.complianceCertificateTransactionId = txId
            workflow.complianceCertificateSerialNumber = serial
            workflow.certificateStatus = CertificateStatus.DELIVERED
            workflowRepository.save(workflow)
        }
    }
}
```

---

## Future Enhancements

### 1. Automated Certificate Issuance
- Trigger certificate minting automatically when workflow completes
- Integrate with EUDR verification API
- Auto-transfer to importer when shipment is sent

### 2. Certificate Metadata
- Store additional metadata in IPFS
- Link IPFS CID to NFT metadata
- Include images, documents, and attestations

### 3. Smart Contract Integration
- Create Hedera smart contract for certificate lifecycle
- Automate transfers based on shipment status
- Implement escrow for payment upon certificate transfer

### 4. Mobile App Integration
- QR code scanning for certificate verification
- Real-time notifications for certificate transfers
- Offline certificate viewing

---

## Conclusion

The new implementation properly handles EUDR compliance certificates as minted NFTs rather than derived values. Each supply chain workflow can now have its own certificate that is:
- Properly minted on Hedera blockchain
- Tracked through its entire lifecycle
- Transferable between supply chain actors
- Verifiable by customs authorities
- Immutable and tamper-proof

This provides a robust, compliant, and auditable system for EUDR certification that meets EU regulatory requirements while leveraging blockchain technology for transparency and trust.
