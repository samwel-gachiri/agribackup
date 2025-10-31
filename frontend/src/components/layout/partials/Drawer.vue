<!-- eslint-disable vue/no-use-v-if-with-v-for -->
<template>
  <div class="">
    <div class="app-drawer-glass">
      <!-- Header: Brand + User (minimal) -->
      <div class="app-drawer-header">
        <div class="app-brand-row">
          <logo-title class="app-brand" />
        </div>
        <div class="app-user-mini tw-w-full" v-if="displayName || userRole">
          <span class="app-portal-badge">Portal</span>
<!--          <avatar class="mr-2" />-->
          <div class="app-user-info tw-flex tw-flex-row tw-border-r tw-py-2 tw-px-4 tw-rounded-lg">
            <!-- <div class="app-user-name">{{ displayName || 'User' }}</div> -->
            <div class="app-role-chip">
              <span>{{ userRole || 'Guest' }}</span>
            </div>
          </div>
        </div>
        <role-indicator />
      </div>

      <!-- Navigation (comprehensive, role-based) -->
      <nav class="app-nav-list">
        <template v-for="(item, i) in navigationItems">
          <!-- Section with children (collapsible) -->
          <div v-if="item.isSection && canView(item)" :key="`section-${i}`" class="app-nav-section">
            <div class="app-nav-section-header" @click="toggleSection(item.text)">
              <span class="app-nav-icon">
                <v-icon :color="item.iconColor || 'primary'">{{ item.icon }}</v-icon>
              </span>
              <span class="app-nav-text">{{ item.text }}</span>
              <v-icon class="app-nav-expand-icon" :class="{ 'app-nav-expanded': isSectionExpanded(item.text) }">
                mdi-chevron-down
              </v-icon>
            </div>
            <transition name="app-nav-expand">
              <div v-show="isSectionExpanded(item.text)" class="app-nav-children">
                <router-link
                  v-for="(child, j) in item.children"
                  v-if="canView(child)"
                  :key="`${i}-${j}`"
                  :to="child.link"
                  class="app-nav-item app-nav-child"
                  active-class="app-nav-active"
                  exact
                >
                  <span class="app-nav-icon">
                    <v-icon :color="child.iconColor || 'primary'">{{ child.icon }}</v-icon>
                  </span>
                  <span class="app-nav-text">{{ child.text }}</span>
                </router-link>
              </div>
            </transition>
          </div>
          <!-- Regular navigation item -->
          <router-link
            v-else-if="!item.isSection && canView(item)"
            :key="`item-${i}`"
            :to="item.link"
            class="app-nav-item"
            active-class="app-nav-active"
            exact
          >
            <span class="app-nav-icon">
              <v-icon :color="item.iconColor || 'primary'">{{ item.icon }}</v-icon>
            </span>
            <span class="app-nav-text">{{ item.text }}</span>
          </router-link>
        </template>
      </nav>

      <!-- Feedback (minimal) -->
      <div v-if="false" class="app-feedback-section">
        <v-btn block text color="primary" class="app-feedback-btn" @click="$emit('openFeedback')">
          <v-icon left>mdi-lightbulb-outline</v-icon>
          Suggest Improvement
        </v-btn>
      </div>
    </div>
  </div>
</template>

<script>

import jwtDecode from 'jwt-decode';
import LogoTitle from '@/components/shared/LogoText.vue';
// eslint-disable-next-line vue/no-unused-components
import Avatar from '@/components/layout/partials/nav/Avatar.vue';
// eslint-disable-next-line no-unused-vars
import RoleIndicator from '@/components/shared/RoleIndicator.vue';

export default {
  name: 'AppDrawer',
  // eslint-disable-next-line vue/no-unused-components
  components: { LogoTitle, Avatar, RoleIndicator },
  data: () => ({
    expandedSections: [], // Default expanded sections
    navigationItems: [
      // Common: Dashboard for all roles
      {
        icon: 'mdi-apps-box',
        text: 'Dashboard',
        link: { name: 'Dashboard' },
        roles: ['FARMER', 'BUYER', 'EXPORTER', 'SYSTEM_ADMIN', 'ZONE_SUPERVISOR', 'AGGREGATOR'],
        iconColor: '#222',
      },

      // === FARMER PORTAL (NON-EUDR - COMMENTED OUT) ===
      // {
      //   icon: 'mdi-barn',
      //   text: 'My Farm',
      //   get link() {
      //     const id = getCurrentUserId();
      //     return id ? { name: 'MyFarm', params: { farmerId: id } } : { name: 'MyFarm' };
      //   },
      //   roles: ['FARMER'],
      //   iconColor: '#16a34a',
      // },
      // {
      //   icon: 'mdi-sprout-outline',
      //   text: 'Harvest & Yields',
      //   link: { name: 'HarvestAndYields' },
      //   roles: ['FARMER'],
      //   iconColor: '#f97316',
      // },
      // {
      //   icon: 'mdi-format-list-bulleted-square',
      //   text: 'Listings',
      //   link: { name: 'Listings' },
      //   roles: ['FARMER'],
      //   iconColor: '#6366f1',
      // },
      // {
      //   icon: 'mdi-truck-delivery-outline',
      //   text: 'Orders',
      //   link: { name: 'FarmerOrders' },
      //   roles: ['FARMER'],
      //   iconColor: '#0ea5e9',
      // },
      // {
      //   icon: 'mdi-chart-line',
      //   text: 'Reports',
      //   link: { name: 'FarmerReports' },
      //   roles: ['FARMER'],
      //   iconColor: '#8b5cf6',
      // },

      // === FARMER EUDR SECTION ===
      {
        icon: 'mdi-map-marker-radius',
        text: 'My Production Units',
        link: { name: 'FarmerProductionUnits' },
        roles: ['FARMER'],
        iconColor: '#16a34a',
      },
      {
        icon: 'mdi-file-document-multiple',
        text: 'My Documents',
        link: { name: 'FarmerDocuments' },
        roles: ['FARMER'],
        iconColor: '#0ea5e9',
      },

      // === AGGREGATOR EUDR SECTION ===
      {
        icon: 'mdi-vector-polygon',
        text: 'Spatial Intersections',
        link: { name: 'AggregatorSpatialIntersections' },
        roles: ['AGGREGATOR'],
        iconColor: '#2563eb',
      },

      // === PROCESSOR EUDR SECTION ===
      {
        icon: 'mdi-factory',
        text: 'Processor Dashboard',
        link: { name: 'ProcessorDashboard' },
        roles: ['PROCESSOR'],
        iconColor: '#7c3aed',
      },

      // === IMPORTER EUDR SECTION ===
      {
        icon: 'mdi-ship-wheel',
        text: 'Importer Dashboard',
        link: { name: 'ImporterDashboard' },
        roles: ['IMPORTER'],
        iconColor: '#0891b2',
      },

      // === SYSTEM ADMIN EUDR SECTION ===
      {
        icon: 'mdi-shield-crown',
        text: 'EUDR Administration',
        link: { name: 'EudrAdministration' },
        roles: ['SYSTEM_ADMIN'],
        iconColor: '#dc2626',
      },
      {
        icon: 'mdi-database-check',
        text: 'Data Verification',
        link: { name: 'DataVerification' },
        roles: ['SYSTEM_ADMIN'],
        iconColor: '#16a34a',
      },
      {
        icon: 'mdi-chart-timeline-variant',
        text: 'System Analytics',
        link: { name: 'SystemAnalytics' },
        roles: ['SYSTEM_ADMIN'],
        iconColor: '#8b5cf6',
      },

      // === BUYER PORTAL (NON-EUDR - COMMENTED OUT) ===
      // {
      //   icon: 'mdi-account-group',
      //   text: 'My Farmers',
      //   link: { name: 'MyFarmers' },
      //   roles: ['BUYER'],
      //   iconColor: '#16a34a',
      // },
      // {
      //   icon: 'mdi-magnify',
      //   text: 'Browse Listings',
      //   link: { name: 'BrowseListings' },
      //   roles: ['BUYER'],
      //   iconColor: '#6366f1',
      // },
      // {
      //   icon: 'mdi-cart-outline',
      //   text: 'My Orders',
      //   link: { name: 'BuyerOrders' },
      //   roles: ['BUYER'],
      //   iconColor: '#0ea5e9',
      // },
      // {
      //   icon: 'mdi-map-marker-path',
      //   text: 'Pickup Planning',
      //   link: { name: 'PickupPlanning' },
      //   roles: ['BUYER'],
      //   iconColor: '#f59e0b',
      // },
      // {
      //   icon: 'mdi-chart-areaspline',
      //   text: 'Analytics',
      //   link: { name: 'BuyerAnalytics' },
      //   roles: ['BUYER'],
      //   iconColor: '#8b5cf6',
      // },

      // === EXPORTER PORTAL (EUDR Management) ===
      {
        icon: 'mdi-shield-check',
        text: 'EUDR Compliance',
        isSection: true,
        roles: ['EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR'],
        iconColor: '#dc2626',
        children: [
          {
            icon: 'mdi-alert-octagon',
            text: 'Risk Management',
            link: { name: 'RiskManagement' },
            roles: ['EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR'],
            iconColor: '#ea580c',
          },
          {
            icon: 'mdi-clipboard-check-outline',
            text: 'Mitigation Tracking',
            link: { name: 'MitigationTracking' },
            roles: ['EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR'],
            iconColor: '#f59e0b',
          },
          {
            icon: 'mdi-file-document-outline',
            text: 'Compliance Reporting',
            link: { name: 'ComplianceReporting' },
            roles: ['EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR'],
            iconColor: '#10b981',
          },
          {
            icon: 'mdi-certificate',
            text: 'Certificate Viewer',
            link: { name: 'CertificateViewer' },
            roles: ['EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR', 'IMPORTER'],
            iconColor: '#3b82f6',
          },
        ],
      },
      {
        icon: 'mdi-map-marker-radius',
        text: 'Zones',
        link: { name: 'ZoneManagement' },
        roles: ['EXPORTER', 'SYSTEM_ADMIN'],
        iconColor: '#f59e42',
      },
      {
        icon: 'mdi-account-multiple',
        text: 'Farmers',
        link: { name: 'FarmersManagement' },
        roles: ['EXPORTER', 'SYSTEM_ADMIN'],
        iconColor: '#f97316',
      },
      {
        icon: 'mdi-truck-delivery',
        text: 'Aggregators',
        link: { name: 'AggregatorsManagement' },
        roles: ['EXPORTER', 'SYSTEM_ADMIN'],
        iconColor: '#2563eb',
      },
      {
        icon: 'mdi-factory',
        text: 'Processors',
        link: { name: 'ProcessorsManagement' },
        roles: ['EXPORTER', 'SYSTEM_ADMIN'],
        iconColor: '#7c3aed',
      },
      {
        icon: 'mdi-ship-wheel',
        text: 'Importers',
        link: { name: 'ImportersManagement' },
        roles: ['EXPORTER', 'SYSTEM_ADMIN'],
        iconColor: '#0891b2',
      },
      {
        icon: 'mdi-transit-connection-variant',
        text: 'Supply Chain Workflow',
        link: { name: 'SupplyChainWorkflow' },
        roles: ['EXPORTER', 'SYSTEM_ADMIN'],
        iconColor: '#10b981',
      },

      // === ZONE SUPERVISOR PORTAL (NON-EUDR - COMMENTED OUT) ===
      // {
      //   icon: 'mdi-map-marker-path',
      //   text: 'Zone Management',
      //   link: { name: 'ZoneManagement' },
      //   roles: ['ZONE_SUPERVISOR'],
      //   iconColor: '#f59e42',
      // },
      // {
      //   icon: 'mdi-account-multiple',
      //   text: 'Zone Farmers',
      //   link: { name: 'ZoneFarmers' },
      //   roles: ['ZONE_SUPERVISOR'],
      //   iconColor: '#16a34a',
      // },
      // {
      //   icon: 'mdi-truck-delivery',
      //   text: 'Zone Pickups',
      //   link: { name: 'ZonePickups' },
      //   roles: ['ZONE_SUPERVISOR'],
      //   iconColor: '#0ea5e9',
      // },

      // === ADMIN PORTAL (NON-EUDR - COMMENTED OUT) ===
      // {
      //   icon: 'mdi-file-document-check',
      //   text: 'License Review',
      //   link: { name: 'LicenseReview' },
      //   roles: ['ADMIN'],
      //   iconColor: '#f59e0b',
      // },
      // {
      //   icon: 'mdi-account-group',
      //   text: 'Users Report',
      //   link: { name: 'UsersReport' },
      //   roles: ['ADMIN'],
      //   iconColor: '#6366f1',
      // },
      // {
      //   icon: 'mdi-file-document-outline',
      //   text: 'Orders Report',
      //   link: { name: 'OrdersReport' },
      //   roles: ['ADMIN'],
      //   iconColor: '#0ea5e9',
      // },
      // {
      //   icon: 'mdi-chart-line',
      //   text: 'System Reports',
      //   link: { name: 'SystemReports' },
      //   roles: ['ADMIN'],
      //   iconColor: '#8b5cf6',
      // },
      // {
      //   icon: 'mdi-cog',
      //   text: 'System Settings',
      //   link: { name: 'SystemSettings' },
      //   roles: ['ADMIN'],
      //   iconColor: '#64748b',
      // },

      // === COMMON: Settings for all users (except FARMER for EUDR focus) ===
      // {
      //   icon: 'mdi-cog-outline',
      //   text: 'Settings',
      //   link: { name: 'Settings' },
      //   roles: ['BUYER', 'EXPORTER', 'SYSTEM_ADMIN', 'ZONE_SUPERVISOR', 'ADMIN', 'AGGREGATOR', 'PROCESSOR', 'IMPORTER'],
      //   iconColor: '#64748b',
      // },
    ],
  }),
  computed: {
    displayName() {
      const token = this.$store.state.auth.token;
      if (!token) return '';
      try {
        const decoded = jwtDecode(token);
        const user = decoded.user || {};
        return user.fullName || [user.firstName, user.lastName].filter(Boolean).join(' ') || user.username || '';
      } catch (e) { return ''; }
    },
    userRole() {
      const token = this.$store.state.auth.token;
      if (!token) return null;
      try {
        const decoded = jwtDecode(token);
        return decoded.role || decoded.user?.role || null;
      } catch (error) { return null; }
    },
  },
  methods: {
    canView(item) {
      // Check EUDR navigation setting
      if (item.text && item.text.includes('EUDR') && !this.isEudrEnabled()) {
        return false;
      }

      return !item.roles || item.roles.length === 0
        ? true
        : (this.userRole && item.roles.includes(this.userRole));
    },
    isEudrEnabled() {
      return localStorage.getItem('eudrEnabled') !== 'false';
    },
    toggleSection(sectionName) {
      const index = this.expandedSections.indexOf(sectionName);
      if (index > -1) {
        this.expandedSections.splice(index, 1);
      } else {
        this.expandedSections.push(sectionName);
      }
    },
    isSectionExpanded(sectionName) {
      return this.expandedSections.includes(sectionName);
    },
  },
};
</script>

<style scoped>
.app-drawer-wrap {
  position: relative;
  height: 100vh;
  min-width: 300px;
  max-width: 360px;
  width: 100%;
  display: flex;
  flex-direction: column;
  z-index: 20;
  right: 0;
  box-shadow: -4px 0 32px rgba(46, 125, 50, 0.12);
  background: linear-gradient(135deg, rgba(247, 245, 238, 0.95), rgba(230, 244, 234, 0.95));
}
.app-drawer-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}
.app-drawer-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(48px);
  opacity: 0.15;
  animation: app-drawer-float 18s ease-in-out infinite;
}
.app-drawer-shape.shape-1 {
  width: 180px; height: 180px; top: -40px; right: -40px;
  background: radial-gradient(circle at 70% 30%, #bbf7d0 0%, #86efac 80%, transparent 100%);
  animation-delay: 0s;
}
.app-drawer-shape.shape-2 {
  width: 120px; height: 120px; bottom: 10%; right: 10%;
  background: radial-gradient(circle at 80% 80%, #dcfce7 0%, #bbf7d0 80%, transparent 100%);
  animation-delay: 2.5s;
}
@keyframes app-drawer-float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(18px) scale(1.03); }
}
.app-drawer-glass {
  position: relative;
  z-index: 1;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: rgba(255,255,255,0.92);
  border-left: 2px solid rgba(230, 225, 195, 0.4);
  box-shadow: -4px 0 32px rgba(46, 125, 50, 0.08);
  backdrop-filter: blur(16px) saturate(160%);
  border-top-right-radius: 24px;
  overflow: hidden;
  border-image: linear-gradient(135deg, rgba(46, 125, 50, 0.2), rgba(76, 175, 80, 0.2)) 1;
}
.app-drawer-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
  padding: 24px 18px 12px 18px;
  border-bottom: 2px solid rgba(230, 225, 195, 0.4);
  background: rgba(255,255,255,0.98);
  z-index: 2;
  backdrop-filter: blur(8px);
  border-top-right-radius: 24px;
}
.app-brand-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.app-brand {
  margin-bottom: 2px;
}
.app-portal-badge {
  background: linear-gradient(135deg, #2e7d32, #4caf50);
  color: #fff;
  font-size: 0.85rem;
  font-weight: 700;
  border-radius: 12px;
  padding: 4px 12px;
  margin-left: 4px;
  letter-spacing: 0.02em;
  box-shadow: 0 2px 8px rgba(46, 125, 50, 0.2);
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
}
.app-user-mini {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(247, 245, 238, 0.9));
  border: 2px solid rgba(230, 225, 195, 0.4);
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.08);
  backdrop-filter: blur(8px);
}
.app-user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.app-user-name {
  font-weight: 800;
  font-size: 1.05rem;
  color: #2e7d32;
  text-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.app-role-chip {
  display: inline-flex;
  align-items: center;
  font-size: 0.82rem;
  color: #4caf50;
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.1), rgba(46, 125, 50, 0.1));
  border: 2px solid rgba(76, 175, 80, 0.2);
  padding: 4px 12px;
  border-radius: 999px;
  font-weight: 700;
  letter-spacing: 0.01em;
  box-shadow: 0 2px 6px rgba(46, 125, 50, 0.08);
}
.app-nav-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 0 0 0;
  flex: 1 1 auto;
  overflow-y: auto;
}
.app-nav-section {
  display: flex;
  flex-direction: column;
  margin: 0 8px;
}
.app-nav-section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 6px;
  color: #2e7d32;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  background: rgba(255,255,255,0.8);
  backdrop-filter: blur(4px);
  text-shadow: 0 1px 2px rgba(0,0,0,0.02);
  letter-spacing: 0.01em;
  user-select: none;
}
.app-nav-section-header:hover {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.15), rgba(46, 125, 50, 0.15));
  color: #2e7d32;
  border-color: rgba(76, 175, 80, 0.3);
  box-shadow: 0 6px 20px rgba(46, 125, 50, 0.12);
}
.app-nav-expand-icon {
  margin-left: auto;
  transition: transform 0.3s ease;
}
.app-nav-expanded {
  transform: rotate(180deg);
}
.app-nav-children {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 4px 0;
  margin-left: 12px;
  border-left: 2px solid rgba(76, 175, 80, 0.2);
  padding-left: 8px;
}
.app-nav-child {
  font-size: 0.95rem !important;
  padding: 5px 10px !important;
}
.app-nav-expand-enter-active,
.app-nav-expand-leave-active {
  transition: all 0.3s ease;
  max-height: 500px;
  overflow: hidden;
}
.app-nav-expand-enter-from,
.app-nav-expand-leave-to {
  max-height: 0;
  opacity: 0;
}
.app-nav-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px 6px 12px;
  border-radius: 6px;
  color: #2e7d32;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.3s ease;
  margin: 0 8px;
  border: 2px solid transparent;
  background: rgba(255,255,255,0.6);
  backdrop-filter: blur(4px);
  text-shadow: 0 1px 2px rgba(0,0,0,0.02);
  letter-spacing: 0.01em;
}
.app-nav-item:hover {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.1), rgba(46, 125, 50, 0.1));
  color: #2e7d32;
  border-color: rgba(76, 175, 80, 0.3);
  /* transform: translateX(4px); */
  box-shadow: 0 6px 20px rgba(46, 125, 50, 0.12);
}
.app-nav-active {
  background: linear-gradient(135deg, rgba(46, 125, 50, 0.15), rgba(76, 175, 80, 0.15)) !important;
  color: #2e7d32 !important;
  font-weight: 800 !important;
  border-color: rgba(46, 125, 50, 0.4) !important;
  box-shadow: 0 8px 25px rgba(46, 125, 50, 0.15) !important;
  /* transform: translateX(6px); */
}
.app-nav-icon {
  display: flex;
  align-items: center;
  /* font-size: 1.4em; */
  color: #4caf50;
  text-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.app-nav-item:hover .app-nav-icon {
  color: #2e7d32;
  transform: scale(1.1);
}
.app-nav-active .app-nav-icon {
  color: #2e7d32 !important;
}
.app-nav-text {
  font-size: 1.02rem;
  font-weight: light;
  letter-spacing: 0.01em;
  line-height: 1.3;
}
.app-feedback-section {
  padding: 20px 18px 20px 18px;
  margin-top: auto;
  border-top: 2px solid rgba(230, 225, 195, 0.4);
  background: linear-gradient(135deg, rgba(255,255,255,0.98), rgba(247, 245, 238, 0.98));
  box-shadow: 0 -4px 16px rgba(46, 125, 50, 0.06);
  backdrop-filter: blur(8px);
}
.app-feedback-btn {
  font-weight: 700;
  font-size: 1.02rem;
  letter-spacing: 0.01em;
  border-radius: 14px;
  padding: 12px 0;
  text-transform: none;
  background: linear-gradient(135deg, #2e7d32, #4caf50) !important;
  color: white !important;
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.2);
  text-shadow: 0 1px 2px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
}
.app-feedback-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(46, 125, 50, 0.3) !important;
  background: linear-gradient(135deg, #4caf50, #2e7d32) !important;
}
@media (max-width: 1200px) {
  .app-drawer-wrap { min-width: 220px; }
}
@media (max-width: 960px) {
  .app-drawer-wrap { min-width: 0; max-width: 100vw; }
  .app-nav-text { font-size: 0.98rem; }
  .app-nav-item { min-height: 42px; padding: 12px 20px; }
}
</style>
