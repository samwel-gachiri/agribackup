<template>
  <default>
    <v-container fluid class="supply-chain-container">
      <v-row>
        <v-col cols="12">
          <div class="page-header">
            <v-icon large color="primary" class="mr-3">mdi-package-variant-closed</v-icon>
            <div>
              <h1 class="text-h4 font-weight-bold">EUDR Supply Chain Management</h1>
              <p class="text-subtitle-1 text--secondary">
                Create and manage the complete supply chain from farm to export
              </p>
            </div>
          </div>
        </v-col>
      </v-row>

    <!-- Supply Chain Stepper -->
    <v-row>
      <v-col cols="12">
        <v-card class="supply-chain-card">
          <v-card-title class="primary white--text">
            <v-icon left dark>mdi-timeline-check</v-icon>
            Supply Chain Workflow
          </v-card-title>
          <v-card-text class="pa-4">
            <v-stepper v-model="currentStep" vertical>
              <!-- Step 1: Create Aggregator -->
              <v-stepper-step :complete="currentStep > 1" step="1" color="success">
                Create Aggregator
                <small v-if="aggregator.id">Created: {{ aggregator.organizationName }}</small>
              </v-stepper-step>
              <v-stepper-content step="1">
                <v-card flat>
                  <v-card-text>
                    <v-form ref="aggregatorForm">
                      <v-text-field
                        v-model="aggregator.organizationName"
                        label="Organization Name"
                        required
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="aggregator.registrationNumber"
                        label="Registration Number"
                        required
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="aggregator.facilityAddress"
                        label="Facility Address"
                        required
                        outlined
                        dense
                      />
                      <v-row>
                        <v-col cols="6">
                          <v-text-field
                            v-model.number="aggregator.storageCapacityTons"
                            label="Storage Capacity (tons)"
                            type="number"
                            outlined
                            dense
                          />
                        </v-col>
                        <v-col cols="6">
                          <v-text-field
                            v-model.number="aggregator.collectionRadiusKm"
                            label="Collection Radius (km)"
                            type="number"
                            outlined
                            dense
                          />
                        </v-col>
                      </v-row>
                      <v-text-field
                        v-model="aggregator.primaryCommodities"
                        label="Primary Commodities"
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="aggregator.email"
                        label="Email"
                        type="email"
                        required
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="aggregator.phoneNumber"
                        label="Phone Number"
                        required
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="aggregator.fullName"
                        label="Contact Person Full Name"
                        required
                        outlined
                        dense
                      />
                    </v-form>
                  </v-card-text>
                  <v-card-actions>
                    <v-btn color="primary" @click="createAggregator" :loading="loading">
                      Create Aggregator
                    </v-btn>
                  </v-card-actions>
                </v-card>
              </v-stepper-content>

              <!-- Step 2: Record Farmer Collections -->
              <v-stepper-step :complete="currentStep > 2" step="2" color="success">
                Record Farmer Collections
                <small v-if="collections.length > 0">{{ collections.length }} collection(s) recorded</small>
              </v-stepper-step>
              <v-stepper-content step="2">
                <v-card flat>
                  <v-card-text>
                    <v-alert type="info" text dense class="mb-4">
                      Record collections from individual farmers at the aggregation center
                    </v-alert>
                    <v-form ref="collectionForm">
                      <v-text-field
                        v-model="collection.farmerId"
                        label="Farmer ID"
                        required
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="collection.produceType"
                        label="Produce Type (e.g., COFFEE)"
                        required
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model.number="collection.quantityKg"
                        label="Quantity (kg)"
                        type="number"
                        required
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="collection.qualityGrade"
                        label="Quality Grade"
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model.number="collection.pricePerKg"
                        label="Price per Kg"
                        type="number"
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="collection.collectionDate"
                        label="Collection Date"
                        type="date"
                        outlined
                        dense
                      />
                    </v-form>

                    <v-list v-if="collections.length > 0" class="mt-4">
                      <v-subheader>Recorded Collections</v-subheader>
                      <v-list-item v-for="(col, index) in collections" :key="index">
                        <v-list-item-content>
                          <v-list-item-title>
                            Farmer: {{ col.farmerId }} - {{ col.quantityKg }}kg {{ col.produceType }}
                          </v-list-item-title>
                          <v-list-item-subtitle>
                            Grade: {{ col.qualityGrade }} | Hedera TX: {{ col.hederaTransactionId || 'Pending' }}
                          </v-list-item-subtitle>
                        </v-list-item-content>
                      </v-list-item>
                    </v-list>
                  </v-card-text>
                  <v-card-actions>
                    <v-btn color="primary" @click="recordCollection" :loading="loading">
                      Record Collection
                    </v-btn>
                    <v-btn color="success" @click="currentStep = 3" :disabled="collections.length === 0">
                      Continue to Batch Creation
                    </v-btn>
                  </v-card-actions>
                </v-card>
              </v-stepper-content>

              <!-- Step 3: Create Consolidated Batch -->
              <v-stepper-step :complete="currentStep > 3" step="3" color="success">
                Create Consolidated Batch
                <small v-if="batch.id">Batch: {{ batch.batchNumber }}</small>
              </v-stepper-step>
              <v-stepper-content step="3">
                <v-card flat>
                  <v-card-text>
                    <v-alert type="info" text dense class="mb-4">
                      Consolidate farmer collections into a single batch
                    </v-alert>
                    <v-form ref="batchForm">
                      <v-text-field
                        v-model="batch.batchNumber"
                        label="Batch Number"
                        required
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="batch.produceType"
                        label="Produce Type"
                        required
                        outlined
                        dense
                      />
                      <v-textarea
                        v-model="batch.aggregationEventIds"
                        label="Collection IDs (comma-separated)"
                        outlined
                        dense
                        rows="2"
                        hint="Enter the IDs of collections to include in this batch"
                      />
                      <v-text-field
                        v-model.number="batch.totalQuantityKg"
                        label="Total Quantity (kg)"
                        type="number"
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model.number="batch.numberOfFarmers"
                        label="Number of Farmers"
                        type="number"
                        outlined
                        dense
                      />
                    </v-form>
                  </v-card-text>
                  <v-card-actions>
                    <v-btn color="primary" @click="createBatch" :loading="loading">
                      Create Batch
                    </v-btn>
                    <v-btn text @click="currentStep = 2">Back</v-btn>
                  </v-card-actions>
                </v-card>
              </v-stepper-content>

              <!-- Step 4: Create Processor -->
              <v-stepper-step :complete="currentStep > 4" step="4" color="success">
                Create Processor
                <small v-if="processor.id">Created: {{ processor.facilityName }}</small>
              </v-stepper-step>
              <v-stepper-content step="4">
                <v-card flat>
                  <v-card-text>
                    <v-form ref="processorForm">
                      <v-text-field
                        v-model="processor.facilityName"
                        label="Facility Name"
                        required
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="processor.facilityAddress"
                        label="Facility Address"
                        required
                        outlined
                        dense
                      />
                      <v-textarea
                        v-model="processor.processingCapabilities"
                        label="Processing Capabilities"
                        outlined
                        dense
                        rows="2"
                      />
                      <v-textarea
                        v-model="processor.certificationDetails"
                        label="Certification Details"
                        outlined
                        dense
                        rows="2"
                      />
                      <v-text-field
                        v-model="processor.email"
                        label="Email"
                        type="email"
                        required
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="processor.phoneNumber"
                        label="Phone Number"
                        required
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="processor.fullName"
                        label="Contact Person Full Name"
                        required
                        outlined
                        dense
                      />
                    </v-form>
                  </v-card-text>
                  <v-card-actions>
                    <v-btn color="primary" @click="createProcessor" :loading="loading">
                      Create Processor
                    </v-btn>
                    <v-btn text @click="currentStep = 3">Back</v-btn>
                  </v-card-actions>
                </v-card>
              </v-stepper-content>

              <!-- Step 5: Record Processing Event -->
              <v-stepper-step :complete="currentStep > 5" step="5" color="success">
                Record Processing Event
                <small v-if="processingEvent.id">Event recorded</small>
              </v-stepper-step>
              <v-stepper-content step="5">
                <v-card flat>
                  <v-card-text>
                    <v-form ref="processingEventForm">
                      <v-text-field
                        v-model="processingEvent.processType"
                        label="Process Type (e.g., ROASTING_AND_PACKAGING)"
                        required
                        outlined
                        dense
                      />
                      <v-row>
                        <v-col cols="6">
                          <v-text-field
                            v-model.number="processingEvent.inputQuantityKg"
                            label="Input Quantity (kg)"
                            type="number"
                            outlined
                            dense
                          />
                        </v-col>
                        <v-col cols="6">
                          <v-text-field
                            v-model.number="processingEvent.outputQuantityKg"
                            label="Output Quantity (kg)"
                            type="number"
                            outlined
                            dense
                          />
                        </v-col>
                      </v-row>
                      <v-text-field
                        v-model="processingEvent.processingDate"
                        label="Processing Date"
                        type="date"
                        outlined
                        dense
                      />
                      <v-text-field
                        v-model="processingEvent.qualityGrade"
                        label="Quality Grade"
                        outlined
                        dense
                      />
                      <v-textarea
                        v-model="processingEvent.processParameters"
                        label="Process Parameters"
                        outlined
                        dense
                        rows="2"
                      />
                      <v-textarea
                        v-model="processingEvent.packagingDetails"
                        label="Packaging Details"
                        outlined
                        dense
                        rows="2"
                      />
                    </v-form>
                  </v-card-text>
                  <v-card-actions>
                    <v-btn color="primary" @click="recordProcessingEvent" :loading="loading">
                      Record Processing
                    </v-btn>
                    <v-btn text @click="currentStep = 4">Back</v-btn>
                  </v-card-actions>
                </v-card>
              </v-stepper-content>

              <!-- Step 6: Summary -->
              <v-stepper-step step="6" color="success">
                Supply Chain Summary
              </v-stepper-step>
              <v-stepper-content step="6">
                <v-card flat>
                  <v-card-text>
                    <v-alert type="success" text>
                      Supply chain successfully created and recorded on Hedera blockchain!
                    </v-alert>

                    <div class="summary-section">
                      <h3 class="text-h6 mb-3">
                        <v-icon left color="success">mdi-check-circle</v-icon>
                        Supply Chain Traceability
                      </h3>

                      <v-simple-table>
                        <tbody>
                          <tr>
                            <td class="font-weight-bold">Aggregator</td>
                            <td>{{ aggregator.organizationName }}</td>
                            <td>
                              <v-chip small color="success" text-color="white">
                                {{ aggregator.id }}
                              </v-chip>
                            </td>
                          </tr>
                          <tr>
                            <td class="font-weight-bold">Collections</td>
                            <td>{{ collections.length }} farmer(s)</td>
                            <td>
                              Total: {{ collections.reduce((sum, c) => sum + c.quantityKg, 0) }}kg
                            </td>
                          </tr>
                          <tr>
                            <td class="font-weight-bold">Batch</td>
                            <td>{{ batch.batchNumber }}</td>
                            <td>
                              <v-chip small color="primary" text-color="white">
                                {{ batch.id }}
                              </v-chip>
                            </td>
                          </tr>
                          <tr>
                            <td class="font-weight-bold">Processor</td>
                            <td>{{ processor.facilityName }}</td>
                            <td>
                              <v-chip small color="success" text-color="white">
                                {{ processor.id }}
                              </v-chip>
                            </td>
                          </tr>
                          <tr v-if="processingEvent.id">
                            <td class="font-weight-bold">Processing</td>
                            <td>{{ processingEvent.processType }}</td>
                            <td>
                              {{ processingEvent.inputQuantityKg }}kg → {{ processingEvent.outputQuantityKg }}kg
                            </td>
                          </tr>
                        </tbody>
                      </v-simple-table>

                      <v-divider class="my-4"></v-divider>

                      <div class="hedera-section">
                        <h4 class="text-subtitle-1 mb-2">
                          <v-icon left color="primary">mdi-blockchain</v-icon>
                          Hedera Blockchain Records
                        </h4>
                        <v-list dense>
                          <v-list-item v-if="aggregator.hederaAccountId">
                            <v-list-item-icon>
                              <v-icon color="success">mdi-check</v-icon>
                            </v-list-item-icon>
                            <v-list-item-content>
                              <v-list-item-title>Aggregator: {{ aggregator.hederaAccountId }}</v-list-item-title>
                            </v-list-item-content>
                          </v-list-item>
                          <v-list-item v-for="(col, index) in collections" :key="'col-' + index">
                            <v-list-item-icon>
                              <v-icon color="success">mdi-check</v-icon>
                            </v-list-item-icon>
                            <v-list-item-content>
                              <v-list-item-title>
                                Collection {{ index + 1 }}: {{ col.hederaTransactionId || 'Pending' }}
                              </v-list-item-title>
                            </v-list-item-content>
                          </v-list-item>
                          <v-list-item v-if="batch.hederaTransactionId">
                            <v-list-item-icon>
                              <v-icon color="success">mdi-check</v-icon>
                            </v-list-item-icon>
                            <v-list-item-content>
                              <v-list-item-title>Batch: {{ batch.hederaTransactionId }}</v-list-item-title>
                            </v-list-item-content>
                          </v-list-item>
                          <v-list-item v-if="processor.hederaAccountId">
                            <v-list-item-icon>
                              <v-icon color="success">mdi-check</v-icon>
                            </v-list-item-icon>
                            <v-list-item-content>
                              <v-list-item-title>Processor: {{ processor.hederaAccountId }}</v-list-item-title>
                            </v-list-item-content>
                          </v-list-item>
                          <v-list-item v-if="processingEvent.hederaTransactionId">
                            <v-list-item-icon>
                              <v-icon color="success">mdi-check</v-icon>
                            </v-list-item-icon>
                            <v-list-item-content>
                              <v-list-item-title>
                                Processing: {{ processingEvent.hederaTransactionId }}
                              </v-list-item-title>
                            </v-list-item-content>
                          </v-list-item>
                        </v-list>
                      </div>
                    </div>
                  </v-card-text>
                  <v-card-actions>
                    <v-btn color="primary" @click="resetWorkflow">
                      Create New Supply Chain
                    </v-btn>
                  </v-card-actions>
                </v-card>
              </v-stepper-content>
            </v-stepper>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Snackbar for notifications -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="4000" top>
      {{ snackbarMessage }}
      <template v-slot:action="{ attrs }">
        <v-btn text v-bind="attrs" @click="snackbar = false">Close</v-btn>
      </template>
    </v-snackbar>
  </v-container>
  </default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'SupplyChainManagement',
  components: {
    Default,
  },
  data() {
    return {
      currentStep: 1,
      loading: false,
      snackbar: false,
      snackbarMessage: '',
      snackbarColor: 'success',

      // Aggregator data
      aggregator: {
        id: null,
        organizationName: '',
        registrationNumber: '',
        facilityAddress: '',
        storageCapacityTons: null,
        collectionRadiusKm: null,
        primaryCommodities: '',
        email: '',
        phoneNumber: '',
        fullName: '',
        hederaAccountId: null,
      },

      // Collection data
      collection: {
        farmerId: '',
        produceType: 'COFFEE',
        quantityKg: null,
        qualityGrade: '',
        pricePerKg: null,
        collectionDate: new Date().toISOString().substr(0, 10),
      },
      collections: [],

      // Batch data
      batch: {
        id: null,
        batchNumber: '',
        produceType: 'COFFEE',
        aggregationEventIds: '',
        totalQuantityKg: null,
        numberOfFarmers: null,
        hederaTransactionId: null,
      },

      // Processor data
      processor: {
        id: null,
        facilityName: '',
        facilityAddress: '',
        processingCapabilities: '',
        certificationDetails: '',
        email: '',
        phoneNumber: '',
        fullName: '',
        hederaAccountId: null,
      },

      // Processing event data
      processingEvent: {
        id: null,
        processType: 'ROASTING_AND_PACKAGING',
        inputQuantityKg: null,
        outputQuantityKg: null,
        processingDate: new Date().toISOString().substr(0, 10),
        qualityGrade: '',
        processParameters: '',
        packagingDetails: '',
        hederaTransactionId: null,
      },
    };
  },
  methods: {
    async createAggregator() {
      this.loading = true;
      try {
        const response = await axios.post('/api/v1/aggregators', this.aggregator);
        this.aggregator.id = response.data.id;
        this.aggregator.hederaAccountId = response.data.hederaAccountId;
        this.showSuccess('Aggregator created successfully!');
        this.currentStep = 2;
      } catch (error) {
        this.showError(`Failed to create aggregator: ${error.response?.data?.message || error.message}`);
      } finally {
        this.loading = false;
      }
    },

    async recordCollection() {
      this.loading = true;
      try {
        const payload = {
          ...this.collection,
          aggregatorId: this.aggregator.id,
        };
        const response = await axios.post('/api/v1/aggregators/collections', payload);
        this.collections.push(response.data);
        this.showSuccess('Collection recorded successfully!');
        // Reset collection form
        this.collection = {
          farmerId: '',
          produceType: 'COFFEE',
          quantityKg: null,
          qualityGrade: '',
          pricePerKg: null,
          collectionDate: new Date().toISOString().substr(0, 10),
        };
      } catch (error) {
        this.showError(`Failed to record collection: ${error.response?.data?.message || error.message}`);
      } finally {
        this.loading = false;
      }
    },

    async createBatch() {
      this.loading = true;
      try {
        const payload = {
          ...this.batch,
          aggregatorId: this.aggregator.id,
          aggregationEventIds: this.batch.aggregationEventIds.split(',').map((id) => id.trim()),
        };
        const response = await axios.post('/api/v1/aggregators/batches', payload);
        this.batch.id = response.data.id;
        this.batch.hederaTransactionId = response.data.hederaTransactionId;
        this.showSuccess('Batch created successfully!');
        this.currentStep = 4;
      } catch (error) {
        this.showError(`Failed to create batch: ${error.response?.data?.message || error.message}`);
      } finally {
        this.loading = false;
      }
    },

    async createProcessor() {
      this.loading = true;
      try {
        const response = await axios.post('/api/v1/processors', this.processor);
        this.processor.id = response.data.id;
        this.processor.hederaAccountId = response.data.hederaAccountId;
        this.showSuccess('Processor created successfully!');
        this.currentStep = 5;
      } catch (error) {
        this.showError(`Failed to create processor: ${error.response?.data?.message || error.message}`);
      } finally {
        this.loading = false;
      }
    },

    async recordProcessingEvent() {
      this.loading = true;
      try {
        const payload = {
          ...this.processingEvent,
          processorId: this.processor.id,
          sourceBatchId: this.batch.id,
        };
        const response = await axios.post('/api/v1/processors/processing-events', payload);
        this.processingEvent.id = response.data.id;
        this.processingEvent.hederaTransactionId = response.data.hederaTransactionId;
        this.showSuccess('Processing event recorded successfully!');
        this.currentStep = 6;
      } catch (error) {
        this.showError(`Failed to record processing event: ${error.response?.data?.message || error.message}`);
      } finally {
        this.loading = false;
      }
    },

    resetWorkflow() {
      this.currentStep = 1;
      this.aggregator = {
        id: null,
        organizationName: '',
        registrationNumber: '',
        facilityAddress: '',
        storageCapacityTons: null,
        collectionRadiusKm: null,
        primaryCommodities: '',
        email: '',
        phoneNumber: '',
        fullName: '',
        hederaAccountId: null,
      };
      this.collections = [];
      this.batch = {
        id: null,
        batchNumber: '',
        produceType: 'COFFEE',
        aggregationEventIds: '',
        totalQuantityKg: null,
        numberOfFarmers: null,
        hederaTransactionId: null,
      };
      this.processor = {
        id: null,
        facilityName: '',
        facilityAddress: '',
        processingCapabilities: '',
        certificationDetails: '',
        email: '',
        phoneNumber: '',
        fullName: '',
        hederaAccountId: null,
      };
      this.processingEvent = {
        id: null,
        processType: 'ROASTING_AND_PACKAGING',
        inputQuantityKg: null,
        outputQuantityKg: null,
        processingDate: new Date().toISOString().substr(0, 10),
        qualityGrade: '',
        processParameters: '',
        packagingDetails: '',
        hederaTransactionId: null,
      };
    },

    showSuccess(message) {
      this.snackbarMessage = message;
      this.snackbarColor = 'success';
      this.snackbar = true;
    },

    showError(message) {
      this.snackbarMessage = message;
      this.snackbarColor = 'error';
      this.snackbar = true;
    },
  },
};
</script>

<style scoped>
.supply-chain-container {
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  margin-bottom: 24px;
}

.supply-chain-card {
  border-radius: 12px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.summary-section {
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.hedera-section {
  padding: 16px;
  background: #e3f2fd;
  border-radius: 8px;
  margin-top: 16px;
}
</style>
