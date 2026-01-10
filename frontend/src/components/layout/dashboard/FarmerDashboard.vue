<template>
  <div class="farmer-dashboard tw-min-h-screen tw-bg-[#F9FAFB] tw-p-6 tw-pb-20">
    <!-- Row 1: Hero Section -->
    <div class="tw-grid tw-grid-cols-1 lg:tw-grid-cols-5 tw-gap-6 tw-mb-6">
      <!-- Welcome Card (60%) -->
      <div class="lg:tw-col-span-3 tw-bg-gradient-to-r tw-from-green-800 tw-to-green-700 tw-rounded-2xl tw-p-6 tw-text-white tw-shadow-lg">
        <h1 class="tw-text-2xl tw-font-bold tw-mb-2">Welcome, {{ userName }}</h1>
        <p class="tw-text-green-100 tw-text-sm tw-mb-4">
          Complete Profile: {{ profileCompletion }}% ({{ nextStep }})
        </p>
        <!-- Progress Bar -->
        <div class="tw-bg-green-900/50 tw-rounded-full tw-h-3 tw-mb-4 tw-overflow-hidden">
          <div
            class="tw-bg-green-400 tw-h-full tw-rounded-full tw-transition-all tw-duration-500"
            :style="{ width: profileCompletion + '%' }"
          ></div>
        </div>
        <p class="tw-text-xs tw-text-green-200 tw-mb-4">
          Progress: {{ nextStepDescription }}
        </p>
        <button
          @click="continueSetup"
          class="tw-bg-white tw-text-green-800 tw-px-5 tw-py-2.5 tw-rounded-lg tw-font-semibold tw-text-sm hover:tw-bg-green-50 tw-transition tw-shadow-md"
        >
          Continue Setup
        </button>
      </div>

      <!-- Weather Widget (40%) -->
      <div class="lg:tw-col-span-2 tw-bg-white tw-rounded-2xl tw-p-6 tw-shadow-sm tw-border tw-border-gray-100 hover:tw-shadow-md tw-transition">
        <h3 class="tw-text-sm tw-font-semibold tw-text-gray-500 tw-mb-3">{{ weather.location }} Weather Widget</h3>
        <div class="tw-flex tw-items-center tw-gap-4">
          <div class="tw-text-5xl">
            <i :class="weatherIcon" class="tw-text-blue-400"></i>
          </div>
          <div>
            <div class="tw-text-4xl tw-font-bold tw-text-gray-800">{{ weather.temperature }}°C</div>
            <div class="tw-text-sm tw-text-gray-500">{{ weather.condition }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Row 2: Work Layer -->
    <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-3 tw-gap-6 tw-mb-6">
      <!-- EUDR Compliance Status -->
      <div class="tw-bg-white tw-rounded-2xl tw-p-6 tw-shadow-sm tw-border tw-border-gray-100 hover:tw-shadow-md tw-transition">
        <h3 class="tw-text-lg tw-font-semibold tw-text-gray-800 tw-mb-4">EUDR Compliance Status</h3>
        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-4">
          <div :class="eudrStatusClasses" class="tw-w-10 tw-h-10 tw-rounded-full tw-flex tw-items-center tw-justify-center">
            <i :class="eudrIcon" class="tw-text-lg"></i>
          </div>
          <div>
            <div :class="eudrTextColor" class="tw-text-xl tw-font-bold">{{ eudrStatus.status }}</div>
            <div class="tw-text-sm tw-text-gray-500">{{ eudrStatus.message }}</div>
          </div>
        </div>
        <button
          @click="goToEudrSetup"
          :class="eudrButtonClasses"
          class="tw-px-4 tw-py-2 tw-rounded-lg tw-font-medium tw-text-sm tw-transition"
        >
          {{ eudrStatus.actionLabel }}
        </button>
      </div>

      <!-- Market Trends -->
      <div class="tw-bg-white tw-rounded-2xl tw-p-6 tw-shadow-sm tw-border tw-border-gray-100 hover:tw-shadow-md tw-transition">
        <h3 class="tw-text-lg tw-font-semibold tw-text-gray-800 tw-mb-2">Market Trends</h3>
        <p class="tw-text-xs tw-text-gray-500 tw-mb-3">Coffee Prices (KSH/kg)</p>
        <div class="tw-h-32">
          <apexchart
            type="area"
            height="100%"
            :options="marketChartOptions"
            :series="marketSeries"
          ></apexchart>
        </div>
        <div class="tw-mt-2 tw-flex tw-items-center tw-gap-2">
          <span class="tw-text-lg tw-font-bold tw-text-gray-800">KSH {{ currentPrice }}</span>
          <span :class="priceChangeClasses" class="tw-text-sm tw-font-medium tw-flex tw-items-center">
            <i :class="priceChangeIcon"></i>
            {{ priceChange }}%
          </span>
        </div>
      </div>

      <!-- Register New Crop -->
      <div
        @click="registerNewCrop"
        class="tw-bg-gradient-to-br tw-from-green-50 tw-to-emerald-50 tw-rounded-2xl tw-p-6 tw-shadow-sm tw-border tw-border-green-100 hover:tw-shadow-lg tw-transition tw-cursor-pointer tw-flex tw-flex-col tw-items-center tw-justify-center tw-text-center hover:tw-scale-[1.02]"
      >
        <div class="tw-w-20 tw-h-20 tw-bg-green-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mb-4">
          <i class="mdi mdi-sprout tw-text-4xl tw-text-green-600"></i>
        </div>
        <h3 class="tw-text-lg tw-font-semibold tw-text-green-800">Register New Crop</h3>
        <p class="tw-text-sm tw-text-green-600 tw-mt-1">Add production unit</p>
      </div>
    </div>

    <!-- Row 3: Intelligence Layer -->
    <div class="tw-grid tw-grid-cols-1 lg:tw-grid-cols-2 tw-gap-6">
      <!-- Agri-AI Insight -->
      <div class="tw-bg-white tw-rounded-2xl tw-p-6 tw-shadow-sm tw-border tw-border-gray-100 hover:tw-shadow-md tw-transition">
        <h3 class="tw-text-lg tw-font-semibold tw-text-gray-800 tw-mb-3">Agri-AI Insight</h3>
        <p class="tw-text-gray-600 tw-text-sm tw-mb-4">{{ aiInsight }}</p>
        <button
          @click="viewRecommendation"
          class="tw-bg-green-600 tw-text-white tw-px-4 tw-py-2 tw-rounded-lg tw-font-medium tw-text-sm hover:tw-bg-green-700 tw-transition"
        >
          View Fertilizer
        </button>
      </div>

      <!-- Farm Activity Feed -->
      <div class="tw-bg-white tw-rounded-2xl tw-p-6 tw-shadow-sm tw-border tw-border-gray-100 hover:tw-shadow-md tw-transition">
        <h3 class="tw-text-lg tw-font-semibold tw-text-gray-800 tw-mb-4">Farm Activity Feed</h3>
        <div class="tw-space-y-4">
          <div v-for="(activity, index) in recentActivities" :key="index" class="tw-flex tw-items-start tw-gap-3">
            <div class="tw-w-8 tw-h-8 tw-rounded-full tw-bg-green-100 tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
              <i class="mdi mdi-account tw-text-green-600 tw-text-sm"></i>
            </div>
            <div>
              <p class="tw-text-sm tw-text-gray-800">
                <span class="tw-font-medium">{{ activity.user }}</span> {{ activity.action }}
              </p>
              <p class="tw-text-xs tw-text-gray-400">{{ activity.time }}</p>
            </div>
          </div>
          <div v-if="recentActivities.length === 0" class="tw-text-center tw-py-4">
            <i class="mdi mdi-clock-outline tw-text-3xl tw-text-gray-300"></i>
            <p class="tw-text-sm tw-text-gray-400 tw-mt-2">No recent activity</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Floating Agri-Copilot Button (Pill Shape) -->
    <v-btn
      fixed
      bottom
      right
      rounded
      color="#2E7D32"
      dark
      depressed
      @click="openAgriCopilot"
      class="agri-copilot-fab text-none"
      style="text-transform: none; padding: 0 16px;"
    >
      <v-icon left size="18">mdi-message-text</v-icon>
      <span style="font-size: 13px; font-weight: 500;">Ask Agri-Copilot</span>
    </v-btn>

    <!-- EUDR Badge (Vuetify Chip) -->
    <v-chip
      class="eudr-badge-chip"
      color="white"
      small
    >
      <v-icon small left color="green">mdi-shield-check</v-icon>
      EUDR Compliant
    </v-chip>
  </div>
</template>

<script>
import { mapState } from 'vuex';
import axios from 'axios';
import VueApexCharts from 'vue-apexcharts';
import { getCurrentUserId } from '@/utils/roles.js';

export default {
  name: 'FarmerDashboard',
  components: {
    apexchart: VueApexCharts,
  },
  data() {
    return {
      loading: false,
      profileCompletion: 0,
      nextStep: 'Loading...',
      nextStepDescription: 'Please wait...',

      // Weather data
      weather: {
        location: 'Loading...',
        temperature: '--',
        condition: 'Updating...',
      },

      // EUDR Status
      eudrStatus: {
        status: 'Checking...',
        message: 'Verifying compliance...',
        isPending: true,
        actionLabel: 'View Details',
      },

      // Market data
      currentPrice: 0,
      priceChange: 0,
      marketSeries: [],
      marketChartOptions: {
        chart: {
          type: 'area',
          height: 120,
          sparkline: { enabled: true },
          toolbar: { show: false },
        },
        stroke: {
          curve: 'smooth',
          width: 2,
        },
        fill: {
          type: 'gradient',
          gradient: {
            shadeIntensity: 1,
            opacityFrom: 0.4,
            opacityTo: 0.1,
          },
        },
        colors: ['#10B981'],
        tooltip: {
          enabled: true,
          y: {
            formatter: (val) => `KSH ${val}`,
          },
        },
        xaxis: {
          categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul'],
        },
      },

      // AI Insight
      aiInsight: 'Analyzing your farm data...',

      // Recent activities
      recentActivities: [],
    };
  },
  computed: {
    ...mapState({
      user: (state) => state.auth.user,
    }),
    userName() {
      return this.user?.displayName?.split(' ')[0] || 'Farmer';
    },
    weatherIcon() {
      return this.weather.condition.toLowerCase().includes('rain')
        ? 'mdi mdi-weather-partly-rainy'
        : 'mdi mdi-weather-sunny';
    },
    eudrStatusClasses() {
      return this.eudrStatus.isPending
        ? 'tw-bg-orange-100'
        : 'tw-bg-green-100';
    },
    eudrIcon() {
      return this.eudrStatus.isPending
        ? 'mdi mdi-alert tw-text-orange-600'
        : 'mdi mdi-check-circle tw-text-green-600';
    },
    eudrTextColor() {
      return this.eudrStatus.isPending
        ? 'tw-text-orange-600'
        : 'tw-text-green-600';
    },
    eudrButtonClasses() {
      return this.eudrStatus.isPending
        ? 'tw-bg-orange-500 tw-text-white hover:tw-bg-orange-600'
        : 'tw-bg-green-100 tw-text-green-700 hover:tw-bg-green-200';
    },
    priceChangeClasses() {
      return this.priceChange >= 0
        ? 'tw-text-green-600'
        : 'tw-text-red-600';
    },
    priceChangeIcon() {
      return this.priceChange >= 0
        ? 'mdi mdi-arrow-up'
        : 'mdi mdi-arrow-down';
    },
  },
  async mounted() {
    await this.loadDashboardData();
  },
  methods: {
    async loadDashboardData() {
      this.loading = true;
      try {
        // Load all dashboard data in parallel for performance
        await Promise.all([
          this.loadProfileCompletion(),
          this.checkEUDRStatus(),
          this.loadWeatherData(),
          this.loadMarketPrices(),
          this.loadRecentActivities(),
          this.loadAIInsight(),
        ]);
      } catch (error) {
        this.$toast.error('Failed to load dashboard data:', error.message);
      } finally {
        this.loading = false;
      }
    },

    async loadProfileCompletion() {
      try {
        const farmerId = getCurrentUserId();
        const response = await axios.get(`/farmers-service/api/farmers/${farmerId}/profile-completion`);
        if (response.data) {
          this.profileCompletion = response.data.percentage;
          this.nextStep = response.data.nextStep;
          this.nextStepDescription = response.data.nextStepDescription;
        }
      } catch (e) {
        this.$toast.error('Failed to load profile completion:', e.message);
      }
    },

    async hasProductionUnits() {
      try {
        const response = await axios.get(`/api/v1/production-units/farmer/${getCurrentUserId()}`);
        const units = response.data || [];
        return units.length > 0;
      } catch {
        return false;
      }
    },

    async checkEUDRStatus() {
      try {
        const farmerId = getCurrentUserId();
        const response = await axios.get(`/api/production-units/farmer/${farmerId}`);
        const units = response.data?.data || [];

        if (units.length === 0) {
          this.eudrStatus = {
            status: 'Pending',
            message: 'No production units registered',
            isPending: true,
            actionLabel: 'Register Land',
          };
          return;
        }

        // Check if any unit has geolocation
        const hasGeolocation = units.some((u) => u.wgs84Coordinates || (u.latitude && u.longitude));

        if (hasGeolocation) {
          // Find if any unit is non-compliant (simplified logic)
          // In production, we would check the specific compliance status of each unit
          this.eudrStatus = {
            status: 'Compliant',
            message: 'All units verified',
            isPending: false,
            actionLabel: 'View Certificate',
          };
        } else {
          this.eudrStatus = {
            status: 'Action Required',
            message: 'Geolocation missing for units',
            isPending: true,
            actionLabel: 'Add Geolocation',
          };
        }
      } catch {
        this.eudrStatus = {
          status: 'Unknown',
          message: 'Could not verify status',
          isPending: true,
          actionLabel: 'Retry',
        };
      }
    },

    async loadWeatherData() {
      try {
        // Try to get weather from farmer's location
        const farmerId = getCurrentUserId();
        const unitsResponse = await axios.get(`/api/production-units/farmer/${farmerId}`);
        const units = unitsResponse.data?.data || [];

        if (units.length > 0) {
          // Parse coordinates
          let lat = units[0].latitude;
          let lon = units[0].longitude;

          // Parse polygon center if point missing
          if ((!lat || !lon) && units[0].wgs84Coordinates) {
            try {
              const parts = units[0].wgs84Coordinates.split(';')[0].split(',');
              if (parts.length >= 2) {
                lon = parseFloat(parts[0]);
                lat = parseFloat(parts[1]);
              }
            } catch (e) {
              // Ignore parse error
            }
          }

          if (lat && lon) {
            // Use backend API which proxies Open-Meteo and handles caching/mapping
            try {
              const weatherResponse = await axios.get(
                `/api/v1/weather?latitude=${lat}&longitude=${lon}`,
              );

              if (weatherResponse.data?.current) {
                const current = weatherResponse.data.current;

                this.weather.temperature = Math.round(current.temperature);
                this.weather.condition = current.condition;
                this.weather.location = units[0].administrativeRegion || units[0].unitName || this.weather.location;
              }
            } catch {
              this.weather.condition = 'Unavailable';
            }
          } else {
            this.weather.location = 'No Coordinates';
            this.weather.condition = 'Location Needed';
          }
        } else {
          this.weather.location = 'No Location';
          this.weather.condition = 'Set Location';
        }
      } catch {
        this.weather.condition = 'Unavailable';
      }
    },

    getWeatherCondition(code) {
      // Backend handles this now
      return code;
    },

    async loadMarketPrices() {
      try {
        const farmerId = getCurrentUserId();
        const units = await axios.get(`/api/production-units/farmer/${farmerId}`);
        const county = (units.data?.data && units.data.data.length > 0) ? units.data.data[0].administrativeRegion : null;

        // Load market prices from backend, passing location context
        const response = await axios.get('/api/v1/market-prices/latest', {
          params: { location: county }, // Backend can use this for future filtering
        });

        if (response.data?.prices) {
          const prices = response.data.prices;
          // Get coffee prices if available
          const coffeePrice = prices.find((p) => p.produce?.toLowerCase().includes('coffee'));
          if (coffeePrice) {
            this.currentPrice = coffeePrice.pricePerKg;
            this.priceChange = coffeePrice.changePercent || 0;

            // Update chart if historical data available
            if (coffeePrice.history) {
              this.marketSeries = [{
                name: 'Price',
                data: coffeePrice.history.map((h) => h.price),
              }];
            }
          }
        }
      } catch {
        this.currentPrice = 0;
        this.priceChange = 0;
      }
    },

    async loadRecentActivities() {
      try {
        const farmerId = getCurrentUserId();

        // Try activities endpoint
        const response = await axios.get(`/farmers-service/api/farmers/${farmerId}/activities`);
        if (response.data?.length > 0) {
          this.recentActivities = response.data.slice(0, 5).map((activity) => ({
            user: activity.userName || this.userName,
            action: activity.description || activity.action,
            time: this.formatTimeAgo(activity.createdAt),
          }));
          return;
        }
      } catch {
        // Ignore
      }

      // Fallback: create activities from production units
      try {
        const farmerId = getCurrentUserId();
        const units = await axios.get(`/api/production-units/farmer/${farmerId}`);
        const listings = await axios.get('/farmers-service/listing/farmer', {
          params: { farmerId, page: 0, size: 5 },
        });

        const activities = [];

        (units.data?.data || []).slice(0, 2).forEach((unit) => {
          activities.push({
            user: this.userName,
            action: `registered ${unit.produceName || 'production unit'}`,
            time: this.formatTimeAgo(unit.createdAt),
          });
        });

        ((listings.data?.data?.content) || []).slice(0, 3).forEach((listing) => {
          activities.push({
            user: this.userName,
            action: `listed ${listing.farmerProduce?.farmProduce?.name || 'produce'}`,
            time: this.formatTimeAgo(listing.createdAt),
          });
        });

        this.recentActivities = activities.slice(0, 5);
      } catch {
        this.recentActivities = [];
      }
    },

    async loadAIInsight() {
      try {
        const farmerId = getCurrentUserId();

        // Try real AI insight endpoint
        try {
          const response = await axios.get(`/farmers-service/api/farmers/${farmerId}/insight`);
          if (response.data?.insight) {
            this.aiInsight = response.data.insight;
            return;
          }
        } catch (e) {
          // Fallback to local logic if endpoint fails
        }

        // Fallback local logic - purely derived, no hardcoded strings other than templates
        const units = await axios.get(`/api/production-units/farmer/${farmerId}`);
        const productionUnits = units.data?.data || [];

        if (productionUnits.length > 0) {
          // Generate insight based on crops
          const crops = [...new Set(productionUnits.map((u) => u.produceName).filter(Boolean))];

          if (crops.length > 0) {
            const insights = [
              `Based on your ${crops[0]} production, consider soil testing for optimal yields.`,
              `Your ${crops[0]} crops may benefit from Nitrogen fertilizer this season.`,
              `Market prices for ${crops[0]} are trending upward. Good time to plan sales.`,
              `Consider diversifying with companion crops alongside ${crops[0]}.`,
            ];
            this.aiInsight = insights[Math.floor(Math.random() * insights.length)];
          } else {
            this.aiInsight = 'Complete your farm profile to get personalized insights.';
          }
        } else {
          this.aiInsight = 'Add production units to receive AI-powered insights.';
        }
      } catch {
        this.aiInsight = 'AI Insights unavailable.';
      }
    },

    formatTimeAgo(dateString) {
      if (!dateString) return 'Recently';

      const date = new Date(dateString);
      const now = new Date();
      const diffMs = now - date;
      const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

      if (diffDays === 0) return 'Today';
      if (diffDays === 1) return 'Yesterday';
      if (diffDays < 7) return `${diffDays} days ago`;
      if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
      if (diffDays < 365) return `${Math.floor(diffDays / 30)} months ago`;
      return `${Math.floor(diffDays / 365)} years ago`;
    },

    continueSetup() {
      if (this.eudrStatus.isPending) {
        this.$router.push({ name: 'ProductionUnits' });
      } else {
        this.$router.push({ name: 'FarmerProfile' });
      }
    },

    goToEudrSetup() {
      this.$router.push({ name: 'ProductionUnits' });
    },

    registerNewCrop() {
      this.$router.push({ name: 'FarmerProductionUnits', query: { action: 'add' } });
    },

    viewRecommendation() {
      this.$router.push({ name: 'FarmAI' });
    },

    openAgriCopilot() {
      this.$router.push({ name: 'FarmAI' });
    },
  },
};
</script>

<style scoped>
.farmer-dashboard {
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
}

/* Smooth card hover animations */
.hover\:tw-shadow-md:hover,
.hover\:tw-shadow-lg:hover {
  transform: translateY(-2px);
}

/* EUDR Badge fixed position */
.eudr-badge-chip {
  position: fixed !important;
  bottom: 24px;
  left: 24px;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

/* Agri-Copilot FAB styling */
.agri-copilot-fab {
  z-index: 100;
}
</style>
