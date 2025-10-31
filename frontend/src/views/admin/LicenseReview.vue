<template>
  <Default>
    <div class="license-review">
      <!-- Header -->
      <div class="d-flex justify-space-between align-center mb-6">
        <div>
          <h1 class="text-h4 font-weight-bold">License Review</h1>
          <p class="text-subtitle-1 text--secondary">
            Review and approve exporter license applications
          </p>
        </div>
        <div class="d-flex align-center">
          <v-chip color="primary" small class="mr-4">
            <v-icon left small>mdi-file-document-check</v-icon>
            {{ pendingLicenses.length }} Pending
          </v-chip>
          <v-btn color="primary" @click="refreshLicenses" :loading="loading">
            <v-icon left>mdi-refresh</v-icon>
            Refresh
          </v-btn>
        </div>
      </div>

      <!-- Main Content -->
      <div v-if="pendingLicenses.length > 0" class="license-cards">
        <v-card
          v-for="license in pendingLicenses"
          :key="license.id"
          class="mb-6"
          elevation="2"
        >
          <!-- License Header -->
          <v-card-title class="blue lighten-1 white--text">
            <v-icon left color="white">mdi-file-document</v-icon>
            <div class="flex-grow-1">
              <div class="headline">{{ license.exporterName }}</div>
              <div class="caption">License Application #{{ license.id }}</div>
            </div>
            <v-chip color="orange" small class="white--text">
              <v-icon left small>mdi-clock-outline</v-icon>
              Pending Review
            </v-chip>
          </v-card-title>

          <!-- License Details -->
          <v-card-text>
            <v-row>
              <!-- Exporter Information -->
              <v-col cols="12" md="6">
                <h4 class="font-weight-medium mb-3">Exporter Information</h4>
                <div class="info-grid">
                  <div class="info-row">
                    <span class="info-label">Name:</span>
                    <span class="info-value">{{ license.exporterName }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Email:</span>
                    <span class="info-value">{{ license.exporterEmail }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Phone:</span>
                    <span class="info-value">{{ license.exporterPhone }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Company:</span>
                    <span class="info-value">{{ license.companyName }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Applied Date:</span>
                    <span class="info-value">{{ formatDate(license.applicationDate) }}</span>
                  </div>
                </div>
              </v-col>

              <!-- License Details -->
              <v-col cols="12" md="6">
                <h4 class="font-weight-medium mb-3">License Details</h4>
                <div class="info-grid">
                  <div class="info-row">
                    <span class="info-label">License Type:</span>
                    <span class="info-value">{{ license.licenseType }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Business Type:</span>
                    <span class="info-value">{{ license.businessType }}</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Export Volume:</span>
                    <span class="info-value">{{ license.expectedVolume }} tons/year</span>
                  </div>
                  <div class="info-row">
                    <span class="info-label">Regions:</span>
                    <span class="info-value">{{ license.targetRegions.join(', ') }}</span>
                  </div>
                </div>
              </v-col>
            </v-row>

            <!-- Documents Section -->
            <div class="mt-6">
              <h4 class="font-weight-medium mb-3">Submitted Documents</h4>
              <v-row>
                <v-col
                  v-for="doc in license.documents"
                  :key="doc.id"
                  cols="12"
                  sm="6"
                  md="4"
                >
                  <v-card outlined class="pa-3">
                    <div class="d-flex align-center">
                      <v-icon :color="getDocumentIconColor(doc.type)" class="mr-3">
                        {{ getDocumentIcon(doc.type) }}
                      </v-icon>
                      <div class="flex-grow-1">
                        <div class="font-weight-medium text-body-2">{{ doc.name }}</div>
                        <div class="caption grey--text">{{ doc.type }}</div>
                      </div>
                      <v-btn icon small @click="viewDocument(doc)">
                        <v-icon small>mdi-eye</v-icon>
                      </v-btn>
                    </div>
                  </v-card>
                </v-col>
              </v-row>
            </div>

            <!-- Review Comments -->
            <div class="mt-6">
              <v-textarea
                v-model="license.reviewComments"
                label="Review Comments (Optional)"
                placeholder="Add any comments or notes about this license application..."
                outlined
                rows="3"
                dense
              />
            </div>

            <!-- Action Buttons -->
            <div class="mt-6 d-flex justify-end">
              <v-btn
                color="error"
                outlined
                @click="openRejectDialog(license)"
                :loading="license.rejecting"
                class="mr-3"
              >
                <v-icon left>mdi-close-circle</v-icon>
                Reject
              </v-btn>
              <v-btn
                color="success"
                @click="approveLicense(license)"
                :loading="license.approving"
              >
                <v-icon left>mdi-check-circle</v-icon>
                Approve License
              </v-btn>
            </div>
          </v-card-text>
        </v-card>
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-12">
        <v-icon size="64" color="grey" class="mb-4">mdi-file-document-check-outline</v-icon>
        <h3 class="text-h5 font-weight-medium mb-2">No Pending Licenses</h3>
        <p class="text--secondary">All license applications have been reviewed.</p>
      </div>
    </div>

    <!-- Reject License Dialog -->
    <v-dialog v-model="rejectDialog" max-width="500">
      <v-card>
        <v-card-title class="d-flex align-center">
          <v-icon color="error" class="mr-2">mdi-alert-circle</v-icon>
          Reject License Application
        </v-card-title>
        <v-card-text>
          <p class="mb-4">Are you sure you want to reject the license application for <strong>{{ selectedLicense?.exporterName }}</strong>?</p>
          <v-textarea
            v-model="rejectionReason"
            label="Rejection Reason (Required)"
            placeholder="Please provide a reason for rejection..."
            outlined
            rows="3"
            :rules="[v => !!v || 'Rejection reason is required']"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn @click="closeRejectDialog">Cancel</v-btn>
          <v-btn color="error" @click="confirmReject" :loading="rejecting">
            Reject License
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Loading Overlay -->
    <v-overlay v-model="loading">
      <v-progress-circular indeterminate size="64"></v-progress-circular>
    </v-overlay>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'LicenseReview',
  components: {
    Default,
  },
  data() {
    return {
      loading: false,
      pendingLicenses: [],
      rejectDialog: false,
      selectedLicense: null,
      rejectionReason: '',
      rejecting: false,
    };
  },
  async mounted() {
    await this.loadPendingLicenses();
  },
  methods: {
    async loadPendingLicenses() {
      this.loading = true;
      try {
        const response = await axios.get('/api/admin/licenses/pending');
        this.pendingLicenses = response.data.data.map((license) => ({
          id: license.licenseId, // Map licenseId to id for template
          exporterName: license.exporterName,
          exporterEmail: 'N/A', // Not provided in API
          exporterPhone: 'N/A', // Not provided in API
          companyName: license.companyName,
          applicationDate: license.submittedAt,
          licenseType: license.licenseIdValue || 'Export License',
          businessType: 'Export Business', // Not provided in API
          expectedVolume: 'N/A', // Not provided in API
          targetRegions: ['Global'], // Not provided in API
          documents: license.documentUrl ? [{
            id: 'doc-1',
            name: 'License Document',
            type: 'business_license',
            url: license.documentUrl,
          }] : [],
          reviewComments: '',
          approving: false,
          rejecting: false,
        }));
      } catch (error) {
        this.$toast.error('Failed to load pending licenses');
        this.pendingLicenses = [];
      } finally {
        this.loading = false;
      }
    },

    async approveLicense(license) {
      // eslint-disable-next-line no-param-reassign
      license.approving = true;
      try {
        const response = await axios.post(`/api/admin/licenses/${license.id}/review`, {
          decision: 'APPROVE',
          comments: license.reviewComments,
        });

        if (response.data.success) {
          this.$toast.success('License approved successfully');
          await this.loadPendingLicenses();
        } else {
          this.$toast.error(response.data.message || 'Failed to approve license');
        }
      } catch (error) {
        this.$toast.error('Failed to approve license');
      } finally {
        // eslint-disable-next-line no-param-reassign
        license.approving = false;
      }
    },

    openRejectDialog(license) {
      this.selectedLicense = license;
      this.rejectionReason = '';
      this.rejectDialog = true;
    },

    closeRejectDialog() {
      this.rejectDialog = false;
      this.selectedLicense = null;
      this.rejectionReason = '';
    },

    async confirmReject() {
      if (!this.rejectionReason.trim()) {
        this.$toast.error('Please provide a rejection reason');
        return;
      }

      this.rejecting = true;
      try {
        const response = await axios.post(`/api/admin/licenses/${this.selectedLicense.id}/review`, {
          decision: 'REJECT',
          comments: this.rejectionReason,
        });

        if (response.data.success) {
          this.$toast.success('License rejected successfully');
          this.closeRejectDialog();
          await this.loadPendingLicenses();
        } else {
          this.$toast.error(response.data.message || 'Failed to reject license');
        }
      } catch (error) {
        this.$toast.error('Failed to reject license');
      } finally {
        this.rejecting = false;
      }
    },

    async refreshLicenses() {
      await this.loadPendingLicenses();
    },

    viewDocument(doc) {
      // Open document in new tab or modal
      window.open(doc.url, '_blank');
    },

    getDocumentIcon(type) {
      const icons = {
        business_license: 'mdi-file-document-outline',
        tax_certificate: 'mdi-file-chart',
        identity_proof: 'mdi-card-account-details',
        address_proof: 'mdi-map-marker',
        bank_statement: 'mdi-bank',
        other: 'mdi-file',
      };
      return icons[type] || 'mdi-file';
    },

    getDocumentIconColor(type) {
      const colors = {
        business_license: 'blue',
        tax_certificate: 'green',
        identity_proof: 'purple',
        address_proof: 'orange',
        bank_statement: 'teal',
        other: 'grey',
      };
      return colors[type] || 'grey';
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      });
    },
  },
};
</script>

<style scoped>
.license-review {
  min-height: 100vh;
}
</style>
