<template>
  <Default>
    <v-container fluid class="aggregator-collection-page tw-bg-gray-50 tw-min-h-screen">
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <div class="tw-flex tw-items-center tw-justify-between tw-mb-6">
            <div class="tw-flex tw-items-center tw-gap-4">
              <v-btn icon @click="$router.back()">
                <v-icon>mdi-arrow-left</v-icon>
              </v-btn>
              <div>
                <h1 class="tw-text-2xl tw-font-bold tw-text-gray-800">Record Collection</h1>
                <p class="tw-text-gray-500 tw-text-sm">Collect produce from farmers</p>
              </div>
            </div>
            <div class="tw-flex tw-items-center tw-gap-3">
              <v-chip v-if="sessionCount > 0" color="green" text-color="white">
                <v-icon left small>mdi-check</v-icon>
                {{ sessionCount }} collected today
              </v-chip>
              <v-switch
                v-model="quickMode"
                label="Quick Mode"
                color="amber"
                hide-details
                class="tw-mt-0"
              ></v-switch>
            </div>
          </div>
        </v-col>
      </v-row>

      <v-row>
        <!-- Left Column: QR Scanner & Farmer Search -->
        <v-col cols="12" md="5">
          <!-- QR Scanner Card -->
          <v-card class="tw-mb-4" elevation="2">
            <v-card-title class="tw-bg-purple-600 tw-text-white">
              <v-icon color="white" class="tw-mr-2">mdi-qrcode-scan</v-icon>
              Scan Farmer QR Code
            </v-card-title>
            <v-card-text class="tw-p-4">
              <div v-if="showScanner" class="qr-scanner-container">
                <qrcode-stream
                  @decode="onQrDecode"
                  @init="onScannerInit"
                  :camera="cameraFacing"
                  class="tw-rounded-lg tw-overflow-hidden"
                >
                  <div v-if="scannerLoading" class="tw-absolute tw-inset-0 tw-flex tw-items-center tw-justify-center tw-bg-black tw-bg-opacity-50">
                    <v-progress-circular indeterminate color="white"></v-progress-circular>
                  </div>
                </qrcode-stream>
                <div class="tw-flex tw-justify-center tw-gap-2 tw-mt-3">
                  <v-btn small outlined @click="toggleCamera">
                    <v-icon left small>mdi-camera-flip</v-icon>
                    Flip Camera
                  </v-btn>
                  <v-btn small outlined color="red" @click="showScanner = false">
                    <v-icon left small>mdi-close</v-icon>
                    Close
                  </v-btn>
                </div>
              </div>
              <div v-else class="tw-text-center tw-py-8">
                <v-icon size="64" color="purple lighten-2">mdi-qrcode</v-icon>
                <p class="tw-text-gray-600 tw-mt-2">Scan farmer's QR code for quick identification</p>
                <v-btn color="purple" dark class="tw-mt-4" @click="startScanner">
                  <v-icon left>mdi-camera</v-icon>
                  Start Scanner
                </v-btn>
              </div>
              <v-alert v-if="scannerError" type="error" dense class="tw-mt-3">
                {{ scannerError }}
              </v-alert>
            </v-card-text>
          </v-card>

          <!-- Manual Search Card -->
          <v-card elevation="2">
            <v-card-title class="tw-text-base tw-font-semibold">
              <v-icon color="blue" class="tw-mr-2">mdi-account-search</v-icon>
              Or Search Manually
            </v-card-title>
            <v-card-text>
              <v-text-field
                v-model="farmerSearch"
                label="Search by phone number or name"
                outlined
                dense
                prepend-inner-icon="mdi-magnify"
                :loading="searchingFarmer"
                @input="debouncedFarmerSearch"
                clearable
              ></v-text-field>

              <v-list v-if="farmerSearchResults.length > 0" dense class="tw-max-h-48 tw-overflow-y-auto">
                <v-list-item
                  v-for="farmer in farmerSearchResults"
                  :key="farmer.id"
                  @click="selectFarmer(farmer)"
                  class="tw-cursor-pointer hover:tw-bg-gray-100"
                >
                  <v-list-item-avatar color="green lighten-4">
                    <v-icon color="green">mdi-account</v-icon>
                  </v-list-item-avatar>
                  <v-list-item-content>
                    <v-list-item-title>{{ farmer.name }}</v-list-item-title>
                    <v-list-item-subtitle>{{ farmer.phoneNumber }}</v-list-item-subtitle>
                  </v-list-item-content>
                  <v-list-item-action>
                    <v-icon color="primary">mdi-chevron-right</v-icon>
                  </v-list-item-action>
                </v-list-item>
              </v-list>

              <v-alert v-if="farmerSearch && farmerSearchResults.length === 0 && !searchingFarmer"
                       type="info" dense class="tw-mt-2">
                No farmers found. Try a different search term.
              </v-alert>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- Right Column: Collection Form -->
        <v-col cols="12" md="7">
          <v-card elevation="2">
            <v-card-title class="tw-bg-green-600 tw-text-white">
              <v-icon color="white" class="tw-mr-2">mdi-basket</v-icon>
              Collection Details
              <v-spacer></v-spacer>
              <v-chip v-if="selectedFarmer" small color="white" text-color="green">
                <v-icon left small>mdi-check-circle</v-icon>
                Farmer Selected
              </v-chip>
            </v-card-title>

            <v-card-text class="tw-p-6">
              <!-- Selected Farmer Display -->
              <v-alert v-if="selectedFarmer" type="success" dense class="tw-mb-4">
                <div class="tw-flex tw-items-center tw-justify-between">
                  <div>
                    <strong>{{ selectedFarmer.firstName }}</strong>
                    <span class="tw-text-sm tw-ml-2">({{ selectedFarmer.phoneNumber }})</span>
                  </div>
                  <v-btn x-small text color="white" @click="clearFarmer">
                    <v-icon small>mdi-close</v-icon> Change
                  </v-btn>
                </div>
              </v-alert>

              <v-alert v-else type="warning" dense class="tw-mb-4">
                Please scan QR code or search for a farmer first.
              </v-alert>

              <v-form ref="collectionForm" v-model="formValid" :disabled="!selectedFarmer">
                <!-- Production Unit Selection -->
                <v-select
                  v-model="collection.productionUnitId"
                  :items="farmerProductionUnits"
                  item-text="displayName"
                  item-value="id"
                  label="Select Production Unit *"
                  outlined
                  dense
                  prepend-inner-icon="mdi-map-marker"
                  :loading="loadingProductionUnits"
                  :rules="[rules.required]"
                  class="tw-mb-2"
                >
                  <template v-slot:item="{ item }">
                    <div class="tw-py-1">
                      <div class="tw-font-medium">{{ item.unitName }}</div>
                      <div class="tw-text-xs tw-text-gray-500">
                        {{ item.areaHectares }} ha • {{ item.administrativeRegion || 'Unknown region' }}
                      </div>
                    </div>
                  </template>
                </v-select>
                <v-card v-if="selectedFarmer && farmerProductionUnits.length === 0 && !loadingProductionUnits"
                         outlined class="tw-mb-4 tw-border-orange-300 tw-bg-orange-50">
                  <v-card-text class="tw-py-3">
                    <div class="tw-flex tw-items-center tw-justify-between">
                      <div class="tw-flex tw-items-center tw-gap-2">
                        <v-icon color="orange">mdi-alert</v-icon>
                        <span class="tw-text-orange-800">No production units registered for this farmer.</span>
                      </div>
                      <v-btn small color="green" dark @click="openAddProductionUnitDialog">
                        <v-icon left small>mdi-plus</v-icon>
                        Add Unit
                      </v-btn>
                    </div>
                  </v-card-text>
                </v-card>

                <v-row>
                  <v-col cols="12" md="6">
                    <v-combobox
                      v-model="collection.produceType"
                      :items="commonProduceTypes"
                      label="Produce Type *"
                      outlined
                      dense
                      prepend-inner-icon="mdi-leaf"
                      :rules="[rules.required]"
                    ></v-combobox>
                  </v-col>
                  <v-col cols="12" md="6">
                    <v-text-field
                      v-model.number="collection.quantityKg"
                      label="Quantity (kg) *"
                      type="number"
                      outlined
                      dense
                      prepend-inner-icon="mdi-weight-kilogram"
                      :rules="[rules.required, rules.positive]"
                      ref="quantityField"
                    ></v-text-field>
                  </v-col>
                </v-row>

                <v-row>
                  <v-col cols="12" md="4">
                    <v-text-field
                      v-model="collection.collectionDate"
                      label="Collection Date *"
                      type="date"
                      outlined
                      dense
                      prepend-inner-icon="mdi-calendar"
                      :rules="[rules.required]"
                    ></v-text-field>
                  </v-col>
                  <v-col cols="12" md="4">
                    <div class="tw-text-sm tw-text-gray-600 tw-mb-1">Quality Grade *</div>
                    <v-btn-toggle v-model="collection.qualityGrade" mandatory class="tw-w-full">
                      <v-btn value="A" color="green" class="tw-flex-1" :outlined="collection.qualityGrade !== 'A'">
                        <v-icon left small>mdi-star</v-icon> A
                      </v-btn>
                      <v-btn value="B" color="blue" class="tw-flex-1" :outlined="collection.qualityGrade !== 'B'">
                        <v-icon left small>mdi-star-half-full</v-icon> B
                      </v-btn>
                      <v-btn value="C" color="orange" class="tw-flex-1" :outlined="collection.qualityGrade !== 'C'">
                        <v-icon left small>mdi-star-outline</v-icon> C
                      </v-btn>
                    </v-btn-toggle>
                  </v-col>
                  <v-col cols="12" md="4">
                    <v-text-field
                      v-model.number="collection.pricePerKg"
                      label="Price per kg"
                      type="number"
                      step="0.01"
                      outlined
                      dense
                      prefix="KES"
                      prepend-inner-icon="mdi-currency-usd"
                    ></v-text-field>
                  </v-col>
                </v-row>

                <!-- Payment Summary -->
                <v-card v-if="totalPayment > 0" outlined class="tw-bg-blue-50 tw-mb-4">
                  <v-card-text class="tw-py-3">
                    <div class="tw-flex tw-justify-between tw-items-center">
                      <span class="tw-text-gray-700">Total Payment:</span>
                      <span class="tw-text-2xl tw-font-bold tw-text-blue-700">
                        KES {{ formatNumber(totalPayment) }}
                      </span>
                    </div>
                  </v-card-text>
                </v-card>

                <v-textarea
                  v-model="collection.notes"
                  label="Notes (optional)"
                  outlined
                  dense
                  rows="2"
                  prepend-inner-icon="mdi-note-text"
                ></v-textarea>
              </v-form>
            </v-card-text>

            <v-divider></v-divider>

            <v-card-actions class="tw-px-6 tw-py-4 tw-bg-gray-50">
              <v-chip v-if="quickMode" small color="amber" text-color="black">
                <v-icon left small>mdi-lightning-bolt</v-icon>
                Quick Mode: Form will reset after save
              </v-chip>
              <v-spacer></v-spacer>
              <v-btn text @click="$router.push({ name: 'AggregatorDashboard' })">
                Cancel
              </v-btn>
              <v-btn
                color="green"
                dark
                large
                :loading="submitting"
                :disabled="!selectedFarmer || !formValid"
                @click="submitCollection"
              >
                <v-icon left>mdi-check-bold</v-icon>
                {{ quickMode ? 'Save & Next' : 'Save Collection' }}
              </v-btn>
            </v-card-actions>
          </v-card>

          <!-- Recent Collections -->
          <v-card v-if="recentCollections.length > 0" class="tw-mt-4" elevation="1">
            <v-card-title class="tw-text-base">
              <v-icon color="green" class="tw-mr-2">mdi-history</v-icon>
              Recent Collections This Session
            </v-card-title>
            <v-list dense>
              <v-list-item v-for="(item, index) in recentCollections" :key="index">
                <v-list-item-avatar color="green lighten-4">
                  <v-icon color="green">mdi-check</v-icon>
                </v-list-item-avatar>
                <v-list-item-content>
                  <v-list-item-title>{{ item.farmerName }}</v-list-item-title>
                  <v-list-item-subtitle>
                    {{ item.quantityKg }} kg {{ item.produceType }} • Grade {{ item.qualityGrade }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-list>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Add Production Unit Full-Screen Drawer -->
    <v-navigation-drawer
      v-model="showAddUnitDrawer"
      fixed
      temporary
      right
      width="100%"
      class="production-unit-drawer-container"
    >
      <div class="tw-h-full tw-flex tw-flex-col">
        <!-- Compact Header -->
        <div class="tw-bg-green-600 tw-px-4 tw-py-2 tw-flex tw-items-center tw-justify-between tw-flex-shrink-0">
          <div class="tw-flex tw-items-center tw-gap-2 tw-text-white">
            <v-icon color="white" small>mdi-map-marker-plus</v-icon>
            <span class="tw-font-medium">Draw Unit for {{ selectedFarmer?.firstName || 'Farmer' }}</span>
          </div>
          <v-btn icon small @click="closeUnitDrawer" color="white">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </div>

        <!-- Production Unit Drawer Component -->
        <div class="tw-flex-1 tw-overflow-hidden">
          <ProductionUnitDrawer
            v-if="showAddUnitDrawer"
            ref="unitDrawer"
            :initial-location="{ lat: -1.2921, lng: 36.8219 }"
            :existing-units="[]"
            @save-unit="openSaveUnitDialog"
          />
        </div>
      </div>
    </v-navigation-drawer>

    <!-- Save Unit Dialog (appears after drawing polygon) -->
    <v-dialog v-model="showSaveUnitDialog" max-width="400" persistent>
      <v-card>
        <v-card-title class="tw-flex tw-items-center tw-gap-2">
          <v-icon color="green">mdi-map-marker-check</v-icon>
          Save Production Unit
        </v-card-title>
        <v-card-text>
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
              placeholder="e.g., Main Farm, North Plot"
              autofocus
            />
            <v-text-field
              v-model="pendingUnit.administrativeRegion"
              label="Region (Optional)"
              outlined
              dense
              placeholder="e.g., Kiambu County"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-btn text @click="cancelSaveUnit">Cancel</v-btn>
          <v-spacer></v-spacer>
          <v-btn
            color="success"
            :loading="creatingUnit"
            :disabled="!saveFormValid"
            @click="saveProductionUnit"
          >
            <v-icon left small>mdi-content-save</v-icon>
            Save Unit
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';
import ProductionUnitDrawer from '@/components/exporter/ProductionUnitDrawer.vue';
import VueQrcodeReader from 'vue-qrcode-reader';

export default {
  name: 'AggregatorCollection',
  components: {
    Default,
    ProductionUnitDrawer,
    QrcodeStream: VueQrcodeReader.QrcodeStream,
  },
  data() {
    return {
      // Scanner
      showScanner: false,
      scannerLoading: false,
      scannerError: null,
      cameraFacing: 'environment', // 'environment' = back camera, 'user' = front

      // Farmer search
      farmerSearch: '',
      searchingFarmer: false,
      farmerSearchResults: [],
      selectedFarmer: null,
      searchDebounceTimer: null,

      // Production units
      farmerProductionUnits: [],
      loadingProductionUnits: false,

      // Add Production Unit Drawer
      showAddUnitDrawer: false,
      showSaveUnitDialog: false,
      saveFormValid: false,
      creatingUnit: false,
      pendingUnit: {
        unitName: '',
        administrativeRegion: '',
      },
      pendingUnitGeometry: null,
      pendingUnitArea: 0,

      // Collection form
      formValid: false,
      submitting: false,
      collection: {
        productionUnitId: null,
        produceType: '',
        quantityKg: null,
        collectionDate: new Date().toISOString().split('T')[0],
        qualityGrade: 'A',
        pricePerKg: null,
        notes: '',
      },

      // Quick mode
      quickMode: true,
      sessionCount: 0,
      recentCollections: [],

      // Validation rules
      rules: {
        required: (v) => !!v || 'Required field',
        positive: (v) => (v && v > 0) || 'Must be greater than 0',
      },

      // Produce types
      commonProduceTypes: [
        'Coffee Arabica',
        'Coffee Robusta',
        'Tea',
        'Macadamia',
        'Avocado',
        'Maize',
        'Beans',
        'Sorghum',
        'Millet',
        'Cassava',
      ],
    };
  },
  computed: {
    aggregatorId() {
      return this.$store.state.user?.entityId || null;
    },
    totalPayment() {
      if (this.collection.quantityKg && this.collection.pricePerKg) {
        return this.collection.quantityKg * this.collection.pricePerKg;
      }
      return 0;
    },
  },
  methods: {
    // ========== QR SCANNER ==========
    startScanner() {
      this.showScanner = true;
      this.scannerError = null;
    },

    async onScannerInit(promise) {
      this.scannerLoading = true;
      try {
        await promise;
      } catch (error) {
        if (error.name === 'NotAllowedError') {
          this.scannerError = 'Camera access denied. Please allow camera permissions.';
        } else if (error.name === 'NotFoundError') {
          this.scannerError = 'No camera found on this device.';
        } else if (error.name === 'NotSupportedError') {
          this.scannerError = 'Camera not supported. Try using HTTPS.';
        } else {
          this.scannerError = `Camera error: ${error.message}`;
        }
        this.showScanner = false;
      } finally {
        this.scannerLoading = false;
      }
    },

    onQrDecode(decodedString) {
      // QR code decoded - search for farmer
      this.showScanner = false;
      this.farmerSearch = decodedString;
      this.searchFarmerById(decodedString);
    },

    toggleCamera() {
      this.cameraFacing = this.cameraFacing === 'environment' ? 'user' : 'environment';
    },

    // ========== FARMER SEARCH ==========
    debouncedFarmerSearch() {
      clearTimeout(this.searchDebounceTimer);
      this.searchDebounceTimer = setTimeout(() => {
        this.searchFarmers();
      }, 300);
    },

    async searchFarmers() {
      if (!this.farmerSearch || this.farmerSearch.length < 2) {
        this.farmerSearchResults = [];
        return;
      }

      this.searchingFarmer = true;
      try {
        const response = await axios.get('/api/farmers/search', {
          params: {
            phone: this.farmerSearch,
            name: this.farmerSearch,
          },
        });

        this.farmerSearchResults = (response.data || []).map((farmer) => ({
          id: farmer.farmerId || farmer.id,
          name: farmer.fullName || farmer.name || farmer.firstName || 'Unknown',
          phoneNumber: farmer.phoneNumber || 'N/A',
        }));
      } catch (error) {
        this.$toast.error('Farmer search error:', error.message);
        this.farmerSearchResults = [];
      } finally {
        this.searchingFarmer = false;
      }
    },

    async searchFarmerById(farmerId) {
      this.searchingFarmer = true;
      try {
        const response = await axios.get('/farmers-service/farmer', {
          params: { farmerId },
        });

        if (response.data?.data) {
          const farmer = response.data.data;
          this.selectFarmer({
            id: farmer.id || farmer.farmerId,
            name: farmer.fullName || `${farmer.firstName} ${farmer.lastName}`.trim(),
            phoneNumber: farmer.phoneNumber || 'N/A',
          });
        } else {
          this.$toast.warning('Farmer not found with that QR code');
        }
      } catch (error) {
        this.$toast.error('Failed to find farmer:', error.message);
      } finally {
        this.searchingFarmer = false;
      }
    },

    async selectFarmer(farmer) {
      this.selectedFarmer = farmer;
      this.farmerSearchResults = [];
      this.farmerSearch = '';
      await this.loadProductionUnits(farmer.id);
    },

    clearFarmer() {
      this.selectedFarmer = null;
      this.farmerProductionUnits = [];
      this.collection.productionUnitId = null;
    },

    // ========== PRODUCTION UNITS ==========
    async loadProductionUnits(farmerId) {
      this.loadingProductionUnits = true;
      this.farmerProductionUnits = [];
      try {
        const response = await axios.get(`/api/production-units/farmer/${farmerId}`);
        const units = response.data?.data || response.data || [];
        this.farmerProductionUnits = units.map((unit) => ({
          ...unit,
          displayName: `${unit.unitName} (${unit.areaHectares || 0} ha)`,
        }));

        // Auto-select if only one unit
        if (this.farmerProductionUnits.length === 1) {
          this.collection.productionUnitId = this.farmerProductionUnits[0].id;
        }
      } catch (error) {
        this.$toast.error('Error loading production units:', error.message);
        this.farmerProductionUnits = [];
      } finally {
        this.loadingProductionUnits = false;
      }
    },

    openAddProductionUnitDialog() {
      this.pendingUnit = {
        unitName: '',
        administrativeRegion: '',
      };
      this.pendingUnitGeometry = null;
      this.pendingUnitArea = 0;
      this.showAddUnitDrawer = true;
    },

    closeUnitDrawer() {
      this.showAddUnitDrawer = false;
      this.pendingUnitGeometry = null;
      this.pendingUnitArea = 0;
    },

    openSaveUnitDialog(unitData) {
      // Called when user clicks "Done" in ProductionUnitDrawer
      this.pendingUnitGeometry = unitData.geometry;
      this.pendingUnitArea = unitData.area;
      this.pendingUnit = {
        unitName: '',
        administrativeRegion: '',
      };
      this.showSaveUnitDialog = true;
    },

    cancelSaveUnit() {
      this.showSaveUnitDialog = false;
      this.pendingUnit = { unitName: '', administrativeRegion: '' };
      this.pendingUnitGeometry = null;
      this.pendingUnitArea = 0;

      // Clear the drawing in the drawer
      if (this.$refs.unitDrawer) {
        this.$refs.unitDrawer.clearDrawing();
      }
    },

    async saveProductionUnit() {
      if (!this.pendingUnit.unitName) {
        this.$toast.error('Please enter a unit name');
        return;
      }

      if (!this.pendingUnitGeometry) {
        this.$toast.error('No polygon geometry available');
        return;
      }

      this.creatingUnit = true;
      try {
        // Round coordinates and ensure polygon is closed (from EditFarmerDetails.vue)
        const roundCoordinate = (coord) => coord.map((val) => parseFloat(val.toFixed(8)));
        const rings = this.pendingUnitGeometry.rings.map((ring) => {
          const roundedRing = ring.map(roundCoordinate);
          const coords = [...roundedRing];
          const first = coords[0];
          const last = coords[coords.length - 1];
          // Ensure ring is closed
          if (first[0] !== last[0] || first[1] !== last[1]) {
            coords.push([...first]);
          }
          return coords;
        });

        const geoJsonPolygon = JSON.stringify({
          type: 'Polygon',
          coordinates: rings,
        });

        const payload = {
          farmerId: this.selectedFarmer.id,
          unitName: this.pendingUnit.unitName,
          administrativeRegion: this.pendingUnit.administrativeRegion || '',
          geoJsonPolygon,
        };

        const response = await axios.post('/api/production-units', payload);
        const newUnit = response.data?.data || response.data;

        this.$toast.success(`"${this.pendingUnit.unitName}" saved! Area: ${this.pendingUnitArea?.toFixed(2)} ha`);

        // Close dialogs
        this.showSaveUnitDialog = false;
        this.showAddUnitDrawer = false;
        this.pendingUnit = { unitName: '', administrativeRegion: '' };
        this.pendingUnitGeometry = null;
        this.pendingUnitArea = 0;

        // Reload production units and auto-select the new one
        await this.loadProductionUnits(this.selectedFarmer.id);
        if (newUnit?.id) {
          this.collection.productionUnitId = newUnit.id;
        }
      } catch (error) {
        this.$toast.error(`Failed: ${error.response?.data?.message || error.message}`);
      } finally {
        this.creatingUnit = false;
      }
    },

    // ========== SUBMIT COLLECTION ==========
    async submitCollection() {
      if (!this.$refs.collectionForm.validate()) return;

      this.submitting = true;
      try {
        const payload = {
          aggregatorId: this.aggregatorId,
          farmerId: this.selectedFarmer.id,
          farmerName: this.selectedFarmer.name,
          productionUnitId: this.collection.productionUnitId,
          produceType: this.collection.produceType,
          quantityKg: this.collection.quantityKg,
          collectionDate: this.collection.collectionDate,
          qualityGrade: this.collection.qualityGrade,
          pricePerKg: this.collection.pricePerKg,
          totalPayment: this.totalPayment,
          paymentStatus: 'PENDING',
          notes: this.collection.notes,
        };

        await axios.post(`/api/v1/aggregators/${this.aggregatorId}/collection-events`, payload);

        this.$toast.success(`Collected ${this.collection.quantityKg} kg from ${this.selectedFarmer.name}`);

        // Add to recent collections list
        this.recentCollections.unshift({
          farmerName: this.selectedFarmer.name,
          quantityKg: this.collection.quantityKg,
          produceType: this.collection.produceType,
          qualityGrade: this.collection.qualityGrade,
        });

        this.sessionCount += 1;

        if (this.quickMode) {
          // Reset for next collection
          this.resetForm();
          this.$nextTick(() => {
            if (this.$refs.quantityField) {
              this.$refs.quantityField.focus();
            }
          });
        } else {
          // Go back to dashboard
          this.$router.push({ name: 'AggregatorDashboard' });
        }
      } catch (error) {
        this.$toast.error(`Failed: ${error.response?.data?.message || error.message}`);
      } finally {
        this.submitting = false;
      }
    },

    resetForm() {
      this.selectedFarmer = null;
      this.farmerProductionUnits = [];
      this.collection = {
        productionUnitId: null,
        produceType: this.collection.produceType, // Keep
        quantityKg: null,
        collectionDate: new Date().toISOString().split('T')[0],
        qualityGrade: this.collection.qualityGrade, // Keep
        pricePerKg: this.collection.pricePerKg, // Keep
        notes: '',
      };
    },

    formatNumber(num) {
      return num ? num.toLocaleString() : '0';
    },
  },
};
</script>

<style scoped>
.aggregator-collection-page {
  padding-top: 16px;
  padding-bottom: 32px;
}

.qr-scanner-container {
  position: relative;
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.qr-scanner-container video {
  width: 100%;
  border-radius: 8px;
}

.production-unit-drawer-container {
  z-index: 999 !important;
}

.production-unit-drawer-container >>> .production-unit-drawer {
  height: 100%;
}
</style>
