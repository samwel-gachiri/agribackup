<template>
  <div class="aggregator-dashboard">
    <v-container fluid>
      <!-- Header Section -->
      <v-row>
        <v-col cols="12">
          <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
            <div>
              <h1 class="tw-text-3xl tw-font-bold tw-text-gray-800">Aggregator Dashboard</h1>
              <p class="tw-text-gray-600 tw-mt-1">Manage farmer collections and consolidated batches</p>
            </div>
            <v-btn color="primary" large @click="showCollectionDialog = true">
              <v-icon left>mdi-plus</v-icon>
              New Collection
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- Statistics Cards -->
      <v-row>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="green lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-green-700">{{ statistics.totalCollectionEvents || 0 }}</div>
                  <div class="stat-label">Total Collections</div>
                </div>
                <v-icon size="48" color="green">mdi-package-variant</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="blue lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-blue-700">{{ formatNumber(statistics.totalVolumeCollectedKg) }} kg</div>
                  <div class="stat-label">Total Volume</div>
                </div>
                <v-icon size="48" color="blue">mdi-weight-kilogram</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="orange lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-orange-700">{{ statistics.totalBatchesCreated || 0 }}</div>
                  <div class="stat-label">Batches Created</div>
                </div>
                <v-icon size="48" color="orange">mdi-cube-send</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" sm="6" md="3">
          <v-card class="stats-card" color="red lighten-5">
            <v-card-text>
              <div class="tw-flex tw-items-center tw-justify-between">
                <div>
                  <div class="stat-value tw-text-red-700">{{ statistics.pendingPayments || 0 }}</div>
                  <div class="stat-label">Pending Payments</div>
                </div>
                <v-icon size="48" color="red">mdi-currency-usd-circle</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Tabs for Collections and Batches -->
      <v-row>
        <v-col cols="12">
          <v-card>
            <v-tabs v-model="activeTab" background-color="primary" dark>
              <v-tab>
                <v-icon left>mdi-clipboard-list</v-icon>
                Collection Events
              </v-tab>
              <v-tab>
                <v-icon left>mdi-package-variant-closed</v-icon>
                Consolidated Batches
              </v-tab>
              <v-tab>
                <v-icon left>mdi-chart-bar</v-icon>
                Analytics
              </v-tab>
            </v-tabs>

            <v-tabs-items v-model="activeTab">
              <!-- Collection Events Tab -->
              <v-tab-item>
                <v-card-text>
                  <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
                    <v-text-field
                      v-model="collectionSearch"
                      prepend-inner-icon="mdi-magnify"
                      label="Search collections..."
                      outlined
                      dense
                      hide-details
                      clearable
                      style="max-width: 400px"
                    ></v-text-field>
                    <v-btn outlined color="primary" @click="createConsolidatedBatch">
                      <v-icon left>mdi-package-variant</v-icon>
                      Create Batch
                    </v-btn>
                  </div>

                  <v-data-table
                    :headers="collectionHeaders"
                    :items="collectionEvents"
                    :loading="loadingCollections"
                    :search="collectionSearch"
                    class="elevation-1"
                  >
                    <template v-slot:item.farmerName="{ item }">
                      <div class="tw-font-medium">{{ item.farmerName }}</div>
                    </template>

                    <template v-slot:item.quantityKg="{ item }">
                      <v-chip small color="blue lighten-4">
                        {{ formatNumber(item.quantityKg) }} kg
                      </v-chip>
                    </template>

                    <template v-slot:item.qualityGrade="{ item }">
                      <v-chip small :color="getQualityColor(item.qualityGrade)">
                        {{ item.qualityGrade }}
                      </v-chip>
                    </template>

                    <template v-slot:item.paymentStatus="{ item }">
                      <v-chip small :color="getPaymentStatusColor(item.paymentStatus)" text-color="white">
                        {{ item.paymentStatus }}
                      </v-chip>
                    </template>

                    <template v-slot:item.hederaTransactionId="{ item }">
                      <HederaTransactionViewer
                        v-if="item.hederaTransactionId"
                        mode="compact"
                        :transaction-id="item.hederaTransactionId"
                        :network="hederaNetwork"
                      />
                      <span v-else class="tw-text-gray-400">Not recorded</span>
                    </template>

                    <template v-slot:item.actions="{ item }">
                      <v-btn
                        v-if="item.paymentStatus === 'PENDING'"
                        x-small
                        color="success"
                        @click="markAsPaid(item)"
                      >
                        Mark Paid
                      </v-btn>
                      <v-btn x-small text color="primary" @click="viewCollectionDetails(item)">
                        <v-icon small>mdi-eye</v-icon>
                      </v-btn>
                    </template>
                  </v-data-table>
                </v-card-text>
              </v-tab-item>

              <!-- Consolidated Batches Tab -->
              <v-tab-item>
                <v-card-text>
                  <v-data-table
                    :headers="batchHeaders"
                    :items="consolidatedBatches"
                    :loading="loadingBatches"
                    class="elevation-1"
                  >
                    <template v-slot:item.batchNumber="{ item }">
                      <div class="tw-font-medium tw-text-blue-600">{{ item.batchNumber }}</div>
                    </template>

                    <template v-slot:item.totalQuantityKg="{ item }">
                      <v-chip small color="blue lighten-4">
                        {{ formatNumber(item.totalQuantityKg) }} kg
                      </v-chip>
                    </template>

                    <template v-slot:item.numberOfFarmers="{ item }">
                      <v-chip small color="green lighten-4">
                        {{ item.numberOfFarmers }} farmers
                      </v-chip>
                    </template>

                    <template v-slot:item.status="{ item }">
                      <v-chip small :color="getBatchStatusColor(item.status)" text-color="white">
                        {{ item.status }}
                      </v-chip>
                    </template>

                    <template v-slot:item.hederaVerification="{ item }">
                      <HederaTransactionViewer
                        v-if="item.hederaTransactionId"
                        mode="compact"
                        :transaction-id="item.hederaTransactionId"
                        :network="hederaNetwork"
                        :document-hash="item.hederaBatchHash"
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
                          <v-list-item @click="viewBatchDetails(item)">
                            <v-list-item-icon>
                              <v-icon small>mdi-eye</v-icon>
                            </v-list-item-icon>
                            <v-list-item-title>View Details</v-list-item-title>
                          </v-list-item>
                          <v-list-item v-if="item.status === 'PENDING'" @click="markBatchShipped(item)">
                            <v-list-item-icon>
                              <v-icon small>mdi-truck</v-icon>
                            </v-list-item-icon>
                            <v-list-item-title>Mark Shipped</v-list-item-title>
                          </v-list-item>
                        </v-list>
                      </v-menu>
                    </template>
                  </v-data-table>
                </v-card-text>
              </v-tab-item>

              <!-- Analytics Tab -->
              <v-tab-item>
                <v-card-text>
                  <v-row>
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

                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="tw-text-base">Monthly Performance</v-card-title>
                        <v-card-text>
                          <div class="tw-text-center tw-py-4">
                            <div class="tw-text-4xl tw-font-bold tw-text-blue-600">
                              {{ formatNumber(statistics.currentMonthVolumeKg) }} kg
                            </div>
                            <div class="tw-text-sm tw-text-gray-600 tw-mt-2">Current Month Volume</div>
                          </div>
                          <v-divider class="tw-my-4"></v-divider>
                          <div class="tw-text-center">
                            <div class="tw-text-2xl tw-font-bold tw-text-green-600">
                              {{ formatNumber(statistics.averageCollectionSizeKg) }} kg
                            </div>
                            <div class="tw-text-sm tw-text-gray-600 tw-mt-2">Average Collection Size</div>
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

    <!-- Collection Event Dialog -->
    <v-dialog v-model="showCollectionDialog" max-width="800" persistent>
      <v-card>
        <v-card-title class="tw-bg-primary tw-text-white">
          <v-icon color="white" class="tw-mr-2">mdi-package-variant</v-icon>
          Record New Collection
          <v-spacer></v-spacer>
          <v-btn icon dark @click="showCollectionDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <v-card-text class="tw-pt-4">
          <v-form ref="collectionForm" v-model="collectionFormValid">
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="newCollection.farmerId"
                  label="Farmer ID *"
                  outlined
                  dense
                  :rules="[rules.required]"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="newCollection.farmerName"
                  label="Farmer Name *"
                  outlined
                  dense
                  :rules="[rules.required]"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="newCollection.produceType"
                  label="Produce Type *"
                  outlined
                  dense
                  :rules="[rules.required]"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model.number="newCollection.quantityKg"
                  label="Quantity (kg) *"
                  type="number"
                  outlined
                  dense
                  :rules="[rules.required, rules.positive]"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="newCollection.collectionDate"
                  label="Collection Date *"
                  type="date"
                  outlined
                  dense
                  :rules="[rules.required]"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-select
                  v-model="newCollection.qualityGrade"
                  label="Quality Grade *"
                  :items="['A', 'B', 'C']"
                  outlined
                  dense
                  :rules="[rules.required]"
                ></v-select>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model.number="newCollection.pricePerKg"
                  label="Price per kg *"
                  type="number"
                  step="0.01"
                  outlined
                  dense
                  :rules="[rules.required, rules.positive]"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  :value="calculateTotal"
                  label="Total Payment"
                  outlined
                  dense
                  readonly
                  prefix="$"
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-textarea
                  v-model="newCollection.notes"
                  label="Notes"
                  outlined
                  dense
                  rows="3"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>

        <v-card-actions class="tw-px-6 tw-pb-4">
          <v-spacer></v-spacer>
          <v-btn text @click="showCollectionDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="submitting" @click="submitCollection">
            <v-icon left>mdi-check</v-icon>
            Record Collection
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import HederaTransactionViewer from '@/components/shared/HederaTransactionViewer.vue';

export default {
  name: 'AggregatorDashboard',
  components: {
    HederaTransactionViewer,
  },
  data() {
    return {
      activeTab: 0,
      hederaNetwork: 'testnet',
      statistics: {},
      collectionEvents: [],
      consolidatedBatches: [],
      loadingCollections: false,
      loadingBatches: false,
      collectionSearch: '',
      showCollectionDialog: false,
      collectionFormValid: false,
      submitting: false,
      newCollection: {
        farmerId: '',
        farmerName: '',
        produceType: '',
        quantityKg: null,
        collectionDate: new Date().toISOString().split('T')[0],
        qualityGrade: '',
        pricePerKg: null,
        notes: '',
      },
      rules: {
        required: (v) => !!v || 'Required field',
        positive: (v) => (v && v > 0) || 'Must be greater than 0',
      },
      collectionHeaders: [
        { text: 'Farmer', value: 'farmerName' },
        { text: 'Produce', value: 'produceType' },
        { text: 'Quantity', value: 'quantityKg' },
        { text: 'Quality', value: 'qualityGrade' },
        { text: 'Collection Date', value: 'collectionDate' },
        { text: 'Payment', value: 'paymentStatus' },
        { text: 'Hedera', value: 'hederaTransactionId' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      batchHeaders: [
        { text: 'Batch Number', value: 'batchNumber' },
        { text: 'Produce Type', value: 'produceType' },
        { text: 'Total Quantity', value: 'totalQuantityKg' },
        { text: 'Farmers', value: 'numberOfFarmers' },
        { text: 'Date', value: 'consolidationDate' },
        { text: 'Status', value: 'status' },
        { text: 'Hedera', value: 'hederaVerification' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
    };
  },
  computed: {
    aggregatorId() {
      return this.$store.state.user?.entityId || null;
    },
    calculateTotal() {
      if (this.newCollection.quantityKg && this.newCollection.pricePerKg) {
        return (this.newCollection.quantityKg * this.newCollection.pricePerKg).toFixed(2);
      }
      return '0.00';
    },
  },
  mounted() {
    this.loadData();
  },
  methods: {
    async loadData() {
      await Promise.all([
        this.loadStatistics(),
        this.loadCollectionEvents(),
        this.loadConsolidatedBatches(),
      ]);
    },

    async loadStatistics() {
      const response = await axios.get(`/api/v1/aggregators/${this.aggregatorId}/statistics`);
      this.statistics = response.data;
    },

    async loadCollectionEvents() {
      this.loadingCollections = true;
      const response = await axios.get(`/api/v1/aggregators/${this.aggregatorId}/collection-events`);
      this.collectionEvents = response.data.content;
      this.loadingCollections = false;
    },

    async loadConsolidatedBatches() {
      this.loadingBatches = true;
      const response = await axios.get(`/api/v1/aggregators/${this.aggregatorId}/consolidated-batches`);
      this.consolidatedBatches = response.data.content;
      this.loadingBatches = false;
    },

    async submitCollection() {
      if (!this.$refs.collectionForm.validate()) return;

      this.submitting = true;
      try {
        const payload = {
          ...this.newCollection,
          aggregatorId: this.aggregatorId,
          totalPayment: parseFloat(this.calculateTotal),
          paymentStatus: 'PENDING',
        };

        await axios.post(`/api/v1/aggregators/${this.aggregatorId}/collection-events`, payload);

        this.$toast.success('Collection recorded successfully!');
        this.showCollectionDialog = false;
        this.$refs.collectionForm.reset();
        await this.loadData();
      } catch (error) {
        this.$toast.error(`Failed to record collection: ${error.response?.data?.message || error.message}`);
      } finally {
        this.submitting = false;
      }
    },

    async markAsPaid(item) {
      try {
        await axios.patch(`/api/v1/aggregators/collection-events/${item.id}/payment`, {
          eventId: item.id,
          paymentStatus: 'PAID',
        });
        this.$toast.success('Payment status updated!');
        await this.loadCollectionEvents();
      } catch (error) {
        this.$toast.error('Failed to update payment status');
      }
    },

    viewCollectionDetails() {
      // Navigate to details view
    },

    createConsolidatedBatch() {
      // Navigate to batch creation wizard
      this.$router.push({ name: 'CreateConsolidatedBatch' });
    },

    viewBatchDetails() {
      // Navigate to batch details
    },

    async markBatchShipped(item) {
      try {
        await axios.patch(`/api/v1/aggregators/consolidated-batches/${item.id}/status?status=SHIPPED`);
        this.$toast.success('Batch marked as shipped!');
        await this.loadConsolidatedBatches();
      } catch (error) {
        this.$toast.error('Failed to update batch status');
      }
    },

    formatNumber(num) {
      return num ? num.toLocaleString() : '0';
    },

    getQualityColor(grade) {
      const colors = { A: 'green', B: 'blue', C: 'orange' };
      return colors[grade] || 'grey';
    },

    getPaymentStatusColor(status) {
      const colors = { PAID: 'success', PENDING: 'warning', CANCELLED: 'error' };
      return colors[status] || 'grey';
    },

    getBatchStatusColor(status) {
      const colors = {
        PENDING: 'warning',
        SHIPPED: 'info',
        DELIVERED: 'success',
        CANCELLED: 'error',
      };
      return colors[status] || 'grey';
    },
  },
};
</script>

<style scoped>
.aggregator-dashboard {
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
