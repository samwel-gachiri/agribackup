<template>
  <Default>
    <div class="tw-flex tw-flex-row tw-justify-between">
      <h1 class="tw-text-2xl tw-font-bold tw-mb-4">Order Report</h1>
      <v-btn
          @click="fetchReportData"
          color="primary"
          class="tw-border-4"
          outlined
      >
        <v-icon color="primary">mdi-reload</v-icon>
        Reload</v-btn>
    </div>
    <!-- Charts -->
    <v-row>
      <v-col cols="12" md="8">
        <apexchart type="bar" :options="chartOptions" :series="chartSeries" height="350"></apexchart>
      </v-col>
    </v-row>

    <!-- Table -->
    <v-card class="tw-mt-6 tw-p-4 tw-shadow-lg">
      <v-card-title class="tw-text-lg tw-font-bold">Order Report Summary</v-card-title>
      <v-data-table :headers="headers" :items="[reportData]" class="elevation-1"></v-data-table>
    </v-card>
  </Default>
</template>

<script>
import VueApexCharts from 'vue-apexcharts';
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  components: {
    Default,
    apexchart: VueApexCharts,
  },
  data() {
    return {
      reportData: {},
      chartOptions: {
        chart: { type: 'bar' },
        xaxis: {
          categories: [
            'Total Listings', 'Total Orders', 'Pending Orders', 'Booked Orders',
            'Declined Orders', 'Supplied Orders', 'Supplied & Paid Orders',
          ],
        },
        colors: ['#1E40AF', '#DC2626', '#FBBF24', '#10B981', '#9333EA', '#3B82F6', '#D97706'],
        title: { text: 'Order Report Breakdown', align: 'center' },
      },
      chartSeries: [{
        name: 'Orders',
        data: [],
      }],
      headers: [
        { text: 'Total Listings', value: 'totalListings' },
        { text: 'Total Orders', value: 'totalOrders' },
        { text: 'Pending Orders', value: 'totalPendingOrders' },
        { text: 'Booked Orders', value: 'totalBookedOrders' },
        { text: 'Declined Orders', value: 'totalDeclinedOrders' },
        { text: 'Supplied Orders', value: 'totalSuppliedOrders' },
        { text: 'Supplied & Paid Orders', value: 'totalSuppliedAndPaidOrders' },
        { text: 'Total Transaction Amount (Ksh)', value: 'totalTransactionAmount', format: (val) => `$${val.toFixed(2)}` },
      ],
    };
  },
  methods: {
    async fetchReportData() {
      try {
        const response = await axios.get('/api/admin/order-report');
        this.reportData = response.data;
        this.chartSeries[0].data = [
          response.data.totalListings,
          response.data.totalOrders,
          response.data.totalPendingOrders,
          response.data.totalBookedOrders,
          response.data.totalDeclinedOrders,
          response.data.totalSuppliedOrders,
          response.data.totalSuppliedAndPaidOrders,
        ];
      } catch (error) {
        this.$toast.error('Error fetching order report:', error.message);
      }
    },
  },
  mounted() {
    this.fetchReportData();
  },
};
</script>

<style scoped>
</style>
