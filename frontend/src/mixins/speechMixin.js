export default {
  data() {
    return {
      isListening: false,
      lastTranscript: '',
      speechRecognition: null,
    };
  },

  methods: {
    initSpeech() {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

      if (!SpeechRecognition) {
        // console.warn('Speech API unavailable');
        return;
      }

      this.speechRecognition = new SpeechRecognition();
      this.speechRecognition.continuous = false;
      this.speechRecognition.interimResults = true;

      this.speechRecognition.onresult = (event) => {
        const transcript = event.results[0][0].transcript;
        if (event.results[0].isFinal) {
          this.lastTranscript = transcript;
          this.$emit('speech-text', transcript); // Emit to parent
          this.processCommand(transcript); // Or call your AI directly
        }
      };

      this.speechRecognition.onend = () => {
        if (this.isListening) this.startListening(); // Auto-restart
      };
    },

    startListening() {
      if (!this.speechRecognition) this.initSpeech();
      this.isListening = true;
      this.speechRecognition.start();
    },

    stopListening() {
      this.isListening = false;
      this.speechRecognition.onend = null; // Disable auto-restart
      this.speechRecognition.stop();
    },

    speak(text) {
      if (window.speechSynthesis.speaking) {
        window.speechSynthesis.cancel();
      }
      const utterance = new SpeechSynthesisUtterance(text);
      window.speechSynthesis.speak(utterance);
    },
  },

  beforeDestroy() {
    if (this.speechRecognition) {
      this.speechRecognition.stop();
    }
    window.speechSynthesis.cancel();
  },
};
