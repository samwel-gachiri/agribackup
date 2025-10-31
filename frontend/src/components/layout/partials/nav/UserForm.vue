<template>
  <v-card class="tw-px-4 md:tw-px-8 tw-flex tw-justify-center tw-w-full" flat>
    <v-form v-model="isValid" class="tw-w-full tw-mt-5">
        <div class="tw-flex md:tw-flex-row tw-flex-col tw-gap-5">
          <div class="tw-w-full tw-flex tw-flex-col tw-justify-between">
            <!-- Personal Details Section -->
            <div class="tw-flex-1 tw-flex tw-flex-col tw-gap-4">
              <h2 class="tw-text-lg tw-font-semibold tw-text-green-600">Personal Details</h2>
              <v-text-field
                  label="Full Name"
                  :outlined="false"
                  :filled="true"
                  dense
                  v-model="fullName"
                  :rules="[required('Name'), noDigitFormat()]"
                  :hide-details="false"
                  class="modern-input tw-w-full"
                  aria-label="Full Name"
                  autocomplete="name"
                  placeholder="e.g. Jane Doe"
              />
              <vue-phone-number-input
                  v-model="phoneNumber"
                  :default-country-code="'KE'"
                  :preferred-countries="['KE', 'US', 'GB']"
                  show-code-on-list
                  @update="onPhoneUpdate"
                  aria-label="Phone Number"
                  class="tw-w-full"
                  placeholder="e.g. +254712345678"
              />
              <v-text-field
                  v-model="email"
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
            <div v-if="role != 'exporter'" class="tw-fleFx tw-w-full tw-flex-col tw-justify-start tw-items-start">
                <h5>{{ loc_name === '' ? "Give in your location details": "Your Location Details"}}</h5>
                <v-alert
                  type="info"
                >This will be used by farmers, buyers and the public to notice you.</v-alert>
                <v-text-field
                    outlined
                    label="My Location"
                    v-model="loc_name"
                    class="tw-font-bold tw-w-full"
                ></v-text-field>
                <div class="tw-flex tw-flex-row">
                  <v-text-field
                      label="latitude"
                      prefix="("
                      suffix=","
                      v-model="lat"
                  ></v-text-field>
                  <v-text-field
                      label="longitude"
                      suffix=")"
                      v-model="lng"
                  ></v-text-field>
                </div>
                <v-btn
                    @click="getLocation"
                    class="tw-mb-4"
                    color="primary"
                >
                  Use Device Location
                  <v-icon color="linear-gradient(green, red)" >mdi-google-maps</v-icon>
                </v-btn>
            </div>
          </div>
          <div v-if="false" class="tw-w-full">
            <strong>Pinpoint Your Location Here <v-icon>mdi-gesture-two-tap</v-icon></strong>
            <!-- Leaflet Map -->
            <div id="map" class="map-container"></div>
          </div>
        </div>
        <v-card-actions class="col-sm-6 offset-sm-3">
          <v-btn block small
                 id="userUpdate"
                 color="secondary"
                 @click="updateProfile"
                 :disabled="!isValid"
                 :loading="loading">
            Update Details
          </v-btn>
        </v-card-actions>
    </v-form>
  </v-card>
</template>

<script>
import VuePhoneNumberInput from 'vue-phone-number-input';
import { mapGetters, mapState } from 'vuex';
import axios from 'axios';
import L from 'leaflet';
import { getCurrentUserRole, getCurrentUserId } from '@/utils/roles.js';
import validations from '@/utils/validations.js';

export default {
  name: 'UserForm',
  components: {
    VuePhoneNumberInput,
  },
  data() {
    return {
      fullname: '',
      email: '',
      phoneNumber: '',
      fullPhoneNumber: '',
      kra_pin: '',
      lat: '',
      lng: '',
      loc_name: '',
      isValid: false,
      ...validations,
      loading: false,
      role: getCurrentUserRole(),
      map: null,
      marker: null,
    };
  },
  computed: {
    ...mapState({
      user: (state) => state.auth.user,
    }),
    ...mapGetters('auth', ['hasAuthenticationStatus', 'authenticationStatus']),
  },
  mounted() {
    // this.initMap();
    // this.resizeObserver = new ResizeObserver(() => {
    //   if (this.map) {
    //     this.map.invalidateSize();
    //   }
    // });
    // if (this.resizeObserver != null) this.resizeObserver.observe(document.getElementById('map'));
    this.fullname = this.fullName;
    this.email = this.user.email;
    this.phoneNumber = this.user.phoneNumber;
    if (this.user.location && this.role !== 'exporter') {
      this.map.panTo([this.user?.location?.latitude, this.user?.location?.longitude]);
      this.marker = L.marker([this.user?.location?.latitude, this.user?.location?.longitude], {
        icon: L.icon({
          iconUrl: `https://agri-image.s3.us-east-1.amazonaws.com/uploads/${this.role}_map_icon.png`, // Path to your image
          iconSize: [25, 41],
          iconAnchor: [12, 41],
          popupAnchor: [1, -34],
          shadowUrl: 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-shadow.png',
          shadowSize: [41, 41], // Shadow size
        }),
      }).addTo(this.map);
      if (this.user.location.latitude) {
        this.lat = this.user.location.latitude;
      }
      if (this.user.location.longitude) {
        this.lng = this.user.location.longitude;
      }
      if (this.user.location.customName) {
        this.loc_name = this.user.location.customName;
      }
    }
  },
  // beforeDestroy() {
  //   if (this.resizeObserver) {
  //     this.resizeObserver.disconnect();
  //   }
  // },
  methods: {
    // Initialize the Leaflet map
    initMap() {
      this.$nextTick(() => {
        if (!this.map) {
          this.map = L.map('map', {
            preferCanvas: true, // Better for performance
          }).setView([0, 0], 2);

          L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors',
          }).addTo(this.map);

          this.map.on('click', this.updateLocation);

          // Force a resize in case the container was hidden initially
          setTimeout(() => {
            this.map.invalidateSize();
          }, 100);
        }
      });
    },

    onPhoneUpdate(phoneData) {
      // Get the full international number with country code
      this.fullPhoneNumber = phoneData.e164 || phoneData.formatInternational;
    },
    // Update location based on user click
    updateLocation(event) {
      const { lat, lng } = event.latlng;

      // Update user location
      this.lat = lat;
      this.lng = lng;

      // Reverse geocode (fetch location name)
      this.getLocationName(lat, lng);

      // Move marker
      if (this.marker) {
        this.marker.setLatLng([lat, lng]);
      } else {
        this.marker = L.marker([lat, lng]).addTo(this.map);
      }
    },
    async getLocationName(lat, lng) {
      try {
        const response = await fetch(
          `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`,
        );
        const data = await response.json();
        this.loc_name = data.display_name || 'Unknown Location';
      } catch (e) {
        this.$toast.error(e.message);
        this.loc_name = '';
      }
    },
    async updateProfile() {
      this.loading = true;
      // { email: this.email, phoneNumber: this.phoneNumber, name: this.fullname }
      await this.$store.dispatch('auth/updateUser', { name: this.fullname, phoneNumber: this.fullPhoneNumber, email: this.email }).then(() => {
        if (this.hasAuthenticationStatus) {
          this.loading = false;
          // if (this.authenticationStatus.variant === 'error') {
          //   this.$toast.error(this.authenticationStatus.message, 'Error');
          // } else {
          //   this.$toast.success(this.authenticationStatus.message, 'Success');
          // }
        }
      }).finally(async () => {
        if (this.role === 'exporter') {
          axios.put(`/exporters-service/exporter/${getCurrentUserId()}`, {
            name: this.fullname,
            email: this.email,
            phoneNumber: this.fullPhoneNumber ? this.fullPhoneNumber : '',
          }).then((response) => {
            if (response.data.success === true) {
              this.$toast.success('Exporter Profile Updated successfully!');
            } else {
              this.$toast.error('Failed to update exporter profile', response.data.msg);
            }
          }).catch((error) => {
            this.$toast.error(error.message);
          });
          return;
        }
        axios.put(`${getCurrentUserRole()}s-service/${getCurrentUserRole()}`, {
          [`${getCurrentUserRole()}Id`]: getCurrentUserId(),
          name: this.fullname,
          phoneNumber: this.fullPhoneNumber ? this.fullPhoneNumber : '',
          email: this.email,
          location: {
            latitude: this.lat,
            longitude: this.lng,
            customName: this.loc_name,
          },
        }).then((response) => {
          if (response.data.success === true) {
            this.$toast.success('Farmer Profile Updated successfully!');
          } else {
            this.$toast.error('Failed to update farmer profile', response.data.msg);
          }
        }).catch((error) => {
          this.$toast.error(error.message);
        }).finally(() => this.saveLocation());
      });
    },
    getLocation() {
      navigator.geolocation.getCurrentPosition(async (position) => {
        this.lat = position.coords.latitude;
        this.lng = position.coords.longitude;
        this.getLocationName(this.lat, this.lng);
        if (this.marker) {
          this.marker.setLatLng([position.coords.latitude, position.coords.longitude]);
        } else {
          this.marker = L.marker([position.coords.latitude, position.coords.longitude]).addTo(this.map);
        }
      },
      (positionError) => {
        this.$toast.error(positionError.message);
      });
    },
  },
};
</script>
<style>
.map-container {
  width: 100%;
  height: 400px; /* Fixed height */
  min-height: 400px; /* Prevent collapsing */
  margin-top: 10px;
  border-radius: 8px;
  overflow: hidden;
  position: relative; /* Important for Leaflet */
  z-index: 0; /* Ensure proper stacking */
}
</style>
