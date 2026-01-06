<template>
  <nav class="professional-navbar" :class="{ 'navbar-scrolled': isScrolled, 'mobile-menu-open': isMenuOpen, 'landing-page': isLandingPage }">
    <div class="navbar-container">
      <div class="navbar-brand" @click="goHome">
        <span class="brand-name">AgriBackup</span>
      </div>

      <!-- Desktop Menu -->
      <div class="navbar-menu desktop-only">
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
        <button
          class="nav-link"
          @click="$router.push({ name: 'Contact' })"
        >
          Contact
        </button>
      </div>

      <!-- Desktop Actions -->
      <div class="navbar-actions desktop-only">
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

      <!-- Mobile Menu Toggle -->
      <button class="mobile-toggle" @click="toggleMenu">
        <!-- <span class="sr-only">Open main menu</span> -->
        <div class="hamburger-icon" :class="{ 'open': isMenuOpen }">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </button>
    </div>

    <!-- Mobile Menu Overlay -->
    <transition name="mobile-menu-fade">
      <div v-if="isMenuOpen" class="mobile-menu-container">
        <div class="mobile-menu-content">
          <button class="mobile-nav-link" @click="handleMobileClick(goToEUDRInfo)">
            What is EUDR?
          </button>
          <button class="mobile-nav-link" @click="handleMobileClick(() => $router.push({ name: 'Documentation' }))">
            Documentation
          </button>
          <button class="mobile-nav-link" @click="handleMobileClick(() => $router.push({ name: 'Contact' }))">
            Contact
          </button>
          <div class="mobile-divider"></div>
          <button class="mobile-nav-link" @click="handleMobileClick(handleLogin)">
            Sign in
          </button>
          <button class="mobile-nav-cta" @click="handleMobileClick(handleSignup)">
            Get Started
          </button>
        </div>
      </div>
    </transition>
  </nav>
</template>

<script>
import analyticsMixin from '@/mixins/analyticsMixin.js';

export default {
  name: 'AppHeader',
  mixins: [analyticsMixin],
  data() {
    return {
      isScrolled: false,
      isMenuOpen: false,
    };
  },
  computed: {
    isLandingPage() {
      return this.$route.name === 'Landing';
    },
  },
  mounted() {
    window.addEventListener('scroll', this.handleScroll);
    this.handleScroll();
  },
  beforeDestroy() {
    window.removeEventListener('scroll', this.handleScroll);
  },
  watch: {
    $route() {
      this.isMenuOpen = false;
    },
  },
  methods: {
    handleScroll() {
      this.isScrolled = window.scrollY > 50;
    },
    toggleMenu() {
      this.isMenuOpen = !this.isMenuOpen;
      if (this.isMenuOpen) {
        document.body.style.overflow = 'hidden';
      } else {
        document.body.style.overflow = '';
      }
    },
    handleMobileClick(action) {
      if (typeof action === 'function') {
        action();
      }
      this.toggleMenu();
    },
    goHome() {
      if (this.$route.name !== 'Landing') {
        this.$router.push({ name: 'Landing' });
      }
      if (this.isMenuOpen) this.toggleMenu();
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
  transition: all 0.3s ease;
}

/* Default state: white background for non-landing pages */
.professional-navbar:not(.landing-page) {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* Landing page: transparent when not scrolled */
.professional-navbar.landing-page {
  background: transparent;
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  border-bottom: 1px solid transparent;
  box-shadow: none;
}

/* ... existing styles ... */
.professional-navbar.navbar-scrolled {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.professional-navbar.mobile-menu-open {
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.navbar-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 12px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  z-index: 1002;
  position: relative;
}

.navbar-brand::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 0;
  height: 2px;
  background: linear-gradient(90deg, #2e7d32, #4ade80);
  transition: width 0.3s ease;
}

.navbar-brand:hover::after {
  width: 100%;
}

.brand-name {
  font-size: 26px;
  font-weight: 700;
  letter-spacing: -0.8px;
  transition: all 0.3s ease;
}

/* Default state: green for non-landing pages */
.professional-navbar:not(.landing-page) .brand-name {
  color: #2e7d32;
  text-shadow: none;
}

/* Landing page: white when not scrolled */
.professional-navbar.landing-page .brand-name {
  color: #ffffff;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.3);
}

/* Landing page scrolled or menu open: green */
.professional-navbar.landing-page.navbar-scrolled .brand-name,
.professional-navbar.landing-page.mobile-menu-open .brand-name {
  color: #2e7d32;
  text-shadow: none;
}

.navbar-menu {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: center;
  gap: 8px;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-link {
  background: none;
  border: none;
  font-size: 15px;
  font-weight: 500;
  padding: 10px 18px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  border-radius: 6px;
}

.nav-link::before {
  content: '';
  position: absolute;
  bottom: 6px;
  left: 18px;
  right: 18px;
  height: 2px;
  background: currentColor;
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.nav-link:hover::before {
  transform: scaleX(1);
}

/* Default state: dark text for non-landing pages */
.professional-navbar:not(.landing-page) .nav-link {
  color: #374151;
  text-shadow: none;
}

/* Landing page: white when not scrolled */
.professional-navbar.landing-page .nav-link {
  color: #ffffff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

/* Landing page scrolled or menu open: dark */
.professional-navbar.landing-page.navbar-scrolled .nav-link,
.professional-navbar.landing-page.mobile-menu-open .nav-link {
  color: #374151;
  text-shadow: none;
}

.nav-link:hover {
  color: #2e7d32;
  background: rgba(46, 125, 50, 0.08);
}

.nav-cta {
  background: linear-gradient(135deg, #2e7d32 0%, #1b5e20 100%);
  color: white;
  border: none;
  font-size: 15px;
  font-weight: 600;
  padding: 11px 28px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(46, 125, 50, 0.2);
  position: relative;
  overflow: hidden;
}

.nav-cta::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s ease;
}

.nav-cta:hover::before {
  left: 100%;
}

.nav-cta:hover {
  background: linear-gradient(135deg, #1b5e20 0%, #145219 100%);
  box-shadow: 0 4px 16px rgba(46, 125, 50, 0.4);
  transform: translateY(-1px);
}

.nav-cta:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(46, 125, 50, 0.3);
}

/* Mobile Toggle Styles */
.mobile-toggle {
  display: none;
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  z-index: 1002;
}

.hamburger-icon {
  width: 24px;
  height: 20px;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.hamburger-icon span {
  display: block;
  height: 2px;
  width: 100%;
  border-radius: 2px;
  transition: all 0.3s ease;
}

/* Default state: dark for non-landing pages */
.professional-navbar:not(.landing-page) .hamburger-icon span {
  background-color: #374151;
  box-shadow: none;
}

/* Landing page: white when not scrolled */
.professional-navbar.landing-page .hamburger-icon span {
  background-color: #ffffff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

/* Landing page scrolled or menu open: dark */
.professional-navbar.landing-page.navbar-scrolled .hamburger-icon span,
.professional-navbar.landing-page.mobile-menu-open .hamburger-icon span {
  background-color: #374151;
  box-shadow: none;
}

.hamburger-icon.open span:nth-child(1) {
  transform: translateY(9px) rotate(45deg);
}

.hamburger-icon.open span:nth-child(2) {
  opacity: 0;
}

.hamburger-icon.open span:nth-child(3) {
  transform: translateY(-9px) rotate(-45deg);
}

/* Mobile Menu Styles */
.mobile-menu-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #ffffff;
  z-index: 1001;
  padding-top: 80px;
  display: flex;
  flex-direction: column;
}

.mobile-menu-content {
  display: flex;
  flex-direction: column;
  padding: 24px;
  gap: 20px;
}

.mobile-nav-link {
  background: none;
  border: none;
  text-align: left;
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  padding: 12px 0;
  cursor: pointer;
}

.mobile-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 10px 0;
}

.mobile-nav-cta {
  background: #2e7d32;
  color: white;
  border: none;
  font-size: 16px;
  font-weight: 600;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  text-align: center;
  margin-top: 10px;
}

/* Responsive Media Queries */
@media (max-width: 768px) {
  .desktop-only {
    display: none;
  }

  .mobile-toggle {
    display: block;
  }
}

.mobile-menu-fade-enter-active,
.mobile-menu-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.mobile-menu-fade-enter, .mobile-menu-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
