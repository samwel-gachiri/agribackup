<template>
  <div class="tw-flex tw-rounded-lg tw-w-full tw-items-center">
    <!-- Country Code Dropdown -->
    <v-select
        prepend-icon="mdi-phone"
        outlined
        label="Country code"
        v-model="selectedCountry"
        :items="filteredCountries"
        item-text="display"
        return-object
        dense
        class="tw-w-48"
        variant="outlined"
        @update:model-value="updatePhone"
    >
      <template v-slot:selection="{ item }">
        <span class="tw-flex tw-items-center">
          <span class="tw-mr-1">{{ item.flag }}</span>
          <span>{{ item.dialCode }}</span>
        </span>
      </template>
      <template v-slot:item="{ item }">
        <span class="tw-flex tw-items-center">
          <span class="tw-mr-2">{{ item.flag }}</span>
          <span>{{ item.name }} ({{ item.dialCode }})</span>
        </span>
      </template>
    </v-select>

    <!-- Phone Number Input -->
    <v-text-field
        outlined
        v-model="phoneNumber"
        label="Phone number"
        placeholder="123456789"
        dense
        class=""
        variant="outlined"
        :rules="[phoneNumber.length === 0 || validatePhone]"
        @input="updatePhone"
    />
  </div>
</template>

<script>
export default {
  props: {
    modelValue: String, // v-model binding
    defaultCountryCode: {
      type: String,
      default: 'KE',
    },
    preferredCountries: {
      type: Array,
      default: () => ['KE', 'US', 'UG', 'TZ'],
    },
  },
  data() {
    return {
      selectedCountry: null,
      phoneNumber: '',
      countries: [
        {
          name: 'Kenya', code: 'KE', dialCode: '+254', flag: '🇰🇪', regex: /^[0-9]{9}$/,
        },
        // {
        //   name: 'United States', code: 'US', dialCode: '+1', flag: '🇺🇸', regex: /^[0-9]{10}$/,
        // },
        // {
        //   name: 'United Kingdom', code: 'GB', dialCode: '+44', flag: '🇬🇧', regex: /^[0-9]{10}$/,
        // },
        // {
        //   name: 'India', code: 'IN', dialCode: '+91', flag: '🇮🇳', regex: /^[0-9]{10}$/,
        // },
        // {
        //   name: 'Nigeria', code: 'NG', dialCode: '+234', flag: '🇳🇬', regex: /^[0-9]{10}$/,
        // },
        // {
        //   name: 'Uganda', code: 'UG', dialCode: '+256', flag: '🇺🇬', regex: /^[0-9]{9}$/,
        // },
        // {
        //   name: 'Tanzania', code: 'TZ', dialCode: '+255', flag: '🇹🇿', regex: /^[0-9]{9}$/,
        // },
        // Add more countries as needed
      ],
    };
  },
  computed: {
    filteredCountries() {
      return this.countries.filter((c) => this.preferredCountries.includes(c.code));
    },
  },
  watch: {
    modelValue(newValue) {
      if (newValue) {
        const matchedCountry = this.countries.find((c) => newValue.startsWith(c.dialCode));
        if (matchedCountry) {
          this.selectedCountry = matchedCountry;
          this.phoneNumber = newValue.replace(matchedCountry.dialCode, '');
        }
      }
    },
  },
  mounted() {
    this.selectedCountry = this.countries.find((c) => c.code === this.defaultCountryCode) || this.countries[0];
  },
  methods: {
    updatePhone() {
      const fullPhone = `${this.selectedCountry.dialCode}${this.phoneNumber}`;
      this.$emit('update:phoneNumber', fullPhone); // Emit the full phone number
    },
    validatePhone(value) {
      if (!this.selectedCountry) return true; // Skip validation if no country is selected
      const regex = this.selectedCountry.regex;
      return regex.test(value) || 'Enter a valid phone number';
    },
  },
};
</script>
