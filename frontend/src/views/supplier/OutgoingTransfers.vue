<template>
  <Default>
    <v-container fluid class="tw-py-6 tw-bg-slate-50 tw-min-h-screen">
      <!-- Header -->
      <div class="tw-mb-6">
        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-2">
          <v-icon color="orange" size="32">mdi-inbox-arrow-up</v-icon>
          <div>
            <h1 class="tw-text-2xl tw-font-bold tw-text-slate-900">Outgoing Transfers</h1>
            <p class="tw-text-slate-500 tw-text-sm">Track transfers you've sent to other suppliers</p>
          </div>
        </div>
      </div>

      <!-- Filters -->
      <v-card class="tw-mb-4">
        <v-card-text>
          <v-row align="center">
            <v-col cols="12" md="4">
              <v-select
                v-model="statusFilter"
                :items="statusOptions"
                label="Filter by Status"
                outlined
                dense
                hide-details
                clearable
              ></v-select>
            </v-col>
            <v-col cols="12" md="4">
              <v-text-field
                v-model="search"
                prepend-inner-icon="mdi-magnify"
                label="Search..."
                outlined
                dense
                hide-details
                clearable
              ></v-text-field>
            </v-col>
            <v-col cols="12" md="4" class="tw-text-right">
              <v-btn color="primary" @click="$router.push({ name: 'SendToSupplier' })">
                <v-icon left>mdi-plus</v-icon>
                New Transfer
              </v-btn>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- Transfers Table -->
      <v-card>
        <v-data-table
          :headers="headers"
          :items="filteredTransfers"
          :loading="loading"
          :search="search"
          class="elevation-0"
        >
          <!-- Recipient Column -->
          <template v-slot:item.recipient="{ item }">
            <div class="tw-flex tw-items-center tw-gap-2 tw-py-2">
              <v-avatar size="36" :color="getRecipientColor(item.recipientType)">
                <v-icon color="white" small>{{ getRecipientIcon(item.recipientType) }}</v-icon>
              </v-avatar>
              <div>
                <div class="tw-font-medium">{{ item.recipientName }}</div>
                <div class="tw-text-xs tw-text-slate-500">{{ item.recipientType }}</div>
              </div>
            </div>
          </template>

          <!-- Produce Column -->
          <template v-slot:item.produce="{ item }">
            <div class="tw-font-medium">{{ item.produceType }}</div>
          </template>

          <!-- Quantities Column -->
          <template v-slot:item.quantities="{ item }">
            <div>
              <div class="tw-font-medium">{{ formatNumber(item.senderQuantityKg) }} kg sent</div>
              <div v-if="item.receiverQuantityKg && item.receiverQuantityKg !== item.senderQuantityKg">
                   class="tw-text-xs tw-text-orange-600">
                {{ formatNumber(item.receiverQuantityKg) }} kg received
                <v-icon x-small color="orange">mdi-alert</v-icon>
              </div>
            </div>
          </template>

          <!-- Status Column -->
          <template v-slot:item.status="{ item }">
            <v-chip :color="getStatusColor(item.status)" text-color="white" small>
              {{ item.status }}
            </v-chip>
          </template>

          <!-- Hedera Column -->
          <template v-slot:item.hedera="{ item }">
            <span v-if="item.hederaTransactionId" class="tw-text-xs tw-font-mono tw-text-blue-600">
              {{ item.hederaTransactionId.substring(0, 15) }}...
            </span>
            <span v-else class="tw-text-slate-400">Pending</span>
          </template>

          <!-- Date Column -->
          <template v-slot:item.createdAt="{ item }">
            {{ formatDate(item.createdAt) }}
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
  name: 'OutgoingTransfers',
  components: { Default },
  data() {
    return {
      loading: false,
      transfers: [],
      statusFilter: null,
      search: '',
      statusOptions: [
        { text: 'All', value: null },
        { text: 'Pending', value: 'PENDING' },
        { text: 'Confirmed', value: 'CONFIRMED' },
        { text: 'Rejected', value: 'REJECTED' },
        { text: 'Disputed', value: 'DISPUTED' },
      ],
      headers: [
        { text: 'Recipient', value: 'recipient' },
        { text: 'Produce', value: 'produce' },
        { text: 'Quantity', value: 'quantities' },
        { text: 'Status', value: 'status' },
        { text: 'Hedera TX', value: 'hedera' },
        { text: 'Date', value: 'createdAt' },
      ],
    };
  },
  computed: {
    currentUserId() {
      return this.$store.state.user?.entityId || this.$store.state.auth?.user?.id;
    },
    filteredTransfers() {
      if (!this.statusFilter) return this.transfers;
      return this.transfers.filter((t) => t.status === this.statusFilter);
    },
  },
  mounted() {
    this.fetchTransfers();
  },
  methods: {
    async fetchTransfers() {
      this.loading = true;
      try {
        const response = await axios.get(`/api/v1/transfers/outgoing/${this.currentUserId}`);
        this.transfers = response.data?.content || response.data || [];
      } catch (error) {
        this.$toast.error('Failed to fetch outgoing transfers:', error.message);
        this.transfers = [];
      } finally {
        this.loading = false;
      }
    },

    getRecipientColor(type) {
      const colors = {
        PROCESSOR: 'purple',
        EXPORTER: 'blue',
        AGGREGATOR: 'teal',
        IMPORTER: 'cyan',
      };
      return colors[type] || 'grey';
    },

    getRecipientIcon(type) {
      const icons = {
        PROCESSOR: 'mdi-factory',
        EXPORTER: 'mdi-export',
        AGGREGATOR: 'mdi-warehouse',
        IMPORTER: 'mdi-import',
      };
      return icons[type] || 'mdi-account';
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
