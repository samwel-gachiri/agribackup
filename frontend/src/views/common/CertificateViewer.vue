<template>
  <Default class="certificate-viewer">
    <v-container fluid>
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <v-card class="header-card" elevation="3">
            <v-card-title class="primary white--text">
              <v-icon left color="white" size="32">mdi-certificate</v-icon>
              <span class="text-h4">EUDR Compliance Certificates</span>
              <v-spacer></v-spacer>
              <v-icon color="white" size="32">mdi-shield-check</v-icon>
            </v-card-title>
            <v-card-subtitle class="white--text">
              View and validate blockchain-verified compliance certificates
            </v-card-subtitle>
          </v-card>
        </v-col>
      </v-row>

      <!-- Statistics Cards -->
      <v-row class="mt-4">
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card compliant-card" elevation="4">
            <v-card-text>
              <div class="d-flex align-center justify-space-between">
                <div>
                  <div class="text-caption">Total Certificates</div>
                  <div class="text-h4">{{ totalCertificates }}</div>
                </div>
                <v-icon size="48" color="white">mdi-certificate-outline</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card valid-card" elevation="4">
            <v-card-text>
              <div class="d-flex align-center justify-space-between">
                <div>
                  <div class="text-caption">Valid & Active</div>
                  <div class="text-h4">{{ validCertificates }}</div>
                </div>
                <v-icon size="48" color="white">mdi-check-circle</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card transferred-card" elevation="4">
            <v-card-text>
              <div class="d-flex align-center justify-space-between">
                <div>
                  <div class="text-caption">Transferred</div>
                  <div class="text-h4">{{ transferredCertificates }}</div>
                </div>
                <v-icon size="48" color="white">mdi-swap-horizontal</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="stat-card verified-card" elevation="4">
            <v-card-text>
              <div class="d-flex align-center justify-space-between">
                <div>
                  <div class="text-caption">Customs Verified</div>
                  <div class="text-h4">{{ verifiedCertificates }}</div>
                </div>
                <v-icon size="48" color="white">mdi-shield-check-outline</v-icon>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Filters -->
      <v-row class="mt-4">
        <v-col cols="12">
          <v-card elevation="2">
            <v-card-text>
              <v-row>
                <v-col cols="12" md="4">
                  <v-select
                    v-model="filterStatus"
                    :items="statusOptions"
                    label="Filter by Status"
                    outlined
                    dense
                    clearable
                    prepend-inner-icon="mdi-filter"
                  ></v-select>
                </v-col>
                <v-col cols="12" md="4">
                  <v-select
                    v-model="filterComplianceStatus"
                    :items="complianceStatusOptions"
                    label="Filter by Compliance"
                    outlined
                    dense
                    clearable
                    prepend-inner-icon="mdi-shield"
                  ></v-select>
                </v-col>
                <v-col cols="12" md="4">
                  <v-text-field
                    v-model="search"
                    label="Search certificates..."
                    outlined
                    dense
                    clearable
                    prepend-inner-icon="mdi-magnify"
                  ></v-text-field>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Certificates Grid -->
      <v-row class="mt-4">
        <v-col
          cols="12"
          md="6"
          lg="4"
          v-for="certificate in filteredCertificates"
          :key="certificate.id"
        >
          <v-card
            class="certificate-card"
            elevation="3"
            @click="viewCertificate(certificate)"
          >
            <v-card-title class="d-flex align-center">
              <v-icon
                :color="getStatusColor(certificate.eudrComplianceStatus)"
                left
                size="32"
              >
                {{ getStatusIcon(certificate.eudrComplianceStatus) }}
              </v-icon>
              <span class="text-subtitle-1">{{ certificate.shipmentNumber }}</span>
              <v-spacer></v-spacer>
              <v-chip
                :color="getStatusColor(certificate.eudrComplianceStatus)"
                text-color="white"
                small
              >
                {{ certificate.eudrComplianceStatus }}
              </v-chip>
            </v-card-title>
            <v-divider></v-divider>
            <v-card-text>
              <div class="certificate-info">
                <div class="info-row">
                  <v-icon small color="grey darken-1">mdi-truck</v-icon>
                  <span class="ml-2">{{ certificate.produceType }}</span>
                </div>
                <div class="info-row">
                  <v-icon small color="grey darken-1">mdi-weight-kilogram</v-icon>
                  <span class="ml-2">{{ certificate.quantityKg }} kg</span>
                </div>
                <div class="info-row">
                  <v-icon small color="grey darken-1">mdi-map-marker</v-icon>
                  <span class="ml-2">{{ certificate.originCountry }}</span>
                </div>
                <div class="info-row" v-if="certificate.complianceCertificateSerialNumber">
                  <v-icon small color="primary">mdi-fingerprint</v-icon>
                  <span class="ml-2">NFT #{{ certificate.complianceCertificateSerialNumber }}</span>
                </div>
                <div class="info-row">
                  <v-icon small color="grey darken-1">mdi-calendar</v-icon>
                  <span class="ml-2">{{ formatDate(certificate.createdAt) }}</span>
                </div>
              </div>
            </v-card-text>
            <v-divider></v-divider>
            <v-card-actions>
              <v-btn
                text
                color="primary"
                @click.stop="viewCertificate(certificate)"
              >
                <v-icon left>mdi-eye</v-icon>
                View Details
              </v-btn>
              <v-spacer></v-spacer>
              <v-btn
                icon
                color="teal"
                @click.stop="viewOnHedera(certificate)"
                v-if="certificate.hederaTransactionId"
                title="View on Hedera"
              >
                <v-icon>mdi-alpha-h-circle</v-icon>
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>

      <!-- Empty State -->
      <v-row v-if="filteredCertificates.length === 0 && !loading" class="mt-4">
        <v-col cols="12">
          <v-card elevation="2">
            <v-card-text class="text-center py-12">
              <v-icon size="80" color="grey lighten-1">mdi-certificate-outline</v-icon>
              <div class="text-h6 mt-4 grey--text">No certificates found</div>
              <div class="text-subtitle-1 grey--text">
                Certificates will appear here once shipments are compliance-verified
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Loading State -->
      <v-row v-if="loading" class="mt-4">
        <v-col cols="12">
          <v-card elevation="2">
            <v-card-text class="text-center py-12">
              <v-progress-circular
                indeterminate
                color="primary"
                :size="80"
                :width="8"
              ></v-progress-circular>
              <div class="text-h6 mt-4">Loading certificates...</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>

    <!-- Certificate Detail Dialog -->
    <v-dialog v-model="detailDialog" max-width="900px" scrollable>
      <v-card v-if="selectedCertificate">
        <v-card-title class="primary white--text">
          <v-icon left color="white">mdi-certificate</v-icon>
          Certificate Details: {{ selectedCertificate.shipmentNumber }}
          <v-spacer></v-spacer>
          <v-chip
            :color="getStatusColor(selectedCertificate.eudrComplianceStatus)"
            text-color="white"
          >
            {{ selectedCertificate.eudrComplianceStatus }}
          </v-chip>
        </v-card-title>
        <v-divider></v-divider>
        <v-card-text style="max-height: 600px;">
          <v-row class="mt-4">
            <!-- QR Code -->
            <v-col cols="12" md="4" class="text-center">
              <div class="qr-code-container">
                <div ref="qrcode" id="qrcode"></div>
                <div class="text-caption mt-2">Scan to verify on blockchain</div>
              </div>
              <v-divider class="my-4"></v-divider>
              <div class="validity-indicator">
                <v-icon
                  :color="selectedCertificate.eudrComplianceStatus === 'COMPLIANT' ? 'success' : 'error'"
                  size="64"
                >
                  {{ selectedCertificate.eudrComplianceStatus === 'COMPLIANT' ? 'mdi-check-circle' : 'mdi-alert-circle' }}
                </v-icon>
                <div class="text-h6 mt-2">
                  {{ selectedCertificate.eudrComplianceStatus === 'COMPLIANT' ? 'Valid Certificate' : 'Invalid Certificate' }}
                </div>
              </div>
            </v-col>

            <!-- Certificate Information -->
            <v-col cols="12" md="8">
              <v-simple-table>
                <tbody>
                  <tr>
                    <td class="font-weight-bold">Shipment Number</td>
                    <td>{{ selectedCertificate.shipmentNumber }}</td>
                  </tr>
                  <tr>
                    <td class="font-weight-bold">Produce Type</td>
                    <td>{{ selectedCertificate.produceType }}</td>
                  </tr>
                  <tr>
                    <td class="font-weight-bold">Quantity</td>
                    <td>{{ selectedCertificate.quantityKg }} kg</td>
                  </tr>
                  <tr>
                    <td class="font-weight-bold">Origin Country</td>
                    <td>{{ selectedCertificate.originCountry }}</td>
                  </tr>
                  <tr v-if="selectedCertificate.departurePort">
                    <td class="font-weight-bold">Departure Port</td>
                    <td>{{ selectedCertificate.departurePort }}</td>
                  </tr>
                  <tr v-if="selectedCertificate.arrivalPort">
                    <td class="font-weight-bold">Arrival Port</td>
                    <td>{{ selectedCertificate.arrivalPort }}</td>
                  </tr>
                  <tr>
                    <td class="font-weight-bold">Shipment Status</td>
                    <td>
                      <v-chip small :color="getShipmentStatusColor(selectedCertificate.status)">
                        {{ selectedCertificate.status }}
                      </v-chip>
                    </td>
                  </tr>
                </tbody>
              </v-simple-table>

              <v-divider class="my-4"></v-divider>

              <!-- NFT Information -->
              <div class="text-h6 mb-2">Blockchain Certificate (NFT)</div>
              <v-simple-table>
                <tbody>
                  <tr v-if="selectedCertificate.complianceCertificateNftId">
                    <td class="font-weight-bold">NFT Token ID</td>
                    <td>
                      <code>{{ selectedCertificate.complianceCertificateNftId }}</code>
                    </td>
                  </tr>
                  <tr v-if="selectedCertificate.complianceCertificateSerialNumber">
                    <td class="font-weight-bold">Serial Number</td>
                    <td>#{{ selectedCertificate.complianceCertificateSerialNumber }}</td>
                  </tr>
                  <tr v-if="selectedCertificate.currentOwnerAccountId">
                    <td class="font-weight-bold">Current Owner (Hedera Account)</td>
                    <td>
                      <code>{{ selectedCertificate.currentOwnerAccountId }}</code>
                    </td>
                  </tr>
                  <tr v-if="selectedCertificate.complianceCertificateTransactionId">
                    <td class="font-weight-bold">Issuance Transaction</td>
                    <td>
                      <code>{{ selectedCertificate.complianceCertificateTransactionId }}</code>
                    </td>
                  </tr>
                </tbody>
              </v-simple-table>

              <v-divider class="my-4"></v-divider>

              <!-- Dates -->
              <div class="text-h6 mb-2">Timeline</div>
              <v-timeline dense>
                <v-timeline-item
                  small
                  color="success"
                  v-if="selectedCertificate.createdAt"
                >
                  <template v-slot:opposite>
                    <span class="text-caption">{{ formatDate(selectedCertificate.createdAt) }}</span>
                  </template>
                  <div>
                    <div class="font-weight-bold">Shipment Created</div>
                  </div>
                </v-timeline-item>
                <v-timeline-item
                  small
                  color="primary"
                  v-if="selectedCertificate.shippingDate"
                >
                  <template v-slot:opposite>
                    <span class="text-caption">{{ formatDate(selectedCertificate.shippingDate) }}</span>
                  </template>
                  <div>
                    <div class="font-weight-bold">Shipped</div>
                  </div>
                </v-timeline-item>
                <v-timeline-item
                  small
                  color="info"
                  v-if="selectedCertificate.customsClearanceDate"
                >
                  <template v-slot:opposite>
                    <span class="text-caption">{{ formatDate(selectedCertificate.customsClearanceDate) }}</span>
                  </template>
                  <div>
                    <div class="font-weight-bold">Customs Cleared</div>
                  </div>
                </v-timeline-item>
                <v-timeline-item
                  small
                  color="success"
                  v-if="selectedCertificate.actualArrivalDate"
                >
                  <template v-slot:opposite>
                    <span class="text-caption">{{ formatDate(selectedCertificate.actualArrivalDate) }}</span>
                  </template>
                  <div>
                    <div class="font-weight-bold">Arrived</div>
                  </div>
                </v-timeline-item>
              </v-timeline>
            </v-col>
          </v-row>
        </v-card-text>
        <v-divider></v-divider>
        <v-card-actions>
          <v-btn
            color="primary"
            @click="verifyOnBlockchain"
            :loading="verifying"
          >
            <v-icon left>mdi-shield-check</v-icon>
            Verify on Blockchain
          </v-btn>
          <v-btn
            color="teal"
            @click="viewOnHedera(selectedCertificate)"
            v-if="selectedCertificate.hederaTransactionId"
          >
            <v-icon left>mdi-alpha-h-circle</v-icon>
            View on Hedera
          </v-btn>
          <v-spacer></v-spacer>
          <v-btn text @click="detailDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Blockchain Verification Result Dialog -->
    <v-dialog v-model="verificationDialog" max-width="600px">
      <v-card v-if="verificationResult">
        <v-card-title
          :class="verificationResult.certificateValid ? 'success white--text' : 'error white--text'"
        >
          <v-icon left color="white">
            {{ verificationResult.certificateValid ? 'mdi-check-circle' : 'mdi-alert-circle' }}
          </v-icon>
          Blockchain Verification Result
        </v-card-title>
        <v-card-text class="pt-4">
          <v-alert
            :type="verificationResult.approved ? 'success' : 'error'"
            prominent
          >
            <div class="text-h6">{{ verificationResult.message }}</div>
          </v-alert>
          <v-simple-table class="mt-4">
            <tbody>
              <tr>
                <td class="font-weight-bold">Certificate Valid</td>
                <td>
                  <v-icon :color="verificationResult.certificateValid ? 'success' : 'error'">
                    {{ verificationResult.certificateValid ? 'mdi-check' : 'mdi-close' }}
                  </v-icon>
                  {{ verificationResult.certificateValid ? 'Yes' : 'No' }}
                </td>
              </tr>
              <tr>
                <td class="font-weight-bold">Compliance Status</td>
                <td>
                  <v-chip
                    small
                    :color="verificationResult.complianceStatus === 'COMPLIANT' ? 'success' : 'error'"
                    text-color="white"
                  >
                    {{ verificationResult.complianceStatus }}
                  </v-chip>
                </td>
              </tr>
              <tr v-if="verificationResult.certificateNftId">
                <td class="font-weight-bold">NFT ID</td>
                <td><code>{{ verificationResult.certificateNftId }}</code></td>
              </tr>
              <tr v-if="verificationResult.currentOwner">
                <td class="font-weight-bold">Current Owner</td>
                <td><code>{{ verificationResult.currentOwner }}</code></td>
              </tr>
              <tr>
                <td class="font-weight-bold">Verified At</td>
                <td>{{ formatDateTime(verificationResult.verifiedAt) }}</td>
              </tr>
            </tbody>
          </v-simple-table>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="verificationDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </Default>
</template>

<script>
import axios from 'axios';
import QRCode from 'qrcodejs2';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'CertificateViewer',
  components: {
    Default,
  },
  data() {
    return {
      loading: false,
      verifying: false,
      certificates: [],
      search: '',
      filterStatus: null,
      filterComplianceStatus: null,
      statusOptions: [
        'PENDING',
        'IN_TRANSIT',
        'CUSTOMS_CLEARANCE',
        'QUALITY_INSPECTION',
        'DELIVERED',
        'CANCELLED',
      ],
      complianceStatusOptions: [
        'PENDING',
        'COMPLIANT',
        'NON_COMPLIANT',
        'UNDER_REVIEW',
      ],
      detailDialog: false,
      verificationDialog: false,
      selectedCertificate: null,
      verificationResult: null,
    };
  },
  computed: {
    filteredCertificates() {
      let filtered = this.certificates;

      if (this.filterStatus) {
        filtered = filtered.filter((c) => c.status === this.filterStatus);
      }

      if (this.filterComplianceStatus) {
        filtered = filtered.filter(
          (c) => c.eudrComplianceStatus === this.filterComplianceStatus,
        );
      }

      if (this.search) {
        const searchLower = this.search.toLowerCase();
        filtered = filtered.filter(
          (c) => c.shipmentNumber.toLowerCase().includes(searchLower)
            || c.produceType.toLowerCase().includes(searchLower)
            || c.originCountry.toLowerCase().includes(searchLower),
        );
      }

      return filtered;
    },
    totalCertificates() {
      return this.certificates.length;
    },
    validCertificates() {
      return this.certificates.filter(
        (c) => c.eudrComplianceStatus === 'COMPLIANT',
      ).length;
    },
    transferredCertificates() {
      return this.certificates.filter(
        (c) => c.currentOwnerAccountId !== null,
      ).length;
    },
    verifiedCertificates() {
      return this.certificates.filter(
        (c) => c.customsClearanceDate !== null,
      ).length;
    },
  },
  mounted() {
    this.loadCertificates();
  },
  methods: {
    async loadCertificates() {
      this.loading = true;
      try {
        // Load all EUDR certificates
        const response = await axios.get('/api/eudr/certificates');
        if (response.data.success) {
          this.certificates = response.data.data;
        }
      } catch (error) {
        this.$toast.error('Failed to load certificates');
      } finally {
        this.loading = false;
      }
    },
    viewCertificate(certificate) {
      this.selectedCertificate = certificate;
      this.detailDialog = true;

      // Generate QR code after dialog opens
      this.$nextTick(() => {
        this.generateQRCode();
      });
    },
    generateQRCode() {
      // Clear existing QR code
      const qrcodeElement = document.getElementById('qrcode');
      if (qrcodeElement) {
        qrcodeElement.innerHTML = '';

        // Generate QR code with Hedera transaction link
        if (this.selectedCertificate.hederaTransactionId) {
          const hederaUrl = `https://hashscan.io/testnet/transaction/${this.selectedCertificate.hederaTransactionId}`;
          // eslint-disable-next-line no-new
          new QRCode(qrcodeElement, {
            text: hederaUrl,
            width: 200,
            height: 200,
            colorDark: '#000000',
            colorLight: '#ffffff',
            correctLevel: QRCode.CorrectLevel.H,
          });
        }
      }
    },
    async verifyOnBlockchain() {
      if (!this.selectedCertificate) return;

      this.verifying = true;
      try {
        const response = await axios.get(
          `/api/v1/importers/shipments/${this.selectedCertificate.id}/verify-customs`,
        );
        if (response.data.success) {
          this.verificationResult = response.data.data;
          this.verificationDialog = true;
        }
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to verify certificate');
      } finally {
        this.verifying = false;
      }
    },
    viewOnHedera(certificate) {
      if (certificate.hederaTransactionId) {
        const hederaExplorerUrl = `https://hashscan.io/testnet/transaction/${certificate.hederaTransactionId}`;
        window.open(hederaExplorerUrl, '_blank');
      }
    },
    getStatusColor(status) {
      const colors = {
        COMPLIANT: 'success',
        NON_COMPLIANT: 'error',
        PENDING: 'warning',
        UNDER_REVIEW: 'info',
      };
      return colors[status] || 'grey';
    },
    getStatusIcon(status) {
      const icons = {
        COMPLIANT: 'mdi-check-circle',
        NON_COMPLIANT: 'mdi-alert-circle',
        PENDING: 'mdi-clock-outline',
        UNDER_REVIEW: 'mdi-magnify',
      };
      return icons[status] || 'mdi-help-circle';
    },
    getShipmentStatusColor(status) {
      const colors = {
        PENDING: 'grey',
        IN_TRANSIT: 'info',
        CUSTOMS_CLEARANCE: 'warning',
        QUALITY_INSPECTION: 'orange',
        DELIVERED: 'success',
        CANCELLED: 'error',
      };
      return colors[status] || 'grey';
    },
    formatDate(date) {
      if (!date) return 'N/A';
      return new Date(date).toLocaleDateString();
    },
    formatDateTime(date) {
      if (!date) return 'N/A';
      return new Date(date).toLocaleString();
    },
  },
};
</script>

<style scoped>
.certificate-viewer {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px 0;
}

.header-card {
  background: linear-gradient(135deg, #1976d2 0%, #1565c0 100%);
}

.stat-card {
  transition: transform 0.2s;
  color: white;
  cursor: pointer;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.compliant-card {
  background: linear-gradient(135deg, #1976d2, #1565c0);
}

.valid-card {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
}

.transferred-card {
  background: linear-gradient(135deg, #ff9800, #f57c00);
}

.verified-card {
  background: linear-gradient(135deg, #9c27b0, #7b1fa2);
}

.certificate-card {
  transition: all 0.3s;
  cursor: pointer;
  height: 100%;
}

.certificate-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2) !important;
}

.certificate-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
}

.qr-code-container {
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
  display: inline-block;
}

#qrcode {
  display: flex;
  justify-content: center;
}

.validity-indicator {
  text-align: center;
  padding: 16px;
}
</style>
