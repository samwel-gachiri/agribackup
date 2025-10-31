<template>
  <v-card class="tw-rounded-xl tw-h-full tw-flex tw-flex-col">
    <!-- Header -->
    <div class="tw-text-white tw-px-6 tw-py-4 tw-flex tw-items-center tw-justify-between tw-bg-gradient-to-r tw-from-green-600 tw-to-green-700 tw-rounded-t-xl">
      <div class="tw-flex tw-items-center tw-gap-3">
        <v-icon color="white">mdi-map-check</v-icon>
        <div>
          <h2 class="tw-text-lg tw-font-bold">Edit {{ displayName }} Production Units</h2>
          <p class="tw-text-xs tw-text-green-100">(EUDR Tracing)</p>
        </div>
      </div>
      <v-btn icon @click="close" color="white" text small>
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </div>

    <!-- Content -->
    <v-card-text class="tw-flex-1 tw-overflow-auto tw-p-0 tw-flex tw-flex-col tw-gap-4">
      <!-- Error Alert -->
      <v-alert v-if="error" type="error" dismissible @input="error = null" class="tw-mb-0 tw-rounded-none">
        {{ error }}
      </v-alert>

      <!-- Left Panel: Units List -->
      <div class="tw-flex tw-flex-1">
        <!-- Map Drawer -->
        <div class="tw-flex-1 tw-min-h-0">
          <ProductionUnitDrawer
            ref="drawer"
            :initial-location="farmerLocation"
            :existing-units="productionUnits"
            @unit-drawn="handleUnitDrawn"
            @unit-updated="handleUnitUpdated"
            @save-unit="handleSaveUnit"
          />
        </div>

        <!-- Right Sidebar: Units List -->
        <div class="tw-w-80 tw-bg-gray-50 tw-border-l tw-border-gray-200 tw-overflow-auto tw-flex tw-flex-col">
          <div class="tw-p-4 tw-border-b tw-border-gray-200">
            <h3 class="tw-font-semibold tw-text-gray-800 tw-flex tw-items-center tw-gap-2">
              <v-icon small color="primary">mdi-layers</v-icon>
              Units ({{ productionUnits.length }})
            </h3>
          </div>

          <!-- Units List Content -->
          <div class="tw-flex-1 tw-overflow-auto">
            <!-- Loading State -->
            <div v-if="loading" class="tw-p-6 tw-text-center">
              <v-progress-circular indeterminate color="primary" size="40" />
              <div class="tw-text-sm tw-text-gray-500 tw-mt-3">Loading production units...</div>
            </div>

            <!-- Units List -->
            <div v-else-if="productionUnits.length" class="tw-space-y-2 tw-p-3">
              <div
                v-for="(unit, idx) in productionUnits"
                :key="unit.id || idx"
                class="tw-p-3 tw-bg-white tw-rounded-lg tw-border tw-border-gray-200 hover:tw-shadow-md tw-transition"
              >
                <div class="tw-flex tw-items-start tw-justify-between tw-gap-2">
                  <div class="tw-flex-1">
                    <div class="tw-font-medium tw-text-gray-800 tw-text-sm tw-truncate">
                      {{ unit.unitName || `Unit ${idx + 1}` }}
                    </div>
                    <div class="tw-text-xs tw-text-gray-500">
                      {{ (unit.areaHectares || 0).toFixed(2) }} ha
                    </div>
                  </div>
                  <v-btn icon x-small color="red" @click="deleteProductionUnit(idx)">
                    <v-icon x-small>mdi-delete</v-icon>
                  </v-btn>
                </div>
              </div>
            </div>

            <!-- Empty State -->
            <div v-else class="tw-text-sm tw-text-gray-500 tw-p-4 tw-text-center">
              No units yet
            </div>
          </div>

          <!-- New Unit Form -->
          <div class="tw-p-3 tw-border-t tw-border-gray-200 tw-bg-white tw-space-y-3">
            <v-text-field
              v-model="newUnit.unitName"
              label="Unit/farm Name"
              outlined
              dense
              small
              hide-details
              placeholder="e.g., North Plot"
            />
            <v-text-field
              v-model="newUnit.administrativeRegion"
              label="Region (Optional)"
              outlined
              dense
              small
              hide-details
              placeholder="e.g., Kiambu"
            />

            <!-- Deforestation Check Button -->
            <v-btn
              small
              color="info"
              block
              @click="checkDeforestation"
              :disabled="!lastDrawnGeometry"
              :loading="checkingDeforestation"
              outlined
            >
              <v-icon left x-small>mdi-shield-search</v-icon>
              Check for Deforestation
            </v-btn>

            <!-- Deforestation Status Alert -->
            <v-alert
              v-if="deforestationCheckResult"
              :type="deforestationAlertType"
              dense
              text
              dismissible
              @input="deforestationCheckResult = null"
            >
              <div class="tw-text-xs">
                <div class="tw-font-semibold tw-mb-1">{{ deforestationCheckResult.message }}</div>
                <div v-if="deforestationCheckResult.totalAlerts > 0" class="tw-space-y-1">
                  <div>Total Alerts: {{ deforestationCheckResult.totalAlerts }}</div>
                  <div v-if="deforestationCheckResult.gladAlerts > 0">
                    GLAD Alerts: {{ deforestationCheckResult.gladAlerts }}
                  </div>
                  <div v-if="deforestationCheckResult.viirsAlerts > 0">
                    Fire Alerts: {{ deforestationCheckResult.viirsAlerts }}
                  </div>
                  <div v-if="deforestationCheckResult.treeCoverLossAlerts > 0">
                    Tree Cover Loss: {{ deforestationCheckResult.treeCoverLossAlerts }}
                  </div>
                  <div class="tw-mt-2 tw-pt-2 tw-border-t tw-border-gray-300">
                    <span class="tw-font-semibold">Risk Level:</span> {{ deforestationCheckResult.riskLevel }}
                  </div>
                </div>
              </div>
            </v-alert>

            <v-btn
              small
              color="success"
              block
              @click="addUnitFromDrawing"
              :disabled="!lastDrawnGeometry"
            >
              <v-icon left x-small>mdi-plus</v-icon>
              Add Unit
            </v-btn>
          </div>
        </div>
      </div>
    </v-card-text>

    <!-- Footer -->
    <v-card-actions class="tw-px-6 tw-py-4 tw-bg-gray-50 tw-border-t tw-border-gray-200 tw-rounded-b-xl">
      <v-spacer></v-spacer>
      <v-btn text @click="close">Cancel</v-btn>
      <v-btn color="success" @click="saveChanges" :loading="saving">
        <v-icon left small>mdi-content-save</v-icon>
        Save Changes
      </v-btn>
    </v-card-actions>

    <!-- Success Dialog -->
    <v-dialog v-model="successDialog" max-width="400" persistent>
      <v-card class="tw-rounded-lg">
        <v-card-text class="tw-text-center tw-py-8">
          <v-icon color="success" size="48">mdi-check-circle</v-icon>
          <h3 class="tw-text-lg tw-font-bold tw-mt-3 tw-mb-1">Changes Saved!</h3>
          <p class="tw-text-sm tw-text-gray-600">Production units updated successfully.</p>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" text small @click="successDialog = false; close()">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteConfirmDialog" max-width="400">
      <v-card>
        <v-card-title>Delete Production Unit?</v-card-title>
        <v-card-text>
          Are you sure you want to delete this production unit? This action cannot be undone.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="deleteConfirmDialog = false">Cancel</v-btn>
          <v-btn color="error" @click="confirmDelete">Delete</v-btn>
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
      saving: false,
      loading: false,
      error: null,
      successDialog: false,
      productionUnits: [],
      newUnit: {
        unitName: '',
        administrativeRegion: '',
      },
      lastDrawnGeometry: null,
      lastDrawnArea: null,
      deleteConfirmDialog: false,
      deleteConfirmIndex: null,
      checkingDeforestation: false,
      deforestationCheckResult: null,
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
    deforestationAlertType() {
      if (!this.deforestationCheckResult) return 'info';

      const riskLevel = this.deforestationCheckResult.riskLevel;
      if (riskLevel === 'HIGH') return 'error';
      if (riskLevel === 'MEDIUM') return 'warning';
      if (riskLevel === 'LOW') return 'info';
      if (riskLevel === 'NONE') return 'success';
      return 'error'; // For ERROR case
    },
  },
  watch: {
    value(val) {
      this.dialog = val;
      if (val) {
        this.initializeForm();
      }
    },
    dialog(val) {
      this.$emit('input', val);
    },
  },
  mounted() {
    // If dialog is already open when component mounts, fetch production units
    if (this.value) {
      this.initializeForm();
    }
  },
  methods: {
    async initializeForm() {
      await this.fetchProductionUnits();
    },

    async fetchProductionUnits() {
      if (!this.farmer || !this.farmer.id) {
        this.$toast.show('No farmer ID available for fetching production units');
        return;
      }

      this.loading = true;
      this.error = null;

      try {
        const response = await axios.get(`/api/production-units/farmer/${this.farmer.id}`);
        const units = response.data.data || [];

        // Convert backend format to frontend format
        this.productionUnits = units.map((unit) => ({
          id: unit.id,
          unitName: unit.unitName,
          administrativeRegion: unit.administrativeRegion || '',
          areaHectares: unit.areaHectares,
          // Parse GeoJSON string to ESRI-like format for the map
          parcelGeometry: unit.parcelGeometryGeoJson
            ? this.parseGeoJsonToEsri(unit.parcelGeometryGeoJson)
            : null,
          isNew: false, // Existing units from database
        }));
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Failed to load production units';
        this.error = errorMsg;
        this.productionUnits = [];
      } finally {
        this.loading = false;
      }
    },

    parseGeoJsonToEsri(geoJsonString) {
      try {
        const geoJson = JSON.parse(geoJsonString);
        // Convert GeoJSON Polygon to ESRI format
        return {
          rings: geoJson.coordinates,
          spatialReference: { wkid: 4326 },
        };
      } catch (e) {
        return null;
      }
    },

    handleUnitDrawn(data) {
      this.lastDrawnGeometry = data.geometry;
      this.lastDrawnArea = data.area;
    },

    handleUnitUpdated(data) {
      this.lastDrawnGeometry = data.geometry;
      this.lastDrawnArea = data.area;
    },

    handleSaveUnit(data) {
      this.lastDrawnGeometry = data.geometry;
      this.lastDrawnArea = data.area;
      this.addUnitFromDrawing();
    },

    addUnitFromDrawing() {
      if (!this.newUnit.unitName) {
        this.error = 'Please enter a unit name';
        return;
      }

      if (!this.lastDrawnGeometry) {
        this.error = 'Please draw a polygon on the map first';
        return;
      }

      const newUnit = {
        unitName: this.newUnit.unitName,
        administrativeRegion: this.newUnit.administrativeRegion,
        parcelGeometry: this.lastDrawnGeometry,
        areaHectares: this.lastDrawnArea,
        isNew: true,
      };

      this.productionUnits.push(newUnit);

      // Reset form
      this.newUnit = {
        unitName: '',
        administrativeRegion: '',
      };
      this.lastDrawnGeometry = null;
      this.lastDrawnArea = null;

      // Clear map in drawer
      if (this.$refs.drawer) {
        this.$refs.drawer.clearDrawing();
      }

      this.error = null;
    },

    deleteProductionUnit(index) {
      this.deleteConfirmIndex = index;
      this.deleteConfirmDialog = true;
    },

    confirmDelete() {
      if (this.deleteConfirmIndex !== null) {
        this.productionUnits.splice(this.deleteConfirmIndex, 1);
        this.deleteConfirmDialog = false;
        this.deleteConfirmIndex = null;
      }
    },

    async checkDeforestation() {
      if (!this.lastDrawnGeometry) {
        this.error = 'Please draw a polygon on the map first';
        return;
      }

      this.checkingDeforestation = true;
      this.error = null;
      this.deforestationCheckResult = null;

      try {
        // Helper function to round coordinates to 8 decimal places
        const roundCoordinate = (coord) => coord.map((val) => parseFloat(val.toFixed(8)));

        // Ensure polygon rings are properly closed for GeoJSON
        const rings = this.lastDrawnGeometry.rings.map((ring) => {
          // Round coordinates to max 8 decimal places
          const roundedRing = ring.map(roundCoordinate);

          // GeoJSON requires the first and last coordinate to be identical
          const coords = [...roundedRing];
          const first = coords[0];
          const last = coords[coords.length - 1];

          // Check if ring is already closed
          const isClosed = first[0] === last[0] && first[1] === last[1];

          // If not closed, add the first coordinate at the end
          if (!isClosed) {
            coords.push([...first]);
          }

          return coords;
        });

        // Convert ESRI geometry to GeoJSON string
        const geoJsonPolygon = JSON.stringify({
          type: 'Polygon',
          coordinates: rings,
        });
        const response = await axios.post('/api/v1/deforestation-alerts/check-geometry', {
          geoJsonPolygon,
        });

        this.deforestationCheckResult = response.data;

        // Show success notification if no alerts
        if (!this.deforestationCheckResult.hasAlerts) {
          this.$emit('info', 'Area is clean - no deforestation alerts detected');
        }
      } catch (error) {
        const errorMsg = error.response?.data?.message || 'Failed to check for deforestation alerts';
        this.error = errorMsg;

        // Set error result
        this.deforestationCheckResult = {
          hasAlerts: false,
          totalAlerts: 0,
          riskLevel: 'ERROR',
          message: errorMsg,
        };
      } finally {
        this.checkingDeforestation = false;
      }
    },

    async saveChanges() {
      this.saving = true;
      this.error = null;

      try {
        const newUnits = this.productionUnits.filter((u) => u.isNew);

        // Convert each unit's ESRI geometry to GeoJSON before sending
        await Promise.all(newUnits.map(async (unit) => {
          // Skip if no geometry
          if (!unit.parcelGeometry || !unit.parcelGeometry.rings) {
            throw new Error(`Unit "${unit.unitName}" has no geometry`);
          }

          // Helper function to round coordinates to 8 decimal places
          const roundCoordinate = (coord) => coord.map((val) => parseFloat(val.toFixed(8)));

          // Ensure polygon rings are properly closed for GeoJSON
          const rings = unit.parcelGeometry.rings.map((ring) => {
            // Round coordinates to max 8 decimal places (EUDR requirement)
            const roundedRing = ring.map(roundCoordinate);

            // GeoJSON requires the first and last coordinate to be identical
            const coords = [...roundedRing];
            const first = coords[0];
            const last = coords[coords.length - 1];

            // Check if ring is already closed
            const isClosed = first[0] === last[0] && first[1] === last[1];

            // If not closed, add the first coordinate at the end
            if (!isClosed) {
              coords.push([...first]);
            }

            return coords;
          });

          // Convert ESRI geometry to GeoJSON string
          const geoJsonPolygon = JSON.stringify({
            type: 'Polygon',
            coordinates: rings,
          });

          return axios.post('/api/production-units', {
            farmerId: this.farmer.id,
            unitName: unit.unitName,
            administrativeRegion: unit.administrativeRegion || '',
            geoJsonPolygon,
          });
        }));

        this.successDialog = true;
        this.$emit('saved');
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to save changes';
      } finally {
        this.saving = false;
      }
    },

    close() {
      this.dialog = false;
      this.error = null;
      this.newUnit = {
        unitName: '',
        administrativeRegion: '',
      };
      this.lastDrawnGeometry = null;
      this.lastDrawnArea = null;
      this.$emit('closed');
    },
  },
};
</script>

<style scoped>
/* Minimal styles */
</style>
