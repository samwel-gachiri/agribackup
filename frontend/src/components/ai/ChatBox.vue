<template>
  <div class="tw-p-4 tw-overflow-y-auto tw-flex-1">
    <div v-for="(m,i) in messages" :key="i" class="tw-mb-2">
      <div class="tw-text-xs tw-text-gray-500">{{ new Date(m.time||Date.now()).toLocaleTimeString() }}</div>
      <div class="tw-px-3 tw-py-2 tw-rounded tw-bg-gray-100">{{ m.text }}</div>
    </div>
  </div>
</template>
<script>
export default { name: 'ChatBox', props: { messages: { type: Array, default: () => [] } } };
</script>
<template>
  <div class="tw-flex-1 tw-overflow-y-auto tw-p-4">
    <div
        v-for="(message, index) in messages"
        :key="index"
        class="tw-mb-3"
        :class="{
        'tw-text-right': message.sender === 'user',
        'tw-text-left': message.sender === 'ada'
      }"
    >
      <div
          class="tw-inline-block tw-p-2 tw-rounded-lg"
          :class="{
          'tw-bg-blue-500 tw-text-white': message.sender === 'user',
          'tw-bg-gray-200': message.sender === 'ada'
        }"
      >
        {{ message.text }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatBox',
  props: {
    messages: {
      type: Array,
      required: true,
    },
  },
};
</script>
