<template>
  <div class="exporter-batches">
    <v-container fluid>
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <div class="d-flex align-center mb-4">
            <v-icon large color="teal" class="mr-3">mdi-package-variant-closed</v-icon>
            <div>
              <h1 class="text-h4 font-weight-bold">Batch Management</h1>
              <p class="text-subtitle-1 text--secondary mb-0">
                Track and manage commodity batches from suppliers through export
              </p>
            </div>
          </div>
        </v-col>
      </v-row>

      <!-- Batch Statistics -->
      <v-row class="mb-4">
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card received" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-inbox</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ receivedBatches }}</div>
              <div class="text-subtitle-2">Received Batches</div>
              <div class="text-caption">From suppliers</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card processing" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-cog</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ processingBatches }}</div>
              <div class="text-subtitle-2">Processing</div>
              <div class="text-caption">In facility</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card exported" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-export</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ exportedBatches }}</div>
              <div class="text-subtitle-2">Exported</div>
              <div class="text-caption">This month</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card total-volume" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-weight</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ totalVolume }}</div>
              <div class="text-subtitle-2">Total Volume (MT)</div>
              <div class="text-caption">This quarter</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Batch Filters -->
      <v-row class="mb-4">
        <v-col cols="12">
          <v-card elevation="2">
            <v-card-text>
              <v-row align="center">
                <v-col cols="12" sm="6" md="3">
                  <v-select
                    v-model="statusFilter"
                    :items="statusOptions"
                    label="Filter by Status"
                    dense
                    outlined
                    hide-details
                  ></v-select>
                </v-col>
                <v-col cols="12" sm="6" md="3">
                  <v-select
                    v-model="commodityFilter"
                    :items="commodityOptions"
                    label="Filter by Commodity"
                    dense
                    outlined
                    hide-details
                  ></v-select>
                </v-col>
                <v-col cols="12" sm="6" md="3">
                  <v-select
                    v-model="supplierFilter"
                    :items="supplierOptions"
                    label="Filter by Supplier"
                    dense
                    outlined
                    hide-details
                  ></v-select>
                </v-col>
                <v-col cols="12" sm="6" md="3">
                  <v-text-field
                    v-model="search"
                    append-icon="mdi-magnify"
                    label="Search batches..."
                    dense
                    outlined
                    hide-details
                  ></v-text-field>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Batches List -->
      <v-row>
        <v-col cols="12">
          <v-card elevation="3">
            <v-card-title class="d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-format-list-bulleted</v-icon>
              Commodity Batches
              <v-spacer></v-spacer>
              <v-btn
                color="success"
                small
                @click="exportBatches"
                class="mr-2"
              >
                <v-icon left>mdi-download</v-icon>
                Export Data
              </v-btn>
            </v-card-title>
            <v-data-table
              :headers="headers"
              :items="filteredBatches"
              :search="search"
              :loading="loading"
              class="elevation-0"
            >
              <template v-slot:item.batchInfo="{ item }">
                <div class="d-flex align-center">
                  <v-icon :color="getCommodityColor(item.commodity)" class="mr-2">
                    {{ getCommodityIcon(item.commodity) }}
                  </v-icon>
                  <div>
                    <div class="font-weight-medium">{{ item.batchId }}</div>
                    <div class="text-caption text--secondary">{{ item.commodity }}</div>
                  </div>
                </div>
              </template>
              <template v-slot:item.supplier="{ item }">
                <div>
                  <div class="font-weight-medium">{{ item.supplierName }}</div>
                  <div class="text-caption text--secondary">{{ item.supplierRegion }}</div>
                </div>
              </template>
              <template v-slot:item.status="{ item }">
                <v-chip
                  :color="getStatusColor(item.status)"
                  text-color="white"
                  small
                >
                  {{ item.status }}
                </v-chip>
              </template>
              <template v-slot:item.volume="{ item }">
                {{ item.volume }} kg
              </template>
              <template v-slot:item.receivedAt="{ item }">
                {{ formatDate(item.receivedAt) }}
              </template>
              <template v-slot:item.compliance="{ item }">
                <div class="d-flex align-center">
                  <v-progress-circular
                    :value="item.complianceScore"
                    :color="getComplianceColor(item.complianceScore)"
                    size="30"
                    width="3"
                    class="mr-2"
                  >
                    <span class="text-caption">{{ item.complianceScore }}%</span>
                  </v-progress-circular>
                </div>
              </template>
              <template v-slot:item.traceability="{ item }">
                <v-btn
                  icon
                  small
                  :color="item.traceabilityComplete ? 'success' : 'warning'"
                  @click="viewTraceability(item)"
                >
                  <v-icon>{{ item.traceabilityComplete ? 'mdi-check-circle' : 'mdi-alert-circle' }}</v-icon>
                </v-btn>
              </template>
              <template v-slot:item.actions="{ item }">
                <v-btn
                  icon
                  small
                  color="primary"
                  @click="viewBatch(item)"
                >
                  <v-icon>mdi-eye</v-icon>
                </v-btn>
                <v-btn
                  v-if="item.status === 'Received'"
                  icon
                  small
                  color="success"
                  @click="processBatch(item)"
                >
                  <v-icon>mdi-play</v-icon>
                </v-btn>
                <v-btn
                  v-if="item.status === 'Processing'"
                  icon
                  small
                  color="warning"
                  @click="exportBatch(item)"
                >
                  <v-icon>mdi-export</v-icon>
                </v-btn>
                <!-- QR Code Button -->
                <BatchQRCode
                  v-if="item.status === 'Exported'"
                  :batch-code="item.batchId"
                  button-text=""
                  small
                  outlined
                />
                <!-- Create Shipment Button -->
                <v-btn
                  v-if="item.status === 'Exported'"
                  icon
                  small
                  color="cyan"
                  @click="openShipmentDialog(item)"
                  title="Create Shipment to Importer"
                >
                  <v-icon>mdi-ship-wheel</v-icon>
                </v-btn>
              </template>
            </v-data-table>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Batch Details Dialog -->
    <v-dialog v-model="showDetailsDialog" max-width="800px">
      <v-card v-if="selectedBatch">
        <v-card-title class="d-flex align-center">
          <v-icon :color="getCommodityColor(selectedBatch.commodity)" class="mr-2">
            {{ getCommodityIcon(selectedBatch.commodity) }}
          </v-icon>
          Batch Details: {{ selectedBatch.batchId }}
        </v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <v-card outlined>
                <v-card-subtitle>Batch Information</v-card-subtitle>
                <v-card-text>
                  <div class="mb-2">
                    <strong>Commodity:</strong> {{ selectedBatch.commodity }}
                  </div>
                  <div class="mb-2">
                    <strong>Volume:</strong> {{ selectedBatch.volume }} kg
                  </div>
                  <div class="mb-2">
                    <strong>Quality Grade:</strong> {{ selectedBatch.quality }}
                  </div>
                  <div class="mb-2">
                    <strong>Harvest Date:</strong> {{ formatDate(selectedBatch.harvestDate) }}
                  </div>
                </v-card-text>
              </v-card>
            </v-col>
            <v-col cols="12" md="6">
              <v-card outlined>
                <v-card-subtitle>Supplier Information</v-card-subtitle>
                <v-card-text>
                  <div class="mb-2">
                    <strong>Supplier:</strong> {{ selectedBatch.supplierName }}
                  </div>
                  <div class="mb-2">
                    <strong>Region:</strong> {{ selectedBatch.supplierRegion }}
                  </div>
                  <div class="mb-2">
                    <strong>Farm ID:</strong> {{ selectedBatch.farmId }}
                  </div>
                  <div class="mb-2">
                    <strong>Compliance Score:</strong>
                    <v-chip
                      :color="getComplianceColor(selectedBatch.complianceScore)"
                      text-color="white"
                      small
                      class="ml-2"
                    >
                      {{ selectedBatch.complianceScore }}%
                    </v-chip>
                  </div>
                </v-card-text>
              </v-card>
            </v-col>
            <v-col cols="12">
              <v-card outlined>
                <v-card-subtitle>Processing Timeline</v-card-subtitle>
                <v-card-text>
                  <v-timeline dense>
                    <v-timeline-item
                      color="success"
                      small
                    >
                      <div>
                        <div class="font-weight-bold">Batch Received</div>
                        <div class="text-caption">{{ formatDate(selectedBatch.receivedAt) }}</div>
                      </div>
                    </v-timeline-item>
                    <v-timeline-item
                      :color="selectedBatch.status === 'Processing' || selectedBatch.status === 'Exported' ? 'success' : 'grey'"
                      small
                    >
                      <div>
                        <div class="font-weight-bold">Processing Started</div>
                        <div class="text-caption">
                          {{ selectedBatch.processingStarted ? formatDate(selectedBatch.processingStarted) : 'Pending' }}
                        </div>
                      </div>
                    </v-timeline-item>
                    <v-timeline-item
                      :color="selectedBatch.status === 'Exported' ? 'success' : 'grey'"
                      small
                    >
                      <div>
                        <div class="font-weight-bold">Export Completed</div>
                        <div class="text-caption">
                          {{ selectedBatch.exportedAt ? formatDate(selectedBatch.exportedAt) : 'Pending' }}
                        </div>
                      </div>
                    </v-timeline-item>
                  </v-timeline>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="showDetailsDialog = false">Close</v-btn>
          <!-- QR Code Button -->
          <BatchQRCode
            v-if="selectedBatch && selectedBatch.status === 'Exported'"
            :batch-code="selectedBatch.batchId"
            button-text="Generate QR Code"
          />
          <v-btn
            color="primary"
            @click="viewTraceability(selectedBatch)"
          >
            View Full Traceability
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Create Shipment Dialog -->
    <v-dialog v-model="showShipmentDialog" max-width="600" persistent>
      <v-card v-if="shipmentBatch">
        <v-card-title class="tw-bg-cyan-600 tw-text-white">
          <v-icon color="white" class="tw-mr-2">mdi-ship-wheel</v-icon>
          Create Shipment to Importer
        </v-card-title>
        <v-card-text class="tw-pt-6">
          <v-alert type="info" text dense class="tw-mb-4">
            Create a shipment for batch <strong>{{ shipmentBatch.batchId }}</strong>
            ({{ shipmentBatch.volume }} {{ shipmentBatch.commodity }})
          </v-alert>

          <v-form ref="shipmentForm" v-model="shipmentFormValid">
            <v-autocomplete
              v-model="shipment.importerId"
              :items="importersList"
              item-text="displayName"
              item-value="id"
              label="Select Importer *"
              outlined
              dense
              prepend-inner-icon="mdi-domain"
              :rules="[v => !!v || 'Required']"
              :loading="loadingImporters"
            >
              <template v-slot:item="{ item }">
                <div class="tw-py-2">
                  <div class="tw-font-medium">{{ item.companyName }}</div>
                  <div class="tw-text-xs tw-text-slate-500">{{ item.country }} • {{ item.port || 'N/A' }}</div>
                </div>
              </template>
            </v-autocomplete>

            <v-text-field
              v-model="shipment.shipmentNumber"
              label="Shipment Number"
              outlined
              dense
              prepend-inner-icon="mdi-barcode"
              placeholder="Auto-generated if empty"
            ></v-text-field>

            <v-row>
              <v-col cols="6">
                <v-text-field
                  v-model="shipment.departureDate"
                  label="Departure Date *"
                  type="date"
                  outlined
                  dense
                  :rules="[v => !!v || 'Required']"
                ></v-text-field>
              </v-col>
              <v-col cols="6">
                <v-text-field
                  v-model="shipment.estimatedArrival"
                  label="Est. Arrival Date"
                  type="date"
                  outlined
                  dense
                ></v-text-field>
              </v-col>
            </v-row>

            <v-text-field
              v-model="shipment.portOfOrigin"
              label="Port of Origin"
              outlined
              dense
              prepend-inner-icon="mdi-anchor"
              placeholder="e.g., Mombasa"
            ></v-text-field>

            <v-textarea
              v-model="shipment.notes"
              label="Shipping Notes"
              outlined
              dense
              rows="2"
            ></v-textarea>
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-btn text @click="showShipmentDialog = false">Cancel</v-btn>
          <v-spacer></v-spacer>
          <v-btn
            color="cyan"
            dark
            :loading="creatingShipment"
            :disabled="!shipmentFormValid"
            @click="createShipment"
          >
            <v-icon left>mdi-send</v-icon>
            Create Shipment
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
/* eslint-disable */
import axios from 'axios';
import BatchQRCode from '@/components/shared/BatchQRCode.vue';

export default {
  name: 'ExporterBatches',
  components: {
    BatchQRCode,
  },
  data() {
    return {
      loading: false,
      search: '',
      statusFilter: 'All',
      commodityFilter: 'All',
      supplierFilter: 'All',
      showDetailsDialog: false,
      selectedBatch: null,
      // Shipment dialog
      showShipmentDialog: false,
      shipmentBatch: null,
      shipmentFormValid: false,
      creatingShipment: false,
      loadingImporters: false,
      importersList: [],
      shipment: {
        importerId: null,
        shipmentNumber: '',
        departureDate: new Date().toISOString().split('T')[0],
        estimatedArrival: '',
        portOfOrigin: '',
        notes: '',
      },
      statusOptions: ['All', 'Received', 'Processing', 'Exported', 'Rejected'],
      commodityOptions: [
        { text: 'All Commodities', value: 'All' },
        { text: 'Coffee', value: 'Coffee' },
        { text: 'Cocoa', value: 'Cocoa' },
        { text: 'Palm Oil', value: 'Palm Oil' },
        { text: 'Soy', value: 'Soy' },
      ],
      supplierOptions: [
        { text: 'All Suppliers', value: 'All' },
        { text: 'Green Valley Farm', value: 'Green Valley Farm' },
        { text: 'Mountain Coffee Co.', value: 'Mountain Coffee Co.' },
        { text: 'Sunrise Plantation', value: 'Sunrise Plantation' },
      ],
      headers: [
        { text: 'Batch Info', value: 'batchInfo', sortable: true },
        { text: 'Supplier', value: 'supplier', sortable: true },
        { text: 'Volume', value: 'volume', sortable: true },
        { text: 'Status', value: 'status', sortable: true },
        { text: 'Received', value: 'receivedAt', sortable: true },
        { text: 'Compliance', value: 'compliance', sortable: true },
        { text: 'Traceability', value: 'traceability', sortable: false },
        {
          text: 'Actions', value: 'actions', sortable: false, width: '150px',
        },
      ],
      batches: [
        {
          id: 1,
          batchId: 'CB-2024-001',
          commodity: 'Coffee',
          volume: 500,
          status: 'Processing',
          supplierName: 'Green Valley Farm',
          supplierRegion: 'Central Kenya',
          farmId: 'GVF-001',
          quality: 'Premium',
          harvestDate: '2024-11-15',
          receivedAt: '2024-12-01',
          processingStarted: '2024-12-05',
          exportedAt: null,
          complianceScore: 95,
          traceabilityComplete: true,
        },
        {
          id: 2,
          batchId: 'CC-2024-002',
          commodity: 'Cocoa',
          volume: 300,
          status: 'Received',
          supplierName: 'Sunrise Plantation',
          supplierRegion: 'Eastern Province',
          farmId: 'SP-003',
          quality: 'Grade A',
          harvestDate: '2024-11-20',
          receivedAt: '2024-12-10',
          processingStarted: null,
          exportedAt: null,
          complianceScore: 88,
          traceabilityComplete: false,
        },
        {
          id: 3,
          batchId: 'CB-2024-003',
          commodity: 'Coffee',
          volume: 750,
          status: 'Exported',
          supplierName: 'Mountain Coffee Co.',
          supplierRegion: 'Western Region',
          farmId: 'MCC-002',
          quality: 'Premium',
          harvestDate: '2024-10-25',
          receivedAt: '2024-11-15',
          processingStarted: '2024-11-20',
          exportedAt: '2024-12-01',
          complianceScore: 92,
          traceabilityComplete: true,
        },
      ],
    };
  },
  computed: {
    filteredBatches() {
      let filtered = this.batches;
      if (this.statusFilter !== 'All') {
        filtered = filtered.filter((batch) => batch.status === this.statusFilter);
      }
      if (this.commodityFilter !== 'All') {
        filtered = filtered.filter((batch) => batch.commodity === this.commodityFilter);
      }
      if (this.supplierFilter !== 'All') {
        filtered = filtered.filter((batch) => batch.supplierName === this.supplierFilter);
      }
      return filtered;
    },
    receivedBatches() {
      return this.batches.filter((b) => b.status === 'Received').length;
    },
    processingBatches() {
      return this.batches.filter((b) => b.status === 'Processing').length;
    },
    exportedBatches() {
      return this.batches.filter((b) => b.status === 'Exported').length;
    },
    totalVolume() {
      return Math.round(this.batches.reduce((sum, batch) => sum + batch.volume, 0) / 1000);
    },
  },
  methods: {
    getStatusColor(status) {
      const colors = {
        Received: 'info',
        Processing: 'warning',
        Exported: 'success',
        Rejected: 'error',
      };
      return colors[status] || 'grey';
    },
    getCommodityColor(commodity) {
      const colors = {
        Coffee: 'brown',
        Cocoa: 'orange',
        'Palm Oil': 'green',
        Soy: 'yellow',
      };
      return colors[commodity] || 'grey';
    },
    getCommodityIcon(commodity) {
      const icons = {
        Coffee: 'mdi-coffee',
        Cocoa: 'mdi-food',
        'Palm Oil': 'mdi-leaf',
        Soy: 'mdi-soy-sauce',
      };
      return icons[commodity] || 'mdi-package';
    },
    getComplianceColor(score) {
      if (score >= 90) return 'success';
      if (score >= 80) return 'warning';
      return 'error';
    },
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString();
    },
    viewBatch(batch) {
      this.selectedBatch = batch;
      this.showDetailsDialog = true;
    },
    processBatch(batch) {
      batch.status = 'Processing';
      batch.processingStarted = new Date().toISOString().split('T')[0];
      this.$toast.success(`Processing started for batch ${batch.batchId}`);
    },
    exportBatch(batch) {
      batch.status = 'Exported';
      batch.exportedAt = new Date().toISOString().split('T')[0];
      this.$toast.success(`Batch ${batch.batchId} marked as exported`);
    },
    viewTraceability(batch) {
    },
    exportBatches() {
    },
    // Shipment methods
    async openShipmentDialog(batch) {
      this.shipmentBatch = batch;
      this.showShipmentDialog = true;
      await this.loadImporters();
    },
    async loadImporters() {
      this.loadingImporters = true;
      try {
        const response = await axios.get('/api/v1/importers');
        this.importersList = (response.data || []).map((i) => ({
          ...i,
          displayName: `${i.companyName} (${i.country})`,
        }));
      } catch (error) {
        this.$toast.error('Failed to fetch importers:', error.message);
        this.importersList = [];
      } finally {
        this.loadingImporters = false;
      }
    },
    async createShipment() {
      if (!this.$refs.shipmentForm.validate()) return;

      this.creatingShipment = true;
      try {
        const payload = {
          batchId: this.shipmentBatch.batchId,
          importerId: this.shipment.importerId,
          shipmentNumber: this.shipment.shipmentNumber || `SHP-${Date.now()}`,
          departureDate: this.shipment.departureDate,
          estimatedArrival: this.shipment.estimatedArrival,
          portOfOrigin: this.shipment.portOfOrigin,
          notes: this.shipment.notes,
        };

        await axios.post('/api/v1/shipments', payload);
        this.$toast.success('Shipment created! Importer will need to confirm receipt.');
        this.showShipmentDialog = false;
        this.resetShipmentForm();
      } catch (error) {
        this.$toast.error('Failed to create shipment');
      } finally {
        this.creatingShipment = false;
      }
    },
    resetShipmentForm() {
      this.shipment = {
        importerId: null,
        shipmentNumber: '',
        departureDate: new Date().toISOString().split('T')[0],
        estimatedArrival: '',
        portOfOrigin: '',
        notes: '',
      };
      this.$refs.shipmentForm?.resetValidation();
    },
  },
};
</script>

<style scoped>
.exporter-batches {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px 0;
}
.stat-card {
  transition: transform 0.2s;
  color: white;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-card.received {
  background: linear-gradient(135deg, #2196f3, #1565c0);
}
.stat-card.processing {
  background: linear-gradient(135deg, #ff9800, #f57c00);
}
.stat-card.exported {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
}
.stat-card.total-volume {
  background: linear-gradient(135deg, #9c27b0, #6a1b9a);
}
</style>
