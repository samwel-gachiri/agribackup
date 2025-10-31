<template>
  <div class="tw-relative tw-w-32 tw-h-32 tw-rounded-lg tw-overflow-hidden tw-bg-black tw-shadow">
    <!-- Main photo image -->
    <img
      :src="photo.url"
      alt="Photo"
      class="tw-w-full tw-h-full tw-object-cover tw-cursor-pointer"
      @click="$emit('view-location', photo)"
      draggable="false"
    />

    <!-- Location indicator overlay -->
    <div class="tw-absolute tw-top-2 tw-right-2">
      <span class="tw-bg-green-600 tw-text-white tw-text-xs tw-px-2 tw-py-1 tw-rounded tw-flex tw-items-center tw-gap-1">
        <svg class="tw-w-3 tw-h-3" fill="white" stroke="black" stroke-width="2" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
          <circle cx="12" cy="9" r="2.5" fill="white" stroke="black" stroke-width="1"/>
        </svg>
        GPS
      </span>
    </div>

    <!-- Actions overlay (bottom) -->
    <div class="tw-absolute tw-bottom-0 tw-left-0 tw-w-full tw-bg-black/60 tw-p-2 tw-flex tw-items-center tw-justify-between">
      <button
        type="button"
        @click.stop.prevent="$emit('view-location', photo)"
        title="View location"
        class="tw-bg-transparent tw-text-white tw-p-1 tw-rounded hover:tw-bg-white/20"
      >
        <svg class="tw-w-5 tw-h-5" fill="white" stroke="black" stroke-width="2" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
          <circle cx="12" cy="9" r="2.5" fill="white" stroke="black" stroke-width="1"/>
        </svg>
      </button>
      <button
        type="button"
        @click.stop.prevent="downloadPhoto"
        title="Download"
        class="tw-bg-transparent tw-text-white tw-p-1 tw-rounded hover:tw-bg-white/20"
      >
        <svg class="tw-w-5 tw-h-5" fill="white" stroke="black" stroke-width="2" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/>
        </svg>
      </button>
      <button
        type="button"
        @click.stop="$emit('delete', photo.id)"
        title="Delete"
        class="tw-bg-transparent tw-text-white tw-p-1 tw-rounded hover:tw-bg-red-600"
      >
        <svg class="tw-w-5 tw-h-5" fill="white" stroke="black" stroke-width="2" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PhotoCard',
  props: {
    photo: {
      type: Object,
      required: true,
    },
  },
  methods: {
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString();
    },
    formatLocation(location) {
      return `${location.latitude.toFixed(4)}, ${location.longitude.toFixed(4)}`;
    },
    formatFileSize(bytes) {
      if (bytes === 0) return '0 Bytes';
      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      // eslint-disable-next-line no-restricted-properties
      return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
    },
    downloadPhoto() {
      const link = document.createElement('a');
      link.href = this.photo.url;
      link.download = `geotagged-photo-${this.photo.id}.jpg`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
  },
};
</script>

<style scoped>
/* Add any component-specific styles here */
</style>
