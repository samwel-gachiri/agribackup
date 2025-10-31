<template>
  <Default>
    <v-container fluid class="pa-6">
      <v-row>
        <v-col cols="12" class="pb-0">
          <div class="tw-space-y-1">
            <h1 class="tw-text-2xl tw-font-bold tw-text-gray-900">Pickup Schedules</h1>
            <p class="tw-text-sm tw-text-gray-600">Create and manage efficient multi‑stop collection routes.</p>
          </div>
        </v-col>
      </v-row>

      <v-row class="tw-mt-6">
        <v-col cols="12">
          <v-card class="tw-shadow-sm tw-border tw-border-gray-100">
            <v-card-text class="pa-4">
              <PickupRoutes />
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </Default>
</template>
<script>
/* eslint-disable */
import Default from '@/components/layout/Default.vue';
import PickupRoutes from '@/views/PickupRoutes.vue';

export default { name: 'PickupSchedulesManagement', components: { Default, PickupRoutes } };
</script>

<style scoped>
.pickup-schedules-root {
  background: linear-gradient(135deg, #f8fafc 0%, #eef2f7 100%);
  min-height: 100vh;
}
</style>
