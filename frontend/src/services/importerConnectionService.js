/* eslint-disable */
import apiClient from './apiClient.js';

/**
 * Service for managing exporter-importer connections
 */
class ImporterConnectionService {
  /**
   * Get importers connected to an exporter
   */
  async getConnected(exporterId, page = 0, size = 20) {
    const response = await apiClient.get('/api/v1/importers/connected', {
      params: { exporterId, page, size },
    });
    return response.data;
  }

  /**
   * Get importers available to connect (not yet connected)
   */
  async getAvailable(exporterId, page = 0, size = 20) {
    const response = await apiClient.get('/api/v1/importers/available', {
      params: { exporterId, page, size },
    });
    return response.data;
  }

  /**
   * Connect exporter to an importer
   */
  async connect(importerId, exporterId, notes = null) {
    const response = await apiClient.post(
      `/api/v1/importers/${importerId}/connect`,
      null,
      { params: { exporterId, notes } },
    );
    return response.data;
  }

  /**
   * Disconnect exporter from an importer
   */
  async disconnect(importerId, exporterId) {
    const response = await apiClient.delete(
      `/api/v1/importers/${importerId}/disconnect`,
      { params: { exporterId } },
    );
    return response.data;
  }
}

export const importerConnectionService = new ImporterConnectionService();
export default importerConnectionService;
