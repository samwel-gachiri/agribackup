<template>
  <v-app>
    <v-main class="professional-signin">
      <v-container fluid class="fill-height tw-pa-0">
        <v-row no-gutters class="fill-height">
          <!-- Left Panel - Branding & Info -->
          <v-col
            cols="12"
            md="6"
            class="branding-panel d-none d-md-flex"
          >
            <div class="branding-content">
              <!-- Logo & Company Name -->
              <div class="brand-header">
                <v-avatar size="64" class="mb-4">
                  <v-img src="@/assets/images/happy-farmer.jpg" alt="AgriBackup" />
                </v-avatar>
                <h1 class="company-name">AgriBackup</h1>
                <p class="company-tagline">
                  EUDR-Compliant Supply Chain Platform
                </p>
              </div>

              <!-- Features List -->
              <div class="features-list">
                <div class="feature-item">
                  <v-icon color="white" size="24">mdi-shield-check</v-icon>
                  <div>
                    <h3>Secure & Compliant</h3>
                    <p>Enterprise-grade security with EUDR compliance</p>
                  </div>
                </div>
                <div class="feature-item">
                  <v-icon color="white" size="24">mdi-chart-line</v-icon>
                  <div>
                    <h3>Real-time Tracking</h3>
                    <p>Monitor your supply chain from farm to export</p>
                  </div>
                </div>
                <div class="feature-item">
                  <v-icon color="white" size="24">mdi-account-group</v-icon>
                  <div>
                    <h3>Trusted by Thousands</h3>
                    <p>Join farmers, buyers, and exporters worldwide</p>
                  </div>
                </div>
              </div>

              <!-- Footer Info -->
              <div class="brand-footer">
                <p>&copy; 2024 AgriBackup. All rights reserved.</p>
              </div>
            </div>
          </v-col>

          <!-- Right Panel - Sign In Form -->
          <v-col
            cols="12"
            md="6"
            class="form-panel"
          >
            <div class="form-container">
              <!-- Mobile Logo (shown only on mobile) -->
              <div class="mobile-logo d-md-none text-center mb-8">
                <v-avatar size="56" class="mb-3">
                  <v-img src="@/assets/images/happy-farmer.jpg" alt="AgriBackup" />
                </v-avatar>
                <h2 class="text-h5 font-weight-bold" style="color: #2e7d32;">
                  AgriBackup
                </h2>
              </div>

              <!-- Form Header -->
              <div class="form-header">
                <h2 class="form-title">Sign in to your account</h2>
                <p class="form-subtitle">
                  Welcome back! Please enter your details.
                </p>

                <!-- Portal Badge -->
                <div v-if="portalContext" class="mt-4">
                  <v-chip
                    :color="getPortalColor()"
                    text-color="white"
                    small
                    class="portal-badge"
                  >
                    <v-icon left x-small>{{ getPortalIcon() }}</v-icon>
                    {{ getPortalDisplayName() }}
                  </v-chip>
                </div>
              </div>

              <!-- Google Sign In -->
              <div class="mb-6">
                <GoogleSignIn @google-login-success="handleGoogleLogin" />
              </div>

              <!-- Divider -->
              <div class="divider-section mb-6">
                <v-divider></v-divider>
                <span class="divider-text">OR</span>
                <v-divider></v-divider>
              </div>

              <!-- Input Type Selector -->
              <div class="input-type-selector mb-4">
                <v-btn
                  :outlined="inputType !== 'phone'"
                  :color="inputType === 'phone' ? 'primary' : 'grey'"
                  :class="{ 'active-input-type': inputType === 'phone' }"
                  class="flex-grow-1"
                  @click="inputType = 'phone'"
                >
                  <v-icon left small>mdi-phone</v-icon>
                  Phone
                </v-btn>
                <v-btn
                  :outlined="inputType !== 'email'"
                  :color="inputType === 'email' ? 'primary' : 'grey'"
                  :class="{ 'active-input-type': inputType === 'email' }"
                  class="flex-grow-1 ml-2"
                  @click="inputType = 'email'"
                >
                  <v-icon left small>mdi-email</v-icon>
                  Email
                </v-btn>
              </div>

              <!-- Email Input -->
              <div v-if="inputType === 'email'" class="mb-4">
                <label class="input-label">Email address</label>
                <v-text-field
                  v-model="emailOrPhone"
                  type="email"
                  placeholder="Enter your email"
                  outlined
                  dense
                  hide-details="auto"
                  autocomplete="email"
                  name="email"
                  class="professional-input"
                />
              </div>

              <!-- Phone Input -->
              <div v-else class="mb-4">
                <label class="input-label">Phone number</label>
                <vue-phone-number-input
                  v-model="phoneNumber"
                  :default-country-code="'KE'"
                  :preferred-countries="['KE', 'US', 'GB']"
                  class="w-100 professional-phone"
                  show-code-on-list
                  @update="onPhoneUpdate"
                  autocomplete="tel"
                  name="phone"
                />
              </div>

              <!-- Password Input -->
              <div class="mb-2">
                <label class="input-label">Password</label>
                <v-text-field
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="Enter your password"
                  :append-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                  outlined
                  dense
                  hide-details="auto"
                  autocomplete="current-password"
                  name="password"
                  class="professional-input"
                  @keydown.enter="signIn"
                  @click:append="showPassword = !showPassword"
                />
              </div>

              <!-- Forgot Password -->
              <div class="text-right mb-4">
                <v-btn
                  text
                  x-small
                  color="primary"
                  class="forgot-password-btn"
                  @click="$router.push({ name: 'ForgotPassword' })"
                >
                  Forgot password?
                </v-btn>
              </div>

              <!-- Sign In Button -->
              <v-btn
                color="primary"
                class="signin-btn mb-4"
                block
                large
                depressed
                :loading="isLoading"
                @click="signIn"
              >
                Sign in
              </v-btn>

              <!-- Error Alert -->
              <v-alert
                v-if="errorMessage"
                type="error"
                dense
                text
                class="mb-4 professional-alert"
              >
                {{ errorMessage }}
                <div v-if="showCreateAccountBtn" class="mt-3">
                  <v-btn
                    color="success"
                    small
                    depressed
                    @click="goToSignUp"
                  >
                    <v-icon left small>mdi-account-plus</v-icon>
                    Create Account
                  </v-btn>
                </div>
                <div v-else-if="showCreateRoleBtn" class="mt-3">
                  <v-btn
                    color="warning"
                    small
                    depressed
                    @click="goToSignUp"
                  >
                    <v-icon left small>mdi-account-cog</v-icon>
                    Create {{ roleName }} Role
                  </v-btn>
                </div>
              </v-alert>

              <!-- Success Alert -->
              <v-alert
                v-if="successMessage"
                type="success"
                dense
                text
                class="mb-4 professional-alert"
              >
                {{ successMessage }}
              </v-alert>

              <!-- Sign Up Link -->
              <div class="signup-section text-center">
                <p class="signup-text">
                  Don't have an account?
                  <v-btn
                    text
                    color="primary"
                    class="signup-link"
                    @click="$router.push({ name: 'SignUp' })"
                  >
                    Sign up
                  </v-btn>
                </p>
              </div>

              <!-- Support -->
              <div class="support-section text-center mt-6">
                <p class="support-text">Need help?</p>
                <v-btn
                  text
                  x-small
                  color="grey darken-1"
                  href="tel:+254705644805"
                >
                  <v-icon left x-small>mdi-phone</v-icon>
                  +254 705 644 805
                </v-btn>
              </div>
            </div>
          </v-col>
        </v-row>
      </v-container>
    </v-main>

    <!-- Role Selection Dialog -->
    <RoleSelectionDialog ref="roleSelectionDialog" />
  </v-app>
</template>

<script>
import axios from 'axios';
import VuePhoneNumberInput from 'vue-phone-number-input';
import 'vue-phone-number-input/dist/vue-phone-number-input.css';
import { getCurrentUserRole } from '@/utils/roles.js';
import GoogleSignIn from '@/components/auth/GoogleSignIn.vue';
import RoleSelectionDialog from '@/components/auth/RoleSelectionDialog.vue';
import AnalyticsMixin from '@/mixins/analyticsMixin.js';

export default {
  name: 'SignInView',
  metaInfo: {
    title: 'Sign In | AgriBackup - Secure Access for All Supply Chain Actors',
    meta: [
      { name: 'description', content: 'Sign in to AgriBackup to securely access your account as a farmer, buyer, exporter, aggregator, processor, or importer. Connect directly, eliminate middlemen, and manage your agricultural business globally.' },
      { name: 'keywords', content: 'AgriBackup sign in, login, farmer portal, buyer portal, exporter portal, aggregator portal, processor portal, importer portal, agriculture, secure access, global marketplace, supply chain, direct selling, eliminate middlemen' },
      { name: 'robots', content: 'index,follow' },
      // eslint-disable-next-line sonarjs/no-duplicate-string
      { property: 'og:title', content: 'Sign In | AgriBackup' },
      { property: 'og:description', content: 'Sign in to AgriBackup for secure access to your account. Connect and manage your agricultural business across the entire supply chain.' },
      { property: 'og:type', content: 'website' },
      // eslint-disable-next-line sonarjs/no-duplicate-string
      { property: 'og:url', content: 'https://agribackup.com/signin' },
      // eslint-disable-next-line sonarjs/no-duplicate-string
      { property: 'og:image', content: 'https://agribackup.com/assets/images/happy-farmer.jpg' },
      { name: 'twitter:card', content: 'summary_large_image' },
      { name: 'twitter:title', content: 'Sign In | AgriBackup' },
      { name: 'twitter:description', content: 'Sign in to AgriBackup for secure access to your account across the agricultural supply chain.' },
      { name: 'twitter:image', content: 'https://agribackup.com/assets/images/happy-farmer.jpg' },
      { rel: 'canonical', href: 'https://agribackup.com/signin' },
    ],
    script: [
      {
        type: 'application/ld+json',
        json: {
          '@context': 'https://schema.org',
          '@type': 'WebPage',
          name: 'Sign In | AgriBackup',
          url: 'https://agribackup.com/signin',
          description: 'Sign in to AgriBackup to securely access your account as a farmer, buyer, exporter, aggregator, processor, or importer. Connect directly, eliminate middlemen, and manage your agricultural business globally.',
          image: 'https://agribackup.com/assets/images/happy-farmer.jpg',
        },
      },
    ],
  },
  components: {
    VuePhoneNumberInput,
    GoogleSignIn,
    // eslint-disable-next-line vue/no-unused-components
    RoleSelectionDialog,
  },
  mixins: [AnalyticsMixin],
  data() {
    return {
      inputType: 'phone',
      emailOrPhone: '',
      phoneNumber: '',
      fullPhoneNumber: '',
      password: '',
      showPassword: false,
      errorMessage: '',
      successMessage: '',
      showCreateAccountBtn: false,
      showCreateRoleBtn: false,
      showRetryBtn: false,
      roleName: '',
      isLoading: false,
      retryCount: 0,
      maxRetries: 3,
    };
  },
  computed: {
    isExporter() {
      return getCurrentUserRole() === 'exporter';
    },
    portalContext() {
      return this.$route.query.portal || this.$store.getters['auth/portalContext']?.portal;
    },
    roleContext() {
      return this.$route.query.role || this.$store.getters['auth/portalContext']?.role;
    },
    isFormValid() {
      return this.emailOrPhone.trim() && this.password.trim();
    },
  },
  mounted() {
    // Check for portal context from route or store
    if (this.portalContext) {
      this.successMessage = `Welcome to ${this.getPortalDisplayName()}`;
      setTimeout(() => {
        this.successMessage = '';
      }, 3000);
    }
  },
  methods: {
    onPhoneUpdate(phoneData) {
      // Get the full international number with country code
      this.fullPhoneNumber = phoneData.e164 || phoneData.formatInternational;
      this.emailOrPhone = this.fullPhoneNumber;
    },
    async handleGoogleLogin(user) {
      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';

      try {
        const response = await axios.post('/api/auth/google-login', {
          credential: user.credential,
        });

        if (response.data.success) {
          const data = response.data.data;

          // Check if new user needs to complete registration
          if (data.roleSpecificData?.needsRegistration) {
            this.isLoading = false;
            this.successMessage = 'Account created! Please complete your registration.';

            // // Redirect to sign-up page with pre-filled data
            // setTimeout(() => {
            //   this.$router.push({
            //     name: 'SignUp',
            //     query: {
            //       email: data.roleSpecificData.email,
            //       fullName: data.roleSpecificData.fullName,
            //       fromGoogle: 'true',
            //     },
            //   });
            // }, 1500);
            // return;
          }

          // Check if role selection is required
          if (data.requiresRoleSelection && data.availableRoles) {
            // User has multiple roles - show selection dialog
            this.isLoading = false;
            const selectedRole = await this.$refs.roleSelectionDialog.show(data.availableRoles);

            // Complete login with selected role
            this.isLoading = true;
            await this.completeGoogleLoginWithRole(user.credential, selectedRole);
          } else if (data.token) {
            // Single role - auto-login
            await this.completeLogin(data);
          } else {
            this.handleSignInError('Invalid login response');
          }
        }
      } catch (error) {
        const msg = error.response?.data?.message || error.message || 'Google Login failed';
        this.handleSignInError(msg);
      } finally {
        this.isLoading = false;
      }
    },
    async completeGoogleLoginWithRole(credential, selectedRole) {
      try {
        const response = await axios.post('/api/auth/google-login', {
          credential,
          selectedRole,
        });

        if (response.data.success) {
          await this.completeLogin(response.data.data);
        } else {
          // eslint-disable-next-line sonarjs/no-duplicate-string
          this.handleSignInError(response.data.msg || 'Login failed');
        }
      } catch (error) {
        const msg = error.response?.data?.message || error.message || 'Login failed';
        this.handleSignInError(msg);
      }
    },
    async completeLogin(data) {
      await this.$store.dispatch('auth/setPortalContext', {
        portal: this.portalContext,
        role: this.roleContext,
      });

      await this.$store.dispatch('auth/signIn', { data });

      this.successMessage = 'Sign in successful! Redirecting...';

      setTimeout(() => {
        // After smart role detection, user always has a specific role
        // Redirect directly to dashboard
        this.$router.push({ name: 'Dashboard' });
      }, 1500);

      this.trackFormSubmit('signin_google', true, {
        portal_type: this.portalContext,
      });
    },
    async signIn() {
      if (!this.isFormValid) {
        this.errorMessage = 'Please fill in all required fields';
        return;
      }

      this.isLoading = true;
      this.errorMessage = '';
      this.successMessage = '';
      this.showCreateAccountBtn = false;
      this.showCreateRoleBtn = false;
      this.showRetryBtn = false;
      this.roleName = '';

      try {
        // Smart role detection: only send roleType if portal context is set
        const roleType = this.portalContext ? this.getRoleForAuth() : null;
        const payload = {
          emailOrPhone: this.emailOrPhone.trim(),
          password: this.password,
          roleType,
          portalContext: this.portalContext,
        };

        const response = await axios.post('/api/auth/login', payload);

        if (response.data.success) {
          const data = response.data.data;

          // Check if role selection is required (multi-role user)
          if (data.requiresRoleSelection && data.availableRoles) {
            // User has multiple roles - show selection dialog
            this.isLoading = false;
            const selectedRole = await this.$refs.roleSelectionDialog.show(data.availableRoles);

            // Retry login with selected role
            this.isLoading = true;
            await this.signInWithRole(selectedRole);
            return;
          }

          // Single role or specific role - proceed with login
          await this.$store.dispatch('auth/setPortalContext', {
            portal: this.portalContext,
            role: this.roleContext,
          });

          await this.$store.dispatch('auth/signIn', { data });

          this.successMessage = 'Sign in successful! Redirecting...';

          // Redirect after a short delay
          setTimeout(() => {
            this.$router.push({ name: 'Dashboard' });
          }, 1500);

          // Track successful login
          this.trackFormSubmit('signin', true, {
            portal_type: this.portalContext,
            role_type: roleType,
            input_type: this.inputType,
          });
        } else {
          this.handleSignInError(response.data.msg || 'Login failed');
        }
      } catch (error) {
        const msg = error.response?.data?.message || error.message || 'Network error occurred';
        this.handleSignInError(msg);
      } finally {
        this.isLoading = false;
      }

      // Track login attempt
      this.trackFormInteraction('signin', 'attempt', {
        portal_type: this.portalContext,
        input_type: this.inputType,
      });
    },
    async signInWithRole(selectedRole) {
      try {
        const payload = {
          emailOrPhone: this.emailOrPhone.trim(),
          password: this.password,
          roleType: selectedRole,
        };

        const response = await axios.post('/api/auth/login', payload);

        if (response.data.success) {
          await this.completeLogin(response.data.data);
        } else {
          this.handleSignInError(response.data.msg || 'Login failed');
        }
      } catch (error) {
        const msg = error.response?.data?.message || error.message || 'Login failed';
        this.handleSignInError(msg);
      }
    },
    handleSignInError(msg) {
      this.errorMessage = msg;
      // eslint-disable-next-line no-plusplus
      this.retryCount++;

      // Check for "user not found"
      if (/user not found/i.test(msg)) {
        this.showCreateAccountBtn = true;
      }
      // Check for "User does not have {role} role" or "User does not have role: {ROLE}"
      let roleMatch = msg.match(/User does not have ([\w\s]+) role/i);
      if (!roleMatch) {
        roleMatch = msg.match(/User does not have role:\s*([\w\s]+)/i);
      }
      if (roleMatch) {
        this.showCreateRoleBtn = true;
        // Format role name for display (capitalize, remove underscores)
        this.roleName = roleMatch[1].replace(/_/g, ' ').replace(/\b\w/g, (l) => l.toUpperCase());
      }

      // Show retry button for network errors
      if (/network|timeout|connection/i.test(msg) && this.retryCount < this.maxRetries) {
        this.showRetryBtn = true;
      }

      // Track login failure
      this.trackFormSubmit('signin', false, {
        portal_type: this.portalContext,
        // eslint-disable-next-line no-nested-ternary
        error_type: this.showCreateAccountBtn ? 'user_not_found'
          : this.showCreateRoleBtn ? 'missing_role' : 'auth_failed',
      });
    },

    retrySignIn() {
      this.signIn();
    },

    goToSignUp() {
      this.trackNavigation('create_account_from_signin', 'signup_page');

      const query = {};
      if (this.portalContext) query.portal = this.portalContext;
      if (this.roleContext) query.role = this.roleContext;

      this.$router.push({
        name: 'SignUp',
        query,
      });
    },

    getPortalTitle() {
      if (!this.portalContext) return 'Login';

      const titles = {
        farmer: 'Farmer Login',
        buyer: 'Buyer Login',
        exporter: 'Exporter Login',
        aggregator: 'Aggregator Login',
        processor: 'Processor Login',
        importer: 'Importer Login',
      };

      return titles[this.portalContext] || 'Login';
    },

    getPortalDisplayName() {
      if (!this.portalContext) return '';

      const names = {
        farmer: 'Farmer Portal',
        buyer: 'Buyer Portal',
        exporter: this.getRoleDisplayName(),
        aggregator: 'Aggregator Portal',
        processor: 'Processor Portal',
        importer: 'Importer Portal',
      };

      return names[this.portalContext] || '';
    },

    getRoleDisplayName() {
      if (this.portalContext !== 'exporter' || !this.roleContext) return 'Exporter Portal';

      const roleNames = {
        exporter: 'Super Admin Portal',
        system_admin: 'System Admin Portal',
        zone_supervisor: 'Zone Supervisor Portal',
      };

      return roleNames[this.roleContext] || 'Exporter Portal';
    },

    getPortalColor() {
      const colors = {
        farmer: 'green',
        buyer: 'blue',
        exporter: 'purple',
        aggregator: 'orange',
        processor: 'teal',
        importer: 'indigo',
      };

      return colors[this.portalContext] || 'primary';
    },

    getPortalIcon() {
      const icons = {
        farmer: 'mdi-barn',
        buyer: 'mdi-cart',
        exporter: 'mdi-crown',
        aggregator: 'mdi-warehouse',
        processor: 'mdi-factory',
        importer: 'mdi-ship-wheel',
      };

      return icons[this.portalContext] || 'mdi-account';
    },

    getRoleForAuth() {
      if (this.portalContext === 'exporter' && this.roleContext) {
        return this.roleContext.toUpperCase();
      }

      const roleMap = {
        farmer: 'FARMER',
        buyer: 'BUYER',
        exporter: 'EXPORTER',
        aggregator: 'AGGREGATOR',
        processor: 'PROCESSOR',
        importer: 'IMPORTER',
      };

      return roleMap[this.portalContext] || 'USER';
    },
  },
};
</script>

<style scoped>
/* Professional Enterprise Sign-In Styles */
.professional-signin {
  background: #f8f9fa;
  min-height: 100vh;
}

/* Left Branding Panel */
.branding-panel {
  background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.branding-panel::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: pulse 15s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.1); opacity: 0.5; }
}

.branding-content {
  position: relative;
  z-index: 1;
  max-width: 500px;
  padding: 60px 40px;
}

.brand-header {
  margin-bottom: 60px;
}

.company-name {
  font-size: 42px;
  font-weight: 700;
  margin-bottom: 12px;
  letter-spacing: -0.5px;
}

.company-tagline {
  font-size: 18px;
  opacity: 0.9;
  font-weight: 300;
  line-height: 1.6;
}

/* Features List */
.features-list {
  margin-bottom: 60px;
}

.feature-item {
  display: flex;
  gap: 20px;
  margin-bottom: 32px;
  align-items: flex-start;
}

.feature-item h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 6px;
}

.feature-item p {
  font-size: 14px;
  opacity: 0.85;
  line-height: 1.5;
  margin: 0;
}

.brand-footer {
  opacity: 0.7;
  font-size: 13px;
}

/* Right Form Panel */
.form-panel {
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-container {
  width: 100%;
  max-width: 420px;
  padding: 40px 24px;
}

/* Form Header */
.form-header {
  margin-bottom: 32px;
}

.form-title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.form-subtitle {
  font-size: 15px;
  color: #6b7280;
  margin: 0;
}

.portal-badge {
  font-weight: 500;
  letter-spacing: 0.3px;
}

/* Divider */
.divider-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.divider-text {
  color: #9ca3af;
  font-size: 13px;
  font-weight: 500;
  white-space: nowrap;
}

/* Input Type Selector */
.input-type-selector {
  display: flex;
  gap: 0;
}

.input-type-selector .v-btn {
  text-transform: none;
  font-weight: 500;
  letter-spacing: 0;
}

.active-input-type {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

/* Input Labels */
.input-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

/* Professional Inputs */
.professional-input >>> .v-input__control {
  font-size: 15px;
}

.professional-input >>> .v-input__slot {
  min-height: 44px !important;
  border-radius: 8px !important;
}

.professional-input >>> fieldset {
  border-color: #d1d5db !important;
  border-width: 1.5px !important;
}

.professional-input >>> .v-input__slot:hover fieldset {
  border-color: #9ca3af !important;
}

.professional-input.v-input--is-focused >>> fieldset {
  border-color: #1976d2 !important;
  border-width: 2px !important;
}

.professional-input >>> input::placeholder {
  color: #9ca3af;
  opacity: 1;
}

/* Phone Input */
.professional-phone >>> .vue-phone-number-input {
  border-radius: 8px;
}

.professional-phone >>> .select-country-container {
  border-radius: 8px 0 0 8px;
  border: 1.5px solid #d1d5db;
  border-right: none;
}

.professional-phone >>> .input-tel {
  border-radius: 0 8px 8px 0;
  border: 1.5px solid #d1d5db;
}

/* Forgot Password */
.forgot-password-btn {
  text-transform: none;
  font-size: 13px;
  letter-spacing: 0;
  padding: 0 !important;
  min-width: auto !important;
  height: auto !important;
}

/* Sign In Button */
.signin-btn {
  text-transform: none;
  font-weight: 600;
  font-size: 15px;
  letter-spacing: 0.3px;
  height: 48px !important;
  border-radius: 8px !important;
  box-shadow: none !important;
}

.signin-btn:hover {
  box-shadow: 0 4px 12px rgba(25, 118, 210, 0.3) !important;
}

/* Alerts */
.professional-alert {
  border-radius: 8px !important;
  font-size: 14px;
}

/* Sign Up Section */
.signup-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}

.signup-text {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.signup-link {
  text-transform: none;
  font-weight: 600;
  letter-spacing: 0;
  padding: 0 4px !important;
  min-width: auto !important;
  height: auto !important;
  vertical-align: baseline;
}

/* Support Section */
.support-section {
  padding-top: 16px;
}

.support-text {
  font-size: 13px;
  color: #9ca3af;
  margin-bottom: 8px;
}

/* Mobile Logo */
.mobile-logo {
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* Responsive Design */
@media (max-width: 959px) {
  .form-container {
    max-width: 100%;
    padding: 32px 20px;
  }

  .form-title {
    font-size: 24px;
  }

  .company-name {
    font-size: 32px;
  }
}

/* Button Transitions */
.v-btn {
  transition: all 0.2s ease !important;
}

/* Loading State */
.v-btn--loading {
  pointer-events: none;
}

/* Focus Visible for Accessibility */
.v-btn:focus-visible {
  outline: 2px solid #1976d2;
  outline-offset: 2px;
}

/* Smooth Scrolling */
html {
  scroll-behavior: smooth;
}
</style>
