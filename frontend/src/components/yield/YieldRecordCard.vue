<template>
  <div class="yield-record-card flex flex-col gap-2 p-4 rounded shadow border bg-white">
    <div class="flex items-center gap-2">
      <span class="font-bold">{{ yieldItem.produceName }}</span>
      <span class="badge bg-blue-100 text-blue-800">{{ formatDate(yieldItem.harvestDate) }}</span>
    </div>
    <div class="flex items-center gap-2">
      <span class="text-xs">Yield: <b>{{ yieldItem.yieldAmount }}</b> {{ yieldItem.yieldUnit }}</span>
      <span v-if="yieldItem.seasonYear || yieldItem.seasonName" class="text-xs">Season: {{ yieldItem.seasonYear }} {{ yieldItem.seasonName }}</span>
    </div>
    <div class="flex items-center gap-2">
      <span class="text-xs">Notes: {{ yieldItem.notes || '—' }}</span>
      <span v-if="yieldItem.remainingAmount !== undefined" class="text-xs">Remaining: <b>{{ yieldItem.remainingAmount }}</b></span>
    </div>
    <div class="flex gap-2 mt-2">
      <button class="btn btn-sm btn-secondary" @click="$emit('edit', yieldItem)">Edit</button>
      <button class="btn btn-sm btn-danger" @click="$emit('delete', yieldItem)">Delete</button>
      <button class="btn btn-sm btn-primary" @click="$emit('list', yieldItem)">List for Sale</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'YieldRecordCard',
  props: {
    yieldItem: Object,
  },
  methods: {
    formatDate(date) {
      if (!date) return '-';
      try { return new Date(date).toLocaleDateString(); } catch { return String(date); }
    },
  },
};
</script>

<style scoped>
.yield-record-card {
  min-width: 220px;
  max-width: 400px;
}
.badge {
  font-size: 0.8rem;
  padding: 0.15rem 0.7rem;
  border-radius: 9999px;
  font-weight: 600;
}
.btn {
  background: #e0e7ef;
  color: #1e293b;
  border: none;
  border-radius: 0.375rem;
  padding: 0.25rem 0.7rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-primary {
  background: #2563eb;
  color: #fff;
}
.btn-secondary {
  background: #e0e7ef;
  color: #1e293b;
}
.btn-danger {
  background: #dc2626;
  color: #fff;
}
.btn-sm {
  font-size: 0.85rem;
  padding: 0.25rem 0.5rem;
}
</style>
