<template>
  <div class="tw-flex tw-flex-col tw-items-center tw-justify-center tw-h-screen tw-bg-gray-100">
    <h1 class="tw-text-4xl tw-font-bold tw-text-blue-700 tw-mb-4">Your Company Name</h1>
    <p class="tw-text-xl tw-text-gray-700 tw-mb-8">You are currently offline</p>
    <!-- Cool animation placeholder (replace with Lottie or CSS animation) -->
    <div class="tw-w-40 tw-h-40 tw-bg-blue-200 tw-rounded-full tw-flex tw-items-center tw-justify-center tw-animate-bounce">
      <span class="tw-text-6xl tw-text-blue-500">📡</span>
    </div>
  </div>
</template>
<script>
export default {
  name: 'Offline',
};
</script>
<style scoped>
/* You can add more animation styles here */
</style>
