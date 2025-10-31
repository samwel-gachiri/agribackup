import Vue from 'vue';
import VueMeta from 'vue-meta';
import VueCookies from 'vue-cookies';
// eslint-disable-next-line import/extensions
import store from '@/store';
import initialize from '@/utils/initialize.js';
import VueIziToast from 'vue-izitoast';
import InfiniteLoading from 'vue-infinite-loading';
import '@/assets/css/tailwind.css';
import '@/assets/css/color/default.css';
import '@/assets/css/nprogress.css';
import '@/assets/css/style.css';
import 'izitoast/dist/css/iziToast.min.css';
import 'leaflet/dist/leaflet.css';
import 'vue-phone-number-input/dist/vue-phone-number-input.css';

import helperMixins from '@/mixins/helperMixins.js';
// import VuePhoneNumberInput from '@/components/layout/components/PhoneNumberInput.vue';

import Avatar from 'vue-avatar';
// eslint-disable-next-line import/extensions
import router from './router';

import vuetify from './plugins/vuetify.js';
import './plugins/vuetify-money.js';

import App from './App.vue';
import './registerServiceWorker.js';

Vue.config.productionTip = false;

Vue.mixin(helperMixins);

Vue.use(VueCookies);
Vue.use(VueMeta, {
  refreshOnceOnNavigation: true,
  keyName: 'head',
  attribute: 'data-vue-meta',
});
Vue.$cookies.config('7d', null, null, true, 'Strict');

// Vue.component('phone-number-input', VuePhoneNumberInput);
// Vue.component('VueTelInput', VueTelInput);
Vue.use(VueIziToast, { position: 'topRight', timeout: 5000 });
Vue.use(InfiniteLoading);

Vue.use(Avatar);

initialize.init(router, store);
new Vue({
  vuetify,
  router,
  store,
  render: (h) => h(App),
}).$mount('#app');
