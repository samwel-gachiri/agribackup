<template>
  <Default>
    <div class="harvest-analytics-page">
      <h1 class="text-2xl font-bold mb-6">Harvest Analytics Dashboard</h1>
      <div v-if="loading" class="flex justify-center items-center h-40">
        <span>Loading analytics...</span>
      </div>
      <div v-else>
        <div v-if="!analytics">
          <div class="text-gray-500">No analytics data available.</div>
        </div>
        <div v-else>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            <div class="card p-4">
              <h2 class="font-semibold mb-2">Summary</h2>
              <ul>
                <li>Total Produces: <b>{{ analytics.totalProduces }}</b></li>
                <li>Total Yields: <b>{{ analytics.totalYields }}</b></li>
                <li>Total Yield Amount: <b>{{ analytics.totalYieldAmount }}</b></li>
                <li>Average Growth Days: <b>{{ analytics.averageGrowthDays }}</b></li>
                <li>Prediction Accuracy: <b>{{ analytics.predictionAccuracy }}%</b></li>
              </ul>
            </div>
            <div class="card p-4">
              <h2 class="font-semibold mb-2">Top Performing Produces</h2>
              <ul>
                <li v-for="produce in analytics.topPerformingProduces" :key="produce.produceName">
                  {{ produce.produceName }}: {{ produce.totalYield }}
                </li>
              </ul>
            </div>
          </div>
          <div class="mb-8">
            <h2 class="font-semibold mb-2">Yield Trends (Monthly)</h2>
            <apexchart v-if="yieldsByMonthOptions && yieldsByMonthSeries" type="bar" height="300" :options="yieldsByMonthOptions" :series="yieldsByMonthSeries" />
          </div>
          <div class="mb-8">
            <h2 class="font-semibold mb-2">Seasonal Trends</h2>
            <apexchart v-if="seasonalTrendsOptions && seasonalTrendsSeries" type="line" height="300" :options="seasonalTrendsOptions" :series="seasonalTrendsSeries" />
          </div>
          <div class="mb-8">
            <button class="btn btn-info" @click="exportReport">Export as CSV</button>
          </div>
        </div>
      </div>
    </div>
  </Default>
</template>

<script>
import axios from 'axios';
import VueApexCharts from 'vue-apexcharts';
import { getCurrentUserId } from '@/utils/roles.js';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'HarvestAnalytics',
  components: {
    apexchart: VueApexCharts,
    Default,
  },
  data() {
    return {
      loading: true,
      analytics: null,
      yieldsByMonthOptions: null,
      yieldsByMonthSeries: null,
      seasonalTrendsOptions: null,
      seasonalTrendsSeries: null,
    };
  },
  methods: {
    async fetchAnalytics() {
      this.loading = true;
      try {
        const farmerId = getCurrentUserId() || this.$route.params.farmerId;
        const { data } = await axios.get(`/api/harvest/analytics/farmer/${farmerId}`);
        this.analytics = data.data || null;
        this.prepareApexCharts();
      } catch {
        this.analytics = null;
      } finally {
        this.loading = false;
      }
    },
    prepareApexCharts() {
      // Yield by Month
      if (this.analytics?.yieldsByMonth) {
        this.yieldsByMonthOptions = {
          chart: { id: 'yieldsByMonth' },
          xaxis: { categories: Object.keys(this.analytics.yieldsByMonth) },
          title: { text: 'Yield by Month' },
        };
        this.yieldsByMonthSeries = [
          {
            name: 'Yield Amount',
            data: Object.values(this.analytics.yieldsByMonth),
          },
        ];
      }
      // Seasonal Trends
      if (this.analytics?.seasonalTrends) {
        this.seasonalTrendsOptions = {
          chart: { id: 'seasonalTrends' },
          xaxis: { categories: this.analytics.seasonalTrends.map((t) => t.seasonName) },
          title: { text: 'Seasonal Trends' },
        };
        this.seasonalTrendsSeries = [
          {
            name: 'Yield',
            data: this.analytics.seasonalTrends.map((t) => t.totalYield),
          },
        ];
      }
    },
    exportReport() {
      if (!this.analytics) return;
      // Simple CSV export for yields by month
      let csv = 'Month,Yield\n';
      // eslint-disable-next-line no-restricted-syntax
      for (const [month, yieldAmount] of Object.entries(this.analytics.yieldsByMonth || {})) {
        csv += `${month},${yieldAmount}\n`;
      }
      const blob = new Blob([csv], { type: 'text/csv' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'yield_report.csv';
      a.click();
      URL.revokeObjectURL(url);
    },
  },
  mounted() {
    this.fetchAnalytics();
  },
};
</script>

<style scoped>
.harvest-analytics-page {
  padding: 2rem;
}
.card {
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  background: #fff;
}
.btn {
  background: #06b6d4;
  color: #fff;
  border: none;
  border-radius: 0.375rem;
  padding: 0.5rem 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-info {
  background: #06b6d4;
}
</style>
