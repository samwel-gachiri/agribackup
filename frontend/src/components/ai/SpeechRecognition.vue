<!--<template>-->
<!--  <div>-->
<!--    <button-->
<!--        @click="toggleListening"-->
<!--        :disabled="!isSupported"-->
<!--        :class="{ 'listening': isListening }"-->
<!--    >-->
<!--      {{ buttonText }}-->
<!--    </button>-->
<!--    <p>Status: {{ status }}</p>-->
<!--    <p v-if="transcript">You said: {{ transcript }}</p>-->
<!--  </div>-->
<!--</template>-->

<!--<script>-->
<!--export default {-->
<!--  name: 'SpeechRecognition',-->
<!--  data() {-->
<!--    return {-->
<!--      isListening: false,-->
<!--      isSupported: false,-->
<!--      status: 'Initializing...',-->
<!--      transcript: '',-->
<!--      recognition: null,-->
<!--    };-->
<!--  },-->
<!--  computed: {-->
<!--    buttonText() {-->
<!--      if (!this.isSupported) return 'Speech API Not Supported';-->
<!--      return this.isListening ? 'Stop Listening' : 'Start Listening';-->
<!--    },-->
<!--  },-->
<!--  mounted() {-->
<!--    this.initSpeechRecognition();-->
<!--  },-->
<!--  beforeDestroy() {-->
<!--    this.stopRecognition();-->
<!--  },-->
<!--  methods: {-->
<!--    initSpeechRecognition() {-->
<!--      // Feature detection-->
<!--      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;-->

<!--      if (!SpeechRecognition) {-->
<!--        this.status = 'Speech recognition not supported';-->
<!--        return;-->
<!--      }-->

<!--      this.isSupported = true;-->
<!--      this.recognition = new SpeechRecognition();-->
<!--      this.recognition.continuous = false; // Process speech when user pauses-->
<!--      this.recognition.interimResults = true; // Get interim results-->
<!--      this.recognition.lang = 'en-US';-->

<!--      // Event handlers-->
<!--      this.recognition.onstart = () => {-->
<!--        this.isListening = true;-->
<!--        this.status = 'Listening...';-->
<!--      };-->

<!--      this.recognition.onresult = (event) => {-->
<!--        const transcript = Array.from(event.results)-->
<!--          .map((result) => result[0])-->
<!--          .map((result) => result.transcript)-->
<!--          .join('');-->

<!--        this.transcript = transcript;-->

<!--        // Send to parent component or backend when final result-->
<!--        if (event.results[0].isFinal) {-->
<!--          this.$emit('transcript', transcript);-->
<!--        }-->
<!--      };-->

<!--      this.recognition.onerror = (event) => {-->
<!--        console.error('Recognition error:', event.error);-->
<!--        this.status = `Error: ${event.error}`;-->
<!--        this.isListening = false;-->
<!--      };-->

<!--      this.recognition.onend = () => {-->
<!--        if (this.isListening) {-->
<!--          // Auto-restart if not manually stopped-->
<!--          setTimeout(() => this.recognition.start(), 300);-->
<!--        }-->
<!--      };-->
<!--    },-->

<!--    toggleListening() {-->
<!--      if (this.isListening) {-->
<!--        this.stopRecognition();-->
<!--      } else {-->
<!--        this.startRecognition();-->
<!--      }-->
<!--    },-->

<!--    startRecognition() {-->
<!--      if (this.recognition) {-->
<!--        this.recognition.start();-->
<!--      }-->
<!--    },-->

<!--    stopRecognition() {-->
<!--      this.isListening = false;-->
<!--      this.status = 'Ready';-->
<!--      if (this.recognition) {-->
<!--        this.recognition.stop();-->
<!--      }-->
<!--    },-->
<!--  },-->
<!--};-->
<!--</script>-->

<!--<style scoped>-->
<!--button.listening {-->
<!--  background-color: red;-->
<!--  color: white;-->
<!--}-->
<!--</style>-->
