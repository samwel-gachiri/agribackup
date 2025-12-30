<template>
  <div class="tw-bg-white tw-rounded-xl tw-border tw-border-gray-200">
    <!-- Header -->
    <div class="tw-p-4 tw-border-b tw-border-gray-100 tw-flex tw-items-center tw-justify-between">
      <div class="tw-flex tw-items-center tw-gap-3">
        <div class="tw-w-10 tw-h-10 tw-rounded-full tw-bg-orange-100 tw-flex tw-items-center tw-justify-center">
          <v-icon color="orange">mdi-package-variant</v-icon>
        </div>
        <div>
          <h3 class="tw-font-semibold tw-text-gray-900">Consolidation Events</h3>
          <p class="tw-text-sm tw-text-gray-500">Move commodities between suppliers in the supply chain</p>
        </div>
      </div>
      <v-btn color="primary" depressed @click="openAddDialog">
        <v-icon left>mdi-plus</v-icon>
        Add Consolidation
      </v-btn>
    </div>

    <!-- Summary Stats -->
    <div class="tw-grid tw-grid-cols-4 tw-gap-4 tw-p-4 tw-bg-gray-50 tw-border-b">
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-orange-600">{{ consolidationEvents.length }}</p>
        <p class="tw-text-xs tw-text-gray-500">Transfers</p>
      </div>
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-green-600">{{ totalQuantity.toFixed(0) }}</p>
        <p class="tw-text-xs tw-text-gray-500">Total kg</p>
      </div>
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-blue-600">{{ uniqueSourceSuppliers }}</p>
        <p class="tw-text-xs tw-text-gray-500">Source Suppliers</p>
      </div>
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-purple-600">{{ uniqueTargetSuppliers }}</p>
        <p class="tw-text-xs tw-text-gray-500">Target Suppliers</p>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="tw-p-8 tw-text-center">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
      <p class="tw-text-gray-500 tw-mt-2">Loading consolidation events...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="consolidationEvents.length === 0" class="tw-p-8 tw-text-center">
      <v-icon size="48" color="grey lighten-2">mdi-package-variant-closed</v-icon>
      <p class="tw-text-gray-500 tw-mt-2">No consolidation events yet</p>
      <p class="tw-text-sm tw-text-gray-400">Click "Add Consolidation" to transfer commodities between suppliers</p>
      <p class="tw-text-xs tw-text-gray-400 tw-mt-2">
        Example: Move coffee from Aggregator A to Processor B for washing
      </p>
    </div>

    <!-- Consolidation Events List -->
    <div v-else class="tw-divide-y tw-divide-gray-100">
      <div
        v-for="event in consolidationEvents"
        :key="event.id"
        class="tw-p-4 hover:tw-bg-gray-50 tw-transition-colors"
      >
        <div class="tw-flex tw-items-start tw-justify-between">
          <div class="tw-flex tw-items-start tw-gap-3">
            <div class="tw-w-10 tw-h-10 tw-rounded-full tw-bg-green-100 tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
              <v-icon color="green">mdi-check</v-icon>
            </div>
            <div>
              <p class="tw-font-medium tw-text-gray-900">
                {{ event.aggregatorName || event.sourceSupplierName || 'Source Supplier' }}
                <v-icon small color="grey" class="tw-mx-1">mdi-arrow-right</v-icon>
                {{ event.processorName || event.targetSupplierName || 'Target Supplier' }}
              </p>
              <p v-if="event.batchNumber" class="tw-text-sm tw-text-gray-500">
                Batch: {{ event.batchNumber }}
              </p>
              <p v-if="event.transportDetails" class="tw-text-xs tw-text-gray-400 tw-mt-1">
                <v-icon x-small class="tw-mr-1">mdi-truck</v-icon>
                {{ event.transportDetails }}
              </p>
              <p class="tw-text-xs tw-text-gray-400 tw-mt-1">
                Transferred on {{ formatDate(event.consolidationDate) }}
              </p>
            </div>
          </div>
          <div class="tw-text-right">
            <p class="tw-text-lg tw-font-semibold tw-text-gray-900">{{ parseFloat(event.quantitySentKg).toFixed(1) }} kg</p>
            <v-chip v-if="event.hederaHash || event.hederaTransactionId" x-small color="purple" text-color="white">
              <v-icon left x-small>mdi-check-decagram</v-icon>
              Blockchain
            </v-chip>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Consolidation Dialog -->
    <v-dialog v-model="showAddDialog" max-width="600" persistent>
      <v-card>
        <v-card-title class="tw-text-lg">
          <v-icon left color="orange">mdi-package-variant</v-icon>
          Record Consolidation Event
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text class="tw-pt-4">
          <v-alert type="info" dense text class="tw-mb-4">
            <strong>Consolidation</strong> records the movement of commodities from one supplier to another in the supply chain.
          </v-alert>

          <v-form ref="consolidationForm" v-model="formValid">
            <!-- Source Supplier -->
            <v-select
              v-model="newEvent.aggregatorId"
              :items="sourceSuppliers"
              item-text="displayName"
              item-value="id"
              label="Source Supplier (From)"
              outlined
              dense
              :rules="[rules.required]"
              prepend-inner-icon="mdi-account-arrow-right"
              @change="onSourceSupplierChange"
            >
              <template #item="{ item }">
                <div class="tw-flex tw-items-center tw-justify-between tw-w-full">
                  <div class="tw-flex tw-items-center tw-gap-2">
                    <v-chip x-small :color="getSupplierTypeColor(item.supplierType)">
                      {{ formatSupplierType(item.supplierType) }}
                    </v-chip>
                    <span>{{ item.displayName }}</span>
                  </div>
                  <span class="tw-text-xs tw-text-gray-500">{{ item.availableQuantity.toFixed(0) }} kg avail.</span>
                </div>
              </template>
              <template #selection="{ item }">
                <v-chip x-small :color="getSupplierTypeColor(item.supplierType)" class="tw-mr-2">
                  {{ formatSupplierType(item.supplierType) }}
                </v-chip>
                {{ item.displayName }} ({{ item.availableQuantity.toFixed(0) }} kg avail.)
              </template>
            </v-select>

            <!-- Available Quantity Display -->
            <v-alert
              v-if="selectedSourceSupplier"
              type="info"
              text
              dense
              class="tw-mb-4"
            >
              Available for transfer: <strong>{{ selectedSourceSupplier.availableQuantity.toFixed(2) }} kg</strong>
            </v-alert>

            <!-- Target Supplier -->
            <v-select
              v-model="newEvent.processorId"
              :items="targetSuppliers"
              item-text="displayName"
              item-value="id"
              label="Target Supplier (To)"
              outlined
              dense
              :rules="[rules.required, rules.notSameAsSource]"
              prepend-inner-icon="mdi-account-arrow-left"
              :disabled="!newEvent.aggregatorId"
            >
              <template #item="{ item }">
                <div class="tw-flex tw-items-center tw-gap-2">
                  <v-chip x-small :color="getSupplierTypeColor(item.supplierType)">
                    {{ formatSupplierType(item.supplierType) }}
                  </v-chip>
                  <span>{{ item.displayName }}</span>
                </div>
              </template>
              <template #selection="{ item }">
                <v-chip x-small :color="getSupplierTypeColor(item.supplierType)" class="tw-mr-2">
                  {{ formatSupplierType(item.supplierType) }}
                </v-chip>
                {{ item.displayName }}
              </template>
            </v-select>

            <!-- Quantity -->
            <v-text-field
              v-model="newEvent.quantitySentKg"
              label="Quantity to Transfer (kg)"
              type="number"
              outlined
              dense
              :rules="[rules.required, rules.positive, rules.notExceedAvailable]"
              prepend-inner-icon="mdi-weight-kilogram"
              suffix="kg"
              :hint="quantityHint"
              persistent-hint
            ></v-text-field>

            <!-- Consolidation Date -->
            <v-text-field
              v-model="newEvent.consolidationDate"
              label="Transfer Date"
              type="date"
              outlined
              dense
              :rules="[rules.required]"
              prepend-inner-icon="mdi-calendar"
              class="tw-mt-4"
            ></v-text-field>

            <!-- Batch Number -->
            <v-text-field
              v-model="newEvent.batchNumber"
              label="Batch Number (Optional)"
              outlined
              dense
              prepend-inner-icon="mdi-barcode"
              :placeholder="suggestedBatchNumber"
            ></v-text-field>

            <!-- Transport Details -->
            <v-text-field
              v-model="newEvent.transportDetails"
              label="Transport Details (Optional)"
              outlined
              dense
              prepend-inner-icon="mdi-truck"
              placeholder="e.g., Truck ABC-123, Driver: John"
            ></v-text-field>

            <!-- Notes -->
            <v-textarea
              v-model="newEvent.notes"
              label="Notes (Optional)"
              outlined
              dense
              rows="2"
              prepend-inner-icon="mdi-note-text"
            ></v-textarea>
          </v-form>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions class="tw-px-4 tw-py-3">
          <v-spacer></v-spacer>
          <v-btn text @click="closeDialog">Cancel</v-btn>
          <v-btn
            color="primary"
            depressed
            :loading="saving"
            :disabled="!formValid"
            @click="saveConsolidationEvent"
          >
            <v-icon left>mdi-package-variant</v-icon>
            Save Transfer
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'ConsolidationEventsPanel',

  props: {
    workflowId: {
      type: String,
      required: true,
    },
    exporterId: {
      type: String,
      default: null,
    },
  },

  data() {
    return {
      loading: false,
      saving: false,
      showAddDialog: false,
      formValid: false,

      consolidationEvents: [],
      allSuppliers: [],
      supplierBalances: {}, // Map of supplierId -> available quantity

      newEvent: {
        aggregatorId: null, // Source supplier
        processorId: null, // Target supplier
        quantitySentKg: null,
        consolidationDate: new Date().toISOString().split('T')[0],
        transportDetails: '',
        batchNumber: '',
        notes: '',
      },

      rules: {
        required: (v) => !!v || 'Required',
        positive: (v) => (v && parseFloat(v) > 0) || 'Must be greater than 0',
        notSameAsSource: (v) => v !== this.newEvent.aggregatorId || 'Target must be different from source',
        notExceedAvailable: (v) => {
          if (!v || !this.selectedSourceSupplier) return true;
          return parseFloat(v) <= this.selectedSourceSupplier.availableQuantity
            || `Cannot exceed available quantity (${this.selectedSourceSupplier.availableQuantity.toFixed(2)} kg)`;
        },
      },
    };
  },

  computed: {
    effectiveExporterId() {
      return this.exporterId || this.$store.state.auth?.user?.id;
    },

    totalQuantity() {
      return this.consolidationEvents.reduce((sum, e) => sum + parseFloat(e.quantitySentKg || 0), 0);
    },

    uniqueSourceSuppliers() {
      const ids = this.consolidationEvents.map((e) => e.aggregatorId).filter(Boolean);
      return new Set(ids).size;
    },

    uniqueTargetSuppliers() {
      const ids = this.consolidationEvents.map((e) => e.processorId).filter(Boolean);
      return new Set(ids).size;
    },

    sourceSuppliers() {
      // Suppliers that have collected commodities (have positive balance)
      return this.allSuppliers
        .filter((s) => (this.supplierBalances[s.id] || 0) > 0)
        .map((s) => ({
          ...s,
          displayName: s.supplierName || s.name || `Supplier ${s.id}`,
          availableQuantity: this.supplierBalances[s.id] || 0,
        }))
        .sort((a, b) => b.availableQuantity - a.availableQuantity);
    },

    targetSuppliers() {
      // All suppliers except the selected source
      return this.allSuppliers
        .filter((s) => s.id !== this.newEvent.aggregatorId)
        .map((s) => ({
          ...s,
          displayName: s.supplierName || s.name || `Supplier ${s.id}`,
        }));
    },

    selectedSourceSupplier() {
      if (!this.newEvent.aggregatorId) return null;
      return this.sourceSuppliers.find((s) => s.id === this.newEvent.aggregatorId);
    },

    quantityHint() {
      if (!this.selectedSourceSupplier) return '';
      return `Maximum: ${this.selectedSourceSupplier.availableQuantity.toFixed(2)} kg`;
    },

    suggestedBatchNumber() {
      const now = new Date();
      const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '');
      const random = Math.random().toString(36).substring(2, 6).toUpperCase();
      return `CONS-${dateStr}-${random}`;
    },
  },

  async mounted() {
    await this.loadData();
  },

  methods: {
    async loadData() {
      this.loading = true;
      try {
        await Promise.all([
          this.loadConsolidationEvents(),
          this.loadSuppliers(),
          this.loadSupplierBalances(),
        ]);
      } finally {
        this.loading = false;
      }
    },

    async loadConsolidationEvents() {
      try {
        const response = await axios.get(`/api/v1/supply-chain/workflows/${this.workflowId}/consolidation`);
        this.consolidationEvents = response.data || [];
      } catch (error) {
        this.$toast.error('Failed to load consolidation events:', error.message);
      }
    },

    async loadSuppliers() {
      try {
        // First try to get connected suppliers for the exporter
        const connectedResponse = await axios.get(`/api/v1/supply-chain/exporter/${this.effectiveExporterId}/connected-suppliers`);
        let suppliers = connectedResponse.data?.data || connectedResponse.data || [];

        // If no connected suppliers, get all active suppliers
        if (suppliers.length === 0) {
          const allResponse = await axios.get('/api/v1/supply-chain/suppliers', {
            params: { isActive: true, size: 200 },
          });
          suppliers = allResponse.data?.content || allResponse.data || [];
        }

        this.allSuppliers = suppliers;
      } catch (error) {
        this.$toast.error('Failed to load suppliers:', error.message);
      }
    },

    async loadSupplierBalances() {
      try {
        // Get collection events to calculate balances
        const collectionsResponse = await axios.get(`/api/v1/supply-chain/workflows/${this.workflowId}/collection`);
        const collections = collectionsResponse.data || [];

        // Get existing consolidation events
        const consolidations = this.consolidationEvents;

        // Calculate balance for each collector/supplier
        const balances = {};

        // Add collected quantities
        collections.forEach((c) => {
          const collectorId = c.aggregatorId || c.collectorSupplierId;
          if (collectorId) {
            balances[collectorId] = (balances[collectorId] || 0) + parseFloat(c.quantityCollectedKg || 0);
          }
        });

        // Subtract sent quantities (source loses quantity)
        consolidations.forEach((c) => {
          const sourceId = c.aggregatorId;
          if (sourceId) {
            balances[sourceId] = (balances[sourceId] || 0) - parseFloat(c.quantitySentKg || 0);
          }
        });

        // Add received quantities (target gains quantity)
        consolidations.forEach((c) => {
          const targetId = c.processorId;
          if (targetId) {
            balances[targetId] = (balances[targetId] || 0) + parseFloat(c.quantitySentKg || 0);
          }
        });

        this.supplierBalances = balances;
      } catch (error) {
        this.$toast.error('Failed to calculate supplier balances:', error.message);
      }
    },

    openAddDialog() {
      this.resetForm();
      this.showAddDialog = true;
    },

    onSourceSupplierChange() {
      // Reset target and quantity when source changes
      this.newEvent.processorId = null;
      this.newEvent.quantitySentKg = null;
    },

    async saveConsolidationEvent() {
      if (!this.$refs.consolidationForm.validate()) return;

      this.saving = true;
      try {
        // Convert date to LocalDateTime format
        const consolidationDateTime = `${this.newEvent.consolidationDate}T00:00:00`;

        await axios.post(`/api/v1/supply-chain/workflows/${this.workflowId}/consolidation`, {
          aggregatorId: this.newEvent.aggregatorId, // Backend expects aggregatorId for source
          processorId: this.newEvent.processorId, // Backend expects processorId for target
          quantitySentKg: parseFloat(this.newEvent.quantitySentKg),
          consolidationDate: consolidationDateTime,
          transportDetails: this.newEvent.transportDetails || null,
          batchNumber: this.newEvent.batchNumber || this.suggestedBatchNumber,
          notes: this.newEvent.notes || null,
        });

        this.$emit('consolidation-added');
        await this.loadData();
        this.closeDialog();
      } catch (error) {
        this.$emit('error', error.response?.data?.message || 'Failed to save consolidation event');
      } finally {
        this.saving = false;
      }
    },

    closeDialog() {
      this.showAddDialog = false;
      this.resetForm();
    },

    resetForm() {
      this.newEvent = {
        aggregatorId: null,
        processorId: null,
        quantitySentKg: null,
        consolidationDate: new Date().toISOString().split('T')[0],
        transportDetails: '',
        batchNumber: '',
        notes: '',
      };
      if (this.$refs.consolidationForm) {
        this.$refs.consolidationForm.resetValidation();
      }
    },

    formatDate(dateString) {
      if (!dateString) return 'N/A';
      return new Date(dateString).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    },

    formatSupplierType(type) {
      if (!type) return 'Supplier';
      return type
        .replace(/_/g, ' ')
        .toLowerCase()
        .split(' ')
        .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
    },

    getSupplierTypeColor(type) {
      const colors = {
        AGGREGATOR: 'orange',
        TRADER: 'amber darken-2',
        COOPERATIVE: 'teal',
        FARMER_GROUP: 'green',
        PROCESSOR: 'purple',
        WAREHOUSE: 'brown',
        EXPORTER: 'blue',
        IMPORTER: 'indigo',
        DISTRIBUTOR: 'cyan',
      };
      return colors[type] || 'grey';
    },
  },
};
</script>
