<!-- eslint-disable vue/no-use-v-if-with-v-for -->
<template>
  <div class="modern-drawer">
    <!-- Header Section -->
    <div class="drawer-header">
      <div class="brand-section">
        <logo-title class="brand-logo" />
        <!-- <span class="portal-tag">Portal</span> -->
      </div>

      <!-- User Info -->
      <div v-if="displayName || userRole" class="user-card">
        <div class="user-avatar">
          <v-icon color="white" size="20">mdi-account</v-icon>
        </div>
        <div class="user-details">
          <!-- <span class="user-name">{{ displayName || 'User' }}</span> -->
          <span class="user-role">{{ userRole || 'Guest' }}</span>
        </div>
      </div>

      <role-indicator v-if="false" />
    </div>

    <!-- Navigation Section -->
    <nav class="drawer-nav">
      <div class="nav-scroll">
        <template v-for="(item, i) in navigationItems">
          <!-- Section with children -->
          <div v-if="item.isSection && canView(item)" :key="`section-${i}`" class="nav-group">
            <button
              class="nav-group-header"
              @click="toggleSection(item.text)"
            >
              <v-icon size="20" :color="item.iconColor || '#2e7d32'">{{ item.icon }}</v-icon>
              <span class="nav-label">{{ item.text }}</span>
              <v-icon
                size="18"
                class="expand-icon"
                :class="{ 'expanded': isSectionExpanded(item.text) }"
              >
                mdi-chevron-down
              </v-icon>
            </button>
            <transition name="expand">
              <div v-show="isSectionExpanded(item.text)" class="nav-children">
                <router-link
                  v-for="(child, j) in item.children"
                  v-if="canView(child)"
                  :key="`${i}-${j}`"
                  :to="child.link"
                  class="nav-link nav-child"
                  active-class="nav-active"
                  exact
                >
                  <v-icon size="18" :color="child.iconColor || '#666'">{{ child.icon }}</v-icon>
                  <span>{{ child.text }}</span>
                </router-link>
              </div>
            </transition>
          </div>

          <!-- Regular navigation item -->
          <router-link
            v-else-if="!item.isSection && canView(item)"
            :key="`item-${i}`"
            :to="item.link"
            class="nav-link"
            active-class="nav-active"
            exact
          >
            <v-icon size="20" :color="item.iconColor || '#2e7d32'">{{ item.icon }}</v-icon>
            <span class="nav-label">{{ item.text }}</span>
          </router-link>
        </template>
      </div>
    </nav>

    <!-- Footer Section -->
    <div class="drawer-footer">
      <div class="footer-info">
        <v-icon size="16" color="#94a3b8">mdi-shield-check</v-icon>
        <span>EUDR Compliant</span>
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
import { getCurrentUserId } from '@/utils/roles';

export default {
  name: 'AppDrawer',
  components: {
    // eslint-disable-next-line vue/no-unused-components
    LogoTitle, Avatar, RoleIndicator, getCurrentUserId,
  },
  data: () => ({
    // expandedSections: ['EUDR Compliance'],
    navigationItems: [
      // Common: Dashboard for all roles
      {
        icon: 'mdi-view-dashboard',
        text: 'Dashboard',
        link: { name: 'Dashboard' },
        roles: ['FARMER', 'BUYER', 'EXPORTER', 'SYSTEM_ADMIN', 'ZONE_SUPERVISOR', 'AGGREGATOR'],
        iconColor: '#2e7d32',
      },
      // === FARMER EUDR SECTION ===
      {
        icon: 'mdi-map-marker-radius',
        text: 'My Production Units',
        link: { name: 'FarmerProductionUnits' },
        roles: ['FARMER'],
        iconColor: '#16a34a',
      },

      // === FARMER PORTAL (NON-EUDR - COMMENTED OUT) ===
      {
        icon: 'mdi-barn',
        text: 'My Produces',
        get link() {
          const id = getCurrentUserId();
          return id ? { name: 'MyFarm', params: { farmerId: id } } : { name: 'MyFarm' };
        },
        roles: ['FARMER'],
        iconColor: '#16a34a',
      },
      {
        icon: 'mdi-format-list-bulleted-square',
        text: 'Listings',
        link: { name: 'Listings' },
        roles: ['FARMER'],
        iconColor: '#6366f1',
      },
      {
        icon: 'mdi-chart-line',
        text: 'Reports',
        link: { name: 'FarmerReports' },
        roles: ['FARMER'],
        iconColor: '#8b5cf6',
      },

      // === AGGREGATOR EUDR SECTION ===
      {
        icon: 'mdi-basket-plus',
        text: 'Record Collection',
        link: { name: 'AggregatorCollection' },
        roles: ['AGGREGATOR'],
        iconColor: '#16a34a',
      },
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

      // === EXPORTER PORTAL (EUDR Management) ===
      // {
      //   icon: 'mdi-shield-check',
      //   text: 'EUDR Compliance',
      //   isSection: true,
      //   roles: ['EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR'],
      //   iconColor: '#dc2626',
      //   children: [
      {
        icon: 'mdi-transit-connection-variant',
        text: 'Traceability Workflow',
        link: { name: 'SupplyChainWorkflow' },
        roles: ['EXPORTER', 'SYSTEM_ADMIN'],
        iconColor: '#10b981',
      },
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
        icon: 'mdi-certificate',
        text: 'Certificate Viewer',
        link: { name: 'CertificateViewer' },
        roles: ['EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR', 'IMPORTER'],
        iconColor: '#3b82f6',
      },
      {
        icon: 'mdi-file-document-outline',
        text: 'Compliance Reporting',
        link: { name: 'ComplianceReporting' },
        roles: ['EXPORTER', 'SYSTEM_ADMIN', 'VERIFIER', 'AUDITOR'],
        iconColor: '#10b981',
      },
      //   ],
      // },
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
        text: 'Suppliers',
        link: { name: 'SupplierManagement' },
        roles: ['EXPORTER', 'SYSTEM_ADMIN'],
        iconColor: '#2563eb',
      },
      // {
      //   icon: 'mdi-truck-delivery',
      //   text: 'Aggregators',
      //   link: { name: 'AggregatorsManagement' },
      //   roles: ['EXPORTER', 'SYSTEM_ADMIN'],
      //   iconColor: '#2563eb',
      // },
      // {
      //   icon: 'mdi-factory',
      //   text: 'Processors',
      //   link: { name: 'ProcessorsManagement' },
      //   roles: ['EXPORTER', 'SYSTEM_ADMIN'],
      //   iconColor: '#7c3aed',
      // },
      {
        icon: 'mdi-ship-wheel',
        text: 'Importers',
        link: { name: 'ImportersManagement' },
        roles: ['EXPORTER', 'SYSTEM_ADMIN'],
        iconColor: '#0891b2',
      },
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
/* Main Drawer Container */
.modern-drawer {
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 280px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  border-right: 1px solid #e2e8f0;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
}

/* Header Section */
.drawer-header {
  padding: 24px 20px 20px;
  border-bottom: 1px solid #e2e8f0;
  background: white;
}

.brand-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.brand-logo {
  flex: 1;
}

.portal-tag {
  background: linear-gradient(135deg, #2e7d32, #4caf50);
  color: white;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 4px 10px;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(46, 125, 50, 0.2);
}

/* User Card */
.user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #f8fafc, #f1f5f9);
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  margin-bottom: 12px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #2e7d32, #4caf50);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  font-weight: 500;
  color: #2e7d32;
  background: #dcfce7;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
  width: fit-content;
}

/* Navigation Section */
.drawer-nav {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.nav-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

/* Navigation Groups */
.nav-group {
  margin-bottom: 8px;
}

.nav-group-header {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.nav-group-header:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.expand-icon {
  margin-left: auto;
  transition: transform 0.2s ease;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

/* Navigation Links */
.nav-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  margin-bottom: 4px;
  border-radius: 8px;
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  transition: all 0.2s ease;
  border: 1px solid transparent;
  background: white;
}

.nav-link:hover {
  background: #f1f5f9;
  color: #1e293b;
  border-color: #e2e8f0;
}

.nav-link.nav-active {
  background: linear-gradient(135deg, #dcfce7, #bbf7d0);
  color: #166534;
  font-weight: 600;
  border-color: #86efac;
  box-shadow: 0 1px 3px rgba(22, 101, 52, 0.1);
}

.nav-label {
  flex: 1;
}

/* Navigation Children */
.nav-children {
  padding: 8px 0 8px 16px;
  border-left: 2px solid #e2e8f0;
  margin-left: 28px;
  margin-top: 4px;
}

.nav-child {
  font-size: 13px;
  padding: 8px 12px;
}

/* Expand Animation */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  max-height: 500px;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  max-height: 0;
  opacity: 0;
}

/* Footer Section */
.drawer-footer {
  padding: 16px 20px;
  border-top: 1px solid #e2e8f0;
  background: white;
}

.footer-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

/* Scrollbar Styling */
.nav-scroll::-webkit-scrollbar {
  width: 6px;
}

.nav-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.nav-scroll::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.nav-scroll::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Responsive */
@media (max-width: 960px) {
  .modern-drawer {
    width: 240px;
  }

  .user-name {
    font-size: 13px;
  }

  .nav-link,
  .nav-group-header {
    font-size: 13px;
    padding: 8px 10px;
  }
}
</style>
