<template>
  <div class="farmer-reports">
    <v-container fluid>
      <!-- Header -->
      <div class="reports-header mb-6">
        <h1 class="text-h4 font-weight-bold text-primary">
          <v-icon large color="primary" class="mr-3">mdi-chart-line</v-icon>
          Farmer Reports & Analytics
        </h1>
        <p class="text-subtitle-1 text--secondary mt-2">
          Track your farming performance, yield trends, and harvest accuracy
        </p>
      </div>

      <!-- Date Range Selector -->
      <v-row class="mb-6">
        <v-col cols="12" md="6">
          <v-card class="elevation-2">
            <v-card-title class="primary white--text">
              <v-icon left color="white">mdi-calendar-range</v-icon>
              Report Period
            </v-card-title>
            <v-card-text class="pa-4">
              <v-row>
                <v-col cols="12" sm="6">
                  <v-menu
                    v-model="startDateMenu"
                    :close-on-content-click="false"
                    transition="scale-transition"
                    offset-y
                    min-width="auto"
                  >
                    <template v-slot:activator="{ on, attrs }">
                      <v-text-field
                        v-model="dateRange.start"
                        label="Start Date"
                        prepend-icon="mdi-calendar"
                        readonly
                        outlined
                        dense
                        v-bind="attrs"
                        v-on="on"
                      ></v-text-field>
                    </template>
                    <v-date-picker
                      v-model="dateRange.start"
                      @input="startDateMenu = false; loadReportsData()"
                      :max="dateRange.end || today"
                    ></v-date-picker>
                  </v-menu>
                </v-col>
                <v-col cols="12" sm="6">
                  <v-menu
                    v-model="endDateMenu"
                    :close-on-content-click="false"
                    transition="scale-transition"
                    offset-y
                    min-width="auto"
                  >
                    <template v-slot:activator="{ on, attrs }">
                      <v-text-field
                        v-model="dateRange.end"
                        label="End Date"
                        prepend-icon="mdi-calendar"
                        readonly
                        outlined
                        dense
                        v-bind="attrs"
                        v-on="on"
                      ></v-text-field>
                    </template>
                    <v-date-picker
                      v-model="dateRange.end"
                      @input="endDateMenu = false; loadReportsData()"
                      :min="dateRange.start"
                      :max="today"
                    ></v-date-picker>
                  </v-menu>
                </v-col>
              </v-row>
              <v-row class="mt-2">
                <v-col cols="12">
                  <v-btn-toggle v-model="quickPeriod" @change="setQuickPeriod" mandatory>
                    <v-btn value="7d" small>Last 7 Days</v-btn>
                    <v-btn value="30d" small>Last 30 Days</v-btn>
                    <v-btn value="90d" small>Last 3 Months</v-btn>
                    <v-btn value="1y" small>Last Year</v-btn>
                  </v-btn-toggle>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Export Options -->
        <v-col cols="12" md="6">
          <v-card class="elevation-2">
            <v-card-title class="success white--text">
              <v-icon left color="white">mdi-download</v-icon>
              Export Reports
            </v-card-title>
            <v-card-text class="pa-4">
              <v-row>
                <v-col cols="12" sm="6">
                  <v-btn
                    block
                    color="error"
                    @click="exportToPDF"
                    :loading="exportingPDF"
                  >
                    <v-icon left>mdi-file-pdf-box</v-icon>
                    Export PDF
                  </v-btn>
                </v-col>
                <v-col cols="12" sm="6">
                  <v-btn
                    block
                    color="success"
                    @click="exportToCSV"
                    :loading="exportingCSV"
                  >
                    <v-icon left>mdi-file-excel</v-icon>
                    Export CSV
                  </v-btn>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Key Metrics Summary -->
      <v-row class="mb-6">
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card elevation-2" color="primary">
            <v-card-text class="white--text">
              <div class="d-flex align-center">
                <div class="flex-grow-1">
                  <div class="text-h4 font-weight-bold">{{ metrics.totalYields }}</div>
                  <div class="text-subtitle-2">Total Yields</div>
                </div>
                <v-icon size="48" color="white" class="ml-3">mdi-sprout</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card elevation-2" color="success">
            <v-card-text class="white--text">
              <div class="d-flex align-center">
                <div class="flex-grow-1">
                  <div class="text-h4 font-weight-bold">{{ formatNumber(metrics.totalProduction) }}</div>
                  <div class="text-subtitle-2">Total Production (kg)</div>
                </div>
                <v-icon size="48" color="white" class="ml-3">mdi-weight</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card elevation-2" color="warning">
            <v-card-text class="white--text">
              <div class="d-flex align-center">
                <div class="flex-grow-1">
                  <div class="text-h4 font-weight-bold">{{ metrics.harvestAccuracy }}%</div>
                  <div class="text-subtitle-2">Harvest Accuracy</div>
                </div>
                <v-icon size="48" color="white" class="ml-3">mdi-target</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card elevation-2" color="info">
            <v-card-text class="white--text">
              <div class="d-flex align-center">
                <div class="flex-grow-1">
                  <div class="text-h4 font-weight-bold">{{ metrics.avgGrowthDays }}</div>
                  <div class="text-subtitle-2">Avg Growth Days</div>
                </div>
                <v-icon size="48" color="white" class="ml-3">mdi-calendar-clock</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row>
        <!-- Yield Trends Chart -->
        <v-col cols="12" lg="8">
          <v-card class="elevation-2">
            <v-card-title class="primary white--text">
              <v-icon left color="white">mdi-chart-line</v-icon>
              Yield Trends Over Time
            </v-card-title>
            <v-card-text class="pa-4">
              <div v-if="loadingCharts" class="text-center pa-6">
                <v-progress-circular indeterminate color="primary"></v-progress-circular>
                <p class="mt-3 text--secondary">Loading chart data...</p>
              </div>
              <div v-else-if="!hasYieldData" class="text-center pa-6">
                <v-icon size="64" color="grey lighten-2">mdi-chart-line</v-icon>
                <p class="text-h6 text--secondary mt-3">No yield data available</p>
                <p class="text--secondary">Record some yields to see trends</p>
              </div>
              <div v-else class="chart-container">
                <apexchart
                  type="line"
                  height="300"
                  :options="yieldTrendsOptions"
                  :series="yieldTrendsSeries"
                ></apexchart>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Harvest Accuracy Chart -->
        <v-col cols="12" lg="4">
          <v-card class="elevation-2">
            <v-card-title class="warning white--text">
              <v-icon left color="white">mdi-target</v-icon>
              Harvest Accuracy
            </v-card-title>
            <v-card-text class="pa-4">
              <div v-if="loadingCharts" class="text-center pa-6">
                <v-progress-circular indeterminate color="primary"></v-progress-circular>
              </div>
              <div v-else-if="!hasAccuracyData" class="text-center pa-6">
                <v-icon size="64" color="grey lighten-2">mdi-target</v-icon>
                <p class="text-h6 text--secondary mt-3">No accuracy data</p>
                <p class="text--secondary">Need harvest predictions to calculate accuracy</p>
              </div>
              <div v-else class="chart-container">
                <apexchart
                  type="donut"
                  height="300"
                  :options="accuracyOptions"
                  :series="accuracySeries"
                ></apexchart>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
import axios from 'axios';
import { getCurrentUserId } from '@/utils/roles.js';

export default {
  name: 'FarmerReports',
  data() {
    return {
      loadingCharts: false,
      loadingTable: false,
      exportingPDF: false,
      exportingCSV: false,
      startDateMenu: false,
      endDateMenu: false,
      quickPeriod: '30d',
      search: '',
      dateRange: {
        start: null,
        end: null,
      },
      metrics: {
        totalYields: 0,
        totalProduction: 0,
        harvestAccuracy: 0,
        avgGrowthDays: 0,
      },
      yieldRecords: [],
      hasYieldData: false,
      hasAccuracyData: false,
      // ApexCharts data
      yieldTrendsSeries: [{
        name: 'Yield Amount',
        data: [],
      }],
      yieldTrendsOptions: {
        chart: {
          type: 'line',
          toolbar: {
            show: false,
          },
        },
        stroke: {
          curve: 'smooth',
          width: 3,
        },
        colors: ['#1976d2'],
        xaxis: {
          categories: [],
          title: {
            text: 'Date',
          },
        },
        yaxis: {
          title: {
            text: 'Yield Amount (kg)',
          },
        },
        fill: {
          type: 'gradient',
          gradient: {
            shadeIntensity: 1,
            opacityFrom: 0.3,
            opacityTo: 0.1,
          },
        },
        tooltip: {
          y: {
            formatter: (val) => `${val} kg`,
          },
        },
      },
      accuracySeries: [],
      accuracyOptions: {
        chart: {
          type: 'donut',
        },
        labels: ['Accurate Predictions', 'Inaccurate Predictions'],
        colors: ['#4caf50', '#f44336'],
        legend: {
          position: 'bottom',
        },
        tooltip: {
          y: {
            formatter: (val) => `${val}%`,
          },
        },
      },
    };
  },
  async mounted() {
    this.setQuickPeriod('30d');
    await this.loadReportsData();
    this.initializeCharts();
  },
  methods: {
    setQuickPeriod(period) {
      const end = new Date();
      const start = new Date();

      // eslint-disable-next-line default-case
      switch (period) {
        case '7d':
          start.setDate(end.getDate() - 7);
          break;
        case '30d':
          start.setDate(end.getDate() - 30);
          break;
        case '90d':
          start.setDate(end.getDate() - 90);
          break;
        case '1y':
          start.setFullYear(end.getFullYear() - 1);
          break;
      }

      this.dateRange.start = start.toISOString().substr(0, 10);
      this.dateRange.end = end.toISOString().substr(0, 10);
      this.loadReportsData();
    },

    async loadReportsData() {
      try {
        this.loadingTable = true;
        const params = {
          startDate: this.dateRange.start,
          endDate: this.dateRange.end,
        };

        const response = await axios.get(`/api/farmers/${getCurrentUserId()}/reports`, { params });
        const data = response.data;

        this.metrics = data.metrics || this.metrics;
        this.yieldRecords = data.yieldRecords || [];

        // Check if we have data for charts
        this.hasYieldData = this.yieldRecords.length > 0;
        this.hasAccuracyData = this.yieldRecords.some((record) => record.accuracy !== null);
      } catch (error) {
        this.$toast.error('Failed to load reports data');
      } finally {
        this.loadingTable = false;
      }
    },

    async initializeCharts() {
      if (!this.hasYieldData && !this.hasAccuracyData) return;

      this.loadingCharts = true;
      const params = {
        startDate: this.dateRange.start,
        endDate: this.dateRange.end,
      };

      const response = await axios.get(`/api/farmers/${getCurrentUserId()}/reports/charts`, { params });
      const chartData = response.data;

      await this.$nextTick();

      // Update yield trends chart
      if (this.hasYieldData && chartData.yieldTrends) {
        this.yieldTrendsOptions.xaxis.categories = chartData.yieldTrends.labels || [];
        this.yieldTrendsSeries[0].data = chartData.yieldTrends.values || [];
      }

      // Update accuracy chart
      if (this.hasAccuracyData && chartData.accuracy) {
        this.accuracySeries = [chartData.accuracy.accurate || 0, chartData.accuracy.inaccurate || 0];
      }
      this.loadingCharts = false;
    },

    async exportToPDF() {
      this.exportingPDF = true;
      try {
        const params = {
          startDate: this.dateRange.start,
          endDate: this.dateRange.end,
        };

        const response = await axios.get(`/api/farmers/${getCurrentUserId()}/reports/export/pdf`, {
          params,
          responseType: 'blob',
        });

        const blob = new Blob([response.data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `farmer-reports-${this.dateRange.start}-to-${this.dateRange.end}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);

        this.$toast.success('PDF report exported successfully');
      } catch (error) {
        this.$toast.error('Failed to export PDF report');
      } finally {
        this.exportingPDF = false;
      }
    },

    async exportToCSV() {
      this.exportingCSV = true;
      try {
        const params = {
          startDate: this.dateRange.start,
          endDate: this.dateRange.end,
        };

        const response = await axios.get(`/api/farmers/${getCurrentUserId()}/reports/export/csv`, {
          params,
          responseType: 'blob',
        });

        const blob = new Blob([response.data], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `farmer-reports-${this.dateRange.start}-to-${this.dateRange.end}.csv`;
        link.click();
        window.URL.revokeObjectURL(url);

        this.$toast.success('CSV report exported successfully');
      } catch (error) {
        this.$toast.error('Failed to export CSV report');
      } finally {
        this.exportingCSV = false;
      }
    },

    formatNumber(number) {
      if (!number) return '0';
      return new Intl.NumberFormat().format(number);
    },
  },
};
</script>

<style scoped>
.farmer-reports {
  background-color: #f8fafc;
  min-height: 100vh;
  padding: 1rem 0;
}

.reports-header {
  text-align: center;
  margin-bottom: 2rem;
}

.metric-card {
  border-radius: 8px;
  transition: transform 0.2s ease;
  margin-bottom: 1rem;
}

.metric-card:hover {
  transform: translateY(-2px);
}

.v-card {
  border-radius: 8px;
  margin-bottom: 1rem;
}

.v-card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.chart-container {
  position: relative;
  height: 300px;
  width: 100%;
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .farmer-reports {
    padding: 0.5rem 0;
  }

  .reports-header {
    margin-bottom: 1rem;
  }

  .reports-header h1 {
    font-size: 1.5rem !important;
  }

  .metric-card .text-h4 {
    font-size: 1.8rem !important;
  }

  .chart-container {
    height: 250px;
  }

  .v-card-text {
    padding: 1rem !important;
  }
}

@media (max-width: 480px) {
  .reports-header h1 {
    font-size: 1.3rem !important;
  }

  .metric-card .text-h4 {
    font-size: 1.5rem !important;
  }

  .chart-container {
    height: 200px;
  }

  .v-card-title {
    font-size: 1rem !important;
    padding: 0.75rem 1rem !important;
  }

  .v-card-text {
    padding: 0.75rem !important;
  }

  .v-btn-toggle .v-btn {
    font-size: 0.75rem;
    padding: 0 8px;
  }
}

/* Touch-friendly interfaces */
@media (hover: none) and (pointer: coarse) {
  .v-btn {
    min-height: 44px;
    min-width: 44px;
  }

  .metric-card:hover {
    transform: none;
  }
}

/* Clean professional styling */
.farmer-reports {
  background: #f8fafc;
}

/* Ensure proper spacing on all screen sizes */
.v-container {
  padding: 0 1rem;
}

@media (min-width: 1264px) {
  .v-container {
    max-width: 1200px;
  }
}
</style>
