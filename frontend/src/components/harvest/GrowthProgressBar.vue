<template>
  <div class="growth-progress-bar flex items-center gap-2 w-full">
    <div class="relative w-full h-4 bg-gray-200 rounded overflow-hidden">
      <div class="absolute left-0 top-0 h-4 rounded bg-green-500 transition-all" :style="{ width: progress + '%' }"></div>
    </div>
    <span class="text-xs font-mono ml-2">{{ progress }}%</span>
  </div>
</template>

<script>
export default {
  name: 'GrowthProgressBar',
  props: {
    progress: {
      type: Number,
      required: true,
      default: 0,
    },
    daysToHarvest: {
      type: Number,
      default: null,
    },
  },
};
</script>

<style scoped>
.growth-progress-bar {
  min-width: 120px;
  max-width: 220px;
}
</style>
