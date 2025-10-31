<template>
  <div class="supply-chain-traceability">
    <v-card class="tw-shadow-lg">
      <v-card-title class="tw-bg-gradient-to-r tw-from-green-600 tw-to-green-700 tw-text-white">
        <v-icon color="white" class="tw-mr-2">mdi-map-marker-path</v-icon>
        Complete Supply Chain Traceability
        <v-spacer></v-spacer>
        <v-chip
          v-if="eudrCompliant !== null"
          :color="eudrCompliant ? 'success' : 'error'"
          text-color="white"
          small
        >
          <v-icon left small>{{ eudrCompliant ? 'mdi-check-circle' : 'mdi-alert-circle' }}</v-icon>
          {{ eudrCompliant ? 'EUDR Compliant' : 'Non-Compliant' }}
        </v-chip>
      </v-card-title>

      <!-- Loading State -->
      <v-card-text v-if="loading" class="tw-py-8">
        <div class="tw-flex tw-flex-col tw-items-center">
          <v-progress-circular
            indeterminate
            color="primary"
            size="64"
          ></v-progress-circular>
          <p class="tw-mt-4 tw-text-gray-600">Loading supply chain data...</p>
        </div>
      </v-card-text>

      <!-- Error State -->
      <v-card-text v-else-if="error">
        <v-alert type="error" outlined>
          <strong>Error loading supply chain data:</strong> {{ error }}
        </v-alert>
      </v-card-text>

      <!-- Traceability Timeline -->
      <v-card-text v-else class="tw-px-0">
        <v-timeline align-top dense class="supply-chain-timeline">
          <!-- Origin: Farmer/Production Unit -->
          <v-timeline-item
            v-if="traceData.origin"
            color="green"
            icon="mdi-sprout"
            fill-dot
            large
          >
            <template v-slot:opposite>
              <span class="tw-text-sm tw-font-semibold">{{ formatDate(traceData.origin.timestamp) }}</span>
            </template>

            <v-card class="elevation-2">
              <v-card-title class="tw-text-base tw-bg-green-50">
                <v-icon color="green" class="tw-mr-2">mdi-sprout</v-icon>
                Origin: Farm Production
              </v-card-title>
              <v-card-text>
                <div class="detail-grid">
                  <div class="detail-item">
                    <span class="label">Farmer:</span>
                    <span class="value">{{ traceData.origin.farmerName }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Production Unit:</span>
                    <span class="value">{{ traceData.origin.productionUnitName }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Area:</span>
                    <span class="value">{{ traceData.origin.areaHectares }} hectares</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Coordinates:</span>
                    <span class="value monospace">{{ traceData.origin.coordinates }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Harvest Date:</span>
                    <span class="value">{{ formatDate(traceData.origin.harvestDate) }}</span>
                  </div>
                </div>

                <!-- Hedera Verification -->
                <HederaTransactionViewer
                  v-if="traceData.origin.hederaTransactionId"
                  mode="inline"
                  :network="hederaNetwork"
                  :transaction-id="traceData.origin.hederaTransactionId"
                  :consensus-timestamp="traceData.origin.hederaConsensusTimestamp"
                  :document-hash="traceData.origin.hederaDocumentHash"
                  class="tw-mt-3"
                />
              </v-card-text>
            </v-card>
          </v-timeline-item>

          <!-- Aggregator Collection -->
          <v-timeline-item
            v-if="traceData.aggregation"
            color="orange"
            icon="mdi-warehouse"
            fill-dot
            large
          >
            <template v-slot:opposite>
              <span class="tw-text-sm tw-font-semibold">{{ formatDate(traceData.aggregation.timestamp) }}</span>
            </template>

            <v-card class="elevation-2">
              <v-card-title class="tw-text-base tw-bg-orange-50">
                <v-icon color="orange" class="tw-mr-2">mdi-warehouse</v-icon>
                Aggregation: Collection Center
              </v-card-title>
              <v-card-text>
                <div class="detail-grid">
                  <div class="detail-item">
                    <span class="label">Aggregator:</span>
                    <span class="value">{{ traceData.aggregation.aggregatorName }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Type:</span>
                    <span class="value">{{ traceData.aggregation.aggregatorType }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Quantity Collected:</span>
                    <span class="value">{{ traceData.aggregation.quantityKg }} kg</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Quality Grade:</span>
                    <span class="value">{{ traceData.aggregation.qualityGrade }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Collection Date:</span>
                    <span class="value">{{ formatDate(traceData.aggregation.collectionDate) }}</span>
                  </div>
                </div>

                <!-- Hedera Verification -->
                <HederaTransactionViewer
                  v-if="traceData.aggregation.hederaTransactionId"
                  mode="inline"
                  :network="hederaNetwork"
                  :transaction-id="traceData.aggregation.hederaTransactionId"
                  :consensus-timestamp="traceData.aggregation.hederaConsensusTimestamp"
                  :document-hash="traceData.aggregation.hederaBatchHash"
                  class="tw-mt-3"
                />
              </v-card-text>
            </v-card>
          </v-timeline-item>

          <!-- Processor -->
          <v-timeline-item
            v-if="traceData.processing"
            color="blue"
            icon="mdi-factory"
            fill-dot
            large
          >
            <template v-slot:opposite>
              <span class="tw-text-sm tw-font-semibold">{{ formatDate(traceData.processing.timestamp) }}</span>
            </template>

            <v-card class="elevation-2">
              <v-card-title class="tw-text-base tw-bg-blue-50">
                <v-icon color="blue" class="tw-mr-2">mdi-factory</v-icon>
                Processing: Value Addition
              </v-card-title>
              <v-card-text>
                <div class="detail-grid">
                  <div class="detail-item">
                    <span class="label">Processor:</span>
                    <span class="value">{{ traceData.processing.processorName }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Processing Type:</span>
                    <span class="value">{{ traceData.processing.processingType }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Input Quantity:</span>
                    <span class="value">{{ traceData.processing.inputQuantity }} kg</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Output Quantity:</span>
                    <span class="value">{{ traceData.processing.outputQuantity }} kg</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Processing Date:</span>
                    <span class="value">{{ formatDate(traceData.processing.processingDate) }}</span>
                  </div>
                </div>

                <!-- Hedera Verification -->
                <HederaTransactionViewer
                  v-if="traceData.processing.hederaTransactionId"
                  mode="inline"
                  :network="hederaNetwork"
                  :transaction-id="traceData.processing.hederaTransactionId"
                  :consensus-timestamp="traceData.processing.hederaConsensusTimestamp"
                  class="tw-mt-3"
                />
              </v-card-text>
            </v-card>
          </v-timeline-item>

          <!-- Importer/Shipment -->
          <v-timeline-item
            v-if="traceData.import"
            color="purple"
            icon="mdi-ship-wheel"
            fill-dot
            large
          >
            <template v-slot:opposite>
              <span class="tw-text-sm tw-font-semibold">{{ formatDate(traceData.import.timestamp) }}</span>
            </template>

            <v-card class="elevation-2">
              <v-card-title class="tw-text-base tw-bg-purple-50">
                <v-icon color="purple" class="tw-mr-2">mdi-ship-wheel</v-icon>
                Import: EU Destination
              </v-card-title>
              <v-card-text>
                <div class="detail-grid">
                  <div class="detail-item">
                    <span class="label">Importer:</span>
                    <span class="value">{{ traceData.import.importerName }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Shipment Number:</span>
                    <span class="value">{{ traceData.import.shipmentNumber }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Destination Country:</span>
                    <span class="value">{{ traceData.import.destinationCountry }}</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">Quantity:</span>
                    <span class="value">{{ traceData.import.quantityKg }} kg</span>
                  </div>
                  <div class="detail-item">
                    <span class="label">EUDR Status:</span>
                    <v-chip
                      x-small
                      :color="getEudrStatusColor(traceData.import.eudrStatus)"
                      text-color="white"
                    >
                      {{ traceData.import.eudrStatus }}
                    </v-chip>
                  </div>
                </div>

                <!-- Customs Documents -->
                <div v-if="traceData.import.customsDocuments && traceData.import.customsDocuments.length > 0" class="tw-mt-3">
                  <h4 class="tw-text-sm tw-font-semibold tw-mb-2">Customs Documents:</h4>
                  <v-chip
                    v-for="doc in traceData.import.customsDocuments"
                    :key="doc.id"
                    small
                    class="tw-mr-2 tw-mb-1"
                  >
                    <v-icon left x-small>mdi-file-document</v-icon>
                    {{ doc.documentType }}
                  </v-chip>
                </div>

                <!-- Hedera Verification -->
                <HederaTransactionViewer
                  v-if="traceData.import.hederaTransactionId"
                  mode="inline"
                  :network="hederaNetwork"
                  :transaction-id="traceData.import.hederaTransactionId"
                  :consensus-timestamp="traceData.import.hederaConsensusTimestamp"
                  :document-hash="traceData.import.hederaShipmentHash"
                  class="tw-mt-3"
                />
              </v-card-text>
            </v-card>
          </v-timeline-item>
        </v-timeline>

        <!-- Summary Statistics -->
        <v-divider class="tw-my-4"></v-divider>
        <div class="summary-section tw-px-4 tw-pb-4">
          <h3 class="tw-text-lg tw-font-semibold tw-mb-3">Supply Chain Summary</h3>
          <v-row>
            <v-col cols="12" sm="6" md="3">
              <v-card outlined>
                <v-card-text class="tw-text-center">
                  <div class="tw-text-3xl tw-font-bold tw-text-green-600">{{ getTotalActors() }}</div>
                  <div class="tw-text-sm tw-text-gray-600">Supply Chain Actors</div>
                </v-card-text>
              </v-card>
            </v-col>
            <v-col cols="12" sm="6" md="3">
              <v-card outlined>
                <v-card-text class="tw-text-center">
                  <div class="tw-text-3xl tw-font-bold tw-text-blue-600">{{ getTotalDays() }}</div>
                  <div class="tw-text-sm tw-text-gray-600">Days Farm to EU</div>
                </v-card-text>
              </v-card>
            </v-col>
            <v-col cols="12" sm="6" md="3">
              <v-card outlined>
                <v-card-text class="tw-text-center">
                  <div class="tw-text-3xl tw-font-bold tw-text-purple-600">{{ getHederaTransactions() }}</div>
                  <div class="tw-text-sm tw-text-gray-600">Hedera Transactions</div>
                </v-card-text>
              </v-card>
            </v-col>
            <v-col cols="12" sm="6" md="3">
              <v-card outlined>
                <v-card-text class="tw-text-center">
                  <div class="tw-text-3xl tw-font-bold tw-text-orange-600">100%</div>
                  <div class="tw-text-sm tw-text-gray-600">Traceability</div>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
        </div>
      </v-card-text>
    </v-card>
  </div>
</template>

<script>
/* eslint-disable */
import HederaTransactionViewer from './HederaTransactionViewer.vue';

export default {
  name: 'SupplyChainTraceability',
  components: {
    HederaTransactionViewer,
  },
  props: {
    // Can pass either shipmentId, batchId, or full traceData object
    shipmentId: {
      type: String,
      default: null,
    },
    batchId: {
      type: String,
      default: null,
    },
    traceData: {
      type: Object,
      default: () => ({}),
    },
    hederaNetwork: {
      type: String,
      default: 'testnet',
      validator: (value) => ['mainnet', 'testnet'].includes(value),
    },
  },
  data() {
    return {
      loading: false,
      error: null,
      eudrCompliant: null,
    };
  },
  computed: {
    hasTraceData() {
      return Object.keys(this.traceData).length > 0;
    },
  },
  mounted() {
    if (!this.hasTraceData && (this.shipmentId || this.batchId)) {
      this.loadTraceabilityData();
    } else if (this.hasTraceData) {
      this.updateEudrCompliance();
    }
  },
  methods: {
    async loadTraceabilityData() {
      this.loading = true;
      this.error = null;

      try {
        // API call to fetch traceability data
        let endpoint = '';
        if (this.shipmentId) {
          endpoint = `/api/v1/traceability/shipment/${this.shipmentId}`;
        } else if (this.batchId) {
          endpoint = `/api/v1/traceability/batch/${this.batchId}`;
        }

        const response = await axios.get(endpoint);
        Object.assign(this.traceData, response.data);
        this.updateEudrCompliance();
      } catch (err) {
        this.error = err.response?.data?.message || err.message || 'Failed to load traceability data';
      } finally {
        this.loading = false;
      }
    },

    updateEudrCompliance() {
      if (this.traceData.import && this.traceData.import.eudrStatus) {
        this.eudrCompliant = this.traceData.import.eudrStatus === 'COMPLIANT';
      }
    },

    formatDate(dateString) {
      if (!dateString) return 'N/A';
      try {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
          year: 'numeric',
          month: 'short',
          day: 'numeric',
        });
      } catch (e) {
        return dateString;
      }
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

    getTotalActors() {
      let count = 0;
      if (this.traceData.origin) count++;
      if (this.traceData.aggregation) count++;
      if (this.traceData.processing) count++;
      if (this.traceData.import) count++;
      return count;
    },

    getTotalDays() {
      if (!this.traceData.origin?.timestamp || !this.traceData.import?.timestamp) {
        return 'N/A';
      }
      try {
        const start = new Date(this.traceData.origin.timestamp);
        const end = new Date(this.traceData.import.timestamp);
        const diffTime = Math.abs(end - start);
        return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      } catch (e) {
        return 'N/A';
      }
    },

    getHederaTransactions() {
      let count = 0;
      if (this.traceData.origin?.hederaTransactionId) count++;
      if (this.traceData.aggregation?.hederaTransactionId) count++;
      if (this.traceData.processing?.hederaTransactionId) count++;
      if (this.traceData.import?.hederaTransactionId) count++;
      return count;
    },
  },
};
</script>

<style scoped>
.supply-chain-traceability {
  width: 100%;
}

.supply-chain-timeline {
  padding: 24px 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.detail-item {
  display: flex;
  flex-direction: column;
}

.detail-item .label {
  font-size: 12px;
  font-weight: 600;
  color: #666;
  margin-bottom: 4px;
}

.detail-item .value {
  font-size: 14px;
  color: #333;
}

.monospace {
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
}

.summary-section {
  background-color: #f9fafb;
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
