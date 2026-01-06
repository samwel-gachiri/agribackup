<template>
  <!-- <Default> -->
    <div class="tw-min-h-screen tw-bg-gray-50 tw-p-6">
      <!-- Header -->
      <div class="tw-flex tw-items-center tw-justify-between tw-mb-8">
        <div>
          <h1 class="tw-text-2xl tw-font-bold tw-text-gray-900">EUDR Compliance Dashboard</h1>
          <p class="tw-text-gray-500 tw-mt-1">Track your supply chain from farm to export</p>
        </div>
        <div class="tw-flex tw-gap-3">
          <v-btn color="primary" depressed @click="createNewShipment">
            <v-icon left>mdi-plus</v-icon>
            New Shipment
          </v-btn>
        </div>
      </div>

      <!-- Quick Stats Cards -->
      <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-4 tw-gap-6 tw-mb-8">
        <!-- Farms Card -->
        <div class="tw-bg-white tw-rounded-xl tw-p-6 tw-shadow-sm tw-border tw-border-gray-100">
          <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
            <div class="tw-w-12 tw-h-12 tw-bg-green-100 tw-rounded-lg tw-flex tw-items-center tw-justify-center">
              <v-icon color="green">mdi-map-marker</v-icon>
            </div>
            <v-chip small :color="farmsNeedingAttention > 0 ? 'warning' : 'success'" text-color="white">
              {{ farmsNeedingAttention > 0 ? farmsNeedingAttention + ' need attention' : 'All verified' }}
            </v-chip>
          </div>
          <p class="tw-text-3xl tw-font-bold tw-text-gray-900">{{ totalFarms }}</p>
          <p class="tw-text-sm tw-text-gray-500">Registered Farms</p>
          <div class="tw-mt-4 tw-pt-4 tw-border-t tw-border-gray-100">
            <div class="tw-flex tw-justify-between tw-text-sm">
              <span class="tw-text-gray-500">Verified</span>
              <span class="tw-font-medium tw-text-green-600">{{ verifiedFarms }}</span>
            </div>
            <div class="tw-flex tw-justify-between tw-text-sm tw-mt-1">
              <span class="tw-text-gray-500">Pending</span>
              <span class="tw-font-medium tw-text-amber-600">{{ pendingFarms }}</span>
            </div>
          </div>
        </div>

        <!-- Suppliers Card -->
        <div class="tw-bg-white tw-rounded-xl tw-p-6 tw-shadow-sm tw-border tw-border-gray-100">
          <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
            <div class="tw-w-12 tw-h-12 tw-bg-blue-100 tw-rounded-lg tw-flex tw-items-center tw-justify-center">
              <v-icon color="blue">mdi-account-group</v-icon>
            </div>
            <v-btn x-small text color="primary" @click="goToSuppliers">Manage</v-btn>
          </div>
          <p class="tw-text-3xl tw-font-bold tw-text-gray-900">{{ totalSuppliers }}</p>
          <p class="tw-text-sm tw-text-gray-500">Active Suppliers</p>
          <div class="tw-mt-4 tw-pt-4 tw-border-t tw-border-gray-100">
            <div class="tw-flex tw-justify-between tw-text-sm">
              <span class="tw-text-gray-500">Farmers</span>
              <span class="tw-font-medium">{{ suppliersByType.FARMER || 0 }}</span>
            </div>
            <div class="tw-flex tw-justify-between tw-text-sm tw-mt-1">
              <span class="tw-text-gray-500">Cooperatives</span>
              <span class="tw-font-medium">{{ suppliersByType.COOPERATIVE || 0 }}</span>
            </div>
          </div>
        </div>

        <!-- Sourced Produce Card -->
        <div class="tw-bg-white tw-rounded-xl tw-p-6 tw-shadow-sm tw-border tw-border-gray-100">
          <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
            <div class="tw-w-12 tw-h-12 tw-bg-amber-100 tw-rounded-lg tw-flex tw-items-center tw-justify-center">
              <v-icon color="amber darken-2">mdi-package-variant</v-icon>
            </div>
            <span class="tw-text-xs tw-text-gray-400">This Season</span>
          </div>
          <p class="tw-text-3xl tw-font-bold tw-text-gray-900">{{ formatWeight(totalSourcedKg) }}</p>
          <p class="tw-text-sm tw-text-gray-500">Total Sourced</p>
          <div class="tw-mt-4 tw-pt-4 tw-border-t tw-border-gray-100">
            <div class="tw-flex tw-justify-between tw-text-sm">
              <span class="tw-text-gray-500">Traceable</span>
              <span class="tw-font-medium tw-text-green-600">{{ formatWeight(traceableKg) }}</span>
            </div>
            <div class="tw-flex tw-justify-between tw-text-sm tw-mt-1">
              <span class="tw-text-gray-500">Shipped</span>
              <span class="tw-font-medium tw-text-blue-600">{{ formatWeight(shippedKg) }}</span>
            </div>
          </div>
        </div>

        <!-- DDS Ready Card -->
        <div class="tw-bg-white tw-rounded-xl tw-p-6 tw-shadow-sm tw-border tw-border-gray-100">
          <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
            <div class="tw-w-12 tw-h-12 tw-bg-purple-100 tw-rounded-lg tw-flex tw-items-center tw-justify-center">
              <v-icon color="purple">mdi-file-certificate</v-icon>
            </div>
            <v-chip small :color="shipmentsReady > 0 ? 'success' : 'grey'" text-color="white">
              {{ shipmentsReady }} ready
            </v-chip>
          </div>
          <p class="tw-text-3xl tw-font-bold tw-text-gray-900">{{ totalShipments }}</p>
          <p class="tw-text-sm tw-text-gray-500">Active Shipments</p>
          <div class="tw-mt-4 tw-pt-4 tw-border-t tw-border-gray-100">
            <div class="tw-flex tw-justify-between tw-text-sm">
              <span class="tw-text-gray-500">DDS Generated</span>
              <span class="tw-font-medium tw-text-green-600">{{ ddsGenerated }}</span>
            </div>
            <div class="tw-flex tw-justify-between tw-text-sm tw-mt-1">
              <span class="tw-text-gray-500">In Progress</span>
              <span class="tw-font-medium tw-text-amber-600">{{ shipmentsInProgress }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div class="tw-grid tw-grid-cols-1 lg:tw-grid-cols-3 tw-gap-6">
        <!-- Active Shipments (2/3 width) -->
        <div class="lg:tw-col-span-2">
          <div class="tw-bg-white tw-rounded-xl tw-shadow-sm tw-border tw-border-gray-100">
            <div class="tw-flex tw-items-center tw-justify-between tw-p-6 tw-border-b tw-border-gray-100">
              <h2 class="tw-text-lg tw-font-semibold tw-text-gray-900">Active Shipments</h2>
              <v-btn small text color="primary" @click="viewAllShipments">View All</v-btn>
            </div>

            <div v-if="loading" class="tw-p-8 tw-text-center">
              <v-progress-circular indeterminate color="primary"></v-progress-circular>
            </div>

            <div v-else-if="!shipments.length" class="tw-p-12 tw-text-center">
              <v-icon size="64" color="grey lighten-2">mdi-package-variant</v-icon>
              <p class="tw-text-gray-500 tw-mt-4">No active shipments</p>
              <v-btn small color="primary" class="tw-mt-4" @click="createNewShipment">
                <v-icon left small>mdi-plus</v-icon>
                Create First Shipment
              </v-btn>
            </div>

            <div v-else class="tw-divide-y tw-divide-gray-100">
              <div
                v-for="shipment in shipments.slice(0, 5)"
                :key="shipment.id"
                class="tw-p-4 hover:tw-bg-gray-50 tw-cursor-pointer tw-transition-colors"
                @click="openShipment(shipment)"
              >
                <div class="tw-flex tw-items-start tw-justify-between">
                  <div class="tw-flex-1">
                    <div class="tw-flex tw-items-center tw-gap-2 tw-mb-1">
                      <span class="tw-font-semibold tw-text-gray-900">{{ shipment.workflowName }}</span>
                      <v-chip x-small :color="getStatusColor(shipment)" text-color="white">
                        {{ getStatusLabel(shipment) }}
                      </v-chip>
                    </div>
                    <p class="tw-text-sm tw-text-gray-500">
                      {{ shipment.produceType }} • {{ formatWeight(shipment.totalCollected) }} from {{ shipment.farmCount || 0 }} farms
                    </p>
                  </div>
                  <div class="tw-text-right">
                    <div v-if="isDDSReady(shipment)" class="tw-mb-2">
                      <v-btn
                        small
                        color="success"
                        depressed
                        @click.stop="generateDDS(shipment)"
                      >
                        <v-icon left small>mdi-file-download</v-icon>
                        Generate DDS
                      </v-btn>
                    </div>
                    <p class="tw-text-xs tw-text-gray-400">{{ formatDate(shipment.createdAt) }}</p>
                  </div>
                </div>

                <!-- Progress Bar -->
                <div class="tw-mt-3">
                  <div class="tw-flex tw-items-center tw-gap-2">
                    <div class="tw-flex-1 tw-bg-gray-100 tw-rounded-full tw-h-2">
                      <div
                        class="tw-bg-green-500 tw-rounded-full tw-h-2 tw-transition-all"
                        :style="{ width: getProgress(shipment) + '%' }"
                      ></div>
                    </div>
                    <span class="tw-text-xs tw-text-gray-500">{{ getProgress(shipment) }}%</span>
                  </div>
                  <p class="tw-text-xs tw-text-gray-500 tw-mt-1">{{ getCurrentStep(shipment) }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Quick Actions & Recent Activity (1/3 width) -->
        <div class="tw-space-y-6">
          <!-- Quick Actions -->
          <div class="tw-bg-white tw-rounded-xl tw-shadow-sm tw-border tw-border-gray-100 tw-p-6">
            <h2 class="tw-text-lg tw-font-semibold tw-text-gray-900 tw-mb-4">Quick Actions</h2>
            <div class="tw-space-y-3">
              <v-btn block outlined color="primary" @click="addFarm">
                <v-icon left>mdi-map-marker-plus</v-icon>
                Register New Farm
              </v-btn>
              <v-btn block outlined color="blue" @click="runDeforestationCheck">
                <v-icon left>mdi-satellite-variant</v-icon>
                Run Deforestation Check
              </v-btn>
              <v-btn block outlined color="amber darken-2" @click="recordPurchase">
                <v-icon left>mdi-cart-plus</v-icon>
                Record Purchase
              </v-btn>
              <v-btn block outlined color="purple" @click="goToReports">
                <v-icon left>mdi-file-document</v-icon>
                View Reports
              </v-btn>
            </div>
          </div>

          <!-- Compliance Alerts -->
          <div class="tw-bg-white tw-rounded-xl tw-shadow-sm tw-border tw-border-gray-100 tw-p-6">
            <h2 class="tw-text-lg tw-font-semibold tw-text-gray-900 tw-mb-4">Alerts</h2>

            <div v-if="!alerts.length" class="tw-text-center tw-py-4">
              <v-icon color="green" size="32">mdi-check-circle</v-icon>
              <p class="tw-text-sm tw-text-gray-500 tw-mt-2">No compliance alerts</p>
            </div>

            <div v-else class="tw-space-y-3">
              <div
                v-for="alert in alerts.slice(0, 5)"
                :key="alert.id"
                class="tw-flex tw-items-start tw-gap-3 tw-p-3 tw-rounded-lg"
                :class="getAlertClass(alert)"
              >
                <v-icon small :color="getAlertColor(alert)">{{ getAlertIcon(alert) }}</v-icon>
                <div class="tw-flex-1">
                  <p class="tw-text-sm tw-font-medium tw-text-gray-900">{{ alert.title }}</p>
                  <p class="tw-text-xs tw-text-gray-500">{{ alert.message }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- What is EUDR? Info Box -->
          <div class="tw-bg-gradient-to-br tw-from-green-50 tw-to-emerald-50 tw-rounded-xl tw-p-6 tw-border tw-border-green-100">
            <div class="tw-flex tw-items-center tw-gap-2 tw-mb-3">
              <v-icon color="green">mdi-information</v-icon>
              <h3 class="tw-font-semibold tw-text-green-800">EUDR Compliance</h3>
            </div>
            <p class="tw-text-sm tw-text-green-700 tw-leading-relaxed">
              The EU Deforestation Regulation requires proof that your products are
              <strong>deforestation-free</strong> and <strong>traceable</strong> to their source.
            </p>
            <div class="tw-mt-4 tw-space-y-2 tw-text-sm tw-text-green-700">
              <div class="tw-flex tw-items-center tw-gap-2">
                <v-icon x-small color="green">mdi-check</v-icon>
                <span>Register all source farms</span>
              </div>
              <div class="tw-flex tw-items-center tw-gap-2">
                <v-icon x-small color="green">mdi-check</v-icon>
                <span>Verify deforestation-free status</span>
              </div>
              <div class="tw-flex tw-items-center tw-gap-2">
                <v-icon x-small color="green">mdi-check</v-icon>
                <span>Generate Due Diligence Statement</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  <!-- </Default> -->
</template>

<script>
import axios from 'axios';
// import Default from '@/components/layout/Default.vue';

export default {
  name: 'EudrDashboard',
  // components: { Default },
  data() {
    return {
      loading: false,
      // Stats
      totalFarms: 0,
      verifiedFarms: 0,
      pendingFarms: 0,
      farmsNeedingAttention: 0,
      totalSuppliers: 0,
      suppliersByType: {},
      totalSourcedKg: 0,
      traceableKg: 0,
      shippedKg: 0,
      totalShipments: 0,
      shipmentsReady: 0,
      shipmentsInProgress: 0,
      ddsGenerated: 0,
      // Data
      shipments: [],
      alerts: [],
    };
  },
  computed: {
    exporterId() {
      return this.$store.state.auth?.user?.id || '';
    },
  },
  mounted() {
    this.loadDashboardData();
  },
  methods: {
    async loadDashboardData() {
      this.loading = true;
      try {
        await Promise.all([
          this.loadFarmStats(),
          this.loadSupplierStats(),
          this.loadShipments(),
          this.loadAlerts(),
        ]);
      } catch (error) {
        this.$toast.error('Failed to load dashboard:', error.message);
      } finally {
        this.loading = false;
      }
    },

    async loadFarmStats() {
      try {
        const response = await axios.get(`/api/production-units/exporter/${this.exporterId}/connected`);
        const farms = Array.isArray(response.data) ? response.data : (response.data?.content || []);

        this.totalFarms = farms.length;
        this.verifiedFarms = farms.filter((f) => f.lastVerifiedAt).length;
        this.pendingFarms = farms.filter((f) => !f.lastVerifiedAt).length;
        this.farmsNeedingAttention = farms.filter((f) => !f.lastVerifiedAt || (f.deforestationAlerts && f.deforestationAlerts.length > 0)).length;
      } catch (error) {
        this.$toast.error('Failed to load farm stats:', error.message);
      }
    },

    async loadSupplierStats() {
      try {
        const response = await axios.get(`/api/v1/supply-chain/exporter/${this.exporterId}/connected-suppliers`);
        const suppliers = response.data?.data || response.data?.content || response.data || [];

        this.totalSuppliers = suppliers.length;
        this.suppliersByType = suppliers.reduce((acc, s) => {
          const type = s.supplierType || 'OTHER';
          acc[type] = (acc[type] || 0) + 1;
          return acc;
        }, {});
      } catch (error) {
        this.$toast.error('Failed to load supplier stats:', error.message);
      }
    },

    async loadShipments() {
      try {
        const response = await axios.get(`/api/v1/supply-chain/workflows/exporter/${this.exporterId}`);
        const workflows = response.data?.content || response.data || [];

        this.shipments = workflows.map((w) => ({
          ...w,
          farmCount: w.collectionEventCount || 0,
        }));

        this.totalShipments = workflows.length;
        this.shipmentsReady = workflows.filter((w) => this.isDDSReady(w)).length;
        this.shipmentsInProgress = workflows.filter((w) => w.status === 'IN_PROGRESS').length;
        this.ddsGenerated = workflows.filter((w) => w.certificateStatus && w.certificateStatus !== 'NOT_CREATED').length;

        this.totalSourcedKg = workflows.reduce((sum, w) => sum + (w.totalCollected || 0), 0);
        this.traceableKg = this.totalSourcedKg; // All sourced is traceable in this system
        this.shippedKg = workflows.reduce((sum, w) => sum + (w.totalShipped || 0), 0);
      } catch (error) {
        this.$toast.error('Failed to load shipments:', error.message);
      }
    },

    async loadAlerts() {
      // Generate alerts based on data
      this.alerts = [];

      if (this.farmsNeedingAttention > 0) {
        this.alerts.push({
          id: 'farms-attention',
          type: 'warning',
          title: `${this.farmsNeedingAttention} farms need attention`,
          message: 'Some farms need verification or have deforestation alerts',
        });
      }

      if (this.shipmentsReady > 0) {
        this.alerts.push({
          id: 'dds-ready',
          type: 'success',
          title: `${this.shipmentsReady} shipments ready for DDS`,
          message: 'You can generate Due Diligence Statements',
        });
      }
    },

    // Helpers
    formatWeight(kg) {
      if (!kg) return '0 kg';
      if (kg >= 1000) {
        return `${(kg / 1000).toFixed(1)} t`;
      }
      return `${kg.toFixed(0)} kg`;
    },

    formatDate(dateStr) {
      if (!dateStr) return '';
      return new Date(dateStr).toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
      });
    },

    isDDSReady(shipment) {
      // Ready if has collections and passed deforestation checks
      return shipment.totalCollected > 0
             && shipment.status === 'IN_PROGRESS'
             && (!shipment.certificateStatus || shipment.certificateStatus === 'NOT_CREATED');
    },

    getStatusColor(shipment) {
      if (shipment.certificateStatus === 'COMPLIANT') return 'success';
      if (shipment.status === 'COMPLETED') return 'success';
      if (shipment.status === 'IN_PROGRESS') return 'primary';
      return 'grey';
    },

    getStatusLabel(shipment) {
      if (shipment.certificateStatus === 'COMPLIANT') return 'DDS Ready';
      if (shipment.status === 'COMPLETED') return 'Completed';
      if (shipment.status === 'IN_PROGRESS') return 'In Progress';
      return shipment.status;
    },

    getProgress(shipment) {
      // Simplified progress based on what's done
      let progress = 0;
      if (shipment.collectionEventCount > 0) progress += 40;
      if (shipment.totalCollected > 0) progress += 20;
      if (shipment.certificateStatus && shipment.certificateStatus !== 'NOT_CREATED') progress += 30;
      if (shipment.status === 'COMPLETED') progress = 100;
      return Math.min(progress, 100);
    },

    getCurrentStep(shipment) {
      if (shipment.status === 'COMPLETED') return 'Completed';
      if (shipment.certificateStatus === 'COMPLIANT') return 'Ready to generate DDS';
      if (shipment.totalShipped > 0) return 'Shipping in progress';
      if (shipment.totalCollected > 0) return 'Sourcing complete, ready for verification';
      return 'Register farms and record purchases';
    },

    getAlertClass(alert) {
      const classes = {
        warning: 'tw-bg-amber-50',
        error: 'tw-bg-red-50',
        success: 'tw-bg-green-50',
        info: 'tw-bg-blue-50',
      };
      return classes[alert.type] || 'tw-bg-gray-50';
    },

    getAlertColor(alert) {
      const colors = {
        warning: 'amber',
        error: 'error',
        success: 'success',
        info: 'primary',
      };
      return colors[alert.type] || 'grey';
    },

    getAlertIcon(alert) {
      const icons = {
        warning: 'mdi-alert',
        error: 'mdi-alert-circle',
        success: 'mdi-check-circle',
        info: 'mdi-information',
      };
      return icons[alert.type] || 'mdi-information';
    },

    // Actions
    createNewShipment() {
      // eslint-disable-next-line sonarjs/no-duplicate-string
      this.$router.push('/exporter/supply-chain-workflow');
    },

    openShipment(shipment) {
      this.$router.push(`/exporter/supply-chain-workflow?id=${shipment.id}`);
    },

    viewAllShipments() {
      this.$router.push('/exporter/supply-chain-workflow');
    },

    addFarm() {
      this.$router.push('/exporter/farmers');
    },

    runDeforestationCheck() {
      this.$router.push('/exporter/deforestation-check');
    },

    recordPurchase() {
      // Go to workflow page to record purchase
      this.$router.push('/exporter/supply-chain-workflow');
    },

    goToSuppliers() {
      this.$router.push('/exporter/supplier-management');
    },

    goToReports() {
      this.$router.push('/exporter/reports');
    },

    async generateDDS(shipment) {
      try {
        const response = await axios.post(`/api/v1/supply-chain/workflows/${shipment.id}/issue-certificate`);
        if (response.data?.success) {
          this.$emit('snackbar', { message: 'DDS generated successfully!', color: 'success' });
          this.loadShipments();
        } else {
          this.$emit('snackbar', { message: response.data?.message || 'Failed to generate DDS', color: 'error' });
        }
      } catch (error) {
        this.$emit('snackbar', { message: 'Failed to generate DDS', color: 'error' });
      }
    },
  },
};
</script>

<style scoped>
/* Component styles handled by Tailwind */
</style>
