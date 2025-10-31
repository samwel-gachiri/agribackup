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
    };
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
      // In a real app, you would apply the theme here
    },
    updateLanguage() {
      localStorage.setItem('language', this.language);
      this.$toast.info(`Language changed to ${this.language}`);
      // In a real app, you would change the i18n locale here
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
