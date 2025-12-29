<template>
  <v-app>
    <v-main class="signup-wrapper">
      <v-container fluid class="tw-pt-3 fill-height">
        <v-row no-gutters class="fill-height">
          <!-- Left Side: Brand & Testimonial -->
          <v-col cols="12" md="6" lg="4" class="brand-column d-none d-md-flex flex-column justify-space-between pa-10 white--text">
            <!-- Background Image/Gradient -->
            <div class="brand-bg"></div>

            <div class="brand-header tw-relative tw-z-10">
              <div class="tw-d-flex align-center">
                 <v-icon color="white" size="32" class="mr-3">mdi-leaf</v-icon>
                 <span class="tw-text-2xl tw-font-bold tw-tracking-tight">AgriBackup</span>
              </div>
            </div>

            <div class="brand-body my-auto tw-relative tw-z-10">
              <h1 class="tw-text-h3 tw-font-bold tw-mb-6 leading-tight">
                Grow with<br>confidence.
              </h1>
              <p class="tw-text-h6 tw-font-weight-regular tw-opacity-90 tw-mb-10">
                Join the world's most trusted agricultural supply chain platform. Track produce, ensure compliance, and connect with global buyers.
              </p>

              <div v-if="false" class="testimonial-card tw-pa-4 tw-rounded-lg">
                <div class="d-flex align-center mb-3">
                  <v-avatar size="40" color="green lighten-4" class="mr-3">
                    <span class="tw-text-green-600 tw-font-bold">JD</span>
                  </v-avatar>
                  <div>
                    <div class="tw-font-bold tw-text-body-2">John Doe</div>
                    <div class="tw-caption tw-opacity-80">Farmer, Kenya</div>
                  </div>
                  <v-spacer></v-spacer>
                  <v-rating :value="5" color="amber" dense small readonly background-color="rgba(255,255,255,0.3)"></v-rating>
                </div>
                <div class="text-body-2 font-italic opacity-90">
                  "AgriBackup helped me reach international markets I never thought possible. The EUDR compliance tools are a game changer."
                </div>
              </div>
            </div>

            <div class="brand-footer caption opacity-70 tw-relative tw-z-10">
              &copy; {{ new Date().getFullYear() }} AgriBackup Inc. All rights reserved.
            </div>
          </v-col>

          <!-- Right Side: Form -->
          <v-col cols="12" md="6" lg="8" class="form-column white">
            <div class="form-container">
              <!-- Header -->
              <div class="text-center mb-8">
                <h2 class="text-h4 font-weight-bold mb-2">{{ stepTitle }}</h2>
                <p class="text-body-1 grey--text text--darken-1">{{ stepSubtitle }}</p>
              </div>

              <v-window v-model="step" touchless>
                <!-- Step 1: Registration -->
                <v-window-item :value="1">
                  <!-- Google Sign In -->
                  <GoogleSignIn @google-login-success="handleGoogleLogin" />

                  <div class="divider-container my-6">
                    <div class="divider-line"></div>
                    <div class="divider-text">or register with email</div>
                    <div class="divider-line"></div>
                  </div>

                  <v-form ref="form" v-model="valid" @submit.prevent="signUpUser">
                    <v-row>
                      <v-col cols="12">
                        <label class="input-label">Full Name</label>
                        <v-text-field
                          v-model="form.fullName"
                          :rules="[rules.required]"
                          placeholder="John Doe"
                          outlined
                          dense
                          class="professional-input"
                          hide-details="auto"
                        ></v-text-field>
                      </v-col>

                      <v-col cols="12">
                        <label class="input-label">Email Address</label>
                        <v-text-field
                          v-model="form.email"
                          :rules="[rules.required, rules.email]"
                          placeholder="john@example.com"
                          outlined
                          dense
                          class="professional-input"
                          hide-details="auto"
                        ></v-text-field>
                      </v-col>

                      <v-col cols="12">
                        <label class="input-label">Phone Number</label>
                        <VuePhoneNumberInput
                          v-model="form.phoneNumber"
                          :preferred-countries="['KE', 'UG', 'TZ', 'RW']"
                          default-country-code="KE"
                          @update="onPhoneUpdate"
                          class="professional-phone-input mb-4"
                        />
                      </v-col>

                      <v-col cols="12">
                        <label class="input-label">Password</label>
                        <v-text-field
                          v-model="form.password"
                          :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                          :type="showPassword ? 'text' : 'password'"
                          :rules="[rules.required, rules.min]"
                          placeholder="Min. 8 characters"
                          outlined
                          dense
                          class="professional-input"
                          hide-details="auto"
                          @click:append="showPassword = !showPassword"
                        ></v-text-field>
                      </v-col>

                      <v-col cols="12">
                        <v-checkbox
                          v-model="form.terms"
                          color="green darken-2"
                          hide-details
                          class="mt-0"
                        >
                          <template v-slot:label>
                            <div class="text-body-2 grey--text text--darken-1">
                              I agree to the <a href="#" @click.prevent="openTermsDialog" class="green--text text--darken-2 font-weight-bold text-decoration-none">Terms of Service</a> and <a href="#" class="green--text text--darken-2 font-weight-bold text-decoration-none">Privacy Policy</a>.
                            </div>
                          </template>
                        </v-checkbox>
                      </v-col>

                      <v-col cols="12" class="mt-6">
                        <v-btn
                          color="green darken-2"
                          block
                          x-large
                          depressed
                          :loading="loading"
                          :disabled="!valid || !form.terms"
                          type="submit"
                          class="submit-btn white--text text-capitalize text-body-1 font-weight-bold"
                          elevation="2"
                        >
                          Create Account
                        </v-btn>
                      </v-col>
                    </v-row>
                  </v-form>

                  <div class="text-center mt-8">
                    <p class="body-2 grey--text text--darken-1">
                      Already have an account? <a href="#" @click.prevent="$emit('go-to-signin')" class="green--text text--darken-2 font-weight-bold text-decoration-none hover-underline">Sign in</a>
                    </p>
                  </div>
                </v-window-item>

                <!-- Step 2: Role Selection -->
                <v-window-item :value="2">
                  <v-row>
                    <v-col v-for="role in availableRoles" :key="role.roleType" cols="12">
                      <v-card
                        outlined
                        class="mb-3 role-selection-card"
                        :class="{ 'selected': selectedRoles.includes(role.roleType) }"
                        @click="toggleRole(role.roleType)"
                        style="cursor: pointer; transition: all 0.2s;"
                      >
                        <v-card-text class="d-flex align-center pa-4">
                           <v-icon size="32" :color="role.color" class="mr-4">{{ role.icon }}</v-icon>
                           <div>
                             <div class="font-weight-bold text-body-1 black--text">{{ role.displayName }}</div>
                             <div class="caption grey--text">{{ role.description }}</div>
                           </div>
                           <v-spacer></v-spacer>
                           <v-checkbox
                             :input-value="selectedRoles.includes(role.roleType)"
                             readonly
                             hide-details
                             class="mt-0 pt-0"
                             color="green darken-2"
                           ></v-checkbox>
                        </v-card-text>
                      </v-card>

                      <!-- Supplier Type Sub-selection -->
                      <v-expand-transition>
                        <div v-if="role.roleType === 'SUPPLIER' && selectedRoles.includes('SUPPLIER')">
                          <v-card flat class="ml-8 mb-4 blue lighten-5" style="border-left: 4px solid #1976d2;">
                            <v-card-text>
                              <div class="font-weight-bold mb-3 blue--text text--darken-2">
                                <v-icon small color="blue darken-2" class="mr-1">mdi-help-circle</v-icon>
                                What type of supplier are you?
                              </div>
                              <v-row dense>
                                <v-col
                                  v-for="stype in supplierTypes"
                                  :key="stype.value"
                                  cols="12"
                                  sm="6"
                                >
                                  <v-card
                                    outlined
                                    :class="{
                                      'elevation-2 blue lighten-4': selectedSupplierType === stype.value,
                                      'grey lighten-4': selectedSupplierType !== stype.value
                                    }"
                                    @click="selectedSupplierType = stype.value"
                                    style="cursor: pointer; transition: all 0.2s;"
                                    class="pa-2"
                                  >
                                    <div class="d-flex align-center">
                                      <v-icon :color="selectedSupplierType === stype.value ? 'blue darken-2' : 'grey'" class="mr-2">
                                        {{ stype.icon }}
                                      </v-icon>
                                      <div class="flex-grow-1">
                                        <div class="font-weight-medium text-body-2">{{ stype.text }}</div>
                                        <div class="caption grey--text">{{ stype.description }}</div>
                                      </div>
                                      <v-icon v-if="selectedSupplierType === stype.value" color="blue darken-2">
                                        mdi-check-circle
                                      </v-icon>
                                    </div>
                                  </v-card>
                                </v-col>
                              </v-row>
                            </v-card-text>
                          </v-card>
                        </div>
                      </v-expand-transition>
                    </v-col>
                  </v-row>
                  <v-btn
                    block
                    x-large
                    color="green darken-2"
                    class="white--text mt-6"
                    :disabled="!canSubmitRoles"
                    :loading="loading"
                    @click="submitRoles"
                  >
                    Continue
                  </v-btn>
                </v-window-item>

                <!-- Step 3: Welcome -->
                <v-window-item :value="3">
                  <div class="text-center py-12">
                    <v-icon size="80" color="green darken-2" class="mb-6">mdi-check-circle-outline</v-icon>
                    <h3 class="text-h5 font-weight-bold mb-2">You're all set!</h3>
                    <p class="grey--text text--darken-1">Redirecting you to your dashboard...</p>
                    <v-progress-circular indeterminate color="green darken-2" class="mt-6"></v-progress-circular>
                  </div>
                </v-window-item>
              </v-window>
            </div>
          </v-col>
        </v-row>
      </v-container>
    </v-main>

    <terms-and-conditions ref="termsDialog"/>
  </v-app>
</template>

<script>
import axios from 'axios';
import VuePhoneNumberInput from 'vue-phone-number-input';
import 'vue-phone-number-input/dist/vue-phone-number-input.css';
import TermsAndConditions from '@/components/auth/TermsAndConditions.vue';
import GoogleSignIn from '@/components/auth/GoogleSignIn.vue';

export default {
  name: 'UserSignUp',
  components: {
    VuePhoneNumberInput,
    TermsAndConditions,
    GoogleSignIn,
  },
  data() {
    return {
      step: 1,
      valid: false,
      loading: false,
      showPassword: false,
      form: {
        fullName: '',
        email: '',
        phoneNumber: '',
        fullPhoneNumber: '',
        password: '',
        terms: false,
      },
      isGoogleUser: false,
      registeredUserId: null,
      selectedRoles: [],
      selectedSupplierType: null,
      supplierTypes: [
        {
          value: 'AGGREGATOR',
          text: 'Aggregator',
          description: 'Collect produce from farmers',
          icon: 'mdi-warehouse',
        },
        {
          value: 'PROCESSOR',
          text: 'Processor',
          description: 'Process raw produce',
          icon: 'mdi-factory',
        },
        {
          value: 'DISTRIBUTOR',
          text: 'Distributor',
          description: 'Distribute to markets',
          icon: 'mdi-truck-delivery',
        },
        {
          value: 'TRADER',
          text: 'Trader',
          description: 'Trade agricultural products',
          icon: 'mdi-handshake',
        },
        {
          value: 'WAREHOUSE',
          text: 'Warehouse',
          description: 'Store agricultural products',
          icon: 'mdi-warehouse',
        },
        {
          value: 'TRANSPORTER',
          text: 'Transporter',
          description: 'Transport products',
          icon: 'mdi-truck',
        },
        {
          value: 'COOPERATIVE',
          text: 'Cooperative',
          description: 'Farmer cooperative organization',
          icon: 'mdi-account-group',
        },
        {
          value: 'CERTIFICATION_BODY',
          text: 'Certification Body',
          description: 'Provide certifications',
          icon: 'mdi-certificate',
        },
        {
          value: 'OTHER',
          text: 'Other',
          description: 'Other supply chain participant',
          icon: 'mdi-dots-horizontal',
        },
      ],
      availableRoles: [
        {
          roleType: 'FARMER', displayName: 'Farmer', description: 'I grow crops or raise livestock', icon: 'mdi-sprout', color: 'green darken-2',
        },
        {
          roleType: 'SUPPLIER', displayName: 'Supplier', description: 'I am a supply chain participant (aggregator, processor, distributor, etc.)', icon: 'mdi-link-variant', color: 'blue darken-2',
        },
        {
          roleType: 'EXPORTER', displayName: 'Exporter', description: 'I export produce to international markets and need EUDR compliance', icon: 'mdi-airplane', color: 'purple darken-2',
        },
      ],
      rules: {
        required: (v) => !!v || 'Required',
        email: (v) => /.+@.+\..+/.test(v) || 'Invalid email',
        min: (v) => v.length >= 8 || 'Min 8 chars',
      },
    };
  },
  computed: {
    stepTitle() {
      if (this.step === 1) return 'Create your account';
      if (this.step === 2) return 'Getting Started';
      return 'Welcome to AgriBackup';
    },
    stepSubtitle() {
      if (this.step === 1) return 'Start your journey with AgriBackup today.';
      if (this.step === 2) return 'Select the roles you want to be involved in.';
      return 'Your account is ready.';
    },
    canSubmitRoles() {
      if (this.selectedRoles.length === 0) return false;
      // If SUPPLIER is selected, require a supplier type
      if (this.selectedRoles.includes('SUPPLIER') && !this.selectedSupplierType) {
        return false;
      }
      return true;
    },
  },
  methods: {
    onPhoneUpdate(phoneData) {
      this.form.fullPhoneNumber = phoneData.e164 || phoneData.formatInternational;
    },
    handleGoogleLogin(user) {
      this.form.fullName = user.fullName;
      this.form.email = user.email;
      this.isGoogleUser = true;
      this.signUpUser();
    },
    openTermsDialog() {
      this.$refs.termsDialog.openDialog();
    },
    async signUpUser() {
      if (!this.$refs.form.validate() && !this.isGoogleUser) return;

      this.loading = true;
      try {
        const payload = {
          fullName: this.form.fullName,
          email: this.form.email,
          phoneNumber: this.form.fullPhoneNumber,
          password: this.form.password,
        };

        const response = await axios.post('/api/auth/register/user', payload);

        if (response.data.success) {
          this.$toast.success('Account created successfully!');
          this.registeredUserId = response.data.data.id;
          this.step = 2;
          window.scrollTo(0, 0);
        } else {
          this.$toast.error(response.data.msg || 'Registration failed');
        }
      } catch (error) {
        const msg = error.response?.data?.message || error.message || 'An error occurred';
        this.$toast.error(msg);
      } finally {
        this.loading = false;
      }
    },
    toggleRole(roleType) {
      const index = this.selectedRoles.indexOf(roleType);
      if (index === -1) {
        this.selectedRoles.push(roleType);
      } else {
        this.selectedRoles.splice(index, 1);
        // Clear supplier type if SUPPLIER is deselected
        if (roleType === 'SUPPLIER') {
          this.selectedSupplierType = null;
        }
      }
    },
    async submitRoles() {
      if (!this.canSubmitRoles) return;

      this.loading = true;
      try {
        const payload = {
          userId: this.registeredUserId,
          roles: this.selectedRoles,
        };

        // Include supplier type if SUPPLIER role is selected
        if (this.selectedRoles.includes('SUPPLIER') && this.selectedSupplierType) {
          payload.supplierType = this.selectedSupplierType;
        }

        const response = await axios.post('/api/auth/assign-roles', payload);

        if (response.data.success) {
          // Auto-login
          await this.autoLogin();

          this.step = 3;
          setTimeout(() => {
            this.$router.push({ name: 'Dashboard' });
          }, 2000);
        } else {
          this.$toast.error(response.data.msg || 'Failed to assign roles');
        }
      } catch (error) {
        const msg = error.response?.data?.message || error.message || 'An error occurred';
        this.$toast.error(msg);
      } finally {
        this.loading = false;
      }
    },
    async autoLogin() {
      try {
        const payload = {
          emailOrPhone: this.form.email || this.form.fullPhoneNumber,
          password: this.form.password,
          // We don't specify roleType, so backend will detect roles.
          // Since we just assigned roles, it should return success with roles.
          // If multiple roles, it might return requiresRoleSelection.
          // But we want to just log them in.
          // If requiresRoleSelection is true, we still get a token? No.
          // We need to handle this.
        };

        // If user selected multiple roles, login might return requiresRoleSelection.
        // If user selected one role, login might auto-login.

        // Actually, for the purpose of "Getting Started" -> Dashboard,
        // we probably want to land them on the Dashboard with *one* of the roles active.
        // Or land them on RoleSelection if they have multiple?
        // The user said "one is taken to the dashboard".

        // Let's try to login.
        const response = await axios.post('/api/auth/login', payload);

        if (response.data.success) {
          const data = response.data.data;
          if (data.requiresRoleSelection && data.availableRoles) {
            // If multiple roles, pick the first one from selectedRoles to start with?
            // Or just redirect to RoleSelection page?
            // But we want Dashboard.
            // Let's pick the first selected role.
            const firstRole = this.selectedRoles[0];
            await this.loginWithRole(firstRole);
          } else {
            await this.$store.dispatch('auth/setPortalContext', {
              portal: null, // or detect from role
              role: null,
            });
            await this.$store.dispatch('auth/signIn', { data });
          }
        }
      } catch (e) {
        this.$toast.error('Auto-login failed', e.message);
      }
    },
    async loginWithRole(roleType) {
      const payload = {
        emailOrPhone: this.form.email || this.form.fullPhoneNumber,
        password: this.form.password,
        roleType,
      };
      const response = await axios.post('/api/auth/login', payload);
      if (response.data.success) {
        await this.$store.dispatch('auth/signIn', { data: response.data.data });
      }
    },
  },
};
</script>

<style scoped>
/* Remove all default padding/margin */
.v-application,
.v-application .v-main,
.signup-wrapper {
  padding: 0 !important;
  margin: 0 !important;
}

.signup-wrapper {
  background: #f8f9fa;
  min-height: 100vh;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

.role-selection-card {
  transition: all 0.2s;
  border: 2px solid transparent !important;
}
.role-selection-card:hover {
  border-color: #2e7d32 !important;
  background-color: #f1f8e9;
}
.role-selection-card.selected {
  border-color: #2e7d32 !important;
  background-color: #e8f5e9;
}

/* Brand Column */
.brand-column {
  position: relative;
  background-color: #1b5e20;
  overflow: hidden;
}

.brand-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #1b5e20 0%, #2e7d32 100%);
  z-index: 0;
}

.brand-bg::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  z-index: 1;
}

.testimonial-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.relative { position: relative; }
.z-10 { z-index: 10; }
.leading-tight { line-height: 1.2; }
.tracking-tight { letter-spacing: -0.5px; }
.opacity-90 { opacity: 0.9; }
.opacity-80 { opacity: 0.8; }
.opacity-70 { opacity: 0.7; }

/* Form Column */
.form-column {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.form-container {
  width: 100%;
  max-width: 480px;
  padding: 2rem;
  margin: auto;
}

/* Divider */
.divider-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
}

.divider-line {
  flex-grow: 1;
  height: 1px;
  background-color: #e0e0e0;
}

.divider-text {
  padding: 0 16px;
  color: #757575;
  font-size: 0.875rem;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* Inputs */
.input-label {
  display: block;
  font-size: 0.875rem;
  font-weight: 600;
  color: #424242;
  margin-bottom: 6px;
}

.professional-input >>> .v-input__slot {
  min-height: 48px !important;
  border-radius: 8px !important;
}

.professional-input >>> .v-input__control > .v-input__slot fieldset {
  border-color: #e0e0e0;
}

.professional-input:hover >>> .v-input__control > .v-input__slot fieldset {
  border-color: #bdbdbd;
}

.professional-input.v-input--is-focused >>> .v-input__control > .v-input__slot fieldset {
  border-color: #2e7d32 !important;
  border-width: 2px;
}

/* Phone Input */
.professional-phone-input >>> .country-selector__input,
.professional-phone-input >>> .input-tel__input {
  border-radius: 8px !important;
  border-color: #e0e0e0 !important;
  height: 48px !important;
  box-shadow: none !important;
}

.professional-phone-input >>> .country-selector__input:focus,
.professional-phone-input >>> .input-tel__input:focus {
  border-color: #2e7d32 !important;
  border-width: 2px !important;
}

/* Button */
.submit-btn {
  border-radius: 8px;
  height: 52px !important;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(46, 125, 50, 0.25) !important;
}

.hover-underline:hover {
  text-decoration: underline !important;
}

/* Responsive */
@media (max-width: 960px) {
  .signup-wrapper {
    height: auto;
    min-height: 100vh;
    overflow-y: auto;
  }

  .form-column {
    padding: 2rem 1rem;
    align-items: flex-start;
  }

  .form-container {
    margin: 0 auto;
    padding-top: 2rem;
  }
}
</style>
