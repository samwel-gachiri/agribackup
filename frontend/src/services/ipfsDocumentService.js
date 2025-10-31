/* eslint-disable */ 
import axios from 'axios'

class IpfsDocumentService {
  constructor() {
    this.baseURL = '/api/v1/documents'
  }

  /**
   * Upload document to IPFS
   */
  async uploadDocument(file, documentType, ownerEntityId, ownerEntityType, uploaderId, metadata = {}) {
    try {
      const formData = new FormData()
      formData.append('file', file)
      formData.append('documentType', documentType)
      formData.append('ownerEntityId', ownerEntityId)
      formData.append('ownerEntityType', ownerEntityType)
      formData.append('uploaderId', uploaderId)

      if (Object.keys(metadata).length > 0) {
        formData.append('metadata', JSON.stringify(metadata))
      }

      const response = await axios.post(`${this.baseURL}/upload`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          // Emit progress event for UI updates
          window.dispatchEvent(new CustomEvent('documentUploadProgress', {
            detail: { percentCompleted, file: file.name }
          }))
        }
      })

      return response.data
    } catch (error) {
      throw error
    }
  }

  /**
   * Get document details
   */
  async getDocument(documentId) {
    try {
      const response = await axios.get(`${this.baseURL}/${documentId}`)
      return response.data
    } catch (error) {
      throw error
    }
  }

  /**
   * Download document from IPFS
   */
  async downloadDocument(documentId) {
    try {
      const response = await axios.get(`${this.baseURL}/${documentId}/download`, {
        responseType: 'blob'
      })

      // Create download link
      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement('a')
      link.href = url

      // Get filename from response headers
      const contentDisposition = response.headers['content-disposition']
      let filename = 'document'
      if (contentDisposition) {
        const filenameMatch = contentDisposition.match(/filename="(.+)"/)
        if (filenameMatch) {
          filename = filenameMatch[1]
        }
      }

      link.setAttribute('download', filename)
      document.body.appendChild(link)
      link.click()
      link.remove()
      window.URL.revokeObjectURL(url)

      return { success: true, filename }
    } catch (error) {
      throw error
    }
  }

  /**
   * Get document access URL
   */
  async getDocumentAccessUrl(documentId, expiryMinutes = 60, accessType = 'VIEW') {
    try {
      const response = await axios.post(`${this.baseURL}/${documentId}/access`, {
        documentId,
        expiryMinutes,
        accessType
      })
      return response.data
    } catch (error) {
      throw error
    }
  }

  /**
   * Search documents
   */
  async searchDocuments(searchCriteria) {
    try {
      const response = await axios.post(`${this.baseURL}/search`, searchCriteria)
      return response.data
    } catch (error) {
      throw error
    }
  }

  /**
   * Get documents by owner
   */
  async getDocumentsByOwner(ownerEntityId, ownerEntityType) {
    try {
      const response = await axios.get(`${this.baseURL}/owner/${ownerEntityId}?ownerEntityType=${ownerEntityType}`)
      return response.data
    } catch (error) {
      throw error
    }
  }

  /**
   * Get documents by type
   */
  async getDocumentsByType(documentType) {
    try {
      const response = await axios.get(`${this.baseURL}/type/${documentType}`)
      return response.data
    } catch (error) {
      throw error
    }
  }

  /**
   * Check document integrity
   */
  async checkDocumentIntegrity(documentIds) {
    try {
      const response = await axios.post(`${this.baseURL}/integrity-check`, {
        documentIds
      })
      return response.data
    } catch (error) {
      throw error
    }
  }

  /**
   * Delete document
   */
  async deleteDocument(documentId) {
    try {
      const response = await axios.delete(`${this.baseURL}/${documentId}`)
      return response.data
    } catch (error) {
      throw error
    }
  }

  /**
   * Get IPFS statistics
   */
  async getIpfsStats() {
    try {
      const response = await axios.get(`${this.baseURL}/ipfs/stats`)
      return response.data
    } catch (error) {
      throw error
    }
  }

  /**
   * Validate file before upload
   */
  validateFile(file, maxSizeMB = 50) {
    const errors = []

    // Check file size
    const maxSizeBytes = maxSizeMB * 1024 * 1024
    if (file.size > maxSizeBytes) {
      errors.push(`File size exceeds maximum allowed size of ${maxSizeMB}MB`)
    }

    // Check file type
    const allowedTypes = [
      'application/pdf',
      'image/jpeg',
      'image/png',
      'image/tiff',
      'application/msword',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      'text/plain',
      'application/json'
    ]

    if (!allowedTypes.includes(file.type)) {
      errors.push(`File type not allowed: ${file.type}`)
    }

    // Check filename
    if (!file.name || file.name.trim() === '') {
      errors.push('File must have a valid name')
    }

    return {
      isValid: errors.length === 0,
      errors
    }
  }

  /**
   * Get document type options
   */
  getDocumentTypes() {
    return [
      { value: 'LAND_OWNERSHIP_CERTIFICATE', text: 'Land Ownership Certificate' },
      { value: 'FARMING_LICENSE', text: 'Farming License' },
      { value: 'ENVIRONMENTAL_PERMIT', text: 'Environmental Permit' },
      { value: 'ORGANIC_CERTIFICATE', text: 'Organic Certificate' },
      { value: 'QUALITY_CERTIFICATE', text: 'Quality Certificate' },
      { value: 'EXPORT_LICENSE', text: 'Export License' },
      { value: 'PHYTOSANITARY_CERTIFICATE', text: 'Phytosanitary Certificate' },
      { value: 'INVOICE', text: 'Invoice' },
      { value: 'PACKING_LIST', text: 'Packing List' },
      { value: 'BILL_OF_LADING', text: 'Bill of Lading' },
      { value: 'INSURANCE_CERTIFICATE', text: 'Insurance Certificate' },
      { value: 'THIRD_PARTY_VERIFICATION', text: 'Third Party Verification' },
      { value: 'RISK_ASSESSMENT_REPORT', text: 'Risk Assessment Report' },
      { value: 'DUE_DILIGENCE_STATEMENT', text: 'Due Diligence Statement' },
      { value: 'SUPPLY_CHAIN_MAP', text: 'Supply Chain Map' },
      { value: 'DEFORESTATION_ANALYSIS', text: 'Deforestation Analysis' },
      { value: 'SATELLITE_IMAGERY', text: 'Satellite Imagery' },
      { value: 'FIELD_INSPECTION_REPORT', text: 'Field Inspection Report' },
      { value: 'CORRECTIVE_ACTION_PLAN', text: 'Corrective Action Plan' },
      { value: 'COMPLIANCE_REPORT', text: 'Compliance Report' },
      { value: 'OTHER', text: 'Other' }
    ]
  }

  /**
   * Get file type icon
   */
  getFileTypeIcon(mimeType) {
    const iconMap = {
      'application/pdf': 'mdi-file-pdf-box',
      'image/jpeg': 'mdi-file-image',
      'image/png': 'mdi-file-image',
      'image/tiff': 'mdi-file-image',
      'application/msword': 'mdi-file-word',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'mdi-file-word',
      'text/plain': 'mdi-file-document',
      'application/json': 'mdi-code-json'
    }

    return iconMap[mimeType] || 'mdi-file'
  }

  /**
   * Format file size
   */
  formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes'

    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  /**
   * Get document status color
   */
  getDocumentStatusColor(document) {
    if (document.isHederaVerified && document.isIpfsVerified) {
      return 'success'
    } else if (document.isIpfsVerified) {
      return 'warning'
    } else {
      return 'error'
    }
  }

  /**
   * Get document status text
   */
  getDocumentStatusText(document) {
    if (document.isHederaVerified && document.isIpfsVerified) {
      return 'Fully Verified'
    } else if (document.isIpfsVerified) {
      return 'IPFS Stored'
    } else {
      return 'Not Verified'
    }
  }

  /**
   * Generate document preview URL
   */
  generatePreviewUrl(document) {
    if (!document.ipfsHash) return null

    // For images and PDFs, we can generate preview URLs
    if (document.mimeType.startsWith('image/') || document.mimeType === 'application/pdf') {
      return `https://ipfs.io/ipfs/${document.ipfsHash}`
    }

    return null
  }

  /**
   * Bulk upload documents
   */
  async bulkUploadDocuments(files, documentType, ownerEntityId, ownerEntityType, uploaderId, metadata = {}) {
    const results = []

    for (const file of files) {
      try {
        const result = await this.uploadDocument(
          file, documentType, ownerEntityId, ownerEntityType, uploaderId, metadata
        )
        results.push({ file: file.name, success: true, result })
      } catch (error) {
        results.push({ file: file.name, success: false, error: error.message })
      }
    }

    return results
  }

  /**
   * Create document sharing link
   */
  async createSharingLink(documentId, expiryHours = 24, password = null) {
    try {
      const accessResponse = await this.getDocumentAccessUrl(documentId, expiryHours * 60, 'SHARE')

      if (accessResponse.success) {
        return {
          success: true,
          shareUrl: accessResponse.accessUrl,
          expiresAt: accessResponse.expiresAt,
          password: password
        }
      } else {
        throw new Error(accessResponse.error)
      }
    } catch (error) {
      throw error
    }
  }
}

export const ipfsDocumentService = new IpfsDocumentService()
export default ipfsDocumentService;