<template>
  <v-card class="farmer-card elevation-2" :class="{ 'inactive': farmer.status !== 'ACTIVE' }">
    <!-- Farmer Header -->
    <v-card-title class="pb-2">
      <v-avatar size="48" class="mr-3">
        <v-img
          :src="farmer.farmer.avatar || '/default-avatar.png'"
          :alt="farmer.farmer.fullName"
        ></v-img>
      </v-avatar>
      <div class="flex-grow-1">
        <div class="text-h6 font-weight-bold">{{ farmer.farmer.fullName }}</div>
        <div class="text-caption text--secondary">
          <v-icon small class="mr-1">mdi-map-marker</v-icon>
          {{ farmer.farmer.location?.customName || 'Location not set' }}
        </div>
      </div>
      <v-menu bottom left>
        <template v-slot:activator="{ on, attrs }">
          <v-btn icon v-bind="attrs" v-on="on">
            <v-icon>mdi-dots-vertical</v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-item @click="$emit('view-details', farmer)">
            <v-list-item-icon>
              <v-icon>mdi-eye</v-icon>
            </v-list-item-icon>
            <v-list-item-title>View Details</v-list-item-title>
          </v-list-item>
          <v-list-item @click="showNotesDialog = true">
            <v-list-item-icon>
              <v-icon>mdi-note-edit</v-icon>
            </v-list-item-icon>
            <v-list-item-title>Edit Notes</v-list-item-title>
          </v-list-item>
          <v-divider></v-divider>
          <v-list-item @click="$emit('remove-farmer', farmer)" class="error--text">
            <v-list-item-icon>
              <v-icon color="error">mdi-account-remove</v-icon>
            </v-list-item-icon>
            <v-list-item-title>Remove Farmer</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </v-card-title>

    <!-- Status Badge -->
    <div class="status-badge">
      <v-chip
        :color="getStatusColor(farmer.status)"
        text-color="white"
        small
      >
        <v-icon left small>{{ getStatusIcon(farmer.status) }}</v-icon>
        {{ farmer.status }}
      </v-chip>
    </div>

    <!-- Performance Metrics -->
    <v-card-text class="pt-2">
      <v-row dense>
        <v-col cols="6">
          <div class="metric-item">
            <v-icon small color="primary" class="mr-1">mdi-star</v-icon>
            <span class="text-caption">Rating:</span>
            <div class="font-weight-bold">
              {{ farmer.performanceMetrics?.qualityRating?.toFixed(1) || 'N/A' }}
            </div>
          </div>
        </v-col>
        <v-col cols="6">
          <div class="metric-item">
            <v-icon small color="success" class="mr-1">mdi-cart</v-icon>
            <span class="text-caption">Orders:</span>
            <div class="font-weight-bold">
              {{ farmer.performanceMetrics?.totalOrders || 0 }}
            </div>
          </div>
        </v-col>
        <v-col cols="6">
          <div class="metric-item">
            <v-icon small color="warning" class="mr-1">mdi-clock</v-icon>
            <span class="text-caption">Reliability:</span>
            <div class="font-weight-bold">
              {{ farmer.performanceMetrics?.reliabilityScore || 0 }}%
            </div>
          </div>
        </v-col>
        <v-col cols="6">
          <div class="metric-item">
            <v-icon small color="info" class="mr-1">mdi-sprout</v-icon>
            <span class="text-caption">Harvests:</span>
            <div class="font-weight-bold">
              {{ farmer.upcomingHarvests?.length || 0 }}
            </div>
          </div>
        </v-col>
      </v-row>

      <!-- Connection Date -->
      <div class="mt-3 text-caption text--secondary">
        <v-icon small class="mr-1">mdi-calendar-plus</v-icon>
        Connected {{ formatDate(farmer.connectionDate) }}
      </div>

      <!-- Notes Preview -->
      <div v-if="farmer.notes" class="mt-2">
        <div class="text-caption text--secondary mb-1">Notes:</div>
        <div class="notes-preview text-body-2">
          {{ truncateNotes(farmer.notes) }}
        </div>
      </div>

      <!-- Upcoming Harvests -->
      <div v-if="farmer.upcomingHarvests && farmer.upcomingHarvests.length > 0" class="mt-3">
        <div class="text-caption text--secondary mb-2">Upcoming Harvests:</div>
        <v-chip
          v-for="harvest in farmer.upcomingHarvests.slice(0, 2)"
          :key="harvest.id"
          small
          outlined
          class="mr-1 mb-1"
        >
          {{ harvest.produceName }}
        </v-chip>
        <v-chip
          v-if="farmer.upcomingHarvests.length > 2"
          small
          outlined
          class="mr-1 mb-1"
        >
          +{{ farmer.upcomingHarvests.length - 2 }} more
        </v-chip>
      </div>
    </v-card-text>

    <!-- Action Buttons -->
    <v-card-actions>
      <v-btn
        text
        color="primary"
        @click="$emit('view-details', farmer)"
      >
        <v-icon left small>mdi-eye</v-icon>
        View Details
      </v-btn>
      <v-spacer></v-spacer>
      <v-btn
        text
        color="success"
        :disabled="farmer.status !== 'ACTIVE'"
        @click="contactFarmer"
      >
        <v-icon left small>mdi-phone</v-icon>
        Contact
      </v-btn>
    </v-card-actions>

    <!-- Notes Dialog -->
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
export default {
  name: 'FarmerCard',
  props: {
    farmer: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      showNotesDialog: false,
      editedNotes: '',
    };
  },
  watch: {
    showNotesDialog(val) {
      if (val) {
        this.editedNotes = this.farmer.notes || '';
      }
    },
  },
  methods: {
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

    formatDate(date) {
      if (!date) return 'Unknown';
      return new Date(date).toLocaleDateString();
    },

    truncateNotes(notes) {
      if (!notes) return '';
      return notes.length > 100 ? `${notes.substring(0, 100)}...` : notes;
    },

    contactFarmer() {
      // Open phone dialer or messaging app
      if (this.farmer.farmer.phoneNumber) {
        window.open(`tel:${this.farmer.farmer.phoneNumber}`);
      } else {
        this.$toast.info('No phone number available for this farmer');
      }
    },

    saveNotes() {
      this.$emit('update-notes', this.farmer, this.editedNotes);
      this.showNotesDialog = false;
    },
  },
};
</script>

<style scoped>
.farmer-card {
  border-radius: 12px;
  transition: all 0.3s ease;
  position: relative;
  margin-bottom: 1rem;
}

.farmer-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
}

.farmer-card.inactive {
  opacity: 0.7;
}

.status-badge {
  position: absolute;
  top: 8px;
  right: 8px;
}

.metric-item {
  display: flex;
  align-items: center;
  margin-bottom: 0.5rem;
}

.metric-item .text-caption {
  margin-left: 0.25rem;
  margin-right: 0.5rem;
}

.notes-preview {
  background: #f5f5f5;
  padding: 0.5rem;
  border-radius: 4px;
  font-style: italic;
}

/* Mobile responsiveness */
@media (max-width: 768px) {
  .farmer-card {
    margin-bottom: 0.5rem;
  }

  .v-card-title {
    padding: 1rem 1rem 0.5rem 1rem;
  }

  .v-card-text {
    padding: 0.5rem 1rem;
  }

  .v-card-actions {
    padding: 0.5rem 1rem 1rem 1rem;
  }
}

/* Touch-friendly interfaces */
@media (hover: none) and (pointer: coarse) {
  .farmer-card:hover {
    transform: none;
  }

  .v-btn {
    min-height: 44px;
  }
}
</style>
