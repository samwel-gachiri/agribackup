<template>
  <div id="app">
<!--    <img alt="Vue logo" class="tw-h-1/4" src="./assets/logo.png">-->
    <router-view/>
  </div>
</template>

<script>
import 'bootstrap/dist/css/bootstrap.min.css';

export default {
  name: 'App',
};
</script>
<style>
</style>
<!--<script setup>-->
<!--import { ref, onMounted } from 'vue';-->
<!--import { supabase } from './lib/supabaseClient.js';-->

<!--const instruments = ref([]);-->

<!--async function getInstruments() {-->
<!--  const { data } = await supabase.from('instruments').select();-->
<!--  instruments.value = data;-->
<!--}-->

<!--onMounted(() => {-->
<!--  getInstruments();-->
<!--});-->
<!--</script>-->

<!--<template>-->
<!--  <ul>-->
<!--    <li v-for="instrument in instruments" :key="instrument.id">{{ instrument.name }}</li>-->
<!--  </ul>-->
<!--</template>-->
