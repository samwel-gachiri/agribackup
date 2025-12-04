/* eslint-disable */
// Service for exporter public website
import apiClient from './apiClient';

const API_BASE = '/api/exporters-service/exporter';

export default {
    /**
     * Get exporter details for public website
     * @param {string} exporterId - The exporter ID
     * @returns {Promise} Exporter data
     */
    async getExporterPublicProfile(exporterId) {
        try {
            const response = await apiClient.get(`${API_BASE}/${exporterId}/public`);
            return response.data;
        } catch (error) {
            console.error('Error fetching exporter profile:', error);
            throw error;
        }
    },

    /**
     * Get exporter's products/batches for public display
     * @param {string} exporterId - The exporter ID
     * @returns {Promise} Products/batches data
     */
    async getExporterProducts(exporterId) {
        try {
            // Use the new public endpoint for exporter batches
            const response = await apiClient.get(`/api/eudr/public/exporter/${exporterId}/batches`);
            return response.data;
        } catch (error) {
            console.error('Error fetching exporter products:', error);
            // Return empty array if not accessible
            return { success: true, data: [] };
        }
    },

    /**
     * Get exporter's certificates for public display
     * @param {string} exporterId - The exporter ID
     * @returns {Promise} Certificates data
     */
    async getExporterCertificates(exporterId) {
        try {
            // For now, certificates are included in the products endpoint
            // In the future, we might want a separate public certificates endpoint
            const productsResponse = await this.getExporterProducts(exporterId);
            const certificates = productsResponse.data.filter(product =>
                product.complianceCertificateNftId ||
                product.complianceCertificateSerialNumber
            );
            return { success: true, data: certificates };
        } catch (error) {
            console.error('Error fetching exporter certificates:', error);
            return { success: true, data: [] };
        }
    },

    /**
     * Get comprehensive exporter profile including products and certificates
     * @param {string} exporterId - The exporter ID
     * @returns {Promise} Complete exporter data
     */
    async getExporterWebsiteData(exporterId) {
        try {
            const [profile, products, certificates] = await Promise.all([
                this.getExporterPublicProfile(exporterId),
                this.getExporterProducts(exporterId),
                this.getExporterCertificates(exporterId)
            ]);

            return {
                profile: profile.data || profile,
                products: products.data || [],
                certificates: certificates.data || []
            };
        } catch (error) {
            console.error('Error fetching exporter website data:', error);
            throw error;
        }
    }
};