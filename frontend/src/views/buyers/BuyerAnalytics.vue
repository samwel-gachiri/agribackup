<template>
  <Default>
    <div class="buyer-analytics">
      <v-container fluid>
        <!-- Header -->
        <div class="analytics-header mb-6">
          <h1 class="text-h4 font-weight-bold text-primary">
            <v-icon large color="primary" class="mr-3">mdi-chart-areaspline</v-icon>
            Buyer Analytics & Performance
          </h1>
          <p class="text-subtitle-1 text--secondary mt-2">
            Detailed analysis of your farmer network performance and market insights
          </p>
        </div>

      <!-- Performance Overview Cards -->
      <v-row class="mb-6">
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card elevation-2" color="primary">
            <v-card-text class="white--text">
              <div class="d-flex align-center">
                <div class="flex-grow-1">
                  <div class="text-h4 font-weight-bold">{{ analytics.avgFarmerRating.toFixed(1) }}</div>
                  <div class="text-subtitle-2">Avg Farmer Rating</div>
                </div>
                <v-icon size="48" color="white" class="ml-3">mdi-star</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card elevation-2" color="success">
            <v-card-text class="white--text">
              <div class="d-flex align-center">
                <div class="flex-grow-1">
                  <div class="text-h4 font-weight-bold">{{ analytics.reliabilityScore }}%</div>
                  <div class="text-subtitle-2">Network Reliability</div>
                </div>
                <v-icon size="48" color="white" class="ml-3">mdi-shield-check</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="metric-card elevation-2" color="warning">
            <v-card-text class="white--text">
              <div class="d-flex align-center">
                <div class="flex-grow-1">
                  <div class="text-h4 font-weight-bold">${{ formatCurrency(analytics.avgOrderValue) }}</div>
                  <div class="text-subtitle-2">Avg Order Value</div>
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
                  <div class="text-h4 font-weight-bold">{{ analytics.seasonalAvailability }}%</div>
                  <div class="text-subtitle-2">Seasonal Availability</div>
                </div>
                <v-icon size="48" color="white" class="ml-3">mdi-calendar-check</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row>
        <!-- Farmer Performance Comparison -->
        <v-col cols="12" lg="8">
          <v-card class="elevation-2">
            <v-card-title class="primary white--text">
              <v-icon left color="white">mdi-compare</v-icon>
              Farmer Performance Comparison
            </v-card-title>
            <v-card-text class="pa-4">
              <div v-if="loadingAnalytics" class="text-center pa-6">
                <v-progress-circular indeterminate color="primary"></v-progress-circular>
                <p class="mt-3 text--secondary">Loading performance data...</p>
              </div>
              <div v-else-if="!hasPerformanceData" class="text-center pa-6">
                <v-icon size="64" color="grey lighten-2">mdi-compare</v-icon>
                <p class="text-h6 text--secondary mt-3">No performance data available</p>
                <p class="text--secondary">Connect with farmers and place orders to see comparisons</p>
              </div>
              <div v-else>
                <!-- Performance Chart using ApexCharts -->
                <apexchart
                  type="radar"
                  height="400"
                  :options="performanceChartOptions"
                  :series="performanceChartSeries"
                ></apexchart>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Top Performing Farmers -->
        <v-col cols="12" lg="4">
          <v-card class="elevation-2">
            <v-card-title class="success white--text">
              <v-icon left color="white">mdi-trophy</v-icon>
              Top Performers
            </v-card-title>
            <v-card-text class="pa-0">
              <v-list v-if="topPerformers.length > 0">
                <v-list-item
                  v-for="(farmer, index) in topPerformers"
                  :key="farmer.id"
                  @click="viewFarmerDetails(farmer)"
                >
                  <v-list-item-avatar>
                    <v-chip :color="getPerformanceColor(index)" text-color="white" small>
                      {{ index + 1 }}
                    </v-chip>
                  </v-list-item-avatar>
                  <v-list-item-content>
                    <v-list-item-title>{{ farmer.name }}</v-list-item-title>
                    <v-list-item-subtitle>
                      <div class="d-flex align-center">
                        <v-rating
                          :value="farmer.overallScore"
                          color="amber"
                          dense
                          half-increments
                          readonly
                          size="16"
                          class="mr-2"
                        ></v-rating>
                        <span class="text-caption">{{ farmer.overallScore.toFixed(1) }}/5</span>
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
                <v-icon size="64" color="grey lighten-2">mdi-trophy</v-icon>
                <p class="text-h6 text--secondary mt-3">No performance data</p>
                <p class="text--secondary">Start ordering to see top performers</p>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';
import { getCurrentUserId } from '@/utils/roles.js';

export default {
  name: 'BuyerAnalytics',
  components: {
    Default,
  },
  data() {
    return {
      loadingAnalytics: false,
      analytics: {
        avgFarmerRating: 4.2,
        reliabilityScore: 85,
        avgOrderValue: 250,
        seasonalAvailability: 78,
      },
      topPerformers: [],
      hasPerformanceData: false,
      performanceChartOptions: {
        chart: {
          type: 'radar',
          toolbar: {
            show: false,
          },
        },
        xaxis: {
          categories: ['Quality', 'Reliability', 'Price', 'Delivery', 'Communication'],
        },
        yaxis: {
          show: false,
        },
        plotOptions: {
          radar: {
            polygons: {
              strokeColors: '#e8e8e8',
              fill: {
                colors: ['#f8f8f8', '#fff'],
              },
            },
          },
        },
        colors: ['#1976d2', '#4caf50', '#ff9800'],
        legend: {
          position: 'bottom',
        },
      },
      performanceChartSeries: [],
    };
  },
  async mounted() {
    await this.loadAnalytics();
  },
  methods: {
    async loadAnalytics() {
      this.loadingAnalytics = true;
      try {
        const buyerId = getCurrentUserId();

        // Load dashboard analytics
        const dashboardResponse = await axios.get(`/api/buyer/analytics/${buyerId}/dashboard`);
        const dashboardData = dashboardResponse.data;

        // Load farmer performance metrics
        const performanceResponse = await axios.get(`/api/buyer/analytics/${buyerId}/farmer-performance`);
        const performanceData = performanceResponse.data;

        // Load seasonal trends
        const trendsResponse = await axios.get(`/api/buyer/analytics/${buyerId}/seasonal-trends`);
        const trendsData = trendsResponse.data;

        // Process and combine the data
        this.analytics = {
          avgFarmerRating: this.calculateAverageRating(performanceData),
          reliabilityScore: this.calculateAverageReliability(performanceData),
          avgOrderValue: dashboardData.averageDeliveryTime || 250, // Using delivery time as proxy
          seasonalAvailability: this.calculateSeasonalAvailability(trendsData),
        };

        this.topPerformers = this.processTopPerformers(performanceData);
        this.hasPerformanceData = this.topPerformers.length > 0;

        if (this.hasPerformanceData) {
          this.performanceChartSeries = this.buildPerformanceChartSeries(performanceData);
        }
      } catch (error) {
        this.$toast.error('Failed to load analytics data. Using demo data.');
        // Keep the existing mock data as fallback
        this.topPerformers = [
          {
            id: 1,
            name: 'John Smith',
            overallScore: 4.8,
            totalOrders: 25,
            reliabilityScore: 95,
          },
          {
            id: 2,
            name: 'Mary Johnson',
            overallScore: 4.5,
            totalOrders: 18,
            reliabilityScore: 88,
          },
        ];
        this.hasPerformanceData = true;
      } finally {
        this.loadingAnalytics = false;
      }
    },

    calculateAverageRating(performanceData) {
      if (!performanceData || performanceData.length === 0) return 4.2;
      const totalRating = performanceData.reduce((sum, farmer) => sum + (farmer.qualityRating || 0), 0);
      return totalRating / performanceData.length;
    },

    calculateAverageReliability(performanceData) {
      if (!performanceData || performanceData.length === 0) return 85;
      const totalReliability = performanceData.reduce((sum, farmer) => sum + (farmer.reliabilityScore || 0), 0);
      return Math.round(totalReliability / performanceData.length);
    },

    calculateSeasonalAvailability(trendsData) {
      // Calculate based on seasonal trends data
      if (!trendsData) return 78;

      const seasons = [trendsData.spring, trendsData.summer, trendsData.fall, trendsData.winter];
      const totalAvailability = seasons.reduce((sum, season) => sum + (season?.totalProduces || 0), 0);
      const avgAvailability = totalAvailability / seasons.length;

      // Convert to percentage (assuming max 100 produces per season)
      return Math.min(100, Math.round((avgAvailability / 100) * 100));
    },

    processTopPerformers(performanceData) {
      if (!performanceData || performanceData.length === 0) return [];

      return performanceData
        .map((farmer) => ({
          id: farmer.farmerId,
          name: farmer.farmerName,
          overallScore: (farmer.qualityRating + farmer.reliabilityScore / 20) / 2, // Normalize reliability to 0-5 scale
          totalOrders: farmer.totalOrders,
          reliabilityScore: farmer.reliabilityScore,
        }))
        .sort((a, b) => b.overallScore - a.overallScore)
        .slice(0, 5); // Top 5 performers
    },

    buildPerformanceChartSeries(performanceData) {
      if (!performanceData || performanceData.length === 0) return [];

      // Take top 3 farmers for comparison
      const topFarmers = performanceData.slice(0, 3);

      return topFarmers.map((farmer) => ({
        name: farmer.farmerName,
        data: [
          farmer.qualityRating || 0,
          farmer.reliabilityScore || 0,
          farmer.onTimeDeliveryRate || 0,
          farmer.averageYield || 0,
          4.0, // Placeholder for communication score
        ],
      }));
    },

    viewFarmerDetails(farmer) {
      this.$router.push({
        name: 'FarmerDetails',
        params: { farmerId: farmer.id },
      });
    },

    getPerformanceColor(index) {
      const colors = ['success', 'primary', 'warning', 'info', 'purple'];
      return colors[index % colors.length];
    },

    formatCurrency(amount) {
      if (!amount) return '0';
      return new Intl.NumberFormat('en-US', {
        minimumFractionDigits: 0,
        maximumFractionDigits: 0,
      }).format(amount);
    },
  },
};
</script>

<style scoped>
.buyer-analytics {
  min-height: 100vh;
  padding: 1rem 0;
}

.analytics-header {
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

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .buyer-analytics {
    padding: 0.5rem 0;
  }

  .analytics-header {
    margin-bottom: 1rem;
  }

  .analytics-header h1 {
    font-size: 1.5rem !important;
  }

  .metric-card .text-h4 {
    font-size: 1.8rem !important;
  }

  .v-card-text {
    padding: 1rem !important;
  }
}

/* Clean professional styling */
</style>
