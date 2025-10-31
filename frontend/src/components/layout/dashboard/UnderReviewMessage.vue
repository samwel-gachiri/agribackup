<template>
  <v-container fluid class="under-review-message">
    <v-row justify="center">
      <v-col cols="12" md="8" lg="6">
        <v-card class="elevation-4">
          <v-card-title class="text-h5 warning white--text">
            <v-icon left color="white">mdi-clock-outline</v-icon>
            Account Under Review
          </v-card-title>
          <v-card-text class="pa-6 text-center">
            <v-icon size="80" color="warning" class="mb-4">mdi-account-clock</v-icon>
            <h2 class="text-h5 mb-4">Your Account is Being Reviewed</h2>
            <p class="text-body-1 mb-4">
              Thank you for submitting your export license ID and document. Our team is currently reviewing your information to verify your exporter status.
            </p>
            <v-alert type="info" class="mb-4 text-left">
              <strong>What happens next:</strong>
              <ul class="mb-0 mt-2">
                <li>Our verification team will review your submitted documents</li>
                <li>You will receive an email notification once the review is complete</li>
                <li>Review typically takes 1-3 business days</li>
                <li>If additional information is needed, we will contact you</li>
              </ul>
            </v-alert>
            <p class="text-body-2 text--secondary">
              You can still browse the platform, but full exporter features will be available once your account is verified.
            </p>
            <v-btn color="primary" @click="refreshStatus" class="mt-4">
              <v-icon left>mdi-refresh</v-icon>
              Check Status
            </v-btn>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
export default {
  name: 'UnderReviewMessage',
  methods: {
    refreshStatus() {
      // Refresh the page or emit an event to check status
      window.location.reload();
    },
  },
};
</script>

<style scoped>
.under-review-message {
  min-height: 100vh;
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
}
</style>
