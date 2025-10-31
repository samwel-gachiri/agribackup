<template>
  <Default>
    <div class="tw-p-6">
      <div class="tw-flex tw-flex-row tw-justify-between">
        <h2 class="tw-text-xl tw-font-bold tw-mb-4">User Sign-in Report</h2>
        <v-btn
            @click="fetchData"
            color="primary"
            class="tw-border-4"
            outlined
        >
          <v-icon color="primary">mdi-reload</v-icon>
          Reload</v-btn>
      </div>
      <div class="tw-grid tw-grid-cols-2 tw-gap-4 tw-mb-6">
        <v-card class="tw-p-4 tw-bg-green-100 tw-shadow-lg">
          <v-card-title class="tw-text-lg tw-font-semibold">ACTIVE Farmers</v-card-title>
          <v-card-text>
            <h2 class="tw-text-3xl tw-font-bold">{{ totalFarmers }}</h2>
          </v-card-text>
        </v-card>
        <v-card class="tw-p-4 tw-bg-blue-100 tw-shadow-lg">
          <v-card-title class="tw-text-lg tw-font-semibold">ACTIVE Buyers</v-card-title>
          <v-card-text>
            <h2 class="tw-text-3xl tw-font-bold">{{ totalBuyers }}</h2>
          </v-card-text>
        </v-card>
      </div>
      <div class="tw-p-4 tw-bg-white tw-shadow-lg tw-rounded-lg">
        <h2 class="tw-text-xl tw-font-bold tw-text-gray-700 tw-mb-4">Daily Sign-Ins (Farmers & Buyers)</h2>
        <apexchart
            type="line"
            height="350"
            :options="chartOptions"
            :series="series">
        </apexchart>
      </div>
    </div>
  </Default>
</template>

<script>
import axios from 'axios';
import VueApexCharts from 'vue-apexcharts';
import Default from '@/components/layout/Default.vue';

export default {
  components: {
    Default,
    apexchart: VueApexCharts,
  },
  data() {
    return {
      totalFarmers: 0,
      totalBuyers: 0,
      series: [],
      chartOptions: {
        chart: {
          type: 'area',
          toolbar: { show: true },
        },
        xaxis: {
          type: 'datetime',
          labels: {
            format: 'dd MMM yyyy', // Format date
            rotate: -45, // Rotate labels for readability
            datetimeFormatter: {
              year: 'yyyy',
              month: "MMM 'yy",
              day: 'dd MMM',
            },
          },
        },
        yaxis: {
          title: { text: 'Sign-Ins' },
          min: 0,
        },
        stroke: {
          curve: 'smooth',
          width: 3,
        },
        markers: {
          size: 4,
        },
        colors: ['#4CAF50', '#2196F3'], // Green for farmers, Blue for buyers
        legend: {
          position: 'top',
        },
        tooltip: {
          x: { format: 'yyyy-MM-dd' },
        },
      },
    };
  },
  async mounted() {
    await this.fetchData();
  },
  methods: {
    async fetchData() {
      try {
        const [signInResponse, usersResponse] = await Promise.all([
          axios.get('/api/admin/daily-signins'),
          axios.get('/api/admin/total-users'),
        ]);

        // Ensure total users are set
        this.totalFarmers = usersResponse.data.totalFarmers;
        this.totalBuyers = usersResponse.data.totalBuyers;

        // Ensure the response contains the expected array
        const data = signInResponse.data;
        if (!Array.isArray(data)) {
          throw new Error('Invalid API response: Expected an array');
        }
        const dates = data.map((item) => item.date);
        const farmersSignIn = data.map((item) => item.farmersSignedIn);
        const buyersSignIn = data.map((item) => item.buyersSignedIn);

        this.series = [
          { name: 'Farmers Signed In', data: dates.map((d, i) => [new Date(d).getTime(), farmersSignIn[i]]) },
          { name: 'Buyers Signed In', data: dates.map((d, i) => [new Date(d).getTime(), buyersSignIn[i]]) },
        ];
      } catch (error) {
        this.$toast.error('Error fetching sign-in report:', error.message);
      }
    },
  },
};
</script>

<style scoped>
.grid {
  display: grid;
}
</style>
