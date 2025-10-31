<template>
  <div class="hedera-transaction-viewer">
    <!-- Compact Mode - Inline Badge -->
    <div v-if="compact" class="hedera-badge-compact">
      <v-chip
        small
        color="success"
        text-color="white"
        @click="showDialog = true"
      >
        <v-icon left small>mdi-shield-check</v-icon>
        Verified on Hedera
      </v-chip>
    </div>

    <!-- Inline Mode - Full Details -->
    <div v-else-if="!dialog" class="hedera-details-inline">
      <v-card outlined class="hedera-card">
        <v-card-title class="tw-text-sm tw-py-2">
          <v-icon color="success" class="tw-mr-2">mdi-blockchain</v-icon>
          Hedera Verification
        </v-card-title>
        <v-card-text>
          <div class="hedera-info-grid">
            <div v-if="topicId" class="info-row">
              <span class="label">Topic ID:</span>
              <span class="value monospace">{{ topicId }}</span>
            </div>

            <div v-if="transactionId" class="info-row">
              <span class="label">Transaction ID:</span>
              <span class="value monospace">{{ formatTransactionId(transactionId) }}</span>
              <v-btn
                x-small
                icon
                @click="copyToClipboard(transactionId)"
                title="Copy transaction ID"
              >
                <v-icon x-small>mdi-content-copy</v-icon>
              </v-btn>
            </div>

            <div v-if="consensusTimestamp" class="info-row">
              <span class="label">Consensus Timestamp:</span>
              <span class="value">{{ formatTimestamp(consensusTimestamp) }}</span>
            </div>

            <div v-if="documentHash" class="info-row">
              <span class="label">Document Hash:</span>
              <span class="value monospace">{{ formatHash(documentHash) }}</span>
              <v-btn
                x-small
                icon
                @click="copyToClipboard(documentHash)"
                title="Copy hash"
              >
                <v-icon x-small>mdi-content-copy</v-icon>
              </v-btn>
            </div>

            <div v-if="transactionId" class="info-row">
              <v-btn
                small
                outlined
                color="primary"
                :href="getHashScanUrl(transactionId)"
                target="_blank"
                rel="noopener noreferrer"
              >
                <v-icon left small>mdi-open-in-new</v-icon>
                View on HashScan
              </v-btn>
            </div>
          </div>
        </v-card-text>
      </v-card>
    </div>

    <!-- Dialog Mode - Full Details in Modal -->
    <v-dialog v-model="showDialog" max-width="600">
      <v-card>
        <v-card-title class="tw-bg-green-600 tw-text-white">
          <v-icon color="white" class="tw-mr-2">mdi-shield-check</v-icon>
          Hedera Blockchain Verification
          <v-spacer></v-spacer>
          <v-btn icon dark @click="showDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <v-card-text class="tw-pt-4">
          <v-alert
            type="success"
            outlined
            dense
            class="tw-mb-4"
          >
            This record has been permanently verified on the Hedera public ledger with immutable consensus timestamp.
          </v-alert>

          <div class="hedera-detail-section">
            <h3 class="tw-font-semibold tw-mb-2">Network Details</h3>
            <v-simple-table dense>
              <tbody>
                <tr v-if="network">
                  <td class="tw-font-medium">Network:</td>
                  <td>
                    <v-chip x-small :color="network === 'mainnet' ? 'success' : 'warning'">
                      {{ network.toUpperCase() }}
                    </v-chip>
                  </td>
                </tr>
                <tr v-if="topicId">
                  <td class="tw-font-medium">Topic ID:</td>
                  <td class="monospace">{{ topicId }}</td>
                </tr>
                <tr v-if="transactionId">
                  <td class="tw-font-medium">Transaction ID:</td>
                  <td class="monospace tw-text-xs">
                    {{ transactionId }}
                    <v-btn
                      x-small
                      icon
                      @click="copyToClipboard(transactionId)"
                      title="Copy"
                    >
                      <v-icon x-small>mdi-content-copy</v-icon>
                    </v-btn>
                  </td>
                </tr>
                <tr v-if="consensusTimestamp">
                  <td class="tw-font-medium">Consensus Time:</td>
                  <td>{{ formatTimestamp(consensusTimestamp) }}</td>
                </tr>
              </tbody>
            </v-simple-table>
          </div>

          <div v-if="documentHash" class="hedera-detail-section tw-mt-4">
            <h3 class="tw-font-semibold tw-mb-2">Data Integrity</h3>
            <v-simple-table dense>
              <tbody>
                <tr>
                  <td class="tw-font-medium">Document Hash:</td>
                  <td class="monospace tw-text-xs">
                    {{ documentHash }}
                    <v-btn
                      x-small
                      icon
                      @click="copyToClipboard(documentHash)"
                      title="Copy"
                    >
                      <v-icon x-small>mdi-content-copy</v-icon>
                    </v-btn>
                  </td>
                </tr>
                <tr>
                  <td class="tw-font-medium">Hash Algorithm:</td>
                  <td>SHA-256</td>
                </tr>
              </tbody>
            </v-simple-table>
          </div>

          <div v-if="additionalData && Object.keys(additionalData).length > 0" class="hedera-detail-section tw-mt-4">
            <h3 class="tw-font-semibold tw-mb-2">Additional Information</h3>
            <v-simple-table dense>
              <tbody>
                <tr v-for="(value, key) in additionalData" :key="key">
                  <td class="tw-font-medium">{{ formatKey(key) }}:</td>
                  <td>{{ value }}</td>
                </tr>
              </tbody>
            </v-simple-table>
          </div>
        </v-card-text>

        <v-card-actions class="tw-justify-center tw-pb-4">
          <v-btn
            color="primary"
            :href="getHashScanUrl(transactionId)"
            target="_blank"
            rel="noopener noreferrer"
            large
          >
            <v-icon left>mdi-open-in-new</v-icon>
            View on HashScan Explorer
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar for Copy Feedback -->
    <v-snackbar
      v-model="copySnackbar"
      :timeout="2000"
      color="success"
      bottom
    >
      Copied to clipboard!
      <template v-slot:action="{ attrs }">
        <v-btn
          text
          v-bind="attrs"
          @click="copySnackbar = false"
        >
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script>
export default {
  name: 'HederaTransactionViewer',
  props: {
    // Display mode: 'compact' (just badge), 'inline' (full details), 'dialog' (open in modal)
    mode: {
      type: String,
      default: 'inline',
      validator: (value) => ['compact', 'inline', 'dialog'].includes(value),
    },
    // Hedera network: 'mainnet' or 'testnet'
    network: {
      type: String,
      default: 'testnet',
      validator: (value) => ['mainnet', 'testnet'].includes(value),
    },
    // Consensus Service Topic ID
    topicId: {
      type: String,
      default: null,
    },
    // Transaction ID from HCS submission
    transactionId: {
      type: String,
      required: true,
    },
    // Consensus timestamp (ISO 8601 format)
    consensusTimestamp: {
      type: String,
      default: null,
    },
    // SHA-256 hash of document/data
    documentHash: {
      type: String,
      default: null,
    },
    // Additional metadata to display
    additionalData: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      showDialog: false,
      copySnackbar: false,
    };
  },
  computed: {
    compact() {
      return this.mode === 'compact';
    },
    dialog() {
      return this.mode === 'dialog';
    },
  },
  methods: {
    getHashScanUrl(transactionId) {
      const baseUrl = this.network === 'mainnet'
        ? 'https://hashscan.io/mainnet'
        : 'https://hashscan.io/testnet';

      return `${baseUrl}/transaction/${transactionId}`;
    },

    formatTransactionId(txId) {
      if (!txId) return '';
      // Truncate if very long
      return txId.length > 40 ? `${txId.substring(0, 40)}...` : txId;
    },

    formatHash(hash) {
      if (!hash) return '';
      // Show first 16 and last 16 characters
      return hash.length > 40
        ? `${hash.substring(0, 16)}...${hash.substring(hash.length - 16)}`
        : hash;
    },

    formatTimestamp(timestamp) {
      if (!timestamp) return 'N/A';
      try {
        const date = new Date(timestamp);
        return date.toLocaleString('en-US', {
          year: 'numeric',
          month: 'short',
          day: 'numeric',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit',
          timeZoneName: 'short',
        });
      } catch (e) {
        return timestamp;
      }
    },

    formatKey(key) {
      // Convert camelCase to Title Case
      return key
        .replace(/([A-Z])/g, ' $1')
        .replace(/^./, (str) => str.toUpperCase())
        .trim();
    },

    copyToClipboard(text) {
      navigator.clipboard.writeText(text).then(() => {
        this.copySnackbar = true;
      }).catch(() => {
        // Handle error if needed
      });
    },
  },
};
</script>

<style scoped>
.hedera-transaction-viewer {
  width: 100%;
}

.hedera-badge-compact {
  display: inline-block;
}

.hedera-card {
  border-left: 4px solid #4caf50 !important;
}

.hedera-info-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-row .label {
  font-weight: 600;
  min-width: 140px;
  color: #666;
}

.info-row .value {
  flex: 1;
  font-size: 14px;
}

.monospace {
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  word-break: break-all;
}

.hedera-detail-section {
  margin-top: 16px;
}

.hedera-detail-section h3 {
  font-size: 16px;
  color: #333;
}
</style>
