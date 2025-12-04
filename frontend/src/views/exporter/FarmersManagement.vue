<!-- eslint-disable -->
<template>
  <Default>
    <div class="tw-min-h-screen tw-bg-gray-50 tw-font-sans">
      <!-- iOS-style Header -->
      <div class=" tw-top-0 tw-z-30 tw-bg-white/80 tw-backdrop-blur-md tw-border-b tw-border-gray-200 tw-px-4 tw-py-3">
        <div class="tw-max-w-3xl tw-mx-auto tw-flex tw-items-center tw-justify-between">
          <h1 class="tw-text-xl tw-font-bold tw-text-gray-900 tw-tracking-tight">Farmers</h1>

          <!-- Zone Selector (iOS Picker Style) -->
          <div class="tw-relative">
            <select
              v-model="selectedZoneId"
              @focus="fetchZonesIfNeeded"
              @change="onZoneChange"
              class="tw-appearance-none tw-bg-gray-100 tw-text-blue-600 tw-font-medium tw-py-1.5 tw-pl-3 tw-pr-8 tw-rounded-lg tw-text-sm focus:tw-outline-none focus:tw-ring-2 focus:tw-ring-blue-500/50 tw-cursor-pointer"
              :disabled="loadingZones"
            >
              <option :value="null" disabled>Select Zone</option>
              <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.name }}</option>
            </select>
            <v-icon small color="blue" class="tw-absolute tw-right-2 tw-top-1/2 -tw-translate-y-1/2 tw-pointer-events-none">mdi-chevron-down</v-icon>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div class="tw-max-w-3xl tw-mx-auto tw-p-4 tw-space-y-4">
        <!-- Search Bar -->
        <div class="tw-relative">
          <v-icon small class="tw-absolute tw-left-3 tw-top-1/2 -tw-translate-y-1/2 tw-text-gray-400">mdi-magnify</v-icon>
          <input
            v-model.trim="search"
            type="text"
            placeholder="Search"
            class="tw-w-full tw-bg-gray-200/70 tw-text-gray-900 tw-placeholder-gray-500 tw-rounded-xl tw-py-2 tw-pl-9 tw-pr-4 tw-text-sm focus:tw-outline-none focus:tw-bg-gray-200 tw-transition"
          />
        </div>

        <!-- Loading State -->
        <div v-if="loadingFarmers" class="tw-flex tw-justify-center tw-py-8">
          <v-progress-circular indeterminate color="grey" size="24" width="2"></v-progress-circular>
        </div>

        <!-- Empty State -->
        <div v-else-if="!filteredFarmers.length" class="tw-text-center tw-py-12 tw-text-gray-400">
          <v-icon large color="grey lighten-2" class="tw-mb-2">mdi-account-off</v-icon>
          <p class="tw-text-sm">No farmers found</p>
        </div>

        <!-- Farmers List (iOS Table View Style) -->
        <div v-else class="tw-bg-white tw-rounded-xl tw-overflow-hidden tw-shadow-sm tw-border tw-border-gray-200">
          <div
            v-for="(farmer, index) in filteredFarmers"
            :key="farmer.id"
            class="tw-flex tw-items-center tw-justify-between tw-p-4 hover:tw-bg-gray-50 tw-transition tw-cursor-pointer tw-border-b tw-border-gray-100 last:tw-border-0"
            @click="openEditFarmerDialog(farmer)"
          >
            <div class="tw-flex tw-items-center tw-gap-3">
              <div class="tw-w-10 tw-h-10 tw-rounded-full tw-bg-gray-100 tw-flex tw-items-center tw-justify-center tw-text-gray-500 tw-font-semibold tw-text-sm">
                {{ (displayName(farmer)[0] || '?').toUpperCase() }}
              </div>
              <div>
                <div class="tw-font-semibold tw-text-gray-900 tw-text-base">{{ displayName(farmer) }}</div>
                <div class="tw-text-sm tw-text-gray-500">{{ farmer.phoneNumber || farmer.phone || 'No phone' }}</div>
              </div>
            </div>

            <div class="tw-flex tw-items-center tw-gap-1">
               <v-btn icon small color="blue" @click.stop="openFarmerDocuments(farmer)">
                <v-icon small>mdi-file-document-outline</v-icon>
              </v-btn>
              <v-icon small color="grey lighten-1">mdi-chevron-right</v-icon>
            </div>
          </div>
        </div>
        
        <div class="tw-flex tw-justify-center tw-text-xs tw-text-gray-400 tw-py-2">
          {{ filteredFarmers.length }} Farmers
        </div>

      </div>

      <!-- Floating Action Button for Add -->
      <v-btn
        fab
        fixed
        bottom
        right
        color="black"
        dark
        class="tw-shadow-lg tw-mb-4 tw-mr-4"
        @click="openAddFarmerDialog"
      >
        <v-icon>mdi-plus</v-icon>
      </v-btn>

      <!-- Dialogs -->
      <AssistedFarmerRegistration
        v-if="selectedZone"
        v-model="showAddFarmerDialog"
        :zoneId="selectedZone?.id"
        :zone="selectedZone"
        @closed="handleFarmerDialogClosed"
      />

      <v-dialog
        v-model="showEditFarmerDialog"
        fullscreen
        transition="dialog-bottom-transition"
      >
        <EditFarmerDetails
          v-if="farmerToEdit"
          v-model="showEditFarmerDialog"
          :farmer="farmerToEdit"
          @closed="handleEditFarmerDialogClosed"
          @saved="handleFarmerSaved"
        />
      </v-dialog>

      <!-- Minimalistic Documents Dialog -->
      <v-dialog v-model="showDocumentsDialog" max-width="600" scrollable content-class="tw-rounded-2xl">
        <div class="tw-bg-white tw-rounded-2xl tw-overflow-hidden tw-flex tw-flex-col tw-max-h-[80vh]">
          <div class="tw-px-4 tw-py-3 tw-border-b tw-border-gray-100 tw-flex tw-items-center tw-justify-between tw-bg-gray-50/50">
             <h3 class="tw-font-semibold tw-text-gray-900">Documents</h3>
             <v-btn icon small @click="showDocumentsDialog = false"><v-icon small>mdi-close</v-icon></v-btn>
          </div>
          
          <div class="tw-p-4 tw-overflow-y-auto tw-flex-1">
            <div v-if="selectedDocumentFarmer" class="tw-mb-4">
               <div class="tw-text-xs tw-text-gray-500 tw-uppercase tw-tracking-wide tw-mb-1">Farmer</div>
               <div class="tw-font-medium tw-text-gray-900">{{ displayName(selectedDocumentFarmer) }}</div>
            </div>

            <div class="tw-space-y-3">
              <div
                v-for="doc in farmerDocuments"
                :key="doc.id"
                class="tw-flex tw-items-center tw-justify-between tw-p-3 tw-bg-gray-50 tw-rounded-xl tw-border tw-border-gray-100"
              >
                <div class="tw-flex tw-items-center tw-gap-3 tw-overflow-hidden">
                  <v-icon :color="getFileIcon(doc.fileName).color" small>{{ getFileIcon(doc.fileName).icon }}</v-icon>
                  <div class="tw-truncate">
                    <div class="tw-text-sm tw-font-medium tw-text-gray-900 tw-truncate">{{ doc.fileName }}</div>
                    <div class="tw-text-xs tw-text-gray-500">{{ formatDate(doc.uploadedAt) }}</div>
                  </div>
                </div>
                <div class="tw-flex tw-items-center">
                  <v-btn icon x-small color="blue" @click="downloadDocument(doc)"><v-icon small>mdi-download</v-icon></v-btn>
                  <v-btn icon x-small color="red" @click="deleteDocument(doc)"><v-icon small>mdi-trash-can-outline</v-icon></v-btn>
                </div>
              </div>
              <div v-if="!farmerDocuments.length" class="tw-text-center tw-py-6 tw-text-gray-400 tw-text-sm">
                No documents yet
              </div>
            </div>
          </div>

          <div class="tw-p-4 tw-border-t tw-border-gray-100 tw-bg-gray-50/30">
             <input
                ref="fileInput"
                type="file"
                multiple
                accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
                @change="handleFileUpload"
                class="tw-hidden"
              />
            <v-btn block color="black" dark depressed class="tw-rounded-xl" :loading="uploadingDocument" @click="$refs.fileInput.click()">
              <v-icon left small>mdi-upload</v-icon> Upload Document
            </v-btn>
          </div>
        </div>
      </v-dialog>

      <v-snackbar v-model="snackbar.show" :timeout="3000" bottom content-class="tw-rounded-xl">
        {{ snackbar.message }}
        <template v-slot:action="{ attrs }">
          <v-btn text v-bind="attrs" @click="snackbar.show = false" color="blue">Close</v-btn>
        </template>
      </v-snackbar>

    </div>
  </Default>
</template>

<script>
/* eslint-disable */
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
