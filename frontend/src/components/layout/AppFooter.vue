<template>
  <footer class="tw-bg-green-900 tw-text-white tw-py-12 tw-px-6">
    <div class="tw-max-w-6xl tw-mx-auto tw-flex tw-flex-col md:tw-flex-row tw-items-center tw-justify-between">
      <div class="tw-mb-4 md:tw-mb-0">
        <h2 class="tw-text-xl tw-font-semibold">Ready to transform your farm?</h2>
      </div>
      <button
        @click="handleSignup"
        class="tw-bg-white tw-text-green-700 tw-font-semibold tw-py-3 tw-px-6 tw-rounded-lg tw-shadow tw-transition hover:tw-bg-green-100"
        aria-label="Join AgriBackup Now"
      >
        Join Now
      </button>
    </div>
    <div class="tw-mt-6 tw-text-center tw-text-sm tw-opacity-80">
      &copy; {{ new Date().getFullYear() }} AgriBackup. All rights reserved. |
      <router-link to="/contact" class="tw-underline tw-text-white">Contact Us</router-link>
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
