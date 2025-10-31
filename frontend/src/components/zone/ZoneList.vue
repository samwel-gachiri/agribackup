<template>
  <div>
    <ul v-if="zones.length > 0" class="tw-divide-y tw-rounded-lg tw-bg-gray-50 tw-shadow-inner">
      <li
        v-for="zone in zones"
        :key="zone.id"
        @click="$emit('select', zone)"
        :class="[
          'tw-p-4 tw-cursor-pointer tw-transition tw-rounded-lg',
          selectedZone && selectedZone.id === zone.id ? 'tw-bg-blue-100 tw-shadow' : 'hover:tw-bg-gray-100'
        ]"
      >
        <div class="tw-font-semibold tw-text-gray-800">{{ zone.name }}</div>
        <div class="tw-text-xs tw-text-gray-600">
          {{ zone.produceType || 'N/A' }} - {{ zone.radiusKm }}km
        </div>
      </li>
    </ul>
    <div v-else-if="!loading" class="tw-mt-4 tw-text-blue-700 tw-bg-blue-50 tw-p-3 tw-rounded-lg tw-shadow-inner">
      No zones found
    </div>
  </div>
</template>

<script>
export default {
  name: 'ZoneList',
  props: {
    zones: {
      type: Array,
      default: () => [],
    },
    selectedZone: {
      type: Object,
      default: null,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
};
</script>
