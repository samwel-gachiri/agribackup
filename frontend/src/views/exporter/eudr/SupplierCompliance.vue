<template>
  <div class="supplier-compliance">
    <v-container fluid>
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <div class="d-flex align-center justify-space-between mb-4">
            <div class="d-flex align-center">
              <v-icon large color="green" class="mr-3">mdi-account-group</v-icon>
              <div>
                <h1 class="text-h4 font-weight-bold">Supplier Compliance</h1>
                <p class="text-subtitle-1 text--secondary mb-0">
                  Manage and verify all farmer suppliers' EUDR compliance
                </p>
              </div>
            </div>
            <v-btn
              color="primary"
              @click="refreshData"
              :loading="loading"
              elevation="2"
            >
              <v-icon left>mdi-refresh</v-icon>
              Refresh
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- Compliance Overview -->
      <v-row class="mb-4" v-if="!loading && complianceOverview">
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card compliant" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-check-circle</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ complianceOverview.compliantSuppliers }}</div>
              <div class="text-subtitle-2">Compliant Suppliers</div>
              <div class="text-caption">{{ getPercentage(complianceOverview.compliantSuppliers, complianceOverview.totalSuppliers) }}% of total</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card pending" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-clock-outline</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ complianceOverview.pendingSuppliers }}</div>
              <div class="text-subtitle-2">Pending Review</div>
              <div class="text-caption">{{ getPercentage(complianceOverview.pendingSuppliers, complianceOverview.totalSuppliers) }}% of total</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card at-risk" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-alert-circle</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ complianceOverview.atRiskSuppliers }}</div>
              <div class="text-subtitle-2">At Risk</div>
              <div class="text-caption">{{ getPercentage(complianceOverview.atRiskSuppliers, complianceOverview.totalSuppliers) }}% of total</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card total" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-account-multiple</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ complianceOverview.totalSuppliers }}</div>
              <div class="text-subtitle-2">Total Suppliers</div>
              <div class="text-caption">Active in network</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Loading State -->
      <v-row v-if="loading" class="mb-4">
        <v-col cols="12" sm="6" md="3" v-for="n in 4" :key="n">
          <v-card elevation="4" class="metric-card">
            <v-card-text class="text-center">
              <v-skeleton-loader type="avatar" class="mb-2"></v-skeleton-loader>
              <v-skeleton-loader type="heading" class="mb-1"></v-skeleton-loader>
              <v-skeleton-loader type="text" width="60%"></v-skeleton-loader>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Suppliers Table -->
      <v-row>
        <v-col cols="12">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-format-list-bulleted</v-icon>
              Supplier Compliance Status
            </v-card-title>
            <v-data-table
              :headers="headers"
              :items="suppliers"
              :loading="loading"
              class="elevation-0"
            >
              <template v-slot:item.supplier="{ item }">
                <div class="d-flex align-center">
                  <v-avatar size="32" class="mr-3">
                    <v-icon color="primary">mdi-account</v-icon>
                  </v-avatar>
                  <div>
                    <div class="font-weight-medium">{{ item.supplierName }}</div>
                    <div class="text-caption text--secondary">{{ item.supplierId.substring(0, 8) }}</div>
                  </div>
                </div>
              </template>
              <template v-slot:item.complianceStatus="{ item }">
                <v-chip
                  :color="getComplianceColor(item.complianceStatus)"
                  text-color="white"
                  small
                >
                  {{ formatComplianceStatus(item.complianceStatus) }}
                </v-chip>
              </template>
              <template v-slot:item.complianceScore="{ item }">
                <div class="d-flex align-center">
                  <v-progress-circular
                    :value="item.complianceScore * 100"
                    :color="getScoreColor(item.complianceScore)"
                    size="40"
                    width="4"
                    class="mr-2"
                  >
                    <span class="text-caption">{{ Math.round(item.complianceScore * 100) }}%</span>
                  </v-progress-circular>
                </div>
              </template>
              <template v-slot:item.lastAssessmentDate="{ item }">
                <div class="text-caption">
                  {{ item.lastAssessmentDate ? formatDate(item.lastAssessmentDate) : 'Never' }}
                </div>
              </template>
              <template v-slot:item.actions="{ item }">
                <v-btn
                  icon
                  small
                  color="primary"
                  @click="viewSupplier(item)"
                >
                  <v-icon>mdi-eye</v-icon>
                </v-btn>
              </template>
            </v-data-table>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Supplier Details Dialog -->
    <v-dialog v-model="supplierDialog" max-width="800px">
      <v-card v-if="selectedSupplier">
        <v-card-title class="d-flex align-center">
          <v-icon color="primary" class="mr-2">mdi-account</v-icon>
          {{ selectedSupplier.supplierName }}
          <v-spacer></v-spacer>
          <v-btn icon @click="supplierDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <v-card outlined class="pa-3">
                <h3 class="text-h6 mb-3">Compliance Details</h3>
                <div class="d-flex align-center mb-2">
                  <v-chip :color="getComplianceColor(selectedSupplier.complianceStatus)" text-color="white" class="mr-2">
                    {{ formatComplianceStatus(selectedSupplier.complianceStatus) }}
                  </v-chip>
                  <span class="text-h6">{{ Math.round(selectedSupplier.complianceScore * 100) }}% Score</span>
                </div>
                <div class="text-caption text--secondary mb-2">
                  Last Assessment: {{ selectedSupplier.lastAssessmentDate ? formatDate(selectedSupplier.lastAssessmentDate) : 'Never' }}
                </div>
                <div class="mb-2">
                  <strong>Total Batches:</strong> {{ selectedSupplier.totalBatches }}
                </div>
                <div class="mb-2">
                  <strong>Compliant Batches:</strong> {{ selectedSupplier.compliantBatches }}
                </div>
                <div class="mb-2">
                  <strong>At Risk Batches:</strong> {{ selectedSupplier.atRiskBatches }}
                </div>
              </v-card>
            </v-col>
            <v-col cols="12" md="6">
              <v-card outlined class="pa-3">
                <h3 class="text-h6 mb-3">Supplier Information</h3>
                <div class="mb-2">
                  <strong>ID:</strong> {{ selectedSupplier.supplierId }}
                </div>
                <div class="mb-2">
                  <strong>Region:</strong> {{ selectedSupplier.region }}
                </div>
                <div class="mb-2">
                  <strong>Primary Commodity:</strong> {{ selectedSupplier.primaryCommodity }}
                </div>
              </v-card>
            </v-col>
          </v-row>
          <v-row v-if="selectedSupplier.riskFactors.length > 0">
            <v-col cols="12">
              <v-card outlined class="pa-3">
                <h3 class="text-h6 mb-3">Risk Factors</h3>
                <v-chip
                  v-for="factor in selectedSupplier.riskFactors"
                  :key="factor"
                  small
                  color="orange"
                  text-color="white"
                  class="mr-2 mb-2"
                >
                  {{ factor }}
                </v-chip>
              </v-card>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
/* eslint-disable */
import axios from 'axios';

export default {
  name: 'SupplierCompliance',
  data() {
    return {
      loading: false,
      complianceOverview: null,
      suppliers: [],
      headers: [
        { text: 'Supplier', value: 'supplier', sortable: true },
        { text: 'Region', value: 'region', sortable: true },
        { text: 'Commodity', value: 'primaryCommodity', sortable: true },
        { text: 'Compliance', value: 'complianceStatus', sortable: true },
        { text: 'Score', value: 'complianceScore', sortable: true },
        { text: 'Last Assessment', value: 'lastAssessmentDate', sortable: true },
        {
          text: 'Actions', value: 'actions', sortable: false, width: '150px',
        },
      ],
      supplierDialog: false,
      selectedSupplier: null,
    };
  },
  mounted() {
    this.fetchSupplierCompliance();
  },
  methods: {
    async fetchSupplierCompliance() {
      this.loading = true;
      try {
        const response = await axios.get('/api/eudr/supplier-compliance');
        if (response.data.success) {
          this.complianceOverview = response.data.data;
          this.suppliers = response.data.data.suppliers;
        } else {
          this.$toast.error(response.data.message || 'Failed to fetch supplier compliance data');
        }
      } catch (error) {
        this.$toast.error('Failed to load supplier compliance data');
        // Fallback to empty data
        this.complianceOverview = {
          totalSuppliers: 0,
          compliantSuppliers: 0,
          pendingSuppliers: 0,
          atRiskSuppliers: 0,
          suppliers: []
        };
        this.suppliers = [];
      } finally {
        this.loading = false;
      }
    },
    refreshData() {
      this.fetchSupplierCompliance();
    },
    getPercentage(value, total) {
      return total > 0 ? Math.round((value / total) * 100) : 0;
    },
    getComplianceColor(status) {
      const colors = {
        COMPLIANT: 'success',
        PENDING_REVIEW: 'warning',
        AT_RISK: 'error',
      };
      return colors[status] || 'grey';
    },
    formatComplianceStatus(status) {
      const labels = {
        COMPLIANT: 'Compliant',
        PENDING_REVIEW: 'Pending Review',
        AT_RISK: 'At Risk',
      };
      return labels[status] || status;
    },
    getScoreColor(score) {
      if (score >= 0.8) return 'success';
      if (score >= 0.6) return 'warning';
      return 'error';
    },
    formatDate(dateString) {
      if (!dateString) return 'Never';
      return new Date(dateString).toLocaleDateString();
    },
    viewSupplier(supplier) {
      this.selectedSupplier = supplier;
      this.supplierDialog = true;
    },
  },
};
</script>

<style scoped>
.supplier-compliance {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px 0;
}
.metric-card {
  transition: transform 0.2s;
  color: white;
}
.metric-card:hover {
  transform: translateY(-2px);
}
.metric-card.compliant {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
}
.metric-card.pending {
  background: linear-gradient(135deg, #ff9800, #f57c00);
}
.metric-card.at-risk {
  background: linear-gradient(135deg, #f44336, #c62828);
}
.metric-card.total {
  background: linear-gradient(135deg, #2196f3, #1565c0);
}
</style>
