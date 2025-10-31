<template>
  <Default>
    <div class="tw-p-6 tw-space-y-4">
      <h1 class="tw-text-2xl tw-font-bold">Exporter Profile</h1>
      <p class="tw-text-gray-600">Placeholder view: display exporter organization details & verification status.</p>
      <v-alert type="info" outlined dense>
        Backend endpoint suggestion: GET /api/exporters-service/exporter/{id}
      </v-alert>
    </div>
  </Default>
</template>
<script>
import Default from '@/components/layout/Default.vue';

export default { name: 'ExporterProfile', components: { Default } };
</script>
