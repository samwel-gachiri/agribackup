<template>
  <div class="tw-space-y-4">
    <div class="tw-bg-orange-50 tw-rounded-lg tw-p-4 tw-border tw-border-orange-200">
      <div class="tw-flex tw-items-center tw-gap-2 tw-text-orange-800">
        <v-icon color="orange darken-2">mdi-information</v-icon>
        <span class="tw-font-semibold">Consolidate batches with an aggregator</span>
      </div>
    </div>

    <v-select
      v-model="formData.aggregatorId"
      :items="aggregators"
      item-text="name"
      item-value="id"
      label="Select Aggregator"
      outlined
      prepend-inner-icon="mdi-account-tie"
      :rules="[rules.required]"
    >
      <template #selection="{ item }">
        <div class="tw-flex tw-items-center tw-gap-2">
          <v-avatar size="24" color="orange lighten-4">
            <span class="tw-text-xs tw-font-bold tw-text-orange-800">{{ getInitials(item.name) }}</span>
          </v-avatar>
          <span>{{ item.name }}</span>
        </div>
      </template>
      <template #item="{ item }">
        <div class="tw-py-2">
          <div class="tw-font-semibold">{{ item.name }}</div>
          <div class="tw-text-xs tw-text-gray-500">{{ item.location || 'Location not set' }}</div>
        </div>
      </template>
    </v-select>

    <v-text-field
      v-model="formData.batchNumber"
      label="Batch Number"
      outlined
      prepend-inner-icon="mdi-barcode"
      :rules="[rules.required]"
      hint="Unique identifier for this batch"
    ></v-text-field>

    <v-text-field
      v-model="formData.consolidationDate"
      label="Consolidation Date"
      type="date"
      outlined
      prepend-inner-icon="mdi-calendar"
      :rules="[rules.required]"
    ></v-text-field>

    <v-text-field
      v-model="formData.consolidatedQuantity"
      label="Consolidated Quantity (kg)"
      type="number"
      outlined
      prepend-inner-icon="mdi-weight-kilogram"
      :rules="[rules.required, rules.positive]"
      suffix="kg"
    ></v-text-field>

    <v-text-field
      v-model="formData.storageLocation"
      label="Storage Location"
      outlined
      prepend-inner-icon="mdi-warehouse"
      :rules="[rules.required]"
    ></v-text-field>

    <v-select
      v-model="formData.storageCondition"
      :items="storageConditions"
      label="Storage Condition"
      outlined
      prepend-inner-icon="mdi-thermometer"
      :rules="[rules.required]"
    ></v-select>

    <v-textarea
      v-model="formData.notes"
      label="Additional Notes"
      outlined
      rows="3"
      prepend-inner-icon="mdi-note-text"
    ></v-textarea>
  </div>
</template>

<script>
export default {
  name: 'ConsolidationStageForm',
  props: {
    stage: {
      type: Object,
      required: true,
    },
    aggregators: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      formData: {
        aggregatorId: '',
        batchNumber: '',
        consolidationDate: '',
        consolidatedQuantity: '',
        storageLocation: '',
        storageCondition: '',
        notes: '',
        ...this.stage.data,
      },
      storageConditions: ['Cold Storage', 'Dry Storage', 'Ambient', 'Refrigerated'],
      rules: {
        required: (v) => !!v || 'This field is required',
        positive: (v) => (v && parseFloat(v) > 0) || 'Must be greater than 0',
      },
    };
  },
  watch: {
    formData: {
      deep: true,
      handler(newVal) {
        this.$emit('update', newVal);
      },
    },
  },
  methods: {
    getInitials(name) {
      if (!name) return '?';
      return name.split(' ').map((n) => n[0]).join('').substring(0, 2)
        .toUpperCase();
    },
  },
};
</script>
