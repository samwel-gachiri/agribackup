<template>
  <Default>
    <div v-if="!loading">
      <div v-if="requests.length === 0"
           class="tw-absolute tw-top-0 tw-bottom-0 tw-left-0 tw-right-0 tw-flex tw-justify-center tw-items-center tw-flex-col tw-w-full tw-h-full">
        <div
            class="tw-flex md:tw-flex-row tw-flex-col tw-items-center tw-justify-center"
        >
          <div class="tw-flex tw-flex-col tw-items-center tw-text-center tw-bg-gray-200 tw-rounded-lg tw-p-5">
            <strong>You have not made any request</strong>
            <v-icon class="tw-my-3" size="64">mdi-magnify</v-icon>
            <h2>No need to search multiple listings</h2>
            <h2 class="c-blue-text">farmers will respond to your request</h2>
          </div>
          <v-btn
              dense
              class="tw-rounded-lg tw-ml-5"
              color="secondary"
              @click="requestDialog = true"
          >Request for a produce</v-btn>
        </div>
      </div>
      <div class="" v-else>
        <div class="tw-flex tw-flex-col tw-p-5">
          <h2 class="tw-text-xl tw-font-bold tw-text-green-800">My Requests</h2>
          <div>
            <v-btn
                dense
                rounded="lg"
                class="tw-rounded-lg tw-mt-5"
                color="secondary"
                @click="requestDialog = true"
            >
              <v-icon>mdi-request</v-icon>
              Request Produce
            </v-btn>
          </div>
        </div>
        <div>
          <v-data-table
              :headers="headers"
              :items="requests"
              :loading="loading"
              :items-per-page="size"
              :page.sync="page"
              @update:page="fetchRequests"
              class="elevation-1"
          >
            <!--            :server-items-length="totalElements"-->
            <!-- Custom Row Template -->
            <template v-slot:item.actions="{ item }">
              <v-btn small color="primary" @click="viewDetails(item)">View</v-btn>
            </template>
          </v-data-table>
          <v-pagination v-model="page" :length="totalPages-1" @input="fetchRequests" />
          <!-- Dialog for Request Details -->
          <v-dialog v-model="dialog" max-width="600px">
            <request :request-id="this.selectedRequestId"></request>
          </v-dialog>
        </div>
      </div>
    </div>
    <!-- Loading Spinner -->
    <div v-else class="tw-flex tw-justify-center tw-items-center tw-h-screen">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
    </div>
    <v-dialog v-model="requestDialog" max-width="500px">
      <create-request/>
      <v-btn
          class="tw-bg-white"
          color="error"
          text
          @click="requestDialog = false"
      >
        Close
      </v-btn>
    </v-dialog>
  </Default>
</template>

<script>

import axios from 'axios';
import pluralize from 'pluralize';
import Default from '@/components/layout/Default.vue';
import { getCurrentUserId } from '@/utils/roles.js';
import Request from '../components/request/Request.vue';
import CreateRequest from '../components/request/CreateRequest.vue';

export default {
  components: {
    Request, CreateRequest, Default,
  },
  data() {
    return {
      requestDialog: false,
      headers: [
        { text: 'Product', value: 'produceName' },
        { text: 'Quantity', value: 'quantityWithUnit' },
        { text: 'Price', value: 'priceWithCurrency' },
        { text: 'Rating', value: 'rating' },
        { text: 'Status', value: 'status' },
        { text: 'Total Price', value: 'totalPrice' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      // Data
      requests: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      page: 0,
      loading: false,
      // Dialog
      dialog: false,
      selectedRequestId: '',
    };
  },
  mounted() {
    this.fetchRequests();
  },
  computed: {
    getCurrentUserId,
  },
  methods: {
    // fetchRequests() {
    //   axios.get(`/request/buyer?buyerId=${getCurrentUserId()}`)
    //     .then((response) => {
    //     })
    //     .catch((error) => {
    //     });
    // },
    // Fetch requests from the API
    async fetchRequests() {
      this.loading = true;
      try {
        const response = await axios.get('/buyers-service/request/buyer', {
          params: {
            buyerId: getCurrentUserId(),
            page: this.page,
            size: this.size,
          },
        });
        const data = response.data.data;
        this.requests = data.content
          .map((item) => ({
            ...item,
            produceName: item.preferredProduce.bsfarmProduce.name,
            quantityWithUnit: `${item.quantity} ${item.quantity > 1 ? item.unit : pluralize(item.unit)}`,
            priceWithCurrency: `${item.price.currency} ${item.price.price} @ ${item.unit}`,
            totalPrice: `${item.price.currency} ${item.price.price * item.quantity}`,
          }));
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
      } catch (error) {
        this.$toast.error('Error fetching requests:', error.message);
      } finally {
        this.loading = false;
      }
    },
    // View details of a request
    viewDetails(item) {
      this.selectedRequestId = item.id;
      this.dialog = true;
    },
  },
  watch: {
    requestDialog(newValue) {
      if (newValue !== true) {
        this.fetchRequests();
      }
    },
  },
};
</script>

<style scoped>

</style>
