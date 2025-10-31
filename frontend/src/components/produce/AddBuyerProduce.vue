<template>
  <div class="produce-selection">
    <!-- Selected Produces Display -->
    <div v-if="selectedProduces.length > 0" class="mb-3">
      <label class="text-body-2 font-weight-medium">Selected Produces:</label>
      <div class="d-flex flex-wrap gap-2 mt-2">
        <v-chip
          v-for="(produce, index) in selectedProduces"
          :key="index"
          small
          close
          color="primary"
          text-color="white"
          @click:close="removeProduce(produce)"
        >
          {{ produce }}
        </v-chip>
      </div>
    </div>

    <!-- Add New Produce Input -->
    <div class="d-flex gap-2 align-center">
      <v-combobox
        v-model="newProduce"
        :items="availableProduceNames"
        label="Add Produce"
        :loading="loading"
        dense
        outlined
        hide-no-data
        clearable
        placeholder="Type or select a produce"
        class="flex-grow-1"
        @keydown.enter.prevent="addProduce"
      />
      <v-btn
        color="primary"
        :disabled="!newProduce || newProduce.trim() === ''"
        @click="addProduce"
      >
        Add
      </v-btn>
    </div>

    <!-- Helper Text -->
    <div class="text-caption text--secondary mt-2">
      Select from existing produces or type a new one and click "Add"
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import validations from '@/utils/validations.js';

export default {
  name: 'AddBuyerProduce',
  props: {
    value: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      availableProduces: [],
      selectedProduces: [],
      newProduce: '',
      loading: false,
      ...validations,
    };
  },
  computed: {
    availableProduceNames() {
      return this.availableProduces.map((p) => p.name);
    },
  },
  watch: {
    value: {
      handler(newVal) {
        // Only update internal state if external value changed and it's different
        const newValue = newVal || [];
        if (this.selectedProduces.length !== newValue.length
            || !this.selectedProduces.every((item) => newValue.includes(item))) {
          this.selectedProduces = [...newValue];
        }
      },
      immediate: true,
    },
  },
  mounted() {
    this.loadAvailableProduces();
  },
  methods: {
    async loadAvailableProduces() {
      try {
        this.loading = true;
        const response = await axios.get('/farmers-service/produce');
        if (response.data.success) {
          this.availableProduces = response.data.data;
        }
      } catch (error) {
        this.$toast.error('Failed to load available produces');
      } finally {
        this.loading = false;
      }
    },
    addProduce() {
      if (!this.newProduce || this.newProduce.trim() === '') {
        return;
      }

      const produceName = this.newProduce.trim();

      // Check if already selected
      if (this.selectedProduces.includes(produceName)) {
        this.$toast.warning('This produce is already selected');
        return;
      }
      // Add to selected produces
      this.selectedProduces.push(produceName);
      this.$emit('input', [...this.selectedProduces]);
      this.newProduce = ''; // Clear input
    },
    removeProduce(produceName) {
      const index = this.selectedProduces.indexOf(produceName);
      if (index >= 0) {
        this.selectedProduces.splice(index, 1);
        this.$emit('input', [...this.selectedProduces]);
      }
    },
  },
};
</script>

<style scoped>
.produce-selection {
  width: 100%;
}

.gap-2 {
  gap: 8px;
}

.flex-grow-1 {
  flex-grow: 1;
}
</style>
