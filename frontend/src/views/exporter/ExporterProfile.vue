<template>
  <Default>
    <div class="tw-max-w-4xl tw-mx-auto tw-p-6">
      <!-- Profile Header -->
      <div class="tw-flex tw-items-center tw-justify-between tw-mb-8">
        <div>
          <h1 class="tw-text-3xl tw-font-bold tw-text-gray-900">Company Profile</h1>
          <p class="tw-text-gray-500 tw-mt-1">Manage your company information and EUDR compliance settings</p>
        </div>
        <v-chip
          :color="getVerificationColor(profile.verificationStatus)"
          text-color="white"
          class="tw-px-4"
        >
          <v-icon left small>{{ getVerificationIcon(profile.verificationStatus) }}</v-icon>
          {{ profile.verificationStatus || 'PENDING' }}
        </v-chip>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="tw-flex tw-justify-center tw-py-20">
        <v-progress-circular indeterminate color="primary" size="48"></v-progress-circular>
      </div>

      <!-- Profile Form -->
      <v-form v-else ref="profileForm" v-model="formValid">
        <div class="tw-grid tw-grid-cols-1 lg:tw-grid-cols-2 tw-gap-6">
          <!-- Company Information Card -->
          <v-card class="tw-rounded-xl">
            <v-card-title class="tw-flex tw-items-center tw-gap-2 tw-border-b tw-pb-4">
              <v-icon color="primary">mdi-domain</v-icon>
              <span>Company Information</span>
            </v-card-title>
            <v-card-text class="tw-pt-6">
              <v-text-field
                v-model="profile.companyName"
                label="Company Name"
                outlined
                dense
                prepend-inner-icon="mdi-office-building"
                :rules="[rules.required]"
              ></v-text-field>

              <v-textarea
                v-model="profile.companyDesc"
                label="Company Description"
                outlined
                dense
                rows="3"
                prepend-inner-icon="mdi-text"
              ></v-textarea>

              <v-text-field
                v-model="profile.licenseId"
                label="Export License Number"
                outlined
                dense
                readonly
                prepend-inner-icon="mdi-license"
                hint="Contact support to update license number"
                persistent-hint
              ></v-text-field>
            </v-card-text>
          </v-card>

          <!-- EUDR Compliance Settings Card -->
          <v-card class="tw-rounded-xl">
            <v-card-title class="tw-flex tw-items-center tw-gap-2 tw-border-b tw-pb-4">
              <v-icon color="green">mdi-leaf</v-icon>
              <span>EUDR Compliance Settings</span>
            </v-card-title>
            <v-card-text class="tw-pt-6">
              <v-alert type="info" dense text class="tw-mb-4">
                <strong>Origin Country</strong> is required for EUDR compliance and will appear on your Due Diligence Statements.
              </v-alert>

              <v-autocomplete
                v-model="profile.originCountry"
                :items="countries"
                item-text="name"
                item-value="name"
                label="Primary Country of Origin"
                outlined
                dense
                prepend-inner-icon="mdi-map-marker"
                :rules="[rules.required]"
                :loading="loadingCountries"
                :disabled="loadingCountries"
                @change="onCountryChange"
              >
                <template #item="{ item }">
                  <div class="tw-flex tw-items-center tw-gap-2">
                    <img v-if="item.flagUrl" :src="item.flagUrl" alt="" class="tw-w-6 tw-h-4 tw-object-cover tw-rounded-sm" />
                    <span v-else class="tw-text-xl">{{ item.flag }}</span>
                    <span>{{ item.name }}</span>
                    <span class="tw-text-gray-400 tw-text-sm">({{ item.code }})</span>
                  </div>
                </template>
                <template #selection="{ item }">
                  <div class="tw-flex tw-items-center tw-gap-2">
                    <img v-if="item.flagUrl" :src="item.flagUrl" alt="" class="tw-w-6 tw-h-4 tw-object-cover tw-rounded-sm" />
                    <span v-else class="tw-text-xl">{{ item.flag }}</span>
                    <span>{{ item.name }}</span>
                  </div>
                </template>
              </v-autocomplete>

              <v-text-field
                v-model="profile.originCountryCode"
                label="Country Code (ISO 3166-1 alpha-3)"
                outlined
                dense
                readonly
                prepend-inner-icon="mdi-earth"
                hint="Automatically set based on selected country"
                persistent-hint
              ></v-text-field>
            </v-card-text>
          </v-card>

          <!-- Contact Information Card -->
          <v-card class="tw-rounded-xl">
            <v-card-title class="tw-flex tw-items-center tw-gap-2 tw-border-b tw-pb-4">
              <v-icon color="blue">mdi-account</v-icon>
              <span>Contact Information</span>
            </v-card-title>
            <v-card-text class="tw-pt-6">
              <v-text-field
                v-model="profile.name"
                label="Full Name"
                outlined
                dense
                prepend-inner-icon="mdi-account"
                :rules="[rules.required]"
              ></v-text-field>

              <v-text-field
                v-model="profile.email"
                label="Email Address"
                outlined
                dense
                prepend-inner-icon="mdi-email"
                :rules="[rules.required, rules.email]"
              ></v-text-field>

              <v-text-field
                v-model="profile.phoneNumber"
                label="Phone Number"
                outlined
                dense
                prepend-inner-icon="mdi-phone"
              ></v-text-field>
            </v-card-text>
          </v-card>

          <!-- Account Status Card -->
          <v-card class="tw-rounded-xl">
            <v-card-title class="tw-flex tw-items-center tw-gap-2 tw-border-b tw-pb-4">
              <v-icon color="orange">mdi-shield-check</v-icon>
              <span>Account Status</span>
            </v-card-title>
            <v-card-text class="tw-pt-6">
              <div class="tw-space-y-4">
                <div class="tw-flex tw-justify-between tw-items-center tw-p-3 tw-bg-gray-50 tw-rounded-lg">
                  <span class="tw-text-gray-600">Verification Status</span>
                  <v-chip :color="getVerificationColor(profile.verificationStatus)" small text-color="white">
                    {{ profile.verificationStatus || 'PENDING' }}
                  </v-chip>
                </div>

                <div class="tw-flex tw-justify-between tw-items-center tw-p-3 tw-bg-gray-50 tw-rounded-lg">
                  <span class="tw-text-gray-600">Export License</span>
                  <v-chip :color="profile.exportLicenseFormUrl ? 'success' : 'grey'" small text-color="white">
                    {{ profile.exportLicenseFormUrl ? 'Uploaded' : 'Not Uploaded' }}
                  </v-chip>
                </div>

                <div class="tw-flex tw-justify-between tw-items-center tw-p-3 tw-bg-gray-50 tw-rounded-lg">
                  <span class="tw-text-gray-600">Origin Country</span>
                  <v-chip :color="profile.originCountry ? 'success' : 'warning'" small text-color="white">
                    {{ profile.originCountry || 'Not Set' }}
                  </v-chip>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </div>

        <!-- Save Button -->
        <div class="tw-flex tw-justify-end tw-mt-6">
          <v-btn
            color="primary"
            large
            :loading="saving"
            :disabled="!formValid"
            @click="saveProfile"
            class="tw-px-8"
          >
            <v-icon left>mdi-content-save</v-icon>
            Save Changes
          </v-btn>
        </div>
      </v-form>

      <!-- Snackbar -->
      <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="4000" top right>
        {{ snackbar.message }}
      </v-snackbar>
    </div>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';

export default {
  name: 'ExporterProfile',
  components: { Default },

  data() {
    return {
      loading: true,
      saving: false,
      formValid: false,
      loadingCountries: false,

      profile: {
        id: '',
        companyName: '',
        companyDesc: '',
        licenseId: '',
        verificationStatus: 'PENDING',
        exportLicenseFormUrl: '',
        originCountry: '',
        originCountryCode: '',
        name: '',
        email: '',
        phoneNumber: '',
      },

      // Countries loaded from REST Countries API
      countries: [],

      rules: {
        required: (v) => !!v || 'This field is required',
        email: (v) => /.+@.+\..+/.test(v) || 'Please enter a valid email',
      },

      snackbar: {
        show: false,
        message: '',
        color: 'success',
      },
    };
  },

  computed: {
    exporterId() {
      return this.$store.state.auth?.user?.id || localStorage.getItem('userId');
    },
  },

  async mounted() {
    await Promise.all([
      this.loadCountries(),
      this.loadProfile(),
    ]);
  },

  methods: {
    async loadCountries() {
      this.loadingCountries = true;
      try {
        const response = await axios.get('https://restcountries.com/v3.1/all?fields=name,cca3,flags');
        this.countries = response.data
          .map((country) => ({
            name: country.name.common,
            code: country.cca3,
            flag: country.flags?.emoji || '',
            flagUrl: country.flags?.png || '',
          }))
          .sort((a, b) => a.name.localeCompare(b.name));
      } catch (error) {
        this.$toast.error('Failed to load countries:', error.message);
        // Fallback to common commodity-exporting countries
        this.countries = [
          { name: 'Kenya', code: 'KEN', flag: '🇰🇪' },
          { name: 'Uganda', code: 'UGA', flag: '🇺🇬' },
          { name: 'Tanzania', code: 'TZA', flag: '🇹🇿' },
          { name: 'Ethiopia', code: 'ETH', flag: '🇪🇹' },
          { name: 'Rwanda', code: 'RWA', flag: '🇷🇼' },
          { name: 'Brazil', code: 'BRA', flag: '🇧🇷' },
          { name: 'Colombia', code: 'COL', flag: '🇨🇴' },
          { name: 'Indonesia', code: 'IDN', flag: '🇮🇩' },
          { name: 'Vietnam', code: 'VNM', flag: '🇻🇳' },
        ];
      } finally {
        this.loadingCountries = false;
      }
    },

    async loadProfile() {
      this.loading = true;
      try {
        // Load exporter profile
        const response = await axios.get(`/api/exporters-service/exporter/${this.exporterId}`);
        const data = response.data?.data || response.data;

        this.profile = {
          id: data.id || this.exporterId,
          companyName: data.companyName || '',
          companyDesc: data.companyDesc || '',
          licenseId: data.licenseId || '',
          verificationStatus: data.verificationStatus || 'PENDING',
          exportLicenseFormUrl: data.exportLicenseFormUrl || '',
          originCountry: data.originCountry || '',
          originCountryCode: data.originCountryCode || '',
          name: data.name || data.fullName || '',
          email: data.email || '',
          phoneNumber: data.phoneNumber || '',
        };
      } catch (error) {
        this.showSnackbar('Failed to load profile', 'error');
      } finally {
        this.loading = false;
      }
    },

    onCountryChange(countryName) {
      const country = this.countries.find((c) => c.name === countryName);
      if (country) {
        this.profile.originCountryCode = country.code;
      }
    },

    async saveProfile() {
      if (!this.$refs.profileForm.validate()) return;

      this.saving = true;
      try {
        await axios.put(`/api/exporters-service/exporter/${this.exporterId}`, {
          name: this.profile.name,
          email: this.profile.email,
          phoneNumber: this.profile.phoneNumber,
          companyName: this.profile.companyName,
          companyDesc: this.profile.companyDesc,
          originCountry: this.profile.originCountry,
          originCountryCode: this.profile.originCountryCode,
        });

        this.showSnackbar('Profile updated successfully', 'success');
      } catch (error) {
        this.$toast.error('Failed to save profile:', error.message);
        this.showSnackbar(error.response?.data?.message || 'Failed to save profile', 'error');
      } finally {
        this.saving = false;
      }
    },

    getVerificationColor(status) {
      const colors = {
        VERIFIED: 'success',
        PENDING: 'warning',
        REJECTED: 'error',
        SUSPENDED: 'grey',
      };
      return colors[status] || 'grey';
    },

    getVerificationIcon(status) {
      const icons = {
        VERIFIED: 'mdi-check-circle',
        PENDING: 'mdi-clock-outline',
        REJECTED: 'mdi-close-circle',
        SUSPENDED: 'mdi-pause-circle',
      };
      return icons[status] || 'mdi-help-circle';
    },

    showSnackbar(message, color = 'success') {
      this.snackbar = { show: true, message, color };
    },
  },
};
</script>
