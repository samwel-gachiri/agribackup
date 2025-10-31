<template>
  <v-app>
    <v-main>
      <div class="minimal-bg">
        <!-- Minimal SVG background graphic -->
        <svg
          class="minimal-bg-svg"
          viewBox="0 0 800 600"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <ellipse
            cx="400"
            cy="300"
            rx="320"
            ry="180"
            fill="#1976d2"
            fill-opacity="0.08"
          />
          <ellipse
            cx="600"
            cy="100"
            rx="120"
            ry="60"
            fill="#43a047"
            fill-opacity="0.1"
          />
          <ellipse
            cx="200"
            cy="500"
            rx="100"
            ry="40"
            fill="#fffde7"
            fill-opacity="0.13"
          />
        </svg>
      </div>
      <v-container class="fill-height" fluid>
        <v-row align="center" justify="center">
          <v-col cols="12" sm="10" md="6">
            <v-card
              class="pa-6"
              elevation="12"
              rounded="xl"
              >
              <!-- :class="{ 'expensive-exporter-card': isExporter }" -->
              <v-card-title class="justify-center">
                <v-avatar size="64">
                  <v-img src="@/assets/images/happy-farmer.jpg" alt="Happy Farmer" />
                </v-avatar>
              </v-card-title>
              <v-card-subtitle class="text-center text-h5 font-weight-bold mb-2">
                {{ getPortalTitle() }}
              </v-card-subtitle>
              <!-- Portal Context Display -->
              <div v-if="portalContext" class="text-center mb-4">
                <v-chip
                  :color="getPortalColor()"
                  text-color="white"
                  small
                  class="ma-1"
                >
                  <v-icon left small>{{ getPortalIcon() }}</v-icon>
                  {{ getPortalDisplayName() }}
                </v-chip>
              </div>
              <v-card-text>
                <v-btn-toggle v-model="inputType" class="mb-4" mandatory>
                  <v-btn value="email" variant="outlined">Email</v-btn>
                  <v-btn value="phone" variant="outlined">Phone</v-btn>
                </v-btn-toggle>
                <div v-if="inputType === 'email'">
                  <v-text-field
                    v-model="emailOrPhone"
                    label="Email"
                    type="email"
                    prepend-inner-icon="mdi-email-outline"
                    outlined
                    dense
                    required
                    autocomplete="email"
                    name="email"
                  />
                </div>
                <div v-else class="tw-mb-6">
                  <vue-phone-number-input
                    v-model="phoneNumber"
                    :default-country-code="'KE'"
                    :preferred-countries="['KE', 'US', 'GB']"
                    class="w-100"
                    show-code-on-list
                    aria-label="Phone"
                    @update="onPhoneUpdate"
                    autocomplete="tel"
                    name="phone"
                  />
                </div>
                <v-text-field
                  v-model="password"
                  :type="showPassword ? 'text' : 'password'"
                  label="Password"
                  prepend-inner-icon="mdi-lock-outline"
                  :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                  outlined
                  dense
                  required
                  autocomplete="current-password"
                  name="password"
                  class="mt-2"
                  @keydown.enter="signIn"
                  @click:append-inner="showPassword = !showPassword"
                />
                <v-btn
                  color="primary"
                  class="mt-4"
                  block
                  large
                  rounded
                  :loading="isLoading"
                  @click="signIn"
                >
                  <v-icon left>mdi-login</v-icon>
                  Sign In
                </v-btn>
                <!-- Sign In{{ portalContext ? ` to ${getPortalDisplayName()}` : '' }} -->
                <!-- Error message display -->
                <v-alert
                  v-if="errorMessage"
                  type="error"
                  class="mt-4"
                  dense
                  outlined
                >
                  <div class="d-flex align-center">
                    <!-- <v-icon color="error" class="mr-2">mdi-alert-circle</v-icon> -->
                    <span>{{ errorMessage }}</span>
                  </div>
                  <div v-if="showCreateAccountBtn" class="mt-3">
                    <v-btn
                      color="success"
                      rounded
                      block
                      @click="goToSignUp"
                    >
                      <v-icon left>mdi-account-plus</v-icon>
                      Create Account
                    </v-btn>
                  </div>
                  <div v-else-if="showCreateRoleBtn" class="mt-3">
                    <v-btn
                      color="warning"
                      rounded
                      block
                      @click="goToSignUp"
                    >
                      <v-icon left>mdi-account-cog</v-icon>
                      Create {{ roleName }} Role
                    </v-btn>
                  </div>
                  <div v-if="showRetryBtn" class="mt-3">
                    <v-btn
                      color="primary"
                      rounded
                      block
                      outlined
                      @click="retrySignIn"
                    >
                      <v-icon left>mdi-refresh</v-icon>
                      Try Again
                    </v-btn>
                  </div>
                </v-alert>

                <!-- Success message display -->
                <v-alert
                  v-if="successMessage"
                  type="success"
                  class="mt-4"
                  dense
                  outlined
                >
                  <div class="d-flex align-center">
                    <!-- <v-icon color="success" class="mr-2">mdi-check-circle</v-icon> -->
                    <span>{{ successMessage }}</span>
                  </div>
                </v-alert>
                <div class="mt-3 text-center">
                  <span class="text-caption">Forgot password?</span>
                  <v-btn
                    text
                    color="primary"
                    class="ml-1"
                    small
                    @click="$router.push({ name: 'ForgotPassword' })"
                  >Reset</v-btn>
                </div>
                <div class="mt-4 text-center">
                  <v-btn
                    outlined
                    color="success"
                    large
                    block
                    @click="$router.push({ name: 'SignUp' })"
                  >
                    <v-icon left>mdi-account-plus</v-icon>
                    Create account
                  </v-btn>
                </div>

                <!-- Contact Information -->
                <div class="mt-4 text-center">
                  <span class="text-caption text--secondary">Need help? Contact us:</span>
                  <br>
                  <v-btn
                    text
                    color="primary"
                    small
                    href="tel:+254705644805"
                    class="mt-1"
                  >
                    <v-icon left small>mdi-phone</v-icon>
                    +254 705 644 805
                  </v-btn>
                </div>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<script>
import axios from 'axios';
import VuePhoneNumberInput from 'vue-phone-number-input';
import 'vue-phone-number-input/dist/vue-phone-number-input.css';
import { getCurrentUserRole } from '@/utils/roles.js';

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
  components: { VuePhoneNumberInput },
  data() {
    return {
      inputType: 'email',
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
    if (!this.portalContext) {
      this.$toast.info('Please select a portal to sign in');
      const currentPath = window.location.pathname + window.location.search + window.location.hash;
      this.$router.push({
        name: 'Home',
        query: {
          redirect: btoa(currentPath),
        },
      });
    } else {
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
        const roleType = this.getRoleForAuth();
        const payload = {
          emailOrPhone: this.emailOrPhone.trim(),
          password: this.password,
          roleType,
          portalContext: this.portalContext,
        };

        const response = await axios.post('/api/auth/login', payload);

        if (response.data.success) {
          const data = response.data.data;

          // Store portal and role context
          await this.$store.dispatch('auth/setPortalContext', {
            portal: this.portalContext,
            role: this.roleContext,
          });

          await this.$store.dispatch('auth/signIn', { data });

          this.successMessage = 'Sign in successful! Redirecting...';

          // Redirect after a short delay
          setTimeout(() => {
            // const redirect = this.$route.query.redirect;
            // if (redirect) {
            //   try {
            //     this.$router.push(atob(redirect));
            //   } catch {
            //     this.$router.push({ name: 'Dashboard' });
            //   }
            // } else {
            //   this.$router.push({ name: 'Dashboard' });
            // }
            this.$router.push({ name: 'Dashboard' });
          }, 1500);
        } else {
          this.handleSignInError(response.data.msg || 'Login failed');
        }
      } catch (error) {
        const msg = error.response?.data?.message || error.message || 'Network error occurred';
        this.handleSignInError(msg);
      } finally {
        this.isLoading = false;
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
    },

    retrySignIn() {
      this.signIn();
    },

    goToSignUp() {
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

      return roleMap[this.portalContext] || 'FARMER';
    },
  },
};
</script>

<style scoped>
/* Vuetify handles most styling. Custom styles for vue-phone-number-input: */
.vue-phone-number-input__input-wrapper {
  display: flex;
  gap: 0.5rem;
}
.vue-phone-number-input__country-selector {
  border-radius: 20px !important;
  border: 1px solid #d1d5db;
  background: #fff;
  margin: 0.5rem 0.75rem;
}
.input-tel input-phone-number {
  border-radius: 0.75rem;
  border: 1px solid #0e5acc;
  background: #fff;
  padding: 0.5rem 0.75rem;
  flex: 1;
}
.vue-phone-number-input__country-selector:focus,
.vue-phone-number-input__input:focus {
  border-color: #1976d2;
  box-shadow: 0 0 0 2px #1976d2;
  outline: none;
}

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

/* Expensive styling for exporter role */
.expensive-exporter-card {
  background: linear-gradient(135deg, #f7e9a0 0%, #e3c888 100%);
  border: 2px solid #bfa14a;
  box-shadow: 0 8px 32px 0 rgba(191, 161, 74, 0.25);
  color: #7c6a2c;
  position: relative;
}
.expensive-exporter-card::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  pointer-events: none;
  border: 3px double #d4af37;
  opacity: 0.5;
}
</style>
