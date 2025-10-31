<template>
  <div class="">
    <h4 class="grey--text" v-if="label !== ''">{{label}}</h4>
    <div class="number-input">
      <v-btn @click="decrement" :disabled="value <= 1">-</v-btn>
      <input type="number" v-model.number="value" :min="1" :max="maximumValue"/>
      <v-btn @click="increment" :disabled="value >= maximumValue">+</v-btn>
    </div>
  </div>
</template>

<script>
export default {
  name: 'number-input',
  props: {
    initialValue: {
      type: Number,
      default: 1,
    },
    label: {
      type: String,
      default: '',
    },
    maximumValue: {
      type: Number,
      default: 100000000000,
    },
  },
  data() {
    return {
      value: this.initialValue,
    };
  },
  methods: {
    increment() {
      this.value += 1;
    },
    decrement() {
      if (this.value > 1) {
        this.value -= 1;
      }
    },
  },
  watch: {
    value(newValue) {
      // Emit an event to notify the parent component of the updated value
      this.$emit('input', newValue);
    },
  },
};
</script>

<style scoped>
.number-input {
  display: flex;
  align-items: center;
}

button {
  padding: 5px 10px;
  font-size: 16px;
  cursor: pointer;
}

input {
  width: 50px;
  text-align: center;
  font-size: 16px;
  margin: 0 5px;
  border: 1px solid #ccc;
  padding: 5px;
}
</style>
