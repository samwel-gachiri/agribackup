<template>
  <v-app id="admin-app">
    <v-main class="admin-bg">
      <div class="admin-login-container">
        <v-card class="admin-login-card" elevation="12">
          <v-card-title class="admin-login-header">
            <div class="admin-login-title">
              <v-icon size="48" color="primary">mdi-shield-account</v-icon>
              <h1 class="ml-3">Admin Portal</h1>
            </div>
            <p class="admin-login-subtitle">AgriConnect Administration</p>
          </v-card-title>

          <v-card-text class="admin-login-form">
            <v-form ref="loginForm" v-model="isValid" @submit.prevent="login">
              <v-text-field
                v-model="form.email"
                label="Email Address"
                type="email"
                outlined
                dense
                :rules="[required('Email'), emailFormat()]"
                prepend-inner-icon="mdi-email"
                class="mb-4"
              />

              <v-text-field
                v-model="form.password"
                label="Password"
                type="password"
                outlined
                dense
                :rules="[required('Password')]"
                prepend-inner-icon="mdi-lock"
                class="mb-6"
              />

              <v-btn
                type="submit"
                color="primary"
                block
                large
                :loading="loading"
                :disabled="!isValid"
                class="admin-login-btn"
              >
                <v-icon left>mdi-login</v-icon>
                Sign In
              </v-btn>
            </v-form>

            <v-alert
              v-if="error"
              type="error"
              class="mt-4"
              dense
              dismissible
              @input="error = null"
            >
              {{ error }}
            </v-alert>
          </v-card-text>
        </v-card>

        <div class="admin-login-footer">
          <p>&copy; 2025 AgriConnect. All rights reserved.</p>
        </div>
      </div>
    </v-main>
  </v-app>
</template>

<script>
import axios from 'axios';
import validations from '@/utils/validations.js';

export default {
  name: 'AdminLogin',
  data() {
    return {
      form: {
        email: '',
        password: '',
      },
      isValid: false,
      loading: false,
      error: null,
      ...validations,
    };
  },
  methods: {
    async login() {
      if (!this.$refs.loginForm.validate()) return;

      this.loading = true;
      this.error = null;

      try {
        const response = await axios.post('/api/auth/login', {
          emailOrPhone: this.form.email,
          password: this.form.password,
          roleType: 'ADMIN',
        });

        if (response.data.success) {
          // Store admin token and info using Vuex store like regular sign-in
          const adminData = response.data.data;

          // Set portal context for admin
          await this.$store.dispatch('auth/setPortalContext', {
            portal: 'admin',
            role: 'admin',
          });

          await this.$store.dispatch('auth/signIn', { data: adminData });

          // Redirect to admin dashboard
          this.$router.push('/admin/dashboard');
        } else {
          this.error = response.data.msg || 'Login failed';
        }
      } catch (err) {
        this.error = err.response?.data?.msg || 'Login failed. Please try again.';
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.admin-bg {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.admin-login-container {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

.admin-login-card {
  border-radius: 16px;
  overflow: hidden;
}

.admin-login-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
  padding: 32px 24px;
}

.admin-login-title {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
}

.admin-login-title h1 {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
}

.admin-login-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.admin-login-form {
  padding: 32px 24px;
}

.admin-login-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
}

.admin-login-footer {
  text-align: center;
  margin-top: 24px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
}

@media (max-width: 600px) {
  .admin-login-container {
    padding: 16px;
  }

  .admin-login-header {
    padding: 24px 16px;
  }

  .admin-login-form {
    padding: 24px 16px;
  }
}
</style>
