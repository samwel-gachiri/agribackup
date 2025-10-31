<template>
  <Default>
    <v-container fluid class="pa-6">
      <!-- Header -->
      <v-row class="mb-6">
        <v-col cols="12">
          <div class="tw-d-flex align-center justify-space-between">
            <div class="tw-d-flex align-center">
              <v-icon large color="orange" class="mr-3">mdi-alert-octagon</v-icon>
              <h1 class="text-h4 font-weight-bold">Risk Management</h1>
            </div>
            <v-btn
              color="primary"
              @click="assessBatchDialog = true"
              elevation="2"
              large
            >
              <v-icon left>mdi-refresh</v-icon>
              Assess Batch
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- Risk Assessment Table -->
      <v-row>
        <v-col cols="12">
          <v-card elevation="2" class="rounded-lg">
            <v-card-title class="d-flex align-center pa-6">
              <v-icon color="primary" class="mr-2">mdi-format-list-bulleted</v-icon>
              <span class="text-h6">Risk Assessments</span>
              <v-spacer></v-spacer>
              <v-text-field
                v-model="search"
                append-icon="mdi-magnify"
                label="Search batches..."
                single-line
                hide-details
                dense
                outlined
                class="search-field"
              ></v-text-field>
            </v-card-title>
            <v-data-table
              :headers="headers"
              :items="riskAssessments"
              :search="search"
              :loading="loading"
              class="elevation-0"
            >
              <template v-slot:item.batchCode="{ item }">
                <div class="d-flex align-center">
                  <v-icon color="primary" class="mr-2">mdi-package-variant</v-icon>
                  <div>
                    <div class="font-weight-medium">{{ item.batchCode }}</div>
                    <div class="text-caption text--secondary">{{ item.commodityDescription }}</div>
                  </div>
                </div>
              </template>
              <template v-slot:item.riskLevel="{ item }">
                <v-chip
                  :color="getRiskColor(item.riskLevel)"
                  text-color="white"
                  small
                >
                  {{ item.riskLevel }}
                </v-chip>
              </template>
              <template v-slot:item.overallRiskScore="{ item }">
                <div class="d-flex align-center">
                  <v-progress-circular
                    :value="item.overallRiskScore * 100"
                    :color="getScoreColor(item.overallRiskScore * 100)"
                    size="40"
                    width="4"
                    class="mr-2"
                  >
                    <span class="text-caption">{{ Math.round(item.overallRiskScore * 100) }}</span>
                  </v-progress-circular>
                </div>
              </template>
              <template v-slot:item.assessedAt="{ item }">
                {{ formatDate(item.assessedAt) }}
              </template>
              <template v-slot:item.actions="{ item }">
                <v-btn
                  icon
                  small
                  color="primary"
                  @click="viewAssessment(item)"
                  title="View Details"
                >
                  <v-icon>mdi-eye</v-icon>
                </v-btn>
                <v-btn
                  icon
                  small
                  color="warning"
                  @click="createMitigation(item)"
                  title="Create Mitigation"
                  v-if="item.riskLevel === 'HIGH' || item.riskLevel === 'MEDIUM'"
                >
                  <v-icon>mdi-shield-alert</v-icon>
                </v-btn>
                <v-btn
                  icon
                  small
                  color="info"
                  @click="reassessRisk(item)"
                  title="Reassess"
                >
                  <v-icon>mdi-refresh</v-icon>
                </v-btn>
                <v-btn
                  icon
                  small
                  color="teal"
                  @click="viewOnHedera(item)"
                  title="View on Hedera"
                  v-if="item.hederaTransactionId"
                >
                  <v-icon>mdi-alpha-h-circle</v-icon>
                </v-btn>
              </template>
            </v-data-table>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Fullscreen Assess Batch Dialog -->
    <v-dialog v-model="assessBatchDialog" fullscreen hide-overlay transition="dialog-bottom-transition">
      <v-card>
        <v-toolbar dark color="primary" elevation="0">
          <v-btn icon dark @click="assessBatchDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
          <v-toolbar-title>Select Batches to Assess</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-toolbar-items>
            <v-btn
              dark
              text
              @click="runBulkAssessment"
              :loading="assessmentRunning"
              :disabled="selectedBatchIds.length === 0"
            >
              <v-icon left>mdi-check</v-icon>
              Assess {{ selectedBatchIds.length }} Batch{{ selectedBatchIds.length !== 1 ? 'es' : '' }}
            </v-btn>
          </v-toolbar-items>
        </v-toolbar>

        <v-container fluid class="pa-6">
          <v-row>
            <v-col cols="12">
              <v-alert type="info" outlined class="mb-4">
                <div class="d-flex align-center">
                  <v-icon color="info" class="mr-3">mdi-information</v-icon>
                  <div>
                    <div class="font-weight-bold">Risk Assessment</div>
                    <div>Select one or more batches to run comprehensive EUDR compliance risk analysis.</div>
                  </div>
                </div>
              </v-alert>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="12">
              <v-card elevation="2">
                <v-card-title>
                  <v-text-field
                    v-model="batchSearch"
                    append-icon="mdi-magnify"
                    label="Search batches..."
                    single-line
                    hide-details
                    dense
                    outlined
                    class="mr-4"
                  ></v-text-field>
                </v-card-title>
                <v-data-table
                  v-model="selectedBatchIds"
                  :headers="batchHeaders"
                  :items="batches"
                  :search="batchSearch"
                  show-select
                  item-key="id"
                  class="elevation-0"
                >
                  <template v-slot:item.batchCode="{ item }">
                    <div class="d-flex align-center">
                      <v-icon color="primary" class="mr-2">mdi-package-variant</v-icon>
                      <div>
                        <div class="font-weight-medium">{{ item.batchCode }}</div>
                        <div class="text-caption text--secondary">{{ item.commodityDescription }}</div>
                      </div>
                    </div>
                  </template>
                  <template v-slot:item.quantity="{ item }">
                    {{ item.quantityKg }} kg
                  </template>
                  <template v-slot:item.status="{ item }">
                    <v-chip small :color="getStatusColor(item.status)">
                      {{ item.status }}
                    </v-chip>
                  </template>
                  <template v-slot:item.riskLevel="{ item }">
                    <v-chip
                      v-if="item.riskLevel"
                      small
                      :color="getRiskColor(item.riskLevel)"
                      text-color="white"
                    >
                      {{ item.riskLevel }}
                    </v-chip>
                    <span v-else class="text--secondary">Not assessed</span>
                  </template>
                  <template v-slot:item.lastAssessment="{ item }">
                    {{ item.lastRiskAssessment ? formatDate(item.lastRiskAssessment) : 'Never' }}
                  </template>
                </v-data-table>
              </v-card>
            </v-col>
          </v-row>
        </v-container>
      </v-card>
    </v-dialog>

    <!-- Assessment Detail Dialog -->
    <v-dialog v-model="assessmentDetailDialog" max-width="900px">
      <v-card v-if="selectedAssessment">
        <v-card-title class="primary white--text">
          <v-icon left color="white">mdi-chart-box</v-icon>
          Risk Assessment Details: {{ selectedAssessment.batchCode }}
          <v-spacer></v-spacer>
          <v-chip :color="getRiskColor(selectedAssessment.riskLevel)" text-color="white">
            {{ selectedAssessment.riskLevel }}
          </v-chip>
        </v-card-title>
        <v-card-text class="tw-pt-4">
          <v-row>
            <v-col cols="12" md="6">
              <div class="tw-text-h6 tw-mb-3">Overall Risk Score</div>
              <v-progress-circular
                :value="selectedAssessment.overallRiskScore * 100"
                :color="getScoreColor(selectedAssessment.overallRiskScore * 100)"
                :size="120"
                :width="12"
              >
                <span class="text-h5">{{ Math.round(selectedAssessment.overallRiskScore * 100) }}</span>
              </v-progress-circular>
            </v-col>
            <v-col cols="12" md="6">
              <div class="tw-text-h6 tw-mb-3">Risk Factors Breakdown</div>
              <div class="risk-breakdown-item">
                <span>Country Risk:</span>
                <v-chip small :color="getScoreColor(selectedAssessment.countryRiskScore * 100)">
                  {{ Math.round(selectedAssessment.countryRiskScore * 100) }}%
                </v-chip>
              </div>
              <div class="risk-breakdown-item">
                <span>Deforestation Risk:</span>
                <v-chip small :color="getScoreColor(selectedAssessment.deforestationRiskScore * 100)">
                  {{ Math.round(selectedAssessment.deforestationRiskScore * 100) }}%
                </v-chip>
              </div>
              <div class="risk-breakdown-item">
                <span>Supplier Risk:</span>
                <v-chip small :color="getScoreColor(selectedAssessment.supplierRiskScore * 100)">
                  {{ Math.round(selectedAssessment.supplierRiskScore * 100) }}%
                </v-chip>
              </div>
              <div class="risk-breakdown-item">
                <span>Documentation Risk:</span>
                <v-chip small :color="getScoreColor(selectedAssessment.documentationRiskScore * 100)">
                  {{ Math.round(selectedAssessment.documentationRiskScore * 100) }}%
                </v-chip>
              </div>
            </v-col>
          </v-row>
          <v-divider class="my-4"></v-divider>
          <div class="text-h6 mb-3">Recommendations</div>
          <v-list dense>
            <v-list-item v-for="(recommendation, index) in selectedAssessment.recommendations" :key="index">
              <v-list-item-icon>
                <v-icon color="warning">mdi-lightbulb</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>{{ recommendation }}</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list>
        </v-card-text>
        <v-card-actions>
          <v-btn
            color="warning"
            @click="createMitigation(selectedAssessment)"
            v-if="selectedAssessment.riskLevel === 'HIGH' || selectedAssessment.riskLevel === 'MEDIUM'"
          >
            <v-icon left>mdi-shield-alert</v-icon>
            Create Mitigation
          </v-btn>
          <v-spacer></v-spacer>
          <v-btn text @click="assessmentDetailDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </Default>
</template>

<script>
/* eslint-disable */
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'RiskManagement',
  components: {
    Default,
  },
  data() {
    return {
      loading: false,
      assessmentRunning: false,
      search: '',
      batchSearch: '',
      headers: [
        { text: 'Batch', value: 'batchCode', sortable: true },
        { text: 'Risk Level', value: 'riskLevel', sortable: true },
        { text: 'Risk Score', value: 'overallRiskScore', sortable: true },
        { text: 'Last Assessment', value: 'assessedAt', sortable: true },
        {
          text: 'Actions', value: 'actions', sortable: false, width: '200px',
        },
      ],
      batchHeaders: [
        { text: 'Batch', value: 'batchCode', sortable: true },
        { text: 'Quantity', value: 'quantity', sortable: true },
        { text: 'Status', value: 'status', sortable: true },
        { text: 'Current Risk', value: 'riskLevel', sortable: true },
        { text: 'Last Assessment', value: 'lastAssessment', sortable: true },
      ],
      riskAssessments: [],
      batches: [],
      selectedBatchIds: [],
      assessmentDetailDialog: false,
      assessBatchDialog: false,
      selectedAssessment: null,
    };
  },
  mounted() {
    this.loadBatches();
    this.loadRiskAssessments();
  },
  methods: {
    async loadBatches() {
      try {
        const response = await axios.get('/api/eudr/batches');
        if (response.data.success) {
          this.batches = response.data.data;
        }
      } catch (error) {
        this.$toast.error('Failed to load batches');
      }
    },
    async loadRiskAssessments() {
      this.loading = true;
      try {
        // Load all batches and their risk assessments
        const response = await axios.get('/api/eudr/batches');
        if (response.data.success) {
          const batches = response.data.data;
          const assessments = [];
          
          for (const batch of batches) {
            if (batch.riskLevel) {
              try {
                const assessmentResponse = await axios.get(`/api/eudr/assess?batchId=${batch.id}`);
                if (assessmentResponse.data.success) {
                  // Transform API response to match frontend expectations
                  const apiData = assessmentResponse.data.data;
                  const transformedAssessment = {
                    batchId: apiData.batchId,
                    batchCode: batch.batchCode || apiData.batchId, // Use batch code from batch data
                    overallRiskScore: apiData.overallScore,
                    riskLevel: apiData.riskLevel,
                    assessedAt: apiData.assessedAt,
                    countryRiskScore: apiData.components?.countryRisk?.score || 0,
                    deforestationRiskScore: apiData.components?.deforestationRisk?.score || 0,
                    supplierRiskScore: apiData.components?.supplierRisk?.score || 0,
                    documentationRiskScore: apiData.components?.documentationRisk?.score || 0,
                    commodityRiskScore: apiData.components?.commodityRisk?.score || 0,
                    geospatialRiskScore: apiData.components?.geospatialRisk?.score || 0,
                    recommendations: apiData.recommendations || [],
                    hederaTransactionId: apiData.hederaTransactionId,
                    // Include component details for detailed view
                    components: apiData.components
                  };
                  assessments.push(transformedAssessment);
                }
              } catch (error) {
              }
            }
          }
          
          this.riskAssessments = assessments;
        }
      } catch (error) {
        this.$toast.error('Failed to load risk assessments');
      } finally {
        this.loading = false;
      }
    },
    getRiskColor(riskLevel) {
      const colors = {
        HIGH: 'error',
        MEDIUM: 'warning',
        LOW: 'info',
        NONE: 'success',
      };
      return colors[riskLevel] || 'grey';
    },
    getStatusColor(status) {
      const colors = {
        PENDING: 'grey',
        APPROVED: 'success',
        IN_TRANSIT: 'info',
        DELIVERED: 'teal',
        REJECTED: 'error',
      };
      return colors[status] || 'grey';
    },
    getScoreColor(score) {
      if (score >= 80) return 'error';
      if (score >= 60) return 'warning';
      if (score >= 40) return 'info';
      return 'success';
    },
    formatDate(date) {
      if (!date) return 'N/A';
      return new Date(date).toLocaleDateString();
    },
    async runBulkAssessment() {
      if (this.selectedBatchIds.length === 0) {
        this.$toast.warning('Please select batches to assess');
        return;
      }

      this.assessmentRunning = true;
      try {
        const batchIds = this.selectedBatchIds.map(batch => batch.id);
        const response = await axios.post('/api/eudr/assess/bulk', batchIds);
        if (response.data.success) {
          this.$toast.success(`Bulk assessment completed for ${batchIds.length} batches`);
          this.assessBatchDialog = false;
          this.selectedBatchIds = [];
          await this.loadRiskAssessments();
        }
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to run bulk assessment');
      } finally {
        this.assessmentRunning = false;
      }
    },
    viewAssessment(assessment) {
      this.selectedAssessment = assessment;
      this.assessmentDetailDialog = true;
    },
    async createMitigation(assessment) {
      try {
        // Navigate to mitigation tracking with pre-filled data
        this.$router.push({
          name: 'MitigationTracking',
          query: {
            batchId: assessment.batchId,
            riskLevel: assessment.riskLevel,
          },
        });
      } catch (error) {
      }
    },
    async reassessRisk(assessment) {
      this.assessmentRunning = true;
      try {
        const response = await axios.get(`/api/eudr/assess?batchId=${assessment.batchId}`);
        if (response.data.success) {
          this.$toast.success('Risk reassessment completed successfully');
          await this.loadRiskAssessments();
        }
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to reassess risk');
      } finally {
        this.assessmentRunning = false;
      }
    },
    viewOnHedera(assessment) {
      if (assessment.hederaTransactionId) {
        const hederaExplorerUrl = `https://hashscan.io/testnet/transaction/${assessment.hederaTransactionId}`;
        window.open(hederaExplorerUrl, '_blank');
      }
    },
  },
};
</script>

<style scoped>
.search-field {
  max-width: 300px;
}

.risk-breakdown-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #e0e0e0;
}

.risk-breakdown-item:last-child {
  border-bottom: none;
}

.risk-breakdown-item span {
  font-weight: 500;
}
</style>
