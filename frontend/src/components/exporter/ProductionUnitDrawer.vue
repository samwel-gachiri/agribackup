<template>
  <div class="production-unit-drawer">
    <!-- Top Bar with Drawing Tools -->
    <div class="top-bar">
      <!-- Left: Instructions Toggle -->
      <div class="top-bar-left">
        <v-btn
          v-if="!isDrawing"
          small
          text
          :color="showInstructions ? 'primary' : 'grey'"
          @click="showInstructions = !showInstructions"
        >
          <v-icon small left>mdi-help-circle-outline</v-icon>
          Help
        </v-btn>
        <v-chip v-if="isDrawing" small color="blue" text-color="white" class="tw-animate-pulse">
          <v-icon small left>mdi-pencil</v-icon>
          Drawing: {{ currentDrawingPoints }} points
        </v-chip>
      </div>

      <!-- Center: Main Drawing Controls -->
      <div class="top-bar-center">
        <v-btn
          small
          :color="isDrawing ? 'primary' : 'success'"
          :disabled="isDrawing || loading"
          @click="startDrawing"
          class="tw-mx-1"
        >
          <v-icon small left>mdi-pencil</v-icon>
          {{ isDrawing ? 'Drawing...' : 'Draw' }}
        </v-btn>

        <v-btn
          small
          outlined
          color="error"
          :disabled="!unitPolygon && !isDrawing || loading"
          @click="clearDrawing"
          class="tw-mx-1"
        >
          <v-icon small left>mdi-eraser</v-icon>
          Clear
        </v-btn>

        <v-btn
          small
          color="success"
          :disabled="!unitPolygon || loading"
          @click="saveUnit"
          class="tw-mx-1"
        >
          <v-icon small left>mdi-check</v-icon>
          Done
        </v-btn>

        <v-divider vertical class="tw-mx-2"></v-divider>

        <v-text-field
          v-model="searchQuery"
          placeholder="Search location..."
          dense
          solo
          flat
          hide-details
          clearable
          prepend-inner-icon="mdi-magnify"
          style="max-width: 200px;"
          @keyup.enter="searchLocation"
          @click:clear="searchQuery = ''"
        />
      </div>

      <!-- Right: Map Controls -->
      <div class="top-bar-right">
        <v-btn x-small icon @click="zoomIn" :disabled="loading" class="tw-mx-1">
          <v-icon small>mdi-plus</v-icon>
        </v-btn>
        <v-btn x-small icon @click="zoomOut" :disabled="loading" class="tw-mx-1">
          <v-icon small>mdi-minus</v-icon>
        </v-btn>
        <v-btn small icon :loading="gettingLocation" @click="goToMyLocation" class="tw-mx-1">
          <v-icon small>mdi-crosshairs-gps</v-icon>
        </v-btn>
        <v-menu offset-y>
          <template #activator="{ on, attrs }">
            <v-btn v-bind="attrs" v-on="on" small icon class="tw-mx-1">
              <v-icon small>mdi-layers</v-icon>
            </v-btn>
          </template>
          <v-list dense>
            <v-subheader class="tw-text-xs">MAP STYLE</v-subheader>
            <v-list-item @click="changeBasemap('hybrid')" :class="{ 'v-list-item--active': currentBasemap === 'hybrid' }">
              <v-list-item-icon class="tw-mr-2"><v-icon small>mdi-satellite-variant</v-icon></v-list-item-icon>
              <v-list-item-title class="tw-text-sm">Satellite + Labels</v-list-item-title>
            </v-list-item>
            <v-list-item @click="changeBasemap('satellite')" :class="{ 'v-list-item--active': currentBasemap === 'satellite' }">
              <v-list-item-icon class="tw-mr-2"><v-icon small>mdi-satellite</v-icon></v-list-item-icon>
              <v-list-item-title class="tw-text-sm">Satellite Only</v-list-item-title>
            </v-list-item>
            <v-list-item @click="changeBasemap('streets')" :class="{ 'v-list-item--active': currentBasemap === 'streets' }">
              <v-list-item-icon class="tw-mr-2"><v-icon small>mdi-map</v-icon></v-list-item-icon>
              <v-list-item-title class="tw-text-sm">Street Map</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </div>
    </div>

    <!-- Main Content Area -->
    <div class="main-content">
      <!-- Left Sidebar: Instructions or Drawing Status -->
      <div v-if="showInstructions || isDrawing" class="left-sidebar">
        <!-- Instructions Panel -->
        <v-card v-if="showInstructions && !isDrawing && !unitPolygon" class="tw-mb-2" outlined>
          <v-card-text class="tw-py-2 tw-px-3">
            <div class="tw-font-semibold tw-text-sm tw-text-gray-800 tw-mb-2">
              <v-icon small color="green" class="tw-mr-1">mdi-help-circle</v-icon>
              How to Draw
            </div>
            <div class="tw-space-y-1 tw-text-xs tw-text-gray-600">
              <div><v-chip x-small color="primary">1</v-chip> Click "Draw" to start</div>
              <div><v-chip x-small color="primary">2</v-chip> Click map to add points</div>
              <div><v-chip x-small color="primary">3</v-chip> Double-click to finish</div>
              <div><v-chip x-small color="success">4</v-chip> Click "Done" to confirm</div>
            </div>
          </v-card-text>
        </v-card>

        <!-- Drawing Status Panel -->
        <v-card v-if="isDrawing" class="tw-border-l-4 tw-border-blue-500" outlined>
          <v-card-text class="tw-py-2 tw-px-3">
            <div class="tw-font-semibold tw-text-sm tw-text-blue-800 tw-mb-2">
              Drawing Mode Active
            </div>
            <div class="tw-text-sm">
              Points: <v-chip small :color="currentDrawingPoints >= 3 ? 'success' : 'warning'">{{ currentDrawingPoints }}</v-chip>
            </div>
            <div v-if="currentDrawingPoints < 3" class="tw-text-xs tw-text-orange-600 tw-mt-1">
              Need at least 3 points
            </div>
            <div v-else class="tw-text-xs tw-text-green-600 tw-mt-1">
              Double-click to complete
            </div>
            <v-btn v-if="currentDrawingPoints > 0" x-small text color="error" class="tw-mt-2" @click="undoLastPoint">
              <v-icon x-small left>mdi-undo</v-icon> Undo
            </v-btn>
          </v-card-text>
        </v-card>

        <!-- Boundary Info Panel -->
        <v-card v-if="unitPolygon" class="tw-border-l-4 tw-border-green-500" outlined>
          <v-card-text class="tw-py-2 tw-px-3">
            <div class="tw-font-semibold tw-text-sm tw-text-green-800 tw-mb-2">
              <v-icon small color="green" class="tw-mr-1">mdi-check-circle</v-icon>
              Boundary Complete
            </div>
            <div class="tw-grid tw-grid-cols-3 tw-gap-2 tw-text-center tw-text-xs">
              <div>
                <div class="tw-font-bold tw-text-green-600">{{ unitArea.toFixed(2) }}</div>
                <div class="tw-text-gray-500">Ha</div>
              </div>
              <div>
                <div class="tw-font-bold tw-text-blue-600">{{ (unitPerimeter / 1000).toFixed(2) }}</div>
                <div class="tw-text-gray-500">km</div>
              </div>
              <div>
                <div class="tw-font-bold tw-text-purple-600">{{ unitPointCount }}</div>
                <div class="tw-text-gray-500">Pts</div>
              </div>
            </div>
          </v-card-text>
        </v-card>
      </div>

      <!-- Map Container -->
      <div class="map-area">
        <div ref="mapView" class="map-container"></div>

        <!-- Coordinates Display -->
        <div v-if="cursorCoordinates" class="coordinates-display">
          <v-icon x-small class="tw-mr-1">mdi-crosshairs</v-icon>
          {{ cursorCoordinates.lat.toFixed(6) }}, {{ cursorCoordinates.lng.toFixed(6) }}
        </div>
      </div>
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
// Using global window.require from ArcGIS API (same as ZoneManagement.vue)

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
      currentDrawingPoints: 0,
      drawingPointsRef: [], // Reference for undo functionality

      // UI state
      loading: true,
      loadingMessage: 'Initializing map...',
      showSuccessMessage: false,
      showErrorMessage: false,
      successMessage: '',
      errorMessage: '',
      currentBasemap: 'hybrid',
      showInstructions: true,

      // Search and location
      searchQuery: '',
      searching: false,
      gettingLocation: false,
      cursorCoordinates: null,

      // Symbol configurations
      POLYGON_SYMBOL: {
        // eslint-disable-next-line sonarjs/no-duplicate-string
        type: 'simple-fill',
        color: [51, 153, 51, 0.4],
        outline: {
          color: [51, 153, 51, 0.8],
          width: 2,
        },
      },
      DRAWING_SYMBOL: {
        type: 'simple-fill',
        color: [66, 133, 244, 0.3],
        outline: {
          color: [66, 133, 244, 1],
          width: 2,
          style: 'dash',
        },
      },
    };
  },

  async mounted() {
    this.farmerLocation = {
      lat: this.initialLocation.lat || -1.2921,
      lng: this.initialLocation.lng || 36.8219,
    };

    // Load ArcGIS API first (similar to ZoneManagement.vue)
    await this.loadArcGISScript();
    await this.loadArcGISModules();
    this.$nextTick(() => {
      this.initializeMap();
    });
  },

  beforeDestroy() {
    if (this.view) {
      this.view.destroy();
    }
  },

  methods: {
    async loadArcGISScript() {
      // Load ArcGIS script if not already loaded (same pattern as ZoneManagement.vue)
      if (!window.require) {
        await new Promise((resolve, reject) => {
          // Add CSS
          const cssLink = document.createElement('link');
          cssLink.rel = 'stylesheet';
          cssLink.href = 'https://js.arcgis.com/4.28/esri/themes/light/main.css';
          document.head.appendChild(cssLink);

          // Add script
          const script = document.createElement('script');
          script.src = 'https://js.arcgis.com/4.28/init.js';
          script.onload = resolve;
          script.onerror = reject;
          document.head.appendChild(script);
        });
      }
    },

    async loadArcGISModules() {
      if (this.Map) return; // Already loaded

      try {
        await new Promise((resolve, reject) => {
          window.require([
            'esri/Map',
            'esri/views/MapView',
            'esri/Graphic',
            'esri/layers/GraphicsLayer',
            'esri/geometry/Point',
            'esri/geometry/Polygon',
            'esri/geometry/geometryEngine',
          ], (Map, MapView, Graphic, GraphicsLayer, Point, Polygon, geometryEngine) => {
            this.Map = Map;
            this.MapView = MapView;
            this.Graphic = Graphic;
            this.GraphicsLayer = GraphicsLayer;
            this.Point = Point;
            this.Polygon = Polygon;
            this.geometryEngine = geometryEngine;
            resolve();
          }, reject);
        });
      } catch (error) {
        this.$toast.error('Failed to load ArcGIS modules:', error.message);
        this.showError('Failed to load map resources');
        this.loading = false;
      }
    },

    async loadArcGIS() {
      // Deprecated - keeping for compatibility but using new methods
      await this.loadArcGISScript();
      await this.loadArcGISModules();
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

      // Store reference to component
      const self = this;

      // Simple polygon drawing with click handlers
      this.drawingPointsRef = [];
      let tempGraphic = null;

      // Track cursor position for coordinate display
      this.view.on('pointer-move', (event) => {
        const point = self.view.toMap(event);
        if (point) {
          self.cursorCoordinates = {
            lat: point.latitude,
            lng: point.longitude,
          };
        }
      });

      this.view.on('click', (event) => {
        if (!self.isDrawing) return;

        const point = self.view.toMap(event);
        if (!point) return;

        self.drawingPointsRef.push([point.longitude, point.latitude]);
        self.currentDrawingPoints = self.drawingPointsRef.length;

        // Remove temporary graphic
        if (tempGraphic) {
          self.graphicsLayer.remove(tempGraphic);
        }

        // Draw temporary polygon with dashed outline
        if (self.drawingPointsRef.length >= 2) {
          const rings = [self.drawingPointsRef];
          const polygon = new self.Polygon({
            rings,
            spatialReference: { wkid: 4326 },
          });

          tempGraphic = new self.Graphic({
            geometry: polygon,
            symbol: self.DRAWING_SYMBOL,
          });

          self.graphicsLayer.add(tempGraphic);
        }

        // Add point marker for each vertex
        const pointGraphic = new self.Graphic({
          geometry: new self.Point({
            longitude: point.longitude,
            latitude: point.latitude,
          }),
          symbol: {
            type: 'simple-marker',
            color: [66, 133, 244],
            size: 8,
            outline: {
              color: [255, 255, 255],
              width: 2,
            },
          },
          attributes: { isDrawingPoint: true },
        });
        self.graphicsLayer.add(pointGraphic);
      });

      // Double-click to finish drawing
      this.view.on('double-click', (event) => {
        if (!self.isDrawing || self.drawingPointsRef.length < 3) return;

        event.stopPropagation();

        // Close the polygon
        const rings = [self.drawingPointsRef];
        const polygon = new self.Polygon({
          rings,
          spatialReference: { wkid: 4326 },
        });

        // Remove temporary graphic and point markers
        if (tempGraphic) {
          self.graphicsLayer.remove(tempGraphic);
        }
        // Remove drawing point markers
        const pointMarkers = self.graphicsLayer.graphics.toArray().filter((g) => g.attributes?.isDrawingPoint);
        self.graphicsLayer.removeMany(pointMarkers);

        // Add final polygon with solid outline
        const finalGraphic = new self.Graphic({
          geometry: polygon,
          symbol: self.POLYGON_SYMBOL,
        });

        self.graphicsLayer.add(finalGraphic);

        // Store geometry and calculate area
        self.unitGeometry = polygon;
        self.unitPolygon = polygon;
        self.calculateUnitArea(polygon);

        // Reset drawing state
        self.isDrawing = false;
        self.drawingPointsRef = [];
        self.currentDrawingPoints = 0;
        tempGraphic = null;

        self.showSuccess('Boundary drawn successfully! Click Done to confirm.');
        self.$emit('unit-drawn', {
          geometry: self.unitGeometry,
          area: self.unitArea,
        });
      });
    },

    undoLastPoint() {
      if (this.drawingPointsRef.length === 0) return;

      // Remove last point
      this.drawingPointsRef.pop();
      this.currentDrawingPoints = this.drawingPointsRef.length;

      // Remove the last point marker
      const pointMarkers = this.graphicsLayer.graphics.toArray().filter((g) => g.attributes?.isDrawingPoint);
      if (pointMarkers.length > 0) {
        this.graphicsLayer.remove(pointMarkers[pointMarkers.length - 1]);
      }

      // Redraw the temporary polygon
      const tempGraphics = this.graphicsLayer.graphics.toArray().filter(
        (g) => g.symbol?.outline?.style === 'dash',
      );
      this.graphicsLayer.removeMany(tempGraphics);

      if (this.drawingPointsRef.length >= 2) {
        const rings = [this.drawingPointsRef];
        const polygon = new this.Polygon({
          rings,
          spatialReference: { wkid: 4326 },
        });

        const tempGraphic = new this.Graphic({
          geometry: polygon,
          symbol: this.DRAWING_SYMBOL,
        });

        this.graphicsLayer.add(tempGraphic);
      }

      this.showSuccess('Point removed');
    },

    async searchLocation() {
      if (!this.searchQuery || !this.searchQuery.trim()) return;

      this.searching = true;
      try {
        // Use Nominatim (OpenStreetMap) for geocoding
        const query = encodeURIComponent(this.searchQuery);
        const response = await fetch(
          `https://nominatim.openstreetmap.org/search?format=json&q=${query}&limit=1`,
        );
        const results = await response.json();

        if (results && results.length > 0) {
          const { lat, lon } = results[0];
          this.view.goTo({
            center: [parseFloat(lon), parseFloat(lat)],
            zoom: 16,
          });
          this.showSuccess(`Found: ${results[0].display_name.substring(0, 50)}...`);
        } else {
          this.showError('Location not found. Try a different search term.');
        }
      } catch (error) {
        this.showError('Search failed. Please try again.');
      } finally {
        this.searching = false;
      }
    },

    async goToMyLocation() {
      if (!navigator.geolocation) {
        this.showError('Geolocation is not supported by your browser');
        return;
      }

      this.gettingLocation = true;
      try {
        const position = await new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject, {
            enableHighAccuracy: true,
            timeout: 10000,
          });
        });

        const { latitude, longitude } = position.coords;
        this.view.goTo({
          center: [longitude, latitude],
          zoom: 17,
        });
        this.showSuccess('Moved to your current location');
      } catch (error) {
        this.showError('Could not get your location. Please enable location services.');
      } finally {
        this.gettingLocation = false;
      }
    },

    zoomIn() {
      if (this.view) {
        this.view.zoom += 1;
      }
    },

    zoomOut() {
      if (this.view) {
        this.view.zoom -= 1;
      }
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
      this.drawingPointsRef = [];
      this.currentDrawingPoints = 0;
      this.showSuccess('Drawing cleared');
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

    changeBasemap(basemapName) {
      if (this.map) {
        this.map.basemap = basemapName;
        this.currentBasemap = basemapName;
      }
    },
  },
};
</script>

<style scoped>
/* Main container - flexbox column layout */
.production-unit-drawer {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background-color: #f5f5f5;
}

/* Top toolbar bar */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background-color: white;
  border-bottom: 1px solid #e0e0e0;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  flex-shrink: 0;
  z-index: 10;
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-bar-center {
  display: flex;
  align-items: center;
  gap: 4px;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* Main content area - flexbox row */
.main-content {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* Left sidebar for instructions */
.left-sidebar {
  width: 220px;
  padding: 12px;
  background-color: #fafafa;
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
  flex-shrink: 0;
}

/* Map area */
.map-area {
  flex: 1;
  position: relative;
  min-width: 0;
}

/* Map container fills the map area */
.map-container {
  width: 100%;
  height: 100%;
}

/* Coordinates display at bottom right of map */
.coordinates-display {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: rgba(255,255,255,0.9);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-family: monospace;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

/* Transition animations */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.3s ease;
}
.slide-fade-enter,
.slide-fade-leave-to {
  transform: translateX(-10px);
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}
.slide-up-enter,
.slide-up-leave-to {
  transform: translateY(10px);
  opacity: 0;
}

/* Ensure proper z-indexing for floating elements */
.tw-z-10 {
  z-index: 10;
}

.tw-z-20 {
  z-index: 20;
}

/* Pulsing animation for drawing indicator */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.tw-animate-pulse {
  animation: pulse 1.5s ease-in-out infinite;
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
