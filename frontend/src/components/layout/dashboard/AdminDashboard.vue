<template>
  <Default>
    <div class="admin-system-dashboard">
      <v-container fluid class="pa-6">
        <!-- Header -->
        <div class="d-flex justify-space-between align-center mb-6">
          <div>
            <h1 class="text-h4 font-weight-bold">System Administration Dashboard</h1>
            <p class="text-subtitle-1 text--secondary">Platform-wide analytics and management</p>
          </div>
          <v-btn
            @click="refreshData"
            color="primary"
            outlined
            :loading="loading"
          >
            <v-icon left>mdi-refresh</v-icon>
            Refresh
          </v-btn>
        </div>

        <!-- System Statistics Cards -->
        <v-row class="mb-6">
          <v-col cols="12" sm="6" md="3">
            <v-card class="stat-card" elevation="2">
              <v-card-text class="d-flex align-center">
                <div class="stat-icon mr-4">
                  <v-icon color="primary" size="32">mdi-account-group</v-icon>
                </div>
                <div>
                  <div class="stat-value">{{ stats.totalUsers }}</div>
                  <div class="stat-label">Total Users</div>
                </div>
              </v-card-text>
            </v-card>
          </v-col>

          <v-col cols="12" sm="6" md="3">
            <v-card class="stat-card" elevation="2">
              <v-card-text class="d-flex align-center">
                <div class="stat-icon mr-4">
                  <v-icon color="success" size="32">mdi-storefront</v-icon>
                </div>
                <div>
                  <div class="stat-value">{{ stats.totalExporters }}</div>
                  <div class="stat-label">Active Exporters</div>
                </div>
              </v-card-text>
            </v-card>
          </v-col>

          <v-col cols="12" sm="6" md="3">
            <v-card class="stat-card" elevation="2">
              <v-card-text class="d-flex align-center">
                <div class="stat-icon mr-4">
                  <v-icon color="info" size="32">mdi-shopping</v-icon>
                </div>
                <div>
                  <div class="stat-value">{{ stats.totalOrders }}</div>
                  <div class="stat-label">Total Orders</div>
                </div>
              </v-card-text>
            </v-card>
          </v-col>

          <v-col cols="12" sm="6" md="3">
            <v-card class="stat-card" elevation="2">
              <v-card-text class="d-flex align-center">
                <div class="stat-icon mr-4">
                  <v-icon color="warning" size="32">mdi-file-document-check</v-icon>
                </div>
                <div>
                  <div class="stat-value">{{ stats.pendingLicenses }}</div>
                  <div class="stat-label">Pending Licenses</div>
                </div>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>

        <!-- Charts Row -->
        <v-row>
          <!-- User Registration Trend -->
          <v-col cols="12" md="6">
            <v-card class="chart-card" elevation="2">
              <v-card-title>
                <v-icon left>mdi-account-plus</v-icon>
                User Registration Trend
              </v-card-title>
              <v-card-text>
                <apexchart
                  type="area"
                  :options="userRegistrationOptions"
                  :series="userRegistrationSeries"
                  height="300"
                />
              </v-card-text>
            </v-card>
          </v-col>

          <!-- Platform Activity -->
          <v-col cols="12" md="6">
            <v-card class="chart-card" elevation="2">
              <v-card-title>
                <v-icon left>mdi-chart-line</v-icon>
                Platform Activity
              </v-card-title>
              <v-card-text>
                <apexchart
                  type="bar"
                  :options="activityOptions"
                  :series="activitySeries"
                  height="300"
                />
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>

        <!-- Recent Activity -->
        <v-row class="mt-6">
          <v-col cols="12">
            <v-card elevation="2">
              <v-card-title>
                <v-icon left>mdi-history</v-icon>
                Recent System Activity
              </v-card-title>
              <v-card-text>
                <v-timeline dense>
                  <v-timeline-item
                    v-for="activity in recentActivities"
                    :key="activity.id"
                    :color="activity.color"
                    small
                  >
                    <template v-slot:icon>
                      <v-icon>{{ activity.icon }}</v-icon>
                    </template>
                    <div class="font-weight-medium">{{ activity.title }}</div>
                    <div class="text-caption text--secondary">{{ activity.description }}</div>
                    <div class="text-caption text--secondary">{{ formatDate(activity.timestamp) }}</div>
                  </v-timeline-item>
                </v-timeline>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </div>
  </Default>
</template>

<script>
import VueApexCharts from 'vue-apexcharts';
import Default from '@/components/layout/Default.vue';
import { mapGetters } from 'vuex';
import axios from 'axios';

export default {
  name: 'AdminDashboard',
  components: {
    apexchart: VueApexCharts,
    Default,
  },
  data() {
    return {
      loading: false,
      stats: {
        totalUsers: 0,
        totalExporters: 0,
        totalOrders: 0,
        pendingLicenses: 0,
      },
      recentActivities: [],
      // User Registration Chart
      userRegistrationSeries: [],
      userRegistrationOptions: {
        chart: {
          type: 'area',
          toolbar: { show: false },
        },
        fill: {
          type: 'gradient',
          gradient: {
            shadeIntensity: 1,
            inverseColors: false,
            opacityFrom: 0.7,
            opacityTo: 0.3,
            stops: [0, 90, 100],
          },
        },
        xaxis: {
          categories: [],
          title: { text: 'Date' },
        },
        yaxis: {
          title: { text: 'New Users' },
        },
        colors: ['#2196F3'],
      },
      // Activity Chart
      activitySeries: [],
      activityOptions: {
        chart: {
          type: 'bar',
          toolbar: { show: false },
        },
        plotOptions: {
          bar: {
            horizontal: false,
            columnWidth: '55%',
          },
        },
        xaxis: {
          categories: ['Orders', 'Listings', 'Users', 'Licenses'],
        },
        colors: ['#4CAF50', '#FF9800', '#2196F3', '#9C27B0'],
      },
    };
  },
  computed: {
    ...mapGetters(['currentUser']),
  },
  mounted() {
    this.loadDashboardData();
  },
  methods: {
    async loadDashboardData() {
      this.loading = true;
      try {
        await Promise.all([
          this.loadSystemStats(),
          this.loadUserRegistrationData(),
          this.loadActivityData(),
          this.loadRecentActivities(),
        ]);
      } catch (error) {
        this.$toast.error('Failed to load dashboard data');
      } finally {
        this.loading = false;
      }
    },

    async loadSystemStats() {
      try {
        const response = await axios.get('/api/admin/system/stats');
        this.stats = response.data;
      } catch (error) {
        // Set default values
        this.stats = {
          totalUsers: 0,
          totalExporters: 0,
          totalOrders: 0,
          pendingLicenses: 0,
        };
      }
    },

    async loadUserRegistrationData() {
      const response = await axios.get('/api/admin/user-registrations');
      const data = response.data;

      this.userRegistrationOptions = {
        ...this.userRegistrationOptions,
        xaxis: {
          ...this.userRegistrationOptions.xaxis,
          categories: data.map((item) => this.formatDate(item.date)),
        },
      };

      this.userRegistrationSeries = [{
        name: 'New Registrations',
        data: data.map((item) => item.count),
      }];
    },

    async loadActivityData() {
      try {
        const response = await axios.get('/api/admin/activity-summary');
        const data = response.data;

        this.activitySeries = [{
          name: 'Count',
          data: [
            data.orders || 0,
            data.listings || 0,
            data.users || 0,
            data.licenses || 0,
          ],
        }];
      } catch (error) {
        this.activitySeries = [{
          name: 'Count',
          data: [0, 0, 0, 0],
        }];
      }
    },

    async loadRecentActivities() {
      try {
        const response = await axios.get('/api/admin/recent-activities');
        this.recentActivities = response.data.map((activity) => ({
          ...activity,
          color: this.getActivityColor(activity.type),
          icon: this.getActivityIcon(activity.type),
        }));
      } catch (error) {
        this.recentActivities = [];
      }
    },

    async refreshData() {
      await this.loadDashboardData();
    },

    getActivityColor(type) {
      const colors = {
        USER_REGISTERED: 'success',
        ORDER_PLACED: 'primary',
        LICENSE_APPROVED: 'info',
        LISTING_CREATED: 'warning',
      };
      return colors[type] || 'grey';
    },

    getActivityIcon(type) {
      const icons = {
        USER_REGISTERED: 'mdi-account-plus',
        ORDER_PLACED: 'mdi-shopping',
        LICENSE_APPROVED: 'mdi-check-circle',
        LISTING_CREATED: 'mdi-plus-circle',
      };
      return icons[type] || 'mdi-information';
    },

    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('en-GB', {
        month: 'short',
        day: '2-digit',
      }).format(date);
    },
  },
};
</script>

<style scoped>
.admin-system-dashboard {
  min-height: 100vh;
}

.admin-drawer {
  background: linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%);
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  background: rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  padding: 12px;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
}

.stat-label {
  font-size: 0.875rem;
  color: #718096;
  font-weight: 500;
  margin-top: 4px;
}

.chart-card {
  border-radius: 12px;
  height: 100%;
}
</style>
