/* eslint-disable */
import apiClient from './apiClient';

/**
 * SME Classification Service
 * Handles EUDR Article 13 SME (Small and Medium Enterprise) classification
 * for simplified due diligence eligibility
 */

const SME_THRESHOLDS = {
  MICRO: { employees: 10, turnover: 2000000, balance: 2000000 },
  SMALL: { employees: 50, turnover: 10000000, balance: 10000000 },
  MEDIUM: { employees: 250, turnover: 50000000, balance: 43000000 },
};

const SME_CATEGORY_LABELS = {
  MICRO: 'Micro Enterprise',
  SMALL: 'Small Enterprise',
  MEDIUM: 'Medium Enterprise',
  LARGE: 'Large Enterprise',
};

const smeClassificationService = {
  /**
   * Get SME classification for an exporter
   * @param {string} exporterId - The exporter ID
   * @returns {Promise} SME classification data
   */
  async getExporterSmeClassification(exporterId) {
    const response = await apiClient.get(`/exporters-service/exporter/${exporterId}/sme-classification`);
    return response.data;
  },

  /**
   * Update SME declaration for an exporter
   * @param {string} exporterId - The exporter ID
   * @param {object} declaration - SME declaration data
   * @returns {Promise} Updated SME classification
   */
  async updateExporterSmeDeclaration(exporterId, declaration) {
    const response = await apiClient.put(
      `/exporters-service/exporter/${exporterId}/sme-declaration`,
      declaration
    );
    return response.data;
  },

  /**
   * Get SME classification for an importer
   * @param {string} importerId - The importer ID
   * @returns {Promise} SME classification data
   */
  async getImporterSmeClassification(importerId) {
    const response = await apiClient.get(`/v1/importers/${importerId}/sme-classification`);
    return response.data;
  },

  /**
   * Update SME declaration for an importer
   * @param {string} importerId - The importer ID
   * @param {object} declaration - SME declaration data
   * @returns {Promise} Updated SME classification
   */
  async updateImporterSmeDeclaration(importerId, declaration) {
    const response = await apiClient.put(
      `/v1/importers/${importerId}/sme-declaration`,
      declaration
    );
    return response.data;
  },

  /**
   * Calculate SME category based on thresholds
   * @param {number} employeeCount - Number of employees
   * @param {number} annualTurnover - Annual turnover in EUR
   * @param {number} balanceSheetTotal - Balance sheet total in EUR
   * @returns {string} SME category
   */
  calculateSmeCategory(employeeCount, annualTurnover, balanceSheetTotal) {
    if (
      employeeCount < SME_THRESHOLDS.MICRO.employees &&
      (annualTurnover <= SME_THRESHOLDS.MICRO.turnover || balanceSheetTotal <= SME_THRESHOLDS.MICRO.balance)
    ) {
      return 'MICRO';
    }
    if (
      employeeCount < SME_THRESHOLDS.SMALL.employees &&
      (annualTurnover <= SME_THRESHOLDS.SMALL.turnover || balanceSheetTotal <= SME_THRESHOLDS.SMALL.balance)
    ) {
      return 'SMALL';
    }
    if (
      employeeCount < SME_THRESHOLDS.MEDIUM.employees &&
      (annualTurnover <= SME_THRESHOLDS.MEDIUM.turnover || balanceSheetTotal <= SME_THRESHOLDS.MEDIUM.balance)
    ) {
      return 'MEDIUM';
    }
    return 'LARGE';
  },

  /**
   * Get category label for display
   * @param {string} category - SME category code
   * @returns {string} Human-readable label
   */
  getCategoryLabel(category) {
    return SME_CATEGORY_LABELS[category] || category;
  },

  /**
   * Check if category qualifies for simplified due diligence
   * @param {string} category - SME category
   * @returns {boolean} True if eligible
   */
  isEligibleForSimplifiedDD(category) {
    return category && category !== 'LARGE';
  },

  /**
   * Format currency for display
   * @param {number} amount - Amount in EUR
   * @returns {string} Formatted currency string
   */
  formatCurrency(amount) {
    if (amount === null || amount === undefined) return '—';
    return new Intl.NumberFormat('en-EU', {
      style: 'currency',
      currency: 'EUR',
      maximumFractionDigits: 0,
    }).format(amount);
  },

  /**
   * Get SME thresholds for reference
   * @returns {object} Threshold values
   */
  getThresholds() {
    return SME_THRESHOLDS;
  },
};

export default smeClassificationService;
