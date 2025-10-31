<template>
  <div class="importer-dashboard">
    <v-container fluid>
      <!-- Header Section -->
      <v-row>
        <v-col cols="12">
          <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
            <div>
              <h1 class="tw-text-3xl tw-font-bold tw-text-gray-800">Importer Dashboard</h1>
              <p class="tw-text-gray-600 tw-mt-1">Track shipments and ensure EUDR compliance</p>
            </div>
            <v-btn color="primary" large @click="showShipmentDialog = true">
              <v-icon left>mdi-plus</v-icon>
              New Shipment
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- Statistics Cards -->
      <v-row>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="blue lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-blue-700">{{ statistics.totalShipmentsReceived || 0 }}</div>
                  <div class="stat-label">Total Shipments</div>
                </div>
                <v-icon size="48" color="blue">mdi-ship-wheel</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="purple lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-purple-700">{{ formatNumber(statistics.totalImportVolumeKg) }} kg</div>
                  <div class="stat-label">Total Volume</div>
                </div>
                <v-icon size="48" color="purple">mdi-weight-kilogram</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="green lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-green-700">{{ statistics.eudrCompliantShipments || 0 }}</div>
                  <div class="stat-label">EUDR Compliant</div>
                </div>
                <v-icon size="48" color="green">mdi-check-decagram</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="orange lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-orange-700">{{ statistics.inTransitShipments || 0 }}</div>
                  <div class="stat-label">In Transit</div>
                </div>
                <v-icon size="48" color="orange">mdi-truck-delivery</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Tabs -->
      <v-row>
        <v-col cols="12">
          <v-card>
            <v-tabs v-model="activeTab" background-color="primary" dark>
              <v-tab>
                <v-icon left>mdi-format-list-bulleted</v-icon>
                Shipments
              </v-tab>
              <v-tab>
                <v-icon left>mdi-shield-check</v-icon>
                EUDR Compliance
              </v-tab>
              <v-tab>
                <v-icon left>mdi-file-document</v-icon>
                Documents
              </v-tab>
              <v-tab>
                <v-icon left>mdi-chart-line</v-icon>
                Analytics
              </v-tab>
            </v-tabs>

            <v-tabs-items v-model="activeTab">
              <!-- Shipments Tab -->
              <v-tab-item>
                <v-card-text>
                  <v-text-field
                    v-model="shipmentSearch"
                    prepend-inner-icon="mdi-magnify"
                    label="Search shipments..."
                    outlined
                    dense
                    hide-details
                    clearable
                    class="tw-mb-4"
                    style="max-width: 400px"
                  ></v-text-field>

                  <v-data-table
                    :headers="shipmentHeaders"
                    :items="shipments"
                    :loading="loadingShipments"
                    :search="shipmentSearch"
                    class="elevation-1"
                  >
                    <template v-slot:item.shipmentNumber="{ item }">
                      <div class="tw-font-medium tw-text-blue-600 tw-cursor-pointer" @click="viewShipmentDetails(item)">
                        {{ item.shipmentNumber }}
                      </div>
                    </template>

                    <template v-slot:item.quantityKg="{ item }">
                      <v-chip small color="blue lighten-4">
                        {{ formatNumber(item.quantityKg) }} kg
                      </v-chip>
                    </template>

                    <template v-slot:item.status="{ item }">
                      <v-chip small :color="getShipmentStatusColor(item.status)" text-color="white">
                        {{ item.status }}
                      </v-chip>
                    </template>

                    <template v-slot:item.eudrComplianceStatus="{ item }">
                      <v-chip small :color="getEudrStatusColor(item.eudrComplianceStatus)" text-color="white">
                        {{ item.eudrComplianceStatus }}
                      </v-chip>
                    </template>

                    <template v-slot:item.hedera="{ item }">
                      <HederaTransactionViewer
                        v-if="item.hederaTransactionId"
                        mode="compact"
                        :transaction-id="item.hederaTransactionId"
                        :network="hederaNetwork"
                      />
                      <span v-else class="tw-text-gray-400">Not recorded</span>
                    </template>

                    <template v-slot:item.actions="{ item }">
                      <v-menu offset-y>
                        <template v-slot:activator="{ on, attrs }">
                          <v-btn x-small text v-bind="attrs" v-on="on">
                            <v-icon small>mdi-dots-vertical</v-icon>
                          </v-btn>
                        </template>
                        <v-list dense>
                          <v-list-item @click="viewShipmentDetails(item)">
                            <v-list-item-icon><v-icon small>mdi-eye</v-icon></v-list-item-icon>
                            <v-list-item-title>View Details</v-list-item-title>
                          </v-list-item>
                          <v-list-item @click="viewTraceability(item)">
                            <v-list-item-icon><v-icon small>mdi-map-marker-path</v-icon></v-list-item-icon>
                            <v-list-item-title>Supply Chain Trace</v-list-item-title>
                          </v-list-item>
                          <v-list-item @click="uploadDocument(item)">
                            <v-list-item-icon><v-icon small>mdi-file-upload</v-icon></v-list-item-icon>
                            <v-list-item-title>Upload Document</v-list-item-title>
                          </v-list-item>
                          <v-list-item @click="recordInspection(item)">
                            <v-list-item-icon><v-icon small>mdi-clipboard-check</v-icon></v-list-item-icon>
                            <v-list-item-title>Record Inspection</v-list-item-title>
                          </v-list-item>
                          <v-list-item v-if="item.status !== 'DELIVERED'" @click="updateShipmentStatus(item)">
                            <v-list-item-icon><v-icon small>mdi-update</v-icon></v-list-item-icon>
                            <v-list-item-title>Update Status</v-list-item-title>
                          </v-list-item>
                        </v-list>
                      </v-menu>
                    </template>
                  </v-data-table>
                </v-card-text>
              </v-tab-item>

              <!-- EUDR Compliance Tab -->
              <v-tab-item>
                <v-card-text>
                  <v-alert type="info" outlined class="tw-mb-4">
                    <strong>EUDR Compliance Overview:</strong> Track shipments against EU Deforestation Regulation requirements
                  </v-alert>

                  <v-row>
                    <v-col cols="12" md="4">
                      <v-card color="green lighten-5" class="tw-h-full">
                        <v-card-text class="tw-text-center">
                          <v-icon size="64" color="green">mdi-check-circle</v-icon>
                          <div class="tw-text-3xl tw-font-bold tw-mt-2 tw-text-green-700">
                            {{ statistics.eudrCompliantShipments || 0 }}
                          </div>
                          <div class="tw-text-sm tw-text-gray-700 tw-mt-1">Compliant Shipments</div>
                        </v-card-text>
                      </v-card>
                    </v-col>

                    <v-col cols="12" md="4">
                      <v-card color="red lighten-5" class="tw-h-full">
                        <v-card-text class="tw-text-center">
                          <v-icon size="64" color="red">mdi-alert-circle</v-icon>
                          <div class="tw-text-3xl tw-font-bold tw-mt-2 tw-text-red-700">
                            {{ statistics.nonCompliantShipments || 0 }}
                          </div>
                          <div class="tw-text-sm tw-text-gray-700 tw-mt-1">Non-Compliant Shipments</div>
                        </v-card-text>
                      </v-card>
                    </v-col>

                    <v-col cols="12" md="4">
                      <v-card color="orange lighten-5" class="tw-h-full">
                        <v-card-text class="tw-text-center">
                          <v-icon size="64" color="orange">mdi-clock-outline</v-icon>
                          <div class="tw-text-3xl tw-font-bold tw-mt-2 tw-text-orange-700">
                            {{ getPendingReview() }}
                          </div>
                          <div class="tw-text-sm tw-text-gray-700 tw-mt-1">Pending Review</div>
                        </v-card-text>
                      </v-card>
                    </v-col>
                  </v-row>

                  <v-data-table
                    :headers="complianceHeaders"
                    :items="getComplianceShipments()"
                    class="elevation-1 tw-mt-4"
                  >
                    <template v-slot:item.shipmentNumber="{ item }">
                      <div class="tw-font-medium">{{ item.shipmentNumber }}</div>
                    </template>

                    <template v-slot:item.eudrComplianceStatus="{ item }">
                      <v-chip small :color="getEudrStatusColor(item.eudrComplianceStatus)" text-color="white">
                        {{ item.eudrComplianceStatus }}
                      </v-chip>
                    </template>

                    <template v-slot:item.inspectionPassed="{ item }">
                      <v-icon v-if="item.qualityInspectionPassed === true" color="success">mdi-check-circle</v-icon>
                      <v-icon v-else-if="item.qualityInspectionPassed === false" color="error">mdi-close-circle</v-icon>
                      <span v-else class="tw-text-gray-400">Pending</span>
                    </template>

                    <template v-slot:item.actions="{ item }">
                      <v-btn
                        v-if="item.eudrComplianceStatus === 'PENDING_REVIEW'"
                        x-small
                        color="primary"
                        @click="reviewCompliance(item)"
                      >
                        Review
                      </v-btn>
                      <v-btn
                        x-small
                        text
                        color="primary"
                        @click="viewTraceability(item)"
                      >
                        <v-icon small>mdi-map-marker-path</v-icon>
                      </v-btn>
                    </template>
                  </v-data-table>
                </v-card-text>
              </v-tab-item>

              <!-- Documents Tab -->
              <v-tab-item>
                <v-card-text>
                  <p class="tw-text-gray-600 tw-mb-4">
                    Customs and regulatory documents for all shipments
                  </p>
                  <v-simple-table>
                    <thead>
                      <tr>
                        <th>Shipment</th>
                        <th>Document Type</th>
                        <th>Issue Date</th>
                        <th>Issuing Authority</th>
                        <th>Hedera Hash</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="doc in getAllDocuments()" :key="doc.id">
                        <td>{{ doc.shipmentNumber }}</td>
                        <td>
                          <v-chip x-small>{{ doc.documentType }}</v-chip>
                        </td>
                        <td>{{ formatDate(doc.issueDate) }}</td>
                        <td>{{ doc.issuingAuthority || 'N/A' }}</td>
                        <td>
                          <HederaTransactionViewer
                            v-if="doc.hederaDocumentHash"
                            mode="compact"
                            :transaction-id="doc.hederaDocumentHash"
                            :network="hederaNetwork"
                          />
                          <span v-else class="tw-text-gray-400">Not recorded</span>
                        </td>
                        <td>
                          <v-btn x-small text color="primary">
                            <v-icon small>mdi-download</v-icon>
                          </v-btn>
                        </td>
                      </tr>
                    </tbody>
                  </v-simple-table>
                </v-card-text>
              </v-tab-item>

              <!-- Analytics Tab -->
              <v-tab-item>
                <v-card-text>
                  <v-row>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="tw-text-base">Top Origin Countries</v-card-title>
                        <v-card-text>
                          <div v-if="statistics.topOriginCountries && statistics.topOriginCountries.length > 0">
                            <v-list dense>
                              <v-list-item v-for="country in statistics.topOriginCountries" :key="country.country">
                                <v-list-item-content>
                                  <v-list-item-title>{{ country.country }}</v-list-item-title>
                                </v-list-item-content>
                                <v-list-item-action>
                                  <v-chip small>{{ country.totalShipments }} shipments</v-chip>
                                </v-list-item-action>
                              </v-list-item>
                            </v-list>
                          </div>
                          <div v-else class="tw-text-center tw-py-8 tw-text-gray-500">
                            No country data available
                          </div>
                        </v-card-text>
                      </v-card>
                    </v-col>

                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="tw-text-base">Top Produce Types</v-card-title>
                        <v-card-text>
                          <div v-if="statistics.topProduceTypes && statistics.topProduceTypes.length > 0">
                            <v-list dense>
                              <v-list-item v-for="produce in statistics.topProduceTypes" :key="produce.produceType">
                                <v-list-item-content>
                                  <v-list-item-title>{{ produce.produceType }}</v-list-item-title>
                                </v-list-item-content>
                                <v-list-item-action>
                                  <v-chip small>{{ formatNumber(produce.totalVolumeKg) }} kg</v-chip>
                                </v-list-item-action>
                              </v-list-item>
                            </v-list>
                          </div>
                          <div v-else class="tw-text-center tw-py-8 tw-text-gray-500">
                            No produce data available
                          </div>
                        </v-card-text>
                      </v-card>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-tab-item>
            </v-tabs-items>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Traceability Dialog -->
    <v-dialog v-model="showTraceabilityDialog" max-width="1200">
      <SupplyChainTraceability
        v-if="selectedShipment"
        :shipment-id="selectedShipment.id"
        :hedera-network="hederaNetwork"
      />
    </v-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import HederaTransactionViewer from '@/components/shared/HederaTransactionViewer.vue';
import SupplyChainTraceability from '@/components/shared/SupplyChainTraceability.vue';

export default {
  name: 'ImporterDashboard',
  components: {
    HederaTransactionViewer,
    SupplyChainTraceability,
  },
  data() {
    return {
      activeTab: 0,
      hederaNetwork: 'testnet',
      statistics: {},
      shipments: [],
      loadingShipments: false,
      shipmentSearch: '',
      showShipmentDialog: false,
      showTraceabilityDialog: false,
      selectedShipment: null,
      shipmentHeaders: [
        { text: 'Shipment #', value: 'shipmentNumber' },
        { text: 'Produce', value: 'produceType' },
        { text: 'Origin', value: 'originCountry' },
        { text: 'Quantity', value: 'quantityKg' },
        { text: 'Status', value: 'status' },
        { text: 'EUDR', value: 'eudrComplianceStatus' },
        { text: 'Hedera', value: 'hedera' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      complianceHeaders: [
        { text: 'Shipment #', value: 'shipmentNumber' },
        { text: 'Origin', value: 'originCountry' },
        { text: 'Produce', value: 'produceType' },
        { text: 'EUDR Status', value: 'eudrComplianceStatus' },
        { text: 'Inspection', value: 'inspectionPassed' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
    };
  },
  computed: {
    importerId() {
      return this.$store.state.user?.entityId || null;
    },
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      await Promise.all([
        this.loadStatistics(),
        this.loadShipments(),
      ]);
    },

    async loadStatistics() {
      try {
        const response = await axios.get(`/api/v1/importers/${this.importerId}/statistics`);
        this.statistics = response.data;
      } catch (error) {
        this.$toast.error('Failed to load statistics');
      }
    },

    async loadShipments() {
      this.loadingShipments = true;
      try {
        const response = await axios.get(`/api/v1/importers/${this.importerId}/shipments`);
        this.shipments = response.data.content;
      } catch (error) {
        this.$toast.error('Failed to load shipments');
      } finally {
        this.loadingShipments = false;
      }
    },

    viewShipmentDetails(shipment) {
      this.$router.push({ name: 'ShipmentDetails', params: { id: shipment.id } });
    },

    viewTraceability(shipment) {
      this.selectedShipment = shipment;
      this.showTraceabilityDialog = true;
    },

    // eslint-disable-next-line no-unused-vars
    uploadDocument(shipment) {
      // Navigate to document upload
    },

    // eslint-disable-next-line no-unused-vars
    recordInspection(shipment) {
      // Navigate to inspection form
    },

    // eslint-disable-next-line no-unused-vars
    updateShipmentStatus(shipment) {
      // Show status update dialog
    },

    // eslint-disable-next-line no-unused-vars
    reviewCompliance(shipment) {
      // Navigate to compliance review
    },

    getComplianceShipments() {
      return this.shipments;
    },

    getAllDocuments() {
      // Flatten all documents from all shipments
      return this.shipments.flatMap((s) => (s.customsDocuments || []).map((d) => ({
        ...d,
        shipmentNumber: s.shipmentNumber,
      })));
    },

    getPendingReview() {
      return this.shipments.filter((s) => s.eudrComplianceStatus === 'PENDING_REVIEW').length;
    },

    formatNumber(num) {
      return num ? num.toLocaleString() : '0';
    },

    formatDate(dateString) {
      if (!dateString) return 'N/A';
      try {
        return new Date(dateString).toLocaleDateString();
      } catch {
        return dateString;
      }
    },

    getShipmentStatusColor(status) {
      const colors = {
        PENDING: 'grey',
        IN_TRANSIT: 'blue',
        CUSTOMS_CLEARANCE: 'orange',
        DELIVERED: 'green',
      };
      return colors[status] || 'grey';
    },

    getEudrStatusColor(status) {
      const colors = {
        COMPLIANT: 'success',
        NON_COMPLIANT: 'error',
        PENDING_REVIEW: 'warning',
        UNDER_INVESTIGATION: 'orange',
      };
      return colors[status] || 'grey';
    },
  },
};
</script>

<style scoped>
.importer-dashboard {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.stats-card {
  height: 100%;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
}
</style>
