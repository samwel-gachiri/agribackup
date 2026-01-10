<template>
  <Default>
    <div class="ar-exporters tw-min-h-screen tw-bg-gradient-to-br tw-from-slate-50 tw-to-purple-50">
      <!-- Page Header -->
      <div class="tw-bg-white tw-border-b tw-border-slate-200">
        <div class="tw-max-w-7xl tw-mx-auto tw-px-4 sm:tw-px-6 lg:tw-px-8 tw-py-6">
          <div class="tw-flex tw-flex-col md:tw-flex-row md:tw-items-center md:tw-justify-between tw-gap-4">
            <div>
              <h1 class="tw-text-2xl tw-font-bold tw-text-slate-900 tw-flex tw-items-center tw-gap-3">
                <v-icon color="purple" large>mdi-crown</v-icon>
                My Exporters
              </h1>
              <p class="tw-text-slate-600 tw-mt-1">
                Exporters you are authorized to represent under EUDR Article 6
              </p>
            </div>
            <div>
              <v-btn color="deep-purple" :to="{ name: 'ARDashboard' }">
                <v-icon left>mdi-arrow-left</v-icon>
                Back to Dashboard
              </v-btn>
            </div>
          </div>
        </div>
      </div>

      <div class="tw-max-w-7xl tw-mx-auto tw-px-4 sm:tw-px-6 lg:tw-px-8 tw-py-8">
        <!-- Loading -->
        <div v-if="loading" class="tw-flex tw-justify-center tw-py-12">
          <v-progress-circular indeterminate color="deep-purple" size="48"></v-progress-circular>
        </div>

        <!-- Empty State -->
        <div v-else-if="exporters.length === 0" class="tw-bg-white tw-rounded-2xl tw-shadow-lg tw-border tw-border-slate-200 tw-p-12 tw-text-center">
          <div class="tw-w-24 tw-h-24 tw-bg-purple-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mx-auto tw-mb-6">
            <v-icon size="48" color="purple">mdi-crown</v-icon>
          </div>
          <h3 class="tw-text-xl tw-font-bold tw-text-slate-900 tw-mb-2">No Exporters Yet</h3>
          <p class="tw-text-slate-600 tw-mb-6 tw-max-w-md tw-mx-auto">
            You don't have any active exporters yet. Accept mandate invitations from non-EU exporters to start representing them.
          </p>
          <v-btn color="deep-purple" :to="{ name: 'ARMandates' }">
            <v-icon left>mdi-file-sign</v-icon>
            View Pending Mandates
          </v-btn>
        </div>

        <!-- Exporters Grid -->
        <div v-else class="tw-grid tw-grid-cols-1 md:tw-grid-cols-2 lg:tw-grid-cols-3 tw-gap-6">
          <div v-for="exporter in exporters" :key="exporter.id" class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-overflow-hidden hover:tw-shadow-lg tw-transition-shadow">
            <div class="tw-p-6">
              <div class="tw-flex tw-items-start tw-gap-4">
                <div class="tw-w-14 tw-h-14 tw-bg-gradient-to-br tw-from-purple-500 tw-to-purple-700 tw-rounded-xl tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
                  <v-icon color="white" size="28">mdi-crown</v-icon>
                </div>
                <div class="tw-flex-1 tw-min-w-0">
                  <h3 class="tw-font-bold tw-text-slate-900 tw-truncate">{{ exporter.companyName }}</h3>
                  <p class="tw-text-sm tw-text-slate-500 tw-flex tw-items-center tw-gap-1">
                    <v-icon x-small>mdi-map-marker</v-icon>
                    {{ exporter.country }}
                  </p>
                </div>
              </div>

              <div class="tw-mt-4 tw-space-y-2">
                <div class="tw-flex tw-justify-between tw-text-sm">
                  <span class="tw-text-slate-500">Mandate Scope</span>
                  <v-chip x-small :color="getScopeColor(exporter.scope)" text-color="white">
                    {{ formatScope(exporter.scope) }}
                  </v-chip>
                </div>
                <div class="tw-flex tw-justify-between tw-text-sm">
                  <span class="tw-text-slate-500">Valid Until</span>
                  <span :class="isExpiringSoon(exporter.validTo) ? 'tw-text-red-600 tw-font-medium' : 'tw-text-slate-900'">
                    {{ formatDate(exporter.validTo) }}
                    <v-icon v-if="isExpiringSoon(exporter.validTo)" x-small color="red">mdi-alert</v-icon>
                  </span>
                </div>
                <div class="tw-flex tw-justify-between tw-text-sm">
                  <span class="tw-text-slate-500">Contact</span>
                  <span class="tw-text-slate-900 tw-truncate tw-max-w-[150px]">{{ exporter.contactEmail }}</span>
                </div>
              </div>
            </div>

            <div class="tw-bg-slate-50 tw-px-6 tw-py-4 tw-border-t tw-border-slate-100">
              <div class="tw-flex tw-gap-2">
                <v-btn small color="primary" block :to="{ name: 'ARExporterView', params: { exporterId: exporter.id } }">
                  <v-icon left small>mdi-eye</v-icon>
                  View & Act On Behalf
                </v-btn>
              </div>
            </div>
          </div>
        </div>

        <!-- Info Banner -->
        <div class="tw-mt-8 tw-bg-blue-50 tw-rounded-xl tw-border tw-border-blue-200 tw-p-6">
          <div class="tw-flex tw-items-start tw-gap-4">
            <div class="tw-w-10 tw-h-10 tw-bg-blue-100 tw-rounded-lg tw-flex tw-items-center tw-justify-center tw-flex-shrink-0">
              <v-icon color="blue">mdi-information</v-icon>
            </div>
            <div>
              <h4 class="tw-font-bold tw-text-blue-900 tw-mb-1">Acting on Behalf of Exporters</h4>
              <p class="tw-text-sm tw-text-blue-800">
                As an Authorised Representative, you can access exporter data and submit Due Diligence Statements (DDS)
                to EU TRACES on their behalf, depending on your mandate scope. All actions are logged for audit purposes.
              </p>
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
  name: 'ARExporters',
  components: { Default },
  data() {
    return {
      loading: true,
      exporters: [],
    };
  },
  mounted() {
    this.loadExporters();
  },
  methods: {
    async loadExporters() {
      this.loading = true;
      try {
        const arId = this.$store.getters['auth/userId'];
        if (arId) {
          // Get mandates to find exporters
          const mandatesRes = await authorisedRepresentativeService.getMandatesForAR(arId);
          const activeMandates = (mandatesRes.data || []).filter((m) => m.status === 'ACTIVE');

          // For each mandate, fetch exporter details
          const exporterPromises = activeMandates.map(async (mandate) => {
            try {
              const exporterRes = await axios.get(`/api/v1/exporters/${mandate.exporterId}`);
              return {
                id: mandate.exporterId,
                companyName: exporterRes.data?.companyName || mandate.exporterName || 'Unknown',
                country: exporterRes.data?.country || mandate.exporterCountry || 'Unknown',
                contactEmail: exporterRes.data?.email || 'N/A',
                scope: mandate.scope,
                validTo: mandate.validTo,
                mandateId: mandate.id,
              };
            } catch {
              return {
                id: mandate.exporterId,
                companyName: mandate.exporterName || 'Unknown',
                country: mandate.exporterCountry || 'Unknown',
                contactEmail: 'N/A',
                scope: mandate.scope,
                validTo: mandate.validTo,
                mandateId: mandate.id,
              };
            }
          });

          this.exporters = await Promise.all(exporterPromises);
        }
      } catch (error) {
        this.$toast.error('Failed to load exporters:', error.message);
      } finally {
        this.loading = false;
      }
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
