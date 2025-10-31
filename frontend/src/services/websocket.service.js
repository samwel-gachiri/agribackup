import io from 'socket.io-client';

class WebSocketService {
  constructor(url) {
    this.socket = io(url);
    this.listeners = {};
  }

  on(event, callback) {
    this.socket.on(event, callback);
    if (!this.listeners[event]) {
      this.listeners[event] = [];
    }
    this.listeners[event].push(callback);
  }

  emit(event, data) {
    this.socket.emit(event, data);
  }

  disconnect() {
    Object.keys(this.listeners).forEach((event) => {
      this.listeners[event].forEach((callback) => {
        this.socket.off(event, callback);
      });
    });
    this.socket.disconnect();
  }
}

export default WebSocketService;
