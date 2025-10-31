<template>
  <!-- Deprecated standalone exporter dashboard view. The unified dashboard now lives at components/layout/dashboard/ExporterDashboard.vue
       This file is kept as a lightweight redirect/stub to avoid broken imports during transition. -->
  <div class="tw-p-8 tw-text-center tw-text-gray-600">
    <h2 class="tw-text-xl tw-font-semibold tw-mb-4">Exporter Dashboard Moved</h2>
    <p>Use the consolidated dashboard via <code>/dashboard</code>. This legacy view will be removed.</p>
    <v-btn color="primary" class="tw-mt-6" @click="$router.push({ name: 'Dashboard' })">Go to Dashboard</v-btn>
  </div>
</template>

<script>
export default {
  name: 'ExporterDashboardLegacyStub',
};
</script>

<style scoped>
/* minimal styling only */
</style>
