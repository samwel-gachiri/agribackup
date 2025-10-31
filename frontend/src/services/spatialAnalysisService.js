/* eslint-disable */
import axios from 'axios';

const BASE_PATH = '/api/production-units';

class SpatialAnalysisService {
  async findIntersectingProductionUnits(geoJsonArea) {
    const response = await axios.post(`${BASE_PATH}/search/intersecting-area`, {
      geoJsonArea,
    });
    return response.data;
  }

  async validatePolygon(geoJsonPolygon) {
    const response = await axios.post(`${BASE_PATH}/validate-polygon`, {
      geoJsonPolygon,
    });
    return response.data;
  }

  async calculatePolygonMetrics(geoJsonPolygon) {
    const response = await axios.post(`${BASE_PATH}/calculate-metrics`, {
      geoJsonPolygon,
    });
    return response.data;
  }
}

export const spatialAnalysisService = new SpatialAnalysisService();
export default spatialAnalysisService;
