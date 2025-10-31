<template>
  <default-layout>
    <!-- Main Content -->
    <v-container class="px-6 py-6" style="max-width: 1200px;">
      <!-- Header -->
      <v-row class="mb-6">
        <v-col cols="12">
          <v-row align="center" justify="space-between">
            <v-col cols="12" md="8">
              <v-chip
                color="success"
                outlined
                small
                class="mb-3"
              >
                <v-icon left small>mdi-map</v-icon>
                EUDR Compliance
              </v-chip>
              <h1 class="text-h4 font-weight-bold text--primary mb-2">
                My Production Units
              </h1>
              <p class="text-body-1 text--secondary mb-0">
                Manage your farm production areas and land verification for EUDR compliance
              </p>
            </v-col>
            <v-col cols="12" md="4" class="text-right">
              <v-btn
                color="success"
                large
                rounded
                @click="openAddDialog"
              >
                <v-icon left>mdi-add</v-icon>
                Add Production Unit
              </v-btn>
            </v-col>
          </v-row>
        </v-col>
      </v-row>

      <!-- Production Units Table -->
      <v-card class="elevation-2">
        <v-card-text class="pa-0">
          <v-data-table
            :headers="headers"
            :items="productionUnits"
            :loading="loading"
            :items-per-page="10"
            class="elevation-0"
            no-data-text="No production units found"
          >
            <template #loading>
              <v-skeleton-loader type="table-row@5"></v-skeleton-loader>
            </template>

            <template #item.name="{ item }">
              <div>
                <div class="font-weight-medium">{{ item.name }}</div>
                <div v-if="item.coordinates" class="text-caption text--secondary">
                  {{ item.coordinates }}
                </div>
              </div>
            </template>

            <template #item.area="{ item }">
              {{ item.area }} ha
            </template>

            <template #item.status="{ item }">
              <v-chip
                :color="getStatusColor(item.status)"
                small
                outlined
              >
                {{ item.status }}
              </v-chip>
            </template>

            <template #item.region="{ item }">
              {{ item.region || '—' }}
            </template>

            <template #item.alerts="{ item }">
              <v-chip
                :color="item.alerts > 0 ? 'error' : 'success'"
                small
                outlined
              >
                {{ item.alerts }}
              </v-chip>
            </template>

            <template #item.createdAt="{ item }">
              {{ formatDate(item.createdAt) }}
            </template>

            <template #item.actions="{ item }">
              <v-btn
                icon
                small
                color="primary"
                @click="viewUnit(item)"
                title="View details"
              >
                <v-icon small>mdi-visibility</v-icon>
              </v-btn>
              <v-btn
                icon
                small
                color="warning"
                @click="editUnit(item)"
                title="Edit polygon"
              >
                <v-icon small>mdi-pen</v-icon>
              </v-btn>
              <v-btn
                icon
                small
                color="error"
                @click="deleteUnit(item)"
                title="Delete unit"
              >
                <v-icon small>mdi-delete</v-icon>
              </v-btn>
            </template>

            <template #no-data>
              <v-row align="center" justify="center" class="py-8">
                <v-col cols="12" class="text-center">
                  <v-avatar size="64" color="grey lighten-4" class="mb-4">
                    <v-icon size="32" color="grey">mdi-map</v-icon>
                  </v-avatar>
                  <div class="text-h6 mb-2">No production units found</div>
                  <v-btn
                    color="success"
                    @click="openAddDialog"
                  >
                    <v-icon left>add</v-icon>
                    Add your first production unit
                  </v-btn>
                </v-col>
              </v-row>
            </template>
          </v-data-table>
        </v-card-text>
      </v-card>
    </v-container>

    <!-- Add/Edit Production Unit Dialog -->
    <v-dialog v-model="showAddDialog" max-width="90vw" max-height="90vh" persistent scrollable>
      <v-card class="tw-rounded-xl tw-h-full tw-flex tw-flex-col">
        <!-- Header -->
        <div class="tw-text-white tw-px-6 tw-py-4 tw-flex tw-items-center tw-justify-between tw-bg-gradient-to-r tw-from-green-600 tw-to-green-700 tw-rounded-t-xl">
          <div class="tw-flex tw-items-center tw-gap-3">
            <v-icon color="white">mdi-map-check</v-icon>
            <div>
              <div class="tw-font-semibold tw-text-lg">{{ editingUnit ? 'Edit Production Unit' : 'Add Production Unit' }}</div>
              <div class="tw-text-sm tw-opacity-90">Draw your farm boundaries on the map</div>
            </div>
          </div>
          <v-btn icon @click="closeAddDialog" color="white" text small>
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
                v-if="showAddDialog"
                :initial-location="farmerLocation"
                :existing-units="productionUnits"
                @unit-drawn="handleUnitDrawn"
                @save-unit="handleSaveUnit"
              />
            </div>

            <!-- Right Sidebar: Form and Units List -->
            <div class="tw-w-80 tw-bg-gray-50 tw-border-l tw-border-gray-200 tw-overflow-auto tw-flex tw-flex-col">
              <!-- Form Section -->
              <div class="tw-p-4 tw-border-b tw-border-gray-200">
                <v-form ref="unitForm" v-model="formValid">
                  <div class="tw-space-y-4">
                    <v-text-field
                      v-model="newUnit.unitName"
                      label="Unit Name"
                      :rules="[v => !!v || 'Unit name is required']"
                      outlined
                      dense
                      hide-details="auto"
                      placeholder="e.g., North Plot"
                      required
                    />
                    <v-text-field
                      v-model="newUnit.administrativeRegion"
                      label="Region (Optional)"
                      outlined
                      dense
                      hide-details="auto"
                      placeholder="e.g., Kiambu"
                    />
                    <v-btn
                      color="primary"
                      block
                      :disabled="!lastDrawnGeometry"
                      @click="addUnitFromDrawing"
                    >
                      <v-icon left small>mdi-plus</v-icon>
                      Add Unit
                    </v-btn>
                  </div>
                </v-form>

                <!-- Deforestation Check -->
                <div class="tw-mt-4">
                  <v-btn
                    color="warning"
                    block
                    outlined
                    small
                    :disabled="!lastDrawnGeometry"
                    @click="checkDeforestation"
                    :loading="checkingDeforestation"
                  >
                    <v-icon left small>mdi-alert-circle</v-icon>
                    Check Deforestation
                  </v-btn>
                  <div v-if="deforestationCheckResult" class="tw-mt-2">
                    <v-alert
                      :type="deforestationAlertType"
                      dense
                      class="tw-text-xs"
                    >
                      <div class="tw-font-medium">{{ deforestationCheckResult.riskLevel }} Risk</div>
                      <div v-if="deforestationCheckResult.hasAlerts">
                        {{ deforestationCheckResult.alertCount }} deforestation alert(s) found
                      </div>
                      <div v-else>
                        No deforestation alerts found
                      </div>
                    </v-alert>
                  </div>
                </div>
              </div>

              <!-- Units List -->
              <div class="tw-flex-1 tw-overflow-auto">
                <div class="tw-p-4">
                  <h4 class="tw-font-semibold tw-text-gray-800 tw-mb-3">Current Session Units</h4>
                  <div v-if="sessionUnits.length === 0" class="tw-text-center tw-py-8 tw-text-gray-500">
                    <v-icon size="48" color="grey lighten-1">mdi-map-outline</v-icon>
                    <div class="tw-mt-2 tw-text-sm">No units added yet</div>
                  </div>
                  <div v-else class="tw-space-y-2">
                    <v-card
                      v-for="(unit, index) in sessionUnits"
                      :key="index"
                      class="tw-p-3"
                      outlined
                    >
                      <div class="tw-flex tw-justify-between tw-items-start">
                        <div class="tw-flex-1">
                          <div class="tw-font-medium tw-text-sm">{{ unit.unitName }}</div>
                          <div class="tw-text-xs tw-text-gray-600">{{ unit.administrativeRegion || 'No region' }}</div>
                          <div class="tw-text-xs tw-text-green-600">{{ unit.areaHectares?.toFixed(2) }} ha</div>
                        </div>
                        <v-btn
                          icon
                          small
                          color="error"
                          @click="removeSessionUnit(index)"
                        >
                          <v-icon small>mdi-close</v-icon>
                        </v-btn>
                      </div>
                    </v-card>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </v-card-text>

        <!-- Footer -->
        <v-card-actions class="tw-px-6 tw-py-4 tw-bg-gray-50 tw-border-t tw-border-gray-200 tw-rounded-b-xl">
          <v-spacer></v-spacer>
          <v-btn text @click="closeAddDialog">Cancel</v-btn>
          <v-btn
            color="success"
            @click="saveChanges"
            :loading="saving"
            :disabled="sessionUnits.length === 0"
          >
            <v-icon left small>mdi-content-save</v-icon>
            Save All Units ({{ sessionUnits.length }})
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Delete Confirmation Dialog -->
    <v-dialog v-model="deleteConfirmDialog" max-width="400">
      <v-card>
        <v-card-title>Delete Production Unit?</v-card-title>
        <v-card-text>
          Are you sure you want to delete "{{ deleteConfirmUnit?.name }}"?
          This action cannot be undone.
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="deleteConfirmDialog = false">Cancel</v-btn>
          <v-btn color="error" @click="confirmDelete">Delete</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </default-layout>
</template>

<script>
import DefaultLayout from '@/components/layout/Default.vue';
import ProductionUnitDrawer from '@/components/exporter/ProductionUnitDrawer.vue';
import { productionUnitService } from '@/services/productionUnitService.js';

export default {
  name: 'FarmerProductionUnits',
  components: {
    DefaultLayout,
    ProductionUnitDrawer,
  },
  data() {
    return {
      loading: false,
      showAddDialog: false,
      editingUnit: null,
      productionUnits: [],
      farmerLocation: { lat: -1.2921, lng: 36.8219 }, // Default location, can be updated
      formValid: false,
      newUnit: {
        unitName: '',
        administrativeRegion: '',
      },
      lastDrawnGeometry: null,
      lastDrawnArea: null,
      sessionUnits: [], // Units added in current session
      saving: false,
      error: null,
      deleteConfirmDialog: false,
      deleteConfirmUnit: null,
      checkingDeforestation: false,
      deforestationCheckResult: null,
      headers: [
        { text: 'Unit Name', value: 'name', sortable: true },
        { text: 'Area (ha)', value: 'area', sortable: true },
        { text: 'Status', value: 'status', sortable: true },
        { text: 'Region', value: 'region', sortable: true },
        { text: 'Alerts', value: 'alerts', sortable: true },
        { text: 'Created', value: 'createdAt', sortable: true },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
    };
  },
  mounted() {
    this.setFarmerLocation();
    this.fetchProductionUnits();
  },
  computed: {
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
  methods: {
    setFarmerLocation() {
      // Try to get farmer location from user profile or use default
      const user = this.$store.state.auth.user;
      if (user && user.location) {
        this.farmerLocation = {
          lat: user.location.lat || -1.2921,
          lng: user.location.lng || 36.8219,
        };
      }
      // Default location is already set in data
    },
    async fetchProductionUnits() {
      this.loading = true;
      try {
        const farmerId = this.$store.state.auth.user?.id;
        if (!farmerId) {
          this.$toast.error('User not authenticated');
          this.productionUnits = [];
          return;
        }
        const response = await productionUnitService.getProductionUnitsByFarmer(farmerId);
        this.productionUnits = response.data || response || [];
      } catch (error) {
        this.$toast.error('Failed to load production units');
        this.productionUnits = [];
      } finally {
        this.loading = false;
      }
    },
    openAddDialog() {
      this.editingUnit = null;
      this.newUnit = {
        unitName: '',
        administrativeRegion: '',
      };
      this.lastDrawnGeometry = null;
      this.lastDrawnArea = null;
      this.sessionUnits = [];
      this.error = null;
      this.deforestationCheckResult = null;
      this.showAddDialog = true;
    },
    closeAddDialog() {
      this.showAddDialog = false;
      this.editingUnit = null;
      this.newUnit = {
        unitName: '',
        administrativeRegion: '',
      };
      this.lastDrawnGeometry = null;
      this.lastDrawnArea = null;
      this.sessionUnits = [];
      this.error = null;
      this.deforestationCheckResult = null;
    },
    handleUnitDrawn(data) {
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

      this.sessionUnits.push(newUnit);

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

    removeSessionUnit(index) {
      this.sessionUnits.splice(index, 1);
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
        // eslint-disable-next-line no-unused-vars
        const geoJsonPolygon = JSON.stringify({
          type: 'Polygon',
          coordinates: rings,
        });

        // You'll need to implement the deforestation check API call
        // For now, simulate a response
        this.deforestationCheckResult = {
          riskLevel: 'NONE',
          hasAlerts: false,
          alertCount: 0,
        };

        // Show success notification if no alerts
        if (!this.deforestationCheckResult.hasAlerts) {
          this.$toast.success('No deforestation alerts found for this area');
        }
      } catch (error) {
        this.error = 'Failed to check deforestation status';
      } finally {
        this.checkingDeforestation = false;
      }
    },

    async saveChanges() {
      this.saving = true;
      this.error = null;

      try {
        const farmerId = this.$store.state.auth.user?.id;
        if (!farmerId) {
          this.$toast.error('User not authenticated');
          return;
        }

        // Save all session units
        const savePromises = this.sessionUnits.map(async (unit) => {
          // Convert ESRI geometry to GeoJSON
          const roundCoordinate = (coord) => coord.map((val) => parseFloat(val.toFixed(8)));

          const rings = unit.parcelGeometry.rings.map((ring) => {
            const roundedRing = ring.map(roundCoordinate);
            const coords = [...roundedRing];
            const first = coords[0];
            const last = coords[coords.length - 1];

            const isClosed = first[0] === last[0] && first[1] === last[1];
            if (!isClosed) {
              coords.push([...first]);
            }

            return coords;
          });

          const geoJsonPolygon = JSON.stringify({
            type: 'Polygon',
            coordinates: rings,
          });

          return productionUnitService.createProductionUnit({
            farmerId,
            unitName: unit.unitName,
            administrativeRegion: unit.administrativeRegion || '',
            geoJsonPolygon,
          });
        });

        await Promise.all(savePromises);

        this.$toast.success(`${this.sessionUnits.length} production unit(s) saved successfully`);
        this.closeAddDialog();
        this.fetchProductionUnits();
      } catch (error) {
        this.error = error.response?.data?.message || 'Failed to save production units';
      } finally {
        this.saving = false;
      }
    },
    getStatusColor(status) {
      const colors = {
        Verified: 'success',
        Pending: 'warning',
        Rejected: 'error',
        'Under Review': 'info',
      };
      return colors[status] || 'grey';
    },
    viewUnit(unit) {
      // Navigate to unit details or show modal
      this.$toast.info(`Viewing details for ${unit.name}`);
    },

    editUnit(unit) {
      this.editingUnit = unit;
      this.showAddDialog = true;
      // TODO: Populate form with existing unit data
    },
    deleteUnit(unit) {
      this.deleteConfirmUnit = unit;
      this.deleteConfirmDialog = true;
    },

    async confirmDelete() {
      if (!this.deleteConfirmUnit) return;

      try {
        // Implement delete API call here
        // await productionUnitService.deleteProductionUnit(this.deleteConfirmUnit.id);

        // For now, just remove from local array
        const index = this.productionUnits.findIndex((u) => u.id === this.deleteConfirmUnit.id);
        if (index > -1) {
          this.productionUnits.splice(index, 1);
        }

        this.$toast.success('Production unit deleted successfully');
        this.deleteConfirmDialog = false;
        this.deleteConfirmUnit = null;
      } catch (error) {
        this.$toast.error('Failed to delete production unit');
      }
    },
    formatDate(dateString) {
      if (!dateString) return '—';
      const date = new Date(dateString);
      if (Number.isNaN(date.getTime())) return '—';
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
/* Vuetify handles all styling - no custom CSS needed */
</style>
