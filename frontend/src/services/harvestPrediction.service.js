// Service for interacting with harvest prediction backend APIs
import axios from 'axios';

const BASE = '/api/harvest';

function unwrap(result) {
  if (result && result.success !== false && result.data !== undefined) return result.data;
  if (result && result.success === undefined && result.data === undefined) return result; // raw
  throw new Error(result?.message || 'Request failed');
}

export default {
  async predictHarvest({ farmerProduceId, plantingDate }) {
    const res = await axios.post(`${BASE}/predict`, { farmerProduceId, plantingDate });
    return unwrap(res.data);
  },
  async markHarvested(farmerProduceId, actualHarvestDate) {
    const res = await axios.post(`${BASE}/${farmerProduceId}/harvested`, { actualHarvestDate });
    return unwrap(res.data);
  },
  async listExporterPredictions(exporterId) {
    const res = await axios.get(`${BASE}/exporter/${exporterId}/predictions`);
    return unwrap(res.data);
  },
  async listFarmerPredictions(farmerId) {
    const res = await axios.get(`${BASE}/farmer/${farmerId}/stored-predictions`);
    return unwrap(res.data);
  },
  async listUpcomingForExporter(exporterId, days = 30) {
    const all = await this.listExporterPredictions(exporterId);
    const now = new Date();
    const limit = new Date(now.getTime() + days * 86400000);
    return (all || [])
      .filter((p) => {
        if (!p.predictedHarvestDate) return false;
        const d = new Date(p.predictedHarvestDate);
        return d >= now && d <= limit && p.status !== 'HARVESTED';
      })
      .sort((a, b) => new Date(a.predictedHarvestDate) - new Date(b.predictedHarvestDate));
  },
};
