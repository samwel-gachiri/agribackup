<template>
  <div class="arcgis-location-picker">
    <v-card>
      <v-card-title class="tw-text-sm tw-py-2">
        <v-icon class="tw-mr-2">mdi-map-marker</v-icon>
        Select Location
      </v-card-title>

      <v-card-text class="tw-p-0">
        <!-- Map Container -->
        <div :id="mapId" class="tw-h-80 tw-w-full"></div>

        <!-- Search Box -->
        <div class="tw-absolute tw-top-16 tw-left-4 tw-right-4 tw-z-10">
          <v-text-field
            v-model="searchQuery"
            placeholder="Search for a location..."
            density="compact"
            variant="outlined"
            bg-color="white"
            append-inner-icon="mdi-magnify"
            @click:append-inner="searchLocation"
            @keyup.enter="searchLocation"
            class="tw-shadow-md"
          />
        </div>

        <!-- Coordinates Display -->
        <div v-if="selectedLocation" class="tw-absolute tw-bottom-4 tw-left-4 tw-z-10 tw-bg-white tw-rounded tw-p-2 tw-shadow-md">
          <div class="tw-text-xs tw-text-gray-600">
            <div><strong>Lat:</strong> {{ selectedLocation.latitude.toFixed(6) }}</div>
            <div><strong>Lng:</strong> {{ selectedLocation.longitude.toFixed(6) }}</div>
          </div>
        </div>

        <!-- Accuracy Indicator -->
        <div class="tw-absolute tw-top-16 tw-right-4 tw-z-10">
          <v-btn
            size="small"
            icon
            @click="getCurrentLocation"
            :loading="gettingLocation"
            color="primary"
            class="tw-shadow-md"
          >
            <v-icon>mdi-crosshairs-gps</v-icon>
          </v-btn>
        </div>
      </v-card-text>

      <v-card-actions class="tw-px-4 tw-py-2">
        <v-spacer />
        <v-btn size="small" @click="$emit('cancel')">Cancel</v-btn>
        <v-btn
          size="small"
          color="primary"
          @click="confirmLocation"
          :disabled="!selectedLocation"
        >
          Confirm Location
        </v-btn>
      </v-card-actions>
    </v-card>

    <!-- Loading Overlay -->
    <v-overlay v-model="loading" class="tw-flex tw-items-center tw-justify-center">
      <v-progress-circular indeterminate size="64" />
    </v-overlay>
  </div>
</template>

<script>
export default {
  name: 'ArcGISLocationPicker',
  props: {
    initialLocation: {
      type: Object,
      default: null,
    },
    allowSearch: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      mapId: `arcgis-location-picker-${Math.random().toString(36).substr(2, 9)}`,
      mapView: null,
      selectedLocation: null,
      searchQuery: '',
      loading: false,
      gettingLocation: false,
      // ArcGIS modules
      Map: null,
      MapView: null,
      Graphic: null,
      Point: null,
      SimpleMarkerSymbol: null,
      Locator: null,
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
          'esri/geometry/Point',
          'esri/symbols/SimpleMarkerSymbol',
          'esri/tasks/Locator',
        ], (Map, MapView, Graphic, Point, SimpleMarkerSymbol, Locator) => {
          this.Map = Map;
          this.MapView = MapView;
          this.Graphic = Graphic;
          this.Point = Point;
          this.SimpleMarkerSymbol = SimpleMarkerSymbol;
          this.Locator = Locator;
          resolve();
        });
      }));
    },

    initializeMap() {
      if (this.mapView || !document.getElementById(this.mapId)) return;

      const map = new this.Map({
        basemap: 'streets-navigation-vector',
      });

      const center = this.initialLocation
        ? [this.initialLocation.longitude, this.initialLocation.latitude]
        : [36.8219, -1.2921]; // Nairobi, Kenya

      this.mapView = new this.MapView({
        container: this.mapId,
        map,
        center,
        zoom: this.initialLocation ? 15 : 10,
        ui: {
          components: ['attribution'],
        },
      });

      // Map click handler
      this.mapView.on('click', (event) => {
        this.setLocation(event.mapPoint.longitude, event.mapPoint.latitude);
      });

      // Set initial location if provided
      if (this.initialLocation) {
        this.setLocation(this.initialLocation.longitude, this.initialLocation.latitude);
      }
    },

    setLocation(longitude, latitude) {
      this.selectedLocation = { longitude, latitude };
      this.updateLocationMarker();
      this.$emit('location-changed', this.selectedLocation);
    },

    updateLocationMarker() {
      if (!this.selectedLocation || !this.mapView) return;

      // Clear existing graphics
      this.mapView.graphics.removeAll();

      const point = new this.Point({
        longitude: this.selectedLocation.longitude,
        latitude: this.selectedLocation.latitude,
      });

      const marker = new this.Graphic({
        geometry: point,
        symbol: new this.SimpleMarkerSymbol({
          color: 'red',
          size: 12,
          outline: {
            color: 'white',
            width: 2,
          },
        }),
      });

      this.mapView.graphics.add(marker);

      // Center map on location
      this.mapView.goTo({
        center: point,
        zoom: 15,
      });
    },

    async searchLocation() {
      if (!this.searchQuery.trim()) return;

      this.loading = true;

      try {
        const locator = new this.Locator({
          url: 'https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer',
        });

        const results = await locator.addressToLocations({
          address: {
            SingleLine: this.searchQuery,
          },
          maxLocations: 1,
          outFields: ['*'],
        });

        if (results.length > 0) {
          const result = results[0];
          this.setLocation(result.location.longitude, result.location.latitude);
          // eslint-disable-next-line sonarjs/no-duplicate-string
          this.$emit('show-message', {
            type: 'success',
            text: `Found: ${result.attributes.LongLabel || this.searchQuery}`,
          });
        } else {
          this.$emit('show-message', {
            type: 'warning',
            text: 'Location not found. Please try a different search term.',
          });
        }
      } catch (error) {
        this.$emit('show-message', {
          type: 'error',
          text: 'Failed to search location. Please try again.',
        });
      } finally {
        this.loading = false;
      }
    },

    getCurrentLocation() {
      if (!navigator.geolocation) {
        this.$emit('show-message', {
          type: 'error',
          text: 'Geolocation is not supported by this browser.',
        });
        return;
      }

      this.gettingLocation = true;

      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.setLocation(position.coords.longitude, position.coords.latitude);
          this.$emit('show-message', {
            type: 'success',
            text: 'Current location detected successfully.',
          });
          this.gettingLocation = false;
        },
        (error) => {
          let message = 'Failed to get current location.';

          // eslint-disable-next-line default-case
          switch (error.code) {
            case error.PERMISSION_DENIED:
              message = 'Location access denied. Please enable location services.';
              break;
            case error.POSITION_UNAVAILABLE:
              message = 'Location information unavailable.';
              break;
            case error.TIMEOUT:
              message = 'Location request timed out.';
              break;
          }

          this.$emit('show-message', { type: 'error', text: message });
          this.gettingLocation = false;
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000,
        },
      );
    },

    confirmLocation() {
      if (this.selectedLocation) {
        this.$emit('location-selected', {
          latitude: this.selectedLocation.latitude,
          longitude: this.selectedLocation.longitude,
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
.arcgis-location-picker {
  position: relative;
}

/* Custom ArcGIS styling */
:deep(.esri-view .esri-view-surface--inset-outline:focus::after) {
  outline: none !important;
}

:deep(.esri-attribution) {
  font-size: 10px !important;
}
</style>
