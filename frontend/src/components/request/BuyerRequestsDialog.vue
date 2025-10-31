<template>
  <v-dialog v-model="isOpen" max-width="800px">
    <v-card v-if="selectedBuyer">
      <v-card-title>
        <span class="text-h5">{{ selectedBuyer.buyer.name }}</span>
      </v-card-title>

      <v-card-subtitle>
        Location: {{ selectedBuyer.customName }}
      </v-card-subtitle>

      <v-card-text>
        <v-row class="tw-mb-4">
          <v-col cols="6">
            <v-btn :color="isConnected ? 'white' : 'green'" @click="toggleConnection" :disabled="getCurrentUserRole() !== 'farmer'">
              {{ isConnected ? "Connected" : "Connect" }}
              <v-icon v-if="isConnected" color="primary"> mdi-check-circle</v-icon>
            </v-btn>
          </v-col>
        </v-row>

        <v-divider></v-divider>

        <h3 class="mt-4">Products</h3>
        <v-chip-group v-if="selectedBuyer.buyer.preferredProduces.length > 0">
          <v-chip v-for="product in selectedBuyer.buyer.preferredProduces" :key="product.id" color="primary">
            {{ product.bsfarmProduce.name }}
          </v-chip>
        </v-chip-group>
        <p v-else>No products available</p>

        <v-divider class="my-4"></v-divider>

        <h3>Requests</h3>
        <v-data-table
            :headers="headers"
            :items="requests.filter((request) => request.status === 'ACTIVE')"
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
      <v-card v-if="requestToOrder">
        <v-card-title>
          <span class="text-h5">Order Request</span>
        </v-card-title>

        <v-card-subtitle>
          Produce: {{ requestToOrder.preferredProduce.bsfarmProduce.name }}
        </v-card-subtitle>

        <v-card-text>
          <v-row class="tw-mb-4">
            <v-col cols="6">
              <number-input
                  :label="`Quantity (in ${requestToOrder.unit})`"
                  v-model="orderQuantity"
                  :min="1"
                  :initial-value="1"
              ></number-input>
            </v-col>
          </v-row>
          <v-row class="tw-mb-4">
            <v-col cols="6">
              <v-btn color="primary" :loading="loading" @click="orderRequest" :disabled="getCurrentUserRole() !== 'farmer'">
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
    selectedBuyer: Object,
  },
  data() {
    return {
      loading: false,
      isOpen: false,
      requests: [],
      buyerProducts: [],
      isConnected: false,
      requestToOrder: null,
      orderDialog: false,
      orderQuantity: 0,
      headers: [
        { text: 'Produce', value: 'preferredProduce.bsfarmProduce.name' },
        { text: 'Quantity', value: 'quantity' },
        { text: 'Unit', value: 'unit' },
        { text: 'Price', value: 'price.price' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
    };
  },
  watch: {
    selectedBuyer: {
      immediate: true,
      handler(newBuyer) {
        if (newBuyer) {
          this.isOpen = true;
          this.fetchRequests();
          if (getCurrentUserRole() === 'farmer') {
            this.checkConnection();
          }
        }
      },
    },
  },
  methods: {
    getCurrentUserId,
    getCurrentUserRole,
    async fetchRequests() {
      try {
        const response = await axios.get('/buyers-service/request/buyer', {
          params: { buyerId: this.selectedBuyer.buyer.id, pageable: { page: 0, size: 10 } },
        });
        this.requests = response.data.data.content || [];
      } catch (error) {
        this.$toast.error('Error fetching requests:', error.message);
      }
    },
    //
    // async fetchBuyerProducts() {
    //   try {
    //     const response = await axios.get('/buyers-service/products', {
    //       params: { buyerId: this.selectedBuyer.buyer.id },
    //     });
    //     this.buyerProducts = response.data.data || [];
    //   } catch (error) {
    //   }
    // },

    async checkConnection() {
      try {
        const response = await axios.get(`/connection-service/${getCurrentUserRole()}-connections/${getCurrentUserId()}`);
        if (response.data.success === false) {
          this.$toast.error('Error checking connection:', response.data.msg);
        }
        const connectedBuyers = response.data.data || [];
        this.isConnected = connectedBuyers.some((f) => f.buyerId === this.selectedBuyer.buyer.id);
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
          params: { buyerId: this.selectedBuyer.buyer.id, farmerId: getCurrentUserId() },
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
    popUpOrderDialog(request) {
      this.requestToOrder = request;
      this.orderDialog = true;
    },
    async orderRequest() {
      this.loading = true;
      const quantity = Number(this.orderQuantity);
      if (!Number.isFinite(quantity) || quantity <= 0) {
        this.$toast.error('Enter a valid quantity.');
        return;
      }

      try {
        const response = await axios.post('/buyers-service/request/order', {
          requestId: this.requestToOrder.id,
          buyerId: getCurrentUserId(),
          quantity,
        });
        if (response.data.success) {
          this.$toast.success('Order placed successfully!');
          this.orderDialog = false;
          this.requestToOrder = null;
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
