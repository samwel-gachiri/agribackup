<template>
  <Default>
    <div class="tw-px-4">
      <div>
        <spacer></spacer>
        <v-btn
            color="primary"
            @click="$router.push({name: 'FarmerOrders'})"
          >View Your Request Bids</v-btn>
      </div>
      <!-- Search Bar -->
      <v-text-field
          v-model="searchQuery"
          label="Search for a produce..."
          variant="outlined"
          class="tw-mb-4"
          prepend-inner-icon="mdi-magnify"
          clearable
      ></v-text-field>

      <!-- Requests Grid with Skeleton Loader -->
      <v-row>
        <template v-if="loading">
          <v-col v-for="n in 5" :key="n" cols="12" sm="6" md="4" lg="3">
            <v-skeleton-loader type="card" class="tw-rounded-2xl tw-shadow-lg"/>
          </v-col>
        </template>

        <template v-else>
          <v-col v-for="request in filteredRequests" :key="request.id" cols="12" sm="6" md="4" lg="3">
            <v-card class="tw-rounded-2xl tw-shadow-lg hover:tw-shadow-xl tw-transition tw-duration-300">
              <!-- Image -->
<!--                  :src="request.preferredProduce.imageUrls[0] || 'https://via.placeholder.com/200'"-->
              <v-img
                  height="200"
                  cover
                  class="tw-rounded-t-2xl"
              >
                <p>Posted: <span class="tw-font-bold">{{ formatDate(request.dateCreated) }}</span></p>
              </v-img>

              <!-- Card Details -->
              <v-card-title class="tw-text-lg tw-font-semibold">{{ request.preferredProduce.bsfarmProduce.name }}</v-card-title>
              <v-card-text class="tw-text-gray-600">
                <p>Price: <span class="tw-font-bold">{{ request.price.price }} {{ request.price.currency }}</span></p>
                <p>Quantity: <span class="tw-font-bold">{{ request.quantity }} {{ request.unit }}</span></p>
              </v-card-text>

              <!-- Actions -->
              <v-card-actions>
                <v-btn color="primary" class="tw-w-full" variant="flat" @click="openDetails(request)">
                  View Details
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-col>
        </template>
      </v-row>
    </div>

    <!-- Dialog for Details & Ordering -->
    <v-dialog v-model="detailsDialog" max-width="700px">
      <v-card class="tw-rounded-xl tw-p-6 tw-shadow-lg">
        <v-card-text class="tw-flex md:tw-flex-row tw-flex-col">
          <div>
            <!-- Product Details -->
            <h2 class="tw-text-2xl tw-font-bold tw-text-center tw-mb-3">
              {{ requestToOrder?.preferredProduce.bsfarmProduce.name }}
            </h2>
            <div class="tw-bg-gray-100 tw-p-4 tw-rounded-lg">
              <p class="tw-text-gray-700"><span class="tw-font-bold">Description:</span> {{ requestToOrder?.preferredProduce.bsfarmProduce.description }}</p>
              <p class="tw-text-gray-700"><span class="tw-font-bold">Farming Type:</span> {{ requestToOrder?.preferredProduce.farmingType }}</p>
            </div>
            <!-- Order Section -->
            <div class="tw-bg-blue-50 tw-p-4 tw-rounded-lg tw-text-center">
              <h3 class="tw-text-xl tw-font-bold tw-text-blue-700">Place Your Bid</h3>
              <number-input
                  v-model="orderQuantity"
                  :label=" requestToOrder? `Enter quantity in ${pluralize(requestToOrder?.unit)}`: ''"
                  class="tw-mt-2"
                  :initial-value="1"
                  :maximum-value="requestToOrder?.quantity"
              ></number-input>
              <v-btn color="primary" class="tw-w-full tw-mt-2" :loading="loading" @click="orderRequest">
                Bid now
              </v-btn>
            </div>
          </div>
          <!-- Image Carousel -->
          <v-carousel show-arrows hide-delimiters height="300px">
            <v-carousel-item
                v-for="(img, index) in requestToOrder?.preferredProduce.imageUrls"
                :key="index"
                :src="img"
                class="tw-rounded-lg tw-object-cover"
            ></v-carousel-item>
          </v-carousel>
        </v-card-text>

        <v-card-actions class="tw-justify-end tw-mt-4">
          <v-btn color="red" variant="text" @click="detailsDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </Default>
</template>

<script>
import axios from 'axios';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import pluralize from 'pluralize';
import Default from '@/components/layout/Default.vue';
import NumberInput from '@/components/shared/NumberInput.vue';
import { getCurrentUserId } from '@/utils/roles.js';

// Extend dayjs with relativeTime
dayjs.extend(relativeTime);

export default {
  components: { NumberInput, Default },
  data() {
    return {
      requests: [],
      searchQuery: '',
      detailsDialog: false,
      requestToOrder: null,
      orderQuantity: '',
      loading: false,
    };
  },
  computed: {
    filteredRequests() {
      return this.requests.filter((request) => request.preferredProduce.bsfarmProduce.name?.toLowerCase().includes(this.searchQuery?.toLowerCase())
          || request.preferredProduce.bsfarmProduce.description?.toLowerCase().includes(this.searchQuery?.toLowerCase())
          || request.preferredProduce.description?.toLowerCase().includes(this.searchQuery?.toLowerCase())
          || request.preferredProduce.farmingType?.toLowerCase().includes(this.searchQuery?.toLowerCase()));
    },
    getCurrentUserId,
  },
  mounted() {
    this.fetchRequests();
  },
  methods: {
    pluralize,
    formatDate(date) {
      return dayjs(date).fromNow(); // Converts raw date to "x time ago"
    },
    async fetchRequests() {
      this.loading = true;
      try {
        const response = await axios.get('/buyers-service/request/list');
        this.requests = response.data.data;
      } catch (error) {
        this.$toast.error('Error fetching requests:', error);
      } finally {
        this.loading = false;
      }
    },
    openDetails(request) {
      this.requestToOrder = request;
      this.detailsDialog = true;
    },
    async orderRequest() {
      this.loading = true;
      const quantity = Number(this.orderQuantity);

      if (!Number.isFinite(quantity) || quantity <= 0) {
        this.$toast.error('Enter a valid quantity.');
        this.loading = false;
        return;
      }

      try {
        const response = await axios.post('/buyers-service/request/order', {
          requestId: this.requestToOrder.id,
          farmerId: this.getCurrentUserId,
          quantity,
        });

        if (response.data.success) {
          this.$toast.success('Order placed successfully!');
          this.detailsDialog = false;
          this.requestToOrder = null;
          this.orderQuantity = '';
        } else {
          this.$toast.error(response.data.msg);
        }
      } catch (error) {
        this.$toast.error(`Error placing order: ${error.response?.data?.msg || error.message}`);
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style>
/* Additional Tailwind styles */
</style>
