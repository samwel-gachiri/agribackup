/* eslint-disable */ 
import axios from 'axios'

class DeforestationAlertService {
    constructor() {
        this.baseURL = '/api/v1/deforestation-alerts'
    }

    /**
     * Get deforestation alerts for a specific production unit
     */
    async getAlertsForProductionUnit(productionUnitId, startDate = null, endDate = null, severity = null) {
        try {
            const params = new URLSearchParams()
            if (startDate) params.append('startDate', startDate)
            if (endDate) params.append('endDate', endDate)
            if (severity) params.append('severity', severity)

            const response = await axios.get(
                `${this.baseURL}/production-unit/${productionUnitId}?${params.toString()}`
            )
            return response.data
        } catch (error) {
            throw error
        }
    }

    /**
     * Get alert summary for a production unit
     */
    async getAlertSummary(productionUnitId) {
        try {
            const response = await axios.get(`${this.baseURL}/production-unit/${productionUnitId}/summary`)
            return response.data
        } catch (error) {
            throw error
        }
    }

    /**
     * Search deforestation alerts with filters
     */
    async searchAlerts(searchRequest) {
        try {
            const response = await axios.post(`${this.baseURL}/search`, searchRequest)
            return response.data
        } catch (error) {
            throw error
        }
    }

    /**
     * Process alerts for a production unit
     */
    async processAlerts(productionUnitId, forceRefresh = false) {
        try {
            const response = await axios.post(`${this.baseURL}/process`, {
                productionUnitId,
                forceRefresh
            })
            return response.data
        } catch (error) {
            throw error
        }
    }

    /**
     * Check alert integrity against Hedera blockchain
     */
    async checkIntegrity(alertIds) {
        try {
            const response = await axios.post(`${this.baseURL}/integrity-check`, {
                alertIds
            })
            return response.data
        } catch (error) {
            throw error
        }
    }

    /**
     * Export alerts in various formats
     */
    async exportAlerts(exportRequest) {
        try {
            const response = await axios.post(`${this.baseURL}/export`, exportRequest)
            return response.data
        } catch (error) {
            throw error
        }
    }

    /**
     * Get alerts by farmer ID
     */
    async getAlertsByFarmer(farmerId, daysSince = 30) {
        try {
            const response = await axios.get(`${this.baseURL}/farmer/${farmerId}?daysSince=${daysSince}`)
            return response.data
        } catch (error) {
            throw error
        }
    }

    /**
     * Update alert monitoring configuration
     */
    async updateConfiguration(configuration) {
        try {
            const response = await axios.post(`${this.baseURL}/configuration`, configuration)
            return response.data
        } catch (error) {
            throw error
        }
    }

    /**
     * Get current alert monitoring configuration
     */
    async getConfiguration() {
        try {
            const response = await axios.get(`${this.baseURL}/configuration`)
            return response.data
        } catch (error) {
            throw error
        }
    }
}

export const deforestationAlertService = new DeforestationAlertService()
export default deforestationAlertService;