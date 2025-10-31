<template>
  <v-container fluid class="tw-p-6 tw-bg-gray-50 tw-min-h-screen">
    <!-- Header Section -->
    <div class="tw-flex tw-flex-col sm:tw-flex-row tw-justify-between tw-items-start sm:tw-items-center tw-mb-8">
      <div>
        <h1 class="tw-text-3xl tw-font-bold tw-text-gray-800 tw-mb-2">System Admin Dashboard</h1>
        <p class="tw-text-gray-600">Zone and farmer management operations</p>
      </div>
      <div class="tw-flex tw-gap-3 tw-mt-4 sm:tw-mt-0">
        <v-btn color="primary" @click="refreshData" :loading="loading">
          <v-icon left>mdi-refresh</v-icon>
          Refresh
        </v-btn>
        <v-btn color="success" @click="openCreateZoneDialog">
          <v-icon left>mdi-plus</v-icon>
          New Zone
        </v-btn>
      </div>
    </div>

    <!-- Key Metrics Cards -->
    <v-row class="tw-mb-8">
      <v-col cols="12" sm="6" md="3">
        <v-card class="tw-rounded-xl tw-shadow-md hover:tw-shadow-lg tw-transition-shadow tw-h-full">
          <v-card-text class="tw-p-6">
            <div class="tw-flex tw-items-center tw-justify-between">
              <div>
                <p class="tw-text-sm tw-text-gray-600 tw-mb-1">My Zones</p>
                <p class="tw-text-3xl tw-font-bold tw-text-blue-600">{{ myZones.length }}</p>
              </div>
              <v-avatar color="blue" class="tw-bg-blue-100">
                <v-icon color="blue">mdi-map-marker-multiple</v-icon>
              </v-avatar>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card class="tw-rounded-xl tw-shadow-md hover:tw-shadow-lg tw-transition-shadow tw-h-full">
          <v-card-text class="tw-p-6">
            <div class="tw-flex tw-items-center tw-justify-between">
              <div>
                <p class="tw-text-sm tw-text-gray-600 tw-mb-1">Total Farmers</p>
                <p class="tw-text-3xl tw-font-bold tw-text-green-600">{{ totalFarmers }}</p>
              </div>
              <v-avatar color="green" class="tw-bg-green-100">
                <v-icon color="green">mdi-account-group</v-icon>
              </v-avatar>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card class="tw-rounded-xl tw-shadow-md hover:tw-shadow-lg tw-transition-shadow tw-h-full">
          <v-card-text class="tw-p-6">
            <div class="tw-flex tw-items-center tw-justify-between">
              <div>
                <p class="tw-text-sm tw-text-gray-600 tw-mb-1">Zone Supervisors</p>
                <p class="tw-text-3xl tw-font-bold tw-text-purple-600">{{ zoneSupervisors.length }}</p>
              </div>
              <v-avatar color="purple" class="tw-bg-purple-100">
                <v-icon color="purple">mdi-account-supervisor</v-icon>
              </v-avatar>
            </div>
          </v-card-text>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card class="tw-rounded-xl tw-shadow-md hover:tw-shadow-lg tw-transition-shadow tw-h-full">
          <v-card-text class="tw-p-6">
            <div class="tw-flex tw-items-center tw-justify-between">
              <div>
                <p class="tw-text-sm tw-text-gray-600 tw-mb-1">Pending Tasks</p>
                <p class="tw-text-3xl tw-font-bold tw-text-orange-600">{{ pendingTasks }}</p>
              </div>
              <v-avatar color="orange" class="tw-bg-orange-100">
                <v-icon color="orange">mdi-clipboard-list</v-icon>
              </v-avatar>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Main Content -->
    <v-row>
      <!-- Zone Management -->
      <v-col cols="12" lg="8">
        <v-card class="tw-rounded-xl tw-shadow-md tw-h-full">
          <v-card-title class="tw-bg-gradient-to-r tw-from-blue-500 tw-to-purple-500 tw-text-white tw-rounded-t-xl">
            <v-icon class="tw-mr-2">mdi-map</v-icon>
            Zone Management
          </v-card-title>
          <v-card-text class="tw-p-0">
            <ArcGISZoneManager
              :zones="myZones"
              :farmers="farmers"
              :user-role="'SYSTEM_ADMIN'"
              @zone-selected="onZoneSelected"
              @zone-created="onZoneCreated"
              @zone-updated="onZoneUpdated"
              @show-message="showMessage"
            />
          </v-card-text>
        </v-card>
      </v-col>

      <!-- Side Panel -->
      <v-col cols="12" lg="4">
        <div class="tw-space-y-6">
          <!-- Quick Actions -->
          <v-card class="tw-rounded-xl tw-shadow-md">
            <v-card-title class="tw-text-lg tw-font-semibold">
              <v-icon class="tw-mr-2">mdi-lightning-bolt</v-icon>
              Quick Actions
            </v-card-title>
            <v-card-text>
              <div class="tw-space-y-3">
                <v-btn
                  block
                  color="primary"
                  variant="outlined"
                  @click="openCreateZoneDialog"
                >
                  <v-icon left>mdi-map-marker-plus</v-icon>
                  Create Zone
                </v-btn>
                <v-btn
                  block
                  color="success"
                  variant="outlined"
                  @click="openAssignSupervisorDialog"
                  :disabled="!selectedZone"
                >
                  <v-icon left>mdi-account-plus</v-icon>
                  Assign Supervisor
                </v-btn>
                <v-btn
                  block
                  color="info"
                  variant="outlined"
                  @click="openAddFarmerDialog"
                  :disabled="!selectedZone"
                >
                  <v-icon left>mdi-account-group</v-icon>
                  Add Farmer
                </v-btn>
              </div>
            </v-card-text>
          </v-card>

          <!-- Zone Supervisors -->
          <v-card class="tw-rounded-xl tw-shadow-md">
            <v-card-title class="tw-text-lg tw-font-semibold">
              <v-icon class="tw-mr-2">mdi-account-supervisor</v-icon>
              Zone Supervisors
            </v-card-title>
            <v-card-text>
              <div class="tw-space-y-3 tw-max-h-64 tw-overflow-y-auto">
                <div v-for="supervisor in zoneSupervisors" :key="supervisor.id"
                     class="tw-flex tw-justify-between tw-items-center tw-p-3 tw-bg-gray-50 tw-rounded-lg">
                  <div>
                    <p class="tw-font-medium tw-text-sm">{{ supervisor.fullName }}</p>
                    <p class="tw-text-xs tw-text-gray-600">{{ supervisor.zones?.length || 0 }} zones</p>
                  </div>
                  <v-chip size="small" :color="supervisor.status === 'ACTIVE' ? 'green' : 'grey'">
                    {{ supervisor.status }}
                  </v-chip>
                </div>
              </div>
            </v-card-text>
          </v-card>

          <!-- Recent Activities -->
          <v-card class="tw-rounded-xl tw-shadow-md">
            <v-card-title class="tw-text-lg tw-font-semibold">
              <v-icon class="tw-mr-2">mdi-history</v-icon>
              Recent Activities
            </v-card-title>
            <v-card-text>
              <v-timeline density="compact" class="tw-mt-2">
                <v-timeline-item
                  v-for="activity in recentActivities"
                  :key="activity.id"
                  :dot-color="activity.color"
                  size="small"
                >
                  <div class="tw-text-sm">
                    <p class="tw-font-medium">{{ activity.title }}</p>
                    <p class="tw-text-gray-600 tw-text-xs">{{ formatTime(activity.timestamp) }}</p>
                  </div>
                </v-timeline-item>
              </v-timeline>
            </v-card-text>
          </v-card>
        </div>
      </v-col>
    </v-row>

    <!-- Create Zone Dialog -->
    <v-dialog v-model="createZoneDialog" max-width="600">
      <v-card>
        <v-card-title>Create New Zone</v-card-title>
        <v-card-text>
          <v-form ref="zoneForm" v-model="zoneFormValid">
            <v-text-field
              v-model="newZone.name"
              label="Zone Name"
              :rules="[rules.required]"
              variant="outlined"
            />
            <v-text-field
              v-model="newZone.produceType"
              label="Produce Type"
              variant="outlined"
            />
            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="newZone.centerLatitude"
                  label="Center Latitude"
                  type="number"
                  :rules="[rules.required]"
                  variant="outlined"
                />
              </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model="newZone.centerLongitude"
                  label="Center Longitude"
                  type="number"
                  :rules="[rules.required]"
                  variant="outlined"
                />
              </v-col>
            </v-row>
            <v-slider
              v-model="newZone.radiusKm"
              label="Radius (km)"
              min="0.5"
              max="50"
              step="0.5"
              thumb-label
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="createZoneDialog = false">Cancel</v-btn>
          <v-btn
            color="primary"
            @click="createZone"
            :loading="creatingZone"
            :disabled="!zoneFormValid"
          >
            Create Zone
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

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
import axios from 'axios';
import ArcGISZoneManager from '@/components/zone/ArcGISZoneManager.vue';

export default {
  name: 'SystemAdminDashboard',
  components: {
    ArcGISZoneManager,
  },
  data() {
    return {
      loading: false,
      myZones: [],
      farmers: [],
      zoneSupervisors: [],
      selectedZone: null,
      totalFarmers: 0,
      pendingTasks: 0,
      recentActivities: [],

      // Dialog states
      createZoneDialog: false,
      assignSupervisorDialog: false,
      addFarmerDialog: false,

      // Form data
      newZone: {
        name: '',
        produceType: '',
        centerLatitude: null,
        centerLongitude: null,
        radiusKm: 5,
      },

      // Form validation
      zoneFormValid: false,
      creatingZone: false,

      // Validation rules
      rules: {
        required: (value) => !!value || 'Required.',
      },

      // Snackbar
      snackbar: {
        show: false,
        text: '',
        color: 'success',
      },
    };
  },
  mounted() {
    this.loadDashboardData();
  },
  methods: {
    async loadDashboardData() {
      this.loading = true;

      try {
        await Promise.all([
          this.loadMyZones(),
          this.loadFarmers(),
          this.loadZoneSupervisors(),
          this.loadRecentActivities(),
        ]);
        this.calculateMetrics();
      } catch (error) {
        this.showMessage({ type: 'error', text: 'Failed to load dashboard data' });
      } finally {
        this.loading = false;
      }
    },

    async loadMyZones() {
      const response = await axios.get('/api/admin-service/zones/my-zones');
      if (response.data.success) {
        this.myZones = response.data.data;
      }
    },

    async loadFarmers() {
      const response = await axios.get('/api/admin-service/farmers');
      if (response.data.success) {
        this.farmers = response.data.data;
      }
    },

    async loadZoneSupervisors() {
      const response = await axios.get('/api/admin-service/zone-supervisors');
      if (response.data.success) {
        this.zoneSupervisors = response.data.data;
      }
    },

    async loadRecentActivities() {
      // Mock recent activities - in production, this would come from an audit log
      this.recentActivities = [
        {
          id: 1,
          title: 'Zone "East Region" created',
          timestamp: new Date(Date.now() - 1000 * 60 * 15), // 15 minutes ago
          color: 'blue',
        },
        {
          id: 2,
          title: 'Supervisor assigned to Central Zone',
          timestamp: new Date(Date.now() - 1000 * 60 * 45), // 45 minutes ago
          color: 'green',
        },
        {
          id: 3,
          title: 'Farmer John added to North Zone',
          timestamp: new Date(Date.now() - 1000 * 60 * 60 * 2), // 2 hours ago
          color: 'orange',
        },
        {
          id: 4,
          title: 'Zone comments updated',
          timestamp: new Date(Date.now() - 1000 * 60 * 60 * 4), // 4 hours ago
          color: 'purple',
        },
      ];
    },

    calculateMetrics() {
      this.totalFarmers = this.farmers.length;
      this.pendingTasks = Math.floor(Math.random() * 10) + 1; // Mock pending tasks
    },

    async refreshData() {
      await this.loadDashboardData();
      this.showMessage({ type: 'success', text: 'Dashboard data refreshed' });
    },

    openCreateZoneDialog() {
      this.newZone = {
        name: '',
        produceType: '',
        centerLatitude: null,
        centerLongitude: null,
        radiusKm: 5,
      };
      this.createZoneDialog = true;
    },

    openAssignSupervisorDialog() {
      if (!this.selectedZone) {
        this.showMessage({ type: 'warning', text: 'Please select a zone first' });
        return;
      }
      this.showMessage({ type: 'info', text: 'Assign supervisor functionality coming soon' });
    },

    openAddFarmerDialog() {
      if (!this.selectedZone) {
        this.showMessage({ type: 'warning', text: 'Please select a zone first' });
        return;
      }
      this.showMessage({ type: 'info', text: 'Add farmer functionality coming soon' });
    },

    async createZone() {
      if (!this.zoneFormValid) return;

      this.creatingZone = true;

      try {
        const zoneData = {
          ...this.newZone,
          exporterId: this.$store.getters.currentUser.exporterId,
        };

        const response = await axios.post('/api/admin-service/zones', zoneData);

        if (response.data.success) {
          this.showMessage({ type: 'success', text: 'Zone created successfully' });
          this.createZoneDialog = false;
          await this.loadMyZones();
        }
      } catch (error) {
        this.showMessage({ type: 'error', text: 'Failed to create zone' });
      } finally {
        this.creatingZone = false;
      }
    },

    onZoneSelected(zone) {
      this.selectedZone = zone;
    },

    async onZoneCreated(zone) {
      this.myZones.push(zone);
      this.showMessage({ type: 'success', text: `Zone "${zone.name}" created successfully` });
    },

    async onZoneUpdated(zone) {
      const index = this.myZones.findIndex((z) => z.id === zone.id);
      if (index !== -1) {
        this.myZones.splice(index, 1, zone);
      }
      this.showMessage({ type: 'success', text: `Zone "${zone.name}" updated successfully` });
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
        // eslint-disable-next-line no-nested-ternary
        color: message.type === 'error' ? 'error' : message.type === 'warning' ? 'warning' : 'success',
      };
    },
  },
};
</script>

<style scoped>
/* Custom gradient background */
.tw-bg-gradient-to-r {
  background: linear-gradient(to right, #3b82f6, #8b5cf6);
}

/* Hover effects */
.hover\:tw-shadow-lg:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.tw-transition-shadow {
  transition: box-shadow 0.3s ease-in-out;
}

/* Custom scrollbar */
:deep(.tw-overflow-y-auto) {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e0 #f7fafc;
}

:deep(.tw-overflow-y-auto::-webkit-scrollbar) {
  width: 6px;
}

:deep(.tw-overflow-y-auto::-webkit-scrollbar-track) {
  background: #f7fafc;
}

:deep(.tw-overflow-y-auto::-webkit-scrollbar-thumb) {
  background-color: #cbd5e0;
  border-radius: 3px;
}

/* Card hover animations */
.v-card {
  transition: transform 0.2s ease-in-out, box-shadow 0.2s ease-in-out;
}

.v-card:hover {
  transform: translateY(-2px);
}
</style>
