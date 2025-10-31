<template>
  <v-navigation-drawer
    :value="value"
    right
    temporary
    width="380"
    class="tw-px-4 tw-py-6"
    @input="$emit('input', $event)"
  >
    <div class="tw-flex tw-items-center tw-justify-between tw-mb-4">
      <h3 class="tw-text-lg tw-font-semibold tw-text-gray-800">Farmer Details</h3>
      <v-btn icon small @click="$emit('input', false)"><v-icon small>mdi-close</v-icon></v-btn>
    </div>
    <v-skeleton-loader v-if="!farmer" type="paragraph" />
    <div v-else class="tw-space-y-4">
      <div>
        <p class="tw-text-xs tw-text-gray-500 tw-uppercase">Name</p>
        <p class="tw-font-medium">{{ farmer.farmerName || farmer.name }}</p>
      </div>
      <div class="tw-grid tw-grid-cols-2 tw-gap-4">
        <div>
          <p class="tw-text-xs tw-text-gray-500 tw-uppercase">Phone</p>
          <p class="tw-font-medium">{{ farmer.phoneNumber || '—' }}</p>
        </div>
        <div>
          <p class="tw-text-xs tw-text-gray-500 tw-uppercase">Zone</p>
          <p class="tw-font-medium">{{ farmer.zoneName || farmer.zoneId || '—' }}</p>
        </div>
      </div>
      <div>
        <p class="tw-text-xs tw-text-gray-500 tw-uppercase">Status</p>
        <v-chip x-small :color="statusColor(farmer.status)" dark>{{ farmer.status || 'ACTIVE' }}</v-chip>
      </div>
      <div v-if="farmer.location">
        <p class="tw-text-xs tw-text-gray-500 tw-uppercase">Location</p>
        <p class="tw-font-medium">{{ farmer.location.latitude }}, {{ farmer.location.longitude }}</p>
        <v-btn small text color="primary" class="tw-mt-1" @click="$emit('locate', farmer)">
          <v-icon left small>mdi-crosshairs-gps</v-icon>
          Center on Map
        </v-btn>
      </div>
      <div>
        <p class="tw-text-xs tw-text-gray-500 tw-uppercase">Actions</p>
        <div class="tw-flex tw-gap-2 tw-mt-1">
          <v-btn small outlined color="primary" @click="$emit('message', farmer)"><v-icon left small>mdi-message</v-icon>Message</v-btn>
          <v-btn small outlined color="error" @click="$emit('flag', farmer)"><v-icon left small>mdi-flag</v-icon>Flag</v-btn>
        </div>
      </div>
    </div>
  </v-navigation-drawer>
</template>
<script>
export default {
  name: 'FarmerDetailDrawer',
  props: {
    value: { type: Boolean, default: false },
    farmer: { type: Object, default: null },
  },
  methods: {
    statusColor(status) {
      if (status === 'INACTIVE') return 'grey';
      if (status === 'PENDING') return 'orange';
      return 'green';
    },
  },
};
</script>
