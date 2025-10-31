<template>
  <v-card class="farmer-onboarding">
    <v-card-title class="primary white--text">
      <v-icon left color="white">mdi-account-plus</v-icon>
      Add Farmer to Your Network
      <v-spacer></v-spacer>
      <v-btn icon dark @click="$emit('close')">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-card-title>

    <v-card-text class="pa-6">
      <v-stepper v-model="currentStep" vertical>
        <!-- Step 1: Search Method -->
        <v-stepper-step :complete="currentStep > 1" step="1">
          Choose Search Method
          <small>How would you like to find the farmer?</small>
        </v-stepper-step>
        <v-stepper-content step="1">
          <v-row>
            <v-col cols="12" md="6">
              <v-card
                class="search-method-card"
                :class="{ 'selected': searchMethod === 'phone' }"
                @click="selectSearchMethod('phone')"
                outlined
              >
                <v-card-text class="text-center pa-4">
                  <v-icon size="48" color="primary" class="mb-2">mdi-phone</v-icon>
                  <div class="text-h6 mb-2">Phone Number</div>
                  <div class="text-caption">Search by farmer's phone number</div>
                </v-card-text>
              </v-card>
            </v-col>
            <v-col cols="12" md="6">
              <v-card
                class="search-method-card"
                :class="{ 'selected': searchMethod === 'name' }"
                @click="selectSearchMethod('name')"
                outlined
              >
                <v-card-text class="text-center pa-4">
                  <v-icon size="48" color="primary" class="mb-2">mdi-account-search</v-icon>
                  <div class="text-h6 mb-2">Name & Location</div>
                  <div class="text-caption">Search by name and location</div>
                </v-card-text>
              </v-card>
            </v-col>
          </v-row>
          <div class="mt-4">
            <v-btn
              color="primary"
              :disabled="!searchMethod"
              @click="currentStep = 2"
            >
              Continue
            </v-btn>
          </div>
        </v-stepper-content>

        <!-- Step 2: Search Farmers -->
        <v-stepper-step :complete="currentStep > 2" step="2">
          Search for Farmer
          <small>Find the farmer you want to connect with</small>
        </v-stepper-step>
        <v-stepper-content step="2">
          <!-- Phone Search -->
          <div v-if="searchMethod === 'phone'">
            <v-text-field
              v-model="searchPhone"
              label="Farmer's Phone Number"
              prepend-icon="mdi-phone"
              outlined
              dense
              @input="searchFarmers"
              :loading="searching"
            ></v-text-field>
          </div>

          <!-- Name & Location Search -->
          <div v-else-if="searchMethod === 'name'">
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="searchName"
                  label="Farmer's Name"
                  prepend-icon="mdi-account"
                  outlined
                  dense
                  @input="searchFarmers"
                ></v-text-field>
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="searchLocation"
                  label="Location (Optional)"
                  prepend-icon="mdi-map-marker"
                  outlined
                  dense
                  @input="searchFarmers"
                ></v-text-field>
              </v-col>
            </v-row>
          </div>

          <!-- Search Results -->
          <div v-if="searchResults.length > 0" class="mt-4">
            <div class="text-subtitle-2 mb-3">Search Results:</div>
            <v-list>
              <v-list-item
                v-for="farmer in searchResults"
                :key="farmer.id"
                @click="selectFarmer(farmer)"
                :class="{ 'selected-farmer': selectedFarmer?.id === farmer.id }"
              >
                <v-list-item-avatar>
                  <v-img
                    :src="farmer.avatar || '/default-avatar.png'"
                    :alt="farmer.fullName"
                  ></v-img>
                </v-list-item-avatar>
                <v-list-item-content>
                  <v-list-item-title>{{ farmer.fullName }}</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-icon small class="mr-1">mdi-phone</v-icon>
                    {{ farmer.phoneNumber }}
                    <span v-if="farmer.location?.customName" class="ml-2">
                      <v-icon small class="mr-1">mdi-map-marker</v-icon>
                      {{ farmer.location.customName }}
                    </span>
                  </v-list-item-subtitle>
                </v-list-item-content>
                <v-list-item-action>
                  <v-icon v-if="selectedFarmer?.id === farmer.id" color="primary">
                    mdi-check-circle
                  </v-icon>
                </v-list-item-action>
              </v-list-item>
            </v-list>
          </div>

          <!-- No Results -->
          <div v-else-if="hasSearched && !searching" class="text-center pa-4">
            <v-icon size="64" color="grey lighten-2">mdi-account-off</v-icon>
            <div class="text-h6 text--secondary mt-2">No farmers found</div>
            <div class="text-body-2 text--secondary">
              Try adjusting your search criteria or invite the farmer to join AgriBackup
            </div>
            <v-btn color="primary" class="mt-3" @click="showInviteDialog = true">
              <v-icon left>mdi-email-send</v-icon>
              Send Invitation
            </v-btn>
          </div>

          <div class="mt-4">
            <v-btn text @click="currentStep = 1" class="mr-2">Back</v-btn>
            <v-btn
              color="primary"
              :disabled="!selectedFarmer"
              @click="currentStep = 3"
            >
              Continue
            </v-btn>
          </div>
        </v-stepper-content>

        <!-- Step 3: Connection Details -->
        <v-stepper-step :complete="currentStep > 3" step="3">
          Connection Details
          <small>Add notes and finalize connection</small>
        </v-stepper-step>
        <v-stepper-content step="3">
          <div v-if="selectedFarmer">
            <!-- Farmer Summary -->
            <v-card outlined class="mb-4">
              <v-card-text>
                <div class="d-flex align-center">
                  <v-avatar size="64" class="mr-4">
                    <v-img
                      :src="selectedFarmer.avatar || '/default-avatar.png'"
                      :alt="selectedFarmer.fullName"
                    ></v-img>
                  </v-avatar>
                  <div>
                    <div class="text-h6">{{ selectedFarmer.fullName }}</div>
                    <div class="text-body-2 text--secondary">
                      <v-icon small class="mr-1">mdi-phone</v-icon>
                      {{ selectedFarmer.phoneNumber }}
                    </div>
                    <div v-if="selectedFarmer.location?.customName" class="text-body-2 text--secondary">
                      <v-icon small class="mr-1">mdi-map-marker</v-icon>
                      {{ selectedFarmer.location.customName }}
                    </div>
                  </div>
                </div>
              </v-card-text>
            </v-card>

            <!-- Connection Notes -->
            <v-textarea
              v-model="connectionNotes"
              label="Notes (Optional)"
              rows="3"
              outlined
              hint="Add any notes about this farmer or your business relationship"
              persistent-hint
            ></v-textarea>

            <!-- Connection Preferences -->
            <v-row class="mt-4">
              <v-col cols="12" md="6">
                <v-select
                  v-model="notificationPreference"
                  :items="notificationOptions"
                  label="Notification Preference"
                  outlined
                  dense
                ></v-select>
              </v-col>
              <v-col cols="12" md="6">
                <v-select
                  v-model="priorityLevel"
                  :items="priorityOptions"
                  label="Priority Level"
                  outlined
                  dense
                ></v-select>
              </v-col>
            </v-row>
          </div>

          <div class="mt-4">
            <v-btn text @click="currentStep = 2" class="mr-2">Back</v-btn>
            <v-btn
              color="primary"
              :loading="connecting"
              @click="connectToFarmer"
            >
              <v-icon left>mdi-account-plus</v-icon>
              Connect to Farmer
            </v-btn>
          </div>
        </v-stepper-content>
      </v-stepper>
    </v-card-text>

    <!-- Invite Dialog -->
    <v-dialog v-model="showInviteDialog" max-width="500">
      <v-card>
        <v-card-title>
          <span class="text-h6">Invite Farmer to AgriBackup</span>
        </v-card-title>
        <v-card-text>
          <v-text-field
            v-model="invitePhone"
            label="Farmer's Phone Number"
            prepend-icon="mdi-phone"
            outlined
            dense
          ></v-text-field>
          <v-text-field
            v-model="inviteName"
            label="Farmer's Name"
            prepend-icon="mdi-account"
            outlined
            dense
          ></v-text-field>
          <v-textarea
            v-model="inviteMessage"
            label="Personal Message (Optional)"
            rows="3"
            outlined
            hint="Add a personal message to encourage them to join"
            persistent-hint
          ></v-textarea>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="showInviteDialog = false">Cancel</v-btn>
          <v-btn color="primary" :loading="sendingInvite" @click="sendInvitation">
            <v-icon left>mdi-email-send</v-icon>
            Send Invitation
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script>
import axios from 'axios';
import { getCurrentUserId } from '@/utils/roles.js';

export default {
  name: 'FarmerOnboarding',
  data() {
    return {
      currentStep: 1,
      searchMethod: null,
      searchPhone: '',
      searchName: '',
      searchLocation: '',
      searchResults: [],
      selectedFarmer: null,
      connectionNotes: '',
      notificationPreference: 'all',
      priorityLevel: 'normal',
      searching: false,
      connecting: false,
      hasSearched: false,
      showInviteDialog: false,
      invitePhone: '',
      inviteName: '',
      inviteMessage: '',
      sendingInvite: false,
      notificationOptions: [
        { text: 'All Notifications', value: 'all' },
        { text: 'Important Only', value: 'important' },
        { text: 'None', value: 'none' },
      ],
      priorityOptions: [
        { text: 'High Priority', value: 'high' },
        { text: 'Normal Priority', value: 'normal' },
        { text: 'Low Priority', value: 'low' },
      ],
    };
  },
  methods: {
    selectSearchMethod(method) {
      this.searchMethod = method;
      this.resetSearch();
    },

    resetSearch() {
      this.searchPhone = '';
      this.searchName = '';
      this.searchLocation = '';
      this.searchResults = [];
      this.selectedFarmer = null;
      this.hasSearched = false;
    },

    async searchFarmers() {
      if (this.searchMethod === 'phone' && this.searchPhone.length < 3) return;
      if (this.searchMethod === 'name' && this.searchName.length < 2) return;

      this.searching = true;
      this.hasSearched = true;

      try {
        const params = {};
        if (this.searchMethod === 'phone') {
          params.phone = this.searchPhone;
        } else {
          params.name = this.searchName;
          if (this.searchLocation) params.location = this.searchLocation;
        }

        const response = await axios.get('/api/farmers/search', { params });
        // Map API response to expected frontend format
        this.searchResults = (response.data || []).map((farmer) => ({
          id: farmer.farmerId,
          fullName: `${farmer.firstName} ${farmer.lastName}`.trim(),
          phoneNumber: farmer.phoneNumber,
          email: farmer.email,
          location: {
            customName: farmer.location,
          },
          avatar: null, // API doesn't provide avatar
          totalProduces: farmer.totalProduces,
          upcomingHarvests: farmer.upcomingHarvests,
          produceTypes: farmer.produceTypes,
        }));
      } catch (error) {
        this.$toast.error('Failed to search farmers. Please try again.');
        this.searchResults = [];
      } finally {
        this.searching = false;
      }
    },

    selectFarmer(farmer) {
      this.selectedFarmer = farmer;
    },

    async connectToFarmer() {
      if (!this.selectedFarmer) return;

      this.connecting = true;
      try {
        const buyerId = getCurrentUserId();
        const payload = {
          farmerId: this.selectedFarmer.id,
          notes: this.connectionNotes,
          notificationPreference: this.notificationPreference,
          priorityLevel: this.priorityLevel,
        };

        await axios.post(`/api/buyer/${buyerId}/farmers`, payload);
        this.$toast.success(`Successfully connected to ${this.selectedFarmer.fullName}!`);
        this.$emit('farmer-added');
      } catch (error) {
        const message = error.response?.data?.message || 'Failed to connect to farmer. Please try again.';
        this.$toast.error(message);
      } finally {
        this.connecting = false;
      }
    },

    async sendInvitation() {
      if (!this.invitePhone || !this.inviteName) {
        this.$toast.error('Please fill in the required fields');
        return;
      }

      this.sendingInvite = true;
      try {
        const buyerId = getCurrentUserId();
        const payload = {
          phoneNumber: this.invitePhone,
          name: this.inviteName,
          message: this.inviteMessage,
        };

        await axios.post(`/api/buyer/${buyerId}/invite-farmer`, payload);
        this.$toast.success('Invitation sent successfully!');
        this.showInviteDialog = false;
        this.resetInviteForm();
      } catch (error) {
        this.$toast.error('Failed to send invitation. Please try again.');
      } finally {
        this.sendingInvite = false;
      }
    },

    resetInviteForm() {
      this.invitePhone = '';
      this.inviteName = '';
      this.inviteMessage = '';
    },
  },
};
</script>

<style scoped>
.farmer-onboarding {
  border-radius: 12px;
}

.search-method-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
}

.search-method-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.search-method-card.selected {
  border-color: #1976d2;
  background-color: rgba(25, 118, 210, 0.05);
}

.selected-farmer {
  background-color: rgba(25, 118, 210, 0.05);
}

.v-stepper {
  box-shadow: none;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .v-card-text {
    padding: 1rem !important;
  }

  .search-method-card {
    margin-bottom: 1rem;
  }
}

/* Touch-friendly interfaces */
@media (hover: none) and (pointer: coarse) {
  .search-method-card:hover {
    transform: none;
  }

  .v-btn {
    min-height: 44px;
  }
}
</style>
