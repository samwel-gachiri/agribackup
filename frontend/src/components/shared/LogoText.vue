<script>
export default {
  name: 'LogoTitle',
  props: {
    text: {
      type: String,
      default: 'AgriBackup',
    },
  },
};
</script>

<template>
  <div class="tw-flex tw-justify-start tw-items-center tw-flex-row tw-cursor-pointer" @click="()=>this.$router.push({path: '/'})">
    <img
        src="@/assets/images/logo.png"
        alt="Company Logo"
        class="tw-border tw-rounded-lg tw-ml-2"
        width="40px"
        height="40px"
        loading="lazy"
    />
    <v-divider vertical/>
    <div class="tw-flex tw-justify-center tw-items-center tw-flex-col tw-px-4">
      <h4 class="tw-text-md c-green-text tw-font-bold">
        {{text}}
      </h4>
      <slot></slot>
    </div>
  </div>
</template>

<style scoped>

</style>
