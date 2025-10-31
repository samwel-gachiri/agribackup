<template>
  <div class="harvest-timeline">
    <h3 class="font-semibold mb-2">Harvest Timeline</h3>
    <ul class="timeline-list">
      <li v-for="event in events" :key="event.date" class="timeline-event">
        <div class="dot" :class="eventColor(event.type)"></div>
        <div class="event-content">
          <div class="event-title">{{ event.label }}</div>
          <div class="event-date">{{ formatDate(event.date) }}</div>
          <div v-if="event.notes" class="event-notes">{{ event.notes }}</div>
        </div>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'HarvestTimeline',
  props: {
    events: {
      type: Array,
      required: true,
      // [{ type: 'planting'|'growth'|'harvest'|'listing', label, date, notes }]
    },
  },
  methods: {
    formatDate(date) {
      if (!date) return '-';
      try { return new Date(date).toLocaleDateString(); } catch { return String(date); }
    },
    eventColor(type) {
      switch (type) {
        case 'planting': return 'bg-yellow-400';
        case 'growth': return 'bg-blue-400';
        case 'harvest': return 'bg-green-500';
        case 'listing': return 'bg-purple-500';
        default: return 'bg-gray-300';
      }
    },
  },
};
</script>

<style scoped>
.harvest-timeline {
  padding: 1rem 0;
}
.timeline-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.timeline-event {
  display: flex;
  align-items: flex-start;
  margin-bottom: 1.2rem;
}
.dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  margin-right: 1rem;
  margin-top: 0.2rem;
}
.event-content {
  flex: 1;
}
.event-title {
  font-weight: 600;
  font-size: 1rem;
}
.event-date {
  font-size: 0.85rem;
  color: #64748b;
}
.event-notes {
  font-size: 0.85rem;
  color: #475569;
  margin-top: 0.2rem;
}
</style>
