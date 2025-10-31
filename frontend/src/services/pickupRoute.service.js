// Service for interacting with pickup route backend APIs
import axios from 'axios';

const API_BASE = '/api/admin-service/pickup-routes';

export default {
  async createRoute({
    scheduledDate, zoneId, exporterId, farmerIds,
  }) {
    const payload = {
      scheduledDate, zoneId, exporterId, farmerIds,
    };
    const res = await axios.post(API_BASE, payload);
    return res.data;
  },
  async getRoute(routeId) {
    const res = await axios.get(`${API_BASE}/${routeId}`);
    return res.data;
  },
  async listRoutes(date) {
    const res = await axios.get(API_BASE, { params: { date } });
    return res.data;
  },
  async updateRouteStatus(routeId, status) {
    const res = await axios.patch(`${API_BASE}/${routeId}/status`, { status });
    return res.data;
  },
  async updateStopStatus(routeId, stopId, status, notes) {
    const res = await axios.patch(`${API_BASE}/${routeId}/stops/${stopId}`, { status, notes });
    return res.data;
  },
  async suggestPickups({
    exporterId,
    start,
    end,
    zoneId,
    minConfidence,
  }) {
    const res = await axios.get(`${API_BASE}/suggestions`, {
      params: {
        exporterId,
        start,
        end,
        zoneId,
        minConfidence,
      },
    });
    return res.data;
  },
};
