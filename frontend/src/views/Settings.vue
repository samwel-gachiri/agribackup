<template>
  <v-container class="settings-container">
    <v-row justify="center">
      <v-col cols="12" md="8" lg="6">
        <v-card class="settings-card">
          <v-card-title class="settings-header">
            <v-icon left>mdi-cog</v-icon>
            Settings
          </v-card-title>

          <v-card-text>
            <!-- Wallet Connection Section -->
            <v-card outlined class="mb-4">
              <v-card-title class="subsection-header">
                <v-icon left color="primary">mdi-wallet</v-icon>
                Hedera Wallet Connection
              </v-card-title>
              <v-card-text>
                <wallet-connection-example />
              </v-card-text>
            </v-card>

            <!-- EUDR Navigation Toggle -->
            <v-card outlined class="mb-4">
              <v-card-title class="subsection-header">
                <v-icon left color="success">mdi-shield-check</v-icon>
                EUDR Compliance Features
              </v-card-title>
              <v-card-text>
                <v-switch
                  v-model="eudrEnabled"
                  label="Enable EUDR navigation and features"
                  color="success"
                  inset
                  @change="toggleEudrNavigation"
                />
                <v-alert
                  v-if="!eudrEnabled"
                  type="info"
                  dense
                  class="mt-2"
                >
                  EUDR compliance features are hidden. Toggle to show EUDR-related navigation and dashboards.
                </v-alert>
                <v-alert
                  v-else
                  type="success"
                  dense
                  class="mt-2"
                >
                  EUDR compliance features are enabled. You can access EUDR dashboards and tools.
                </v-alert>
              </v-card-text>
            </v-card>

            <!-- Manage Roles Section -->
            <v-card outlined class="mb-4">
              <v-card-title class="subsection-header">
                <v-icon left color="purple">mdi-account-multiple</v-icon>
                Manage Your Roles
              </v-card-title>
              <v-card-text>
                <!-- Current Roles -->
                <div class="mb-4">
                  <div class="text-subtitle-2 mb-2">Current Roles:</div>
                  <div class="d-flex flex-wrap gap-2">
                    <v-chip
                      v-for="role in currentRoles"
                      :key="role"
                      color="primary"
                      small
                      class="mr-2 mb-2"
                    >
                      <v-icon left x-small>mdi-check-circle</v-icon>
                      {{ formatRoleName(role) }}
                    </v-chip>
                    <v-chip v-if="currentRoles.length === 0" color="grey" small outlined>
                      No roles assigned
                    </v-chip>
                  </div>
                </div>

                <!-- Add Role Section -->
                <v-divider class="mb-4"></v-divider>
                <div class="text-subtitle-2 mb-2">Add Additional Role:</div>
                <v-alert
                  v-if="availableRoles.length === 0"
                  type="info"
                  dense
                  class="mb-3"
                >
                  You have all available roles.
                </v-alert>
                <div v-else>
                  <v-select
                    v-model="selectedRole"
                    :items="availableRoles"
                    label="Select a role to add"
                    outlined
                    dense
                    class="mb-2"
                  ></v-select>

                  <!-- Supplier Type (shown when SUPPLIER is selected) -->
                  <v-select
                    v-if="selectedRole === 'SUPPLIER'"
                    v-model="selectedSupplierType"
                    :items="supplierTypes"
                    label="Supplier Type"
                    outlined
                    dense
                    class="mb-2"
                  ></v-select>

                  <v-btn
                    color="success"
                    :disabled="!selectedRole"
                    :loading="addingRole"
                    @click="addRole"
                  >
                    <v-icon left>mdi-plus</v-icon>
                    Add Role
                  </v-btn>
                </div>

                <v-alert
                  v-if="roleAddSuccess"
                  type="success"
                  dense
                  dismissible
                  class="mt-3"
                >
                  Role added successfully! Please log out and log back in to access your new role.
                </v-alert>
              </v-card-text>
            </v-card>

            <!-- User Preferences -->
            <v-card outlined class="mb-4">
              <v-card-title class="subsection-header">
                <v-icon left color="info">mdi-account-cog</v-icon>
                User Preferences
              </v-card-title>
              <v-card-text>
                <v-select
                  v-model="theme"
                  :items="themeOptions"
                  label="Theme"
                  outlined
                  dense
                  @change="updateTheme"
                />
                <v-select
                  v-model="language"
                  :items="languageOptions"
                  label="Language"
                  outlined
                  dense
                  class="mt-3"
                  @change="updateLanguage"
                />
              </v-card-text>
            </v-card>

            <!-- System Information -->
            <v-card outlined>
              <v-card-title class="subsection-header">
                <v-icon left color="grey">mdi-information</v-icon>
                System Information
              </v-card-title>
              <v-card-text>
                <v-simple-table dense>
                  <tbody>
                    <tr>
                      <td><strong>Version</strong></td>
                      <td>{{ appVersion }}</td>
                    </tr>
                    <tr>
                      <td><strong>Environment</strong></td>
                      <td>{{ environment }}</td>
                    </tr>
                    <tr>
                      <td><strong>Last Updated</strong></td>
                      <td>{{ lastUpdated }}</td>
                    </tr>
                  </tbody>
                </v-simple-table>
              </v-card-text>
            </v-card>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import axios from 'axios';
import WalletConnectionExample from '@/components/WalletConnectionExample.vue';

export default {
  name: 'Settings',
  components: {
    WalletConnectionExample,
  },
  data() {
    return {
      eudrEnabled: true,
      theme: 'light',
      language: 'en',
      themeOptions: [
        { text: 'Light', value: 'light' },
        { text: 'Dark', value: 'dark' },
        { text: 'Auto', value: 'auto' },
      ],
      languageOptions: [
        { text: 'English', value: 'en' },
        { text: 'Spanish', value: 'es' },
        { text: 'French', value: 'fr' },
      ],
      appVersion: '1.0.0',
      environment: 'Production',
      lastUpdated: new Date().toLocaleDateString(),
      // Role management
      selectedRole: null,
      selectedSupplierType: 'AGGREGATOR',
      addingRole: false,
      roleAddSuccess: false,
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
    };
  },
  computed: {
    currentRoles() {
      return this.$store.state.auth?.user?.roles || [];
    },
    availableRoles() {
      const current = this.currentRoles.map((r) => r.toUpperCase());
      return this.allRoles.filter((r) => !current.includes(r.value));
    },
  },
  mounted() {
    this.loadSettings();
  },
  methods: {
    loadSettings() {
      // Load settings from localStorage
      this.eudrEnabled = localStorage.getItem('eudrEnabled') !== 'false';
      this.theme = localStorage.getItem('theme') || 'light';
      this.language = localStorage.getItem('language') || 'en';
    },
    toggleEudrNavigation() {
      localStorage.setItem('eudrEnabled', this.eudrEnabled.toString());
      this.$toast.success(`EUDR features ${this.eudrEnabled ? 'enabled' : 'disabled'}`);

      // Emit event to update navigation
      this.$emit('eudr-toggled', this.eudrEnabled);
    },
    updateTheme() {
      localStorage.setItem('theme', this.theme);
      this.$toast.info(`Theme changed to ${this.theme}`);
    },
    updateLanguage() {
      localStorage.setItem('language', this.language);
      this.$toast.info(`Language changed to ${this.language}`);
    },
    formatRoleName(role) {
      if (!role) return '';
      return role.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());
    },
    async addRole() {
      if (!this.selectedRole) return;

      this.addingRole = true;
      this.roleAddSuccess = false;

      try {
        const userId = this.$store.state.auth?.user?.id;
        if (!userId) {
          this.$toast.error('User not authenticated');
          return;
        }

        const payload = {
          userId,
          roles: [this.selectedRole],
        };

        // Add supplier type if SUPPLIER role
        if (this.selectedRole === 'SUPPLIER') {
          payload.supplierType = this.selectedSupplierType;
        }

        await axios.post('/api/auth/assign-roles', payload);

        this.roleAddSuccess = true;
        this.selectedRole = null;
        this.$toast.success('Role added successfully! Please log out and log back in.');
      } catch (error) {
        const msg = error.response?.data?.msg || error.message;
        this.$toast.error(`Failed to add role: ${msg}`);
      } finally {
        this.addingRole = false;
      }
    },
  },
};
</script>

<style scoped>
.settings-container {
  padding: 20px;
}

.settings-card {
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.settings-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px 12px 0 0;
}

.subsection-header {
  font-size: 1.1rem;
  font-weight: 600;
  padding-bottom: 8px;
}

.v-card.outlined {
  border: 2px solid #e0e0e0;
}

@media (max-width: 960px) {
  .settings-container {
    padding: 10px;
  }
}

</style>
