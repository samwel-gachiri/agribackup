<template>
  <div class="harvest-status-card flex flex-col gap-2 p-4 rounded shadow border" :class="statusColorClass">
    <div class="flex items-center gap-2">
      <span class="font-bold text-lg">{{ produceName }}</span>
      <span class="badge" :class="statusColorClass">{{ statusLabel }}</span>
    </div>
    <div class="flex items-center gap-2">
      <span class="text-xs text-gray-500">Planted: {{ formatDate(plantingDate) }}</span>
      <span v-if="predictedHarvestDate" class="text-xs text-blue-700">Predicted Harvest: {{ formatDate(predictedHarvestDate) }}</span>
    </div>
    <div class="flex items-center gap-2">
      <GrowthProgressBar :progress="growthProgress" :daysToHarvest="daysToHarvest" />
      <span v-if="daysToHarvest !== null" class="text-xs text-gray-600">{{ daysToHarvest }} days left</span>
    </div>
    <div class="flex items-center gap-2">
      <span class="text-xs">Total Yields: <b>{{ totalYields }}</b></span>
      <span class="text-xs">Total Yield: <b>{{ totalYieldAmount }}</b></span>
      <span v-if="lastHarvestDate" class="text-xs">Last Harvest: {{ formatDate(lastHarvestDate) }}</span>
    </div>
  </div>
</template>

<script>
import GrowthProgressBar from './GrowthProgressBar.vue';

export default {
  name: 'HarvestStatusCard',
  components: { GrowthProgressBar },
  props: {
    produceName: String,
    status: String,
    plantingDate: String,
    predictedHarvestDate: String,
    daysToHarvest: Number,
    growthProgress: Number,
    totalYields: Number,
    totalYieldAmount: [Number, String],
    lastHarvestDate: String,
  },
  computed: {
    statusLabel() {
      switch ((this.status || '').toUpperCase()) {
        case 'PLANTED': return 'Planted';
        case 'GROWING': return 'Growing';
        case 'READY_TO_HARVEST': return 'Ready';
        case 'HARVESTED': return 'Harvested';
        case 'COMPLETED': return 'Completed';
        default: return this.status;
      }
    },
    statusColorClass() {
      switch ((this.status || '').toUpperCase()) {
        case 'PLANTED': return 'border-yellow-400 bg-yellow-50';
        case 'GROWING': return 'border-blue-400 bg-blue-50';
        case 'READY_TO_HARVEST': return 'border-green-500 bg-green-50';
        case 'HARVESTED': return 'border-purple-400 bg-purple-50';
        case 'COMPLETED': return 'border-gray-400 bg-gray-50';
        default: return 'border-gray-200 bg-white';
      }
    },
  },
  methods: {
    formatDate(date) {
      if (!date) return '-';
      try { return new Date(date).toLocaleDateString(); } catch { return String(date); }
    },
  },
};
</script>

<style scoped>
.harvest-status-card {
  min-width: 260px;
  max-width: 400px;
}
.badge {
  font-size: 0.8rem;
  padding: 0.15rem 0.7rem;
  border-radius: 9999px;
  font-weight: 600;
  background: #f3f4f6;
}
</style>
