<template>
  <div>
    <div v-if="zone" class="tw-mb-6">
      <h4 class="tw-font-semibold tw-text-lg tw-mb-2">Zone: {{ zone.name }}</h4>
      <p><strong>Produce Type:</strong> {{ zone.produceType || 'N/A' }}</p>
      <p><strong>Center:</strong> {{ zone.centerLatitude }}, {{ zone.centerLongitude }}</p>
      <p><strong>Radius:</strong> {{ zone.radiusKm }} km</p>
    </div>
    <div class="tw-mb-6">
      <h4 class="tw-font-semibold tw-text-lg tw-mb-2">Supervisors</h4>
      <button
        v-if="!supervisors.length && zone"
        @click="$emit('fetch-supervisors')"
        :disabled="loading"
        class="tw-bg-blue-600 hover:tw-bg-blue-700 tw-text-white tw-py-2 tw-px-4 tw-rounded-lg tw-font-semibold tw-shadow-md tw-transition"
      >
        <span v-if="loading">Loading...</span>
        <span v-else>Load Supervisors</span>
      </button>
      <ul v-if="supervisors.length > 0" class="tw-divide-y tw-rounded-lg tw-bg-gray-50 tw-shadow-inner">
        <li v-for="supervisor in supervisors" :key="supervisor.id" class="tw-p-4">
          <div class="tw-font-semibold tw-text-gray-800">{{ supervisor.name }}</div>
          <div class="tw-text-xs tw-text-gray-600">{{ supervisor.email }}</div>
        </li>
      </ul>
      <div v-else-if="!loading && supervisors.length === 0 && zone" class="tw-mt-4 tw-text-blue-700 tw-bg-blue-50 tw-p-3 tw-rounded-lg tw-shadow-inner">
        No supervisors found for this zone
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ZoneDetails',
  props: {
    zone: {
      type: Object,
      default: null,
    },
    supervisors: {
      type: Array,
      default: () => [],
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
