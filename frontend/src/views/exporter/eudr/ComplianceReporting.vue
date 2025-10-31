<template>
  <Default>
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title class="d-flex align-center">
            <v-icon left color="primary">mdi-file-chart</v-icon>
            <span>Authority Compliance Reporting</span>
            <v-spacer></v-spacer>
            <v-btn color="primary" @click="showGenerateDialog = true">
              <v-icon left>mdi-file-plus</v-icon>
              Generate Report
            </v-btn>
          </v-card-title>

          <!-- Statistics Cards -->
          <v-card-text>
            <v-row>
              <v-col cols="12" md="3">
                <v-card color="blue lighten-5" elevation="0">
                  <v-card-text>
                    <div class="d-flex align-center">
                      <v-icon large color="blue">mdi-file-document-multiple</v-icon>
                      <div class="ml-4">
                        <div class="text-h5 font-weight-bold">{{ statistics.totalReports }}</div>
                        <div class="text-caption">Total Reports</div>
                      </div>
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>

              <v-col cols="12" md="3">
                <v-card color="orange lighten-5" elevation="0">
                  <v-card-text>
                    <div class="d-flex align-center">
                      <v-icon large color="orange">mdi-clock-outline</v-icon>
                      <div class="ml-4">
                        <div class="text-h5 font-weight-bold">{{ statistics.pendingReview }}</div>
                        <div class="text-caption">Pending Review</div>
                      </div>
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>

              <v-col cols="12" md="3">
                <v-card color="green lighten-5" elevation="0">
                  <v-card-text>
                    <div class="d-flex align-center">
                      <v-icon large color="green">mdi-check-circle</v-icon>
                      <div class="ml-4">
                        <div class="text-h5 font-weight-bold">{{ statistics.approved }}</div>
                        <div class="text-caption">Approved</div>
                      </div>
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>

              <v-col cols="12" md="3">
                <v-card color="red lighten-5" elevation="0">
                  <v-card-text>
                    <div class="d-flex align-center">
                      <v-icon large color="red">mdi-alert-circle</v-icon>
                      <div class="ml-4">
                        <div class="text-h5 font-weight-bold">{{ statistics.requiresAction }}</div>
                        <div class="text-caption">Requires Action</div>
                      </div>
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Submission History Table -->
    <v-row>
      <v-col cols="12">
        <v-card>
          <v-card-title>
            <span>Submission History</span>
            <v-spacer></v-spacer>
            <v-text-field
              v-model="search"
              append-icon="mdi-magnify"
              label="Search"
              single-line
              hide-details
              dense
              outlined
              class="mr-2"
              style="max-width: 300px;"
            ></v-text-field>
            <v-select
              v-model="statusFilter"
              :items="statusOptions"
              label="Status"
              dense
              outlined
              hide-details
              clearable
              style="max-width: 200px;"
            ></v-select>
          </v-card-title>

          <v-card-text>
            <v-data-table
              :headers="headers"
              :items="submissions"
              :search="search"
              :loading="loading"
              :items-per-page="10"
              class="elevation-0"
            >
              <template v-slot:item.submittedAt="{ item }">
                {{ formatDateTime(item.submittedAt) }}
              </template>

              <template v-slot:item.status="{ item }">
                <v-chip
                  :color="getStatusColor(item.status)"
                  small
                  dark
                >
                  {{ item.status }}
                </v-chip>
              </template>

              <template v-slot:item.actions="{ item }">
                <v-btn icon small @click="viewReport(item)" title="View Report">
                  <v-icon small>mdi-eye</v-icon>
                </v-btn>
                <v-btn icon small @click="downloadReport(item)" title="Download">
                  <v-icon small>mdi-download</v-icon>
                </v-btn>
                <v-btn
                  icon
                  small
                  @click="checkStatus(item)"
                  title="Check Status"
                  :loading="item.checking"
                >
                  <v-icon small>mdi-refresh</v-icon>
                </v-btn>
                <v-btn
                  icon
                  small
                  @click="viewFeedback(item)"
                  title="View Feedback"
                  v-if="item.authorityFeedback"
                >
                  <v-icon small color="blue">mdi-comment-text</v-icon>
                </v-btn>
              </template>
            </v-data-table>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Generate Report Dialog -->
    <v-dialog v-model="showGenerateDialog" max-width="800" persistent>
      <v-card>
        <v-card-title class="headline">
          <v-icon left color="primary">mdi-file-plus</v-icon>
          Generate Authority Report
        </v-card-title>

        <v-card-text>
          <v-stepper v-model="generationStep" non-linear>
            <v-stepper-header>
              <v-stepper-step :complete="generationStep > 1" step="1" editable>
                Select Batches
              </v-stepper-step>
              <v-divider></v-divider>
              <v-stepper-step :complete="generationStep > 2" step="2" editable>
                Configure Report
              </v-stepper-step>
              <v-divider></v-divider>
              <v-stepper-step step="3">
                Preview & Submit
              </v-stepper-step>
            </v-stepper-header>

            <v-stepper-items>
              <!-- Step 1: Select Batches -->
              <v-stepper-content step="1">
                <v-row>
                  <v-col cols="12">
                    <v-alert type="info" dense outlined>
                      Select one or more batches to include in the authority compliance report.
                    </v-alert>
                  </v-col>

                  <v-col cols="12" md="6">
                    <v-menu
                      v-model="dateRangeMenu"
                      :close-on-content-click="false"
                      max-width="290"
                    >
                      <template v-slot:activator="{ on, attrs }">
                        <v-text-field
                          :value="dateRangeText"
                          label="Date Range"
                          prepend-icon="mdi-calendar"
                          readonly
                          v-bind="attrs"
                          v-on="on"
                          outlined
                          dense
                        ></v-text-field>
                      </template>
                      <v-date-picker
                        v-model="dateRange"
                        range
                        @change="filterBatches"
                      ></v-date-picker>
                    </v-menu>
                  </v-col>

                  <v-col cols="12" md="6">
                    <v-select
                      v-model="batchRiskFilter"
                      :items="riskLevels"
                      label="Risk Level"
                      prepend-icon="mdi-alert-octagon"
                      outlined
                      dense
                      clearable
                      @change="filterBatches"
                    ></v-select>
                  </v-col>

                  <v-col cols="12">
                    <v-data-table
                      v-model="selectedBatches"
                      :headers="batchHeaders"
                      :items="availableBatches"
                      :loading="loadingBatches"
                      show-select
                      item-key="id"
                      class="elevation-1"
                    >
                      <template v-slot:item.riskLevel="{ item }">
                        <v-chip
                          :color="getRiskColor(item.riskLevel)"
                          small
                          dark
                        >
                          {{ item.riskLevel }}
                        </v-chip>
                      </template>

                      <template v-slot:item.quantity="{ item }">
                        {{ item.quantity }} {{ item.unit }}
                      </template>
                    </v-data-table>
                  </v-col>
                </v-row>

                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn text @click="closeGenerateDialog">Cancel</v-btn>
                  <v-btn
                    color="primary"
                    @click="generationStep = 2"
                    :disabled="selectedBatches.length === 0"
                  >
                    Next
                  </v-btn>
                </v-card-actions>
              </v-stepper-content>

              <!-- Step 2: Configure Report -->
              <v-stepper-content step="2">
                <v-row>
                  <v-col cols="12">
                    <v-alert type="info" dense outlined>
                      Configure report settings and select the target authority.
                    </v-alert>
                  </v-col>

                  <v-col cols="12" md="6">
                    <v-select
                      v-model="reportConfig.format"
                      :items="formatOptions"
                      label="Report Format"
                      prepend-icon="mdi-file-document"
                      outlined
                      dense
                    ></v-select>
                  </v-col>

                  <v-col cols="12" md="6">
                    <v-select
                      v-model="reportConfig.authorityCode"
                      :items="authorities"
                      item-text="name"
                      item-value="code"
                      label="Target Authority"
                      prepend-icon="mdi-shield-check"
                      outlined
                      dense
                    ></v-select>
                  </v-col>

                  <v-col cols="12">
                    <v-checkbox
                      v-model="reportConfig.includeDigitalSignature"
                      label="Include Digital Signature"
                      hint="Digitally sign the report for authenticity verification"
                      persistent-hint
                    ></v-checkbox>
                  </v-col>

                  <v-col cols="12">
                    <v-checkbox
                      v-model="reportConfig.includeSensitiveData"
                      label="Include Sensitive Supply Chain Data"
                      hint="Include detailed supplier information and coordinates"
                      persistent-hint
                    ></v-checkbox>
                  </v-col>

                  <v-col cols="12">
                    <v-textarea
                      v-model="reportConfig.notes"
                      label="Additional Notes"
                      hint="Optional notes or context for the authority"
                      outlined
                      rows="3"
                    ></v-textarea>
                  </v-col>

                  <v-col cols="12">
                    <v-alert v-if="selectedBatches.length > 0" type="success" dense outlined>
                      <strong>{{ selectedBatches.length }}</strong> batch(es) selected for this report
                    </v-alert>
                  </v-col>
                </v-row>

                <v-card-actions>
                  <v-btn text @click="generationStep = 1">Back</v-btn>
                  <v-spacer></v-spacer>
                  <v-btn text @click="closeGenerateDialog">Cancel</v-btn>
                  <v-btn color="primary" @click="previewReport" :loading="generating">
                    Preview Report
                  </v-btn>
                </v-card-actions>
              </v-stepper-content>

              <!-- Step 3: Preview & Submit -->
              <v-stepper-content step="3">
                <v-row>
                  <v-col cols="12">
                    <v-alert type="success" dense outlined>
                      Report generated successfully! Review and submit to authority.
                    </v-alert>
                  </v-col>

                  <v-col cols="12">
                    <v-card outlined>
                      <v-card-title class="text-subtitle-1">
                        <v-icon left>mdi-file-document</v-icon>
                        Report Preview
                      </v-card-title>
                      <v-card-text>
                        <div v-if="generatedReport">
                          <v-simple-table dense>
                            <tbody>
                              <tr>
                                <td><strong>Report ID:</strong></td>
                                <td>{{ generatedReport.reportId }}</td>
                              </tr>
                              <tr>
                                <td><strong>Format:</strong></td>
                                <td>{{ reportConfig.format }}</td>
                              </tr>
                              <tr>
                                <td><strong>Batches Included:</strong></td>
                                <td>{{ selectedBatches.length }}</td>
                              </tr>
                              <tr>
                                <td><strong>Target Authority:</strong></td>
                                <td>{{ getAuthorityName(reportConfig.authorityCode) }}</td>
                              </tr>
                              <tr>
                                <td><strong>Generated At:</strong></td>
                                <td>{{ formatDateTime(new Date()) }}</td>
                              </tr>
                              <tr>
                                <td><strong>File Size:</strong></td>
                                <td>{{ generatedReport.fileSize }}</td>
                              </tr>
                            </tbody>
                          </v-simple-table>

                          <v-divider class="my-4"></v-divider>

                          <div class="text-center">
                            <v-btn
                              color="blue"
                              outlined
                              @click="downloadPreview"
                              class="mr-2"
                            >
                              <v-icon left>mdi-download</v-icon>
                              Download Preview
                            </v-btn>
                          </div>
                        </div>
                      </v-card-text>
                    </v-card>
                  </v-col>

                  <v-col cols="12">
                    <v-alert type="warning" dense outlined>
                      <strong>Note:</strong> Submitting this report will send it to the selected authority's system.
                      This action cannot be undone.
                    </v-alert>
                  </v-col>
                </v-row>

                <v-card-actions>
                  <v-btn text @click="generationStep = 2">Back</v-btn>
                  <v-spacer></v-spacer>
                  <v-btn text @click="closeGenerateDialog">Cancel</v-btn>
                  <v-btn
                    color="success"
                    @click="submitReport"
                    :loading="submitting"
                  >
                    <v-icon left>mdi-send</v-icon>
                    Submit to Authority
                  </v-btn>
                </v-card-actions>
              </v-stepper-content>
            </v-stepper-items>
          </v-stepper>
        </v-card-text>
      </v-card>
    </v-dialog>

    <!-- View Report Dialog -->
    <v-dialog v-model="showReportDialog" max-width="900">
      <v-card>
        <v-card-title class="headline">
          <v-icon left>mdi-file-document</v-icon>
          Report Details
          <v-spacer></v-spacer>
          <v-btn icon @click="showReportDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <v-card-text v-if="selectedReport">
          <v-simple-table>
            <tbody>
              <tr>
                <td width="200"><strong>Submission ID:</strong></td>
                <td>{{ selectedReport.submissionId }}</td>
              </tr>
              <tr>
                <td><strong>Batch ID:</strong></td>
                <td>{{ selectedReport.batchId }}</td>
              </tr>
              <tr>
                <td><strong>Authority:</strong></td>
                <td>{{ selectedReport.authorityCode }}</td>
              </tr>
              <tr>
                <td><strong>Submitted At:</strong></td>
                <td>{{ formatDateTime(selectedReport.submittedAt) }}</td>
              </tr>
              <tr>
                <td><strong>Submitted By:</strong></td>
                <td>{{ selectedReport.submittedBy }}</td>
              </tr>
              <tr>
                <td><strong>Status:</strong></td>
                <td>
                  <v-chip :color="getStatusColor(selectedReport.status)" small dark>
                    {{ selectedReport.status }}
                  </v-chip>
                </td>
              </tr>
              <tr>
                <td><strong>Report Filename:</strong></td>
                <td>{{ selectedReport.reportFilename }}</td>
              </tr>
              <tr v-if="selectedReport.notes">
                <td><strong>Notes:</strong></td>
                <td>{{ selectedReport.notes }}</td>
              </tr>
            </tbody>
          </v-simple-table>

          <v-divider class="my-4"></v-divider>

          <div v-if="selectedReport.authorityFeedback">
            <h3 class="mb-3">Authority Feedback</h3>
            <v-alert type="info" outlined>
              {{ selectedReport.authorityFeedback }}
            </v-alert>
          </div>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" text @click="showReportDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Feedback Dialog -->
    <v-dialog v-model="showFeedbackDialog" max-width="600">
      <v-card>
        <v-card-title class="headline">
          <v-icon left color="blue">mdi-comment-text</v-icon>
          Authority Feedback
        </v-card-title>

        <v-card-text v-if="selectedReport">
          <v-alert type="info" outlined>
            {{ selectedReport.authorityFeedback }}
          </v-alert>

          <div class="mt-4">
            <p><strong>Submission ID:</strong> {{ selectedReport.submissionId }}</p>
            <p><strong>Last Updated:</strong> {{ formatDateTime(selectedReport.lastUpdated) }}</p>
          </div>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" text @click="showFeedbackDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar for notifications -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" :timeout="3000">
      {{ snackbarText }}
      <template v-slot:action="{ attrs }">
        <v-btn text v-bind="attrs" @click="snackbar = false">Close</v-btn>
      </template>
    </v-snackbar>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'ComplianceReporting',
  components: {
    Default,
  },
  data() {
    return {
      loading: false,
      loadingBatches: false,
      generating: false,
      submitting: false,
      search: '',
      statusFilter: null,

      // Statistics
      statistics: {
        totalReports: 0,
        pendingReview: 0,
        approved: 0,
        requiresAction: 0,
      },

      // Submissions table
      headers: [
        { text: 'Submission ID', value: 'submissionId' },
        { text: 'Batch Code', value: 'batchCode' },
        { text: 'Authority', value: 'authorityCode' },
        { text: 'Submitted At', value: 'submittedAt' },
        { text: 'Status', value: 'status' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      submissions: [],

      statusOptions: [
        'SUBMITTED',
        'PENDING_REVIEW',
        'UNDER_REVIEW',
        'APPROVED',
        'REJECTED',
        'REQUIRES_ACTION',
      ],

      // Generate report dialog
      showGenerateDialog: false,
      generationStep: 1,
      dateRangeMenu: false,
      dateRange: [],
      batchRiskFilter: null,

      batchHeaders: [
        { text: 'Batch Code', value: 'batchCode' },
        { text: 'Commodity', value: 'commodityDescription' },
        { text: 'Quantity', value: 'quantity' },
        { text: 'Risk Level', value: 'riskLevel' },
        { text: 'Country', value: 'countryOfProduction' },
      ],
      availableBatches: [],
      selectedBatches: [],

      riskLevels: ['NEGLIGIBLE', 'LOW', 'MEDIUM', 'HIGH'],

      reportConfig: {
        format: 'PDF',
        authorityCode: '',
        includeDigitalSignature: false,
        includeSensitiveData: false,
        notes: '',
      },

      formatOptions: ['PDF', 'JSON', 'ZIP'],

      authorities: [
        { code: 'EU-DG-ENV', name: 'EU DG Environment' },
        { code: 'EU-CUSTOMS', name: 'EU Customs Union' },
        { code: 'EUTR-CA', name: 'EUTR Competent Authority' },
        { code: 'LOCAL-FOREST', name: 'Local Forest Authority' },
      ],

      generatedReport: null,

      // View report dialog
      showReportDialog: false,
      selectedReport: null,

      // Feedback dialog
      showFeedbackDialog: false,

      // Snackbar
      snackbar: false,
      snackbarText: '',
      snackbarColor: 'success',
    };
  },

  computed: {
    dateRangeText() {
      return this.dateRange.length === 2
        ? `${this.dateRange[0]} to ${this.dateRange[1]}`
        : 'Select date range';
    },
  },

  mounted() {
    this.loadSubmissionHistory();
    this.loadAvailableBatches();
    this.updateStatistics();
  },

  methods: {
    async loadSubmissionHistory() {
      this.loading = true;
      try {
        const response = await axios.get('/api/eudr/authority-report/submissions');
        if (response.data.success) {
          this.submissions = response.data.data.submissions || [];
        }
      } catch (error) {
        this.showSnackbar('Failed to load submission history', 'error');
      } finally {
        this.loading = false;
      }
    },

    async loadAvailableBatches() {
      this.loadingBatches = true;
      try {
        const response = await axios.get('/api/eudr/batches');
        if (response.data.success) {
          this.availableBatches = response.data.data || [];
        }
      } catch (error) {
        this.showSnackbar('Failed to load batches', 'error');
      } finally {
        this.loadingBatches = false;
      }
    },

    filterBatches() {
      // In production, implement server-side filtering
      this.loadAvailableBatches();
    },

    async previewReport() {
      this.generating = true;
      try {
        const batchIds = this.selectedBatches.map((b) => b.id);

        // Generate report for first batch as preview - fetch as blob for PDF
        const response = await axios.get(`/api/eudr/authority-report/${batchIds[0]}`, {
          params: {
            format: this.reportConfig.format,
            includeDigitalSignature: this.reportConfig.includeDigitalSignature,
          },
          responseType: this.reportConfig.format === 'PDF' ? 'blob' : 'json',
        });

        this.generatedReport = {
          reportId: `REP-${Date.now()}`,
          fileSize: this.reportConfig.format === 'PDF' ? this.formatFileSize(response.data.size || 0) : 'N/A',
          content: response.data,
          format: this.reportConfig.format,
        };

        this.generationStep = 3;
        this.showSnackbar('Report generated successfully', 'success');
      } catch (error) {
        this.showSnackbar('Failed to generate report', 'error');
      } finally {
        this.generating = false;
      }
    },

    async submitReport() {
      this.submitting = true;
      try {
        const batchId = this.selectedBatches[0].id; // In production, handle multiple batches

        const response = await axios.post('/api/eudr/authority-report/submit', null, {
          params: {
            batchId,
            authorityCode: this.reportConfig.authorityCode,
            notes: this.reportConfig.notes,
          },
        });

        if (response.data.success) {
          this.showSnackbar('Report submitted successfully to authority', 'success');
          this.closeGenerateDialog();
          this.loadSubmissionHistory();
          this.updateStatistics();
        }
      } catch (error) {
        this.showSnackbar('Failed to submit report', 'error');
      } finally {
        this.submitting = false;
      }
    },

    downloadPreview() {
      if (!this.generatedReport || !this.generatedReport.content) {
        this.showSnackbar('No report available for download', 'error');
        return;
      }

      try {
        // Use the blob directly (already created from axios response)
        const blob = this.generatedReport.content;
        const url = window.URL.createObjectURL(blob);

        // Create download link
        const link = document.createElement('a');
        link.href = url;
        link.download = this.generatedReport.filename || `authority-report-${this.selectedBatches[0]?.batchCode || 'preview'}.pdf`;
        document.body.appendChild(link);
        link.click();
        link.remove();

        // Clean up the object URL
        window.URL.revokeObjectURL(url);

        this.showSnackbar('Report downloaded successfully', 'success');
      } catch (error) {
        this.showSnackbar('Failed to download report', 'error');
      }
    },

    viewReport(item) {
      this.selectedReport = item;
      this.showReportDialog = true;
    },

    async downloadReport(item) {
      try {
        const response = await axios.get(`/api/eudr/authority-report/${item.batchId}`, {
          params: { format: 'PDF' },
          responseType: 'blob',
        });

        const blob = new Blob([response.data], { type: 'application/pdf' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = item.reportFilename || `compliance-report-${item.batchCode}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);

        this.showSnackbar('Report downloaded successfully', 'success');
      } catch (error) {
        this.showSnackbar('Failed to download report', 'error');
      }
    },

    async checkStatus(item) {
      this.$set(item, 'checking', true);
      try {
        const response = await axios.get(`/api/eudr/authority-report/submission-status/${item.submissionId}`);
        if (response.data.success) {
          const status = response.data.data;
          // eslint-disable-next-line no-param-reassign
          item.status = status.status;
          // eslint-disable-next-line no-param-reassign
          item.lastUpdated = status.lastUpdated;
          // eslint-disable-next-line no-param-reassign
          item.authorityFeedback = status.authorityFeedback;
          this.showSnackbar('Status updated', 'success');
        }
      } catch (error) {
        this.showSnackbar('Failed to check status', 'error');
      } finally {
        this.$set(item, 'checking', false);
      }
    },

    viewFeedback(item) {
      this.selectedReport = item;
      this.showFeedbackDialog = true;
    },

    updateStatistics() {
      this.statistics = {
        totalReports: this.submissions.length,
        pendingReview: this.submissions.filter((s) => s.status === 'PENDING_REVIEW').length,
        approved: this.submissions.filter((s) => s.status === 'APPROVED').length,
        requiresAction: this.submissions.filter((s) => s.status === 'REQUIRES_ACTION').length,
      };
    },

    closeGenerateDialog() {
      this.showGenerateDialog = false;
      this.generationStep = 1;
      this.selectedBatches = [];
      this.reportConfig = {
        format: 'PDF',
        authorityCode: '',
        includeDigitalSignature: false,
        includeSensitiveData: false,
        notes: '',
      };
      this.generatedReport = null;
    },

    getStatusColor(status) {
      const colors = {
        SUBMITTED: 'blue',
        PENDING_REVIEW: 'orange',
        UNDER_REVIEW: 'orange',
        APPROVED: 'green',
        REJECTED: 'red',
        REQUIRES_ACTION: 'red',
      };
      return colors[status] || 'grey';
    },

    getRiskColor(riskLevel) {
      const colors = {
        NEGLIGIBLE: 'green',
        LOW: 'blue',
        MEDIUM: 'orange',
        HIGH: 'red',
      };
      return colors[riskLevel] || 'grey';
    },

    getAuthorityName(code) {
      const authority = this.authorities.find((a) => a.code === code);
      return authority ? authority.name : code;
    },

    formatDateTime(dateTime) {
      if (!dateTime) return 'N/A';
      const date = new Date(dateTime);
      return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      return `${parseFloat((bytes / (k ** i)).toFixed(2))} ${sizes[i]}`;
    },
  },
};
</script>

<style scoped>
.v-stepper {
  box-shadow: none;
}

.v-data-table >>> .v-data-table__wrapper {
  max-height: 400px;
  overflow-y: auto;
}
</style>
