<template>
  <v-card class="tw-rounded-xl tw-shadow-sm">
    <v-card-title class="tw-text-sm tw-font-semibold">
      <v-icon left small>mdi-account-group</v-icon>
      Farmers
      <v-spacer />
      <v-text-field
        v-model="search"
        append-icon="mdi-magnify"
        dense
        hide-details
        solo
        flat
        placeholder="Search"
        class="tw-max-w-xs"
      />
    </v-card-title>
    <v-data-table
      :headers="headers"
      :items="filteredFarmers"
      :loading="loading"
      dense
      class="elevation-0"
      :items-per-page="5"
    >
      <template #item.status="{ item }">
        <v-chip x-small :color="statusColor(item.status)" dark>{{ item.status || 'ACTIVE' }}</v-chip>
      </template>
      <template #item.actions="{ item }">
        <v-btn icon x-small @click="$emit('select', item)"><v-icon small>mdi-eye</v-icon></v-btn>
      </template>
      <template #no-data>
        <div class="tw-text-center tw-p-6 tw-text-gray-500">No farmers found</div>
      </template>
    </v-data-table>
  </v-card>
</template>
<script>
export default {
  name: 'FarmerList',
  props: {
    farmers: { type: Array, default: () => [] },
    loading: { type: Boolean, default: false },
  },
  data() {
    return {
      search: '',
      headers: [
        { text: 'Name', value: 'farmerName' },
        { text: 'Phone', value: 'phoneNumber' },
        { text: 'Zone', value: 'zoneName', sortable: false },
        { text: 'Status', value: 'status' },
        { text: '', value: 'actions', sortable: false },
      ],
    };
  },
  computed: {
    filteredFarmers() {
      const q = this.search.toLowerCase();
      return this.farmers.filter((f) => (f.farmerName || '').toLowerCase().includes(q));
    },
  },
  methods: {
    statusColor(status) {
      if (status === 'INACTIVE') return 'grey';
      if (status === 'PENDING') return 'orange';
      return 'green';
    },
  },
};
</script>
