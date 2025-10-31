/* eslint-disable indent */
// Service for zones: list, details, and comments update
import axios from 'axios';

const API_BASE = '/api/admin-service/zones';

export default {
    async listZones() {
        const res = await axios.get(API_BASE);
        return res.data;
    },
    async getZone(zoneId) {
        const res = await axios.get(`${API_BASE}/${zoneId}`);
        return res.data;
    },
    async updateComments(zoneId, comments) {
        const res = await axios.put(`${API_BASE}/${zoneId}/comments`, { comments });
        return res.data;
    },
};
