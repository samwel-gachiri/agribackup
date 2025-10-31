<template>
  <v-app>
    <v-main>
      <div class="minimal-bg">
        <svg class="minimal-bg-svg" viewBox="0 0 800 600" fill="none" xmlns="http://www.w3.org/2000/svg">
          <ellipse cx="400" cy="300" rx="320" ry="180" fill="#1976d2" fill-opacity="0.08"/>
          <ellipse cx="600" cy="100" rx="120" ry="60" fill="#43a047" fill-opacity="0.10"/>
          <ellipse cx="200" cy="500" rx="100" ry="40" fill="#fffde7" fill-opacity="0.13"/>
        </svg>
      </div>
      <v-container class="fill-height" fluid>
        <v-row align="center" justify="center">
          <v-col cols="12" sm="8" md="4">
            <v-card class="pa-6" elevation="10" rounded="xl">
              <v-card-title class="justify-center">
                <v-icon color="primary" size="40">mdi-lock-reset</v-icon>
              </v-card-title>
              <v-card-subtitle class="text-center text-h5 font-weight-bold mb-2">
                Forgot Password
              </v-card-subtitle>
              <v-card-text>
                <form @submit.prevent="requestReset">
                  <v-text-field
                    v-model="email"
                    label="Email or Phone"
                    prepend-inner-icon="mdi-account-circle-outline"
                    outlined
                    rounded
                    dense
                    required
                    autocomplete="off"
                  />
                  <v-btn
                    color="primary"
                    block
                    large
                    rounded
                    class="mt-4"
                    type="submit"
                  >
                    Send Reset Link
                  </v-btn>
                  <v-alert
                    v-if="message"
                    type="info"
                    class="mt-4"
                    border="start"
                    color="primary"
                    dense
                  >
                    {{ message }}
                  </v-alert>
                </form>
              </v-card-text>
              <v-card-actions class="justify-center mt-2">
                <v-btn text color="primary" @click="$router.push({ name: 'SignIn' })">
                  Back to Login
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      email: '',
      message: '',
    };
  },
  methods: {
    async requestReset() {
      try {
        const response = await axios.post('/api/auth/forgot-password', {
          input: this.email,
        });
        this.message = response.data.msg;
        if (response.data.success) {
          // Navigate to reset password with otp as query param
          this.$router.push({ name: 'ResetPassword' });
        }
      } catch (error) {
        this.message = error.response?.data || 'An error occurred';
      }
    },
  },
};
</script>

<style scoped>
/* Minimal background graphic styles */
.minimal-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}
.minimal-bg-svg {
  position: absolute;
  width: 100vw;
  height: 100vh;
  left: 0;
  top: 0;
  z-index: 0;
  display: block;
}
</style>
