<template>
  <div class="admin-dashboard tw-min-h-screen tw-bg-gray-50">
    <!-- Header Section -->
    <div class="tw-bg-white tw-shadow-sm tw-border-b tw-border-gray-200">
      <div class="tw-max-w-7xl tw-mx-auto tw-px-4 sm:tw-px-6 lg:tw-px-8">
        <div class="tw-py-6">
          <div class="tw-flex tw-items-center tw-justify-between">
            <div>
              <h1 class="tw-text-2xl tw-font-bold tw-text-gray-900">System Admin Dashboard</h1>
              <p class="tw-text-sm tw-text-gray-600 tw-mt-1">
                Manage zones and farmers efficiently
              </p>
            </div>
            <div class="tw-flex tw-items-center tw-space-x-4">
              <v-chip color="primary" small>
                <v-icon left small>mdi-account-cog</v-icon>
                System Admin
              </v-chip>
              <v-btn color="primary" @click="refreshData" :loading="loading">
                <v-icon left>mdi-refresh</v-icon>
                Refresh
              </v-btn>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Stats -->
    <div class="tw-max-w-7xl tw-mx-auto tw-px-4 sm:tw-px-6 lg:tw-px-8 tw-py-8">
      <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-3 tw-gap-6 tw-mb-8">
        <!-- Zones Managed -->
        <v-card class="tw-h-28 tw-relative tw-overflow-hidden" elevation="2">
          <div class="tw-absolute tw-inset-0 tw-bg-gradient-to-br tw-from-indigo-500 tw-to-indigo-600"></div>
          <v-card-text class="tw-relative tw-z-10 tw-text-white tw-h-full tw-flex tw-items-center tw-justify-between">
            <div>
              <p class="tw-text-indigo-100 tw-text-sm">Zones Managed</p>
              <p class="tw-text-3xl tw-font-bold">{{ stats.zonesManaged }}</p>
            </div>
            <v-icon size="48" color="white" class="tw-opacity-80">mdi-map-marker-radius</v-icon>
          </v-card-text>
        </v-card>

        <!-- Farmers Added -->
        <v-card class="tw-h-28 tw-relative tw-overflow-hidden" elevation="2">
          <div class="tw-absolute tw-inset-0 tw-bg-gradient-to-br tw-from-emerald-500 tw-to-emerald-600"></div>
          <v-card-text class="tw-relative tw-z-10 tw-text-white tw-h-full tw-flex tw-items-center tw-justify-between">
            <div>
              <p class="tw-text-emerald-100 tw-text-sm">Farmers Added</p>
              <p class="tw-text-3xl tw-font-bold">{{ stats.farmersAdded }}</p>
            </div>
            <v-icon size="48" color="white" class="tw-opacity-80">mdi-account-group</v-icon>
          </v-card-text>
        </v-card>

        <!-- Supervisors Assigned -->
        <v-card class="tw-h-28 tw-relative tw-overflow-hidden" elevation="2">
          <div class="tw-absolute tw-inset-0 tw-bg-gradient-to-br tw-from-amber-500 tw-to-amber-600"></div>
          <v-card-text class="tw-relative tw-z-10 tw-text-white tw-h-full tw-flex tw-items-center tw-justify-between">
            <div>
              <p class="tw-text-amber-100 tw-text-sm">Supervisors Assigned</p>
              <p class="tw-text-3xl tw-font-bold">{{ stats.supervisorsAssigned }}</p>
            </div>
            <v-icon size="48" color="white" class="tw-opacity-80">mdi-account-supervisor</v-icon>
          </v-card-text>
        </v-card>
      </div>

      <!-- Main Content -->
      <div class="tw-grid tw-grid-cols-1 lg:tw-grid-cols-2 tw-gap-8">
        <!-- Zone Management -->
        <v-card elevation="2" class="tw-h-fit">
          <v-card-title class="tw-flex tw-items-center tw-justify-between">
            <div class="tw-flex tw-items-center">
              <v-icon class="tw-mr-2" color="primary">mdi-map-marker-radius</v-icon>
              <span>Zone Management</span>
            </div>
            <v-btn small text @click="$router.push({ name: 'ZoneManagement' })">
              Manage All
              <v-icon right small>mdi-arrow-right</v-icon>
            </v-btn>
          </v-card-title>
          <v-card-text>
            <!-- Zone Creation Form -->
            <v-expansion-panels v-model="zoneCreationPanel" class="tw-mb-4">
              <v-expansion-panel>
                <v-expansion-panel-header>
                  <div class="tw-flex tw-items-center">
                    <v-icon class="tw-mr-2" color="success">mdi-plus-circle</v-icon>
                    <span>Create New Zone</span>
                  </div>
                </v-expansion-panel-header>
                <v-expansion-panel-content>
                  <v-form ref="zoneForm" v-model="zoneFormValid">
                    <v-text-field
                      v-model="newZone.name"
                      label="Zone Name"
                      :rules="[v => !!v || 'Zone name is required']"
                      density="compact"
                      variant="outlined"
                      class="tw-mb-2"
                    />
                    <v-text-field
                      v-model="newZone.produceType"
                      label="Produce Type (Optional)"
                      density="compact"
                      variant="outlined"
                      class="tw-mb-2"
                    />
                    <div class="tw-grid tw-grid-cols-2 tw-gap-2 tw-mb-2">
                      <v-text-field
                        v-model.number="newZone.centerLatitude"
                        label="Latitude"
                        type="number"
                        :rules="[v => !!v || 'Latitude is required']"
                        density="compact"
                        variant="outlined"
                      />
                      <v-text-field
                        v-model.number="newZone.centerLongitude"
                        label="Longitude"
                        type="number"
                        :rules="[v => !!v || 'Longitude is required']"
                        density="compact"
                        variant="outlined"
                      />
                    </div>
                    <v-slider
                      v-model="newZone.radiusKm"
                      label="Radius (km)"
                      min="0.5"
                      max="50"
                      step="0.5"
                      thumb-label
                      class="tw-mb-4"
                    />
                    <div class="tw-flex tw-justify-end tw-space-x-2">
                      <v-btn size="small" @click="resetZoneForm">Reset</v-btn>
                      <v-btn
                        size="small"
                        color="primary"
                        @click="createZone"
                        :disabled="!zoneFormValid"
                        :loading="creatingZone"
                      >
                        Create Zone
                      </v-btn>
                    </div>
                  </v-form>
                </v-expansion-panel-content>
              </v-expansion-panel>
            </v-expansion-panels>

            <!-- Recent Zones -->
            <div class="tw-space-y-3">
              <h4 class="tw-font-medium tw-text-gray-900">Recent Zones</h4>
              <div v-for="zone in recentZones" :key="zone.id"
                   class="tw-flex tw-items-center tw-justify-between tw-p-3 tw-bg-gray-50 tw-rounded-lg">
                <div class="tw-flex tw-items-center">
                  <div class="tw-w-3 tw-h-3 tw-bg-indigo-500 tw-rounded-full tw-mr-3"></div>
                  <div>
                    <p class="tw-font-medium tw-text-gray-900">{{ zone.name }}</p>
                    <p class="tw-text-sm tw-text-gray-600">{{ zone.farmerCount }} farmers</p>
                  </div>
                </div>
                <v-btn size="small" text @click="viewZone(zone)">
                  <v-icon small>mdi-eye</v-icon>
                </v-btn>
              </div>
            </div>
          </v-card-text>
        </v-card>

        <!-- Farmer Management -->
        <v-card elevation="2" class="tw-h-fit">
          <v-card-title class="tw-flex tw-items-center tw-justify-between">
            <div class="tw-flex tw-items-center">
              <v-icon class="tw-mr-2" color="primary">mdi-account-group</v-icon>
              <span>Farmer Management</span>
            </div>
            <v-btn small text @click="$router.push({ name: 'FarmersManagement' })">
              Manage All
              <v-icon right small>mdi-arrow-right</v-icon>
            </v-btn>
          </v-card-title>
          <v-card-text>
            <!-- Add Farmer to Zone -->
            <v-expansion-panels v-model="farmerAssignmentPanel" class="tw-mb-4">
              <v-expansion-panel>
                <v-expansion-panel-header>
                  <div class="tw-flex tw-items-center">
                    <v-icon class="tw-mr-2" color="success">mdi-account-plus</v-icon>
                    <span>Add Farmer to Zone</span>
                  </div>
                </v-expansion-panel-header>
                <v-expansion-panel-content>
                  <v-form ref="farmerForm" v-model="farmerFormValid">
                    <v-select
                      v-model="farmerAssignment.zoneId"
                      :items="availableZones"
                      item-title="name"
                      item-value="id"
                      label="Select Zone"
                      :rules="[v => !!v || 'Zone is required']"
                      density="compact"
                      variant="outlined"
                      class="tw-mb-2"
                    />
                    <v-autocomplete
                      v-model="farmerAssignment.farmerId"
                      :items="availableFarmers"
                      item-title="fullName"
                      item-value="id"
                      label="Select Farmer"
                      :rules="[v => !!v || 'Farmer is required']"
                      density="compact"
                      variant="outlined"
                      class="tw-mb-4"
                    />
                    <div class="tw-flex tw-justify-end tw-space-x-2">
                      <v-btn size="small" @click="resetFarmerForm">Reset</v-btn>
                      <v-btn
                        size="small"
                        color="primary"
                        @click="addFarmerToZone"
                        :disabled="!farmerFormValid"
                        :loading="addingFarmer"
                      >
                        Add Farmer
                      </v-btn>
                    </div>
                  </v-form>
                </v-expansion-panel-content>
              </v-expansion-panel>
            </v-expansion-panels>

            <!-- Recent Farmer Activities -->
            <div class="tw-space-y-3">
              <h4 class="tw-font-medium tw-text-gray-900">Recent Farmer Activities</h4>
              <div v-for="activity in farmerActivities" :key="activity.id"
                   class="tw-flex tw-items-center tw-justify-between tw-p-3 tw-bg-gray-50 tw-rounded-lg">
                <div class="tw-flex tw-items-center">
                  <v-icon :color="activity.color" class="tw-mr-3">{{ activity.icon }}</v-icon>
                  <div>
                    <p class="tw-font-medium tw-text-gray-900">{{ activity.title }}</p>
                    <p class="tw-text-sm tw-text-gray-600">{{ activity.description }}</p>
                  </div>
                </div>
                <span class="tw-text-xs tw-text-gray-500">{{ formatTime(activity.timestamp) }}</span>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </div>

      <!-- Zone Supervisors Section -->
      <div class="tw-mt-8">
        <v-card elevation="2">
          <v-card-title class="tw-flex tw-items-center tw-justify-between">
            <div class="tw-flex tw-items-center">
              <v-icon class="tw-mr-2" color="primary">mdi-account-supervisor</v-icon>
              <span>Zone Supervisors</span>
            </div>
            <v-btn small text @click="$router.push({ name: 'ZoneSupervisorsManagement' })">
              Manage All
              <v-icon right small>mdi-arrow-right</v-icon>
            </v-btn>
          </v-card-title>
          <v-card-text>
            <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-3 tw-gap-4">
              <div v-for="supervisor in zoneSupervisors" :key="supervisor.id"
                   class="tw-p-4 tw-bg-gray-50 tw-rounded-lg">
                <div class="tw-flex tw-items-center tw-justify-between tw-mb-2">
                  <h5 class="tw-font-medium tw-text-gray-900">{{ supervisor.fullName }}</h5>
                  <v-chip small :color="supervisor.status === 'ACTIVE' ? 'success' : 'warning'">
                    {{ supervisor.status }}
                  </v-chip>
                </div>
                <p class="tw-text-sm tw-text-gray-600 tw-mb-2">{{ supervisor.email }}</p>
                <div class="tw-flex tw-items-center tw-justify-between">
                  <span class="tw-text-xs tw-text-gray-500">{{ supervisor.zones.length }} zones</span>
                  <v-btn size="small" text @click="viewSupervisor(supervisor)">
                    <v-icon small>mdi-eye</v-icon>
                  </v-btn>
                </div>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </div>
    </div>

    <!-- Loading Overlay -->
    <v-overlay v-model="loading">
      <v-progress-circular indeterminate size="64"></v-progress-circular>
    </v-overlay>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
import axios from 'axios';

export default {
  name: 'AdminDashboard',
  data() {
    return {
      loading: false,
      stats: {
        zonesManaged: 0,
        farmersAdded: 0,
        supervisorsAssigned: 0,
      },
      zoneCreationPanel: null,
      farmerAssignmentPanel: null,
      zoneFormValid: false,
      farmerFormValid: false,
      creatingZone: false,
      addingFarmer: false,
      newZone: {
        name: '',
        produceType: '',
        centerLatitude: null,
        centerLongitude: null,
        radiusKm: 5,
      },
      farmerAssignment: {
        zoneId: null,
        farmerId: null,
      },
      recentZones: [],
      availableZones: [],
      availableFarmers: [],
      farmerActivities: [],
      zoneSupervisors: [],
    };
  },
  computed: {
    ...mapGetters(['currentUser']),
  },
  async mounted() {
    await this.loadDashboardData();
  },
  methods: {
    async loadDashboardData() {
      this.loading = true;
      try {
        await Promise.all([
          this.loadStats(),
          this.loadRecentZones(),
          this.loadAvailableZones(),
          this.loadAvailableFarmers(),
          this.loadFarmerActivities(),
          this.loadZoneSupervisors(),
        ]);
      } catch (error) {
        this.$toast.error('Failed to load dashboard data');
      } finally {
        this.loading = false;
      }
    },

    async loadStats() {
      try {
        const response = await axios.get('/api/admin/dashboard/stats');
        this.stats = response.data;
      } catch (error) {
        // Set default values if API fails
        this.stats = {
          zonesManaged: 0,
          farmersAdded: 0,
          supervisorsAssigned: 0,
        };
      }
    },

    async loadRecentZones() {
      try {
        const response = await axios.get('/api/admin/zones/recent');
        this.recentZones = response.data;
      } catch (error) {
        this.recentZones = [];
      }
    },

    async loadAvailableZones() {
      try {
        const response = await axios.get('/api/admin/zones');
        this.availableZones = response.data;
      } catch (error) {
        this.availableZones = [];
      }
    },

    async loadAvailableFarmers() {
      try {
        const response = await axios.get('/api/admin/farmers/available');
        this.availableFarmers = response.data;
      } catch (error) {
        this.availableFarmers = [];
      }
    },

    async loadFarmerActivities() {
      try {
        const response = await axios.get('/api/admin/activities/recent');
        this.farmerActivities = response.data.map((activity) => ({
          ...activity,
          color: this.getActivityColor(activity.type),
          icon: this.getActivityIcon(activity.type),
        }));
      } catch (error) {
        this.farmerActivities = [];
      }
    },

    async loadZoneSupervisors() {
      try {
        const response = await axios.get('/api/admin/supervisors');
        this.zoneSupervisors = response.data;
      } catch (error) {
        this.zoneSupervisors = [];
      }
    },

    async createZone() {
      if (!this.$refs.zoneForm.validate()) return;

      this.creatingZone = true;
      try {
        // eslint-disable-next-line no-unused-vars
        const response = await axios.post('/api/admin/zones', this.newZone);
        this.$toast.success('Zone created successfully');
        this.resetZoneForm();
        await this.loadRecentZones();
        await this.loadStats();
      } catch (error) {
        this.$toast.error('Failed to create zone');
      } finally {
        this.creatingZone = false;
      }
    },

    async addFarmerToZone() {
      if (!this.$refs.farmerForm.validate()) return;

      this.addingFarmer = true;
      try {
        await axios.post('/api/admin/zones/add-farmer', this.farmerAssignment);
        this.$toast.success('Farmer added to zone successfully');
        this.resetFarmerForm();
        await this.loadFarmerActivities();
        await this.loadStats();
      } catch (error) {
        this.$toast.error('Failed to add farmer to zone');
      } finally {
        this.addingFarmer = false;
      }
    },

    resetZoneForm() {
      this.newZone = {
        name: '',
        produceType: '',
        centerLatitude: null,
        centerLongitude: null,
        radiusKm: 5,
      };
      this.zoneCreationPanel = null;
      if (this.$refs.zoneForm) {
        this.$refs.zoneForm.reset();
      }
    },

    resetFarmerForm() {
      this.farmerAssignment = {
        zoneId: null,
        farmerId: null,
      };
      this.farmerAssignmentPanel = null;
      if (this.$refs.farmerForm) {
        this.$refs.farmerForm.reset();
      }
    },

    async refreshData() {
      await this.loadDashboardData();
    },

    viewZone(zone) {
      this.$router.push({ name: 'ZoneDetail', params: { id: zone.id } });
    },

    viewSupervisor(supervisor) {
      this.$router.push({ name: 'SupervisorDetail', params: { id: supervisor.id } });
    },

    getActivityColor(type) {
      const colors = {
        ZONE_JOINED: 'success',
        PRODUCE_ADDED: 'primary',
        ORDER_PLACED: 'info',
        ZONE_LEFT: 'warning',
      };
      return colors[type] || 'grey';
    },

    getActivityIcon(type) {
      const icons = {
        ZONE_JOINED: 'mdi-account-plus',
        PRODUCE_ADDED: 'mdi-plus-circle',
        ORDER_PLACED: 'mdi-cart-plus',
        ZONE_LEFT: 'mdi-account-minus',
      };
      return icons[type] || 'mdi-information';
    },

    formatTime(timestamp) {
      return new Date(timestamp).toLocaleString();
    },
  },
};
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
}
</style>
