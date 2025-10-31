<!-- eslint-disable sonarjs/no-duplicate-string -->
<template>
  <div class="zone-boundary-validator">
    <v-card class="tw-rounded-xl tw-shadow-md">
      <v-card-title class="tw-bg-gradient-to-r tw-from-purple-500 tw-to-pink-500 tw-text-white tw-rounded-t-xl">
        <v-icon class="tw-mr-2">mdi-vector-polygon</v-icon>
        Zone Boundary Validation
      </v-card-title>

      <v-card-text class="tw-p-0">
        <!-- Map Container -->
        <div :id="mapId" class="tw-h-96 tw-w-full"></div>

        <!-- Validation Controls -->
        <div class="tw-absolute tw-top-16 tw-left-4 tw-z-10 tw-bg-white tw-rounded-lg tw-shadow-md tw-p-4 tw-w-80">
          <h4 class="tw-font-semibold tw-mb-3">Zone Validation</h4>

          <!-- Zone Selection -->
          <v-select
            v-model="selectedZone"
            :items="zones"
            item-title="name"
            item-value="id"
            label="Select Zone to Validate"
            density="compact"
            variant="outlined"
            class="tw-mb-3"
            @update:model-value="validateSelectedZone"
          />

          <!-- Farmer Selection -->
          <v-select
            v-model="selectedFarmer"
            :items="farmers"
            item-title="farmerName"
            item-value="farmerId"
            label="Select Farmer to Validate"
            density="compact"
            variant="outlined"
            class="tw-mb-3"
            @update:model-value="validateSelectedFarmer"
          />

          <!-- Validation Actions -->
          <div class="tw-space-y-2">
            <v-btn
              block
              color="primary"
              size="small"
              @click="validateAllFarmersInZone"
              :loading="validating"
              :disabled="!selectedZone"
            >
              <v-icon left>mdi-check-all</v-icon>
              Validate All Farmers
            </v-btn>

            <v-btn
              block
              color="info"
              size="small"
              @click="checkZoneOverlaps"
              :loading="checkingOverlaps"
              :disabled="!selectedZone"
            >
              <v-icon left>mdi-vector-intersection</v-icon>
              Check Zone Overlaps
            </v-btn>

            <v-btn
              block
              color="success"
              size="small"
              @click="findOptimalZone"
              :loading="findingOptimal"
              :disabled="!selectedFarmer"
            >
              <v-icon left>mdi-target</v-icon>
              Find Optimal Zone
            </v-btn>
          </div>
        </div>

        <!-- Validation Results Panel -->
        <div v-if="validationResults.length > 0" class="tw-absolute tw-bottom-4 tw-right-4 tw-z-10 tw-bg-white tw-rounded-lg tw-shadow-md tw-p-4 tw-w-80 tw-max-h-64 tw-overflow-y-auto">
          <h4 class="tw-font-semibold tw-mb-3">Validation Results</h4>

          <div class="tw-space-y-3">
            <div
              v-for="result in validationResults"
              :key="result.id"
              :class="getResultCardClass(result)"
              class="tw-p-3 tw-rounded-lg tw-border"
            >
              <div class="tw-flex tw-items-center tw-justify-between tw-mb-2">
                <span class="tw-font-medium tw-text-sm">{{ result.farmerName }}</span>
                <v-chip
                  :color="result.isValid ? 'green' : 'red'"
                  size="x-small"
                  variant="flat"
                >
                  {{ result.isValid ? 'Valid' : 'Invalid' }}
                </v-chip>
              </div>

              <div class="tw-text-xs tw-text-gray-600">
                <div>Distance: {{ result.distance }}km</div>
                <div>Zone: {{ result.zoneName }}</div>
                <div class="tw-mt-1">{{ result.message }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- Zone Overlap Results -->
        <div v-if="overlapResults.length > 0" class="tw-absolute tw-top-16 tw-right-4 tw-z-10 tw-bg-white tw-rounded-lg tw-shadow-md tw-p-4 tw-w-80 tw-max-h-64 tw-overflow-y-auto">
          <h4 class="tw-font-semibold tw-mb-3">Zone Overlaps</h4>

          <div class="tw-space-y-3">
            <div
              v-for="overlap in overlapResults"
              :key="overlap.zoneId"
              class="tw-p-3 tw-rounded-lg tw-border tw-border-orange-200 tw-bg-orange-50"
            >
              <div class="tw-flex tw-items-center tw-justify-between tw-mb-2">
                <span class="tw-font-medium tw-text-sm">{{ overlap.zoneName }}</span>
                <v-chip color="orange" size="x-small" variant="flat">
                  {{ overlap.overlapPercentage }}% overlap
                </v-chip>
              </div>

              <div class="tw-text-xs tw-text-gray-600">
                <div>Distance: {{ overlap.distance }}km</div>
                <div>Overlap: {{ overlap.overlapDistance }}km</div>
              </div>
            </div>
          </div>
        </div>
      </v-card-text>
    </v-card>

    <!-- Loading Overlay -->
    <v-overlay v-model="loading" class="tw-flex tw-items-center tw-justify-center">
      <v-progress-circular indeterminate size="64" />
    </v-overlay>
  </div>
</template>
<script>
import axios from 'axios';

export default {
  name: 'ZoneBoundaryValidator',
  props: {
    zones: {
      type: Array,
      default: () => [],
    },
    farmers: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      mapId: `zone-boundary-validator-${Math.random().toString(36).substr(2, 9)}`,
      mapView: null,
      selectedZone: null,
      selectedFarmer: null,
      validationResults: [],
      overlapResults: [],
      loading: false,
      validating: false,
      checkingOverlaps: false,
      findingOptimal: false,

      // ArcGIS modules
      Map: null,
      MapView: null,
      Graphic: null,
      GraphicsLayer: null,
      Circle: null,
      Point: null,
      SimpleMarkerSymbol: null,
      SimpleFillSymbol: null,
      SimpleLineSymbol: null,
      TextSymbol: null,
    };
  },
  mounted() {
    this.loadArcGIS().then(() => {
      this.initializeMap();
    });
  },
  beforeDestroy() {
    this.destroyMap();
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
          'esri/symbols/SimpleMarkerSymbol',
          'esri/symbols/SimpleFillSymbol',
          'esri/symbols/SimpleLineSymbol',
          'esri/symbols/TextSymbol',
        ], (Map, MapView, Graphic, GraphicsLayer, Circle, Point,
          SimpleMarkerSymbol, SimpleFillSymbol, SimpleLineSymbol, TextSymbol) => {
          this.Map = Map;
          this.MapView = MapView;
          this.Graphic = Graphic;
          this.GraphicsLayer = GraphicsLayer;
          this.Circle = Circle;
          this.Point = Point;
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

      const map = new this.Map({
        basemap: 'hybrid',
      });

      // Create graphics layers
      const zoneLayer = new this.GraphicsLayer({ title: 'Zones' });
      const farmerLayer = new this.GraphicsLayer({ title: 'Farmers' });
      const validationLayer = new this.GraphicsLayer({ title: 'Validation' });
      const overlapLayer = new this.GraphicsLayer({ title: 'Overlaps' });

      map.addMany([zoneLayer, farmerLayer, validationLayer, overlapLayer]);

      this.mapView = new this.MapView({
        container: this.mapId,
        map,
        center: [36.8219, -1.2921], // Nairobi, Kenya
        zoom: 10,
        ui: {
          components: ['attribution'],
        },
      });

      // Display initial data
      this.displayZones();
      this.displayFarmers();
    },

    displayZones() {
      if (!this.mapView) return;

      const zoneLayer = this.mapView.map.layers.find((layer) => layer.title === 'Zones');
      zoneLayer.removeAll();

      // eslint-disable-next-line no-unused-vars
      this.zones.forEach((zone, index) => {
        const center = new this.Point({
          longitude: zone.centerLongitude,
          latitude: zone.centerLatitude,
        });

        const circle = new this.Circle({
          center,
          radius: zone.radiusKm,
          radiusUnit: 'kilometers',
        });

        const isSelected = this.selectedZone === zone.id;
        const color = isSelected ? [255, 165, 0, 0.3] : [0, 100, 255, 0.2];

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
            color: isSelected ? 'orange' : 'blue',
            size: 8,
          }),
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
        });

        zoneLayer.addMany([circleGraphic, centerGraphic, labelGraphic]);
      });
    },

    displayFarmers() {
      if (!this.mapView) return;

      const farmerLayer = this.mapView.map.layers.find((layer) => layer.title === 'Farmers');
      farmerLayer.removeAll();

      this.farmers.forEach((farmer) => {
        if (!farmer.location) return;

        const point = new this.Point({
          longitude: farmer.location.longitude,
          latitude: farmer.location.latitude,
        });

        const isSelected = this.selectedFarmer === farmer.farmerId;
        const color = isSelected ? 'orange' : 'green';

        const farmerGraphic = new this.Graphic({
          geometry: point,
          symbol: new this.SimpleMarkerSymbol({
            color,
            size: isSelected ? 12 : 8,
            outline: new this.SimpleLineSymbol({
              color: 'white',
              width: 2,
            }),
          }),
          attributes: { ...farmer, type: 'farmer' },
        });

        farmerLayer.add(farmerGraphic);
      });
    },

    async validateSelectedZone() {
      this.displayZones();
      this.clearValidationResults();
    },

    async validateSelectedFarmer() {
      this.displayFarmers();
      this.clearValidationResults();
    },

    async validateAllFarmersInZone() {
      if (!this.selectedZone) return;

      this.validating = true;
      this.validationResults = [];

      try {
        const zone = this.zones.find((z) => z.id === this.selectedZone);
        // eslint-disable-next-line consistent-return
        const promises = this.farmers.map(async (farmer) => {
          try {
            const response = await axios.post('/api/geospatial/validate-farmer-in-zone', {
              farmerId: farmer.farmerId,
              zoneId: this.selectedZone,
            });

            if (response.data.success) {
              const result = response.data.data;
              return {
                id: farmer.farmerId,
                farmerName: farmer.farmerName,
                isValid: result.isValid,
                distance: result.distance,
                zoneName: result.zoneName,
                message: result.validationMessage,
              };
            }
          } catch (error) {
            return {
              id: farmer.farmerId,
              farmerName: farmer.farmerName,
              isValid: false,
              distance: 0,
              zoneName: zone.name,
              message: 'Validation failed',
            };
          }
        });

        const results = await Promise.all(promises);
        this.validationResults = results.filter((r) => r !== null);

        this.displayValidationResults();
        this.$emit('validation-completed', this.validationResults);
      } catch (error) {
        // eslint-disable-next-line sonarjs/no-duplicate-string
        this.$emit('show-message', { type: 'error', text: 'Failed to validate farmers' });
      } finally {
        this.validating = false;
      }
    },

    async checkZoneOverlaps() {
      if (!this.selectedZone) return;

      this.checkingOverlaps = true;
      this.overlapResults = [];

      try {
        const zone = this.zones.find((z) => z.id === this.selectedZone);
        const response = await axios.post('/api/geospatial/validate-zone-boundaries', {
          name: zone.name,
          produceType: zone.produceType,
          centerLatitude: zone.centerLatitude,
          centerLongitude: zone.centerLongitude,
          radiusKm: zone.radiusKm,
          exporterId: zone.exporterId,
        }, {
          params: { excludeZoneId: this.selectedZone },
        });

        if (response.data.success) {
          const result = response.data.data;
          this.overlapResults = result.overlaps.map((overlap) => ({
            zoneId: overlap.existingZone.id,
            zoneName: overlap.existingZone.name,
            distance: overlap.distance,
            overlapDistance: overlap.overlapDistance,
            overlapPercentage: overlap.overlapPercentage,
          }));

          this.displayOverlapResults();
          this.$emit('overlaps-found', this.overlapResults);
        }
      } catch (error) {
        this.$emit('show-message', { type: 'error', text: 'Failed to check zone overlaps' });
      } finally {
        this.checkingOverlaps = false;
      }
    },

    async findOptimalZone() {
      if (!this.selectedFarmer) return;

      this.findingOptimal = true;

      try {
        const response = await axios.post('/api/geospatial/find-optimal-zone', {
          farmerId: this.selectedFarmer,
          exporterId: this.$store.getters.currentUser.exporterId,
        });

        if (response.data.success) {
          const result = response.data.data;
          this.highlightOptimalZone(result.recommendedZone);
          this.$emit('optimal-zone-found', result);
          this.$emit('show-message', {
            type: 'success',
            text: `Optimal zone: ${result.recommendedZone.name} (${result.distance}km away)`,
          });
        }
      } catch (error) {
        this.$emit('show-message', { type: 'error', text: 'Failed to find optimal zone' });
      } finally {
        this.findingOptimal = false;
      }
    },

    displayValidationResults() {
      const validationLayer = this.mapView.map.layers.find((layer) => layer.title === 'Validation');
      validationLayer.removeAll();

      this.validationResults.forEach((result) => {
        const farmer = this.farmers.find((f) => f.farmerId === result.id);
        if (!farmer || !farmer.location) return;

        const point = new this.Point({
          longitude: farmer.location.longitude,
          latitude: farmer.location.latitude,
        });

        const color = result.isValid ? 'green' : 'red';
        const validationGraphic = new this.Graphic({
          geometry: point,
          symbol: new this.SimpleMarkerSymbol({
            color,
            size: 6,
            style: 'circle',
          }),
        });

        validationLayer.add(validationGraphic);
      });
    },

    displayOverlapResults() {
      const overlapLayer = this.mapView.map.layers.find((layer) => layer.title === 'Overlaps');
      overlapLayer.removeAll();

      this.overlapResults.forEach((overlap) => {
        const zone = this.zones.find((z) => z.id === overlap.zoneId);
        if (!zone) return;

        const center = new this.Point({
          longitude: zone.centerLongitude,
          latitude: zone.centerLatitude,
        });

        const circle = new this.Circle({
          center,
          radius: zone.radiusKm,
          radiusUnit: 'kilometers',
        });

        const overlapGraphic = new this.Graphic({
          geometry: circle,
          symbol: new this.SimpleFillSymbol({
            color: [255, 0, 0, 0.3],
            outline: new this.SimpleLineSymbol({
              color: [255, 0, 0, 0.8],
              width: 3,
              style: 'dash',
            }),
          }),
        });

        overlapLayer.add(overlapGraphic);
      });
    },

    highlightOptimalZone(zone) {
      const validationLayer = this.mapView.map.layers.find((layer) => layer.title === 'Validation');
      validationLayer.removeAll();

      const center = new this.Point({
        longitude: zone.centerLongitude,
        latitude: zone.centerLatitude,
      });

      const circle = new this.Circle({
        center,
        radius: zone.radiusKm,
        radiusUnit: 'kilometers',
      });

      const highlightGraphic = new this.Graphic({
        geometry: circle,
        symbol: new this.SimpleFillSymbol({
          color: [0, 255, 0, 0.4],
          outline: new this.SimpleLineSymbol({
            color: [0, 255, 0, 1],
            width: 4,
          }),
        }),
      });

      validationLayer.add(highlightGraphic);
    },

    clearValidationResults() {
      this.validationResults = [];
      this.overlapResults = [];

      const validationLayer = this.mapView.map.layers.find((layer) => layer.title === 'Validation');
      const overlapLayer = this.mapView.map.layers.find((layer) => layer.title === 'Overlaps');

      validationLayer.removeAll();
      overlapLayer.removeAll();
    },

    getResultCardClass(result) {
      return result.isValid
        ? 'tw-border-green-200 tw-bg-green-50'
        : 'tw-border-red-200 tw-bg-red-50';
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
/* Custom gradient background */
.tw-bg-gradient-to-r {
  background: linear-gradient(to right, #8b5cf6, #ec4899);
}

/* Custom scrollbar */
:deep(.tw-overflow-y-auto) {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e0 #f7fafc;
}

:deep(.tw-overflow-y-auto::-webkit-scrollbar) {
  width: 6px;
}

:deep(.tw-overflow-y-auto::-webkit-scrollbar-track) {
  background: #f7fafc;
}

:deep(.tw-overflow-y-auto::-webkit-scrollbar-thumb) {
  background-color: #cbd5e0;
  border-radius: 3px;
}

/* ArcGIS styling */
:deep(.esri-view .esri-view-surface--inset-outline:focus::after) {
  outline: none !important;
}

:deep(.esri-attribution) {
  font-size: 10px !important;
}
</style>
