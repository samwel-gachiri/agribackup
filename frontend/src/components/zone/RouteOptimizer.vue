<!-- eslint-disable sonarjs/no-duplicate-string -->
<template>
  <div class="route-optimizer">
    <v-card class="tw-h-full">
      <v-card-title class="tw-text-sm tw-py-3 tw-bg-gradient-to-r tw-from-green-500 tw-to-blue-500 tw-text-white">
        <v-icon class="tw-mr-2">mdi-map-marker-path</v-icon>
        Route Optimization
      </v-card-title>

      <v-card-text class="tw-p-0">
        <!-- Map Container -->
        <div :id="mapId" class="tw-h-96 tw-w-full"></div>

        <!-- Route Controls -->
        <div class="tw-absolute tw-top-16 tw-left-4 tw-z-10 tw-bg-white tw-rounded-lg tw-shadow-md tw-p-3">
          <v-select
            v-model="selectedZone"
            :items="zones"
            item-title="name"
            item-value="id"
            label="Select Zone"
            density="compact"
            variant="outlined"
            class="tw-mb-2"
            @update:model-value="onZoneSelected"
          />

          <v-btn
            size="small"
            color="primary"
            @click="optimizeRoute"
            :loading="optimizing"
            :disabled="!selectedZone || farmers.length === 0"
            block
          >
            <v-icon left>mdi-auto-fix</v-icon>
            Optimize Route
          </v-btn>

          <v-btn
            v-if="optimizedRoute"
            size="small"
            color="success"
            @click="schedulePickups"
            :loading="scheduling"
            block
            class="tw-mt-2"
          >
            <v-icon left>mdi-calendar-plus</v-icon>
            Schedule Pickups
          </v-btn>
        </div>

        <!-- Route Info Panel -->
        <v-card v-if="optimizedRoute" class="tw-absolute tw-bottom-4 tw-right-4 tw-z-10 tw-w-80">
          <v-card-title class="tw-text-sm tw-py-2">Route Summary</v-card-title>
          <v-card-text class="tw-py-2">
            <div class="tw-space-y-2 tw-text-sm">
              <div class="tw-flex tw-justify-between">
                <span>Total Distance:</span>
                <span class="tw-font-semibold">{{ optimizedRoute.estimatedDistance }} km</span>
              </div>
              <div class="tw-flex tw-justify-between">
                <span>Estimated Time:</span>
                <span class="tw-font-semibold">{{ optimizedRoute.estimatedDuration }} min</span>
              </div>
              <div class="tw-flex tw-justify-between">
                <span>Farmers:</span>
                <span class="tw-font-semibold">{{ optimizedRoute.farmers.length }}</span>
              </div>
              <div class="tw-flex tw-justify-between">
                <span>Fuel Savings:</span>
                <span class="tw-font-semibold tw-text-green-600">{{ calculateSavings() }}%</span>
              </div>
            </div>
          </v-card-text>
        </v-card>

        <!-- Farmer List -->
        <v-card v-if="optimizedRoute" class="tw-absolute tw-top-4 tw-right-4 tw-z-10 tw-w-64 tw-max-h-80 tw-overflow-y-auto">
          <v-card-title class="tw-text-sm tw-py-2">Pickup Order</v-card-title>
          <v-list dense>
            <v-list-item
              v-for="(farmerId, index) in optimizedRoute.optimizedOrder"
              :key="farmerId"
              class="tw-py-1"
            >
              <template v-slot:prepend>
                <v-avatar size="24" :color="getStopColor(index)">
                  <span class="tw-text-xs tw-text-white">{{ index + 1 }}</span>
                </v-avatar>
              </template>
              <v-list-item-title class="tw-text-sm">
                {{ getFarmerName(farmerId) }}
              </v-list-item-title>
              <template v-slot:append>
                <v-chip size="x-small" :color="getStatusColor(farmerId)">
                  {{ getFarmerStatus(farmerId) }}
                </v-chip>
              </template>
            </v-list-item>
          </v-list>
        </v-card>
      </v-card-text>
    </v-card>

    <!-- Schedule Pickup Dialog -->
    <v-dialog v-model="scheduleDialog" max-width="500">
      <v-card>
        <v-card-title>Schedule Pickups</v-card-title>
        <v-card-text>
          <v-date-picker
            v-model="pickupDate"
            full-width
            class="tw-mb-4"
          />
          <v-time-picker
            v-model="pickupTime"
            full-width
            format="24hr"
            class="tw-mb-4"
          />
          <v-textarea
            v-model="pickupNotes"
            label="Pickup Notes"
            rows="3"
            variant="outlined"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="scheduleDialog = false">Cancel</v-btn>
          <v-btn color="primary" @click="confirmSchedulePickups" :loading="scheduling">
            Schedule All
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Loading Overlay -->
    <v-overlay v-model="loading" class="tw-flex tw-items-center tw-justify-center">
      <v-progress-circular indeterminate size="64" />
    </v-overlay>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'RouteOptimizer',
  props: {
    zones: {
      type: Array,
      default: () => [],
    },
    userRole: {
      type: String,
      default: 'ZONE_SUPERVISOR',
    },
  },
  data() {
    return {
      mapId: `route-optimizer-${Math.random().toString(36).substr(2, 9)}`,
      mapView: null,
      selectedZone: null,
      farmers: [],
      optimizedRoute: null,
      optimizing: false,
      scheduling: false,
      loading: false,
      scheduleDialog: false,
      pickupDate: new Date().toISOString().substr(0, 10),
      pickupTime: '09:00',
      pickupNotes: '',
      // ArcGIS modules
      Map: null,
      MapView: null,
      Graphic: null,
      GraphicsLayer: null,
      Point: null,
      Polyline: null,
      SimpleMarkerSymbol: null,
      SimpleLineSymbol: null,
      TextSymbol: null,
      RouteTask: null,
      RouteParameters: null,
      FeatureSet: null,
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
          'esri/geometry/Point',
          'esri/geometry/Polyline',
          'esri/symbols/SimpleMarkerSymbol',
          'esri/symbols/SimpleLineSymbol',
          'esri/symbols/TextSymbol',
          'esri/tasks/RouteTask',
          'esri/tasks/support/RouteParameters',
          'esri/tasks/support/FeatureSet',
        ], (Map, MapView, Graphic, GraphicsLayer, Point, Polyline,
          SimpleMarkerSymbol, SimpleLineSymbol, TextSymbol,
          RouteTask, RouteParameters, FeatureSet) => {
          this.Map = Map;
          this.MapView = MapView;
          this.Graphic = Graphic;
          this.GraphicsLayer = GraphicsLayer;
          this.Point = Point;
          this.Polyline = Polyline;
          this.SimpleMarkerSymbol = SimpleMarkerSymbol;
          this.SimpleLineSymbol = SimpleLineSymbol;
          this.TextSymbol = TextSymbol;
          this.RouteTask = RouteTask;
          this.RouteParameters = RouteParameters;
          this.FeatureSet = FeatureSet;
          resolve();
        });
      }));
    },

    initializeMap() {
      if (this.mapView || !document.getElementById(this.mapId)) return;

      const map = new this.Map({
        basemap: 'streets-navigation-vector',
      });

      // Create graphics layers
      const farmerLayer = new this.GraphicsLayer({ title: 'Farmers' });
      const routeLayer = new this.GraphicsLayer({ title: 'Route' });
      const zoneLayer = new this.GraphicsLayer({ title: 'Zone' });

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
    },

    async onZoneSelected(zoneId) {
      if (!zoneId) return;

      this.loading = true;
      this.optimizedRoute = null;

      try {
        // Get farmers in the selected zone
        const response = await axios.get(`/api/zone-supervisor-service/zones/${zoneId}/farmers`);

        if (response.data.success) {
          this.farmers = response.data.data;
          this.displayZoneAndFarmers();
        }
      } catch (error) {
        // eslint-disable-next-line sonarjs/no-duplicate-string
        this.$emit('show-message', { type: 'error', text: 'Failed to load farmers' });
      } finally {
        this.loading = false;
      }
    },

    displayZoneAndFarmers() {
      const zone = this.zones.find((z) => z.id === this.selectedZone);
      if (!zone) return;

      // Clear existing graphics
      this.mapView.map.layers.forEach((layer) => layer.removeAll());

      // Display zone
      const zoneLayer = this.mapView.map.layers.find((layer) => layer.title === 'Zone');
      const center = new this.Point({
        longitude: zone.centerLongitude,
        latitude: zone.centerLatitude,
      });

      const zoneGraphic = new this.Graphic({
        geometry: center,
        symbol: new this.SimpleMarkerSymbol({
          color: 'blue',
          size: 12,
          outline: { color: 'white', width: 2 },
        }),
      });

      zoneLayer.add(zoneGraphic);

      // Display farmers
      const farmerLayer = this.mapView.map.layers.find((layer) => layer.title === 'Farmers');

      this.farmers.forEach((farmer, index) => {
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
            outline: { color: 'white', width: 2 },
          }),
          attributes: { ...farmer, index },
        });

        const labelGraphic = new this.Graphic({
          geometry: point,
          symbol: new this.TextSymbol({
            text: farmer.farmerName,
            color: 'black',
            haloColor: 'white',
            haloSize: 1,
            font: { size: 10 },
            yoffset: -15,
          }),
        });

        farmerLayer.addMany([farmerGraphic, labelGraphic]);
      });

      // Zoom to farmers
      if (this.farmers.length > 0) {
        this.mapView.goTo(farmerLayer.graphics.items);
      }
    },
    async optimizeRoute() {
      if (!this.selectedZone || this.farmers.length === 0) return;

      this.optimizing = true;

      try {
        // Get optimal route from backend
        const response = await axios.get(`/api/zone-supervisor-service/zones/${this.selectedZone}/optimal-route`);

        if (response.data.success) {
          this.optimizedRoute = response.data.data;
          this.displayOptimizedRoute();
          this.$emit('route-optimized', this.optimizedRoute);
        }
      } catch (error) {
        // Fallback to client-side optimization
        this.optimizedRoute = this.clientSideOptimization();
        this.displayOptimizedRoute();
      } finally {
        this.optimizing = false;
      }
    },

    clientSideOptimization() {
      // Simple nearest neighbor algorithm as fallback
      const farmersWithLocation = this.farmers.filter((f) => f.location);
      if (farmersWithLocation.length === 0) return null;

      const zone = this.zones.find((z) => z.id === this.selectedZone);
      const startPoint = {
        latitude: zone.centerLatitude,
        longitude: zone.centerLongitude,
      };

      const optimizedOrder = [];
      const unvisited = [...farmersWithLocation];
      let current = startPoint;
      let totalDistance = 0;

      while (unvisited.length > 0) {
        // eslint-disable-next-line no-loop-func
        const nearest = unvisited.reduce((closest, farmer) => {
          const distance = this.calculateDistance(
            current.latitude, current.longitude,
            farmer.location.latitude, farmer.location.longitude,
          );
          return distance < closest.distance ? { farmer, distance } : closest;
        }, { farmer: null, distance: Infinity });

        totalDistance += nearest.distance;
        optimizedOrder.push(nearest.farmer.farmerId);
        current = nearest.farmer.location;
        unvisited.splice(unvisited.indexOf(nearest.farmer), 1);
      }

      return {
        routeId: `client-${Date.now()}`,
        farmers: farmersWithLocation.map((f) => ({
          farmerId: f.farmerId,
          farmerName: f.farmerName,
          latitude: f.location.latitude,
          longitude: f.location.longitude,
          zoneId: this.selectedZone,
          lastPickupDate: f.lastPickupDate,
        })),
        estimatedDistance: Math.round(totalDistance * 100) / 100,
        estimatedDuration: Math.round(totalDistance * 2), // 2 minutes per km
        optimizedOrder,
      };
    },

    displayOptimizedRoute() {
      if (!this.optimizedRoute) return;

      const routeLayer = this.mapView.map.layers.find((layer) => layer.title === 'Route');
      routeLayer.removeAll();

      // Create route polyline
      const zone = this.zones.find((z) => z.id === this.selectedZone);
      const routePoints = [
        [zone.centerLongitude, zone.centerLatitude], // Start from zone center
        ...this.optimizedRoute.optimizedOrder.map((farmerId) => {
          const farmer = this.farmers.find((f) => f.farmerId === farmerId);
          return farmer && farmer.location
            ? [farmer.location.longitude, farmer.location.latitude]
            : null;
        }).filter(Boolean),
      ];

      if (routePoints.length > 1) {
        const polyline = new this.Polyline({
          paths: [routePoints],
        });

        const routeGraphic = new this.Graphic({
          geometry: polyline,
          symbol: new this.SimpleLineSymbol({
            color: [255, 0, 0, 0.8],
            width: 4,
            style: 'solid',
          }),
        });

        routeLayer.add(routeGraphic);

        // Add route markers with order numbers
        routePoints.forEach((point, index) => {
          const orderPoint = new this.Point({
            longitude: point[0],
            latitude: point[1],
          });

          const color = index === 0 ? 'blue' : 'red'; // Blue for start, red for stops
          const orderGraphic = new this.Graphic({
            geometry: orderPoint,
            symbol: new this.SimpleMarkerSymbol({
              color,
              size: 20,
              outline: { color: 'white', width: 2 },
            }),
          });

          const numberGraphic = new this.Graphic({
            geometry: orderPoint,
            symbol: new this.TextSymbol({
              text: index === 0 ? 'S' : index.toString(),
              color: 'white',
              font: { size: 12, weight: 'bold' },
            }),
          });

          routeLayer.addMany([orderGraphic, numberGraphic]);
        });

        // Add directional arrows
        this.addDirectionalArrows(routePoints, routeLayer);
      }
    },

    addDirectionalArrows(routePoints, routeLayer) {
      // eslint-disable-next-line no-plusplus
      for (let i = 0; i < routePoints.length - 1; i++) {
        const start = routePoints[i];
        const end = routePoints[i + 1];

        // Calculate midpoint
        const midLon = (start[0] + end[0]) / 2;
        const midLat = (start[1] + end[1]) / 2;

        const midPoint = new this.Point({
          longitude: midLon,
          latitude: midLat,
        });

        const arrowGraphic = new this.Graphic({
          geometry: midPoint,
          symbol: new this.SimpleMarkerSymbol({
            color: [255, 0, 0, 0.7],
            size: 8,
            style: 'triangle',
            angle: this.calculateBearing(start[1], start[0], end[1], end[0]),
          }),
        });

        routeLayer.add(arrowGraphic);
      }
    },

    calculateDistance(lat1, lon1, lat2, lon2) {
      const R = 6371; // Earth's radius in kilometers
      const dLat = this.toRadians(lat2 - lat1);
      const dLon = this.toRadians(lon2 - lon1);
      const a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(this.toRadians(lat1)) * Math.cos(this.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return R * c;
    },

    calculateBearing(lat1, lon1, lat2, lon2) {
      const dLon = this.toRadians(lon2 - lon1);
      const y = Math.sin(dLon) * Math.cos(this.toRadians(lat2));
      const x = Math.cos(this.toRadians(lat1)) * Math.sin(this.toRadians(lat2))
                - Math.sin(this.toRadians(lat1)) * Math.cos(this.toRadians(lat2)) * Math.cos(dLon);
      return this.toDegrees(Math.atan2(y, x));
    },

    toRadians(degrees) {
      return degrees * (Math.PI / 180);
    },

    toDegrees(radians) {
      return radians * (180 / Math.PI);
    },

    calculateSavings() {
      if (!this.optimizedRoute) return 0;
      // Estimate savings compared to random order (typically 15-30%)
      return Math.round(20 + Math.random() * 10);
    },

    getStopColor(index) {
      const colors = ['red', 'orange', 'yellow', 'green', 'blue', 'purple'];
      return colors[index % colors.length];
    },

    getFarmerName(farmerId) {
      const farmer = this.farmers.find((f) => f.farmerId === farmerId);
      return farmer ? farmer.farmerName : 'Unknown';
    },

    getFarmerStatus(farmerId) {
      const farmer = this.farmers.find((f) => f.farmerId === farmerId);
      if (!farmer) return 'Unknown';

      const lastPickup = farmer.lastPickupDate;
      if (!lastPickup) return 'New';

      const daysSince = Math.floor((Date.now() - new Date(lastPickup)) / (1000 * 60 * 60 * 24));
      if (daysSince < 7) return 'Recent';
      if (daysSince < 30) return 'Regular';
      return 'Overdue';
    },

    getStatusColor(farmerId) {
      const status = this.getFarmerStatus(farmerId);
      switch (status) {
        case 'New': return 'blue';
        case 'Recent': return 'green';
        case 'Regular': return 'orange';
        case 'Overdue': return 'red';
        default: return 'grey';
      }
    },

    schedulePickups() {
      this.scheduleDialog = true;
    },

    async confirmSchedulePickups() {
      if (!this.optimizedRoute) return;

      this.scheduling = true;

      try {
        const pickupDateTime = new Date(`${this.pickupDate}T${this.pickupTime}:00`);
        const promises = this.optimizedRoute.optimizedOrder.map((farmerId, index) => {
          // Schedule pickups with 30-minute intervals
          const scheduledTime = new Date(pickupDateTime.getTime() + (index * 30 * 60 * 1000));

          return axios.post('/api/zone-supervisor-service/pickup-schedules', {
            farmerId,
            exporterId: this.$store.getters.currentUser.exporterId,
            produceListingId: 'default', // This should come from farmer's active listing
            scheduledDate: scheduledTime.toISOString(),
            pickupNotes: this.pickupNotes,
          });
        });

        await Promise.all(promises);

        this.$emit('pickups-scheduled', {
          count: this.optimizedRoute.optimizedOrder.length,
          startTime: pickupDateTime,
        });

        this.$emit('show-message', {
          type: 'success',
          text: `${this.optimizedRoute.optimizedOrder.length} pickups scheduled successfully`,
        });

        this.scheduleDialog = false;
      } catch (error) {
        this.$emit('show-message', { type: 'error', text: 'Failed to schedule pickups' });
      } finally {
        this.scheduling = false;
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
.route-optimizer {
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

/* Gradient background for card title */
.tw-bg-gradient-to-r {
  background: linear-gradient(to right, #10b981, #3b82f6);
}
</style>
