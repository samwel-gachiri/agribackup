# Frontend Guide: Issue EUDR Compliance Certificate NFT

## Overview
This guide shows how exporters can issue EUDR Compliance Certificate NFTs for their supply chain workflows through the UI. **End users interact with buttons and forms, not code.**

---

## User Flow

### For Exporters:

1. **Navigate to Workflow Details** → View a specific supply chain workflow
2. **Click "Issue Certificate" button** → Opens certificate issuance dialog
3. **Review Compliance Status** → System shows workflow readiness
4. **Confirm Issuance** → Click "Confirm" to mint certificate on blockchain
5. **View Certificate** → See transaction details and HashScan link

---

## UI Components to Add

### 1. Certificate Status Badge (Shows current state)
- **Location:** Workflow detail page header
- **States:** NOT_CREATED, COMPLIANT, IN_TRANSIT, TRANSFERRED_TO_IMPORTER, etc.
- **Colors:** Grey (not created), Green (compliant), Blue (in transit), Purple (transferred)

### 2. "Issue Certificate" Button
- **Location:** Workflow detail page, certificate section
- **Visibility:** Only shown when `certificateStatus === 'NOT_CREATED'`
- **Enabled when:** Workflow has collection events and exporter has Hedera account
- **Action:** Opens certificate issuance dialog

### 3. Certificate Dialog
- **Step 1:** Review compliance requirements and workflow summary
- **Step 2:** Loading spinner while minting NFT on Hedera blockchain (5-10 seconds)
- **Step 3:** Success screen with certificate details and HashScan link

---

## Backend API Reference

### Issue Certificate Endpoint
**POST** `/api/v1/supply-chain/workflows/{workflowId}/issue-certificate`

**Who can call:** EXPORTER (owner) or ADMIN

**Success Response:**
```json
{
  "success": true,
  "message": "Certificate issued successfully",
  "data": {
    "workflowId": "workflow-uuid-123",
    "transactionId": "0.0.123@1234567890.0",
    "serialNumber": 1,
    "hederaAccountId": "0.0.123456",
    "certificateStatus": "COMPLIANT",
    "issuedAt": "2025-12-10T10:00:00",
    "hashscanUrl": "https://hashscan.io/testnet/transaction/0.0.123@1234567890.0"
  }
}
```

**Error Scenarios:**
| Error Code | Message | User Action |
|-----------|---------|-------------|
| `COMPLIANCE_CHECK_FAILED` | "No collection events recorded" | Add collection events first |
| `COMPLIANCE_CHECK_FAILED` | "Certificate already issued" | View existing certificate |
| `COMPLIANCE_CHECK_FAILED` | "Exporter does not have Hedera account" | Create Hedera account |
| `CERTIFICATE_ISSUANCE_FAILED` | "Blockchain error" | Try again or contact support |

---

## Implementation Guide for Developers

### Where to Add the UI Components

**File to modify:** `frontend/src/views/exporter/WorkflowDetail.vue` (or similar)

### Component Structure:

```vue
<template>
  <v-container>
    <!-- Workflow details here -->
    
    <!-- Certificate Section -->
    <v-card class="mt-4">
      <v-card-title>
        <v-icon left>mdi-certificate</v-icon>
        EUDR Compliance Certificate
      </v-card-title>
      
      <v-card-text>
        <div v-if="workflow.certificateStatus === 'NOT_CREATED'">
          <v-alert type="info" outlined>
            This workflow does not have a certificate yet. Issue one when all compliance checks pass.
          </v-alert>
          
          <v-btn
            color="primary"
            :loading="issuingCertificate"
            :disabled="!canIssueCertificate"
            @click="issueCertificate"
          >
            <v-icon left>mdi-certificate-outline</v-icon>
            Issue EUDR Certificate
          </v-btn>
        </div>
        
        <div v-else-if="workflow.certificateStatus === 'COMPLIANT'">
          <v-alert type="success" outlined>
            <strong>Certificate Issued!</strong>
            <br>Serial Number: {{ workflow.complianceCertificateSerialNumber }}
            <br>Issued: {{ formatDate(workflow.certificateIssuedAt) }}
          </v-alert>
          
          <v-btn
            text
            color="primary"
            :href="getHashscanUrl(workflow.complianceCertificateTransactionId)"
            target="_blank"
          >
            <v-icon left>mdi-open-in-new</v-icon>
            View on Hedera
          </v-btn>
        </div>
        
        <div v-else>
          <v-alert type="info" outlined>
            Certificate Status: {{ workflow.certificateStatus }}
          </v-alert>
        </div>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script>
import axios from 'axios';

export default {
  name: 'WorkflowDetail',
  data() {
    return {
      workflow: {
        id: '',
        workflowName: '',
        certificateStatus: 'NOT_CREATED',
        complianceCertificateSerialNumber: null,
        complianceCertificateTransactionId: null,
        certificateIssuedAt: null,
      },
      issuingCertificate: false,
    };
  },
  computed: {
    canIssueCertificate() {
      // Add your business logic here
      // Example: workflow must have collection events
      return this.workflow.collectionEvents?.length > 0;
    },
  },
  methods: {
    async issueCertificate() {
      this.issuingCertificate = true;
      
      try {
        const response = await axios.post(
          `/api/v1/supply-chain/workflows/${this.workflow.id}/issue-certificate`
        );
        
        if (response.data.success) {
          // Update workflow with certificate data
          this.workflow.certificateStatus = response.data.data.certificateStatus;
          this.workflow.complianceCertificateSerialNumber = response.data.data.serialNumber;
          this.workflow.complianceCertificateTransactionId = response.data.data.transactionId;
          this.workflow.certificateIssuedAt = response.data.data.issuedAt;
          
          // Show success message
          this.$toast.success('Certificate issued successfully!');
          
          // Optional: Show Hashscan link
          const hashscanUrl = response.data.data.hashscanUrl;
          this.$toast.info(`View on Hedera: ${hashscanUrl}`, { duration: 10000 });
        }
      } catch (error) {
        console.error('Failed to issue certificate:', error);
        
        const errorMessage = error.response?.data?.message || 'Failed to issue certificate';
        this.$toast.error(errorMessage);
        
        // Show specific error guidance
        if (error.response?.data?.error === 'COMPLIANCE_CHECK_FAILED') {
          this.$toast.warning('Please ensure all compliance requirements are met');
        }
      } finally {
        this.issuingCertificate = false;
      }
    },
    
    formatDate(date) {
      if (!date) return 'N/A';
      return new Date(date).toLocaleString();
    },
    
    getHashscanUrl(transactionId) {
      return `https://hashscan.io/testnet/transaction/${transactionId}`;
    },
  },
};
</script>
```

---

### 2. Certificate Dialog Component

For a more detailed certificate issuance flow:

```vue
<template>
  <div>
    <!-- Trigger Button -->
    <v-btn color="primary" @click="openDialog">
      <v-icon left>mdi-certificate-outline</v-icon>
      Issue Certificate
    </v-btn>
    
    <!-- Certificate Dialog -->
    <v-dialog v-model="dialog" max-width="700px" persistent>
      <v-card>
        <v-card-title class="primary white--text">
          <v-icon left color="white">mdi-certificate</v-icon>
          Issue EUDR Compliance Certificate
        </v-card-title>
        
        <v-card-text class="pt-4">
          <!-- Step 1: Pre-issuance checks -->
          <div v-if="step === 1">
            <v-alert type="info" outlined>
              Review the workflow compliance status before issuing the certificate.
            </v-alert>
            
            <v-simple-table>
              <tbody>
                <tr>
                  <td><strong>Workflow Name:</strong></td>
                  <td>{{ workflow.workflowName }}</td>
                </tr>
                <tr>
                  <td><strong>Produce Type:</strong></td>
                  <td>{{ workflow.produceType }}</td>
                </tr>
                <tr>
                  <td><strong>Total Quantity:</strong></td>
                  <td>{{ workflow.totalQuantityKg }} kg</td>
                </tr>
                <tr>
                  <td><strong>Collection Events:</strong></td>
                  <td>
                    <v-chip small :color="collectionCount > 0 ? 'success' : 'error'">
                      {{ collectionCount }}
                    </v-chip>
                  </td>
                </tr>
                <tr>
                  <td><strong>Compliance Status:</strong></td>
                  <td>
                    <v-chip small :color="isCompliant ? 'success' : 'warning'">
                      {{ isCompliant ? 'Ready' : 'Pending' }}
                    </v-chip>
                  </td>
                </tr>
              </tbody>
            </v-simple-table>
            
            <v-alert v-if="!isCompliant" type="warning" outlined class="mt-4">
              <strong>Requirements not met:</strong>
              <ul>
                <li v-if="collectionCount === 0">No collection events recorded</li>
                <li v-if="!hasHederaAccount">Exporter Hedera account required</li>
              </ul>
            </v-alert>
          </div>
          
          <!-- Step 2: Issuing -->
          <div v-if="step === 2">
            <div class="text-center py-4">
              <v-progress-circular
                indeterminate
                color="primary"
                size="64"
              ></v-progress-circular>
              <p class="mt-4">Minting certificate on Hedera blockchain...</p>
              <p class="text-caption grey--text">This may take a few seconds</p>
            </div>
          </div>
          
          <!-- Step 3: Success -->
          <div v-if="step === 3">
            <v-alert type="success" outlined>
              <strong>Certificate Issued Successfully!</strong>
            </v-alert>
            
            <v-simple-table class="mt-4">
              <tbody>
                <tr>
                  <td><strong>Transaction ID:</strong></td>
                  <td>
                    <code>{{ certificateData.transactionId }}</code>
                  </td>
                </tr>
                <tr>
                  <td><strong>Serial Number:</strong></td>
                  <td>{{ certificateData.serialNumber }}</td>
                </tr>
                <tr>
                  <td><strong>Status:</strong></td>
                  <td>
                    <v-chip small color="success">COMPLIANT</v-chip>
                  </td>
                </tr>
                <tr>
                  <td><strong>Issued At:</strong></td>
                  <td>{{ formatDate(certificateData.issuedAt) }}</td>
                </tr>
              </tbody>
            </v-simple-table>
            
            <v-btn
              block
              color="primary"
              class="mt-4"
              :href="certificateData.hashscanUrl"
              target="_blank"
            >
              <v-icon left>mdi-open-in-new</v-icon>
              View on HashScan
            </v-btn>
          </div>
          
          <!-- Step 4: Error -->
          <div v-if="step === 4">
            <v-alert type="error" outlined>
              <strong>Failed to Issue Certificate</strong>
              <br>{{ errorMessage }}
            </v-alert>
          </div>
        </v-card-text>
        
        <v-divider></v-divider>
        
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="closeDialog" :disabled="step === 2">
            {{ step === 3 ? 'Close' : 'Cancel' }}
          </v-btn>
          <v-btn
            v-if="step === 1"
            color="primary"
            :disabled="!isCompliant"
            @click="issueCertificate"
          >
            Issue Certificate
          </v-btn>
          <v-btn
            v-if="step === 4"
            color="primary"
            @click="step = 1"
          >
            Try Again
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'CertificateIssueDialog',
  props: {
    workflow: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      dialog: false,
      step: 1, // 1: Review, 2: Issuing, 3: Success, 4: Error
      certificateData: null,
      errorMessage: '',
    };
  },
  computed: {
    collectionCount() {
      return this.workflow.collectionEvents?.length || 0;
    },
    hasHederaAccount() {
      // This should be checked from exporter data
      return true; // Assume true for now
    },
    isCompliant() {
      return this.collectionCount > 0 && this.hasHederaAccount;
    },
  },
  methods: {
    openDialog() {
      this.dialog = true;
      this.step = 1;
      this.certificateData = null;
      this.errorMessage = '';
    },
    
    closeDialog() {
      this.dialog = false;
      if (this.step === 3) {
        // Emit event to refresh workflow
        this.$emit('certificate-issued', this.certificateData);
      }
    },
    
    async issueCertificate() {
      this.step = 2; // Show loading
      
      try {
        const response = await axios.post(
          `/api/v1/supply-chain/workflows/${this.workflow.id}/issue-certificate`
        );
        
        if (response.data.success) {
          this.certificateData = response.data.data;
          this.step = 3; // Show success
          
          this.$toast.success('Certificate issued successfully!');
        } else {
          this.errorMessage = response.data.message;
          this.step = 4; // Show error
        }
      } catch (error) {
        console.error('Failed to issue certificate:', error);
        
        this.errorMessage = error.response?.data?.message || 'An unexpected error occurred';
        this.step = 4; // Show error
        
        this.$toast.error('Failed to issue certificate');
      }
    },
    
    formatDate(date) {
      if (!date) return 'N/A';
      return new Date(date).toLocaleString();
    },
  },
};
</script>
```

---

### 3. Usage in Workflow Management Page

```vue
<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <h1>{{ workflow.workflowName }}</h1>
        
        <!-- Certificate Status Badge -->
        <v-chip
          :color="getCertificateColor(workflow.certificateStatus)"
          text-color="white"
          class="mb-4"
        >
          <v-icon left small>
            {{ getCertificateIcon(workflow.certificateStatus) }}
          </v-icon>
          {{ workflow.certificateStatus }}
        </v-chip>
      </v-col>
    </v-row>
    
    <!-- Workflow Events -->
    <v-row>
      <!-- Collection Events -->
      <!-- Consolidation Events -->
      <!-- etc. -->
    </v-row>
    
    <!-- Certificate Actions -->
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title>Certificate Management</v-card-title>
          <v-card-text>
            <certificate-issue-dialog
              :workflow="workflow"
              @certificate-issued="onCertificateIssued"
            />
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import CertificateIssueDialog from '@/components/CertificateIssueDialog.vue';

export default {
  name: 'WorkflowManagement',
  components: {
    CertificateIssueDialog,
  },
  data() {
    return {
      workflow: null,
    };
  },
  methods: {
    async loadWorkflow() {
      const response = await axios.get(
        `/api/v1/supply-chain/workflows/${this.$route.params.workflowId}`
      );
      this.workflow = response.data;
    },
    
    onCertificateIssued(certificateData) {
      // Update workflow with new certificate data
      this.workflow.certificateStatus = certificateData.certificateStatus;
      this.workflow.complianceCertificateSerialNumber = certificateData.serialNumber;
      this.workflow.complianceCertificateTransactionId = certificateData.transactionId;
      this.workflow.certificateIssuedAt = certificateData.issuedAt;
      
      // Optionally refresh the entire workflow
      this.loadWorkflow();
    },
    
    getCertificateColor(status) {
      const colors = {
        NOT_CREATED: 'grey',
        PENDING_VERIFICATION: 'warning',
        COMPLIANT: 'success',
        IN_TRANSIT: 'info',
        TRANSFERRED_TO_IMPORTER: 'purple',
        CUSTOMS_VERIFIED: 'teal',
        DELIVERED: 'success',
        FROZEN: 'error',
        EXPIRED: 'grey darken-2',
      };
      return colors[status] || 'grey';
    },
    
    getCertificateIcon(status) {
      const icons = {
        NOT_CREATED: 'mdi-file-document-outline',
        PENDING_VERIFICATION: 'mdi-clock-alert-outline',
        COMPLIANT: 'mdi-check-circle',
        IN_TRANSIT: 'mdi-truck-delivery',
        TRANSFERRED_TO_IMPORTER: 'mdi-swap-horizontal',
        CUSTOMS_VERIFIED: 'mdi-shield-check',
        DELIVERED: 'mdi-package-variant-closed',
        FROZEN: 'mdi-snowflake-alert',
        EXPIRED: 'mdi-clock-remove',
      };
      return icons[status] || 'mdi-help-circle';
    },
  },
  mounted() {
    this.loadWorkflow();
  },
};
</script>
```

---

## Certificate Transfer (Exporter → Importer)

### User Flow:

1. **Exporter views workflow** with issued certificate
2. **Clicks "Transfer to Importer" button**
3. **Selects importer** from dropdown (shows connected importers)
4. **Confirms transfer** → Certificate ownership moves to importer
5. **Status updates** to `TRANSFERRED_TO_IMPORTER`

### UI Components Needed:

- **"Transfer Certificate" button** (visible when status = COMPLIANT)
- **Importer selection dropdown** (from connected importers)
- **Confirmation dialog** with transfer details
- **Success notification** with updated status

---

## What Users See

### Before Certificate Issuance:
```
┌─────────────────────────────────────────────┐
│ Workflow: Coffee Export to EU - Dec 2025   │
│ Status: [grey badge] NOT_CREATED           │
├─────────────────────────────────────────────┤
│ EUDR Compliance Certificate                 │
│                                             │
│ ℹ️  This workflow does not have a          │
│    certificate yet. Issue one when ready.  │
│                                             │
│ [ 🎫 Issue EUDR Certificate ]              │
│   (Button enabled when requirements met)    │
└─────────────────────────────────────────────┘
```

### During Certificate Issuance:
```
┌─────────────────────────────────────────────┐
│        Issue EUDR Certificate               │
├─────────────────────────────────────────────┤
│                                             │
│           [Loading spinner]                 │
│                                             │
│   Minting certificate on Hedera...         │
│   This may take a few seconds              │
│                                             │
└─────────────────────────────────────────────┘
```

### After Certificate Issued:
```
┌─────────────────────────────────────────────┐
│ Workflow: Coffee Export to EU - Dec 2025   │
│ Status: [green badge] COMPLIANT            │
├─────────────────────────────────────────────┤
│ EUDR Compliance Certificate                 │
│                                             │
│ ✅ Certificate Issued Successfully!        │
│                                             │
│ Serial Number: #1                           │
│ Issued: Dec 10, 2025 10:30 AM              │
│ Transaction: 0.0.123@1234567890.0          │
│                                             │
│ [ 🔗 View on Hedera ]                      │
│ [ 📤 Transfer to Importer ]                │
└─────────────────────────────────────────────┘
```

---

## UI Mockups

### Workflow Detail Page Layout

```
┌────────────────────────────────────────────────────────────┐
│  ← Back to Workflows                                       │
│                                                            │
│  Coffee Export to EU - December 2025                       │
│  [COMPLIANT]  Created: Dec 1, 2025                        │
│                                                            │
│  ┌──────────────────────────────────────────────────┐    │
│  │  📋 Workflow Summary                              │    │
│  │  Produce: Coffee Beans                            │    │
│  │  Quantity: 5,000 kg                               │    │
│  │  Farmers: 25                                      │    │
│  │  Production Units: 12                             │    │
│  └──────────────────────────────────────────────────┘    │
│                                                            │
│  ┌──────────────────────────────────────────────────┐    │
│  │  🎫 EUDR Compliance Certificate                   │    │
│  │                                                    │    │
│  │  [Status badge: COMPLIANT]                        │    │
│  │                                                    │    │
│  │  Serial #1                                        │    │
│  │  Issued: Dec 10, 2025                            │    │
│  │  Transaction: 0.0.123@1234567890.0               │    │
│  │                                                    │    │
│  │  [View on HashScan] [Transfer Certificate]       │    │
│  └──────────────────────────────────────────────────┘    │
│                                                            │
│  ┌──────────────────────────────────────────────────┐    │
│  │  📍 Collection Events (25)                        │    │
│  │  [View Events]                                    │    │
│  └──────────────────────────────────────────────────┘    │
│                                                            │
└────────────────────────────────────────────────────────────┘
```

### Certificate Issuance Dialog

```
┌──────────────────────────────────────────┐
│  🎫 Issue EUDR Compliance Certificate    │
├──────────────────────────────────────────┤
│                                          │
│  Review Compliance Status:               │
│                                          │
│  Workflow Name:    Coffee Export EU      │
│  Produce Type:     Coffee Beans          │
│  Total Quantity:   5,000 kg              │
│  Collection Events: ✅ 25 events         │
│  Hedera Account:    ✅ Configured        │
│  Compliance:        ✅ Ready             │
│                                          │
│  ⚠️  This will mint an NFT certificate  │
│     on Hedera blockchain. This action   │
│     cannot be undone.                   │
│                                          │
│  [ Cancel ]  [ Confirm & Issue ]        │
└──────────────────────────────────────────┘
```

---

## Technical Implementation Notes for Developers

### API Service Layer (`src/services/certificateService.js`)

Create a reusable service for certificate operations:

```javascript
// This service is called by Vue components, NOT by users
import axios from 'axios';

export default {
  issueCertificate(workflowId) {
    return axios.post(
      `/api/v1/supply-chain/workflows/${workflowId}/issue-certificate`
    );
  },
  
  transferCertificate(workflowId, importerId) {
    return axios.post(
      `/api/v1/supply-chain/workflows/${workflowId}/transfer-certificate`,
      null,
      { params: { importerId } }
    );
  },
};
```

### Component Logic (Called when user clicks button)

```javascript
// When user clicks "Issue Certificate" button, this runs:
async handleIssueCertificate() {
  this.loading = true;
  try {
    const response = await certificateService.issueCertificate(this.workflowId);
    this.showSuccess(response.data);
---

## Testing Checklist for QA

### Test Scenario 1: Successful Certificate Issuance
1. ✅ Login as exporter
2. ✅ Navigate to workflow with collection events
3. ✅ Verify "Issue Certificate" button is visible and enabled
4. ✅ Click button → Dialog opens
5. ✅ Review compliance status → All checks pass
6. ✅ Click "Confirm" → Loading spinner shows
7. ✅ Wait 5-10 seconds → Success message appears
8. ✅ Verify certificate details displayed (serial #, transaction ID)
9. ✅ Click "View on HashScan" → Opens Hedera explorer
10. ✅ Refresh page → Certificate status persists

### Test Scenario 2: Missing Requirements
1. ✅ Create workflow with NO collection events
2. ✅ Try to issue certificate
3. ✅ Verify error message: "No collection events recorded"
4. ✅ Add collection events
5. ✅ Try again → Should succeed

### Test Scenario 3: Already Issued
1. ✅ Navigate to workflow with existing certificate
2. ✅ Verify "Issue Certificate" button is HIDDEN
3. ✅ Verify certificate details are displayed
4. ✅ Verify "Transfer to Importer" button is visible

---

## Summary

### For End Users (Exporters):
1. **Click "Issue Certificate" button** on workflow page
2. **Review compliance requirements** in dialog
3. **Confirm issuance** → Wait for blockchain transaction
4. **View certificate** with HashScan link

### For Developers:
1. **Add UI components** to workflow detail page
2. **Connect button click** to `certificateService.issueCertificate()`
3. **Handle loading state** during blockchain minting (5-10 seconds)
4. **Display results** or error messages
5. **Update workflow status** after success

### Backend Handles:
- ✅ Compliance validation
- ✅ Hedera account verification
- ✅ NFT minting on blockchain
- ✅ Database updates
- ✅ Transaction recording on HCS

**No manual API calls needed - users just click buttons in the UI!**hScan link
4. **Handle errors** - show appropriate error messages

The backend will:
- ✅ Validate compliance requirements
- ✅ Check exporter has Hedera account
- ✅ Mint NFT certificate on blockchain
- ✅ Update workflow with certificate details
- ✅ Return certificate data including transaction ID

