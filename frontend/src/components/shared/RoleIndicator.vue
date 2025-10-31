<template>
  <div class="role-indicator">
    <v-chip
      :color="roleColor"
      small
      outlined
      class="role-chip"
    >
      <v-icon left small>{{ roleIcon }}</v-icon>
      Logged in as {{ displayRole }}
    </v-chip>
  </div>
</template>

<script>
import { getCurrentUserRole } from '@/utils/roles.js';

export default {
  name: 'RoleIndicator',
  computed: {
    currentRole() {
      return getCurrentUserRole();
    },
    displayRole() {
      const roleMap = {
        FARMER: 'Farmer',
        BUYER: 'Buyer',
        EXPORTER: 'Super Admin',
        SYSTEM_ADMIN: 'System Admin',
        ZONE_SUPERVISOR: 'Zone Supervisor',
      };
      return roleMap[this.currentRole] || 'User';
    },
    roleColor() {
      const colorMap = {
        FARMER: 'green',
        BUYER: 'blue',
        EXPORTER: 'purple',
        SYSTEM_ADMIN: 'orange',
        ZONE_SUPERVISOR: 'teal',
      };
      return colorMap[this.currentRole] || 'grey';
    },
    roleIcon() {
      const iconMap = {
        FARMER: 'mdi-barn',
        BUYER: 'mdi-cart',
        EXPORTER: 'mdi-export',
        SYSTEM_ADMIN: 'mdi-account-cog',
        ZONE_SUPERVISOR: 'mdi-account-supervisor',
      };
      return iconMap[this.currentRole] || 'mdi-account';
    },
  },
};
</script>

<style scoped>
.role-indicator {
  display: flex;
  justify-content: center;
  margin: 8px 0;
}

.role-chip {
  font-weight: 600;
  letter-spacing: 0.5px;
}
</style>
