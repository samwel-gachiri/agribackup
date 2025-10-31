<template>
  <div class="tw-h-full tw-flex tw-flex-col tw-bg-white tw-relative" :class="{ 'tw-fixed tw-inset-0 tw-z-50 tw-bottom-0 tw-left-0 tw-right-0 tw-top-0': isFullscreen }">
    <!-- Map Container (Full Height) -->
    <div ref="mapView" class="tw-w-full tw-h-full"></div>

    <!-- Toolbar: Drawing Tools + Basemap Toggle + Fullscreen (Top Right) -->
    <div class="tw-absolute tw-top-3 tw-right-3 tw-z-20 tw-bg-white tw-rounded-lg tw-shadow-lg tw-p-3 tw-flex tw-gap-2 tw-items-center">
      <!-- Drawing Tools -->
      <div class="tw-flex tw-gap-2">
        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              v-on="on"
              small
              :color="isDrawing ? 'primary' : 'default'"
              :disabled="isDrawing || loading"
              @click="startDrawing"
              icon
            >
              <v-icon small>mdi-pencil</v-icon>
            </v-btn>
          </template>
          <span>Draw Unit</span>
        </v-tooltip>

        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              v-on="on"
              small
              color="red"
              :disabled="!unitPolygon || loading"
              @click="clearDrawing"
              icon
            >
              <v-icon small>mdi-eraser</v-icon>
            </v-btn>
          </template>
          <span>Clear</span>
        </v-tooltip>

        <v-tooltip bottom>
          <template #activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              v-on="on"
              small
              color="success"
              :disabled="!unitPolygon || loading"
              @click="saveUnit"
              icon
            >
              <v-icon small>mdi-check-circle</v-icon>
            </v-btn>
          </template>
          <span>Done</span>
        </v-tooltip>
      </div>

      <!-- Separator -->
      <v-divider vertical></v-divider>

      <!-- Basemap Toggle -->
      <v-menu offset-y>
        <template #activator="{ on, attrs }">
          <v-tooltip bottom>
            <template #activator="{ on: onTooltip, attrs: attrsTooltip }">
              <v-btn
                v-bind="{ ...attrs, ...attrsTooltip }"
                v-on="{ ...on, ...onTooltip }"
                small
                icon
              >
                <v-icon small>mdi-layers</v-icon>
              </v-btn>
            </template>
            <span>Map Layer</span>
          </v-tooltip>
        </template>
        <v-list dense>
          <v-list-item @click="changeBasemap('hybrid')">
            <v-list-item-title>
              <v-icon small :color="currentBasemap === 'hybrid' ? 'primary' : ''">
                mdi-check-circle
              </v-icon>
              Hybrid (Satellite + Labels)
            </v-list-item-title>
          </v-list-item>
          <v-list-item @click="changeBasemap('satellite')">
            <v-list-item-title>
              <v-icon small :color="currentBasemap === 'satellite' ? 'primary' : ''">
                mdi-check-circle
              </v-icon>
              Satellite Only
            </v-list-item-title>
          </v-list-item>
          <v-list-item @click="changeBasemap('streets')">
            <v-list-item-title>
              <v-icon small :color="currentBasemap === 'streets' ? 'primary' : ''">
                mdi-check-circle
              </v-icon>
              Street Map
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>

      <!-- Separator -->
      <v-divider vertical></v-divider>

      <!-- Fullscreen Toggle -->
      <v-tooltip bottom>
        <template #activator="{ on, attrs }">
          <v-btn
            v-bind="attrs"
            v-on="on"
            small
            :color="isFullscreen ? 'primary' : 'default'"
            @click="toggleFullscreen"
            icon
          >
            <v-icon small>{{ isFullscreen ? 'mdi-fullscreen-exit' : 'mdi-fullscreen' }}</v-icon>
          </v-btn>
        </template>
        <span>{{ isFullscreen ? 'Exit Fullscreen' : 'Fullscreen' }}</span>
      </v-tooltip>
    </div>

    <!-- Coordinates Display (Top Right) -->
    <!-- <div v-if="farmerLocation" class="tw-absolute tw-top-3 tw-right-3 tw-z-20">
      <v-chip small color="white" text-color="green darken-2" class="tw-shadow-lg">
        <v-icon left x-small>mdi-crosshairs-gps</v-icon>
        {{ farmerLocation.lat.toFixed(4) }}, {{ farmerLocation.lng.toFixed(4) }}
      </v-chip>
    </div> -->

    <!-- Unit Info (Bottom Left) -->
    <div v-if="unitPolygon" class="tw-absolute tw-bottom-3 tw-left-3 tw-z-20">
      <v-card class="tw-shadow-lg">
        <v-card-text class="tw-py-2 tw-px-3">
          <div class="tw-text-xs tw-space-y-1">
            <div><strong>Area:</strong> {{ unitArea.toFixed(2) }} ha</div>
            <div><strong>Perimeter:</strong> {{ unitPerimeter.toFixed(0) }} m</div>
            <div><strong>Points:</strong> {{ unitPointCount }}</div>
          </div>
        </v-card-text>
      </v-card>
    </div>

    <!-- Status Indicator (Bottom Right) -->
    <div v-if="isDrawing" class="tw-absolute tw-bottom-3 tw-right-3 tw-z-20">
      <v-card class="tw-shadow-lg tw-bg-blue-50">
        <v-card-text class="tw-py-2 tw-px-3">
          <div class="tw-text-xs tw-font-medium tw-text-blue-700">
            Drawing: {{ drawingPoints.length }} points
          </div>
        </v-card-text>
      </v-card>
    </div>

    <!-- Loading Overlay -->
    <v-overlay :value="loading" absolute>
      <v-progress-circular
        indeterminate
        size="64"
        color="green"
      ></v-progress-circular>
      <div class="tw-mt-4 tw-text-white tw-font-medium tw-text-center">{{ loadingMessage }}</div>
    </v-overlay>

    <!-- Success/Error Snackbars -->
    <v-snackbar
        v-model="showSuccessMessage"
        color="success"
        timeout="3000"
        bottom
        left
    >
      {{ successMessage }}
      <template v-slot:action="{ attrs }">
        <v-btn text v-bind="attrs" @click="showSuccessMessage = false">
          Close
        </v-btn>
      </template>
    </v-snackbar>

    <v-snackbar
        v-model="showErrorMessage"
        color="error"
        timeout="5000"
        bottom
        left
    >
      {{ errorMessage }}
      <template v-slot:action="{ attrs }">
        <v-btn text v-bind="attrs" @click="showErrorMessage = false">
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script>
import { loadModules, loadCss } from 'esri-loader';

export default {
  name: 'ProductionUnitDrawer',
  props: {
    initialLocation: {
      type: Object,
      default: () => ({ lat: -1.2921, lng: 36.8219 }),
    },
    existingUnits: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      // Map and ArcGIS objects
      map: null,
      view: null,
      graphicsLayer: null,
      Map: null,
      MapView: null,
      Graphic: null,
      GraphicsLayer: null,
      Point: null,
      Polygon: null,
      geometryEngine: null,

      unitGeometry: null,
      unitArea: 0,
      unitPerimeter: 0,
      unitPointCount: 0,

      // Farmer location
      farmerLocation: null,

      // Drawing state
      isDrawing: false,
      unitPolygon: null,
      drawingPoints: [],

      // UI state
      loading: true,
      loadingMessage: 'Initializing map...',
      showSuccessMessage: false,
      showErrorMessage: false,
      successMessage: '',
      errorMessage: '',
      isFullscreen: false,
      currentBasemap: 'hybrid',

      // Symbol configurations
      POLYGON_SYMBOL: {
        type: 'simple-fill',
        color: [51, 153, 51, 0.4],
        outline: {
          color: [51, 153, 51, 0.8],
          width: 2,
        },
      },
    };
  },

  async mounted() {
    this.farmerLocation = {
      lat: this.initialLocation.lat || -1.2921,
      lng: this.initialLocation.lng || 36.8219,
    };
    // Add keyboard event listener for Escape key
    document.addEventListener('keydown', this.handleKeydown);

    // Load ArcGIS modules using esri-loader
    await this.loadArcGIS();
    this.$nextTick(() => {
      this.initializeMap();
    });
  },

  beforeDestroy() {
    // Remove keyboard event listener
    document.removeEventListener('keydown', this.handleKeydown);
    if (this.view) {
      this.view.destroy();
    }
  },

  methods: {
    async loadArcGIS() {
      try {
        // Check if ArcGIS is already loaded (e.g., by parent component)
        if (window.require && window.require.defined && window.require.defined('esri/Map')) {
          // Load modules using existing require
          const [
            Map,
            MapView,
            Graphic,
            GraphicsLayer,
            Point,
            Polygon,
            geometryEngine,
          ] = await new Promise((resolve, reject) => {
            window.require([
              'esri/Map',
              'esri/views/MapView',
              'esri/Graphic',
              'esri/layers/GraphicsLayer',
              'esri/geometry/Point',
              'esri/geometry/Polygon',
              'esri/geometry/geometryEngine',
            ], (...modules) => resolve(modules), reject);
          });

          this.Map = Map;
          this.MapView = MapView;
          this.Graphic = Graphic;
          this.GraphicsLayer = GraphicsLayer;
          this.Point = Point;
          this.Polygon = Polygon;
          this.geometryEngine = geometryEngine;
          return;
        }

        // Load CSS (only if not already loaded)
        if (!document.querySelector('link[href*="arcgis"]')) {
          loadCss('https://js.arcgis.com/4.28/esri/themes/light/main.css');
        }

        // Load ArcGIS modules using esri-loader
        const [
          Map,
          MapView,
          Graphic,
          GraphicsLayer,
          Point,
          Polygon,
          geometryEngine,
        ] = await loadModules([
          'esri/Map',
          'esri/views/MapView',
          'esri/Graphic',
          'esri/layers/GraphicsLayer',
          'esri/geometry/Point',
          'esri/geometry/Polygon',
          'esri/geometry/geometryEngine',
        ], { version: '4.28' });

        // Store modules in data
        this.Map = Map;
        this.MapView = MapView;
        this.Graphic = Graphic;
        this.GraphicsLayer = GraphicsLayer;
        this.Point = Point;
        this.Polygon = Polygon;
        this.geometryEngine = geometryEngine;
      } catch (error) {
        this.showError('Failed to load map resources');
        this.loading = false;
      }
    },

    initializeMap() {
      try {
        if (this.view || !this.Map) return;

        const el = this.$refs.mapView;
        if (!el) {
          this.showError('Map container not found');
          this.loading = false;
          return;
        }

        this.loadingMessage = 'Setting up map...';

        // Create graphics layer for production units
        this.graphicsLayer = new this.GraphicsLayer({
          title: 'Production Units',
        });

        // Create map
        this.map = new this.Map({
          basemap: 'hybrid',
          layers: [this.graphicsLayer],
        });

        // Create map view
        this.view = new this.MapView({
          container: el,
          map: this.map,
          center: [this.farmerLocation.lng, this.farmerLocation.lat],
          zoom: 17,
        });

        // Wait for view to load then setup drawing
        this.view.when(() => {
          this.setupDrawingTools();
          this.addFarmerLocationMarker();
          this.displayExistingUnits();
          this.loading = false;
          this.showSuccess('Map loaded successfully');
        }).catch((error) => {
          this.showError(`Failed to initialize map: ${error.message}`);
          this.loading = false;
        });
      } catch (error) {
        this.showError(`Map setup failed: ${error.message}`);
        this.loading = false;
      }
    },

    setupDrawingTools() {
      if (!this.view) return;

      // Simple polygon drawing with click handlers
      let drawingPoints = [];
      let tempGraphic = null;

      this.view.on('click', (event) => {
        if (!this.isDrawing) return;

        const point = this.view.toMap(event);
        if (!point) return;

        drawingPoints.push([point.longitude, point.latitude]);

        // Remove temporary graphic
        if (tempGraphic) {
          this.graphicsLayer.remove(tempGraphic);
        }

        // Draw temporary polygon
        if (drawingPoints.length >= 2) {
          const rings = [drawingPoints];
          const polygon = new this.Polygon({
            rings,
            spatialReference: { wkid: 4326 },
          });

          tempGraphic = new this.Graphic({
            geometry: polygon,
            symbol: this.POLYGON_SYMBOL,
          });

          this.graphicsLayer.add(tempGraphic);
        }
      });

      // Double-click to finish drawing
      this.view.on('double-click', (event) => {
        if (!this.isDrawing || drawingPoints.length < 3) return;

        event.stopPropagation();

        // Close the polygon
        const rings = [drawingPoints];
        const polygon = new this.Polygon({
          rings,
          spatialReference: { wkid: 4326 },
        });

        // Remove temporary graphic
        if (tempGraphic) {
          this.graphicsLayer.remove(tempGraphic);
        }

        // Add final polygon
        const finalGraphic = new this.Graphic({
          geometry: polygon,
          symbol: this.POLYGON_SYMBOL,
        });

        this.graphicsLayer.add(finalGraphic);

        // Store geometry and calculate area
        this.unitGeometry = polygon;
        this.unitPolygon = polygon;
        this.calculateUnitArea(polygon);

        // Reset drawing state
        this.isDrawing = false;
        drawingPoints = [];
        tempGraphic = null;

        this.showSuccess('Polygon drawn successfully');
        this.$emit('unit-drawn', {
          geometry: this.unitGeometry,
          area: this.unitArea,
        });
      });
    },

    addFarmerLocationMarker() {
      if (!this.graphicsLayer || !this.Graphic || !this.Point) return;

      const locationGraphic = new this.Graphic({
        geometry: new this.Point({
          longitude: this.farmerLocation.lng,
          latitude: this.farmerLocation.lat,
        }),
        symbol: {
          type: 'simple-marker',
          color: [0, 100, 255],
          size: 12,
          outline: {
            color: [255, 255, 255],
            width: 2,
          },
        },
        attributes: {
          title: 'Farm Center',
        },
      });

      this.graphicsLayer.add(locationGraphic);
    },

    displayExistingUnits() {
      if (!this.existingUnits || this.existingUnits.length === 0 || !this.Graphic) return;

      try {
        this.existingUnits.forEach((unit) => {
          if (unit.geometry) {
            const graphic = new this.Graphic({
              geometry: {
                type: 'polygon',
                rings: unit.geometry.coordinates || unit.geometry.rings,
                spatialReference: { wkid: 4326 },
              },
              symbol: {
                type: 'simple-fill',
                color: [100, 100, 100, 0.3],
                outline: {
                  color: [100, 100, 100, 0.8],
                  width: 1.5,
                },
              },
              attributes: {
                title: unit.name || 'Production Unit',
                unitId: unit.id,
              },
            });
            this.graphicsLayer.add(graphic);
          }
        });
      } catch (error) {
        // Handle error if needed
      }
    },

    startDrawing() {
      if (!this.view) {
        this.showError('Map not initialized');
        return;
      }
      this.isDrawing = true;
      this.showSuccess('Click on the map to draw polygon vertices. Double-click to finish.');
    },

    calculateUnitArea(polygon) {
      if (!polygon || !this.geometryEngine) {
        this.unitArea = 0;
        this.unitPerimeter = 0;
        return;
      }

      try {
        // Calculate area in square meters using geodesic (works with geographic coordinates)
        const areaInSquareMeters = this.geometryEngine.geodesicArea(polygon, 'square-meters');
        const perimeterInMeters = this.geometryEngine.geodesicLength(polygon, 'meters');

        // Convert to hectares (1 hectare = 10000 square meters)
        this.unitArea = Math.abs(areaInSquareMeters) / 10000;
        this.unitPerimeter = Math.abs(perimeterInMeters);

        if (polygon.rings && polygon.rings[0]) {
          this.unitPointCount = polygon.rings[0].length - 1;
        }
      } catch (error) {
        this.showError('Error calculating area');
        this.unitArea = 0;
        this.unitPerimeter = 0;
      }
    },

    clearDrawing() {
      if (this.graphicsLayer) {
        // Remove all graphics except the farmer location marker and existing units
        const graphics = this.graphicsLayer.graphics.toArray();
        // Keep only the first graphic (farmer location) and graphics with existing unit data
        const toRemove = graphics.filter((g, index) => index > 0 && !g.attributes?.isExisting);
        this.graphicsLayer.removeMany(toRemove);
      }

      this.unitPolygon = null;
      this.unitGeometry = null;
      this.unitArea = 0;
      this.unitPerimeter = 0;
      this.unitPointCount = 0;
      this.isDrawing = false;
      this.drawingPoints = [];
      this.showSuccess('Cleared drawing');
    },

    saveUnit() {
      if (!this.unitPolygon) {
        this.showError('No unit boundary to save.');
        return;
      }

      try {
        // Emit event with unit data
        this.$emit('save-unit', {
          geometry: this.unitPolygon,
          area: this.unitArea,
          perimeter: this.unitPerimeter,
          pointCount: this.unitPointCount,
        });

        this.showSuccess(`Unit saved! Area: ${this.unitArea.toFixed(5)} hectares`);
      } catch (error) {
        this.showError('Failed to save unit. Please try again.');
      }
    },

    showSuccess(message) {
      this.successMessage = message;
      this.showSuccessMessage = true;
    },

    showError(message) {
      this.errorMessage = message;
      this.showErrorMessage = true;
    },

    toggleFullscreen() {
      this.isFullscreen = !this.isFullscreen;
      if (this.view) {
        // Force map to resize after fullscreen toggle
        setTimeout(() => {
          this.view.resize();
        }, 100);
      }
    },

    changeBasemap(basemapName) {
      if (this.map) {
        this.map.basemap = basemapName;
        this.currentBasemap = basemapName;
      }
    },

    handleKeydown(event) {
      // Exit fullscreen when Escape is pressed
      if (event.key === 'Escape' && this.isFullscreen) {
        this.isFullscreen = false;
      }
    },
  },
};
</script>

<style scoped>
/* Ensure proper z-indexing for floating elements */
.tw-z-10 {
  z-index: 10;
}

/* Custom scrollbar for small screens */
::-webkit-scrollbar {
  width: 4px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #888;
  border-radius: 2px;
}

::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>
