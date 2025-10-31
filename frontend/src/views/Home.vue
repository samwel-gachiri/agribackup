<template>
  <div class="tw-bg-gradient-to-br tw-from-[#f7f5ee] tw-to-[#e6f4ea] tw-min-h-screen tw-flex tw-flex-col tw-relative">
    <!-- Responsive Glassmorphism Navbar -->
    <nav class="tw-fixed tw-top-0 tw-left-0 tw-w-full tw-z-50 tw-bg-[#f7f5ee] tw-bg-opacity-80 tw-backdrop-blur-lg tw-shadow-lg tw-border-b tw-border-[#e6e1c3]">
      <div class="tw-max-w-7xl tw-mx-auto tw-flex tw-items-center tw-justify-center tw-px-6 tw-py-4">
        <div class="tw-flex tw-items-center">
          <!-- Creative logo: AGRIBackUp with SVG leaf accent -->
          <span class="tw-font-extrabold tw-text-green-800 tw-text-3xl md:tw-text-4xl tw-tracking-tight tw-select-none tw-drop-shadow-sm tw-flex tw-items-center">
            Agri
            <span class="tw-text-green-900 tw-ml-1">
              Backup
            </span>
          </span>
        </div>
      </div>
    </nav>

    <!-- Spacer for navbar height -->
    <div class="tw-h-24"></div>

    <!-- Cozy Curved SVG Line Background with agricultural colors -->
    <svg
      class="tw-absolute tw-left-0 tw-w-full tw-h-56 tw-z-0 tw-pointer-events-none"
      style="top: 90px; min-width:100vw;"
      viewBox="0 0 1440 224"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <path
        d="M0,112 Q720,224 1440,112"
        stroke="#b6a16b"
        stroke-width="12"
        fill="none"
        opacity="0.18"
      />
      <ellipse cx="200" cy="60" rx="120" ry="32" fill="#e6e1c3" opacity="0.18"/>
      <ellipse cx="1240" cy="180" rx="80" ry="24" fill="#b6e4a3" opacity="0.18"/>
    </svg>

    <main class="home-root">
      <div class="home-shell">
        <header class="brand">
          <img src="@/assets/images/logo.png" alt="AgriBackup" class="brand-logo" />
          <!-- <div class="brand-content">
            <h1 class="brand-title">Welcome to AgriBackup</h1>
            <p class="brand-sub">EUDR-Compliant Supply Chain Platform</p>
          </div> -->
          <p class="brand-description">Choose your role to access features</p>
        </header>

        <section v-if="!showExporterRoles" class="portals" :class="{ 'fade-out': showExporterRoles }">
          <button class="tile" @click="openSignIn('farmer')" aria-label="Farmer portal">
            <div class="tile-icon"><v-icon size="24">mdi-barn</v-icon></div>
            <div class="tile-body">
              <div class="tile-title">Farmer</div>
              <div class="tile-sub">Manage crops & record yields</div>
            </div>
            <v-icon class="tile-arrow">mdi-arrow-right</v-icon>
          </button>

          <!-- <button class="tile" @click="openSignIn('buyer')" aria-label="Buyer portal">
            <div class="tile-icon"><v-icon size="24">mdi-cart</v-icon></div>
            <div class="tile-body">
              <div class="tile-title">Buyer</div>
              <div class="tile-sub">Connect with farmers & buy produce</div>
            </div>
            <v-icon class="tile-arrow">mdi-arrow-right</v-icon>
          </button> -->

          <button class="tile" @click="openSignIn('aggregator')" aria-label="Aggregator portal">
            <div class="tile-icon"><v-icon size="24">mdi-truck-delivery</v-icon></div>
            <div class="tile-body">
              <div class="tile-title">Aggregator</div>
              <div class="tile-sub">Collect & coordinate produce logistics</div>
            </div>
            <v-icon class="tile-arrow">mdi-arrow-right</v-icon>
          </button>

          <button class="tile" @click="openSignIn('processor')" aria-label="Processor portal">
            <div class="tile-icon"><v-icon size="24">mdi-factory</v-icon></div>
            <div class="tile-body">
              <div class="tile-title">Processor</div>
              <div class="tile-sub">Process & prepare for export</div>
            </div>
            <v-icon class="tile-arrow">mdi-arrow-right</v-icon>
          </button>
          <button class="tile" @click="showExporterSelection" aria-label="Exporter portal">
            <div class="tile-icon"><v-icon size="24">mdi-export</v-icon></div>
            <div class="tile-body">
              <div class="tile-title">Exporter</div>
              <div class="tile-sub">Manage zones & logistics</div>
            </div>
            <v-icon class="tile-arrow">mdi-arrow-right</v-icon>
          </button>

          <button class="tile" @click="openSignIn('importer')" aria-label="Importer portal">
            <div class="tile-icon"><v-icon size="24">mdi-ship-wheel</v-icon></div>
            <div class="tile-body">
              <div class="tile-title">Importer</div>
              <div class="tile-sub">Import & distribute products</div>
            </div>
            <v-icon class="tile-arrow">mdi-arrow-right</v-icon>
          </button>

        </section>

        <!-- Exporter Roles Section -->
        <section class="portals exporter-roles" :class="{ 'fade-in': showExporterRoles }">
          <div class="roles-header">
            <button class="back-button" @click="hideExporterSelection" aria-label="Go back">
              <v-icon size="20">mdi-arrow-left</v-icon>
              Back to Roles
            </button>
            <h3 class="roles-title">Choose Exporter Role</h3>
          </div>

          <button class="tile role-tile" @click="openSignIn('exporter')" aria-label="Super Admin">
            <div class="tile-icon"><v-icon size="24">mdi-shield-account</v-icon></div>
            <div class="tile-body">
              <div class="tile-title">Super Admin</div>
              <div class="tile-sub">Full platform administration</div>
            </div>
            <v-icon class="tile-arrow">mdi-arrow-right</v-icon>
          </button>

          <button class="tile role-tile" @click="openSignIn('system_admin')" aria-label="System Admin">
            <div class="tile-icon"><v-icon size="24">mdi-cog</v-icon></div>
            <div class="tile-body">
              <div class="tile-title">System Admin</div>
              <div class="tile-sub">System management & oversight</div>
            </div>
            <v-icon class="tile-arrow">mdi-arrow-right</v-icon>
          </button>

          <button class="tile role-tile" @click="openSignIn('zone_supervisor')" aria-label="Zone Supervisor">
            <div class="tile-icon"><v-icon size="24">mdi-map-marker-radius</v-icon></div>
            <div class="tile-body">
              <div class="tile-title">Zone Supervisor</div>
              <div class="tile-sub">Regional zone management</div>
            </div>
            <v-icon class="tile-arrow">mdi-arrow-right</v-icon>
          </button>
        </section>

        <footer class="home-foot muted">© AgriBackup · Privacy · Terms</footer>
      </div>
    </main>
  </div>
</template>

<script>
import { getCurrentUserRole } from '@/utils/roles.js';

export default {
  data() {
    return {
      showExporterRoles: false,
    };
  },
  methods: {
    clearViewRole() {
      this.$store.dispatch('auth/setViewRole', '');
    },
    showExporterSelection() {
      this.showExporterRoles = true;
    },
    hideExporterSelection() {
      this.showExporterRoles = false;
    },
    async openSignIn(name) {
      const userName = String(name).toLowerCase();

      // Determine portal and role context
      let portal = userName;
      let role = '';

      // Map EUDR roles - these should use their own portal context
      const eudrRoles = ['aggregator', 'processor', 'importer'];
      if (eudrRoles.includes(userName)) {
        portal = userName; // Keep portal as the role name
        role = userName.toUpperCase(); // Set role to uppercase
      } else if (userName !== 'farmer' && userName !== 'buyer' && userName !== 'exporter') {
        // Exporter sub-roles like 'system_admin'
        role = userName;
        portal = 'exporter';
      }

      // If switching roles/portals, sign out current user to avoid leaking session
      if (getCurrentUserRole() !== userName) {
        await this.$store.dispatch('auth/signOut');
      }

      // Persist portal context in the auth store so SignIn/SignUp can pick it up
      await this.$store.dispatch('auth/setPortalContext', { portal, role });

      // Keep existing behavior of setting view role for UI toggles
      await this.$store.dispatch('auth/setViewRole', userName);

      // Check for redirect query param and decode if present
      let redirectPath = this.$route.query.redirect;
      if (redirectPath) {
        try {
          redirectPath = atob(redirectPath);
        } catch (e) {
          // fallback: use as is if not base64
        }
        await this.$router.push(redirectPath);
      } else {
        const defaultRedirect = this.$router.resolve({ name: 'Dashboard' }).route.fullPath;
        await this.$router.push({
          name: 'SignIn',
          query: {
            mode: 'self',
            redirect: defaultRedirect,
          },
        });
      }
    },
  },
  computed: {
    getCurrentUserRole,
  },
};
</script>

<style scoped>
/* Glassmorphism effect for navbar */
nav {
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.08);
}

/* Enhanced Home view with beautiful green styling */
.home-root {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 36px;
  position: relative;
  z-index: 1;
}

.home-shell {
  width: 100%;
  max-width: 980px;
  background: rgba(255,255,255,0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(10,20,30,0.1);
  display: flex;
  flex-direction: column;
  gap: 32px;
  border: 2px solid rgba(230, 225, 195, 0.3);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.brand {
  display: flex;
  align-items: center;
  gap: 24px;
  text-align: center;
  justify-content: center;
  flex-direction: column;
  margin-bottom: 16px;
}

.brand-content {
  max-width: 600px;
}

.brand-logo {
  width: 72px;
  height: 72px;
  border-radius: 16px;
  object-fit: contain;
  border: 3px solid #e6e1c3;
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.15);
}

.brand-title {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 800;
  color: #2e7d32;
  text-shadow: 0 2px 4px rgba(0,0,0,0.1);
  line-height: 1.2;
}

.brand-sub {
  margin: 0 0 12px 0;
  color: #4caf50;
  font-size: 16px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0,0,0,0.05);
}

.brand-description {
  margin: 0;
  color: #6b4f2c;
  font-size: 15px;
  line-height: 1.5;
  font-weight: 400;
  opacity: 0.9;
}

.portals {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  transition: all 0.5s ease;
}

.portals.fade-out {
  opacity: 0;
  transform: translateY(-20px);
  pointer-events: none;
}

.exporter-roles {
  opacity: 0;
  transform: translateY(20px);
  pointer-events: none;
}

.exporter-roles.fade-in {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.roles-header {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  position: relative;
}

.back-button {
  position: absolute;
  left: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255,255,255,0.9);
  border: 2px solid rgba(230, 225, 195, 0.4);
  color: #2e7d32;
  padding: 8px 16px;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.back-button:hover {
  background: rgba(46, 125, 50, 0.1);
  border-color: #2e7d32;
  transform: translateX(-2px);
}

.roles-title {
  color: #2e7d32;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  text-align: center;
}

.role-tile {
  background: linear-gradient(135deg, rgba(255,255,255,0.95), rgba(247, 245, 238, 0.95));
  border: 2px solid rgba(230, 225, 195, 0.4);
}

.role-tile:hover {
  border-color: #2e7d32;
  background: linear-gradient(135deg, rgba(255,255,255,0.98), rgba(230, 244, 234, 0.95));
}

.role-tile .tile-icon {
  background: linear-gradient(135deg, #ff9800, #f57c00);
}

.role-tile:nth-child(2) .tile-icon {
  background: linear-gradient(135deg, #2196f3, #1976d2);
}

.role-tile:nth-child(3) .tile-icon {
  background: linear-gradient(135deg, #9c27b0, #7b1fa2);
}

.home-foot {
  text-align: center;
  color: #6b4f2c;
  font-size: 14px;
  margin-top: 16px;
  font-weight: 500;
}

.muted {
  color: #6b4f2c;
}

.roles-card {
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(10,20,30,0.15);
  border: 1px solid rgba(230, 225, 195, 0.3);
}

.roles-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role {
  text-transform: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #2e7d32, #4caf50);
  color: white;
  font-weight: 600;
  padding: 12px;
  transition: all 0.3s ease;
}

.role:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(46, 125, 50, 0.3);
}

.tile {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(247, 245, 238, 0.9));
  border: 2px solid rgba(230, 225, 195, 0.4);
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 25px rgba(46, 125, 50, 0.08);
}

.tile:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 40px rgba(46, 125, 50, 0.15);
  border-color: #2e7d32;
  background: linear-gradient(135deg, rgba(255,255,255,0.95), rgba(230, 244, 234, 0.95));
}

.tile-icon {
  width: 52px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2e7d32, #4caf50);
  border-radius: 12px;
  color: white;
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.2);
}

.tile-body {
  flex: 1;
}

.tile-title {
  font-weight: 700;
  font-size: 18px;
  color: #2e7d32;
  margin-bottom: 4px;
}

.tile-sub {
  font-size: 14px;
  color: #6b4f2c;
  line-height: 1.4;
}

.tile-arrow {
  color: #2e7d32;
  transition: transform 0.3s ease;
}

.tile:hover .tile-arrow {
  transform: translateX(4px);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .home-shell {
    padding: 24px;
    margin: 20px;
  }

  .brand {
    flex-direction: column;
    gap: 20px;
    margin-bottom: 24px;
  }

  .brand-content {
    max-width: 100%;
  }

  .brand-title {
    font-size: 28px;
  }

  .brand-sub {
    font-size: 15px;
  }

  .brand-description {
    font-size: 14px;
  }

  .brand-logo {
    width: 64px;
    height: 64px;
  }

  .portals {
    grid-template-columns: 1fr;
  }

  .tile {
    padding: 20px;
  }

  .roles-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .back-button {
    position: static;
    align-self: flex-start;
  }

  .roles-title {
    font-size: 20px;
  }
}
</style>
