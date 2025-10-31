<template>
  <v-card>
    <v-card-title>
      <card-title>
        <span class="text-xl font-bold">Request Details</span>
      </card-title>
    </v-card-title>
    <v-card-text>
      <div v-if="request">
        <p><strong>ID:</strong> {{ request.produceRequest.id }}</p>
        <p><strong>Price:</strong> {{ request.produceRequest.price.price.toLocaleString() }} {{ request.produceRequest.price.currency }} per {{request.produceRequest.unit}}</p>
        <p><strong>Earnings:</strong> {{ request.earnings }} {{request.produceRequest.price.currency}}</p>
<!--        <p><strong>Rating:</strong> {{ request.produceRequest.rating }}</p>-->
<!--        <p><strong>Status:</strong> {{ request.produceRequest.status }}</p>-->
        <p><strong>Quantity Sold:</strong> {{ request.quantitySold }} {{ request.produceRequest.unit }} out of {{ request.quantityLeft }} {{ request.produceRequest.unit }}</p>
        <p><strong>No of purchases:</strong> {{ request.noOfPurchases }}</p>

        <v-divider class="my-4"></v-divider>

        <h3 class="text-lg font-bold mb-2">Bids From Farmers</h3>
        <v-data-table
            :headers="orderHeaders"
            :items="request.produceRequest.requestOrders"
            class="elevation-1"
        >
          <template v-slot:item.actions="{ item }">
            <v-btn v-if="item.status === 'PENDING_ACCEPTANCE'" small color="success" @click="acceptOrder(item)">Accept</v-btn>
            <v-btn v-if="item.status === 'BOOKED_FOR_SUPPLY'" small color="success" @click="confirmSupply(item)">confirm supply</v-btn>
          </template>
        </v-data-table>
      </div>
    </v-card-text>
  </v-card>
</template>

<script>
import axios from 'axios';
import CardTitle from '@/components/shared/CardTitle.vue';

export default {
  name: 'Request',
  components: { CardTitle },
  data() {
    return {
      orderHeaders: [
        { text: 'Farmer ID', value: 'farmerId' },
        { text: 'Date', value: 'dateCreated' },
        { text: 'Quantity', value: 'quantity' },
        { text: 'Date Accepted', value: 'dateAccepted' },
        { text: 'Date Supplied', value: 'dateSupplied' },
        { text: 'Date Paid', value: 'datePaid' },
        { text: 'Status', value: 'status' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      request: {
        quantitySold: 2,
        quantityLeft: 8,
        earnings: 200,
        noOfPurchases: 1,
        produceRequest: {
          id: '68d2ad3f-8042-4089-bb97-557bb670580a',
          quantity: 10,
          price: {
            price: 100,
            currency: 'KSH',
          },
          unit: 'pineapples',
          rating: 0,
          status: 'ACTIVE',
          buyerProduce: {
            id: '3cf68e23-a729-40fc-affa-a5e1be7a5473',
            farmProduce: {
              id: 'f209e2c3-b35e-497a-8ffe-8d5b8f0ec7f6',
              name: 'pineapple',
              description: '',
              farmingType: '',
              status: 'ACTIVE',
            },
            status: 'ON_SALE',
          },
          requestOrders: [
            {
              id: '3f4e3ba6-ab11-4ee2-9b27-cff10420aac6',
              buyerId: '8dd3971f-b1b8-45c7-aedd-15f33e5b6cbf',
              dateCreated: '2025-02-11T07:56:31',
              dateAccepted: '2025-02-11T08:01:44',
              dateSupplied: null,
              datePaid: null,
              quantity: 2,
              status: 'BOOKED_FOR_SUPPLY',
            },
          ],
        },
      },
      loading: false,
      intervalId: null,
      eventSource: null,
    };
  },
  props: {
    requestId: {
      type: String,
      default: '',
    },
  },
  mounted() {
    // this.intervalId = setInterval(() => {
    // }, 3000);
    this.fetchRequest();
  },
  beforeDestroy() {
    clearInterval(this.intervalId);
    if (this.eventSource) {
      this.eventSource.close();
    }
  },
  methods: {
    connectToSSE() {
      this.eventSource = new EventSource(
        `${process.env.VUE_APP_API_BASE_URL}buyers-service/request/stream?requestId=${this.request.produceRequest.id}`,
      );

      // Listen for custom event types
      this.eventSource.addEventListener('order-added-to-request', () => {
        this.$toast.success('Order has been made to one of your request', 'New order!!!');
        this.fetchRequest();
      });

      // Listen for custom event types
      this.eventSource.addEventListener('supply-confirmed', (event) => {
        this.fetchRequest();
        this.$toast.success('Supply confirmed for an order', `Supply for ${event.data}`);
      });

      // Listen for custom event types
      this.eventSource.addEventListener('payment-confirmed', () => {
        this.$toast.success('Payment has been confirmed to one of your request orders', 'Finished!!');
        this.fetchRequest();
      });

      this.eventSource.onmessage = (event) => {
        this.$toast.error('Received event:', event.data);
        this.fetchRequest();
      };

      this.eventSource.onopen = () => {
        this.$toast.show('SSE connection opened');
      };

      // eslint-disable-next-line no-unused-vars
      this.eventSource.onerror = (error) => {
        // this.$toast.error('SSE error:', error.message);
        this.eventSource.close();
      };
    },
    async fetchRequest() {
      this.loading = true;
      try {
        const response = await axios.get('/buyers-service/request', {
          params: {
            requestId: this.requestId,
          },
        });
        this.request = response.data.data;
      } catch (error) {
        this.$toast.error('Error fetching request');
      } finally {
        this.loading = false;
        if (!this.eventSource) {
          this.connectToSSE();
        }
      }
    },
    // Accept an order
    acceptOrder(order) {
      axios.put(`/buyers-service/request/order/accept?orderId=${order.id}&requestId=${this.request.produceRequest.id}`).then((response) => {
        if (response.data.success === true) {
          this.$toast.success('Order accepted');
        } else {
          this.$toast.error(response.data.msg, 'Failed to accept order');
        }
      })
        .catch((error) => {
          this.$toast.error(error.message);
        })
        .finally(() => {
          this.fetchRequest();
        });
    },
    confirmSupply(order) {
      axios.put(`/buyers-service/request/order/confirm-supply?orderId=${order.id}&requestId=${this.request.produceRequest.id}`).then((response) => {
        if (response.data.success === true) {
          this.$toast.success('Supply confirmed', order.id);
        } else {
          this.$toast.error(response.data.msg, 'Failed to confirm supply');
        }
      })
        .catch((error) => {
          this.$toast.error(error.message);
        })
        .finally(() => {
          this.fetchRequest();
        });
    },
  },
};
</script>

<style scoped>

</style>
