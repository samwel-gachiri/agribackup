<template>
  <div class="yield-chart bg-white p-4 rounded shadow">
    <apexchart type="bar" height="300" :options="options" :series="series" />
  </div>
</template>

<script>
import VueApexCharts from 'vue-apexcharts';

export default {
  name: 'YieldChart',
  components: { apexchart: VueApexCharts },
  props: {
    yieldsByMonth: {
      type: Object,
      required: true,
    },
  },
  computed: {
    options() {
      return {
        chart: { id: 'yield-bar' },
        xaxis: { categories: Object.keys(this.yieldsByMonth) },
        title: { text: 'Yield by Month' },
      };
    },
    series() {
      return [
        {
          name: 'Yield',
          data: Object.values(this.yieldsByMonth),
        },
      ];
    },
  },
};
</script>

<style scoped>
.yield-chart {
  min-width: 320px;
  max-width: 600px;
}
</style>
