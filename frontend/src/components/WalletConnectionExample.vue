<template>
  <v-card class="mx-auto" max-width="500">
    <v-card-title>
      <v-icon left>mdi-wallet</v-icon>
      Hedera Wallet Connection
    </v-card-title>

    <v-card-text>
      <div v-if="!isConnected">
        <p>Connect your wallet to interact with the Hedera network.</p>
        <p class="text-caption">
          <v-icon small>mdi-information</v-icon>
          Network: {{ defaultNetwork }} (from environment)
        </p>
      </div>

      <div v-else>
        <v-alert type="success" dense>
          <strong>Connected!</strong>
        </v-alert>

        <v-list dense>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title>Account</v-list-item-title>
              <v-list-item-subtitle class="text-wrap">
                {{ account }}
              </v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-action>
              <v-btn icon small @click="copyToClipboard(account)">
                <v-icon>mdi-content-copy</v-icon>
              </v-btn>
            </v-list-item-action>
          </v-list-item>

          <v-list-item>
            <v-list-item-content>
              <v-list-item-title>Network</v-list-item-title>
              <v-list-item-subtitle>
                {{ networkName }} (ID: {{ networkId }})
              </v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>

          <v-list-item v-if="balance">
            <v-list-item-content>
              <v-list-item-title>Balance</v-list-item-title>
              <v-list-item-subtitle>
                {{ balance }} HBAR
              </v-list-item-subtitle>
            </v-list-item-content>
          </v-list-item>
        </v-list>
      </div>
    </v-card-text>

    <v-card-actions>
      <v-btn
        v-if="!isConnected"
        color="primary"
        :loading="connecting"
        @click="connectWallet"
      >
        <v-icon left>mdi-link</v-icon>
        Connect Wallet
      </v-btn>

      <template v-else>
        <v-btn
          color="secondary"
          :loading="loadingBalance"
          @click="getBalance"
        >
          <v-icon left>mdi-refresh</v-icon>
          Refresh Balance
        </v-btn>

        <v-spacer></v-spacer>

        <v-btn
          color="error"
          text
          @click="disconnectWallet"
        >
          <v-icon left>mdi-logout</v-icon>
          Disconnect
        </v-btn>
      </template>
    </v-card-actions>

    <v-snackbar v-model="snackbar" :timeout="3000" :color="snackbarColor">
      {{ snackbarText }}
      <template v-slot:action="{ attrs }">
        <v-btn text v-bind="attrs" @click="snackbar = false">
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </v-card>
</template>

<script>
// eslint-disable-next-line import/no-named-as-default
import hederaWalletService from '@/services/hederaWalletService.js';
import { getDefaultNetwork } from '@/services/walletConnectUtil.js';

export default {
  name: 'WalletConnectionExample',

  data() {
    return {
      isConnected: false,
      account: null,
      networkId: null,
      networkName: 'Unknown',
      balance: null,
      connecting: false,
      loadingBalance: false,
      snackbar: false,
      snackbarText: '',
      snackbarColor: 'success',
      defaultNetwork: getDefaultNetwork(),
    };
  },

  async mounted() {
    // Check if already connected
    const status = hederaWalletService.getWalletStatus();
    if (status.isConnected) {
      this.updateWalletInfo(status);
    }

    // Listen for wallet events
    this.setupEventListeners();
  },

  beforeDestroy() {
    this.removeEventListeners();
  },

  methods: {
    async connectWallet() {
      this.connecting = true;

      try {
        // Use the default network from environment
        const result = await hederaWalletService.quickConnectDefault();

        this.updateWalletInfo(result);
        this.showSnackbar('Wallet connected successfully!', 'success');

        // Get balance
        await this.getBalance();
      } catch (error) {
        this.showSnackbar(`Failed to connect: ${error.message}`, 'error');
      } finally {
        this.connecting = false;
      }
    },

    async disconnectWallet() {
      try {
        await hederaWalletService.disconnectWallet();

        this.isConnected = false;
        this.account = null;
        this.networkId = null;
        this.networkName = 'Unknown';
        this.balance = null;

        this.showSnackbar('Wallet disconnected', 'info');
      } catch (error) {
        this.showSnackbar(`Failed to disconnect: ${error.message}`, 'error');
      }
    },

    async getBalance() {
      if (!this.isConnected) return;

      this.loadingBalance = true;

      try {
        this.balance = await hederaWalletService.getBalance();
      } catch (error) {
        this.showSnackbar(`Failed to get balance: ${error.message}`, 'error');
      } finally {
        this.loadingBalance = false;
      }
    },

    updateWalletInfo(info) {
      this.isConnected = info.isConnected;
      this.account = info.account;
      this.networkId = info.networkId;
      this.networkName = hederaWalletService.getNetworkName();
    },

    setupEventListeners() {
      window.addEventListener('walletAccountChanged', this.handleAccountChanged);
      window.addEventListener('walletChainChanged', this.handleChainChanged);
      window.addEventListener('walletDisconnected', this.handleDisconnected);
    },

    removeEventListeners() {
      window.removeEventListener('walletAccountChanged', this.handleAccountChanged);
      window.removeEventListener('walletChainChanged', this.handleChainChanged);
      window.removeEventListener('walletDisconnected', this.handleDisconnected);
    },

    handleAccountChanged(event) {
      this.account = event.detail.account;
      if (this.account) {
        this.showSnackbar('Account changed', 'info');
        this.getBalance();
      } else {
        this.disconnectWallet();
      }
    },

    handleChainChanged(event) {
      this.networkId = event.detail.chainId;
      this.networkName = hederaWalletService.getNetworkName();
      this.showSnackbar('Network changed', 'info');
    },

    handleDisconnected() {
      this.disconnectWallet();
    },

    async copyToClipboard(text) {
      try {
        await navigator.clipboard.writeText(text);
        this.showSnackbar('Copied to clipboard!', 'success');
      } catch (error) {
        this.showSnackbar('Failed to copy', 'error');
      }
    },

    showSnackbar(text, color = 'success') {
      this.snackbarText = text;
      this.snackbarColor = color;
      this.snackbar = true;
    },
  },
};
</script>

<style scoped>
.text-wrap {
  word-break: break-all;
  white-space: normal;
}
</style>
