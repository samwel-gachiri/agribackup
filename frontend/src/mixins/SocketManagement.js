// export default {
//   data() {
//     return {
//       socket: null,
//     };
//   },
//   methods: {
//     initializeSocket() {
//       // placeholder socket init
//       this.socket = { connected: false };
//     },
//     cleanupSocket() {
//       this.socket = null;
//     },
//   },
// };
import io from 'socket.io-client';
import AudioManagement from '@/mixins/AudioManagement.js';

const SocketManagement = {
  mixins: [AudioManagement],
  methods: {
    initializeSocket() {
      this.socket = io(this.SERVER_URL, {
        reconnectionAttempts: 5,
        transports: ['websocket'],
      });

      // Set up all socket event listeners
      this.socket.on('connect', this.handleSocketConnect);
      this.socket.on('disconnect', this.cleanupSocket);
      // this.socket.on('disconnect', this.handleSocketDisconnect);
      // ... other event listeners
    },

    handleSocketConnect() {
      this.isConnected = true;
      this.statusText = this.isMuted ? 'Connected. Mic is Muted.' : 'Connected. Ready.';
      if (!this.isMuted) {
        // this.startRecognition();
      }
    },

    // Other socket handlers...

    cleanupSocket() {
      if (this.socket) {
        this.socket.disconnect();
        this.socket = null;
      }
    },
  },
};

export default SocketManagement;
