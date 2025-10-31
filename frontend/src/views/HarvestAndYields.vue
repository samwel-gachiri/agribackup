<template>
  <default>
    <div class="page-wrap">
      <div class="bg-shape bg-1"></div>
      <div class="bg-shape bg-2"></div>
      <div class="bg-shape bg-3"></div>
      <v-container class="glass-container harvest-yields-page" fluid>
        <v-row align="center" justify="space-between" class="mb-8">
          <v-col cols="12" md="7">
            <h1 class="display-2 font-weight-bold text--primary mb-1 d-flex align-center">
              <v-icon color="primary" large class="mr-3">mdi-food-apple</v-icon>
              Harvest & Yields
            </h1>
            <div class="subtitle-1 text--secondary mt-1">Your farm’s real-time harvest and yield insights</div>
          </v-col>
          <v-col cols="12" md="5" class="d-flex align-end justify-end gap-2">
            <v-btn color="success" class="elevation-10 px-8 py-4 text-lg font-weight-bold mr-2" large @click="goToYieldRecording">
              <v-icon left>mdi-plus-circle</v-icon> Record Harvest
            </v-btn>
            <v-btn color="info" class="elevation-10 px-8 py-4 text-lg font-weight-bold" large @click="goToAnalytics">
              <v-icon left>mdi-chart-bar</v-icon> Analytics
            </v-btn>
          </v-col>
        </v-row>
        <v-row v-if="loading" align="center" justify="center" class="my-12">
          <v-col cols="12" class="d-flex flex-column align-center justify-center">
            <v-progress-circular indeterminate color="primary" size="64" class="mb-4" />
            <span class="subtitle-1 text--secondary">Loading your harvest data…</span>
          </v-col>
        </v-row>
        <v-row v-else>
          <v-col cols="12" v-if="isEmptyState" class="empty-state d-flex flex-column align-center justify-center py-12">
            <!-- <img src="/img/empty-harvest.svg" alt="No harvests" class="w-48 mb-6 drop-shadow-xl" /> -->
            <h2 class="display-1 font-weight-bold mb-2 text--primary">No Harvest Data Yet</h2>
            <p class="mb-6 text--secondary subtitle-1">Start by planting your first crop or recording a harvest to see your progress here.</p>
            <v-btn color="primary" class="elevation-6 px-8 py-4 text-lg font-weight-bold" large @click="goToMyFarm">Go to My Farm</v-btn>
          </v-col>
          <template v-else>
            <!-- Animated Stats -->
            <v-row class="mb-10" dense>
              <v-col cols="12" md="4">
                <v-card class="elevation-12 pa-6 d-flex flex-column align-center justify-center glass-card" color="green lighten-5">
                  <v-icon color="success" x-large class="mb-2">mdi-seed</v-icon>
                  <div class="display-2 font-weight-bold text-success animate-pulse">{{ readyToHarvest.length }}</div>
                  <div class="subtitle-2 text--secondary">Ready to Harvest</div>
                </v-card>
              </v-col>
              <v-col cols="12" md="4">
                <v-card class="elevation-12 pa-6 d-flex flex-column align-center justify-center glass-card" color="blue lighten-5">
                  <v-icon color="info" x-large class="mb-2">mdi-basket</v-icon>
                  <div class="display-2 font-weight-bold text-info animate-pulse">{{ recentHarvests.length }}</div>
                  <div class="subtitle-2 text--secondary">Recently Harvested</div>
                </v-card>
              </v-col>
              <v-col cols="12" md="4">
                <v-card class="elevation-12 pa-6 d-flex flex-column align-center justify-center glass-card" color="yellow lighten-5">
                  <v-icon color="warning" x-large class="mb-2">mdi-repeat-variant</v-icon>
                  <div class="display-2 font-weight-bold text-warning animate-pulse">{{ multipleHarvests.length }}</div>
                  <div class="subtitle-2 text--secondary">Multiple Harvests</div>
                </v-card>
              </v-col>
            </v-row>
            <!-- Status Categories -->
            <v-row class="status-categories" dense>
              <v-col cols="12" md="4">
                <h3 class="font-weight-bold mb-4 text-lg text-success d-flex align-center"><v-icon left color="success">mdi-seed</v-icon> Ready to Harvest</h3>
                <div v-if="readyToHarvest.length === 0" class="text--secondary text-center">No crops ready for harvest.</div>
                <v-list two-line v-else class="transparent-list">
                  <v-list-item v-for="item in readyToHarvest" :key="item.farmerProduceId" class="mb-4 glass-card">
                    <v-list-item-content>
                      <v-list-item-title class="font-weight-bold text-success text-lg">{{ item.produceName }}</v-list-item-title>
                      <v-list-item-subtitle>Predicted Harvest: <b>{{ formatDate(item.predictedHarvestDate) }}</b></v-list-item-subtitle>
                      <v-list-item-subtitle>Status: <span class="font-weight-bold text-success">{{ item.status }}</span></v-list-item-subtitle>
                      <v-list-item-subtitle>Progress: <span class="font-mono">{{ item.growthProgress }}%</span></v-list-item-subtitle>
                    </v-list-item-content>
                    <v-list-item-action>
                      <v-btn color="primary" small class="elevation-4" @click="goToYieldRecording(item)">Record</v-btn>
                    </v-list-item-action>
                  </v-list-item>
                </v-list>
              </v-col>
              <v-col cols="12" md="4">
                <h3 class="font-weight-bold mb-4 text-lg text-info d-flex align-center"><v-icon left color="info">mdi-basket</v-icon> Recently Harvested</h3>
                <div v-if="recentHarvests.length === 0" class="text--secondary text-center">No recent harvests.</div>
                <v-list two-line v-else class="transparent-list">
                  <v-list-item v-for="item in recentHarvests" :key="item.farmerProduceId" class="mb-4 glass-card">
                    <v-list-item-content>
                      <v-list-item-title class="font-weight-bold text-info text-lg">{{ item.produceName }}</v-list-item-title>
                      <v-list-item-subtitle>Last Harvest: <b>{{ formatDate(item.lastHarvestDate) }}</b></v-list-item-subtitle>
                      <v-list-item-subtitle>Total Yields: <b>{{ item.totalYields }}</b></v-list-item-subtitle>
                      <v-list-item-subtitle>Total Yield: <b>{{ item.totalYieldAmount }} {{ item.yieldUnit || '' }}</b></v-list-item-subtitle>
                    </v-list-item-content>
                    <v-list-item-action>
                      <v-btn color="secondary" small class="elevation-4" @click="goToYieldHistory(item)">History</v-btn>
                    </v-list-item-action>
                  </v-list-item>
                </v-list>
              </v-col>
              <v-col cols="12" md="4">
                <h3 class="font-weight-bold mb-4 text-lg text-warning d-flex align-center"><v-icon left color="warning">mdi-repeat-variant</v-icon> Multiple Harvests</h3>
                <div v-if="multipleHarvests.length === 0" class="text--secondary text-center">No crops with multiple harvests.</div>
                <v-list two-line v-else class="transparent-list">
                  <v-list-item v-for="item in multipleHarvests" :key="item.farmerProduceId" class="mb-4 glass-card">
                    <v-list-item-content>
                      <v-list-item-title class="font-weight-bold text-warning text-lg">{{ item.produceName }}</v-list-item-title>
                      <v-list-item-subtitle>Harvests: <b>{{ item.totalYields }}</b></v-list-item-subtitle>
                      <v-list-item-subtitle>Last Harvest: <b>{{ formatDate(item.lastHarvestDate) }}</b></v-list-item-subtitle>
                    </v-list-item-content>
                    <v-list-item-action>
                      <v-btn color="warning" small class="elevation-4" @click="goToYieldHistory(item)">Details</v-btn>
                    </v-list-item-action>
                  </v-list-item>
                </v-list>
              </v-col>
            </v-row>
          </template>
        </v-row>
      </v-container>
    </div>
  </default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

import { getCurrentUserId } from '@/utils/roles.js';

export default {
  name: 'HarvestAndYields',
  components: { Default },
  data() {
    return {
      loading: true,
      harvestStatus: [],
    };
  },
  computed: {
    isEmptyState() {
      return this.harvestStatus.length === 0;
    },
    readyToHarvest() {
      return this.harvestStatus.filter((item) => item.status === 'READY_TO_HARVEST');
    },
    recentHarvests() {
      // Show produces harvested in the last 30 days
      const now = new Date();
      return this.harvestStatus.filter((item) => {
        if (!item.lastHarvestDate) return false;
        const diff = (now - new Date(item.lastHarvestDate)) / (1000 * 60 * 60 * 24);
        return diff <= 30;
      });
    },
    multipleHarvests() {
      return this.harvestStatus.filter((item) => item.totalYields > 1);
    },
  },
  methods: {
    async fetchHarvestStatus() {
      this.loading = true;
      try {
        const farmerId = getCurrentUserId() || this.$route.params.farmerId;
        const { data } = await axios.get(`/api/harvest/farmer/${farmerId}/status`);
        this.harvestStatus = data.data || [];
      } catch (e) {
        this.harvestStatus = [];
      } finally {
        this.loading = false;
      }
    },
    formatDate(date) {
      if (!date) return '-';
      try {
        return new Date(date).toLocaleDateString();
      } catch {
        return String(date);
      }
    },
    goToYieldRecording(item) {
      if (item && item.farmerProduceId) {
        this.$router.push({ name: 'YieldRecording', params: { farmerProduceId: item.farmerProduceId } });
      } else {
        const userId = getCurrentUserId();
        if (userId) {
          this.$router.push({ name: 'YieldRecording', params: { farmerProduceId: userId } });
        } else {
          this.$router.push({ name: 'YieldRecording' });
        }
      }
    },
    goToAnalytics() {
      this.$router.push({ name: 'HarvestAnalytics' });
    },
    goToYieldHistory(item) {
      this.$router.push({ name: 'YieldHistory', params: { farmerProduceId: item.farmerProduceId } });
    },
    goToMyFarm() {
      const userId = getCurrentUserId();
      if (userId) {
        this.$router.push({ name: 'MyFarm', params: { farmerId: userId } });
      } else {
        this.$router.push({ name: 'MyFarm' });
      }
    },
  },
  created() {
    this.fetchHarvestStatus();
  },
};
</script>

<style scoped>
.harvest-yields-page {
  padding: 2rem;
}
.card {
  border: 1px solid #e5e7eb;
}
.btn {
  padding: 1rem 1rem; /* px-4 py-2 */
  border-radius: 0.5rem; /* rounded */
  font-weight: 600; /* font-semibold */
  box-shadow: 0 1px 2px 0 rgb(0 0 0 / 0.05); /* shadow */
}
.btn-primary {
  background-color: #2563eb; /* bg-blue-600 */
  color: #fff; /* text-white */
}
.btn-primary:hover {
  background-color: #1d4ed8; /* bg-blue-700 */
}
.btn-success {
  background-color: #16a34a; /* bg-green-600 */
  color: #fff; /* text-white */
}
.btn-success:hover {
  background-color: #15803d; /* bg-green-700 */
}
.btn-info {
  background-color: #0891b2; /* bg-cyan-600 */
  color: #fff; /* text-white */
}
.btn-info:hover {
  background-color: #0e7490; /* bg-cyan-700 */
}
.btn-secondary {
  background-color: #dbeafe; /* bg-blue-100 */
  color: #1e40af; /* text-blue-800 */
}
.btn-secondary:hover {
  background-color: #bfdbfe; /* bg-blue-200 */
}
.btn-warning {
  background-color: #facc15; /* bg-yellow-400 */
  color: #713f12; /* text-yellow-900 */
}
.btn-warning:hover {
  background-color: #f59e42; /* bg-yellow-500 */
}
.btn-sm {
  font-size: 0.75rem; /* text-xs */
  padding: 0.25rem 0.5rem; /* px-2 py-1 */
}
.empty-state img {
  filter: grayscale(0.2);
}
</style>
