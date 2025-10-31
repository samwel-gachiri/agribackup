<template>
  <div class="tw-min-h-screen tw-bg-gradient-to-br tw-from-gray-50 tw-to-green-50 tw-p-6 tw-pb-16">
    <!-- Stats Overview -->
  <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-3 tw-gap-6 tw-mb-8">
      <!-- Active Listings -->
      <div class="tw-bg-green-50 tw-border tw-border-green-100 tw-rounded-xl tw-p-5 tw-shadow-sm hover:tw-shadow-md tw-transition">
        <div class="tw-flex tw-justify-between tw-items-center">
          <div>
            <p class="tw-text-green-700 tw-text-xs tw-font-medium">Active Listings</p>
            <h3 class="tw-text-2xl tw-font-bold tw-mt-1 tw-text-green-900">{{ liveCount.activeListings }}</h3>
          </div>
          <i class="mdi mdi-sprout tw-text-green-400 tw-text-2xl"></i>
        </div>
      </div>
      <!-- Buyer Interactions -->
      <div class="tw-bg-blue-50 tw-border tw-border-blue-100 tw-rounded-xl tw-p-5 tw-shadow-sm hover:tw-shadow-md tw-transition">
        <div class="tw-flex tw-justify-between tw-items-center">
          <div>
            <p class="tw-text-blue-700 tw-text-xs tw-font-medium">Buyer Interactions</p>
            <h3 class="tw-text-2xl tw-font-bold tw-mt-1 tw-text-blue-900">{{ liveCount.buyersInteraction }}</h3>
          </div>
          <i class="mdi mdi-account-group tw-text-blue-400 tw-text-2xl"></i>
        </div>
      </div>
      <!-- 30-Day Revenue -->
      <div class="tw-bg-purple-50 tw-border tw-border-purple-100 tw-rounded-xl tw-p-5 tw-shadow-sm hover:tw-shadow-md tw-transition">
        <div class="tw-flex tw-justify-between tw-items-center">
          <div>
            <p class="tw-text-purple-700 tw-text-xs tw-font-medium">30-Day Revenue</p>
            <h3 class="tw-text-2xl tw-font-bold tw-mt-1 tw-text-purple-900">{{ liveCount.revenue30Days.currency }} {{ liveCount.revenue30Days.price }}</h3>
          </div>
          <i class="mdi mdi-cash-multiple tw-text-purple-400 tw-text-2xl"></i>
        </div>
      </div>

      <!-- Active Crops -->
      <div class="tw-bg-emerald-50 tw-border tw-border-emerald-100 tw-rounded-xl tw-p-5 tw-shadow-sm hover:tw-shadow-md tw-transition">
        <div class="tw-flex tw-justify-between tw-items-center">
          <div>
            <p class="tw-text-emerald-700 tw-text-xs tw-font-medium">Active Crops</p>
            <h3 class="tw-text-2xl tw-font-bold tw-mt-1 tw-text-emerald-900">{{ liveCount.activeCrops || 0 }}</h3>
          </div>
          <i class="mdi mdi-leaf tw-text-emerald-400 tw-text-2xl"></i>
        </div>
      </div>

      <!-- Upcoming Harvests (60 days) -->
      <div class="tw-bg-amber-50 tw-border tw-border-amber-100 tw-rounded-xl tw-p-5 tw-shadow-sm hover:tw-shadow-md tw-transition">
        <div class="tw-flex tw-justify-between tw-items-center">
          <div>
            <p class="tw-text-amber-700 tw-text-xs tw-font-medium">Upcoming Harvests (60d)</p>
            <h3 class="tw-text-2xl tw-font-bold tw-mt-1 tw-text-amber-900">{{ liveCount.upcomingHarvestsCount || 0 }}</h3>
          </div>
          <i class="mdi mdi-calendar-month tw-text-amber-400 tw-text-2xl"></i>
        </div>
      </div>

      <!-- Advertise Section -->
      <div class="tw-bg-gradient-to-r tw-from-indigo-400 tw-to-purple-400 tw-rounded-xl tw-shadow-sm tw-overflow-hidden hover:tw-shadow-md tw-transition md:tw-col-span-3">
        <div class="tw-p-5 tw-text-white">
          <h2 class="tw-text-lg tw-font-semibold tw-mb-2">Boost Sales</h2>
          <p class="tw-mb-3 tw-text-xs">Promote your listings for more visibility.</p>
          <button
            @click="goToListings"
            class="tw-bg-white tw-text-indigo-600 tw-px-4 tw-py-2 tw-rounded-lg tw-font-medium tw-text-xs hover:tw-bg-gray-100 tw-transition"
          >
            Advertise
          </button>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="tw-grid tw-grid-cols-1 lg:tw-grid-cols-3 tw-gap-8">
      <!-- Left Column -->
      <div class="lg:tw-col-span-2 tw-space-y-8">
        <!-- Sales Chart -->
        <div class="tw-bg-white tw-border tw-border-gray-100 tw-rounded-xl tw-p-6 tw-shadow-sm hover:tw-shadow-md tw-transition">
          <div class="tw-flex tw-justify-between tw-items-center tw-mb-4">
            <h2 class="tw-text-lg tw-font-semibold tw-text-gray-800">Sales Analytics</h2>
            <div class="tw-flex tw-gap-2">
              <button class="tw-px-3 tw-py-1 tw-bg-green-100 tw-text-green-700 tw-rounded-lg tw-text-xs hover:tw-bg-green-200">Monthly</button>
              <button class="tw-px-3 tw-py-1 tw-bg-gray-100 tw-text-gray-500 tw-rounded-lg tw-text-xs hover:tw-bg-gray-200">Yearly</button>
            </div>
          </div>
          <div class="tw-h-64">
            <apexchart
              type="bar"
              height="100%"
              :options="chartOptions"
              :series="series"
            ></apexchart>
          </div>
        </div>

        <!-- Recent Listings -->
        <div class="tw-bg-white tw-border tw-border-gray-100 tw-rounded-xl tw-shadow-sm tw-overflow-hidden hover:tw-shadow-md tw-transition">
          <div class="tw-p-6 tw-pb-0">
            <div class="tw-flex tw-justify-between tw-items-center">
              <h2 class="tw-text-lg tw-font-semibold tw-text-gray-800">Recent Listings</h2>
              <button
                @click="fetchListings"
                class="tw-text-green-600 hover:tw-text-green-700 tw-flex tw-items-center tw-gap-1 tw-text-xs"
              >
                <i class="mdi mdi-refresh"></i>
                <span>Refresh</span>
              </button>
            </div>
          </div>
          <div class="tw-overflow-x-auto">
            <table class="tw-min-w-full tw-divide-y tw-divide-gray-100">
              <thead class="tw-bg-gray-50">
                <tr>
                  <th
                    v-for="header in headers"
                    :key="header.text"
                    class="tw-px-4 tw-py-2 tw-text-left tw-text-xs tw-font-medium tw-text-gray-400 tw-uppercase"
                  >
                    {{ header.text }}
                  </th>
                </tr>
              </thead>
              <tbody class="tw-bg-white tw-divide-y tw-divide-gray-100">
                <tr v-for="(item, index) in listings" :key="index" class="hover:tw-bg-gray-50">
                  <td class="tw-px-4 tw-py-3 tw-whitespace-nowrap">
                    <div class="tw-flex tw-items-center">
                      <div class="tw-flex-shrink-0 tw-h-8 tw-w-8">
                        <img
                          v-if="item.farmerProduce?.farmProduce?.photo"
                          :src="item.farmerProduce.farmProduce.photo"
                          class="tw-h-8 tw-w-8 tw-rounded-full tw-border tw-border-gray-200"
                        >
                        <div v-else class="tw-h-8 tw-w-8 tw-rounded-full tw-bg-gray-100 tw-flex tw-items-center tw-justify-center">
                          <i class="mdi mdi-leaf tw-text-gray-300"></i>
                        </div>
                      </div>
                      <div class="tw-ml-3">
                        <div class="tw-text-xs tw-font-medium tw-text-gray-900">
                          {{ item.farmerProduce?.farmProduce?.name || 'N/A' }}
                        </div>
                        <div class="tw-text-xs tw-text-gray-400">
                          {{ item.status }}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td class="tw-px-4 tw-py-3 tw-whitespace-nowrap">
                    <div class="tw-text-xs tw-text-gray-900">{{ item.quantityWithUnit }}</div>
                  </td>
                  <td class="tw-px-4 tw-py-3 tw-whitespace-nowrap">
                    <div class="tw-text-xs tw-text-gray-900">{{ item.priceWithCurrency }}</div>
                  </td>
                  <td class="tw-px-4 tw-py-3 tw-whitespace-nowrap">
                    <div class="tw-text-xs tw-text-gray-900">{{ formatToHumanWithTime(item.createdAt) }}</div>
                  </td>
                  <td class="tw-px-4 tw-py-3 tw-whitespace-nowrap">
                    <span
                      :class="{
                        'tw-bg-green-100 tw-text-green-700': item.status === 'ACTIVE',
                        'tw-bg-yellow-100 tw-text-yellow-700': item.status === 'PENDING',
                        'tw-bg-red-100 tw-text-red-700': item.status === 'REJECTED',
                        'tw-bg-blue-100 tw-text-blue-700': item.status === 'SOLD'
                      }"
                      class="tw-px-2 tw-py-1 tw-text-xs tw-font-medium tw-rounded-lg"
                    >
                      {{ item.status }}
                    </span>
                  </td>
                  <td class="tw-px-4 tw-py-3 tw-whitespace-nowrap tw-text-xs tw-text-gray-900">
                    {{ item.totalPrice }}
                  </td>
                </tr>
                <tr v-if="listings.length === 0">
                  <td colspan="6" class="tw-px-4 tw-py-3 tw-text-center tw-text-gray-400 tw-text-xs">
                    No listings found. Create your first one!
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="tw-px-6 tw-py-4 tw-bg-gray-50 tw-flex tw-items-center tw-justify-between tw-border-t tw-border-gray-100">
            <div class="tw-text-xs tw-text-gray-400">
              Showing <span class="tw-font-medium">{{ listings.length }}</span> of <span class="tw-font-medium">{{ totalElements }}</span>
            </div>
            <div class="tw-flex tw-gap-2">
              <button
                @click="page = Math.max(0, page - 1)"
                :disabled="page === 0"
                class="tw-px-3 tw-py-1 tw-border tw-border-gray-200 tw-rounded-lg tw-text-xs tw-bg-white hover:tw-bg-gray-100 disabled:tw-opacity-50"
              >
                Prev
              </button>
              <button
                @click="page = page + 1"
                :disabled="page >= totalPages - 1"
                class="tw-px-3 tw-py-1 tw-border tw-border-gray-200 tw-rounded-lg tw-text-xs tw-bg-white hover:tw-bg-gray-100 disabled:tw-opacity-50"
              >
                Next
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Right Column -->
      <div class="tw-space-y-8">
        <!-- Quick Actions -->
        <div class="tw-bg-white tw-border tw-border-gray-100 tw-rounded-xl tw-shadow-sm tw-overflow-hidden hover:tw-shadow-md tw-transition">
          <div class="tw-p-6">
            <h2 class="tw-text-lg tw-font-semibold tw-text-gray-800 tw-mb-4">Quick Actions</h2>
            <div class="tw-space-y-2">
              <div
                v-for="(link, index) in quickLinks"
                :key="index"
                @click="quickLinkClicked(link.link)"
                class="tw-p-3 tw-rounded-lg tw-cursor-pointer tw-transition hover:tw-bg-gray-100"
              >
                <div class="tw-flex tw-items-center tw-gap-3">
                  <i
                    :class="link.icon"
                    class="tw-text-xl"
                    :style="{ color: link.iconColor }"
                  ></i>
                  <div>
                    <h3 class="tw-font-medium tw-text-gray-800 tw-text-sm">{{ link.title }}</h3>
                    <p class="tw-text-xs tw-text-gray-400">{{ link.text }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Daily Insight (AI) & Market Tips -->
        <div class="tw-bg-white tw-border tw-border-gray-100 tw-rounded-xl tw-shadow-sm tw-overflow-hidden hover:tw-shadow-md tw-transition">
          <div class="tw-p-6 tw-space-y-4">
            <div>
              <h2 class="tw-text-lg tw-font-semibold tw-text-gray-800 tw-mb-2">Daily Insight (AI)</h2>
              <p class="tw-text-xs tw-text-gray-600" v-if="liveCount.dailyInsight && liveCount.dailyInsight.length > 0">
                {{ liveCount.dailyInsight }}
              </p>
              <p class="tw-text-xs tw-text-gray-400" v-else>
                Insight will appear here based on your active crops.
              </p>
            </div>
            <div>
              <h3 class="tw-text-sm tw-font-semibold tw-text-gray-800 tw-mb-2">Market Tips</h3>
              <ul class="tw-space-y-2">
                <li v-for="(tip, i) in (liveCount.marketTips || [])" :key="i" class="tw-text-xs tw-text-gray-600 tw-flex tw-gap-2">
                  <i class="mdi mdi-lightbulb-on-outline tw-text-yellow-500 tw-mt-0.5"></i>
                  <span>{{ tip }}</span>
                </li>
                <li v-if="!liveCount.marketTips || liveCount.marketTips.length === 0" class="tw-text-xs tw-text-gray-400">
                  No tips yet. Keep your listings updated to get better insights.
                </li>
              </ul>
            </div>
          </div>
        </div>
  <!-- Harvest Predictions Panel -->
  <FarmerHarvestPanel class="tw-mt-4" />
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import axios from 'axios';
import pluralize from 'pluralize';
import VueApexCharts from 'vue-apexcharts';
import { getCurrentUserId } from '@/utils/roles.js';
import { formatToHumanWithTime } from '@/utils/time.js';
import FarmerHarvestPanel from '@/components/farmer/FarmerHarvestPanel.vue';

export default {
  components: {
    apexchart: VueApexCharts,
    FarmerHarvestPanel,
  },
  data() {
    return {
      loading: false,
      listingDialog: false,
      headers: [
        { text: 'Product', value: 'farmerProduce.farmProduce.name' },
        { text: 'Quantity', value: 'quantityWithUnit' },
        { text: 'Price', value: 'priceWithCurrency' },
        { text: 'Created At', value: 'createdAt' },
        { text: 'Status', value: 'status' },
        { text: 'Total Price', value: 'totalPrice' },
      ],
      listings: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      page: 0,
      dialog: false,
      liveCount: {
        activeListings: 0,
        buyersInteraction: 0,
        revenue30Days: {
          price: 0,
          currency: 'KSH',
        },
        activeCrops: 0,
        upcomingHarvestsCount: 0,
        marketTips: [],
        dailyInsight: '',
      },
      quickLinks: [
        {
          title: 'Farm AI',
          text: 'Get Advice on Farming Practices',
          icon: 'mdi-robot',
          iconColor: '#10B981',
          link: {
            name: 'FarmAI',
          },
        },
        {
          title: 'Community',
          text: 'View farmers and buyers near you!',
          icon: 'mdi-account-group',
          iconColor: '#3B82F6',
          link: {
            name: 'Community',
          },
        },
        {
          title: 'Sales',
          text: 'Sell your produce, view what you sold',
          icon: 'mdi-cash-multiple',
          iconColor: '#8B5CF6',
          link: {
            name: 'Listings',
          },
        },
      ],
      chartOptions: {
        chart: {
          type: 'bar',
          height: 350,
          stacked: false,
          toolbar: { show: false },
          fontFamily: 'Inter, sans-serif',
          foreColor: '#6B7280',
        },
        plotOptions: {
          bar: {
            columnWidth: '45%',
            borderRadius: 4,
          },
        },
        dataLabels: {
          enabled: false,
        },
        stroke: {
          width: [0, 2],
          curve: 'smooth',
        },
        xaxis: {
          categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul'],
          axisTicks: {
            show: false,
          },
          axisBorder: {
            show: false,
          },
        },
        yaxis: [
          {
            title: {
              text: 'Revenue (KSH)',
              style: {
                color: '#10B981',
              },
            },
            labels: {
              formatter: (value) => `KSH ${value.toLocaleString()}`,
              style: {
                colors: '#10B981',
              },
            },
          },
          {
            opposite: true,
            title: {
              text: 'Quantity (kg)',
              style: {
                color: '#3B82F6',
              },
            },
            labels: {
              formatter: (value) => `${value.toLocaleString()} kg`,
              style: {
                colors: '#3B82F6',
              },
            },
          },
        ],
        fill: {
          opacity: 1,
          colors: ['#10B981'],
        },
        colors: ['#10B981', '#3B82F6'],
        tooltip: {
          shared: true,
          intersect: false,
          y: {
            formatter(y) {
              if (typeof y !== 'undefined') {
                return y.toFixed(0) + (this.seriesIndex === 0 ? ' KSH' : ' kg');
              }
              return y;
            },
          },
        },
        legend: {
          position: 'top',
          horizontalAlign: 'right',
          offsetY: 0,
          markers: {
            radius: 2,
          },
        },
        grid: {
          borderColor: '#F3F4F6',
          strokeDashArray: 2,
          padding: {
            top: 0,
            right: 20,
            bottom: 0,
            left: 20,
          },
        },
      },
      series: [
        {
          name: 'Revenue',
          type: 'column',
          data: [44000, 55000, 41000, 67000, 58000, 80000, 72000],
        },
        {
          name: 'Quantity Sold',
          type: 'line',
          data: [1200, 1500, 1100, 1700, 1500, 2000, 1900],
        },
      ],
    };
  },
  computed: {
    ...mapState({
      user: (state) => state.auth.user,
    }),
  },
  mounted() {
    this.fetchLiveCount();
    this.fetchListings();
    this.fetchSalesReport();
  },
  methods: {
    formatToHumanWithTime,
    quickLinkClicked(link) {
      this.$router.push(link);
    },
    goToListings() {
      this.$router.push({ name: 'Listings' });
    },
    async fetchSalesReport() {
      try {
        const response = await axios.get('/farmers-service/api/dashboard/sales-report', {
          params: { farmerId: getCurrentUserId() },
        });
        const rows = response.data || [];

        // Aggregate by month
        const revenueByMonth = new Map();
        const quantityByMonth = new Map();
        rows.forEach((row) => {
          const m = row.saleMonth;
          const rev = Number(row.totalRevenue || 0);
          const qty = Number(row.totalSold || 0);
          revenueByMonth.set(m, (revenueByMonth.get(m) || 0) + rev);
          quantityByMonth.set(m, (quantityByMonth.get(m) || 0) + qty);
        });

        const categories = Array.from(revenueByMonth.keys());
        const revenueSeries = categories.map((m) => revenueByMonth.get(m) || 0);
        const qtySeries = categories.map((m) => quantityByMonth.get(m) || 0);

        // Update chart reactivity-friendly
        this.chartOptions = {
          ...this.chartOptions,
          xaxis: { ...this.chartOptions.xaxis, categories },
        };
        this.series = [
          { name: 'Revenue', type: 'column', data: revenueSeries },
          { name: 'Quantity Sold', type: 'line', data: qtySeries },
        ];
      } catch (e) {
        this.$toast.error('Failed to load sales analytics');
      }
    },
    async fetchListings() {
      this.loading = true;
      try {
        const response = await axios.get('/farmers-service/listing/farmer', {
          params: {
            farmerId: getCurrentUserId(),
            page: this.page,
            size: this.size,
          },
        });
        const data = response.data.data;
        this.listings = data.content
          .map((item) => ({
            ...item,
            quantityWithUnit: `${item.quantity} ${item.quantity > 1 ? item.unit : pluralize(item.unit)}`,
            priceWithCurrency: `${item.price.currency} ${item.price.price} @ ${item.unit}`,
            totalPrice: `${item.price.currency} ${item.price.price * item.quantity}`,
          }));
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
      } catch (error) {
        this.$toast.error('Error fetching listings:', error.message);
      } finally {
        this.loading = false;
      }
    },
    async fetchLiveCount() {
      this.loading = true;
      try {
        const response = await axios.get('/farmers-service/api/dashboard/live/count', {
          params: {
            farmerId: getCurrentUserId(),
          },
        });
        if (response.data.success === true) this.liveCount = response.data.data;
      } catch (error) {
        this.$toast.error('Error fetching live count', error.message);
      } finally {
        this.loading = false;
      }
    },
  },
  watch: {
    listingDialog(newValue) {
      if (newValue !== true) {
        this.fetchLiveCount();
        this.fetchListings();
      }
    },
  },
};
</script>

<style>
/* Custom scrollbar */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}
::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}
::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

/* Smooth transitions */
.tw-transition-all {
  transition-property: all;
  transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
  transition-duration: 300ms;
}

/* Gradient icon for Farm AI */
.gradient-icon {
  background: linear-gradient(135deg, #10B981 0%, #3B82F6 100%);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
</style>
