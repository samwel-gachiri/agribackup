<template>
  <Default>
    <div class="tw-space-y-8 tw-bg-gradient-to-br tw-from-slate-50 tw-via-white tw-to-slate-100 tw-min-h-screen">
      <!-- Header -->
      <div class="tw-flex tw-items-start tw-justify-between tw-flex-wrap tw-gap-6">
        <div class="tw-space-y-2">
          <!-- <div class="tw-flex tw-items-center tw-gap-3">
            <h1 class="tw-text-3xl tw-font-black tw-tracking-tight tw-bg-clip-text tw-text-transparent primary">Farmers Management</h1>
          </div> -->
          <!-- <p class="tw-text-sm tw-text-slate-600 tw-max-w-xl">Operational intelligence for your farmer network: locations, produce readiness, and harvest projections in one unified workspace.</p> -->
          <div class="tw-flex tw-gap-2 tw-flex-wrap">
            <v-btn color="primary" :loading="loadingFarmers" @click="refreshCurrent" class="tw-rounded-xl tw-shadow-sm tw-font-medium" small>
              <v-icon left small>mdi-refresh</v-icon>Refresh
            </v-btn>
            <v-btn color="success" small class="tw-rounded-xl tw-shadow-sm tw-font-medium" @click="openAddFarmerDialog">
              <v-icon left small>mdi-account-plus</v-icon>Add Farmer
            </v-btn>
            <v-btn small outlined class="tw-rounded-xl tw-font-medium" @click="clearAllFilters">
              <v-icon left small>mdi-filter-remove</v-icon>Reset Filters
            </v-btn>
            <v-btn small outlined class="tw-rounded-xl tw-font-medium" @click="toggleView">
              <v-icon left small>{{ viewMode==='list' ? 'mdi-view-grid' : 'mdi-format-list-bulleted' }}</v-icon>{{ viewMode==='list' ? 'Grid View' : 'List View' }}
            </v-btn>
          </div>
        </div>
        <div class="tw-grid tw-grid-cols-2 md:tw-grid-cols-5 tw-gap-3 tw-w-full md:tw-w-auto">
          <div v-for="m in metrics" :key="m.key" class="tw-bg-white tw-rounded-2xl tw-border tw-border-slate-200 tw-px-4 tw-py-3 tw-flex tw-flex-col tw-gap-1 tw-shadow-sm hover:tw-shadow transition">
            <div class="tw-text-[11px] tw-font-semibold tw-tracking-wide tw-text-slate-500 tw-uppercase">{{ m.label }}</div>
            <div class="tw-flex tw-items-end tw-gap-2">
              <div class="tw-text-xl tw-font-bold tw-text-slate-800">{{ m.value }}</div>
              <v-chip v-if="m.delta" x-small :color="m.delta>0 ? 'green' : 'grey'" text-color="white" class="tw-font-medium">{{ m.delta>0? '+'+m.delta : m.delta }}</v-chip>
            </div>
            <div v-if="m.sub" class="tw-text-[10px] tw-text-slate-400 tw-font-medium">{{ m.sub }}</div>
          </div>
        </div>
      </div>

      <!-- Filters Card -->
      <div class="tw-bg-white/80 tw-backdrop-blur-sm tw-shadow-sm tw-rounded-2xl tw-border tw-border-slate-200 tw-p-6 tw-space-y-6">
        <div class="tw-grid md:tw-grid-cols-4 tw-gap-5">
          <!-- Zone Selector -->
          <div>
            <label class="tw-block tw-font-medium tw-text-gray-700 tw-mb-2">Zone</label>
            <div class="tw-relative">
              <select
                v-model="selectedZoneId"
                @focus="fetchZonesIfNeeded"
                @change="onZoneChange"
                class="tw-w-full tw-p-3 tw-bg-gray-50 tw-rounded-lg tw-border tw-border-gray-300 focus:tw-outline-none focus:tw-border-blue-500 focus:tw-bg-white tw-transition"
                :disabled="loadingZones"
              >
                <option :value="null" disabled>Select zone</option>
                <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.name }}</option>
              </select>
              <div v-if="loadingZones && !zones.length" class="tw-absolute tw-inset-y-0 tw-right-3 tw-flex tw-items-center">
                <v-progress-circular indeterminate color="primary" size="18" width="2" />
              </div>
            </div>
          </div>
          <!-- Search -->
          <div class="md:tw-col-span-2">
            <label class="tw-block tw-font-medium tw-text-gray-700 tw-mb-2">Search</label>
            <input
              v-model.trim="search"
              type="text"
              placeholder="Search by name or phone"
              class="tw-w-full tw-p-3 tw-bg-gray-50 tw-rounded-lg tw-border tw-border-gray-300 focus:tw-outline-none focus:tw-border-blue-500 focus:tw-bg-white tw-transition"
            />
          </div>
          <!-- Extra Filters -->
          <div>
            <label class="tw-block tw-font-medium tw-text-gray-700 tw-mb-2">Filter</label>
            <select v-model="statusFilter" class="tw-w-full tw-p-3 tw-bg-gray-50 tw-rounded-lg tw-border tw-border-gray-300 focus:tw-outline-none tw-transition">
              <option value="">All</option>
              <option value="withLocation">With Location</option>
              <option value="missingLocation">Missing Location</option>
            </select>
          </div>
        </div>
        <div class="tw-flex tw-flex-wrap tw-gap-3 tw-items-center">
          <v-chip v-if="selectedZone" close small color="primary" outlined @click:close="clearZone">
            Zone: {{ selectedZone?.name }}
          </v-chip>
          <v-chip v-if="search" close small color="indigo" outlined @click:close="search = ''">Search: {{ search }}</v-chip>
          <v-chip v-if="statusFilter" close small color="teal" outlined @click:close="statusFilter = ''">Filter: {{ readableStatus }}</v-chip>
          <div v-if="produceStatuses.length">
            <v-chip
              v-for="ps in produceStatuses"
              :key="ps.value"
              small
              :outlined="produceStatusFilter!==ps.value"
              :color="produceStatusFilter===ps.value ? ps.color : 'grey'"
              class="tw-cursor-pointer tw-font-medium"
              @click="toggleProduceStatus(ps.value)"
            >
              <v-icon left x-small :color="produceStatusFilter===ps.value ? 'white' : ps.color">mdi-leaf</v-icon>{{ ps.label }}
            </v-chip>
          </div>
          <div v-if="!selectedZone && !search && !statusFilter" class="tw-text-xs tw-text-gray-500">No filters applied</div>
        </div>
      </div>

      <!-- Content Split -->
      <div class="tw-grid lg:tw-grid-cols-3 tw-gap-8">
        <!-- Farmers List -->
        <div class="lg:tw-col-span-2 tw-space-y-4">
          <div class="tw-bg-white/90 tw-backdrop-blur-sm tw-rounded-2xl tw-border tw-border-slate-200 tw-shadow-sm tw-overflow-hidden" style="min-height:420px;display:flex;flex-direction:column;">
            <div class="tw-flex tw-items-center tw-justify-between tw-px-6 tw-py-4 tw-bg-gradient-to-r tw-from-blue-700 tw-to-indigo-600 tw-text-white">
              <div class="tw-font-semibold tw-text-sm md:tw-text-base tw-flex tw-items-center tw-gap-2">
                <v-icon small>mdi-account-group</v-icon>
                Farmers <span class="tw-text-white/80 tw-text-xs">({{ filteredFarmers.length }})</span>
              </div>
              <div class="tw-flex tw-items-center tw-gap-2">
                <v-btn small text class="tw-text-white tw-font-medium tw-rounded-lg" @click="exportCsv" :disabled="!filteredFarmers.length">
                  <v-icon left small>mdi-download</v-icon> Export CSV
                </v-btn>
              </div>
            </div>
            <div v-if="loadingFarmers" class="tw-px-6 tw-pt-6 tw-grid tw-grid-cols-1 md:tw-grid-cols-2 xl:tw-grid-cols-3 tw-gap-4 tw-w-full" style="flex:1;">
              <div v-for="n in 6" :key="n" class="tw-animate-pulse tw-rounded-xl tw-bg-slate-100 tw-h-32 tw-border tw-border-slate-200"></div>
            </div>
            <div v-else style="flex:1;display:flex;flex-direction:column;">
              <div v-if="!filteredFarmers.length" class="tw-p-10 tw-text-center tw-text-slate-500 tw-text-sm">No farmers match the current filters.</div>
              <!-- CARD GRID MODE -->
              <div v-if="viewMode==='cards'" class="tw-px-6 tw-py-5 tw-grid sm:tw-grid-cols-2 xl:tw-grid-cols-3 tw-gap-5" style="flex:1;overflow:auto;">
                <div v-for="farmer in paginatedFarmers" :key="farmer.id" class="tw-group tw-relative tw-rounded-xl tw-border tw-border-slate-200 tw-bg-gradient-to-br tw-from-white tw-to-slate-50 hover:tw-from-white hover:tw-to-white tw-shadow-sm hover:tw-shadow transition tw-p-4 tw-flex tw-flex-col tw-gap-3">
                  <div class="tw-flex tw-justify-between tw-items-start">
                    <div class="tw-flex tw-items-center tw-gap-2 tw-font-semibold tw-text-slate-800">
                      <v-icon x-small color="green" v-if="hasLocation(farmer)">mdi-map-marker</v-icon>
                      <span class="tw-text-sm tw-leading-tight">{{ displayName(farmer) }}</span>
                    </div>
                    <v-chip x-small :color="hasLocation(farmer) ? 'green' : 'grey'" text-color="white" class="tw-font-semibold tw-uppercase tw-tracking-wide">{{ hasLocation(farmer) ? 'Loc' : 'No-Loc' }}</v-chip>
                  </div>
                  <div class="tw-text-[11px] tw-text-slate-500 tw-flex tw-flex-wrap tw-gap-x-3 tw-gap-y-1">
                    <div class="tw-flex tw-items-center tw-gap-1"><v-icon x-small color="indigo">mdi-phone</v-icon>{{ farmer.phoneNumber || farmer.phone || '—' }}</div>
                    <div class="tw-flex tw-items-center tw-gap-1" v-if="summarizeProduces(farmer) !== '—'"><v-icon x-small color="orange">mdi-leaf</v-icon>{{ summarizeProduces(farmer) }}</div>
                    <div class="tw-flex tw-items-center tw-gap-1" v-if="expectedHarvestSummary(farmer) !== '—'"><v-icon x-small color="emerald">mdi-calendar-clock</v-icon>{{ expectedHarvestSummary(farmer) }}</div>
                  </div>
                  <div class="tw-flex tw-items-center tw-justify-end tw-gap-2 tw-mt-auto">
                    <v-btn icon x-small color="primary" @click="openProfile(farmer)"><v-icon x-small>mdi-account-box</v-icon></v-btn>
                    <v-btn icon x-small color="orange" @click="openEditFarmerDialog(farmer)"><v-icon x-small>mdi-pencil</v-icon></v-btn>
                    <v-btn icon x-small color="indigo" :disabled="!hasLocation(farmer)" @click="focusFarmerOnMap(farmer)"><v-icon x-small>mdi-map-search</v-icon></v-btn>
                  </div>
                  <div class="tw-absolute -tw-top-2 -tw-left-2 tw-bg-white tw-rounded-full tw-shadow tw-border tw-border-slate-200 tw-px-2 tw-py-0.5 tw-text-[10px] tw-font-semibold tw-text-slate-500">ID {{ farmer.id.substring(0,4) }}</div>
                </div>
              </div>
              <!-- LIST MODE -->
              <ul v-else class="tw-divide-y tw-bg-slate-50/60" style="flex:1;overflow:auto;">
                <li v-for="farmer in paginatedFarmers" :key="farmer.id" class="tw-p-4 tw-grid md:tw-grid-cols-12 tw-gap-4 hover:tw-bg-white tw-transition">
                  <div class="md:tw-col-span-4">
                    <div class="tw-font-semibold tw-text-gray-800 tw-flex tw-items-center tw-gap-2">
                      <v-icon small color="green" v-if="hasLocation(farmer)">mdi-map-marker</v-icon>
                      {{ displayName(farmer) }}
                    </div>
                    <div class="tw-text-xs tw-text-gray-500">ID: {{ farmer.id }}</div>
                  </div>
                  <div class="md:tw-col-span-2 tw-text-sm">
                    <div class="tw-text-gray-700">{{ farmer.phoneNumber || farmer.phone || '—' }}</div>
                    <div class="tw-text-xs tw-text-gray-500">Phone</div>
                  </div>
                  <div class="md:tw-col-span-3 tw-text-sm">
                    <div class="tw-text-gray-700">{{ summarizeProduces(farmer) }}</div>
                    <div class="tw-text-xs tw-text-gray-500">Produces</div>
                  </div>
                  <div class="md:tw-col-span-2 tw-text-sm">
                    <div class="tw-text-gray-700">{{ expectedHarvestSummary(farmer) }}</div>
                    <div class="tw-text-xs tw-text-gray-500">Expected Harvest</div>
                  </div>
                  <div class="md:tw-col-span-1 tw-flex tw-items-center tw-gap-2 tw-justify-end">
                    <v-tooltip top>
                      <template #activator="{ on, attrs }">
                        <v-btn v-bind="attrs" v-on="on" small icon color="primary" @click="openProfile(farmer)">
                          <v-icon small>mdi-account-box</v-icon>
                        </v-btn>
                      </template>
                      <span>Open Profile</span>
                    </v-tooltip>
                    <v-tooltip top>
                      <template #activator="{ on, attrs }">
                        <v-btn v-bind="attrs" v-on="on" small icon color="orange" @click="openEditFarmerDialog(farmer)">
                          <v-icon small>mdi-pencil</v-icon>
                        </v-btn>
                      </template>
                      <span>Edit Details</span>
                    </v-tooltip>
                    <v-tooltip top>
                      <template #activator="{ on, attrs }">
                        <v-btn v-bind="attrs" v-on="on" small icon color="blue" @click="openFarmerDocuments(farmer)">
                          <v-icon small>mdi-file-document-multiple</v-icon>
                        </v-btn>
                      </template>
                      <span>EUDR Documents</span>
                    </v-tooltip>
                    <v-tooltip top>
                      <template #activator="{ on, attrs }">
                        <v-btn v-bind="attrs" v-on="on" small icon color="indigo" :disabled="!hasLocation(farmer)" @click="focusFarmerOnMap(farmer)">
                          <v-icon small>mdi-map-search</v-icon>
                        </v-btn>
                      </template>
                      <span>Focus on Map</span>
                    </v-tooltip>
                  </div>
                </li>
              </ul>
              <!-- Pagination -->
              <div v-if="pages > 1" class="tw-flex tw-items-center tw-justify-between tw-p-4 tw-bg-white tw-border-t tw-border-gray-200">
                <div class="tw-text-xs tw-text-gray-500">Page {{ page }} / {{ pages }}</div>
                <div class="tw-flex tw-gap-2">
                  <v-btn small outlined :disabled="page===1" @click="page--">Prev</v-btn>
                  <v-btn small outlined :disabled="page===pages" @click="page++">Next</v-btn>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Map / Details Side Panel -->
        <div class="tw-space-y-4">
          <div class="tw-bg-white/90 tw-backdrop-blur-sm tw-rounded-2xl tw-border tw-border-slate-200 tw-shadow-sm tw-overflow-hidden">
            <div class="tw-bg-gradient-to-r tw-from-teal-600 tw-to-emerald-600 tw-text-white tw-px-5 tw-py-3 tw-font-semibold tw-flex tw-items-center tw-justify-between">
              <span class="tw-flex tw-items-center tw-gap-2"><v-icon small>mdi-map</v-icon> Farmers Map</span>
              <div class="tw-flex tw-items-center tw-gap-2">
                <v-btn icon small text class="tw-text-white" :disabled="!selectedZone || !farmers.length" @click="fitAllFarmers">
                  <v-icon small>mdi-crosshairs-gps</v-icon>
                </v-btn>
              </div>
            </div>
            <div class="tw-relative" style="height:400px;">
              <div v-if="!mapView" class="tw-absolute tw-inset-0 tw-flex tw-items-center tw-justify-center tw-text-gray-400 tw-text-sm">Map initializing...</div>
              <div :id="mapId" class="tw-w-full tw-h-full tw-bg-slate-200 tw-rounded-b-2xl"></div>
              <div v-if="mapView" class="tw-absolute tw-bottom-3 tw-right-3 tw-bg-white/90 tw-backdrop-blur tw-rounded-xl tw-shadow tw-border tw-border-slate-200 tw-px-3 tw-py-2 tw-flex tw-flex-col tw-gap-1 tw-text-[10px] tw-font-medium tw-text-slate-600">
                <div class="tw-flex tw-items-center tw-gap-1"><span class="tw-inline-block tw-w-2 tw-h-2 tw-rounded-full tw-bg-green-500"></span> Farmer</div>
                <div class="tw-flex tw-items-center tw-gap-1"><span class="tw-inline-block tw-w-2 tw-h-2 tw-rounded-full tw-bg-blue-500"></span> Zone Center (future)</div>
              </div>
            </div>
          </div>
          <div v-if="selectedFarmer" class="tw-bg-white tw-rounded-xl tw-border tw-border-gray-200 tw-shadow-sm tw-p-5 tw-space-y-2">
            <div class="tw-flex tw-items-center tw-justify-between">
              <h3 class="tw-font-semibold tw-text-gray-800 tw-text-lg">Selected Farmer</h3>
              <v-btn icon small @click="selectedFarmer=null"><v-icon small>mdi-close</v-icon></v-btn>
            </div>
            <div class="tw-text-sm tw-text-gray-700 tw-font-medium">{{ displayName(selectedFarmer) }}</div>
            <div class="tw-text-xs tw-text-gray-500">{{ selectedFarmer.phoneNumber || selectedFarmer.phone || 'No phone' }}</div>
            <div class="tw-text-xs tw-text-gray-500" v-if="hasLocation(selectedFarmer)">Lat: {{ selectedFarmer.latitude }}, Lng: {{ selectedFarmer.longitude }}</div>
            <div class="tw-flex tw-gap-2 tw-mt-2">
              <v-btn small color="primary" @click="openProfile(selectedFarmer)"><v-icon left small>mdi-account-box</v-icon> Profile</v-btn>
              <v-btn small color="blue" @click="openFarmerDocuments(selectedFarmer)"><v-icon left small>mdi-file-document-multiple</v-icon> Documents</v-btn>
              <v-btn small color="orange" @click="openEditFarmerDialog(selectedFarmer)"><v-icon left small>mdi-pencil</v-icon> Edit</v-btn>
              <v-btn small outlined color="indigo" :disabled="!hasLocation(selectedFarmer)" @click="focusFarmerOnMap(selectedFarmer)"><v-icon left small>mdi-map-marker</v-icon> Locate</v-btn>
            </div>
          </div>
        </div>
      </div>

      <!-- Snackbar -->
      <v-snackbar v-model="snackbar.show" :timeout="4500" top right :color="snackbarColor(snackbar.color)" elevation="6">
        <div class="tw-flex tw-items-center tw-gap-2">
          <v-icon small v-if="snackbar.color==='success'">mdi-check-circle</v-icon>
          <v-icon small v-else-if="snackbar.color==='error'">mdi-alert-circle</v-icon>
          <v-icon small v-else-if="snackbar.color==='warning'">mdi-alert</v-icon>
          <v-icon small v-else>mdi-information</v-icon>
          <span class="tw-font-medium tw-text-sm">{{ snackbar.message }}</span>
          <v-spacer></v-spacer>
          <v-btn icon small text @click="snackbar.show=false"><v-icon small>mdi-close</v-icon></v-btn>
        </div>
      </v-snackbar>
      <!-- Assisted Farmer Registration Dialog -->
      <AssistedFarmerRegistration
        v-if="selectedZone"
        v-model="showAddFarmerDialog"
        :zoneId="selectedZone?.id"
        :zone="selectedZone"
        @closed="handleFarmerDialogClosed"
      />

      <!-- Edit Farmer Details Dialog -->
       <v-dialog
        v-model="showEditFarmerDialog"
        fullscreen
        persistent
        scrollable
        >
        <EditFarmerDetails
          v-if="farmerToEdit"
          v-model="showEditFarmerDialog"
          :farmer="farmerToEdit"
          @closed="handleEditFarmerDialogClosed"
          @saved="handleFarmerSaved"
        />
      </v-dialog>

      <!-- Farmer Documents Dialog -->
      <v-dialog v-model="showDocumentsDialog" max-width="900" scrollable>
        <v-card v-if="selectedDocumentFarmer" class="tw-rounded-2xl">
          <v-card-title class="tw-bg-gradient-to-r tw-from-blue-600 tw-to-indigo-600 tw-text-white">
            <div class="tw-flex tw-items-center tw-justify-between tw-w-full">
              <div class="tw-flex tw-items-center tw-gap-3">
                <v-icon color="white">mdi-file-document-multiple</v-icon>
                <span>EUDR Documents - {{ displayName(selectedDocumentFarmer) }}</span>
              </div>
              <v-btn icon dark @click="showDocumentsDialog = false">
                <v-icon>mdi-close</v-icon>
              </v-btn>
            </div>
          </v-card-title>
          <v-card-text class="tw-p-6 tw-space-y-4">
            <!-- Upload Section -->
            <div class="tw-border-2 tw-border-dashed tw-border-gray-300 tw-rounded-xl tw-p-6 tw-text-center hover:tw-border-blue-500 tw-transition">
              <input
                ref="fileInput"
                type="file"
                multiple
                accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
                @change="handleFileUpload"
                class="tw-hidden"
              />
              <v-icon size="48" color="blue">mdi-cloud-upload</v-icon>
              <div class="tw-mt-3 tw-text-sm tw-font-medium tw-text-gray-700">
                Upload EUDR Compliance Documents
              </div>
              <div class="tw-mt-1 tw-text-xs tw-text-gray-500">
                Supported: PDF, JPG, PNG, DOC, DOCX (Max 10MB per file)
              </div>
              <v-btn
                color="blue"
                class="tw-mt-4"
                @click="$refs.fileInput.click()"
                :loading="uploadingDocument"
              >
                <v-icon left>mdi-file-plus</v-icon>
                Select Files
              </v-btn>
            </div>

            <!-- Documents List -->
            <div v-if="farmerDocuments.length > 0" class="tw-space-y-3">
              <h3 class="tw-text-sm tw-font-semibold tw-text-gray-700 tw-uppercase">Uploaded Documents</h3>
              <div
                v-for="doc in farmerDocuments"
                :key="doc.id"
                class="tw-flex tw-items-center tw-justify-between tw-p-4 tw-bg-gray-50 tw-rounded-xl tw-border tw-border-gray-200 hover:tw-shadow tw-transition"
              >
                <div class="tw-flex tw-items-center tw-gap-3">
                  <v-icon :color="getFileIcon(doc.fileName).color">
                    {{ getFileIcon(doc.fileName).icon }}
                  </v-icon>
                  <div>
                    <div class="tw-text-sm tw-font-medium tw-text-gray-900">{{ doc.fileName }}</div>
                    <div class="tw-text-xs tw-text-gray-500">
                      Uploaded {{ formatDate(doc.uploadedAt) }} • {{ doc.fileSize }}
                    </div>
                  </div>
                </div>
                <div class="tw-flex tw-items-center tw-gap-2">
                  <v-btn icon small color="blue" @click="downloadDocument(doc)">
                    <v-icon small>mdi-download</v-icon>
                  </v-btn>
                  <v-btn icon small color="red" @click="deleteDocument(doc)">
                    <v-icon small>mdi-delete</v-icon>
                  </v-btn>
                </div>
              </div>
            </div>
            <div v-else class="tw-text-center tw-py-8 tw-text-gray-500 tw-text-sm">
              No documents uploaded yet
            </div>
          </v-card-text>
        </v-card>
      </v-dialog>
    </div>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';
import { getCurrentUserId } from '@/utils/roles.js';
import AssistedFarmerRegistration from '@/components/AssistedFarmerRegistration.vue';
import EditFarmerDetails from '@/components/exporter/EditFarmerDetails.vue';

export default {
  name: 'FarmersManagement',
  components: { Default, AssistedFarmerRegistration, EditFarmerDetails },
  data() {
    return {
      zones: [],
      selectedZoneId: null,
      loadingZones: false,
      farmers: [],
      loadingFarmers: false,
      search: '',
      statusFilter: '',
      produceStatusFilter: '',
      snackbar: { show: false, message: '', color: 'info' },
      page: 1,
      perPage: 15,
      mapId: `farmers-map-${Math.random().toString(36).substr(2, 8)}`,
      mapView: null,
      EsriMap: null,
      EsriMapView: null,
      EsriGraphic: null,
      EsriPoint: null,
      selectedFarmer: null,
      showAddFarmerDialog: false,
      pendingOpenAddFarmer: false,
      viewMode: 'list',
      showEditFarmerDialog: false,
      farmerToEdit: null,
      showDocumentsDialog: false,
      selectedDocumentFarmer: null,
      farmerDocuments: [],
      uploadingDocument: false,
    };
  },
  computed: {
    selectedZone() {
      return this.zones.find((z) => z.id === this.selectedZoneId) || null;
    },
    filteredFarmers() {
      let list = this.farmers;
      if (this.statusFilter) {
        list = list.filter((f) => (this.statusFilter === 'withLocation' ? this.hasLocation(f) : !this.hasLocation(f)));
      }
      if (this.search) {
        const q = this.search.toLowerCase();
        list = list.filter((f) => (this.displayName(f) || '').toLowerCase().includes(q) || (f.phoneNumber || f.phone || '').toLowerCase().includes(q));
      }
      if (this.produceStatusFilter) {
        list = list.filter((f) => (f.produces || []).some((p) => p.status === this.produceStatusFilter));
      }
      return list;
    },
    paginatedFarmers() {
      const start = (this.page - 1) * this.perPage;
      return this.filteredFarmers.slice(start, start + this.perPage);
    },
    pages() { return Math.ceil(this.filteredFarmers.length / this.perPage) || 1; },
    readableStatus() {
      switch (this.statusFilter) {
        case 'withLocation': return 'With Location';
        case 'missingLocation': return 'Missing Location';
        default: return '';
      }
    },
    produceStatuses() {
      // Derive available statuses from loaded produces
      const all = new Set();
      this.farmers.forEach((f) => (f.produces || []).forEach((p) => p.status && all.add(p.status)));
      const palette = {
        ON_FARM: 'green',
        ON_SALE: 'orange',
        HARVEST_PLANNED: 'teal',
        HARVESTED: 'purple',
        INACTIVE: 'grey',
      };
      return Array.from(all).sort().map((s) => ({ value: s, label: s.replace(/_/g, ' '), color: palette[s] || 'indigo' }));
    },
    metrics() {
      const total = this.farmers.length;
      const withLoc = this.farmers.filter((f) => this.hasLocation(f)).length;
      const withoutLoc = total - withLoc;
      const uniqueProduce = new Set();
      this.farmers.forEach((f) => (f.produces || []).forEach((p) => p.name && uniqueProduce.add(p.name)));
      const earliestHarvest = this.farmers
        .flatMap((f) => (f.expectedHarvests || []).map((h) => h.predictedHarvestDate).filter(Boolean))
        .sort()[0];
      return [
        { key: 'total', label: 'Total Farmers', value: total },
        {
          key: 'withLoc', label: 'With Location', value: withLoc, sub: withoutLoc ? `${withoutLoc} w/out` : '',
        },
        { key: 'uniqueProd', label: 'Unique Produces', value: uniqueProduce.size },
        { key: 'harvest', label: 'Earliest Harvest', value: earliestHarvest || '—' },
        { key: 'avgPerZone', label: 'Avg / Zone', value: this.zones.length ? Math.round((total / this.zones.length) * 10) / 10 : 0 },
      ];
    },
  },
  watch: {
    filteredFarmers() { if (this.page > this.pages) this.page = 1; },
  },
  async mounted() {
    // await this.loadArcGIS();
    // this.initMap();
    // Wait a tick for auth store to populate; retry a few times if user not yet present
    this.deferFetchZones();
  },
  methods: {
    ensureUserReady(retries = 5) {
      const user = this.$store?.state?.auth?.user;
      if (user && (user.id || user.uid)) return true;
      if (retries <= 0) return false;
      return false;
    },
    deferFetchZones(attempt = 0) {
      const ready = this.ensureUserReady();
      if (!ready && attempt < 5) {
        setTimeout(() => this.deferFetchZones(attempt + 1), 200);
        return;
      }
      this.fetchZones();
    },
    async fetchZones(force = false) {
      if (this.loadingZones) return;
      if (this.zones.length && !force) return; // already have zones
      this.loadingZones = true;
      try {
        const exporterResp = await axios.get(`/api/admin-service/zones/exporter/${getCurrentUserId()}`);
        let zones = exporterResp.data.data || [];
        if (!zones.length) {
          const generalResp = await axios.get('/api/admin-service/zones');
          zones = generalResp.data.data || [];
        }
        this.zones = zones;
        if (!this.selectedZoneId && this.zones.length) {
          // Defer selection to next tick to allow dependent reactive state (like auth) to stabilize
          this.$nextTick(() => {
            this.selectedZoneId = this.zones[0].id;
            this.fetchFarmers();
            if (this.pendingOpenAddFarmer) {
              this.pendingOpenAddFarmer = false;
              this.showAddFarmerDialog = true;
            }
          });
        } else if (this.pendingOpenAddFarmer && this.selectedZone) {
          // Zone already selected; honor pending open
          this.pendingOpenAddFarmer = false;
          this.$nextTick(() => { this.showAddFarmerDialog = true; });
        }
      } catch (e) {
        this.showSnackbar('Failed to fetch zones', 'error');
      } finally { this.loadingZones = false; }
    },
    fetchZonesIfNeeded() { this.fetchZones(false); },
    async fetchFarmers() {
      if (!this.selectedZone) return;
      this.loadingFarmers = true;
      try {
        const response = await axios.get(`/api/admin-service/zones/${this.selectedZone.id}/farmers`);
        const raw = response.data.data || [];
        // Normalize/flatten for UI convenience
        this.farmers = raw.map((f) => ({
          ...f,
          id: f.farmerId,
          farmerId: undefined,
          // Flatten nested location -> latitude/longitude for existing map logic
          latitude: f.latitude || f.location?.latitude || f.location?.lat || null,
          longitude: f.longitude || f.location?.longitude || f.location?.lng || null,
          produces: Array.isArray(f.produces) ? f.produces : [],
          expectedHarvests: Array.isArray(f.expectedHarvests) ? f.expectedHarvests : [],
        }));
        this.page = 1;
        // this.renderFarmersOnMap();
        if (!this.farmers.length) this.showSnackbar('No farmers found in this zone', 'info');
      } catch (e) {
        this.showSnackbar('Failed to load farmers', 'error');
        this.farmers = [];
        this.renderFarmersOnMap();
      } finally { this.loadingFarmers = false; }
    },
    onZoneChange() {
      this.farmers = [];
      this.selectedFarmer = null;
      this.renderFarmersOnMap();
      this.fetchFarmers();
    },
    clearZone() { this.selectedZoneId = null; this.farmers = []; this.renderFarmersOnMap(); },
    displayName(f) { return f?.farmerName || f?.name || 'Unnamed'; },
    summarizeProduces(f) {
      if (f?.produces && Array.isArray(f.produces) && f.produces.length) {
        const first = f.produces.slice(0, 2).map((p) => p.name || p.produceType || p.type).join(', ');
        return first + (f.produces.length > 2 ? '…' : '');
      }
      return f?.produceType || '—';
    },
    expectedHarvestSummary(f) {
      if (!f?.expectedHarvests || !f.expectedHarvests.length) return '—';
      // Pick earliest predictedHarvestDate
      const dates = f.expectedHarvests
        .map((h) => h.predictedHarvestDate)
        .filter(Boolean)
        .sort();
      if (!dates.length) return '—';
      return dates[0];
    },
    hasLocation(f) { return !!(f && (f.latitude) && (f.longitude)); },
    openProfile(f) {
      if (!f) return;
      this.$router.push({ name: 'Produces', params: { farmerId: f.id } });
    },
    openAddFarmerDialog() {
      // If zones still loading, mark intent and wait
      if (this.loadingZones) { this.pendingOpenAddFarmer = true; return; }
      // If no zones loaded yet, trigger fetch and remember to open
      if (!this.zones.length) {
        this.pendingOpenAddFarmer = true;
        this.fetchZones(true);
        return;
      }
      // Ensure a zone is selected
      if (!this.selectedZoneId) {
        this.selectedZoneId = this.zones[0].id;
        // fetch farmers but don't block dialog opening
        this.fetchFarmers();
      }
      // Open after next tick so computed selectedZone is truthy for v-if
      this.$nextTick(() => { this.showAddFarmerDialog = true; });
    },
    handleFarmerDialogClosed() { this.showAddFarmerDialog = false; this.fetchFarmers(); },
    openEditFarmerDialog(farmer) {
      if (!farmer) return;
      this.farmerToEdit = farmer;
      this.showEditFarmerDialog = true;
    },
    handleEditFarmerDialogClosed() {
      this.showEditFarmerDialog = false;
      this.farmerToEdit = null;
    },
    handleFarmerSaved() {
      this.showSnackbar('Farmer details updated successfully', 'success');
      this.fetchFarmers();
    },
    focusFarmerOnMap(f) {
      if (!this.mapView || !this.hasLocation(f)) return;
      const lon = f.longitude || f.lng; const lat = f.latitude || f.lat;
      this.mapView.goTo({ center: [lon, lat], zoom: 14 }).catch(() => {});
      this.selectedFarmer = f;
    },
    exportCsv() {
      const rows = [['ID', 'Name', 'Phone', 'Latitude', 'Longitude', 'Produces', 'ExpectedHarvestEarliest']];
      this.filteredFarmers.forEach((f) => rows.push([
        f.id,
        this.displayName(f),
        f.phoneNumber || f.phone || '',
        f.latitude || '',
        f.longitude || '',
        this.summarizeProduces(f),
        this.expectedHarvestSummary(f),
      ]));
      const csv = rows.map((r) => r.map((v) => `"${String(v).replace(/"/g, '""')}"`).join(',')).join('\n');
      const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a'); a.href = url; a.download = 'farmers.csv'; a.click(); URL.revokeObjectURL(url);
      this.showSnackbar('Export complete', 'success');
    },
    toggleView() { this.viewMode = this.viewMode === 'list' ? 'cards' : 'list'; },
    toggleProduceStatus(val) { this.produceStatusFilter = this.produceStatusFilter === val ? '' : val; },
    clearAllFilters() { this.search = ''; this.statusFilter = ''; this.produceStatusFilter = ''; this.clearZone(); },
    refreshCurrent() { if (this.selectedZone) this.fetchFarmers(); else this.fetchZones(); },
    showSnackbar(message, color = 'info') { this.snackbar = { show: true, message, color }; },
    snackbarColor(kind) {
      switch (kind) {
        case 'success': return 'green lighten-1';
        case 'error': return 'red lighten-1';
        case 'warning': return 'amber darken-2';
        case 'info': return 'blue lighten-1';
        default: return 'primary';
      }
    },
    async loadArcGIS() {
      if (!window.require) {
        await new Promise((resolve, reject) => {
          const s = document.createElement('script'); s.src = 'https://js.arcgis.com/4.25/init.js'; s.onload = resolve; s.onerror = reject; document.head.appendChild(s);
          const l = document.createElement('link'); l.rel = 'stylesheet'; l.href = 'https://js.arcgis.com/4.25/esri/themes/light/main.css'; document.head.appendChild(l);
        });
      }
      if (!this.EsriMap) {
        await new Promise((resolve, reject) => {
          try { window.require(['esri/Map', 'esri/views/MapView', 'esri/Graphic', 'esri/geometry/Point'], (Map, MapView, Graphic, Point) => { this.EsriMap = Map; this.EsriMapView = MapView; this.EsriGraphic = Graphic; this.EsriPoint = Point; resolve(); }); } catch (e) { reject(e); }
        });
      }
    },
    initMap() {
      if (this.mapView || !this.EsriMapView) return;
      const el = document.getElementById(this.mapId);
      if (!el) return;
      this.mapView = new this.EsriMapView({
        container: el,
        map: new this.EsriMap({ basemap: 'streets-navigation-vector' }),
        center: [36.8219, -1.2921],
        zoom: 7,
      });
    },
    renderFarmersOnMap() {
      if (!this.mapView || !this.EsriGraphic || !this.EsriPoint) return;
      this.mapView.graphics.removeAll();
      this.farmers.filter((f) => this.hasLocation(f)).forEach((f) => {
        const point = new this.EsriPoint({ longitude: f.longitude || f.lng, latitude: f.latitude || f.lat });
        const g = new this.EsriGraphic({
          geometry: point, symbol: { type: 'simple-marker', color: 'green', size: 7 }, attributes: f, popupTemplate: { title: this.displayName(f), content: `${f.phoneNumber || f.phone || ''}` },
        });
        this.mapView.graphics.add(g);
      });
    },
    fitAllFarmers() {
      if (!this.mapView) return; if (!this.mapView.graphics.items.length) return; this.mapView.goTo(this.mapView.graphics.items).catch(() => {});
    },
    async openFarmerDocuments(farmer) {
      if (!farmer || !farmer.id) {
        this.showSnackbar('Invalid farmer data', 'error');
        return;
      }
      this.selectedDocumentFarmer = farmer;
      this.showDocumentsDialog = true;
      await this.fetchFarmerDocuments(farmer.id);
    },
    async fetchFarmerDocuments(farmerId) {
      if (!farmerId) {
        this.farmerDocuments = [];
        return;
      }
      try {
        // Use EUDR documents API
        const response = await axios.get(`/api/v1/documents/owner/${farmerId}`, {
          params: { ownerEntityType: 'FARMER' },
        });
        this.farmerDocuments = response.data || [];
      } catch (error) {
        this.farmerDocuments = [];
        this.showSnackbar('Failed to load documents', 'error');
      }
    },
    async handleFileUpload(event) {
      const files = Array.from(event.target.files);
      if (!files.length) return;

      if (!this.selectedDocumentFarmer || !this.selectedDocumentFarmer.id) {
        this.showSnackbar('Invalid farmer selection', 'error');
        return;
      }

      this.uploadingDocument = true;

      try {
        const exporterId = this.$store.state.auth?.user?.id;
        if (!exporterId) {
          throw new Error('User ID not found');
        }

        // Filter valid files and upload them all
        const validFiles = files.filter((file) => {
          if (file.size > 50 * 1024 * 1024) {
            this.showSnackbar(`File ${file.name} exceeds 50MB limit`, 'warning');
            return false;
          }
          return true;
        });

        // Upload all valid files in parallel
        const uploadPromises = validFiles.map((file) => {
          const formData = new FormData();
          formData.append('file', file);
          formData.append('documentType', 'DUE_DILIGENCE_STATEMENT');
          formData.append('ownerEntityId', this.selectedDocumentFarmer.id);
          formData.append('ownerEntityType', 'FARMER');
          formData.append('uploaderId', exporterId);

          return axios.post('/api/v1/documents/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
          });
        });

        await Promise.all(uploadPromises);

        this.showSnackbar('Documents uploaded successfully', 'success');
        await this.fetchFarmerDocuments(this.selectedDocumentFarmer.id);
        this.$refs.fileInput.value = '';
      } catch (error) {
        this.showSnackbar('Failed to upload documents', 'error');
      } finally {
        this.uploadingDocument = false;
      }
    },
    async downloadDocument(doc) {
      try {
        const response = await axios.get(`/api/v1/documents/${doc.id}/download`, {
          responseType: 'blob',
        });
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', doc.fileName);
        document.body.appendChild(link);
        link.click();
        link.remove();
        window.URL.revokeObjectURL(url);
      } catch (error) {
        this.showSnackbar('Failed to download document', 'error');
      }
    },
    async deleteDocument(doc) {
      // eslint-disable-next-line no-alert, no-restricted-globals
      // if (!confirm(`Delete ${doc.fileName}?`)) return;

      try {
        await axios.delete(`/api/v1/documents/${doc.id}`);
        this.showSnackbar('Document deleted successfully', 'success');
        await this.fetchFarmerDocuments(this.selectedDocumentFarmer.id);
      } catch (error) {
        this.showSnackbar('Failed to delete document', 'error');
      }
    },
    getFileIcon(fileName) {
      const ext = fileName.split('.').pop().toLowerCase();
      const imageIcon = { icon: 'mdi-file-image', color: 'green' };
      const wordIcon = { icon: 'mdi-file-word', color: 'blue' };
      const icons = {
        pdf: { icon: 'mdi-file-pdf-box', color: 'red' },
        doc: wordIcon,
        docx: wordIcon,
        jpg: imageIcon,
        jpeg: imageIcon,
        png: imageIcon,
      };
      return icons[ext] || { icon: 'mdi-file', color: 'grey' };
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
  beforeDestroy() { if (this.mapView) { this.mapView.destroy(); this.mapView = null; } },
};
</script>

<style scoped>
/* Minimal additional styles if needed */
</style>
