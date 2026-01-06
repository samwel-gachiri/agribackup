<template>
  <footer class="footer-professional">
    <!-- CTA Section -->
    <div class="footer-cta">
      <div class="tw-max-w-6xl tw-mx-auto tw-px-6 tw-py-16 tw-flex tw-flex-col md:tw-flex-row tw-items-center tw-justify-between">
        <div class="tw-mb-6 md:tw-mb-0 tw-text-center md:tw-text-left">
          <h2 class="tw-text-3xl tw-font-bold tw-text-white tw-mb-2">Ready to achieve EUDR compliance?</h2>
          <p class="tw-text-green-100 tw-text-lg">Join thousands of stakeholders building transparent supply chains.</p>
        </div>
        <button
          @click="handleSignup"
          class="tw-bg-white tw-text-green-700 tw-font-bold tw-py-4 tw-px-10 tw-rounded-xl tw-shadow-lg tw-transition hover:tw-bg-green-50 hover:tw-shadow-xl tw-text-lg"
          aria-label="Get Started Now"
        >
          Get Started Now
        </button>
      </div>
    </div>

    <!-- Main Footer -->
    <div class="footer-main">
      <div class="tw-max-w-6xl tw-mx-auto tw-px-6 tw-py-16">
        <div class="tw-grid tw-grid-cols-1 md:tw-grid-cols-4 tw-gap-12">
          <!-- Brand Column -->
          <div class="tw-col-span-1 md:tw-col-span-1">
            <h3 class="tw-text-2xl tw-font-bold tw-text-white tw-mb-4">AgriBackup</h3>
            <p class="tw-text-gray-400 tw-text-sm tw-leading-relaxed tw-mb-6">
              EUDR-compliant supply chain platform powered by Hedera DLT. Full transparency from farm to export.
            </p>
            <div class="tw-flex tw-gap-4">
              <a href="#" class="tw-w-10 tw-h-10 tw-bg-gray-800 tw-rounded-lg tw-flex tw-items-center tw-justify-center tw-transition hover:tw-bg-green-600" aria-label="LinkedIn">
                <v-icon size="20" color="white">mdi-linkedin</v-icon>
              </a>
              <a href="#" class="tw-w-10 tw-h-10 tw-bg-gray-800 tw-rounded-lg tw-flex tw-items-center tw-justify-center tw-transition hover:tw-bg-green-600" aria-label="Twitter">
                <v-icon size="20" color="white">mdi-twitter</v-icon>
              </a>
              <a href="#" class="tw-w-10 tw-h-10 tw-bg-gray-800 tw-rounded-lg tw-flex tw-items-center tw-justify-center tw-transition hover:tw-bg-green-600" aria-label="GitHub">
                <v-icon size="20" color="white">mdi-github</v-icon>
              </a>
            </div>
          </div>

          <!-- Platform Column -->
          <div>
            <h4 class="tw-text-sm tw-font-semibold tw-text-white tw-uppercase tw-tracking-wider tw-mb-4">Platform</h4>
            <ul class="tw-space-y-3">
              <li><a href="#" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">For Farmers</a></li>
              <li><a href="#" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">For Suppliers</a></li>
              <li><a href="#" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">For Exporters</a></li>
              <li><a href="#" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">Pricing</a></li>
            </ul>
          </div>

          <!-- Resources Column -->
          <div>
            <h4 class="tw-text-sm tw-font-semibold tw-text-white tw-uppercase tw-tracking-wider tw-mb-4">Resources</h4>
            <ul class="tw-space-y-3">
              <li><router-link to="/eudr-info" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">What is EUDR?</router-link></li>
              <li><router-link to="/documentation" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">Documentation</router-link></li>
              <li><a href="#" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">API Reference</a></li>
              <li><a href="#" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">Blog</a></li>
            </ul>
          </div>

          <!-- Company Column -->
          <div>
            <h4 class="tw-text-sm tw-font-semibold tw-text-white tw-uppercase tw-tracking-wider tw-mb-4">Company</h4>
            <ul class="tw-space-y-3">
              <li><a href="#" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">About Us</a></li>
              <li><router-link to="/contact" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">Contact</router-link></li>
              <li><a href="#" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">Privacy Policy</a></li>
              <li><a href="#" class="tw-text-gray-400 hover:tw-text-white tw-transition tw-text-sm">Terms of Service</a></li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Bar -->
    <div class="footer-bottom">
      <div class="tw-max-w-6xl tw-mx-auto tw-px-6 tw-py-6 tw-flex tw-flex-col md:tw-flex-row tw-items-center tw-justify-between">
        <p class="tw-text-gray-500 tw-text-sm tw-mb-4 md:tw-mb-0">
          &copy; {{ new Date().getFullYear() }} AgriBackup. All rights reserved.
        </p>
        <div class="tw-flex tw-items-center tw-gap-6">
          <span class="tw-text-gray-500 tw-text-xs tw-flex tw-items-center tw-gap-2">
            <v-icon size="14" color="grey">mdi-shield-check</v-icon>
            EUDR Compliant
          </span>
          <span class="tw-text-gray-500 tw-text-xs tw-flex tw-items-center tw-gap-2">
            <v-icon size="14" color="grey">mdi-cube-outline</v-icon>
            Powered by Hedera
          </span>
        </div>
      </div>
    </div>
  </footer>
</template>

<script>
export default {
  name: 'AppFooter',
  methods: {
    handleSignup() {
      this.$router.push({ name: 'SignUp' });
    },
  },
};
</script>

<style scoped>
.footer-professional {
  background: #111827;
}

.footer-cta {
  background: linear-gradient(135deg, #166534 0%, #15803d 100%);
}

.footer-main {
  border-bottom: 1px solid #1f2937;
}

.footer-bottom {
  background: #0f172a;
}
</style>
