<template>
  <Default>
    <v-container fluid class="pa-6 system-analytics-root">
      <v-row>
        <v-col cols="12" class="pb-0">
          <div class="tw-space-y-1">
            <h1 class="tw-text-2xl tw-font-bold tw-text-gray-900">System Analytics</h1>
            <p class="tw-text-sm tw-text-gray-600">Comprehensive exporter system performance and analytics dashboard.</p>
          </div>
        </v-col>
      </v-row>

      <!-- Action Buttons -->
      <v-row class="tw-mt-4">
        <v-col cols="12">
          <div class="tw-flex tw-justify-end tw-space-x-2">
            <v-btn
              color="primary"
              :loading="refreshing"
              @click="refreshDashboard"
              small
            >
              <v-icon left>mdi-refresh</v-icon>
              Refresh Data
            </v-btn>
            <v-btn
              color="info"
              :loading="checkingIntegrity"
              @click="checkDataIntegrity"
              small
            >
              <v-icon left>mdi-shield-check</v-icon>
              Check Integrity
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- Loading State -->
      <v-row v-if="loading">
        <v-col cols="12" class="tw-text-center tw-py-12">
          <v-progress-circular
            indeterminate
            color="primary"
            size="64"
          ></v-progress-circular>
          <p class="tw-mt-4 tw-text-gray-600">Loading analytics data...</p>
        </v-col>
      </v-row>

      <!-- Error State -->
      <v-row v-else-if="error">
        <v-col cols="12">
          <v-alert type="error" class="tw-mb-4">
            {{ error }}
          </v-alert>
        </v-col>
      </v-row>

      <!-- Analytics Content -->
      <template v-else>
        <!-- Overview Cards -->
        <v-row class="tw-mt-6">
          <v-col cols="12" md="3">
            <v-card class="tw-shadow-sm tw-border tw-border-gray-100">
              <v-card-text class="pa-4">
                <div class="tw-flex tw-items-center tw-justify-between">
                  <div>
                    <p class="tw-text-sm tw-font-medium tw-text-gray-600">Total Zones</p>
                    <p class="tw-text-2xl tw-font-bold tw-text-gray-900">{{ analytics.totalZones || 0 }}</p>
                  </div>
                  <v-icon color="primary" size="32">mdi-map-marker-multiple</v-icon>
                </div>
              </v-card-text>
            </v-card>
          </v-col>

          <v-col cols="12" md="3">
            <v-card class="tw-shadow-sm tw-border tw-border-gray-100">
              <v-card-text class="pa-4">
                <div class="tw-flex tw-items-center tw-justify-between">
                  <div>
                    <p class="tw-text-sm tw-font-medium tw-text-gray-600">Total Farmers</p>
                    <p class="tw-text-2xl tw-font-bold tw-text-gray-900">{{ analytics.totalFarmers || 0 }}</p>
                  </div>
                  <v-icon color="success" size="32">mdi-account-group</v-icon>
                </div>
              </v-card-text>
            </v-card>
          </v-col>

          <v-col cols="12" md="3">
            <v-card class="tw-shadow-sm tw-border tw-border-gray-100">
              <v-card-text class="pa-4">
                <div class="tw-flex tw-items-center tw-justify-between">
                  <div>
                    <p class="tw-text-sm tw-font-medium tw-text-gray-600">Active Admins</p>
                    <p class="tw-text-2xl tw-font-bold tw-text-gray-900">{{ analytics.activeSystemAdmins || 0 }}</p>
                  </div>
                  <v-icon color="warning" size="32">mdi-account-cog</v-icon>
                </div>
              </v-card-text>
            </v-card>
          </v-col>

          <v-col cols="12" md="3">
            <v-card class="tw-shadow-sm tw-border tw-border-gray-100">
              <v-card-text class="pa-4">
                <div class="tw-flex tw-items-center tw-justify-between">
                  <div>
                    <p class="tw-text-sm tw-font-medium tw-text-gray-600">Active Supervisors</p>
                    <p class="tw-text-2xl tw-font-bold tw-text-gray-900">{{ analytics.activeZoneSupervisors || 0 }}</p>
                  </div>
                  <v-icon color="info" size="32">mdi-account-supervisor</v-icon>
                </div>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>

        <!-- Real-time Analytics -->
        <v-row class="tw-mt-6" v-if="realTimeData">
          <v-col cols="12">
            <v-card class="tw-shadow-sm tw-border tw-border-gray-100">
              <v-card-title class="tw-bg-blue-50 tw-text-blue-900">
                <v-icon left color="blue">mdi-gauge</v-icon>
                Real-time Analytics
                <v-spacer></v-spacer>
                <v-chip small color="blue" text-color="white">
                  Live Data
                </v-chip>
              </v-card-title>
              <v-card-text class="pa-6">
                <v-row>
                  <v-col cols="12" md="3">
                    <div class="tw-text-center">
                      <p class="tw-text-3xl tw-font-bold tw-text-blue-600">{{ realTimeData.totalOrders || 0 }}</p>
                      <p class="tw-text-sm tw-text-gray-600">Total Orders</p>
                    </div>
                  </v-col>
                  <v-col cols="12" md="3">
                    <div class="tw-text-center">
                      <p class="tw-text-3xl tw-font-bold tw-text-orange-600">{{ realTimeData.pendingOrders || 0 }}</p>
                      <p class="tw-text-sm tw-text-gray-600">Pending Orders</p>
                    </div>
                  </v-col>
                  <v-col cols="12" md="3">
                    <div class="tw-text-center">
                      <p class="tw-text-3xl tw-font-bold tw-text-green-600">{{ realTimeData.totalYields || 0 }}</p>
                      <p class="tw-text-sm tw-text-gray-600">Total Yields</p>
                    </div>
                  </v-col>
                  <v-col cols="12" md="3">
                    <div class="tw-text-center">
                      <p class="tw-text-3xl tw-font-bold tw-text-purple-600">{{ realTimeData.activeListings || 0 }}</p>
                      <p class="tw-text-sm tw-text-gray-600">Active Listings</p>
                    </div>
                  </v-col>
                </v-row>
                <v-row class="tw-mt-4">
                  <v-col cols="12" md="6">
                    <div class="tw-flex tw-items-center tw-justify-between tw-p-3 tw-bg-gray-50 tw-rounded">
                      <span class="tw-text-sm tw-font-medium">System Load</span>
                      <v-progress-linear
                        :value="realTimeData.systemLoad || 0"
                        color="primary"
                        height="8"
                        class="tw-w-24"
                      ></v-progress-linear>
                      <span class="tw-text-sm tw-font-bold">{{ Math.round(realTimeData.systemLoad || 0) }}%</span>
                    </div>
                  </v-col>
                  <v-col cols="12" md="6">
                    <div class="tw-flex tw-items-center tw-justify-between">
                      <span class="tw-text-sm tw-font-medium">Data Freshness</span>
                      <v-chip
                        small
                        :color="realTimeData.dataFreshness === 'REAL_TIME' ? 'success' : 'warning'"
                        text-color="white"
                      >
                        {{ realTimeData.dataFreshness || 'Unknown' }}
                      </v-chip>
                    </div>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>

        <!-- Zone Breakdown -->
        <v-row class="tw-mt-6" v-if="analytics.zoneBreakdown && analytics.zoneBreakdown.length > 0">
          <v-col cols="12">
            <v-card class="tw-shadow-sm tw-border tw-border-gray-100">
              <v-card-title class="tw-bg-green-50 tw-text-green-900">
                <v-icon left color="green">mdi-chart-pie</v-icon>
                Zone Breakdown
              </v-card-title>
              <v-card-text class="pa-6">
                <v-row>
                  <v-col
                    v-for="zone in analytics.zoneBreakdown"
                    :key="zone.zoneId"
                    cols="12"
                    md="4"
                  >
                    <v-card outlined class="tw-h-full">
                      <v-card-text class="pa-4">
                        <h4 class="tw-font-semibold tw-text-gray-900 tw-mb-2">{{ zone.zoneName }}</h4>
                        <div class="tw-space-y-2">
                          <div class="tw-flex tw-justify-between">
                            <span class="tw-text-sm tw-text-gray-600">Farmers:</span>
                            <span class="tw-font-medium">{{ zone.farmerCount || 0 }}</span>
                          </div>
                          <div class="tw-flex tw-justify-between">
                            <span class="tw-text-sm tw-text-gray-600">Supervisors:</span>
                            <span class="tw-font-medium">{{ zone.supervisorCount || 0 }}</span>
                          </div>
                        </div>
                      </v-card-text>
                    </v-card>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>

        <!-- Data Integrity Status -->
        <v-row class="tw-mt-6" v-if="integrityReport">
          <v-col cols="12">
            <v-card class="tw-shadow-sm tw-border tw-border-gray-100">
              <v-card-title class="tw-bg-orange-50 tw-text-orange-900">
                <v-icon left color="orange">mdi-shield-alert</v-icon>
                Data Integrity Report
                <v-spacer></v-spacer>
                <v-chip
                  small
                  :color="getIntegrityColor(integrityReport.overallStatus)"
                  text-color="white"
                >
                  {{ integrityReport.overallStatus || 'Unknown' }}
                </v-chip>
              </v-card-title>
              <v-card-text class="pa-6">
                <v-row>
                  <v-col cols="12" md="3">
                    <div class="tw-text-center">
                      <p class="tw-text-2xl tw-font-bold tw-text-red-600">{{ integrityReport.highSeverityIssues || 0 }}</p>
                      <p class="tw-text-sm tw-text-gray-600">High Severity</p>
                    </div>
                  </v-col>
                  <v-col cols="12" md="3">
                    <div class="tw-text-center">
                      <p class="tw-text-2xl tw-font-bold tw-text-orange-600">{{ integrityReport.mediumSeverityIssues || 0 }}</p>
                      <p class="tw-text-sm tw-text-gray-600">Medium Severity</p>
                    </div>
                  </v-col>
                  <v-col cols="12" md="3">
                    <div class="tw-text-center">
                      <p class="tw-text-2xl tw-font-bold tw-text-yellow-600">{{ integrityReport.lowSeverityIssues || 0 }}</p>
                      <p class="tw-text-sm tw-text-gray-600">Low Severity</p>
                    </div>
                  </v-col>
                  <v-col cols="12" md="3">
                    <div class="tw-text-center">
                      <p class="tw-text-2xl tw-font-bold tw-text-gray-600">{{ integrityReport.totalIssues || 0 }}</p>
                      <p class="tw-text-sm tw-text-gray-600">Total Issues</p>
                    </div>
                  </v-col>
                </v-row>

                <v-row class="tw-mt-4" v-if="integrityReport.issues && integrityReport.issues.length > 0">
                  <v-col cols="12">
                    <v-expansion-panels>
                      <v-expansion-panel>
                        <v-expansion-panel-header>
                          <span class="tw-font-medium">View Issues Details</span>
                        </v-expansion-panel-header>
                        <v-expansion-panel-content>
                          <v-data-table
                            :headers="issueHeaders"
                            :items="integrityReport.issues"
                            :items-per-page="5"
                            class="tw-border tw-border-gray-200"
                          >
                            <template v-slot:item.severity="{ item }">
                              <v-chip
                                small
                                :color="getSeverityColor(item.severity)"
                                text-color="white"
                              >
                                {{ item.severity }}
                              </v-chip>
                            </template>
                          </v-data-table>
                        </v-expansion-panel-content>
                      </v-expansion-panel>
                    </v-expansion-panels>
                  </v-col>
                </v-row>

                <v-row class="tw-mt-4" v-if="integrityReport.totalIssues > 0">
                  <v-col cols="12" class="tw-text-center">
                    <v-btn
                      color="error"
                      :loading="fixingIntegrity"
                      @click="fixDataIntegrity"
                    >
                      <v-icon left>mdi-wrench</v-icon>
                      Fix Data Integrity Issues
                    </v-btn>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>

        <!-- Recent Activities -->
        <v-row class="tw-mt-6" v-if="dashboardData && dashboardData.recentActivities">
          <v-col cols="12">
            <v-card class="tw-shadow-sm tw-border tw-border-gray-100">
              <v-card-title class="tw-bg-purple-50 tw-text-purple-900">
                <v-icon left color="purple">mdi-history</v-icon>
                Recent Activities
              </v-card-title>
              <v-card-text class="pa-6">
                <v-timeline dense>
                  <v-timeline-item
                    v-for="activity in dashboardData.recentActivities"
                    :key="activity.id"
                    small
                    :color="getActivityColor(activity.severity)"
                  >
                    <template v-slot:icon>
                      <v-icon small :color="getActivityColor(activity.severity)">
                        {{ getActivityIcon(activity.type) }}
                      </v-icon>
                    </template>
                    <div class="tw-ml-4">
                      <p class="tw-font-medium tw-text-gray-900">{{ activity.title }}</p>
                      <p class="tw-text-sm tw-text-gray-600">{{ activity.description }}</p>
                      <p class="tw-text-xs tw-text-gray-500">{{ formatTimestamp(activity.timestamp) }}</p>
                    </div>
                  </v-timeline-item>
                </v-timeline>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </template>
    </v-container>
  </Default>
</template>

<script>
// exporter analytic
// import exporterAnalyticsService from '@/services/exporterAnalytics.service.js';
import Default from '@/components/layout/Default.vue';
import { getCurrentUserId } from '@/utils/roles.js';

export default {
  name: 'SystemAnalytics',
  components: {
    Default,
  },
  data() {
    return {
      loading: false,
      refreshing: false,
      checkingIntegrity: false,
      fixingIntegrity: false,
      error: null,
      analytics: {},
      realTimeData: null,
      dashboardData: null,
      integrityReport: null,
      issueHeaders: [
        { text: 'Type', value: 'type' },
        { text: 'Description', value: 'description' },
        { text: 'Severity', value: 'severity' },
        { text: 'Entity', value: 'affectedEntity' },
      ],
    };
  },
  async mounted() {
    await this.loadAnalytics();
  },
  methods: {
    async loadAnalytics() {
      this.loading = true;
      this.error = null;

      try {
        const exporterId = getCurrentUserId();
        if (!exporterId) {
          throw new Error('Unable to get exporter ID. Please ensure you are logged in.');
        }

        // Mock data for analytics
        this.analytics = {
          totalZones: 12,
          totalFarmers: 156,
          activeSystemAdmins: 3,
          activeZoneSupervisors: 8,
          zoneBreakdown: [
            {
              zoneId: 1,
              zoneName: 'Nairobi Central',
              farmerCount: 45,
              supervisorCount: 2,
            },
            {
              zoneId: 2,
              zoneName: 'Kiambu Region',
              farmerCount: 38,
              supervisorCount: 2,
            },
            {
              zoneId: 3,
              zoneName: 'Nakuru Area',
              farmerCount: 73,
              supervisorCount: 4,
            },
          ],
        };

        // Mock real-time data
        this.realTimeData = {
          totalOrders: 1247,
          pendingOrders: 89,
          totalYields: 45230,
          activeListings: 156,
          systemLoad: 67,
          dataFreshness: 'REAL_TIME',
        };

        // Mock dashboard data
        this.dashboardData = {
          recentActivities: [
            {
              id: 1,
              title: 'Zone Optimization Completed',
              description: 'Route optimization for Nairobi Central zone finished successfully',
              type: 'CALCULATION',
              severity: 'INFO',
              timestamp: new Date().toISOString(),
            },
            {
              id: 2,
              title: 'New Farmer Registration',
              description: 'Farmer John Doe registered in Kiambu Region',
              type: 'UPDATE',
              severity: 'INFO',
              timestamp: new Date(Date.now() - 3600000).toISOString(),
            },
          ],
        };

        // Mock integrity report
        this.integrityReport = {
          overallStatus: 'HEALTHY',
          highSeverityIssues: 0,
          mediumSeverityIssues: 2,
          lowSeverityIssues: 5,
          totalIssues: 7,
          issues: [
            {
              type: 'DATA_CONSISTENCY',
              description: 'Minor data synchronization delay',
              severity: 'LOW',
              affectedEntity: 'Zone_5',
            },
            {
              type: 'PERFORMANCE',
              description: 'Query optimization needed',
              severity: 'MEDIUM',
              affectedEntity: 'Dashboard_API',
            },
          ],
        };
      } catch (error) {
        this.error = error.message || 'Failed to load analytics data';
      } finally {
        this.loading = false;
      }
    },

    async refreshDashboard() {
      this.refreshing = true;
      try {
        // Mock refresh functionality
        await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate API call
        this.$toast.success('Dashboard data refreshed successfully');
        await this.loadAnalytics(); // Reload all data
      } catch (error) {
        this.$toast.error('Failed to refresh dashboard');
      } finally {
        this.refreshing = false;
      }
    },

    async checkDataIntegrity() {
      this.checkingIntegrity = true;
      try {
        // Mock integrity check
        await new Promise((resolve) => setTimeout(resolve, 1500)); // Simulate API call
        this.$toast.success('Data integrity check completed');
        // Integrity report is already set in loadAnalytics
      } catch (error) {
        this.$toast.error('Failed to check data integrity');
      } finally {
        this.checkingIntegrity = false;
      }
    },

    async fixDataIntegrity() {
      this.fixingIntegrity = true;
      try {
        // Mock integrity fix
        await new Promise((resolve) => setTimeout(resolve, 2000)); // Simulate API call
        this.$toast.success('Data integrity issues fixed successfully');
        await this.checkDataIntegrity(); // Re-check integrity
        await this.loadAnalytics(); // Reload analytics
      } catch (error) {
        this.$toast.error('Failed to fix data integrity issues');
      } finally {
        this.fixingIntegrity = false;
      }
    },

    getIntegrityColor(status) {
      switch (status) {
        case 'HEALTHY': return 'success';
        case 'MINOR_ISSUES': return 'warning';
        case 'WARNING': return 'warning';
        case 'CRITICAL': return 'error';
        default: return 'grey';
      }
    },

    getSeverityColor(severity) {
      switch (severity) {
        case 'HIGH': return 'error';
        case 'MEDIUM': return 'warning';
        case 'LOW': return 'info';
        default: return 'grey';
      }
    },

    getActivityColor(severity) {
      switch (severity) {
        case 'ERROR': return 'red';
        case 'WARNING': return 'orange';
        case 'INFO': return 'blue';
        default: return 'grey';
      }
    },

    getActivityIcon(type) {
      switch (type) {
        case 'SYNC': return 'mdi-sync';
        case 'UPDATE': return 'mdi-update';
        case 'CALCULATION': return 'mdi-calculator';
        default: return 'mdi-information';
      }
    },

    formatTimestamp(timestamp) {
      if (!timestamp) return 'Unknown';
      return new Date(timestamp).toLocaleString();
    },
  },
};
</script>

<style scoped>
.system-analytics-root {
  background: linear-gradient(135deg, #f8fafc 0%, #eef2f7 100%);
  min-height: 100vh;
}

.v-card-title {
  font-weight: 600;
}

.v-chip {
  font-weight: 500;
}
</style>
