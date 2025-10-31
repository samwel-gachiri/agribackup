<template>
  <v-card>
    <card-title
        icon="mdi-book"
        class="tw-mt-4">Listing Details</card-title>
    <v-card-text>
      <div v-if="listing">
        <div class="tw-flex md:tw-flex-row tw-flex-col-reverse">
          <div class="tw-flex tw-flex-col tw-w-full">
<!--            <p><strong>ID:</strong> {{ listing.produceListing.id }}</p>-->
            <p><strong>Price:</strong> {{ listing.produceListing.price.price.toLocaleString() }} {{ listing.produceListing.price.currency }} per {{listing.produceListing.unit}}</p>
            <p><strong>Earnings:</strong> {{ listing.earnings.toFixed(2) }} {{listing.produceListing.price.currency}}</p>
<!--            <p><strong>Rating:</strong> {{ listing.produceListing.rating }}</p>-->
<!--            <p><strong>Status:</strong> {{ listing.produceListing.status }}</p>-->
            <p><strong>Quantity Sold:</strong> {{ listing.quantitySold }} {{ listing.produceListing.unit }}</p>
            <p><strong>Quantity Left:</strong> {{ listing.quantityLeft }} {{ listing.produceListing.unit }}</p>
            <p><strong>No of purchases:</strong> {{ listing.noOfPurchases }} <v-icon>mdi-arrow-down</v-icon></p>
          </div>
          <div v-if="false" class="tw-w-full">
            <v-card class="tw-p-4">
              <h1 class="tw-text-xs tw-uppercase tw-font-bold tw-mb-5">
                Progress
              </h1>

              <div class="tw-flex tw-justify-between tw-items-center tw-mb-4">
                <div class="tw-text-green-700 tw-text-3xl tw-font-bold">{{percentageSold}}% SOLD</div>
                <div class="tw-text-gray-500 tw-text-lg tw-font-light">
                  {{listing.produceListing.price.currency}} {{listing.earnings.toFixed(2)}} earned
                </div>
              </div>

              <v-card-text>
                <!-- Eligibility Review Label -->
                <div
                    :style="`right: calc(${review}px - 32px)`"
                    class="tw-absolute tw-text-xs tw-text-green-700 tw-mt-[-1.5rem]"
                >
                  Review
                </div>

                <!-- Progress Bar -->
                <v-progress-linear
                    color="green darken-3"
                    height="22"
                    :value="percentageSold"
                    class="tw-w-full"
                    rounded
                >
                  <!-- Badge Indicator -->
                  <v-badge
                      :style="`right: ${review}px`"
                      class="tw-absolute"
                      color="white"
                      dot
                      inline
                  ></v-badge>
                </v-progress-linear>

                <div class="tw-flex tw-justify-between tw-py-3">
        <span class="tw-text-green-700 tw-font-medium">
          {{listing.produceListing.price.currency}} {{(listing.quantitySold * listing.produceListing.price.price).toFixed(2)}} earned
        </span>
                  <span class="tw-text-gray-500"> {{listing.produceListing.price.currency}} {{(listing.produceListing.price.price * listing.produceListing.quantity).toFixed(2)}} Total</span>
                </div>
              </v-card-text>

              <v-divider></v-divider>

              <v-list-item
                  append-icon="mdi-chevron-right"
                  lines="two"
                  subtitle="Details and agreement"
                  link
              ></v-list-item>
            </v-card>
          </div>
        </div>

        <v-divider class="my-4"></v-divider>

        <h3 class="text-lg font-bold mb-2">Orders</h3>
        <v-data-table
            :headers="orderHeaders"
            :items="listing.produceListing.listingOrders"
            class="elevation-1"
        >
          <template v-slot:item.actions="{ item }">
            <div v-if="item.status === 'PENDING_ACCEPTANCE'" class="tw-flex tw-flex-row">
              <v-btn  small color="success" @click="acceptOrder(item)">Accept</v-btn>
              /
              <v-btn  small color="red" @click="() => {declineOrderDialog = true; declinedOrder = item}">Decline</v-btn>
            </div>
            <v-btn v-if="item.status === 'BOOKED_FOR_SUPPLY'" small color="success" disabled>confirm pay</v-btn>
            <v-btn v-if="item.status === 'SUPPLIED'" small color="success" @click="confirmPayment(item)">confirm pay</v-btn>
          </template>
        </v-data-table>
      </div>
    </v-card-text>
    <v-card-actions>
      <v-btn
          @click="$emit('close-dialog')"
      >Close</v-btn>
    </v-card-actions>
    <v-dialog v-model="declineOrderDialog" max-width="300px">
      <v-card type="warning">
        <v-alert
            type="warning"
            text="text"
        >
          <h2 class="">You are about to decline an offer</h2>
        </v-alert>
        <v-card-text>
          <h2>Please provide a reason for declining the offer so that the buyer can be notified.</h2>
          <v-textarea
              label="Give in reason here"
              outlined
              v-model="farmerComment"
          ></v-textarea>
          <v-btn @click="this.declineOrder">continue</v-btn>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script>
import axios from 'axios';
import CardTitle from '@/components/shared/CardTitle.vue';

export default {
  name: 'Listing',
  components: { CardTitle },
  data() {
    return {
      orderHeaders: [
        { text: 'Buyer ID', value: 'buyerId' },
        { text: 'Date Created', value: 'dateCreated' },
        { text: 'Quantity ordered', value: 'quantity' },
        { text: 'Date Accepted', value: 'dateAccepted' },
        { text: 'Date Supplied', value: 'dateSupplied' },
        { text: 'Date Paid', value: 'datePaid' },
        { text: 'Status', value: 'status' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      listing: {
        quantitySold: 2,
        quantityLeft: 8,
        earnings: 200,
        noOfPurchases: 1,
        produceListing: {
          id: '68d2ad3f-8042-4089-bb97-557bb670580a',
          quantity: 10,
          price: {
            price: 100,
            currency: 'KSH',
          },
          unit: 'pineapples',
          rating: 0,
          status: 'ACTIVE',
          farmerProduce: {
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
          listingOrders: [
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
      declineOrderDialog: false, // this is when the user declines to supply an order
      declinedOrder: null,
      farmerComment: '',
      review: '100',
    };
  },
  props: {
    listingId: {
      type: String,
      default: '',
    },
  },
  mounted() {
    // this.intervalId = setInterval(() => {
    // }, 3000);
    this.fetchListing();
  },
  beforeDestroy() {
    clearInterval(this.intervalId);
    if (this.eventSource) {
      this.eventSource.close();
    }
  },
  computed: {
    percentageSold() {
      // eslint-disable-next-line no-mixed-operators
      return (this.listing.quantitySold / (this.listing.quantitySold + this.listing.quantityLeft) * 100).toFixed(2);
    },
  },
  methods: {
    connectToSSE() {
      this.eventSource = new EventSource(
        `${process.env.VUE_APP_API_BASE_URL}farmers-service/listing/stream?listingId=${this.listing.produceListing.id}`,
      );

      // Listen for custom event types
      this.eventSource.addEventListener('order-added-to-listing', () => {
        this.$toast.success('Order has been made to one of your listing', 'New order!!!');
        this.fetchListing();
      });

      // Listen for custom event types
      this.eventSource.addEventListener('supply-confirmed', (event) => {
        this.fetchListing();
        this.$toast.success('Supply confirmed for an order', `Supply for ${event.data}`);
      });

      // Listen for custom event types
      this.eventSource.addEventListener('payment-confirmed', () => {
        this.$toast.success('Payment has been confirmed to one of your listing orders', 'Finished!!');
        this.fetchListing();
      });

      this.eventSource.onmessage = (event) => {
        this.$toast.error('Received event:', event.data);
        this.fetchListing();
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
    async fetchListing() {
      this.loading = true;
      try {
        const response = await axios.get('/farmers-service/listing', {
          params: {
            listingId: this.listingId,
          },
        });
        this.listing = response.data.data;
      } catch (error) {
        this.$toast.error('Error fetching listing');
      } finally {
        this.loading = false;
        if (!this.eventSource) {
          this.connectToSSE();
        }
      }
    },
    // Accept an order
    acceptOrder(order) {
      axios.put(`/farmers-service/listing/order/accept?orderId=${order.id}&listingId=${this.listing.produceListing.id}`).then((response) => {
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
          this.fetchListing();
        });
    },
    declineOrder() {
      axios.put(`/farmers-service/listing/order/decline?orderId=${this.declinedOrder.id}&listingId=${this.listing.produceListing.id}&farmerComment=${this.farmerComment}`).then((response) => {
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
          this.declineOrderDialog = false;
          this.fetchListing();
        });
    },
    confirmPayment(order) {
      axios.put(`/farmers-service/listing/order/confirm-payment?orderId=${order.id}&listingId=${this.listing.produceListing.id}`).then((response) => {
        if (response.data.success === true) {
          this.$toast.success('Payment confirmed', order.id);
        } else {
          this.$toast.error(response.data.msg, 'Failed to confirm payment');
        }
      })
        .catch((error) => {
          this.$toast.error(error.message);
        })
        .finally(() => {
          this.fetchListing();
        });
    },
  },
};
</script>

<style scoped>

</style>
