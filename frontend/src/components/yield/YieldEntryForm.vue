<template>
  <div class="yield-entry-form">
    <h3 class="text-h6 font-weight-bold mb-4 d-flex align-center">
      <v-icon color="primary" class="mr-2">mdi-plus-circle</v-icon>
      Record New Yield
    </h3>
    <v-form @submit.prevent="submit">
      <v-select
        v-model="form.farmerProduceId"
        :items="produces"
        item-text="farmProduce.name"
        item-value="id"
        label="Select Produce"
        outlined
        dense
        required
        class="mb-4"
        prepend-icon="mdi-sprout"
      >
        <template v-slot:no-data>
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title>
                No produces found. Add some produces first.
              </v-list-item-title>
            </v-list-item-content>
          </v-list-item>
        </template>
      </v-select>

      <v-text-field
        v-model.number="form.yieldAmount"
        label="Yield Amount"
        type="number"
        min="0.01"
        step="0.01"
        outlined
        dense
        required
        class="mb-4"
        prepend-icon="mdi-weight"
      />

      <v-select
        v-model="form.yieldUnit"
        :items="units"
        label="Unit"
        outlined
        dense
        required
        class="mb-4"
        prepend-icon="mdi-scale-balance"
      />

      <v-text-field
        v-model="form.plantingDate"
        label="Planting Date (optional)"
        type="date"
        outlined
        dense
        class="mb-4"
        prepend-icon="mdi-seed"
      />

      <v-text-field
        v-model="form.harvestDate"
        label="Harvest Date"
        type="date"
        outlined
        dense
        required
        class="mb-4"
        prepend-icon="mdi-calendar-check"
      />

      <v-row class="mb-4">
        <v-col cols="6">
          <v-text-field
            v-model.number="form.seasonYear"
            label="Season Year"
            type="number"
            min="2000"
            max="2100"
            outlined
            dense
            prepend-icon="mdi-calendar"
          />
        </v-col>
        <v-col cols="6">
          <v-text-field
            v-model="form.seasonName"
            label="Season Name"
            placeholder="e.g. Long Rains"
            outlined
            dense
            prepend-icon="mdi-weather-rainy"
          />
        </v-col>
      </v-row>

      <v-textarea
        v-model="form.notes"
        label="Notes (optional)"
        outlined
        dense
        rows="2"
        class="mb-4"
        prepend-icon="mdi-note-text"
        placeholder="Any notes about this harvest..."
      />

      <v-btn
        type="submit"
        color="success"
        block
        large
        class="font-weight-bold"
      >
        <v-icon left>mdi-content-save</v-icon>
        Record Yield
      </v-btn>
    </v-form>
  </div>
</template>

<script>
export default {
  name: 'YieldEntryForm',
  props: {
    produces: Array,
    units: Array,
    form: Object,
    submit: Function,
  },
};
</script>

<style scoped>
.yield-entry-form {
  background: white;
  border-radius: 8px;
  padding: 0;
}
</style>
