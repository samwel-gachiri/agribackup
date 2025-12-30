    <template>
    <div class="tw-bg-white tw-rounded-xl tw-border tw-border-gray-200">
    <!-- Header -->
    <div class="tw-p-4 tw-border-b tw-border-gray-100 tw-flex tw-items-center tw-justify-between">
      <div class="tw-flex tw-items-center tw-gap-3">
        <div class="tw-w-10 tw-h-10 tw-rounded-full tw-bg-teal-100 tw-flex tw-items-center tw-justify-center">
          <v-icon color="teal">mdi-shield-check</v-icon>
        </div>
        <div>
          <h3 class="tw-font-semibold tw-text-gray-900">EUDR Compliance</h3>
          <p class="tw-text-sm tw-text-gray-500">Risk assessment and due diligence statement</p>
        </div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="tw-p-8 tw-text-center">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
      <p class="tw-text-gray-500 tw-mt-2">Loading compliance data...</p>
    </div>

    <div v-else class="tw-p-4 tw-space-y-4">
      <!-- Compliance Checklist Summary -->
      <div class="tw-bg-gray-50 tw-rounded-lg tw-p-4">
        <h4 class="tw-font-semibold tw-text-gray-800 tw-mb-3">Pre-Compliance Checklist</h4>
        <div class="tw-space-y-2">
          <div class="tw-flex tw-items-center tw-gap-2">
            <v-icon :color="hasLinkedUnits ? 'success' : 'grey'">
              {{ hasLinkedUnits ? 'mdi-check-circle' : 'mdi-circle-outline' }}
            </v-icon>
            <span :class="hasLinkedUnits ? 'tw-text-gray-800' : 'tw-text-gray-500'">
              Production units linked ({{ linkedUnitsCount }})
            </span>
          </div>
          <div class="tw-flex tw-items-center tw-gap-2">
            <v-icon :color="hasVerifiedGeolocation ? 'success' : 'grey'">
              {{ hasVerifiedGeolocation ? 'mdi-check-circle' : 'mdi-circle-outline' }}
            </v-icon>
            <span :class="hasVerifiedGeolocation ? 'tw-text-gray-800' : 'tw-text-gray-500'">
              Geolocation verified ({{ verifiedCount }}/{{ linkedUnitsCount }})
            </span>
          </div>
          <div class="tw-flex tw-items-center tw-gap-2">
            <v-icon :color="hasDeforestationCheck ? 'success' : 'grey'">
              {{ hasDeforestationCheck ? 'mdi-check-circle' : 'mdi-circle-outline' }}
            </v-icon>
            <span :class="hasDeforestationCheck ? 'tw-text-gray-800' : 'tw-text-gray-500'">
              Deforestation checks passed ({{ deforestationClearCount }}/{{ linkedUnitsCount }})
            </span>
          </div>
          <div class="tw-flex tw-items-center tw-gap-2">
            <v-icon :color="hasCollectionEvents ? 'success' : 'grey'">
              {{ hasCollectionEvents ? 'mdi-check-circle' : 'mdi-circle-outline' }}
            </v-icon>
            <span :class="hasCollectionEvents ? 'tw-text-gray-800' : 'tw-text-gray-500'">
              Collection events recorded ({{ collectionEventCount }})
            </span>
          </div>
        </div>
      </div>

      <!-- Risk Assessment Section -->
      <div class="tw-border tw-rounded-lg tw-p-4">
        <div class="tw-flex tw-items-center tw-justify-between tw-mb-3">
          <h4 class="tw-font-semibold tw-text-gray-800">Risk Assessment</h4>
          <v-chip v-if="riskAssessment && !isInRiskAssessmentStage" :color="getRiskColor(riskAssessment.classification)">
            <v-icon left small>{{ getRiskIcon(riskAssessment.classification) }}</v-icon>
            {{ riskAssessment.classification }}
          </v-chip>
          <v-chip v-else-if="isInRiskAssessmentStage" color="amber" outlined>
            <v-icon left small>mdi-clock-outline</v-icon>
            Pending
          </v-chip>
        </div>

        <!-- Prominent CTA when in RISK_ASSESSMENT stage -->
        <div v-if="isInRiskAssessmentStage">
          <v-alert type="info" border="left" colored-border class="tw-mb-4">
            <strong>Action Required:</strong> Run the risk assessment to evaluate EUDR compliance
            based on country of origin, supply chain complexity, and deforestation verification status.
          </v-alert>
          <v-btn
            color="amber darken-2"
            depressed
            large
            :loading="assessingRisk"
            :disabled="!canRunRiskAssessment"
            @click="runRiskAssessment"
            class="tw-w-full"
          >
            <v-icon left>mdi-shield-search</v-icon>
            Perform Risk Assessment
          </v-btn>
          <p v-if="!canRunRiskAssessment" class="tw-text-xs tw-text-orange-600 tw-mt-2 tw-text-center">
            Complete all checklist items above before running risk assessment
          </p>
        </div>

        <!-- Show results when past RISK_ASSESSMENT stage -->
        <div v-else-if="riskAssessment" class="tw-space-y-2">
          <div class="tw-grid tw-grid-cols-2 tw-gap-4 tw-text-sm">
            <div>
              <span class="tw-text-gray-500">Country Risk:</span>
              <span class="tw-ml-2 tw-font-medium">{{ riskAssessment.countryRisk || 'N/A' }}</span>
            </div>
            <div>
              <span class="tw-text-gray-500">Supply Chain Complexity:</span>
              <span class="tw-ml-2 tw-font-medium">{{ riskAssessment.complexityRisk || 'N/A' }}</span>
            </div>
            <div>
              <span class="tw-text-gray-500">Deforestation Risk:</span>
              <span class="tw-ml-2 tw-font-medium">{{ riskAssessment.deforestationRisk || 'N/A' }}</span>
            </div>
            <div>
              <span class="tw-text-gray-500">Overall Score:</span>
              <span class="tw-ml-2 tw-font-medium">{{ riskAssessment.score || 'N/A' }}</span>
            </div>
          </div>
          <p class="tw-text-xs tw-text-gray-500 tw-mt-2">
            Assessed on {{ formatDate(riskAssessment.assessedAt) }}
          </p>
        </div>
      </div>

      <!-- Due Diligence Statement Section - Only visible in DUE_DILIGENCE_STATEMENT stage or later -->
      <div
        v-if="shouldShowDDSSection"
        class="tw-border tw-rounded-lg tw-p-4"
      >
        <div class="tw-flex tw-items-center tw-justify-between tw-mb-3">
          <h4 class="tw-font-semibold tw-text-gray-800">Due Diligence Statement (DDS)</h4>
          <v-chip v-if="ddsGenerated" color="success" small>
            <v-icon left small>mdi-check</v-icon>
            Generated
          </v-chip>
          <v-chip v-else-if="isInDDSStage" color="primary" outlined>
            <v-icon left small>mdi-clock-outline</v-icon>
            Pending
          </v-chip>
        </div>

        <!-- Prominent CTA when in DUE_DILIGENCE_STATEMENT stage and not yet generated -->
        <div v-if="!ddsGenerated && isInDDSStage">
          <v-alert type="info" border="left" colored-border class="tw-mb-4">
            <strong>Action Required:</strong> Generate the Due Diligence Statement (DDS) required for EU import.
            This document compiles all traceability evidence and will be recorded on Hedera blockchain.
          </v-alert>
          <v-btn
            color="primary"
            depressed
            large
            :loading="generatingDDS"
            :disabled="!canGenerateDDS"
            @click="generateDDS"
            class="tw-w-full"
          >
            <v-icon left>mdi-file-certificate</v-icon>
            Generate Due Diligence Statement
          </v-btn>
          <p v-if="!canGenerateDDS" class="tw-text-xs tw-text-orange-600 tw-mt-2 tw-text-center">
            Complete risk assessment first before generating DDS
          </p>
        </div>

        <!-- Show DDS download options when generated -->
        <div v-else-if="ddsGenerated" class="tw-space-y-2">
          <div class="tw-bg-green-50 tw-rounded-lg tw-p-3">
            <div class="tw-flex tw-items-center tw-gap-2 tw-text-green-800">
              <v-icon color="success">mdi-file-document-check</v-icon>
              <span class="tw-font-medium">DDS Reference: {{ ddsReference }}</span>
            </div>
          </div>
          <div class="tw-flex tw-gap-2 tw-mt-3">
            <v-btn small outlined color="primary" @click="downloadDDS">
              <v-icon left small>mdi-download</v-icon>
              Download PDF
            </v-btn>
            <v-btn small outlined color="grey" @click="viewDDS">
              <v-icon left small>mdi-eye</v-icon>
              Preview
            </v-btn>
          </div>
        </div>
      </div>

      <!-- Blockchain Record -->
      <div v-if="blockchainRecorded" class="tw-bg-purple-50 tw-rounded-lg tw-p-4">
        <div class="tw-flex tw-items-center tw-gap-2 tw-text-purple-800 tw-mb-2">
          <v-icon color="purple">mdi-check-decagram</v-icon>
          <span class="tw-font-semibold">Recorded on Hedera Blockchain</span>
        </div>
        <p class="tw-text-sm tw-text-purple-700">
          Transaction ID: <code class="tw-bg-purple-100 tw-px-2 tw-py-0.5 tw-rounded">{{ blockchainTransactionId }}</code>
        </p>
        <v-btn x-small text color="purple" class="tw-mt-2" @click="viewOnBlockchain">
          <v-icon left x-small>mdi-open-in-new</v-icon>
          View on HashScan
        </v-btn>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'CompliancePanel',

  props: {
    workflowId: {
      type: String,
      required: true,
    },
    currentStage: {
      type: String,
      default: 'RISK_ASSESSMENT',
    },
  },

  data() {
    return {
      loading: false,
      assessingRisk: false,
      generatingDDS: false,

      // Workflow data
      linkedUnitsCount: 0,
      verifiedCount: 0,
      deforestationClearCount: 0,
      collectionEventCount: 0,

      // Risk assessment
      riskAssessment: null,

      // DDS
      ddsGenerated: false,
      ddsReference: null,

      // Blockchain
      blockchainRecorded: false,
      blockchainTransactionId: null,
    };
  },

  computed: {
    hasLinkedUnits() {
      return this.linkedUnitsCount > 0;
    },

    hasVerifiedGeolocation() {
      return this.verifiedCount > 0 && this.verifiedCount === this.linkedUnitsCount;
    },

    hasDeforestationCheck() {
      return this.deforestationClearCount > 0 && this.deforestationClearCount === this.linkedUnitsCount;
    },

    hasCollectionEvents() {
      return this.collectionEventCount > 0;
    },

    canRunRiskAssessment() {
      return this.hasLinkedUnits && this.hasVerifiedGeolocation && this.hasDeforestationCheck && this.hasCollectionEvents;
    },

    canGenerateDDS() {
      return this.riskAssessment !== null;
    },

    shouldShowDDSSection() {
      // Show DDS section ONLY when past RISK_ASSESSMENT stage
      // This is purely stage-based, not data-based
      const ddsAndLaterStages = ['DUE_DILIGENCE_STATEMENT', 'EXPORT_SHIPMENT', 'CUSTOMS_CLEARANCE', 'DELIVERY_COMPLETE'];
      return ddsAndLaterStages.includes(this.currentStage);
    },

    isInRiskAssessmentStage() {
      return this.currentStage === 'RISK_ASSESSMENT';
    },

    isInDDSStage() {
      return this.currentStage === 'DUE_DILIGENCE_STATEMENT';
    },
  },

  async mounted() {
    await this.loadComplianceData();
  },

  methods: {
    async loadComplianceData() {
      this.loading = true;
      try {
        // Load workflow progress to get compliance status
        const response = await axios.get(`/api/eudr/workflow-stages/workflows/${this.workflowId}/progress`);

        // Extract compliance-related data
        const data = response.data;
        this.linkedUnitsCount = data.linkedProductionUnits || 0;
        this.verifiedCount = data.verifiedProductionUnits || 0;
        this.deforestationClearCount = data.deforestationClearUnits || 0;
        this.collectionEventCount = data.collectionEventCount || 0;

        // Risk assessment - populate all risk details from backend
        if (data.riskClassification) {
          this.riskAssessment = {
            classification: data.riskClassification,
            assessedAt: data.riskAssessedAt,
            countryRisk: data.countryRisk,
            complexityRisk: data.complexityRisk,
            deforestationRisk: data.deforestationRisk,
            score: data.riskScore,
          };
        }

        // DDS
        if (data.ddsReference) {
          this.ddsGenerated = true;
          this.ddsReference = data.ddsReference;
        }

        // Blockchain
        if (data.blockchainTransactionId) {
          this.blockchainRecorded = true;
          this.blockchainTransactionId = data.blockchainTransactionId;
        }
      } catch (error) {
        this.$toast.error('Failed to load compliance data:', error.message);
      } finally {
        this.loading = false;
      }
    },

    async runRiskAssessment() {
      this.assessingRisk = true;
      try {
        const response = await axios.post(`/api/eudr/workflow-stages/workflows/${this.workflowId}/risk-assessment`);

        if (response.data) {
          this.riskAssessment = {
            classification: response.data.classification,
            assessedAt: response.data.assessedAt || new Date().toISOString(),
            countryRisk: response.data.riskFactors?.find((f) => f.type === 'COUNTRY')?.level || 'N/A',
            complexityRisk: response.data.riskFactors?.find((f) => f.type === 'SUPPLY_CHAIN')?.level || 'N/A',
            deforestationRisk: response.data.riskFactors?.find((f) => f.type === 'DEFORESTATION')?.level || 'N/A',
            score: response.data.overallScore,
          };
          this.$emit('risk-assessed', response.data);
        }
      } catch (error) {
        this.$toast.error('Failed to run risk assessment:', error.message);
        this.$emit('error', error.response?.data?.message || 'Failed to run risk assessment');
      } finally {
        this.assessingRisk = false;
      }
    },

    async generateDDS() {
      this.generatingDDS = true;
      try {
        const response = await axios.post(`/api/eudr/workflow-stages/workflows/${this.workflowId}/generate-dds`);

        if (response.data) {
          this.ddsGenerated = true;
          this.ddsReference = response.data.ddsReference;
          this.$emit('dds-generated', response.data);
        }
      } catch (error) {
        this.$toast.error('Failed to generate DDS:', error.message);
        this.$emit('error', error.response?.data?.message || 'Failed to generate Due Diligence Statement');
      } finally {
        this.generatingDDS = false;
      }
    },

    async downloadDDS() {
      // Proper PDF download using axios with blob response type (like authority reports)
      try {
        const response = await axios.get(`/api/eudr/workflow-stages/workflows/${this.workflowId}/dds/download`, {
          responseType: 'blob',
        });

        // Create blob from response and trigger download
        const blob = response.data instanceof Blob
          ? response.data
          : new Blob([response.data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);

        const link = document.createElement('a');
        link.href = url;
        link.download = `DDS-${this.ddsReference || this.workflowId}.pdf`;
        document.body.appendChild(link);
        link.click();
        link.remove();

        // Clean up
        window.URL.revokeObjectURL(url);
      } catch (error) {
        this.$emit('error', 'Failed to download Due Diligence Statement');
      }
    },

    async viewDDS() {
      // Open DDS in new tab for preview
      try {
        const response = await axios.get(`/api/eudr/workflow-stages/workflows/${this.workflowId}/dds/download`, {
          responseType: 'blob',
        });

        const blob = response.data instanceof Blob
          ? response.data
          : new Blob([response.data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        window.open(url, '_blank');
      } catch (error) {
        this.$toast.error('Failed to preview DDS:', error.message);
        this.$emit('error', 'Failed to preview Due Diligence Statement');
      }
    },

    viewOnBlockchain() {
      if (this.blockchainTransactionId) {
        window.open(`https://hashscan.io/testnet/transaction/${this.blockchainTransactionId}`, '_blank');
      }
    },

    formatDate(dateString) {
      if (!dateString) return 'N/A';
      return new Date(dateString).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    },

    getRiskColor(classification) {
      const colors = {
        LOW: 'success', STANDARD: 'info', HIGH: 'warning', CRITICAL: 'error',
      };
      return colors[classification] || 'grey';
    },

    getRiskIcon(classification) {
      const icons = {
        LOW: 'mdi-shield-check',
        STANDARD: 'mdi-shield-half-full',
        HIGH: 'mdi-shield-alert',
        CRITICAL: 'mdi-shield-off',
      };
      return icons[classification] || 'mdi-shield';
    },
  },
};
</script>
