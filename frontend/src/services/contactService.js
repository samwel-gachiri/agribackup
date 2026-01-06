import axios from 'axios';

/**
 * Service for handling contact form submissions via backend API
 */
class ContactService {
  constructor() {
    // Use backend API endpoint instead of calling Resend directly
    this.apiBaseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080/';
    this.contactEndpoint = `${this.apiBaseUrl}api/contact/send`;
  }

  /**
   * Send contact form message via backend API
   * @param {Object} contactData - Contact form data
   * @param {string} contactData.name - Sender's name
   * @param {string} contactData.email - Sender's email
   * @param {string} contactData.subject - Message subject
   * @param {string} contactData.message - Message content
   * @returns {Promise<Object>} Response from backend API
   */
  async sendContactMessage(contactData) {
    try {
      const {
        name, email, subject, message,
      } = contactData;

      // Send to backend API which will handle Resend
      const response = await axios.post(this.contactEndpoint, {
        name,
        email,
        subject: subject || 'General Inquiry',
        message,
      });

      return {
        success: true,
        data: response.data,
        message: 'Message sent successfully',
      };
    } catch (error) {
      return {
        success: false,
        error: error.response?.data?.message || error.message,
        message: error.response?.data?.message || 'Failed to send message. Please try again later.',
      };
    }
  }
}

export default new ContactService();
