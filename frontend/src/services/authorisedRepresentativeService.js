/* eslint-disable */
import apiClient from './apiClient';

/**
 * Authorised Representative Service
 * Handles EUDR Article 6 Authorised Representative management
 * Non-EU operators must designate an EU-based AR to submit DDS
 */

const EU_MEMBER_STATES = [
  { code: 'AT', name: 'Austria', flag: '🇦🇹' },
  { code: 'BE', name: 'Belgium', flag: '🇧🇪' },
  { code: 'BG', name: 'Bulgaria', flag: '🇧🇬' },
  { code: 'HR', name: 'Croatia', flag: '🇭🇷' },
  { code: 'CY', name: 'Cyprus', flag: '🇨🇾' },
  { code: 'CZ', name: 'Czech Republic', flag: '🇨🇿' },
  { code: 'DK', name: 'Denmark', flag: '🇩🇰' },
  { code: 'EE', name: 'Estonia', flag: '🇪🇪' },
  { code: 'FI', name: 'Finland', flag: '🇫🇮' },
  { code: 'FR', name: 'France', flag: '🇫🇷' },
  { code: 'DE', name: 'Germany', flag: '🇩🇪' },
  { code: 'GR', name: 'Greece', flag: '🇬🇷' },
  { code: 'HU', name: 'Hungary', flag: '🇭🇺' },
  { code: 'IE', name: 'Ireland', flag: '🇮🇪' },
  { code: 'IT', name: 'Italy', flag: '🇮🇹' },
  { code: 'LV', name: 'Latvia', flag: '🇱🇻' },
  { code: 'LT', name: 'Lithuania', flag: '🇱🇹' },
  { code: 'LU', name: 'Luxembourg', flag: '🇱🇺' },
  { code: 'MT', name: 'Malta', flag: '🇲🇹' },
  { code: 'NL', name: 'Netherlands', flag: '🇳🇱' },
  { code: 'PL', name: 'Poland', flag: '🇵🇱' },
  { code: 'PT', name: 'Portugal', flag: '🇵🇹' },
  { code: 'RO', name: 'Romania', flag: '🇷🇴' },
  { code: 'SK', name: 'Slovakia', flag: '🇸🇰' },
  { code: 'SI', name: 'Slovenia', flag: '🇸🇮' },
  { code: 'ES', name: 'Spain', flag: '🇪🇸' },
  { code: 'SE', name: 'Sweden', flag: '🇸🇪' },
];

const MANDATE_STATUS = {
  PENDING: { label: 'Pending', color: 'warning', icon: 'mdi-clock-outline' },
  ACTIVE: { label: 'Active', color: 'success', icon: 'mdi-check-circle' },
  REJECTED: { label: 'Rejected', color: 'error', icon: 'mdi-close-circle' },
  EXPIRED: { label: 'Expired', color: 'grey', icon: 'mdi-calendar-remove' },
  REVOKED: { label: 'Revoked', color: 'error', icon: 'mdi-cancel' },
};

const MANDATE_SCOPE = {
  FULL: { label: 'Full Authority', description: 'Submit DDS, manage supply chain, handle compliance' },
  DDS_ONLY: { label: 'DDS Only', description: 'Can only submit Due Diligence Statements' },
  VIEW_ONLY: { label: 'View Only', description: 'Can view data but not submit on behalf' },
};

const authorisedRepresentativeService = {
  // ==================== AR Registration & Lookup ====================

  /**
   * Register as an Authorised Representative
   * @param {object} registrationData - AR registration details
   * @returns {Promise} Registered AR data
   */
  async registerAR(registrationData) {
    const response = await apiClient.post('/auth/register/authorised-representative', registrationData);
    return response.data;
  },

  /**
   * Get AR by ID
   * @param {string} arId - AR ID
   * @returns {Promise} AR details
   */
  async getARById(arId) {
    const response = await apiClient.get(`/v1/authorised-representatives/${arId}`);
    return response.data;
  },

  /**
   * Get AR by EORI number
   * @param {string} eoriNumber - EORI number
   * @returns {Promise} AR details
   */
  async getARByEori(eoriNumber) {
    const response = await apiClient.get(`/v1/authorised-representatives/eori/${eoriNumber}`);
    return response.data;
  },

  /**
   * Get AR profile for current logged-in user
   * @returns {Promise} Current user's AR profile
   */
  async getMyARProfile() {
    const response = await apiClient.get('/v1/authorised-representatives/me');
    return response.data;
  },

  /**
   * Get available ARs that are accepting new mandates
   * @param {string} memberState - Optional: filter by EU member state
   * @returns {Promise} List of available ARs
   */
  async getAvailableARs(memberState = null) {
    const params = memberState ? { memberState } : {};
    const response = await apiClient.get('/v1/authorised-representatives/available', { params });
    return response.data;
  },

  /**
   * Search ARs by company name
   * @param {string} query - Search term
   * @returns {Promise} Matching ARs
   */
  async searchARs(query) {
    const response = await apiClient.get('/v1/authorised-representatives/search', {
      params: { query }
    });
    return response.data;
  },

  // ==================== Mandate Management ====================

  /**
   * Exporter invites an AR to represent them
   * @param {object} inviteData - { exporterId, arId, validFrom, validTo, scope, message }
   * @returns {Promise} Created mandate
   */
  async inviteAR(inviteData) {
    const response = await apiClient.post('/v1/authorised-representatives/mandates/invite', inviteData);
    return response.data;
  },

  /**
   * AR offers to represent an exporter
   * @param {object} offerData - { arId, exporterId, validFrom, validTo, scope, message }
   * @returns {Promise} Created mandate
   */
  async offerRepresentation(offerData) {
    const response = await apiClient.post('/v1/authorised-representatives/mandates/offer', offerData);
    return response.data;
  },

  /**
   * Accept a pending mandate
   * @param {string} mandateId - Mandate ID
   * @returns {Promise} Updated mandate
   */
  async acceptMandate(mandateId) {
    const response = await apiClient.patch(`/v1/authorised-representatives/mandates/${mandateId}/accept`);
    return response.data;
  },

  /**
   * Reject a pending mandate
   * @param {string} mandateId - Mandate ID
   * @param {string} reason - Optional rejection reason
   * @returns {Promise} Updated mandate
   */
  async rejectMandate(mandateId, reason = null) {
    const response = await apiClient.patch(`/v1/authorised-representatives/mandates/${mandateId}/reject`, {
      reason
    });
    return response.data;
  },

  /**
   * Revoke an active mandate
   * @param {string} mandateId - Mandate ID
   * @param {string} revokedBy - 'EXPORTER' or 'REPRESENTATIVE'
   * @param {string} reason - Optional revocation reason
   * @returns {Promise} Updated mandate
   */
  async revokeMandate(mandateId, revokedBy, reason = null) {
    const response = await apiClient.patch(`/v1/authorised-representatives/mandates/${mandateId}/revoke`, {
      revokedBy,
      reason
    });
    return response.data;
  },

  /**
   * Get active mandate for an exporter
   * @param {string} exporterId - Exporter ID
   * @returns {Promise} Active mandate or null
   */
  async getActiveMandateForExporter(exporterId) {
    try {
      const response = await apiClient.get(`/v1/authorised-representatives/mandates/exporter/${exporterId}/active`);
      return response.data;
    } catch (error) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  /**
   * Get all mandates for an exporter
   * @param {string} exporterId - Exporter ID
   * @returns {Promise} List of mandates
   */
  async getMandatesForExporter(exporterId) {
    const response = await apiClient.get(`/v1/authorised-representatives/mandates/exporter/${exporterId}`);
    return response.data;
  },

  /**
   * Get all mandates for an AR
   * @param {string} arId - AR ID
   * @returns {Promise} List of mandates
   */
  async getMandatesForAR(arId) {
    const response = await apiClient.get(`/v1/authorised-representatives/mandates/ar/${arId}`);
    return response.data;
  },

  /**
   * Get pending mandates for AR to respond to
   * @param {string} arId - AR ID
   * @returns {Promise} List of pending mandates
   */
  async getPendingMandatesForAR(arId) {
    const response = await apiClient.get(`/v1/authorised-representatives/mandates/ar/${arId}/pending`);
    return response.data;
  },

  /**
   * Upload signed mandate document
   * @param {string} mandateId - Mandate ID
   * @param {string} documentUrl - Document URL
   * @param {string} documentId - Optional document ID
   * @returns {Promise} Updated mandate
   */
  async uploadMandateDocument(mandateId, documentUrl, documentId = null) {
    const response = await apiClient.post(`/v1/authorised-representatives/mandates/${mandateId}/document`, {
      documentUrl,
      documentId
    });
    return response.data;
  },

  // ==================== Access Control ====================

  /**
   * Check if AR can access exporter data
   * @param {string} arId - AR ID
   * @param {string} exporterId - Exporter ID
   * @returns {Promise} { canAccess: boolean, scope: string }
   */
  async checkARAccess(arId, exporterId) {
    const response = await apiClient.get(`/v1/authorised-representatives/access/${arId}/exporter/${exporterId}`);
    return response.data;
  },

  /**
   * Get exporters that AR can act on behalf of
   * @param {string} arId - AR ID
   * @returns {Promise} List of exporter IDs
   */
  async getExportersForAR(arId) {
    const response = await apiClient.get(`/v1/authorised-representatives/exporters/${arId}`);
    return response.data;
  },

  // ==================== Utility Functions ====================

  /**
   * Validate EORI number format
   * @param {string} eoriNumber - EORI to validate
   * @returns {object} { valid: boolean, error: string }
   */
  validateEoriNumber(eoriNumber) {
    if (!eoriNumber) {
      return { valid: false, error: 'EORI number is required' };
    }

    const normalized = eoriNumber.toUpperCase().trim();
    const regex = /^[A-Z]{2}[A-Z0-9]{1,15}$/;

    if (!regex.test(normalized)) {
      return {
        valid: false,
        error: 'Invalid format. Must be 2-letter EU country code + up to 15 alphanumeric characters',
      };
    }

    const countryCode = normalized.substring(0, 2);
    const validCountry = EU_MEMBER_STATES.find(s => s.code === countryCode);

    if (!validCountry) {
      return {
        valid: false,
        error: `'${countryCode}' is not a valid EU member state code`,
      };
    }

    return { valid: true, countryCode, countryName: validCountry.name };
  },

  /**
   * Get human-readable mandate status
   * @param {string} status - Status code
   * @returns {object} Status info with label and color
   */
  getMandateStatusInfo(status) {
    return MANDATE_STATUS[status] || { label: status, color: 'grey', icon: 'mdi-help' };
  },

  /**
   * Get scope description
   * @param {string} scope - Scope code
   * @returns {object} Scope info with label and description
   */
  getScopeInfo(scope) {
    return MANDATE_SCOPE[scope] || { label: scope, description: '' };
  },

  /**
   * Check if exporter is from a non-EU country and needs AR
   * @param {string} countryCode - Country code
   * @returns {boolean} True if non-EU
   */
  requiresAR(countryCode) {
    return !EU_MEMBER_STATES.find(s => s.code === countryCode);
  },

  // Export constants
  EU_MEMBER_STATES,
  MANDATE_STATUS,
  MANDATE_SCOPE,
};

export default authorisedRepresentativeService;
