// export default {
//   data() {
//     return {
//       audioContext: null,
//     };
//   },
//   methods: {
//     initializeAudio() {
//       try {
//         if (!this.audioContext) {
//           const Ctx = window.AudioContext || window.webkitAudioContext;
//           this.audioContext = new Ctx();
//         }
//       } catch (e) {
//         // silent fail
//       }
//     },
//     cleanupAudio() {
//       try { this.audioContext && this.audioContext.close(); } catch (e) { /* noop */ }
//       this.audioContext = null;
//     },
//   },
// };
const AudioManagement = {
  methods: {
    initializeAudio() {
      if (!this.audioContext) {
        try {
          const AudioCtx = window.AudioContext || window.webkitAudioContext;
          if (!AudioCtx) {
            this.$toast.error('Web Audio API not supported.');
            this.statusText = `${this.statusText} (Audio Playback Not Supported)`;
            return false;
          }
          this.audioContext = new AudioCtx({ sampleRate: 24000 });
          if (this.audioContext.state === 'suspended') {
            this.$toast.warning('AudioContext suspended...');
            this.statusText = 'Click page or button to enable audio.';
          }
          return true;
        } catch (e) {
          this.$toast.error('Error connecting to audiosocket', e.message);
          this.statusText = 'Browser Audio Error.';
          return false;
        }
      }
      return true;
    },

    async resumeAudioContext() {
      if (this.audioContext && this.audioContext.state === 'suspended') {
        try {
          // console.log('Attempting to resume AudioContext...');
          await this.audioContext.resume();
          // console.log('AudioContext resumed. State:', this.audioContext.state);
          if (this.statusText === 'Click page or button to enable audio.') {
            this.statusText = 'Audio enabled.';
          }
          if (this.audioQueue.length > 0 && !this.isPlaying) {
            this.playNextAudioChunk();
          }
        } catch (e) {
          this.$toast.error('Error resuming audio context', e.message);
          this.statusText = 'Failed to enable audio.';
        }
      }
    },

    async playNextAudioChunk() {
      // Implementation similar to React version
    },

    cleanupAudio() {
      if (this.audioContext) {
        this.audioContext.close()
          .then(() => this.$toast.show('AudioContext closed successfully.'))
          .catch((e) => this.$toast.error('Error closing AudioContext:', e.message));
        this.audioContext = null;
      }
    },
  },

  beforeDestroy() {
    this.cleanupAudio();
  },
};

export default AudioManagement;
