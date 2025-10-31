<template>
  <div>
    <!-- Settings Menu Button -->
    <v-menu
        v-model="menuOpen"
        :close-on-content-click="false"
        :nudge-width="300"
        offset-y
    >
      <template v-slot:activator="{ on, attrs }">
        <v-btn
            icon
            v-bind="attrs"
            v-on="on"
            :disabled="disabled"
            class="tw-relative"
        >
          <v-icon>mdi-cog</v-icon>
          <!-- Active camera indicator -->
<!--          <v-badge-->
<!--              v-if="selectedCameraId"-->
<!--              dot-->
<!--              color="success"-->
<!--              overlap-->
<!--              offset-x="8"-->
<!--              offset-y="8"-->
<!--          >-->
<!--          </v-badge>-->
        </v-btn>
      </template>

      <v-card class="tw-min-w-0" style="min-width: 320px;">
        <v-card-title class="tw-py-3">
          <v-icon class="tw-mr-2">mdi-camera</v-icon>
          Camera Settings
        </v-card-title>

        <v-divider></v-divider>

        <v-card-text class="tw-p-0">
          <!-- Camera List -->
          <v-list v-if="availableCameras.length > 0" dense>
            <v-list-item
                v-for="camera in availableCameras"
                :key="camera.id"
                @click="onCameraSelect(camera.id)"
                :class="{ 'v-list-item--active': camera.id === selectedCameraId }"
            >
              <v-list-item-icon>
                <v-icon :color="camera.id === selectedCameraId ? 'primary' : 'default'">
                  {{ getCameraIcon(camera.facingMode) }}
                </v-icon>
              </v-list-item-icon>

              <v-list-item-content>
                <v-list-item-title class="tw-font-medium">
                  {{ camera.label }}
                </v-list-item-title>
                <v-list-item-subtitle class="tw-text-xs tw-capitalize">
                  {{ camera.facingMode === 'unknown' ? 'Unknown orientation' : camera.facingMode + ' camera' }}
                </v-list-item-subtitle>
              </v-list-item-content>

              <v-list-item-action v-if="camera.id === selectedCameraId">
                <v-chip x-small color="primary">
                  Active
                </v-chip>
              </v-list-item-action>
            </v-list-item>
          </v-list>

          <!-- No cameras available -->
          <div v-else class="tw-p-4">
            <v-alert type="warning" dense>
              <div class="tw-flex tw-items-center">
                <v-icon small class="tw-mr-2">mdi-camera-off</v-icon>
                No cameras detected. Please check your camera permissions.
              </div>
            </v-alert>
          </div>
        </v-card-text>

        <!-- Current Camera Info -->
        <v-divider v-if="selectedCameraInfo"></v-divider>
        <v-card-text v-if="selectedCameraInfo" class="tw-py-2 tw-bg-blue-50">
          <div class="tw-flex tw-items-center tw-text-xs">
            <v-icon small class="tw-mr-2" color="info">mdi-information</v-icon>
            <span>
              <strong>{{ selectedCameraInfo.label }}</strong> -
              {{ selectedCameraInfo.facingMode === 'environment' ? 'Back Camera' :
                selectedCameraInfo.facingMode === 'user' ? 'Front Camera' : 'Camera' }}
            </span>
          </div>
        </v-card-text>

        <!-- Close Button -->
        <v-card-actions class="tw-px-4 tw-py-2">
          <v-spacer></v-spacer>
          <v-btn
              text
              small
              @click="menuOpen = false"
          >
            Close
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-menu>
  </div>
</template>

<script>
export default {
  name: 'CameraSelector',
  props: {
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      availableCameras: [],
      selectedCameraId: '',
      menuOpen: false,
    };
  },
  computed: {
    selectedCameraInfo() {
      return this.availableCameras.find((camera) => camera.id === this.selectedCameraId);
    },
  },

  methods: {
    getCameraIcon(facingMode) {
      switch (facingMode) {
        case 'user':
          return 'mdi-camera-front';
        case 'environment':
          return 'mdi-camera-rear';
        default:
          return 'mdi-camera';
      }
    },

    onCameraSelect(cameraId) {
      this.selectedCameraId = cameraId;
      this.rememberCameraId(cameraId);
      this.$emit('camera-selected', cameraId);
      // Keep menu open so user can see the selection change
      // Optionally close after a short delay
      setTimeout(() => {
        this.menuOpen = false;
      }, 800);
    },

    async getAvailableCameras() {
      try {
        const devices = await navigator.mediaDevices.enumerateDevices();
        this.availableCameras = devices
          .filter((device) => device.kind === 'videoinput')
          .map((device) => ({
            id: device.deviceId,
            label: device.label || `Camera ${this.availableCameras.length + 1}`,
            facingMode: this.detectFacingMode(device.label),
          }));

        // Set default camera with priority order
        if (this.availableCameras.length > 0 && !this.selectedCameraId) {
          // 1st priority: Previously selected camera (from localStorage)
          const rememberedCameraId = this.getRememberedCameraId();
          const rememberedCamera = rememberedCameraId
            ? this.availableCameras.find((camera) => camera.id === rememberedCameraId) : null;

          if (rememberedCamera) {
            this.selectedCameraId = rememberedCamera.id;
          } else {
            // 2nd priority: Back camera
            const backCamera = this.availableCameras.find((camera) => camera.facingMode === 'environment'
                || camera.label.toLowerCase().includes('back'));

            if (backCamera) {
              this.selectedCameraId = backCamera.id;
            } else {
              // 3rd priority: System default camera
              this.selectedCameraId = await this.getDefaultCameraId();
            }
          }
        }
      } catch (error) {
        this.cameraError = 'Failed to detect available cameras';
      }
    },

    async getDefaultCameraId() {
      try {
        // Request default camera (no deviceId constraint)
        const stream = await navigator.mediaDevices.getUserMedia({ video: true });
        const track1 = stream.getVideoTracks()[0];
        const settings = track1.getSettings();
        const defaultCameraId = settings.deviceId;

        // Stop the stream immediately
        stream.getTracks().forEach((track) => track.stop());

        return defaultCameraId;
      } catch (error) {
        this.$toast.error('Could not determine default camera, using first available:', error.message);
        // Fallback to first camera if we can't determine default
        return this.availableCameras.length > 0 ? this.availableCameras[0].id : null;
      }
    },

    // Remember camera selection in localStorage
    getRememberedCameraId() {
      try {
        return localStorage.getItem('selectedCameraId');
      } catch (error) {
        this.$toast.error('Could not access localStorage:', error.message);
        return null;
      }
    },

    rememberCameraId(cameraId) {
      try {
        if (cameraId) {
          localStorage.setItem('selectedCameraId', cameraId);
        } else {
          localStorage.removeItem('selectedCameraId');
        }
      } catch (error) {
        this.$toast.error('Could not save to localStorage:', error.message);
      }
    },

    detectFacingMode(label) {
      const lowerLabel = label.toLowerCase();
      if (lowerLabel.includes('back') || lowerLabel.includes('rear')) {
        return 'environment';
        // eslint-disable-next-line sonarjs/no-same-line-conditional
      } if (lowerLabel.includes('front') || lowerLabel.includes('user')) {
        return 'user';
      }
      return 'unknown';
    },

    onCameraPermissionDenied() {
      this.availableCameras = [];
    },
  },
  watch: {
    selectedCameraId(selectedCameraId) {
      this.$emit('camera-selected', selectedCameraId);
    },
  },
};
</script>
