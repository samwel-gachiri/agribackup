<template>
  <default-layout>
    <v-container fluid class="tw-py-6">
      <!-- Header -->
      <div class="tw-mb-6 tw-flex tw-items-center tw-justify-between">
        <div>
          <h1 class="tw-text-3xl tw-font-bold tw-text-slate-900">Aggregator Management</h1>
        </div>
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          @click="openAddDialog"
        >
          Add Aggregator
        </v-btn>
      </div>

      <!-- Aggregators Table -->
      <v-card>
        <v-data-table
          :headers="headers"
          :items="aggregators"
          :loading="loading"
          :search="searchQuery"
          class="elevation-1"
          :items-per-page="10"
        >
          <!-- Top toolbar with search -->
          <template #top>
            <v-toolbar flat>
              <v-text-field
                v-model="searchQuery"
                prepend-inner-icon="mdi-magnify"
                label="Search aggregators..."
                single-line
                hide-details
                clearable
                variant="outlined"
                density="compact"
                class="tw-max-w-md"
              />
            </v-toolbar>
          </template>

          <!-- Aggregator column -->
          <template v-slot:item.organizationName="{ item }">
            <div class="tw-flex tw-items-center tw-gap-3 tw-py-2">
              <v-avatar color="primary" size="40">
                <span class="tw-text-sm tw-font-semibold">{{ getInitials(item.organizationName) }}</span>
              </v-avatar>
              <div>
                <div class="tw-font-semibold tw-text-slate-900">{{ item.organizationName }}</div>
                <div class="tw-text-xs tw-text-slate-500">{{ item.organizationType || 'N/A' }}</div>
              </div>
            </div>
          </template>

          <!-- Contact column -->
          <template v-slot:item.contact="{ item }">
            <div class="tw-space-y-1">
              <div class="tw-text-sm">{{ item.userProfile?.email || 'N/A' }}</div>
              <div class="tw-text-xs tw-text-slate-500">{{ item.userProfile?.phoneNumber || 'N/A' }}</div>
            </div>
          </template>

          <!-- Location column -->
          <template v-slot:item.facilityAddress="{ item }">
            <div class="tw-text-sm">{{ item.facilityAddress || '—' }}</div>
          </template>

          <!-- Connection Status column -->
          <template v-slot:item.connectionStatus="{ item }">
            <v-chip
              :color="item.isConnected ? 'success' : 'grey'"
              size="small"
              variant="flat"
            >
              {{ item.isConnected ? 'Connected' : 'Not Connected' }}
            </v-chip>
          </template>

          <!-- Actions column -->
          <template v-slot:item.actions="{ item }">
            <v-btn
              v-if="item.isConnected"
              color="error"
              variant="text"
              size="small"
              :loading="disconnectingId === item.id"
              @click="disconnectFromAggregator(item.id)"
            >
              Disconnect
            </v-btn>
            <v-btn
              v-else
              color="primary"
              variant="flat"
              size="small"
              :loading="connectingId === item.id"
              @click="connectToAggregator(item.id)"
            >
              Connect
            </v-btn>
          </template>

          <!-- Loading state -->
          <template v-slot:loading>
            <v-skeleton-loader type="table-row@5" />
          </template>

          <!-- No data state -->
          <template v-slot:no-data>
            <div class="tw-flex tw-flex-col tw-items-center tw-justify-center tw-gap-4 tw-py-12">
              <v-icon size="64" color="grey-lighten-1">mdi-truck</v-icon>
              <div class="tw-text-center">
                <p class="tw-text-lg tw-font-medium tw-text-slate-600">No aggregators found</p>
                <p class="tw-text-sm tw-text-slate-400">Add your first aggregator to get started</p>
              </div>
              <v-btn color="primary" @click="openAddDialog">
                Add Aggregator
              </v-btn>
            </div>
          </template>
        </v-data-table>
      </v-card>

      <!-- Add Aggregator Dialog -->
      <v-dialog v-model="showDialog" max-width="600px" persistent>
        <v-card>
          <v-card-title class="tw-bg-primary tw-text-white">
            <span class="tw-text-xl tw-font-bold">Add New Aggregator</span>
          </v-card-title>

          <v-card-text class="tw-pt-6">
            <v-form ref="aggregatorForm" v-model="formValid">
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="formData.organizationName"
                    label="Organization Name *"
                    :rules="[rules.required]"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.email"
                    label="Email *"
                    type="email"
                    :rules="[rules.required, rules.email]"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.phone"
                    label="Phone Number"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>

                <v-col cols="12">
                  <v-select
                    v-model="formData.organizationType"
                    label="Organization Type"
                    :items="organizationTypes"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>

                <v-col cols="12">
                  <v-textarea
                    v-model="formData.facilityAddress"
                    label="Facility Address"
                    rows="3"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>
              </v-row>
            </v-form>
          </v-card-text>

          <v-card-actions class="tw-px-6 tw-pb-4">
            <v-spacer />
            <v-btn
              variant="text"
              @click="closeDialog"
            >
              Cancel
            </v-btn>
            <v-btn
              color="primary"
              :loading="saving"
              :disabled="!formValid"
              @click="saveAggregator"
            >
              Add Aggregator
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-container>
  </default-layout>
</template>

<script>
/* eslint-disable */
import axios from 'axios';
import DefaultLayout from '@/components/layout/Default.vue';
import { aggregatorConnectionService } from '@/services/aggregatorConnectionService.js';

export default {
  name: 'AggregatorsManagement',
  components: {
    DefaultLayout,
  },
  data() {
    return {
      aggregators: [],
      loading: false,
      saving: false,
      showDialog: false,
      searchQuery: '',
      connectingId: null,
      disconnectingId: null,
      formValid: false,
      headers: [
        { title: 'Aggregator', value: 'organizationName', sortable: true },
        { title: 'Contact', value: 'contact', sortable: false },
        { title: 'Location', value: 'facilityAddress', sortable: true },
        { title: 'Connection Status', value: 'connectionStatus', sortable: true, align: 'center' },
        { title: 'Actions', value: 'actions', sortable: false, align: 'center' },
      ],
      organizationTypes: [
        'COOPERATIVE',
        'PRIVATE_COMPANY',
        'ASSOCIATION',
        'GOVERNMENT',
        'NGO',
      ],
      formData: {
        organizationName: '',
        email: '',
        phone: '',
        organizationType: 'COOPERATIVE',
        facilityAddress: '',
      },
      rules: {
        required: (v) => !!v || 'This field is required',
        email: (v) => /.+@.+\..+/.test(v) || 'Email must be valid',
      },
    };
  },
  computed: {
    exporterId() {
      return this.$store.state.auth?.user?.id;
    },
  },
  mounted() {
    this.fetchAggregators();
  },
  methods: {
    async fetchAggregators() {
      this.loading = true;
      try {
        // Fetch all aggregators from the system
        const allAggregatorsResponse = await axios.get('/api/v1/aggregators', {
          params: { page: 0, size: 1000 },
        });

        const allAggregators = allAggregatorsResponse.data.content || [];

        // If user is an exporter, fetch connected aggregators
        if (this.exporterId) {
          try {
            const connectedResponse = await aggregatorConnectionService.getConnected(this.exporterId, 0, 1000);
            const connectedIds = new Set(
              (connectedResponse?.content || []).map((conn) => conn.id)
            );

            // Mark aggregators as connected or not
            this.aggregators = allAggregators.map((agg) => ({
              ...agg,
              isConnected: connectedIds.has(agg.id),
            }));
          } catch (error) {
            console.warn('Failed to fetch connections:', error);
            // If connection fetch fails, just show all aggregators as not connected
            this.aggregators = allAggregators.map((agg) => ({
              ...agg,
              isConnected: false,
            }));
          }
        } else {
          // Non-exporter users see all aggregators without connection status
          this.aggregators = allAggregators.map((agg) => ({
            ...agg,
            isConnected: false,
          }));
        }

        // Sort: connected first, then by organization name
        this.aggregators.sort((a, b) => {
          if (a.isConnected && !b.isConnected) return -1;
          if (!a.isConnected && b.isConnected) return 1;
          return (a.organizationName || '').localeCompare(b.organizationName || '');
        });


      } catch (error) {
        this.$toast.error('Failed to load aggregators');
        this.aggregators = [];
      } finally {
        this.loading = false;
      }
    },
    openAddDialog() {
      this.formData = {
        organizationName: '',
        email: '',
        phone: '',
        organizationType: 'COOPERATIVE',
        facilityAddress: '',
      };
      this.showDialog = true;
    },
    closeDialog() {
      this.showDialog = false;
      this.formData = {
        organizationName: '',
        email: '',
        phone: '',
        organizationType: 'COOPERATIVE',
        facilityAddress: '',
      };
    },
    async saveAggregator() {
      if (!this.$refs.aggregatorForm.validate()) {
        return;
      }

      this.saving = true;
      try {
        const payload = {
          user: {
            fullName: this.formData.organizationName,
            email: this.formData.email,
            phoneNumber: this.formData.phone,
            password: 'ChangeMe123!', // Default password
          },
          organizationName: this.formData.organizationName,
          organizationType: this.formData.organizationType,
          facilityAddress: this.formData.facilityAddress,
        };

        await axios.post('/api/v1/auth/register/aggregator', payload);
        this.$toast.success('Aggregator added successfully');
        this.closeDialog();
        await this.fetchAggregators();
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to add aggregator');
      } finally {
        this.saving = false;
      }
    },
    getInitials(name) {
      if (!name) return '?';
      return name
        .split(' ')
        .map((n) => n[0])
        .join('')
        .toUpperCase()
        .slice(0, 2);
    },
    async connectToAggregator(aggregatorId) {
      if (!this.exporterId) {
        this.$toast.error('Only exporters can connect to aggregators');
        return;
      }
      
      this.connectingId = aggregatorId;
      try {
        await aggregatorConnectionService.connect(aggregatorId, this.exporterId);
        this.$toast.success('Successfully connected to aggregator');
        await this.fetchAggregators();
      } catch (error) {
        this.$toast.error('Failed to connect to aggregator');
      } finally {
        this.connectingId = null;
      }
    },
    async disconnectFromAggregator(aggregatorId) {
      if (!this.exporterId) {
        this.$toast.error('Only exporters can disconnect from aggregators');
        return;
      }
      
      this.disconnectingId = aggregatorId;
      try {
        await aggregatorConnectionService.disconnect(aggregatorId, this.exporterId);
        this.$toast.success('Successfully disconnected from aggregator');
        await this.fetchAggregators();
      } catch (error) {
        this.$toast.error('Failed to disconnect from aggregator');
      } finally {
        this.disconnectingId = null;
      }
    },
  },
};
</script>
