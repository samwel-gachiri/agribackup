<template>
  <Default>
    <div class="ar-dashboard tw-min-h-screen tw-bg-gradient-to-br tw-from-slate-50 tw-to-purple-50">
      <!-- Page Header -->
      <div class="tw-bg-white tw-border-b tw-border-slate-200">
        <div class="tw-max-w-7xl tw-mx-auto tw-px-4 sm:tw-px-6 lg:tw-px-8 tw-py-6">
          <div class="tw-flex tw-flex-col md:tw-flex-row md:tw-items-center md:tw-justify-between tw-gap-4">
            <div>
              <h1 class="tw-text-2xl tw-font-bold tw-text-slate-900 tw-flex tw-items-center tw-gap-3">
                <v-icon color="deep-purple" large>mdi-account-tie</v-icon>
                Authorised Representative Dashboard
              </h1>
              <p class="tw-text-slate-600 tw-mt-1">
                Manage your EUDR Article 6 mandates and act on behalf of non-EU exporters
              </p>
            </div>
            <div class="tw-flex tw-items-center tw-gap-3">
              <v-chip color="deep-purple" text-color="white" v-if="arProfile">
                <v-icon left small>mdi-shield-check</v-icon>
                {{ arProfile.isVerified ? 'Verified AR' : 'Pending Verification' }}
              </v-chip>
            </div>
          </div>
        </div>
      </div>

      <div class="tw-max-w-7xl tw-mx-auto tw-px-4 sm:tw-px-6 lg:tw-px-8 tw-py-8">
        <!-- AR Profile Card -->
        <div class="tw-bg-white tw-rounded-2xl tw-shadow-lg tw-border tw-border-slate-200 tw-p-6 tw-mb-8" v-if="arProfile">
          <div class="tw-flex tw-flex-col md:tw-flex-row tw-gap-6">
            <div class="tw-flex-shrink-0">
              <div class="tw-w-20 tw-h-20 tw-bg-gradient-to-br tw-from-purple-500 tw-to-purple-700 tw-rounded-2xl tw-flex tw-items-center tw-justify-center">
                <v-icon color="white" size="40">mdi-account-tie</v-icon>
              </div>
            </div>
            <div class="tw-flex-1">
              <h2 class="tw-text-xl tw-font-bold tw-text-slate-900">{{ arProfile.companyName }}</h2>
              <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-3 tw-gap-4 tw-mt-4">
                <div>
                  <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">EORI Number</p>
                  <p class="tw-text-sm tw-font-medium tw-text-slate-900">{{ arProfile.eoriNumber }}</p>
                </div>
                <div>
                  <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">EU Member State</p>
                  <p class="tw-text-sm tw-font-medium tw-text-slate-900">{{ arProfile.euMemberState }}</p>
                </div>
                <div>
                  <p class="tw-text-xs tw-text-slate-500 tw-uppercase tw-tracking-wide">Contact Email</p>
                  <p class="tw-text-sm tw-font-medium tw-text-slate-900">{{ arProfile.contactEmail }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="tw-flex tw-justify-center tw-py-12">
          <v-progress-circular indeterminate color="deep-purple" size="48"></v-progress-circular>
        </div>

        <!-- Stats Cards -->
        <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-4 tw-gap-6 tw-mb-8" v-if="!loading">
          <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-5">
            <div class="tw-flex tw-items-center tw-gap-4">
              <div class="tw-w-12 tw-h-12 tw-bg-purple-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                <v-icon color="purple">mdi-crown</v-icon>
              </div>
              <div>
                <p class="tw-text-2xl tw-font-bold tw-text-slate-900">{{ stats.activeExporters }}</p>
                <p class="tw-text-sm tw-text-slate-500">Active Exporters</p>
              </div>
            </div>
          </div>

          <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-5">
            <div class="tw-flex tw-items-center tw-gap-4">
              <div class="tw-w-12 tw-h-12 tw-bg-orange-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                <v-icon color="orange">mdi-clock-outline</v-icon>
              </div>
              <div>
                <p class="tw-text-2xl tw-font-bold tw-text-slate-900">{{ stats.pendingMandates }}</p>
                <p class="tw-text-sm tw-text-slate-500">Pending Invites</p>
              </div>
            </div>
          </div>

          <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-5">
            <div class="tw-flex tw-items-center tw-gap-4">
              <div class="tw-w-12 tw-h-12 tw-bg-green-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                <v-icon color="green">mdi-file-sign</v-icon>
              </div>
              <div>
                <p class="tw-text-2xl tw-font-bold tw-text-slate-900">{{ stats.activeMandates }}</p>
                <p class="tw-text-sm tw-text-slate-500">Active Mandates</p>
              </div>
            </div>
          </div>

          <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-5">
            <div class="tw-flex tw-items-center tw-gap-4">
              <div class="tw-w-12 tw-h-12 tw-bg-red-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                <v-icon color="red">mdi-calendar-alert</v-icon>
              </div>
              <div>
                <p class="tw-text-2xl tw-font-bold tw-text-slate-900">{{ stats.expiringSoon }}</p>
                <p class="tw-text-sm tw-text-slate-500">Expiring Soon</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Pending Mandate Invites -->
        <div class="tw-bg-white tw-rounded-2xl tw-shadow-lg tw-border tw-border-slate-200 tw-mb-8" v-if="pendingMandates.length > 0">
          <div class="tw-px-6 tw-py-4 tw-border-b tw-border-slate-200">
            <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-flex tw-items-center tw-gap-2">
              <v-icon color="orange">mdi-bell-ring</v-icon>
              Pending Mandate Invitations
            </h3>
          </div>
          <div class="tw-divide-y tw-divide-slate-100">
            <div v-for="mandate in pendingMandates" :key="mandate.id" class="tw-p-6">
              <div class="tw-flex tw-flex-col md:tw-flex-row md:tw-items-center tw-justify-between tw-gap-4">
                <div class="tw-flex tw-items-start tw-gap-4">
                  <div class="tw-w-12 tw-h-12 tw-bg-orange-100 tw-rounded-full tw-flex tw-items-center tw-justify-center">
                    <v-icon color="orange">mdi-crown</v-icon>
                  </div>
                  <div>
                    <p class="tw-font-semibold tw-text-slate-900">{{ mandate.exporterName }}</p>
                    <p class="tw-text-sm tw-text-slate-500">{{ mandate.exporterCountry }}</p>
                    <div class="tw-flex tw-items-center tw-gap-2 tw-mt-2">
                      <v-chip x-small :color="getScopeColor(mandate.scope)" text-color="white">
                        {{ mandate.scope }}
                      </v-chip>
                      <span class="tw-text-xs tw-text-slate-400">
                        Valid: {{ formatDate(mandate.validFrom) }} - {{ formatDate(mandate.validTo) }}
                      </span>
                    </div>
                    <p v-if="mandate.message" class="tw-text-sm tw-text-slate-600 tw-mt-2 tw-italic">
                      "{{ mandate.message }}"
                    </p>
                  </div>
                </div>
                <div class="tw-flex tw-gap-2">
                  <v-btn color="success" @click="acceptMandate(mandate.id)" :loading="processingMandate === mandate.id">
                    <v-icon left>mdi-check</v-icon>
                    Accept
                  </v-btn>
                  <v-btn outlined color="error" @click="rejectMandate(mandate.id)" :loading="processingMandate === mandate.id">
                    <v-icon left>mdi-close</v-icon>
                    Decline
                  </v-btn>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Quick Actions -->
        <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-3 tw-gap-6 tw-mb-8">
          <router-link :to="{ name: 'ARMandates' }" class="tw-group">
            <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-6 hover:tw-shadow-lg tw-transition-shadow">
              <div class="tw-flex tw-items-center tw-gap-4">
                <div class="tw-w-14 tw-h-14 tw-bg-green-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center group-hover:tw-bg-green-200 tw-transition-colors">
                  <v-icon color="green" size="28">mdi-file-sign</v-icon>
                </div>
                <div>
                  <h4 class="tw-font-bold tw-text-slate-900">Manage Mandates</h4>
                  <p class="tw-text-sm tw-text-slate-500">View and manage your mandates</p>
                </div>
              </div>
            </div>
          </router-link>

          <router-link :to="{ name: 'ARExporters' }" class="tw-group">
            <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-6 hover:tw-shadow-lg tw-transition-shadow">
              <div class="tw-flex tw-items-center tw-gap-4">
                <div class="tw-w-14 tw-h-14 tw-bg-purple-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center group-hover:tw-bg-purple-200 tw-transition-colors">
                  <v-icon color="purple" size="28">mdi-crown</v-icon>
                </div>
                <div>
                  <h4 class="tw-font-bold tw-text-slate-900">My Exporters</h4>
                  <p class="tw-text-sm tw-text-slate-500">Access exporter data on behalf</p>
                </div>
              </div>
            </div>
          </router-link>

          <router-link :to="{ name: 'Documentation' }" class="tw-group">
            <div class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-slate-200 tw-p-6 hover:tw-shadow-lg tw-transition-shadow">
              <div class="tw-flex tw-items-center tw-gap-4">
                <div class="tw-w-14 tw-h-14 tw-bg-blue-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center group-hover:tw-bg-blue-200 tw-transition-colors">
                  <v-icon color="blue" size="28">mdi-book-open-page-variant</v-icon>
                </div>
                <div>
                  <h4 class="tw-font-bold tw-text-slate-900">Documentation</h4>
                  <p class="tw-text-sm tw-text-slate-500">EUDR Article 6 guidelines</p>
                </div>
              </div>
            </div>
          </router-link>
        </div>

        <!-- Active Mandates Table -->
        <div class="tw-bg-white tw-rounded-2xl tw-shadow-lg tw-border tw-border-slate-200" v-if="activeMandates.length > 0">
          <div class="tw-px-6 tw-py-4 tw-border-b tw-border-slate-200 tw-flex tw-items-center tw-justify-between">
            <h3 class="tw-text-lg tw-font-bold tw-text-slate-900">Active Mandates</h3>
            <v-btn text color="deep-purple" :to="{ name: 'ARMandates' }">
              View All
              <v-icon right>mdi-chevron-right</v-icon>
            </v-btn>
          </div>
          <v-simple-table>
            <thead>
              <tr>
                <th>Exporter</th>
                <th>Country</th>
                <th>Scope</th>
                <th>Valid Until</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="mandate in activeMandates.slice(0, 5)" :key="mandate.id">
                <td class="tw-font-medium">{{ mandate.exporterName }}</td>
                <td>{{ mandate.exporterCountry }}</td>
                <td>
                  <v-chip x-small :color="getScopeColor(mandate.scope)" text-color="white">
                    {{ mandate.scope }}
                  </v-chip>
                </td>
                <td :class="isExpiringSoon(mandate.validTo) ? 'tw-text-red-600 tw-font-medium' : ''">
                  {{ formatDate(mandate.validTo) }}
                  <v-icon v-if="isExpiringSoon(mandate.validTo)" small color="red">mdi-alert</v-icon>
                </td>
                <td>
                  <v-btn x-small color="primary" :to="{ name: 'ARExporterView', params: { exporterId: mandate.exporterId } }">
                    View Data
                  </v-btn>
                </td>
              </tr>
            </tbody>
          </v-simple-table>
        </div>

        <!-- Empty State -->
        <div v-if="!loading && activeMandates.length === 0 && pendingMandates.length === 0" class="tw-bg-white tw-rounded-2xl tw-shadow-lg tw-border tw-border-slate-200 tw-p-12 tw-text-center">
          <div class="tw-w-24 tw-h-24 tw-bg-purple-100 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-mx-auto tw-mb-6">
            <v-icon size="48" color="purple">mdi-file-sign</v-icon>
          </div>
          <h3 class="tw-text-xl tw-font-bold tw-text-slate-900 tw-mb-2">No Active Mandates</h3>
          <p class="tw-text-slate-600 tw-mb-6 tw-max-w-md tw-mx-auto">
            You don't have any active mandates yet. Non-EU exporters can invite you to act as their Authorised Representative for EUDR compliance.
          </p>
          <v-btn color="deep-purple" :to="{ name: 'Documentation' }">
            <v-icon left>mdi-book-open-page-variant</v-icon>
            Learn About AR Role
          </v-btn>
        </div>
      </div>
    </div>
  </Default>
</template>

<script>
import Default from '@/components/layout/Default.vue';
import authorisedRepresentativeService from '@/services/authorisedRepresentativeService';

export default {
  name: 'ARDashboard',
  components: { Default },
  data() {
    return {
      loading: true,
      processingMandate: null,
      arProfile: null,
      pendingMandates: [],
      activeMandates: [],
      stats: {
        activeExporters: 0,
        pendingMandates: 0,
        activeMandates: 0,
        expiringSoon: 0,
      },
    };
  },
  mounted() {
    this.loadDashboardData();
  },
  methods: {
    async loadDashboardData() {
      this.loading = true;
      try {
        // Load AR profile
        const arId = this.$store.getters['auth/userId'];
        if (arId) {
          const profileRes = await authorisedRepresentativeService.getARById(arId);
          this.arProfile = profileRes.data;

          // Load mandates for this AR
          const mandatesRes = await authorisedRepresentativeService.getMandatesForAR(arId);
          const mandates = mandatesRes.data || [];

          this.pendingMandates = mandates.filter((m) => m.status === 'PENDING_AR_ACCEPTANCE');
          this.activeMandates = mandates.filter((m) => m.status === 'ACTIVE');

          // Calculate stats
          this.stats.pendingMandates = this.pendingMandates.length;
          this.stats.activeMandates = this.activeMandates.length;
          this.stats.activeExporters = new Set(this.activeMandates.map((m) => m.exporterId)).size;
          this.stats.expiringSoon = this.activeMandates.filter((m) => this.isExpiringSoon(m.validTo)).length;
        }
      } catch (error) {
        this.$toast.error('Failed to load AR dashboard:', error.message);
      } finally {
        this.loading = false;
      }
    },
    async acceptMandate(mandateId) {
      this.processingMandate = mandateId;
      try {
        await authorisedRepresentativeService.acceptMandate(mandateId);
        this.$toast.success('Mandate accepted successfully');
        await this.loadDashboardData();
      } catch (error) {
        this.$toast.error('Failed to accept mandate:', error.message);
      } finally {
        this.processingMandate = null;
      }
    },
    async rejectMandate(mandateId) {
      this.processingMandate = mandateId;
      try {
        await authorisedRepresentativeService.rejectMandate(mandateId);
        this.$toast.success('Mandate declined');
        await this.loadDashboardData();
      } catch (error) {
        this.$toast.error('Failed to decline mandate:', error.message);
      } finally {
        this.processingMandate = null;
      }
    },
    getScopeColor(scope) {
      const colors = {
        FULL: 'green',
        DDS_ONLY: 'blue',
        VIEW_ONLY: 'grey',
      };
      return colors[scope] || 'grey';
    },
    formatDate(date) {
      if (!date) return 'N/A';
      return new Date(date).toLocaleDateString('en-GB', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
      });
    },
    isExpiringSoon(validTo) {
      if (!validTo) return false;
      const days = Math.ceil((new Date(validTo) - new Date()) / (1000 * 60 * 60 * 24));
      return days <= 30 && days > 0;
    },
  },
};
</script>

<style scoped>
.ar-dashboard a {
  text-decoration: none;
}
</style>
