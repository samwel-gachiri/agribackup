<template>
  <div class="contact-page">
    <AppHeader />

    <main class="main-content">
      <!-- Hero Section -->
      <section class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">Get in Touch</h1>
          <p class="hero-subtitle">
            Have questions about EUDR compliance? We're here to help you navigate specific regulations and support your journey.
          </p>
        </div>
      </section>

      <!-- Contact Content -->
      <section class="contact-section">
        <div class="container">
          <div class="contact-grid">
            <!-- Contact Form -->
            <div class="contact-form-card">
              <h2 class="section-title">Send us a message</h2>
              <form @submit.prevent="submitForm" class="contact-form">
                <div class="form-group">
                  <label for="name">Full Name</label>
                  <input
                    type="text"
                    id="name"
                    v-model="form.name"
                    placeholder="John Doe"
                    required
                  >
                </div>

                <div class="form-group">
                  <label for="email">Email Address</label>
                  <input
                    type="email"
                    id="email"
                    v-model="form.email"
                    placeholder="john@example.com"
                    required
                  >
                </div>

                <div class="form-group">
                  <label for="subject">Subject</label>
                  <select id="subject" v-model="form.subject">
                    <option value="" disabled selected>Select a topic</option>
                    <option value="general">General Inquiry</option>
                    <option value="support">Technical Support</option>
                    <option value="sales">Sales & Pricing</option>
                    <option value="eudr">EUDR Compliance</option>
                  </select>
                </div>

                <div class="form-group">
                  <label for="message">Message</label>
                  <textarea
                    id="message"
                    v-model="form.message"
                    rows="5"
                    placeholder="How can we help you?"
                    required
                  ></textarea>
                </div>

                <button type="submit" class="submit-btn" :disabled="isSubmitting">
                  <span v-if="!isSubmitting">Send Message</span>
                  <span v-else>Sending...</span>
                </button>

                <!-- Success/Error Messages -->
                <div v-if="submitStatus.message" class="status-message" :class="submitStatus.type">
                  <v-icon small :color="submitStatus.type === 'success' ? 'green' : 'red'" class="status-icon">
                    {{ submitStatus.type === 'success' ? 'mdi-check-circle' : 'mdi-alert-circle' }}
                  </v-icon>
                  <span>{{ submitStatus.message }}</span>
                </div>
              </form>
            </div>

            <!-- Contact Info -->
            <div class="contact-info-column">
              <!-- Info Card -->
              <div class="info-card">
                <h3 class="info-title">Contact Information</h3>
                <p class="info-description">
                  Reach out to us directly through any of these channels. Our team is available Mon-Fri, 9am - 5pm EAT.
                </p>

                <div class="info-list">
                  <div class="info-item">
                    <div class="info-icon">
                      <v-icon color="#2e7d32">mdi-phone</v-icon>
                    </div>
                    <div class="info-details">
                      <span class="label">Phone</span>
                      <a href="tel:+254705644805" class="value">+254 705 644 805</a>
                    </div>
                  </div>

                  <div class="info-item">
                    <div class="info-icon">
                      <v-icon color="#2e7d32">mdi-email</v-icon>
                    </div>
                    <div class="info-details">
                      <span class="label">Email</span>
                      <a href="mailto:contact@agribackup.com" class="value">contact@agribackup.com</a>
                    </div>
                  </div>

                  <div class="info-item">
                    <div class="info-icon">
                      <v-icon color="#2e7d32">mdi-map-marker</v-icon>
                    </div>
                    <div class="info-details">
                      <span class="label">Office</span>
                      <span class="value">Nairobi, Kenya</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- FAQ Card -->
              <div class="faq-card">
                <h3 class="info-title">Quick FAQ</h3>
                <div class="faq-item">
                  <h4 class="faq-question">What is the response time?</h4>
                  <p class="faq-answer">We typically respond to all inquiries within 24 hours during business days.</p>
                </div>
                <div class="faq-item">
                  <h4 class="faq-question">Need documentation?</h4>
                  <p class="faq-answer">
                    Check out our <router-link :to="{ name: 'Documentation' }">Documentation</router-link> page for detailed guides.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <AppFooter />
  </div>
</template>

<script>
import AppHeader from '@/components/layout/AppHeader.vue';
import AppFooter from '@/components/layout/AppFooter.vue';
import analyticsMixin from '@/mixins/analyticsMixin.js';
import contactService from '@/services/contactService';

export default {
  name: 'ContactView',
  components: {
    AppHeader,
    AppFooter,
  },
  mixins: [analyticsMixin],
  metaInfo: {
    title: 'Contact Us | AgriBackup - Get Help and Support',
    meta: [
      { name: 'description', content: 'Contact AgriBackup support for help with sign in, sign up, and any other issues. Our team is ready to assist you.' },
      { name: 'keywords', content: 'AgriBackup contact, support, help, customer service, phone support, email support' },
      { name: 'robots', content: 'index,follow' },
      { property: 'og:title', content: 'Contact Us | AgriBackup' },
      { property: 'og:description', content: 'Contact AgriBackup support for help with sign in, sign up, and any other issues.' },
    ],
  },
  data() {
    return {
      form: {
        name: '',
        email: '',
        subject: '',
        message: '',
      },
      isSubmitting: false,
      submitStatus: {
        type: '', // 'success' or 'error'
        message: '',
      },
    };
  },
  mounted() {
    // Scroll to top when component mounts
    window.scrollTo(0, 0);
  },
  methods: {
    async submitForm() {
      this.isSubmitting = true;
      this.submitStatus = { type: '', message: '' };

      try {
        // Send contact message via backend (backend handles all emails)
        const result = await contactService.sendContactMessage(this.form);

        if (result.success) {
          this.trackButtonClick('submit_contact_form', {
            subject: this.form.subject,
            status: 'success',
          });

          // Show success message
          this.submitStatus = {
            type: 'success',
            message: 'Thank you for your message! We will get back to you shortly. Check your email for confirmation.',
          };

          // Reset form
          this.form = {
            name: '',
            email: '',
            subject: '',
            message: '',
          };

          // Clear success message after 8 seconds
          setTimeout(() => {
            this.submitStatus = { type: '', message: '' };
          }, 8000);
        } else {
          throw new Error(result.message || 'Failed to send message');
        }
      } catch (error) {
        this.trackButtonClick('submit_contact_form', {
          subject: this.form.subject,
          status: 'error',
        });

        this.submitStatus = {
          type: 'error',
          message: error.message || 'Failed to send message. Please try again or contact us directly at contact@agribackup.com',
        };

        // Clear error message after 10 seconds
        setTimeout(() => {
          this.submitStatus = { type: '', message: '' };
        }, 10000);
      } finally {
        this.isSubmitting = false;
      }
    },
  },
};
</script>

<style scoped>
.contact-page {
  font-family: 'Inter', sans-serif;
  color: #1f2937;
  background: #f9fafb;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding-top: 80px; /* Space for fixed header */
}

/* Hero Section */
.hero-section {
  background: linear-gradient(135deg, #166534 0%, #15803d 100%);
  padding: 80px 24px;
  text-align: center;
  color: white;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
}

.hero-title {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 16px;
  letter-spacing: -1px;
}

.hero-subtitle {
  font-size: 20px;
  opacity: 0.9;
  line-height: 1.6;
}

/* Contact Section */
.contact-section {
  padding: 60px 24px;
  margin-top: -40px; /* Overlap effect */
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.contact-grid {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 40px;
}

/* Form Card */
.contact-form-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 1px solid #e5e7eb;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 30px;
  color: #111827;
}

.contact-form .form-group {
  margin-bottom: 24px;
}

.contact-form label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 8px;
}

.contact-form input,
.contact-form select,
.contact-form textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 16px;
  color: #1f2937;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: #f9fafb;
}

.contact-form input:focus,
.contact-form select:focus,
.contact-form textarea:focus {
  outline: none;
  border-color: #2e7d32;
  box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.1);
  background: white;
}

.submit-btn {
  background: #2e7d32;
  color: white;
  border: none;
  width: 100%;
  padding: 16px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: #1b5e20;
  transform: translateY(-1px);
}

.submit-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

/* Info Column */
.contact-info-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card, .faq-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.02);
  border: 1px solid #e5e7eb;
}

.info-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #111827;
}

.info-description {
  color: #6b7280;
  font-size: 15px;
  margin-bottom: 24px;
  line-height: 1.5;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f3f4f6;
}

.info-item:last-child {
  border-bottom: none;
}

.info-icon {
  width: 40px;
  height: 40px;
  background: #f0fdf4;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-details {
  display: flex;
  flex-direction: column;
}

.info-details .label {
  font-size: 12px;
  text-transform: uppercase;
  color: #6b7280;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.info-details .value {
  font-size: 16px;
  font-weight: 500;
  color: #111827;
  text-decoration: none;
}

.info-details a.value:hover {
  color: #2e7d32;
  text-decoration: underline;
}

/* FAQ Styles */
.faq-item {
  margin-bottom: 16px;
}

.faq-question {
  font-size: 15px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 4px;
}

.faq-answer {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.5;
}

.faq-answer a {
  color: #2e7d32;
  text-decoration: none;
  font-weight: 500;
}

/* Status Message Styles */
.status-message {
  margin-top: 16px;
  padding: 14px 18px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  font-weight: 500;
  animation: slideIn 0.3s ease-out;
}

.status-message.success {
  background-color: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.status-message.error {
  background-color: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.status-icon {
  flex-shrink: 0;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 900px) {
  .contact-grid {
    grid-template-columns: 1fr;
  }

  .contact-section {
    margin-top: 0;
  }
}

@media (max-width: 600px) {
  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 18px;
  }

  .contact-form-card, .info-card {
    padding: 24px;
  }
}
</style>
