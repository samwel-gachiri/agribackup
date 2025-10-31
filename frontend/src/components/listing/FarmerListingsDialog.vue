<template>
  <v-dialog v-model="isOpen" max-width="800px">
    <v-card v-if="selectedFarmer">
      <v-card-title>
        <span class="text-h5">{{ selectedFarmer.farmer?.name }}</span>
      </v-card-title>

      <v-card-subtitle>
        Location: {{ selectedFarmer.customName }}
      </v-card-subtitle>

      <v-card-text>
        <v-row class="tw-mb-4">
          <v-col cols="6">
            <v-btn :color="isConnected ? 'white' : 'green'" @click="toggleConnection" :disabled="getCurrentUserRole() !== 'buyer'">
              {{ isConnected ? "Connected" : "Connect" }}
              <v-icon v-if="isConnected" color="primary"> mdi-check-circle</v-icon>
            </v-btn>
          </v-col>
        </v-row>

        <v-divider></v-divider>

        <h3 class="mt-4">Products</h3>
        <v-chip-group v-if="selectedFarmer.farmer.farmerProduces.length > 0">
          <v-chip v-for="product in selectedFarmer.farmer.farmerProduces" :key="product.id" color="primary">
            {{ product.farmProduce.name }}
          </v-chip>
        </v-chip-group>
        <p v-else>No products available</p>

        <v-divider class="my-4"></v-divider>

        <h3>Listings</h3>
        <v-data-table
            :headers="headers"
            :items="listings.filter((listing) => listing.status === 'ACTIVE')"
            item-value="id"
            class="elevation-1"
        >
          <template v-slot:item.actions="{ item }">
            <v-btn color="primary" @click="popUpOrderDialog(item)">Order</v-btn>
          </template>
        </v-data-table>
      </v-card-text>

      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="gray" @click="closeDialog">Close</v-btn>
      </v-card-actions>
    </v-card>
    <v-dialog v-model="orderDialog" max-width="500px">
      <v-card v-if="listingToOrder">
        <v-card-title>
          <span class="text-h5">Order Listing</span>
        </v-card-title>

        <v-card-subtitle>
          Produce: {{ listingToOrder.farmerProduce.farmProduce.name }}
        </v-card-subtitle>

        <v-card-text>
          <v-row class="tw-mb-4">
            <v-col cols="6">
              <number-input
                  :label="`Quantity (in ${listingToOrder.unit})`"
                  v-model="orderQuantity"
                  :min="1"
                  :initial-value="1"
              ></number-input>
            </v-col>
          </v-row>
          <v-row class="tw-mb-4">
            <v-col cols="6">
              <v-btn color="primary" :loading="loading" @click="orderListing" :disabled="getCurrentUserRole() !== 'buyer'">
                Order Now
              </v-btn>
            </v-col>
          </v-row>
        </v-card-text>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="gray" @click="orderDialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-dialog>
</template>

<script>
import axios from 'axios';
import { getCurrentUserId, getCurrentUserRole } from '@/utils/roles.js';
import NumberInput from '@/components/shared/NumberInput.vue';

export default {
  components: { NumberInput },
  props: {
    selectedFarmer: {
      type: Object,
    },
  },
  data() {
    return {
      loading: false,
      isOpen: false,
      listings: [],
      farmerProducts: [],
      isConnected: false,
      listingToOrder: null,
      orderDialog: false,
      orderQuantity: 0,
      headers: [
        { text: 'Produce', value: 'farmerProduce.farmProduce.name' },
        { text: 'Quantity', value: 'quantity' },
        { text: 'Unit', value: 'unit' },
        { text: 'Price', value: 'price.price' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
    };
  },
  watch: {
    selectedFarmer: {
      immediate: true,
      handler(newFarmer) {
        if (newFarmer) {
          this.isOpen = true;
          this.fetchListings();
          if (getCurrentUserRole() === 'buyer') {
            this.checkConnection();
          }
        }
      },
    },
  },
  methods: {
    getCurrentUserId,
    getCurrentUserRole,
    async fetchListings() {
      try {
        const response = await axios.get('/farmers-service/listing/farmer', {
          params: { farmerId: this.selectedFarmer.farmer.id, pageable: { page: 0, size: 10 } },
        });
        this.listings = response.data.data.content || [];
      } catch (error) {
        this.$toast.error('Error fetching listings:', error.message);
      }
    },
    //
    // async fetchFarmerProducts() {
    //   try {
    //     const response = await axios.get('/farmers-service/products', {
    //       params: { farmerId: this.selectedFarmer.farmer.id },
    //     });
    //     this.farmerProducts = response.data.data || [];
    //   } catch (error) {
    //   }
    // },

    async checkConnection() {
      try {
        const response = await axios.get(`/connection-service/${getCurrentUserRole()}-connections/${getCurrentUserId()}`);
        if (response.data.success === false) {
          this.$toast.error('Error checking connection:', response.data.msg);
        }
        const connectedFarmers = response.data.data || [];
        this.isConnected = connectedFarmers.some((f) => f.farmerId === this.selectedFarmer.farmer.id);
      } catch (error) {
        this.$toast.error('Error checking connection:', error.message);
      }
    },

    async toggleConnection() {
      const url = this.isConnected ? '/connection-service/disconnect' : '/connection-service/connect';
      try {
        const response = await axios({
          method: this.isConnected ? 'DELETE' : 'POST',
          url,
          params: { farmerId: this.selectedFarmer.farmer.id, buyerId: getCurrentUserId() },
        });
        if (response.data.success) {
          this.isConnected = !this.isConnected;
          const msg = this.isConnected ? 'Connected Successfully' : 'Disconnected successfully!';
          this.$toast.success(msg);
        }
      } catch (error) {
        this.$toast.error('Error updating connection:', error.message);
      }
    },
    popUpOrderDialog(listing) {
      this.listingToOrder = listing;
      this.orderDialog = true;
    },
    async orderListing() {
      this.loading = true;
      const quantity = Number(this.orderQuantity);
      if (!Number.isFinite(quantity) || quantity <= 0) {
        this.$toast.error('Enter a valid quantity.');
        return;
      }

      try {
        const response = await axios.post('/farmers-service/listing/order', {
          listingId: this.listingToOrder.id,
          buyerId: getCurrentUserId(),
          quantity,
        });
        if (response.data.success) {
          this.$toast.success('Order placed successfully!');
          this.orderDialog = false;
          this.listingToOrder = null;
        } else {
          this.$toast.error(response.data.msg);
        }
      } catch (error) {
        this.$toast.error('Error placing order:', error.message);
      } finally {
        this.loading = false;
      }
    },

    closeDialog() {
      this.isOpen = false;
      this.$emit('close');
    },
  },
};
</script>
