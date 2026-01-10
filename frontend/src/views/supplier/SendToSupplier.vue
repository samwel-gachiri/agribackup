<template>
  <Default>
    <v-container fluid class="tw-py-6 tw-bg-slate-50 tw-min-h-screen">
      <!-- Header -->
      <div class="tw-mb-6">
        <div class="tw-flex tw-items-center tw-gap-3 tw-mb-2">
          <v-icon color="blue" size="32">mdi-send</v-icon>
          <div>
            <h1 class="tw-text-2xl tw-font-bold tw-text-slate-900">Send to Supplier</h1>
            <p class="tw-text-slate-500 tw-text-sm">Initiate a transfer to another supplier in your network</p>
          </div>
        </div>
      </div>

      <v-row>
        <!-- Transfer Form -->
        <v-col cols="12" md="7">
          <v-card>
            <v-card-title class="tw-bg-blue-600 tw-text-white">
              <v-icon color="white" class="tw-mr-2">mdi-package-variant</v-icon>
              New Transfer
            </v-card-title>
            <v-card-text class="tw-pt-6">
              <v-form ref="transferForm" v-model="formValid">
                <!-- Destination Supplier -->
                <v-autocomplete
                  v-model="transfer.toSupplierId"
                  :items="connectedSuppliers"
                  item-text="displayName"
                  item-value="id"
                  label="Destination Supplier *"
                  outlined
                  dense
                  prepend-inner-icon="mdi-account-arrow-right"
                  :loading="loadingSuppliers"
                  :rules="[v => !!v || 'Required']"
                  class="tw-mb-2"
                >
                  <template v-slot:item="{ item }">
                    <div class="tw-py-2">
                      <div class="tw-font-medium">{{ item.supplierName }}</div>
                      <div class="tw-text-xs tw-text-slate-500">
                        {{ item.supplierType }} • {{ item.region || 'Unknown region' }}
                      </div>
                    </div>
                  </template>
                </v-autocomplete>

                <!-- Source (Inventory or Production Unit) -->
                <v-select
                  v-model="transfer.sourceType"
                  :items="sourceTypes"
                  label="Transfer From *"
                  outlined
                  dense
                  prepend-inner-icon="mdi-source-branch"
                  :rules="[v => !!v || 'Required']"
                ></v-select>

                <!-- Inventory Selection (if from inventory) -->
                <v-select
                  v-if="transfer.sourceType === 'INVENTORY'"
                  v-model="transfer.inventoryItemId"
                  :items="inventoryItems"
                  item-text="displayName"
                  item-value="id"
                  label="Select Inventory Item *"
                  outlined
                  dense
                  prepend-inner-icon="mdi-warehouse"
                  :loading="loadingInventory"
                  :rules="[v => !!v || 'Required']"
                ></v-select>

                <!-- Production Unit Selection (if from production unit - for farmers) -->
                <v-select
                  v-if="transfer.sourceType === 'PRODUCTION_UNIT'"
                  v-model="transfer.productionUnitId"
                  :items="productionUnits"
                  item-text="displayName"
                  item-value="id"
                  label="Select Production Unit *"
                  outlined
                  dense
                  prepend-inner-icon="mdi-map-marker"
                  :loading="loadingUnits"
                  :rules="[v => !!v || 'Required']"
                ></v-select>

                <v-row>
                  <v-col cols="12" md="6">
                    <v-combobox
                      v-model="transfer.produceType"
                      :items="produceTypes"
                      label="Produce Type *"
                      outlined
                      dense
                      prepend-inner-icon="mdi-leaf"
                      :rules="[v => !!v || 'Required']"
                    ></v-combobox>
                  </v-col>
                  <v-col cols="12" md="6">
                    <v-text-field
                      v-model.number="transfer.quantityKg"
                      label="Quantity (kg) *"
                      type="number"
                      outlined
                      dense
                      prepend-inner-icon="mdi-weight-kilogram"
                      :rules="[v => v > 0 || 'Must be greater than 0']"
                    ></v-text-field>
                  </v-col>
                </v-row>

                <v-row>
                  <v-col cols="12" md="6">
                    <v-select
                      v-model="transfer.qualityGrade"
                      :items="['A', 'B', 'C']"
                      label="Quality Grade"
                      outlined
                      dense
                      prepend-inner-icon="mdi-star"
                    ></v-select>
                  </v-col>
                  <v-col cols="12" md="6">
                    <v-text-field
                      v-model="transfer.transferDate"
                      label="Transfer Date"
                      type="date"
                      outlined
                      dense
                      prepend-inner-icon="mdi-calendar"
                    ></v-text-field>
                  </v-col>
                </v-row>

                <v-textarea
                  v-model="transfer.notes"
                  label="Notes (optional)"
                  outlined
                  dense
                  rows="2"
                  prepend-inner-icon="mdi-note-text"
                ></v-textarea>
              </v-form>
            </v-card-text>
            <v-card-actions class="tw-px-6 tw-pb-6">
              <v-spacer></v-spacer>
              <v-btn text @click="resetForm">Clear</v-btn>
              <v-btn
                color="primary"
                large
                :loading="submitting"
                :disabled="!formValid"
                @click="submitTransfer"
              >
                <v-icon left>mdi-send</v-icon>
                Send Transfer Request
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>

        <!-- Info Panel -->
        <v-col cols="12" md="5">
          <v-card class="tw-mb-4">
            <v-card-title class="tw-text-base">
              <v-icon color="info" class="tw-mr-2">mdi-information</v-icon>
              How It Works
            </v-card-title>
            <v-card-text>
              <v-timeline dense>
                <v-timeline-item color="blue" small>
                  <div class="tw-font-medium">1. You Send</div>
                  <div class="tw-text-sm tw-text-slate-500">Create a transfer request with quantity and details</div>
                </v-timeline-item>
                <v-timeline-item color="orange" small>
                  <div class="tw-font-medium">2. They Confirm</div>
                  <div class="tw-text-sm tw-text-slate-500">Recipient reviews and confirms receipt</div>
                </v-timeline-item>
                <v-timeline-item color="green" small>
                  <div class="tw-font-medium">3. Recorded on Hedera</div>
                  <div class="tw-text-sm tw-text-slate-500">Only after confirmation is data written immutably</div>
                </v-timeline-item>
              </v-timeline>

              <v-alert type="info" text dense class="tw-mt-4">
                <strong>Two-Party Verification:</strong> Both sender and receiver must agree before the transfer is finalized. This ensures data truthfulness.
              </v-alert>
            </v-card-text>
          </v-card>

          <!-- Recent Transfers -->
          <v-card>
            <v-card-title class="tw-text-base">
              <v-icon color="purple" class="tw-mr-2">mdi-history</v-icon>
              Recent Outgoing
            </v-card-title>
            <v-list dense v-if="recentTransfers.length > 0">
              <v-list-item v-for="t in recentTransfers" :key="t.id">
                <v-list-item-avatar>
                  <v-icon :color="getStatusColor(t.status)">mdi-package-variant</v-icon>
                </v-list-item-avatar>
                <v-list-item-content>
                  <v-list-item-title>{{ t.quantityKg }} kg to {{ t.recipientName }}</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip x-small :color="getStatusColor(t.status)" text-color="white">
                      {{ t.status }}
                    </v-chip>
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-list>
            <v-card-text v-else class="tw-text-center tw-text-slate-400">
              No recent transfers
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'SendToSupplier',
  components: { Default },
  data() {
    return {
      formValid: false,
      submitting: false,
      loadingSuppliers: false,
      loadingInventory: false,
      loadingUnits: false,
      connectedSuppliers: [],
      inventoryItems: [],
      productionUnits: [],
      recentTransfers: [],
      transfer: {
        toSupplierId: null,
        sourceType: 'INVENTORY',
        inventoryItemId: null,
        productionUnitId: null,
        produceType: '',
        quantityKg: null,
        qualityGrade: 'A',
        transferDate: new Date().toISOString().split('T')[0],
        notes: '',
      },
      sourceTypes: [
        { text: 'From My Inventory', value: 'INVENTORY' },
        { text: 'From Production Unit', value: 'PRODUCTION_UNIT' },
      ],
      produceTypes: [
        'Coffee Arabica',
        'Coffee Robusta',
        'Tea',
        'Macadamia',
        'Avocado',
        'Cocoa',
      ],
    };
  },
  computed: {
    currentUserId() {
      return this.$store.state.user?.entityId || this.$store.state.auth?.user?.id;
    },
  },
  mounted() {
    this.loadConnectedSuppliers();
    this.loadInventory();
    this.loadRecentTransfers();
  },
  methods: {
    async loadConnectedSuppliers() {
      this.loadingSuppliers = true;
      try {
        const response = await axios.get(`/api/v1/supply-chain/supplier/${this.currentUserId}/connections`);
        this.connectedSuppliers = (response.data || []).map((s) => ({
          ...s,
          displayName: `${s.supplierName} (${s.supplierType})`,
        }));
      } catch (error) {
        this.$toast.error('Failed to fetch connected suppliers:', error.message);
        this.connectedSuppliers = [];
      } finally {
        this.loadingSuppliers = false;
      }
    },

    async loadInventory() {
      this.loadingInventory = true;
      try {
        const response = await axios.get(`/api/v1/suppliers/${this.currentUserId}/inventory`);
        this.inventoryItems = (response.data || []).map((i) => ({
          ...i,
          displayName: `${i.produceType} - ${i.availableKg} kg (Batch: ${i.batchCode})`,
        }));
      } catch (error) {
        this.$toast.error('Failed to fetch inventory:', error.message);
        this.inventoryItems = [];
      } finally {
        this.loadingInventory = false;
      }
    },

    async loadRecentTransfers() {
      try {
        const response = await axios.get(`/api/v1/transfers/outgoing/${this.currentUserId}?limit=5`);
        this.recentTransfers = response.data?.content || response.data || [];
      } catch (error) {
        this.recentTransfers = [];
      }
    },

    async submitTransfer() {
      if (!this.$refs.transferForm.validate()) return;

      this.submitting = true;
      try {
        const payload = {
          fromSupplierId: this.currentUserId,
          toSupplierId: this.transfer.toSupplierId,
          produceType: this.transfer.produceType,
          quantityKg: this.transfer.quantityKg,
          qualityGrade: this.transfer.qualityGrade,
          transferDate: this.transfer.transferDate,
          notes: this.transfer.notes,
          sourceType: this.transfer.sourceType,
          sourceId: this.transfer.sourceType === 'INVENTORY'
            ? this.transfer.inventoryItemId
            : this.transfer.productionUnitId,
        };

        await axios.post('/api/v1/transfers', payload);
        this.$toast.success('Transfer request sent! Waiting for recipient confirmation.');
        this.resetForm();
        await this.loadRecentTransfers();
      } catch (error) {
        this.$toast.error('Failed to create transfer request');
      } finally {
        this.submitting = false;
      }
    },

    resetForm() {
      this.transfer = {
        toSupplierId: null,
        sourceType: 'INVENTORY',
        inventoryItemId: null,
        productionUnitId: null,
        produceType: '',
        quantityKg: null,
        qualityGrade: 'A',
        transferDate: new Date().toISOString().split('T')[0],
        notes: '',
      };
      // eslint-disable-next-line no-unused-expressions
      this.$refs.transferForm?.resetValidation();
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
  },
};
</script>
