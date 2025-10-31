<template>
  <v-card class="tw-px-4 md:tw-px-8 tw-flex tw-justify-center tw-w-full" flat>
    <v-form v-model="isValid" class="tw-w-full tw-mt-5">
      <div class="tw-flex tw-flex-row tw-gap-5">
        <div class="tw-w-full">
          <v-text-field
              id="name"
              type="text"
              name="name"
              v-model="fullname"
              label="Full Name"
              class="my-1"
              :rules="[required('Full Name')]"
          >
          </v-text-field>
          <!--        <v-text-field-->
          <!--            id="email"-->
          <!--            type="email"-->
          <!--            name="email"-->
          <!--            v-model="email"-->
          <!--            label="Email Address"-->
          <!--            :rules="[required('Email'), emailFormat()]"-->
          <!--            disabled-->
          <!--        >-->
          <!--        </v-text-field>-->
          <div
              class="tw-flex tw-w-full tw-flex-row tw-justify-start tw-items-start"
          >
            <div
                class="tw-flex tw-flex-col"
            >
              <v-text-field
                  label="My Location"
                  v-model="userLocation.location.customName"
                  class="tw-font-bold"
              ></v-text-field>
              <div class="tw-flex tw-flex-row">
                <v-text-field
                    label="latitude"
                    prefix="("
                    suffix=","
                    v-model="userLocation.location.latitude"
                ></v-text-field>
                <v-text-field
                    label="longitude"
                    suffix=")"
                    v-model="userLocation.location.longitude"
                ></v-text-field>
              </div>
            </div>
          </div>
          <v-text-field
              id="phone"
              type="text"
              name="phone"
              v-model="phone_number"
              label="Phone Number"
              :rules="[required('Mobile No.')]"
              disabled
          >
          </v-text-field>
        </div>
        <div class="tw-w-full tw-border-4">
          <v-btn
              @click="getLocation"
          >
            Get Location
            <v-icon color="linear-gradient(green, red)" >mdi-google-maps</v-icon>
          </v-btn>
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
import { mapGetters, mapState } from 'vuex';
import axios from 'axios';
import L from 'leaflet';
import { getCurrentUserRole, getCurrentUserId } from '@/utils/roles.js';
import validations from '@/utils/validations.js';

export default {
  name: 'UserForm',
  data() {
    return {
      initialDetails: {
        fullname: '',
        latitude: '',
        longitude: '',
        customName: '',
        phoneNumber: '',
      },
      fullname: '',
      email: '',
      phone_number: '',
      kra_pin: '',
      isValid: false,
      ...validations,
      loading: false,
      role: '',
      userLocation: {
        location: {
          latitude: 0.0,
          longitude: 0.0,
          customName: '',
        },
      },
      map: null,
      marker: null,
    };
  },
  computed: {
    ...mapState({
      user: (state) => state.auth.user,
    }),
    ...mapGetters('auth', ['hasAuthenticationStatus', 'authenticationStatus']),
    getCurrentUserId,
    getCurrentUserRole,
  },
  mounted() {
    this.role = getCurrentUserRole();
    // Guard against user not yet loaded
    this.fullname = this.user?.userProfile?.fullName || '';
    this.email = this.user?.email || '';
    this.phone_number = this.user?.phone_number || '';
    axios.get(`${getCurrentUserRole()}s-service/location/${this.role}?${this.role}Id=${getCurrentUserId()}`).then((response) => {
      if (response.data.success === true) {
        const data = response.data.data;
        this.userLocation.location.latitude = data?.latitude;
        this.userLocation.location.longitude = data?.longitude;
        this.userLocation.location.customName = data?.customName;
      }
    }).catch((e) => {
      this.$toast.error(e.message);
    }).finally(() => {
      this.initialDetails = {
        fullname: this.fullname,
        latitude: this.userLocation.location.latitude,
        longitude: this.userLocation.location.longitude,
        customName: this.userLocation.location.customName,
        phoneNumber: this.phone_number,
      };
    });
    this.initMap();
  },
  methods: {
    // Initialize the Leaflet map
    initMap() {
      this.map = L.map('map').setView([0, 0], 2); // Default view

      // Add OpenStreetMap tiles
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
      }).addTo(this.map);

      // Click event to update location
      this.map.on('click', this.updateLocation);
    },

    // Update location based on user click
    updateLocation(event) {
      const { lat, lng } = event.latlng;

      // Update user location
      this.userLocation.location.latitude = lat;
      this.userLocation.location.longitude = lng;

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
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`,
      );
      const data = await response.json();
      this.userLocation.location.customName = data.display_name || 'Unknown Location';
    },
    async updateProfile() {
      this.loading = true;
      // { email: this.email, phone_number: this.phone_number, name: this.fullname }
      await this.$store.dispatch('auth/updateUser', { name: this.fullname }).then(() => {
        if (this.hasAuthenticationStatus) {
          this.loading = false;
          if (this.authenticationStatus.variant === 'error') {
            this.$toast.error(this.authenticationStatus.message, 'Error');
          } else {
            this.$toast.success(this.authenticationStatus.message, 'Success');
          }
        }
      });
    },
    getLocation() {
      this.$toast.show('Collecting location infor');
      navigator.geolocation.getCurrentPosition(async (position) => {
        this.userLocation.location.latitude = position.coords.latitude;
        this.userLocation.location.longitude = position.coords.longitude;
        const url = `https://maps.googleapis.com/maps/api/geocode/json?latlng=${this.userLocation.location.latitude},${this.userLocation.location.longitude}
&location_type=ROOFTOP&result_type=street_address&key=${process.env.VUE_APP_GOOGLE_MAPS_API_KEY}`;
        fetch(url)
          .then((response) => {
            if (!response.ok) {
              this.$toast.error(`Network response was not ok ${response.statusText}`);
            }
            return response.json(); // or response.text(), response.blob(), etc.
          })
          .then((data) => {
            this.userLocation.location.customName = data.plus_code.compound_code;
          })
          .catch((error) => {
            this.$toast.error('Error fetching location name', error.message);
          });
      },
      (positionError) => {
        this.$toast.error(positionError.message);
      });
    },
    saveLocation() {
      axios.put(`/${this.role}s-service/location/${this.role}`, {
        [`${this.role}Id`]: getCurrentUserId(),
        locationDto: {
          latitude: this.userLocation.location.latitude,
          longitude: this.userLocation.location.longitude,
          customName: this.userLocation.location.customName,
        },
      }).then((response) => {
        if (response.data.success === true) {
          this.$toast.success('Location updated successfully!');
        } else {
          this.$toast.error('Failed to update location', response.data.msg);
        }
      }).catch((error) => {
        this.$toast.error(error.message);
      });
    },
  },
};
</script>
<style>
.map-container {
  width: 100%;
  height: 400px;
  margin-top: 10px;
  border-radius: 8px;
  overflow: hidden;
}
</style>
