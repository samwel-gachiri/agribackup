/* eslint-disable */
import apiClient from './apiClient.js';

/**
 * Service for managing exporter-processor connections
 */
class ProcessorConnectionService {
  /**
   * Get processors connected to an exporter
   */
  async getConnected(exporterId, page = 0, size = 20) {
    const response = await apiClient.get('/api/v1/processors/connected', {
      params: { exporterId, page, size },
    });
    return response.data;
  }

  /**
   * Get processors available to connect (not yet connected)
   */
  async getAvailable(exporterId, page = 0, size = 20) {
    const response = await apiClient.get('/api/v1/processors/available', {
      params: { exporterId, page, size },
    });
    return response.data;
  }

  /**
   * Connect exporter to a processor
   */
  async connect(processorId, exporterId, notes = null) {
    const response = await apiClient.post(
      `/api/v1/processors/${processorId}/connect`,
      null,
      { params: { exporterId, notes } },
    );
    return response.data;
  }

  /**
   * Disconnect exporter from a processor
   */
  async disconnect(processorId, exporterId) {
    const response = await apiClient.delete(
      `/api/v1/processors/${processorId}/disconnect`,
      { params: { exporterId } },
    );
    return response.data;
  }
}

export const processorConnectionService = new ProcessorConnectionService();
export default processorConnectionService;
