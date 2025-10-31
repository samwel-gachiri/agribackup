/* disable-eslint */
<template>
  <Default>
    <!-- Dialog for Marker Info -->
    <v-dialog v-model="userDialog" max-width="400">
      <FarmerListingsDialog
          v-if="selectedUserFromMap?.role === 'farmer'"
          :selected-farmer="selectedUserFromMap"
          @close="selectedUserFromMap = null"
      />
      <BuyerRequestsDialog
          v-if="selectedUserFromMap?.role === 'buyer'"
          :selected-buyer="selectedUserFromMap"
          @close="selectedUserFromMap = null"
      />
    </v-dialog>

    <div class="tw-relative tw-h-screen tw-w-full tw-bg-gray-900">
      <!-- Map Container -->
      <div ref="map" id="map" class="tw-h-full tw-w-full tw-z-0"></div>

      <!-- Top Search Bar - Overlay -->
      <div class="tw-absolute tw-top-4 tw-left-4 tw-right-4 tw-z-50">
        <div class="tw-bg-gray-800/90 tw-backdrop-blur-md tw-rounded-2xl tw-shadow-2xl tw-border tw-border-gray-700/50 tw-p-6">
          <h1 class="tw-text-2xl tw-font-bold tw-text-white tw-mb-4 tw-text-center">
            Find Farmers & Buyers
          </h1>

          <!-- Search Controls -->
          <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-4 tw-gap-4">
            <v-select
                v-model="farmerFilter"
                :items="['All Farmers', 'Selling Farmers', 'None']"
                @change="loadMap"
                label="Select Farmers"
                dark
                outlined
                dense
                class="tw-text-white"
            ></v-select>

            <v-combobox
                v-model="selectedFarmer"
                :items="filteredFarmers"
                item-text="name"
                item-value=""
                return-object
                label="Search Farmers"
                :disabled="filteredFarmers.length === 0"
                clearable
                dark
                outlined
                dense
            ></v-combobox>

            <v-select
                v-model="buyerFilter"
                :items="['All Buyers', 'Requesting Buyers', 'None']"
                @change="loadMap"
                label="Select Buyers"
                dark
                outlined
                dense
            ></v-select>

            <v-combobox
                v-model="selectedBuyer"
                :items="filteredBuyers"
                return-object
                item-text="name"
                label="Search Buyers"
                :disabled="filteredBuyers.length === 0"
                clearable
                dark
                outlined
                dense
            ></v-combobox>
          </div>

          <!-- Distance Display -->
          <div v-if="distance" class="tw-text-center tw-mt-4">
            <p class="tw-text-cyan-400 tw-font-semibold">
              Distance: {{ (distance / 1000).toFixed(2) }} km
            </p>
          </div>
        </div>
      </div>

      <!-- Right Sidebar - Overlay -->
      <div class="tw-absolute tw-top-48 tw-right-4 tw-bottom-4 tw-w-80 tw-z-40 tw-flex tw-flex-col tw-gap-4">
        <!-- Farmers List -->
        <div v-if="showFarmersList" class="tw-bg-gray-800/90 tw-backdrop-blur-md tw-rounded-2xl tw-shadow-2xl tw-border tw-border-gray-700/50 tw-flex-1 tw-overflow-hidden">
          <div class="tw-p-4 tw-border-b tw-border-gray-700/50">
            <div class="tw-flex tw-items-center tw-justify-between">
              <h2 class="tw-text-lg tw-font-bold tw-text-green-400 tw-flex tw-items-center">
                <svg class="tw-w-5 tw-h-5 tw-mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/>
                  <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/>
                </svg>
                Farmers Nearby ({{ filteredFarmers.length }})
              </h2>
              <button @click="showFarmersList = false" class="tw-text-gray-400 hover:tw-text-white tw-transition-colors">
                <svg class="tw-w-5 tw-h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="tw-overflow-y-auto tw-p-4 tw-space-y-3" style="max-height: 300px;">
            <div
                v-for="farmer in filteredFarmers"
                :key="farmer.id"
                class="tw-bg-gray-700/50 tw-rounded-xl tw-p-4 tw-cursor-pointer tw-transition-all tw-duration-300 hover:tw-bg-gray-600/50 hover:tw-scale-105 tw-border tw-border-gray-600/30"
                @click="setFocusToLocation(farmer.location.latitude, farmer.location.longitude)"
            >
              <h3 class="tw-text-white tw-font-bold tw-text-sm tw-mb-2">{{ farmer.name }}</h3>
              <p class="tw-text-green-300 tw-text-xs tw-mb-2">
                📍 {{ farmer.location.customName || 'Location' }}
              </p>
              <p class="tw-text-gray-300 tw-text-xs">
                🌾 {{ farmer.farmerProduces ? farmer.farmerProduces.map(p => p.farmProduce.name).join(', ') || 'No produce listed' : 'No produce listed' }}
              </p>
            </div>
          </div>
        </div>

        <!-- Buyers List -->
        <div v-if="showBuyersList" class="tw-bg-gray-800/90 tw-backdrop-blur-md tw-rounded-2xl tw-shadow-2xl tw-border tw-border-gray-700/50 tw-flex-1 tw-overflow-hidden">
          <div class="tw-p-4 tw-border-b tw-border-gray-700/50">
            <div class="tw-flex tw-items-center tw-justify-between">
              <h2 class="tw-text-lg tw-font-bold tw-text-blue-400 tw-flex tw-items-center">
                <svg class="tw-w-5 tw-h-5 tw-mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z"/>
                </svg>
                Buyers Nearby ({{ filteredBuyers.length }})
              </h2>
              <button @click="showBuyersList = false" class="tw-text-gray-400 hover:tw-text-white tw-transition-colors">
                <svg class="tw-w-5 tw-h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="tw-overflow-y-auto tw-p-4 tw-space-y-3" style="max-height: 300px;">
            <div
                v-for="buyer in filteredBuyers"
                :key="buyer.id"
                class="tw-bg-gray-700/50 tw-rounded-xl tw-p-4 tw-cursor-pointer tw-transition-all tw-duration-300 hover:tw-bg-gray-600/50 hover:tw-scale-105 tw-border tw-border-gray-600/30"
                @click="setFocusToLocation(buyer.location.latitude, buyer.location.longitude)"
            >
              <h3 class="tw-text-white tw-font-bold tw-text-sm tw-mb-2">{{ buyer.name }}</h3>
              <p class="tw-text-blue-300 tw-text-xs tw-mb-2">
                📍 {{ buyer.location.customName || 'Location' }}
              </p>
              <p class="tw-text-gray-300 tw-text-xs">
                🛒 {{ buyer.preferredProduces ? buyer.preferredProduces.map(p => p.bsfarmProduce.name).join(', ') || 'No preferences listed' : 'No preferences listed' }}
              </p>
            </div>
          </div>
        </div>

        <!-- Toggle Buttons -->
        <div class="tw-flex tw-gap-2">
          <button
              v-if="!showFarmersList && filteredFarmers.length > 0"
              @click="showFarmersList = true"
              class="tw-bg-green-600/80 tw-backdrop-blur-md tw-text-white tw-px-4 tw-py-2 tw-rounded-xl tw-font-semibold tw-transition-all tw-duration-300 hover:tw-bg-green-500 tw-flex-1 tw-text-sm"
          >
            Show Farmers ({{ filteredFarmers.length }})
          </button>

          <button
              v-if="!showBuyersList && filteredBuyers.length > 0"
              @click="showBuyersList = true"
              class="tw-bg-blue-600/80 tw-backdrop-blur-md tw-text-white tw-px-4 tw-py-2 tw-rounded-xl tw-font-semibold tw-transition-all tw-duration-300 hover:tw-bg-blue-500 tw-flex-1 tw-text-sm"
          >
            Show Buyers ({{ filteredBuyers.length }})
          </button>
        </div>
      </div>
    </div>
  </Default>
</template>

<script>
import { mapGetters, mapState } from 'vuex';
import L from 'leaflet';
import axios from 'axios';
import Default from '@/components/layout/Default.vue';
import FarmerListingsDialog from '@/components/listing/FarmerListingsDialog.vue';
import { getCurrentUserId } from '@/utils/roles.js';
import BuyerRequestsDialog from '@/components/request/BuyerRequestsDialog.vue';

// Fix for default markers in Leaflet
// eslint-disable-next-line no-underscore-dangle
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  // eslint-disable-next-line global-require
  iconRetinaUrl: require('leaflet/dist/images/marker-icon-2x.png'),
  // eslint-disable-next-line global-require
  iconUrl: require('leaflet/dist/images/marker-icon.png'),
  // eslint-disable-next-line global-require
  shadowUrl: require('leaflet/dist/images/marker-shadow.png'),
});

export default {
  components: { BuyerRequestsDialog, FarmerListingsDialog, Default },
  data() {
    return {
      map: null,
      markers: [],
      center: { lat: -1.2921, lng: 36.8219 }, // Default to Nairobi
      farmersLocation: [],
      buyersLocation: [],
      farmerFilter: 'All Farmers',
      buyerFilter: 'All Buyers',
      selectedFarmer: null,
      selectedBuyer: null,
      distance: null,
      userDialog: false,
      selectedUserFromMap: null,
      showFarmersList: true,
      showBuyersList: true,

      // Distance calculation
      routeControl: null,

      // Custom icons
      farmerIcon: null,
      buyerIcon: null,
      userIcon: null,
    };
  },
  computed: {
    ...mapState({
      user: (state) => state.auth.user,
      role: (state) => state.auth.role,
    }),
    ...mapGetters('auth', ['isAuthenticated']),
    filteredFarmers() {
      if (this.farmerFilter === 'Selling Farmers') {
        return this.farmersLocation.filter((farmer) => farmer.farmer && farmer.farmer.farmerProduces
            && farmer.farmer.farmerProduces.some((produce) => produce.status === 'ON_SALE'));
      }
      if (this.farmerFilter === 'None') {
        return [];
      }
      return this.farmersLocation;
    },
    filteredBuyers() {
      if (this.buyerFilter === 'Requesting Buyers') {
        return this.buyersLocation.filter((buyer) => buyer.buyer && buyer.buyer.preferredProduces
            && buyer.buyer.preferredProduces.some((preferred) => preferred.bsfarmProduce.status === 'REQUESTING'));
      }
      if (this.buyerFilter === 'None') return [];
      return this.buyersLocation;
    },
  },
  watch: {
    selectedFarmer(newFarmerLocation) {
      if (newFarmerLocation != null) {
        this.setFocusToLocation(newFarmerLocation.location.latitude, newFarmerLocation.location.longitude);
      }
    },
    selectedBuyer(newBuyerLocation) {
      if (newBuyerLocation != null) {
        this.setFocusToLocation(newBuyerLocation.location.latitude, newBuyerLocation.location.longitude);
      }
    },
    selectedUserFromMap(newValue) {
      if (newValue == null) {
        this.userDialog = false;
      }
    },
  },
  methods: {
    getCurrentUserId,

    initMap() {
      // Initialize Leaflet map
      this.map = L.map('map', {
        zoomControl: false,
      }).setView([this.center.lat, this.center.lng], 13);

      // Add custom zoom control
      L.control.zoom({
        position: 'bottomright',
      }).addTo(this.map);

      // Light theme tile layer
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19,
      }).addTo(this.map);

      // Create custom icons
      this.createCustomIcons();

      // Load markers
      this.loadMap();
    },

    createCustomIcons() {
      // Farmer icon (green)
      this.farmerIcon = L.divIcon({
        html: `
          <div class="tw-w-10 tw-h-10 tw-bg-green-500 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-shadow-lg tw-border-2 tw-border-white">
            <svg class="tw-w-5 tw-h-5 tw-text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/>
              <path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/>
            </svg>
          </div>
        `,
        // eslint-disable-next-line sonarjs/no-duplicate-string
        className: 'custom-div-icon',
        iconSize: [40, 40],
        iconAnchor: [20, 20],
      });

      // Buyer icon (blue)
      this.buyerIcon = L.divIcon({
        html: `
          <div class="tw-w-10 tw-h-10 tw-bg-blue-500 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-shadow-lg tw-border-2 tw-border-white">
            <svg class="tw-w-5 tw-h-5 tw-text-white" fill="currentColor" viewBox="0 0 20 20">
              <path d="M3 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1H4a1 1 0 01-1-1V4zM3 10a1 1 0 011-1h6a1 1 0 011 1v6a1 1 0 01-1 1H4a1 1 0 01-1-1v-6zM14 9a1 1 0 00-1 1v6a1 1 0 001 1h2a1 1 0 001-1v-6a1 1 0 00-1-1h-2z"/>
            </svg>
          </div>
        `,
        className: 'custom-div-icon',
        iconSize: [40, 40],
        iconAnchor: [20, 20],
      });

      // User location icon (red)
      this.userIcon = L.divIcon({
        html: `
          <div class="tw-w-12 tw-h-12 tw-bg-red-500 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-shadow-lg tw-border-2 tw-border-white tw-animate-pulse">
            <svg class="tw-w-6 tw-h-6 tw-text-white" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z" clip-rule="evenodd"/>
            </svg>
          </div>
        `,
        className: 'custom-div-icon',
        iconSize: [48, 48],
        iconAnchor: [24, 24],
      });
    },

    loadMap() {
      // Clear existing markers
      this.markers.forEach((marker) => this.map.removeLayer(marker));
      this.markers = [];

      // Add user location marker if available
      if (this.user && this.user.location) {
        const userMarker = L.marker([this.user.location.latitude, this.user.location.longitude], {
          icon: this.userIcon,
        }).addTo(this.map);

        userMarker.bindPopup(`
          <div class="tw-p-2">
            <h3 class="tw-font-bold tw-text-gray-800">Your Location</h3>
            <p class="tw-text-sm tw-text-gray-600">You are here</p>
          </div>
        `);

        this.markers.push(userMarker);
      }

      // Add farmer markers
      this.filteredFarmers.forEach((farmer) => {
        if (farmer.location) {
          const farmerMarker = L.marker([farmer.location.latitude, farmer.location.longitude], {
            icon: this.farmerIcon,
          }).addTo(this.map);

          const produces = farmer.farmer && farmer.farmer.farmerProduces
            ? farmer.farmer.farmerProduces.map((p) => p.farmProduce.name).join(', ')
            : 'No produce listed';

          farmerMarker.bindPopup(`
            <div class="tw-p-3 tw-max-w-xs">
              <h3 class="tw-font-bold tw-text-gray-800 tw-mb-2">${farmer.name || 'Farmer'}</h3>
              <p class="tw-text-sm tw-text-gray-600 tw-mb-2">📍 ${farmer.location.customName || 'Location'}</p>
              <p class="tw-text-sm tw-text-gray-600 tw-mb-3">🌾 ${produces}</p>
              <button class="tw-bg-green-500 tw-text-white tw-px-3 tw-py-1 tw-rounded tw-text-sm hover:tw-bg-green-600 tw-transition-colors" onclick="this.openUserDialog()">
                View Details
              </button>
            </div>
          `);

          farmerMarker.on('click', () => {
            this.selectedUserFromMap = { ...farmer, role: 'farmer' };
            this.calculateDistance(farmer.location.latitude, farmer.location.longitude);
          });

          this.markers.push(farmerMarker);
        }
      });

      // Add buyer markers
      this.filteredBuyers.forEach((buyer) => {
        if (buyer.location) {
          const buyerMarker = L.marker([buyer.location.latitude, buyer.location.longitude], {
            icon: this.buyerIcon,
          }).addTo(this.map);

          const preferences = buyer.buyer && buyer.buyer.preferredProduces
            ? buyer.buyer.preferredProduces.map((p) => p.bsfarmProduce.name).join(', ')
            : 'No preferences listed';

          buyerMarker.bindPopup(`
            <div class="tw-p-3 tw-max-w-xs">
              <h3 class="tw-font-bold tw-text-gray-800 tw-mb-2">${buyer.name || 'Buyer'}</h3>
              <p class="tw-text-sm tw-text-gray-600 tw-mb-2">📍 ${buyer.location.customName || 'Location'}</p>
              <p class="tw-text-sm tw-text-gray-600 tw-mb-3">🛒 ${preferences}</p>
              <button class="tw-bg-blue-500 tw-text-white tw-px-3 tw-py-1 tw-rounded tw-text-sm hover:tw-bg-blue-600 tw-transition-colors" onclick="this.openUserDialog()">
                View Details
              </button>
            </div>
          `);

          buyerMarker.on('click', () => {
            this.selectedUserFromMap = { ...buyer, role: 'buyer' };
            this.calculateDistance(buyer.location.latitude, buyer.location.longitude);
          });

          this.markers.push(buyerMarker);
        }
      });
    },

    setFocusToLocation(lat, lng) {
      if (lat != null && lng != null) {
        this.map.setView([lat, lng], 16, {
          animate: true,
          duration: 1,
        });
        // Calculate distance when focusing on a location
        this.calculateDistance(lat, lng);
      } else {
        this.$toast.warning('Could not pan to location');
      }
    },
    openUserDialog() {
      this.userDialog = true;
    },

    calculateDistance(targetLat, targetLng) {
      if (this.user && this.user.location) {
        const userLat = this.user.location.latitude;
        const userLng = this.user.location.longitude;

        // Calculate distance using Haversine formula
        const R = 6371000; // Earth's radius in meters
        const dLat = this.toRadians(targetLat - userLat);
        const dLng = this.toRadians(targetLng - userLng);
        const a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(this.toRadians(userLat)) * Math.cos(this.toRadians(targetLat))
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        this.distance = R * c; // Distance in meters

        // Clear previous route
        if (this.routeControl) {
          this.map.removeControl(this.routeControl);
        }

        // Add route line (requires leaflet-routing-machine plugin)
        // For now, we'll draw a simple line
        this.drawRouteLine(userLat, userLng, targetLat, targetLng);
      }
    },

    drawRouteLine(fromLat, fromLng, toLat, toLng) {
      // Remove existing route line
      this.markers.forEach((marker) => {
        // eslint-disable-next-line no-underscore-dangle
        if (marker._routeLine) {
          this.map.removeLayer(marker);
        }
      });

      // Draw a simple line between points
      const routeLine = L.polyline([
        [fromLat, fromLng],
        [toLat, toLng],
      ], {
        color: '#ef4444',
        weight: 3,
        opacity: 0.7,
        dashArray: '5, 10',
      }).addTo(this.map);

      // eslint-disable-next-line no-underscore-dangle
      routeLine._routeLine = true;
      this.markers.push(routeLine);
    },

    toRadians(degrees) {
      return degrees * (Math.PI / 180);
    },

    fetchUsersLocation() {
      // Mock data for testing - replace with actual API calls
      // this.farmersLocation = [
      //   {
      //     id: '1',
      //     name: 'John Farmer',
      //     email: 'john@farmer.com',
      //     phoneNumber: '+254700000001',
      //     farmer: {
      //       name: 'John Farmer',
      //       farmerProduces: [
      //         {
      //           id: '1',
      //           farmProduce: { name: 'Tomatoes', description: 'Fresh tomatoes' },
      //           status: 'ON_SALE',
      //         },
      //       ],
      //     },
      //     location: {
      //       latitude: -1.2921 + (Math.random() - 0.5) * 0.1,
      //       longitude: 36.8219 + (Math.random() - 0.5) * 0.1,
      //       customName: 'Nairobi Farm',
      //     },
      //   },
      //   {
      //     id: '2',
      //     name: 'Mary Grower',
      //     email: 'mary@grower.com',
      //     phoneNumber: '+254700000002',
      //     farmer: {
      //       name: 'Mary Grower',
      //       farmerProduces: [
      //         {
      //           id: '2',
      //           farmProduce: { name: 'Carrots', description: 'Organic carrots' },
      //           status: 'ON_SALE',
      //         },
      //       ],
      //     },
      //     location: {
      //       latitude: -1.2921 + (Math.random() - 0.5) * 0.1,
      //       longitude: 36.8219 + (Math.random() - 0.5) * 0.1,
      //       customName: 'Organic Farm',
      //     },
      //   },
      // ];
      //
      // this.buyersLocation = [
      //   {
      //     id: '1',
      //     name: 'Restaurant ABC',
      //     email: 'abc@restaurant.com',
      //     phoneNumber: '+254700000003',
      //     buyer: {
      //       name: 'Restaurant ABC',
      //       preferredProduces: [
      //         {
      //           id: '1',
      //           bsfarmProduce: { name: 'Vegetables', description: 'Fresh vegetables' },
      //           status: 'REQUESTING',
      //         },
      //       ],
      //     },
      //     location: {
      //       latitude: -1.2921 + (Math.random() - 0.5) * 0.1,
      //       longitude: 36.8219 + (Math.random() - 0.5) * 0.1,
      //       customName: 'Restaurant Location',
      //     },
      //   },
      //   {
      //     id: '2',
      //     name: 'Market XYZ',
      //     email: 'xyz@market.com',
      //     phoneNumber: '+254700000004',
      //     buyer: {
      //       name: 'Market XYZ',
      //       preferredProduces: [
      //         {
      //           id: '2',
      //           bsfarmProduce: { name: 'Fruits', description: 'Fresh fruits' },
      //           status: 'REQUESTING',
      //         },
      //       ],
      //     },
      //     location: {
      //       latitude: -1.2921 + (Math.random() - 0.5) * 0.1,
      //       longitude: 36.8219 + (Math.random() - 0.5) * 0.1,
      //       customName: 'Market Center',
      //     },
      //   },
      // ];

      this.initMap();

      // Uncomment and modify these for actual API calls
      axios.get('/farmers-service/location/farmers', {
        params: {
          latitude: this.user.location.latitude,
          longitude: this.user.location.longitude,
        },
      })
        .then((response) => {
          if (response.data.success) {
            this.farmersLocation = response.data.data;
          } else {
            this.$toast.error('Failed to load farmers locations');
          }
        })
        .catch((reason) => this.$toast.error(reason.message))
        .finally(() => {
          axios.get('/buyers-service/location/buyers', {
            params: {
              latitude: this.user.location.latitude,
              longitude: this.user.location.longitude,
            },
          })
            .then((buyersLocationRes) => {
              if (buyersLocationRes.data.success) {
                this.buyersLocation = buyersLocationRes.data.data;
              } else {
                this.$toast.error('Failed to load buyers locations');
              }
              this.initMap();
            })
            .catch((reason) => this.$toast.error(reason.message));
        });
    },
  },
  mounted() {
    // Check if user has location, otherwise get current position
    if (!this.user || !this.user.location) {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const location = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
          };

          this.center.lat = location.latitude;
          this.center.lng = location.longitude;

          // Update Vuex store if available
          if (this.$store) {
            await this.$store.dispatch('auth/updateUserLocation', location);
          }

          this.fetchUsersLocation();
        },
        (error) => {
          this.$toast.warning('Geolocation error:', error.message);
          // Use default location (Nairobi)
          this.fetchUsersLocation();
        },
        { enableHighAccuracy: true },
      );
    } else {
      this.center.lat = this.user.location.latitude;
      this.center.lng = this.user.location.longitude;
      this.fetchUsersLocation();
    }
  },

  beforeDestroy() {
    if (this.map) {
      this.map.remove();
    }
  },
};
</script>

<style scoped>
/* Custom styles for the map */
.custom-div-icon {
  background: none !important;
  border: none !important;
}

/* Ensure map container has proper styling */
#map {
  background-color: #f8fafc;
}

/* Custom scrollbar for sidebar */
.tw-overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}

.tw-overflow-y-auto::-webkit-scrollbar-track {
  background: rgba(75, 85, 99, 0.3);
  border-radius: 4px;
}

.tw-overflow-y-auto::-webkit-scrollbar-thumb {
  background: rgba(156, 163, 175, 0.5);
  border-radius: 4px;
}

.tw-overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: rgba(156, 163, 175, 0.8);
}

/* Animation for markers */
@keyframes marker-bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

.marker-bounce {
  animation: marker-bounce 2s infinite;
}

/* Glassmorphism effect */
.glass-effect {
  backdrop-filter: blur(16px) saturate(180%);
  -webkit-backdrop-filter: blur(16px) saturate(180%);
  background-color: rgba(17, 25, 40, 0.75);
  border: 1px solid rgba(255, 255, 255, 0.125);
}

/* Smooth transitions */
.smooth-transition {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Pulse animation for user location */
@keyframes pulse-ring {
  0% {
    transform: scale(0.33);
  }
  40%, 50% {
    opacity: 0;
  }
  100% {
    opacity: 0;
    transform: scale(1.2);
  }
}

.pulse-ring {
  animation: pulse-ring 1.25s cubic-bezier(0.215, 0.61, 0.355, 1) infinite;
}
</style>
