<template>
  <div class="exporter-eudr-dashboard">
    <v-container fluid>
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <div class="d-flex align-center mb-4">
            <v-icon large color="primary" class="mr-3">mdi-shield-account</v-icon>
            <div>
              <h1 class="text-h4 font-weight-bold">EUDR Compliance Dashboard</h1>
              <p class="text-subtitle-1 text--secondary mb-0">
                Monitor and manage your suppliers' EUDR compliance status
              </p>
            </div>
          </div>
        </v-col>
      </v-row>

      <!-- Key Metrics -->
      <v-row class="mb-4">
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card compliant" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-check-circle</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ metrics.compliantSuppliers }}</div>
              <div class="text-subtitle-2">Compliant Suppliers</div>
              <div class="text-caption">{{ getPercentage(metrics.compliantSuppliers, metrics.totalSuppliers) }}% of total</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card at-risk" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-alert-circle</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ metrics.atRiskSuppliers }}</div>
              <div class="text-subtitle-2">At-Risk Suppliers</div>
              <div class="text-caption">{{ getPercentage(metrics.atRiskSuppliers, metrics.totalSuppliers) }}% of total</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card non-compliant" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-close-circle</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ metrics.nonCompliantSuppliers }}</div>
              <div class="text-subtitle-2">Non-Compliant</div>
              <div class="text-caption">{{ getPercentage(metrics.nonCompliantSuppliers, metrics.totalSuppliers) }}% of total</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card total" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-account-group</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ metrics.totalSuppliers }}</div>
              <div class="text-subtitle-2">Total Suppliers</div>
              <div class="text-caption">Active in network</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Compliance Overview Chart -->
      <v-row class="mb-4">
        <v-col cols="12" md="8">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-chart-line</v-icon>
              Compliance Trends
            </v-card-title>
            <v-card-text>
              <div class="chart-placeholder">
                <v-row>
                  <v-col cols="12">
                    <div class="text-center py-8">
                      <v-icon size="64" color="grey lighten-2">mdi-chart-line</v-icon>
                      <div class="text-h6 grey--text mt-2">Compliance Trend Chart</div>
                      <div class="text-caption grey--text">Shows compliance progress over time</div>
                    </div>
                  </v-col>
                </v-row>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="4">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="warning" class="mr-2">mdi-clock-alert</v-icon>
              Upcoming Deadlines
            </v-card-title>
            <v-card-text>
              <v-list dense>
                <v-list-item
                  v-for="deadline in upcomingDeadlines"
                  :key="deadline.id"
                  class="deadline-item"
                >
                  <v-list-item-icon>
                    <v-icon :color="getDeadlineColor(deadline.daysLeft)">
                      {{ getDeadlineIcon(deadline.daysLeft) }}
                    </v-icon>
                  </v-list-item-icon>
                  <v-list-item-content>
                    <v-list-item-title>{{ deadline.title }}</v-list-item-title>
                    <v-list-item-subtitle>
                      {{ deadline.daysLeft }} days remaining
                    </v-list-item-subtitle>
                  </v-list-item-content>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Risk Heat Map -->
      <v-row class="mb-4">
        <v-col cols="12" md="6">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="error" class="mr-2">mdi-map-marker-alert</v-icon>
              Risk Heat Map
            </v-card-title>
            <v-card-text>
              <v-row>
                <v-col
                  cols="6" sm="4"
                  v-for="region in riskRegions"
                  :key="region.name"
                >
                  <div class="risk-region">
                    <div class="d-flex align-center justify-space-between mb-2">
                      <span class="font-weight-medium">{{ region.name }}</span>
                      <v-chip
                        :color="getRiskColor(region.riskLevel)"
                        text-color="white"
                        x-small
                      >
                        {{ region.riskLevel }}
                      </v-chip>
                    </div>
                    <v-progress-linear
                      :value="region.riskScore"
                      :color="getRiskColor(region.riskLevel)"
                      height="6"
                      rounded
                    ></v-progress-linear>
                    <div class="text-caption text--secondary mt-1">
                      {{ region.suppliers }} suppliers
                    </div>
                  </div>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="6">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="info" class="mr-2">mdi-alert-octagon</v-icon>
              Recent Alerts
            </v-card-title>
            <v-card-text>
              <v-list dense>
                <v-list-item
                  v-for="alert in recentAlerts"
                  :key="alert.id"
                  @click="viewAlert(alert)"
                  class="alert-item"
                >
                  <v-list-item-icon>
                    <v-icon :color="getAlertColor(alert.severity)">
                      {{ getAlertIcon(alert.type) }}
                    </v-icon>
                  </v-list-item-icon>
                  <v-list-item-content>
                    <v-list-item-title>{{ alert.title }}</v-list-item-title>
                    <v-list-item-subtitle>
                      {{ alert.supplier }} • {{ formatDate(alert.date) }}
                    </v-list-item-subtitle>
                  </v-list-item-content>
                  <v-list-item-action>
                    <v-chip
                      :color="getAlertColor(alert.severity)"
                      text-color="white"
                      x-small
                    >
                      {{ alert.severity }}
                    </v-chip>
                  </v-list-item-action>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Action Items -->
      <v-row>
        <v-col cols="12">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="warning" class="mr-2">mdi-clipboard-check</v-icon>
              Priority Actions Required
            </v-card-title>
            <v-card-text>
              <v-data-table
                :headers="actionHeaders"
                :items="priorityActions"
                :items-per-page="5"
                class="elevation-0"
              >
                <template v-slot:item.priority="{ item }">
                  <v-chip
                    :color="getPriorityColor(item.priority)"
                    text-color="white"
                    small
                  >
                    {{ item.priority }}
                  </v-chip>
                </template>

                <template v-slot:item.dueDate="{ item }">
                  {{ formatDate(item.dueDate) }}
                </template>

                <template v-slot:item.actions="{ item }">
                  <v-btn
                    icon
                    small
                    color="primary"
                    @click="handleAction(item)"
                  >
                    <v-icon>mdi-arrow-right</v-icon>
                  </v-btn>
                </template>
              </v-data-table>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
export default {
  name: 'ExporterEudrDashboard',

  data() {
    return {
      metrics: {
        compliantSuppliers: 45,
        atRiskSuppliers: 12,
        nonCompliantSuppliers: 8,
        totalSuppliers: 65,
      },

      upcomingDeadlines: [
        {
          id: 1,
          title: 'Q1 Compliance Report',
          daysLeft: 15,
        },
        {
          id: 2,
          title: 'Supplier Verification',
          daysLeft: 7,
        },
        {
          id: 3,
          title: 'Risk Assessment Update',
          daysLeft: 30,
        },
      ],

      riskRegions: [
        {
          name: 'Central Kenya',
          riskLevel: 'Low',
          riskScore: 25,
          suppliers: 15,
        },
        {
          name: 'Western Region',
          riskLevel: 'Medium',
          riskScore: 60,
          suppliers: 22,
        },
        {
          name: 'Eastern Province',
          riskLevel: 'High',
          riskScore: 85,
          suppliers: 8,
        },
        {
          name: 'Northern Areas',
          riskLevel: 'Low',
          riskScore: 30,
          suppliers: 12,
        },
        {
          name: 'Coastal Region',
          riskLevel: 'Medium',
          riskScore: 55,
          suppliers: 8,
        },
      ],

      recentAlerts: [
        {
          id: 1,
          title: 'Deforestation Alert',
          supplier: 'Green Valley Farm',
          type: 'deforestation',
          severity: 'High',
          date: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000),
        },
        {
          id: 2,
          title: 'Document Expired',
          supplier: 'Mountain Coffee Co.',
          type: 'document',
          severity: 'Medium',
          date: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
        },
        {
          id: 3,
          title: 'Missing GPS Coordinates',
          supplier: 'Sunrise Plantation',
          type: 'location',
          severity: 'Low',
          date: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000),
        },
      ],

      actionHeaders: [
        { text: 'Action', value: 'action', sortable: false },
        { text: 'Supplier', value: 'supplier', sortable: true },
        { text: 'Priority', value: 'priority', sortable: true },
        { text: 'Due Date', value: 'dueDate', sortable: true },
        {
          text: 'Actions', value: 'actions', sortable: false, width: '100px',
        },
      ],

      priorityActions: [
        {
          id: 1,
          action: 'Review deforestation alert',
          supplier: 'Green Valley Farm',
          priority: 'High',
          dueDate: new Date(Date.now() + 3 * 24 * 60 * 60 * 1000),
        },
        {
          id: 2,
          action: 'Verify land ownership documents',
          supplier: 'Mountain Coffee Co.',
          priority: 'Medium',
          dueDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
        },
        {
          id: 3,
          action: 'Update supplier risk assessment',
          supplier: 'Sunrise Plantation',
          priority: 'Low',
          dueDate: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000),
        },
      ],
    };
  },

  methods: {
    getPercentage(value, total) {
      return total > 0 ? Math.round((value / total) * 100) : 0;
    },

    getDeadlineColor(daysLeft) {
      if (daysLeft <= 7) return 'error';
      if (daysLeft <= 14) return 'warning';
      return 'success';
    },

    getDeadlineIcon(daysLeft) {
      if (daysLeft <= 7) return 'mdi-alert-circle';
      if (daysLeft <= 14) return 'mdi-clock-alert';
      return 'mdi-check-circle';
    },

    getRiskColor(riskLevel) {
      const colors = {
        Low: 'success',
        Medium: 'warning',
        High: 'error',
      };
      return colors[riskLevel] || 'grey';
    },

    getAlertColor(severity) {
      const colors = {
        High: 'error',
        Medium: 'warning',
        Low: 'info',
      };
      return colors[severity] || 'grey';
    },

    getAlertIcon(type) {
      const icons = {
        deforestation: 'mdi-tree-outline',
        document: 'mdi-file-alert',
        location: 'mdi-map-marker-alert',
      };
      return icons[type] || 'mdi-alert';
    },

    getPriorityColor(priority) {
      const colors = {
        High: 'error',
        Medium: 'warning',
        Low: 'info',
      };
      return colors[priority] || 'grey';
    },

    formatDate(date) {
      return new Date(date).toLocaleDateString();
    },

    viewAlert(alert) {
      this.$router.push({
        name: 'RiskManagement',
        query: { alertId: alert.id },
      });
    },

    handleAction(action) {
      // Navigate to appropriate page based on action type
      if (action.action.includes('deforestation')) {
        this.$router.push({ name: 'RiskManagement' });
      } else if (action.action.includes('documents')) {
        this.$router.push({ name: 'SupplierCompliance' });
      } else {
        this.$router.push({ name: 'DueDiligence' });
      }
    },
  },
};
</script>

<style scoped>
.exporter-eudr-dashboard {
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

.metric-card.at-risk {
  background: linear-gradient(135deg, #ff9800, #f57c00);
}

.metric-card.non-compliant {
  background: linear-gradient(135deg, #f44336, #c62828);
}

.metric-card.total {
  background: linear-gradient(135deg, #2196f3, #1565c0);
}

.chart-placeholder {
  height: 300px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.deadline-item {
  cursor: pointer;
  transition: background-color 0.2s;
}

.deadline-item:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.risk-region {
  padding: 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.7);
  margin-bottom: 12px;
}

.alert-item {
  cursor: pointer;
  transition: background-color 0.2s;
}

.alert-item:hover {
  background-color: rgba(0, 0, 0, 0.04);
}
</style>
