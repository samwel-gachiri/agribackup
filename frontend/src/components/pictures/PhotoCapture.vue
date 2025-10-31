<template>
  <div v-if="hasCameraPermission && hasLocationPermission" class="tw-fixed tw-inset-0 tw-bg-black tw-z-50 tw-overflow-hidden tw-w-screen tw-h-screen">
    <!-- Camera Preview (fills screen) -->
    <video
      ref="videoElement"
      v-show="showCamera"
      autoplay
      playsinline
      class="tw-absolute tw-inset-0 tw-w-full tw-h-full tw-object-cover tw-bg-black"
      style="z-index:1"
    ></video>
    <canvas ref="canvasElement" style="display: none;"></canvas>

    <!-- Top bar: Close and Switch Camera -->
    <div class="tw-absolute tw-top-0 tw-left-0 tw-w-full tw-flex tw-justify-between tw-items-center tw-p-4 tw-z-20">
      <button @click.prevent="closeCamera" aria-label="Close" class="tw-bg-black/60 tw-rounded-full tw-p-2 hover:tw-bg-black/80 tw-transition">
        <svg class="tw-w-6 tw-h-6 tw-text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
        </svg>
      </button>
      <button @click.prevent="switchToNextCamera" aria-label="Switch Camera" class="tw-bg-black/60 tw-rounded-full tw-p-2 hover:tw-bg-black/80 tw-transition">
        <svg class="tw-w-6 tw-h-6 tw-text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>
    </div>

    <!-- Error message (centered) -->
    <div v-if="cameraError" class="tw-absolute tw-top-1/2 tw-left-1/2 tw-transform -tw-translate-x-1/2 -tw-translate-y-1/2 tw-bg-black/80 tw-text-white tw-p-4 tw-rounded tw-z-30">
      {{ cameraError }}
    </div>

    <!-- Captured Photos Gallery (bottom, horizontal scroll, overlays video) -->
    <div
      v-if="capturedPhotos.length"
      class="tw-absolute tw-bottom-28 md:tw-bottom-32 tw-left-0 tw-w-full tw-overflow-x-auto tw-px-4 tw-py-2 tw-bg-black/40 tw-flex tw-space-x-2 tw-items-center tw-justify-start tw-z-20"
      style="backdrop-filter: blur(2px);"
    >
      <div v-for="photo in capturedPhotos" :key="photo.id" class="tw-relative">
        <img :src="photo.url" class="tw-h-16 tw-w-16 tw-object-cover tw-rounded tw-border-2 tw-border-white tw-shadow" alt="Captured photo" />
        <button @click.prevent="deletePhoto(photo.id)" class="tw-absolute tw-top-0 tw-right-0 tw-bg-black/70 tw-rounded-full tw-p-1 tw-m-1 hover:tw-bg-red-600" aria-label="Delete photo">
          <svg class="tw-w-4 tw-h-4 tw-text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- Bottom Controls (always visible, overlays video, safe area for mobile) -->
    <div
      class="tw-absolute tw-bottom-0 tw-left-0 tw-w-full tw-flex tw-justify-center tw-items-center tw-mb-4 tw-py-2 tw-bg-gradient-to-t tw-from-black/80 tw-to-transparent tw-z-30"
      style="padding-bottom: env(safe-area-inset-bottom, 1.5rem);"
    >
      <button
        @click.prevent="capturePhoto"
        :disabled="!currentLocation"
        aria-label="Capture Photo"
        class="tw-bg-white tw-rounded-full tw-w-16 tw-h-16 tw-flex tw-items-center tw-justify-center tw-shadow-lg tw-border-4 tw-border-gray-300 tw-outline-none focus:tw-ring-2 focus:tw-ring-blue-400 tw-transition"
      >
        <svg class="tw-w-10 tw-h-10 tw-text-gray-800" fill="currentColor" viewBox="0 0 24 24">
          <circle cx="12" cy="12" r="10"/>
        </svg>
      </button>
      <button
        v-if="capturedPhotos.length"
        @click.prevent="submitPhotos"
        aria-label="Submit Photos"
        class="tw-ml-8 tw-bg-blue-600 tw-text-white tw-rounded-full tw-px-6 tw-py-3 tw-shadow hover:tw-bg-blue-700 tw-transition"
      >
        Submit
      </button>
    </div>
  </div>

  <!-- Permissions UI (centered) -->
  <div v-else class="tw-fixed tw-inset-0 tw-bg-black tw-flex tw-flex-col tw-items-center tw-justify-center tw-z-50">
    <div class="tw-bg-white tw-rounded-xl tw-shadow-lg tw-p-8 tw-flex tw-flex-col tw-items-center tw-space-y-6 tw-w-80">
      <div>
        <svg v-if="permissionStates.camera !== 'granted'" class="tw-w-12 tw-h-12 tw-text-yellow-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none"/>
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4m0 4h.01"/>
        </svg>
        <svg v-else class="tw-w-12 tw-h-12 tw-text-green-500" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" fill="none"/>
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4"/>
        </svg>
      </div>
      <div class="tw-text-center">
        <h2 class="tw-text-lg tw-font-semibold tw-mb-2">Camera & Location Permissions</h2>
        <p class="tw-text-gray-600 tw-mb-2">To take photos, please allow camera and location access.</p>
        <div class="tw-flex tw-justify-center tw-space-x-4 tw-mt-2">
          <span class="tw-text-xs tw-font-medium" :class="permissionStates.camera === 'granted' ? 'tw-text-green-600' : 'tw-text-yellow-600'">
            Camera: {{ permissionStates.camera }}
          </span>
          <span class="tw-text-xs tw-font-medium" :class="permissionStates.geolocation === 'granted' ? 'tw-text-green-600' : 'tw-text-yellow-600'">
            Location: {{ permissionStates.geolocation }}
          </span>
        </div>
      </div>
      <button
        @click.prevent="requestPermissions"
        :disabled="loading"
        class="tw-bg-blue-600 tw-text-white tw-rounded-full tw-px-6 tw-py-3 tw-shadow hover:tw-bg-blue-700 tw-transition tw-w-full"
      >
        {{ loading ? 'Requesting...' : 'Request Permissions' }}
      </button>
      <div v-if="locationError" class="tw-text-red-600 tw-text-xs tw-mt-2">{{ locationError }}</div>
    </div>
  </div>
</template>

<script>
// filepath: g:\documents\computer science\4.1\SCO400 - Project\MyProject\Application\frontend\farmer-portal-frontend\src\components\pictures\PhotoCapture.vue
import piexif from 'piexifjs';
import { getCurrentUserId } from '@/utils/roles.js';

export default {
  name: 'PhotoCapture',
  data() {
    return {
      hasLocationPermission: false,
      hasCameraPermission: false,
      currentLocation: null,
      locationError: null,
      stream: null,
      capturedPhotos: [],
      isCapturing: false,
      showCamera: false,
      loading: false,
      permissionStates: {
        camera: 'prompt',
        geolocation: 'prompt',
      },
      selectedCameraId: null,
      cameraDevices: [],
      checkingPermissions: true,
      cameraError: null,
      watchId: null,
    };
  },
  mounted() {
    this.checkPermissionsOnLoad();
  },
  methods: {
    async requestPermissions() {
      this.loading = true;
      this.locationError = null;
      try {
        try {
          await this.requestLocationPermission();
        } catch (locErr) {
          this.locationError = locErr.message;
          this.hasLocationPermission = false;
          this.permissionStates.geolocation = 'denied';
        }
        try {
          await this.requestCameraPermission();
        } catch (camErr) {
          // Camera permission denied or error occurred; handled by permissionStates update above.
        }
        if (this.hasLocationPermission) {
          try {
            this.startLocationTracking();
          } catch (trackErr) {
            this.$toast.error(`Location tracking error: ${trackErr.message}`);
          }
        }
      } catch (error) {
        this.$toast.error(`Permission error: ${error.message}`);
        this.locationError = error.message;
      } finally {
        this.loading = false;
        if (this.hasLocationPermission && this.hasCameraPermission) {
          await this.startCamera();
        }
      }
    },
    async requestLocationPermission() {
      return new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
          reject(new Error('Geolocation is not supported by this browser'));
          return;
        }
        try {
          navigator.geolocation.getCurrentPosition(
            (position) => {
              this.hasLocationPermission = true;
              this.permissionStates.geolocation = 'granted';
              this.currentLocation = {
                latitude: position.coords.latitude,
                longitude: position.coords.longitude,
                accuracy: position.coords.accuracy,
                timestamp: new Date(),
              };
              resolve();
            },
            (error) => {
              let errorMessage = 'Location access denied';
              // eslint-disable-next-line default-case
              switch (error.code) {
                case error.PERMISSION_DENIED:
                  errorMessage = 'Location access denied by user';
                  break;
                case error.POSITION_UNAVAILABLE:
                  errorMessage = 'Location information unavailable';
                  break;
                case error.TIMEOUT:
                  errorMessage = 'Location request timed out';
                  break;
              }
              this.hasLocationPermission = false;
              this.permissionStates.geolocation = 'denied';
              reject(new Error(errorMessage));
            },
            {
              enableHighAccuracy: true,
              timeout: 10000,
              maximumAge: 0,
            },
          );
        } catch (err) {
          this.hasLocationPermission = false;
          this.permissionStates.geolocation = 'denied';
          reject(new Error('Unexpected geolocation error'));
        }
      });
    },
    async requestCameraPermission() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({
          video: { facingMode: 'environment' },
        });
        stream.getTracks().forEach((track) => track.stop());
        this.hasCameraPermission = true;
        this.permissionStates.camera = 'granted';
        await this.getAvailableCameras();
      } catch (error) {
        this.hasCameraPermission = false;
        this.permissionStates.camera = 'denied';
        throw new Error('Camera access denied or unavailable');
      }
    },
    async getAvailableCameras() {
      try {
        const devices = await navigator.mediaDevices.enumerateDevices();
        this.cameraDevices = devices.filter((d) => d.kind === 'videoinput');
        if (!this.selectedCameraId && this.cameraDevices.length) {
          this.selectedCameraId = this.cameraDevices[0].deviceId;
        }
      } catch (e) {
        this.cameraDevices = [];
      }
    },
    async switchToNextCamera() {
      if (!this.cameraDevices.length) return;
      const idx = this.cameraDevices.findIndex((d) => d.deviceId === this.selectedCameraId);
      const nextIdx = (idx + 1) % this.cameraDevices.length;
      this.selectedCameraId = this.cameraDevices[nextIdx].deviceId;
      await this.startCamera();
    },
    async startCamera() {
      try {
        const constraints = {
          video: {
            width: { ideal: 1920 },
            height: { ideal: 1080 },
            deviceId: this.selectedCameraId ? { exact: this.selectedCameraId } : undefined,
            facingMode: this.selectedCameraId ? undefined : 'environment',
          },
        };
        if (this.stream) {
          this.stream.getTracks().forEach((track) => track.stop());
        }
        this.stream = await navigator.mediaDevices.getUserMedia(constraints);
        this.$refs.videoElement.srcObject = this.stream;
        this.showCamera = true;
        this.cameraError = null;
      } catch (error) {
        this.cameraError = `Failed to start camera: ${error.message}`;
      }
    },
    closeCamera() {
      this.stopCamera();
      // Optionally emit an event to parent to close the modal/view
      this.$emit('close');
    },
    stopCamera() {
      if (this.stream) {
        this.stream.getTracks().forEach((track) => track.stop());
        this.stream = null;
      }
      this.showCamera = false;
    },
    startLocationTracking() {
      if (!navigator.geolocation) return;
      if (this.watchId) {
        try {
          navigator.geolocation.clearWatch(this.watchId);
        } catch (e) {
          // Intentionally ignored: error clearing geolocation watch
        }
      }
      try {
        this.watchId = navigator.geolocation.watchPosition(
          (position) => {
            this.currentLocation = {
              latitude: position.coords.latitude,
              longitude: position.coords.longitude,
              accuracy: position.coords.accuracy,
              timestamp: new Date(),
            };
          },
          (error) => {
            this.$toast.error(`Location tracking error: ${error.message}`);
          },
          {
            enableHighAccuracy: true,
            timeout: 5000,
            maximumAge: 30000,
          },
        );
      } catch (err) {
        this.$toast.error(`Location tracking error: ${err.message}`);
      }
    },
    async capturePhoto() {
      if (!this.currentLocation) {
        this.$toast.error('Location not available');
        return;
      }
      if (Math.abs(this.currentLocation.latitude) > 90 || Math.abs(this.currentLocation.longitude) > 180) {
        this.$toast.error('Invalid GPS coordinates');
        return;
      }
      const video = this.$refs.videoElement;
      const canvas = this.$refs.canvasElement;
      const context = canvas.getContext('2d');
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      context.drawImage(video, 0, 0, canvas.width, canvas.height);
      canvas.toBlob(async (blob) => {
        if (blob) {
          this.capturedPhotos.push(await this.addGeotagToPhoto(blob));
        }
      }, 'image/jpeg', 0.9);
    },
    async addGeotagToPhoto(photoBlob) {
      try {
        const arrayBuffer = await photoBlob.arrayBuffer();
        const photoData = new Uint8Array(arrayBuffer);
        let binary = '';
        // eslint-disable-next-line no-plusplus
        for (let i = 0; i < photoData.length; i++) {
          binary += String.fromCharCode(photoData[i]);
        }
        const base64String = btoa(binary);
        const dataUrl = `data:image/jpeg;base64,${base64String}`;
        const lat = Math.max(-90, Math.min(90, this.currentLocation.latitude));
        const lon = Math.max(-180, Math.min(180, this.currentLocation.longitude));
        const exifDict = {
          '0th': {
            [piexif.ImageIFD.Make]: 'Web Browser',
            [piexif.ImageIFD.Model]: navigator.userAgent.split(' ')[0],
            [piexif.ImageIFD.Software]: 'Vue Geotagged Camera',
            [piexif.ImageIFD.Artist]: getCurrentUserId(),
          },
          Exif: {
            [piexif.ExifIFD.DateTimeOriginal]: this.getExifDateTime(),
            [piexif.ExifIFD.DateTimeDigitized]: this.getExifDateTime(),
          },
          GPS: {
            [piexif.GPSIFD.GPSVersionID]: [7, 7, 7, 7],
            [piexif.GPSIFD.GPSLatitudeRef]: lat >= 0 ? 'N' : 'S',
            [piexif.GPSIFD.GPSLatitude]: piexif.GPSHelper.degToDmsRational(lat),
            [piexif.GPSIFD.GPSLongitudeRef]: lon >= 0 ? 'E' : 'W',
            [piexif.GPSIFD.GPSLongitude]: piexif.GPSHelper.degToDmsRational(lon),
          },
        };
        const exifBytes = piexif.dump(exifDict);
        const newDataUrl = piexif.insert(exifBytes, dataUrl);
        const newBlob = await this.dataURLToBlob(newDataUrl);
        return {
          id: Date.now(),
          blob: newBlob,
          url: URL.createObjectURL(newBlob),
          location: { ...this.currentLocation },
          timestamp: new Date().toISOString(),
          size: newBlob.size,
          hasEmbeddedGPS: true,
          exif: {
            latitude: this.currentLocation.latitude,
            longitude: this.currentLocation.longitude,
            accuracy: this.currentLocation.accuracy,
            timestamp: this.currentLocation.timestamp,
          },
        };
      } catch (error) {
        this.$toast.error(`Error embedding GPS EXIF data: ${error.message}`);
        return this.createSimpleGeotaggedPhoto(photoBlob);
      }
    },
    async dataURLToBlob(dataURL) {
      const response = await fetch(dataURL);
      // eslint-disable-next-line no-return-await
      return await response.blob();
    },
    createSimpleGeotaggedPhoto(photoBlob) {
      return {
        id: Date.now(),
        blob: photoBlob,
        url: URL.createObjectURL(photoBlob),
        location: { ...this.currentLocation },
        timestamp: new Date().toISOString(),
        size: photoBlob.size,
        hasEmbeddedGPS: false,
        exif: {
          latitude: this.currentLocation.latitude,
          longitude: this.currentLocation.longitude,
          accuracy: this.currentLocation.accuracy,
          timestamp: this.currentLocation.timestamp,
        },
      };
    },
    deletePhoto(photoId) {
      const index = this.capturedPhotos.findIndex((photo) => photo.id === photoId);
      if (index > -1) {
        URL.revokeObjectURL(this.capturedPhotos[index].url);
        this.capturedPhotos.splice(index, 1);
      }
    },
    submitPhotos() {
      this.stopCamera();
      this.$emit('captured-photos', this.capturedPhotos);
    },
    getExifDateTime() {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');
      return `${year}:${month}:${day} ${hours}:${minutes}:${seconds}`;
    },
    async checkPermissionsOnLoad() {
      this.checkingPermissions = true;
      await this.checkCameraPermission();
      await this.checkLocationPermission();
      if (this.permissionStates.camera === 'granted' && this.permissionStates.geolocation === 'granted') {
        this.hasLocationPermission = true;
        this.hasCameraPermission = true;
        await this.getAvailableCameras();
        await this.startCamera();
        this.startLocationTracking();
      }
      this.checkingPermissions = false;
    },
    async checkCameraPermission() {
      try {
        if ('permissions' in navigator) {
          const permission = await navigator.permissions.query({ name: 'camera' });
          this.permissionStates.camera = permission.state;
          permission.onchange = () => {
            this.permissionStates.camera = permission.state;
            this.hasCameraPermission = permission.state === 'granted';
            if (permission.state === 'granted') {
              this.getAvailableCameras();
            } else if (permission.state === 'denied') {
              this.stopCamera();
            }
          };
          if (permission.state === 'granted') {
            await this.getAvailableCameras();
            this.hasCameraPermission = true;
          }
        } else {
          try {
            const stream = await navigator.mediaDevices.getUserMedia({ video: true });
            stream.getTracks().forEach((track) => track.stop());
            this.permissionStates.camera = 'granted';
            this.hasCameraPermission = true;
            await this.getAvailableCameras();
          } catch (error) {
            this.permissionStates.camera = 'denied';
            this.hasCameraPermission = false;
          }
        }
      } catch (error) {
        this.$toast.error(`Camera permission check failed: ${error.message}`);
        this.permissionStates.camera = 'denied';
      }
    },
    async checkLocationPermission() {
      try {
        if ('permissions' in navigator) {
          const permission = await navigator.permissions.query({ name: 'geolocation' });
          this.permissionStates.geolocation = permission.state;
          permission.onchange = () => {
            this.permissionStates.geolocation = permission.state;
            this.hasLocationPermission = permission.state === 'granted';
            if (permission.state === 'denied') {
              this.currentLocation = null;
              if (this.watchId) {
                try {
                  navigator.geolocation.clearWatch(this.watchId);
                } catch (e) {
                  // Intentionally ignored: error clearing geolocation watch
                }
              }
            }
          };
          if (permission.state === 'granted') {
            this.hasLocationPermission = true;
          }
        } else {
          await this.getLocation();
        }
      } catch (error) {
        this.$toast.error(`Location permission check failed: ${error.message}`);
        this.permissionStates.geolocation = 'denied';
        this.hasLocationPermission = false;
      }
    },
    async getLocation() {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          this.permissionStates.geolocation = 'granted';
          this.hasLocationPermission = true;
          this.currentLocation = {
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            accuracy: position.coords.accuracy,
            timestamp: new Date(),
          };
          this.startLocationTracking();
        },
        (error) => {
          this.permissionStates.geolocation = error.code === 1 ? 'denied' : 'prompt';
          this.hasLocationPermission = false;
        },
        { timeout: 1000 },
      );
    },
  },
  beforeDestroy() {
    this.stopCamera();
    if (this.watchId) {
      navigator.geolocation.clearWatch(this.watchId);
    }
    this.capturedPhotos.forEach((photo) => {
      URL.revokeObjectURL(photo.url);
    });
  },
};
</script>

<style scoped>
/* Hide scrollbars for gallery */
::-webkit-scrollbar {
  display: none;
}
</style>
