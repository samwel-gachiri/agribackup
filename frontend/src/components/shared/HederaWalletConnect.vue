<template>
  <div class="tw-relative">
    <!-- Connect Button (when not connected) -->
    <button
      v-if="!isConnected"
      type="button"
      class="tw-inline-flex tw-items-center tw-gap-2 tw-rounded-xl tw-border-2 tw-border-emerald-200 tw-bg-white tw-px-4 tw-py-2 tw-text-sm tw-font-semibold tw-text-emerald-700 tw-shadow-sm tw-transition hover:tw-bg-emerald-50 hover:tw-border-emerald-300"
      @click="toggleDropdown"
      :disabled="connecting"
    >
      <v-icon size="18">{{ connecting ? 'mdi-loading mdi-spin' : 'mdi-wallet' }}</v-icon>
      <span>{{ connecting ? 'Connecting...' : 'Connect Wallet' }}</span>
    </button>

    <!-- Connected Account Display -->
    <button
      v-else
      type="button"
      class="tw-inline-flex tw-items-center tw-gap-3 tw-rounded-xl tw-border-2 tw-border-emerald-500 tw-bg-emerald-50 tw-px-4 tw-py-2 tw-text-sm tw-font-semibold tw-text-emerald-900 tw-shadow-sm tw-transition hover:tw-bg-emerald-100"
      @click="toggleDropdown"
    >
      <div class="tw-flex tw-h-6 tw-w-6 tw-items-center tw-justify-center tw-rounded-full tw-bg-emerald-600 tw-text-white">
        <v-icon size="14">mdi-check</v-icon>
      </div>
      <div class="tw-flex tw-flex-col tw-items-start">
        <span class="tw-text-xs tw-text-emerald-600">{{ networkName }}</span>
        <span class="tw-font-mono tw-text-xs">{{ formatAddress(connectedAccount) }}</span>
      </div>
      <v-icon size="16" class="tw-text-emerald-600">{{ showDropdown ? 'mdi-chevron-up' : 'mdi-chevron-down' }}</v-icon>
    </button>

    <!-- Dropdown Menu -->
    <transition name="dropdown-fade">
      <div
        v-if="showDropdown"
        class="tw-absolute tw-right-0 tw-z-50 tw-mt-2 tw-w-80 tw-rounded-xl tw-border tw-border-slate-200 tw-bg-white tw-shadow-2xl"
      >
        <!-- Not Connected - Wallet Selection -->
        <div v-if="!isConnected" class="tw-p-4">
          <div class="tw-mb-3 tw-text-sm tw-font-semibold tw-text-slate-900">Connect to Hedera</div>
          <div class="tw-space-y-2">
            <button
              v-if="availableWallets.hashpack"
              type="button"
              class="tw-flex tw-w-full tw-items-center tw-gap-3 tw-rounded-lg tw-border tw-border-slate-200 tw-bg-white tw-p-3 tw-text-left tw-transition hover:tw-border-emerald-500 hover:tw-bg-emerald-50"
              @click="connectWallet('hashpack')"
            >
              <div class="tw-flex tw-h-10 tw-w-10 tw-items-center tw-justify-center tw-rounded-full tw-bg-slate-100">
                <v-icon size="20" color="purple">mdi-hexagon-multiple</v-icon>
              </div>
              <div class="tw-flex-1">
                <div class="tw-text-sm tw-font-semibold tw-text-slate-900">HashPack</div>
                <div class="tw-text-xs tw-text-slate-500">Hedera native wallet</div>
              </div>
            </button>

            <button
              v-if="availableWallets.blade"
              type="button"
              class="tw-flex tw-w-full tw-items-center tw-gap-3 tw-rounded-lg tw-border tw-border-slate-200 tw-bg-white tw-p-3 tw-text-left tw-transition hover:tw-border-emerald-500 hover:tw-bg-emerald-50"
              @click="connectWallet('blade')"
            >
              <div class="tw-flex tw-h-10 tw-w-10 tw-items-center tw-justify-center tw-rounded-full tw-bg-slate-100">
                <v-icon size="20" color="blue">mdi-sword</v-icon>
              </div>
              <div class="tw-flex-1">
                <div class="tw-text-sm tw-font-semibold tw-text-slate-900">Blade Wallet</div>
                <div class="tw-text-xs tw-text-slate-500">Multi-chain wallet</div>
              </div>
            </button>

            <button
              v-if="availableWallets.metamask"
              type="button"
              class="tw-flex tw-w-full tw-items-center tw-gap-3 tw-rounded-lg tw-border tw-border-slate-200 tw-bg-white tw-p-3 tw-text-left tw-transition hover:tw-border-emerald-500 hover:tw-bg-emerald-50"
              @click="connectWallet('metamask')"
            >
              <div class="tw-flex tw-h-10 tw-w-10 tw-items-center tw-justify-center tw-rounded-full tw-bg-slate-100">
                <v-icon size="20" color="orange">mdi-fox</v-icon>
              </div>
              <div class="tw-flex-1">
                <div class="tw-text-sm tw-font-semibold tw-text-slate-900">MetaMask</div>
                <div class="tw-text-xs tw-text-slate-500">Popular web3 wallet</div>
              </div>
            </button>
          </div>

          <div v-if="Object.keys(availableWallets).length === 0" class="tw-py-8 tw-text-center">
            <v-icon size="40" class="tw-mb-3 tw-text-slate-300">mdi-wallet-outline</v-icon>
            <p class="tw-text-sm tw-text-slate-600">No wallet detected</p>
            <p class="tw-mt-1 tw-text-xs tw-text-slate-400">
              Install HashPack, Blade, or MetaMask to continue
            </p>
          </div>
        </div>

        <!-- Connected - Account Info -->
        <div v-else class="tw-p-4">
          <div class="tw-mb-4 tw-flex tw-items-center tw-justify-between">
            <div class="tw-text-sm tw-font-semibold tw-text-slate-900">Connected Account</div>
            <div class="tw-inline-flex tw-items-center tw-gap-1 tw-rounded-full tw-bg-emerald-100 tw-px-2 tw-py-1 tw-text-xs tw-font-semibold tw-text-emerald-700">
              <div class="tw-h-2 tw-w-2 tw-rounded-full tw-bg-emerald-500"></div>
              {{ networkName }}
            </div>
          </div>

          <div class="tw-mb-4 tw-rounded-lg tw-border tw-border-slate-200 tw-bg-slate-50 tw-p-3">
            <div class="tw-mb-2 tw-text-xs tw-font-medium tw-text-slate-500">Account Address</div>
            <div class="tw-flex tw-items-center tw-justify-between">
              <span class="tw-font-mono tw-text-sm tw-text-slate-900">{{ formatAddress(connectedAccount, 'full') }}</span>
              <button
                type="button"
                class="tw-rounded tw-p-1 tw-text-slate-400 tw-transition hover:tw-bg-slate-200 hover:tw-text-slate-600"
                @click="copyAddress"
                title="Copy address"
              >
                <v-icon size="16">{{ copied ? 'mdi-check' : 'mdi-content-copy' }}</v-icon>
              </button>
            </div>
          </div>

          <div v-if="balance" class="tw-mb-4 tw-rounded-lg tw-border tw-border-slate-200 tw-bg-slate-50 tw-p-3">
            <div class="tw-mb-1 tw-text-xs tw-font-medium tw-text-slate-500">Balance</div>
            <div class="tw-text-lg tw-font-bold tw-text-slate-900">{{ balance }} ℏ</div>
          </div>

          <button
            type="button"
            class="tw-flex tw-w-full tw-items-center tw-justify-center tw-gap-2 tw-rounded-lg tw-bg-red-50 tw-px-4 tw-py-2 tw-text-sm tw-font-semibold tw-text-red-700 tw-transition hover:tw-bg-red-100"
            @click="disconnect"
          >
            <v-icon size="16">mdi-power</v-icon>
            Disconnect
          </button>
        </div>
      </div>
    </transition>

    <!-- Click outside to close -->
    <div
      v-if="showDropdown"
      class="tw-fixed tw-inset-0 tw-z-40"
      @click="showDropdown = false"
    ></div>
  </div>
</template>

<script>
import { hederaWalletService } from '@/services/hederaWalletService.js';

export default {
  name: 'HederaWalletConnect',
  data() {
    return {
      isConnected: false,
      connecting: false,
      connectedAccount: null,
      networkId: null,
      networkName: '',
      balance: null,
      showDropdown: false,
      copied: false,
      availableWallets: {},
    };
  },
  mounted() {
    this.checkWalletAvailability();
    this.checkExistingConnection();
    this.setupEventListeners();
  },
  beforeDestroy() {
    this.removeEventListeners();
  },
  methods: {
    checkWalletAvailability() {
      this.availableWallets = {
        hashpack: !!window.hashpack,
        blade: !!window.blade,
        metamask: !!window.ethereum,
      };
    },
    async checkExistingConnection() {
      const status = hederaWalletService.getWalletStatus();
      if (status.isConnected) {
        this.isConnected = true;
        this.connectedAccount = status.account;
        this.networkId = status.networkId;
        this.networkName = hederaWalletService.getNetworkName();
        await this.loadBalance();
      }
    },
    toggleDropdown() {
      this.showDropdown = !this.showDropdown;
    },
    async connectWallet(walletType) {
      this.connecting = true;
      try {
        const result = await hederaWalletService.quickConnect();
        this.isConnected = true;
        this.connectedAccount = result.account;
        this.networkId = result.networkId;
        this.networkName = hederaWalletService.getNetworkName();
        this.showDropdown = false;

        await this.loadBalance();

        this.$toast.success(`Connected to ${walletType} wallet`);
        this.$emit('connected', result);
      } catch (error) {
        this.$toast.error(`Failed to connect: ${error.message}`);
      } finally {
        this.connecting = false;
      }
    },
    async disconnect() {
      try {
        await hederaWalletService.disconnectWallet();
        this.isConnected = false;
        this.connectedAccount = null;
        this.networkId = null;
        this.networkName = '';
        this.balance = null;
        this.showDropdown = false;

        this.$toast.info('Wallet disconnected');
        this.$emit('disconnected');
      } catch (error) {
        this.$toast.error('Failed to disconnect wallet');
      }
    },
    async loadBalance() {
      try {
        const bal = await hederaWalletService.getBalance();
        this.balance = parseFloat(bal).toFixed(4);
      } catch (error) {
        this.balance = null;
      }
    },
    formatAddress(address, format = 'short') {
      if (!address) return '';
      if (format === 'full') return address;
      return `${address.slice(0, 6)}...${address.slice(-4)}`;
    },
    async copyAddress() {
      try {
        await navigator.clipboard.writeText(this.connectedAccount);
        this.copied = true;
        this.$toast.success('Address copied to clipboard');
        setTimeout(() => {
          this.copied = false;
        }, 2000);
      } catch (error) {
        this.$toast.error('Failed to copy address');
      }
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
      this.connectedAccount = event.detail.account;
      this.$emit('accountChanged', event.detail.account);
      this.loadBalance();
    },
    handleChainChanged(event) {
      this.networkId = event.detail.chainId;
      this.networkName = hederaWalletService.getNetworkName();
      this.$emit('chainChanged', event.detail.chainId);
      this.loadBalance();
    },
    handleDisconnected() {
      this.isConnected = false;
      this.connectedAccount = null;
      this.networkId = null;
      this.networkName = '';
      this.balance = null;
      this.$emit('disconnected');
    },
  },
};
</script>

<style scoped>
.dropdown-fade-enter-active,
.dropdown-fade-leave-active {
  transition: all 0.2s ease;
}

.dropdown-fade-enter,
.dropdown-fade-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}
</style>
