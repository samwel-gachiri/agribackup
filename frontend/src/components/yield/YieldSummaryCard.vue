<template>
  <div class="yield-summary-card bg-white p-4 rounded shadow flex flex-col gap-2">
    <div class="font-bold text-lg">Yield Summary</div>
    <div class="flex flex-wrap gap-4">
      <div class="flex flex-col items-center">
        <span class="text-2xl font-bold text-green-600">{{ totalYields }}</span>
        <span class="text-xs text-gray-500">Total Yields</span>
      </div>
      <div class="flex flex-col items-center">
        <span class="text-2xl font-bold text-blue-600">{{ totalYieldAmount }}</span>
        <span class="text-xs text-gray-500">Total Yield Amount</span>
      </div>
      <div class="flex flex-col items-center">
        <span class="text-2xl font-bold text-yellow-600">{{ averageYield }}</span>
        <span class="text-xs text-gray-500">Average per Harvest</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'YieldSummaryCard',
  props: {
    totalYields: Number,
    totalYieldAmount: [Number, String],
    averageYield: [Number, String],
  },
};
</script>

<style scoped>
.yield-summary-card {
  min-width: 220px;
  max-width: 400px;
}
</style>
