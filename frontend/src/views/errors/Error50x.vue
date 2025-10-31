<template>
  <v-app content="center" class="bg-gradient-blue tw-h-screen tw-flex tw-items-center">
    <div class="tw-w-full tw-flex tw-justify-center tw-flex-col tw-py-10 md:tw-py-16">
      <h2 class="tw-text-center tw-py-3 tw-text-3xl md:tw-text-6xl c-white-text">Sorry!</h2>
      <h2 class="tw-text-center tw-py-2 tw-text-2xl md:tw-text-4xl c-white-text">Well, this is unexpected ...</h2>
      <div class="tw-flex tw-justify-center">
        <img width="500" src="/images/50x.svg" alt="50x">
      </div>
      <div class="tw-flex tw-justify-center tw-flex-col tw-text-center">
        <h5 class="tw-py-6 tw-text-xl md:tw-text-2xl c-white-text">An error has occurred and your request couldn't be completed.
          <br>
          Go back and try again and the If error persists, <br>
          Either check the URL, <router-link class="tw-text-yellow-500" to="/">go home</router-link>, or feel free to report the issue to <strong>0705644805</strong>.
        </h5>
        <v-btn @click="$router.go(-1)" class="tw-w-48 tw-self-center tw-mt-6" color="secondary"><v-icon color="#fff">mdi-chevron-left</v-icon> Go Back</v-btn>
      </div>
    </div>
  </v-app>
</template>

<script>
export default {
  name: 'Error-50x',
};
</script>

<style scoped>
.bg-gradient-blue {
  background: linear-gradient(-180deg, rgb(17, 76, 172) 0%, rgb(6, 40, 96) 100%);
}
</style>
