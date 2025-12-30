<template>
  <div class="tw-bg-white tw-rounded-xl tw-border tw-border-gray-200">
    <!-- Header -->
    <div class="tw-p-4 tw-border-b tw-border-gray-100 tw-flex tw-items-center tw-justify-between">
      <div class="tw-flex tw-items-center tw-gap-3">
        <div class="tw-w-10 tw-h-10 tw-rounded-full tw-bg-blue-100 tw-flex tw-items-center tw-justify-center">
          <v-icon color="blue">mdi-basket</v-icon>
        </div>
        <div>
          <h3 class="tw-font-semibold tw-text-gray-900">Collection Events</h3>
          <p class="tw-text-sm tw-text-gray-500">Record produce collection from linked production units</p>
        </div>
      </div>
      <v-btn color="primary" depressed @click="showAddDialog = true">
        <v-icon left>mdi-plus</v-icon>
        Add Collection
      </v-btn>
    </div>

    <!-- Summary Stats -->
    <div class="tw-grid tw-grid-cols-4 tw-gap-4 tw-p-4 tw-bg-gray-50 tw-border-b">
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-blue-600">{{ collectionEvents.length }}</p>
        <p class="tw-text-xs tw-text-gray-500">Collections</p>
      </div>
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-green-600">{{ totalQuantity.toFixed(0) }}</p>
        <p class="tw-text-xs tw-text-gray-500">Total kg</p>
      </div>
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-orange-600">{{ uniqueFarmers }}</p>
        <p class="tw-text-xs tw-text-gray-500">Farmers</p>
      </div>
      <div class="tw-text-center">
        <p class="tw-text-2xl tw-font-bold tw-text-purple-600">{{ uniqueCollectors }}</p>
        <p class="tw-text-xs tw-text-gray-500">Collectors</p>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="tw-p-8 tw-text-center">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
      <p class="tw-text-gray-500 tw-mt-2">Loading collection events...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="collectionEvents.length === 0" class="tw-p-8 tw-text-center">
      <v-icon size="48" color="grey lighten-2">mdi-basket-outline</v-icon>
      <p class="tw-text-gray-500 tw-mt-2">No collection events yet</p>
      <p class="tw-text-sm tw-text-gray-400">Click "Add Collection" to record produce collection from farmers</p>
    </div>

    <!-- Collection Events List -->
    <div v-else class="tw-divide-y tw-divide-gray-100">
      <div
        v-for="event in collectionEvents"
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
                {{ event.farmerName || 'Unknown Farmer' }}
                <span class="tw-text-gray-400 tw-mx-2">→</span>
                {{ event.aggregatorName || 'Unknown Aggregator' }}
              </p>
              <p class="tw-text-sm tw-text-gray-500">
                {{ event.productionUnitName || 'Unknown Unit' }}
                <span v-if="event.qualityGrade" class="tw-ml-2">
                  <v-chip x-small :color="getQualityColor(event.qualityGrade)">
                    Grade {{ event.qualityGrade }}
                  </v-chip>
                </span>
              </p>
              <p class="tw-text-xs tw-text-gray-400 tw-mt-1">
                Collected on {{ formatDate(event.collectionDate) }}
              </p>
            </div>
          </div>
          <div class="tw-text-right">
            <p class="tw-text-lg tw-font-semibold tw-text-gray-900">{{ event.quantityCollectedKg.toFixed(1) }} kg</p>
            <v-chip v-if="event.hederaHash" x-small color="purple" text-color="white">
              <v-icon left x-small>mdi-check-decagram</v-icon>
              Blockchain
            </v-chip>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Collection Dialog -->
    <v-dialog v-model="showAddDialog" max-width="500" persistent>
      <v-card>
        <v-card-title class="tw-text-lg">
          <v-icon left color="blue">mdi-basket-plus</v-icon>
          Record Collection Event
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text class="tw-pt-4">
          <v-form ref="collectionForm" v-model="formValid">
            <v-select
              v-model="newEvent.productionUnitId"
              :items="linkedProductionUnits"
              item-text="name"
              item-value="id"
              label="Production Unit (Farm)"
              outlined
              dense
              :rules="[rules.required]"
              prepend-inner-icon="mdi-sprout"
              @change="onProductionUnitChange"
            ></v-select>

            <v-select
              v-model="newEvent.collectorSupplierId"
              :items="collectorSuppliers"
              item-text="displayName"
              item-value="id"
              label="Collector (Aggregator/Trader/Cooperative)"
              outlined
              dense
              :rules="[rules.required]"
              prepend-inner-icon="mdi-account-group"
              :hint="selectedCollectorHint"
              persistent-hint
            >
              <template #item="{ item }">
                <div class="tw-flex tw-items-center tw-gap-2">
                  <v-chip x-small :color="getSupplierTypeColor(item.supplierType)">
                    {{ item.supplierType }}
                  </v-chip>
                  <span>{{ item.displayName }}</span>
                </div>
              </template>
            </v-select>

            <v-text-field
              v-model="newEvent.quantityCollectedKg"
              label="Quantity Collected (kg)"
              type="number"
              outlined
              dense
              :rules="[rules.required, rules.positive]"
              prepend-inner-icon="mdi-weight-kilogram"
              suffix="kg"
            ></v-text-field>

            <v-text-field
              v-model="newEvent.collectionDate"
              label="Collection Date"
              type="date"
              outlined
              dense
              :rules="[rules.required]"
              prepend-inner-icon="mdi-calendar"
            ></v-text-field>

            <v-select
              v-model="newEvent.qualityGrade"
              :items="qualityGrades"
              label="Quality Grade"
              outlined
              dense
              prepend-inner-icon="mdi-star"
            ></v-select>

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
            @click="saveCollectionEvent"
          >
            Save Collection
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'CollectionEventsPanel',

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

      collectionEvents: [],
      linkedProductionUnits: [],
      collectorSuppliers: [], // Suppliers that can act as collectors (aggregators, traders, cooperatives, etc.)

      newEvent: {
        productionUnitId: null,
        collectorSupplierId: null,
        quantityCollectedKg: null,
        collectionDate: new Date().toISOString().split('T')[0],
        qualityGrade: null,
        notes: '',
      },

      qualityGrades: ['A', 'B', 'C', 'D'],

      rules: {
        required: (v) => !!v || 'Required',
        positive: (v) => (v && parseFloat(v) > 0) || 'Must be greater than 0',
      },
    };
  },

  computed: {
    effectiveExporterId() {
      return this.exporterId || this.$store.state.auth?.user?.id;
    },

    totalQuantity() {
      return this.collectionEvents.reduce((sum, e) => sum + (e.quantityCollectedKg || 0), 0);
    },

    uniqueFarmers() {
      const farmerIds = this.collectionEvents.map((e) => e.farmerId).filter(Boolean);
      return new Set(farmerIds).size;
    },

    uniqueCollectors() {
      // Count unique collectors (aggregators, traders, etc.)
      const collectorIds = this.collectionEvents.map((e) => e.aggregatorId || e.collectorSupplierId).filter(Boolean);
      return new Set(collectorIds).size;
    },

    selectedCollectorHint() {
      if (!this.newEvent.collectorSupplierId) return '';
      const supplier = this.collectorSuppliers.find((s) => s.id === this.newEvent.collectorSupplierId);
      return supplier ? `${supplier.supplierType} - ${supplier.region || supplier.countryCode || 'No location'}` : '';
    },
  },

  async mounted() {
    await Promise.all([
      this.loadCollectionEvents(),
      this.loadLinkedProductionUnits(),
      this.loadCollectorSuppliers(),
    ]);
  },

  methods: {
    async loadCollectionEvents() {
      this.loading = true;
      try {
        const response = await axios.get(`/api/v1/supply-chain/workflows/${this.workflowId}/collection`);
        this.collectionEvents = response.data || [];
      } catch (error) {
        this.$toast.error('Failed to load collection events:', error.message);
      } finally {
        this.loading = false;
      }
    },

    async loadLinkedProductionUnits() {
      try {
        const response = await axios.get(`/api/v1/supply-chain/workflows/${this.workflowId}/production-units`);
        // API returns { success: true, data: [...], count: X }
        const units = response.data?.data || response.data || [];
        this.linkedProductionUnits = units.map((wu) => ({
          id: wu.productionUnitId,
          name: wu.productionUnitName || `Unit ${wu.productionUnitId}`,
          farmerId: wu.farmerId,
          farmerName: wu.farmerName,
        }));
      } catch (error) {
        this.$toast.error('Failed to load linked production units:', error.message);
      }
    },

    async loadCollectorSuppliers() {
      try {
        // Load suppliers connected to this exporter (aggregators, traders, cooperatives, etc.)
        const response = await axios.get(`/api/v1/supply-chain/exporter/${this.effectiveExporterId}/connected-suppliers`);
        const suppliers = response.data?.data || response.data || [];

        // Filter to only show supplier types that can act as collectors
        const collectorTypes = ['AGGREGATOR', 'TRADER', 'COOPERATIVE', 'FARMER_GROUP', 'WAREHOUSE'];
        this.collectorSuppliers = suppliers
          .filter((s) => collectorTypes.includes(s.supplierType) || !s.supplierType) // Include if type matches or no type
          .map((s) => ({
            id: s.id,
            displayName: s.supplierName || s.name || `Supplier ${s.id}`,
            supplierType: s.supplierType || 'AGGREGATOR',
            region: s.region,
            countryCode: s.countryCode,
          }));

        // If no connected suppliers found, also try to get all suppliers
        if (this.collectorSuppliers.length === 0) {
          const allSuppliersResponse = await axios.get('/api/v1/supply-chain/suppliers', {
            params: { isActive: true },
          });
          const allSuppliers = allSuppliersResponse.data?.content || allSuppliersResponse.data || [];
          this.collectorSuppliers = allSuppliers
            .filter((s) => collectorTypes.includes(s.supplierType))
            .map((s) => ({
              id: s.id,
              displayName: s.supplierName || s.name || `Supplier ${s.id}`,
              supplierType: s.supplierType || 'AGGREGATOR',
              region: s.region,
              countryCode: s.countryCode,
            }));
        }
      } catch (error) {
        this.$toast.error('Failed to load collector suppliers:', error.message);
        // Don't show error to user, just log it - they may need to add suppliers first
      }
    },

    onProductionUnitChange(productionUnitId) {
      const unit = this.linkedProductionUnits.find((u) => u.id === productionUnitId);
      if (unit) {
        this.newEvent.farmerId = unit.farmerId;
      }
    },

    async saveCollectionEvent() {
      if (!this.$refs.collectionForm.validate()) return;

      this.saving = true;
      try {
        const unit = this.linkedProductionUnits.find((u) => u.id === this.newEvent.productionUnitId);

        // Convert date to LocalDateTime format (add time component)
        const collectionDateTime = `${this.newEvent.collectionDate}T00:00:00`;

        await axios.post(`/api/v1/supply-chain/workflows/${this.workflowId}/collection`, {
          productionUnitId: this.newEvent.productionUnitId,
          farmerId: unit?.farmerId,
          aggregatorId: this.newEvent.collectorSupplierId, // Backend still uses aggregatorId
          quantityCollectedKg: parseFloat(this.newEvent.quantityCollectedKg),
          collectionDate: collectionDateTime,
          qualityGrade: this.newEvent.qualityGrade,
          notes: this.newEvent.notes,
        });

        this.$emit('collection-added');
        await this.loadCollectionEvents();
        this.closeDialog();
      } catch (error) {
        this.$toast.error('Failed to save collection event:', error.message);
        this.$emit('error', error.response?.data?.message || 'Failed to save collection event');
      } finally {
        this.saving = false;
      }
    },

    closeDialog() {
      this.showAddDialog = false;
      this.newEvent = {
        productionUnitId: null,
        collectorSupplierId: null,
        quantityCollectedKg: null,
        collectionDate: new Date().toISOString().split('T')[0],
        qualityGrade: null,
        notes: '',
      };
      // eslint-disable-next-line no-unused-expressions
      this.$refs.collectionForm?.reset();
    },

    formatDate(dateString) {
      if (!dateString) return 'N/A';
      return new Date(dateString).toLocaleDateString('en-US', {
        month: 'short',
        day: 'numeric',
        year: 'numeric',
      });
    },

    getSupplierTypeColor(type) {
      const colors = {
        AGGREGATOR: 'blue',
        TRADER: 'orange',
        COOPERATIVE: 'green',
        FARMER_GROUP: 'teal',
        WAREHOUSE: 'purple',
        PROCESSOR: 'indigo',
      };
      return colors[type] || 'grey';
    },

    getQualityColor(grade) {
      const colors = {
        A: 'success', B: 'info', C: 'warning', D: 'error',
      };
      return colors[grade] || 'grey';
    },
  },
};
</script>
