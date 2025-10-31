<template>
  <div class="arcgis-zone-manager">
    <!-- Map Container -->
    <div :id="mapId" class="tw-h-[600px] tw-w-full tw-rounded-lg tw-shadow-lg tw-overflow-hidden"></div>
    <!-- Map Controls -->
    <div class="tw-absolute tw-top-4 tw-right-4 tw-z-10 tw-bg-white tw-rounded-lg tw-shadow-md tw-p-3">
      <v-btn-toggle v-model="mapMode" mandatory class="tw-mb-2">
        <v-btn size="small" value="view">
          <v-icon>mdi-eye</v-icon>
          View
        </v-btn>
        <v-btn size="small" value="create">
          <v-icon>mdi-plus-circle</v-icon>
          Create
        </v-btn>
        <v-btn size="small" value="edit">
          <v-icon>mdi-pencil</v-icon>
          Edit
        </v-btn>
      </v-btn-toggle>

      <div class="tw-flex tw-flex-col tw-gap-2">
        <v-btn size="small" @click="toggleFarmerLocations" :color="showFarmers ? 'primary' : 'default'">
          <v-icon>mdi-account-group</v-icon>
          Farmers
        </v-btn>
        <v-btn size="small" @click="toggleRouteOptimization" :color="showRoutes ? 'success' : 'default'">
          <v-icon>mdi-map-marker-path</v-icon>
          Routes
        </v-btn>
        <v-btn size="small" @click="resetView">
          <v-icon>mdi-home</v-icon>
          Reset
        </v-btn>
      </div>
    </div>

    <!-- Zone Creation Panel -->
    <v-card v-if="mapMode === 'create' && creatingZone" class="tw-absolute tw-bottom-4 tw-left-4 tw-z-10 tw-w-80">
      <v-card-title class="tw-text-sm">Create New Zone</v-card-title>
      <v-card-text>
        <v-text-field
          v-model="newZone.name"
          label="Zone Name"
          density="compact"
          variant="outlined"
        />
        <v-text-field
          v-model="newZone.produceType"
          label="Produce Type"
          density="compact"
          variant="outlined"
        />
        <v-slider
          v-model="newZone.radiusKm"
          label="Radius (km)"
          min="0.5"
          max="50"
          step="0.5"
          thumb-label
        />
        <div class="tw-text-xs tw-text-gray-600 tw-mb-2">
          Click on the map to set zone center
        </div>
      </v-card-text>
      <v-card-actions>
        <v-btn size="small" @click="cancelZoneCreation">Cancel</v-btn>
        <v-btn size="small" color="primary" @click="saveZone" :disabled="!canSaveZone">Save</v-btn>
      </v-card-actions>
    </v-card>

    <!-- Zone Info Panel -->
    <v-card v-if="selectedZone" class="tw-absolute tw-bottom-4 tw-right-4 tw-z-10 tw-w-80">
      <v-card-title class="tw-text-sm">{{ selectedZone.name }}</v-card-title>
      <v-card-text>
        <div class="tw-text-xs tw-space-y-1">
          <div><strong>Produce:</strong> {{ selectedZone.produceType || 'Mixed' }}</div>
          <div><strong>Radius:</strong> {{ selectedZone.radiusKm }} km</div>
          <div><strong>Farmers:</strong> {{ selectedZone.farmerCount || 0 }}</div>
          <div><strong>Supervisors:</strong> {{ selectedZone.supervisorIds?.length || 0 }}</div>
        </div>
        <v-textarea
          v-if="mapMode === 'edit'"
          v-model="selectedZone.comments"
          label="Comments"
          density="compact"
          variant="outlined"
          rows="3"
          class="tw-mt-2"
        />
      </v-card-text>
      <v-card-actions v-if="mapMode === 'edit'">
        <v-btn size="small" @click="updateZoneComments">Update</v-btn>
        <v-btn size="small" color="error" @click="deleteZone">Delete</v-btn>
      </v-card-actions>
    </v-card>

    <!-- Loading Overlay -->
    <v-overlay v-model="loading" class="tw-flex tw-items-center tw-justify-center">
      <v-progress-circular indeterminate size="64" />
    </v-overlay>
  </div>
</template>

<script>
import axios from 'axios';
// Reusable constants to reduce duplicate literals
const LAYER = Object.freeze({
  ZONES: 'Zones',
  FARMERS: 'Farmers',
  ROUTES: 'Routes',
  PREVIEW: 'preview',
});
const BASEMAP = 'satellite';
const UNIT_KM = 'kilometers';

/* eslint-disable sonarjs/no-duplicate-string */

export default {
  name: 'ArcGISZoneManager',
  props: {
    zones: {
      type: Array,
      default: () => [],
    },
    farmers: {
      type: Array,
      default: () => [],
    },
    userRole: {
      type: String,
      default: 'ZONE_SUPERVISOR',
    },
    readonly: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      mapId: `arcgis-zone-manager-${Math.random().toString(36).substr(2, 9)}`,
      mapView: null,
      mapMode: 'view',
      loading: false,
      showFarmers: false,
      showRoutes: false,
      selectedZone: null,
      creatingZone: false,
      newZone: {
        name: '',
        produceType: '',
        centerLatitude: null,
        centerLongitude: null,
        radiusKm: 5,
      },
      // ArcGIS modules
      Map: null,
      MapView: null,
      Graphic: null,
      GraphicsLayer: null,
      Circle: null,
      Point: null,
      Polyline: null,
      SimpleMarkerSymbol: null,
      SimpleFillSymbol: null,
      SimpleLineSymbol: null,
      TextSymbol: null,
    };
  },
  computed: {
    canSaveZone() {
      return this.newZone.name
        && this.newZone.centerLatitude
        && this.newZone.centerLongitude
        && this.newZone.radiusKm > 0;
    },
  },
  mounted() {
    this.loadArcGIS().then(() => {
      this.initializeMap();
    });
  },
  beforeDestroy() {
    this.destroyMap();
  },
  watch: {
    zones: {
      handler() {
        this.displayZones();
      },
      deep: true,
    },
    farmers: {
      handler() {
        if (this.showFarmers) {
          this.displayFarmers();
        }
      },
      deep: true,
    },
    mapMode(newMode) {
      if (newMode === 'create') {
        this.startZoneCreation();
      } else {
        this.cancelZoneCreation();
      }
    },
  },
  methods: {
    async loadArcGIS() {
      return new Promise((resolve) => {
        if (window.require) {
          resolve();
          return;
        }
        const script = document.createElement('script');
        script.src = 'https://js.arcgis.com/4.28/init.js';
        script.onload = resolve;
        document.head.appendChild(script);
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.href = 'https://js.arcgis.com/4.28/esri/themes/light/main.css';
        document.head.appendChild(link);
      }).then(() => new Promise((resolve) => {
        window.require([
          'esri/Map',
          'esri/views/MapView',
          'esri/Graphic',
          'esri/layers/GraphicsLayer',
          'esri/geometry/Circle',
          'esri/geometry/Point',
          'esri/geometry/Polyline',
          'esri/symbols/SimpleMarkerSymbol',
          'esri/symbols/SimpleFillSymbol',
          'esri/symbols/SimpleLineSymbol',
          'esri/symbols/TextSymbol',
        ], (
          Map,
          MapView,
          Graphic,
          GraphicsLayer,
          Circle,
          Point,
          Polyline,
          SimpleMarkerSymbol,
          SimpleFillSymbol,
          SimpleLineSymbol,
          TextSymbol,
        ) => {
          this.Map = Map;
          this.MapView = MapView;
          this.Graphic = Graphic;
          this.GraphicsLayer = GraphicsLayer;
          this.Circle = Circle;
          this.Point = Point;
          this.Polyline = Polyline;
          this.SimpleMarkerSymbol = SimpleMarkerSymbol;
          this.SimpleFillSymbol = SimpleFillSymbol;
          this.SimpleLineSymbol = SimpleLineSymbol;
          this.TextSymbol = TextSymbol;
          resolve();
        });
      }));
    },

    initializeMap() {
      if (this.mapView || !document.getElementById(this.mapId)) return;

      const map = new this.Map({ basemap: BASEMAP });

      // Create graphics layers
      const zoneLayer = new this.GraphicsLayer({ title: LAYER.ZONES });
      const farmerLayer = new this.GraphicsLayer({ title: LAYER.FARMERS });
      const routeLayer = new this.GraphicsLayer({ title: LAYER.ROUTES });

      map.addMany([zoneLayer, farmerLayer, routeLayer]);

      this.mapView = new this.MapView({
        container: this.mapId,
        map,
        center: [36.8219, -1.2921], // Nairobi, Kenya
        zoom: 10,
        ui: {
          components: ['attribution'],
        },
      });

      // Map click handler
      this.mapView.on('click', (event) => {
        if (this.mapMode === 'create' && this.creatingZone) {
          this.setZoneCenter(event.mapPoint.longitude, event.mapPoint.latitude);
        } else {
          this.handleMapClick(event);
        }
      });

      // Display initial data
      this.displayZones();
    },

    startZoneCreation() {
      this.creatingZone = true;
      this.newZone = {
        name: '',
        produceType: '',
        centerLatitude: null,
        centerLongitude: null,
        radiusKm: 5,
      };
    },

    cancelZoneCreation() {
      this.creatingZone = false;
      this.clearPreviewGraphics();
    },

    setZoneCenter(longitude, latitude) {
      this.newZone.centerLongitude = longitude;
      this.newZone.centerLatitude = latitude;
      this.updateZonePreview();
    },

    updateZonePreview() {
      if (!this.newZone.centerLatitude || !this.newZone.centerLongitude) return;

      this.clearPreviewGraphics();

      const center = new this.Point({
        longitude: this.newZone.centerLongitude,
        latitude: this.newZone.centerLatitude,
      });

      const circle = new this.Circle({
        center,
        radius: this.newZone.radiusKm,
        radiusUnit: UNIT_KM,
      });

      const circleGraphic = new this.Graphic({
        geometry: circle,
        symbol: new this.SimpleFillSymbol({
          color: [255, 165, 0, 0.3],
          outline: new this.SimpleLineSymbol({
            color: [255, 165, 0, 0.8],
            width: 2,
          }),
        }),
      });

      const centerGraphic = new this.Graphic({
        geometry: center,
        symbol: new this.SimpleMarkerSymbol({
          color: 'orange',
          size: 8,
        }),
      });

      const previewLayer = this.mapView.map.findLayerById(LAYER.PREVIEW)
        || new this.GraphicsLayer({ id: LAYER.PREVIEW, title: 'Preview' });

      if (!this.mapView.map.findLayerById('preview')) {
        this.mapView.map.add(previewLayer);
      }

      previewLayer.addMany([circleGraphic, centerGraphic]);
      this.mapView.goTo(circle.extent);
    },

    clearPreviewGraphics() {
      const previewLayer = this.mapView.map.findLayerById(LAYER.PREVIEW);
      if (previewLayer) {
        previewLayer.removeAll();
      }
    },

    displayZones() {
      if (!this.mapView) return;
      const zoneLayer = this.mapView.map.layers.find((layer) => layer.title === LAYER.ZONES);
      if (!zoneLayer) return;

      zoneLayer.removeAll();

      this.zones.forEach((zone, index) => {
        const center = new this.Point({
          longitude: zone.centerLongitude,
          latitude: zone.centerLatitude,
        });

        const circle = new this.Circle({
          center,
          radius: zone.radiusKm,
          radiusUnit: UNIT_KM,
        });

        const colors = [
          [255, 0, 0, 0.2], [0, 255, 0, 0.2], [0, 0, 255, 0.2],
          [255, 255, 0, 0.2], [255, 0, 255, 0.2], [0, 255, 255, 0.2],
        ];

        const isSelected = this.selectedZone && this.selectedZone.id === zone.id;
        const color = isSelected ? [255, 165, 0, 0.3] : colors[index % colors.length];

        const circleGraphic = new this.Graphic({
          geometry: circle,
          symbol: new this.SimpleFillSymbol({
            color,
            outline: new this.SimpleLineSymbol({
              color: color.map((c) => (c === 0.2 || c === 0.3 ? 0.8 : c)),
              width: 2,
            }),
          }),
          attributes: { ...zone, type: 'zone' },
        });

        const centerGraphic = new this.Graphic({
          geometry: center,
          symbol: new this.SimpleMarkerSymbol({
            color: isSelected ? 'orange' : 'red',
            size: 8,
          }),
          attributes: { ...zone, type: 'zone-center' },
        });

        const labelGraphic = new this.Graphic({
          geometry: center,
          symbol: new this.TextSymbol({
            text: zone.name,
            color: 'white',
            haloColor: 'black',
            haloSize: 1,
            font: { size: 12, weight: 'bold' },
          }),
          attributes: { ...zone, type: 'zone-label' },
        });

        zoneLayer.addMany([circleGraphic, centerGraphic, labelGraphic]);
      });

      if (this.zones.length > 0) {
        this.mapView.goTo(zoneLayer.graphics.items);
      }
    },
    displayFarmers() {
      if (!this.mapView) return;
      const farmerLayer = this.mapView.map.layers.find((layer) => layer.title === LAYER.FARMERS);
      if (!farmerLayer) return;

      farmerLayer.removeAll();

      this.farmers.forEach((farmer) => {
        if (!farmer.location) return;

        const point = new this.Point({
          longitude: farmer.location.longitude,
          latitude: farmer.location.latitude,
        });

        const farmerGraphic = new this.Graphic({
          geometry: point,
          symbol: new this.SimpleMarkerSymbol({
            color: 'green',
            size: 10,
            outline: new this.SimpleLineSymbol({
              color: 'white',
              width: 2,
            }),
          }),
          attributes: { ...farmer, type: 'farmer' },
        });

        const labelGraphic = new this.Graphic({
          geometry: point,
          symbol: new this.TextSymbol({
            text: farmer.farmerName || farmer.name,
            color: 'white',
            haloColor: 'green',
            haloSize: 1,
            font: { size: 10 },
            yoffset: -15,
          }),
          attributes: { ...farmer, type: 'farmer-label' },
        });

        farmerLayer.addMany([farmerGraphic, labelGraphic]);
      });
    },

    toggleFarmerLocations() {
      this.showFarmers = !this.showFarmers;
      const farmerLayer = this.mapView.map.layers.find((layer) => layer.title === LAYER.FARMERS);
      if (this.showFarmers) {
        this.displayFarmers();
        farmerLayer.visible = true;
      } else {
        farmerLayer.visible = false;
      }
    },

    toggleRouteOptimization() {
      this.showRoutes = !this.showRoutes;

      if (this.showRoutes) {
        this.calculateOptimalRoutes();
      } else {
        const routeLayer = this.mapView.map.layers.find((layer) => layer.title === LAYER.ROUTES);
        routeLayer.removeAll();
      }
    },

    async calculateOptimalRoutes() {
      if (!this.selectedZone) {
        this.$emit('show-message', { type: 'warning', text: 'Please select a zone first' });
        return;
      }

      this.loading = true;

      try {
        // Get optimal route from backend
        const response = await axios.get(`/api/admin-service/zones/${this.selectedZone.id}/optimal-route`);

        if (response.data.success) {
          this.displayOptimalRoute(response.data.data);
        }
      } catch (error) {
        this.$emit('show-message', { type: 'error', text: 'Failed to calculate optimal route' });
      } finally {
        this.loading = false;
      }
    },

    displayOptimalRoute(routeData) {
      const routeLayer = this.mapView.map.layers.find((layer) => layer.title === LAYER.ROUTES);
      routeLayer.removeAll();

      // Create route polyline
      const routePoints = routeData.optimizedOrder
        .map((farmerId) => {
          const farmer = this.farmers.find((f) => f.farmerId === farmerId);
          return farmer ? [farmer.location.longitude, farmer.location.latitude] : null;
        })
        .filter(Boolean);

      if (routePoints.length > 1) {
        const polyline = new this.Polyline({
          paths: [routePoints],
        });

        const routeGraphic = new this.Graphic({
          geometry: polyline,
          symbol: new this.SimpleLineSymbol({
            color: [255, 0, 0, 0.8],
            width: 3,
            style: 'dash',
          }),
        });

        routeLayer.add(routeGraphic);

        // Add route markers with order numbers
        routePoints.forEach((point, index) => {
          const orderPoint = new this.Point({
            longitude: point[0],
            latitude: point[1],
          });

          const orderGraphic = new this.Graphic({
            geometry: orderPoint,
            symbol: new this.SimpleMarkerSymbol({
              color: 'red',
              size: 16,
              outline: new this.SimpleLineSymbol({
                color: 'white',
                width: 2,
              }),
            }),
          });

          const numberGraphic = new this.Graphic({
            geometry: orderPoint,
            symbol: new this.TextSymbol({
              text: (index + 1).toString(),
              color: 'white',
              font: { size: 10, weight: 'bold' },
            }),
          });

          routeLayer.addMany([orderGraphic, numberGraphic]);
        });

        // Show route info
        this.$emit('route-calculated', {
          distance: routeData.estimatedDistance,
          duration: routeData.estimatedDuration,
          farmersCount: routeData.farmers.length,
        });
      }
    },

    handleMapClick(event) {
      this.mapView.hitTest(event).then((response) => {
        if (response.results.length > 0) {
          const graphic = response.results[0].graphic;

          if (graphic.attributes && graphic.attributes.type === 'zone') {
            this.selectZone(graphic.attributes);
          } else if (graphic.attributes && graphic.attributes.type === 'farmer') {
            this.$emit('farmer-selected', graphic.attributes);
          }
        } else {
          this.selectedZone = null;
        }
      });
    },

    selectZone(zone) {
      this.selectedZone = { ...zone };
      this.$emit('zone-selected', zone);
      this.displayZones(); // Refresh to show selection
    },

    async saveZone() {
      if (!this.canSaveZone) return;

      this.loading = true;

      try {
        const zoneData = {
          name: this.newZone.name,
          produceType: this.newZone.produceType || null,
          centerLatitude: this.newZone.centerLatitude,
          centerLongitude: this.newZone.centerLongitude,
          radiusKm: this.newZone.radiusKm,
          exporterId: this.$store.getters.currentUser.exporterId,
        };

        const response = await axios.post('/api/admin-service/zones', zoneData);

        if (response.data.success) {
          this.$emit('zone-created', response.data.data);
          this.$emit('show-message', { type: 'success', text: 'Zone created successfully' });
          this.cancelZoneCreation();
          this.mapMode = 'view';
        }
      } catch (error) {
        this.$emit('show-message', { type: 'error', text: 'Failed to create zone' });
      } finally {
        this.loading = false;
      }
    },

    async updateZoneComments() {
      if (!this.selectedZone) return;

      this.loading = true;

      try {
        const response = await axios.put(
          `/api/admin-service/zones/${this.selectedZone.id}/comments`,
          { comments: this.selectedZone.comments },
        );

        if (response.data.success) {
          this.$emit('zone-updated', response.data.data);
          this.$emit('show-message', { type: 'success', text: 'Zone comments updated' });
        }
      } catch (error) {
        this.$emit('show-message', { type: 'error', text: 'Failed to update zone' });
      } finally {
        this.loading = false;
      }
    },

    async deleteZone() {
      if (!this.selectedZone) return;
      if (!this.pendingDelete || this.pendingDelete !== this.selectedZone.id) {
        this.pendingDelete = this.selectedZone.id;
        this.$emit('show-message', { type: 'warning', text: 'Click delete again to confirm zone removal' });
        setTimeout(() => {
          if (this.pendingDelete === this.selectedZone.id) this.pendingDelete = null;
        }, 4000);
        return;
      }
      this.pendingDelete = null;

      this.loading = true;

      try {
        const response = await axios.delete(`/api/admin-service/zones/${this.selectedZone.id}`);

        if (response.data.success) {
          this.$emit('zone-deleted', this.selectedZone.id);
          this.$emit('show-message', { type: 'success', text: 'Zone deleted successfully' });
          this.selectedZone = null;
        }
      } catch (error) {
        this.$emit('show-message', { type: 'error', text: 'Failed to delete zone' });
      } finally {
        this.loading = false;
      }
    },

    resetView() {
      if (this.zones.length > 0) {
        const zoneLayer = this.mapView.map.layers.find((layer) => layer.title === LAYER.ZONES);
        this.mapView.goTo(zoneLayer.graphics.items);
      } else {
        this.mapView.goTo({
          center: [36.8219, -1.2921],
          zoom: 10,
        });
      }
    },

    destroyMap() {
      if (this.mapView) {
        this.mapView.destroy();
        this.mapView = null;
      }
    },
  },
};
</script>

<style scoped>
.arcgis-zone-manager {
  position: relative;
  width: 100%;
  height: 100%;
}

/* Custom ArcGIS styling */
:deep(.esri-view .esri-view-surface--inset-outline:focus::after) {
  outline: none !important;
}

:deep(.esri-attribution) {
  font-size: 10px !important;
}
</style>
