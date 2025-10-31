/* eslint-disable */
import apiClient from './apiClient.js';

/**
 * Service for managing exporter-aggregator connections
 */
class AggregatorConnectionService {
  /**
   * Get aggregators connected to an exporter
   */
  async getConnected(exporterId, page = 0, size = 20) {
    const response = await apiClient.get('/api/v1/aggregators/connected', {
      params: { exporterId, page, size },
    });
    return response.data;
  }

  /**
   * Get aggregators available to connect (not yet connected)
   */
  async getAvailable(exporterId, page = 0, size = 20) {
    const response = await apiClient.get('/api/v1/aggregators/available', {
      params: { exporterId, page, size },
    });
    return response.data;
  }

  /**
   * Connect exporter to an aggregator
   */
  async connect(aggregatorId, exporterId, notes = null) {
    const response = await apiClient.post(
      `/api/v1/aggregators/${aggregatorId}/connect`,
      null,
      { params: { exporterId, notes } },
    );
    return response.data;
  }

  /**
   * Disconnect exporter from an aggregator
   */
  async disconnect(aggregatorId, exporterId) {
    const response = await apiClient.delete(
      `/api/v1/aggregators/${aggregatorId}/disconnect`,
      { params: { exporterId } },
    );
    return response.data;
  }
}

export const aggregatorConnectionService = new AggregatorConnectionService();
export default aggregatorConnectionService;
