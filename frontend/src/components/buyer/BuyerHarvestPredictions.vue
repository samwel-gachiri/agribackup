<template>
  <v-card class="tw-rounded-xl tw-shadow-md tw-mt-6" :loading="loading">
    <v-card-title class="tw-flex tw-justify-between tw-items-center">
      <div class="tw-flex tw-items-center tw-gap-2">
        <v-icon color="teal">mdi-sprout</v-icon>
        <span class="tw-font-semibold tw-text-sm">Upcoming Harvests (Next {{ windowDays }}d)</span>
      </div>
      <div class="tw-flex tw-gap-2">
        <v-select v-model="windowDays" :items="[15,30,45,60]" hide-details density="compact" style="max-width:80px" @update:modelValue="load" />
        <v-btn size="small" variant="text" @click="load" :loading="loading">
          <v-icon left>mdi-refresh</v-icon>Refresh
        </v-btn>
      </div>
    </v-card-title>
    <v-data-table
      :headers="headers"
      :items="rows"
      :loading="loading"
      density="compact"
      class="elevation-0"
    >
      <template #item.predictedHarvestDate="{ item }">
        <span>{{ fmt(item.predictedHarvestDate) }}</span>
      </template>
      <template #item.plantingDate="{ item }">
        <span>{{ fmt(item.plantingDate) }}</span>
      </template>
      <template #item.confidence="{ item }">
        <span v-if="item.confidence != null">{{ (item.confidence*100).toFixed(0) }}%</span>
        <span v-else class="tw-text-gray-400">—</span>
      </template>
      <template #item.status="{ item }">
        <v-chip size="x-small" :color="statusColor(item.status)" variant="flat">{{ item.status }}</v-chip>
      </template>
    </v-data-table>
    <v-alert v-if="!loading && rows.length===0" type="info" density="compact" class="tw-m-4">No upcoming harvests.</v-alert>
  </v-card>
</template>
<script>
// import harvestApi from '@/services/harvestPrediction.service.js';
import { mapState } from 'vuex';

export default {
  name: 'BuyerHarvestPredictions',
  data() {
    return {
      loading: false,
      windowDays: 30,
      rows: [],
      headers: [
        { title: 'Harvest', key: 'predictedHarvestDate' },
        { title: 'Produce', key: 'produceName' },
        { title: 'Farmer', key: 'farmerName' },
        { title: 'Planted', key: 'plantingDate' },
        { title: 'Species', key: 'predictedSpecies' },
        { title: 'Confidence', key: 'confidence' },
        { title: 'Status', key: 'status' },
      ],
    };
  },
  computed: {
    ...mapState({ user: (s) => s.auth.user }),
    exporterId() { return this.user?.exporterId || this.user?.roleSpecificId || null; },
  },
  methods: {
    async load() {
      if (!this.exporterId) return;
      this.loading = true;
      try {
        // Mock harvest predictions data
        await new Promise((resolve) => setTimeout(resolve, 800)); // Simulate API call
        this.rows = [
          {
            predictedHarvestDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            produceName: 'Tomatoes',
            farmerName: 'John Farmer',
            plantingDate: new Date(Date.now() - 60 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            predictedSpecies: 'Roma Tomatoes',
            confidence: 0.85,
            status: 'HARVEST_PLANNED',
          },
          {
            predictedHarvestDate: new Date(Date.now() + 14 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            produceName: 'Maize',
            farmerName: 'Mary Johnson',
            plantingDate: new Date(Date.now() - 75 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            predictedSpecies: 'Yellow Maize',
            confidence: 0.92,
            status: 'HARVEST_PLANNED',
          },
          {
            predictedHarvestDate: new Date(Date.now() + 21 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            produceName: 'Potatoes',
            farmerName: 'David Smith',
            plantingDate: new Date(Date.now() - 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            predictedSpecies: 'Irish Potatoes',
            confidence: 0.78,
            status: 'HARVESTED',
          },
        ];
      } catch (e) {
        // silent fail; could add toast
      } finally { this.loading = false; }
    },
    fmt(d) { return d ? new Date(d).toLocaleDateString() : '—'; },
    statusColor(s) {
      if (s === 'HARVEST_PLANNED') return 'teal';
      if (s === 'HARVESTED') return 'green';
      return 'grey';
    },
  },
  mounted() { this.load(); },
};
</script>
