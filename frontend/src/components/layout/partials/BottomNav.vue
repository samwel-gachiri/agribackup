<template>
  <nav class="billion-bottom-nav">
    <div class="billion-bottom-nav-bg"></div>
    <router-link
      v-for="item in filteredNavItems"
      :key="item.text"
      :to="item.link"
      class="billion-bottom-link"
      :aria-label="item.text"
      exact
    >
      <v-btn icon :color="isActive(item) ? 'primary' : 'grey darken-1'" class="billion-bottom-btn" :aria-label="item.text">
        <v-icon :class="{ 'billion-bottom-icon-active': isActive(item) }">{{ item.icon }}</v-icon>
      </v-btn>
      <span class="billion-bottom-label" :class="{'billion-bottom-label-active': isActive(item)}">
        {{ item.short || item.text.split(' ')[0] }}
      </span>
    </router-link>
  </nav>
</template>

<script>
// eslint-disable-next-line no-unused-vars
import { getCurrentUserId, viewPermissions, getCurrentUserRole } from '@/utils/roles.js';

export default {
  name: 'BottomNav',
  methods: {
    viewPermissions,
    isActive(item) {
      // Check if the current route matches the nav item
      return this.$route.name === (item.link && item.link.name);
    },
  },
  computed: {
    navItems() {
      // Mobile navigation items for all EUDR roles
      // NOTE: Roles must be UPPERCASE to match store.getters['auth/role']
      return [
        // === COMMON ===
        {
          icon: 'mdi-view-dashboard',
          text: 'Dashboard',
          short: 'Home',
          link: { name: 'Dashboard' },
          roles: ['FARMER', 'BUYER', 'EXPORTER', 'SYSTEM_ADMIN', 'ZONE_SUPERVISOR', 'AGGREGATOR', 'PROCESSOR', 'IMPORTER'],
        },

        // === FARMER ===
        {
          icon: 'mdi-map-marker-radius',
          text: 'My Units',
          short: 'Units',
          link: { name: 'FarmerProductionUnits' },
          roles: ['FARMER'],
        },
        {
          icon: 'mdi-sprout',
          text: 'My Produces',
          short: 'Farm',
          link: { name: 'MyFarm', params: { farmerId: getCurrentUserId() } },
          roles: ['FARMER'],
        },
        {
          icon: 'mdi-cash-multiple',
          text: 'Listings',
          short: 'Sales',
          link: { name: 'Listings' },
          roles: ['FARMER'],
        },

        // === AGGREGATOR ===
        {
          icon: 'mdi-basket-plus',
          text: 'Collection',
          short: 'Collect',
          link: { name: 'AggregatorCollection' },
          roles: ['AGGREGATOR'],
        },
        {
          icon: 'mdi-vector-polygon',
          text: 'Intersections',
          short: 'Spatial',
          link: { name: 'AggregatorSpatialIntersections' },
          roles: ['AGGREGATOR'],
        },

        // === EXPORTER ===
        {
          icon: 'mdi-transit-connection-variant',
          text: 'Traceability',
          short: 'Trace',
          link: { name: 'SupplyChainWorkflow' },
          roles: ['EXPORTER', 'SYSTEM_ADMIN'],
        },
        {
          icon: 'mdi-certificate',
          text: 'Certificates',
          short: 'Certs',
          link: { name: 'CertificateViewer' },
          roles: ['EXPORTER', 'IMPORTER', 'SYSTEM_ADMIN'],
        },
        {
          icon: 'mdi-account-multiple',
          text: 'Farmers',
          short: 'Farmers',
          link: { name: 'FarmersManagement' },
          roles: ['EXPORTER', 'SYSTEM_ADMIN'],
        },

        // === PROCESSOR ===
        {
          icon: 'mdi-factory',
          text: 'Processing',
          short: 'Process',
          link: { name: 'ProcessorDashboard' },
          roles: ['PROCESSOR'],
        },

        // === IMPORTER ===
        {
          icon: 'mdi-ship-wheel',
          text: 'Imports',
          short: 'Import',
          link: { name: 'ImporterDashboard' },
          roles: ['IMPORTER'],
        },

        // === ADMIN ===
        {
          icon: 'mdi-shield-crown',
          text: 'EUDR Admin',
          short: 'Admin',
          link: { name: 'EudrAdministration' },
          roles: ['SYSTEM_ADMIN'],
        },
      ];
    },
    currentRole() {
      // Get current user role for debugging
      return this.$store.getters['auth/role'] || 'none';
    },
    filteredNavItems() {
      // Filter nav items based on permissions and limit to 5 for mobile
      const filtered = this.navItems.filter((item) => this.viewPermissions(item.roles));

      // If no items match (user not logged in or role mismatch), show Dashboard as fallback
      if (filtered.length === 0) {
        return [{
          icon: 'mdi-view-dashboard',
          text: 'Dashboard',
          short: 'Home',
          link: { name: 'Dashboard' },
          roles: ['*'],
        }];
      }

      return filtered.slice(0, 5); // Max 5 items for mobile bottom nav
    },
  },
};
</script>

<style scoped>
.billion-bottom-nav {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  height: 68px;
  z-index: 1000;
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: transparent;
  box-shadow: 0 -4px 32px rgba(34,197,94,0.10);
}
.billion-bottom-nav-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  background: rgba(255,255,255,0.85);
  border-top: 1.5px solid rgba(34,197,94,0.10);
  box-shadow: 0 -8px 32px rgba(34,197,94,0.10);
  backdrop-filter: blur(18px) saturate(180%);
  border-radius: 18px 18px 0 0;
}
.billion-bottom-link {
  flex: 1 1 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  z-index: 1;
  position: relative;
  text-decoration: none;
}
.billion-bottom-btn {
  margin-bottom: 2px;
  background: none !important;
  box-shadow: none !important;
  border-radius: 50%;
  transition: background 0.18s, box-shadow 0.18s;
}
.billion-bottom-btn:active, .billion-bottom-btn:focus {
  background: rgba(34,197,94,0.10) !important;
}
.billion-bottom-icon-active {
  color: #22c55e !important;
  filter: drop-shadow(0 2px 8px #22c55e33);
}
.billion-bottom-label {
  font-size: 12px;
  line-height: 1.1rem;
  font-weight: 600;
  color: #64748b;
  letter-spacing: 0.01em;
  margin-top: -2px;
  transition: color 0.18s;
}
.billion-bottom-label-active {
  color: #22c55e;
  text-shadow: 0 2px 8px #22c55e22;
}
</style>
