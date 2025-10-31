<template>
  <div class="tw-p-4 tw-border-t tw-flex tw-items-center tw-gap-2">
    <v-text-field v-model="text" dense hide-details placeholder="Type message" @keyup.enter="emitSend" />
    <v-btn small color="primary" @click="emitSend">Send</v-btn>
  </div>
</template>
<script>
export default {
  name: 'InputArea',
  props: {
    isMuted: Boolean, isListening: Boolean, micSupported: Boolean, isWebcamVisible: Boolean,
  },
  data: () => ({ text: '' }),
  methods: { emitSend() { if (!this.text.trim()) return; this.$emit('send-text', this.text); this.text = ''; } },
};
</script>
<template>
  <div class="tw-p-4 tw-border-t tw-flex tw-items-center">
    <v-btn
        @click="$emit('toggle-mute')"
        :color="isMuted ? 'error' : 'success'"
        icon
    >
      <v-icon>{{ isMuted ? 'mdi-microphone-off' : 'mdi-microphone' }}</v-icon>
    </v-btn>

    <v-text-field
        v-model="inputText"
        @keyup.enter="sendMessage"
        placeholder="Type your message..."
        outlined
        dense
        class="tw-mx-2"
    ></v-text-field>

    <v-btn
        @click="sendMessage"
        color="primary"
        :disabled="!inputText.trim()"
    >
      Send
    </v-btn>

    <v-btn
        @click="$emit('toggle-webcam')"
        :color="isWebcamVisible ? 'error' : 'primary'"
        icon
        class="tw-ml-2"
    >
      <v-icon>mdi-video{{ isWebcamVisible ? '-off' : '' }}</v-icon>
    </v-btn>
  </div>
</template>

<script>
export default {
  name: 'InputArea',
  props: {
    isMuted: Boolean,
    isListening: Boolean,
    micSupported: Boolean,
    isWebcamVisible: Boolean,
  },
  data() {
    return {
      inputText: '',
    };
  },
  methods: {
    sendMessage() {
      if (this.inputText.trim()) {
        this.$emit('send-text', this.inputText);
        this.inputText = '';
      }
    },
  },
};
</script>
