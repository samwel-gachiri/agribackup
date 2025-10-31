<template>
  <div class="tw-space-y-4">
    <div class="tw-bg-blue-50 tw-rounded-lg tw-p-4 tw-border tw-border-blue-200">
      <div class="tw-flex tw-items-center tw-gap-2 tw-text-blue-800">
        <v-icon color="blue darken-2">mdi-information</v-icon>
        <span class="tw-font-semibold">Record produce collection from farmers</span>
      </div>
    </div>

    <v-text-field
      v-model="formData.collectionDate"
      label="Collection Date"
      type="date"
      outlined
      prepend-inner-icon="mdi-calendar"
      :rules="[rules.required]"
    ></v-text-field>

    <v-text-field
      v-model="formData.quantity"
      label="Quantity (kg)"
      type="number"
      outlined
      prepend-inner-icon="mdi-weight-kilogram"
      :rules="[rules.required, rules.positive]"
      suffix="kg"
    ></v-text-field>

    <v-text-field
      v-model="formData.farmerCount"
      label="Number of Farmers"
      type="number"
      outlined
      prepend-inner-icon="mdi-account-group"
      :rules="[rules.required, rules.positive]"
    ></v-text-field>

    <v-text-field
      v-model="formData.collectionPoint"
      label="Collection Point"
      outlined
      prepend-inner-icon="mdi-map-marker"
      :rules="[rules.required]"
    ></v-text-field>

    <v-select
      v-model="formData.quality"
      :items="qualityOptions"
      label="Quality Grade"
      outlined
      prepend-inner-icon="mdi-star"
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
  name: 'CollectionStageForm',
  props: {
    stage: {
      type: Object,
      required: true,
    },
  },
  data() {
    return {
      formData: {
        collectionDate: '',
        quantity: '',
        farmerCount: '',
        collectionPoint: '',
        quality: '',
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
};
</script>
