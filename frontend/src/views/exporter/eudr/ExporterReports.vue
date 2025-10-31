<template>
  <div class="exporter-reports">
    <v-container fluid>
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <div class="d-flex align-center justify-space-between mb-4">
            <div class="d-flex align-center">
              <v-icon large color="indigo" class="mr-3">mdi-chart-line</v-icon>
              <div>
                <h1 class="text-h4 font-weight-bold">EUDR Reports</h1>
                <p class="text-subtitle-1 text--secondary mb-0">
                  Generate and manage compliance reports for regulatory submission
                </p>
              </div>
            </div>
            <v-btn
              color="primary"
              @click="showGenerateDialog = true"
              elevation="2"
            >
              <v-icon left>mdi-plus</v-icon>
              Generate Report
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- Report Statistics -->
      <v-row class="mb-4">
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card generated" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-file-chart</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ totalReports }}</div>
              <div class="text-subtitle-2">Total Reports</div>
              <div class="text-caption">Generated this year</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card submitted" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-send</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ submittedReports }}</div>
              <div class="text-subtitle-2">Submitted</div>
              <div class="text-caption">To authorities</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card compliance-rate" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-shield-check</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ complianceRate }}%</div>
              <div class="text-subtitle-2">Compliance Rate</div>
              <div class="text-caption">Average across reports</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card volume" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-weight</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ totalVolume }}</div>
              <div class="text-subtitle-2">Total Volume (MT)</div>
              <div class="text-caption">Reported commodities</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Reports List -->
      <v-row>
        <v-col cols="12">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-format-list-bulleted</v-icon>
              Generated Reports
              <v-spacer></v-spacer>
              <v-text-field
                v-model="search"
                append-icon="mdi-magnify"
                label="Search reports..."
                single-line
                hide-details
                dense
                outlined
                class="search-field"
              ></v-text-field>
            </v-card-title>
            <v-data-table
              :headers="headers"
              :items="reports"
              :search="search"
              :loading="loading"
              class="elevation-0"
            >
              <template v-slot:item.reportId="{ item }">
                <div class="d-flex align-center">
                  <v-icon :color="getReportTypeColor(item.type)" class="mr-2">
                    {{ getReportTypeIcon(item.type) }}
                  </v-icon>
                  <span class="font-weight-medium">{{ item.reportId }}</span>
                </div>
              </template>
              <template v-slot:item.status="{ item }">
                <v-chip
                  :color="getStatusColor(item.status)"
                  text-color="white"
                  small
                >
                  {{ item.status }}
                </v-chip>
              </template>
              <template v-slot:item.generatedAt="{ item }">
                {{ formatDate(item.generatedAt) }}
              </template>
              <template v-slot:item.complianceScore="{ item }">
                <div class="d-flex align-center">
                  <v-progress-linear
                    :value="item.complianceScore"
                    :color="getComplianceColor(item.complianceScore)"
                    height="6"
                    rounded
                    class="mr-2"
                    style="width: 60px;"
                  ></v-progress-linear>
                  <span class="text-caption">{{ item.complianceScore }}%</span>
                </div>
              </template>
              <template v-slot:item.actions="{ item }">
                <v-btn
                  icon
                  small
                  color="primary"
                  @click="viewReport(item)"
                >
                  <v-icon>mdi-eye</v-icon>
                </v-btn>
                <v-btn
                  icon
                  small
                  color="success"
                  @click="downloadReport(item)"
                >
                  <v-icon>mdi-download</v-icon>
                </v-btn>
                <v-btn
                  v-if="item.status === 'Generated'"
                  icon
                  small
                  color="warning"
                  @click="submitReport(item)"
                >
                  <v-icon>mdi-send</v-icon>
                </v-btn>
              </template>
            </v-data-table>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Generate Report Dialog -->
    <v-dialog v-model="showGenerateDialog" max-width="600px" persistent>
      <v-card>
        <v-card-title class="text-h5">Generate New Report</v-card-title>
        <v-card-text>
          <v-form ref="generateForm" v-model="formValid">
            <v-select
              v-model="newReport.type"
              :items="reportTypes"
              label="Report Type"
              :rules="[v => !!v || 'Report type is required']"
              required
              outlined
            ></v-select>
            <v-text-field
              v-model="newReport.title"
              label="Report Title"
              :rules="[v => !!v || 'Title is required']"
              required
              outlined
            ></v-text-field>
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="newReport.periodFrom"
                  label="Period From"
                  type="date"
                  :rules="[v => !!v || 'Start date is required']"
                  required
                  outlined
                ></v-text-field>
              </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model="newReport.periodTo"
                  label="Period To"
                  type="date"
                  :rules="[v => !!v || 'End date is required']"
                  required
                  outlined
                ></v-text-field>
              </v-col>
            </v-row>
            <v-select
              v-model="newReport.commodities"
              :items="commodityOptions"
              label="Include Commodities"
              multiple
              chips
              outlined
            ></v-select>
            <v-textarea
              v-model="newReport.notes"
              label="Notes (Optional)"
              rows="3"
              outlined
            ></v-textarea>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="cancelGenerate">Cancel</v-btn>
          <v-btn
            color="primary"
            :disabled="!formValid"
            :loading="generating"
            @click="generateReport"
          >
            Generate Report
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
/* eslint-disable */
export default {
  name: 'ExporterReports',
  data() {
    return {
      loading: false,
      search: '',
      showGenerateDialog: false,
      formValid: false,
      generating: false,
      reportTypes: [
        { text: 'Monthly Compliance Report', value: 'monthly' },
        { text: 'Quarterly Summary', value: 'quarterly' },
        { text: 'Annual EUDR Report', value: 'annual' },
        { text: 'Supplier Audit Report', value: 'audit' },
        { text: 'Risk Assessment Report', value: 'risk' },
      ],
      commodityOptions: ['Coffee', 'Cocoa', 'Palm Oil', 'Soy', 'Rubber'],
      headers: [
        { text: 'Report ID', value: 'reportId', sortable: true },
        { text: 'Type', value: 'type', sortable: true },
        { text: 'Title', value: 'title', sortable: true },
        { text: 'Status', value: 'status', sortable: true },
        { text: 'Generated', value: 'generatedAt', sortable: true },
        { text: 'Compliance', value: 'complianceScore', sortable: true },
        {
          text: 'Actions', value: 'actions', sortable: false, width: '150px',
        },
      ],
      reports: [
        {
          id: 1,
          reportId: 'RPT-2024-001',
          type: 'monthly',
          title: 'December 2024 Compliance Report',
          status: 'Generated',
          generatedAt: '2024-12-15',
          complianceScore: 92,
          periodFrom: '2024-12-01',
          periodTo: '2024-12-31',
        },
        {
          id: 2,
          reportId: 'RPT-2024-002',
          type: 'quarterly',
          title: 'Q4 2024 Summary Report',
          status: 'Submitted',
          generatedAt: '2024-12-10',
          complianceScore: 88,
          periodFrom: '2024-10-01',
          periodTo: '2024-12-31',
        },
      ],
      newReport: {
        type: '',
        title: '',
        periodFrom: '',
        periodTo: '',
        commodities: [],
        notes: '',
      },
    };
  },
  computed: {
    totalReports() {
      return this.reports.length;
    },
    submittedReports() {
      return this.reports.filter((r) => r.status === 'Submitted').length;
    },
    complianceRate() {
      if (this.reports.length === 0) return 0;
      const total = this.reports.reduce((sum, report) => sum + report.complianceScore, 0);
      return Math.round(total / this.reports.length);
    },
    totalVolume() {
      return 1250; // Mock total volume
    },
  },
  methods: {
    getStatusColor(status) {
      const colors = {
        Generated: 'info',
        Submitted: 'success',
        Processing: 'warning',
        Approved: 'success',
      };
      return colors[status] || 'grey';
    },
    getReportTypeColor(type) {
      const colors = {
        monthly: 'blue',
        quarterly: 'green',
        annual: 'purple',
        audit: 'orange',
        risk: 'red',
      };
      return colors[type] || 'grey';
    },
    getReportTypeIcon(type) {
      const icons = {
        monthly: 'mdi-calendar-month',
        quarterly: 'mdi-calendar-range',
        annual: 'mdi-calendar',
        audit: 'mdi-clipboard-check',
        risk: 'mdi-alert-circle',
      };
      return icons[type] || 'mdi-file-chart';
    },
    getComplianceColor(score) {
      if (score >= 90) return 'success';
      if (score >= 80) return 'warning';
      return 'error';
    },
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString();
    },
    viewReport(report) {
    },
    downloadReport(report) {
    },
    submitReport(report) {
    },
    cancelGenerate() {
      this.showGenerateDialog = false;
      this.resetForm();
    },
    async generateReport() {
      if (!this.$refs.generateForm.validate()) return;

      this.generating = true;
      try {
        // Simulate API call
        await new Promise((resolve) => setTimeout(resolve, 2000));

        // Generate report ID
        const year = new Date().getFullYear();
        const sequence = String(this.reports.length + 1).padStart(3, '0');
        const reportId = `RPT-${year}-${sequence}`;

        // Add to list
        const newId = Math.max(...this.reports.map((r) => r.id)) + 1;
        this.reports.unshift({
          id: newId,
          reportId,
          type: this.newReport.type,
          title: this.newReport.title,
          status: 'Generated',
          generatedAt: new Date().toISOString().split('T')[0],
          complianceScore: Math.floor(Math.random() * 20) + 80, // Random score 80-100
          periodFrom: this.newReport.periodFrom,
          periodTo: this.newReport.periodTo,
        });

        this.showGenerateDialog = false;
        this.resetForm();
        this.$toast.success('Report generated successfully!');
      } catch (error) {
        this.$toast.error('Failed to generate report');
      } finally {
        this.generating = false;
      }
    },
    resetForm() {
      this.newReport = {
        type: '',
        title: '',
        periodFrom: '',
        periodTo: '',
        commodities: [],
        notes: '',
      };
      if (this.$refs.generateForm) {
        this.$refs.generateForm.resetValidation();
      }
    },
  },
};
</script>

<style scoped>
.exporter-reports {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px 0;
}
.stat-card {
  transition: transform 0.2s;
  color: white;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-card.generated {
  background: linear-gradient(135deg, #2196f3, #1565c0);
}
.stat-card.submitted {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
}
.stat-card.compliance-rate {
  background: linear-gradient(135deg, #9c27b0, #6a1b9a);
}
.stat-card.volume {
  background: linear-gradient(135deg, #ff9800, #f57c00);
}
.search-field {
  max-width: 300px;
}
</style>
