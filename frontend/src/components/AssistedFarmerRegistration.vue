<template>
  <v-dialog v-model="dialog" max-width="700px" persistent>
    <v-card>
      <v-card-title class="tw-font-bold tw-text-lg tw-bg-blue-100">
        Add Farmers to Zone: <span class="tw-text-blue-700 tw-ml-2">{{ zone?.name }}</span>
      </v-card-title>
      <v-card-text>
              <div class="tw-space-y-6">
                <div class="tw-bg-gray-50 tw-p-4 tw-rounded-lg tw-border tw-border-gray-200">
                  <h3 class="tw-font-semibold tw-text-gray-700 tw-mb-2">Add Existing Farmer</h3>
                  <div class="tw-grid md:tw-grid-cols-3 tw-gap-3">
                    <v-text-field v-model.trim="lookup.email" label="Email" dense hide-details="auto" />
                    <v-text-field v-model.trim="lookup.phoneNumber" label="Phone" dense hide-details="auto" />
                    <v-btn :loading="lookupLoading" color="primary" class="tw-mt-1" @click="lookupAndAddExisting" :disabled="!canLookup">Add Existing</v-btn>
                  </div>
                  <div v-if="lookupError" class="tw-text-xs tw-text-red-600 tw-mt-2">{{ lookupError }}</div>
                  <div v-if="lookupSuccess" class="tw-text-xs tw-text-green-600 tw-mt-2">{{ lookupSuccess }}</div>
                </div>
                <div class="tw-flex tw-items-center tw-gap-2 tw-text-xs tw-text-gray-500 tw-italic">
                  <v-icon small>mdi-information</v-icon>
                  <span>If farmer not found, register below.</span>
                </div>
                <FarmerSignUp @farmer-registered="handleFarmerRegistered" />
          <div v-if="addedFarmers.length" class="tw-mt-4">
            <h3 class="tw-font-semibold tw-text-green-700 tw-mb-2">Farmers Added to Zone</h3>
            <ul class="tw-list-disc tw-pl-6">
              <li v-for="farmer in addedFarmers" :key="farmer.id" class="tw-mb-1">
                {{ farmer.name }} ({{ farmer.phoneNumber }})
              </li>
            </ul>
          </div>
        </div>
      </v-card-text>
      <v-card-actions>
        <v-btn color="primary" @click="resetForm" class="tw-mr-2">Add Another Farmer</v-btn>
        <v-spacer />
        <v-btn color="success" @click="closeDialog">Done</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
import axios from 'axios';
import FarmerSignUp from '@/components/auth/FarmerSignUp.vue';

export default {
  name: 'AssistedFarmerRegistration',
  components: { FarmerSignUp },
  props: {
    zoneId: { type: [String, Number], required: true },
    zone: { type: Object, required: true },
    value: { type: Boolean, default: false }, // v-model for dialog
  },
  data() {
    return {
      dialog: this.value,
      addedFarmers: [],
      lastFarmer: null,
      lookup: { email: '', phoneNumber: '' },
      lookupLoading: false,
      lookupError: '',
      lookupSuccess: '',
    };
  },
  watch: {
    value(val) { this.dialog = val; },
    dialog(val) { this.$emit('input', val); },
  },
  computed: {
    canLookup() {
      const l = this.lookup || {}; // defensive
      return ((l.email && l.email.trim()) || (l.phoneNumber && l.phoneNumber.trim())) && !this.lookupLoading;
    },
  },
  methods: {
    async handleFarmerRegistered(farmer) {
      // farmer: { id, name, phoneNumber, ... }
      this.lastFarmer = farmer;
      try {
        // Use canonical admin-service endpoint expecting AddFarmerToZoneDto body
        await axios.post(`/api/admin-service/zones/${this.zoneId}/farmers`, { farmerId: farmer.id });
        this.addedFarmers.push(farmer);
        this.$toast.success('Farmer added to zone successfully');
      } catch (error) {
        this.$toast.error(`Failed to add farmer to zone: ${error.message}`);
      }
    },
    async lookupAndAddExisting() {
      // Ensure lookup object exists
      if (!this.lookup) this.lookup = { email: '', phoneNumber: '' };
      this.lookupError = '';
      this.lookupSuccess = '';
      const email = (this.lookup.email || '').trim();
      const phone = (this.lookup.phoneNumber || '').trim();
      if (!email && !phone) { this.lookupError = 'Provide email or phone'; return; }
      // Basic email format guard (optional: keeps backend from choking on undefined)
      if (email && !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) { this.lookupError = 'Invalid email format'; return; }
      this.lookupLoading = true;
      try {
        const payload = { email: email || null, phoneNumber: phone || null };
        const res = await axios.post(`/api/admin-service/zones/${this.zoneId}/farmers/lookup`, payload);
        if (res.data && res.data.success) {
          const farmerDto = res.data.data;
          const farmer = { id: farmerDto.farmerId, name: farmerDto.farmerName, phoneNumber: farmerDto.phoneNumber };
          // avoid duplicates
          if (!this.addedFarmers.find((f) => f.id === farmer.id)) this.addedFarmers.push(farmer);
          this.lookupSuccess = 'Existing farmer added to zone';
        } else {
          this.lookupError = res.data?.msg || 'Failed to add existing farmer';
        }
      } catch (e) {
        this.lookupError = e.response?.data?.msg || e.message;
      } finally { this.lookupLoading = false; }
    },
    resetForm() {
      // Reset FarmerSignUp by emitting event or using key
      this.lastFarmer = null;
      this.lookup = { email: '', phoneNumber: '' };
      // Optionally, you can use a key prop on FarmerSignUp to force re-render
    },
    closeDialog() {
      this.dialog = false;
      this.$emit('closed'); // parent should fetchZoneFarmers
    },
  },
};
</script>

<style scoped>
/* ...existing code... */
</style>
