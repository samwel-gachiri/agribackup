<template>
  <div class="buyer-dashboard">
    <v-container fluid>
      <!-- Header -->
      <div class="dashboard-header mb-6">
        <h1 class="text-h4 font-weight-bold text-primary">
          <v-icon large color="primary" class="mr-3">mdi-cart</v-icon>
          Buyer Dashboard
        </h1>
        <p class="text-subtitle-1 text--secondary mt-2">
          Welcome back! Here's your agricultural marketplace overview
        </p>
      </div>

      <!-- Key Metrics Cards -->
      <v-row class="mb-6">
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card elevation-2" color="primary">
            <v-card-text class="white--text">
              <div class="d-flex align-center">
                <div class="flex-grow-1">
                  <div class="text-h4 font-weight-bold">{{ metrics.totalFarmers }}</div>
                  <div class="text-subtitle-2">Connected Farmers</div>
                </div>
                <v-icon size="48" color="white" class="ml-3">mdi-account-group</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card elevation-2" color="success">
            <v-card-text class="white--text">
              <div class="d-flex align-center">
                <div class="flex-grow-1">
                  <div class="text-h4 font-weight-bold">{{ metrics.totalOrders }}</div>
                  <div class="text-subtitle-2">Total Orders</div>
                </div>
                <v-icon size="48" color="white" class="ml-3">mdi-cart-outline</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card elevation-2" color="warning">
            <v-card-text class="white--text">
              <div class="d-flex align-center">
                <div class="flex-grow-1">
                  <div class="text-h4 font-weight-bold">${{ formatCurrency(metrics.totalSpent) }}</div>
                  <div class="text-subtitle-2">Total Spent</div>
                </div>
                <v-icon size="48" color="white" class="ml-3">mdi-currency-usd</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card elevation-2" color="info">
            <v-card-text class="white--text">
              <div class="d-flex align-center">
                <div class="flex-grow-1">
                  <div class="text-h4 font-weight-bold">{{ metrics.activeListings }}</div>
                  <div class="text-subtitle-2">Available Listings</div>
                </div>
                <v-icon size="48" color="white" class="ml-3">mdi-format-list-bulleted</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row>
        <!-- Orders Chart -->
        <v-col cols="12" md="8">
          <v-card class="elevation-2">
            <v-card-title class="primary white--text">
              <v-icon left color="white">mdi-chart-line</v-icon>
              Order Trends (Last 6 Months)
            </v-card-title>
            <v-card-text class="pa-4">
              <div v-if="loadingCharts" class="text-center pa-6">
                <v-progress-circular indeterminate color="primary"></v-progress-circular>
                <p class="mt-3 text--secondary">Loading chart data...</p>
              </div>
              <div v-else-if="!hasOrderData" class="text-center pa-6">
                <v-icon size="64" color="grey lighten-2">mdi-chart-line</v-icon>
                <p class="text-h6 text--secondary mt-3">No order data available</p>
                <p class="text--secondary">Start placing orders to see trends</p>
              </div>
              <div v-else class="chart-container">
                <apexchart
                  type="line"
                  height="300"
                  :options="ordersChartOptions"
                  :series="ordersChartSeries"
                ></apexchart>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Top Products -->
        <v-col cols="12" md="4">
          <v-card class="elevation-2">
            <v-card-title class="success white--text">
              <v-icon left color="white">mdi-trophy</v-icon>
              Top Products
            </v-card-title>
            <v-card-text class="pa-0">
              <v-list v-if="topProducts.length > 0">
                <v-list-item
                  v-for="(product, index) in topProducts"
                  :key="product.id"
                >
                  <v-list-item-avatar>
                    <v-chip :color="getProductColor(index)" text-color="white" small>
                      {{ index + 1 }}
                    </v-chip>
                  </v-list-item-avatar>
                  <v-list-item-content>
                    <v-list-item-title>{{ product.name }}</v-list-item-title>
                    <v-list-item-subtitle>
                      {{ product.totalOrders }} orders • ${{ formatCurrency(product.totalSpent) }}
                    </v-list-item-subtitle>
                  </v-list-item-content>
                  <v-list-item-action>
                    <v-progress-linear
                      :value="topProducts[0] ? (product.totalSpent / topProducts[0].totalSpent) * 100 : 0"
                      height="4"
                      color="success"
                    ></v-progress-linear>
                  </v-list-item-action>
                </v-list-item>
              </v-list>
              <div v-else class="text-center pa-6">
                <v-icon size="64" color="grey lighten-2">mdi-trophy</v-icon>
                <p class="text-h6 text--secondary mt-3">No products yet</p>
                <p class="text--secondary">Start ordering to see your top products</p>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row class="mt-4">
        <!-- Spend Analysis -->
        <v-col cols="12" md="6">
          <v-card class="elevation-2">
            <v-card-title class="warning white--text">
              <v-icon left color="white">mdi-chart-pie</v-icon>
              Spend Analysis
            </v-card-title>
            <v-card-text class="pa-4">
              <div v-if="loadingCharts" class="text-center pa-6">
                <v-progress-circular indeterminate color="primary"></v-progress-circular>
              </div>
              <div v-else-if="!hasSpendData" class="text-center pa-6">
                <v-icon size="64" color="grey lighten-2">mdi-chart-pie</v-icon>
                <p class="text-h6 text--secondary mt-3">No spending data</p>
                <p class="text--secondary">Make purchases to see analysis</p>
              </div>
              <div v-else class="chart-container">
                <apexchart
                  type="donut"
                  height="300"
                  :options="spendChartOptions"
                  :series="spendChartSeries"
                ></apexchart>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Farmer Performance Overview -->
        <v-col cols="12" md="6">
          <v-card class="elevation-2">
            <v-card-title class="info white--text">
              <v-icon left color="white">mdi-account-star</v-icon>
              Top Performing Farmers
            </v-card-title>
            <v-card-text class="pa-0">
              <v-list v-if="topFarmers.length > 0">
                <v-list-item
                  v-for="farmer in topFarmers"
                  :key="farmer.id"
                  @click="viewFarmerDetails(farmer)"
                >
                  <v-list-item-avatar>
                    <v-img
                      :src="farmer.avatar || '/default-avatar.png'"
                      :alt="farmer.name"
                    ></v-img>
                  </v-list-item-avatar>
                  <v-list-item-content>
                    <v-list-item-title>{{ farmer.name }}</v-list-item-title>
                    <v-list-item-subtitle>
                      <div class="d-flex align-center">
                        <v-rating
                          :value="farmer.rating"
                          color="amber"
                          dense
                          half-increments
                          readonly
                          size="16"
                          class="mr-2"
                        ></v-rating>
                        <span class="text-caption">{{ farmer.rating }}/5</span>
                      </div>
                      <div class="text-caption">
                        {{ farmer.totalOrders }} orders • {{ farmer.reliabilityScore }}% reliable
                      </div>
                    </v-list-item-subtitle>
                  </v-list-item-content>
                  <v-list-item-action>
                    <v-btn icon small @click.stop="viewFarmerDetails(farmer)">
                      <v-icon>mdi-chevron-right</v-icon>
                    </v-btn>
                  </v-list-item-action>
                </v-list-item>
              </v-list>
              <div v-else class="text-center pa-6">
                <v-icon size="64" color="grey lighten-2">mdi-account-star</v-icon>
                <p class="text-h6 text--secondary mt-3">No farmers connected</p>
                <p class="text--secondary">Connect with farmers to see performance data</p>
              </div>
              <div v-if="topFarmers.length > 0" class="pa-3 text-center">
                <v-btn
                  text
                  color="primary"
                  @click="viewAllFarmers"
                >
                  View All Farmers
                  <v-icon right>mdi-arrow-right</v-icon>
                </v-btn>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Recent Activity -->
      <v-row class="mt-4">
        <v-col cols="12">
          <v-card class="elevation-2">
            <v-card-title class="grey darken-2 white--text">
              <v-icon left color="white">mdi-clock-outline</v-icon>
              Recent Activity
            </v-card-title>
            <v-card-text class="pa-0">
              <v-timeline dense>
                <v-timeline-item
                  v-for="activity in recentActivities"
                  :key="activity.id"
                  :color="activity.color"
                  small
                >
                  <template v-slot:icon>
                    <v-icon color="white">{{ activity.icon }}</v-icon>
                  </template>
                  <v-card class="elevation-1">
                    <v-card-text class="pa-3">
                      <div class="font-weight-medium">{{ activity.title }}</div>
                      <div class="text-caption text--secondary">{{ activity.description }}</div>
                      <div class="text-caption text--secondary mt-1">
                        {{ formatDateTime(activity.timestamp) }}
                      </div>
                    </v-card-text>
                  </v-card>
                </v-timeline-item>
              </v-timeline>
              <div v-if="recentActivities.length === 0" class="text-center pa-6">
                <v-icon size="64" color="grey lighten-2">mdi-clock-outline</v-icon>
                <p class="text-h6 text--secondary mt-3">No recent activity</p>
                <p class="text--secondary">Start by browsing listings or connecting with farmers</p>
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
import VueApexCharts from 'vue-apexcharts';
import { getCurrentUserId } from '@/utils/roles.js';

export default {
  name: 'BuyerDashboard',
  components: {
    apexchart: VueApexCharts,
  },
  data() {
    return {
      loadingCharts: false,
      hasOrderData: false,
      hasSpendData: false,
      metrics: {
        totalFarmers: 0,
        totalOrders: 0,
        totalSpent: 0,
        activeListings: 0,
      },
      topProducts: [],
      topFarmers: [],
      recentActivities: [],
      ordersChartOptions: {
        chart: {
          type: 'line',
          height: 300,
          toolbar: {
            show: false,
          },
        },
        colors: ['#1976d2'],
        dataLabels: {
          enabled: false,
        },
        stroke: {
          curve: 'smooth',
          width: 3,
        },
        fill: {
          type: 'gradient',
          gradient: {
            shade: 'light',
            type: 'vertical',
            opacityFrom: 0.4,
            opacityTo: 0.1,
          },
        },
        xaxis: {
          categories: [],
        },
        yaxis: {
          min: 0,
          tickAmount: 5,
        },
        tooltip: {
          theme: 'light',
        },
      },
      ordersChartSeries: [{
        name: 'Orders',
        data: [],
      }],
      spendChartOptions: {
        chart: {
          type: 'donut',
          height: 300,
        },
        colors: ['#4caf50', '#2196f3', '#ff9800', '#f44336', '#9c27b0', '#607d8b'],
        labels: [],
        legend: {
          position: 'bottom',
        },
        tooltip: {
          theme: 'light',
        },
        responsive: [{
          breakpoint: 480,
          options: {
            chart: {
              width: 200,
            },
            legend: {
              position: 'bottom',
            },
          },
        }],
      },
      spendChartSeries: [],
    };
  },
  async mounted() {
    await this.loadDashboardData();
    this.initializeCharts();
  },
  beforeDestroy() {
    // No cleanup needed for ApexCharts
  },
  methods: {
    async loadDashboardData() {
      try {
        const response = await axios.get(`/api/buyer/${getCurrentUserId()}/analytics/dashboard`);
        const data = response.data;

        this.metrics = data.metrics;
        this.topProducts = data.topProducts || [];
        this.topFarmers = data.topFarmers || [];
        this.recentActivities = data.recentActivities || [];
      } catch (error) {
        this.$toast.error('Failed to load dashboard data');
      }
    },
    async initializeCharts() {
      this.loadingCharts = true;
      try {
        // Load chart data
        const response = await axios.get(`/api/buyer/${getCurrentUserId()}/analytics/charts`);
        const chartData = response.data;

        // Check if we have data
        this.hasOrderData = chartData.orderTrends && chartData.orderTrends.data && chartData.orderTrends.data.length > 0;
        this.hasSpendData = chartData.spendAnalysis && chartData.spendAnalysis.data && chartData.spendAnalysis.data.length > 0;

        // Update Orders Chart
        if (this.hasOrderData) {
          this.ordersChartOptions.xaxis.categories = chartData.orderTrends.labels;
          this.ordersChartSeries[0].data = chartData.orderTrends.data;
        }

        // Update Spend Chart
        if (this.hasSpendData) {
          this.spendChartOptions.labels = chartData.spendAnalysis.labels;
          this.spendChartSeries = chartData.spendAnalysis.data;
        }
      } catch (error) {
        // Set fallback data for demo purposes
        this.hasOrderData = false;
        this.hasSpendData = false;
      } finally {
        this.loadingCharts = false;
      }
    },
    getProductColor(index) {
      const colors = ['success', 'primary', 'warning', 'info', 'purple'];
      return colors[index % colors.length];
    },
    viewFarmerDetails(farmer) {
      this.$router.push({
        name: 'FarmerDetails',
        params: { farmerId: farmer.id },
      });
    },
    viewAllFarmers() {
      this.$router.push({ name: 'MyFarmers' });
    },
    formatCurrency(amount) {
      return new Intl.NumberFormat('en-US', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(amount);
    },
    formatDateTime(timestamp) {
      return new Date(timestamp).toLocaleString();
    },
  },
};
</script>

<style scoped>
.buyer-dashboard {
  background-color: #f8fafc;
  min-height: 100vh;
  padding: 1rem 0;
}

.dashboard-header {
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

.chart-container canvas {
  max-height: 300px;
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .buyer-dashboard {
    padding: 0.5rem 0;
  }

  .dashboard-header {
    margin-bottom: 1rem;
  }

  .dashboard-header h1 {
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

  .v-list-item {
    padding: 0.5rem 1rem;
  }

  .v-timeline {
    padding-left: 1rem;
  }
}

@media (max-width: 480px) {
  .dashboard-header h1 {
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
}

/* Touch-friendly interfaces */
@media (hover: none) and (pointer: coarse) {
  .v-btn {
    min-height: 44px;
    min-width: 44px;
  }

  .v-list-item {
    min-height: 56px;
  }

  .metric-card:hover {
    transform: none;
  }
}

/* Clean professional styling */
.buyer-dashboard {
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
