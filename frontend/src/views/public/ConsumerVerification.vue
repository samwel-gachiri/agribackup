<template>
  <div class="consumer-verification">
    <!-- Header Bar -->
    <div class="verification-header">
      <div class="header-content">
        <div class="logo-section">
          <v-icon size="32" color="white">mdi-leaf-circle</v-icon>
          <span class="logo-text">AgriBackup</span>
        </div>
        <div class="tagline">Blockchain Verified Supply Chain</div>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <v-progress-circular
        indeterminate
        color="green"
        size="64"
      ></v-progress-circular>
      <p class="loading-text">Verifying product authenticity...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="error-container">
      <v-icon size="80" color="red">mdi-alert-circle-outline</v-icon>
      <h2>Verification Failed</h2>
      <p>{{ error }}</p>
      <v-btn color="primary" @click="retryVerification">
        <v-icon left>mdi-refresh</v-icon>
        Try Again
      </v-btn>
    </div>

    <!-- Verified Content -->
    <div v-else-if="verificationData" class="verification-content">
      <!-- Verification Badge -->
      <div class="verification-badge" :class="verificationData.compliance.eudrCompliant ? 'compliant' : 'non-compliant'">
        <v-icon size="48" color="white">
          {{ verificationData.compliance.eudrCompliant ? 'mdi-check-decagram' : 'mdi-alert' }}
        </v-icon>
        <div class="badge-text">
          <h2>{{ verificationData.compliance.eudrCompliant ? 'EUDR Verified' : 'Verification Issue' }}</h2>
          <p>{{ verificationData.compliance.eudrCompliant ? 'Deforestation-Free Product' : 'Please contact supplier' }}</p>
        </div>
      </div>

      <!-- Product Info Card -->
      <v-card class="info-card product-card" elevation="2">
        <v-card-title class="card-header">
          <v-icon left color="green">mdi-package-variant</v-icon>
          Product Information
        </v-card-title>
        <v-card-text>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">Product</span>
              <span class="value">{{ verificationData.product.commodity }}</span>
            </div>
            <div class="info-item">
              <span class="label">Quantity</span>
              <span class="value">{{ verificationData.product.quantity }} {{ verificationData.product.unit }}</span>
            </div>
            <div class="info-item">
              <span class="label">Origin</span>
              <span class="value">
                <v-icon small color="green" class="mr-1">mdi-map-marker</v-icon>
                {{ verificationData.origin.country }}
              </span>
            </div>
            <div class="info-item" v-if="verificationData.product.harvestDate">
              <span class="label">Harvest Date</span>
              <span class="value">{{ formatDate(verificationData.product.harvestDate) }}</span>
            </div>
          </div>
        </v-card-text>
      </v-card>

      <!-- Origin & Farmers Card -->
      <v-card class="info-card origin-card" elevation="2">
        <v-card-title class="card-header">
          <v-icon left color="brown">mdi-account-group</v-icon>
          Source Information
        </v-card-title>
        <v-card-text>
          <div class="stats-row">
            <div class="stat-item">
              <div class="stat-value">{{ verificationData.origin.farmerCount }}</div>
              <div class="stat-label">Farmers</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ verificationData.origin.productionUnitCount }}</div>
              <div class="stat-label">Production Units</div>
            </div>
            <div class="stat-item">
              <v-icon :color="verificationData.origin.isDeforestationFree ? 'green' : 'red'" size="32">
                {{ verificationData.origin.isDeforestationFree ? 'mdi-tree' : 'mdi-tree-off' }}
              </v-icon>
              <div class="stat-label">{{ verificationData.origin.isDeforestationFree ? 'Deforestation Free' : 'Check Required' }}</div>
            </div>
          </div>
        </v-card-text>
      </v-card>

      <!-- Supply Chain Journey -->
      <v-card class="info-card journey-card" elevation="2">
        <v-card-title class="card-header">
          <v-icon left color="blue">mdi-truck-delivery</v-icon>
          Supply Chain Journey
        </v-card-title>
        <v-card-text>
          <v-timeline dense align-top class="journey-timeline">
            <v-timeline-item
              v-for="step in verificationData.supplyChainJourney"
              :key="step.step"
              :color="getStepColor(step.step)"
              small
            >
              <div class="timeline-content">
                <div class="step-header">
                  <span class="step-number">{{ step.step }}</span>
                  <span class="step-event">{{ step.event }}</span>
                </div>
                <div class="step-details" v-if="step.location || step.date">
                  <span v-if="step.location" class="step-location">
                    <v-icon x-small>mdi-map-marker</v-icon> {{ step.location }}
                  </span>
                  <span v-if="step.date" class="step-date">
                    <v-icon x-small>mdi-calendar</v-icon> {{ formatDateTime(step.date) }}
                  </span>
                </div>
                <div class="step-actor" v-if="step.actor">
                  <v-chip x-small outlined>{{ step.actor }}</v-chip>
                </div>
              </div>
            </v-timeline-item>
          </v-timeline>
        </v-card-text>
      </v-card>

      <!-- Blockchain Proof Card -->
      <v-card class="info-card blockchain-card" elevation="2">
        <v-card-title class="card-header">
          <v-icon left color="purple">mdi-cube-outline</v-icon>
          Blockchain Verification
        </v-card-title>
        <v-card-text>
          <div class="blockchain-info">
            <div class="network-badge">
              <v-icon color="purple" size="20">mdi-alpha-h-circle</v-icon>
              <span>{{ verificationData.blockchain.network }}</span>
            </div>

            <div class="transaction-info" v-if="verificationData.blockchain.transactionId">
              <div class="info-item">
                <span class="label">Transaction ID</span>
                <span class="value transaction-id">{{ verificationData.blockchain.transactionId }}</span>
              </div>
              <div class="info-item" v-if="verificationData.blockchain.consensusTimestamp">
                <span class="label">Consensus Timestamp</span>
                <span class="value">{{ formatTimestamp(verificationData.blockchain.consensusTimestamp) }}</span>
              </div>
            </div>

            <v-btn
              v-if="verificationData.blockchain.explorerUrl"
              color="purple"
              dark
              block
              class="verify-button"
              :href="verificationData.blockchain.explorerUrl"
              target="_blank"
            >
              <v-icon left>mdi-open-in-new</v-icon>
              View on Hedera Explorer
            </v-btn>

            <div v-else class="no-blockchain">
              <v-icon color="grey">mdi-clock-outline</v-icon>
              <span>Blockchain recording pending</span>
            </div>
          </div>
        </v-card-text>
      </v-card>

      <!-- Batch Code & Verified At -->
      <div class="verification-footer">
        <div class="batch-code">
          <span class="label">Batch Code:</span>
          <span class="code">{{ verificationData.batchCode }}</span>
        </div>
        <div class="verified-at">
          Verified at {{ formatDateTime(verificationData.verifiedAt) }}
        </div>
      </div>

      <!-- QR Code for Sharing -->
      <v-card class="info-card share-card" elevation="2">
        <v-card-title class="card-header">
          <v-icon left color="teal">mdi-share-variant</v-icon>
          Share This Verification
        </v-card-title>
        <v-card-text class="text-center">
          <div ref="qrcodeContainer" class="qrcode-container"></div>
          <p class="share-text">Scan to share this verification page</p>
          <v-btn text color="primary" @click="copyLink">
            <v-icon left>mdi-content-copy</v-icon>
            Copy Link
          </v-btn>
        </v-card-text>
      </v-card>
    </div>

    <!-- Footer -->
    <div class="verification-footer-bar">
      <p>Powered by <strong>AgriBackup</strong> &amp; <strong>Hedera Hashgraph</strong></p>
      <p class="footer-subtitle">EUDR Compliance Made Simple</p>
    </div>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="3000">
      {{ snackbarText }}
    </v-snackbar>
  </div>
</template>

<script>
import axios from 'axios';
import QRCode from 'qrcodejs2';

export default {
  name: 'ConsumerVerification',

  data() {
    return {
      loading: true,
      error: null,
      verificationData: null,
      snackbar: false,
      snackbarText: '',
      snackbarColor: 'success',
      qrcode: null,
    };
  },

  computed: {
    batchCode() {
      return this.$route.params.batchCode;
    },
  },

  async mounted() {
    await this.verifyProduct();
  },

  methods: {
    async verifyProduct() {
      this.loading = true;
      this.error = null;

      try {
        const response = await axios.get(`/api/public/verify/${this.batchCode}`);

        if (response.data.success) {
          this.verificationData = response.data.data;
          this.$nextTick(() => {
            this.generateQRCode();
          });
        } else {
          this.error = response.data.message || 'Verification failed';
        }
      } catch (err) {
        // console.error('Verification error:', err);
        this.error = 'Unable to verify product. Please check your connection and try again.';
      } finally {
        this.loading = false;
      }
    },

    retryVerification() {
      this.verifyProduct();
    },

    generateQRCode() {
      if (this.$refs.qrcodeContainer && !this.qrcode) {
        this.$refs.qrcodeContainer.innerHTML = '';
        this.qrcode = new QRCode(this.$refs.qrcodeContainer, {
          text: window.location.href,
          width: 150,
          height: 150,
          colorDark: '#1B5E20',
          colorLight: '#ffffff',
        });
      }
    },

    copyLink() {
      navigator.clipboard.writeText(window.location.href).then(() => {
        this.snackbarText = 'Link copied to clipboard!';
        this.snackbarColor = 'success';
        this.snackbar = true;
      }).catch(() => {
        this.snackbarText = 'Failed to copy link';
        this.snackbarColor = 'error';
        this.snackbar = true;
      });
    },

    getStepColor(step) {
      const colors = ['green', 'light-green', 'teal', 'cyan', 'blue', 'indigo', 'purple'];
      return colors[(step - 1) % colors.length];
    },

    formatDate(dateStr) {
      if (!dateStr) return 'N/A';
      return new Date(dateStr).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
      });
    },

    formatDateTime(dateStr) {
      if (!dateStr) return 'N/A';
      return new Date(dateStr).toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    },

    formatTimestamp(timestamp) {
      if (!timestamp) return 'N/A';
      return new Date(timestamp).toLocaleString();
    },
  },
};
</script>

<style scoped>
.consumer-verification {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  padding-bottom: 80px;
}

/* Header */
.verification-header {
  background: linear-gradient(135deg, #1B5E20 0%, #2E7D32 100%);
  color: white;
  padding: 16px 20px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.header-content {
  max-width: 600px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
}

.tagline {
  font-size: 12px;
  opacity: 0.9;
}

/* Loading */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 20px;
}

.loading-text {
  color: #666;
  font-size: 16px;
}

/* Error */
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  gap: 16px;
  padding: 20px;
  text-align: center;
}

.error-container h2 {
  color: #d32f2f;
  margin: 0;
}

.error-container p {
  color: #666;
  margin: 0;
}

/* Content */
.verification-content {
  max-width: 600px;
  margin: 0 auto;
  padding: 16px;
}

/* Verification Badge */
.verification-badge {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 16px;
  color: white;
}

.verification-badge.compliant {
  background: linear-gradient(135deg, #2E7D32 0%, #43A047 100%);
}

.verification-badge.non-compliant {
  background: linear-gradient(135deg, #c62828 0%, #e53935 100%);
}

.badge-text h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.badge-text p {
  margin: 4px 0 0;
  opacity: 0.9;
  font-size: 14px;
}

/* Info Cards */
.info-card {
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  background: #f5f5f5;
  font-size: 16px !important;
  padding: 12px 16px !important;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-item .label {
  font-size: 12px;
  color: #666;
  text-transform: uppercase;
}

.info-item .value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

/* Stats Row */
.stats-row {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1B5E20;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

/* Journey Timeline */
.journey-timeline {
  padding: 0;
}

.timeline-content {
  padding-bottom: 12px;
}

.step-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.step-number {
  background: #e8f5e9;
  color: #1B5E20;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.step-event {
  font-weight: 600;
  color: #333;
}

.step-details {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 4px;
  font-size: 12px;
  color: #666;
}

.step-actor {
  margin-top: 4px;
}

/* Blockchain Card */
.network-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f3e5f5;
  border-radius: 20px;
  width: fit-content;
  margin-bottom: 16px;
  font-size: 14px;
  color: #7b1fa2;
}

.transaction-id {
  word-break: break-all;
  font-family: monospace;
  font-size: 14px !important;
}

.verify-button {
  margin-top: 16px;
}

.no-blockchain {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #999;
  font-size: 14px;
}

/* Footer Info */
.verification-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  font-size: 12px;
  color: #666;
}

.batch-code {
  display: flex;
  gap: 4px;
}

.batch-code .code {
  font-family: monospace;
  font-weight: 600;
}

/* Share Card */
.qrcode-container {
  display: inline-block;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
}

.share-text {
  margin-top: 12px;
  color: #666;
  font-size: 14px;
}

/* Footer Bar */
.verification-footer-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #1B5E20;
  color: white;
  text-align: center;
  padding: 12px;
}

.verification-footer-bar p {
  margin: 0;
  font-size: 12px;
}

.footer-subtitle {
  opacity: 0.8;
  font-size: 10px !important;
}

/* Mobile Responsiveness */
@media (max-width: 480px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .stats-row {
    flex-direction: column;
    gap: 16px;
  }

  .header-content {
    flex-direction: column;
    gap: 4px;
    text-align: center;
  }

  .verification-badge {
    flex-direction: column;
    text-align: center;
  }
}
</style>
