<template>
    <v-app class="tw-p-8 tw-bg-white tw-rounded-2xl tw-max-w-7xl tw-mx-auto tw-mt-10">
        <div class="tw-flex tw-flex-col md:tw-flex-row tw-gap-8">
            <!-- Personal Details Section -->
            <div class="tw-flex-1 tw-flex tw-flex-col tw-gap-4">
                <h2 class="tw-text-lg tw-font-semibold tw-text-green-600">Personal Details</h2>
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
                    placeholder="e.g. Jane Doe"
                />
                <vue-phone-number-input
                    v-model="form.phoneNumber"
                    :default-country-code="'KE'"
                    :preferred-countries="['KE', 'US', 'GB']"
                    show-code-on-list
                    @update="onPhoneUpdate"
                    aria-label="Phone Number"
                    class="tw-w-full"
                    placeholder="e.g. +254712345678"
                />
                <v-text-field
                    v-model="form.email"
                    label="Email Address"
                    :outlined="false"
                    :filled="true"
                    dense
                    :rules="[emailFormat()]"
                    :hide-details="false"
                    class="modern-input tw-w-full"
                    aria-label="Email Address"
                    autocomplete="email"
                    placeholder="e.g. jane@email.com"
                />
            </div>
            <!-- Business Details Section -->
            <div class="tw-flex-1 tw-flex tw-flex-col tw-gap-4">
                <h2 class="tw-text-lg tw-font-semibold tw-text-green-600">Business Details</h2>
                <v-text-field
                    label="Business Name"
                    dense
                    v-model="form.businessName"
                    :outlined="false"
                    :filled="true"
                    :rules="[required('Business name'), noDigitFormat()]"
                    :hide-details="false"
                    class="modern-input tw-w-full"
                    aria-label="Business Name"
                    placeholder="e.g. Buyer Ltd"
                />
                <v-text-field
                    label="Business Location Name"
                    dense
                    v-model="form.location.customName"
                    :outlined="false"
                    :filled="true"
                    :rules="[required('Location name')]"
                    :hide-details="false"
                    class="modern-input tw-w-full"
                    aria-label="Business Location Name"
                    placeholder="e.g. Nairobi Central, Kiambu Region"
                />
                <v-text-field
                    label="Business Coordinates"
                    dense
                    :value="form.location.lat && form.location.lng ? `(${parseFloat(form.location.lat).toFixed(6)}, ${parseFloat(form.location.lng).toFixed(6)})` : ''"
                    append-icon="mdi-map-marker"
                    @click:append="openLocationPicker"
                    :rules="[required('Coordinates')]"
                    :hide-details="false"
                    class="modern-input tw-w-full"
                    aria-label="Business Coordinates"
                    placeholder="Click the map icon to select coordinates"
                    readonly
                />
            </div>
            <!-- Password Section -->
            <div class="tw-flex-1 tw-flex tw-flex-col tw-gap-4">
                <h2 class="tw-text-lg tw-font-semibold tw-text-green-600">Password</h2>
                <v-text-field
                    label="Password"
                    :type="showPassword ? 'text' : 'password'"
                    v-model="form.password"
                    :outlined="false"
                    :filled="true"
                    dense
                    :rules="[required('Password')]"
                    :hide-details="false"
                    class="modern-input tw-w-full"
                    aria-label="Password"
                    autocomplete="new-password"
                    placeholder="e.g. ********"
                    :append-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                    @click:append="showPassword = !showPassword"
                />
            </div>
        </div>
        <!-- Preferred Produces Section -->
        <div class="tw-mt-8">
            <h2 class="tw-text-lg tw-font-semibold tw-text-green-600">Preferred Produces</h2>
            <p class="tw-text-sm tw-text-gray-600 tw-mb-4">Select or enter the types of farm produces you are interested in buying</p>
            <AddBuyerProduce v-model="form.selectedProduces" />
        </div>
        <!-- Terms and Button Section -->
        <div class="tw-mt-8">
          <h2 class="tw-text-lg tw-font-semibold">Terms and Conditions</h2>
          <p>I hereby Confirm that the information set is correct and accurate</p>
          <v-checkbox
              id="checkbox"
              dense
              v-model="form.terms"
              :rules="[check()]"
              :color="'primary'"
              :hide-details="false"
              aria-checked="form.terms"
              aria-label="Agree to Terms and Conditions"
              :label="`I agree to the Terms and Conditions`"
          />
          <a
            class="tw-text-blue-600 hover:tw-text-blue-800 tw-underline tw-ml-2"
            href="#"
            @click.prevent="openTermsDialog"
            aria-label="Open Terms and Conditions"
          >
            Terms and Conditions
          </a>
        </div>
        <div class="tw-flex tw-justify-end tw-my-2">
            <v-btn
                color="primary"
                class="tw-px-8 tw-py-3 tw-rounded-xl tw-font-semibold tw-text-lg tw-shadow"
                @click="signUpUser"
                aria-label="Sign Up"
            >
              Sign Up
            </v-btn>
        </div>
        <LocationPicker
          v-model="locationPickerDialog"
          :initial-location="form.location.lat && form.location.lng ? { lat: parseFloat(form.location.lat), lng: parseFloat(form.location.lng) } : null"
          @location-selected="handleLocationSelected"
        />
        <terms-and-conditions ref="termsDialog"/>
    </v-app>
</template>
<script>
// add default and phone number prop here
import axios from 'axios';
import VuePhoneNumberInput from 'vue-phone-number-input';
import TermsAndConditions from '@/components/auth/TermsAndConditions.vue';
import LocationPicker from '@/components/LocationPicker.vue';
import AddBuyerProduce from '@/components/produce/AddBuyerProduce.vue';
import validations from '@/utils/validations.js';

export default {
  components: {
    TermsAndConditions,
    VuePhoneNumberInput,
    LocationPicker,
    AddBuyerProduce,
  },
  data() {
    return {
      form: {
        name: '',
        phoneNumber: '',
        email: '',
        businessName: '',
        password: '', // Added password field
        licenseId: '',
        terms: false,
        fullPhoneNumber: '',
        location: {
          customName: '',
          lat: '',
          lng: '',
        },
        selectedProduces: [],
      },
      ...validations,
      mode: 'exporter', // or 'farmer' based on your logic
      showPassword: false,
      locationPickerDialog: false,
    };
  },
  mounted() {
    // Produce loading is now handled by AddBuyerProduce component
  },
  methods: {
    async signUpUser() {
      try {
        // Validate that at least one produce is selected
        if (!this.form.selectedProduces || this.form.selectedProduces.length === 0) {
          this.$toast.error('Please select at least one preferred produce');
          return;
        }

        // Process preferred produces - send the selected produce names
        // The backend will handle creating buyer-specific produces if needed
        const preferredProduces = this.form.selectedProduces.map((produceName) => ({
          name: produceName,
        }));

        const userPayload = {
          user: {
            fullName: this.form.name,
            email: this.form.email,
            phoneNumber: this.form.fullPhoneNumber,
            password: this.form.password,
          },
          companyName: this.form.businessName,
          businessType: '',
          location: {
            latitude: parseFloat(this.form.location.lat) || 0,
            longitude: parseFloat(this.form.location.lng) || 0,
            customName: this.form.location.customName,
          },
          preferredProduces,
        };

        const saveResponse = await axios.post('/api/auth/register/buyer', userPayload);
        if (saveResponse.data.success === true) {
          this.user = { ...this.user, ...saveResponse.data.data };
          this.$router.push({ name: 'SignIn' });
          this.$toast.success('Buyer account created successfully! Please sign in.');
        } else {
          this.$toast.error(saveResponse.data.msg, 'Error');
        }
      } catch (error) {
        this.$toast.error('Error creating account. Please try again.');
      }
    },
    onPhoneUpdate(phoneData) {
      // Get the full international number with country code
      this.form.fullPhoneNumber = phoneData.e164 || phoneData.formatInternational;
    },
    openTermsDialog() {
      this.$refs.termsDialog.openDialog();
    },
    openLocationPicker() {
      this.locationPickerDialog = true;
    },
    handleLocationSelected(location) {
      this.form.location.lat = location.lat.toFixed(6);
      this.form.location.lng = location.lng.toFixed(6);
      this.locationPickerDialog = false;
      this.$toast.success('Coordinates selected successfully');
    },
  },
};
</script>
<style>
/* ...existing code... */
</style>
