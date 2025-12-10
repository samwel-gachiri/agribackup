# Workflow API Certificate Optimization

## Issue
The `/api/v1/supply-chain/workflows/exporter/{exporterId}` endpoint was returning workflows without certificate information, causing the frontend to not display certificate status badges.

## Solution Implemented

### 1. Updated `WorkflowResponseDto` 
**File:** `backend/src/main/kotlin/com/agriconnect/farmersportalapis/application/dtos/SupplyChainWorkflowDtos.kt`

Added 6 certificate fields:
```kotlin
// Certificate information
val certificateStatus: String?,
val complianceCertificateNftId: String?,
val complianceCertificateSerialNumber: Long?,
val complianceCertificateTransactionId: String?,
val certificateIssuedAt: LocalDateTime?,
val currentOwnerAccountId: String?
```

### 2. Updated Mapper in Service
**File:** `backend/src/main/kotlin/com/agriconnect/farmersportalapis/service/supplychain/SupplyChainWorkflowService.kt`

Modified `toWorkflowResponseDto()` to include certificate fields from entity:
```kotlin
// Certificate information
certificateStatus = workflow.certificateStatus?.name,
complianceCertificateNftId = workflow.complianceCertificateNftId,
complianceCertificateSerialNumber = workflow.complianceCertificateSerialNumber,
complianceCertificateTransactionId = workflow.complianceCertificateTransactionId,
certificateIssuedAt = workflow.certificateIssuedAt,
currentOwnerAccountId = workflow.currentOwnerAccountId
```

## Updated API Response

### Before:
```json
{
    "content": [
        {
            "id": "8059a403-b410-470b-81bc-d85a545750db",
            "exporterId": "WQ1gQtXWyCY6h2OPGHC9TdUtnUQ2",
            "workflowName": "Rice Workflow #1 - Dec 2025",
            "produceType": "Rice",
            "status": "IN_PROGRESS",
            "currentStage": "COLLECTION",
            "totalCollected": 0,
            "collectionEventCount": 0
            // No certificate fields!
        }
    ]
}
```

### After:
```json
{
    "content": [
        {
            "id": "8059a403-b410-470b-81bc-d85a545750db",
            "exporterId": "WQ1gQtXWyCY6h2OPGHC9TdUtnUQ2",
            "workflowName": "Rice Workflow #1 - Dec 2025",
            "produceType": "Rice",
            "status": "IN_PROGRESS",
            "currentStage": "COLLECTION",
            "totalCollected": 0,
            "collectionEventCount": 0,
            "certificateStatus": "NOT_CREATED",
            "complianceCertificateNftId": null,
            "complianceCertificateSerialNumber": null,
            "complianceCertificateTransactionId": null,
            "certificateIssuedAt": null,
            "currentOwnerAccountId": null
        }
    ]
}
```

## Certificate Status Values
- `NOT_CREATED` - No certificate issued yet
- `PENDING_VERIFICATION` - Verification in progress
- `COMPLIANT` - Certificate issued and compliant
- `IN_TRANSIT` - Certificate in transit
- `TRANSFERRED_TO_IMPORTER` - Transferred to importer
- `CUSTOMS_VERIFIED` - Verified by customs
- `DELIVERED` - Delivery completed
- `FROZEN` - Certificate frozen
- `EXPIRED` - Certificate expired

## Frontend Impact

### Workflow Card Display
The frontend can now:
1. ✅ Show certificate status badges on workflow cards
2. ✅ Display "Issue Certificate" button when `certificateStatus === 'NOT_CREATED'`
3. ✅ Show certificate status chip when certificate exists
4. ✅ Enable/disable certificate actions based on status

### Example Frontend Usage
```javascript
// Workflow card can now access certificate info directly
<v-chip 
  v-if="workflow.certificateStatus"
  :color="getCertificateStatusColor(workflow.certificateStatus)"
>
  {{ workflow.certificateStatus }}
</v-chip>

// Show issue button only if no certificate
<v-btn 
  v-if="workflow.certificateStatus === 'NOT_CREATED' && workflow.collectionEventCount > 0"
  @click="issueCertificate(workflow)"
>
  Issue Certificate
</v-btn>
```

## Performance Optimization
✅ **Single Query** - All certificate data included in initial workflow fetch
✅ **No Extra Calls** - Frontend doesn't need to fetch certificates separately
✅ **Efficient Rendering** - Certificate badges render immediately with workflow list

## Testing Checklist
- [ ] Verify workflows without certificates show `certificateStatus: "NOT_CREATED"`
- [ ] Verify workflows with issued certificates show all certificate fields populated
- [ ] Verify frontend displays certificate badges correctly
- [ ] Verify "Issue Certificate" button appears/disappears based on status
- [ ] Verify API performance with large workflow lists

## Migration Notes
- ✅ No database migration needed (fields already exist in `supply_chain_workflows` table)
- ✅ Backward compatible (all certificate fields are nullable)
- ✅ Existing workflows will show `certificateStatus: null` or `"NOT_CREATED"` by default

