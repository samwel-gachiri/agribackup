<template>
  <Default>
    <div class="tw-p-6 tw-space-y-6">
      <!-- Header -->
      <div class="tw-flex tw-items-end tw-justify-between tw-flex-wrap tw-gap-3">
        <div>
          <h1 class="tw-text-2xl tw-font-bold tw-text-gray-900">Zone Comments</h1>
          <p class="tw-text-sm tw-text-gray-500">Review and update comments for your zones.</p>
        </div>
        <button @click="refresh" class="tw-bg-gray-900 hover:tw-bg-black tw-text-white tw-text-sm tw-font-medium tw-rounded-lg tw-px-4 tw-py-2">
          Refresh
        </button>
      </div>

      <!-- Content -->
      <div class="tw-grid tw-grid-cols-1 lg:tw-grid-cols-3 tw-gap-6">
        <!-- Zones list -->
        <div class="tw-bg-white tw-rounded-xl tw-shadow-sm tw-border tw-border-gray-100 tw-p-4">
          <div class="tw-flex tw-items-center tw-justify-between tw-mb-3">
            <div class="tw-font-semibold tw-text-gray-800">My Zones</div>
            <input v-model="search" type="text" placeholder="Search…" class="tw-text-sm tw-border tw-border-gray-300 tw-rounded-lg tw-px-2 tw-py-1 focus:tw-ring-2 focus:tw-ring-green-500 focus:tw-outline-none" />
          </div>
          <div v-if="loading" class="tw-text-sm tw-text-gray-500">Loading…</div>
          <ul v-else class="tw-divide-y tw-divide-gray-100 tw-max-h-[520px] tw-overflow-y-auto">
            <li v-for="z in filteredZones" :key="z.id" @click="select(z)" class="tw-py-3 tw-cursor-pointer tw-group">
              <div class="tw-flex tw-items-start tw-justify-between">
                <div>
                  <div class="tw-font-medium tw-text-gray-800 group-hover:tw-text-green-700">{{ z.name }}</div>
                  <div class="tw-text-xs tw-text-gray-500">{{ z.produceType || 'Mixed' }} • {{ z.farmerCount || 0 }} farmers</div>
                </div>
                <span class="tw-text-[10px] tw-rounded-full tw-bg-gray-100 tw-text-gray-600 tw-px-2 tw-py-0.5">{{ z.radiusKm }} km</span>
              </div>
            </li>
          </ul>
        </div>

        <!-- Details / Editor -->
        <div class="tw-bg-white tw-rounded-xl tw-shadow-sm tw-border tw-border-gray-100 tw-p-6 lg:tw-col-span-2">
          <div v-if="!activeZone" class="tw-text-center tw-text-gray-500 tw-py-20">Select a zone to view and edit comments.</div>
          <div v-else class="tw-space-y-5">
            <div class="tw-flex tw-items-start tw-justify-between tw-gap-4">
              <div>
                <div class="tw-text-xl tw-font-semibold tw-text-gray-900">{{ activeZone.name }}</div>
                <div class="tw-text-sm tw-text-gray-500">Produce: {{ activeZone.produceType || 'Mixed' }} • Radius: {{ activeZone.radiusKm }} km</div>
              </div>
              <div class="tw-text-xs tw-text-gray-400">ID: {{ activeZone.id }}</div>
            </div>

            <div class="tw-space-y-2">
              <label class="tw-text-sm tw-font-medium tw-text-gray-700">Comments</label>
              <textarea v-model="form.comments" class="tw-w-full tw-min-h-[140px] tw-border tw-border-gray-300 tw-rounded-lg tw-p-3 tw-text-sm focus:tw-ring-2 focus:tw-ring-green-500 focus:tw-outline-none" placeholder="Add notes, constraints, or context for this zone…"></textarea>
              <div class="tw-text-xs tw-text-gray-500">These comments are visible to supervisors managing this zone.</div>
            </div>

            <div class="tw-flex tw-justify-end tw-gap-2">
              <button @click="reset" class="tw-bg-white tw-border tw-border-gray-300 tw-text-gray-700 hover:tw-bg-gray-50 tw-rounded-lg tw-text-sm tw-font-medium tw-px-4 tw-py-2">Reset</button>
              <button :disabled="saving" @click="save" class="tw-bg-green-600 hover:tw-bg-green-700 disabled:tw-opacity-60 tw-text-white tw-rounded-lg tw-text-sm tw-font-medium tw-px-4 tw-py-2">
                {{ saving ? 'Saving…' : 'Save Comments' }}
              </button>
            </div>

            <div v-if="message" :class="messageClass" class="tw-text-sm tw-rounded-md tw-px-3 tw-py-2">{{ message }}</div>
          </div>
        </div>
      </div>
    </div>
  </Default>
 </template>

<script>
import Default from '@/components/layout/Default.vue';
// import zoneService from '@/services/zone.service.js';

export default {
  name: 'ZoneCommentsManagement',
  components: { Default },
  data() {
    return {
      zones: [],
      loading: false,
      activeZone: null,
      form: { comments: '' },
      saving: false,
      search: '',
      message: '',
      messageType: 'success',
    };
  },
  computed: {
    filteredZones() {
      if (!this.search) return this.zones;
      const s = this.search.toLowerCase();
      return this.zones.filter((z) => `${z.name} ${z.produceType || ''}`.toLowerCase().includes(s));
    },
    messageClass() {
      return this.messageType === 'success'
        ? 'tw-bg-green-50 tw-text-green-700 tw-border tw-border-green-100'
        : 'tw-bg-red-50 tw-text-red-700 tw-border tw-border-red-100';
    },
  },
  created() {
    this.loadZones();
  },
  methods: {
    async loadZones() {
      this.loading = true;
      try {
        // Mock zones data
        await new Promise((resolve) => setTimeout(resolve, 800)); // Simulate API call
        this.zones = [
          {
            id: 'zone_001',
            name: 'Nairobi Central Zone',
            produceType: 'Vegetables',
            farmerCount: 25,
            radiusKm: 15,
            comments: 'High demand for tomatoes and leafy greens. Good transport infrastructure.',
          },
          {
            id: 'zone_002',
            name: 'Kiambu Agricultural Zone',
            produceType: 'Mixed',
            farmerCount: 18,
            radiusKm: 12,
            comments: 'Focus on maize and beans. Some areas have irrigation challenges.',
          },
          {
            id: 'zone_003',
            name: 'Nakuru Produce Zone',
            produceType: 'Fruits',
            farmerCount: 32,
            radiusKm: 20,
            comments: 'Excellent for potatoes and carrots. Well-established farming community.',
          },
        ];
        if (this.zones.length) {
          const fromQuery = this.$route && this.$route.query ? this.$route.query.zoneId : null;
          const found = fromQuery ? this.zones.find((z) => z.id === fromQuery) : null;
          this.select(found || this.zones[0]);
        }
      } catch (e) {
        this.$toast.error('Failed to load zones');
        this.zones = [];
      }
      this.loading = false;
    },
    select(zone) {
      this.activeZone = zone;
      this.form = { comments: zone.comments || '' };
      this.message = '';
    },
    reset() {
      if (!this.activeZone) return;
      this.form.comments = this.activeZone.comments || '';
      this.message = '';
    },
    async save() {
      if (!this.activeZone) return;
      this.saving = true;
      try {
        // Mock update comments
        await new Promise((resolve) => setTimeout(resolve, 500)); // Simulate API call
        // Update local zone data
        const updatedZone = { ...this.activeZone, comments: this.form.comments || null };
        const idx = this.zones.findIndex((z) => z.id === updatedZone.id);
        if (idx !== -1) this.zones.splice(idx, 1, updatedZone);
        this.activeZone = updatedZone;
        this.$toast.success('Zone comments updated');
      } catch (e) {
        this.$toast.error('Failed to update comments');
      }
      this.saving = false;
    },
    refresh() {
      this.loadZones();
    },
  },
};
</script>

<style scoped>
</style>
