<template>
  <v-app content="center" class="tw-h-screen tw-flex tw-items-center">
    <div class="tw-w-full tw-flex tw-justify-center tw-flex-col tw-py-10 md:tw-py-16">
      <h2 class="tw-text-center tw-py-6 tw-text-3xl md:tw-text-6xl c-white-text">Ooops!</h2>
      <div class=" tw-flex tw-w-full tw-bg-green-400 tw-justify-center">
        <v-icon class="t" size="100px">mdi-alert-outline</v-icon>
      </div>
      <div class="tw-flex tw-justify-center tw-flex-col tw-text-center">
        <h4 class="tw-py-6 tw-text-2xl md:tw-text-4xl">The page you're looking for couldn't be found</h4>
        <v-btn :to="{ name: 'SignIn' }" class="tw-w-48 tw-self-center tw-mt-6" color="secondary">Back To Homepage</v-btn>
      </div>
    </div>
  </v-app>
</template>

<script>
export default {
  name: 'Error-404x',
};
</script>

<style scoped>
.bg-gradient-blue {
  background: linear-gradient(-180deg, rgb(17, 76, 172) 0%, rgb(6, 40, 96) 100%);
}
</style>
