<template>
  <div class="tw-h-screen tw-flex tw-flex-col">
    <!-- Header -->
<!--    <v-app-bar app color="green darken-2" dark class="tw-shadow-lg" height="64">-->
    <div class="tw-w-full tw-flex tw-flex-row tw-bg-green-600">
      <v-toolbar-title class="tw-font-bold">
        <v-icon left>mdi-map-marker</v-icon>
        Farm Boundary
      </v-toolbar-title>
      <v-spacer></v-spacer>
      <v-chip v-if="userLocation" color="white" text-color="green darken-2" class="tw-mr-2">
        <v-icon left small>mdi-crosshairs-gps</v-icon>
        Location: {{ userLocation.lat.toFixed(4) }}, {{ userLocation.lng.toFixed(4) }}
      </v-chip>
    </div>
    <!-- Main Content -->
    <div class="tw-flex tw-flex-1 tw-overflow-hidden" style="padding-top: 64px;">
      <!-- Map Container -->
      <div class="tw-flex-1 tw-relative">
        <!-- Drawing Controls -->
        <div class="tw-absolute tw-top-4 tw-right-4 tw-z-10">
          <v-card class="tw-shadow-xl">
            <v-card-title class="tw-py-2 tw-px-3">
              <v-icon left color="green">mdi-draw</v-icon>
              <span class="tw-text-sm tw-font-medium">Drawing Tools</span>
            </v-card-title>
            <v-card-text class="tw-py-2 tw-px-3">
              <div class="tw-space-y-2">
                <v-btn
                    @click="startDrawing"
                    :disabled="isDrawing || loading"
                    color="green"
                    outlined
                    small
                    block
                >
                  <v-icon left small>mdi-pencil</v-icon>
                  {{ isDrawing ? 'Drawing...' : 'Draw Farm Boundary' }}
                </v-btn>

                <v-btn
                    @click="clearDrawing"
                    :disabled="!farmPolygon || loading"
                    color="red"
                    outlined
                    small
                    block
                >
                  <v-icon left small>mdi-delete</v-icon>
                  Clear Drawing
                </v-btn>

                <v-btn
                    @click="saveFarmBoundary"
                    :disabled="!farmPolygon || loading"
                    color="blue"
                    outlined
                    small
                    block
                >
                  <v-icon left small>mdi-content-save</v-icon>
                  Save Farm
                </v-btn>
              </div>
            </v-card-text>
          </v-card>
        </div>

        <!-- Farm Info Panel -->
        <div v-if="farmPolygon" class="tw-absolute tw-bottom-4 tw-left-4 tw-z-10">
          <v-card class="tw-shadow-xl tw-max-w-xs">
            <v-card-title class="tw-py-2 tw-px-3 green white--text">
              <v-icon left>mdi-information</v-icon>
              <span class="tw-text-sm tw-font-medium">Farm Details</span>
            </v-card-title>
            <v-card-text class="tw-py-2 tw-px-3">
              <div class="tw-space-y-1 tw-text-sm">
                <div><strong>Area:</strong> {{ farmArea }} hectares</div>
                <div><strong>Perimeter:</strong> {{ farmPerimeter }} meters</div>
                <div><strong>Coordinates:</strong> {{ farmPolygon.rings[0].length - 1 }} points</div>
              </div>
            </v-card-text>
          </v-card>
        </div>
        <div ref="mapView" class="tw-w-full tw-h-full"></div>

        <!-- Loading Overlay -->
        <v-overlay :value="loading" absolute>
          <v-progress-circular
              indeterminate
              size="64"
              color="green"
          ></v-progress-circular>
          <div class="tw-mt-4 tw-text-white tw-font-medium">{{ loadingMessage }}</div>
        </v-overlay>
      </div>
    </div>

    <!-- Success/Error Snackbars -->
    <v-snackbar
        v-model="showSuccessMessage"
        color="success"
        timeout="3000"
        bottom
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
import { loadModules, loadScript } from 'esri-loader';

export default {
  name: 'FarmMap',
  data() {
    return {
      // Map and ArcGIS objects
      map: null,
      view: null,
      sketchViewModel: null,
      draw: null,
      graphicsLayer: null,
      farmGeometry: null,
      farmArea: 0,
      currentDrawingAction: null,

      // User location
      userLocation: null,

      // Drawing state
      isDrawing: false,
      farmPolygon: null,
      farmPerimeter: 0,

      // UI state
      loading: true,
      loadingMessage: 'Initializing map...',
      showSuccessMessage: false,
      showErrorMessage: false,
      successMessage: '',
      errorMessage: '',
    };
  },

  async mounted() {
    await this.initializeMap();
  },

  beforeDestroy() {
    if (this.view) {
      this.view.destroy();
    }
  },

  methods: {
    async initializeMap() {
      try {
        this.loadingMessage = 'Loading ArcGIS API...';

        // Load ArcGIS API with specific version
        await loadScript({
          url: 'https://js.arcgis.com/4.28/',
          css: 'https://js.arcgis.com/4.28/esri/themes/light/main.css',
        });

        this.loadingMessage = 'Loading map modules...';

        // Load required ArcGIS modules with retry logic
        const [
          Map,
          MapView,
          SketchViewModel,
          GraphicsLayer,
          Graphic,
          geometryEngine,
          // eslint-disable-next-line no-unused-vars
          Point,
          Polygon,
        ] = await this.loadModulesWithRetry([
          'esri/Map',
          'esri/views/MapView',
          'esri/widgets/Sketch/SketchViewModel',
          'esri/layers/GraphicsLayer',
          'esri/Graphic',
          'esri/geometry/geometryEngine',
          'esri/geometry/Point',
          'esri/geometry/Polygon',
        ]);

        // Store modules for later use
        this.geometryEngine = geometryEngine;
        this.Graphic = Graphic;
        this.Polygon = Polygon;

        this.loadingMessage = 'Getting your location...';

        // Get user's current location
        await this.getUserLocation();

        this.loadingMessage = 'Setting up map...';

        // Create graphics layer for farm boundaries
        this.graphicsLayer = new GraphicsLayer({
          title: 'Farm Boundaries',
        });

        // Create map
        this.map = new Map({
          basemap: 'satellite',
          layers: [this.graphicsLayer],
        });

        // Create map view
        this.view = new MapView({
          container: this.$refs.mapView,
          map: this.map,
          center: [this.userLocation.lng, this.userLocation.lat],
          zoom: 16,
        });

        // Wait for view to load
        await this.view.when();

        this.sketchViewModel = new SketchViewModel({
          view: this.view,
          layer: this.graphicsLayer,
          polygonSymbol: {
            type: 'simple-fill',
            color: [51, 153, 51, 0.4], // Green with transparency
            outline: {
              color: [51, 153, 51, 0.8],
              width: 2,
            },
          },
          defaultUpdateOptions: {
            tool: 'reshape',
            toggleToolOnClick: false,
          },
        });

        // Set up sketch event listeners
        this.setupSketchEvents(geometryEngine);

        // Add user location marker
        this.addUserLocationMarker(Graphic);

        this.loading = false;
        this.showSuccess('Map initialized successfully!');
      } catch (error) {
        this.showError('Failed to initialize map. Please refresh and try again.');
        this.loading = false;
      }
    },

    // eslint-disable-next-line consistent-return
    async loadModulesWithRetry(modules, retries = 3) {
      // eslint-disable-next-line no-plusplus
      for (let i = 0; i < retries; i++) {
        try {
          // eslint-disable-next-line no-await-in-loop
          return await loadModules(modules);
        } catch (error) {
          this.showErrorMessage(`Attempt ${i + 1} failed:`);
          if (i === retries - 1) throw error;
          // Wait before retrying
          // eslint-disable-next-line no-await-in-loop
          await new Promise((resolve) => setTimeout(resolve, 1000));
        }
      }
    },

    async getUserLocation() {
      return new Promise((resolve) => {
        if (!navigator.geolocation) {
          // Default to Nairobi, Kenya if geolocation is not available
          this.userLocation = { lat: -1.2921, lng: 36.8219 };
          resolve();
          return;
        }

        navigator.geolocation.getCurrentPosition(
          (position) => {
            this.userLocation = {
              lat: position.coords.latitude,
              lng: position.coords.longitude,
            };
            resolve();
          },
          () => {
            this.showErrorMessage('Geolocation error:');
            // Default to Nairobi, Kenya if geolocation fails
            this.userLocation = { lat: -1.2921, lng: 36.8219 };
            resolve();
          },
          {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 60000,
          },
        );
      });
    },

    addUserLocationMarker(Graphic) {
      const userLocationGraphic = new Graphic({
        geometry: {
          type: 'point',
          longitude: this.userLocation.lng,
          latitude: this.userLocation.lat,
        },
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
          title: 'Your Location',
        },
        popupTemplate: {
          title: 'Your Current Location',
          content: `Latitude: ${this.userLocation.lat.toFixed(6)}<br>Longitude: ${this.userLocation.lng.toFixed(6)}`,
        },
      });

      this.graphicsLayer.add(userLocationGraphic);
    },

    setupSketchEvents(geometryEngine) {
      this.sketchViewModel.on('create', async (event) => {
        if (event.state === 'start') {
          this.isDrawing = true;
          this.showSuccess('create start');
        } else if (event.state === 'complete') {
          this.showSuccess('create complete');
          this.farmGeometry = event.graphic.geometry;
          await this.calculateFarmArea(geometryEngine);
          this.isDrawing = false;
          this.$emit('farm-boundary-created', {
            geometry: this.farmGeometry,
            area: this.farmArea,
            location: this.userLocation,
          });
        } else if (event.state === 'cancel') {
          this.isDrawing = false;
          this.showSuccess('create cancel');
        }
      });
      this.sketchViewModel.on('update', async (event) => {
        // eslint-disable-next-line no-empty
        if (event.state === 'complete') {
          this.showSuccess('update complete');
          this.farmGeometry = event.graphics[0].geometry;
          await this.calculateFarmArea(geometryEngine);
          this.$emit('farm-boundary-updated', {
            geometry: this.farmGeometry,
            area: this.farmArea,
            location: this.userLocation,
          });
        }
      });

      this.sketchViewModel.on('delete', () => {
        this.showSuccess('delete');
      });
    },
    startDrawing() {
      if (this.sketchViewModel) {
        this.sketchViewModel.create('polygon');
        this.showSuccess('started sketchview draw');
        this.isDrawing = true;
      }
    },
    async calculateFarmArea(geometryEngine) {
      if (this.farmGeometry) {
        try {
          // Calculate area in square meters
          const areaInSquareMeters = geometryEngine.planarArea(this.farmGeometry, 'square-meters');
          // Convert to acres (1 acre = 4047 square meters)
          this.farmArea = Math.abs(areaInSquareMeters) / 4047;
        } catch (error) {
          this.showErrorMessage('Error calculating area:');
          this.farmArea = 0;
        }
      }
    },

    clearDrawing() {
      // Cancel current drawing action
      if (this.currentDrawingAction) {
        try {
          this.currentDrawingAction.destroy();
        } catch (e) {
          this.showErrorMessage('Error destroying draw action:');
        }
        this.currentDrawingAction = null;
      }

      if (this.graphicsLayer) {
        // Remove all graphics except user location (keep first graphic)
        const graphics = this.graphicsLayer.graphics.items;
        if (graphics.length > 1) {
          this.graphicsLayer.removeMany(graphics.slice(1));
        }
      }

      this.farmPolygon = null;
      this.farmArea = 0;
      this.farmPerimeter = 0;
      this.isDrawing = false;
    },

    saveFarmBoundary() {
      if (!this.farmPolygon) {
        this.showError('No farm boundary to save.');
        return;
      }

      try {
        // Create farm data object
        const farmData = {
          id: Date.now(), // Simple ID generation
          geometry: {
            type: 'polygon',
            coordinates: this.farmPolygon.rings,
          },
          properties: {
            area: this.farmArea,
            perimeter: this.farmPerimeter,
            createdAt: new Date().toISOString(),
            userLocation: this.userLocation,
          },
        };

        // Store in localStorage (in a real app, this would be sent to a server)
        const savedFarms = JSON.parse(localStorage.getItem('savedFarms') || '[]');
        savedFarms.push(farmData);
        localStorage.setItem('savedFarms', JSON.stringify(savedFarms));

        // Emit event for parent component
        this.$emit('farm-saved', farmData);

        this.showSuccess(`Farm boundary saved! Area: ${this.farmArea} hectares`);
      } catch (error) {
        this.showErrorMessage('Error saving farm boundary:');
        this.showError('Failed to save farm boundary. Please try again.');
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
  },
};
</script>

<style scoped>
/* Custom styles for map container */
.esri-view {
  height: 100%;
  width: 100%;
}

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
