<template>
  <v-dialog v-model="dialog" max-width="500" persistent>
<!--    <AppAssistant v-if="false"/>-->
    <v-card class="ai-assistant-card">
      <v-toolbar color="primary" dark flat>
        <v-avatar size="40" class="mr-2">
          <v-img src="@/assets/images/ai_avatar.png"></v-img>
        </v-avatar>
        <v-toolbar-title>AgriAI Assistant</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn icon @click="dialog = false">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-toolbar>

      <div class="ai-conversation">
        <div v-for="(message, index) in conversation" :key="index" :class="['message', message.user]">
          <div class="message-content">
            <div class="message-bubble">
              <div v-if="message.user === 'ai'" class="ai-avatar">
                <v-icon color="primary">mdi-robot</v-icon>
              </div>
              <div class="text">
                {{ message.text }}
                <div v-if="message.user === 'ai' && message.actions" class="quick-actions">
                  <v-chip
                      v-for="(action, i) in message.actions"
                      :key="i"
                      small
                      class="mr-1 mb-1"
                      @click="handleQuickAction(action)"
                  >
                    {{ action }}
                  </v-chip>
                </div>
                <div v-if="message.type === 'PRODUCES'" class="tw-flex">
                  <v-card
                      rounded
                      v-for="(product, i) in message.data" :key="i">
                    <v-card-title>{{product.name}}</v-card-title>
                    <div class="tw-px-3">
                      <div class="tw-flex tw-flex-row tw-items-center tw-justify-start">
                        <v-icon>mdi-face</v-icon>
                        <p>{{product.farmer.name}}</p>
                      </div>
                      <div class="tw-flex tw-flex-row tw-items-center tw-justify-start">
                        <v-icon>mdi-home-map-marker</v-icon>
                        <p>{{product.location.customName.split(',')[0]}}</p>
                        <h4>{{product.distance}}</h4>
                      </div>
                    </div>
                    <v-card-actions>
                      <button class="btn btn-primary">Request</button>
                    </v-card-actions>
                  </v-card>
                </div>
              </div>
              <div v-if="message.user === 'user'" class="user-avatar">
                <v-icon color="green">mdi-account</v-icon>
              </div>
            </div>
            <div class="message-time">
              {{ message.time | formatDate }}
            </div>
          </div>
        </div>

        <div v-if="isListening" class="listening-indicator">
          <div class="pulse"></div>
          <v-icon color="red">mdi-microphone</v-icon>
          <span>Listening...</span>
        </div>
      </div>

      <v-divider></v-divider>

      <div class="ai-input">
        <v-text-field
            v-model="userInput"
            placeholder="Ask me anything about your farm..."
            outlined
            rounded
            dense
            hide-details
            @keyup.enter="sendMessageFromInput"
            ref="inputField"
        >
          <template v-slot:append>
            <v-btn icon @click="toggleVoice" :color="isListening ? 'red' : 'primary'">
              <v-icon>mdi-microphone</v-icon>
            </v-btn>
            <v-btn icon color="primary" @click="sendMessageFromInput">
              <v-icon>mdi-send</v-icon>
            </v-btn>
          </template>
        </v-text-field>
      </div>
    </v-card>
  </v-dialog>
</template>

<script>
// import AppAssistant from '@/components/ai/AIApp.vue';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import L from 'leaflet';
import axios from 'axios';
import { getCurrentUserId, getCurrentUserRole } from '@/utils/roles.js';
// eslint-disable-next-line import/extensions
import helperMixins from '@/mixins/helperMixins';

export default {
  name: 'AiAssistant',
  // components: { AppAssistant },
  data() {
    return {
      dialog: false,
      socket: null,
      stompClient: null,
      userInput: '',
      isListening: false,
      wsUrl: '/ws',
      speechRecognition: null,
      connection: null,
      conversation: [
        {
          user: 'ai',
          text: 'Hello! I\'m your AgriAI assistant. How can I help you with your farm today?',
          time: new Date(),
          type: 'NORMAL',
          actions: ['Field status', 'Irrigation report', 'Pest alerts'],
        },
      ],
      // Add these to your data()
      audioContext: null,
      audioQueue: [],
      isPlaying: false,
      audioBufferSource: null,
    };
  },
  methods: {
    getCurrentUserId,
    getCurrentUserRole,
    openDialog() {
      this.dialog = true;
    },
    connectToSocket() {
      try {
        const userId = getCurrentUserId();
        const socket = new SockJS(`${this.wsUrl}?userId=${userId}`);
        this.stompClient = Stomp.over(socket);
        this.stompClient.onConnect = () => this.onWebSocketConnected();
        this.stompClient.onDisconnect = (error) => this.onWSError(error);
        this.stompClient.connect({ userId }, this.onWebSocketConnected, this.onWSError);
      } catch (e) {
        this.$toast.error('Error during connection', e.message);
      }
    },

    onWebSocketConnected() {
      // const joinMessage = {
      //   type: 'JOIN',
      //   sender: getCurrentUserId(),
      // };

      // Subscribe to text responses
      this.stompClient.subscribe('/user/ai/chat', this.onMsgReceivedFromWebSocket);

      this.stompClient.subscribe('/user/ai/chat/request-location', () => {
        // eslint-disable-next-line no-restricted-globals,no-alert
        if (confirm('Let me get your location to assist you')) {
          // get location details
          navigator.geolocation.getCurrentPosition(async (position) => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;
            // get location name
            const locationNameResponse = await fetch(
              `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`,
            );
            const data = await locationNameResponse.json();
            const customName = data.display_name || 'Unknown Location';
            // save location
            axios.put(`/${this.getCurrentUserRole()}s-service/location/${this.getCurrentUserRole()}`, {
              [`${this.getCurrentUserRole()}Id`]: getCurrentUserId(),
              locationDto: {
                latitude: lat,
                longitude: lng,
                customName,
              },
            }).then((saveLocationResponse) => {
              if (saveLocationResponse.data.success === true) {
                this.$toast.success('Location updated successfully!');
              } else {
                this.$toast.error('Failed to update location', saveLocationResponse.data.msg);
              }
            }).catch((error) => {
              this.sendMsgToWebsocket(error.message);
              this.$toast.error(error.message);
            }).finally(() => {
              this.sendMsgToWebsocket(`location:name=${customName},latitude=${lat},longitude=${lng}`);
            });
            if (this.marker) {
              this.marker.setLatLng([position.coords.latitude, position.coords.longitude]);
            } else {
              this.marker = L.marker([position.coords.latitude, position.coords.longitude]).addTo(this.map);
            }
          },
          (positionError) => {
            this.$toast.error(positionError.message);
          });
        }
      });

      this.stompClient.subscribe('/user/ai/chat/display-farm-produce', (data) => {
        this.conversation.push({
          user: 'ai',
          text: 'The following are farmers related produces',
          time: new Date(),
          type: 'PRODUCES',
          data: JSON.parse(data.body),
        });
      });
      // Subscribe to binary audio responses
      this.stompClient.subscribe('/user/queue/audio', this.onAudioChunkReceived);

      // this.stompClient.send('/user/chat.addUser',
      //   {},
      //   JSON.stringify(joinMessage));

      // // updateNotificationDisplay();
      // this.stompClient.subscribe('/topic/messages', (message) => {
      //   // showMessage(JSON.parse(message.body).content);
      //   this.$toast.success(JSON.parse(message.body).content, 'public msg');
      // });

      // this.stompClient.subscribe('/user/topic/private-messages', (message) => {
      //   // showMessage(JSON.parse(message.body).content);
      //   this.$toast.success(JSON.parse(message.body).content, 'private msg');
      // });

      // this.stompClient.subscribe('/topic/global-notifications', (message) => {
      //   // notificationCount = notificationCount + 1;
      //   // updateNotificationDisplay();
      // });
      //
      // this.stompClient.subscribe('/user/topic/private-notifications', (message) => {
      //   // notificationCount = notificationCount + 1;
      //   // updateNotificationDisplay();
      // });
    },

    onWSError(error) {
      this.$toast.error('Failed to connect to websocket', error.message);
    },

    sendMsgToWebsocket(msg) {
      if (msg && this.stompClient) {
        const chatMessage = {
          sender: getCurrentUserId(),
          userSection: getCurrentUserRole(),
          content: msg,
          type: 'CHAT',
          responseType: 'auio', // Add this to indicate you want audio response
        };
        this.stompClient.send('/app/chat.sendPrompt', {}, JSON.stringify(chatMessage));
        // this.stompClient.send('/app/chat.sendPrompt', {}, JSON.stringify(chatMessage));
        // this.stompClient.send('/app/message', {}, JSON.stringify({ messageContent: 'Hey' }));
        this.stompClient.send('/app/private-message', {}, JSON.stringify({ messageContent: 'Hey private' }));
      }
    },

    onMsgReceivedFromWebSocket(payload) {
      try {
        if (!payload) return;
        const message = JSON.parse(payload.body);
        // const sender = message.sender;
        const content = message.content;
        if (content == null) return;
        this.conversation.push({
          type: 'ai',
          text: content,
          time: new Date(),
        });
      } catch (e) {
        this.$toast.error('Error parsing WebSocket message:', e.message);
      }
    },
    onAudioChunkReceived(payload) {
      try {
        // Add the chunk to the queue
        this.audioChunks.push(payload.body);

        // Start playing if not already
        if (!this.isAudioPlaying) {
          this.playNextChunk();
        }
      } catch (e) {
        this.$toast.error('Error processing audio chunk:', e.message);
      }
    },
    sendMessageFromInput() {
      if (!this.userInput.trim()) return;

      // Add user message
      this.conversation.push({
        type: 'user',
        text: this.userInput,
        time: new Date(),
      });
      this.sendMsgToWebsocket(this.userInput);

      this.userInput = '';
      this.scrollToBottom();
    },
    // The following are for speech recognition
    initSpeech() {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

      if (!SpeechRecognition) {
        this.$toast.warning('Speech API unavailable');
        return;
      }

      this.speechRecognition = new SpeechRecognition();
      this.speechRecognition.continuous = false;
      this.speechRecognition.interimResults = true;

      this.speechRecognition.onresult = (event) => {
        const transcript = event.results[0][0].transcript;
        if (event.results[0].isFinal) {
          // this.lastTranscript = transcript;
          // this.$emit('speech-text', transcript); // Emit to parent
          // this.processCommand(transcript); // Or call your AI directly
          // this.$toast.success(transcript);
          // this.sendMsgToWebsocket(transcript);
          this.userInput = transcript;
          this.sendMessageFromInput();
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
    toggleVoice() {
      this.isListening = !this.isListening;
      if (this.isListening) {
        this.startListening();
      } else {
        this.stopListening();
      }
    },
    // Add these methods
    initAudioContext() {
      if (!this.audioContext) {
        this.audioContext = new (window.AudioContext || window.webkitAudioContext)({
          sampleRate: 24000, // Match ElevenLabs output
        });
      }
    },
    async playAudioChunk(chunk) {
      if (!this.audioContext) this.initAudioContext();

      try {
        // Decode and play the chunk
        const audioBuffer = await this.audioContext.decodeAudioData(chunk);
        const source = this.audioContext.createBufferSource();
        source.buffer = audioBuffer;
        source.connect(this.audioContext.destination);
        source.start();

        source.onended = () => {
          this.isAudioPlaying = false;
          this.playNextChunk();
        };

        this.isAudioPlaying = true;
      } catch (error) {
        this.$toast.error('Error playing audio chunk:', error.message);
      }
    },
    playNextChunk() {
      if (this.audioChunks.length > 0 && !this.isAudioPlaying) {
        const chunk = this.audioChunks.shift();
        this.playAudioChunk(chunk);
      }
    },
    async playAudioStream(audioData) {
      if (!this.audioContext) {
        this.initAudioContext();
      }

      try {
        // Decode the audio data
        const audioBuffer = await this.audioContext.decodeAudioData(audioData);

        // Create a buffer source
        const source = this.audioContext.createBufferSource();
        source.buffer = audioBuffer;
        source.connect(this.audioContext.destination);

        // Play the audio
        source.start();

        // Store the source so we can stop it if needed
        this.audioBufferSource = source;

        source.onended = () => {
          this.isPlaying = false;
          this.playNextInQueue();
        };

        this.isPlaying = true;
      } catch (error) {
        this.$toast.error('Error playing audio', error.message);
      }
    },

    playNextInQueue() {
      if (this.audioQueue.length > 0 && !this.isPlaying) {
        const nextAudio = this.audioQueue.shift();
        this.playAudioStream(nextAudio);
      }
    },

    queueAudio(audioData) {
      this.audioQueue.push(audioData);
      if (!this.isPlaying) {
        this.playNextInQueue();
      }
    },

    stopAudio() {
      if (this.audioBufferSource) {
        this.audioBufferSource.stop();
        this.isPlaying = false;
      }
      this.audioQueue = [];
    },
    // Add this new method for handling audio
    onAudioReceived(payload) {
      try {
        // Convert the binary payload to ArrayBuffer
        const audioData = new Uint8Array(payload.body).buffer;
        this.queueAudio(audioData);
      } catch (e) {
        this.$toast.error('Error processing audio', e.message);
      }
    },
    handleQuickAction(action) {
      this.userInput = action;
      this.sendMessageFromInput();
    },
    scrollToBottom() {
      this.$nextTick(() => {
        const container = document.querySelector('.ai-conversation');
        container.scrollTop = container.scrollHeight;
      });
    },
  },
  mixins: [helperMixins],
  beforeDestroy() {
    if (this.socket) {
      this.socket.close();
    }
  },
  watch: {
    dialog(val) {
      if (val) {
        this.initSpeech();
        this.connectToSocket();

        this.$nextTick(() => {
          this.scrollToBottom();
          this.$refs.inputField.focus();
        });
      } else {
        this.stompClient.disconnect(() => {
        });
      }
    },
  },
};
</script>

<style scoped>
.ai-assistant-card {
  border-radius: 16px !important;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.95) !important;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2) !important;
}

.ai-conversation {
  padding: 16px;
  max-height: 400px;
  overflow-y: auto;
  background: linear-gradient(to bottom, rgba(245, 250, 255, 0.8), rgba(240, 248, 255, 0.9));
}

.message {
  margin-bottom: 16px;
  display: flex;
}

.message.ai {
  justify-content: flex-start;
}

.message.user {
  justify-content: flex-end;
}

.message-content {
  max-width: 80%;
}

.message-bubble {
  display: flex;
  align-items: flex-end;
  padding: 12px 16px;
  border-radius: 18px;
  position: relative;
}

.ai .message-bubble {
  background: rgba(230, 240, 255, 0.9);
  border-bottom-left-radius: 4px;
}

.user .message-bubble {
  background: rgba(220, 250, 230, 0.9);
  border-bottom-right-radius: 4px;
  flex-direction: row-reverse;
}

.ai-avatar, .user-avatar {
  margin-right: 8px;
  margin-left: 8px;
  display: flex;
  align-items: center;
}

.text {
  flex-grow: 1;
}

.message-time {
  font-size: 0.7rem;
  color: #666;
  margin-top: 4px;
  text-align: right;
}

.ai .message-time {
  text-align: left;
}

.quick-actions {
  margin-top: 8px;
}

.ai-input {
  padding: 12px;
}

.listening-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  background: rgba(255, 0, 0, 0.1);
  border-radius: 20px;
  margin: 8px 0;
}

.listening-indicator span {
  margin-left: 8px;
}

.pulse {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: red;
  margin-right: 8px;
  animation: pulse-animation 1.5s infinite;
}

@keyframes pulse-animation {
  0% {
    transform: scale(0.8);
    opacity: 0.7;
  }
  70% {
    transform: scale(1.3);
    opacity: 0.3;
  }
  100% {
    transform: scale(0.8);
    opacity: 0.7;
  }
}

.v-chip {
  cursor: pointer;
  transition: all 0.2s;
}

.v-chip:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}
</style>
