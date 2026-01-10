<template>
  <v-app class="tw-p-8 tw-bg-white tw-rounded-2xl tw-max-w-7xl tw-mx-auto tw-mt-10">
    <div class="tw-flex tw-flex-col md:tw-flex-row tw-gap-8">
      <!-- Personal Details Section -->
      <div class="tw-flex-1 tw-flex tw-flex-col tw-gap-4">
        <h2 class="tw-text-lg tw-font-semibold tw-text-indigo-600">Personal Details</h2>
        <v-text-field
          label="Full Name"
          :outlined="false"
          :filled="true"
          dense
          v-model="form.name"
          :rules="[required('Name'), noDigitFormat()]"
          :hide-details="false"
          class="modern-input tw-w-full"
          aria-label="Full Name"
          autocomplete="name"
          placeholder="e.g. John Doe"
        />
        <vue-phone-number-input
          v-model="form.phoneNumber"
          :default-country-code="defaultCountryCode"
          :preferred-countries="['DE', 'FR', 'NL', 'BE', 'IT', 'ES', 'PL']"
          show-code-on-list
          @update="onPhoneUpdate"
          aria-label="Phone Number"
          class="tw-w-full"
          placeholder="e.g. +49 30 12345678"
        />
        <v-text-field
          v-model="form.email"
          label="Email Address"
          :outlined="false"
          :filled="true"
          dense
          :rules="[required('Email'), emailFormat()]"
          :hide-details="false"
          class="modern-input tw-w-full"
          aria-label="Email Address"
          autocomplete="email"
          placeholder="e.g. john@company.eu"
        />
      </div>

      <!-- EU Business Details Section -->
      <div class="tw-flex-1 tw-flex tw-flex-col tw-gap-4">
        <h2 class="tw-text-lg tw-font-semibold tw-text-indigo-600">EU Business Details</h2>

        <!-- EORI Number - Critical for EU TRACES access -->
        <v-text-field
          label="EORI Number"
          v-model="form.eoriNumber"
          :outlined="false"
          :filled="true"
          dense
          :rules="[required('EORI Number'), eoriFormat()]"
          :hide-details="false"
          class="modern-input tw-w-full"
          aria-label="EORI Number"
          placeholder="e.g. DE123456789012345"
          hint="Economic Operators Registration and Identification number (required for EU customs)"
          persistent-hint
          @input="formatEoriNumber"
        />

        <v-text-field
          label="Company Name"
          dense
          v-model="form.companyName"
          :outlined="false"
          :filled="true"
          :rules="[required('Company name')]"
          :hide-details="false"
          class="modern-input tw-w-full"
          aria-label="Company Name"
          placeholder="e.g. AgriConnect GmbH"
        />

        <!-- EU Member State Selection -->
        <v-autocomplete
          v-model="form.euMemberState"
          :items="euMemberStates"
          item-text="name"
          item-value="code"
          label="EU Member State (Establishment)"
          :outlined="false"
          :filled="true"
          dense
          :rules="[required('EU Member State')]"
          :hide-details="false"
          class="modern-input tw-w-full"
          aria-label="EU Member State"
          @change="onMemberStateChange"
        >
          <template #item="{ item }">
            <div class="tw-flex tw-items-center tw-gap-2">
              <span class="tw-text-xl">{{ item.flag }}</span>
              <span>{{ item.name }}</span>
              <span class="tw-text-gray-400 tw-text-sm">({{ item.code }})</span>
            </div>
          </template>
          <template #selection="{ item }">
            <div class="tw-flex tw-items-center tw-gap-2">
              <span class="tw-text-xl">{{ item.flag }}</span>
              <span>{{ item.name }}</span>
            </div>
          </template>
        </v-autocomplete>

        <v-text-field
          label="Business Registration Number"
          dense
          v-model="form.registrationNumber"
          :outlined="false"
          :filled="true"
          :rules="[required('Registration number')]"
          :hide-details="false"
          class="modern-input tw-w-full"
          aria-label="Business Registration Number"
          placeholder="e.g. HRB 123456"
        />

        <v-text-field
          label="VAT Number (Optional)"
          dense
          v-model="form.vatNumber"
          :outlined="false"
          :filled="true"
          :hide-details="false"
          class="modern-input tw-w-full"
          aria-label="VAT Number"
          placeholder="e.g. DE123456789"
        />
      </div>

      <!-- Business Address & Password Section -->
      <div class="tw-flex-1 tw-flex tw-flex-col tw-gap-4">
        <h2 class="tw-text-lg tw-font-semibold tw-text-indigo-600">Address & Security</h2>

        <v-textarea
          label="Business Address"
          dense
          v-model="form.businessAddress"
          :outlined="false"
          :filled="true"
          :rules="[required('Business address')]"
          :hide-details="false"
          class="modern-input tw-w-full"
          rows="3"
          aria-label="Business Address"
          placeholder="Full registered business address in the EU"
        />

        <v-text-field
          label="Password"
          :type="showPassword ? 'text' : 'password'"
          v-model="form.password"
          :outlined="false"
          :filled="true"
          dense
          :rules="[required('Password'), minLength(8)]"
          :hide-details="false"
          class="modern-input tw-w-full"
          aria-label="Password"
          autocomplete="new-password"
          placeholder="Minimum 8 characters"
          :append-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
          @click:append="showPassword = !showPassword"
        />

        <v-text-field
          label="Confirm Password"
          :type="showConfirmPassword ? 'text' : 'password'"
          v-model="form.confirmPassword"
          :outlined="false"
          :filled="true"
          dense
          :rules="[required('Confirm password'), matchPassword()]"
          :hide-details="false"
          class="modern-input tw-w-full"
          aria-label="Confirm Password"
          autocomplete="new-password"
          :append-icon="showConfirmPassword ? 'mdi-eye-off' : 'mdi-eye'"
          @click:append="showConfirmPassword = !showConfirmPassword"
        />
      </div>
    </div>

    <!-- EUDR Article 6 Info Banner -->
    <v-alert
      type="info"
      dense
      text
      class="tw-mt-6"
      icon="mdi-information"
    >
      <strong>EUDR Article 6 Compliance</strong><br>
      As an Authorised Representative, you will be able to submit Due Diligence Statements
      on behalf of non-EU operators and access EU TRACES for commodity verification.
      Your EORI number is required for EU customs integration.
    </v-alert>

    <!-- Terms and Button Section -->
    <div class="tw-mt-6">
      <h2 class="tw-text-lg tw-font-semibold">Terms and Conditions</h2>
      <p class="tw-text-gray-600 tw-mb-2">
        By registering as an Authorised Representative, you confirm that:
      </p>
      <ul class="tw-list-disc tw-ml-6 tw-text-gray-600 tw-mb-4">
        <li>You are established in an EU Member State</li>
        <li>Your EORI number is valid and active</li>
        <li>You accept responsibility for DDS submissions on behalf of operators</li>
        <li>You will maintain accurate records as required by EUDR</li>
      </ul>

      <v-checkbox
        id="checkbox"
        dense
        v-model="form.terms"
        :rules="[check()]"
        :color="'primary'"
        :hide-details="false"
        aria-checked="form.terms"
        aria-label="Agree to Terms and Conditions"
        :label="`I agree to the Terms and Conditions and EUDR compliance requirements`"
      />
      <a
        class="tw-text-blue-600 hover:tw-text-blue-800 tw-underline tw-ml-2"
        href="#"
        @click.prevent="openTermsDialog"
        aria-label="Open Terms and Conditions"
      >
        View Terms and Conditions
      </a>
    </div>

    <div class="tw-flex tw-justify-end tw-my-4">
      <v-btn
        color="indigo"
        class="tw-px-8 tw-py-3 tw-rounded-xl tw-font-semibold tw-text-lg tw-shadow"
        :loading="loading"
        :disabled="!isFormValid"
        @click="signUpUser"
        aria-label="Register as Authorised Representative"
      >
        <v-icon left>mdi-account-check</v-icon>
        Register as Authorised Representative
      </v-btn>
    </div>

    <terms-and-conditions ref="termsDialog" />
  </v-app>
</template>

<script>
import axios from 'axios';
import VuePhoneNumberInput from 'vue-phone-number-input';
import TermsAndConditions from '@/components/auth/TermsAndConditions.vue';
import validations from '@/utils/validations.js';
import authorisedRepresentativeService from '@/services/authorisedRepresentativeService';

export default {
  name: 'AuthorisedRepresentativeSignUp',
  components: {
    TermsAndConditions,
    VuePhoneNumberInput,
  },

  data() {
    return {
      form: {
        name: '',
        phoneNumber: '',
        email: '',
        eoriNumber: '',
        companyName: '',
        euMemberState: '',
        registrationNumber: '',
        vatNumber: '',
        businessAddress: '',
        password: '',
        confirmPassword: '',
        terms: false,
        fullPhoneNumber: '',
      },
      ...validations,
      showPassword: false,
      showConfirmPassword: false,
      loading: false,
      euMemberStates: authorisedRepresentativeService.EU_MEMBER_STATES,
    };
  },

  computed: {
    isFormValid() {
      return this.form.name
        && this.form.email
        && this.form.eoriNumber
        && this.form.companyName
        && this.form.euMemberState
        && this.form.registrationNumber
        && this.form.businessAddress
        && this.form.password
        && this.form.password === this.form.confirmPassword
        && this.form.terms
        && this.isEoriValid;
    },

    isEoriValid() {
      const eoriRegex = /^[A-Z]{2}[A-Z0-9]{1,15}$/;
      return eoriRegex.test(this.form.eoriNumber);
    },

    defaultCountryCode() {
      // Map EU member state to phone country code - same code applies
      return this.form.euMemberState || 'DE';
    },
  },

  methods: {
    /**
     * Format EORI number to uppercase
     */
    formatEoriNumber() {
      this.form.eoriNumber = this.form.eoriNumber.toUpperCase().replace(/[^A-Z0-9]/g, '');
    },

    /**
     * Update phone number when member state changes
     */
    onMemberStateChange(stateCode) {
      // Auto-populate EORI country prefix if empty
      if (!this.form.eoriNumber || this.form.eoriNumber.length < 2) {
        this.form.eoriNumber = stateCode;
      }
    },

    /**
     * EORI format validation rule
     */
    eoriFormat() {
      return (v) => {
        if (!v) return 'EORI number is required';
        const eoriRegex = /^[A-Z]{2}[A-Z0-9]{1,15}$/;
        if (!eoriRegex.test(v)) {
          return 'Invalid EORI format. Must be 2-letter country code followed by 1-15 alphanumeric characters (e.g., DE123456789012345)';
        }
        // Validate country code is an EU member state
        const countryCode = v.substring(0, 2);
        const validCodes = this.euMemberStates.map((s) => s.code);
        if (!validCodes.includes(countryCode)) {
          return `Country code ${countryCode} is not a valid EU Member State`;
        }
        return true;
      };
    },

    /**
     * Minimum length validation rule
     */
    minLength(min) {
      return (v) => (v && v.length >= min) || `Minimum ${min} characters required`;
    },

    /**
     * Password match validation rule
     */
    matchPassword() {
      return (v) => v === this.form.password || 'Passwords do not match';
    },

    /**
     * Handle phone number update from VuePhoneNumberInput
     */
    onPhoneUpdate(phoneData) {
      this.form.fullPhoneNumber = phoneData.e164 || phoneData.formatInternational;
    },

    /**
     * Open terms and conditions dialog
     */
    openTermsDialog() {
      this.$refs.termsDialog.openDialog();
    },

    /**
     * Submit registration
     */
    async signUpUser() {
      if (!this.form.terms) {
        this.$toast.error('Please accept the Terms and Conditions');
        return;
      }

      if (!this.isEoriValid) {
        this.$toast.error('Please enter a valid EORI number');
        return;
      }

      this.loading = true;
      try {
        const payload = {
          user: {
            fullName: this.form.name,
            email: this.form.email,
            phoneNumber: this.form.fullPhoneNumber || this.form.phoneNumber,
            password: this.form.password,
          },
          eoriNumber: this.form.eoriNumber,
          companyName: this.form.companyName,
          euMemberState: this.form.euMemberState,
          registrationNumber: this.form.registrationNumber,
          vatNumber: this.form.vatNumber || null,
          contactPhone: this.form.fullPhoneNumber || this.form.phoneNumber,
          businessAddress: this.form.businessAddress,
        };

        const response = await axios.post('/api/auth/register/authorised-representative', payload);

        if (response.data.success === true) {
          this.$emit('ar-registered');
          this.$toast.success(
            'Authorised Representative account created successfully! Your account is pending verification. Please sign in.',
          );
        } else {
          this.$toast.error(response.data.msg || 'Registration failed', 'Error');
        }
      } catch (error) {
        const errorMessage = error.response?.data?.message
          || error.response?.data?.msg
          || 'Error creating account. Please check your details and try again.';
        this.$toast.error(errorMessage);
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.modern-input {
  border-radius: 12px;
}
</style>
