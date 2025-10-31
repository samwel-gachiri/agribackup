<template>
  <div>
    <h2>Forgot Password</h2>
    <form @submit.prevent="requestReset">
      <div>
        <label>Email:</label>
        <input v-model="email" type="email" required />
      </div>
      <button type="submit">Send Reset Link</button>
      <p v-if="message">{{ message }}</p>
    </form>
  </div>
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
          email: this.email,
        });
        this.message = response.data;
      } catch (error) {
        this.message = error.response?.data || 'An error occurred';
      }
    },
  },
};
</script>
