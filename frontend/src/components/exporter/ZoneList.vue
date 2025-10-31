<template>
  <v-card class="tw-rounded-xl tw-shadow-sm">
    <v-card-title class="tw-text-sm tw-font-semibold">
      <v-icon left small>mdi-map-marker-multiple</v-icon>
      Zones
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
      :items="filteredZones"
      :loading="loading"
      dense
      class="elevation-0"
      :items-per-page="5"
    >
      <template #item.farmerCount="{ item }">
        <v-chip x-small :color="farmerColor(item.farmerCount)" dark>{{ item.farmerCount }}</v-chip>
      </template>
      <template #item.actions="{ item }">
        <v-btn icon x-small @click="$emit('select', item)"><v-icon small>mdi-eye</v-icon></v-btn>
      </template>
      <template #no-data>
        <div class="tw-text-center tw-p-6 tw-text-gray-500">No zones found</div>
      </template>
    </v-data-table>
  </v-card>
</template>
<script>
export default {
  name: 'ZoneList',
  props: {
    zones: { type: Array, default: () => [] },
    loading: { type: Boolean, default: false },
  },
  data() {
    return {
      search: '',
      headers: [
        { text: 'Name', value: 'name' },
        { text: 'Produce', value: 'produceType' },
        { text: 'Farmers', value: 'farmerCount' },
        { text: 'Supervisors', value: 'supervisorIds.length', sortable: false },
        { text: '', value: 'actions', sortable: false },
      ],
    };
  },
  computed: {
    filteredZones() {
      const q = this.search.toLowerCase();
      return this.zones.filter((z) => z.name.toLowerCase().includes(q));
    },
  },
  methods: {
    farmerColor(count) {
      if (count === 0) return 'grey';
      if (count < 5) return 'orange';
      if (count < 15) return 'blue';
      return 'green';
    },
  },
};
</script>
