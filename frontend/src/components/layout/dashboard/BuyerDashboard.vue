<template>
  <v-container class="tw-bg-gray-50">
    <v-dialog v-model="requestDialog" max-width="500px">
      <create-request/>
      <v-btn
          class="tw-bg-white"
          color="error"
          text
          @click="requestDialog = false"
      >
        Close
      </v-btn>
    </v-dialog>
    <!-- Dashboard Header -->
    <v-row class="tw-mb-3">
      <v-col cols="12">
        <h1 class="tw-text-3xl tw-font-bold tw-text-gray-800">Buyer Dashboard</h1>
        <p class="tw-text-gray-600">Welcome back, {{ user.fullName }}! Here's your overview.</p>
      </v-col>
    </v-row>

    <!-- Stats Cards -->
    <v-row class="tw-mb-8">
      <v-col cols="12" md="4">
        <v-card rounded="xl" class="tw-pl-4 tw-pt-2 tw-rounded-lg tw-shadow-md hover:shadow-lg transition-shadow">
          <h2 class="tw-text-xl font-semibold text-gray-800">Total Requests</h2>
          <p class="tw-text-3xl tw-font-bold tw-text-green-600">{{liveCount.activeRequests}}</p>
          <p class="tw-text-gray-500">Active requests</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card rounded="xl" class="tw-pl-4 tw-pt-2 tw-rounded-lg tw-shadow-md hover:shadow-lg transition-shadow">
          <h2 class="tw-text-xl tw-font-semibold tw-text-gray-800">Farmer Interactions</h2>
          <p class="tw-text-3xl tw-font-bold tw-text-blue-600">{{liveCount.buyersInteraction}}</p>
          <p class="tw-text-gray-500">This month</p>
        </v-card>
      </v-col>
      <v-col cols="12" md="4">
        <v-card rounded="xl" class="tw-pl-4 tw-pt-2 tw-rounded-lg tw-shadow-md hover:shadow-lg transition-shadow">
          <h2 class="tw-text-xl tw-font-semibold tw-text-gray-800">Expenditure</h2>
          <p class="tw-text-3xl tw-font-bold tw-text-purple-600">{{liveCount.revenue30Days.currency + liveCount.revenue30Days.price.toLocaleString()}}</p>
          <p class="tw-text-gray-500">Last 30 days</p>
        </v-card>
      </v-col>
    </v-row>
    <!-- Stats Cards -->
    <v-row class="tw-mb-8">
      <!--      <v-col cols="12" md="4">-->
      <!--        <v-card rounded="xl" class="tw-pl-4 tw-pt-2 tw-rounded-lg tw-shadow-md hover:shadow-lg transition-shadow">-->
      <!--          <h2 class="tw-text-xl font-semibold text-gray-800">Total Requests</h2>-->
      <!--          <p class="tw-text-3xl tw-font-bold tw-text-green-600">{{liveCount.activeRequests}}</p>-->
      <!--          <p class="tw-text-gray-500">Active requests</p>-->
      <!--        </v-card>-->
      <!--      </v-col>-->
      <!--      <v-col cols="12" md="4">-->
      <!--        <v-card rounded="xl" class="tw-pl-4 tw-pt-2 tw-rounded-lg tw-shadow-md hover:shadow-lg transition-shadow">-->
      <!--          <h2 class="tw-text-xl tw-font-semibold tw-text-gray-800">Farmer Interactions</h2>-->
      <!--          <p class="tw-text-3xl tw-font-bold tw-text-blue-600">{{liveCount.buyersInteraction}}</p>-->
      <!--          <p class="tw-text-gray-500">This month</p>-->
      <!--        </v-card>-->
      <!--      </v-col>-->
      <v-col cols="12">
        <v-card :loading="loading" rounded="xl" class="tw-pl-4 tw-pt-2 tw-rounded-lg tw-shadow-md hover:shadow-lg transition-shadow">
<!--          <apexchart-->
<!--              type="line"-->
<!--              height="350"-->
<!--              :options="revenueChartOptions"-->
<!--              :series="revenueChartOptions.series"-->
<!--          ></apexchart>-->
          <v-data-table :headers="headers" :items="orders" class="elevation-1">
            <template v-slot:item.dateCreated="{ item }">
              {{ item.dateCreated | formatToHumanWithTime }}
            </template>
            <template v-slot:item.totalAmount="{ item }">
              <h2>KSH {{ (item.pricePerUnit * item.quantity).toFixed(2) }}</h2>
            </template>
          </v-data-table>
        </v-card>
      </v-col>
    </v-row>
    <!-- Quick Actions -->
    <v-row>
      <v-col cols="12">
        <v-card class="tw-p-6 tw-rounded-lg tw-shadow-md">
          <h2 class="tw-text-xl tw-font-semibold tw-text-gray-800 tw-mb-4">Quick Actions</h2>
          <div class="tw-flex md:tw-flex-row tw-flex-col tw-gap-5">
            <v-btn color="primary" class="flex-1" @click="requestDialog = true">
              <v-icon left>mdi-plus</v-icon>
              Add New Request
            </v-btn>
          </div>
        </v-card>
      </v-col>
      <v-col cols="12">
        <BuyerHarvestPredictions />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { mapState } from 'vuex';
import axios from 'axios';
import { getCurrentUserId } from '@/utils/roles.js';
import CreateRequest from '@/components/request/CreateRequest.vue';
import { formatToHumanWithTime } from '@/utils/time.js';
// import VueApexCharts from 'vue-apexcharts';
import BuyerHarvestPredictions from '@/components/buyer/BuyerHarvestPredictions.vue';

export default {
  components: {
    CreateRequest,
    BuyerHarvestPredictions,
    // apexchart: VueApexCharts,
  },
  data() {
    return {
      orders: [],
      headers: [
        { text: 'Produce Name', value: 'produceName' },
        { text: 'Quantity', value: 'quantity' },
        { text: 'Date ordered', value: 'dateCreated' },
        { text: 'Status', value: 'status' },
        { text: 'Price per unit (KSH)', value: 'pricePerUnit' },
        { text: 'Total Amount', value: 'totalAmount' },
      ],
      loading: false,
      requestDialog: false,
      dialog: false,
      liveCount: {
        activeRequests: 3,
        buyersInteraction: 1,
        revenue30Days: {
          price: 100,
          currency: 'KSH',
        },
      },
      revenueChartOptions: {
        forecastDataPoints: {
          count: 0,
          fillOpacity: 0.5,
          strokeWidth: undefined,
          dashArray: 4,
        },
        series: [{
          name: 'Pineapple',
          type: 'bar',
          data: [44, 55, 41, 37, 22, 43, 21],
        }, {
          name: 'Sales made',
          type: 'line',
          data: [23, 42, 35, 27, 43, 22, 17],
        }, {
          name: 'Mango',
          type: 'bar',
          data: [53, 32, 33, 52, 13, 43, 32],
        }, {
          name: 'Orange',
          type: 'bar',
          data: [12, 17, 11, 9, 15, 11, 20],
        }, {
          name: 'Sukuma wiki',
          type: 'bar',
          data: [9, 7, 5, 8, 6, 9, 4],
        }, {
          name: 'Cabbage',
          type: 'bar',
          data: [25, 12, 19, 32, 25, 24, 10],
        }],
        chart: {
          height: 350,
          type: 'line',
        },
        stroke: {
          width: [0, 4],
        },
        title: {
          text: 'Expenditure Over Time',
          align: 'left',
          margin: 10,
          offsetX: 0,
          offsetY: 0,
          floating: false,
          style: {
            fontSize: '14px',
            fontWeight: 'bold',
            fontFamily: 'Trebuc',
            color: '#263238',
          },
        },
        dataLabels: {
          enabled: true,
          enabledOnSeries: [1],
        },
        labels: ['Jan 2001', 'Feb 2001', 'March 2001', 'April 2001', 'May 2001', 'Jun 2001', 'Jul 2001', 'Aug 2001', 'Sep 2001', '10 Jan 2001', '11 Jan 2001', '12 Jan 2001'],
        yaxis: [{
          title: {
            text: 'Expenditure Over Time',
          },
          labels: {
            formatter(value) {
              return `Ksh ${value}`;
            },
          },
        }, {
          opposite: true,
          title: {
            text: 'Sales made',
          },
        }],
      },
    };
  },
  computed: {
    ...mapState({
      user: (state) => state.auth.user,
    }),
  },
  mounted() {
    this.fetchLiveCount();
    this.fetchOrders();
  },
  methods: {
    formatToHumanWithTime,
    // fetchRequests() {
    //   axios.get(`/request/buyer?buyerId=${getCurrentUserId()}`)
    //     .then((response) => {
    //     })
    //     .catch((error) => {
    //     });
    // },
    // Fetch requests from the API
    async fetchOrders() {
      try {
        const response = await axios.get(`/farmers-service/listing-orders?buyerId=${getCurrentUserId()}`);
        if (response.data.success) {
          this.orders = response.data.data;
          this.setUpEventSources();
        }
      } catch (error) {
        this.$toast.error('Error fetching orders:', error);
      }
    },
    async fetchLiveCount() {
      this.loading = true;
      try {
        const response = await axios.get('/buyers-service/api/dashboard/live/count', {
          params: {
            buyerId: getCurrentUserId(),
          },
        });
        if (response.data.success === true) this.liveCount = response.data.data;
      } catch (error) {
        this.$toast.error('Error fetching live count', error.message);
      } finally {
        this.loading = false;
      }
    },
    getStatusColor(status) {
      const statusColors = {
        PENDING_ACCEPTANCE: 'orange',
        BOOKED_FOR_SUPPLY: 'blue',
        SUPPLIED: 'green',
      };
      return statusColors[status] || 'grey';
    },
  },
  watch: {
    requestDialog(newValue) {
      if (newValue !== true) {
        this.fetchLiveCount();
        this.fetchRequests();
      }
    },
  },
  filters: {
    formatToHumanWithTime,
  },
};
</script>

<style scoped>
/* Tailwind-like utility classes */
.bg-gray-50 {
  background-color: #f9fafb;
}
.min-h-screen {
  min-height: 100vh;
}
.p-8 {
  padding: 2rem;
}
.mb-8 {
  margin-bottom: 2rem;
}
.text-3xl {
  font-size: 1.875rem;
}
.font-bold {
  font-weight: 700;
}
.text-gray-800 {
  color: #1f2937;
}
.text-gray-600 {
  color: #4b5563;
}
.text-green-600 {
  color: #16a34a;
}
.text-blue-600 {
  color: #2563eb;
}
.text-purple-600 {
  color: #9333ea;
}
.rounded-lg {
  border-radius: 0.5rem;
}
.shadow-md {
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}
.hover\:shadow-lg:hover {
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}
.transition-shadow {
  transition: box-shadow 0.3s ease-in-out;
}
.flex {
  display: flex;
}
.space-x-4 > * + * {
  margin-left: 1rem;
}
.flex-1 {
  flex: 1;
}
</style>
