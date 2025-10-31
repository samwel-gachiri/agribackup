<template>
  <div class="tw-bg-white tw-border tw-border-gray-100 tw-rounded-xl tw-shadow-sm tw-p-5 tw-space-y-4">
    <div class="tw-flex tw-justify-between tw-items-center">
      <h2 class="tw-text-sm tw-font-semibold tw-text-gray-700">Harvest Predictions</h2>
      <div class="tw-flex tw-gap-2">
        <button class="tw-text-xs tw-bg-green-100 tw-text-green-700 tw-px-2 tw-py-1 tw-rounded" @click="refresh" :disabled="loading">Refresh</button>
      </div>
    </div>

    <div class="tw-grid tw-grid-cols-2 tw-gap-3">
      <div>
        <label class="tw-text-[10px] tw-uppercase tw-text-gray-500">Produce</label>
        <select v-model="form.farmerProduceId" class="tw-w-full tw-text-xs tw-border tw-rounded tw-px-2 tw-py-1">
          <option value="" disabled>Select...</option>
          <option v-for="p in produces" :key="p.id" :value="p.id">{{ p.name }}</option>
        </select>
      </div>
      <div>
        <label class="tw-text-[10px] tw-uppercase tw-text-gray-500">Planting Date</label>
        <input type="date" v-model="form.plantingDate" class="tw-w-full tw-text-xs tw-border tw-rounded tw-px-2 tw-py-1" />
      </div>
    </div>
    <button class="tw-w-full tw-bg-green-600 tw-text-white tw-text-xs tw-font-medium tw-py-2 tw-rounded disabled:tw-opacity-50" :disabled="!canPredict || predicting" @click="submitPrediction">{{ predicting ? 'Predicting...' : 'Predict Harvest' }}</button>

    <p v-if="error" class="tw-text-red-500 tw-text-xs">{{ error }}</p>

    <div v-if="predictions.length" class="tw-overflow-x-auto">
      <table class="tw-w-full tw-text-xs tw-border-collapse">
        <thead>
          <tr class="tw-bg-gray-50">
            <th class="tw-text-left tw-font-medium tw-px-2 tw-py-1">Produce</th>
            <th class="tw-text-left tw-font-medium tw-px-2 tw-py-1">Planted</th>
            <th class="tw-text-left tw-font-medium tw-px-2 tw-py-1">Pred Harvest</th>
            <th class="tw-text-left tw-font-medium tw-px-2 tw-py-1">Conf</th>
            <th class="tw-text-left tw-font-medium tw-px-2 tw-py-1">Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in predictions" :key="p.id" class="hover:tw-bg-gray-50">
            <td class="tw-px-2 tw-py-1">{{ p.produceName }}</td>
            <td class="tw-px-2 tw-py-1">{{ fmt(p.plantingDate) }}</td>
            <td class="tw-px-2 tw-py-1">{{ fmt(p.predictedHarvestDate) }}</td>
            <td class="tw-px-2 tw-py-1">{{ pct(p.predictionConfidence) }}</td>
            <td class="tw-px-2 tw-py-1">
              <span :class="statusClass(p.status)" class="tw-px-2 tw-py-0.5 tw-rounded tw-text-[10px] tw-font-semibold">{{ p.status }}</span>
            </td>
            <td class="tw-px-2 tw-py-1">
              <button v-if="p.status !== 'HARVESTED'" class="tw-text-[10px] tw-text-green-700 hover:tw-underline" @click="markHarvested(p)" :disabled="harvestingId===p.id">{{ harvestingId===p.id ? '...' : 'Mark' }}</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-else-if="!loading" class="tw-text-gray-400 tw-text-[11px]">No predictions yet.</div>
  </div>
</template>

<script>
// import harvestApi from '@/services/harvestPrediction.service.js';
import axios from 'axios';
import { getCurrentUserId } from '@/utils/roles.js';

export default {
  name: 'FarmerHarvestPanel',
  props: { farmerId: { type: String, default: null } },
  data() {
    return {
      produces: [],
      predictions: [],
      form: { farmerProduceId: '', plantingDate: '' },
      loading: false,
      predicting: false,
      harvestingId: null,
      error: null,
    };
  },
  computed: {
    effectiveFarmerId() { return this.farmerId || getCurrentUserId(); },
    canPredict() { return this.form.farmerProduceId && this.form.plantingDate; },
  },
  methods: {
    async refresh() {
      this.error = null;
      await Promise.all([this.loadProduces(), this.loadPredictions()]);
    },
    async loadProduces() {
      try {
        const r = await axios.get(`/api/farmers/${this.effectiveFarmerId}/produces`);
        this.produces = r.data?.data || r.data || [];
      } catch { /* silent */ }
    },
    async loadPredictions() {
      try {
        this.loading = true;
        // Mock predictions data
        this.predictions = [
          {
            id: 1,
            farmerProduceId: 'prod_1',
            predictedHarvestDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
            confidence: 0.85,
            status: 'HARVEST_PLANNED',
            quantity: 150,
            unit: 'kg',
          },
          {
            id: 2,
            farmerProduceId: 'prod_2',
            predictedHarvestDate: new Date(Date.now() + 45 * 24 * 60 * 60 * 1000).toISOString(),
            confidence: 0.72,
            status: 'HARVEST_PLANNED',
            quantity: 200,
            unit: 'kg',
          },
        ];
      } catch (e) { this.error = e.message; } finally { this.loading = false; }
    },
    async submitPrediction() {
      if (!this.canPredict) return;
      this.predicting = true; this.error = null;
      try {
        // Mock prediction submission
        await new Promise((resolve) => setTimeout(resolve, 1000)); // Simulate API call
        this.$toast.success('Harvest prediction created successfully');
        this.form.plantingDate = '';
        await this.loadPredictions();
      } catch (e) { this.error = e.message; } finally { this.predicting = false; }
    },
    async markHarvested(p) {
      this.harvestingId = p.id;
      try {
        // Mock harvest marking
        await new Promise((resolve) => setTimeout(resolve, 800)); // Simulate API call
        this.$toast.success('Harvest marked as completed');
        await this.loadPredictions();
      } catch (e) { this.error = e.message; } finally { this.harvestingId = null; }
    },
    fmt(d) { return d ? new Date(d).toLocaleDateString() : '-'; },
    pct(v) { return v == null ? '-' : `${(v * 100).toFixed(0)}%`; },
    statusClass(s) {
      return {
        'tw-bg-teal-100 tw-text-teal-700': s === 'HARVEST_PLANNED',
        'tw-bg-green-100 tw-text-green-700': s === 'HARVESTED',
        'tw-bg-gray-100 tw-text-gray-600': s !== 'HARVESTED' && s !== 'HARVEST_PLANNED',
      };
    },
  },
  mounted() { this.refresh(); },
};
</script>
