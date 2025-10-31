<template>
  <v-app id="agri-future" class="app-background">
    <!-- Navigation Drawer -->
    <v-navigation-drawer
      class="futuristic-drawer"
      v-model="drawer"
  app
  :temporary="isMobile"
    >
  <drawer @openFeedback="handleOpenFeedback" />
    </v-navigation-drawer>

    <!-- App Bar -->
    <v-app-bar app flat color="transparent" class="glass-navbar" :elevate-on-scroll="true">
      <div class="bar-left">
        <v-app-bar-nav-icon class="tw-block" @click="toggleNav">
          <v-icon>mdi-menu</v-icon>
        </v-app-bar-nav-icon>
        <avatar class="ml-2" ref="avatar" />
        <v-toolbar-title class="brand-title ml-2" @click="$router.push({ name: 'Dashboard' })">
          <span class="brand-dot" />
          <span class="brand-text">Agri Portal</span>
        </v-toolbar-title>
      </div>

      <div class="bar-center" v-if="!isMobile">
        <v-text-field
          v-model="quickQuery"
          class="quick-search"
          dense
          rounded
          solo-inverted
          hide-details
          clearable
          prepend-inner-icon="mdi-magnify"
          placeholder="Search farmers, listings, orders…"
        />
      </div>

      <div class="bar-right">
        <v-btn icon class="mr-1" v-if="isMobile" @click="openSearch"><v-icon>mdi-magnify</v-icon></v-btn>
        <hedera-wallet-connect class="mr-2" />
        <notification-bell class="mr-1" />
        <v-btn icon class="mr-1" @click="$router.push({ name: 'Settings' })" title="Settings">
          <v-icon>mdi-cog</v-icon>
        </v-btn>
        <div v-if="!isAuthenticated()" class="ml-2">
          <v-btn to="/signin" rounded class="btn-glass-outline">
            <span class="mr-1">Login</span>
            <v-icon small>mdi-login</v-icon>
          </v-btn>
        </div>
      </div>
    </v-app-bar>

    <!-- Main Content -->
    <v-main :class="['futuristic-main', { 'has-bottom-nav': isMobile }]">
      <v-container class="content-container" fluid>
        <transition name="fade" mode="out-in">
          <div>
            <slot />
          </div>
        </transition>
      </v-container>
    </v-main>

    <!-- Bottom Navigation (Mobile Only) -->
    <bottom-nav v-if="isMobile" v-show="!drawer" />

    <!-- AI Assistant Dialog -->
    <ai-assistant ref="assistant" />

    <!-- Feedback Dialog -->
    <feedback-dialog ref="feedbackDialog" />

    <!-- Screen Idle Component -->
    <screen-idle :dialog="isAppIdle" />
  </v-app>
</template>

<script>
import { mapGetters } from 'vuex';
import Drawer from '@/components/layout/partials/Drawer.vue';
import Avatar from '@/components/layout/partials/nav/Avatar.vue';
import ScreenIdle from '@/components/shared/ScreenIdle.vue';
import { isAuthenticated } from '@/utils/roles.js';
import NotificationBell from '@/components/layout/partials/nav/NotificationBell.vue';
import AiAssistant from '@/components/ai/AiAssistant.vue';
// Import the new BottomNav component
import BottomNav from '@/components/layout/partials/BottomNav.vue';
import FeedbackDialog from '@/components/shared/FeedbackDialog.vue';
import HederaWalletConnect from '@/components/shared/HederaWalletConnect.vue';

export default {
  name: 'FuturisticLayout',
  components: {
    ScreenIdle,
    Avatar,
    Drawer,
    NotificationBell,
    AiAssistant,
    BottomNav,
    FeedbackDialog,
    HederaWalletConnect,
  },
  data: () => ({
    drawer: false,
    isAppIdle: false,
    quickQuery: '',
  }),
  computed: {
    ...mapGetters('auth', ['authenticatedUser']),
    // ...mapGetters('farm', ['currentWeather']),
    isMobile() {
      return this.$vuetify.breakpoint.smAndDown;
    },
  },
  methods: {
    isAuthenticated,
    activateAI() {
      this.$refs.assistant.openDialog();
    },
    toggleNav() {
      this.drawer = !this.drawer;
    },
    openSearch() {
      // Placeholder for a future command palette
      this.$toast.info('Search coming soon');
    },
    handleOpenFeedback() {
      // Open the feedback dialog
      this.$refs.feedbackDialog.openDialog();
    },
  },
  mounted() {
    // Desktop: open drawer in mini, Mobile: keep closed until toggled
    if (!this.isMobile) {
      this.drawer = true;
    }
  },
  watch: {
    // When switching breakpoints, normalize drawer state
    isMobile(val) {
      if (val) {
        this.drawer = false;
      } else {
        this.drawer = true;
      }
    },
    // Auto-close temporary drawer on route change (mobile)
    $route() {
      if (this.isMobile && this.drawer) this.drawer = false;
    },
  },
};
</script>

<style scoped>
/* App Background */
.app-background {
  background:
    radial-gradient(1200px 600px at 10% -10%, #ebfff3 0%, rgba(235,255,243,0) 60%),
    radial-gradient(1000px 600px at 110% 10%, #e9f3ff 0%, rgba(233,243,255,0) 60%),
    linear-gradient(135deg, #f8fbff 0%, #ffffff 100%);
}

/* Glass App Bar */
.glass-navbar {
  background: rgba(255, 255, 255, 0.78) !important;
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.brand-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 800;
  color: #0f172a;
}
.brand-dot { width: 10px; height: 10px; background: #22c55e; border-radius: 3px; box-shadow: 0 6px 14px rgba(34,197,94,0.35); }
.brand-text { letter-spacing: .2px; }

.btn-glass-outline { background: rgba(255,255,255,0.6) !important; border: 1px solid rgba(15,23,42,0.08) !important; }

/* App Bar layout */
.bar-left, .bar-right { display: inline-flex; align-items: center; }
.bar-left { gap: 8px; }
.bar-center { flex: 1 1 auto; max-width: 720px; margin: 0 16px; }
.quick-search { min-width: 380px; }

/* Drawer */
.futuristic-drawer {
  background: rgba(255, 255, 255, 0.92) !important;
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
  transition: all 0.25s ease;
  border-right: 1px solid rgba(15,23,42,0.06);
}

/* Main */
.futuristic-main { min-height: 100vh; }
.content-container { padding: 20px; max-width: 1400px; margin: 0 auto; }
.has-bottom-nav .content-container { padding-bottom: 84px; }

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter, .fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* Responsive tweaks */
@media (max-width: 960px) {
  .content-container { padding: 16px; }
}
</style>
