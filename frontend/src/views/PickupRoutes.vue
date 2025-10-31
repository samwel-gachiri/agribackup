<template>
    <div class="tw-p-6 tw-space-y-6">
      <!-- Header -->
      <div class="tw-flex tw-items-end tw-justify-between tw-flex-wrap tw-gap-4">
        <div class="tw-space-y-1">
          <h1 class="tw-text-2xl tw-font-bold tw-text-gray-900">Pickup Routes</h1>
          <p class="tw-text-sm tw-text-gray-500">Plan and track multi-stop pickups with an optimized route.</p>
        </div>
        <div class="tw-flex tw-items-center tw-gap-3">
          <input type="date" v-model="selectedDate" class="tw-border tw-border-gray-300 tw-rounded-lg tw-px-3 tw-py-2 tw-text-sm tw-text-gray-800 focus:tw-outline-none focus:tw-ring-2 focus:tw-ring-green-500" />
          <button @click="openCreate" class="tw-inline-flex tw-items-center tw-gap-2 tw-bg-green-600 hover:tw-bg-green-700 tw-text-white tw-font-medium tw-px-4 tw-py-2 tw-rounded-lg tw-shadow-sm tw-transition">
            <span>Plan Route</span>
          </button>
        </div>
      </div>

      <!-- Loading / Empty -->
      <div v-if="loading" class="tw-text-gray-500">Loading routes…</div>
      <div v-else-if="routes.length === 0" class="tw-bg-white tw-border tw-border-gray-100 tw-rounded-xl tw-shadow-sm tw-p-8 tw-text-center tw-space-y-3">
        <div class="tw-text-lg tw-font-medium tw-text-gray-800">No routes on {{ selectedDate }}</div>
        <p class="tw-text-gray-500 tw-text-sm">Create your first pickup route using predictions and smart ordering.</p>
        <button @click="openCreate" class="tw-inline-flex tw-items-center tw-gap-2 tw-bg-gray-900 hover:tw-bg-black tw-text-white tw-font-medium tw-px-4 tw-py-2 tw-rounded-lg tw-transition">Plan a new route</button>
      </div>

      <!-- Routes Grid -->
      <div v-else class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 xl:tw-grid-cols-3 tw-gap-4">
        <div v-for="r in routes" :key="r.routeId" class="tw-bg-white tw-border tw-border-gray-100 tw-rounded-xl tw-shadow-sm tw-p-5 tw-space-y-3 hover:tw-shadow-md tw-transition">
          <div class="tw-flex tw-items-start tw-justify-between">
            <div class="tw-space-y-1">
              <div class="tw-text-[11px] tw-font-mono tw-text-gray-400">{{ r.routeId }}</div>
              <div class="tw-text-sm tw-text-gray-500">{{ formatDateTime(r.scheduledDate) }}</div>
            </div>
            <span :class="statusClass(r.status)" class="tw-text-[11px] tw-font-semibold tw-rounded-full tw-px-2 tw-py-1">{{ r.status }}</span>
          </div>
          <div class="tw-flex tw-items-center tw-justify-between tw-pt-2">
            <div class="tw-text-sm tw-text-gray-700"><span class="tw-font-semibold">{{ (r.stops && r.stops.length) || r.stopCount || 0 }}</span> stops</div>
            <button @click="selectRoute(r.routeId)" class="tw-text-sm tw-text-green-700 hover:tw-text-green-800 tw-font-medium">Open</button>
          </div>
        </div>
      </div>

      <!-- Drawers / Modals -->
      <PickupRouteDetail v-if="activeRoute" :route-id="activeRoute" @close="activeRoute=null" />
      <CreatePickupRouteModal v-if="showCreate" @close="showCreate=false" @created="onCreated" />
    </div>
 </template>

<script>
// import pickupRouteService from '@/services/pickupRoute.service.js';
import PickupRouteDetail from './PickupRoutes/PickupRouteDetail.vue';
import CreatePickupRouteModal from './PickupRoutes/CreatePickupRouteModal.vue';

export default {
  components: { PickupRouteDetail, CreatePickupRouteModal },
  data() {
    return {
      selectedDate: new Date().toISOString().substring(0, 10),
      routes: [],
      loading: false,
      activeRoute: null,
      showCreate: false,
    };
  },
  created() {
    this.fetchRoutes();
  },
  watch: {
    selectedDate() {
      this.fetchRoutes();
    },
  },
  methods: {
    async fetchRoutes() {
      this.loading = true;
      try {
        // Mock routes data
        await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate API call
        this.routes = [
          {
            routeId: 'route_001',
            name: 'Morning Nairobi Route',
            scheduledDate: this.selectedDate,
            status: 'PLANNED',
            farmerCount: 12,
            estimatedDuration: '3.5 hours',
            totalDistance: '45.2 km',
            createdAt: new Date().toISOString(),
          },
          {
            routeId: 'route_002',
            name: 'Afternoon Kiambu Route',
            scheduledDate: this.selectedDate,
            status: 'IN_PROGRESS',
            farmerCount: 8,
            estimatedDuration: '2.8 hours',
            totalDistance: '32.1 km',
            createdAt: new Date(Date.now() - 3600000).toISOString(),
          },
          {
            routeId: 'route_003',
            name: 'Evening Nakuru Route',
            scheduledDate: this.selectedDate,
            status: 'COMPLETED',
            farmerCount: 15,
            estimatedDuration: '4.2 hours',
            totalDistance: '67.8 km',
            createdAt: new Date(Date.now() - 7200000).toISOString(),
          },
        ];
      } catch (e) { this.routes = []; }
      this.loading = false;
    },
    openCreate() { this.showCreate = true; },
    onCreated(route) {
      // If API returns full route detail, refresh list; otherwise push summary card
      if (!route || !route.routeId) { this.fetchRoutes(); return; }
      this.fetchRoutes();
      this.showCreate = false;
    },
    selectRoute(id) { this.activeRoute = id; },
    statusClass(status) {
      switch (status) {
        case 'PLANNED': return 'tw-bg-blue-100 tw-text-blue-700';
        case 'IN_PROGRESS': return 'tw-bg-yellow-100 tw-text-yellow-700';
        case 'COMPLETED': return 'tw-bg-green-100 tw-text-green-700';
        case 'CANCELLED': return 'tw-bg-red-100 tw-text-red-700';
        default: return 'tw-bg-gray-100 tw-text-gray-600';
      }
    },
    formatDateTime(dt) {
      if (!dt) return '';
      return new Date(dt).toLocaleString();
    },
  },
};
</script>

<style scoped>
</style>
