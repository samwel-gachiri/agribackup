<template>
  <Default>
    <v-card>
      <v-card-title>My Bids</v-card-title>
      <v-card-text>
        <v-data-table :headers="headers" :items="orders" class="elevation-1">
          <template v-slot:item.dateCreated="{ item }">
            {{ item.dateCreated | formatToHumanWithTime }}
          </template>
          <template v-slot:item.status="{ item }">
            <v-chip :color="getStatusColor(item.status)" dark>
              {{ item.status }}
            </v-chip>
          </template>
          <template v-slot:item.actions="{ item }">
            <v-btn v-if="item.status === 'BOOKED_FOR_SUPPLY'" small color="success" disabled>confirm pay</v-btn>
            <v-btn
                v-if="item.status === 'SUPPLIED'"
                color="primary"
                @click="confirmPayment(item)">
              Confirm Pay
            </v-btn>
          </template>
          <template v-slot:item.view="{ item }">
            <v-btn color="secondary" @click="viewOrder(item)">
              <v-icon color="primary">mdi-notebook</v-icon>
              view
            </v-btn>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>
    <v-snackbar v-model="snackbar" :color="snackbarColor" top>
      {{ snackbarMessage }}
    </v-snackbar>
    <v-dialog v-model="dialog" width="600px" persistent>
      <v-card rounded="none" class="tw-p-4">
        <v-card-title class="tw-text-lg tw-font-bold">Bidding Details</v-card-title>
        <v-card-text>
          <p class="text-gray-700">Produce: <span class="tw-font-bold">{{ selectedOrder.produceName }}</span></p>
          <p class="text-gray-700">Quantity: <span class="tw-font-bold">{{ selectedOrder.quantity }}</span></p>
          <p class="text-gray-700">Status: <span class="tw-bg-green-800 tw-p-1 tw-text-white tw-rounded-lg" >{{ selectedOrder.status?.replaceAll('_', ' ') }}</span></p>
          <v-divider/>
          <v-timeline>
            <v-timeline-item v-if="selectedOrder.dateCreated" color="blue">
              <template v-slot:opposite>
                <span class="tw-text-gray-900 tw-font-semibold tw-tracking-wide">
                  Start
                </span>
              </template>
              <v-card rounded="xl" elevation="3" color="blue">
                <v-card-title class="text-h5">
                    <span class="tw-text-base tw-font-semibold">Order Created</span>
                </v-card-title>
                <v-card-text>
                  <h2>{{ selectedOrder.dateCreated | formatToHumanWithTime }}</h2>
                </v-card-text>
              </v-card>
            </v-timeline-item>
            <v-timeline-item v-if="selectedOrder.dateAccepted" color="green">
              <v-card rounded="xl" elevation="3" color="green">
                <v-card-title>
                  <span class="tw-text-base tw-font-semibold">Order Accepted</span>
                </v-card-title>
                <v-card-text>
                  <h2>{{ selectedOrder.dateAccepted | formatToHumanWithTime }}</h2>
                </v-card-text>
              </v-card>
            </v-timeline-item>
            <v-timeline-item v-if="selectedOrder.dateSupplied" color="orange">
              <v-card rounded="xl" elevation="3" color="orange">
                <v-card-title>
                  <span class="tw-text-base tw-font-semibold">Order Supplied</span>
                </v-card-title>
                <v-card-text>
                  <h2>{{ selectedOrder.dateSupplied | formatToHumanWithTime }}</h2>
                </v-card-text>
              </v-card>
            </v-timeline-item>
            <v-timeline-item v-if="selectedOrder.datePaid" color="purple">
              <template v-slot:opposite>
                <span class="tw-text-gray-900 tw-font-semibold tw-tracking-wide">
                  Completed
                </span>
              </template>
              <v-card rounded="xl" elevation="3" color="purple">
                <v-card-title class="">
                  <span class="tw-text-base tw-font-semibold">Payment Confirmed</span>
                </v-card-title>
                <v-card-text>
                  <h2 class="">{{ selectedOrder.datePaid | formatToHumanWithTime }}</h2>
                </v-card-text>
              </v-card>
            </v-timeline-item>
          </v-timeline>
        </v-card-text>
        <v-card-actions class="tw-justify-end tw-mt-4">
          <v-btn color="red" @click="dialog = false">Close</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </Default>
</template>

<script>
import axios from 'axios';
import { getCurrentUserId } from '@/utils/roles.js';
import Default from '@/components/layout/Default.vue';
import { formatToHumanWithTime } from '@/utils/time.js';

export default {
  components: { Default },
  data() {
    return {
      orders: [],
      headers: [
        { text: 'Produce Name', value: 'produceName' },
        { text: 'Quantity', value: 'quantity' },
        { text: 'Date ordered', value: 'dateCreated' },
        { text: 'Status', value: 'status' },
        { text: 'View', value: 'view', sortable: false },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      snackbar: false,
      snackbarMessage: '',
      snackbarColor: '',
      dialog: false,
      selectedOrder: {},
      eventSources: [],
      metaInfo: {
        title: 'My Orders',
      },
    };
  },
  created() {
    this.fetchOrders();
  },
  beforeDestroy() {
    Object.values(this.eventSources).forEach((source) => {
      source.close();
    });
  },
  methods: {
    formatToHumanWithTime,
    viewOrder(item) {
      this.selectedOrder = item;
      this.dialog = true;
    },
    setUpEventSources() {
      this.orders.forEach((order) => {
        const requestId = order.requestId;
        if (!this.eventSources[requestId]) {
          this.eventSources[requestId] = new EventSource(
            `${process.env.VUE_APP_API_BASE_URL}buyers-service/request/stream?requestId=${order.requestId}`,
            // `${process.env.VUE_APP_API_BASE_URL}/request/stream-sse`,
          );

          // Listen for custom event types
          this.eventSources[requestId].addEventListener('order-accepted', () => {
            this.$toast.success('One of your orders has been accepted for supply!', 'Hurray!');
            this.fetchOrders();
          });

          // Listen for custom event types
          this.eventSources[requestId].addEventListener('supply-confirmed', (event) => {
            this.$toast.success('Supply confirmed for one of your orders', `Supply for ${event.data}`);
            this.fetchOrders();
          });

          // Listen for custom event types
          this.eventSources[requestId].addEventListener('payment-confirmed', () => {
            this.$toast.success('Payment has been confirmed to one of your orders', 'Finished!!');
            this.fetchOrders();
          });

          this.eventSources[requestId].onmessage = (event) => {
            this.$toast.error('Received event:', event.data);
            this.fetchRequest();
          };

          this.eventSources[requestId].onopen = () => {
            this.$toast.show('SSE connection opened');
          };

          // eslint-disable-next-line no-unused-vars
          this.eventSources[requestId].onerror = (error) => {
            // this.$toast.error('SSE error:', error.message);
            this.eventSources[requestId].close();
            delete this.eventSources[requestId];
          };
        }
      });
    },
    async fetchOrders() {
      try {
        const farmerId = getCurrentUserId();
        const response = await axios.get(`/buyers-service/request-orders?farmerId=${farmerId}`);
        if (response.data.success) {
          this.orders = response.data.data;
          this.setUpEventSources();
        }
      } catch (error) {
        this.$toast.error('Error fetching orders:', error);
      }
    },
    async confirmPayment(order) {
      try {
        const response = await axios.put(`/buyers-service/request/order/confirm-payment?orderId=${order.orderId}&requestId=${order.requestId}`);
        if (response.data.success) {
          this.fetchOrders();
        } else {
          this.$toast.error('Failed to update order', response.data.msg);
        }
      } catch (error) {
        this.$toast.error('An error occured', error.message);
      } finally {
        this.snackbar = true;
      }
    },
    getStatusColor(status) {
      const statusColors = {
        PENDING_ACCEPTANCE: 'orange',
        BOOKED_FOR_SUPPLY: 'blue',
        SUPPLIED: 'green',
      };
      return statusColors[status] || 'grey';
    },
  },
  filters: {
    formatToHumanWithTime,
  },
};
</script>
