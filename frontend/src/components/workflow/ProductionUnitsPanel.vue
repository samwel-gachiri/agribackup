<template>
  <div class="production-units-panel">
    <!-- Header -->
    <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
      <div>
        <h3 class="tw-text-lg tw-font-semibold tw-text-gray-900">Production Units</h3>
        <p class="tw-text-sm tw-text-gray-500">Select production units for this workflow</p>
      </div>
      <v-btn small outlined color="primary" @click="$emit('navigate-to-farmers')">
        <v-icon small left>mdi-plus</v-icon>
        Add via Farmers
      </v-btn>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="tw-flex tw-justify-center tw-py-8">
      <v-progress-circular indeterminate color="primary" size="32"></v-progress-circular>
    </div>

    <!-- Empty State -->
    <div v-else-if="!productionUnits.length" class="tw-text-center tw-py-12 tw-bg-gray-50 tw-rounded-lg tw-border tw-border-dashed tw-border-gray-300">
      <v-icon size="48" color="grey lighten-2">mdi-map-marker-off</v-icon>
      <p class="tw-text-gray-500 tw-mt-4">No production units found</p>
      <p class="tw-text-gray-400 tw-text-sm tw-mt-1">Add production units through Farmers Management</p>
      <v-btn small color="primary" class="tw-mt-4" @click="$emit('navigate-to-farmers')">
        <v-icon small left>mdi-account-group</v-icon>
        Go to Farmers
      </v-btn>
    </div>

    <!-- Production Units Grid -->
    <div v-else class="tw-space-y-3">
      <!-- Summary Stats -->
      <div class="tw-grid tw-grid-cols-5 tw-gap-3 tw-mb-4">
        <div class="tw-bg-blue-50 tw-rounded-lg tw-p-3 tw-text-center">
          <p class="tw-text-2xl tw-font-bold tw-text-blue-600">{{ productionUnits.length }}</p>
          <p class="tw-text-xs tw-text-blue-600">Total Units</p>
        </div>
        <div class="tw-bg-indigo-50 tw-rounded-lg tw-p-3 tw-text-center">
          <p class="tw-text-2xl tw-font-bold tw-text-indigo-600">{{ linkedUnitsCount }}</p>
          <p class="tw-text-xs tw-text-indigo-600">Linked</p>
        </div>
        <div class="tw-bg-green-50 tw-rounded-lg tw-p-3 tw-text-center">
          <p class="tw-text-2xl tw-font-bold tw-text-green-600">{{ verifiedCount }}</p>
          <p class="tw-text-xs tw-text-green-600">Verified</p>
        </div>
        <div class="tw-bg-amber-50 tw-rounded-lg tw-p-3 tw-text-center">
          <p class="tw-text-2xl tw-font-bold tw-text-amber-600">{{ alertCount }}</p>
          <p class="tw-text-xs tw-text-amber-600">With Alerts</p>
        </div>
        <div class="tw-bg-purple-50 tw-rounded-lg tw-p-3 tw-text-center">
          <p class="tw-text-2xl tw-font-bold tw-text-purple-600">{{ totalAreaHa.toFixed(1) }}</p>
          <p class="tw-text-xs tw-text-purple-600">Total Ha</p>
        </div>
      </div>

      <!-- Units List -->
      <div class="tw-bg-white tw-rounded-lg tw-border tw-border-gray-200 tw-overflow-hidden">
        <table class="tw-w-full tw-text-sm">
          <thead class="tw-bg-gray-50 tw-border-b tw-border-gray-200">
            <tr>
              <th class="tw-text-left tw-px-4 tw-py-3 tw-font-medium tw-text-gray-600">Unit</th>
              <th class="tw-text-left tw-px-4 tw-py-3 tw-font-medium tw-text-gray-600">Farmer</th>
              <th class="tw-text-left tw-px-4 tw-py-3 tw-font-medium tw-text-gray-600">Location</th>
              <th class="tw-text-center tw-px-4 tw-py-3 tw-font-medium tw-text-gray-600">Area (Ha)</th>
              <th class="tw-text-center tw-px-4 tw-py-3 tw-font-medium tw-text-gray-600">Status</th>
              <th class="tw-text-center tw-px-4 tw-py-3 tw-font-medium tw-text-gray-600">Deforestation</th>
              <th class="tw-text-center tw-px-4 tw-py-3 tw-font-medium tw-text-gray-600">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="unit in productionUnits"
              :key="unit.id"
              class="tw-border-b tw-border-gray-100 last:tw-border-0 hover:tw-bg-gray-50 tw-transition-colors"
              :class="{ 'tw-bg-blue-50': selectedUnitId === unit.id }"
            >
              <!-- Unit Name -->
              <td class="tw-px-4 tw-py-3">
                <div class="tw-flex tw-items-center tw-gap-2">
                  <v-icon small color="green">mdi-map-marker</v-icon>
                  <span class="tw-font-medium tw-text-gray-900">{{ unit.unitName || 'Unnamed' }}</span>
                </div>
              </td>

              <!-- Farmer -->
              <td class="tw-px-4 tw-py-3">
                <span class="tw-text-gray-700">
                  {{ getFarmerName(unit) }}
                </span>
              </td>

              <!-- Location -->
              <td class="tw-px-4 tw-py-3">
                <span class="tw-text-gray-500 tw-text-xs">
                  {{ unit.administrativeRegion || getCoordinates(unit) }}
                </span>
              </td>

              <!-- Area -->
              <td class="tw-px-4 tw-py-3 tw-text-center">
                <span class="tw-text-gray-700">{{ (unit.areaHectares || 0).toFixed(2) }}</span>
              </td>

              <!-- Verification Status -->
              <td class="tw-px-4 tw-py-3 tw-text-center">
                <v-chip
                  x-small
                  :color="getVerificationColor(unit)"
                  text-color="white"
                >
                  {{ getVerificationStatus(unit) }}
                </v-chip>
              </td>

              <!-- Deforestation Status -->
              <td class="tw-px-4 tw-py-3 tw-text-center">
                <v-chip
                  x-small
                  :color="getDeforestationColor(unit)"
                  text-color="white"
                >
                  <v-icon x-small left>{{ getDeforestationIcon(unit) }}</v-icon>
                  {{ getDeforestationStatus(unit) }}
                </v-chip>
              </td>

              <!-- Action -->
              <td class="tw-px-4 tw-py-3 tw-text-center">
                <div class="tw-flex tw-items-center tw-justify-center tw-gap-1">
                  <!-- Stage 1: Link/Unlink only -->
                  <template v-if="currentStage === 'PRODUCTION_REGISTRATION'">
                    <v-btn
                      v-if="isUnitLinked(unit)"
                      x-small
                      color="success"
                      depressed
                      :loading="processingUnitId === unit.id"
                      @click.stop="unlinkUnitFromWorkflow(unit)"
                      title="Linked - click to unlink"
                    >
                      <v-icon x-small left>mdi-check</v-icon>
                      Linked
                    </v-btn>
                    <v-btn
                      v-else
                      x-small
                      color="primary"
                      :loading="processingUnitId === unit.id"
                      @click.stop="linkUnitToWorkflow(unit)"
                      title="Click to link"
                    >
                      <v-icon x-small left>mdi-link</v-icon>
                      Link
                    </v-btn>
                  </template>

                  <!-- Stage 2: Verify Geolocation (with link option for unlinked) -->
                  <template v-else-if="currentStage === 'GEOLOCATION_VERIFICATION'">
                    <v-btn
                      v-if="!isUnitLinked(unit)"
                      x-small
                      color="primary"
                      :loading="processingUnitId === unit.id"
                      @click.stop="linkUnitToWorkflow(unit)"
                      title="Link first, then verify"
                    >
                      <v-icon x-small left>mdi-link</v-icon>
                      Link
                    </v-btn>
                    <v-btn
                      v-else-if="isGeolocationVerified(unit)"
                      x-small
                      color="success"
                      depressed
                    >
                      <v-icon x-small left>mdi-check</v-icon>
                      Verified
                    </v-btn>
                    <v-btn
                      v-else
                      x-small
                      color="blue"
                      :loading="processingUnitId === unit.id"
                      @click.stop="verifyGeolocation(unit)"
                      title="Verify GPS coordinates"
                    >
                      <v-icon x-small left>mdi-map-marker-check</v-icon>
                      Verify GPS
                    </v-btn>
                  </template>

                  <!-- Stage 3: Deforestation Check (with link option for unlinked) -->
                  <template v-else-if="currentStage === 'DEFORESTATION_CHECK'">
                    <v-btn
                      v-if="!isUnitLinked(unit)"
                      x-small
                      color="primary"
                      :loading="processingUnitId === unit.id"
                      @click.stop="linkUnitToWorkflow(unit)"
                      title="Link first, then check deforestation"
                    >
                      <v-icon x-small left>mdi-link</v-icon>
                      Link
                    </v-btn>
                    <v-btn
                      v-else-if="isDeforestationChecked(unit) && isDeforestationClear(unit)"
                      x-small
                      color="success"
                      depressed
                    >
                      <v-icon x-small left>mdi-tree-outline</v-icon>
                      Clear
                    </v-btn>
                    <v-btn
                      v-else-if="isDeforestationChecked(unit) && !isDeforestationClear(unit)"
                      x-small
                      color="error"
                      depressed
                    >
                      <v-icon x-small left>mdi-alert</v-icon>
                      Alerts Found
                    </v-btn>
                    <v-btn
                      v-else
                      x-small
                      color="orange"
                      :loading="processingUnitId === unit.id"
                      @click.stop="checkDeforestation(unit)"
                      title="Run satellite deforestation check"
                    >
                      <v-icon x-small left>mdi-satellite-variant</v-icon>
                      Check
                    </v-btn>
                  </template>

                  <!-- Default: Show status -->
                  <template v-else>
                    <v-chip x-small :color="isUnitLinked(unit) ? 'success' : 'grey'" text-color="white">
                      {{ isUnitLinked(unit) ? 'Linked' : 'Not Linked' }}
                    </v-chip>
                  </template>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Documents quick access -->
      <div v-if="selectedUnit" class="tw-bg-white tw-rounded-lg tw-border tw-border-gray-200 tw-p-4 tw-mt-4">
        <div class="tw-flex tw-items-center tw-justify-between tw-mb-3">
          <h4 class="tw-text-sm tw-font-semibold tw-text-gray-700">
            Selected: {{ selectedUnit.unitName }}
          </h4>
          <v-btn x-small text color="primary" @click="viewUnitDetails">
            <v-icon x-small left>mdi-eye</v-icon>
            View Details
          </v-btn>
        </div>
        <div class="tw-grid tw-grid-cols-3 tw-gap-3 tw-text-xs">
          <div>
            <span class="tw-text-gray-500">Farmer:</span>
            <span class="tw-ml-1 tw-text-gray-800">{{ getFarmerName(selectedUnit) }}</span>
          </div>
          <div>
            <span class="tw-text-gray-500">Crops:</span>
            <span class="tw-ml-1 tw-text-gray-800">{{ selectedUnit.primaryCrops || 'Not specified' }}</span>
          </div>
          <div>
            <span class="tw-text-gray-500">Last Verified:</span>
            <span class="tw-ml-1 tw-text-gray-800">{{ formatDate(selectedUnit.lastVerifiedAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ProductionUnitsPanel',
  props: {
    workflowId: {
      type: String,
      required: true,
    },
    exporterId: {
      type: String,
      default: '',
    },
    currentStage: {
      type: String,
      default: 'PRODUCTION_REGISTRATION',
    },
  },
  data() {
    return {
      loading: false,
      productionUnits: [],
      selectedUnitId: null,
      linkedUnitIds: new Set(),
      linkedUnitsData: {}, // Store linked unit data including verification status
      processingUnitId: null, // Track which unit is being processed
    };
  },
  computed: {
    selectedUnit() {
      return this.productionUnits.find((u) => u.id === this.selectedUnitId);
    },
    verifiedCount() {
      return this.productionUnits.filter((u) => u.lastVerifiedAt).length;
    },
    alertCount() {
      return this.productionUnits.filter((u) => this.hasDeforestationAlerts(u)).length;
    },
    totalAreaHa() {
      return this.productionUnits.reduce((sum, u) => sum + (u.areaHectares || 0), 0);
    },
    linkedUnitsCount() {
      return this.productionUnits.filter((u) => this.linkedUnitIds.has(u.id)).length;
    },
  },
  watch: {
    workflowId: {
      immediate: true,
      handler() {
        this.loadProductionUnits();
        this.loadLinkedUnits();
      },
    },
  },
  methods: {
    async loadProductionUnits() {
      this.loading = true;
      try {
        const exporterId = this.exporterId || this.$store.state.auth?.user?.id;
        const response = await axios.get(`/api/production-units/exporter/${exporterId}/connected`);

        // Handle different response formats
        let units = [];
        if (Array.isArray(response.data)) {
          units = response.data;
        } else if (response.data?.content) {
          units = response.data.content;
        } else if (response.data?.data) {
          units = response.data.data;
        }

        this.productionUnits = units;
      } catch (error) {
        this.$emit('error', 'Failed to load production units');
        this.productionUnits = [];
      } finally {
        this.loading = false;
      }
    },
    async loadLinkedUnits() {
      if (!this.workflowId) return;
      try {
        const response = await axios.get(`/api/v1/supply-chain/workflows/${this.workflowId}/production-units`);
        const linked = response.data?.data || response.data || [];
        // Use productionUnitId to match against the production unit's id
        this.linkedUnitIds = new Set(linked.map((u) => u.productionUnitId));
        // Store full linked data for verification status
        this.linkedUnitsData = {};
        linked.forEach((u) => {
          this.linkedUnitsData[u.productionUnitId] = u;
        });
      } catch (error) {
        // Workflow may not have linked units yet - not an error
        this.linkedUnitIds = new Set();
        this.linkedUnitsData = {};
      }
    },
    async linkUnitToWorkflow(unit) {
      if (!this.workflowId) return;
      this.processingUnitId = unit.id;
      try {
        const response = await axios.post(`/api/v1/supply-chain/workflows/${this.workflowId}/production-units`, {
          productionUnitId: unit.id,
        });
        // Force Vue reactivity by creating a new Set
        this.linkedUnitIds = new Set([...this.linkedUnitIds, unit.id]);
        // Store the linked data
        if (response.data?.data) {
          this.linkedUnitsData = { ...this.linkedUnitsData, [unit.id]: response.data.data };
        }
        this.$emit('unit-linked', unit);
      } catch (error) {
        this.$emit('error', error.response?.data?.message || 'Failed to link production unit to workflow');
      } finally {
        this.processingUnitId = null;
      }
    },
    async unlinkUnitFromWorkflow(unit) {
      if (!this.workflowId) return;
      this.processingUnitId = unit.id;
      try {
        await axios.delete(`/api/v1/supply-chain/workflows/${this.workflowId}/production-units/${unit.id}`);
        // Force Vue reactivity by creating a new Set
        const newSet = new Set(this.linkedUnitIds);
        newSet.delete(unit.id);
        this.linkedUnitIds = newSet;
        // Remove from linked data
        const newData = { ...this.linkedUnitsData };
        delete newData[unit.id];
        this.linkedUnitsData = newData;
        this.$emit('unit-unlinked', unit);
      } catch (error) {
        this.$emit('error', 'Failed to unlink production unit from workflow');
      } finally {
        this.processingUnitId = null;
      }
    },
    async verifyGeolocation(unit) {
      if (!this.workflowId) return;
      this.processingUnitId = unit.id;
      try {
        const response = await axios.post(
          `/api/v1/supply-chain/workflows/${this.workflowId}/production-units/${unit.id}/verify-geolocation`,
        );
        if (response.data?.data) {
          this.linkedUnitsData = { ...this.linkedUnitsData, [unit.id]: response.data.data };
        }
        this.$emit('unit-verified', unit);
      } catch (error) {
        this.$emit('error', error.response?.data?.message || 'Failed to verify geolocation');
      } finally {
        this.processingUnitId = null;
      }
    },
    async checkDeforestation(unit) {
      if (!this.workflowId) return;
      this.processingUnitId = unit.id;
      try {
        const response = await axios.post(
          `/api/v1/supply-chain/workflows/${this.workflowId}/production-units/${unit.id}/check-deforestation`,
        );
        if (response.data?.data) {
          this.linkedUnitsData = { ...this.linkedUnitsData, [unit.id]: response.data.data };
        }
        this.$emit('deforestation-checked', unit, response.data?.deforestationClear);
      } catch (error) {
        this.$emit('error', error.response?.data?.message || 'Failed to check deforestation');
      } finally {
        this.processingUnitId = null;
      }
    },
    getLinkedUnitData(unitId) {
      return this.linkedUnitsData[unitId] || {};
    },
    isGeolocationVerified(unit) {
      const data = this.getLinkedUnitData(unit.id);
      return data.geolocationVerified === true;
    },
    isDeforestationChecked(unit) {
      const data = this.getLinkedUnitData(unit.id);
      return data.deforestationChecked === true;
    },
    isDeforestationClear(unit) {
      const data = this.getLinkedUnitData(unit.id);
      return data.deforestationClear === true;
    },
    isUnitLinked(unit) {
      return this.linkedUnitIds.has(unit.id);
    },
    getFarmerName(unit) {
      if (unit.farmer) {
        return `${unit.farmer.firstName || ''} ${unit.farmer.lastName || ''}`.trim() || 'Unknown';
      }
      return unit.farmerName || 'Unknown';
    },
    getCoordinates(unit) {
      if (unit.wgs84Coordinates) {
        const coords = unit.wgs84Coordinates;
        if (typeof coords === 'string') {
          return `${coords.substring(0, 30)}...`;
        }
        return `${coords.latitude?.toFixed(4)}, ${coords.longitude?.toFixed(4)}`;
      }
      return 'No location';
    },
    getVerificationStatus(unit) {
      return unit.lastVerifiedAt ? 'Verified' : 'Pending';
    },
    getVerificationColor(unit) {
      return unit.lastVerifiedAt ? 'success' : 'warning';
    },
    hasDeforestationAlerts(unit) {
      return unit.deforestationAlerts && unit.deforestationAlerts.length > 0;
    },
    getDeforestationStatus(unit) {
      if (this.hasDeforestationAlerts(unit)) {
        return `${unit.deforestationAlerts.length} Alert(s)`;
      }
      return 'Clear';
    },
    getDeforestationColor(unit) {
      if (this.hasDeforestationAlerts(unit)) {
        return 'error';
      }
      return 'success';
    },
    getDeforestationIcon(unit) {
      return this.hasDeforestationAlerts(unit) ? 'mdi-alert' : 'mdi-check-circle';
    },
    selectUnit(unit) {
      if (this.selectedUnitId === unit.id) {
        this.selectedUnitId = null;
        this.$emit('unit-selected', null);
      } else {
        this.selectedUnitId = unit.id;
        this.$emit('unit-selected', unit);
      }
    },
    viewUnitDetails() {
      // Navigate to farmer management or show details dialog
      this.$emit('navigate-to-farmers');
    },
    formatDate(dateString) {
      if (!dateString) return 'Never';
      const date = new Date(dateString);
      if (Number.isNaN(date.getTime())) return 'Never';
      return new Intl.DateTimeFormat(undefined, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      }).format(date);
    },
  },
};
</script>

<style scoped>
.production-units-panel {
  /* No extra styles needed with Tailwind */
}
</style>
