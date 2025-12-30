<template>
  <v-card class="tw-h-screen tw-flex tw-flex-col tw-overflow-hidden">
    <!-- Compact Header -->
    <div class="tw-bg-green-600 tw-px-4 tw-py-2 tw-flex tw-items-center tw-justify-between tw-flex-shrink-0">
      <div class="tw-flex tw-items-center tw-gap-2 tw-text-white">
        <v-icon color="white" small>mdi-map-marker-plus</v-icon>
        <span class="tw-font-medium">{{ displayName }}'s Production Units</span>
        <v-chip v-if="productionUnits.length > 0" x-small color="white" text-color="green" class="tw-ml-2">
          {{ productionUnits.length }} units
        </v-chip>
      </div>
      <v-btn icon small @click="close" color="white">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </div>

    <!-- Full Map Area -->
    <div class="tw-flex-1 tw-relative tw-min-h-0" style="height: calc(100vh - 48px);">
      <!-- Loading Overlay -->
      <div v-if="loading" class="tw-absolute tw-inset-0 tw-bg-white tw-bg-opacity-80 tw-z-40 tw-flex tw-items-center tw-justify-center">
        <div class="tw-text-center">
          <v-progress-circular indeterminate color="green" size="48" />
          <div class="tw-text-sm tw-text-gray-600 tw-mt-3">Loading production units...</div>
        </div>
      </div>

      <ProductionUnitDrawer
        v-if="dialog"
        ref="drawer"
        class="tw-absolute tw-inset-0"
        :initial-location="farmerLocation"
        :existing-units="existingUnitsWithGeometry"
        :show-help-on-start="isFirstTimeUser"
        @unit-drawn="handleUnitDrawn"
        @save-unit="openSaveUnitDialog"
      />

      <!-- Floating Units List Toggle Button (Bottom Left) -->
      <v-btn
        v-if="productionUnits.length > 0 && !loading"
        fab
        small
        color="white"
        class="tw-absolute tw-bottom-4 tw-left-4 tw-z-30 tw-shadow-lg"
        @click="showUnitsList = !showUnitsList"
      >
        <v-badge :content="productionUnits.length" color="green" overlap>
          <v-icon>mdi-format-list-bulleted</v-icon>
        </v-badge>
      </v-btn>

      <!-- Sliding Units Panel (Left Side) -->
      <transition name="slide-left">
        <div
          v-if="showUnitsList"
          class="tw-absolute tw-top-0 tw-left-0 tw-bottom-0 tw-w-64 tw-bg-white tw-shadow-xl tw-z-30 tw-flex tw-flex-col"
        >
          <div class="tw-p-3 tw-bg-green-50 tw-border-b tw-flex tw-items-center tw-justify-between">
            <div class="tw-flex tw-items-center tw-gap-2">
              <v-icon small color="green">mdi-map-marker-multiple</v-icon>
              <span class="tw-font-semibold tw-text-green-800 tw-text-sm">Units ({{ productionUnits.length }})</span>
            </div>
            <v-btn icon x-small @click="showUnitsList = false">
              <v-icon small>mdi-close</v-icon>
            </v-btn>
          </div>
          <div class="tw-flex-1 tw-overflow-auto">
            <div
              v-for="(unit, index) in productionUnits"
              :key="unit.id || index"
              class="tw-flex tw-items-center tw-gap-2 tw-px-3 tw-py-2 tw-border-b tw-border-gray-100 hover:tw-bg-gray-50 tw-cursor-pointer"
              @click="zoomToUnit(unit)"
            >
              <div
                class="tw-w-3 tw-h-3 tw-rounded-full tw-flex-shrink-0"
                :style="{ backgroundColor: getUnitColor(index) }"
              ></div>
              <div class="tw-flex-1 tw-min-w-0">
                <div class="tw-font-medium tw-text-sm tw-text-gray-800 tw-truncate">{{ unit.unitName || `Unit ${index + 1}` }}</div>
                <div class="tw-text-xs tw-text-gray-500">{{ (unit.areaHectares || 0).toFixed(2) }} ha</div>
              </div>
              <v-btn icon x-small color="red" @click.stop="deleteProductionUnit(unit, index)">
                <v-icon x-small>mdi-delete</v-icon>
              </v-btn>
            </div>
          </div>
        </div>
      </transition>
    </div>

    <!-- Save Unit Dialog (appears after drawing polygon) -->
    <v-dialog v-model="showSaveUnitDialog" max-width="400" persistent>
      <v-card>
        <v-card-title class="tw-flex tw-items-center tw-gap-2">
          <v-icon color="green">mdi-map-marker-check</v-icon>
          Save Production Unit
        </v-card-title>
        <v-card-text>
          <v-alert v-if="saveError" type="error" dense dismissible @input="saveError = null" class="tw-mb-4">
            {{ saveError }}
          </v-alert>

          <div class="tw-mb-4 tw-p-3 tw-bg-green-50 tw-rounded-lg">
            <div class="tw-flex tw-justify-between tw-text-sm">
              <span class="tw-text-gray-600">Area:</span>
              <span class="tw-font-semibold tw-text-green-700">{{ pendingUnitArea?.toFixed(2) }} hectares</span>
            </div>
          </div>

          <v-form ref="saveUnitForm" v-model="saveFormValid">
            <v-text-field
              v-model="pendingUnit.unitName"
              label="Unit Name *"
              :rules="[v => !!v || 'Name is required']"
              outlined
              dense
              placeholder="e.g., North Plot"
              autofocus
            />
            <v-text-field
              v-model="pendingUnit.administrativeRegion"
              label="Region (Optional)"
              outlined
              dense
              placeholder="e.g., Kiambu County"
            />

            <!-- Deforestation Check -->
            <v-btn
              color="warning"
              outlined
              block
              small
              :loading="checkingDeforestation"
              @click="checkPendingUnitDeforestation"
              class="tw-mb-3"
            >
              <v-icon left small>mdi-shield-search</v-icon>
              Check Deforestation Status
            </v-btn>

            <v-alert
              v-if="deforestationCheckResult"
              :type="deforestationAlertType"
              dense
              class="tw-text-sm"
            >
              <strong>{{ deforestationCheckResult.riskLevel }} Risk</strong>
              <span v-if="deforestationCheckResult.hasAlerts">
                - {{ deforestationCheckResult.totalAlerts }} alert(s)
              </span>
            </v-alert>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-btn text @click="cancelSaveUnit">Cancel</v-btn>
          <v-spacer></v-spacer>
          <v-btn
            color="success"
            :loading="savingUnit"
            :disabled="!saveFormValid"
            @click="saveUnitToBackend"
          >
            <v-icon left small>mdi-content-save</v-icon>
            Save Unit
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteConfirmDialog" max-width="400">
      <v-card>
        <v-card-title>Delete Production Unit?</v-card-title>
        <v-card-text>
          Are you sure you want to delete "{{ deleteConfirmUnit?.unitName }}"? This action cannot be undone.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="deleteConfirmDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="confirmDelete">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script>
import axios from 'axios';
import ProductionUnitDrawer from './ProductionUnitDrawer.vue';

export default {
  name: 'EditFarmerDetails',
  components: {
    ProductionUnitDrawer,
  },
  props: {
    value: Boolean,
    farmer: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      dialog: false,
      loading: false,
      productionUnits: [],
      // New minimalist UI state
      showUnitsList: false,
      showSaveUnitDialog: false,
      pendingUnit: {
        unitName: '',
        administrativeRegion: '',
      },
      pendingUnitGeometry: null,
      pendingUnitArea: 0,
      saveFormValid: false,
      savingUnit: false,
      saveError: null,
      // Delete state
      deleteConfirmDialog: false,
      deleteConfirmUnit: null,
      deleteConfirmIndex: null,
      deleting: false,
      // Deforestation check
      checkingDeforestation: false,
      deforestationCheckResult: null,
      // First time help
      isFirstTimeUser: !localStorage.getItem('editFarmerHelpSeen'),
    };
  },
  computed: {
    displayName() {
      return this.farmer?.farmerName || this.farmer?.name || 'Farmer';
    },
    farmerLocation() {
      return {
        lat: this.farmer?.latitude || this.farmer?.lat || -1.2921,
        lng: this.farmer?.longitude || this.farmer?.lng || 36.8219,
      };
    },
    existingUnitsWithGeometry() {
      // Transform production units to include parsed geometry for the map drawer
      return this.productionUnits.map((unit) => {
        let geometry = null;
        try {
          if (unit.parcelGeometryGeoJson) {
            const geoJson = typeof unit.parcelGeometryGeoJson === 'string'
              ? JSON.parse(unit.parcelGeometryGeoJson)
              : unit.parcelGeometryGeoJson;
            geometry = {
              type: 'polygon',
              coordinates: geoJson.coordinates,
            };
          } else if (unit.parcelGeometry && unit.parcelGeometry.rings) {
            geometry = {
              type: 'polygon',
              coordinates: unit.parcelGeometry.rings,
            };
          }
        } catch (e) {
          this.$toast.error('Failed to parse geometry for unit:', e.message);
        }
        return {
          id: unit.id,
          name: unit.unitName,
          area: unit.areaHectares,
          geometry,
        };
      }).filter((unit) => unit.geometry !== null);
    },
    deforestationAlertType() {
      if (!this.deforestationCheckResult) return 'info';
      const riskLevel = this.deforestationCheckResult.riskLevel;
      if (riskLevel === 'HIGH') return 'error';
      if (riskLevel === 'MEDIUM') return 'warning';
      if (riskLevel === 'LOW') return 'info';
      if (riskLevel === 'NONE') return 'success';
      return 'error';
    },
  },
  watch: {
    value(val) {
      this.dialog = val;
      if (val) {
        this.fetchProductionUnits();
      }
    },
    dialog(val) {
      this.$emit('input', val);
    },
  },
  mounted() {
    // Sync local dialog state with value prop on mount
    this.dialog = this.value;
    if (this.value) {
      this.fetchProductionUnits();
    }
  },
  methods: {
    async fetchProductionUnits() {
      if (!this.farmer || !this.farmer.id) {
        this.$toast.error('No farmer ID available');
        return;
      }

      this.loading = true;
      try {
        const response = await axios.get(`/api/production-units/farmer/${this.farmer.id}`);
        this.productionUnits = response.data.data || [];
      } catch (error) {
        this.$toast.error('Failed to load production units');
        this.productionUnits = [];
      } finally {
        this.loading = false;
      }
    },

    handleUnitDrawn(data) {
      // Just track the drawn geometry
      this.pendingUnitGeometry = data.geometry;
      this.pendingUnitArea = data.area;
    },

    openSaveUnitDialog(data) {
      this.pendingUnitGeometry = data.geometry;
      this.pendingUnitArea = data.area;
      this.pendingUnit = {
        unitName: '',
        administrativeRegion: '',
      };
      this.saveError = null;
      this.deforestationCheckResult = null;
      this.showSaveUnitDialog = true;

      // Mark help as seen after first drawing
      if (this.isFirstTimeUser) {
        localStorage.setItem('editFarmerHelpSeen', 'true');
        this.isFirstTimeUser = false;
      }
    },

    cancelSaveUnit() {
      this.showSaveUnitDialog = false;
      this.pendingUnit = { unitName: '', administrativeRegion: '' };
      this.pendingUnitGeometry = null;
      this.pendingUnitArea = 0;
      this.saveError = null;
      this.deforestationCheckResult = null;

      if (this.$refs.drawer) {
        this.$refs.drawer.clearDrawing();
      }
    },

    async checkPendingUnitDeforestation() {
      if (!this.pendingUnitGeometry) {
        this.saveError = 'No polygon geometry available';
        return;
      }

      this.checkingDeforestation = true;
      this.saveError = null;
      this.deforestationCheckResult = null;

      try {
        const roundCoordinate = (coord) => coord.map((val) => parseFloat(val.toFixed(8)));
        const rings = this.pendingUnitGeometry.rings.map((ring) => {
          const roundedRing = ring.map(roundCoordinate);
          const coords = [...roundedRing];
          const first = coords[0];
          const last = coords[coords.length - 1];
          if (first[0] !== last[0] || first[1] !== last[1]) {
            coords.push([...first]);
          }
          return coords;
        });

        const geoJsonPolygon = JSON.stringify({
          type: 'Polygon',
          coordinates: rings,
        });

        const response = await axios.post('/api/v1/deforestation-alerts/check-geometry', {
          geoJsonPolygon,
        });

        this.deforestationCheckResult = response.data;

        if (!this.deforestationCheckResult.hasAlerts) {
          this.$toast.success('No deforestation alerts found');
        }
      } catch (error) {
        this.saveError = 'Failed to check deforestation status';
        this.deforestationCheckResult = {
          riskLevel: 'ERROR',
          hasAlerts: false,
          totalAlerts: 0,
        };
      } finally {
        this.checkingDeforestation = false;
      }
    },

    async saveUnitToBackend() {
      if (!this.pendingUnit.unitName) {
        this.saveError = 'Please enter a unit name';
        return;
      }

      if (!this.pendingUnitGeometry) {
        this.saveError = 'No polygon geometry available';
        return;
      }

      this.savingUnit = true;
      this.saveError = null;

      try {
        const roundCoordinate = (coord) => coord.map((val) => parseFloat(val.toFixed(8)));
        const rings = this.pendingUnitGeometry.rings.map((ring) => {
          const roundedRing = ring.map(roundCoordinate);
          const coords = [...roundedRing];
          const first = coords[0];
          const last = coords[coords.length - 1];
          if (first[0] !== last[0] || first[1] !== last[1]) {
            coords.push([...first]);
          }
          return coords;
        });

        const geoJsonPolygon = JSON.stringify({
          type: 'Polygon',
          coordinates: rings,
        });

        await axios.post('/api/production-units', {
          farmerId: this.farmer.id,
          unitName: this.pendingUnit.unitName,
          administrativeRegion: this.pendingUnit.administrativeRegion || '',
          geoJsonPolygon,
        });

        this.$toast.success(`"${this.pendingUnit.unitName}" saved successfully`);

        // Close dialog and reset
        this.showSaveUnitDialog = false;
        this.pendingUnit = { unitName: '', administrativeRegion: '' };
        this.pendingUnitGeometry = null;
        this.pendingUnitArea = 0;
        this.deforestationCheckResult = null;

        if (this.$refs.drawer) {
          this.$refs.drawer.clearDrawing();
        }

        // Refresh to show the new unit on the map
        await this.fetchProductionUnits();
        this.$emit('saved');
      } catch (error) {
        this.saveError = error.response?.data?.message || 'Failed to save production unit';
      } finally {
        this.savingUnit = false;
      }
    },

    getUnitColor(index) {
      const colors = ['#4CAF50', '#2196F3', '#9C27B0', '#FF9800', '#00BCD4'];
      return colors[index % colors.length];
    },

    zoomToUnit(unit) {
      if (this.$refs.drawer && unit.id) {
        this.$refs.drawer.zoomToUnit(unit.id);
        this.showUnitsList = false;
      }
    },

    deleteProductionUnit(unit, index) {
      this.deleteConfirmUnit = unit;
      this.deleteConfirmIndex = index;
      this.deleteConfirmDialog = true;
    },

    async confirmDelete() {
      if (!this.deleteConfirmUnit) return;

      this.deleting = true;
      try {
        // If unit has an id, delete from backend
        if (this.deleteConfirmUnit.id) {
          await axios.delete(`/api/production-units/${this.deleteConfirmUnit.id}`);
        }

        // Remove from local array
        if (this.deleteConfirmIndex !== null) {
          this.productionUnits.splice(this.deleteConfirmIndex, 1);
        }

        this.$toast.success('Production unit deleted');
        this.deleteConfirmDialog = false;
        this.deleteConfirmUnit = null;
        this.deleteConfirmIndex = null;

        // Refresh to update map
        await this.fetchProductionUnits();
      } catch (error) {
        this.$toast.error('Failed to delete production unit');
      } finally {
        this.deleting = false;
      }
    },

    close() {
      this.dialog = false;
      this.showUnitsList = false;
      this.showSaveUnitDialog = false;
      this.$emit('closed');
    },
  },
};
</script>

<style scoped>
/* Slide transition for units panel */
.slide-left-enter-active,
.slide-left-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}
.slide-left-enter,
.slide-left-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}
</style>
