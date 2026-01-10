<template>
  <Default>
    <div class="ar-exporter-view tw-min-h-screen tw-bg-gradient-to-br tw-from-slate-50 tw-to-purple-50">
      <!-- Acting As Banner -->
      <div class="tw-bg-gradient-to-r tw-from-purple-600 tw-to-purple-800 tw-text-white tw-py-3 tw-px-4">
        <div class="tw-max-w-7xl tw-mx-auto tw-flex tw-items-center tw-justify-between">
          <div class="tw-flex tw-items-center tw-gap-3">
            <v-icon color="white">mdi-account-switch</v-icon>
            <span class="tw-font-medium">
              Acting on behalf of: <strong>{{ exporter?.companyName || 'Loading...' }}</strong>
            </span>
            <v-chip small color="white" text-color="purple" v-if="mandate">
              {{ formatScope(mandate.scope) }}
            </v-chip>
          </div>
          <v-btn small color="white" text @click="$router.push({ name: 'ARExporters' })">
            <v-icon left small>mdi-close</v-icon>
            Exit View
          </v-btn>
        </div>
      </div>

      <!-- Page Header -->
      <div class="tw-bg-white tw-border-b tw-border-slate-200 tw-sticky tw-top-0 tw-z-10">
        <div class="tw-max-w-7xl tw-mx-auto tw-px-4 sm:tw-px-6 lg:tw-px-8 tw-py-6">
          <div class="tw-flex tw-flex-col md:tw-flex-row md:tw-items-center md:tw-justify-between tw-gap-4">
            <div class="tw-flex tw-items-center tw-gap-4">
              <div class="tw-w-16 tw-h-16 tw-bg-gradient-to-br tw-from-purple-500 tw-to-purple-700 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                <v-icon color="white" size="32">mdi-crown</v-icon>
              </div>
              <div>
                <h1 class="tw-text-2xl tw-font-bold tw-text-slate-900">{{ exporter?.companyName }}</h1>
                <p class="tw-text-slate-600">{{ exporter?.country }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="tw-max-w-7xl tw-mx-auto tw-px-4 sm:tw-px-6 lg:tw-px-8 tw-py-8">
        <!-- Loading -->
        <div v-if="loading" class="tw-flex tw-justify-center tw-py-12">
          <v-progress-circular indeterminate color="deep-purple" size="48"></v-progress-circular>
        </div>

        <div v-else>
          <!-- Scope Restriction Alert -->
          <v-alert v-if="mandate?.scope === 'VIEW_ONLY'" type="info" prominent class="tw-mb-6">
            <strong>View Only Access:</strong> Your mandate only allows viewing data. You cannot submit DDS or modify data on behalf of this exporter.
          </v-alert>

          <!-- Quick Stats -->
          <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-4 tw-gap-6 tw-mb-8">
            <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-5">
              <div class="tw-flex tw-items-center tw-gap-4">
                <div class="tw-w-12 tw-h-12 tw-bg-green-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                  <v-icon color="green">mdi-barn</v-icon>
                </div>
                <div>
                  <p class="tw-text-2xl tw-font-bold tw-text-slate-900">{{ stats.farmers }}</p>
                  <p class="tw-text-sm tw-text-slate-500">Farmers</p>
                </div>
              </div>
            </div>

            <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-5">
              <div class="tw-flex tw-items-center tw-gap-4">
                <div class="tw-w-12 tw-h-12 tw-bg-blue-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                  <v-icon color="blue">mdi-map-marker-radius</v-icon>
                </div>
                <div>
                  <p class="tw-text-2xl tw-font-bold tw-text-slate-900">{{ stats.productionUnits }}</p>
                  <p class="tw-text-sm tw-text-slate-500">Production Units</p>
                </div>
              </div>
            </div>

            <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-5">
              <div class="tw-flex tw-items-center tw-gap-4">
                <div class="tw-w-12 tw-h-12 tw-bg-orange-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                  <v-icon color="orange">mdi-certificate</v-icon>
                </div>
                <div>
                  <p class="tw-text-2xl tw-font-bold tw-text-slate-900">{{ stats.certificates }}</p>
                  <p class="tw-text-sm tw-text-slate-500">Certificates</p>
                </div>
              </div>
            </div>

            <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-5">
              <div class="tw-flex tw-items-center tw-gap-4">
                <div class="tw-w-12 tw-h-12 tw-bg-red-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                  <v-icon color="red">mdi-alert-octagon</v-icon>
                </div>
                <div>
                  <p class="tw-text-2xl tw-font-bold tw-text-slate-900">{{ stats.pendingRisks }}</p>
                  <p class="tw-text-sm tw-text-slate-500">Pending Risks</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Quick Actions for AR -->
          <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mb-4">Actions You Can Take</h3>
          <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-3 tw-gap-6 tw-mb-8">
            <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-6">
              <div class="tw-flex tw-items-center tw-gap-4 tw-mb-4">
                <div class="tw-w-12 tw-h-12 tw-bg-green-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                  <v-icon color="green">mdi-certificate</v-icon>
                </div>
                <div>
                  <h4 class="tw-font-bold tw-text-slate-900">View Certificates</h4>
                  <p class="tw-text-sm tw-text-slate-500">EUDR compliance certificates</p>
                </div>
              </div>
              <v-btn color="green" block outlined @click="viewCertificates">
                <v-icon left>mdi-eye</v-icon>
                View Certificates
              </v-btn>
            </div>

            <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-6">
              <div class="tw-flex tw-items-center tw-gap-4 tw-mb-4">
                <div class="tw-w-12 tw-h-12 tw-bg-blue-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                  <v-icon color="blue">mdi-transit-connection-variant</v-icon>
                </div>
                <div>
                  <h4 class="tw-font-bold tw-text-slate-900">Supply Chain</h4>
                  <p class="tw-text-sm tw-text-slate-500">Traceability workflow</p>
                </div>
              </div>
              <v-btn color="blue" block outlined @click="viewSupplyChain">
                <v-icon left>mdi-eye</v-icon>
                View Supply Chain
              </v-btn>
            </div>

            <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-6" :class="{ 'tw-opacity-50': mandate?.scope === 'VIEW_ONLY' }">
              <div class="tw-flex tw-items-center tw-gap-4 tw-mb-4">
                <div class="tw-w-12 tw-h-12 tw-bg-purple-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                  <v-icon color="purple">mdi-file-document-edit</v-icon>
                </div>
                <div>
                  <h4 class="tw-font-bold tw-text-slate-900">Submit DDS</h4>
                  <p class="tw-text-sm tw-text-slate-500">Due Diligence Statement</p>
                </div>
              </div>
              <v-btn
                color="purple"
                block
                :disabled="mandate?.scope === 'VIEW_ONLY'"
                @click="submitDDS"
              >
                <v-icon left>mdi-send</v-icon>
                Prepare DDS
              </v-btn>
              <p v-if="mandate?.scope === 'VIEW_ONLY'" class="tw-text-xs tw-text-center tw-text-slate-400 tw-mt-2">
                Requires DDS or Full scope
              </p>
            </div>
          </div>

          <!-- Exporter Details -->
          <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-6 tw-mb-8">
            <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mb-4">Exporter Information</h3>
            <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-3 tw-gap-6">
              <div>
                <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Company Name</p>
                <p class="tw-text-sm tw-font-medium tw-text-slate-900">{{ exporter?.companyName || 'N/A' }}</p>
              </div>
              <div>
                <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Country</p>
                <p class="tw-text-sm tw-font-medium tw-text-slate-900">{{ exporter?.country || 'N/A' }}</p>
              </div>
              <div>
                <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Registration Number</p>
                <p class="tw-text-sm tw-font-medium tw-text-slate-900">{{ exporter?.registrationNumber || 'N/A' }}</p>
              </div>
              <div>
                <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Contact Email</p>
                <p class="tw-text-sm tw-font-medium tw-text-slate-900">{{ exporter?.email || 'N/A' }}</p>
              </div>
              <div>
                <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Phone</p>
                <p class="tw-text-sm tw-font-medium tw-text-slate-900">{{ exporter?.phone || 'N/A' }}</p>
              </div>
              <div>
                <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Address</p>
                <p class="tw-text-sm tw-font-medium tw-text-slate-900">{{ exporter?.address || 'N/A' }}</p>
              </div>
            </div>
          </div>

          <!-- Mandate Details -->
          <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-6">
            <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mb-4">Your Mandate</h3>
            <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-4 tw-gap-6">
              <div>
                <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Scope</p>
                <v-chip small :color="getScopeColor(mandate?.scope)" text-color="white" class="tw-mt-1">
                  {{ formatScope(mandate?.scope) }}
                </v-chip>
              </div>
              <div>
                <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Valid From</p>
                <p class="tw-text-sm tw-font-medium tw-text-slate-900">{{ formatDate(mandate?.validFrom) }}</p>
              </div>
              <div>
                <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Valid Until</p>
                <p class="tw-text-sm tw-font-medium" :class="isExpiringSoon(mandate?.validTo) ? 'tw-text-red-600' : 'tw-text-slate-900'">
                  {{ formatDate(mandate?.validTo) }}
                  <v-icon v-if="isExpiringSoon(mandate?.validTo)" small color="red">mdi-alert</v-icon>
                </p>
              </div>
              <div>
                <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Status</p>
                <v-chip small color="success" text-color="white" class="tw-mt-1">
                  Active
                </v-chip>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Default>
</template>

<script>
import Default from '@/components/layout/Default.vue';
import authorisedRepresentativeService from '@/services/authorisedRepresentativeService';
import axios from 'axios';

export default {
  name: 'ARExporterView',
  components: { Default },
  data() {
    return {
      loading: true,
      exporter: null,
      mandate: null,
      stats: {
        farmers: 0,
        productionUnits: 0,
        certificates: 0,
        pendingRisks: 0,
      },
    };
  },
  computed: {
    exporterId() {
      return this.$route.params.exporterId;
    },
  },
  mounted() {
    this.loadExporterData();
  },
  methods: {
    async loadExporterData() {
      this.loading = true;
      try {
        const arId = this.$store.getters['auth/userId'];

        // Check access
        const accessRes = await authorisedRepresentativeService.checkARAccess(arId, this.exporterId);
        if (!accessRes.data?.canAccess) {
          this.$toast.error('You do not have access to this exporter');
          this.$router.push({ name: 'ARExporters' });
          return;
        }

        // Load mandate details
        const mandatesRes = await authorisedRepresentativeService.getMandatesForAR(arId);
        this.mandate = (mandatesRes.data || []).find(
          (m) => m.exporterId === this.exporterId && m.status === 'ACTIVE',
        );

        // Load exporter details
        try {
          const exporterRes = await axios.get(`/api/v1/exporters/${this.exporterId}`);
          this.exporter = exporterRes.data;
        } catch {
          this.exporter = {
            companyName: this.mandate?.exporterName || 'Unknown',
            country: this.mandate?.exporterCountry || 'Unknown',
          };
        }

        // Load stats (mock for now - would need actual endpoints)
        await this.loadStats();
      } catch (error) {
        this.$toast.error('Failed to load exporter data:', error.message);
      } finally {
        this.loading = false;
      }
    },
    async loadStats() {
      // These would be actual API calls in production
      try {
        // Mock stats - replace with actual API calls
        this.stats = {
          farmers: Math.floor(Math.random() * 100) + 10,
          productionUnits: Math.floor(Math.random() * 200) + 20,
          certificates: Math.floor(Math.random() * 50) + 5,
          pendingRisks: Math.floor(Math.random() * 10),
        };
      } catch (error) {
        this.$toast.error('Failed to load stats:', error.message);
      }
    },
    viewCertificates() {
      // Navigate to certificate viewer with exporter context
      this.$router.push({
        name: 'CertificateViewer',
        query: { exporterId: this.exporterId, actingAs: 'ar' },
      });
    },
    viewSupplyChain() {
      // Navigate to supply chain workflow with exporter context
      this.$router.push({
        name: 'SupplyChainWorkflow',
        query: { exporterId: this.exporterId, actingAs: 'ar' },
      });
    },
    submitDDS() {
      // Navigate to DDS submission
      this.$toast.info('DDS submission coming soon');
    },
    getScopeColor(scope) {
      const colors = { FULL: 'green', DDS_ONLY: 'blue', VIEW_ONLY: 'grey' };
      return colors[scope] || 'grey';
    },
    formatScope(scope) {
      const labels = { FULL: 'Full Access', DDS_ONLY: 'DDS Only', VIEW_ONLY: 'View Only' };
      return labels[scope] || scope;
    },
    formatDate(date) {
      if (!date) return 'N/A';
      return new Date(date).toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
    },
    isExpiringSoon(validTo) {
      if (!validTo) return false;
      const days = Math.ceil((new Date(validTo) - new Date()) / (1000 * 60 * 60 * 24));
      return days <= 30 && days > 0;
    },
  },
};
</script>
