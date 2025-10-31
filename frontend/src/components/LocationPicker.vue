<!-- eslint-disable  -->
<template>
  <v-dialog v-model="dialog" max-width="800px" persistent>
    <v-card class="rounded-xl elevation-12">
      <v-card-title class="green white--text rounded-t-xl">
        <v-icon left>mdi-map-marker</v-icon>
        Select Location
        <v-spacer></v-spacer>
        <v-btn icon @click="close" color="white" class="rounded-circle">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>

      <v-card-text class="pa-0">
        <div class="map-container">
          <div ref="mapContainer" class="map-view rounded-b-xl"></div>

          <!-- Location Info -->
          <div class="location-info">
            <v-chip v-if="selectedLocation" color="green" text-color="white" class="rounded-pill">
              <v-icon left small>mdi-crosshairs-gps</v-icon>
              {{ selectedLocation.lat.toFixed(6) }}, {{ selectedLocation.lng.toFixed(6) }}
            </v-chip>
          </div>

          <!-- Controls -->
          <div class="map-controls">
            <v-btn
              @click="useCurrentLocation"
              color="blue"
              class="rounded-pill elevation-3"
              :loading="gettingLocation"
              prepend-icon="mdi-crosshairs-gps"
            >
              Use Current Location
            </v-btn>

            <v-btn
              @click="confirmLocation"
              color="green"
              class="rounded-pill elevation-3"
              :disabled="!selectedLocation"
              prepend-icon="mdi-check"
            >
              Confirm Location
            </v-btn>
          </div>
        </div>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
/* eslint-disable */
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

export default {
  name: 'LocationPicker',
  props: {
    value: {
      type: Boolean,
      default: false,
    },
    initialLocation: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      dialog: false,
      map: null,
      selectedLocation: null,
      marker: null,
      gettingLocation: false,
      loading: true,
    };
  },
  watch: {
    value(val) {
      this.dialog = val;
      if (val && !this.map) {
        this.$nextTick(() => {
          this.initializeMap();
        });
      }
    },
    dialog(val) {
      this.$emit('input', val);
    },
  },
  beforeDestroy() {
    if (this.map) {
      this.map.remove();
    }
  },
  methods: {
    initializeMap() {
      try {
        this.loading = true;

        // Fix for default markers in Leaflet
        delete L.Icon.Default.prototype._getIconUrl;
        L.Icon.Default.mergeOptions({
          iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
          iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
          shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
        });

        // Determine initial center
        let center = [-1.2921, 36.8219]; // Default to Nairobi, Kenya [lat, lng]
        if (this.initialLocation) {
          center = [this.initialLocation.lat, this.initialLocation.lng];
        }

        // Create map
        this.map = L.map(this.$refs.mapContainer, {
          center: center,
          zoom: 12,
          zoomControl: true,
        });

        // Add OpenStreetMap tiles
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '© OpenStreetMap contributors',
          maxZoom: 19,
        }).addTo(this.map);

        // Set up click handler for location selection
        this.map.on('click', (event) => {
          this.selectLocation(event.latlng.lng, event.latlng.lat);
        });

        // Set initial location if provided
        if (this.initialLocation) {
          this.selectLocation(this.initialLocation.lng, this.initialLocation.lat);
        }

        this.loading = false;
      } catch (error) {
        this.$toast.error('Failed to load map. Please try again.');
        this.close();
      }
    },

    selectLocation(lng, lat) {
      this.selectedLocation = { lat, lng };

      // Remove existing marker
      if (this.marker) {
        this.map.removeLayer(this.marker);
      }

      // Create new marker
      this.marker = L.marker([lat, lng], {
        draggable: true,
      }).addTo(this.map);

      // Handle marker drag
      this.marker.on('dragend', (event) => {
        const position = event.target.getLatLng();
        this.selectedLocation = { lat: position.lat, lng: position.lng };
      });
    },

    async useCurrentLocation() {
      this.gettingLocation = true;
      try {
        const position = await this.getCurrentPosition();
        this.selectLocation(position.coords.longitude, position.coords.latitude);
        this.map.setView([position.coords.latitude, position.coords.longitude], 15);
      } catch (error) {
        this.$toast.error('Unable to get your current location. Please select location manually on the map.');
      } finally {
        this.gettingLocation = false;
      }
    },

    getCurrentPosition() {
      return new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
          reject(new Error('Geolocation is not supported'));
          return;
        }

        navigator.geolocation.getCurrentPosition(resolve, reject, {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 60000,
        });
      });
    },

    confirmLocation() {
      if (this.selectedLocation) {
        this.$emit('location-selected', this.selectedLocation);
        this.close();
      }
    },

    close() {
      this.dialog = false;
      this.selectedLocation = null;
      if (this.marker) {
        this.map.removeLayer(this.marker);
        this.marker = null;
      }
    },
  },
};
</script>

<style scoped>
.map-container {
  position: relative;
  height: 500px;
}

.map-view {
  width: 100%;
  height: 100%;
  border-radius: 0 0 12px 12px;
}

.location-info {
  position: absolute;
  top: 15px;
  left: 15px;
  z-index: 1000;
}

.map-controls {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 15px;
  z-index: 1000;
}

/* Leaflet controls styling */
.leaflet-control-container .leaflet-routing-container-hide {
  display: none;
}

.leaflet-control-container .leaflet-bar {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.leaflet-control-zoom-in,
.leaflet-control-zoom-out {
  border-radius: 8px 8px 0 0;
  margin-bottom: 1px;
}

.leaflet-control-zoom-out {
  border-radius: 0 0 8px 8px;
}

/* Custom marker styling */
.custom-marker {
  background-color: #ff4444;
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

/* Loading overlay */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1001;
  border-radius: 12px;
}
</style>
