import Vue from 'vue';
import vuetifyMoney from 'vuetify-money';

Vue.use(vuetifyMoney, {
  locale: 'en-US',
  prefix: 'Ksh. ',
  precision: 2,
});

Vue.component('v-money', vuetifyMoney);
