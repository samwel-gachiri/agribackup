<template>
  <v-dialog
    v-model="dialog"
    fullscreen
    persistent
    transition="dialog-bottom-transition"
    class="role-selection-dialog"
  >
    <v-card class="role-dialog-card">
      <v-card-text class="role-dialog-content">
        <v-container fluid class="fill-height">
          <v-row align="center" justify="center" class="fill-height">
            <v-col cols="12" sm="10" md="8" lg="6">
              <!-- Header -->
              <div class="text-center mb-8">
                <h1 class="display-1 font-weight-bold mb-3">
                  Select Your Role
                </h1>
                <p class="text-h6 grey--text text--darken-1">
                  You have multiple roles. Please choose one to continue.
                </p>
              </div>

              <!-- Role Cards -->
              <v-row>
                <v-col
                  v-for="role in roles"
                  :key="role.roleType"
                  cols="12"
                  sm="6"
                  md="4"
                >
                  <v-card
                    class="role-card"
                    hover
                    ripple
                    @click="selectAndConfirm(role)"
                  >
                    <v-card-text class="role-card-content">
                      <div class="role-icon mb-4">
                        <v-icon size="64" color="black">
                          {{ getRoleIcon(role.roleType) }}
                        </v-icon>
                      </div>

                      <h3 class="text-h5 font-weight-bold mb-2 role-title">
                        {{ role.displayName }}
                      </h3>
                      <p class="text-body-2 grey--text">
                        {{ role.description }}
                      </p>
                    </v-card-text>
                  </v-card>
                </v-col>
              </v-row>

            </v-col>
          </v-row>
        </v-container>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  name: 'RoleSelectionDialog',
  data() {
    return {
      dialog: false,
      roles: [],
      selectedRoleType: null,
      resolveCallback: null,
    };
  },
  methods: {
    show(availableRoles) {
      this.roles = availableRoles || [];
      this.selectedRoleType = null;
      this.dialog = true;

      // Return promise that resolves when user selects a role
      return new Promise((resolve) => {
        this.resolveCallback = resolve;
      });
    },
    selectAndConfirm(role) {
      // Select and immediately confirm in one click
      this.selectedRoleType = role.roleType;
      if (this.resolveCallback) {
        this.dialog = false;
        this.resolveCallback(this.selectedRoleType);
        this.resolveCallback = null;
      }
    },
    getRoleIcon(roleType) {
      const icons = {
        FARMER: 'mdi-sprout',
        BUYER: 'mdi-cart',
        EXPORTER: 'mdi-airplane',
        AGGREGATOR: 'mdi-warehouse',
        PROCESSOR: 'mdi-factory',
        IMPORTER: 'mdi-package-variant',
      };
      return icons[roleType] || 'mdi-account';
    },
    getRoleColor(roleType) {
      const colors = {
        FARMER: 'green darken-2',
        BUYER: 'blue darken-2',
        EXPORTER: 'purple darken-2',
        AGGREGATOR: 'orange darken-2',
        PROCESSOR: 'indigo darken-2',
        IMPORTER: 'teal darken-2',
      };
      return colors[roleType] || 'grey darken-2';
    },
  },
};
</script>

<style scoped>
.role-dialog-card {
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
}

.role-dialog-content {
  padding: 24px 0;
}

.role-card {
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
  border-radius: 16px !important;
  background: white;
  height: 100%;
}

.role-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.12) !important;
  border-color: #2e7d32 !important;
}

.role-card-content {
  text-align: center;
}

.role-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 96px;
  height: 96px;
  margin: 0 auto;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.04);
}

.role-title {
  text-align: center;
  word-wrap: break-word;
}

/* Responsive adjustments */
@media (max-width: 960px) {
  .role-card-content {
    padding: 24px 16px !important;
  }

  .role-icon {
    width: 80px;
    height: 80px;
  }
}
</style>
