<template>
  <Default>
    <div class="ar-management-page tw-bg-gray-50 tw-min-h-screen">
      <v-container fluid class="tw-max-w-6xl tw-mx-auto tw-py-8">
        <!-- Header -->
        <div class="tw-mb-8">
          <div class="tw-flex tw-items-center tw-gap-4 tw-mb-4">
            <div class="tw-w-14 tw-h-14 tw-bg-indigo-600 tw-rounded-xl tw-flex tw-items-center tw-justify-center">
              <v-icon large color="white">mdi-account-tie</v-icon>
            </div>
            <div>
              <h1 class="tw-text-3xl tw-font-bold tw-text-gray-900 tw-mb-1">
                Authorised Representative Management
              </h1>
              <p class="tw-text-gray-600">
                EUDR Article 6 — Designate an EU-based representative to submit DDS on your behalf
              </p>
            </div>
          </div>

          <!-- Non-EU Operator Requirement Alert -->
          <v-alert
            type="info"
            prominent
            border="left"
            colored-border
            class="tw-rounded-xl"
          >
            <div class="tw-flex tw-items-start tw-gap-4">
              <v-icon color="info" large>mdi-information</v-icon>
              <div>
                <div class="tw-font-bold tw-text-lg tw-mb-2">Why do you need an Authorised Representative?</div>
                <p class="tw-mb-2">
                  Under <strong>EUDR Article 6</strong>, operators established outside the EU must designate
                  an Authorised Representative established within the EU to submit Due Diligence Statements
                  on their behalf.
                </p>
                <p class="tw-text-sm tw-opacity-90">
                  Your AR will have access to the EU TRACES system using their <strong>EORI number</strong>
                  and can submit DDS for your commodities entering the EU market.
                </p>
              </div>
            </div>
          </v-alert>
        </div>

        <!-- Main Content -->
        <v-row>
          <!-- Left Column: Current AR Status -->
          <v-col cols="12" md="5">
            <v-card class="tw-rounded-xl" elevation="2">
              <v-card-title class="tw-bg-gradient-to-r tw-from-indigo-600 tw-to-purple-600 tw-text-white tw-py-4">
                <v-icon left color="white">mdi-account-check</v-icon>
                Current Representative
              </v-card-title>

              <v-card-text class="tw-py-6">
                <!-- Loading State -->
                <div v-if="loading" class="tw-flex tw-justify-center tw-py-8">
                  <v-progress-circular indeterminate color="primary" size="48" />
                </div>

                <!-- No AR Assigned -->
                <div v-else-if="!currentMandate" class="tw-text-center tw-py-8">
                  <v-icon size="64" color="grey lighten-2" class="tw-mb-4">mdi-account-question</v-icon>
                  <h3 class="tw-text-lg tw-font-semibold tw-text-gray-700 tw-mb-2">
                    No Representative Assigned
                  </h3>
                  <p class="tw-text-gray-500 tw-mb-4">
                    You haven't designated an Authorised Representative yet.
                    Search and invite one from the list.
                  </p>
                  <v-btn
                    color="primary"
                    @click="showInviteDialog = true"
                  >
                    <v-icon left>mdi-account-plus</v-icon>
                    Find & Invite AR
                  </v-btn>
                </div>

                <!-- Current AR Details -->
                <div v-else>
                  <div class="tw-flex tw-items-center tw-justify-between tw-mb-6">
                    <v-chip
                      :color="getMandateStatusColor(currentMandate.status)"
                      dark
                      class="tw-px-4"
                    >
                      <v-icon left small>{{ getMandateStatusIcon(currentMandate.status) }}</v-icon>
                      {{ currentMandate.status }}
                    </v-chip>
                    <v-chip
                      :color="getScopeColor(currentMandate.scope)"
                      outlined
                      small
                    >
                      {{ getScopeLabel(currentMandate.scope) }}
                    </v-chip>
                  </div>

                  <div class="tw-space-y-4">
                    <div class="tw-p-4 tw-bg-gray-50 tw-rounded-lg">
                      <div class="tw-text-xs tw-text-gray-500 tw-uppercase tw-tracking-wide tw-mb-1">Representative</div>
                      <div class="tw-text-lg tw-font-semibold">{{ currentMandate.representativeName }}</div>
                      <div class="tw-text-sm tw-text-gray-600">{{ currentMandate.representativeCompany }}</div>
                    </div>

                    <div class="tw-p-4 tw-bg-blue-50 tw-rounded-lg">
                      <div class="tw-text-xs tw-text-blue-600 tw-uppercase tw-tracking-wide tw-mb-1">EORI Number</div>
                      <div class="tw-text-lg tw-font-mono tw-font-semibold tw-text-blue-800">
                        {{ currentMandate.representativeEori }}
                      </div>
                      <div class="tw-text-xs tw-text-blue-600 tw-mt-1">
                        <v-icon x-small color="blue">mdi-map-marker</v-icon>
                        {{ getEoriCountry(currentMandate.representativeEori) }}
                      </div>
                    </div>

                    <div class="tw-grid tw-grid-cols-2 tw-gap-4">
                      <div class="tw-p-3 tw-bg-gray-50 tw-rounded-lg">
                        <div class="tw-text-xs tw-text-gray-500">Valid From</div>
                        <div class="tw-font-semibold">{{ formatDate(currentMandate.validFrom) }}</div>
                      </div>
                      <div class="tw-p-3 tw-bg-gray-50 tw-rounded-lg">
                        <div class="tw-text-xs tw-text-gray-500">Valid To</div>
                        <div class="tw-font-semibold">
                          {{ currentMandate.validTo ? formatDate(currentMandate.validTo) : 'Indefinite' }}
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Actions -->
                  <div class="tw-mt-6 tw-pt-4 tw-border-t tw-flex tw-gap-2">
                    <v-btn
                      v-if="currentMandate.status === 'ACTIVE'"
                      color="error"
                      outlined
                      small
                      @click="showRevokeDialog = true"
                    >
                      <v-icon left small>mdi-account-remove</v-icon>
                      Revoke Mandate
                    </v-btn>
                    <v-btn
                      color="primary"
                      outlined
                      small
                      @click="showInviteDialog = true"
                    >
                      <v-icon left small>mdi-swap-horizontal</v-icon>
                      Change AR
                    </v-btn>
                  </div>
                </div>
              </v-card-text>
            </v-card>

            <!-- Mandate History -->
            <v-card class="tw-rounded-xl tw-mt-6" elevation="2">
              <v-card-title class="tw-text-lg">
                <v-icon left color="grey">mdi-history</v-icon>
                Mandate History
              </v-card-title>
              <v-card-text>
                <v-list v-if="mandateHistory.length > 0" dense>
                  <v-list-item
                    v-for="mandate in mandateHistory"
                    :key="mandate.id"
                    class="tw-rounded-lg tw-mb-2"
                  >
                    <v-list-item-avatar size="36">
                      <v-icon :color="getMandateStatusColor(mandate.status)">
                        {{ getMandateStatusIcon(mandate.status) }}
                      </v-icon>
                    </v-list-item-avatar>
                    <v-list-item-content>
                      <v-list-item-title>{{ mandate.representativeName }}</v-list-item-title>
                      <v-list-item-subtitle>
                        {{ formatDate(mandate.validFrom) }} — {{ mandate.validTo ? formatDate(mandate.validTo) : 'Present' }}
                      </v-list-item-subtitle>
                    </v-list-item-content>
                    <v-list-item-action>
                      <v-chip x-small :color="getMandateStatusColor(mandate.status)" dark>
                        {{ mandate.status }}
                      </v-chip>
                    </v-list-item-action>
                  </v-list-item>
                </v-list>
                <div v-else class="tw-text-center tw-py-4 tw-text-gray-500">
                  No previous mandates
                </div>
              </v-card-text>
            </v-card>
          </v-col>

          <!-- Right Column: Available ARs -->
          <v-col cols="12" md="7">
            <v-card class="tw-rounded-xl" elevation="2">
              <v-card-title class="tw-bg-gray-100 tw-py-4">
                <v-icon left color="primary">mdi-account-search</v-icon>
                Available Authorised Representatives
                <v-spacer />
                <v-btn
                  color="primary"
                  small
                  @click="loadAvailableARs"
                  :loading="loadingARs"
                >
                  <v-icon left small>mdi-refresh</v-icon>
                  Refresh
                </v-btn>
              </v-card-title>

              <v-card-text class="tw-py-4">
                <!-- Search & Filters -->
                <div class="tw-flex tw-gap-4 tw-mb-4">
                  <v-text-field
                    v-model="searchQuery"
                    placeholder="Search by name, company, or EORI..."
                    prepend-inner-icon="mdi-magnify"
                    outlined
                    dense
                    hide-details
                    class="tw-flex-grow"
                    @input="filterARs"
                  />
                  <v-select
                    v-model="filterCountry"
                    :items="euCountries"
                    item-text="name"
                    item-value="code"
                    label="EU Country"
                    outlined
                    dense
                    hide-details
                    clearable
                    style="max-width: 200px;"
                    @change="filterARs"
                  />
                </div>

                <!-- Loading -->
                <div v-if="loadingARs" class="tw-flex tw-justify-center tw-py-8">
                  <v-progress-circular indeterminate color="primary" />
                </div>

                <!-- AR List -->
                <div v-else-if="filteredARs.length > 0" class="tw-space-y-3">
                  <v-card
                    v-for="ar in filteredARs"
                    :key="ar.id"
                    outlined
                    class="tw-rounded-lg hover:tw-shadow-md tw-transition-shadow"
                  >
                    <v-card-text class="tw-py-3">
                      <div class="tw-flex tw-items-center tw-justify-between">
                        <div class="tw-flex tw-items-center tw-gap-4">
                          <v-avatar color="indigo" size="48">
                            <span class="tw-text-white tw-font-bold">
                              {{ getInitials(ar.fullName) }}
                            </span>
                          </v-avatar>
                          <div>
                            <div class="tw-font-semibold tw-text-gray-900">{{ ar.fullName }}</div>
                            <div class="tw-text-sm tw-text-gray-600">{{ ar.companyName }}</div>
                            <div class="tw-flex tw-items-center tw-gap-2 tw-mt-1">
                              <v-chip x-small color="blue" dark>
                                {{ ar.eoriNumber }}
                              </v-chip>
                              <v-chip x-small outlined>
                                {{ getEoriCountry(ar.eoriNumber) }}
                              </v-chip>
                              <v-icon
                                v-if="ar.isVerified"
                                small
                                color="success"
                                title="Verified AR"
                              >
                                mdi-check-decagram
                              </v-icon>
                            </div>
                          </div>
                        </div>
                        <v-btn
                          color="primary"
                          small
                          @click="selectARForInvite(ar)"
                        >
                          <v-icon left small>mdi-send</v-icon>
                          Invite
                        </v-btn>
                      </div>
                    </v-card-text>
                  </v-card>
                </div>

                <!-- No Results -->
                <div v-else class="tw-text-center tw-py-8">
                  <v-icon size="48" color="grey lighten-2">mdi-account-search</v-icon>
                  <p class="tw-text-gray-500 tw-mt-2">
                    No Authorised Representatives found matching your criteria.
                  </p>
                </div>
              </v-card-text>
            </v-card>

            <!-- How it Works -->
            <v-card class="tw-rounded-xl tw-mt-6" elevation="1">
              <v-card-title class="tw-text-lg">
                <v-icon left color="amber">mdi-lightbulb-outline</v-icon>
                How the AR Process Works
              </v-card-title>
              <v-card-text>
                <v-stepper vertical flat class="tw-bg-transparent">
                  <v-stepper-step step="1" complete color="primary">
                    <span class="tw-font-semibold">Search & Select</span>
                    <small>Find a verified AR from an EU member state</small>
                  </v-stepper-step>
                  <v-stepper-step step="2" complete color="primary">
                    <span class="tw-font-semibold">Send Invitation</span>
                    <small>Invite them to represent you with specific scope</small>
                  </v-stepper-step>
                  <v-stepper-step step="3" complete color="primary">
                    <span class="tw-font-semibold">AR Accepts Mandate</span>
                    <small>The AR reviews and accepts your invitation</small>
                  </v-stepper-step>
                  <v-stepper-step step="4" complete color="success">
                    <span class="tw-font-semibold">DDS Submission</span>
                    <small>AR can now submit Due Diligence Statements via EU TRACES</small>
                  </v-stepper-step>
                </v-stepper>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>

        <!-- Invite Dialog -->
        <v-dialog v-model="showInviteDialog" max-width="500" persistent>
          <v-card class="tw-rounded-xl">
            <v-card-title class="tw-bg-primary tw-text-white">
              <v-icon left color="white">mdi-account-plus</v-icon>
              Invite Authorised Representative
            </v-card-title>
            <v-card-text class="tw-pt-6">
              <div v-if="selectedAR" class="tw-mb-6 tw-p-4 tw-bg-gray-50 tw-rounded-lg">
                <div class="tw-font-semibold">{{ selectedAR.fullName }}</div>
                <div class="tw-text-sm tw-text-gray-600">{{ selectedAR.companyName }}</div>
                <div class="tw-font-mono tw-text-sm tw-text-blue-600 tw-mt-1">{{ selectedAR.eoriNumber }}</div>
              </div>

              <v-select
                v-model="inviteScope"
                :items="scopeOptions"
                item-text="label"
                item-value="value"
                label="Mandate Scope"
                outlined
                dense
              >
                <template #item="{ item }">
                  <div>
                    <div class="tw-font-medium">{{ item.label }}</div>
                    <div class="tw-text-xs tw-text-gray-500">{{ item.description }}</div>
                  </div>
                </template>
              </v-select>

              <v-menu
                v-model="showDatePicker"
                :close-on-content-click="false"
                transition="scale-transition"
                offset-y
                min-width="auto"
              >
                <template #activator="{ on, attrs }">
                  <v-text-field
                    v-model="inviteValidTo"
                    label="Valid Until (optional)"
                    prepend-inner-icon="mdi-calendar"
                    readonly
                    outlined
                    dense
                    clearable
                    v-bind="attrs"
                    v-on="on"
                  />
                </template>
                <v-date-picker
                  v-model="inviteValidTo"
                  :min="minDate"
                  @input="showDatePicker = false"
                />
              </v-menu>

              <v-textarea
                v-model="inviteMessage"
                label="Personal Message (optional)"
                placeholder="Add a message to the AR explaining your request..."
                outlined
                dense
                rows="3"
              />
            </v-card-text>
            <v-card-actions class="tw-px-6 tw-pb-4">
              <v-spacer />
              <v-btn text @click="closeInviteDialog">Cancel</v-btn>
              <v-btn
                color="primary"
                :loading="sendingInvite"
                :disabled="!selectedAR"
                @click="sendInvitation"
              >
                <v-icon left>mdi-send</v-icon>
                Send Invitation
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>

        <!-- Revoke Dialog -->
        <v-dialog v-model="showRevokeDialog" max-width="450">
          <v-card class="tw-rounded-xl">
            <v-card-title class="tw-bg-error tw-text-white">
              <v-icon left color="white">mdi-alert</v-icon>
              Revoke Mandate
            </v-card-title>
            <v-card-text class="tw-pt-6">
              <p class="tw-mb-4">
                Are you sure you want to revoke the mandate for
                <strong>{{ currentMandate?.representativeName }}</strong>?
              </p>
              <v-alert type="warning" dense text>
                This will immediately terminate their authority to submit DDS on your behalf.
                You will need to designate a new AR before placing goods on the EU market.
              </v-alert>
              <v-textarea
                v-model="revokeReason"
                label="Reason for revocation"
                placeholder="Optional: Explain why you're revoking this mandate..."
                outlined
                dense
                rows="2"
                class="tw-mt-4"
              />
            </v-card-text>
            <v-card-actions class="tw-px-6 tw-pb-4">
              <v-spacer />
              <v-btn text @click="showRevokeDialog = false">Cancel</v-btn>
              <v-btn
                color="error"
                :loading="revoking"
                @click="revokeMandate"
              >
                <v-icon left>mdi-account-remove</v-icon>
                Revoke
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>

        <!-- Snackbar -->
        <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="4000" top right>
          {{ snackbar.message }}
        </v-snackbar>
      </v-container>
    </div>
  </Default>
</template>

<script>
import Default from '@/components/layout/Default.vue';
import authorisedRepresentativeService from '@/services/authorisedRepresentativeService';

export default {
  name: 'AuthorisedRepresentativeManagement',
  components: { Default },

  data() {
    return {
      loading: true,
      loadingARs: false,
      sendingInvite: false,
      revoking: false,

      // Current mandate
      currentMandate: null,
      mandateHistory: [],

      // Available ARs
      availableARs: [],
      filteredARs: [],
      searchQuery: '',
      filterCountry: null,

      // EU Countries for filter
      euCountries: authorisedRepresentativeService.EU_MEMBER_STATES,

      // Invite dialog
      showInviteDialog: false,
      selectedAR: null,
      inviteScope: 'FULL',
      inviteValidTo: null,
      inviteMessage: '',
      showDatePicker: false,
      scopeOptions: [
        { value: 'FULL', label: 'Full Authority', description: 'Submit DDS, manage supply chain, handle compliance' },
        { value: 'DDS_ONLY', label: 'DDS Only', description: 'Can only submit Due Diligence Statements' },
        { value: 'VIEW_ONLY', label: 'View Only', description: 'Can view data but not submit on behalf' },
      ],

      // Revoke dialog
      showRevokeDialog: false,
      revokeReason: '',

      // Snackbar
      snackbar: {
        show: false,
        message: '',
        color: 'success',
      },
    };
  },

  computed: {
    exporterId() {
      return this.$store.state.auth?.user?.id || localStorage.getItem('userId');
    },
    minDate() {
      return new Date().toISOString().substr(0, 10);
    },
  },

  async mounted() {
    await Promise.all([
      this.loadCurrentMandate(),
      this.loadAvailableARs(),
    ]);
  },

  methods: {
    async loadCurrentMandate() {
      this.loading = true;
      try {
        const response = await authorisedRepresentativeService.getExporterMandates(this.exporterId);
        const mandates = response.data || response || [];

        // Find active mandate
        this.currentMandate = mandates.find((m) => m.status === 'ACTIVE' || m.status === 'PENDING');
        this.mandateHistory = mandates.filter((m) => m.status !== 'ACTIVE' && m.status !== 'PENDING');
      } catch (error) {
        this.$toast.error('Failed to load mandate:', error.message);
      } finally {
        this.loading = false;
      }
    },

    async loadAvailableARs() {
      this.loadingARs = true;
      try {
        const response = await authorisedRepresentativeService.getAvailableARs();
        this.availableARs = response.data || response || [];
        this.filterARs();
      } catch (error) {
        this.$toast.error('Failed to load ARs:', error.message);
        this.availableARs = [];
        this.filteredARs = [];
      } finally {
        this.loadingARs = false;
      }
    },

    filterARs() {
      let result = [...this.availableARs];

      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        result = result.filter((ar) => ar.fullName?.toLowerCase().includes(query)
            || ar.companyName?.toLowerCase().includes(query)
            || ar.eoriNumber?.toLowerCase().includes(query));
      }

      if (this.filterCountry) {
        result = result.filter((ar) => ar.eoriNumber?.startsWith(this.filterCountry));
      }

      this.filteredARs = result;
    },

    selectARForInvite(ar) {
      this.selectedAR = ar;
      this.showInviteDialog = true;
    },

    closeInviteDialog() {
      this.showInviteDialog = false;
      this.selectedAR = null;
      this.inviteScope = 'FULL';
      this.inviteValidTo = null;
      this.inviteMessage = '';
    },

    async sendInvitation() {
      if (!this.selectedAR) return;

      this.sendingInvite = true;
      try {
        await authorisedRepresentativeService.inviteAR(this.exporterId, {
          representativeId: this.selectedAR.id,
          scope: this.inviteScope,
          validTo: this.inviteValidTo,
          message: this.inviteMessage,
        });

        this.showSnackbar('Invitation sent successfully! The AR will be notified.', 'success');
        this.closeInviteDialog();
        await this.loadCurrentMandate();
      } catch (error) {
        this.$toast.error('Failed to send invitation:', error.message);
      } finally {
        this.sendingInvite = false;
      }
    },

    async revokeMandate() {
      if (!this.currentMandate) return;

      this.revoking = true;
      try {
        await authorisedRepresentativeService.revokeMandate(
          this.currentMandate.id,
          this.revokeReason,
        );

        this.showSnackbar('Mandate revoked successfully', 'success');
        this.showRevokeDialog = false;
        this.revokeReason = '';
        await this.loadCurrentMandate();
      } catch (error) {
        this.$toast.error('Failed to revoke mandate:', error.message);
      } finally {
        this.revoking = false;
      }
    },

    // Helpers
    getMandateStatusColor(status) {
      const colors = {
        ACTIVE: 'success',
        PENDING: 'warning',
        REJECTED: 'error',
        EXPIRED: 'grey',
        REVOKED: 'error',
      };
      return colors[status] || 'grey';
    },

    getMandateStatusIcon(status) {
      const icons = {
        ACTIVE: 'mdi-check-circle',
        PENDING: 'mdi-clock-outline',
        REJECTED: 'mdi-close-circle',
        EXPIRED: 'mdi-calendar-remove',
        REVOKED: 'mdi-cancel',
      };
      return icons[status] || 'mdi-help-circle';
    },

    getScopeColor(scope) {
      const colors = {
        FULL: 'primary',
        DDS_ONLY: 'info',
        VIEW_ONLY: 'grey',
      };
      return colors[scope] || 'grey';
    },

    getScopeLabel(scope) {
      const labels = {
        FULL: 'Full Authority',
        DDS_ONLY: 'DDS Only',
        VIEW_ONLY: 'View Only',
      };
      return labels[scope] || scope;
    },

    getEoriCountry(eori) {
      if (!eori || eori.length < 2) return '';
      const countryCode = eori.substring(0, 2);
      const country = this.euCountries.find((c) => c.code === countryCode);
      return country ? country.name : countryCode;
    },

    getInitials(name) {
      if (!name) return '?';
      return name.split(' ').map((n) => n[0]).join('').substring(0, 2)
        .toUpperCase();
    },

    formatDate(date) {
      if (!date) return '';
      return new Date(date).toLocaleDateString('en-GB', {
        day: 'numeric',
        month: 'short',
        year: 'numeric',
      });
    },

    showSnackbar(message, color = 'success') {
      this.snackbar = { show: true, message, color };
    },
  },
};
</script>

<style scoped>
.ar-management-page {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ee 100%);
}
</style>
