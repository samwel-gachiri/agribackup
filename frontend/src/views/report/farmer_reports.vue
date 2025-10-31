<template>
  <Default>
    <v-container class="tw-bg-gray-50 tw-min-h-screen tw-p-8">
      <!-- Reports Header -->
<!--      <v-row class="tw-mb-8">-->
<!--        <v-col cols="12">-->
<!--          <h1 class="tw-text-3xl tw-font-bold tw-text-gray-800">Farmer Reports</h1>-->
<!--          <p class="tw-text-gray-600">Gain insights into your farming operations.</p>-->
<!--        </v-col>-->
<!--      </v-row>-->

      <!-- Filter and Parameter Controls -->
      <v-row class="tw-mb-8">
        <v-col cols="12" md="4">
          <v-select
              v-model="selectedReportType"
              :items="reportTypes"
              label="Select Report Type"
              outlined
              dense
          ></v-select>
        </v-col>
        <v-col cols="12" md="4">
          <v-menu
              v-model="dateMenu"
              :close-on-content-click="false"
              transition="scale-transition"
              offset-y
              min-width="auto"
          >
            <template v-slot:activator="{ on, attrs }">
              <v-text-field
                  v-model="selectedDateRange"
                  label="Select Date Range"
                  readonly
                  outlined
                  dense
                  v-bind="attrs"
                  v-on="on"
              ></v-text-field>
            </template>
            <v-date-picker
                v-model="selectedDateRange"
                range
                no-title
                :max="new Date().toISOString()"
                @input="dateMenu = !(selectedDateRange.length>1)"
                @change="generateReport"
            ></v-date-picker>
          </v-menu>
        </v-col>
        <v-col cols="12" md="4">
          <v-btn color="primary" class="tw-w-full"
                 :loading="isDownloading"
                 @click="downloadPdf"
                 :disabled="ordersHistory.length <= 0">
            Generate Report PDF
            <v-icon color="white">mdi-file-pdf</v-icon>
          </v-btn>
        </v-col>
      </v-row>

      <!-- Detailed Report with Chart -->
      <v-row class="tw-mb-8" v-if="showReport && (selectedReportType === 'all' || selectedReportType === 'detailed')">
        <v-col cols="12">
          <v-card class="tw-p-6 tw-rounded-lg tw-shadow-md">
            <h2 class="tw-text-xl tw-font-semibold tw-text-gray-800 tw-mb-4">
              Sales and Revenue
            </h2>
            <apexchart
                type="bar"
                height="350"
                :options="detailedChartOptions"
                :series="detailedChartSeries"
            ></apexchart>
          </v-card>
        </v-col>
      </v-row>

      <!-- Filtered Report with Chart -->
      <v-row class="tw-mb-8" v-if="showReport && (selectedReportType === 'all' || selectedReportType === 'filtered')">
        <v-col cols="12">
          <v-card class="tw-p-6 tw-rounded-lg tw-shadow-md">
            <h2 class="tw-text-xl tw-font-semibold tw-text-gray-800 tw-mb-4">
              Top performing crops
            </h2>
            <apexchart
                type="pie"
                height="350"
                :options="filteredChartOptions"
                :series="filteredChartSeries"
            ></apexchart>
          </v-card>
        </v-col>
      </v-row>

      <!-- Parameterized Report with Chart -->
      <v-row class="tw-mb-8" v-if="showReport && (selectedReportType === 'all' || selectedReportType === 'parameterized')">
        <v-col cols="12">
          <v-card class="tw-p-6 tw-rounded-lg tw-shadow-md">
            <h2 class="tw-text-xl tw-font-semibold tw-text-gray-800 tw-mb-4">
              Sales Over Time <span class="tw-text-blue-600 hover:tw-underline" @click="dateMenu=true">({{ selectedDateRange[0] +' to '+ selectedDateRange[1] }})</span>
            </h2><h2 class="tw-text-xl tw-font-semibold tw-text-gray-800 tw-mb-4">
              Total Revenue <span class="tw-text-green-600 hover:tw-underline">Ksh. {{this.ordersHistory.reduce((sum, item) => sum + item.revenue, 0)}}</span>
            </h2>
            <apexchart
                type="line"
                height="350"
                :options="parameterizedChartOptions"
                :series="parameterizedChartSeries"
            ></apexchart>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </Default>
</template>

<script>
import VueApexCharts from 'vue-apexcharts';
import axios from 'axios';
import Default from '@/components/layout/Default.vue';
import { getCurrentUserId } from '@/utils/roles.js';
import { formatToHumanWithTime } from '@/utils/time.js';

export default {
  components: {
    Default,
    apexchart: VueApexCharts,
  },
  data() {
    return {
      selectedReportType: 'all',
      reportTypes: ['all', 'detailed', 'filtered', 'parameterized'],
      selectedDateRange: [
        new Date(new Date().setDate(new Date().getDate() - 30)).toISOString().substr(0, 10),
        new Date().toISOString().substr(0, 10),
      ],
      dateMenu: false,
      showReport: false,
      isDownloading: false,
      groupedOrders: [
        {
          date: '2025-02-10T06:15:27.644Z',
          product: 'string',
          quantitySold: 0,
          currency: 'string',
          revenue: 0,
        },
      ],
      ordersHistory: [
        {
          date: '2025-02-10T06:15:27.644Z',
          product: 'string',
          quantitySold: 0,
          currency: 'string',
          revenue: 0,
        },
      ],
      // // Detailed Report Data
      // detailedReport: [
      //   {
      //     // eslint-disable-next-line sonarjs/no-duplicate-string
      //     product: 'Organic Tomatoes', quantitySold: 120, revenue: 600, buyerFeedback: 4,
      //   },
      //   {
      //     // eslint-disable-next-line sonarjs/no-duplicate-string
      //     product: 'Fresh Strawberries', quantitySold: 80, revenue: 400, buyerFeedback: 5,
      //   },
      //   {
      //     product: 'Free-Range Eggs', quantitySold: 200, revenue: 300, buyerFeedback: 3,
      //   },
      // ],

      // // Filtered Report Data
      // filteredReport: [
      //   { product: 'Organic Tomatoes', totalRevenue: 600, profitMargin: 25 },
      //   { product: 'Fresh Strawberries', totalRevenue: 400, profitMargin: 30 },
      // ],

      // // Parameterized Report Data
      // parameterizedReport: [
      //   {
      //     date: '2023-10-01', product: 'Organic Tomatoes', quantitySold: 50, revenue: 250,
      //   },
      //   {
      //     date: '2023-10-02', product: 'Fresh Strawberries', quantitySold: 30, revenue: 150,
      //   },
      //   {
      //     date: '2023-10-03', product: 'Free-Range Eggs', quantitySold: 100, revenue: 200,
      //   },
      // ],

      // Chart Options and Series
      detailedChartOptions: {
        chart: {
          type: 'bar',
        },
        xaxis: {
          title: {
            text: 'Products',
            style: {
              fontSize: '14px',
              fontWeight: 'bold',
            },
          },
          categories: [],
        },
        yaxis: {
          title: {
            text: 'Amount',
          },
          labels: {
            formatter(value) {
              return ` ${value}`;
            },
          },
        },
        title: {
          text: 'Product Sales and Revenue',
          align: 'center',
          style: {
            fontSize: '16px',
            fontWeight: 'bold',
          },
        },
      },
      detailedChartSeries: [
        {
          name: 'Quantity Sold',
          data: [],
        },
        {
          name: 'Revenue',
          data: [],
        },
      ],

      filteredChartOptions: {
        chart: {
          type: 'pie',
        },
        labels: [],
        title: {
          text: 'Top Performers by Revenue',
          align: 'center',
          style: {
            fontSize: '16px',
            fontWeight: 'bold',
          },
        },
      },
      filteredChartSeries: [],

      parameterizedChartOptions: {
        chart: {
          type: 'line',
        },
        xaxis: {
          title: {
            text: 'Date And Time Of Sales',
          },
          categories: [],
        },
        yaxis: {
          title: {
            text: 'Revenue Generated',
          },
          labels: {
            formatter(value) {
              return `Ksh ${value}`;
            },
          },
        },
        title: {
          text: 'Daily Sales Over Time',
          align: 'center',
          style: {
            fontSize: '16px',
            fontWeight: 'bold',
          },
        },
      },
      parameterizedChartSeries: [
        {
          name: 'Revenue ($)',
          data: [],
        },
      ],
    };
  },
  mounted() {
    this.generateReport();
  },
  methods: {
    async fetchReport() {
      this.loading = true;
      try {
        const startDateTime = new Date(this.selectedDateRange[0]);
        startDateTime.setHours(0, 0, 0, 0);
        const endDateTime = new Date(this.selectedDateRange.length > 1 ? this.selectedDateRange[1] : this.selectedDateRange[0]);
        endDateTime.setHours(23, 59, 59, 999);
        await axios.get('/farmers-service/api/reports/orders/grouped', {
          params: {
            farmerId: getCurrentUserId(), // Replace with actual farmer ID
            startDateTime: startDateTime.toISOString(),
            endDateTime: endDateTime.toISOString(),
          },
        }).then((response) => {
          this.groupedOrders = response.data.data;
        });
        await axios.get('/farmers-service/api/reports/orders/history', {
          params: {
            farmerId: getCurrentUserId(), // Replace with actual farmer ID
            startDateTime: startDateTime.toISOString(),
            endDateTime: endDateTime.toISOString(),
          },
        }).then((response) => {
          this.ordersHistory = response.data.data;
        });
      } catch (error) {
        this.$toast.error('Error fetching listings:', error.message);
      } finally {
        this.loading = false;
      }
    },
    async generateReport() {
      await this.fetchReport();
      this.showReport = true;

      // Update Detailed Chart Data
      const detailedChartCategories = this.groupedOrders.map((item) => item.product);
      const detailedChartSeriesData = [
        { name: 'Quantity Sold', data: this.groupedOrders.map((item) => item.quantitySold) },
        { name: 'Revenue (Ksh)', data: this.groupedOrders.map((item) => item.revenue) },
      ];

      this.detailedChartOptions.xaxis.categories = detailedChartCategories;
      this.detailedChartSeries = detailedChartSeriesData;

      // Update Filtered Chart Data
      this.filteredChartOptions.labels = this.groupedOrders.map((item) => item.product);
      this.filteredChartSeries = this.groupedOrders.map((item) => item.revenue);

      // Update Parameterized Chart Data
      const parameterizedChartCategories = this.ordersHistory.map((item) => formatToHumanWithTime(item.date));
      const parameterizedChartSeriesData = [
        { name: 'Revenue', data: this.ordersHistory.map((item) => item.revenue) },
      ];

      this.parameterizedChartOptions.xaxis.categories = parameterizedChartCategories;
      this.parameterizedChartSeries = parameterizedChartSeriesData;
    },
    async downloadPdf() {
      this.isDownloading = true;
      const startDateTime = new Date(this.selectedDateRange[0]);
      startDateTime.setHours(0, 0, 0, 0);
      const endDateTime = new Date(this.selectedDateRange.length > 1 ? this.selectedDateRange[1] : this.selectedDateRange[0]);
      endDateTime.setHours(23, 59, 59, 999);
      await axios.get('/farmers-service/api/reports/orders/pdf', {
        params: {
          farmerId: getCurrentUserId(), // Replace with actual farmer ID
          startDateTime: startDateTime.toISOString(),
          endDateTime: endDateTime.toISOString(),
        },
        responseType: 'arraybuffer',
      })
        .then((res) => {
          const contentDisposition = res.headers['content-disposition'];
          const fileNameRegex = /filename="(.+?)"/;
          const fileNameMatch = contentDisposition?.match(fileNameRegex);
          const fileName = fileNameMatch ? fileNameMatch[1] : 'REPORT.pdf';

          // Create a Blob from the byte array
          const blob = new Blob([res.data], { type: 'application/pdf' });
          const url = window.URL.createObjectURL(blob);

          // Create a temporary link to trigger the download
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', fileName);
          document.body.appendChild(link);
          link.click();

          // Cleanup
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
        })
        .catch((err) => {
          this.$toast.error('error', err.message);
        })
        // eslint-disable-next-line no-return-assign
        .finally(() => (this.isDownloading = false));
    },
  },
  computed: {
    getCurrentUserId,
  },
};
</script>

<style scoped>

</style>
