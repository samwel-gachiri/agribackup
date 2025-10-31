<template>
  <default-layout>
    <v-container fluid class="tw-py-6">
      <!-- Header -->
      <div class="tw-mb-6 tw-flex tw-items-center tw-justify-between">
        <div>
          <h1 class="tw-text-3xl tw-font-bold tw-text-slate-900">Processor Management</h1>
          <p class="tw-mt-1 tw-text-sm tw-text-slate-600">
            Connect with processors to build your supply chain network
          </p>
        </div>
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          @click="openAddDialog"
        >
          Add Processor
        </v-btn>
      </div>

      <!-- Processors Table -->
      <v-card>
        <v-data-table
          :headers="headers"
          :items="processors"
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
                label="Search processors..."
                single-line
                hide-details
                clearable
                variant="outlined"
                density="compact"
                class="tw-max-w-md"
              />
            </v-toolbar>
          </template>

          <!-- Processor column -->
          <template v-slot:item.facilityName="{ item }">
            <div class="tw-flex tw-items-center tw-gap-3 tw-py-2">
              <v-avatar color="purple" size="40">
                <span class="tw-text-sm tw-font-semibold">{{ getInitials(item.facilityName) }}</span>
              </v-avatar>
              <div>
                <div class="tw-font-semibold tw-text-slate-900">{{ item.facilityName }}</div>
                <div class="tw-text-xs tw-text-slate-500">{{ item.processingType || 'N/A' }}</div>
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
              @click="disconnectFromProcessor(item.id)"
            >
              Disconnect
            </v-btn>
            <v-btn
              v-else
              color="primary"
              variant="flat"
              size="small"
              :loading="connectingId === item.id"
              @click="connectToProcessor(item.id)"
            >
              Connect
            </v-btn>
          </template>

          <!-- Loading state -->
          <template #loading>
            <v-skeleton-loader type="table-row@5" />
          </template>

          <!-- No data state -->
          <template #no-data>
            <div class="tw-flex tw-flex-col tw-items-center tw-justify-center tw-gap-4 tw-py-12">
              <v-icon size="64" color="grey-lighten-1">mdi-factory</v-icon>
              <div class="tw-text-center">
                <p class="tw-text-lg tw-font-medium tw-text-slate-600">No processors found</p>
                <p class="tw-text-sm tw-text-slate-400">Add your first processor to get started</p>
              </div>
              <v-btn color="primary" @click="openAddDialog">
                Add Processor
              </v-btn>
            </div>
          </template>
        </v-data-table>
      </v-card>

      <!-- Add Processor Dialog -->
      <v-dialog v-model="showDialog" max-width="600px" persistent>
        <v-card>
          <v-card-title class="tw-bg-purple-600 tw-text-white">
            <span class="tw-text-xl tw-font-bold">Add New Processor</span>
          </v-card-title>

          <v-card-text class="tw-pt-6">
            <v-form ref="processorForm" v-model="formValid">
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="formData.facilityName"
                    label="Facility Name *"
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
                    v-model="formData.processingType"
                    label="Processing Type"
                    :items="processingTypes"
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
              @click="saveProcessor"
            >
              Add Processor
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
import { processorConnectionService } from '@/services/processorConnectionService.js';

export default {
  name: 'ProcessorsManagement',
  components: {
    DefaultLayout,
  },
  data() {
    return {
      processors: [],
      loading: false,
      saving: false,
      showDialog: false,
      searchQuery: '',
      connectingId: null,
      disconnectingId: null,
      formValid: false,
      headers: [
        { title: 'Processor', value: 'facilityName', sortable: true },
        { title: 'Contact', value: 'contact', sortable: false },
        { title: 'Location', value: 'facilityAddress', sortable: true },
        { title: 'Connection Status', value: 'connectionStatus', sortable: true, align: 'center' },
        { title: 'Actions', value: 'actions', sortable: false, align: 'center' },
      ],
      processingTypes: [
        'WASHING',
        'DRYING',
        'HULLING',
        'GRADING',
        'PACKAGING',
        'ROASTING',
        'MILLING',
        'OTHER',
      ],
      formData: {
        facilityName: '',
        email: '',
        phone: '',
        processingType: 'WASHING',
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
    this.fetchProcessors();
  },
  methods: {
    async fetchProcessors() {
      this.loading = true;
      try {
        // Fetch all processors from the system
        const allProcessorsResponse = await axios.get('/api/v1/processors', {
          params: { page: 0, size: 1000 },
        });

        const allProcessors = allProcessorsResponse.data.content || [];

        // If user is an exporter, fetch connected processors
        if (this.exporterId) {
          try {
            const connectedResponse = await processorConnectionService.getConnected(this.exporterId, 0, 1000);
            const connectedIds = new Set(
              (connectedResponse?.content || []).map((conn) => conn.id)
            );

            // Mark processors as connected or not
            this.processors = allProcessors.map((proc) => ({
              ...proc,
              isConnected: connectedIds.has(proc.id),
            }));
          } catch (error) {
            console.warn('Failed to fetch connections:', error);
            // If connection fetch fails, just show all processors as not connected
            this.processors = allProcessors.map((proc) => ({
              ...proc,
              isConnected: false,
            }));
          }
        } else {
          // Non-exporter users see all processors without connection status
          this.processors = allProcessors.map((proc) => ({
            ...proc,
            isConnected: false,
          }));
        }

        // Sort: connected first, then by facility name
        this.processors.sort((a, b) => {
          if (a.isConnected && !b.isConnected) return -1;
          if (!a.isConnected && b.isConnected) return 1;
          return (a.facilityName || '').localeCompare(b.facilityName || '');
        });

      } catch (error) {
        this.$toast.error('Failed to load processors');
        this.processors = [];
      } finally {
        this.loading = false;
      }
    },
    openAddDialog() {
      this.formData = {
        facilityName: '',
        email: '',
        phone: '',
        processingType: 'WASHING',
        facilityAddress: '',
      };
      this.showDialog = true;
    },
    closeDialog() {
      this.showDialog = false;
      this.formData = {
        facilityName: '',
        email: '',
        phone: '',
        processingType: 'WASHING',
        facilityAddress: '',
      };
    },
    async saveProcessor() {
      if (!this.$refs.processorForm.validate()) {
        return;
      }

      this.saving = true;
      try {
        const payload = {
          user: {
            fullName: this.formData.facilityName,
            email: this.formData.email,
            phoneNumber: this.formData.phone,
            password: 'ChangeMe123!', // Default password
          },
          facilityName: this.formData.facilityName,
          processingType: this.formData.processingType,
          facilityAddress: this.formData.facilityAddress,
        };

        await axios.post('/api/v1/auth/register/processor', payload);
        this.$toast.success('Processor added successfully');
        this.closeDialog();
        await this.fetchProcessors();
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to add processor');
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
    async connectToProcessor(processorId) {
      if (!this.exporterId) {
        this.$toast.error('Only exporters can connect to processors');
        return;
      }
      
      this.connectingId = processorId;
      try {
        await processorConnectionService.connect(processorId, this.exporterId);
        this.$toast.success('Successfully connected to processor');
        await this.fetchProcessors();
      } catch (error) {
        this.$toast.error('Failed to connect to processor');
      } finally {
        this.connectingId = null;
      }
    },
    async disconnectFromProcessor(processorId) {
      if (!this.exporterId) {
        this.$toast.error('Only exporters can disconnect from processors');
        return;
      }
      
      this.disconnectingId = processorId;
      try {
        await processorConnectionService.disconnect(processorId, this.exporterId);
        this.$toast.success('Successfully disconnected from processor');
        await this.fetchProcessors();
      } catch (error) {
        this.$toast.error('Failed to disconnect from processor');
      } finally {
        this.disconnectingId = null;
      }
    },
  },
};
</script>
