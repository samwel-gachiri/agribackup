<template>
  <div class="currency-selector">
    <v-select
        v-model="localCurrency"
        :items="currencies"
        label="Currency"
        outlined
        dense
        class="mb-4"
    />
    <v-money
        v-model="amount"
        :options="vuetifyMoneyOptions"
        :label="label"
        outlined
        dense
    />
  </div>
</template>

<script>
import HelperMixins from '@/mixins/helperMixins.js';

export default {
  name: 'currency-field',
  data() {
    return {
      localCurrency: this.currency,
      localAmount: this.amount,
    };
  },
  props: {
    currency: {
      type: String,
      required: true,
      default: 'KSH',
    },
    amount: {
      type: Number,
      required: true,
    },
    currencies: {
      type: Array,
      default: () => ['KSH', 'USD', 'EUR', 'GBP'],
      required: false,
    },
    label: {
      type: String,
      default: 'Amount',
    },
  },
  watch: {
    // Sync props with local data
    currency: {
      immediate: true,
      handler(newValue) {
        this.localCurrency = newValue;
      },
    },
    amount: {
      immediate: true,
      handler(newValue) {
        this.localAmount = newValue;
      },
    },
    // Emit changes to the parent
    localCurrency(newValue) {
      this.$emit('update:currency', newValue);
    },
    localAmount(newValue) {
      this.$emit('update:amount', newValue);
    },
  },
  mixins: [HelperMixins],
};
</script>

<style scoped>
.currency-selector {
  max-width: 400px;
  margin: auto;
}
</style>
