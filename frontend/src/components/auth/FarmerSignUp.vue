<template>
  <v-app>
    <v-container class="tw-max-w-7xl">
      <div class="tw-flex tw-flex-col md:tw-flex-row tw-gap-8 tw-border-3">
          <!-- Personal Details Section -->
          <div class="tw-flex-1 tw-flex tw-flex-col tw-gap-4">
            <h2 class="tw-text-lg tw-text-green-600 tw-font-semibold">Personal Details</h2>
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
          <!-- Farm Details Section -->
          <div class="tw-flex-1 tw-flex tw-flex-col tw-gap-4">
              <h2 class="tw-text-lg tw-text-green-600 tw-font-semibold">Farm Details</h2>
              <v-text-field
                  label="Farm Name"
                  dense
                  v-model="form.farmName"
                  :outlined="false"
                  :filled="true"
                  :rules="[required('Farm name'), noDigitFormat()]"
                  :hide-details="false"
                  class="modern-input tw-w-full"
                  aria-label="Farm Name"
                  placeholder="e.g. Green Acres"
              />
              <v-text-field
                  label="Farm Size (acres)"
                  :outlined="false"
                  :filled="true"
                  dense
                  append-icon="mdi-map"
                  @click:append="farmMapDialog = true"
                  v-model="form.farmSize"
                  :rules="[required('Farm size')]"
                  :hide-details="false"
                  class="modern-input tw-w-full"
                  aria-label="Farm Size"
                  placeholder="e.g. 2.5"
              />
              <v-text-field
                  label="Farm Location Name"
                  dense
                  v-model="form.location.customName"
                  :outlined="false"
                  :filled="true"
                  :rules="[required('Location name')]"
                  :hide-details="false"
                  class="modern-input tw-w-full"
                  aria-label="Farm Location Name"
                  placeholder="e.g. Nairobi Central, Kiambu Region"
              />
              <v-text-field
                  label="Farm Coordinates"
                  dense
                  :value="form.location.lat && form.location.lng ? `(${parseFloat(form.location.lat).toFixed(6)}, ${parseFloat(form.location.lng).toFixed(6)})` : ''"
                  append-icon="mdi-map-marker"
                  @click:append="openLocationPicker"
                  :rules="[required('Coordinates')]"
                  :hide-details="false"
                  class="modern-input tw-w-full"
                  aria-label="Farm Coordinates"
                  placeholder="Click the map icon to select coordinates"
                  readonly
              />
          </div>
          <!-- Password Section -->
          <div class="tw-flex-1 tw-flex tw-flex-col tw-gap-4">
              <h2 class="tw-text-lg tw-text-green-600 tw-font-semibold">Password</h2>
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
      <!-- Terms and Button Section -->
      <div class="tw-mt-8">
        <h2 class="tw-text-lg tw-text-green-600 tw-font-semibold">Terms and Conditions</h2>
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
      <div class="tw-flex tw-justify-center tw-mt-4">
          <v-btn
              color="primary"
              class="tw-py-3 tw-px-10 tw-rounded-xl tw-text-lg tw-font-semibold tw-shadow-lg"
              @click="signUpUser"
              aria-label="Sign Up"
          >
              Sign Up
          </v-btn>
      </div>
      <v-dialog v-model="farmMapDialog" aria-modal="true" role="dialog">
          <FarmMap
              @farm-boundary-updated="handleFarmBoundaryUpdate"
              @farm-boundary-created="handleFarmBoundaryUpdate"
          />
      </v-dialog>
      <LocationPicker
        v-model="locationPickerDialog"
        :initial-location="form.location.lat && form.location.lng ? { lat: parseFloat(form.location.lat), lng: parseFloat(form.location.lng) } : null"
        @location-selected="handleLocationSelected"
      />
      <terms-and-conditions ref="termsDialog"/>
    </v-container>
  </v-app>
</template>
<script>
import axios from 'axios';
import VuePhoneNumberInput from 'vue-phone-number-input';
import TermsAndConditions from '@/components/auth/TermsAndConditions.vue';
import FarmMap from '@/components/FarmMap.vue';
import LocationPicker from '@/components/LocationPicker.vue';
import validations from '@/utils/validations.js';

export default {
  components: {
    FarmMap, LocationPicker, TermsAndConditions, VuePhoneNumberInput,
  },
  props: {
    phoneNumber: {
      type: String,
      default: '',
    },
  },
  data() {
    return {
      form: {
        name: '',
        phoneNumber: '',
        email: '',
        password: '',
        farmName: '',
        farmSize: '',
        location: {
          customName: '',
          lat: '',
          lng: '',
        },
        terms: false,
        fullPhoneNumber: '',
      },
      userRole: 'farmer',
      farmMapDialog: false,
      locationPickerDialog: false,
      showPassword: false,
      ...validations,
    };
  },
  watch: {
    // Watch for changes in the custom location name - no longer parsing coordinates
  },
  methods: {
    // Handles user sign up API call
    async signUpUser() {
      try {
        const userPayload = {
          user: {
            fullName: this.form.name,
            email: this.form.email,
            phoneNumber: this.form.fullPhoneNumber,
            password: this.form.password,
          },
          farmName: this.form.farmName,
          farmSize: parseFloat(this.form.farmSize.replace('acres', '').replace('acre', '').trim()) || 0,
          location: {
            latitude: parseFloat(this.form.location.lat) || 0,
            longitude: parseFloat(this.form.location.lng) || 0,
            customName: this.form.location.customName,
          },
          farmerProduces: [],
        };
        const saveResponse = await axios.post('/api/auth/register/farmer', userPayload);
        if (saveResponse.data.success === true) {
          this.user = { ...this.user, ...saveResponse.data.data };
          // Emit farmer-registered event with relevant info
          this.$emit('farmer-registered', {
            id: saveResponse.data.data.id,
            name: this.form.name,
            phoneNumber: this.form.fullPhoneNumber,
            email: this.form.email,
            farmName: this.form.farmName,
            farmSize: this.form.farmSize,
            location: { ...this.form.location },
          });
          this.$toast.success(`${this.userRole} profile set up`, 'success');
        } else {
          this.$toast.error(saveResponse.data.msg, 'Error');
        }
      } catch (error) {
        this.$toast.error('Error signing up user:', error.message);
      }
    },
    // Uses browser geolocation to get user's current location
    getUserLocation() {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.form.location.lat = position.coords.latitude;
          this.form.location.lng = position.coords.longitude;
          this.form.location.customName = `(${this.form.location.lat.toFixed(4)}, ${this.form.location.lng.toFixed(4)})`;
        },
      );
    },
    // Updates phone number to international format
    onPhoneUpdate(phoneData) {
      this.form.fullPhoneNumber = phoneData.e164 || phoneData.formatInternational;
    },
    openTermsDialog() {
      this.$refs.termsDialog.openDialog();
    },
    // Handles farm boundary update from map dialog
    handleFarmBoundaryUpdate(payload) {
      // eslint-disable-next-line no-unused-vars
      const { geometry, area, location } = payload;
      this.form.farmSize = `${area.toFixed(5)} acres`;
      this.form.location.lat = location.lat.toFixed(4);
      this.form.location.lng = location.lng.toFixed(4);
      // Don't overwrite customName as it's now for the location name
      this.farmMapDialog = false;
      this.$toast.success('Farm size received as', area.toString());
    },
    // Opens the location picker dialog
    openLocationPicker() {
      this.locationPickerDialog = true;
    },
    // Handles location selected from the location picker
    handleLocationSelected(location) {
      this.form.location.lat = location.lat.toFixed(6);
      this.form.location.lng = location.lng.toFixed(6);
      // Don't overwrite customName as it's now for the location name
      this.$toast.success('Coordinates selected successfully');
    },
  },
};
</script>
<style>
/* ...existing code... */
</style>
