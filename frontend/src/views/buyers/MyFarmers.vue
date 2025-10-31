<template>
  <Default>
    <div class="my-farmers-page">
      <v-container fluid class="py-8">
        <!-- Header Section -->
        <v-row class="mb-6">
          <v-col cols="12">
            <div class="d-flex align-center justify-space-between">
              <div>
                <h1 class="text-h4 font-weight-bold text-primary mb-2">
                  <v-icon color="primary" large class="mr-3">mdi-account-group</v-icon>
                  My Farmers Network
                </h1>
                <p class="text-subtitle-1 text-grey-darken-1">
                  Manage your farmer connections and track their performance
                </p>
              </div>
              <v-btn
                color="primary"
                large
                @click="showOnboardingDialog = true"
                class="font-weight-bold"
              >
                <v-icon left>mdi-account-plus</v-icon>
                Register your own Farmers
              </v-btn>
            </div>
          </v-col>
        </v-row>

        <!-- Quick Stats -->
        <v-row class="mb-8">
          <v-col cols="12" md="3">
            <v-card class="text-center pa-4" elevation="2">
              <v-icon color="success" size="40" class="mb-2">mdi-account-check</v-icon>
              <div class="text-h5 font-weight-bold">{{ activeFarmers }}</div>
              <div class="text-caption text-grey-darken-1">Active Farmers</div>
            </v-card>
          </v-col>
          <v-col cols="12" md="3">
            <v-card class="text-center pa-4" elevation="2">
              <v-icon color="warning" size="40" class="mb-2">mdi-clock-outline</v-icon>
              <div class="text-h5 font-weight-bold">{{ upcomingHarvests }}</div>
              <div class="text-caption text-grey-darken-1">Upcoming Harvests</div>
            </v-card>
          </v-col>
          <v-col cols="12" md="3">
            <v-card class="text-center pa-4" elevation="2">
              <v-icon color="info" size="40" class="mb-2">mdi-cart</v-icon>
              <div class="text-h5 font-weight-bold">{{ totalOrders }}</div>
              <div class="text-caption text-grey-darken-1">Total Orders</div>
            </v-card>
          </v-col>
          <v-col cols="12" md="3">
            <v-card class="text-center pa-4" elevation="2">
              <v-icon color="primary" size="40" class="mb-2">mdi-star</v-icon>
              <div class="text-h5 font-weight-bold">{{ averageRating.toFixed(1) }}</div>
              <div class="text-caption text-grey-darken-1">Avg Rating</div>
            </v-card>
          </v-col>
        </v-row>

        <!-- Farmers List -->
        <v-row>
          <v-col cols="12">
            <v-card elevation="2">
              <v-card-title class="d-flex align-center justify-space-between">
                <span class="text-h6">Your Farmers</span>
                <v-text-field
                  v-model="searchQuery"
                  append-icon="mdi-magnify"
                  label="Search farmers..."
                  single-line
                  hide-details
                  dense
                  outlined
                  class="ml-4"
                  style="max-width: 300px;"
                />
              </v-card-title>
              <v-card-text>
                <v-row v-if="loading" class="justify-center">
                  <v-col cols="12" class="text-center">
                    <v-progress-circular indeterminate color="primary" size="64" />
                    <p class="mt-4 text-grey-darken-1">Loading your farmers...</p>
                  </v-col>
                </v-row>
                <v-row v-else-if="filteredFarmers.length === 0" class="justify-center">
                  <v-col cols="12" class="text-center py-8">
                    <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-account-off</v-icon>
                    <h3 class="text-h6 text-grey-darken-1 mb-2">No farmers found</h3>
                    <p class="text-body-2 text-grey-darken-1 mb-4">
                      {{ searchQuery ? 'No farmers match your search.' : 'Start building your farmer network!' }}
                    </p>
                    <v-btn color="primary" @click="showOnboardingDialog = true">
                      <v-icon left>mdi-account-plus</v-icon>
                      Add Your First Farmer
                    </v-btn>
                  </v-col>
                </v-row>
                <v-row v-else>
                  <v-col
                    v-for="farmer in filteredFarmers"
                    :key="farmer.connectionId"
                    cols="12"
                    md="6"
                    lg="4"
                  >
                    <farmer-card
                      :farmer="farmer"
                      @view-details="viewFarmerDetails"
                      @remove-farmer="removeFarmer"
                      @update-notes="updateFarmerNotes"
                    />
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-container>

      <!-- Farmer Onboarding Dialog -->
      <v-dialog v-model="showOnboardingDialog" max-width="600" persistent>
        <farmer-onboarding
          @close="showOnboardingDialog = false"
          @farmer-added="onFarmerAdded"
        />
      </v-dialog>

      <!-- Farmer Details Dialog -->
      <v-dialog v-model="showFarmerDetails" max-width="800">
        <farmer-details
          v-if="selectedFarmer"
          :farmer="selectedFarmer"
          @close="showFarmerDetails = false"
          @update-notes="updateFarmerNotes"
        />
      </v-dialog>

      <!-- Remove Farmer Confirmation Dialog -->
      <v-dialog v-model="showRemoveConfirmation" max-width="400">
        <v-card>
          <v-card-title class="text-h6">
            <v-icon color="warning" class="mr-2">mdi-alert</v-icon>
            Remove Farmer
          </v-card-title>
          <v-card-text v-if="farmerToRemove">
            Are you sure you want to remove <strong>{{ farmerToRemove.farmer.fullName }}</strong> from your network?
            This action cannot be undone.
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <v-btn text @click="cancelRemoveFarmer">Cancel</v-btn>
            <v-btn color="error" @click="confirmRemoveFarmer">Remove</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </div>
  </Default>
</template>

<script>
import axios from 'axios';
import Default from '@/components/layout/Default.vue';
import FarmerCard from '@/components/buyer/FarmerCard.vue';
import FarmerOnboarding from '@/components/buyer/FarmerOnboarding.vue';
import FarmerDetails from '@/components/buyer/FarmerDetails.vue';
import { getCurrentUserId } from '@/utils/roles.js';

export default {
  name: 'MyFarmers',
  components: {
    Default,
    FarmerCard,
    FarmerOnboarding,
    FarmerDetails,
  },
  data() {
    return {
      loading: true,
      farmers: [],
      searchQuery: '',
      showOnboardingDialog: false,
      showFarmerDetails: false,
      selectedFarmer: null,
      showRemoveConfirmation: false,
      farmerToRemove: null,
    };
  },
  computed: {
    filteredFarmers() {
      if (!this.searchQuery) return this.farmers;
      const query = this.searchQuery.toLowerCase();
      return this.farmers.filter((farmer) => farmer.farmer.fullName.toLowerCase().includes(query)
        || farmer.farmer.location?.customName?.toLowerCase().includes(query)
        || farmer.notes?.toLowerCase().includes(query));
    },
    activeFarmers() {
      return this.farmers.filter((f) => f.status === 'ACTIVE').length;
    },
    upcomingHarvests() {
      return this.farmers.reduce((count, farmer) => count + (farmer.upcomingHarvests?.length || 0), 0);
    },
    totalOrders() {
      return this.farmers.reduce((count, farmer) => count + (farmer.performanceMetrics?.totalOrders || 0), 0);
    },
    averageRating() {
      const ratings = this.farmers
        .map((f) => f.performanceMetrics?.qualityRating)
        .filter((r) => r && r > 0);
      if (ratings.length === 0) return 0;
      return ratings.reduce((sum, rating) => sum + rating, 0) / ratings.length;
    },
  },
  methods: {
    async fetchMyFarmers() {
      this.loading = true;
      try {
        const buyerId = getCurrentUserId();
        const { data } = await axios.get(`/api/buyer/${buyerId}/farmers`);
        this.farmers = data || [];
      } catch (error) {
        this.$toast.error('Failed to load your farmers. Please try again.');
        this.farmers = [];
      } finally {
        this.loading = false;
      }
    },
    viewFarmerDetails(farmer) {
      this.selectedFarmer = farmer;
      this.showFarmerDetails = true;
    },
    async removeFarmer(farmer) {
      this.farmerToRemove = farmer;
      this.showRemoveConfirmation = true;
    },
    cancelRemoveFarmer() {
      this.farmerToRemove = null;
      this.showRemoveConfirmation = false;
    },
    async confirmRemoveFarmer() {
      if (!this.farmerToRemove) return;

      try {
        await axios.delete(`/api/buyer/farmers/${this.farmerToRemove.connectionId}`);
        this.$toast.success(`${this.farmerToRemove.farmer.fullName} removed from your network`);
        this.fetchMyFarmers();
      } catch (error) {
        this.$toast.error('Failed to remove farmer. Please try again.');
      } finally {
        this.farmerToRemove = null;
        this.showRemoveConfirmation = false;
      }
    },
    async updateFarmerNotes(farmer, notes) {
      try {
        await axios.put(`/api/buyer/farmers/${farmer.connectionId}/notes`, { notes });
        this.$toast.success('Notes updated successfully');
        this.fetchMyFarmers();
      } catch (error) {
        this.$toast.error('Failed to update notes. Please try again.');
      }
    },
    onFarmerAdded() {
      this.showOnboardingDialog = false;
      this.fetchMyFarmers();
      this.$toast.success('Farmer added to your network!');
    },
  },
  async created() {
    await this.fetchMyFarmers();
  },
};
</script>

<style scoped>
.my-farmers-page {
  min-height: 100vh;
  background: #f8fafc;
}
</style>
