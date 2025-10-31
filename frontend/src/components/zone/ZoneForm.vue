<template>
  <form @submit.prevent="$emit('submit')" ref="form">
    <div class="tw-mb-5">
      <label class="tw-block tw-mb-2 tw-font-medium tw-text-gray-700">Zone Name</label>
      <input
        v-model="zone.name"
        type="text"
        class="tw-w-full tw-p-3 tw-bg-gray-50 focus:tw-outline-none focus:tw-border-blue-500 focus:tw-bg-white tw-transition tw-rounded-full"
        :class="{ 'tw-border-red-500 border-danger': !zone.name && formTouched }"
        required
        placeholder="Enter zone name"
        @input="$emit('update:zone', { ...zone })"
      />
    </div>
    <div class="tw-mb-5">
      <label class="tw-block tw-mb-2 tw-font-medium tw-text-gray-700">Produce Type</label>
      <select
        v-model="zone.produceType"
        class="tw-w-full tw-p-3 tw-bg-gray-50 tw-rounded-full"
        @change="$emit('update:zone', { ...zone })"
      >
        <option v-for="type in produceTypes" :key="type" :value="type">{{ type }}</option>
      </select>
    </div>
    <div class="tw-mb-5">
      <label class="tw-block tw-mb-2 tw-font-medium tw-text-gray-700">Center Latitude</label>
      <input
        v-model.number="zone.centerLatitude"
        type="number"
        step="any"
        class="tw-w-full tw-p-3 tw-bg-gray-50 focus:tw-outline-none focus:tw-border-blue-500 focus:tw-bg-white tw-transition tw-rounded-full"
        :class="{ 'tw-border-red-500 border-danger': !rules.latitude(zone.centerLatitude) && formTouched }"
        required
        placeholder="e.g. -1.2921"
        @input="$emit('update:zone', { ...zone })"
      />
    </div>
    <div class="tw-mb-5">
      <label class="tw-block tw-mb-2 tw-font-medium tw-text-gray-700">Center Longitude</label>
      <input
        v-model.number="zone.centerLongitude"
        type="number"
        step="any"
        class="tw-w-full tw-p-3 tw-bg-gray-50 focus:tw-outline-none focus:tw-border-blue-500 focus:tw-bg-white tw-transition tw-rounded-full"
        :class="{ 'tw-border-red-500 border-danger': !rules.longitude(zone.centerLongitude) && formTouched }"
        required
        placeholder="e.g. 36.8219"
        @input="$emit('update:zone', { ...zone })"
      />
    </div>
    <div class="tw-mb-6">
      <label class="tw-block tw-mb-2 tw-font-medium tw-text-gray-700">Radius (KM)</label>
      <input
        v-model.number="zone.radiusKm"
        type="number"
        step="any"
        class="tw-w-full tw-p-3 tw-bg-gray-50 focus:tw-outline-none focus:tw-border-blue-500 focus:tw-bg-white tw-transition tw-rounded-full"
        :class="{ 'tw-border-red-500 border-danger': !rules.positive(zone.radiusKm) && formTouched }"
        required
        placeholder="e.g. 10"
        @input="$emit('update:zone', { ...zone })"
      />
    </div>
    <button
      type="submit"
      :disabled="loading"
      class="tw-bg-blue-600 hover:tw-bg-blue-700 tw-text-white tw-py-3 tw-px-4 tw-rounded-lg tw-w-full tw-font-semibold tw-shadow-md tw-transition"
    >
      <span v-if="loading">Processing...</span>
      <span v-else>{{ zone.id ? 'Update Zone' : 'Create Zone' }}</span>
    </button>
  </form>
</template>

<script>
export default {
  name: 'ZoneForm',
  props: {
    zone: {
      type: Object,
      required: true,
    },
    formTouched: {
      type: Boolean,
      default: false,
    },
    rules: {
      type: Object,
      required: true,
    },
    loading: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      produceTypes: [
        'Maize', 'Beans', 'Rice', 'Wheat', 'Coffee', 'Tea',
        'Sugarcane', 'Cotton', 'Vegetables', 'Fruits', 'Other',
      ],
    };
  },
  methods: {
    reset() {
      this.$refs.form.reset();
    },
  },
};
</script>
