/* eslint-disable */
import { ethers, BrowserProvider } from 'ethers';
import walletConnectFcn, {
  connectToHederaDefault,
  connectToHederaTestnet,
  connectToHederaMainnet,
  connectToHederaPreviewnet,
  isWalletAvailable as checkWalletAvailable,
  getCurrentNetwork,
  getDefaultNetwork
} from './walletConnectUtil';

class HederaWalletService {
  constructor() {
    this.provider = null;
    this.signer = null;
    this.account = null;
    this.isConnected = false;
    this.networkId = null;

    // Error messages
    this.WALLET_NOT_CONNECTED_ERROR = 'Wallet not connected';

    // Hedera network configurations
    this.networks = {
      testnet: {
        chainId: '0x128', // 296 in decimal
        chainName: 'Hedera Testnet',
        nativeCurrency: {
          name: 'HBAR',
          symbol: 'ℏℏ',
          decimals: 18,
        },
        rpcUrls: ['https://testnet.hashio.io/api'],
        blockExplorerUrls: ['https://hashscan.io/testnet'],
      },
      previewnet: {
        chainId: '0x129', // 297 in decimal
        chainName: 'Hedera Previewnet',
        nativeCurrency: {
          name: 'HBAR',
          symbol: 'ℏℏ',
          decimals: 18,
        },
        rpcUrls: ['https://previewnet.hashio.io/api'],
        blockExplorerUrls: ['https://hashscan.io/previewnet'],
      },
      mainnet: {
        chainId: '0x127', // 295 in decimal
        chainName: 'Hedera Mainnet',
        nativeCurrency: {
          name: 'HBAR',
          symbol: 'ℏℏ',
          decimals: 18,
        },
        rpcUrls: ['https://mainnet.hashio.io/api'],
        blockExplorerUrls: ['https://hashscan.io/mainnet'],
      },
    };
  }

  /**
   * Check if wallet extension is available
   */
  static isWalletAvailable() {
    return (
      typeof window !== 'undefined'
      && (window.ethereum || window.hashpack || window.blade)
    );
  }

  /**
   * Quick connect to Hedera Testnet (most common use case)
   */
  async quickConnectTestnet() {
    try {
      const [account, provider, network] = await connectToHederaTestnet();

      this.provider = provider;
      this.account = account;
      this.isConnected = true;
      this.networkId = 296; // Testnet chain ID

      // Set up event listeners
      this.setupEventListeners();

      return {
        account: this.account,
        networkId: this.networkId,
        isConnected: this.isConnected,
        network: network
      };
    } catch (error) {
      throw error;
    }
  }

  /**
   * Quick connect to Hedera Mainnet
   */
  async quickConnectMainnet() {
    try {
      const [account, provider, network] = await connectToHederaMainnet();

      this.provider = provider;
      this.account = account;
      this.isConnected = true;
      this.networkId = 295; // Mainnet chain ID

      // Set up event listeners
      this.setupEventListeners();

      return {
        account: this.account,
        networkId: this.networkId,
        isConnected: this.isConnected,
        network: network
      };
    } catch (error) {
      throw error;
    }
  }

  /**
   * Quick connect using the utility function
   */
  /**
   * Quick connect using environment variable (recommended)
   */
  async quickConnectDefault() {
    try {
      const [account, provider, networkType] = await connectToHederaDefault();

      this.provider = provider;
      this.account = account;
      this.isConnected = true;

      // Set network ID based on network type
      switch (networkType) {
        case 'testnet':
          this.networkId = 296;
          break;
        case 'previewnet':
          this.networkId = 297;
          break;
        case 'mainnet':
          this.networkId = 295;
          break;
        default:
          this.networkId = 296; // default to testnet
      }

      // Set up event listeners
      this.setupEventListeners();


      return {
        account: this.account,
        networkId: this.networkId,
        isConnected: this.isConnected,
        network: networkType,
        envNetwork: getDefaultNetwork()
      };
    } catch (error) {
      throw error;
    }
  }

  async quickConnect(network = null) {
    try {
      // If no network specified, use environment variable
      const targetNetwork = network || getDefaultNetwork();
      const [account, provider, networkType] = await walletConnectFcn(targetNetwork);

      this.provider = provider;
      this.account = account;
      this.isConnected = true;

      // Set network ID based on network type
      switch (networkType) {
        case 'testnet':
          this.networkId = 296;
          break;
        case 'previewnet':
          this.networkId = 297;
          break;
        case 'mainnet':
          this.networkId = 295;
          break;
        default:
          this.networkId = 296; // default to testnet
      }

      // Set up event listeners
      this.setupEventListeners();

      return {
        account: this.account,
        networkId: this.networkId,
        isConnected: this.isConnected,
        network: networkType,
        envNetwork: getDefaultNetwork()
      };
    } catch (error) {
      throw error;
    }
  }

  /**
   * Connect to wallet (HashPack, Blade, or MetaMask)
   */
  async connectWallet(walletType = 'auto', network = null) {
    try {
      // Use provided network or fall back to environment variable
      const targetNetwork = network || getDefaultNetwork();


      if (!HederaWalletService.isWalletAvailable()) {
        throw new Error(
          'No wallet extension found. Please install HashPack, Blade, or MetaMask.',
        );
      }

      let provider;
      let selectedAccount;

      switch (walletType) {
        case 'hashpack':
          provider = await HederaWalletService.connectHashPack();
          break;
        case 'blade':
          provider = await HederaWalletService.connectBlade();
          break;
        case 'metamask':
          provider = await this.connectMetaMask(targetNetwork);
          break;
        default:
          // Auto-detect available wallet
          if (window.hashpack) {
            provider = await HederaWalletService.connectHashPack();
          } else if (window.blade) {
            provider = await HederaWalletService.connectBlade();
          } else if (window.ethereum) {
            provider = await this.connectMetaMask(targetNetwork);
          } else {
            throw new Error('No compatible wallet found');
          }
      }

      this.provider = provider;

      // Get account using the provider's send method for better compatibility
      try {
        const accounts = await provider.send("eth_requestAccounts", []);
        selectedAccount = accounts[0];
        this.account = selectedAccount;
      } catch (accountError) {
        // Fallback to signer method (v6 syntax)
        this.signer = await provider.getSigner();
        this.account = await this.signer.getAddress();
        selectedAccount = this.account;
      }

      this.isConnected = true;

      // Get network information
      try {
        const networkInfo = await provider.getNetwork();
        // In ethers v6, chainId is a bigint, convert to number
        this.networkId = Number(networkInfo.chainId);
      } catch (networkError) {
        // Set default network ID based on network parameter
        this.networkId = targetNetwork === 'testnet' ? 296 : (targetNetwork === 'previewnet' ? 297 : 295);
      }

      // Set up event listeners
      this.setupEventListeners();

      const result = {
        account: this.account,
        networkId: this.networkId,
        isConnected: this.isConnected,
        network: targetNetwork,
        envNetwork: getDefaultNetwork()
      };


      return result;
    } catch (error) {
      throw error;
    }
  }

  /**
   * Connect to HashPack wallet
   */
  static async connectHashPack() {
    if (!window.hashpack) {
      throw new Error('HashPack wallet not found');
    }

    const hashpack = window.hashpack;
    await hashpack.connectToLocalWallet();

    // Create ethers provider for HashPack (v6 syntax)
    return new BrowserProvider(hashpack);
  }

  /**
   * Connect to Blade wallet
   */
  static async connectBlade() {
    if (!window.blade) {
      throw new Error('Blade wallet not found');
    }

    const blade = window.blade;
    const accounts = await blade.request({ method: 'eth_requestAccounts' });

    if (!accounts || accounts.length === 0) {
      throw new Error('No accounts found in Blade wallet');
    }

    return new BrowserProvider(blade);
  }

  /**
   * Connect to MetaMask (configured for Hedera)
   */
  async connectMetaMask(network = 'testnet') {
    if (!window.ethereum) {
      throw new Error('MetaMask not found');
    }


    // ETHERS PROVIDER (v6 syntax)
    const provider = new BrowserProvider(window.ethereum);

    // SWITCH TO HEDERA NETWORK
    let chainId;
    if (network === "testnet") {
      chainId = "0x128";
    } else if (network === "previewnet") {
      chainId = "0x129";
    } else {
      chainId = "0x127";
    }

    try {
      await window.ethereum.request({
        method: "wallet_addEthereumChain",
        params: [{
          chainName: `Hedera ${network}`,
          chainId: chainId,
          nativeCurrency: { name: "HBAR", symbol: "ℏℏ", decimals: 18 },
          rpcUrls: [`https://${network}.hashio.io/api`],
          blockExplorerUrls: [`https://hashscan.io/${network}/`],
        }],
      });
    } catch (networkError) {
      // Continue anyway, might already be on correct network
    }

    // CONNECT TO ACCOUNT
    let selectedAccount;

    try {
      await provider.send("eth_requestAccounts", []).then((accounts) => {
        selectedAccount = accounts[0];
      });

      if (!selectedAccount) {
        throw new Error('No accounts found in MetaMask');
      }

      return provider;
    } catch (connectError) {
      throw new Error(`Failed to connect to MetaMask: ${connectError.message}`);
    }
  }

  /**
   * Switch to Hedera network
   */
  async switchToHederaNetwork(network = 'testnet') {
    if (!window.ethereum) {
      throw new Error('Wallet not available');
    }


    let chainId;
    if (network === "testnet") {
      chainId = "0x128";
    } else if (network === "previewnet") {
      chainId = "0x129";
    } else if (network === "mainnet") {
      chainId = "0x127";
    } else {
      chainId = "0x128"; // default to testnet
    }

    try {
      // Always try to add the network first (this handles both adding and switching)
      await window.ethereum.request({
        method: "wallet_addEthereumChain",
        params: [{
          chainName: `Hedera ${network}`,
          chainId: chainId,
          nativeCurrency: { name: "HBAR", symbol: "ℏℏ", decimals: 18 },
          rpcUrls: [`https://${network}.hashio.io/api`],
          blockExplorerUrls: [`https://hashscan.io/${network}/`],
        }],
      });
    } catch (error) {
      // If adding fails, try switching
      try {
        await window.ethereum.request({
          method: 'wallet_switchEthereumChain',
          params: [{ chainId: chainId }],
        });
      } catch (switchError) {
        throw new Error(`Failed to switch to Hedera ${network}: ${switchError.message}`);
      }
    }
  }

  /**
   * Disconnect wallet
   */
  async disconnectWallet() {
    this.provider = null;
    this.signer = null;
    this.account = null;
    this.isConnected = false;
    this.networkId = null;

    // Remove event listeners
    this.removeEventListeners();
  }

  /**
   * Get account balance
   */
  async getBalance() {
    if (!this.isConnected || !this.signer) {
      throw new Error(this.WALLET_NOT_CONNECTED_ERROR);
    }

    const balance = await this.signer.provider.getBalance(this.account);
    return ethers.formatEther(balance);
  }

  /**
   * Sign a message
   */
  async signMessage(message) {
    if (!this.isConnected || !this.signer) {
      throw new Error(this.WALLET_NOT_CONNECTED_ERROR);
    }

    return this.signer.signMessage(message);
  }

  /**
   * Verify a signature
   */
  static verifySignature(message, signature, address) {
    try {
      const recoveredAddress = ethers.verifyMessage(message, signature);
      return recoveredAddress.toLowerCase() === address.toLowerCase();
    } catch (error) {
      return false;
    }
  }

  /**
   * Send transaction
   */
  async sendTransaction(transaction) {
    if (!this.isConnected || !this.signer) {
      throw new Error(this.WALLET_NOT_CONNECTED_ERROR);
    }

    return this.signer.sendTransaction(transaction);
  }

  /**
   * Get transaction receipt
   */
  async getTransactionReceipt(txHash) {
    if (!this.provider) {
      throw new Error('Provider not available');
    }

    return this.provider.getTransactionReceipt(txHash);
  }

  /**
   * Setup event listeners for wallet events
   */
  setupEventListeners() {
    if (window.ethereum) {
      window.ethereum.on(
        'accountsChanged',
        this.handleAccountsChanged.bind(this),
      );
      window.ethereum.on('chainChanged', this.handleChainChanged.bind(this));
      window.ethereum.on('disconnect', this.handleDisconnect.bind(this));
    }
  }

  /**
   * Remove event listeners
   */
  removeEventListeners() {
    if (window.ethereum) {
      window.ethereum.removeListener(
        'accountsChanged',
        this.handleAccountsChanged,
      );
      window.ethereum.removeListener('chainChanged', this.handleChainChanged);
      window.ethereum.removeListener('disconnect', this.handleDisconnect);
    }
  }

  /**
   * Handle account changes
   */
  handleAccountsChanged(accounts) {
    if (accounts.length === 0) {
      this.disconnectWallet();
    } else {
      this.account = accounts[0];
    }

    // Emit event for components to listen to
    window.dispatchEvent(
      new CustomEvent('walletAccountChanged', {
        detail: { account: this.account },
      }),
    );
  }

  /**
   * Handle chain changes
   */
  handleChainChanged(chainId) {
    this.networkId = parseInt(chainId, 16);

    // Emit event for components to listen to
    window.dispatchEvent(
      new CustomEvent('walletChainChanged', {
        detail: { chainId: this.networkId },
      }),
    );
  }

  /**
   * Handle disconnect
   */
  handleDisconnect() {
    this.disconnectWallet();

    // Emit event for components to listen to
    window.dispatchEvent(new CustomEvent('walletDisconnected'));
  }

  /**
   * Get current wallet status
   */
  getWalletStatus() {
    return {
      isConnected: this.isConnected,
      account: this.account,
      networkId: this.networkId,
      provider: this.provider ? 'connected' : null,
    };
  }

  /**
   * Format Hedera account ID from address
   */
  static formatHederaAccountId(address) {
    // This is a simplified conversion - in practice, you'd need proper mapping
    // between Ethereum addresses and Hedera account IDs
    return address ? `0.0.${parseInt(address.slice(-8), 16)}` : null;
  }

  /**
   * Get network name
   */
  getNetworkName() {
    switch (this.networkId) {
      case 295:
        return 'Hedera Mainnet';
      case 296:
        return 'Hedera Testnet';
      case 297:
        return 'Hedera Previewnet';
      default:
        return 'Unknown Network';
    }
  }

  /**
   * Check if on correct network
   */
  isOnHederaNetwork() {
    return this.networkId === 295 || this.networkId === 296 || this.networkId === 297;
  }

  /**
   * Get current network type
   */
  getCurrentNetworkType() {
    switch (this.networkId) {
      case 295:
        return 'mainnet';
      case 296:
        return 'testnet';
      case 297:
        return 'previewnet';
      default:
        return 'unknown';
    }
  }

  /**
   * Get detailed network information
   */
  async getNetworkInfo() {
    try {
      if (checkWalletAvailable()) {
        const networkInfo = await getCurrentNetwork();
        return {
          ...networkInfo,
          connectedAccount: this.account,
          isConnected: this.isConnected
        };
      } else {
        return {
          chainId: null,
          chainIdDecimal: null,
          networkName: 'No Wallet',
          networkType: 'none',
          isHedera: false,
          connectedAccount: null,
          isConnected: false
        };
      }
    } catch (error) {
      return {
        chainId: null,
        chainIdDecimal: null,
        networkName: 'Error',
        networkType: 'error',
        isHedera: false,
        connectedAccount: this.account,
        isConnected: this.isConnected,
        error: error.message
      };
    }
  }

  /**
   * Validate connection and network
   */
  async validateConnection() {
    try {
      if (!this.isConnected || !this.provider) {
        return { isValid: false, reason: 'Not connected to wallet' };
      }

      const networkInfo = await this.getNetworkInfo();

      if (!networkInfo.isHedera) {
        return {
          isValid: false,
          reason: `Connected to wrong network: ${networkInfo.networkName}. Please switch to a Hedera network.`,
          currentNetwork: networkInfo
        };
      }

      // Verify account is still accessible
      try {
        const accounts = await this.provider.send("eth_requestAccounts", []);
        if (!accounts.includes(this.account)) {
          return {
            isValid: false,
            reason: 'Account no longer accessible. Please reconnect.',
            currentNetwork: networkInfo
          };
        }
      } catch (accountError) {
        return {
          isValid: false,
          reason: 'Cannot access wallet accounts. Please reconnect.',
          currentNetwork: networkInfo
        };
      }

      return {
        isValid: true,
        reason: 'Connection is valid',
        currentNetwork: networkInfo
      };
    } catch (error) {
      return {
        isValid: false,
        reason: `Validation error: ${error.message}`,
        error: error
      };
    }
  }
}

export const hederaWalletService = new HederaWalletService();
export default hederaWalletService;
