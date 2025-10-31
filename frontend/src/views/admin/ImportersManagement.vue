<template>
  <default-layout>
    <v-container fluid class="tw-py-6">
      <!-- Header -->
      <div class="tw-mb-6 tw-flex tw-items-center tw-justify-between">
        <div>
          <h1 class="tw-text-3xl tw-font-bold tw-text-slate-900">Importer Management</h1>
          <p class="tw-mt-1 tw-text-sm tw-text-slate-600">
            Connect with importers to build your supply chain network
          </p>
        </div>
        <v-btn
          color="primary"
          prepend-icon="mdi-plus"
          @click="openAddDialog"
        >
          Add Importer
        </v-btn>
      </div>

      <!-- Importers Table -->
      <v-card>
        <v-data-table
          :headers="headers"
          :items="importers"
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
                label="Search importers..."
                single-line
                hide-details
                clearable
                variant="outlined"
                density="compact"
                class="tw-max-w-md"
              />
            </v-toolbar>
          </template>

          <!-- Importer column -->
          <template v-slot:item.companyName="{ item }">
            <div class="tw-flex tw-items-center tw-gap-3 tw-py-2">
              <v-avatar color="cyan" size="40">
                <span class="tw-text-sm tw-font-semibold">{{ getInitials(item.companyName) }}</span>
              </v-avatar>
              <div>
                <div class="tw-font-semibold tw-text-slate-900">{{ item.companyName }}</div>
                <div class="tw-text-xs tw-text-slate-500">{{ item.importLicenseNumber || 'N/A' }}</div>
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

          <!-- Country column -->
          <template v-slot:item.operatingCountry="{ item }">
            <div class="tw-text-sm">{{ item.operatingCountry || '—' }}</div>
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
              @click="disconnectFromImporter(item.id)"
            >
              Disconnect
            </v-btn>
            <v-btn
              v-else
              color="primary"
              variant="flat"
              size="small"
              :loading="connectingId === item.id"
              @click="connectToImporter(item.id)"
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
              <v-icon size="64" color="grey-lighten-1">mdi-ship-wheel</v-icon>
              <div class="tw-text-center">
                <p class="tw-text-lg tw-font-medium tw-text-slate-600">No importers found</p>
                <p class="tw-text-sm tw-text-slate-400">Add your first importer to get started</p>
              </div>
              <v-btn color="primary" @click="openAddDialog">
                Add Importer
              </v-btn>
            </div>
          </template>
        </v-data-table>
      </v-card>

      <!-- Add Importer Dialog -->
      <v-dialog v-model="showDialog" max-width="600px" persistent>
        <v-card>
          <v-card-title class="tw-bg-cyan-600 tw-text-white">
            <span class="tw-text-xl tw-font-bold">Add New Importer</span>
          </v-card-title>

          <v-card-text class="tw-pt-6">
            <v-form ref="importerForm" v-model="formValid">
              <v-row>
                <v-col cols="12">
                  <v-text-field
                    v-model="formData.companyName"
                    label="Company Name *"
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

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.operatingCountry"
                    label="Operating Country"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>

                <v-col cols="12" md="6">
                  <v-text-field
                    v-model="formData.importLicenseNumber"
                    label="Import License Number"
                    variant="outlined"
                    density="comfortable"
                  />
                </v-col>

                <v-col cols="12">
                  <v-textarea
                    v-model="formData.businessAddress"
                    label="Business Address"
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
              @click="saveImporter"
            >
              Add Importer
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
import { importerConnectionService } from '@/services/importerConnectionService.js';

export default {
  name: 'ImportersManagement',
  components: {
    DefaultLayout,
  },
  data() {
    return {
      importers: [],
      loading: false,
      saving: false,
      showDialog: false,
      searchQuery: '',
      connectingId: null,
      disconnectingId: null,
      formValid: false,
      headers: [
        { title: 'Importer', value: 'companyName', sortable: true },
        { title: 'Contact', value: 'contact', sortable: false },
        { title: 'Country', value: 'operatingCountry', sortable: true },
        { title: 'Connection Status', value: 'connectionStatus', sortable: true, align: 'center' },
        { title: 'Actions', value: 'actions', sortable: false, align: 'center' },
      ],
      formData: {
        companyName: '',
        email: '',
        phone: '',
        operatingCountry: '',
        importLicenseNumber: '',
        businessAddress: '',
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
    this.fetchImporters();
  },
  methods: {
    async fetchImporters() {
      this.loading = true;
      try {
        // Fetch all importers from the system
        const allImportersResponse = await axios.get('/api/v1/importers', {
          params: { page: 0, size: 1000 },
        });

        const allImporters = allImportersResponse.data.content || [];

        // Only fetch connections if user is an exporter
        if (this.exporterId) {
          try {
            const connectedResponse = await importerConnectionService.getConnected(this.exporterId, 0, 1000);
            const connectedIds = new Set(
              (connectedResponse?.content || []).map((conn) => conn.id)
            );

            // Mark importers as connected or not
            this.importers = allImporters.map((imp) => ({
              ...imp,
              isConnected: connectedIds.has(imp.id),
            }));
          } catch (connectionError) {
            // If connection fetch fails, show all as not connected
            this.importers = allImporters.map((imp) => ({
              ...imp,
              isConnected: false,
            }));
          }
        } else {
          // Non-exporters see all importers as not connected
          this.importers = allImporters.map((imp) => ({
            ...imp,
            isConnected: false,
          }));
        }

        // Sort: connected first, then by company name
        this.importers.sort((a, b) => {
          if (a.isConnected && !b.isConnected) return -1;
          if (!a.isConnected && b.isConnected) return 1;
          return (a.companyName || '').localeCompare(b.companyName || '');
        });

      } catch (error) {
        this.$toast.error('Failed to load importers');
        this.importers = [];
      } finally {
        this.loading = false;
      }
    },
    openAddDialog() {
      this.formData = {
        companyName: '',
        email: '',
        phone: '',
        operatingCountry: '',
        importLicenseNumber: '',
        businessAddress: '',
      };
      this.showDialog = true;
    },
    closeDialog() {
      this.showDialog = false;
      this.formData = {
        companyName: '',
        email: '',
        phone: '',
        operatingCountry: '',
        importLicenseNumber: '',
        businessAddress: '',
      };
    },
    async saveImporter() {
      if (!this.$refs.importerForm.validate()) {
        return;
      }

      this.saving = true;
      try {
        const payload = {
          user: {
            fullName: this.formData.companyName,
            email: this.formData.email,
            phoneNumber: this.formData.phone,
            password: 'ChangeMe123!', // Default password
          },
          companyName: this.formData.companyName,
          operatingCountry: this.formData.operatingCountry,
          importLicenseNumber: this.formData.importLicenseNumber,
          businessAddress: this.formData.businessAddress,
        };

        await axios.post('/api/v1/auth/register/importer', payload);
        this.$toast.success('Importer added successfully');
        this.closeDialog();
        await this.fetchImporters();
      } catch (error) {
        this.$toast.error(error.response?.data?.message || 'Failed to add importer');
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
    async connectToImporter(importerId) {
      if (!this.exporterId) {
        this.$toast.error('Only exporters can connect to importers');
        return;
      }
      
      this.connectingId = importerId;
      try {
        await importerConnectionService.connect(importerId, this.exporterId);
        this.$toast.success('Successfully connected to importer');
        await this.fetchImporters();
      } catch (error) {
        this.$toast.error('Failed to connect to importer');
      } finally {
        this.connectingId = null;
      }
    },
    async disconnectFromImporter(importerId) {
      if (!this.exporterId) {
        this.$toast.error('Only exporters can disconnect from importers');
        return;
      }
      
      this.disconnectingId = importerId;
      try {
        await importerConnectionService.disconnect(importerId, this.exporterId);
        this.$toast.success('Successfully disconnected from importer');
        await this.fetchImporters();
      } catch (error) {
        this.$toast.error('Failed to disconnect from importer');
      } finally {
        this.disconnectingId = null;
      }
    },
  },
};
</script>
