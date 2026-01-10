<template>
  <div class="tw-min-h-screen tw-bg-slate-50 tw-px-4 tw-py-8 lg:tw-px-10">
    <div class="tw-mx-auto tw-max-w-7xl tw-space-y-6">
      <!-- Header -->
      <div>
        <div class="tw-mb-2 tw-inline-flex tw-items-center tw-rounded-full tw-bg-purple-100 tw-px-3 tw-py-1 tw-text-sm tw-font-semibold tw-text-purple-700">
          <span class="tw-mr-2">🏭</span>
          EUDR Compliance Portal
        </div>
        <h1 class="tw-text-3xl tw-font-bold tw-text-slate-900">Processor Dashboard</h1>
        <p class="tw-mt-2 tw-text-sm tw-text-slate-600">
          Monitor processing operations, receive batches from suppliers, and forward to exporters.
        </p>
      </div>

      <!-- Quick Stats -->
      <div class="tw-grid tw-gap-4 md:tw-grid-cols-4">
        <div class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-p-6 tw-shadow-sm">
          <div class="tw-text-sm tw-font-medium tw-text-slate-500">Pending Receipts</div>
          <div class="tw-mt-2 tw-text-3xl tw-font-bold tw-text-amber-600">{{ stats.pendingReceipts }}</div>
          <div class="tw-mt-1 tw-text-xs tw-text-slate-400">Awaiting confirmation</div>
        </div>
        <div class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-p-6 tw-shadow-sm">
          <div class="tw-text-sm tw-font-medium tw-text-slate-500">In Processing</div>
          <div class="tw-mt-2 tw-text-3xl tw-font-bold tw-text-blue-600">{{ stats.inProcessing }}</div>
          <div class="tw-mt-1 tw-text-xs tw-text-slate-400">Current inventory</div>
        </div>
        <div class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-p-6 tw-shadow-sm">
          <div class="tw-text-sm tw-font-medium tw-text-slate-500">Sent to Exporters</div>
          <div class="tw-mt-2 tw-text-3xl tw-font-bold tw-text-emerald-600">{{ stats.sentToExporters }}</div>
          <div class="tw-mt-1 tw-text-xs tw-text-slate-400">This month</div>
        </div>
        <div class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-p-6 tw-shadow-sm">
          <div class="tw-text-sm tw-font-medium tw-text-slate-500">Total Volume (kg)</div>
          <div class="tw-mt-2 tw-text-3xl tw-font-bold tw-text-purple-600">{{ formatNumber(stats.totalVolumeKg) }}</div>
          <div class="tw-mt-1 tw-text-xs tw-text-slate-400">Processed this quarter</div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-p-6 tw-shadow-sm">
        <h2 class="tw-text-lg tw-font-semibold tw-text-slate-900">Quick Actions</h2>
        <div class="tw-mt-4 tw-grid tw-gap-3 md:tw-grid-cols-2 lg:tw-grid-cols-4">
          <router-link :to="{ name: 'IncomingTransfers' }" class="tw-flex tw-items-center tw-gap-3 tw-rounded-xl tw-border tw-border-green-200 tw-bg-green-50 tw-p-4 hover:tw-bg-green-100 tw-transition-colors tw-cursor-pointer">
            <div class="tw-flex tw-h-10 tw-w-10 tw-items-center tw-justify-center tw-rounded-full tw-bg-green-200">
              <v-icon color="green">mdi-inbox-arrow-down</v-icon>
            </div>
            <div>
              <div class="tw-font-semibold tw-text-slate-900">Incoming Transfers</div>
              <div class="tw-text-xs tw-text-green-600">{{ stats.pendingReceipts }} pending</div>
            </div>
          </router-link>

          <router-link :to="{ name: 'OutgoingTransfers' }" class="tw-flex tw-items-center tw-gap-3 tw-rounded-xl tw-border tw-border-orange-200 tw-bg-orange-50 tw-p-4 hover:tw-bg-orange-100 tw-transition-colors tw-cursor-pointer">
            <div class="tw-flex tw-h-10 tw-w-10 tw-items-center tw-justify-center tw-rounded-full tw-bg-orange-200">
              <v-icon color="orange">mdi-inbox-arrow-up</v-icon>
            </div>
            <div>
              <div class="tw-font-semibold tw-text-slate-900">Outgoing Transfers</div>
              <div class="tw-text-xs tw-text-orange-600">Track your shipments</div>
            </div>
          </router-link>

          <router-link :to="{ name: 'SendToSupplier' }" class="tw-flex tw-items-center tw-gap-3 tw-rounded-xl tw-border tw-border-blue-200 tw-bg-blue-50 tw-p-4 hover:tw-bg-blue-100 tw-transition-colors tw-cursor-pointer">
            <div class="tw-flex tw-h-10 tw-w-10 tw-items-center tw-justify-center tw-rounded-full tw-bg-blue-200">
              <v-icon color="blue">mdi-send</v-icon>
            </div>
            <div>
              <div class="tw-font-semibold tw-text-slate-900">Send to Exporter</div>
              <div class="tw-text-xs tw-text-blue-600">Forward processed goods</div>
            </div>
          </router-link>

          <router-link :to="{ name: 'SupplierInventory' }" class="tw-flex tw-items-center tw-gap-3 tw-rounded-xl tw-border tw-border-purple-200 tw-bg-purple-50 tw-p-4 hover:tw-bg-purple-100 tw-transition-colors tw-cursor-pointer">
            <div class="tw-flex tw-h-10 tw-w-10 tw-items-center tw-justify-center tw-rounded-full tw-bg-purple-200">
              <v-icon color="purple">mdi-warehouse</v-icon>
            </div>
            <div>
              <div class="tw-font-semibold tw-text-slate-900">My Inventory</div>
              <div class="tw-text-xs tw-text-purple-600">View confirmed stock</div>
            </div>
          </router-link>
        </div>
      </div>

      <!-- Recent Activity -->
      <div class="tw-rounded-2xl tw-border tw-border-slate-200 tw-bg-white tw-p-6 tw-shadow-sm">
        <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
          <h2 class="tw-text-lg tw-font-semibold tw-text-slate-900">Recent Transfers</h2>
          <v-btn small text color="primary" @click="fetchRecentTransfers" :loading="loading">
            <v-icon left small>mdi-refresh</v-icon> Refresh
          </v-btn>
        </div>

        <div v-if="recentTransfers.length > 0">
          <div v-for="transfer in recentTransfers" :key="transfer.id" class="tw-flex tw-items-center tw-justify-between tw-py-3 tw-border-b tw-border-slate-100 last:tw-border-0">
            <div class="tw-flex tw-items-center tw-gap-3">
              <v-avatar size="40" :color="transfer.direction === 'IN' ? 'green' : 'orange'">
                <v-icon color="white" small>{{ transfer.direction === 'IN' ? 'mdi-arrow-down' : 'mdi-arrow-up' }}</v-icon>
              </v-avatar>
              <div>
                <div class="tw-font-medium tw-text-slate-900">
                  {{ transfer.direction === 'IN' ? 'From' : 'To' }} {{ transfer.partyName }}
                </div>
                <div class="tw-text-xs tw-text-slate-500">
                  {{ transfer.produceType }} • {{ formatNumber(transfer.quantityKg) }} kg
                </div>
              </div>
            </div>
            <div class="tw-text-right">
              <v-chip :color="getStatusColor(transfer.status)" small text-color="white">
                {{ transfer.status }}
              </v-chip>
              <div class="tw-text-xs tw-text-slate-400 tw-mt-1">{{ formatDate(transfer.createdAt) }}</div>
            </div>
          </div>
        </div>
        <div v-else class="tw-flex tw-flex-col tw-items-center tw-justify-center tw-gap-3 tw-p-10">
          <div class="tw-rounded-full tw-bg-slate-100 tw-p-4">
            <v-icon size="32" color="grey">mdi-swap-horizontal</v-icon>
          </div>
          <p class="tw-text-sm tw-font-medium tw-text-slate-600">No recent transfers</p>
          <p class="tw-text-xs tw-text-slate-400">Receive or send transfers to see them here</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ProcessorDashboard',
  data() {
    return {
      loading: false,
      stats: {
        pendingReceipts: 0,
        inProcessing: 0,
        sentToExporters: 0,
        totalVolumeKg: 0,
      },
      recentTransfers: [],
    };
  },
  computed: {
    currentUserId() {
      return this.$store.state.user?.entityId || this.$store.state.auth?.user?.id;
    },
  },
  mounted() {
    this.fetchStats();
    this.fetchRecentTransfers();
  },
  methods: {
    async fetchStats() {
      try {
        const response = await axios.get(`/api/v1/transfers/pending/count/${this.currentUserId}`);
        this.stats.pendingReceipts = response.data?.count || 0;
      } catch (error) {
        this.$toast.error('Failed to fetch stats:', error.message);
        this.stats.pendingReceipts = 0;
      }
    },

    async fetchRecentTransfers() {
      this.loading = true;
      try {
        const [incoming, outgoing] = await Promise.all([
          axios.get(`/api/v1/transfers/incoming/${this.currentUserId}?size=5`),
          axios.get(`/api/v1/transfers/outgoing/${this.currentUserId}?size=5`),
        ]);

        const inTransfers = (incoming.data?.content || []).map((t) => ({
          ...t,
          direction: 'IN',
          partyName: t.senderName,
        }));
        const outTransfers = (outgoing.data?.content || []).map((t) => ({
          ...t,
          direction: 'OUT',
          partyName: t.recipientName,
        }));

        this.recentTransfers = [...inTransfers, ...outTransfers]
          .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
          .slice(0, 5);
      } catch (error) {
        this.$toast.error('Failed to fetch recent transfers:', error.message);
        this.recentTransfers = [];
      } finally {
        this.loading = false;
      }
    },

    getStatusColor(status) {
      const colors = {
        PENDING: 'orange',
        CONFIRMED: 'success',
        REJECTED: 'error',
        DISPUTED: 'warning',
      };
      return colors[status] || 'grey';
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
