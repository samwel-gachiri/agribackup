<template>
  <Default>
    <div class="ar-mandates tw-min-h-screen tw-bg-gradient-to-br tw-from-slate-50 tw-to-green-50">
      <!-- Page Header -->
      <div class="tw-bg-white tw-border-b tw-border-slate-200">
        <div class="tw-max-w-7xl tw-mx-auto tw-px-4 sm:tw-px-6 lg:tw-px-8 tw-py-6">
          <div class="tw-flex tw-flex-col md:tw-flex-row md:tw-items-center md:tw-justify-between tw-gap-4">
            <div>
              <h1 class="tw-text-2xl tw-font-bold tw-text-slate-900 tw-flex tw-items-center tw-gap-3">
                <v-icon color="green" large>mdi-file-sign</v-icon>
                My Mandates
              </h1>
              <p class="tw-text-slate-600 tw-mt-1">
                View and manage your representation mandates
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
        <!-- Tabs -->
        <v-tabs v-model="tab" color="deep-purple" class="tw-mb-6 tw-bg-white tw-rounded-lg tw-shadow-sm">
          <v-tab>
            <v-icon left>mdi-clock-outline</v-icon>
            Pending ({{ pendingMandates.length }})
          </v-tab>
          <v-tab>
            <v-icon left>mdi-check-circle</v-icon>
            Active ({{ activeMandates.length }})
          </v-tab>
          <v-tab>
            <v-icon left>mdi-history</v-icon>
            History ({{ historicalMandates.length }})
          </v-tab>
        </v-tabs>

        <!-- Loading -->
        <div v-if="loading" class="tw-flex tw-justify-center tw-py-12">
          <v-progress-circular indeterminate color="deep-purple" size="48"></v-progress-circular>
        </div>

        <v-tabs-items v-model="tab" v-if="!loading">
          <!-- Pending Tab -->
          <v-tab-item>
            <div v-if="pendingMandates.length === 0" class="tw-bg-white tw-rounded-xl tw-shadow-md tw-p-12 tw-text-center">
              <v-icon size="64" color="grey lighten-2">mdi-inbox-outline</v-icon>
              <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mt-4">No Pending Invitations</h3>
              <p class="tw-text-slate-500">You don't have any pending mandate invitations.</p>
            </div>
            <div v-else class="tw-space-y-4">
              <div v-for="mandate in pendingMandates" :key="mandate.id" class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-orange-200 tw-p-6">
                <div class="tw-flex tw-flex-col lg:tw-flex-row lg:tw-items-center tw-justify-between tw-gap-4">
                  <div class="tw-flex tw-items-start tw-gap-4">
                    <div class="tw-w-14 tw-h-14 tw-bg-orange-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                      <v-icon color="orange" size="28">mdi-crown</v-icon>
                    </div>
                    <div>
                      <h4 class="tw-font-bold tw-text-slate-900 tw-text-lg">{{ mandate.exporterName }}</h4>
                      <p class="tw-text-sm tw-text-slate-500">{{ mandate.exporterCountry }}</p>
                      <div class="tw-flex tw-flex-wrap tw-items-center tw-gap-2 tw-mt-3">
                        <v-chip small :color="getScopeColor(mandate.scope)" text-color="white">
                          {{ formatScope(mandate.scope) }}
                        </v-chip>
                        <v-chip small outlined>
                          <v-icon left x-small>mdi-calendar</v-icon>
                          {{ formatDate(mandate.validFrom) }} - {{ formatDate(mandate.validTo) }}
                        </v-chip>
                        <v-chip small outlined color="orange">
                          <v-icon left x-small>mdi-clock</v-icon>
                          Invited {{ formatDate(mandate.createdAt) }}
                        </v-chip>
                      </div>
                      <p v-if="mandate.message" class="tw-text-sm tw-text-slate-600 tw-mt-3 tw-p-3 tw-bg-slate-50 tw-rounded-lg tw-italic">
                        "{{ mandate.message }}"
                      </p>
                    </div>
                  </div>
                  <div class="tw-flex tw-gap-2 tw-flex-shrink-0">
                    <v-btn color="success" @click="acceptMandate(mandate.id)" :loading="processingId === mandate.id">
                      <v-icon left>mdi-check</v-icon>
                      Accept
                    </v-btn>
                    <v-btn outlined color="error" @click="openRejectDialog(mandate)" :loading="processingId === mandate.id">
                      <v-icon left>mdi-close</v-icon>
                      Decline
                    </v-btn>
                  </div>
                </div>
              </div>
            </div>
          </v-tab-item>

          <!-- Active Tab -->
          <v-tab-item>
            <div v-if="activeMandates.length === 0" class="tw-bg-white tw-rounded-xl tw-shadow-md tw-p-12 tw-text-center">
              <v-icon size="64" color="grey lighten-2">mdi-file-sign</v-icon>
              <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mt-4">No Active Mandates</h3>
              <p class="tw-text-slate-500">You don't have any active mandates yet.</p>
            </div>
            <div v-else class="tw-space-y-4">
              <div v-for="mandate in activeMandates" :key="mandate.id" class="tw-bg-white tw-rounded-xl tw-shadow-md tw-border tw-border-green-200 tw-p-6">
                <div class="tw-flex tw-flex-col lg:tw-flex-row lg:tw-items-center tw-justify-between tw-gap-4">
                  <div class="tw-flex tw-items-start tw-gap-4">
                    <div class="tw-w-14 tw-h-14 tw-bg-green-100 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
                      <v-icon color="green" size="28">mdi-check-circle</v-icon>
                    </div>
                    <div>
                      <h4 class="tw-font-bold tw-text-slate-900 tw-text-lg">{{ mandate.exporterName }}</h4>
                      <p class="tw-text-sm tw-text-slate-500">{{ mandate.exporterCountry }}</p>
                      <div class="tw-flex tw-flex-wrap tw-items-center tw-gap-2 tw-mt-3">
                        <v-chip small :color="getScopeColor(mandate.scope)" text-color="white">
                          {{ formatScope(mandate.scope) }}
                        </v-chip>
                        <v-chip small :color="isExpiringSoon(mandate.validTo) ? 'error' : 'success'" outlined>
                          <v-icon left x-small>mdi-calendar</v-icon>
                          Valid until {{ formatDate(mandate.validTo) }}
                          <span v-if="isExpiringSoon(mandate.validTo)" class="tw-ml-1">(Expiring soon!)</span>
                        </v-chip>
                      </div>
                    </div>
                  </div>
                  <div class="tw-flex tw-gap-2 tw-flex-shrink-0">
                    <v-btn color="primary" :to="{ name: 'ARExporterView', params: { exporterId: mandate.exporterId } }">
                      <v-icon left>mdi-eye</v-icon>
                      View Data
                    </v-btn>
                    <v-btn outlined color="error" @click="openRevokeDialog(mandate)">
                      <v-icon left>mdi-close-circle</v-icon>
                      Revoke
                    </v-btn>
                  </div>
                </div>
              </div>
            </div>
          </v-tab-item>

          <!-- History Tab -->
          <v-tab-item>
            <div v-if="historicalMandates.length === 0" class="tw-bg-white tw-rounded-xl tw-shadow-md tw-p-12 tw-text-center">
              <v-icon size="64" color="grey lighten-2">mdi-history</v-icon>
              <h3 class="tw-text-lg tw-font-bold tw-text-slate-900 tw-mt-4">No History</h3>
              <p class="tw-text-slate-500">No expired or revoked mandates.</p>
            </div>
            <div v-else>
              <v-simple-table class="tw-bg-white tw-rounded-xl tw-shadow-md">
                <thead>
                  <tr>
                    <th>Exporter</th>
                    <th>Country</th>
                    <th>Scope</th>
                    <th>Period</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="mandate in historicalMandates" :key="mandate.id">
                    <td class="tw-font-medium">{{ mandate.exporterName }}</td>
                    <td>{{ mandate.exporterCountry }}</td>
                    <td>
                      <v-chip x-small :color="getScopeColor(mandate.scope)" text-color="white">
                        {{ formatScope(mandate.scope) }}
                      </v-chip>
                    </td>
                    <td>{{ formatDate(mandate.validFrom) }} - {{ formatDate(mandate.validTo) }}</td>
                    <td>
                      <v-chip x-small :color="getStatusColor(mandate.status)" text-color="white">
                        {{ mandate.status }}
                      </v-chip>
                    </td>
                  </tr>
                </tbody>
              </v-simple-table>
            </div>
          </v-tab-item>
        </v-tabs-items>
      </div>

      <!-- Reject Dialog -->
      <v-dialog v-model="rejectDialog" max-width="500">
        <v-card>
          <v-card-title>Decline Mandate Invitation</v-card-title>
          <v-card-text>
            <p class="tw-mb-4">Are you sure you want to decline the mandate from <strong>{{ selectedMandate?.exporterName }}</strong>?</p>
            <v-textarea
              v-model="rejectReason"
              label="Reason (optional)"
              outlined
              rows="3"
              placeholder="Explain why you're declining..."
            ></v-textarea>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn text @click="rejectDialog = false">Cancel</v-btn>
            <v-btn color="error" @click="rejectMandate" :loading="processingId === selectedMandate?.id">Decline</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- Revoke Dialog -->
      <v-dialog v-model="revokeDialog" max-width="500">
        <v-card>
          <v-card-title class="error--text">Revoke Mandate</v-card-title>
          <v-card-text>
            <v-alert type="warning" dense class="tw-mb-4">
              This will immediately end your representation of this exporter.
            </v-alert>
            <p class="tw-mb-4">Are you sure you want to revoke the mandate for <strong>{{ selectedMandate?.exporterName }}</strong>?</p>
            <v-textarea
              v-model="revokeReason"
              label="Reason"
              outlined
              rows="3"
              placeholder="Explain why you're revoking..."
              :rules="[v => !!v || 'Reason is required']"
            ></v-textarea>
          </v-card-text>
          <v-card-actions>
            <v-spacer></v-spacer>
            <v-btn text @click="revokeDialog = false">Cancel</v-btn>
            <v-btn color="error" @click="revokeMandate" :loading="processingId === selectedMandate?.id" :disabled="!revokeReason">Revoke</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </div>
  </Default>
</template>

<script>
import Default from '@/components/layout/Default.vue';
import authorisedRepresentativeService from '@/services/authorisedRepresentativeService';

export default {
  name: 'ARMandates',
  components: { Default },
  data() {
    return {
      loading: true,
      tab: 0,
      processingId: null,
      mandates: [],
      rejectDialog: false,
      revokeDialog: false,
      selectedMandate: null,
      rejectReason: '',
      revokeReason: '',
    };
  },
  computed: {
    pendingMandates() {
      return this.mandates.filter((m) => m.status === 'PENDING_AR_ACCEPTANCE');
    },
    activeMandates() {
      return this.mandates.filter((m) => m.status === 'ACTIVE');
    },
    historicalMandates() {
      return this.mandates.filter((m) => ['EXPIRED', 'REVOKED', 'REJECTED'].includes(m.status));
    },
  },
  mounted() {
    this.loadMandates();
  },
  methods: {
    async loadMandates() {
      this.loading = true;
      try {
        const arId = this.$store.getters['auth/userId'];
        if (arId) {
          const response = await authorisedRepresentativeService.getMandatesForAR(arId);
          this.mandates = response.data || [];
        }
      } catch (error) {
        this.$toast.error('Failed to load mandates:', error.message);
      } finally {
        this.loading = false;
      }
    },
    async acceptMandate(mandateId) {
      this.processingId = mandateId;
      try {
        await authorisedRepresentativeService.acceptMandate(mandateId);
        this.$toast.success('Mandate accepted successfully');
        await this.loadMandates();
      } catch (error) {
        this.$toast.error('Failed to accept mandate:', error.message);
      } finally {
        this.processingId = null;
      }
    },
    openRejectDialog(mandate) {
      this.selectedMandate = mandate;
      this.rejectReason = '';
      this.rejectDialog = true;
    },
    async rejectMandate() {
      if (!this.selectedMandate) return;
      this.processingId = this.selectedMandate.id;
      try {
        await authorisedRepresentativeService.rejectMandate(this.selectedMandate.id, this.rejectReason);
        this.$toast.success('Mandate declined');
        this.rejectDialog = false;
        await this.loadMandates();
      } catch (error) {
        this.$toast.error('Failed to decline mandate:', error.message);
      } finally {
        this.processingId = null;
      }
    },
    openRevokeDialog(mandate) {
      this.selectedMandate = mandate;
      this.revokeReason = '';
      this.revokeDialog = true;
    },
    async revokeMandate() {
      if (!this.selectedMandate || !this.revokeReason) return;
      this.processingId = this.selectedMandate.id;
      try {
        const arId = this.$store.getters['auth/userId'];
        await authorisedRepresentativeService.revokeMandate(this.selectedMandate.id, arId, this.revokeReason);
        this.$toast.success('Mandate revoked');
        this.revokeDialog = false;
        await this.loadMandates();
      } catch (error) {
        this.$toast.error('Failed to revoke mandate:', error.message);
      } finally {
        this.processingId = null;
      }
    },
    getScopeColor(scope) {
      const colors = { FULL: 'green', DDS_ONLY: 'blue', VIEW_ONLY: 'grey' };
      return colors[scope] || 'grey';
    },
    getStatusColor(status) {
      const colors = {
        ACTIVE: 'success',
        EXPIRED: 'grey',
        REVOKED: 'error',
        REJECTED: 'warning',
      };
      return colors[status] || 'grey';
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
