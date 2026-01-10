<template>
  <Default>
    <v-container fluid class="tw-py-6 tw-bg-slate-50 tw-min-h-screen">
      <!-- Header -->
      <div class="tw-mb-6">
        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-2">
          <v-icon color="purple" size="32">mdi-warehouse</v-icon>
          <div>
            <h1 class="tw-text-2xl tw-font-bold tw-text-slate-900">My Inventory</h1>
            <p class="tw-text-slate-500 tw-text-sm">Confirmed produce from verified transfers</p>
          </div>
        </div>
      </div>

      <!-- Stats -->
      <v-row class="tw-mb-6">
        <v-col cols="12" sm="3">
          <v-card color="purple lighten-5">
            <v-card-text class="tw-text-center">
              <div class="tw-text-3xl tw-font-bold tw-text-purple-700">{{ totalItems }}</div>
              <div class="tw-text-sm tw-text-slate-600">Inventory Items</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="3">
          <v-card color="blue lighten-5">
            <v-card-text class="tw-text-center">
              <div class="tw-text-3xl tw-font-bold tw-text-blue-700">{{ formatNumber(totalQuantity) }} kg</div>
              <div class="tw-text-sm tw-text-slate-600">Total Stock</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="3">
          <v-card color="green lighten-5">
            <v-card-text class="tw-text-center">
              <div class="tw-text-3xl tw-font-bold tw-text-green-700">{{ uniqueProduceTypes }}</div>
              <div class="tw-text-sm tw-text-slate-600">Produce Types</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="3">
          <v-card color="teal lighten-5">
            <v-card-text class="tw-text-center">
              <div class="tw-text-3xl tw-font-bold tw-text-teal-700">{{ uniqueSources }}</div>
              <div class="tw-text-sm tw-text-slate-600">Source Suppliers</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Inventory Table -->
      <v-card>
        <v-card-title class="tw-flex tw-items-center tw-justify-between">
          <span>Inventory Items</span>
          <v-btn small color="primary" @click="$router.push({ name: 'SendToSupplier' })">
            <v-icon left small>mdi-send</v-icon>
            Transfer Stock
          </v-btn>
        </v-card-title>
        <v-data-table
          :headers="headers"
          :items="inventory"
          :loading="loading"
          class="elevation-0"
        >
          <!-- Produce Column -->
          <template v-slot:item.produce="{ item }">
            <div class="tw-flex tw-items-center tw-gap-2 tw-py-2">
              <v-icon :color="getProduceColor(item.produceType)">mdi-leaf</v-icon>
              <div>
                <div class="tw-font-medium">{{ item.produceType }}</div>
                <div class="tw-text-xs tw-text-slate-500">Grade {{ item.qualityGrade }}</div>
              </div>
            </div>
          </template>

          <!-- Source Column -->
          <template v-slot:item.source="{ item }">
            <div>
              <div class="tw-font-medium">{{ item.sourceName }}</div>
              <div class="tw-text-xs tw-text-slate-500">{{ item.sourceType }}</div>
            </div>
          </template>

          <!-- Quantity Column -->
          <template v-slot:item.quantity="{ item }">
            <v-chip color="blue lighten-4">
              {{ formatNumber(item.quantityKg) }} kg
            </v-chip>
          </template>

          <!-- Hedera Column -->
          <template v-slot:item.hedera="{ item }">
            <v-chip v-if="item.hederaTransactionId" small color="green" text-color="white">
              <v-icon left x-small>mdi-check</v-icon>
              Verified
            </v-chip>
          </template>

          <!-- Date Column -->
          <template v-slot:item.receivedAt="{ item }">
            {{ formatDate(item.receivedAt) }}
          </template>

          <!-- Actions Column -->
          <template v-slot:item.actions="{ item }">
            <v-btn small text color="primary" @click="viewDetails(item)">
              <v-icon small>mdi-eye</v-icon>
            </v-btn>
            <v-btn small text color="blue" @click="sendItem(item)">
              <v-icon small>mdi-send</v-icon>
            </v-btn>
          </template>
        </v-data-table>
      </v-card>
    </v-container>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'SupplierInventory',
  components: { Default },
  data() {
    return {
      loading: false,
      inventory: [],
      headers: [
        { text: 'Produce', value: 'produce' },
        { text: 'Source', value: 'source' },
        { text: 'Quantity', value: 'quantity' },
        { text: 'Verified', value: 'hedera' },
        { text: 'Received', value: 'receivedAt' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
    };
  },
  computed: {
    currentUserId() {
      return this.$store.state.user?.entityId || this.$store.state.auth?.user?.id;
    },
    totalItems() {
      return this.inventory.length;
    },
    totalQuantity() {
      return this.inventory.reduce((sum, i) => sum + (i.quantityKg || 0), 0);
    },
    uniqueProduceTypes() {
      return new Set(this.inventory.map((i) => i.produceType)).size;
    },
    uniqueSources() {
      return new Set(this.inventory.map((i) => i.sourceId)).size;
    },
  },
  mounted() {
    this.fetchInventory();
  },
  methods: {
    async fetchInventory() {
      this.loading = true;
      try {
        const response = await axios.get(`/api/v1/suppliers/${this.currentUserId}/inventory`);
        this.inventory = response.data?.content || response.data || [];
      } catch (error) {
        this.$toast.error('Failed to fetch inventory:', error.message);
        this.inventory = [];
      } finally {
        this.loading = false;
      }
    },

    viewDetails(item) {
      // Navigate to detail view
      this.$toast.info(`Viewing ${item.produceType} details`);
    },

    sendItem(item) {
      this.$router.push({
        name: 'SendToSupplier',
        query: { inventoryId: item.id },
      });
    },

    getProduceColor(type) {
      const colors = {
        'Coffee Arabica': 'brown',
        'Coffee Robusta': 'brown darken-2',
        Tea: 'green',
        Cocoa: 'orange',
        Macadamia: 'amber',
      };
      return colors[type] || 'grey';
    },

    formatNumber(num) {
      return num ? num.toLocaleString() : '0';
    },

    formatDate(dateString) {
      if (!dateString) return 'N/A';
      return new Date(dateString).toLocaleDateString();
    },
  },
};
</script>
