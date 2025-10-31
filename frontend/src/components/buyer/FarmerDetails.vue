<template>
  <v-card class="farmer-details">
    <v-card-title class="primary white--text">
      <v-avatar size="48" class="mr-3">
        <v-img
          :src="farmer.farmer.avatar || '/default-avatar.png'"
          :alt="farmer.farmer.fullName"
        ></v-img>
      </v-avatar>
      <div>
        <div class="text-h6">{{ farmer.farmer.fullName }}</div>
        <div class="text-subtitle-2">Farmer Details</div>
      </div>
      <v-spacer></v-spacer>
      <v-btn icon dark @click="$emit('close')">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </v-card-title>

    <v-card-text class="pa-0">
      <v-tabs v-model="activeTab" background-color="grey lighten-4">
        <v-tab>
          <v-icon left>mdi-account</v-icon>
          Profile
        </v-tab>
        <v-tab>
          <v-icon left>mdi-chart-line</v-icon>
          Performance
        </v-tab>
        <v-tab>
          <v-icon left>mdi-sprout</v-icon>
          Crops
        </v-tab>
        <v-tab>
          <v-icon left>mdi-cart</v-icon>
          Orders
        </v-tab>
      </v-tabs>

      <v-tabs-items v-model="activeTab">
        <!-- Profile Tab -->
        <v-tab-item>
          <v-container class="pa-6">
            <v-row>
              <!-- Basic Information -->
              <v-col cols="12" md="6">
                <v-card outlined>
                  <v-card-title class="text-h6">Basic Information</v-card-title>
                  <v-card-text>
                    <div class="info-item">
                      <v-icon class="mr-2" color="primary">mdi-account</v-icon>
                      <span class="font-weight-medium">Full Name:</span>
                      <span class="ml-2">{{ farmer.farmer.fullName }}</span>
                    </div>
                    <div class="info-item">
                      <v-icon class="mr-2" color="primary">mdi-phone</v-icon>
                      <span class="font-weight-medium">Phone:</span>
                      <span class="ml-2">{{ farmer.farmer.phoneNumber }}</span>
                    </div>
                    <div class="info-item">
                      <v-icon class="mr-2" color="primary">mdi-email</v-icon>
                      <span class="font-weight-medium">Email:</span>
                      <span class="ml-2">{{ farmer.farmer.email || 'Not provided' }}</span>
                    </div>
                    <div class="info-item">
                      <v-icon class="mr-2" color="primary">mdi-map-marker</v-icon>
                      <span class="font-weight-medium">Location:</span>
                      <span class="ml-2">{{ farmer.farmer.location?.customName || 'Not set' }}</span>
                    </div>
                    <div class="info-item">
                      <v-icon class="mr-2" color="primary">mdi-calendar-plus</v-icon>
                      <span class="font-weight-medium">Member Since:</span>
                      <span class="ml-2">{{ formatDate(farmer.farmer.createdAt) }}</span>
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>

              <!-- Connection Information -->
              <v-col cols="12" md="6">
                <v-card outlined>
                  <v-card-title class="text-h6">Connection Details</v-card-title>
                  <v-card-text>
                    <div class="info-item">
                      <v-icon class="mr-2" color="success">mdi-calendar-check</v-icon>
                      <span class="font-weight-medium">Connected:</span>
                      <span class="ml-2">{{ formatDate(farmer.connectionDate) }}</span>
                    </div>
                    <div class="info-item">
                      <v-icon class="mr-2" :color="getStatusColor(farmer.status)">
                        {{ getStatusIcon(farmer.status) }}
                      </v-icon>
                      <span class="font-weight-medium">Status:</span>
                      <v-chip
                        :color="getStatusColor(farmer.status)"
                        text-color="white"
                        small
                        class="ml-2"
                      >
                        {{ farmer.status }}
                      </v-chip>
                    </div>
                    <div class="info-item">
                      <v-icon class="mr-2" color="info">mdi-bell</v-icon>
                      <span class="font-weight-medium">Notifications:</span>
                      <span class="ml-2">{{ farmer.notificationPreference || 'All' }}</span>
                    </div>
                    <div class="info-item">
                      <v-icon class="mr-2" color="warning">mdi-flag</v-icon>
                      <span class="font-weight-medium">Priority:</span>
                      <span class="ml-2">{{ farmer.priorityLevel || 'Normal' }}</span>
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>

              <!-- Notes Section -->
              <v-col cols="12">
                <v-card outlined>
                  <v-card-title class="text-h6">
                    Notes
                    <v-spacer></v-spacer>
                    <v-btn small color="primary" @click="editNotes">
                      <v-icon left small>mdi-pencil</v-icon>
                      Edit
                    </v-btn>
                  </v-card-title>
                  <v-card-text>
                    <div v-if="farmer.notes" class="notes-content">
                      {{ farmer.notes }}
                    </div>
                    <div v-else class="text--secondary font-italic">
                      No notes added yet. Click Edit to add notes about this farmer.
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </v-container>
        </v-tab-item>

        <!-- Performance Tab -->
        <v-tab-item>
          <v-container class="pa-6">
            <v-row>
              <!-- Performance Metrics -->
              <v-col cols="12" md="6">
                <v-card outlined>
                  <v-card-title class="text-h6">Performance Metrics</v-card-title>
                  <v-card-text>
                    <div class="metric-item">
                      <div class="d-flex align-center justify-space-between">
                        <span>Quality Rating</span>
                        <div class="d-flex align-center">
                          <v-rating
                            :value="farmer.performanceMetrics?.qualityRating || 0"
                            color="amber"
                            dense
                            half-increments
                            readonly
                            size="20"
                          ></v-rating>
                          <span class="ml-2">{{ (farmer.performanceMetrics?.qualityRating || 0).toFixed(1) }}/5</span>
                        </div>
                      </div>
                    </div>
                    <v-divider class="my-3"></v-divider>
                    <div class="metric-item">
                      <div class="d-flex align-center justify-space-between">
                        <span>Reliability Score</span>
                        <div class="d-flex align-center">
                          <v-progress-linear
                            :value="farmer.performanceMetrics?.reliabilityScore || 0"
                            color="success"
                            height="8"
                            rounded
                            class="mr-2"
                            style="width: 100px;"
                          ></v-progress-linear>
                          <span>{{ farmer.performanceMetrics?.reliabilityScore || 0 }}%</span>
                        </div>
                      </div>
                    </div>
                    <v-divider class="my-3"></v-divider>
                    <div class="metric-item">
                      <div class="d-flex align-center justify-space-between">
                        <span>Total Orders</span>
                        <v-chip color="primary" text-color="white" small>
                          {{ farmer.performanceMetrics?.totalOrders || 0 }}
                        </v-chip>
                      </div>
                    </div>
                    <v-divider class="my-3"></v-divider>
                    <div class="metric-item">
                      <div class="d-flex align-center justify-space-between">
                        <span>On-Time Deliveries</span>
                        <span>{{ farmer.performanceMetrics?.onTimeDeliveries || 0 }}%</span>
                      </div>
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>

              <!-- Recent Activity -->
              <v-col cols="12" md="6">
                <v-card outlined>
                  <v-card-title class="text-h6">Recent Activity</v-card-title>
                  <v-card-text>
                    <v-timeline dense>
                      <v-timeline-item
                        v-for="activity in recentActivities"
                        :key="activity.id"
                        :color="activity.color"
                        small
                      >
                        <template v-slot:icon>
                          <v-icon color="white" small>{{ activity.icon }}</v-icon>
                        </template>
                        <div class="text-body-2">{{ activity.title }}</div>
                        <div class="text-caption text--secondary">{{ formatDateTime(activity.date) }}</div>
                      </v-timeline-item>
                    </v-timeline>
                    <div v-if="recentActivities.length === 0" class="text-center text--secondary">
                      No recent activity
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </v-container>
        </v-tab-item>

        <!-- Crops Tab -->
        <v-tab-item>
          <v-container class="pa-6">
            <v-row>
              <!-- Current Crops -->
              <v-col cols="12" md="6">
                <v-card outlined>
                  <v-card-title class="text-h6">Current Crops</v-card-title>
                  <v-card-text>
                    <div v-if="currentCrops.length > 0">
                      <v-chip
                        v-for="crop in currentCrops"
                        :key="crop.id"
                        class="ma-1"
                        color="success"
                        outlined
                      >
                        <v-icon left small>mdi-sprout</v-icon>
                        {{ crop.name }}
                      </v-chip>
                    </div>
                    <div v-else class="text--secondary">
                      No current crops information available
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>

              <!-- Upcoming Harvests -->
              <v-col cols="12" md="6">
                <v-card outlined>
                  <v-card-title class="text-h6">Upcoming Harvests</v-card-title>
                  <v-card-text>
                    <div v-if="farmer.upcomingHarvests && farmer.upcomingHarvests.length > 0">
                      <v-list dense>
                        <v-list-item
                          v-for="harvest in farmer.upcomingHarvests"
                          :key="harvest.id"
                        >
                          <v-list-item-icon>
                            <v-icon color="warning">mdi-calendar-clock</v-icon>
                          </v-list-item-icon>
                          <v-list-item-content>
                            <v-list-item-title>{{ harvest.produceName }}</v-list-item-title>
                            <v-list-item-subtitle>
                              Expected: {{ formatDate(harvest.expectedDate) }}
                            </v-list-item-subtitle>
                          </v-list-item-content>
                        </v-list-item>
                      </v-list>
                    </div>
                    <div v-else class="text--secondary">
                      No upcoming harvests scheduled
                    </div>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </v-container>
        </v-tab-item>

        <!-- Orders Tab -->
        <v-tab-item>
          <v-container class="pa-6">
            <v-data-table
              :headers="orderHeaders"
              :items="orderHistory"
              :loading="loadingOrders"
              class="elevation-1"
              :items-per-page="10"
            >
              <template v-slot:item.orderDate="{ item }">
                {{ formatDate(item.orderDate) }}
              </template>
              <template v-slot:item.status="{ item }">
                <v-chip
                  :color="getOrderStatusColor(item.status)"
                  text-color="white"
                  small
                >
                  {{ item.status }}
                </v-chip>
              </template>
              <template v-slot:item.totalAmount="{ item }">
                ${{ item.totalAmount.toFixed(2) }}
              </template>
              <template v-slot:no-data>
                <div class="text-center pa-4">
                  <v-icon size="64" color="grey lighten-2">mdi-cart-off</v-icon>
                  <div class="text-h6 text--secondary mt-2">No orders yet</div>
                  <div class="text-body-2 text--secondary">
                    Start ordering from this farmer to see order history
                  </div>
                </div>
              </template>
            </v-data-table>
          </v-container>
        </v-tab-item>
      </v-tabs-items>
    </v-card-text>

    <!-- Notes Edit Dialog -->
    <v-dialog v-model="showNotesDialog" max-width="500">
      <v-card>
        <v-card-title>
          <span class="text-h6">Edit Notes for {{ farmer.farmer.fullName }}</span>
        </v-card-title>
        <v-card-text>
          <v-textarea
            v-model="editedNotes"
            label="Notes"
            rows="4"
            outlined
            hint="Add any notes about this farmer (quality, preferences, etc.)"
            persistent-hint
          ></v-textarea>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn text @click="showNotesDialog = false">Cancel</v-btn>
          <v-btn color="primary" @click="saveNotes">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<script>
import axios from 'axios';

export default {
  name: 'FarmerDetails',
  props: {
    farmer: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      activeTab: 0,
      showNotesDialog: false,
      editedNotes: '',
      loadingOrders: false,
      orderHistory: [],
      recentActivities: [],
      currentCrops: [],
      orderHeaders: [
        { text: 'Order ID', value: 'id', sortable: true },
        { text: 'Date', value: 'orderDate', sortable: true },
        { text: 'Products', value: 'productCount', sortable: true },
        { text: 'Total Amount', value: 'totalAmount', sortable: true },
        { text: 'Status', value: 'status', sortable: true },
      ],
    };
  },
  async mounted() {
    await this.loadFarmerDetails();
  },
  methods: {
    async loadFarmerDetails() {
      try {
        // Load order history
        this.loadingOrders = true;
        const ordersResponse = await axios.get(`/api/buyer/farmers/${this.farmer.connectionId}/orders`);
        this.orderHistory = ordersResponse.data.data || [];

        // Load recent activities
        const activitiesResponse = await axios.get(`/api/buyer/farmers/${this.farmer.connectionId}/activities`);
        this.recentActivities = activitiesResponse.data.data || [];

        // Load current crops
        const cropsResponse = await axios.get(`/api/farmers/${this.farmer.farmer.id}/current-crops`);
        this.currentCrops = cropsResponse.data.data || [];
      } catch (error) {
        this.$toast.error('Failed to load farmer details');
      } finally {
        this.loadingOrders = false;
      }
    },

    editNotes() {
      this.editedNotes = this.farmer.notes || '';
      this.showNotesDialog = true;
    },

    saveNotes() {
      this.$emit('update-notes', this.farmer, this.editedNotes);
      this.showNotesDialog = false;
    },

    getStatusColor(status) {
      const colors = {
        ACTIVE: 'success',
        INACTIVE: 'grey',
        PENDING: 'warning',
        BLOCKED: 'error',
      };
      return colors[status] || 'grey';
    },

    getStatusIcon(status) {
      const icons = {
        ACTIVE: 'mdi-check-circle',
        INACTIVE: 'mdi-pause-circle',
        PENDING: 'mdi-clock-outline',
        BLOCKED: 'mdi-block-helper',
      };
      return icons[status] || 'mdi-help-circle';
    },

    getOrderStatusColor(status) {
      const colors = {
        PENDING: 'warning',
        CONFIRMED: 'info',
        SHIPPED: 'primary',
        DELIVERED: 'success',
        CANCELLED: 'error',
      };
      return colors[status] || 'grey';
    },

    formatDate(date) {
      if (!date) return 'N/A';
      return new Date(date).toLocaleDateString();
    },

    formatDateTime(date) {
      if (!date) return 'N/A';
      return new Date(date).toLocaleString();
    },
  },
};
</script>

<style scoped>
.farmer-details {
  border-radius: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 1rem;
}

.metric-item {
  margin-bottom: 1rem;
}

.notes-content {
  background: #f5f5f5;
  padding: 1rem;
  border-radius: 8px;
  white-space: pre-wrap;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .v-container {
    padding: 1rem !important;
  }

  .info-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .info-item .v-icon {
    margin-bottom: 0.25rem;
  }
}

/* Touch-friendly interfaces */
@media (hover: none) and (pointer: coarse) {
  .v-btn {
    min-height: 44px;
  }
}
</style>
