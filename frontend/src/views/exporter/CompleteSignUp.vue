<template>
  <v-container fluid class="complete-signup">
    <v-row justify="center">
      <v-col cols="12" md="8" lg="6">
        <v-card class="elevation-4">
          <v-card-title class="text-h5 primary white--text">
            Complete Your Exporter Registration
          </v-card-title>
          <v-card-text class="pa-6">
            <p class="text-body-1 mb-4">
              To fully access the exporter dashboard and features, please provide your export license ID and upload your export license or permit document.
              This is required for verification purposes.
            </p>

            <v-alert type="info" class="mb-4">
              <strong>Requirements:</strong>
              <ul class="mb-0">
                <li>Enter your export license ID (optional, but recommended)</li>
                <li>Upload your export license/permit document (required)</li>
                <li>File size must be less than 10MB</li>
                <li>Document will be securely stored and reviewed</li>
              </ul>
            </v-alert>

            <v-text-field
              v-model="licenseId"
              label="Export License ID (Optional)"
              :rules="licenseRules"
              outlined
              class="mb-4"
              placeholder="e.g. EXP123456789"
            ></v-text-field>

            <v-file-input
              v-model="selectedFile"
              label="Upload Export License/Permit Document"
              accept=".pdf,.jpg,.jpeg,.png"
              :rules="fileRules"
              show-size
              prepend-icon="mdi-file-document"
              class="mb-4"
            ></v-file-input>

            <v-btn
              color="primary"
              :disabled="!selectedFile || submitting"
              :loading="submitting"
              @click="submitLicenseAndDocument"
              block
              large
            >
              <v-icon left>mdi-upload</v-icon>
              Submit License & Document
            </v-btn>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import axios from 'axios';

export default {
  name: 'CompleteSignUp',
  data() {
    return {
      licenseId: '',
      selectedFile: null,
      submitting: false,
      rules: {
        required: (value) => !!value || 'This field is required.',
      },
      licenseRules: [
        // License ID is now optional, but if provided, it should not be empty
        (v) => !v || v.trim().length > 0 || 'License ID cannot be empty if provided',
      ],
      fileRules: [
        (v) => !v || v.size < 10000000 || 'File size should be less than 10 MB!',
        (v) => !v || ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png'].includes(v.type) || 'Only PDF, JPG, or PNG files are allowed!',
      ],
    };
  },
  methods: {
    async submitLicenseAndDocument() {
      if (!this.selectedFile) return;

      this.submitting = true;
      const formData = new FormData();
      if (this.licenseId && this.licenseId.trim()) {
        formData.append('licenseId', this.licenseId.trim());
      }
      formData.append('document', this.selectedFile);

      try {
        const response = await axios.post('/api/exporters-service/exporter/submit-license-document', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        });

        if (response.data.success) {
          this.$toast.success('License and document submitted successfully! Your account is now under review.');
          // Update user verification status
          const updatedUser = { ...this.$store.state.auth.user, verificationStatus: 'under_review' };
          this.$store.commit('auth/setUserAuthenticated', {
            token: this.$store.state.auth.token,
            roleSpecificData: updatedUser,
            role: this.$store.state.auth.role,
          });
          // Optionally reload or emit event
          this.$emit('completed');
        } else {
          this.$toast.error(response.data.msg || 'Submission failed');
        }
      } catch (error) {
        this.$toast.error(`Error submitting license and document: ${error.message}`);
      } finally {
        this.submitting = false;
      }
    },
  },
};
</script>

<style scoped>
.complete-signup {
  min-height: 100vh;
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
</style>
