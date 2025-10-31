<template>
  <div class="data-verification">
    <v-container fluid>
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <div class="d-flex align-center justify-space-between mb-4">
            <div class="d-flex align-center">
              <v-icon large color="green" class="mr-3">mdi-database-check</v-icon>
              <div>
                <h1 class="text-h4 font-weight-bold">Data Verification</h1>
                <p class="text-subtitle-1 text--secondary mb-0">
                  Verify and validate EUDR compliance data integrity
                </p>
              </div>
            </div>
            <v-btn
              color="primary"
              @click="runFullVerification"
              elevation="2"
              :loading="verifying"
            >
              <v-icon left>mdi-shield-check</v-icon>
              Run Full Verification
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- Verification Overview -->
      <v-row class="mb-4">
        <v-col cols="12" sm="6" md="3">
          <v-card class="verify-card verified" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-check-circle</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ verifiedRecords }}</div>
              <div class="text-subtitle-2">Verified Records</div>
              <div class="text-caption">Blockchain confirmed</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="verify-card pending" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-clock-outline</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ pendingRecords }}</div>
              <div class="text-subtitle-2">Pending Verification</div>
              <div class="text-caption">Awaiting confirmation</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="verify-card failed" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-alert-circle</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ failedRecords }}</div>
              <div class="text-subtitle-2">Failed Verification</div>
              <div class="text-caption">Require attention</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="verify-card integrity" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-shield-check</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ integrityScore }}%</div>
              <div class="text-subtitle-2">Integrity Score</div>
              <div class="text-caption">Overall system</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Verification Tabs -->
      <v-row>
        <v-col cols="12">
          <v-card elevation="3">
            <v-tabs v-model="activeTab" background-color="primary" dark>
              <v-tab>
                <v-icon left>mdi-database</v-icon>
                Data Integrity
              </v-tab>
              <v-tab>
                <v-icon left>mdi-link-variant</v-icon>
                Blockchain Verification
              </v-tab>
              <v-tab>
                <v-icon left>mdi-file-document</v-icon>
                Document Verification
              </v-tab>
              <v-tab>
                <v-icon left>mdi-map-marker</v-icon>
                Geospatial Validation
              </v-tab>
            </v-tabs>

            <v-tabs-items v-model="activeTab">
              <!-- Data Integrity Tab -->
              <v-tab-item>
                <v-card-text>
                  <div class="d-flex justify-space-between align-center mb-4">
                    <h3>Data Integrity Checks</h3>
                    <v-btn color="primary" @click="runIntegrityCheck" :loading="checkingIntegrity">
                      <v-icon left>mdi-refresh</v-icon>
                      Run Check
                    </v-btn>
                  </div>

                  <v-row>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="text-h6">
                          <v-icon color="success" class="mr-2">mdi-database-check</v-icon>
                          Database Consistency
                        </v-card-title>
                        <v-card-text>
                          <div class="integrity-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Foreign Key Integrity</span>
                              <v-chip color="success" text-color="white" x-small>
                                Passed
                              </v-chip>
                            </div>
                          </div>
                          <div class="integrity-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Data Type Validation</span>
                              <v-chip color="success" text-color="white" x-small>
                                Passed
                              </v-chip>
                            </div>
                          </div>
                          <div class="integrity-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Constraint Violations</span>
                              <v-chip color="warning" text-color="white" x-small>
                                3 Issues
                              </v-chip>
                            </div>
                          </div>
                          <div class="integrity-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Orphaned Records</span>
                              <v-chip color="success" text-color="white" x-small>
                                None Found
                              </v-chip>
                            </div>
                          </div>
                        </v-card-text>
                      </v-card>
                    </v-col>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="text-h6">
                          <v-icon color="info" class="mr-2">mdi-chart-line</v-icon>
                          Data Quality Metrics
                        </v-card-title>
                        <v-card-text>
                          <div class="quality-metric">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Completeness</span>
                              <span class="font-weight-bold">{{ dataQuality.completeness }}%</span>
                            </div>
                            <v-progress-linear
                              :value="dataQuality.completeness"
                              color="success"
                              height="8"
                              rounded
                            ></v-progress-linear>
                          </div>
                          <div class="quality-metric">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Accuracy</span>
                              <span class="font-weight-bold">{{ dataQuality.accuracy }}%</span>
                            </div>
                            <v-progress-linear
                              :value="dataQuality.accuracy"
                              color="primary"
                              height="8"
                              rounded
                            ></v-progress-linear>
                          </div>
                          <div class="quality-metric">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Consistency</span>
                              <span class="font-weight-bold">{{ dataQuality.consistency }}%</span>
                            </div>
                            <v-progress-linear
                              :value="dataQuality.consistency"
                              color="warning"
                              height="8"
                              rounded
                            ></v-progress-linear>
                          </div>
                          <div class="quality-metric">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Timeliness</span>
                              <span class="font-weight-bold">{{ dataQuality.timeliness }}%</span>
                            </div>
                            <v-progress-linear
                              :value="dataQuality.timeliness"
                              color="info"
                              height="8"
                              rounded
                            ></v-progress-linear>
                          </div>
                        </v-card-text>
                      </v-card>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-tab-item>

              <!-- Blockchain Verification Tab -->
              <v-tab-item>
                <v-card-text>
                  <div class="d-flex justify-space-between align-center mb-4">
                    <h3>Blockchain Verification Status</h3>
                    <v-btn color="primary" @click="verifyBlockchain" :loading="verifyingBlockchain">
                      <v-icon left>mdi-link-variant</v-icon>
                      Verify All
                    </v-btn>
                  </div>

                  <v-data-table
                    :headers="blockchainHeaders"
                    :items="blockchainRecords"
                    :loading="loading"
                    class="elevation-0"
                  >
                    <template v-slot:item.recordType="{ item }">
                      <v-chip
                        :color="getRecordTypeColor(item.recordType)"
                        text-color="white"
                        small
                      >
                        {{ item.recordType }}
                      </v-chip>
                    </template>
                    <template v-slot:item.verificationStatus="{ item }">
                      <v-chip
                        :color="getVerificationColor(item.verificationStatus)"
                        text-color="white"
                        small
                      >
                        {{ item.verificationStatus }}
                      </v-chip>
                    </template>
                    <template v-slot:item.lastVerified="{ item }">
                      {{ formatDate(item.lastVerified) }}
                    </template>
                    <template v-slot:item.actions="{ item }">
                      <v-btn
                        icon
                        small
                        color="primary"
                        @click="viewBlockchainRecord(item)"
                      >
                        <v-icon>mdi-eye</v-icon>
                      </v-btn>
                      <v-btn
                        icon
                        small
                        color="info"
                        @click="reverifyRecord(item)"
                      >
                        <v-icon>mdi-refresh</v-icon>
                      </v-btn>
                    </template>
                  </v-data-table>
                </v-card-text>
              </v-tab-item>

              <!-- Document Verification Tab -->
              <v-tab-item>
                <v-card-text>
                  <div class="d-flex justify-space-between align-center mb-4">
                    <h3>Document Verification</h3>
                    <v-btn color="primary" @click="verifyDocuments" :loading="verifyingDocs">
                      <v-icon left>mdi-file-check</v-icon>
                      Verify Documents
                    </v-btn>
                  </div>

                  <v-row>
                    <v-col cols="12" md="8">
                      <v-data-table
                        :headers="documentHeaders"
                        :items="documentRecords"
                        :loading="loading"
                        class="elevation-0"
                      >
                        <template v-slot:item.documentType="{ item }">
                          <v-chip
                            :color="getDocumentTypeColor(item.documentType)"
                            text-color="white"
                            small
                          >
                            {{ item.documentType }}
                          </v-chip>
                        </template>
                        <template v-slot:item.verificationStatus="{ item }">
                          <v-chip
                            :color="getVerificationColor(item.verificationStatus)"
                            text-color="white"
                            small
                          >
                            {{ item.verificationStatus }}
                          </v-chip>
                        </template>
                        <template v-slot:item.actions="{ item }">
                          <v-btn
                            icon
                            small
                            color="primary"
                            @click="viewDocument(item)"
                          >
                            <v-icon>mdi-eye</v-icon>
                          </v-btn>
                          <v-btn
                            icon
                            small
                            color="info"
                            @click="reverifyDocument(item)"
                          >
                            <v-icon>mdi-refresh</v-icon>
                          </v-btn>
                        </template>
                      </v-data-table>
                    </v-col>
                    <v-col cols="12" md="4">
                      <v-card outlined>
                        <v-card-title class="text-h6">
                          <v-icon color="warning" class="mr-2">mdi-file-alert</v-icon>
                          Document Issues
                        </v-card-title>
                        <v-card-text>
                          <v-list dense>
                            <v-list-item
                              v-for="issue in documentIssues"
                              :key="issue.id"
                              class="px-0"
                            >
                              <v-list-item-icon class="mr-2">
                                <v-icon :color="issue.severity === 'High' ? 'error' : 'warning'" small>
                                  mdi-alert-circle
                                </v-icon>
                              </v-list-item-icon>
                              <v-list-item-content>
                                <v-list-item-title class="text-caption">
                                  {{ issue.description }}
                                </v-list-item-title>
                                <v-list-item-subtitle class="text-caption">
                                  {{ issue.document }}
                                </v-list-item-subtitle>
                              </v-list-item-content>
                            </v-list-item>
                          </v-list>
                        </v-card-text>
                      </v-card>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-tab-item>

              <!-- Geospatial Validation Tab -->
              <v-tab-item>
                <v-card-text>
                  <div class="d-flex justify-space-between align-center mb-4">
                    <h3>Geospatial Data Validation</h3>
                    <v-btn color="primary" @click="validateGeospatial" :loading="validatingGeo">
                      <v-icon left>mdi-map-check</v-icon>
                      Validate All
                    </v-btn>
                  </div>

                  <v-row>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="text-h6">
                          <v-icon color="success" class="mr-2">mdi-map-marker-check</v-icon>
                          Coordinate Validation
                        </v-card-title>
                        <v-card-text>
                          <div class="geo-validation-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Valid WGS84 Coordinates</span>
                              <v-chip color="success" text-color="white" x-small>
                                {{ geoValidation.validCoordinates }}
                              </v-chip>
                            </div>
                          </div>
                          <div class="geo-validation-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Invalid Coordinates</span>
                              <v-chip color="error" text-color="white" x-small>
                                {{ geoValidation.invalidCoordinates }}
                              </v-chip>
                            </div>
                          </div>
                          <div class="geo-validation-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Polygon Integrity</span>
                              <v-chip color="success" text-color="white" x-small>
                                {{ geoValidation.validPolygons }}%
                              </v-chip>
                            </div>
                          </div>
                          <div class="geo-validation-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Area Calculations</span>
                              <v-chip color="warning" text-color="white" x-small>
                                {{ geoValidation.areaAccuracy }}% Accurate
                              </v-chip>
                            </div>
                          </div>
                        </v-card-text>
                      </v-card>
                    </v-col>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="text-h6">
                          <v-icon color="info" class="mr-2">mdi-satellite-variant</v-icon>
                          Satellite Data Sync
                        </v-card-title>
                        <v-card-text>
                          <div class="sync-status">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Last Sync</span>
                              <span class="font-weight-bold">{{ satelliteSync.lastSync }}</span>
                            </div>
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Sync Status</span>
                              <v-chip color="success" text-color="white" x-small>
                                {{ satelliteSync.status }}
                              </v-chip>
                            </div>
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Coverage</span>
                              <span class="font-weight-bold">{{ satelliteSync.coverage }}%</span>
                            </div>
                            <v-progress-linear
                              :value="satelliteSync.coverage"
                              color="primary"
                              height="8"
                              rounded
                              class="mb-4"
                            ></v-progress-linear>
                            <v-btn color="primary" block @click="syncSatelliteData" :loading="syncingSatellite">
                              <v-icon left>mdi-sync</v-icon>
                              Sync Now
                            </v-btn>
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
  </div>
</template>

<script>
export default {
  name: 'DataVerification',
  data() {
    return {
      verifying: false,
      activeTab: 0,
      loading: false,
      checkingIntegrity: false,
      verifyingBlockchain: false,
      verifyingDocs: false,
      validatingGeo: false,
      syncingSatellite: false,

      // Overview stats
      verifiedRecords: 2847,
      pendingRecords: 156,
      failedRecords: 23,
      integrityScore: 94,

      // Data quality metrics
      dataQuality: {
        completeness: 96,
        accuracy: 92,
        consistency: 88,
        timeliness: 94,
      },

      // Blockchain verification
      blockchainHeaders: [
        { text: 'Record ID', value: 'recordId' },
        { text: 'Type', value: 'recordType' },
        { text: 'Transaction ID', value: 'transactionId' },
        { text: 'Status', value: 'verificationStatus' },
        { text: 'Last Verified', value: 'lastVerified' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      blockchainRecords: [
        {
          recordId: 'BATCH-001',
          recordType: 'Batch',
          transactionId: '0.0.123456@1703123456.789',
          verificationStatus: 'Verified',
          lastVerified: '2024-12-15',
        },
        {
          recordId: 'DOC-002',
          recordType: 'Document',
          transactionId: '0.0.123456@1703123457.890',
          verificationStatus: 'Pending',
          lastVerified: '2024-12-14',
        },
      ],

      // Document verification
      documentHeaders: [
        { text: 'Document ID', value: 'documentId' },
        { text: 'Type', value: 'documentType' },
        { text: 'Owner', value: 'owner' },
        { text: 'Status', value: 'verificationStatus' },
        { text: 'Hash Match', value: 'hashMatch' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      documentRecords: [
        {
          documentId: 'DOC-001',
          documentType: 'License',
          owner: 'Green Valley Farm',
          verificationStatus: 'Verified',
          hashMatch: true,
        },
        {
          documentId: 'DOC-002',
          documentType: 'Certificate',
          owner: 'Mountain Coffee Co.',
          verificationStatus: 'Failed',
          hashMatch: false,
        },
      ],

      documentIssues: [
        {
          id: 1,
          description: 'Hash mismatch detected',
          document: 'Certificate DOC-002',
          severity: 'High',
        },
        {
          id: 2,
          description: 'Missing EXIF data',
          document: 'Photo DOC-003',
          severity: 'Medium',
        },
      ],

      // Geospatial validation
      geoValidation: {
        validCoordinates: 2456,
        invalidCoordinates: 12,
        validPolygons: 97,
        areaAccuracy: 94,
      },

      satelliteSync: {
        lastSync: '2024-12-15 06:00 AM',
        status: 'Up to Date',
        coverage: 95,
      },
    };
  },
  methods: {
    async runFullVerification() {
      this.verifying = true;
      try {
        // Simulate full verification
        await new Promise((resolve) => setTimeout(resolve, 3000));
        this.$toast.success('Full verification completed successfully');
      } catch (error) {
        this.$toast.error('Verification failed');
      } finally {
        this.verifying = false;
      }
    },

    async runIntegrityCheck() {
      this.checkingIntegrity = true;
      try {
        await new Promise((resolve) => setTimeout(resolve, 2000));
        this.$toast.success('Data integrity check completed');
      } finally {
        this.checkingIntegrity = false;
      }
    },

    async verifyBlockchain() {
      this.verifyingBlockchain = true;
      try {
        await new Promise((resolve) => setTimeout(resolve, 2500));
        this.$toast.success('Blockchain verification completed');
      } finally {
        this.verifyingBlockchain = false;
      }
    },

    async verifyDocuments() {
      this.verifyingDocs = true;
      try {
        await new Promise((resolve) => setTimeout(resolve, 2000));
        this.$toast.success('Document verification completed');
      } finally {
        this.verifyingDocs = false;
      }
    },

    async validateGeospatial() {
      this.validatingGeo = true;
      try {
        await new Promise((resolve) => setTimeout(resolve, 2500));
        this.$toast.success('Geospatial validation completed');
      } finally {
        this.validatingGeo = false;
      }
    },

    async syncSatelliteData() {
      this.syncingSatellite = true;
      try {
        await new Promise((resolve) => setTimeout(resolve, 3000));
        this.satelliteSync.lastSync = new Date().toLocaleString();
        this.$toast.success('Satellite data synchronized');
      } finally {
        this.syncingSatellite = false;
      }
    },

    getRecordTypeColor(type) {
      const colors = {
        Batch: 'primary',
        Document: 'success',
        Assessment: 'warning',
        Alert: 'error',
      };
      return colors[type] || 'grey';
    },

    getVerificationColor(status) {
      const colors = {
        Verified: 'success',
        Pending: 'warning',
        Failed: 'error',
      };
      return colors[status] || 'grey';
    },

    getDocumentTypeColor(type) {
      const colors = {
        License: 'primary',
        Certificate: 'success',
        Invoice: 'info',
        Report: 'warning',
      };
      return colors[type] || 'grey';
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString();
    },

    viewBlockchainRecord() {
    },

    reverifyRecord() {
    },

    viewDocument() {
    },

    reverifyDocument() {
    },
  },
};
</script>

<style scoped>
.data-verification {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px 0;
}

.verify-card {
  transition: transform 0.2s;
  color: white;
}

.verify-card:hover {
  transform: translateY(-2px);
}

.verify-card.verified {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
}

.verify-card.pending {
  background: linear-gradient(135deg, #ff9800, #f57c00);
}

.verify-card.failed {
  background: linear-gradient(135deg, #f44336, #c62828);
}

.verify-card.integrity {
  background: linear-gradient(135deg, #2196f3, #1565c0);
}

.integrity-item,
.quality-metric,
.geo-validation-item {
  margin-bottom: 16px;
}

.sync-status {
  background: rgba(0, 0, 0, 0.02);
  padding: 12px;
  border-radius: 8px;
}
</style>
