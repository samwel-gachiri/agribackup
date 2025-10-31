<template>
  <Default>
    <div fluid class="pa-6 zone-management-root">
      <v-row>
        <v-col cols="12" class="pb-0">
      <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
<!--            <div>-->
<!--              <h1 class="tw-text-2xl tw-font-bold tw-mb-1">Zone Management</h1>-->
<!--              <p class="tw-text-sm tw-text-gray-600">Create, visualize and manage exporter zones & supervisors</p>-->
<!--            </div>-->
            <div class="d-flex align-center">
              <v-btn text icon :loading="globalLoading" @click="refreshCurrentTab" :disabled="globalLoading">
                <v-icon>mdi-refresh</v-icon>
              </v-btn>
              <v-btn text icon @click="$router.push({ name: 'ZoneCommentsManagement' })" :disabled="globalLoading">
                <v-icon>mdi-comment-multiple-outline</v-icon>
              </v-btn>
            </div>
          </div>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12">
          <v-tabs v-model="activeTab" color="primary" grow class="tw-shadow-md tw-rounded-lg">
            <v-tab v-if="hasPermission('CREATE_ZONE')">Create Zone</v-tab>
            <v-tab v-if="hasPermission('UPDATE_ZONE') || hasPermission('VIEW_ZONE_SUPERVISOR')">All Zones</v-tab>
            <v-tab v-if="hasPermission('VIEW_ZONE_SUPERVISOR')">Zone Details</v-tab>
          </v-tabs>
        </v-col>
      </v-row>

      <v-tabs-items v-model="activeTab">
        <!-- Create Zone -->
        <v-tab-item v-if="hasPermission('CREATE_ZONE')">
          <v-row class="tw-mt-2">
            <v-col cols="12" md="4">
              <v-card class="tw-h-full tw-shadow-lg">
                <v-card-title class="tw-bg-primary tw-text-white tw-py-3">
                  <v-icon left>mdi-map-plus</v-icon>
                  <span class="tw-font-medium">Create New Zone</span>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text>
                  <ZoneForm
                    :zone="newZone"
                    :formTouched="formTouched"
                    :rules="rules"
                    :loading="loading"
                    @submit="createZone"
                    @update:zone="newZone = $event"
                    ref="createForm"
                  />
                </v-card-text>
                <v-divider></v-divider>
                <v-card-actions class="justify-end">
                  <v-btn variant="text" color="grey" @click="resetCreateForm" :disabled="loading">Reset</v-btn>
                  <v-btn color="primary" :loading="loading" @click="$refs.createForm.submit()">Save Zone</v-btn>
                </v-card-actions>
              </v-card>
            </v-col>
            <v-col cols="12" md="8">
              <v-card class="tw-h-full tw-shadow-lg">
                <v-card-title class="tw-bg-secondary tw-text-white tw-py-3">
                  <v-icon left>mdi-radar</v-icon>
                  <span class="tw-font-medium">Zone Preview</span>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text class="pa-0" style="height:500px;">
                  <div :id="createMapId" class="tw-w-full tw-h-full tw-rounded-b-xl tw-overflow-hidden tw-bg-gray-200" style="height:500px; position:relative;">
                    <div v-if="!createMapView" class="tw-absolute tw-inset-0 tw-flex tw-items-center tw-justify-center tw-text-gray-500 tw-text-sm">Loading map...</div>
                  </div>
                </v-card-text>
                <v-alert v-if="!newZone.centerLatitude || !newZone.centerLongitude" type="info" variant="tonal" class="tw-m-4" density="comfortable">
                  Click on the map to set the centre point of the zone.
                </v-alert>
              </v-card>
            </v-col>
          </v-row>
        </v-tab-item>

        <!-- All Zones -->
        <v-tab-item v-if="hasPermission('UPDATE_ZONE') || hasPermission('VIEW_ZONE_SUPERVISOR')">
          <v-row class="tw-mt-2">
            <v-col cols="12" md="4">
              <v-card elevation="2" class="tw-mb-4">
                <v-card-title class="tw-bg-deep-purple tw-text-white tw-py-3">
                  <v-icon left>mdi-format-list-bulleted</v-icon>
                  <span class="tw-font-medium">Zones</span>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text>
                  <v-btn block color="primary" class="tw-mb-4" :loading="loading" @click="fetchAllZones">
                    <v-icon left>mdi-database-search</v-icon>
                    Load Zones
                  </v-btn>
                  <ZoneList
                    :zones="allZones"
                    :selectedZone="selectedZone"
                    :loading="loading"
                    @select="selectZone"
                  />
                </v-card-text>
              </v-card>
              <v-card v-if="selectedZone && hasPermission('UPDATE_ZONE')" elevation="2">
                <v-card-title class="tw-bg-indigo tw-text-white tw-py-3">
                  <v-icon left>mdi-pencil-map</v-icon>
                  <span class="tw-font-medium">Edit Zone</span>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text>
                  <ZoneForm
                    :zone="editZone"
                    :formTouched="formTouched"
                    :rules="rules"
                    :loading="loading"
                    @submit="updateZone"
                    @update:zone="editZone = $event"
                    ref="editZoneForm"
                  />
                </v-card-text>
                <v-card-actions class="tw-justify-end">
                  <v-btn color="primary" :loading="loading" @click="$refs.editZoneForm.submit()">Update</v-btn>
                </v-card-actions>
              </v-card>
            </v-col>
            <v-col cols="12" md="8">
              <v-card class="tw-h-full tw-shadow-lg">
                <v-card-title class="tw-bg-deep-purple-accent-2 tw-text-white tw-py-3">
                  <v-icon left>mdi-map</v-icon>
                  <span class="tw-font-medium">Zone Map</span>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text class="pa-0" style="height:600px;">
                  <div :id="allZonesMapId" class="tw-w-full tw-h-full tw-rounded-b-xl tw-overflow-hidden tw-bg-gray-200" style="height:600px; position:relative;">
                    <div v-if="!allZonesMapView" class="tw-absolute tw-inset-0 tw-flex tw-items-center tw-justify-center tw-text-gray-500 tw-text-sm">Loading map...</div>
                  </div>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </v-tab-item>

        <!-- Zone Details -->
        <v-tab-item v-if="hasPermission('VIEW_ZONE_SUPERVISOR')">
          <v-row class="tw-mt-2">
            <v-col cols="12" md="4">
              <v-card elevation="2" class="tw-mb-4">
                <v-card-title class="tw-bg-teal tw-text-white tw-py-3">
                  <v-icon left>mdi-magnify</v-icon>
                  <span class="tw-font-medium">Select Zone</span>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text>
                  <v-btn block color="teal" class="tw-mb-3" :loading="loadingZones" @click="fetchZoneOptions">
                    <v-icon left>mdi-sync</v-icon>
                    Fetch Zones
                  </v-btn>
                  <ZoneSelector
                    :zones="zoneOptions"
                    :selectedZone="selectedZoneDetails"
                    :loading="loadingZones"
                    @fetch="fetchZoneOptions"
                    @select="selectZoneDetails"
                  />
                </v-card-text>
              </v-card>
            </v-col>
            <v-col cols="12" md="8">
              <v-card elevation="2">
                <v-card-title class="tw-bg-teal-darken-1 tw-text-white tw-py-3">
                  <v-icon left>mdi-information-outline</v-icon>
                  <span class="tw-font-medium">Zone Details</span>
                </v-card-title>
                <v-divider></v-divider>
                <v-card-text>
                  <ZoneDetails
                    :zone="selectedZoneDetails"
                    :supervisors="zoneSupervisors"
                    :loading="loading"
                    @fetch-supervisors="fetchZoneSupervisors"
                  />
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </v-tab-item>
      </v-tabs-items>

      <!-- Snackbar -->
            <v-snackbar v-model="snackbar.show" :timeout="5000" top right multi-line elevation="6" class="tw-max-w-sm">
        <v-alert :type="mapAlertType(snackbar.color)" :color="snackbarColor(snackbar.color)" dense outlined class="ma-0 pa-2" border="left">
            <div class="tw-flex tw-items-center">
            <v-icon size="20" class="tw-mr-2" v-if="snackbar.color==='success'">mdi-check-circle</v-icon>
            <v-icon size="20" class="tw-mr-2" v-else-if="snackbar.color==='error'">mdi-alert-circle</v-icon>
            <v-icon size="20" class="tw-mr-2" v-else-if="snackbar.color==='warning'">mdi-alert</v-icon>
            <v-icon size="20" class="tw-mr-2" v-else>mdi-information</v-icon>
            <span class="tw-font-medium">{{ snackbar.message }}</span>
            <v-spacer></v-spacer>
            <v-btn text icon small @click="snackbar.show=false">
              <v-icon>mdi-close</v-icon>
            </v-btn>
          </div>
        </v-alert>
      </v-snackbar>

      <!-- Global Overlay Loader -->
      <!-- <v-overlay :model-value="globalLoading" persistent absolute class="align-center justify-center">
        <v-progress-circular indeterminate size="64" color="primary" />
      </v-overlay> -->
    </div>
  </Default>
</template>

<script>
/* eslint-disable indent */
import axios from 'axios';
import jwtDecode from 'jwt-decode';
import Default from '@/components/layout/Default.vue';
import { getCurrentUserId } from '@/utils/roles.js';
import ZoneForm from '@/components/zone/ZoneForm.vue';
import ZoneList from '@/components/zone/ZoneList.vue';
import ZoneSelector from '@/components/zone/ZoneSelector.vue';
import ZoneDetails from '@/components/zone/ZoneDetails.vue';

export default {
  name: 'ZoneManagement',
  components: {
    Default,
    ZoneForm,
    ZoneList,
    ZoneSelector,
    ZoneDetails,
  },
  data() {
    return {
      activeTab: 0,
      loading: false,
      loadingZones: false,
      formTouched: false,
  globalLoading: false,

  // Map IDs & Views
  createMapId: `create-map-${Math.random().toString(36).substr(2, 8)}`,
  allZonesMapId: `zones-map-${Math.random().toString(36).substr(2, 8)}`,
  createMapView: null,
  allZonesMapView: null,
  EsriMap: null,
  EsriMapView: null,
  EsriGraphic: null,
  EsriCircle: null,
  EsriPoint: null,

      // Create Zone Form
      newZone: {
        name: '',
        produceType: '',
        centerLatitude: null,
        centerLongitude: null,
        radiusKm: null,
      },

      // Produce types
      produceTypes: [
        'Maize', 'Beans', 'Rice', 'Wheat', 'Coffee', 'Tea',
        'Sugarcane', 'Cotton', 'Vegetables', 'Fruits', 'Other',
      ],

      // View All Zones
      allZones: [],
      selectedZone: null,
      editZone: null,

      // Zone Details
      zoneOptions: [],
      selectedZoneDetails: null,
      zoneSupervisors: [],

      // Validation rules
      rules: {
        required: (value) => !!value || 'This field is required',
        latitude: (value) => {
          if (!value) return true;
          const num = parseFloat(value);
          return (num >= -90 && num <= 90) || 'Latitude must be between -90 and 90';
        },
        longitude: (value) => {
          if (!value) return true;
          const num = parseFloat(value);
          return (num >= -180 && num <= 180) || 'Longitude must be between -180 and 180';
        },
        positive: (value) => {
          if (!value) return true;
          return parseFloat(value) > 0 || 'Value must be positive';
        },
      },

      // Snackbar
      snackbar: {
        show: false,
        message: '',
        color: 'success',
      },
    };
  },
  computed: {
    userPermissions() {
      const token = this.$store.state.auth.token;
      if (!token) return [];
      try {
        const decoded = jwtDecode(token);
        return decoded.user?.permissions || [];
        // eslint-disable-next-line no-unused-vars
      } catch (error) {
        return [];
      }
    },
  },
  async mounted() {
    await this.loadArcGIS();
    this.$nextTick(() => {
      if (this.hasPermission('CREATE_ZONE')) this.activeTab = 0; else if (this.hasPermission('UPDATE_ZONE') || this.hasPermission('VIEW_ZONE_SUPERVISOR')) this.activeTab = 1;
      // Initialize the map for the default tab
      if (this.activeTab === 0) this.initializeCreateMap();
      if (this.activeTab === 1) this.initializeAllZonesMap();
    });
  },
  watch: {
    activeTab(newTab) {
      this.$nextTick(() => {
        if (newTab === 0) {
          this.initializeCreateMap();
          this.updateCreatePreview();
        } else if (newTab === 1) {
          this.initializeAllZonesMap();
          this.renderAllZones();
        } else if (newTab === 2) {
          this.fetchZoneOptions();
        }
      });
    },
    'newZone.centerLatitude': function onCenterLatChange() { this.updateCreatePreview(); },
    'newZone.centerLongitude': function onCenterLngChange() { this.updateCreatePreview(); },
    'newZone.radiusKm': function onRadiusChange() { this.updateCreatePreview(); },
  },
  methods: {
    hasPermission(permission) {
      return this.userPermissions.includes(permission);
    },
    async loadArcGIS() {
      if (!window.require) {
        await new Promise((resolve, reject) => {
          const s = document.createElement('script');
          s.src = 'https://js.arcgis.com/4.25/init.js';
          s.onload = resolve; s.onerror = reject; document.head.appendChild(s);
          const l = document.createElement('link'); l.rel = 'stylesheet'; l.href = 'https://js.arcgis.com/4.25/esri/themes/light/main.css'; document.head.appendChild(l);
        });
      }
      if (!this.EsriMap) {
        await new Promise((resolve, reject) => {
          try {
            window.require([
              'esri/Map',
              'esri/views/MapView',
              'esri/Graphic',
              'esri/geometry/Circle',
              'esri/geometry/Point',
            ], (Map, MapView, Graphic, Circle, Point) => {
              this.EsriMap = Map;
              this.EsriMapView = MapView;
              this.EsriGraphic = Graphic;
              this.EsriCircle = Circle;
              this.EsriPoint = Point;
              resolve();
            });
          } catch (e) { reject(e); }
        });
      }
    },
    initializeCreateMap() {
      if (this.createMapView || !this.EsriMapView) return;
      const el = document.getElementById(this.createMapId);
      if (!el) return;
      const center = this.defaultCenter();
      this.createMapView = new this.EsriMapView({
        map: new this.EsriMap({ basemap: 'streets-navigation-vector' }),
        container: el,
        center,
        zoom: 10,
      });
      this.createMapView.on('click', (evt) => {
        if (!evt.mapPoint) return;
        this.newZone.centerLatitude = evt.mapPoint.latitude;
        this.newZone.centerLongitude = evt.mapPoint.longitude;
      });
      this.createMapView.when(() => this.updateCreatePreview());
    },
    initializeAllZonesMap() {
      if (this.allZonesMapView || !this.EsriMapView) return;
      const el = document.getElementById(this.allZonesMapId);
      if (!el) return;
      this.allZonesMapView = new this.EsriMapView({
        map: new this.EsriMap({ basemap: 'streets-navigation-vector' }),
        container: el,
        center: this.defaultCenter(),
        zoom: 7,
      });
      this.allZonesMapView.when(() => this.renderAllZones());
    },
    defaultCenter() {
      if (this.newZone.centerLongitude && this.newZone.centerLatitude) {
        return [this.newZone.centerLongitude, this.newZone.centerLatitude];
      }
      return [36.8219, -1.2921]; // Nairobi fallback
    },
    updateCreatePreview() {
      if (!this.createMapView || !this.EsriGraphic || !this.EsriPoint) return;
      this.createMapView.graphics.removeAll();
      if (!(this.newZone.centerLongitude && this.newZone.centerLatitude)) return;
      const center = new this.EsriPoint({ longitude: this.newZone.centerLongitude, latitude: this.newZone.centerLatitude });
      const centerGraphic = new this.EsriGraphic({
        geometry: center,
        symbol: { type: 'simple-marker', color: 'red', size: 8 },
      });
      this.createMapView.graphics.add(centerGraphic);
      if (this.newZone.radiusKm) {
        const circle = new this.EsriCircle({ center, radius: this.newZone.radiusKm, radiusUnit: 'kilometers' });
        const circleGraphic = new this.EsriGraphic({
          geometry: circle,
            symbol: { type: 'simple-fill', color: [0, 100, 255, 0.1], outline: { color: [0, 100, 255, 0.8], width: 2 } },
        });
        this.createMapView.graphics.add(circleGraphic);
        this.createMapView.goTo(circle.extent).catch(() => {});
      } else {
        this.createMapView.goTo({ center, zoom: 12 }).catch(() => {});
      }
    },
    renderAllZones() {
      if (!this.allZonesMapView || !this.EsriGraphic || !this.EsriPoint) return;
      this.allZonesMapView.graphics.removeAll();
      if (!this.allZones.length) return;
      this.allZones.forEach((zone, idx) => {
        const center = new this.EsriPoint({ longitude: zone.centerLongitude, latitude: zone.centerLatitude });
        const radius = zone.radiusKm;
        const circle = new this.EsriCircle({ center, radius, radiusUnit: 'kilometers' });
        const palette = [
          [255, 0, 0, 0.2], [0, 255, 0, 0.2], [0, 0, 255, 0.2], [255, 255, 0, 0.2], [255, 0, 255, 0.2], [0, 255, 255, 0.2],
        ];
        const selected = this.selectedZone && this.selectedZone.id === zone.id;
        const circleGraphic = new this.EsriGraphic({
          geometry: circle,
          symbol: { type: 'simple-fill', color: selected ? [255, 165, 0, 0.3] : palette[idx % palette.length], outline: { color: selected ? [255, 165, 0, 0.8] : [80, 80, 80, 0.6], width: 2 } },
          attributes: zone,
        });
        const centerGraphic = new this.EsriGraphic({ geometry: center, symbol: { type: 'simple-marker', color: selected ? 'orange' : 'red', size: 6 } });
        this.allZonesMapView.graphics.addMany([circleGraphic, centerGraphic]);
      });
      this.allZonesMapView.goTo(this.allZonesMapView.graphics.items).catch(() => {});
    },
    centerOnSelected(zone) {
      if (!this.allZonesMapView || !zone) return;
      const p = new this.EsriPoint({ longitude: zone.centerLongitude, latitude: zone.centerLatitude });
      this.allZonesMapView.goTo({ center: p, zoom: 11 }).catch(() => {});
    },
    refreshCurrentTab() {
      switch (this.activeTab) {
        case 0:
          this.initializeCreateMap();
          this.updateCreatePreview();
          break;
        case 1:
          this.initializeAllZonesMap();
          this.fetchAllZones();
          break;
        case 2:
          this.fetchZoneOptions();
          if (this.selectedZoneDetails) this.fetchZoneSupervisors();
          break;
        default:
          break;
      }
    },
    async createZone() {
      this.formTouched = true;
      if (
        !this.newZone.name
        || !this.rules.latitude(this.newZone.centerLatitude)
        || !this.rules.longitude(this.newZone.centerLongitude)
        || !this.rules.positive(this.newZone.radiusKm)
      ) {
        this.showSnackbar('Please fill all required fields correctly.', 'error');
        return;
      }
      this.loading = true;
      try {
        // eslint-disable-next-line no-unused-vars
        const response = await axios.post(
          '/api/admin-service/zones',
          {
            name: this.newZone.name,
            produceType: this.newZone.produceType,
            centerLatitude: this.newZone.centerLatitude,
            centerLongitude: this.newZone.centerLongitude,
            radiusKm: this.newZone.radiusKm,
            exporterId: getCurrentUserId(),
          },
          { headers: { Authorization: `Bearer ${this.$store.state.auth.token}` } },
        );
        this.showSnackbar('Zone created successfully!', 'success');
        this.resetCreateForm();
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Error creating zone', 'error');
      } finally {
        this.loading = false;
      }
    },
    resetCreateForm() {
      this.newZone = {
        name: '',
        produceType: '',
        centerLatitude: null,
        centerLongitude: null,
        radiusKm: null,
      };
  if (this.createMapView) this.createMapView.graphics.removeAll();
      this.$refs.createForm.reset();
      this.formTouched = false;
    },
    async fetchAllZones() {
      if (!getCurrentUserId()) {
        this.showSnackbar('Please enter a User ID', 'warning');
        return;
      }
      this.loading = true;
      try {
        const response = await axios.get(
          `/api/admin-service/zones/exporter/${getCurrentUserId()}`,
          { headers: { Authorization: `Bearer ${this.$store.state.auth.token}` } },
        );
        this.allZones = response.data.data;
  this.initializeAllZonesMap();
  this.renderAllZones();
        if (this.allZones.length === 0) {
          this.showSnackbar('No zones found for this exporter', 'info');
        }
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Error fetching zones', 'error');
        this.allZones = [];
      } finally {
        this.loading = false;
      }
    },
    selectZone(zone) {
      this.selectedZone = zone;
      this.editZone = { ...zone };
  this.renderAllZones();
  this.centerOnSelected(zone);
    },
    async updateZone() {
      if (!this.editZone || !this.selectedZone) return;
      this.loading = true;
      try {
        await axios.put(
          `/api/admin-service/zones/${this.selectedZone.id}`,
          {
            name: this.editZone.name,
            produceType: this.editZone.produceType,
            centerLatitude: this.editZone.centerLatitude,
            centerLongitude: this.editZone.centerLongitude,
            radiusKm: this.editZone.radiusKm,
          },
          { headers: { Authorization: `Bearer ${this.$store.state.auth.token}` } },
        );
        const idx = this.allZones.findIndex((z) => z.id === this.selectedZone.id);
        if (idx !== -1) {
          this.allZones[idx] = { ...this.editZone, id: this.selectedZone.id };
        }
        this.selectedZone = { ...this.editZone, id: this.selectedZone.id };
        this.showSnackbar('Zone updated successfully!', 'success');
  this.renderAllZones();
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Error updating zone', 'error');
      } finally {
        this.loading = false;
      }
    },
    async fetchZoneOptions() {
      if (this.zoneOptions.length > 0) return;
      this.loadingZones = true;
      try {
        const response = await axios.get(
          `/api/admin-service/zones/exporter/${getCurrentUserId()}`,
          { headers: { Authorization: `Bearer ${this.$store.state.auth.token}` } },
        );
        this.zoneOptions = response.data.data.map((zone) => ({
          id: zone.id,
          name: zone.name,
        }));
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Error fetching zones', 'error');
      } finally {
        this.loadingZones = false;
      }
    },
    selectZoneDetails(zone) {
      this.selectedZoneDetails = zone;
      this.fetchZoneSupervisors();
    },
    async fetchZoneSupervisors() {
      if (!this.selectedZoneDetails) {
        this.showSnackbar('Please select a zone', 'warning');
        return;
      }
      this.loading = true;
      try {
        const response = await axios.get(
          `/api/admin-service/zones/${this.selectedZoneDetails.id}/supervisors`,
          { headers: { Authorization: `Bearer ${this.$store.state.auth.token}` } },
        );
        this.zoneSupervisors = response.data.data;
        if (this.zoneSupervisors.length === 0) {
          this.showSnackbar('No supervisors found for this zone', 'info');
        }
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Error fetching supervisors', 'error');
        this.zoneSupervisors = [];
      } finally {
        this.loading = false;
      }
    },
    updateCoordinates({ latitude, longitude }) { // retained for API symmetry if reused elsewhere
      this.newZone.centerLatitude = latitude;
      this.newZone.centerLongitude = longitude;
    },
    showSnackbar(message, color = 'success') {
      this.snackbar = { show: true, message, color };
    },
    snackbarColor(kind) {
      switch (kind) {
        case 'success': return 'green lighten-1';
        case 'error': return 'red lighten-1';
        case 'warning': return 'amber darken-2';
        case 'info': return 'blue lighten-1';
        default: return 'primary';
      }
    },
    mapAlertType(kind) {
      if (['success', 'error', 'warning', 'info'].includes(kind)) return kind;
      return 'info';
    },
  },
  beforeDestroy() {
    if (this.createMapView) { this.createMapView.destroy(); this.createMapView = null; }
    if (this.allZonesMapView) { this.allZonesMapView.destroy(); this.allZonesMapView = null; }
  },
};
</script>

<style scoped>
 .zone-management-root {
  background: linear-gradient(135deg, #f8fafc 0%, #eef2f7 100%);
  min-height: 100vh;
 }
 .v-card-title {
  font-weight: 600;
 }
 .v-alert :deep(.v-alert__content) {
  width: 100%;
 }
 .v-tab {
  font-weight: 500;
  text-transform: none;
 }
 .tw-max-w-sm {
  max-width: 480px;
 }
 .tw-h-full {
  height: 100%;
 }
</style>
