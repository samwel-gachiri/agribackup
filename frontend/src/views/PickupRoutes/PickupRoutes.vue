<template>
  <div class="space-y-4">
    <div class="flex items-center gap-2">
      <input type="date" v-model="date" class="border p-2 rounded" />
      <button class="px-3 py-2 bg-green-600 text-white rounded" @click="openCreate">New Route</button>
    </div>
    <div>
      <h3 class="font-semibold mb-2">Routes on {{ date }}</h3>
      <ul class="space-y-2">
        <li v-for="r in routes" :key="r.routeId" class="p-3 border rounded flex justify-between items-center">
          <div>
            <div class="font-medium">Route {{ r.routeId }}</div>
            <div class="text-xs text-gray-500">Stops: {{ r.stopCount }} • Status: {{ r.status }}</div>
          </div>
          <button class="text-blue-600" @click="select(r.routeId)">Open</button>
        </li>
      </ul>
    </div>

    <CreatePickupRouteModal v-if="showCreate" @close="showCreate=false" @created="onCreated" />
    <PickupRouteDetail v-if="selected" :route-id="selected" @close="selected=null" />
  </div>
</template>
<script>
// import pickupRouteService from '@/services/pickupRoute.service.js';
import CreatePickupRouteModal from './CreatePickupRouteModal.vue';
import PickupRouteDetail from './PickupRouteDetail.vue';

export default {
  components: { CreatePickupRouteModal, PickupRouteDetail },
  data() {
    const today = new Date().toISOString().slice(0, 10);
    return {
      date: today,
      routes: [],
      showCreate: false,
      selected: null,
    };
  },
  created() { this.load(); },
  methods: {
    async load() {
      // Mock routes data
      await new Promise((resolve) => setTimeout(resolve, 800)); // Simulate API call
      this.routes = [
        {
          routeId: 'route_001',
          name: 'Morning Nairobi Route',
          scheduledDate: this.date,
          status: 'PLANNED',
          farmerCount: 12,
          estimatedDuration: '3.5 hours',
          totalDistance: '45.2 km',
        },
        {
          routeId: 'route_002',
          name: 'Afternoon Kiambu Route',
          scheduledDate: this.date,
          status: 'IN_PROGRESS',
          farmerCount: 8,
          estimatedDuration: '2.8 hours',
          totalDistance: '32.1 km',
        },
      ];
    },
    openCreate() { this.showCreate = true; },
    async onCreated() { this.showCreate = false; await this.load(); },
    select(id) { this.selected = id; },
  },
};
</script>
