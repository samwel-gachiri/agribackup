<template>
  <v-card class="tw-rounded-lg" :loading="loading">
    <v-card-title class="tw-bg-blue-600 tw-text-white tw-py-3">
      <v-icon left color="white">mdi-account-tie</v-icon>
      Authorised Representative - EUDR Article 6
    </v-card-title>

    <v-card-subtitle class="tw-pt-4 tw-pb-2">
      <v-alert type="info" dense outlined class="tw-mb-0">
        <div class="tw-text-sm">
          <strong>Why do you need an Authorised Representative?</strong><br>
          Non-EU operators must designate an EU-based Authorised Representative to submit
          Due Diligence Statements (DDS) to the EU TRACES system on your behalf.
        </div>
      </v-alert>
    </v-card-subtitle>

    <v-card-text>
      <!-- Current Mandate Status -->
      <div v-if="currentMandate" class="tw-mb-6">
        <div class="tw-text-sm tw-text-gray-600 tw-mb-2">Current Representative</div>
        <v-card outlined class="tw-pa-4">
          <div class="tw-flex tw-items-start tw-justify-between">
            <div>
              <div class="tw-text-lg tw-font-semibold">{{ currentMandate.representativeName }}</div>
              <div class="tw-text-sm tw-text-gray-600">
                <v-icon small class="tw-mr-1">mdi-identifier</v-icon>
                EORI: {{ currentMandate.representativeEori }}
              </div>
              <div class="tw-text-sm tw-text-gray-500 tw-mt-1">
                <v-icon small class="tw-mr-1">mdi-calendar</v-icon>
                Valid: {{ formatDate(currentMandate.validFrom) }}
                <span v-if="currentMandate.validTo">to {{ formatDate(currentMandate.validTo) }}</span>
                <span v-else>(indefinite)</span>
              </div>
            </div>
            <div class="tw-flex tw-flex-col tw-items-end tw-gap-2">
              <v-chip
                :color="getMandateStatusColor(currentMandate.status)"
                dark
                small
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
          </div>

          <!-- Actions for current mandate -->
          <div v-if="currentMandate.status === 'ACTIVE'" class="tw-mt-4 tw-pt-4 tw-border-t">
            <v-btn
              color="error"
              outlined
              small
              @click="showRevokeDialog = true"
            >
              <v-icon left small>mdi-cancel</v-icon>
              Revoke Mandate
            </v-btn>
          </div>

          <div v-if="currentMandate.status === 'PENDING' && currentMandate.initiatedBy === 'REPRESENTATIVE'" class="tw-mt-4 tw-pt-4 tw-border-t">
            <v-btn
              color="success"
              small
              class="tw-mr-2"
              @click="acceptMandate"
            >
              <v-icon left small>mdi-check</v-icon>
              Accept
            </v-btn>
            <v-btn
              color="error"
              outlined
              small
              @click="showRejectDialog = true"
            >
              <v-icon left small>mdi-close</v-icon>
              Reject
            </v-btn>
          </div>
        </v-card>
      </div>

      <!-- No mandate - search and invite -->
      <div v-else>
        <v-alert type="warning" dense text class="tw-mb-4">
          <v-icon left>mdi-alert</v-icon>
          You do not have an Authorised Representative. You cannot submit Due Diligence Statements
          to the EU TRACES system until you designate one.
        </v-alert>

        <!-- Search ARs -->
        <div class="tw-mb-4">
          <v-text-field
            v-model="searchQuery"
            label="Search Authorised Representatives"
            placeholder="Search by company name or EORI..."
            outlined
            dense
            prepend-inner-icon="mdi-magnify"
            clearable
            @input="debouncedSearch"
          />

          <v-select
            v-model="filterMemberState"
            :items="memberStateOptions"
            label="Filter by EU Member State"
            outlined
            dense
            clearable
            prepend-inner-icon="mdi-map-marker"
            class="tw-mt-2"
          />
        </div>

        <!-- Available ARs -->
        <div v-if="availableARs.length > 0">
          <div class="tw-text-sm tw-text-gray-600 tw-mb-2">Available Representatives</div>
          <v-list dense>
            <v-list-item
              v-for="ar in availableARs"
              :key="ar.id"
              class="tw-border tw-rounded tw-mb-2"
              @click="selectAR(ar)"
            >
              <v-list-item-avatar>
                <v-icon :color="ar.isVerified ? 'success' : 'grey'">
                  {{ ar.isVerified ? 'mdi-check-decagram' : 'mdi-account-tie' }}
                </v-icon>
              </v-list-item-avatar>
              <v-list-item-content>
                <v-list-item-title class="tw-font-medium">{{ ar.companyName }}</v-list-item-title>
                <v-list-item-subtitle>
                  <span class="tw-mr-4">
                    <v-icon x-small class="tw-mr-1">mdi-identifier</v-icon>
                    {{ ar.eoriNumber }}
                  </span>
                  <span>
                    <v-icon x-small class="tw-mr-1">mdi-map-marker</v-icon>
                    {{ getMemberStateName(ar.euMemberState) }}
                  </span>
                </v-list-item-subtitle>
              </v-list-item-content>
              <v-list-item-action>
                <v-btn color="primary" small outlined>
                  <v-icon left small>mdi-send</v-icon>
                  Invite
                </v-btn>
              </v-list-item-action>
            </v-list-item>
          </v-list>
        </div>

        <div v-else-if="searchQuery && !loadingSearch" class="tw-text-center tw-py-4">
          <v-icon large color="grey">mdi-account-search</v-icon>
          <div class="tw-text-gray-500 tw-mt-2">No Authorised Representatives found</div>
        </div>
      </div>
    </v-card-text>

    <!-- Invite Dialog -->
    <v-dialog v-model="showInviteDialog" max-width="500" persistent>
      <v-card>
        <v-card-title class="tw-bg-blue-600 tw-text-white">
          <v-icon left color="white">mdi-send</v-icon>
          Invite Authorised Representative
        </v-card-title>
        <v-card-text class="tw-pt-4">
          <div v-if="selectedAR" class="tw-mb-4 tw-p-3 tw-bg-gray-50 tw-rounded">
            <div class="tw-font-semibold">{{ selectedAR.companyName }}</div>
            <div class="tw-text-sm tw-text-gray-600">EORI: {{ selectedAR.eoriNumber }}</div>
          </div>

          <v-form ref="inviteForm" v-model="inviteFormValid">
            <v-menu
              v-model="validFromMenu"
              :close-on-content-click="false"
              transition="scale-transition"
              offset-y
              min-width="290px"
            >
              <template #activator="{ on }">
                <v-text-field
                  v-model="inviteForm.validFrom"
                  label="Mandate Start Date"
                  readonly
                  outlined
                  dense
                  prepend-inner-icon="mdi-calendar"
                  v-on="on"
                />
              </template>
              <v-date-picker v-model="inviteForm.validFrom" @input="validFromMenu = false" />
            </v-menu>

            <v-menu
              v-model="validToMenu"
              :close-on-content-click="false"
              transition="scale-transition"
              offset-y
              min-width="290px"
            >
              <template #activator="{ on }">
                <v-text-field
                  v-model="inviteForm.validTo"
                  label="Mandate End Date (optional)"
                  readonly
                  outlined
                  dense
                  clearable
                  prepend-inner-icon="mdi-calendar"
                  hint="Leave empty for indefinite mandate"
                  persistent-hint
                  v-on="on"
                />
              </template>
              <v-date-picker v-model="inviteForm.validTo" @input="validToMenu = false" />
            </v-menu>

            <v-select
              v-model="inviteForm.scope"
              :items="scopeOptions"
              label="Mandate Scope"
              outlined
              dense
              item-text="label"
              item-value="value"
            >
              <template #item="{ item }">
                <v-list-item-content>
                  <v-list-item-title>{{ item.label }}</v-list-item-title>
                  <v-list-item-subtitle>{{ item.description }}</v-list-item-subtitle>
                </v-list-item-content>
              </template>
            </v-select>

            <v-textarea
              v-model="inviteForm.message"
              label="Message to Representative (optional)"
              outlined
              dense
              rows="3"
              hint="Add any additional information for the representative"
              persistent-hint
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn text @click="closeInviteDialog">Cancel</v-btn>
          <v-btn
            color="primary"
            :loading="submitting"
            :disabled="!inviteFormValid"
            @click="submitInvite"
          >
            <v-icon left>mdi-send</v-icon>
            Send Invitation
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Revoke Dialog -->
    <v-dialog v-model="showRevokeDialog" max-width="400">
      <v-card>
        <v-card-title class="tw-bg-red-600 tw-text-white">
          <v-icon left color="white">mdi-cancel</v-icon>
          Revoke Mandate
        </v-card-title>
        <v-card-text class="tw-pt-4">
          <v-alert type="warning" dense text>
            Are you sure you want to revoke this mandate? You will not be able to submit
            DDS until you designate a new Authorised Representative.
          </v-alert>
          <v-textarea
            v-model="revokeReason"
            label="Reason for revocation (optional)"
            outlined
            dense
            rows="2"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn text @click="showRevokeDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="submitting" @click="revokeMandate">
            Revoke
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Reject Dialog -->
    <v-dialog v-model="showRejectDialog" max-width="400">
      <v-card>
        <v-card-title class="tw-bg-orange-600 tw-text-white">
          <v-icon left color="white">mdi-close</v-icon>
          Reject Offer
        </v-card-title>
        <v-card-text class="tw-pt-4">
          <v-textarea
            v-model="rejectReason"
            label="Reason for rejection (optional)"
            outlined
            dense
            rows="2"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn text @click="showRejectDialog = false">Cancel</v-btn>
          <v-btn color="error" :loading="submitting" @click="rejectMandate">
            Reject
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Snackbar -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="4000">
      {{ snackbar.text }}
      <template #action="{ attrs }">
        <v-btn text v-bind="attrs" @click="snackbar.show = false">Close</v-btn>
      </template>
    </v-snackbar>
  </v-card>
</template>

<script>
import authorisedRepresentativeService from '@/services/authorisedRepresentativeService';
import debounce from 'lodash/debounce';

export default {
  name: 'AuthorisedRepresentativeCard',

  props: {
    exporterId: {
      type: String,
      required: true,
    },
  },

  data() {
    return {
      loading: false,
      loadingSearch: false,
      submitting: false,
      currentMandate: null,
      availableARs: [],
      selectedAR: null,
      searchQuery: '',
      filterMemberState: null,

      // Dialogs
      showInviteDialog: false,
      showRevokeDialog: false,
      showRejectDialog: false,

      // Form
      inviteFormValid: false,
      inviteForm: {
        validFrom: new Date().toISOString().substr(0, 10),
        validTo: null,
        scope: 'FULL',
        message: '',
      },
      validFromMenu: false,
      validToMenu: false,

      revokeReason: '',
      rejectReason: '',

      // Snackbar
      snackbar: {
        show: false,
        text: '',
        color: 'success',
      },

      // Options
      scopeOptions: [
        { value: 'FULL', label: 'Full Authority', description: 'Submit DDS, manage supply chain, handle compliance' },
        { value: 'DDS_ONLY', label: 'DDS Only', description: 'Can only submit Due Diligence Statements' },
        { value: 'VIEW_ONLY', label: 'View Only', description: 'Can view data but not submit on your behalf' },
      ],
    };
  },

  computed: {
    memberStateOptions() {
      return authorisedRepresentativeService.EU_MEMBER_STATES.map((s) => ({
        text: s.name,
        value: s.code,
      }));
    },
  },

  created() {
    this.debouncedSearch = debounce(this.searchARs, 300);
    this.loadCurrentMandate();
    this.loadAvailableARs();
  },

  methods: {
    async loadCurrentMandate() {
      this.loading = true;
      try {
        this.currentMandate = await authorisedRepresentativeService.getActiveMandateForExporter(this.exporterId);
      } catch (error) {
        this.$toast.error('Failed to load mandate:', error.message);
      } finally {
        this.loading = false;
      }
    },

    async loadAvailableARs() {
      this.loadingSearch = true;
      try {
        this.availableARs = await authorisedRepresentativeService.getAvailableARs(this.filterMemberState);
      } catch (error) {
        this.$toast.error('Failed to load ARs:', error.message);
      } finally {
        this.loadingSearch = false;
      }
    },

    async searchARs() {
      if (!this.searchQuery || this.searchQuery.length < 2) {
        this.loadAvailableARs();
        return;
      }

      this.loadingSearch = true;
      try {
        this.availableARs = await authorisedRepresentativeService.searchARs(this.searchQuery);
      } catch (error) {
        this.$toast.error('Search failed:', error.message);
      } finally {
        this.loadingSearch = false;
      }
    },

    selectAR(ar) {
      this.selectedAR = ar;
      this.showInviteDialog = true;
    },

    closeInviteDialog() {
      this.showInviteDialog = false;
      this.selectedAR = null;
      this.inviteForm = {
        validFrom: new Date().toISOString().substr(0, 10),
        validTo: null,
        scope: 'FULL',
        message: '',
      };
    },

    async submitInvite() {
      if (!this.selectedAR) return;

      this.submitting = true;
      try {
        await authorisedRepresentativeService.inviteAR({
          exporterId: this.exporterId,
          arId: this.selectedAR.id,
          validFrom: this.inviteForm.validFrom,
          validTo: this.inviteForm.validTo,
          scope: this.inviteForm.scope,
          message: this.inviteForm.message,
        });

        this.showSnackbar('Invitation sent successfully!', 'success');
        this.closeInviteDialog();
        this.loadCurrentMandate();
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Failed to send invitation', 'error');
      } finally {
        this.submitting = false;
      }
    },

    async acceptMandate() {
      if (!this.currentMandate) return;

      this.submitting = true;
      try {
        await authorisedRepresentativeService.acceptMandate(this.currentMandate.id);
        this.showSnackbar('Mandate accepted!', 'success');
        this.loadCurrentMandate();
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Failed to accept mandate', 'error');
      } finally {
        this.submitting = false;
      }
    },

    async rejectMandate() {
      if (!this.currentMandate) return;

      this.submitting = true;
      try {
        await authorisedRepresentativeService.rejectMandate(this.currentMandate.id, this.rejectReason);
        this.showSnackbar('Offer rejected', 'success');
        this.showRejectDialog = false;
        this.rejectReason = '';
        this.loadCurrentMandate();
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Failed to reject offer', 'error');
      } finally {
        this.submitting = false;
      }
    },

    async revokeMandate() {
      if (!this.currentMandate) return;

      this.submitting = true;
      try {
        await authorisedRepresentativeService.revokeMandate(
          this.currentMandate.id,
          'EXPORTER',
          this.revokeReason,
        );
        this.showSnackbar('Mandate revoked', 'success');
        this.showRevokeDialog = false;
        this.revokeReason = '';
        this.currentMandate = null;
        this.loadAvailableARs();
      } catch (error) {
        this.showSnackbar(error.response?.data?.message || 'Failed to revoke mandate', 'error');
      } finally {
        this.submitting = false;
      }
    },

    formatDate(dateStr) {
      if (!dateStr) return '';
      return new Date(dateStr).toLocaleDateString();
    },

    getMandateStatusColor(status) {
      const info = authorisedRepresentativeService.getMandateStatusInfo(status);
      return info.color;
    },

    getMandateStatusIcon(status) {
      const info = authorisedRepresentativeService.getMandateStatusInfo(status);
      return info.icon;
    },

    getScopeLabel(scope) {
      const info = authorisedRepresentativeService.getScopeInfo(scope);
      return info.label;
    },

    getScopeColor(scope) {
      switch (scope) {
        case 'FULL': return 'success';
        case 'DDS_ONLY': return 'primary';
        case 'VIEW_ONLY': return 'grey';
        default: return 'grey';
      }
    },

    getMemberStateName(code) {
      const state = authorisedRepresentativeService.EU_MEMBER_STATES.find((s) => s.code === code);
      return state ? state.name : code;
    },

    showSnackbar(text, color) {
      this.snackbar = { show: true, text, color };
    },
  },

  watch: {
    filterMemberState() {
      this.loadAvailableARs();
    },
  },
};
</script>

<style scoped>
.v-list-item:hover {
  background-color: #f5f5f5;
}
</style>
