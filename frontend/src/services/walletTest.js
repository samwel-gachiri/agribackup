/* eslint-disable */
// Simple test file to verify wallet connection
import { connectToHederaDefault, getDefaultNetwork } from './walletConnectUtil';
import hederaWalletService from './hederaWalletService';

/**
 * Test the wallet connection utility
 */
export async function testWalletConnection() {
    try {

        // Test the utility function
        const [account, provider, network] = await connectToHederaDefault();


        return { account, provider, network };
    } catch (error) {
        throw error;
    }
}

/**
 * Test the wallet service
 */
export async function testWalletService() {
    try {

        // Test the service
        const result = await hederaWalletService.quickConnectDefault();


        // Test network info
        const networkInfo = await hederaWalletService.getNetworkInfo();

        // Test validation
        const validation = await hederaWalletService.validateConnection();

        return result;
    } catch (error) {
        throw error;
    }
}

/**
 * Quick test function for console
 */
export async function quickTest() {
    try {
        const result = await hederaWalletService.quickConnect();
        return result;
    } catch (error) {
        throw error;
    }
}

// Export for console testing
if (typeof window !== 'undefined') {
    window.testWallet = {
        testConnection: testWalletConnection,
        testService: testWalletService,
        quickTest: quickTest,
        service: hederaWalletService
    };

}