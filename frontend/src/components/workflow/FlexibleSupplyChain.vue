<template>
  <div class="tw-space-y-6">
    <!-- Header with Add Supplier Button -->
    <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
      <div>
        <h3 class="tw-text-lg tw-font-semibold tw-text-gray-900">Supply Chain Flow</h3>
        <p class="tw-text-sm tw-text-gray-500">
          Build a flexible supply chain from source to destination
        </p>
      </div>
      <v-btn small color="primary" @click="openAddSupplierDialog">
        <v-icon left small>mdi-plus</v-icon>
        Add Supplier
      </v-btn>
    </div>

    <!-- Supply Chain Visual Flow -->
    <div class="tw-bg-gray-50 tw-rounded-xl tw-p-6 tw-border tw-border-gray-200">
      <!-- Empty State -->
      <div v-if="!chainNodes.length" class="tw-text-center tw-py-12">
        <v-icon size="64" color="grey lighten-2">mdi-transit-connection-variant</v-icon>
        <p class="tw-text-gray-500 tw-mt-4">No supply chain built yet</p>
        <p class="tw-text-gray-400 tw-text-sm tw-mt-2">
          Click "Add Supplier" to start building your supply chain
        </p>
      </div>

      <!-- Flow Visualization -->
      <div v-else class="tw-flex tw-flex-wrap tw-items-center tw-gap-4">
        <!-- Chain Nodes with Arrows -->
        <div
          v-for="(node, index) in chainNodes"
          :key="'node-' + node.id"
          class="tw-flex tw-items-center tw-gap-4"
        >
          <!-- Node Card -->
          <div
            class="tw-bg-white tw-rounded-lg tw-p-4 tw-border-2 tw-shadow-sm tw-transition-all tw-min-w-[200px]"
            :class="{
              'tw-border-blue-500 tw-bg-blue-50': selectedNodeId === node.id,
              [getSupplierBorderColor(node.supplierType)]: selectedNodeId !== node.id
            }"
            @click="selectNode(node)"
          >
            <!-- Node Header -->
            <div class="tw-flex tw-items-center tw-justify-between tw-mb-2">
              <v-chip
                x-small
                :color="getSupplierColor(node.supplierType)"
                text-color="white"
              >
                <v-icon x-small left>{{ getSupplierIcon(node.supplierType) }}</v-icon>
                {{ formatSupplierType(node.supplierType) }}
              </v-chip>
              <v-chip x-small outlined>{{ index + 1 }}</v-chip>
            </div>

            <!-- Supplier Name -->
            <p class="tw-text-sm tw-font-semibold tw-text-gray-900 tw-mb-1">
              {{ node.supplierName }}
            </p>

            <!-- Location -->
            <p v-if="node.region || node.countryCode" class="tw-text-xs tw-text-gray-500 tw-mb-2">
              <v-icon x-small class="tw-mr-1">mdi-map-marker</v-icon>
              {{ node.region || '' }} {{ node.countryCode }}
            </p>

            <!-- Connection Info -->
            <div v-if="node.connectionInfo" class="tw-mt-2 tw-pt-2 tw-border-t tw-border-gray-200">
              <div class="tw-text-xs tw-text-gray-600">
                <v-icon x-small color="green" class="tw-mr-1">mdi-package-variant</v-icon>
                {{ node.connectionInfo.quantityKg }} kg received
              </div>
              <div v-if="node.connectionInfo.hederaTransactionId" class="tw-flex tw-items-center tw-mt-1">
                <v-icon x-small color="purple" class="tw-mr-1">mdi-shield-check</v-icon>
                <span class="tw-text-xs tw-text-purple-600 tw-font-mono">
                  {{ node.connectionInfo.hederaTransactionId.substring(0, 12) }}...
                </span>
              </div>
            </div>

            <!-- Actions -->
            <div class="tw-flex tw-gap-2 tw-mt-3">
              <v-btn
                v-if="index < chainNodes.length - 1"
                x-small
                text
                color="blue"
                @click.stop="viewConnection(node, chainNodes[index + 1])"
              >
                <v-icon x-small>mdi-link</v-icon>
              </v-btn>
              <v-btn
                x-small
                text
                color="error"
                @click.stop="removeFromChain(node)"
              >
                <v-icon x-small>mdi-delete</v-icon>
              </v-btn>
            </div>
          </div>

          <!-- Arrow between nodes -->
          <div v-if="index < chainNodes.length - 1" class="tw-flex tw-items-center">
            <v-icon color="grey" class="tw-mx-2">mdi-arrow-right</v-icon>
          </div>
        </div>

        <!-- Add Next Supplier Button (at end of chain) -->
        <div
          v-if="chainNodes.length > 0"
          class="tw-border-2 tw-border-dashed tw-border-gray-300 tw-rounded-lg tw-p-4 tw-min-w-[150px] tw-text-center tw-cursor-pointer hover:tw-border-primary hover:tw-bg-gray-100 tw-transition-all"
          @click="openAddSupplierDialog"
        >
          <v-icon color="grey">mdi-plus</v-icon>
          <p class="tw-text-xs tw-text-gray-500 tw-mt-1">Add Next</p>
        </div>
      </div>
    </div>

    <!-- Available Suppliers Panel -->
    <div class="tw-bg-white tw-rounded-xl tw-p-6 tw-border tw-border-gray-200">
      <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
        <h4 class="tw-text-sm tw-font-semibold tw-text-gray-700">
          Available Suppliers
        </h4>
        <!-- Filter by Type -->
        <v-select
          v-model="supplierTypeFilter"
          :items="supplierTypeOptions"
          item-text="text"
          item-value="value"
          label="Filter by type"
          dense
          outlined
          hide-details
          class="tw-max-w-[200px]"
          clearable
        ></v-select>
      </div>

      <!-- Supplier Cards Grid -->
      <div class="tw-grid tw-grid-cols-2 md:tw-grid-cols-3 lg:tw-grid-cols-4 tw-gap-4">
        <div
          v-for="supplier in filteredSuppliers"
          :key="supplier.id"
          class="tw-bg-gray-50 tw-rounded-lg tw-p-3 tw-border tw-border-gray-200 tw-cursor-pointer hover:tw-border-primary hover:tw-bg-gray-100 tw-transition-all"
          :class="{ 'tw-opacity-50 tw-pointer-events-none': isInChain(supplier.id) }"
          @click="addToChain(supplier)"
        >
          <!-- Type Badge -->
          <v-chip
            x-small
            :color="getSupplierColor(supplier.supplierType)"
            text-color="white"
            class="tw-mb-2"
          >
            <v-icon x-small left>{{ getSupplierIcon(supplier.supplierType) }}</v-icon>
            {{ formatSupplierType(supplier.supplierType) }}
          </v-chip>

          <!-- Supplier Info -->
          <p class="tw-text-sm tw-font-medium tw-text-gray-900">{{ supplier.supplierName }}</p>
          <p class="tw-text-xs tw-text-gray-500">{{ supplier.region || supplier.countryCode }}</p>

          <!-- Verification Status -->
          <div class="tw-flex tw-items-center tw-mt-2">
            <v-icon
              x-small
              :color="supplier.verificationStatus === 'VERIFIED' ? 'success' : 'warning'"
              class="tw-mr-1"
            >
              {{ supplier.verificationStatus === 'VERIFIED' ? 'mdi-check-circle' : 'mdi-clock' }}
            </v-icon>
            <span class="tw-text-xs tw-text-gray-500">{{ supplier.verificationStatus }}</span>
          </div>

          <!-- Already in Chain Badge -->
          <v-chip
            v-if="isInChain(supplier.id)"
            x-small
            color="grey"
            class="tw-mt-2"
          >
            Already in chain
          </v-chip>
        </div>

        <!-- Empty State -->
        <div v-if="!filteredSuppliers.length" class="tw-col-span-full tw-text-center tw-py-8">
          <v-icon size="48" color="grey lighten-2">mdi-account-group</v-icon>
          <p class="tw-text-gray-400 tw-mt-2">No suppliers found</p>
        </div>
      </div>
    </div>

    <!-- Connection Dialog -->
    <v-dialog v-model="showConnectionDialog" max-width="600" persistent>
      <v-card class="tw-rounded-xl">
        <v-card-title class="tw-text-lg tw-font-medium tw-px-6 tw-py-4">
          <div>
            <div class="tw-flex tw-items-center tw-gap-2 tw-mb-2">
              <v-chip x-small :color="getSupplierColor(connectionSource?.supplierType)" text-color="white">
                {{ formatSupplierType(connectionSource?.supplierType) }}
              </v-chip>
              <v-icon small>mdi-arrow-right</v-icon>
              <v-chip x-small :color="getSupplierColor(connectionTarget?.supplierType)" text-color="white">
                {{ formatSupplierType(connectionTarget?.supplierType) }}
              </v-chip>
            </div>
            <div class="tw-text-base tw-text-gray-600">
              {{ connectionSource?.supplierName }} → {{ connectionTarget?.supplierName }}
            </div>
          </div>
        </v-card-title>
        <v-divider></v-divider>

        <v-card-text class="tw-px-6 tw-py-6">
          <v-form ref="connectionForm" v-model="connectionFormValid">
            <!-- Connection Type -->
            <v-select
              v-model="connectionData.connectionType"
              :items="connectionTypeOptions"
              item-text="text"
              item-value="value"
              label="Connection Type"
              :rules="[rules.required]"
              outlined
              dense
              class="tw-mb-4"
            ></v-select>

            <!-- Quantity -->
            <v-text-field
              v-model.number="connectionData.quantityKg"
              label="Quantity (kg)"
              type="number"
              :rules="[rules.required, rules.positive]"
              outlined
              dense
              class="tw-mb-4"
            ></v-text-field>

            <!-- Transfer Date -->
            <v-text-field
              v-model="connectionData.transferDate"
              label="Transfer Date"
              type="datetime-local"
              :rules="[rules.required]"
              outlined
              dense
              class="tw-mb-4"
            ></v-text-field>

            <!-- Batch Reference -->
            <v-text-field
              v-model="connectionData.batchReference"
              label="Batch Reference (Optional)"
              outlined
              dense
              class="tw-mb-4"
            ></v-text-field>

            <!-- Transport Method -->
            <v-select
              v-model="connectionData.transportMethod"
              :items="transportMethods"
              label="Transport Method (Optional)"
              outlined
              dense
              clearable
              class="tw-mb-4"
            ></v-select>

            <!-- Transport Document -->
            <v-text-field
              v-model="connectionData.transportDocumentRef"
              label="Transport Document Reference (Optional)"
              outlined
              dense
              class="tw-mb-4"
            ></v-text-field>

            <!-- Notes -->
            <v-textarea
              v-model="connectionData.notes"
              label="Notes (Optional)"
              rows="2"
              outlined
              dense
            ></v-textarea>
          </v-form>
        </v-card-text>

        <v-divider></v-divider>
        <v-card-actions class="tw-px-6 tw-py-4">
          <v-spacer></v-spacer>
          <v-btn text @click="closeConnectionDialog">Cancel</v-btn>
          <v-btn
            color="primary"
            :disabled="!connectionFormValid"
            :loading="creatingConnection"
            @click="createConnection"
          >
            <v-icon left small>mdi-link</v-icon>
            Create Connection
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="4000" top right>
      {{ snackbar.message }}
    </v-snackbar>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'FlexibleSupplyChain',
  props: {
    workflowId: {
      type: String,
      required: true,
    },
  },
  data() {
    return {
      loading: false,
      creatingConnection: false,

      // Chain nodes (ordered list of suppliers in the chain)
      chainNodes: [],

      // All available suppliers
      allSuppliers: [],

      // Selected node in the chain
      selectedNodeId: null,

      // Supplier type filter
      supplierTypeFilter: null,

      // Connection dialog
      showConnectionDialog: false,
      connectionSource: null,
      connectionTarget: null,
      connectionFormValid: false,
      connectionData: {
        connectionType: 'TRANSFER',
        quantityKg: null,
        transferDate: null,
        batchReference: '',
        transportMethod: null,
        transportDocumentRef: '',
        notes: '',
      },

      // Options
      supplierTypeOptions: [
        { text: 'All Types', value: null },
        { text: 'Farmer', value: 'FARMER' },
        { text: 'Farmer Group', value: 'FARMER_GROUP' },
        { text: 'Cooperative', value: 'COOPERATIVE' },
        { text: 'Aggregator', value: 'AGGREGATOR' },
        { text: 'Trader', value: 'TRADER' },
        { text: 'Processor', value: 'PROCESSOR' },
        { text: 'Distributor', value: 'DISTRIBUTOR' },
        { text: 'Exporter', value: 'EXPORTER' },
        { text: 'Importer', value: 'IMPORTER' },
        { text: 'Wholesaler', value: 'WHOLESALER' },
        { text: 'Retailer', value: 'RETAILER' },
        { text: 'Warehouse', value: 'WAREHOUSE' },
        { text: 'Transporter', value: 'TRANSPORTER' },
      ],

      connectionTypeOptions: [
        { text: 'Harvest', value: 'HARVEST' },
        { text: 'Collection', value: 'COLLECTION' },
        { text: 'Aggregation', value: 'AGGREGATION' },
        { text: 'Transport', value: 'TRANSPORT' },
        { text: 'Storage', value: 'STORAGE' },
        { text: 'Processing', value: 'PROCESSING' },
        { text: 'Quality Check', value: 'QUALITY_CHECK' },
        { text: 'Consolidation', value: 'CONSOLIDATION' },
        { text: 'Export', value: 'EXPORT' },
        { text: 'Import', value: 'IMPORT' },
        { text: 'Distribution', value: 'DISTRIBUTION' },
        { text: 'Delivery', value: 'DELIVERY' },
      ],

      transportMethods: [
        'Road - Truck',
        'Road - Van',
        'Rail - Freight',
        'Sea - Container',
        'Sea - Bulk',
        'Air - Cargo',
        'Mixed Transport',
      ],

      rules: {
        required: (v) => !!v || 'Required',
        positive: (v) => v > 0 || 'Must be greater than 0',
      },

      snackbar: {
        show: false,
        message: '',
        color: 'success',
      },
    };
  },

  computed: {
    filteredSuppliers() {
      let suppliers = this.allSuppliers;

      if (this.supplierTypeFilter) {
        suppliers = suppliers.filter((s) => s.supplierType === this.supplierTypeFilter);
      }

      // Sort: verified first, then by name
      return suppliers.sort((a, b) => {
        if (a.verificationStatus === 'VERIFIED' && b.verificationStatus !== 'VERIFIED') return -1;
        if (a.verificationStatus !== 'VERIFIED' && b.verificationStatus === 'VERIFIED') return 1;
        return a.supplierName.localeCompare(b.supplierName);
      });
    },
  },

  async mounted() {
    await this.loadSuppliers();
    await this.loadChainNodes();
  },

  methods: {
    async loadSuppliers() {
      try {
        const response = await axios.get('/api/v1/supply-chain/suppliers', {
          params: { page: 0, size: 200, isActive: true },
        });
        this.allSuppliers = response.data.content || response.data || [];
      } catch (error) {
        this.showSnackbar('Failed to load suppliers', 'error');
      }
    },

    async loadChainNodes() {
      try {
        const response = await axios.get(`/api/v1/supply-chain/workflows/${this.workflowId}/chain`);
        this.chainNodes = response.data || [];
      } catch (error) {
        // Chain might not exist yet
        this.chainNodes = [];
      }
    },

    isInChain(supplierId) {
      return this.chainNodes.some((node) => node.id === supplierId);
    },

    selectNode(node) {
      this.selectedNodeId = this.selectedNodeId === node.id ? null : node.id;
    },

    addToChain(supplier) {
      if (this.isInChain(supplier.id)) return;

      // If chain is empty, just add
      if (this.chainNodes.length === 0) {
        this.chainNodes.push({ ...supplier, connectionInfo: null });
        this.showSnackbar(`${supplier.supplierName} added as starting point`, 'success');
        return;
      }

      // Otherwise, open connection dialog
      this.connectionSource = this.chainNodes[this.chainNodes.length - 1];
      this.connectionTarget = supplier;
      this.resetConnectionData();
      this.showConnectionDialog = true;
    },

    removeFromChain(node) {
      const index = this.chainNodes.findIndex((n) => n.id === node.id);
      if (index !== -1) {
        // Remove this node and all after it
        this.chainNodes.splice(index);
        this.showSnackbar(`${node.supplierName} removed from chain`, 'info');
      }
    },

    viewConnection(source, target) {
      this.connectionSource = source;
      this.connectionTarget = target;
      // Could open a view dialog here
      this.showSnackbar(`Connection: ${source.supplierName} → ${target.supplierName}`, 'info');
    },

    openAddSupplierDialog() {
      // Scroll to suppliers panel
      this.$nextTick(() => {
        const panel = this.$el.querySelector('.tw-grid');
        if (panel) panel.scrollIntoView({ behavior: 'smooth' });
      });
    },

    resetConnectionData() {
      const now = new Date();
      const localDatetime = new Date(now.getTime() - now.getTimezoneOffset() * 60000)
        .toISOString()
        .slice(0, 16);

      // Auto-suggest connection type based on supplier types
      let suggestedType = 'TRANSPORT';
      if (this.connectionSource?.supplierType === 'FARMER') suggestedType = 'HARVEST';
      else if (this.connectionTarget?.supplierType === 'AGGREGATOR') suggestedType = 'COLLECTION';
      else if (this.connectionTarget?.supplierType === 'PROCESSOR') suggestedType = 'PROCESSING';
      else if (this.connectionTarget?.supplierType === 'EXPORTER') suggestedType = 'EXPORT';
      else if (this.connectionTarget?.supplierType === 'IMPORTER') suggestedType = 'IMPORT';
      else if (this.connectionTarget?.supplierType === 'DISTRIBUTOR') suggestedType = 'DISTRIBUTION';
      else if (this.connectionTarget?.supplierType === 'WAREHOUSE') suggestedType = 'STORAGE';

      this.connectionData = {
        connectionType: suggestedType,
        quantityKg: null,
        transferDate: localDatetime,
        batchReference: this.generateBatchReference(),
        transportMethod: null,
        transportDocumentRef: '',
        notes: '',
      };
    },

    generateBatchReference() {
      const now = new Date();
      const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '');
      const random = Math.random().toString(36).substring(2, 8).toUpperCase();
      return `EUDR-${dateStr}-${random}`;
    },

    closeConnectionDialog() {
      this.showConnectionDialog = false;
      this.connectionSource = null;
      this.connectionTarget = null;
    },

    async createConnection() {
      if (!this.$refs.connectionForm.validate()) return;

      this.creatingConnection = true;
      try {
        await axios.post(`/api/v1/supply-chain/workflows/${this.workflowId}/connections`, {
          fromSupplierId: this.connectionSource.id,
          toSupplierId: this.connectionTarget.id,
          connectionType: this.connectionData.connectionType,
          quantityKg: this.connectionData.quantityKg,
          transferDate: this.connectionData.transferDate,
          batchReference: this.connectionData.batchReference || null,
          transportMethod: this.connectionData.transportMethod || null,
          transportDocumentRef: this.connectionData.transportDocumentRef || null,
          notes: this.connectionData.notes || null,
        });

        // Add the target to the chain with connection info
        this.chainNodes.push({
          ...this.connectionTarget,
          connectionInfo: {
            quantityKg: this.connectionData.quantityKg,
            connectionType: this.connectionData.connectionType,
            hederaTransactionId: null, // Will be populated from response
          },
        });

        this.closeConnectionDialog();
        this.showSnackbar('Connection created successfully', 'success');

        // Emit event to parent
        this.$emit('chain-updated', this.chainNodes);
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Failed to create connection', 'error');
      } finally {
        this.creatingConnection = false;
      }
    },

    // Helper methods for supplier display
    getSupplierColor(type) {
      const colors = {
        FARMER: 'green',
        FARMER_GROUP: 'green darken-1',
        COOPERATIVE: 'teal',
        AGGREGATOR: 'orange',
        TRADER: 'amber darken-2',
        PROCESSOR: 'purple',
        DISTRIBUTOR: 'indigo',
        EXPORTER: 'blue',
        IMPORTER: 'blue darken-2',
        WHOLESALER: 'cyan darken-1',
        RETAILER: 'pink',
        WAREHOUSE: 'brown',
        TRANSPORTER: 'grey darken-1',
        CERTIFICATION_BODY: 'deep-purple',
        OTHER: 'grey',
      };
      return colors[type] || 'grey';
    },

    getSupplierBorderColor(type) {
      const colors = {
        FARMER: 'tw-border-green-300',
        FARMER_GROUP: 'tw-border-green-400',
        COOPERATIVE: 'tw-border-teal-300',
        AGGREGATOR: 'tw-border-orange-300',
        TRADER: 'tw-border-amber-400',
        PROCESSOR: 'tw-border-purple-300',
        DISTRIBUTOR: 'tw-border-indigo-300',
        EXPORTER: 'tw-border-blue-300',
        IMPORTER: 'tw-border-blue-400',
        WHOLESALER: 'tw-border-cyan-300',
        RETAILER: 'tw-border-pink-300',
        WAREHOUSE: 'tw-border-amber-800',
        TRANSPORTER: 'tw-border-gray-400',
        CERTIFICATION_BODY: 'tw-border-purple-400',
        OTHER: 'tw-border-gray-300',
      };
      return colors[type] || 'tw-border-gray-300';
    },

    getSupplierIcon(type) {
      const icons = {
        FARMER: 'mdi-account',
        FARMER_GROUP: 'mdi-account-group',
        COOPERATIVE: 'mdi-account-multiple',
        AGGREGATOR: 'mdi-package-variant',
        TRADER: 'mdi-swap-horizontal',
        PROCESSOR: 'mdi-cog',
        DISTRIBUTOR: 'mdi-truck-delivery',
        EXPORTER: 'mdi-airplane-takeoff',
        IMPORTER: 'mdi-airplane-landing',
        WHOLESALER: 'mdi-store',
        RETAILER: 'mdi-storefront',
        WAREHOUSE: 'mdi-warehouse',
        TRANSPORTER: 'mdi-truck',
        CERTIFICATION_BODY: 'mdi-certificate',
        OTHER: 'mdi-dots-horizontal',
      };
      return icons[type] || 'mdi-account';
    },

    formatSupplierType(type) {
      if (!type) return '';
      return type
        .replace(/_/g, ' ')
        .toLowerCase()
        .split(' ')
        .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');
    },

    showSnackbar(message, color = 'success') {
      this.snackbar = { show: true, message, color };
    },
  },
};
</script>

<style scoped>
/* Smooth transitions */
.tw-transition-all {
  transition: all 0.2s ease-in-out;
}
</style>
