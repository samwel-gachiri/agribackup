<template>
  <div class="tw-flex tw-flex-col tw-min-h-screen">
<!--    <div class="tw-flex tw-flex-row">-->
<!--      &lt;!&ndash; <cozmo :status="visualizerStatus" /> &ndash;&gt;-->
<!--      <teacher :status="visualizerStatus" />-->
<!--    </div>-->

<!--    <ai-visualizer v-if="false" :status="visualizerStatus" />-->
<!--    <status-display :status="statusText" />-->
    <chat-box :messages="messages" />

    <input-area
        :is-muted="isMuted"
        :is-listening="isListening"
        :mic-supported="micSupported"
        :is-webcam-visible="showWebcam"
    />
<!--        @toggle-mute="handleToggleMute"-->
<!--        @send-text="handleSendText"-->
<!--        @toggle-webcam="handleToggleWebcam"-->

<!--    &lt;!&ndash; Widgets Area &ndash;&gt;-->
<!--    <webcam-feed-->
<!--        v-if="showWebcam"-->
<!--        @close="handleToggleWebcam"-->
<!--        :socket="socket"-->
<!--    />-->

<!--    <weather-widget :weather-data="weatherInfo" />-->
<!--    <map-widget :map-data="mapInfo" />-->

<!--    <code-execution-widget-->
<!--        v-if="executableCode"-->
<!--        :code="executableCode"-->
<!--        :language="codeLanguage"-->
<!--        @close="handleCloseCodeWidget"-->
<!--    />-->

<!--    <search-results-widget-->
<!--        v-if="searchInfo"-->
<!--        :search-data="searchInfo"-->
<!--        @close="handleCloseSearchResultsWidget"-->
<!--    />-->

    <footer class="tw-p-4 tw-text-center tw-text-sm">
      <p>Location: Smyrna, Georgia</p>
      <p>Current Time: {{ currentTime }}</p>
    </footer>
  </div>
</template>

<script>
import AudioManagement from '@/mixins/AudioManagement.js';
import SocketManagement from '@/mixins/SocketManagement.js';
import ChatBox from '@/components/ai/ChatBox.vue';
import InputArea from '@/components/ai/InputArea.vue';

export default {
  name: 'AppAssistant',
  components: { InputArea, ChatBox },

  mixins: [
    AudioManagement,
    SocketManagement,
    // StatusManagement,
  ],

  data() {
    return {
      SERVER_URL: 'http://localhost:5000',

      // State variables
      isConnected: false,
      isMuted: true,
      statusText: 'Initializing...',
      messages: [],
      isListening: false,
      micSupported: false,
      weatherInfo: null,
      mapInfo: null,
      visualizerStatus: 'idle', // Will be matched with VISUALIZER_STATUS
      showWebcam: false,
      executableCode: null,
      codeLanguage: null,
      searchInfo: null,
      currentTime: this.getCurrentTime(),

      // Refs as data properties
      socket: null,
      recognition: null,
      audioContext: null,
      audioQueue: [],
      isPlaying: false,
      userRequestedStop: false,
      restartTimer: null,
      adaMessageIndex: -1,
    };
  },

  created() {
    this.initializeSocket();
    // this.initializeSpeechRecognition();
    this.initializeAudio();

    // Time updater
    this.timeInterval = setInterval(() => {
      this.currentTime = this.getCurrentTime();
    }, 1000);
  },

  beforeDestroy() {
    clearInterval(this.timeInterval);
    this.cleanupSocket();
    // this.cleanupSpeechRecognition();
    this.cleanupAudio();
  },

  methods: {
    getCurrentTime() {
      return new Date().toLocaleString('en-US', {
        timeZone: 'America/New_York',
        hour: 'numeric',
        minute: 'numeric',
        second: 'numeric',
        hour12: true,
      });
    },
    // Other methods will be in mixins
  },
};
</script>

<style lang="scss">
</style>
