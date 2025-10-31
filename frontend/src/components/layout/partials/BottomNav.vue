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
      // Only show a subset of main navigation items for mobile
      const items = [
        {
          icon: 'mdi-view-dashboard',
          text: 'Dashboard',
          short: 'Home',
          link: { name: 'Dashboard' },
          roles: ['buyer', 'admin'],
        },
        {
          icon: 'mdi-account-group',
          text: 'Community',
          short: 'Comm',
          link: { name: 'Community' },
          roles: ['farmer', 'buyer', 'anybody'],
        },
        {
          icon: 'mdi-cash-multiple',
          text: 'My Sales',
          short: 'Sales',
          link: { name: 'Listings' },
          roles: ['farmer'],
        },
        {
          icon: 'mdi-sprout',
          text: 'My Farm',
          short: 'Farm',
          link: { name: 'Produces', params: { farmerId: getCurrentUserId() } },
          roles: ['farmer'],
        },
      ];
      // If farmer, ensure Orders/Browse Requests aren't present
      const role = getCurrentUserRole && getCurrentUserRole();
      if (role === 'FARMER') {
        return items.filter((i) => !['My Orders', 'Browse Requests'].includes(i.text));
      }
      return items;
    },
    filteredNavItems() {
      // Filter nav items based on permissions
      return this.navItems.filter((item) => this.viewPermissions(item.roles));
    },
  },
};
</script>

<style scoped>
.billion-bottom-nav {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  height: 68px;
  z-index: 100;
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
