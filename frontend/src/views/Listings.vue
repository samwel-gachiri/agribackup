<template>
  <Default>
    <div class="page-wrap">
      <div class="glass-container">
        <!-- Header -->
        <div class="tw-flex tw-flex-col md:tw-flex-row tw-items-start md:tw-items-center tw-justify-between tw-gap-3 tw-mb-5">
          <div>
            <div class="tw-text-xs tw-tracking-wide tw-text-green-700">Sales</div>
            <h1 class="tw-text-2xl md:tw-text-3xl tw-font-extrabold tw-text-gray-800 tw-mt-1">My Listings</h1>
            <div class="tw-text-xs tw-text-gray-500 tw-mt-1">{{ listings.length }} active item(s)</div>
          </div>
          <div class="tw-flex tw-items-center tw-gap-2">
            <v-btn-toggle v-model="viewMode" dense class="tw-rounded-lg tw-bg-white tw-border tw-border-gray-200">
              <v-btn small value="grid"><v-icon small left>mdi-view-grid</v-icon>Grid</v-btn>
              <v-btn small value="table"><v-icon small left>mdi-table</v-icon>Table</v-btn>
            </v-btn-toggle>
            <v-btn class="btn-gradient" small @click="listingDialog = true">
              <v-icon small left>mdi-tag-plus</v-icon>
              Create Listing
            </v-btn>
          </div>
        </div>

        <!-- Loading state -->
        <div v-if="loading" class="skeleton-grid">
          <div v-for="i in 6" :key="i" class="skeleton-card">
            <div class="skeleton-img"></div>
            <div class="skeleton-line tw-mt-3"></div>
            <div class="skeleton-line tw-w-3/5"></div>
          </div>
        </div>

        <!-- Grid View -->
        <div v-else-if="viewMode === 'grid'" class="tw-grid tw-grid-cols-1 sm:tw-grid-cols-2 lg:tw-grid-cols-3 tw-gap-6">
          <div v-for="item in listings" :key="item.id" class="card-glass tw-overflow-hidden">
            <div class="card-media">
              <v-img
                :src="(item.imageUrls && item.imageUrls[0]) || 'https://via.placeholder.com/800x450?text=No+Image'"
                height="160"
                class="tw-rounded-xl"
                cover
              />
            </div>
            <div class="card-body">
              <div class="tw-flex tw-items-center tw-justify-between">
                <h3 class="tw-text-base tw-font-bold tw-text-gray-800">{{ item.farmerProduce?.farmProduce?.name || 'Listing' }}</h3>
                <v-chip x-small :color="statusColor(item.status)" class="tw-text-white chip-elevated">{{ item.status }}</v-chip>
              </div>
              <div class="tw-text-[12px] tw-text-gray-600 tw-mt-2">
                {{ item.quantity }} {{ item.unit }} • {{ item.price?.currency }} {{ item.price?.price }} / {{ item.unit }}
              </div>
              <div class="tw-flex tw-justify-between tw-items-center tw-mt-3">
                <div class="tw-text-[11px] tw-text-gray-500">Total: {{ item.price?.currency }} {{ (item.price?.price * item.quantity) || 0 }}</div>
                <div class="tw-flex tw-gap-2">
                  <v-btn x-small text class="btn-ghost" @click="viewDetails(item)"><v-icon x-small left>mdi-eye</v-icon>View</v-btn>
                  <v-btn x-small text class="btn-ghost" @click="downloadPdf(item)"><v-icon x-small left>mdi-file-download</v-icon>PDF</v-btn>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Table View -->
        <div v-else>
          <v-card class="glass-surface">
            <v-data-table
              :headers="headers"
              :items="listings"
              :loading="loading"
              :items-per-page="size"
              :page.sync="page"
              @update:page="fetchListings"
              class="elevation-0"
            >
              <template v-slot:item.actions="{ item }">
                <v-btn x-small class="btn-glass-outline" text @click="viewDetails(item)"><v-icon x-small left>mdi-eye</v-icon>View</v-btn>
              </template>
              <template v-slot:item.file="{ item }">
                <v-btn x-small class="btn-glass-outline" text @click="downloadPdf(item)"><v-icon x-small left>mdi-file-download</v-icon>PDF</v-btn>
              </template>
            </v-data-table>
          </v-card>
        </div>

        <!-- Create Listing Dialog -->
        <v-dialog
          v-model="listingDialog"
          :max-width="$vuetify.breakpoint.smAndDown ? 'calc(100vw - 24px)' : '800px'"
          :fullscreen="$vuetify.breakpoint.xsOnly"
          scrollable
        >
          <v-card class="glass-surface">
            <v-card-title class="tw-flex tw-items-center tw-justify-between tw-gap-2">
              <div class="tw-flex tw-items-center tw-gap-2"><v-icon color="primary">mdi-tag-plus</v-icon><span class="tw-font-bold">Create Listing</span></div>
              <v-btn icon @click="listingDialog = false"><v-icon>mdi-close</v-icon></v-btn>
            </v-card-title>
            <v-card-text>
              <create-listing />
            </v-card-text>
            <v-card-actions class="tw-justify-end">
              <v-btn class="btn-glass-outline" text @click="listingDialog = false">Close</v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>

        <!-- Listing Details Dialog -->
        <v-dialog
          v-model="dialog"
          :max-width="$vuetify.breakpoint.smAndDown ? 'calc(100vw - 24px)' : '700px'"
          :fullscreen="$vuetify.breakpoint.xsOnly"
          scrollable
        >
          <listing
            :key="selectedListingId"
            :listing-id="selectedListingId"
            @close-dialog="dialog = false"
          />
        </v-dialog>
      </div>
    </div>
  </Default>
</template>

<script>

import axios from 'axios';
import pluralize from 'pluralize';
import Default from '@/components/layout/Default.vue';
import CreateListing from '@/components/listing/CreateListing.vue';
import { getCurrentUserId } from '@/utils/roles.js';
import Listing from '@/components/listing/Listing.vue';

export default {
  components: {
    Listing, CreateListing, Default,
  },
  data() {
    return {
      listingDialog: false,
      viewMode: 'grid',
      headers: [
        { text: 'Product', value: 'farmerProduce.farmProduce.name' },
        { text: 'Quantity', value: 'quantityWithUnit' },
        { text: 'Price', value: 'priceWithCurrency' },
        { text: 'Rating', value: 'rating' },
        { text: 'Status', value: 'status' },
        { text: 'Total Price', value: 'totalPrice' },
        { text: 'Actions', value: 'actions', sortable: false },
        { text: 'File', value: 'file', sortable: false },
      ],
      // Data
      listings: [],
      totalElements: 0,
      totalPages: 0,
      size: 10,
      page: 0,
      loading: false,
      isDownloading: false,
      // Dialog
      dialog: false,
      selectedListingId: '',
    };
  },
  mounted() {
    this.fetchListings();
  },
  computed: {
    getCurrentUserId,
  },
  methods: {
    openListingDialog() {
      this.$toast.show('show');
      this.listingDialog = true;
    },
    statusColor(status) {
      const s = (status || '').toString().toLowerCase();
      switch (s) {
        case 'active': return 'green';
        case 'pending': return 'amber';
        case 'sold': return 'grey';
        case 'inactive': return 'grey';
        default: return 'blue';
      }
    },
    // fetchListings() {
    //   axios.get(`/listing/farmer?farmerId=${getCurrentUserId()}`)
    //     .then((response) => {
    //     })
    //     .catch((error) => {
    //     });
    // },
    // Fetch listings from the API
    async fetchListings() {
      this.loading = true;
      try {
        const response = await axios.get('/farmers-service/listing/farmer', {
          params: {
            farmerId: getCurrentUserId(),
            page: this.page,
            size: this.size,
          },
        });
        const data = response.data.data;
        this.listings = data.content
          .map((item) => ({
            ...item,
            quantityWithUnit: `${item.quantity} ${item.quantity > 1 ? item.unit : pluralize(item.unit)}`,
            priceWithCurrency: `${item.price.currency} ${item.price.price} @ ${item.unit}`,
            totalPrice: `${item.price.currency} ${item.price.price * item.quantity}`,
          }));
        this.totalElements = data.totalElements;
        this.totalPages = data.totalPages;
      } catch (error) {
        this.$toast.error('Error fetching listings:', error.message);
      } finally {
        this.loading = false;
      }
    },
    // View details of a listing
    viewDetails(item) {
      this.selectedListingId = item.id;
      this.dialog = true;
    },
    async downloadPdf(item) {
      this.isDownloading = true;
      await axios.get('/farmers-service/api/reports/listing/pdf', {
        params: {
          listingId: item.id,
        },
        responseType: 'arraybuffer',
      })
        .then((res) => {
          const contentDisposition = res.headers['content-disposition'];
          const fileNameRegex = /filename="(.+?)"/;
          const fileNameMatch = contentDisposition?.match(fileNameRegex);
          const fileName = fileNameMatch ? fileNameMatch[1] : 'REPORT.pdf';

          // Create a Blob from the byte array
          const blob = new Blob([res.data], { type: 'application/pdf' });
          const url = window.URL.createObjectURL(blob);

          // Create a temporary link to trigger the download
          const link = document.createElement('a');
          link.href = url;
          link.setAttribute('download', fileName);
          document.body.appendChild(link);
          link.click();

          // Cleanup
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
        })
        .catch((err) => {
          this.$toast.error('error', err.message);
        })
      // eslint-disable-next-line no-return-assign
        .finally(() => (this.isDownloading = false));
    },
  },
  watch: {
    listingDialog(newValue) {
      if (newValue !== true) {
        this.fetchListings();
      }
    },
  },
};
</script>

<style scoped>
/* Background + container */
.page-wrap { position: relative; min-height: 100vh; padding: 24px; }
.glass-container {
  position: relative; z-index: 1; margin: 0 auto; max-width: 1200px;
  background: rgba(255,255,255,0.5); border: 1px solid rgba(255,255,255,0.6);
  box-shadow: 0 8px 30px rgba(0,0,0,0.05); border-radius: 16px; padding: 20px;
  backdrop-filter: saturate(180%) blur(14px);
}

/* Glass cards */
.card-glass {
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid transparent;
  background-image:
    linear-gradient(180deg, rgba(255,255,255,0.75), rgba(255,255,255,0.6)) ,
    linear-gradient(135deg, #d1fae5, #bfdbfe, #f5d0fe);
  background-origin: border-box; background-clip: padding-box, border-box;
  border-radius: 16px; box-shadow: 0 6px 20px rgba(0,0,0,0.06);
  padding: 12px; transition: transform 0.2s ease, box-shadow 0.2s ease; position: relative;
}
.card-glass:hover { transform: translateY(-2px); box-shadow: 0 12px 28px rgba(0,0,0,0.10); }
.card-media { position: relative; }
.card-body { padding: 10px 4px 4px; }

/* Dialog glass */
.glass-surface {
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: saturate(180%) blur(12px);
  border: 1px solid rgba(255,255,255,0.7);
}

/* Buttons match theme */
.btn-glass-outline { background: rgba(255,255,255,0.55) !important; color: #0f766e !important; border: 1px solid rgba(15,118,110,0.25) !important; backdrop-filter: blur(8px); }
.btn-ghost { color: #0f766e !important; }
.btn-ghost:hover { background: rgba(15,118,110,0.08) !important; }
.btn-gradient { background: linear-gradient(135deg, #22c55e 0%, #16a34a 35%, #0ea5e9 100%) !important; color: #ffffff !important; border: none !important; box-shadow: 0 8px 18px rgba(34,197,94,0.25) !important; }

/* Skeletons */
.skeleton-grid { display: grid; grid-template-columns: repeat(1, minmax(0, 1fr)); gap: 1.25rem; }
@media (min-width: 640px) { .skeleton-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (min-width: 1024px) { .skeleton-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
.skeleton-card { border-radius: 16px; padding: 12px; background: linear-gradient(180deg, rgba(255,255,255,0.75), rgba(255,255,255,0.6)); border: 1px solid rgba(255,255,255,0.7); box-shadow: 0 6px 20px rgba(0,0,0,0.06); }
.skeleton-img, .skeleton-line { position: relative; overflow: hidden; background: #eef2f7; border-radius: 12px; }
.skeleton-img { height: 160px; }
.skeleton-line { height: 12px; margin-top: 8px; }
.skeleton-img::after, .skeleton-line::after { content: ""; position: absolute; inset: 0; background: linear-gradient(90deg, rgba(255,255,255,0), rgba(255,255,255,0.7), rgba(255,255,255,0)); transform: translateX(-100%); animation: shimmer 1.6s infinite; }
@keyframes shimmer { to { transform: translateX(100%); } }

@media (max-width: 768px) { .glass-container { padding: 16px; } }
</style>
