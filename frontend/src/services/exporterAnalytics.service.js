/* eslint-disable indent */
// Service for exporter analytics
import axios from 'axios';

const API_BASE = '/api/exporters-service/exporter';

export default {
    async getExporterAnalytics(exporterId) {
        const res = await axios.get(`${API_BASE}/analytics`, {
            params: { exporterId },
        });
        return res.data;
    },

    async getSynchronizedDashboardData(exporterId) {
        const res = await axios.get(`${API_BASE}/${exporterId}/dashboard/synchronized`);
        return res.data;
    },

    async getRealTimeAnalytics(exporterId) {
        const res = await axios.get(`${API_BASE}/${exporterId}/analytics/real-time`);
        return res.data;
    },

    async refreshDashboardData(exporterId) {
        const res = await axios.post(`${API_BASE}/${exporterId}/dashboard/refresh`);
        return res.data;
    },

    async checkDataIntegrity(exporterId) {
        const res = await axios.get(`${API_BASE}/${exporterId}/data-integrity/check`);
        return res.data;
    },

    async fixDataIntegrityIssues(exporterId) {
        const res = await axios.post(`${API_BASE}/${exporterId}/data-integrity/fix`);
        return res.data;
    },
};
