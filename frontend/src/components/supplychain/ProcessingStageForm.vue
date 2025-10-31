<template>
  <div class="tw-space-y-4">
    <div class="tw-bg-purple-50 tw-rounded-lg tw-p-4 tw-border tw-border-purple-200">
      <div class="tw-flex tw-items-center tw-gap-2 tw-text-purple-800">
        <v-icon color="purple darken-2">mdi-information</v-icon>
        <span class="tw-font-semibold">Record produce processing details</span>
      </div>
    </div>

    <v-select
      v-model="formData.processorId"
      :items="processors"
      item-text="name"
      item-value="id"
      label="Select Processor"
      outlined
      prepend-inner-icon="mdi-factory"
      :rules="[rules.required]"
    >
      <template #selection="{ item }">
        <div class="tw-flex tw-items-center tw-gap-2">
          <v-avatar size="24" color="purple lighten-4">
            <span class="tw-text-xs tw-font-bold tw-text-purple-800">{{ getInitials(item.name) }}</span>
          </v-avatar>
          <span>{{ item.name }}</span>
        </div>
      </template>
      <template #item="{ item }">
        <div class="tw-py-2">
          <div class="tw-font-semibold">{{ item.name }}</div>
          <div class="tw-text-xs tw-text-gray-500">{{ item.location || 'Location not set' }}</div>
          <v-chip x-small color="purple" text-color="white" class="tw-mt-1">
            {{ item.processingCapacity || 'N/A' }} kg/day
          </v-chip>
        </div>
      </template>
    </v-select>

    <v-text-field
      v-model="formData.processingDate"
      label="Processing Date"
      type="date"
      outlined
      prepend-inner-icon="mdi-calendar"
      :rules="[rules.required]"
    ></v-text-field>

    <v-text-field
      v-model="formData.inputQuantity"
      label="Input Quantity (kg)"
      type="number"
      outlined
      prepend-inner-icon="mdi-arrow-down-bold"
      :rules="[rules.required, rules.positive]"
      suffix="kg"
    ></v-text-field>

    <v-text-field
      v-model="formData.outputQuantity"
      label="Output Quantity (kg)"
      type="number"
      outlined
      prepend-inner-icon="mdi-arrow-up-bold"
      :rules="[rules.required, rules.positive]"
      suffix="kg"
    ></v-text-field>

    <v-text-field
      v-model="formData.processingMethod"
      label="Processing Method"
      outlined
      prepend-inner-icon="mdi-cog"
      :rules="[rules.required]"
      hint="e.g., Washed, Natural, Honey"
    ></v-text-field>

    <v-select
      v-model="formData.qualityAfterProcessing"
      :items="qualityOptions"
      label="Quality After Processing"
      outlined
      prepend-inner-icon="mdi-star"
      :rules="[rules.required]"
    ></v-select>

    <v-text-field
      v-model="formData.packagingType"
      label="Packaging Type"
      outlined
      prepend-inner-icon="mdi-package"
      :rules="[rules.required]"
      hint="e.g., 60kg Jute Bags, 1kg Vacuum Packs"
    ></v-text-field>

    <v-text-field
      v-model="formData.numberOfPackages"
      label="Number of Packages"
      type="number"
      outlined
      prepend-inner-icon="mdi-numeric"
      :rules="[rules.required, rules.positive]"
    ></v-text-field>

    <v-textarea
      v-model="formData.notes"
      label="Processing Notes"
      outlined
      rows="3"
      prepend-inner-icon="mdi-note-text"
    ></v-textarea>
  </div>
</template>

<script>
export default {
  name: 'ProcessingStageForm',
  props: {
    stage: {
      type: Object,
      required: true,
    },
    processors: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      formData: {
        processorId: '',
        processingDate: '',
        inputQuantity: '',
        outputQuantity: '',
        processingMethod: '',
        qualityAfterProcessing: '',
        packagingType: '',
        numberOfPackages: '',
        notes: '',
        ...this.stage.data,
      },
      qualityOptions: ['A - Premium', 'B - Good', 'C - Standard', 'D - Below Standard'],
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
      return name.split(' ')
        .map((n) => n[0])
        .join('')
        .substring(0, 2)
        .toUpperCase();
    },
  },
};
</script>
