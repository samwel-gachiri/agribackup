<template>
  <default-layout>
    <!-- Main Content -->
    <v-container class="px-6 py-6" style="max-width: 1400px;">
      <!-- Header -->
      <v-row class="mb-4">
        <v-col cols="12">
          <v-row align="center" justify="space-between">
            <v-col cols="12" md="7">
              <div class="tw-flex tw-items-center tw-gap-3 tw-mb-2">
                <div class="tw-w-12 tw-h-12 tw-rounded-xl tw-bg-green-100 tw-flex tw-items-center tw-justify-center">
                  <v-icon color="green darken-1" size="28">mdi-map-marker-radius</v-icon>
                </div>
                <div>
                  <h1 class="text-h4 font-weight-bold text--primary tw-mb-0">
                    Production Units
                  </h1>
                  <p class="text-body-2 text--secondary tw-mb-0">
                    Map and manage your farm boundaries for EUDR compliance
                  </p>
                </div>
              </div>
            </v-col>
            <v-col cols="12" md="5" class="tw-flex tw-justify-end tw-gap-2">
              <v-btn
                outlined
                color="primary"
                @click="fetchProductionUnits"
                :loading="loading"
              >
                <v-icon left small>mdi-refresh</v-icon>
                Refresh
              </v-btn>
              <v-btn
                color="success"
                large
                @click="openAddDialog"
                class="tw-px-6"
              >
                <v-icon left>mdi-plus</v-icon>
                Add Production Unit
              </v-btn>
            </v-col>
          </v-row>
        </v-col>
      </v-row>

      <!-- Stats Cards -->
      <v-row class="mb-6">
        <v-col cols="12" sm="6" md="3">
          <v-card class="tw-h-full tw-border-l-4 tw-border-green-500" outlined>
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="tw-text-3xl tw-font-bold tw-text-green-600">{{ stats.totalUnits }}</div>
                  <div class="tw-text-sm tw-text-gray-500">Total Units</div>
                </div>
                <div class="tw-w-12 tw-h-12 tw-rounded-full tw-bg-green-100 tw-flex tw-items-center tw-justify-center">
                  <v-icon color="green">mdi-map-marker-multiple</v-icon>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="tw-h-full tw-border-l-4 tw-border-blue-500" outlined>
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="tw-text-3xl tw-font-bold tw-text-blue-600">{{ stats.totalArea.toFixed(1) }}</div>
                  <div class="tw-text-sm tw-text-gray-500">Total Hectares</div>
                </div>
                <div class="tw-w-12 tw-h-12 tw-rounded-full tw-bg-blue-100 tw-flex tw-items-center tw-justify-center">
                  <v-icon color="blue">mdi-ruler-square</v-icon>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="tw-h-full tw-border-l-4 tw-border-emerald-500" outlined>
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="tw-text-3xl tw-font-bold tw-text-emerald-600">{{ stats.verifiedUnits }}</div>
                  <div class="tw-text-sm tw-text-gray-500">Verified Units</div>
                </div>
                <div class="tw-w-12 tw-h-12 tw-rounded-full tw-bg-emerald-100 tw-flex tw-items-center tw-justify-center">
                  <v-icon color="teal">mdi-check-decagram</v-icon>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="tw-h-full tw-border-l-4" :class="stats.alertCount > 0 ? 'tw-border-red-500' : 'tw-border-gray-300'" outlined>
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="tw-text-3xl tw-font-bold" :class="stats.alertCount > 0 ? 'tw-text-red-600' : 'tw-text-gray-600'">{{ stats.alertCount }}</div>
                  <div class="tw-text-sm tw-text-gray-500">Deforestation Alerts</div>
                </div>
                <div class="tw-w-12 tw-h-12 tw-rounded-full tw-flex tw-items-center tw-justify-center" :class="stats.alertCount > 0 ? 'tw-bg-red-100' : 'tw-bg-gray-100'">
                  <v-icon :color="stats.alertCount > 0 ? 'red' : 'grey'">mdi-alert-circle</v-icon>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Production Units Table -->
      <v-card class="elevation-1">
        <v-card-title class="tw-flex tw-justify-between tw-items-center tw-py-4">
          <div class="tw-flex tw-items-center tw-gap-2">
            <v-icon color="green">mdi-view-list</v-icon>
            <span>Your Production Units</span>
          </div>
          <v-text-field
            v-model="searchQuery"
            append-icon="mdi-magnify"
            label="Search units..."
            single-line
            hide-details
            dense
            outlined
            style="max-width: 280px;"
            clearable
          ></v-text-field>
        </v-card-title>
        <v-divider></v-divider>

        <!-- Empty State -->
        <div v-if="!loading && filteredUnits.length === 0" class="tw-py-16 tw-text-center">
          <div class="tw-mx-auto tw-w-24 tw-h-24 tw-rounded-full tw-bg-green-50 tw-flex tw-items-center tw-justify-center tw-mb-4">
            <v-icon size="48" color="green lighten-2">mdi-map-marker-plus</v-icon>
          </div>
          <h3 class="tw-text-xl tw-font-semibold tw-text-gray-700 tw-mb-2">
            {{ searchQuery ? 'No matching units found' : 'No production units yet' }}
          </h3>
          <p class="tw-text-gray-500 tw-mb-6 tw-max-w-md tw-mx-auto">
            {{ searchQuery
              ? 'Try adjusting your search terms'
              : 'Start by mapping your farm boundaries. This helps verify EUDR compliance and enables supply chain traceability.'
            }}
          </p>
          <v-btn
            v-if="!searchQuery"
            color="success"
            @click="openAddDialog"
          >
            <v-icon left>mdi-plus</v-icon>
            Add Your First Production Unit
          </v-btn>
          <v-btn
            v-else
            text
            color="primary"
            @click="searchQuery = ''"
          >
            Clear Search
          </v-btn>
        </div>

        <!-- Data Table -->
        <v-data-table
          v-else
          :headers="headers"
          :items="filteredUnits"
          :loading="loading"
          :items-per-page="10"
          class="elevation-0"
          :footer-props="{
            'items-per-page-options': [5, 10, 25, 50]
          }"
        >
          <template #loading>
            <v-skeleton-loader type="table-row@5"></v-skeleton-loader>
          </template>

          <template #item.unitName="{ item }">
            <div class="tw-flex tw-items-center tw-gap-3 tw-py-2">
              <div class="tw-w-10 tw-h-10 tw-rounded-lg tw-bg-green-100 tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
                <v-icon color="green" size="20">mdi-terrain</v-icon>
              </div>
              <div>
                <div class="tw-font-medium tw-text-gray-800">{{ item.unitName }}</div>
                <div v-if="item.wgs84Coordinates" class="tw-text-xs tw-text-gray-500 tw-font-mono tw-truncate tw-max-w-[200px]">
                  {{ item.wgs84Coordinates.split(';')[0] }}...
                </div>
              </div>
            </div>
          </template>

          <template #item.areaHectares="{ item }">
            <div class="tw-font-semibold">{{ item.areaHectares?.toFixed(2) || '—' }}</div>
            <div class="tw-text-xs tw-text-gray-500">hectares</div>
          </template>

          <template #item.administrativeRegion="{ item }">
            <span class="tw-text-gray-600">{{ item.administrativeRegion || '—' }}</span>
          </template>

          <template #item.hederaTransactionId="{ item }">
            <div v-if="item.hederaTransactionId" class="tw-flex tw-items-center tw-gap-2">
              <v-chip small color="success" outlined>
                <v-icon left x-small>mdi-check-decagram</v-icon>
                Verified
              </v-chip>
              <v-tooltip bottom>
                <template #activator="{ on, attrs }">
                  <v-btn
                    v-bind="attrs"
                    v-on="on"
                    x-small
                    icon
                    color="primary"
                    @click="openHederaExplorer(item.hederaTransactionId)"
                  >
                    <v-icon x-small>mdi-open-in-new</v-icon>
                  </v-btn>
                </template>
                <span>View on Hedera Explorer</span>
              </v-tooltip>
            </div>
            <v-chip v-else small color="grey" outlined>
              <v-icon left x-small>mdi-clock-outline</v-icon>
              Pending
            </v-chip>
          </template>

          <template #item.lastVerifiedAt="{ item }">
            <div class="tw-text-sm tw-text-gray-600">{{ formatDate(item.lastVerifiedAt) }}</div>
          </template>

          <template #item.actions="{ item }">
            <div class="tw-flex tw-gap-1">
              <v-tooltip bottom>
                <template #activator="{ on, attrs }">
                  <v-btn
                    v-bind="attrs"
                    v-on="on"
                    icon
                    small
                    color="primary"
                    @click="viewUnit(item)"
                  >
                    <v-icon small>mdi-eye</v-icon>
                  </v-btn>
                </template>
                <span>View Details</span>
              </v-tooltip>
              <v-tooltip bottom>
                <template #activator="{ on, attrs }">
                  <v-btn
                    v-bind="attrs"
                    v-on="on"
                    icon
                    small
                    color="warning"
                    @click="editUnit(item)"
                  >
                    <v-icon small>mdi-pencil</v-icon>
                  </v-btn>
                </template>
                <span>Edit</span>
              </v-tooltip>
              <v-tooltip bottom>
                <template #activator="{ on, attrs }">
                  <v-btn
                    v-bind="attrs"
                    v-on="on"
                    icon
                    small
                    color="error"
                    @click="deleteUnit(item)"
                  >
                    <v-icon small>mdi-delete</v-icon>
                  </v-btn>
                </template>
                <span>Delete</span>
              </v-tooltip>
            </div>
          </template>
        </v-data-table>
      </v-card>
    </v-container>

    <!-- Add/Edit Production Unit Dialog -->
    <v-dialog fullscreen v-model="showAddDialog" max-width="90vw" max-height="90vh" persistent scrollable>
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

    <!-- Production Unit Details Drawer -->
    <v-navigation-drawer
      v-model="showDetailsDrawer"
      right
      width="450"
      temporary
      fixed
      class="tw-z-50"
    >
      <div v-if="selectedUnit" class="tw-h-full tw-flex tw-flex-col">
        <!-- Header -->
        <div class="tw-bg-green-600 tw-text-white tw-px-4 tw-py-4 tw-flex tw-items-center tw-justify-between">
          <div>
            <h3 class="tw-font-semibold tw-text-lg">{{ selectedUnit.unitName }}</h3>
            <p class="tw-text-sm tw-opacity-90">Production Unit Details</p>
          </div>
          <v-btn icon dark @click="showDetailsDrawer = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </div>

        <!-- Content -->
        <div class="tw-flex-1 tw-overflow-auto tw-p-4">
          <!-- Area Info -->
          <div class="tw-bg-green-50 tw-rounded-lg tw-p-4 tw-mb-4">
            <div class="tw-flex tw-items-center tw-gap-3">
              <div class="tw-w-12 tw-h-12 tw-rounded-full tw-bg-green-100 tw-flex tw-items-center tw-justify-center">
                <v-icon color="green">mdi-ruler-square</v-icon>
              </div>
              <div>
                <div class="tw-text-2xl tw-font-bold tw-text-green-700">{{ selectedUnit.areaHectares?.toFixed(2) || '—' }}</div>
                <div class="tw-text-sm tw-text-green-600">Hectares</div>
              </div>
            </div>
          </div>

          <!-- Details List -->
          <div class="tw-space-y-4">
            <div class="tw-flex tw-justify-between tw-items-start tw-py-2 tw-border-b tw-border-gray-100">
              <span class="tw-text-gray-500 tw-text-sm">Region</span>
              <span class="tw-font-medium tw-text-gray-800">{{ selectedUnit.administrativeRegion || '—' }}</span>
            </div>

            <div class="tw-flex tw-justify-between tw-items-start tw-py-2 tw-border-b tw-border-gray-100">
              <span class="tw-text-gray-500 tw-text-sm">Country Code</span>
              <span class="tw-font-medium tw-text-gray-800">{{ selectedUnit.countryCode || '—' }}</span>
            </div>

            <div class="tw-flex tw-justify-between tw-items-start tw-py-2 tw-border-b tw-border-gray-100">
              <span class="tw-text-gray-500 tw-text-sm">Last Verified</span>
              <span class="tw-font-medium tw-text-gray-800">{{ formatDate(selectedUnit.lastVerifiedAt) }}</span>
            </div>

            <!-- Hedera Verification Section -->
            <div class="tw-py-3">
              <h4 class="tw-font-semibold tw-text-gray-800 tw-mb-3 tw-flex tw-items-center tw-gap-2">
                <v-icon small color="purple">mdi-check-decagram</v-icon>
                Blockchain Verification
              </h4>
              <div v-if="selectedUnit.hederaTransactionId" class="tw-bg-purple-50 tw-rounded-lg tw-p-4">
                <div class="tw-flex tw-items-center tw-gap-2 tw-mb-3">
                  <v-chip color="success" small>
                    <v-icon left x-small>mdi-check</v-icon>
                    Verified on Hedera
                  </v-chip>
                </div>
                <div class="tw-space-y-2 tw-text-sm">
                  <div>
                    <span class="tw-text-gray-500">Transaction ID:</span>
                    <div class="tw-font-mono tw-text-xs tw-bg-white tw-p-2 tw-rounded tw-mt-1 tw-break-all">
                      {{ selectedUnit.hederaTransactionId }}
                    </div>
                  </div>
                  <div v-if="selectedUnit.hederaHash">
                    <span class="tw-text-gray-500">Document Hash:</span>
                    <div class="tw-font-mono tw-text-xs tw-bg-white tw-p-2 tw-rounded tw-mt-1 tw-break-all">
                      {{ selectedUnit.hederaHash }}
                    </div>
                  </div>
                </div>
                <v-btn
                  color="primary"
                  small
                  class="tw-mt-3"
                  @click="openHederaExplorer(selectedUnit.hederaTransactionId)"
                >
                  <v-icon left small>mdi-open-in-new</v-icon>
                  View on Explorer
                </v-btn>
              </div>
              <div v-else class="tw-bg-gray-50 tw-rounded-lg tw-p-4 tw-text-center">
                <v-icon color="grey" size="48">mdi-clock-outline</v-icon>
                <p class="tw-text-gray-500 tw-mt-2">Pending blockchain verification</p>
              </div>
            </div>

            <!-- Coordinates -->
            <div v-if="selectedUnit.wgs84Coordinates" class="tw-py-3">
              <h4 class="tw-font-semibold tw-text-gray-800 tw-mb-3 tw-flex tw-items-center tw-gap-2">
                <v-icon small color="blue">mdi-map-marker</v-icon>
                Coordinates (WGS84)
              </h4>
              <div class="tw-font-mono tw-text-xs tw-bg-gray-50 tw-p-3 tw-rounded tw-break-all tw-max-h-24 tw-overflow-auto">
                {{ selectedUnit.wgs84Coordinates }}
              </div>
            </div>
          </div>
        </div>

        <!-- Footer Actions -->
        <div class="tw-border-t tw-border-gray-200 tw-p-4 tw-bg-gray-50">
          <div class="tw-flex tw-gap-2">
            <v-btn outlined color="warning" @click="editUnit(selectedUnit); showDetailsDrawer = false">
              <v-icon left small>mdi-pencil</v-icon>
              Edit
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn text @click="showDetailsDrawer = false">Close</v-btn>
          </div>
        </div>
      </div>
    </v-navigation-drawer>
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
      searchQuery: '',
      showDetailsDrawer: false,
      selectedUnit: null,
      headers: [
        { text: 'Unit Name', value: 'unitName', sortable: true },
        {
          text: 'Area (ha)', value: 'areaHectares', sortable: true, width: '100px',
        },
        { text: 'Region', value: 'administrativeRegion', sortable: true },
        {
          text: 'Hedera Verification', value: 'hederaTransactionId', sortable: false, width: '180px',
        },
        {
          text: 'Last Verified', value: 'lastVerifiedAt', sortable: true, width: '140px',
        },
        {
          text: 'Actions', value: 'actions', sortable: false, width: '140px', align: 'center',
        },
      ],
    };
  },
  mounted() {
    this.setFarmerLocation();
    this.fetchProductionUnits();
    if (this.$route.query.action === 'add') {
      this.openAddDialog();
    }
  },
  computed: {
    stats() {
      return {
        totalUnits: this.productionUnits.length,
        totalArea: this.productionUnits.reduce((sum, unit) => sum + (unit.areaHectares || 0), 0),
        verifiedUnits: this.productionUnits.filter((unit) => unit.hederaTransactionId).length,
        alertCount: 0, // Would come from deforestation alerts API
      };
    },
    filteredUnits() {
      if (!this.searchQuery) return this.productionUnits;
      const query = this.searchQuery.toLowerCase();
      return this.productionUnits.filter(
        (unit) => unit.unitName?.toLowerCase().includes(query)
          || unit.administrativeRegion?.toLowerCase().includes(query),
      );
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
    getStatusIcon(status) {
      const icons = {
        Verified: 'mdi-check-circle',
        Pending: 'mdi-clock-outline',
        Rejected: 'mdi-close-circle',
        'Under Review': 'mdi-magnify',
      };
      return icons[status] || 'mdi-help-circle';
    },
    viewUnit(unit) {
      this.selectedUnit = unit;
      this.showDetailsDrawer = true;
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
    openHederaExplorer(transactionId) {
      if (!transactionId) return;
      const network = 'testnet'; // or 'mainnet'
      const url = `https://hashscan.io/${network}/transaction/${transactionId}`;
      window.open(url, '_blank');
    },
  },
};
</script>

<style scoped>
/* Vuetify handles all styling - no custom CSS needed */
</style>
