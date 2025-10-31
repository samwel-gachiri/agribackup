<template>
  <Default>
    <v-container>
      <v-card id="printSection">
        <v-card-title>Buyer Report</v-card-title>
        <v-card-text>
          <logo-title/>
          <v-row>
            <v-col cols="12" md="4">
              <v-text-field v-model="buyerId" label="Buyer ID" disabled></v-text-field>
            </v-col>
            <v-col cols="12" md="4">
              <v-menu v-model="startMenu" :close-on-content-click="false" transition="scale-transition" offset-y>
                <template v-slot:activator="{ on }">
                  <v-text-field v-model="startDate" label="Start Date" readonly v-on="on"></v-text-field>
                </template>
                <v-date-picker v-model="startDate" @input="startMenu = false"></v-date-picker>
              </v-menu>
            </v-col>
            <v-col cols="12" md="4">
              <v-menu v-model="endMenu" :close-on-content-click="false" transition="scale-transition" offset-y>
                <template v-slot:activator="{ on }">
                  <v-text-field v-model="endDate" label="End Date" readonly v-on="on"></v-text-field>
                </template>
                <v-date-picker v-model="endDate" @input="endMenu = false"></v-date-picker>
              </v-menu>
            </v-col>
          </v-row>
          <v-btn color="primary" @click="fetchReport">Get Report</v-btn>
          <v-data-table :headers="headers" :items="reportData" class="mt-4"></v-data-table>
          <apexchart type="bar" :options="chartOptions" :series="chartSeries"></apexchart>
        </v-card-text>
      </v-card>
    </v-container>
  </Default>
</template>

<script>
import VueApexCharts from 'vue-apexcharts';
import Default from '@/components/layout/Default.vue';
import { getCurrentUserId } from '@/utils/roles.js';
import LogoTitle from '@/components/shared/LogoText.vue';

export default {
  components: { LogoTitle, Default, apexchart: VueApexCharts },
  data() {
    return {
      buyerId: getCurrentUserId(),
      startDate: new Date(new Date().setDate(new Date().getDate() - 30)).toISOString().substr(0, 10),
      endDate: new Date().toISOString().substr(0, 10),
      startMenu: false,
      endMenu: false,
      reportData: [],
      headers: [
        { text: 'Farmer Name', value: 'farmerName' },
        { text: 'Produce', value: 'produceName' },
        { text: 'Total Spent (Ksh)', value: 'totalSpent' },
        { text: 'Farmer Interactions', value: 'farmerInteractions' },
        { text: 'Pending Orders', value: 'pendingOrders' },
        // { text: 'Accepted Orders', value: 'acceptedOrders' },
        // { text: 'Supplied Orders', value: 'suppliedOrders' },
      ],
      chartOptions: {
        chart: { type: 'bar' },
        xaxis: {
          type: 'category',
          categories: [],
          title: { text: 'Produce Name' }, // Label for the x-axis
        },
        yaxis: {
          title: { text: 'Total Spent (KSH)' }, // Label for the y-axis
        },
        dataLabels: {
          enabled: true,
          formatter: (val, { dataPointIndex }) => this.reportData[dataPointIndex]?.produceName || '',
        },
      },
      chartSeries: [{ name: 'Total Spent', data: [] }],
    };
  },
  watch: {
    startDate() {
      this.fetchReport();
    },
    endDate() {
      this.fetchReport();
    },
  },
  mounted() {
    this.fetchReport();
  },
  methods: {
    formatDateTime(date) {
      return date ? new Date(date).toISOString() : '';
    },
    fetchReport() {
      const formattedStartDate = this.formatDateTime(this.startDate);
      const formattedEndDate = this.formatDateTime(this.endDate);

      fetch(`/api/reports/buyer?buyerId=${this.buyerId}&startDate=${formattedStartDate}&endDate=${formattedEndDate}`)
        .then((res) => res.json())
        .then((data) => {
          this.reportData = data;
          this.chartOptions.xaxis.categories = data.map((item) => item.produceName);
          this.chartSeries[0].data = data.map((item) => item.totalSpent);
        })
        .catch((err) => this.$toast.error(err.message));
    },
    printReport() {
      const printContent = document.getElementById('printSection').innerHTML;
      const originalContent = document.body.innerHTML;

      document.body.innerHTML = printContent;
      window.print();
      document.body.innerHTML = originalContent;
      window.location.reload(); // Reload to restore Vue bindings
    },
  },

};
</script>
