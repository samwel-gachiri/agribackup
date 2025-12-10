<template>
  <nav class="professional-navbar">
    <div class="navbar-container">
      <div class="navbar-brand" @click="goHome">
        <span class="brand-name">AgriBackup</span>
      </div>
      <div class="navbar-menu">
        <button
          class="nav-link"
          @click="goToEUDRInfo"
        >
          What is EUDR?
        </button>
        <button
          class="nav-link"
          @click="$router.push({ name: 'Documentation' })"
        >
          Documentation
        </button>
      </div>
      <div class="navbar-actions">
        <button
          class="nav-link"
          @click="handleLogin"
        >
          Sign in
        </button>
        <button
          class="nav-cta"
          @click="handleSignup"
        >
          Get Started
        </button>
      </div>
    </div>
  </nav>
</template>

<script>
import analyticsMixin from '@/mixins/analyticsMixin.js';

export default {
  name: 'AppHeader',
  mixins: [analyticsMixin],
  methods: {
    goHome() {
      if (this.$route.name !== 'Landing') {
        this.$router.push({ name: 'Landing' });
      }
    },
    goToEUDRInfo() {
      this.trackButtonClick('what_is_eudr', { source: 'navbar' });
      if (this.$route.name !== 'EUDRInfo') {
        this.$router.push({ name: 'EUDRInfo' });
      }
    },
    handleLogin() {
      this.trackButtonClick('login_navbar', { source: 'navbar' });
      this.$store.dispatch('auth/setViewRole', '');
      this.$router.push({ name: 'SignIn' });
    },
    handleSignup() {
      this.trackButtonClick('create_account_navbar', { source: 'navbar' });
      this.$store.dispatch('auth/setViewRole', '');
      this.$router.push({ name: 'SignUp' });
    },
  },
};
</script>

<style scoped>
.professional-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.navbar-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.brand-name {
  font-size: 24px;
  font-weight: 700;
  color: #2e7d32;
  letter-spacing: -0.5px;
}

.navbar-menu {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: center;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-link {
  background: none;
  border: none;
  color: #374151;
  font-size: 15px;
  font-weight: 500;
  padding: 8px 16px;
  cursor: pointer;
  transition: color 0.2s;
}

.nav-link:hover {
  color: #2e7d32;
}

.nav-cta {
  background: #2e7d32;
  color: white;
  border: none;
  font-size: 15px;
  font-weight: 600;
  padding: 10px 24px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-cta:hover {
  background: #1b5e20;
  box-shadow: 0 4px 12px rgba(46, 125, 50, 0.3);
}
</style>
