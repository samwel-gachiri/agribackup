<template>
  <div class="tw-fixed tw-inset-0 tw-bg-black/30 tw-flex tw-items-start tw-justify-end tw-z-40">
    <div class="tw-bg-white tw-w-full tw-max-w-4xl tw-h-full tw-flex tw-flex-col tw-shadow-2xl tw-rounded-l-2xl">
      <div class="tw-flex tw-items-center tw-justify-between tw-p-4 tw-border-b">
        <div>
          <h2 class="tw-text-lg tw-font-bold">Route {{ route?.routeId }}</h2>
          <p class="tw-text-[11px] tw-text-gray-500" v-if="route">Scheduled: {{ formatDateTime(route.scheduledDate) }}</p>
        </div>
        <button @click="$emit('close')" class="tw-text-gray-500 hover:tw-text-black">✕</button>
      </div>
      <div class="tw-flex-1 tw-overflow-y-auto tw-p-4 tw-grid tw-grid-cols-1 lg:tw-grid-cols-3 tw-gap-4">
        <div class="lg:tw-col-span-2 tw-space-y-4">
          <div class="tw-h-96 tw-bg-gray-200 tw-rounded-xl tw-relative">
            <div id="route-map" class="tw-absolute tw-inset-0 tw-rounded-xl"></div>
          </div>
          <div>
            <h3 class="tw-font-semibold tw-mb-2">Stops</h3>
            <ol class="tw-space-y-2">
              <li v-for="s in route?.stops || []" :key="s.stopId" class="tw-p-2 tw-border tw-border-gray-200 tw-rounded-lg tw-flex tw-justify-between tw-items-center">
                <div>
                  <div class="tw-text-sm tw-font-medium">{{ s.farmerName || s.farmerId }}</div>
                  <div class="tw-text-[11px] tw-text-gray-500">Order: {{ s.sequenceOrder }}</div>
                </div>
                <div class="tw-flex tw-items-center tw-gap-2">
                  <select v-model="stopStatus[s.stopId]" @change="updateStopStatus(s)" class="tw-text-xs tw-border tw-rounded tw-p-1">
                    <option>PENDING</option>
                    <option>ARRIVED</option>
                    <option>COMPLETED</option>
                    <option>SKIPPED</option>
                  </select>
                </div>
              </li>
            </ol>
          </div>
        </div>
        <div class="tw-space-y-4">
          <div class="tw-border tw-border-gray-200 tw-rounded-xl tw-p-4">
            <h3 class="tw-font-semibold tw-mb-2">Route Status</h3>
            <select v-model="routeStatus" @change="updateRouteStatus" class="tw-border tw-rounded tw-p-2 tw-w-full">
              <option>PLANNED</option>
              <option>IN_PROGRESS</option>
              <option>COMPLETED</option>
              <option>CANCELLED</option>
            </select>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
// import pickupRouteService from '@/services/pickupRoute.service.js';
import L from 'leaflet';

export default {
  props: { routeId: { type: String, required: true } },
  data() {
    return {
      route: null,
      routeStatus: 'PLANNED',
      stopStatus: {},
      map: null,
      markers: [],
      polyline: null,
    };
  },
  created() { this.load(); },
  methods: {
    async load() {
      // Mock route data
      this.route = {
        routeId: this.routeId,
        status: 'PLANNED',
        scheduledDate: new Date().toISOString().split('T')[0],
        stops: [
          {
            stopId: 'stop_1',
            farmerName: 'John Farmer',
            location: 'Nairobi Central',
            latitude: -1.2864,
            longitude: 36.8172,
            status: 'PENDING',
            produce: 'Tomatoes',
            quantity: 150,
            unit: 'kg',
          },
          {
            stopId: 'stop_2',
            farmerName: 'Mary Johnson',
            location: 'Kiambu Region',
            latitude: -1.1715,
            longitude: 36.8304,
            status: 'PENDING',
            produce: 'Maize',
            quantity: 200,
            unit: 'kg',
          },
          {
            stopId: 'stop_3',
            farmerName: 'David Smith',
            location: 'Nakuru Area',
            latitude: -0.3031,
            longitude: 36.0800,
            status: 'PENDING',
            produce: 'Potatoes',
            quantity: 100,
            unit: 'kg',
          },
        ],
      };
      this.routeStatus = this.route.status;
      this.stopStatus = this.route.stops.reduce((acc, s) => { acc[s.stopId] = s.status; return acc; }, {});
      this.$nextTick(() => this.renderMap());
    },
    renderMap() {
      if (!this.map) {
        this.map = L.map('route-map');
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '&copy; OpenStreetMap contributors',
        }).addTo(this.map);
      }
      // Clear old layers
      this.markers.forEach((m) => m.remove());
      this.markers = [];
      if (this.polyline) { this.polyline.remove(); this.polyline = null; }
      const coords = (this.route.stops || [])
        .filter((s) => s.latitude != null && s.longitude != null)
        .map((s) => [s.latitude, s.longitude]);
      coords.forEach((c, idx) => {
        const m = L.marker(c).bindPopup(`#${idx + 1}`);
        m.addTo(this.map);
        this.markers.push(m);
      });
      if (coords.length) {
        // Try routing via public OSRM; fallback to straight polyline on failure
        const url = `https://router.project-osrm.org/route/v1/driving/${coords.map((c) => `${c[1]},${c[0]}`).join(';')}?overview=full&geometries=geojson`;
        fetch(url).then((r) => r.json()).then((data) => {
          const route = data?.routes?.[0]?.geometry;
          if (route) {
            this.polyline = L.geoJSON(route, { style: { color: 'blue' } }).addTo(this.map);
            this.map.fitBounds(this.polyline.getBounds(), { padding: [20, 20] });
          } else {
            this.polyline = L.polyline(coords, { color: 'blue' }).addTo(this.map);
            this.map.fitBounds(this.polyline.getBounds(), { padding: [20, 20] });
          }
        }).catch(() => {
          this.polyline = L.polyline(coords, { color: 'blue' }).addTo(this.map);
          this.map.fitBounds(this.polyline.getBounds(), { padding: [20, 20] });
        });
      }
    },
    formatDateTime(dt) { if (!dt) return ''; return new Date(dt).toLocaleString(); },
    async updateRouteStatus() {
      // Mock route status update
      // Simulate API delay
      await new Promise((resolve) => setTimeout(resolve, 500));
      this.route.status = this.routeStatus;
      this.$toast.success('Route status updated successfully');
      await this.load();
    },
    async updateStopStatus(stop) {
      // Mock stop status update
      // Simulate API delay
      await new Promise((resolve) => setTimeout(resolve, 500));
      // Update the stop status in the route data
      const stopIndex = this.route.stops.findIndex((s) => s.stopId === stop.stopId);
      if (stopIndex !== -1) {
        this.route.stops[stopIndex].status = this.stopStatus[stop.stopId];
      }
      this.$toast.success('Stop status updated successfully');
      await this.load();
    },
  },
};
</script>

<style scoped>
#route-map { height: 100%; width: 100%; }
</style>
