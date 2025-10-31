<template>
  <div class="eudr-administration">
    <v-container fluid>
      <!-- Header -->
      <v-row>
        <v-col cols="12">
          <div class="d-flex align-center justify-space-between mb-4">
            <div class="d-flex align-center">
              <v-icon large color="red" class="mr-3">mdi-shield-crown</v-icon>
              <div>
                <h1 class="text-h4 font-weight-bold">EUDR Administration</h1>
                <p class="text-subtitle-1 text--secondary mb-0">
                  System-wide EUDR administration and oversight
                </p>
              </div>
            </div>
            <v-btn
              color="primary"
              @click="refreshSystemData"
              elevation="2"
              :loading="refreshing"
            >
              <v-icon left>mdi-refresh</v-icon>
              Refresh Data
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <!-- System Overview -->
      <v-row class="mb-4">
        <v-col cols="12" sm="6" md="3">
          <v-card class="admin-card users" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-account-group</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ totalUsers }}</div>
              <div class="text-subtitle-2">Total Users</div>
              <div class="text-caption">Active in system</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="admin-card batches" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-package-variant</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ totalBatches }}</div>
              <div class="text-subtitle-2">Total Batches</div>
              <div class="text-caption">Under management</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="admin-card compliance" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-shield-check</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ complianceRate }}%</div>
              <div class="text-subtitle-2">Compliance Rate</div>
              <div class="text-caption">System-wide</div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" sm="6" md="3">
          <v-card class="admin-card alerts" elevation="4">
            <v-card-text class="text-center">
              <v-icon large color="white">mdi-alert-circle</v-icon>
              <div class="text-h3 font-weight-bold mt-2">{{ activeAlerts }}</div>
              <div class="text-subtitle-2">Active Alerts</div>
              <div class="text-caption">Require attention</div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- Administration Tabs -->
      <v-row>
        <v-col cols="12">
          <v-card elevation="3">
            <v-tabs v-model="activeTab" background-color="primary" dark>
              <v-tab>
                <v-icon left>mdi-cog</v-icon>
                System Configuration
              </v-tab>
              <v-tab>
                <v-icon left>mdi-account-multiple</v-icon>
                User Management
              </v-tab>
              <v-tab>
                <v-icon left>mdi-database</v-icon>
                Data Management
              </v-tab>
              <v-tab>
                <v-icon left>mdi-chart-line</v-icon>
                System Analytics
              </v-tab>
            </v-tabs>

            <v-tabs-items v-model="activeTab">
              <!-- System Configuration Tab -->
              <v-tab-item>
                <v-card-text>
                  <v-row>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="text-h6">
                          <v-icon color="primary" class="mr-2">mdi-server</v-icon>
                          Hedera Network Configuration
                        </v-card-title>
                        <v-card-text>
                          <v-select
                            v-model="systemConfig.hederaNetwork"
                            :items="networkOptions"
                            label="Active Network"
                            outlined
                            dense
                          ></v-select>
                          <v-text-field
                            v-model="systemConfig.hederaAccountId"
                            label="Account ID"
                            outlined
                            dense
                            readonly
                          ></v-text-field>
                          <div class="d-flex align-center">
                            <v-icon :color="systemConfig.networkStatus === 'Connected' ? 'success' : 'error'" class="mr-2">
                              {{ systemConfig.networkStatus === 'Connected' ? 'mdi-check-circle' : 'mdi-alert-circle' }}
                            </v-icon>
                            <span>{{ systemConfig.networkStatus }}</span>
                          </div>
                        </v-card-text>
                      </v-card>
                    </v-col>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="text-h6">
                          <v-icon color="warning" class="mr-2">mdi-map</v-icon>
                          Country Risk Configuration
                        </v-card-title>
                        <v-card-text>
                          <v-data-table
                            :headers="countryHeaders"
                            :items="countryRiskConfig"
                            :items-per-page="5"
                            class="elevation-0"
                            dense
                          >
                            <template v-slot:item.riskLevel="{ item }">
                              <v-select
                                v-model="item.riskLevel"
                                :items="riskLevels"
                                dense
                                @change="updateCountryRisk(item)"
                              ></v-select>
                            </template>
                          </v-data-table>
                        </v-card-text>
                      </v-card>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-tab-item>

              <!-- User Management Tab -->
              <v-tab-item>
                <v-card-text>
                  <div class="d-flex justify-space-between align-center mb-4">
                    <h3>User Management</h3>
                    <v-btn color="primary" @click="showCreateUserDialog = true">
                      <v-icon left>mdi-plus</v-icon>
                      Add User
                    </v-btn>
                  </div>
                  <v-data-table
                    :headers="userHeaders"
                    :items="users"
                    :search="userSearch"
                    class="elevation-0"
                  >
                    <template v-slot:top>
                      <v-text-field
                        v-model="userSearch"
                        append-icon="mdi-magnify"
                        label="Search users..."
                        single-line
                        hide-details
                        class="mb-4"
                      ></v-text-field>
                    </template>
                    <template v-slot:item.role="{ item }">
                      <v-chip
                        :color="getRoleColor(item.role)"
                        text-color="white"
                        small
                      >
                        {{ item.role }}
                      </v-chip>
                    </template>
                    <template v-slot:item.status="{ item }">
                      <v-chip
                        :color="item.status === 'Active' ? 'success' : 'error'"
                        text-color="white"
                        small
                      >
                        {{ item.status }}
                      </v-chip>
                    </template>
                    <template v-slot:item.actions="{ item }">
                      <v-btn icon small color="primary" @click="editUser(item)">
                        <v-icon>mdi-pencil</v-icon>
                      </v-btn>
                      <v-btn icon small color="error" @click="deactivateUser(item)">
                        <v-icon>mdi-account-off</v-icon>
                      </v-btn>
                    </template>
                  </v-data-table>
                </v-card-text>
              </v-tab-item>

              <!-- Data Management Tab -->
              <v-tab-item>
                <v-card-text>
                  <v-row>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="text-h6">
                          <v-icon color="info" class="mr-2">mdi-database-export</v-icon>
                          Data Export
                        </v-card-title>
                        <v-card-text>
                          <v-select
                            v-model="exportConfig.dataType"
                            :items="exportTypes"
                            label="Data Type"
                            outlined
                            dense
                          ></v-select>
                          <v-select
                            v-model="exportConfig.format"
                            :items="exportFormats"
                            label="Export Format"
                            outlined
                            dense
                          ></v-select>
                          <v-menu
                            v-model="dateMenu"
                            :close-on-content-click="false"
                            transition="scale-transition"
                            offset-y
                            min-width="auto"
                          >
                            <template v-slot:activator="{ on, attrs }">
                              <v-text-field
                                v-model="exportConfig.dateRange"
                                label="Date Range"
                                prepend-icon="mdi-calendar"
                                readonly
                                v-bind="attrs"
                                v-on="on"
                                outlined
                                dense
                              ></v-text-field>
                            </template>
                            <v-date-picker
                              v-model="exportConfig.dateRange"
                              range
                              @input="dateMenu = false"
                            ></v-date-picker>
                          </v-menu>
                          <v-btn color="primary" block @click="exportData" :loading="exporting">
                            <v-icon left>mdi-download</v-icon>
                            Export Data
                          </v-btn>
                        </v-card-text>
                      </v-card>
                    </v-col>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="text-h6">
                          <v-icon color="success" class="mr-2">mdi-backup-restore</v-icon>
                          System Backup
                        </v-card-title>
                        <v-card-text>
                          <div class="backup-info">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Last Backup:</span>
                              <span class="font-weight-bold">{{ lastBackup }}</span>
                            </div>
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Backup Size:</span>
                              <span class="font-weight-bold">{{ backupSize }}</span>
                            </div>
                            <div class="d-flex justify-space-between align-center mb-4">
                              <span>Status:</span>
                              <v-chip color="success" text-color="white" x-small>
                                Healthy
                              </v-chip>
                            </div>
                          </div>
                          <v-btn color="success" block @click="createBackup" :loading="backing">
                            <v-icon left>mdi-content-save</v-icon>
                            Create Backup
                          </v-btn>
                        </v-card-text>
                      </v-card>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-tab-item>

              <!-- System Analytics Tab -->
              <v-tab-item>
                <v-card-text>
                  <v-row>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="text-h6">
                          <v-icon color="primary" class="mr-2">mdi-chart-bar</v-icon>
                          Usage Statistics
                        </v-card-title>
                        <v-card-text>
                          <div class="analytics-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Daily Active Users</span>
                              <span class="font-weight-bold">{{ analytics.dailyUsers }}</span>
                            </div>
                            <v-progress-linear
                              :value="(analytics.dailyUsers / totalUsers) * 100"
                              color="primary"
                              height="8"
                              rounded
                            ></v-progress-linear>
                          </div>
                          <div class="analytics-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Batches Created Today</span>
                              <span class="font-weight-bold">{{ analytics.batchesToday }}</span>
                            </div>
                            <v-progress-linear
                              :value="analytics.batchesToday * 10"
                              color="success"
                              height="8"
                              rounded
                            ></v-progress-linear>
                          </div>
                          <div class="analytics-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Documents Uploaded</span>
                              <span class="font-weight-bold">{{ analytics.documentsToday }}</span>
                            </div>
                            <v-progress-linear
                              :value="analytics.documentsToday * 5"
                              color="info"
                              height="8"
                              rounded
                            ></v-progress-linear>
                          </div>
                        </v-card-text>
                      </v-card>
                    </v-col>
                    <v-col cols="12" md="6">
                      <v-card outlined>
                        <v-card-title class="text-h6">
                          <v-icon color="warning" class="mr-2">mdi-alert-octagon</v-icon>
                          System Health
                        </v-card-title>
                        <v-card-text>
                          <div class="health-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Database Performance</span>
                              <v-chip color="success" text-color="white" x-small>
                                Excellent
                              </v-chip>
                            </div>
                          </div>
                          <div class="health-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Hedera Network</span>
                              <v-chip color="success" text-color="white" x-small>
                                Connected
                              </v-chip>
                            </div>
                          </div>
                          <div class="health-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>External APIs</span>
                              <v-chip color="warning" text-color="white" x-small>
                                Degraded
                              </v-chip>
                            </div>
                          </div>
                          <div class="health-item">
                            <div class="d-flex justify-space-between align-center mb-2">
                              <span>Storage Usage</span>
                              <span class="font-weight-bold">{{ storageUsage }}%</span>
                            </div>
                            <v-progress-linear
                              :value="storageUsage"
                              :color="storageUsage > 80 ? 'error' : 'success'"
                              height="8"
                              rounded
                            ></v-progress-linear>
                          </div>
                        </v-card-text>
                      </v-card>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-tab-item>
            </v-tabs-items>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
export default {
  name: 'EudrAdministration',
  data() {
    return {
      refreshing: false,
      activeTab: 0,
      userSearch: '',
      dateMenu: false,
      exporting: false,
      backing: false,
      showCreateUserDialog: false,

      // System stats
      totalUsers: 1247,
      totalBatches: 3456,
      complianceRate: 87,
      activeAlerts: 23,

      // System configuration
      systemConfig: {
        hederaNetwork: 'Testnet',
        hederaAccountId: '0.0.123456',
        networkStatus: 'Connected',
      },

      networkOptions: ['Testnet', 'Mainnet'],

      // Country risk configuration
      countryHeaders: [
        { text: 'Country', value: 'country' },
        { text: 'Risk Level', value: 'riskLevel' },
      ],
      countryRiskConfig: [
        { country: 'Kenya', riskLevel: 'Low' },
        { country: 'Brazil', riskLevel: 'High' },
        { country: 'Indonesia', riskLevel: 'High' },
        { country: 'Colombia', riskLevel: 'Standard' },
      ],
      riskLevels: ['Low', 'Standard', 'High'],

      // User management
      userHeaders: [
        { text: 'Name', value: 'name' },
        { text: 'Email', value: 'email' },
        { text: 'Role', value: 'role' },
        { text: 'Status', value: 'status' },
        { text: 'Last Login', value: 'lastLogin' },
        { text: 'Actions', value: 'actions', sortable: false },
      ],
      users: [
        {
          id: 1,
          name: 'John Smith',
          email: 'john@example.com',
          role: 'FARMER',
          status: 'Active',
          lastLogin: '2024-12-15',
        },
        {
          id: 2,
          name: 'Jane Doe',
          email: 'jane@example.com',
          role: 'EXPORTER',
          status: 'Active',
          lastLogin: '2024-12-14',
        },
      ],

      // Export configuration
      exportConfig: {
        dataType: '',
        format: '',
        dateRange: [],
      },
      exportTypes: ['Batches', 'Users', 'Documents', 'Compliance Reports'],
      exportFormats: ['CSV', 'JSON', 'PDF', 'Excel'],

      // Backup info
      lastBackup: '2024-12-15 02:00 AM',
      backupSize: '2.3 GB',

      // Analytics
      analytics: {
        dailyUsers: 156,
        batchesToday: 23,
        documentsToday: 45,
      },
      storageUsage: 67,
    };
  },
  methods: {
    async refreshSystemData() {
      this.refreshing = true;
      try {
        // Simulate API call
        await new Promise((resolve) => setTimeout(resolve, 1500));
        this.$toast.success('System data refreshed successfully');
      } catch (error) {
        this.$toast.error('Failed to refresh system data');
      } finally {
        this.refreshing = false;
      }
    },

    getRoleColor(role) {
      const colors = {
        FARMER: 'success',
        EXPORTER: 'primary',
        SYSTEM_ADMIN: 'error',
        BUYER: 'info',
      };
      return colors[role] || 'grey';
    },

    updateCountryRisk(country) {
      this.$toast.success(`Updated ${country.country} risk level to ${country.riskLevel}`);
    },

    editUser() {
    },

    deactivateUser() {
    },

    async exportData() {
      this.exporting = true;
      try {
        // Simulate export
        await new Promise((resolve) => setTimeout(resolve, 2000));
        this.$toast.success('Data export completed successfully');
      } catch (error) {
        this.$toast.error('Failed to export data');
      } finally {
        this.exporting = false;
      }
    },

    async createBackup() {
      this.backing = true;
      try {
        // Simulate backup
        await new Promise((resolve) => setTimeout(resolve, 3000));
        this.lastBackup = new Date().toLocaleString();
        this.$toast.success('System backup created successfully');
      } catch (error) {
        this.$toast.error('Failed to create backup');
      } finally {
        this.backing = false;
      }
    },
  },
};
</script>

<style scoped>
.eudr-administration {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100vh;
  padding: 20px 0;
}

.admin-card {
  transition: transform 0.2s;
  color: white;
}

.admin-card:hover {
  transform: translateY(-2px);
}

.admin-card.users {
  background: linear-gradient(135deg, #2196f3, #1565c0);
}

.admin-card.batches {
  background: linear-gradient(135deg, #4caf50, #2e7d32);
}

.admin-card.compliance {
  background: linear-gradient(135deg, #9c27b0, #6a1b9a);
}

.admin-card.alerts {
  background: linear-gradient(135deg, #f44336, #c62828);
}

.analytics-item,
.health-item {
  margin-bottom: 16px;
}

.backup-info {
  background: rgba(0, 0, 0, 0.02);
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 16px;
}
</style>
