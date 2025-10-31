<template>
  <div class="tw-fixed tw-inset-0 tw-bg-black/40 tw-backdrop-blur-[1px] tw-flex tw-items-center tw-justify-center tw-z-50">
    <div class="tw-bg-white tw-rounded-2xl tw-shadow-xl tw-w-full tw-max-w-xl tw-p-6">
      <div class="tw-flex tw-items-start tw-justify-between tw-mb-4">
        <div>
          <h2 class="tw-text-xl tw-font-bold tw-text-gray-900">Plan a Pickup Route</h2>
          <p class="tw-text-xs tw-text-gray-500">Use predictions to select the best farmers and sequence.</p>
        </div>
        <button type="button" @click="$emit('close')" class="tw-text-gray-400 hover:tw-text-gray-600">✕</button>
      </div>
      <form @submit.prevent="submit" class="tw-space-y-5">
        <div>
          <label class="tw-block tw-text-sm tw-font-medium tw-text-gray-700 tw-mb-1">Scheduled Date & Time</label>
          <input type="datetime-local" v-model="scheduledDate" class="tw-border tw-border-gray-300 tw-w-full tw-px-3 tw-py-2 tw-rounded-lg focus:tw-outline-none focus:tw-ring-2 focus:tw-ring-green-500" required />
        </div>
        <div>
          <label class="tw-block tw-text-sm tw-font-medium tw-text-gray-700 tw-mb-1">Zone</label>
          <select v-model="zoneId" class="tw-border tw-border-gray-300 tw-w-full tw-px-3 tw-py-2 tw-rounded-lg focus:tw-outline-none focus:tw-ring-2 focus:tw-ring-green-500" required>
            <option disabled value="">Select zone</option>
            <option v-for="z in zones" :key="z.id" :value="z.id">{{ z.name }}</option>
          </select>
        </div>
        <div class="tw-border tw-border-gray-100 tw-bg-gray-50 tw-rounded-xl tw-p-3">
          <div class="tw-flex tw-items-center tw-justify-between tw-mb-2">
            <div class="tw-text-sm tw-font-medium tw-text-gray-800">Suggestions (next 14 days)</div>
            <div class="tw-flex tw-items-center tw-gap-2">
              <label class="tw-text-xs tw-text-gray-500">Min confidence</label>
              <input type="number" step="0.05" min="0" max="1" v-model.number="minConfidence" class="tw-border tw-border-gray-300 tw-px-2 tw-py-1 tw-rounded-md tw-text-sm tw-w-24" />
              <button type="button" class="tw-text-xs tw-border tw-border-gray-300 hover:tw-border-gray-400 tw-rounded-md tw-px-2 tw-py-1 tw-text-gray-700" @click="loadSuggestions">Refresh</button>
            </div>
          </div>
          <div class="tw-max-h-36 tw-overflow-y-auto tw-space-y-2">
            <div v-for="s in suggestions" :key="s.farmerId + s.produceName" class="tw-flex tw-items-center tw-justify-between tw-bg-white tw-rounded-lg tw-border tw-border-gray-100 tw-px-3 tw-py-2">
              <div>
                <div class="tw-text-sm tw-font-medium tw-text-gray-900">{{ s.farmerName }} • {{ s.produceName }}</div>
                <div class="tw-text-[11px] tw-text-gray-500">Harvest: {{ s.predictedHarvestDate }} • Qty: {{ s.expectedQuantity || '?' }} {{ s.expectedUnit || '' }}</div>
              </div>
              <button type="button" class="tw-text-sm tw-text-green-700 hover:tw-text-green-800 tw-font-medium" @click="toggleFarmer(s.farmerId)">
                {{ farmerIds.includes(s.farmerId) ? 'Remove' : 'Add' }}
              </button>
            </div>
          </div>
        </div>
        <div>
          <label class="tw-block tw-text-sm tw-font-medium tw-text-gray-700 tw-mb-1">Farmers (multi‑select)</label>
          <div class="tw-border tw-border-gray-200 tw-rounded-xl tw-p-2 tw-h-40 tw-overflow-y-auto">
            <label v-for="f in farmers" :key="f.id" class="tw-flex tw-items-center tw-justify-between tw-text-sm tw-gap-2 tw-py-1 tw-px-2 hover:tw-bg-gray-50 tw-rounded-lg">
              <div class="tw-flex tw-items-center tw-gap-2">
                <input type="checkbox" :value="f.id" v-model="farmerIds" />
                <span class="tw-text-gray-800">{{ f.name }}</span>
              </div>
              <div class="tw-text-[11px] tw-text-gray-500" v-if="f.expected">
                {{ f.expected.qty }} {{ f.expected.unit }}
              </div>
            </label>
          </div>
        </div>
        <div class="tw-flex tw-justify-end tw-gap-2">
          <button type="button" @click="$emit('close')" class="tw-px-4 tw-py-2 tw-rounded-lg tw-border tw-border-gray-300 hover:tw-bg-gray-50 tw-text-gray-700">Cancel</button>
          <button type="submit" :disabled="submitting" class="tw-px-4 tw-py-2 tw-rounded-lg tw-bg-green-600 hover:tw-bg-green-700 tw-text-white tw-shadow-sm">Create</button>
        </div>
      </form>
    </div>
  </div>
</template>
<script>
import axios from 'axios';
// import pickupRouteService from '@/services/pickupRoute.service.js';

export default {
  data() {
    return {
      scheduledDate: new Date().toISOString().substring(0, 16),
      zones: [],
      farmers: [],
      zoneId: '',
      exporterId: '',
      farmerIds: [],
      suggestions: [],
      minConfidence: 0.4,
      submitting: false,
    };
  },
  created() {
    this.initialize();
  },
  methods: {
    async initialize() {
      await this.loadZones();
      if (this.zoneId) await this.loadFarmers();
      await this.loadSuggestions();
    },
    async loadZones() {
      try {
        const res = await axios.get('/api/zone-supervisor-service/zones/assigned');
        const data = res.data?.data || res.data;
        this.zones = Array.isArray(data) ? data : [];
        if (!this.zoneId && this.zones.length) {
          this.zoneId = this.zones[0].id;
          this.exporterId = this.zones[0].exporterId;
        }
      } catch (e) {
        this.$emit('error', 'Failed to load zones');
      }
    },
    async loadFarmers() {
      try {
        const res = await axios.get('/api/zone-supervisor-service/farmers');
        const all = res.data?.data || res.data || [];
        const filtered = all.filter((f) => f.zoneId === this.zoneId);
        this.farmers = filtered.map((f) => ({ id: f.farmerId, name: f.farmerName }));
      } catch (e) {
        this.$emit('error', 'Failed to load farmers');
      }
    },
    async loadSuggestions() {
      try {
        // Mock pickup suggestions
        await new Promise((resolve) => setTimeout(resolve, 800)); // Simulate API call
        this.suggestions = [
          {
            farmerId: 'farmer_001',
            farmerName: 'John Farmer',
            expectedQuantity: 150,
            expectedUnit: 'kg',
            confidence: 0.87,
            predictedHarvestDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
          },
          {
            farmerId: 'farmer_002',
            farmerName: 'Mary Johnson',
            expectedQuantity: 200,
            expectedUnit: 'kg',
            confidence: 0.92,
            predictedHarvestDate: new Date(Date.now() + 10 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
          },
          {
            farmerId: 'farmer_003',
            farmerName: 'David Smith',
            expectedQuantity: 120,
            expectedUnit: 'kg',
            confidence: 0.78,
            predictedHarvestDate: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
          },
        ];

        // Merge suggestions into farmers list for quick selection display
        const mapExp = new Map(this.suggestions.map((s) => [s.farmerId, s]));
        this.farmers = this.farmers.map((f) => ({
          ...f,
          expected: mapExp.get(f.id)
            ? { qty: mapExp.get(f.id).expectedQuantity, unit: mapExp.get(f.id).expectedUnit }
            : null,
        }));
      } catch (e) {
        this.$emit('error', 'Failed to load suggestions');
      }
    },
    toggleFarmer(farmerId) {
      const idx = this.farmerIds.indexOf(farmerId);
      if (idx >= 0) this.farmerIds.splice(idx, 1); else this.farmerIds.push(farmerId);
    },
    async submit() {
      if (this.farmerIds.length === 0) {
        // console.warn('No farmers selected');
        this.$emit('error', 'Select at least one farmer');
        return;
      }
      this.submitting = true;
      try {
        // Mock route creation
        await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate API call
        const route = {
          id: `route_${Date.now()}`,
          scheduledDate: this.scheduledDate,
          zoneId: this.zoneId,
          exporterId: this.exporterId,
          farmerIds: this.farmerIds,
          status: 'scheduled',
          createdAt: new Date().toISOString(),
          totalFarmers: this.farmerIds.length,
          estimatedDuration: '2-3 hours',
          estimatedDistance: '45 km',
        };
        this.$emit('created', route);
      } catch (e) {
        this.$emit('error', 'Failed to create');
      }
      this.submitting = false;
    },
  },
  watch: {
    zoneId(newVal, oldVal) {
      if (newVal !== oldVal) {
        const z = this.zones.find((x) => x.id === newVal);
        this.exporterId = z?.exporterId || this.exporterId;
        this.loadFarmers();
        this.loadSuggestions();
      }
    },
  },
};
</script>
