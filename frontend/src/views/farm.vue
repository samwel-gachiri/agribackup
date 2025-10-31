<template>
  <default>
    <!-- Background + glass container -->
    <div class="page-wrap">
      <div class="bg-shape bg-1"></div>
      <div class="bg-shape bg-2"></div>
      <div class="bg-shape bg-3"></div>

      <div class="glass-container">
        <!-- Loading state -->
        <div v-if="loading" class="skeleton-grid">
          <div v-for="i in 6" :key="i" class="skeleton-card">
            <div class="skeleton-img"></div>
            <div class="skeleton-line tw-mt-3"></div>
            <div class="skeleton-line tw-w-3/5"></div>
          </div>
        </div>

        <div v-else>
          <!-- Header -->
          <div class="tw-flex tw-flex-col md:tw-flex-row tw-items-start md:tw-items-center tw-justify-between tw-gap-3 tw-mb-6">
            <div>
              <div class="tw-text-xs tw-tracking-wide tw-text-green-700">My Farm</div>
              <h1 class="tw-text-2xl md:tw-text-3xl tw-font-extrabold tw-text-gray-800 tw-mt-1">
                {{ farmer.farmName || farmer.name || 'Your Farm' }}
              </h1>
              <div class="tw-text-xs tw-text-gray-500 tw-mt-1">{{ (farmer.farmerProduces?.length) || 0 }} produce item(s)</div>
            </div>
            <div class="tw-flex tw-gap-2">
              <v-btn class="btn-glass-outline" outlined small @click="openShareDialog">
                <v-icon left small>mdi-share-variant</v-icon> Share
              </v-btn>
              <v-btn
                v-if="getCurrentUserId() === farmer.id"
                class="btn-gradient"
                small
                @click="$router.push({ name: 'AddFarmerProduce' })"
                aria-label="Add Produce"
              >
                <v-icon left small>mdi-plus</v-icon> Add Produce
              </v-btn>
            </div>
          </div>

          <!-- Empty state -->
      <div v-if="farmer.farmerProduces.length === 0" class="empty-state">
            <div class="tw-text-lg tw-font-semibold tw-text-gray-700">No produce yet</div>
            <div class="tw-text-xs tw-text-gray-500 tw-mt-1">Add your first crop to showcase your farm.</div>
            <v-btn
              v-if="getCurrentUserId() === farmer.id"
              class="btn-gradient tw-mt-3"
              @click="$router.push({ name: 'AddFarmerProduce' })"
            >
              <v-icon left small>mdi-seed</v-icon> Add Produce
            </v-btn>
          </div>

          <!-- Produce grid -->
          <div v-else class="tw-grid tw-grid-cols-1 sm:tw-grid-cols-2 lg:tw-grid-cols-3 tw-gap-6">
            <div
              v-for="item in farmer.farmerProduces"
              :key="item.id"
              class="card-glass tw-overflow-hidden card-clickable"
              @click="openDetails(item)"
            >
              <div class="card-media">
                <v-img
                  :src="(item.imageUrls && item.imageUrls[0]) || 'https://via.placeholder.com/800x450?text=No+Image'"
                  height="180"
                  class="tw-rounded-xl"
                  :alt="(item.farmProduce && item.farmProduce.name) ? item.farmProduce.name : 'Produce image'"
                  cover
                />
                <div v-if="(item.imageUrls?.length || 0) > 1" class="thumb-row">
                  <v-img
                    v-for="(imageUrl, i) in item.imageUrls.slice(1, 4)"
                    :key="i"
                    :src="imageUrl"
                    height="48"
                    width="72"
                    class="tw-rounded-lg tw-border tw-border-gray-100 thumb"
                    :alt="`More image ${i+1}`"
                    cover
                  />
                </div>
              </div>
              <div class="card-body">
                <div class="tw-flex tw-items-center tw-justify-between">
                  <h3 class="tw-text-base tw-font-bold tw-text-gray-800">
                    {{ item.farmProduce?.name || 'Produce' }}
                  </h3>
                  <v-chip x-small :color="statusColor(item.status)" class="tw-text-white chip-elevated">{{ item.status }}</v-chip>
                </div>
                <div class="tw-mt-2 tw-flex tw-items-center tw-gap-2">
                  <v-progress-linear
                    v-if="item.status === 'GROWING' && item.plantingDate && item.predictedHarvestDate"
                    :value="growthProgress(item)"
                    height="6"
                    color="green"
                    rounded
                    style="width: 80%"
                  ></v-progress-linear>
                  <span v-if="item.status === 'GROWING' && item.plantingDate && item.predictedHarvestDate" class="tw-text-xs tw-text-green-700">{{ growthProgress(item) }}%</span>
                </div>
                <div v-if="item.status === 'PLANTED' || item.status === 'READY_TO_HARVEST' || item.status === 'GROWING'" class="tw-mt-1 tw-text-xs tw-text-gray-500">
                  <span v-if="item.predictedHarvestDate">Predicted Harvest: {{ formatDate(item.predictedHarvestDate) }}</span>
                  <span v-if="item.predictionConfidence"> (Confidence: {{ (item.predictionConfidence * 100).toFixed(0) }}%)</span>
                </div>
                <v-btn
                  v-if="item.status === 'PLANTED' || !item.plantingDate"
                  x-small
                  color="primary"
                  class="tw-mt-2"
                  @click.stop="openStartGrowth(item)"
                >
                  <v-icon left x-small>mdi-seed</v-icon>
                  Start Growth
                </v-btn>
                <!-- ...existing code... -->

                <div class="desc-clamp tw-mt-2" v-html="renderMarkdown(item.description)"></div>
                <div class="tw-flex tw-items-center tw-justify-between tw-mt-3 tw-text-[11px] tw-text-gray-500">
                  <span>{{ isLivestock(item) ? 'Acquired' : 'Planted' }}: {{ formatDate(getPlantedOrAcquiredDate(item)) || '—' }}</span>
                  <span class="tw-hidden sm:tw-inline">Type: {{ item.farmingType || '—' }}</span>
                </div>
                <div class="tw-flex tw-justify-between tw-items-center tw-mt-3">
                  <div class="tw-text-[11px] tw-text-gray-500">{{ item.farmingType || '—' }}</div>
                  <div class="tw-flex tw-gap-2">
                    <v-btn x-small text class="btn-ghost" @click.stop="() => { editProduceDialog = true; selectedFarmerProduce = item; }">
                      <v-icon left x-small>mdi-pencil</v-icon>
                      Edit
                    </v-btn>
                    <v-btn x-small text class="btn-ghost" @click.stop="listingDialog = true">
                      <v-icon left x-small>mdi-tag-outline</v-icon>
                      Sell
                    </v-btn>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Dialogs -->
          <v-dialog v-model="startGrowthDialog" max-width="400">
            <v-card>
              <v-card-title>Start Growth</v-card-title>
              <v-card-text>
                <v-date-picker v-model="plantingDate" :max="new Date().toISOString().substr(0, 10)" label="Planting Date" required></v-date-picker>
                <div v-if="plantingDateError" class="tw-text-red-600 tw-text-xs tw-mt-2">{{ plantingDateError }}</div>
              </v-card-text>
              <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn text @click="startGrowthDialog = false">Cancel</v-btn>
                <v-btn color="primary" @click="submitStartGrowth">Start</v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>

          <v-dialog
            v-model="listingDialog"
            :max-width="$vuetify.breakpoint.smAndDown ? 'calc(100vw - 24px)' : '500px'"
            :fullscreen="$vuetify.breakpoint.xsOnly"
          >
            <create-listing />
          </v-dialog>

          <!-- Details Dialog -->
          <v-dialog
            v-model="detailsDialog"
            :max-width="$vuetify.breakpoint.smAndDown ? 'calc(100vw - 24px)' : '900px'"
            :fullscreen="$vuetify.breakpoint.xsOnly"
            scrollable
          >
            <v-card class="glass-surface">
              <v-card-title class="tw-items-start tw-gap-2 tw-flex tw-flex-col md:tw-flex-row md:tw-items-center md:tw-justify-between">
                <div>
                  <div class="tw-text-xs tw-text-gray-500">{{ isLivestock(selectedFarmerProduce || {}) ? 'Livestock' : 'Crop' }}</div>
                  <div class="tw-text-xl tw-font-bold tw-text-gray-800">{{ selectedFarmerProduce?.farmProduce?.name || 'Produce' }}</div>
                </div>
                <v-chip small :color="statusColor(selectedFarmerProduce?.status)" class="tw-text-white chip-elevated">{{ selectedFarmerProduce?.status }}</v-chip>
              </v-card-title>
              <v-card-text>
                <!-- Gallery -->
                <div class="details-gallery">
                  <v-carousel
                    hide-delimiters
                    :height="$vuetify.breakpoint.smAndDown ? 220 : 320"
                    v-if="(selectedFarmerProduce?.imageUrls?.length || 0) > 0"
                  >
                    <v-carousel-item
                      v-for="(img, i) in selectedFarmerProduce.imageUrls"
                      :key="i"
                    >
                      <v-img :src="img" class="tw-rounded-xl" cover :height="$vuetify.breakpoint.smAndDown ? 220 : 320" />
                    </v-carousel-item>
                  </v-carousel>
                  <v-img v-else :src="(selectedFarmerProduce?.imageUrls && selectedFarmerProduce?.imageUrls[0]) || 'https://via.placeholder.com/1200x600?text=No+Image'" :height="$vuetify.breakpoint.smAndDown ? 220 : 320" class="tw-rounded-xl" cover />
                </div>
                <!-- Meta -->
                <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-3 tw-gap-4 tw-mt-4">
                  <div class="meta-card">
                    <div class="meta-label">{{ isLivestock(selectedFarmerProduce || {}) ? 'Acquired' : 'Planted' }}</div>
                    <div class="meta-value">{{ formatDate(getPlantedOrAcquiredDate(selectedFarmerProduce || {})) || '—' }}</div>
                  </div>
                  <div class="meta-card">
                    <div class="meta-label">Type</div>
                    <div class="meta-value">{{ selectedFarmerProduce?.farmingType || '—' }}</div>
                  </div>
                  <div class="meta-card">
                    <div class="meta-label">Status</div>
                    <div class="meta-value">{{ selectedFarmerProduce?.status || '—' }}</div>
                  </div>
                </div>
                <!-- Full Description -->
                <div class="tw-mt-4">
                  <div class="tw-text-sm tw-font-semibold tw-text-gray-700 tw-mb-1">Description</div>
                  <div class="tw-text-sm tw-text-gray-700" v-html="renderMarkdown(selectedFarmerProduce?.description)"></div>
                </div>
                <!-- History -->
                <div class="tw-mt-6">
                  <div class="tw-text-sm tw-font-semibold tw-text-gray-700 tw-mb-1">History</div>
                  <div v-if="historyLoading" class="tw-text-xs tw-text-gray-500">Loading history…</div>
                  <div v-else-if="history && (history.entries?.length || 0) > 0">
                    <div class="history-list">
                      <div class="history-item" v-for="(h, idx) in history.entries" :key="idx">
                        <div class="tw-text-xs tw-text-gray-500">{{ h.period }}</div>
                        <div class="tw-text-sm tw-font-medium">{{ h.label }}</div>
                        <div class="tw-text-xs tw-text-gray-500">{{ h.value }}</div>
                      </div>
                    </div>
                  </div>
                  <div v-else class="tw-text-xs tw-text-gray-500">No history available yet. View full reports in Reports.</div>
                </div>
              </v-card-text>
              <v-card-actions class="tw-justify-end">
                <v-btn class="btn-glass-outline" text @click="closeDetails">Close</v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>

          <v-dialog
            v-model="editProduceDialog"
            :max-width="$vuetify.breakpoint.smAndDown ? 'calc(100vw - 24px)' : '560px'"
            :fullscreen="$vuetify.breakpoint.xsOnly"
            scrollable
          >
            <v-card class="glass-surface">
              <v-card-title class="dialog-title">
                <div class="tw-flex tw-items-center tw-gap-2">
                  <v-icon color="primary">mdi-leaf</v-icon>
                  <span class="tw-text-lg tw-font-bold tw-text-gray-800">Edit Produce</span>
                </div>
                <v-chip v-if="selectedFarmerProduce?.status" x-small :color="statusColor(selectedFarmerProduce?.status)" class="tw-text-white chip-elevated">{{ selectedFarmerProduce?.status }}</v-chip>
              </v-card-title>
              <v-card-text>
                <edit-produce-form
                  :key="selectedFarmerProduce?.id"
                  :selected-farmer-produce="selectedFarmerProduce" />
              </v-card-text>
              <v-card-actions class="tw-justify-end tw-gap-2">
                <v-btn class="btn-glass-outline" text @click="() => { editProduceDialog = false; fetchFarmerDetails(); }">Close</v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>

          <!-- Share Dialog -->
          <v-dialog
            v-model="showDialog"
            :max-width="$vuetify.breakpoint.smAndDown ? 'calc(100vw - 24px)' : '400px'"
            :fullscreen="$vuetify.breakpoint.xsOnly"
          >
            <v-card class="glass-surface">
              <v-card-title>Share this profile</v-card-title>
              <v-card-text>
                <v-list>
                  <v-list-item @click="copyUrl">
                    <v-list-item-icon><v-icon>mdi-content-copy</v-icon></v-list-item-icon>
                    <v-list-item-title>Copy URL</v-list-item-title>
                  </v-list-item>
                  <v-list-item @click="shareToWhatsApp">
                    <v-list-item-icon><v-icon>mdi-whatsapp</v-icon></v-list-item-icon>
                    <v-list-item-title>Share to WhatsApp</v-list-item-title>
                  </v-list-item>
                  <v-list-item @click="shareToFacebook">
                    <v-list-item-icon><v-icon>mdi-facebook</v-icon></v-list-item-icon>
                    <v-list-item-title>Share to Facebook</v-list-item-title>
                  </v-list-item>
                  <v-list-item @click="shareToInstagram">
                    <v-list-item-icon><v-icon>mdi-instagram</v-icon></v-list-item-icon>
                    <v-list-item-title>Share to Instagram</v-list-item-title>
                  </v-list-item>
                </v-list>
              </v-card-text>
              <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn class="btn-glass-outline" text @click="closeDialog">Close</v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>

          <!-- Snackbar for Feedback -->
          <v-snackbar v-model="showSnackbar" :timeout="3000">
            {{ snackbarMessage }}
          </v-snackbar>
        </div>
      </div>
    </div>
  </default>
</template>

<script>
import axios from 'axios';
import MarkdownIt from 'markdown-it';
import DOMPurify from 'dompurify';
import { getCurrentUserId } from '@/utils/roles.js';
import CreateListing from '@/components/listing/CreateListing.vue';
import Default from '@/components/layout/Default.vue';
import EditProduceForm from '@/components/produce/EditProduceForm.vue';

export default {
  components: {
    EditProduceForm,
    Default,
    CreateListing,
  },
  data() {
    return {
      farmer: {
        id: '',
        name: '',
        email: '',
        phoneNumber: '',
        createdAt: '2025-01-25T09:38:38.536Z',
        location: {
          latitude: 0.0,
          longitude: 0.0,
          customName: '',
        },
        farmerProduces: [],
      },
      selectedFarmerProduce: null,
      listingDialog: false,
      editProduceDialog: false,
      showDialog: false,
      showSnackbar: false,
      snackbarMessage: '',
      loading: false,
      md: new MarkdownIt({
        html: true,
        xhtmlOut: true,
        breaks: true,
        linkify: true,
        typographer: true,
        strict: true,
      }),
      detailsDialog: false,
      historyLoading: false,
      history: { entries: [] },
      // Start Growth dialog state
      startGrowthDialog: false,
      selectedProduce: null,
      plantingDate: '',
      plantingDateError: '',
    };
  },
  mounted() {
    this.loading = true;
    this.fetchFarmerDetails();
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize);
  },
  computed: {
  // reserved for future derived state
  },
  methods: {
    // expose function for both template and script
    getCurrentUserId,
    openDetails(item) {
      this.selectedFarmerProduce = item;
      this.detailsDialog = true;
      this.loadHistory();
    },
    closeDetails() {
      this.detailsDialog = false;
    },
    isLivestock(item) {
      const type = (item?.farmingType || item?.category || '').toString().toLowerCase();
      return /livestock|animal|poultry|dairy|goat|cattle|cow|sheep|pig|hen|chicken/.test(type);
    },
    getPlantedOrAcquiredDate(item) {
      // Try common field names; fallback to createdAt
      return (
        item?.plantedAt
        || item?.plantingDate
        || item?.seedDate
        || item?.acquiredAt
        || item?.birthDate
        || item?.startedAt
        || item?.createdAt
        || null
      );
    },
    formatDate(val) {
      if (!val) return '';
      try { return new Date(val).toLocaleDateString(); } catch (e) { return String(val); }
    },
    async loadHistory() {
      this.historyLoading = true;
      this.history = { entries: [] };
      try {
        // Best-effort: use existing sales-report and filter client-side if possible
        const farmerId = this.farmer?.id || this.$route.params.farmerId;
        const { data } = await axios.get(`/farmers-service/api/dashboard/sales-report?farmerId=${farmerId}`);
        const payload = data?.data || data;
        // Expecting shape like { categories: [...], series: [...] }
        if (payload?.categories && payload?.series) {
          const months = payload.categories;
          // Sum all series as a fallback
          const totals = (payload.series || []).reduce((acc, s) => {
            (s.data || []).forEach((v, idx) => { acc[idx] = (acc[idx] || 0) + (v || 0); });
            return acc;
          }, []);
          this.history.entries = months.map((m, i) => ({ period: m, label: 'Sales', value: totals[i] || 0 }));
        }
      } catch (e) {
        // Silent fail; show no history available
      } finally {
        this.historyLoading = false;
      }
    },
    fetchFarmerDetails() {
      axios.get(`/farmers-service/farmer?farmerId=${this.$route.params.farmerId}`)
        .then(async (response) => {
          this.farmer = { ...this.farmer, ...response.data.data };
        })
        .catch((e) => {
          this.$toast.error(e.message);
        })
        .finally(() => {
          this.loading = false;
        });
    },
    // Start Growth dialog methods
    openStartGrowth(item) {
      this.selectedProduce = item;
      this.plantingDate = '';
      this.plantingDateError = '';
      this.startGrowthDialog = true;
    },
    async submitStartGrowth() {
      if (!this.plantingDate) {
        this.plantingDateError = 'Please select a planting date.';
        return;
      }
      if (new Date(this.plantingDate) > new Date()) {
        this.plantingDateError = 'Planting date cannot be in the future.';
        return;
      }
      try {
        await axios.post('/api/harvest/start-growth', {
          farmerProduceId: this.selectedProduce.id,
          plantingDate: this.plantingDate,
          notes: null,
        });
        this.$toast.success('Growth started!');
        this.startGrowthDialog = false;
        this.fetchFarmerDetails();
      } catch (e) {
        this.plantingDateError = e.response?.data?.message || e.response?.data?.msg || 'Failed to start growth.';
      }
    },
    growthProgress(item) {
      if (!item.plantingDate || !item.predictedHarvestDate) return 0;
      const total = (new Date(item.predictedHarvestDate) - new Date(item.plantingDate)) / (1000 * 60 * 60 * 24);
      const elapsed = (new Date() - new Date(item.plantingDate)) / (1000 * 60 * 60 * 24);
      return Math.min(100, Math.max(0, Math.round((elapsed / total) * 100)));
    },
    openShareDialog() {
      this.showDialog = true;
    },
    closeDialog() {
      this.showDialog = false;
    },
    copyUrl() {
      const currentUrl = window.location.href;
      navigator.clipboard
        .writeText(currentUrl)
        .then(() => {
          this.snackbarMessage = 'URL copied to clipboard!';
          this.showSnackbar = true;
        })
        .catch((err) => {
          this.snackbarMessage = 'Failed to copy URL.';
          this.showSnackbar = true;
          this.$toast.error('Failed to copy URL: ', err.message);
        });
    },
    shareToWhatsApp() {
      const currentUrl = encodeURIComponent(window.location.href);
      window.open(`https://wa.me/?text=${currentUrl}`, '_blank');
    },
    shareToFacebook() {
      const currentUrl = encodeURIComponent(window.location.href);
      window.open(`https://www.facebook.com/sharer/sharer.php?u=${currentUrl}`, '_blank');
    },
    shareToInstagram() {
      this.snackbarMessage = 'Instagram does not support direct URL sharing.';
      this.showSnackbar = true;
      window.open('https://www.instagram.com', '_blank');
    },
    statusColor(status) {
      switch ((status || '').toUpperCase()) {
        case 'ACTIVE': return 'green';
        case 'PENDING': return 'orange';
        case 'REJECTED': return 'red';
        case 'SOLD': return 'blue';
        default: return 'grey';
      }
    },
    renderMarkdown(text) {
      return DOMPurify.sanitize(this.md.render(text || ''));
    },
  },
  watch: {
    listingDialog(newValue) {
      if (newValue !== true) {
        this.fetchFarmerDetails();
      }
    },
  },
};
</script>

<style scoped>
/* Page background with subtle gradient */
.page-wrap {
  position: relative;
  min-height: 100vh;
  padding: 24px;
  background: radial-gradient(1200px 600px at 10% -10%, #ebfff3 0%, rgba(235,255,243,0) 60%),
              radial-gradient(1000px 600px at 110% 10%, #e9f3ff 0%, rgba(233,243,255,0) 60%),
              linear-gradient(135deg, #f5fdf7 0%, #f2fbff 100%);
}

/* Decorative blurred blobs */
.bg-shape {
  position: absolute;
  filter: blur(40px);
  opacity: 0.55;
  z-index: 0;
  animation: float 14s ease-in-out infinite;
}
.bg-1 { width: 320px; height: 320px; top: -40px; left: -40px; background: radial-gradient(circle at 30% 30%, #86efac, transparent 60%); animation-delay: 0s; }
.bg-2 { width: 360px; height: 360px; bottom: -60px; right: -60px; background: radial-gradient(circle at 70% 70%, #93c5fd, transparent 60%); animation-delay: 1.2s; }
.bg-3 { width: 280px; height: 280px; top: 40%; left: 60%; background: radial-gradient(circle at 50% 50%, #f0abfc, transparent 60%); animation-delay: 2.1s; }

/* Glassmorphic container */
.glass-container {
  position: relative;
  z-index: 1;
  margin: 0 auto;
  max-width: 1200px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  padding: 20px;
  backdrop-filter: saturate(180%) blur(14px);
}

/* Glass cards */
.card-glass {
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid transparent;
  background-image:
    linear-gradient(180deg, rgba(255,255,255,0.75), rgba(255,255,255,0.6)) ,
    linear-gradient(135deg, #d1fae5, #bfdbfe, #f5d0fe);
  background-origin: border-box;
  background-clip: padding-box, border-box;
  border-radius: 16px;
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
  padding: 12px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  position: relative;
}
.card-glass:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(0,0,0,0.10);
}
.card-glass::after {
  content: "";
  position: absolute;
  inset: -1px;
  border-radius: inherit;
  background: linear-gradient(120deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.6) 15%, rgba(255,255,255,0) 30%);
  transform: translateX(-120%);
  transition: transform 0.6s ease;
  pointer-events: none;
}
.card-glass:hover::after { transform: translateX(120%); }
.card-media { position: relative; }
.thumb-row { display:flex; gap:8px; margin-top:8px; }
.thumb { transition: transform 0.2s ease, box-shadow 0.2s ease; }
.thumb:hover { transform: translateY(-2px) scale(1.02); box-shadow: 0 6px 14px rgba(0,0,0,0.10); }
.card-body { padding: 10px 4px 4px; }

/* Card clickable affordance */
.card-clickable { cursor: pointer; }
.card-clickable .btn-ghost { cursor: default; }

/* Dialog glass surface */
.glass-surface {
  background: rgba(255, 255, 255, 0.6) !important;
  backdrop-filter: saturate(180%) blur(12px);
  border: 1px solid rgba(255,255,255,0.7);
  border-radius: 20px !important;
  overflow: hidden;
}

/* Buttons */
.btn-gradient {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 35%, #0ea5e9 100%) !important;
  color: #ffffff !important;
  border: none !important;
  box-shadow: 0 8px 18px rgba(34,197,94,0.25) !important;
}
.btn-gradient:hover { filter: brightness(1.05); transform: translateY(-1px); }
.btn-glass-outline {
  background: rgba(255,255,255,0.55) !important;
  color: #0f766e !important;
  border: 1px solid rgba(15,118,110,0.25) !important;
  backdrop-filter: blur(8px);
}
.btn-ghost { color: #0f766e !important; }
.btn-ghost:hover { background: rgba(15,118,110,0.08) !important; }

/* Chip */
.chip-elevated { box-shadow: 0 4px 12px rgba(0,0,0,0.10); backdrop-filter: blur(6px); }

/* Skeletons */
.skeleton-grid { display: grid; grid-template-columns: repeat(1, minmax(0, 1fr)); gap: 1.25rem; }
@media (min-width: 640px) { .skeleton-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (min-width: 1024px) { .skeleton-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); } }
.skeleton-card {
  border-radius: 16px;
  padding: 12px;
  background: linear-gradient(180deg, rgba(255,255,255,0.75), rgba(255,255,255,0.6));
  border: 1px solid rgba(255,255,255,0.7);
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
}
.skeleton-img, .skeleton-line {
  position: relative;
  overflow: hidden;
  background: #eef2f7;
  border-radius: 12px;
}
.skeleton-img { height: 180px; }
.skeleton-line { height: 12px; margin-top: 8px; }
.skeleton-img::after, .skeleton-line::after {
  content: "";
  position: absolute; inset: 0;
  background: linear-gradient(90deg, rgba(255,255,255,0), rgba(255,255,255,0.7), rgba(255,255,255,0));
  transform: translateX(-100%);
  animation: shimmer 1.6s infinite;
}

/* Description Clamp */
.desc-clamp {
  position: relative;
  color: #475569;
  font-size: 12px;
  line-height: 1.25rem;
  max-height: calc(1.25rem * 3);
  overflow: hidden;
}
.desc-clamp::after {
  content: '';
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 28px;
  background: linear-gradient(180deg, rgba(255,255,255,0), rgba(255,255,255,0.9));
}

/* Details meta */
.meta-card { background: rgba(255,255,255,0.55); border: 1px solid rgba(255,255,255,0.7); border-radius: 12px; padding: 12px; }
.meta-label { font-size: 10px; color: #6b7280; text-transform: uppercase; letter-spacing: .05em; }
.meta-value { font-size: 13px; color: #111827; font-weight: 600; }
.history-list { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.history-item { background: rgba(255,255,255,0.55); border: 1px solid rgba(255,255,255,0.7); border-radius: 10px; padding: 10px; }

/* Dialog title */
.dialog-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

/* Keyframes */
@keyframes shimmer { to { transform: translateX(100%); } }
@keyframes float {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(0, 8px, 0) scale(1.02); }
}

@media (max-width: 768px) {
  .glass-container { padding: 16px; }
}
</style>
