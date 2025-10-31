<template>
  <v-container fluid class="tw-p-6 tw-bg-gray-50 tw-min-h-screen">
    <!-- Header Section -->
    <div class="tw-flex tw-flex-col sm:tw-flex-row tw-justify-between tw-items-start sm:tw-items-center tw-mb-8">
      <div>
        <h1 class="tw-text-3xl tw-font-bold tw-text-gray-800 tw-mb-2">Zone Supervisor Dashboard</h1>
        <p>Farmer operations and pickup coordination</p>
      </div>
      <div class="tw-flex tw-gap-3 tw-mt-4 sm:tw-mt-0">
        <v-btn color="primary" @click="refreshData" :loading="loading">
          <v-icon left>mdi-refresh</v-icon>
          Refresh
        </v-btn>
        <v-btn color="success" @click="openSchedulePickupDialog">
          <v-icon left>mdi-calendar-plus</v-icon>
          Schedule Pickup
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
                <p class="tw-text-3xl tw-font-bold tw-text-blue-600">{{ assignedZones.length }}</p>
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
                <p class="tw-text-sm tw-text-gray-600 tw-mb-1">Farmers</p>
                <p class="tw-text-3xl tw-font-bold tw-text-green-600">{{ farmersInZones.length }}</p>
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
                <p class="tw-text-sm tw-text-gray-600 tw-mb-1">Today's Pickups</p>
                <p class="tw-text-3xl tw-font-bold tw-text-orange-600">{{ todaysPickups }}</p>
              </div>
              <v-avatar color="orange" class="tw-bg-orange-100">
                <v-icon color="orange">mdi-truck</v-icon>
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
                <p class="tw-text-sm tw-text-gray-600 tw-mb-1">Completion Rate</p>
                <p class="tw-text-3xl tw-font-bold tw-text-purple-600">{{ completionRate }}%</p>
              </div>
              <v-avatar color="purple" class="tw-bg-purple-100">
                <v-icon color="purple">mdi-chart-line</v-icon>
              </v-avatar>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Main Content -->
    <v-row>
      <!-- Map and Route Optimization -->
      <v-col cols="12" lg="8">
        <v-tabs v-model="activeTab" class="tw-mb-4">
          <v-tab value="map">
            <v-icon left>mdi-map</v-icon>
            Zone Map
          </v-tab>
          <v-tab value="routes">
            <v-icon left>mdi-map-marker-path</v-icon>
            Route Optimizer
          </v-tab>
        </v-tabs>

        <v-card class="tw-rounded-xl tw-shadow-md tw-h-full">
          <v-window v-model="activeTab">
            <v-window-item value="map">
              <v-card-title class="tw-bg-gradient-to-r tw-from-green-500 tw-to-blue-500 tw-text-white tw-rounded-t-xl">
                <v-icon class="tw-mr-2">mdi-map</v-icon>
                Zone Overview
              </v-card-title>
              <v-card-text class="tw-p-0">
                <ArcGISZoneManager
                  :zones="assignedZones"
                  :farmers="farmersInZones"
                  :user-role="'ZONE_SUPERVISOR'"
                  :readonly="true"
                  @zone-selected="onZoneSelected"
                  @farmer-selected="onFarmerSelected"
                  @show-message="showMessage"
                />
              </v-card-text>
            </v-window-item>

            <v-window-item value="routes">
              <v-card-title class="tw-bg-gradient-to-r tw-from-orange-500 tw-to-red-500 tw-text-white tw-rounded-t-xl">
                <v-icon class="tw-mr-2">mdi-map-marker-path</v-icon>
                Route Optimization
              </v-card-title>
              <v-card-text class="tw-p-0">
                <RouteOptimizer
                  :zones="assignedZones"
                  :user-role="'ZONE_SUPERVISOR'"
                  @route-optimized="onRouteOptimized"
                  @pickups-scheduled="onPickupsScheduled"
                  @show-message="showMessage"
                />
              </v-card-text>
            </v-window-item>
          </v-window>
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
                  @click="openSchedulePickupDialog"
                >
                  <v-icon left>mdi-calendar-plus</v-icon>
                  Schedule Pickup
                </v-btn>
                <v-btn
                  block
                  color="success"
                  variant="outlined"
                  @click="openEditFarmerDialog"
                  :disabled="!selectedFarmer"
                >
                  <v-icon left>mdi-account-edit</v-icon>
                  Edit Farmer
                </v-btn>
                <v-btn
                  block
                  color="info"
                  variant="outlined"
                  @click="openZoneCommentsDialog"
                  :disabled="!selectedZone"
                >
                  <v-icon left>mdi-comment-edit</v-icon>
                  Update Comments
                </v-btn>
              </div>
            </v-card-text>
          </v-card>

          <!-- Farmers List -->
          <v-card class="tw-rounded-xl tw-shadow-md">
            <v-card-title class="tw-text-lg tw-font-semibold">
              <v-icon class="tw-mr-2">mdi-account-group</v-icon>
              Farmers in My Zones
            </v-card-title>
            <v-card-text>
              <div class="tw-space-y-3 tw-max-h-64 tw-overflow-y-auto">
                <div v-for="farmer in farmersInZones" :key="farmer.farmerId"
                     class="tw-flex tw-justify-between tw-items-center tw-p-3 tw-bg-gray-50 tw-rounded-lg tw-cursor-pointer hover:tw-bg-gray-100"
                     @click="selectFarmer(farmer)">
                  <div>
                    <p class="tw-font-medium tw-text-sm">{{ farmer.farmerName }}</p>
                    <p class="tw-text-xs tw-text-gray-600">{{ farmer.zoneName }}</p>
                  </div>
                  <v-chip size="small" :color="getFarmerStatusColor(farmer)">
                    {{ getFarmerStatus(farmer) }}
                  </v-chip>
                </div>
              </div>
            </v-card-text>
          </v-card>

          <!-- Today's Schedule -->
          <v-card class="tw-rounded-xl tw-shadow-md">
            <v-card-title class="tw-text-lg tw-font-semibold">
              <v-icon class="tw-mr-2">mdi-calendar-today</v-icon>
              Today's Schedule
            </v-card-title>
            <v-card-text>
              <v-timeline density="compact" class="tw-mt-2">
                <v-timeline-item
                  v-for="pickup in todaysSchedule"
                  :key="pickup.id"
                  :dot-color="getPickupStatusColor(pickup.status)"
                  size="small"
                >
                  <div class="tw-text-sm">
                    <p class="tw-font-medium">{{ pickup.farmerName }}</p>
                    <p class="tw-text-gray-600 tw-text-xs">{{ formatTime(pickup.scheduledTime) }}</p>
                  </div>
                </v-timeline-item>
              </v-timeline>
            </v-card-text>
          </v-card>
        </div>
      </v-col>
    </v-row>

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
import RouteOptimizer from '@/components/zone/RouteOptimizer.vue';

export default {
  name: 'ZoneSupervisorDashboard',
  components: {
    ArcGISZoneManager,
    RouteOptimizer,
  },
  data() {
    return {
      loading: false,
      activeTab: 'map',
      assignedZones: [],
      farmersInZones: [],
      selectedZone: null,
      selectedFarmer: null,
      todaysPickups: 0,
      completionRate: 85,
      todaysSchedule: [],

      // Dialog states
      schedulePickupDialog: false,
      editFarmerDialog: false,
      zoneCommentsDialog: false,

      // Form data
      newPickup: {
        farmerId: null,
        date: new Date().toISOString().substr(0, 10),
        time: '09:00',
        notes: '',
      },

      // Form validation
      pickupFormValid: false,
      schedulingPickup: false,

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
          this.loadAssignedZones(),
          this.loadFarmersInZones(),
          this.loadTodaysSchedule(),
        ]);
        this.calculateMetrics();
      } catch (error) {
        this.showMessage({ type: 'error', text: 'Failed to load dashboard data' });
      } finally {
        this.loading = false;
      }
    },

    async loadAssignedZones() {
      const response = await axios.get('/api/zone-supervisor-service/zones');
      if (response.data.success) {
        this.assignedZones = response.data.data;
      }
    },

    async loadFarmersInZones() {
      const response = await axios.get('/api/zone-supervisor-service/farmers');
      if (response.data.success) {
        this.farmersInZones = response.data.data;
      }
    },

    async loadTodaysSchedule() {
      try {
        const today = new Date().toISOString().split('T')[0];
        const response = await axios.get(`/api/zone-supervisor-service/pickup-schedules?date=${today}`);
        if (response.data.success) {
          this.todaysSchedule = response.data.data.map((pickup) => ({
            id: pickup.id,
            farmerName: pickup.farmerName,
            scheduledTime: new Date(pickup.scheduledDate),
            status: pickup.status,
          }));
        }
      } catch (error) {
        // Mock data for demonstration
        this.todaysSchedule = [
          {
            id: 1,
            farmerName: 'John Doe',
            scheduledTime: new Date(Date.now() + 1000 * 60 * 60 * 2), // 2 hours from now
            status: 'SCHEDULED',
          },
          {
            id: 2,
            farmerName: 'Jane Smith',
            scheduledTime: new Date(Date.now() + 1000 * 60 * 60 * 4), // 4 hours from now
            status: 'SCHEDULED',
          },
        ];
      }
    },

    calculateMetrics() {
      this.todaysPickups = this.todaysSchedule.length;
      // Mock completion rate calculation
      this.completionRate = Math.floor(Math.random() * 20) + 80; // 80-100%
    },

    async refreshData() {
      await this.loadDashboardData();
      this.showMessage({ type: 'success', text: 'Dashboard data refreshed' });
    },

    openSchedulePickupDialog() {
      this.newPickup = {
        farmerId: null,
        date: new Date().toISOString().substr(0, 10),
        time: '09:00',
        notes: '',
      };
      this.schedulePickupDialog = true;
    },

    openEditFarmerDialog() {
      if (!this.selectedFarmer) {
        this.showMessage({ type: 'warning', text: 'Please select a farmer first' });
        return;
      }
      this.showMessage({ type: 'info', text: 'Edit farmer functionality coming soon' });
    },

    openZoneCommentsDialog() {
      const zoneId = this.selectedZone ? this.selectedZone.id : null;
      this.$router.push({ name: 'ZoneCommentsManagement', query: zoneId ? { zoneId } : {} });
    },

    async schedulePickup() {
      if (!this.pickupFormValid) return;

      this.schedulingPickup = true;

      try {
        const pickupDateTime = new Date(`${this.newPickup.date}T${this.newPickup.time}:00`);

        const response = await axios.post('/api/zone-supervisor-service/pickup-schedules', {
          farmerId: this.newPickup.farmerId,
          exporterId: this.$store.getters.currentUser.exporterId,
          produceListingId: 'default', // This should come from farmer's active listing
          scheduledDate: pickupDateTime.toISOString(),
          pickupNotes: this.newPickup.notes,
        });

        if (response.data.success) {
          this.showMessage({ type: 'success', text: 'Pickup scheduled successfully' });
          this.schedulePickupDialog = false;
          await this.loadTodaysSchedule();
        }
      } catch (error) {
        this.showMessage({ type: 'error', text: 'Failed to schedule pickup' });
      } finally {
        this.schedulingPickup = false;
      }
    },

    onZoneSelected(zone) {
      this.selectedZone = zone;
    },

    onFarmerSelected(farmer) {
      this.selectedFarmer = farmer;
    },

    onRouteOptimized(route) {
      this.showMessage({
        type: 'success',
        text: `Route optimized: ${route.estimatedDistance}km, ${route.estimatedDuration}min`,
      });
    },

    onPickupsScheduled() {
      this.loadTodaysSchedule();
    },

    selectFarmer(farmer) {
      this.selectedFarmer = farmer;
    },

    getFarmerStatus(farmer) {
      if (!farmer.lastPickupDate) return 'New';

      const daysSince = Math.floor((Date.now() - new Date(farmer.lastPickupDate)) / (1000 * 60 * 60 * 24));
      if (daysSince < 7) return 'Recent';
      if (daysSince < 30) return 'Regular';
      return 'Overdue';
    },

    getFarmerStatusColor(farmer) {
      const status = this.getFarmerStatus(farmer);
      switch (status) {
        case 'New': return 'blue';
        case 'Recent': return 'green';
        case 'Regular': return 'orange';
        case 'Overdue': return 'red';
        default: return 'grey';
      }
    },

    getPickupStatusColor(status) {
      switch (status) {
        case 'SCHEDULED': return 'blue';
        case 'IN_PROGRESS': return 'orange';
        case 'COMPLETED': return 'green';
        case 'CANCELLED': return 'red';
        default: return 'grey';
      }
    },

    formatTime(timestamp) {
      return new Date(timestamp).toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      });
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
/* Custom gradient backgrounds */
.tw-bg-gradient-to-r {
  background: linear-gradient(to right, var(--tw-gradient-stops));
}

.tw-from-green-500 {
  --tw-gradient-from: #10b981;
  --tw-gradient-stops: var(--tw-gradient-from), var(--tw-gradient-to, rgba(16, 185, 129, 0));
}

.tw-to-blue-500 {
  --tw-gradient-to: #3b82f6;
}

.tw-from-orange-500 {
  --tw-gradient-from: #f97316;
  --tw-gradient-stops: var(--tw-gradient-from), var(--tw-gradient-to, rgba(249, 115, 22, 0));
}

.tw-to-red-500 {
  --tw-gradient-to: #ef4444;
}

/* Hover effects */
.hover\:tw-shadow-lg:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.tw-transition-shadow {
  transition: box-shadow 0.3s ease-in-out;
}

.hover\:tw-bg-gray-100:hover {
  background-color: #f3f4f6;
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
