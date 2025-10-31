<template>
  <div class="tw-flex tw--mt-4 md:tw--mt-3">
    <div class="tw-w-8 tw-h-8 md:tw-w-12 md:tw-h-12  tw-flex tw-justify-center tw-items-center tw-rounded-lg primary">
      <v-icon v-if="!isImage" color="white" size="30" >{{ icon }}</v-icon>
      <v-img v-else src='https://cdn.vuetifyjs.com/images/parallax/material.jpg'></v-img>
    </div>
    <div class="tw-ml-5 tw-pt-4">
      <h2 class="tw-text-lg md:tw-text-xl c-green-text title-font"><slot></slot></h2>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CardTitle',
  props: {
    isImage: {
      type: Boolean,
      default: false,
    },
    icon: {
      type: String,
      default: 'mdi-corn',
    },
  },
};
</script>
