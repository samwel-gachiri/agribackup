<template>
  <div class="tw-mb-5">
    <label class="tw-block tw-mb-2 tw-font-medium tw-text-gray-700">Select Zone</label>
    <select
      v-model="localSelectedZone"
      class="tw-w-full tw-p-3 tw-bg-gray-50 focus:tw-outline-none focus:tw-border-blue-500 focus:tw-bg-white tw-transition tw-rounded tw-border tw-border-gray-300"
      :disabled="loading"
      @focus="$emit('fetch')"
      @change="$emit('select', localSelectedZone)"
    >
      <option :value="null" disabled>Select a zone</option>
      <option v-for="zone in zones" :key="zone.id" :value="zone">
        {{ zone.name }}
      </option>
    </select>
  </div>
</template>

<script>
export default {
  name: 'ZoneSelector',
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
  computed: {
    localSelectedZone: {
      get() {
        return this.selectedZone;
      },
      set(value) {
        this.$emit('update:selectedZone', value);
      },
    },
  },
};
</script>
