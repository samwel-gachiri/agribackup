<template>
  <Default>
    <v-container fluid class="tw-py-6 tw-bg-slate-50 tw-min-h-screen">
      <!-- Header -->
      <div class="tw-mb-6">
        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-2">
          <v-icon color="green" size="32">mdi-inbox-arrow-down</v-icon>
          <div>
            <h1 class="tw-text-2xl tw-font-bold tw-text-slate-900">Incoming Transfers</h1>
            <p class="tw-text-slate-500 tw-text-sm">Review and confirm produce transfers from farmers and suppliers</p>
          </div>
        </div>
      </div>

      <!-- Stats -->
      <v-row class="tw-mb-6">
        <v-col cols="12" sm="4">
          <v-card color="orange lighten-5" class="tw-h-full">
            <v-card-text class="tw-text-center">
              <v-icon size="40" color="orange">mdi-clock-outline</v-icon>
              <div class="tw-text-3xl tw-font-bold tw-text-orange-700 tw-mt-2">{{ pendingCount }}</div>
              <div class="tw-text-sm tw-text-slate-600">Pending Confirmation</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="4">
          <v-card color="green lighten-5" class="tw-h-full">
            <v-card-text class="tw-text-center">
              <v-icon size="40" color="green">mdi-check-circle</v-icon>
              <div class="tw-text-3xl tw-font-bold tw-text-green-700 tw-mt-2">{{ confirmedCount }}</div>
              <div class="tw-text-sm tw-text-slate-600">Confirmed This Week</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="4">
          <v-card color="red lighten-5" class="tw-h-full">
            <v-card-text class="tw-text-center">
              <v-icon size="40" color="red">mdi-alert-circle</v-icon>
              <div class="tw-text-3xl tw-font-bold tw-text-red-700 tw-mt-2">{{ disputedCount }}</div>
              <div class="tw-text-sm tw-text-slate-600">Disputed</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Transfers List -->
      <v-card>
        <v-card-title class="tw-flex tw-items-center tw-justify-between">
          <span>Pending Transfers</span>
          <v-btn small text color="primary" @click="fetchTransfers" :loading="loading">
            <v-icon left small>mdi-refresh</v-icon> Refresh
          </v-btn>
        </v-card-title>

        <v-data-table
          :headers="headers"
          :items="transfers"
          :loading="loading"
          class="elevation-0"
        >
          <!-- Sender Column -->
          <template v-slot:item.sender="{ item }">
            <div class="tw-flex tw-items-center tw-gap-2 tw-py-2">
              <v-avatar size="36" :color="getSenderColor(item.senderType)">
                <v-icon color="white" small>{{ getSenderIcon(item.senderType) }}</v-icon>
              </v-avatar>
              <div>
                <div class="tw-font-medium">{{ item.senderName }}</div>
                <div class="tw-text-xs tw-text-slate-500">{{ item.senderType }}</div>
              </div>
            </div>
          </template>

          <!-- Produce Column -->
          <template v-slot:item.produce="{ item }">
            <div>
              <div class="tw-font-medium">{{ item.produceType }}</div>
              <div class="tw-text-xs tw-text-slate-500">{{ item.productionUnitName || 'N/A' }}</div>
            </div>
          </template>

          <!-- Quantity Column -->
          <template v-slot:item.quantity="{ item }">
            <v-chip color="blue lighten-4" small>
              {{ formatNumber(item.quantityKg) }} kg
            </v-chip>
          </template>

          <!-- Status Column -->
          <template v-slot:item.status="{ item }">
            <v-chip :color="getStatusColor(item.status)" text-color="white" small>
              {{ item.status }}
            </v-chip>
          </template>

          <!-- Date Column -->
          <template v-slot:item.createdAt="{ item }">
            {{ formatDate(item.createdAt) }}
          </template>

          <!-- Actions Column -->
          <template v-slot:item.actions="{ item }">
            <div class="tw-flex tw-gap-1" v-if="item.status === 'PENDING'">
              <v-btn small color="success" @click="openConfirmDialog(item)">
                <v-icon left small>mdi-check</v-icon> Confirm
              </v-btn>
              <v-btn small outlined color="orange" @click="openDisputeDialog(item)">
                <v-icon left small>mdi-alert</v-icon> Dispute
              </v-btn>
              <v-btn small text color="error" @click="rejectTransfer(item)">
                <v-icon small>mdi-close</v-icon>
              </v-btn>
            </div>
            <span v-else class="tw-text-slate-400">—</span>
          </template>
        </v-data-table>
      </v-card>

      <!-- Confirm Dialog -->
      <v-dialog v-model="showConfirmDialog" max-width="500" persistent>
        <v-card v-if="selectedTransfer">
          <v-card-title class="tw-bg-green-600 tw-text-white">
            <v-icon color="white" class="tw-mr-2">mdi-check-circle</v-icon>
            Confirm Transfer Receipt
          </v-card-title>
          <v-card-text class="tw-pt-6">
            <v-alert type="info" text dense class="tw-mb-4">
              <strong>{{ selectedTransfer.senderName }}</strong> claims to have sent you
              <strong>{{ selectedTransfer.quantityKg }} kg</strong> of
              <strong>{{ selectedTransfer.produceType }}</strong>.
            </v-alert>

            <v-text-field
              v-model.number="confirmedQuantity"
              label="Quantity Received (kg)"
              type="number"
              outlined
              dense
              :hint="confirmedQuantity !== selectedTransfer.quantityKg ? 'Quantity differs from sender claim - this will be recorded' : ''"
              persistent-hint
            ></v-text-field>

            <v-textarea
              v-model="confirmNotes"
              label="Notes (optional)"
              outlined
              dense
              rows="2"
            ></v-textarea>
          </v-card-text>
          <v-card-actions>
            <v-btn text @click="showConfirmDialog = false">Cancel</v-btn>
            <v-spacer></v-spacer>
            <v-btn color="success" :loading="confirming" @click="confirmTransfer">
              <v-icon left>mdi-check</v-icon>
              Confirm Receipt
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Dispute Dialog -->
      <v-dialog v-model="showDisputeDialog" max-width="500" persistent>
        <v-card v-if="selectedTransfer">
          <v-card-title class="tw-bg-orange-600 tw-text-white">
            <v-icon color="white" class="tw-mr-2">mdi-alert</v-icon>
            Report Discrepancy
          </v-card-title>
          <v-card-text class="tw-pt-6">
            <v-alert type="warning" text dense class="tw-mb-4">
              Report a discrepancy in quantity or quality. Both claims will be recorded immutably.
            </v-alert>

            <v-text-field
              v-model.number="disputedQuantity"
              label="Actual Quantity Received (kg)"
              type="number"
              outlined
              dense
            ></v-text-field>

            <v-textarea
              v-model="disputeReason"
              label="Reason for Dispute *"
              outlined
              dense
              rows="3"
              :rules="[v => !!v || 'Required']"
            ></v-textarea>
          </v-card-text>
          <v-card-actions>
            <v-btn text @click="showDisputeDialog = false">Cancel</v-btn>
            <v-spacer></v-spacer>
            <v-btn color="orange" :loading="disputing" @click="submitDispute">
              <v-icon left>mdi-alert</v-icon>
              Submit Dispute
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-container>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'IncomingTransfers',
  components: { Default },
  data() {
    return {
      loading: false,
      confirming: false,
      disputing: false,
      transfers: [],
      showConfirmDialog: false,
      showDisputeDialog: false,
      selectedTransfer: null,
      confirmedQuantity: 0,
      confirmNotes: '',
      disputedQuantity: 0,
      disputeReason: '',
      headers: [
        { text: 'From', value: 'sender' },
        { text: 'Produce', value: 'produce' },
        { text: 'Quantity', value: 'quantity' },
        { text: 'Status', value: 'status' },
        { text: 'Date', value: 'createdAt' },
        {
          text: 'Actions', value: 'actions', sortable: false, width: '280px',
        },
      ],
    };
  },
  computed: {
    currentUserId() {
      return this.$store.state.user?.entityId || this.$store.state.auth?.user?.id;
    },
    pendingCount() {
      return this.transfers.filter((t) => t.status === 'PENDING').length;
    },
    confirmedCount() {
      return this.transfers.filter((t) => t.status === 'CONFIRMED').length;
    },
    disputedCount() {
      return this.transfers.filter((t) => t.status === 'DISPUTED').length;
    },
  },
  mounted() {
    this.fetchTransfers();
  },
  methods: {
    async fetchTransfers() {
      this.loading = true;
      try {
        const response = await axios.get(`/api/v1/transfers/incoming/${this.currentUserId}`);
        this.transfers = response.data?.content || response.data || [];
      } catch (error) {
        this.$toast.error('Failed to fetch incoming transfers:', error.message);
        this.transfers = [];
      } finally {
        this.loading = false;
      }
    },

    openConfirmDialog(transfer) {
      this.selectedTransfer = transfer;
      this.confirmedQuantity = transfer.quantityKg;
      this.confirmNotes = '';
      this.showConfirmDialog = true;
    },

    openDisputeDialog(transfer) {
      this.selectedTransfer = transfer;
      this.disputedQuantity = transfer.quantityKg;
      this.disputeReason = '';
      this.showDisputeDialog = true;
    },

    async confirmTransfer() {
      this.confirming = true;
      try {
        await axios.put(`/api/v1/transfers/${this.selectedTransfer.id}/confirm`, {
          receivedQuantityKg: this.confirmedQuantity,
          notes: this.confirmNotes,
        });
        this.$toast.success('Transfer confirmed! Data will be written to Hedera.');
        this.showConfirmDialog = false;
        await this.fetchTransfers();
      } catch (error) {
        this.$toast.error('Failed to confirm transfer');
      } finally {
        this.confirming = false;
      }
    },

    async submitDispute() {
      if (!this.disputeReason) {
        this.$toast.warning('Please provide a reason for the dispute');
        return;
      }
      this.disputing = true;
      try {
        await axios.put(`/api/v1/transfers/${this.selectedTransfer.id}/dispute`, {
          actualQuantityKg: this.disputedQuantity,
          reason: this.disputeReason,
        });
        this.$toast.warning('Dispute recorded. Both claims are now immutably stored.');
        this.showDisputeDialog = false;
        await this.fetchTransfers();
      } catch (error) {
        this.$toast.error('Failed to submit dispute');
      } finally {
        this.disputing = false;
      }
    },

    async rejectTransfer(transfer) {
      // eslint-disable-next-line no-alert
      if (!window.confirm('Are you sure you want to reject this transfer?')) return;
      try {
        await axios.put(`/api/v1/transfers/${transfer.id}/reject`);
        this.$toast.info('Transfer rejected');
        await this.fetchTransfers();
      } catch (error) {
        this.$toast.error('Failed to reject transfer');
      }
    },

    getSenderColor(type) {
      const colors = {
        FARMER: 'green',
        AGGREGATOR: 'blue',
        PROCESSOR: 'purple',
        COOPERATIVE: 'teal',
      };
      return colors[type] || 'grey';
    },

    getSenderIcon(type) {
      const icons = {
        FARMER: 'mdi-account',
        AGGREGATOR: 'mdi-warehouse',
        PROCESSOR: 'mdi-factory',
        COOPERATIVE: 'mdi-account-group',
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
