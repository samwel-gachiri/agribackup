<template>
  <div class="tw-px-1 tw-py-2">
    <!-- Main Content -->
    <div class="tw-max-w-6xl tw-mx-auto">
      <div class="tw-grid lg:tw-grid-cols-3 tw-gap-6">

        <!-- Left Panel - Form -->
        <div class="lg:tw-col-span-2">
          <v-card class="glass-surface tw-rounded-2xl tw-overflow-hidden">
            <div class="tw-px-5 tw-pt-4 tw-pb-0 tw-flex tw-items-center tw-justify-between">
              <h1 class="tw-text-lg tw-font-extrabold tw-text-gray-800 tw-flex tw-items-center">
                <v-icon color="primary" class="tw-mr-2">mdi-tag-plus</v-icon>
                Create Listing
              </h1>
            </div>

            <v-card-text class="tw-p-5">
              <v-form v-model="isValid">
                <!-- Product Selection Section -->
                <div class="tw-mb-6">
                  <h3 class="tw-text-sm tw-font-bold tw-text-gray-700 tw-mb-3 tw-flex tw-items-center tw-uppercase tw-tracking-wide">
                    <v-icon class="tw-mr-2 tw-text-green-600">mdi-sprout</v-icon>
                    Product Information
                  </h3>

                  <div class="tw-grid md:tw-grid-cols-2 tw-gap-4">
                    <div class="tw-space-y-2">
                      <v-combobox
                          v-model="selectedProduce"
                          :items="formattedFarmerProduces"
                          item-value="id"
                          item-text="name"
                          label="Select Farm Produce"
                          prepend-inner-icon="mdi-fruit-grapes"
                          @update:model-value="farmProduceListingChanged"
                          return-object
                          dense
                          solo
                          class="data-input"
                      ></v-combobox>

                      <v-alert
                          v-if="formattedFarmerProduces?.length === 0"
                          type="info"
                          outlined
                          class="tw-rounded-lg tw-border tw-border-blue-200 tw-bg-blue-50"
                      >
                        No produce found.
                        <router-link
                            class="tw-underline tw-font-medium"
                            style="color: #2563eb;"
                            :to="{name: 'Produces', params: { farmerId: getCurrentUserId() }}"
                        >
                          Add produce first
                        </router-link>
                      </v-alert>
                    </div>

                    <v-combobox
                        v-model="listing.unit"
                        :items="units"
                        label="Unit of Measurement"
                        prepend-inner-icon="mdi-scale-balance"
                        dense
                        solo
                        class="data-input"
                    ></v-combobox>
                  </div>
                </div>

                <!-- Quantity & Pricing Section -->
                <div class="tw-mb-6">
                  <h3 class="tw-text-sm tw-font-bold tw-text-gray-700 tw-mb-3 tw-flex tw-items-center tw-uppercase tw-tracking-wide">
                    <v-icon class="tw-mr-2 tw-text-green-600">mdi-calculator</v-icon>
                    Quantity & Pricing
                  </h3>

                  <div class="tw-grid md:tw-grid-cols-3 tw-gap-4">
                    <v-text-field
                        v-model="listing.quantity"
                        :label="`Quantity (${listing.unit})`"
                        type="number"
                        prepend-inner-icon="mdi-package-variant"
                        min="1"
                        :rules="[v => v > 0 || 'Quantity must be greater than 0']"
                        dense
                        solo
                        class="data-input"
                    ></v-text-field>

                    <vuetify-money
                        v-model="listing.price.price"
                        :label="`Price per ${listing.unit}`"
                        prepend-inner-icon="mdi-currency-usd"
                        :options="{
                        locale: 'en-US',
                        prefix: 'Ksh. ',
                        length: 11,
                        precision: 2,
                      }"
                        class="data-input"
                    ></vuetify-money>

                    <vuetify-money
                        label="Total Amount"
                        readonly
                        prepend-inner-icon="mdi-cash-multiple"
                        :value="listing.price.price * listing.quantity"
                        :options="vuetifyMoneyOptions"
                        class="data-input"
                    ></vuetify-money>
                  </div>
                </div>

                <!-- Photo Section -->
                <div class="tw-mb-6">
                  <h3 class="tw-text-sm tw-font-bold tw-text-gray-700 tw-mb-3 tw-flex tw-items-center tw-uppercase tw-tracking-wide">
                    <v-icon class="tw-mr-2 tw-text-green-600">mdi-camera</v-icon>
                    Product Photos
                  </h3>

                  <photo-gallery
                      ref="photoGallery"
                      :photos="listing.photos"
                      @delete-photo="deletePhoto"
                      @add-photo="photoCaptureDialog = true"
                      class="tw-border tw-border-gray-200 tw-rounded-lg tw-p-4 tw-bg-white"
                  />
                </div>

                <!-- Action Button -->
                <div class="tw-flex tw-justify-center tw-pt-6">
                  <v-btn
                      size="x-large"
                      rounded
                      :loading="loading"
                      @click="postListing"
                      class="btn-gradient"
                      elevation="0"
                  >
                    <v-icon class="tw-mr-2">mdi-upload</v-icon>
                    Post Listing
                  </v-btn>
                </div>
              </v-form>
            </v-card-text>
          </v-card>
        </div>

        <!-- Right Panel - Preview & Tips -->
        <div class="tw-space-y-6">
          <!-- Listing Preview -->
          <v-card class="glass-surface tw-rounded-2xl tw-overflow-hidden">
            <div class="tw-bg-gradient-to-r tw-from-blue-600 tw-to-indigo-600 tw-p-4">
              <h3 class="tw-text-base tw-font-semibold tw-text-white tw-flex tw-items-center">
                <v-icon class="tw-mr-2 tw-text-white">mdi-eye</v-icon>
                Preview
              </h3>
            </div>

            <v-card-text class="tw-p-5">
              <div class="tw-space-y-4">
                <div class="tw-flex tw-items-center tw-justify-between">
                  <span class="tw-text-xs tw-text-gray-600">Product:</span>
                  <span class="tw-font-medium">{{ selectedProduce?.name || 'Not selected' }}</span>
                </div>

                <div class="tw-flex tw-items-center tw-justify-between">
                  <span class="tw-text-xs tw-text-gray-600">Quantity:</span>
                  <span class="tw-font-medium">{{ listing.quantity }} {{ listing.unit }}</span>
                </div>

                <div class="tw-flex tw-items-center tw-justify-between">
                  <span class="tw-text-xs tw-text-gray-600">Unit Price:</span>
                  <span class="tw-font-medium tw-text-green-600">Ksh. {{ listing.price.price }}</span>
                </div>

                <div class="tw-flex tw-items-center tw-justify-between tw-border-t tw-pt-2">
                  <span class="tw-text-xs tw-font-semibold tw-text-gray-800">Total:</span>
                  <span class="tw-font-bold tw-text-base tw-text-green-600">
                    Ksh. {{ (listing.price.price * listing.quantity).toFixed(2) }}
                  </span>
                </div>

                <div class="tw-flex tw-items-center tw-justify-between">
                  <span class="tw-text-xs tw-text-gray-600">Photos:</span>
                  <span class="tw-font-medium">{{ listing.photos.length }} uploaded</span>
                </div>
              </div>
            </v-card-text>
          </v-card>

          <!-- Tips Card -->
          <v-card class="glass-surface tw-rounded-2xl tw-overflow-hidden">
            <div class="tw-bg-gradient-to-r tw-from-amber-500 tw-to-orange-500 tw-p-4">
              <h3 class="tw-text-base tw-font-semibold tw-text-white tw-flex tw-items-center">
                <v-icon class="tw-mr-2 tw-text-white">mdi-lightbulb</v-icon>
                Tips for Success
              </h3>
            </div>

            <v-card-text class="tw-p-5">
              <div class="tw-space-y-4">
                <div class="tw-flex tw-items-start tw-space-x-3">
                  <v-icon class="tw-text-green-500 tw-mt-1">mdi-check-circle</v-icon>
                  <p class="tw-text-sm tw-text-gray-700">Add clear, high-quality photos of your produce</p>
                </div>

                <div class="tw-flex tw-items-start tw-space-x-3">
                  <v-icon class="tw-text-green-500 tw-mt-1">mdi-check-circle</v-icon>
                  <p class="tw-text-sm tw-text-gray-700">Set competitive prices based on market rates</p>
                </div>

                <div class="tw-flex tw-items-start tw-space-x-3">
                  <v-icon class="tw-text-green-500 tw-mt-1">mdi-check-circle</v-icon>
                  <p class="tw-text-sm tw-text-gray-700">Ensure accurate quantity measurements</p>
                </div>

                <div class="tw-flex tw-items-start tw-space-x-3">
                  <v-icon class="tw-text-green-500 tw-mt-1">mdi-check-circle</v-icon>
                  <p class="tw-text-sm tw-text-gray-700">Update your listing regularly for freshness</p>
                </div>
              </div>
            </v-card-text>
          </v-card>
        </div>
      </div>
    </div>

    <!-- Photo Capture Dialog -->
    <v-dialog
      v-model="photoCaptureDialog"
      :max-width="$vuetify.breakpoint.smAndDown ? 'calc(100vw - 24px)' : '500px'"
      :fullscreen="$vuetify.breakpoint.xsOnly"
    >
      <PhotoCapture
        @captured-photos="handleCapturedPhotos"
        @close="photoCaptureDialog = false"
      />
    </v-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import HelperMixins from '@/mixins/helperMixins.js';
import { getCurrentUserId } from '@/utils/roles.js';
import validations from '@/utils/validations.js';
import PhotoGallery from '@/components/pictures/PhotoGallery.vue';
import PhotoCapture from '@/components/pictures/PhotoCapture.vue';

export default {
  name: 'CreateListing',
  components: {
    PhotoCapture,
    PhotoGallery,
  },
  mixins: [HelperMixins],
  data() {
    return {
      units: ['KG', 'L', 'Bunch', 'Crate'],
      selectedProduce: null,
      selectedYield: null,
      availableYields: [],
      farmer: {
        id: 'string',
        name: 'string',
        email: 'string',
        phoneNumber: 'string',
        createdAt: '2025-01-25T09:38:38.536Z',
        location: {
          latitude: 0.0,
          longitude: 0.0,
          customName: '',
        },
        farmerProduces: [
          {
            id: 'string',
            farmer: 'string',
            farmProduce: {
              id: 'string',
              name: 'string',
              description: 'string',
              farmingType: 'string',
              status: 'INACTIVE',
            },
            status: 'INACTIVE',
          },
        ],
      },
      listing: {
        farmerProduceId: '',
        quantity: 1,
        price: {
          price: 0,
          currency: 'KSH',
        },
        unit: 'KG',
        photos: [],
      },
      photoCaptureDialog: false,
      imagePreviews: [],
      isValid: false,
      ...validations,
      loading: false,
    };
  },
  mounted() {
    axios.get(`/farmers-service/farmer?farmerId=${getCurrentUserId()}`).then((response) => {
      if (response.data.data == null) throw Error('User not found');
      this.farmer = { ...this.farmer, ...response.data.data };
    }).catch((e) => {
      this.$toast.error(e.message);
    });
  },
  computed: {
    getCurrentUserId,
    formattedFarmerProduces() {
      return this.farmer.farmerProduces.map((item) => ({
        id: item.id,
        name: item.farmProduce.name,
      }));
    },
    vuetifyMoneyOptions() {
      return {
        locale: 'en-US',
        prefix: 'Ksh. ',
        length: 11,
        precision: 2,
      };
    },
  },
  watch: {
    selectedProduce(newProduce) {
      if (newProduce && newProduce.id) {
        this.listing.farmerProduceId = newProduce.id;
      } else {
        this.listing.farmerProduceId = '';
      }
    },
  },
  methods: {
    async farmProduceListingChanged(farmProduce) {
      if (!farmProduce) return;
      this.selectedProduce = farmProduce;
      this.units = ['KG', 'L', 'Bunch', 'Crate'];
      this.listing.unit = 'KG';
      this.listing.farmerProduceId = farmProduce.id;
      this.selectedYield = null;
      this.availableYields = [];
      // Fetch available yields for this produce
      try {
        const { data } = await axios.get(`/api/yields/farmer-produce/${farmProduce.id}`);
        this.availableYields = (data.data || []).map((y) => ({
          ...y,
          display: `${y.yieldAmount} ${y.yieldUnit} (${this.formatDate(y.harvestDate)})`,
        })).filter((y) => y.remainingAmount > 0);
      } catch {
        this.availableYields = [];
      }
    },
    yieldSelectionChanged(yieldObj) {
      if (!yieldObj) return;
      this.selectedYield = yieldObj;
      this.listing.unit = yieldObj.yieldUnit;
      this.listing.quantity = yieldObj.remainingAmount;
      this.listing.farmerProduceId = yieldObj.farmerProduceId;
      // Optionally pre-fill price or other fields
    },
    formatDate(date) {
      if (!date) return '-';
      try { return new Date(date).toLocaleDateString(); } catch { return String(date); }
    },
    handleCapturedPhotos(photos) {
      this.listing.photos = [...photos];
      this.photoCaptureDialog = false;
    },
    deletePhoto(photoId) {
      const index = this.listing.photos.findIndex((photo) => photo.id === photoId);
      if (index > -1) {
        URL.revokeObjectURL(this.listing.photos[index].url);
        this.listing.photos.splice(index, 1);
      }
    },
    previewImages() {
      this.imagePreviews = [];
      if (this.listing.images) {
        this.listing.images.forEach((file) => {
          this.imagePreviews.push(URL.createObjectURL(file));
        });
      }
    },
    validateImages(value) {
      // eslint-disable-next-line no-mixed-operators
      return value.length >= 1 && value.length <= 3 || 'You must upload between 1 and 3 images';
    },
    async postListing() {
      // Validate against yield availability if yield is selected
      if (this.selectedYield && Number(this.listing.quantity) > Number(this.selectedYield.remainingAmount)) {
        this.$toast.error('Listing quantity exceeds available yield amount.');
        return;
      }

      // Specific field validation with better error messages
      if (!this.selectedProduce || !this.selectedProduce.id) {
        this.$toast.error('Please select a farm produce');
        return;
      }
      if (!this.listing.price.price || this.listing.price.price <= 0) {
        this.$toast.error('Please enter a valid price greater than 0');
        return;
      }
      if (!this.listing.quantity || this.listing.quantity <= 0) {
        this.$toast.error('Please enter a valid quantity greater than 0');
        return;
      }
      if (!this.isValid) {
        this.$toast.error('Please correct the form errors before submitting');
        return;
      }

      this.loading = true;
      try {
        const formData = new FormData();
        formData.append('farmerProduceId', this.listing.farmerProduceId);
        formData.append('unit', this.listing.unit);
        formData.append('quantity', this.listing.quantity);
        formData.append('price', this.listing.price.price);
        formData.append('currency', this.listing.price.currency);
        if (this.selectedYield) {
          formData.append('produceYieldId', this.selectedYield.id);
        }

        if (this.listing.photos.length) {
          // Prefer blobs/files; fallback to fetching blob URLs
          const fileEntries = await Promise.all(this.listing.photos.map(async (photo, idx) => {
            const filename = photo?.name || `listing-photo-${idx + 1}.jpg`;
            try {
              if (photo?.blob) return { file: photo.blob, name: filename };
              if (photo?.file) return { file: photo.file, name: filename };
              if (photo?.url && photo.url.startsWith('blob:')) {
                const res = await fetch(photo.url);
                const blob = await res.blob();
                return { file: blob, name: filename };
              }
              if (photo?.url) return { url: photo.url };
            } catch (e) {
              return null;
            }
            return null;
          }));
          fileEntries.forEach((entry) => {
            if (!entry) return;
            if (entry.file) formData.append('images', entry.file, entry.name);
            else if (entry.url) formData.append('images', entry.url);
          });
        }

        const response = await axios.post('/farmers-service/listing', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });

        if (response.data.success) {
          this.farmerProduces = response.data.data;
          this.$toast.success('Listing posted successfully!');
          // Reset form
          this.resetForm();
        } else {
          this.$toast.error(`Failed to post listing: ${response.data.msg}`);
        }
      } catch (error) {
        this.$toast.error(`Error posting listing: ${error.message}`);
      } finally {
        this.loading = false;
      }
    },
    resetForm() {
      this.selectedProduce = null;
      this.listing = {
        farmerProduceId: '',
        quantity: 1,
        price: {
          price: 0,
          currency: 'KSH',
        },
        unit: 'KG',
        photos: [],
      };
    },
  },
};
</script>

<style scoped>
/* Match the glass theme used elsewhere */
.glass-surface {
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: saturate(180%) blur(12px);
  border: 1px solid rgba(255,255,255,0.7);
}

.data-input {
  background-color: #FEEED5 !important;
  border-radius: 10px !important;
  margin: 5px 0 !important;
}

.btn-gradient {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 35%, #0ea5e9 100%) !important;
  color: #ffffff !important;
  border: none !important;
  box-shadow: 0 8px 18px rgba(34,197,94,0.25) !important;
}

.btn-gradient:hover { filter: brightness(1.05); transform: translateY(-1px); }
</style>
