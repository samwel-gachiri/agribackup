<template>
  <v-card class="tw-px-4 md:tw-px-8 tw-py-4" flat>
    <v-progress-linear v-if="loading" indeterminate color="primary" class="tw-mb-4"></v-progress-linear>

    <!-- Current Roles Section -->
    <div class="tw-mb-6">
      <h2 class="tw-text-lg tw-font-semibold tw-text-green-600 tw-mb-3">Your Current Roles</h2>

      <v-alert v-if="!loading && currentRoles.length === 0" type="info" dense>
        No roles assigned yet.
      </v-alert>

      <v-expansion-panels v-else v-model="expandedPanel" accordion>
        <v-expansion-panel
          v-for="role in currentRoles"
          :key="role"
        >
          <v-expansion-panel-header>
            <div class="tw-flex tw-items-center tw-gap-3">
              <v-icon :color="getRoleColor(role)">{{ getRoleIcon(role) }}</v-icon>
              <span class="tw-font-medium">{{ formatRoleName(role) }}</span>
              <v-chip x-small color="success" class="tw-ml-2">Active</v-chip>
            </div>
          </v-expansion-panel-header>
          <v-expansion-panel-content>
            <!-- Exporter Properties -->
            <div v-if="role === 'EXPORTER' && exporterData" class="tw-space-y-4">
              <v-text-field
                v-model="exporterData.companyName"
                label="Company Name"
                outlined
                dense
                hide-details="auto"
              ></v-text-field>
              <v-text-field
                v-model="exporterData.licenseId"
                label="License ID"
                outlined
                dense
                hide-details="auto"
              ></v-text-field>
              <v-btn color="primary" small :loading="savingExporter" @click="saveExporterDetails">
                <v-icon left small>mdi-content-save</v-icon>
                Save Exporter Details
              </v-btn>
            </div>

            <!-- Aggregator Properties -->
            <div v-else-if="role === 'AGGREGATOR' && aggregatorData" class="tw-space-y-4">
              <v-text-field
                v-model="aggregatorData.organizationName"
                label="Organization Name"
                outlined
                dense
                hide-details="auto"
              ></v-text-field>
              <v-text-field
                v-model="aggregatorData.registrationNumber"
                label="Registration Number"
                outlined
                dense
                hide-details="auto"
              ></v-text-field>
              <v-btn color="primary" small :loading="savingAggregator" @click="saveAggregatorDetails">
                <v-icon left small>mdi-content-save</v-icon>
                Save Aggregator Details
              </v-btn>
            </div>

            <!-- Supplier Properties -->
            <div v-else-if="role === 'SUPPLIER' && supplierData" class="tw-space-y-4">
              <v-text-field
                v-model="supplierData.supplierName"
                label="Supplier/Business Name"
                outlined
                dense
                hide-details="auto"
              ></v-text-field>
              <v-select
                v-model="supplierData.supplierType"
                :items="supplierTypes"
                label="Supplier Type"
                outlined
                dense
                hide-details="auto"
              ></v-select>
              <v-text-field
                v-model="supplierData.countryCode"
                label="Country Code"
                outlined
                dense
                hide-details="auto"
              ></v-text-field>
              <v-btn color="primary" small :loading="savingSupplier" @click="saveSupplierDetails">
                <v-icon left small>mdi-content-save</v-icon>
                Save Supplier Details
              </v-btn>
            </div>

            <!-- Generic role (Farmer, Buyer, etc.) -->
            <div v-else class="tw-py-2 tw-text-gray-600">
              <v-icon small class="tw-mr-2">mdi-information</v-icon>
              This role doesn't have additional properties to configure.
            </div>
          </v-expansion-panel-content>
        </v-expansion-panel>
      </v-expansion-panels>
    </div>

    <v-divider class="tw-my-4"></v-divider>

    <!-- Add New Role Section -->
    <div>
      <h2 class="tw-text-lg tw-font-semibold tw-text-purple-600 tw-mb-3">Add Additional Role</h2>

      <v-alert v-if="availableRoles.length === 0" type="success" dense>
        <v-icon left>mdi-check-circle</v-icon>
        You have all available roles!
      </v-alert>

      <div v-else>
        <v-select
          v-model="selectedRole"
          :items="availableRoles"
          label="Select a role to add"
          outlined
          dense
          class="tw-mb-2"
        ></v-select>

        <v-select
          v-if="selectedRole === 'SUPPLIER'"
          v-model="newSupplierType"
          :items="supplierTypes"
          label="Select Supplier Type"
          outlined
          dense
          class="tw-mb-2"
        ></v-select>

        <v-btn
          color="success"
          :disabled="!selectedRole"
          :loading="addingRole"
          @click="addRole"
        >
          <v-icon left>mdi-plus</v-icon>
          Add {{ selectedRole ? formatRoleName(selectedRole) : 'Role' }}
        </v-btn>

        <v-alert
          v-if="roleAddSuccess"
          type="success"
          dense
          dismissible
          class="tw-mt-3"
        >
          Role added successfully! Please log out and log back in to access your new role features.
        </v-alert>
      </div>
    </div>
  </v-card>
</template>

<script>
import axios from 'axios';
import { mapState } from 'vuex';

export default {
  name: 'RolesForm',
  data() {
    return {
      loading: false,
      expandedPanel: null,
      // Role data
      exporterData: null,
      aggregatorData: null,
      supplierData: null,
      // Saving states
      savingExporter: false,
      savingAggregator: false,
      savingSupplier: false,
      // Add role
      selectedRole: null,
      newSupplierType: 'AGGREGATOR',
      addingRole: false,
      roleAddSuccess: false,
      // Role options
      allRoles: [
        { text: 'Farmer', value: 'FARMER' },
        { text: 'Buyer', value: 'BUYER' },
        { text: 'Exporter', value: 'EXPORTER' },
        { text: 'Aggregator', value: 'AGGREGATOR' },
        { text: 'Processor', value: 'PROCESSOR' },
        { text: 'Importer', value: 'IMPORTER' },
        { text: 'Supplier', value: 'SUPPLIER' },
      ],
      supplierTypes: [
        { text: 'Farmer', value: 'FARMER' },
        { text: 'Farmer Group', value: 'FARMER_GROUP' },
        { text: 'Aggregator', value: 'AGGREGATOR' },
        { text: 'Cooperative', value: 'COOPERATIVE' },
        { text: 'Trader', value: 'TRADER' },
        { text: 'Processor', value: 'PROCESSOR' },
        { text: 'Warehouse', value: 'WAREHOUSE' },
      ],
      // Roles fetched from API
      fetchedRoles: [],
    };
  },
  computed: {
    ...mapState({
      user: (state) => state.auth.user,
      userRole: (state) => state.auth.role,
    }),
    currentRoles() {
      // Use fetched roles from API, fallback to current auth role
      if (this.fetchedRoles.length > 0) {
        return this.fetchedRoles.map((r) => r.roleType.toUpperCase());
      }
      // Fallback to single auth role
      if (!this.userRole) return [];
      return [this.userRole.toUpperCase()];
    },
    availableRoles() {
      return this.allRoles.filter((r) => !this.currentRoles.includes(r.value));
    },
  },
  mounted() {
    this.loadUserRoles();
    this.loadRoleDetails();
  },
  methods: {
    async loadUserRoles() {
      try {
        const response = await axios.get('/api/auth/my-roles');
        if (response.data?.success && response.data?.data) {
          this.fetchedRoles = response.data.data;
        }
      } catch (err) {
        // change the following to this.$toast
        this.$toast.error('Failed to load user roles:', err.message);
        // Fallback to current auth role is handled in computed
      }
    },
    async loadRoleDetails() {
      this.loading = true;
      try {
        const userId = this.user?.id;
        if (!userId) return;

        // Load exporter data if user has exporter role
        if (this.currentRoles.includes('EXPORTER')) {
          await this.loadExporterData(userId);
        }

        // Load aggregator data if user has aggregator role
        if (this.currentRoles.includes('AGGREGATOR')) {
          await this.loadAggregatorData(userId);
        }

        // Load supplier data if user has supplier role
        if (this.currentRoles.includes('SUPPLIER')) {
          await this.loadSupplierData(userId);
        }
      } catch (error) {
        this.$toast.error('Failed to load role details:', error.message);
      } finally {
        this.loading = false;
      }
    },

    async loadExporterData(exporterId) {
      try {
        const response = await axios.get(`/exporters-service/exporter/${exporterId}`);
        if (response.data?.success && response.data?.data) {
          this.exporterData = {
            companyName: response.data.data.companyName || '',
            licenseId: response.data.data.licenseId || '',
            ...response.data.data,
          };
        } else if (response.data?.id) {
          this.exporterData = {
            companyName: response.data.companyName || '',
            licenseId: response.data.licenseId || '',
            ...response.data,
          };
        } else {
          this.exporterData = { companyName: '', licenseId: '' };
        }
      } catch (err) {
        this.$toast.error('Failed to load exporter data:', err.message);
        this.exporterData = { companyName: '', licenseId: '' };
      }
    },

    async loadAggregatorData(userId) {
      try {
        const aggregatorId = this.user?.entityId || userId;
        const response = await axios.get(`/api/v1/aggregators/${aggregatorId}`);
        if (response.data) {
          this.aggregatorData = response.data;
        }
      } catch (err) {
        this.$toast.error('Failed to load aggregator data:', err.message);
        this.aggregatorData = { organizationName: '', registrationNumber: '' };
      }
    },

    async loadSupplierData() {
      try {
        const supplierId = this.user?.supplierId;
        if (supplierId) {
          const response = await axios.get(`/api/v1/supply-chain/suppliers/${supplierId}`);
          if (response.data?.data) {
            this.supplierData = response.data.data;
          }
        } else {
          this.supplierData = { supplierName: '', supplierType: 'AGGREGATOR', countryCode: 'KE' };
        }
      } catch (err) {
        this.$toast.error('Failed to load supplier data:', err.message);
        this.supplierData = { supplierName: '', supplierType: 'AGGREGATOR', countryCode: 'KE' };
      }
    },

    async saveExporterDetails() {
      this.savingExporter = true;
      try {
        const userId = this.user?.id;
        await axios.put(`/exporters-service/exporter/${userId}`, {
          companyName: this.exporterData.companyName,
          licenseId: this.exporterData.licenseId,
        });
        this.$toast.success('Exporter details updated!');
      } catch (error) {
        this.$toast.error('Failed to update exporter details');
      } finally {
        this.savingExporter = false;
      }
    },

    async saveAggregatorDetails() {
      this.savingAggregator = true;
      try {
        const aggregatorId = this.user?.entityId;
        await axios.put(`/api/v1/aggregators/${aggregatorId}`, {
          organizationName: this.aggregatorData.organizationName,
          registrationNumber: this.aggregatorData.registrationNumber,
        });
        this.$toast.success('Aggregator details updated!');
      } catch (error) {
        this.$toast.error('Failed to update aggregator details');
      } finally {
        this.savingAggregator = false;
      }
    },

    async saveSupplierDetails() {
      this.savingSupplier = true;
      try {
        const supplierId = this.user?.supplierId;
        await axios.put(`/api/v1/supply-chain/suppliers/${supplierId}`, {
          supplierName: this.supplierData.supplierName,
          supplierType: this.supplierData.supplierType,
          countryCode: this.supplierData.countryCode,
        });
        this.$toast.success('Supplier details updated!');
      } catch (error) {
        this.$toast.error('Failed to update supplier details');
      } finally {
        this.savingSupplier = false;
      }
    },

    async addRole() {
      if (!this.selectedRole) return;

      this.addingRole = true;
      this.roleAddSuccess = false;

      try {
        // Use userId (actual UserProfile ID), NOT id (which is entity-specific like exporter ID)
        const userId = this.user?.userId || this.user?.id;
        if (!userId) {
          this.$toast.error('User ID not found');
          return;
        }

        const payload = {
          userId,
          roles: [this.selectedRole],
        };

        if (this.selectedRole === 'SUPPLIER') {
          payload.supplierType = this.newSupplierType;
        }

        await axios.post('/api/auth/assign-roles', payload);

        this.roleAddSuccess = true;
        this.selectedRole = null;

        // Reload role details
        await this.loadRoleDetails();
      } catch (error) {
        const msg = error.response?.data?.msg || error.message;
        this.$toast.error(`Failed to add role: ${msg}`);
      } finally {
        this.addingRole = false;
      }
    },

    formatRoleName(role) {
      if (!role) return '';
      return role.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());
    },

    getRoleIcon(role) {
      const icons = {
        FARMER: 'mdi-sprout',
        BUYER: 'mdi-cart',
        EXPORTER: 'mdi-export',
        AGGREGATOR: 'mdi-warehouse',
        PROCESSOR: 'mdi-factory',
        IMPORTER: 'mdi-import',
        SUPPLIER: 'mdi-truck-delivery',
      };
      return icons[role] || 'mdi-account';
    },

    getRoleColor(role) {
      const colors = {
        FARMER: 'green',
        BUYER: 'blue',
        EXPORTER: 'purple',
        AGGREGATOR: 'orange',
        PROCESSOR: 'teal',
        IMPORTER: 'indigo',
        SUPPLIER: 'amber',
      };
      return colors[role] || 'grey';
    },
  },
};
</script>
