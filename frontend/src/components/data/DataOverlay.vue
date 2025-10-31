<template>
  <div class="data-overlay">
    <div class="overlay-container" :class="{ 'overlay-active': active }">
      <!-- Farm Status Summary -->
      <div class="data-card farm-status">
        <div class="card-header">
          <v-icon color="primary" class="mr-2">mdi-sprout</v-icon>
          <span>Farm Status</span>
        </div>
        <div class="card-content">
          <div class="status-item">
            <div class="status-value">78%</div>
            <div class="status-label">Crop Health</div>
            <v-progress-linear
                :value="78"
                height="8"
                color="green"
                background-color="green lighten-4"
                rounded
            ></v-progress-linear>
          </div>
          <div class="status-item">
            <div class="status-value">64%</div>
            <div class="status-label">Soil Moisture</div>
            <v-progress-linear
                :value="64"
                height="8"
                color="blue"
                background-color="blue lighten-4"
                rounded
            ></v-progress-linear>
          </div>
          <div class="status-item">
            <div class="status-value">92%</div>
            <div class="status-label">Equipment Status</div>
            <v-progress-linear
                :value="92"
                height="8"
                color="orange"
                background-color="orange lighten-4"
                rounded
            ></v-progress-linear>
          </div>
        </div>
      </div>

      <!-- Weather Forecast -->
      <div class="data-card weather-forecast">
        <div class="card-header">
          <v-icon color="primary" class="mr-2">mdi-weather-partly-cloudy</v-icon>
          <span>Weather Forecast</span>
        </div>
        <div class="card-content">
          <div class="weather-grid">
            <div v-for="(day, index) in forecast" :key="index" class="weather-day">
              <div class="day-name">{{ day.day }}</div>
              <v-icon large>{{ day.icon }}</v-icon>
              <div class="temp-range">
                <span class="high">{{ day.high }}°</span>
                <span class="low">{{ day.low }}°</span>
              </div>
              <div class="precip">{{ day.precip }}%</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Sensor Network -->
      <div class="data-card sensor-network">
        <div class="card-header">
          <v-icon color="primary" class="mr-2">mdi-access-point-network</v-icon>
          <span>Sensor Network</span>
        </div>
        <div class="card-content">
          <div class="sensor-grid">
            <div v-for="(sensor, index) in sensors" :key="index" class="sensor-item">
              <div class="sensor-icon" :class="sensor.status">
                <v-icon small>{{ sensor.icon }}</v-icon>
              </div>
              <div class="sensor-info">
                <div class="sensor-name">{{ sensor.name }}</div>
                <div class="sensor-value">{{ sensor.value }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Toggle Button -->
      <v-btn
          fab
          small
          color="primary"
          class="toggle-button"
          @click="active = !active"
      >
        <v-icon>{{ active ? 'mdi-chevron-right' : 'mdi-chevron-left' }}</v-icon>
      </v-btn>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DataOverlay',
  data() {
    return {
      active: true,
      forecast: [
        {
          day: 'Today', icon: 'mdi-weather-sunny', high: 28, low: 18, precip: 10,
        },
        {
          day: 'Tue', icon: 'mdi-weather-partly-cloudy', high: 26, low: 17, precip: 20,
        },
        {
          day: 'Wed', icon: 'mdi-weather-pouring', high: 22, low: 16, precip: 80,
        },
        {
          day: 'Thu', icon: 'mdi-weather-cloudy', high: 24, low: 15, precip: 40,
        },
        {
          day: 'Fri', icon: 'mdi-weather-sunny', high: 27, low: 16, precip: 0,
        },
      ],
      sensors: [
        {
          name: 'Soil Moisture A', icon: 'mdi-water', value: '64%', status: 'normal',
        },
        {
          name: 'Soil Temp B', icon: 'mdi-thermometer', value: '22°C', status: 'normal',
        },
        {
          name: 'Air Quality', icon: 'mdi-air-filter', value: 'Good', status: 'normal',
        },
        {
          name: 'Irrigation C', icon: 'mdi-sprinkler', value: 'Active', status: 'warning',
        },
        {
          name: 'Solar Panel', icon: 'mdi-solar-power', value: '78%', status: 'normal',
        },
        {
          name: 'Drone Battery', icon: 'mdi-drone', value: '42%', status: 'alert',
        },
      ],
    };
  },
};
</script>

<style scoped>
.data-overlay {
  position: fixed;
  top: 0;
  right: 0;
  height: 100vh;
  z-index: 100;
  display: flex;
  align-items: center;
}

.overlay-container {
  position: relative;
  height: 90vh;
  width: 300px;
  margin-right: -260px;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overlay-active {
  margin-right: 0;
}

.data-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  transition: all 0.3s ease;
  flex: 1;
}

.card-header {
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.02);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  font-weight: 600;
  color: var(--v-primary-base);
}

.card-content {
  padding: 16px;
}

/* Farm Status Styles */
.status-item {
  margin-bottom: 16px;
}

.status-value {
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--v-primary-base);
}

.status-label {
  font-size: 0.8rem;
  color: #666;
  margin-bottom: 4px;
}

/* Weather Forecast Styles */
.weather-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
}

.weather-day {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.day-name {
  font-weight: 600;
  font-size: 0.8rem;
}

.temp-range {
  display: flex;
  width: 100%;
  justify-content: space-between;
  font-size: 0.7rem;
}

.high {
  color: #ff6b6b;
}

.low {
  color: #74b9ff;
}

.precip {
  font-size: 0.6rem;
  color: #0984e3;
}

/* Sensor Network Styles */
.sensor-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.sensor-item {
  display: flex;
  align-items: center;
  padding: 6px;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.02);
}

.sensor-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
}

.sensor-icon.normal {
  background: rgba(0, 200, 83, 0.1);
  color: #00c853;
}

.sensor-icon.warning {
  background: rgba(255, 171, 0, 0.1);
  color: #ffab00;
}

.sensor-icon.alert {
  background: rgba(255, 82, 82, 0.1);
  color: #ff5252;
}

.sensor-name {
  font-size: 0.7rem;
  color: #666;
}

.sensor-value {
  font-size: 0.9rem;
  font-weight: 600;
}

/* Toggle Button */
.toggle-button {
  position: absolute;
  left: -20px;
  top: 50%;
  transform: translateY(-50%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: -1;
}

/* Animations */
.data-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}
</style>
