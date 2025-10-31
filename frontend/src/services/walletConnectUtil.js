/* eslint-disable */
import { BrowserProvider } from 'ethers';

// Constants
const SEPARATOR = '\n=======================================';

/**
 * Get network from environment variables
 */
function getDefaultNetwork() {
    return process.env.VUE_APP_HEDERA_NETWORK || 'testnet';
}

/**
 * Simple wallet connection utility based on working example
 * @param {string} network - Network type: 'testnet', 'previewnet', or 'mainnet' (optional, defaults to env var)
 * @returns {Promise<Array>} [selectedAccount, provider, network]
 */
async function walletConnectFcn(network = null) {
    // Use provided network or fall back to environment variable
    const targetNetwork = network || getDefaultNetwork();


    // Check if MetaMask is available
    if (!window.ethereum) {
        throw new Error('MetaMask not found. Please install MetaMask extension.');
    }

    // ETHERS PROVIDER (v6 syntax)
    const provider = new BrowserProvider(window.ethereum);

    // SWITCH TO HEDERA NETWORK
    let chainId;
    if (targetNetwork === 'testnet') {
        chainId = '0x128';
    } else if (targetNetwork === 'previewnet') {
        chainId = '0x129';
    } else {
        chainId = '0x127';
    }

    try {
        await window.ethereum.request({
            method: 'wallet_addEthereumChain',
            params: [{
                chainName: `Hedera ${targetNetwork}`,
                chainId,
                nativeCurrency: { name: 'HBAR', symbol: 'ℏℏ', decimals: 18 },
                rpcUrls: [`https://${targetNetwork}.hashio.io/api`],
                blockExplorerUrls: [`https://hashscan.io/${targetNetwork}/`],
            }],
        });
    } catch (networkError) {
        // Continue anyway, might already be on correct network
    }

    // CONNECT TO ACCOUNT
    let selectedAccount;

    try {
        await provider.send('eth_requestAccounts', []).then((accounts) => {
            selectedAccount = accounts[0];
        }).catch((connectError) => {
            throw connectError;
        });

        if (!selectedAccount) {
            throw new Error('No accounts found in wallet');
        }


        return [selectedAccount, provider, targetNetwork];
    } catch (error) {
        throw error;
    }
}

/**
 * Quick connection function using environment variable (recommended)
 */
async function connectToHederaDefault() {
    return walletConnectFcn();
}

/**
 * Quick connection function for testnet (most common use case)
 */
async function connectToHederaTestnet() {
    return walletConnectFcn('testnet');
}

/**
 * Quick connection function for mainnet
 */
async function connectToHederaMainnet() {
    return walletConnectFcn('mainnet');
}

/**
 * Quick connection function for previewnet
 */
async function connectToHederaPreviewnet() {
    return walletConnectFcn('previewnet');
}

/**
 * Check if wallet is available
 */
function isWalletAvailable() {
    return typeof window !== 'undefined' && !!window.ethereum;
}

/**
 * Get current network info
 */
async function getCurrentNetwork() {
    if (!window.ethereum) {
        throw new Error('Wallet not available');
    }

    try {
        const chainId = await window.ethereum.request({ method: 'eth_chainId' });
        const chainIdDecimal = parseInt(chainId, 16);

        let networkName = 'Unknown';
        let networkType = 'unknown';

        switch (chainIdDecimal) {
            case 295:
                networkName = 'Hedera Mainnet';
                networkType = 'mainnet';
                break;
            case 296:
                networkName = 'Hedera Testnet';
                networkType = 'testnet';
                break;
            case 297:
                networkName = 'Hedera Previewnet';
                networkType = 'previewnet';
                break;
            default:
                networkName = 'Unknown Network';
                networkType = 'unknown';
                break;
        }

        return {
            chainId,
            chainIdDecimal,
            networkName,
            networkType,
            isHedera: [295, 296, 297].includes(chainIdDecimal),
            defaultNetwork: getDefaultNetwork(),
        };
    } catch (error) {
        throw error;
    }
}

export default walletConnectFcn;
export {
    connectToHederaDefault,
    connectToHederaTestnet,
    connectToHederaMainnet,
    connectToHederaPreviewnet,
    isWalletAvailable,
    getCurrentNetwork,
    getDefaultNetwork,
};
