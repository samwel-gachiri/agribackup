<template>
  <v-app id="inspire">
    <!-- Background Animation - hidden in exporter mode -->
    <div v-if="mode !== 'exporter'" class="background-animation">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <v-main style="height: 100vh; max-height: 100vh;">
      <!-- Main Content -->
      <div class="tw-w-full tw-flex tw-flex-row" style="height: 100%;" :class="mode === 'exporter' ? 'tw-bg-gray-50' : 'tw-bg-white'">
        <!-- Left Side - Image (hidden on mobile and exporter mode) -->
        <div v-if="mode !== 'exporter'" class="tw-hidden lg:tw-block lg:tw-w-1/2 tw-relative">
          <v-img
            src="@/assets/images/happy-farmer.jpg"
            class="tw-h-full tw-object-cover"
            gradient="to bottom, rgba(0,0,0,.3), rgba(0,0,0,.6)"
          >
            <div class="tw-absolute tw-bottom-10 tw-left-10 tw-text-white">
              <h2 class="tw-text-3xl tw-font-bold tw-mb-2">Welcome to AgriConnect</h2>
              <p class="tw-text-lg tw-opacity-90">Connecting farmers with opportunities</p>
            </div>
          </v-img>
        </div>

        <!-- Right Side - Form -->
        <div :class="mode === 'exporter' ? 'tw-w-full tw-px-4 sm:tw-px-8 tw-py-6' : 'tw-w-full lg:tw-w-1/2 tw-px-4 sm:tw-px-8 tw-py-6'">
          <div class="tw-max-w-md tw-mx-auto tw-h-full tw-flex tw-flex-col tw-justify-center">

            <!-- Exporter Mode Header -->
            <div v-if="mode === 'exporter'" class="tw-mb-8 tw-text-center">
              <div v-if="false" class="tw-bg-teal-500 tw-text-white tw-px-4 tw-py-2 tw-rounded-full tw-inline-block tw-mb-4">
                <v-icon color="white" class="tw-mr-2">mdi-account-plus</v-icon>
                Exporter Mode
              </div>
              <h1 class="tw-text-2xl tw-font-bold tw-text-gray-800 tw-mb-2">Farmer Details:</h1>
              <!-- <p class="tw-text-gray-600">Register a farmer to your zone id: {{this.$route.query.zoneId}}</p> -->
            </div>

            <!-- Card Container -->
            <v-card
              :class="mode === 'exporter' ? 'tw-shadow-lg' : 'tw-shadow-xl'"
              :style="mode === 'exporter' ? 'border-radius: 16px; border: 1px solid #e0e0e0;' : 'border-radius: 20px; border: 1px solid #f6eeee;'"
              flat
            >
              <!-- Exporter Mode Progress Indicator -->
              <div v-if="mode === 'exporter'" class="tw-p-4 tw-border-b tw-border-gray-200">
                <v-progress-linear
                  :value="(step / 4) * 100"
                  color="primary"
                  height="4"
                  rounded
                ></v-progress-linear>
                <div class="tw-flex tw-justify-between tw-mt-2 tw-text-xs tw-text-gray-600">
                  <span>Contact Info</span>
                  <span>Details</span>
                  <span>Verification</span>
                </div>
              </div>

              <v-card-text class="tw-p-4 sm:tw-p-6">
                <v-stepper
                  flat
                  v-model="step"
                  class="tw-shadow-none"
                >
                  <!-- Step Header -->
                  <div class="tw-mb-6 tw-text-center">
                    <h3 class="tw-text-xl tw-font-semibold" :class="mode === 'exporter' ? 'tw-text-gray-800' : 'tw-text-gray-700'">
                      {{steps[step - 1].title}}
                    </h3>
                    <div class="tw-mt-3 tw-flex tw-justify-center tw-space-x-2">
                      <div
                        v-for="(stepItem, index) in steps"
                        :key="index"
                        :class="[
                          'tw-w-3 tw-h-3 tw-rounded-full tw-transition-all tw-duration-300',
                          index + 1 === step ?
                            (mode === 'exporter' ? 'tw-bg-teal-500' : 'tw-bg-blue-500') :
                            'tw-bg-gray-300'
                        ]"
                      ></div>
                    </div>
                  </div>

                  <!-- Step 1 - Contact Information -->
                  <v-form v-if="step === 1" v-model="isValid1" class="tw-space-y-6">
                    <div class="tw-space-y-4">
                      <div>
                        <label class="tw-block tw-text-sm tw-font-medium" :class="mode === 'exporter' ? 'tw-text-gray-700' : 'tw-text-gray-600'">
                          Phone Number
                        </label>
                        <vue-phone-number-input
                          v-model="form.phoneNumber"
                          :default-country-code="'KE'"
                          :preferred-countries="['KE', 'US', 'GB']"
                          show-code-on-list
                          @update="onPhoneUpdate"
                          class="tw-w-full"
                        />
                      </div>
                    </div>

                    <div class="tw-space-y-4">
                      <v-btn
                        :color="mode === 'exporter' ? 'teal' : 'primary'"
                        :loading="loading"
                        @click="continueStepOne"
                        :disabled="!isValid1"
                        class="tw-w-full tw-py-3"
                        large
                        rounded
                      >
                        <v-icon left>
                          {{ mode === 'exporter' ? 'mdi-account-plus' : 'mdi-arrow-right' }}
                        </v-icon>
                        {{ mode === 'exporter' ? 'Add Farmer' : (this.mode === 'self' ? 'Continue' : 'Add Farmer')}}
                      </v-btn>

                      <v-divider v-if="mode !== 'exporter'" class="tw-my-4"></v-divider>

                      <v-btn
                        v-if="mode !== 'exporter'"
                        color="white"
                        @click="signInWithGooglePopup"
                        outlined
                        large
                        class="tw-w-full tw-py-3"
                        style="border: 2px solid #e5e7eb; text-transform: none;"
                      >
                        <img src="https://upload.wikimedia.org/wikipedia/commons/3/3c/Google_Favicon_2025.svg" width="24" height="24" alt="Google Logo" class="tw-mr-3">
                        <span style="color: #374151; font-size: 16px; font-weight: 500;">Continue with Google</span>
                      </v-btn>
                    </div>
                  </v-form>

                  <!-- Step 2 - Profile Information -->
                  <v-form v-if="step === 2" v-model="isValid2" class="tw-space-y-6">
                    <div class="tw-space-y-4">

                      <div class="tw-flex tw-space-x-3">
                        <v-btn
                          outlined
                          @click="step = 1"
                          class="tw-flex-1"
                        >
                          <v-icon left>mdi-arrow-left</v-icon>
                          Back
                        </v-btn>
                        <v-btn
                          :color="mode === 'exporter' ? 'teal' : 'primary'"
                          :loading="loading"
                          @click="continueStepTwo"
                          class="tw-flex-1"
                        >
                          <v-icon left>mdi-check</v-icon>
                          {{ mode === 'exporter' ? 'Add Farmer' : 'Sign Up' }}
                        </v-btn>
                      </div>
                    </div>
                  </v-form>

                  <!-- Step 4 - OTP Verification -->
                  <v-form v-if="step === 4" v-model="isValid4" class="tw-space-y-6">
                    <div class="tw-text-center tw-mb-6">
                      <v-icon :color="mode === 'exporter' ? 'teal' : 'primary'" size="48" class="tw-mb-3">
                        mdi-shield-check
                      </v-icon>
                      <p class="tw-text-gray-600">Enter the verification code sent to your phone</p>
                    </div>

                    <div class="tw-flex tw-justify-center tw-mb-6">
                      <v-otp-input
                        v-model="otp"
                        :color="mode === 'exporter' ? 'teal' : 'primary'"
                      />
                    </div>

                    <div class="tw-flex tw-space-x-3">
                      <v-btn
                        outlined
                        @click="userMustBeSignedUp? step = 2: step = 1"
                        class="tw-flex-1"
                      >
                        <v-icon left>mdi-arrow-left</v-icon>
                        Back
                      </v-btn>
                      <v-btn
                        :color="mode === 'exporter' ? 'teal' : 'primary'"
                        :loading="loading"
                        @click="verifyOtp"
                        :disabled="!isValid4"
                        class="tw-flex-1"
                      >
                        <v-icon left>mdi-check-circle</v-icon>
                        Verify
                      </v-btn>
                    </div>
                  </v-form>
                </v-stepper>
              </v-card-text>
            </v-card>
          </div>
        </div>
      </div>
    </v-main>
    <div id="recaptcha-container"></div>
  </v-app>
</template>
<script>
import VuePhoneNumberInput from 'vue-phone-number-input';
import validations from '@/utils/validations.js';
import AuthMixins from '@/mixins/AuthMixins.js';
import { getCurrentUserRole, isAuthenticated } from '@/utils/roles.js';
import 'vue-phone-number-input/dist/vue-phone-number-input.css';
// import GoogleSignIn from '@/components/auth/GoogleSignIn.vue';
// import Auth from '@aws-amplify/auth';
// import AuthConfig from '@/utils/aws-exports.js';
import { auth } from '@/firebase.js';
import {
  RecaptchaVerifier, signInWithPhoneNumber, GoogleAuthProvider, signInWithPopup, signInWithRedirect, getRedirectResult, setPersistence, browserLocalPersistence, onAuthStateChanged,
// eslint-disable-next-line import/extensions
} from 'firebase/auth';
import axios from 'axios';
import { throttleer as grecaptcha } from 'vue-infinite-loading/src/utils';

export default {
  components: { VuePhoneNumberInput },
  data() {
    return {
      metaInfo: {
        title: 'Sign In',
      },
      mode: this.$route.query.mode,
      step: 1,
      steps: [
        { title: 'Enter Phone number' }, // search for the user
        { title: 'Add Farmer Details' }, // if not found or is in another table, accept terms too
        { title: 'Details of your business' }, // if not found and is buyer
        { title: 'Enter OTP sent to you phone' }, // if found from the db
      ],
      form: {
        name: '',
        password: '',
        phoneNumber: '',
        fullPhoneNumber: '',
        email: '',
      },
      ...validations,
      selectedCountry: 'KE',
      passwordField: 'password',
      passwordConfirmField: 'password',
      isValid1: false,
      isValid2: false,
      isValid3: false,
      isValid4: false,
      hasNotGivenPhoneNumber: true,
      show: false,
      isValid: false,
      loading: false,
      isSignInWithGoogle: false,
      isSignInWithPhone: true,
      isExistenceChecked: false,
      existAs: {
        farmer: false,
        buyer: false,
        exporter: false,
      },
      userIsCreatingSecondProfile: false,
      userMustBeSignedUp: false,
      otp: '',
      isOtpGiven: false,
      userRole: this.$route.query.mode === 'exporter' ? 'farmer' : getCurrentUserRole(),
      user: {
        uid: '',
        accessToken: '', // and other credentials too
      },
      farmMapDialog: false,
    };
  },
  mixins: [AuthMixins],
  computed: {
    isAuthenticated,
  },
  async mounted() {
    try {
      // await this.initAuth();
      // Check user role only after auth state is confirmed
      if (!this.userRole) {
        this.$router.push({ name: 'Home' });
      }
    } catch (e) {
      this.$toast.error(`Error: ${e.message}`);
    }
  },
  destroyed() {
    if (window.recaptchaVerifier) {
      window.recaptchaVerifier.clear();
    }
  },
  methods: {
    async checkRedirectResult() {
      const result = await getRedirectResult(auth);

      if (result) {
        this.isSignInWithGoogle = true;
        this.isSignInWithPhone = false;
        this.$toast.success(`Signed in as ${result.user.email}`);
        await this.handleGoogleUser(result);
      }
    },
    async signInWithGooglePopup() {
      try {
        this.isSignInWithPhone = false;
        this.isSignInWithGoogle = true;
        const googleAuthProvider = new GoogleAuthProvider();
        googleAuthProvider.setCustomParameters({ prompt: 'select_account' });
        // Explicitly await the redirect
        const result = await signInWithPopup(auth, googleAuthProvider);
        this.handleGoogleUser(result.user);
      } catch (e) {
        this.$toast.error(`Failed to start Google Sign-In: ${e.message}`);
      }
    },
    async signInWithGoogleRedirect() {
      try {
        this.isSignInWithPhone = false;
        this.isSignInWithGoogle = true;
        const googleAuthProvider = new GoogleAuthProvider();
        googleAuthProvider.setCustomParameters({ prompt: 'select_account' });
        // Explicitly await the redirect
        await signInWithRedirect(auth, googleAuthProvider);
      } catch (e) {
        this.$toast.error(`Failed to start Google Sign-In: ${e.message}`);
      }
    },
    handleGoogleUser(user) {
      // const credentials = GoogleAuthProvider.credentialFromResult(results);
      this.isSignInWithGoogle = true;
      this.isSignInWithPhone = false;
      this.user.uid = user.uid;
      this.user.accessToken = user.accessToken;
      this.form.name = user.displayName;
      this.form.email = user.email;
      this.checkFarmerOrBuyerExistence();
    },
    handlePhoneUser(user) {
      // const credentials = GoogleAuthProvider.credentialFromResult(results);
      this.isSignInWithPhone = true;
      this.isSignInWithGoogle = false;
      this.user.uid = user.uid;
      this.user.accessToken = user.accessToken;
      this.form.phoneNumber = user.phoneNumber;
      this.checkFarmerOrBuyerExistence();
    },
    continueStepOne() {
      this.isSignInWithPhone = true;
      this.isSignInWithGoogle = false;
      this.hasNotGivenPhoneNumber = !this.form.fullPhoneNumber || this.form.fullPhoneNumber.length < 4;
      this.steps[1].title = `Add ${this.userRole ? this.userRole[0].toUpperCase() + this.userRole.slice(1) : ''} Details`;
      this.checkFarmerOrBuyerExistence();
    },
    async continueStepTwo() {
      if (this.isSignInWithPhone) {
        if (this.mode === 'self') {
          this.goToOTP();
        } else {
          await this.login();
        }
      } else if (this.isSignInWithGoogle) {
        await this.login();
      }
    },
    checkFarmerOrBuyerExistence() {
      axios.get('/farmers-service/user/check-existence', {
        params: {
          phoneNumber: this.form.fullPhoneNumber,
          email: this.form.email,
        },
      })
        .then((response) => {
          if (response.data.success) {
            this.existAs = response.data.data;
            this.isExistenceChecked = true;
            // MATCHES
            if (
              (this.userRole.toLowerCase() === 'farmer' && this.existAs.farmer)
                || (this.userRole.toLowerCase() === 'buyer' && this.existAs.buyer)
                || (this.userRole.toLowerCase() === 'exporter' && this.existAs.exporter)
            ) {
              this.userMustBeSignedUp = false;
              if (this.isSignInWithPhone && this.mode === 'self') {
                this.goToOTP();
              }
              if (this.isSignInWithGoogle && this.mode === 'self') {
                this.login();
              }
            } else if ((this.userRole.toLowerCase() === 'farmer' && this.existAs.buyer)// SIGNING IN WITH DIFFERENT PROFILE
                || (this.userRole.toLowerCase() === 'buyer' && this.existAs.farmer)) {
              // add them to the usertype now then step 4
              this.userIsCreatingSecondProfile = true;
              this.userMustBeSignedUp = true;
              this.step = 2;
            } else { // NEEDS TO SIGN UP
              this.userMustBeSignedUp = true;
              this.step = 2;
            }
            // if (this.mode === 'exporter') {
            //   this.userMustBeSignedUp = this.existAs.farmer;
            //   this.step = 2;
            // }
          } else {
            this.$toast.error(response.data.msg);
          }
        });
    },
    goToOTP() {
      this.step = 4;
      this.sendOtp();
    },
    async sendOtp() {
      try {
        if (window.recaptchaVerifier) {
          window.recaptchaVerifier.clear(); // Removes the reCAPTCHA widget
          window.recaptchaVerifier = null; // Clean up reference
        }
        if (!window.recaptchaVerifier) {
          window.recaptchaVerifier = new RecaptchaVerifier(auth, 'recaptcha-container', {
            size: 'invisible',
            'expired-callback': () => {
              // Response expired. Ask user to solve reCAPTCHA again.
              this.$toast.error('Recaptcha expired');
            },
          });
        }
        const appVerifier = window.recaptchaVerifier;
        window.confirmationResult = await signInWithPhoneNumber(auth, this.form.fullPhoneNumber, appVerifier);
        this.$toast.success('SMS sent to your mobile number');
      } catch (e) {
        // Handle errors (e.g., invalid phone number, quota exceeded)
        this.$toast.error('SMS not sent', e.message);
        window.recaptchaVerifier.render().then((widgetId) => {
          grecaptcha.reset(widgetId);
        });
      }
    },
    async verifyOtp() {
      try {
        const confirmationResult = await window.confirmationResult.confirm(this.otp);
        this.user = {
          uid: confirmationResult.user.uid,
          accessToken: confirmationResult.user.accessToken,
        };
        this.$toast.success('OTP verified successfully');
        // Handle successful sign-in (e.g., redirect to home page)
        await this.login();
      } catch (error) {
        // Handle errors (e.g., invalid code)
        this.$toast.error(error.message);
      }
    },
    async login() {
      if (this.userMustBeSignedUp) {
        await this.signUpUser();
      }
      // fetching the users credentials and putting them into state
      const mode = this.mode;
      const zoneId = this.$route.query.zoneId;
      // const exporterId = this.$route.query.exporterId;
      const encodedRedirect = this.$route.query.r;
      const redirectPath = encodedRedirect ? atob(encodedRedirect) : '/';
      if (mode === 'self') {
        // get credentials and store them in the store
        let response = '';
        if (this.userRole === 'farmer' || this.userRole === 'buyer') {
          response = await axios.get(`/${this.userRole}s-service/${this.userRole}?${this.userRole}Id=${this.user.uid}`);
          if (response.data.data == null) {
            await this.signUpUser();
            const response2 = await axios.get(`/${this.userRole}s-service/${this.userRole}?${this.userRole}Id=${this.user.uid}`);
            if (response2.data.data == null) {
              this.$toast.error('user not found');
            }
          }
        } else if (this.userRole === 'exporter') {
          response = await axios.get(`/farmers-service/${this.userRole}/${this.user.uid}`);
          if (response.data.data == null) {
            await this.signUpUser();
            const response2 = await axios.get(`/${this.userRole}s-service/${this.userRole}?${this.userRole}Id=${this.user.uid}`);
            if (response2.data.data == null) {
              this.$toast.error('user not found');
            }
          }
        }
        this.user = { ...this.user, ...response.data.data };
        const user = this.user;
        await this.$store.dispatch('auth/signIn', {
          user,
        });
      }

      if (mode === 'exporter') {
        // i would like for you to add farmer above to zone with the following api
        //    Add Farmer to Zone
        //  - Endpoint: POST /farmers-service/exporter/zones/{zoneId}/farmers/{farmerId}
        //  - Creates relationship between farmer and zone
        if (zoneId) {
          try {
            await axios.post(`/farmers-service/exporter/zones/${zoneId}/farmers/${this.user.id}`);
            this.$toast.success('Farmer added to zone successfully');
          } catch (error) {
            this.$toast.error(`Failed to add farmer to zone: ${error.message}`);
          }
        } else {
          this.$toast.error('No zone specified for the exporter');
        }
        this.$router.push(redirectPath || { name: 'OperatingZonesManagement' });
      } else if (mode === 'self') {
        // Maybe redirect to a specific dashboard
        if (this.userRole === 'exporter') {
          this.$router.push({ name: 'OperatingZonesManagement' });
        } else {
          this.$router.push({ name: 'Dashboard' });
        }
      }

      this.$toast.success('Signed in successfully!', `${this.form.name}`);
    },
    // async onSubmit() {
    //   this.loading = true;
    //   await this.signInUser(this.form.phoneNumber, this.form.password)
    //     .then((user) => {
    //       this.loading = false;
    //       if (user != null) {
    //         this.$toast.success('Signed in successfully!', `${user.attributes.name}`);
    //         this.$router.push({ name: 'Dashboard' });
    //       } else {
    //         this.$toast.error('Login failed');
    //       }
    //     })
    //     .catch((err) => {
    //       this.loading = false;
    //       this.$toast.error(err.message);
    //     });
    // },
    toForgotPassword() {
      this.$router.push({
        name: 'ForgotPassword',
      });
    },
  },
};
</script>
<style scoped>
.background-animation {
  background-color: #f3ecec;
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.20;
}

.circle-1 {
  width: 500px;
  height: 500px;
  background: var(--v-primary-base);
  top: -100px;
  left: -100px;
  /* animation: circle-clockwise 15s infinite linear; */
}

.circle-2 {
  width: 400px;
  height: 400px;
  background: var(--v-secondary-base);
  bottom: -100px;
  right: -50px;
  /* animation: circle-clockwise 18s infinite linear; */
  /* animation-delay: 2s; */
}

.circle-3 {
  width: 300px;
  height: 300px;
  background: var(--v-accent-base);
  top: 50%;
  left: 30%;
  /* animation: circle-clockwise 12s infinite linear; */
  /* animation-delay: 4s; */
}

/* Clockwise circular motion */
@keyframes circle-clockwise {
  0% {
    transform: rotate(0deg) translateX(150px) rotate(0deg);
  }
  100% {
    transform: rotate(360deg) translateX(150px) rotate(-360deg);
  }
}

/* Anticlockwise circular motion */
@keyframes circle-anticlockwise {
  0% {
    transform: rotate(0deg) translateX(200px) rotate(0deg);
  }
  100% {
    transform: rotate(-360deg) translateX(200px) rotate(360deg);
  }
}
</style>
