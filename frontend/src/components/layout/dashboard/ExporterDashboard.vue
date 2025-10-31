<template>
  <v-container fluid class="exporter-dashboard">
    <CompleteSignUp v-if="verificationStatus === 'PENDING'" @completed="onVerificationCompleted" />
    <UnderReviewMessage v-else-if="verificationStatus === 'UNDER_REVIEW'" />
    <!-- Header Section -->
    <div class="dashboard-header mb-8">
      <v-row align="center" justify="space-between">
        <v-col cols="12" md="8">
          <h1 class="text-h4 font-weight-bold text-primary mb-2">Exporter Dashboard</h1>
          <p class="text-subtitle-1 text--secondary">Comprehensive system oversight and management</p>
        </v-col>
        <v-col cols="12" md="4" class="text-right">
          <div class="d-flex justify-end">
            <v-btn color="primary" @click="refreshData" :loading="loading" class="mr-2">
              <v-icon left>mdi-refresh</v-icon>
              Refresh
            </v-btn>
            <v-btn color="success" @click="exportReport">
              <v-icon left>mdi-download</v-icon>
              Export
            </v-btn>
          </div>
        </v-col>
      </v-row>
    </div>

    <!-- Key Metrics Cards -->
    <v-row class="mb-8">
      <v-col cols="12" sm="6" md="3">
        <v-card class="metric-card elevation-2" color="primary">
          <v-card-text class="white--text">
            <div class="d-flex align-center">
              <div class="flex-grow-1">
                <div class="text-h4 font-weight-bold">{{ analytics.totalZones }}</div>
                <div class="text-subtitle-2">Total Zones</div>
              </div>
              <v-icon size="48" color="white" class="ml-3">mdi-map-marker-multiple</v-icon>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card class="metric-card elevation-2" color="success">
          <v-card-text class="white--text">
            <div class="d-flex align-center">
              <div class="flex-grow-1">
                <div class="text-h4 font-weight-bold">{{ analytics.totalFarmers }}</div>
                <div class="text-subtitle-2">Active Farmers</div>
              </div>
              <v-icon size="48" color="white" class="ml-3">mdi-account-group</v-icon>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card class="metric-card elevation-2" color="purple">
          <v-card-text class="white--text">
            <div class="d-flex align-center">
              <div class="flex-grow-1">
                <div class="text-h4 font-weight-bold">{{ analytics.activeSystemAdmins }}</div>
                <div class="text-subtitle-2">System Admins</div>
              </div>
              <v-icon size="48" color="white" class="ml-3">mdi-account-tie</v-icon>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card class="metric-card elevation-2" color="orange">
          <v-card-text class="white--text">
            <div class="d-flex align-center">
              <div class="flex-grow-1">
                <div class="text-h4 font-weight-bold">{{ analytics.activeZoneSupervisors }}</div>
                <div class="text-subtitle-2">Zone Supervisors</div>
              </div>
              <v-icon size="48" color="white" class="ml-3">mdi-account-supervisor</v-icon>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Main Content Grid -->
    <v-row v-if="false">
      <!-- Zone Management -->
      <v-col cols="12" lg="8">
        <v-card class="elevation-2">
          <v-card-title class="primary white--text">
            <v-icon left color="white">mdi-map</v-icon>
            Zone Management
          </v-card-title>
          <v-card-text class="pa-0">
            <ArcGISZoneManager
              :zones="zones"
              :farmers="farmers"
              :user-role="'EXPORTER'"
              @zone-selected="onZoneSelected"
              @zone-created="onZoneCreated"
              @zone-updated="onZoneUpdated"
              @zone-deleted="onZoneDeleted"
              @show-message="showMessage"
            />
          </v-card-text>
        </v-card>
      </v-col>

      <!-- Quick Actions & Analytics -->
      <v-col cols="12" lg="4">
        <v-row>
          <!-- Quick Actions -->
          <v-col cols="12">
            <v-card class="elevation-2 mb-4">
              <v-card-title class="info white--text">
                <v-icon left color="white">mdi-lightning-bolt</v-icon>
                Quick Actions
              </v-card-title>
              <v-card-text class="pa-4">
                <v-btn
                  block
                  color="primary"
                  outlined
                  @click="openCreateSystemAdminDialog"
                  class="mb-3"
                >
                  <v-icon left>mdi-account-plus</v-icon>
                  Add System Admin
                </v-btn>
                <v-btn
                  block
                  color="success"
                  outlined
                  @click="openCreateZoneSupervisorDialog"
                  class="mb-3"
                >
                  <v-icon left>mdi-account-supervisor</v-icon>
                  Add Zone Supervisor
                </v-btn>
                <v-btn
                  block
                  color="info"
                  outlined
                  @click="openRolePermissionsDialog"
                  class="mb-3"
                >
                  <v-icon left>mdi-shield-account</v-icon>
                  Manage Permissions
                </v-btn>
                <v-btn
                  block
                  color="warning"
                  outlined
                  @click="checkDataIntegrity"
                  class="mb-3"
                >
                  <v-icon left>mdi-database-check</v-icon>
                  Check Data Integrity
                </v-btn>
                <v-btn
                  block
                  color="error"
                  outlined
                  @click="fixDataIntegrityIssues"
                >
                  <v-icon left>mdi-database-sync</v-icon>
                  Fix Data Issues
                </v-btn>
              </v-card-text>
            </v-card>
          </v-col>

          <!-- Zone Breakdown -->
          <v-col cols="12">
            <v-card class="elevation-2 mb-4">
              <v-card-title class="warning white--text">
                <v-icon left color="white">mdi-chart-pie</v-icon>
                Zone Breakdown
              </v-card-title>
              <v-card-text class="pa-4">
                <div v-if="analytics.zoneBreakdown && analytics.zoneBreakdown.length > 0">
                  <div v-for="zone in analytics.zoneBreakdown" :key="zone.zoneId" class="zone-item mb-3">
                    <div class="d-flex justify-space-between align-center pa-3 grey lighten-4 rounded">
                      <div>
                        <div class="font-weight-medium">{{ zone.zoneName }}</div>
                        <div class="text-caption text--secondary">{{ zone.farmerCount }} farmers</div>
                      </div>
                      <v-chip small :color="getZoneStatusColor(zone.farmerCount)" text-color="white">
                        {{ zone.supervisorCount }} supervisors
                      </v-chip>
                    </div>
                  </div>
                </div>
                <div v-else class="text-center text--secondary">
                  No zone data available
                </div>
              </v-card-text>
            </v-card>
          </v-col>

          <!-- Recent Activity -->
          <v-col cols="12">
            <v-card class="elevation-2">
              <v-card-title class="grey darken-2 white--text">
                <v-icon left color="white">mdi-history</v-icon>
                Recent Activity
              </v-card-title>
              <v-card-text class="pa-4">
                <v-timeline dense>
                  <v-timeline-item
                    v-for="activity in recentActivities"
                    :key="activity.id"
                    :color="activity.color"
                    small
                  >
                    <template v-slot:icon>
                      <v-icon color="white" small>mdi-circle</v-icon>
                    </template>
                    <div>
                      <div class="font-weight-medium">{{ activity.title }}</div>
                      <div class="text-caption text--secondary">{{ formatTime(activity.timestamp) }}</div>
                    </div>
                  </v-timeline-item>
                </v-timeline>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-col>
    </v-row>

    <!-- Data Lists Row -->
    <v-row class="tw-mt-8">
      <v-col cols="12" md="6">
        <ZoneList
          :zones="zones"
          :loading="loading"
          @select="onZoneSelected"
        />
      </v-col>
      <v-col cols="12" md="6">
        <FarmerList
          :farmers="farmers"
          :loading="loading"
          @select="onFarmerSelected"
        />
      </v-col>
    </v-row>

    <!-- Harvest Predictions -->
    <v-row class="mt-4">
      <v-col cols="12">
        <v-card class="elevation-2">
          <v-card-title class="teal white--text">
            <v-icon left color="white">mdi-sprout</v-icon>
            Upcoming Harvests
            <v-spacer></v-spacer>
            <v-btn small text dark @click="loadHarvestPredictions" :loading="loadingHarvests">
              <v-icon left>mdi-refresh</v-icon>
              Refresh
            </v-btn>
          </v-card-title>
          <v-data-table
            :items="harvestPredictions"
            :headers="harvestHeaders"
            class="elevation-0"
            :loading="loadingHarvests"
            :items-per-page="10"
          >
            <template v-slot:item.predictedHarvestDate="{ item }">
              <span v-if="item.predictedHarvestDate">{{ formatDate(item.predictedHarvestDate) }}</span>
              <span v-else class="text--secondary">Pending</span>
            </template>
            <template v-slot:item.plantingDate="{ item }">
              <span v-if="item.plantingDate">{{ formatDate(item.plantingDate) }}</span>
              <span v-else class="text--secondary">—</span>
            </template>
            <template v-slot:item.status="{ item }">
              <v-chip small :color="statusColor(item.status)" text-color="white">{{ item.status }}</v-chip>
            </template>
            <template v-slot:item.confidence="{ item }">
              <span v-if="item.confidence">{{ (item.confidence * 100).toFixed(0) }}%</span>
              <span v-else class="text--secondary">—</span>
            </template>
            <template v-slot:no-data>
              <div class="text-center pa-6">
                <v-icon size="64" color="grey lighten-2">mdi-sprout-outline</v-icon>
                <p class="text-h6 text--secondary mt-3">No harvest predictions available</p>
                <p class="text--secondary">Predictions will appear as farmers plant crops</p>
              </div>
            </template>
          </v-data-table>
        </v-card>
      </v-col>
    </v-row>

    <!-- System Admin Management Dialog -->
    <v-dialog v-model="createSystemAdminDialog" max-width="500">
      <v-card>
        <v-card-title>Create System Admin</v-card-title>
        <v-card-text>
          <v-form ref="systemAdminForm" v-model="systemAdminFormValid">
            <v-text-field
              v-model="newSystemAdmin.fullName"
              label="Full Name"
              :rules="[rules.required]"
              variant="outlined"
            />
            <v-text-field
              v-model="newSystemAdmin.email"
              label="Email"
              :rules="[rules.required, rules.email]"
              variant="outlined"
            />
            <v-text-field
              v-model="newSystemAdmin.phoneNumber"
              label="Phone Number"
              variant="outlined"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="createSystemAdminDialog = false">Cancel</v-btn>
          <v-btn
            color="primary"
            @click="createSystemAdmin"
            :loading="creatingSystemAdmin"
            :disabled="!systemAdminFormValid"
          >
            Create
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Zone Supervisor Management Dialog -->
    <v-dialog v-model="createZoneSupervisorDialog" max-width="500">
      <v-card>
        <v-card-title>Create Zone Supervisor</v-card-title>
        <v-card-text>
          <v-form ref="zoneSupervisorForm" v-model="zoneSupervisorFormValid">
            <v-text-field
              v-model="newZoneSupervisor.fullName"
              label="Full Name"
              :rules="[rules.required]"
              variant="outlined"
            />
            <v-text-field
              v-model="newZoneSupervisor.email"
              label="Email"
              :rules="[rules.required, rules.email]"
              variant="outlined"
            />
            <v-text-field
              v-model="newZoneSupervisor.phoneNumber"
              label="Phone Number"
              variant="outlined"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="createZoneSupervisorDialog = false">Cancel</v-btn>
          <v-btn
            color="primary"
            @click="createZoneSupervisor"
            :loading="creatingZoneSupervisor"
            :disabled="!zoneSupervisorFormValid"
          >
            Create
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  <!-- Role Permissions Dialog -->
  <RolePermissionsDialog v-model="rolePermissionsDialog" />
  <!-- Farmer Detail Drawer -->
  <FarmerDetailDrawer
    v-model="farmerDrawer"
    :farmer="selectedFarmer"
    @locate="centerOnFarmer"
    @message="messageFarmer"
    @flag="flagFarmer"
  />

    <!-- Snackbar for messages -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="4000">
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn variant="text" @click="snackbar.show = false">Close</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>
<script>
import ArcGISZoneManager from '@/components/zone/ArcGISZoneManager.vue';
import ZoneList from '@/components/exporter/ZoneList.vue';
import FarmerList from '@/components/exporter/FarmerList.vue';
import RolePermissionsDialog from '@/components/exporter/RolePermissionsDialog.vue';
import FarmerDetailDrawer from '@/components/exporter/FarmerDetailDrawer.vue';
import CompleteSignUp from '@/views/exporter/CompleteSignUp.vue';
import UnderReviewMessage from '@/components/layout/dashboard/UnderReviewMessage.vue';
import axios from 'axios';
// import harvestService from '@/services/harvestPrediction.service.js';
import { getCurrentUserId } from '@/utils/roles.js';
import { mapState } from 'vuex';

export default {
  name: 'ExporterDashboardLayout',
  components: {
    ArcGISZoneManager,
    ZoneList,
    FarmerList,
    RolePermissionsDialog,
    FarmerDetailDrawer,
    CompleteSignUp,
    UnderReviewMessage,
  },
  computed: {
    ...mapState({
      user: (state) => state.auth.user,
    }),
    exporterId() {
      return getCurrentUserId() || null;
    },
    verificationStatus() {
      return this.user?.verificationStatus || 'PENDING';
    },
    isVerified() {
      return this.verificationStatus === 'VERIFIED';
    },
  },
  data() {
    return {
      loading: false,
      analytics: {
        totalZones: 0,
        totalFarmers: 0,
        activeSystemAdmins: 0,
        activeZoneSupervisors: 0,
        zoneBreakdown: [],
      },
      zones: [],
      farmers: [],
      recentActivities: [],

      // Dialog states
      createSystemAdminDialog: false,
      createZoneSupervisorDialog: false,
      rolePermissionsDialog: false,
      farmerDrawer: false,

      // Form data
      newSystemAdmin: {
        fullName: '',
        email: '',
        phoneNumber: '',
      },
      newZoneSupervisor: {
        fullName: '',
        email: '',
        phoneNumber: '',
      },
      selectedFarmer: null,

      // Form validation
      systemAdminFormValid: false,
      zoneSupervisorFormValid: false,
      creatingSystemAdmin: false,
      creatingZoneSupervisor: false,

      // Validation rules
      rules: {
        required: (value) => !!value || 'Required.',
        email: (value) => {
          const pattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
          return pattern.test(value) || 'Invalid email.';
        },
      },

      // Snackbar
      snackbar: {
        show: false,
        text: '',
        color: 'success',
      },

      // Harvest predictions
      harvestPredictions: [],
      loadingHarvests: false,
      harvestHeaders: [
        { title: 'Produce', key: 'produceName' },
        { title: 'Farmer', key: 'farmerName' },
        { title: 'Planting', key: 'plantingDate' },
        { title: 'Predicted Harvest', key: 'predictedHarvestDate' },
        { title: 'Species', key: 'predictedSpecies' },
        { title: 'Confidence', key: 'confidence' },
        { title: 'Status', key: 'status' },
      ],
    };
  },
  mounted() {
    this.loadDashboardData();
    this.loadHarvestPredictions();
  },
  methods: {
    async loadDashboardData() {
      if (!this.exporterId) {
        this.showMessage({ type: 'error', text: 'Exporter ID not found' });
        return;
      }

      this.loading = true;

      try {
        // Try to load synchronized data first
        const syncResponse = await axios.get(`/api/exporters-service/exporter/${this.exporterId}/dashboard/synchronized`);
        if (syncResponse.data.success) {
          const syncData = syncResponse.data.data;
          this.analytics = syncData.analytics;
          this.zones = syncData.zones;
          this.farmers = syncData.farmers;
          this.harvestPredictions = syncData.harvestPredictions;
          this.recentActivities = syncData.recentActivities;

          // Check data integrity status
          if (syncData.dataIntegrityStatus !== 'SYNCHRONIZED') {
            this.showMessage({
              type: 'warning',
              text: `Data integrity status: ${syncData.dataIntegrityStatus}`,
            });
          }
        } else {
          throw new Error('Synchronized data not available');
        }
      } catch (error) {
        // console.warn('Synchronized data loading failed, falling back to individual endpoints:', error);
        // Fallback to individual data loading
        try {
          await Promise.all([
            this.loadAnalytics(),
            this.loadZones(),
            this.loadFarmers(),
            this.loadRecentActivities(),
            this.loadHarvestPredictions(),
          ]);
        } catch (fallbackError) {
          this.showMessage({ type: 'error', text: 'Failed to load dashboard data' });
        }
      } finally {
        this.loading = false;
      }
    },
    async loadHarvestPredictions() {
      if (!this.exporterId) return;
      this.loadingHarvests = true;
      try {
        // Mock harvest predictions data
        await new Promise((resolve) => setTimeout(resolve, 800)); // Simulate API call
        this.harvestPredictions = [
          {
            id: 1,
            produceName: 'Tomatoes',
            farmerName: 'John Farmer',
            plantingDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            predictedHarvestDate: new Date(Date.now() + 15 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            predictedSpecies: 'Roma Tomatoes',
            confidence: 0.87,
            status: 'HARVEST_PLANNED',
          },
          {
            id: 2,
            produceName: 'Maize',
            farmerName: 'Mary Johnson',
            plantingDate: new Date(Date.now() - 45 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            predictedHarvestDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            predictedSpecies: 'Sweet Corn',
            confidence: 0.92,
            status: 'HARVEST_PLANNED',
          },
          {
            id: 3,
            produceName: 'Potatoes',
            farmerName: 'David Smith',
            plantingDate: new Date(Date.now() - 20 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            predictedHarvestDate: new Date(Date.now() + 25 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            predictedSpecies: 'Russet Potatoes',
            confidence: 0.78,
            status: 'HARVEST_PLANNED',
          },
        ];
      } finally {
        this.loadingHarvests = false;
      }
    },

    async loadAnalytics() {
      try {
        const response = await axios.get('/api/exporters-service/exporter/analytics', {
          params: { exporterId: this.exporterId },
        });
        if (response.data.success) {
          this.analytics = response.data.data;
        }
      } catch (error) {
        this.showMessage({ type: 'error', text: 'Failed to load analytics data' });
      }
    },

    async loadZones() {
      try {
        const response = await axios.get('/api/admin-service/zones', {
          params: this.exporterId ? { exporterId: this.exporterId } : {},
        });
        if (response.data.success) {
          this.zones = response.data.data;
        }
      } catch (error) {
        if (error.response && error.response.status === 403) {
          this.showMessage({ type: 'error', text: 'Access denied loading zones.' });
        }
      }
    },

    async loadFarmers() {
      try {
        const response = await axios.get('/api/zone-supervisor-service/farmers', {
          params: this.exporterId ? { exporterId: this.exporterId } : {},
        });
        if (response.data.success) {
          this.farmers = response.data.data;
        }
      } catch (error) {
        if (error.response && error.response.status === 403) {
          this.showMessage({ type: 'error', text: 'Access denied loading farmers.' });
        }
      }
    },

    async loadRecentActivities() {
      // Mock recent activities - in production, this would come from an audit log
      this.recentActivities = [
        {
          id: 1,
          title: 'New zone "North Region" created',
          timestamp: new Date(Date.now() - 1000 * 60 * 30), // 30 minutes ago
          color: 'blue',
        },
        {
          id: 2,
          title: 'System Admin John Doe added',
          timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2), // 2 hours ago
          color: 'green',
        },
        {
          id: 3,
          title: 'Zone Supervisor assigned to Central Zone',
          timestamp: new Date(Date.now() - 1000 * 60 * 60 * 4), // 4 hours ago
          color: 'orange',
        },
        {
          id: 4,
          title: 'Permissions updated for Zone Supervisor role',
          timestamp: new Date(Date.now() - 1000 * 60 * 60 * 24), // 1 day ago
          color: 'purple',
        },
      ];
    },

    async refreshData() {
      if (!this.exporterId) {
        this.showMessage({ type: 'error', text: 'Exporter ID not found' });
        return;
      }

      this.loading = true;
      try {
        // Use synchronized refresh endpoint
        const response = await axios.post(`/api/exporters-service/exporter/${this.exporterId}/dashboard/refresh`);
        if (response.data.success) {
          const syncData = response.data.data;
          this.analytics = syncData.analytics;
          this.zones = syncData.zones;
          this.farmers = syncData.farmers;
          this.harvestPredictions = syncData.harvestPredictions;
          this.recentActivities = syncData.recentActivities;
          this.showMessage({ type: 'success', text: 'Dashboard data synchronized successfully' });
        } else {
          throw new Error(response.data.message || 'Failed to refresh data');
        }
      } catch (error) {
        this.showMessage({ type: 'error', text: 'Failed to refresh dashboard data' });
        // Fallback to individual data loading
        await this.loadDashboardData();
      } finally {
        this.loading = false;
      }
    },

    exportReport() {
      // Mock export functionality
      this.showMessage({ type: 'info', text: 'Export functionality coming soon' });
    },

    async checkDataIntegrity() {
      if (!this.exporterId) return;

      try {
        const response = await axios.get(`/api/exporters-service/exporter/${this.exporterId}/data-integrity/check`);
        if (response.data.success) {
          const report = response.data.data;
          if (report.overallStatus === 'HEALTHY') {
            this.showMessage({ type: 'success', text: 'Data integrity check passed - no issues found' });
          } else {
            this.showMessage({
              type: 'warning',
              text: `Data integrity issues found: ${report.totalIssues} (${report.overallStatus})`,
            });
          }
        }
      } catch (error) {
        this.showMessage({ type: 'error', text: 'Failed to check data integrity' });
      }
    },

    async fixDataIntegrityIssues() {
      if (!this.exporterId) return;

      try {
        const response = await axios.post(`/api/exporters-service/exporter/${this.exporterId}/data-integrity/fix`);
        if (response.data.success) {
          const result = response.data.data;
          this.showMessage({
            type: 'success',
            text: `Fixed ${result.issuesFixed} of ${result.totalIssuesFound} issues`,
          });
          // Refresh data after fixing issues
          await this.refreshData();
        }
      } catch (error) {
        this.showMessage({ type: 'error', text: 'Failed to fix data integrity issues' });
      }
    },

    openCreateSystemAdminDialog() {
      this.newSystemAdmin = { fullName: '', email: '', phoneNumber: '' };
      this.createSystemAdminDialog = true;
    },

    openCreateZoneSupervisorDialog() {
      this.newZoneSupervisor = { fullName: '', email: '', phoneNumber: '' };
      this.createZoneSupervisorDialog = true;
    },

    openRolePermissionsDialog() {
      this.rolePermissionsDialog = true;
    },

    async createSystemAdmin() {
      if (!this.systemAdminFormValid) return;

      this.creatingSystemAdmin = true;

      try {
        const payload = {
          ...this.newSystemAdmin,
          exporterId: this.exporterId,
        };
        const response = await axios.post('/api/admin-service/system-admins', payload);

        if (response.data.success) {
          this.showMessage({ type: 'success', text: 'System Admin created successfully' });
          this.createSystemAdminDialog = false;
          await this.loadAnalytics();
        }
      } catch (error) {
        if (error.response && error.response.status === 403) {
          this.showMessage({ type: 'error', text: 'Access denied creating System Admin' });
        } else {
          this.showMessage({ type: 'error', text: 'Failed to create System Admin' });
        }
      } finally {
        this.creatingSystemAdmin = false;
      }
    },

    async createZoneSupervisor() {
      if (!this.zoneSupervisorFormValid) return;

      this.creatingZoneSupervisor = true;

      try {
        const payload = {
          ...this.newZoneSupervisor,
          exporterId: this.exporterId,
        };
        const response = await axios.post('/api/admin-service/zone-supervisors', payload);

        if (response.data.success) {
          this.showMessage({ type: 'success', text: 'Zone Supervisor created successfully' });
          this.createZoneSupervisorDialog = false;
          await this.loadAnalytics();
        }
      } catch (error) {
        if (error.response && error.response.status === 403) {
          this.showMessage({ type: 'error', text: 'Access denied creating Zone Supervisor' });
        } else {
          this.showMessage({ type: 'error', text: 'Failed to create Zone Supervisor' });
        }
      } finally {
        this.creatingZoneSupervisor = false;
      }
    },

    // eslint-disable-next-line no-unused-vars
    onZoneSelected(zone) {
    },

    async onZoneCreated(zone) {
      this.zones.push(zone);
      await this.loadAnalytics();
      this.showMessage({ type: 'success', text: `Zone "${zone.name}" created successfully` });
    },

    async onZoneUpdated(zone) {
      const index = this.zones.findIndex((z) => z.id === zone.id);
      if (index !== -1) {
        this.zones.splice(index, 1, zone);
      }
      this.showMessage({ type: 'success', text: `Zone "${zone.name}" updated successfully` });
    },

    async onZoneDeleted(zoneId) {
      this.zones = this.zones.filter((z) => z.id !== zoneId);
      await this.loadAnalytics();
      this.showMessage({ type: 'success', text: 'Zone deleted successfully' });
    },

    getZoneStatusColor(farmerCount) {
      if (farmerCount === 0) return 'grey';
      if (farmerCount < 5) return 'orange';
      if (farmerCount < 15) return 'blue';
      return 'green';
    },

    formatTime(timestamp) {
      const now = new Date();
      const diff = now - timestamp;
      const minutes = Math.floor(diff / (1000 * 60));
      const hours = Math.floor(diff / (1000 * 60 * 60));
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));

      if (minutes < 60) return `${minutes}m ago`;
      if (hours < 24) return `${hours}h ago`;
      return `${days}d ago`;
    },

    showMessage(message) {
      this.snackbar = {
        show: true,
        text: message.text,
        color: (() => {
          if (message.type === 'error') return 'error';
          if (message.type === 'warning') return 'warning';
          return 'success';
        })(),
      };
    },
    onFarmerSelected(farmer) {
      this.selectedFarmer = farmer;
      this.farmerDrawer = true;
    },
    centerOnFarmer(farmer) {
      this.showMessage({ type: 'success', text: `Centering map on ${farmer.farmerName || farmer.name}` });
    },
    messageFarmer(farmer) {
      this.showMessage({ type: 'info', text: `Messaging ${farmer.farmerName || farmer.name} (stub)` });
    },
    flagFarmer(farmer) {
      this.showMessage({ type: 'warning', text: `Flagged ${farmer.farmerName || farmer.name} (stub)` });
    },
    onVerificationCompleted() {
      // Refresh the dashboard data after verification completion
      this.loadDashboardData();
      this.showMessage({ type: 'success', text: 'Verification submitted successfully! Your account is now under review.' });
    },
    formatDate(dt) {
      try { return new Date(dt).toLocaleDateString(); } catch { return dt; }
    },
    statusColor(status) {
      if (status === 'HARVEST_PLANNED') return 'teal';
      if (status === 'HARVESTED') return 'green';
      return 'grey';
    },
  },
};
</script>

<style scoped>
.exporter-dashboard {
  background-color: #f8fafc;
  min-height: 100vh;
  padding: 1rem 0;
}

.dashboard-header {
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

.zone-item {
  margin-bottom: 0.75rem;
}

/* Timeline styling */
.v-timeline {
  max-height: 300px;
  overflow-y: auto;
}

/* Mobile Responsiveness */
@media (max-width: 768px) {
  .exporter-dashboard {
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

  .v-card-text {
    padding: 1rem !important;
  }

  .v-data-table {
    font-size: 0.875rem;
  }
}

@media (max-width: 480px) {
  .dashboard-header h1 {
    font-size: 1.3rem !important;
  }

  .metric-card .text-h4 {
    font-size: 1.5rem !important;
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

  .metric-card:hover {
    transform: none;
  }
}

/* Clean professional styling */
.exporter-dashboard {
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

/* Data table responsive */
.v-data-table {
  border-radius: 8px;
}

/* Form styling */
.v-text-field, .v-select {
  margin-bottom: 1rem;
}

/* Dialog styling */
.v-dialog .v-card {
  border-radius: 12px;
}
</style>
