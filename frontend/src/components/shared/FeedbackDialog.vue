<template>
  <v-dialog v-model="dialog" max-width="500px" persistent>
    <v-card>
      <v-card-title class="headline">
        <v-icon left>mdi-lightbulb-outline</v-icon>
        Share Your Ideas with AgriBackup
      </v-card-title>

      <v-card-text>
        <p class="mb-4">Help us improve AgriBackup! Share your feature requests, report bugs, or suggest improvements.</p>

        <v-form ref="feedbackForm" v-model="valid">
          <v-text-field
            v-model="feedback.title"
            label="Title"
            :rules="[rules.required]"
            outlined
            dense
            class="mb-3"
          />

          <v-textarea
            v-model="feedback.description"
            label="Description"
            :rules="[rules.required, rules.minLength]"
            outlined
            rows="4"
            hint="Please provide details about your suggestion or the issue you're experiencing"
            persistent-hint
            class="mb-3"
          />

          <v-select
            v-model="feedback.category"
            :items="categories"
            label="Request Type"
            outlined
            dense
            class="mb-3"
          />

          <v-text-field
            v-model="feedback.email"
            label="Email (optional)"
            type="email"
            outlined
            dense
            hint="We'll use this to follow up if needed"
            persistent-hint
          />
        </v-form>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn text @click="closeDialog">
          Cancel
        </v-btn>
        <v-btn
          color="primary"
          :loading="submitting"
          :disabled="!valid"
          @click="submitFeedback"
        >
          Submit
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import axios from 'axios';

export default {
  name: 'FeedbackDialog',
  data() {
    return {
      dialog: false,
      valid: false,
      submitting: false,
      feedback: {
        title: '',
        description: '',
        category: '',
        email: '',
      },
      categories: [
        'FEATURE',
        'BUG',
        'IMPROVEMENT',
      ],
      rules: {
        required: (value) => !!value || 'This field is required.',
        minLength: (value) => (value && value.length >= 10) || 'Please provide at least 10 characters.',
      },
    };
  },
  methods: {
    openDialog() {
      this.dialog = true;
      this.resetForm();
    },

    closeDialog() {
      this.dialog = false;
      this.resetForm();
    },

    resetForm() {
      this.feedback = {
        title: '',
        description: '',
        category: '',
        email: '',
      };
      if (this.$refs.feedbackForm) {
        this.$refs.feedbackForm.resetValidation();
      }
    },

    async submitFeedback() {
      if (!this.$refs.feedbackForm.validate()) return;

      this.submitting = true;

      try {
        await this.submitFeedbackRequest(this.feedback);
        this.$toast.success('Thank you for your feedback! We appreciate your input.');
        this.closeDialog();
      } catch (error) {
        this.$toast.error('Failed to submit feedback. Please try again.');
      } finally {
        this.submitting = false;
      }
    },

    async submitFeedbackRequest(feedbackData) {
      const response = await axios.post('/api/feature-requests', {
        requestType: feedbackData.category,
        message: `${feedbackData.title}\n\n${feedbackData.description}`,
        userId: this.getCurrentUserId(),
        userSection: this.getCurrentUserSection(),
        aiGenerated: false,
      });

      return response.data;
    },

    getCurrentUserId() {
      // Get user ID from store or local storage
      const token = localStorage.getItem('token') || this.$store?.state?.auth?.token;
      if (token) {
        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          return payload.user?.id || payload.sub || 'anonymous';
        } catch (e) {
          return 'anonymous';
        }
      }
      return 'anonymous';
    },

    getCurrentUserSection() {
      // Get current user role/section
      const token = localStorage.getItem('token') || this.$store?.state?.auth?.token;
      if (token) {
        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          return payload.role || payload.user?.role || 'general';
        } catch (e) {
          return 'general';
        }
      }
      return 'general';
    },
  },
};
</script>

<style scoped>
.headline {
  background: linear-gradient(135deg, #2e7d32, #4caf50);
  color: white;
  border-radius: 16px 16px 0 0;
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.2);
}

.headline .v-icon {
  color: #bbf7d0 !important;
}

/* Update dialog styling to match home page theme */
.v-dialog .v-card {
  border-radius: 16px;
  border: 2px solid rgba(230, 225, 195, 0.4);
  box-shadow: 0 20px 60px rgba(46, 125, 50, 0.15);
  background: linear-gradient(135deg, rgba(255,255,255,0.95), rgba(247, 245, 238, 0.95));
  backdrop-filter: blur(10px);
}

.v-text-field .v-input__control .v-input__slot {
  border-radius: 12px;
  border: 2px solid rgba(230, 225, 195, 0.4);
  background: rgba(255,255,255,0.8);
}

.v-text-field .v-input__control .v-input__slot:hover {
  border-color: rgba(76, 175, 80, 0.4);
}

.v-text-field .v-input__control .v-input__slot:focus-within {
  border-color: #2e7d32;
  box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.1);
}

.v-select .v-input__control .v-input__slot {
  border-radius: 12px;
  border: 2px solid rgba(230, 225, 195, 0.4);
  background: rgba(255,255,255,0.8);
}

.v-select .v-input__control .v-input__slot:hover {
  border-color: rgba(76, 175, 80, 0.4);
}

.v-select .v-input__control .v-input__slot:focus-within {
  border-color: #2e7d32;
  box-shadow: 0 0 0 3px rgba(46, 125, 50, 0.1);
}

.v-btn {
  border-radius: 12px;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.01em;
}

.v-btn.primary {
  background: linear-gradient(135deg, #2e7d32, #4caf50) !important;
  color: white !important;
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.2);
}

.v-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(46, 125, 50, 0.3) !important;
  background: linear-gradient(135deg, #4caf50, #2e7d32) !important;
}

.v-card__text {
  color: #2e7d32;
  font-weight: 500;
}

.v-card__text p {
  color: #6b4f2c;
  line-height: 1.6;
}

.v-label {
  color: #2e7d32 !important;
  font-weight: 600;
}

.v-messages__message {
  color: #f44336 !important;
  font-weight: 500;
}
</style>
