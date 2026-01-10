<template>
    <v-container fluid class="supplier-dashboard tw-bg-slate-50 tw-min-h-screen">
      <!-- Header Section -->
      <v-row>
        <v-col cols="12">
          <div class="tw-flex tw-items-center tw-justify-between tw-mb-6">
            <div class="tw-flex tw-items-center tw-gap-4">
              <v-icon color="purple" size="48">mdi-domain</v-icon>
              <div>
                <h1 class="tw-text-3xl tw-font-bold tw-text-gray-800">Supplier Dashboard</h1>
                <p class="tw-text-gray-600 tw-mt-1">Manage transfers, inventory, and supply chain operations</p>
              </div>
            </div>
            <v-btn color="primary" large :to="{ name: 'SendToSupplier' }">
              <v-icon left>mdi-send</v-icon>
              New Transfer
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- Statistics Cards -->
      <v-row>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="orange lighten-5" elevation="2">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-3xl tw-font-bold tw-text-orange-700">{{ stats.pendingIncoming }}</div>
                  <div class="stat-label tw-text-sm tw-text-gray-600">Pending Incoming</div>
                </div>
                <v-icon size="48" color="orange">mdi-inbox-arrow-down</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="green lighten-5" elevation="2">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-3xl tw-font-bold tw-text-green-700">{{ stats.inventoryItems }}</div>
                  <div class="stat-label tw-text-sm tw-text-gray-600">Inventory Items</div>
                </div>
                <v-icon size="48" color="green">mdi-warehouse</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="blue lighten-5" elevation="2">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-3xl tw-font-bold tw-text-blue-700">{{ formatNumber(stats.totalQuantityKg) }} kg</div>
                  <div class="stat-label tw-text-sm tw-text-gray-600">Total Stock</div>
                </div>
                <v-icon size="48" color="blue">mdi-weight-kilogram</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="purple lighten-5" elevation="2">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-3xl tw-font-bold tw-text-purple-700">{{ stats.outgoingTransfers }}</div>
                  <div class="stat-label tw-text-sm tw-text-gray-600">Outgoing Transfers</div>
                </div>
                <v-icon size="48" color="purple">mdi-send</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Quick Actions -->
      <v-row class="tw-mt-4">
        <v-col cols="12">
          <h2 class="tw-text-xl tw-font-semibold tw-text-gray-800 tw-mb-4">Quick Actions</h2>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="quick-action-card" hover :to="{ name: 'IncomingTransfers' }">
            <v-card-text class="tw-text-center tw-py-6">
              <v-icon size="48" color="orange" class="tw-mb-2">mdi-inbox-arrow-down</v-icon>
              <div class="tw-font-medium tw-text-gray-800">Incoming Transfers</div>
              <div class="tw-text-sm tw-text-gray-500">Review & confirm</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="quick-action-card" hover :to="{ name: 'SupplierInventory' }">
            <v-card-text class="tw-text-center tw-py-6">
              <v-icon size="48" color="green" class="tw-mb-2">mdi-warehouse</v-icon>
              <div class="tw-font-medium tw-text-gray-800">My Inventory</div>
              <div class="tw-text-sm tw-text-gray-500">View stock levels</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="quick-action-card" hover :to="{ name: 'SendToSupplier' }">
            <v-card-text class="tw-text-center tw-py-6">
              <v-icon size="48" color="blue" class="tw-mb-2">mdi-send</v-icon>
              <div class="tw-font-medium tw-text-gray-800">Send Transfer</div>
              <div class="tw-text-sm tw-text-gray-500">Transfer to another supplier</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="quick-action-card" hover :to="{ name: 'OutgoingTransfers' }">
            <v-card-text class="tw-text-center tw-py-6">
              <v-icon size="48" color="purple" class="tw-mb-2">mdi-send-check</v-icon>
              <div class="tw-font-medium tw-text-gray-800">Outgoing Transfers</div>
              <div class="tw-text-sm tw-text-gray-500">Track sent items</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Recent Activity -->
      <v-row class="tw-mt-4">
        <v-col cols="12" md="6">
          <v-card>
            <v-card-title class="tw-flex tw-items-center tw-justify-between">
              <span>
                <v-icon left color="orange">mdi-inbox-arrow-down</v-icon>
                Pending Incoming
              </span>
              <v-btn text small color="primary" :to="{ name: 'IncomingTransfers' }">View All</v-btn>
            </v-card-title>
            <v-divider></v-divider>
            <v-list v-if="pendingTransfers.length > 0">
              <v-list-item v-for="transfer in pendingTransfers.slice(0, 5)" :key="transfer.id">
                <v-list-item-icon>
                  <v-icon color="orange">mdi-package-variant</v-icon>
                </v-list-item-icon>
                <v-list-item-content>
                  <v-list-item-title>{{ transfer.produceType || 'Produce' }}</v-list-item-title>
                  <v-list-item-subtitle>
                    {{ transfer.senderQuantityKg }} kg from {{ transfer.senderName || 'Unknown' }}
                  </v-list-item-subtitle>
                </v-list-item-content>
                <v-list-item-action>
                  <v-chip small color="orange" text-color="white">Pending</v-chip>
                </v-list-item-action>
              </v-list-item>
            </v-list>
            <v-card-text v-else class="tw-text-center tw-py-8 tw-text-gray-500">
              <v-icon size="48" color="grey lighten-1">mdi-inbox</v-icon>
              <div class="tw-mt-2">No pending incoming transfers</div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="6">
          <v-card>
            <v-card-title class="tw-flex tw-items-center tw-justify-between">
              <span>
                <v-icon left color="green">mdi-warehouse</v-icon>
                Inventory Summary
              </span>
              <v-btn text small color="primary" :to="{ name: 'SupplierInventory' }">View All</v-btn>
            </v-card-title>
            <v-divider></v-divider>
            <v-list v-if="inventoryItems.length > 0">
              <v-list-item v-for="item in inventoryItems.slice(0, 5)" :key="item.id">
                <v-list-item-icon>
                  <v-icon color="green">mdi-leaf</v-icon>
                </v-list-item-icon>
                <v-list-item-content>
                  <v-list-item-title>{{ item.produceType }}</v-list-item-title>
                  <v-list-item-subtitle>{{ item.quantityKg }} kg - Grade {{ item.qualityGrade || 'N/A' }}</v-list-item-subtitle>
                </v-list-item-content>
                <v-list-item-action>
                  <v-chip small>{{ item.sourceName || 'Unknown' }}</v-chip>
                </v-list-item-action>
              </v-list-item>
            </v-list>
            <v-card-text v-else class="tw-text-center tw-py-8 tw-text-gray-500">
              <v-icon size="48" color="grey lighten-1">mdi-package-variant</v-icon>
              <div class="tw-mt-2">No inventory items yet</div>
              <div class="tw-text-sm">Confirm incoming transfers to add stock</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
</template>

<script>
import axios from 'axios';
import { getAuth } from 'firebase/auth';

export default {
  name: 'SupplierDashboard',
  data() {
    return {
      loading: true,
      supplierId: null,
      stats: {
        pendingIncoming: 0,
        inventoryItems: 0,
        totalQuantityKg: 0,
        outgoingTransfers: 0,
      },
      pendingTransfers: [],
      inventoryItems: [],
    };
  },
  async mounted() {
    await this.getCurrentUser();
    await Promise.all([
      this.loadPendingTransfers(),
      this.loadInventory(),
    ]);
    this.loading = false;
  },
  methods: {
    async getCurrentUser() {
      const auth = getAuth();
      this.supplierId = auth.currentUser?.uid;
    },

    async loadPendingTransfers() {
      if (!this.supplierId) return;
      try {
        const response = await axios.get(`/api/v1/transfers/incoming/${this.supplierId}`, {
          params: { page: 0, size: 10 },
        });
        const transfers = response.data?.content || response.data || [];
        this.pendingTransfers = transfers.filter((t) => t.status === 'PENDING');
        this.stats.pendingIncoming = this.pendingTransfers.length;
      } catch (error) {
        this.$toast.error('Failed to load pending transfers:', error.message);
        this.pendingTransfers = [];
      }
    },

    async loadInventory() {
      if (!this.supplierId) return;
      try {
        const response = await axios.get(`/api/v1/suppliers/${this.supplierId}/inventory`);
        this.inventoryItems = response.data || [];
        this.stats.inventoryItems = this.inventoryItems.length;
        this.stats.totalQuantityKg = this.inventoryItems.reduce(
          (sum, item) => sum + (parseFloat(item.quantityKg) || 0), 0,
        );
      } catch (error) {
        this.$toast.error('Failed to load inventory:', error.message);
        this.inventoryItems = [];
      }
    },

    formatNumber(num) {
      if (!num) return '0';
      return Number(num).toLocaleString(undefined, { maximumFractionDigits: 2 });
    },
  },
};
</script>

<style scoped>
.supplier-dashboard {
  padding: 24px;
}

.stats-card {
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1) !important;
}

.quick-action-card {
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.quick-action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
}
</style>
